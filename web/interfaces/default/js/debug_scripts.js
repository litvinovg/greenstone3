function DebugWidget()
{
	//Private member variables
	var _debugOn = false;
	var _pauseSelector = false;
	var _elements = new Array();
	var _itemSelected = false; //Used to prevent multiple elements from being highlighted
	var _mainDiv;
	var _textDiv;
	var _unpauseButton;
	
	var createDebugDiv = function()
	{
		_mainDiv = $("<div>", {"id":"debugDiv", "class":"ui-corner-all"});
		_mainDiv.css(
		{
			"position":"fixed", 
			"font-size":"0.7em", 
			"bottom":"0px",  
			"height":"25%", 
			"width":"100%", 
			"background":"white", 
			"border":"1px black solid", 
			"padding":"5px"
		});

		var toolBarDiv = $("<div>");
		toolBarDiv.css({"height":"15%"});
		_textDiv = $("<div>");
		_textDiv.css({"overflow":"auto", "width":"100%", "height":"85%", "margin-top":"5px"});
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
		
		toolBarDiv.append(pickElementButton);
		toolBarDiv.append(_unpauseButton);
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
				var surroundingDiv = $("<div>");
				surroundingDiv.css("cursor","pointer");
				var fromDIV = $("<div>");
				var elementDIV = $("<div>");
				elementDIV.css("margin-bottom", "0.6em");

				surroundingDiv.append(fromDIV);
				surroundingDiv.append(elementDIV);
				
				_elements.push(surroundingDiv);

				surroundingDiv.click(function()
				{
					var editArea = $("<textarea>");
					$.ajax();
				});
				
				surroundingDiv.mouseover(function()
				{
					$(this).data("background", $(this).css("background"));
					$(this).css("background", "yellow");
				});
				
				surroundingDiv.mouseout(function()
				{
					$(this).css("background", $(this).data("background"));
				});
				
				var nodeName = $(this).attr("nodename");
				var filename = $(this).attr("filename");
				var attrstr = "";
				var illegalNames = ["nodename", "filename", "style", "debug", "id", "class"];

				$(this.attributes).each(function()
				{
					for(var i = 0; i < illegalNames.length; i++)
					{
						if(this.name == illegalNames[i]){return;}
					}
					attrstr += this.name + "=\"" + this.value + "\" ";
				});
				
				fromDIV.text("From " + filename + ":");
				elementDIV.text("<" + nodeName + " " + attrstr + ">");
				
				_textDiv.prepend(surroundingDiv);
				
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
