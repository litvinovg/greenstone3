<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util">

  <xsl:variable name="groupPath"><xsl:value-of select="/page/pageRequest/paramList/param[@name='group']/@value"/></xsl:variable>

  
	<!-- use the 'main' layout -->
	<xsl:include href="layouts/main.xsl"/>

	<!-- set page title -->
	<xsl:template name="pageTitle"><xsl:choose><xsl:when test="$groupPath != ''"><gslib:groupName path="{$groupPath}"/></xsl:when><xsl:otherwise><gslib:siteName/></xsl:otherwise></xsl:choose></xsl:template>

	<!-- set page breadcrumbs -->
	<xsl:template name="breadcrumbs">
		<xsl:if test="$groupPath != ''">
			<gslib:siteLink/>
			<xsl:for-each select="/page/pageResponse/pathList/group">
				<xsl:sort data-type="number" select="@position"/>
				<gslib:rightArrow/>
				<xsl:if test="position() != last()">
				  <!-- don't want the current group in the breadcrumbs -->
				  <a>
				    <xsl:attribute name="href"><gslib:groupHref path="{@path}"/></xsl:attribute>				
				    <xsl:attribute name="title"><gslib:groupName path="{@path}"/></xsl:attribute>
				    <gslib:groupName path="{@path}"/>
				  </a>
				</xsl:if>
			</xsl:for-each>
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
		<xsl:variable name="siteDesc"><xsl:choose><xsl:when test="$groupPath != ''"><gslib:groupDescription path="{$groupPath}"/></xsl:when><xsl:otherwise><gslib:siteDescription/></xsl:otherwise></xsl:choose></xsl:variable>
		<xsl:if test="$siteDesc != ''">
		  <xsl:value-of select="$siteDesc"/>
		  <div style="clear: both; padding-top: 4px; padding-bottom: 4px;"><hr/></div>
		</xsl:if>
		<gslib:serviceClusterList/>

		<xsl:for-each select="serviceList/service[@type='query']">
			<gslib:serviceLink/><br/>
		</xsl:for-each>
		
		<xsl:for-each select="serviceList/service[@type='authen']">
			<gslib:authenticationLink/><br/><br/>
			<gslib:registerLink/><br/>
		</xsl:for-each>

	</xsl:template>


	<xsl:template match="/page/xsltparams">
	  <!-- suppress xsltparam block in page -->
	</xsl:template>

</xsl:stylesheet>


