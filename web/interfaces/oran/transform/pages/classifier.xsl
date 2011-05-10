<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util">
	
	<!-- use the 'main' layout -->
	<xsl:include href="layouts/main.xsl"/>

	<!-- set page title -->
	<xsl:template name="pageTitle"><gslib:serviceName/></xsl:template>

	<!-- set page breadcrumbs -->
	<xsl:template name="breadcrumbs"><gslib:siteLink/><gslib:rightArrow/> <gslib:collectionNameLinked/><gslib:rightArrow/></xsl:template>

	<!-- the page content -->
	<xsl:template match="/page/pageResponse">
		<xsl:call-template name="classifierLoadScript"/>
		<!-- this right sidebar -->
		<xsl:if test="$berryBasketOn">
			<div id="rightSidebar">
				<!-- show the berry basket if it's turned on -->
				<gslib:berryBasket/>
				<xsl:text> </xsl:text>
			</div>
		</xsl:if>
	
		<!--
			show the clasifier results - 
			you can change the appearance of the results by editing
			the two templates at the bottom of this file
		-->
		<ul id="results">
			<xsl:apply-templates select="classifier/*"/>
		</ul>
		<div class="clear"><xsl:text> </xsl:text></div>

	</xsl:template>


	<!--
	TEMPLATE FOR DOCUMENTS
	-->
	<xsl:template match="documentNode" priority="3">

		<!-- show the document details -->
		<li class="document">

			<a>
				<xsl:attribute name="href"><xsl:value-of select="$library_name"/>?a=d&amp;c=<xsl:value-of select="/page/pageResponse/collection/@name"/>&amp;d=<xsl:value-of select="@nodeID"/>&amp;dt=<xsl:value-of select="@docType"/>&amp;p.a=b&amp;p.s=<xsl:value-of select="/page/pageResponse/service/@name"/>&amp;ed=1</xsl:attribute>
				<xsl:value-of disable-output-escaping="yes"  select="metadataList/metadata[@name='Title']"/>
			</a>
			<xsl:call-template name="documentBerryForClassifierOrSearchPage"/>

		</li>

	</xsl:template>


	<!--
	TEMPLATE FOR GROUPS OF DOCUMENTS
	-->
	<xsl:template match="classifierNode" priority="3">

		<table id="title{@nodeID}"><tr>
			<!-- Expand/collapse button -->
			<td class="headerTD">
				<img id="toggle{@nodeID}" onclick="toggleSection('{@nodeID}');" class="icon">			
					<xsl:attribute name="src">
						<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'expand_image')"/>
					</xsl:attribute>
				</img>
			</td>
			<!-- Bookshelf icon -->
			<td>
				<img>
					<xsl:attribute name="src"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'bookshelf_image')"/></xsl:attribute>
				</img>
			</td>
			<!-- Link title -->
			<td>
				<a href="javascript:toggleSection('{@nodeID}');">
					<xsl:value-of disable-output-escaping="yes"  select="metadataList/metadata[@name='Title']"/>
				</a>
			</td>
		</tr></table>
		
		<!-- Show any documents or sub-groups in this group -->
		<xsl:if test="documentNode|classifierNode">
			<div id="div{@nodeID}" class="classifierContainer" style="display:block;">
				<xsl:apply-templates select="documentNode|classifierNode"/>
			</div>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="classifierLoadScript">
		<script type="text/javascript">
			<xsl:text disable-output-escaping="yes">
				var collapseImageURL = "</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'collapse_image')"/><xsl:text disable-output-escaping="yes">";
				var expandImageURL = "</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'expand_image')"/><xsl:text disable-output-escaping="yes">";
				var loadingImageURL = "</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'loading_image')"/><xsl:text disable-output-escaping="yes">";
				var inProgress = new Array();
			
				function isExpanded(sectionID)
				{
					var divElem = document.getElementById("div" + sectionID);
					if(divElem.style.display == "block")
					{
						return true;
					}
					return false;
				}
			
				function toggleSection(sectionID)
				{
					var section = document.getElementById("div" + sectionID);
					var sectionToggle = document.getElementById("toggle" + sectionID);
					
					if(section)
					{
						if(isExpanded(sectionID))
						{
							section.style.display = "none";
							sectionToggle.setAttribute("src", expandImageURL);
						}
						else
						{
							section.style.display = "block";
							sectionToggle.setAttribute("src", collapseImageURL);
						}
					}
					else
					{
						httpRequest(sectionID);
					}
				}
				
				function httpRequest(sectionID)
				{
					if(!inProgress[sectionID])
					{
						inProgress[sectionID] = true;
						var httpRequest;
						if (window.XMLHttpRequest) {
							httpRequest = new XMLHttpRequest();
						}
						else if (window.ActiveXObject) {
							httpRequest = new ActiveXObject("Microsoft.XMLHTTP");
						}
						
						var sectionToggle = document.getElementById("toggle" + sectionID);
						sectionToggle.setAttribute("src", loadingImageURL);

						var url = document.URL;
						url = url.replace(/(&amp;|\?)cl=([a-z\.0-9]+)/gi, "$1cl=" + sectionID + "&amp;excerptid=div" + sectionID);

						httpRequest.open('GET', url, true);
						httpRequest.onreadystatechange = function() 
						{
							if (httpRequest.readyState == 4) 
							{
								if (httpRequest.status == 200) 
								{
									var newDiv = document.createElement("div");			
									var sibling = document.getElementById("title" + sectionID);
									var parent = sibling.parentNode;
									
									if(sibling.nextSibling)
									{
										parent.insertBefore(newDiv, sibling.nextSibling);
									}
									else
									{
										parent.appendChild(newDiv);
									}

									newDiv.innerHTML = httpRequest.responseText;
									sectionToggle.setAttribute("src", collapseImageURL);
								}
								else
								{
									sectionToggle.setAttribute("src", expandImageURL);
								}
								inProgress[sectionID] = false;
							}
						}
						httpRequest.send();
					}
				}
			</xsl:text>
		</script>
	</xsl:template>

</xsl:stylesheet>

