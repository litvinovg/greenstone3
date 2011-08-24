/********************
* EXPANSION SCRIPTS *
********************/

var collapseImageURL = gs.imageURLs.collapse;
var expandImageURL = gs.imageURLs.expand;
var chapterImageURL = gs.imageURLs.chapter;
var pageImageURL = gs.imageURLs.page;

function toggleSection(sectionID)
{
	var docElem = document.getElementById("doc" + sectionID);
	var tocElem = document.getElementById("toc" + sectionID);
	
	var tocToggleElem = document.getElementById("ttoggle" + sectionID);
	var docToggleElem = document.getElementById("dtoggle" + sectionID);
	
	if(docElem.style.display == "none")
	{
		docElem.style.display = "block";
		docToggleElem.setAttribute("src", collapseImageURL);
		
		if(tocToggleElem)
		{
			tocToggleElem.setAttribute("src", collapseImageURL);
		}
		
		if(tocElem)
		{
			tocElem.style.display = "block";
		}
	}
	else
	{
		docElem.style.display = "none";
		
		//Use the page image if this is a leaf node and the chapter image if it not
		docToggleElem.setAttribute("src", expandImageURL);
		
		if(tocToggleElem)
		{
			tocToggleElem.setAttribute("src", expandImageURL);
		}
		
		if(tocElem)
		{
			tocElem.style.display = "none";
		}
	}
}

function isExpanded(sectionID)
{
	var docElem = document.getElementById("doc" + sectionID);
	if(docElem.style.display == "block")
	{
		return true;
	}
	return false;
}

function isParentOf(parent, child)
{
	if(child.indexOf(parent) != -1 && child.length > parent.length && child[parent.length] == '.')
	{
		return true;
	}
	return false;
}

function focusSection(sectionID)
{
	var tableOfContentsDiv = document.getElementById("tableOfContents");
	var images = tableOfContentsDiv.getElementsByTagName("img");
	var nodeArray = new Array();
	
	for(var i = 0; i < images.length; i++)
	{	
		nodeArray[i] = images[i].getAttribute("id").substring(7);
	}
	
	for(var j = 0; j < nodeArray.length; j++)
	{
		//If this is the node that was clicked and it is not expanded then expand it
		if(nodeArray[j] == sectionID)
		{
			if(!isExpanded(nodeArray[j]))
			{
				toggleSection(nodeArray[j]);
			}
			
			continue;
		}
		
		//If the node is a parent or child of the node that is clicked and is not expanded then expand it
		if((isParentOf(nodeArray[j], sectionID) || isParentOf(sectionID, nodeArray[j])) && !isExpanded(nodeArray[j]))
		{
			toggleSection(nodeArray[j]);
		}
		//If the node is not a parent or child and is expanded then collapse it
		else if(!(isParentOf(nodeArray[j], sectionID) || isParentOf(sectionID, nodeArray[j])) && isExpanded(nodeArray[j]))
		{
			toggleSection(nodeArray[j]);
		}
	}
}

/***********************
* HIGHLIGHTING SCRIPTS *
***********************/
function addHighlight()
{
	var spans = document.getElementsByTagName("span");
	for(var i = 0; i < spans.length; i++)
	{
		var currentSpan = spans[i];
		if(currentSpan.getAttribute("class") == "noTermHighlight")
		{
			currentSpan.setAttribute("class", "termHighlight");
		}
	}
	
	var option = document.getElementById("highlightOption");
	option.setAttribute("onclick", "removeHighlight();");
}

function removeHighlight()
{
	var spans = document.getElementsByTagName("span");
	for(var i = 0; i < spans.length; i++)
	{
		var currentSpan = spans[i];
		if(currentSpan.getAttribute("class") == "termHighlight")
		{
			currentSpan.setAttribute("class", "noTermHighlight");
		}
	}
	
	var option = document.getElementById("highlightOption");
	option.setAttribute("onclick", "addHighlight();");
}

