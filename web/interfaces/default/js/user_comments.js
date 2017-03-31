// http://toddmotto.com/avoiding-anonymous-javascript-functions
// https://toddmotto.com/everything-you-wanted-to-know-about-javascript-scope/
// pass event to addEventListener onmouseover
// http://stackoverflow.com/questions/256754/how-to-pass-arguments-to-addeventlistener-listener-function
// http://stackoverflow.com/questions/8714472/cant-pass-event-to-addeventlistener-closure-issue
// https://www.sitepoint.com/demystifying-javascript-closures-callbacks-iifes/
// http://stackoverflow.com/questions/4869712/new-without-delete-on-same-variable-in-javascript
// http://stackoverflow.com/questions/7375120/why-is-arr-faster-than-arr-new-array
// http://stackoverflow.com/questions/874205/what-is-the-difference-between-an-array-and-an-object
// http://stackoverflow.com/questions/33514915/what-s-the-difference-between-and-while-declaring-a-javascript-array
// http://www.nfriedly.com/techblog/2009/06/advanced-javascript-objects-arrays-and-array-like-objects/
// stackoverflow.com/questions/36661748/what-is-the-exact-negation-of-ifvariable-in-javascript
// http://stackoverflow.com/questions/784929/what-is-the-not-not-operator-in-javascript

/***************
* USER COMMENTS
****************/

// avoid making usercomments js functions global (which attaches them as properties to the window object)
gs.usercomments = {};

// http://stackoverflow.com/questions/6312993/javascript-seconds-to-time-with-format-hhmmss
// Call as: alert(timestamp.printTime());
gs.usercomments.formatTime = function(timestamp) {
    var int_timestamp    = parseInt(timestamp, 10); // don't forget the second param
    var date = new Date(int_timestamp);
    return date.toLocaleDateString() + " " + date.toLocaleTimeString();   
}

gs.usercomments.loadUserComments = function() {

    // don't bother loading comments if we're not on a document page (in which case there's no docid)
    var doc_id = gs.variables["d"]; ///"_cgiargdJssafe_" in GS2

    if(!doc_id) {	
	return;
    }

    // Don't allow the user to add comments (which calls set-meta-array) until we've finished loading all 
    // user comments (which calls get-meta-array). Since get-meta-array and set-meta-array are called 
    // asynchronously, this is to help prevent any overlap in these functions' access of meta files in
    // index|archives|import.
    // Prevent users from adding comments by disabling the submit button
    // until existing comments have been retrieved and displayed
    var submitButton = document.getElementById("usercommentSubmitButton");    
    if(submitButton) { // there'll be no submitButton if the comment form is not displayed (user not logged in)
	submitButton.disabled = true;
    }

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

    //var json_result_str = gs.functions.getMetadataArray(gs.variables["c"], gs.variables["site"], docArray, "index");

    gs.functions.getMetadataArray(gs.variables["c"], gs.variables["site"], docArray, "index", gs.usercomments.loadedUserComments, false); // false for asynchronous
}

gs.usercomments.loadedUserComments = function(data)
{
    // don't bother displaying comments if we're not on a document page
    // (in which case there's no usercommentdiv).
    // This shouldn't happen here, since we only call this from loadUserComments()
    // and that first checks we're actually on a document page.
    var usercommentdiv = document.getElementById("usercomments");
    if(usercommentdiv == undefined || usercommentdiv == null) {
	return;
    }

    // data is xmlHttpRequest Object if gsajaxapi is used for the ajax call.
    // And data is a string if jQuery AJAX was used.
    // Using JavaScript's feature sensing to detect which of the two we're dealing with:
    var json_result_str = (data.responseText) ? data.responseText : data; 

    //console.log("Got user comments to display: " + json_result_str);

    var result = JSON.parse(json_result_str);
    // result contains only one docrec (result[0]), since we asked for the usercomments of one docid
    var metatable = result[0].metatable;
    
    var i = 0;
    var looping = true;
    
    
    // if there's at least one existing comment OR if the form is currently being displayed
    // (regardless of whether previous comments exist), display a heading for the comments section
    if(metatable[0].metavals[0] != undefined || document.getElementById("usercommentform") != undefined) {
	var heading=document.createElement("div");
	var attr=document.createAttribute("class");
	attr.nodeValue="usercommentheading";
	heading.setAttributeNode(attr);
	var txt=document.createTextNode(gs.variables["textusercommentssection"]); ///"_textusercommentssection_" in GS2
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
	    
	    // No need to sort by time, as the meta are already stored sorted 
	    // and hence retrieved in the right order by using the i (metapos) counter
	    // If sorting the array of comment records, which would be by timestamp, see
	    // https://developer.mozilla.org/en-US/docs/JavaScript/Reference/Global_Objects/Array/sort
	    
	    // for each usercomment, create a child div with the username, timestamp and comment
	    gs.usercomments.displayInUserCommentList(usercommentdiv, username, timestamp, comment);
	    
	    i++;
	}		
    }
    
    var submitButton = document.getElementById("usercommentSubmitButton");
    // Now we've finished loading all user comments,
    // allow the user to add a comment by enabling the submit button again
    if(submitButton) {
	submitButton.disabled = false;
    }
}


