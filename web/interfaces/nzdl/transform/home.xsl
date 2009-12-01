<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">

  <!-- style includes global params interface_name, library_name -->
  <xsl:include href="style.xsl"/>
  <xsl:include href="home-text.xsl"/>
  <xsl:include href="page-common.xsl"/>

  <xsl:output method="html"/> 

    <!-- the main page layout template is here -->
  <xsl:template match="page">
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
	<xsl:attribute name="dir"><xsl:call-template name="direction"/></xsl:attribute>
	<div id="page-wrapper">
	   <xsl:apply-templates select="pageResponse"/>
	   <xsl:call-template name="greenstoneFooter"/>                	   
	</div>
      </body>
    </html>
  </xsl:template>  

  <xsl:template name="pageTitle">
    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'nzdl')"/>
  </xsl:template>

  <!-- page specific style goes here -->
  <xsl:template name="pageStyle"/>

  <xsl:template match="pageResponse">
    <xsl:call-template name="nzdlPageBanner"/>     
    <center>
    <xsl:apply-templates select="collectionList"/>
    </center>
    <center><xsl:call-template name="dividerBar"/></center>    
    <p />
    <center><h2><xsl:call-template name="textprojhead"/></h2></center>
    
    <table border='0' cellpadding='5'>
      <xsl:call-template name="project_info"/>
      <xsl:call-template name="software_info"/>
      <xsl:call-template name="research_info"/>
      <xsl:call-template name="affiliate_info"/>
    </table>
    <center>
      <xsl:call-template name="dividerBar"/>
      <xsl:call-template name="greenstone_info"/>
      <p />
      <xsl:call-template name="nzdlpagefooter"/> 
      <br />September 2003
    </center>
  </xsl:template>

  <xsl:template name="nzdlPageBanner">
    <div><img src="interfaces/nzdl/images/nzdl2gr.gif" width="457" height="181" ><xsl:attribute name="alt"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'nzdl')"/></xsl:attribute></img></div>
  </xsl:template>

  <xsl:template name="project_info">
    <tr valign='top'>
      <td>
	<table><tr valign='middle'><td><img src="interfaces/nzdl/images/gbull.gif"/></td><td><a href="mailto:greenstone@cs.waikato.ac.nz">Feedback</a></td></tr></table>
	<table><tr valign='middle'><td><img src="interfaces/nzdl/images/gbull.gif"/></td><td><a href="http://www.nzdl.org/html/people.html">People</a></td></tr></table>
	<table><tr valign='middle'><td><img src="interfaces/nzdl/images/gbull.gif"/></td><td><a href="http://greenstone.cs.uct.ac.za/gsdl/cgi-bin/library">African Digital Library Centre</a></td></tr></table>
<table><tr valign='middle'><td><img src="interfaces/nzdl/images/gbull.gif"/></td><td><a href="http://sadl.uleth.ca">Southern Alberta Digital Library Centre</a></td></tr></table>
      </td>
      <td><xsl:call-template name="textprojinfo"/></td>
    </tr>
  </xsl:template>

  <xsl:template name="software_info">
    <tr>
      <td></td>
      <th align='left'><xsl:call-template name="titlesoftwareinfo"/></th>
    </tr>
    <tr valign='top'>
      <td>
	<table><tr valign='middle'><td><img src="interfaces/nzdl/images/gbull.gif"/></td><td><a href="{$library_name}?a=p&amp;sa=gsdl">About<br />Greenstone 2</a></td></tr></table>
	<table><tr valign='middle'><td><img src="interfaces/nzdl/images/gbull.gif"/></td><td><a href="http://www.greenstone.org/english/docs.html">Greenstone 2<br />Documentation</a></td></tr></table>
      </td>
      <td><xsl:call-template name="textsoftwareinfo"/></td>
    </tr>
  </xsl:template>
  
  <xsl:template name="research_info">
    <tr>
    <td></td>
    <th align='left'><xsl:call-template name="titleresearchinfo"/></th>
  </tr>
    <tr valign='top'>
      <td>
	<table><tr valign='middle'><td><img src="interfaces/nzdl/images/gbull.gif"/></td><td><a href="http://www.nzdl.org/html/research.html">Research</a></td></tr></table>
	<table><tr valign='middle'><td><img src="interfaces/nzdl/images/gbull.gif"/></td><td><a href="http://www.nzdl.org/html/projects.html">Projects</a></td></tr></table>
	<table><tr valign='middle'><td><img src="interfaces/nzdl/images/gbull.gif"/></td><td><a href="http://www.nzdl.org/html/software.html">Software<br />Downloads</a></td></tr></table>
	<table><tr valign='middle'><td><img src="interfaces/nzdl/images/gbull.gif"/></td><td><a href="http://www.cs.waikato.ac.nz/~nzdl/publications/">Publications</a></td></tr></table>
      </td>
      <td><xsl:call-template name="textresearchinfo"/></td>
    </tr>
    
