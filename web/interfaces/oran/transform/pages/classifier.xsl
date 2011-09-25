<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util">
	
	<!-- use the 'main' layout -->
	<xsl:import href="layouts/main.xsl"/>
	<xsl:include href="classifiertools.xsl"/>

	<!-- set page title -->
	<xsl:template name="pageTitle"><gslib:serviceName/></xsl:template>

	<!-- set page breadcrumbs -->
	<xsl:template name="breadcrumbs"><gslib:siteLink/><gslib:rightArrow/> <gslib:collectionNameLinked/><gslib:rightArrow/></xsl:template>

	<!-- the page content -->
	<xsl:template match="/page/pageResponse">
		<script type="text/javascript" src="interfaces/{$interface_name}/js/classifier_scripts.js"><xsl:text> </xsl:text></script>
		
		<!-- this right sidebar -->
		<xsl:if test="$berryBasketOn or $documentBasketOn">
			<div id="rightSidebar">
				<xsl:if test="$berryBasketOn">
					<!-- show the berry basket if it's turned on -->
					<gslib:berryBasket/>
					<xsl:text> </xsl:text>
				</xsl:if>

				<xsl:if test="$documentBasketOn">
					<gslib:documentBasket/>
					<xsl:text> </xsl:text>
				</xsl:if>
			</div>
		</xsl:if>
	
		<!--
			show the clasifier results - 
			you can change the appearance of the results by editing
			the two templates at the bottom of this file
		-->
		<div id="results">
			<xsl:variable name="collName"><xsl:value-of select="/page/pageRequest/paramList/param[@name='c']/@value"/></xsl:variable>
			<xsl:variable name="serviceName"><xsl:value-of select="service/@name"/></xsl:variable>

			<xsl:apply-templates select="classifier">
				<xsl:with-param name="collName" select="$collName"/>
				<xsl:with-param name="serviceName" select="$serviceName"/>
			</xsl:apply-templates>
		</div>
		<script type="text/javascript">openStoredClassifiers();</script>
		<div class="clear"><xsl:text> </xsl:text></div>
	</xsl:template>

	<!--
	TEMPLATE FOR DOCUMENTS
	-->

	<xsl:template match="documentNode"><!-- priority="3"-->
		<!-- The book icon -->
		<td>
			<img>			
				<xsl:attribute name="src">
					<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'book_image')"/>
				</xsl:attribute>
			</img>
		</td>
		<!-- The document link -->
		<td>
			<a>
				<xsl:attribute name="href"><xsl:value-of select="$library_name"/>?a=d&amp;c=<xsl:value-of select="/page/pageResponse/collection/@name"/>&amp;d=<xsl:value-of select="@nodeID"/>&amp;dt=<xsl:value-of select="@docType"/>&amp;p.a=b&amp;p.s=<xsl:value-of select="/page/pageResponse/service/@name"/>&amp;ed=1</xsl:attribute>
				<xsl:value-of disable-output-escaping="yes"  select="metadataList/metadata[@name='Title']"/>
			</a>
		</td>
		<!-- The berry (optional) -->
		<td>
			<xsl:call-template name="documentBerryForClassifierOrSearchPage"/>
		</td>
	</xsl:template>


	<!--
	TEMPLATE FOR GROUPS OF DOCUMENTS
	-->
	<xsl:template match="classifierNode"><!-- priority="3"-->

		<table id="title{@nodeID}"><tbody><tr>
			<!-- Expand/collapse button -->
			<td class="headerTD">
				<img id="toggle{@nodeID}" onclick="toggleSection('{@nodeID}');" class="icon">			
					<xsl:attribute name="src">
						<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'expand_image')"/>
					</xsl:attribute>
				</img>
			</td>
			<!-- Bookshelf icon -->
			<td>
				<img>
					<xsl:attribute name="src"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'bookshelf_image')"/></xsl:attribute>
				</img>
			</td>
			<!-- Link title -->
			<td>
				<a href="javascript:toggleSection('{@nodeID}');">
					<xsl:value-of disable-output-escaping="yes"  select="metadataList/metadata[@name='Title']"/>
				</a>
			</td>
		</tr></tbody></table>
		
		<!-- Show any documents or sub-groups in this group -->
		<xsl:if test="documentNode|classifierNode">
			<div id="div{@nodeID}" class="classifierContainer">
				<table>
					<xsl:for-each select="documentNode|classifierNode">
						<tr>
							<xsl:apply-templates select="."/>
						</tr>
					</xsl:for-each>
				</table>
			</div>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>

