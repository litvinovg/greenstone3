<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">
  
  <!-- style includes global params interface_name, library_name -->
  <xsl:include href="style.xsl"/>
  
  <xsl:output method="html"/>  
  
  <!-- the main page layout template is here -->
  <xsl:template match="page">
    <html>
      <head>
	<title>
	  <!-- put a space in the title in case the actual value is missing - mozilla will not display a page with no title-->
	  <xsl:call-template name="pageTitle"/><xsl:text> </xsl:text>
	</title>
	<xsl:call-template name="globalStyle"/>
	<xsl:call-template name="pageStyle"/>
      </head>
      <body>
	<xsl:attribute name="dir"><xsl:call-template name="direction"/></xsl:attribute>
	<div id="page-wrapper">
	  <xsl:apply-templates select="pageResponse"/>
	  <xsl:call-template name="greenstoneFooter"/>                	   
	</div>
      </body>
    </html>
  </xsl:template>

  <xsl:template name="pageTitle">
    <xsl:value-of select="pageResponse/service/applet"/>
  </xsl:template>

  <!-- page specific style goes here -->
  <xsl:template name="pageStyle"/>

  <xsl:template match="pageResponse">
    <xsl:variable name="collName"><xsl:value-of select="../pageRequest/paramList/param[@name='c']/@value"/></xsl:variable>
    <center>
      <xsl:call-template name="standardPageBanner">
	<xsl:with-param name="collName" select="$collName"/>
	<xsl:with-param name="pageType"><xsl:choose><xsl:when test="service[@name='PhindApplet']">phrases</xsl:when><xsl:otherwise>applet</xsl:otherwise></xsl:choose></xsl:with-param>
      </xsl:call-template>
      <p/>
      <xsl:call-template name="navigationBar">
	<xsl:with-param name="collName" select="$collName"/>
      </xsl:call-template>      
      <!--      <xsl:call-template name="dividerBar">
      <xsl:with-param name="text" select="service/displayItem[@name='name']"/>
    </xsl:call-template>-->
      <p/>
      <xsl:copy-of select="service/applet"/>
      <xsl:call-template name="dividerBar"/>
    </center>
  </xsl:template>
</xsl:stylesheet>  

