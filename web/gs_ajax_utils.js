

function loadAsync(sUri, SOAPMessage) {
   var xmlHttp = XmlHttp.create();
   var async = true;
   xmlHttp.open("POST", sUri, async);
   xmlHttp.onreadystatechange = function () {
      if (xmlHttp.readyState == 4){
		var result = xmlHttp.responseText;
			getTitle2(xmlHttp.responseXML, xmlHttp.responseText);
    }     
   }
   xmlHttp.setRequestHeader("SOAPAction", " ");
   xmlHttp.setRequestHeader("Content-Type", "Content-Type: text/xml; charset=utf-8");
 
   xmlHttp.send(SOAPMessage);
}


function messageToSOAP(message) {
    soapBody = '<soapenv:Body>' + message + '</soapenv:Body>'
    soap = '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' + soapBody + '</soapenv:Envelope>'
    x= '<?xml version="1.0" encoding="UTF-8"?>' + soap;
    return x;
}




function getText(element) { //get the text from possibly multiple text nodes
if (element.hasChildNodes()) {
	tempString = '';
    for (j=0; j < element.childNodes.length; j++) {
    				if (element.childNodes[j].nodeType == Node.TEXT_NODE ) { // =2
    					tempString += element.childNodes[j].nodeValue;
    				} 
					else {
						tempString += 'non text node: ';
					}
    } 
	return tempString;
}
else {
	return 'getText: element has no ChildNodes from which to extract Text';
}
}


function newOpenTag(name) {
	return '<' + name + '>';
}

function newCloseTag(name) {
	return '</' + name + '>';
}

function newEmptyTag(name) {
	return '<' + name + '/>';
}

function newElement(name, content) {
    e = '<' + name + '>' + content;
    e += '</' + name + '>';
    return e;
}


function newElementAtt1(name, content, attName, attValue) {
    e = '<' + name + ' ' + attName + '="' + attValue +'">' + content;
    e += '</' + name + '>';
    return e;
}




function newElementAtt(name, content, nameArray, valueArray) {
    e = '<' + name + ' ' ;
    for (i=0; i < nameArray.length; i++) {
        e += newAttribute(nameArray[i], valueArray[i])
    }
    e += '>' + content;
    e += '</' + name + '>';
    return e;
}


function newAttribute(name, value) {
				 return ' ' + name + '="' + value + '"';
}

/*
var a = [];
var b = [];
a[0] = 'title';
b[0] = 'test';
a[1] = 'title2';
b[1] = 'test2';

alert( newElement('message', 'some content', a=[], b=[]));

*/

function countElementChildren(node) {
  	count= 0;
	childList = node.childNodes;
	for(var i=0; i < (childList.length); i++) {
		childNode = childList.item(i);
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