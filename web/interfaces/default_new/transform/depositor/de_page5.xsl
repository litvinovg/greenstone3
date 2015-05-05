<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util">
	
	<!-- set page title -->
	<xsl:variable name="title">View Collection</xsl:variable>

	<!-- the page content -->
	<xsl:template name="wizardPage">
		<div class="ui-state-default ui-corner-all">
		<a>
			<xsl:attribute name="href"><xsl:value-of select="$library_name"/>/collection/<xsl:value-of select="/page/pageResponse/collection/@name"/>/page/about</xsl:attribute>
			View your collection
		</a>
		</div>
	</xsl:template>
</xsl:stylesheet>  

