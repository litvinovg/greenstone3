/**
 * @author dnk2
 */

function showContents() {
	var contents = document.getElementById("contents") ;
	var contentsLink = document.getElementById("contentsLink") ;
	
	contents.style.left = contentsLink.offsetLeft + "px" ;
	contents.style.top = (contentsLink.offsetTop + contentsLink.offsetHeight) + "px" ;
	
	contents.onmouseout = new Function("event", "if (isMouseLeaveOrEnter(event, this)) hideContents() ;") ;
	
	var ul = getSublist(contents) ;
	
	for(var i=0 ; i<ul.childNodes.length ; i++) {
		collapseNode(ul.childNodes[i]) ;
	}
	
	var currNode = document.getElementById("currentSection") ;
	if (currNode != null) {
		//alert("current section found") ;
		currNode = currNode.parentNode ;
		expandAncestors(currNode) ;
	} else {
		//alert("no current section found") ;
	}
	
	
	contents.style.display = "block" ;
}


function expandAncestors(node) {
	
	var parent = node.parentNode.parentNode ;
	
	if (parent.id != "contents") {
		parent.className = "expanded" ;
		expandAncestors(parent) ;
	}	
}

function collapseNode(node) {
	if (node.className == "expanded")
		node.className = "collapsed" ;
}

function toggleNode(nodeId) {
	
	var node = document.getElementById(nodeId) ;

	var ul = getSublist(node) ;
	
	if (node.className == "expanded")
		node.className = "collapsed" ;
	else
		node.className = "expanded" ;
		
	for(var i=0 ; i<ul.childNodes.length ; i++) {
		//alert(ul.childNodes[i].innerHTML)
		collapseNode(ul.childNodes[i]) ;
	}
}
 
function hideContents() {
 	var contents = document.getElementById("contents") ;
	contents.style.display = "none" ;
}

 
 // this function determines whether the event is the equivalent of the microsoft // mouseleave or mouseenter events. 
function isMouseLeaveOrEnter(e, handler) { 

	if (e.type != 'mouseout' && e.type != 'mouseover') 
		return false; 
		
	var reltg = e.relatedTarget ? e.relatedTarget : e.type == 'mouseout' ? e.toElement : e.fromElement; 
	while (reltg && reltg != handler) 
		reltg = reltg.parentNode; 
		
	return (reltg != handler); 
}

function getSublist(node) {
	
	for (var x=0 ; x<node.childNodes.length ; x++){
		
		if (node.childNodes[x].tagName == "UL")
			return node.childNodes[x] ;
	}
	
	return null ;
}