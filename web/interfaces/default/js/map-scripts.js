var _docList = new Array();
_docList.ids = new Array();
_docList.getDocByIndex = function(index)
{
	return _docList[_docList.ids[index]];
};

var _map;
var _intervalHandle;
var _baseURL = document.URL.substring(0, document.URL.indexOf("?") + 1);
var _retrievedClassifiers = new Array();
var _preventLoopingSingleMarker = false;
var _searchRunning = false;
var _nearbyDocs = new Array();

function initializeMapScripts() 
{
	modifyFunctions();
	setUpMap();
	
	var jsonNodeDiv = $("#jsonNodes");
	if(jsonNodeDiv.length)
	{
		var jsonNodes = eval(jsonNodeDiv.html());
		if(jsonNodes && jsonNodes.length > 0)
		{
			for(var i = 0; i < jsonNodes.length; i++)
			{
				_docList[jsonNodes[i].nodeID] = jsonNodes[i];
				_docList.ids.push(jsonNodes[i].nodeID);
				createMarker(jsonNodes[i], true);
			}
			updateMap();
		}
		else
		{
			$("#map_canvas").css({visibility:"hidden", height:"0px"});
		}
	}
	
	_docList.loopIndex = 0;
	
	if(_docList.ids.length > 1)
	{
		var startStopCheckbox = $("<input>", {"type": "checkbox", "checked": "true", "id": "scrollCheckbox"});
		startStopCheckbox.click(function()
		{
			if(startStopCheckbox.attr("checked"))
			{
				if(_intervalHandle == null)
				{
					_intervalHandle = setInterval(loopThroughMarkers, 2000);
				}
			}
			else
			{
				clearInterval(_intervalHandle);
				_intervalHandle = null;
			}
		});
		
		var label = $("<span>Scroll through places</span>");
		var container = $("<div>", {"class": "ui-widget-header ui-corner-all", "style": "clear:right; float:right; padding:0px 5px 3px 0px;"});
		container.append(startStopCheckbox);
		container.append(label);

		$(container).insertAfter("#map_canvas");
		
		_intervalHandle = setInterval(loopThroughMarkers, 2000);
	}
}

function setUpMap()
{
	var myOptions = 
	{
		zoom: 2,
		center: new google.maps.LatLng(0, 0),
		mapTypeId: google.maps.MapTypeId.HYBRID
	};
	_map = new google.maps.Map($("#map_canvas")[0], myOptions);
	google.maps.event.addListener(_map, 'bounds_changed', performSearchForMarkers);
}

