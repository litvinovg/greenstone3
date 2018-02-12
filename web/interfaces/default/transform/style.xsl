<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  xmlns:gslib="http://www.greenstone.org/skinning"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">
  
  <xsl:include href="icons.xsl"/>

  <!-- some global parameters - these are set by whoever is invoking the transformation -->
  <xsl:param name="interface_name"/>
  <xsl:param name="library_name"/>

  <!-- any global style stuff should go in here -->
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

  <xsl:template name="globalStyle">
    <link rel="stylesheet" href="interfaces/default/style/core.css" type="text/css"/>
    <!--<link rel="stylesheet" href="interfaces/gs2/style/gs2-style2.css" type="text/css"/>-->
  </xsl:template>
  
  <xsl:template name="genericPageBanner">
    <xsl:param name='text'/>
    <xsl:variable name="lang" select="/page/@lang"/>
    <center>
      <table width="100%">
	<tr><td align="left"></td>
	  <td align="right">
	    <table>
	      <tr><td class="gsbutton">
		  <a class="gsbutton" href="{$library_name}?a=p&amp;sa=home">
		    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home_b')"/>
		  </a>
		</td></tr></table>
	  </td>
	</tr>
      </table>
    </center>
    <xsl:call-template name="dividerBar"><xsl:with-param name='text' select="$text"/></xsl:call-template>
  </xsl:template>
  
  <xsl:template name="standardPageBanner">
    <xsl:param name="collName"/>
    <xsl:param name="pageType"/>
    <xsl:param name="clTop"/>
    <xsl:variable name="this-element" select="/page/pageResponse/collection"/>
    <div id="banner">
      <div class="pageinfo"> 
	<xsl:call-template name="top-buttons">
	  <xsl:with-param name="collName" select="$collName"/>
	  <xsl:with-param name="pageType" select="$pageType"/>
	</xsl:call-template>
	<xsl:if test="not(/page/pageRequest/@action='d')">
	  <xsl:variable name="text">
	    <xsl:choose>
	      <xsl:when test="$pageType='browse'">
		<xsl:value-of select="/page/pageResponse/collection/serviceList/service[@name='ClassifierBrowse']/classifierList/classifier[@name=$clTop]/displayItem[@name='name']"/>
	      </xsl:when>
	      <xsl:otherwise>
		<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, concat($pageType, '_t'))"/>
	      </xsl:otherwise>
	    </xsl:choose>
	  </xsl:variable>
	  <xsl:if test="$text != '__t_'">
	    <p class="bannertitle"><xsl:value-of select="$text"/></p>
	  </xsl:if>
	</xsl:if>
      </div>

      <div class="collectimage">
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
	      <b><xsl:value-of select="$this-element/displayItem[@name='name']"/></b>
	    </xsl:otherwise>
	  </xsl:choose>
	</a>
      </div>
    </div>
    <div class="bannerextra"></div>	
  </xsl:template>
  
  <xsl:template name="top-buttons">
    <xsl:param name="collName"/>
    <xsl:param name="pageType"/>
    <p class="bannerlinks">
      <a class="navlink" href="{$library_name}?a=p&amp;sa=home"><xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home_tip')"/></xsl:attribute>
	<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home_b')"/>
      </a>
      <!--<xsl:choose>
        <xsl:when test="$pageType='help'">
	  <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_b')"/>
	</xsl:when>
	<xsl:otherwise>
	  <a class="navlink" href="{$library_name}?a=p&amp;sa=help&amp;c={$collName}"><xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_tip')"/></xsl:attribute>
	    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_b')"/>
	  </a>
	</xsl:otherwise>
      </xsl:choose> -->
      <xsl:choose>
	<xsl:when test="$pageType='pref'">
	  <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_b')"/>
	</xsl:when>
	<xsl:otherwise>
	  <a class="navlink" href="{$library_name}?a=p&amp;sa=pref&amp;c={$collName}"><xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_tip')"/></xsl:attribute>
	    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_b')"/>
	  </a>
	</xsl:otherwise>
      </xsl:choose>      
    </p>
  </xsl:template>
  
  <xsl:template name="navigationBar">
    <xsl:param name="collName"/>
    <xsl:param name="clTop"/>
    <xsl:variable name="this-element" select="/page/pageResponse/collection"/>
    <xsl:variable name="this-service" select="/page/pageResponse/service/@name"/>
    <xsl:variable name="classifiers" select="$this-element/serviceList/service[@name='ClassifierBrowse']/classifierList[1]"/>

    <!-- display order, for Arabic etc. languages, display right to left -->
    <xsl:variable name="dir"><xsl:call-template name="direction"/></xsl:variable>
    <xsl:variable name="sort_order"><xsl:choose><xsl:when test="$dir='rtl'">descending</xsl:when><xsl:otherwise>ascending</xsl:otherwise></xsl:choose></xsl:variable>	    

    <div class="navbar">
      <p class="navbar">
	<xsl:choose>
	  <xsl:when test="not($dir='rtl')">
	    <xsl:if test="$this-element/serviceList/service[@type='query']">
	      <xsl:call-template name="navbar-search"><xsl:with-param name="collName" select="$collName"/></xsl:call-template>
	    </xsl:if>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:if test="$this-element/serviceList/service[@name='PhindApplet']">
	      <xsl:call-template name="navbar-phind"><xsl:with-param name="collName" select="$collName"/></xsl:call-template>
	    </xsl:if>
	  </xsl:otherwise>
	</xsl:choose>

	<xsl:for-each select="$classifiers/classifier">
	  <xsl:sort order="{$sort_order}" select="position()" />
	  <xsl:apply-templates select="." mode="navbar">
	    <xsl:with-param name="collName" select="$collName"/>
	    <xsl:with-param name="clTop" select="$clTop"/>
	  </xsl:apply-templates>
	</xsl:for-each>
	
	<xsl:choose>	
	  <xsl:when test="$dir='rtl'">
	    <xsl:if test="$this-element/serviceList/service[@type='query']">
	      <xsl:call-template name="navbar-search"><xsl:with-param name="collName" select="$collName"/></xsl:call-template>
	    </xsl:if>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:if test="$this-element/serviceList/service[@name='PhindApplet']">
	      <xsl:call-template name="navbar-phind"><xsl:with-param name="collName" select="$collName"/></xsl:call-template>
	    </xsl:if>
	  </xsl:otherwise>
	</xsl:choose>
      </p>
    </div> 
  </xsl:template>

  <xsl:template name="navbar-search">
    <xsl:param name="collName"/>
    <xsl:choose>
      <xsl:when test="/page/pageRequest/@action='q'">	  
	<span class="narspace">search</span>
      </xsl:when>
      <xsl:otherwise>
	<xsl:variable name="service">
	  <xsl:choose>
	    <xsl:when  test="not(/page/pageRequest/paramList/param[@name='ct'])">TextQuery</xsl:when>
	    <xsl:when test="/page/pageRequest/paramList/param[@name='ct']/@value='0'">TextQuery</xsl:when>
	    <xsl:when test="/page/pageRequest/paramList/param[@name='qt']/@value='1'"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='qfm']/@value='1'">AdvancedFieldQuery</xsl:when><xsl:otherwise>FieldQuery</xsl:otherwise></xsl:choose></xsl:when>
	    <xsl:otherwise>TextQuery</xsl:otherwise>
	  </xsl:choose>
	</xsl:variable>
	<a class="navlink" href="{$library_name}?a=q&amp;rt=d&amp;s={$service}&amp;c={$collName}"><xsl:attribute name='title'><xsl:value-of select="/page/pageResponse/collection/service[@name=$service]/displayItem[@name='description']"/></xsl:attribute>search</a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="navbar-phind">
    <xsl:param name="collName"/>
    <xsl:choose>
      <xsl:when test="/page/pageRequest/paramList/param[@name='s']/@value='PhindApplet'">
	<span class="narspace">phrases</span>
      </xsl:when>
      <xsl:otherwise>
	<a class="navlink" href="{$library_name}?a=a&amp;rt=d&amp;s=PhindApplet&amp;c={$collName}"><xsl:if test="/page/pageResponse/collection/service[@name='PhindApplet']/displayItem[@name='description']"><xsl:attribute name='title'><xsl:value-of select="/page/pageResponse/collection/service[@name='PhindApplet']/displayItem[@name='description']"/></xsl:attribute></xsl:if>phrases</a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="classifier" mode="navbar">
    <xsl:param name="collName"/>
    <xsl:param name="clTop"/>
    <xsl:choose>
      <xsl:when test="/page/pageRequest/@action='b' and $clTop=@name">
	<span class="narspace"><xsl:value-of select="displayItem[@name='name']"/></span>
      </xsl:when>
      <xsl:otherwise>
	<a class="navlink" href="{$library_name}?a=b&amp;rt=r&amp;s=ClassifierBrowse&amp;c={$collName}&amp;cl={@name}"><xsl:attribute name='title'><xsl:value-of select="displayItem[@name='description']"/></xsl:attribute><xsl:value-of select="displayItem[@name='name']"/></a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="dividerBar">   
    <xsl:param name='text'/>
    <xsl:choose>
      <xsl:when test="$text">
	<div class="divbar"><p class="navbar"><xsl:value-of select="$text"/></p></div>
      </xsl:when>
      <xsl:otherwise>
	<div class="divbar"><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></div>
      </xsl:otherwise>
    </xsl:choose> 
  </xsl:template>
  
  <xsl:template name="greenstoneFooter">
    <div class="divbar"><p class="navbar">powered by greenstone3</p>
    </div>
  </xsl:template>  

  <xsl:template name="direction">
    <xsl:if test="/page/@lang='ar' or /page/@lang='fa' or /page/@lang='he' or /page/@lang='ur' or /page/@lang='ps' or /page/@lang='prs'">rtl</xsl:if>
  </xsl:template>

</xsl:stylesheet>  
