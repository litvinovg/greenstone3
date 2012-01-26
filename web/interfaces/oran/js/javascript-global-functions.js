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

gs.functions.buildCollections = function(collections, finalFunction)
{
	if(!collections || collections.length == 0)
	{
		console.log("List of collections to build is empty");
		return;
	}

	var counter = 0;
	var buildFunction = function()
	{
		var ajax = new gs.functions.ajaxRequest();
		ajax.open("GET", _baseURL + "?a=g&rt=r&ro=1&s=BuildCollection&s1.collection=" + collections[counter]);
		ajax.onreadystatechange = function()
		{
			if(ajax.readyState == 4 && ajax.status == 200)
			{
				var text = ajax.responseText;
				var xml = validateXML(text);

				if(!xml || checkForErrors(xml))
				{
					console.log("Could not build collection -> " + collections[counter] + ", aborting");
					return;
				}

				var status = xml.getElementsByTagName("status")[0];
				var pid = status.getAttribute("pid");

				startCheckLoop(pid, "BuildCollection", function()
				{
					var localAjax = new gs.functions.ajaxRequest();
					localAjax.open("GET", _baseURL + "?a=g&rt=r&ro=1&s=ActivateCollection&s1.collection=" + collections[counter], true);
					localAjax.onreadystatechange = function()
					{
						if(localAjax.readyState == 4 && localAjax.status == 200)
						{
							var localText = localAjax.responseText;
							var localXML = validateXML(localText);
							
							if(!xml || checkForErrors(xml))
							{
								console.log("Could not activate collection -> " + collections[counter] + ", aborting");
								return;
							}

							var localStatus = localXML.getElementsByTagName("status")[0];
							if(localStatus)
							{
								var localPID = localStatus.getAttribute("pid");
								startCheckLoop(localPID, "ActivateCollection", function()
								{
									if (++counter == _collections.length)
									{
										//Run this function once we are done building all the collections
										if(finalFunction){finalFunction();}
									}
									else
									{
										buildFunction();
									}
								});
							}
						}
					}
					localAjax.send();
				});
			}
		}
		ajax.send();
	}
	buildFunction();
}

function startCheckLoop(pid, serverFunction, callbackFunction)
{
	var ajaxFunction = function()
	{
		var ajax = new gs.functions.ajaxRequest();
		ajax.open("GET", _baseURL + "?a=g&rt=s&ro=1&s=" + serverFunction + "&s1.pid=" + pid, true);
		ajax.onreadystatechange = function()
		{
			if(ajax.readyState == 4 && ajax.status == 200)
			{
				var text = ajax.responseText;
				var xml = validateXML(text);
				
				if(!xml || checkForErrors(xml))
				{
					console.log("Could not check status of " + serverFunction + ", there was an error in the XML, aborting");		
					return;
				}

				var status = xml.getElementsByTagName("status")[0];
				var code = status.getAttribute("code");

				if (code == COMPLETED || code == SUCCESS)
				{
					callbackFunction();
				}
				else if (code == HALTED || code == ERROR)
				{
					console.log("Could not check status of " + serverFunction + ", there was an error on the server, aborting");
				}
				else
				{
					setTimeout(ajaxFunction, 1000);
				}
			}
		}
		ajax.send();
	}
	ajaxFunction();
}

function callMetadataServer(callingFunction, url, responseFunction)
{
	var ajax = new gs.functions.ajaxRequest();
	ajax.open("GET", url, true);
	ajax.onreadystatechange = function()
	{
		if(ajax.readyState == 4 && ajax.status == 200)
		{
			console.log("(" + callingFunction + ") Response received from server: " + ajax.responseText);
			
			if(responseFunction != null)
			{
				responseFunction(ajax.responseText);
			}
		}
		else if(ajax.readyState == 4)
		{
			console.log("(" + callingFunction + ") Failed");
		}
	}
}

/*************************
* SET METADATA FUNCTIONS *
*************************/

gs.functions.setImportMetadata = function(collection, site, documentID, metadataName, metadataValue, responseFunction)
{
	callMetadataServer("setImportMetadata", "cgi-bin/metadata-server.pl?a=set-import-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metavalue=" + metadataValue, responseFunction);
}

gs.functions.setArchivesMetadata = function(collection, site, documentID, metadataName, metadataPosition, metadataValue, responseFunction)
{
	if(metadataPosition != null)
	{
		callMetadataServer("setArchivesMetadata", "cgi-bin/metadata-server.pl?a=set-archives-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metapos=" + metadataPosition, responseFunction);
	}
	else if(metadataValue != null)
	{
		callMetadataServer("setArchivesMetadata", "cgi-bin/metadata-server.pl?a=set-archives-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metavalue=" + metadataValue, responseFunction);
	}
}

