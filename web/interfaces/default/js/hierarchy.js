//hierarchy storage object

//hierarchy menu Button text
var hierarchyMenuButton = "Menu";
//Find first ancestor element by tag name
function findAncestorByTagName (element, tagName) {
    while ((element.tagName != tagName) && (element = element.parentElement));
    return element;
}
// Function to set Id as TEXTAREA value
function setHierarchyId(a)
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
		removeSuggestionsMenu(tr, metaTitle);
		//Hide menu after click
		$(tr).find(".metaDataHierarchyMenu").find("ul li ul li").hide();
		//If we left TEXTAREA, hide all menus
		//if (document.activeElement.tagName != "TEXTAREA")
		//{
		//	$(tr).find(".metaDataHierarchyMenu").hide();
		//}
		tr.getElementsByTagName("TEXTAREA")[0].focus();
		
		createSuggestionsMenu(tr);
		
	}
}
function openHierarchyMenuLevel(menuItem)
{
        //console.log(menuItem);
			
			var tr = findAncestorByTagName(menuItem,"TR");
			//get current MetaDataName
			var metaName = $(tr.getElementsByClassName("metaTableCellName")[0]).text();
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
                //console.log("ID " + id);
                //id.match(getIdExp);
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
   						levelItems[key]='<li id="'+key+'" ><button onclick="setHierarchyId(this)"  metavalue='+ hierarchyData[key][0] +' metatitle='+ hierarchyData[key][1] +'>' + hierarchyData[key][1] + '</button></li>';
      					//console.log(levelItems[key]);
      					
   					}
   				 }
              //If no elements in hierarchy level
   				 if (jQuery.isEmptyObject(levelItems))
   				 {
   					 //add empty menu. Invisible. Used by checks in setHierarchyEventsWrappers focusout to prevent menu hiding while choosing suggestion menu item leaf
   					menuItem.append("<ul></ul>");
   				 	
   				 //	console.log("no elements in hierarchy level");
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
                  
									 
   				 	//console.log("debug line 5");
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
				//var expr = /^([0-9]+(?:\.[0-9]+)*)\ ([0-9]+(?:\.[0-9]+)*)\ (.*)/m;
				var expr = /^(\S*|\"[^\"]*\")\ +([0-9]+(?:\.[0-9]+)*)\ +(.*)/m;
				StringData = hierarchyFile.split('\n');
				for (var i = 0; i < StringData.length; i++) 
					{
					var result = StringData[i].match(expr);
					// If result not null
					if (result != null && result.length == 4) 
						{
						// populate hierarchy object
						hierarchyData[result[2]] = [result[1], result[3]];
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
		var metaName = $(row.getElementsByClassName("metaTableCellName")[0]).text();
		
		var hierarchyMenuName = 'Menu';
		// Check if textarea already contain right menu key
		var textAreaValue = $(row).find('TEXTAREA').val();
		
		//Get current hierarchy from storages
		var hierarchyData = hierarchyStorage[metaName];
		
		if (hierarchyData[textAreaValue] && (hierarchyData[textAreaValue] != null)) 
		{				
			hierarchyMenuName = hierarchyData[textAreaValue][1];
			
		}
		
		//Menu element
		var mainmenu = '<td class="metaDataHierarchyMenu" style="display: none;"><ul><li id="hierarchyLevel"><button class="hierarchyMenuButton"  title="Menu">' + hierarchyMenuName + '</button></li></ul></td>'
		//Insert hierarchy menu
		$(row).find('.metaTableCellRemove').after(mainmenu);
		//Set hover event on hierarchy menu
		$(row).each(function(){setHierarchyHoverEvent($(this),".metaDataHierarchyMenu ul li")});
		//Set menu name or SuggestionsMenu on change of textarea set menu name to appropriate menu item if exists
		$(row).find('.metaTableCellArea').bind('input propertychange',function()
		{
			var input = $(this).val();
			var row = this.parentElement.parentElement;
			//RegExp to test a valid key in input 
			var KeyExp = /^[0-9]+(?:\.[0-9]+)*$/;
			//RegExp to test a valid key start in input
			var KeyStartExp = /^(?:[0-9]+(?:\.[0-9]+)*)?\.$/;
			//if input valid and key found 
			if ( KeyExp.test(input) && hierarchyData[input]) 
			{
				removeSuggestionsMenu(row,hierarchyData[input]);
				createSuggestionsMenu(row);
			}
			else if (KeyStartExp.test(input)) 
			{
				removeSuggestionsMenu(row,hierarchyMenuButton);
				createSuggestionsMenu(row);
			}
			else {
				removeSuggestionsMenu(row,hierarchyMenuButton);	
			} 
		});
		//Show created menu
		$(row).find('.metaDataHierarchyMenu').show();
}

function createSuggestionsMenu(row)
{
	//get current MetaDataName
	var metaName = $(row.getElementsByClassName("metaTableCellName")[0]).text();
	//Get current hierarchy from storages
	var hierarchyData = hierarchyStorage[metaName];
	
	var input = $(row.getElementsByClassName("metaTableCellArea")[0]).val();
	
	//RegExp to get SuggestionsMenu
	var SuggestionsMenuExp = new RegExp("^0*" + input.replace(/\./g, '\\.0*') + "\\.?[0-9]+$")
	//Hierarchy suggestions menu
	var SuggestionsMenu = "";
	for(var key in hierarchyData)
	{
	var SuggestionsMenuItems = {};
		
		if (SuggestionsMenuExp.test(key)) 
		{
			SuggestionsMenuItems[key]='<li class="hierarchySuggestionsMenu" id="'+key+'" ><button metavalue='+ hierarchyData[key][0] +' metatitle='+ hierarchyData[key][1] +' onclick="setHierarchyId(this)" >' + key.substring(String(input).length) + " " + hierarchyData[key][1] + '</button></li>';
		}
		
		for(var key in SuggestionsMenuItems)
		{
			//Fill menu with items
			SuggestionsMenu += SuggestionsMenuItems[key];
		}
		
	}
	
	//Append new SuggestionsMenu
	$(row).find(".metaDataHierarchyMenu ul").append(SuggestionsMenu);
	//Register event
	$(row).each(function(){setHierarchyHoverEvent($(this),".hierarchySuggestionsMenu")});
}
function removeSuggestionsMenu(row,menuNewText)
{
	//Remove old SuggestionsMenu
	$(row).find(".hierarchySuggestionsMenu").remove();
	//Replace text on Hierarchy menu to default
	$(row).find(".hierarchyMenuButton").text(menuNewText);
}

function setHierarchyEventsWrappers(metaName)
{
	
	//Loop through every metaTableCell
	$(".metaTableCellName").each(function() {
		//Check if it is a hierarchy row
		//TODO implement real check
	    //if($(this).text()=="rubricator")
		var currentMetaName = $(this).text();
		//console.log(metaName)
		//console.log(metaDataName)
		if (currentMetaName in hierarchyStorage && currentMetaName == metaName)
	    {
			//console.log('testXX')
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
  	    		    	    //Test if there are open submenu
  	    		    	    var found = $(row).find('.metaDataHierarchyMenu ul li ul').filter(":visible")[0];
  	    		    	    
  	    		    	    //Hide hierarchy menu if there are no open submenus
	    		       		if ( found === undefined) 
	    		       		{
	    		       			//Hide menu
	    		       			$(row).find('.metaDataHierarchyMenu').hide();
	    		       			//console.log(this);
	    		       			//console.log(row);
	    		       			//console.log(found);
	    		       		}
	    		       		
	    		       		
	    		       }
	    		   );
	    }
	  });
}
var hierarchyStorage = {};
function addHierarchyToStorage(metaDataName,processedHierarchy)
{
	hierarchyStorage[metaDataName] = processedHierarchy;
	//console.log( hierarchyStorage)
	//console.log( metaDataName)
	
}

