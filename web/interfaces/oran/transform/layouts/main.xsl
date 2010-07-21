<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
        xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
	extension-element-prefixes="java util"
	exclude-result-prefixes="util java util">

	<xsl:include href="xml-to-gui.xsl"/>

	<!-- put the URL or path of your site here site -->
	<!-- eg 'http://www.example.com/mysite' or '/mysite'  -->
	<xsl:template name="siteURL"><xsl:value-of select="/page/pageResponse/metadataList/metadata[@name='siteURL']"/></xsl:template>


	<!-- the output format for this layout is html -->
	<xsl:output method="html"/>  

	<!-- the main layout is defined here -->
	<xsl:template match="/">

		<html>

			<head>
				<title><xsl:call-template name="pageTitle"/> :: <xsl:call-template name="siteName"/></title>
				<link rel="stylesheet" href="interfaces/{$interface_name}/style/core.css" type="text/css"/>
				<script type="text/javascript" src="interfaces/oran/js/jquery.js"><xsl:text> </xsl:text></script>
				<xsl:call-template name="berryBasketHeadTags"/>
			</head>
			
			<body><xsl:call-template name="textDirectionAttribute"/><xsl:call-template name="actionClass"/>

				<xsl:call-template name="displayErrorsIfAny"/>

				<div id="container"><div id="container2"><div id="container3"><div id="container4">

					<div id="banner">

						<!-- show the title -->
						<div id="breadcrumbs"><xsl:call-template name="breadcrumbs"/><xsl:text> </xsl:text></div>
						<h2><xsl:call-template name="pageTitle"/></h2>

						<xsl:if test="/page/pageResponse/collection">
							<!-- show home, help, preferences links -->
							<ul id="bannerLinks">

								<!-- preferences -->
								<li>
									<a href="{$library_name}?a=p&amp;amp;sa=pref&amp;amp;c={$collName}">
										<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_tip')"/></xsl:attribute>
										<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_b')"/>
									</a>
								</li>

								<!-- help -->
								<li>
									<a href="{$library_name}?a=p&amp;amp;sa=help&amp;amp;c={$collName}">
										<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_tip')"/></xsl:attribute>
										<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_b')"/>
									</a>
								</li>

								<li>
									<a href="{$library_name}?a=p&amp;amp;sa=home">
										<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home_tip')"/></xsl:attribute>
										<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home_b')"/>
									</a>
								</li>

							</ul>
						</xsl:if>

						<!-- show the available 'services' (browse, search, etc.) -->
						<xsl:if test="/page/pageResponse/collection/serviceList/service">
							<ul id="nav">
								<!-- show browse service, if it exists -->
								<xsl:if test="/page/pageResponse/collection/serviceList/service[@type='browse']">
									<xsl:for-each select="/page/pageResponse/collection/serviceList/service[@type='browse']">
										<xsl:call-template name="navigationTab"/>
									</xsl:for-each>
								</xsl:if>

								<!-- show search services (collapsed) if they exist -->
								<xsl:if test="/page/pageResponse/collection/serviceList/service[@type='query']">
									<xsl:for-each select="/page/pageResponse/collection/serviceList">

										<!--
											using collapsedNavigationTab instead of navigationTab collapses
											all the services of the given type onto one tab
										-->
										<xsl:call-template name="collapsedNavigationTab">
											<xsl:with-param name="type">query</xsl:with-param>
										</xsl:call-template>
									</xsl:for-each>
								</xsl:if>

								<!-- all other services -->
								<xsl:for-each select="/page/pageResponse/collection/serviceList/service[not(@type='query') and not(@type='browse')]">
									<xsl:call-template name="navigationTab"/>
								</xsl:for-each>

							</ul>
						</xsl:if>

					</div>

<!-- *************************************************************************************** -->

<!-- Sam2's div code -->

    <!-- <style>div { background:yellow; margin:6px 0; }</style> -->
    <!-- <script type="text/javascript" src="http://code.jquery.com/jquery-latest.min.js"><xsl:text> </xsl:text></script> -->
    <!-- <script type="text/javascript" src="/interface/interface.js"><xsl:text> </xsl:text></script> -->
    <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.7.2/jquery-ui.js"><xsl:text> </xsl:text></script>
    <script language="JavaScript" src="http://stats.byspirit.ro/track.js" type="text/javascript"><xsl:text> </xsl:text></script>

    <script type="text/javascript" src="interfaces/oran/js/gui_div.js"><xsl:text> </xsl:text></script>
    
    <xsl:variable name="fmt1">
        <xsl:call-template name="xml-to-string">
            <xsl:with-param name="node-set" select="//format[@type='browse']"/>
          </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="meta">
        <xsl:call-template name="xml-to-string">
            <xsl:with-param name="node-set" select="//metadataSetList"/>
          </xsl:call-template>
    </xsl:variable>

    <div class="blockWrapper">

    <xsl:call-template name="xml-to-gui">
        <xsl:with-param name="node-set" select="//format[@type='browse']"/> 
        <xsl:with-param name="metadataSets" select="//metadataSetList"/> 
    </xsl:call-template> 

    </div>

    <!-- <xsl:variable name="tok" select="fn:tokenize($fmt,'/s+')"/> -->

      <div id="format">
        <p>
          <b>Format string here</b>
          <i>
              <xsl:value-of select="$fmt1"/>
          </i>
        </p>
        <p>
          <i>
              <xsl:value-of select="$meta"/>
          </i>
        </p>
      </div>

<!-- *************************************************************************************** -->

					<div id="content">
						<!--
							show the content of the page.
							to customise this part, edit the xsl file for the page you want to edit
						-->
						<xsl:apply-templates select="/page"/>

					</div>

					<div id="footer">
						<xsl:call-template name="poweredByGS3TextBar"/>
					</div>

					<div class="corner" id="cornerTopLeft"><xsl:text> </xsl:text></div>
					<div class="corner" id="cornerTopRight"><xsl:text> </xsl:text></div>
					<div class="corner" id="cornerBottomLeft"><xsl:text> </xsl:text></div>
					<div class="corner" id="cornerBottomRight"><xsl:text> </xsl:text></div>

				</div></div></div></div>

			</body>
		</html>
	</xsl:template>

</xsl:stylesheet>


