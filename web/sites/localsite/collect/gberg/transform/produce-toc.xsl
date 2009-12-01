<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">
  
  <xsl:output method="xml"/>

  <xsl:strip-space elements='*'/>

  <xsl:template match="gutbook | book | acknowledge | frontmatter | bookbody | backmatter | part | chapter | chapheader "><xsl:copy><xsl:for-each select="@*"><xsl:copy/></xsl:for-each><xsl:apply-templates/></xsl:copy></xsl:template>

  <xsl:template match="title | chapnum">
    <xsl:copy-of select="."/>
  </xsl:template>

  <!-- any thing else is ignored -->
  <xsl:template match="*"></xsl:template>
  
</xsl:stylesheet>