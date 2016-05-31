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
	<xsl:template name="breadcrumbs">
		<xsl:if test="/page/pageRequest/paramList/param[@name='group']">
			<gslib:siteLink/>
		</xsl:if>
	</xsl:template>

	<!-- the page content -->
	<xsl:template match="/page/pageResponse">

		<div id="quickSearch">
			<gslib:crossCollectionQuickSearchForm/>
		</div>

		<h2><gslib:selectACollectionTextBar/></h2>

		<xsl:for-each select="collectionList/collection|groupList/group">
                	<xsl:sort data-type="number" select="@position"/>
			<xsl:if test="name() = 'collection'">
                                <gslib:collectionLinkWithImage/>
			</xsl:if>
			<xsl:if test="name() = 'group'">
                                <gslib:groupLinkWithImage/>
			</xsl:if>			
                </xsl:for-each>
	
	        <div style="clear: both; padding-top: 4px; padding-bottom: 4px;"><hr/></div>

		<gslib:serviceClusterList/>

		<xsl:for-each select="serviceList/service[@type='query']">
			<gslib:serviceLink/><br/>
		</xsl:for-each>
		
		<xsl:for-each select="serviceList/service[@type='authen']">
			<gslib:authenticationLink/><br/><br/>
			<gslib:registerLink/><br/>
		</xsl:for-each>

	</xsl:template>

	<xsl:template name="groupLinks">
		<div id="groupLinks">
			<!-- <xsl:if test="count(groupList/group) = 0">
				<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home.no_collections')"/>
				<br/>
			</xsl:if>
			-->
			<xsl:for-each select="groupList/group">
				<gslib:groupLinkWithImage/>
			</xsl:for-each>
			<br class="clear"/>
		</div>
	</xsl:template>
	
	<xsl:template name="collectionLinks">
		<div id="collectionLinks">
			<xsl:if test="count(collectionList/collection) = 0">
				<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home.no_collections')"/>
				<br/>
			</xsl:if>
			<xsl:for-each select="collectionList/collection">
				<gslib:collectionLinkWithImage/>
			</xsl:for-each>
			<br class="clear"/>
		</div>
	</xsl:template>

	<xsl:template match="/page/xsltparams">
	  <!-- suppress xsltparam block in page -->
	</xsl:template>

</xsl:stylesheet>


