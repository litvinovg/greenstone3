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

    /*    
    $('div.blockWrapper').NestedSortable(
    {
        accept: 'block',
        helperclass: 'sortHelper',
        activeclass :   'sortableactive',
        hoverclass :    'sortablehover',

        // Restricts sort start click to the specified element.
        handle: 'div.header',
        onStart : function()
            {
                $.iAutoscroller.start(this, document.getElementsByTagName('body'));
            },
            onStop : function()
            {
                $.iAutoscroller.stop();
            }
    }
    );
    */
 
     
    $('#formatStatement').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.gsf\\:template',
            'placeholder':'placeholder',
            //'nested':'div'
    });
    
    $('.gsf\\:template').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.table, .gsf\\:choose-metadata, .gsf\\:metadata',
            'placeholder':'placeholder'
            //'nested':'.gsf:metadata'
    });

    $('.table').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.leaf .gsf\\:choose-metadata, .gsf\\:link, .gsf\\:switch',
            'placeholder':'placeholder'
            //'nested':'.gsf:metadata'
    });
 
    $('.gsf\\:choose-metadata').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.gsf\\:metadata',
            'placeholder':'placeholder',
            'connectWith':'.gsf\\:choose-metadata'
            //'nested':'.gsf:metadata'
    });
    
    $('.gsf\\:link').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.gsf\\:icon',
            'placeholder':'placeholder'
            //'nested':'.gsf:metadata'
    });

    $('.gsf\\:switch').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.gsf\\:metadata, .gsf\\:when, .gsf\\otherwise',
            'placeholder':'placeholder'
            //'nested':'.gsf:metadata'
    });

    $('.gsf\\:when').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.leaf',
            'placeholder':'placeholder'
            //'nested':'.gsf:metadata'
    });

    $('.gsf\\:otherwise').sortable({
            'cursor':'pointer',
            'tolerance': 'pointer',
            'items':'.leaf',
            'placeholder':'placeholder'
            //'nested':'.gsf:metadata'
    });

    /* 
    $('div.blockWrapper').sortable(
        {
            accept: 'block',
            helperclass: 'sortHelper',
            activeclass :   'sortableactive',
            hoverclass :    'sortablehover',

            // Restricts sort start click to the specified element.
            handle: 'div.header',

            // This is the way the reordering behaves during drag. Possible values: 'intersect', 'pointer'. In some setups, 'pointer' is more natural.
            // * intersect: draggable overlaps the droppable at least 50%
            // * pointer: mouse pointer overlaps the droppable
            tolerance: 'pointer',
            //containment: 'parent',
            nested: 'div',
            onChange : function(ser)
            {
            },
            onStart : function()
            {
                $.iAutoscroller.start(this, document.getElementsByTagName('body'));
            },
            onStop : function()
            {
                $.iAutoscroller.stop();
            }
        }
    );
    */

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


