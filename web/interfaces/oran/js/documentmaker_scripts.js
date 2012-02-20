var _transactions = new Array();
var _collectionsToBuild = new Array();
var _allContents = new Array();
var _idCounter = 0;
var _indexCounter = 0;
var _deletedSections = new Array();
var _undoOperations = new Array();
var _baseURL;
var _statusBar;
var _metadataSetList = new Array();

function init()
{
	de.init();
	//Get all of the links on the page
	var allLinks = document.getElementsByTagName("a");
	
	//Work out which links are the actual document links
	var docLinks = new Array();
	for(var i = 0; i < allLinks.length; i++)
	{
		if(allLinks[i].getAttribute("class") && (hasClass(allLinks[i], "dbdoc")))
		{
			docLinks.push(allLinks[i]);
		}
	}
	
	if(gs.cgiParams.docToEdit)
	{
		var content = document.getElementById("gs_content");
		var newLink = document.createElement("A");
		newLink.setAttribute("href", gs.xsltParams.library_name + "?a=d&c=" + gs.cgiParams.p_c + "&dt=hierarchy&ed=1&d=" + gs.cgiParams.docToEdit);
		content.appendChild(newLink);
		docLinks.push(newLink);
	}
	
	if(docLinks.length == 0)
	{
		document.getElementById("gs_content").innerHTML = "No documents in the Document Basket";
		return;
	}
	
	//Create the top menu bar
	var menuBar = createTopMenuBar();
	
	//Add the menu bar to the page
	var mainContentDiv = document.getElementById("gs_content");
	mainContentDiv.appendChild(menuBar);
	
	var dbDiv = document.createElement("DIV");
	dbDiv.setAttribute("id", "dbDiv");
	insertAfter(dbDiv, menuBar);
	
	var statusDiv = document.createElement("DIV");
	statusDiv.setAttribute("class", "statusBar");
	insertAfter(statusDiv, menuBar);	
	_statusBar = new StatusBar(statusDiv);
	
	//Request the html for each link's page
	for(var i = 0; i < docLinks.length; i++)
	{
		var callback = {
			success: addDocumentStructureToPage,
			failure: function(data){/*alert("FAILED");*/}
		}
		callback.currentLink = docLinks[i];
		YAHOO.util.Connect.asyncRequest("GET", docLinks[i].getAttribute("href").concat('&dmd=true&excerptid=gs-document-text&hhf=[{"name":"Cache-Control", "value":"no-cache"}]'), callback);
	}

	_baseURL = gs.xsltParams.library_name;
}

