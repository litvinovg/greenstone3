
console.log("Loading gui_div.js\n");

/* DOCUMENT SPECIFIC FUNCTIONS */

function displayTOC(checkbox)
{
    if (checkbox.checked == true)
    {
        console.log("Show the TOC!");
        displaySideBar(true);
        $("#tableOfContents").css("display", "block");
    }
    else
    {
        console.log("Hide the TOC!");
        $("#tableOfContents").css("display", "none");
        if ($("#coverImage").css("display") == "none")
            displaySideBar(false);
    }

    return;
}

function displayBookCover(checkbox)
{
    if (checkbox.checked == true)
    {
        console.log("Show the book cover!");
        displaySideBar(true);
        $("#coverImage").css("display", "block");
    }
    else
    {
        console.log("Hide the book cover!");
        $("#coverImage").css("display", "none");
        if ($("#tableOfContents").css("display") == "none")
            displaySideBar(false);
    }

    return;
}

function displaySideBar(toggle)
{
    if (toggle == true)
    {
        console.log("Show the sidebar!");
        $("#rightSidebar").css("display", "block");
    }
    else
    {
        console.log("Hide the sidebar!");
        $("#rightSidebar").css("display", "none");
    }

    return;
}

function checkDocumentRadio()
{
    var selection = $('input[name="documentChanges"]'); //document.quiz.colour;

    for (i=0; i<selection.length; i++)

        if (selection[i].checked == true)
            return selection[i].value;

    return "this";
}

function saveDocumentChanges()
{
    console.log("Saving changes to "+checkDocumentRadio());
    console.log("TOC="+$('input[name="TOC"]').attr('checked'));
    console.log("Cover Image="+$('input[name="bookCover"]').attr('checked'));

    var myurl = document.URL;

    var collection_name = getSubstring(myurl, "&c", "&");
    var document_id = getSubstring(myurl, "&d", "&");
    var document_type = getSubstring(myurl, "&dt", "&");
    var prev_action = getSubstring(myurl, "&p.a", "&");
    var prev_service = getSubstring(myurl, "&p.s", "&");

    var post_url = "http://localhost:8383/greenstone3/dev?a=f&sa=saveDocument&c=" + collection_name + "&d=" + document_id + "&dt=" + document_type + "&p.a=" + prev_action + "&p.s=" + prev_service;

    // XML will be automatically wrapped in <display><format> tags when saved to collection config
    var xml = '<format><gsf:option name="TOC" value="'+$('input[name="TOC"]').attr('checked')+'"/><gsf:option name="coverImage" value="'+$('input[name="bookCover"]').attr('checked')+'"/></format>';

    $.post(post_url, {data: xml}, function(data) {
            console.log("Success, we have received data");
    }, 'xml');
}

/* FUNCTIONS FOR FORMAT EDITING */                                                                    

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
    
function getFormatStatement()
{
    var formatDiv = document.getElementById('formatStatement');
    var formatStatement = innerXHTML(formatDiv);
    return formatStatement;
}

function checkClassifierRadio()
{
    var selection = $('input[name="classifiers"]'); //document.quiz.colour;

    for (i=0; i<selection.length; i++)

          if (selection[i].checked == true)
              return selection[i].value;

    return "this";

}

function updateFormatStatement()
{
    var formatStatement = getFormatStatement();

    var thisOrAll = checkClassifierRadio();
    console.log(thisOrAll);
    var myurl = document.URL;

    var collection_name = getSubstring(myurl, "&c", "&");
    var service_name = getSubstring(myurl, "&s", "&");  

    if(thisOrAll == "all")
        service_name = "AllClassifierBrowse";

    var classifier_name = null;

    if(service_name == "ClassifierBrowse")
        classifier_name = getSubstring(myurl, "&cl", "&");

    var post_url = "http://localhost:8383/greenstone3/dev?a=f&sa=update&c=" + collection_name +"&s=" + service_name;

    if(classifier_name != null)
        post_url = post_url + "&cl=" + classifier_name;

    $.post(post_url, {data: formatStatement}, function(data) {
        //$('.result').innerHTML = data; //html(data);
    
        // An error is returned because there is no valid XSLT for a format update action, there probably shouldn't be one so we ignore what the post returns.    
        console.log("Successfully updated");
        //console.log(data);
        }, 'html');
}

