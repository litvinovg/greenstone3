var SUCCESS = 1;
var ACCEPTED = 2;
var ERROR = 3;
var CONTINUING = 10;
var COMPLETED = 11;
var HALTED = 12;

gs.functions = new Array();

gs.jqGet = function(id)
{
    return $("#" + id.replace(/\./g, "\\.").replace(/:/g,"\\:"));
}

gs.functions.ajaxRequest = function()
{
	var activexmodes=["Msxml2.XMLHTTP", "Microsoft.XMLHTTP"]; 
	if(window.ActiveXObject)
	{ 
		for (var i=0; i<activexmodes.length; i++)
		{
			try
			{
				return new ActiveXObject(activexmodes[i]);
			}
			catch(e){}
		}
	}
	else if (window.XMLHttpRequest)
	{
		return new XMLHttpRequest();
	}
	else
	{
		return false
	}
}

gs.functions.hasClass = function(elem, classVal)
{
	if(!elem || !elem.getAttribute("class"))
	{
		return false;
	}

	return (elem.getAttribute("class").search(classVal) != -1)
}

gs.functions.getElementsByClassName = function(cl) 
{
	var nodes = new Array();
	var classRegEx = new RegExp('\\b'+cl+'\\b');
	var allElems = document.getElementsByTagName('*');
	
	for (var i = 0; i < allElems.length; i++) 
	{
		var classes = allElems[i].className;
		if (classRegEx.test(classes)) 
		{
			nodes.push(allElems[i]);
		}
	}
	return nodes;
}; 

gs.functions.makeToggle = function(buttons, divs)
{
	var buttonArray = (buttons.length) ? buttons : [buttons];
	var divArray = (divs.length) ? divs : [divs];
	
	for(var i = 0; i < buttonArray.length; i++)
	{
		buttonArray[i].onclick = function()
		{
			for(var j = 0; j < divArray.length; j++)
			{
				if(divArray[j].style.display == "none")
				{
					divArray[j].style.display = "block";
				}
				else
				{
					divArray[j].style.display = "none";
				}
			}
			
			for(var j = 0; j < buttonArray.length; j++)
			{
				if(buttonArray[j].getAttribute("src") == gs.imageURLs.collapse)
				{
					buttonArray[j].setAttribute("src", gs.imageURLs.expand);
				}
				else if(buttonArray[j].getAttribute("src") == gs.imageURLs.expand)
				{
					buttonArray[j].setAttribute("src", gs.imageURLs.collapse);
				}
			}
		};
	}
}

gs.functions.checkForErrors = function(xml)
{
	var errorElems = xml.getElementsByTagName("error");
	
	if(errorElems && errorElems.length > 0)
	{
		var errorString = gs.text.dse.error_saving_changes + ": ";
		for(var i = 0; i < errorElems.length; i++)
		{
			errorString += " " + errorElems.item(i).firstChild.nodeValue;
		}
		alert(errorString);
		return true;
	}
	return false; //No errors
}

gs.functions.validateXML = function(txt)
{
	// code for IE
	if (window.ActiveXObject)
	{
		var xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
		xmlDoc.async = "false";
		xmlDoc.loadXML(document.all(txt).value);

		if(xmlDoc.parseError.errorCode!=0)
		{
			txt = dse.error_code + ": " + xmlDoc.parseError.errorCode + "\n";
			txt = txt + dse.error_reason + ": " + xmlDoc.parseError.reason;
			txt = txt + dse.error_line + ": " + xmlDoc.parseError.line;
			console.log(txt);
			return null;
		}
		
		return xmlDoc;
	}
	// code for Mozilla, Firefox, Opera, etc.
	else if (document.implementation.createDocument)
	{
		var parser = new DOMParser();
		var xmlDoc = parser.parseFromString(txt,"text/xml");

		if (xmlDoc.getElementsByTagName("parsererror").length > 0)
		{
			console.log(gs.text.dse.xml_error);
			return null;
		}
		
		return xmlDoc;
	}
	else
	{
		console.log(gs.text.dse.browse_cannot_validate_xml);
	}
	return null;
}

