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

var xsltUtil_stringVariables = {};

//public static void storeString(String name, String value)
function storeString(name,value)
{
    xsltUtil_stringVariables[name] = value;
}

//public static String getString(String name)
function getString(name)
{
    return xsltUtil_stringVariables[name];
}


//public static String escapeNewLines(String str)
function escapeNewLines(str)
{
    if (str == null || str.length < 1)
    {
	return null;
    }
    return str.replace("\n", "\\\n");
}

//public static String escapeQuotes(String str)
function escapeQuotes(str)
{
    if (str == null || str.length < 1)
    {
	return null;
    }
    return str.replace("\"", "\\\"");
}

//public static String escapeNewLinesAndQuotes(String str)
function escapeNewLinesAndQuotes(str)
{
    if (str == null || str.length < 1)
    {
	return null;
    }
    return escapeNewLines(escapeQuotes(str));
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




//public static boolean oidIsMatchOrParent(String first, String second)
function oidIsMatchOrParent(first, second)
{
    if (first == second)
    {
	return true;
    }

    var firstParts = first.split(".");
    var secondParts = second.split(".");

    if (firstParts.length >= secondParts.length)
    {
	return false;
    }
    
    for (var i = 0; i < firstParts.length; i++)
    {
	if (!firstParts[i].equals(secondParts[i]))
	{
	    return false;
	}
    }
    
    return true;
}

//public static String oidDocumentRoot(String oid)
function oidDocumentRoot(oid)
{
    var oidParts = oid.split("\\.");
    
    return oidParts[0];
}



//public static String hashToSectionId(String hashString)
function hashToSectionId(hashString)
{
    if (hashString == null || hashString.length == 0)
    {
	return "";
    }
    
    var firstDotIndex = hashString.indexOf(".");
    if (firstDotIndex == -1)
    {
	return "";
    }
    
    var sectionString = hashString.substring(firstDotIndex + 1);
    
    return sectionString;
}

//public static String hashToDepthClass(hashString)
function hashToDepthClass(hashString)
{
    if (hashString == null || hashString.length == 0)
    {
	return "";
    }
    
    var sectionString = hashToSectionId(hashString);
    
    var count = sectionString.split("\\.").length;

    if (sectionString == "")
    {
	return "sectionHeaderDepthTitle";
    }
    else
    {
	return "sectionHeaderDepth" + count;
    }
}

//alert("hashToDepthClass(\"HASH134B.1\")=" + hashToDepthClass("HASH134B.1"));
//alert("hashToSectionId(\"HASH134B.1\")=" + hashToSectionId("HASH134B.1"));

//alert(getInterfaceTextSubstituteArgs("test {0} test {1} test" ,"1;2"));
//alert(getNumberedItem("item0,item1,item2" ,1));
