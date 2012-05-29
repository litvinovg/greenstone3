<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  extension-element-prefixes="java">
  
  <!-- style includes global params interface_name, library_name -->
  <xsl:include href="style.xsl"/>
  
  <!-- the main page layout template is here -->
  <xsl:template match="page">
    <html>
      <head>
	<title>
	  <!-- put a space in the title in case the actual value is missing - mozilla will not display a page with no title-->
	  <xsl:text> </xsl:text>
	</title>
	<xsl:call-template name="globalStyle"/>
	<xsl:call-template name="pageStyle"/>
      </head>
      <body>
	<xsl:attribute name="dir"><xsl:call-template name="direction"/></xsl:attribute>
	<div id="page-wrapper">
	  <xsl:call-template name="response" />
	  <xsl:call-template name="greenstoneFooter"/>
	  <xsl:call-template name="pageTitle"/>
	</div>
      </body>
    </html>
  </xsl:template>
  
  <xsl:template name="pageTitle">
    <span class="getTextFor null document.title.gsdl">&amp;nbsp;</span>
  </xsl:template>
  
  <!-- page specific style goes here -->
  <xsl:template name="pageStyle"/>
  
  <xsl:template match="pageResponse">
    <xsl:value-of select="status"/>
  </xsl:template>
  
</xsl:stylesheet>