
console.log("Loading format_document.js\n");

$(document).ready(function(){

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
        });

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

    //var collection_name = getSubstring(myurl, "&c", "&");
    var document_id = getSubstring(myurl, "&d", "&");
    var document_type = getSubstring(myurl, "&dt", "&");
    var prev_action = getSubstring(myurl, "&p.a", "&");
    var prev_service = getSubstring(myurl, "&p.s", "&");

    //var post_url = "http://localhost:8383/greenstone3/dev?a=f&sa=saveDocument&c=" + collection_name + "&d=" + document_id + "&dt=" + document_type + "&p.a=" + prev_action + "&p.s=" + prev_service;
    var post_url = host_info.pre_URL + "?a=f&sa=saveDocument&c=" + host_info.collection_name + "&d=" + document_id + "&dt=" + document_type + "&p.a=" + prev_action + "&p.s=" + prev_service;

    // XML will be automatically wrapped in <display><format> tags when saved to collection config
    var xml = '<format><gsf:option name="TOC" value="'+$('input[name="TOC"]').attr('checked')+'"/><gsf:option name="coverImage" value="'+$('input[name="bookCover"]').attr('checked')+'"/></format>';

    $.post(post_url, {data: xml}, function(data) {
            console.log("Success, we have received data");
    }, 'xml');
}