function addDocumentStructureToPage(data)
{
	//Get the HTML
	var page = data.responseText;
	
	//Add the HTML to the page inside an invisible div
	var tempDiv = document.createElement("DIV");
	tempDiv.innerHTML = page;
	tempDiv.style.display = "none";
	insertAfter(tempDiv, this.currentLink);
	
	//Get the collection that this document belongs to
	var collection = document.getElementById("gs-document-text").getAttribute("collection");

	//Get the Document Basket div
	var dbDiv = document.getElementById("dbDiv");
	
	//Create the container list and add it to the Document Basket div
	var containerUL = document.createElement("UL");
	containerUL.setAttribute("class", "topLevel");
	dbDiv.appendChild(containerUL);

	//Get all of the headers in the page
	var headers = getElementsByClassName("sectionTitle", tempDiv);

	//Some necessary loop variables
	var prevItem = null;
	var prevDepth = 0;
	var levelContainers = new Array();
	levelContainers[0] = containerUL;
	
	//Loop through all of the headers
	for(var i = 0; i < headers.length; i++)
	{
		var currentHeader = headers[i];
		
		//If the currentHeader is not a <td> element then we are not interested
		if(currentHeader.nodeName.toLowerCase() != "td")
		{
			continue;
		}

		//Split the section ID on . to get its position in the document
		var posArray = currentHeader.getAttribute("id").split(".");
		
		//Save the document ID
		var docID = posArray[0].substring(6);
		
		//Save the depth of the section (top level is 0)
		var depth = posArray.length - 1;
		
		//Turn the position array into a string
		var position = ""; 
		for(var j = 1; j < posArray.length; j++)
		{
			if(j != 1) 
			{
				position += ".";
			}
			position += posArray[j];
		}
		
		//Save the section number
		var secID = currentHeader.getAttribute("id").substring("6");
		
		//Get the text of the section
		var currentText = document.getElementById("text" + secID);
		var renderedDiv = createSectionTextDiv(currentText.innerHTML);

		var newItem = document.createElement("LI");
		new YAHOO.example.DDList(newItem);

		var title = createSectionTitle(currentHeader.innerHTML);
		newItem.sectionTitle = title;
		newItem.appendChild(title);
		newItem.setAttribute("class", depth == 0 ? "dragItem topLevel" : "dragItem");
		newItem.textDiv = renderedDiv;
		renderedDiv.parentItem = newItem;
		
		var metadataTable = document.getElementById("meta" + secID);
		renderedDiv.insertBefore(metadataTable, renderedDiv.firstChild);
		addFunctionalityToTable(metadataTable);
		
		if(depth > prevDepth)
		{
			var newContainer = document.createElement("UL");
			new YAHOO.util.DDTarget(newContainer);
			newContainer.setAttribute("class", "dragList");
			
			prevItem.childList = newContainer;
			prevItem.menu.newSectionLink.style.display = "none";
			newContainer.parentItem = prevItem;
			levelContainers[depth - 1].appendChild(newContainer);
			levelContainers[depth] = newContainer;
		}
		prevDepth = depth;

		levelContainers[depth].appendChild(newItem);
		levelContainers[depth].appendChild(renderedDiv);

		createSectionMenu(newItem);
		setMouseOverAndOutFunctions(newItem);

		//Set various section properties
		//newItem.collection = collectionName;
		newItem.documentID = docID;
		newItem.position = position;
		newItem.nodeID = secID;
		newItem.dbID = _idCounter++;
		newItem.index = newItem.dbID;
		newItem.collection = collection;
		newItem.parentList = levelContainers[depth];

		prevItem = newItem;

		//Insert the section into the list of sections
		_allContents.push(newItem);
	}
	
	removeFromParent(this.currentLink);
	updateFromTop();
}

function createSectionTextDiv(text)
{
	var renderedDiv = document.createElement("DIV");
	renderedDiv.setAttribute("style", "display:none;");
	
	var textDiv = document.createElement("DIV");
	if(text && text.length > 0)
	{
		textDiv.innerHTML = text;
	}
	else
	{
		textDiv.innerHTML = "&nbsp;";
	}
	textDiv.setAttribute("class", "renderedText editable");
	
	//This registering can cause a sizeable delay so we'll thread it (effectively) so the browser is not paused
	setTimeout(function(){de.doc.registerEditSection(textDiv)}, 0);

	renderedDiv.appendChild(textDiv);
	textDiv.parentDiv = renderedDiv;
	
	return renderedDiv;
}

function createNewDocumentArea()
{
	//Create the necessary elements
	var topLevelUL = document.createElement("UL");
	var topLevelLI = document.createElement("LI");
	var contentUL = document.createElement("UL");
	
	//Append the top-level list item to the top-level list
	topLevelUL.appendChild(topLevelLI);
	topLevelUL.setAttribute("class", "topLevel");
	
	//Set up the top-level item 
	topLevelLI.setAttribute("class", "dragItem topLevel");
	topLevelLI.childList = contentUL;
	contentUL.parentItem = topLevelLI;
	
	//Add a textDiv to the top-level item
	var textDiv = createSectionTextDiv(null);
	topLevelLI.textDiv = textDiv;
	topLevelUL.appendChild(textDiv);
	
	//Add a title to the top-level item
	var title = createSectionTitle("UNTITLED DOCUMENT");
	topLevelLI.appendChild(title);
	topLevelLI.sectionTitle = title;
	
	createSectionMenu(topLevelLI);
	setMouseOverAndOutFunctions(topLevelLI);
	
	//Set up the placeholder for the first section
	contentUL.setAttribute("class", "dragList");
	new YAHOO.util.DDTarget(contentUL);
	
	//Create a placeholder and add it to first section
	var placeHolder = createPlaceholder(null, contentUL, false);
	contentUL.appendChild(placeHolder);
	
	var dbDiv = document.getElementById("dbDiv");
	
	//Add elements to the page
	if(dbDiv.firstChild)
	{
		dbDiv.insertBefore(topLevelUL, dbDiv.firstChild);
	}
	else
	{
		dbDiv.appendChild(topLevelUL);
	}
	insertAfter(contentUL, topLevelLI.textDiv);
	
	//Correct any issues
	updateFromTop();
}

