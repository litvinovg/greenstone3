<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util gsf">

  <xsl:template name="panoramaViewerFeaturesJSONNodes">
    <div id="jsonPanoNodes" style="display:none;">
      <xsl:text>[</xsl:text>
      <xsl:for-each select="//documentNode">
	<xsl:if test="metadataList/metadata[@name = 'Latitude'] and metadataList/metadata[@name = 'Longitude'] and metadataList/metadata[@name = 'PhotoType']='Panorama'">
	  <xsl:text>{</xsl:text>
	  <xsl:text disable-output-escaping="yes">"nodeID":"</xsl:text><xsl:value-of select="@nodeID"/><xsl:text disable-output-escaping="yes">",</xsl:text>
	  <xsl:text disable-output-escaping="yes">"source":"</xsl:text><xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name = 'Source']"/>"<xsl:text>,</xsl:text>
	  <xsl:text disable-output-escaping="yes">"lat":</xsl:text><xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name = 'Latitude']"/><xsl:text>,</xsl:text>
	  <xsl:text disable-output-escaping="yes">"lng":</xsl:text><xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name = 'Longitude']"/>
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
    <gsf:metadata name="Image" hidden="true"/>
    <gsf:metadata name="SourceFile" hidden="true"/>
    <gsf:metadata name="assocfilepath" hidden="true"/>
    <gsf:metadata name="PhotoType" hidden="true"/>
    <gsf:metadata name="cv.rotation" hidden="true"/>
    <gsf:metadata name="ex.Angle" hidden="true"/>
    <gsf:metadata name="Angle" hidden="true"/>
    
  </xsl:template>
  
  <xsl:template name="panoramaViewerFeatures">

    <div id="asdf">
      <div id="pano-container" class="pano_canvas_half" style="position: relative">
	<div style="position: absolute; top: 0px; right: 0px;"><a id="gofullscreen" style="background-color: #008000;" href="">Go Fullscreen</a></div>
	<xsl:text> </xsl:text>
      </div>
    </div>
    



    <gsf:script>
<![CDATA[
	    var cgiargs = '<xsl:template name="mainTemplate"> \
		<html> \
			<head> \
				<xsl:call-template name="create-html-header"/> \
			</head> \
			\
			<body><xsl:call-template name="textDirectionAttribute"/><xsl:call-template name="actionClass"/> \
				<div id="container"> \
					<div id="gs_content"> \
						<xsl:apply-templates select="/page"/> \
					</div> \
				</div> \
			</body> \
		</html> \
	      </xsl:template> \
	      \
   	      <xsl:template match="/page/pageResponse"> \
		<xsl:call-template name="panoramaViewerFeaturesJSONNodes"/> \
		<div id="asdf"> \
	          <div id="pano-container" class="pano_canvas_fullscreen"> \
	            <xsl:text> </xsl:text> \
	         </div> \
	       </div> \
	      </xsl:template>';

	      // encodeURIComponent(cgiargs);
	      // cgiargs.replace(/ /g,"%20")

	      var full_url = document.location + "?ilt=" + escape(cgiargs);
	      $('#gofullscreen').attr('href',full_url);
]]>
    </gsf:script>
    

  </xsl:template>
	

  <xsl:template name="panoramaViewerFeaturesIcon">
    <td style="padding-left:5px; padding-right:5px;" valign="top">
      <a href="javascript:switchPanorama('{@nodeID}');">
	<img src="interfaces/default/images/map_marker.png"/>
      </a>
    </td>
  </xsl:template>

</xsl:stylesheet>

