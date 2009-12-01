//used to stored the current items in berrybasket, including collection name, document id and dodument title
//in the format of collection:id:[title], it is updated when the page is loaded (reloaded) or the user adds
// new items in the berry basket (see ygDDplayer.js). It is used to show the contents of the berry basket.

var berryList =  new Array();
var show = false;

// the default width and height of the berry basket
var oldHeight=90;
var oldWidth=140;
var oldBg;

var dd = new ygDDOnTop('berrybasket');
dd.setHandleElId('baskethandle');


var checkout = function(){

	if ( document.getElementById('berrybasket') == null ) return;
//alert( arguments.callee );
	var request_type = "GET";
	var url = "?a=pr&rt=r&ro=1&s=ItemNum&o=XML&c=";

	var responseSuccess = function(o){
//alert( arguments.callee );
		var response = o.responseXML;
		var size = response.getElementsByTagName('size').item(0).getAttribute('value');

		var items = response.getElementsByTagName('item');

		for (var i=0;i < items.length ; i++ ){

			// remove berries images from if the berry basket has already contains this item
			var itemID = items[i].getAttribute('collection')+":"+items[i].getAttribute('name');
			var item = YAHOO.util.Dom.get(itemID);
			if (item != null){
				var parent = item.parentNode;
				parent.removeChild(item);
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
			}
			berryList[berryList.length] = items[i];
		}

		//add berryimage into the berry basket
		updateBerryImages();

		// set berries images visible
		// they are set to be invisible to prevent flickering when first loaded (see berry.css)

		var berries = $('img');
		var berrybasket = new YAHOO.util.DDTarget('berrybasket','basket');

		for (var j=0; j < berries.length; j++){
			var berry = berries[j]; 
			var parent = berry.parentNode;
			if (parent !=null && parent.id == "documentberries"){
				var root = YAHOO.util.Dom.get(berry.id+":root");
				var section = YAHOO.util.Dom.get(berry.id+":section");
				if(root!=null && root.id.indexOf(berry.id) !=-1){
					root.style.visibility ="visible" ;
				}

				if(section!=null && section.id.indexOf(berry.id) !=-1){
					section.style.visibility ="visible" ;
				}
			}

			if (berry.className && berry.className == 'pick'){
				berry.style.visibility = "visible";
				new ygDDPlayer(berry.id,'basket',berryList); 
			}
		}
		//updateBerryCount();
		correctBerryExpandCollapseButtons();
	}

	var responseFailure = function(o){
//alert( arguments.callee );
	}

	var callback = {
		success: responseSuccess,
		failure: responseFailure
	}

	YAHOO.util.Connect.asyncRequest(request_type , url , callback);
}

function updateBerryImages(){
//alert( arguments.callee );
	var berries = YAHOO.util.Dom.get('berries');
	if ( berries == null ) return;
	for (var i =0;i < berryList.length && i<12 ; i++ ){
		var img = document.createElement('img');
		img.src = 'interfaces/oran/images/berry.png';
		berries.appendChild(img);
	}

	//show the help message if there are no berries
	if ( berryList.length == 0 ) {
		var berryHelpMsg = document.createElement('span');
		berries.appendChild(berryHelpMsg);
		berryHelpMsg.appendChild(document.createTextNode('Drag & drop berries here to add documents to your Berry Basket'));
		berryHelpMsg.setAttribute('id','berryHelpMsg');
	}

	//show the expand buttons
	correctBerryExpandCollapseButtons();

}

function correctBerryExpandCollapseButtons() {
	var bbecl = YAHOO.util.Dom.get('berryBasketExpandCollapseLinks');
	if ( bbecl == null ) return;
	if ( berryList.length > 0 ) {
		YAHOO.util.Dom.get('berryBasketExpandCollapseLinks').style.display='';
	} else {
		YAHOO.util.Dom.get('berryBasketExpandCollapseLinks').style.display='none';
	}
}

function updateBerryCount() {
	var berries = YAHOO.util.Dom.get('berries');
	var berryCountElement = YAHOO.util.Dom.get('berryBasketCount');
	if ( berries == null || berryCountElement == null ) return;
	if ( berryList.length == 0 ) {
		berryCountElement.innerHTML = "empty";
	} else {
		berryCountElement.innerHTML = berryList.length;
	}
}

function basketUpdate(e) {
//alert( arguments.callee );
	var target = e.target;
	if ((target.id.toLowerCase() == 'berrybasket' || target.id.toLowerCase() == 'berries') && !show){
		showBasket();
		show = true;
	}else{
		if (target.id.toLowerCase() == 'hideview' && show){
			hideBasket();
			show = false;
		}
	}

	if (target.className == 'pick'){
		addBerry(target);
	}
}

