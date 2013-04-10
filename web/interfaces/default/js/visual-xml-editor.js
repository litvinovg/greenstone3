// ********************************************************************** //
// Visual XML Editor                                                      //
// This class represents an editor that allows you to modify XML visually //
// ********************************************************************** //
function visualXMLEditor(xmlString)
{
	//Variables that store the visual editor and a link to the DebugWidget
	var _thisEditor = this;
	var _greenbug;
	
	//Store this file's location and name
	var _fileLocation;
	var _fileName;

	//Stores what id we are up to (used to compare VEElements)
	var _globalID = 0;

	//Stores the current state of the XML
	var _xml;

	//Elements of the editor
	var _mainDiv = $("<div>", {"id":"veMainDiv"});
	var _toolboxDiv = $("<div>", {"id":"veToolboxDiv"});
	var _editorContainer = $("<div>", {"id":"veEditorContainer"});
	var _editorDiv = $("<div>", {"id":"veEditorDiv"});
	var _infoDiv = $("<div>", {"id":"veInfoDiv"});

	//State-keeping variables
	var _rootElement;
	var _selectedElement;
	var _overTrash = false;
	var _validDropSpot = false;
	var _validDropType;
	var _validDropElem;
	var _origDDParent;
	var _origDDPosition;

	//Keep track of what is currently being edited
	var _editingNodes = new Array();

	//Stores what elements we are currently over while dragging (necessary to find the deepest element)
	var _overList = new Array();
	_overList.freeSpaces = new Array();

	//Keep a list of what has been changed so that it can be undone
	var _transactions = new Array();

	//A list of "ready-made" attributes for certain elements
	var _validAttrList = 
	{
		gsf:
		{
			"cgi-param":["name"],
			"collectionText":["args", "name"],
			"displayItem":["name"],
			"displayText":["name"],
			"equivlinkgs3":["name"],
			"foreach-metadata":["name", "separator"],
			"icon":["type"],
			"if-metadata-exists":["name"],
			"image":["type"],
			"interfaceText":["name"],
			"link":["OID", "OIDmetadata", "name", "nodeID", "target", "title", "titlekey", "type"],
			"metadata":["format", "hidden", "name", "pos", "prefix", "separator", "suffix", "type"],
			"script":["src"],
			"style":["src"],
			"switch":["preprocess", "test", "test-value"]
		}
	}

	//The list of elements that show up in the toolbox (gslib is added dynamically later)
	var _elemList = 
	{
		html:["a", "br", "div", "li", "link", "p", "script", "span", "table", "td", "tr", "ul"],
		xsl:
		[
			"apply-imports", "apply-templates", "attribute", "attribute-set", "call-template", 
			"choose", "copy", "copy-of", "decimal-format", "element", 
			"fallback", "for-each", "if", "import", "include",
			"key", "message", "namespace-alias", "number", "otherwise",
			"output", "param", "preserve-space", "processing-instruction", "sort",
			"strip-space", "stylesheet", "template", "text", "transform",
			"value-of", "variable", "when", "with-param"
		],
		gsf:
		[
			"cgi-param", "choose-metadata", "collectionText", "displayItem", "displayText",
			"equivlinkgs3", "foreach-metadata", "icon", "if-metadata-exists", "image",
			"interfaceText", "link", "meta-value", "metadata", "script",
			"style", "switch", "template", "text", "variable"
		]
	};

	//Restricts what elements can be added to a given element
	var _childRestrictions = 
	{
		gsf:
		{
			"choose-metadata":["gsf:metadata", "gsf:default"],
			"metadata":[]
		}
	};

	this.setFileLocation = function(fileLocation)
	{
		_fileLocation = fileLocation;
	}
	
	this.setFileName = function(fileName)
	{
		_fileName = fileName;
	}

	//Get a connection to the DebugWidget
	this.setGreenbug = function(gb)
	{
		_greenbug = gb;
	}

	//Get the XML in its current state
	this.getXML = function()
	{
		return _xml;
	}

	//Undo a transation
	this.undo = function()
	{
		if(_transactions.length > 0)
		{
			var t = _transactions.pop();
			//Undo an added element
			if(t.type == "addElem")
			{
				$(t.vElem.data("parentVEElement").getXMLNode()).remove();
				t.vElem.remove();
				resizeAll();
			}
			//Undo a removed or moved element
			else if(t.type == "remMvElem")
			{
				var parent = t.vElemParent;
				var pos = t.vElemPos;
				var elem = t.vElem;

				elem.detach();
				if(pos == 0)
				{
					parent.prepend(elem);
					$(parent.parent().data("parentVEElement").getXMLNode()).prepend(elem.data("parentVEElement").getXMLNode());
				}
				else if(pos == parent.children(".veElement").length)
				{
					$(parent.children(".veElement").eq(pos - 1).data("parentVEElement").getXMLNode()).after(elem.data("parentVEElement").getXMLNode());
					parent.children(".veElement").eq(pos - 1).after(elem);
				}
				else
				{
					$(parent.children(".veElement").eq(pos).data("parentVEElement").getXMLNode()).before(elem.data("parentVEElement").getXMLNode());
					parent.children(".veElement").eq(pos).before(elem);
				}
				resizeAll();

				//Check if we need to change the recycle bin icon
				var found = false;
				for(var i = 0; i < _transactions.length; i++)
				{
					if(_transactions[i].type == "remMvElem"){found = true; break;}
				}

				if(!found)
				{
					$("#veTrash").children("img").attr("src", gs.imageURLs.trashEmpty);
				}
			}
			//Undo an added attribute
			else if(t.type == "addAttr")
			{
				if(t.row)
				{
					t.row.remove();
				}
			}
			//Undo a removed or edited attribute
			else if(t.type == "editAttr")
			{
				t.elem.removeAttribute(t.newName);
				t.elem.setAttribute(t.name, t.value);
				if(t.row)
				{
					t.row.children("td").eq(0).text(t.name);
					t.row.children("td").eq(1).text(t.value);
				}
			}
			//Undo a removed or edited attribute
			else if(t.type == "remAttr")
			{
				t.elem.setAttribute(t.name, t.value);
				if(t.rowParent)
				{
					t.rowParent.append(t.row);
				}
			}
			//Undo edited text
			else if(t.type == "editText")
			{
				t.elem.nodeValue = t.value;
				if(t.vElem)
				{
					t.vElem.text(t.value);
				}
			}
		}
	}

	//Check if an element is allowed as a child to another element
	var checkRestricted = function(child, parent)
	{
		var pFullNodename = parent.tagName;
		var cFullNodename = child.tagName;
		var pNamespace;
		var pNodeName;
		if(pFullNodename.indexOf(":") == -1)
		{
			pNamespace = "no namespace";
			pNodeName = pFullNodename;
		}
		else
		{
			pNamespace = pFullNodename.substring(0, pFullNodename.indexOf(":"));
			pNodeName = pFullNodename.substring(pFullNodename.indexOf(":") + 1);
		}

		var namespaceList = _childRestrictions[pNamespace];
		if(namespaceList)
		{
			var childList = namespaceList[pNodeName];
			if(childList)
			{
				for(var i = 0; i < childList.length; i++)
				{
					if(childList[i] == cFullNodename)
					{
						return true;
					}
				}
				return false;
			}
		}

		return true;
	}

	//Add the trash bin to the editor
	var placeTrashBin = function()
	{
		var binImage = $("<img src=\"" + gs.imageURLs.trashEmpty + "\"/>");
		var bin = $("<div id=\"veTrash\">");
		bin.append(binImage);
		bin.addClass("ui-state-default");
		bin.addClass("ui-corner-all");
		bin.droppable(
		{
			"over":function()
			{
				_overTrash = true;
			},
			"out":function()
			{
				_overTrash = false;
			}
		});
		_editorContainer.append(bin);
	}

	//Dynamically retrieve the gslib elements from the gslib.xsl file to put into the toolbox
	var retrieveGSLIBTemplates = function(callback)
	{
		var url = gs.xsltParams.library_name + "?a=g&rt=r&s=GetTemplateListFromFile&s1.locationName=interface&s1.interfaceName=" + gs.xsltParams.interface_name + "&s1.fileName=gslib.xsl";

		$.ajax(url)
		.success(function(response)
		{
			startIndex = response.search("<templateList>") + "<templateList>".length;
			endIndex = response.search("</templateList>");

			if(startIndex == "<templateList>".length - 1)
			{
				console.log("Error retrieving GSLIB templates");
				return;
			}

			var listString = response.substring(startIndex, endIndex);
			var list = eval(listString.replace(/&quot;/g, "\""));
			var modifiedList = new Array();

			for(var i = 0; i < list.length; i++)
			{
				var current = list[i];
				if(current.name)
				{
					modifiedList.push(current.name);
				}
			}

			_elemList["gslib"] = modifiedList;

			if(callback)
			{
				callback();
			}
		})
		.error(function()
		{
			console.log("Error retrieving GSLIB templates");
		});
	}

	//Create the toolbar
	var populateToolbar = function()
	{
		var tabHolder = $("<ul>");
		_toolboxDiv.append(tabHolder);

		for(var key in _elemList)
		{
			var currentList = _elemList[key];

			var tab = $("<li>");
			var tabLink = $("<a>", {"href":"#ve" + key});
			tabLink.css({"font-size":"0.9em", "padding":"5px"});
			tabLink.text(key);
			tab.append(tabLink);
			tabHolder.append(tab);

			var tabDiv = $("<div>", {"id":"ve" + key});
			for(var j = 0; j < currentList.length; j++)
			{
				var elemName = currentList[j];

				var ns = (key == "html") ? "" : (key + ":");
				var newElem = _xml.createElement(ns + elemName);
				var veElement = new VEElement(newElem);
				veElement.setShortName(true);
				var veDiv = veElement.getDiv();
				veDiv.css("float", "none");
				veDiv.data("toolbar", true);
				tabDiv.append(veDiv);
			}

			_toolboxDiv.append(tabDiv);
		}

		var otherTab = $("<li>");
		var otherTabLink = $("<a>", {"href":"#veother"});
		otherTabLink.css({"font-size":"0.9em", "padding":"5px"});
		otherTabLink.text("other");
		otherTab.append(otherTabLink);
		tabHolder.append(otherTab);

		var otherTabDiv = $("<div>", {"id":"veother"});
		var textNode = _xml.createTextNode("text");
		var textVEElement = new VEElement(textNode);
		var textDiv = textVEElement.getDiv();
		textDiv.css("float", "none");
		textDiv.data("toolbar", true);
		otherTabDiv.append(textDiv);

		var customInput = $("<input type=\"text\">");
		var customElemHolder = $("<div>");
		var customCreateButton = $("<button>Create element</button>");
		customCreateButton.click(function()
		{
			var elemName = customInput.val();
			if(elemName.length)
			{
				var elem = _xml.createElement(elemName);
				var veElement = new VEElement(elem);
				var customElemDiv = veElement.getDiv();
				customElemDiv.css("float", "none");
				customElemDiv.data("toolbar", true);
				customElemHolder.empty();
				customElemHolder.append(customElemDiv);
			}
		});
		otherTabDiv.append(customInput);
		otherTabDiv.append(customCreateButton);
		otherTabDiv.append(customElemHolder);

		_toolboxDiv.append(otherTabDiv);

		_toolboxDiv.tabs();

		customCreateButton.button();
	}

	//Turns the given XML into the nice visual structure recursively
	var constructDivsRecursive = function(currentDiv, currentParent, level)
	{
		if(!level)
		{
			level = 1;
		}

		var container = $("<div>");
		container.addClass("veContainerElement");
		currentDiv.append(container);

		var allowedList = new Array();
		var counter = currentParent.firstChild;
		while(counter)
		{
			if(counter.nodeType == 1)
			{
				allowedList.push(counter)
			}
			else if(counter.nodeType == 3 && counter.nodeValue.search(/\S/) != -1)
			{
				allowedList.push(counter)
			}
			counter = counter.nextSibling;
		}

		var width = 100 / allowedList.length;
		for(var i = 0; i < allowedList.length; i++)
		{
			var currentElement = allowedList[i];

			var veElement = new VEElement(currentElement);
			var elementDiv = veElement.getDiv();
			veElement.setWidth(width);

			if(!_rootElement)
			{
				_rootElement = elementDiv;
			}

			container.append(elementDiv);
			if(currentElement.firstChild)
			{
				constructDivsRecursive(elementDiv, currentElement, level + 1);
			}

			currentElement = currentElement.nextSibling;
		}

		container.append($("<div>", {"style":"clear:both;"}));
	}

	//Fake a click on the root element
	this.selectRootElement = function()
	{
		var height = _editorDiv.height() + 10;
		if(height < 300){height = 300;}

		_editorContainer.css("height", height + "px");
		_infoDiv.css("height", height + "px");
		_rootElement.trigger("click");
	}

	//Return the main visual editor div
	this.getMainDiv = function()
	{
		return _mainDiv;
	}

	//Save any unfinished edits
	this.savePendingEdits = function()
	{
		while(_editingNodes && _editingNodes.length > 0)
		{
			var attr = _editingNodes.pop();
			attr.saveEdits();
		}
	}

	//Add the given VEElement to the list of VEElements we are currently dragging over
	var addToOverList = function(veElement)
	{
		if(veElement.getDiv().data("toolbar"))
		{
			return;
		}

		for(var i = 0; i < _overList.length; i++)
		{
			if(!_overList[i])
			{
				continue;
			}

			if(_overList[i].getID() == veElement.getID())
			{
				return false;
			}
		}

		if(_overList.freeSpaces.length > 0)
		{
			_overList[_overList.freeSpaces.pop()] = veElement;
		}
		else
		{
			_overList.push(veElement);
		}
	}

	//Remove the given VEElement from the list of VElements we are currently dragging over
	var removeFromOverList = function(veElement)
	{
		for(var i = 0; i < _overList.length; i++)
		{
			if(!_overList[i])
			{
				continue;
			}

			if(_overList[i].getID() == veElement.getID())
			{
				delete _overList[i];
				_overList.freeSpaces.push(i);
			}
		}
	}

	//Get the deepest VEElement we are currently dragging over
	var getDeepestOverElement = function()
	{
		if(_overList.length == 0)
		{
			return null;
		}

		var deepestVal = 0;
		var deepestElem = _overList[0];

		for(var i = 0; i < _overList.length; i++)
		{
			if(!_overList[i])
			{
				continue;
			}

			var depth = _overList[i].getDiv().parents(".veElement").length;
			depth = (depth) ? depth : 0;

			if (depth > deepestVal)
			{
				deepestVal = depth;
				deepestElem = _overList[i];
			}
		}

		return deepestElem;
	}

	//Resize all the VEElements
	var resizeAll = function()
	{
		var filterFunction = function()
		{
			var toolbarStatus = $(this).data("toolbar");
			var beingDraggedStatus = $(this).data("dragging");

			if(beingDraggedStatus || !toolbarStatus)
			{
				return true;
			}

			return false;
		}

		var allElems = $(".veElement").filter(filterFunction).each(function()
		{
			if($(this).data("helper")){return;}

			var size = $(this).data("expanded");
			if(size == "small")
			{
				var width = (20 / ($(this).siblings(".veElement").filter(function(){return !($(this).data("helper"))}).length - 1));
				$(this).css("width", width + "%");
			}
			else if(size == "normal")
			{
				var width = (100 / ($(this).siblings(".veElement").filter(function(){return !($(this).data("helper"))}).length + 1));
				$(this).css("width", width + "%");
			}
			else if(size == "expanded")
			{
				$(this).css("width", "80%");
			}
		});
	}

	//Initialise the visual editor
	var initVXE = function()
	{
		try
		{
			_xml = $.parseXML('<testContainer xmlns:xslt="http://www.w3.org/1999/XSL/Transform" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:java="http://xml.apache.org/xslt/java" xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil" xmlns:gslib="http://www.greenstone.org/skinning" xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat">' + xmlString + "</testContainer>");
			constructDivsRecursive(_editorDiv, _xml.firstChild);
		}
		catch(error)
		{
			console.log(error);
			return null;
		}

		retrieveGSLIBTemplates(function(){populateToolbar();});
		placeTrashBin();

		_editorContainer.append(_editorDiv);
		_mainDiv.append(_toolboxDiv);
		_mainDiv.append(_editorContainer);
		_mainDiv.append($("<div>", {"id":"veSpacerDiv"}));
		_mainDiv.append(_infoDiv);
		_mainDiv.append($("<div>", {"style":"clear:both;"}));		
	}

	// *********************************************************************** //
	// Visual Editor Text                                                      //
	// This inner class represents a single xml text node in the visual editor //
	// *********************************************************************** //
	var VEText = function(node)
	{
		//Constructor code
		var _thisNode = this;
		var _xmlNode = node;

		var _textEditor = $("<div>");
		var _nodeText = $("<div>");
		_nodeText.text(_xmlNode.nodeValue);

		_textEditor.append(_nodeText);

		var _editButton = $("<button>Edit text</button>");
		_editButton.click(function()
		{
			if(_editButton.button("option", "label") == "Edit text")
			{
				_thisNode.editMode();
			}
			else
			{
				_thisNode.saveEdits();
			}
		});
		_textEditor.append(_editButton);

		//Enable editing of this text node
		this.editMode = function()
		{
			_editingNodes.push(_thisNode);
			_nodeText.data("prevTextValue", _nodeText.text());
			var textArea = $("<textarea>");
			textArea.val(_nodeText.text());
			_nodeText.text("");
			_nodeText.append(textArea);
			_editButton.button("option", "label", "Done");
		}

		//Save edits to this text node
		this.saveEdits = function()
		{
			for(var i = 0; i < _editingNodes.length; i++)
			{
				if(_editingNodes[i] == _thisNode)
				{
					_editingNodes.splice(i, 1);
					break;
				}
			}

			_transactions.push({type:"editText", elem:_xmlNode, vElem: _nodeText, value:_nodeText.data("prevTextValue")});
			var textArea = _nodeText.find("textarea");
			var newValue = textArea.val();
			_xmlNode.nodeValue = newValue;
			_nodeText.empty();
			_nodeText.text(newValue);
			_editButton.button("option", "label", "Edit text");
		}

		//Create a text node editor
		this.getDiv = function()
		{
			//A hack to make sure the edit text button is styled correctly
			setTimeout(function(){_editButton.button()}, 1);
			return _textEditor;
		}
	}

	// *********************************************************************** //
	// Visual Editor Attribute                                                 //
	// This inner class represents a single xml attribute in the visual editor //
	// *********************************************************************** //
	var VEAttribute = function(attrElem, xmlElem, name, value)
	{
		//Constructor code
		var _name;
		if(name)
		{
			_name = name;
		}
		else if(attrElem && attrElem.name)
		{
			_name = attrElem.name;
		}

		var _value;
		if(value)
		{
			_value = value;
		}
		else if(attrElem && attrElem.value)
		{
			_value = attrElem.value;
		}
		var _xmlElem = xmlElem;
		var _row;

		var _thisAttr = this;

		//Get the attribute name
		this.getName = function()
		{
			return _name;
		}

		//Get the attribute value
		this.getValue = function()
		{
			return _value;
		}

		//Get the name cell of the attribute table
		var createNameCell = function()
		{
			var cell = $("<td>", {"class":"veNameCell"});
			cell.text(_name);
			return cell;
		}

		//Get the value cell of the attribute table
		var createValueCell = function()
		{
			var cell = $("<td>", {"class":"veValueCell"});
			cell.text(_value);
			return cell;
		}

		//Get the edit cell of the attribute table
		var createEditCell = function()
		{
			var cell = $("<td>", {"class":"veEditCell"});
			var link = $("<a href=\"javascript:;\">edit</a>");

			link.click(function()
			{
				_thisAttr.editMode();
			});
			cell.append(link);
			return cell;
		}

		//Get the delete cell of the attribute table
		var createDeleteCell = function()
		{
			var cell = $("<td>", {"class":"veDeleteCell"});
			var link = $("<a href=\"javascript:;\">delete</a>");
			link.click(function()
			{
				_transactions.push({type:"remAttr", elem:_xmlElem, row:_row, rowParent:_row.parent(), name:_name, value:_value});
				_xmlElem.removeAttribute(_name);
				_row.detach();	
			});
			cell.append(link);
			return cell;
		}

		//Create a table row from this attribute
		this.getAsTableRow = function()
		{
			var tableRow = $("<tr>");

			var attributeName = createNameCell();
			tableRow.append(attributeName);

			var attributeValue = createValueCell();
			tableRow.append(attributeValue);

			var editCell = createEditCell()
			tableRow.append(editCell);

			var deleteCell = createDeleteCell();
			tableRow.append(deleteCell);

			_row = tableRow;

			return tableRow;
		}

		//Enable editing of this attribute
		this.editMode = function(editValue)
		{
			_editingNodes.push(_thisAttr);

			var nameCell = _row.children("td").eq(0);
			var valueCell = _row.children("td").eq(1);
			var editLink = _row.children("td").eq(2).find("a");

			editLink.text("done");
			editLink.off("click");
			editLink.click(function()
			{
				_thisAttr.saveEdits();
			});

			var nameInput = $("<input type=\"text\">");
			nameInput.width(nameCell.width() - 5);
			nameInput.val(_name);

			var valueInput = $("<input type=\"text\">");
			valueInput.width(valueCell.width() - 5);
			valueInput.val(_value);

			nameCell.text("");
			valueCell.text("");

			nameCell.append(nameInput);
			valueCell.append(valueInput);

			if(editValue)
			{
				valueInput.focus();
			}
			else
			{
				nameInput.focus();
			}
		}

		//Save edits to this attribute
		this.saveEdits = function()
		{
			for(var i = 0; i < _editingNodes.length; i++)
			{
				if(_editingNodes[i] == _thisAttr)
				{
					_editingNodes.splice(i, 1);
					break;
				}
			}

			var nameCell = _row.children("td").eq(0);
			var valueCell = _row.children("td").eq(1);
			var editLink = _row.children("td").eq(2).find("a");

			editLink.text("edit");
			editLink.off("click");
			editLink.click(function()
			{
				_thisAttr.editMode();
			});

			var nameInput = nameCell.children("input");
			var valueInput = valueCell.children("input");

			nameInput.blur();
			valueInput.blur();

			var name = nameInput.val();
			var value = valueInput.val();

			if(name.length == 0 || name.search(/\w/g) == -1)
			{
				_row.remove();
				return;
			}

			nameCell.empty();
			nameCell.text(name);

			valueCell.empty();
			valueCell.text(value);

			if(nameCell.data("prevName") != "")
			{
				_xmlElem.removeAttribute(_name);
			}
			_xmlElem.setAttribute(name, value);

			_transactions.push({type:"editAttr", elem:_xmlElem, row:_row, newName:name, name:_name, value:_value});

			_name = name;
			_value = value;
		}
	}
	
	// ********************************************************************************** //
	// Visual Editor Element                                                              //
	// This inner class represents a single xml element or text node in the visual editor //
	// ********************************************************************************** //
	var VEElement = function(xml)
	{
		var _div = $("<div>");
		var _xmlNode = xml;
		var _id = _globalID++;

		_div.data("parentVEElement", this);
		_div.data("expanded", "normal");

		//Add the necessary functions to make this VEElement draggable
		var makeDraggable = function()
		{
			_div.draggable(
			{
				"distance":"20",
				"revert":"true", 
				"helper":function()
				{
					//Make sure the cursor is put in the centre of the div
					var height = _div.children(".veTitleElement").height();
					_div.draggable("option", "cursorAt", {top:(height / 2), left:(_div.width() / 2)});

					var tempVEE = new VEElement(_xmlNode);
					var tempDiv = tempVEE.getDiv();
					tempDiv.css("border", "1px solid orangered");
					tempDiv.css("background", "orange");
					tempDiv.width(_div.width());
					tempDiv.data("helper", true);
					return tempDiv;
				},
				"cursor":"move",
				"appendTo":_mainDiv,
				"start":function(event, ui)
				{
					_overTrash = false;
					_origDDParent = _div.parent();
					_origDDPosition = _div.index();

					_div.siblings(".veElement").filter(function(){return !($(this).data("helper"))}).data("expanded", "normal");
					_div.css("border", "1px solid orangered");
					_div.data("prevBackground", _div.css("background"));
					_div.css("background", "orange");
					_div.css("float", "left");

					_div.data("dragging", true);
					if(_div.data("toolbar"))
					{
						var cloneElem = new VEElement(_xmlNode.cloneNode(true));
						cloneElem.setShortName(true);
						var cloneDiv = cloneElem.getDiv();
						cloneDiv.css("float", "none");
						cloneDiv.data("toolbar", true);
						_div.before(cloneDiv);
					}
					_div.detach();

					resizeAll();
				},
				"drag":function(event, ui)
				{
					var foundDefined = false;
					for(var i = 0; i < _overList.length; i++)
					{
						if(!(_overList[i] === "undefined"))
						{
							foundDefined = true;
						}
					}

					_validDropSpot = false;
					if(foundDefined)
					{
						var overElement = getDeepestOverElement();
						if(overElement && overElement.getXMLNode().nodeType != 3 && checkRestricted(_xmlNode, overElement.getXMLNode()))
						{
							_validDropSpot = true;
							var overDiv = overElement.getDiv();

							var overLeft = overDiv.offset().left;
							var helperLeft = ui.helper.offset().left;
							var helperMiddle = helperLeft + (ui.helper.width() / 2);

							var overContainers = overDiv.children(".veContainerElement");
							if(!overContainers.length)
							{
								overDiv.append($("<div>", {"class":"veContainerElement"}));
								overContainers = overDiv.children(".veContainerElement");
							}
							var overChildren = overContainers.children(".veElement").filter(function(){return !($(this).data("helper")) && !(_div.data("parentVEElement").getID() == $(this).data("parentVEElement").getID())});
							var overChildrenLength = overChildren.length + 1;

							if(!overChildren.length)
							{
								_validDropElem = overDiv;
								_validDropType = "into";
								overContainers.append(_div);
							}
							else
							{
								var posPercent = (helperMiddle - overLeft) / overDiv.width();
								if(posPercent < 0)
								{
									posPercent = 0;
								}
								else if(posPercent > 1)
								{
									posPercent = 1;
								}
								var pos = Math.floor(overChildrenLength * posPercent);

								if(pos < overChildrenLength - 1)
								{
									_validDropElem = overChildren.eq(pos);
									_validDropType = "before";
									overChildren.eq(pos).before(_div);
								}
								else
								{
									_validDropElem = overChildren.eq(pos - 1);
									//Necessary to fix a rare bug that causes pos to be off by one
									if(!_validDropElem.length)
									{
										_validDropElem = overChildren.eq(pos - 2);
									}
									_validDropType = "after";
									overChildren.eq(pos - 1).after(_div);
								}
							}

							overChildren.data("expanded", "normal");
							_div.data("expanded", "normal");

							resizeAll();
						}
					}
				},
				"stop":function(event)
				{
					var transactionType = (_div.data("toolbar")) ? "addElem" : "remMvElem";

					if(_div.data("toolbar"))
					{
						_div.data("parentVEElement").setShortName(false);
					}

					_div.css("border", "1px solid black");
					_div.css("background", _div.data("prevBackground"));

					//If the element was not dropped in a valid place then put it back
					if(!_validDropSpot && !_div.data("toolbar"))
					{
						_div.detach();
						if(_origDDPosition == 0)
						{
							_origDDParent.prepend(_div);
						}
						else if(_origDDPosition == _origDDParent.children(".veElement").length)
						{
							_origDDParent.children(".veElement").eq(_origDDPosition - 1).after(_div);
						}
						else
						{
							_origDDParent.children(".veElement").eq(_origDDPosition).before(_div);
						}

						if(_overTrash)
						{
							_div.data("parentVEElement").remove();
							return;
						}

						resizeAll();
					}
					//Otherwise modify the XML
					else
					{
						var xmlNode = _validDropElem.data("parentVEElement").getXMLNode();
						if(_validDropType == "before")
						{
							$(xmlNode).before(_xmlNode);
						}
						else if (_validDropType == "after")
						{
							$(xmlNode).after(_xmlNode);
						}
						else if (_validDropType == "into")
						{
							$(xmlNode).append(_xmlNode);
						}
						_transactions.push({type:transactionType, vElemParent:_origDDParent, vElemPos:_origDDPosition, vElem:_div});
					}

					_div.data("dragging", false);
					_div.data("toolbar", false);

					_overList = new Array();
					_overList.freeSpaces = new Array();
				}
			});

			//Also make this element a drop-zone for other elements
			_div.droppable(
			{
				"over":function(event, ui)
				{
					addToOverList($(this).data("parentVEElement"));
					event.stopPropagation();
				},
				"out":function(event)
				{
					removeFromOverList($(this).data("parentVEElement"));
					event.stopPropagation();
				}
			});
		}

		//Get the underlying div
		this.getDiv = function()
		{
			return _div;
		}

		//Get the XML for this element
		this.getXMLNode = function()
		{
			return _xmlNode;
		}

		//Get the unique ID of this VEElement
		this.getID = function()
		{
			return _id;
		}

		//Fill the information area with details about this element
		this.populateInformationDiv = function()
		{
			_thisEditor.savePendingEdits();
			_infoDiv.empty();

			if(_xmlNode.nodeType == 1)
			{
				var nameElementTitle = $("<div>", {"class":"ui-state-default ui-corner-all veInfoDivTitle"}).text("Element name:");
				_infoDiv.append(nameElementTitle);
				_infoDiv.append($("<p>").text(_xmlNode.nodeName));
			}
			else
			{
				var textElementTitle = $("<div>", {"class":"ui-state-default ui-corner-all veInfoDivTitle"}).text("Text node:");
				_infoDiv.append(textElementTitle);
			}

			if(_xmlNode.nodeType == 1)
			{
				var attributeTableTitle = $("<div>", {"class":"ui-state-default ui-corner-all veInfoDivTitle"});
				attributeTableTitle.text("Attributes:");
				var attributeTable = $("<table>");
				attributeTable.addClass("veAttributeTableContainer");

				attributeTable.append($("<tr>").html("<td class=\"veNameCell\">Name</td><td class=\"veValueCell\">Value</td>"));

				$(_xmlNode.attributes).each(function()
				{
					var veAttribute = new VEAttribute(this, _xmlNode);
					attributeTable.append(veAttribute.getAsTableRow());
				});

				_infoDiv.append(attributeTableTitle);
				_infoDiv.append(attributeTable);

				var addDiv = $("<div>", {"class":"veInfoDivTitle"});
				var addSelect = $("<select>");
				addSelect.append("<option>[blank]</option>", {value:"[blank]"});
				var fullName = _xmlNode.tagName;
				var namespace;
				var nodeName;
				if(fullName.indexOf(":") == -1)
				{
					namespace = "no namespace";
					nodeName = fullName;
				}
				else
				{
					namespace = fullName.substring(0, fullName.indexOf(":"));
					nodeName = fullName.substring(fullName.indexOf(":") + 1);
				}
				var nameList = _validAttrList[namespace];
				if(nameList)
				{
					var nameList = _validAttrList[namespace];
					var attrList = nameList[nodeName];
					if(attrList)
					{
						for(var i = 0; i < attrList.length; i++)
						{
							addSelect.append($("<option>" + attrList[i] + "</option>", {value:attrList[i]}));
						}
					}
				}

				var addButton = $("<button>Add attribute</button>");
				addButton.click(function()
				{
					var newAtt;
					var editModeValue;
					if(addSelect.find(":selected").val() == "[blank]")
					{
						newAtt = new VEAttribute(null, _xmlNode, "", "");
						editModeValue = false;
					}
					else
					{
						newAtt = new VEAttribute(null, _xmlNode, addSelect.find(":selected").val(), "");
						editModeValue = true;
					}
 
					var row = newAtt.getAsTableRow();
					attributeTable.append(row);
					newAtt.editMode(editModeValue);
					_transactions.push({type:"addAttr", row:row})
				});
				addDiv.append(addSelect);
				addDiv.append(addButton);
				_infoDiv.append(addDiv);
				addButton.button();

				if(_xmlNode.tagName == "xsl:call-template" && _xmlNode.getAttribute("name").length > 0)
				{
					var extraOptionsTitle = $("<div>", {"class":"ui-state-default ui-corner-all veInfoDivTitle"}).text("Additional options:");
					var visitTemplateOption = $("<button>View called template</button>");
					
					_infoDiv.append(extraOptionsTitle);
					_infoDiv.append(visitTemplateOption);
					
					visitTemplateOption.button();
					visitTemplateOption.click(function()
					{
						var url = gs.xsltParams.library_name + "?a=g&rt=r&s=ResolveCallTemplate&s1.interfaceName=" + gs.xsltParams.interface_name + "&s1.siteName=" + gs.xsltParams.site_name + "&s1.collectionName=" + gs.cgiParams.c + "&s1.fileName=" + _fileName + "&s1.templateName=" + _xmlNode.getAttribute("name");
						$.ajax(url)
						.success(function(response)
						{
							var startIndex = response.indexOf("<requestedTemplate>") + ("<requestedTemplate>").length;
							var endIndex = response.indexOf("</requestedTemplate>");
							
							if(endIndex != -1)
							{
								var templateFileName = response.substring(startIndex, endIndex);
								var file = _greenbug.fileNameToLocationAndName(templateFileName);
								_greenbug.changeCurrentTemplate(file.location, file.filename, "template", "xsl", _xmlNode.getAttribute("name"), null);
							}
							else
							{
								_greenbug.changeCurrentTemplate("interface", "gslib.xsl", "template", "xsl", _xmlNode.getAttribute("name"), null);
							}
						});
					});
				}
			}

			if(_xmlNode.nodeType == 3)
			{
				var textNode = new VEText(_xmlNode);
				_infoDiv.append(textNode.getDiv());
			}
		}

		//Add mouseover/out and click events to this element
		var addMouseEvents = function()
		{
			_div.mouseover(function(event)
			{
				event.stopPropagation();
				_div.css("border", "1px solid orange");
				var titleString = " ";
				if(_xmlNode.nodeType == 1)
				{
					for(var i = 0; i < _xmlNode.attributes.length; i++)
					{
						var current = _xmlNode.attributes[i];
						var name = current.name;
						var value = current.value;
						
						titleString += name + "=\"" + value + "\" ";
					}
				}
				else if(_xmlNode.nodeType == 3)
				{
					titleString = _xmlNode.nodeValue;
				}
				_div.attr("title", titleString);
			});
			_div.mouseout(function(event)
			{
				_div.css("border", "1px solid black");
				event.stopPropagation();
			});
			_div.click(function(event)
			{
				if(_selectedElement)
				{
					_selectedElement.css("border", _selectedElement.prevBorder);
				}
				_selectedElement = _div;
				_div.prevBorder = _div.css("border");
				_div.css("border", "red solid 1px");

				_div.data("parentVEElement").focus();
				_div.data("parentVEElement").populateInformationDiv();

				event.stopPropagation();
			});
		}

		//Check if we need to expand an element before we do
		var checkResizeNecessary = function()
		{
			var elemsToCheck = _div.find(".veTitleElement");
			for(var i = 0; i < elemsToCheck.length; i++)
			{
				var titleElem = elemsToCheck.eq(i);
				titleElem.html("<span>" + titleElem.html() + "</span>");
				var titleSpan = titleElem.children("span");

				var resizeNecessary = false;
				if(titleSpan.width() >= titleElem.parent().width())
				{
					resizeNecessary = true;
				}
				titleElem.html(titleSpan.text());
				
				if(resizeNecessary)
				{
					return true;
				}
			}
			return false;
		}

		//Remove this from the editor
		this.remove = function()
		{
			var divParent = _div.parents(".veElement");
			_transactions.push({type:"remMvElem", vElemParent:_div.parent(), vElemPos:_div.index(), vElem:_div});
			_div.data("expanded", "normal");
			$(_xmlNode).remove();
			_div.detach();
			_infoDiv.empty();

			if(divParent.length)
			{
				divParent.first().trigger("click");
			}

			$("#veTrash").children("img").attr("src", gs.imageURLs.trashFull);
		}

		//Expend this element horizontally
		this.expand = function()
		{
			var siblings = _div.siblings(".veElement");
			if(!(_div.data("expanded") == "expanded") && siblings.length && siblings.length > 0)
			{
				var sibWidth = 20 / siblings.length;
				siblings.each(function()
				{
					$(this).animate({width:sibWidth + "%"}, 900);
					$(this).data("expanded", "small");
				});

				_div.animate({width:"80%"}, 1000);
				_div.data("expanded", "expanded");
			}
		}

		//Evenly distribute the children of this node evenly
		this.evenlyDistributeChildren = function()
		{
			var children = _div.find(".veElement")
			.each(function()
			{
				$(this).data("expanded", "normal");
				var length = $(this).siblings(".veElement").filter(function(){return !($(this).data("helper"))}).length + 1;
				$(this).css("width", (100 / length) + "%");//$(this).animate({"width":(100 / length) + "%"}, 900);
			});
		}

		//Expand this node and any parents and evenly distribute its children
		this.focus = function()
		{
			if(checkResizeNecessary())
			{
				_div.data("parentVEElement").expand();

				var parents = _div.parents(".veElement");
				parents.each(function()
				{
					$(this).data("parentVEElement").expand();
				});
			}

			_div.data("parentVEElement").evenlyDistributeChildren();
		}

		//Set whether to use the short name for this element (i.e. without the namespace)
		this.setShortName = function(short)
		{
			if(short && _xmlNode.nodeType == 1 && _xmlNode.tagName.indexOf(":") != -1)
			{
				_div.children(".veTitleElement").text(_xmlNode.tagName.substring(_xmlNode.tagName.indexOf(":") + 1));
			}
			else if(!short)
			{
				_div.children(".veTitleElement").text(_xmlNode.tagName);
			}
		}

		//Set the width of this element
		this.setWidth = function(width)
		{
			_div.css("width", width + "%");
		}

		//Visual Editor Element constructor
		var initVEE = function()
		{
			_div.addClass("veElement");
			_div.addClass("ui-corner-all");
			makeDraggable();

			var titleText;
			if(_xmlNode.nodeType == 3 && _xmlNode.nodeValue.search(/\S/) != -1)
			{
				_div.addClass("veTextElement");
				titleText = "[text]";
			}
			else if (_xmlNode.nodeType == 1)
			{
				if(_xmlNode.tagName.search(/^xsl/) != -1)
				{
					_div.addClass("veXSLElement");
				}
				else if(_xmlNode.tagName.search(/^gsf/) != -1)
				{
					_div.addClass("veGSFElement");
				}
				else if(_xmlNode.tagName.search(/^gslib/) != -1)
				{
					_div.addClass("veGSLIBElement");
				}
				else
				{
					_div.addClass("veHTMLElement");
				}
				titleText = _xmlNode.tagName;
			}

			addMouseEvents();

			_div.append("<div class=\"veTitleElement\">" + titleText + "</div>");
		}

		initVEE();
	}

	//Call the constructor
	initVXE();
}