function performSearchForMarkers()
{
	if(_searchRunning)
	{
		return;
	}
	
	_searchRunning = true;
	
	var bounds = _map.getBounds();
	
	var neLat = bounds.getNorthEast().lat();
	var neLng = bounds.getNorthEast().lng();
	var swLat = bounds.getSouthWest().lat();
	var swLng = bounds.getSouthWest().lng();
	
	var latDistance = neLat - swLat;
	var lngDistance = neLng - swLng;
	
	console.log("neLat = " + neLat + " neLng = " + neLng + " swLat = " + swLat + " swLng = " + swLng + " latDistance = " + latDistance + " lngDistance = " + lngDistance);
	
	//Check which increment to use for latitude (i.e. 0.001, 0.01, 0.1 or 1 degree increments)
	var latDelta;
	var latPrecision;
	for(var i = 3; i >= 0; i--)
	{
		latDelta = (1 / Math.pow(10, i));
		if((latDistance / latDelta) <= 5 || latDelta == 1)
		{
			latPrecision = i;
			break;
		}
	}
	
	//Check which increment to use for longitude (i.e. 0.001, 0.01, 0.1 or 1 degree increments)
	var lngDelta;
	for(var i = 3; i >= 0; i--)
	{
		lngDelta = (1 / Math.pow(10, i));
		if((lngDistance / lngDelta) <= 5 || lngDelta == 1)
		{
			lngPrecision = i;
			break;
		}
	}
	
	if(latDelta == 0.1){latDelta = 1; latPrecision = 0;}
	if(lngDelta == 0.1){lngDelta = 1; lngPrecision = 0;}
	
	var query = "";
	for(var i = 0; i <= Math.floor(latDistance / latDelta) + 1; i++)
	{
		for(var j = 0; j <= Math.floor(lngDistance / lngDelta) + 1; j++)
		{
			//Some necessary variables
			var newLat = neLat - (latDelta * i);
			var newLatString = "" + newLat;
			var newLatTrunc;
			if(newLat < 0){newLatTrunc = Math.ceil(newLat);}
			else{newLatTrunc = Math.floor(newLat);}
			
			var newLng = neLng - (lngDelta * j);
			var newLngString = "" + newLng;
			var newLngTrunc;
			if(newLng < 0){newLngTrunc = Math.ceil(newLng);}
			else{newLngTrunc = Math.floor(newLng);}

			//Construct query
			query += "(";
			query += "LA:" + coordToAbsDirected(newLatTrunc, "lat");
			if(latDelta != 1)
			{ 
				query += "+AND+";
				query += "LA:" + newLatString.substring(newLatString.indexOf(".") + 1, newLatString.indexOf(".") + latPrecision + 1);
			}
			query += "+AND+";
			query += "LN:" + coordToAbsDirected(newLngTrunc, "lng");
			if(lngDelta != 1)
			{ 
				query += "+AND+";
				query += "LN:" + newLngString.substring(newLngString.indexOf(".") + 1, newLngString.indexOf(".") + lngPrecision + 1);
			}
			query += ")";

			if(i != (Math.floor(latDistance / latDelta) + 1) || j != (Math.floor(lngDistance / lngDelta) + 1)){ query += "+OR+"; }
		}
	}
	
	var url = gs.xsltParams.library_name + "?a=q&s=RawQuery&rt=rd&c=" + gs.cgiParams.c + "&s1.rawquery=" + query + "&excerptid=jsonNodes";
	
	$.ajax(url)
	.success(function(responseText)
	{
		if(responseText.search("id=\"jsonNodes") != -1)
		{
			var startIndex = responseText.indexOf(">");
			var endIndex = responseText.indexOf("</");

			var jsonNodes = eval(responseText.substring(startIndex+1, endIndex));
			if(jsonNodes && jsonNodes.length > 0)
			{
				for(var i = 0; i < jsonNodes.length; i++)
				{
					var doc = jsonNodes[i];
					
					var found = false;
					for(var j = 0; j < _docList.ids.length; j++){if(doc.nodeID == _docList.ids[j]){found = true; break;}}
					
					if(!found)
					{
						_docList[doc.nodeID] = doc;
						_docList.ids.push(doc.nodeID);

						createMarker(doc, false);
					}
				}
			}
		}
		else
		{
			console.log("No JSON information received");
		}
		
		_searchRunning = false;
	});
}

function coordToAbsDirected(coord, type)
{
	var value = "" + coord;
	if(coord < 0)
	{
		value = value.substring(1);
		if(type == "lat")
		{
			value += "S";	
		}
		else
		{
			value += "W";
		}
	}
	else
	{
		if(type == "lat")
		{
			value += "N";	
		}
		else
		{
			value += "E";
		}
	}
	
	return value;
}

function updateMap()
{
	var north = -180;
	var east = -180;
	var south = 180;
	var west = 180;

	var markersOnMap = 0;
	for(var i = 0; i < _docList.ids.length; i++)
	{
		var doc = _docList.getDocByIndex(i);

		if(doc.parentCL && doc.parentCL.style.display == "none")
		{
			doc.marker.setVisible(false);
			continue;
		}
		else
		{
			doc.marker.setVisible(true);
			markersOnMap++;
		}
		
		if(doc.lat > north)
		{
			north = doc.lat;
		}
		if(doc.lat < south)
		{
			south = doc.lat;
		}
		if(doc.lng > east)
		{
			east = doc.lng;
		}
		if(doc.lng < west)
		{
			west = doc.lng;
		}
	}
	
	//As there is always 2 possible bounding boxes we want the smaller of the two
	if(east - west > 180)
	{
		var temp = east;
		east = west;
		west = temp;
	}
	
	var bounds;
	if(markersOnMap > 0)
	{
		bounds = new google.maps.LatLngBounds(new google.maps.LatLng(south, west), new google.maps.LatLng(north, east));
		_map.fitBounds(bounds);
	}
}

