//used to stored the current items in document basket, including collection name, document id and dodument title
//in the format of collection:id:[title], it is updated when the page is loaded (reloaded) or the user adds
// new items in the berry basket (see documentBasketDDPlayer.js). It is used to show the contents of the berry basket.

var show = false;
var docList =  new Array();

var oldHeight=120;
var oldWidth=140;
var oldBg;

var dd = new ygDDOnTop('documentbasket');
dd.setHandleElId('documenthandle');

var dmcheckout = function(){

	if ( document.getElementById('documentbasket') == null ) return;
	
	var request_type = "GET";
	var url = "?a=pr&rt=r&ro=1&s=GetDocuments&o=XML&hhf=[{\"name\":\"Cache-Control\", \"value\":\"no-cache\"}]&c=";

	var responseSuccess = function(o){
		//alert(o.responseText);
		var response = o.responseXML;
		var size = response.getElementsByTagName('size').item(0).getAttribute('value');

		var items = response.getElementsByTagName('item');

		docList = new Array();
		for (var i=0;i < items.length ; i++ )
		{
			// remove document images from the document basket if it already contains this item
			var itemID = "documentBasketBook" + items[i].getAttribute('collection') + ":" + items[i].getAttribute('name');
			var item = YAHOO.util.Dom.get(itemID);
			if (item != null){
				var parent = item.parentNode;
				parent.removeChild(item);
			}
			docList[docList.length] = items[i];
		}

		//add document image into the document basket
		updateDocumentImages();

		// set document images visible
		// they are set to be invisible to prevent flickering when first loaded (see documentbasket.css)

		var docs = document.getElementsByTagName('img');
		new YAHOO.util.DDTarget('documentbasket','documentbox');

		for (var j=0; j < docs.length; j++){
			var doc = docs[j]; 
			var parent = doc.parentNode;

			if (doc.id.indexOf("documentBasketBook") != -1){
				doc.style.visibility = "visible";
				new documentBasketDDPlayer(doc.id,'documentbox',docList); 
			}
		}
		//updateDocumentCount();
		correctDocumentExpandCollapseButtons();
	}

	var responseFailure = function(o){
		alert("CHECKOUT FAILED");
	}

	var callback = {
		success: responseSuccess,
		failure: responseFailure
	}

	//var date = new Date();
	//url += "&rand=" + date.getTime();
	YAHOO.util.Connect.asyncRequest(request_type , url , callback);
}

function updateDocumentImages(){

	var docs = YAHOO.util.Dom.get('documentpages');
	if ( docs == null ) return;
	
	while(docs.hasChildNodes())
	{
		docs.removeChild(docs.firstChild);
	}
	
	for (var i = 0; i < docList.length && i < 12; i++ ){
		var img = document.createElement('img');
		img.src = gs.imageURLs.pageIcon;
		docs.appendChild(img);
	}

	//show the help message if there are no docs
	if ( docList.length == 0 ) {
		var docHelpMsg = document.createElement('span');
		docs.appendChild(docHelpMsg);
		docHelpMsg.appendChild(document.createTextNode('Drop your document/pages here'));
		docHelpMsg.setAttribute('id','docHelpMsg');
	}

	//show the expand buttons
	correctDocumentExpandCollapseButtons();

}

function correctDocumentExpandCollapseButtons() {
	var dbecl = YAHOO.util.Dom.get('documentBasketExpandCollapseLinks');
	if ( dbecl == null ) return;
	if ( docList.length > 0 ) {
		YAHOO.util.Dom.get('documentBasketExpandCollapseLinks').style.display='';
	} else {
		YAHOO.util.Dom.get('documentBasketExpandCollapseLinks').style.display='none';
	}
}

function updateDocumentCount() {
	var docs = YAHOO.util.Dom.get('documentpages');
	var documentCount = YAHOO.util.Dom.get('documentCount');
	if ( docs == null || documentCount == null ) return;
	if ( docList.length == 0 ) {
		documentCount.innerHTML = "empty";
	} else {
		documentCount.innerHTML = docList.length;
	}
}

function documentBoxUpdate(e) {
//alert( arguments.callee );
	var target = e.target;
	if ((target.id.toLowerCase() == 'documentbasket' || target.id.toLowerCase() == 'documentpages') && !show){
		showDocumentBox();
		show = true;
	}else{
		if (target.id.toLowerCase() == 'hideview' && show){
			hideDocumentBox();
			show = false;
		}
	}

	if (target.className == 'pick'){
		addDocument(target);
	}
}

