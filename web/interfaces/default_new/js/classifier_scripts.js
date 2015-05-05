var inProgress = new Array();
var openClassifiers = new Array();
var busy = false;

function isExpanded(sectionID)
{
	var divElem = gs.jqGet("div" + sectionID);
	if(!divElem.css("display") || divElem.css("display") != "none")
	{
		return true;
	}
	return false;
}

function toggleSection(sectionID)
{
	var section = gs.jqGet("div" + sectionID);
	var sectionToggle = gs.jqGet("toggle" + sectionID);
	
	if(sectionToggle == undefined)
	{
		return;
	}
	
	//If the div exists
	if(section.length)
	{
		if(isExpanded(sectionID))
		{
			section.css("display", "none");
			sectionToggle.attr("src", gs.imageURLs.expand);

			if(openClassifiers[sectionID] != undefined)
			{
				delete openClassifiers[sectionID];
			}
		}
		else
		{
			section.css("display", "block");
			sectionToggle.attr("src", gs.imageURLs.collapse);
			openClassifiers[sectionID] = true;	
		}
		updateOpenClassifiers();
	}
	else
	{
		httpRequest(sectionID);
	}
}

function updateOpenClassifiers()
{
	var oc = "";
	var first = true;
	for(var key in openClassifiers)
	{
		if(first)
		{
			first = false;
		}
		else
		{
			oc += ",";
		}
		
		oc += key;
	}
	
	if(oc != undefined)
	{
		window.location.hash = oc;
	}
}

function openStoredClassifiers()
{
	if(window.location.hash != undefined && window.location.hash.length > 1)
	{
		var toOpen = window.location.hash.substring(1,window.location.hash.length).split(",");
		var loopFunction = function(sectionArray, index)
		{
			if(!busy && index < sectionArray.length)
			{
				busy = true;
				toggleSection(sectionArray[index]);
				setTimeout(function()
				{
					loopFunction(sectionArray, index + 1);
				}, 25);
				
				return true;
			}
			
			setTimeout(function()
			{
				loopFunction(sectionArray, index);
			}, 25);
			return false;
		}
		
		if(toOpen.length > 0)
		{
			loopFunction(toOpen, 0);
		}
	}
}

function httpRequest(sectionID)
{
	if(!inProgress[sectionID])
	{
		inProgress[sectionID] = true;
		var httpRequest = new gs.functions.ajaxRequest();
		
		var sectionToggle = gs.jqGet("toggle" + sectionID);
		sectionToggle.attr("src", gs.imageURLs.loading);
		
		var url = gs.xsltParams.library_name + "/collection/" + gs.cgiParams.c + "/browse/" + sectionID.replace(/\./g, "/") + "?excerptid=div" + sectionID;

		if(gs.cgiParams.berrybasket == "on")
		{
			url = url + "&berrybasket=on";
		} 

		if(url.indexOf("#") != -1)
		{
			url = url.substring(0, url.indexOf("#"));
		}
		
		$.ajax(url)
		.success(function(data)
		{
			var newDiv = $("<div>");										
			var sibling = gs.jqGet("title" + sectionID);
			sibling.after(newDiv);
			
			newDiv.html(data);
			sectionToggle.attr("src", gs.imageURLs.collapse);
			openClassifiers[sectionID] = true;	
			
			if(gs.cgiParams.berrybasket == "on")
			{
				checkout();
			}
			else if(gs.cgiParams.documentbasket == "on")
			{
				dmcheckout();
			}
			updateOpenClassifiers();
		})
		.error(function()
		{
			sectionToggle.attr("src", gs.imageURLs.expand);
		})
		.complete(function()
		{
			inProgress[sectionID] = false;
			busy = false;
		});
	}
}