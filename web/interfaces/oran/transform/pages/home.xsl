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
	<xsl:template name="pageTitle"><gslib:siteName/></xsl:template>

	<!-- set page breadcrumbs -->
	<xsl:template name="breadcrumbs"></xsl:template>

	<!-- the page content -->
	<xsl:template match="/page/pageResponse">

		<div id="quickSearch">
			<gslib:crossCollectionQuickSearchForm/>
		</div>

		<h2><gslib:selectACollectionTextBar/></h2>

		<div id="collectionLinks">
			<xsl:for-each select="collectionList/collection">
				<gslib:collectionLinkWithImage/>
			</xsl:for-each>
			<br class="clear"/>
		</div>


		<gslib:serviceClusterList/>

		<xsl:for-each select="serviceList/service[@type='query']">
			<gslib:serviceLink/><br/>
		</xsl:for-each>
		
		<xsl:for-each select="serviceList/service[@type='authen']">
			<gslib:authenticationLink/><br/>
		</xsl:for-each>

	</xsl:template>

</xsl:stylesheet>


