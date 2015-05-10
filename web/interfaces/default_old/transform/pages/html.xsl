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
	<xsl:template name="pageTitle"><gslib:collectionName/></xsl:template>

	<!-- set page breadcrumbs -->
	<xsl:template name="breadcrumbs"><gslib:siteLink/><gslib:rightArrow/></xsl:template>

	<!-- the page content -->
	<xsl:template match="/page">
	  <iframe width="100%" height="600" frameborder="0"><xsl:attribute name="src"><xsl:value-of select="/page/pageRequest/paramList/param[@name='url']/@value"/></xsl:attribute>Frame for <xsl:value-of select="/page/pageRequest/paramList/param[@name='url']/@value"/></iframe>
	</xsl:template>


</xsl:stylesheet>  

