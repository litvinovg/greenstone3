package org.greenstone.gsdl3.service;


// Greenstone classes
import org.greenstone.gsdl3.util.*;

// XML classes
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

// General Java classes

// just needs to override a few methods that are TEI specific
public class TEIRetrieve extends XMLRetrieve {

   protected Element extractTitleMeta(Element section) {
	
	Element meta_elem = this.doc.createElement(GSXML.METADATA_ELEM);
	meta_elem.setAttribute(GSXML.NAME_ATT, "Title");
	
	Element section_head = (Element)GSXML.getChildByTagName(section, "head");
	if (section_head == null || !section_head.hasChildNodes()) {
	    // there is no head element, so take the type attribute and make that the title
	    String title = "("+section.getAttribute("type")+")";
	    Text t = this.doc.createTextNode(title);
	    meta_elem.appendChild(t);
	} else {
	    // add the head element as the metadata content
	    meta_elem.appendChild(this.doc.importNode(section_head, true));
	}
	return meta_elem;
    }

    // this assumes that the scope refers to a top level node - this may be overwritten if the scope bit in the id is a shorthand of some sort
    protected String translateScope(String scope) {
	if (scope.equals("front")) {
	    return "text/front";
	}
	if (scope.equals("body")) {
	    return "text/body";
	}
	if (scope.equals("back")) {
	    return "text/back";
	}
	if (scope.equals("full")) { // is this ever used???
	    return "";
	}
	return scope;
    }

}
