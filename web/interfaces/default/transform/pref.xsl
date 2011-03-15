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
		<xsl:call-template name="pageTitleDeferred" />
      </body>
    </html>
  </xsl:template>
  
  <xsl:template name="pageTitle">
    <xsl:choose><xsl:when test="/page/pageResponse/*/displayItem[@name='name']"><xsl:value-of select="/page/pageResponse/*/displayItem[@name='name']"/></xsl:when><xsl:otherwise></xsl:otherwise></xsl:choose>
  </xsl:template>
  
  <xsl:template name="pageTitleDeferred">
  	    <xsl:choose><xsl:when test="/page/pageResponse/*/displayItem[@name='name']"></xsl:when><xsl:otherwise><span class="getTextFor null document.title.gsdl,document.title.text:' : '[a],document.title.pref_t[a]"></span>:</xsl:otherwise></xsl:choose>
  </xsl:template>

  <!-- page specific style goes here -->
  <xsl:template name="pageStyle"/>


  <xsl:template match="pageResponse">
    <xsl:variable name="collName" select="/page/pageRequest/paramList/param[@name='c']/@value"/>
    
    <xsl:variable name="tidyoption"><xsl:value-of select="/page/pageResponse/collection/metadataList/metadata[@name='tidyoption']"/></xsl:variable>
    
    <xsl:call-template name="standardPageBanner">
      <xsl:with-param name="collName" select="$collName"/>
      <xsl:with-param name="pageType">pref</xsl:with-param>
    </xsl:call-template>

    <xsl:call-template name="navigationBar">
      <xsl:with-param name="collName" select="$collName"/>
    </xsl:call-template>      
    <div id="qureyform">
      <form name="PrefForm" method="get" action="{$library_name}">
	<input type='hidden' name='a' value='p'/>
	<input type='hidden' name='sa' value='pref'/>
	<input type='hidden' name='c' value="{$collName}"/>
	<div class="formheading"><span class="getTextFor pref.prespref"></span></div>
	<ul id="presprefs"><xsl:call-template name="pres-prefs"/></ul>
	<ul id="berrybasketprefs" ><xsl:call-template name="berrybasket-prefs"/></ul>
	
	<xsl:if test="$tidyoption='tidy'">
	  <ul id="bookprefs"><xsl:call-template name="book-prefs"/></ul>
	</xsl:if>
	
	<div class="formheading"><span class="getTextFor pref.searchpref"></span></div>
	<ul id="searchprefs"><xsl:call-template name="search-prefs"/></ul>
	<input type='submit' class="getTextFor null this.value.pref.set_prefs" id="test"></input>
      </form>
    </div>
  </xsl:template>
  
  <xsl:template name="pres-prefs">
  	<li><span class="getTextFor pref.interfacelang"></span></li>
    <li><xsl:call-template name="lang-list"/></li>
    <li><span class="rightspace"><span class="getTextFor pref.encoding"></span></span>x</li>
  </xsl:template>
  
  <xsl:template name="lang-list">
    <xsl:variable name="current" select="/page/@lang"/>
    <select name="l">
      <xsl:for-each select="/page/pageResponse/languageList/language">
	<option value="{@name}"><xsl:if test="@name=$current"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="displayItem[@name='name']"/></option>
      </xsl:for-each>
    </select>
  </xsl:template>
  
  <xsl:template name="search-prefs">
    <xsl:variable name="hits"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='hitsPerPage']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='hitsPerPage']/@value"/></xsl:when><xsl:otherwise>20</xsl:otherwise></xsl:choose></xsl:variable> 
    <li><span class="getTextFor pref.hitsperpage">&amp;amp;nbsp;</span> 
      <select name="hitsPerPage">
	<option value="20"><xsl:if test="$hits=20"><xsl:attribute name="selected"></xsl:attribute></xsl:if>20</option> 
	<option value="50"><xsl:if test="$hits=50"><xsl:attribute name="selected"></xsl:attribute></xsl:if>50</option>
	<option value="100"><xsl:if test="$hits=100"><xsl:attribute name="selected"></xsl:attribute></xsl:if>100</option>
	<option value="-1" class="getTextFor pref.all"><xsl:if test="$hits=-1"><xsl:attribute name="selected"></xsl:attribute></xsl:if></option>
      </select>
    </li>
  </xsl:template>

  <xsl:template name="berrybasket-prefs">
    <xsl:variable name="berrybasket"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='berrybasket']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='berrybasket']/@value"/></xsl:when><xsl:otherwise>off</xsl:otherwise></xsl:choose></xsl:variable>
    
    <li><span class="getTextFor pref.berrybasket">&amp;amp;nbsp;</span> <select name="berrybasket">
        <option value="on" class="getTextFor pref.berrybasket.on"><xsl:if test="$berrybasket='on'"><xsl:attribute name="selected"></xsl:attribute></xsl:if></option> 
	<option value="off" class="getTextFor pref.berrybasket.off"><xsl:if test="$berrybasket='off'"><xsl:attribute name="selected"></xsl:attribute></xsl:if></option>
      </select>
    </li>
  </xsl:template>

  <xsl:template name="book-prefs">
    <xsl:variable name="book"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='book']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='book']/@value"/></xsl:when><xsl:otherwise>off</xsl:otherwise></xsl:choose></xsl:variable>
    
    <span class="getTextFor pref.book">&amp;amp;nbsp;</span> <select name="book">
      <option value="on" class="getTextFor pref.berrybasket.on"><xsl:if test="$book='on'"><xsl:attribute name="selected"></xsl:attribute></xsl:if></option> 
      <option value="off" class="getTextFor pref.berrybasket.off"><xsl:if test="$book='off'"><xsl:attribute name="selected"></xsl:attribute></xsl:if></option>
    </select>
    
  </xsl:template>
</xsl:stylesheet>
