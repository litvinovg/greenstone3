/*

Greenstone 3 'Client-side transformer'
Performs client-side transformations of XSLT, using HTML5 local storage (simulated if the browser doesn't support it).

Currently only supports Firefox 3 and greater, since it's the only browser (at the time of writing) that can do this properly.

@author		Steven McTainsh
@date		14/02/2011

*/

/* These URLs and file paths are fetched dynamically */
//var gsweb = ""; // The file path to the Greenstone 3 web directory
//var gsurl = ""; // The root URL for this Greenstone 3 installation

/* Misc. switches and paths */
var keyUrl = ''; // Used across methods to build up query string for text retrieval (client-side transformed version)
var on = true; // Set to false to disable operation
var debug = true;

var index = 0; // Used for query array (to keep track of number of elements)
var deferredEls = new Array(); // Elements to defer text retrieval for until later
var queryArr = new Array(); // Text to query for (the text for the corresponding deferred element)

function notSupported() {
    // Set not supported cookie here
    document.cookie = 'supportsXSLT=false; expires=0; path=/';
    // Fall back to server version
    var location = window.location.search.substring(1);
    if(location == '') {
	// Start with a question mark
	location = window.location + "?o=server";
    } else {
	// Start with an ampersand
	location = window.location + "&o=server";
    }		
    window.location = location;
}

/*
$(document).ready(function() {
    console.log("Client-side XSLT: Document is ready");
    
    if (isSupported()) {		
	transform(false);
    }
    else {
	notSupported();
    }
});
*/


var onSaxonLoad = function() {

    try {
	var rooturl = window.location.pathname;
	var queryStr = window.location.search.substring(1);
	queryStr = queryStr.replace(/o=.*?(&|$)/g,"");
	if (queryStr != '') {
	    queryStr += "&";
	}
	queryStr += "o=clientside";
	    
	console.log("*** rooturl = " + rooturl);
	console.log("*** queryStr = " + queryStr);

	var skindoc = "";
	var xmldoc = "";	
	
	$.get(rooturl + "?" + queryStr, function(data) {

	    //dataStr = dataStr.replace(/gs3:id/g, "gs3id");
		
	    //data = parseFromString(dataStr, "text/xml");
		
	    var toplevel_children = $(data).children().eq(0).children();
	    var skindoc = toplevel_children[0];
	    var xmldoc = toplevel_children[1];
	    
		
	    var site_name = $('xsltparams>param[name=site_name]', xmldoc).text();
	    var library_name = $('xsltparams>param[name=library_name]', xmldoc).text();
	    var interface_name = $('xsltparams>param[name=interface_name]', xmldoc).text();
	    
	    //// Convert temporarily to text here		
	    skindoc = convertToString(skindoc);
	    xmldoc = convertToString(xmldoc);
		
	    //// This could just be done on the server (later)
	    ////data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" /*+ "<?xml-stylesheet type=\"text/xsl\" href=\"" + skinurl + "\"?>\r\n"*/ + data;
	    //// replace all!
	    //// Be careful with regex syntax and the use of special characters!
	    //skindoc = skindoc.replace(/util\:exists\(\$meta, ''\)/g, "$meta!=''"); // For now - use regex instead
	    //skindoc = skindoc.replace(/%3A/g, ":"); // undo colon escaping

	    //skindoc = skindoc.replace(/util\:.+?\(\.*?\)/g, "true");

	    skindoc = skindoc.replace(/<xslt:stylesheet\s+/,"<xslt:stylesheet xmlns:ixsl=\"http://saxonica.com/ns/interactiveXSLT\" xmlns:js=\"http://saxonica.com/ns/globalJS\" ");
	    //extension-element-prefixes="ixsl" exclude-result-prefixes="xs xd d js"
	    skindoc = skindoc.replace(/extension-element-prefixes="(.*?)"/,"extension-element-prefixes=\"$1 ixsl\"");
	    //skindoc = skindoc.replace(/exclude-result-prefixes="(.*?)"/,"exclude-result-prefixes=\"$1 js\"");
	    
	    
	    skindoc = skindoc.replace(/util\:exists\(\$meta, ''\)/g, "$meta!=''"); // For now - use regex instead
	    skindoc = skindoc.replace(/util:replace\((.*?)\)/g, "replace($1)"); // exists in XSLT 2.0
	    skindoc = skindoc.replace(/util:storeString\((.+?),(.+)?\)/g, "$1=$2");
	    skindoc = skindoc.replace(/util:getString\((.+?)\)/g, "$1");
	    skindoc = skindoc.replace(/util:getDetailFromDate\((.+?),.+?,.+?\)/g, "$1");

	    //skindoc = skindoc.replace(/util:getNumberedItem\(([^,]+),([^,]+)\)$/,"js:getNumberedItem($1,$2)"); // never tested
	    
	    //skindoc = skindoc.replace(/\{\{/,"\\{");
	    //skindoc = skindoc.replace(/\}\}/,"\\}");
	    //skindoc = skindoc.replace(/"js:[^"]+"/g, "js:console.log('foo')");
	    
	    // Convert to XML
	    xmldoc = parseFromString(xmldoc, "text/xml");			
	    skindoc = parseFromString(skindoc, "text/xml");
	    
	    var output = '';
		/*
	    var params = {  'library_name': library_name, 'interface_name': interface_name };
	    var proc = Saxon.run( {
 		stylesheet:   skindoc,
 		source:       xmldoc,
		parameters: params
	    });
*/
		
      /*
		var xml = Saxon.requestXML("http://localhost:8383/greenstone3/mozarts-library/collection/digital-music-stand/page/about?o=xml");
		var xsl = Saxon.requestXML("http://localhost:8383/greenstone3/mozarts-library/collection/digital-music-stand/page/about?o=skinandlibdocfinal");
*/
	    var proc = Saxon.newXSLT20Processor(skindoc);
		
	    proc.setParameter(null, 'library_name', library_name);
	    proc.setParameter(null, 'interface_name', interface_name);
	    proc.setParameter(null, 'site_name', site_name);		

	    result = proc.transformToDocument(xmldoc);
	    xmlSer = new XMLSerializer();
	    output = xmlSer.serializeToString(result);

	    var doc = document.open();
	    doc.write(output);
	    doc.close();

	    document.cookie = 'supportsXSLT=true; expires=0; path=/';

			
	    
	}, 'xml');
    }
    catch (e) {
	if(trial) {
	    notSupportedCookie();
	}
	else {
	    notSupported();
	}
    }
}

      


