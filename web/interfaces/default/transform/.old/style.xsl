<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">

  <xsl:include href="icons.xsl"/>

  <!-- some global parameters - these are set by whoever is invoking the transformation -->
  <xsl:param name="interface_name"/>
  <xsl:param name="library_name"/>

  <!-- global style info goes here  -->
  <xsl:template name="globalStyle">
    <link rel="stylesheet" href="interfaces/basic/style/core.css" type="text/css"/>
  </xsl:template>

  <xsl:template name="response">
    <xsl:apply-templates select="pageResponse"/>
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

  <xsl:template name="greenstoneFooter">
    <div id="footer">
      <xsl:call-template name="dividerBar">
	<xsl:with-param name="text" select="util:getInterfaceText($interface_name, /page/@lang, 'gs3power')"/> 
      </xsl:call-template>
    </div>
  </xsl:template>
  
  <xsl:template match="error">
    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'error')"/> <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template name="standardPageBanner">
    <xsl:param name="collName"/>
    <xsl:param name="pageType"/>
    <xsl:variable name="this-element" select="/page/pageResponse/collection|/page/pageResponse/serviceCluster"/>
    <div  id="banner">    
      <p>
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
      </p>
      <ul id="bannerlist"> 
	<xsl:call-template name="top-buttons">
	  <xsl:with-param name="collName" select="$collName"/>
	  <xsl:with-param name="pageType" select="$pageType"/>
	</xsl:call-template>
      </ul>
    </div>
  </xsl:template>
  
  <xsl:template name="top-buttons">
    <xsl:param name="collName"/>
    <xsl:param name="pageType"/>
    <li><a  href="{$library_name}?a=p&amp;sa=home"><xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home_tip')"/></xsl:attribute>
	<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home_b')"/>
      </a></li>
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

  <xsl:template name="navigationBar">
    <xsl:param name="collName"/>
    <xsl:variable name="this-element" select="/page/pageResponse/collection|/page/pageResponse/serviceCluster"/>
    <xsl:variable name="this-service" select="/page/pageResponse/service/@name"/>
    <xsl:choose>
      <xsl:when test="$this-element/serviceList/service">
	<div id="navbar">
	  <ul id="navbarlist">
	    <xsl:for-each select="$this-element/serviceList/service">
	      <xsl:variable name="action"><xsl:choose>
		  <xsl:when test="@name=$this-service">CURRENT</xsl:when>
		  <xsl:when test="@type='query'">q</xsl:when>
		  <xsl:when test="@type='browse'">b</xsl:when>
		  <xsl:when test="@type='process'">pr</xsl:when>
		  <xsl:when test="@type='applet'">a</xsl:when>
		  <xsl:otherwise>DO_NOT_DISPLAY</xsl:otherwise>
		</xsl:choose></xsl:variable>
	      <xsl:choose>
		<xsl:when test="$action='CURRENT'">
		  <li><a ><xsl:value-of select="displayItem[@name='name']"/></a></li>
		</xsl:when>
		<xsl:when test="$action !='DO_NOT_DISPLAY'">
		  <li><a href="{$library_name}?a={$action}&amp;rt=d&amp;s={@name}&amp;c={$collName}"><xsl:if test="displayItem[@name='description']"><xsl:attribute name='title'><xsl:value-of select="displayItem[@name='description']"/></xsl:attribute></xsl:if><xsl:value-of select="displayItem[@name='name']"/></a></li>
		</xsl:when>
	      </xsl:choose>
	    </xsl:for-each>
	  </ul>
	</div> 
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="dividerBar">
	  <xsl:with-param name="text" select="'&#160;'"/> 
        </xsl:call-template>            
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  
  <xsl:template name="dividerBar">
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


  <xsl:template name="direction">
    <xsl:if test="/page/@lang='ar' or /page/@lang='fa' or /page/@lang='he' or /page/@lang='ur' or /page/@lang='ps' or /page/@lang='prs'">rtl</xsl:if>
  </xsl:template>

</xsl:stylesheet>  