gs.functions.buildCollections = function(collections, finalFunction)
{
	if(!collections || collections.length == 0)
	{
		console.log("List of collections to build is empty");
		return;
	}

	var counter = 0;
	var buildFunction = function()
	{
		var ajax = new gs.functions.ajaxRequest();
		ajax.open("GET", gs.xsltParams.library_name + "?a=g&rt=r&ro=1&s=BuildCollection&s1.collection=" + collections[counter]);
		ajax.onreadystatechange = function()
		{
			if(ajax.readyState == 4 && ajax.status == 200)
			{
				var text = ajax.responseText;
				var xml = gs.functions.validateXML(text);

				if(!xml || gs.functions.checkForErrors(xml))
				{
					console.log("Could not build collection -> " + collections[counter] + ", aborting");
					return;
				}

				var status = xml.getElementsByTagName("status")[0];
				var pid = status.getAttribute("pid");

				gs.functions.startCheckLoop(pid, "BuildCollection", function()
				{
					var localAjax = new gs.functions.ajaxRequest();
					localAjax.open("GET", gs.xsltParams.library_name + "?a=g&rt=r&ro=1&s=ActivateCollection&s1.collection=" + collections[counter], true);
					localAjax.onreadystatechange = function()
					{
						if(localAjax.readyState == 4 && localAjax.status == 200)
						{
							var localText = localAjax.responseText;
							var localXML = gs.functions.validateXML(localText);
							
							if(!xml || gs.functions.checkForErrors(xml))
							{
								console.log("Could not activate collection -> " + collections[counter] + ", aborting");
								return;
							}

							var localStatus = localXML.getElementsByTagName("status")[0];
							if(localStatus)
							{
								var localPID = localStatus.getAttribute("pid");
								gs.functions.startCheckLoop(localPID, "ActivateCollection", function()
								{
									if (++counter == collections.length)
									{
										//Run this function once we are done building all the collections
										if(finalFunction){finalFunction();}
									}
									else
									{
										buildFunction();
									}
								});
							}
						}
					}
					localAjax.send();
				});
			}
		}
		ajax.send();
	}
	buildFunction();
}

gs.functions.startCheckLoop = function(pid, serverFunction, callbackFunction)
{
	var ajaxFunction = function()
	{
		var ajax = new gs.functions.ajaxRequest();
		ajax.open("GET", gs.xsltParams.library_name + "?a=g&rt=s&ro=1&s=" + serverFunction + "&s1.pid=" + pid, true);
		ajax.onreadystatechange = function()
		{
			if(ajax.readyState == 4 && ajax.status == 200)
			{
				var text = ajax.responseText;
				var xml = gs.functions.validateXML(text);
				
				if(!xml || gs.functions.checkForErrors(xml))
				{
					console.log("Could not check status of " + serverFunction + ", there was an error in the XML, aborting");		
					return;
				}

				var status = xml.getElementsByTagName("status")[0];
				var code = status.getAttribute("code");

				if (code == COMPLETED || code == SUCCESS)
				{
					callbackFunction();
				}
				else if (code == HALTED || code == ERROR)
				{
					console.log("Could not check status of " + serverFunction + ", there was an error on the server, aborting");
				}
				else
				{
					setTimeout(ajaxFunction, 1000);
				}
			}
		}
		ajax.send();
	}
	ajaxFunction();
}

function inc(a, b)
{
	var carry = 0;
	var num = 0;
	var i = 0;
	
	while((carry || (i < a.length) || (i < b.length)) && (i < 100))
	{
		num = carry;
		if(i < a.length){num += a[i];}
		if(i < b.length){num += b[i];}
		
		if(num >= 256)
		{
			num -= 256;
			carry = 1;
		}
		else
		{
			carry = 0;
		}
		
		a[i] = num;
		
		i++;
	}
}

