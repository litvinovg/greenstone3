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
				<xsl:value-of select="$library_name"/>/collection/<xsl:value-of select="$collName"/>/document/<xsl:value-of select="/page/pageResponse/document/documentNode[1]/@nodeID"/>
			</xsl:attribute>
			<xsl:variable name="documentTitleVar">
				<gslib:documentTitle/>
			</xsl:variable>
			<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.document')"/>
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
	
	<!-- Used to make sure that regardless what the collection designer uses for the title and content we can wrap it properly -->
	<!-- If editing, be aware that the Document Basket looks for specific classes that this template bakes in (key points marked with ***) -->
	<xsl:template name="wrapDocumentNodes">
		<xsl:for-each select="documentNode">
			<a name="{@nodeID}"><xsl:text> </xsl:text></a>
			<!-- Section header -->
			<table class="sectionHeader"><tr>
			
				<!-- Expand/collapse button -->
				<xsl:if test="not(/page/pageResponse/format[@type='display' or @type='browse' or @type='search']/gsf:option[@name='sectionExpandCollapse']/@value) or /page/pageResponse/format[@type='display' or @type='browse' or @type='search']/gsf:option[@name='sectionExpandCollapse']/@value = 'true'">
					<td class="headerTD">
						<img id="dtoggle{@nodeID}" onclick="toggleSection('{@nodeID}');" class="icon">			
							<xsl:attribute name="src">
								<xsl:choose>
									<xsl:when test="/page/pageRequest/paramList/param[@name = 'ed']/@value = '1' or util:oidIsMatchOrParent(@nodeID, /page/pageResponse/document/@selectedNode)">
										<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'collapse_image')"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'expand_image')"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:attribute>
						</img>
					</td>
				</xsl:if>
				
				<!-- Title -->
				<td id="header{@nodeID}" class="headerTD sectionTitle"><!-- *** -->
					<!-- Get the title from the title sectionTitle template -->
					<xsl:choose>
						<xsl:when test="not(/page/pageRequest/paramList/param[@name = 'dmd']) or /page/pageRequest/paramList/param[@name = 'dmd']/@value = 'false'">
							<xsl:apply-templates select="." mode="sectionTitleFormat"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:apply-templates select="." mode="sectionTitle"/>
						</xsl:otherwise>
					</xsl:choose>
				</td>
				
				<!-- "back to top" link -->
				<xsl:if test="util:hashToDepthClass(@nodeID) != 'sectionHeaderDepthTitle'">
					<td class="backToTop headerTD">
						<a href="javascript:scrollToTop();">
							<xsl:text disable-output-escaping="yes">&#9650;</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.back_to_top')"/>
						</a>
					</td>
				</xsl:if>
			</tr></table>
			
			<div id="doc{@nodeID}"><!-- *** -->
				<xsl:choose>
					<xsl:when test="/page/pageRequest/paramList/param[@name = 'ed']/@value = '1' or /page/pageResponse/document/@selectedNode = @nodeID">
						<xsl:attribute name="class">
							<xsl:text>sectionContainer hasText</xsl:text>
						</xsl:attribute>
						<xsl:attribute name="style">
							<xsl:text>display:block;</xsl:text>
						</xsl:attribute>
					</xsl:when>
					<xsl:when test="/page/pageRequest/paramList/param[@name = 'ed']/@value = '1' or util:oidIsMatchOrParent(@nodeID, /page/pageResponse/document/@selectedNode)">
						<xsl:attribute name="class">
							<xsl:text>sectionContainer noText</xsl:text>
						</xsl:attribute>
						<xsl:attribute name="style">
							<xsl:text>display:block;</xsl:text>
						</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="class">
							<xsl:text>sectionContainer noText</xsl:text>
						</xsl:attribute>
						<xsl:attribute name="style">
							<xsl:text>display:none;</xsl:text>
						</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
			
				<xsl:if test="/page/pageRequest/userInformation and (util:contains(/page/pageRequest/userInformation/@groups, 'administrator') or util:contains(/page/pageRequest/userInformation/@groups, 'all-collections-editor') or util:contains(/page/pageRequest/userInformation/@groups, $thisCollectionEditor))">
					<table id="meta{@nodeID}">
						<xsl:attribute name="style">
							<xsl:choose>
								<xsl:when test="/page/pageRequest/paramList/param[@name = 'dmd']/@value = 'true'">
									<xsl:text>display:block;</xsl:text>
								</xsl:when>
								<xsl:otherwise>
									<xsl:text>display:none;</xsl:text>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:attribute>
						<xsl:value-of select="util:clearMetadataStorage()"/>
						<xsl:for-each select="metadataList/metadata">
							<xsl:sort select="@name"/>
							<xsl:if test="util:checkMetadataNotDuplicate(@name, .)">
								<tr>
									<td class="metaTableCellName"><xsl:value-of select="@name"/></td>
									<td class="metaTableCell"><xsl:value-of select="."/></td>
								</tr>
							</xsl:if>
						</xsl:for-each>
					</table>
				</xsl:if>

				<gsf:variable name="screenImageWidth"><gsf:metadata name="ScreenWidth"/></gsf:variable>
				<gsf:variable name="screenImageHeight"><gsf:metadata name="ScreenHeight"/></gsf:variable>
				<gsf:variable name="imageWidth"><gsf:metadata name="ImageWidth"/></gsf:variable>
				<gsf:variable name="imageHeight"><gsf:metadata name="ImageHeight"/></gsf:variable>

				<xsl:choose>
					<xsl:when test="metadataList/metadata[@name = 'Screen'] and metadataList/metadata[@name = 'Source']">
						<div id="wrap{util:replace(@nodeID, '.', '_')}" class="zoomImage" style="position:relative; width: {$screenImageWidth}px; height: {$screenImageHeight}px;">
							<div id="small{util:replace(@nodeID, '.', '_')}" style="position:relative; width: {$screenImageWidth}px; height: {$screenImageHeight}px;">
								<gsf:image type="screen"/>
							</div>
							<div id="mover{util:replace(@nodeID, '.', '_')}" style="border: 1px solid green; position: absolute; top: 0; left: 0; width: 198px; height: 198px; overflow: hidden; z-index: 100; background: white; display: none;">
								<div id="overlay{util:replace(@nodeID, '.', '_')}" style="width: 200px; height: 200px; position: absolute; top: 0; left: 0; z-index: 200;">
									<xsl:text> </xsl:text>
								</div>
								<div id="large{util:replace(@nodeID, '.', '_')}" style="position: relative;">
									<gsf:image type="source"/>
								</div>
							</div>
						</div>
						<script type="text/javascript">
							<xsl:text disable-output-escaping="yes">
								$(window).load(function()
								{
									var nodeID = "</xsl:text><xsl:value-of select="@nodeID"/><xsl:text disable-output-escaping="yes">";
									var bigHeight = </xsl:text><xsl:value-of select="$imageHeight"/><xsl:text disable-output-escaping="yes">;
									var smallHeight = </xsl:text><xsl:value-of select="$screenImageHeight"/><xsl:text disable-output-escaping="yes">;
									
									nodeID = nodeID.replace(/\./g, "_");
									var multiplier = bigHeight / smallHeight;

									$("#wrap" + nodeID).anythingZoomer({
										smallArea: "#small" + nodeID,
										largeArea: "#large" + nodeID,
										zoomPort: "#overlay" + nodeID,
										mover: "#mover" + nodeID,
										expansionSize:50,  
										speedMultiplier:multiplier   
									}); 
								});
							</xsl:text>
						</script>
					</xsl:when>
					<xsl:when test="metadataList/metadata[@name = 'Screen']">
						<div id="image{@nodeID}">
							<xsl:attribute name="style">
								<xsl:choose>
									<xsl:when test="/page/pageRequest/paramList/param[@name = 'view']/@value = 'text'">
										<xsl:text>display:none;</xsl:text>
									</xsl:when>
									<xsl:otherwise>
										<xsl:text>display:block;</xsl:text>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:attribute>
							<gsf:image type="screen"/>
						</div>
					</xsl:when>
				</xsl:choose>
				<div id="text{@nodeID}" class="sectionText"><!-- *** -->
					<xsl:attribute name="style">
						<xsl:choose>
							<xsl:when test="/page/pageRequest/paramList/param[@name = 'view']/@value = 'image'">
								<xsl:text>display:none;</xsl:text>
							</xsl:when>
							<xsl:otherwise>
								<xsl:text>display:block;</xsl:text>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:attribute>
					<!-- Get the section content from the document template -->
					<xsl:apply-templates select="." mode="document"/>
				</div>
				<xsl:if test="documentNode">
					<xsl:call-template name="wrapDocumentNodes"/>
				</xsl:if>
			</div>	
		</xsl:for-each>
	</xsl:template>

	<!-- the page content -->
	<xsl:template match="/page/pageResponse/document">
		<xsl:if test="/page/pageRequest/userInformation and (util:contains(/page/pageRequest/userInformation/@groups, 'administrator') or util:contains(/page/pageRequest/userInformation/@groups, 'all-collections-editor') or util:contains(/page/pageRequest/userInformation/@groups, $thisCollectionEditor))">
			<script type="text/javascript" src="interfaces/{$interface_name}/js/documentmaker_scripts.js"><xsl:text> </xsl:text></script>
			<script type="text/javascript" src="interfaces/{$interface_name}/js/documentmaker_scripts_util.js"><xsl:text> </xsl:text></script>
			<gsf:metadata name="all"/>
		</xsl:if>

		<xsl:if test="$bookswitch = 'off'">
			<div id="bookdiv" style="visibility:hidden; height:0px; display:inline;"><xsl:text> </xsl:text></div>
		
			<script type="text/javascript" src="interfaces/{$interface_name}/js/document_scripts.js"><xsl:text> </xsl:text></script>
			
			<xsl:if test="/page/pageResponse/collection[@name = $collName]/metadataList/metadata[@name = 'tidyoption'] = 'tidy'">
				<script type="text/javascript">
					<xsl:text disable-output-escaping="yes">
						if(document.URL.indexOf("book=on") != -1)
						{
							loadBook();
						}
					</xsl:text>
				</script>
			</xsl:if>
		
			<!-- show the little berries for this document -->
			<xsl:call-template name="documentBerryForDocumentPage"/>
			
			<xsl:if test="/page/pageRequest/userInformation and (util:contains(/page/pageRequest/userInformation/@groups, 'administrator') or util:contains(/page/pageRequest/userInformation/@groups, 'all-collections-editor') or util:contains(/page/pageRequest/userInformation/@groups, $thisCollectionEditor))">
				<xsl:call-template name="editBar"/>
			</xsl:if>

			<xsl:if test="not(/page/pageResponse/format[@type='display']/gsf:option[@name='sideBar']) or /page/pageResponse/format[@type='display']/gsf:option[@name='sideBar']/@value='true'">
				<xsl:call-template name="rightSidebar"/>
			</xsl:if>
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
				<div id="bookdiv" style="display:inline;"><xsl:text> </xsl:text></div>
				<!-- *** in document-scripts.js *** -->
				<script type="text/javascript">
					<xsl:text disable-output-escaping="yes">
						if(document.URL.indexOf("book=on") != -1)
						{
							loadBook();
						}
					</xsl:text>
				</script>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="/page/pageRequest/paramList/param[@name = 'ed']/@value = '1' or not(util:contains(/page/pageResponse/document/@selectedNode, '.'))">
						<div id="gs-document">
							<xsl:call-template name="documentPre"/>
							<div id="gs-document-text" class="documenttext" collection="{/page/pageResponse/collection/@name}"><!-- *** -->
								<xsl:call-template name="wrapDocumentNodes"/>
							</div>
						</div>
					</xsl:when>
					<xsl:otherwise>
						<div id="gs-document">							
							<div id="tocLoadingImage" style="text-align:center;">
								<img src="{util:getInterfaceText($interface_name, /page/@lang, 'loading_image')}"/><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.loading')"/><xsl:text>...</xsl:text>
							</div>
						</div>
						<script type="text/javascript">
							<xsl:text disable-output-escaping="yes">
								$(window).load(function()
								{
									loadTopLevelPage(function()
									{
										focusSection("</xsl:text><xsl:value-of select="/page/pageResponse/document/@selectedNode"/><xsl:text disable-output-escaping="yes">");
									});
									console.log("HASH IS " + gs.functions.hashString("" + (new Date()).getTime()) + " ON " + (new Date()).getTime());
								});
							</xsl:text>
						</script>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
		
		<div class="clear"><xsl:text> </xsl:text></div>
	</xsl:template>
	
	<xsl:template name="editBar">
		<table style="width:100%"><tr>
			<td id="editBarLeft" style="width:70%"><xsl:text> </xsl:text></td>
			<td id="editBarRight">
				<div style="text-align:center;">
					<div style="margin:5px;" class="ui-state-default ui-corner-all">
						<a style="padding: 3px; text-decoration:none;" href="{$library_name}?a=g&amp;sa=documentbasket&amp;c=&amp;s=DisplayDocumentList&amp;rt=r&amp;p.c={/page/pageResponse/collection/@name}&amp;docToEdit={/page/pageResponse/document/documentNode/@nodeID}"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.edit_structure')"/></a>
					</div>
					<div style="margin:5px;" class="ui-state-default ui-corner-all">
						<a id="editContentButton" style="padding: 3px; text-decoration:none;" href="javascript:readyPageForEditing();"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.edit_content')"/></a>
					</div>
				</div>
			</td>
		</tr></table>
		<xsl:call-template name="document-editor-language-fragments"/>
	</xsl:template>
	
	<xsl:template name="rightSidebar">
		<table id="rightSidebar">
			<tr><td>
				<xsl:call-template name="viewOptions"/>
			</td></tr>
			<tr><td>
				<div id="contentsArea">	
					<!-- show the berry basket if it's turned on -->
					<gslib:berryBasket/>

					<!-- the book's cover image -->
					<div id="coverImage">
						<xsl:attribute name="class">
							<xsl:choose>
								<xsl:when test="not(/page/pageResponse/format[@type='display']/gsf:option[@name='coverImage']) or /page/pageResponse/format[@type='display']/gsf:option[@name='coverImage']/@value='true'">visible</xsl:when>
								<xsl:otherwise>hidden</xsl:otherwise>    
							</xsl:choose>
						</xsl:attribute>
						<gslib:coverImage/><xsl:text> </xsl:text>
					</div>

					<!-- the contents (if enabled) -->
					<xsl:choose>
						<xsl:when test="/page/pageResponse/document/@docType = 'paged'">
							<!-- Table of contents will be dynamically retrieved when viewing a paged document -->
							<script type="text/javascript">
								<xsl:text disable-output-escaping="yes">
									$(window).load(function()
									{
										retrieveTableOfContentsAndTitles();
									});
								</xsl:text>
							</script>
							<div id="tocLoadingImage" style="text-align:center;">
								<img src="{util:getInterfaceText($interface_name, /page/@lang, 'loading_image')}"/><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.loading')"/><xsl:text>...</xsl:text>
							</div>
						</xsl:when>
						<xsl:when test="not(/page/pageRequest/paramList/param[@name = 'ed']/@value = '1')">
							<div id="tableOfContents">
								<div id="tocLoadingImage" style="text-align:center;">
									<img src="{util:getInterfaceText($interface_name, /page/@lang, 'loading_image')}"/><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.loading')"/><xsl:text>...</xsl:text>
								</div>
							</div>
							<script type="text/javascript">
								<xsl:text disable-output-escaping="yes">
									$(window).load(function()
									{
										retrieveFullTableOfContents();
									});
								</xsl:text>
							</script>
						</xsl:when>
						<xsl:otherwise>
							<div id="tableOfContents">
								<xsl:attribute name="class">
									<xsl:choose>
										<xsl:when test="count(//documentNode) > 1 and not(/page/pageResponse/format[@type='display']/gsf:option[@name='TOC']) or /page/pageResponse/format[@type='display']/gsf:option[@name='TOC']/@value='true'">visible</xsl:when>
										<xsl:otherwise>hidden</xsl:otherwise>
									</xsl:choose>
								</xsl:attribute>
								<xsl:apply-templates select="documentNode" mode="TOC"/>
								<xsl:if test="@docType = 'paged'">
									<table style="width:100%;"><tbody><tr>
										<td><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.filter_pages')"/><xsl:text>: </xsl:text><input id="filterText" type="text" size="27"/></td>
									</tr></tbody></table>
								</xsl:if>
							</div>
						</xsl:otherwise>
					</xsl:choose>
				</div>
			</td></tr>
		</table>
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
	
	<xsl:template match="documentNode" mode="sectionTitleFormat">
		<p>
			<xsl:attribute name="class"><xsl:value-of select="util:hashToDepthClass(@nodeID)"/> sectionHeader</xsl:attribute>
			
			<xsl:if test="util:hashToSectionId(@nodeID)">
				<span class="sectionNumberSpan">
					<xsl:value-of select="util:hashToSectionId(@nodeID)"/>
					<xsl:text> </xsl:text>
				</span>
			</xsl:if>
			<!-- Display the title for the section regardless of whether automatic section numbering is turned on -->
			<span><xsl:apply-templates select="." mode="sectionTitle"/></span>
		</p>
	</xsl:template>
	
	<!-- The default template for displaying section titles -->
	<xsl:template match="documentNode" mode="sectionTitle">
		<xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name = 'Title']"/>
	</xsl:template>
	
	<!-- The default template for displaying the document content -->
	<xsl:template match="documentNode" mode="document">
		<!-- Section text -->
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
		</xsl:for-each><xsl:text> </xsl:text>
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
								<img id="ttoggle{@nodeID}" onclick="toggleSection('{@nodeID}');" class="icon">
									<xsl:attribute name="src">
										<xsl:choose>
											<xsl:when test="/page/pageRequest/paramList/param[@name = 'ed']/@value = '1' or /page/pageResponse/document/@selectedNode = @nodeID">
												<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'collapse_image')"/>
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'expand_image')"/>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:attribute>
								</img>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="class">emptyIcon</xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>
					</td>
				</xsl:if>
				
				<!-- The chapter/page icon -->
				<td>
					<img>
						<xsl:if test="nodeContent and not(documentNode)">
							<xsl:attribute name="class">leafNode</xsl:attribute>
						</xsl:if>
						
						<xsl:attribute name="src">
							<xsl:choose>
								<xsl:when test="nodeContent and not(documentNode)">
									<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'page_image')"/> 
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'chapter_image')"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:attribute>
					</img>
				</td>
				
				<!-- The section name, links to the section in the document -->
				<td>				
					<a id="toclink{@nodeID}">
						<xsl:choose>
							<xsl:when test="/page/pageResponse/document/@docType = 'paged'">
								<xsl:attribute name="href"><xsl:value-of select="$library_name"/>?a=d&amp;c=<xsl:value-of select="/page/pageResponse/collection/@name"/>&amp;d=<xsl:value-of select="@nodeID"/>&amp;dt=<xsl:value-of select="@docType"/>&amp;p.a=b&amp;p.s=<xsl:value-of select="/page/pageResponse/service/@name"/></xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="href">javascript:focusSection('<xsl:value-of select="@nodeID"/>');</xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:if test="util:hashToSectionId(@nodeID)">
							<xsl:value-of select="util:hashToSectionId(@nodeID)"/>
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:apply-templates select="." mode="sectionTitle"/>
					</a>
				</td>
			</tr></table>
		
			<!-- display any child items -->		
			<xsl:if test="documentNode">
				<li id="toc{@nodeID}">
					<xsl:attribute name="style">
						<xsl:choose>
							<xsl:when test="/page/pageRequest/paramList/param[@name = 'ed']/@value = '1' or /page/pageResponse/document/@selectedNode = @nodeID">
								<xsl:text>display:block;</xsl:text>
							</xsl:when>
							<xsl:otherwise>
								<xsl:text>display:none;</xsl:text>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:attribute>
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
		<table class="viewOptions ui-state-default ui-corner-all"><tr>
		
			<!-- Paged-image options -->
			<xsl:if test="count(//documentNode/metadataList/metadata[@name = 'Screen']) > 0 or /page/pageRequest/paramList/param[@name = 'dt']/@value = 'paged'">
				<td>
					<select id="viewSelection" onchange="changeView();">
						<xsl:choose>
							<xsl:when test="/page/pageRequest/paramList/param[@name = 'view']/@value = 'image'">
								<option>Default view</option>
								<option selected="true">Image view</option>
								<option>Text view</option>
							</xsl:when>
							<xsl:when test="/page/pageRequest/paramList/param[@name = 'view']/@value = 'text'">
								<option>Default view</option>
								<option>Image view</option>
								<option selected="true">Text view</option>
							</xsl:when>
							<xsl:otherwise>
								<option selected="true">Default view</option>
								<option>Image view</option>
								<option>Text view</option>
							</xsl:otherwise>
						</xsl:choose>
					</select>
				</td>
			</xsl:if>
		
			<!-- Realistic books link -->
			<xsl:if test="/page/pageResponse/collection[@name = $collName]/metadataList/metadata[@name = 'tidyoption'] = 'tidy'">
				<td>
					<img>
						<xsl:attribute name="src"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'realistic_books_image')"/></xsl:attribute>
					</img>
					<input id="rbOption" type="checkbox" onclick="bookInit();" class="optionCheckBox"/>
				</td>
			</xsl:if>
			
			<!-- Highlight on/off button -->
			<xsl:if test="/page/pageRequest/paramList/param[@name = 'p.a']/@value = 'q' or /page/pageRequest/paramList/param[@name = 's1.query']">
				<td>
					<img>
						<xsl:attribute name="src"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'highlight_image')"/></xsl:attribute>
					</img>
					<input id="highlightOption" type="checkbox" class="optionCheckBox">
						<xsl:choose>
							<xsl:when test="/page/pageRequest/paramList/param[@name = 'hl']/@value = 'on'">
								<xsl:attribute name="onclick">
									<xsl:text>removeHighlight();</xsl:text>
								</xsl:attribute>
								<xsl:attribute name="checked">true</xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="onclick">
									<xsl:text>addHighlight();</xsl:text>
								</xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>
					</input>
				</td>
			</xsl:if>
			<td style="vertical-align:top; text-align:right;">
				<xsl:if test="not(/page/pageResponse/format[@type='display']/gsf:option[@name='TOC']) or /page/pageResponse/format[@type='display']/gsf:option[@name='TOC']/@value='true'">
					<span class="tableOfContentsTitle"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.table_of_contents')"/></span>

					<a id="sidebarMinimizeButton" href="javascript:minimizeSidebar();" style="float: right; font-size:0.6em;">
						<img class="icon" style="padding-top:3px;">
							<xsl:attribute name="src">
								<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'collapse_image')"/>
							</xsl:attribute>
						</img>
					</a>
					<a id="sidebarMaximizeButton" href="javascript:maximizeSidebar();" style="float: right; font-size:0.6em; display:none;">
						<img class="icon" style="padding-top:3px;">
							<xsl:attribute name="src">
								<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'expand_image')"/>
							</xsl:attribute>
						</img>
					</a>
				</xsl:if>
			</td>
		</tr>
		<xsl:if test="count(//documentNode/metadataList/metadata[@name = 'Screen']) > 0 and count(//documentNode/metadataList/metadata[@name = 'Source']) > 0">
			<tr>
				<td style="width:40%;">
					<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.zoom')"/><input id="zoomToggle" type="checkbox"/>
					<script type="text/javascript">
						<xsl:text disable-output-escaping="yes">
							$("#zoomToggle").change(function()
							{
								_imageZoomEnabled = !_imageZoomEnabled;
							});
						</xsl:text>
					</script>
				</td>
				<td style="width:60%;">
					<div>
						<div style="float:left; width:30%;"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.zoom_size')"/><xsl:text>:</xsl:text></div>
						<div id="zoomSlider" style="float:right; width:65%; height:5px; margin-top:6px;"><xsl:text> </xsl:text></div>
						<script type="text/javascript">
							<xsl:text disable-output-escaping="yes">
								$("#zoomSlider").slider(
								{
									change: function(event, ui)
									{
										var sliderValue = ui.value;
										var divs = document.getElementsByTagName("DIV");
										for(var i = 0; i &lt; divs.length; i++)
										{
											if(divs[i].getAttribute("id") &amp;&amp; divs[i].getAttribute("id").search(/^mover.*/) != -1)
											{
												divs[i].style.height = 200 + (2 * sliderValue) + "px";
												divs[i].style.width = 200 + (2 * sliderValue) + "px";
											}
										}
									}
								});
							</xsl:text>
						</script>
						<style>
							.ui-slider .ui-slider-handle{height:0.8em; width:1.0em;}
						</style>
						<div style="float:clear;"><xsl:text> </xsl:text></div>
					</div>
				</td>
			</tr>
		</xsl:if>
		</table>	
	</xsl:template>
	
	<xsl:template name="documentPre">
		<xsl:if test="/page/pageResponse/format[@type='display' or @type='browse' or @type='search']/gsf:option[@name='mapEnabled']/@value = 'true'">
			<xsl:call-template name="mapFeatures"/>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="mapFeatures">
		<div id="map_canvas"><xsl:text> </xsl:text></div>

		<xsl:if test="metadataList/metadata[@name = 'Latitude'] and metadataList/metadata[@name = 'Longitude']">
			<div style="background:#BBFFBB; padding: 5px; margin:0px auto; width:890px;">
				<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.maps.nearby_docs')"/>
				<img id="nearbyDocumentsToggle" src="interfaces/oran/images/expand.png">
					<xsl:attribute name="onclick">
						<xsl:text>performDistanceSearch('</xsl:text>
						<xsl:value-of select="@nodeID"/>
						<xsl:text>', '</xsl:text>
						<gsf:metadata name="Latitude"/>
						<xsl:text>', '</xsl:text>
						<gsf:metadata name="Longitude"/>
						<xsl:text>', 2);</xsl:text>
					</xsl:attribute>
				</img>
				<div id="nearbyDocuments"><xsl:text> </xsl:text></div>
			</div>
		</xsl:if>
		
		<div id="jsonNodes" style="display:none;">
			<xsl:text>[</xsl:text>
			<xsl:for-each select="//documentNode">
				<xsl:if test="metadataList/metadata[@name = 'Latitude'] and metadataList/metadata[@name = 'Longitude']">
					<xsl:text>{</xsl:text>
					<xsl:text disable-output-escaping="yes">"nodeID":"</xsl:text><xsl:value-of select="@nodeID"/><xsl:text disable-output-escaping="yes">",</xsl:text>
					<xsl:text disable-output-escaping="yes">"title":"</xsl:text><xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name = 'Title']"/><xsl:text disable-output-escaping="yes">",</xsl:text>
					<xsl:text disable-output-escaping="yes">"lat":</xsl:text><xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name = 'Latitude']"/><xsl:text>,</xsl:text>
					<xsl:text disable-output-escaping="yes">"lng":</xsl:text><xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name = 'Longitude']"/>
					<xsl:text>}</xsl:text>
					<xsl:if test="not(position() = count(//documentNode))">
						<xsl:text>,</xsl:text>
					</xsl:if>
				</xsl:if>
			</xsl:for-each>
			<xsl:text>]</xsl:text>
		</div>
	</xsl:template>
</xsl:stylesheet>

