package org.greenstone.gsdl3.action;

import org.greenstone.gsdl3.core.ModuleInterface;
import org.greenstone.gsdl3.util.*;
import org.greenstone.util.GlobalProperties;

// XML classes
import org.w3c.dom.Node; 
import org.w3c.dom.Element; 
import org.w3c.dom.Document; 

// other java stuff
import java.io.File;
import java.util.HashMap;
import java.util.*;
import java.io.StringReader;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.*;
import org.xml.sax.*;
import javax.xml.parsers.SAXParserFactory; 
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.*;

public class FormatAction extends Action {
    
    static Logger logger = Logger.getLogger(org.greenstone.gsdl3.action.FormatAction.class.getName());
    XMLTransformer transformer = null;

    String tempVal = "";

    /** process a request */
    public Node process (Node message_node) {
	
	    Element message = this.converter.nodeToElement(message_node);

    	// assume only one request
	    Element request = (Element)GSXML.getChildByTagName(message, GSXML.REQUEST_ELEM);
	
    	String subaction = request.getAttribute(GSXML.SUBACTION_ATT);
	    String lang = request.getAttribute(GSXML.LANG_ATT);
    	String uid = request.getAttribute(GSXML.USER_ID_ATT);
	    // get the param list
    	Element cgi_param_list = (Element)GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
	    HashMap params = GSXML.extractParams(cgi_param_list, false);

    	Element result = this.doc.createElement(GSXML.MESSAGE_ELEM);
	
	    String coll = (String)params.get(GSParams.COLLECTION); //SYSTEM_CLUSTER);
        String service = (String)params.get(GSParams.SERVICE);
        String classifier = (String)params.get("cl");

        String to = "";
        if (coll!=null && !coll.equals("")) {
            to = coll;
        }

        Element mr_request_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
        Element mr_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_FORMAT_STRING, to, lang, uid);

        mr_request.setAttribute("service", service);
        mr_request.setAttribute("subaction", subaction);
        //if(classifier != null)
        mr_request.setAttribute("classifier", classifier);

        mr_request_message.appendChild(mr_request);
	
        String format_string = (String)params.get("data");
    
        Element page_response = this.doc.createElement(GSXML.RESPONSE_ELEM);

        Iterator it = params.keySet().iterator();

        if(subaction.equals("saveDocument"))
        {
            Element format = this.doc.createElement(GSXML.FORMAT_STRING_ELEM);
            try{
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                InputSource is = new InputSource( new StringReader( format_string ) );
                Document d = (Document) builder.parse( is );
                Node n1 = d.getFirstChild();

                Element format_statement = (Element) this.doc.importNode(n1, true);
                format.appendChild(format_statement);
                mr_request.appendChild(format);
            } catch( Exception ex ) {
                logger.error("There was an exception "+ex);

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw, true);
                ex.printStackTrace(pw);
                pw.flush();
                sw.flush();
                logger.error(sw.toString());
            }

        }

        else
        {

            try {
                Document d = this.converter.getDOM(format_string); 

                // Call XSLT to transform document to xml format string
                XMLTransformer transformer = new XMLTransformer();
                String style = GSFile.interfaceStylesheetFile(GlobalProperties.getGSDL3Home(),(String)this.config_params.get(GSConstants.INTERFACE_NAME), "formatString.xsl");
                //logger.error("Style doc is "+style+", compared to /research/sjb48/greenstone3/web/interfaces/oran/transform/formatString.xsl");
                Document style_doc = this.converter.getDOM(new File(style), "UTF-8");
                //Document style_doc = this.converter.getDOM(new File("/research/sjb48/greenstone3/web/interfaces/oran/transform/formatString.xsl"), "UTF-8");  /*************************/

                if(style_doc == null)
                    logger.error("style_doc is null");

                logger.error("About to transform");
                Node transformed = (Node) transformer.transform(style_doc, d);  
                
                if(transformed.getNodeType() == Node.DOCUMENT_NODE)
                    transformed = ((Document)transformed).getDocumentElement(); 

                Element format = this.doc.createElement(GSXML.FORMAT_STRING_ELEM);
                format.appendChild(this.doc.importNode(transformed,true));
                mr_request.appendChild(format); 
                logger.error("Transformed: "+transformed);

            } catch( Exception ex ) {
                logger.error("There was an exception "+ex);

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw, true);
                ex.printStackTrace(pw);
                pw.flush();
                sw.flush();
                logger.error(sw.toString());
            }
        }

        Node response_message = this.mr.process(mr_request_message);
	
        result.appendChild(GSXML.duplicateWithNewName(this.doc, (Element)GSXML.getChildByTagName(response_message, GSXML.RESPONSE_ELEM), GSXML.RESPONSE_ELEM, true));
        return result;
	
    }

}
