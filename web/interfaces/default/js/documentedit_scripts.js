/** Javascript file for editing a single document's content - metadata and text */
/** uses other functions in documentedit_scripts_util.js */


/* some vars for document editing */
/* if true, will look through all the metadata for the document, and add each namespace into the list of metadata sets. If set to false, will only add in the ones defined in setStaticMetadataSets function (defined below) - override this function to make a custom list of sets */
var dynamic_metadata_set_list = true;
/* if false, will hide the metadata list selector. So the user will only get to see the default metadata set. */
var display_metadata_set_selector = true;
/* if true, will make the editing controls stay visible even on page scrolling */
var keep_editing_controls_visible = true;
/* Here you can choose which save buttons you like. Choose from 'save', 'rebuild', 'saveandrebuild' */
var save_and_rebuild_buttons = ["saveandrebuild"];
//var save_and_rebuild_buttons = ["save", "rebuild", "saveandrebuild"];

/* What kind of metadata element selection do we provide?
   plain: just a text input box
   fixedlist: a drop down menu with a fixed list of options (provided by the availableMetadataElements list)
   autocomplete: a text input box with a list of suggestions to choose from (provided by the availableMetadataElements list). Allows additional input other than the fixed list 
*/
var new_metadata_field_input_type = "plain";
/* Metadata elements to be used in the fixedlist/autocomplete options above */
var availableMetadataElements = ["dc.Title", "dc.Subject"];
/* metadata elements that have a list of values/suggestions */
var autocompleteMetadata = new Array();
/* for each metadata element specified here, one should provide an array of values. The name is the meta_name + "_values", but you must strip . and _ from the name.
for example
var autocompleteMetadata = ["dc.Subject"];
var dcSubject_values = ["Kings", "Queens", "others"];
*/


/************************
* METADATA EDIT SCRIPTS *
************************/

function addEditMetadataLink(cell)
{
	cell = $(cell);
	var id = cell.attr("id").substring(6);
	var metaTable = gs.jqGet("meta" + id);

	var row = cell.parent();
	var newCell = $("<td>", {"style": "font-size:0.7em; padding:0px 10px", "class": "editMetadataButton"});
	var linkSpan = $("<span>", {"class": "ui-state-default ui-corner-all", "style": "padding: 2px; float:left;"});
	
        var linkLabel = $("<span>"+gs.text.de.edit_metadata+"</span>");
 	var linkIcon = $("<span>", {"class": "ui-icon ui-icon-folder-collapsed"});
	newCell.linkIcon = linkIcon;
	newCell.linkLabel = linkLabel;
	
	var uList = $("<ul>", {"style": "outline: 0 none; margin:0px; padding:0px;"});
	var labelItem = $("<li>", {"style": "float:left; list-style:none outside none;"});
	var iconItem = $("<li>", {"style": "float:left; list-style:none outside none;"});

	uList.append(iconItem);
	uList.append(labelItem);
	labelItem.append(linkLabel);
	iconItem.append(linkIcon);
	
	var newLink = $("<a>", {"href": "javascript:;"});
	newLink.click(function()
	{
		if(metaTable.css("display") == "none")
		{
		    linkLabel.html(gs.text.de.hide_metadata);
			linkIcon.attr("class", "ui-icon ui-icon-folder-open");
			metaTable.css("display", "block");
			metaTable.metaNameField.css("display", "inline");
			metaTable.addRowButton.css("display", "inline");
		}
		else
		{
		    linkLabel.html(gs.text.de.edit_metadata);
			linkIcon.attr("class", "ui-icon ui-icon-folder-collapsed");
			metaTable.css("display", "none");
			metaTable.metaNameField.css("display", "none");
			metaTable.addRowButton.css("display", "none");
		}
	});

	newLink.append(uList);
	linkSpan.append(newLink);
	newCell.append(linkSpan);
	row.append(newCell);
	
	addFunctionalityToTable(metaTable);
	metaTable.metaNameField.css("display", "none");
	metaTable.addRowButton.css("display", "none");
}

