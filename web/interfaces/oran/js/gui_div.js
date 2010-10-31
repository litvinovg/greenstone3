
console.log("Loading gui_div.js\n");

$(document).ready(function(){

    console.log("Document ready function\n");

    var CURRENT_SELECT_VALUE = "";

    /******************************************/
    /*              DRAGGABLES                */
    /******************************************/

    $(".element_type_gsf_template").draggable({
            cursor: 'crosshair',
            connectToSortable: '#formatStatement',
            helper: 'clone',
            revert: 'invalid'
    });

    $(".element_type_table").draggable({
            cursor: 'crosshair',
            connectToSortable: '.gsf_template',
            helper: 'clone',
            revert: 'invalid'
    });

    $(".element_type_tr").draggable({
            cursor: 'crosshair',
            connectToSortable: '.gsf_table',
            helper: 'clone',
            revert: 'invalid'
    })

    $(".element_type_td").draggable({
            cursor: 'crosshair',
            //connectToSortable: '.td',
            helper: 'clone',
            revert: 'invalid'
    })

    $(".element_type_text").draggable({
            cursor: 'crosshair',
            connectToSortable: '.gsf_otherwise, .gsf_link',
            helper: 'clone',
            revert: 'invalid'
    });
    
    $(".draggable_gsf_choose_metadata").draggable({
            cursor: 'crosshair',
            connectToSortable: '.td-div',
            helper: 'clone',
            revert: 'invalid'
    });

    //$(".element_type_gsf_metadata").draggable({
    $(".draggable_gsf_metadata").draggable({
            cursor: 'crosshair',
            connectToSortable: '.gsf_choose_metadata, .gsf_when, .gsf_otherwise, .td-div',
            helper: 'clone',
            revert: 'invalid'
    });

    $(".draggable_gsf_link").draggable({
            cursor: 'crosshair',
            connectToSortable: '.td-div',
            helper: 'clone',
            revert: 'invalid'
    });

    // switch, when, otherwise, icon
    $(".draggable_gsf_switch").draggable({
            cursor: 'crosshair',
            connectToSortable: '.td-div',
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
            connectToSortable: '.gsf_link',
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
    console.log('function bind_tables()');
    //$('.tr').resize_tables($(this)); //equalHeights();

    
    $('td').click(function () {
         console.log('td click *');
         return false;
    });

    $(".td-div").resizable({
                alsoResize: 'parent',
                //containment: 'parent',
                handles: 'w,e',
                stop: function(event, ui) {
                        console.log('Resize table on stop');
                        resize_tables($(this));
                        //$(this).parent().parent().equalHeights();
                }, });

    //$(".droppable").sortable({
    //        'cursor':'pointer',
    //        'tolerance': 'pointer',
    //        'items':'.column, .td-div',
    //        'placeholder':'placeholder'
    //});

    $(".droppable").droppable({
            accept: '.element_type_td',
            tolerance: 'pointer', 
            activate: function(event, ui) { $(this).addClass("droppable_hl");}, // console.log("droppable activated")},
            deactivate: function(event, ui) { $(this).removeClass("droppable_hl"); }, // console.log("droppable deactivated")},
            drop: function(event, ui) {
                //if ($(this).hasClass("ui-draggable"))
                //if (ui.helper.hasClass("ui-draggable"))
                //{
                 var neverempty = document.createElement("div");
                 neverempty.setAttribute("class","neverempty block");
                 neverempty.setAttribute("style","height:50px");
                 neverempty.setAttribute("style","display:block");
                 var text = document.createTextNode('NEVER EMPTY');
                 neverempty.appendChild(text);
                 var td = document.createElement("td");
                 var div = document.createElement("div"); // class=\"td block\" title=\"td-div\"");
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
                 //bind_all_sortables();
                //}
                //else
                //{
                //    console.log("Attempting to add");
                    //$(this).appendTo(ui.draggable[0]);
                //    $(this).prepend(ui.draggable[0]);
                    //$(ui.draggable[0]).appendTo($(this));
                //}
            }
        });

}

function replace_with(item, me)
{
    // Search me for select
    if(me.search("select") != -1)
    {
    // If select exists, then find CURRENT_SELECT_VALUE
        var index = me.search(CURRENT_SELECT_VALUE);
        if(index == -1)
            console.log("Did not find " + CURRENT_SELECT_VALUE);
        else
            console.log("Found " + CURRENT_SELECT_VALUE + " at index " + index);    
            index = index + CURRENT_SELECT_VALUE.length + 1;
            console.log("Attempt inserting select at new index "+index);
            a = me.substring(0,index);
            b = me.substring(index);
            me = a.concat("selected",b);
    }

    //console.log('function replace_with(item, me)');
    item.replaceWith(me); //'<div class="element element-txt">This text box has been added!</div>');
    //item.find('select').attr("value", CURRENT_SELECT_VALUE);


    //if(select != null){
    //    console.log("Attempting to select " + CURRENT_SELECT_VALUE);
    //    console.log("length = "+select.length);
    //    for(index = 0; index < select.length; index++) {
    //          console.log(select[index].value);  
    //       if(select[index].value == CURRENT_SELECT_VALUE)
    //          console.log("Found "+CURRENT_SELECT_VALUE+" at index " + index);
    //          select.selectedIndex = index;
    //    }
    // }

    resize_tables(item);

    bind_all_sortables();
}

function resize_tables(item)
{
    //console.log('function resize_tables(item)');
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
                //if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('css_table')) { replace_with(ui.item, "<table border=\"1\" width=\"100%\" height=\"50px\"><tr><td><div class=\"td block\" title=\"td-div\">XXXXXXXXXXXXXXXXXXXXXXXX</div></td></tr></table>"); }
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_table')) { replace_with(ui.item, "<table class=\"table\" border=\"2\"></table>"); }
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_gsf_choose_metadata')) { replace_with(ui.item, gsf_choose_metadata_element); }
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_gsf_metadata')) { replace_with(ui.item, gsf_metadata_element); }
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_gsf_link')) { replace_with(ui.item, gsf_link_element); }
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_gsf_switch')) { replace_with(ui.item, gsf_switch_element); }
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

    //$('.column').sortable({
    //    connectWith:['.column'],
    //    placeholder: 'placeholder',
    //    items:'td-div'
    //});


    //$('.column').sortable({
    //        'cursor':'pointer',
    //        'tolerance': 'pointer',
    //        'items':'.td-div',
    //        'placeholder':'placeholder',
    //        'connectWith':'.column'
            //'nested':'.gsf:metadata'
            //receive: function(event, ui) { alert("Attempted to receive"); },
            //stop: function(event, ui) {
            //    if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_gsf_metadata')) { replace_with(ui.item, gsf_metadata_element); }

    //});

    //console.log('function bind_td_sortable()');
    $('.td-div').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.gsf_metadata, .gsf_choose_metadata, .gsf_link, .gsf_switch',
            'placeholder':'placeholder',
            //'connectWith':'.td-div',
            //'nested':'.gsf:metadata'
            receive: function(event, ui) { alert("Attempted to receive"); },
            stop: function(event, ui) {
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_gsf_metadata')) { replace_with(ui.item, gsf_metadata_element); }
                
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
            'items':'.gsf_metadata, .gsf_default',
            'placeholder':'placeholder',
            'connectWith':'.gsf_choose_metadata',
            //'nested':'.gsf:metadata'
            stop: function(event, ui) {
                //alert("STOP");
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('draggable_gsf_metadata')) { 
                    console.log("Current select value = "+CURRENT_SELECT_VALUE);
                    var select = $(ui.item).find('select');
                    var value = select.attr("value");
                    console.log("We want this:"+value);

                    replace_with(ui.item, gsf_metadata_element); 
                    
                }
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
            'items':'.gsf_icon, .gsf_text',
            'placeholder':'placeholder',
            'connectWith':'.gsf_link',
            //'nested':'.gsf:metadata'
            stop: function(event, ui) {
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_gsf_icon')) { replace_with(ui.item, gsf_icon_element); }
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_text')) { replace_with(ui.item, "<input type=\"text\" name=\"rawtextinput\" size=\"10\"/>"); }
            }
    });
}

function bind_switch_sortable()
{
    //console.log('function bind_switch_sortable()');
    $('.gsf_switch').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.gsf_metadata, .gsf_when, .gsf_otherwise',
            'placeholder':'placeholder',
            'connectWith':'.gsf_switch',
            //'nested':'.gsf:metadata'
            stop: function(event, ui) {
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_gsf_when')) { replace_with(ui.item, gsf_when_element); }
                if (ui.item.hasClass("ui-draggable") && ui.item.hasClass('element_type_gsf_otherwise')) { replace_with(ui.item, gsf_otherwise_element); }
            }
    });
}

function bind_when_sortable()
{
    //console.log('function bind_when_sortable()');
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
    //console.log('function bind_otherwise_sortable()');
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
    //console.log('var toggleContent = function(e)');
    console.log('parent: ' + $(this).html());
    if ($(this).html() == '[+]'){ //targetContent.css('display') == 'none') {
        //$(this).parent().parent().parent().parent().parent().children(".block,.table").slideDown(300);
        //$(this).parents().children(".block,.table").stopPropagation().slideDown(300);
        //var x = $(this).parent().parent().parent().parent().parent();
        //var y = $(this).parent().parent().parent().parent().parent().children(".block,.table");
        //var z = $(this).closest(".block").children(".table, .block");
        $(this).closest(".block").children(".table, .block").slideDown(300);
        $(this).html('[-]');
        $(this).removeClass("ui-icon-plusthick");
        $(this).addClass("ui-icon-minusthick");
    } else {
        //$(this).parent().parent().parent().parent().parent().children(".block,.table").slideUp(300);
        //$(this).parents().children(".block,.table").slideUp(300);
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

