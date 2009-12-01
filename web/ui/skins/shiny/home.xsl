<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">

  <!-- style includes global params interface_name, library_name -->
  <xsl:include href="style.xsl"/>

  <xsl:output method="html"/>  

  <xsl:template name="pageTitle">
    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'gsdl')"/>
  </xsl:template>
  
  <!-- page specific style goes here -->
  <xsl:template name="pageStyle"/>

  <xsl:template match="pageResponse">
    <xsl:call-template name="greenstonePageBanner"/> 
    <xsl:apply-templates select="collectionList"/>   
  </xsl:template>

  <xsl:template match="collectionList">
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
      <xsl:value-of select="util:getInterfaceText('classic', /page/@lang, 'home.coll_warning')"/>
    </div>	
   </xsl:template>
</xsl:stylesheet>  


