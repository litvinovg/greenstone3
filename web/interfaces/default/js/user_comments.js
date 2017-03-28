// http://toddmotto.com/avoiding-anonymous-javascript-functions
// https://toddmotto.com/everything-you-wanted-to-know-about-javascript-scope/
// pass event to addEventListener onmouseover
// http://stackoverflow.com/questions/256754/how-to-pass-arguments-to-addeventlistener-listener-function
// http://stackoverflow.com/questions/8714472/cant-pass-event-to-addeventlistener-closure-issue
// https://www.sitepoint.com/demystifying-javascript-closures-callbacks-iifes/

/***************
* USER COMMENTS
****************/
// http://stackoverflow.com/questions/6312993/javascript-seconds-to-time-with-format-hhmmss
// Call as: alert(timestamp.printTime());
function formatTime(timestamp) {
    var int_timestamp    = parseInt(timestamp, 10); // don't forget the second param
    var date = new Date(int_timestamp);
    return date.toLocaleDateString() + " " + date.toLocaleTimeString();   
}

function loadUserComments() {

    // don't bother loading comments if we're not on a document page (in which case there's no docid)
    var doc_id = gs.variables["d"]; ///"_cgiargdJssafe_"; //escape("cgiargd");

    // stackoverflow.com/questions/36661748/what-is-the-exact-negation-of-ifvariable-in-javascript
    if(!doc_id) { // vs http://stackoverflow.com/questions/784929/what-is-the-not-not-operator-in-javascript
	
	return;
    }

    // Don't allow the user to add comments (which calls set-meta-array) until we've finished loading all 
    // user comments (which calls get-meta-array). Since get-meta-array and set-meta-array are called 
    // asynchronously, this is to help prevent any overlap in these functions' access of meta files in
    // index|archives|import.
    var submitButton = document.getElementById("usercommentSubmitButton");    
    if(submitButton) { // there'll be no submitButton if the comment form is not displayed (user not logged in)
	submitButton.disabled = true;
    }

    
    // don't allow users to add comments (disable the submit button)
    // until existing comments have been retrieved and displayed
    //document.getElementById("usercommentSubmitButton").disabled = true;

    // Since we have a docid, get toplevel section of the docid
    
    var period = doc_id.indexOf(".");
    if(period != -1) {
	doc_id = doc_id.substring(0, period);
    }
    
    var username_rec = {
	metaname: "username",
	metapos: "all"
    };
    
    var timestamp_rec = {
	metaname: "usertimestamp",
	metapos: "all"
    };
    
    var comment_rec = {
	metaname: "usercomment",
	metapos: "all"
    };

    var doc_rec = {
	docid: doc_id,
	metatable: [username_rec, timestamp_rec, comment_rec]	    
    };

    var docArray = [doc_rec];
    //alert(JSON.stringify(docArray));

    //var json_result_str = gs.functions.getMetadataArray(gs.variables["c"], gs.variables["site"], docArray, "index");

    gs.functions.getMetadataArray(gs.variables["c"], gs.variables["site"], docArray, "index", loadedUserComments, false); // false for asynchronous
}

function loadedUserComments(xmlHttpObj)
{
    // don't bother displaying comments if we're not on a document page
    // (in which case there's no usercommentdiv).
    // This shouldn't happen here, since we only call this from loadUserComments()
    // and that first checks we're actually on a document page.
    var usercommentdiv = document.getElementById("usercomments");
    if(usercommentdiv == undefined || usercommentdiv == null) {
	return;
    }

    var json_result_str = xmlHttpObj.responseText;
    //	alert(json_result_str);
    console.log("Got to display: " + json_result_str);
    var result = JSON.parse(json_result_str);
    // result contains only one docrec (result[0]), since we asked for the usercomments of one docid
    var metatable = result[0].metatable;
    //	alert(JSON.stringify(metatable));
    
    var i = 0;
    var looping = true;
    
    
    // if there's at least one existing comment OR if the form is currently being displayed
    // (regardless of whether previous comments exist), display a heading for the comments section
    if(metatable[0].metavals[0] != undefined || document.getElementById("usercommentform") != undefined) {
	var heading=document.createElement("div");
	var attr=document.createAttribute("class");
	attr.nodeValue="usercommentheading";
	heading.setAttributeNode(attr);
	var txt=document.createTextNode(gs.variables["textusercommentssection"]); ///"_textusercommentssection_");
	heading.appendChild(txt);
	usercommentdiv.appendChild(heading);
    }
    
    
    // metatable[0] = list of usernames, metatable[1] = list of timestamps, metatable[2] = list of comments	
    // the 3 lists/arrays should be of even length. Assuming this, loop as long as there's another username
    while(looping) {
	var metaval_rec = metatable[0].metavals[i];
	if(metaval_rec == undefined) {
	    looping = false;
	} 
	else {
	    
    	    var username = metaval_rec.metavalue;
	    var timestamp = metatable[1].metavals[i].metavalue;  
	    var comment = metatable[2].metavals[i].metavalue; 
	    
	    //alert("Comment: " + username + " " + timestamp + " " + comment);
	    
	    // No need to sort by time, as the meta are already stored sorted 
	    // and hence retrieved in the right order by using the i (metapos) counter
	    // If sorting the array of comment records, which would be by timestamp, see
	    // https://developer.mozilla.org/en-US/docs/JavaScript/Reference/Global_Objects/Array/sort
	    
	    // for each usercomment, create a child div with the username, timestamp and comment
	    displayInUserCommentList(usercommentdiv, username, timestamp, comment);
	    
	    i++;
	}		
    }
    
    var submitButton = document.getElementById("usercommentSubmitButton");
    // Now we've finished loading all user comments, allow the user to add a comment
    if(submitButton) {
	submitButton.disabled = false;
    }
}