function transform(trial) {

    //alert("transform(): trial = " + trial);
    
    try {
	var rooturl = window.location.pathname;
	var queryStr = window.location.search.substring(1);
	queryStr = queryStr.replace(/o=.*?(&|$)/g,"");
	if (queryStr != '') {
	    queryStr += "&";
	}
	queryStr += "o=clientside";
	    
	console.log("*** rooturl = " + rooturl);
	console.log("*** queryStr = " + queryStr);

	var skindoc = "";
	var xmldoc = "";	
	
	    $.get(rooturl + "?" + queryStr, function(dataStr) {
 		//dataStr = dataStr.trim();
		dataStr = dataStr.replace(/gs3:id/g, "gs3id");
		//dataStr = dataStr.replace(/&/g,"&amp;");
		//dataStr = dataStr.replace(/&amp;quot;/g,"&quot;");
		//dataStr = dataStr.replace(/&amp;lt;/g,"&lt;");
		//dataStr = dataStr.replace(/&amp;gt;/g,"&gt;");
		//dataStr = dataStr.replace(/&amp;amp;/g,"&amp;");
		
		data = parseFromString(dataStr, "text/xml");
		
		var toplevel_children = $(data).children().eq(0).children();
		var skindoc = toplevel_children[0];
		var xmldoc = toplevel_children[1];

		
	        //gsurl = $($(xmldoc).find("metadata[name=siteURL]")[0]).text();
	        //gsurl = $(xmldoc).find("pageRequest").attr("baseURL");
	        var site_name = $('xsltparams>param[name=site_name]', xmldoc).text();
	        var library_name = $('xsltparams>param[name=library_name]', xmldoc).text();
	        var interface_name = $('xsltparams>param[name=interface_name]', xmldoc).text();

 	        //gsurl = $(xmldoc).find("pageRequest").attr("baseURL") + site_name;
		//gsweb = new RegExp($($(xmldoc).find("param[name=filepath]")[0]).text().replace(/\\/g, "\\\\"), "g");
			    
		//// Convert temporarily to text here		
		skindoc = convertToString(skindoc);
		xmldoc = convertToString(xmldoc);
		
		//// This could just be done on the server (later)
		////data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" /*+ "<?xml-stylesheet type=\"text/xsl\" href=\"" + skinurl + "\"?>\r\n"*/ + data;
		//// replace all!
		//// Be careful with regex syntax and the use of special characters!
		//skindoc = skindoc.replace(/util\:exists\(\$meta, ''\)/g, "$meta!=''"); // For now - use regex instead
		//skindoc = skindoc.replace(/%3A/g, ":"); // undo colon escaping

		//skindoc = skindoc.replace(/util\:.+?\(\.*?\)/g, "true");

		skindoc = skindoc.replace(/util\:exists\(\$meta, ''\)/g, "$meta!=''"); // For now - use regex instead
		skindoc = skindoc.replace(/util:storeString\((.+?),(.+)?\)/g, "$1=$2");
		skindoc = skindoc.replace(/util:getString\((.+?)\)/g, "$1");
		skindoc = skindoc.replace(/util:getDetailFromDate\((.+?),.+?,.+?\)/g, "$1");
		
		
		// Convert to XML
		xmldoc = parseFromString(xmldoc, "text/xml");			
		skindoc = parseFromString(skindoc, "text/xml");
			
		var output = '';

		// And post-process...			
		if(window.ActiveXObject) {
		    // IE
		    var procFactory = new ActiveXObject("MSXML2.XSLTemplate");
		    procFactory.stylesheet = skindoc;
		    var proc = procFactory.createProcessor();
		    proc.input = xmldoc;
		    
		    proc.addParameter('library_name', library_name);
		    proc.addParameter('interface_name', interface_name);
		    
		    proc.transform();
		    output = proc.output;
		}
		else {			
		    // Firefox, Chrome ...
		    xsltProc = new XSLTProcessor();
		    // The leading slash is oh-so important here
		    xsltProc.setParameter(null, 'library_name', library_name);
		    xsltProc.setParameter(null, 'interface_name', interface_name);
		    try {
			xsltProc.importStylesheet(skindoc);
		    }
		    catch(e) {
			alert("Malformed importStyle sheet:" + e.message + "\n======\nSee web browser console for more details");
			// Look for util: of java:
			if (debug) {
			    console.error("Stylesheet:\n" + skindoc.documentElement.outerHTML);
			}
		    }
		    
		    //
		    try {
			result = xsltProc.transformToDocument(xmldoc);
			xmlSer = new XMLSerializer();
			output = xmlSer.serializeToString(result);
		    }
		    catch(e) {
			alert("XSL Transform failed:" + e.message + "\n======\nSee web browser console for more details");
			console.error("doc:\n" + convertToString(xmldoc) + "\n========\nStylesheet:\n" + convertToString(skindoc));
		    }
		}
			
		var doc = document.open();
		doc.write(output);
		doc.close();
			
		document.cookie = 'supportsXSLT=true; expires=0; path=/';
		
	    }, 'text');
	}
    catch (e) {
	if(trial) {
	    notSupportedCookie();
	}
	else {
	    notSupported();
	}
    }
}