/**************************
* REALISTIC BOOKS SCRIPTS *
**************************/

function bookInit()
{
	loadBook(); 
	hideText(); 
	showBook(); 
	swapLinkJavascript(false);
}

function hideText()
{
	var textDiv = document.getElementById("gs-document-text");
	textDiv.style.visibility = "hidden";
}

function showText()
{
	var textDiv = document.getElementById("gs-document-text");
	textDiv.style.visibility = "visible";
}

function hideBook()
{
	var bookDiv = document.getElementById("bookdiv");
	bookDiv.style.visibility = "hidden";
	bookDiv.style.height = "0px";
	
	var bookObject = document.getElementById("bookObject");
	bookObject.style.visibility = "hidden";
	bookObject.style.height = "0px";
	
	var bookEmbed = document.getElementById("bookEmbed");
	bookEmbed.style.visibility = "hidden";
	bookEmbed.style.height = "0px";
}

function showBook()
{
	var bookDiv = document.getElementById("bookdiv");
	bookDiv.style.visibility = "visible";
	bookDiv.style.height = "600px";
	
	var bookObject = document.getElementById("bookObject");
	bookObject.style.visibility = "visible";
	bookObject.style.height = "600px";
	
	var bookEmbed = document.getElementById("bookEmbed");
	bookEmbed.style.visibility = "visible";
	bookEmbed.style.height = "600px";
}

function swapLinkJavascript(rbOn)
{
	var option = document.getElementById("rbOption");
	if(rbOn)
	{
		option.setAttribute("onclick", "hideText(); showBook(); swapLinkJavascript(false);");
	}
	else
	{
		option.setAttribute("onclick", "hideBook(); showText(); swapLinkJavascript(true);");
	}
}

//Helper function to create param elements
function createParam(name, value)
{
	var param = document.createElement("PARAM");
	param.setAttribute("name", name);
	param.setAttribute("value", value);
	return param;
}

function loadBook()
{
	var doc_url = document.URL; 
	doc_url = doc_url.replace(/(&|\?)book=[a-z]+/gi,'');
	doc_url += '&book=flashxml';
	
	var img_cover = gs.collectionMetadata.httpPath + '/index/assoc/' + gs.documentMetadata.assocfilepath + '/cover.jpg';

	var flash_plug_html = ""
	flash_plug_html += '<OBJECT align="middle" classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" \n';
	flash_plug_html += '  height="600px" id="bookObject" swLiveConnect="true" \n';
	flash_plug_html += '  codebase="http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=8,0,0,0" \n';
	flash_plug_html += '  width="70%">\n';
	flash_plug_html += '    <PARAM name="allowScriptAccess" value="always" />\n';
	flash_plug_html += '    <PARAM name="movie" value="Book.swf';
	flash_plug_html += '?src_image=' + escape(img_cover);
	flash_plug_html += '&doc_url=' + escape(doc_url)
	flash_plug_html += '" />\n';
	flash_plug_html += '    <PARAM name="quality" value="high" />\n';
	flash_plug_html += '    <PARAM name="bgcolor" value="#FFFFFF" />\n';
	flash_plug_html += '    <EMBED align="middle" \n';
	flash_plug_html += '      allowScriptAccess="always" swLiveConnect="true" \n';
	flash_plug_html += '      bgcolor="#FFFFFF" height="600px" name="Book" \n';
	flash_plug_html += '      pluginspage="http://www.macromedia.com/go/getflashplayer" \n';
	flash_plug_html += '      quality="high" id="bookEmbed"\n';
	flash_plug_html += '      src="Book.swf';
	flash_plug_html += '?src_image=' + escape(img_cover);
	flash_plug_html += '&doc_url=' + escape(doc_url);
	flash_plug_html += '"\n'; 
	flash_plug_html += '      type="application/x-shockwave-flash" width="70%" />\n';
	flash_plug_html += '</OBJECT>\n';
	var flash_div = document.getElementById("bookdiv");
	flash_div.innerHTML = flash_plug_html;
}