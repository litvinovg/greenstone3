<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"

	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
    xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
    
    xmlns:lxslt="http://xml.apache.org/xslt"
    xmlns:result="http://www.example.com/results"
    xmlns:exsl="http://exslt.org/common"

	extension-element-prefixes="java util result exsl"
	exclude-result-prefixes="util java util">

	<xsl:include href="header.xsl"/>
	<xsl:include href="formatmanager.xsl"/>
	

	<!-- put the URL or path of your site here site -->
	<!-- eg 'http://www.example.com/mysite' or '/mysite'  -->
	<xsl:template name="siteURL"><xsl:value-of select="/page/pageResponse/metadataList/metadata[@name='siteURL']"/></xsl:template>


	<!-- the output format for this layout is html -->
    <!-- <xsl:output method="xml" version="1.0" encoding="UTF-8" doctype-public="-//W3C//DTD XHTML 1.1//EN" doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd" indent="yes" omit-xml-declaration="yes"/> -->
	<xsl:output method="html" omit-xml-declaration="yes"/> 

	<!-- the main layout is defined here -->
	<xsl:template match="/">

		<html>

			<head>
				<!-- ***** in header.xsl ***** -->
				<xsl:call-template name="create-html-header"/>
				<xsl:call-template name="berryBasketHeadTags"/>	
			</head>
			
			<body><xsl:call-template name="textDirectionAttribute"/><xsl:call-template name="actionClass"/>
				
				<xsl:call-template name="displayErrorsIfAny"/>
				<a name="top"><xsl:text> </xsl:text></a>
				<div id="container">

					<div id="gs_banner">
						<!-- ***** in header.xsl ***** -->
						<xsl:call-template name="create-banner"/>
					</div>
					
					<xsl:if test="/page/pageRequest/paramList/param[(@name='formatedit') and (@value='on')]">
						<xsl:call-template name="formatmanagerpre"/>
					</xsl:if>
    
					<div id="gs_content">
						<!--
							show the content of the page.
							to customise this part, edit the xsl file for the page you want to edit
						-->
						<xsl:apply-templates select="/page"/>

					</div>
					
					<!--<xsl:call-template name="formatmanagerpost"/>-->

					<div id="gs_footer">
						<a href="http://www.greenstone.org"><xsl:call-template name="poweredByGS3TextBar"/></a>
					</div>

					<div class="corner" id="cornerTopLeft"><xsl:text> </xsl:text></div>
					<div class="corner" id="cornerTopRight"><xsl:text> </xsl:text></div>
					<div class="corner" id="cornerBottomLeft"><xsl:text> </xsl:text></div>
					<div class="corner" id="cornerBottomRight"><xsl:text> </xsl:text></div>
				</div>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>


