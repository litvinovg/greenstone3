<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util gsf">
		
	<xsl:include href="../query-common.xsl"/>
	<xsl:include href="../javascript-global-setup.xsl"/>
	
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
	
	<!-- Creates a header for the html page -->
	<xsl:template name="create-html-header">
		<base>
			<xsl:attribute name="href">
				<xsl:choose>
					<xsl:when test="/page/pageResponse/metadataList/metadata[@name = 'baseURL']">
						<xsl:value-of select="/page/pageResponse/metadataList/metadata[@name = 'baseURL']"/>
					</xsl:when>
					<xsl:when test="/page/pageRequest/@baseURL">
						<xsl:value-of select="/page/pageRequest/@baseURL"/>
					</xsl:when>
				</xsl:choose>
			</xsl:attribute>
		</base>
		<xsl:comment>[if lte IE 6]&gt;&lt;/base&gt;&lt;![endif]</xsl:comment>
	
		<title><xsl:call-template name="pageTitle"/> :: <xsl:call-template name="siteName"/></title>
		
		<xsl:choose>
			<xsl:when test="/page/pageResponse/interfaceOptions/option[@name = 'cssTheme']/@value">
				<!-- Get the theme from the interfaceConfig.xml file -->
				<link rel="stylesheet" href="{/page/pageResponse/interfaceOptions/option[@name = 'cssTheme']/@value}" type="text/css"/>
			</xsl:when>
			<xsl:otherwise>
				<link rel="stylesheet" href="interfaces/{$interface_name}/style/themes/main/jquery-ui-1.8.16.custom.css" type="text/css"/>
			</xsl:otherwise>
		</xsl:choose>
		<link rel="stylesheet" href="interfaces/{$interface_name}/style/core.css" type="text/css"/>
		<link rel="shortcut icon" href="favicon.ico"/> 
		
		<script type="text/javascript" src="interfaces/{$interface_name}/js/jquery.min.js"><xsl:text> </xsl:text></script>
		<script type="text/javascript" src="interfaces/{$interface_name}/js/jquery-ui.min.js"><xsl:text> </xsl:text></script>
		<script type="text/javascript" src="interfaces/{$interface_name}/js/jquery.themeswitcher.min.js"><xsl:text> </xsl:text></script>
		
		<script type="text/javascript" src="interfaces/{$interface_name}/js/direct-edit.js"><xsl:text> </xsl:text></script>
		<script type="text/javascript" src="interfaces/{$interface_name}/js/zoomer.js"><xsl:text> </xsl:text></script>

		<xsl:if test="/page/pageResponse/format[@type='display' or @type='browse' or @type='search']/gsf:option[@name='mapEnabled']/@value = 'true'">
			<xsl:call-template name="map-scripts"/>
		</xsl:if>
		
		<xsl:if test="/page/pageRequest/userInformation and (util:contains(/page/pageRequest/userInformation/@groups, 'administrator') or util:contains(/page/pageRequest/userInformation/@groups, 'all-collections-editor') or util:contains(/page/pageRequest/userInformation/@groups, $thisCollectionEditor))">
			<xsl:call-template name="init-direct-edit"/>
		</xsl:if>
		<xsl:call-template name="setup-gs-variable"/>
		<xsl:call-template name="additionalHeaderContent"/>
	</xsl:template>
	
	<xsl:template name="additionalHeaderContent">
		<!-- This template should be overridden in the collectionConfig.xml file if you want to add extra header content -->
	</xsl:template>
	
	<xsl:template name="init-direct-edit">
		<script type="text/javascript">
			<xsl:text disable-output-escaping="yes">
				de.onready(function() 
				{
					try 
					{
						de.init();
					}
					catch (err) 
					{
						alert("Seaweed failed to initialise: " + err.message);
					}
				});
			</xsl:text>
		</script>
	</xsl:template>
		
	<!-- ***** HEADER LAYOUT TEMPLATE ***** -->
	<xsl:template name="create-banner">		
		<div id="gs_banner" class="ui-widget-header ui-corner-bottom">
			<table id="titlesearchcontainer">
				<tr>
					<xsl:call-template name="page-title-area"/>
					<xsl:call-template name="quick-search-area"/>
				</tr>
			</table>
			<xsl:call-template name="browsing-tabs"/>
		</div>
	</xsl:template>
	
	<!-- ***** BROWSING TABS ***** -->
	<xsl:template name="browsing-tabs">
		<xsl:if test="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service">
			<ul id="nav">
				<!-- If this collection has a ClassifierBrowse service then add a tab for each classifier-->
				<xsl:if test="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@type='browse' and @name='ClassifierBrowse']">
					<!-- Loop through each classifier -->
					<xsl:for-each select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='ClassifierBrowse']/classifierList/classifier">
						<li>
							<xsl:choose>
								<!-- If this tab is selected then colour it differently -->
								<xsl:when test="util:contains(/page/pageRequest/paramList/param[@name = 'cl' and /page/pageRequest/@action = 'b']/@value, @name)">
									<xsl:attribute name='class'>ui-state-default ui-corner-top ui-tabs-selected ui-state-active</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute name='class'>ui-state-default ui-corner-top</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
							
							<a>
								<!-- Add a title element to the <a> tag if a description exists for this classifier -->
								<xsl:if test="displayItem[@name='description']">
									<xsl:attribute name='title'><xsl:value-of select="displayItem[@name='description']"/></xsl:attribute>
								</xsl:if>
								
								<!-- Add the href element to the <a> tag -->
								<xsl:choose>
									<xsl:when test="@name">
										<xsl:attribute name="href"><xsl:value-of select="$library_name"/>/collection/<xsl:value-of select="/page/pageResponse/collection[@name=$collNameChecked]/@name"/>/browse/<xsl:value-of select="@name"/></xsl:attribute>
									</xsl:when>
									<xsl:otherwise>
										<xsl:attribute name="href"><xsl:value-of select="$library_name"/>/collection/<xsl:value-of select="/page/pageResponse/collection[@name=$collNameChecked]/@name"/>/browse/1</xsl:attribute>
									</xsl:otherwise>
								</xsl:choose>
								
								<!-- Add the actual text of the <a> tag -->
								<xsl:value-of select="displayItem[@name='name']"/>
							</a>
						</li>
					</xsl:for-each>
				</xsl:if>

				<!-- all other services -->
				<xsl:for-each select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[not(@type='query') and not(@type='browse')]">
					<xsl:call-template name="navigationTab"/>
				</xsl:for-each>
			</ul>
		</xsl:if>
	</xsl:template>
	
	<!-- ***** HOME HELP PREFERENCES LOGIN ***** -->
	<xsl:template name="home-help-preferences">
		<ul id="bannerLinks">
			<!-- preferences -->
			<li class="ui-state-default ui-corner-all">
				<a href="{$library_name}/collection/{$collNameChecked}/page/pref">
					<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_tip')"/></xsl:attribute>
					<ul>
						<li><span><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_b')"/></span></li>
						<li><span class="ui-icon ui-icon-wrench"><xsl:text> </xsl:text></span></li>
					</ul>
				</a>
			</li>

			<!-- help -->
			<li class="ui-state-default ui-corner-all">
				<a href="{$library_name}/collection/{$collNameChecked}/page/help">
					<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_tip')"/></xsl:attribute>
					<ul>
						<li><span><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_b')"/></span></li>
						<li><span class="ui-icon ui-icon-help"><xsl:text> </xsl:text></span></li>
					</ul>
				</a>
			</li>
			
			<!-- login/logout -->
			<li class="ui-state-default ui-corner-all" id="userMenuButton">
				<xsl:choose>
					<xsl:when test="/page/pageRequest/userInformation/@username">
						<a>
							<xsl:attribute name="href">javascript:toggleUserMenu();</xsl:attribute>
							<script type="text/javascript">
								<xsl:text disable-output-escaping="yes">
									function toggleUserMenu()
									{
										var button = $("#userMenuButton");
										var menu;

										if(button.data("userMenu"))
										{
											menu = button.data("userMenu");
											if(menu.css("display") == "block")
											{
												menu.hide();
											}
											else
											{
												menu.show();
											}
										}
										else
										{
											menu = $("&lt;UL&gt;")
												.css("position", "absolute")
												.css("display", "block")
												.css("z-index", "100")
												.css("list-style", "none outside none")
												.css("margin-left", "0px")
												.css("padding", "0px")
												.css("font-size", "90%");

											button.data("userMenu", menu);

											var settingsLink = $("&lt;a&gt;")
												.attr("href", gs.xsltParams.library_name + "/admin/AccountSettings?s1.username=</xsl:text><xsl:value-of select="/page/pageRequest/userInformation/@username"/><xsl:text disable-output-escaping="yes">");
											var settingsButton = $("&lt;LI&gt;")
												.css("padding", "3px")
												.html("Account settings")
												.addClass("ui-state-default");
											settingsLink.append(settingsButton);

											var url = document.URL;
											var hasQueryString = (url.indexOf("?") != -1);
											var hashIndex = url.indexOf("#");
											
											var hashPart;
											if(hashIndex != -1)
											{
												hashPart = url.substring(hashIndex);
												url = url.substring(0, hashIndex);
											}
											
											var logoutLink = $("&lt;a&gt;")
												.attr("href", url + (hasQueryString ? "&amp;" : "?") + "logout=" + (hashPart ? hashPart : ""));
											var logoutButton = $("&lt;LI&gt;")
												.css("padding", "3px")
												.html("Logout")
												.addClass("ui-state-default");
											logoutLink.append(logoutButton);

											menu.append(settingsLink);
											menu.append(logoutLink);

											var buttonLeft = button.offset().left;
											var buttonTop = button.offset().top;
											var buttonHeight = button.height();

											menu.offset({top: buttonTop + buttonHeight + 4, left: buttonLeft});
											$("#topArea").append(menu);
										}
									}
								</xsl:text>
							</script>
							<ul>
								<li><span><xsl:value-of select="/page/pageRequest/userInformation/@username"/></span></li>
								<li><span class="ui-icon ui-icon-unlocked"><xsl:text> </xsl:text></span></li>
							</ul>
						</a>
					</xsl:when>
					<xsl:otherwise>
						<a>
							<xsl:attribute name="href">
								<xsl:value-of select="$library_name"/>
								<xsl:text>?a=p&amp;sa=login&amp;redirectURL=</xsl:text>
								<xsl:value-of select="$library_name"/>
								<xsl:text>%3F</xsl:text>
								<xsl:if test="/page/pageRequest/@action">
									<xsl:text>a=</xsl:text>
									<xsl:value-of select="/page/pageRequest/@action"/>
								</xsl:if>
								<xsl:if test="/page/pageRequest/@subaction">
									<xsl:text>%26sa=</xsl:text>
									<xsl:value-of select="/page/pageRequest/@subaction"/>
								</xsl:if>
								<xsl:for-each select="/page/pageRequest/paramList/param">
									<xsl:if test="@name != 'password' and @name != 's1.password' and @name != 's1.newPassword' and @name != 's1.oldPassword'">
										<xsl:text>%26</xsl:text>
										<xsl:value-of select="@name"/>
										<xsl:text>=</xsl:text>
										<xsl:value-of select="@value"/>
									</xsl:if>
								</xsl:for-each>
							</xsl:attribute>
							<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'login_tip')"/></xsl:attribute>
							<ul>
								<li><span><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'login_b')"/></span></li>
								<li><span class="ui-icon ui-icon-locked"><xsl:text> </xsl:text></span></li>
							</ul>
						</a>
					</xsl:otherwise>
				</xsl:choose>
			</li>
			<!-- debuginfo (doesn't use class="ui-state-error" since the text is not legible due to inherited text-colour) -->
			<xsl:if test="/page/pageRequest/paramList/param[(@name='debug') and (@value='on' or @value='true' or @value='1')]">
			  <li class="ui-state-default ui-corner-all">
			    <a href="{$library_name}/collection/{$collNameChecked}/page/debuginfo">
			      <xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'debuginfo_tip')"/></xsl:attribute>
			      <ul>
				<li><span><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'debuginfo_b')"/></span></li>
				<li><span class="ui-icon ui-icon-info"><xsl:text> </xsl:text></span></li>
			      </ul>
			    </a>
			  </li>
			</xsl:if>
		</ul>
	</xsl:template>
	
	<!-- ***** PAGE TITLE ***** -->
	<xsl:template name="page-title-area">
		<xsl:variable name="pageTitleVar"><xsl:call-template name="pageTitle"/></xsl:variable>
		<td id="titlearea">
			<h2>
				<!-- Resize the title based on how long it is (There's probably a better way to do this) -->
				<xsl:attribute name="style">
					<xsl:choose>
						<xsl:when test="string-length($pageTitleVar) &lt; 20">
							<xsl:text>font-size: 1.5em;</xsl:text>
						</xsl:when>
						<xsl:when test="string-length($pageTitleVar) &lt; 30">
							<xsl:text>font-size: 1.4em;</xsl:text>
						</xsl:when>
						<xsl:when test="string-length($pageTitleVar) &lt; 40">
							<xsl:text>font-size: 1.3em;</xsl:text>
						</xsl:when>
						<xsl:when test="string-length($pageTitleVar) &lt; 50">
							<xsl:text>font-size: 1.2em;</xsl:text>
						</xsl:when>
						<xsl:when test="string-length($pageTitleVar) &lt; 60">
							<xsl:text>font-size: 1.1em;</xsl:text>
						</xsl:when>
						<xsl:when test="string-length($pageTitleVar) &lt; 70">
							<xsl:text>font-size: 1em;</xsl:text>
						</xsl:when>
						<xsl:when test="string-length($pageTitleVar) &lt; 80">
							<xsl:text>font-size: 0.9em;</xsl:text>
						</xsl:when>
						<xsl:when test="string-length($pageTitleVar) &lt; 90">
							<xsl:text>font-size: 0.8em;</xsl:text>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text>font-size: 0.7em;</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:attribute>
				<!--<xsl:value-of select="string-length($pageTitleVar)" />-->
				<xsl:value-of select="$pageTitleVar" />
			</h2>
		</td>
	</xsl:template>
	
	<!-- ***** QUICK SEARCH AREA ***** -->
	<!-- Search form should only appear if there's a search (query) service AND it has an index. 
	     By default, all collections end up with some query service (default is MGPP) even when they have
	     no search indexes, which is why the extra test for the presence of an index is necessary. -->
	<xsl:template name="quick-search-area">
		<xsl:if test="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@type='query']/paramList/param[@name='index']">
			<xsl:variable name="subaction" select="/page/pageRequest/@subaction"/>
			<td id="quicksearcharea">
				<form action="{$library_name}/collection/{$collNameChecked}/search/TextQuery">
					<input type="hidden" name="rt" value="rd"/>
					<input type="hidden" name="s1.level">
						<xsl:attribute name="value">
							<xsl:choose>
								<xsl:when test="/page/pageRequest/paramList/param[@name = 's1.level']">
									<xsl:value-of select="/page/pageRequest/paramList/param[@name = 's1.level']/@value"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:text>Sec</xsl:text>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:attribute>
					</input>
					<xsl:choose>
						<xsl:when test="/page/pageResponse/service[@name = 'TextQuery']/paramList/param[@name = 'startPage']">
							<input type="hidden" name="s1.startPage" value="1"/>
						</xsl:when>
						<xsl:otherwise>
							<input type="hidden" name="startPage" value="1"/>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:if test="not(/page/pageRequest/paramList/param[@name = 's1.hitsPerPage'])">
						<input type="hidden" name="s1.hitsPerPage" value="20"/>
					</xsl:if>
					<xsl:if test="not(/page/pageRequest/paramList/param[@name = 's1.maxDocs'])">
						<input type="hidden" name="s1.maxDocs" value="100"/>
					</xsl:if>
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
					<input type="submit" id="quickSearchSubmitButton">
						<xsl:attribute name="value">
							<xsl:value-of select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='TextQuery']/displayItem[@name='submit']"/>
						</xsl:attribute>
					</input>
					<br/>
					<!-- The list of other search types -->
					<ul>
						<xsl:for-each select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@type='query']">
							<li class="ui-state-default ui-corner-all">
								<a>
									<xsl:attribute name="href">
										<xsl:value-of select="$library_name"/>/collection/<xsl:value-of select="$collNameChecked"/>/search/<xsl:value-of select="@name"/>
									</xsl:attribute>
									<xsl:value-of select="displayItem[@name='name']"/>
								</a>
							</li>
						</xsl:for-each>
					</ul>
				</form>
			</td>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="map-scripts">
		<meta content="initial-scale=1.0, user-scalable=no" name="viewport"/>
		<script src="http://maps.googleapis.com/maps/api/js?sensor=false" type="text/javascript"><xsl:text> </xsl:text></script>
		<script src="interfaces/{$interface_name}/js/map-scripts.js" type="text/javascript"><xsl:text> </xsl:text></script>
		<script type="text/javascript">$(window).load(initializeMapScripts);</script>
	</xsl:template>
</xsl:stylesheet>
