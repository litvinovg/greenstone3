var Dom = YAHOO.util.Dom;
var Event = YAHOO.util.Event;
var DDM = YAHOO.util.DragDropMgr;
//var tempPlaceholderList;
//var tempPlaceholder;

//////////////////////////////////////////////////////////////////////////////
// custom drag and drop implementation
//////////////////////////////////////////////////////////////////////////////

YAHOO.example.DDList = function(id, sGroup, config) {

	YAHOO.example.DDList.superclass.constructor.call(this, id, sGroup, config);

	this.logger = this.logger || YAHOO;
	var el = this.getDragEl();
	Dom.setStyle(el, "opacity", 0.67); // The proxy is slightly transparent

	this.goingUp = false;
	this.lastY = 0;
};

YAHOO.extend(YAHOO.example.DDList, YAHOO.util.DDProxy, {

	startDrag: function(x, y) {
		this.logger.log(this.id + " startDrag");

		/*
		if(!tempPlaceholder)
		{
			tempPlaceholderList = document.createElement("UL");
			tempPlaceholderList.setAttribute("class", "emptyList");
			tempPlaceHolder = createPlaceholder(null, null, false);
			tempPlaceholderList.appendChild(tempPlaceHolder);
		}
		*/
		
		// make the proxy look like the source element
		var dragEl = this.getDragEl();
		var clickEl = this.getEl();
		
		this.sourcePrevSibling = getPrevSiblingOfType(clickEl, "li");
		this.sourceNextSibling = getNextSiblingOfType(clickEl, "li");
		this.sourceParentList = clickEl.parentList;
		
		closeAllOpenContents();

		Dom.setStyle(clickEl, "visibility", "hidden");
		dragEl.innerHTML = clickEl.innerHTML;
		
		var child = clickEl.childList;
		var contents = clickEl.textDiv;
		if(child)
		{
			this.dragElChild = child;
			Dom.setStyle(child, "display", "none");
		}
		else
		{
			this.dragElChild = null;
		}
		
		if(contents)
		{
			this.contents = contents;
		}
		else
		{
			this.contents = null;
		}

		Dom.setStyle(dragEl, "color", Dom.getStyle(clickEl, "color"));
		Dom.setStyle(dragEl, "backgroundColor", Dom.getStyle(clickEl, "backgroundColor"));
		Dom.setStyle(dragEl, "border", "2px solid gray");
	},

	endDrag: function(e) {

		var srcEl = this.getEl();
		var proxy = this.getDragEl();

		// Show the proxy element and animate it to the src element's location
		Dom.setStyle(proxy, "visibility", "");

		var a = new YAHOO.util.Motion( 
			proxy, { 
				points: { 
					to: Dom.getXY(srcEl)
				}
			}, 
			0.1,
			YAHOO.util.Easing.easeOut 
		);

		var proxyElem = proxy;
		var thisElem = this;
		var last = this.lastMoved;

		// Hide the proxy and show the source element when finished with the animation
		a.onComplete.subscribe(function() 
		{
			//Make the helper invisible
			Dom.setStyle(proxyElem.id, "visibility", "hidden");
			//Make the actual item visible again
			Dom.setStyle(srcEl.id, "visibility", "");
			
			//Show the child list if the section has one
			if(srcEl.childList)
			{
				Dom.setStyle(srcEl.childList, "display", "block");
				insertAfter(srcEl.childList, srcEl);
			}

			//Move the element's text div if it has one
			if(srcEl.textDiv)
			{
				insertAfter(srcEl.textDiv, srcEl);
			}

			var reverse = false;
			var newNodeID = null;
	
			//Make sure we have the ID of the last section moved
			if(last.nodeID)
			{
				newNodeID = last.nodeID;
				//We only want to modify the new node ID if the two sections are at the same level in the same document
				if(!srcEl.nodeID || (newNodeID.split(".").length == srcEl.nodeID.split(".").length && srcEl.nodeID.split(".")[0] == newNodeID.split(".")[0]))
				{
					if(thisElem.goingUp && (last.index > srcEl.index))
					{
						var lastDigit = parseInt(newNodeID.substring(newNodeID.lastIndexOf(".") + 1));
						if(lastDigit == 1){lastDigit = 2;} //Make sure we never get 0

						newNodeID = newNodeID.replace(/\.[^\.]*$/, "." + --lastDigit);
						reverse = true;
					}
					else if(!srcEl.nodeID || (!thisElem.goingUp && (last.index < srcEl.index)))
					{
						var lastDigit = parseInt(newNodeID.substring(newNodeID.lastIndexOf(".") + 1));
						newNodeID = newNodeID.replace(/\.[^\.]*$/, "." + ++lastDigit);
						reverse = true;
					}
				}
			}

			var operation;
			if(thisElem.goingUp)
			{
				operation = reverse ? "insertAfter" : "insertBefore";
			}
			else
			{
				operation = reverse ? "insertBefore" : "insertAfter";
			}

			//If a new section has just been dragged in
			if(srcEl.newSection)
			{
				srcEl.newSection = null;

				//Create a new draggable section to replace the one we just moved
				createDraggableNewSection(srcEl.parent);

				//Add the normal things to the new section
				var textDiv = createSectionTextDiv(null);
				srcEl.textDiv = textDiv;
				insertAfter(textDiv, srcEl);
				textDiv.parentItem = srcEl;

				createSectionMenu(srcEl);
				setMouseOverAndOutFunctions(srcEl);
				srcEl.setAttribute("class", srcEl.getAttribute("class").replace(/newSection/g, ""));
				srcEl.dbID = _idCounter++;
				srcEl.collection = last.collection;
				_allContents.push(srcEl);

				var undo = new Array();
				undo.op = "del";
				undo.srcElem = srcEl;
				undo.removeTransaction = true;
				_undoOperations.push(undo);

				saveTransaction('{"operation":"create", "subOperation":"' + operation + '", "collection":"' + last.collection + '", "oid":"' + newNodeID + '"}');
				addCollectionToBuild(last.collection);
			}
			else
			{
				var needsUndo = false;
				if(gs.functions.hasClass(last, "placeHolder"))
				{
					//Removed the dashed border that surrounds an empty section
					if(last.parentNode)
					{
						removeFromParent(last);
					}
					
					if(last.isEmptyList)
					{
						saveTransaction('{"operation":"move", "subOperation":"append", "collection":"' + srcEl.collection + '", "oid":"' + srcEl.nodeID + '", "newCollection":"' + last.collection + '", "newOID":"' + last.nodeID + '"}');
					}
					else
					{
						saveTransaction('{"operation":"move", "subOperation":"insertAfter", "collection":"' + srcEl.collection + '", "oid":"' + srcEl.nodeID + '", "newCollection":"' + last.collection + '", "newOID":"' + last.nodeID + '"}');
					}
					addCollectionToBuild(srcEl.collection);
					addCollectionToBuild(last.collection);
					needsUndo = true;
				}
				else
				{
					if(srcEl.collection != last.collection || srcEl.nodeID != newNodeID)
					{
						saveTransaction('{"operation":"move", "subOperation":"' + operation + '", "collection":"' + srcEl.collection + '", "oid":"' + srcEl.nodeID + '", "newCollection":"' + last.collection + '", "newOID":"' + newNodeID + '"}');
						addCollectionToBuild(srcEl.collection);
						addCollectionToBuild(last.collection);
						needsUndo = true;
					}
				}

				//Save the undo operation if necessary
				if(needsUndo)
				{
					var undo = new Array();
					if(thisElem.sourceNextSibling)
					{
						undo.op = "mvb";
						undo.refElem = thisElem.sourceNextSibling;
					}
					else if(thisElem.sourcePrevSibling)
					{
						undo.op = "mva";
						undo.refElem = thisElem.sourcePrevSibling;
					}
					else
					{
						undo.op = "mvi";
						undo.refElem = thisElem.sourceParentList;
					}

					undo.srcElem = srcEl;
					undo.removeTransaction = true;
					_undoOperations.push(undo);
				}
			}

			updateFromTop();
		});
		a.animate();

		if(gs.functions.hasClass(srcEl.previousSibling, "placeHolder"))
		{
			removeFromParent(srcEl.previousSibling);
			new YAHOO.example.DDList(srcEl.parentNode.previousSibling);
		}
		if(gs.functions.hasClass(srcEl, "topLevelItem"))
		{
			srcEl.setAttribute("class", srcEl.getAttribute("class").replace(/topLevelItem/g, ""));
		}
	},

	onDragDrop: function(e, id) {

		// If there is one drop interaction, the li was dropped either on the list,
		// or it was dropped on the current location of the source element.
		if (DDM.interactionInfo.drop.length === 1) {

			// The position of the cursor at the time of the drop (YAHOO.util.Point)
			var pt = DDM.interactionInfo.point; 

			// The region occupied by the source element at the time of the drop
			var region = DDM.interactionInfo.sourceRegion; 

			// Check to see if we are over the source element's location.  We will
			// append to the bottom of the list once we are sure it was a drop in
			// the negative space (the area of the list without any list items)
			if (!region.intersect(pt)) {
				var destEl = Dom.get(id);
				var destDD = DDM.getDDById(id);
				if(gs.functions.hasClass(destEl, "topLevelItem") && gs.functions.hasClass(destEl, "dragList"))
				{
					destEl.appendChild(this.getEl());
				}
				destDD.isEmpty = false;
				DDM.refreshCache();
			}

		}
	},

	onDrag: function(e) {

		// Keep track of the direction of the drag for use during onDragOver
		var y = Event.getPageY(e);

		if (y < this.lastY) {
			this.goingUp = true;
		} else if (y > this.lastY) {
			this.goingUp = false;
		}

		this.lastY = y;
	},

	onDragOver: function(e, id) {
	
		var srcEl = this.getEl();
		var destEl = Dom.get(id);
		
		// We are only concerned with list items, we ignore the dragover
		// notifications for the list.
		if(destEl.nodeName.toLowerCase() == "li") {
			if((gs.functions.hasClass(destEl, "dragItem") || gs.functions.hasClass(destEl, "placeHolder")) && !gs.functions.hasClass(destEl, "topLevelItem") && !gs.functions.hasClass(destEl, "newSection"))
			{
				var orig_p = srcEl.parentNode;
				var p = destEl.parentNode;
				
				var child = destEl.childList;
				var contents = destEl.textDiv;
					
				if (this.goingUp) {
					p.insertBefore(srcEl, destEl);
				} else {
					p.insertBefore(srcEl, destEl.nextSibling);
				}

				//This is to stop items being inserted between a section and its children
				if(child)
				{
					insertAfter(child, destEl);
				}
				if(contents)
				{
					insertAfter(contents, destEl);
				}
				
				this.lastMoved = destEl;

				DDM.refreshCache();
			}
		}
	}
});