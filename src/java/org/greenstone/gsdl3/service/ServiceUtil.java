package org.greenstone.gsdl3.service;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.XMLConverter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ServiceUtil extends ServiceRack
{
	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.ServiceUtil.class.getName());
	
	/**********************************************************
	 * The list of services the utility service rack supports *
	 *********************************************************/
	protected static final String GET_ALL_IMAGES_IN_COLLECTION = "GetAllImagesInCollection";
	/*********************************************************/
	
	String[] services = { GET_ALL_IMAGES_IN_COLLECTION };
	
	public boolean configure(Element info, Element extra_info)
	{
		if (!super.configure(info, extra_info))
		{
			return false;
		}

		logger.info("Configuring ServiceUtil...");
		this.config_info = info;

		for (int i = 0; i < services.length; i++)
		{
			Element service = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
			service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
			service.setAttribute(GSXML.NAME_ATT, services[i]);
			this.short_service_info.appendChild(service);
		}

		return true;
	}

	protected Element getServiceDescription(Document doc, String service_id, String lang, String subset)
	{
		for (int i = 0; i < services.length; i++)
		{
			if (service_id.equals(services[i]))
			{
				Element service_elem = doc.createElement(GSXML.SERVICE_ELEM);
				service_elem.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
				service_elem.setAttribute(GSXML.NAME_ATT, services[i]);
				return service_elem;
			}
		}

		return null;
	}
	
	protected Element processGetAllImagesInCollection(Element request)
	{
	  Document result_doc = XMLConverter.newDOM();
		Element result = GSXML.createBasicResponse(result_doc, GET_ALL_IMAGES_IN_COLLECTION);

		if (request == null)
		{
			GSXML.addError(result, GET_ALL_IMAGES_IN_COLLECTION + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}
		
		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);
		
		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);

		if (param_list == null) {
			GSXML.addError(result, GET_ALL_IMAGES_IN_COLLECTION + ": No param list specified", GSXML.ERROR_TYPE_SYNTAX);
			return result;  // Return the empty result
		}

		HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);
		
		String regex = (String)params.get("extregex");
		if(regex == null)
		{
			GSXML.addError(result, GET_ALL_IMAGES_IN_COLLECTION + ": No file name extensions specified", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}
		regex = ".*" + regex + ".*";
		
		String collection = (String)params.get("c");
		if(collection == null)
		{
			GSXML.addError(result, GET_ALL_IMAGES_IN_COLLECTION + ": No collection specified", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}
		
		File indexDir = new File(this.site_home + File.separator + "collect" + File.separator + collection + File.separator + "index" + File.separator + "assoc");
		ArrayList<String> images = new ArrayList<String>();
		getImagesRecursive(indexDir, regex, images);
		
		Element imageListElem = result_doc.createElement("imageList");
		result.appendChild(imageListElem);
		for(String i : images)
		{
			Element imageElem = result_doc.createElement("image");
			imageElem.appendChild(result_doc.createTextNode(i));
			imageListElem.appendChild(imageElem);
		}
		return result;
	}
	
	/********************
	 * Helper functions *
	 *******************/
	
	protected void getImagesRecursive(File current, String regex, ArrayList<String> images)
	{
		if(current.isDirectory())
		{
			File[] files = current.listFiles();
			for(File f : files)
			{
				getImagesRecursive(f, regex, images);
			}
		}
		else
		{
			String path = current.getAbsolutePath();
			String filename = path.substring(path.lastIndexOf(File.separator) + File.separator.length());

			if(filename.matches(regex))
			{
				images.add(path);
			}
			else
			{
		
			}
		}
	}
}