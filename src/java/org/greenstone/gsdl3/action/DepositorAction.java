package org.greenstone.gsdl3.action;

import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.greenstone.gsdl3.util.DerbyWrapper;
import org.greenstone.gsdl3.util.GSConstants;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.GSXSLT;
import org.greenstone.gsdl3.util.UserContext;
import org.greenstone.util.GlobalProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DepositorAction extends Action
{
	//Sub actions
	private final String DE_RETRIEVE_WIZARD = "getWizard";

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
		boolean parseFail = false;
		try
		{
			pageNum = Integer.parseInt(((String) params.get("dePage")));
		}
		catch (Exception ex)
		{
			parseFail = true;
			ex.printStackTrace();
		}

		DerbyWrapper database = new DerbyWrapper();
		database.connectDatabase(GlobalProperties.getGSDL3Home() + File.separator + "sites" + File.separator + this.config_params.get(GSConstants.SITE_NAME) + File.separatorChar + "etc" + File.separatorChar + "usersDB", false);
		if (parseFail)
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

		String subaction = ((Element) request).getAttribute(GSXML.SUBACTION_ATT);
		if (subaction.equals(DE_RETRIEVE_WIZARD))
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

				database.addUserData(currentUsername, "DE___" + collection + "___" + pageNum + "___CACHED_VALUES", saveString.toString());
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
					page.setAttribute("pageNum", "" + pageNum);
					String cachedValues = database.getUserData(currentUsername, "DE___" + collection + "___" + i + "___CACHED_VALUES");
					page.appendChild(this.doc.createTextNode(cachedValues));
					cachedValueElement.appendChild(page);
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