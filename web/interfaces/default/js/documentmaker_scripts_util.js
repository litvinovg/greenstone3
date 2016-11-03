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

function enableSaveButtons(enabled) {
  if (enabled) {
    $("#saveButton, #quickSaveButton").html(gs.text.de.save_changes);
    $("#saveButton, #quickSaveButton").removeAttr("disabled");

  } else {
    $("#saveButton, #quickSaveButton").html(gs.text.de.saving + "...");
    $("#saveButton, #quickSaveButton").attr("disabled", "disabled");
   
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

function save() {
  saveAndRebuild(false);
}

function rebuildCurrentCollection() {

  console.log(gs.text.de.rebuilding_collection);
  enableSaveButtons(false);
  var collection = gs.cgiParams.c;

  var collectionsArray = new Array();
  collectionsArray.push(collection);
  buildCollections(collectionsArray);
}

function saveAndRebuild(rebuild) 
{
//This works in most cases but will not work when taking a doc from one collection to another, will need to be fixed at some point
  var collection;
  if(gs.cgiParams.c && gs.cgiParams.c != "") {
    
    collection = gs.cgiParams.c;
  }
  else {
    collection = gs.cgiParams.p_c;
  }
  
	var sendBuildRequest = function()
	{
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
		ajax.open("POST", gs.xsltParams.library_name, true);
		ajax.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
		ajax.onreadystatechange = function()
		{
			if(ajax.readyState == 4 && ajax.status == 200)
			{
				var text = ajax.responseText;
				var xml = validateXML(text);
				
				var errorElems;
				if(!xml || checkForErrors(xml))
				{
					alert(gs.text.dse.error_saving);
				
					enableSaveButtons(true);
					
					if(_statusBar)
					{
						_statusBar.removeStatus(statusID);
					}
					return;
				}

				if(_statusBar)
				{
					_statusBar.removeStatus(statusID);
				}
				if (rebuild) {
				  buildCollections(_collectionsToBuild);
				} else {
				  // reset the save button here
				  enableSaveButtons(true);
				}
			}
		}

		if(_collectionsToBuild.length > 0)
		{
		  enableSaveButtons(false);

			if(_statusBar)
			{
				statusID = _statusBar.addStatus(gs.text.dse.modifying_archives + "...");
			}
			ajax.send("a=g&rt=r&s=DocumentExecuteTransaction&s1.transactions=" + request);
		}
	} // end sendBuildRequest definition

  var metadataChanges = new Array();
  if (_deletedMetadata.length > 0) {
    addCollectionToBuild(collection);

    for(var i = 0; i < _deletedMetadata.length; i++) {
      
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
      var value = valueCell.getElementsByTagName("TEXTAREA")[0].value;
      metadataChanges.push({type:'delete', docID:docID, name:name, value:value});
      removeFromParent(currentRow);
    }
  }

  var changes = changesToUpdate();
  //Clean changes
  editableInitStates = editableLastStates;
	for(var i = 0; i < changes.length; i++)
	{
		var changedElem = changes[i];
		//Save metadata 	
		if(gs.functions.hasClass(changedElem, "metaTableCellArea")) 
		{
			//Get document ID
			var currentElem = changedElem;
			while((currentElem = currentElem.parentNode).tagName != "TABLE");
			var docID = currentElem.getAttribute("id").substring(4);

			//Get metadata name
			var row = changedElem.parentNode.parentNode;
			var cells = row.getElementsByTagName("TD");
			var nameCell = cells[0];
			var name = nameCell.innerHTML;
			var value = changedElem.value;
			value = value.replace(/&nbsp;/g, " ");

			var orig = changedElem.originalValue;
			if (orig) {
			  orig = orig.replace(/&nbsp;/g, " ");
			}
			metadataChanges.push({collection:collection, docID:docID, name:name, value:value, orig:orig});
			changedElem.originalValue = changedElem.value;
			addCollectionToBuild(collection);
		}
		//Save content
		else if(gs.functions.hasClass(changedElem, "renderedText"))
		{
			var section = changedElem.parentDiv.parentItem;
			saveTransaction('{"operation":"setText", "text":"' + CKEDITOR.instances[changedElem.getAttribute("id")].getData().replace(/%/g, "%25").replace(/"/g, "\\\"").replace(/&/g, "%26") + '", "collection":"' + section.collection + '", "oid":"' + section.nodeID + '"}'); //'
			addCollectionToBuild(section.collection);
		}
		else if(gs.functions.hasClass(changedElem, "sectionText"))
		{
			var id = changedElem.getAttribute("id");
			var sectionID = id.substring(4);
			saveTransaction('{"operation":"setText", "text":"' + CKEDITOR.instances[changedElem.getAttribute("id")].getData().replace(/%/g, "%25").replace(/"/g, "\\\"").replace(/&/g, "%26") + '", "collection":"' + gs.cgiParams.c + '", "oid":"' + sectionID + '"}'); //'
			addCollectionToBuild(gs.cgiParams.c);
		}
	}

	
	var processChangesLoop = function(index)
	{
	  
		var change = metadataChanges[index];
		
		var callbackFunction;
		if(index + 1 == metadataChanges.length)
		{
			callbackFunction = sendBuildRequest;
		}
		else
		{
			callbackFunction = function(){processChangesLoop(index + 1)};
		}
		if (change.type == "delete") {
		  gs.functions.removeArchivesMetadata(collection, gs.xsltParams.site_name, change.docID, change.name, null, change.value, function(){callbackFunction();});
		} else {
		  if(change.orig)
		    {
		      gs.functions.setArchivesMetadata(change.collection, gs.xsltParams.site_name, change.docID, change.name, null, change.value, change.orig, "override", function(){callbackFunction();});
		    }
		  else
		    {
		      gs.functions.setArchivesMetadata(change.collection, gs.xsltParams.site_name, change.docID, change.name, null, change.value, null, "accumulate", function(){callbackFunction();});
		    }
		}
	}
	if (metadataChanges.length>0) {
	  // this will process each change one by one, and then send the build request
	  processChangesLoop(0);
	}
	else if(_collectionsToBuild.length > 0) {
	  // if there are no metadata changes, but some other changes eg text have happened, then we need to send the build request.
	  sendBuildRequest();
	}
	  
	/* need to clear the changes from the page so that we don't process them again next time */
	while (_deletedMetadata.length>0) {
	  _deletedMetadata.pop();
	}

}


function buildCollections(collections, documents, callback)
{
	if(!collections || collections.length == 0)
	{
		console.log(gs.text.dse.empty_collection_list);
		enableSaveButtons(true);
		return;
	}
	
	var docs = "";
	var buildOperation = "";
	if(documents)
	{
		buildOperation = "ImportCollection";
		docs += "&s1.documents=";
		for(var i = 0; i < documents.length; i++)
		{
			docs += documents[i];
			if(i < documents.length - 1)
			{			
				docs += ",";
			}
		}
	}
	else
	{
		buildOperation = "BuildAndActivateCollection";
	}

	var counter = 0;
	var statusID = 0;
	var buildFunction = function()
	{
		var ajax = new gs.functions.ajaxRequest();
		ajax.open("GET", gs.xsltParams.library_name + "?a=g&rt=r&ro=1&s=" + buildOperation + "&s1.incremental=true&s1.collection=" + collections[counter] + docs);
		ajax.onreadystatechange = function()
		{
			if(ajax.readyState == 4 && ajax.status == 200)
			{
				var text = ajax.responseText;
				var xml = validateXML(text);

				if(!xml || checkForErrors(xml))
				{
					alert(gs.text.dse.could_not_build_p1 + " " + collections[counter] + gs.text.dse.could_not_build_p2);
					
					if(_statusBar)
					{
						_statusBar.removeStatus(statusID);
					}
					enableSaveButtons(true);
					
					return;
				}

				var status = xml.getElementsByTagName("status")[0];
				var pid = status.getAttribute("pid");

				startCheckLoop(pid, buildOperation, statusID, function()
				{
					/*
					var localAjax = new gs.functions.ajaxRequest();
					localAjax.open("GET", gs.xsltParams.library_name + "?a=g&rt=r&ro=1&s=ActivateCollection&s1.collection=" + collections[counter], true);
					localAjax.onreadystatechange = function()
					{
						if(localAjax.readyState == 4 && localAjax.status == 200)
						{
							var localText = localAjax.responseText;
							var localXML = validateXML(localText);
							
							if(!xml || checkForErrors(xml))
							{
								alert(gs.text.dse.could_not_activate_p1 + " " + collections[counter] + gs.text.dse.could_not_activate_p2);
								
								if(_statusBar)
								{
									_statusBar.removeStatus(statusID);
								}
								enableSaveButtons(true);
								
								return;
							}

							var localStatus = localXML.getElementsByTagName("status")[0];
							if(localStatus)
							{
								var localPID = localStatus.getAttribute("pid");
								startCheckLoop(localPID, "ActivateCollection", statusID, function()
								{
								*/
									if(counter == collections.length - 1)
									{
										removeCollectionsFromBuildList(collections);
										if(callback)
										{
											callback();
										}
									}
									else
									{
										counter++;
										buildFunction();
									}

									_transactions = new Array();

									if(_statusBar)
									{
										_statusBar.removeStatus(statusID);
									}
									enableSaveButtons(true);
								/*
								});
							}
						}
					}
					if(_statusBar)
					{
						_statusBar.changeStatus(statusID, gs.text.dse.activating + " " + collections[counter] + "...");
					}
					localAjax.send();
					*/
				});
			}
		}
		if(_statusBar)
		{
			statusID = _statusBar.addStatus(gs.text.dse.building + " " + collections[counter] + "...");
		}
		ajax.send();
	}
	buildFunction();
}

function startCheckLoop(pid, serverFunction, statusID, callbackFunction)
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
				var xml = validateXML(text);
				
				if(!xml || checkForErrors(xml))
				{
					alert(gs.text.dse.could_not_check_status_p1 + " " + serverFunction + gs.text.dse.could_not_check_status_p2a);
					
					if(_statusBar)
					{
						_statusBar.removeStatus(statusID);
					}
					enableSaveButtons(true);
					
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
					alert(gs.text.dse.could_not_check_status_p1 + " " + serverFunction + gs.text.dse.could_not_check_status_p2b);
					
					if(_statusBar)
					{
						_statusBar.removeStatus(statusID);
					}
					enableSaveButtons(true);
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

function onVisibleMetadataSetChange()
{
	var metadataList = document.getElementById("metadataSetList");
	var index = metadataList.selectedIndex;
	var options = metadataList.getElementsByTagName("OPTION");
	var selectedOption = options[index];
	
	var selectedSet = selectedOption.value;
	changeVisibleMetadata(selectedSet);
}

function changeVisibleMetadata(metadataSetName)
{
        var metaSetList = metadataSetName.split(",");
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
						if (metaSetList.indexOf(setName)!= -1) 
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
	cell.originalValue = cell.value;
	setTimeout(function(){addEditableState(cell, editableInitStates)}, 0);
}

function addOptionToList(list, optionvalue, optiontext, selected) {
  var newOption = $("<option>");
  if (optiontext) {
    newOption.html(optiontext);
    newOption.attr("value", optionvalue);
  } else {
    newOption.html(optionvalue);
  }
  if (selected) {
    newOption.attr("selected", true);
  }
  list.append(newOption);
}

/* returns either an input or a select element. Data based on 
   availableMetadataElements var. */
function createMetadataElementSelector() {
  var metaNameField;
  if (new_metadata_field_input_type == "fixedlist") {
    metaNameField =  $("<select>", {"class": "ui-state-default"});
    for(var i=0; i<availableMetadataElements.length; i++) {
      addOptionToList(metaNameField, availableMetadataElements[i]);
    }
    return metaNameField;
  }
  metaNameField = $("<input>", {"type": "text","style":"margin: 5px; border: 1px solid #000;"}); 
  if (new_metadata_field_input_type == "autocomplete") {
    metaNameField.autocomplete({
	minLength: 0,
	  source: availableMetadataElements
	  });
      metaNameField.attr("title", gs.text.de.enter_meta_dropdwon); //"Enter a metadata name, or use the down arrow to select one, then click 'Add New Metadata'");
  } else {
      metaNameField.attr("title", gs.text.de.enter_meta_name); //"Enter a metadata name, then click 'Add New Metadata'");
  }
  
  return metaNameField;
}



function addFunctionalityToTable(table)
{
	table.find("tr").each(function()
	{
		var cells = $(this).find("td");
		var metadataName = $(cells[0]).html();
		
		if(dynamic_metadata_set_list == true && metadataName.indexOf(".") != -1)
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
				addOptionToList( $("#metadataSetList"), metadataSetName);
			}
		}
			
		asyncRegisterEditSection(cells[1].getElementsByTagName("textarea")[0]);
		addRemoveLinkToRow(this);
	});

    // set up autocomplete values
    var value_cells = $(".metaTableCellArea");
    for (var k=0; k<autocompleteMetadata.length; k++) {
	var source_name = autocompleteMetadata[k].replace(/[\.-]/g, "");
	var source_obj = window[source_name+"_values"];
	if (source_obj) {
	    value_cells.filter("."+source_name).autocomplete({
		minLength: 0,
		source: source_obj
	    });
	}
    }
        var metaNameField = createMetadataElementSelector(); 	
	table.after(metaNameField);
	table.metaNameField = metaNameField;
	
	var addRowButton = $("<button>",{"class": "ui-state-default ui-corner-all", "style": "margin: 5px;"});
	addRowButton.html(gs.text.de.add_new_metadata);
	addRowButton.click(function() 
	{ 
	        var name = metaNameField.val();
		if(!name || name == "")
		{
			console.log(gs.text.de.no_meta_name_given);
			return;
		}
	        var clean_name = name.replace(/[\.-]/g, "");
		var newRow = $("<tr>", {"style": "display: table-row;"});
		var nameCell = $("<td>" + name + "</td>");
		nameCell.attr("class", "metaTableCellName");
		var valueCell = $("<td>", {"class": "metaTableCell"}); 	
	        var textValue = $("<textarea>", {"class": "metaTableCellArea "+ clean_name}); 
	    
	    
	        if (autocompleteMetadata.includes(name)) {
		    var source_obje = window[clean_name +"_values"];
		    if (source_obje) {
			textValue.autocomplete({
			    minLength: 0,
			    source: source_obje
			});
		    }
		}
		valueCell.append(textValue);
		newRow.append(nameCell);
		newRow.append(valueCell);
		addRemoveLinkToRow(newRow.get(0));
		table.append(newRow);
		
		var undo = new Array();
		undo.op = "delMeta";
		undo.srcElem = newRow;
		undo.removeTransaction = false;
		_undoOperations.push(undo);
		if ( hierarchyStorage && hierarchyStorage[name])
                {
                        setHierarchyEventsWrappers(name);
                }

		
	});
	table.addRowButton = addRowButton;
	metaNameField.after(addRowButton);

}

function addRemoveLinkToRow(row)
{
	var newCell = $("<td>");
	var removeLink = $("<a>"+gs.text.de.remove+"</a>", {"href": "javascript:;"});
	removeLink.click(function()
	{
		var undo = new Array();
		undo.srcElem = row;
		undo.op = "display";
		undo.subOp = "table-row";
		undo.removeDeletedMetadata = true;
		_undoOperations.push(undo);
		_deletedMetadata.push(row);
		//row.css("display", "none");
		$(row).hide();
	});
	newCell.append(removeLink);
	newCell.attr({"class": "metaTableCellRemove", "style": "font-size:0.6em; padding-left: 3px; padding-right: 3px;"});
	$(row).append(newCell);
}

/* This is for 'edit structure' menu bar */
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
    metadataListLabel.innerHTML = gs.text.de.visible_metadata; 
	var metadataList = document.createElement("SELECT");
	metadataList.setAttribute("id", "metadataSetList");
	metadataList.onchange = onVisibleMetadataSetChange;
	var allMetadataOption = document.createElement("OPTION");
	metadataList.appendChild(allMetadataOption);
    allMetadataOption.innerHTML = gs.text.de.all_metadata; 
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
	saveButton.innerHTML = gs.text.de.save_changes;
	saveButton.setAttribute("onclick", "saveAndRebuild();");
	saveButton.setAttribute("id", "saveButton");
	saveCell.appendChild(saveButton);
	
	//The "Undo" button
	var undoButton = document.createElement("BUTTON");
    undoButton.innerHTML = gs.text.dse.undo; 
	undoButton.setAttribute("onclick", "undo();");
	undoCell.appendChild(undoButton);

	//The "Create new document" button
	var newDocButton = document.createElement("BUTTON");
	newDocButton.innerHTML = gs.text.dse.create_new_document;
	newDocButton.setAttribute("onclick", "createNewDocumentArea();");
	newDocButton.setAttribute("id", "createNewDocumentButton");
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
	return "";
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
	if(typeof textDiv.style == "undefined" || typeof textDiv.style.display == "undefined" || textDiv.style.display == "block")
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
			section.menu.editTextLink.innerHTML = gs.text.dse.edit;
		}
		else
		{
			textDiv.style.display = "block";
			section.menu.editTextLink.innerHTML = gs.text.dse.hide;
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
		textSpan.appendChild(document.createTextNode(" [" + gs.text.dse.untitled_section + "] "));
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
	newSpan.innerHTML = gs.text.dse.insert_new_section + " ";
	
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
	
	this.clear = function()
	{
		for(var p in _statusMap)
		{
			if(_statusMap.hasOwnProperty(p))
			{
				if(_statusMap[p] && _statusMap[p].parentNode)
				{
					removeFromParent(_statusMap[p]);
				}
			
				if(--_activeMessages == 0)
				{
					_mainElem.style.display = "none";
				}
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
