/*
 *    GS2LuceneRetrieve.java
 *    Copyright (C) 2005 New Zealand Digital Library, http://www.nzdl.org
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
package org.greenstone.gsdl3.service;

// Greenstone classes
import org.greenstone.gsdl3.core.GSException;
import org.greenstone.gsdl3.util.GSFile;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.DBInfo;
import org.greenstone.gsdl3.util.GSHTML;
import org.greenstone.gsdl3.util.OID;
// XML classes
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

// General Java classes
import java.io.File;

import org.apache.log4j.Logger;

/**
 * Retrieve documents from a gs2 lucene collection. Note that this doesn't
 * actually use lucene, as the documents are stored in XML files
 */
public class GS2LuceneRetrieve extends AbstractGS2DocumentRetrieve
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.GS2LuceneRetrieve.class.getName());

	protected static final String DOC_LEVEL = "Doc";
	protected static final String SEC_LEVEL = "Sec";
	protected static final String ID_ATT = "gs2:docOID";

	// Parameters used
	private static final String LEVEL_PARAM = "level";

	// Elements used in the config file that are specific to this class
	private static final String DEFAULT_LEVEL_ELEM = "defaultLevel";

	private String default_level = null;
	private String text_dir = null;

	private boolean text_available = true;

	public GS2LuceneRetrieve()
	{
	}

	public void cleanUp()
	{
		super.cleanUp();
	}

	/** configure this service */
	public boolean configure(Element info, Element extra_info)
	{
		if (!super.configure(info, extra_info))
		{
			return false;
		}

		// Do specific configuration
		logger.info("Configuring GS2LuceneRetrieve...");

		text_dir = GSFile.collectionIndexDir(this.site_home, this.cluster_name) + File.separatorChar + "text" + File.separatorChar;
		if (!(new File(text_dir).isDirectory()))
		{
			logger.error("Text directory " + text_dir + " does not exist, will be unable to retrieve text for " + cluster_name);
			text_available = false;
			return true; // return true so that we still get the other services for the collection
		}
		// Get the default level out of <defaultLevel> (buildConfig.xml)
		Element def = (Element) GSXML.getChildByTagName(info, DEFAULT_LEVEL_ELEM);
		if (def != null)
		{
			this.default_level = def.getAttribute(GSXML.SHORTNAME_ATT);
		}
		if (this.default_level == null || this.default_level.equals(""))
		{
			logger.error("Default level not specified for " + this.cluster_name + ", assuming " + DOC_LEVEL);
			this.default_level = DOC_LEVEL;
		}

		return true;

	}

	/**
	 * returns the content of a node should return a nodeContent element:
	 * <nodeContent>text content or other elements</nodeContent>
	 */
	protected Element getNodeContent(String doc_id, String lang) throws GSException
	{
		String[] args = new String[1];
		args[0] = doc_id;
		String doc_content = getTextString("TextRetrievalError", lang, args);
		try
		{
			if (!text_available)
			{
				throw new Exception("No text directory available");
			}

			DBInfo info = this.coll_db.getInfo(OID.getTop(doc_id));
			if (info == null)
			{
				throw new Exception("Couldn't get database entry for " + OID.getTop(doc_id));
			}

			String archivedir = info.getInfo("archivedir");
			File doc_xml_file = new File(text_dir + archivedir + File.separatorChar + "doc.xml");
			if (!doc_xml_file.isFile())
			{
				throw new Exception("Doc XML file " + doc_xml_file.getPath() + " does not exist");
			}
			Document doc_xml_doc = this.converter.getDOM(doc_xml_file, "utf-8");
			if (doc_xml_doc == null)
			{
				throw new Exception("Couldn't parse file " + doc_xml_file.getPath());
			}
			Element full_document = doc_xml_doc.getDocumentElement();
			if (full_document == null)
			{
				throw new Exception("Couldn't parse file " + doc_xml_file.getPath());
			}
			Element current_section = null;
			if (default_level.equals(DOC_LEVEL))
			{
				current_section = full_document;
			}
			else
			{
				current_section = GSXML.getNamedElement(full_document, SEC_LEVEL, ID_ATT, doc_id);
			}
			if (current_section == null)
			{
				throw new Exception("Couldn't find section " + doc_id + " in file " + doc_xml_file.getPath());
			}
			doc_content = GSXML.getNodeText(current_section);
			if (doc_content == null)
			{
				doc_content = "";
			}
			else
			{
				doc_content = resolveTextMacros(doc_content, doc_id, lang);
			}
		}
		catch (Exception e)
		{
			logger.error("Error trying to get document text for " + doc_id + " in collection " + this.cluster_name + ": " + e);
		}

		Element content_node = this.doc.createElement(GSXML.NODE_CONTENT_ELEM);
		Text t = this.doc.createTextNode(doc_content);
		content_node.appendChild(t);
		return content_node;
	}
}
