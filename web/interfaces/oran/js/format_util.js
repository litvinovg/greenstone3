
console.log("Loading format_util.js\n");

// Ensures that a change to a text field is remembered
function onTextChange(item, text)
{
    console.log("I have set "+item+".value to "+text);
    item.setAttribute("value",text);
}

// Ensures that a change to a select field is remembered
function onSelectChange(item)
{
    console.log("I have set "+item.value+".selected to selected");
    for (var i=0; i<item.options.length; i++)
    {
        if(item.selectedIndex == i)
            item.options[i].setAttribute("selected", "selected");
        else
            item.options[i].removeAttribute("selected");
    }
    //item.options[item.selectedIndex].selected = "selected";
    //item.setAttribute("selected","selected");
}

function getSubstring(str, first, last)
{
    var first_index = str.indexOf(first)+first.length+1;
    var last_index = str.indexOf(last, first_index);

    if(last_index == -1)
        last_index = str.length;

    var substring = str.substring(first_index, last_index);

    console.log(substring);

    return substring;
}
    
function getXSLT(classname)
{
    var myurl = document.URL;

    //var collection_name = getSubstring(myurl, "&c", "&");
    var document_id = getSubstring(myurl, "&d", "&");
    var document_type = getSubstring(myurl, "&dt", "&");
    var prev_action = getSubstring(myurl, "&p.a", "&");
    console.log("Prev action: "+ prev_action);
    var prev_service = getSubstring(myurl, "&p.s", "&");
    console.log("Prev service: "+ prev_service);

    //var post_url = "http://localhost:8383/greenstone3/dev?a=d&c=" + collection_name + "&d=" + document_id + "&dt=" + document_type + "&p.a=" + prev_action + "&p.s=" + prev_service + "&o=skinandlib";
    var post_url = host_info.pre_URL +"?a=d&c=" + host_info.collection_name + "&d=" + document_id + "&dt=" + document_type + "&o=skinandlib";

    $.post(post_url, {data: classname}, function(data) {
            console.log("Success, we have received data");
            console.log(data);
            classname = "." + classname;
            console.log(classname); 
            var content = $(data).find(classname);
            console.log(content.innerXHTML());
            console.log(content.xml());
            console.log(content);
            $("#XSLTcode").val(content.xml());
            }, 'xml');
}

function traverse(node, formatstring)
{

    if(node.nodeName=='DIV')
    {
        console.log("Found a div" +node.nodeName+","+node.nodeType);
        formatstring = formatstring + find_class(node);
        console.log(formatstring);
    }

    var children = $(node).children();
    for(var i=0; i < children.length; i++)
        formatstring = formatstring + traverse(children[i], formatstring);

    return formatstring;
}
        

