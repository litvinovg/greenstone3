
function GSAjaxAPI(gwcgi,collect,un,ky) 
{
    var gwcgi_   = gwcgi;
    var collect_ = collect;
    var un_ = un;
    var ky_ = ky;


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

	if(un_ != null) {
	    url += "&un=" + un_;
	}
	if(ky_ != null) {
	    url += "&ky=" + ky_;
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

       if(un_ != null) {
	   url += "&un=" + un_;
       }
       if(ky_ != null) {
	   url += "&ky=" + ky_;
       }

       xmlHttp.open("GET",url,false);
       xmlHttp.send(null);

       // alert("response = '" + xmlHttp.responseText + "'");
    
       return xmlHttp.responseText;
    }

//*********ADDITIONS TO BRING GS3 VERSION OF THIS FILE UP TO SPEED WITH GS2 VERSION********************//
//*********BUT NOT USED BY GS3. SEE GS3's javascript-global-functions.js INSTEAD (UPCOMING CHANGES)
//*********FOR THE PORTED VERSIONS OF THOSE FUNCTIONS AMONG THESE ADDITIONS THAT ARE NECESSARY FOR GS3.

// New, an Ajax Synchronous Post method.
// http://www.degraeve.com/reference/simple-ajax-example.php
// Async vs Sync: http://www.w3schools.com/ajax/ajax_xmlhttprequest_send.asp 
// Also:
// http://stackoverflow.com/questions/6312447/in-an-ajax-post-do-i-need-to-urlencode-parameters-before-sending
// http://api.jquery.com/jQuery.post/
// http://www.w3schools.com/ajax/ajax_xmlhttprequest_send.asp
    this.urlPostSync = function(scriptURL, params) {
    var xmlHttp=false;
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

    // e.g. scriptURL: /greenstone/cgi-bin/metadata-server.pl
    xmlHttp.open('POST', scriptURL, false); // false means synchronous
    xmlHttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    
    if(un_ != null) {
	params += "&un=" + un_;
    }
    if(ky_ != null) {
	params += "&ky=" + ky_;
    }

    xmlHttp.send(params); // needs to be escaped/encoded

    //alert(scriptURL + "?" + params);
    //alert(xmlHttp.responseText); // if synchronous, process xmlHttp.responseText AFTER send() call
    return xmlHttp.responseText;
}

    // New, an Ajax Asynchronous Post method.
    // For helpful links, see the urlPostSync() method above
    this.urlPostAsync = function(scriptURL, params, callback) {
    var xmlHttp=false;
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



    // e.g. scriptURL: /greenstone/cgi-bin/metadata-server.pl
    xmlHttp.open('POST', scriptURL, true); // true means asynchronous
    xmlHttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');


    // If asynchronous:
    // If the callback param is a function, we will set it up to get called when 
    // the async post has finished (is ready)
    // if the callback parameter isn't a function, the param represents a field 
    // that we want to dynamically update when the async post process has finished

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
		    callback(xmlHttp); // e.g. this might do: updatepage(xmlHttp.responseText);
		}
	    }
	}
	else {
	    alert("Unrecognized type of callback value: " + typeof_callback);
	}
    
    if(un_ != null) {
	params += "&un=" + un_;
    }
    if(ky_ != null) {
	params += "&ky=" + ky_;
    }
    //alert("Posting Async: " + scriptURL + "?" + params);

    xmlHttp.send(params); // needs to be escaped/encoded
    // if synchronous, would process xmlHttp AFTER send() call, such as by
    // accessing xmlHttp.responseText to return that to the caller at this point.
}

    // New
    // The where parameter can be specified as one or more of: import, archives, index, live 
    // separated by |. If null, it is assumed to be index which is the original default 
    // behaviour of calling set-metadata. E.g. where=import|archives|index
    this.setMetadata = function(docid,metaname,metapos,metavalue,metamode,where)
    {
        var mdserver = this.metadataserverURL();
    
        var params = "a=set-metadata";
	if(where != null) {
	    params += "&where=" + where; // if where not specified, meta-server will default to setting index meta
	    //} else {
	    //    params += "&where=import|archives|index";
	}
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
	
        //this.urlGetSync(mdserver + "?" + params);
        this.urlPostSync(mdserver,params);
    }

    // New
    // The where parameter can be specified as one or more of: import, archives, index, live 
    // separated by |. If null, it is assumed to be index which is the original default 
    // behaviour of calling set-metadata-array). E.g. where=import|archives|index
    this.setMetadataArray = function(docArray,metamode,where) 
    {
	docArrayJSON = JSON.stringify(docArray);
	
	var mdserver = this.metadataserverURL();
	
	var params = "a=" + escape("set-metadata-array"); //"a=set-metadata-array";
	if(where != null) {
	    params += "&where=" + escape(where); // if where not specified, meta-server will default to setting index meta
	    //} else {
	    //    params += "&where=import|archives|index";
	}
	params += "&c="+escape(collect_);
	params += "&json="+escape(docArrayJSON);
	
	if (metamode!=null) {
	    params += "&metamode=" + escape(metamode);
	}
	
	//this.urlGetSync(mdserver + "?" + params);
	return this.urlPostSync(mdserver,params);	
    }

    // New
    this.getArchivesMetadata = function(docoid,metaname,metapos)
    {
        var mdserver = this.metadataserverURL();
    
        var url = mdserver + "?a=get-archives-metadata";
        url += "&c="+collect_;
        url += "&d="+docoid;
        url += "&metaname=" + metaname;
        if (metapos!=null) {
            url += "&metapos=" + metapos;
        }

	//alert("In getArchivesMeta. URL: " + url)
        return this.urlGetSync(url); //Once this works, make it POST
    }

    this.getMetadataArray = function(docArray,where)
    {
	docArrayJSON = JSON.stringify(docArray);

	var mdserver = this.metadataserverURL();
	
	var params = "a=" + escape("get-metadata-array"); //"a=set-metadata-array";
	if(where != null) {
	    params += "&where=" + escape(where); // if where not specified, meta-server will default to setting index meta
	    //} else {
	    //    params += "&where=import|archives|index";
	}
	params += "&c="+escape(collect_);
	params += "&json="+escape(docArrayJSON);
	
	//this.urlGetSync(mdserver + "?" + params);
	return this.urlPostSync(mdserver,params);	
    }
//*******END OF ADDITIONS TO BRING GS3 VERSION OF THIS FILE UP TO SPEED WITH GS2 VERSION********//
    
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