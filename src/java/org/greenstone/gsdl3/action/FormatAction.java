package org.greenstone.gsdl3.action;

import org.greenstone.gsdl3.core.ModuleInterface;
import org.greenstone.gsdl3.util.*;

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

	String to = "";
	if (coll!=null && !coll.equals("")) {
	    to = coll;
	}

	Element mr_request_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
	Element mr_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_FORMAT_STRING, to, lang, uid);
	mr_request_message.appendChild(mr_request);
	
	Element format = this.doc.createElement(GSXML.FORMAT_STRING_ELEM);
	mr_request.appendChild(format);

    String format_string = (String)params.get("data");
    
    Element page_response = this.doc.createElement(GSXML.RESPONSE_ELEM);

    Iterator it = params.keySet().iterator();
    while(it.hasNext())
    {
        logger.error("Param: "+it.next());
    }      

    try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            //String input = "<html><head><title></title></head><body>" + format_string + "</body></html>";
            String input = format_string;
            InputSource is = new InputSource( new StringReader( input ) );
            Document d = builder.parse( is );
            Element e = d.getDocumentElement();
            
            page_response.appendChild(this.doc.importNode(e, true));
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
    this.transformer = new XMLTransformer();

    // not sure what to do here - some code from Transforming Receptionist

    // create a mesage to send to the collection object via the message router

	Node response_message = this.mr.process(mr_request_message);
	
	result.appendChild(GSXML.duplicateWithNewName(this.doc, (Element)GSXML.getChildByTagName(response_message, GSXML.RESPONSE_ELEM), GSXML.RESPONSE_ELEM, true));
	return result;
	
    }

}
