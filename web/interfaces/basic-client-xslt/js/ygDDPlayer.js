/* Copyright (c) 2006 Yahoo! Inc. All rights reserved. */

function ygDDStack(id, sGroup) {
	if (id) {
		this.init(id, sGroup);
		this.initFrame();
	}
}

ygDDStack.prototype = new YAHOO.util.DDTarget();

ygDDPlayer.TYPE = "ygDDPlayer";

/**
 * @class a YAHOO.util.DDFramed implementation. During the drag over event, the
 * dragged element is inserted before the dragged-over element.
 *
 * @extends YAHOO.util.DDProxy
 * @constructor
 * @param {String} id the id of the linked element
 * @param {String} sGroup the group of related DragDrop objects
 */
function ygDDPlayer(id, sGroup,bl) {
    this.initPlayer(id, sGroup);
    this.berryList = bl;     
            
}

ygDDPlayer.prototype = new YAHOO.util.DDProxy();

ygDDPlayer.prototype.berryList = new Array();

ygDDPlayer.prototype.initPlayer = function(id, sGroup) {
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

    this.type = ygDDPlayer.TYPE;
    this.slot = null;
    this.startPos = YAHOO.util.Dom.getXY( this.getEl() );
   
}

ygDDPlayer.prototype.startDrag = function(x, y) {
	
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
              img.src = 'interfaces/basic/images/berry3.png';
              img.height = 15;
              img.width = 15;
             img.border = 0;              
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
};

ygDDPlayer.prototype.getTargetDomRef = function(oDD) {
	if (oDD.player) {
		return oDD.player.getEl();
	} else {
		return oDD.getEl();
	}
};

ygDDPlayer.prototype.endDrag = function(e) {
	// reset the linked element styles
	var s = this.getEl().style;
	s.opacity = 1;
	s.filter = "alpha(opacity=100)";

	this.resetTargets();
};

ygDDPlayer.prototype.resetTargets = function() {

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
var addurlPath ="?a=pr&rt=r&ro=1&s=AddItem&c=&s1.id=2&o=XML&s1.item=";
var delurlPath ="?a=pr&rt=r&ro=1&s=DeleteItem&c=&o=XML&s1.item=";
var postdata = null;

ygDDPlayer.prototype.onDragDrop = function(e, id) {
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

     var  addurl = addurlPath + el.id;	
     var addSuccess = function(o){ 
                 var text = o.responseText;
	         var result = o.responseXML;
                 var items = result.getElementsByTagName('item');
                 if (items.length > 0){
                    var item = items[0];
                    var berrybasket = YAHOO.util.Dom.get('berrybasket');
        	    var berries = YAHOO.util.Dom.get('berries');
                    player.berryList[player.berryList.length]= item;     
                
                    var itemID = item.getAttribute('collection')+":"+item.getAttribute('name');
                    var parent =el.parentNode;
                    parent.removeChild(el);
 	        	
                    if (parent !=null && parent.id == "documentberries"){
                         var root = YAHOO.util.Dom.get(itemID+":root");
                         var section = YAHOO.util.Dom.get(itemID+":section");
                         if(root!=null && root.id.indexOf(itemID) !=-1){
                             parent.removeChild(root);
                          }
     
	                  if(section!=null && section.id.indexOf(itemID) !=-1){
                             parent.removeChild(section);
        	          }                                                
                     }
                   

                    if (!YAHOO.util.Dom.get('hideview') && player.berryList.length <13){
                       
                       while (berries.hasChildNodes()) {
       	                   berries.removeChild(berries.firstChild);
	                }
                        
                     for (var i =0; i < player.berryList.length; i++ ){
                             var img = document.createElement('img');
                              img.src = 'interfaces/basic/images/berry3.png';
                              img.height = 15;
                              img.width = 15;
                              img.border = 0;              
                              berries.appendChild(img);
                       }
      	                                   
	          }
                 else{
                      
        
                    if (YAHOO.util.Dom.get('hideview')){
                      
                        var berryBasket  = YAHOO.util.Dom.get('berrybasket');
                        var berries  = YAHOO.util.Dom.get('berries');
                        var doclist = YAHOO.util.Dom.get('doclist'); 
                        var tid = el.id;                                  
                        var berryItem; 
                        var berryElement = document.createElement('li');
                         for (var i in player.berryList){
                             berryItem = player.berryList[i];
                             var id = berryItem.getAttribute('collection')+":"+berryItem.getAttribute('name');
                             if (id == tid){
                               var title = berryItem.getAttribute('title');
                               var root_title = berryItem.getAttribute('root_title');  
                                if (root_title != ""){
                                      root_title +=":";
                                     }
	    
                                title = root_title+title;	
                                if (title.length > 50){
                                   title = title.substring(0,20)+" ... "+title.substr(title.length-35,35);
                                }
                                
                               berryElement.appendChild(document.createTextNode(title));
                               berryElement.setAttribute("class","berryitem");
                               doclist.appendChild(berryElement);
                               var heightStr =  berryBasket.style.height+"";
                               var height =parseInt(heightStr.substring(0,heightStr.length-2)) +18;                             
                               berryBasket.style.height = height;
                               berries.style.height = height;
                               break;
                            }
                         }
                         }    
                 
                 }
               }
                
                       

         }

     var addFailure = function(o){ }


     var addcallback = {
        	success:addSuccess,
     	        failure:addFailure,  
                argument:player
                }
    

    var  delurl = delurlPath + el.id;	
     var delSuccess = function(o){ 
 		 var result = o.responseXML;
                    var parent =el.parentNode;
                    if (parent == null) return;                   

                    parent.removeChild(el);
                     
                    for (var i in player.berryList){
                       var berry = player.berryList[i];
                  
                       var id = berry['collection']+":"+berry['name'];
                       
                       if (id == el.id){
                             player.berryList.splice(i,1);
                                    
                             break;
                         }

                   }

                   if (!parent.hasChildNodes()){
                         var content =  YAHOO.util.Dom.get('content');
                           while (content.hasChildNodes()) {
		              content.removeChild(content.firstChild);
	                }  
                        content.appendChild(document.createTextNode('Your berry basket is empty.'));
                     }
                   var trashbin =  YAHOO.util.Dom.get('trashbin');
                   if ( trashbin !=null){
                         trashbin.style.background = 'url("interfaces/basic/images/trash-full.png") 0 0 no-repeat';
                  }
              }                                       
         

     var delFailure = function(o){ }


     var delcallback = {
        	success:delSuccess,
     	        failure:delFailure,  
                argument:player
                }
    

  if (id == 'berrybasket'){
       YAHOO.util.Connect.asyncRequest(request_type , addurl , addcallback);         
  }
  else{
        if (id == 'trashbin'){
             YAHOO.util.Connect.asyncRequest(request_type , delurl , delcallback); 
         }
   }

   this.resetTargets();  	
   this.slot = oDD;	
   this.slot.player = this;
 
};

ygDDPlayer.prototype.swap = function(el1, el2) {
    var dom = YAHOO.util.Dom;
	var pos1 = dom.getXY(el1);
	var pos2 = dom.getXY(el2);
	dom.setXY(el1, pos2);
	dom.setXY(el2, pos1);
};

ygDDPlayer.prototype.onDragOver = function(e, id) {};

ygDDPlayer.prototype.onDrag = function(e, id) {};