function saveFormatStatement()
{
    var formatStatement = getFormatStatement();
    var thisOrAll = checkClassifierRadio();

    var myurl = document.URL;

    var collection_name = getSubstring(myurl, "&c", "&");
    var service_name = getSubstring(myurl, "&s", "&");
    var classifier_name = null;

    if(thisOrAll == "all")
        service_name = "AllClassifierBrowse";

    if(service_name == "ClassifierBrowse")
        classifier_name = getSubstring(myurl, "&cl", "&");

    var post_url = "http://localhost:8383/greenstone3/dev?a=f&sa=save&c=" + collection_name +"&s=" + service_name;

    if(classifier_name != null)
        post_url = post_url + "&cl=" + classifier_name;

    $.post(post_url, {data: formatStatement}, function(data) {
        // An error is returned because there is no valid XSLT for a format update action, there probably shouldn't be one so we ignore what the post returns.    
        console.log("Successfully saved");
        }, 'html');
}

function getXSLT(classname)
{
    var myurl = document.URL;

    var collection_name = getSubstring(myurl, "&c", "&");
    var document_id = getSubstring(myurl, "&d", "&");
    var document_type = getSubstring(myurl, "&dt", "&");
    var prev_action = getSubstring(myurl, "&p.a", "&");
    var prev_service = getSubstring(myurl, "&p.s", "&");

    var post_url = "http://localhost:8383/greenstone3/dev?a=d&c=" + collection_name + "&d=" + document_id + "&dt=" + document_type + "&p.a=" + prev_action + "&p.s=" + prev_service + "&o=skinandlib";

    $.post(post_url, {data: classname}, function(data) {
            console.log("Success, we have received data");
            classname = "." + classname;
            console.log(classname); 
            var content = $( data ).find(classname);
            console.log(content.xml());
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
        
function find_class(current)
{
    var classes = current.className.split(' ');
    var none = "";
    for(var i = 0; i < classes.length; i++)
    {
        switch(classes[i])
        {
            case 'gsf_template':
              return create_gsf_template(current);
            default:
            {
              console.log("Class not found");
              return none;
            }
        }
    }
}

function create_gsf_template(current)
{
    // find match text which is an input with class match
    var match = $(current).find('.match')[0].value;
    console.log(match);
    
    // find mode text which is an input with class mode
    var mode = $(current).find('.mode')[0].value;
    console.log(mode);

    // "<gsf:template match=\"classifierNode\" mode=\"horizontal\">"
    var gsf = "<gsf:template match=\"" + match + "\"";
    if(mode != "vertical")
        gsf = gsf + " mode=\"" + mode + "\"";
    gsf = gsf + ">";
    
    return gsf;

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

    /* DOCUMENT SPECIFIC FUNCTIONS */

    $('.sectionHeader').click(function () {
        console.log('section Header click *');
        getXSLT("sectionHeader");
        return false; //don't event bubble
    });

    $('.sectionContainer').click(function () {
        console.log('section Container click *');
        getXSLT("sectionContainer");
        return false; // don't event bubble
    });
	
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

	//Retrieve the collection metadataset using ajax
	$.ajax
	({
		type: "GET",
		url: "?a=g&s=CoverageMetadataRetrieve&o=xml&rt=r&c=" + collection,
		success: function(data) 
		{
			var str = "<select name=\"meta_select\" onChange=\"onSelectChange(this)\">";
			
			var selectorArea = document.getElementById("metadataSelector");		
			var metadataSets = data.getElementsByTagName("metadataSet");
			for(var i = 0; i < metadataSets.length; i++)
			{
				var metadata = metadataSets[i].getElementsByTagName("metadata");
				for(var j = 0; j < metadata.length; j++)
				{
					var metaValue = metadataSets[i].getAttribute("name") + "." + metadata[j].getAttribute("name");
					str += "<option value=\"" + metaValue + "\">" + metaValue + "</option>";
				}
			}
			
			str += "</select>";

			selectorArea.innerHTML = str;
			gsf_metadata_element = str;
		}
	});

$.ui.plugin.add("draggable", "cursor", {
	start: function(event, ui) {
		var t = $('body'), o = $(this).data('draggable').options;
		if (t.css("cursor")) o._cursor = t.css("cursor");
		t.css("cursor", o.cursor);
	},
	stop: function(event, ui) {
		var o = $(this).data('draggable').options;
		if (o._cursor) $('body').css("cursor", o._cursor);
	}
});

	$.ui.draggable.prototype._createHelper = function(event) {

                var o = this.options;
                var helper = $.isFunction(o.helper) ? $(o.helper.apply(this.element[0], [event])) : (o.helper == 'clone' ? this.element.clone().removeAttr('id') : this.element);

		var select = $(this.element).find('select');
		var value = select.attr('value');
		console.log("Found "+value+" in helper");
		CURRENT_SELECT_VALUE = value;
		helper.find('select').attr('value', value);

                if(!helper.parents('body').length)
                        helper.appendTo((o.appendTo == 'parent' ? this.element[0].parentNode : o.appendTo));

                if(helper[0] != this.element[0] && !(/(fixed|absolute)/).test(helper.css("position")))
                        helper.css("position", "absolute");

                return helper;

        };
	
	$.ui.sortable.prototype._removeCurrentsFromItems = function() {
		//console.log("IN _removeCurrentsFromItems FUNCTION");
		//console.log("this = " + this.currentItem[0].getAttribute('class'));
		var list = this.currentItem.find(":data(sortable-item)");

		var i = 0;
		while (i<this.items.length) {
			var found_match = false;
			for (var j=0; j<list.length; j++) {
				if(this.items[i])
				{
					if(list[j] == this.items[i].item[0]) {
						//console.log("Item to splice = " + this.items[i].item[0].getAttribute('class'));
						this.items.splice(i,1);
						found_match = true;
						break;
					}
				}
			};
			if (!found_match)
				i++;
			else
				break;
		}
	};
	
	$.ui.plugin.add("draggable", "connectToSortable", {
	start: function(event, ui) {
        //console.log("FUNCTION start draggable connectToSortable");
		var inst = $(this).data("draggable"), o = inst.options,
			uiSortable = $.extend({}, ui, { item: inst.element });
		inst.sortables = [];
		$(o.connectToSortable).each(function() {
			var sortable = $.data(this, 'sortable');
			if (sortable && !sortable.options.disabled) {
				inst.sortables.push({
					instance: sortable,
					shouldRevert: sortable.options.revert
				});
				sortable._refreshItems();	//Do a one-time refresh at start to refresh the containerCache
				sortable._trigger("activate", event, uiSortable);
			}
		});

	},
	stop: function(event, ui) {

        //console.log("FUNCTION stop draggable connectToSortable");
		//If we are still over the sortable, we fake the stop event of the sortable, but also remove helper
		var inst = $(this).data("draggable"),
			uiSortable = $.extend({}, ui, { item: inst.element });

		$.each(inst.sortables, function() {
			if(this.instance.isOver) {

				this.instance.isOver = 0;

				inst.cancelHelperRemoval = true; //Don't remove the helper in the draggable instance
				this.instance.cancelHelperRemoval = false; //Remove it in the sortable instance (so sortable plugins like revert still work)

				//The sortable revert is supported, and we have to set a temporary dropped variable on the draggable to support revert: 'valid/invalid'
				if(this.shouldRevert) this.instance.options.revert = true;

				//Trigger the stop of the sortable
                //console.log("Draggable tells sortable to stop");
				this.instance._mouseStop(event);

				this.instance.options.helper = this.instance.options._helper;

				//If the helper has been the original item, restore properties in the sortable
				if(inst.options.helper == 'original')
					this.instance.currentItem.css({ top: 'auto', left: 'auto' });

			} else {
				this.instance.cancelHelperRemoval = false; //Remove the helper in the sortable instance
				this.instance._trigger("deactivate", event, uiSortable);
			}

		});

	},
	drag: function(event, ui) {
        //console.log("FUNCTION drag draggable connectToSortable");

		var inst = $(this).data("draggable"), self = this;

		var checkPos = function(o) {
			var dyClick = this.offset.click.top, dxClick = this.offset.click.left;
			var helperTop = this.positionAbs.top, helperLeft = this.positionAbs.left;
			var itemHeight = o.height, itemWidth = o.width;
			var itemTop = o.top, itemLeft = o.left;

			return $.ui.isOver(helperTop + dyClick, helperLeft + dxClick, itemTop, itemLeft, itemHeight, itemWidth);
		};

        var intersecting_items = new Array();

		$.each(inst.sortables, function(i) {
			
			//Copy over some variables to allow calling the sortable's native _intersectsWith
			this.instance.positionAbs = inst.positionAbs;
			this.instance.helperProportions = inst.helperProportions;
			this.instance.offset.click = inst.offset.click;
			
			if(this.instance._intersectsWith(this.instance.containerCache)) {

				//If it intersects, we use a little isOver variable and set it once, so our move-in stuff gets fired only once
				//if(!this.instance.isOver) {

                    //console.log('Line 1113');

				//	this.instance.isOver = 1;

                    intersecting_items.push(this.instance); // sam
                //} //sam

					//Now we fake the start of dragging for the sortable instance,
					//by cloning the list group item, appending it to the sortable and using it as inst.currentItem
					//We can then fire the start event of the sortable with our passed browser event, and our own helper (so it doesn't create a new one)
					//sam this.instance.currentItem = $(self).clone().appendTo(this.instance.element).data("sortable-item", true);
					//sam this.instance.options._helper = this.instance.options.helper; //Store helper option to later restore it
					//sam this.instance.options.helper = function() { return ui.helper[0]; };

					//sam event.target = this.instance.currentItem[0];
					//sam this.instance._mouseCapture(event, true);
					//sam this.instance._mouseStart(event, true, true);

					//Because the browser event is way off the new appended portlet, we modify a couple of variables to reflect the changes
					//sam this.instance.offset.click.top = inst.offset.click.top;
					//sam this.instance.offset.click.left = inst.offset.click.left;
					//sam this.instance.offset.parent.left -= inst.offset.parent.left - this.instance.offset.parent.left;
					//sam this.instance.offset.parent.top -= inst.offset.parent.top - this.instance.offset.parent.top;

					//sam inst._trigger("toSortable", event);
					//sam inst.dropped = this.instance.element; //draggable revert needs that
					//hack so receive/update callbacks work (mostly)
					//sam inst.currentItem = inst.element;
					//sam this.instance.fromOutside = inst;

				//sam brace

				//Provided we did all the previous steps, we can fire the drag event of the sortable on every draggable drag, when it intersects with the sortable
				//sam if(this.instance.currentItem) this.instance._mouseDrag(event);

			} else {

				//If it doesn't intersect with the sortable, and it intersected before,
				//we fake the drag stop of the sortable, but make sure it doesn't remove the helper by using cancelHelperRemoval
				if(this.instance.isOver) {

                    console.log("UNSETTING ISOVER");
                    console.log("ON ITEM="+this.instance.currentItem[0].getAttribute('class'))
					this.instance.isOver = 0;
					this.instance.cancelHelperRemoval = true;
					
					//Prevent reverting on this forced stop
					this.instance.options.revert = false;
					
					// The out event needs to be triggered independently
					this.instance._trigger('out', event, this.instance._uiHash(this.instance));
					
					this.instance._mouseStop(event, true);
					this.instance.options.helper = this.instance.options._helper;

					//Now we remove our currentItem, the list group clone again, and the placeholder, and animate the helper back to it's original size
                    //console.log("DO WE GET HERE?");
					this.instance.currentItem.remove();
					if(this.instance.placeholder) this.instance.placeholder.remove();

					inst._trigger("fromSortable", event);
					inst.dropped = false; //draggable revert needs that
				
	            }
       
            } 
        });

        //sam
        //console.log("Contents of intersecting_items");
        var innermostContainer = null, innermostIndex = null;       
        for (i=0;i<intersecting_items.length;i++)
        {
            //console.log('ITEM: '+intersecting_items[i].element[0].getAttribute('class'));
    
            if(innermostContainer && $.ui.contains(intersecting_items[i].element[0], innermostContainer.element[0]))
                continue;
                        
            innermostContainer = intersecting_items[i];
            innermostIndex = i; 
                    
        }

        for (i=0;i<intersecting_items.length;i++)
        {
            if(intersecting_items[i] != innermostContainer)
                if(intersecting_items[i].isOver) {

                    console.log("UNSETTING ISOVER");
                    console.log("ON ITEM="+intersecting_items[i].currentItem[0].getAttribute('class'))
                    intersecting_items[i].isOver = 0;
                    intersecting_items[i].cancelHelperRemoval = true;

                    //Prevent reverting on this forced stop
                    intersecting_items[i].options.revert = false;

                    // The out event needs to be triggered independently
                    intersecting_items[i]._trigger('out', event, intersecting_items[i]._uiHash(intersecting_items[i]));

                    intersecting_items[i]._mouseStop(event, true);
                    intersecting_items[i].options.helper = intersecting_items[i].options._helper;

                    //Now we remove our currentItem, the list group clone again, and the placeholder, and animate the helper back to it's original size
                    //console.log("DO WE GET HERE?");
                    if(intersecting_items[i].currentItem) intersecting_items[i].currentItem.remove();
                    if(intersecting_items[i].placeholder) intersecting_items[i].placeholder.remove();

                    inst._trigger("fromSortable", event);
                    inst.dropped = false; //draggable revert needs that
                }

                intersecting_items[i].isOver = 0;
            
        }

        if(innermostContainer && !innermostContainer.isOver)
        {
            console.log("INNER="+innermostContainer.element[0].getAttribute('class'));
            console.log("SETTING ISOVER");
    	    innermostContainer.isOver = 1;

            //Now we fake the start of dragging for the sortable instance,
            //by cloning the list group item, appending it to the sortable and using it as inst.currentItem
            //We can then fire the start event of the sortable with our passed browser event, and our own helper (so it doesn't create a new one)

            if(innermostContainer.currentItem) innermostContainer.currentItem.remove();
            if(innermostContainer.placeholder) innermostContainer.placeholder.remove();

            innermostContainer.currentItem = $(self).clone().appendTo(innermostContainer.element).data("sortable-item", true);
            
            innermostContainer.options._helper = innermostContainer.options.helper; //Store helper option to later restore it
            innermostContainer.options.helper = function() { return ui.helper[0]; };

            console.log("EVENT TARGET="+innermostContainer.currentItem[0].getAttribute('class'));
            event.target = innermostContainer.currentItem[0];
            innermostContainer._mouseCapture(event, true);
            innermostContainer._mouseStart(event, true, true);

            //Because the browser event is way off the new appended portlet, we modify a couple of variables to reflect the changes
            innermostContainer.offset.click.top = inst.offset.click.top;
            innermostContainer.offset.click.left = inst.offset.click.left;
            innermostContainer.offset.parent.left -= inst.offset.parent.left - innermostContainer.offset.parent.left;
            innermostContainer.offset.parent.top -= inst.offset.parent.top - innermostContainer.offset.parent.top;

            inst._trigger("toSortable", event);
            inst.dropped = innermostContainer.element; //draggable revert needs that
            //hack so receive/update callbacks work (mostly)
            inst.currentItem = inst.element;
            innermostContainer.fromOutside = inst;

            //sam brace
        }

        if(innermostContainer)
        {
            //Provided we did all the previous steps, we can fire the drag event of the sortable on every draggable drag, when it intersects with the sortable
            if(innermostContainer.currentItem) innermostContainer._mouseDrag(event);
        }

	}
});

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
    if(me.search("select") != -1)
    {
    // If select exists, then find CURRENT_SELECT_VALUE
	if(me.search(CURRENT_SELECT_VALUE) != -1)
	{
	    var index = me.search(CURRENT_SELECT_VALUE);
            if(index == -1)
                console.log("Did not find " + CURRENT_SELECT_VALUE);
            else
	    {
                console.log("Found " + CURRENT_SELECT_VALUE + " at index " + index);    
                index = index + CURRENT_SELECT_VALUE.length + 1;
                console.log("Attempt inserting select at new index "+index);
                a = me.substring(0,index);
                b = me.substring(index);
                me = a.concat("selected",b);
            }
	}
    }

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
            'connectWith':'column'});

    $('.td-div').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.gsf_metadata, .gsf_choose_metadata, .gsf_link, .gsf_switch',
            'placeholder':'placeholder',
            receive: function(event, ui) { alert("Attempted to receive"); },
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
            stop: function(event, ui) {
                // gsf metadata
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_metadata')) { replace_with(ui.item, gsf_metadata_element); } 
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

