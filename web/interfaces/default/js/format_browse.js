
console.log("Loading format_browse.js\n");

/* FUNCTIONS FOR FORMAT EDITING */                                                                    

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

    //var collection_name = getSubstring(myurl, "&c", "&");
    var service_name = getSubstring(myurl, "&s", "&");  

    if(thisOrAll == "all")
        service_name = "AllClassifierBrowse";

    var classifier_name = null;

    if(service_name == "ClassifierBrowse")
        classifier_name = getSubstring(myurl, "&cl", "&");

    //var post_url = "http://localhost:8383/greenstone3/dev?a=f&sa=update&c=" + collection_name +"&s=" + service_name;
    var post_url = host_info.pre_URL + "?a=f&sa=update&c=" + host_info.collection_name +"&s=" + service_name;

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

    //var collection_name = getSubstring(myurl, "&c", "&");
    var service_name = getSubstring(myurl, "&s", "&");
    var classifier_name = null;

    if(thisOrAll == "all")
        service_name = "AllClassifierBrowse";

    if(service_name == "ClassifierBrowse")
        classifier_name = getSubstring(myurl, "&cl", "&");

    //var post_url = "http://localhost:8383/greenstone3/dev?a=f&sa=save&c=" + collection_name +"&s=" + service_name;
    var post_url = host_info.pre_URL + "?a=f&sa=save&c=" + host_info.collection_name +"&s=" + service_name;

    if(classifier_name != null)
        post_url = post_url + "&cl=" + classifier_name;

    $.post(post_url, {data: formatStatement}, function(data) {
        // An error is returned because there is no valid XSLT for a format update action, there probably shouldn't be one so we ignore what the post returns.    
        console.log("Successfully saved");
        }, 'html');
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


