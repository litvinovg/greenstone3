var docList = new Array();
var urlonly = false;
var mailinfo = new Array();
mailinfo['address'] = "To: ";
mailinfo['cc'] = "CC: ";
mailinfo['bcc'] = "Bcc: ";
mailinfo['subject'] = "Subject: ";
var textwin;
var mailwin;

var options = new Array(3);
options[0] = 'fullview';
options[1] = 'textview';
options[2] = 'email';

function navigate(e){
	
	var target = this;

	if ( target.id.toLowerCase() == '' ) {
		target = target.parentNode;
	}

	if (target.id.toLowerCase() == 'fullview'){
		berryCheckoutHighlight( 'fullview' );
		showFullView();
	}

	if (target.id.toLowerCase() == 'textview'){
		berryCheckoutHighlight( 'textview' );
		showTextView();
	}

	if (target.id.toLowerCase() == 'email'){
		berryCheckoutHighlight( 'email' );
		showEmail();
	}

	if (target.id.toLowerCase() == 'sendmail'){
		sendMail();
	}

	if (target.id.toLowerCase() == 'urlcheck' && urlonly){
		var urlcheck = YAHOO.util.Dom.get('urlcheck');
		urlcheck.src = 'interfaces/default/images/check3.gif';
		var parea =YAHOO.util.Dom.get('pretextarea');
		urlonly = false;
		
		this.value='URL only view';
		
		populateUrlsAndMetadata(parea);
		return;
	}

	if (target.id.toLowerCase() == 'urlcheck' && !urlonly ){
		var urlcheck = YAHOO.util.Dom.get('urlcheck');
		urlcheck.src = 'interfaces/default/images/check4.gif';
		var parea =YAHOO.util.Dom.get('pretextarea');
		populateUrls(parea);
		urlonly = true;
		
		this.value='URL and Metadata view';
		
		return;
	}

	if (target.id.toLowerCase() == 'extextview' ){
		if (textwin != null){
			textwin.close();
		}

		textwin = window.open("","Berry basket plain text view","status=1,width=450,height=300");
		textwin.moveTo(0,0);
		var content = document.createElement('div');
		buildPreview(content);
		var body = textwin.document.getElementsByTagName('body')[0]; 
		body.appendChild(content);
		var prearea = textwin.document.getElementsByTagName('textarea')[0];
		prearea.cols = '55';
		prearea.rows = '15';
	}

	if (target.id.toLowerCase() == 'exemail' ){
		if (mailwin != null){
			mailwin.close();
		}
		mailwin = window.open("","Berry basket mail to a friend","status=1,width=450,height=350");
		mailwin.moveTo(0,0);
		var content = document.createElement('div');
		getEmailContent(content);
		var body = mailwin.document.getElementsByTagName('body')[0];
		body.appendChild(content);
		var prearea = mailwin.document.getElementsByTagName('textarea')[0];
		prearea.cols = '50';
		prearea.rows = '11';
	}
}

function pageLoad(){
	for(var j = 0; j < options.length; j++)
	{
		var ele = document.getElementById(options[j]);
		YAHOO.util.Event.addListener(ele, 'click', navigate);
	}
	
	showFullView();
}

