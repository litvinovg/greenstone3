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
	<xsl:import href="layouts/toc.xsl"/>
	
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
				<xsl:call-template name="mainTemplate"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="sectionHeader">
		<xsl:call-template name="sectionTitle"/>
	</xsl:template>
	
	<!-- Used to make sure that regardless what the collection designer uses for the title and content we can wrap it properly -->
	<!-- If editing, be aware that the Document Basket looks for specific classes that this template bakes in (key points marked with ***) -->
	<xsl:template name="wrapDocumentNodes">
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
				<p>
					<xsl:attribute name="class"><xsl:value-of select="util:hashToDepthClass(@nodeID)"/> sectionHeader</xsl:attribute>

					<xsl:if test="util:hashToSectionId(@nodeID)">
						<span class="sectionNumberSpan">
							<xsl:value-of select="util:hashToSectionId(@nodeID)"/>
							<xsl:text> </xsl:text>
						</span>
					</xsl:if>
					<!-- Display the title for the section regardless of whether automatic section numbering is turned on -->
					<span><xsl:call-template name="sectionHeader"/></span>
				</p>
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
								<td class="metaTableCell"><xsl:value-of disable-output-escaping="yes" select="."/></td>
							</tr>
						</xsl:if>
					</xsl:for-each>
				</table>
			</xsl:if>
			
			<xsl:call-template name="sectionImage"/>
			
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
				<xsl:call-template name="documentNodeText"/>
			</div>
			<xsl:if test="documentNode">
				<xsl:for-each select="documentNode">
					<xsl:call-template name="wrapDocumentNodes"/>
				</xsl:for-each>
			</xsl:if>
		</div>	
	</xsl:template>

	<!-- the page content -->
	<xsl:template match="/page/pageResponse/document">
		<xsl:if test="/page/pageRequest/userInformation and (util:contains(/page/pageRequest/userInformation/@groups, 'administrator') or util:contains(/page/pageRequest/userInformation/@groups, 'all-collections-editor') or util:contains(/page/pageRequest/userInformation/@groups, $thisCollectionEditor))">
			<script type="text/javascript" src="interfaces/{$interface_name}/js/documentmaker_scripts.js"><xsl:text> </xsl:text></script>
			<script type="text/javascript" src="interfaces/{$interface_name}/js/documentmaker_scripts_util.js"><xsl:text> </xsl:text></script>
			<script type="text/javascript">
				<xsl:text disable-output-escaping="yes">
					$(window).load(function()
					{
						if(gs.cgiParams.docEdit == "1")
						{
							readyPageForEditing();
						}
					});
				</xsl:text>
			</script>
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
				<xsl:call-template name="documentNodeFlashXML"/>
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
			<xsl:when test="@docType='simple'">
				<xsl:call-template name="documentHeading"/><br/>
				<xsl:call-template name="documentContent"/>
			</xsl:when>			
			<xsl:otherwise> <!-- display the standard greenstone document -->
				<xsl:call-template name="documentContent"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="documentContent">
		<xsl:choose>
			<xsl:when test="@docType='simple'">
				<xsl:call-template name="documentNodeText"/>
			</xsl:when>
			<xsl:otherwise> 
				<xsl:call-template name="wrappedDocument"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="wrappedDocument">
		<xsl:choose>
			<!-- NOTE: alb = ajax load bypass -->
			<xsl:when test="/page/pageResponse/document/@docType = 'hierarchy' and (/page/pageRequest/paramList/param[@name = 'alb']/@value = '1' or (string-length(/page/pageRequest/paramList/param[@name = 'd']/@value) > 0 and (/page/pageRequest/paramList/param[@name = 'ed']/@value = '1' or not(util:contains(/page/pageResponse/document/@selectedNode, '.')))))">
				<div id="gs-document">
					<xsl:call-template name="documentPre"/>
					<div id="gs-document-text" class="documenttext" collection="{/page/pageResponse/collection/@name}"><!-- *** -->
						<xsl:for-each select="documentNode">
							<xsl:call-template name="wrapDocumentNodes"/>
						</xsl:for-each>
					</div>
				</div>
			</xsl:when>
			<xsl:when test="/page/pageResponse/document/@docType = 'paged'">
				<div id="gs-document">							
					<div id="tocLoadingImage" style="text-align:center;">
						<img src="{util:getInterfaceText($interface_name, /page/@lang, 'loading_image')}"/><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.loading')"/><xsl:text>...</xsl:text>
					</div>
				</div>
				<script type="text/javascript">
					<xsl:text disable-output-escaping="yes">
						$(window).load(function()
						{
							var url = gs.xsltParams.library_name + "?a=d&amp;c=" + gs.cgiParams.c + "&amp;excerptid=gs-document&amp;dt=hierarchy&amp;d=" + gs.cgiParams.d.replace(/([^.]*)\..*/, "$1");
							loadTopLevelPage(null, url);
						});
					</xsl:text>
				</script>
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
								//Don't focus the section until the table of contents is loaded
								var tocCheck = function()
								{
									if(gs.variables.tocLoaded)
									{
										focusSection("</xsl:text><xsl:value-of select="/page/pageResponse/document/@selectedNode"/><xsl:text disable-output-escaping="yes">");
									}
									else
									{
										setTimeout(tocCheck, 500);
									}
								}
								tocCheck();
							});
						});
					</xsl:text>
				</script>
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
						<a id="editContentButton" style="padding: 3px; text-decoration:none;">
							<xsl:attribute name="href">
								<xsl:value-of select="$library_name"/>
								<xsl:text>/collection/</xsl:text>
								<xsl:value-of select="$collName"/>
								<xsl:text>/document/</xsl:text>
								<xsl:value-of select="/page/pageResponse/document/documentNode/@nodeID"/>
								<xsl:if test="not(/page/pageRequest/paramList/param[@name = 'docEdit']/@value = '1')">
									<xsl:text>?ed=1&amp;docEdit=1</xsl:text>
								</xsl:if>
							</xsl:attribute>
							<xsl:choose>
								<xsl:when test="/page/pageRequest/paramList/param[@name = 'docEdit']/@value = '1'">
									<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.hide_editor')"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.edit_content')"/>
								</xsl:otherwise>
							</xsl:choose>
						</a>
					</div>
				</div>
			</td>
		</tr></table>
		<gslib:langfrag name="dse"/>
	</xsl:template>
	
	<!-- Highlight annotations if requested -->
	<xsl:template name="displayAnnotation">
		<xsl:choose>
			<xsl:when test="/page/pageRequest/paramList/param[@name = 'hl']/@value = 'on'">
				<span class="termHighlight"><xsl:value-of select="."/></span>
			</xsl:when>
			<xsl:otherwise>
				<span class="noTermHighlight"><xsl:value-of select="."/></span>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
		
	<!-- The default template for displaying section titles -->
	<xsl:template name="sectionTitle">
		<xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name = 'Title']"/>
	</xsl:template>
	
	<xsl:template name="sectionImage">
		<gsf:variable name="screenImageWidth"><gsf:metadata name="ScreenWidth"/></gsf:variable>
		<gsf:variable name="screenImageHeight"><gsf:metadata name="ScreenHeight"/></gsf:variable>
		<gsf:variable name="imageWidth"><gsf:metadata name="ImageWidth"/></gsf:variable>
		<gsf:variable name="imageHeight"><gsf:metadata name="ImageHeight"/></gsf:variable>

		<xsl:choose>
			<xsl:when test="metadataList/metadata[@name = 'Screen'] and metadataList/metadata[@name = 'Source'] and ($imageWidth div $screenImageWidth > 1.2)">
				<div id="image{@nodeID}">
					<div id="wrap{util:replace(@nodeID, '.', '_')}" class="zoomImage" style="position:relative; width: {$screenImageWidth}px; height: {$screenImageHeight}px;">
						<div id="small{util:replace(@nodeID, '.', '_')}" style="position:relative; width: {$screenImageWidth}px; height: {$screenImageHeight}px;">
							<gsf:image type="screen"/>
						</div>
						<div id="mover{util:replace(@nodeID, '.', '_')}" style="border: 1px solid green; position: absolute; top: 0; left: 0; width: 198px; height: 198px; overflow: hidden; z-index: 100; background: white; display: none;">
							<div id="overlay{util:replace(@nodeID, '.', '_')}" style="width: 200px; height: 200px; position: absolute; top: 0; left: 0; z-index: 200;">
								<xsl:text> </xsl:text>
							</div>
							<div id="large{util:replace(@nodeID, '.', '_')}" style="position: relative; width: {$imageWidth}px; height: {$imageHeight}px;">
								<gsf:image type="source"/>
							</div>
						</div>
					</div>
					<script type="text/javascript">
						<xsl:text disable-output-escaping="yes">
							{
								var nodeID = "</xsl:text><xsl:value-of select="@nodeID"/><xsl:text disable-output-escaping="yes">";
								nodeID = nodeID.replace(/\./g, "_");

								var bigHeight = </xsl:text><xsl:value-of select="$imageHeight"/><xsl:text disable-output-escaping="yes">;
								var smallHeight = </xsl:text><xsl:value-of select="$screenImageHeight"/><xsl:text disable-output-escaping="yes">;

								var multiplier = bigHeight / smallHeight;

								$("#wrap" + nodeID).anythingZoomer({
									smallArea: "#small" + nodeID,
									largeArea: "#large" + nodeID,
									zoomPort: "#overlay" + nodeID,
									mover: "#mover" + nodeID,
									expansionSize:50,  
									speedMultiplier:multiplier   
								});
								
								$("#zoomOptions").css("display", null);
							}
						</xsl:text>
					</script>
				</div>
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
	</xsl:template>
	
	<xsl:template name="documentHeading">
		<b><gsf:metadata name="Title"/></b><br/>
	</xsl:template>
	
	<!-- The default template for displaying the document node text -->
	<!-- equivalent to gsf:text -->
	<xsl:template name="documentNodeText">
		<!-- Hides the "This document has no text." message -->
		<xsl:variable name="noText"><gsf:metadata name="NoText"/></xsl:variable>
		<xsl:if test="not($noText = '1')">
			<!-- Section text -->
			<xsl:for-each select="nodeContent">
				<xsl:for-each select="node()">
					<xsl:choose>
						<xsl:when test="not(name())">
							<xsl:value-of select="." disable-output-escaping="yes"/>
						</xsl:when>
						<xsl:when test="name() = 'annotation'">
							<xsl:call-template name="displayAnnotation"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:apply-templates/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:for-each>
			</xsl:for-each>
		</xsl:if><xsl:text> </xsl:text>
	</xsl:template>

	<!-- Used to produce a version of the page in a format that can be read by the realistic books plugin -->
	<xsl:template name="documentNodeFlashXML">
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
				
		<xsl:for-each select="documentNode">
			<xsl:call-template name="documentNodeFlashXML"/>
		</xsl:for-each>
				
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
	
	<xsl:template name="documentPre">
		<xsl:if test="/page/pageResponse/format[@type='display' or @type='browse' or @type='search']/gsf:option[@name='mapEnabled']/@value = 'true'">
			<xsl:call-template name="mapFeatures"/>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="mapFeatures">
		<div id="map_canvas" class="map_canvas_full"><xsl:text> </xsl:text></div>

		<xsl:for-each select="documentNode">
			<xsl:if test="metadataList/metadata[@name = 'Latitude'] and metadataList/metadata[@name = 'Longitude']">
				<div style="background:#BBFFBB; padding: 5px; margin:0px auto; width:890px;">
					<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.maps.nearby_docs')"/>
					<img id="nearbyDocumentsToggle" style="margin-left:5px;" src="interfaces/{$interface_name}/images/expand.png">
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
		</xsl:for-each>
		
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