<!--    <tr valign='top'>
      <td>
	<table><tr valign='middle'><td><img src="interfaces/nzdl/images/gbull.gif"/></td><td><a href="http://nzdl2.cs.waikato.ac.nz/cgi-bin/zdemo/library">Z39.50<br />demo</a></td></tr></table>
      </td><td>
	We have an experimental facility for searching Z39.50 collections using  Greenstone.  Ultimately we plan a full bibliographic search facility, with  the ability to combine tests on different fields, and to be able to search multiple collections, including cross-searching Greenstone and Z39.50 collections.
      </td>
    </tr>-->
  </xsl:template>

  <xsl:template name="affiliate_info">
    <tr>
      <td></td>
      <th align='left'><xsl:call-template name="titleaffiliateinfo"/></th>
    </tr>
    <tr valign='top'>
      <td>
	<img src="interfaces/nzdl/images/ghproj.jpg" alt="Human Info NGO" width='100' height='90'/>
      </td>
      <td><xsl:call-template name="textaffiliatehumaninfo"/></td>
    </tr>
    <tr valign='top'>
      <td><img src="interfaces/nzdl/images/unesco.gif" alt="UNESCO" width='100' height='90' />
      </td>
      <td><xsl:call-template name='textaffiliateunesco'/></td>
      </tr>
  </xsl:template>
  
  <xsl:template match="collection">
  <xsl:param name="extraArgs"/>
    <xsl:variable name="ct"><xsl:choose><xsl:when test="metadataList/metadata[@name='buildType']='mgpp'">1</xsl:when><xsl:otherwise>0</xsl:otherwise></xsl:choose></xsl:variable>
    <p />
    <a href="{$library_name}?a=p&amp;sa=about&amp;c={@name}&amp;ct={$ct}{$extraArgs}">
      <img width="150" border="1">
	<xsl:attribute name="src">
	  <xsl:value-of select="metadataList/metadata[@name='httpPath']"/>/images/<xsl:choose><xsl:when test="displayItem[@name='smallicon']"><xsl:value-of select="displayItem[@name='smallicon']"/></xsl:when><xsl:otherwise><xsl:value-of select="displayItem[@name='icon']"/></xsl:otherwise></xsl:choose>
	</xsl:attribute>
	<xsl:attribute name="alt">
	  <xsl:value-of select="displayItem[@name='name']"/>
	</xsl:attribute>
      </img>
    </a>
    <p />
  </xsl:template> 


  <xsl:template match="collectionList">
    <center>
      <xsl:call-template name="dividerBar"><xsl:with-param name="text">Humanitarian and UN collections</xsl:with-param></xsl:call-template>
      <p /><xsl:call-template name="collectfao"/>
      <xsl:call-template name="dividerBar"><xsl:with-param name="text">Demonstration collections</xsl:with-param></xsl:call-template> 
      <p /><xsl:call-template name="collectrest"/>
    </center>
  </xsl:template>

  <xsl:template name="collectfao">
    <table width="537">
      <tr valign='top'>
	<td align='center'>
	  <p /><xsl:apply-templates select="collection[@name='hdl']"/> 
	  <p /><xsl:apply-templates select="collection[@name='fnl']"/>
 	  <p /><xsl:apply-templates select="collection[@name='envl']"/> 
	  <p /><xsl:apply-templates select="collection[@name='aginfo']"/> 
	  <p /><xsl:apply-templates select="collection[@name='gtz']"/> 
	  <p /><xsl:apply-templates select="collection[@name='cdl']"/> 
	  <p /><xsl:apply-templates select="collection[@name='edudev']"/>
	  <p /><xsl:apply-templates select="collection[@name='nigeria']"/>
	  <p /><xsl:apply-templates select="collection[@name='safem']"/>
	  <p />firstaid<xsl:apply-templates select="collection[@name='firstaid']"/>
	</td>
	<td align='center'>
	  <p /><xsl:apply-templates select="collection[@name='mhl']"/> 
	  <p /><xsl:apply-templates select="collection[@name='fnl1_1']"/> 
	  <p /><xsl:apply-templates select="collection[@name='paho']"/>
	  <p /><xsl:apply-templates select="collection[@name='unesco']"/>
          <p /><xsl:apply-templates select="collection[@name='muster']"/>
	  <p /><xsl:apply-templates select="collection[@name='unescoen']"/>
	  <p /><xsl:apply-templates select="collection[@name='dfid']"/>	
	  <p /><xsl:apply-templates select="collection[@name='ewf']"/>	
	  <p /><xsl:apply-templates select="collection[@name='fi1998']"/> 
	  <p /><xsl:apply-templates select="collection[@name='faodocs']"/>  
	</td>
	<td align='center'> 
	  <p /><xsl:apply-templates select="collection[@name='ccgi']"/> 
	  <p /><xsl:apply-templates select="collection[@name='fnl2.2']"/> 
	  <p /><xsl:apply-templates select="collection[@name='who']"/> 
	  <p /><xsl:apply-templates select="collection[@name='tulane']"/> 
	  <p /><xsl:apply-templates select="collection[@name='unaids']"/> 
	  <p /><xsl:apply-templates select="collection[@name='aedl']"/>
	  <p /><xsl:apply-templates select="collection[@name='whoedm']"/>
	  <p /><xsl:apply-templates select="collection[@name='helid']"/>
	  <p /><xsl:apply-templates select="collection[@name='ipc']"/>	
	  <p /><xsl:apply-templates select="collection[@name='povsem']"/>
	</td>
      </tr>
    </table>
  </xsl:template>
  

  <xsl:template name="collectrest">
    <table width='537'>
      <tr valign='top'>
	<td align='center'>
	  <p /><xsl:apply-templates select="collection[@name='acrodemo']"/> 
	  <p /><xsl:apply-templates select="collection[@name='niupepa']"/> 
	  <p />howto DL<xsl:apply-templates select="collection[@name='howto']"/> 
	  <p /><xsl:apply-templates select="collection[@name='arabic']"/> 
	  <p /><xsl:apply-templates select="collection[@name='chinese']">
	  <xsl:with-param name="extraArgs">&amp;l=zh</xsl:with-param>
	  </xsl:apply-templates>	 
 	  <p /><xsl:apply-templates select="collection[@name='folktale']"/> 
	  <p /><xsl:apply-templates select="collection[@name='demooai']"/> 
	  <p /><xsl:apply-templates select="collection[@name='tidbits']"/> 
	</td>
	<td align='center'>
	  <p />meldex<xsl:apply-templates select="collection[@name='meldex']"/> 
	  <p /><xsl:apply-templates select="collection[@name='wordpdf']"/> 
	  <p /><xsl:apply-templates select="collection[@name='musvid']"/> 
	  <p /><xsl:apply-templates select="collection[@name='gberg']"/> 
 	  <p /><xsl:apply-templates select="collection[@name='allshake']"/> 
	  <p /><xsl:apply-templates select="collection[@name='aircraft']"/> 
	  <p /><xsl:apply-templates select="collection[@name='csbib']"/> 
	  <p /><xsl:apply-templates select="collection[@name='coltbib']">
	  <xsl:with-param name="extraArgs">&amp;qt=1</xsl:with-param>
	  </xsl:apply-templates> 
	</td>
	<td align='center'>
	  <p /><xsl:apply-templates select="collection[@name='cstr']"/> 
	  <p /><xsl:apply-templates select="collection[@name='gsarch']"/> 
	  <p /><xsl:apply-templates select="collection[@name='ohist']"/> 
	  <p /><xsl:apply-templates select="collection[@name='hcibib']"/> 
 	  <p /><xsl:apply-templates select="collection[@name='whist']"/> 
	  <p /><xsl:apply-templates select="collection[@name='beowulf']"/>
	  <p /><xsl:apply-templates select="collection[@name='tcc']"/>
	  <p /><xsl:apply-templates select="collection[@name='jair']"/>  
	</td>
      </tr>
    </table>
  </xsl:template>
  
</xsl:stylesheet>  




