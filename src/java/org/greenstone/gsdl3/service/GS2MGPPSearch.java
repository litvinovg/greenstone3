/*
 *    GS2MGPPSearch.java
 *    Copyright (C) 2002 New Zealand Digital Library, http://www.nzdl.org
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

// Greenstone classes
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.greenstone.gsdl3.util.FacetWrapper;
import org.greenstone.gsdl3.util.GSFile;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.XMLConverter;

import org.greenstone.mgpp.MGPPDocInfo;
import org.greenstone.mgpp.MGPPQueryResult;
import org.greenstone.mgpp.MGPPSearchWrapper;
import org.greenstone.mgpp.MGPPTermInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GS2MGPPSearch extends AbstractGS2FieldSearch
{
	private static MGPPSearchWrapper mgpp_src = null;

	private String physical_index_name = "idx";

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.GS2MGPPSearch.class.getName());

	/** constructor */
	public GS2MGPPSearch()
	{
		if (mgpp_src == null)
		{
			mgpp_src = new MGPPSearchWrapper();
		}
	}

	public void cleanUp()
	{
		super.cleanUp();
		mgpp_src.unloadIndexData();
	}

	/** process a query */
	protected Element processAnyQuery(Element request, int query_type)
	{
		synchronized (mgpp_src)
		{
			return super.processAnyQuery(request, query_type);
		}
	}

	/** configure this service */
	public boolean configure(Element info, Element extra_info)
	{
		if (!super.configure(info, extra_info))
		{
			return false;
		}

		// set up the defaults which are not dependent on query parameters
		// the default level is also the level which the database is expecting
		// this must not be overwritten
		mgpp_src.setReturnLevel(this.default_db_level);
		// return term info
		mgpp_src.setReturnTerms(true);
		mgpp_src.setMaxNumeric(this.maxnumeric);
		return true;
	}

	/** add in the mgpp specific params to TextQuery */
	protected void addCustomQueryParams(Element param_list, String lang)
	{
		super.addCustomQueryParams(param_list, lang);
		createParameter(RANK_PARAM, param_list, lang);
	}

	protected boolean setUpQueryer(HashMap<String, Serializable> params)
	{

		// set up the defaults that may be changed by query params
		mgpp_src.setQueryLevel(this.default_level);
		// we have case folding on by default
		if (this.does_case) {
		  mgpp_src.setCase(paramDefaults.get(CASE_PARAM).equals(BOOLEAN_PARAM_ON) ? true : false);
		}
		if (this.does_stem) {
		  mgpp_src.setStem(paramDefaults.get(STEM_PARAM).equals(BOOLEAN_PARAM_ON) ? true : false);
		}
		if (this.does_accent) {
		  mgpp_src.setAccentFold(paramDefaults.get(ACCENT_PARAM).equals(BOOLEAN_PARAM_ON) ? true : false);
		}
		// set up the query params
		Set entries = params.entrySet();
		Iterator i = entries.iterator();
		String current_physical_index_name = this.physical_index_name;
		String physical_sub_index_name = this.default_index_subcollection;
		String physical_index_language_name = this.default_index_language;
		while (i.hasNext())
		{
			Map.Entry m = (Map.Entry) i.next();
			String name = (String) m.getKey();
			String value = (String) m.getValue();

			if (name.equals(CASE_PARAM) && this.does_case)
			{
				boolean val = (value.equals(BOOLEAN_PARAM_ON) ? true : false);
				mgpp_src.setCase(val);
			}
			else if (name.equals(STEM_PARAM) && this.does_stem)
			{
				boolean val = (value.equals(BOOLEAN_PARAM_ON) ? true : false);
				mgpp_src.setStem(val);
			}
			else if (name.equals(ACCENT_PARAM) && this.does_accent)
			{
				boolean val = (value.equals(BOOLEAN_PARAM_ON) ? true : false);
				mgpp_src.setAccentFold(val);
			}
			else if (name.equals(MAXDOCS_PARAM) && !value.equals(""))
			{
				int docs = Integer.parseInt(value);
				mgpp_src.setMaxDocs(docs);
			}
			else if (name.equals(LEVEL_PARAM))
			{
				mgpp_src.setQueryLevel(value);
			}
			else if (name.equals(MATCH_PARAM))
			{
				int mode;
				if (value.equals(MATCH_PARAM_ALL))
					mode = 1;
				else
					mode = 0;
				mgpp_src.setMatchMode(mode);
			}
			else if (name.equals(RANK_PARAM))
			{
				if (value.equals(RANK_PARAM_RANK))
				{
					mgpp_src.setSortByRank(true);
				}
				else if (value.equals(RANK_PARAM_NONE))
				{
					mgpp_src.setSortByRank(false);
				}
			}
			else if (name.equals(INDEX_SUBCOLLECTION_PARAM))
			{
				physical_sub_index_name = value;
			}
			else if (name.equals(INDEX_LANGUAGE_PARAM))
			{
				physical_index_language_name = value;
			} // ignore any others
		}

		if (physical_index_name.equals("idx"))
		{
			if (physical_sub_index_name != null)
			{
				current_physical_index_name += physical_sub_index_name;
			}
			if (physical_index_language_name != null)
			{
				current_physical_index_name += physical_index_language_name;
			}
		}

		// set up mgpp_src
		String indexdir = GSFile.collectionBaseDir(this.site_home, this.cluster_name) + File.separatorChar + GSFile.collectionIndexPath(this.index_stem, current_physical_index_name);
		mgpp_src.loadIndexData(indexdir);

		return true;
	}

	protected Object runQuery(String query)
	{
		mgpp_src.runQuery(query);
		MGPPQueryResult mqr = mgpp_src.getQueryResult();
		return mqr;

	}

	protected long numDocsMatched(Object query_result)
	{
		return ((MGPPQueryResult) query_result).getTotalDocs();
	}

	protected String[] getDocIDs(Object query_result)
	{

		Vector docs = ((MGPPQueryResult) query_result).getDocs();
		String[] doc_nums = new String[docs.size()];
		for (int d = 0; d < docs.size(); d++)
		{
			doc_nums[d] = Long.toString((((MGPPDocInfo) docs.elementAt(d)).num_));
		}
		return doc_nums;
	}

	protected String[] getDocRanks(Object query_result)
	{

		Vector docs = ((MGPPQueryResult) query_result).getDocs();
		String[] doc_ranks = new String[docs.size()];
		for (int d = 0; d < docs.size(); d++)
		{
			doc_ranks[d] = Float.toString(((MGPPDocInfo) docs.elementAt(d)).rank_);
		}
		return doc_ranks;
	}

	protected boolean addTermInfo(Element term_list, HashMap<String, Serializable> params, Object query_result)
	{
	  Document doc = term_list.getOwnerDocument();
		String query_level = (String) params.get(LEVEL_PARAM); // the current query level

		Vector terms = ((MGPPQueryResult) query_result).getTerms();
		for (int t = 0; t < terms.size(); t++)
		{
			MGPPTermInfo term_info = (MGPPTermInfo) terms.get(t);

			Element term_elem = doc.createElement(GSXML.TERM_ELEM);
			term_elem.setAttribute(GSXML.NAME_ATT, term_info.term_);
			term_elem.setAttribute(STEM_ATT, "" + term_info.stem_method_);
			term_elem.setAttribute(FREQ_ATT, "" + term_info.term_freq_);
			term_elem.setAttribute(NUM_DOCS_MATCH_ATT, "" + term_info.match_docs_);
			String field = term_info.tag_;
			if (field.equals(query_level))
			{
				// ignore
				field = "";
			}
			term_elem.setAttribute(FIELD_ATT, field);

			Vector equiv_terms = term_info.equiv_terms_;
			Element equiv_term_list = doc.createElement(EQUIV_TERM_ELEM + GSXML.LIST_MODIFIER);
			term_elem.appendChild(equiv_term_list);

			for (int et = 0; et < equiv_terms.size(); et++)
			{
				String equiv_term = (String) equiv_terms.get(et);

				Element equiv_term_elem = doc.createElement(GSXML.TERM_ELEM);
				equiv_term_elem.setAttribute(GSXML.NAME_ATT, equiv_term);
				equiv_term_elem.setAttribute(NUM_DOCS_MATCH_ATT, "");
				equiv_term_elem.setAttribute(FREQ_ATT, "");
				equiv_term_list.appendChild(equiv_term_elem);
			}

			term_list.appendChild(term_elem);
		}
		return true;
	}

	protected String addFieldInfo(String query, String field)
	{
		if (field.equals("") || field.equals("ZZ"))
		{
			return query;
		}
		return "[" + query + "]:" + field;
	}

	protected void addQueryElem(StringBuffer final_query, String query, String field, String combine)
	{

		String comb = "";
		if (final_query.length() > 0)
		{
			comb = " " + combine + " ";
		}
		final_query.append(comb + addFieldInfo(query, field));
	}

	protected String addStemOptions(String query, String stem, String casef, String accent)
	{
		String mods = "#";
		if (casef != null)
		{
			if (casef.equals("1"))
			{
				mods += "i";
			}
			else
			{
				mods += "c";
			}
		}
		if (stem != null)
		{
			if (stem.equals("1"))
			{
				mods += "s";
			}
			else
			{
				mods += "u";
			}
		}
		if (accent != null)
		{
			if (accent.equals("1"))
			{
				mods += "f";
			}
			else
			{
				mods += "a";
			}
		}

		StringBuffer temp = new StringBuffer();
		String[] terms = query.split(" ");
		for (int i = 0; i < terms.length; i++)
		{
			String t = terms[i].trim();
			// what is the TX bit about???
			if (!t.equals("") && !t.equals("TX"))
			{
				temp.append(" " + t + mods);
			}
		}
		return temp.toString();
	}

	protected ArrayList<FacetWrapper> getFacets(Object query_result)
	{
		return null;
	}
}
