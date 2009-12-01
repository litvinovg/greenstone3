<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xslt="output.xsl"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:gsf="http://www.greenstone.org/configformat"
  extension-element-prefixes="java">
  
  <xsl:output method="xml"/>
  <xsl:namespace-alias
    stylesheet-prefix="xslt" result-prefix="xsl"/>


  <xsl:template match="format">
    <format hello="yes">
      <xsl:apply-templates/>
    </format>
  </xsl:template>
  
  <xsl:template match="gsf:template">
    <xslt:template>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="priority">2</xsl:attribute> 
      <xsl:if test=".//gsf:link/@type">
	<xslt:param name="serviceName">service-name</xslt:param>
      </xsl:if>
      <xsl:if test=".//gsf:link">
	<xslt:param name="collName">coll-name</xslt:param>
	<xslt:variable name='library' select='ancestor::page/config/library_name'/>
      </xsl:if>
      <xsl:apply-templates/>
    </xslt:template>
  </xsl:template>
  
  <xsl:template match="gsf:link">
    <xsl:choose>
      <xsl:when test="@type='classifier'">
	<a><xslt:attribute name='href'><xslt:value-of select='$library'/>?a=b&amp;s=<xslt:value-of select='$serviceName'/>&amp;c=<xslt:value-of select='$collName'/>&amp;cl=<xslt:value-of select='@nodeID'/><xslt:if test="classifierNode|documentNode">.pr</xslt:if></xslt:attribute>
	  <xsl:apply-templates/>
	</a>
      </xsl:when>
      <xsl:otherwise> <!-- a document link -->
	<a><xsl:attribute name="href">{$library}?a=d&amp;c={$collName}&amp;d={@nodeID}</xsl:attribute>
	  <xsl:apply-templates/>
	</a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="gsf:icon">
    <xsl:choose>
      <xsl:when test="@type='classifier'">
	<img src='interfaces/default/images/bshelf.gif' border='0' width='20' height='16' />
      </xsl:when>
      <xsl:otherwise>
	<xslt:apply-templates select="." mode="displayNodeIcon"/>
	<!--
	<img src='interfaces/default/images/book.gif' width='18' height='11' border='0'/>-->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="gsf:metadata">    
    <xslt:value-of><xsl:attribute name="select">metadataList/metadata[@name='<xsl:value-of select="@name"/>']</xsl:attribute></xslt:value-of>
  </xsl:template>

<!--
  <xsl:template match="gsf:text">
TODO
  </xsl:template>
-->
  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>


<!-- copied from style for now-->
<!-- Display the appropriate image, depending on the node type -->
<xsl:template match="documentNode" mode="displayNodeIcon">

  <!-- Root node: book icon (open or closed) -->
  <xsl:if test="@nodeType='root'">
    <xsl:choose>
      <xsl:when test="documentNode">
	<img border="0" width="28" height="23"
	     src="interfaces/default/images/openbook.gif"
	     alt="Close this book"/>
      </xsl:when>
      <xsl:otherwise>
	<img border="0" width="18" height="11"
	     src="interfaces/default/images/book.gif"
	     alt="Open this document and view contents"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:if>

  <!-- Interior node: folder icon (open or closed) -->
  <xsl:if test="@nodeType='interior'">
    <xsl:choose>
      <xsl:when test="documentNode">
	<img border="0" width="23" height="15"
	     src="interfaces/default/images/openfldr.gif"
	     alt="Close this folder"/>
      </xsl:when>
      <xsl:otherwise>
	<img border="0" width="23" height="15"
	     src="interfaces/default/images/clsdfldr.gif"
	     alt="Open this folder and view contents"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:if>

  <!-- Leaf node: page icon -->
  <xsl:if test="@nodeType='leaf'">
    <img border="0" width="16" height="21"
	 src="interfaces/default/images/itext.gif"
	 alt="View the document"/>
  </xsl:if>
</xsl:template>

</xsl:stylesheet>


