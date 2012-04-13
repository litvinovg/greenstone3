<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">
  
  <!-- style includes global params interface_name, library_name -->
  <xsl:include href="style.xsl"/>
  <xsl:include href="classifiertools.xsl"/>
  <!-- classifiertools needs this-->
  <xsl:include href="berrytools.xsl"/>
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
    <xsl:variable name="serviceName"><xsl:value-of select="service/@name"/></xsl:variable>
    <xsl:variable name="cl" select="/page/pageRequest/paramList/param[@name='cl']/@value"/>
    <xsl:variable name="clTop">
      <xsl:choose>
	<xsl:when test="contains($cl, '.')"><xsl:value-of select="substring-before($cl, '.')"/></xsl:when>
	<xsl:otherwise>
	  <xsl:value-of select="$cl"/>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:call-template name="standardPageBanner">
      <xsl:with-param name="collName" select="$collName"/>
      <xsl:with-param name="pageType">browse</xsl:with-param>
      <xsl:with-param name="clTop" select="$clTop"/>
    </xsl:call-template>
    <xsl:call-template name="navigationBar">
      <xsl:with-param name="collName" select="$collName"/>
      <xsl:with-param name="clTop" select="$clTop"/>
    </xsl:call-template>      
    <div class="document">
      <xsl:apply-templates select="classifier">
	<xsl:with-param name="collName" select="$collName"/>
	<xsl:with-param name="serviceName" select="$serviceName"/>
      </xsl:apply-templates>
    </div>
  </xsl:template>

    <xsl:template match="classifierList">
    <xsl:param name="collName"/>
    <xsl:param name="serviceName"/>
    <xsl:variable name="selected" select="/page/pageResponse/classifier/@name"/>
    <ul id="classifierlist">
      <xsl:for-each select="classifier">
	<xsl:choose>
	  <xsl:when test="@name=$selected">
	    <li id="activeclassifier"><xsl:value-of select="displayItem[@name='name']"/></li>
	  </xsl:when>
	  <xsl:otherwise>
	    <li><a href="{$library_name}?a=b&amp;rt=r&amp;s={$serviceName}&amp;c={$collName}&amp;cl={@name}"><xsl:value-of select="displayItem[@name='name']"/></a></li></xsl:otherwise></xsl:choose>
	
      </xsl:for-each>
    </ul>
  </xsl:template>


</xsl:stylesheet>






