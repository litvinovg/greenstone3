"use strict";



var foo_count = 0;

function gitDebug(key,val,args) {
    console.log(key+"="+val+" ("+args+")");
    foo_count++;
    return val;
}


// Port of methods from GSXLUtil.java and Dictionary.java to support client side XSLT

// Function for inserting args into retrieved interface string
function getInterfaceTextSubstituteArgs(initial,argsStr)
{
    var args = argsStr.split(";");

    var complete = ""; //new StringBuffer();

    // While we still have initial string left.
    while ((initial.length > 0) && (initial.indexOf('{') != -1) && (initial.indexOf('}') != -1))
    {
	// Remove preamble
	var opening = initial.indexOf('{');
	var closing = initial.indexOf('}');
	var comment_mark = initial.indexOf('-', opening); // May not exist
	if (comment_mark > closing)
	{ // May also be detecting a later comment
	    comment_mark = -1;
	}
	complete += initial.substring(0, opening);
	
	// Parse arg_num
	var arg_str = null;
	if (comment_mark != -1)
	{
	    arg_str = initial.substring(opening + 1, comment_mark);
	}
	else
	{
	    arg_str = initial.substring(opening + 1, closing);
	}
	if (closing + 1 < initial.length)
	{
	    initial = initial.substring(closing + 1);
	}
	else
	{
	    initial = "";
	}
	
	var arg_num = Number(arg_str);
	// Insert argument
	if ((args != null) && (0 <= arg_num) && (arg_num < args.length))
	{
	    complete += args[arg_num];
	}
    }
    
    complete += initial;

    return complete;
}

function getNumberedItem(list, number)
{
    var items = list.split(","); 
    
    if (items.length > number)
    {
	return items[number];
    }

    return ""; // index out of bounds
}


//alert(getInterfaceTextSubstituteArgs("test {0} test {1} test" ,"1;2"));
//alert(getNumberedItem("item0,item1,item2" ,1));
