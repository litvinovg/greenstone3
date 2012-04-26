<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gs3="http://www.greenstone.org/gs3"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  xmlns:gslib="http://www.greenstone.org/skinning"
  xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util gsf">

  <!-- use the 'main' layout -->
  <xsl:import href="layouts/main.xsl"/>

  <xsl:template name="pageTitle">
    <xsl:value-of select="/page/pageResponse/documentNode/metadataList/metadata[@name='root_Title']"/>
  </xsl:template>

  <!-- set page breadcrumbs -->
  <xsl:template name="breadcrumbs">
    <gslib:siteLink/><gslib:rightArrow/> 
    <gslib:collectionNameLinked/><gslib:rightArrow/> 
  </xsl:template>

  <!-- page specific style goes here -->
  <xsl:template name="additionalHeaderContent">
    <link type="text/css" href="sites/localsite/collect/gberg/transform/darwin.css" rel="stylesheet"/>
  </xsl:template>

  <xsl:template match="/page"><xsl:apply-templates select="/page/pageResponse/documentNode"/></xsl:template>

  <xsl:template match="documentNode">
    <xsl:variable name="docname"><xsl:choose><xsl:when test="contains(@nodeID, '.')"><xsl:value-of select="substring-before(@nodeID, '.')"/></xsl:when><xsl:otherwise><xsl:value-of select="@nodeID"/></xsl:otherwise></xsl:choose></xsl:variable>
    <h1>
      <center>
	<a href="{$library_name}?a=xd&amp;sa=toc&amp;c={$collName}&amp;d={$docname}"><xsl:attribute name="title">Go to the document contents</xsl:attribute><xsl:value-of select="metadataList/metadata[@name='root_Title']"/>&#160;</a>
      </center>
    </h1>
    <xsl:apply-templates select="nodeContent/*"/>
  </xsl:template>

  <xsl:template match="gutblurb | markupblurb | endmarkupblurb | endgutblurb">
  </xsl:template>

  <xsl:template match="gutbook | book | frontmatter | bookbody | backmatter | titlepage| htitlepage | toc | dedication | preface | introduction | chapter |  part | chapheader | index | appendix | glossary">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

<!-- dont seem to be able to apply styles to title tags - cos they are special to html. so we rename title to title1-->
  <xsl:template match="title">
    <title1>
      <xsl:apply-templates/>
    </title1>
  </xsl:template>  
    
  <xsl:template match="*">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="metadataList">
  </xsl:template>

</xsl:stylesheet>
