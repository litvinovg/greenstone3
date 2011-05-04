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
	
	<!-- style includes global params interface_name, library_name -->
	<xsl:include href=".old/berrytools.xsl"/>
	<xsl:include href="document-scripts.xsl"/>
	
	<xsl:variable name="bookswitch">
		<xsl:choose>
			<xsl:when test="/page/pageRequest/paramList/param[@name='book']/@value">
				<xsl:value-of select="/page/pageRequest/paramList/param[@name='book']/@value"/>
			</xsl:when>
			<xsl:otherwise>off</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>

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
	
	<xsl:template match="/">
		<xsl:choose>
			<!-- if this is the realistic books version of the page -->
			<xsl:when test="$bookswitch = 'flashxml'">
				<html>
					<body>
						<xsl:apply-templates select="/page/pageResponse/document"/>
					</body>
				</html>
			</xsl:when>
			<!-- if this is the regular version of the page -->
			<xsl:otherwise>
				<xsl:apply-imports/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- the page content -->
	<xsl:template match="/page/pageResponse/document">
		<xsl:if test="$bookswitch = 'off'">
			<!-- Add the Javascript that adds and removes highlighting ( *** in document-scripts.xsl *** ) -->
			<xsl:call-template name="highlightingScript"/>
			
			<!-- Add the Javascript that expands and collapses sections ( *** in document-scripts.xsl *** ) -->
			<xsl:call-template name="expansionScript"/>
		
			<!-- show the little berries for this document -->
			<xsl:call-template name="documentBerryForDocumentPage"/>

			<table id="rightSidebar"> 
				<tr><td>
					<xsl:call-template name="viewOptions"/>
				</td></tr>
				<tr><td>
					<!-- the sidebar -->
					<div id="contentsArea">				
						<!-- show the berry basket if it's turned on -->
						<gslib:berryBasket/>

						<!-- the book's cover image -->
						<div id="coverImage"><gslib:coverImage/></div>

						<!-- the contents -->
						<div id="tableOfContents">
							<xsl:apply-templates select="documentNode" mode="TOC"/>
						</div>
					</div>
				</td></tr>
			</table>
		</xsl:if>
		
		<!-- display the document -->
		<xsl:choose>
			<xsl:when test="@external != ''">
				<xsl:call-template name="externalPage">
					<xsl:with-param name="external" select="@external"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$bookswitch = 'flashxml'">
				<xsl:apply-templates mode="flashxml"/>
			</xsl:when>
			<xsl:when test="$bookswitch = 'on'">
				<!-- *** in document-scripts.xsl *** -->
				<xsl:call-template name="realisticBooksScript"/>
			</xsl:when>
			<xsl:otherwise>
				<div id="gs-document-text" class="documenttext"> 
					<xsl:apply-templates select="documentNode" mode="document"/>
				</div>
			</xsl:otherwise>
		</xsl:choose>

		<div class="clear"><xsl:text> </xsl:text></div>
	</xsl:template>
	
	<!-- Highlight annotations if requested -->
	<xsl:template match="annotation">
		<xsl:choose>
			<xsl:when test="/page/pageRequest/paramList/param[@name='hl' and @value='on']">
				<span class="termHighlight"><xsl:value-of select="."/></span>
			</xsl:when>
			<xsl:otherwise>
				<span class="noTermHighlight"><xsl:value-of select="."/></span>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- This template is used to display the document content -->
	<xsl:template match="documentNode" mode="document">
		<a name="{@nodeID}"><xsl:text> </xsl:text></a>
		<!-- Section header -->
		<table><tr>
			<!-- Expand/collapse button -->
			<td class="headerTD">
				<div id="dtoggle{@nodeID}" onclick="toggleSection('{@nodeID}');">			
					<xsl:attribute name="class">
					<xsl:choose>
						<xsl:when test="nodeContent and not(documentNode)">icon leafNode toggleImageCollapse</xsl:when>
						<xsl:otherwise>icon toggleImageCollapse</xsl:otherwise>
					</xsl:choose>
					</xsl:attribute>
				</div>
			</td>
			
			<!-- Automatic section number -->
			<td class="headerTD">
				<p>
					<xsl:attribute name="class"><xsl:value-of select="util:hashToDepthClass(@nodeID)"/> sectionHeader</xsl:attribute>
					
					<xsl:if test="util:hashToSectionId(@nodeID)">
						<span class="sectionNumberSpan">
							<xsl:value-of select="util:hashToSectionId(@nodeID)"/>
							<xsl:text> </xsl:text>
						</span>
					</xsl:if>
					<xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name = 'Title']"/> 
				</p>
			</td>
			
			<!-- "back to top" link -->
			<xsl:if test="util:hashToDepthClass(@nodeID) != 'sectionHeaderDepthTitle'">
				<td class="backToTop headerTD">
					<a href="#top">
						<xsl:text disable-output-escaping="yes">&#9650;back to top</xsl:text>
					</a>
				</td>
			</xsl:if>
		</tr></table>
		
		<!-- Section text -->
		<div id="doc{@nodeID}" class="sectionContainer" style="display:block;">		
			<xsl:for-each select="nodeContent">
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
			<xsl:if test="documentNode">
				<xsl:apply-templates select="documentNode" mode="document"/>
			</xsl:if>
		</div>	

	</xsl:template>

	<!-- This template is used to display the table of contents -->
	<xsl:template match="documentNode" mode="TOC">

		<!-- check if this is the currently selected table of contents item -->
		<xsl:variable name="isCurrent" select="nodeContent"/>

		<!-- formulate the link -->
		<xsl:variable name="contentsLink">
			<xsl:value-of select='$library_name'/>?a=d&amp;c=<gslib:collectionNameShort/>&amp;d=<xsl:value-of select='@nodeID'/><xsl:if test="documentNode">.pr</xsl:if>&amp;sib=1
		</xsl:variable>

		<ul>
			<table><tr>
				<!-- The expand/collapse button (not displayed for the top level node) -->
				<xsl:if test="util:hashToDepthClass(@nodeID) != 'sectionHeaderDepthTitle'">
					<td>
						<xsl:choose>
							<xsl:when test="not(nodeContent and not(documentNode))">
								<div id="ttoggle{@nodeID}" onclick="toggleSection('{@nodeID}');" class="icon toggleImageCollapse"/>
							</xsl:when>
							<xsl:otherwise>
								<div class="icon"/>
							</xsl:otherwise>
						</xsl:choose>
					</td>
				</xsl:if>
				
				<!-- The chapter/page icon -->
				<td>
					<div>
						<xsl:attribute name="class">
							<xsl:choose>
								<xsl:when test="nodeContent and not(documentNode)">
									icon leafNode toggleImagePage
								</xsl:when>
								<xsl:otherwise>
									icon toggleImageChapter
								</xsl:otherwise>
							</xsl:choose>
						</xsl:attribute>
					</div>
				</td>
				
				<!-- The section name, links to the section in the document -->
				<td>				
					<!-- display this item from the table of contents -->
					<xsl:if test="$isCurrent"><xsl:attribute name="class">current</xsl:attribute></xsl:if>
					<a>
						<xsl:attribute name="href">#<xsl:value-of select="@nodeID"/></xsl:attribute>
						<xsl:if test="util:hashToSectionId(@nodeID)">
							<xsl:value-of select="util:hashToSectionId(@nodeID)"/>
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name = 'Title']"/>
					</a>
				</td>
			</tr></table>
		
			<!-- display any child items -->		
			<xsl:if test="documentNode">
				<li id="toc{@nodeID}" style="display:block;">
					<xsl:apply-templates select="documentNode" mode="TOC"/>
				</li>
			</xsl:if>
			
		</ul>
	</xsl:template>
	
	<!-- Used to produce a version of the page in a format that can be read by the realistic books plugin -->
	<xsl:template match="documentNode" mode="flashxml">
		<xsl:text disable-output-escaping="yes">
			&lt;Section&gt;
			&lt;Description&gt;
			&lt;Metadata name="Title"&gt;
		</xsl:text>
		<xsl:value-of select="normalize-space(metadataList/metadata[@name = 'Title'])"/>
		<xsl:text disable-output-escaping="yes">
			&lt;/Metadata&gt;
			&lt;/Description&gt;
		</xsl:text>
		
		<xsl:value-of select="normalize-space(nodeContent)" disable-output-escaping="yes"/>
				
		<xsl:if test="documentNode">
			<xsl:apply-templates select="documentNode" mode="flashxml"/>
		</xsl:if>
				
		<xsl:text disable-output-escaping="yes">
			&lt;/Section&gt;
		</xsl:text>
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
	
	<xsl:template name="viewOptions">
		<table class="viewOptions"><tr>
			<!-- Highlight on/off button -->
			<xsl:if test="/page/pageRequest/paramList/param[@name = 'p.a']/@value = 'q'">
				<td>
					<a id="highlightOption">
						<xsl:choose>
							<xsl:when test="/page/pageRequest/paramList/param[@name = 'hl']/@value = 'on'">
								<xsl:attribute name="href">
									<xsl:text>javascript:removeHighlight();</xsl:text>
								</xsl:attribute>
								<xsl:text>No Highlighting</xsl:text>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="href">
									<xsl:text>javascript:addHighlight();</xsl:text>
								</xsl:attribute>
								<xsl:text>Highlighting</xsl:text>
							</xsl:otherwise>
						</xsl:choose>
					</a>
				</td>
			</xsl:if>
			
			<!-- Realistic books link -->
			<xsl:if test="/page/pageResponse/collection[@name = $collName]/metadataList/metadata[@name = 'tidyoption'] = 'tidy'">
				<td>
					<a title="Realistic book view" href="{$library_name}?a=d&amp;c={$collName}&amp;d={/page/pageResponse/document/documentNode[1]/@nodeID}&amp;dt={/page/pageResponse/document/documentNode/@docType}&amp;p.a=b&amp;p.s={/page/pageResponse/service/@name}&amp;book=on&amp;ed=1">
						<img src="interfaces/oran/images/rbook.png"/>
					</a>
				</td>
			</xsl:if>
		</tr></table>	
	</xsl:template>
</xsl:stylesheet>

