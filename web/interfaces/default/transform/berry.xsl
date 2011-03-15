<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  extension-element-prefixes="java"
  exclude-result-prefixes="java">

  <!--<xsl:include href="style.xsl"/>-->
  <xsl:include href="service-params.xsl"/>
  <xsl:include href="berrytools.xsl"/>

  <xsl:output method="html"/>  

  <xsl:param name="library_name"/>
  
   <!-- the main page layout template is here -->
  <xsl:template match="page">
    <html>
      <xsl:call-template name="pageHead"/>	 
      <body>
	<xsl:attribute name="dir"><xsl:call-template name="direction"/></xsl:attribute>
	<div id="page-wrapper">
	  <xsl:apply-templates select="pageResponse"/>
	</div>
      </body>
    </html>
  </xsl:template>

  <!-- put a space in the title in case the actual value is missing - mozilla will not display a page with no title-->
  <xsl:template name="pageHead">
    <head>
      <title>
	<xsl:call-template name="pageTitle"/>
      </title>
      <xsl:call-template name="pageStyle"/>
    </head>
  </xsl:template>
  
  <xsl:template name="pageTitle">
    <xsl:value-of select="/page/pageResponse/serviceCluster/metadataList/metadata[@name='Title']"/>
    Berry Basket
  </xsl:template>

  <!-- page specific style goes here -->
  <xsl:template name="pageStyle" >
    <xsl:call-template name="styleSheet" />
    <xsl:call-template name="loadLibrary" />
    <xsl:call-template name="buildContent" />   
    <!-- <xsl:call-template name="showMailBox" />-->
  </xsl:template>    
  
  <xsl:template match="pageResponse">
    <xsl:variable name="clusterName"><xsl:value-of select="/page/pageRequest/paramList/param[@name='c']/@value"/></xsl:variable>
    <xsl:call-template name="berryPageBanner" />
    <xsl:apply-templates select="paramList"/>
    <div id="content" ><span></span></div> 
    <xsl:call-template name="footBanner" />
  </xsl:template>
  

  <xsl:template match="collection">
    <xsl:for-each select="item">
      <xsl:text disable-output-escaping="yes">
	var  doc = new Array();             
      </xsl:text>
      <xsl:for-each select="@*">
	<xsl:text disable-output-escaping="yes">
	  doc["</xsl:text><xsl:value-of select="name()" /><xsl:text disable-output-escaping="yes">"]='</xsl:text><xsl:value-of select="." /><xsl:text disable-output-escaping="yes">'; </xsl:text>   
      </xsl:for-each>
      <xsl:text disable-output-escaping="yes"> 
	docList[docList.length] = doc; 
      </xsl:text>  
    </xsl:for-each>     
  </xsl:template>
  
  
  <xsl:template name="buildContent">
    <script type="text/javascript">
      <xsl:text disable-output-escaping="yes">
	var previousView;
	var docList = new Array();
	var urlonly = false;
	var mailinfo = new Array();
	mailinfo['address'] = "To: ";
	mailinfo['cc'] = "CC: ";
	mailinfo['bcc'] = "Bcc: ";
	mailinfo['subject'] = "Subject: "; 
	var textwin;
	var mailwin;
      </xsl:text>
      <xsl:apply-templates select="/page/pageResponse/collection" />       
      
      <xsl:text disable-output-escaping="yes">
    
       function navigate(e){   
             var target = e.target;
       
	     if (target.id.toLowerCase() == 'fullview'){  
                  showFullView();
              }

             if (target.id.toLowerCase() == 'textview'){       
               showTextView();
             }

             if (target.id.toLowerCase() == 'email'){       
               showEmail();
            }

            if (target.id.toLowerCase() == 'sendmail'){       
               sendMail();
            }
            
           

            if (target.id.toLowerCase() == 'urlcheck' &amp;amp;&amp;amp; urlonly){
               var urlcheck = YAHOO.util.Dom.get('urlcheck');
               urlcheck.src = 'interfaces/default/images/check3.gif';              
               var parea  =YAHOO.util.Dom.get('pretextarea');       
               urlonly = false;
               populateUrlsAndMetadata(parea);
               return;
               
            }

            if (target.id.toLowerCase() == 'urlcheck' &amp;amp;&amp;amp; !urlonly ){ 
                var urlcheck = YAHOO.util.Dom.get('urlcheck');
                urlcheck.src = 'interfaces/default/images/check4.gif';                       
                var parea  =YAHOO.util.Dom.get('pretextarea');
                populateUrls(parea);        
                urlonly = true;
                return;
             } 

            if (target.id.toLowerCase() == 'extextview' ){
               if (textwin != null){
                   textwin.close();
               }

               textwin = window.open("","Berry basket plain text view","status=1,width=450,height=300");
               textwin.moveTo(0,0);
               var content = document.createElement('div');
                buildPreview(content);
                var body = textwin.document.getElementsByTagName('body')[0]; 
                body.appendChild(content);
                var prearea =  textwin.document.getElementsByTagName('textarea')[0];
                prearea.cols = '55';
                prearea.rows = '15';        
             }

             if (target.id.toLowerCase() == 'exemail' ){
               if (mailwin != null){
                   mailwin.close();
               }
               mailwin = window.open("","Berry basket mail to a friend","status=1,width=450,height=350");
               mailwin.moveTo(0,0);
               var content = document.createElement('div');
               getEmailContent(content);
               var body = mailwin.document.getElementsByTagName('body')[0]; 
               body.appendChild(content);           
                var prearea =  mailwin.document.getElementsByTagName('textarea')[0];
                prearea.cols = '50';
                prearea.rows = '11'; 
             }
              
        }

        function showFullView(){
          
             var content =  YAHOO.util.Dom.get('content'); 
             var fullview =  YAHOO.util.Dom.get('fullview');

              if (previousView != null){
                 previousView.style.backgroundImage =  'url("interfaces/default/images/bg_on.png")'; 
              } 
              previousView = fullview;
              previousView.style.backgroundImage =  'url("interfaces/default/images/bg_green.png")';

               while (content.hasChildNodes()) {
		 content.removeChild(content.firstChild);
	       }                        
           
             if (docList.length == 0){
                 content.appendChild(document.createTextNode("Your berry basket is empty."));
                 return;
              }

 
            var trashbin = document.createElement('div');
            trashbin.id ='trashbin';   
            var binhandle = document.createElement('div');
            binhandle.id = 'binhandle';   
            binhandle.appendChild(document.createElement('span'));          
            trashbin.appendChild(binhandle) 

            content.appendChild(trashbin);

            var dd = new ygDDOnTop('trashbin');
            dd.setHandleElId('binhandle');      
            new YAHOO.util.DDTarget('trashbin','trash');
        
            var dlist = document.createElement('div');
            content.appendChild(dlist);                          
            var ol =   document.createElement('ol');            
            dlist.appendChild(ol); 

             for (var i in docList){
               var doc = docList[i];
               var li = document.createElement('li'); 
               var a = document.createElement('a'); 
               var text ="";
               a.href ="?a=d&amp;amp;c="+doc['collection']+"&amp;amp;d="+doc['name'];
               a.appendChild(document.createTextNode(doc['title'])); 
                if (doc['root_title'] != ""){
                     li.appendChild(document.createTextNode(doc['root_title']+": ")); 
                   }               
               li.appendChild(a);
               var metadata = "";
               for (var metaItem in doc &amp;amp;&amp;amp; metaItem != 'title'){
                    metadata = "\n"+metaItem+": "+ doc[metaItem]+"\n";
               }
               text +=metadata+"\n";
               li.appendChild(document.createTextNode(text));
               li.id = doc['collection']+":"+ doc['name']; 
               li.className = 'berrydoc';
               ol.appendChild(li);
                new ygDDPlayer(li.id,'trash',docList); 
         }

         /**          
           var extra = document.createElement('div');
           var textview =  document.createElement('span'); 
           textview.id = 'extextview';
           textview.className = 'extralink';   
           textview.appendChild(document.createTextNode("plain text version"));                   
           var email =  document.createElement('span');
           email.appendChild(document.createTextNode("email to a friend"));
           email.id = 'exemail';      
           email.className = 'extralink';    
           extra.appendChild(textview);
           extra.appendChild(email);
           content.appendChild(extra);
         **/ 
        }

       function showTextView(){
             var content =  YAHOO.util.Dom.get('content'); 
             var textview =  YAHOO.util.Dom.get('textview');

              if (previousView != null &amp;amp;&amp;amp; textview !=null){
                 previousView.style.backgroundImage =  'url("interfaces/default/images/bg_on.png")'; 
              } 

             if (textview !=null){
               previousView = textview;
               previousView.style.backgroundImage =  'url("interfaces/default/images/bg_green.png")';
             }

               while (content.hasChildNodes()) {
		 content.removeChild(content.firstChild);
	       }                        
           
             if (docList.length == 0){
                content.appendChild(document.createTextNode("Your berry basket is empty."));
                return;
              }

             buildPreview(content);   

        
       }

       function getEmailContent(content){
           var item ;
           var tr;
           var td;
           var input;
           
           table = document.createElement('table');
           table.setAttribute("class","mailtable");          

          for (item in mailinfo){
  	   tr = document.createElement('tr');       
           td = document.createElement('td');
           td.setAttribute("class","mailitem");
           td.appendChild(document.createTextNode(mailinfo[item]));
           tr.appendChild(td);
 
           td = document.createElement('td');
           input = document.createElement('input'); 
           input.setAttribute("id", item);
           input.setAttribute("class", "mailinput");
           input.setAttribute("type", "text");
           td.appendChild(input);
           tr.appendChild(td);	        
           table.appendChild(tr);
          
          }

 	   // an empty line
           tr = document.createElement('tr');
           td = document.createElement('td');
           td.appendChild(document.createElement('br'));
	   tr.appendChild(td);
  	   table.appendChild(tr);

	   content.appendChild(table);
          
           buildPreview(content);   

           //send button
	   input = document.createElement('input'); 
           input.setAttribute("id", 'sendmail');
           input.setAttribute("class", "sendbutton");
           input.setAttribute("type", "button");
           input.setAttribute("value", "send");        
           content.appendChild(input);
           
        }

      
        function showEmail(){
          
             var content =  YAHOO.util.Dom.get('content'); 
             var email =  YAHOO.util.Dom.get('email');

              if (previousView != null &amp;amp;&amp;amp; email !=null ){
                 previousView.style.backgroundImage =  'url("interfaces/default/images/bg_on.png")'; 
              } 
              
              if (email != null){
                previousView = email;
               previousView.style.backgroundImage =  'url("interfaces/default/images/bg_green.png")';
             }
               while (content.hasChildNodes()) {
		 content.removeChild(content.firstChild);
	       }                        
           
             if (docList.length == 0){
                content.appendChild(document.createTextNode("Your berry basket is empty."));
                return;
              }

                   
           var item ;
           var tr;
           var td;
           var input;
           
           table = document.createElement('table');
           table.setAttribute("class","mailtable");          

          for (item in mailinfo){
  	   tr = document.createElement('tr');       
           td = document.createElement('td');
           td.setAttribute("class","mailitem");
           td.appendChild(document.createTextNode(mailinfo[item]));
           tr.appendChild(td);
 
           td = document.createElement('td');
           input = document.createElement('input'); 
           input.setAttribute("id", item);
           input.setAttribute("class", "mailinput");
           input.setAttribute("type", "text");
           td.appendChild(input);
           tr.appendChild(td);	        
           table.appendChild(tr);
          
          }

 	   // an empty line
           tr = document.createElement('tr');
           td = document.createElement('td');
           td.appendChild(document.createElement('br'));
	   tr.appendChild(td);
  	   table.appendChild(tr);

	   content.appendChild(table);
          
           buildPreview(content);   

           //send button
	   input = document.createElement('input'); 
           input.setAttribute("id", 'sendmail');
           input.setAttribute("class", "sendbutton");
           input.setAttribute("type", "button");
           input.setAttribute("value", "send");        
           content.appendChild(input);

         }


         function buildPreview(parent){
             var div = document.createElement('div');
         
         
             var img =  document.createElement('img');
             img.src = 'interfaces/default/images/check3.gif';
             img.id = 'urlcheck'; 
             div.appendChild(img);
       
             var urls = document.createElement('span');
             urls.id = 'urls';
             urls.className = 'berrycheck';
             urls.appendChild(document.createTextNode('URL only'));
             div.appendChild(urls);

            // var urlsmetadata = document.createElement('span');
            // urlsmetadata.id = 'urlsmetadata'
            // urlsmetadata.className = 'berryradio';
            // urlsmetadata.appendChild(document.createTextNode('URLs and Metadata'));
              
            // div.appendChild(urlsmetadata);
          
          
             parent.appendChild(div);

                       
             var parea  = document.createElement('textarea');
             parea.id = 'pretextarea';                        

             parent.appendChild(parea);        
          
         
             populateUrlsAndMetadata(parea);	    
               
                       	
         }

        
       function populateUrls(parea){
          var urls="";
          var doc_url = document.URL;
          var root_url = doc_url.substring(0,doc_url.indexOf('?'));
           
          for (var i in docList){
              var doc = docList[i];
              urls +=root_url+"?a=d&amp;amp;c="+doc["collection"]+"&amp;amp;d="+doc["name"]+"\n\n";  
                     
          }

           parea.value = urls;
            
         }

	 function populateUrlsAndMetadata(parea){
                       
              var doc_url = document.URL;
              var root_url = doc_url.substring(0,doc_url.indexOf('?'));
              var fulltext="";

              for (var i in docList){
                   var doc = docList[i];
                   var url =root_url+"?a=d&amp;amp;c="+doc['collection']+"&amp;amp;d="+doc['name']+"\n";  
                    for (var metaItem in doc){
                        var metadata = metaItem+": "+ doc[metaItem]+"\n";
                      }
                     fulltext +=url+metadata+"\n";
                 
              }

               parea.value = fulltext;
                  
           }                                                    	    		 
	                     
          function sendMail(){
              var  url = "?a=pr&amp;amp;rt=r&amp;amp;ro=1&amp;amp;s=SendMail&amp;amp;c=";
                     var request_type = "POST";
                     var postdata = ""; 
 	             var i;	     
                     //get checked items
                      for (i in mailinfo)
			{ 
                         
                          var input = YAHOO.util.Dom.get(i);  
                          var value = input.value;  
			  postdata +="&amp;amp;s1."+i+"="+value;	
			}                                   


                       var content = YAHOO.util.Dom.get('pretextarea').value;
                       
                       content = content.replace(/&amp;amp;/g,'-------');  
                       postdata +="&amp;amp;s1.content="+content;
                                                                
                      
                        var callback = {
     			   success:function(o){
                                    var result = o.responseText;
                                    alert(result);     
	 			                                                                      
                                	} ,
     			    failure:function(o){
                                        alert("Sending mail falied");    
 				     }    
  			 }
                                               
 	               
   			YAHOO.util.Connect.asyncRequest(request_type , url , callback, postdata);         
         }	

        YAHOO.util.Event.addListener(window,'click', navigate);
        YAHOO.util.Event.addListener(window,'load', showFullView);
      </xsl:text>
    </script>    
  </xsl:template>

  <xsl:template name="styleSheet">
    <link rel="stylesheet" href="interfaces/default/style/core.css" type="text/css"/>  
    <link rel="stylesheet" href="interfaces/default/style/berry.css" type="text/css"/>  
  </xsl:template>
  
  <xsl:template name="showMailBox">
    <script type="text/javascript">
      <xsl:text disable-output-escaping="yes">
	var show = false;	
	var table;
	var mailserver  = new Array();     
	mailserver['host'] = "Mail Server: ";
	mailserver['user'] = "User Name: ";
	mailserver['password'] = "Password: ";
	
	var mailinfo = new Array();
	mailinfo['address'] = "To: ";
	mailinfo['cc'] = "CC: ";
	mailinfo['bcc'] = "Bcc: ";
	mailinfo['subject'] = "Subject: "; 
	
	function mailBoxUpdate(e) {
		var target = e.target;
		if (target.id.toLowerCase() == 'sendasemail' &amp;amp;&amp;amp; !show){
	       	    showMailBox();
                    show = true;
                }
                else{
                    if (target.id.toLowerCase() == 'sendasemail' &amp;amp;&amp;amp; show){
                       hideMailBox();
                       show = false;
                    }

                     if (target.id.toLowerCase() == 'sendmail'){
                      sendMail();
                    }
                }


                	
        }

       function showMailBox(){
           var mailbox = YAHOO.util.Dom.get('mailbox');  
           if (table != null &amp;amp;&amp;amp; !show){
               mailbox.appendChild(table);
              return;
           }
                        
           var item ;
           var tr;
           var td;
           var input;
           
           table = document.createElement('table');
           table.setAttribute("class","mailtable");          

           // mail server
           tr = document.createElement('tr');
           td = document.createElement('td');
           td.setAttribute("class","mailitem");
           td.appendChild(document.createTextNode(mailserver['host']));
           tr.appendChild(td);
 
           td = document.createElement('td');
           input = document.createElement('input'); 
           input.setAttribute("id", 'host');
           input.setAttribute("class", "mailinput");
           input.setAttribute("type", "text");
           td.appendChild(input);
           td.appendChild(document.createTextNode(' e.g. webmail.cs.waikato.ac.nz'));
           tr.appendChild(td);	        
           table.appendChild(tr);
         
           //user name 
	   tr = document.createElement('tr');
           td = document.createElement('td');
           td.setAttribute("class","mailitem");
           td.appendChild(document.createTextNode(mailserver['user']));
           tr.appendChild(td);
           td = document.createElement('td');
           input = document.createElement('input'); 
           input.setAttribute("id", 'user');
           input.setAttribute("class", "mailinput");
           input.setAttribute("type", "text");
           td.appendChild(input);
           tr.appendChild(td);	        
           table.appendChild(tr);

           //password 	
	   tr = document.createElement('tr');
           td = document.createElement('td');
           td.setAttribute("class","mailitem");
           td.appendChild(document.createTextNode(mailserver['password']));
           tr.appendChild(td);
           td = document.createElement('td');
           input = document.createElement('input'); 
           input.setAttribute("id", 'password');
           input.setAttribute("class", "mailinput");
           input.setAttribute("type", "password");
           td.appendChild(input);
           tr.appendChild(td);	        
           table.appendChild(tr);

           // an empty line
           tr = document.createElement('tr');
           td = document.createElement('td');
           td.appendChild(document.createElement('br'));
	   tr.appendChild(td);
  	   table.appendChild(tr);

          for (item in mailinfo){
  	   tr = document.createElement('tr');       
           td = document.createElement('td');
           td.setAttribute("class","mailitem");
           td.appendChild(document.createTextNode(mailinfo[item]));
           tr.appendChild(td);
 
           td = document.createElement('td');
           input = document.createElement('input'); 
           input.setAttribute("id", item);
           input.setAttribute("class", "mailinput");
           input.setAttribute("type", "text");
           td.appendChild(input);
           tr.appendChild(td);	        
           table.appendChild(tr);
          
          }

 	// an empty line
           tr = document.createElement('tr');
           td = document.createElement('td');
           td.appendChild(document.createElement('br'));
	   tr.appendChild(td);
  	   table.appendChild(tr);

	//send button
	   tr = document.createElement('tr');
       	   td = document.createElement('td');
           td.setAttribute("class","mailitem");
           input = document.createElement('input'); 
           input.setAttribute("id", 'sendmail');
           input.setAttribute("class", "sendbutton");
           input.setAttribute("type", "button");
           input.setAttribute("value", "send");
           td.appendChild(input);
           tr.appendChild(td);	        
           table.appendChild(tr);

           
          mailbox.appendChild(table);
         }            
     

       function hideMailBox(){
          var mailbox = YAHOO.util.Dom.get('mailbox');	
          while (mailbox.hasChildNodes()) {
		mailbox.removeChild(mailbox.firstChild);
	   }
         }
      
         function sendMail(){
              var  url = "?a=pr&amp;amp;rt=r&amp;amp;ro=1&amp;amp;s=SendMail&amp;amp;c=";
                     var request_type = "POST";
                     var postdata = ""; 
 	             var i;	     
                     //get checked items
                      for (i in mailinfo)
			{ 
                         
                          var input = YAHOO.util.Dom.get(i);  
                          var value = input.value;  
			  postdata +="&amp;amp;s1."+i+"="+value;	
			}                                   


                       for (i in mailserver){
                          var input = YAHOO.util.Dom.get(i);  
                          var value = input.value; 
                           postdata +="&amp;amp;s1."+i+"="+value;
                        }

                       var content = YAHOO.util.Dom.get('pretextarea').value;
                       
                       content = content.replace(/&amp;amp;/g,'-------');  
                       postdata +="&amp;amp;s1.content="+content;
                                                                
                      
                        var callback = {
     			   success:function(o){
                                    var response = o.responseXML;
            	                       
	 			         alert(response.getAttribute('status'));                                                               
                                	} ,
     			    failure:function(o){
                                        alert("Sending mail falied");    
 				     }    
  			 }
                                               
 	               
   			YAHOO.util.Connect.asyncRequest(request_type , url , callback, postdata);         
                 }	

              
            YAHOO.util.Event.addListener(window,'click',mailBoxUpdate );

      </xsl:text> 
    </script>
  </xsl:template>
  
  <xsl:template name="berryPageBanner">
    <div class='banner'>
      <div class='pageinfo'>
	<p class='bannerlinks'>
	  <a class='navlink' href='{$library_name}?a=p&amp;amp;sa=home' title='Library home page' >HOME</a>
	  <a class='navlink' href='{$library_name}?a=p&amp;amp;sa=help&amp;amp;c='  title='Help pages' >HELP</a>
	  <a class='navlink' href='{$library_name}?a=p&amp;amp;sa=pref&amp;amp;c=' title='Change your interface preferences'>PREFERENCES</a>
	</p>      
      </div>
      <div class='basketimage'><p class='bannertitle'>Berry Basket</p></div>        
    </div>
    <div class="bannerextra"></div>
    
    <div >
      <p class='navbar'><span id='fullview' class='navlink'>full view</span><span id='textview' class='navlink'>plain text version</span><span id='email' class='navlink'>email to a friend</span></p>
    </div> 
  </xsl:template>
  
  <xsl:template name="footBanner">
    <div >
      <p class='navbar'><span >powered by greenstone3</span></p>
    </div> 
  </xsl:template>
  
  <xsl:template name="direction">
    <xsl:if test="/page/@lang='ar' or /page/@lang='fa' or /page/@lang='he' or /page/@lang='ur' or /page/@lang='ps' or /page/@lang='prs'">rtl</xsl:if>
  </xsl:template>

</xsl:stylesheet>  

