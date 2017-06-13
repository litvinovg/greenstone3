<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util">

	<!-- the page content -->
	<xsl:template match="/page/pageResponse">
					<!-- SLIDER -->
							<xsl:if test="collectionList/collection/displayItemList/displayItem[@name='smallicon']">
			<div class="slider-wrapper theme-halftone">
	            <div id="slider" class="nivoSlider">
					<xsl:call-template name="collSlider"/>
	            </div>
	            <div class="slider-left"><xsl:text> </xsl:text></div>
	            <div class="slider-right"><xsl:text> </xsl:text></div>
	        </div>
			<!-- ENDS SLIDER -->
				</xsl:if>			
			<!-- headline -->
			<div class="headline">Select a collection</div>
			<!-- ENDS headline -->

			<!-- featured -->
			<ul class="feature cf">
				<xsl:call-template name="collList"/>
			</ul>
			<!-- ENDS featured -->
			
	</xsl:template>

	
	
<xsl:template name="collSlider">
<xsl:for-each select="./collectionList/collection">
<xsl:variable name="homeImage" select="displayItemList/displayItem[@name='smallicon']"/>

<xsl:choose>
<xsl:when test="$homeImage">
<xsl:variable name="collectionFolder" select="@name"/>
<xsl:variable name="collectionName" select="displayItemList/displayItem[@name='name']"/>

<a href="{$library_name}/collection/{$collectionFolder}/page/about">
<img src="sites/{$site_name}/collect/{$collectionFolder}/images/{$homeImage}" title="{$collectionName}" />
</a>
</xsl:when>
<xsl:otherwise/>
</xsl:choose>

</xsl:for-each>
</xsl:template>	

<xsl:template name="collList">
<xsl:for-each select="./collectionList/collection">
<xsl:variable name="collectionFolder" select="@name"/>
<xsl:variable name="collectionName" select="displayItemList/displayItem[@name='name']"/>
<xsl:variable name="homeImage" select="displayItemList/displayItem[@name='smallicon']"/>

<li>
	<a href="{$library_name}/collection/{$collectionFolder}/page/about" class="thumb" >
		<xsl:choose>
			<xsl:when test="$homeImage">
				<img src="sites/{$site_name}/collect/{$collectionFolder}/images/{$homeImage}" title="{$collectionName}" />
			</xsl:when>
			<xsl:otherwise>
				<img src="interfaces/{$interface_name}/images/default.jpg" title="{$collectionName}" />
			</xsl:otherwise>
		</xsl:choose>
	</a>
		<a href="{$library_name}/collection/{$collectionFolder}/page/about"  class="excerpt"><xsl:value-of select="$collectionName"/></a>
		 	</li>  
</xsl:for-each>
</xsl:template>		
	
<!--
				<li>
					<a href="single.html" class="thumb" >
						<img src="interfaces/{$interface_name}/img/dummies/t3.jpg" alt="Thumbnail" />
						<div class="img-overlay"><i class="icon-plus-sign"><xsl:text> </xsl:text></i></div>
						<div class="date"><span class="m">JAN</span><span class="d">09</span></div>
					</a>
					<a href="single.html"  class="excerpt">Pellentesque habitant morbi tristique senectus</a>
					<div class="categories"><a href="#" >webdesign, </a><a href="#" >print, </a><a href="#" >photo, </a></div>
				</li>
-->	
	
	
	
	
	<xsl:template name="extra-script">
	<!-- scripts concatenated and minified via build script -->
	<script src="interfaces/{$interface_name}/js/jquery-1.7.1.min.js"><xsl:text> </xsl:text></script>
	<script src="interfaces/{$interface_name}/js/custom.js"><xsl:text> </xsl:text></script>
	
	<!-- superfish -->
	<script  src="interfaces/{$interface_name}/js/superfish-1.4.8/js/hoverIntent.js"><xsl:text> </xsl:text></script>
	<script  src="interfaces/{$interface_name}/js/superfish-1.4.8/js/superfish.js"><xsl:text> </xsl:text></script>
	<script  src="interfaces/{$interface_name}/js/superfish-1.4.8/js/supersubs.js"><xsl:text> </xsl:text></script>
	<!-- ENDS superfish -->
	
	<script src="interfaces/{$interface_name}/js/jquery.nivo.slider.js" ><xsl:text> </xsl:text></script>
	<script src="interfaces/{$interface_name}/js/css3-mediaqueries.js"><xsl:text> </xsl:text></script>
	<script src="interfaces/{$interface_name}/js/tabs.js"><xsl:text> </xsl:text></script>
	<script  src="interfaces/{$interface_name}/js/poshytip-1.1/src/jquery.poshytip.js"><xsl:text> </xsl:text></script>
	<!-- end scripts -->
	
	</xsl:template>	
	
	

</xsl:stylesheet>