function ifposDec(a, b)
{
	var carry = 0;
	var num = 0;
	var i = 0;
	
	if(b.length > a.length){return a;}
	if(b.length == a.length)
	{
		i = a.length - 1;
		while(i >= 0)
		{
			if(a[i] > b[i]){break;}
			if(a[i] < b[i]){return a;}
			i--;
		}
	}
	
	i = 0;
	var len = 0;
	var outString = "";
	while((i < a.length) || (i < b.length))
	{
		num = -carry;
		if(i < a.length){num += a[i];}
		if(i < b.length){num -= b[i];}
		
		if(num < 0)
		{
			num += 256;
			carry = 1;
		}
		else
		{
			carry = 0;
		}
		
		a[i] = num;
		outString += num + ","
		i++
		
		if(num != 0){len = i}
	}

	if(len < a.length)
	{
		a = a.slice(0, len);
	}
	
	return a;
}

function convertNum(a)
{
	var result = new Array();
	var i;
	var convert = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"];
	
	if(a.length == 0)
	{
		result.push("0");
		return result;
	}
	
	for(i = a.length - 1; i >= 0; i--)
	{
		result.push(convert[Math.floor(a[i]/16)]);
		result.push(convert[Math.floor(a[i]%16)]);
	}
	
	var resultString = "";
	for(var j = 0; j < result.length; j++)
	{
		resultString += result[j];
	}
	
	return resultString;
}

gs.functions.hashString = function(str)
{
	var remainder = new Array();
	var primePow = new Array();
	var pow = 
	[
		255, 255, 255,
		255, 255, 255,
		255, 255, 255,
		255, 255, 1
	];
	
	for(var i = 0; i < 8; i++)
	{
		primePow.push(pow.slice()); //The javascript way to do an array copy (yuck!)
		inc(pow, pow);
	}
	
	for(var i = 0; i < str.length; i++)
	{
		var c = str.charCodeAt(i);

		if(remainder.length == 99)
		{
			return null;
		}

		for(var j = remainder.length; j > 0; j--)
		{
			remainder[j] = remainder[j-1];
		}
		remainder[0] = c;

		for(var j = 7; j >= 0; j--)
		{
			remainder = ifposDec(remainder, primePow[j]);
		}	
	}
	
	return convertNum(remainder);
}

