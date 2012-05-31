/*
 *    SOAPServer.java.in: a template for a new SOAPServer
 *    Copyright (C) 2002 New Zealand Digital Library, http://www.nzdl.org
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.greenstone.gsdl3;

import org.greenstone.gsdl3.core.MessageRouter;
import org.greenstone.util.GlobalProperties;
import org.greenstone.gsdl3.util.GSFile;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.XMLConverter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import java.io.File;

/**
 * The server side of a SOAP connection
 *
 * @author Katherine Don
 * @see <a href="http://www.w3.org/TR/SOAP/">Simple Object Access Protocol (SOAP) 1.1 </a>
 */

public class SOAPServerlocalsite
{
    
    /** The message router we're talking to */
    protected MessageRouter mr=null;
    /** the name of the site we are serving */
    protected String site_name = "localsite";

    /** The no-args constructor */
    public SOAPServerlocalsite() {

	String gsdl3_home = GlobalProperties.getGSDL3Home();
	if (gsdl3_home == null || gsdl3_home.equals("")) {
	    System.err.println("Couldn't access GSDL3Home from GlobalProerties.getGSDL3HOME, can't initialize the SOAP Server.");
	    return;
	}
	String site_home = GSFile.siteHome(gsdl3_home, this.site_name);
	
	File site_file = new File(site_home);
	if (!site_file.isDirectory()) {
	    System.err.println("The site directory "+site_file.getPath()+" doesn't exist. Can't initialize the SOAP Server.");
	    return;
	}   	
	mr = new MessageRouter();
	mr.setSiteName(this.site_name);
	mr.configure();
    }
    
    
    public Element [] process (Element [] xml_in) {
	Element [] result = new Element[xml_in.length];
	for (int i=0; i<xml_in.length; i++) {
	    Element req = xml_in[i];
	    // get rid of the obligatory namespace that axis needs
	    String tag_name = req.getTagName();
	    String namespace="";
	    if (tag_name.indexOf(':')!= -1) {
		namespace = tag_name.substring(0, tag_name.indexOf(':'));
		tag_name = tag_name.substring(tag_name.indexOf(':')+1);	
	    }
	    Element new_req = GSXML.duplicateWithNewName(req.getOwnerDocument(), req, tag_name, true);
	    Node n = mr.process(new_req);
	    Element r = XMLConverter.nodeToElement(n);
	    // add the namespace back on
	    //Element new_res = r;
	    //if (!namespace.equals("")) {
	    //	new_res = GSXML.duplicateWithNewName(r.getOwnerDocument(), r, namespace+r.getTagName(), true);
	    //}
	    result[i] = r;
	}
	return result;
    }   
}