function showBasket() {
//alert( arguments.callee );
	var berryBasket = YAHOO.util.Dom.get('berrybasket');
	var basketHandle = YAHOO.util.Dom.get('baskethandle');
	var berries = YAHOO.util.Dom.get('berries');
	var div = document.createElement('div');
	var list = document.createElement('ol');
	var width = 500;
	var height = 40;
	var i=0;

	list.id = 'doclist';

	//remove berryImages in the berry basket
	while (berries.hasChildNodes()) {
		berries.removeChild(berries.firstChild);
	}
	while (basketHandle.hasChildNodes()) {
		basketHandle.removeChild(basketHandle.firstChild);
	}

	//put the berries in
	for (i in berryList){
		var berryItem = berryList[i];
		var berryElement = document.createElement('li');
		var title = berryItem.getAttribute('title');
		var root_title = berryItem.getAttribute('root_title');
		var id = berryItem.getAttribute('collection')+":"+berryItem.getAttribute('name');

		if (root_title != ""){
			root_title +=":";
		}

		title = root_title+title;
		if (title.length > 50){
			title = title.substring(0,20)+" ... "+title.substr(title.length-35,35);
		}

		berryElement.appendChild(document.createTextNode(title));
		berryElement.setAttribute("class","berryitem");
		list.appendChild(berryElement);
		height +=18;
	}

	oldHeight = berryBasket.style.height;
	oldWidth = berryBasket.style.width;
	oldBg = berryBasket.style.background;
	berryBasket.style.height = height;
	berryBasket.style.width = width;
	berryBasket.style.background ='url("interfaces/default/images/kete2.png") 0 0 repeat';
	berryBasket.style.cursor = "default";
	berryBasket.className = "show";
	div.appendChild(list);
	berries.appendChild(div);
	berries.style.height = height - 40;

	//put the full view link in
	var fullView = document.createElement('a');
	berries.appendChild(fullView);
	fullView.appendChild(document.createTextNode('Full View »'));
	fullView.setAttribute("href","?a=g&sa=berry&c=&s=DisplayList&rt=r");
	fullView.setAttribute("id","berryFullViewLink");


	//toggle expand/collapse links
	var expandLink = YAHOO.util.Dom.get('berryBasketExpandLink');
	var collapseLink = YAHOO.util.Dom.get('berryBasketCollapseLink');
	if ( expandLink != null && collapseLink != null ) {
		expandLink.style.display = 'none';
		collapseLink.style.display = '';
	}

}

function hideBasket() {
//alert( arguments.callee );
	var i = 0;
	var berryBasket = YAHOO.util.Dom.get('berrybasket');
	var basketHandle = YAHOO.util.Dom.get('baskethandle');
	var berries = YAHOO.util.Dom.get('berries');
	berryBasket.style.height = oldHeight;
	berryBasket.style.width = oldWidth;
	berryBasket.style.background = oldBg;
	berryBasket.style.cursor = "pointer";
	berryBasket.className = "hide";

	while (berries.hasChildNodes()) {
		berries.removeChild(berries.firstChild);
	}

	while (basketHandle.hasChildNodes()) {
		basketHandle.removeChild(basketHandle.firstChild);
	}

	updateBerryImages();
	//updateBerryCount();

	//toggle expand/collapse links
	var expandLink = YAHOO.util.Dom.get('berryBasketExpandLink');
	var collapseLink = YAHOO.util.Dom.get('berryBasketCollapseLink');
	if ( expandLink != null && collapseLink != null ) {
		expandLink.style.display = '';
		collapseLink.style.display = 'none';
	}
	
}


function addBerry(el){
//alert( arguments.callee );
	var addurl = "?a=pr&rt=r&ro=1&s=AddItem&c=&s1.id=2&o=XML&s1.item=" + el.id;

	var addSuccess = function(o){
alert( arguments.callee );
		var result = o.responseXML;
		var items = result.getElementsByTagName('item');

		if (items.length < 0){

			var item = items[0];
			var berrybasket = YAHOO.util.Dom.get('berrybasket');
			var berries = YAHOO.util.Dom.get('berries');
			berryList[berryList.length]= item;
			var parent =el.parentNode;
			if (parent == null) return;
			parent.removeChild(el);
			var itemID = item.getAttribute('collection')+":"+item.getAttribute('name');
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

			if (!YAHOO.util.Dom.get('hideview') && berryList.length < 13){

				while (berries.hasChildNodes()) {
					berries.removeChild(berries.firstChild);
				}

				for (var i =0; i < berryList.length; i++ ){
					var img = document.createElement('img');
					img.src = 'interfaces/oran/images/berry.png';
					berries.appendChild(img);
				}

			} else {

				if (YAHOO.util.Dom.get('hideview')){
					var berryBasket = YAHOO.util.Dom.get('berrybasket');
					var berries = YAHOO.util.Dom.get('berries');
					var doclist = YAHOO.util.Dom.get('doclist');
					var tid = el.id;
					var berryItem;
					var berryElement = document.createElement('li');

					for (var i in berryList){
						berryItem = berryList[i];
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
							var heightStr = berryBasket.style.height+"";
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

	var addFailure = function(o){
//alert( arguments.callee );
	}

	var addcallback = {
		success:addSuccess,
		failure:addFailure
	}

	YAHOO.util.Connect.asyncRequest(request_type , addurl , addcallback);
}

YAHOO.util.Event.addListener(window, 'load', checkout);
//YAHOO.util.Event.addListener(window, 'click', basketUpdate);



