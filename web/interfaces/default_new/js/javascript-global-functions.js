var SUCCESS = 1;
var ACCEPTED = 2;
var ERROR = 3;
var CONTINUING = 10;
var COMPLETED = 11;
var HALTED = 12;

gs.functions = new Array();

gs.jqGet = function(id)
{
    return $("#" + id.replace(/\./g, "\\.").replace(/:/g,"\\:"));
}

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

gs.functions.hasClass = function(elem, classVal)
{
	if(!elem || !elem.getAttribute("class"))
	{
		return false;
	}

	return (elem.getAttribute("class").search(classVal) != -1)
}

gs.functions.getElementsByClassName = function(cl) 
{
	var nodes = new Array();
	var classRegEx = new RegExp('\\b'+cl+'\\b');
	var allElems = document.getElementsByTagName('*');
	
	for (var i = 0; i < allElems.length; i++) 
	{
		var classes = allElems[i].className;
		if (classRegEx.test(classes)) 
		{
			nodes.push(allElems[i]);
		}
	}
	return nodes;
}; 

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

gs.functions.checkForErrors = function(xml)
{
	var errorElems = xml.getElementsByTagName("error");
	
	if(errorElems && errorElems.length > 0)
	{
		var errorString = gs.text.dse.error_saving_changes + ": ";
		for(var i = 0; i < errorElems.length; i++)
		{
			errorString += " " + errorElems.item(i).firstChild.nodeValue;
		}
		alert(errorString);
		return true;
	}
	return false; //No errors
}

