<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:gslib="http://www.greenstone.org/XSL/Library"
  exclude-result-prefixes="xalan gslib gsf xslt gs3">


  <xsl:output method="html" 
    doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" 
    doctype-system="http://www.w3.org/TR/html4/loose.dtd"/>

  <!-- some global parameters - these are set by whoever is invoking the transformation -->
  <xsl:param name="interface_name"/>
  <xsl:param name="library_name"/>
  
  <!-- every pages .............................................. -->
  
  <xsl:variable name="a"><xsl:value-of select="/page/pageRequest/paramList/param[@name='a']/@value"/></xsl:variable>
  <xsl:variable name="collections" select="/page/pageResponse/collectionList/collection"/>
  
  <xsl:template name="GS2LibDividerBar">
    <xsl:param name='text'/>
    <xsl:choose>
      <xsl:when test="$text">
	<div class="divbar"><xsl:value-of select="$text"/></div>
      </xsl:when>
      <xsl:otherwise>
	<div class="divbar"><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></div>
      </xsl:otherwise>
    </xsl:choose> 
  </xsl:template>
  
  <xsl:template match="error">
    Error: <xsl:value-of select="."/>
  </xsl:template>
  
  <!-- site home .................................................... -->


  <xsl:template name="pageTitleLanguageDependant">
    <!-- put a space in the title in case the actual value is missing - mozilla will not display a page with no title-->
    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'gsdl')"/><xsl:text> </xsl:text>
  </xsl:template>


  <xsl:template name="greenstoneLogoAlternateText">
    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'gsdl')"/>
  </xsl:template>


  <xsl:template name="collectionLinkWithImage">
    <xsl:choose>
      <xsl:when test="displayItem[@name='icon']">
	<a href="{$library_name}?a=p&amp;amp;sa=about&amp;amp;c={@name}">
	  <img>
	    <xsl:attribute name="src">
	      <xsl:value-of select="metadataList/metadata[@name='httpPath']"/>/images/<xsl:value-of select="displayItem[@name='icon']"/>
	    </xsl:attribute>
	    <xsl:attribute name="alt">
	      <xsl:value-of select="displayItem[@name='name']"/>
	    </xsl:attribute>
	  </img> 
	</a>  
      </xsl:when>
      <xsl:otherwise>
	<a class="noimage" href="{$library_name}?a=p&amp;amp;sa=about&amp;amp;c={@name}">  
	  <xsl:value-of select="displayItem[@name='name']"/>
	</a>
      </xsl:otherwise>
    </xsl:choose> 
  </xsl:template>

  <xsl:variable name="collName" select="/page/pageRequest/paramList/param[@name='c']/@value"/>
  <xsl:variable name="this-element" select="/page/pageResponse/collection|/page/pageResponse/serviceCluster"/>

  <xsl:template name="collectionHomeLinkWithLogoIfAvailable">
    <a href="{$library_name}?a=p&amp;sa=about&amp;c={$collName}">
      <xsl:choose>
	<xsl:when test="$this-element/displayItem[@name='icon']">
	  <img border="0">
	    <xsl:attribute name="src">
	      <xsl:value-of select="$this-element/metadataList/metadata[@name='httpPath']"/>/images/<xsl:value-of select="$this-element/displayItem[@name='icon']"/>
	    </xsl:attribute>	
	    <xsl:attribute name="alt">
	      <xsl:value-of select="$this-element/displayItem[@name='name']"/>
	    </xsl:attribute>
	    <xsl:attribute name="title">
	      <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'aboutpage')"/>
	    </xsl:attribute>
	  </img>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:value-of select="$this-element/displayItem[@name='name']"/>
	</xsl:otherwise>
      </xsl:choose>
    </a>
  </xsl:template>

  <xsl:template name="quickSearchForm">
    <form name="QuickSearch" method="get" action="{$library_name}">
      <input type="hidden" name="a" value="q"/>
      <input type="hidden" name="rt" value="rd"/>
      <input type="hidden" name="s" value="{/page/pageResponse/serviceList/service[@name='TextQuery']/@name}"/>
      <input type="hidden" name="s1.collection" value="all"/>
      <input type="text" name="s1.query" size="20"/>
      <input type="submit" value="Quick Search"/>
    </form>
  </xsl:template>
  
  
  <xsl:template match="serviceClusterList">
    <xsl:for-each select="serviceCluster"> 
      <a href="{$library_name}?a=p&amp;amp;sa=about&amp;amp;c={@name}"><xsl:value-of select='@name'/><xsl:value-of select="displayItem[@name='name']"/></a>
    </xsl:for-each>
  </xsl:template>
  
  
  
  <xsl:template name="poweredByGS3LanguageDependant">
    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'gs3power')"/> 
  </xsl:template>

  <!-- the gberg collection has two options for its templates - a gs2 one and a gs3 one. Because calling a template is now a compile time error with tomcat 6, we can't call undefined templates anymore. this was happening in the branch that wasn't used. So, defining templates here that are used by the gs3 version. hopefully this will go away once we are using oran skin and derivatives -->
  <xsl:template name="aboutCollectionPageTitle"/>
  <xsl:template name="textDirectionAttribute"/>
  <xsl:template name="displayErrorsIfAny"/>
  <xsl:template name="homeButtonTop"/>
  <xsl:template name="helpButtonTop"/>
  <xsl:template name="preferencesButtonTop"/>
  <xsl:template name="servicesNavigationBar"/>
  <xsl:template name="noTextBar"/>
  <xsl:template name="collectionDescriptionTextAndServicesLinks"/>
  <xsl:template name="poweredByGS3TextBar"/>


</xsl:stylesheet>


