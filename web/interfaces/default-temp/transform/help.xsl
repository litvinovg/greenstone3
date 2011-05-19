<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  extension-element-prefixes="java"
  exclude-result-prefixes="java">
  
  <!-- style includes global params interface_name, library_name -->
  <xsl:include href="style.xsl"/>

  <xsl:output method="html"/>  
  
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
	</div>
		<xsl:call-template name="pageTitle"/>
      </body>
    </html>
  </xsl:template>
  
  <xsl:template name="pageTitle">
    <xsl:choose>
    	<xsl:when test="/page/pageResponse/*/displayItem[@name='name']">
    		<span>
    			<xsl:attribute name="class">
					<xsl:text>getTextFor null document.title.text:'</xsl:text>
    				<xsl:value-of select="/page/pageResponse/*/displayItem[@name='name']"/>
					<xsl:text>'</xsl:text>
    			</xsl:attribute>
    		</span>
    	</xsl:when>
    	<xsl:otherwise>
    		<span class="getTextFor null document.title.gsdl">&amp;amp;nbsp;</span>
    	</xsl:otherwise>
    </xsl:choose>
    <span class="getTextFor null document.title.text:'&#160;:&#160;'[a],document.title.help_t[a]">&amp;amp;nbsp;</span>
  </xsl:template>

  <!-- page specific style goes here -->
  <xsl:template name="pageStyle"/>


  <xsl:template match="pageResponse">
    <xsl:variable name="collName" select="/page/pageRequest/paramList/param[@name='c']/@value"/>
    <xsl:call-template name="standardPageBanner">
      <xsl:with-param name="collName" select="$collName"/>
      <xsl:with-param name="pageType">help</xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="navigationBar">
      <xsl:with-param name="collName" select="$collName"/>
    </xsl:call-template>      
    
    some help text goes here
  </xsl:template>
  
</xsl:stylesheet>
