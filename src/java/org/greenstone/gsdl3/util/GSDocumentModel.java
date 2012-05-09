package org.greenstone.gsdl3.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.greenstone.gsdl3.core.MessageRouter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GSDocumentModel
{
	//The two archive databases
	protected static final String ARCHIVEINFSRC = "archiveinf-src";
	protected static final String ARCHIVEINFDOC = "archiveinf-doc";

	//Set operations
	public static final int OPERATION_REPLACE = 1;
	public static final int OPERATION_INSERT_BEFORE = 2;
	public static final int OPERATION_INSERT_AFTER = 3;
	public static final int OPERATION_APPEND = 4;

	//Duplicate and move operations
	public static final int OPERATION_TYPE_DOC_TO_DOC = 1;
	public static final int OPERATION_TYPE_DOC_TO_SEC = 2;
	public static final int OPERATION_TYPE_SEC_TO_DOC = 3;
	public static final int OPERATION_TYPE_SEC_TO_SEC = 4;

	//Error codes
	public static final int NO_ERROR = 0;
	public static final int ERROR_OID_NOT_SPECIFIED = -1;
	public static final int ERROR_COLLECTION_NOT_SPECIFIED = -2;
	public static final int ERROR_SOURCE_DOCUMENT_OR_SECTION_DOES_NOT_EXIST = -3;
	public static final int ERROR_DESTINATION_DOCUMENT_OR_SECTION_DOES_NOT_EXIST = -4;
	public static final int ERROR_DESTINATION_DOCUMENT_OR_SECTION_ALREADY_EXISTS = -5;
	public static final int ERROR_COULD_NOT_DUPLICATE = -6;
	public static final int ERROR_COULD_NOT_MOVE = -7;
	public static final int ERROR_DOC_XML_COULD_NOT_BE_CREATED = -8;
	public static final int ERROR_EXCEPTION_CREATING_DOC_XML_FILE = -9;
	public static final int ERROR_METADATA_NAME_NOT_SPECIFIED = -10;
	public static final int ERROR_METADATA_VALUE_NOT_SPECIFIED = -11;
	public static final int ERROR_COULD_NOT_RETRIEVE_DOC_XML = -12;
	public static final int ERROR_COULD_NOT_WRITE_TO_DOC_XML = -13;
	public static final int ERROR_COULD_NOT_RETRIEVE_SECTION = -14;
	public static final int ERROR_COULD_NOT_OPEN_DATABASE = -15;
	public static final int ERROR_DATA_NOT_FOUND_IN_DATABASE = -16;
	public static final int ERROR_COULD_NOT_DELETE = -16;
	public static final int ERROR_OID_INCORRECT_FORMAT = -17;
	public static final int ERROR_INVALID_MERGE = -18;
	public static final int ERROR_INVALID_METADATA_POSITION = -19;
	public static final int ERROR_INVALID_SPLIT = -20;
	public static final int ERROR_DESTINATION_OID_NOT_SPECIFIED = -21;
	public static final HashMap<Integer, String> _errorMessageMap;

	static
	{
		//Corresponding error messages
		HashMap<Integer, String> errorMessageMap = new HashMap<Integer, String>();
		errorMessageMap.put(ERROR_OID_NOT_SPECIFIED, "OID not specified");
		errorMessageMap.put(ERROR_COLLECTION_NOT_SPECIFIED, "Collection not specified");
		errorMessageMap.put(ERROR_SOURCE_DOCUMENT_OR_SECTION_DOES_NOT_EXIST, "The specified source document or section does not exist");
		errorMessageMap.put(ERROR_DESTINATION_DOCUMENT_OR_SECTION_DOES_NOT_EXIST, "The specified destination document or section does not exist");
		errorMessageMap.put(ERROR_DESTINATION_DOCUMENT_OR_SECTION_ALREADY_EXISTS, "The specified destination document or section already exists");
		errorMessageMap.put(ERROR_COULD_NOT_DUPLICATE, "There was an error duplicating document or section");
		errorMessageMap.put(ERROR_COULD_NOT_MOVE, "There was an error moving document or section");
		errorMessageMap.put(ERROR_DOC_XML_COULD_NOT_BE_CREATED, "The doc.xml file already exists or could not be created");
		errorMessageMap.put(ERROR_EXCEPTION_CREATING_DOC_XML_FILE, "There was an exception while creating the doc.xml file");
		errorMessageMap.put(ERROR_METADATA_NAME_NOT_SPECIFIED, "The name of the requested metadata was not specified");
		errorMessageMap.put(ERROR_METADATA_VALUE_NOT_SPECIFIED, "The new value for this metadata was not specified");
		errorMessageMap.put(ERROR_COULD_NOT_RETRIEVE_DOC_XML, "Could not retrieve the necessary doc.xml file");
		errorMessageMap.put(ERROR_COULD_NOT_WRITE_TO_DOC_XML, "There was an error writing to the doc.xml file");
		errorMessageMap.put(ERROR_COULD_NOT_RETRIEVE_SECTION, "There was an error retrieving the specified section from the doc.xml file");
		errorMessageMap.put(ERROR_COULD_NOT_OPEN_DATABASE, "There was an error opening the archive database");
		errorMessageMap.put(ERROR_DATA_NOT_FOUND_IN_DATABASE, "The specified information could not be found in the database");
		errorMessageMap.put(ERROR_COULD_NOT_DELETE, "There was an error deleting this document");
		errorMessageMap.put(ERROR_OID_INCORRECT_FORMAT, "The given OID was not in the correct format");
		errorMessageMap.put(ERROR_INVALID_MERGE, "Merge can only be performed on two sections of the same level or a section and it's parent. Also the destination section cannot have any child sections");
		errorMessageMap.put(ERROR_INVALID_METADATA_POSITION, "There is no metadata at the given position");
		errorMessageMap.put(ERROR_INVALID_SPLIT, "A split at the given location is not possible, either the section does not have text or the split point does not exist");
		errorMessageMap.put(ERROR_DESTINATION_OID_NOT_SPECIFIED, "The destination OID was not specified");
		_errorMessageMap = errorMessageMap;
	}

	protected int _errorStatus = NO_ERROR;

	protected String _siteHome;
	protected Document _mainDoc;
	protected MessageRouter _router;
	protected HashMap<String, Document> _docCache = new HashMap<String, Document>();

	public GSDocumentModel(String siteHome, Document mainDocument, MessageRouter router)
	{
		_siteHome = siteHome;
		_mainDoc = mainDocument;
		_router = router;
	}

	/**
	 * Can be used to create a document or create a section of a document
	 * 
	 * @param oid
	 *            is the identifier of the document/section to create.
	 * @param collection
	 *            is the collection we want to create the document/section in.
	 */
	public void documentCreate(String oid, String collection, UserContext userContext)
	{
		_errorStatus = NO_ERROR;
		//If the collection is not specified then we cannot continue
		if (collection == null || collection.equals(""))
		{
			_errorStatus = ERROR_COLLECTION_NOT_SPECIFIED;
			return;
		}

		//If the collection is not specified then we cannot continue
		if (oid == null || oid.equals(""))
		{
			_errorStatus = ERROR_OID_NOT_SPECIFIED;
			return;
		}

		if (archiveCheckDocumentOrSectionExists(oid, collection, userContext))
		{
			_errorStatus = ERROR_DESTINATION_DOCUMENT_OR_SECTION_ALREADY_EXISTS;
			return;
		}

		//Check if the OID is the OID for a section
		boolean section = false;
		if (oid.contains("."))
		{
			section = true;
		}

		if (!section)
		{
			//Create a basic doc.xml file to go in the new folder
			documentXMLCreateDocXML(oid, collection, userContext);
		}
		else
		{
			documentXMLCreateSection(oid, collection, userContext);
		}
	}

	/**
	 * Can be used to delete a document or section
	 * 
	 * @param oid
	 *            is the identifier of the document/section to delete.
	 * @param collection
	 *            is the collection to delete the document/section from.
	 */
	public void documentDelete(String oid, String collection, UserContext userContext)
	{
		_errorStatus = NO_ERROR;
		if (oid == null || oid.equals(""))
		{
			_errorStatus = ERROR_OID_NOT_SPECIFIED;
			return;
		}
		else if (collection == null || collection.equals(""))
		{
			_errorStatus = ERROR_COLLECTION_NOT_SPECIFIED;
			return;
		}

		if (!archiveCheckDocumentOrSectionExists(oid, collection, userContext))
		{
			_errorStatus = ERROR_SOURCE_DOCUMENT_OR_SECTION_DOES_NOT_EXIST;
			return;
		}

		//Check if the OID is the OID for a section
		boolean section = false;
		if (oid.contains("."))
		{
			section = true;
		}

		if (!section)
		{
			String archivesFile = archiveGetDocumentFilePath(oid, collection, userContext);
			String archivesFolder = archivesFile.substring(0, archivesFile.lastIndexOf(File.separator));
			File dirToDelete = new File(archivesFolder);

			if (!dirToDelete.exists() || !dirToDelete.isDirectory() || !deleteDirectory(dirToDelete))
			{
				_errorStatus = ERROR_COULD_NOT_DELETE;
				return;
			}

			//Remove the entry from the archive database 
			archiveRemoveEntryFromDatabase(oid, collection, userContext);
		}
		else
		{
			documentXMLDeleteSection(oid, collection, userContext);
		}
	}

	/**
	 * Can be used to copy a document or section from one place to another.
	 * 
	 * @param oid
	 *            is the identifier of the document/section that is to be
	 *            copied.
	 * @param collection
	 *            is the collection the source document resides in.
	 * @param newOID
	 *            is the new identifier for the document/section (it cannot
	 *            already exist).
	 * @param newCollection
	 *            is the collection the new document/section will be copied to.
	 *            If this is null then the collection parameter will be used
	 *            instead.
	 */
	public void documentMoveOrDuplicate(String oid, String collection, String newOID, String newCollection, int operation, boolean move, UserContext userContext)
	{
		if ((_errorStatus = checkOIDandCollection(oid, collection, userContext)) != NO_ERROR)
		{
			return;
		}

		//If a new collection is not specified then assume the collection of the original document
		if (newCollection == null || newCollection.equals(""))
		{
			newCollection = collection;
		}

		//Generate an OID for the duplicate if we are not given one
		if (newOID == null || newOID.equals(""))
		{
			_errorStatus = ERROR_DESTINATION_OID_NOT_SPECIFIED;
			return;
		}

		boolean requiresDatabaseEntry = false;
		int operationType = getOperation(oid, newOID);
		switch (operationType)
		{
		case OPERATION_TYPE_DOC_TO_DOC:
		{
			String archiveDir = archiveGetDocumentFilePath(oid, collection, userContext);
			if (_errorStatus != NO_ERROR)
			{
				return;
			}

			//Remove doc.xml from the file name
			archiveDir = archiveDir.substring(0, archiveDir.lastIndexOf(File.separator));
			File dirToDuplicate = new File(archiveDir);

			//This is possibly not the best way to name the new directory
			File newDir = new File(archiveDir.substring(0, archiveDir.lastIndexOf(File.separator) + File.separator.length()) + newOID);

			if (dirToDuplicate.exists() && dirToDuplicate.isDirectory() && !newDir.exists())
			{
				if (!copyDirectory(dirToDuplicate, newDir))
				{
					_errorStatus = ERROR_COULD_NOT_DUPLICATE;
					return;
				}
			}
			else
			{
				_errorStatus = ERROR_COULD_NOT_DUPLICATE;
				return;
			}

			if (move)
			{
				deleteDirectory(dirToDuplicate);
			}

			requiresDatabaseEntry = true;
			break;
		}
		case OPERATION_TYPE_DOC_TO_SEC:
		{
			Document originalDocument = getDocXML(oid, collection, userContext);
			Element originalSection = getTopLevelSectionElement(originalDocument);

			documentXMLCreateSection(newOID, newCollection, userContext);
			if (_errorStatus != NO_ERROR)
			{
				return;
			}

			documentXMLSetSection(newOID, newCollection, originalSection, operation, userContext);

			if (move)
			{
				String archiveDirStr = archiveGetDocumentFilePath(oid, collection, userContext);
				if (_errorStatus != NO_ERROR)
				{
					return;
				}

				File archiveDir = new File(archiveDirStr);

				if (archiveDir.exists() && archiveDir.isDirectory())
				{
					deleteDirectory(archiveDir);
				}
			}
			break;
		}
		case OPERATION_TYPE_SEC_TO_DOC:
		{
			Document originalDocument = getDocXML(oid, collection, userContext);
			Element originalSection = getSectionBySectionNumber(originalDocument, getSectionFromOID(oid));

			documentCreate(newOID, newCollection, userContext);
			if (_errorStatus != NO_ERROR)
			{
				return;
			}

			documentXMLCreateSection(newOID, newCollection, userContext);
			if (_errorStatus != NO_ERROR)
			{
				return;
			}

			documentXMLSetSection(newOID, newCollection, originalSection, operation, userContext);

			if (move)
			{
				originalDocument = getDocXML(oid, collection, userContext);
				originalSection.getParentNode().removeChild(originalSection);

				//Write the new change back into the file
				if (!writeXMLFile(originalDocument, oid, collection, userContext))
				{
					_errorStatus = ERROR_COULD_NOT_WRITE_TO_DOC_XML;
					return;
				}
			}

			requiresDatabaseEntry = true;
			break;
		}
		case OPERATION_TYPE_SEC_TO_SEC:
		{
			Document originalDocument = getDocXML(oid, collection, userContext);
			Element originalSection = getSectionBySectionNumber(originalDocument, getSectionFromOID(oid));

			if (operation == OPERATION_REPLACE)
			{
				documentXMLCreateSection(newOID, newCollection, userContext);
				if (_errorStatus != NO_ERROR)
				{
					return;
				}
			}

			documentXMLSetSection(newOID, newCollection, originalSection, operation, userContext);
			if (_errorStatus != NO_ERROR)
			{
				return;
			}

			if (move)
			{
				originalDocument = getDocXML(oid, collection, userContext);
				originalSection.getParentNode().removeChild(originalSection);

				//Write the new change back into the file
				if (!writeXMLFile(originalDocument, oid, collection, userContext))
				{
					_errorStatus = ERROR_COULD_NOT_WRITE_TO_DOC_XML;
					return;
				}
			}

			break;
		}
		}

		if (requiresDatabaseEntry)
		{
			HashMap<String, ArrayList<String>> entries = new HashMap<String, ArrayList<String>>();
			ArrayList<String> values = new ArrayList<String>();
			values.add(newOID + "/doc.xml");
			entries.put("doc-file", values);

			//Write the new entry to the archive database 
			archiveWriteEntryToDatabase(newOID, newCollection, entries, userContext);
		}
	}

	/**
	 * Can be used to acquire information about a given document or section.
	 * 
	 * @param oid
	 *            is the identifier of the document or section.
	 * @param collection
	 *            is the collection the document or section resides in.
	 * @param requestedInfo
	 *            is an array containing the various requests.
	 * @return This returns an array containing the requested information.
	 */
	public String[] documentGetInformation(String oid, String collection, String[] requestedInfo, UserContext userContext)
	{
		if ((_errorStatus = checkOIDandCollection(oid, collection, userContext)) != NO_ERROR)
		{
			return null;
		}

		for (int j = 0; j < requestedInfo.length; j++)
		{
			String currentRequest = requestedInfo[j];
			//TODO: Decide what info requests are valid (e.g. number of sections etc.)
			//-How many child/sibling sections
			//-Metadata keys
		}
		//TODO: Implement
		return null;
	}

	/**
	 * Can be used to merge two parts of a document together. The sections must
	 * be in the same document at the same level (e.g. D11.1.2 and D11.1.3) or a
	 * section and it's parent (e.g. D11.1.2 and D11.1). Also, the destination
	 * section cannot have any child sections.
	 * 
	 * @param oid
	 *            the identifier of the section that is to be merged.
	 * @param collection
	 *            the collection the section resides in.
	 * @param mergeOID
	 *            the identifier of the section that the source section will be
	 *            merged into.
	 */
	public void documentMerge(String oid, String collection, String mergeOID, UserContext userContext)
	{
		if ((_errorStatus = checkOIDandCollection(oid, collection, userContext)) != NO_ERROR)
		{
			return;
		}

		if ((_errorStatus = checkOIDandCollection(mergeOID, collection, userContext)) != NO_ERROR)
		{
			return;
		}

		int op = getOperation(oid, mergeOID);
		if (op != OPERATION_TYPE_SEC_TO_SEC && op != OPERATION_TYPE_SEC_TO_DOC) //We allow SEC_TO_DOC in the case that someone wants to merge D11.1 with D11 (for example)
		{
			_errorStatus = ERROR_INVALID_MERGE;
			return;
		}

		String[] sourceLevels = oid.split("\\.");
		String[] destinationLevels = mergeOID.split("\\.");

		if (destinationLevels.length > sourceLevels.length)
		{
			_errorStatus = ERROR_INVALID_MERGE;
			return;
		}

		for (int i = 0; i < sourceLevels.length - 1; i++)
		{
			if (i >= destinationLevels.length || !sourceLevels[i].equals(destinationLevels[i]))
			{
				_errorStatus = ERROR_INVALID_MERGE;
				return;
			}
		}

		Document docXML = getDocXML(oid, collection, userContext);
		if (docXML == null)
		{
			_errorStatus = ERROR_COULD_NOT_RETRIEVE_DOC_XML;
			return;
		}

		Element sourceSection = getSectionBySectionNumber(docXML, getSectionFromOID(oid));
		Element destinationSection = getSectionBySectionNumber(docXML, getSectionFromOID(mergeOID));

		//Make sure the destination Section does not have any child Sections.
		NodeList childSections = GSXML.getChildrenByTagName(destinationSection, GSXML.DOCXML_SECTION_ELEM);
		if (childSections.getLength() != 0 && sourceLevels.length == destinationLevels.length)
		{
			_errorStatus = ERROR_INVALID_MERGE;
			return;
		}

		//Get the children of the destination section so we can copy them to the source section before we overwrite the destination
		NodeList childrenToKeep = destinationSection.getChildNodes();
		ArrayList<Node> childList = new ArrayList<Node>();
		for (int i = 0; i < childrenToKeep.getLength(); i++)
		{
			//Need to put these in a list to make them easier to loop through as using a NodeList is messy
			childList.add(childrenToKeep.item(i));
		}

		//for(int i = 0; i < childrenToKeep.getLength(); i++)
		for (int i = 0; i < childList.size(); i++)
		{
			Node currentChild = childList.get(i);
			//If the child is not a <Content> node then add it to source section
			if (!currentChild.getNodeName().equals(GSXML.DOCXML_CONTENT_ELEM))
			{
				sourceSection.appendChild(currentChild);
				continue;
			}

			//Get the destination Section's Content node's text node, if it's empty then we don't need to worry about appending it
			Node destinationTextNode = currentChild.getFirstChild();
			if (destinationTextNode == null || destinationTextNode.getNodeValue() == null || destinationTextNode.getNodeValue().equals(""))
			{
				continue;
			}

			//If the source Section does not have a content then directly append the destination content
			Element sourceContent = (Element) GSXML.getChildByTagName(sourceSection, GSXML.DOCXML_CONTENT_ELEM);
			if (sourceContent == null)
			{
				sourceSection.appendChild(currentChild);
				continue;
			}

			//If the source Section's Content is empty then again we can directly append the destination content
			Node sourceTextNode = sourceContent.getFirstChild();
			if (sourceTextNode == null || sourceTextNode.getNodeValue() == null || sourceTextNode.getNodeValue().equals(""))
			{
				sourceSection.appendChild(currentChild);
				continue;
			}

			//Otherwise, set the new content to be destination + source text in that order.
			sourceTextNode.setNodeValue(destinationTextNode.getNodeValue() + " " + sourceTextNode.getNodeValue());
		}

		documentXMLSetSection(mergeOID, collection, sourceSection, OPERATION_REPLACE, userContext);
		if (_errorStatus != NO_ERROR)
		{
			return;
		}

		documentXMLDeleteSection(oid, collection, userContext);
	}

	/**
	 * Can be used to split a section into two sections (e.g. D11.2.1 will
	 * become D11.2.1 and D11.2.2). Any child section will belong to the second
	 * section (D11.2.2 in the example).
	 * 
	 * @param oid
	 *            is the identifer of the section to be split.
	 * @param collection
	 *            is the collection the section resides in.
	 * @param splitPoint
	 *            is the point in the text we want to split at.
	 */
	public void documentSplit(String oid, String collection, int splitPoint, UserContext userContext)
	{
		if ((_errorStatus = checkOIDandCollection(oid, collection, userContext)) != NO_ERROR)
		{
			return;
		}

		Document docXML = getDocXML(oid, collection, userContext);
		if (docXML == null)
		{
			_errorStatus = ERROR_COULD_NOT_RETRIEVE_DOC_XML;
			return;
		}

		Element sectionToSplit = null;
		if (!oid.contains("."))
		{
			sectionToSplit = getTopLevelSectionElement(docXML);
		}
		else
		{
			sectionToSplit = getSectionBySectionNumber(docXML, getSectionFromOID(oid));
		}

		Element content = (Element) GSXML.getChildByTagName(sectionToSplit, GSXML.DOCXML_CONTENT_ELEM);
		if (content == null)
		{
			_errorStatus = ERROR_INVALID_SPLIT;
			return;
		}

		Node textNode = content.getFirstChild();
		if (textNode == null)
		{
			_errorStatus = ERROR_INVALID_SPLIT;
			return;
		}

		String text = textNode.getNodeValue();
		if (splitPoint > text.length() - 2 || splitPoint < 1) //-1 would be the index of the last character, so the last valid split point is -2
		{
			_errorStatus = ERROR_INVALID_SPLIT;
			return;
		}

		String firstPart = text.substring(0, splitPoint);
		String secondPart = text.substring(splitPoint);

		Element newSection = docXML.createElement(GSXML.DOCXML_SECTION_ELEM);
		Element newContent = docXML.createElement(GSXML.DOCXML_CONTENT_ELEM);
		Node newTextNode = docXML.createTextNode(firstPart);
		newContent.appendChild(newTextNode);
		newSection.appendChild(newContent);

		documentXMLSetSection(oid, collection, newSection, OPERATION_INSERT_BEFORE, userContext);
		if (_errorStatus != NO_ERROR)
		{
			return;
		}
		textNode.setNodeValue(secondPart);

		//Write the new change back into the file
		if (!writeXMLFile(docXML, oid, collection, userContext))
		{
			_errorStatus = ERROR_COULD_NOT_WRITE_TO_DOC_XML;
		}
	}

	/**
	 * Creates a basic doc.xml file with minimal contents.
	 * 
	 * @param oid
	 *            is the identifier of the document to be created.
	 * @param collection
	 *            is the collection the new document will reside in.
	 */
	public void documentXMLCreateDocXML(String oid, String collection, UserContext userContext)
	{
		_errorStatus = NO_ERROR;
		try
		{
			String s = File.separator;

			String docFolderPath = _siteHome + s + "collect" + s + collection + s + "import" + s + oid;
			File docFolder = new File(docFolderPath);

			if (!docFolder.exists())
			{
				if (!docFolder.mkdirs())
				{
					_errorStatus = ERROR_DOC_XML_COULD_NOT_BE_CREATED;
					return;
				}
			}

			File docFile = new File(docFolderPath + s + "doc.xml");
			if (!docFile.exists() && !docFile.createNewFile())
			{
				_errorStatus = ERROR_DOC_XML_COULD_NOT_BE_CREATED;
				return;
			}

			BufferedWriter bw = new BufferedWriter(new FileWriter(docFile));
			bw.write("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n");
			bw.write("<!DOCTYPE Archive SYSTEM \"http://greenstone.org/dtd/Archive/1.0/Archive.dtd\">\n");
			bw.write("<Archive>\n");
			bw.write("  <Section>\n");
			bw.write("    <Description>\n");
			bw.write("      <Metadata name=\"Identifier\">" + oid + "</Metadata>\n");
			bw.write("      <Metadata name=\"dc.Title\">UNTITLED DOCUMENT</Metadata>\n");
			bw.write("    </Description>\n");
			bw.write("    <Content>\n");
			bw.write("    </Content>\n");
			bw.write("  </Section>\n");
			bw.write("</Archive>\n");
			bw.close();

			Document docXML = null;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			docXML = db.parse(docFile);

			_docCache.put(oid + "__" + collection, docXML);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			_errorStatus = ERROR_EXCEPTION_CREATING_DOC_XML_FILE;
			return;
		}
	}

	/**
	 * Gets a metadata value from a document or section.
	 * 
	 * @param oid
	 *            is the identifier of the section or document to get metadata
	 *            from.
	 * @param collection
	 *            is the collection the section or document resides in.
	 * @param metadataName
	 *            is the name of metadata to retrieve
	 * @return an array of metadata elements containing the resquested metadata
	 */
	public ArrayList<Element> documentXMLGetMetadata(String oid, String collection, String metadataName, UserContext userContext)
	{
		if ((_errorStatus = checkOIDandCollection(oid, collection, userContext)) != NO_ERROR)
		{
			return null;
		}
		else if (metadataName == null || metadataName.equals(""))
		{
			_errorStatus = ERROR_METADATA_NAME_NOT_SPECIFIED;
			return null;
		}

		Document docXML = getDocXML(oid, collection, userContext);
		if (docXML == null)
		{
			_errorStatus = ERROR_COULD_NOT_RETRIEVE_DOC_XML;
			return null;
		}

		return getMetadataElementsFromSection(docXML, oid, metadataName);
	}

	/**
	 * 
	 * @param oid
	 *            is the identifier of the document or section that is to have
	 *            it's metadata set.
	 * @param collection
	 *            is the collection the document/section resides in.
	 * @param metadataName
	 *            is the name of the metadata value that is to be set.
	 * @param newMetadataValue
	 *            is the new value of the metadata.
	 * @param position
	 *            specifies the position of the value to set.
	 * @param operation
	 *            can be one of OPERATION_REPLACE, OPERATION_INSERT_BEFORE,
	 *            OPERATION_INSERT_AFTER or OPERATION_APPEND.
	 */
	public void documentXMLSetMetadata(String oid, String collection, String metadataName, String newMetadataValue, int position, int operation, UserContext userContext)
	{
		if ((_errorStatus = checkOIDandCollection(oid, collection, userContext)) != NO_ERROR)
		{
			return;
		}
		else if (metadataName == null || metadataName.equals(""))
		{
			_errorStatus = ERROR_METADATA_NAME_NOT_SPECIFIED;
			return;
		}
		else if (newMetadataValue == null || newMetadataValue.equals(""))
		{
			_errorStatus = ERROR_METADATA_VALUE_NOT_SPECIFIED;
			return;
		}

		Document docXML = getDocXML(oid, collection, userContext);
		if (docXML == null)
		{
			_errorStatus = ERROR_COULD_NOT_RETRIEVE_DOC_XML;
			return;
		}

		ArrayList<Element> metadataElems = getMetadataElementsFromSection(docXML, oid, metadataName);

		if (operation != OPERATION_APPEND && metadataElems.get(position) == null)
		{
			_errorStatus = ERROR_INVALID_METADATA_POSITION;
			return;
		}

		if (operation == OPERATION_REPLACE)
		{
			metadataElems.get(position).setNodeValue(newMetadataValue);
		}
		else if (operation == OPERATION_INSERT_BEFORE)
		{
			Element newMetadata = createElementWithValue(docXML, GSXML.DOCXML_METADATA_ELEM, metadataName, newMetadataValue);
			Element existingMetadata = metadataElems.get(position);

			existingMetadata.getParentNode().insertBefore(newMetadata, existingMetadata);
		}
		else if (operation == OPERATION_INSERT_AFTER)
		{
			Element newMetadata = createElementWithValue(docXML, GSXML.DOCXML_METADATA_ELEM, metadataName, newMetadataValue);
			Element existingMetadata = metadataElems.get(position + 1);

			if (existingMetadata != null)
			{
				existingMetadata.getParentNode().insertBefore(newMetadata, existingMetadata);
			}
			else
			{
				existingMetadata = metadataElems.get(position);
				existingMetadata.getParentNode().appendChild(newMetadata);
			}
		}
		else
		{
			Element section = getSectionBySectionNumber(docXML, getSectionFromOID(oid));
			Element description = (Element) GSXML.getChildByTagName(section, GSXML.DOCXML_DESCRIPTION_ELEM);
			if (description == null)
			{
				description = docXML.createElement(GSXML.DOCXML_DESCRIPTION_ELEM);
				section.appendChild(description);
			}
			Element newMetadata = createElementWithValue(docXML, GSXML.DOCXML_METADATA_ELEM, metadataName, newMetadataValue);
			description.appendChild(newMetadata);
		}

		//Write the new change back into the file
		if (!writeXMLFile(docXML, oid, collection, userContext))
		{
			_errorStatus = ERROR_COULD_NOT_WRITE_TO_DOC_XML;
		}
	}

	/**
	 * Can be used to delete metadata at a specific position in a document or
	 * section (such as the third author of a document).
	 * 
	 * @param oid
	 *            is the identifier of the document/section to delete metadata
	 *            from.
	 * @param collection
	 *            is the collection the document resides in.
	 * @param metadataName
	 *            is the name of the metadata that is to have an item deleted.
	 * @param position
	 *            is position of the item that is to be deleted.
	 */
	public void documentXMLDeleteMetadata(String oid, String collection, String metadataName, int position, UserContext userContext)
	{
		if ((_errorStatus = checkOIDandCollection(oid, collection, userContext)) != NO_ERROR)
		{
			return;
		}
		else if (metadataName == null || metadataName.equals(""))
		{
			_errorStatus = ERROR_METADATA_NAME_NOT_SPECIFIED;
			return;
		}

		Document docXML = getDocXML(oid, collection, userContext);
		if (docXML == null)
		{
			_errorStatus = ERROR_COULD_NOT_RETRIEVE_DOC_XML;
			return;
		}

		ArrayList<Element> metadataElems = getMetadataElementsFromSection(docXML, oid, metadataName);

		if (metadataElems.get(position) != null)
		{
			metadataElems.get(position).getParentNode().removeChild(metadataElems.get(position));
		}
	}

	/**
	 * Can be used to delete all the metadata with a specific name from a
	 * document or section (e.g. all of the authors).
	 * 
	 * @param oid
	 *            is the identifier of the document or section to delete the
	 *            metadata from.
	 * @param collection
	 *            is the collection the document resides in.
	 * @param metadataName
	 *            is the name of the metadata to delete.
	 */
	public void documentXMLDeleteMetadata(String oid, String collection, String metadataName, UserContext userContext)
	{
		if ((_errorStatus = checkOIDandCollection(oid, collection, userContext)) != NO_ERROR)
		{
			return;
		}
		else if (metadataName == null || metadataName.equals(""))
		{
			_errorStatus = ERROR_METADATA_NAME_NOT_SPECIFIED;
			return;
		}

		Document docXML = getDocXML(oid, collection, userContext);
		if (docXML == null)
		{
			_errorStatus = ERROR_COULD_NOT_RETRIEVE_DOC_XML;
			return;
		}

		ArrayList<Element> metadataElems = getMetadataElementsFromSection(docXML, oid, metadataName);

		for (Element elem : metadataElems)
		{
			elem.getParentNode().removeChild(elem);
		}
	}

	/**
	 * Can be used to replace a specific metadata item of a given name, given
	 * it's value.
	 * 
	 * @param oid
	 *            is the document/section of the metadata that is to be
	 *            replaced.
	 * @param collection
	 *            is the collection the document resides in.
	 * @param metadataName
	 *            is the name of the metadata to be replaced.
	 * @param oldMetadataValue
	 *            is the old value of the metadata (the value that will be
	 *            replaced).
	 * @param newMetadataValue
	 *            is the new value of the metadata that will replace the old
	 *            value.
	 */
	public void documentXMLReplaceMetadata(String oid, String collection, String metadataName, String oldMetadataValue, String newMetadataValue, UserContext userContext)
	{
		if ((_errorStatus = checkOIDandCollection(oid, collection, userContext)) != NO_ERROR)
		{
			return;
		}
		else if (metadataName == null || metadataName.equals(""))
		{
			_errorStatus = ERROR_METADATA_NAME_NOT_SPECIFIED;
			return;
		}
		else if (newMetadataValue == null || newMetadataValue.equals(""))
		{
			_errorStatus = ERROR_METADATA_VALUE_NOT_SPECIFIED;
			return;
		}
		else if (oldMetadataValue == null || oldMetadataValue.equals(""))
		{
			_errorStatus = ERROR_METADATA_VALUE_NOT_SPECIFIED;
			return;
		}

		Document docXML = getDocXML(oid, collection, userContext);
		if (docXML == null)
		{
			_errorStatus = ERROR_COULD_NOT_RETRIEVE_DOC_XML;
			return;
		}

		ArrayList<Element> metadataElems = getMetadataElementsFromSection(docXML, oid, metadataName);

		for (Element elem : metadataElems)
		{
			Node textNode = elem.getFirstChild();

			if (textNode != null && textNode.getNodeValue().equals(oldMetadataValue))
			{
				textNode.setNodeValue(newMetadataValue);
			}
		}

		//Write the new change back into the file
		if (!writeXMLFile(docXML, oid, collection, userContext))
		{
			_errorStatus = ERROR_COULD_NOT_WRITE_TO_DOC_XML;
		}
	}

	/**
	 * Can be used to create a blank section.
	 * 
	 * @param oid
	 *            is the identifier of the section to be created.
	 * @param collection
	 *            is the collection the document resides in.
	 */
	public void documentXMLCreateSection(String oid, String collection, UserContext userContext)
	{
		_errorStatus = NO_ERROR;
		if (oid == null || oid.equals(""))
		{
			_errorStatus = ERROR_OID_NOT_SPECIFIED;
			return;
		}
		else if (collection == null || collection.equals(""))
		{
			_errorStatus = ERROR_COLLECTION_NOT_SPECIFIED;
			return;
		}

		if (oid.contains(".") && !archiveCheckDocumentOrSectionExists(oid.substring(0, oid.indexOf(".")), collection, userContext))
		{
			documentCreate(oid.substring(0, oid.indexOf(".")), collection, userContext);
			if (_errorStatus != NO_ERROR)
			{
				return;
			}
		}

		Document docXML = getDocXML(oid, collection, userContext);
		if (docXML == null)
		{
			_errorStatus = ERROR_COULD_NOT_RETRIEVE_DOC_XML;
			return;
		}

		Element topLevel = docXML.getDocumentElement();
		if (!oid.contains("."))
		{
			if (GSXML.getChildByTagName(topLevel, GSXML.DOCXML_SECTION_ELEM) == null)
			{
				Element newSection = docXML.createElement(GSXML.DOCXML_SECTION_ELEM);
				topLevel.appendChild(newSection);
			}
		}
		else
		{
			int[] intLevels = null;
			try
			{
				intLevels = oidToSectionNumberArray(oid);
			}
			catch (Exception ex)
			{
				_errorStatus = ERROR_OID_INCORRECT_FORMAT;
				return;
			}

			Element current = (Element) GSXML.getChildByTagName(topLevel, GSXML.DOCXML_SECTION_ELEM);
			if (current == null)
			{
				Element topLevelSection = docXML.createElement(GSXML.DOCXML_SECTION_ELEM);
				topLevel.appendChild(topLevelSection);
				current = topLevelSection;
			}

			for (int currentLevelValue : intLevels)
			{
				NodeList sections = GSXML.getChildrenByTagName(current, GSXML.DOCXML_SECTION_ELEM);

				if (sections.item(currentLevelValue - 1) == null)
				{
					Element latest = null;
					for (int j = 0; j < currentLevelValue - sections.getLength(); j++)
					{
						Element blankSection = docXML.createElement(GSXML.DOCXML_SECTION_ELEM);
						current.appendChild(blankSection);
						latest = blankSection;
					}
					current = latest;
				}
				else
				{
					current = (Element) sections.item(currentLevelValue - 1);
				}
			}
		}

		//Write the new change back into the file
		if (!writeXMLFile(docXML, oid, collection, userContext))
		{
			_errorStatus = ERROR_COULD_NOT_WRITE_TO_DOC_XML;
		}
	}

	/**
	 * Can be used to delete an entire section.
	 * 
	 * @param oid
	 *            is the identifier of the section to be deleted.
	 * @param collection
	 *            is the collection the document resides in.
	 */
	public void documentXMLDeleteSection(String oid, String collection, UserContext userContext)
	{
		if ((_errorStatus = checkOIDandCollection(oid, collection, userContext)) != NO_ERROR)
		{
			return;
		}

		Document docXML = getDocXML(oid, collection, userContext);
		if (docXML == null)
		{
			_errorStatus = ERROR_COULD_NOT_RETRIEVE_DOC_XML;
			return;
		}

		Element section = getSectionBySectionNumber(docXML, getSectionFromOID(oid));
		if (section == null)
		{
			_errorStatus = ERROR_COULD_NOT_DELETE;
			return;
		}

		section.getParentNode().removeChild(section);

		//Write the new change back into the file
		if (!writeXMLFile(docXML, oid, collection, userContext))
		{
			_errorStatus = ERROR_COULD_NOT_WRITE_TO_DOC_XML;
		}
	}

	/**
	 * Can be used to get a section from a document.
	 * 
	 * @param oid
	 *            is the identifier of the section to get.
	 * @param collection
	 *            is the collection the document resides in.
	 * @return the requested section.
	 */
	public Element documentXMLGetSection(String oid, String collection, UserContext userContext)
	{
		if ((_errorStatus = checkOIDandCollection(oid, collection, userContext)) != NO_ERROR)
		{
			return null;
		}

		Document docXML = getDocXML(oid, collection, userContext);
		if (docXML == null)
		{
			_errorStatus = ERROR_COULD_NOT_RETRIEVE_DOC_XML;
			return null;
		}

		Element section = null;
		if (!oid.contains("."))
		{
			section = getTopLevelSectionElement(docXML);
		}
		else
		{
			section = getSectionBySectionNumber(docXML, getSectionFromOID(oid));
		}

		if (section == null)
		{
			_errorStatus = ERROR_COULD_NOT_RETRIEVE_SECTION;
			return null;
		}

		return section;
	}

	/**
	 * Can be used to set an OID to the given section element.
	 * 
	 * @param oid
	 *            is the identifier of the section to be set.
	 * @param collection
	 *            is the collection the section will reside in.
	 * @param newSection
	 *            is the new section element.
	 * @param operation
	 *            can be one of OPERATION_REPLACE, OPERATION_INSERT_BEFORE,
	 *            OPERATION_INSERT_AFTER or OPERATION_APPEND.
	 * @throws IOException
	 */
	public void documentXMLSetSection(String oid, String collection, Element newSection, int operation, UserContext userContext)
	{
		if ((_errorStatus = checkOIDandCollection(oid, collection, userContext)) != NO_ERROR)
		{
			return;
		}

		Document docXML = getDocXML(oid, collection, userContext);
		if (docXML == null)
		{
			_errorStatus = ERROR_COULD_NOT_RETRIEVE_DOC_XML;
			return;
		}

		Element existingSection = null;
		if (!oid.contains("."))
		{
			existingSection = getTopLevelSectionElement(docXML);
		}
		else
		{
			existingSection = getSectionBySectionNumber(docXML, getSectionFromOID(oid));
		}

		if (existingSection == null)
		{
			_errorStatus = ERROR_COULD_NOT_RETRIEVE_SECTION;
			return;
		}

		Element importedSection = (Element) docXML.importNode(newSection.cloneNode(true), true);
		Node sectionParent = existingSection.getParentNode();

		if (operation == OPERATION_APPEND)
		{
			existingSection.appendChild(importedSection);
		}
		else
		{
			//Remove the attributes that are only there to help us find the section
			importedSection.removeAttribute(GSXML.NODE_ID_ATT);
			importedSection.removeAttribute(GSXML.COLLECTION_ATT);

			if (operation == OPERATION_INSERT_BEFORE || operation == OPERATION_REPLACE)
			{
				sectionParent.insertBefore(importedSection, existingSection);
			}
			else if (operation == OPERATION_INSERT_AFTER)
			{
				Node siblingNode = existingSection.getNextSibling();
				while (siblingNode != null && siblingNode.getNodeType() != Node.ELEMENT_NODE)
				{
					siblingNode = siblingNode.getNextSibling();
				}

				if (siblingNode != null)
				{
					sectionParent.insertBefore(importedSection, siblingNode);
				}
				else
				{
					sectionParent.appendChild(importedSection);
				}
			}

			if (operation == OPERATION_REPLACE)
			{
				sectionParent.removeChild(existingSection);
			}
		}

		//Write the new change back into the file
		if (!writeXMLFile(docXML, oid, collection, userContext))
		{
			_errorStatus = ERROR_COULD_NOT_WRITE_TO_DOC_XML;
			return;
		}
	}

	/**
	 * Gets the text of a given section as a string.
	 * 
	 * @param oid
	 *            is the identifier of the section to get the text from.
	 * @param collection
	 *            is the collection the document resides in.
	 * @return the text from the section.
	 */
	public String documentXMLGetText(String oid, String collection, UserContext userContext)
	{
		if ((_errorStatus = checkOIDandCollection(oid, collection, userContext)) != NO_ERROR)
		{
			return null;
		}

		Document docXML = getDocXML(oid, collection, userContext);
		if (docXML == null)
		{
			_errorStatus = ERROR_COULD_NOT_RETRIEVE_DOC_XML;
			return null;
		}

		Element section = null;
		if (!oid.contains("."))
		{
			section = getTopLevelSectionElement(docXML);
		}
		else
		{
			section = getSectionBySectionNumber(docXML, getSectionFromOID(oid));
		}

		if (section == null)
		{
			_errorStatus = ERROR_COULD_NOT_RETRIEVE_SECTION;
			return null;
		}

		Element contentNode = (Element) GSXML.getChildByTagName(section, GSXML.DOCXML_CONTENT_ELEM);
		if (contentNode == null)
		{
			return null;
		}

		Node textNode = contentNode.getFirstChild();
		if (textNode == null)
		{
			return null;
		}

		return textNode.getNodeValue();
	}

	/**
	 * Sets the text of a given section using an element.
	 * 
	 * @param oid
	 *            is the identifier of the section to set the text of.
	 * @param collection
	 *            is the collection the document resides in.
	 * @param newContent
	 *            is the new content element for the section.
	 */
	public void documentXMLSetText(String oid, String collection, Element newContent, UserContext userContext)
	{
		if ((_errorStatus = checkOIDandCollection(oid, collection, userContext)) != NO_ERROR)
		{
			return;
		}

		Document docXML = getDocXML(oid, collection, userContext);
		if (docXML == null)
		{
			_errorStatus = ERROR_COULD_NOT_RETRIEVE_DOC_XML;
			return;
		}

		Element section = null;
		if (!oid.contains("."))
		{
			section = getTopLevelSectionElement(docXML);
		}
		else
		{
			section = getSectionBySectionNumber(docXML, getSectionFromOID(oid));
		}

		if (section == null)
		{
			_errorStatus = ERROR_COULD_NOT_RETRIEVE_SECTION;
			return;
		}

		Element existingContent = (Element) GSXML.getChildByTagName(section, GSXML.DOCXML_CONTENT_ELEM);

		//Remove the attributes that are only there to help us find the content
		newContent.removeAttribute(GSXML.NODE_ID_ATT);
		newContent.removeAttribute(GSXML.COLLECTION_ATT);

		Element importedContent = (Element) docXML.importNode(newContent, true);
		//If the current section does not have content then just add it, otherwise replace the existing one
		if (existingContent == null)
		{
			section.appendChild(importedContent);
		}
		else
		{
			Node contentParent = existingContent.getParentNode();

			//Replace the old node in the document tree
			contentParent.insertBefore(importedContent, existingContent);
			contentParent.removeChild(existingContent);
		}

		//Write the new change back into the file
		if (!writeXMLFile(docXML, oid, collection, userContext))
		{
			_errorStatus = ERROR_COULD_NOT_WRITE_TO_DOC_XML;
			return;
		}
	}

	/**
	 * Sets the text of a given section using a string.
	 * 
	 * @param oid
	 *            is the identifier of the section to set the text of.
	 * @param collection
	 *            is the collection the document resides in.
	 * @param newContent
	 *            is the new text for the section.
	 */
	public void documentXMLSetText(String oid, String collection, String newContent, UserContext userContext)
	{
		if ((_errorStatus = checkOIDandCollection(oid, collection, userContext)) != NO_ERROR)
		{
			return;
		}

		Document docXML = getDocXML(oid, collection, userContext);
		if (docXML == null)
		{
			_errorStatus = ERROR_COULD_NOT_RETRIEVE_DOC_XML;
			return;
		}

		Element section = null;
		if (!oid.contains("."))
		{
			section = getTopLevelSectionElement(docXML);
		}
		else
		{
			section = getSectionBySectionNumber(docXML, getSectionFromOID(oid));
		}

		if (section == null)
		{
			_errorStatus = ERROR_COULD_NOT_RETRIEVE_SECTION;
			return;
		}

		Element existingContent = (Element) GSXML.getChildByTagName(section, GSXML.DOCXML_CONTENT_ELEM);

		//If the current section does not have content then just add it, otherwise replace the existing one
		if (existingContent == null)
		{
			Element newContentElem = docXML.createElement(GSXML.DOCXML_CONTENT_ELEM);
			Node textNode = docXML.createTextNode(newContent);
			newContentElem.appendChild(textNode);
		}
		else
		{
			Node textNode = existingContent.getFirstChild();
			if (textNode != null)
			{
				textNode.setNodeValue(newContent);
			}
			else
			{
				existingContent.appendChild(docXML.createTextNode(newContent));
			}
		}

		//Write the new change back into the file
		if (!writeXMLFile(docXML, oid, collection, userContext))
		{
			_errorStatus = ERROR_COULD_NOT_WRITE_TO_DOC_XML;
			return;
		}
	}

	/**
	 * Can be used to get the file path of the doc.xml file containing the given
	 * OID.
	 * 
	 * @param oid
	 *            is the identifier of the document/section to get the doc.xml
	 *            of.
	 * @param collection
	 *            is the collection the document resides in.
	 * @return the file path to the doc.xml file.
	 */
	public String archiveGetDocumentFilePath(String oid, String collection, UserContext userContext)
	{
		_errorStatus = NO_ERROR;

		if (oid.contains("."))
		{
			oid = oid.substring(0, oid.indexOf("."));
		}

		String assocFilePath = getDocFilePathFromDatabase(oid, collection, userContext);
		if (assocFilePath == null)
		{
			_errorStatus = ERROR_DATA_NOT_FOUND_IN_DATABASE;
			return null;
		}

		if (File.separator.equals("\\"))
		{
			assocFilePath = assocFilePath.replace("/", "\\");
		}

		String docFilePath = _siteHome + File.separatorChar + "collect" + File.separatorChar + collection + File.separatorChar + "archives" + File.separatorChar + assocFilePath;
		return docFilePath;
	}

	/**
	 * Can be used to find the document that a specific source file is used in.
	 * 
	 * @param srcFile
	 *            is the name of the source file.
	 * @param collection
	 *            is the collection the source file resides in.
	 * @return the OID of the document that the source file is used in.
	 */
	public String archiveGetSourceFileOID(String srcFile, String collection, UserContext userContext)
	{
		_errorStatus = NO_ERROR;
		SimpleCollectionDatabase coll_db = openDatabase(collection, ARCHIVEINFSRC, SimpleCollectionDatabase.READ, userContext);
		if (coll_db == null)
		{
			_errorStatus = ERROR_COULD_NOT_OPEN_DATABASE;
			return null;
		}

		DBInfo info = coll_db.getInfo(srcFile);
		if (info == null)
		{
			_errorStatus = ERROR_DATA_NOT_FOUND_IN_DATABASE;
			coll_db.closeDatabase();
			return null;
		}

		String oid = info.getInfo("oid");

		coll_db.closeDatabase();
		return oid;
	}

	/**
	 * Checks to see if a document or section at a given OID exists.
	 * 
	 * @param oid
	 *            is the identifier of the document/section to check.
	 * @param collection
	 *            is the collection to search in.
	 * @return true if the document/section exists, false otherwise.
	 */
	public boolean archiveCheckDocumentOrSectionExists(String oid, String collection, UserContext userContext)
	{
		_errorStatus = NO_ERROR;
		SimpleCollectionDatabase coll_db = openDatabase(collection, ARCHIVEINFDOC, SimpleCollectionDatabase.READ, userContext);
		if (coll_db == null)
		{
			_errorStatus = ERROR_COULD_NOT_OPEN_DATABASE;
			return false;
		}

		boolean section = false;
		if (oid.contains("."))
		{
			section = true;
		}

		DBInfo info = null;
		if (section)
		{
			info = coll_db.getInfo(oid.substring(0, oid.indexOf(".")));
		}
		else
		{
			info = coll_db.getInfo(oid);
		}
		boolean exists = (info != null);

		coll_db.closeDatabase();
		if (section && exists)
		{
			Document docXML = getDocXML(oid, collection, userContext);
			if (getSectionBySectionNumber(docXML, getSectionFromOID(oid)) == null)
			{
				return false;
			}
			return true;
		}

		return exists;
	}

	/**
	 * Can be used to write a series of entries to the document database.
	 * 
	 * @param oid
	 *            is the key that the entries will be written to.
	 * @param collection
	 *            is the collection whose database will be written to.
	 * @param infoList
	 *            is the list of entries to write.
	 */
	public void archiveWriteEntryToDatabase(String oid, String collection, HashMap<String, ArrayList<String>> infoList, UserContext userContext)
	{
		_errorStatus = NO_ERROR;
		if (oid == null || oid.equals(""))
		{
			_errorStatus = ERROR_OID_NOT_SPECIFIED;
			return;
		}
		else if (collection == null || collection.equals(""))
		{
			_errorStatus = ERROR_COLLECTION_NOT_SPECIFIED;
			return;
		}

		DBInfo info = new DBInfo();

		for (String s : infoList.keySet())
		{
			for (String v : infoList.get(s))
			{
				info.addInfo(s, v);
			}
		}

		SimpleCollectionDatabase coll_db = openDatabase(collection, ARCHIVEINFDOC, SimpleCollectionDatabase.WRITE, userContext);
		if (coll_db == null)
		{
			_errorStatus = ERROR_COULD_NOT_OPEN_DATABASE;
			return;
		}

		coll_db.setInfo(oid, info);
		coll_db.closeDatabase();
	}

	/**
	 * Can be used to remove an entry from the document database.
	 * 
	 * @param oid
	 *            is the key of the entry to erase.
	 * @param collection
	 *            is the collection whose database will have the entry removed.
	 */
	public void archiveRemoveEntryFromDatabase(String oid, String collection, UserContext userContext)
	{
		_errorStatus = NO_ERROR;
		if (oid == null || oid.equals(""))
		{
			_errorStatus = ERROR_OID_NOT_SPECIFIED;
			return;
		}
		else if (collection == null || collection.equals(""))
		{
			_errorStatus = ERROR_COLLECTION_NOT_SPECIFIED;
			return;
		}

		SimpleCollectionDatabase coll_db = openDatabase(collection, ARCHIVEINFDOC, SimpleCollectionDatabase.WRITE, userContext);
		if (coll_db == null)
		{
			_errorStatus = ERROR_COULD_NOT_OPEN_DATABASE;
			return;
		}
		coll_db.deleteKey(oid);
		coll_db.closeDatabase();
	}

	/**
	 * Gets the list of associated files for a given document.
	 * 
	 * @param oid
	 *            is the identifier that will be used to search for associated
	 *            documents.
	 * @param collection
	 *            is the collection whose database will be searched.
	 * @return the list of associated files.
	 */
	public ArrayList<String> archiveGetAssociatedImportFiles(String oid, String collection, UserContext userContext)
	{
		_errorStatus = NO_ERROR;
		if (oid == null || oid.equals(""))
		{
			_errorStatus = ERROR_OID_NOT_SPECIFIED;
			return null;
		}
		else if (collection == null || collection.equals(""))
		{
			_errorStatus = ERROR_COLLECTION_NOT_SPECIFIED;
			return null;
		}

		SimpleCollectionDatabase coll_db = openDatabase(collection, ARCHIVEINFDOC, SimpleCollectionDatabase.READ, userContext);
		if (coll_db == null)
		{
			_errorStatus = ERROR_COULD_NOT_OPEN_DATABASE;
			return null;
		}

		DBInfo info = coll_db.getInfo(oid);
		if (info == null)
		{
			_errorStatus = ERROR_DATA_NOT_FOUND_IN_DATABASE;
			coll_db.closeDatabase();
			return null;
		}

		String srcFile = info.getInfo("src-file");
		Vector data = info.getMultiInfo("assoc-file");

		ArrayList<String> assocFiles = new ArrayList<String>();
		assocFiles.add(srcFile);
		for (String d : (Vector<String>) data)
		{
			assocFiles.add(d);
		}

		coll_db.closeDatabase();
		return assocFiles;
	}

	/********************
	 * Helper functions *
	 *******************/

	public boolean checkError(Element elem, String methodName)
	{
		if (_errorMessageMap.get(_errorStatus) != null)
		{
			GSXML.addError(elem.getOwnerDocument(), elem, methodName + ": " + _errorMessageMap.get(_errorStatus), GSXML.ERROR_TYPE_SYNTAX);
			return true;
		}

		return false;
	}

	public String getSectionFromOID(String oid)
	{
		if (!oid.contains("."))
		{
			return null;
		}
		return oid.substring(oid.indexOf(".") + 1);
	}

	public Element createElementWithValue(Document doc, String nodeName, String name, String value)
	{
		Element metadataElem = doc.createElement(nodeName);
		metadataElem.setAttribute(GSXML.NAME_ATT, name);
		Node textNode = doc.createTextNode(value);
		metadataElem.appendChild(textNode);
		return metadataElem;
	}

	public int getOperation(String to, String from)
	{
		int op;
		if (!to.contains(".") && !from.contains("."))
		{
			op = OPERATION_TYPE_DOC_TO_DOC;
		}
		else if (!to.contains(".") && from.contains("."))
		{
			op = OPERATION_TYPE_DOC_TO_SEC;
		}
		else if (to.contains(".") && !from.contains("."))
		{
			op = OPERATION_TYPE_SEC_TO_DOC;
		}
		else
		{
			op = OPERATION_TYPE_SEC_TO_SEC;
		}
		return op;
	}

	public int[] oidToSectionNumberArray(String oid) throws Exception
	{
		String[] strLevels = oid.split("\\.");
		int[] intLevels = new int[strLevels.length - 1];

		for (int i = 1; i < strLevels.length; i++) //Start at 1 to avoid the document identifier part of the OID
		{
			intLevels[i - 1] = Integer.parseInt(strLevels[i]);
		}

		return intLevels;
	}

	public String getDocFilePathFromDatabase(String oid, String collection, UserContext userContext)
	{
		SimpleCollectionDatabase coll_db = openDatabase(collection, ARCHIVEINFDOC, SimpleCollectionDatabase.WRITE, userContext);
		if (coll_db == null)
		{
			return null;
		}
		DBInfo info = coll_db.getInfo(oid);
		if (info == null)
		{
			return null;
		}

		String docFile = info.getInfo("doc-file");
		coll_db.closeDatabase();
		return docFile;
	}

	public boolean deleteDirectory(File current)
	{
		try
		{
			if (current == null || !current.exists())
			{
				return false;
			}

			if (!current.isDirectory())
			{
				current.delete();
				return true;
			}

			for (File f : current.listFiles())
			{
				if (f.isDirectory())
				{
					deleteDirectory(f);
				}
				else
				{
					f.delete();
				}
			}
			current.delete();
		}
		catch (Exception ex)
		{
			return false;
		}

		return true;
	}

	public int checkOIDandCollection(String oid, String collection, UserContext userContext)
	{
		if (oid == null || oid.equals(""))
		{
			return ERROR_OID_NOT_SPECIFIED;
		}

		if (collection == null || collection.equals(""))
		{
			return ERROR_COLLECTION_NOT_SPECIFIED;
		}

		if (!archiveCheckDocumentOrSectionExists(oid, collection, userContext))
		{
			return ERROR_SOURCE_DOCUMENT_OR_SECTION_DOES_NOT_EXIST;
		}
		return NO_ERROR;
	}

	public boolean copyDirectory(File src, File dest)
	{
		if (src.isDirectory())
		{
			//If the destination directory does not exist then create it
			if (!dest.exists())
			{
				dest.mkdir();
			}

			//Get all the files in the directory
			String files[] = src.list();
			for (String file : files)
			{
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);

				if (!copyDirectory(srcFile, destFile))
				{
					return false;
				}
			}
		}
		else
		{
			try
			{
				FileChannel in = new FileInputStream(src).getChannel();
				FileChannel out = new FileOutputStream(dest).getChannel();

				in.transferTo(0, in.size(), out);

				in.close();
				out.close();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public Element getTopLevelSectionElement(Document docXML)
	{
		return (Element) GSXML.getChildByTagName(docXML.getDocumentElement(), GSXML.DOCXML_SECTION_ELEM);
	}

	public boolean writeXMLFile(Document doc, String oid, String collection, UserContext userContext)
	{
		try
		{
			DOMSource source = new DOMSource(doc);

			String test = archiveGetDocumentFilePath(oid, collection, userContext);
			File xmlFile = new File(test);
			Result result = new StreamResult(xmlFile);

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(source, result);
		}
		catch (Exception ex)
		{
			return false;
		}
		return true;
	}

	public Document getDocXML(String oid, String collection, UserContext userContext)
	{
		if (oid.contains("."))
		{
			oid = oid.substring(0, oid.indexOf("."));
		}

		Document docXML = null;
		if ((docXML = _docCache.get(oid + "__" + collection)) == null)
		{
			String filePath = archiveGetDocumentFilePath(oid, collection, userContext);
			File docFile = new File(filePath);

			if (!docFile.exists())
			{
				return null;
			}

			try
			{
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				docXML = db.parse(docFile);

				_docCache.put(oid + "__" + collection, docXML);
			}
			catch (Exception ex)
			{
				return null;
			}
		}
		return docXML;
	}

	public ArrayList<Element> getMetadataElementsFromSection(Document docXML, String oid, String metadataName)
	{
		if (oid.contains("."))
		{
			Element section = getSectionBySectionNumber(docXML, getSectionFromOID(oid));
			return getMetadataElementsFromSection(section, metadataName);
		}
		else
		{
			return getMetadataElementsFromSection(getTopLevelSectionElement(docXML), metadataName);
		}
	}

	public ArrayList<Element> getMetadataElementsFromSection(Element section, String metadataName)
	{
		Element description = (Element) GSXML.getChildByTagName(section, GSXML.DOCXML_DESCRIPTION_ELEM);
		if (description == null)
		{
			return null;
		}

		ArrayList<Element> elemList = new ArrayList<Element>();
		NodeList metadataNodes = GSXML.getChildrenByTagName(description, GSXML.DOCXML_METADATA_ELEM);
		for (int j = 0; j < metadataNodes.getLength(); j++)
		{
			//If this is a metadata element with the requested name then we have found what we are looking for
			if (((Element) metadataNodes.item(j)).getAttribute(GSXML.NAME_ATT).equals(metadataName))
			{
				elemList.add((Element) metadataNodes.item(j));
			}
		}

		return elemList;
	}

	public Element getSectionBySectionNumber(Document docXML, String sectionNum)
	{
		return getSectionBySectionNumber(getTopLevelSectionElement(docXML), sectionNum);
	}

	public Element getSectionBySectionNumber(Element current, String sectionNum)
	{
		if (sectionNum == null || sectionNum.equals(""))
		{
			return current;
		}

		try
		{
			String[] levels = sectionNum.split("\\.");
			int currentSectionNum = Integer.parseInt(levels[0]);

			NodeList sections = GSXML.getChildrenByTagName(current, GSXML.DOCXML_SECTION_ELEM);
			if (levels.length > 1)
			{
				return getSectionBySectionNumber((Element) sections.item(currentSectionNum - 1), sectionNum.substring(sectionNum.indexOf(".") + 1));
			}
			else
			{
				return (Element) sections.item(currentSectionNum - 1);
			}
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	public String getDatabaseTypeFromCollection(String collection, UserContext userContext)
	{
		//Find out what kind of database we have
		Element dbTypeMessage = _mainDoc.createElement(GSXML.MESSAGE_ELEM);
		Element dbTypeRequest = GSXML.createBasicRequest(_mainDoc, GSXML.REQUEST_TYPE_DESCRIBE, collection, userContext);
		dbTypeMessage.appendChild(dbTypeRequest);
		Element dbTypeResponse = (Element) _router.process(dbTypeMessage);

		String path = GSPath.appendLink(GSXML.RESPONSE_ELEM, GSXML.COLLECTION_ELEM);
		Element collectionElem = (Element) GSXML.getNodeByPath(dbTypeResponse, path);

		if (collectionElem != null)
		{
			return collectionElem.getAttribute(GSXML.DB_TYPE_ATT);
		}
		return "gdbm"; //The default collection database type
	}

	public SimpleCollectionDatabase openDatabase(String collection, String dbName, int readWrite, UserContext userContext)
	{
		//Find out what kind of database we have
		String databaseType = getDatabaseTypeFromCollection(collection, userContext);
		String dbExt = DBHelper.getDBExtFromDBType(databaseType);

		SimpleCollectionDatabase coll_db = new SimpleCollectionDatabase(databaseType);
		if (!coll_db.databaseOK())
		{
			System.err.println("Couldn't create the collection database of type " + databaseType);
			return null;
		}

		coll_db.openDatabase(GSFile.collectionArchiveDir(_siteHome, collection) + File.separatorChar + dbName + dbExt, readWrite);

		return coll_db;
	}

	public int operationStringToInt(String operation)
	{
		if (operation.equals("insertBefore"))
		{
			return OPERATION_INSERT_BEFORE;
		}
		else if (operation.equals("insertAfter"))
		{
			return OPERATION_INSERT_AFTER;
		}
		else if (operation.equals("append"))
		{
			return OPERATION_APPEND;
		}
		else
		{
			return OPERATION_REPLACE;
		}
	}

	public int getErrorStatus()
	{
		return _errorStatus;
	}
}