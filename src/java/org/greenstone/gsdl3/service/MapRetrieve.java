package org.greenstone.gsdl3.service;

// Greenstone classes
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;
import org.greenstone.gsdl3.util.GSFile;
import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.XMLConverter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MapRetrieve extends ServiceRack
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.MapRetrieve.class.getName());

	// the services on offer
	protected static final String DOCUMENT_STRUCTURE_RETRIEVE_SERVICE = "DocumentStructureRetrieve";
	protected static final String DOCUMENT_METADATA_RETRIEVE_SERVICE = "DocumentMetadataRetrieve";
	protected static final String DOCUMENT_CONTENT_RETRIEVE_SERVICE = "DocumentContentRetrieve";

	protected static final int DOCUMENT = 1;

	protected ArrayList<String> index_name_list = null;
	protected ArrayList<String> index_display_list = null;
	protected String files_home_dir = null;
	protected String temp_files_dir = null;
	//protected String temp_image_file = null;
	//protected String temp_image_file2 = null;
	protected final String jpg_ext = ".jpg";
	protected String http_image_dir = null;
	protected String http_temp_image_dir = null;
	private LinkedList<String> namesInList = new LinkedList<String>();

	private DefaultMutableTreeNode tree;

	/** constructor */
	public MapRetrieve()
	{
	}

	/** configure this service */
	public boolean configure(Element info, Element extra_info)
	{
		if (!super.configure(info, extra_info))
		{
			return false;
		}

		logger.info("Configuring MapRetrieve...");
		this.config_info = info;

		// set up short_service_info_ - for now just has name and type
		Element dmr_service = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
		dmr_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
		dmr_service.setAttribute(GSXML.NAME_ATT, DOCUMENT_METADATA_RETRIEVE_SERVICE);
		short_service_info.appendChild(dmr_service);

		Element dcr_service = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
		dcr_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
		dcr_service.setAttribute(GSXML.NAME_ATT, DOCUMENT_CONTENT_RETRIEVE_SERVICE);
		short_service_info.appendChild(dcr_service);

		Element dsr_service = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
		dsr_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
		dsr_service.setAttribute(GSXML.NAME_ATT, DOCUMENT_STRUCTURE_RETRIEVE_SERVICE);
		short_service_info.appendChild(dsr_service);

		// set the files_home variable for this collection
		this.files_home_dir = GSFile.collectionIndexDir(this.site_home, this.cluster_name) + File.separator + "assoc" + File.separator;
		this.temp_files_dir = GSFile.collectionBaseDir(this.site_home, this.cluster_name) + File.separator + "temp" + File.separator;

		//this.files_home_dir + "maps" + File.separator + "temp" + File.separator;
		//this.temp_image_file = this.temp_files_dir+"temp_";
		//this.temp_image_file2 = this.temp_files_dir+"temp2_";

		this.http_image_dir = this.site_http_address + "/collect/" + this.cluster_name + "/index/assoc/maps/";
		this.http_temp_image_dir = this.site_http_address + "/collect/" + this.cluster_name + "/temp/";

		this.index_name_list = new ArrayList<String>();
		this.index_display_list = new ArrayList<String>();
		Element index_list = (Element) GSXML.getChildByTagName(this.config_info, GSXML.INDEX_ELEM + GSXML.LIST_MODIFIER);
		Element display_index_list = (Element) GSXML.getChildByTagName(extra_info, "search");
		if (index_list != null && display_index_list != null)
		{
			NodeList indexes = index_list.getElementsByTagName(GSXML.INDEX_ELEM);
			for (int i = 0; i < indexes.getLength(); i++)
			{
				String name = ((Element) indexes.item(i)).getAttribute(GSXML.NAME_ATT);
				// add the index name
				this.index_name_list.add(name);
				// now look for the display name
				Element this_index = GSXML.getNamedElement(display_index_list, "index", "name", name);
				if (this_index != null)
				{
					Element disp = GSXML.getNamedElement(this_index, "displayItem", "name", "name");
					if (disp != null)
					{
						String display = GSXML.getNodeText(disp);
						if (!display.equals(""))
						{
							this.index_display_list.add(display);
							continue;
						}

					}
				}
				// add the id as default text
				this.index_display_list.add(name);

			}
		}

		// look for document display format
		String path = GSPath.appendLink(GSXML.DISPLAY_ELEM, GSXML.FORMAT_ELEM);
		Element display_format = (Element) GSXML.getNodeByPath(extra_info, path);
		if (display_format != null)
		{
			this.format_info_map.put(DOCUMENT_CONTENT_RETRIEVE_SERVICE, this.desc_doc.importNode(display_format, true));
			// shoudl we make a copy?
		}

		// load the 2-dimensional tree for searching
		try
		{
			ObjectInputStream objectin = new ObjectInputStream(new FileInputStream(this.files_home_dir + "nametree.dat"));
			tree = (DefaultMutableTreeNode) objectin.readObject();
			objectin.close();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		catch (ClassNotFoundException cnfe)
		{
			cnfe.printStackTrace();
		}

		return true;
	}

	/** */
	protected Element getServiceDescription(Document doc, String service, String lang, String subset)
	{
		if (service.equals(DOCUMENT_STRUCTURE_RETRIEVE_SERVICE))
		{

			Element tq_service = doc.createElement(GSXML.SERVICE_ELEM);
			tq_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_QUERY);
			tq_service.setAttribute(GSXML.NAME_ATT, DOCUMENT_STRUCTURE_RETRIEVE_SERVICE);
			return tq_service;
		}
		else if (service.equals(DOCUMENT_METADATA_RETRIEVE_SERVICE))
		{

			Element tq_service = doc.createElement(GSXML.SERVICE_ELEM);
			tq_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_QUERY);
			tq_service.setAttribute(GSXML.NAME_ATT, DOCUMENT_METADATA_RETRIEVE_SERVICE);
			return tq_service;
		}
		else if (service.equals(DOCUMENT_CONTENT_RETRIEVE_SERVICE))
		{

			Element tq_service = doc.createElement(GSXML.SERVICE_ELEM);
			tq_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_QUERY);
			tq_service.setAttribute(GSXML.NAME_ATT, DOCUMENT_CONTENT_RETRIEVE_SERVICE);
			return tq_service;
		}

		return null;

	}

	/** Retrieve the structure of a document */
	protected Element processDocumentStructureRetrieve(Element request)
	{
		// Create a new (empty) result message
	  Element result = XMLConverter.newDOM().createElement(GSXML.RESPONSE_ELEM);
		return result;
	}

	protected Element processDocumentMetadataRetrieve(Element request)
	{
	  Document result_doc = XMLConverter.newDOM();
		// Create a new (empty) result message
		try
		{
			Element result = result_doc.createElement(GSXML.RESPONSE_ELEM);

			String uid = request.getAttribute(GSXML.USER_ID_ATT);
			if (uid.equals(""))
			{
				logger.info("in metadata retrieve, uid = ''\n" + converter.getPrettyString(request));
			}
			result.setAttribute(GSXML.FROM_ATT, DOCUMENT_METADATA_RETRIEVE_SERVICE);
			result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

			// Get the parameters of the request
			Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
			if (param_list == null)
			{
				logger.error("missing paramList.\n");
				return result; // Return the empty result
			}

			// The metadata information required
			Vector<String> metadata_list = new Vector<String>();
			boolean all_metadata = false;
			// Process the request parameters
			Element param = GSXML.getFirstElementChild(param_list);//(Element) param_list.getFirstChild();

			while (param != null)
			{
				// Identify the metadata information desired
				if (param.getAttribute(GSXML.NAME_ATT).equals("metadata"))
				{
					String metadata = GSXML.getValue(param);
					if (metadata.equals("all"))
					{
						all_metadata = true;
						break;
					}
					metadata_list.add(metadata);
				}
				param = (Element) param.getNextSibling();
			}

			Element node_list = result_doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
			result.appendChild(node_list);

			// Get the documents
			Element request_node_list = (Element) GSXML.getChildByTagName(request, GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
			if (request_node_list == null)
			{
				logger.error(" DocumentMetadataRetrieve request had no " + GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
				return result;
			}

			NodeList request_nodes = request_node_list.getChildNodes();

			try
			{
				//used just to ensure outfile is initialised
				BufferedWriter outfile = new BufferedWriter(new FileWriter(this.temp_files_dir + "emptynothingness"));

				for (int i = 0; i < request_nodes.getLength(); i++)
				{
					Element request_node = (Element) request_nodes.item(i);
					String node_id = request_node.getAttribute(GSXML.NODE_ID_ATT);
					String place_data = "";
					String year = "";
					String coOrdinates = "";
					String thumb = "";
					String link = "";
					int mapFreq = 0;
					LinkedList place_data_list = new LinkedList();
					LinkedList coOrdinates_list = new LinkedList();

					//if this is a first displaying of the map
					if (node_id.indexOf("```") != -1 && node_id.indexOf("imageChooser") == -1)
					{
						//get the number of query terms on this map
						mapFreq = Integer.parseInt(node_id.substring(node_id.lastIndexOf('`', node_id.indexOf("```") - 1) + 1, node_id.indexOf("```")));
						//get the place names on this map
						place_data = node_id.substring(node_id.indexOf("```") + 3, node_id.length());
						//get the map metadata
						node_id = node_id.substring(0, node_id.indexOf('`', node_id.indexOf("```")));

						try
						{
							// loop for each query term on the map
							for (int r = 0; r < mapFreq; r++)
							{
								// get title, type, location, and the string for creating the hyperlink for this query term
								String title = place_data.substring(0, place_data.indexOf('`'));
								String type = place_data.substring(place_data.indexOf('`') + 1, place_data.indexOf('`', place_data.indexOf('`') + 1));
								String location = place_data.substring(place_data.indexOf('`', place_data.indexOf('`') + 1) + 1, place_data.indexOf('`', place_data.indexOf('`', place_data.indexOf('`') + 1) + 1));
								if (place_data.indexOf("```") != -1)
									link = place_data.substring(0, place_data.indexOf("```"));
								else
									link = place_data;
								// resolve the type and location
								type = getType(type);
								location = getLocation(location);
								// remove this query term from the string
								if (place_data.indexOf("```") != -1)
									place_data = place_data.substring(place_data.indexOf("```") + 3, place_data.length());

								//add the co-ordinates of this query term to the co-ordinates list
								coOrdinates_list.add("`" + link.substring(link.lastIndexOf('`') + 1, link.length()) + "`" + link.substring(link.lastIndexOf('`', link.lastIndexOf('`') - 1) + 2, link.lastIndexOf('`')));

								// add the title, type and location to the places list
								place_data_list.add(title + ", " + type + ", " + location + ";");
							}
						}
						catch (StringIndexOutOfBoundsException sioobe)
						{
							sioobe.printStackTrace();
						}
					}

					// Add the document to the list
					Element new_node = (Element) result_doc.importNode(request_node, false);
					node_list.appendChild(new_node);

					// Add the requested metadata information
					Element node_meta_list = result_doc.createElement(GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
					new_node.appendChild(node_meta_list);

					// create the navigation thumbnails.  This doesn't seem to work most of the time ???????
					for (int m = 0; m < metadata_list.size(); m++)
					{
						String metadata = metadata_list.get(m);
						thumb = "";
						String value = "";
						if (node_id.indexOf('.') != -1)
							thumb = node_id.substring(0, node_id.indexOf('.'));
						if (node_id.indexOf('`') != -1)
							value = node_id.substring(node_id.lastIndexOf('`', node_id.lastIndexOf('`') - 1) + 1, node_id.lastIndexOf('`', node_id.lastIndexOf('`') - 1) + 5);
						year = value;

						place_data = "";
						if (place_data_list.size() != 0)
							for (int q = 0; q < mapFreq; q++)
							{
								link = (String) place_data_list.get(q);
								if (q != 0)
								{
									place_data = place_data + "<br>" + link;
								}
								else
								{
									place_data = link;
									coOrdinates = "``";
								}
								coOrdinates = coOrdinates + (String) coOrdinates_list.get(q);
							}

						link = "<a href=\"?a=d&c=" + this.cluster_name + "&d=" + node_id + coOrdinates + "&dt=map\">";
						thumb = "<img src=\"" + this.http_image_dir + thumb + "thumb.jpg\" border=0>";
						value = "<table><tr><td>" + link + "<p>" + place_data + "<br>" + value + "</a></td><td>" + link + thumb + "</a></td></tr></table>";
						if (metadata.equals("Title"))
							if (!(place_data.equals("")) && place_data.indexOf(", , ;") == -1 && node_id.indexOf("```") == -1)
								GSXML.addMetadata(node_meta_list, "Title", value);//metadata, value);
							else
								GSXML.addMetadata(node_meta_list, metadata, "");

						if (place_data.indexOf(", , ;") == -1)
						{
							if (i == 0)
								if (!(mapFreq == 0))
								{
									outfile = new BufferedWriter(new FileWriter(this.temp_files_dir + "links" + uid));
									outfile.write("<table align=\"center\"><tr>");
								}

							if (!(mapFreq == 0))
							{
								if (i % 7 == 0)
									outfile.write("</tr><tr>");
								outfile.write("<td align=\"center\">" + link + thumb + "</a><br>" + link + year + "</a></td>");
							}

							if (i == request_nodes.getLength() - 1)
							{
								outfile.write("</tr></table><p>");
								outfile.flush();
								outfile.close();
							}
						}
					}

				}
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}

			return result;
		}
		catch (Exception excep)
		{
			excep.printStackTrace();
		}
		return null;

	}

	protected Element processDocumentContentRetrieve(Element request)
	{
	  Document result_doc = XMLConverter.newDOM();
		// Create a new (empty) result message
		Element result = result_doc.createElement(GSXML.RESPONSE_ELEM);
		result.setAttribute(GSXML.FROM_ATT, DOCUMENT_CONTENT_RETRIEVE_SERVICE);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

		String uid = request.getAttribute(GSXML.USER_ID_ATT);
		String temp_image_file = this.temp_files_dir + "temp_" + uid + ".jpg";
		String temp_image_file2 = this.temp_files_dir + "temp_" + uid + "_2.jpg";

		Element query_doc_list = (Element) GSXML.getChildByTagName(request, GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		if (query_doc_list == null)
		{
			logger.error("DocumentContentRetrieve request specified no doc nodes.\n");
			return result;
		}

		String legend_file = this.temp_files_dir + "legend_" + uid + ".jpg";
		String blank_file = this.files_home_dir + "blank.jpg";
		Element doc_list = result_doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		result.appendChild(doc_list);

		// Get the documents
		String[] doc_ids = GSXML.getAttributeValuesFromList(query_doc_list, GSXML.NODE_ID_ATT);

		for (int i = 0; i < doc_ids.length; i++)
		{
			String doc_id = doc_ids[i];
			// img_num is the name of the map file eg. 072.jpg
			String img_num = doc_id.substring(0, doc_id.indexOf('.'));
			// strings for inserting image in html
			String img_left = "<img border=0 src=\"" + this.http_temp_image_dir;
			String doc_content = "";
			String co_ordinates = "";
			String img_size = "";
			int height = 0;
			int width = 0;
			// the number for the legend image if adding locations to the map
			int leg_num = 0;
			double ratio = 0;

			// if user has clicked on the map.  This does not resolve maps that are not North oriented.
			if (doc_id.indexOf("imageChooser") == 0)
			{
				try
				{
					doc_id = doc_id.substring(doc_id.indexOf('`') + 1, doc_id.length());
					img_num = doc_id.substring(0, doc_id.indexOf('.'));

					// get the map size
					String get_size[] = { "identify", "-size", "10000", temp_image_file };
					Process proc;
					proc = Runtime.getRuntime().exec(get_size);
					BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					img_size = br.readLine();
					proc.waitFor();
					img_size = img_size.substring(img_size.indexOf("JPEG") + 5, img_size.indexOf(" ", img_size.indexOf("JPEG") + 5));
					width = Integer.parseInt(img_size.substring(0, img_size.indexOf("x")));
					height = Integer.parseInt(img_size.substring(img_size.indexOf("x") + 1, img_size.length()));

					// scale image size according to the ratio
					ratio = Double.parseDouble(doc_id.substring(doc_id.lastIndexOf('`', doc_id.indexOf("```") - 1) + 1, doc_id.indexOf("```")));
					width = (int) (width * ratio);
					height = (int) (height * ratio);

					// get the position of the mouse click on the image
					int xclick = Integer.parseInt(doc_id.substring(doc_id.indexOf("```") + 3, doc_id.lastIndexOf('`')));
					int yclick = Integer.parseInt(doc_id.substring(doc_id.lastIndexOf('`') + 1, doc_id.length()));
					doc_id = doc_id.substring(doc_id.indexOf('`') + 1, doc_id.indexOf("```"));

					// convert click position to percentage distance accross and down the image.
					double xpercent = (xclick * 1.0) / width;
					double ypercent = (yclick * 1.0) / height;

					// get the top left and bottom right co-ordinates of the map
					double ytop = Double.parseDouble(doc_id.substring(0, doc_id.indexOf('`'))) * -1;
					doc_id = doc_id.substring(doc_id.indexOf('`') + 1, doc_id.length());
					double xleft = Double.parseDouble(doc_id.substring(0, doc_id.indexOf('`')));
					doc_id = doc_id.substring(doc_id.indexOf('`') + 1, doc_id.length());
					double ybot = Double.parseDouble(doc_id.substring(0, doc_id.indexOf('`'))) * -1;
					doc_id = doc_id.substring(doc_id.indexOf('`') + 1, doc_id.length());
					double xright = Double.parseDouble(doc_id.substring(0, doc_id.indexOf('`')));
					doc_id = doc_id.substring(doc_id.indexOf('`') + 1, doc_id.length());

					// calculate the map co-ordinates of the mouse click
					xpercent = ((xright - xleft) * xpercent) + xleft;
					ypercent = ((ybot - ytop) * ypercent) + ytop;

					// search the tree for nearby place names
					namesInList.clear();
					findName(xpercent, ypercent, 0.1, 0.1, tree, true);

					int namesInArraySize = namesInList.size();
					String returnNames[] = new String[namesInArraySize];

					//put the names in an array
					for (int ri = 0; namesInList.size() > 0; ri++)
					{
						returnNames[ri] = namesInList.getFirst();
						returnNames[ri] = returnNames[ri].substring(0, returnNames[ri].indexOf('`'));
						namesInList.removeFirst();
					}

					//sort the names
					Arrays.sort(returnNames);
					doc_content = "\n<script>\nfunction openIt(loc){\n\topener.location=loc;\n\tself.close();\n}\n</script>";
					for (int nameIndex = 0; nameIndex < namesInArraySize; nameIndex++)
					{
						String tempName = returnNames[nameIndex];
						//convert names with spaces for hyperlinks
						if (returnNames[nameIndex].indexOf(' ') != -1)
						{
							returnNames[nameIndex] = returnNames[nameIndex].replaceAll(" ", "+");
							returnNames[nameIndex] = "%22" + returnNames[nameIndex] + "%22";
						}
						// add the place name to the html
						doc_content = doc_content + "<a href=\"\" onClick=openIt('?a=q&sa=&rt=r&s=MapQuery&c=" + this.cluster_name + "&startPage=1&s1.index=none&s1.maxDocs=10&s1.query=" + returnNames[nameIndex] + "')>" + tempName + "</a><br>";
					}

				}
				catch (Exception ioexception)
				{
					ioexception.printStackTrace();
				}
			}
			else
			{

				try
				{
					//file for converting image    
					BufferedWriter bw = new BufferedWriter(new FileWriter(this.temp_files_dir + "add_x_" + uid));
					;
					Process proc;

					// if a new search
					if (doc_id.indexOf("```") != -1)
					{
						// copy requested map to temp.jpg
						proc = Runtime.getRuntime().exec("cp " + this.files_home_dir + "maps" + File.separator + img_num + ".jpg " + temp_image_file);
						proc.waitFor();
					}

					//get the image size
					String get_size[] = { "identify", "-size", "10000", temp_image_file };
					proc = Runtime.getRuntime().exec(get_size);
					BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					img_size = br.readLine();
					proc.waitFor();
					img_size = img_size.substring(img_size.indexOf("JPEG") + 5, img_size.indexOf(" ", img_size.indexOf("JPEG") + 5));
					if (img_size.indexOf("+") != -1)
					{
						img_size = img_size.substring(0, img_size.indexOf("+"));
					}
					width = Integer.parseInt(img_size.substring(0, img_size.indexOf("x")));
					height = Integer.parseInt(img_size.substring(img_size.indexOf("x") + 1, img_size.length()));

					// if a new search
					if (doc_id.indexOf("```") != -1)
					{
						co_ordinates = doc_id.substring(doc_id.indexOf("```") + 2, doc_id.length());
						// get the number of places to mark on the map
						int x_number = Integer.parseInt(doc_id.substring(doc_id.lastIndexOf('`', doc_id.indexOf("```") - 1) + 1, doc_id.indexOf("```")));

						//write the convert command
						bw.write("convert -font helvetica -fill red -pointsize 48 ");

						for (int xi = 0; xi < x_number; xi++)
						{

							// get the co-ordinates of the place name
							double xco = Double.parseDouble(co_ordinates.substring(co_ordinates.lastIndexOf('`', co_ordinates.lastIndexOf('`') - 1) + 1, co_ordinates.lastIndexOf('`', co_ordinates.lastIndexOf('`') - 1) + 7));
							double yco = Double.parseDouble(co_ordinates.substring(co_ordinates.lastIndexOf('`') + 1, co_ordinates.length()));
							if (xi != x_number - 1)
								co_ordinates = co_ordinates.substring(0, co_ordinates.lastIndexOf('`', co_ordinates.lastIndexOf('`') - 1));

							int index = 0;
							index = doc_id.indexOf('`') + 2;

							// get the zoom ratio and the top left and bottom right co-ordinates of the map
							ratio = 0.4;
							double tly = Double.parseDouble(doc_id.substring(index, index + 5));
							index = doc_id.indexOf('`', index) + 1;
							double tlx = Double.parseDouble(doc_id.substring(index, index + 6));
							index = doc_id.indexOf('`', index) + 2;
							double bry = Double.parseDouble(doc_id.substring(index, index + 5));
							index = doc_id.indexOf('`', index) + 1;
							double brx = Double.parseDouble(doc_id.substring(index, index + 6));
							index = doc_id.indexOf('`', index) + 1;
							double orient = Double.parseDouble(doc_id.substring(index, doc_id.indexOf('`', index)));

							// find the centre of the map
							double xcent = ((brx - tlx) / 2) + tlx;
							double ycent = ((bry - tly) / 2) + tly;

							// get the orientation of the map
							orient = Math.toRadians(orient);

							// rotate the co-ordinates around the centre of the map 
							xco = xco - xcent;
							yco = yco - ycent;
							double oldx = xco;
							xco = xco * Math.cos(orient) - yco * Math.sin(orient);
							yco = oldx * Math.sin(orient) + yco * Math.cos(orient);
							xco = xco + xcent;
							yco = yco + ycent;
							xco = (xco - tlx) / (brx - tlx);
							yco = (yco - tly) / (bry - tly);

							// calculate the pixels for placing the mark
							width = (int) (xco * width) - 5;
							height = (int) (yco * height) - 5;

							// write the options for convert
							bw.write("-draw 'text " + width + "," + height + " \"x\"' ");
							bw.flush();

							// reset the width and height variables for the image
							width = Integer.parseInt(img_size.substring(0, img_size.indexOf("x")));
							height = Integer.parseInt(img_size.substring(img_size.indexOf("x") + 1, img_size.length()));
						}
						doc_id = doc_id.substring(0, doc_id.lastIndexOf('`', doc_id.indexOf("```") - 1));

						// write the end of the convert command, then command for zooming image to the right level, then command to copy the blank image over legend.jpg
						bw.write(temp_image_file + " " + temp_image_file + ";convert -scale " + ((int) (ratio * width)) + "x" + ((int) (ratio * height)) + " " + temp_image_file + " " + temp_image_file2 + ";cp " + blank_file + " " + legend_file);

						// reset legend number
						leg_num = 0;
					}

					else
					// if changing zoom level
					if (doc_id.substring(doc_id.lastIndexOf('`'), doc_id.length()).indexOf('.') != -1)
					{
						// get the ratio
						ratio = Double.parseDouble(doc_id.substring(doc_id.lastIndexOf('`') + 1, doc_id.length()));
						// write the command for scaling image
						bw.write("convert -scale " + ((int) (ratio * width)) + "x" + ((int) (ratio * height)) + " " + temp_image_file + " " + temp_image_file2);
						doc_id = doc_id.substring(0, doc_id.lastIndexOf('`'));
					}

					// if adding locations to map
					else
					{

						// get the location type to add
						String restricter = doc_id.substring(doc_id.lastIndexOf('`') + 1, doc_id.length());
						doc_id = doc_id.substring(0, doc_id.lastIndexOf('`'));

						// get the number for the legend image
						leg_num = Integer.parseInt(doc_id.substring(doc_id.lastIndexOf('`') + 1, doc_id.length()));
						doc_id = doc_id.substring(0, doc_id.lastIndexOf('`'));

						//open file for location type
						BufferedReader inType = new BufferedReader(new FileReader(this.files_home_dir + "place_types" + File.separator + restricter + ".txt"));

						//check through the file and add any places that are in the bounds of this map.

						// check value. set to true if a place to add is found
						boolean add_place_type = false;

						int index = 0;
						index = doc_id.indexOf('`') + 2;
						// get zoom ratio
						ratio = Double.parseDouble(doc_id.substring(doc_id.lastIndexOf('`') + 1, doc_id.length()));
						// get top left and bottom right co-ordinates of the map
						double tly = Double.parseDouble(doc_id.substring(index, index + 5));
						index = doc_id.indexOf('`', index) + 1;
						double tlx = Double.parseDouble(doc_id.substring(index, index + 6));
						index = doc_id.indexOf('`', index) + 2;
						double bry = Double.parseDouble(doc_id.substring(index, index + 5));
						index = doc_id.indexOf('`', index) + 1;
						double brx = Double.parseDouble(doc_id.substring(index, index + 6));
						index = doc_id.indexOf('`', index) + 1;
						// get orientation of the map
						double orient = Double.parseDouble(doc_id.substring(index, doc_id.indexOf('`', index)));
						// calculate centre of map
						double xcent = ((brx - tlx) / 2) + tlx;
						double ycent = ((bry - tly) / 2) + tly;
						orient = Math.toRadians(orient);

						String type_point = "";
						double xco = 0.0;
						double yco = 0.0;

						// read the file
						while (inType.ready())
						{
							//read a line
							type_point = inType.readLine();

							// get the co-ordinates of the point
							xco = Double.parseDouble(type_point.substring(type_point.lastIndexOf('`') + 1, type_point.length()));
							yco = Double.parseDouble(type_point.substring(type_point.lastIndexOf('`', type_point.lastIndexOf('`') - 1) + 2, type_point.lastIndexOf('`')));

							// if it is within the co-ordinates of the map
							if (xco >= tlx && xco < brx && yco >= tly && yco < bry)
							{
								// rotate the point around the centre according to map orientation
								xco = xco - xcent;
								yco = yco - ycent;
								double oldx = xco;
								xco = xco * Math.cos(orient) - yco * Math.sin(orient);
								yco = oldx * Math.sin(orient) + yco * Math.cos(orient);
								xco = xco + xcent;
								yco = yco + ycent;
								// get the pixels for where to place the mark
								xco = (xco - tlx) / (brx - tlx);
								yco = (yco - tly) / (bry - tly);
								width = (int) (xco * width) - 5;
								height = (int) (yco * height) - 5;

								//if this is the first point to be added
								if (!add_place_type)
								{
									// write the initial convert command
									bw.write("convert -font helvetica -fill red -pointsize 36 ");
									// toggle check value
									add_place_type = true;
								}

								// write the options for the convert command
								bw.write("-draw 'text " + width + "," + height + " \"" + leg_num + "\"' ");
								bw.flush();

								// reset width and height variables for the image
								width = Integer.parseInt(img_size.substring(0, img_size.indexOf("x")));
								height = Integer.parseInt(img_size.substring(img_size.indexOf("x") + 1, img_size.length()));
							}
						}

						//if there are places to mark on the map
						if (add_place_type)
						{
							// finish the convert command and write command for scaling image
							bw.write(temp_image_file + " " + temp_image_file + ";convert -scale " + ((int) (ratio * width)) + "x" + ((int) (ratio * height)) + " " + temp_image_file + " " + temp_image_file2);

							// open file for converting the legend command
							BufferedWriter buf = new BufferedWriter(new FileWriter(this.temp_files_dir + "add_l_" + uid));
							if (leg_num == 1)
								buf.write("cp " + blank_file + " " + legend_file + ";");
							// write the command for adding to the legend
							buf.write("convert -font helvetica -fill red -pointsize 12 -draw 'text 15," + (leg_num * 15 + 20) + " \"" + leg_num + " " + getType(restricter) + "\"' " + legend_file + " " + legend_file);
							buf.flush();
							buf.close();
							// execute the command for the legend image
							proc = Runtime.getRuntime().exec("sh " + this.temp_files_dir + "add_l_" + uid);
							proc.waitFor();
						}
						inType.close();
					}
					bw.flush();
					bw.close();

					// execute the convert commands etc.
					proc = Runtime.getRuntime().exec("sh " + this.temp_files_dir + "add_x_" + uid);
					proc.waitFor();

				}
				catch (Exception ioe)
				{
					ioe.printStackTrace();
				}

				//write the html for the document

				String doc_content_head = "?a=d&c=" + this.cluster_name + "&dt=map&d=";
				doc_content = "<td valign=\"top\"><script>document.write('" + img_left + "legend_" + uid + ".jpg" + "?'+new Date().getTime())</script>\"></td>";
				doc_content = doc_content + "<td><script>document.write('" + img_left + "temp_" + uid + "_2.jpg?'+new Date().getTime())</script>\" onclick=\"imgClickHandler(event, this, '" + img_num + "')\"></td>";

				// replace _httpimg_ with the correct address
				doc_content = "<table><tr>" + doc_content + "</tr></table>";

				String javascript = getJavascript(ratio, doc_id, leg_num);

				doc_content = "<center>You are currently viewing the map at " + (int) (ratio * 100) + "%<br>Select a radio button for a new zoom level.</center>" + javascript + doc_content;
				doc_content = doc_content.replaceAll("zoomhead", doc_content_head);
				doc_content = doc_content.replaceAll("placeChooserImage", doc_content_head + "imageChooser`" + doc_id + "`" + ratio);

				// read the file for the navigation thumbnails
				try
				{
					BufferedReader infile = new BufferedReader(new FileReader(this.temp_files_dir + "links" + uid));
					doc_content = doc_content + infile.readLine();
					infile.close();
				}
				catch (Exception ioexc)
				{
					ioexc.printStackTrace();
				}
			}

			// put the html in a text node
			Element text_doc = result_doc.createElement(GSXML.DOC_NODE_ELEM);
			text_doc.setAttribute(GSXML.NODE_ID_ATT, doc_id);
			GSXML.addDocText(text_doc, doc_content);
			doc_list.appendChild(text_doc);

		}
		return result;
	}

	// resolves location codes for places from LINZ database
	private String getLocation(String location)
	{
		String read;
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(this.files_home_dir + "files" + File.separator + "landdist.txt"));
			in.readLine();
			while (in.ready())
			{
				read = in.readLine();
				if (read.substring(0, 2).equals(location))
				{
					in.close();
					return read.substring(3, read.length());
				}
			}
		}
		catch (Exception e)
		{
		}
		return "";
	}

	// resolves type codes for the LINZ database
	private String getType(String type)
	{
		String read;
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(this.files_home_dir + "files" + File.separator + "pointdes.txt"));
			in.readLine();
			while (in.ready())
			{
				read = in.readLine();
				if (read.substring(0, read.indexOf('`')).toLowerCase().equals(type.toLowerCase()))
				{
					in.close();
					return read.substring(read.indexOf('`') + 1, read.indexOf(':'));
				}
			}
		}
		catch (Exception e)
		{
		}
		return "";
	}

	//recursive pre-order traversal of the 2-dimensional tree
	// x & y are co-ordinates of mouse click, xoff & yoff are the distance in degrees away from x & y to search,
	// d is the current node of the tree, xcomp controls whether searching is by x co-ordinate or y co-ordinate.
	private void findName(double x, double y, double xoff, double yoff, DefaultMutableTreeNode d, boolean xcomp)
	{
		//if a leaf node return.  All leaf nodes are empty
		if (d.isLeaf())
			return;
		//get coordinates of the current node
		double xco = Double.parseDouble(((String) d.getUserObject()).substring(((String) d.getUserObject()).length() - 6, ((String) d.getUserObject()).length()));
		double yco = Double.parseDouble(((String) d.getUserObject()).substring(((String) d.getUserObject()).length() - 12, ((String) d.getUserObject()).length() - 7));
		//if in range
		if ((x - xoff) < xco && (x + xoff) > xco && (y - yoff) < yco && (y + yoff) > yco)
		{
			//add to list
			namesInList.addFirst((String) d.getUserObject());
		}
		//if comparing the x axis
		if (xcomp)
		{
			//if need to search left subtree
			if ((x - xoff) < xco)
				findName(x, y, xoff, yoff, (DefaultMutableTreeNode) d.getChildAt(0), !xcomp);
			//if need to search right subtree
			if ((x + xoff) > xco)
				findName(x, y, xoff, yoff, (DefaultMutableTreeNode) d.getChildAt(1), !xcomp);
		}
		else
		{
			//if need to search left subtree
			if ((y - yoff) < yco)
				findName(x, y, xoff, yoff, (DefaultMutableTreeNode) d.getChildAt(0), !xcomp);
			//if need to search right subtree
			if ((y + yoff) > yco)
				findName(x, y, xoff, yoff, (DefaultMutableTreeNode) d.getChildAt(1), !xcomp);
		}
	}

	// a whole lot of stuff for the html that is hidden at the bottom here because it clutters more than I already have further up!
	private String getJavascript(double ratio, String doc_id, int leg_num)
	{
		String javascript = "\n<script>\nfunction imgClickHandler (evt, img, num) {\n\tif (window.event){\n\t\tvar chooserString = 'placeChooserImage' + '```' + window.event.offsetX + '`' + window.event.offsetY\n\t\tmywindow = window.open(chooserString,'Choose','width=600, height=500, scrollbars=yes, resizable=yes, toolbar=no, location=no, status=yes, menubar=no');\n\t\t//alert(window.event.offsetX + ':' + window.event.offsetY + ':' + num);\n\t}\n\telse if (evt.target) {\n\t\tvar coords = {x: 0, y: 0 };\n\t\tvar el = evt.target;\n\t\tdo {\n\t\t\tcoords.x += el.offsetLeft;\n\t\t\tcoords.y += el.offsetTop;\n\t\t}\n\t\twhile ((el = el.offsetParent));\n\t\tvar offsetX = evt.clientX - coords.x;\n\t\tvar offsetY = evt.clientY - coords.y;\n\t\talert(offsetX + ':' + offsetY + ':' + num);\n\t}\n}\nfunction leapTo (link)\n{\n\tvar new_url=link;\n\tif (  (new_url != \"\")  &&  (new_url != null)  ) \n\t\twindow.location=new_url;\n\telse \n\t\talert(\"You must make a selection.\");\n}\nfunction jumpTo (front,link,back)\n{\n\tvar new_url=front+link+back;\n\tif (  (new_url != \"\")  &&  (new_url != null)  ) \n\t\twindow.location=new_url;\n\telse \n\t\talert(\"You must make a selection.\");\n}// Deactivate Cloaking -->\n</script>";

		String radioButtons = "\n<center><FORM>";
		if (ratio != 0.4)
			radioButtons = radioButtons + "\n\t<INPUT TYPE=\"radio\" NAME=\"buttons1\" onClick=\"leapTo('zoomhead" + doc_id + "`0.4')\">40%";
		if (ratio != 0.5)
			radioButtons = radioButtons + "\n\t<INPUT TYPE=\"radio\" NAME=\"buttons1\" onClick=\"leapTo('zoomhead" + doc_id + "`0.5')\">50%";
		if (ratio != 0.6)
			radioButtons = radioButtons + "\n\t<INPUT TYPE=\"radio\" NAME=\"buttons1\" onClick=\"leapTo('zoomhead" + doc_id + "`0.6')\">60%";
		if (ratio != 0.7)
			radioButtons = radioButtons + "\n\t<INPUT TYPE=\"radio\" NAME=\"buttons1\" onClick=\"leapTo('zoomhead" + doc_id + "`0.7')\">70%";
		if (ratio != 0.8)
			radioButtons = radioButtons + "\n\t<INPUT TYPE=\"radio\" NAME=\"buttons1\" onClick=\"leapTo('zoomhead" + doc_id + "`0.8')\">80%";
		if (ratio != 0.9)
			radioButtons = radioButtons + "\n\t<INPUT TYPE=\"radio\" NAME=\"buttons1\" onClick=\"leapTo('zoomhead" + doc_id + "`0.9')\">90%";
		if (ratio != 1.0)
			radioButtons = radioButtons + "\n\t<INPUT TYPE=\"radio\" NAME=\"buttons1\" onClick=\"leapTo('zoomhead" + doc_id + "`1.0')\">100%";
		radioButtons = radioButtons + "\n</FORM></center>";

		leg_num = leg_num + 1;

		String doc_link = "?a=d&c=" + this.cluster_name + "&dt=map&d=" + doc_id + "`" + ratio + "`" + leg_num + "`";

		StringBuffer dropDownBox = new StringBuffer();

		dropDownBox.append("Add Locations Of Type: <form name=\"myForm\"><select name=\"s1index\" onChange=\"jumpTo('");
		dropDownBox.append(doc_link);
		dropDownBox.append("',document.myForm.s1index.options[document.myForm.s1index.selectedIndex].value,'')\">\n");

		for (int i = 0; i < this.index_name_list.size(); i++)
		{
			String name = this.index_name_list.get(i);
			dropDownBox.append("<option value=\"");
			dropDownBox.append(name);
			dropDownBox.append("\">");
			String display = this.index_display_list.get(i);
			dropDownBox.append(display);
			dropDownBox.append("\n");
		}
		// for each option, Malcolm had onClick=\"leapTo('"+doc_link+name+"')\" - dont seem to need this
		dropDownBox.append("</select></form>\n");

		return javascript + radioButtons + dropDownBox;
	}
}
