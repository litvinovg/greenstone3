var _imageZoomEnabled = false;
var _linkCellMap = new Array();
var _onCells = new Array();

/********************
* EXPANSION SCRIPTS *
********************/

function getTextForSection(sectionID, callback)
{
	if(!callback)
	{
		console.log("Cannot get text as the callback function is not defined");
	}

	var template = "";
	template += '<xsl:template match="/">';
	template +=   '<text>';
	template +=     '<xsl:for-each select="/page/pageResponse/document//documentNode[@nodeID = \'' + sectionID + '\']">';
	template +=       '<xsl:call-template name="sectionContent"/>';
	template +=     '</xsl:for-each>';
	template +=   '</text>';
	template += '</xsl:template>';

	var hlCheckBox = document.getElementById("highlightOption");
	
	var hl = "";
	if(hlCheckBox)
	{
		if(hlCheckBox.checked)
		{
			hl = "on";
		}
		else
		{
			hl = "off";
		}
	}
	
	var ajax = gs.functions.ajaxRequest();
	ajax.open("GET", gs.xsltParams.library_name + "/collection/" + gs.cgiParams.c + "/document/" + sectionID + "?hl=" + hl + "&p.s=TextQuery&ilt=" + template.replace(" ", "%20"), true);
	ajax.onreadystatechange = function()
	{
		if(ajax.readyState == 4 && ajax.status == 200)
		{
			var response = ajax.responseText;
			
			if(response)
			{
				var textStart = response.indexOf(">", response.indexOf(">") + 1) + 1;
				var textEnd = response.lastIndexOf("<");
				
				if(textStart == 0 || textEnd == -1 || textEnd <= textStart)
				{
					callback("");
				}
				
				var text = response.substring(textStart, textEnd);
				callback(text);
			}
			else
			{
				callback(null);
			}
		}
		else if(ajax.readyState == 4)
		{
			callback(null);
		}
	}
	ajax.send();
}

function getSubSectionsForSection(sectionID, callback)
{
	if(!callback)
	{
		console.log("Cannot get sub sections as the callback function is not defined");
	}

	var template = "";
	template += '<xsl:template match="/">';
	template +=   '<sections>';
	template +=     '<xsl:for-each select="/page/pageResponse/document//documentNode[@nodeID = \'' + sectionID + '\']/documentNode">';
	template +=       '<xsl:call-template name="wrapDocumentNodes"/>';
	template +=     '</xsl:for-each>';
	template +=   '</sections>';
	template += '</xsl:template>';

	var ajax = gs.functions.ajaxRequest();
	var url = gs.xsltParams.library_name + "/collection/" + gs.cgiParams.c + "/document/" + sectionID + "?ilt=" + template.replace(" ", "%20");

	if(gs.documentMetadata.docType == "paged")
	{
		url += "&dt=hierarchy";
	}
	ajax.open("GET", url, true);
	ajax.onreadystatechange = function()
	{
		if(ajax.readyState == 4 && ajax.status == 200)
		{
			var response = ajax.responseText;
			
			if(response)
			{
				var sectionsStart = response.indexOf(">", response.indexOf(">") + 1) + 1;
				var sectionsEnd = response.lastIndexOf("<");
				
				if(sectionsStart == 0 || sectionsEnd == -1 || sectionsEnd <= sectionsStart)
				{
					callback(" ");
					return;
				}
				
				var sections = response.substring(sectionsStart, sectionsEnd);
				callback(sections);
			}
			else
			{
				callback(null);
			}
		}
		else if(ajax.readyState == 4)
		{
			callback(null);
		}
	}
	ajax.send();
}

