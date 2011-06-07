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
	  <xsl:call-template name="response" />
	  <xsl:call-template name="greenstoneFooter"/>
	</div>
      </body>
    </html>
  </xsl:template>
  
  <xsl:variable name="berrybasketswitch"><xsl:value-of select="/page/pageRequest/paramList/param[@name='berrybasket']/@value"/></xsl:variable> 

  <xsl:template name="pageTitle">
    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'gsdl')"/>
  </xsl:template>

  <!-- page specific style goes here -->
  <xsl:template name="pageStyle">
    <xsl:if test="$berrybasketswitch = 'on'">
      <xsl:call-template name="berryStyleSheet"/>  
      <xsl:call-template name="js-library"/>
    </xsl:if>
  </xsl:template>


  <xsl:template match="pageResponse">
    <xsl:variable name="collName"><xsl:value-of select="/page/pageRequest/paramList/param[@name='c']/@value"/></xsl:variable>
    <xsl:variable name="serviceName"><xsl:value-of select="service/@name"/></xsl:variable>
    <xsl:call-template name="standardPageBanner">
      <xsl:with-param name="collName" select="$collName"/>
    </xsl:call-template>

    <xsl:call-template name="navigationBar">
      <xsl:with-param name="collName" select="$collName"/>
    </xsl:call-template>      
  
    <!-- Sam's div code -->

    <!-- <xsl:if test="/page/pageRequest[@name='output']='debug'"> -->

    <xsl:variable name="fmt">
	<xsl:call-template name="xml-to-string">
            <xsl:with-param name="node-set" select="//format[@type='browse']"/>
          </xsl:call-template>
    </xsl:variable>

    <!-- <xsl:variable name="tok" select="fn:tokenize($fmt,'/s+')"/> -->

     <!--<div id="format">
        <p>
	  <b>Format string here</b>
	  <i>
	      <xsl:value-of select="$fmt"/>-->
	      <!-- <xsl:value-of select="$tok"/> -->
	  <!--</i>
        </p>
      </div>-->
    <!-- </xsl:if> -->

  
    <div id="content">
      <xsl:apply-templates select="service/classifierList">
        <xsl:with-param name="collName" select="$collName"/>
        <xsl:with-param name="serviceName" select="$serviceName"/>
      </xsl:apply-templates>
      
      <xsl:if test="$berrybasketswitch = 'on'">
	<xsl:call-template name="berrybasket" />
      </xsl:if>  
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






