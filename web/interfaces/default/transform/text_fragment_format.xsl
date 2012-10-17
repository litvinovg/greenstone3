<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xslt="output.xsl"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	extension-element-prefixes="java">
	<xsl:param name="interface_name"/>
	<xsl:param name="library_name"/>
	<xsl:param name="site_name"/>
	<xsl:param name="collName"/>
	<xsl:param name="lang"/>
  
	<xsl:output method="xml"/>
	<xsl:namespace-alias stylesheet-prefix="xslt" result-prefix="xsl"/>

	<!-- With gsf:interfaceText, a user can request a string from the interface dictionaries in the current lang -->
	<xsl:template match="gsf:interfaceText" name="gsf:interfaceText">
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, $lang, @name)"/>
	</xsl:template>

	<!-- With gsf:collectionText, a user can request a string from the collection's dictionary in the current lang -->
	<xsl:template match="gsf:collectionText" name="gsf:collectionText">
		<xsl:copy-of select="util:getCollectionText($collName, $site_name, $lang, @name, @args)"/>
	</xsl:template>

	<xsl:template match="*">
		<xsl:copy>
		<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>


