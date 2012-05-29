
function GSAjaxAPI(gwcgi,collect) 
{
    var gwcgi_   = gwcgi;
    var collect_ = collect;


    this.fullDomainURL = function(localURL)
    {
	return window.location.protocol+'//'+window.location.host+localURL;
    }

    this.apiURL = function(apiProg)
    {
        //get the location of the cgi program
        splitpos = gwcgi_.lastIndexOf("/");

        var mdserver;
        if (splitpos >= 0) {
            mdserver = gwcgi.substring(0,(splitpos+1)) + apiProg;
        }
        else {
          mdserver = apiProg;
        }

       return mdserver;
    }

    this.metadataserverURL = function()
    {
        return this.apiURL("metadata-server.pl");
    }

    this.indexserverURL = function()
    {
        return this.apiURL("index-server.pl");
    }

	this.buildserverURL = function()
    {
        return this.apiURL("build-server.pl");
    }
	
    this.explodeserverURL = function()
    {
        return this.apiURL("explode-server.pl");
    }

    this.myspaceserverURL = function()
    {
        return this.apiURL("myspace-server.pl");
    }


    this.urlGetAsync = function(url,callback)
    {
       var xmlHttp;
       try {
         // Firefox, Opera 8.0+, Safari
         xmlHttp=new XMLHttpRequest();
       }
       catch (e) {
         // Internet Explorer
         try {
           xmlHttp=new ActiveXObject("Msxml2.XMLHTTP");
         }
         catch (e) {
           try {
             xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
           }
           catch (e) {
             alert("Your browser does not support AJAX!");
             return false;
           }
         }
       }

	var typeof_callback = typeof(callback);
	if ((typeof_callback == "string") || (typeof_callback == "number") || (typeof_callback == "boolean")) {
	    var locid = callback;

	    xmlHttp.onreadystatechange=function() {
		if(xmlHttp.readyState==4) {
		    if (locelem != null) {
			var locelem = document.getElementById(locid);
			
			locelem.innerHTML = xmlHttp.responseText;
		    }
		}
	    }
	}
	else if (typeof_callback == "function") {
	    xmlHttp.onreadystatechange=function() {
		if(xmlHttp.readyState==4) {
		    callback(xmlHttp);
		}
	    }
	}
	else {
	    alert("Unrecognized type of callback value: " + typeof_callback);
	}

	xmlHttp.open("GET",url,true);
	xmlHttp.send(null);
    }
    
        
    this.urlGetSync = function(url)
    {
	// alert("url = " + url);

       var xmlHttp;
       try {
         // Firefox, Opera 8.0+, Safari
         xmlHttp=new XMLHttpRequest();
       }
       catch (e) {
         // Internet Explorer
         try {
           xmlHttp=new ActiveXObject("Msxml2.XMLHTTP");
         }
         catch (e) {
           try {
             xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
           }
           catch (e) {
             alert("Your browser does not support AJAX!");
             return false;
           }
         }
       }
    
       xmlHttp.open("GET",url,false);
       xmlHttp.send(null);

       // alert("response = '" + xmlHttp.responseText + "'");
    
       return xmlHttp.responseText;
    }
    

    
    
    
    this.setLiveMetadata = function(id,metaname,metavalue)
    {
        var mdserver = this.metadataserverURL();
    
        var url = mdserver + "?a=set-live-metadata";
        url += "&c="+collect_;
        url += "&d="+id;
        url += "&metaname=" + metaname;
        url += "&metavalue=" + metavalue;

        this.urlGetSync(url);
    }
    
    this._setMetadata = function(mode,docid,metaname,metapos,metavalue,metamode)
    {
        var mdserver = this.metadataserverURL();
    
        var params = "a=set" + mode + "-metadata";
        params += "&c="+collect_;
        params += "&d="+docid;
        params += "&metaname=" + metaname;
        if (metapos!=null) {
            params += "&metapos=" + metapos;
        }
        params += "&metavalue=" + metavalue;
		if (metamode!=null) {
            params += "&metamode=" + metamode;
		}
	
        this.urlGetSync(mdserver + "?" + params);
        //this.urlPostSync(mdserver,params);
    }
    
	
	this._setDocumentArrayMetadata = function(mode,docArray,metamode) 
	{
	    docArrayJSON = JSON.stringify(docArray);
  
		var mdserver = this.metadataserverURL();
    
        var params = "a=set" + mode + "-metadata-array";
        params += "&c="+collect_;
		params += "&json="+docArrayJSON;
	
		if (metamode!=null) {
            params += "&metamode=" + metamode;
		}
		
		this.urlGetSync(mdserver + "?" + params);
   
	}
	
	
    this.setDocumentMetadata = function(docid,metaname,metapos,metavalue)
    {
	// Allow for three param call to function, where metapos is missed out
        if (metavalue==null) {
	    // 4 param case
	    metavalue = metapos;
	    metapos = null;
		}
	
		this._setMetadata("",docid,metaname,metapos,metavalue);
		this._setMetadata("-archives",docid,metaname,metapos,metavalue,"override");
	
    }

	this.setDocumentArrayMetadata = function(docArray,metamode) 
	{
	    //showDialog('Greenstone Javascript API','This sequence of changes has been commited into the system.','success', 2);
	
		this._setDocumentArrayMetadata("",docArray,metamode);
		this._setDocumentArrayMetadata("-archives",docArray,metamode);
	}
	
    this.setNewDocumentMetadata = function(docid,metaname,metavalue)
    {
		this._setMetadata("",docid,metaname,null,metavalue);
		this._setMetadata("-archives",docid,metaname,null,metavalue,"accumulate");
    }
	
    this.setImportMetadata = function(docid,metaname,metapos,metavalue)
    {
		this._setMetadata("-import",docid,metaname,metapos,metavalue,"override");
    }
	

    this.explodeDocument = function(docid)
    {
        var exserver = this.explodeserverURL();

        var url = exserver + "?a=explode-document";
        url += "&c="+collect_;
        url += "&d="+docid;
	
        this.urlGetSync(url);
    }

	 this.deleteDocument = function(docid,onlyAdd)
    {
        var exserver = this.explodeserverURL();

        var url = exserver + "?a=delete-document";
        url += "&c="+collect_;
		params += "&onlyadd="+onlyAdd;
        url += "&d="+docid;
	
        this.urlGetSync(url);
    }

	this.deleteDocumentArray = function(docArray,onlyAdd) 
	{
	    docArrayJSON = JSON.stringify(docArray);
  
		var exserver = this.explodeserverURL();
    
        var params = "a=delete-document-array";
        params += "&c="+collect_;
		params += "&onlyadd="+onlyAdd;
		params += "&json="+docArrayJSON;
		
		this.urlGetSync(exserver + "?" + params);
   
	}
	

    this.cloneDocument = function(docid,toCollect)
    {
        var msserver = this.myspaceserverURL();

        var url = msserver + "?a=clone";
        url += "&c="+collect_;
        url += "&d="+docid;
        url += "&toCollect="+toCollect;
	
        this.urlGetSync(url);
    }
    
    // consider name change to reindexDocument
    this.documentReindex = function(docid)
    {
        var mdserver = this.metadataserverURL();
    
        var url = mdserver + "?a=reindex-document";
        url += "&c="+collect_;
        url += "&d="+docid;

        this.urlGetSync(url);
    }


    this.reindexCollection = function(mode,callback)
    {
        if (mode==null) {
			mode = "incremental";
		}

        var idserver = this.indexserverURL();
    
        var url = idserver + "?a=" + mode + "-rebuild";
        url += "&c="+collect_;
    
        this.urlGetAsync(url,callback);
    }    
    

	this.buildByManifestGeneral = function(hashargs) 
	{
        var idserver = this.buildserverURL();
    
        var url = idserver + "?a=build-by-manifest";
        url += "&c="+collect_;
		
		if (hashargs["index-files"] != undefined) {
			url += "&index-files=" +  JSON.stringify(hashargs["index-files"]);
		}
		
		if (hashargs["reindex-files"] != undefined) {
			url += "&reindex-files=" +  JSON.stringify(hashargs["reindex-files"]);
		}
		if (hashargs["delete-OIDs"] != undefined) {
			url += "&delete-OIDs=" +  JSON.stringify(hashargs["delete-OIDs"]);
		}
	
        this.urlGetSync(url);
	}
	
	this.indexByManifest = function(docidArray) 
	{
		var hashargs = {};
		hashargs["index-files"] = docidArray;
		this.buildByManifestGeneral(hashargs);
	}
	
	this.reindexByManifest = function(docidArray) 
	{
		var hashargs = {};
		hashargs["reindex-files"] = docidArray;
		this.buildByManifestGeneral(hashargs);
	}
	this.deleteByManifest = function(docidArray) 
	{
		var hashargs = {};
		hashargs["delete-OIDs"] = docidArray;
		this.buildByManifestGeneral(hashargs);
	}
	
    this.getLiveMetadata = function(id,metaname)
    {
        var mdserver = this.metadataserverURL();
    
        var url = mdserver + "?a=get-live-metadata";
        url += "&c="+collect_;
        url += "&d="+id;
        url += "&metaname=" + metaname;

        var metavalue = this.urlGetSync(url);
  
        return metavalue;
    }
    
    this.getDocumentMetadata = function(docoid,metaname,metapos)
    {
        var mdserver = this.metadataserverURL();
    
        var url = mdserver + "?a=get-metadata";
        url += "&c="+collect_;
        url += "&d="+docoid;
        url += "&metaname=" + metaname;
        if (metapos!=null) {
            url += "&metapos=" + metapos;
        }

        return this.urlGetSync(url);
    }
    
    this.removeLiveMetadata = function(id,metaname)
    {
        var mdserver = this.metadataserverURL();
    
        var url = mdserver + "?a=remove-live-metadata";
        url += "&c="+collect_;
        url += "&d="+id;
        url += "&metaname=" + metaname;

        this.urlGetSync(url);
    }
    
    this.removeDocumentMetadata = function(docid,metaname,metapos)
    {
        var mdserver = this.metadataserverURL();
    
        var url = mdserver + "?a=remove-metadata";
        url += "&c="+collect_;
        url += "&d="+docid;
        url += "&metaname=" + metaname;
        if (metapos!=null) {
            url += "&metapos=" + metapos;
        }

        this.urlGetSync(url);
    }

    return true;

}