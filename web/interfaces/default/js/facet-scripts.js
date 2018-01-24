function performRefinedSearch()
{
	var allCheckBoxes = $("#facetSelector input");
	var counts = new Array();
	for(var i = 0; i < allCheckBoxes.length; i++)
	{
		var current = $(allCheckBoxes[i]);
		if(current.prop("checked"))
		{
			counts.push(current.parent().parent().attr("indexName") + ":(\"" + current.siblings("span").first().html() + "\")");
		}
	}

	var searchString = "";
	for(var key in gs.cgiParams)
	{
		if (gs.cgiParams.hasOwnProperty(key)) 
		{
			searchString += key.replace(/_/g, ".") + "=" + gs.cgiParams[key] + "&";
		}
	}
	
	var countsString = "s1.facetQueries=&";
	if(counts.length > 0)
	{
		countsString = "s1.facetQueries=[";
		var countsStringBuffer = "";
		for(var i = 0; i < counts.length; i++)
		{
		    // escape any apostrophes in facet query terms
		    // (ext/solr's Greenstone3SearchHandler does the other half of handling them)
		    countsStringBuffer += "\"" + encodeURI(counts[i]).replace(/'/g, "%2527") + "\"";
			if(i < counts.length - 1)
			{
				countsStringBuffer += ", ";
			}
		}
		
		countsString += encodeURI(countsStringBuffer) + "]&";
	}
	
	console.log("STRING IS " + countsString)
    
	$.ajax(gs.xsltParams.library_name + "/collection/" + gs.cgiParams.c + "/search/" + gs.cgiParams.s + "?" + searchString + countsString + "excerptid=resultsArea")
		.done(function(response)
		{
			$("#resultsArea").html("");
			$("#resultsArea").html(response.substring(response.indexOf(">") + 1, response.lastIndexOf("<"))); 
		});
}

function expandFacetList(indexName, countSize)
{
	var tables = $(".facetTable");
	
	for(var i = 0; i < tables.length; i++)
	{
		var current = $(tables[i]);
		if(current.attr("indexName") == indexName)
		{
			var items = current.children("li");
			
			for(var j = 0; j < items.length; j++)
			{
				$(items[j]).css("display", "block");
			}
			
			break;
		}
	}
	
    // the above code has made both see more and see less links display=block, so we need to hide
    // see more
    var morelink = $(".expandFacetList" + indexName);
    morelink.css("display", "none");

}

function collapseFacetList(indexName, countSize)
{
	var tables = $(".facetTable");
	
	for(var i = 0; i < tables.length; i++)
	{
		var current = $(tables[i]);
		if(current.attr("indexName") == indexName)
		{
			var items = current.children("li");
			
			for(var j = 0; j < items.length; j++)
			{
			    if(j > countSize) 
			    {
				$(items[j]).css("display", "none");
			    }
			}
		    
			break;
		}
	}
	
    // the above code has hidden both the see more and see less links.
    // display the see more one
    var morelink = $(".expandFacetList" + indexName);
    morelink.css("display", "block");

}

