/* Copyright (c) 2006 Yahoo! Inc. All rights reserved. */

function documentBasketDDStack(id, sGroup) {
	if (id) {
		this.init(id, sGroup);
		this.initFrame();
	}
}

documentBasketDDStack.prototype = new YAHOO.util.DDTarget();

documentBasketDDPlayer.TYPE = "documentBasketDDPlayer";

/**
* @class a YAHOO.util.DDFramed implementation. During the drag over event, the
* dragged element is inserted before the dragged-over element.
*
* @extends YAHOO.util.DDProxy
* @constructor
* @param {String} id the id of the linked element
* @param {String} sGroup the group of related DragDrop objects
*/
function documentBasketDDPlayer(id, sGroup, bl) {
	this.initPlayer(id, sGroup);
	this.documentList = bl;     
	
}

documentBasketDDPlayer.prototype = new YAHOO.util.DDProxy();

documentBasketDDPlayer.prototype.documentList = new Array();

documentBasketDDPlayer.prototype.initPlayer = function(id, sGroup) {
	if (!id) { return; }

	this.init(id, sGroup);
	this.initFrame();
	var s = this.getDragEl().style;
	s.borderColor = "transparent";
	// s.backgroundColor = "#cccccc";
	s.opacity = 0.76;
	s.filter = "alpha(opacity=76)";

	// specify that this is not currently a drop target
	this.isTarget = false;

	this.originalStyles = [];

	this.type = documentBasketDDPlayer.TYPE;
	this.slot = null;
	this.startPos = YAHOO.util.Dom.getXY( this.getEl() );

}

documentBasketDDPlayer.prototype.startDrag = function(x, y) {
	
	var dragEl = this.getDragEl();
	var clickEl = this.getEl();
	
	dragEl.innerHTML = clickEl.innerHTML;
	//dragEl.className = clickEl.className;
	//dragEl.style.color = clickEl.style.color;
	//dragEl.style.backgroundColor = clickEl.style.backgroundColor;
	//dragEl.style.visibility = clickEl.style.visibility;
	
	while (dragEl.hasChildNodes())	{
		dragEl.removeChild(dragEl.firstChild);
	}	
	
	var img = document.createElement('img');
	img.src = gs.imageURLs.pageIcon;
	dragEl.appendChild(img);
	
	var added = true;

	var s = clickEl.style;
	s.opacity = .1;
	s.filter = "alpha(opacity=10)";

	var targets = YAHOO.util.DDM.getRelated(this, true);
	
	for (var i=0; i<targets.length; i++) {
		
		var targetEl = this.getTargetDomRef(targets[i]);

		if (!this.originalStyles[targetEl.id]) {
			this.originalStyles[targetEl.id] = targetEl.className;
		}

		targetEl.className = "target";
	}
	correctDocumentExpandCollapseButtons();
};

documentBasketDDPlayer.prototype.getTargetDomRef = function(oDD) {
	if (oDD.player) {
		return oDD.player.getEl();
	} else {
		return oDD.getEl();
	}
};

documentBasketDDPlayer.prototype.endDrag = function(e) {
	// reset the linked element styles
	var s = this.getEl().style;
	s.opacity = 1;
	s.filter = "alpha(opacity=100)";

	this.resetTargets();
};

documentBasketDDPlayer.prototype.resetTargets = function() {

	// reset the target styles
	var targets = YAHOO.util.DDM.getRelated(this, true);
	for (var i=0; i<targets.length; i++) {
		var targetEl = this.getTargetDomRef(targets[i]);
		var oldStyle = this.originalStyles[targetEl.id];
		if (oldStyle) {
			targetEl.className = oldStyle;
		}
	}
};

var request_type = "GET";
var docAddurlPath = gs.xsltParams.library_name + "?a=pr&rt=r&ro=1&s=AddDocument&c=&s1.id=2&o=XML&hhf=[{\"name\":\"Cache-Control\", \"value\":\"no-cache\"}]&s1.item=";
var docDelurlPath = gs.xsltParams.library_name + "?a=pr&rt=r&ro=1&s=DeleteDocument&c=&o=XML&hhf=[{\"name\":\"Cache-Control\", \"value\":\"no-cache\"}]&s1.item=";
var postdata = null;

