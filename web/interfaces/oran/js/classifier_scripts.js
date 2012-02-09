var inProgress = new Array();
var openClassifiers = new Array();
var busy = false;

function isExpanded(sectionID)
{
	var divElem = document.getElementById("div" + sectionID);
	if(!divElem.style.display || divElem.style.display == "block")
	{
		return true;
	}
	return false;
}

function toggleSection(sectionID)
{
	var section = document.getElementById("div" + sectionID);
	var sectionToggle = document.getElementById("toggle" + sectionID);
	
	if(sectionToggle == undefined)
	{
		return;
	}
	
	if(section)
	{
		if(isExpanded(sectionID))
		{
			section.style.display = "none";
			sectionToggle.setAttribute("src", gs.imageURLs.expand);
			
			if(openClassifiers[sectionID] != undefined)
			{
				delete openClassifiers[sectionID];
			}
		}
		else
		{
			section.style.display = "block";
			sectionToggle.setAttribute("src", gs.imageURLs.collapse);
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
		
		var sectionToggle = document.getElementById("toggle" + sectionID);
		sectionToggle.setAttribute("src", gs.imageURLs.loading);

		var url = document.URL;
		url = url.replace(/(&|\?)cl=[a-z\.0-9]+/gi, "$1cl=" + sectionID + "&excerptid=div" + sectionID);

		if(gs.cgiParams.berryBasket == "on")
		{
			url = url + "&berrybasket=on";
		}

		if(url.indexOf("#") != -1)
		{
			url = url.substring(0, url.indexOf("#"));
		}
		httpRequest.open('GET', url, true);
		httpRequest.onreadystatechange = function() 
		{
			if (httpRequest.readyState == 4) 
			{
				if (httpRequest.status == 200) 
				{
					var newDiv = document.createElement("div");										
					var sibling = document.getElementById("title" + sectionID);
					var parent = sibling.parentNode;
					if(sibling.nextSibling)
					{
						parent.insertBefore(newDiv, sibling.nextSibling);
					}
					else
					{
						parent.appendChild(newDiv);
					}
					
					newDiv.innerHTML = httpRequest.responseText;
					sectionToggle.setAttribute("src", gs.imageURLs.collapse);
					openClassifiers[sectionID] = true;	
					
					if(gs.cgiParams.berryBasket == "on")
					{
						checkout();
					}
					else if(gs.cgiParams.documentbasket == "on")
					{
						dmcheckout();
					}
					updateOpenClassifiers();
				}
				else
				{
					sectionToggle.setAttribute("src", gs.imageURLs.expand);
				}
				inProgress[sectionID] = false;
				busy = false;
			}
		}
		httpRequest.send();
	}
}