function showDocumentBox() {
//alert( arguments.callee );
	var documentBasket= YAHOO.util.Dom.get('documentbasket');
	var documentHandle = YAHOO.util.Dom.get('documenthandle');
	var documentPages = YAHOO.util.Dom.get('documentpages');
	var width = 500;
	var height = 40;
	var i=0;

	//remove berryImages in the berry basket
	while (documentPages.hasChildNodes()) {
		documentPages.removeChild(documentPages.firstChild);
	}
	while (documentHandle.hasChildNodes()) {
		documentHandle.removeChild(documentHandle.firstChild);
	}
	
	var div = document.createElement('div');
	documentPages.appendChild(div);
	var list = document.createElement('ol');
	div.appendChild(list);
	
	list.id = 'doclist';

	//put the document pages in
	for (i; i < docList.length; i++){
		var documentItem = docList[i];
		var documentElement = document.createElement('li');
		list.appendChild(documentElement);
		var title = documentItem.getAttribute('title');
		var root_title = documentItem.getAttribute('root_title');
		//var id = berryItem.getAttribute('collection')+":"+berryItem.getAttribute('name');

		if (root_title != ""){
			root_title +=":";
		}

		title = root_title+title;
		if (title.length > 50){
			title = title.substring(0,20)+" ... "+title.substr(title.length-35,35);
		}
		
		documentElement.setAttribute("class","documentitem");
		documentElement.setAttribute("title",title);
		documentElement.innerHTML = title;
		height +=40;
	}

	oldHeight = documentBasket.style.height;
	oldWidth = documentBasket.style.width;
	oldBg = documentBasket.style.background;

	documentBasket.style.background ='url("interfaces/default/images/kete2.png") 0 0 repeat';
	documentBasket.style.cursor = "default";
	documentBasket.className = "show";

	//put the full view link in
	var fullView = document.createElement('a');
	documentPages.appendChild(fullView);
	
	//Find the collection in the cgi parameters
	var url = window.location.href;
	var colstart = url.indexOf("&c=");
	var collectionName = "";
	if (colstart != -1)
	{
		var colend = url.indexOf("&", (colstart + 1));
		if (colend == -1)
		{
			colend = url.length - 1;
		}
		collectionName = url.substring(colstart + 3, colend);
	}
	
	fullView.appendChild(document.createTextNode('Full View »'));
	fullView.setAttribute("href","?a=g&sa=documentbasket&c=&s=DisplayDocumentList&rt=r&p.c=" + collectionName);
	fullView.setAttribute("id","documentpagesFullViewLink");

	//toggle expand/collapse links
	var expandLink = YAHOO.util.Dom.get('documentBasketExpandLink');
	var collapseLink = YAHOO.util.Dom.get('documentBasketCollapseLink');
	if ( expandLink != null && collapseLink != null ) {
		expandLink.style.display = 'none';
		collapseLink.style.display = '';
	}

}

function hideDocumentBox() {
//alert( arguments.callee );
	var i = 0;
	var documentBasket = YAHOO.util.Dom.get('documentbasket');
	var documentHandle = YAHOO.util.Dom.get('documenthandle');
	var documentPages = YAHOO.util.Dom.get('documentpages');
	documentBasket.style.height = oldHeight;
	documentBasket.style.width = oldWidth;
	documentBasket.style.background = oldBg;
	documentBasket.style.cursor = "pointer";
	documentBasket.className = "hide";

	while (documentPages.hasChildNodes()) {
		documentPages.removeChild(documentPages.firstChild);
	}

	while (documentHandle.hasChildNodes()) {
		documentHandle.removeChild(documentHandle.firstChild);
	}

	updateDocumentImages();
	//updateDocumentCount();

	//toggle expand/collapse links
	var expandLink = YAHOO.util.Dom.get('documentBasketExpandLink');
	var collapseLink = YAHOO.util.Dom.get('documentBasketCollapseLink');
	if ( expandLink != null && collapseLink != null ) {
		expandLink.style.display = '';
		collapseLink.style.display = 'none';
	}
	
}


function addDocument(el){
	var addurl = "?a=pr&rt=r&ro=1&s=AddDocument&c=&s1.id=2&o=XML&hhf=[{\"name\":\"Cache-Control\", \"value\":\"no-cache\"}]&s1.item=" + el.id;

	var addSuccess = function(o){
		var result = o.responseXML;
		var items = result.getElementsByTagName('item');

		if (items.length < 0){

			var item = items[0];
			var documentbasket = YAHOO.util.Dom.get('documentbasket');
			var documentPages = YAHOO.util.Dom.get('documentpages');
			docList[docList.length]= item;
			var parent =el.parentNode;
			if (parent == null) return;
			parent.removeChild(el);
			var itemID = item.getAttribute('collection')+":"+item.getAttribute('name');
			//added
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

			if (!YAHOO.util.Dom.get('hideview') && docList.length < 13){

				while (documentpages.hasChildNodes()) {
					documentpages.removeChild(documentpages.firstChild);
				}

				for (var i =0; i < docList.length; i++ ){
					var img = document.createElement('img');
					img.src = gs.imageURLs.pageIcon;
					documentpages.appendChild(img);
				}

			}  else {

				if (YAHOO.util.Dom.get('hideview')){
					var documentBasket = YAHOO.util.Dom.get('documentbasket');
					var documentPages = YAHOO.util.Dom.get('documentpages');
					var doclist = YAHOO.util.Dom.get('doclist');
					var tid = el.id;
					var documentItem;
					var documentElement = document.createElement('li');

					for (var i in docList){
						documentItem = docList[i];
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
							doclist.appendChild(documentElement);
							var heightStr = documentBasket.style.height+"";
							var height =parseInt(heightStr.substring(0,heightStr.length-2)) +18;
							documentBasket.style.height = height;
							documentPages.style.height = height;
							break;
						}

					}

				}

			}
		}
	}

	var addFailure = function(o){
	}

	var addcallback = {
		success:addSuccess,
		failure:addFailure
	}
	
	YAHOO.util.Connect.asyncRequest(request_type , addurl , addcallback);
}

YAHOO.util.Event.addListener(window, 'load', dmcheckout);
//YAHOO.util.Event.addListener(window, 'click', documentBoxUpdate);

