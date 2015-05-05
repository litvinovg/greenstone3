
console.log("Loading format_jquery_prototypes.js\n");

$(document).ready(function()
{
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

    //Retrieve the collection metadataset using ajax
    $.ajax
    ({
        type: "GET",
        url: "?a=g&s=CoverageMetadataRetrieve&o=xml&rt=r&c=" + host_info.collection_name,
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
            gsf_metadata_element =  "<div class=\"gsf_metadata css_gsf_metadata block leaf\" title=\"gsf:metadata\"><table class=\"header\"><tbody><tr><td class=\"header\">" + str + "</td><td class=\"header\"><a href=\"#\" class=\"remove ui-icon ui-icon-closethick\" title=\"Click me to remove\"/></td></tr></tbody></table></div>";
            //console.log("Metadata element from ajax: " + gsf_metadata_element);
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
            var inst = $(this).data("draggable"), o = inst.options, uiSortable = $.extend({}, ui, { item: inst.element });
            inst.sortables = [];
            $(o.connectToSortable).each(function() {
                var sortable = $.data(this, 'sortable');
                if (sortable && !sortable.options.disabled) {
                    inst.sortables.push({instance: sortable, shouldRevert: sortable.options.revert});
                    sortable._refreshItems();   //Do a one-time refresh at start to refresh the containerCache
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

                    //  this.instance.isOver = 1;

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

});