function toggleSection(sectionID, callback, tocDisabled)
{
	var docElem = document.getElementById("doc" + sectionID);
	var tocElem = document.getElementById("toc" + sectionID);
	
	var tocToggleElem = document.getElementById("ttoggle" + sectionID);
	var docToggleElem = document.getElementById("dtoggle" + sectionID);
	
	if(docElem.style.display == "none")
	{
		if(tocToggleElem && !tocDisabled)
		{
			tocToggleElem.setAttribute("src", gs.imageURLs.collapse);
		}
		
		if(tocElem && !tocDisabled)
		{
			tocElem.style.display = "block";
		}
		
		if(gs.functions.hasClass(docElem, "noText"))
		{
			getTextForSection(sectionID, function(text)
			{
				if(text)
				{	
					var nodeID = sectionID.replace(/\./g, "_");
					if(text.search("wrap" + nodeID) != -1)
					{
						document.getElementById("zoomOptions").style.display = null;
						document.getElementById("pagedImageOptions").style.display = null;
					}
					getSubSectionsForSection(sectionID, function(sections)
					{					
						if(sections)
						{
							var textElem = document.getElementById("doc" + sectionID);
							$(textElem).html(text + sections);
							
							docElem.setAttribute("class", docElem.getAttribute("class").replace(/\bnoText\b/g, ""));
							docElem.style.display = "block";
							docToggleElem.setAttribute("src", gs.imageURLs.collapse);
							
							if(callback)
							{
								callback(true);
							}
							
							if(document.getElementById("viewSelection"))
							{
								changeView();
							}
						}
						else
						{
							docToggleElem.setAttribute("src", gs.imageURLs.expand);
							if(callback)
							{
								callback(false);
							}
						}
					});
				}
				else
				{
					docToggleElem.setAttribute("src", gs.imageURLs.expand);
					if(callback)
					{
						callback(false);
					}
				}
			});
		
			docToggleElem.setAttribute("src", gs.imageURLs.loading);
		}
		else
		{
			docToggleElem.setAttribute("src", gs.imageURLs.collapse);
			docElem.style.display = "block";
			
			if(callback)
			{
				callback(true);
			}
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
		
		if(callback)
		{
			callback(true);
		}
	}
}

function scrollToTop()
{
	$('html, body').stop().animate({scrollTop: 0}, 1000);
}

function focusSection(sectionID, level, tocDisabled)
{
	if(!level)
	{
		level = 0;
	}

	var parts = sectionID.split(".");
	if(level >= parts.length)
	{
		var topVal = $(document.getElementById("doc" + sectionID)).offset().top - 50;
		$('html, body').stop().animate({scrollTop: topVal}, 1000);
		return;
	}
	
	var idToExpand = "";
	for(var i = 0; i < level + 1; i++)
	{
		if(i > 0)
		{
			idToExpand += ".";
		}
	
		idToExpand += parts[i];
	}
	
	if(!isExpanded(idToExpand))
	{
		toggleSection(idToExpand, function(success)
		{
			if(success)
			{
				focusSection(sectionID, level + 1, tocDisabled);
			}
		}, tocDisabled);
	}
	else
	{
		focusSection(sectionID, level + 1, tocDisabled);
	}
}

function expandOrCollapseAll(expand)
{
	var divs = document.getElementsByTagName("DIV");
	var startCounter = 0;
	var endCounter = 0;
	
	for(var i = 0; i < divs.length; i++)
	{
		if(divs[i].getAttribute("id") && divs[i].getAttribute("id").search(/^doc/) != -1)
		{
			var id = divs[i].getAttribute("id").replace(/^doc(.*)/, "$1");
			if(isExpanded(id) != expand)
			{
				//Don't collapse the top level
				if(!expand && id.indexOf(".") == -1)
				{
					continue;
				}
				startCounter++;

				var toggleFunction = function(tid)
				{
					toggleSection(tid, function(success)
					{
						if(success)
						{
							endCounter++;
						}
						else
						{
							setTimeout(function(){toggleFunction(tid)}, 500);
						}
					});
				}
				toggleFunction(id);
			}
		}
	}
	
	if(startCounter != 0)
	{
		var checkFunction = function()
		{
			if(startCounter == endCounter)
			{
				expandOrCollapseAll(expand);
			}
			else
			{
				setTimeout(checkFunction, 500);
			}
		}
		checkFunction();
	}
}

function loadTopLevelPage(callbackFunction, customURL)
{
	var ajax = gs.functions.ajaxRequest();

	var url = gs.xsltParams.library_name + "?a=d&c=" + gs.cgiParams.c + "&excerptid=gs-document";
	if(gs.cgiParams.d && gs.cgiParams.d.length > 0)
	{
		url += "&d=" + gs.cgiParams.d.replace(/([^.]*)\..*/, "$1");
	}
	else if(gs.cgiParams.href && gs.cgiParams.href.length > 0)
	{
		url += "&d=&alb=1&rl=1&href=" + gs.cgiParams.href;
	}

	if(customURL != null)
	{
		ajax.open("GET", customURL, true);
	}
	else
	{
		ajax.open("GET", url, true);
	}

	ajax.onreadystatechange = function()
	{
		if(ajax.readyState == 4 && ajax.status == 200)
		{
			var response = ajax.responseText;
			
			if(response)
			{
				var targetElem = document.getElementById("gs-document");
				var docStart = response.indexOf(">") + 1;
				var docEnd = response.lastIndexOf("<");
				var doc = response.substring(docStart, docEnd);

				targetElem.innerHTML = doc;
				
				if(callbackFunction)
				{
					callbackFunction();
				}
			}
		}
		else if(ajax.readyState == 4)
		{
			setTimeout(function(){loadTopLevelPage(callbackFunction, customURL);}, 1000);
		}
	};
	ajax.send();
}

function retrieveFullTableOfContents()
{
	var ajax = gs.functions.ajaxRequest();
	
	var url = gs.xsltParams.library_name + "/collection/" + gs.cgiParams.c + "?excerptid=tableOfContents&ed=1";
	if(gs.cgiParams.d && gs.cgiParams.d.length > 0)
	{
		url += "&a=d&d=" + gs.cgiParams.d;
	}
	else if(gs.cgiParams.href && gs.cgiParams.href.length > 0)
	{
		url += "&a=d&d=&alb=1&rl=1&href=" + gs.cgiParams.href;
	}
	
	ajax.open("GET", url, true);
	ajax.onreadystatechange = function()
	{
		if(ajax.readyState == 4 && ajax.status == 200)
		{
			var newTOCElem = ajax.responseText;
			var tocStart = newTOCElem.indexOf(">") + 1;
			var tocEnd = newTOCElem.lastIndexOf("<");
			
			var newTOC = newTOCElem.substring(tocStart, tocEnd);
			
			//Add the "Expand document"/"Collapse document" links
			newTOC = "<table style=\"width:100%; text-align:center;\"><tr><td><a href=\"javascript:expandOrCollapseAll(true);\">Expand document</a></td><td><a href=\"javascript:expandOrCollapseAll(false);\">Collapse document</a></td></tr></table>" + newTOC;
			
			//Collapse the TOC
			newTOC = newTOC.replace(/display:block/g, "display:none");
			newTOC = newTOC.replace(/display:none/, "display:block");
			newTOC = newTOC.replace(/images\/collapse/g, "images/expand");
			
			var tocElem = document.getElementById("tableOfContents");
			tocElem.innerHTML = newTOC;
			
			gs.variables.tocLoaded = true;
		}
		else if(ajax.readyState == 4)
		{
			setTimeout(retrieveFullTableOfContents, 1000);
		}
	}
	ajax.send();
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
	var toc = document.getElementById("contentsArea");
	var maxLink = document.getElementById("sidebarMaximizeButton");
	var minLink = document.getElementById("sidebarMinimizeButton");
	
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

function retrieveTableOfContentsAndTitles()
{
	var ilt = "";
	ilt += '<xsl:template match="/">';
	ilt +=   '<xsl:for-each select="/page/pageResponse/document/documentNode">';
	ilt +=     '<xsl:call-template name="documentNodeTOC"/>';
	ilt +=   '</xsl:for-each>';
	ilt += '</xsl:template>';

	var ajax = gs.functions.ajaxRequest();
	ajax.open("GET", gs.xsltParams.library_name + "?a=d&ed=1&c=" + gs.cgiParams.c + "&d=" + gs.cgiParams.d + "&ilt=" + ilt.replace(/ /g, "%20"), true);
	ajax.onreadystatechange = function()
	{
		if(ajax.readyState == 4 && ajax.status == 200)
		{
			document.getElementById("tableOfContents").innerHTML = ajax.responseText;
			replaceLinksWithSlider();
			var loading = document.getElementById("tocLoadingImage");
			loading.parentNode.removeChild(loading);
		}
		else if(ajax.readyState == 4)
		{
			setTimeout(function(){retrieveTableOfContentsAndTitles();}, 1000);
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
	
	//Disable all TOC toggles
	var imgs = document.getElementsByTagName("IMG");
	for(var j = 0; j < imgs.length; j++)
	{
		var currentImage = imgs[j];
		if(currentImage.getAttribute("id") && currentImage.getAttribute("id").search(/^ttoggle/) != -1)
		{
			currentImage.setAttribute("onclick", "");
		}
		else if(currentImage.getAttribute("id") && currentImage.getAttribute("id").search(/^dtoggle/) != -1)
		{
			currentImage.setAttribute("onclick", currentImage.getAttribute("onclick").replace(/\)/, ", null, true)"));
		}
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
	
	//The image row of the table
	var _linkRow = document.createElement("TR");
	_linkTable.appendChild(_linkRow);
	
	//The list of titles we can search through
	var _titles = new Array();

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
	
	var setUpFilterBox = function()
	{
		var filter = $("#filterText");
		filter.keyup(function()
		{
			var currentValue = filter.val();
			var isRange = (currentValue.search(/\d+-\d+/) != -1)
			
			var found = false;
			for(var i = 0; i < _titles.length; i++)
			{
				if(_titles[i][0] == currentValue)
				{
					found = true;
				}
			}
			
			if(!found && isRange)
			{
				var firstNumber = currentValue.replace(/^(\d+)-\d+$/, "$1");
				var secondNumber = currentValue.replace(/^\d+-(\d+)$/, "$1");
				
				if(firstNumber <= secondNumber)
				{
					var matchingTitles = new Array();
					for(var i = firstNumber; i <= secondNumber; i++)
					{
						var numString = i + "";
						for(var j = 0; j < _titles.length; j++)
						{
							var currentTitle = _titles[j];
							if(currentTitle[0].search(numString) != -1)
							{
								matchingTitles.push(currentTitle);
							}
						}
					}
					
					for(var i = 0; i < _titles.length; i++)
					{
						_titles[i][1].cell.style.display = "none";
					}
					
					for(var i = 0; i < matchingTitles.length; i++)
					{
						matchingTitles[i][1].cell.style.display = "table-cell";
					}
				}
			}
			else
			{
				for(var i = 0; i < _titles.length; i++)
				{
					var currentTitle = _titles[i];
					if(currentTitle[0].search(currentValue.replace(/\./g, "\\.")) != -1)
					{
						currentTitle[1].cell.style.display = "table-cell";
					}
					else
					{
						currentTitle[1].cell.style.display = "none";
					}
				}
			}
		});
	}
	
	var getImage = function(page, attemptNumber)
	{
		var ajax = gs.functions.ajaxRequest();

		var href = page.getAttribute("href");
		var startHREF = href.indexOf("'") + 1;
		var endHREF = href.indexOf("'", startHREF);
		var nodeID = href.substring(startHREF, endHREF);
		href = gs.xsltParams.library_name + "/collection/" + gs.cgiParams.c + "/document/" + nodeID;
		
		var template = '';
		template += '<xsl:template match="/">';
		template +=   '<gsf:metadata name=\"Thumb\"/>';
		template +=   '<html>';
		template +=     '<img>';
		template +=       '<xsl:attribute name="src">';
		template +=         "<xsl:value-of disable-output-escaping=\"yes\" select=\"/page/pageResponse/collection/metadataList/metadata[@name = 'httpPath']\"/>";
		template +=         '<xsl:text>/index/assoc/</xsl:text>';
		template +=         "<xsl:value-of disable-output-escaping=\"yes\" select=\"/page/pageResponse/document/metadataList/metadata[@name = 'assocfilepath']\"/>";
		template +=         '<xsl:text>/</xsl:text>';
		template +=         "<xsl:value-of disable-output-escaping=\"yes\" select=\"/page/pageResponse/document//documentNode[@nodeID = '" + nodeID + "']/metadataList/metadata[@name = 'Thumb']\"/>";
		template +=       '</xsl:attribute>';
		template +=     '</img>';
		template +=     '<p>';
		template +=       "<xsl:value-of disable-output-escaping=\"yes\" select=\"/page/pageResponse/document/documentNode/metadataList/metadata[@name = 'Title']\"/>";
		template +=     '</p>';
		template +=   '</html>';
		template += '</xsl:template>';

		ajax.open("GET", href + "?ilt=" + template.replace(" ", "%20"));
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
					if(!attemptNumber || attemptNumber < 3)
					{
						setTimeout(function(){getImage(page, ((!attemptNumber) ? 1 : attemptNumber + 1));}, 500);
					}
					else
					{
						page.isLoading = false;
						page.noImage = true;
						image.setAttribute("src", gs.imageURLs.blank);
					}
				});
				image.setAttribute("src", href);
				
				var titleStart = text.indexOf("<p>") + 3;
				var titleEnd = text.indexOf("</p>");
				var title = text.substring(titleStart, titleEnd);
			}
			else if (ajax.readyState == 4)
			{
				page.failed = true;
				if(!attemptNumber || attemptNumber < 3)
				{
					setTimeout(function(){getImage(page, ((!attemptNumber) ? 1 : attemptNumber + 1));}, 500);
				}
				else
				{
					var image = document.createElement("IMG");
					image.setAttribute("src", gs.imageURLs.blank);
					page.link.innerHTML = "";
					page.link.appendChild(image);
					page.isLoading = false;
					page.noImage = true;
				}
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
		link.setAttribute("href", href.replace(/\)/, ", 0, true)"));
		
		if(!_linkCellMap[href])
		{
			_linkCellMap[href] = new Array();
		}
		_linkCellMap[href].push(_links[i]);
		
		var image = document.createElement("IMG");
		link.appendChild(image);
		image.setAttribute("src", gs.imageURLs.loading);
		_links[i].image = image;
		
		var title = _links[i].innerHTML;
		if(title.search(/^[^ ]+ [^ ]+$/) != -1)
		{
			var section = title.replace(/^([^ ]+) [^ ]+$/, "$1");
			var page = title.replace(/^[^ ]+ ([^ ]+)$/, "$1");
			if(page.search(/^[0-9]+$/) != -1)
			{
				title = page;
			}
		}
		_titles.push([title, _links[i]]);
		
		var text = document.createTextNode(title);
		col.appendChild(text);
	}
	
	setUpFilterBox();
	startCheckFunction();
}

/***********************
* HIGHLIGHTING SCRIPTS *
***********************/
function swapHighlight()
{
	var hlCheckbox = document.getElementById("highlightOption");
	
	var from;
	var to;
	if(hlCheckbox.checked)
	{
		from = "noTermHighlight";
		to = "termHighlight";
	}
	else
	{
		from = "termHighlight";
		to = "noTermHighlight";
	}
	
	var spans = document.getElementsByTagName("span");
	for(var i = 0; i < spans.length; i++)
	{
		var currentSpan = spans[i];
		if(currentSpan.getAttribute("class") == from)
		{
			currentSpan.setAttribute("class", to);
		}
	}
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
	newCell.setAttribute("class", "editMetadataButton");
	
	var linkSpan = document.createElement("SPAN");
	linkSpan.setAttribute("class", "ui-state-default ui-corner-all");
	linkSpan.setAttribute("style", "padding: 2px; float:left;");
	
	var linkLabel = document.createElement("SPAN");
	linkLabel.innerHTML = "edit metadata";
	newCell.linkLabel = linkLabel;
	var linkIcon = document.createElement("SPAN");
	linkIcon.setAttribute("class", "ui-icon ui-icon-folder-collapsed");
	newCell.linkIcon = linkIcon;
	
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

function setEditingFeaturesVisible(visible)
{
	if(visible)
	{
		document.getElementById("editContentButton").innerHTML = "Hide editor";
	}
	else
	{
		document.getElementById("editContentButton").innerHTML = "Edit content";
	}

	var saveButton = document.getElementById("saveButton");
	var metadataListLabel = document.getElementById("metadataListLabel");
	var metadataList = document.getElementById("metadataSetList");
	
	var visibility = (visible ? "" : "none");
	saveButton.style.display = visibility;
	metadataListLabel.style.display = visibility;
	metadataList.style.display = visibility;
	
	var buttons = gs.functions.getElementsByClassName("editMetadataButton");
	
	for(var i = 0; i < buttons.length; i++)
	{
		buttons[i].style.display = visibility;
		buttons[i].linkLabel.innerHTML = "edit metadata";
		buttons[i].linkIcon.setAttribute("class", "ui-icon ui-icon-folder-collapsed");
	}
	
	var tables = document.getElementsByTagName("TABLE");
	for(var i = 0; i < tables.length; i++)
	{
		var currentTable = tables[i];
		if(currentTable.getAttribute("id") && currentTable.getAttribute("id").search(/^meta/) != -1)
		{
			currentTable.style.display = "none";
			currentTable.metaNameField.style.display = "none";
			currentTable.addRowButton.style.display = "none";
		}
	}
}

function readyPageForEditing()
{
	if(document.getElementById("metadataSetList"))
	{
		var setList = document.getElementById("metadataSetList");
		if(!setList.style.display || setList.style.display == "")
		{
			setEditingFeaturesVisible(false);
		}
		else
		{
			setEditingFeaturesVisible(true);
		}
		return;
	}

	document.getElementById("editContentButton").innerHTML = "Hide Editor";
	
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
	metadataListLabel.setAttribute("id", "metadataListLabel");
	metadataListLabel.setAttribute("style", "margin-left:20px;");
	metadataListLabel.innerHTML = "Visible metadata: ";
	editBar.appendChild(metadataListLabel);
	editBar.appendChild(visibleMetadataList);
	visibleMetadataList.onchange = onVisibleMetadataSetChange;
	
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

function floatMenu(enabled)
{
	var menu = $(".tableOfContentsContainer");
	if(enabled)
	{
		menu.data("position", menu.css("position"));
		menu.data("width", menu.css("width"));
		menu.data("right", menu.css("right"));
		menu.data("top", menu.css("top"));
		menu.data("max-height", menu.css("max-height"));
		menu.data("overflow", menu.css("overflow"));
		menu.data("z-index", menu.css("z-index"));
		
		menu.css("position", "fixed");
		menu.css("width", "300px");
		menu.css("right", "0px");
		menu.css("top", "100px");
		menu.css("max-height", "600px");
		menu.css("overflow", "auto");
		menu.css("z-index", "200");
		
		$("#unfloatTOCButton").show();
	}
	else
	{
		menu.css("position", menu.data("position"));
		menu.css("width", menu.data("width"));
		menu.css("right", menu.data("right"));
		menu.css("top", menu.data("top"));
		menu.css("max-height", menu.data("max-height"));
		menu.css("overflow", menu.data("overflow"));
		menu.css("z-index", menu.data("z-index"));
		
		$("#unfloatTOCButton").hide();
		$("#floatTOCToggle").attr("checked", false);
	}
}

function showSlideShow()
{
	var visible = $("#ssOption").attr('checked');
	if(visible)
	{
		$("#gs-document").hide();
		if(!($("#gs-slideshow").length))
		{
			var slideshowDiv = $("<div>", {id:"gs-slideshow"});
			var loadingImage = $("<img>", {src:gs.imageURLs.loading});
			slideshowDiv.append(loadingImage);
			
			$("#gs-document").after(slideshowDiv);
			
			retrieveImagesForSlideShow(function(imageIDArray)
			{
				loadingImage.hide();
				if(imageIDArray && imageIDArray.length > 0)
				{
					var imageURLs = new Array();
					for(var i = 0; i < imageIDArray.length; i++)
					{
						if(imageIDArray[i].source && imageIDArray[i].source.search(/.*\.(gif|jpg|jpeg|png)$/) != -1)
						{
							imageURLs.push(gs.collectionMetadata.httpPath + "/index/assoc/" + gs.documentMetadata.assocfilepath + "/" + imageIDArray[i].source);
						}
					}
					new SlideShowWidget(slideshowDiv, imageURLs, imageIDArray);
				}
			});
		}
		else
		{
			$("#gs-slideshow").show();
		}
	}
	else
	{
		if($("#gs-slideshow").length)
		{
			$("#gs-slideshow").hide();
		}
		$("#gs-document").show();
	}
}

function retrieveImagesForSlideShow(callback)
{
	var template = "";
	template += '<xsl:template match="/">';
	template +=   '<images>[';
	template +=     '<xsl:for-each select="//documentNode">';
	template +=       '<xsl:text disable-output-escaping="yes">{"source":"</xsl:text><gsf:metadata name="Source"/><xsl:text disable-output-escaping="yes">",</xsl:text>';
	template +=       '<xsl:text disable-output-escaping="yes">"id":"</xsl:text><xsl:value-of select="@nodeID"/><xsl:text disable-output-escaping="yes">"}</xsl:text>';
	template +=       '<xsl:if test="position() != count(//documentNode)">,</xsl:if>';
	template +=     '</xsl:for-each>';
	template +=   ']</images>';
	template += '</xsl:template>';

	var url = gs.xsltParams.library_name + "/collection/" + gs.cgiParams.c + "/document/" + gs.cgiParams.d + "?ed=1&ilt=" + template.replace(" ", "%20");

	$.ajax(
	{
		url:url,
		success: function(data)
		{
			var startIndex = data.indexOf(">", data.indexOf(">") + 1) + 1;
			var endIndex = data.lastIndexOf("<");
			var arrayString = data.substring(startIndex, endIndex);
			var imageIDArray = eval(arrayString);

			callback(imageIDArray);
		}
	});
}

function SlideShowWidget(mainDiv, images, idArray)
{
	var _inTransition = false;
	var _images = new Array();
	var _mainDiv = mainDiv;
	var _imageDiv = $("<div>", {id:"ssImageDiv"});
	var _navDiv = $("<div>", {style:"height:2em;"});
	var _nextButton = $("<img>", {src:gs.imageURLs.next, style:"float:right; cursor:pointer;"});
	var _prevButton = $("<img>", {src:gs.imageURLs.prev, style:"float:left; cursor:pointer;"});
	var _clearDiv = $("<div>", {style:"clear:both;"});
	var _currentIndex = 0;
	
	_navDiv.append(_nextButton);
	_navDiv.append(_prevButton);
	_navDiv.append(_clearDiv);
	_mainDiv.append(_navDiv);
	_mainDiv.append(_imageDiv);
	
	for(var i = 0; i < images.length; i++)
	{
		_images.push($("<img>", {src:images[i], "class":"slideshowImage"}));
	}
	
	_imageDiv.append(_images[0]);
	
	this.nextImage = function()
	{
		if(!_inTransition)
		{
			_inTransition = true;
			if((_currentIndex + 1) < _images.length)
			{
				_imageDiv.fadeOut(1000, function()
				{
					_imageDiv.empty();
					_imageDiv.append(_images[_currentIndex + 1]);
					_currentIndex++;
					_imageDiv.fadeIn(1000, function()
					{
						_inTransition = false;
					});
				});
			}
			else
			{
				_inTransition = false;
			}
		}
	}
	
	this.prevImage = function()
	{
		if(!_inTransition)
		{
			_inTransition = true;
			if((_currentIndex - 1) >= 0)
			{
				_imageDiv.fadeOut(1000, function()
				{
					_imageDiv.empty();
					_imageDiv.append(_images[_currentIndex - 1]);
					_currentIndex--;
					_imageDiv.fadeIn(1000, function()
					{
						_inTransition = false;
					});
				});
			}
			else
			{
				_inTransition = false;
			}
		}
	}
	
	var getRootFilenameFromURL = function(url)
	{
		var urlSegments = url.split("/");
		var filename = urlSegments[urlSegments.length - 1];
		return filename.replace(/_thumb\..*$/, "");
	}
	
	var setLink = function(currentLink, index)
	{
		$(currentLink).click(function()
		{
			_inTransition = true;
			_currentIndex = index;
			_imageDiv.fadeOut(1000, function()
			{
				_imageDiv.empty();
				_imageDiv.append(_images[_currentIndex]);
				_imageDiv.fadeIn(1000, function()
				{
					_inTransition = false;
				});
			});
		});
	}

	var sliderLinks = $(".pageSliderCol a");
	for(var i = 0; i < sliderLinks.length; i++)
	{
		var currentLink = sliderLinks[i];
		var id = $(currentLink).attr("href").split("'")[1];

		for(var j = 0; j < idArray.length; j++)
		{
			if(idArray[j].id == id)
			{
				var image = idArray[j].source;
				
				for(var l = 0; l < images.length; l++)
				{
					var filename = getRootFilenameFromURL(images[l]);
					if (filename == image)
					{
						setLink(currentLink, l);
						break;
					}
				}
				
				break;
			}
		}
	}

	_nextButton.click(this.nextImage);
	_prevButton.click(this.prevImage);
}