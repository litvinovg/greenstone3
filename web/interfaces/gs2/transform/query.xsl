<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">

  <!-- style includes global params interface_name, library_name -->
  <xsl:include href="style.xsl"/>
  <xsl:include href="service-params.xsl"/>
  <xsl:include href="querytools.xsl"/>
  <!-- querytools uses this -->
  <xsl:include href="berrytools.xsl"/>
  <xsl:include href="query-common.xsl"/>  
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
    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'gsdl')"/>
  </xsl:template>

  <!-- page specific style goes here -->
  <xsl:template name="pageStyle"/>


  <xsl:template match="pageResponse">
    <xsl:variable name="collName"><xsl:value-of select="/page/pageRequest/paramList/param[@name='c']/@value"/></xsl:variable>

    <xsl:call-template name="standardPageBanner">
      <xsl:with-param name="collName" select="$collName"/>
      <xsl:with-param name="pageType">search</xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="navigationBar">
      <xsl:with-param name="collName" select="$collName"/>
    </xsl:call-template>      
    <div class="document">
      <xsl:apply-templates select="service">
	<xsl:with-param name="collName" select="$collName"/>
      </xsl:apply-templates>
      <xsl:if test="documentNodeList">
	<xsl:call-template name="query-response">
	  <xsl:with-param name="collName" select="$collName"/>
	</xsl:call-template>
      </xsl:if>
    </div>
  </xsl:template>
  
  <xsl:template name="query-response">
    <xsl:param name="collName"/>
    <xsl:call-template name="dividerBar">
      <xsl:with-param name='text'><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.results')"/></xsl:with-param></xsl:call-template>

    <!-- If query term information is available, display it -->
    <xsl:call-template name="termInfo"/>    
    <!-- If the number of matching documents is known, display it -->
    <xsl:call-template name="matchDocs"/>
    
    <!-- Display the matching documents -->
    <xsl:call-template name="resultList">
      <xsl:with-param name="collName" select="$collName"/>
    </xsl:call-template>
    <!-- next and prev links at bottom of page -->
    <xsl:call-template name="resultNavigation">
      <xsl:with-param name="collName" select="$collName"/>
    </xsl:call-template>	
  </xsl:template>

</xsl:stylesheet>  
