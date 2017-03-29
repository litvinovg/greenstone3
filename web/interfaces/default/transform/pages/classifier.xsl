<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util gsf">
	
	<!-- use the 'main' layout -->
	<xsl:import href="layouts/main.xsl"/>
	<xsl:import href="classifiertools.xsl"/>

	<!-- set page title -->
	<xsl:template name="pageTitle"><gslib:serviceName/></xsl:template>

	<!-- set page breadcrumbs -->
	<xsl:template name="breadcrumbs"><gslib:siteLink/><gslib:rightArrow/><gslib:collectionNameLinked/><gslib:rightArrow/></xsl:template>

	<!-- optional cgi-params for links to document pages -->
	<xsl:variable name="opt-doc-link-args"></xsl:variable>
	<!-- the page content -->
	<xsl:template match="/page/pageResponse">
		<xsl:call-template name="classifierPre"/>
		
		<script type="text/javascript" src="interfaces/{$interface_name}/js/classifier_scripts.js"><xsl:text> </xsl:text></script>
		<script type="text/javascript">$(window).load(openStoredClassifiers);</script>
		
		<!-- this right sidebar -->
		<xsl:if test="$berryBasketOn or ($documentBasketOn and (util:contains(/page/pageRequest/userInformation/@groups, 'administrator') or util:contains(/page/pageRequest/userInformation/@groups, 'all-collections-editor') or util:contains(/page/pageRequest/userInformation/@groups, $thisCollectionEditor)))">
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

			<xsl:call-template name="classifierResultsPre"/>
			
			<xsl:apply-templates select="classifier">
				<xsl:with-param name="collName" select="$collName"/>
				<xsl:with-param name="serviceName" select="$serviceName"/>
			</xsl:apply-templates>
		</div>

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
				<xsl:choose>
					<xsl:when test="@docType = 'paged'">
						<xsl:attribute name="href"><xsl:value-of select="$library_name"/>?a=d&amp;c=<xsl:value-of select="/page/pageResponse/collection/@name"/>&amp;d=<xsl:value-of select="@nodeID"/>&amp;dt=<xsl:value-of select="@docType"/>&amp;p.a=b&amp;p.s=<xsl:value-of select="/page/pageResponse/service/@name"/></xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="href"><xsl:value-of select="$library_name"/>?a=d&amp;c=<xsl:value-of select="/page/pageResponse/collection/@name"/>&amp;d=<xsl:value-of select="@nodeID"/>&amp;dt=<xsl:value-of select="@docType"/>&amp;p.a=b&amp;p.s=<xsl:value-of select="/page/pageResponse/service/@name"/>&amp;ed=1</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
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
	<xsl:template match="classifierNode[@classifierStyle = 'HList']" >
	  <gsf:link type="classifier">
	    <xsl:value-of disable-output-escaping="yes"  select="metadataList/metadata[@name='Title']"/>
	  </gsf:link>
	</xsl:template>

	<xsl:template match="classifierNode"><!-- priority="3"-->

		<!--<table id="title{@nodeID}"><tbody><tr>-->
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
		<!--</tr></tbody></table>-->
	
		<!-- Show any documents or sub-groups in this group -->
	<!--	<xsl:if test="documentNode|classifierNode">
			<div id="div{@nodeID}" class="classifierContainer">
				<table>
					<xsl:for-each select="documentNode|classifierNode">
						<tr>
							<xsl:apply-templates select="."/>
						</tr>
					</xsl:for-each>
				</table>
			</div>
		</xsl:if>-->
	</xsl:template>
	
	<xsl:template name="classifierPre">
		<xsl:if test="/page/pageResponse/format[@type='display' or @type='browse' or @type='search']/gsf:option[@name='mapEnabled']/@value = 'true'">
			<xsl:call-template name="mapFeaturesJSONNodes"/>
		</xsl:if>
		
		<xsl:if test="/page/pageResponse/format/gsf:option[@name='panoramaViewerEnabled']/@value = 'true'">
			<xsl:call-template name="panoramaViewerFeaturesJSONNodes"/>
		</xsl:if>
		
	</xsl:template>
	
	<xsl:template name="classifierResultsPre">
		<xsl:if test="/page/pageResponse/format[@type='display' or @type='browse' or @type='search']/gsf:option[@name='mapEnabled']/@value = 'true'">
		  <xsl:call-template name="mapFeaturesMap"/>
		</xsl:if>
		<xsl:if test="/page/pageResponse/format/gsf:option[@name='panoramaViewerEnabled']/@value = 'true'">
		  <xsl:call-template name="panoramaViewerFeatures"/>
		</xsl:if>
	</xsl:template>
	
	


	<xsl:template match="/page/xsltparams">
	  <!-- suppress xsltparam block in page -->
	</xsl:template>
</xsl:stylesheet>

