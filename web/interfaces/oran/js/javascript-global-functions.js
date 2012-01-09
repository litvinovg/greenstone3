gs.functions = new Array();

gs.functions.ajaxRequest = function()
{
	var activexmodes=["Msxml2.XMLHTTP", "Microsoft.XMLHTTP"]; 
	if(window.ActiveXObject)
	{ 
		for (var i=0; i<activexmodes.length; i++)
		{
			try
			{
				return new ActiveXObject(activexmodes[i]);
			}
			catch(e){}
		}
	}
	else if (window.XMLHttpRequest)
	{
		return new XMLHttpRequest();
	}
	else
	{
		return false
	}
}

gs.functions.makeToggle = function(buttons, divs)
{
	var buttonArray = (buttons.length) ? buttons : [buttons];
	var divArray = (divs.length) ? divs : [divs];
	
	for(var i = 0; i < buttonArray.length; i++)
	{
		buttonArray[i].onclick = function()
		{
			for(var j = 0; j < divArray.length; j++)
			{
				if(divArray[j].style.display == "none")
				{
					divArray[j].style.display = "block";
				}
				else
				{
					divArray[j].style.display = "none";
				}
			}
			
			for(var j = 0; j < buttonArray.length; j++)
			{
				if(buttonArray[j].getAttribute("src") == gs.imageURLs.collapse)
				{
					buttonArray[j].setAttribute("src", gs.imageURLs.expand);
				}
				else if(buttonArray[j].getAttribute("src") == gs.imageURLs.expand)
				{
					buttonArray[j].setAttribute("src", gs.imageURLs.collapse);
				}
			}
		};
	}
}