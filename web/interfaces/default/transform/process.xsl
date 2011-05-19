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

  <xsl:template name="pageTitle">
    <xsl:value-of select="/page/pageResponse/serviceCluster/metadataList/metadata[@name='Title']"/>
  </xsl:template>

  <!-- page specific style goes here -->
  <xsl:template name="pageStyle"/>


  <xsl:template match="pageResponse">
    <xsl:variable name="clusterName"><xsl:value-of select="/page/pageRequest/paramList/param[@name='c']/@value"/></xsl:variable>
    <center>
      <xsl:call-template name="standardPageBanner">
	<xsl:with-param name="collName" select="$clusterName"/>
      </xsl:call-template>
      <xsl:call-template name="navigationBar">
	<xsl:with-param name="collName" select="$clusterName"/>
      </xsl:call-template>      
      <xsl:apply-templates select="service">
	<xsl:with-param name="clusterName" select="$clusterName"/>
      </xsl:apply-templates>
      <xsl:apply-templates select="status">
	<xsl:with-param name="clusterName" select="$clusterName"/>
      </xsl:apply-templates>
    </center>
  </xsl:template>

  <xsl:template match="service">
    <xsl:param name="clusterName"/>
    <xsl:variable name='subaction' select="/page/pageRequest/@subaction"/>
    <xsl:variable name='action' select="/page/pageRequest/@action"/>
    <xsl:variable name='lang' select="/page/@lang"/>
    <p/>
    <form name="BuildForm" method="get" action="{$library_name}">
      <xsl:apply-templates select="paramList"/>
      <input type="hidden" name="a" value="{$action}"/>
      <input type="hidden" name="c" value="{$clusterName}"/>
      <input type="hidden" name="sa" value="p"/>
      <input type="hidden" name='l' value='{$lang}'/>
      <input type="hidden" name="s" value="{@name}"/>
      <input type="hidden" name="rt" value="r"/>
      <input type="submit"><xsl:attribute name="value"><xsl:value-of select="displayItem[@name='submit']"/></xsl:attribute></input>
    </form>
    <xsl:call-template name="dividerBar"/>
  </xsl:template>
  
  <xsl:template match="status">
    <xsl:param name="clusterName"/>
    <center/>
    <applet code="org.greenstone.gsdl3.build.StatusDisplay.class" codebase='lib' archive='gsdl3.jar, xercesImpl.jar, xml-apis.jar' width='600' height='150'>The status display applet.
      <param name='library'><xsl:attribute name="value"><xsl:value-of select='$library_name'/>?a=pr&amp;rt=s&amp;c=<xsl:value-of select='$clusterName'/>&amp;s=<xsl:value-of select='/page/pageResponse/service/@name'/>&amp;o=xml&amp;ro=1&amp;l=<xsl:value-of select='/page/@lang'/>&amp;pid=<xsl:value-of select="@pid"/><xsl:apply-templates select="/page/pageResponse/service/paramList" mode="cgi"/></xsl:attribute></param>
      <param name='initial_text'><xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute></param>
      <param name='initial_code'><xsl:attribute name="value"><xsl:value-of  select="@code"/></xsl:attribute></param></applet>
    <xsl:call-template name="dividerBar"/>
  </xsl:template>

  
</xsl:stylesheet>  