// This method performs an AJAX call after working out, based on parameters and internal decision-making code,
// if it's using GET or POST,
// asynchronous or synchronous AJAX, 
// jQuery's .ajax() method or gsajaxapi.js' regular JavaScript way of calling AJAX (necessary functions
// now ported from GS2 to GS3)
// and whether it needs to transmit the payload in URL or data structure (Java object) form.
// In the past, the AJAX calls to metadataserver.pl only dealt with URLs and used jQuery .ajax(). As a
// consequence of this particular combination, the calls in the past were all GET operations.
//
// - payload param: contains both the URL form and the data object form of the package to transmit over
// AJAX to metadataserver.pl. Based on the parameters and some internal variables, callMetadataServer()
// determines which to use.
// - opts param: No function overloading in JavaScript. Can pass a custom object, however can pass opts,
// see http://stackoverflow.com/questions/456177/function-overloading-in-javascript-best-practices
function callMetadataServer(callingFunction, payload, responseFunction, opts)
{

    // async AJAX by default for get operations: because internal processing of 'read' operations (get meta) 
    // is not order dependent.
    // Set/remove operations will switch to synchronous AJAX, unless opt["forceSync"] is set otherwise
    var async_setting = true; 
    var method = "GET"; // GET was the default before
    
    // Set to false if you wish to use the regular JavaScript AJAX way in gsajaxapi.js (will use payload.url)
    // Set to true if using jQuery AJAX (will use payload.data).
    var _use_jQuery_ajax_not_gsajaxapi = true; 

    // _use_payload_in_data_not_url_form is determined based on vars method and _use_jQuery_ajax_not_gsajaxapi 
    // If using AJAX with payload data (with jQuery) rather than using URLs containing data (payload in url): 
    // using data will allow us to use jQuery to POST stuff too.
    // For gsajaxapi, payload to be transmitted over AJAX must be in URL form, whether GET or POST.
    // For jQuery, AJAX calls ended up as GET when the payload is in URL form.
    // Default used to be payload in url form. To get the default back, 
    // set method = "GET" (above, but also in calling functions that specify this optional parameter!)
    // and set the default here below for _use_payload_in_data_not_url_form to false.
    var _use_payload_in_data_not_url_form = false;

    var _modifyingMeta  = false;

    var url = payload["url"]; // for jQuery GET, and for GET and POST using JavaScript AJAX 
    var data = payload["data"]; // for jQuery POST


    // check for any caller overrides
    if(opts != null) {
	//if(opts["use_payload_in_data_not_url_form"] != null) {
	  //  _use_payload_in_data_not_url_form = opts["use_payload_in_data_not_url_form"];
	//}
	if(opts["requestMethod"] != null) {
	    method = opts["requestMethod"];
	}
    }
    
    // sync or async? Generally, synchronous AJAX for set-meta operations, and asynchronous for get-meta ops
    var metaServerCommand = (data["s1.a"] == null) ? data["a"] : data["s1.a"];
    if(metaServerCommand.indexOf("set-") != -1 || metaServerCommand.indexOf("remove-") != -1) {
	_modifyingMeta  = true;
	async_setting = false; // for 'write' operations (set/remove meta), we force sequential processing of the internal operation.

	// for meta modification operatons, should we make the method=POST??????
	// method = "POST";
    }
    // check for any overrides by calling code that knows what it's doing
    if (opts != null && opts["forceSync"] != null) {
	async_setting = (!opts["forceSync"]);
    }

    if(_use_jQuery_ajax_not_gsajaxapi) {
	if(method == "POST") {
	    _use_payload_in_data_not_url_form = true;
	}  // if GET, can use payload in URL form or in data form for jQuery AJAX
	// to put it another way: can't do jQuery POST operations with payload in URL form

    } else { // using gsajaxapi.js, which only accepts the payload in URL form, whether GET or POST	
	_use_payload_in_data_not_url_form = false;
    }

    // use the URL form or the data form to transmit the payload over AJAX?
    // Payload in data form implies jQuery AJAX, not gsajaxapi calls,
    // since we can't use gsajaxapi.js AJAX GET/POST calls without payload in URL form
    if(_use_payload_in_data_not_url_form) { // using data payload to do AJAX (regardless of request method)
	
	//method = "POST";

	// for get-meta operations, go directly through metadata-server.pl
	// for set-meta ops, should go via GS3 authentication, which is off the GS3 library servlet
	url = (_modifyingMeta) ? gs.xsltParams.library_name : "cgi-bin/metadata-server.pl"; 	

    } else { // uses data encoded into URL, rather than a data structure. 
	data = null; // we're using the URL as payload, don't duplicate the payload to be transmitted into data
	
	url = payload["url"]; // payload["url"] contains the URL + data encoded in URL form
	// URL is already correct for get-meta vs meta-modification operations.
	// For meta-modification ops, it will through GS3 authentication first rather than metadata-server.pl
	
    } 

    // finally, can do the AJAX call

    console.log("*** Away to call: " + url);
    var ajaxResponse = async_setting ? "*** No response received yet, async ajax request" : null;


    if(_use_jQuery_ajax_not_gsajaxapi) {
	// ajax calls default to using method GET, we want to do POST operations for get-meta and set-meta requests
	// since get-meta-array and especially set-meta-array can be large, e.g. for user comments.
	$.ajax({url: url, async: async_setting, type: method, data: data})
	    .success(function(response) {
		ajaxResponse = response;
		console.log("** (" + callingFunction + ") Response received from server: " + ajaxResponse);
		
		//var xml = $.parseXML(response);
		//console.log(xml);
		
		if(responseFunction != null) {
		    
		    responseFunction(response);
		}
	    })
	    .error(function() {
		console.log("(" + callingFunction + ") Failed");
	    });
    }
    else {
	// USES GSAJAXAPI.JS to do AJAX. In this case, the payload must be in URL form
	
	var splitURL = url.split("?");
	url = splitURL[0]; // base URL
	var params = splitURL[1]; // query-string

	// Don't need to delete objects created with 'new' in JavaScript. Garbage collection will do it.
	// http://stackoverflow.com/questions/4869712/new-without-delete-on-same-variable-in-javascript
	var gsapi = new GSAjaxAPI(url);
	
	// ajax calls default to using method GET, we want to do POST operations for get-meta and set-meta requests
	// since get-meta-array and especially set-meta-array can be large, e.g. for user comments.
	
	if(async_setting) {
	    gsapi.urlPostAsync(url, params, responseFunction);
	} else {
	    ajaxResponse = gsapi.urlPostSync(url, params);
	    ajaxResponse = ajaxResponse;
	}
	
	console.log("*** (" + callingFunction + ") Response from server: " + ajaxResponse);

    }
    
    console.log("*** Finished ajax call to: " + url);    
    console.log("*** Got response: " + ajaxResponse);

    return ajaxResponse;
}

