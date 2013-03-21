function DebugWidget()
{
	//************************
	//Private member variables
	//************************
	
	//The this variable
	var _greenbug = this;
	
	//Debugger state-keeping variables
	var _debugOn = false;
	var _pauseSelector = false;
	var _elements = new Array();
	var _itemSelected = false; //Used to prevent multiple elements from being highlighted
	var _editModeText = false;
	var _selectedTemplate;
	
	//Page elements
	var _mainDiv;
	
	var _textEditor;
	var _vEditor;
	
	var _navArea;
	var _fileSelector;
	var _templateSelector;
	var _editor;
	var _editingDiv;
	var _unpauseButton;
	var _closeEditorButton;
	var _xmlStatusBar;
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
	
	var callStyleFunctions = function()
	{
		for(var i = 0; i < _styleFunctions.length; i++)
		{
			var sFunction = _styleFunctions[i];
			sFunction();
		}
	}
	
	var createButtonDiv = function(buttonDiv)
	{
		var pickElementButton = $("<button>Enable selector</button>");
		pickElementButton.click(function()
		{
			if(!_debugOn)
			{
				pickElementButton.button("option", "label", "Disable selector");
				$("a").click(function(e)
				{
					e.preventDefault();
				});
				_debugOn = true;
			}
			else
			{
				pickElementButton.button("option", "label", "Enable selector");
				$("a").off("click");
				clearAll();
				_unpauseButton.button("option", "disabled", true);
				_pauseSelector = false;
				_debugOn = false;
			}
		});
		_styleFunctions.push(function(){pickElementButton.button({icons:{primary:"ui-icon-power"}})});

		_unpauseButton = $("<button>Select new element</button>");
		_unpauseButton.click(function()
		{
			if(_pauseSelector)
			{
				_pauseSelector = false;
				_unpauseButton.button("option", "disabled", true);
			}
		});
		_styleFunctions.push(function(){_unpauseButton.button({icons:{primary:"ui-icon-pencil"}, disabled:true})});
		
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
					var xml = $.parseXML('<testContainer xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:java="http://xml.apache.org/xslt/java" xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil" xmlns:gslib="http://www.greenstone.org/skinning" xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat">' + xmlString + "</testContainer>");
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
		
		buttonDiv.append(pickElementButton);
		buttonDiv.append(_unpauseButton);
		buttonDiv.append(_closeEditorButton);
		buttonDiv.append(_saveButton);
		buttonDiv.append(_swapEditorButton);
		buttonDiv.append(undoButton);
	}
	
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
		createButtonDiv(buttonDiv);
		createXMLStatusBar(buttonDiv);
		
		_navArea = $("<div>", {"id":"veNavArea"});
		_templateSelector = $("<div>", {"id":"veTemplateSelector"});
		_fileSelector = $("<div>", {"id":"veFileSelector", "class":"ui-state-default ui-corner-all"});
		_navArea.append(_fileSelector);
		_navArea.append(_templateSelector);
		_navArea.append("<div>", {style:"clear:both;"});
		
		//Populate the file selector
		var url = gs.xsltParams.library_name + "?a=g&rt=r&s=GetXSLTFilesForCollection&s1.interfaceName=" + gs.xsltParams.interface_name + "&s1.siteName=" + gs.xsltParams.site_name + "&s1.collectionName=" + gs.cgiParams.c;
		$.ajax(url)
		.success(function(response)
		{
			var listStartIndex = response.indexOf("<fileListJSON>") + "<fileListJSON>".length;
			var listEndIndex = response.indexOf("</fileListJSON>");
			
			var listString = response.substring(listStartIndex, listEndIndex).replace(/&quot;/g, "\"").replace(/\\/g, "/");
			var list = eval(listString);
			
			var selectBox = $("<select>");
			selectBox.append($("<option>-- Select a file --</option>", {value:"none"}));
			_fileSelector.append("<span>Files: </span>");
			_fileSelector.append(selectBox);
			for(var i = 0; i < list.length; i++)
			{
				var item = list[i];
				var option = $("<option>" + item.path + " (" + item.location + ")</option>", {value:item.path});
				option.data("fileItem", item);
				selectBox.append(option);
			}

			selectBox.change(function()
			{
				var selectedItem = selectBox.find(":selected");
				
				var getURL = gs.xsltParams.library_name + "?a=g&rt=r&s=GetTemplateListFromFile&s1.fileName=" + selectedItem.data("fileItem").path + "&s1.locationName=" + selectedItem.data("fileItem").location + "&s1.interfaceName=" + gs.xsltParams.interface_name + "&s1.siteName=" + gs.xsltParams.site_name + "&s1.collectionName=" + gs.cgiParams.c;
				$.ajax(getURL)
				.success(function(templateResponse)
				{
					var templateListStart = templateResponse.indexOf("<templateList>") + "<templateList>".length;
					var templateListEnd = templateResponse.indexOf("</templateList>");
					var templateListString = templateResponse.substring(templateListStart, templateListEnd).replace(/&quot;/g, "\"");
					var templateList = eval(templateListString);
					
					clearAll();
					
					for(var i = 0; i < templateList.length; i++)
					{
						var fileName = selectedItem.data("fileItem").path;
						var location = selectedItem.data("fileItem").location;
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
						
						var infoContainer = $("<div>", {"class":"gbTemplateContainer ui-state-default ui-corner-all"});
						
						_elements.push(infoContainer);
						
						addMouseEventsToInfoContainer(infoContainer, fileName, location, nodename, namespace, name, match);
						
						if(name && name.length > 0)
						{
							infoContainer.text(name);
						}
						if(match && match.length > 0)
						{
							infoContainer.text(match);
						}
						
						if(_templateSelector.children("div").length > 0)
						{/*
							var spacer = $("<div>&gt;&gt;</div>");
							spacer.addClass("gbSpacer");

							_templateSelector.prepend(spacer);
							_elements.push(spacer);
							*/
						}
						
						_templateSelector.prepend(infoContainer);
						
						//resizeContainers();
					}
				});
			});
		})
		.error(function()
		{
			console.log("Error retrieving XSLT files");
		});
		
		_styleFunctions.push(function(){$(".ui-button").css({"margin-right":"0.5em"});});
		
		_mainDiv.append(toolBarDiv);
		_mainDiv.append(_editingDiv);
		_mainDiv.append(_navArea);
	}
	
	var clearAll = function()
	{
		_itemSelected = false;
		$(_elements).each(function()
		{
			$(this).remove();
		});
	}
	
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
	
	this.changeCurrentTemplate = function(location, fileName, nodename, namespace, name, match)
	{
		var responseName = "requestedNameTemplate";
		
		var url = gs.xsltParams.library_name + "?a=g&rt=r&s=GetXMLTemplateFromFile&s1.fileName=" + fileName + "&s1.interfaceName=" + gs.xsltParams.interface_name + "&s1.siteName=" + gs.xsltParams.site_name + "&s1.collectionName=" + gs.cgiParams.c + "&s1.locationName=" + location + "&s1.namespace=" + namespace + "&s1.nodename=" + nodename;
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
			_editingDiv.append($("<p>Location: " + location + " <br/>Filename: " + fileName + "</p>"));
			_editingDiv.append(_textEditor);
			
			_vEditor = new visualXMLEditor(template);
			_editingDiv.append(_vEditor.getMainDiv());
			_vEditor.setGreenbug(_greenbug);
			_vEditor.selectRootElement();
			
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
			console.log("ERROR");
		});
	}
	
	var addMouseEventsToInfoContainer = function(infoContainer, fileName, location, nodename, namespace, name, match)
	{
		infoContainer.click(function()
		{
			if(_selectedTemplate)
			{
				_selectedTemplate.css("border", _selectedTemplate.prevBorder);
			}
			_selectedTemplate = infoContainer;
			_selectedTemplate.prevBorder = _selectedTemplate.css("border");
			_selectedTemplate.css("border", "red 1px solid");
		
			_currentFileName = fileName;
			_currentLocation = location;
			_currentNodename = nodename;
			_currentNamespace = namespace;
			_currentName = name;
			_currentMatch = match;
		
			_greenbug.changeCurrentTemplate(location, fileName, nodename, namespace, name, match);
		});
		infoContainer.mouseover(function()
		{
			$(this).removeClass("ui-state-default");
			$(this).addClass("ui-state-active");
		});
		infoContainer.mouseout(function()
		{
			$(this).addClass("ui-state-default");
			$(this).removeClass("ui-state-active");
		});
	}

	var addMouseEventsToDebugElements = function(debugElems)
	{
		debugElems.click(function()
		{
			if(_debugOn)
			{
				_pauseSelector = true;
				_unpauseButton.button("option", "disabled", false);
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

					var location;
					var fileName;
					if(filepath.search(/[\/\\]interfaces[\/\\]/) != -1)
					{
						location = "interface";
						fileName = filepath.replace(/.*[\/\\]transform[\/\\]/, "");
					}
					else if(filepath.search(/[\/\\]sites[\/\\].*[\/\\]collect[\/\\].*[\/\\]etc[\/\\]/) != -1)
					{
						location = "collectionConfig";
						fileName = filepath.replace(/.*[\/\\]sites[\/\\].*[\/\\]collect[\/\\].*[\/\\]etc[\/\\]/, "");
					}
					else if(filepath.search(/[\/\\]sites[\/\\].*[\/\\]collect[\/\\].*[\/\\]transform[\/\\]/) != -1)
					{
						location = "collection";
						fileName = filepath.replace(/.*[\/\\]sites[\/\\].*[\/\\]collect[\/\\].*[\/\\]transform[\/\\]/, "");
					}
					else if(filepath.search(/[\/\\]sites[\/\\].*[\/\\]transform[\/\\]/) != -1)
					{
						location = "site";
						fileName = filepath.replace(/.*[\/\\]sites[\/\\].*[\/\\]transform[\/\\]/, "");
					}

					var infoContainer = $("<div>", {"class":"gbTemplateContainer ui-state-default ui-corner-all"});

					_elements.push(infoContainer);

					addMouseEventsToInfoContainer(infoContainer, fileName, location, nodename, namespace, name, match);

					if(name && name.length > 0)
					{
						infoContainer.text(name);
					}
					if(match && match.length > 0)
					{
						infoContainer.text(match);
					}

					if(_templateSelector.children("div").length > 0)
					{
						/*
						var spacer = $("<div>&gt;&gt;</div>");
						spacer.addClass("gbSpacer");

						_templateSelector.prepend(spacer);
						_elements.push(spacer);
						*/
					}

					_templateSelector.prepend(infoContainer);

					//resizeContainers();
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
			}
		});
	}
	
	var fixTitle = function()
	{
		$("title").text($("title").text().replace(/<[^>]*>/g, ""));
	}
	
	var resizeContainers = function()
	{
		var templates = _templateSelector.children(".gbTemplateContainer");
		var spacers = _templateSelector.children(".gbSpacer");
		
		var templateWidth = (79/templates.length) + "%";
		templates.css("width", templateWidth);
		
		if(spacers.length > 0)
		{
			var spacersWidth = (19/spacers.length) + "%";
			spacers.css("width", spacersWidth);
		}
	}
	
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
				url = url.replace("debug=0", "");

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

$(window).load(function()
{
	var debugWidget = new DebugWidget();
	debugWidget.init();
});
