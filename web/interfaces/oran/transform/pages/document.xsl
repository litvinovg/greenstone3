<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util gsf">

	<!-- style includes global params interface_name, library_name -->
	<xsl:include href=".old/berrytools.xsl"/>

	<!-- use the 'main' layout -->
	<xsl:include href="layouts/main.xsl"/>

	<!-- set page title -->
	<xsl:template name="pageTitle"><gslib:documentTitle/></xsl:template>

	<!-- set page breadcrumbs -->
	<xsl:template name="breadcrumbs">
		<gslib:siteLink/><gslib:rightArrow/> 
		<gslib:collectionNameLinked/><gslib:rightArrow/> 
		<a>
			<xsl:attribute name="href">
				<xsl:value-of select="$library_name"/>?a=d&amp;c=<xsl:value-of select="$collName"/>&amp;d=<xsl:value-of select="/page/pageResponse/document/documentNode[1]/@nodeID"/>&amp;dt=<xsl:value-of select="/page/pageResponse/document/documentNode/@docType"/>&amp;p.a=b&amp;p.s=<xsl:value-of select="/page/pageResponse/service/@name"/>
			</xsl:attribute>
			<xsl:variable name="documentTitleVar">
				<gslib:documentTitle/>
			</xsl:variable>
			Document
		</a>
	</xsl:template>

	<!-- the page content -->
	<xsl:template match="/page/pageResponse/document">

		<!-- show the little berries for this document -->
		<xsl:call-template name="documentBerryForDocumentPage"/>

		<!-- the sidebar -->
		<div id="rightSidebar">

			<!-- show the berry basket if it's turned on -->
			<gslib:berryBasket/>

			<!-- the book's cover image -->
			<div id="coverImage"><gslib:coverImage/></div>
			<br/>

			<!-- the contents -->
			<ul id="tableOfContents">
				<xsl:apply-templates select="documentNode/documentNode"/>
			</ul>

		</div>

		<!-- display the document -->
		<xsl:choose>
			<xsl:when test="@external != ''">
				<xsl:call-template name="externalPage">
					<xsl:with-param name="external" select="@external"/>
				</xsl:call-template>
			</xsl:when>

			<xsl:otherwise>
				<!-- document heading -->
				<xsl:variable name="doCoverImage" select="/page/pageResponse/format/gsf:option[@name='coverImages']/@value"/>
				<xsl:variable name="doTOC" select="/page/pageResponse/format/gsf:option[@name='documentTOC']/@value"/>
				<xsl:variable name="p.a" select="/page/pageRequest/paramList/param[@name='p.a']/@value"/>
				<xsl:variable name="p.sa" select="/page/pageRequest/paramList/param[@name='p.sa']/@value"/>
				<xsl:variable name="p.s" select="/page/pageRequest/paramList/param[@name='p.s']/@value"/>
				<xsl:variable name="p.c"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='p.c']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='p.c']/@value"/></xsl:when><xsl:otherwise><xsl:value-of select="$collName"/></xsl:otherwise></xsl:choose></xsl:variable>

				<!--
				<div id="documentheading">

					<!- -<a href="{$library_name}?a={$p.a}&amp;sa={$p.sa}&amp;s={$p.s}&amp;c={$p.c}&amp;rt=rd"><xsl:call-template name="openbookimg"><xsl:with-param name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'close_book')"/></xsl:with-param></xsl:call-template></a>- ->

					<xsl:choose>
						<xsl:when test="@docType='simple'"><xsl:value-of select="metadataList/metadata[@name='Title']" disable-output-escaping="yes"/></xsl:when>
						<xsl:otherwise><xsl:value-of select="documentNode/metadataList/metadata[@name='Title']" disable-output-escaping="yes"/></xsl:otherwise>
					</xsl:choose>

				</div>
				-->

				<div id="gs-document-text" class="documenttext"> 
					<xsl:for-each select="descendant-or-self::node()/nodeContent">
						<h3><xsl:value-of disable-output-escaping="yes" select="../metadataList/metadata[@name='Title']"/></h3>
						<xsl:for-each select="node()">
							<xsl:choose>
								<xsl:when test="not(name())">
									<xsl:value-of select="." disable-output-escaping="yes"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:apply-templates select="."/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each>
					</xsl:for-each>
				</div>

				<gslib:previousNextButtons/>

			</xsl:otherwise>
		</xsl:choose>

		<div class="clear"><xsl:text> </xsl:text></div>

	</xsl:template>

	<xsl:template match="documentNode">

		<!-- check if this is the currently selected table of contents item -->
		<xsl:variable name="isCurrent" select="nodeContent"/>

		<!-- formulate the link -->
		<xsl:variable name="contentsLink">
			<xsl:value-of select='$library_name'/>?a=d&amp;c=<gslib:collectionNameShort/>&amp;d=<xsl:value-of select='@nodeID'/><xsl:if test="documentNode">.pr</xsl:if>&amp;sib=1<!--&amp;<xsl:if test="string($ec) = '1'">ec=1&amp;</xsl:if>
			p.a=<xsl:value-of select="$p.a"/>&amp;
			p.sa=<xsl:value-of select="$p.sa"/>&amp;
			p.s=<xsl:value-of select="$p.s"/>&amp;
			p.c=<xsl:value-of select="$p.c"/>-->
		</xsl:variable>

		<li>
			<!-- display this item from the table of contents -->
			<xsl:if test="$isCurrent"><xsl:attribute name="class">current</xsl:attribute></xsl:if>
			<a>
				<xsl:attribute name="href"><xsl:value-of select="translate( $contentsLink , ' ', '' )"/></xsl:attribute>
				<xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name='Title']"/>
			</a>

			<!-- display any child items -->			
			<xsl:if test="documentNode">
				<ul>
					<xsl:apply-templates select="documentNode"/>
				</ul>
			</xsl:if>

		</li>

	</xsl:template>
	
	<xsl:template name="externalPage">
		<xsl:param name="external"/>
		<xsl:variable name="go_forward_link">
			<a>
				<xsl:attribute name="href">
					<xsl:value-of select="$external"/>
				</xsl:attribute>
				<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'external.go_forward')"/>
			</a>
		</xsl:variable>
		<h2><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'external.title')"/></h2>
		<p><xsl:value-of select="util:getInterfaceTextWithDOM($interface_name, /page/@lang, 'external.text', $go_forward_link)" disable-output-escaping="yes"/></p>
	</xsl:template>

	<xsl:template match="/page"><xsl:apply-templates select="/page/pageResponse/document"/></xsl:template> <!-- this to be deleted eventually -->

</xsl:stylesheet>

