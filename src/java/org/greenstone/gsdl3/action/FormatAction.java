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
    //String subaction = (String)params.get(GSParams.SUBACTION);
    String service = (String)params.get(GSParams.SERVICE);
    String classifier = (String)params.get("cl");


    logger.error("Collection="+coll);
    logger.error("Subaction="+subaction);
    logger.error("Service="+service);
    logger.error("Classifier="+classifier);


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
	
	//Element format = this.doc.createElement(GSXML.FORMAT_STRING_ELEM);
	//mr_request.appendChild(format);

    String format_string = (String)params.get("data");
    //logger.error("Original format string");
    //logger.error(format_string);
    
    Element page_response = this.doc.createElement(GSXML.RESPONSE_ELEM);

    Iterator it = params.keySet().iterator();
    //while(it.hasNext())
    //{
    //    logger.error("Param: "+it.next());
    //}      

    try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            //String input = "<html><head><title></title></head><body>" + format_string + "</body></html>";
            String input = format_string;
            InputSource is = new InputSource( new StringReader( input ) );
            Document d = (Document) builder.parse( is );
            //Node n1 = d.getFirstChild();            
            //Document d2 = (Document) this.doc.importNode(e, true);

            //Element format_statement = this.doc.importNode(d, true);

            // Call XSLT to transform document to xml format string
            XMLTransformer transformer = new XMLTransformer();
            // HOW DO I DO THIS PROPERLY?
            Document style_doc = this.converter.getDOM(new File("/home/sam/greenstone3/web/interfaces/oran/transform/formatString.xsl"), "UTF-8");

            if(style_doc == null)
                logger.error("style_doc is null");

            // not sure what to do here - some code from Transforming Receptionist
            String transformed = transformer.transformToString(style_doc, d);
            logger.error("About to transform");
            //Node transformed = (Node) transformer.transform(style_doc, d);  // Failing org.w3c.dom.DOMException: HIERARCHY_REQUEST_ERR: An attempt was made to insert a node where it is not permitted. ; SystemID: file:///home/sam/greenstone3/packages/tomcat/bin/dummy.xsl

            logger.error("Transform successful?");
         
            if(transformed==null)  // not null
                logger.error("TRANSFORMED IS NULL");

            //logger.error("begin import"); 
            //Node imported = this.doc.importNode(transformed, true); // There was an exception org.w3c.dom.DOMException: NOT_SUPPORTED_ERR: The implementation does not support the requested type of object or operation.
            //logger.error("finished import"); 

            //String format_string2 = GSXML.xmlNodeToString(imported); // null pointer exception occuring here
            //logger.error("format string="+format_string2);
 
            Element format = this.doc.createElement(GSXML.FORMAT_STRING_ELEM);
            GSXML.setNodeText(format, transformed);
            //format.appendChild(transformed);
            //format.setNodeValue(transformed);
            mr_request.appendChild(format); 
            logger.error("Transformed: "+transformed);


            //page_response.appendChild(this.doc.importNode(e, true));
    } catch( Exception ex ) {
            logger.error("There was an exception "+ex);
            
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw, true);
            ex.printStackTrace(pw);
            pw.flush();
            sw.flush();
            logger.error(sw.toString());
    }


    // Call XSLT to transform document to xml format string
    //XMLTransformer transformer = new XMLTransformer();

    // not sure what to do here - some code from Transforming Receptionist
    //transformer.transformToString(Document stylesheet, Document source);

    // create a mesage to send to the collection object via the message router

	Node response_message = this.mr.process(mr_request_message);
	
	result.appendChild(GSXML.duplicateWithNewName(this.doc, (Element)GSXML.getChildByTagName(response_message, GSXML.RESPONSE_ELEM), GSXML.RESPONSE_ELEM, true));
	return result;
	
    }

}
