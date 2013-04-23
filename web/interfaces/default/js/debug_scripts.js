function DebugWidget()
{
	//************************
	//Private member variables
	//************************

	//The this variable
	var _greenbug = this;
	
	//Template tracker class
	var TemplateTracker = function()
	{
		var templates = new Array();
		var currentIndex = -1;

		this.push = function(object)
		{
			if(currentIndex < templates.length - 1 && templates.length > 0)
			{
				templates.splice(currentIndex + 1, templates.length - 1 - currentIndex);
			}
			templates[++currentIndex] = object;

			_forwardButton.button("option", "disabled", true);
			if(templates.length > 1)
			{
				_backButton.button("option", "disabled", false);
			}
		}

		this.next = function()
		{
			if(currentIndex == templates.length - 1)
			{
				return;
			}

			if(currentIndex + 1 == templates.length - 1)
			{
				_forwardButton.button("option", "disabled", true);
			}
			_backButton.button("option", "disabled", false);

			return templates[++currentIndex];
		}

		this.previous = function()
		{
			if(currentIndex == 0)
			{
				return;
			}

			if(currentIndex - 1 == 0)
			{
				_backButton.button("option", "disabled", true);
			}
			_forwardButton.button("option", "disabled", false);

			return templates[--currentIndex];
		}
		
		this.peekPrevious = function()
		{
			if(currentIndex == 0)
			{
				return;
			}

			return templates[currentIndex - 1];
		}

		this.peekNext = function()
		{
			if(currentIndex == templates.length - 1)
			{
				return;
			}

			return templates[currentIndex + 1];
		}
	}

	var _templateTracker = new TemplateTracker();

	//Debugger state-keeping variables
	var _debugOn = false;
	var _pauseSelector = false;
	var _elements = new Array();
	var _itemSelected = false; //Used to prevent multiple elements from being highlighted
	var _editModeText = false;
	var _fromSelection = false;
	var _selectedInfoContainers = new Array();

	//Page elements
	var _mainDiv;

	var _textEditor;
	var _vEditor;

	var _navArea;
	var _fileSelector;
	var _templateSelector;
	var _editor;
	var _editingDiv;
	var _xmlStatusBar;
	
	//Buttons
	var _backButton;
	var _forwardButton;
	var _currentSelectionButton;
	var _enableSelectorButton;
	var _closeEditorButton;
	var _saveButton;
	var _swapEditorButton;

	//Editor state-keeping variables
	var _currentFileName;
	var _currentLocation;
	var _currentNodename;
	var _currentName;
	var _currentMatch;
	var _currentNamespace;
	var _isVisualEditor = true;

	var _styleFunctions = new Array();

	//Used to reload the page while keeping the state of the editor
	var partialPageReload = function(callback)
	{
		$.ajax(document.URL)
		.success(function(response)
		{
			//Get the body text from the response
			var bodyStartIndex = response.indexOf("<body");
			var bodyEndIndex = response.indexOf("</body>");
			var bodyText = response.substring(bodyStartIndex, bodyEndIndex + 7);

			//Get the current top area and container
			var topLevelTopArea = $("#topArea");
			var topLevelContainer = $("#container");

			//Create a temporary div and put the html into it
			var tempDiv = $("<div>");
			tempDiv.html(bodyText);

			//Replace the contents of the old elements with the new elements
			var topArea = tempDiv.find("#topArea");
			var container = tempDiv.find("#container");
			topLevelTopArea.html(topArea.html());
			topLevelContainer.html(container.html());

			//Update the events for the debug elements that currently don't have events associated with them
			var debugElems = $('debug, [debug="true"]').filter(function(){return (!($.data(this, "events"))) ? true : false});
			addMouseEventsToDebugElements(debugElems);
		})
		.error(function()
		{
			alert("There was an error reloading the page, please reload manually.");
		});

		if(callback)
		{
			callback();
		}
	}

	//Some functions need to be called after an element is added to the page. So we store them and call them later.
	var callStyleFunctions = function()
	{
		for(var i = 0; i < _styleFunctions.length; i++)
		{
			var sFunction = _styleFunctions[i];
			sFunction();
		}
	}
	
	var changeToSelectedElement = function(templateIndex, templateList)
	{
		_templateSelector.children("select").empty();
		
		for(var i = 0; i < templateList.length; i++)
		{
			_templateSelector.children("select").append($(templateList[i]).clone(true));
		}

		if(templateIndex === undefined)
		{
			_templateSelector.find("option").first().trigger("change", [true]);
		}
		else
		{
			_templateSelector.find("option").filter(function(){return $(this).data("index") == templateIndex}).first().trigger("change", [true]);
		}
		return;
	}
	
	var createNavButtons = function(buttonDiv)
	{
		var navButtonHolder = $("<div>").css("float", "left");

		var backForwardFunction = function(e)
		{
			var template;
			if($(e.target).attr("id") == "veBack")
			{
				template = _templateTracker.previous();
			}
			else
			{
				template = _templateTracker.next();
			}
			
			if(!template)
			{
				return;
			}

			_fileSelector.find("option").filter(function(){return $(this).data("index") == template.fileIndex}).prop("selected", true);

			changeToSelectedElement(template.templateIndex, template.list);
		}
		
		_backButton = $("<button>Back button</button>").attr("id", "veBack");
		_backButton.click(backForwardFunction);
		_styleFunctions.push(function(){_backButton.button({icons:{primary:"ui-icon-triangle-1-w"}, text:false, disabled:true})});
	
		_forwardButton = $("<button>Forward button</button>").attr("id", "veForwards");
		_forwardButton.click(backForwardFunction);
		_styleFunctions.push(function(){_forwardButton.button({icons:{primary:"ui-icon-triangle-1-e"}, text:false, disabled:true})});

		//Changes the template list to what is currently selected
		_currentSelectionButton = $("<button>Current selection button</button>");
		_currentSelectionButton.click(function()
		{
			_fileSelector.find("option").eq(0).prop("selected", true);
			changeToSelectedElement(undefined, _selectedInfoContainers);
			
			var selectedCopy = new Array();
			for(var i = 0; i < _selectedInfoContainers.length; i++)
			{
				selectedCopy[i] = _selectedInfoContainers[i];
			}
			
			_templateTracker.push({fileIndex:-1, templateIndex:0, list:selectedCopy});
		});
		_styleFunctions.push(function(){_currentSelectionButton.button({icons:{primary:"ui-icon-pencil"}, text:false, disabled:true})});
		
		navButtonHolder.append(_backButton);
		navButtonHolder.append(_forwardButton);
		navButtonHolder.append(_currentSelectionButton);
		buttonDiv.append(navButtonHolder);
	}

	//Create the area where the buttons are stored
	var createControlButtons = function(buttonDiv)
	{
		//Used to enable the selector to get the templates of a particular area of the page
		_enableSelectorButton = $("<button>Select an element</button>");
		_enableSelectorButton.click(function()
		{
			_enableSelectorButton.button("option", "label", "Select new element");
			$("a").click(function(e)
			{
				e.preventDefault();
			});
			_debugOn = true;
			_pauseSelector = false;
			_enableSelectorButton.button("option", "disabled", true);
		});
		_styleFunctions.push(function(){_enableSelectorButton.button({icons:{primary:"ui-icon-pencil"}})});

		//Used to minimise/restore the editor
		_closeEditorButton = $("<button>Close editor</button>");
		_closeEditorButton.click(function()
		{
			if(_closeEditorButton.button("option", "label") == "Close editor")
			{
				_closeEditorButton.button("option", "label", "Open editor");
				_editingDiv.hide();
			}
			else
			{
				_closeEditorButton.button("option", "label", "Close editor");
				_editingDiv.show();
			}
		});
		_closeEditorButton.css("float", "right");
		_styleFunctions.push(function(){_closeEditorButton.button({icons:{secondary:"ui-icon-newwin"}, disabled:true})});

		//Used to save any changes that have been made to this template
		_saveButton = $("<button>Save changes</button>");
		_saveButton.click(function()
		{
			if(_editor)
			{
				var xmlString;
				if(_isVisualEditor)
				{
					_vEditor.savePendingEdits();
					xmlString = new XMLSerializer().serializeToString(_vEditor.getXML());
				}
				else
				{
					xmlString = _editor.getValue();
				}
				xmlString = xmlString.replace(/&/g, "&amp;");

				try
				{
					var xml = $.parseXML('<testContainer xmlns:xslt="http://www.w3.org/1999/XSL/Transform" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:java="http://xml.apache.org/xslt/java" xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil" xmlns:gslib="http://www.greenstone.org/skinning" xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat">' + xmlString + "</testContainer>");
				}
				catch(error)
				{
					alert("Could not save as there is a problem with the XML.");
					return;
				}

				var url = gs.xsltParams.library_name;
				var parameters = {"a":"g", "rt":"r", "s":"SaveXMLTemplateToFile", "s1.locationName":_currentLocation, "s1.fileName":_currentFileName, "s1.interfaceName":gs.xsltParams.interface_name, "s1.siteName":gs.xsltParams.site_name, "s1.collectionName":gs.cgiParams.c, "s1.namespace":_currentNamespace, "s1.nodename":_currentNodename, "s1.xml":xmlString};

				if(_currentName && _currentName.length > 0){parameters["s1.name"] = _currentName;}
				if(_currentMatch && _currentMatch.length > 0){parameters["s1.match"] = _currentMatch;}

				_saveButton.button("option", "disabled", true);
				$.blockUI({message:'<div class="ui-state-active">Saving, please wait...</div>'});

				$.post(url, parameters)
				.success(function()
				{
					$.ajax(gs.xsltParams.library_name + "?a=s&sa=c")
					.success(function()
					{
						partialPageReload(function(){$.unblockUI();});
					})
					.error(function()
					{
						$.unblockUI();
						alert("Error reloading collection.");
					})
					.complete(function()
					{
						_saveButton.button("option", "disabled", false);
					});
				})
				.error(function()
				{
					alert("There was an error sending the request to the server, please try again.");
				});
			}
		});
		_styleFunctions.push(function(){_saveButton.button({icons:{primary:"ui-icon-disk"}, disabled:true})});

		//Used to switch between the XML and Visual editors
		_swapEditorButton = $("<button>Switch to XML editor</button>");
		_swapEditorButton.button().click(function()
		{
			if(_vEditor && _textEditor)
			{
				if(_isVisualEditor)
				{
					_vEditor.savePendingEdits();
					_vEditor.getMainDiv().hide();
					var containerNode = _vEditor.getXML().firstChild;
					var templateNode = containerNode.firstChild;
					while(templateNode)
					{
						if(templateNode.nodeType == 1)
						{
							break;
						}
						templateNode = templateNode.nextSibling;
					}
					var xmlText = new XMLSerializer().serializeToString(templateNode);
					_editor.setValue(xmlText);
					_editor.clearSelection();
					var UndoManager = require("ace/undomanager").UndoManager;
					_editor.getSession().setUndoManager(new UndoManager());
					_textEditor.show();
					_swapEditorButton.button("option", "label", "Switch to visual editor");
					_isVisualEditor = false;
					_xmlStatusBar.show();
				}
				else
				{
					_textEditor.hide();
					var xmlText = _editor.getValue();
					_vEditor.getMainDiv().remove();
					_vEditor = new visualXMLEditor(xmlText);
					_editingDiv.append(_vEditor.getMainDiv());
					_vEditor.selectRootElement();
					_vEditor.getMainDiv().show();
					_swapEditorButton.button("option", "label", "Switch to XML editor");
					_isVisualEditor = true;
					_xmlStatusBar.hide();
				}
			}
		});
		_styleFunctions.push(function(){_swapEditorButton.button({icons:{primary:"ui-icon-refresh"}})});

		undoButton = $("<button>Undo</button>");
		undoButton.click(function()
		{
			if(_isVisualEditor)
			{
				_vEditor.undo();
			}
			else
			{
				_editor.undo();
			}
		});
		_styleFunctions.push(function(){undoButton.button({icons:{primary:"ui-icon-arrowreturnthick-1-w"}})});

		buttonDiv.append(_enableSelectorButton);
		buttonDiv.append(_closeEditorButton);
		buttonDiv.append(_saveButton);
		buttonDiv.append(_swapEditorButton);
		buttonDiv.append(undoButton);
	}

	//Used to monitor the state of the XML in the XML editor and will notify the user if there is an error
	var createXMLStatusBar = function(buttonDiv)
	{
		_xmlStatusBar = $("<span>");
		_xmlStatusBar.css("padding", "5px");
		_xmlStatusBar.addClass("ui-corner-all");
		_styleFunctions.push(function(){_xmlStatusBar.hide();});

		//Check the XML for errors every 2 seconds
		setInterval(function()
		{
			if(_editor)
			{
				var xmlString = _editor.getValue();
				try
				{
					var xml = $.parseXML('<testContainer xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:java="http://xml.apache.org/xslt/java" xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil" xmlns:gslib="http://www.greenstone.org/skinning" xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat">' + xmlString + "</testContainer>");
				}
				catch(error)
				{
					console.log(error);
					_xmlStatusBar.text("XML ERROR! (Mouse over for details)");
					_xmlStatusBar.addClass("ui-state-error");
					_xmlStatusBar.removeClass("ui-state-active");
					_xmlStatusBar.attr("title", error);
					_saveButton.button("option", "disabled", true);
					_swapEditorButton.button("option", "disabled", true);
					return;
				}

				_xmlStatusBar.text("XML OK!");
				_xmlStatusBar.addClass("ui-state-active");
				_xmlStatusBar.removeClass("ui-state-error");
				_xmlStatusBar.removeAttr("title");
				if(_saveButton.button("option", "label") == "Save changes")
				{
					_saveButton.button("option", "disabled", false);
				}
				if(_swapEditorButton.button("option", "label") == "Switch to visual editor")
				{
					_swapEditorButton.button("option", "disabled", false);
				}
			}
		}, 2000);
		buttonDiv.append(_xmlStatusBar);
	}

	//Create the elements that allow 
	var createFileAndTemplateSelectors = function(buttonDiv)
	{
		_templateSelector = $("<div>", {"id":"veTemplateSelector", "class":"ui-state-default ui-corner-all"});
		_templateSelector.append($("<span>Templates: <span>"));
		var templateSelectBox = $("<select>").append("<option>-- No templates --</option>");
		templateSelectBox.change(function(e, triggered)
		{
			var selected = templateSelectBox.find(":selected");
			var changeFunction = selected.data("changeFunction");
			if(changeFunction)
			{
				changeFunction();
			}

			if(!triggered)
			{
				_templateTracker.push({fileIndex:_fileSelector.find(":selected").data("index"), templateIndex:selected.data("index"), list:templateSelectBox.children("option").clone(true)});
			}
		});
		_templateSelector.append(templateSelectBox);
		_fileSelector = $("<div>", {"id":"veFileSelector"});
		buttonDiv.append(_fileSelector);
		buttonDiv.append(_templateSelector);

		//Populate the file selector
		var url = gs.xsltParams.library_name + "?a=g&rt=r&s=GetXSLTFilesForCollection&s1.interfaceName=" + gs.xsltParams.interface_name + "&s1.siteName=" + gs.xsltParams.site_name + "&s1.collectionName=" + gs.cgiParams.c;
		$.ajax(url)
		.success(function(response)
		{
			var listStartIndex = response.indexOf("<fileListJSON>") + "<fileListJSON>".length;
			var listEndIndex = response.indexOf("</fileListJSON>");

			var listString = response.substring(listStartIndex, listEndIndex).replace(/&quot;/g, "\"").replace(/\\/g, "/");
			var list = eval(listString);

			var fileSelectBox = $("<select>");
			fileSelectBox.append($("<option>-- Select a file --</option>").data("index", -1));
			_fileSelector.addClass("ui-state-default");
			_fileSelector.addClass("ui-corner-all");
			_fileSelector.append("<span>Files: </span>");
			_fileSelector.append(fileSelectBox);
			
			for(var i = 0; i < list.length; i++)
			{
				var item = list[i];
				var option = $("<option>" + item.path + " (" + item.location + ")</option>").data("index", i);
				option.data("fileItem", item);
				fileSelectBox.append(option);
			}

			fileSelectBox.change(function()
			{
				var selectedItem = fileSelectBox.find(":selected");
				
				if(!selectedItem.data("fileItem"))
				{
					return;
				}

				_greenbug.populateTemplateSelectorFromFile(selectedItem.data("fileItem").path, selectedItem.data("fileItem").location, function(){_templateSelector.children("select").trigger("change")});
			});
		})
		.error(function()
		{
			console.log("Error retrieving XSLT files");
		});
	}

	//The function that creates all the necessary elements for Greenbug
	var createDebugDiv = function()
	{
		_mainDiv = $("<div>", {"id":"debugDiv"});
		_mainDiv.css(
		{
			"position":"fixed",
			"font-size":"0.8em",
			"bottom":"0px",
			"width":"100%",
			"background":"white",
			"border":"1px black solid",
			"padding":"5px",
			"z-index":100
		});

		_editingDiv = $("<div>");
		var toolBarDiv = $("<div>");
		toolBarDiv.css({"height":"40px"});
		toolBarDiv.append("<div>", {style:"clear:both;"});

		var buttonDiv = $("<div>");
		toolBarDiv.append(buttonDiv);
		createNavButtons(buttonDiv);
		createControlButtons(buttonDiv);
		createFileAndTemplateSelectors(buttonDiv);
		createXMLStatusBar(buttonDiv);

		_styleFunctions.push(function(){$(".ui-button").css({"margin-right":"0.5em"});});

		_mainDiv.append(toolBarDiv);
		_mainDiv.append(_editingDiv);
		_mainDiv.append(_navArea);
	}

	//Clear all selected elements on the page
	var clearAll = function()
	{
		_itemSelected = false;
		$(_elements).each(function()
		{
			$(this).remove();
		});
	}

	//Put a border around the given element
	var highlightElement = function(e)
	{
		var topBorderDiv = $("<div>");
		var bottomBorderDiv = $("<div>");
		var leftBorderDiv = $("<div>");
		var rightBorderDiv = $("<div>");

		topBorderDiv.css({"position":"absolute", "top":e.offset().top + "px", "left":e.offset().left + "px", "height":"0px", "width":e.width() + "px", "border":"1px solid red"});
		bottomBorderDiv.css({"position":"absolute", "top":(e.offset().top + e.height()) + "px", "left":e.offset().left + "px", "height":"0px", "width":e.width() + "px", "border":"1px solid red"});
		leftBorderDiv.css({"position":"absolute", "top":e.offset().top + "px", "left":e.offset().left + "px", "height":e.height() + "px", "width":"0px", "border":"1px solid red"});
		rightBorderDiv.css({"position":"absolute", "top":e.offset().top + "px", "left":(e.offset().left + e.width()) + "px", "height":e.height() + "px", "width":"0px",	"border":"1px solid red"});

		$("body").append(topBorderDiv, bottomBorderDiv, leftBorderDiv, rightBorderDiv);

		_elements.push(topBorderDiv);
		_elements.push(bottomBorderDiv);
		_elements.push(leftBorderDiv);
		_elements.push(rightBorderDiv);
	}
	
	this.populateTemplateSelectorFromFile = function(filename, location, callback)
	{
		var getURL = gs.xsltParams.library_name + "?a=g&rt=r&s=GetTemplateListFromFile&s1.fileName=" + filename + "&s1.locationName=" + location + "&s1.interfaceName=" + gs.xsltParams.interface_name + "&s1.siteName=" + gs.xsltParams.site_name + "&s1.collectionName=" + gs.cgiParams.c;
		$.ajax(getURL)
		.success(function(templateResponse)
		{
			var templateListStart = templateResponse.indexOf("<templateList>") + "<templateList>".length;
			var templateListEnd = templateResponse.indexOf("</templateList>");
			var templateListString = templateResponse.substring(templateListStart, templateListEnd).replace(/&quot;/g, "\"");
			var templateList = eval(templateListString);

			_templateSelector.children("select").empty();
			if(templateList.length == 0)
			{
				_templateSelector.children("select").append($("<option>-- No templates --</option>").data("index", -1));
			}

			for(var i = 0; i < templateList.length; i++)
			{
				var namespace = templateList[i].namespace;
				var nodename = "template";
				var name = templateList[i].name;
				var match = templateList[i].match;

				if(name)
				{
					name = templateList[i].name.replace(/&apos;/g, "'").replace(/&quot;/g, "\"").replace(/&amp;/g, "&");
				}
				if(match)
				{
					match = templateList[i].match.replace(/&apos;/g, "'").replace(/&quot;/g, "\"").replace(/&amp;/g, "&");
				}

				var infoContainer = $("<option>");

				_elements.push(infoContainer);

				addChangeEventToInfoContainer(infoContainer, filename, location, nodename, namespace, name, match);

				if(name && name.length > 0)
				{
					infoContainer.text(name);
				}
				if(match && match.length > 0)
				{
					infoContainer.text(match);
				}

				infoContainer.data("index", i);
				_templateSelector.children("select").append(infoContainer);
			}
			
			if(callback)
			{
				callback();
			}
		});
	}

	//Change the current template in the XML and Visual editor
	this.changeCurrentTemplate = function(location, filename, nodename, namespace, name, match)
	{
		_currentFileName = filename;
		_currentLocation = location;
		_currentNodename = nodename;
		_currentNamespace = namespace;
		_currentName = name;
		_currentMatch = match;
	
		var responseName = "requestedNameTemplate";

		var url = gs.xsltParams.library_name + "?a=g&rt=r&s=GetXMLTemplateFromFile&s1.fileName=" + filename + "&s1.interfaceName=" + gs.xsltParams.interface_name + "&s1.siteName=" + gs.xsltParams.site_name + "&s1.collectionName=" + gs.cgiParams.c + "&s1.locationName=" + location + "&s1.namespace=" + namespace + "&s1.nodename=" + nodename;
		if(match && match.length > 0){url += "&s1.match=" + match; responseName = "requestedMatchTemplate";}
		if(name && name.length > 0){url += "&s1.name=" + name;}

		$.ajax(url)
		.success(function(response)
		{
			var template;
			if(response.search(responseName) != -1)
			{
				var startIndex = response.indexOf("<" + responseName + ">") + responseName.length + 2;
				var endIndex = response.indexOf("</" + responseName + ">");
				template = response.substring(startIndex, endIndex);
			}
			else
			{
				return;
			}

			_textEditor = $("<div>", {"id":"textEditor"});
			_textEditor.css({"width":"100%", "height":"300px"});
			_textEditor.val(template);

			if(_isVisualEditor)
			{
				_textEditor.hide();
			}

			_editingDiv.empty();
			_editingDiv.append($("<p>Location: " + location + " <br/>Filename: " + filename + "</p>"));
			_editingDiv.append(_textEditor);

			_vEditor = new visualXMLEditor(template);
			_editingDiv.append(_vEditor.getMainDiv());
			_vEditor.setGreenbug(_greenbug);
			_vEditor.selectRootElement();
			$("#veToolboxDiv").height($("#veEditorContainer").height());

			_vEditor.setFileLocation(location);
			_vEditor.setFileName(filename);

			if(!_isVisualEditor)
			{
				_vEditor.getMainDiv().hide();
			}

			_editor = ace.edit("textEditor");
			_editor.getSession().setMode("ace/mode/xml");
			_editor.getSession().setUseSoftTabs(false);
			_editor.setValue(template);
			_editor.clearSelection();
			var UndoManager = require("ace/undomanager").UndoManager;
			_editor.getSession().setUndoManager(new UndoManager());

			_textEditor.css({"min-height":"200px", "border-top":"5px solid #444"});
			_textEditor.resizable({handles: 'n', resize:function()
			{
				_textEditor.css({top:"0px"});
				_editor.resize();
			}});

			_closeEditorButton.button("option", "disabled", false);
			if(_closeEditorButton.button("option", "label") == "Open editor")
			{
				_closeEditorButton.button("option", "label", "Close editor");
				_editingDiv.show();
			}
		})
		.error(function()
		{
			console.log("Error getting the XML template from the file");
		});
	}

	//Store the function that is called when this template is selected from the list
	var addChangeEventToInfoContainer = function(infoContainer, filename, location, nodename, namespace, name, match)
	{
		infoContainer.data("changeFunction", function()
		{
			_greenbug.changeCurrentTemplate(location, filename, nodename, namespace, name, match);
		});
	}
	
	this.getTemplateTracker = function()
	{
		return _templateTracker;
	}
	
	//Turns a filename into it's location (i.e. interface/site/collection) and name
	this.fileNameToLocationAndName = function(filepath)
	{
		var location;
		var filename;
		//Use the filepath to work out where this file is from
		if(filepath.search(/[\/\\]interfaces[\/\\]/) != -1)
		{
			location = "interface";
			filename = filepath.replace(/.*[\/\\]transform[\/\\]/, "");
		}
		else if(filepath.search(/[\/\\]sites[\/\\].*[\/\\]collect[\/\\].*[\/\\]etc[\/\\]/) != -1)
		{
			location = "collectionConfig";
			filename = filepath.replace(/.*[\/\\]sites[\/\\].*[\/\\]collect[\/\\].*[\/\\]etc[\/\\]/, "");
		}
		else if(filepath.search(/[\/\\]sites[\/\\].*[\/\\]collect[\/\\].*[\/\\]transform[\/\\]/) != -1)
		{
			location = "collection";
			filename = filepath.replace(/.*[\/\\]sites[\/\\].*[\/\\]collect[\/\\].*[\/\\]transform[\/\\]/, "");
		}
		else if(filepath.search(/[\/\\]sites[\/\\].*[\/\\]transform[\/\\]/) != -1)
		{
			location = "site";
			filename = filepath.replace(/.*[\/\\]sites[\/\\].*[\/\\]transform[\/\\]/, "");
		}
		
		filename = filename.replace(/\\/g, "/");
		
		return {location:location, filename:filename};
	}

	//Add the mouse events to the <debug> elemetns that are called when the selector is anabled
	var addMouseEventsToDebugElements = function(debugElems)
	{
		debugElems.click(function(e)
		{
			if(_debugOn)
			{
				e.stopPropagation();
				$("a").off("click");
				_debugOn = false;
				_pauseSelector = true;
				_enableSelectorButton.button("option", "disabled", false);
				_templateSelector.children("select").trigger("change", [true]);
				_fileSelector.children("select").val("none");
				
				var selectedCopy = new Array();
				for(var i = 0; i < _selectedInfoContainers.length; i++)
				{
					selectedCopy[i] = _selectedInfoContainers[i];
				}
				
				_currentSelectionButton.button("option", "disabled", false);
				
				_templateTracker.push({fileIndex:-1, list:selectedCopy, templateIndex:0});
			}
		});

		debugElems.mouseover(function()
		{
			if(_debugOn && !_pauseSelector)
			{
				_fileSelector.find("select").val("none");
			
				var nodes = new Array();
				if($(this).is("table, tr"))
				{
					var size = parseInt($(this).attr("debugSize"));
					for(var i = 0; i < size; i++)
					{
						var tempNode = $("<div>");
						tempNode.tempAttrs = new Array();
						$(this.attributes).each(function()
						{
							if(this.value.charAt(0) == '[')
							{
								var values = eval(this.value);
								if(values[i] == "")
								{
									return;
								}
								tempNode.attr(this.name, values[i]);
								tempNode.tempAttrs.push({name:this.name, value:values[i]});
							}
						});
						nodes.push(tempNode);
					}
				}
				else
				{
					nodes.push(this);
				}

				$(nodes).each(function()
				{
					var filepath = $(this).attr("filename");
					var fullNodename = $(this).attr("nodename");
					var colonIndex = fullNodename.indexOf(":");
					var namespace = fullNodename.substring(0, colonIndex);
					var nodename = fullNodename.substring(colonIndex + 1);
					var name = $(this).attr("name");
					var match = $(this).attr("match");

					var file = _greenbug.fileNameToLocationAndName(filepath);

					var infoContainer = $("<option>");

					_elements.push(infoContainer);

					addChangeEventToInfoContainer(infoContainer, file.filename, file.location, nodename, namespace, name, match);

					if(name && name.length > 0)
					{
						infoContainer.text(name);
					}
					if(match && match.length > 0)
					{
						infoContainer.text(match);
					}

					_templateSelector.children("select").append(infoContainer);
					infoContainer.data("index", infoContainer.index());
					_selectedInfoContainers.push(infoContainer.clone(true));
				});
				
				if(!_itemSelected)
				{
					_itemSelected = true;
					highlightElement($(this));
				}
			}
		});

		debugElems.mouseout(function()
		{
			if(_debugOn && !_pauseSelector)
			{
				clearAll();
				_templateSelector.children("select").empty();
				_selectedInfoContainers = new Array();
			}
		});
	}

	//Remove <debug> elements from the title
	var fixTitle = function()
	{
		$("title").text($("title").text().replace(/<[^>]*>/g, ""));
	}

	//Initialise Greenbug
	this.init = function()
	{
		//We only want this on if we have debug elements in the page
		var debugElems = $('debug, [debug="true"]');
		if(!debugElems.length)
		{
			var enableGBButtonHolder = $("<div>", {"title":"Enable Greenbug", "id":"gbEnableButton", "class":"ui-state-default ui-corner-all"});
			enableGBButtonHolder.append($("<img>", {"src":gs.imageURLs.greenBug}));
			enableGBButtonHolder.click(function()
			{
				var url = document.URL;
				url = url.replace(/[\?&]debug=0/g, "").replace(/[\?&]debug=1/g, "");

				if(url.indexOf("?") == url.length - 1)
				{
					document.location.href = url += "debug=1";
				}
				else if(url.indexOf("?") != -1)
				{
					document.location.href = url += "&debug=1";
				}
				else
				{
					document.location.href = url += "?debug=1";
				}
			});
			$("body").append(enableGBButtonHolder);
			return;
		}
		
		createDebugDiv();
		$("body").append(_mainDiv);

		callStyleFunctions();

		addMouseEventsToDebugElements(debugElems);
		fixTitle();
	}
}

//The code entry point
$(window).load(function()
{
	var debugWidget = new DebugWidget();
	debugWidget.init();
});
