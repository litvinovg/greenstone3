package org.greenstone.gsdl3.core;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import org.greenstone.util.GlobalProperties;
import org.greenstone.gsdl3.util.*;
import org.w3c.dom.*;

public class Page {

	private Element page ;
	private Element pageRequest ;
	private Element pageResponse ;

	private Document siteUi ;
	private Document collectUi ;
	private Document siteMetadata ;
	private Document collectMetadata ;
	
	private Receptionist receptionist ;
	
	public Page(Element page, Receptionist receptionist) {
		try {
		this.page = page ;
		this.receptionist = receptionist ;
		
		pageRequest = (Element)GSXML.getChildByTagName(page, GSXML.PAGE_REQUEST_ELEM);
		pageResponse = (Element)GSXML.getChildByTagName(page, GSXML.PAGE_RESPONSE_ELEM);
		
		String siteHome = GSFile.siteHome(GlobalProperties.getGSDL3Home(), getSite()) ;
		String collection = getCollection() ;
				
		File siteUiFile = new File(siteHome + File.separatorChar + "ui" + File.separatorChar + "ui.xml") ;	
		//System.out.println("siteUi: " + siteUiFile.getAbsolutePath()) ;
		siteUi = receptionist.converter.getDOM(siteUiFile, "utf-8");
		if (siteUi == null) {
			System.out.println(" could not parse site level ui file: " + siteUiFile.getPath());
		}
		
		File siteMetadataFile = new File(siteHome + File.separatorChar + "metadata.xml") ;
		//System.out.println("siteMetadata: " + siteMetadataFile.getAbsolutePath()) ;
		siteMetadata = receptionist.converter.getDOM(siteMetadataFile, "utf-8");
		if (siteMetadata == null) {
			System.out.println(" could not parse site level metadata file: " + siteMetadataFile.getPath());
		}
		
		if (!collection.equals("")) {
			File collectUiFile = new File(GSFile.collectionBaseDir(siteHome, collection) + File.separatorChar + "ui" + File.separatorChar + "ui.xml") ;
			collectUi = receptionist.converter.getDOM(collectUiFile, "utf-8");
			if (collectUi == null) {
				System.out.println(" could not parse collect level ui file: " + collectUiFile.getPath());
			}
			
			File collectMetadataFile = new File(GSFile.collectionBaseDir(siteHome, collection) + File.separatorChar + "metadata.xml") ;
			collectMetadata = receptionist.converter.getDOM(collectMetadataFile, "utf-8");
			if (collectMetadata == null) {
				System.out.println(" could not parse collect level metadata file: " + collectMetadataFile.getPath());
			}
		}
		} catch (Exception e) {
			e.printStackTrace() ;
		}
	}
	
	public Element getPage() {
		return page ;
	}
	
	public Element getPageRequest() {
		return pageRequest ;
	}
	
	public Element getPageResponse() {
		return pageResponse ;
	}
	
	public String getAction() {
		return pageRequest.getAttribute("action") ;
	}
	
	public String getSubaction() {
		return pageRequest.getAttribute("subaction") ;
	}
	
	public String getSiteHome() {
		return GSFile.siteHome(GlobalProperties.getGSDL3Home(), getSite()) ;
	}
	
	public String getCollectionHome() {
		String collection = this.getCollection() ;
		if (!collection.equals(""))
			return GSFile.collectionBaseDir(getSiteHome(), collection) ;
		else 
			return null ;
	}
	
	public Document getSiteUi() {
		return siteUi ;
	}
	
	public Document getCollectUi() {
		return collectUi ;
	}
	
	public Document getSiteMetadata() {
		return siteMetadata ;
	}
	
	public Document getCollectMetadata() {
		return collectMetadata ;
	}	
	
	public String getLanguage() {
		String lang = pageRequest.getAttribute(GSXML.LANG_ATT);
		return lang ;
	}
	
	public String getSite() {
		return (String) receptionist.config_params.get(GSConstants.SITE_NAME) ;
	}
	
	public String getCollection() {
		String collection = "" ;
		Element request = (Element)GSXML.getChildByTagName(page, GSXML.PAGE_REQUEST_ELEM);
		
		Element cgi_param_list = (Element)GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
		if (cgi_param_list != null) {
			HashMap<String, Serializable> params = GSXML.extractParams(cgi_param_list, false);
			collection = (String)params.get(GSParams.COLLECTION);
			if (collection == null) collection = "";
		}	
		return collection ;
	}
}
