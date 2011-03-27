/*

Greenstone 3 'Client-side transformer'
Performs client-side transformations of XSLT, using HTML5 local storage (simulated if the browser doesn't support it).

Currently only supports Firefox 3 and greater, since it's the only browser (at the time of writing) that can do this properly.

@author		Steven McTainsh
@date		14/02/2011

*/

/* These URLs and file paths are fetched dynamically */
var gsweb = ""; // The file path to the Greenstone 3 web directory
var gsurl = ""; // The root URL for this Greenstone 3 installation

/* Misc. switches and paths */
var keyUrl = ''; // Used across methods to build up query string for text retrieval (client-side transformed version)
var on = true; // Set to false to disable operation

var index = 0; // Used for query array (to keep track of number of elements)
var deferredEls = new Array(); // Elements to defer text retrieval for until later
var queryArr = new Array(); // Text to query for (the text for the corresponding deferred element)

/* Methods */
// This is generally only called from in-line JS in the head (i.e. var text = getText(...);)
function getText(key, el) {
	if(localStorage.getItem(key) != null)
		return localStorage.getItem(key);
	else {
		push(deferredEls, el);
		queryArr[el] = key; // Have it queried for too
		return "";
	}
}

function processEl(el, attr, key, append, isText, theText) {	
	// Assumes all necessary text has been loaded from the servlet
	var displayText = (isText) ? theText : localStorage.getItem(key);
			
	if(el == document && attr == 'title') {
		// Handle document titles
		if(append)
			document.title += displayText;
		else
			document.title = displayText;
	} else if(attr == 'innerText') {
		// Handle element 'inner text'
		if(append)
			$(el).html($(el).html() + displayText);
		else
			$(el).html(displayText);	
	} else if(attr == 'value') {
		// Handle value attribute
		if(append)
			$(el).val($el.val() + displayText);
		else
			$(el).val(displayText);						
	} else {
		// All other cases (generic attributes)
		if(append)
			$(el).attr(attr, $(el).attr(attr) + displayText);
		else
			$(el).attr(attr, displayText);
	}		
}	

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

$(document).ready(function() {	
	if(on && isSupported()) {		
		if(placeholder) {
			// Need to prepare page placeholders
			transform(false);
		}
		else {
			// Need to place text into the page (placeholders ready)
			applyText(false);
		}
	}
});

function applyText(trial) {

	try {
	// Wait for document to be ready before propogating this URL (otherwise undefined)
	keyUrl = 'grabtext?i='+$('#interface').html()+'&l='+$('#language').html()+'&k='	
	
	// First, see if the local storage should be emptied for a new language...
	if(localStorage.getItem("_activeLanguage") != $('#language').html()) {
	   // Languages are different, clear existing strings
	   localStorage.clear();
	   localStorage.setItem("_activeLanguage", $('#language').html());
	}
	
	$('#loading').show();

	var query = '';

	$('.getTextFor').each(function() {

		// Build up query
		// Get the three parts to the class - 'getTextFor' 'element inner text to affect' 'attributes to affect'
		// Proper splitting on spaces
		var parts = parseAction($(this).attr('class'), ' ')
		// parts[1] is this element's innerText key, parts[2] may or may not contain keys
		// Can add null to the array
		if(!contains(queryArr, parts[1]) && localStorage.getItem(parts[1]) == null)
			// If the key has not already been queried for and is not in local storage...
			// NO duplicates!
			queryArr[index++] = parts[1];
		
		if(parts.length == 3 && parts[2] != '') {
			var affected = parts[2].split(',');
		
			for(var j = 0; j < affected.length; j++) {
				var els = parseAction(affected[j], ".");
				// els[2] will contain key
				// Ignore text, and those elements with text already in local storage and that will already be queried for
				if(els[2].indexOf('text:') != 0 && !contains(queryArr, els[2]) && localStorage.getItem(els[2]) == null)
					queryArr[index++] = els[2].replace("[a]", "");
			}
		}
	});
	
	query = queryArr.join(",");

	// Fetch!
	if(query != '') { // There is something to query for
	
	$.get(keyUrl + query, function(data) {

		// Process dictionary, load into local storage
		$(data).find("item").each(function() {
			localStorage.setItem($(this).attr('key'), $(this).attr('value'));
		});
		
		assignText();
	
	}, 'xml');	
	
	} else { assignText(trial); }
	} catch (e) { if(trial) notSupportedCookie(); else notSupported(); }
}