function displayInUserCommentList(usercommentdiv, username, timestamp, comment) {
    
    //alert("Comment: " + username + " " + timestamp + " " + comment);

    var divgroup=document.createElement("div");
    var attr=document.createAttribute("class");
    attr.nodeValue="usercomment";
    divgroup.setAttributeNode(attr);
    
    var divuser=document.createElement("div");
    var divtime=document.createElement("div");
    var divcomment=document.createElement("div");
    
    
    divgroup.appendChild(divuser);
    var txt=document.createTextNode(username);
    divuser.appendChild(txt);
    
    divgroup.appendChild(divtime);
    txt=document.createTextNode(formatTime(timestamp)); // format timestamp for date/time display
    divtime.appendChild(txt);
    
    // any quotes and colons in the fields would have been protected for transmitting as JSON
    // so decode their entity values
    comment = comment.replace(/&quot;/gmi, '"');
    comment = comment.replace(/&58;/gmi, ':');
    
    divgroup.appendChild(divcomment);
    txt=document.createTextNode(comment);
    divcomment.appendChild(txt);
    
    usercommentdiv.appendChild(divgroup);
    
}


// Unused. Replaced in favour of call to escape() in setMetaArray function that calls urlPostSync
// http://stackoverflow.com/questions/6020714/escape-html-using-jquery
function safeHTML(str) {
    return str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace('"',"&quot;").replace("'","&#x27;").replace("/", "&#x2F;"); //"\\""
} 


