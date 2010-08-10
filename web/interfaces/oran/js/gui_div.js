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

    /******************************************/
    /*              DRAGGABLES                */
    /******************************************/

    $(".element_type_gsf_template").draggable({
            connectToSortable: '#formatStatement',
            helper: 'clone',
            revert: 'invalid'
    });

    $(".element_type_table").draggable({
            connectToSortable: '.gsf_template',
            helper: 'clone',
            revert: 'invalid'
    });

    $(".element_type_tr").draggable({
            connectToSortable: '.gsf_table',
            helper: 'clone',
            revert: 'invalid'
    })

    $(".element_type_td").draggable({
            connectToSortable: '.gsf_table',
            helper: 'clone',
            revert: 'invalid'
    })

    $(".element_type_text").draggable({
            connectToSortable: '.gsf_otherwise',
            helper: 'clone',
            revert: 'invalid'
    });
    
    $(".element_type_gsf_choose_metadata").draggable({
            connectToSortable: '.gsf_template',
            helper: 'clone',
            revert: 'invalid'
    });

    $(".element_type_gsf_metadata").draggable({
            connectToSortable: '.gsf_choose_metadata, .gsf_template, .gsf_when, .gsf_otherwise, .td',
            helper: 'clone',
            revert: 'invalid'
    });

    $(".element_type_gsf_link").draggable({
            connectToSortable: '.gsf_template',
            helper: 'clone',
            revert: 'invalid'
    });

    // switch, when, otherwise, icon
    $(".element_type_gsf_switch").draggable({
            connectToSortable: '.gsf_template',
            helper: 'clone',
            revert: 'invalid'
    });

    $(".element_type_gsf_when").draggable({
            connectToSortable: '.gsf_switch',
            helper: 'clone',
            revert: 'invalid'
    });

    $(".element_type_gsf_otherwise").draggable({
            connectToSortable: '.gsf_switch',
            helper: 'clone',
            revert: 'invalid'
    });

    $(".element_type_gsf_icon").draggable({
            connectToSortable: '.gsf_link',
            helper: 'clone',
            revert: 'invalid'
    });

 
    /******************************************/
    /*               SORTABLES                */
    /******************************************/

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
    

    //$(".resizable").resizable({containment: 'parent', alsoResize:'parent'});

    $('.tr').equalHeights();

    $(".td").resizable({
                alsoResize: 'parent',
                //containment: 'parent',
                handles: 'e,s',
                stop: function(event, ui) {
                        $(this).parent().parent().equalHeights();
                }, });

}); 

/*******************************************************************************/

function bind_all_sortables()
{
    bind_template_sortable();
    bind_table_sortable();
    bind_tr_sortable();
    bind_td_sortable();
    bind_choose_metadata_sortable();
    bind_link_sortable();
    bind_switch_sortable();
    bind_when_sortable();
    bind_otherwise_sortable();

    bind_block_mouseover();
    bind_minmax_remove();    
}

function replace_with(item, me)
{
    item.replaceWith(me); //'<div class="element element-txt">This text box has been added!</div>');
    bind_all_sortables();
}

function bind_template_sortable()
{
    $('.gsf_template').sortable({
            'cursor':'pointer',
            'tolerance': 'fit',
            'items':'.table, .gsf_choose_metadata, .gsf_metadata',
            'placeholder':'placeholder',
            //'nested':'.gsf:metadata'
            stop: function(event, ui) {
                //if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('css_table')) { replace_with(ui.item, "<table border=\"1\" width=\"100%\" height=\"50px\"><tr><td><div class=\"td block\" title=\"td-div\">XXXXXXXXXXXXXXXXXXXXXXXX</div></td></tr></table>"); }
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_table')) { replace_with(ui.item, "<table class=\"table\" border=\"2\"></table>"); }
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_gsf_choose_metadata')) { replace_with(ui.item, gsf_choose_metadata_element); }
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_gsf_metadata')) { replace_with(ui.item, gsf_metadata_element); }
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_gsf_link')) { replace_with(ui.item, gsf_link_element); }
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_gsf_switch')) { replace_with(ui.item, gsf_switch_element); }
            }
    });

}

function bind_table_sortable()
{
    $('.table').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.tr',
            'placeholder':'placeholder',
            //'nested':'.gsf:metadata'
            stop: function(event, ui) {
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_tr')) { replace_with(ui.item, "<tr class=\"tr\"></tr>"); }
            }

    });
}

function bind_tr_sortable()
{
    $('.tr').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.td',
            'placeholder':'placeholder',
            //'nested':'.gsf:metadata'
            stop: function(event, ui) {
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_td')) { replace_with(ui.item, "<td class=\"td\"></td>"); }
            }

    });
}

function bind_td_sortable()
{
    $('.td').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.gsf_metadata, .gsf_choose_metadata, .gsf_link, .gsf_switch',
            'placeholder':'placeholder',
            //'nested':'.gsf:metadata'
            stop: function(event, ui) {
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_gsf_metadata')) { replace_with(ui.item, gsf_metadata_element); }
            }

    });
}

function bind_choose_metadata_sortable()
{
    $('.gsf_choose_metadata').sortable({
            'cursor':'pointer',
            'tolerance': 'fit',
            'items':'.gsf_metadata',
            'placeholder':'placeholder',
            'connectWith':'.gsf_choose_metadata',
            //'nested':'.gsf:metadata'
            stop: function(event, ui) {
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_gsf_metadata')) { replace_with(ui.item, gsf_metadata_element); }
            }
    });
}

function bind_link_sortable()
{
    $('.gsf_link').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.gsf_icon',
            'placeholder':'placeholder'
            //'nested':'.gsf:metadata'
    });
}

function bind_switch_sortable()
{
    $('.gsf_switch').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.gsf_metadata, .gsf_when, .gsf_otherwise',
            'placeholder':'placeholder',
            //'nested':'.gsf:metadata'
            stop: function(event, ui) {
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_gsf_when')) { replace_with(ui.item, gsf_when_element); }
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_gsf_otherwise')) { replace_with(ui.item, gsf_otherwise_element); }
            }
    });
}

function bind_when_sortable()
{
    $('.gsf_when').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.leaf',
            'placeholder':'placeholder',
            //'nested':'.gsf:metadata'
            stop: function(event, ui) {
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_gsf_metadata')) { replace_with(ui.item, gsf_metadata_element); }
            }
    });
}

function bind_otherwise_sortable()
{
    $('.gsf_otherwise').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.leaf',
            'placeholder':'placeholder',
            //'nested':'.gsf:metadata'
            stop: function(event, ui) {
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_gsf_metadata')) { replace_with(ui.item, gsf_metadata_element); }
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_text')) { replace_with(ui.item, "<input type=\"text\" name=\"rawtextinput\" size=\"10\"/>"); }
            }
    });
}

function bind_block_mouseover()
{
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
}

function bind_minmax_remove()
{
    $('a.minmax').bind('click', toggleContent);
    $('a.remove').bind('click', removeContent);
};

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

