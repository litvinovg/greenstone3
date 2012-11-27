package org.greenstone.gsdl3.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.log4j.Logger;
import org.greenstone.gsdl3.util.GSParams;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.UserContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class URLFilter implements Filter
{
	private FilterConfig _filterConfig = null;
	private static Logger _logger = Logger.getLogger(org.greenstone.gsdl3.core.URLFilter.class.getName());

	//Restricted URLs
	protected static final String SITECONFIG_URL = "sites/[^/]+/siteConfig.xml";
	protected static final String USERS_DB_URL = "sites/[^/]+/etc/usersDB/.*";
	protected static final ArrayList<String> _restrictedURLs;
	static
	{
		ArrayList<String> restrictedURLs = new ArrayList<String>();
		restrictedURLs.add(SITECONFIG_URL);
		restrictedURLs.add(USERS_DB_URL);
		_restrictedURLs = restrictedURLs;
	}

	//Constants
	protected static final String DOCUMENT_PATH = "document";
	protected static final String COLLECTION_PATH = "collection";
	protected static final String PAGE_PATH = "page";
	protected static final String SYSTEM_PATH = "system";

	protected static final String METADATA_RETRIEVAL_SERVICE = "DocumentMetadataRetrieve";
	protected static final String ASSOCIATED_FILE_PATH = "/index/assoc/";
	protected static final String COLLECTION_FILE_PATH = "/collect/";

	protected static final String SYSTEM_SUBACTION_CONFIGURE = "configure";
	protected static final String SYSTEM_SUBACTION_RECONFIGURE = "reconfigure";
	protected static final String SYSTEM_SUBACTION_ACTIVATE = "activate";
	protected static final String SYSTEM_SUBACTION_DEACTIVATE = "deactivate";

	public void init(FilterConfig filterConfig) throws ServletException
	{
		this._filterConfig = filterConfig;
	}

	public void destroy()
	{
		this._filterConfig = null;
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		if (request instanceof HttpServletRequest)
		{
			HttpServletRequest hRequest = ((HttpServletRequest) request);
			GSHttpServletRequestWrapper gRequest = new GSHttpServletRequestWrapper(hRequest);

			String url = hRequest.getRequestURI().toString();

			if (isURLRestricted(url))
			{
				response.getWriter().println("Access to this page is forbidden.");
				return;
			}

			//If the user is trying to access a collection file we need to run a security check
			if (url.contains(ASSOCIATED_FILE_PATH))
			{
				String dir = null;
				int dirStart = url.indexOf(ASSOCIATED_FILE_PATH) + ASSOCIATED_FILE_PATH.length();
				int dirEnd = -1;
				if (dirStart < url.length() && url.indexOf("/", dirStart) != -1)
				{
					dirEnd = url.indexOf("/", dirStart);
				}
				if (dirEnd != -1)
				{
					dir = url.substring(dirStart, dirEnd);
				}
				if (dir == null)
				{
					return;
				}

				String collection = null;
				int colStart = url.indexOf(COLLECTION_FILE_PATH) + COLLECTION_FILE_PATH.length();
				int colEnd = -1;
				if (colStart < url.length() && url.indexOf("/", colStart) != -1)
				{
					colEnd = url.indexOf("/", colStart);
				}
				if (colEnd != -1)
				{
					collection = url.substring(colStart, colEnd);
				}
				if (collection == null)
				{
					return;
				}

				MessageRouter gsRouter = (MessageRouter) request.getServletContext().getAttribute("GSRouter");
				if (gsRouter == null)
				{
					_logger.error("Receptionist is null, stopping filter");
					return;
				}

				Document gsDoc = (Document) request.getServletContext().getAttribute("GSDocument");
				if (gsDoc == null)
				{
					_logger.error("Document is null, stopping filter");
					return;
				}

				Element metaMessage = gsDoc.createElement(GSXML.MESSAGE_ELEM);
				Element metaRequest = GSXML.createBasicRequest(gsDoc, GSXML.REQUEST_TYPE_PROCESS, collection + "/" + METADATA_RETRIEVAL_SERVICE, new UserContext());
				metaMessage.appendChild(metaRequest);

				Element paramList = gsDoc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
				metaRequest.appendChild(paramList);

				Element param = gsDoc.createElement(GSXML.PARAM_ELEM);
				paramList.appendChild(param);

				param.setAttribute(GSXML.NAME_ATT, "metadata");
				param.setAttribute(GSXML.VALUE_ATT, "contains");

				Element docList = gsDoc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
				metaRequest.appendChild(docList);

				Element doc = gsDoc.createElement(GSXML.DOC_NODE_ELEM);
				docList.appendChild(doc);

				doc.setAttribute(GSXML.NODE_ID_ATT, dir);

				Element metaResponse = (Element) gsRouter.process(metaMessage);

				NodeList metadataList = metaResponse.getElementsByTagName(GSXML.METADATA_ELEM);
				if (metadataList.getLength() == 0)
				{
					_logger.error("Could not find the document related to this url");
				}
				else
				{
					Element metadata = (Element) metadataList.item(0);
					String document = metadata.getTextContent();

					//Get the security info for this collection
					Element securityMessage = gsDoc.createElement(GSXML.MESSAGE_ELEM);
					Element securityRequest = GSXML.createBasicRequest(gsDoc, GSXML.REQUEST_TYPE_SECURITY, collection, new UserContext());
					securityMessage.appendChild(securityRequest);
					if (document != null && !document.equals(""))
					{
						securityRequest.setAttribute(GSXML.NODE_OID, document);
					}

					Element securityResponse = (Element) GSXML.getChildByTagName(gsRouter.process(securityMessage), GSXML.RESPONSE_ELEM);
					ArrayList<String> groups = GSXML.getGroupsFromSecurityResponse(securityResponse);

					if (!groups.contains(""))
					{
						boolean found = false;
						for (String group : groups)
						{
							if (((HttpServletRequest) request).isUserInRole(group))
							{
								found = true;
								break;
							}
						}

						if (!found)
						{
							return;
						}
					}
				}
			}
			else
			{
				//If we have a jsessionid on the end of our URL we want to ignore it
				int index;
				if ((index = url.indexOf(";jsessionid")) != -1)
				{
					url = url.substring(0, index);
				}
				String[] segments = url.split("/");
				for (int i = 0; i < segments.length; i++)
				{
					String[] additionalParameters = null;
					String[] defaultParamValues = null;

					//COLLECTION
					if (segments[i].equals(COLLECTION_PATH) && (i + 1) < segments.length)
					{
						gRequest.setParameter(GSParams.COLLECTION, segments[i + 1]);
					}
					//DOCUMENT
					else if (segments[i].equals(DOCUMENT_PATH) && (i + 1) < segments.length)
					{
						gRequest.setParameter(GSParams.DOCUMENT, segments[i + 1]);

						additionalParameters = new String[] { GSParams.ACTION };
						defaultParamValues = new String[] { "d" };
						//additionalParameters = new String[] { GSParams.ACTION, GSParams.DOCUMENT_TYPE };
						//defaultParamValues = new String[] { "d", "hierarchy" };
					}
					//PAGE
					else if (segments[i].equals(PAGE_PATH) && (i + 1) < segments.length)
					{
						gRequest.setParameter(GSParams.SUBACTION, segments[i + 1]);

						additionalParameters = new String[] { GSParams.ACTION };
						defaultParamValues = new String[] { "p" };
					}
					//SYSTEM
					else if (segments[i].equals(SYSTEM_PATH) && (i + 1) < segments.length)
					{
						String sa = segments[i + 1];
						if (sa.equals(SYSTEM_SUBACTION_CONFIGURE) || sa.equals(SYSTEM_SUBACTION_RECONFIGURE))
						{
							sa = "c";
						}
						else if (sa.equals(SYSTEM_SUBACTION_ACTIVATE))
						{
							sa = "a";
						}
						else if (sa.equals(SYSTEM_SUBACTION_DEACTIVATE))
						{
							sa = "d";
						}

						if (sa.equals("c") && (i + 2) < segments.length)
						{
							gRequest.setParameter(GSParams.SYSTEM_CLUSTER, segments[i + 2]);
						}

						if (sa.equals("a") && (i + 2) < segments.length)
						{
							gRequest.setParameter(GSParams.SYSTEM_MODULE_TYPE, "collection");
							gRequest.setParameter(GSParams.SYSTEM_MODULE_NAME, segments[i + 2]);
						}

						if (sa.equals("d") && (i + 2) < segments.length)
						{
							gRequest.setParameter(GSParams.SYSTEM_CLUSTER, segments[i + 2]);
						}

						gRequest.setParameter(GSParams.SUBACTION, sa);

						additionalParameters = new String[] { GSParams.ACTION };
						defaultParamValues = new String[] { "s" };
					}
					//ADMIN
					else if (segments[i].equals("admin") && (i + 1) < segments.length)
					{
						String pageName = segments[i + 1];

						gRequest.setParameter("s1.authpage", pageName);

						additionalParameters = new String[] { GSParams.ACTION, GSParams.REQUEST_TYPE, GSParams.SUBACTION, GSParams.SERVICE };
						defaultParamValues = new String[] { "g", "r", "authen", "Authentication" };
					}
					//BROWSE
					else if (segments[i].equals("browse") && (i + 1) < segments.length)
					{
						String cl = "";
						for (int j = 1; (i + j) < segments.length; j++)
						{
							String currentSegment = segments[i + j].replace("CL", "").replace("cl", "");
							if (currentSegment.contains("."))
							{
								String[] subsegments = currentSegment.split("\\.");
								for (String subsegment : subsegments)
								{
									subsegment = subsegment.replace("CL", "").replace("cl", "");

									if (cl.length() > 0)
									{
										cl += ".";
									}

									if (subsegment.length() > 0)
									{
										cl += subsegment;
									}
								}
								continue;
							}
							if (!currentSegment.matches("^(CL|cl)?\\d+$"))
							{
								continue;
							}

							if (cl.length() > 0)
							{
								cl += ".";
							}

							cl += currentSegment;
						}

						gRequest.setParameter("cl", "CL" + cl);

						additionalParameters = new String[] { GSParams.ACTION, GSParams.REQUEST_TYPE, GSParams.SERVICE };
						defaultParamValues = new String[] { "b", "s", "ClassifierBrowse" };
					}
					//QUERY
					else if (segments[i].equals("search"))
					{
						String serviceName = "";
						if ((i + 1) < segments.length)
						{
							serviceName = segments[i + 1];
							gRequest.setParameter("s", serviceName);

							additionalParameters = new String[] { GSParams.ACTION, GSParams.SUBACTION, GSParams.REQUEST_TYPE };
							defaultParamValues = new String[] { "q", "", "d" };
						}
						if ((i + 2) < segments.length)
						{
							if (serviceName.equals("TextQuery") || serviceName.equals("RawQuery"))
							{
								additionalParameters = new String[] { GSParams.ACTION, GSParams.SUBACTION, GSParams.REQUEST_TYPE, "s1.maxDocs", "s1.hitsPerPage", "s1.level", "s1.sortBy", "s1.index", "s1.startPage" };
								defaultParamValues = new String[] { "q", "", "rd", "100", "20", "Sec", "rank", "ZZ", "1" };

								gRequest.setParameter("s1.query", segments[i + 2]);
							}
							else if (serviceName.equals("FieldQuery"))
							{
								additionalParameters = new String[] { GSParams.ACTION, GSParams.SUBACTION, GSParams.REQUEST_TYPE, "s1.maxDocs", "s1.hitsPerPage", "s1.level", "s1.sortBy", "s1.fqf", "s1.startPage" };
								defaultParamValues = new String[] { "q", "", "rd", "100", "20", "Sec", "rank", "ZZ", "1" };

								gRequest.setParameter("s1.fqv", segments[i + 2]);
							}
							else if (serviceName.equals("AdvancedFieldQuery"))
							{
								additionalParameters = new String[] { GSParams.ACTION, GSParams.SUBACTION, GSParams.REQUEST_TYPE, "s1.maxDocs", "s1.hitsPerPage", "s1.level", "s1.sortBy", "s1.fqf", "s1.fqk", "s1.startPage" };
								defaultParamValues = new String[] { "q", "", "rd", "100", "20", "Sec", "rank", "ZZ", "0", "1" };

								gRequest.setParameter("s1.fqv", segments[i + 2]);
							}
						}
					}
					if (additionalParameters != null)
					{
						for (int j = 0; j < additionalParameters.length; j++)
						{
							if (gRequest.getParameter(additionalParameters[j]) == null)
							{
								gRequest.setParameter(additionalParameters[j], defaultParamValues[j]);
							}
						}
					}
				}
			}

			chain.doFilter(gRequest, response);
		}
		else
		{
			//Will this ever happen?
			System.err.println("The request was not an HttpServletRequest");
		}
	}

	private boolean isURLRestricted(String url)
	{
		for (String restrictedURL : _restrictedURLs)
		{
			if (url.matches(".*" + restrictedURL + ".*"))
			{
				return true;
			}
		}

		return false;
	}

	private class GSHttpServletRequestWrapper extends HttpServletRequestWrapper
	{
		private HashMap<String, String[]> _newParams = new HashMap<String, String[]>();

		public GSHttpServletRequestWrapper(ServletRequest request)
		{
			super((HttpServletRequest) request);
		}

		public void setParameter(String paramName, String[] paramValues)
		{
			_newParams.put(paramName, paramValues);
		}

		public void setParameter(String paramName, String paramValue)
		{
			_newParams.put(paramName, new String[] { paramValue });
		}

		public String getParameter(String paramName)
		{
			if (super.getParameter(paramName) != null)
			{
				return super.getParameter(paramName);
			}
			else
			{
				if (_newParams.get(paramName) != null && _newParams.get(paramName)[0] != null)
				{
					return _newParams.get(paramName)[0];
				}
				return null;
			}
		}

		public String[] getParameterValues(String paramName)
		{
			if (super.getParameterValues(paramName) != null)
			{
				return super.getParameterValues(paramName);
			}
			else
			{
				return _newParams.get(paramName);
			}
		}

		public Map<String, String[]> getParameterMap()
		{
			HashMap<String, String[]> returnMap = new HashMap<String, String[]>();
			returnMap.putAll(super.getParameterMap());
			returnMap.putAll(_newParams);
			return returnMap;
		}
	}
}