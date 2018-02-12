<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">
 
  <!-- some global parameters - these are set by whoever is invoking the transformation -->
  <xsl:param name="interface_name"/>

  <!-- Display the appropriate image, depending on the node type -->
  <xsl:template match="documentNode" mode="displayNodeIcon">
    
    <!-- Root node: book icon (open or closed) -->
    <xsl:choose>
      <xsl:when test="@nodeType='root'">
	<xsl:choose>
	  <xsl:when test="documentNode">
	    <xsl:call-template name="openbookimg">
	      <xsl:with-param name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'close_book')"/></xsl:with-param>
	    </xsl:call-template>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:call-template name="closedbookimg">
	      <xsl:with-param name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'open_book')"/></xsl:with-param>
	    </xsl:call-template>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:when>
      
      <!-- Internal node: folder icon (open or closed) -->
      <xsl:when test="@nodeType='internal'">
	<xsl:choose>
	  <xsl:when test="documentNode">
	    <xsl:call-template name="openfolderimg">
	      <xsl:with-param name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'close_folder')"/></xsl:with-param>
	    </xsl:call-template>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:call-template name="closedfolderimg">
	      <xsl:with-param name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'open_folder')"/></xsl:with-param>
	    </xsl:call-template>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:when>
      
      <!-- Leaf node: page icon, and this is the default -->
      <xsl:otherwise>
	<xsl:call-template name="textpageimg">
	  <xsl:with-param name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'view_document')"/></xsl:with-param>
	</xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="openbookimg">
    <xsl:param name="alt"/>
    <xsl:param name="title"/>
    <img  border="0" width="28" height="23"
      src="interfaces/basic/images/openbook.gif"
      alt="{$alt}" title="{$title}"/>
  </xsl:template>

  <xsl:template name="closedbookimg">
    <xsl:param name="alt"/>
    <xsl:param name="title"/>
    <img border="0" width="18" height="11"
      src="interfaces/basic/images/book.gif"
      alt="{$alt}" title="{$title}"/>
  </xsl:template>

  <xsl:template name="openfolderimg">
    <xsl:param name="alt"/>
    <xsl:param name="title"/>
    <img border="0" width="23" height="15"
      src="interfaces/basic/images/openfldr.gif"
      alt="{$alt}" title="{$title}"/>
  </xsl:template>

  <xsl:template name="closedfolderimg">
    <xsl:param name="alt"/>
    <xsl:param name="title"/>
    <img  border="0" width="23" height="15"
      src="interfaces/basic/images/clsdfldr.gif"
      alt="{$alt}" title="{$title}"/>
  </xsl:template>

  <xsl:template name="textpageimg">
    <xsl:param name="alt"/>
    <xsl:param name="title"/>
    <img  border="0" width="16" height="21"
      src="interfaces/basic/images/itext.gif"
      alt="{$alt}" title="{$title}"/>
  </xsl:template>

  <xsl:template name="bookshelfimg">
    <xsl:param name="alt"/>
    <xsl:param name="title"/>
    <img  border="0" width="20" height="16"
      src="interfaces/basic/images/bshelf.gif"  
      alt="{$alt}" title="{$title}"/>
  </xsl:template>

  <xsl:template name="iconpdf">
    <xsl:param name="alt">PDF</xsl:param>
    <xsl:param name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'texticonpdf')"/></xsl:param>
    <img border="0" width='26' height='26' 
      src='interfaces/basic/images/ipdf.gif' 
      alt='{$alt}' title='{$title}'/>
  </xsl:template>
  
  <xsl:template name="icondoc">
    <xsl:param name="alt">Word</xsl:param>
    <xsl:param name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'texticonmsword')"/></xsl:param>
    <img border="0" width='26' height='26' 
      src='interfaces/basic/images/imsword.gif' 
      alt='{$alt}' title='{$title}'/>
  </xsl:template>

</xsl:stylesheet>

