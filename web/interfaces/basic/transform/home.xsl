<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  xmlns:gslib="http://www.greenstone.org/skinning"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">

  
  <xsl:template match="page/pageResponse">
    
    <!-- the page layout template is here -->
    <html>
      
      <head>
	<title>
	  <gslib:siteHomePageTitle/>
	</title>
	<link rel="stylesheet" href="interfaces/basic/style/core.css" type="text/css"/>
      </head>
      
      <body><gslib:textDirectionAttribute/>
	
	<div id="page-wrapper">
	  <gslib:displayErrorsIfAny/>
	  
	  
	  <div id="banner">
	    <p>
	      <img src="interfaces/basic/images/gsdlhead.gif"><xsl:attribute name="alt"><gslib:greenstoneLogoAlternateText/></xsl:attribute>
	      </img>
	    </p>
	  </div>
	  
	  <div id="content">
	    
	    <div class="divbar"><gslib:selectACollectionTextBar/></div>
	    
	    <div class="QuickSearch">
	      <gslib:crossCollectionQuickSearchForm/>
	    </div>
	    
	    
	    <ul id="collectionlist">
	      <xsl:for-each select="collectionList/collection"> 
		<li>
		  <gslib:collectionLinkWithImage/>
		</li>
	      </xsl:for-each> 
	    </ul>
	    
	    <gslib:serviceClusterList/>
	    
	    <div class="divbar"><gslib:noTextBar/></div>
	    
	    <ul id="servicelist">
	      <xsl:for-each select="serviceList/service[@type='query']">
		<li><gslib:serviceLink/></li>
	      </xsl:for-each>
	      
	      <xsl:for-each select="serviceList/service[@type='authen']">
		<li><gslib:authenticationLink/></li>
	      </xsl:for-each>
	      <!--uncomment the line below to display a library interface link inside a <li>-->
	      <!--<gslib:libraryInterfaceLink/>-->
	    </ul>
	    
	  </div>

	  
	  <div id="footer">
	    <div class="divbar"><gslib:poweredByGS3TextBar/></div>
	  </div>
	  
	</div>
	
		<script type="text/javascript" src="jquery.js">
			<xsl:comment>Filler for browser</xsl:comment>
		</script>
		<script type="text/javascript" src="test.js">
			<xsl:comment>Filler for browser</xsl:comment>
		</script>
	
      </body>
    </html>
    
    
  </xsl:template>
</xsl:stylesheet>  


