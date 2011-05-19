<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">

  <xsl:param name="berryBaskets"/>
  
  <xsl:template name="berrybasket">
    <div id="berrybasket" class="hide" >
      <div id="baskethandle"><span></span></div>
      <div id ="berries"><span></span></div>
    </div>
  </xsl:template>
  
  <xsl:template name="documentBerryBasket">
    <xsl:param name="collName"/>
    <xsl:param name="selectedNode"/>
    <xsl:param name="rootNode"/>
    <xsl:param name="docType" />
    <div id="berrybasket" class="hide" >
      <div id="baskethandle"><span></span></div>
      <div id ="berries" ><span></span></div>
    </div>
    <xsl:choose>
      <xsl:when test="$selectedNode = $rootNode">
	<p id="documentberries">    
	  <img class='pick'  id="{$collName}:{$rootNode}" src="interfaces/default/images/berry3.png" alt="in basket" width="15" height="15" border="0"/><span id="{$collName}:{$rootNode}:root" class="documentberry">the whole document</span></p>       
      </xsl:when>
      <xsl:otherwise>
	<p id="documentberries">    
	  <img class='pick'  id="{$collName}:{$rootNode}" src="interfaces/default/images/berry3.png" alt="in basket" width="15" height="15" border="0"/><span id="{$collName}:{$rootNode}:root" class="documentberry">the whole document</span><img class='pick'  id="{$collName}:{$selectedNode}" src="interfaces/default/images/berry3.png" alt="in basket" width="15" height="15" border="0"/><span id="{$collName}:{$selectedNode}:section" class="documentberry">the current section</span></p>
      </xsl:otherwise> 
    </xsl:choose>
  </xsl:template>

  
  <xsl:template name="loadLibrary">
    <script type="text/javascript" src="interfaces/default/js/YAHOO.js"><xsl:text disable-output-escaping="yes"> </xsl:text></script>
    <script type="text/javascript" src="interfaces/default/js/event.js"><xsl:text disable-output-escaping="yes"> </xsl:text></script>
    <script type="text/javascript" src="interfaces/default/js/connection.js"><xsl:text disable-output-escaping="yes"> </xsl:text></script>
    <script type="text/javascript" src="interfaces/default/js/dom.js"><xsl:text disable-output-escaping="yes"> </xsl:text></script>
    <script type="text/javascript" src="interfaces/default/js/dragdrop.js"><xsl:text disable-output-escaping="yes"> </xsl:text></script>
    <script type="text/javascript" src="interfaces/default/js/ygDDPlayer.js"><xsl:text disable-output-escaping="yes"> </xsl:text></script>
    <script type="text/javascript" src="interfaces/default/js/ygDDOnTop.js"><xsl:text disable-output-escaping="yes"> </xsl:text></script>
    
  </xsl:template>
  
  <xsl:template name="js-library">
    <xsl:call-template name="loadLibrary" />	
    <xsl:call-template name="basketCheckout" />
  </xsl:template> 
  
  <!-- should be called for a documentNode -->
  <xsl:template name="addBerry">
    <xsl:param name="collName"/>
    <td valign="top"><img class='pick' id="{$collName}:{@nodeID}" src="interfaces/default/images/berry3.png" alt="in basket" width="15" height="15" border="0"/></td>
  </xsl:template>
  
  <xsl:template name="basketCheckout">
    <script type="text/javascript">
      <xsl:text disable-output-escaping="yes">
      //used to stored the current items in berrybasket, including collection name, document id and dodument title 
      //in the format of collection:id:[title], it is updated when the page is loaded (reloaded) or the user adds
      // new items in the berry basket (see ygDDplayer.js). It is used to show the contents of the berry basket. 
     var berryList =  new Array();
     var show = false;    
 
     // the default width and height of the berry basket
     var oldHeight=90;
     var oldWidth=140;
     var oldBg;

     var dd = new ygDDOnTop('berrybasket');
     dd.setHandleElId('baskethandle');

     var checkout = function(){
       var request_type = "GET";
       var url = "?a=pr&amp;rt=r&amp;ro=1&amp;s=ItemNum&amp;o=XML&amp;c="; 
 
         var responseSuccess = function(o){
	     var response = o.responseXML;
             var size = response.getElementsByTagName('size').item(0).getAttribute('value');       
           
             var items = response.getElementsByTagName('item');
             
             for (var i=0;i &lt; items.length ; i++ ){
 
                 // remove berries images from if the berry basket has already contains this item  
                
                 var itemID = items[i].getAttribute('collection')+":"+items[i].getAttribute('name');
                 var item =  YAHOO.util.Dom.get(itemID);
                 if (item != null){
                    var parent = item.parentNode;
                    parent.removeChild(item);
                   
                   if (parent !=null &amp;&amp; parent.id == "documentberries"){
                         var root = YAHOO.util.Dom.get(itemID+":root");
                         var section = YAHOO.util.Dom.get(itemID+":section");
                         if(root!=null &amp;&amp; root.id.indexOf(itemID) !=-1){
                             parent.removeChild(root);
                          }
     
	                  if(section!=null &amp;&amp; section.id.indexOf(itemID) !=-1){
                             parent.removeChild(section);
        	          }
                      
                          
                     }
                 }
                                
                  berryList[berryList.length] = items[i];                   	
	      }

               //add berryimage into the berry basket
                updateBerryImages();         
             
                // set berries images visible
             // they are set to be invisible to prevent flickering when first loaded (see berry.css) 
            
             var berries =document.getElementsByTagName('img');
	     var berrybasket = new YAHOO.util.DDTarget('berrybasket','basket');
           
             for (var j=0; j &lt; berries.length; j++){
          
                  var berry = berries[j]; 
                   var parent = berry.parentNode;
                   
                   
                   if (parent !=null &amp;&amp; parent.id == "documentberries"){
                         var root = YAHOO.util.Dom.get(berry.id+":root");
                         var section = YAHOO.util.Dom.get(berry.id+":section");
                         if(root!=null &amp;&amp; root.id.indexOf(berry.id) !=-1){
                             root.style.visibility ="visible" ;
                          }
     
	                  if(section!=null &amp;&amp; section.id.indexOf(berry.id) !=-1){
                             section.style.visibility ="visible" ;
        	          }
                      
                          
                     }  

               if (berry.className  &amp;&amp;  berry.className  == 'pick')
                  { 
                    berry.style.visibility = "visible";
                    new ygDDPlayer(berry.id,'basket',berryList); 
                  }   
     	      
             }
           
           }

         var responseFailure = function(o){
             
          }  

        var callback = {
        success: responseSuccess,
        failure: responseFailure 
      }

      YAHOO.util.Connect.asyncRequest(request_type , url , callback);
   }

    function updateBerryImages(){
     
      for (var i =0;i &lt; berryList.length &amp;&amp; i &lt;12 ; i++ ){
               var berries = YAHOO.util.Dom.get('berries');
               var img = document.createElement('img');
               img.src = 'interfaces/default/images/berry3.png';
               img.height = 15;
               img.width = 15;
               img.border = 0;              
               berries.appendChild(img);
          }

    }

    function basketUpdate(e) {
		var target = e.target;
               
		if ((target.id.toLowerCase() == 'berrybasket' || target.id.toLowerCase() == 'berries')  &amp;&amp; !show){
	       	    showBasket();
                    show = true;
                }
                else{
                    if (target.id.toLowerCase() == 'hideview' &amp;&amp; show){
                       hideBasket();
                       show = false;
                    }
                }
           
            if (target.className == 'pick'){
              addBerry(target);
            }
            
        }

     function showBasket() {
        var berryBasket  = YAHOO.util.Dom.get('berrybasket');  
        var basketHandle = YAHOO.util.Dom.get('baskethandle'); 
        var berries = YAHOO.util.Dom.get('berries');   
        var div = document.createElement('div');
        var list = document.createElement('ol');
        var width = 500;
        var height = 40;
        var i=0;
 
        list.id = 'doclist';
      
       //remove berryImages in the berry basket 
       while (berries.hasChildNodes()) {
       	  berries.removeChild(berries.firstChild);
	}

       while (basketHandle.hasChildNodes()) {
       	  basketHandle.removeChild(basketHandle.firstChild);
	}

       for (i in berryList){
            var berryItem = berryList[i];
            var berryElement = document.createElement('li');
            var title = berryItem.getAttribute('title');
            var root_title = berryItem.getAttribute('root_title'); 
            var id = berryItem.getAttribute('collection')+":"+berryItem.getAttribute('name'); 

            if (root_title != ""){
              root_title +=":";
            }
	    
            title = root_title+title;	
            if (title.length > 50){
               title = title.substring(0,20)+" ... "+title.substr(title.length-35,35);
             }
  
            berryElement.appendChild(document.createTextNode(title));
            berryElement.setAttribute("class","berryitem");
            list.appendChild(berryElement);
            height +=18;
          } 
      	   
        oldHeight = berryBasket.style.height;
        oldWidth = berryBasket.style.width;
        oldBg = berryBasket.style.background; 
        berryBasket.style.height = height;
        berryBasket.style.width = width;
        berryBasket.style.background ='url("interfaces/default/images/kete2.png") 0 0 repeat';
        berryBasket.style.cursor = "default";         
        berryBasket.className = "show";
        div.appendChild(list); 	
        berries.appendChild(div);  
        berries.style.height = height - 40;

        var links = document.createElement('div');
        var hideView = document.createElement('a');
        var fullView = document.createElement('a');
        links.appendChild(hideView);
        links.appendChild(fullView);
        basketHandle.appendChild(links);
        hideView.appendChild(document.createTextNode("Hide"));
        fullView.appendChild(document.createTextNode("Full View"));
        hideView.setAttribute("id","hideview");
        fullView.setAttribute("href","?a=g&amp;sa=berry&amp;c=&amp;s=DisplayList&amp;rt=r");
        fullView.setAttribute("class","fullview");

        }

      function hideBasket() {
          var i = 0;
          var berryBasket  = YAHOO.util.Dom.get('berrybasket');  
          var basketHandle  = YAHOO.util.Dom.get('baskethandle'); 
          var berries = YAHOO.util.Dom.get('berries'); 
          berryBasket.style.height = oldHeight;
          berryBasket.style.width = oldWidth;
          berryBasket.style.background = oldBg;
          berryBasket.style.cursor = "pointer";
          berryBasket.className = "hide";

          while (berries.hasChildNodes()) {
		berries.removeChild(berries.firstChild);
	}
         
        while (basketHandle.hasChildNodes()) {
		basketHandle.removeChild(basketHandle.firstChild);
	}
         
        updateBerryImages();
      }    


     function addBerry(el){
      var  addurl = "?a=pr&amp;rt=r&amp;ro=1&amp;s=AddItem&amp;c=&amp;s1.id=2&amp;o=XML&amp;s1.item=" + el.id;	   
      var addSuccess = function(o){ 
 		 var result = o.responseXML;
                 var items = result.getElementsByTagName('item');
                 if (items.length &gt; 0){
                    var item = items[0];
                    var berrybasket = YAHOO.util.Dom.get('berrybasket');
        	    var berries = YAHOO.util.Dom.get('berries');
                    berryList[berryList.length]= item;   
                    var parent =el.parentNode;
                    if (parent == null) return;     
                    parent.removeChild(el);
               
                     var itemID = item.getAttribute('collection')+":"+item.getAttribute('name'); 
                    if (parent !=null  &amp;&amp; parent.id == "documentberries"){
                         var root = YAHOO.util.Dom.get(itemID+":root");
                         var section = YAHOO.util.Dom.get(itemID+":section");
                         if(root!=null  &amp;&amp; root.id.indexOf(itemID) !=-1){
                             parent.removeChild(root);
                          }
     
	                  if(section!=null  &amp;&amp; section.id.indexOf(itemID) !=-1){
                             parent.removeChild(section);
        	          }                                                
                     }

                    if (!YAHOO.util.Dom.get('hideview') &amp;&amp;  berryList.length &lt; 13){
                      while (berries.hasChildNodes()) {
       	                   berries.removeChild(berries.firstChild);
	                }
                        
                     for (var i =0; i &lt; berryList.length; i++ ){
                             var img = document.createElement('img');
                              img.src = 'interfaces/default/images/berry3.png';
                              img.height = 15;
                              img.width = 15;
                              img.border = 0;              
                              berries.appendChild(img);
                       }
      	            
                                                     
	          }
                 else{
           
                  if (YAHOO.util.Dom.get('hideview')){
                        var berryBasket  = YAHOO.util.Dom.get('berrybasket');
                        var berries  = YAHOO.util.Dom.get('berries');
                        var doclist = YAHOO.util.Dom.get('doclist'); 
                        var tid = el.id;                                  
                        var berryItem; 
                        var berryElement = document.createElement('li');
                         for (var i in berryList){
                             berryItem = berryList[i];
                             var id = berryItem.getAttribute('collection')+":"+berryItem.getAttribute('name');
                             if (id == tid){
                               var title = berryItem.getAttribute('title');
                               var root_title = berryItem.getAttribute('root_title');  
                                if (root_title != ""){
                                      root_title +=":";
                                     }
	    
                                title = root_title+title;	
                                if (title.length > 50){
                                   title = title.substring(0,20)+" ... "+title.substr(title.length-35,35);
                                }
            
                               berryElement.appendChild(document.createTextNode(title));
                               berryElement.setAttribute("class","berryitem");
                               doclist.appendChild(berryElement);
                               var heightStr =  berryBasket.style.height+"";
                               var height =parseInt(heightStr.substring(0,heightStr.length-2)) +18;                             
                               berryBasket.style.height = height;
                               berries.style.height = height;
                               break;
                            }
                         }
                             
                      }
                    
                 }
               }
                                       
             }

           var addFailure = function(o){ }

           var addcallback = {
        	   success:addSuccess,
     	           failure:addFailure
                }

            YAHOO.util.Connect.asyncRequest(request_type , addurl , addcallback);    

         }

  
      
     YAHOO.util.Event.addListener(window,'load', checkout); 
     YAHOO.util.Event.addListener(window,'click', basketUpdate); 
      </xsl:text>
    </script>
  </xsl:template>

  <xsl:template name="berryStyleSheet">
    <link rel="stylesheet" href="interfaces/default/style/berry.css" type="text/css"/>  
  </xsl:template>
  
</xsl:stylesheet>
