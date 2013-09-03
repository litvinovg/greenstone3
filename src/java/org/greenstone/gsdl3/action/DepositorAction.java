package org.greenstone.gsdl3.action;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.greenstone.gsdl3.util.DerbyWrapper;
import org.greenstone.gsdl3.util.GSConstants;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.GSXSLT;
import org.greenstone.gsdl3.util.UserContext;
import org.greenstone.util.GlobalProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DepositorAction extends Action
{
	//Sub actions
	private final String DE_RETRIEVE_WIZARD = "getwizard";
	private final String DE_DEPOSIT_FILE = "depositfile";
	private final String DE_CLEAR_CACHE = "clearcache";
	private final String DE_CLEAR_DATABASE = "cleardatabase";

	public Node process(Node message)
	{
		Element request = (Element) GSXML.getChildByTagName(message, GSXML.REQUEST_ELEM);

		UserContext uc = new UserContext((Element) request);
		String currentUsername = uc.getUsername();

		Element responseMessage = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element response = GSXML.createBasicResponse(this.doc, this.getClass().getSimpleName());
		responseMessage.appendChild(response);

		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);

		String collection = (String) params.get("c");

		int pageNum = -1;
		boolean pageNumParseFail = false;
		try
		{
			pageNum = Integer.parseInt(((String) params.get("dePage")));
		}
		catch (Exception ex)
		{
			pageNumParseFail = true;
		}

		int prevPageNum = -1;
		boolean prevPageNumFail = false;
		try
		{
			prevPageNum = Integer.parseInt((String) params.get("currentPage"));
		}
		catch (Exception ex)
		{
			prevPageNumFail = true;
		}

		DerbyWrapper database = new DerbyWrapper(GlobalProperties.getGSDL3Home() + File.separatorChar + "etc" + File.separatorChar + "usersDB");
		if (pageNumParseFail)
		{
			try
			{
				pageNum = Integer.parseInt(database.getUserData(currentUsername, "DE___" + collection + "___CACHED_PAGE"));
			}
			catch (Exception ex)
			{
				pageNum = 1;
			}
		}

		int highestVisitedPage = -1;
		String result = "";
		int counter = 1;
		while (result != null)
		{
			result = database.getUserData(currentUsername, "DE___" + collection + "___" + counter + "___VISITED_PAGE");
			if (result != null)
			{
				counter++;
			}
		}
		highestVisitedPage = counter - 1;
		if (highestVisitedPage == 0)
		{
			highestVisitedPage = 1;
		}

		if (pageNum > highestVisitedPage + 1)
		{
			pageNum = highestVisitedPage + 1;
		}

		database.addUserData(currentUsername, "DE___" + collection + "___" + pageNum + "___VISITED_PAGE", "VISITED");

		String subaction = ((Element) request).getAttribute(GSXML.SUBACTION_ATT);
		if (subaction.toLowerCase().equals(DE_RETRIEVE_WIZARD))
		{
			//Save given metadata
			StringBuilder saveString = new StringBuilder("[");
			Iterator<String> paramIter = params.keySet().iterator();
			while (paramIter.hasNext())
			{
				String paramName = paramIter.next();
				if (paramName.startsWith("md___"))
				{
					Object paramValue = params.get(paramName);

					if (paramValue instanceof String)
					{
						saveString.append("{name:\"" + paramName + "\", value:\"" + (String) paramValue + "\"},");
					}
					else if (paramValue instanceof HashMap)
					{
						HashMap<String, String> subMap = (HashMap<String, String>) paramValue;
						Iterator<String> subKeyIter = subMap.keySet().iterator();
						while (subKeyIter.hasNext())
						{
							String subName = subKeyIter.next();
							saveString.append("{name:\"" + paramName + "." + subName + "\", value:\"" + subMap.get(subName) + "\"},");
						}
					}
				}
			}
			if (saveString.length() > 2)
			{
				saveString.deleteCharAt(saveString.length() - 1);
				saveString.append("]");

				if (!prevPageNumFail)
				{
					database.addUserData(currentUsername, "DE___" + collection + "___" + prevPageNum + "___CACHED_VALUES", saveString.toString());
				}
			}

			//Construct the xsl
			Document compiledDepositorFile = null;
			try
			{
				compiledDepositorFile = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			Document depositorBaseFile = GSXSLT.mergedXSLTDocumentCascade("depositor/depositor.xsl", (String) this.config_params.get(GSConstants.SITE_NAME), collection, (String) this.config_params.get(GSConstants.INTERFACE_NAME), (ArrayList<String>) this.config_params.get(GSConstants.BASE_INTERFACES), false);

			Element numOfPagesElement = GSXML.getNamedElement(depositorBaseFile.getDocumentElement(), "xsl:variable", "name", "numOfPages");
			int numberOfPages = Integer.parseInt(numOfPagesElement.getTextContent());

			compiledDepositorFile.appendChild(compiledDepositorFile.importNode(depositorBaseFile.getDocumentElement(), true));

			ArrayList<Document> pageDocs = new ArrayList<Document>();
			ArrayList<String> pageNames = new ArrayList<String>();
			for (int i = 1; i <= numberOfPages; i++)
			{
				Document page = GSXSLT.mergedXSLTDocumentCascade("depositor/de_page" + i + ".xsl", (String) this.config_params.get(GSConstants.SITE_NAME), collection, (String) this.config_params.get(GSConstants.INTERFACE_NAME), (ArrayList<String>) this.config_params.get(GSConstants.BASE_INTERFACES), false);
				pageDocs.add(page);

				Element pageTitleElem = (Element) GSXML.getNamedElement(page.getDocumentElement(), "xsl:variable", "name", "title");
				pageNames.add(pageTitleElem.getTextContent());

				Element wizardPageElem = (Element) GSXML.getNamedElement(page.getDocumentElement(), "xsl:template", "name", "wizardPage");
				wizardPageElem.setAttribute("name", "wizardPage" + i);
				compiledDepositorFile.getDocumentElement().appendChild(compiledDepositorFile.importNode(wizardPageElem, true));
			}

			//Create the wizard bar
			Element wizardBarTemplate = GSXML.getNamedElement(compiledDepositorFile.getDocumentElement(), "xsl:template", "name", "wizardBar");
			Element wizardBar = compiledDepositorFile.createElement("ul");
			wizardBar.setAttribute("id", "wizardBar");
			wizardBarTemplate.appendChild(wizardBar);

			for (int i = 0; i < pageNames.size(); i++)
			{
				String pageName = pageNames.get(i);
				Element pageLi = compiledDepositorFile.createElement("li");
				if (pageNum == i + 1)
				{
					pageLi.setAttribute("class", "wizardStepLink ui-state-active ui-corner-all");
				}
				else if (i + 1 > highestVisitedPage + 1 && i + 1 > pageNum + 1)
				{
					pageLi.setAttribute("class", "wizardStepLink ui-state-disabled ui-corner-all");
				}
				else
				{
					pageLi.setAttribute("class", "wizardStepLink ui-state-default ui-corner-all");
				}
				Element link = compiledDepositorFile.createElement("a");
				pageLi.appendChild(link);

				link.setAttribute(GSXML.HREF_ATT, "javascript:;");
				link.setAttribute("page", "" + (i + 1));
				link.appendChild(compiledDepositorFile.createTextNode(pageName));
				wizardBar.appendChild(pageLi);
			}

			//Add a call-template call to the appropriate page in the xsl
			Element mainDePageElem = GSXML.getNamedElement(compiledDepositorFile.getDocumentElement(), "xsl:template", "match", "/page");
			Element wizardContainer = GSXML.getNamedElement(mainDePageElem, "div", "id", "wizardContainer");
			Element formContainer = GSXML.getNamedElement(wizardContainer, "form", "name", "depositorform");
			Element callToPage = compiledDepositorFile.createElement("xsl:call-template");
			callToPage.setAttribute("name", "wizardPage" + pageNum);
			formContainer.appendChild(callToPage);

			Element cachedValueElement = this.doc.createElement("cachedValues");
			response.appendChild(cachedValueElement);
			try
			{
				for (int i = pageNum; i > 0; i--)
				{
					Element page = this.doc.createElement("pageCache");
					page.setAttribute("pageNum", "" + i);
					String cachedValues = database.getUserData(currentUsername, "DE___" + collection + "___" + i + "___CACHED_VALUES");
					if (cachedValues != null)
					{
						page.appendChild(this.doc.createTextNode(cachedValues));
						cachedValueElement.appendChild(page);
					}
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

			try
			{
				Transformer transformer = TransformerFactory.newInstance().newTransformer();

				File newFileDir = new File(GlobalProperties.getGSDL3Home() + File.separator + "sites" + File.separator + this.config_params.get(GSConstants.SITE_NAME) + File.separator + "collect" + File.separator + collection + File.separator + "transform" + File.separator + "depositor");
				newFileDir.mkdirs();

				File newFile = new File(newFileDir, File.separator + "compiledDepositor.xsl");

				//initialize StreamResult with File object to save to file
				StreamResult sresult = new StreamResult(new FileWriter(newFile));
				DOMSource source = new DOMSource(compiledDepositorFile);
				transformer.transform(source, sresult);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			database.closeDatabase();
		}
		else if (subaction.toLowerCase().equals(DE_DEPOSIT_FILE))
		{
			String fileToAdd = (String) params.get("fileToAdd");
			File tempFile = new File(GlobalProperties.getGSDL3Home() + File.separator + "tmp" + File.separator + fileToAdd);
			if (tempFile.exists())
			{
				File newFileLocationDir = new File(GlobalProperties.getGSDL3Home() + File.separator + "sites" + File.separator + this.config_params.get(GSConstants.SITE_NAME) + File.separator + "collect" + File.separator + collection + File.separator + "import" + File.separator + fileToAdd);
				if (!newFileLocationDir.exists())
				{
					newFileLocationDir.mkdir();
				}
				File newFileLocation = new File(newFileLocationDir, fileToAdd);

				try
				{
					FileUtils.copyFile(tempFile, newFileLocation);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					GSXML.addError(this.doc, responseMessage, "Failed to copy the deposited file into the collection.");
					return responseMessage;
				}

				HashMap<String, String> metadataMap = new HashMap<String, String>();
				for (int i = pageNum; i > 0; i--)
				{
					String cachedValues = database.getUserData(currentUsername, "DE___" + collection + "___" + i + "___CACHED_VALUES");
					if (cachedValues != null)
					{
						Type type = new TypeToken<List<Map<String, String>>>()
						{
						}.getType();

						Gson gson = new Gson();
						List<Map<String, String>> metadataList = gson.fromJson(cachedValues, type);
						for (Map<String, String> metadata : metadataList)
						{
							metadataMap.put(metadata.get("name"), metadata.get("value"));
						}
					}
				}

				String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><!DOCTYPE DirectoryMetadata SYSTEM \"http://greenstone.org/dtd/DirectoryMetadata/1.0/DirectoryMetadata.dtd\"><DirectoryMetadata><FileSet>";
				xmlString += "<FileName>.*</FileName><Description>";
				for (String key : metadataMap.keySet())
				{
					xmlString += "<Metadata name=\"" + key.substring("MD___".length()) + "\" mode=\"accumulate\">" + metadataMap.get(key) + "</Metadata>";
				}
				xmlString += "</Description></FileSet></DirectoryMetadata>";

				File metadataFile = new File(GlobalProperties.getGSDL3Home() + File.separator + "sites" + File.separator + this.config_params.get(GSConstants.SITE_NAME) + File.separator + "collect" + File.separator + collection + File.separator + "import" + File.separator + fileToAdd + File.separator + "metadata.xml");

				try
				{
					BufferedWriter bw = new BufferedWriter(new FileWriter(metadataFile));
					bw.write(xmlString);
					bw.close();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}

				Element buildMessage = this.doc.createElement(GSXML.MESSAGE_ELEM);
				Element buildRequest = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, "ImportCollection", uc);
				buildMessage.appendChild(buildRequest);

				Element paramListElem = this.doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
				buildRequest.appendChild(paramListElem);

				Element collectionParam = this.doc.createElement(GSXML.PARAM_ELEM);
				paramListElem.appendChild(collectionParam);
				collectionParam.setAttribute(GSXML.NAME_ATT, GSXML.COLLECTION_ATT);
				collectionParam.setAttribute(GSXML.VALUE_ATT, collection);

				Element documentsParam = this.doc.createElement(GSXML.PARAM_ELEM);
				paramListElem.appendChild(documentsParam);
				documentsParam.setAttribute(GSXML.NAME_ATT, "documents");
				documentsParam.setAttribute(GSXML.VALUE_ATT, fileToAdd);

				Element buildResponseMessage = (Element) this.mr.process(buildMessage);

				response.appendChild(this.doc.importNode(buildResponseMessage, true));
			}
		}
		else if (subaction.toLowerCase().equals(DE_CLEAR_CACHE))
		{
			database.clearUserDataWithPrefix(currentUsername, "DE___");
		}
		else if (subaction.toLowerCase().equals(DE_CLEAR_DATABASE))
		{
			database.clearUserData();
			database.clearTrackerData();
		}
		else
		{
			Element depositorPage = this.doc.createElement("depositorPage");
			response.appendChild(depositorPage);

			Element collList = getCollectionsInSite();
			depositorPage.appendChild(this.doc.importNode(collList, true));
		}

		return responseMessage;
	}

	public Element getCollectionsInSite()
	{
		Element message = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_DESCRIBE, "", new UserContext());
		message.appendChild(request);
		Element responseMessage = (Element) this.mr.process(message);

		Element response = (Element) GSXML.getChildByTagName(responseMessage, GSXML.RESPONSE_ELEM);
		Element collectionList = (Element) GSXML.getChildByTagName(response, GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER);

		return collectionList;
	}
}