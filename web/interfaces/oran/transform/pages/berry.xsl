<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util">

	<!-- use the 'main' layout -->
	<xsl:include href="layouts/main.xsl"/>

	<!-- set page title -->
	<xsl:template name="pageTitle"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.berrybasket')"/></xsl:template>

	<!-- set page breadcrumbs -->
	<xsl:template name="breadcrumbs"><gslib:siteLink/><gslib:rightArrow/></xsl:template>

	<!-- the page content -->
	<xsl:template match="/page">
		<xsl:variable name="clusterName"><xsl:value-of select="/page/pageRequest/paramList/param[@name='c']/@value"/></xsl:variable>
		<script type="text/javascript" src="interfaces/oran/js/berrycheckout.js"><xsl:text> </xsl:text></script>
		<script type="text/javascript">
			var doc;
			var docList = new Array();
			<xsl:for-each select="/page/pageResponse/collection/item">
				<xsl:text disable-output-escaping="yes">
			doc = new Array();</xsl:text>
				<xsl:for-each select="@*">
					<xsl:text disable-output-escaping="yes">
			doc["</xsl:text><xsl:value-of select="name()" /><xsl:text disable-output-escaping="yes">"]='</xsl:text><xsl:value-of select="." /><xsl:text disable-output-escaping="yes">';</xsl:text>
				</xsl:for-each>
				<xsl:text disable-output-escaping="yes">
			docList[</xsl:text>
				<xsl:value-of select="position()-1"/>
				<xsl:text>] = doc;
</xsl:text>
			</xsl:for-each>
		</script>

		<div class="navList" id="berryCheckoutOptions">
			<ul>
				<li id="fullview" class="current"><span>Full View</span></li>
				<li id="textview"><span>Text View</span></li>
				<li id="email"><span>Email</span></li>
			</ul>
			<div class="clear"><xsl:text> </xsl:text></div>
		</div>
		<div id="berryBasketContent"><span><xsl:text> </xsl:text></span></div>

	</xsl:template>



</xsl:stylesheet>  

