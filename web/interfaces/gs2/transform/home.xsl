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

  <xsl:template match="page/pageResponse">
    <xsl:call-template name="pageBanner"/> 
    <xsl:apply-templates select="collectionList"/>   
    <xsl:apply-templates select="serviceList"/>
  </xsl:template>

  <xsl:template name="pageBanner">
    <div id="banner">
      <div class="collectimage">
	<img src="interfaces/default/images/gsdlhead.gif"><xsl:attribute name="alt"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'gsdl')"/></xsl:attribute>
	</img>
      </div>
    </div>
    <div class="bannerextra"></div>
    <div id="content">
      <xsl:call-template name="dividerBar">
	<xsl:with-param name="text" select="util:getInterfaceText($interface_name, /page/@lang, 'home.select_a_collection')"/> 
      </xsl:call-template>
    </div>			
  </xsl:template>

  <xsl:template match="collectionList">
    <ul id="collectionlist">
      <xsl:for-each select="collection"> 
	<li>
	  <gslib:collectionLinkWithImage/>
	</li>
      </xsl:for-each> 
    </ul>
    <xsl:call-template name="collWarning"/>
  </xsl:template>
  
  <xsl:template match="collectionListOld">
    <xsl:for-each select="collection"> 
      <xsl:variable name="ct"><xsl:choose><xsl:when test="metadataList/metadata[@name='buildType']='mgpp'">1</xsl:when><xsl:otherwise>0</xsl:otherwise></xsl:choose></xsl:variable>

      <p>
	<a href="{$library_name}?a=p&amp;sa=about&amp;c={@name}&amp;ct={$ct}">
	  <img width="150" border="1">
	    <xsl:attribute name="src">
	      <xsl:value-of select="metadataList/metadata[@name='httpPath']"/>/images/<xsl:choose><xsl:when test="displayItem[@name='smallicon']"><xsl:value-of select="displayItem[@name='smallicon']"/></xsl:when><xsl:otherwise><xsl:value-of select="displayItem[@name='icon']"/></xsl:otherwise></xsl:choose>
	    </xsl:attribute>
	    <xsl:attribute name="alt">
	      <xsl:value-of select="displayItem[@name='name']"/>
	    </xsl:attribute>
	  </img>
	</a>
      </p>
    </xsl:for-each> 
    <xsl:call-template name="collWarning"/>
  </xsl:template> 
  
  <xsl:template name="collWarning">
    <xsl:call-template name="dividerBar"/>	
    <div>
      <xsl:value-of select="util:getInterfaceText('gs2', /page/@lang, 'home.coll_warning')" disable-output-escaping="yes"/>
    </div>	
  </xsl:template>

  <xsl:template match="serviceList">
    <ul id="servicelist">
      <xsl:for-each select="service[@type='query']">
	<li><a href="{$library_name}?a=q&amp;amp;rt=d&amp;amp;s={@name}"><xsl:value-of select="displayItem[@name='name']"/></a><xsl:value-of select="displayItem[@name='description']"/>
	  </li>
      </xsl:for-each>
      <xsl:for-each select="service[@type='authen']">
	<li><a href="{$library_name}?a=g&amp;rt=r&amp;sa=authen&amp;s={@name}&amp;s1.aup=Login&amp;s1.un=&amp;s1.asn="><xsl:value-of select="displayItem[@name='name']"/></a><xsl:value-of select="displayItem[@name='description']"/></li>
      </xsl:for-each>
      <!--uncomment to display a library interface link-->
      <!--<li><a href="{$library_name}?a=p&amp;sa=gli4gs3"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home.librarian_interface')"/></a></li>-->
    </ul>
  </xsl:template>

</xsl:stylesheet>  


