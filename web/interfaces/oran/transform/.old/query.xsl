<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util">
	
	<xsl:output method="html"/>

	<!-- the main page layout template is here -->
	<xsl:template match="/page/pageResponse">
		<xsl:for-each select="/">
			
		</xsl:for-each>

		<html>
			<head>

				<!-- put a space in the title in case the actual value is missing - mozilla will not display a page with no title-->
				<title><xsl:call-template name="pageTitle"/><xsl:text> </xsl:text></title>

				<link rel="stylesheet" href="interfaces/oran/style/core.css" type="text/css"/>

				<!-- todo: add berry basket stylesheets and javascript -->
			</head>

			<body><gslib:textDirectionAttribute/>

				<div id="container">
				    <xsl:variable name="collName"><xsl:value-of select="/page/pageRequest/paramList/param[@name='c']/@value"/></xsl:variable> 
				    <xsl:variable name="requesttype"><xsl:value-of select="/page/pageRequest/paramList/param[@name='rt']/@value"/></xsl:variable> 
					<xsl:variable name="this-element" select="/page/pageResponse/collection|/page/pageResponse/serviceCluster"/>
					<div id="banner">
						<p>
							<a href="{$library_name}?a=p&amp;amp;sa=about&amp;amp;c={$collName}">
								<xsl:choose>
									<xsl:when test="$this-element/displayItem[@name='icon']">
										<img>
											<xsl:attribute name="src"><xsl:value-of select="$this-element/metadataList/metadata[@name='httpPath']"/>/images/<xsl:value-of select="$this-element/displayItem[@name='icon']"/></xsl:attribute>
											<xsl:attribute name="alt"><xsl:value-of select="$this-element/displayItem[@name='name']"/></xsl:attribute>
											<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'aboutpage')"/></xsl:attribute>
										</img>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="$this-element/displayItem[@name='name']"/>
									</xsl:otherwise>
								</xsl:choose>
							</a>
						</p>

						<ul id="bannerlist"> 

							<!-- home -->
							<li>
								<a href="{$library_name}?a=p&amp;amp;sa=home">
									<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home_tip')"/></xsl:attribute>
									<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home_b')"/>
							    </a>
							</li>

							<!-- help -->
							<li>
								<a href="{$library_name}?a=p&amp;amp;sa=help&amp;amp;c={$collName}">
									<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_tip')"/></xsl:attribute>
								    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_b')"/>
								</a>
							</li>

							<!-- preferences -->
							<li>
								<a href="{$library_name}?a=p&amp;amp;sa=pref&amp;amp;c={$collName}">
									<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_tip')"/></xsl:attribute>
							    	<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_b')"/>
								</a>
							</li>

						</ul>

					</div>

<!--
				    <xsl:call-template name="navigationBar">
				      <xsl:with-param name="collName" select="$collName"/>
				    </xsl:call-template>
-->

					    <div id="content">

					    	<xsl:apply-templates select="/page/pageResponse/service"/>

							<xsl:if test="contains($requesttype, 'r')">
								<xsl:call-template name="query-response">
									<xsl:with-param name="collName" select="$collName"/>
								</xsl:call-template>
							</xsl:if>
					    </div>

				    <xsl:if test="descendant::error">
				      <script language="Javascript">
					<xsl:text disable-output-escaping="yes">
					  function removeAllChildren(node) {
					    while (node.hasChildNodes()) {
					      node.removeChild(node.firstChild);
					    }
					  }

					  function toggleHideError(obj) {
					    if (obj.style.display == "none") {
					      obj.style.display = "";
					      hide_link = document.getElementById("hide");
					      removeAllChildren(hide_link);
					      hide_link.appendChild(document.createTextNode("</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'hide_error')"/><xsl:text disable-output-escaping="yes">"));
					    } else {
					      obj.style.display = "none";
					      hide_link = document.getElementById("hide");
					      removeAllChildren(hide_link);
					      hide_link.appendChild(document.createTextNode("</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'show_error')"/><xsl:text disable-output-escaping="yes">"));
					    } 
					  }
					</xsl:text>
				      </script>
				      <p align='right'><a id="hide" href="javascript:toggleHideError(error);"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'show_error')"/></a></p>
				      <div id="error" style="display: none;">
					<xsl:apply-templates select="descendant::error"/>
				      </div>
				    </xsl:if>


					<xsl:call-template name="greenstoneFooter"/>
				</div>
			</body>

		</html>
	</xsl:template>

</xsl:stylesheet>
