<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:gslib="http://www.greenstone.org/skinning"
  extension-element-prefixes="java"
  exclude-result-prefixes="java">

  
  <xsl:template match="page">
    
    <!-- the page layout template is here -->
    <html>
      <head>
	<title>
	  <gslib:aboutCollectionPageTitle/>
	</title>
	<link rel="stylesheet" href="interfaces/basic/style/core.css" type="text/css"/>
      </head>
      
      <body><gslib:textDirectionAttribute/>

	<div id="page-wrapper">
	  <gslib:displayErrorsIfAny/>

	  <div id="banner">    
	    <p>
	      <gslib:collectionHomeLinkWithLogoIfAvailable/>
	    </p>
	    <ul id="bannerlist"> 
	      <li><gslib:homeButtonTop/></li>
	      <li><gslib:helpButtonTop/></li>
	      <li><gslib:preferencesButtonTop/></li>
	    </ul>
	  </div>
	  
	  <!--If some services are available for the current collection display the navigation bar-->
	  <xsl:choose>
	    <xsl:when test="$this-element/serviceList/service">
	      <div id="navbar">
		<ul id="navbarlist">
		  <gslib:servicesNavigationBar/>
		</ul>
	      </div> 
	    </xsl:when>
	    <!--Otherwise simply display a blank divider bar-->
	    <xsl:otherwise>
	      <div class="divbar"><gslib:noTextBar/></div>          
	    </xsl:otherwise>
	  </xsl:choose>
	  
	  <div id="content" class="moz-output-escape"> 
	    <!--Display the description text of the current collection,
	    and if some services are available then create a list
	    of links for each service within a <ul id="servicelist"> element.-->
	    <gslib:collectionDescriptionTextAndServicesLinks/>
	  </div>

	  <div id="footer">
	    <div class="divbar"><gslib:poweredByGS3TextBar/></div>
	  </div>
	</div>
      	<span id="language" style="display: none;"><xsl:value-of select="/page/@lang" /></span>
      	<span id="interface" style="display: none;"><xsl:value-of select="$interface_name" /></span>
      	
		<script type="text/javascript"><xsl:text>var placeholder = false;</xsl:text></script>
		<script type="text/javascript" src="jquery.js"><xsl:comment>jQuery</xsl:comment></script>
		<script type="text/javascript" src="test.js"><xsl:comment>Client side transforms</xsl:comment></script>
		
      </body>
    </html>
  </xsl:template>


</xsl:stylesheet>  