function createPlaceholder(parent, parentList, mouseEvents)
{
	//Create the place holder and assign its class
	var placeHolder = document.createElement("LI");
	placeHolder.setAttribute("class", "placeHolder");
	
	//If a parent was given then we can assign the collection and nodeID 
	if(parent)
	{
		placeHolder.collection = parent.collection;
		placeHolder.nodeID = parent.nodeID;
	}
	
	//If this is to be a plain placeholder then we don't want it to react to mouse events
	if(mouseEvents)
	{
		placeHolder.isEmptyList = true;
		
		//Create the delete section link
		var deleteSectionLink = document.createElement("A");
		deleteSectionLink.innerHTML = "delete section";
		deleteSectionLink.setAttribute("href", "javascript:;");
		deleteSectionLink.setAttribute("class", "menuLink");
		deleteSectionLink.style.display = "none";
		
		//Set the onclick behaviour of the delete link
		deleteSectionLink.onclick = function()
		{
			//Delete the place holder
			removeFromParent(placeHolder);
			
			//If this is in a list then delete the list (as this will be the only thing in the list)
			if(parentList)
			{
				var undo = new Array();
			
				undo.op = "mva";
				undo.srcElem = parentList;
				undo.refElem = parent;
				undo.removeTransaction = false;
				_undoOperations.push(undo);
				
				removeFromParent(parentList);
			}
			
			//Enable the "add sub-section" menu option in the parent
			if(parent)
			{
				parent.menu.newSectionLink.style.display = "inline";
				parent.childList = null;
			}
		}
		placeHolder.appendChild(deleteSectionLink);
	
		//Colour the list item and display the menu on mouse over
		placeHolder.onmouseover = function(e)
		{
			deleteSectionLink.style.display = "inline";
			this.style.background = "rgb(255, 200, 0)";
		};
		//Uncolour the list item and hide the menu on mouse out
		placeHolder.onmouseout = function(e)
		{
			deleteSectionLink.style.display = "none";
			this.style.background = "none";
		};
	}
	
	var dragItem = new YAHOO.example.DDList(placeHolder);
	dragItem.addInvalidHandleClass("placeHolder");
	return placeHolder;
}

function duplicateSection(section)
{
	var newLI = document.createElement("LI");
	newLI.setAttribute("class", "dragItem");
	new YAHOO.example.DDList(newLI);
	
	if(section.textDiv)
	{
		var textDiv = createSectionTextDiv(section.textDiv.innerHTML);
		newLI.textDiv = textDiv;
	}
	newLI.collection = section.collectionName;
	
	if(section.sectionTitle)
	{
		var title = createSectionTitle(section.sectionTitle.innerHTML);
		newLI.sectionTitle = title;
		newLI.appendChild(title);
	}
	
	createSectionMenu(newLI);
	setMouseOverAndOutFunctions(newLI);
	newLI.onmouseout();
	
	if(section.childList)
	{
		insertAfter(newLI, section.childList);
	}
	else if(section.textDiv)
	{
		insertAfter(newLI, section.textDiv);
	}
	else
	{
		insertAfter(newLI, section);
	}

	if(newLI.textDiv)
	{
		insertAfter(newLI.textDiv, newLI);
	}
	return newLI;
}

