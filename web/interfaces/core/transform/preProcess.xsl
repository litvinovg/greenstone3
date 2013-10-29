<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
  xmlns:gslib="http://www.greenstone.org/skinning"
  xmlns:gsvar="http://www.greenstone.org/skinning-var"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xslt="output.xsl"
  xmlns:gs3="http://www.greenstone.org/gs3"
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  >

  <xsl:output method="xml"/>
  <xsl:namespace-alias
    stylesheet-prefix="xslt" result-prefix="xsl"/>

  <xsl:template match="/">

    <!-- explicitly output the stylesheet element here so we can include the
    namespace declarations. They were going missing before with the new xalan/xerces-->
    <xslt:stylesheet>
      
      <xsl:for-each select="/skinAndLibraryXsl/skinXsl/xsl:stylesheet">
	
	<!-- produce an exact copy of skin stylesheet, with gslib nodes expanded. -->
	<xsl:for-each select="@*">
	  <xsl:variable name="attribute-name" select="name()"/>
	  <xsl:attribute name="{$attribute-name}">
	    <xsl:value-of select="."/>
	  </xsl:attribute>
	</xsl:for-each>
	<!-- merge the attributes of the library stylesheet -->
	<xsl:for-each select="/skinAndLibraryXsl/libraryXsl/xsl:stylesheet/@*">
	  <xsl:variable name="attribute-name" select="name()"/>
	  <xsl:attribute name="{$attribute-name}">
	    <xsl:value-of select="."/>
	  </xsl:attribute>
	</xsl:for-each>
	
	<xsl:call-template name="expand_gslib_elements" />
	
	<!-- add content of library to the skin stylesheet -->
	<xsl:for-each select="/skinAndLibraryXsl/libraryXsl/xsl:stylesheet">
	  <xsl:call-template name="expand_gslib_elements" />
	</xsl:for-each>
	
      </xsl:for-each>
    </xslt:stylesheet>
    
    
  </xsl:template>


  <!-- produce an exact copy of the current node, but expand and replace all elements belonging to the gslib/gsvar namespaces. -->
  <xsl:template name="expand_gslib_elements">


    <xsl:for-each select="*|text()">
      <xsl:choose>
	<!-- variables -->
	<xsl:when test="namespace-uri(.)=namespace::gsvar">
	  <xsl:element name="xsl:value-of">
		<xsl:attribute name="select">$<xsl:value-of select="local-name()" /></xsl:attribute>
	      </xsl:element>
</xsl:when>
	<!-- templates -->
	<xsl:when test="namespace-uri(.)=namespace::gslib">	  
	  <xsl:variable name="name" select="local-name()"/>
	      <xsl:element name="xsl:call-template">
		<xsl:attribute name="name"> <xsl:value-of select="local-name()" /></xsl:attribute>
		
		<xsl:for-each select="@*">
		  <xsl:element name="xsl:with-param">
		    <xsl:attribute name="name"><xsl:value-of select="name()"/></xsl:attribute>
		    <xsl:call-template name="convert_attVal_to_valueOf" />
		  </xsl:element>					
		</xsl:for-each>	
	      </xsl:element>
	</xsl:when>

	<xsl:when test="self::text()">
	  <xsl:value-of select="."/>
	</xsl:when>

	<!-- if a regular node v2 -->
	<xsl:otherwise>
	  <xsl:variable name="element-name" select="name()"/>
	  <xsl:variable name="element-namespace" select="namespace-uri()"/>
	  <xsl:element name="{$element-name}" namespace="{$element-namespace}">
	    <xsl:for-each select="@*">
	      <xsl:variable name="attribute-name" select="name()"/>
	      <xsl:attribute name="{$attribute-name}">
		<xsl:value-of select="."/>
	      </xsl:attribute>
	    </xsl:for-each>
	    <xsl:call-template name="expand_gslib_elements" />
	  </xsl:element>
	</xsl:otherwise>
	
	
      </xsl:choose>
    </xsl:for-each>

  </xsl:template>

  <!-- converts an attribute value (the current node) into either a literal value or an <xsl:value-of> element -->
  <xsl:template name="convert_attVal_to_valueOf">

    <xsl:choose>
      <!-- if attribute is not literal (starts with a "{") -->
      <xsl:when test="starts-with(string(), '&#123;')">
	<xsl:element name="xsl:value-of">
	  <xsl:attribute name="disable-output-escaping">yes</xsl:attribute>
	  <xsl:attribute name="select"><xsl:value-of select="substring(string(),2,string-length()-2)"/></xsl:attribute>
	</xsl:element>
      </xsl:when>
      <xsl:otherwise>
	<xsl:value-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="create_dummy_variables">
    <xsl:for-each select="//*[namespace-uri()=namespace::gslib]">
      <xsl:element name="xsl:variable">
	<xsl:attribute name="name">
	  <xsl:value-of select="local-name()"/>
	</xsl:attribute>
      </xsl:element>	
    </xsl:for-each>
  </xsl:template>
  
  
</xsl:stylesheet>
