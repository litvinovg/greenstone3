//jQuery( function($) {
//$('div.blockWrapper').iNestedSortable(
//  {
//    accept: 'block',
//  }
//);
//});

console.log("Loading gui_div.js\n");

$(document).ready(function(){

    console.log("Document ready function\n");

    $('a.minmax').bind('click', toggleContent);
    $('a.remove').bind('click', removeContent);

    $(".elementType").draggable({
            connectToSortable: '.gsf_template',
            helper: 'clone',
            revert: 'invalid',
            //start: function(event, ui) { ui.item.addClass('replaceMe');},
            stop: function(event, ui) {
                console.log("Stopped dragging - do we want to create element?"+ui.helper);
                ui.helper.addClass('replaceMe');
            },
        });

    /*
    $('.elementType').sortable({
            cursor: 'pointer',
            tolerance: 'pointer',
            //placeholder:'placeholder',
            connectWith:'.gsf_template',
            //cancel: '.elementType',
            
            start: function(event, ui) {
                        $(this).clone().insertAfter(this);;
            },

    });
    */
    
     function templateAdded() {
         var item = $('.elementType');
         // do something with "item" - its your new pretty cloned dropped item ;]
         item.replaceWith('<h2>REPLACED</h2>');  
         //item.addClass( 'added' );
     };    
 
    $('#formatStatement').sortable({
            cursor: 'pointer',
            tolerance: 'pointer',
            items: '.gsf_template',
            placeholder:'placeholder'
            //'nested':'div'
    });
    
    $('.gsf_template').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.table, .gsf_choose-metadata, .gsf_metadata',
            'placeholder':'placeholder',
            //'nested':'.gsf:metadata'
            stop: function(event, ui) {
                if (ui.item.hasClass("elementType") && ui.item.hasClass('css_gsf_template')) {
                        ui.item.replaceWith(gsf_metadata_element); //'<div class="element element-txt">This text box has been added!</div>');
                        //ui.item.style.border = 
                }
            }

            //receive: function(event, ui) { 
            //    console.log("I have received an item");
                //var item = $('.ui_sortable/.elementType');
            //    var item = $('.div/.elementType'); //css_gsf_template not(.gsf_template)');
            //    console.log(item);
                //item.replaceWith('<h2>REPLACED</h2>');
            //}

    });

    $('.td').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.leaf .gsf_choose-metadata, .gsf_link, .gsf_switch',
            'placeholder':'placeholder'
            //'nested':'.gsf:metadata'
    });
 
    $('.gsf_choose-metadata').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.gsf_metadata',
            'placeholder':'placeholder',
            'connectWith':'.gsf_choose-metadata'
            //'nested':'.gsf:metadata'
    });
    
    $('.gsf_link').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.gsf_icon',
            'placeholder':'placeholder'
            //'nested':'.gsf:metadata'
    });

    $('.gsf_switch').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.gsf_metadata, .gsf_when, .gsf_otherwise',
            'placeholder':'placeholder'
            //'nested':'.gsf:metadata'
    });

    $('.gsf_when').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.leaf',
            'placeholder':'placeholder'
            //'nested':'.gsf:metadata'
    });

    $('.gsf_otherwise').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.leaf',
            'placeholder':'placeholder'
            //'nested':'.gsf:metadata'
    });

    //$(".resizable").resizable({containment: 'parent', alsoResize:'parent'});

    $('.tr').equalHeights();

    $(".td").resizable({
                alsoResize: 'parent',
                //containment: 'parent',
                handles: 'e,s',
                stop: function(event, ui) {
                        $(this).parent().parent().equalHeights();
                }, });


    $(".block").mouseover(function() 
    {
        //alert("Mouse over event");
        $(this).parents().css("border", "");
        $(this).css("border", "1px solid blue");
        return false;
    }).mouseout(function(){
        $(this).css("border", "");
        return false;
    });

}); 

var removeContent = (function () {
        //var parentTag = $(this).parent().get(0).titleName;
        //alert("Removing div " + parentTag);
        $(this).parent().remove();
    });


var toggleContent = function(e)
{
    var targetContent = $('div.block', this.parentNode);
    if (targetContent.css('display') == 'none') {
        targetContent.slideDown(300);
        $(this).html('[-]');
    } else {
        targetContent.slideUp(300);
        $(this).html('[+]');
    }
    return false;
};

function serialize(s)
{
    serial = $.SortSerialize(s);
    alert(serial.hash);
};

/*-------------------------------------------------------------------- 
 * JQuery Plugin: "EqualHeights"
 * by:  Scott Jehl, Todd Parker, Maggie Costello Wachs (http://www.filamentgroup.com)
 *
 * Copyright (c) 2008 Filament Group
 * Licensed under GPL (http://www.opensource.org/licenses/gpl-license.php)
 *
 * Description: Compares the heights or widths of the top-level children of a provided element 
        and sets their min-height to the tallest height (or width to widest width). Sets in em units 
        by default if pxToEm() method is available.
 * Dependencies: jQuery library, pxToEm method  (article: 
        http://www.filamentgroup.com/lab/retaining_scalable_interfaces_with_pixel_to_em_conversion/)                              
 * Usage Example: $(element).equalHeights();
        Optional: to set min-height in px, pass a true argument: $(element).equalHeights(true);
 * Version: 2.0, 08.01.2008
--------------------------------------------------------------------*/

// Modified to get children of children ie. tr -> td -> div

$.fn.equalHeights = function(px) {
    console.log("EQUAL HEIGHTS");
    $(this).each(function(){
        var currentTallest = 0;
        console.log($(this).children());
        console.log($(this).children().children());
        //$(this).children().children().each(function(i){
        //    console.log($(this));
        //    console.log("THIS HEIGHT="+$(this).height()+ " CURRENT TALLEST="+ currentTallest);
            //if ($(this).height() > currentTallest) { currentTallest = $(this).height(); }
            //if ($(this).height() > currentTallest) { currentTallest = $(this).height(); }
        //});
        //if (!px || !Number.prototype.pxToEm) currentTallest = currentTallest.pxToEm(); //use ems unless px is specified
        // for ie6, set height since min-height isn't supported
        //if ($.browser.msie && $.browser.version == 6.0) { $(this).children().children().css({'height': $(this).currentTallest}); }
        $(this).children().children().css({'height': $(this).height()}); //currentTallest}); 
    });
    return this;
};