gs.functions.validateXML = function(txt)
{
	// code for IE
	if (window.ActiveXObject)
	{
		var xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
		xmlDoc.async = "false";
		xmlDoc.loadXML(document.all(txt).value);

		if(xmlDoc.parseError.errorCode!=0)
		{
			txt = dse.error_code + ": " + xmlDoc.parseError.errorCode + "\n";
			txt = txt + dse.error_reason + ": " + xmlDoc.parseError.reason;
			txt = txt + dse.error_line + ": " + xmlDoc.parseError.line;
			console.log(txt);
			return null;
		}
		
		return xmlDoc;
	}
	// code for Mozilla, Firefox, Opera, etc.
	else if (document.implementation.createDocument)
	{
		var parser = new DOMParser();
		var xmlDoc = parser.parseFromString(txt,"text/xml");

		if (xmlDoc.getElementsByTagName("parsererror").length > 0)
		{
			console.log(gs.text.dse.xml_error);
			return null;
		}
		
		return xmlDoc;
	}
	else
	{
		console.log(gs.text.dse.browse_cannot_validate_xml);
	}
	return null;
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
		ajax.open("GET", gs.xsltParams.library_name + "?a=g&rt=r&ro=1&s=BuildCollection&s1.collection=" + collections[counter]);
		ajax.onreadystatechange = function()
		{
			if(ajax.readyState == 4 && ajax.status == 200)
			{
				var text = ajax.responseText;
				var xml = gs.functions.validateXML(text);

				if(!xml || gs.functions.checkForErrors(xml))
				{
					console.log("Could not build collection -> " + collections[counter] + ", aborting");
					return;
				}

				var status = xml.getElementsByTagName("status")[0];
				var pid = status.getAttribute("pid");

				gs.functions.startCheckLoop(pid, "BuildCollection", function()
				{
					var localAjax = new gs.functions.ajaxRequest();
					localAjax.open("GET", gs.xsltParams.library_name + "?a=g&rt=r&ro=1&s=ActivateCollection&s1.collection=" + collections[counter], true);
					localAjax.onreadystatechange = function()
					{
						if(localAjax.readyState == 4 && localAjax.status == 200)
						{
							var localText = localAjax.responseText;
							var localXML = gs.functions.validateXML(localText);
							
							if(!xml || gs.functions.checkForErrors(xml))
							{
								console.log("Could not activate collection -> " + collections[counter] + ", aborting");
								return;
							}

							var localStatus = localXML.getElementsByTagName("status")[0];
							if(localStatus)
							{
								var localPID = localStatus.getAttribute("pid");
								gs.functions.startCheckLoop(localPID, "ActivateCollection", function()
								{
									if (++counter == collections.length)
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

gs.functions.startCheckLoop = function(pid, serverFunction, callbackFunction)
{
	var ajaxFunction = function()
	{
		var ajax = new gs.functions.ajaxRequest();
		ajax.open("GET", gs.xsltParams.library_name + "?a=g&rt=s&ro=1&s=" + serverFunction + "&s1.pid=" + pid, true);
		ajax.onreadystatechange = function()
		{
			if(ajax.readyState == 4 && ajax.status == 200)
			{
				var text = ajax.responseText;
				var xml = gs.functions.validateXML(text);
				
				if(!xml || gs.functions.checkForErrors(xml))
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

function inc(a, b)
{
	var carry = 0;
	var num = 0;
	var i = 0;
	
	while((carry || (i < a.length) || (i < b.length)) && (i < 100))
	{
		num = carry;
		if(i < a.length){num += a[i];}
		if(i < b.length){num += b[i];}
		
		if(num >= 256)
		{
			num -= 256;
			carry = 1;
		}
		else
		{
			carry = 0;
		}
		
		a[i] = num;
		
		i++;
	}
}

function ifposDec(a, b)
{
	var carry = 0;
	var num = 0;
	var i = 0;
	
	if(b.length > a.length){return a;}
	if(b.length == a.length)
	{
		i = a.length - 1;
		while(i >= 0)
		{
			if(a[i] > b[i]){break;}
			if(a[i] < b[i]){return a;}
			i--;
		}
	}
	
	i = 0;
	var len = 0;
	var outString = "";
	while((i < a.length) || (i < b.length))
	{
		num = -carry;
		if(i < a.length){num += a[i];}
		if(i < b.length){num -= b[i];}
		
		if(num < 0)
		{
			num += 256;
			carry = 1;
		}
		else
		{
			carry = 0;
		}
		
		a[i] = num;
		outString += num + ","
		i++
		
		if(num != 0){len = i}
	}

	if(len < a.length)
	{
		a = a.slice(0, len);
	}
	
	return a;
}

function convertNum(a)
{
	var result = new Array();
	var i;
	var convert = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"];
	
	if(a.length == 0)
	{
		result.push("0");
		return result;
	}
	
	for(i = a.length - 1; i >= 0; i--)
	{
		result.push(convert[Math.floor(a[i]/16)]);
		result.push(convert[Math.floor(a[i]%16)]);
	}
	
	var resultString = "";
	for(var j = 0; j < result.length; j++)
	{
		resultString += result[j];
	}
	
	return resultString;
}

gs.functions.hashString = function(str)
{
	var remainder = new Array();
	var primePow = new Array();
	var pow = 
	[
		255, 255, 255,
		255, 255, 255,
		255, 255, 255,
		255, 255, 1
	];
	
	for(var i = 0; i < 8; i++)
	{
		primePow.push(pow.slice()); //The javascript way to do an array copy (yuck!)
		inc(pow, pow);
	}
	
	for(var i = 0; i < str.length; i++)
	{
		var c = str.charCodeAt(i);

		if(remainder.length == 99)
		{
			return null;
		}

		for(var j = remainder.length; j > 0; j--)
		{
			remainder[j] = remainder[j-1];
		}
		remainder[0] = c;

		for(var j = 7; j >= 0; j--)
		{
			remainder = ifposDec(remainder, primePow[j]);
		}	
	}
	
	return convertNum(remainder);
}

function callMetadataServer(callingFunction, url, responseFunction)
{
	$.ajax(url)
	.success(function(response)
	{
		console.log("(" + callingFunction + ") Response received from server: " + response);

		//var xml = $.parseXML(response);
		//console.log(xml);
		
		if(responseFunction != null)
		{
			responseFunction(response);
		}
	})
	.error(function()
	{
		console.log("(" + callingFunction + ") Failed");
	});
}

/*************************
* SET METADATA FUNCTIONS *
*************************/

gs.functions.setImportMetadata = function(collection, site, documentID, metadataName, metadataValue, prevMetadataValue, metamode, responseFunction)
{
	callMetadataServer("setImportMetadata", "cgi-bin/metadata-server.pl?a=set-import-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metavalue=" + metadataValue + "&prevmetavalue=" + prevMetadataValue + "&metamode=" + metamode, responseFunction);
}

gs.functions.setArchivesMetadata = function(collection, site, documentID, metadataName, metadataPosition, metadataValue, prevMetadataValue, metamode, responseFunction)
{
	if(metadataPosition != null)
	{
		callMetadataServer("setArchivesMetadata", "cgi-bin/metadata-server.pl?a=set-archives-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metapos=" + metadataPosition + "&metavalue=" + metadataValue + "&metamode=" + metamode, responseFunction);
	}
	else if(prevMetadataValue != null)
	{
		callMetadataServer("setArchivesMetadata", "cgi-bin/metadata-server.pl?a=set-archives-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metavalue=" + metadataValue + "&prevmetavalue=" + prevMetadataValue + "&metamode=" + metamode, responseFunction);
	}
	else
	{
		callMetadataServer("setArchivesMetadata", "cgi-bin/metadata-server.pl?a=set-archives-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metavalue=" + metadataValue + "&metamode=" + metamode, responseFunction);
	}
}

gs.functions.setIndexMetadata = function(collection, site, documentID, metadataName, metadataPosition, metadataValue, prevMetadataValue, metamode, responseFunction)
{
	if(metadataPosition != null)
	{
		callMetadataServer("setIndexMetadata", "cgi-bin/metadata-server.pl?a=set-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metapos=" + metadataPosition + "&metavalue=" + metadataValue + "&metamode=" + metamode, responseFunction);
	}
	else if(prevMetadataValue != null)
	{
		callMetadataServer("setIndexMetadata", "cgi-bin/metadata-server.pl?a=set-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metavalue=" + metadataValue + "&prevmetavalue=" + prevMetadataValue + "&metamode=" + metamode, responseFunction);
	}
}

gs.functions.setMetadata = function(collection, site, documentID, metadataName, metadataValue, metamode, responseFunction)
{
	var nameArray = ["setImportMetadata", "setArchivesMetadata", "setIndexMetadata"];
	var functionArray = ["set-import-metadata", "set-archives-metadata", "set-metadata"];
	
	for(var i = 0; i < nameArray.length; i++)
	{
		callMetadataServer(nameArray[i], "cgi-bin/metadata-server.pl?a=" + functionArray[i] + "&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metavalue=" + metadataValue + "&metamode=" + metamode, responseFunction);
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
		callMetadataServer("removeArchiveMetadata", "cgi-bin/metadata-server.pl?a=remove-archives-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metapos=" + metadataPosition, responseFunction);
	}
	else if(metadataValue != null)
	{
		callMetadataServer("removeArchiveMetadata", "cgi-bin/metadata-server.pl?a=remove-archives-metadata&c=" + collection + "&site=" + site + "&d=" + documentID + "&metaname=" + metadataName + "&metavalue=" + metadataValue, responseFunction);
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