function showFullView(){

	var content =  YAHOO.util.Dom.get('berryBasketContent');
	var fullview =  YAHOO.util.Dom.get('fullview');
	berryCheckoutPageClear();

	if (docList.length == 0){
		content.appendChild(document.createTextNode("Your berry basket is empty."));
		return;
	}

	var trashbin = document.createElement('div');
	trashbin.id ='trashbin';

	var binhandle = document.createElement('div');
	binhandle.id = 'binhandle';
	binhandle.appendChild(document.createElement('span'));
	trashbin.appendChild(binhandle);
	content.appendChild(trashbin);

	var dd = new ygDDOnTop('trashbin');
	dd.setHandleElId('binhandle');
	new YAHOO.util.DDTarget('trashbin','trash');

	var dlist = document.createElement('div');
	content.appendChild(dlist);
	var ol = document.createElement('ol');
	dlist.appendChild(ol);

	for (var i in docList){
		var doc = docList[i];
		var li = document.createElement('li');
		var a = document.createElement('a');
		var text ="";
		a.href ="?a=d&c="+doc['collection']+"&d="+doc['name'];
		a.appendChild(document.createTextNode(doc['title'])); 
		if (doc['root_title'] != ""){
			li.appendChild(document.createTextNode(doc['root_title']+": ")); 
		}
		li.appendChild(a);

		var metadata = "";
		for (var metaItem in doc && metaItem != 'title'){
			metadata = "\n"+metaItem+": "+ doc[metaItem]+"\n";
		}
		text +=metadata+"\n";
		li.appendChild(document.createTextNode(text));
		li.id = doc['collection']+":"+ doc['name'];
		li.className = 'berrydoc';
		ol.appendChild(li);
		new ygDDPlayer(li.id,'trash',docList);
	}

	/**
	var extra = document.createElement('div');
	var textview = document.createElement('span');
	textview.id = 'extextview';
	textview.className = 'extralink';
	textview.appendChild(document.createTextNode("plain text version"));
	var email = document.createElement('span');
	email.appendChild(document.createTextNode("email to a friend"));
	email.id = 'exemail';
	email.className = 'extralink';
	extra.appendChild(textview);
	extra.appendChild(email);
	content.appendChild(extra);
	**/
	//alert('x');

}

function showTextView(){

	var content = YAHOO.util.Dom.get('berryBasketContent');
	var textview = YAHOO.util.Dom.get('textview');

	berryCheckoutPageClear();
	if (docList.length == 0){
		content.appendChild(document.createTextNode("Your berry basket is empty."));
		return;
	}
	buildPreview(content);

}

function getEmailContent(content){
	var item ;
	var tr;
	var td;
	var input;

	table = document.createElement('table');
	table.setAttribute("class","mailtable");

	for (item in mailinfo){
		tr = document.createElement('tr');
		td = document.createElement('td');
		td.setAttribute("class","mailitem");
		td.appendChild(document.createTextNode(mailinfo[item]));
		tr.appendChild(td);
		td = document.createElement('td');
		input = document.createElement('input');
		input.setAttribute("id", item);
		input.setAttribute("class", "mailinput");
		input.setAttribute("type", "text");
		td.appendChild(input);
		tr.appendChild(td);
		table.appendChild(tr);
	}

	// an empty line
	tr = document.createElement('tr');
	td = document.createElement('td');
	td.appendChild(document.createElement('br'));
	tr.appendChild(td);
	table.appendChild(tr);

	content.appendChild(table);

	buildPreview(content);

	//send button
	input = document.createElement('input');
	input.setAttribute("id", 'sendmail');
	input.setAttribute("class", "sendbutton");
	input.setAttribute("type", "button");
	input.setAttribute("value", "send");
	content.appendChild(input);
}

function showEmail(){
	var content = YAHOO.util.Dom.get('berryBasketContent');
	var email = YAHOO.util.Dom.get('email');

	berryCheckoutPageClear();

	if (docList.length == 0){
		content.appendChild(document.createTextNode("Your berry basket is empty."));
		return;
	}

	var item;
	var tr;
	var td;
	var input;

	table = document.createElement('table');
	table.setAttribute("class","mailtable");

	for (item in mailinfo){
		tr = document.createElement('tr');
		td = document.createElement('td');
		td.setAttribute("class","mailitem");
		td.appendChild(document.createTextNode(mailinfo[item]));
		tr.appendChild(td);

		td = document.createElement('td');
		input = document.createElement('input');
		input.setAttribute("id", item);
		input.setAttribute("class", "mailinput");
		input.setAttribute("type", "text");
		td.appendChild(input);
		tr.appendChild(td);
		table.appendChild(tr);

	}

	// an empty line
	tr = document.createElement('tr');
	td = document.createElement('td');
	td.appendChild(document.createElement('br'));
	tr.appendChild(td);
	table.appendChild(tr);

	content.appendChild(table);

	buildPreview(content);

	//send button
	input = document.createElement('input');
	input.setAttribute("id", 'sendmail');
	input.setAttribute("class", "sendbutton");
	input.setAttribute("type", "button");
	input.setAttribute("value", "send");
	content.appendChild(input);
	
	YAHOO.util.Event.addListener(input, 'click', navigate);
}

