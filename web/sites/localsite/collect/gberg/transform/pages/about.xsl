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
  
		<!--Display the description text of the current collection,
		and if some services are available then create a list
		of links for each service within a <ul id="servicelist"> element.-->
            <gslib:collectionDescriptionTextAndServicesLinks/>
            
  </xsl:template>

  <!-- override this to include documentList -->
  <xsl:template match="collection" priority="5">
    <xsl:value-of select="displayItem[@name='description']" disable-output-escaping="yes"/>
    <xsl:apply-templates select="serviceList">
      <xsl:with-param name="collName" select="$collName"/>
    </xsl:apply-templates>
    <p/><xsl:apply-templates select="documentList">
      <xsl:with-param name="collName" select="@name"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="documentList">
    <xsl:param name="collName"/>
    <ul>
      <xsl:for-each select="document">
	<li><a href="{$library_name}?a=xd&amp;sa=toc&amp;sc=full&amp;c={$collName}&amp;d={@name}"><xsl:value-of select="metadataList/metadata[@name='Title']"/></a></li>
      </xsl:for-each>
    </ul>
  </xsl:template>

</xsl:stylesheet>  