function setEditingFeaturesVisible(visible)
{
	if(visible)
	{
	    $("#editContentButton").html(gs.text.de.hide_editor); 
		$("#editContentButtonDiv").attr("class", "ui-state-default ui-corner-all");
	}
	else
	{
	    $("#editContentButton").html(gs.text.de.edit_content); 
		$("#editContentButtonDiv").attr("class", "");
	}
	
	var visibility = (visible ? "" : "none");
    if (display_metadata_set_selector == true) {
	$("#metadataListLabel, #metadataSetList").css("display", visibility);
    }
	$(".editMetadataButton").each(function()
	{
		$(this).css("display", visibility);
	    $(this.linkLabel).html(gs.text.de.edit_metadata); 
		$(this.linkIcon).attr("class", "ui-icon ui-icon-folder-collapsed");
	});
	
	$("table").each(function()
	{
		if($(this).attr("id") && $(this).attr("id").search(/^meta/) != -1)
		{
			$(this).css("display", "none");
			$(this.metaNameField).css("display", "none");
			$(this.addRowButton).css("display", "none");
		}
	});
}

/* override this function in other interface/site/collection if you want
   a different set of metadata sets 
  Use in conjunction with the dynamic_metadata_set_list variable. */
function setStaticMetadataSets(list) {
  addOptionToList(list, "All", gs.text.de.all_metadata);
}

function readyPageForEditing()
{
    CKEDITOR.on('instanceReady', function(evt) {
	addCKEEditableState(evt,editableInitStates);
    });

	if($("#metadataSetList").length)
	{
		var setList = $("#metadataSetList");
		if(!setList.css("display") || setList.css("display") == "")
		{
			setEditingFeaturesVisible(false);
		}
		else
		{
			setEditingFeaturesVisible(true);
		}
		return;
	}

    $("#editContentButton").html(gs.text.de.hide_editor);
	//wait for 0.5 sec to let ckeditor up 
	//setTimeout(function(){ $(".sectionText").each(function(){addEditableState(this,editableInitStates);}); }, 500);	
	var editBar = $("#editBarLeft");
	

	var visibleMetadataList = $("<select>", {"id": "metadataSetList", "class": "ui-state-default"});
	setStaticMetadataSets(visibleMetadataList);
    
    if (display_metadata_set_selector == true) {
	var metadataListLabel = $("<span>", {"id": "metadataListLabel", "style": "margin-left:20px;"});
	metadataListLabel.html(gs.text.de.visible_metadata); 
	editBar.append(metadataListLabel);
    } else {
	visibleMetadataList.css ("display", "none");
    }
	editBar.append(visibleMetadataList);
	visibleMetadataList.change(onVisibleMetadataSetChange);
	editBar.append("<br>");
    
	for (var i=0; i< save_and_rebuild_buttons.length; i++) {
	  var button_type = save_and_rebuild_buttons[i];
	  if (button_type == "save") {
	    var saveButton = $("<button>", {"id": "saveButton", "class": "ui-state-default ui-corner-all"});
	    saveButton.click(save);
	    saveButton.html(gs.text.de.save);
	    editBar.append(saveButton);
	  } else if(button_type == "rebuild") {
	    var rebuildButton = $("<button>", {"id": "rebuildButton", "class": "ui-state-default ui-corner-all"});
	    rebuildButton.click(rebuildCurrentCollection);
	    rebuildButton.html(gs.text.de.rebuild);
	    editBar.append(rebuildButton);
	  } else if (button_type == "saveandrebuild") {
	    var saveAndRebuildButton = $("<button>", {"id": "saveAndRebuildButton", "class": "ui-state-default ui-corner-all"});
	    saveAndRebuildButton.click(saveAndRebuild);
	    saveAndRebuildButton.html(gs.text.de.saverebuild);
	    editBar.append(saveAndRebuildButton);

	  }
	}
	var statusBarDiv = $("<div>");
	editBar.append(statusBarDiv);
	_statusBar = new StatusBar(statusBarDiv[0]);
	
	var titleDivs = $(".sectionTitle");
	for(var i = 0; i < titleDivs.length; i++)
	{
		addEditMetadataLink(titleDivs[i]);
	}
	
	_baseURL = gs.xsltParams.library_name;
	onVisibleMetadataSetChange(); // make sure that the selected item in the list is active
}

// override the one in documentmaker_scripts_util
// currently not used if other one is present. need to get the js include order right
function enableSaveButtons(enabled) {
  if (enabled) {
    $("#saveButton, #rebuildButton, #saveAndRebuildButton").removeAttr("disabled");
  } else {
    $("#saveButton, #rebuildButton, #saveAndRebuildButton").attr("disabled", "disabled");
  }
}