function notSupportedCookie() {
	document.cookie = 'supportsXSLT=false; expires=0; path=/';
}


// Method equivalent to PHP's in_array method
function contains(array, value) {

	for(var val in array)
		if(array[val] == value)
			return true;
	
	return false;
}

// Method equivalent to PHP's own
function print_r(arr) {
	
	var result = "";
	
	for(var a in arr) {
		result += a + " => " + arr[a] + "\r\n";
	}
	
	return result;
}

function convertToString(content) {
	try {
		// If this fails, it's another indication that the browser doesn't have the support we need
		if(typeof XMLSerializer != 'undefined') {
			return (new XMLSerializer()).serializeToString(content);
		} else {
			return content.xml;
		}		
	}
    catch (e) {
	notSupported();
    }
}

function parseFromString(content, contentType) {
    try {
	var retobj;
	
	if(typeof window.DOMParser != 'undefined') {
	    // Firefox, Chrome
	    retobj = (new DOMParser()).parseFromString(content, contentType);
	} else {
	    // IE
	    var retobj = new ActiveXObject("Microsoft.XMLDOM");
	    retobj.async = "false";
	    retobj.loadXML(content);
	}
	
	return retobj;
    }
    catch(e) {
	var obj = new ActiveXObject('MSXML.DomDocument');
	obj.async = false; obj.loadXML(content);
	return obj;
    }
}

