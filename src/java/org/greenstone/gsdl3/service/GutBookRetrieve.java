package org.greenstone.gsdl3.service;

import org.greenstone.gsdl3.util.*;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.NodeList;

public class GutBookRetrieve 
    extends XMLRetrieve {

    protected Element extractTitleMeta(Element section) {
	Element meta_elem = this.doc.createElement(GSXML.METADATA_ELEM);
	meta_elem.setAttribute(GSXML.NAME_ATT, "Title");

	Element title_elem = (Element)GSXML.getChildByTagName(section, "title");
	if (title_elem == null) {
	    title_elem = (Element)GSXML.getNodeByPath(section, "chapheader/title");
	}
	if (title_elem == null) {
	    // find teh first title elem
	    NodeList titles = section.getElementsByTagName("title");
	    if (titles != null) {
		title_elem = (Element)titles.item(0);
	    }
	}

	if (title_elem == null) {
	    return null; // no title found
	}
	
	
	Text t = (Text) GSXML.getNodeTextNode(title_elem);
	
	meta_elem.appendChild(this.doc.importNode(t, false));
	return meta_elem;

    }

}
