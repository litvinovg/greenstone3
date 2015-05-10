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
	<xsl:template name="pageTitle">The Phind Applet<!--<gslib:collectionName/><xsl:value-of select="/page/pageResponse/service/applet"/>--></xsl:template>

	<!-- set page breadcrumbs -->
	<xsl:template name="breadcrumbs"><gslib:siteLink/><gslib:rightArrow/><gslib:collectionNameLinked/><gslib:rightArrow/></xsl:template>

	<!-- the main page layout template is here -->
	<xsl:template match="/page/pageResponse">
    	<div class="phrasebrowse">
			<xsl:copy-of select="service/applet"/>
		</div>
	</xsl:template>
</xsl:stylesheet>  