function isSupported() {

    return true;
    // change test below to simple XML + XSL Transform
    
    /*
	// Are cookies enabled?
	if(navigator.cookieEnabled && typeof navigator.cookieEnabled != 'undefined') {	
		// Is there a cookie?
		if(document.cookie.indexOf('supportsXSLT=') > -1) {
			// Cookie exists - don't try the transformation, as the server will
			// read the cookie and determine which version to serve up.
			// If it happens to be client-side, allow transformation to proceed
			return (document.cookie.indexOf('supportsXSLT=true') > -1);
		} else {
			// Cookie doesn't exist - test!
			transform(true);
			applyText(true);
			return (document.cookie.indexOf('supportsXSLT=true') > -1);
		}
	} else {
		return false;
	}
*/
}

/* Simulating web storage for browsers that don't support it */
/* Credit: http://www.thewojogroup.com/2010/01/simulating-html5-local-storage/ */
(function(){var k=this;if(!k.localStorage&&navigator.cookieEnabled){var x="storageData_",y="++",z="::",l=function(a,c,b){var e=new Date;e.setTime(e.getTime()+b);b="; expires="+e.toGMTString();document.cookie=a+"="+c+b+"; path=/"},h=function(a){a=a+"=";for(var c=document.cookie.split(";"),b=0,e=c.length;b<e;b++){for(var d=c[b];d.charAt(0)==" ";)d=d.substring(1,d.length);if(d.indexOf(a)===0)return d.substring(a.length,d.length)}return null},m=function(a){l(a,"",-1)},i=function(){for(var a="",c=0;h(y+c)!==null;)a+=h(y+
c++);return a==""?[]:a.split(y)},n=function(a){for(var c=Math.ceil(a.length/4E3),b=0;b<c||h(y+b)!==null;){b<c?l(y+b,a.substr(b*4E3,(b+1)*4E3>a.length?a.length-b*4E3:4E3),2592E3):m(y+b);b++}},f=k.localStorage={length:0,setItem:function(a,c){var b=i(),e=0,d=b.length,g=false;for(e=0;e<d;e++){var j=b[e].split(z);if(j[0]==a){j[1]=c;g=true;b[e]=j.join(z);e=d}}if(!g){b.push(a+z+c.replace(/::/g,": :").replace(/\+\+/g,"+ +"));this.a.push(a);this.length++}n(b.join(y))},
getItem:function(a){var c=i(),b=0,e=c.length;for(b=0;b<e;b++){var d=c[b].split(z);if(d[0]==a&&d[1])return d[1]}return null},removeItem:function(a){var c=i(),b=0,e=c.length,d=false,g=[];for(b=0;b<e;b++)if(c[b].split(z)[0]!=a)g.push(c[b]);else d=true;if(d){n(g.join(y));o()}},clear:function(){for(var a=0;h(y+a)!==null;)m(y+a++);this.a=[];this.length=0},key:function(a){return a<this.length?this.a[a]:null},a:[]},o=function(){f.a=i();for(var a=0;f.a[a];)f.a[a]=f.a[a++].split(z)[0];
f.length=f.a.length};o()}})();
