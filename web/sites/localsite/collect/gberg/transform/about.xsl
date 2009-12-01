<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  xmlns:gslib="http://www.greenstone.org/skinning"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">

  <!-- style includes global params interface_name, library_name -->
  <xsl:include href="style.xsl"/>

  <xsl:output method="html"/>  
  
  <!-- the main page layout template is here -->
  <xsl:template match="page">
    <xsl:choose> 
      <!-- if we are using the classic GS2 look and feel use this template otherwise use the default GS3 look and feel template -->
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
      <!-- default gs3 mode -->
      <xsl:otherwise> 
	<html>
	  <head>
	    <title>
	      <gslib:aboutCollectionPageTitle/>
	    </title>
	    <link rel="stylesheet" href="interfaces/default/style/core.css" type="text/css"/>
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
	      
	      <div id="content"> 
		<!--Display the description text of the current collection,
		and if some services are available then create a list
		of links for each service within a <ul id="servicelist"> element.-->
		<gslib:collectionDescriptionTextAndServicesLinks/>
	      </div>

	      <div id="footer">
		<div class="divbar"><gslib:poweredByGS3TextBar/></div>
	      </div>
	    </div>
	  </body>
	</html>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
   



  <xsl:template name="pageTitle">
    Sample XML Texts Collection
  </xsl:template>

  <!-- page specific style goes here -->
  <xsl:template name="pageStyle"/>

  <xsl:template match="pageResponse">
    <xsl:apply-templates select="collection"/>
  </xsl:template>

  <!-- this is for gs2 mode -->
  <xsl:template match="collection" priority="5">
    <xsl:if test="$interface_name = 'gs2'">
    <xsl:variable name="collName" select="@name"/>
    <xsl:call-template name="standardPageBanner">
      <xsl:with-param name="collName" select="$collName"/>
      <xsl:with-param name="pageType">about</xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="navigationBar">
      <xsl:with-param name="collName" select="$collName"/>
    </xsl:call-template>   
    </xsl:if>   
    <p/><xsl:value-of select="displayItem[@name='description']" disable-output-escaping='yes'/>
    <p/><xsl:apply-templates select="documentList">
      <xsl:with-param name="collName" select="@name"/>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="documentList">
    <xsl:param name="collName"/>
    <ul>
      <xsl:for-each select="document">
	<li><a href="{$library_name}?a=xd&amp;sa=toc&amp;sc=full&amp;c={$collName}&amp;d={@name}"><xsl:value-of select="metadataList/metadata[@name='Title']"/></a></li>
      </xsl:for-each>
    </ul>
  </xsl:template>
</xsl:stylesheet>