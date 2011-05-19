/*
function loadAsync(sUri, SOAPMessage) {
	var xmlHttp = XmlHttp.create();
	xmlHttp.open("POST", sUri, true);
	xmlHttp.onreadystatechange = function () {
		if (xmlHttp.readyState == 4){
			getTitle2(xmlHttp.responseXML, xmlHttp.responseText);
		}     
	}
	xmlHttp.setRequestHeader("SOAPAction", " ");
	xmlHttp.setRequestHeader("Content-Type", "Content-Type: text/xml; charset=utf-8");

	xmlHttp.send(SOAPMessage);
}
*/

function messageToSOAP(message) {
	return ['<?xml version="1.0" encoding="UTF-8"?>', 
		'<soapenv:Envelope ',
		'xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"',
		'xmlns:xsd="http://www.w3.org/2001/XMLSchema"',
		'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">',
			'<soapenv:Body>', 
				message, 
			'</soapenv:Body>',
		'</soapenv:Envelope>'].join("");
}

function getText(element) { //get the text from possibly multiple text nodes
	if (element.hasChildNodes()) {
		var tempString = [];
		for (var j = 0; j < element.childNodes.length; j++) {
			if (element.childNodes[j].nodeType == Node.TEXT_NODE ) { // =2
				tempString.push(element.childNodes[j].nodeValue);
			} 
			else {
				tempString.push('non text node: ');
			}
		} 
		return tempString.join("");
	}
	else {
		return 'getText: element has no ChildNodes from which to extract Text';
	}
}

function newOpenTag(name) {
	return ['<', name, '>'].join("");
}

function newCloseTag(name) {
	return ['</', name, '>'].join("");
}

function newEmptyTag(name) {
	return ['<', name, '/>'].join("");
}

function newElement(name, content) {
	return ['<', name, '>', content, '</', name, '>'].join("");
}

function newElementAtt1(name, content, attName, attValue) {
	return ['<', name, ' ', attName, '="', attValue, '">', content, '</', name, '>'].join("");
}

function newElementAtt(name, content, nameArray, valueArray) {
	var e = ['<', name, ' '];
	for (i=0; i < nameArray.length; i++) {
		e.push(newAttribute(nameArray[i], valueArray[i]));
	}
	e.push(['>', content, '</', name, '>']);
	return e.join("");
}

function newAttribute(name, value) {
	return [' ', name, '="', value, '"'].join("");
}

function countElementChildren(node) {
	var count = 0;
	var childList = node.childNodes;
	for(var i=0; i < (childList.length); i++) {
		var childNode = childList.item(i);
		if ((childNode.nodeType == 1))	{ // only count elements
			count++;
		}
	}
	return count;
}

function removeAllChildren(node) {
	while (node.hasChildNodes()) {
		node.removeChild(node.firstChild);
	}
}

function isElement(node) {
	if (node.nodeType == 1) {
		return true; }
	else {
		return false;
	}
}