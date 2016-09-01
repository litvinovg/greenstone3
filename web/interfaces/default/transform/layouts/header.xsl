<?xml version="1.0" encoding="UTF-8"?>
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
					<xsl:when test="/page/pageResponse/metadataList/metadata[@name = 'siteURL']">
						<xsl:value-of select="/page/pageResponse/metadataList/metadata[@name = 'siteURL']"/>
					</xsl:when>
					<xsl:when test="/page/pageRequest/@baseURL">
						<xsl:value-of select="/page/pageRequest/@baseURL"/>
					</xsl:when>
				</xsl:choose>
			</xsl:attribute>
		</base>
		<xsl:comment>[if lte IE 6]&gt;&lt;/base&gt;&lt;![endif]</xsl:comment>
	
		<title><xsl:call-template name="pageTitle"/> :: <xsl:call-template name="siteName"/></title>
		
		<xsl:if test="/page/pageRequest/@action ='d'">
		  
		  <xsl:variable name="myMetadataHeader" select="/page/pageResponse/format/gsf:headMetaTags/gsf:metadata"/>
		  <xsl:for-each select="$myMetadataHeader">
		    <xsl:variable name="metaname" select="@name"/>
		    
		    <xsl:variable name="metavals" 
				  select="/page/pageResponse/document/metadataList/metadata[@name = $metaname]|/page/pageResponse/document/documentNode/metadataList/metadata[@name = $metaname]"/>
		    <xsl:for-each select="$metavals">
		      <META NAME="{$metaname}" CONTENT="{.}"/>
		    </xsl:for-each>
		  </xsl:for-each>
		  
		</xsl:if>

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
		<link rel="shortcut icon" href="interfaces/{$interface_name}/images/favicon.ico"/> 
		
		<script type="text/javascript" src="interfaces/{$interface_name}/js/jquery.min.js"><xsl:text> </xsl:text></script>
		<script type="text/javascript" src="interfaces/{$interface_name}/js/jquery-ui-1.10.2.custom/js/jquery-ui-1.10.2.custom.min.js"><xsl:text> </xsl:text></script>
		<script type="text/javascript" src="interfaces/{$interface_name}/js/jquery.themeswitcher.min.js"><xsl:text> </xsl:text></script>
		<script type="text/javascript" src="interfaces/{$interface_name}/js/jquery.blockUI.js"><xsl:text> </xsl:text></script>
		<script type="text/javascript" src="interfaces/{$interface_name}/js/ace/ace.js"><xsl:text> </xsl:text></script>
		
		<script type="text/javascript" src="interfaces/{$interface_name}/js/zoomer.js"><xsl:text> </xsl:text></script>

		<xsl:if test="/page/pageResponse/format[@type='display' or @type='browse' or @type='search']/gsf:option[@name='mapEnabled']/@value = 'true'">
		  <xsl:call-template name="map-scripts"/>
		</xsl:if>
		
		<xsl:if test="/page/pageResponse/format/gsf:option[@name='mapEnabledOpenLayers']/@value = 'true'">
		  <xsl:call-template name="openlayers-map-scripts"/>
		</xsl:if>


		<xsl:if test="/page/pageResponse/format/gsf:option[@name='panoramaViewerEnabled']/@value = 'true'">
		  <xsl:call-template name="panoramaViewer-scripts"/>
		</xsl:if>

		<xsl:if test="/page/pageRequest/userInformation and /page/pageRequest/userInformation/@editEnabled = 'true' and (util:contains(/page/pageRequest/userInformation/@groups, 'administrator') or util:contains(/page/pageRequest/userInformation/@groups, 'all-collections-editor') or util:contains(/page/pageRequest/userInformation/@groups, $thisCollectionEditor))">
			<xsl:if test="/page/pageRequest/paramList/param[(@name='docEdit') and (@value='on' or @value='true' or @value='1')]">
				<script type="text/javascript" src="interfaces/{$interface_name}/js/ckeditor/ckeditor.js" defer="true"><xsl:text> </xsl:text></script>	
	<!--			<xsl:call-template name="init-direct-edit"/> -->
			</xsl:if>
			<script type="text/javascript" src="interfaces/{$interface_name}/js/debug_scripts.js"><xsl:text> </xsl:text></script>
			<script type="text/javascript" src="interfaces/{$interface_name}/js/visual-xml-editor.js"><xsl:text> </xsl:text></script>
		</xsl:if>
		
		<xsl:call-template name="setup-gs-variable"/>
		<xsl:call-template name="define-js-macro-variables"/>

		<xsl:call-template name="additionalHeaderContent"/>
	</xsl:template>
	
	<!-- This template allows for extra header content to be added by interface, site and collection. -->
	<xsl:template name="additionalHeaderContent">
	  <xsl:call-template name="additionalHeaderContent-interface"/>
	  <xsl:call-template name="additionalHeaderContent-site"/>
	  <xsl:call-template name="additionalHeaderContent-collection"/>
	</xsl:template>
	<!-- This template should be overridden in header.xsl of a new interface file if you want to add extra header content -->
	<xsl:template name="additionalHeaderContent-interface">
	</xsl:template>
	<!-- This template should be overridden in the header.xsl file in a site's transform directory if you want to add site specific headers -->
	<xsl:template name="additionalHeaderContent-site">
	</xsl:template>
	<!-- This template should be overridden in the collectionConfig.xml file if you want to add extra header content -->
	<xsl:template name="additionalHeaderContent-collection">
	</xsl:template>

	<xsl:template name="init-direct-edit">
	         <!-- might be worth moving loading the JS file to earlier, to give time to load  -->
	  	 <script type="text/javascript" src="interfaces/{$interface_name}/js/direct-edit.js"><xsl:text> </xsl:text></script>	
		<script type="text/javascript">
		  <xsl:text disable-output-escaping="yes">
		    $(document).ready(function() {		    
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
		     });						
			</xsl:text>
		</script>
	</xsl:template> 

		
	<!-- ***** HEADER LAYOUT TEMPLATE ***** -->
	<xsl:template name="create-banner">		
		<div id="gs_banner" class="ui-widget-header ui-corner-bottom">
			<div id="titlesearchcontainer">
				<xsl:call-template name="page-title-area"/>
				<xsl:call-template name="quick-search-area"/>
				<div style="clear:both;"><xsl:text> </xsl:text></div>
			</div>
			<xsl:call-template name="browsing-tabs"/>
		</div>
	</xsl:template>
	
	<xsl:template name="additionalNavTabs">
	  <xsl:for-each select="/page/pageResponse/collection[@name=$collNameChecked]/extraInfo/navigationTab">
	    <li>
	      <xsl:choose>
		<!-- if we are in frame type and this is the current page, colour it differently-->
		<xsl:when test="@type='frame' and /page/pageRequest[@subaction='html'] and /page/pageRequest/paramList/param[@name='url']/@value = @url">
		  <xsl:attribute name='class'>ui-state-default ui-corner-top ui-tabs-selected ui-state-active</xsl:attribute>
		</xsl:when>
		<xsl:otherwise>
		  <xsl:attribute name="class">ui-state-default ui-corner-top</xsl:attribute>
		</xsl:otherwise>
	      </xsl:choose>
	      <a>
		<xsl:if test="displayItem[@name='description']">
		  <xsl:attribute name='title'><xsl:value-of select="displayItem[@name='description']"/></xsl:attribute>
		</xsl:if>
		<xsl:choose>
		  <xsl:when test="@type='external-link'">
		    <xsl:attribute name="href"><xsl:value-of select="@url"/></xsl:attribute>
		  </xsl:when>
		  <xsl:when test="@type='frame'">
		    <xsl:attribute name="href"><xsl:value-of select="$library_name"/>/collection/<xsl:value-of select="/page/pageResponse/collection[@name=$collNameChecked]/@name"/>/page/html?url=<xsl:value-of select="@url"/></xsl:attribute>
		  </xsl:when>
		</xsl:choose>
		<xsl:choose>
		  <xsl:when test="displayItem[@name='name']">
		    <xsl:value-of select="displayItem[@name='name']"/>
		  </xsl:when>
		  <xsl:otherwise>link</xsl:otherwise>
		</xsl:choose>
	      </a>
	    </li>
	  </xsl:for-each>
	  
	</xsl:template>
	<!-- ***** BROWSING TABS ***** -->
	<xsl:template name="browsing-tabs">
		<xsl:if test="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service or /page/pageResponse/collection[@name=$collNameChecked]/extraInfo/navigationTab">
			<ul id="gs-nav">
			  <!-- if this collection has additional tabs, add them here -->
			  <xsl:call-template name="additionalNavTabs"/>
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

				<!-- PhindApplet. Need something similar for the Collage applet too, probably -->
				<xsl:for-each select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='PhindApplet']">
					<li>
						<xsl:choose>
							<!-- If this tab is selected then colour it differently -->
							<xsl:when test="/page/pageRequest[@action='a']">
								<xsl:attribute name='class'>ui-state-default ui-corner-top ui-tabs-selected ui-state-active</xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name='class'>ui-state-default ui-corner-top</xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>
						
						<a>
							<xsl:if test="displayItem[@name='description']">
								<xsl:attribute name="title">
									<xsl:value-of select="displayItem[@name='description']"/>
								</xsl:attribute>
							</xsl:if>						  
							<xsl:attribute name="href"><xsl:value-of select="$library_name"/>?a=a&amp;rt=d&amp;s=<xsl:value-of select="@name"/>&amp;c=<xsl:value-of select="/page/pageResponse/collection/@name"/></xsl:attribute>
							<xsl:value-of select="displayItem[@name='name']"/>
						</a>
					</li>
				</xsl:for-each>
				
				<!-- all other services -->
				<xsl:for-each select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[not(@type='query') and not(@type='browse') and not (@name='PhindApplet')]">
					<xsl:call-template name="navigationTab"/>
				</xsl:for-each>
			</ul>
			<div style="clear:both;"><xsl:text> </xsl:text></div>
		</xsl:if>
	</xsl:template>
	
	<!-- ***** HOME HELP PREFERENCES LOGIN ***** -->
	<xsl:template name="home-help-preferences">
		<ul id="bannerLinks">

		  <!-- RSS feed link can appear in a global format statement (where it has no type attribute) 
		       or in section specific format statements, such as browse, search, display. 
		       If it's present in any format statement, display the RSS link in the bannerlinks section. -->
		  <xsl:if test="/page/pageResponse/format[@type='display' or @type='browse' or @type='search' or not(@type)]/gsf:option[@name='RSS']/@value = 'true'">
			<li><gsf:link type="rss"><gsf:icon file="rssicon.png"/></gsf:link></li>
		  </xsl:if>

			<!-- preferences -->
			<li>
				<a href="{$library_name}/collection/{$collNameChecked}/page/pref">
					<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_tip')"/></xsl:attribute>
					<span id="preferencesButton"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_b')"/></span>
					<script type="text/javascript">
						<xsl:text disable-output-escaping="yes">
							$("#preferencesButton").button({icons:{primary:"ui-icon-wrench"}});
							$("#preferencesButton .ui-button-text").css({"padding-top":"0px", "padding-bottom":"3px"});
						</xsl:text>
					</script>
				</a>
			</li>

			<!-- help -->
			<!--<li>
				<a href="{$library_name}/collection/{$collNameChecked}/page/help">
					<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_tip')"/></xsl:attribute>
					<span id="helpButton"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_b')"/></span>
					<script type="text/javascript">
						<xsl:text disable-output-escaping="yes">
							$("#helpButton").button({icons:{primary:"ui-icon-help"}});
							$("#helpButton .ui-button-text").css({"padding-top":"0px", "padding-bottom":"3px"});
						</xsl:text>
					</script>
				</a>
			</li>-->
			
			<!-- login/logout -->
			<li id="userMenuButton">
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
												.css("margin", "0px")
												.css("padding", "0px")
												.css("font-size", "90%");
												
											menu.attr("id", "userMenu");

											button.data("userMenu", menu);

											var settingsLink = $("&lt;a&gt;")
												.attr("href", gs.xsltParams.library_name + "/admin/AccountSettings?s1.username=</xsl:text><xsl:value-of select="/page/pageRequest/userInformation/@username"/><xsl:text disable-output-escaping="yes">");
											var settingsButton = $("&lt;LI&gt;")
												.css("padding", "3px")
												.html("</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'menu.account_settings')"/><xsl:text disable-output-escaping="yes">")
												.addClass("ui-state-default");
											settingsLink.append(settingsButton);
											
											var editingLink = $("&lt;a&gt;")
												.attr("href", "javascript:;");
											var editingButton = $("&lt;LI&gt;")
												.css("padding", "3px")
												.html((gs.userInformation.editEnabled == "true") ? "</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'menu.disable_edit_mode')"/><xsl:text disable-output-escaping="yes">" : "</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'menu.enable_edit_mode')"/><xsl:text disable-output-escaping="yes">")
												.addClass("ui-state-default")
												.click(function()
												{
													var url = gs.xsltParams.library_name + "?a=g&amp;rt=ro&amp;s=ChangeUserEditMode&amp;s1.username=" + gs.userInformation.username + "&amp;s1.enabled=" + ((gs.userInformation.editEnabled == "true") ? "false" : "true");
													$.ajax(url)
													.success(function(response)
													{
														location.reload();
													});
												});
											editingLink.append(editingButton);

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
												.html("</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'menu.logout')"/><xsl:text disable-output-escaping="yes">")
												.addClass("ui-state-default");
											logoutLink.append(logoutButton);

											menu.append(settingsLink);
											menu.append(editingLink);
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
							<span id="loginButton"><xsl:value-of select="/page/pageRequest/userInformation/@username"/></span>
							<script type="text/javascript">
								<xsl:text disable-output-escaping="yes">
									$("#loginButton").button({icons:{primary:"ui-icon-unlocked"}});
									$("#loginButton .ui-button-text").css({"padding-top":"0px", "padding-bottom":"3px"});
								</xsl:text>
							</script>
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
							<span id="loginButton"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'login_b')"/></span>
							<script type="text/javascript">
								<xsl:text disable-output-escaping="yes">
									$("#loginButton").button({icons:{primary:"ui-icon-locked"}});
									$("#loginButton .ui-button-text").css({"padding-top":"0px", "padding-bottom":"3px"});
								</xsl:text>
							</script>
						</a>
					</xsl:otherwise>
				</xsl:choose>
			</li>
			<!-- debuginfo (doesn't use class="ui-state-error" since the text is not legible due to inherited text-colour) -->
			<xsl:if test="/page/pageRequest/paramList/param[(@name='debug') and (@value='on' or @value='true' or @value='1' or @value='yes')]">
				<li>
					<a href="{$library_name}/collection/{$collNameChecked}/page/debug">
						<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'debuginfo_tip')"/></xsl:attribute>
						<span id="debugButton"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'debuginfo_b')"/></span>
						<script type="text/javascript">
							<xsl:text disable-output-escaping="yes">
								$("#debugButton").button({icons:{primary:"ui-icon-info"}});
								$("#debugButton .ui-button-text").css({"padding-top":"0px", "padding-bottom":"3px"});
							</xsl:text>
						</script>
					</a>
				</li>
			</xsl:if>
		</ul>
	</xsl:template>
	
	<!-- ***** PAGE TITLE ***** -->
	<xsl:template name="page-title-area">
		<xsl:variable name="pageTitleVar"><xsl:call-template name="pageTitle"/></xsl:variable>
		<div id="titlearea">
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
			</h2><xsl:text> </xsl:text>
		</div>
	</xsl:template>
	
	<!-- ***** QUICK SEARCH AREA ***** -->
	<!-- Search form should only appear if there's a search (query) service AND it has an index. 
	     By default, all collections end up with some query service (default is MGPP) even when they have
	     no search indexes, which is why the extra test for the presence of an index/fq-something is necessary. -->
	<xsl:template name="quick-search-area">
		<xsl:if test="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@type='query']">
			<xsl:variable name="subaction" select="/page/pageRequest/@subaction"/>
			<div id="quicksearcharea">
				<xsl:if test="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='TextQuery']">
					<xsl:choose>
					<xsl:when test="not(page/pageRequest[@action='q']) or /page/pageRequest/paramList/param[@name='qs']/@value = '1'">
					<form action="{$library_name}/collection/{$collNameChecked}/search/TextQuery">
						<!-- This parameter says that we have come from the quick search area -->
						<input type="hidden" name="qs" value="1"/>
						<input type="hidden" name="rt" value="rd"/>
						<input type="hidden" name="s1.level">
							<xsl:attribute name="value">
									        <xsl:value-of select="/page/pageResponse/collection/serviceList/service[@name='TextQuery']/paramList/param[@name = 'level']/@default"/>
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

						<!-- The query text box -->
						<span class="querybox">
							<xsl:variable name="qs">
								<xsl:apply-templates select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='TextQuery']/paramList/param[@name='query']" mode="calculate-default"/>
							</xsl:variable>
							<nobr>
								<xsl:apply-templates select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='TextQuery']/paramList/param[@name='query']">
