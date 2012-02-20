//Some "constants" to match the server constants
var SUCCESS = 1;
var ACCEPTED = 2;
var ERROR = 3;
var CONTINUING = 10;
var COMPLETED = 11;
var HALTED = 12;

function getElementsByClassName(cl, parent) 
{
	var elemArray = [];
	var classRegEx = new RegExp("\\b" + cl + "\\b");
	var elems;
	if(parent)
	{	
		elems = parent.getElementsByTagName("*");
	}
	else
	{
		elems = document.getElementsByTagName("*");
	}
	
	for (var i = 0; i < elems.length; i++) 
	{
		var classeName = elems[i].className;
		if (classRegEx.test(classeName)) elemArray.push(elems[i]);
	}
	
	return elemArray;
};

function getNextSiblingOfType(elem, type)
{
	if(elem == null)
	{
		return null;
	}
	
	var current = elem.nextSibling;
	while(current != null)
	{
		if(current.nodeName.toLowerCase() == type)
		{
			return current;
		}
		
		current = current.nextSibling;
	}
	return null;
}

function getPrevSiblingOfType(elem, type)
{
	if(elem == null)
	{
		return null;
	}
	
	var current = elem.previousSibling;
	while(current != null)
	{
		if(current.nodeName.toLowerCase() == type)
		{
			return current;
		}
		
		current = current.previousSibling;
	}
	return null;
}

function saveTransaction(transaction)
{
	console.log(transaction);
	_transactions.push(transaction);
}

function undo()
{
	if(_undoOperations.length == 0)
	{
		return;
	}
	
	var undoOp = _undoOperations.pop();

	//Create/Duplicate undo
	if(undoOp.op == "del")
	{
		if(undoOp.srcElem.childList)
		{
			removeFromParent(undoOp.srcElem.childList);
		}
		if(undoOp.srcElem.parentItem)
		{
			undoOp.srcElem.parentItem.menu.newSectionLink.style.display = "inline";
			undoOp.srcElem.parentItem.childList = null;
		}
		removeFromParent(undoOp.srcElem);
	}
	
	if(undoOp.op == "delMeta")
	{
		if(undoOp.srcElem.childList)
		{
			removeFromParent(undoOp.srcElem.childList);
		}
		if(undoOp.srcElem.parentItem)
		{
			undoOp.srcElem.parentItem.menu.newSectionLink.style.display = "inline";
			undoOp.srcElem.parentItem.childList = null;
		}
		de.doc.unregisterEditSection(undoOp.srcElem);
		removeFromParent(undoOp.srcElem);
	}
	
	//Move undo (mva is move after, mvb is move before, mvi is move into)
	else if(undoOp.op == "mva" || undoOp.op == "mvb" || undoOp.op == "mvi")
	{
		if(undoOp.op == "mvb")
		{
			undoOp.refElem.parentNode.insertBefore(undoOp.srcElem, undoOp.refElem);
		}
		else if(undoOp.op == "mva")
		{
			insertAfter(undoOp.srcElem, undoOp.refElem);
		}
		else
		{
			undoOp.refElem.removeChild(undoOp.refElem.firstChild);
			undoOp.refElem.appendChild(undoOp.srcElem);
		}

		if(undoOp.srcElem.textDiv)
		{
			insertAfter(undoOp.srcElem.textDiv, undoOp.srcElem);
		}
		if(undoOp.srcElem.childList)
		{
			insertAfter(undoOp.srcElem.childList, undoOp.srcElem.textDiv);
		}

		if(undoOp.srcElem.onmouseout)
		{
			//Uncolour the section if it coloured
			undoOp.srcElem.onmouseout();
		}
		updateFromTop();
	}
	else if(undoOp.op == "display")
	{
		undoOp.srcElem.style.display = undoOp.subOp;
	}
	
	if(undoOp.removeDeletedMetadata)
	{
		_deletedMetadata.pop();
	}

	if(undoOp.removeTransaction)
	{
		_transactions.pop();
	}
}

function addCollectionToBuild(collection)
{
	for(var i = 0; i < _collectionsToBuild.length; i++)
	{
		if(collection == _collectionsToBuild[i])
		{
			return;
		}
	}
	_collectionsToBuild.push(collection);
}

