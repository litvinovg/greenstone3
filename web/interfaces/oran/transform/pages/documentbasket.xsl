<?xml version="1.0" encoding="ISO-8859-1"?><xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util gsf">

	<!-- use the 'main' layout -->
	<xsl:include href="layouts/main.xsl"/>
	
	<!-- set page title -->
	<xsl:template name="pageTitle">Document Basket</xsl:template>
	
	<!-- set page breadcrumbs -->
	<xsl:template name="breadcrumbs"><gslib:siteLink/><gslib:rightArrow/> 
	<gslib:collectionNameLinked/><gslib:rightArrow/></xsl:template>

	<!-- the page content -->
	<xsl:template match="/page">
		<xsl:for-each select="//item">
			<input type="checkbox" align="center">
				<xsl:attribute name="id">
					<xsl:text>doc</xsl:text>
					<xsl:value-of select="position()"/>
				</xsl:attribute>
				
				<xsl:attribute name="value">
					<xsl:value-of select="@name"/>
				</xsl:attribute>
			</input>
			<xsl:value-of select="position()"/><xsl:text>. </xsl:text>
			
			<a>  
				<xsl:attribute name="href">   
					<xsl:value-of select="$library_name"/>?a=d&amp;c=<xsl:value-of select="/page/pageResponse/collection/@name"/>&amp;d=<xsl:value-of select="@name"/>&amp;p.a=b&amp;p.s=<xsl:value-of select="/page/pageResponse/service/@name"/>&amp;ed=1
				</xsl:attribute>
				<xsl:value-of select="@title"/>
			</a>
			<br />
		</xsl:for-each>
	</xsl:template>

</xsl:stylesheet>  