function assignText(trial) {

try {
$('.getTextFor').each(function() { 

	var parts = parseAction($(this).attr('class'), " ");
	var me = parts[1];

	// Process it's own inner text first
	processEl($(this), 'innerText', me);

	if(parts.length >= 3 && parts[2] != '') { // Otherwise 'x y ' mucks it up

		// Affecting elements are listed
		var affectees = parts[2];	

		var all = affectees.split(',');

		for(var i = 0; i < all.length; i++) {
			var parts = parseAction(all[i], ".");			
		
			var append = false;
			var isText = false;
			var theText = '';

			if(parts[2].indexOf("[a]") != -1) {
				parts[2] = parts[2].replace("[a]", "");
				append = true;
			}
			
			if(parts[2].indexOf("text:'") != -1) {
				// This action is plain text
				isText = true;
				theText = parts[2].substring(6, parts[2].length - 1);
			}

			if(parts[0] == 'parent') {
				// Affects the parent
				processEl($(this).parent(), parts[1], parts[2], append, isText, theText);
			} else if(parts[0] == 'this') {
				// Affects this element	
				processEl($(this), parts[1], parts[2], append, isText, theText);
			} else if(parts[0] == 'document') {
				// Affects document itself
				processEl(document, parts[1], parts[2], append, isText, theText);
			}

			
		}
	}	
	
	// Process deferred elements now
	for(var def in deferredEls) {
		$(def).html(localStorage[deferredEls[def]]); //dict[deferredEls[def]]);
	}

});

var xsltClientCapable = document.cookie.indexOf('supportsXSLT') != -1;

// At this point, the browser has proven itself capable of XSL transformations
// So, modify all document links into JavaScript links that use AJAX to get content
if(xsltClientCapable) {
    // Could set cookie here too?
    $('.clientDocView').each(function() {
         var link = $(this).attr('href');
         var startIndex = link.indexOf("&amp;d=");
         var endIndex = link.indexOf('&amp;', startIndex + 7);
         // If the endIndex == -1, end not found; just use end index
         endIndex = (endIndex == -1) ? link.length : endIndex;
         var docID = link.substring(startIndex, endIndex);
         docID = docID.replace("&amp;d=", "");
         $(this).attr('id', docID);
         $(this).attr('onclick', "getNodeContent(this); return false;"); // .bind doesn't work for dynamically added HTML!
         
         if(typeof initialHash != 'undefined') // Prevent errors when searching
         if(docID == initialHash) {
            // Load it!
            getNodeContent($(this).get()[0]); // Talk about round the houses! This was the only way that produced something!
         }
    });
	
	document.cookie = 'supportsXSLT=true; expires=0; path=/';
}

// Also undo escaping of necessary entities
$('body').each(function() {
	
	var text = $(this).html();
	text = text.replace(/&lt;/g, "<");
	text = text.replace(/&gt;/g, ">");
	text = text.replace(/&amp;/g, "&");
	$(this).html(text);
	
});

// Remove dead elements
$('[xmlns]').each(function() {
     if($(this).get(0).tagName.toLowerCase() != "html") {
         // use toLower() in case of browser inconsistencies
         $(this).hide();
     }
});

//alert('Language is: ' + $('#language').html() + ', interface is: ' + $('#interface').html());

$('#loading').fadeOut();

} catch (e) { if(trial) notSupportedCookie(); else notSupported(); }

}