<!--
    <xsl:with-param name="default" select="java:org.greenstone.gsdl3.util.XSLTUtil.tidyWhitespace($qs, /page/@lang)"/>
    -->
								  <xsl:with-param name="default" select="normalize-space($qs)"/>
								  
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
									<xsl:with-param name="hideSingle">false</xsl:with-param>
									<xsl:with-param name="quickSearch">true</xsl:with-param>
								</xsl:apply-templates>
							</span>
						</xsl:if>
						<!-- The partition selection list -->						
						<xsl:if test="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='TextQuery']/paramList/param[@name='indexSubcollection']/@type = 'enum_single'">
							<span class="textselect">
								<xsl:apply-templates select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='TextQuery']/paramList/param[@name='indexSubcollection']">
									<xsl:with-param name="default">
										<xsl:apply-templates select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='TextQuery']/paramList/param[@name='indexSubcollection']" mode="calculate-default"/>
									</xsl:with-param>
									<xsl:with-param name="hideSingle">true</xsl:with-param>
									<xsl:with-param name="quickSearch">true</xsl:with-param>
								</xsl:apply-templates>
							</span>
						</xsl:if>	
						<!-- The language selection list -->						
						<xsl:if test="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='TextQuery']/paramList/param[@name='indexLanguage']/@type = 'enum_single'">
							<span class="textselect">
								<xsl:apply-templates select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='TextQuery']/paramList/param[@name='indexLanguage']">
									<xsl:with-param name="default">
										<xsl:apply-templates select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='TextQuery']/paramList/param[@name='indexLanguage']" mode="calculate-default"/>
									</xsl:with-param>
									<xsl:with-param name="hideSingle">true</xsl:with-param>
									<xsl:with-param name="quickSearch">true</xsl:with-param>
								</xsl:apply-templates>
							</span>
						</xsl:if>							
						<!-- The submit button (for TextQuery) -->
						<xsl:if test="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='TextQuery']">
							<input type="submit" id="quickSearchSubmitButton">
								<xsl:attribute name="value">
									<xsl:value-of select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@name='TextQuery']/displayItem[@name='submit']"/>
								</xsl:attribute>
							</input>
							<br/>
						</xsl:if>
					</form>
					</xsl:when>
					<xsl:otherwise><br/></xsl:otherwise>
					</xsl:choose>			
				</xsl:if>
				<!-- The list of other search types -->
				<ul>
					<xsl:for-each select="/page/pageResponse/collection[@name=$collNameChecked]/serviceList/service[@type='query']">
						<li>
						<xsl:choose>
						<xsl:when test="@name = /page/pageRequest/paramList/param[@name='s']/@value and not(/page/pageRequest/paramList/param[@name='qs']/@value = 1)">
					<xsl:attribute name="class">ui-state-default ui-corner-all ui-state-active</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
					<xsl:attribute name="class">ui-state-default ui-corner-all</xsl:attribute>
					</xsl:otherwise>
					</xsl:choose>
							<a>
								<xsl:attribute name="href">
									<xsl:value-of select="$library_name"/>/collection/<xsl:value-of select="$collNameChecked"/>/search/<xsl:value-of select="@name"/>
								</xsl:attribute>
								<xsl:value-of select="displayItem[@name='name']"/>
							</a>
						</li>
					</xsl:for-each>
				</ul>
			</div>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="map-scripts">
		<meta content="initial-scale=1.0, user-scalable=no" name="viewport"/>
		<script src="http://maps.googleapis.com/maps/api/js?key=AIzaSyCofVTps3xHfMvIcTMHqYMMxe3xXfkAxnI" type="text/javascript"><xsl:text> </xsl:text></script>
		<script src="interfaces/{$interface_name}/js/map-scripts.js" type="text/javascript"><xsl:text> </xsl:text></script>
		<script type="text/javascript">$(window).load(initializeMapScripts);</script>
	</xsl:template>

	<xsl:template name="openlayers-map-scripts">
		<script src="interfaces/{interface_name}/js/OpenLayers.js" type="text/javascript"><xsl:text> </xsl:text></script>
	</xsl:template>

	<xsl:template name="panoramaViewer-scripts">
           <script src="interfaces/{$interface_name}/js/three45.min.js" type="text/javascript"><xsl:text> </xsl:text></script>
	   <script src="interfaces/{$interface_name}/js/Tween.js" type="text/javascript"><xsl:text> </xsl:text></script>
	   <script src="interfaces/{$interface_name}/js/Detector.js" type="text/javascript"><xsl:text> </xsl:text></script>
	   <script src="interfaces/{$interface_name}/js/RequestAnimationFrame.js" type="text/javascript"><xsl:text> </xsl:text></script>
	   <script src="interfaces/{$interface_name}/js/panoramaMarker.js" type="text/javascript"><xsl:text> </xsl:text></script>
	   <script src="interfaces/{$interface_name}/js/panoramaViewer.js" type="text/javascript"><xsl:text> </xsl:text></script>
	   <script type="text/javascript">$(window).load(initPanoramaViewer);$(window).load(_animate);</script>
	</xsl:template>

	<xsl:template name="choose-title">
		<gsf:choose-metadata>
			<gsf:metadata name="dc.Title"/>
			<gsf:metadata name="exp.Title"/>
			<gsf:metadata name="ex.dc.Title"/>
			<gsf:metadata name="Title"/>
			<gsf:default>Untitled</gsf:default>
		</gsf:choose-metadata>
	</xsl:template>
</xsl:stylesheet>