documentBasketDDPlayer.prototype.onDragDrop = function(e, id) {

	// get the drag and drop object that was targeted
	var oDD;
	var player = this;

	if ("string" == typeof id) {
		oDD = YAHOO.util.DDM.getDDById(id);
	} else {
		oDD = YAHOO.util.DDM.getBestMatch(id);
	}


	var el = this.getEl();

	// check if the slot has a player in it already
	if (oDD.player) {
		// check if the dragged player was already in a slot
		if (this.slot) {
			// check to see if the player that is already in the
			// slot can go to the slot the dragged player is in
			// YAHOO.util.DDM.isLegalTarget is a new method
			if ( YAHOO.util.DDM.isLegalTarget(oDD.player, this.slot) ) {
				YAHOO.util.DDM.moveToEl(oDD.player.getEl(), el);
				this.slot.player = oDD.player;
				oDD.player.slot = this.slot;
			} else {
				YAHOO.util.Dom.setXY(oDD.player.getEl(), oDD.player.startPos);
				this.slot.player = null;
				oDD.player.slot = null
			}
		} else {
			// the player in the slot will be moved to the dragged
			// players start position
			//oDD.player.slot = null;
			//YAHOO.util.DDM.moveToEl(oDD.player.getEl(), el);
		}
	}else {
		// Move the player into the emply slot
		// I may be moving off a slot so I need to clear the player ref
		if (this.slot) {
			this.slot.player = null;
		}
	}

	var  addurl = docAddurlPath + el.id;	
	var addSuccess = function(o){
		//alert("HERE!" + o.responseText);
		var result = o.responseXML;
		var items = result.getElementsByTagName('item');
		if (items.length > 0){
			var item = items[0];
			var documentbasket = YAHOO.util.Dom.get('documentbasket');
			var documents = YAHOO.util.Dom.get('documentpages');
			player.documentList[player.documentList.length]= item;     
			
			var itemID = item.getAttribute('collection')+":"+item.getAttribute('name');
			var parent =el.parentNode;
			parent.removeChild(el);
			
			if (parent !=null && parent.id == "documentbasket"){
				var root = YAHOO.util.Dom.get(itemID+":root");
				var section = YAHOO.util.Dom.get(itemID+":section");
				if(root!=null && root.id.indexOf(itemID) !=-1){
					parent.removeChild(root);
				}
				
				if(section!=null && section.id.indexOf(itemID) !=-1){
					parent.removeChild(section);
				}                                                
			}
			

			if (!YAHOO.util.Dom.get('hideview') && player.documentList.length <13){
				
				while (documents.hasChildNodes()) {
					documents.removeChild(documents.firstChild);
				}
				
				for (var i =0; i < player.documentList.length; i++ ){
					var img = document.createElement('img');
					img.src = gs.imageURLs.pageIcon;
					documents.appendChild(img);
				}

				correctBerryExpandCollapseButtons();
				
			}
			else{
				
				
				if (YAHOO.util.Dom.get('hideview')){
					
					var documentBasket  = YAHOO.util.Dom.get('documentbasket');
					var documents  = YAHOO.util.Dom.get('documents');
					var doclist = YAHOO.util.Dom.get('doclist'); 
					var tid = el.id;                                  
					var documentItem; 
					var documentElement = document.createElement('li');
					for (var i in player.documentList){
						documentItem = player.documentList[i];
						var id = documentItem.getAttribute('collection')+":"+documentItem.getAttribute('name');
						if (id == tid){
							var title = documentItem.getAttribute('title');
							var root_title = documentItem.getAttribute('root_title');  
							if (root_title != ""){
								root_title +=":";
							}
							
							title = root_title+title;	
							if (title.length > 50){
								title = title.substring(0,20)+" ... "+title.substr(title.length-35,35);
							}
							
							documentElement.appendChild(document.createTextNode(title));
							documentElement.setAttribute("class","documentitem");
							doclist.appendChild(berryElement);
							var heightStr =  documentBasket.style.height+"";
							var height =parseInt(heightStr.substring(0,heightStr.length-2)) +18;                             
							documentBasket.style.height = height;
							documents.style.height = height;
							break;
						}
					}
				}    
				
			}
		}
		
		//Makes sure all the documents on the page are up to date
		dmcheckout();
	}

	var addFailure = function(o){ alert("Adding document failed" + o); }


	var addcallback = {
		success:addSuccess,
		failure:addFailure,  
		argument:player
	}
	

	var  delurl = docDelurlPath + el.id;
	var delSuccess = function(o){ 
		var result = o.responseXML;
		var parent =el.parentNode;
		var ancestor = parent.parentNode;
		if (parent == null) return;                   

		ancestor.removeChild(parent);
		
		for (var i in player.berryList){
			var document = player.berryList[i];
			
			var id = document['collection']+":"+document['name'];
			
			if (id == el.id){
				player.documentList.splice(i,1);
				
				break;
			}

		}

		if (!parent.hasChildNodes()){
			var content =  YAHOO.util.Dom.get('content');
			while (content.hasChildNodes()) {
				content.removeChild(content.firstChild);
			}  
			content.appendChild(document.createTextNode('Your document basket is empty.'));
		}
		var trashbin =  YAHOO.util.Dom.get('trashbin');
		if ( trashbin !=null){
			trashbin.style.background = "url(\"" + gs.imageURLs.trashFull + "\") 0 0 no-repeat";
		}
	}                                       
	

	var delFailure = function(o){ alert("Deletion failed" + o);}


	var delcallback = {
		success:delSuccess,
		failure:delFailure,  
		argument:player
	}
	
	if (id == 'documentbasket'){
		YAHOO.util.Connect.asyncRequest(request_type , addurl , addcallback);    		
	} else {
		if (id == 'trashbin'){
			YAHOO.util.Connect.asyncRequest(request_type , delurl , delcallback); 
		}
	}

	this.resetTargets();  	
	this.slot = oDD;	
	this.slot.player = this;
};

documentBasketDDPlayer.prototype.swap = function(el1, el2) {
	var dom = YAHOO.util.Dom;
	var pos1 = dom.getXY(el1);
	var pos2 = dom.getXY(el2);
	dom.setXY(el1, pos2);
	dom.setXY(el2, pos1);
};

documentBasketDDPlayer.prototype.onDragOver = function(e, id) {};

documentBasketDDPlayer.prototype.onDrag = function(e, id) {};





