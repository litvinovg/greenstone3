/*
 *    DocumentBasket.java
 *    Copyright (C) 2006 New Zealand Digital Library, http://www.nzdl.org
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

import java.sql.Statement;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.greenstone.util.GlobalProperties;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.GSPath;

import java.net.InetAddress;
import java.util.Properties;
import java.util.Date;

import javax.mail.*;
import javax.mail.internet.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

import java.io.FileOutputStream;
import java.io.*;
import java.io.IOException;

import org.apache.log4j.*;

public class DocumentBasket extends ServiceRack
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.DocumentBasket.class.getName());

	// the services on offer
	// these strings must match what is found in the properties file
	protected static final String ADD_ITEM_SERVICE = "AddDocument";
	protected static final String DISPLAY_ITEMS_SERVICE = "DisplayDocumentList";
	protected static final String ITEM_NUM_SERVICE = "GetDocuments";
	protected static final String DELETE_ITEMS_SERVICE = "DeleteDocuments";
	protected static final String DELETE_ITEM_SERVICE = "DeleteDocument";
	//added
	protected static final String MERGE_ITEM_SERVICE = "MergeDocument";
	protected static final String ITEM_PARAM = "item";
	protected static final String delimiter = "|";
	protected static final int delay = 1800000;

	protected static final String BASKET_BOOK = "documentBasketBook";

	protected Hashtable userMap = null;
	protected Hashtable timerMap = null;
	protected String username = "";
	protected String password = "";

	/** constructor */
	public DocumentBasket()
	{
		userMap = new Hashtable();
		timerMap = new Hashtable();
	}

	private Hashtable updateDocMap(Element request)
	{
		String id = request.getAttribute("uid");

		if (userMap.containsKey(id))
		{
			if (timerMap.containsKey(id))
			{
				UserTimer timer = (UserTimer) timerMap.get(id);
				timer.restart();
			}
			return (Hashtable) userMap.get(id);
		}
		else
		{
			UserTimer timer = new UserTimer(delay, id);
			timerMap.put(id, timer);
			timer.start();
			Hashtable newDocs = new Hashtable();
			userMap.put(id, newDocs);
			return newDocs;
		}
	}

	/** configure this service */
	public boolean configure(Element info, Element extra_info)
	{
		logger.info("Configuring DocumentBasket...");
		this.config_info = info;

		// set up short_service_info_ - for now just has name and type
		Element add_service = this.doc.createElement(GSXML.SERVICE_ELEM);
		add_service.setAttribute(GSXML.TYPE_ATT, "gather"); // what??
		add_service.setAttribute(GSXML.NAME_ATT, ADD_ITEM_SERVICE);
		this.short_service_info.appendChild(add_service);

		// set up short_service_info_ - for now just has name and type
		Element disp_service = this.doc.createElement(GSXML.SERVICE_ELEM);
		disp_service.setAttribute(GSXML.TYPE_ATT, "gather"); // what??
		disp_service.setAttribute(GSXML.NAME_ATT, DISPLAY_ITEMS_SERVICE);
		this.short_service_info.appendChild(disp_service);

		// set up short_service_info_ - for now just has name and type
		Element num_service = this.doc.createElement(GSXML.SERVICE_ELEM);
		num_service.setAttribute(GSXML.TYPE_ATT, "gather"); // what??
		num_service.setAttribute(GSXML.NAME_ATT, ITEM_NUM_SERVICE);
		this.short_service_info.appendChild(num_service);

		// set up short_service_info_ - for now just has name and type
		Element delete_service = this.doc.createElement(GSXML.SERVICE_ELEM);
		delete_service.setAttribute(GSXML.TYPE_ATT, "gather"); // what??
		delete_service.setAttribute(GSXML.NAME_ATT, DELETE_ITEMS_SERVICE);
		this.short_service_info.appendChild(delete_service);

		// set up short_service_info_ - for now just has name and type
		Element deleteone_service = this.doc.createElement(GSXML.SERVICE_ELEM);
		deleteone_service.setAttribute(GSXML.TYPE_ATT, "gather"); // what??
		deleteone_service.setAttribute(GSXML.NAME_ATT, DELETE_ITEM_SERVICE);
		this.short_service_info.appendChild(deleteone_service);

		// set up short_service_info_ - for now just has name and type
		Element merge_service = this.doc.createElement(GSXML.SERVICE_ELEM);
		merge_service.setAttribute(GSXML.TYPE_ATT, "gather"); // what??
		merge_service.setAttribute(GSXML.NAME_ATT, MERGE_ITEM_SERVICE);
		this.short_service_info.appendChild(merge_service);

		return true;
	}

	/** returns a specific service description */
	protected Element getServiceDescription(String service_id, String lang, String subset)
	{
		if (service_id.equals(ADD_ITEM_SERVICE))
		{
			Element add_service = this.doc.createElement(GSXML.SERVICE_ELEM);
			add_service.setAttribute(GSXML.TYPE_ATT, "gather"); // what??
			add_service.setAttribute(GSXML.NAME_ATT, ADD_ITEM_SERVICE);
			return add_service;
		}
		if (service_id.equals(DISPLAY_ITEMS_SERVICE))
		{

			Element disp_service = this.doc.createElement(GSXML.SERVICE_ELEM);
			disp_service.setAttribute(GSXML.TYPE_ATT, "gather"); // what??
			disp_service.setAttribute(GSXML.NAME_ATT, DISPLAY_ITEMS_SERVICE);
			return disp_service;
		}

		if (service_id.equals(ITEM_NUM_SERVICE))
		{

			Element num_service = this.doc.createElement(GSXML.SERVICE_ELEM);
			num_service.setAttribute(GSXML.TYPE_ATT, "gather"); // what??
			num_service.setAttribute(GSXML.NAME_ATT, ITEM_NUM_SERVICE);
			return num_service;
		}

		if (service_id.equals(DELETE_ITEMS_SERVICE))
		{

			Element del_service = this.doc.createElement(GSXML.SERVICE_ELEM);
			del_service.setAttribute(GSXML.TYPE_ATT, "gather"); // what??
			del_service.setAttribute(GSXML.NAME_ATT, DELETE_ITEMS_SERVICE);
			return del_service;
		}

		if (service_id.equals(DELETE_ITEM_SERVICE))
		{

			Element delone_service = this.doc.createElement(GSXML.SERVICE_ELEM);
			delone_service.setAttribute(GSXML.TYPE_ATT, "gather"); // what??
			delone_service.setAttribute(GSXML.NAME_ATT, DELETE_ITEM_SERVICE);
			return delone_service;
		}
		if (service_id.equals(MERGE_ITEM_SERVICE))
		{
			Element merge_service = this.doc.createElement(GSXML.SERVICE_ELEM);
			merge_service.setAttribute(GSXML.TYPE_ATT, "gather"); // what??
			merge_service.setAttribute(GSXML.NAME_ATT, MERGE_ITEM_SERVICE);
			return merge_service;
		}
		return null;
	}

	protected Element processAddDocument(Element request)
	{
		//System.err.println("REQUEST = " + GSXML.xmlNodeToString(request));
		Hashtable docsMap = updateDocMap(request);
		//System.err.println("DOCSMAP = " + docsMap);
		// Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (param_list == null)
		{
			logger.error("DocumentBasket Error: AddDocument request had no paramList.");
			return result; // Return the empty result
		}

		HashMap params = GSXML.extractParams(param_list, false);
		//System.err.println("PARAMS = " + params);
		String item = (String) params.get("item");

		int startIndex = item.startsWith(BASKET_BOOK) ? BASKET_BOOK.length() : 0;

		String collection = "";
		int pos = item.indexOf(":");
		if (pos != -1)
		{
			collection = item.substring(startIndex, pos);
			item = item.substring(pos + 1);
		}
		//logger.error("COLLECTION = " + collection + " *** ITEM = " + item);
		if (docsMap.containsKey(collection))
		{
			Hashtable items = (Hashtable) docsMap.get(collection);
			if (!items.containsKey(item))
			{
				Item newItem = generateItem(collection, item);
				items.put(item, newItem);
				result.appendChild(newItem.wrapIntoElement());
			}
		}
		else
		{
			Hashtable items = new Hashtable();
			Item newItem = generateItem(collection, item);
			items.put(item, newItem);
			docsMap.put(collection, items);
			result.appendChild(newItem.wrapIntoElement());
		}

		return result;
	}

	protected Element processMergeDocument(Element request)
	{
		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (param_list == null)
		{
			logger.error("DocumentBasket Error: MergeDocument request had no paramList.");
			return null; // Return the empty result
		}

		HashMap params = GSXML.extractParams(param_list, false);

		String docString = (String) params.get("docs");
		String[] docs = docString.split("-");

		for (String d : docs)
		{
			logger.error("DOC = " + d);
		}

		/*
		 * try{ System.out.println("Concatenate Two PDF"); PdfReader reader1 =
		 * new PdfReader("3A01-01_Part_1-001.pdf"); PdfReader reader2 = new
		 * PdfReader("3A01-01_Part_1-017.pdf"); PdfCopyFields copy = new
		 * PdfCopyFields(new FileOutputStream("concatenatedPDF.pdf"));
		 * copy.addDocument(reader1); copy.addDocument(reader2); copy.close(); }
		 * catch(Exception ex) { ex.printStackTrace(); }
		 */
		// added -->

		try
		{
			{
				PrintWriter pw = new PrintWriter(new FileOutputStream("G:/output1.xml"));
				File file = new File("G:/greenstone3-svn/web/sites/localsite/collect/peij21/archives/HASH0189.dir/");
				File[] files = file.listFiles();

				for (int i = 0; i < files.length; i++)
				{

					//System.out.println(files[i].getName());
					String fileName = files[i].getName();

					if (fileName.equals("doc.xml"))
					{

						System.out.println("Processing " + files[i].getPath() + "... ");
						BufferedReader br = new BufferedReader(new FileReader(files[i].getPath()));
						String line = br.readLine();
						while (line != null)
						{
							pw.println(line);
							line = br.readLine();
						}
						br.close();

					}

				}

				File file1 = new File("G:/greenstone3-svn/web/sites/localsite/collect/peij21/archives/HASHfdc0.dir/");
				File[] files1 = file1.listFiles();

				for (int i = 0; i < files1.length; i++)
				{

					//System.out.println(files[i].getName());
					String fileName = files1[i].getName();

					if (fileName.equals("doc.xml"))
					{

						System.out.println("Processing " + files1[i].getPath() + "... ");
						BufferedReader br = new BufferedReader(new FileReader(files1[i].getPath()));
						String line = br.readLine();
						while (line != null)
						{
							pw.println(line);
							line = br.readLine();
						}
						br.close();

					}

				}

				pw.close();

				System.out.println("All doc.xml files have been concatenated into output1.xml");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

	//end
	private Item generateItem(String collection, String id)
	{
		Item item = new Item(collection, id);
		String to = GSPath.appendLink(collection, "DocumentMetadataRetrieve");
		ArrayList tmp = new ArrayList();
		tmp.add(id);
		Element response = getDocumentMetadata(to, "en", "dumy", tmp.iterator());
		Element doc_node = (Element) response.getElementsByTagName(GSXML.DOC_NODE_ELEM).item(0);

		String node_id = doc_node.getAttribute(GSXML.NODE_ID_ATT);
		Element metadata_list = (Element) doc_node.getElementsByTagName(GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER).item(0);

		//assign title metadata if any
		Element metadata = GSXML.getNamedElement(metadata_list, "metadata", "name", "Title");
		if (metadata != null)
		{
			item.title = GSXML.getNodeText(metadata).trim();
		}
		//assign date metadata if any
		metadata = GSXML.getNamedElement(metadata_list, "metadata", "name", "Date");
		if (metadata != null)
		{
			item.date = GSXML.getNodeText(metadata).trim();
		}

		//assign root title metadata if any
		metadata = GSXML.getNamedElement(metadata_list, "metadata", "name", "root_Title");
		if (metadata != null)
		{
			String rootTitle = GSXML.getNodeText(metadata).trim();
			if (!rootTitle.equals(item.title))
			{
				item.rootTitle = rootTitle;
			}

		}

		return item;
	}

	protected Element processDeleteDocuments(Element request)
	{
		Hashtable docsMap = updateDocMap(request);

		// Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);

		//GSXML.printXMLNode(param_list);

		if (param_list == null)
		{
			logger.error("DocumentBasket Error: DeleteDocument request had no paramList.");
			return result; // Return the empty result
		}

		HashMap params = GSXML.extractParams(param_list, false);

		String param = (String) params.get("items");

		if (param == null)
			return result;

		String[] items = param.split("\\|");

		for (int i = 0; i < items.length; i++)
		{
			String item = items[i];
			if (item.trim().length() == 0)
				continue;

			String collection = "";
			int pos = item.indexOf(":");
			if (pos != -1)
			{
				collection = item.substring(0, pos);
				item = item.substring(pos + 1);
			}

			if (docsMap.containsKey(collection))
			{
				Hashtable itemMap = (Hashtable) docsMap.get(collection);
				if (itemMap.containsKey(item))
				{
					itemMap.remove(item);
				}
				if (itemMap.size() == 0)
				{
					docsMap.remove(collection);
				}
			}
		}

		return result;
	}

	protected Element processDeleteDocument(Element request)
	{
		Hashtable docsMap = updateDocMap(request);

		// Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);

		//GSXML.printXMLNode(param_list);

		if (param_list == null)
		{
			logger.error("DocumentBasket Error: DeleteDocument request had no paramList.");
			return result; // Return the empty result
		}

		HashMap params = GSXML.extractParams(param_list, false);

		String param = (String) params.get("item");

		if (param == null)
			return result;

		String item = param;

		String collection = "";
		int pos = item.indexOf(":");

		if (pos != -1)
		{
			collection = item.substring(0, pos);
			item = item.substring(pos + 1);
		}

		if (docsMap.containsKey(collection))
		{
			Hashtable itemMap = (Hashtable) docsMap.get(collection);
			if (itemMap.containsKey(item))
			{
				itemMap.remove(item);
			}
			if (itemMap.size() == 0)
			{
				docsMap.remove(collection);
			}
		}

		return result;
	}

	protected Element processGetDocuments(Element request)
	{
		// GSXML.printXMLNode(request);
		Hashtable docsMap = updateDocMap(request);

		// Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);

		int size = 0;
		String ids = "";
		Iterator keys = docsMap.keySet().iterator();

		while (keys.hasNext())
		{
			Hashtable items = (Hashtable) docsMap.get((String) keys.next());
			size += items.size();
			Iterator values = items.values().iterator();
			while (values.hasNext())
			{
				Item item = (Item) values.next();
				result.appendChild(item.wrapIntoElement());
			}
		}

		Element selement = this.doc.createElement("size");
		selement.setAttribute("value", size + "");
		result.appendChild(selement);

		return result;
	}

	private Element getDocumentMetadata(String to, String lang, String uid, Iterator ids)
	{

		// Build a request to obtain some document metadata
		Element dm_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element dm_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, to, lang, uid);
		dm_message.appendChild(dm_request);

		// Create a parameter list to specify the required metadata information
		HashSet meta_names = new HashSet();
		meta_names.add("Title"); // the default
		meta_names.add("root_Title");
		meta_names.add("Date");

		Element param_list = this.doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);

		Element param = null;
		Iterator i = meta_names.iterator();
		while (i.hasNext())
		{
			String name = (String) i.next();
			param = this.doc.createElement(GSXML.PARAM_ELEM);
			param_list.appendChild(param);
			param.setAttribute(GSXML.NAME_ATT, "metadata");
			param.setAttribute(GSXML.VALUE_ATT, name);
		}

		dm_request.appendChild(param_list);

		// create the doc node list for the metadata request
		Element dm_doc_list = this.doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		dm_request.appendChild(dm_doc_list);

		while (ids.hasNext())
		{
			// Add the documentNode to the list
			Element dm_doc_node = this.doc.createElement(GSXML.DOC_NODE_ELEM);
			dm_doc_list.appendChild(dm_doc_node);
			dm_doc_node.setAttribute(GSXML.NODE_ID_ATT, (String) ids.next());
		}

		return (Element) this.router.process(dm_message);
	}

	protected Element processDisplayDocumentList(Element request)
	{
		Hashtable docsMap = updateDocMap(request);

		// Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);

		Iterator keys = docsMap.keySet().iterator();

		while (keys.hasNext())
		{
			String collection = (String) keys.next();
			Hashtable items = (Hashtable) docsMap.get(collection);
			Iterator itemItr = items.values().iterator();

			Element collectionNode = this.doc.createElement("documentList");
			collectionNode.setAttribute("name", collection);
			result.appendChild(collectionNode);

			while (itemItr.hasNext())
			{
				Item item = (Item) itemItr.next();
				Element itemElement = this.doc.createElement("item");

				collectionNode.appendChild(itemElement);
				itemElement.setAttribute("name", item.docid);
				itemElement.setAttribute("collection", item.collection);
				itemElement.setAttribute("title", item.title);
				itemElement.setAttribute("date", item.date);
				itemElement.setAttribute("root_title", item.rootTitle);
			}
		}

		return result;
	}

	protected class Item
	{
		public String collection;
		public String docid;
		public String title = "";
		public String query = "";
		public String date = "";
		public String rootTitle = "";

		public Item(String coll, String id)
		{
			this.collection = coll;
			this.docid = id;
		}

		public boolean equals(Object o)
		{
			if (!(o instanceof Item))
			{
				return false;
			}

			Item item = (Item) o;
			String id = collection + ":" + docid;
			String idin = item.collection + ":" + item.docid;
			return id.equals(idin);
		}

		public String toString()
		{
			return collection + ":" + docid + ":" + "[" + ((!rootTitle.equals("")) ? (rootTitle + ":") : "") + title + "]";
		}

		public Element wrapIntoElement()
		{
			Element itemElement = doc.createElement("item");
			itemElement.setAttribute("name", docid);
			itemElement.setAttribute("collection", collection);
			itemElement.setAttribute("title", title);
			itemElement.setAttribute("date", date);
			itemElement.setAttribute("root_title", rootTitle);
			return itemElement;
		}
	}

	private class UserTimer extends Timer implements ActionListener
	{
		String id = "";

		public UserTimer(int delay, String id)
		{
			super(delay, (ActionListener) null);
			addActionListener(this);
			this.id = id;
		}

		public void actionPerformed(ActionEvent e)
		{
			userMap.remove(id);
			timerMap.remove(id);
			stop();
		}
	}
}