gs.usercomments.displayInUserCommentList = function(usercommentdiv, username, timestamp, comment) {
    
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
    txt=document.createTextNode(gs.usercomments.formatTime(timestamp)); // format timestamp for date/time display
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

gs.usercomments.addUserComment = function(_username, _comment, _docid, doc) {
    
    // don't add empty strings for name/comment		
    
    // http://stackoverflow.com/questions/498970/how-do-i-trim-a-string-in-javascript
    //var trimmed_username=_username.replace(/^\s+|\s+$/g, '');
    var trimmed_comment = _comment.replace(/^\s+|\s+$/g, '');
    
    if(!trimmed_comment) {
	doc.AddUserCommentForm.comment.value = "";		      
	//document.AddUserCommentForm.username.value = "";
	document.getElementById("usercommentfeedback").innerHTML = gs.variables["textisempty"]; ///"_textisempty_" in GS2
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
    
    // setMetadataArray escapes the entire JSON, so we don't individually escape the fields here
    // If we did, and called gsapi.setMetadata or gs.functions (in javascript-global-functions.js) instead,
    // then resort to escaping first, e.g.
    // _comment = escape(_comment);
    // gsapi.setMetadata(_docid, "usercomment", null, _comment, "accumulate", "import|archives|index");
    
    
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
    
    // Don't allow the user to submit further comments until the metadata has been updated
    document.getElementById("usercommentSubmitButton").disabled = true;

    
    //var result = gs.functions.setMetadataArray(gs.variables["c"], gs.variables["site"], docArray, "accumulate", "import|archives|index");
    gs.functions.setMetadataArray(gs.variables["c"],
	gs.variables["site"],
	docArray, "accumulate",
	"import|archives|index",
	function(ajaxResult) { return gs.usercomments.doneUpdatingMetatada(ajaxResult, _username, _timestamp, _comment); },
	false); // false for asynchronous, 
    // async here is ok since we're disabling the comment submit button, so no further set-meta-array calls
    // can be made until the ajax call returns and the callback is called which re-enables the submit button
    // But disabling submit does not protect against concurrent access such as someone else editing the 
    // document (doing set-meta operations, updating archives/index/import) at the same time as someone else
    // adding user comments (doing set-meta-array, updating archives|index|import).

}

gs.usercomments.doneUpdatingMetatada = function(data, _username, _timestamp, _comment)
{
    // data is xmlHttpRequest Object if gsajaxapi is used for the ajax call.
    // And data is a string if jQuery AJAX was used.
    // Using JavaScript's feature sensing to detect which of the two we're dealing with:
    var result = (data.responseText) ? data.responseText : data;        

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
    } 
    else if (responseErrorIndex != -1) {
	var endIndex = result.indexOf("</error>");
	var startIndex = result.indexOf(">", responseErrorIndex+1);
	var error = result.substring(startIndex+1,endIndex);
	errormessage="ERROR: Unable to add comment. " + error;
	document.getElementById("usercommentfeedback").innerHTML = errormessage;
    }
    else { // success!
	document.getElementById("usercommentfeedback").innerHTML = gs.variables["textcommentsubmitted"]; ///"_textcommentsubmitted_" in GS2
	
	// update display of existing user comments to show the newly added comment
	var usercommentdiv = document.getElementById("usercomments");
	if(usercommentdiv != undefined) {
	    gs.usercomments.displayInUserCommentList(usercommentdiv, _username, _timestamp, _comment);
     	}
    }

    // whether there was an error or not, re-enable the submit button now
    // that the set-meta-array operation has completed.
    document.getElementById("usercommentSubmitButton").disabled = false;
}

gs.usercomments.commentAreaSetup = function() {
    gs.usercomments.loadUserComments();

    $("div#commentarea").html('<textarea required="required" name="comment" rows="10" cols="64" placeholder="Add your comment here..."></textarea>');

}


// "Handlers added via $(document).ready() don't overwrite each other, but rather execute in turn"
// as explained at http://stackoverflow.com/questions/15564029/adding-to-window-onload-event
// This way we ensure we don't replace any other onLoad() functions, but append the loadUserComments() 
// function to the existing set of eventhandlers called onDocReady
$(document).ready(gs.usercomments.commentAreaSetup);

