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
  
    <!-- the main page layout template is here -->
  <xsl:template match="page">
    
    <xsl:choose> 
	<!-- if we are using hte classic GS2 look and feel use this template otherwise use the default GS3 look and feel template -->
      <xsl:when test="$interface_name = 'gs2'">
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
	<div id="page-wrapper">
	   <xsl:apply-templates select="pageResponse"/>
	   <xsl:call-template name="greenstoneFooter"/>                	   
	</div>
      </body>
    </html>
      </xsl:when>

      <xsl:otherwise>    
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
           <div id="page-wrapper">
   		  <xsl:call-template name="response" />
		  <xsl:call-template name="greenstoneFooter"/>
	   </div>
          </body>
        </html>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="pageTitle">
    <xsl:value-of select="/page/pageResponse/documentNode/metadataList/metadata[@name='root_Title']"/>
  </xsl:template>
  
  <!-- page specific style goes here -->
  <xsl:template name="pageStyle">
    <link type="text/css" href="sites/localsite/collect/gberg/transform/darwin.css" rel="stylesheet"/>
  </xsl:template>

  <xsl:template match="pageResponse">
    <xsl:variable name="collName"><xsl:value-of select="/page/pageRequest/paramList/param[@name='c']/@value"/></xsl:variable>
    <xsl:call-template name="standardPageBanner">
      <xsl:with-param name="collName" select="$collName"/>
    </xsl:call-template>
    <xsl:call-template name="navigationBar">
      <xsl:with-param name="collName" select="$collName"/>
    </xsl:call-template>      
    <!-- display the document -->
    <xsl:apply-templates select="documentNode">
      <xsl:with-param name="collName" select="$collName"/>
    </xsl:apply-templates>
    <xsl:call-template name="dividerBar"/>
  </xsl:template>

  <xsl:template match="documentNode">
    <xsl:param name="collName"/>
    <xsl:variable name="docname"><xsl:choose><xsl:when test="contains(@nodeID, '.')"><xsl:value-of select="substring-before(@nodeID, '.')"/></xsl:when><xsl:otherwise><xsl:value-of select="@nodeID"/></xsl:otherwise></xsl:choose></xsl:variable>
    <h1>
      <center>
	<a href="{$library_name}?a=xd&amp;sa=toc&amp;c={$collName}&amp;d={$docname}"><xsl:attribute name="title">Go to the document contents</xsl:attribute><xsl:value-of select="metadataList/metadata[@name='root_Title']"/>&#160;</a>
      </center>
    </h1>
    <xsl:apply-templates select="nodeContent/*"/>
  </xsl:template>

  <xsl:template match="gutblurb | markupblurb | endmarkupblurb | endgutblurb">
  </xsl:template>

  <xsl:template match="gutbook | book | frontmatter | bookbody | backmatter | titlepage| htitlepage | toc | dedication | preface | introduction | chapter |  part | chapheader | index | appendix | glossary">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

<!-- dont seem to be able to apply styles to title tags - cos they are special to html. so we rename title to title1-->
  <xsl:template match="title">
    <title1>
      <xsl:apply-templates/>
    </title1>
  </xsl:template>  
    
  <xsl:template match="*">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="metadataList">
  </xsl:template>

</xsl:stylesheet>







