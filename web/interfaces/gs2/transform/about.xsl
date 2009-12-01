<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  xmlns:gslib="http://www.greenstone.org/skinning"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">

  <!-- style includes global params interface_name, library_name -->
  <xsl:include href="style.xsl"/>
  <xsl:include href="help-common.xsl"/>
  <xsl:include href="query-common.xsl"/>
  <xsl:include href="service-params.xsl"/>
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
    <xsl:value-of select="/page/pageResponse/collection/displayItem[@name='name']"/>  
  </xsl:template>

  <!-- page specific style goes here -->
  <xsl:template name="pageStyle"/>


  <xsl:template match="pageResponse">
    <xsl:variable name="collName" select="/page/pageRequest/paramList/param[@name='c']/@value"/>
    <xsl:apply-templates select="collection"><xsl:with-param name="collName" select="$collName"/></xsl:apply-templates>

  </xsl:template>

  <xsl:template match="collection">
    <xsl:param name="collName"/>
    <xsl:call-template name="standardPageBanner">
      <xsl:with-param name="collName" select="$collName"/>
      <xsl:with-param name="pageType">about</xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="navigationBar">
      <xsl:with-param name="collName" select="$collName"/>
    </xsl:call-template>
    <div class="document">
      <xsl:if test="not(/page/pageRequest/paramList/param[@name='qt']) or /page/pageRequest/paramList/param[@name='qt']/@value = 0">
      <xsl:apply-templates select="serviceList/service[@name='TextQuery']">
	<xsl:with-param name="collName" select="$collName"/></xsl:apply-templates>
      <xsl:call-template name="dividerBar"/>
      </xsl:if>
    </div>
    <div>
      <xsl:if test="displayItem[@name='description']">
	<h3><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'about.aboutcoll')"/></h3>
	<p><xsl:value-of select="displayItem[@name='description']" disable-output-escaping='yes'/></p>
      </xsl:if>
      <xsl:apply-templates select="." mode="simplehelp"/>
    </div>	
  </xsl:template>


</xsl:stylesheet>  