function save()
{
	for(var i = 0; i < _deletedMetadata.length; i++)
	{
		var currentRow = _deletedMetadata[i];

		//Get document ID
		var currentElem = currentRow;
		while((currentElem = currentElem.parentNode).tagName != "TABLE");
		var docID = currentElem.getAttribute("id").substring(4);

		//Get metadata name
		var cells = currentRow.getElementsByTagName("TD");
		var nameCell = cells[0];
		var name = nameCell.innerHTML;
		var valueCell = cells[1];
		var value = valueCell.innerHTML;
		
		gs.functions.removeArchivesMetadata(gs.cgiParams.p_c /*bad*/, "localsite" /*bad*/, docID, name, null, value, function(){console.log("REMOVED ARCHIVES");});
		addCollectionToBuild(gs.cgiParams.p_c);
		
		removeFromParent(currentRow);
	}

	var changes = de.Changes.getChangedEditableSections();
	
	for(var i = 0; i < changes.length; i++)
	{
		var changedElem = changes[i];

		//Save metadata 
		if(hasClass(changedElem, "metaTableCell")) 
		{
			//Get document ID
			var currentElem = changedElem;
			while((currentElem = currentElem.parentNode).tagName != "TABLE");
			var docID = currentElem.getAttribute("id").substring(4);

			//Get metadata name
			var row = changedElem.parentNode;
			var cells = row.getElementsByTagName("TD");
			var nameCell = cells[0];
			var name = nameCell.innerHTML;

			if(changedElem.originalValue)
			{
				gs.functions.setArchivesMetadata(gs.cgiParams.p_c /*bad*/, "localsite" /*bad*/, docID, name, null, changedElem.innerHTML, changedElem.originalValue, "override", function(){console.log("SAVED ARCHIVES");});
			}
			else
			{
				gs.functions.setArchivesMetadata(gs.cgiParams.p_c /*bad*/, "localsite" /*bad*/, docID, name, null, changedElem.innerHTML, null, "accumulate", function(){console.log("SAVED ARCHIVES");});
			}
			changedElem.originalValue = changedElem.innerHTML;
			addCollectionToBuild(gs.cgiParams.p_c);
		}
		//Save content
		else if(hasClass(changedElem, "renderedText"))
		{
			var section = changedElem.parentDiv.parentItem;
			saveTransaction('{"operation":"setText", "text":"' + changedElem.innerHTML.replace(/"/g, "\\\"").replace(/&/g, "%26") + '", "collection":"' + section.collection + '", "oid":"' + section.nodeID + '"}');
			addCollectionToBuild(section.collection);
		}
	}

	var request = "[";
	for(var i = 0; i < _transactions.length; i++)
	{
		request += _transactions[i];
		if(i != _transactions.length - 1)
		{
			request += ",";
		}
	}
	request += "]";
	
	var statusID;
	var ajax = new gs.functions.ajaxRequest();
	ajax.open("POST", _baseURL, true);
	ajax.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	ajax.onreadystatechange = function()
	{
		if(ajax.readyState == 4 && ajax.status == 200)
		{
			var text = ajax.responseText;
			var xml = validateXML(text);
			
			var errorElems;
			if(!xml || checkForErrors(xml))
			{
				alert("There was an error saving, aborting");
			
				var saveButton = document.getElementById("saveButton");
				saveButton.innerHTML = "Save changes";
				saveButton.disabled = false;
				
				_statusBar.removeStatus(statusID);
				return;
			}

			_statusBar.removeStatus(statusID);
			buildCollections(_collectionsToBuild);
		}
	}

	if(_collectionsToBuild.length > 0)
	{
		var saveButton = document.getElementById("saveButton");
		saveButton.innerHTML = "Saving...";
		saveButton.disabled = true;
		
		statusID = _statusBar.addStatus("Modifying archive files...");
		ajax.send("a=g&rt=r&s=DocumentExecuteTransaction&s1.transactions=" + request);
	}
}

