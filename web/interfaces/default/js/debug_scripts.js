function DebugWidget()
{
	//************************
	//Private member variables
	//************************
	
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
	
	var _templateSelector;
	var _editor;
	var _editingDiv;
	var _unpauseButton;
	var _closeEditorButton;
	var _xmlStatusBar;
	var _saveButton;
	var _swapEditorButton;
	
	//Editor state-keeping variables
	var _currentFilepath;
	var _currentNodename;
	var _currentName;
	var _currentMatch;
	var _currentNamespace;
	var _isVisualEditor = true;
	
	var _styleFunctions = new Array();
	
	var partialPageReload = function()
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
	}
	
	var callStyleFunctions = function()
	{
		for(var i = 0; i < _styleFunctions.length; i++)
		{
			var sFunction = _styleFunctions[i];
			sFunction();
		}
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

		var buttonDiv = $("<div>");
		buttonDiv.css("float", "left");
		toolBarDiv.append(buttonDiv);

		_templateSelector = $("<div>", {"id":"templateSelector"});
		_templateSelector.css({"overflow":"auto", "width":"100%"});

		var pickElementButton = $("<button>Enable debugging</button>");
		pickElementButton.click(function()
		{
			if(!_debugOn)
			{
				pickElementButton.button("option", "label", "Disable debugging");
				$("a").click(function(e)
				{
					e.preventDefault();
				});
				_debugOn = true;
			}
			else
			{
				pickElementButton.button("option", "label", "Enable debugging");
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
		_styleFunctions.push(function(){_closeEditorButton.button({icons:{primary:"ui-icon-newwin"}, disabled:true})});
		
		_saveButton = $("<button>Save changes</button>");
		_saveButton.click(function()
		{
			if(_editor)
			{
				var xmlString;
				if(_isVisualEditor)
				{
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
				var parameters = {"a":"g", "rt":"r", "s":"SaveXMLTemplateToFile", "s1.filePath":_currentFilepath, "s1.namespace":_currentNamespace, "s1.nodename":_currentNodename, "s1.xml":xmlString};

				if(_currentName && _currentName.length > 0){parameters["s1.name"] = _currentName;}
				if(_currentMatch && _currentMatch.length > 0){parameters["s1.match"] = _currentMatch;}

				_saveButton.button("option", "label", "Saving...");
				_saveButton.button("option", "disabled", true);

				$.post(url, parameters)
				.success(function()
				{
					$.ajax(gs.xsltParams.library_name + "?a=s&sa=c")
					.success(function()
					{
						alert("The template has been saved successfully.");
					})
					.error(function()
					{
						alert("Error reloading collection.");
					})
					.complete(function()
					{
						_saveButton.button("option", "label", "Save changes");
						_saveButton.button("option", "disabled", false);
						partialPageReload();
					});
				})
				.error(function()
				{
					alert("There was an error sending the request to the server, please try again.");
				});
			}
		});
		_styleFunctions.push(function(){_saveButton.button({icons:{primary:"ui-icon-disk"}, disabled:true})});
		
		_swapEditorButton = $("<button>Switch to XML Editor</button>");
		_swapEditorButton.button().click(function()
		{
			if(_vEditor && _textEditor)
			{
				if(_isVisualEditor)
				{
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
					_textEditor.show();
					_swapEditorButton.button("option", "label", "Switch to Visual Editor");
					_isVisualEditor = false;
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
					_swapEditorButton.button("option", "label", "Switch to XML Editor");
					_isVisualEditor = true;
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

		_xmlStatusBar = $("<span>");
		_xmlStatusBar.css("padding", "5px");
		_xmlStatusBar.addClass("ui-corner-all");
		
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
				if(_swapEditorButton.button("option", "label") == "Switch to Visual Editor")
				{
					_swapEditorButton.button("option", "disabled", false);
				}
			}
			
		}, 2000);
		
		var clear = $("<span>");
		clear.css("clear", "both");
		toolBarDiv.append(clear);
		
		buttonDiv.append(pickElementButton);
		buttonDiv.append(_unpauseButton);
		buttonDiv.append(_closeEditorButton);
		buttonDiv.append(_saveButton);
		buttonDiv.append(_swapEditorButton);
		buttonDiv.append(undoButton);
		buttonDiv.append(_xmlStatusBar);
		
		_styleFunctions.push(function(){$(".ui-button").css({"margin-right":"0.5em"});});
		
		_mainDiv.append(toolBarDiv);
		_mainDiv.append(_editingDiv);
		_mainDiv.append("<div>Templates:</div>");
		_mainDiv.append(_templateSelector);
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
	
	var addMouseEventsToInfoContainer = function(infoContainer, filepath, nodename, namespace, name, match)
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
		
			_currentFilepath = filepath;
			_currentNodename = nodename;
			_currentNamespace = namespace;
			_currentName = name;
			_currentMatch = match;
		
			var responseName = "requestedNameTemplate";
		
			var url = gs.xsltParams.library_name + "?a=g&rt=r&s=RetrieveXMLTemplateFromFile&s1.filePath=" + _currentFilepath + "&s1.namespace=" + _currentNamespace + "&s1.nodename=" + _currentNodename;
			if(_currentMatch && _currentMatch.length > 0){url += "&s1.match=" + _currentMatch; responseName = "requestedMatchTemplate";}
			if(_currentName && _currentName.length > 0){url += "&s1.name=" + _currentName;}
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
				_editingDiv.append($("<p>" + filepath + "</p>"));
				_editingDiv.append(_textEditor);
				
				_vEditor = new visualXMLEditor(template);
				_editingDiv.append(_vEditor.getMainDiv());
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
				
				_textEditor.css({"min-height":"200px", "border-top":"5px solid #444"});
				_textEditor.resizable({handles: 'n', resize:function()
				{
					_textEditor.css({top:"0px"});
					_editor.resize();
				}});

				_closeEditorButton.button("option", "disabled", true);
			})
			.error(function()
			{
				console.log("ERROR");
			});
		});
		infoContainer.mouseover(function()
		{
			$(this).data("background", $(this).css("background"));
			$(this).css("background", "yellow");
		});
		infoContainer.mouseout(function()
		{
			$(this).css("background", $(this).data("background"));
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
					
					var infoContainer = $("<div>");
					infoContainer.addClass("gbTemplateContainer");
					
					_elements.push(infoContainer);
					
					addMouseEventsToInfoContainer(infoContainer, filepath, nodename, namespace, name, match);
					
					/*
					var attrstr = "";
					var illegalNames = ["nodename", "filename", "style", "debug", "id", "class"];

					var attributes = ((this.tempAttrs) ? this.tempAttrs : this.attributes);
					
					$(attributes).each(function()
					{
						for(var i = 0; i < illegalNames.length; i++)
						{
							if(this.name == illegalNames[i]){return;}
						}
						attrstr += this.name + "=\"" + this.value + "\" ";
					});

					infoContainer.text("<" + fullNodename + " " + attrstr + ">");
					*/
					
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
						var spacer = $("<div>&gt;&gt;</div>");
						spacer.addClass("gbSpacer");

						_templateSelector.prepend(spacer);
						_elements.push(spacer);
					}
					
					_templateSelector.prepend(infoContainer);
					
					resizeContainers();
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
			console.log("No debug tags present, debugging disabled.");
			return;
		}
		
		createDebugDiv();
		$("body").append(_mainDiv);
		
		callStyleFunctions();

		addMouseEventsToDebugElements(debugElems);
	}
	
}

$(window).load(function()
{
	var debugWidget = new DebugWidget();
	debugWidget.init();
});