function transform(trial) {

	try {
	var queryStr = window.location.search.substring(1);
	var rooturl = window.location;
	//var absolute = gsurl;
	var lead = queryStr == '' ? '?' : '&';
	var skinurl = rooturl+lead+"o=skinandlibdoc";
	var xmlurl = rooturl+lead+"o=xml";
	
	var skindoc = "";
	var xmldoc = "";	
	
	$.get(rooturl + lead + "o=clientside", function(data) {
		
		data = data.replace(/gs3:id/g, "gs3id");
		data = parseFromString(data, "text/xml");
		
		// data contains the XML and the stylesheet too!
		var skindoc = $(data).find("xslt\\:stylesheet")[0];
		var xmldoc = $(data).find("page")[0];
		
		gsurl = $($(xmldoc).find("metadata[name=siteURL]")[0]).text();
		gsweb = new RegExp($($(xmldoc).find("param[name=filepath]")[0]).text().replace(/\\/g, "\\\\"), "g");
		
		// Find xsl:include elements and update hrefs accordingly
		$(skindoc).find("xsl\\:include").each(function() {
			$(this).attr('href', $(this).attr('href').replace(gsweb, gsurl).replace(/\\/g, "/"));
		});
		
		$(xmldoc).find("xsl\\:include").each(function() {
			$(this).attr('href', $(this).attr('href').replace(gsweb, gsurl).replace(/\\/g, "/"));
		});
		
		// Convert temporarily to text here		
		skindoc = convertToString(skindoc);
		xmldoc = convertToString(xmldoc);
		
		// This could just be done on the server (later)
		//data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" /*+ "<?xml-stylesheet type=\"text/xsl\" href=\"" + skinurl + "\"?>\r\n"*/ + data;
		// replace all!
		// Be careful with regex syntax and the use of special characters!
		skindoc = skindoc.replace(/util\:exists\(\$meta, ''\)/g, "$meta!=''"); // For now - use regex instead
		skindoc = skindoc.replace(/%3A/g, ":"); // undo colon escaping
		
		// Convert to XML
		xmldoc = parseFromString(xmldoc, "text/xml");			
			skindoc = parseFromString(skindoc, "text/xml");
			
			var output = '';
			var library_name = $('/page/xsltparams/param[name=library_name]', xmldoc).text();
			var interface_name = $('/page/xsltparams/param[name=interface_name]', xmldoc).text();
			
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
			} else {			
				// Fx
				xsltProc = new XSLTProcessor();
				// The leading slash is oh-so important here
				xsltProc.setParameter(null, 'library_name', library_name);
				xsltProc.setParameter(null, 'interface_name', interface_name);
				xsltProc.importStylesheet(skindoc);
				result = xsltProc.transformToDocument(xmldoc);
				output = (new XMLSerializer()).serializeToString(result);
			}
			
			//output = output.replace('var placeholder = false;', 'var placeholder = false; var xsltClientCapable = true;');
			
			var doc = document.open();
			doc.write(output);
			doc.close();
			
			document.cookie = 'supportsXSLT=true; expires=0; path=/';
		
	}, 'html');
	} catch (e) { if(trial) notSupportedCookie(); else notSupported(); }
}

function notSupportedCookie() {
	document.cookie = 'supportsXSLT=false; expires=0; path=/';
}

function getNodeContent(node) {
    var hash = $(node).attr('id');
	
	// Some document titles appear within spans, some do not, so these adjustments are necessary
	$('.clientDocView').next().css('font-weight', 'normal');
	$('.clientDocView').parent().css('font-weight', 'normal');
	$(node).parent().css('font-weight', 'bold');
	$(node).next().css('font-weight', 'bold');
	
	var loadingimg = '<img src="interfaces/default/images/loading.gif" alt="Loading" />';
	
	// Fetch me some fresh XML
	$('.documenttext').html(loadingimg);
	var url = $(node).attr('href') + "&o=xml";
	url = url.replace('&amp;', '&');
	$.get(url, function(data) {
	
	   gsurl = $($(data).find("metadata[name=siteURL]")[0]).text();
	
	   $.get(gsurl + '/interfaces/default/transform/document_text.xsl', function(text) {
	   
	   var output = '';
	   var result = '';
	
	   try {
	       // FF only
	       xsltProc = new XSLTProcessor();
			// The leading slash is oh-so important here
			xsltProc.setParameter(null, 'library_name', $('/page/xsltparams/param[name=library_name]', data).text());
			xsltProc.setParameter(null, 'interface_name', $('/page/xsltparams/param[name=interface_name]', data).text());
			xsltProc.importStylesheet(text);
			result = xsltProc.transformToDocument(data);
			output = (new XMLSerializer()).serializeToString(result);
	   } catch(e) { window.location = $(node).attr('href'); } // will naturally cascade to client-side, then if necessary, server-side
	   
	       // Get the book name
	       var book = $('#documentheading').text();
	       var title_arr = new Array();
	       var temp_arr = new Array();
	       
	       title_arr.push(book);
	       
	       // Go up the tree, until you reach the 'Table of Contents' node
	       $(node).parentsUntil('#tocstart').each(function() {
	           if($(this).get()[0].tagName == 'LI') {
	               // Only list elements...
	               // next child = a, next sibling = text node
	               temp_arr.push($(this).children('span').text());
	           }
	       });
	       
	       // Store these temporarily to reverse them and get it right
	       temp_arr = temp_arr.reverse();
	       title_arr = title_arr.concat(temp_arr);
	       document.title = title_arr.join('::');
	   
	       // Fix up entity encoding first
           output = output.replace(/&amp;/g, '&');
           output = output.replace(/&lt;/g, '<');
           output = output.replace(/&gt;/g, '>');
	   	   $('.documenttext').html(output);
	   
	   }, 'xml');
	   
	}, 'xml');
}

