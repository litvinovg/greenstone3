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

		<!-- show the classifiers if more than one (title, subject, etc.) -->
		<xsl:if test="service/classifierList/classifier[2]">
			<div id="classifierList" class="navList">
				<ul>
					<xsl:for-each select="service/classifierList/classifier">
						<li><gslib:classifierLink/></li>
					</xsl:for-each>
				</ul>
				<div class="clear"><xsl:text> </xsl:text></div>
			</div>
		</xsl:if>

		<!-- this right sidebar -->
		<div id="rightSidebar">
			<!-- show the berry basket if it's turned on -->
			<gslib:berryBasket/>
			<xsl:text> </xsl:text>
		</div>
	
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
				<xsl:attribute name="href"><xsl:value-of select="$library_name"/>?a=d&amp;c=<xsl:value-of select="/page/pageResponse/collection/@name"/>&amp;d=<xsl:value-of select="@nodeID"/>&amp;dt=<xsl:value-of select="@docType"/>&amp;p.a=b&amp;p.s=<xsl:value-of select="/page/pageResponse/service/@name"/></xsl:attribute>
				<xsl:value-of disable-output-escaping="yes"  select="metadataList/metadata[@name='Title']"/>
			</a>
			<xsl:call-template name="documentBerryForClassifierOrSearchPage"/>

		</li>

	</xsl:template>


	<!--
	TEMPLATE FOR GROUPS OF DOCUMENTS
	-->
	<xsl:template match="classifierNode" priority="3">

		<li class="shelf">

			<a>
				<xsl:attribute name="href">
					<xsl:value-of select="$library_name"/>?a=b&amp;rt=r&amp;s=<xsl:value-of select="/page/pageResponse/service/@name"/>&amp;c=<xsl:value-of select="/page/pageResponse/collection/@name"/>&amp;cl=<xsl:value-of select='@nodeID'/><xsl:if test="classifierNode|documentNode">.pr</xsl:if></xsl:attribute>
				<xsl:value-of disable-output-escaping="yes"  select="metadataList/metadata[@name='Title']"/>
			</a>

			<!-- show any documents or sub-groups in this group -->
			<xsl:if test="documentNode|classifierNode">
				<ul>
					<xsl:apply-templates select="documentNode|classifierNode"/>
				</ul>
			</xsl:if>
		</li>

	</xsl:template>

</xsl:stylesheet>

