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
	
	//Page elements
	var _mainDiv;
	var _textDiv;
	var _editingDiv;
	var _unpauseButton;
	var _closeEditorButtonButton;
	var _xmlStatusBar;
	var _saveButton;
	
	//Editor state-keeping variables
	var _currentFilepath;
	var _currentNodename;
	var _currentName;
	var _currentMatch;
	var _currentNamespace;
	
	var createDebugDiv = function()
	{
		_mainDiv = $("<div>", {"id":"debugDiv"});
		_mainDiv.css(
		{
			"position":"fixed", 
			"font-size":"0.7em", 
			"bottom":"0px",  
			"height":"300px", 
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
		_textDiv = $("<div>");
		_textDiv.css({"overflow":"auto", "width":"100%", "height":"260px"});

		var pickElementButton = $("<input type=\"button\" value=\"Enable debugging\">");
		pickElementButton.click(function()
		{
			if(!_debugOn)
			{
				pickElementButton.attr("value", "Disable debugging");
				$("a").click(function(e)
				{
					e.preventDefault();
				});
				_debugOn = true;
			}
			else
			{
				pickElementButton.attr("value", "Enable debugging");
				$("a").off("click");
				clearAll();
				_unpauseButton.attr("disabled", "disabled");
				_pauseSelector = false;
				_debugOn = false;
			}
		});

		_unpauseButton = $("<input type=\"button\" value=\"Select new element\" disabled=\"disabled\">");
		_unpauseButton.click(function()
		{
			if(_pauseSelector)
			{
				_pauseSelector = false;
				$(this).attr("disabled", "disabled");
			}
		});
		
		_closeEditorButton = $("<input type=\"button\" value=\"Close editor\" disabled=\"disabled\">");
		_closeEditorButton.click(function()
		{
			if($(this).val() == "Close editor")
			{
				$(this).val("Open editor");
				_editingDiv.hide();
				_mainDiv.css("height", (_mainDiv.height() - 200) + "px");
			}
			else
			{
				$(this).val("Close editor");
				_editingDiv.show();
				_mainDiv.css("height", (_mainDiv.height() + 200) + "px");
			}
		});
		
		_xmlStatusBar = $("<span>");
		_xmlStatusBar.css("padding", "5px");
		
		//Check the XML for errors every 2 seconds
		setInterval(function()
		{
			var editor = _editingDiv.find("textarea");
			if(editor.length)
			{
				var xmlString = editor.val();
				try
				{
					var xml = $.parseXML('<testContainer xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:java="http://xml.apache.org/xslt/java" xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil" xmlns:gslib="http://www.greenstone.org/skinning" xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat">' + xmlString + "</testContainer>");
				}
				catch(error)
				{
					console.log(error);
					_xmlStatusBar.text("XML ERROR! (Mouse over for details)");
					_xmlStatusBar.css({"color":"white", "background":"red"});
					_xmlStatusBar.attr("title", error);
					_saveButton.attr("disabled", "disabled");
					return;
				}
				
				_xmlStatusBar.text("XML OK!");
				_xmlStatusBar.css({"color":"white", "background": "green"});
				_xmlStatusBar.removeAttr("title");
				if(_saveButton.val() == "Save changes")
				{
					_saveButton.removeAttr("disabled");
				}
			}
			
		}, 2000);
		
		_saveButton = $("<input type=\"button\" value=\"Save changes\" disabled=\"disabled\">");
		_saveButton.click(function()
		{
			var editor = _editingDiv.find("textarea");
			if(editor.length)
			{
				var xmlString = editor.val().replace(/&/g, "&amp;");
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

				_saveButton.val("Saving...");
				_saveButton.attr("disabled", "disabled");

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
						_saveButton.val("Save changes");
						_saveButton.removeAttr("disabled");
					});
				})
				.error(function()
				{
					alert("There was an error sending the request to the server, please try again.");
				});
			}
		});
		
		var minimiseButton = $("<img>", {"src":gs.imageURLs.collapse});
		minimiseButton.css({"cursor":"pointer", "float":"right", "margin-right":"20px"});
		minimiseButton.click(function()
		{
			if($(this).attr("src") == gs.imageURLs.collapse)
			{
				_textDiv.hide();
				$(this).attr("src", gs.imageURLs.expand);
				_mainDiv.css("height", (_mainDiv.height() - 260) + "px");
			}
			else
			{
				_textDiv.show();
				$(this).attr("src", gs.imageURLs.collapse);
				_mainDiv.css("height", (_mainDiv.height() + 260) + "px");
			}
		});
		
		var clear = $("<span>");
		clear.css("clear", "both");
		
		toolBarDiv.append(minimiseButton);
		toolBarDiv.append(clear);
		
		buttonDiv.append(pickElementButton);
		buttonDiv.append(_unpauseButton);
		buttonDiv.append(_closeEditorButton);
		buttonDiv.append(_xmlStatusBar);
		buttonDiv.append(_saveButton);
		_mainDiv.append(_editingDiv);
		_mainDiv.append(toolBarDiv);
		_mainDiv.append(_textDiv);
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
			
				var editArea = $("<textarea>");
				editArea.css({"width":"98%", "height":"180px"});
				editArea.val(template);
				
				_editingDiv.empty();
				_editingDiv.append(editArea);
				_editingDiv.css({"height":"190px"});
				
				_mainDiv.css({"height":"500px"});
				
				_closeEditorButton.removeAttr("disabled");
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
				_unpauseButton.removeAttr("disabled");
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
					infoContainer.css({"cursor":"pointer", "border":"1px dashed #AAAAAA", "margin":"5px"});
					var fromDIV = $("<div>");
					var elementDIV = $("<div>");

					elementDIV.css("font-size", "1.1em");
					fromDIV.css("font-size", "0.9em");

					infoContainer.append(fromDIV);
					infoContainer.append(elementDIV);
					
					_elements.push(infoContainer);
					
					addMouseEventsToInfoContainer(infoContainer, filepath, nodename, namespace, name, match);
					
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
					
					fromDIV.text("From " + filepath + ":");
					elementDIV.text("<" + fullNodename + " " + attrstr + ">");
					
					_textDiv.prepend(infoContainer);
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

		addMouseEventsToDebugElements(debugElems);
	}
	
}

$(window).load(function()
{
	var debugWidget = new DebugWidget();
	debugWidget.init();
});
