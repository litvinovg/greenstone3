/*
 *    GS2LuceneSearch.java
 *    Copyright (C) 2006 New Zealand Digital Library, http://www.nzdl.org
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.greenstone.LuceneWrapper3.GS2LuceneQuery;
import org.greenstone.LuceneWrapper3.LuceneQueryResult;
import org.greenstone.gsdl3.util.FacetWrapper;
import org.greenstone.gsdl3.util.GSFile;
import org.greenstone.gsdl3.util.GSXML;
import org.w3c.dom.Element;

public class GS2LuceneSearch extends SharedSoleneGS2FieldSearch
{
	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.GS2LuceneSearch.class.getName());

	private GS2LuceneQuery lucene_src = null;

	public GS2LuceneSearch()
	{
		this.lucene_src = new GS2LuceneQuery();
	}

	public void cleanUp()
	{
		super.cleanUp();
		this.lucene_src.cleanUp();
	}

	/** methods to handle actually doing the query */

	/** do any initialisation of the query object */
	protected boolean setUpQueryer(HashMap params)
	{
		String indexdir = GSFile.collectionBaseDir(this.site_home, this.cluster_name) + File.separatorChar + "index" + File.separatorChar;

		String index = "didx";
		String physical_index_language_name = null;
		String physical_sub_index_name = null;
		int maxdocs = 100;
		int hits_per_page = 20;
		int start_page = 1;
		String sort_field = GS2LuceneQuery.SORT_RANK;
		String sort_order = SORT_ORDER_ASCENDING;
		// set up the query params
		Set entries = params.entrySet();
		Iterator i = entries.iterator();
		while (i.hasNext())
		{
			Map.Entry m = (Map.Entry) i.next();
			String name = (String) m.getKey();
			String value = (String) m.getValue();

			if (name.equals(MAXDOCS_PARAM) && !value.equals(""))
			{
				maxdocs = Integer.parseInt(value);
			}
			else if (name.equals(HITS_PER_PAGE_PARAM))
			{
				hits_per_page = Integer.parseInt(value);
			}
			else if (name.equals(START_PAGE_PARAM))
			{
				start_page = Integer.parseInt(value);

			}
			else if (name.equals(MATCH_PARAM))
			{
				if (value.equals(MATCH_PARAM_ALL))
				{
					this.lucene_src.setDefaultConjunctionOperator("AND");
				}
				else
				{
					this.lucene_src.setDefaultConjunctionOperator("OR");
				}
			}
			else if (name.equals(RANK_PARAM))
			{
				if (value.equals(RANK_PARAM_RANK))
				{
					value = GS2LuceneQuery.SORT_RANK;
				} else if (value.equals(RANK_PARAM_NONE)) {
				  value = GS2LuceneQuery.SORT_NATURAL;
				}
				this.lucene_src.setSortField(value);
				sort_field = value;
			}
			else if (name.equals(SORT_ORDER_PARAM)) {
			  sort_order = value;
			}
			else if (name.equals(LEVEL_PARAM))
			{
				if (value.toUpperCase().equals("SEC"))
				{
					index = "sidx";
				}
				else
				{
					index = "didx";
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
		// set up start and end results if necessary
		int start_results = 1;
		if (start_page != 1)
		{
			start_results = ((start_page - 1) * hits_per_page) + 1;
		}
		int end_results = hits_per_page * start_page;
		this.lucene_src.setStartResults(start_results);
		this.lucene_src.setEndResults(end_results);

		if (index.equals("sidx") || index.equals("didx"))
		{
			if (physical_sub_index_name != null)
			{
				index += physical_sub_index_name;
			}
			if (physical_index_language_name != null)
			{
				index += physical_index_language_name;
			}
		}

		// default order for rank is descending, while for other
		// fields it is ascending. So reverse_sort is different for
		// the two cases.
		if (sort_field.equals(GS2LuceneQuery.SORT_RANK)) {
		  if (sort_order.equals(SORT_ORDER_ASCENDING)) {
		    this.lucene_src.setReverseSort(true);
		  } else {
		    this.lucene_src.setReverseSort(false);
		  }
		} else {
		  if (sort_order.equals(SORT_ORDER_DESCENDING)) {
		    this.lucene_src.setReverseSort(true);
		  } else {
		    this.lucene_src.setReverseSort(false);
		  }
		}
		this.lucene_src.setIndexDir(indexdir + index);
		this.lucene_src.initialise();
		return true;
	}

	/** do the query */
	protected Object runQuery(String query)
	{
		try
		{
			LuceneQueryResult lqr = this.lucene_src.runQuery(query);
			return lqr;
		}
		catch (Exception e)
		{
			logger.error("Exception happened in runQuery(): ", e);
		}

		return null;
	}

	/** get the total number of docs that match */
	protected long numDocsMatched(Object query_result)
	{
		return ((LuceneQueryResult) query_result).getTotalDocs();
	}

	/** get the list of doc ids */
	protected String[] getDocIDs(Object query_result)
	{
		Vector docs = ((LuceneQueryResult) query_result).getDocs();
		String[] doc_nums = new String[docs.size()];
		for (int d = 0; d < docs.size(); d++)
		{
			String doc_num = ((LuceneQueryResult.DocInfo) docs.elementAt(d)).id_;
			doc_nums[d] = doc_num;
		}
		return doc_nums;
	}

	/** get the list of doc ranks */
	protected String[] getDocRanks(Object query_result)
	{
		Vector docs = ((LuceneQueryResult) query_result).getDocs();
		String[] doc_ranks = new String[docs.size()];
		for (int d = 0; d < docs.size(); d++)
		{
			doc_ranks[d] = Float.toString(((LuceneQueryResult.DocInfo) docs.elementAt(d)).rank_);
		}
		return doc_ranks;
	}

	/** add in term info if available */
	protected boolean addTermInfo(Element term_list, HashMap params, Object query_result)
	{
		String query_level = (String) params.get(LEVEL_PARAM); // the current query level

		Vector terms = ((LuceneQueryResult) query_result).getTerms();
		for (int t = 0; t < terms.size(); t++)
		{
			LuceneQueryResult.TermInfo term_info = (LuceneQueryResult.TermInfo) terms.get(t);

			Element term_elem = this.doc.createElement(GSXML.TERM_ELEM);
			term_elem.setAttribute(GSXML.NAME_ATT, term_info.term_);
			term_elem.setAttribute(FREQ_ATT, "" + term_info.term_freq_);
			term_elem.setAttribute(NUM_DOCS_MATCH_ATT, "" + term_info.match_docs_);
			term_elem.setAttribute(FIELD_ATT, term_info.field_);
			term_list.appendChild(term_elem);
		}

		Vector stopwords = ((LuceneQueryResult) query_result).getStopWords();
		for (int t = 0; t < stopwords.size(); t++)
		{
			String stopword = (String) stopwords.get(t);

			Element stopword_elem = this.doc.createElement(GSXML.STOPWORD_ELEM);
			stopword_elem.setAttribute(GSXML.NAME_ATT, stopword);
			term_list.appendChild(stopword_elem);
		}

		return true;
	}
	
	protected ArrayList<FacetWrapper> getFacets(Object query_result)
	{
		return null;
	}
}