function buildPreview(parent){

	var div = document.createElement('div');
	var cb = document.createElement('input');
	cb.setAttribute('class', 'sendbutton');
	cb.type = 'button';
	cb.id = 'urlcheck';
	if (urlonly)
	{
		cb.value='URL and Metadata view';
	}
	else
	{
		cb.value='URL only view';
	}

	YAHOO.util.Event.addListener(cb, 'click', navigate);
	
	var img = document.createElement('img');
	img.src = 'interfaces/default/images/check3.gif';
	img.id = 'urlcheck';
	div.appendChild(cb);
	//div.appendChild(img);

	var urls = document.createElement('span');
	urls.id = 'urls';
	urls.className = 'berrycheck';
	//urls.appendChild(document.createTextNode('URL only'));
	div.appendChild(urls);

	// var urlsmetadata = document.createElement('span');
	// urlsmetadata.id = 'urlsmetadata'
	// urlsmetadata.className = 'berryradio';
	// urlsmetadata.appendChild(document.createTextNode('URLs and Metadata'));
	// div.appendChild(urlsmetadata);

	parent.appendChild(div);

	var parea = document.createElement('textarea');
	parea.id = 'pretextarea';

	parent.appendChild(parea);

	if(urlonly)
	{
		populateUrls(parea);
	}
	else
	{
		populateUrlsAndMetadata(parea);
	}
}

function populateUrls(parea){

	var urls="";
	var doc_url = document.URL;
	var root_url = doc_url.substring(0,doc_url.indexOf('?'));

	for (var i in docList){
		var doc = docList[i];
		urls +=root_url+"?a=d&c="+doc["collection"]+"&d="+doc["name"]+"\n\n";
	}

	parea.value = urls;

}

function populateUrlsAndMetadata(parea){

	var doc_url = document.URL;
	var root_url = doc_url.substring(0,doc_url.indexOf('?'));
	var fulltext="";

	for (var i in docList){
		var doc = docList[i];
		var url =root_url+"?a=d&c="+doc['collection']+"&d="+doc['name']+"\n";
		for (var metaItem in doc){
			var metadata = metaItem+": "+ doc[metaItem]+"\n";
		}
		fulltext +=url+metadata+"\n";
	}

	parea.value = fulltext;

}

function sendMail(){
	var url = "?a=pr&rt=r&ro=1&s=SendMail&c=";
	var request_type = "POST";
	var postdata = "";
	var i;
	//get checked items
	for (i in mailinfo) {
		var input = YAHOO.util.Dom.get(i);
		var value = input.value;
		postdata +="&s1."+i+"="+value;
	}

	var content = YAHOO.util.Dom.get('pretextarea').value;

	content = content.replace(/&/g,'-------');
	postdata +="&s1.content="+content;

	var callback = {
		success: function(o) {
			var result = o.responseText;
			alert("Sending mail Succeeded");
		} ,
		failure: function(o) {
			alert("Sending mail falied");
		}
	}
	YAHOO.util.Connect.asyncRequest(request_type , url , callback, postdata);
}	

function berryCheckoutPageClear() {
	var bbc = document.getElementById('berryBasketContent');
	if ( bbc == null ) return;
	bbc.innerHTML = '';
}

function berryCheckoutHighlight( id ) {

	for ( var i=0; i<options.length; i++ ) {
		var option = document.getElementById( options[i] );
		if ( option != null ) {
			if ( id == options[i] ) {
				//YAHOO.util.Dom.addClass( option, 'current' );
				option.className='current';
			} else {
				//YAHOO.util.Dom.removeClass( option, 'current' );
				option.className='';
			}
		}
	}

	if ( option == null ) return;
	option.style.className = 'current';

}

YAHOO.util.Event.addListener(window,'load', pageLoad);


