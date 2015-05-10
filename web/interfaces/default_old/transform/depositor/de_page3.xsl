<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util">
	
	<!-- set page title -->
	<xsl:variable name="title">Confirmation</xsl:variable>

	<!-- the page content -->
	<xsl:template name="wizardPage">
		Filename: <span gs-metadata="ex.Filename"><xsl:text> </xsl:text></span><br/>
		File size: <span gs-metadata="ex.Filesize"><xsl:text> </xsl:text></span>
	</xsl:template>
</xsl:stylesheet>  