// Prepare the payload (data package) to transmit to metadataserver.pl over AJAX.
// These next 2 functions prepare both the URL version of the payload and the data object version of 
// of the payload. Then calling functions, and the callMetadataServer() function they call, will control
// and determine which of the two forms ultimately gets used.
// UNUSED: http://stackoverflow.com/questions/8648892/convert-url-parameters-to-a-javascript-object
function getBasicDataForMetadataServer(metaServerCommand, collection, site, documentID, metadataName, metamode, metadataValue, prevMetadataValue, metadataPosition) {

    // if we're doing set- or remove- metadata operations,
    // then we need change the data params that will make up the query string
    // to make sure we go through GS3's authentication
    // 1. prefix meta names with s1,
    // 2. use s1.collection for collectionName since c is a special param name for GS2Construct
    // 3. Additional parameters for rerouting through Authentication: a=g&rt=r&ro=1&s=ModifyMetadata

    var modifyingMeta  = false;
    var prefix = "";
    var colPropName = "c";
    var baseURL = "cgi-bin/metadata-server.pl?";

    // if we need authentication:
    if(metaServerCommand.indexOf("set-") != -1 || metaServerCommand.indexOf("remove-") != -1) {
	modifyingMeta  = true;
	prefix = "s1.";
	colPropName = prefix+"collection"; // "s1.collection"
	baseURL = gs.xsltParams.library_name + "?a=g&rt=r&ro=1&s=ModifyMetadata&";
    }


    // 1. when using jQuery to POST, need to invoke AJAX with a data structure rather than a URL
    var data = {};

    // customizable portion of ajax call
    data[prefix+"a"] = metaServerCommand;
    data[colPropName] = collection;
    data[prefix+"site"] = site;
    data[prefix+"d"] = documentID;
    data[prefix+"metaname"] = metadataName;
    data[prefix+"metapos"] = metadataPosition;
    data[prefix+"metavalue"] = metadataValue;
    data[prefix+"prevmetavalue"] = prevMetadataValue;
    data[prefix+"metamode"] = metamode;

    if(modifyingMeta) {
	// fixed portion of url: add the a=g&rt=r&ro=1&s=ModifyMetadata part of the GS3 URL for
	// going through authentication. Don't prefix "s1." to these!
	data["a"] = "g";
	data["rt"] = "r";
	data["ro"] = "1";
	data["s"] = "ModifyMetadata";
    }

    // 2. Construct the URL version of the metadata-server.pl operation:
    // for GET requests, the URL can contain the data.
    // Regular JavaScript AJAX code in gsajaxapi.js can also POST data in URL form, but not jQuery's .ajax().

    	
    // If doing set- or remove- (not get-) metadata, then rewrite URLs to call GS2Construct's ModfiyMetadata service instead (which will ensure this only works when authenticated). 
    // From:
    // <gs3server>/cgi-bin/metadata-server.pl?a=set-archives-metadata&c=smallcol&site=localsite&d=HASH01454f31011f6b6b26eaf8d7&metaname=Title&metavalue=Moo&prevmetavalue=Blabla&metamode=override
    // To:
    // <gs3server>/library?a=g&rt=r&ro=1&s=ModifyMetadata&s1.a=set-archives-metadata&s1.collection=smallcol&s1.site=localsite&s1.d=HASH01454f31011f6b6b26eaf8d7&s1.metaname=Title&s1.metavalue=Moo&s1.prevmetavalue=Blabla&s1.metamode=override

    var extraParams = "";
    
    if(metadataValue != null) {
	extraParams += "&"+prefix+"metavalue=" + metadataValue;
    }
    
    if(metadataPosition != null)
    {
	extraParams += "&"+prefix+"metapos=" + metadataPosition;	    
    }
    
    if(prevMetadataValue != null) {
	extraParams += "&"+prefix+"prevmetavalue=" + prevMetadataValue;
    }
    
    var url = baseURL + prefix+"a=" + metaServerCommand + "&"+colPropName+"=" + collection + "&"+prefix+"site=" + site + "&"+prefix+"d=" + documentID + "&"+prefix+"metaname=" + metadataName + extraParams + "&"+prefix+"metamode=" + metamode;

    // 3. Return both the constructed url & data variants of the payload to be transmitted over ajax
    var payload = {
	url: url,    
	data: data
    };

    return payload;
}

