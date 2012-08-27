/*
 *    AbstractGS2TextSearch.java
 *    Copyright (C) 2011 New Zealand Digital Library, http://www.nzdl.org
 *
 *    This program is free software; you can redistribute it and/or modify
 *   the Free Software Foundation; either version 2 of the License, or
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

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.BasicDocumentDatabase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class AbstractGS2TextSearch extends AbstractTextSearch
{
	protected static final String EQUIV_TERM_ELEM = "equivTerm";

	protected static final String STEM_ATT = "stem";
	protected static final String NUM_DOCS_MATCH_ATT = "numDocsMatch";
	protected static final String FREQ_ATT = "freq";

	// Elements used in the config file that are specific to this class
	protected static final String DEFAULT_INDEX_ELEM = "defaultIndex";
	protected static final String INDEX_STEM_ELEM = "indexStem";
	protected static final String INDEX_ELEM = "index";
	protected static final String DEFAULT_INDEX_SUBCOLLECTION_ELEM = "defaultIndexSubcollection";
	protected static final String DEFAULT_INDEX_LANGUAGE_ELEM = "defaultIndexLanguage";
	protected static final String INDEX_SUBCOLLECTION_ELEM = "indexSubcollection";
	protected static final String INDEX_LANGUAGE_ELEM = "indexLanguage";

	// Some indexing options
	protected static final String STEMINDEX_OPTION = "stemIndexes";
	protected static final String MAXNUMERIC_OPTION = "maxnumeric";

	/** the stem used for the index files */
	protected String index_stem = null;

	// stem indexes available
	protected boolean does_case = true;
	protected boolean does_stem = true;
	protected boolean does_accent = false;

	// maxnumeric - 
	protected int maxnumeric = 4;

	BasicDocumentDatabase gs_doc_db = null;

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.AbstractGS2TextSearch.class.getName());

	/** constructor */
	public AbstractGS2TextSearch()
	{

	}

	public void cleanUp()
	{
		super.cleanUp();
		this.gs_doc_db.cleanUp();
	}

	/** configure this service */
	public boolean configure(Element info, Element extra_info)
	{
		if (!super.configure(info, extra_info))
		{
			return false;
		}

		// find out what kind of database we have
		Element database_type_elem = (Element) GSXML.getChildByTagName(info, GSXML.DATABASE_TYPE_ELEM);
		String database_type = null;
		if (database_type_elem != null)
		{
			database_type = database_type_elem.getAttribute(GSXML.NAME_ATT);
		}
		if (database_type == null || database_type.equals(""))
		{
			database_type = "gdbm"; // the default
		}

		// the index stem is either the collection name or is specified in the config file
		Element index_stem_elem = (Element) GSXML.getChildByTagName(info, INDEX_STEM_ELEM);
		if (index_stem_elem != null)
		{
			this.index_stem = index_stem_elem.getAttribute(GSXML.NAME_ATT);
		}
		if (this.index_stem == null || this.index_stem.equals(""))
		{
			logger.warn("indexStem element not found, stem will default to collection name");
			this.index_stem = this.cluster_name;
		}

		// replaces default AbstractSearch version with one tied to database
		gs_doc_db = new BasicDocumentDatabase(this.doc, database_type, this.site_home, this.cluster_name, this.index_stem);
		if (!gs_doc_db.isValid())
		{
			logger.error("Failed to open Document Database.");
			return false;
		}
		this.gs_doc = gs_doc_db;

		// do we support any of the extended features?
		does_chunking = true;

		// Get the default index out of <defaultIndex> (buildConfig.xml)
		Element def = (Element) GSXML.getChildByTagName(info, DEFAULT_INDEX_ELEM);
		if (def != null)
		{
			this.default_index = def.getAttribute(GSXML.SHORTNAME_ATT);
		} // otherwise will be "", and the first one will be the default

		//get the default indexSubcollection out of <defaultIndexSubcollection> (buildConfig.xml)
		Element defSub = (Element) GSXML.getChildByTagName(info, DEFAULT_INDEX_SUBCOLLECTION_ELEM);
		if (defSub != null)
		{
			this.default_index_subcollection = defSub.getAttribute(GSXML.SHORTNAME_ATT);
		}

		//get the default indexLanguage out of <defaultIndexLanguage> (buildConfig.xml)
		Element defLang = (Element) GSXML.getChildByTagName(info, DEFAULT_INDEX_LANGUAGE_ELEM);
		if (defLang != null)
		{
			this.default_index_language = defLang.getAttribute(GSXML.SHORTNAME_ATT);
		} //concate defaultIndex + defaultIndexSubcollection + defaultIndexLanguage

		// get index options
		Element index_option_list = (Element) GSXML.getChildByTagName(info, GSXML.INDEX_OPTION_ELEM + GSXML.LIST_MODIFIER);
		if (index_option_list != null)
		{
			NodeList options = index_option_list.getElementsByTagName(GSXML.INDEX_OPTION_ELEM);
			for (int i = 0; i < options.getLength(); i++)
			{
				Element opt = (Element) options.item(i);
				String name = opt.getAttribute(GSXML.NAME_ATT);
				String value = opt.getAttribute(GSXML.VALUE_ATT);
				if (name.equals(MAXNUMERIC_OPTION))
				{
					int maxnum = Integer.parseInt(value);
					if (4 <= maxnum && maxnum < 512)
					{
						maxnumeric = maxnum;
					}
				}
				else if (name.equals(STEMINDEX_OPTION))
				{
					int stemindex = Integer.parseInt(value);
					// stem and case are true by default, accent folding false by default
					if ((stemindex & 1) == 0)
					{
						does_case = false;
					}
					if ((stemindex & 2) == 0)
					{
						does_stem = false;
					}
					if ((stemindex & 4) != 0)
					{
						does_accent = true;
					}
				}
			}
		}

		// get display info from extra info
		if (extra_info != null)
		{
			Document owner = info.getOwnerDocument();
			Element config_search = (Element) GSXML.getChildByTagName(extra_info, GSXML.SEARCH_ELEM);

			// so far we have index and indexSubcollection specific display elements, and global format elements 

			NodeList indexes = info.getElementsByTagName(GSXML.INDEX_ELEM);
			for (int i = 0; i < indexes.getLength(); i++)
			{
				Element ind = (Element) indexes.item(i);
				String name = ind.getAttribute(GSXML.NAME_ATT);
				Element node_extra = GSXML.getNamedElement(config_search, GSXML.INDEX_ELEM, GSXML.NAME_ATT, name);
				if (node_extra == null)
				{
					logger.error("haven't found extra info for index named " + name);
					continue;
				}

				// get the display elements if any - displayName
				NodeList display_names = node_extra.getElementsByTagName(GSXML.DISPLAY_TEXT_ELEM);
				if (display_names != null)
				{
					for (int j = 0; j < display_names.getLength(); j++)
					{
						Element e = (Element) display_names.item(j);
						ind.appendChild(owner.importNode(e, true));
					}
				}
			} // for each index

			NodeList indexSubcollections = info.getElementsByTagName(INDEX_SUBCOLLECTION_ELEM); // buildConfig.xml

			for (int i = 0; i < indexSubcollections.getLength(); i++)
			{
				Element indexSubcollection = (Element) indexSubcollections.item(i);
				String name = indexSubcollection.getAttribute(GSXML.NAME_ATT);
				Element node_extra = GSXML.getNamedElement(config_search, INDEX_SUBCOLLECTION_ELEM, GSXML.NAME_ATT, name); // collectionConfig.xml
				if (node_extra == null)
				{
					logger.error("haven't found extra info for indexSubCollection named " + name);
					continue;
				}

				// get the display elements if any - displayName
				NodeList display_names = node_extra.getElementsByTagName(GSXML.DISPLAY_TEXT_ELEM);
				if (display_names != null)
				{
					for (int j = 0; j < display_names.getLength(); j++)
					{
						Element e = (Element) display_names.item(j);
						indexSubcollection.appendChild(owner.importNode(e, true));
					}
				}
			} // for each indexSubCollection
		}
		return true;
	}

	protected void getIndexData(ArrayList<String> index_ids, ArrayList<String> index_names, String lang)
	{
		// the index info -
		Element index_list = (Element) GSXML.getChildByTagName(this.config_info, INDEX_ELEM + GSXML.LIST_MODIFIER);
		NodeList indexes = index_list.getElementsByTagName(INDEX_ELEM);
		int len = indexes.getLength();
		// now add even if there is only one
		for (int i = 0; i < len; i++)
		{
			Element index = (Element) indexes.item(i);
			String shortname = index.getAttribute(GSXML.SHORTNAME_ATT);
			if (shortname.equals(""))
			{
				continue;
			}
			index_ids.add(shortname);
			String display_name = GSXML.getDisplayText(index, GSXML.DISPLAY_TEXT_NAME, lang, "en");
			if (display_name.equals(""))
			{
				display_name = index.getAttribute(GSXML.NAME_ATT);
				if (display_name.equals(""))
				{
					display_name = shortname;
				}
			}
			index_names.add(display_name);
		}
	}

	protected void getIndexSubcollectionData(ArrayList<String> index_sub_ids, ArrayList<String> index_sub_names, String lang)
	{
		// the index info -
		Element index_sub_list = (Element) GSXML.getChildByTagName(this.config_info, INDEX_SUBCOLLECTION_ELEM + GSXML.LIST_MODIFIER);
		NodeList index_subs = index_sub_list.getElementsByTagName(INDEX_SUBCOLLECTION_ELEM);
		int len = index_subs.getLength();
		// now add even if there is only one
		for (int i = 0; i < len; i++)
		{
			Element indexsub = (Element) index_subs.item(i);
			String shortname = indexsub.getAttribute(GSXML.SHORTNAME_ATT);
			if (shortname.equals(""))
			{
				continue;
			}
			index_sub_ids.add(shortname);
			String display_name = GSXML.getDisplayText(indexsub, GSXML.DISPLAY_TEXT_NAME, lang, "en");
			if (display_name.equals(""))
			{
				display_name = indexsub.getAttribute(GSXML.NAME_ATT);
				if (display_name.equals(""))
				{
					display_name = shortname;
				}
			}
			index_sub_names.add(display_name);
		}
	}

	protected void getIndexLanguageData(ArrayList<String> index_lang_ids, ArrayList<String> index_lang_names, String lang)
	{
		// the index info -
		Element index_lang_list = (Element) GSXML.getChildByTagName(this.config_info, INDEX_LANGUAGE_ELEM + GSXML.LIST_MODIFIER);
		NodeList index_langs = index_lang_list.getElementsByTagName(INDEX_LANGUAGE_ELEM);
		int len = index_langs.getLength();
		// now add even if there is only one
		for (int i = 0; i < len; i++)
		{
			Element indexlang = (Element) index_langs.item(i);
			String shortname = indexlang.getAttribute(GSXML.SHORTNAME_ATT);
			if (shortname.equals(""))
			{
				continue;
			}
			index_lang_ids.add(shortname);
			String display_name = GSXML.getDisplayText(indexlang, GSXML.DISPLAY_TEXT_NAME, lang, "en");
			if (display_name.equals(""))
			{
				display_name = indexlang.getAttribute(GSXML.NAME_ATT);
				if (display_name.equals(""))
				{
					display_name = shortname;
				}
			}
			index_lang_names.add(display_name);
		}

	}

	protected void addCustomQueryParams(Element param_list, String lang)
	{
		if (this.does_case)
		{
			// gs2 has case on by default
			createParameter(CASE_PARAM, param_list, lang, BOOLEAN_PARAM_ON);
		}
		if (this.does_stem)
		{
			// but stem is off by default
			createParameter(STEM_PARAM, param_list, lang, BOOLEAN_PARAM_OFF);
		}
		if (this.does_accent)
		{
			// and so is accent folding
			createParameter(ACCENT_PARAM, param_list, lang, BOOLEAN_PARAM_OFF);
		}
		createParameter(MATCH_PARAM, param_list, lang);
	}

	/** convert indexer internal id to Greenstone oid */
	protected String internalNum2OID(long docnum)
	{
		return this.gs_doc_db.internalNum2OID(docnum);
	}

	protected String internalNum2OID(String docnum)
	{
		return this.gs_doc_db.internalNum2OID(docnum);
	}

}
