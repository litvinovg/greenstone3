<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:import href="./util.xsl"/>

<xsl:output method="html"/>  

<xsl:template match="/">
 <html>
  <head>
    <title>
     <xsl:call-template name="metadataItem">
      <xsl:with-param name="name">siteName</xsl:with-param>
     </xsl:call-template>
	/
     <xsl:call-template name="metadataItem">
      <xsl:with-param name="name">collectionName</xsl:with-param>
     </xsl:call-template>
    </title>

    <xsl:call-template name="cssInclude"/>
  </head>
  <body>
   <div id="mainContentArea">

    <div id="header">
     <div class="title_minor">

      <a href="{$library}?a=p&amp;sa=home">
       <xsl:call-template name="metadataItem">
        <xsl:with-param name="name">siteName</xsl:with-param>
       </xsl:call-template>
      </a>
      /
     </div>
     <div class="title_major">
      <xsl:call-template name="metadataItem">
       <xsl:with-param name="name">collectionName</xsl:with-param>
      </xsl:call-template>
     </div>
		 
		 <div id="tablist" class="linklist">
     		<xsl:call-template name="service_list" />
		 </div>
    
    </div>

    <div id="content" class="noSidebar">
     <xsl:call-template name="uiItem">
      <xsl:with-param name="name">collectionAbout</xsl:with-param>
     </xsl:call-template>
    </div>


  </div>

   <div id="footer" >
    <xsl:call-template name="uiItem">
     <xsl:with-param name="name">collectionFooter</xsl:with-param>
    </xsl:call-template>
   </div>
   
  </body>
 </html>
</xsl:template>


</xsl:stylesheet>
