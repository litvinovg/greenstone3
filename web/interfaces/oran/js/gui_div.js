$(document).ready(function(){

    //function () {
        $('a.minmax').bind('click', toggleContent);
        $('a.remove').bind('click', removeContent);
        $('div.blockWrapper').sortable(
            {
                accept: 'block',
                helperclass: 'sortHelper',
                activeclass :   'sortableactive',
                hoverclass :    'sortablehover',
                handle: 'div.header',
                tolerance: 'pointer',
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
    //}
//};

     $(".block").mouseover(function() {
        //$(this).parents().animate({ backgroundColor: "white" }, 500);
        //$(this).animate({ backgroundColor: "yellow" }, 50);
        $(this).parents().css("border", "");
        $(this).css("border", "1px solid blue");
    }).mouseout(function(){
        //$(this).animate({ backgroundColor: "white" }, 500);
        $(this).css("border", "");
    });


    $("button").click(function () {
        //var parentTag = $(this).parent().get(0).titleName;
        //alert("Removing div " + parentTag);
        $(this).parent().parent().remove(); 
    });

}); 

var removeContent = (function () {
        //var parentTag = $(this).parent().get(0).titleName;
        //alert("Removing div " + parentTag);
        $(this).parent().parent().remove();
    });


var toggleContent = function(e)
{
    var targetContent = $('div.content', this.parentNode.parentNode);
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


