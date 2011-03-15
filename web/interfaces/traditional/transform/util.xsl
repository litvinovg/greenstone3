<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:gslib="http://www.greenstone.org/XSL/Library"
  xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
  xmlns:util="http://org.greenstone.gsdl3.util.XSLTUtil"
  exclude-result-prefixes="util gslib gsf">
  
  <!-- some global parameters - these are set by whoever is invoking the transformation -->
  <xsl:param name="interface_name"/>
  <xsl:param name="library_name"/>

  <xsl:include href="xml-to-string.xsl"/>
  
  <!-- every pages ................................................. -->
  
  <xsl:variable name="a"><xsl:value-of select="/page/pageRequest/paramList/param[@name='a']/@value"/></xsl:variable>
  <xsl:variable name="collections" select="/page/pageResponse/collectionList/collection"/>
  
  
  <xsl:template name="textDirectionAttribute">
    <xsl:attribute name="dir">
      <xsl:choose>
	<xsl:when test="/page/@lang='ar' or /page/@lang='fa' or /page/@lang='he' or /page/@lang='ur' or /page/@lang='ps' or /page/@lang='prs'">rtl</xsl:when>
	<xsl:otherwise>ltr</xsl:otherwise>
      </xsl:choose> 
    </xsl:attribute>
  </xsl:template>
  
  
  <xsl:template name="defaultDividerBar">
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
  
  
  <xsl:template name="displayErrorsIfAny">
    <xsl:if test="descendant::error">
      <script language="Javascript">
	<xsl:text disable-output-escaping="yes">
	  function removeAllChildren(node) {
	  while (node.hasChildNodes()) {
	  node.removeChild(node.firstChild);
	  }
	  }

	  function toggleHideError(obj) {
	  if (obj.style.display == "none") {
	  obj.style.display = "";
	  hide_link = document.getElementById("hide");
	  removeAllChildren(hide_link);
	  hide_link.appendChild(document.createTextNode("</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'hide_error')"/><xsl:text disable-output-escaping="yes">"));
	  } else {
	  obj.style.display = "none";
	  hide_link = document.getElementById("hide");
	  removeAllChildren(hide_link);
	  hide_link.appendChild(document.createTextNode("</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'show_error')"/><xsl:text disable-output-escaping="yes">"));
	  } 
	  }
	</xsl:text>
      </script>
      <p align='right'><a id="hide" href="javascript:toggleHideError(error);"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'show_error')"/></a></p>
      <div id="error" style="display: none;">
	<xsl:apply-templates select="descendant::error"/>
      </div>
    </xsl:if>
  </xsl:template>


  <xsl:template name="noTextBar">
    <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
  </xsl:template>
  
  
  <xsl:template name="poweredByGS3TextBar">
    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'gs3power')"/> 
  </xsl:template>
  
  
  <!-- site home ...................................................... -->

  <xsl:template name="siteHomePageTitle">
    <!-- put a space in the title in case the actual value is missing - mozilla will not display a page with no title-->
    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'gsdl')"/><xsl:text> </xsl:text>
  </xsl:template>


  <xsl:template name="selectACollectionTextBar">
    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home.select_a_collection')"/>
  </xsl:template>
  
  
  <xsl:template name="crossCollectionQuickSearchForm">
    <xsl:apply-templates select="serviceList/service[@name='TextQuery']" mode="quicksearch"/>
  </xsl:template> 
  
  <xsl:template match="service[@name='TextQuery']" mode="quicksearch">
    <form name="QuickSearch" method="get" action="{$library_name}">
      <input type="hidden" name="a" value="q"/>
      <input type="hidden" name="rt" value="rd"/>
      <input type="hidden" name="s" value="{@name}"/>
      <input type="hidden" name="s1.collection" value="all"/>
      <input type="text" name="s1.query" size="20"/>
      <input type="submit"><xsl:attribute name="value"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home.quick_search')"/></xsl:attribute></input>
    </form>
  </xsl:template>
  
  <xsl:template name="collectionLinkWithImage">
    <xsl:choose>
      <xsl:when test="displayItem[@name='icon']">
	<a href="{$library_name}?a=p&amp;sa=about&amp;c={@name}">
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
	<a class="noimage" href="{$library_name}?a=p&amp;sa=about&amp;c={@name}">  
	  <xsl:value-of select="displayItem[@name='name']"/>
	</a>
      </xsl:otherwise>
    </xsl:choose> 
  </xsl:template>
  
  
  <xsl:template name="serviceClusterList">
    <xsl:apply-templates select="serviceClusterList"/>
  </xsl:template> 
  
  <xsl:template match="serviceClusterList">
    <xsl:for-each select="serviceCluster"> 
      <a href="{$library_name}?a=p&amp;sa=about&amp;c={@name}"><xsl:value-of select='@name'/><xsl:value-of select="displayItem[@name='name']"/></a>
    </xsl:for-each>
  </xsl:template>

  
  <xsl:template name="serviceLink">
    <a href="{$library_name}?a=q&amp;rt=d&amp;s={@name}"><xsl:value-of select="displayItem[@name='name']"/></a><xsl:value-of select="displayItem[@name='description']"/>
  </xsl:template>

  
  <xsl:template name="authenticationLink">
    <a href="{$library_name}?a=g&amp;rt=r&amp;sa=authen&amp;s={@name}&amp;s1.aup=Login&amp;s1.un=&amp;s1.asn="><xsl:value-of select="displayItem[@name='name']"/></a><xsl:value-of select="displayItem[@name='description']"/>
  </xsl:template> 
  
  
  <xsl:template name="libraryInterfaceLink">
    <li><a href="{$library_name}?a=p&amp;sa=gli4gs3"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home.librarian_interface')"/></a></li>
  </xsl:template> 
  
  
  <xsl:template name="greenstoneLogoAlternateText">
    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'gsdl')"/>
  </xsl:template>

  <!-- about page - collection home ......................... -->
  <xsl:variable name="collName" select="/page/pageRequest/paramList/param[@name='c']/@value"/>
  <xsl:param name="collName" select="/page/pageRequest/paramList/param[@name='c']/@value"/>
  <xsl:param name="pageType"/>
  <xsl:variable name="this-element" select="/page/pageResponse/collection|/page/pageResponse/serviceCluster"/>
  <xsl:variable name="this-service" select="/page/pageResponse/service/@name"/>


  <xsl:template name="aboutCollectionPageTitle">
    <!-- put a space in the title in case the actual value is missing - mozilla will not display a page with no title-->
    <xsl:value-of select="/page/pageResponse/*/displayItem[@name='name']"/><xsl:text> </xsl:text>
  </xsl:template>

  
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


  <xsl:template name="homeButtonTop">
    <a href="{$library_name}?a=p&amp;sa=home"><xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home_tip')"/></xsl:attribute>
      <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home_b')"/></a>
  </xsl:template>


  <xsl:template name="helpButtonTop">
    <xsl:choose>
      <xsl:when test="$pageType='help'">
	<li><a><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_b')"/></a></li>
      </xsl:when>
      <xsl:otherwise>
	<li><a href="{$library_name}?a=p&amp;sa=help&amp;c={$collName}"><xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_tip')"/></xsl:attribute>
	    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_b')"/>
	  </a></li>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template name="preferencesButtonTop">
    <xsl:choose>
      <xsl:when test="$pageType='pref'">
	<li><a><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_b')"/></a></li>
      </xsl:when>
      <xsl:otherwise>
	<li><a href="{$library_name}?a=p&amp;sa=pref&amp;c={$collName}"><xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_tip')"/></xsl:attribute>
	    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_b')"/>
	  </a></li>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="servicesNavigationBar">
    <xsl:for-each select="$this-element/serviceList/service">
      <xsl:variable name="action"><xsl:choose>
	  <xsl:when test="@hidden='true'">DO_NOT_DISPLAY</xsl:when>
	  <xsl:when test="@name=$this-service">CURRENT</xsl:when>
	  <xsl:when test="@type='query'">q</xsl:when>
	  <xsl:when test="@type='browse'">b</xsl:when>
	  <xsl:when test="@type='process'">pr</xsl:when>
	  <xsl:when test="@type='applet'">a</xsl:when>
	  <xsl:otherwise>DO_NOT_DISPLAY</xsl:otherwise>
	</xsl:choose></xsl:variable>
      <xsl:choose>
	<xsl:when test="$action='CURRENT'">
	  <li><a><xsl:value-of select="displayItem[@name='name']"/></a></li>
	</xsl:when>
	<xsl:when test="$action !='DO_NOT_DISPLAY'">
	  <li><a href="{$library_name}?a={$action}&amp;rt=d&amp;s={@name}&amp;c={$collName}"><xsl:if test="displayItem[@name='description']"><xsl:attribute name='title'><xsl:value-of select="displayItem[@name='description']"/></xsl:attribute></xsl:if><xsl:value-of select="displayItem[@name='name']"/></a></li>
	</xsl:when>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>


  <xsl:template name="collectionDescriptionTextAndServicesLinks">
    <xsl:apply-templates select="pageResponse/collection|serviceCluster"/>
  </xsl:template>

  <xsl:template match="collection|serviceCluster">
    <xsl:value-of select="displayItem[@name='description']" disable-output-escaping="yes"/>
    
    <xsl:apply-templates select="serviceList">
      <xsl:with-param name="collName" select="$collName"/>
    </xsl:apply-templates>
  </xsl:template>


  <xsl:template match="serviceList">
    <xsl:param name="collName"/>
    <h3><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'about.services')"/></h3>
    <xsl:choose>
      <xsl:when test="service">
	<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'about.servicehelp')"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'about.noservices')"/>
      </xsl:otherwise>
    </xsl:choose>
    
    
    <xsl:if test="service">
      <ul id="servicelist">  	
	<xsl:for-each select="service">
	  <xsl:variable name="action"><xsl:choose>
	      <xsl:when test="@hidden='true'">DO_NOT_DISPLAY</xsl:when>
	      <xsl:when test="@type='query'">q</xsl:when>
	      <xsl:when test="@type='browse'">b</xsl:when>
	      <xsl:when test="@type='process'">pr</xsl:when>
	      <xsl:when test="@type='applet'">a</xsl:when>
	      <xsl:otherwise>DO_NOT_DISPLAY</xsl:otherwise>
	    </xsl:choose></xsl:variable>
	  <xsl:if test="$action !='DO_NOT_DISPLAY'">
	    <li><a href="{$library_name}?a={$action}&amp;rt=d&amp;s={@name}&amp;c={$collName}"><xsl:value-of select="displayItem[@name='name']"/></a><xsl:value-of select="displayItem[@name='description']"/></li>
	  </xsl:if>
  	</xsl:for-each>
      </ul> 
    </xsl:if> 
  </xsl:template>

</xsl:stylesheet>
