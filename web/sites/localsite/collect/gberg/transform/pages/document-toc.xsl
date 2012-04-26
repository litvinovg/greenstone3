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

  <!-- set page title -->
  <xsl:template name="pageTitle">
    <xsl:value-of select="/page/pageResponse/documentNode/metadataList/metadata[@name='root_Title']"/>: Table of Contents
  </xsl:template>

  <!-- set page breadcrumbs -->
  <xsl:template name="breadcrumbs">
    <gslib:siteLink/><gslib:rightArrow/> 
    <gslib:collectionNameLinked/><gslib:rightArrow/> 
  </xsl:template>
  
  <xsl:template match="/page"><xsl:apply-templates select="/page/pageResponse/documentNode"/></xsl:template>

  <xsl:template match="/page/pageResponse/documentNode">
    <xsl:variable name="docname"><xsl:choose><xsl:when test="contains(@nodeID, '.')"><xsl:value-of select="substring-before(@nodeID, '.')"/></xsl:when><xsl:otherwise><xsl:value-of select="@nodeID"/></xsl:otherwise></xsl:choose></xsl:variable>
    <h1>
      <center>
	<xsl:value-of select="metadataList/metadata[@name='root_Title']"/>
      </center>
    </h1>
    <xsl:apply-templates select="nodeStructure/gutbook/book"><xsl:with-param name="docName" select="$docname"/></xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="book">
    <xsl:param name="docName"/>
    <h1>
      Contents
    </h1>
    <ul class="toc">
      <xsl:if test="acknowledge">
	<li class='toc'>
	  <a href="{$library_name}?a=xd&amp;sa=text&amp;d={$docName}.acknowledge&amp;c={$collName}">Acknowledgements</a>
	</li>
      </xsl:if>
      <xsl:if test="frontmatter">
	<li class='toc'>
	  <a href="{$library_name}?a=xd&amp;sa=text&amp;d={$docName}.frontmatter&amp;c={$collName}">About this document</a>
	</li>
      </xsl:if>
      <li class='toc'><a href="{$library_name}?a=xd&amp;sa=text&amp;d={$docName}&amp;c={$collName}">Full version</a>
      </li>
    </ul>
    <xsl:apply-templates select="bookbody"><xsl:with-param name="docName" select="$docName"/></xsl:apply-templates>
    <xsl:if test="backmatter">
      <ul class='toc'>
	<li class='toc'>
	<a href="{$library_name}?a=xd&amp;sa=text&amp;d={$docName}.backmatter&amp;c={$collName}">Back matter (may include appendices, index, glossary etc)</a>
	</li>
      </ul>
    </xsl:if>
  </xsl:template>
    
  <xsl:template match="bookbody">
    <xsl:param name="docName"/>
    <ul class="toc">
      <xsl:apply-templates select="part|chapter">
	<xsl:with-param name="docName" select="$docName"/>
      </xsl:apply-templates>
    </ul>
  </xsl:template>

  <xsl:template match="part">
    <xsl:param name="docName"/>
    <li class='toc'>
      <a href="{$library_name}?a=xd&amp;sa=text&amp;c={$collName}&amp;d={$docName}.bookbody.part.{@gs3:id}">Part <xsl:value-of select="position()"/></a>
      <xsl:if test="chapter">
	<ul class='toc'>
	  <xsl:apply-templates select="chapter">
	  <xsl:with-param name="docName" select="$docName"/>
	</xsl:apply-templates>
	</ul>
      </xsl:if>
    </li>
  </xsl:template>

  <xsl:template match="chapter">
    <xsl:param name="docName"/>
    <li class='toc'>
      <a href="{$library_name}?a=xd&amp;sa=text&amp;c={$collName}&amp;d={$docName}.bookbody.chapter.{@gs3:id}">
	<xsl:choose>
	  <xsl:when test="title">
	    <xsl:value-of select="title"/>
	  </xsl:when>
	  <xsl:when test="chapheader">
	    <xsl:value-of select="chapheader/chapnum"/>: <xsl:value-of select="chapheader/title"/>
	  </xsl:when>
	  <xsl:otherwise>
	    Chapter <xsl:value-of select="position()"/>
	  </xsl:otherwise>
	</xsl:choose>
      </a>
    </li>
  </xsl:template>
   
</xsl:stylesheet>