/* this is a cut down version of save() from documentmaker_scripts_util.js 
 going back to using save, will delete this once everything working*/
function saveMetadataChangesOld() {

  console.log("Saving metadata changes");
 
  // get collection name
  var collection = gs.cgiParams.c;;

  // get document id
  var docID = gs.cgiParams.d;

  var metadataChanges = new Array();
  if (_deletedMetadata.length > 0) {

    for(var i = 0; i < _deletedMetadata.length; i++) {
      
      var currentRow = _deletedMetadata[i];
      
      //Get metadata name
      var cells = currentRow.getElementsByTagName("TD");
      var nameCell = cells[0];
      var name = nameCell.innerHTML;
      var valueCell = cells[1];
      var value = valueCell.innerHTML;
      metadataChanges.push({type:'delete', docID:docID, name:name, value:value});
      removeFromParent(currentRow);
    }
  }

  /*var changes = null;
	//var changes = de.Changes.getChangedEditableSections();
  for(var i = 0; i < changes.length; i++) {
    
    var changedElem = changes[i];
		
    //Get metadata name
    var row = changedElem.parentNode;
    var cells = row.getElementsByTagName("TD");
    var nameCell = cells[0];
    var name = nameCell.innerHTML;
    var value = changedElem.innerHTML;
    value = value.replace(/&nbsp;/g, " ");
    
    var orig = changedElem.originalValue;
    if (orig) {
      orig = orig.replace(/&nbsp;/g, " ");
    }
    metadataChanges.push({collection:collection, docID:docID, name:name, value:value, orig:orig});
    changedElem.originalValue = changedElem.innerHTML;
    
  }
*/
  if (metadataChanges.length ==0) {
      console.log(gs.text.de.no_changes);
    return;
  }

  var processChangesLoop = function(index)
    {
      var change = metadataChanges[index];
      
      var callbackFunction;
      if(index + 1 == metadataChanges.length)
	{
	  callbackFunction = function(){console.log("Completed saving metadata changes. You must rebuild the collection for the changes to take effect.");};
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
	    gs.functions.setArchivesMetadata(collection, gs.xsltParams.site_name, docID, change.name, null, change.value, change.orig, "override", function(){callbackFunction();});
	  }
	else
	  {
	    gs.functions.setArchivesMetadata(collection, gs.xsltParams.site_name, docID, change.name, null, change.value, null, "accumulate", function(){callbackFunction();});
	  }
      }
    }
  processChangesLoop(0);
  /* need to clear the changes from the page */
  while (_deletedMetadata.length>0) {
    _deletedMetadata.pop();
  }
    
}


/************************************
*    TEXT EDIT (CKEDITOR) SCRIPTS    *
**************************************/

function addCKEEditableState(evt,stateArray) 
{
    // Event->Editor->CKE DOM Inline Element that editor was for->underlying jquery element 
    element = evt.editor.element.$;
    nodeText = element.innerHTML;
         stateArray.push({
             editableNode : element,
             initHTML : nodeText
         });
   
}
function addEditableState(editable,stateArray)
{

	if(editable.tagName == 'TEXTAREA')
	{
		nodeText = editable.value;
	}
	else 
	{
	 	nodeText = editable.innerHTML;
	}

        stateArray.push({
                editableNode : editable,
                initHTML : nodeText
        });

}

function getLastEditableStates()
{	
	editableLastStates = [];
        $(".sectionText").each(function(){addEditableState(this,editableLastStates);});
        $(".metaTableCellArea").each(function(){addEditableState(this,editableLastStates);});

}

function changesToUpdate() 
{
	var resultArray = new Array();
	getLastEditableStates();
	for (var j in editableLastStates) 
	{	
		if (isNodeChanged(editableLastStates[j])) 
		{
			resultArray.push(editableLastStates[j].editableNode);
		}
	}
	return resultArray;
}


function isNodeChanged(StateToCheck){
	for (var i in editableInitStates) 
	{
	    if ((StateToCheck.editableNode === editableInitStates[i].editableNode)) {
		if ( StateToCheck.initHTML === editableInitStates[i].initHTML ) 
		{
			return false;
		}
		return true;
	    }
	
	}
	return true;
}

