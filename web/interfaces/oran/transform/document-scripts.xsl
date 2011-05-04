<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util gsf">

	<xsl:template name="expansionScript">
		<script type="text/javascript">
			<xsl:text disable-output-escaping="yes">
				function toggleSection(sectionID)
				{
					var docElem = document.getElementById("doc" + sectionID);
					var tocElem = document.getElementById("toc" + sectionID);
					
					var tocToggleElem = document.getElementById("ttoggle" + sectionID);
					var docToggleElem = document.getElementById("dtoggle" + sectionID);
					
					if(docElem.style.display == "none")
					{
						var imageClass = (docToggleElem.getAttribute("class").indexOf("leafNode") != -1) ? "icon toggleImageCollapse leafNode" : "icon toggleImageCollapse";
						
						docElem.style.display = "block";
						docToggleElem.setAttribute("class", imageClass);
						
						if(tocToggleElem)
						{
							tocToggleElem.setAttribute("class", imageClass);
						}
						
						if(tocElem)
						{
							tocElem.style.display = "block";
						}
					}
					else
					{
						docElem.style.display = "none";
						
						//Use the page image if this is a leaf node and the chapter image if it not
						//var imageClass = (docToggleElem.getAttribute("class").indexOf("leafNode") != -1) ? "icon toggleImagePage leafNode" : "icon toggleImageChapter";
						var imageClass = (docToggleElem.getAttribute("class").indexOf("leafNode") != -1) ? "icon toggleImageExpand leafNode" : "icon toggleImageExpand";
						docToggleElem.setAttribute("class", imageClass);
						
						if(tocToggleElem)
						{
							tocToggleElem.setAttribute("class", imageClass);
						}
						
						if(tocElem)
						{
							tocElem.style.display = "none";
						}
					}
				}
				
				function isExpanded(sectionID)
				{
					var docElem = document.getElementById("doc" + sectionID);
					if(docElem.style.display == "block")
					{
						return true;
					}
					return false;
				}
				
				function isParentOf(parent, child)
				{
					if(child.indexOf(parent) != -1 &amp;&amp; child.length > parent.length &amp;&amp; child[parent.length] == '.')
					{
						return true;
					}
					return false;
				}
				
				function focusSection(sectionID)
				{
					var tableOfContentsDiv = document.getElementById("tableOfContents");
					var images = tableOfContentsDiv.getElementsByTagName("img");
					var nodeArray = new Array();
					
					for(var i = 0; i &lt; images.length; i++)
					{	
						nodeArray[i] = images[i].getAttribute("id").substring(7);
					}
					
					for(var j = 0; j &lt; nodeArray.length; j++)
					{
						//If this is the node that was clicked and it is not expanded then expand it
						if(nodeArray[j] == sectionID)
						{
							if(!isExpanded(nodeArray[j]))
							{
								toggleSection(nodeArray[j]);
							}
							
							continue;
						}
						
						//If the node is a parent or child of the node that is clicked and is not expanded then expand it
						if((isParentOf(nodeArray[j], sectionID) || isParentOf(sectionID, nodeArray[j])) &amp;&amp; !isExpanded(nodeArray[j]))
						{
							toggleSection(nodeArray[j]);
						}
						//If the node is not a parent or child and is expanded then collapse it
						else if(!(isParentOf(nodeArray[j], sectionID) || isParentOf(sectionID, nodeArray[j])) &amp;&amp; isExpanded(nodeArray[j]))
						{
							toggleSection(nodeArray[j]);
						}
					}
				}
			</xsl:text>
		</script>
	</xsl:template>
	
	<xsl:template name="highlightingScript">
		<script type="text/javascript">
			<xsl:text disable-output-escaping="yes">
				function addHighlight()
				{
					var spans = document.getElementsByTagName("span");
					for(var i = 0; i &lt; spans.length; i++)
					{
						var currentSpan = spans[i];
						if(currentSpan.getAttribute("class") == "noTermHighlight")
						{
							currentSpan.setAttribute("class", "termHighlight");
						}
					}
					
					var option = document.getElementById("highlightOption");
					option.innerHTML = "No Highlighting";
					option.setAttribute("href", "javascript:removeHighlight();");
				}
				
				function removeHighlight()
				{
					var spans = document.getElementsByTagName("span");
					for(var i = 0; i &lt; spans.length; i++)
					{
						var currentSpan = spans[i];
						if(currentSpan.getAttribute("class") == "termHighlight")
						{
							currentSpan.setAttribute("class", "noTermHighlight");
						}
					}
					
					var option = document.getElementById("highlightOption");
					option.innerHTML = "Highlighting";
					option.setAttribute("href", "javascript:addHighlight();");
				}
			</xsl:text>
		</script>
	</xsl:template>
	
	<xsl:template name="realisticBooksScript">
		<div id="bookdiv"/>
		<script type="text/javascript">
			<xsl:text disable-output-escaping="yes">
				
				//Helper function to create param elements
				function createParam(name, value)
				{
					var param = document.createElement("PARAM");
					param.setAttribute("name", name);
					param.setAttribute("value", value);
					return param;
				}
				
				//Work out the URL to the cover image and the document
				var img_cover = '</xsl:text><xsl:value-of select="/page/pageResponse/collection/metadataList/metadata[@name='httpPath']"/>/index/assoc/<xsl:value-of select="metadataList/metadata[@name='assocfilepath']"/>/cover.jpg<xsl:text disable-output-escaping="yes">';
				var doc_url = document.URL; 
				doc_url = doc_url.replace(/(&amp;|\?)book=[a-z]+/gi,'');
				doc_url += '&amp;book=flashxml';	
				
				//The outer OBJECT element
				var objectElem = document.createElement("OBJECT");
				objectElem.setAttribute("align", "middle");
				objectElem.setAttribute("classid", "clsid:d27cdb6e-ae6d-11cf-96b8-444553540000");
				objectElem.setAttribute("codebase", "http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=8,0,0,0");
				objectElem.setAttribute("height", "600px");
				objectElem.setAttribute("width", "100%");
				
				//Parameter list
				var params = new Array();
				params[0] = createParam("allowScriptAccess", "always");
				params[1] = createParam("movie", "Book.swf?src_image=" + escape(img_cover) + "&amp;doc_url=" + escape(doc_url));
				params[2] = createParam("quality", "high");
				params[3] = createParam("bgcolor", "#FFFFFF");
				
				//The embed element that goes into the object element
				var embedElem = document.createElement("EMBED");
				embedElem.setAttribute("allowScriptAccess", "always");
				embedElem.setAttribute("swLiveConnect", "true");
				embedElem.setAttribute("bgcolor", "#FFFFFF");
				embedElem.setAttribute("height", "600px");
				embedElem.setAttribute("name", "Book");
				embedElem.setAttribute("pluginspage", "http://www.macromedia.com/go/getflashplayer");
				embedElem.setAttribute("quality", "high");
				embedElem.setAttribute("src", "Book.swf?src_image=" + escape(img_cover) + "&amp;doc_url=" + escape(doc_url));
				embedElem.setAttribute("type", "application/x-shockwave-flash");
				embedElem.setAttribute("width", "100%");
				
				//Append the param and embed elements to the object element
				for(var i = 0; i &lt; params.length; i++)
				{
					objectElem.appendChild(params[i]);
				}
				objectElem.appendChild(embedElem);
				
				//Append the object element to the page
				var flashDiv = document.getElementById("bookdiv");
				flashDiv.appendChild(objectElem);
			</xsl:text>
		</script>
	</xsl:template>
</xsl:stylesheet>