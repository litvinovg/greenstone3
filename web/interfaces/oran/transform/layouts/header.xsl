<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util">
		
	<xsl:include href="../query-common.xsl"/>
	
	<!-- If the c parameter is empty then use the p.c parameter for the collection name-->
	<xsl:variable name="collNameChecked">
		<xsl:choose>
		<xsl:when test="$collName = '' and /page/pageRequest/paramList/param[@name='p.c']/@value">
			<xsl:value-of select="/page/pageRequest/paramList/param[@name='p.c']/@value"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="$collName"/>
		</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	
	<!-- Creates a header for -->
	<xsl:template name="create-html-header">
		<title><xsl:call-template name="pageTitle"/> :: <xsl:call-template name="siteName"/></title>
		<link rel="stylesheet" href="interfaces/{$interface_name}/style/core.css" type="text/css"/>
		<link rel="shortcut icon" href="favicon.ico"/> 
	</xsl:template>
		
	<!-- ***** HEADER LAYOUT TEMPLATE ***** -->
	<xsl:template name="create-banner">
		<div id="breadcrumbs"><xsl:call-template name="breadcrumbs"/><xsl:text> </xsl:text></div>
		<table id="titlesearchcontainer">
			<tr>
				<xsl:call-template name="page-title-area"/>
				<xsl:call-template name="quick-search-area"/>
			</tr>
		</table>
		<xsl:call-template name="home-help-preferences"/>
		<xsl:call-template name="browsing-tabs"/>
	</xsl:template>
	
	<!-- ***** BROWSING TABS ***** -->
	<xsl:template name="browsing-tabs">
		<xsl:if test="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service">
			<ul id="nav">
				<!-- If this collection has a ClassifierBrowse service then add a tab for each classifier-->
				<xsl:if test="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@type='browse' and @name='ClassifierBrowse']">
					<!-- Loop through each classifier -->
					<xsl:for-each select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='ClassifierBrowse']/classifierList/classifier">
						<xsl:element name="li">
							<!-- If this tab is selected then colour it differently (right part) -->
							<xsl:if test="@name = /page/pageRequest/paramList/param[@name = 'cl' and /page/pageRequest/@action = 'b']/@value">
								<xsl:attribute name='style'>background: transparent url('interfaces/oran/images/tab-right-selected.png') scroll no-repeat 100% -100px;</xsl:attribute>
							</xsl:if>
							
							<xsl:element name="a">
								<!-- If this tab is selected then colour it differently (left part) -->
								<xsl:if test="@name = /page/pageRequest/paramList/param[@name = 'cl' and /page/pageRequest/@action = 'b']/@value">
									<xsl:attribute name='style'>background: transparent url('interfaces/oran/images/tab-left-selected.png') no-repeat scroll 0 -100px;</xsl:attribute>
								</xsl:if>
								
								<!-- Add a title element to the <a> tag if a description exists for this classifier -->
								<xsl:if test="displayItem[@name='description']">
									<xsl:attribute name='title'><xsl:value-of select="displayItem[@name='description']"/></xsl:attribute>
								</xsl:if>
								
								<!-- Add the href element to the <a> tag -->
								<xsl:choose>
									<xsl:when test="@name">
										<xsl:attribute name="href"><xsl:value-of select="$library_name"/>?a=b&amp;rt=s&amp;s=ClassifierBrowse&amp;c=<xsl:value-of select="/page/pageResponse/collection[@name=$collNameChecked]/@name"/>&amp;cl=<xsl:value-of select="@name"/></xsl:attribute>
									</xsl:when>
									<xsl:otherwise>
										<xsl:attribute name="href"><xsl:value-of select="$library_name"/>?a=b&amp;rt=d&amp;s=ClassifierBrowse&amp;c=<xsl:value-of select="/page/pageResponse/collection[@name=$collNameChecked]/@name"/></xsl:attribute>
									</xsl:otherwise>
								</xsl:choose>
								
								<!-- Add the actual text of the <a> tag -->
								<xsl:value-of select="displayItem[@name='name']"/>
							</xsl:element>
						</xsl:element>
					</xsl:for-each>
				</xsl:if>

				<!-- all other services -->
				<xsl:for-each select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[not(@type='query') and not(@type='browse')]">
					<xsl:call-template name="navigationTab"/>
				</xsl:for-each>
			</ul>
		</xsl:if>
	</xsl:template>
	
	<!-- ***** HOME HELP PREFERENCES ***** -->
	<xsl:template name="home-help-preferences">
		<xsl:if test="/page/pageResponse/collection">
			<ul id="bannerLinks">

				<!-- preferences -->
				<li>
					<a href="{$library_name}?a=p&amp;amp;sa=pref&amp;amp;c={$collNameChecked}">
						<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_tip')"/></xsl:attribute>
						<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_b')"/>
					</a>
				</li>

				<!-- help -->
				<li>
					<a href="{$library_name}?a=p&amp;amp;sa=help&amp;amp;c={$collNameChecked}">
						<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_tip')"/></xsl:attribute>
						<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_b')"/>
					</a>
				</li>

				<!-- home -->
				<li>
					<a href="{$library_name}?a=p&amp;amp;sa=home">
						<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home_tip')"/></xsl:attribute>
						<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home_b')"/>
					</a>
				</li>
			</ul>
		</xsl:if>
	</xsl:template>
	
	<!-- ***** PAGE TITLE ***** -->
	<xsl:template name="page-title-area">
		<xsl:variable name="pageTitleVar"><xsl:call-template name="pageTitle"/></xsl:variable>
		<td id="titlearea">
			<h2>
				<!-- Resize the title based on how long it is -->
				<xsl:attribute name="style">
					<xsl:choose>
						<xsl:when test="string-length($pageTitleVar) &lt; 20">
							font-size: 24px; line-height: 26px;
						</xsl:when>
						<xsl:when test="string-length($pageTitleVar) &lt; 30">
							font-size: 22px; line-height: 24px;
						</xsl:when>
						<xsl:when test="string-length($pageTitleVar) &lt; 40">
							font-size: 20px; line-height: 22px;
						</xsl:when>
						<xsl:when test="string-length($pageTitleVar) &lt; 50">
							font-size: 18px; line-height: 20px;
						</xsl:when>
						<xsl:when test="string-length($pageTitleVar) &lt; 60">
							font-size: 16px; line-height: 18px;
						</xsl:when>
						<xsl:when test="string-length($pageTitleVar) &lt; 70">
							font-size: 15px; line-height: 17px;
						</xsl:when>
						<xsl:when test="string-length($pageTitleVar) &lt; 80">
							font-size: 14px; line-height: 16px;
						</xsl:when>
						<xsl:when test="string-length($pageTitleVar) &lt; 90">
							font-size: 13px; line-height: 15px;
						</xsl:when>
						<xsl:otherwise>
							font-size: 12px; line-height: 14px;
						</xsl:otherwise>
					</xsl:choose>
				</xsl:attribute>
				<!--<xsl:value-of select="string-length($pageTitleVar)" />-->
				<xsl:value-of select="$pageTitleVar" />
			</h2>
		</td>
	</xsl:template>
	
	
	<!-- ***** QUICK SEARCH AREA ***** -->
	<xsl:template name="quick-search-area">
		<xsl:if test="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='TextQuery']">
			<xsl:variable name="subaction" select="/page/pageRequest/@subaction"/>
			<td id="quicksearcharea">
				<form>
					<input type="hidden" name="a" value="q"/>
					<input type="hidden" name="sa" value="{$subaction}"/>
					<input type="hidden" name="rt" value="rd"/>
					<input type="hidden" name="s" value="TextQuery"/>
					<input type="hidden" name="c" value="{$collNameChecked}"/>
					<input type="hidden" name="startPage" value="1"/>
					<!-- The query text box -->
					<span class="querybox">
						<xsl:variable name="qs">
							<xsl:apply-templates select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='TextQuery']/paramList/param[@name='query']" mode="calculate-default"/>
						</xsl:variable>
						<nobr>
							<xsl:apply-templates select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='TextQuery']/paramList/param[@name='query']">
								<xsl:with-param name="default" select="java:org.greenstone.gsdl3.util.XSLTUtil.tidyWhitespace($qs)"/>
							</xsl:apply-templates>
						</nobr>
					</span>
					<!-- The index selection list -->
					<xsl:if test="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='TextQuery']/paramList/param[@name='index']/@type = 'enum_single'">
						<span class="textselect">
							<xsl:apply-templates select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='TextQuery']/paramList/param[@name='index']">
								<xsl:with-param name="default">
									<xsl:apply-templates select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='TextQuery']/paramList/param[@name='index']" mode="calculate-default"/>
								</xsl:with-param>
							</xsl:apply-templates>
						</span>
					</xsl:if>
					<!-- The submit button -->
					<input type="submit">
						<xsl:attribute name="value">
							<xsl:value-of select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='TextQuery']/displayItem[@name='submit']"/>
						</xsl:attribute>
					</input>
					<br/>
					<!-- The list of other search types -->
					<table>
						<tr>
							<xsl:for-each select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@type='query']">
								<td>
									<a>
										<xsl:attribute name="href">
											?a=q&amp;rt=d&amp;c=<xsl:value-of select="$collNameChecked"/>&amp;s=<xsl:value-of select="@name"/>
										</xsl:attribute>
										<xsl:value-of select="displayItem[@name='name']"/>
									</a>
								</td>
							</xsl:for-each>
						</tr>
					</table>
				</form>
			</td>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>