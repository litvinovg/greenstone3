<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util gsf">


  <xsl:template name="mapFeaturesIcon">
    <td style="padding-left:5px; padding-right:5px;" valign="top">
      <a href="javascript:focusDocument('{@nodeID}');">
	<img src="interfaces/default/images/map_marker.png"/>
      </a>
    </td>
  </xsl:template>
  
  <xsl:template name="mapFeaturesJSONNodes">
    <div id="jsonNodes" style="display:none;">
      <xsl:text>[</xsl:text>
      <xsl:for-each select="//documentNode">
	<xsl:if test="metadataList/metadata[@name = 'Latitude'] and metadataList/metadata[@name = 'Longitude']">
	  <xsl:text>{</xsl:text>
	  <xsl:text disable-output-escaping="yes">"nodeID":"</xsl:text>
	  <xsl:value-of select="@nodeID"/>
	  <xsl:text disable-output-escaping="yes">",</xsl:text>
	  <xsl:text disable-output-escaping="yes">"title":"</xsl:text>
	  <xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name = 'Title']"/>
	  <xsl:text disable-output-escaping="yes">",</xsl:text>
	  <xsl:text disable-output-escaping="yes">"lat":</xsl:text>
	  <xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name = 'Latitude']"/>
	  <xsl:text>,</xsl:text>
	  <xsl:text disable-output-escaping="yes">"lng":</xsl:text>
	  <xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name = 'Longitude']"/>
	  <xsl:text>}</xsl:text>
	  <xsl:if test="not(position() = count(//documentNode))">
	    <xsl:text>,</xsl:text>
	  </xsl:if>
				</xsl:if>
      </xsl:for-each>
      <xsl:text>]</xsl:text>
    </div>
   
    <!-- Although these aren't visible, they are necessary because it forces Greenstone to include this metadata in the page xml -->
    <gsf:metadata name="Latitude" hidden="true"/>
    <gsf:metadata name="Longitude" hidden="true"/>
<!--  these were included in version in query. don't think we need them...
    <gsf:metadata name="Image" hidden="true"/>
    <gsf:metadata name="SourceFile" hidden="true"/>
    <gsf:metadata name="assocfilepath" hidden="true"/>
    <gsf:metadata name="PhotoType" hidden="true"/>
    <gsf:metadata name="cv.rotation" hidden="true"/>
    <gsf:metadata name="Angle" hidden="true"/> -->
    
    
  </xsl:template>

  <xsl:template name="mapFeaturesMap">
    <div id="map_canvas" class="map_canvas_half"><xsl:text> </xsl:text></div>
  </xsl:template>


</xsl:stylesheet>

