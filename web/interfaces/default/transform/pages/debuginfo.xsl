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
	<xsl:variable name="debug_property_file" select="concat($interface_name, '2')"/>
	<!-- set page title -->
	<xsl:template name="pageTitle">
		<gslib:collectionName/>
	</xsl:template>

	<!-- set page breadcrumbs -->
	<xsl:template name="breadcrumbs">
		<gslib:siteLink/>
		<gslib:rightArrow/>
	</xsl:template>

	<!-- the page content -->
	<xsl:template match="/page">
		<p>
			<xsl:value-of select="util:getInterfaceText($debug_property_file, /page/@lang, 'debuginfo.usage')"/>
		</p>

		<dl>
			<dt>o=xml</dt>
			<dd>
				<xsl:value-of select="util:getInterfaceText($debug_property_file, /page/@lang, 'debuginfo.xml')"/>
			</dd>
			<dt>o=xmlfinal</dt>
			<dd>
				<xsl:value-of select="util:getInterfaceText($debug_property_file, /page/@lang, 'debuginfo.xmlfinal')"/>
			</dd>
			<dt>o=skindoc</dt>
			<dd>
				<xsl:value-of select="util:getInterfaceText($debug_property_file, /page/@lang, 'debuginfo.skindoc')"/>
			</dd>
			<dt>o=skinandlib</dt>
			<dd>
				<xsl:value-of select="util:getInterfaceText($debug_property_file, /page/@lang, 'debuginfo.skinandlib')"/>
			</dd>
			<dt>o=skinandlibdoc</dt>
			<dd>
				<xsl:value-of select="util:getInterfaceText($debug_property_file, /page/@lang, 'debuginfo.skinandlibdoc')"/>
			</dd>
			<dt>formatedit=on</dt>
			<dd>
				<xsl:value-of select="util:getInterfaceText($debug_property_file, /page/@lang, 'debuginfo.formatedit')"/>
			</dd>
		</dl>	  

		<!-- better to do the following with a util:getInterfaceText
	       so language independent -->
		<p>
			<xsl:value-of select="util:getInterfaceText($debug_property_file, /page/@lang, 'debuginfo.refreshconfig')"/>
		</p>

		<dl>
			<!--<a href="http://host:port/greenstone3/library?a=s&sa=c">a=s&amp;sa=c</a>-->
			<dt>
				<a href="{$library_name}?a=s&amp;sa=c">a=s&amp;sa=c</a>
			</dt>

			<dd>
				<xsl:value-of select="util:getInterfaceText($debug_property_file, /page/@lang, 'debuginfo.reconfigsite')"/>
			</dd>
		</dl>

		<a href="{$library_name}?a=de&amp;c={/page/pageResponse/collection/@name}">Despositor</a>

	</xsl:template>
</xsl:stylesheet>  

