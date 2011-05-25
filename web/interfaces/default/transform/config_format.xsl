<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xslt="output.xsl"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
  extension-element-prefixes="java">
  
  <xsl:output method="xml"/>
  <xsl:namespace-alias
    stylesheet-prefix="xslt" result-prefix="xsl"/>

  <xsl:template match="format">
    <format>
      <xsl:apply-templates/>
    </format>
  </xsl:template>
  
  <xsl:template match="gsf:template">
    <xslt:template>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="priority">2</xsl:attribute> 
      <xsl:if test=".//gsf:link">
	<xslt:param name="serviceName"/>
	<xslt:param name="collName"/>
      </xsl:if>
      <xsl:apply-templates/>
    </xslt:template>
  </xsl:template>
  
  <xsl:template match="gsf:link">
    <xsl:choose>
      <xsl:when test="@type='classifier'">
	<a><xslt:attribute name='href'><xslt:value-of select='$library_name'/>?a=b&amp;rt=r&amp;s=<xslt:value-of select='$serviceName'/>&amp;c=<xslt:value-of select='$collName'/>&amp;cl=<xslt:value-of select='@nodeID'/><xslt:if test="classifierNode|documentNode">.pr</xslt:if><xslt:if test="parent::node()[@orientation='horizontal']">&amp;sib=1</xslt:if></xslt:attribute>
	  <xsl:apply-templates/>
	</a>
      </xsl:when>
	  <xsl:when test="@type='source'">
	<a><xslt:attribute name='href'><xslt:value-of 
	   disable-output-escaping="yes" select="/page/pageResponse/collection/metadataList/metadata[@name='httpPath']" />/index/assoc/<xslt:value-of 
	   disable-output-escaping="yes" select="metadataList/metadata[@name='assocfilepath']" />/<xslt:value-of 
	   disable-output-escaping="yes" select="metadataList/metadata[@name='Source']" /></xslt:attribute>
	  <xsl:apply-templates/>
	</a>
      </xsl:when>
	  
      <xsl:otherwise> <!-- a document link -->
	<xslt:variable name="bookswitch"><xslt:value-of select="/page/pageRequest/paramList/param[@name='book']/@value"/></xslt:variable>
	<xslt:choose>
	  <xslt:when test="$bookswitch = 'on' or $bookswitch = 'flashxml'">
	    <a><xslt:attribute name="href"><xslt:value-of select='$library_name'/>?a=d&amp;ed=1&amp;book=on&amp;c=<xslt:value-of select='$collName'/>&amp;d=<xslt:value-of select='@nodeID'/>&amp;dt=<xslt:value-of select='@docType'/><xslt:if test="@nodeType='leaf'">&amp;sib=1</xslt:if>&amp;p.a=<xslt:value-of select="/page/pageRequest/@action"/>&amp;p.sa=<xsl:value-of select="/page/pageRequest/@subaction"/>&amp;p.s=<xslt:value-of select="$serviceName"/></xslt:attribute><xsl:apply-templates/></a>
	  </xslt:when>
	  <xslt:otherwise>
	    <a><xslt:attribute name="href"><xslt:value-of select='$library_name'/>?a=d&amp;book=off&amp;c=<xslt:value-of select='$collName'/>&amp;d=<xslt:value-of select='@nodeID'/>&amp;dt=<xslt:value-of select='@docType'/><xslt:if test="@nodeType='leaf'">&amp;sib=1</xslt:if>&amp;p.a=<xslt:value-of select="/page/pageRequest/@action"/>&amp;p.sa=<xsl:value-of select="/page/pageRequest/@subaction"/>&amp;p.s=<xslt:value-of select="$serviceName"/></xslt:attribute><xsl:apply-templates/></a>
	  </xslt:otherwise>
	</xslt:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="gsf:icon">
    <xsl:choose>
      <xsl:when test="@type='classifier'">
	<xslt:call-template name="bookshelfimg"/>
      </xsl:when>
      <xsl:otherwise>
	<xslt:apply-templates select="." mode="displayNodeIcon"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="gsf:metadata[@format]">
    <xslt:value-of disable-output-escaping="yes"><xsl:attribute name="select">java:org.greenstone.gsdl3.util.XSLTUtil.<xsl:value-of select="@format"/>(metadataList/metadata[@name='<xsl:apply-templates select="." mode="get-metadata-name"/>'], /page/@lang )</xsl:attribute></xslt:value-of>
  </xsl:template>

  <xsl:template match="gsf:metadata">
    <xslt:value-of disable-output-escaping="yes"><xsl:attribute name="select">metadataList/metadata[@name='<xsl:apply-templates select="." mode="get-metadata-name"/>']</xsl:attribute></xslt:value-of>
  </xsl:template>

  <xsl:template match="gsf:metadata" mode="get-metadata-name">
    <xsl:if test="@multiple='true'">all_</xsl:if><xsl:if test='@select'><xsl:value-of select='@select'/>_</xsl:if><xsl:if test="@separator">*<xsl:value-of select='@separator'/>*_</xsl:if><xsl:value-of select="@name"/>
  </xsl:template>
  
  <xsl:template match="gsf:metadata-old">
    <xslt:value-of disable-output-escaping="yes"><xsl:attribute name="select">metadataList/metadata[@name="<xsl:choose><xsl:when test="@select='parent'">parent_</xsl:when><xsl:when test="@select='root'">root_</xsl:when><xsl:when test="@select='ancestors'">ancestors'<xsl:value-of select='@separator'/>'_</xsl:when><xsl:when test="@select='siblings'">siblings_'<xsl:value-of select='@separator'/>'_</xsl:when></xsl:choose><xsl:value-of select="@name"/>"]</xsl:attribute></xslt:value-of>
  </xsl:template>
  
  <xsl:template match="gsf:text">
    <xslt:apply-templates select="nodeContent"/>
  </xsl:template>
  
  <xsl:template match="gsf:choose-metadata">
    <xslt:choose>
      <xsl:for-each select="gsf:metadata">
	<xslt:when><xsl:attribute name="test">metadataList/metadata[@name='<xsl:apply-templates select="." mode="get-metadata-name"/>']</xsl:attribute>
	  <xsl:apply-templates select="."/>
	</xslt:when>
      </xsl:for-each>
      <xsl:if test="gsf:default">
	<xslt:otherwise><xsl:apply-templates select="gsf:default"/></xslt:otherwise>
      </xsl:if>
    </xslt:choose>
  </xsl:template>
  
  <xsl:template match="gsf:switch">
    <xsl:variable name="meta-name"><xsl:apply-templates select="gsf:metadata" mode="get-metadata-name"/></xsl:variable>
    <xslt:variable name="meta"><xsl:choose><xsl:when test="@preprocess"><xslt:value-of select="util:{@preprocess}(metadataList/metadata[@name='{$meta-name}'])"/></xsl:when><xsl:otherwise><xslt:value-of select="metadataList/metadata[@name='{$meta-name}']"/></xsl:otherwise></xsl:choose></xslt:variable>
    <xslt:choose>
      <xsl:for-each select="gsf:when">
	<xslt:when test="util:{@test}($meta, '{@test-value}')">
	  <xsl:apply-templates/>
	</xslt:when>
      </xsl:for-each>
      <xsl:if test="gsf:otherwise">
	<xslt:otherwise>
	  <xsl:apply-templates select="gsf:otherwise/node()"/>
	</xslt:otherwise>
      </xsl:if>
    </xslt:choose>
  </xsl:template>
  
  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>