function buildCollections(collections)
{
	var saveButton = document.getElementById("saveButton");
	if(!collections || collections.length == 0)
	{
		console.log("List of collections to build is empty");
		saveButton.innerHTML = "Save changes";
		saveButton.disabled = false;
		return;
	}

	var counter = 0;
	var statusID = 0;
	var buildFunction = function()
	{
		var ajax = new gs.functions.ajaxRequest();
		ajax.open("GET", _baseURL + "?a=g&rt=r&ro=1&s=BuildCollection&s1.collection=" + collections[counter]);
		ajax.onreadystatechange = function()
		{
			if(ajax.readyState == 4 && ajax.status == 200)
			{
				var text = ajax.responseText;
				var xml = validateXML(text);

				if(!xml || checkForErrors(xml))
				{
					alert("Could not build collection -> " + collections[counter] + ", aborting");
					
					_statusBar.removeStatus(statusID);
					saveButton.innerHTML = "Save changes";
					saveButton.disabled = false;
					
					return;
				}

				var status = xml.getElementsByTagName("status")[0];
				var pid = status.getAttribute("pid");

				startCheckLoop(pid, "BuildCollection", statusID, function()
				{
					var localAjax = new gs.functions.ajaxRequest();
					localAjax.open("GET", _baseURL + "?a=g&rt=r&ro=1&s=ActivateCollection&s1.collection=" + collections[counter], true);
					localAjax.onreadystatechange = function()
					{
						if(localAjax.readyState == 4 && localAjax.status == 200)
						{
							var localText = localAjax.responseText;
							var localXML = validateXML(localText);
							
							if(!xml || checkForErrors(xml))
							{
								alert("Could not activate collection -> " + collections[counter] + ", aborting");
								
								_statusBar.removeStatus(statusID);
								saveButton.innerHTML = "Save changes";
								saveButton.disabled = false;
								
								return;
							}

							var localStatus = localXML.getElementsByTagName("status")[0];
							if(localStatus)
							{
								var localPID = localStatus.getAttribute("pid");
								startCheckLoop(localPID, "ActivateCollection", statusID, function()
								{
									if(counter == collections.length - 1)
									{
										removeCollectionsFromBuildList(collections);
									}
									else
									{
										counter++;
										buildFunction();
									}

									_transactions = new Array();

									_statusBar.removeStatus(statusID);
									saveButton.innerHTML = "Save changes";
									saveButton.disabled = false;
								});
							}
						}
					}
					_statusBar.changeStatus(statusID, "Activating collection " + collections[counter] + "...");
					localAjax.send();
				});
			}
		}
		statusID = _statusBar.addStatus("Building collection " + collections[counter] + "...");
		ajax.send();
	}
	buildFunction();
}