function loopThroughMarkers()
{
	if(_docList.ids.length == 0)
	{
		return;
	}
	
	var visibleMarkers = new Array();
	for(var i = 0; i < _docList.ids.length; i++)
	{
		var doc = _docList.getDocByIndex(i);
		if(doc.marker.getVisible())
		{
			visibleMarkers.push(doc);
		}
	}
	
	if(visibleMarkers.length < 2)
	{
		clearAllInfoBoxes();
		return;
	}

	clearAllInfoBoxes();
	
	var elem = null;
	while(!elem)
	{
		if(_docList.loopIndex >= visibleMarkers.length)
		{
			_docList.loopIndex = 0;
		}

		var doc = visibleMarkers[_docList.loopIndex];
		var elem = gs.jqGet("div" + doc.nodeID);
		if(elem.length)
		{
			elem.css("background", "#BBFFBB");
			setTimeout(function(){elem.css("background", "");}, 2000);
		}
		_docList.loopIndex++;
	}
	doc.marker.markerInfo.open(_map, doc.marker);
}

function attachClickHandler(marker, nodeID)
{
	google.maps.event.addListener(marker, 'click', function()
	{
		document.location.href = gs.xsltParams.library_name + "?a=d&ed=1&c=" + gs.cgiParams.c + "&d=" + nodeID + "&dt=hierarchy&p.a=b&p.sa=&p.s=ClassifierBrowse";
	});
}

function focusDocument(id)
{
	var doc = _docList[id];
	if(doc)
	{
		clearInterval(_intervalHandle);
		_intervalHandle = null;
		_map.panTo(new google.maps.LatLng(doc.lat, doc.lng));
		clearAllInfoBoxes();
		doc.marker.markerInfo.open(_map, doc.marker);
		var scrollCheckbox = $("#scrollCheckbox");
		if(scrollCheckbox.checked)
		{
			scrollCheckbox.checked = false;
		}
	}
}

function clearAllInfoBoxes()
{
	for(var i = 0; i < _docList.ids.length; i++)
	{
		var doc = _docList.getDocByIndex(i);
		doc.marker.markerInfo.close();
	}
}

function createMarker(doc, mainMarker)
{
	var pos = new google.maps.LatLng(doc.lat,doc.lng);
	var marker
	if(mainMarker)
	{
		marker = new google.maps.Marker
		({
			position: pos,
			title:doc.title,
			map:_map
		});
	}
	else
	{
		marker = new google.maps.Marker
		({
			position: pos,
			title:doc.title,
			map:_map,
			icon:"interfaces/" + gs.xsltParams.interface_name + "/images/bluemarker.png"
		});
	}

	var docElement = gs.jqGet("div" + doc.nodeID);
	var parent;
	if(docElement)
	{
		parent = docElement.parentNode;
	}

	while(parent && parent.nodeName != "BODY")
	{
		if($(parent).attr("id") && $(parent).attr("id").search("divCL") != -1)
		{
			doc.parentCL = parent;
			break;
		}
		
		parent = parent.parentNode;
	}

	var info = new google.maps.InfoWindow({content:doc.title});
	marker.markerInfo = info;
	doc.marker = marker;

	attachClickHandler(marker, doc.nodeID);
}

function getSubClassifier(sectionID)
{
	var url = gs.xsltParams.library_name + "?a=b&rt=s&s=ClassifierBrowse&c=" + gs.cgiParams.c + "&cl=" + sectionID + "&excerptid=jsonNodes";
	$.ajax(url)
	.success(function(responseText)
	{
		var startIndex = responseText.indexOf(">");
		var endIndex = responseText.indexOf("</");
		
		var jsonNodes = eval(responseText.substring(startIndex+1, endIndex));
		if(jsonNodes && jsonNodes.length > 0)
		{
			for(var i = 0; i < jsonNodes.length; i++)
			{
				var doc = jsonNodes[i];
				_docList[doc.nodeID] = doc;
				_docList.ids.push(doc.nodeID);

				createMarker(doc, false);
			}
			
			$("#map_canvas").css({"visibility": "visible", "height": ""});
		}
		
		updateMap();
	})
	.error(function()
	{
		console.log("Error getting subclassifiers");
		return;
	});	
}

