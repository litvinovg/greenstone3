/*

Greenstone 3 'Client-side transformer'
Performs client-side transformations of XSLT, using HTML5 local storage (simulated if the browser doesn't support it).

Currently only supports Firefox 3 and greater, since it's the only browser (at the time of writing) that can do this properly.

@author		Steven McTainsh
@author		David Bainbridge
@date		2011-2016

*/


function isSupported()
{
    // The whole script currently assumes SaxonCE is used to provide XSLT
    // which means it is always supported => return true
    //
    // Could consider testing for native XSLT (using simple
    // hardwired XML + XSL Tranform?) and using that
    // if successful (presumably faster)
    //
    // Or even try to transform the given transform natively,
    // and store 'isNativelySupported' as true/false in cookie
    // accordingly

    return true; 
}

function notSupportedCookie() {
	document.cookie = 'supportsXSLT=false; expires=0; path=/';
}

function notSupported() {
    notSupportedCookie();

    // knock out 'client-' part from URL
    var location_href = window.location.href;

    var new_location_href = location_href.replace(/^(.*)\/(client-)?([^\?\#]+)(.*)$/,"$1/$3$4");
    console.log("Client-side XSLT not supported.  Redirecting to: " + new_location_href);
    
    window.location = new_location_href;
}

function getUrlParameterHashmap() {
    var sPageURL = decodeURIComponent(window.location.search.substring(1));
    var sURLVariables = sPageURL.split('&');

    var paramHashmap = {};
    
    for (var i = 0; i < sURLVariables.length; i++) {
        var sParameterName = sURLVariables[i].split('=');
	paramHashmap[sParameterName[0]] = sParameterName[1];
    }

    return paramHashmap;
}

// From:
//   http://stackoverflow.com/questions/298750/how-do-i-select-text-nodes-with-jquery
function getTextNodesIn(node, includeWhitespaceNodes) {
    var textNodes = [], nonWhitespaceMatcher = /\S/;

    function getTextNodes(node) {
        if (node.nodeType == 3) {
            if (includeWhitespaceNodes || nonWhitespaceMatcher.test(node.nodeValue)) {
                textNodes.push(node);
            }
        }
	else {
            for (var i = 0, len = node.childNodes.length; i < len; ++i) {
                getTextNodes(node.childNodes[i]);
            }
        }
    }

    getTextNodes(node);
    return textNodes;
}

function applyDisableEscapingToTextNodes(elem)
{
    var textNodes = getTextNodesIn(elem,false); // ignore whitespace

    for (var i=textNodes.length-1; i>=0; i--) {
	var text_node = textNodes[i];
	var text = text_node.nodeValue;
	var html = $.parseHTML(text);
	$(text_node).replaceWith(html);	
    }
}



var onSaxonLoad = function() {

    try {
	var paramHashmap = getUrlParameterHashmap();
	
	var rooturl = window.location.pathname;
	var queryStr = window.location.search.substring(1);
	queryStr = queryStr.replace(/o=.*?(&|$)/g,"");
	if (queryStr != '') {
	    queryStr += "&";
	}
	queryStr += "o=clientside";

	paramHashmap['o']="clientside";
	
	//console.log("*** rooturl = " + rooturl);
	//console.log("*** queryStr = " + queryStr);

	var skindoc = "";
	var xmldoc = "";	
	
	$.get(rooturl, paramHashmap, function(data) {

	    var toplevel_children = $(data).children().eq(0).children();
	    var skindoc = toplevel_children[0];
	    var xmldoc = toplevel_children[1];
	    	
	    var library_name = $('xsltparams>param[name=library_name]', xmldoc).text();
	    var interface_name = $('xsltparams>param[name=interface_name]', xmldoc).text();
	    var site_name = $('xsltparams>param[name=site_name]', xmldoc).text();
	    var use_client_side_xslt = $('xsltparams>param[name=use_client_side_xslt]', xmldoc).text();
	    
	    //// Convert temporarily to text here		
	    skindoc = convertToString(skindoc);
	    xmldoc = convertToString(xmldoc);
		
	    skindoc = skindoc.replace(/<xslt:stylesheet\s+/,"<xslt:stylesheet xmlns:ixsl=\"http://saxonica.com/ns/interactiveXSLT\" xmlns:js=\"http://saxonica.com/ns/globalJS\" ");
	    skindoc = skindoc.replace(/extension-element-prefixes="(.*?)"/,"extension-element-prefixes=\"$1 ixsl\"");
	    
	    skindoc = skindoc.replace(/util\:exists\(\$meta, ''\)/g, "$meta!=''"); // For now - use regex instead
	    skindoc = skindoc.replace(/util:replace\((.*?)\)/g, "replace($1)"); // 'replace()' exists in XSLT 2.0
	    skindoc = skindoc.replace(/util:storeString\(\s*'(.+?)'\s*,\s*'(.*?)'\s*\)/g, "js:storeString(string($1),string($2))");
	    skindoc = skindoc.replace(/util:getString\('(.+?)'\)/g, "js:getString(string($1))");
	    skindoc = skindoc.replace(/util:escapeNewLinesAndQuotes\(([^)]+)\)/g, "js:escapeNewLinesAndQuotes(string($1))");
	    //attr_val = attr_val.replaceAll("util:escapeNewLinesAndQuotes\\(\\s*(.+?)\\s*\\)","$1");
	    
	    skindoc = skindoc.replace(/util:getDetailFromDate\((.+?),.+?,.+?\)/g, "'getDetailFromDate $1'"); // ****

	    skindoc = skindoc.replace(/util:oidIsMatchOrParent\(([^,]+),([^)]+)\)/g,"js:oidIsMatchOrParent(string($1),string($2))");
	    skindoc = skindoc.replace(/util:hashToDepthClass\(([^)]+)\)/g,"js:hashToDepthClass(string($1))");
	    skindoc = skindoc.replace(/util:hashToSectionId\(([^)]+)\)/g,"js:hashToSectionId(string($1))");

	    skindoc = skindoc.replace(/java:.*?getNumberedItem\(([^,]+),([^)]+)\)/g,"js:getNumberedItem(string($1),string($2))"); 

	    // Convert to XML
	    xmldoc = parseFromString(xmldoc, "text/xml");			
	    skindoc = parseFromString(skindoc, "text/xml");

	    console.log("Applying client-side XSLT");	    
	    var output = '';

	    //var proc = Saxon.newXSLT20Processor(skindoc);
	    var proc = Saxon.newXSLT20Processor();
	    
	    proc.setParameter(null, 'library_name', library_name);
	    proc.setParameter(null, 'interface_name', interface_name);
	    proc.setParameter(null, 'site_name', site_name);
	    proc.setParameter(null, 'use_client_side_xslt', use_client_side_xslt);		

	    // Consider making above XSLT20Porcessor constructor take no  arguments,
	    // and specify transform through importStylesheet(), this combination
	    // of code is more consistent with other XSLT systems
	    //
	    proc.importStylesheet(skindoc);
	    
	    result = proc.transformToDocument(xmldoc);
	    //result = proc.transformToFragment(xmldoc);

	    //proc.updateHTMLDocument(xmldoc);
	    //return;

	    var excerptid = paramHashmap['excerptid'];
	    if (excerptid) {
		result = result.getElementById(excerptid);
	    }
	    
	    applyDisableEscapingToTextNodes(result);
	    xmlSer = new XMLSerializer();
	    output = xmlSer.serializeToString(result);
	    
	    if (excerptid) {
		var callback = paramHashmap['callback'];
		parent[callback](output);		
	    }
	    else {
		var doc = document.open();
		doc.write(output);
		doc.close();

		// ****
		document.cookie = 'supportsXSLT=true; expires=0; path=/';
	    }
			
	    
	}, 'xml');

    }
    catch (e) {
	alert("Error occured:" + e.message + "\n======\nSee web browser console for more details");
	notSupported();
	
    }
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


