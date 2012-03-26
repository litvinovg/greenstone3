<?xml version="1.0" encoding="ISO-8859-1"?><xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util gsf">

	<!-- use the 'main' layout -->
	<xsl:import href="layouts/main.xsl"/>
	
	<!-- set page title -->
	<xsl:template name="pageTitle"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'db.doc_basket')"/></xsl:template>
	
	<!-- set page breadcrumbs -->
	<xsl:template name="breadcrumbs"><gslib:siteLink/><gslib:rightArrow/> 
	<gslib:collectionNameLinked/><gslib:rightArrow/></xsl:template>

	<xsl:template name="init-seaweed"/>
	
	<!-- the page content -->
	<xsl:template match="/page">
		<!-- Make sure the user is allowed to use this functionality -->
		<xsl:if test="/page/pageRequest/userInformation and (util:contains(/page/pageRequest/userInformation/@groups, 'administrator') or util:contains(/page/pageRequest/userInformation/@groups, 'all-collections-editor') or util:contains(/page/pageRequest/userInformation/@groups, $thisCollectionEditor))">
			<script type="text/javascript" src="interfaces/{$interface_name}/js/documentmaker_scripts.js"><xsl:text> </xsl:text></script>
			<script type="text/javascript" src="interfaces/{$interface_name}/js/documentmaker_scripts_dd.js"><xsl:text> </xsl:text></script>
			<script type="text/javascript" src="interfaces/{$interface_name}/js/documentmaker_scripts_util.js"><xsl:text> </xsl:text></script>
			<script type="text/javascript">YAHOO.util.Event.onDOMReady(initDocumentMaker);</script>

			<link src="interfaces/{$interface_name}/style/documentbasket.js"/>
			<xsl:for-each select="//item">
				<a class="dbdoc">
					<xsl:attribute name="href"><xsl:value-of select="$library_name"/>?a=d&amp;c=<xsl:value-of select="/page/pageResponse/collection/@name"/>&amp;dt=hierarchy&amp;d=<xsl:value-of select="@name"/>&amp;p.a=b&amp;p.s=<xsl:value-of select="/page/pageResponse/service/@name"/>&amp;ed=1</xsl:attribute>
					<xsl:value-of select="@title"/>
				</a>
			</xsl:for-each>
			
			<xsl:call-template name="document-editor-language-fragments"/>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>  



