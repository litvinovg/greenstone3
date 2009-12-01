<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html"/>  

<xsl:template match="/">
 <html>
  <head>
    <title>
     <xsl:call-template name="metadataItem">
      <xsl:with-param name="name">siteName</xsl:with-param>
     </xsl:call-template>
    </title>
    <xsl:call-template name="cssFiles"/>
  </head>
  <body>
   <div id="mainContentArea">
   <h1> 
    <xsl:call-template name="metadataItem">
     <xsl:with-param name="name">siteName</xsl:with-param>
    </xsl:call-template>
   </h1>

   <xsl:call-template name="metadataItem">
    <xsl:with-param name="name">siteDescription</xsl:with-param>
   </xsl:call-template>

   <h2>Collections:</h2>
   <ul>
    <xsl:for-each select="/page/pageResponse/collectionList/collection">
     <li> <em> <xsl:value-of select="displayItem[@name='name']"/> </em> </li>
    </xsl:for-each>
   </ul>

   </div>

   <div id="footer">
   <xsl:call-template name="uiItem">
    <xsl:with-param name="name">footer</xsl:with-param>
   </xsl:call-template>
   </div>

   
  </body>
 </html>
</xsl:template>




<xsl:template name="cssFiles">

 <xsl:for-each select="/page/pageResponse/cssFileList/cssFile">
  <link rel="stylesheet" href="{@path}" type="text/css"/>
 </xsl:for-each>

</xsl:template>

<xsl:template name="metadataItem">
  <xsl:param name="name"/>

  <xsl:variable name="x">
   <xsl:value-of select="/page/pageResponse/metadataList/metadataItem[@name=$name]"/>
  </xsl:variable>

  <xsl:choose>
   <xsl:when test="$x = ''">
    metadataItem '<xsl:value-of select="$name"/>' not defined.
   </xsl:when>
   <xsl:otherwise>
    <xsl:value-of select="$x"/>
   </xsl:otherwise>
  </xsl:choose>
  
</xsl:template>

<xsl:template name="uiItem">
  <xsl:param name="name"/>

  <xsl:variable name="x">
   <xsl:value-of select="/page/pageResponse/uiItemList/uiItem[@name=$name]"/>
  </xsl:variable>

  <xsl:choose>
   <xsl:when test="$x = ''">
    uiItem '<xsl:value-of select="$name"/>' not defined.
   </xsl:when>
   <xsl:otherwise>
    <xsl:value-of select="$x"/>
   </xsl:otherwise>
  </xsl:choose>
  
</xsl:template>


</xsl:stylesheet>
