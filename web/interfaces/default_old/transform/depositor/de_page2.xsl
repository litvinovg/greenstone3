<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util">
	
	<!-- set page title -->
	<xsl:variable name="title">Select File</xsl:variable>

	<!-- the page content -->
	<xsl:template name="wizardPage">
		<table>
			<tr>
				<td>Filename:</td><td><input type="file" name="userfile"/></td>
			</tr>
		</table>
	</xsl:template>
</xsl:stylesheet>  

