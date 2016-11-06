//hierarchy storage object

//Find first ancestor element by tag name
function findAncestorByTagName (element, tagName) {
    while ((element.tagName != tagName) && (element = element.parentElement));
    return element;
}
// Function to set Id as TEXTAREA value
function chooseHierarchyOption(a)
{
	
	var metaValue = a.getAttribute("metavalue");
	var metaTitle = a.getAttribute("metatitle");
	
// If ID defined and not null
	if (metaValue && (metaValue != null)) 
	{
		//find TR Ancestor to get TEXTAREA
		var tr = findAncestorByTagName(a,"TR");
		//Set value to id of clicked element
		$(tr.getElementsByTagName("TEXTAREA")).val(metaValue);
		//	Set button name
		setHierarchyButtonText(tr, metaTitle);
		removeSuggestionsMenu(tr);
		//Hide menu after click
		$(tr).find(".metaDataHierarchyMenu").find("ul li ul li").hide();
		//Set focus on textarea
		tr.getElementsByTagName("TEXTAREA")[0].focus();
		
		createSuggestionsMenu(tr);
		
	}
}
function openHierarchyMenuLevel(menuItem)
{
		   var tr = findAncestorByTagName(menuItem,"TR");
		   //get current MetaDataName
		   var metaName = getMetaName(tr);
		   //Get current hierarchy from storages
		   var hierarchyData = hierarchyStorage[metaName];
		   menuItem = $(menuItem);
           if (menuItem.find('ul').length == 0) 
           {
           		 //Expression to extract hierarchy identifier from menu item id
           		 var getIdExp = /[0-9.]+/;
           		 //Extracted hierarchy identifier
           		 var id;
           		 //Expression to get childs
           		 var childExpr;
           		 if (menuItem.attr('id'))
           		 {
           		 	id = menuItem.attr('id').match(getIdExp);
			     }
                if (id == null)
                {
                	childExpr = /^[0-9]+$/;
                }
                else 
                {
						childExpr = new RegExp("^" + id + "." + "[0-9]+$");                	
                }
                var levelItems = {};
                for(var key in hierarchyData) 
                {
   					if(childExpr.test(key)){
   						levelItems[key]='<li class="hierarchyOption" id="'+key+'" ><button onclick="chooseHierarchyOption(this)"  metavalue="'+ hierarchyData[key][0] +'" metatitle="'+ hierarchyData[key][1] +'">' + hierarchyData[key][1] + '</button></li>';
   					}
   				 }
                 //If no elements in hierarchy level
   				 if (jQuery.isEmptyObject(levelItems))
   				 {
   					 //add empty menu. Invisible. Used by checks in setHierarchyEventsWrappers focusout to prevent menu hiding while choosing suggestion menu item leaf
   					menuItem.append("<ul></ul>");
   				 	
   				 }
   				 else {
   				 //wrap elements in hierarchy level
						var levelMenu = '<ul>';
						for(var key in levelItems)
						{
							//Fill menu with items
							levelMenu += levelItems[key];
						}
						levelMenu += "</ul>";
						menuItem.append(levelMenu);
						menuItem.find("li").hover(
	    					function(){openHierarchyMenuLevel(this);}
	    					,
      	  					function(){closeHierarchyMenuLevel(this);}
        				);
						
						//menuItem.find('ul');
						menuItem.children('ul').slideDown();
   				 }
   				 
            } else {
            	//stop animation
                menuItem.find('ul').stop(true, true);
                //show menu items
                menuItem.children('ul').children('li').show();
                //slide down menu
                menuItem.children('ul').slideDown();
            }            
            menuItem.addClass("active");
}
function closeHierarchyMenuLevel(menuItem)
{
	$(menuItem).removeClass("active");
	$(menuItem).find('ul').hide();
}
// Download hierarchy file and process it
function downloadAndProcessHierarchyFile(hierarchyFileName,metaName)
{

	  var xmlhttp=new XMLHttpRequest();
	  xmlhttp.open("GET",hierarchyFileName,true);
	  xmlhttp.send();
	  xmlhttp.onreadystatechange=function()   
	  {
		  if (xmlhttp.readyState==4 && xmlhttp.status==200)
		  {
			var hierarchyFile = xmlhttp.responseText;
			var StringData = [];
			var hierarchyData = {};
			var expr = /^(\S*|\"[^\"]*\")\ +([0-9]+(?:\.[0-9]+)*)\ +(.*)/m;
			StringData = hierarchyFile.split('\n');
			for (var i = 0; i < StringData.length; i++) {
				var result = StringData[i].match(expr);
				// If result not null
				if (result != null && result.length == 4) {
					// populate hierarchy object
					hierarchyData[result[2]] = [ result[1].replace(/^\"|\"$/g, ''), result[3].replace(/^\"|\"$/g, '') ];
				}

			}
			addHierarchyToStorage(metaName, hierarchyData);
			setHierarchyEventsWrappers(metaName);
		  }
	  }

}


function setHierarchyHoverEvent(father,className)
{
	 
	$(father).find(className).hover(function() 
	{
		openHierarchyMenuLevel(this);
	}, function() {
		closeHierarchyMenuLevel(this);
	});
	
}
function createHierarchyMenuButton(row)
{
		//get current MetaDataName
		var metaName = getMetaName(row);
                defaultHierarchyButtonText = gs.text.de.top_level_menu; //'Top level menu';
		var hierarchyButtonText = defaultHierarchyButtonText;
		// Check if textarea already contain right menu key
		var textAreaValue = $(row).find('TEXTAREA').val();
		
		//Get current hierarchy from storages
		var hierarchyData = hierarchyStorage[metaName];
		//TODO Modificate
		
		
		for(var key in hierarchyData)
		{
			if (hierarchyData[key][0] == textAreaValue)
				{
					hierarchyButtonText = hierarchyData[key][1];
					break;
				}
		}
		
		//Menu element
		var mainmenu = '<td class="metaDataHierarchyMenu" style="display: none;"><ul><li id="hierarchyLevel"><button class="hierarchyMenuButton"  title="Menu">' + hierarchyButtonText + '</button></li></ul></td>'
		//Insert hierarchy menu
		$(row).find('.metaTableCellRemove').after(mainmenu);
		//Set hover event on hierarchy menu
		$(row).each(function(){setHierarchyHoverEvent($(this),".metaDataHierarchyMenu ul li")});
		//Set menu name or SuggestionsMenu on change of textarea set menu name to appropriate menu item if exists
		$(row).find('.metaTableCellArea').bind('input propertychange',function()
		{
			var input = $(this).val();
			var hierarchyButtonText;
			var row = this.parentElement.parentElement;
			//RegExp to test a valid key in input 
			var KeyExp = /^[0-9]+(?:\.[0-9]+)*$/;
			//RegExp to test a valid key start in input
			var KeyStartExp = /^(?:[0-9]+(?:\.[0-9]+)*)?\.$/;
			//if input valid and key found 
			removeSuggestionsMenu(row);
			createSuggestionsMenu(row);
			/*if ( KeyExp.test(input) && hierarchyData[input] || KeyStartExp.test(input)) 
			{
				createSuggestionsMenu(row);
			}
			*/
			if (hierarchyData[input]){
				hierarchyButtonText = hierarchyData[input][1];
			} else {
				hierarchyButtonText = defaultHierarchyButtonText;
			}
			setHierarchyButtonText(row, hierarchyButtonText);
			
		});
		//Show created menu
		$(row).find('.metaDataHierarchyMenu').show();
}

function createSuggestionsMenu(row)
{
	//get current MetaDataName
	var metaName = getMetaName(row); 
	//Get current hierarchy from storage
	var hierarchyData = hierarchyStorage[metaName];
	//Hierarchy suggestions menu
	var SuggestionsMenu = "";
	
	var input = $(row.getElementsByClassName("metaTableCellArea")[0]).val();
	
	if (input.replace(/[0-9\.\s]/g, '') === "")
	{
		//RegExp to get SuggestionsMenu
		var SuggestionsMenuExp = new RegExp("^0*" + input.replace(/\./g, '\\.0*') + "\\.?[0-9]+$")
		
			for(var key in hierarchyData)
		{
			if (SuggestionsMenuExp.test(key)) 
			{
				SuggestionsMenu +='<li class="hierarchySuggestionsMenu hierarchyOption" id="'+key+'" ><button metavalue="'+ hierarchyData[key][0] +'" metatitle="'+ hierarchyData[key][1] +'" onclick="chooseHierarchyOption(this)" >' + key.substring(String(input).length) + " " + hierarchyData[key][1] + '</button></li>';
			}
		}
	} else {
		var escapedInput = input.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
		//RegExp to get SuggestionsMenu
		var SuggestionsMenuExp = new RegExp( escapedInput,"i");
		
			for(var key in hierarchyData)
		{
			if (SuggestionsMenuExp.test(hierarchyData[key][1]) && input !== hierarchyData[key][0]) 
			{
				SuggestionsMenu +='<li class="hierarchySuggestionsMenu hierarchyOption" id="'+key+'" ><button metavalue="'+ hierarchyData[key][0] +'" metatitle="'+ hierarchyData[key][1] +'" onclick="chooseHierarchyOption(this)" >' + hierarchyData[key][1] + '</button></li>';
			}
		}
	}
	
	
	
	//Append new SuggestionsMenu
	$(row).find(".metaDataHierarchyMenu ul").append(SuggestionsMenu);
	//Register event
	$(row).each(function(){setHierarchyHoverEvent($(this),".hierarchySuggestionsMenu")});
}
//function removeSuggestionsMenu(row,menuNewText)
function removeSuggestionsMenu(row)
{
	$(row).find(".hierarchySuggestionsMenu").remove();
}
function setHierarchyButtonText(row, title){
	$(row).find(".hierarchyMenuButton").text(title);
}

function setHierarchyEventsWrappers(metaName)
{
	//Loop through every metaTableCell
	$(".metaTableCellName").each(function() {
		//Check if it is a hierarchy row
		var currentMetaName = $(this).text();
		if (currentMetaName in hierarchyStorage && currentMetaName == metaName)
	    {
	    	var row = this.parentElement;
	    	var textArea = row.getElementsByClassName("metaTableCellArea")[0];
	    	
	    	//Mouse leave row
	    	$(row).mouseleave(function() {
	    		//textArea = this.getElementsByClassName("metaTableCellArea")[0];
	    		if (this.getElementsByClassName("metaDataHierarchyMenu").length != 0 && document.activeElement.tagName != "TEXTAREA") 
	    		{
	    			$(this).find('ul').stop(true, true);
	    			//Remove hierarchy menu
	    			$(this).find('.metaDataHierarchyMenu').hide();
	    		}

	    	});		    	
	    	// Mouse enter row
	    	$(row).mouseenter(
	    		       function() 
	    		       {
	    		    	   var row = this;
	    		    	   var table = row.parentElement;
	    		    	   //If focused on TEXTAREA do nothing
	    		    	   if (document.activeElement.tagName != "TEXTAREA")
	    		    	   {
	    		    		   //Hide all menus in table except this one
	    		    		   $(table).find('.metaDataHierarchyMenu').each(function() {
	    		    			   var currentRow = this.parentElement;
	    		    			   if (!$(currentRow).is($(row)) )
	    		    			   {
	    		    				   $(this).hide();
	    		    			   } 
	    		    		   });
	    		    		   
	    		    		  // createHierarchyMenuButton($(row));
	    		    		   if ( $(row).find('ul').length == 0  ) 
		    		    	   {
		    		    		   createHierarchyMenuButton(row);
		    		    		   createSuggestionsMenu(row);
		    		    	   }
		    		    	   else 
		    		    	   {
		    		    		   //Unhide menu
		    		    		   $(row).find('.metaDataHierarchyMenu').show();
		    		    		   //Minimize nested menus
		    		    		   $(row).find('.metaDataHierarchyMenu ul').find('ul').hide();
		    		    	   }
	    		    	   }
	    		    	   
	    		    	   
	    		       }
	    		   );
	    	
	    	
	    	// Textarea focus
	    	$(textArea).focus(
	    		       function() 
	    		       {
	    		    	    var row = this.parentElement.parentElement;
	    		    	    var table = row.parentElement;
	    		    	    
	    		    	    
	    		    	  //Hide all menus in table except this one
	    		    		   $(table).find('.metaDataHierarchyMenu').each(function() {
	    		    			   var currentRow = this.parentElement;
	    		    			   if (!$(currentRow).is($(row)) )
	    		    			   {
	    		    				   $(this).hide();
	    		    			   } 
	    		    		   });
	    		    	    //Create button
	    		       		if ( $(row).find('ul').length == 0  ) 
	    		       		{
	    		       			createHierarchyMenuButton(row);
	    		       			createSuggestionsMenu(row);
	    		       		}
	    		       		else 
	    		       		{
	    		       			//Unhide menu
	    		       			$(row).find('.metaDataHierarchyMenu').show();
	    		       			//Minimize nested menus
	    		       			$(row).find('.metaDataHierarchyMenu ul').find('ul').slideUp();
	    		       		}
	    		       		
	    		       		
	    		       }
	    		   );
	    			$(textArea).focusout(
	    		       function() 
	    		       {
  	    		    	    var row = this.parentElement.parentElement;
  	    		    	    
  	    		    	    var found = $(row).find('.metaDataHierarchyMenu ul li ul').filter(":visible")[0];
  	    		    	    //Test if there are open submenu and cursor left tr element
	    		       		if ( found === undefined && !$(row).is(':hover')) 
	    		       		{
	    		       			//Hide hierarchy menu if there are no open submenus
	    		       			$(row).find('.metaDataHierarchyMenu').hide();
	    		       			
	    		       			//Set metadata value if textarea contains hierarchy path
	    		       			substituteHierarchyMetaValue(row);
	    		       		}
	    		       		
	    		       		
	    		       }
	    		   );
	    }
	  });
}

function substituteHierarchyMetaValue(row){
	var text = $(row).find('TEXTAREA').val();
	var metaName = getMetaName(row);
	var hierarchyData = hierarchyStorage[metaName];
	if (hierarchyData[text]){
		var metaValue = hierarchyData[text][0];
		$(row).find('TEXTAREA').val(metaValue);
		
	}

}
function getMetaName(row){
	return $(row.getElementsByClassName("metaTableCellName")[0]).text();
}
var hierarchyStorage = {};
function addHierarchyToStorage(metaDataName,processedHierarchy)
{
	hierarchyStorage[metaDataName] = processedHierarchy;
	
}