/*
$("#iframe").ready(function(){
        console.log("iframe is ready ...");
        var iframe = document.getElementById('iframe');
        var iframe_document = iframe.document;

        if(iframe.contentDocument)
        {
            iframe_document = iframe.contentDocument; // For NS6
            console.log("Chose content document");
        }
        else if(iframe.contentWindow)
        {
            iframe_document = iframe.contentWindow.document; // For IE5.5 and IE6
            console.log("Chose content window");
        }

        console.log(iframe_document.documentElement.innerHTML); //document.documentElement.outerHTML

        $(iframe_document.documentElement.innerHTML).find('a').each(function() {
            console.log("data "+$(this).data('href'));
            console.log("getAttribute "+$(this).getAttribute('href'));
            console.log("attr "+$(this).attr('href'));
            console.log("this.href "+this.href);
            var original = this.href; //$(this).attr('href');
            // check if greenstone link ie. starts with format
            //var original = $(this).data('href');
            var modified = original.toString().concat("&excerptid=gs_content");
            console.log(modified);
            this.href = modified;
            //$(this).attr('href',modified);
            //$(this).data('href', modified);
            //console.log($(this).attr('href'));
            console.log("data "+$(this).data('href'));
            console.log("getAttribute "+$(this).getAttribute('href'));
            console.log("attr "+$(this).attr('href'));
            console.log("this.href "+this.href);
            console.log("**********");

        });
}); */
/*
function loadXMLDoc()
{
    if (window.XMLHttpRequest)
    {// code for IE7+, Firefox, Chrome, Opera, Safari
        xmlhttp=new XMLHttpRequest();
    }
    else
    {// code for IE6, IE5
        xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
    }
    
    xmlhttp.onreadystatechange=function()
    {  
        console.log("state changed to " + xmlhttp.readyState);
        console.log("status is " + xmlhttp.status);
        if (xmlhttp.readyState==4 && xmlhttp.status==200)
        {
            console.log("get code");
            console.log(xmlhttp.responseText);
            document.getElementById("gs_content").innerHTML=xmlhttp.responseText;
        }
    }
    
    xmlhttp.open("GET","http://localhost:8080/greenstone3/format?a=b&rt=s&s=ClassifierBrowse&c=simpleht&cl=CL1&excerptid=gs_content",true);
    //xmlhttp.open("GET","http://www.cs.waikato.ac.nz",true);
    //xmlhttp.open("GET","http://wand.net.nz/~sjb48/index.html",true);
    xmlhttp.send();
}
*/
$(document).ready(function(){

    console.log("Document ready function\n");
	
    CURRENT_SELECT_VALUE = ""; //global - gets set by ui.draggable

    console.log(gs.xsltParams.interface_name);
    console.log(gs.collectionMetadata.httpPath);
    console.log(document.URL);
    var r = /(https?:\/\/)?([\da-z\.-]+):(\d+)\/([\da-z]+)\/([\da-z]+)\?.*&c=([\da-z\.-]+).*/;
    var s = document.URL;
    s.match(r);
    //var matches = regex.exec(document.URL);
    /* Host info needs to be globally available */
    host_info = new Object();
    console.log("Hostname: " + RegExp.$2);
    host_info.host_name = RegExp.$2;
    console.log("Port: " + RegExp.$3);
    host_info.port = RegExp.$3;
    console.log("Library: " + RegExp.$4);
    host_info.library = RegExp.$4;
    console.log("Servlet: " + RegExp.$5);
    host_info.servlet = RegExp.$5;
    console.log("Collection: " + RegExp.$6);
    host_info.collection_name = RegExp.$6;

    host_info.pre_URL = "http://" + host_info.host_name + ":" + host_info.port + "/" + host_info.library + "/" + host_info.servlet;
    console.log("Pre URL: " + host_info.pre_URL);

    /*
	var collection = "";
	
	var regex = new RegExp("[?&]c=");
	var matches = regex.exec(document.URL);
	if(matches != null)
	{
		var startIndex = matches.index;
		var endIndex = document.URL.indexOf("&", startIndex + 1);
		
		if(endIndex == -1)
		{
			endIndex = document.URL.length;
		}
		
		collection = document.URL.substring(startIndex, endIndex);
	}
    */

    /*
    var iframe = document.getElementById('iframe');
    var iframe_document = iframe.document;
   
    if(iframe.contentDocument)
    {
        iframe_document = iframe.contentDocument; // For NS6
        console.log("Chose content document");
    }
    else if(iframe.contentWindow)
    {
        iframe_document = iframe.contentWindow.document; // For IE5.5 and IE6
        console.log("Chose content window");
    }

    // Edit the hrefs in preview_html - search for <a href="format?a=b&rt=r&s=ClassifierBrowse&c=simpleht&cl=CL1.2">
    var start_index = 0;
    var end_index = 0;
    while(start_index != -1)
    {
        start_index = preview_html.indexOf("href=\"format", start_index);
        console.log("start index = " + start_index);
        if(start_index != -1)
        {
            end_index = preview_html.indexOf("\">", start_index);
            console.log("end_index = " + end_index);
            a = preview_html.substring(0,end_index);
            b = preview_html.substring(end_index);
            preview_html = a.concat("&excerptid=results", b);
            console.log(preview_html);
            start_index = end_index + "&excerptid=results\">".length;
        }
    }

    // Split the html code in to three parts
    var first_index = preview_html.indexOf("<ul id=\"results\"");
    console.log("First index is "+first_index);
    var second_index = preview_html.indexOf("</ul>", first_index) + "</ul>".length;
    console.log("Second index is "+second_index);

    var first_half = preview_html.substring(0, first_index);
    var iframe_code = preview_html.substring(first_index, second_index);
    var second_half = preview_html.substring(second_index);

    //$('#my_categories').innerHTML = first_half.concat(second_half);
    document.getElementById("my_categories").innerHTML = first_half.concat(second_half);

    // Put the content in the iframe
    if(initialised_iframe == "false")
    {
        console.log("Initialised iframe with preview html");
        console.log(preview_html);
        iframe_document.open();
        iframe_document.writeln(iframe_code); //.concat("&excerptid=gs_content"));
        iframe_document.close(); 
        initialised_iframe = "true";
    }
    */

    /*    
    $(iframe_document.documentElement.innerHTML).find('a').each(function() {
            console.log("data "+$(this).data('href'));
            console.log("getAttribute "+this.getAttribute('href'));
            console.log("attr "+$(this).attr('href'));
            console.log("this.href "+this.href);
            var original = this.href; //$(this).attr('href');
            // check if greenstone link ie. starts with format
            //var original = $(this).data('href');
            var modified = original.toString().concat("&excerptid=gs_content");
            console.log("*                      *");
            console.log(modified);
            console.log("*                      *");
            //this.href = modified;
            //$(this).attr('href',modified);
            //$(this).data('href', modified);
            $(this).attr({ 'href': modified });
            //console.log($(this).attr('href'));
            console.log("data "+$(this).data('href'));
            console.log("getAttribute "+this.getAttribute('href'));
            console.log("attr "+$(this).attr('href'));
            console.log("this.href "+this.href);
            console.log("***********************");
    });
    */

    /******************************************/
    /*              DRAGGABLES                */
    /******************************************/

    $(".draggable_gsf_template").draggable({
            cursor: 'crosshair',
            connectToSortable: '#formatStatement',
            helper: 'clone',
            revert: 'invalid'
    });

    $(".draggable_table").draggable({
            cursor: 'crosshair',
            connectToSortable: '.gsf_template',
            helper: 'clone',
            revert: 'invalid'
    });

    $(".draggable_tr").draggable({
            cursor: 'crosshair',
            connectToSortable: '.gsf_table',
            helper: 'clone',
            revert: 'invalid'
    })

    $(".draggable_td").draggable({
            cursor: 'crosshair',
            //connectToSortable: '.td',
            helper: 'clone',
            revert: 'invalid'
    })

    $(".draggable_gsf_text").draggable({
            cursor: 'crosshair',
            connectToSortable: '.td-div, .gsf_when, .gsf_otherwise, .gsf_link, .gsf_choose_metadata, .gsf_default',
            helper: 'clone',
            revert: 'invalid'
    });
    
    $(".draggable_gsf_choose_metadata").draggable({
            cursor: 'crosshair',
            connectToSortable: '.td-div, .gsf_link, .gsf_when, .gsf_otherwise',
            helper: 'clone',
            revert: 'invalid'
    });

    //$(".element_type_gsf_metadata").draggable({
    $(".draggable_gsf_metadata").draggable({
            cursor: 'crosshair',
            connectToSortable: '.gsf_choose_metadata, .gsf_when, .gsf_otherwise, .gsf_link, .td-div',
            helper: 'clone',
            revert: 'invalid'
            
    });

    $(".draggable_gsf_link").draggable({
            cursor: 'crosshair',
            connectToSortable: '.td-div, .gsf_when, .gsf_otherwise, .gsf_link',
            helper: 'clone',
            revert: 'invalid'
    });

    // switch, when, otherwise, icon
    $(".draggable_gsf_switch").draggable({
            cursor: 'crosshair',
            connectToSortable: '.td-div, .gsf_link',
            helper: 'clone',
            revert: 'invalid'
    });

    $(".draggable_gsf_when").draggable({
            cursor: 'crosshair',
            connectToSortable: '.gsf_switch',
            helper: 'clone',
            revert: 'invalid'
    });

    $(".draggable_gsf_otherwise").draggable({
            cursor: 'crosshair',
            connectToSortable: '.gsf_switch',
            helper: 'clone',
            revert: 'invalid'
    });

    $(".draggable_gsf_icon").draggable({
            cursor: 'crosshair',
            connectToSortable: '.td-div, .gsf_link, .gsf_choose, .gsf_when, .gsf_otherwise',
            helper: 'clone',
            revert: 'invalid'
    });

 
    /******************************************/
    /*               SORTABLES                */
    /******************************************/

    bind_td_sortable();
    bind_all_sortables();

    $('#formatStatement').sortable({
            cursor: 'pointer',
            tolerance: 'pointer',
            items: '.gsf_template',
            placeholder:'placeholder',
            //'nested':'div'
            stop: function(event, ui) {
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('css_gsf_template')) { replace_with(ui.item,gsf_template_element); }
            }
    });

    $(".draggable_gsf_metadata").each(function(){
        this.onselectstart = function() { return false; };
    });
 
    $('div.gsf_template').children(".block,.table").slideUp(300); 
}); 

