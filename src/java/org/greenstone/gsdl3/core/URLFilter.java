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

public class URLFilter implements Filter
{
	private FilterConfig _filterConfig = null;
	private static Logger _logger = Logger.getLogger(org.greenstone.gsdl3.core.URLFilter.class.getName());
	
	//Restricted URLs
	protected static final String SITECONFIG_URL = "sites/[^/]+/siteConfig.xml";

	protected static final ArrayList<String> _restrictedURLs;
	static
	{
		ArrayList<String> restrictedURLs = new ArrayList<String>();
		restrictedURLs.add(SITECONFIG_URL);
		_restrictedURLs = restrictedURLs;
	}

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
			
			if(isURLRestricted(url))
			{
				response.getWriter().println("Access to this page is forbidden.");
				return;
			}
			
			if (url.contains("/index/assoc/"))
			{
				String dir = null;
				int dirStart = url.indexOf("/index/assoc/") + "/index/assoc/".length();
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
				int colStart = url.indexOf("/collect/") + "/collect/".length();
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
				Element metaRequest = GSXML.createBasicRequest(gsDoc, GSXML.REQUEST_TYPE_PROCESS, collection + "/DocumentMetadataRetrieve", new UserContext());
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
				//GSXML.printXMLNode(metaResponse, true);
			}
			else
			{
				String[] segments = url.split("/");
				for (int i = 0; i < segments.length; i++)
				{
					String[] additionalParameters = null;
					String[] defaultParamValues = null;

					//COLLECTION
					if (segments[i].equals("collection") && (i + 1) < segments.length)
					{
						gRequest.setParameter(GSParams.COLLECTION, segments[i + 1]);
					}
					//DOCUMENT
					else if (segments[i].equals("document") && (i + 1) < segments.length)
					{
						gRequest.setParameter(GSParams.DOCUMENT, segments[i + 1]);

						additionalParameters = new String[] { GSParams.ACTION, GSParams.DOCUMENT_TYPE, GSParams.EXPAND_DOCUMENT };
						defaultParamValues = new String[] { "d", "hierarchy", "1" };
					}
					//PAGE
					else if (segments[i].equals("page") && (i + 1) < segments.length)
					{
						gRequest.setParameter(GSParams.SUBACTION, segments[i + 1]);

						additionalParameters = new String[] { GSParams.ACTION };
						defaultParamValues = new String[] { "p" };
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
							if (!segments[i + j].matches("^\\d+$"))
							{
								break;
							}

							if (j > 1)
							{
								cl += ".";
							}

							cl += segments[i + j];
						}

						gRequest.setParameter("cl", "CL" + cl);

						additionalParameters = new String[] { GSParams.ACTION, GSParams.REQUEST_TYPE, GSParams.SERVICE };
						defaultParamValues = new String[] { "b", "s", "ClassifierBrowse" };
					}
					//QUERY
					else if (segments[i].equals("query"))
					{
						additionalParameters = new String[] { GSParams.ACTION, GSParams.SUBACTION, GSParams.REQUEST_TYPE };
						defaultParamValues = new String[] { "q", "", "rd" };
					}
					//SERVICE
					else if (segments[i].equals("service") && (i + 1) < segments.length)
					{
						String serviceName = segments[i + 1];
						gRequest.setParameter(GSParams.SERVICE, serviceName);

						if (serviceName.equals("TextQuery") || serviceName.equals("RawQuery"))
						{
							additionalParameters = new String[] { "s1.maxDocs", "s1.hitsPerPage", "s1.level", "s1.sortBy", "s1.index", "s1.startPage" };
							defaultParamValues = new String[] { "100", "20", "Sec", "rank", "ZZ", "1" };

							if ((i + 2) < segments.length)
							{
								gRequest.setParameter("s1.query", segments[i + 2]);
							}
						}
						else if (serviceName.equals("FieldQuery"))
						{
							additionalParameters = new String[] { "s1.maxDocs", "s1.hitsPerPage", "s1.level", "s1.sortBy", "s1.fqf", "s1.startPage" };
							defaultParamValues = new String[] { "100", "20", "Sec", "rank", "ZZ", "1" };

							if ((i + 2) < segments.length)
							{
								gRequest.setParameter("s1.fqv", segments[i + 2]);
							}
						}
						else if (serviceName.equals("AdvancedFieldQuery"))
						{
							additionalParameters = new String[] { "s1.maxDocs", "s1.hitsPerPage", "s1.level", "s1.sortBy", "s1.fqf", "s1.fqk", "s1.startPage" };
							defaultParamValues = new String[] { "100", "20", "Sec", "rank", "ZZ", "0", "1" };

							if ((i + 2) < segments.length)
							{
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
		for(String restrictedURL : _restrictedURLs)
		{
			if(url.matches(".*" + restrictedURL + ".*"))
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
			if (_newParams.containsKey(paramName))
			{
				return _newParams.get(paramName)[0];
			}
			else
			{
				return super.getParameter(paramName);
			}
		}

		public String[] getParameterValues(String paramName)
		{
			if (_newParams.containsKey(paramName))
			{
				return _newParams.get(paramName);
			}
			else
			{
				return super.getParameterValues(paramName);
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