gs.functions.setIndexMetadata = function(collection, site, documentID, metadataName, metadataPosition, metadataValue, responseFunction)
{
	if(metadataPosition != null)
	{
		callMetadataServer("setIndexMetadata", "cgi-bin/metadata-server.pl?a=set-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metapos=" + metadataPosition, responseFunction);
	}
	else if(metadataValue != null)
	{
		callMetadataServer("setIndexMetadata", "cgi-bin/metadata-server.pl?a=set-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metavalue=" + metadataValue, responseFunction);
	}
}

gs.functions.setMetadata = function(collection, site, documentID, metadataName, metadataValue, responseFunction)
{
	var nameArray = ["setImportMetadata", "setArchivesMetadata", "setIndexMetadata"];
	var functionArray = ["set-import-metadata", "set-archives-metadata", "set-metadata"];
	
	for(var i = 0; i < nameArray.length; i++)
	{
		callMetadataServer(nameArray[i], "cgi-bin/metadata-server.pl?a=" + functionArray[i] + "&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metavalue=" + metadataValue, responseFunction);
	}
}

/*************************
* GET METADATA FUNCTIONS *
*************************/

gs.functions.getImportMetadata = function(collection, site, documentID, metadataName, responseFunction)
{
	var url = "cgi-bin/metadata-server.pl?a=get-import-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName;
	callMetadataServer("getImportMetadata", url, function(responseText)
	{
		var metadata = new GSMetadata(collection, site, documentID, metadataName, null, responseText);
		if(responseFunction != null)
		{
			responseFunction(metadata);
		}
	});
}

gs.functions.getArchivesMetadata = function(collection, site, documentID, metadataName, metadataPosition,  responseFunction)
{
	var url = "cgi-bin/metadata-server.pl?a=get-archives-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName;
	if(metadataPosition != null)
	{
		url += "&metapos=" + metadataPosition;
	}

	callMetadataServer("getArchivesMetadata", url, function(responseText)
	{
		var metadata = new GSMetadata(collection, site, documentID, metadataName, metadataPosition, responseText);
		if(responseFunction != null)
		{
			responseFunction(metadata);
		}
	});
}

gs.functions.getIndexMetadata = function(collection, site, documentID, metadataName, metadataPosition, responseFunction)
{
	var url = "cgi-bin/metadata-server.pl?a=get-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName;
	if(metadataPosition != null)
	{
		url += "&metapos=" + metadataPosition;
	}

	callMetadataServer("getIndexMetadata", url, function(responseText)
	{
		var metadata = new GSMetadata(collection, site, documentID, metadataName, metadataPosition, responseText);
		
		if(responseFunction != null)
		{
			responseFunction(metadata);
		}
	});
}

/****************************
* REMOVE METADATA FUNCTIONS *
****************************/

gs.functions.removeImportMetadata = function(collection, site, documentID, metadataName, metadataValue, responseFunction)
{
	callMetadataServer("removeImportMetadata", "cgi-bin/metadata-server.pl?a=remove-import-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metavalue=" + metadataValue, responseFunction);
}

gs.functions.removeArchivesMetadata = function(collection, site, documentID, metadataName, metadataPosition, metadataValue, responseFunction)
{	
	if(metadataPosition != null)
	{
		callMetadataServer("removeArchiveMetadata", "cgi-bin/metadata-server.pl?a=remove-archive-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metapos=" + metadataPosition, responseFunction);
	}
	else if(metadataValue != null)
	{
		callMetadataServer("removeArchiveMetadata", "cgi-bin/metadata-server.pl?a=remove-archive-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metavalue=" + metadataValue, responseFunction);
	}
}

gs.functions.removeIndexMetadata = function(collection, site, documentID, metadataName, metadataPosition, metadataValue, responseFunction)
{
	if(metadataPosition != null)
	{
		callMetadataServer("removeIndexMetadata", "cgi-bin/metadata-server.pl?a=remove-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metapos=" + metadataPosition, responseFunction);
	}
	else if(metadataValue != null)
	{
		callMetadataServer("removeIndexMetadata", "cgi-bin/metadata-server.pl?a=remove-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metavalue=" + metadataValue, responseFunction);
	}
}

gs.functions.removeMetadata = function(collection, site, documentID, metadataName, metadataValue, responseFunction)
{
	var nameArray = ["removeImportMetadata", "removeArchivesMetadata", "removeIndexMetadata"];
	var functionArray = ["remove-import-metadata", "remove-archives-metadata", "remove-metadata"];
	
	for(var i = 0; i < nameArray.length; i++)
	{
		callMetadataServer(nameArray[i], "cgi-bin/metadata-server.pl?a=" + functionArray[i] + "&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metavalue=" + metadataValue, responseFunction);
	}
}