function performDistanceSearch(id, lat, lng, degrees)
{
	if(parseFloat(lat) > 180 || parseFloat(lat) < -180 || parseFloat(lng) > 180 || parseFloat(lat) < -180)
	{
		console.log("Latitude or longitude incorrectly formatted");
		return;
	}

	if(lat.indexOf(".") == -1 || lng.indexOf(".") == -1 || (lat.indexOf(".") + 3) >= lat.length || (lng.indexOf(".") + 3) >= lng.length)
	{
		console.log("Latitude or longitude does not have the required precision for a distance search");
		return;
	}
	
	var query = "";
	for(var i = 0; i < degrees * 2; i++)
	{
		for (var j = 0; j < degrees * 2; j++)
		{
			var latDelta = (i - degrees) * 0.01;
			var lngDelta = (j - degrees) * 0.01;
			
			query += "(" + getDistanceQueryString(lat, latDelta, 2, "LA", ["N","S"]);
			query += "+AND+";
			query += getDistanceQueryString(lng, lngDelta, 2, "LN", ["E","W"]) + ")";
			
			if(i != ((degrees * 2) - 1) || j != ((degrees * 2) - 1)){ query += "+OR+"; }
		}
	}

	var inlineTemplate = '\
	<xsl:template match="/" priority="5">\
		<table id="nearbyDocs">\
			<tr>\
				<th><a href="javascript:sortByDistance();">Distance (km)</a></th><th><a href="javascript:sortAlphabetically();">Document</a></th>\
			</tr>\
			<xsl:apply-templates select="//documentNode"/>\
		</table>\
	</xsl:template>\
	\
	<xsl:template match="documentNode" priority="5">\
		<tr>\
			<td>___<gsf:metadata name="Latitude"/>______<gsf:metadata name="Longitude"/>___</td>\
			<td><gsf:link type="document"><gsf:metadata name="Title"/></gsf:link></td>\
		</tr>\
	</xsl:template>';
	
	var url = gs.xsltParams.library_name + "?a=q&s=RawQuery&rt=rd&c=" + gs.cgiParams.c + "&s1.rawquery=" + query + "&excerptid=nearbyDocs&ilt=" + inlineTemplate.replace(/ /, "%20");
	$.ajax(url)
	.success(function(response)
	{
		response = response.replace(/<img src="[^"]*map_marker.png"[^>]*>/g, "");

		var nearbyDocsArray = new Array();

		var lats = new Array();
		var lngs = new Array();

		var matches = response.match(/___(-?[0-9\.]*)___/g);
		for(var i = 0; i < matches.length; i += 2)
		{
			var matchLatFloat = parseFloat(matches[i].replace("___", ""));
			var matchLngFloat = parseFloat(matches[i+1].replace("___", ""));

			lats.push(matchLatFloat);
			lngs.push(matchLngFloat);
			
			var distance = Math.sqrt(Math.pow(matchLatFloat - parseFloat(lat), 2) + Math.pow(matchLngFloat - parseFloat(lng), 2)) * (40000.0/360.0);
			var distanceString = "" + distance;
			distanceString = distanceString.substring(0, 6);

			response = response.replace(matches[i] + matches[i+1], distanceString);
		}
		
		var index = 0;
		var i = 0;
		while(true)
		{
			var distanceStart = response.indexOf("<td>", index);
			if(distanceStart == -1)
			{
				break;
			}
			var distanceEnd = response.indexOf("</td>", distanceStart);
			
			var docLinkStart = response.indexOf("<td>", distanceEnd);
			var docLinkEnd = response.indexOf("</td>", docLinkStart);
			
			var dist = response.substring(distanceStart + 4, distanceEnd);
			var docLink = response.substring(docLinkStart + 4, docLinkEnd);

			_nearbyDocs.push({title:docLink, distance:dist, lat:lats[i], lng:lngs[i++]});
			
			index = docLinkEnd;
		}
		
		sortByDistance();
		
		var toggle = $("#nearbyDocumentsToggle");
		toggle.attr("src", gs.imageURLs.collapse);
		gs.functions.makeToggle(toggle, $("#nearbyDocuments"));
	});
}

