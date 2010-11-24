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

public class SystemAction extends Action {
    
      static Logger logger = Logger.getLogger(org.greenstone.gsdl3.action.SystemAction.class.getName());

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
	
	String coll = (String)params.get(GSParams.SYSTEM_CLUSTER);

	String to = "";
	if (coll!=null && !coll.equals("")) {
	    to = coll;
	}

	Element mr_request_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
	Element mr_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_SYSTEM, to, lang, uid);
	mr_request_message.appendChild(mr_request);
	
	Element system = this.doc.createElement(GSXML.SYSTEM_ELEM);
	mr_request.appendChild(system);
	
	// will need to change the following if can do more than one system request at once
	if (subaction.equals("c")) { // configure
	    system.setAttribute(GSXML.TYPE_ATT, GSXML.SYSTEM_TYPE_CONFIGURE);
	    String info = (String)params.get(GSParams.SYSTEM_SUBSET);
	    system.setAttribute(GSXML.SYSTEM_SUBSET_ATT, info);
    }
    else if(subaction.equals("s")) { // save format statement
         logger.error("Initiate save");
         String format_string = (String)params.get("data");
         logger.error("data="+format_string);

         //SamParser sam = new SamParser();
         //String format_statement = sam.parse(input);
         //logger.error("format string="+format_statement);
    
        Element page_response = this.doc.createElement(GSXML.RESPONSE_ELEM);

         Iterator it = params.keySet().iterator();
         while(it.hasNext())
         {
            logger.error("Param: "+it.next());
         }	

        //Node text = this.doc.createTextNode(format_string);
        //page_response.appendChild(text);
    
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            //String input = "<html><head><title></title></head><body>" + format_string + "</body></html>";
            String input = format_string;
            InputSource is = new InputSource( new StringReader( input ) );
            Document d = builder.parse( is );
            Element e = d.getDocumentElement();
            
            page_response.appendChild(this.doc.importNode(e, true));
        }
        catch( Exception ex ) {
            logger.error("There was an exception "+ex);
            
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw, true);
            ex.printStackTrace(pw);
            pw.flush();
            sw.flush();
            logger.error(sw.toString());
        }
        
    
        //Element child = this.doc.createElement("div"); //format_string);
        //Node text = this.doc.createTextNode(format_string); //"<h1>Hi there and greetings!</h1>");
        //child.innerHTML = "<h1>Hi there and greetings!</h1>";
        //child.setNodeValue(format_string);
        //child.appendChild(text);
        result.appendChild(page_response);
        return result;
	} else {
	    String name = (String)params.get(GSParams.SYSTEM_MODULE_NAME);
	    String type = (String)params.get(GSParams.SYSTEM_MODULE_TYPE);

	    system.setAttribute(GSXML.SYSTEM_MODULE_NAME_ATT, name);
	    system.setAttribute(GSXML.SYSTEM_MODULE_TYPE_ATT, type);
	    
	    if (subaction.equals("d")) { // delete
		system.setAttribute(GSXML.TYPE_ATT, GSXML.SYSTEM_TYPE_DEACTIVATE);

	    } else if (subaction.equals("a")) { // add
		system.setAttribute(GSXML.TYPE_ATT, GSXML.SYSTEM_TYPE_ACTIVATE);
	    } else {
	    // create the default response
	    // for now just have an error
	    logger.error("bad subaction type");
	    Element page_response = this.doc.createElement(GSXML.RESPONSE_ELEM);
	    result.appendChild(page_response);
	
	    return result;
	    }
	}

	Node response_message = this.mr.process(mr_request_message);
	
	result.appendChild(GSXML.duplicateWithNewName(this.doc, (Element)GSXML.getChildByTagName(response_message, GSXML.RESPONSE_ELEM), GSXML.RESPONSE_ELEM, true));
	return result;
	
    }

    /*
    public void parse (String message) {
        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();
            InputSource is = new InputSource( new StringReader( message ) );
            //parse the file and also register this class for call backs
            sp.parse(is, new SamParser());

        }catch(SAXException se) {
            se.printStackTrace();
        }catch(ParserConfigurationException pce) {
            pce.printStackTrace();
        }catch (IOException ie) {
            ie.printStackTrace();
        }
    }
    */

    //Event Handlers
/*    public void startElement(String uri, String localName, String qName,
        Attributes attributes) throws SAXException {
        //reset
        logger.error("Start Element: "+qName);
        //if(qName.equalsIgnoreCase("Template")) {
            //create a new instance of employee
            //tempEmp = new Employee();
            //tempEmp.setType(attributes.getValue("type"));
            
        //}
    }


    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch,start,length);
    }

    public void endElement(String uri, String localName,
        String qName) throws SAXException {

        logger.error("Characters: "+tempVal);
        logger.error("End Element: "+qName);
        /*
        if(qName.equalsIgnoreCase("Employee")) {
            //add it to the list
            myEmpls.add(tempEmp);

        }else if (qName.equalsIgnoreCase("Name")) {
            tempEmp.setName(tempVal);
        }else if (qName.equalsIgnoreCase("Id")) {
            tempEmp.setId(Integer.parseInt(tempVal));
        }else if (qName.equalsIgnoreCase("Age")) {
            tempEmp.setAge(Integer.parseInt(tempVal));
        } */

    //}

}