function parseAction(str, chr) {

	// Problem with split - seems to discard the rest of string if it encounters splitable characters but doesn't split on them
	// But, then all the split characters are missing from the array! So, need to slice the substring out.
	// Get additional text
	var parts = str.split(chr, 3);				

	var trimIndex = parts[0].length + parts[1].length + 2;
	parts[2] = str.substring(trimIndex);
	
	return parts;

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
	} catch (e) { notSupported(); }
}

function parseFromString(content, contentType) {
	 try {
		var retobj;
		
		if(typeof window.DOMParser != 'undefined') {
			// Fx
			retobj = (new DOMParser()).parseFromString(content, contentType);
		} else {
			// IE
			var retobj = new ActiveXObject("Microsoft.XMLDOM");
			retobj.async = "false";
			retobj.loadXML(content);
		}
		
	 	return retobj;
	 } catch(e) { var obj = new ActiveXObject('MSXML.DomDocument'); obj.async = false; obj.loadXML(content); return obj; }
}

function isSupported() {
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
}

/* Simulating web storage for browsers that don't support it */
/* Credit: http://www.thewojogroup.com/2010/01/simulating-html5-local-storage/ */
(function(){var k=this;if(!k.localStorage&&navigator.cookieEnabled){var x="storageData_",y="++",z="::",l=function(a,c,b){var e=new Date;e.setTime(e.getTime()+b);b="; expires="+e.toGMTString();document.cookie=a+"="+c+b+"; path=/"},h=function(a){a=a+"=";for(var c=document.cookie.split(";"),b=0,e=c.length;b<e;b++){for(var d=c[b];d.charAt(0)==" ";)d=d.substring(1,d.length);if(d.indexOf(a)===0)return d.substring(a.length,d.length)}return null},m=function(a){l(a,"",-1)},i=function(){for(var a="",c=0;h(y+c)!==null;)a+=h(y+
c++);return a==""?[]:a.split(y)},n=function(a){for(var c=Math.ceil(a.length/4E3),b=0;b<c||h(y+b)!==null;){b<c?l(y+b,a.substr(b*4E3,(b+1)*4E3>a.length?a.length-b*4E3:4E3),2592E3):m(y+b);b++}},f=k.localStorage={length:0,setItem:function(a,c){var b=i(),e=0,d=b.length,g=false;for(e=0;e<d;e++){var j=b[e].split(z);if(j[0]==a){j[1]=c;g=true;b[e]=j.join(z);e=d}}if(!g){b.push(a+z+c.replace(/::/g,": :").replace(/\+\+/g,"+ +"));this.a.push(a);this.length++}n(b.join(y))},
getItem:function(a){var c=i(),b=0,e=c.length;for(b=0;b<e;b++){var d=c[b].split(z);if(d[0]==a&&d[1])return d[1]}return null},removeItem:function(a){var c=i(),b=0,e=c.length,d=false,g=[];for(b=0;b<e;b++)if(c[b].split(z)[0]!=a)g.push(c[b]);else d=true;if(d){n(g.join(y));o()}},clear:function(){for(var a=0;h(y+a)!==null;)m(y+a++);this.a=[];this.length=0},key:function(a){return a<this.length?this.a[a]:null},a:[]},o=function(){f.a=i();for(var a=0;f.a[a];)f.a[a]=f.a[a++].split(z)[0];
f.length=f.a.length};o()}})();