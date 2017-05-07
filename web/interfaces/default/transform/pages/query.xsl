<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	xmlns:gsvar="http://www.greenstone.org/skinning-var"
	xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util gsf">

	<!-- use the 'main' layout -->
	<xsl:import href="layouts/main.xsl"/>
	<xsl:import href="map-tools.xsl"/>
	<xsl:import href="panorama-viewer-tools.xsl"/>

	<!-- set page title -->
	<xsl:template name="pageTitle">
		<gslib:serviceName/>
	</xsl:template>

	<!-- set page breadcrumbs -->
	<xsl:template name="breadcrumbs">
		<gslib:siteLink/>
		<gslib:rightArrow/>
		<xsl:if test="/page/pageResponse/collection">
			<gslib:collectionNameLinked/>
			<gslib:rightArrow/>
		</xsl:if>
	</xsl:template>

	<!-- do we display the line 'displaying 0 to 20 of 40 documents'-->
	<!-- on by default. To turn off, add the following in <search> element in collectionConfig.xml:
	     <gsf:option name="hideStatus" value="true"/> -->
	<xsl:variable name="display_status_bar">
	  <xsl:if test="/page/pageResponse/format[@type='search']/gsf:option[@name='hideStatus']/@value='true'">false</xsl:if></xsl:variable>
	<!-- the page content -->
	<xsl:template match="/page">
		<xsl:if test="not(/page/pageRequest/paramList/param[@name = 'qs']) or /page/pageRequest/paramList/param[@name = 'qs']/@value = ''">
			<xsl:call-template name="queryPage"/>
		</xsl:if>
    <xsl:if test="contains(/page/pageRequest/paramList/param[@name='rt']/@value, 'r')">
		<xsl:call-template name="resultsPage"/>
   </xsl:if>
	</xsl:template>

	<xsl:template name="resultsPage">
		<xsl:call-template name="resultsPagePre"/>
		<xsl:call-template name="displayMatchDocs"/>
		<xsl:call-template name="displayTermInfo"/>
		<xsl:call-template name="displayBaskets"/>
		<xsl:call-template name="displayResults"/>
		<xsl:call-template name="resultsPagePost"/>
	</xsl:template>

	<!-- optional cgi-params for links to document pages -->
	<xsl:variable name="opt-doc-link-args">p.s=<gsvar:this-service/>
	</xsl:variable>

	<xsl:template name="displayBaskets">
	  <xsl:if test="$berryBasketOn or ($documentBasketOn and (util:contains(/page/pageRequest/userInformation/@groups, 'administrator') or util:contains(/page/pageRequest/userInformation/@groups, 'all-collections-editor') or util:contains(/page/pageRequest/userInformation/@groups, $thisCollectionEditor)))">
	    <div id="rightSidebar">
	      <xsl:if test="$berryBasketOn">
		<!-- show the berry basket if it's turned on -->
		<gslib:berryBasket/>
		<xsl:text> </xsl:text>
	      </xsl:if>
	      <xsl:if test="$documentBasketOn and (util:contains(/page/pageRequest/userInformation/@groups, 'administrator') or util:contains(/page/pageRequest/userInformation/@groups, 'all-collections-editor') or util:contains(/page/pageRequest/userInformation/@groups, $thisCollectionEditor))">
		<gslib:documentBasket/>
		<xsl:text> </xsl:text>
	      </xsl:if>
	    </div>
	  </xsl:if>
	</xsl:template>

	<xsl:template name="displayResults">
		<xsl:if test="/page/pageResponse/facetList/facet">
			<script type="text/javascript" src="interfaces/{$interface_name}/js/facet-scripts.js">
				<xsl:text> </xsl:text>
			</script>
			<div id="facetSelector">
				<xsl:for-each select="/page/pageResponse/facetList/facet">
					<xsl:if test="count(count) > 0">
						<ul class="facetTable ui-widget-content" indexName="{@name}">
							<xsl:variable name="serviceName">
								<xsl:value-of select="/page/pageRequest/paramList/param[@name = 's']/@value"/>
							</xsl:variable>
							<xsl:variable name="indexShortName">
								<xsl:value-of select="@name"/>
							</xsl:variable>
							<xsl:variable name="countSize">
								<xsl:choose>
									<xsl:when test="/page/pageResponse/format[@type='search']/gsf:option[@name='facetTableRows']">
										<xsl:value-of select="/page/pageResponse/format[@type='search']/gsf:option[@name='facetTableRows']/@value"/>
									</xsl:when>
									<xsl:otherwise>8</xsl:otherwise>
								</xsl:choose>
							</xsl:variable>

							<li class="ui-widget-header" style="text-transform:capitalize; text-align:center;">
								<xsl:choose>
									<xsl:when test="/page/pageResponse/collection/serviceList/service[@name = $serviceName]/paramList/param[@name = 'index']">
										<xsl:value-of select="/page/pageResponse/collection/serviceList/service[@name = $serviceName]/paramList/param[@name = 'index']/option[@name = $indexShortName]/displayItem"/>
									</xsl:when>
									<xsl:when test="/page/pageResponse/collection/serviceList/service[@name = $serviceName]/paramList/param[@name = 'complexField']/param[@name = 'fqf']">
										<xsl:value-of select="/page/pageResponse/collection/serviceList/service[@name = $serviceName]/paramList/param[@name = 'complexField']/param[@name = 'fqf']/option[@name = $indexShortName]/displayItem"/>
									</xsl:when>
								</xsl:choose>
							</li>
							<xsl:for-each select="count">
								<li>
									<xsl:attribute name="style">
										<xsl:if test="position() > $countSize">display:none;<xsl:value-of select="$indexShortName"/>
										</xsl:if>
									</xsl:attribute>
									<input type="checkbox" onclick="performRefinedSearch();"/>
									<span>
										<xsl:value-of select="@name"/>
									</span>(<xsl:value-of select="."/>)
								</li>
							</xsl:for-each>
							<xsl:if test="count(count) > $countSize">
								<li class="expandFacetList{$indexShortName}">
									<a class="expandFacetListLink{$indexShortName}" href="javascript:expandFacetList('{$indexShortName}', {$countSize});"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.facet.more')"/></a>
								</li>
								<li class="collapseFacetList{$indexShortName}" style="display: none;">
									<a  class="collapseFacetListLink{$indexShortName}" href="javascript:collapseFacetList('{$indexShortName}', {$countSize});"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.facet.less')"/></a>
								</li>
							</xsl:if>
						</ul>
					</xsl:if>
				</xsl:for-each>
			</div>
		</xsl:if>
		<div id="resultsArea">
			<xsl:attribute name="class">
				<xsl:if test="/page/pageResponse/facetList/facet">facetedResults</xsl:if>
			</xsl:attribute>
			<table id="resultsTable">
				<xsl:for-each select="pageResponse/documentNodeList/documentNode">
					<tr id="div{@nodeID}" class="document">
						<xsl:apply-templates select="."/>
						<!-- The berry (optional) -->
						<td>
						  <xsl:call-template name="documentBerryForClassifierOrSearchPage"/>
						</td>
						<xsl:call-template name="highlightSnippet">
							<xsl:with-param name="nodeHASH">
								<xsl:value-of select="@nodeID"/>
							</xsl:with-param>
						</xsl:call-template>
						<xsl:call-template name="documentNodePost"/>
					</tr>
				</xsl:for-each>
				<br/>
			</table>

			<!-- Previous/Next buttons-->
			<xsl:call-template name="prevNextButtons"/>
		</div>
		<xsl:if test="/page/pageResponse/facetList/facet">
			<div style="clear:both;">
				<xsl:text> </xsl:text>
			</div>
		</xsl:if>
	</xsl:template>

	<xsl:template name="highlightSnippet">
		<xsl:param name="nodeHASH">
		</xsl:param>
		<xsl:for-each select="/page/pageResponse/highlightSnippetList/highlightSnippet[@nodeID = $nodeHASH]">
                        <tr>
                        	<td>
                                </td>
                                <td>
        	                        <div class="HighlightSnippet">
                	               		<xsl:value-of select="." disable-output-escaping="yes"/>
	                                </div>
                	        </td>
        	        </tr>
	        </xsl:for-each>
	</xsl:template>
	<xsl:template name="queryPage">
		<xsl:for-each select="pageResponse/service">
			<form name="QueryForm" method="get" action="{$library_name}/collection/{$collName}/search/{@name}">
				<div>
					<input type="hidden" name="a" value="q"/>
					<input type="hidden" name="sa">
						<xsl:attribute name="value">
							<xsl:value-of select="/page/pageRequest/@subaction"/>
						</xsl:attribute>
					</input>
					<input type="hidden" name="rt" value="rd"/>
					
					<xsl:variable name="ns">s1.</xsl:variable>
					<xsl:choose>
						<xsl:when test="/page/pageResponse/hierarchy and paramList/param[@name ='collection']">
						 	<xsl:call-template name="hierarchy">
						 		<xsl:with-param name="ns" select="$ns" />
						 	</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
					<xsl:for-each select="paramList/param">
						<xsl:choose>
							<xsl:when test="@type='multi'">
								<xsl:apply-templates select=".">
									<xsl:with-param name="ns" select="$ns"/>
								</xsl:apply-templates>
							</xsl:when>
							<xsl:otherwise>
								<xsl:call-template name="param-display">
									<xsl:with-param name="ns" select="$ns"/>
								</xsl:call-template>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
						</xsl:otherwise>
					</xsl:choose>
					<br/>
					<input type="submit">
						<xsl:attribute name="value">
							<xsl:value-of select="displayItem[@name='submit']"/>
						</xsl:attribute>
					</input>
				</div>
			</form>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="hierarchy">
		<xsl:param name="ns" />
		<div class="paramLabel">
			<xsl:value-of
				select="paramList/param[@name='collection']/displayItem[@name='name']/text()" />
		</div>
		<div class="paramValue">
			<select multiple="" size='10'>
				<xsl:attribute name="name">
						<xsl:value-of select="$ns"></xsl:value-of>
						<xsl:text>collection</xsl:text>
					</xsl:attribute>
				<xsl:for-each select="/page/pageResponse/hierarchy/*">
					<xsl:call-template name="hierarchy-display">
						<xsl:with-param name="padding">
							0
						</xsl:with-param>
						<xsl:with-param name="path">
							<xsl:text>/</xsl:text>
						</xsl:with-param>
					</xsl:call-template>
				</xsl:for-each>
			</select>
		</div>
		<br class="clear" />
		<xsl:for-each select="paramList/param[@name !='collection']">
			<xsl:choose>
				<xsl:when test="@type='multi'">
					<xsl:apply-templates select=".">
						<xsl:with-param name="ns" select="$ns"/>
					</xsl:apply-templates>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="param-display">
						<xsl:with-param name="ns" select="$ns"/>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="hierarchy-display">
		<xsl:param name="padding" />
		<xsl:param name="path" />
		<xsl:choose>
			<xsl:when test="name(.) = 'group'">
				<option>
					<xsl:attribute name="style">
						<xsl:text>padding-left: </xsl:text>
						<xsl:value-of select="concat($padding,'px')" />
					</xsl:attribute>
					<xsl:attribute name="value">
						<xsl:text>group</xsl:text>
						<xsl:value-of select="$path" />
						<xsl:value-of select="@name" />
					</xsl:attribute>
					<xsl:value-of select="@title"></xsl:value-of>
					<xsl:for-each select="./*">
						<xsl:call-template name="hierarchy-display">
							<xsl:with-param name="padding">
								<xsl:value-of select="$padding + 20" />
							</xsl:with-param>
							<xsl:with-param name="path">
								<xsl:value-of select="$path" />
								<xsl:text>/</xsl:text>
								<xsl:value-of select="../@name" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:for-each>
				</option>
			</xsl:when>
			<xsl:otherwise>
				<option>
					<xsl:attribute name="style">
							<xsl:text>padding-left: </xsl:text>
							<xsl:value-of select="$padding" /> 
							<xsl:text>px</xsl:text>
						</xsl:attribute>
					<!-- <xsl:attribute name="class">
							<xsl:value-of select="$path" /> 
						</xsl:attribute> -->
					<xsl:attribute name="value">
						<xsl:value-of select="current()/@name"></xsl:value-of>
					</xsl:attribute>
					<xsl:value-of
						select="/page/pageResponse/service[@name = 'TextQuery']/paramList/param[@name = 'collection']/option[@name = current()/@name]/displayItem[@name = 'name']/text()"></xsl:value-of>
				</option>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="displayMatchDocs">
		<div id="matchdocs">
			<xsl:variable name="numDocsMatched" select="/page/pageResponse/metadataList/metadata[@name='numDocsMatched']"/>
			<xsl:variable name="numDocsReturned" select="/page/pageResponse/metadataList/metadata[@name='numDocsReturned']"/>
			<xsl:variable name="docLevel">
				<xsl:call-template name="documentLevel"/>
			</xsl:variable>
			<xsl:variable name="docLevelText">
				<xsl:call-template name="documentLevelText">
					<xsl:with-param name="numDocsMatched" select="$numDocsMatched"/>
					<xsl:with-param name="level" select="$docLevel"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:choose>
				<xsl:when test="$numDocsMatched='0' or $numDocsReturned='0'">
					<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.nodocsmatch', $docLevelText)"/>
				</xsl:when>
				<xsl:when test="$numDocsMatched='1' or $numDocsReturned='1'">
					<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.onedocsmatch', $docLevelText)"/>
				</xsl:when>
				<xsl:when test="$numDocsMatched">
				        <xsl:variable name="gitArgs1" select="concat($numDocsMatched, ';', $docLevelText)"/>
				        <xsl:variable name="gitArgs2" select="concat($numDocsReturned, ';', $docLevelText)"/>
					
					<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.manydocsmatch', $gitArgs1)"/>
					<xsl:if test="$numDocsReturned and not($numDocsMatched=$numDocsReturned)"> (<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.docsreturned', $gitArgs2)"/>)</xsl:if>
				</xsl:when>
				<xsl:when test="$numDocsReturned">
				        <xsl:variable name="gitArgs" select="concat($numDocsReturned, ';', $docLevelText)"/>
					<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.atleastdocsmatch', $gitArgs)"/>
				</xsl:when>
			</xsl:choose>
		</div>

	</xsl:template>

	<xsl:template name="documentLevel">
		<xsl:choose>
			<xsl:when test="/page/pageRequest/paramList/param[@name='level']">
				<xsl:value-of select="/page/pageRequest/paramList/param[@name='level']/@value" />
			</xsl:when>
			<xsl:when test="/page/pageRequest/paramList/param[@name='s1.level']">
				<xsl:value-of select="/page/pageRequest/paramList/param[@name='s1.level']/@value" />
			</xsl:when>
			<xsl:otherwise>Doc</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="documentLevelText">
		<xsl:param name="numDocsMatched">0</xsl:param>
		<xsl:param name="level">Doc</xsl:param>
		<xsl:choose>	
			<xsl:when test="$numDocsMatched = 1">
				<xsl:choose>
					<xsl:when test="$level = 'Doc'">
						<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.document')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.section')"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<!-- 0 or more than one use plural. is that the case for all langs??-->
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="$level = 'Doc'">
						<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.document_plural')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.section_plural')"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="displayTermInfo">

		<!-- Find the number of documents displayed per page -->
		<xsl:variable name="level">
			<xsl:call-template name="documentLevel"/>
		</xsl:variable>

		<!-- The list of search terms with their frequency and document count -->
		<p class="termList">
			<xsl:if test="count(/page/pageResponse/termList/stopword) &gt; 0">
				<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.common')"/>
				<xsl:text> </xsl:text>
				<xsl:for-each select="/page/pageResponse/termList/stopword">
					<span style="font-style:italic;">
						<xsl:value-of select="@name"/>
					</span>
					<xsl:text> </xsl:text>
				</xsl:for-each>
				<br />
				<br />
			</xsl:if>

			<!-- If there is only one or two search terms then show the expanded information -->
			<xsl:choose>
			  <xsl:when test="count(/page/pageResponse/termList/term) &lt; 3">
			    
			    <xsl:for-each select="/page/pageResponse/termList/term">
			      <xsl:variable name="levelText">
				<!-- this gets document, section, documents, sections, depending on level and number -->
				<xsl:call-template name="documentLevelText">
				  <xsl:with-param name="level" select="$level"/>
				  <xsl:with-param name="numDocsMatched" select="@numDocsMatch"/>
				</xsl:call-template>
			      </xsl:variable>
			      <span class="termInfo">
						  
				<!-- For solr and mg collections, display: 
				     term x occured y times
				     term a occured b times
				     For collections using all other indexers, display: 
				     term x occurred y times in n sections/documents
				     term a occurred b times in m sections/docs
				-->
				<!-- not all these args are used in every case -->
				<xsl:variable name="args" select="concat(@name,';', @freq, ';', @numDocsMatch,';',  $levelText)"/>
				<xsl:choose>
				  <xsl:when test="@freq = 0">
				    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.termoccurs.0',$args)"/>
				  </xsl:when>
				  <xsl:when test="@freq = 1">
				    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.termoccurs.1',$args)"/>
				  </xsl:when>
				  <xsl:otherwise> <!-- more than one occurrance -->
				    <xsl:choose>
				      <xsl:when test="/page/pageResponse/collection[@type = 'solr'] or /page/pageResponse/collection[@type = 'mg']">
					<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.termoccurs.x',$args)"/>
				      </xsl:when>
				      <xsl:when test="@numDocsMatch = 1">
					<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.termoccurs.x.1',$args)"/>
				      </xsl:when>
				      <xsl:otherwise>
					<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.termoccurs.x.x',$args)"/>
				      </xsl:otherwise>
				    </xsl:choose>
				  </xsl:otherwise>
				</xsl:choose>
				
			      </span>
			      <br/>
			    </xsl:for-each>
			  </xsl:when>
			  <xsl:otherwise>
					<xsl:for-each select="/page/pageResponse/termList/term">
						<span style="font-style:italic;">
							<xsl:value-of select="@name"/>
						</span> (<xsl:value-of select="@freq"/>)
					</xsl:for-each>
				</xsl:otherwise>
			</xsl:choose>
		</p>
	</xsl:template>

	<xsl:template name="prevNextButtons">	
		<!-- Current page -->
		<xsl:variable name="currentPage">
			<xsl:choose>
				<xsl:when test="/page/pageRequest/paramList/param[@name='s1.startPage']/@value">
					<xsl:value-of select="/page/pageRequest/paramList/param[@name='s1.startPage']/@value" />
				</xsl:when>
				<xsl:when test="/page/pageRequest/paramList/param[@name='startPage']/@value">
					<xsl:value-of select="/page/pageRequest/paramList/param[@name='startPage']/@value" />
				</xsl:when>
				<xsl:otherwise>1</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="usesS1">
			<xsl:choose>
				<xsl:when test="/page/pageResponse/service/paramList/param[@name='startPage']">true</xsl:when>
				<xsl:otherwise>false</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- Find the total number of documents returned/matched -->
		<xsl:variable name="docMax">
			<xsl:choose>
				<xsl:when test="/page/pageResponse/metadataList/metadata[@name = 'numDocsReturned']">
					<xsl:value-of select="/page/pageResponse/metadataList/metadata[@name = 'numDocsReturned']"/>
				</xsl:when>
				<xsl:when test="/page/pageResponse/metadataList/metadata[@name = 'numDocsMatched']">
					<xsl:value-of select="/page/pageResponse/metadataList/metadata[@name = 'numDocsMatched']"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="count(/page/pageResponse/documentNodeList/documentNode)"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- Find the number of documents displayed per page -->
		<xsl:variable name="docsPerPage">
			<xsl:choose>
				<xsl:when test="/page/pageRequest/paramList/param[@name='s1.hitsPerPage']">
					<xsl:value-of select="/page/pageRequest/paramList/param[@name='s1.hitsPerPage']/@value" />
				</xsl:when>
				<xsl:when test="/page/pageResponse/service/paramList/param[@name='hitsPerPage']">
				  <xsl:value-of select="/page/pageResponse/service/paramList/param[@name='hitsPerPage']/@default" />
				  </xsl:when>
				<xsl:otherwise><xsl:value-of select="count(/page/pageResponse/documentNodeList/documentNode)"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- Find the level -->
		<xsl:variable name="level">
		  <xsl:call-template name="documentLevel"/>
		</xsl:variable>
		
		<!-- we only need this if number of docs > docsperpage -->
		<xsl:if test="$docMax &gt; $docsPerPage">
		<table id="searchResultNavTable">
			<tr>
				<xsl:variable name="startPageName">
					<xsl:if test="$usesS1 = 'true'">s1.</xsl:if>startPage</xsl:variable>

				<!-- Previous button -->
				<td id="prevArrowTD">
					<xsl:if test="$currentPage != 1">
						<a href="{$library_name}?a=q&amp;sa={/page/pageRequest/@subaction}&amp;c={$collName}&amp;s={/page/pageResponse/service/@name}&amp;rt=rd&amp;{$startPageName}={$currentPage - 1}&amp;qs={/page/pageRequest/paramList/param[@name='qs']/@value}">
						  <xsl:call-template name="previousArrowImage"/>
						</a>
					</xsl:if>
				</td>
				<td id="prevTD">
					<xsl:if test="$currentPage != 1">
						<a href="{$library_name}?a=q&amp;sa={/page/pageRequest/@subaction}&amp;c={$collName}&amp;s={/page/pageResponse/service/@name}&amp;rt=rd&amp;{$startPageName}={$currentPage - 1}&amp;qs={/page/pageRequest/paramList/param[@name='qs']/@value}"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.results.previous')"/></a>
					</xsl:if>
				</td>

				<xsl:if test="$display_status_bar != 'false'">
				<!-- Search result status bar (in english it reads "Displaying X to Y of Z documents") -->
				<xsl:if test="$docsPerPage &gt; 0">
					<xsl:variable name="startdoc" select="($currentPage - 1) * $docsPerPage + 1"/>
					<xsl:variable name="enddoc">
						<xsl:choose>
							<xsl:when test="($currentPage * $docsPerPage + 1) &gt; $docMax">
								<xsl:value-of select="$docMax"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$currentPage * $docsPerPage"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<xsl:variable name="levelString">
					  <xsl:choose>
					    <xsl:when test="$level = 'Doc'">
					      <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.document_plural')"/>
					    </xsl:when>
					    <xsl:otherwise>
					      <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.section_plural')"/>
					    </xsl:otherwise>
					  </xsl:choose>
					</xsl:variable>
					<td id="searchResultsStatusBar">
					        <xsl:variable name="gitArgs" select="concat($startdoc,';', $enddoc, ';', $docMax, ';', $levelString)"/>
						<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.displayingnumdocs', $gitArgs)"/>
					</td>
				</xsl:if>
				</xsl:if>
				<!-- Next button -->
				<td id="nextTD">
					<xsl:if test="$docsPerPage &gt; 0 and ($currentPage * $docsPerPage + 1) &lt; $docMax">
						<a href="{$library_name}?a=q&amp;sa={/page/pageRequest/paramList/param[@name = 'sa']/@value}&amp;c={$collName}&amp;s={/page/pageResponse/service/@name}&amp;rt=rd&amp;{$startPageName}={$currentPage + 1}&amp;qs={/page/pageRequest/paramList/param[@name='qs']/@value}"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.results.next')"/></a>
					</xsl:if>
				</td>
				<td id="nextArrowTD">
					<xsl:if test="$docsPerPage &gt; 0 and ($currentPage * $docsPerPage + 1) &lt; $docMax">
						<a href="{$library_name}?a=q&amp;sa={/page/pageRequest/paramList/param[@name = 'sa']/@value}&amp;c={$collName}&amp;s={/page/pageResponse/service/@name}&amp;rt=rd&amp;{$startPageName}={$currentPage + 1}&amp;qs={/page/pageRequest/paramList/param[@name='qs']/@value}">
							<xsl:call-template name="nextArrowImage"/>
						</a>
					</xsl:if>
				</td>
			</tr>
		</table>
		</xsl:if>
	</xsl:template>

	<xsl:template name="previousArrowImage">
	  <img src="interfaces/default/images/previous.png"/>
	</xsl:template>
	<xsl:template name="nextArrowImage">
	  <img src="interfaces/default/images/next.png"/>
	</xsl:template>
	<!-- puts all the params into a=p&p=h type form - need to change this if use 
	multi params  -->
	<xsl:template match="paramList" mode="cgi">
		<xsl:param name="ns">s1.</xsl:param>
		<xsl:for-each select="param">
			<xsl:variable name='pname' select="@name"/>
			<xsl:text>&amp;</xsl:text>
			<xsl:value-of select="$ns"/>
			<xsl:value-of select="@name"/>=<xsl:apply-templates select="." mode="calculate-default">
				<xsl:with-param name='ns' select='$ns'/>
			</xsl:apply-templates>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="resultsPagePre">
		<!-- OVERWRITE TO INSERT CONTENT BEFORE THE RESULTS PAGE -->
		<xsl:if test="/page/pageResponse/format[@type='display' or @type='browse' or @type='search']/gsf:option[@name='mapEnabled']/@value = 'true'">
			<xsl:call-template name="mapFeaturesJSONNodes"/>
			<xsl:call-template name="mapFeaturesMap"/>
		</xsl:if>
	</xsl:template>

	<xsl:template name="resultsPagePost">
		<!-- OVERWRITE TO INSERT CONTENT AFTER THE RESULTS PAGE -->
	</xsl:template>

	<xsl:template name="documentNodePre">
		<!-- OVERWRITE TO INSERT CONTENT BEFORE EVERY DOCUMENT NODE -->
	</xsl:template>

	<xsl:template name="documentNodePost">
		<!-- OVERWRITE TO INSERT CONTENT AFTER EVERY DOCUMENT NODE -->
		<xsl:if test="/page/pageResponse/format[@type='display' or @type='browse' or @type='search']/gsf:option[@name='mapEnabled']/@value = 'true'">
			<xsl:call-template name="mapFeaturesIcon"/>
		</xsl:if>

		<xsl:if test="/page/pageResponse/format/gsf:option[@name='panoramaViewerEnabled']/@value = 'true'">
		  <xsl:if test=" metadataList/metadata[@name = 'Latitude'] and metadataList/metadata[@name = 'Longitude']">
                    <xsl:call-template name="panoramaViewerFeaturesIcon"/>
                  </xsl:if>
		</xsl:if>

	</xsl:template>



</xsl:stylesheet>