/*******************************************************************************/

function bind_all_sortables()
{
    console.log('function bind_all_sortables()');
    bind_template_sortable();
    bind_choose_metadata_sortable();
    bind_link_sortable();
    bind_switch_sortable();
    bind_when_sortable();
    bind_otherwise_sortable();

    bind_tables();

    bind_block_mouseover();
    bind_minmax_remove();    
}

function bind_tables()
{
    console.log('function bind_tables()');

    $('#sectionHeader').click(function () {
         console.log('section Header click *');
         return true;
    });
    
    $('#sectionContainer').click(function () {
         console.log('section Container click *');
         return true;
    });

    $(".td-div").resizable({
                alsoResize: 'parent',
                handles: 'w,e',
                stop: function(event, ui) {
                        console.log('Resize table on stop');
                        resize_tables($(this));
                }, });

    $(".droppable").droppable({
            accept: '.element_type_td',
            tolerance: 'pointer', 
            activate: function(event, ui) { $(this).addClass("droppable_hl");}, 
            deactivate: function(event, ui) { $(this).removeClass("droppable_hl"); },
            drop: function(event, ui) {
                 var neverempty = document.createElement("div");
                 neverempty.setAttribute("class","neverempty block");
                 neverempty.setAttribute("style","height:50px");
                 neverempty.setAttribute("style","display:block");
                 var text = document.createTextNode('NEVER EMPTY');
                 neverempty.appendChild(text);
                 var td = document.createElement("td");
                 var div = document.createElement("div"); 
                 div.setAttribute("title","td-div");
                 div.setAttribute("class","td-div block");
                 div.setAttribute("style","margin-left:0px");
                 div.setAttribute("width","25px");
                 td.appendChild(div);
                 div.appendChild(neverempty);
                 var sep = document.createElement("td");
                 sep.setAttribute("class","droppable");
                 sep.setAttribute("width","10px"); 
                 $(this).after(sep);                
                 $(this).after(td);                
                 bind_tables();
                 resize_tables($(this));
                 bind_td_sortable();
                 bind_block_mouseover();
            }
        });

}

