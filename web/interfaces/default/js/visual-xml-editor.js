// ********************************************************************** //
// Visual XML Editor                                                      //
// This class represents an editor that allows you to modify XML visually //
// ********************************************************************** //

function visualXMLEditor(xmlString)
{
	var _globalID = 0;

	var _xml;
	
	var _mainDiv = $("<div>", {"id":"veMainDiv"});
	var _toolboxDiv = $("<div>", {"id":"veToolboxDiv"});
	var _editorContainer = $("<div>", {"id":"veEditorContainer"});
	var _editorDiv = $("<div>", {"id":"veEditorDiv"});
	var _infoDiv = $("<div>", {"id":"veInfoDiv"});
	var _rootElement;
	var _selectedElement;

	var _validDropSpot = false;
	var _validDropType;
	var _validDropElem;
	
	var _origDDParent;
	var _origDDPosition;
	
	var _overList = new Array();
	_overList.freeSpaces = new Array();
	
	var _transactions = new Array();
	
	this.getXML = function()
	{
		return _xml;
	}
	
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
				if(pos == 0)
				{
					parent.prepend(elem);
					$(parent.parent().data("parentVEElement").getXMLNode()).prepend(elem.data("parentVEElement").getXMLNode());
				}
				else if(pos == parent.children(".veElement").length - 1)
				{
					parent.children(".veElement").eq(pos - 1).after(elem);
					$(parent.children(".veElement").eq(pos - 1).data("parentVEElement").getXMLNode()).after(elem.data("parentVEElement").getXMLNode());
				}
				else
				{
					parent.children(".veElement").eq(pos).before(elem);
					$(parent.children(".veElement").eq(pos).data("parentVEElement").getXMLNode()).before(elem.data("parentVEElement").getXMLNode());
				}
				resizeAll();
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
	
	var populateToolbar = function()
	{
		var elemList = 
		{
			keys:["html", "xsl", "gsf"], // NEED TO ADD GSLIB AT SOME POINT
			html:["a", "div", "li", "script", "span", "table", "td", "tr", "ul"],
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
			],
		};

		var tabHolder = $("<ul>");
		_toolboxDiv.append(tabHolder);

		for(var i = 0; i < elemList.keys.length; i++)
		{
			var key = elemList.keys[i];
			var currentList = elemList[key];

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
	}

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
	
	this.selectRootElement = function()
	{
		var height = _editorDiv.height() + 10;
		if(height < 300){height = 300;}
		
		_editorContainer.css("height", height + "px");
		_infoDiv.css("height", height + "px");
		_rootElement.trigger("click");
	}
	
	this.getMainDiv = function()
	{
		return _mainDiv;
	}
	
	var addToOverList = function(veElement)
	{
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
				var width = (10 / ($(this).siblings(".veElement").filter(function(){return !($(this).data("helper"))}).length - 1));
				$(this).css("width", width + "%");
			}
			else if(size == "normal")
			{
				var width = (100 / ($(this).siblings(".veElement").filter(function(){return !($(this).data("helper"))}).length + 1));
				$(this).css("width", width + "%");
			}
			else if(size == "expanded")
			{
				$(this).css("width", "90%");
			}
		});
	}

	var initVXE = function()
	{
		try
		{
			_xml = $.parseXML('<testContainer xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:java="http://xml.apache.org/xslt/java" xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil" xmlns:gslib="http://www.greenstone.org/skinning" xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat">' + xmlString + "</testContainer>");
			constructDivsRecursive(_editorDiv, _xml.firstChild);
		}
		catch(error)
		{
			console.log(error);
			return null;
		}

		populateToolbar();

		_editorContainer.append(_editorDiv);
		_mainDiv.append(_toolboxDiv);
		_mainDiv.append(_editorContainer);
		_mainDiv.append($("<div>", {"id":"veSpacerDiv"}));
		_mainDiv.append(_infoDiv);
		_mainDiv.append($("<div>", {"style":"clear:both;"}));
	}

	// *********************************************************************** //
	// Visual Editor Attribute                                                 //
	// This inner class represents a single xml attribute in the visual editor //
	// *********************************************************************** //

	var VEAttribute = function(attrElem, xmlElem, name, value)
	{
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

		this.getName = function()
		{
			return _name;
		}

		this.getValue = function()
		{
			return _value;
		}

		var createNameCell = function()
		{
			var cell = $("<td>", {"class":"veNameCell"});
			cell.text(_name);
			return cell;
		}

		var createValueCell = function()
		{
			var cell = $("<td>", {"class":"veValueCell"});
			cell.text(_value);
			return cell;
		}

		var createEditCell = function()
		{
			var cell = $("<td>", {"class":"veEditCell"});
			var link = $("<a href=\"javascript:;\">edit</a>");
			link.click(function()
			{
				var nameCell = _row.children("td").eq(0);
				var valueCell = _row.children("td").eq(1);
				if(link.text() == "edit")
				{
					link.text("save edit");

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
				}
				else
				{
					link.text("edit");

					var nameInput = nameCell.children("input");
					var valueInput = valueCell.children("input");

					var name = nameInput.val();
					var value = valueInput.val();

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
			});
			cell.append(link);
			return cell;
		}

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
		
		var makeDraggable = function()
		{
			_div.draggable(
			{
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
						if(overElement)
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
				"stop":function(event, ui)
				{
					var transactionType = (_div.data("toolbar")) ? "addElem" : "remMvElem";
					_div.data("dragging", false);
					_div.data("toolbar", false);

					_div.css("border", "1px dashed black");
					_div.css("background", _div.data("prevBackground"));

					//If the element was not dropped in a valid place then put it back
					if(!_validDropSpot)
					{
						if(_origDDPosition == 0)
						{
							_origDDParent.prepend(_div);
						}
						else if(_origDDPosition == _origDDParent.children(".veElement").length - 1)
						{
							_origDDParent.children(".veElement").eq(_origDDPosition - 1).after(_div);
						}
						else
						{
							_origDDParent.children(".veElement").eq(_origDDPosition).before(_div);
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
				}
			});

			_div.droppable(
			{
				"over":function(event)
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
		
		this.getDiv = function()
		{
			return _div;
		}
		
		this.getXMLNode = function()
		{
			return _xmlNode;
		}
		
		this.getID = function()
		{
			return _id;
		}

		var createTableHeader = function()
		{
			var tableHeader = $("<tr>");
			tableHeader.html("<td class=\"veNameCell\">Name</td><td class=\"veValueCell\">Value</td>");
			return tableHeader;
		}
		
		this.populateInformationDiv = function()
		{
			_infoDiv.empty();

			var nameElement = $("<p>");
			if(_xmlNode.nodeType == 1)
			{
				nameElement.text("Name: " + _xmlNode.nodeName);
			}
			else
			{
				nameElement.text("[text node]");
			}
			_infoDiv.append(nameElement);

			if(_xmlNode.nodeType == 1)
			{
				var attributeTableTitle = $("<p>Attributes:<p/>");
				var attributeTable = $("<table>");
				attributeTable.addClass("veAttributeTableContainer");

				attributeTable.append(createTableHeader());

				$(_xmlNode.attributes).each(function()
				{
					var veAttribute = new VEAttribute(this, _xmlNode);
					attributeTable.append(veAttribute.getAsTableRow());
				});

				_infoDiv.append(attributeTableTitle);
				_infoDiv.append(attributeTable);
				
				var addButton = $("<button>Add attribute</button>");
				addButton.click(function()
				{
					var newAtt = new VEAttribute(null, _xmlNode, "", "");
					var row = newAtt.getAsTableRow();
					attributeTable.append(row);
					_transactions.push({type:"addAttr", row:row})
				});
				_infoDiv.append(addButton);
			}

			if(_xmlNode.nodeType == 3)
			{
				var textEditor = $("<div>");
				var textTitle = $("<div>Text:</div>");
				var nodeText = $("<div>");
				nodeText.text(_xmlNode.nodeValue);

				textEditor.append(textTitle);
				textEditor.append(nodeText);

				_infoDiv.append(textEditor);
				
				var editButton = $("<button>edit text</button>");
				editButton.click(function()
				{
					if(editButton.text() == "edit text")
					{
						nodeText.data("prevTextValue", nodeText.text());
						var textArea = $("<textarea>");
						textArea.val(nodeText.text());
						nodeText.text("");
						nodeText.append(textArea);
						editButton.text("save edit");
					}
					else
					{
						_transactions.push({type:"editText", elem:_xmlNode, vElem: nodeText, value:nodeText.data("prevTextValue")});
						var textArea = nodeText.find("textarea");
						var newValue = textArea.val();
						_xmlNode.nodeValue = newValue;
						nodeText.empty();
						nodeText.text(newValue);
						editButton.text("edit text");
					}
				});
				
				textEditor.append(editButton);
			}
			
			_infoDiv.append($("<br>"));
			_infoDiv.append($("<br>"));
			
			var removeButton = $("<button>Delete this element</button>");
			_infoDiv.append(removeButton);
			removeButton.click(function()
			{
				var divParent = _div.parents(".veElement");
				$(_xmlNode).remove();
				_div.remove();
				_infoDiv.empty();
				
				if(divParent.length)
				{
					divParent.first().trigger("click");
				}
			});
		}

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
				_div.css("border", "1px dashed black");
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

		this.expand = function()
		{
			var siblings = _div.siblings(".veElement");
			if(!(_div.data("expanded") == "expanded") && siblings.length && siblings.length > 0)
			{
				var sibWidth = 10 / siblings.length;
				siblings.each(function()
				{
					$(this).animate({width:sibWidth + "%"}, 900);
					$(this).data("expanded", "small");
				});
				
				_div.animate({width:"90%"}, 1000);
				_div.data("expanded", "expanded");
			}
		}

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

		this.focus = function()
		{
			_div.data("parentVEElement").expand();
			
			var parents = _div.parents(".veElement");
			parents.each(function()
			{
				$(this).data("parentVEElement").expand();
			});

			_div.data("parentVEElement").evenlyDistributeChildren();
		}

		this.setWidth = function(width)
		{
			_div.css("width", width + "%");
		}
		
		//Visual Editor Element constructor
		var initVEE = function()
		{
			_div.addClass("veElement");
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