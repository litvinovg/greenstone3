<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util gsf">


  <!-- identity transform -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- rename Section to documentNode -->
  <xsl:template match="Section">
    <documentNode><xsl:apply-templates select="@*|node()" /></documentNode>
  </xsl:template>
  <!-- rename Description to metadataList -->
  <xsl:template match="Description">
    <metadataList><xsl:apply-templates select="@*|node()" /></metadataList>
  </xsl:template>
  <!-- rename Content to nodeContent -->
    <xsl:template match="Content">
    <nodeContent><xsl:apply-templates select="@*|node()" /></nodeContent>
  </xsl:template>

</xsl:stylesheet>
