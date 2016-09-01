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
	    <xsl:choose>
	      <xsl:when test="@docType = 'simple'">
		<xsl:for-each select=".">
		  <xsl:call-template name="displayCoverImage"/>
		</xsl:for-each>
		<xsl:call-template name="viewOptions"/>
		<!--<xsl:text>  </xsl:text>-->
	      </xsl:when>
	    <xsl:otherwise>
	      <xsl:for-each select="documentNode[1]">
		<xsl:call-template name="displayCoverImage"/>
	      </xsl:for-each>
	      <xsl:call-template name="viewOptions"/>
	      <xsl:call-template name="displayTOC"/>
	    </xsl:otherwise>
	  </xsl:choose>
	  </div>
	</xsl:template>
	
	<!-- this is called in the context of the top level node with the metadataList. For a simple doc, this is the document node. For a comples document, this is the first documentNode hcild of the document node. -->
	<xsl:template name="displayCoverImage">
	  <xsl:variable name="hasCover"><gsf:metadata name="hascover"/></xsl:variable>
	  <xsl:if test="$hasCover = '1' and (not(/page/pageResponse/format[@type='display']/gsf:option[@name='coverImage']) or /page/pageResponse/format[@type='display']/gsf:option[@name='coverImage']/@value='true')">
	    <!-- the book's cover image -->
	    <div id="coverImage">
	      <img>
		<xsl:attribute name="src"><xsl:value-of select="$httpPath"/>/index/assoc/<gsf:metadata name="assocfilepath" pos="1"/>/cover.jpg</xsl:attribute>
	      </img><xsl:text> </xsl:text>
	    </div>
	  </xsl:if>
	</xsl:template> 
	
	<xsl:template name="displayTOC">
	  <xsl:if test="not(/page/pageResponse/format[@type='display']/gsf:option[@name='TOC']) or /page/pageResponse/format[@type='display']/gsf:option[@name='TOC']/@value='true'">
		<div class="tableOfContentsContainer ui-state-default">
			<table class="tocTable ui-widget-content">
				<tr>
					<td id="unfloatTOCButton" style="display:none;">
						<a href="javascript:floatMenu(false);">
							<img class="icon" style="padding-top:3px;">
								<xsl:attribute name="src">
									<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'left_arrow_image')"/>
								</xsl:attribute>
							</img>
						</a>
					</td>
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
							<xsl:when test="/page/pageResponse/document/@docType = 'paged' or /page/pageResponse/document/@docType = 'pagedhierarchy'">
								<gsf:image type="Thumb"/>
								<!-- Table of contents will be dynamically retrieved when viewing a paged document -->
								<script type="text/javascript">
									<xsl:text disable-output-escaping="yes">
										$(window).load(function()
										{
											retrieveTableOfContentsAndTitles();
										        <!--setUpFilterButtons();-->
										});
									</xsl:text>
								</script>
								<div id="tableOfContents"><xsl:text> </xsl:text></div>
								<div id="tocLoadingImage" style="text-align:center;">
									<img src="{util:getInterfaceText($interface_name, /page/@lang, 'loading_image')}"/><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.loading')"/><xsl:text>...</xsl:text>
								</div>
								<label for="filterText"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.filter_pages')"/><xsl:text>: </xsl:text></label><span id="filterOnButtons" style="float: right;">...</span><input id="filterText" type="text" style="width: 100%;"/>
								<xsl:if test="/page/pageRequest/userInformation and /page/pageRequest/userInformation/@editEnabled = 'true'">
								  <a href="javascript:extractFilteredPagesToOwnDocument();"><button id="extractDocButton"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'de.extract_pages')"/></button></a>
								</xsl:if>
								  
							<!--	<table style="width:100%;"><tbody><tr>
									<td><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.filter_pages')"/><xsl:text>: </xsl:text><input id="filterText" type="text"/></td>
								</tr>
								<xsl:if test="/page/pageRequest/userInformation and /page/pageRequest/userInformation/@editEnabled = 'true'">
									<tr><td><a href="javascript:extractFilteredPagesToOwnDocument();"><button id="extractDocButton">Extract these pages to document</button></a></td></tr>
								</xsl:if>
								</tbody></table>-->
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
	  </xsl:if>
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
				<td class="tocTextCell">				
					<a id="toclink{@nodeID}" href="javascript:focusSection('{@nodeID}');">
						<xsl:if test="util:hashToSectionId(@nodeID)">
						  <span class="tocSectionNumber"><xsl:value-of select="util:hashToSectionId(@nodeID)"/><!--<xsl:text> </xsl:text>--></span></xsl:if><span class="tocSectionTitle"><xsl:call-template name="sectionHeading"/></span></a>
					
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
				<!-- Paged-image document options -->
	                        <xsl:if test="count(//documentNode/metadataList/metadata[@name = 'Screen']) > 0 or /page/pageResponse/document/@docType = 'paged' or /page/pageResponse/document/@docType = 'pagedhierarchy'">
                                <!-- view selection option -->
				  <xsl:if test="not(/page/pageResponse/format[@type='display']/gsf:option[@name='ViewSelection']) or /page/pageResponse/format[@type='display']/gsf:option[@name='ViewSelection']/@value='true'">
				<li id="pagedImageOptions">
					<select id="viewSelection" onchange="changeView();">
						<xsl:choose>
							<xsl:when test="/page/pageRequest/paramList/param[@name = 'view']/@value = 'image'">
								<option><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.default_view')"/></option>
								<option selected="true"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.image_view')"/></option>
								<option><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.text_view')"/></option>
							</xsl:when>
							<xsl:when test="/page/pageRequest/paramList/param[@name = 'view']/@value = 'text'">
								<option><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.default_view')"/></option>
								<option><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.image_view')"/></option>
								<option selected="true"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.text_view')"/></option>
							</xsl:when>
							<xsl:otherwise>
								<option selected="true"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.default_view')"/></option>
								<option><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.image_view')"/></option>
								<option><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.text_view')"/></option>
							</xsl:otherwise>
						</xsl:choose>
					</select>
				</li>
				</xsl:if>
				<!-- Slide-show options -->
				<xsl:if test="not(/page/pageResponse/format[@type='display']/gsf:option[@name='SlideShow']) or /page/pageResponse/format[@type='display']/gsf:option[@name='SlideShow']/@value='true'">
				<li id="slideShowOptions">
					<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.slideshowTooltip')"/></xsl:attribute>
					<img onclick="showSlideShow()">
						<xsl:attribute name="src"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'slideshow_image')"/></xsl:attribute>
					</img>
				</li>
			        </xsl:if> 
				</xsl:if>
				<!-- Realistic books link -->
				<xsl:if test="/page/pageResponse/collection[@name = $collName]/metadataList/metadata[@name = 'tidyoption'] = 'tidy'">
					<li>
						<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.realisticBooksTooltip')"/></xsl:attribute>
						<img id="rbOptionImage" onclick="bookInit();">
							<xsl:attribute name="src"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'realistic_books_image')"/></xsl:attribute>
						</img>
						<input id="rbOption" type="checkbox" onclick="bookInit();" class="optionCheckBox"/>
					</li>
				</xsl:if>
				
				<!-- Highlight on/off button -->
				<xsl:if test="util:contains(/page/pageRequest/paramList/param[@name = 'p.s']/@value, 'Query') and not(metadataList/metadata[@name='NoText'])">
					<li>
						<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.highlightTooltip')"/></xsl:attribute>
						<img onclick="swapHighlight(true);">
							<xsl:attribute name="src"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'highlight_image')"/></xsl:attribute>
						</img>
						<input id="highlightOption" type="checkbox" class="optionCheckBox" onclick="swapHighlight(false);">
							<xsl:if test="not(/page/pageRequest/paramList/param[@name = 'hl']/@value = 'off')">
								<xsl:attribute name="checked">checked</xsl:attribute>
							</xsl:if>
						</input>
					</li>
				</xsl:if><xsl:text> </xsl:text>
				
				<!-- Zoom on/off button -->
				<li id="zoomOptions" style="display:none;">
					<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.zoomTooltip')"/></xsl:attribute>
					<img id="zoomToggleImage">
						<xsl:attribute name="src"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'zoom_image')"/></xsl:attribute>
					</img>
					<input id="zoomToggle" type="checkbox"/>
					<script type="text/javascript">
						<xsl:text disable-output-escaping="yes">
							$("#zoomToggle").change(function()
							{
								_imageZoomEnabled = $("#zoomToggle").prop("checked");
							});
							
							$("#zoomToggleImage").click(function()
							{
								$("#zoomToggle").prop("checked", !$("#zoomToggle").prop("checked"));
								_imageZoomEnabled = $("#zoomToggle").prop("checked");
							});
						</xsl:text>
					</script>
				</li>

				<!-- Floating TOC on/off button -->
				<xsl:if test="count(//documentNode) > 0 and (not(/page/pageResponse/format[@type='display']/gsf:option[@name='TOC']) or /page/pageResponse/format[@type='display']/gsf:option[@name='TOC']/@value='true')">
					<li id="floatingTOCOptions">
						<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.floatingTooltip')"/></xsl:attribute>
						<img id="floatTOCToggleImage">
							<xsl:attribute name="src"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'float_toc_image')"/></xsl:attribute>
						</img>
						<input id="floatTOCToggle" type="checkbox"/>
						<script type="text/javascript">
							<xsl:text disable-output-escaping="yes">
								$("#floatTOCToggle").prop("checked", false);
								$("#floatTOCToggle").click(function()
								{
									floatMenu($("#floatTOCToggle").prop("checked"));
								});
								
								$("#floatTOCToggleImage").click(function()
								{
									$("#floatTOCToggle").prop("checked", !$("#floatTOCToggle").prop("checked"))
									floatMenu($("#floatTOCToggle").prop("checked"));
								});
							</xsl:text>
						</script>
					</li>
					<xsl:if test="/page/pageRequest/paramList/param[@name='ftoc']/@value = '1'">
						<script type="text/javascript">
							<xsl:text disable-output-escaping="yes">
								$(window).load(function()
								{
									$("#floatTOCToggle").prop("checked", true);
									floatMenu(true);
								});
							</xsl:text>
						</script>
					</xsl:if>
				</xsl:if>
			</ul>
			<div style="clear:both;"><xsl:text> </xsl:text></div>
		</div>
	</xsl:template>
</xsl:stylesheet>