function startCheckLoop(pid, serverFunction, statusID, callbackFunction)
{
	var ajaxFunction = function()
	{
		var saveButton = document.getElementById("saveButton");
		
		var ajax = new gs.functions.ajaxRequest();
		ajax.open("GET", _baseURL + "?a=g&rt=s&ro=1&s=" + serverFunction + "&s1.pid=" + pid, true);
		ajax.onreadystatechange = function()
		{
			if(ajax.readyState == 4 && ajax.status == 200)
			{
				var text = ajax.responseText;
				var xml = validateXML(text);
				
				if(!xml || checkForErrors(xml))
				{
					alert("Could not check status of " + serverFunction + ", there was an error in the XML, aborting");
					
					_statusBar.removeStatus(statusID);
					saveButton.innerHTML = "Save changes";
					saveButton.disabled = false;
					
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
					alert("Could not check status of " + serverFunction + ", there was an error on the server, aborting");
					
					_statusBar.removeStatus(statusID);
					saveButton.innerHTML = "Save changes";
					saveButton.disabled = false;
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

function removeCollectionsFromBuildList(collections)
{
	var tempArray = new Array();
	for(var i = 0; i < _collectionsToBuild.length; i++)
	{
		var found = false;
		for(var j = 0; j < collections.length; j++)
		{
			if(collections[j] == _collectionsToBuild[i])
			{
				found = true;
				break;
			}
		}
		
		if(!found)
		{
			tempArray.push(_collectionsToBuild[i]);
		}
	}
	_collectionsToBuild = tempArray;
}

function checkForErrors(xml)
{
	var errorElems = xml.getElementsByTagName("error");
	
	if(errorElems && errorElems.length > 0)
	{
		var errorString = "There was an error saving your changes: ";
		for(var i = 0; i < errorElems.length; i++)
		{
			errorString += " " + errorElems.item(i).firstChild.nodeValue;
		}
		alert(errorString);
		return true;
	}
	return false; //No errors
}

function validateXML(txt)
{
	// code for IE
	if (window.ActiveXObject)
	{
		var xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
		xmlDoc.async = "false";
		xmlDoc.loadXML(document.all(txt).value);

		if(xmlDoc.parseError.errorCode!=0)
		{
			txt = "Error Code: " + xmlDoc.parseError.errorCode + "\n";
			txt = txt + "Error Reason: " + xmlDoc.parseError.reason;
			txt = txt + "Error Line: " + xmlDoc.parseError.line;
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
			console.log("There was an error parsing the XML");
			return null;
		}
		
		return xmlDoc;
	}
	else
	{
		console.log('Your browser cannot handle XML validation');
	}
	return null;
}

function onVisibleMetadataSetChange()
{
	var metadataList = document.getElementById("metadataSetList");
	var index = metadataList.selectedIndex;
	var options = metadataList.getElementsByTagName("OPTION");
	var selectedOption = options[index];
	
	var selectedSet = selectedOption.innerHTML;
	changeVisibleMetadata(selectedSet);
}

function changeVisibleMetadata(metadataSetName)
{
	var tables = document.getElementsByTagName("TABLE");
	for(var i = 0; i < tables.length; i++)
	{
		var id = tables[i].getAttribute("id");
		if(id && id.search(/^meta/) != -1)
		{
			var rows = tables[i].getElementsByTagName("TR");
			for(var j = 0; j < rows.length; j++)
			{
				if(metadataSetName == "All")
				{
					rows[j].style.display = "table-row";
				}
				else
				{
					var cells = rows[j].getElementsByTagName("TD");
					var cellName = cells[0].innerHTML;
					
					if(cellName.indexOf(".") == -1)
					{
						rows[j].style.display = "none";
					}
					else
					{
						var setName = cellName.substring(0, cellName.lastIndexOf("."));
						if(setName == metadataSetName)
						{
							rows[j].style.display = "table-row";
						}
						else
						{
							rows[j].style.display = "none";
						}
					}
				}
			}
		}
	}
}

function asyncRegisterEditSection(cell)
{
	//This registering can cause a sizeable delay so we'll thread it (effectively) so the browser is not paused
	cell.originalValue = cell.innerHTML;
	setTimeout(function(){de.doc.registerEditSection(cell)}, 0);
}

function addFunctionalityToTable(table)
{
	var rows = table.getElementsByTagName("TR");
	for(var i = 0; i < rows.length; i++)
	{
		var cells = rows[i].getElementsByTagName("TD");
		var metadataName = cells[0].innerHTML;
		
		if(metadataName.indexOf(".") != -1)
		{
			var metadataSetName = metadataName.substring(0, metadataName.lastIndexOf("."));
			
			var found = false;
			for(var j = 0; j < _metadataSetList.length; j++)
			{
				if(metadataSetName == _metadataSetList[j])
				{
					found = true;
					break;
				}
			}
			
			if(!found)
			{
				_metadataSetList.push(metadataSetName);
				
				var metadataSetList = document.getElementById("metadataSetList");
				var newOption = document.createElement("OPTION");
				newOption.innerHTML = metadataSetName;
				metadataSetList.appendChild(newOption);
			}
		}
			
		asyncRegisterEditSection(cells[1]);

		addRemoveLinkToRow(rows[i]);
	}

	var metaNameField = document.createElement("INPUT");
	metaNameField.setAttribute("type", "text");
	insertAfter(metaNameField, table);
	
	var addRowButton = document.createElement("BUTTON");
	addRowButton.innerHTML = "Add new metadata";
	addRowButton.onclick = function() 
	{ 
		var name = metaNameField.value;
		if(!name || name == "")
		{
			console.log("No value given for new metadata name");
			return;
		}
		
		var newRow = document.createElement("TR");
		var nameCell = document.createElement("TD");
		var valueCell = document.createElement("TD");
		nameCell.setAttribute("class", "metaTableCellName");
		nameCell.innerHTML = name;
		valueCell.setAttribute("class", "metaTableCell");
		
		newRow.appendChild(nameCell);
		newRow.appendChild(valueCell);
		addRemoveLinkToRow(newRow);
		table.appendChild(newRow);
		
		var undo = new Array();
		undo.op = "delMeta";
		undo.srcElem = newRow;
		undo.removeTransaction = false;
		_undoOperations.push(undo);
		
		//Threading this function here probably isn't necessary like the other times it is called
		de.doc.registerEditSection(valueCell);
	};
	insertAfter(addRowButton, metaNameField);
}

function addRemoveLinkToRow(row)
{
	var newCell = document.createElement("TD");
	var removeLink = document.createElement("A");
	removeLink.innerHTML = "remove";
	removeLink.setAttribute("href", "javascript:;");
	removeLink.onclick = function()
	{
		var undo = new Array();
		undo.srcElem = row;
		undo.op = "display";
		undo.subOp = "table-row";
		undo.removeDeletedMetadata = true;
		_undoOperations.push(undo);
		_deletedMetadata.push(row);
		row.style.display = "none";
	}
	newCell.appendChild(removeLink);
	newCell.setAttribute("class", "metaTableCell");
	newCell.setAttribute("style", "font-size:0.6em; padding-left: 3px; padding-right: 3px;");
	row.appendChild(newCell);
}

function createTopMenuBar()
{
	//Create the top menu bar
	var headerTable = document.createElement("TABLE");
	var tableBody = document.createElement("TBODY");
	var row = document.createElement("TR");
	var newDocCell = document.createElement("TD");
	var newSecCell = document.createElement("TD");
	var saveCell = document.createElement("TD");
	var undoCell = document.createElement("TD");
	var metadataListCell = document.createElement("TD");
	
	var metadataListLabel = document.createElement("SPAN");
	metadataListLabel.innerHTML = "Visible metadata: ";
	var metadataList = document.createElement("SELECT");
	metadataList.setAttribute("id", "metadataSetList");
	metadataList.onchange = onVisibleMetadataSetChange;
	var allMetadataOption = document.createElement("OPTION");
	metadataList.appendChild(allMetadataOption);
	allMetadataOption.innerHTML = "All";
	metadataListCell.appendChild(metadataListLabel);
	metadataListCell.appendChild(metadataList);

	metadataListCell.setAttribute("class", "headerTableTD");
	newDocCell.setAttribute("class", "headerTableTD");
	newSecCell.setAttribute("class", "headerTableTD");
	undoCell.setAttribute("class", "headerTableTD");
	saveCell.setAttribute("class", "headerTableTD");
	
	headerTable.appendChild(tableBody);
	tableBody.appendChild(row);
	row.appendChild(saveCell);
	row.appendChild(undoCell);
	row.appendChild(newDocCell);
	row.appendChild(newSecCell);
	row.appendChild(metadataListCell);

	//The "Save changes" button
	var saveButton = document.createElement("BUTTON");
	saveButton.innerHTML = "Save changes";
	saveButton.setAttribute("onclick", "save();");
	saveButton.setAttribute("id", "saveButton");
	saveCell.appendChild(saveButton);
	
	//The "Undo" button
	var undoButton = document.createElement("BUTTON");
	undoButton.innerHTML = "Undo";
	undoButton.setAttribute("onclick", "undo();");
	undoCell.appendChild(undoButton);

	//The "Create new document" button
	var newDocButton = document.createElement("BUTTON");
	newDocButton.innerHTML = "Create new document";
	newDocButton.setAttribute("onclick", "createNewDocumentArea();");
	newDocCell.appendChild(newDocButton);

	//The "Insert new section" LI
	var newSecLI = createDraggableNewSection(newSecCell);
	
	return headerTable;
}

function getMetadataFromNode(node, name)
{
	var currentNode = node.firstChild;
	while(currentNode != null)
	{
		if(currentNode.nodeName == "metadataList")
		{
			currentNode = currentNode.firstChild;
			break;
		}
		
		currentNode = currentNode.nextSibling;
	}
	
	while(currentNode != null)
	{
		if(currentNode.nodeName == "metadata" && currentNode.getAttribute("name") == name)
		{
			return currentNode.firstChild.nodeValue;
		}
		
		currentNode = currentNode.nextSibling;
	}
	return "{UNTITLED}";
}

function storeMetadata(node, listItem)
{
	listItem.metadata = new Array();
	
	var currentNode = node.firstChild;
	while(currentNode != null)
	{
		if(currentNode.nodeName == "metadataList")
		{
			currentNode = currentNode.firstChild;
			break;
		}
		
		currentNode = currentNode.nextSibling;
	}
	
	while(currentNode != null)
	{
		if(currentNode.nodeName == "metadata")
		{
			listItem.metadata[currentNode.getAttribute("name")] = currentNode.firstChild.nodeValue;
		}
		
		currentNode = currentNode.nextSibling;
	}
}

function hasClass(elem, classVal)
{
	if(!elem || !elem.getAttribute("class"))
	{
		return false;
	}

	return (elem.getAttribute("class").search(classVal) != -1)
}

function getNodeContent(node)
{
	var currentNode = node.firstChild;
	while(currentNode != null)
	{
		if(currentNode.nodeName == "nodeContent")
		{
			return currentNode.firstChild;
		}
		
		currentNode = currentNode.nextSibling;
	}
	return null;
}

function containsDocumentNode(node)
{
	var currentNode = node.firstChild;
	while(currentNode != null)
	{
		if(currentNode.nodeName == "documentNode")
		{
			return true;
		}
		
		currentNode = currentNode.nextSibling;
	}
	return false;
}

function isExpanded(textDiv)
{
	if(!textDiv.style.display || textDiv.style.display == "block")
	{
		return true;
	}
	return false;
}

function toggleTextDiv(section)
{
	var textDiv = section.textDiv;
	if(textDiv)
	{
		if(isExpanded(textDiv))
		{
			textDiv.style.display = "none";
			section.menu.editTextLink.innerHTML = "edit";
		}
		else
		{
			textDiv.style.display = "block";
			section.menu.editTextLink.innerHTML = "hide";
		}
	}
}

function updateFromTop()
{
	updateRecursive(document.getElementById("dbDiv"), null, null, 0);
}

function insertAfter(elem, refElem)
{
	if(refElem.nextSibling)
	{
		refElem.parentNode.insertBefore(elem, refElem.nextSibling);
	}
	else
	{
		refElem.parentNode.appendChild(elem);
	}
}

function removeFromParent(elem)
{
	elem.parentNode.removeChild(elem);
}

function createSectionTitle(text)
{
	var textSpan = document.createElement("SPAN");
	if(text)
	{
		textSpan.appendChild(document.createTextNode(" " + text + " "));
	}
	else
	{
		textSpan.appendChild(document.createTextNode(" [UNTITLED SECTION] "));
	}
	return textSpan;
}

function setMouseOverAndOutFunctions(section)
{
	//Colour the list item and display the menu on mouse over
	section.onmouseover = function(e)
	{
		if(this.menu){this.menu.style.display = "inline";}
		this.style.background = "rgb(255, 200, 0)";
	};
	//Uncolour the list item and hide the menu on mouse out
	section.onmouseout = function(e)
	{
		if(this.menu){this.menu.style.display = "none";}
		this.style.background = "none";
	};
}

function createDraggableNewSection(parent)
{
	var newSecLI = document.createElement("LI");
	var newSpan = document.createElement("SPAN");
	newSpan.innerHTML = "Insert new section ";
	
	newSecLI.sectionTitle = newSpan;
	newSecLI.appendChild(newSpan);
	newSecLI.setAttribute("class", "dragItem newSection");
	newSecLI.newSection = true;
	newSecLI.parent = parent;
	newSecLI.index = -1;
	new YAHOO.example.DDList(newSecLI);
	parent.appendChild(newSecLI);
}

function closeAllOpenContents()
{
	for(var i = 0; i < _allContents.length; i++)
	{
		if(isExpanded(_allContents[i].textDiv))
		{
			toggleTextDiv(_allContents[i]);
		}
	}
	DDM.refreshCache();
}

//Status Bar class (initialised with new StatusBar(elem);)
function StatusBar(mainElem)
{
	var _statusMap = new Array();
	var _statusIDCounter = 0;
	var _mainElem = mainElem;
	var _activeMessages = 0;
	
	_mainElem.style.display = "none";
	
	this.addStatus = function(newStatus)
	{
		_mainElem.style.display = "block";
		var newStatusDiv = document.createElement("DIV");
		var newStatusSpan = document.createElement("SPAN");
		
		var workingImage = document.createElement("IMG"); 
		workingImage.setAttribute("src", gs.imageURLs.loading);
		workingImage.setAttribute("height", "16px");
		workingImage.setAttribute("width", "16px");
		newStatusDiv.appendChild(workingImage);
		
		newStatusDiv.appendChild(newStatusSpan);
		newStatusSpan.innerHTML = " " + newStatus;
		newStatusDiv.setAttribute("class", "statusMessage");		
		newStatusDiv.span = newStatusSpan;
		
		_mainElem.appendChild(newStatusDiv);
		_statusMap["status" + _statusIDCounter] = newStatusDiv;
		_activeMessages++;
		return _statusIDCounter++;
	}
	
	this.changeStatus = function(id, newStatus)
	{
		if(_statusMap["status" + id])
		{
			_statusMap["status" + id].span.innerHTML = " " + newStatus;
		}
	}
	
	this.removeStatus = function(id)
	{
		if(_statusMap["status" + id])
		{
			removeFromParent(_statusMap["status" + id]);
			
			if(--_activeMessages == 0)
			{
				_mainElem.style.display = "none";
			}
		}
	}
}

/*
function toggleEdit(e)
{
	var mousePos = de.events.getXYInWindowFromEvent(e);
	var cDesc = de.cursor.getCursorDescAtXY(mousePos.x, mousePos.y, de.events.getEventTarget(e));
	de.cursor.setCursor(cDesc);
}
*/