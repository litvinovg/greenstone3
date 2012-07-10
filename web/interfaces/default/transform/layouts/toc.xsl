<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util gsf">
	
	<xsl:template name="rightSidebar">
		<div id="rightSidebar">
			<xsl:call-template name="displayCoverImage"/>
			<xsl:call-template name="viewOptions"/>
			<xsl:call-template name="displayTOC"/>
		</div>
	</xsl:template>
	
	<xsl:template name="displayCoverImage">
		<!-- Need to be in the context of the top-level documentNode rather than the document for the gsf:metadata call to work -->
		<xsl:for-each select="documentNode">
			<xsl:variable name="hasCover"><gsf:metadata name="hascover"/></xsl:variable>
			<xsl:if test="$hasCover = '1' and (not(/page/pageResponse/format[@type='display']/gsf:option[@name='coverImage']) or /page/pageResponse/format[@type='display']/gsf:option[@name='coverImage']/@value='true')">
				<!-- the book's cover image -->
				<div id="coverImage">
					<img>
						<xsl:attribute name="src"><xsl:value-of select="$httpPath"/>/index/assoc/<gsf:metadata name="assocfilepath"/>/cover.jpg</xsl:attribute>
					</img><xsl:text> </xsl:text>
				</div>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="displayTOC">
		<div class="tableOfContentsContainer ui-state-default">
			<table class="tocTable ui-widget-content">
				<tr>
					<td style="vertical-align:top; text-align:right;">
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
					</td>
				</tr>
				<tr><td>
					<div id="contentsArea">	
						<!-- show the berry basket if it's turned on -->
						<gslib:berryBasket/>

						<!-- the contents (if enabled) -->
						<xsl:choose>
							<xsl:when test="/page/pageResponse/document/@docType = 'paged'">
								<gsf:image type="Thumb"/>
								<!-- Table of contents will be dynamically retrieved when viewing a paged document -->
								<script type="text/javascript">
									<xsl:text disable-output-escaping="yes">
										$(window).load(function()
										{
											retrieveTableOfContentsAndTitles();
										});
									</xsl:text>
								</script>
								<div id="tableOfContents"><xsl:text> </xsl:text></div>
								<div id="tocLoadingImage" style="text-align:center;">
									<img src="{util:getInterfaceText($interface_name, /page/@lang, 'loading_image')}"/><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.loading')"/><xsl:text>...</xsl:text>
								</div>
								<table style="width:100%;"><tbody><tr>
									<td><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.filter_pages')"/><xsl:text>: </xsl:text><input id="filterText" type="text" size="27"/></td>
								</tr></tbody></table>
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
									<xsl:for-each select="documentNode">
										<xsl:call-template name="documentNodeTOC"/>
									</xsl:for-each>
								</div>
							</xsl:otherwise>
						</xsl:choose>
					</div>
				</td></tr>
			</table>
		</div>
	</xsl:template>
	
	<!-- This template is used to display the table of contents -->
	<xsl:template name="documentNodeTOC">

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
						<xsl:call-template name="sectionTitle"/>
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
					<xsl:for-each select="documentNode">
						<xsl:call-template name="documentNodeTOC"/>
					</xsl:for-each>
				</li>
			</xsl:if>
			
		</ul>
	</xsl:template>
	
	<xsl:template name="viewOptions">
		<div id="viewAndZoomOptions" class="ui-state-default ui-corner-all">
			<ul id="viewOptions">
				<!-- Paged-image options -->
				<xsl:if test="count(//documentNode/metadataList/metadata[@name = 'Screen']) > 0 or /page/pageResponse/document/documentNode/@docType = 'paged'">
					<li>
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
					</li>
				</xsl:if>
			
				<!-- Realistic books link -->
				<xsl:if test="/page/pageResponse/collection[@name = $collName]/metadataList/metadata[@name = 'tidyoption'] = 'tidy'">
					<li>
						<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.realisticBooksTooltip')"/></xsl:attribute>
						<img>
							<xsl:attribute name="src"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'realistic_books_image')"/></xsl:attribute>
						</img>
						<input id="rbOption" type="checkbox" onclick="bookInit();" class="optionCheckBox"/>
					</li>
				</xsl:if>
				
				<!-- Highlight on/off button -->
				<xsl:if test="util:contains(/page/pageRequest/paramList/param[@name = 'p.s']/@value, 'Query')">
					<li>
						<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.highlightTooltip')"/></xsl:attribute>
						<img>
							<xsl:attribute name="src"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'highlight_image')"/></xsl:attribute>
						</img>
						<input id="highlightOption" type="checkbox" class="optionCheckBox" onclick="swapHighlight();">
							<xsl:if test="/page/pageRequest/paramList/param[@name = 'hl']/@value = 'on'">
								<xsl:attribute name="checked">true</xsl:attribute>
							</xsl:if>
						</input>
					</li>
				</xsl:if>
			</ul>
			<ul id="zoomOptions">
				<!-- This is invisible unless it is made visible by Javascript controlling the image zooming -->
				<xsl:attribute name="style">display: none;</xsl:attribute>
				<li style="width:10%;">
					<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.zoom')"/>
				</li>
				<li style="width:5%;">
					<input id="zoomToggle" type="checkbox"/>
					<script type="text/javascript">
						<xsl:text disable-output-escaping="yes">
							$("#zoomToggle").change(function()
							{
								_imageZoomEnabled = !_imageZoomEnabled;
							});
						</xsl:text>
					</script>
				</li>
				<li style="width:15%;"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.zoom_size')"/><xsl:text>:</xsl:text></li>
				<li id="zoomSlider" style="width:50%; height:5px;"><xsl:text> </xsl:text></li>
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
				<style>.ui-slider .ui-slider-handle{height:0.8em; width:1.0em;}</style>
			</ul>
		</div>
	</xsl:template>
</xsl:stylesheet>