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
	<xsl:template name="pageTitle"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_b')"/></xsl:template>

	<!-- set page breadcrumbs -->
	<xsl:template name="breadcrumbs">
		<gslib:siteLink/><gslib:rightArrow/>
		<xsl:if test="/page/pageResponse/collection">
			<gslib:collectionNameLinked/><gslib:rightArrow/>
		</xsl:if>
	</xsl:template>

	<!-- the page content -->
	<xsl:template match="page">
		<xsl:call-template name="helpTopics"/>
		<xsl:if test="/page/pageResponse/collection">
			<xsl:call-template name="findingInformationHelp"/>
		</xsl:if>
		<xsl:call-template name="readingDocsHelp"/>
		<xsl:call-template name="searchingHelp"/>
		<xsl:call-template name="preferencesHelp"/>
	</xsl:template>
	
	<xsl:template name="helpTopics">
		
	</xsl:template>

	<xsl:template name="findingInformationHelp">
		<xsl:variable name="searchEnabled">
			<xsl:choose>
				<xsl:when test="/page/pageResponse/collection[@name=$collName]/serviceList/service[@type='query']">1</xsl:when>
				<xsl:otherwise>0</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:variable name="numClassifiers">
			<xsl:value-of select="count(/page/pageResponse/collection[@name=$collName]/serviceList/service[@name = 'ClassifierBrowse']/classifierList/classifier)"/>
		</xsl:variable>

		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.findinginformationtitle')"/>
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.findinginformation', $searchEnabled + $numClassifiers)"/>
		
		<!-- using actual <ul> and <li> tags here would cause the XML parser to break -->
		<xsl:text disable-output-escaping="yes">&lt;ul&gt;</xsl:text>
		<xsl:if test="$searchEnabled = 1">
			<xsl:text disable-output-escaping="yes">&lt;li&gt;</xsl:text>
			<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.searchdesc')"/>
			<xsl:text disable-output-escaping="yes">&lt;/li&gt;</xsl:text>
		</xsl:if>
		
		<xsl:for-each select="/page/pageResponse/collection[@name=$collName]/serviceList/service[@name = 'ClassifierBrowse']/classifierList/classifier">
			<xsl:text disable-output-escaping="yes">&lt;li&gt;</xsl:text>
			<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.browsedesc', ./displayItem[@name = 'name'])"/>
			<xsl:text disable-output-escaping="yes">&lt;/li&gt;</xsl:text>
		</xsl:for-each>
		<xsl:text disable-output-escaping="yes">&lt;/ul&gt;</xsl:text>
	</xsl:template>
	
	<xsl:template name="readingDocsHelp">
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.readingdocstitle')"/>
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.readingdocs')"/>
		
		<table>
			<tr>
				<td colspan="2"><img src="interfaces/default/images/bookshelf.png"/></td>
				<td><xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.openbookshelf')"/></td>
			</tr>
			<tr>
				<td><img src="interfaces/default/images/previous.png"/></td>
				<td><img src="interfaces/default/images/next.png"/></td>
				<td><xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.sectionarrows')"/></td>
			</tr>
		</table>
	</xsl:template>
	
	<xsl:template name="searchingHelp">
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.searchingtitle')"/>
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.searching')"/>
		
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.querytermstitle')"/>
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.queryterms')"/>
		
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.querytypetitle')"/>
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.querytype')"/>
		
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.queryscopetitle')"/>
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.queryscope')"/>
	</xsl:template>
	
	<xsl:template name="preferencesHelp">
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.changingpreferencestitle')"/>
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.changingpreferences')"/>
		
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.collectionpreferencestitle')"/>
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.collectionpreferences')"/>
		
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.languagepreferencestitle')"/>
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.languagepreferences')"/>
		
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.presentationpreferencestitle')"/>
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.presentationpreferences')"/>
		
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.searchpreferencestitle')"/>
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.searchpreferences1')"/>
		<xsl:if test="/page/pageResponse/collection[@name=$collName and (@type='mgpp' or @type='mg')]">
			<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.mgsearchpreferences')"/>
		</xsl:if>
		<xsl:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, 'help.searchpreferences2')"/>
	</xsl:template>
</xsl:stylesheet>