function replace_with(item, me)
{
    // Search me for select
    console.log("ME is "+me);
    if(me.search("select") != -1)
    {
        console.log("ME is "+me);
        // If select exists, then find CURRENT_SELECT_VALUE
        if(me.search(CURRENT_SELECT_VALUE) != -1)
        {
            var index = me.search("\""+CURRENT_SELECT_VALUE+"\"");
            if(index == -1)
                console.log("Did not find " + CURRENT_SELECT_VALUE);
            else
            {
                console.log("Found " + CURRENT_SELECT_VALUE + " at index " + index);    
                index = index + CURRENT_SELECT_VALUE.length + 2;
                console.log("Attempt inserting select at new index "+index);
                a = me.substring(0,index);
                b = me.substring(index);
                me = a.concat(" selected",b);
            }
        }
    }
    
    console.log("Replacing "+item+" with "+me);

    item.replaceWith(me); //'<div class="element element-txt">This text box has been added!</div>');

    resize_tables(item);

    bind_all_sortables();
}

function resize_tables(item)
{
    var max_height = 0;
    (item.parents('.table')).each(function(index) {
        $(this).children().children().children().each(function() {
            var sum_one = 0;
            var sum_two = 0;
            $(this).children('.block').each(function() { sum_one = sum_one + $(this).height();  
                $(this).children('.block').each(function() { sum_two = sum_two + $(this).height(); } );
                console.log("My height is " + $(this).height() + ", sum height 2 is " + sum_two);
            });
            console.log("My height is " + $(this).height() + ", sum height 1 is " + sum_one);
            if (sum_two > max_height)
                max_height = sum_two;
        });
    });
    equalHeights(item,max_height);
}

