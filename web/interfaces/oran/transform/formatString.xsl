<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xslt="http://www.w3.org/1999/XSL/Transform"
  xmlns:gslib="http://www.greenstone.org/XSL/Library"
  xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
  xmlns:util="http://org.greenstone.gsdl3.util.XSLTUtil"
  exclude-result-prefixes="util gslib gsf xslt">

  <!-- extension-element-prefixes="java"> -->

  <xsl:strip-space elements="*"/>
  
  <!-- style includes global params interface_name, library_name -->
  
  <!-- the main page layout template is here -->
<!--
  <xsl:template name="xml">
        <xsl:param name="fmt" select="."/>

        <xsl:apply-templates select="$fmt" mode="xml"/>
    </xsl:template>

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="div">
    <h1>a div</h1>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="*">
        <xsl:apply-templates/> 
  </xsl:template>
-->
 
    <xsl:template name="xml">
        <xsl:param name="fmt" select="."/>
        <xsl:apply-templates select="$fmt" mode="xml"/>
    </xsl:template>

  <xsl:template match="td" mode="xml">
    <xsl:choose>
      <xsl:when test="@title">
        <xsl:choose>
          <xsl:when test="@title='td'">
             <xsl:text disable-output-escaping="yes">&lt;td valign="</xsl:text>
             <xsl:value-of select="@valign"/>
             <xsl:text disable-output-escaping="yes">"&gt;</xsl:text>
             <xsl:apply-templates mode="xml"/> 
             <xsl:text disable-output-escaping="yes">&lt;/td&gt;</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates mode="xml"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="xml"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>    

  <xsl:template match="div" mode="xml">
    <xsl:choose>
      <xsl:when test="@title">
        <xsl:choose>

          <!-- GSF TEMPLATE -->
          <xsl:when test="@title='gsf:template'">
            <xsl:text disable-output-escaping="yes">&lt;gsf:template</xsl:text>
            <xsl:apply-templates mode="input"/>  
            <xsl:text disable-output-escaping="yes">&gt;</xsl:text>
            <xsl:apply-templates mode="xml"/> 
            <xsl:text disable-output-escaping="yes">&lt;/gsf:template&gt;</xsl:text>
          </xsl:when>

          <!-- GSF METADATA -->
          <xsl:when test="@title='gsf:metadata'">
            <xsl:text disable-output-escaping="yes">&lt;gsf:metadata name="</xsl:text>
            <!-- if combo is successful then don't do text - probably need to put combo in variable and test it -->
            <xsl:variable name="metadata_test">
              <xsl:apply-templates mode="combo"/>
            </xsl:variable>
            <xsl:choose>
              <xsl:when test="$metadata_test=''">
                <xsl:apply-templates mode="text"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$metadata_test"/>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:text disable-output-escaping="yes">"/&gt;</xsl:text>
          </xsl:when>

          <!-- GSF LINK -->
          <xsl:when test="@title='gsf:link'">
            <xsl:text disable-output-escaping="yes">&lt;gsf:link type="</xsl:text>
            <xsl:apply-templates mode="combo"/>
            <xsl:text disable-output-escaping="yes">"&gt;</xsl:text>
            <xsl:apply-templates mode="xml"/>
            <xsl:text disable-output-escaping="yes">&lt;/gsf:link&gt;</xsl:text>
          </xsl:when>

          <!-- GSF ICON -->
          <xsl:when test="@title='gsf:icon'">
            <xsl:text disable-output-escaping="yes">&lt;gsf:icon type="</xsl:text>  
            <xsl:apply-templates mode="combo"/>
            <xsl:text disable-output-escaping="yes">"&gt;&lt;/gsf:icon&gt;</xsl:text>  
          </xsl:when>

          <!-- GSF CHOOSE -->
          <xsl:when test="@title='gsf:choose-metadata'">
            <xsl:text disable-output-escaping="yes">&lt;gsf:choose-metadata&gt;</xsl:text>
            <xsl:apply-templates mode="xml"/>
            <xsl:text disable-output-escaping="yes">&lt;/gsf:choose-metadata&gt;</xsl:text>  
          </xsl:when>
         
          <!-- GSF SWITCH -->
          <xsl:when test="@title='gsf:switch'">
            <xsl:text disable-output-escaping="yes">&lt;gsf:switch&gt;</xsl:text>
            <xsl:apply-templates mode="xml"/>
            <xsl:text disable-output-escaping="yes">&lt;/gsf:switch&gt;</xsl:text>
          </xsl:when> 

          <xsl:when test="@title='gsf:when'">
            <xsl:text disable-output-escaping="yes">&lt;gsf:when test="exists"&gt;</xsl:text>
            <xsl:apply-templates mode="xml"/>
            <xsl:text disable-output-escaping="yes">&lt;/gsf:when&gt;</xsl:text>
          </xsl:when>

          <xsl:when test="@title='gsf:text'">
            <xsl:apply-templates mode="text"/>  
          </xsl:when>

          <xsl:when test="@title='gsf:otherwise'">
            <xsl:apply-templates mode="xml"/>
          </xsl:when>

          <xsl:when test="@title='gsf:default'">
            <xsl:text disable-output-escaping="yes">&lt;gsf:default&gt;</xsl:text>
            <xsl:apply-templates mode="xml"/>
            <xsl:text disable-output-escaping="yes">&lt;/gsf:default&gt;</xsl:text>
          </xsl:when>

          <xsl:otherwise>
            <xsl:apply-templates mode="xml"/>
          </xsl:otherwise>

        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="xml"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

    
  <!-- Strip out any plain text -->
  <xsl:template match="text()" mode="xml">
    <xsl:apply-templates mode="xml"/>
  </xsl:template>
 
  <!-- Catch all -->
  <xsl:template match="*" mode="xml">
    <xsl:apply-templates mode="xml"/> 
  </xsl:template> 

  <!-- ********************************************************************************************* -->

  <!-- KEEP TEXT mode -->

  <!-- Strip out any plain text -->
  <xsl:template match="text()" mode="text">
    <xsl:text></xsl:text><xsl:value-of select="." disable-output-escaping="yes"/><xsl:text></xsl:text>
    <xsl:apply-templates mode="text"/>
  </xsl:template>

  <!-- Catch all -->
  <xsl:template match="*" mode="text">
    <xsl:apply-templates mode="text"/>
  </xsl:template>

  <!-- ********************************************************************************************* -->

  <!-- INPUT mode - responsible for finding text in input tags -->
  
  <xsl:template match="*" mode="input">
    <xsl:apply-templates mode="input"/>
  </xsl:template>

  <xsl:template match="text()" mode="input">
    <xsl:apply-templates mode="input"/>
  </xsl:template>

  <xsl:template match="input" mode="input">
    <xsl:choose>
    <xsl:when test="@class='match'">
        <xsl:text disable-output-escaping="yes"> match="</xsl:text><xsl:value-of select="@value"/><xsl:text disable-output-escaping="yes">"</xsl:text>
    </xsl:when>
    <xsl:when test="@class='mode'">
        <xsl:text disable-output-escaping="yes"> mode="</xsl:text><xsl:value-of select="@value"/><xsl:text disable-output-escaping="yes">"</xsl:text>
    </xsl:when>
    </xsl:choose>
  </xsl:template>    
  
  <!-- ********************************************************************************************* -->

  <!-- RAW TEXT mode -->
 
  <xsl:template match="*" mode="text">
    <xsl:apply-templates mode="text"/>
  </xsl:template>

  <xsl:template match="input" mode="text">
    <xsl:choose>
    <xsl:when test="@class='text'">
        <xsl:value-of select="@value"/>
    </xsl:when>
    </xsl:choose>
  </xsl:template> 

  <!-- COMBO mode - responsible for extracting value from combo box -->

  <xsl:template match="*" mode="combo">
    <xsl:apply-templates mode="combo"/>
  </xsl:template>

  <xsl:template match="text()" mode="combo">
    <xsl:apply-templates mode="combo"/>
  </xsl:template>

  <xsl:template match="div" mode="combo">
  </xsl:template>  

  <xsl:template match="option" mode="combo">
    <xsl:if test="@selected">
      <xsl:value-of select="@value"/>
    </xsl:if>
  </xsl:template>


  <!-- ********************************************************************************************* -->

  <!-- Main page generation -->

  <xsl:template match="/">
    <!-- <html>
      <head>
	<title>
	  <xsl:call-template name="pageTitle"/><xsl:text> </xsl:text>
	</title>
      </head>
      <body>
        <h2> This should be the format string (unmodified and html version) </h2> -->
        <!-- <xsl:value-of select="/page/pageResponse" disable-output-escaping="yes"/> -->
        <xsl:call-template name="xml">
            <xsl:with-param name="fmt" select="."/>
        </xsl:call-template><xsl:text> </xsl:text>
        <!-- <xsl:apply-templates select="/page/pageResponse" mode="xml"/> -->
      <!-- </body>
    </html> -->
  </xsl:template>
  
  <!--
  <xsl:template name="pageTitle">
    <xsl:value-of select="java:org.greenstone.gsdl3.util.XSLTUtil.getInterfaceText($interface_name, /page/@lang, 'gsdl')"/>
  </xsl:template> -->
  
</xsl:stylesheet>