function duplicateSectionChildrenRecursive(duplicate, original)
{
	if(!original.childList)
	{
		return;
	}
	
	var newUL = document.createElement("UL");
	newUL.setAttribute("class", "dragList");
	new YAHOO.util.DDTarget(newUL);
	insertAfter(newUL, duplicate.textDiv);
		
	var children = new Array();
	var current = original.childList.firstChild;
	while(current != null)
	{
		children.push(current);
		current = current.nextSibling;
	}
	
	for(var i = 0; i < children.length; i++)
	{
		current = children[i];
		if(current.nodeName.toLowerCase() == "li" && !hasClass(current, "placeHolder"))
		{
			var newSection = duplicateSection(current);
			newUL.appendChild(newSection);
			if(current.childList)
			{
				duplicateSectionChildrenRecursive(newSection, current);
			}
		}
	}
	
	duplicate.childList = newUL;
	newUL.parentItem = duplicate.childList;
	
	if(duplicate.menu)
	{
		duplicate.menu.newSectionLink.style.display = "none";
	}
}

function deleteSection(section)
{
	var undo = new Array();
	var prev = getPrevSiblingOfType(section, "li");
	var next = getNextSiblingOfType(section, "li");
	var parent = section.parentList;
	if(prev)
	{
		undo.op = "mva";
		undo.refElem = prev;
	}
	else if(next)
	{
		undo.op = "mvb";
		undo.refElem = next;
	}
	else
	{
		undo.op = "mvi";
		undo.refElem = parent;
	}
	undo.srcElem = section;
	undo.removeTransaction = true;
	_undoOperations.push(undo);

	saveTransaction('{"operation":"delete", "collection":"' + section.collection + '", "oid":"' + section.nodeID + '"}');
	addCollectionToBuild(section.collection);
	
	_deletedSections.push(section);
	if(section.textDiv)
	{
		removeFromParent(section.textDiv);
	}
	if(section.childList)
	{
		removeFromParent(section.childList);
	}
	removeFromParent(section);
	updateFromTop();
}

function createBlankSection(parent)
{
	if(parent.childList)
	{
		return;
	}
	
	var newUL = document.createElement("UL");

	newUL.setAttribute("class", "dragList emptyList");
	new YAHOO.util.DDTarget(newUL);
	
	insertAfter(newUL, parent.textDiv);
	parent.childList = newUL;
	newUL.parentItem = parent;
	
	var menu = parent.menu;
	menu.newSectionLink.style.display = "none";
	
	var undo = new Array();
	undo.op = "del";
	undo.srcElem = newUL;
	undo.removeTransaction = false;
	_undoOperations.push(undo);
}

function createSectionMenu(section)
{
	var menuBar = document.createElement("SPAN");
	
	//Separator
	menuBar.appendChild(document.createTextNode(" "));
	
	//"Edit" link
	var toggleLink = document.createElement("A");
	toggleLink.innerHTML = "edit";
	toggleLink.setAttribute("class", "menuLink");
	toggleLink.setAttribute("href", "javascript:;");
	toggleLink.onclick = function(){toggleTextDiv(section);};
	menuBar.appendChild(toggleLink);
	menuBar.editTextLink = toggleLink;
	
	//Separator
	menuBar.appendChild(document.createTextNode(" "));
		
	var newSectionLink = document.createElement("A");
	newSectionLink.innerHTML = "add&nbsp;sub-section";
	newSectionLink.setAttribute("class", "menuLink");
	newSectionLink.setAttribute("href", "javascript:;");
	newSectionLink.onclick = function()
	{
		createBlankSection(section); 
		updateFromTop();
	};
	menuBar.appendChild(newSectionLink);
	menuBar.newSectionLink = newSectionLink;
	
	//"New Section" link
	if(section.childList)
	{
		newSectionLink.style.display = "none";
	}
	
	//Separator
	menuBar.appendChild(document.createTextNode(" "));
		
	//"Duplicate" link
	var duplicateLink = document.createElement("A");
	duplicateLink.innerHTML = "duplicate";
	duplicateLink.setAttribute("class", "menuLink");
	duplicateLink.setAttribute("href", "javascript:;");
	duplicateLink.onclick = function()
	{
		var newSection = duplicateSection(section);
		if(section.childList)
		{
			duplicateSectionChildrenRecursive(newSection, section);
		}
		
		var newNodeID = section.nodeID;
		var lastDigit = parseInt(newNodeID.substring(newNodeID.lastIndexOf(".") + 1));
		newNodeID = newNodeID.replace(/\.[^\.]*$/, "." + ++lastDigit);
		
		var undo = new Array();
		undo.op = "del";
		undo.srcElem = section;
		undo.removeTransaction = true;
		_undoOperations.push(undo);
		
		saveTransaction('{"operation":"duplicate", "subOperation":"insertBefore", "collection":"' + section.collection + '", "oid":"' + section.nodeID + '", "newCollection":"' + section.collection + '", "newOID":"' + newNodeID + '"}');
		addCollectionToBuild(section.collection);

		updateFromTop();
	};
	menuBar.appendChild(duplicateLink);
	menuBar.duplicateLink = duplicateLink;
	
	//Separator
	menuBar.appendChild(document.createTextNode(" "));
	
	//"Delete" link
	var deleteLink = document.createElement("A");
	deleteLink.innerHTML = "[X]";
	deleteLink.setAttribute("class", "menuLink deleteLink");
	deleteLink.setAttribute("href", "javascript:;");
	deleteLink.onclick = function(){deleteSection(section)};
	menuBar.appendChild(deleteLink);
	menuBar.deleteLink = deleteLink;
	
	menuBar.style.display = "none";
	section.appendChild(menuBar);
	section.menu = menuBar;
}