function bind_template_sortable()
{
    //console.log('function bind_template_sortable()');
    $('.gsf_template').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.table', //.gsf_choose_metadata, .gsf_metadata',
            'placeholder':'placeholder',
            //'nested':'.gsf:metadata'
            receive: function(event, ui) { alert("template attempted to receive"); },
            stop: function(event, ui) {
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_table')) { replace_with(ui.item, "<table class=\"table\" border=\"2\"></table>"); }
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_choose_metadata')) { replace_with(ui.item, gsf_choose_metadata_element); }
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_metadata')) { replace_with(ui.item, gsf_metadata_element); }
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_link')) { replace_with(ui.item, gsf_link_element); }
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_switch')) { replace_with(ui.item, gsf_switch_element); }
            }
    });

    $('.gsf_template').click(function () {
         console.log('gsf_template class click');
         return false;
    });

}

function bind_td_sortable()
{

    $('tr').sortable({
            'cursor':'pointer',
            'tolerance': 'intersect',
            'items':'.column',
            'placeholder':'placeholder_td',
            'connectWith':'column',
            receive: function(event, ui) { alert("template attempted to receive");} 
    });

    $('.td-div').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.gsf_metadata, .gsf_choose_metadata, .gsf_link, .gsf_switch',
            'placeholder':'placeholder',
            receive: function(event, ui) { alert("td-div attempted to receive"); },
            stop: function(event, ui) {
                // gsf metadata
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_metadata')) { replace_with(ui.item, gsf_metadata_element); }
                // gsf choose metadata
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_choose_metadata')) { replace_with(ui.item, gsf_choose_metadata_element); }
                // gsf link
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_link')) { replace_with(ui.item, gsf_link_element); }
                // gsf switch
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_switch')) { replace_with(ui.item, gsf_switch_element); }
                
            }

    });

    $('.td-div').click(function () {    
         //console.log('td class click');
         return true;
    });



}


function bind_choose_metadata_sortable()
{
    //console.log('function bind_choose_metadata_sortable()');
    $('.gsf_choose_metadata').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.gsf_metadata, .gsf_text, .gsf_default',
            'placeholder':'placeholder',
            'connectWith':'.gsf_choose_metadata',
            //'nested':'.gsf:metadata'
            receive: function(event, ui) { alert("choose-metadata attempted to receive"); },
            stop: function(event, ui) {
                // gsf metadata
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_metadata')) { 
                    console.log(gsf_metadata_element);
                    replace_with(ui.item, gsf_metadata_element); 
                } 
                // gsf text
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_text')) { replace_with(ui.item, gsf_text_element); } 
                // gsf default
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_default')) { replace_with(ui.item, gsf_default_element); } 

                bind_all_sortables();
            }
    });
}

function bind_link_sortable()
{
    //console.log('function bind_link_sortable()');
    $('.gsf_link').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.leaf, .gsf_link, .gsf_switch, .gsf_choose',
            'placeholder':'placeholder',
            'connectWith':'.gsf_link',
            //'nested':'.gsf:metadata'
            stop: function(event, ui) {
                // gsf icon
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_icon')) { replace_with(ui.item, gsf_icon_element); }
                // gsf text
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_text')) { replace_with(ui.item, "<input type=\"text\" name=\"rawtextinput\" size=\"10\"/>"); }
                // gsf metadata
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_metadata')) { replace_with(ui.item, gsf_metadata_element); } 
                // gsf link
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_link')) { replace_with(ui.item, gsf_link_element); }
                // gsf switch
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_switch')) { replace_with(ui.item, gsf_switch_element); }
                // gsf choose
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_choose_metadata')) { replace_with(ui.item, gsf_choose_metadata_element); }
            }
    });
}