function sortByDistance()
{
	var sortedTable = '<table id="nearbyDocs"><tr><th><a href="javascript:;">Distance (km)</a></th><th><a href="javascript:sortAlphabetically();">Document</a></th></tr>';
	_nearbyDocs.sort(function(a, b){return (a.distance - b.distance);});
	for(var i = 0; i < _nearbyDocs.length; i++)
	{
		sortedTable += "<tr><td>" + _nearbyDocs[i].distance + '</td><td onmouseover="_map.setCenter(new google.maps.LatLng(' + _nearbyDocs[i].lat + ',' + _nearbyDocs[i].lng + '))">' + _nearbyDocs[i].title + "</td></tr>";
	}
	sortedTable += "</table>";
	
	$("#nearbyDocuments").html(sortedTable);
}

function sortAlphabetically()
{
	var sortedTable = '<table id="nearbyDocs"><tr><th><a href="javascript:sortByDistance();">Distance (km)</a></th><th><a href="javascript:;">Document</a></th></tr>';
	_nearbyDocs.sort(function(a, b)
	{
		var firstTitleStartIndex = a.title.indexOf(">");
		var firstTitleEndIndex = a.title.indexOf("<", firstTitleStartIndex);
		var firstTitle = a.title.substring(firstTitleStartIndex + 1, firstTitleEndIndex);
		var secondTitleStartIndex = b.title.indexOf(">");
		var secondTitleEndIndex = b.title.indexOf("<", secondTitleStartIndex);
		var secondTitle = b.title.substring(secondTitleStartIndex + 1, secondTitleEndIndex);
		return ((firstTitle.toLowerCase() == secondTitle.toLowerCase()) ? 0 : ((firstTitle.toLowerCase() > secondTitle.toLowerCase()) ? 1 : -1));
	});
	for(var i = 0; i < _nearbyDocs.length; i++)
	{
		sortedTable += "<tr><td>" + _nearbyDocs[i].distance + '</td><td onmouseover="_map.setCenter(new google.maps.LatLng(' + _nearbyDocs[i].lat + ',' + _nearbyDocs[i].lng + '))">' + _nearbyDocs[i].title + "</td></tr>";
	}
	sortedTable += "</table>";
	
	$("#nearbyDocuments").html(sortedTable);
}

function getDistanceQueryString(currentCoord, delta, precision, indexName, dirs)
{
	var query = "";
	var coordFloat = parseFloat(currentCoord);

	var newCoord = "" + (coordFloat + delta);
	var beforeDec = newCoord.substring(0, newCoord.indexOf("."));

	var dir = dirs[0];
	if(coordFloat < 0)
	{
		dir = dirs[1];
		beforeDec = beforeDec.substring(1);
	}
	beforeDec += dir;

	var afterDec = newCoord.substring(newCoord.indexOf(".") + 1, newCoord.indexOf(".") + (precision) + 1);

	return indexName + ":" + beforeDec + "+AND+" + indexName + ":" + afterDec;
}

function modifyFunctions()
{
	toggleSection = function(sectionID)
	{
		var section = gs.jqGet("div" + sectionID);
		var sectionToggle = gs.jqGet("toggle" + sectionID);
		
		if(sectionToggle == undefined)
		{
			return;
		}
		
		if(section)
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
			updateMap();
		}
		else
		{
			httpRequest(sectionID);
		}
	}
	
	httpRequest = function(sectionID)
	{
		if(!inProgress[sectionID])
		{
			inProgress[sectionID] = true;
			
			var sectionToggle = gs.jqGet("toggle" + sectionID);
			sectionToggle.attr("src", gs.imageURLs.loading);

			var url = gs.xsltParams.library_name + "/collection/" + gs.cgiParams.c + "/browse/" + sectionID.replace(/\./g, "/") + "?excerptid=div" + sectionID;

			if(gs.cgiParams.berryBasket == "on")
			{
				url = url + "&berrybasket=on";
			}

			if(url.indexOf("#") != -1)
			{
				url = url.substring(0, url.indexOf("#"));
			}
			
			$.ajax(url)
			.success(function(responseText)
			{
				var newDiv = $("<div>");										
				var sibling = gs.jqGet("title" + sectionID);
				sibling.before(newDiv);
				
				newDiv.html(responseText);
				sectionToggle.attr("src", gs.imageURLs.collapse);
				openClassifiers[sectionID] = true;	
				
				if(gs.cgiParams.berryBasket == "on")
				{
					checkout();
				}
				updateOpenClassifiers();
				getSubClassifier(sectionID);
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
}