// See description for getBasicDataForMetadataServer()
function getComplexDataForMetadataServer(metaServerCommand, collection, site, docArray, metamode, where) {

    var docArrayJSON = JSON.stringify(docArray);
    
    // if we're doing set- or remove- metadata operations,
    // then we need change the data params that will make up the query string
    // to make sure we go through GS3's authentication
    // 1. prefix meta names with s1,
    // 2. use s1.collection for collectionName since c is a special param name for GS2Construct
    // 3. Additional parameters for rerouting through Authentication: a=g&rt=r&ro=1&s=ModifyMetadata

    var modifyingMeta  = false;
    var prefix = "";
    var colPropName = "c";
    var baseURL = "cgi-bin/metadata-server.pl?";

    // if we need authentication:
    if(metaServerCommand.indexOf("set-") != -1 || metaServerCommand.indexOf("remove-") != -1) {
	modifyingMeta  = true;
	prefix = "s1.";
	colPropName = prefix+"collection"; // "s1.collection"
	baseURL = gs.xsltParams.library_name + "?a=g&rt=r&ro=1&s=ModifyMetadata&";
    }

    // 1. when using jQuery to POST, need to invoke AJAX with a data structure rather than a URL
    var data = {};

    // customizable portion of ajax call
    data[prefix+"a"] = metaServerCommand;
    data[colPropName] = collection;
    data[prefix+"site"] = site;
    data[prefix+"json"] = docArrayJSON;
    
    if(where != null) {
	data[prefix+"where"] = where;
    }
    if (metamode!=null) {
	data[prefix+"metamode"] = metamode;
    }

    if(modifyingMeta) {
	// fixed portion of url: add the a=g&rt=r&ro=1&s=ModifyMetadata part of the GS3 URL for
	// going through authentication. Don't prefix "s1." to these!
	data["a"] = "g";
	data["rt"] = "r";
	data["ro"] = "1";
	data["s"] = "ModifyMetadata";
    }
    

    // 2. URL for when doing AJAX in URL mode. GET with jQuery allows the data to be part of the URL, but
    // not jQuery POST. But our regular JavaScript AJAX code in gsajaxapi.js allows GET and POST with URLs
    // containing the data.    
    
    var params = prefix+"a=" + escape(metaServerCommand); //"a=set-metadata-array";
    if(where != null) {
	params += "&"+prefix+"where=" + escape(where); // if where not specified, meta-server will default to setting index meta
	//} else {
	//    params += "&"+prefix+"where=import|archives|index";
    }
    params += "&"+colPropName+"="+escape(collection);
    params += "&"+prefix+"site="+escape(site);
    params += "&"+prefix+"json="+escape(docArrayJSON);
    
    if (metamode!=null) {
	params += "&"+prefix+"metamode=" + escape(metamode);
    }

    // 3. Return both the constructed url & data variants of the payload to be transmitted over ajax
    var payload = {
	url: baseURL + params,    
	data: data
    };

    return payload;
}

/*************************
* SET METADATA FUNCTIONS *
*************************/

// callMetadataServerURLMode("setImportMetadata", "cgi-bin/metadata-server.pl?a=set-import-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metavalue=" + metadataValue + "&prevmetavalue=" + prevMetadataValue + "&metamode=" + metamode, responseFunction);

gs.functions.setImportMetadata = function(collection, site, documentID, metadataName, metadataValue, prevMetadataValue, metamode, responseFunction)
{    

    callMetadataServer(
	"setImportMetadata", 
	getBasicDataForMetadataServer("set-import-metadata", collection, site, documentID, metadataName, metamode, metadataValue, prevMetadataValue, null /*metapos*/), 
	responseFunction);
    
}