/* Simulating web storage for browsers that don't support it */
/* Credit: http://www.thewojogroup.com/2010/01/simulating-html5-local-storage/ */
(function(){var k=this;if(!k.localStorage&&navigator.cookieEnabled){var x="storageData_",y="++",z="::",l=function(a,c,b){var e=new Date;e.setTime(e.getTime()+b);b="; expires="+e.toGMTString();document.cookie=a+"="+c+b+"; path=/"},h=function(a){a=a+"=";for(var c=document.cookie.split(";"),b=0,e=c.length;b<e;b++){for(var d=c[b];d.charAt(0)==" ";)d=d.substring(1,d.length);if(d.indexOf(a)===0)return d.substring(a.length,d.length)}return null},m=function(a){l(a,"",-1)},i=function(){for(var a="",c=0;h(y+c)!==null;)a+=h(y+
c++);return a==""?[]:a.split(y)},n=function(a){for(var c=Math.ceil(a.length/4E3),b=0;b<c||h(y+b)!==null;){b<c?l(y+b,a.substr(b*4E3,(b+1)*4E3>a.length?a.length-b*4E3:4E3),2592E3):m(y+b);b++}},f=k.localStorage={length:0,setItem:function(a,c){var b=i(),e=0,d=b.length,g=false;for(e=0;e<d;e++){var j=b[e].split(z);if(j[0]==a){j[1]=c;g=true;b[e]=j.join(z);e=d}}if(!g){b.push(a+z+c.replace(/::/g,": :").replace(/\+\+/g,"+ +"));this.a.push(a);this.length++}n(b.join(y))},
getItem:function(a){var c=i(),b=0,e=c.length;for(b=0;b<e;b++){var d=c[b].split(z);if(d[0]==a&&d[1])return d[1]}return null},removeItem:function(a){var c=i(),b=0,e=c.length,d=false,g=[];for(b=0;b<e;b++)if(c[b].split(z)[0]!=a)g.push(c[b]);else d=true;if(d){n(g.join(y));o()}},clear:function(){for(var a=0;h(y+a)!==null;)m(y+a++);this.a=[];this.length=0},key:function(a){return a<this.length?this.a[a]:null},a:[]},o=function(){f.a=i();for(var a=0;f.a[a];)f.a[a]=f.a[a++].split(z)[0];
f.length=f.a.length};o()}})();
