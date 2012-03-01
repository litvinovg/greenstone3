/********************
* EXPANSION SCRIPTS *
********************/

function toggleSection(sectionID)
{
	var docElem = document.getElementById("doc" + sectionID);
	var tocElem = document.getElementById("toc" + sectionID);
	
	var tocToggleElem = document.getElementById("ttoggle" + sectionID);
	var docToggleElem = document.getElementById("dtoggle" + sectionID);
	
	if(docElem.style.display == "none")
	{
		docElem.style.display = "block";
		docToggleElem.setAttribute("src", gs.imageURLs.collapse);
		
		if(tocToggleElem)
		{
			tocToggleElem.setAttribute("src", gs.imageURLs.collapse);
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
		docToggleElem.setAttribute("src", gs.imageURLs.expand);
		
		if(tocToggleElem)
		{
			tocToggleElem.setAttribute("src", gs.imageURLs.expand);
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

function minimizeSidebar()
{
	var coverImage = document.getElementById("coverImage");
	var toc = document.getElementById("contentsArea");
	var maxLink = document.getElementById("sidebarMaximizeButton");
	var minLink = document.getElementById("sidebarMinimizeButton");
	
	if(coverImage)
	{
		coverImage.style.display = "none";
	}
	
	if(toc)
	{
		toc.style.display = "none";
	}
	
	maxLink.style.display = "block";
	minLink.style.display = "none";
}

function maximizeSidebar()
{
	var coverImage = document.getElementById("coverImage");
	var toc = document.getElementById("contentsArea");
	var maxLink = document.getElementById("sidebarMaximizeButton");
	var minLink = document.getElementById("sidebarMinimizeButton");
	
	if(coverImage)
	{
		coverImage.style.display = "block";
	}
	
	if(toc)
	{
		toc.style.display = "block";
	}
	
	maxLink.style.display = "none";
	minLink.style.display = "block";
}

/**********************
* PAGED-IMAGE SCRIPTS *
**********************/

function changePage(href)
{
	var ajax = new gs.functions.ajaxRequest();
	ajax.open("GET", href + "&excerptid=gs-document");
	ajax.onreadystatechange = function()
	{
		if(ajax.readyState == 4 && ajax.status == 200)
		{
			var contentElem = document.getElementById("gs-document");
			contentElem.innerHTML = ajax.responseText;
		}
	}
	ajax.send();
}

function changeView()
{
	var viewList = document.getElementById("viewSelection");
	var currentVal = viewList.value;
	
	var view;
	if(currentVal == "Image view")
	{
		setImageVisible(true);
		setTextVisible(false);
		view = "image";
	}
	else if(currentVal == "Text view")
	{
		setImageVisible(false);
		setTextVisible(true);
		view = "text";
	}
	else
	{
		setImageVisible(true);
		setTextVisible(true);
		view = "";
	}
	
	var ajax = gs.functions.ajaxRequest();
	ajax.open("GET", gs.xsltParams.library_name + "?a=d&view=" + view + "&c=" + gs.cgiParams.c);
	ajax.send();
}

function setImageVisible(visible)
{
	var divs = document.getElementsByTagName("DIV");
	var images = new Array();
	for (var i = 0; i < divs.length; i++)
	{
		if(divs[i].id && divs[i].id.search(/^image/) != -1)
		{
			images.push(divs[i]);
		}
	}
	
	for(var i = 0; i < images.length; i++)
	{
		var image = images[i];
		if(visible)
		{
			image.style.display = "block";
		}
		else
		{
			image.style.display = "none";
		}
	}
}

function setTextVisible(visible)
{
	var divs = document.getElementsByTagName("DIV");
	var textDivs = new Array();
	for (var i = 0; i < divs.length; i++)
	{
		if(divs[i].id && divs[i].id.search(/^text/) != -1)
		{
			textDivs.push(divs[i]);
		}
	}

	for(var i = 0; i < textDivs.length; i++)
	{
		var text = textDivs[i];
		if(visible)
		{
			text.style.display = "block";
		}
		else
		{
			text.style.display = "none";
		}
	}
}

function retrieveTableOfContents()
{
	var ajax = gs.functions.ajaxRequest();
	
	ajax.open("GET", gs.xsltParams.library_name + "?a=d&ed=1&c=" + gs.cgiParams.c + "&d=" + gs.cgiParams.d + "&excerptid=tableOfContents");
	ajax.onreadystatechange = function()
	{
		if(ajax.readyState == 4 && ajax.status == 200)
		{
			document.getElementById("contentsArea").innerHTML = document.getElementById("contentsArea").innerHTML + ajax.responseText;
			replaceLinksWithSlider();
			var loading = document.getElementById("tocLoadingImage");
			loading.parentNode.removeChild(loading);
		}
		else if(ajax.readyState == 4)
		{
			var loading = document.getElementById("tocLoadingImage");
			loading.parentNode.removeChild(loading);
		}
	}
	ajax.send();
}

function replaceLinksWithSlider()
{
	var tableOfContents = document.getElementById("tableOfContents");
	var liElems = tableOfContents.getElementsByTagName("LI");
	
	var leafSections = new Array();
	for (var i = 0; i < liElems.length; i++)
	{
		var section = liElems[i];
		var add = true;
		for(var j = 0; j < leafSections.length; j++)
		{
			if(leafSections[j] == undefined){continue;}
			
			var leaf = leafSections[j];
			if(leaf.getAttribute("id").search(section.getAttribute("id")) != -1)
			{
				add = false;
			}
			
			if(section.getAttribute("id").search(leaf.getAttribute("id")) != -1)
			{
				delete leafSections[j];
			}
		}
		
		if(add)
		{
			leafSections.push(section);
		}
	}
	
	for(var i = 0 ; i < leafSections.length; i++)
	{
		if(leafSections[i] == undefined){continue;}
		leafSections[i].style.display = "none";
		var links = leafSections[i].getElementsByTagName("A");
		var widget = new SliderWidget(links);
		leafSections[i].parentNode.insertBefore(widget.getElem(), leafSections[i]);
	}
}

function SliderWidget(_links)
{
	//****************
	//MEMBER VARIABLES
	//****************

	//The container for the widget
	var _mainDiv = document.createElement("DIV");
	_mainDiv.setAttribute("class", "ui-widget-content pageSlider");
	
	//The table of images
	var _linkTable = document.createElement("TABLE");
	_mainDiv.appendChild(_linkTable);
	_linkTable.style.width = (75 * _links.length) + "px";
	
	//The image row of the table
	var _linkRow = document.createElement("TR");
	_linkTable.appendChild(_linkRow);
	
	//The label row
	var _numberRow = document.createElement("TR");
	_linkTable.appendChild(_numberRow);

	//****************
	//PUBLIC FUNCTIONS
	//****************
	
	//Function that returns the widget element
	this.getElem = function()
	{
		return _mainDiv;
	}
	
	//*****************
	//PRIVATE FUNCTIONS
	//*****************
	
	var getImage = function(page)
	{
		var ajax = gs.functions.ajaxRequest();

		var href = page.getAttribute("href");
		var template = '';
		template += '<xsl:template match="/">';
		template +=   '<html>';
		template +=     '<img>';
		template +=       '<xsl:attribute name="src">';
		template +=         "<xsl:value-of disable-output-escaping=\"yes\" select=\"/page/pageResponse/collection/metadataList/metadata[@name = 'httpPath']\"/>";
		template +=         '<xsl:text>/index/assoc/</xsl:text>';
		template +=         "<xsl:value-of disable-output-escaping=\"yes\" select=\"/page/pageResponse/document/metadataList/metadata[@name = 'assocfilepath']\"/>";
		template +=         '<xsl:text>/</xsl:text>';
		template +=         "<xsl:value-of disable-output-escaping=\"yes\" select=\"/page/pageResponse/document/documentNode/metadataList/metadata[@name = 'Thumb']\"/>";
		template +=       '</xsl:attribute>';
		template +=     '</img>';
		template +=   '</html>';
		template += '</xsl:template>';
		ajax.open("GET", href + "&ilt=" + template.replace(" ", "%20"));
		ajax.onreadystatechange = function()
		{
			if(ajax.readyState == 4 && ajax.status == 200)
			{
				var text = ajax.responseText;
				var hrefStart = text.indexOf("src=\"") + 5;
				if(hrefStart == -1)
				{
					page.isLoading = false;
					page.noImage = true;
					page.image.setAttribute("src", gs.imageURLs.blank);
					return;
				}
				var hrefEnd = text.indexOf("\"", hrefStart);
				var href = text.substring(hrefStart, hrefEnd);
				console.log(href);
				var image = document.createElement("IMG");
				$(image).load(function()
				{
					page.link.innerHTML = "";
					page.link.appendChild(image);
					page.isLoading = false;
					page.imageLoaded = true;
				});
				$(image).error(function()
				{
					page.isLoading = false;
					page.noImage = true;
					image.setAttribute("src", gs.imageURLs.blank);
				});
				image.setAttribute("src", href);
			}
			else if (ajax.readyState == 4 && !page.failed)
			{
				page.failed = true;
				getImage(page);
			}
		}
		ajax.send();
	}
	
	var startCheckFunction = function()
	{
		var checkFunction = function()
		{
			var widgetLeft = _mainDiv.scrollLeft;
			var widgetRight = _mainDiv.clientWidth + _mainDiv.scrollLeft;

			var visiblePages = new Array();
			for(var i = 0; i < _links.length; i++)
			{
				var current = _links[i].cell;
				var currentLeft = current.offsetLeft;
				var currentRight = currentLeft + current.clientWidth;
				if(currentRight > widgetLeft && currentLeft < widgetRight)
				{
					visiblePages.push(_links[i]);
				}
			}

			for(var i = 0; i < visiblePages.length; i++)
			{
				var page = visiblePages[i];
				if(!page || page.imageLoaded || page.noImage || page.isLoading)
				{
					continue;
				}
				
				page.isLoading = true;
				getImage(page);
			}
		}
		setInterval(checkFunction, 1000);
	}
	
	//***********
	//CONSTRUCTOR
	//***********
	
	for(var i = 0; i < _links.length; i++)
	{
		var col = document.createElement("TD");
		_linkRow.appendChild(col);
		col.setAttribute("class", "pageSliderCol");
		_links[i].cell = col;
		
		var link = document.createElement("A");
		col.appendChild(link);
		_links[i].link = link;
		var href = _links[i].getAttribute("href");
		link.setAttribute("href", "javascript:changePage(\"" + href + "\");");
		
		var image = document.createElement("IMG");
		link.appendChild(image);
		image.setAttribute("src", gs.imageURLs.loading);
		_links[i].image = image;
		
		var spacer = document.createElement("TD");
		_linkRow.appendChild(spacer);
		spacer.setAttribute("class", "pageSliderSpacer");
		
		var num = document.createElement("TD");
		_numberRow.appendChild(num);
		num.innerHTML = "Page " + (i + 1);
		num.style.textAlign = "center";
		
		var spacer = document.createElement("TD");
		_numberRow.appendChild(spacer);
		spacer.setAttribute("class", "pageSliderSpacer");
	}
	
	startCheckFunction();
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
	flash_plug_html += '&doc_url=' + escape(doc_url);
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

function addEditMetadataLink(cell)
{
	var id = cell.getAttribute("id").substring(6);
	var metaTable = document.getElementById("meta" + id);

	var row = cell.parentNode;
	var newCell = document.createElement("TD");
	newCell.setAttribute("style", "font-size:0.7em; padding:0px 10px");
	
	var linkSpan = document.createElement("SPAN");
	linkSpan.setAttribute("class", "ui-state-default ui-corner-all");
	linkSpan.setAttribute("style", "padding: 2px; float:left;");
	
	var linkLabel = document.createElement("SPAN");
	linkLabel.innerHTML = "edit metadata";
	var linkIcon = document.createElement("SPAN");
	linkIcon.setAttribute("class", "ui-icon ui-icon-folder-collapsed");
	
	var uList = document.createElement("UL");
	var labelItem = document.createElement("LI");
	var iconItem = document.createElement("LI");
	uList.appendChild(iconItem);
	uList.appendChild(labelItem);
	labelItem.appendChild(linkLabel);
	iconItem.appendChild(linkIcon);
	
	uList.setAttribute("style", "outline: 0 none; margin:0px; padding:0px;");
	labelItem.setAttribute("style", "float:left; list-style:none outside none;");
	iconItem.setAttribute("style", "float:left; list-style:none outside none;");
	
	var newLink = document.createElement("A");
	newLink.setAttribute("href", "javascript:;");
	newLink.onclick = function()
	{
		if(metaTable.style.display == "none")
		{
			linkLabel.innerHTML = "hide metadata";
			linkIcon.setAttribute("class", "ui-icon ui-icon-folder-open");
			metaTable.style.display = "block";
			metaTable.metaNameField.style.display = "inline";
			metaTable.addRowButton.style.display = "inline";
		}
		else
		{
			linkLabel.innerHTML = "edit metadata";
			linkIcon.setAttribute("class", "ui-icon ui-icon-folder-collapsed");
			metaTable.style.display = "none";
			metaTable.metaNameField.style.display = "none";
			metaTable.addRowButton.style.display = "none";
		}
	}
	newLink.appendChild(uList);
	linkSpan.appendChild(newLink);
	newCell.appendChild(linkSpan);
	row.appendChild(newCell);
	
	addFunctionalityToTable(metaTable);
	metaTable.metaNameField.style.display = "none";
	metaTable.addRowButton.style.display = "none";
}

function readyPageForEditing()
{
	var textDivs = gs.functions.getElementsByClassName("sectionText");
	for(var i = 0; i < textDivs.length; i++)
	{
		de.doc.registerEditSection(textDivs[i]);
	}
	
	var editBar = document.getElementById("editBarLeft");
	var saveButton = document.createElement("BUTTON");
	saveButton.onclick = save;
	saveButton.innerHTML = "Save changes";
	saveButton.setAttribute("id", "saveButton");
	editBar.appendChild(saveButton);
	
	var visibleMetadataList = document.createElement("SELECT");
	var allOption = document.createElement("OPTION");
	allOption.innerHTML = "All";
	visibleMetadataList.appendChild(allOption);
	visibleMetadataList.setAttribute("id", "metadataSetList");
	var metadataListLabel = document.createElement("SPAN");
	metadataListLabel.setAttribute("style", "margin-left:20px;");
	metadataListLabel.innerHTML = "Visible metadata: ";
	editBar.appendChild(metadataListLabel);
	editBar.appendChild(visibleMetadataList);
	
	var statusBarDiv = document.createElement("DIV");
	editBar.appendChild(statusBarDiv);
	_statusBar = new StatusBar(statusBarDiv);
	
	var titleDivs = gs.functions.getElementsByClassName("sectionTitle");
	for(var i = 0; i < titleDivs.length; i++)
	{
		addEditMetadataLink(titleDivs[i]);
	}
	
	_baseURL = gs.xsltParams.library_name;
}