gs.functions.setArchivesMetadata = function(collection, site, documentID, metadataName, metadataPosition, metadataValue, prevMetadataValue, metamode, responseFunction)
{
    if(metadataPosition != null) {
	prevMetadataValue = null; // to force the same ultimate behaviour as in the old version of this code
    }

    callMetadataServer(
	"setArchivesMetadata",
	getBasicDataForMetadataServer("set-archives-metadata", collection, site, documentID, metadataName, metamode, metadataValue, prevMetadataValue, metadataPosition),
	responseFunction);

}

gs.functions.setIndexMetadata = function(collection, site, documentID, metadataName, metadataPosition, metadataValue, prevMetadataValue, metamode, responseFunction)
{
    if(metadataPosition != null) {
	prevMetadataValue = null; // to force the same ultimate behaviour as in the old version of this code
    }

    // old version of this function would only call callMetadataServer if either metapos
    // or prevMetaValue had a value. So sticking to the same behaviour in rewriting this function.
    if(metadataPosition != null || prevMetadataValue != null) { 
    
	callMetadataServer(
	    "setIndexMetadata",
	    getBasicDataForMetadataServer("set-metadata", collection, site, documentID, metadataName, metamode, metadataValue, prevMetadataValue, metadataPosition),
	    responseFunction);
    }
}

gs.functions.setMetadata = function(collection, site, documentID, metadataName, metadataValue, metamode, responseFunction)
{
	var nameArray = ["setImportMetadata", "setArchivesMetadata", "setIndexMetadata"];
	var functionArray = ["set-import-metadata", "set-archives-metadata", "set-metadata"];
	
	for(var i = 0; i < nameArray.length; i++)
	{
	    // previous version of this function did not allow setting metapos or prevMetavalue
	    // so leaving the behaviour the same along with function signature.
	    callMetadataServer(
		nameArray[i],
		getBasicDataForMetadataServer(functionArray[i], collection, site, documentID, metadataName, metamode, metadataValue, null /*prevMetadataValue*/, null /*metadataPosition*/),
		responseFunction);
	}
}

// New. Modified version of the GS2 version of this method in gsajaxapi.js.
// The where parameter can be specified as one or more of: import, archives, index, live 
// separated by |. If null, it is assumed to be index which is the original default 
// behaviour of calling set-metadata-array. E.g. where=import|archives|index
// THIS METHOD IS SYNCHRONOUS by default. Set forceSync to false to override this default behaviour
gs.functions.setMetadataArray = function(collection, site, docArray, metamode, where, responseFunction, forceSync) 
{  

    var payload = getComplexDataForMetadataServer("set-metadata-array", collection, site, docArray, metamode, where);
    
    // set operations are generally synchronous, but allow calling function to force ajax call 
    // to be synchronous or not. Default is synchronous, as it was for GS2
    if(forceSync == null) {
	forceSync = true;
    }
    
    //console.log("cgi-bin/metadata-server.pl?"+params);
    
    var response = callMetadataServer("Setting metadata in "+where, payload, responseFunction, {"forceSync": forceSync, "requestMethod": "POST"});
	
    return response;
}


/*************************
* GET METADATA FUNCTIONS *
*************************/

// New. Modified version of the GS2 version of this method in gsajaxapi.js.
// See description for setMetadataArray above for information about the 'where' parameter.
// THIS METHOD IS SYNCHRONOUS BY DEFAULT. Set forceSync to false to override this default behaviour
gs.functions.getMetadataArray = function(collection, site, docArray, where, responseFunction, forceSync)
{
    var payload = getComplexDataForMetadataServer("get-metadata-array", collection, site, docArray, null /*metamode*/, where);    

    // get operations are generally asynchronous, but allow calling function to force ajax call 
    // to be synchronous or not. Default for get-metadata-array is synchronous, as it was for GS2
    if(forceSync == null) {
	forceSync = true;
    }
    // Objects/maps can use identifiers or strings for property names
    // http://stackoverflow.com/questions/456177/function-overloading-in-javascript-best-practices
    // https://www.w3schools.com/js/js_objects.asp
    var response = callMetadataServer("Getting metadata from "+where, payload, responseFunction, {"forceSync":forceSync, "requestMethod": "POST"});

    return response;
}


