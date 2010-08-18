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
            //connectToSortable: '.td',
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
 
    $('div.gsf_template').children(".block,.table").slideUp(300); 
    //$('div.gsf_template .table').hide();  
    //$('div.block').click(function() {    
        //alert("I was clicked!");
    //    $(this).children(".block").slideToggle('fast');
    //    return false;
    //});

 
    //hide the all of the element with class msg_body
  //$(".gsf_template > table").hide();
  //toggle the componenet with class msg_body
  //$(".gsf_template").click(function()
  //{
    //alert("You clicked? " + $("this > .table"));
    //$(this).getElementsByTagName("table")[0].slideToggle(600);
  //});

    //$(".resizable").resizable({containment: 'parent', alsoResize:'parent'});

    //$('.tr').equalHeights();

    //$(".td").resizable({
    //            alsoResize: 'parent',
                //containment: 'parent',
    //            handles: 'e,s',
    //            stop: function(event, ui) {
    //                    $(this).parent().parent().equalHeights();
    //            }, });

}); 

/*******************************************************************************/

function minimize_templates()
{
    //$(".msg_body").hide();
    //var targetContent = $('.table');
    //targetContent.hide();
    //targetContent.parent().css('display','none');
    //targetContent.slideUp(300);
    //targetContent.parent().html('[+]');

}

function bind_all_sortables()
{
    bind_template_sortable();
    //bind_table_sortable();
    //bind_tr_sortable();
    //bind_td_sortable();
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
    $('.tr').equalHeights();

    $(".td").resizable({
                alsoResize: 'parent',
                //containment: 'parent',
                handles: 'w,e',
                stop: function(event, ui) {
                        $(this).parent().parent().equalHeights();
                }, });

    $(".droppable").droppable({
            accept: '.element_type_td',
            tolerance: 'pointer', 
            activate: function(event, ui) { $(this).addClass("droppable_hl"); console.log("droppable activated")},
            deactivate: function(event, ui) { $(this).removeClass("droppable_hl"); console.log("droppable deactivated")},
            drop: function(event, ui) {
                //alert("Something has been dropped on me!!");
                //$(this).addClass('ui-state-highlight').find('p').html('Dropped!');
                 //var tr = this.parentNode.parentNode; //$(this).getElementsByTagName("tr")[0];
                 var td = document.createElement("td");
                 var div = document.createElement("div"); // class=\"td block\" title=\"td-div\"");
                 div.setAttribute("title","td-div");
                 div.setAttribute("class","td block");
                 div.setAttribute("width","25px");
                 td.appendChild(div);
                 var sep = document.createElement("td");
                 sep.setAttribute("class","droppable");
                 sep.setAttribute("width","10px"); 
                 $(this).after(sep);                
                 $(this).after(td);                
                 bind_tables();
                 //bind_td_sortable();
                 //bind_all_sortables();
            }
        });

}

function replace_with(item, me)
{
/*
    (item.parents('.table')).each(function(index) { 
        $(this).children().children().children('td').each(function() {
            var sum = 0;
            $(this).children('.block').each(function() { sum = sum + $(this).height(); } );
            console.log("My height is " + $(this).height() + ", sum height is " + sum);
        });
    });
*/
    /*
    alert(item.parents('.td').length);

    (item.parents('.td')).each(function(index) { 
        $(this).children.each(function()
        alert(index + " height is " + $(this).height()); // + $(this).style('height') + $(this).offsetHeight); 
        var h = $(this).height(); 
        var a = 0;
        // if we are now the maximum then set to h otherwise ignore?
        $(this).parents('.table').each(function() {$(this).children().children().children().each(function() { if($(this).height() > a) a = $(this).height(); } ) } );
        alert("A is " + a);
        if( h > a)
        {
            h = h + 25; 
            alert("Increasing height to " + h);
            $(this).height(h);
            alert("height is now " + $(this).height());
        }
        else
            alert("height did not need increasing (h=" + h + ",a=" + a + ")");
    }); //forEach().css('height'));
    */
    
    item.replaceWith(me); //'<div class="element element-txt">This text box has been added!</div>');

    (item.parents('.table')).each(function(index) {
        $(this).children().children().children().each(function() {
            var sum = 0;
            $(this).children('.block').each(function() { sum = sum + $(this).height(); } );
            console.log("My height is " + $(this).height() + ", sum height is " + sum);
        });
    });

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

/*
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

*/

function bind_td_sortable()
{
    $('.td').sortable({
            'cursor':'pointer',
            'containment':'parent',
            'tolerance': 'pointer',
            'items':'.gsf_metadata, .gsf_choose_metadata, .gsf_link, .gsf_switch, .td',
            'placeholder':'placeholder',
            //'nested':'.gsf:metadata'
            receive: function(event, ui) { alert("Attempted to receive"); },
            stop: function(event, ui) {
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_gsf_metadata')) { replace_with(ui.item, gsf_metadata_element); }
                /*if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_td')) { 
                    alert("Inserting td " + this.parentNode.parentNode + " " + $('.td').sortable( "widget" )); 
                    var tr = this.parentNode.parentNode; //$(this).getElementsByTagName("tr")[0];
                    var td = document.createElement("td"); 
                    var div = document.createElement("div"); // class=\"td block\" title=\"td-div\"");
                    //var cls = document.createAttribute("class");
                    div.setAttribute("title","td-div");
                    div.setAttribute("class","td block");
                    var span = document.createElement("div");
                    span.setAttribute("class", "block gsf_metadata");
                    span.setAttribute("style","height:50px");
                    //div.setAttribute(cls,"block");
                    div.appendChild(span);
                    td.appendChild(div);
                    tr.appendChild(td); 
                    ui.item.remove(); 
                    bind_all_sortables();
                }*/
                
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
            'placeholder':'placeholder',
            //'nested':'.gsf:metadata'
            stop: function(event, ui) {
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_gsf_icon')) { replace_with(ui.item, gsf_icon_element); }
            }
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
    //var targetContent = $('div.block', this.parentNode);
    //$(".msg_head").click(function()
  //{
    //$(this).next(".block").slideToggle(600);
  //});
    //alert($(this).html() + " " + $(this).css('display') + " " + targetContent.css('display'));
    if ($(this).html() == '[+]'){ //targetContent.css('display') == 'none') {
    //if ($(this).css('display') == 'none') {
   //     var targetContent2 = $('.table');
   //     targetContent2.show();

   //     targetContent.show();
   //     targetContent.slideDown(300);
   //     alert("I should be sliding down! " + $(this).parent());
        $(this).parent().children(".block,.table").slideDown(300);
        $(this).html('[-]');
    } else {
    //    targetContent.slideUp(300);
        $(this).parent().children(".block,.table").slideUp(300);
        $(this).html('[+]');
    }
    //$(this).children(".block, .table").slideToggle('fast');
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
    //console.log("EQUAL HEIGHTS");
    $(this).each(function(){
        var currentTallest = 0;
        //console.log($(this).children());
        //console.log($(this).children().children());
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