function bind_switch_sortable()
{
    //console.log('function bind_switch_sortable()');
    $('.gsf_switch').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.gsf_metadata, .gsf_when, .gsf_otherwise, .gsf_text',
            'placeholder':'placeholder',
            'connectWith':'.gsf_switch',
            //'nested':'.gsf:metadata'
            stop: function(event, ui) {
                // gsf when
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_when')) { replace_with(ui.item, gsf_when_element); }
                // gsf otherwise
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_otherwise')) { replace_with(ui.item, gsf_otherwise_element); }
                // gsf metadata
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_metadata')) { replace_with(ui.item, gsf_metadata_element); } 
                // gsf text
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_text')) { replace_with(ui.item, gsf_text_element); } 
            }
    });
}

function bind_when_sortable()
{
    //console.log('function bind_when_sortable()');
    $('.gsf_when').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.leaf, .gsf_link, .gsf_choose',
            'placeholder':'placeholder',
            //'nested':'.gsf:metadata'
            stop: function(event, ui) {
                // gsf metadata
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_metadata')) { replace_with(ui.item, gsf_metadata_element); }
                // gsf icon
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_icon')) { replace_with(ui.item, gsf_icon_element); } 
                // gsf text
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_text')) { replace_with(ui.item, gsf_text_element); } 
                // gsf link
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_link')) { replace_with(ui.item, gsf_link_element); } 
                // gsf choose
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_choose')) { replace_with(ui.item, gsf_choose_element); } 
            }
    });
}

function bind_otherwise_sortable()
{
    //console.log('function bind_otherwise_sortable()');
    $('.gsf_otherwise').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.leaf, .gsf_link, .gsf_choose',
            'placeholder':'placeholder',
            //'nested':'.gsf:metadata'
            stop: function(event, ui) {
                // gsf metadata
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_metadata')) { replace_with(ui.item, gsf_metadata_element); }
                // gsf text
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_text')) { replace_with(ui.item, "<input type=\"text\" name=\"rawtextinput\" size=\"10\"/>"); }
                // gsf icon
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_icon')) { replace_with(ui.item, gsf_icon_element); } 
                // gsf link
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_link')) { replace_with(ui.item, gsf_link_element); } 
                // gsf choose
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_choose')) { replace_with(ui.item, gsf_choose_element); } 
            }
    });
}

function bind_block_mouseover()
{
    //console.log('function bind_block_mouseover()');
    $(".block").mouseover(function()
    {
        $(this).parents().css("border", "");
        $(this).css("border", "2px solid blue");
        return false;
    }).mouseout(function(){
        $(this).css("border", "");
        return false;
    });
}

function bind_minmax_remove()
{
    console.log('function bind_minmax_remove()');
    $('a.minmax').bind('click', toggleContent);
    $('a.remove').bind('click', removeContent);
};

var removeContent = (function () {
    //console.log('var removeContent = (function ()');
        // this -> a -> td -> tr -> tbody -> table -> div
        //$(this).parent().parent().parent().parent().parent().remove();
        $(this).closest(".block").remove();
    });


var toggleContent = function(e)
{
    console.log('parent: ' + $(this).html());
    if ($(this).html() == '[+]'){ //targetContent.css('display') == 'none') {
        $(this).closest(".block").children(".table, .block").slideDown(300);
        $(this).html('[-]');
        $(this).removeClass("ui-icon-plusthick");
        $(this).addClass("ui-icon-minusthick");
    } else {
        $(this).closest(".block").children(".table, .block").slideUp(300);
        $(this).html('[+]');
        $(this).removeClass("ui-icon-minusthick");
        $(this).addClass("ui-icon-plusthick");
    }
    return false;
};

function serialize(s)
{
    serial = $.SortSerialize(s);
    alert(serial.hash);
};

function equalHeights(item, height) {
    //console.log('function equalHeights(item, height)');
    (item.parents('.table')).each(function(index) {
        $(this).children().children().children().each(function() {
            $(this).height(height);
            $(this).children().height(height);
        });
    });
};