function updateRecursive(parent, currentDocument, currentPosition, level)
{
	if(level == 0)
	{
		_indexCounter = 0;
	}
	
	level++;
	var current = parent.firstChild;
	var posCount = 1;
	var lastItem;
	var liCount = 0;
	while(current != null)
	{
		if(current.nodeName.toLowerCase() == "ul")
		{
			var pos = null;
			if(level > 2)
			{
				if(!currentPosition)
				{
					pos = (posCount - 1);
				}
				else
				{
					pos = currentPosition + "." + (posCount - 1);
				}
			}
			
			updateRecursive(current, currentDocument, pos, level);
		}
		else if (current.nodeName.toLowerCase() == "li" && hasClass(current, "dragItem") && !hasClass(current, "placeHolder"))
		{
			if(currentDocument == null && current.nodeID)
			{
				currentDocument = current.nodeID;
			}

			var pos;
			if(!currentPosition)
			{
				pos = posCount;
			}
			else
			{
				pos = currentPosition + "." + posCount;
			}
			
			if(!hasClass(current, "topLevel"))
			{
				current.nodeID = currentDocument + "." + pos;
				current.position = pos;
				current.documentID = currentDocument;
			}
			posCount++;
			
			current.index = _indexCounter++;
		}
		else if (hasClass(current, "placeHolder") && !current.isEmptyList)
		{
			var pos;
			if(!currentPosition)
			{
				pos = posCount - 1;
			}
			else
			{
				pos = currentPosition + "." + posCount - 1;
			}
			current.nodeID = currentDocument + "." + pos;
		}
		
		if(current.nodeName.toLowerCase() == "li")
		{
			liCount++;
			lastItem = current;
		}
		
		current = current.nextSibling;
	}

	if(level > 2)
	{
		//If the last section a this level has a child list then insert a blank placeholder after it so we can insert sections after the list
		if(lastItem && lastItem.childList)
		{
			var placeHolder = createPlaceholder(lastItem, parent, false);
			parent.appendChild(placeHolder);
		}

		//If this list is empty or has 1 placeholder child
		if(liCount == 0 || (liCount == 1 && hasClass(lastItem, "placeHolder")))
		{
			//Give it the emptyList css class (if it does not already have it)
			if(!parent.getAttribute("class") || parent.getAttribute("class").search("emptyList") == -1)
			{
				var newClass = parent.getAttribute("class") ? parent.getAttribute("class") + " emptyList" : "emptyList";
				parent.setAttribute("class", newClass);
			}
			
			//If the list is empty then add a placeholder
			if(liCount == 0)
			{
				var placeHolder = createPlaceholder(parent.previousSibling.previousSibling, parent, true); //Find a smarter way of doing this
				parent.appendChild(placeHolder);
			}
		}
		//Remove the empty list class if the list is no longer empty
		else if(hasClass(parent, "emptyList"))
		{
			parent.setAttribute("class", parent.getAttribute("class").replace(/emptyList/g, ""));
		}
	}
}

YAHOO.util.Event.onDOMReady(init);