<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">

  <!-- style includes global params interface_name, library_name -->
  <xsl:include href="style.xsl"/>
  <xsl:include href="gsdl-text.xsl"/>
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
    <center>
    <xsl:call-template name="genericPageBanner"><xsl:with-param name='text'><xsl:call-template name="aboutgs"/></xsl:with-param></xsl:call-template>
    </center>
    <p />
    <table border='0' cellspacing='0' cellpadding='0'>
      <tr>
	<td valign='top' align='center'><xsl:call-template name="imagegreenstone"/></td>
	<td valign='top'><xsl:call-template name="textgreenstone1"/>
	  <br /><br />
	</td>
      </tr>
      <tr>
	<td valign='top' align='center'>
	  <h2><a href="http://nzdl.org">nzdl.org</a></h2>
	</td>
	<td valign='top'><xsl:call-template name="textgreenstone2"/>
	  <br /><br />
	</td>
      </tr>
      <tr>
	<td valign='top' align='center'>
	  <h2><xsl:call-template name="textplatformtitle"/></h2>
	</td>
	<td valign='top'><xsl:call-template name="textgreenstone3"/>
	  <br /><br />
	</td>
      </tr>
      <tr>
	<td valign='top' align='center'>
	  <a href="http://nzdl.org/hdl"><img border='0' width='101' height='100' src="interfaces/nzdl/images/hdlimg.jpg"/></a>
	</td>
	<td valign='top'>
	  <xsl:call-template name="textgreenstone4"/>
	  <br /><br />
	</td>
      </tr>
      <tr>
	<td valign='top' align='center'>
	  <h2><xsl:call-template name="textcustomisationtitle"/></h2>
	</td>
	<td valign='top'><xsl:call-template name="textgreenstone5"/>
	  <br /><br />
	</td>
      </tr>
      <tr>
	<td valign='top' align='center'>
	  <h2><a href="http://www.greenstone.org/english/docs.html"><xsl:call-template name="textdocumentationtitle"/></a></h2>
	</td>
	<td valign='top'><xsl:call-template name="textdocuments"/>
	<br /><br />
	</td>
      </tr>
      <tr>
	<td valign='top' align='center'>
	  <h2><xsl:call-template name="textmailinglisttitle"/></h2>
	</td>
	<td valign='top'><xsl:call-template name="textmailinglist"/>
	  <br /><br />
	</td>
      </tr>
      <tr>
	<td valign='top' align='center'>
	  <h2><xsl:call-template name="textbugstitle"/></h2>
	</td>
	<td valign='top'><xsl:call-template name="textreport"/>
	  <br /><br />
	</td>
      </tr>
       <tr>
         <td valign='top' align='center'>
	   <h2><xsl:call-template name="textgs3title"/></h2>
	 </td>
	 <td valign='top'><xsl:call-template name="textgs3"/>
	   <br /><br />
	 </td>
      </tr>

      <tr>
	<td valign='top' align='center'>
	  <h2><xsl:call-template name="textcreditstitle"/></h2>
	</td>
	<td valign='top'><xsl:call-template name="textwhoswho"/>
	</td>
      </tr>
    </table>
    <center>
    <p /><xsl:call-template name="dividerBar"/>
    <xsl:call-template name="greenstone_info"/>
    <xsl:call-template name="nzdlpagefooter"/>
    </center>
  </xsl:template>

</xsl:stylesheet>

