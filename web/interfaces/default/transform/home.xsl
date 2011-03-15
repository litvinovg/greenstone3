<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:gslib="http://www.greenstone.org/skinning"
  extension-element-prefixes="java"
  exclude-result-prefixes="java">

  
  <xsl:template match="page/pageResponse">
    
    <!-- the page layout template is here -->
    <html>
      
      <head>
	<title>
	  <xsl:text> </xsl:text>
	</title>
	<link rel="stylesheet" href="interfaces/default/style/core.css" type="text/css"/>
      </head>
      
      <body>
	
	<div id="page-wrapper">
	  <gslib:displayErrorsIfAny/>
	  
	  
	  <div id="banner">
	    <p>
	      <img src="interfaces/default/images/gsdlhead.gif" class="getTextFor null this.alt.gsdl" />
	    </p>
	  </div>
	  
	  <div id="content" class="moz-output-escape">
	    
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
	    </ul>
	    
	  </div>

	  
	  <div id="footer">
	    <div class="divbar">
	    	<span class="getTextFor gs3power">&amp;nbsp;</span>	
	    </div>
	  </div>
	  
	</div>
	
	<span class="getTextFor null document.title.gsdl">&amp;nbsp;</span>
	
      	<span id="language" style="display: none;"><xsl:value-of select="/page/@lang" /></span>
      	<span id="interface" style="display: none;"><xsl:value-of select="$interface_name" /></span>
	
		<script type="text/javascript">var placeholder = false;</script>
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


