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

	<!-- set page title -->
	<xsl:template name="pageTitle"><gslib:serviceName/></xsl:template>

	<!-- set page breadcrumbs -->
	<xsl:template name="breadcrumbs"> <gslib:siteLink/><gslib:rightArrow/><xsl:if test="/page/pageResponse/collection"> <gslib:collectionNameLinked/><gslib:rightArrow/></xsl:if></xsl:template>

	<!-- the page content -->
	<xsl:template match="/page">
		<xsl:call-template name="queryPage"/>
		<xsl:call-template name="resultsPage"/>
	</xsl:template>
	
	<xsl:template name="resultsPage">
		<xsl:call-template name="resultsPagePre"/>
		<xsl:call-template name="displayTermInfo"/>
		<xsl:call-template name="displayResults"/>
		<xsl:call-template name="resultsPagePost"/>
	</xsl:template>
	
	<xsl:template name="displayResults">
		<xsl:if test="/page/pageResponse/facetList/facet">
			<script type="text/javascript" src="interfaces/{$interface_name}/js/facet-scripts.js"><xsl:text> </xsl:text></script>
			<div id="facetSelector">
				<xsl:for-each select="/page/pageResponse/facetList/facet">
					<xsl:if test="count(count) > 0">
						<ul class="facetTable ui-widget-content" indexName="{@name}">
							<xsl:variable name="serviceName"><xsl:value-of select="/page/pageRequest/paramList/param[@name = 's']/@value"/></xsl:variable>
							<xsl:variable name="indexShortName"><xsl:value-of select="@name"/></xsl:variable>
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
										<xsl:if test="position() > $countSize">display:none;<xsl:value-of select="$indexShortName"/></xsl:if>
									</xsl:attribute>
									<input type="checkbox" onclick="performRefinedSearch();"/><span><xsl:value-of select="@name"/></span>(<xsl:value-of select="."/>)
								</li>
							</xsl:for-each>
							<xsl:if test="count(count) > $countSize">
								<li class="expandCollapseFacetList{$indexShortName}"><a class="expandCollapseFacetListLink{$indexShortName}" href="javascript:expandFacetList('{$indexShortName}', {$countSize});">See more...</a></li>
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
					<tr class="document">
						<xsl:apply-templates select="."/>
						<xsl:call-template name="documentNodePost"/>
					</tr>
				</xsl:for-each>
				<br/>
			</table>
			
			<!-- Previous/Next buttons-->
			<xsl:call-template name="prevNextButtons"/>
		</div>
		<xsl:if test="/page/pageResponse/facetList/facet">
			<div style="clear:both;"><xsl:text> </xsl:text></div>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="displayTermInfo">
		
		<!-- Find the number of documents displayed per page -->
		<xsl:variable name="level">
			<xsl:choose>
				<xsl:when test="/page/pageRequest/paramList/param[@name='level']">
					<xsl:value-of select="/page/pageRequest/paramList/param[@name='level']/@value" />
				</xsl:when>
				<xsl:when test="/page/pageRequest/paramList/param[@name='s1.level']">
					<xsl:value-of select="/page/pageRequest/paramList/param[@name='s1.level']/@value" />
				</xsl:when>
				<xsl:otherwise>Doc</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
	
		<!-- The list of search terms with their frequency and document count -->
		<p class="termList">
			<xsl:if test="count(/page/pageResponse/termList/stopword) &gt; 0">
				<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.common')"/><xsl:text> </xsl:text>
			</xsl:if>
			
			<xsl:for-each select="/page/pageResponse/termList/stopword">
				<span style="font-style:italic;"><xsl:value-of select="@name"/></span><xsl:text> </xsl:text>
			</xsl:for-each>
			<br /><br />
		
			<xsl:for-each select="/page/pageResponse/termList/term">
				<xsl:choose>
					<!-- If there is only one or two search terms then show the expanded information -->
					<xsl:when test="count(/page/pageResponse/termList/term) &lt; 3">
						<span style="font-style:italic;"><xsl:value-of select="@name"/></span>
						<xsl:text> </xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.occurs')"/><xsl:text> </xsl:text>
						<xsl:value-of select="@freq"/>
						<xsl:choose>
							<xsl:when test="@freq = 1">
								<xsl:text> </xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.time')"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:text> </xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.time_plural')"/>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:text> </xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.in')"/><xsl:text> </xsl:text>
						<xsl:value-of select="@numDocsMatch"/>
						<xsl:choose>
							<xsl:when test="@numDocsMatch = 1">
								<xsl:text> </xsl:text>
								<xsl:choose>
									<xsl:when test="$level = 'Doc'">
										<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.document')"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.section')"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:otherwise>
								<xsl:text> </xsl:text>
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
						<br />
					</xsl:when>
					<xsl:otherwise>
						<span style="font-style:italic;"><xsl:value-of select="@name"/></span> (<xsl:value-of select="@freq"/>)
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</p>
	</xsl:template>
	
	<xsl:template name="queryPage">
		<xsl:for-each select="pageResponse/service">
			<form name="QueryForm" method="get" action="{$library_name}/collection/{$collName}/search/{@name}">
				<div>
					<input type="hidden" name="a" value="q"/>
					<input type="hidden" name="sa"><xsl:attribute name="value"><xsl:value-of select="/page/pageRequest/@subaction"/></xsl:attribute></input>
					<input type="hidden" name="rt" value="rd"/>
					<xsl:choose>
						<xsl:when test="/page/pageRequest/paramList/param[@name = 's1.maxDocs']">
							<input type="hidden" name="s1.maxDocs">
								<xsl:attribute name="value">
									<xsl:value-of select="/page/pageRequest/paramList/param[@name = 's1.maxDocs']/@value"/>
								</xsl:attribute>
							</input>
						</xsl:when>
						<xsl:otherwise>
							<input type="hidden" name="s1.maxDocs" value="100"/>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:choose>
						<xsl:when test="/page/pageRequest/paramList/param[@name = 's1.hitsPerPage']">
							<input type="hidden" name="s1.hitsPerPage">
								<xsl:attribute name="value">
									<xsl:value-of select="/page/pageRequest/paramList/param[@name = 's1.hitsPerPage']/@value"/>
								</xsl:attribute>
							</input>
						</xsl:when>
						<xsl:otherwise>
							<input type="hidden" name="s1.hitsPerPage" value="20"/>
						</xsl:otherwise>
					</xsl:choose>

					<xsl:variable name="ns">s1.</xsl:variable>
					<xsl:for-each select="paramList/param">
						<xsl:choose>
							<xsl:when test="@name='case' or @name='stem' or @name='accent'">
							</xsl:when>
							<xsl:when test="@type='multi'">
								<xsl:apply-templates select="."><xsl:with-param name="ns" select="$ns"/></xsl:apply-templates>
							</xsl:when>
							<xsl:when test="@name = 'maxDocs' or @name = 'hitsPerPage'"></xsl:when>
							<xsl:otherwise>
								<xsl:variable name="pvalue"><xsl:apply-templates select="." mode="calculate-default"><xsl:with-param name="ns" select="$ns"/></xsl:apply-templates></xsl:variable>
								<div class="paramLabel"><xsl:value-of select="displayItem[@name='name']"/></div>
								<div class="paramValue">
									<xsl:apply-templates select=".">
										<xsl:with-param name="default" select="$pvalue"/>
										<xsl:with-param name="ns" select="$ns"/>
									</xsl:apply-templates>
								</div>
								<br class="clear"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
					<br/>
					<input type="submit"><xsl:attribute name="value"><xsl:value-of select="displayItem[@name='submit']"/></xsl:attribute></input>
				</div>
			</form>
		</xsl:for-each>
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
	
		<!-- Find the total number of documents returned -->
		<xsl:variable name="docMax">
			<xsl:choose>
				<xsl:when test="/page/pageResponse/metadataList/metadata[@name = 'numDocsReturned']">
					<xsl:value-of select="/page/pageResponse/metadataList/metadata[@name = 'numDocsReturned']"/>
				</xsl:when>
				<xsl:when test="/page/pageResponse/metadataList/metadata[@name = 'numDocsMatched']">
					<xsl:value-of select="/page/pageResponse/metadataList/metadata[@name = 'numDocsMatched']"/>
				</xsl:when>
				<xsl:otherwise><xsl:value-of select="count(/page/pageResponse/documentNodeList/documentNode)"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<!-- Find the number of documents displayed per page -->
		<xsl:variable name="docsPerPage">
			<xsl:choose>
				<xsl:when test="/page/pageRequest/paramList/param[@name='hitsPerPage']">
					<xsl:value-of select="/page/pageRequest/paramList/param[@name='hitsPerPage']/@value" />
				</xsl:when>
				<xsl:when test="/page/pageRequest/paramList/param[@name='s1.hitsPerPage']">
					<xsl:value-of select="/page/pageRequest/paramList/param[@name='s1.hitsPerPage']/@value" />
				</xsl:when>
				<xsl:otherwise>20</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<!-- Find the number of documents displayed per page -->
		<xsl:variable name="level">
			<xsl:choose>
				<xsl:when test="/page/pageRequest/paramList/param[@name='level']">
					<xsl:value-of select="/page/pageRequest/paramList/param[@name='level']/@value" />
				</xsl:when>
				<xsl:when test="/page/pageRequest/paramList/param[@name='s1.level']">
					<xsl:value-of select="/page/pageRequest/paramList/param[@name='s1.level']/@value" />
				</xsl:when>
				<xsl:otherwise>Doc</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<table id="searchResultNavTable"><tr>
			<xsl:variable name="startPageName"><xsl:if test="$usesS1 = 'true'">s1.</xsl:if>startPage</xsl:variable>
			
			<!-- Previous button -->
			<td id="prevArrowTD">
				<xsl:if test="$currentPage != 1">
					<a href="{$library_name}?a=q&amp;sa={/page/pageRequest/@subaction}&amp;c={$collName}&amp;s={/page/pageResponse/service/@name}&amp;rt=rd&amp;{$startPageName}={$currentPage - 1}">
						<img src="interfaces/default/images/previous.png"/>
					</a>
				</xsl:if>
			</td>
			<td id="prevTD">
				<xsl:if test="$currentPage != 1">
					<a href="{$library_name}?a=q&amp;sa={/page/pageRequest/@subaction}&amp;c={$collName}&amp;s={/page/pageResponse/service/@name}&amp;rt=rd&amp;{$startPageName}={$currentPage - 1}">Previous</a>
				</xsl:if>
			</td>
			
			<!-- Search result status bar (in english it reads "Displaying X to Y of Z documents") -->
			<xsl:if test="$docMax &gt; 0">
				<td id="searchResultsStatusBar">
					<!-- "Displaying" -->
					<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.displaying')"/><xsl:text> </xsl:text>
					<!-- "X" -->
					<xsl:value-of select="($currentPage - 1) * $docsPerPage + 1"/>
					<!-- "to" -->
					<xsl:text> </xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.to')"/><xsl:text> </xsl:text>
					<!-- "Y" -->
					<xsl:choose>
						<xsl:when test="($currentPage * $docsPerPage + 1) &gt; $docMax">
							<xsl:value-of select="$docMax"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$currentPage * $docsPerPage"/>
						</xsl:otherwise>
					</xsl:choose>
					<!-- "of" -->
					<xsl:text> </xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.of')"/><xsl:text> </xsl:text>
					<!-- "Z" -->
					<xsl:value-of select="$docMax"/>
					<!-- "document[s]/section[s]"-->
					<xsl:choose>
						<xsl:when test="$docMax = 1">
							<xsl:text> </xsl:text>
							<xsl:choose>
								<xsl:when test="$level = 'Doc'">
									<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.document')"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.section')"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text> </xsl:text>
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
				</td>
			</xsl:if>
			
			<!-- Next button -->
			<td id="nextTD">
				<xsl:if test="($currentPage * $docsPerPage + 1) &lt; $docMax">
					<a href="{$library_name}?a=q&amp;sa={/page/pageRequest/paramList/param[@name = 'sa']/@value}&amp;c={$collName}&amp;s={/page/pageResponse/service/@name}&amp;rt=rd&amp;{$startPageName}={$currentPage + 1}">Next</a>
				</xsl:if>
			</td>
			<td id="nextArrowTD">
				<xsl:if test="($currentPage * $docsPerPage + 1) &lt; $docMax">
					<a href="{$library_name}?a=q&amp;sa={/page/pageRequest/paramList/param[@name = 'sa']/@value}&amp;c={$collName}&amp;s={/page/pageResponse/service/@name}&amp;rt=rd&amp;{$startPageName}={$currentPage + 1}">
						<img src="interfaces/default/images/next.png"/>
					</a>
				</xsl:if>
			</td>
		</tr></table>
	</xsl:template>

	<!-- puts all the params into a=p&p=h type form - need to change this if use 
	multi params  -->
	<xsl:template match="paramList" mode="cgi">
		<xsl:param name="ns">s1.</xsl:param>
		<xsl:for-each select="param">
			<xsl:variable name='pname' select="@name"/>
			<xsl:text>&amp;</xsl:text><xsl:value-of select="$ns"/><xsl:value-of select="@name"/>=<xsl:apply-templates select="." mode="calculate-default"><xsl:with-param name='ns' select='$ns'/></xsl:apply-templates>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="param" mode="calculate-default">
		<xsl:param name="ns">s1.</xsl:param>
		<xsl:variable name="pname"><xsl:value-of select="$ns"/><xsl:value-of select="@name"/></xsl:variable>
		<xsl:choose>
			<xsl:when test="/page/pageRequest/paramList/param[@name=$pname]">
				<xsl:choose>
					<xsl:when test="@type='enum_multi'"><xsl:text>,</xsl:text>
						<xsl:for-each select="/page/pageRequest/paramList/param[@name=$pname]">
							<xsl:value-of select="@value"/>,
						</xsl:for-each>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="/page/pageRequest/paramList/param[@name=$pname]/@value"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="@default"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- invisible params - used by other stuff. in the query form, we set to the default -->
	<xsl:template match="param[@type='invisible']">
		<xsl:param name="ns">s1.</xsl:param>
		<input type='hidden' name='{$ns}{@name}' value='{@default}'/>
	</xsl:template>

	<!-- boolean params -->
	<xsl:template match="param[@type='boolean']">
		<xsl:param name="ns">s1.</xsl:param>
		<xsl:param name="default"/>
		<select name='{$ns}{@name}'>
			<option value="0"><xsl:if test="$default='0'"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="option[@name='0']/displayItem[@name='name']"/></option>
			<option value="1"><xsl:if test="$default='1'"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="option[@name='1']/displayItem[@name='name']"/></option>
		</select>
	</xsl:template>

	<!-- integer params -->
	<xsl:template match="param[@type='integer']">
		<xsl:param name="ns">s1.</xsl:param>
		<xsl:param name="default"/>
		<input type="text" name="{$ns}{@name}" size="3" value="{$default}"/>
	</xsl:template>

	<!-- single selection enum params -->
	<xsl:template match="param[@type='enum_single']">
		<xsl:param name="ns">s1.</xsl:param>
		<xsl:param name="default"/>
		<xsl:choose>
			<xsl:when test="count(option) = 1">
				<xsl:value-of select="option/displayItem[@name='name']"/>
				<input type='hidden' name='{$ns}{@name}'><xsl:attribute name='value'><xsl:value-of  select='option/@name'/></xsl:attribute></input>
			</xsl:when>
			<xsl:otherwise>
				<select name="{$ns}{@name}">
					<xsl:for-each select="option">
						<option value="{@name}"><xsl:if test="@name=$default"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="displayItem[@name='name']"/></option>
					</xsl:for-each>
				</select>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


	<!-- multiple selection enum params -->
	<!-- how to do defaults for this?? -->
	<xsl:template match="param[@type='enum_multi']">
		<xsl:param name="ns">s1.</xsl:param>
		<xsl:param name="default"/>
		<select name="{$ns}{@name}" size="2">
			<xsl:attribute name="multiple"></xsl:attribute>
			<xsl:for-each select="option">
				<option value="{@name}"><xsl:if test="contains($default, concat(',', @name, ','))"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="displayItem[@name='name']"/></option>
			</xsl:for-each>
		</select>
	</xsl:template>

	<!-- string params -->
	<xsl:template match="param[@type='string']">
		<xsl:param name="ns">s1.</xsl:param>
		<xsl:param name="default"/>
		<input type="text" name="{$ns}{@name}" size="30" value="{$default}"/>
	</xsl:template>

	<!-- large string  params -->
	<xsl:template match="param[@type='text']">
		<xsl:param name="ns">s1.</xsl:param>
		<xsl:param name="default"/>
		<textarea name="{$ns}{@name}" cols="50" rows="3"><xsl:value-of select='$default'/></textarea>
	</xsl:template>

	<!-- multi params - params that are combinations of other params -->
	<xsl:template match="param[@type='multi']">
		<xsl:param name="ns">s1.</xsl:param>
		<xsl:variable name="parent" select="@name"/>

		<table>
			<tr class="queryfieldheading">
				<xsl:value-of select="displayItem[@name='name']"/>
				<xsl:for-each select="param">
					<td class="queryfieldname"><xsl:value-of select="displayItem[@name='name']"/></td>
				</xsl:for-each>
			</tr>
			<xsl:apply-templates select="." mode="contents">
				<xsl:with-param name="occurs" select="@occurs"/>
				<xsl:with-param name="ns" select="$ns"/>
			</xsl:apply-templates>
		</table>

	</xsl:template>
	
	<xsl:template match="param[@type = 'checkbox_list']">
		<xsl:param name="ns">s1.</xsl:param>
		<ul class="checkboxList">
			<xsl:for-each select="option">
				<li><input type="checkbox" name="{$ns}{../@name}" value="{@name}"/><xsl:value-of select="displayItem"/></li>
			</xsl:for-each>
		</ul>
	</xsl:template>

	<xsl:template match="param[@type='multi']" mode="contents">
		<xsl:param name="ns">s1.</xsl:param>
		<xsl:param name="occurs">1</xsl:param>
		<xsl:variable name="pos" select="@occurs - $occurs"/>	
		<tr class="queryfieldrow">
			<xsl:for-each select="param">
				<xsl:variable name="pname" select="@name"/>
				<xsl:variable name="values" select="/page/pageRequest/paramList/param[@name=$pname]/@value"/>
				<td class="queryfieldcell">
					<xsl:choose>
						<xsl:when test="not(@ignore) or  @ignore != $pos">      
							<xsl:apply-templates select="."><xsl:with-param name="default" select="java:org.greenstone.gsdl3.util.XSLTUtil.getNumberedItem($values, $pos)"/><xsl:with-param name="ns" select="$ns"/></xsl:apply-templates>
						</xsl:when>
						<xsl:otherwise><!-- put in a hidden placeholder -->
							<input type="hidden" name='{$ns}{@name}' value=''/>
						</xsl:otherwise>
					</xsl:choose>
				</td>
			</xsl:for-each>
		</tr>

		<!-- recursively call this template to get multiple entries -->
		<xsl:if test="$occurs &gt; 1">
			<xsl:apply-templates select="." mode="contents"><xsl:with-param name="occurs" select="$occurs - 1"/><xsl:with-param name="ns" select="$ns"/></xsl:apply-templates>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="resultsPagePre">
		<!-- OVERWRITE TO INSERT CONTENT BEFORE THE RESULTS PAGE -->
		<xsl:if test="/page/pageResponse/format[@type='display' or @type='browse' or @type='search']/gsf:option[@name='mapEnabled']/@value = 'true'">
			<xsl:call-template name="mapFeaturesJSONNodes"/>
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
	</xsl:template>
	
	<xsl:template name="mapFeaturesJSONNodes">
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
		
		<div id="map_canvas" style="margin:0px auto; width:450px; height:500px; float:right;"><xsl:text> </xsl:text></div>
		
		<gsf:metadata name="Latitude"/>
		<gsf:metadata name="Longitude"/>
	</xsl:template>
	
	<xsl:template name="mapFeaturesIcon">
		<td style="padding-left:5px; padding-right:5px;" valign="top">
			<a href="javascript:focusDocument('{@nodeID}');"><img src="interfaces/{$interface_name}/images/bluemarker.png"/></a>
		</td>
	</xsl:template>
</xsl:stylesheet>