gs.functions.getImportMetadata = function(collection, site, documentID, metadataName, responseFunction)
{
    var payload = getBasicDataForMetadataServer("get-import-metadata", collection, site, documentID, metadataName);
    callMetadataServer("getImportMetadata", payload, function(responseText) {
	    var metadata = new GSMetadata(collection, site, documentID, metadataName, null, null, responseText);
		if(responseFunction != null)
		{
			responseFunction(metadata);
		}
	});
}

gs.functions.getArchivesMetadata = function(collection, site, documentID, metadataName, metadataPosition, responseFunction)
{
    var payload = getBasicDataForMetadataServer("get-archives-metadata", collection, site, documentID, metadataName, null /*metamode*/, null /*metavalue*/, null /*prevmetavalue*/, metadataPosition);

    callMetadataServer("getArchivesMetadata", payload, function(responseText) {
	var metadata = new GSMetadata(collection, site, documentID, metadataName, null, metadataPosition, responseText); // indexPos, archivesPos, metaval (responseText)
	if(responseFunction != null)
	{
	    responseFunction(metadata);
	}
    });
}

gs.functions.getIndexMetadata = function(collection, site, documentID, metadataName, metadataPosition, responseFunction)
{
    var payload = getBasicDataForMetadataServer("get-metadata", collection, site, documentID, metadataName, null /*metamode*/, null /*metavalue*/, null /*prevmetavalue*/, metadataPosition);

    callMetadataServer("getIndexMetadata", payload, function(responseText) {
	var metadata = new GSMetadata(collection, site, documentID, metadataName, metadataPosition, null, responseText); // indexPos, archivesPos, metaval (responseText)
	
	if(responseFunction != null)
	{
	    responseFunction(metadata);
	}
    });
}

/****************************
* REMOVE METADATA FUNCTIONS *
****************************/

gs.functions.removeImportMetadata = function(collection, site, documentID, metadataName, metadataValue, responseFunction)
{
    callMetadataServer(
	"removeImportMetadata", 
	getBasicDataForMetadataServer("remove-import-metadata", collection, site, documentID, metadataName, null /*metamode*/, metadataValue, null /*prevmetavalue*/, null /*metapos*/), 
	responseFunction);
}

gs.functions.removeArchivesMetadata = function(collection, site, documentID, metadataName, metadataPosition, metadataValue, responseFunction)
{
    if(metadataPosition != null) {
	metadataValue = null; // retaining behaviour of previous version of this function removeArchivesMetadata()
    }

    callMetadataServer(
	"removeArchiveMetadata", 
	getBasicDataForMetadataServer("remove-archives-metadata", collection, site, documentID, metadataName, null /*metamode*/, metadataValue, null /*prevmetavalue*/, metadataPosition), 
	responseFunction);
}

gs.functions.removeIndexMetadata = function(collection, site, documentID, metadataName, metadataPosition, metadataValue, responseFunction)
{
    if(metadataPosition != null) {
	metadataValue = null; // retaining behaviour of previous version of this function removeIndexMetadata()
    }

    callMetadataServer(
	"removeIndexMetadata", 
	getBasicDataForMetadataServer("remove-metadata", collection, site, documentID, metadataName, null /*metamode*/, metadataValue, null /*prevmetavalue*/, metadataPosition), 
	responseFunction);
}

gs.functions.removeMetadata = function(collection, site, documentID, metadataName, metadataValue, responseFunction)
{
	var nameArray = ["removeImportMetadata", "removeArchivesMetadata", "removeIndexMetadata"];
	var functionArray = ["remove-import-metadata", "remove-archives-metadata", "remove-metadata"];
	
	for(var i = 0; i < nameArray.length; i++)
	{
	    callMetadataServer(
		nameArray[i],
		getBasicDataForMetadataServer(functionArray[i], collection, site, documentID, metadataName, null /*metamode*/, metadataValue, null /*prevmetavalue*/, null /*metapos*/),
		responseFunction);
	}
}