function addUserComment(_username, _comment, _docid, doc) {
    
    // don't add empty strings for name/comment		
    
    // http://stackoverflow.com/questions/498970/how-do-i-trim-a-string-in-javascript
    //var trimmed_username=_username.replace(/^\s+|\s+$/g, '');
    var trimmed_comment = _comment.replace(/^\s+|\s+$/g, '');
    
    if(!trimmed_comment) { // || !trimmed_username
	doc.AddUserCommentForm.comment.value = "";		      
	//document.AddUserCommentForm.username.value = "";
	document.getElementById("usercommentfeedback").innerHTML = gs.variables["textisempty"]; ///"_textisempty_";
	return;
    }
    
    // Need to the add user comment meta of username, timestamp and comment to the
    // topmost section of the document. So only get the docId up to any period mark:
    var period = _docid.indexOf(".");
    if(period != -1) {
	_docid = _docid.substring(0, period);
    }
    
    
    // Want to store username, timestamp and comment in import/metadata.xml, archives/doc.xml
    // and index/col.gdb.
    
    // For getting the current time, see
    // http://stackoverflow.com/questions/3830244/get-current-date-time-in-seconds
    var _timestamp = new Date().getTime(); // div by 1000 to get seconds. valueOf() may return string
    
    //alert("username:" + _username
    //+ "\\ncomment: " + _comment
    //+ "\\ncollection: " + collection 
    //+ "\\ndocid: " + _docid
    //+ "\\ntimestamp: " + _timestamp); 
    
    
    // Entity encode the values before storing (at least <, >, forward slash.
    // And single and double quote, ampersand)
    // http://stackoverflow.com/questions/6020714/escape-html-using-jquery
    // setMetadataArray escapes the entire JSON, is that better than escaping individually here?
    //_docid = escape(_docid);
    //_timestamp = escape(_timestamp);
    //_username = escape(_username); //safeHTML(_username);
    //_comment = escape(_comment); //safeHTML(_comment);
    
    // Use this if making individual api calls to set username meta, then timestamp then comment meta
    // GSAPI already knows the collection
    //gsapi.setMetadata(_docid, "username", null, _username, "accumulate", "import|archives|index");
    //gsapi.setMetadata(_docid, "usertimestamp", null, _timestamp, "accumulate", "import|archives|index");
    //gsapi.setMetadata(_docid, "usercomment", null, _comment, "accumulate", "import|archives|index");
    
    
    // Use the new JSON metatable format to set username, timestamp and comment meta for docid in one go
    
    // For creating the JSON object that gets turned into a string, see
    // http://msdn.microsoft.com/en-us/library/ie/cc836459%28v=vs.94%29.aspx
    // http://jsfiddle.net/qmacro/W54hy/
    
    var username_rec = {
	metaname: "username",
	metavals: [_username]
    };
    
    var timestamp_rec = {
	metaname: "usertimestamp",
	metavals: [_timestamp]
    };
    
    var comment_rec = {
	metaname: "usercomment",
	metavals: [_comment]
    };
    
    var doc_rec = {
	docid: _docid,
	metatable: [username_rec, timestamp_rec, comment_rec],
	metamode: "accumulate"
    };
    
    var docArray = [doc_rec];
    
    //alert(JSON.stringify(docArray));
    
    // Don't allow the user to submit further comments until the metadata has been updated
    document.getElementById("usercommentSubmitButton").disabled = true;

    
    //var result = gs.functions.setMetadataArray(gs.variables["c"], gs.variables["site"], docArray, "accumulate", "import|archives|index");
    gs.functions.setMetadataArray(gs.variables["c"], gs.variables["site"], docArray, "accumulate", "import|archives|index", function(xmlHttpObj) { return doneUpdatingMetatada(xmlHttpObj, _username, _timestamp, _comment); }, false); // false for asynchronous, 
    // this is ok since we're disabling the comment submit button, so no further set-meta-array calls can be
    // made until the ajax call returns and the callback is called which re-enables the submit button
    // But disabling submit does not protect against concurrent access such as someone else editing the 
    // document (doing set-meta operations, updating archives/index/import) at the same time as someone else
    // adding user comments (doing set-meta-array, updating archives|index|import).

}

function doneUpdatingMetatada(xmlHttpObj, _username, _timestamp, _comment)
{
    var result = xmlHttpObj.responseText;
    //alert("Received post response to setMeta: " + result); // just the HTML page

    // clear the comment field as it has now been submitted, but not the username field
    // as the user is logged in, so they should be able to commit again under their username.
    document.AddUserCommentForm.comment.value = ""; 
    
    // check for locked collection error
    var errorIndex = result.indexOf("ERROR");
    // check for any error discovered on the server side
    var responseErrorIndex = result.indexOf("<error");

    if(errorIndex != -1) {
	var endIndex = result.indexOf("\\n");
	var error = result.substring(errorIndex,endIndex);
	errormessage="ERROR: Unable to add comment. " + error;
	document.getElementById("usercommentfeedback").innerHTML = errormessage;
	//alert("Result: " + result);
    } 
    else if (responseErrorIndex != -1) {
	var endIndex = result.indexOf("</error>");
	var startIndex = result.indexOf(">", responseErrorIndex+1);
	var error = result.substring(startIndex+1,endIndex);
	errormessage="ERROR: Unable to add comment. " + error;
	document.getElementById("usercommentfeedback").innerHTML = errormessage;
	//alert("Result: " + result);
    }
    else { // success!
	document.getElementById("usercommentfeedback").innerHTML = gs.variables["textcommentsubmitted"]; ///"_textcommentsubmitted_";		
	
	// update display of existing user comments to show the newly added comment
	var usercommentdiv = document.getElementById("usercomments");
	if(usercommentdiv != undefined) {
	    displayInUserCommentList(usercommentdiv, _username, _timestamp, _comment);
     	}
    }

    // whether there was an error or not, re-enable the submit button now
    // that the set-meta-array operation has completed.
    document.getElementById("usercommentSubmitButton").disabled = false;
}

function commentAreaSetup() {
    loadUserComments();

    //$("div#commentarea").html("<textarea required=\"required\" name=\"comment\" rows=\"10\" cols=\"64\" placeholder=\"Add your comment here...\"></textarea>");
    $("div#commentarea").html('<textarea required="required" name="comment" rows="10" cols="64" placeholder="Add your comment here..."></textarea>');

}


// "Handlers added via $(document).ready() don't overwrite each other, but rather execute in turn"
// as explained at http://stackoverflow.com/questions/15564029/adding-to-window-onload-event
// This way we ensure we don't replace any other onLoad() functions, but append the loadUserComments() 
// function to the existing set of eventhandlers called onDocReady
$(document).ready(commentAreaSetup);

