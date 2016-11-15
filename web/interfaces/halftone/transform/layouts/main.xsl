<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"

	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
    xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
    
    xmlns:lxslt="http://xml.apache.org/xslt"
    xmlns:result="http://www.example.com/results"
    xmlns:exsl="http://exslt.org/common"

	extension-element-prefixes="java util result exsl"
	exclude-result-prefixes="util java util">

	
<!-- The main layout is defined here -->
<xsl:template name="mainTemplate">
<html>
	<head>
		<!-- ***** in header.xsl ***** -->
		<xsl:call-template name="create-html-header"/>
	</head>
	<body>
		<div class="wrapper cf">
		
		<header class="cf">
			
			<!-- social-bar -->
			<div id="social-bar-holder">
				<ul id="social-bar" class="cf">
					<li class="facebook"><a href="https://www.facebook.com/WaikatoUniversity"  title="Facebook" ><xsl:text> </xsl:text></a></li>
					<li class="twitter"><a href="https://twitter.com/WekaMOOC"  title="twitter" ><xsl:text> </xsl:text></a></li>
							  <xsl:if test="/page/pageResponse/format[@type='display' or @type='browse' or @type='search' or not(@type)]/gsf:option[@name='RSS']/@value = 'true'">
		  
			<li class="rss"><a href="{$library_name}?a=rss&amp;amp;l=en&amp;amp;site={$site_name}&amp;amp;c={$collNameChecked}" title="rss"><xsl:text> </xsl:text></a></li>
		  </xsl:if>
					
				</ul>
			</div>
			<div class="cf"><xsl:text> </xsl:text></div>
			<!-- ENDS social-bar -->
			
<div id="top"><xsl:text> </xsl:text></div>
			<div id="logo">
				<div id="title"><a id="lib" href="{$library_name}"><xsl:call-template name="siteName"/></a>
				<xsl:if test="/page/pageResponse/collection"><br/>
				<xsl:value-of select="/page/pageResponse/collection/displayItem[@name='name']"/></xsl:if>
				</div>
			</div>
			
			<!-- nav -->
			<nav class="cf">
				<ul id="nav" class="sf-menu">
					<xsl:call-template name="navBar"/>
				</ul>
				<div id="combo-holder"><xsl:text> </xsl:text></div>
			</nav>
			<!-- ends nav -->
		</header>
				<div role="main" id="main" class="cf">
		<xsl:choose>
		<xsl:when test="not(/page/pageRequest/@subaction='home')">
					<div class="page-content">
					<div class="entry-content cf">
							<xsl:apply-templates select="/page"/>
					</div>					
					</div>

		</xsl:when>
		<xsl:otherwise>
		<xsl:apply-templates select="/page"/>
		</xsl:otherwise>
		</xsl:choose>
				</div>
		<xsl:call-template name="gs_footer"/>
			</div>
		<xsl:call-template name="extra-script"/>
		
	</body>
</html>
</xsl:template>




<xsl:template name="loginLinks">
<xsl:variable name="username" select="/page/pageRequest/userInformation/@username"/>
<xsl:variable name="groups" select="/page/pageRequest/userInformation/@groups"/>

<xsl:choose>
<xsl:when test="$username">
<li class="login cat-item"><a href="{$library_name}/admin/AccountSettings?s1.username={$username}">Logged in as: <xsl:value-of select="$username"/></a></li>
<xsl:if test="contains($groups,'admin')">
<li class="login cat-item"><a href="{$library_name}/admin/AddUser">Add user</a></li>
<li class="login cat-item"><a href="{$library_name}/admin/ListUsers">Administration</a></li>
</xsl:if>
<li class="login cat-item"><a href="{$library_name}?logout=">Logout</a></li>
</xsl:when>
<xsl:otherwise>
<li class="login cat-item">
<a href="{$library_name}?a=p&amp;sa=login&amp;redirectURL={$library_name}%3Fa=p%26sa=home">Login
<xsl:attribute name="title">
<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'login_tip')"/>
</xsl:attribute>
</a>
</li>
</xsl:otherwise>
</xsl:choose>
</xsl:template>







<xsl:variable name="count" select="count(/page/pageResponse/collection/serviceList/service[@name='ClassifierBrowse']/classifierList/classifier)"/>


<!-- 
					<li class="current-menu-item"><a href="index.html"><span>HOME</span></a></li>
					<li><a href="page.html"><span>FEATURES</span></a>
						<ul>
							<li><a href="page.html">Columns</a></li>
							<li><a href="page-typography.html">Typography</a></li>
							<li><a href="page-elements.html">Elements</a></li>
							<li><a href="page-sidebar.html">Sidebar Page</a></li>
						</ul>
					</li>
-->
<xsl:variable name="currentPage" select="page/pageRequest/@fullURL"/>

<xsl:template name="navBar">
<xsl:choose>
<xsl:when test="page/pageResponse/collection">

<li><a href="{$library_name}"><span>Home</span></a></li>
<li>
<xsl:if test="page/pageRequest/@subaction='about'"><xsl:attribute name="class">current-menu-item</xsl:attribute></xsl:if>
<a href="{$library_name}/collection/{$collNameChecked}/page/about"><span>About</span></a>
</li>

<xsl:choose>
<xsl:when test="$count > 5">
<li>
<xsl:if test="page/pageRequest/@action='b'"><xsl:attribute name="class">current-menu-item</xsl:attribute></xsl:if>
<a href="{$library_name}/collection/{$collNameChecked}/browse/CL1"><span>Browse</span></a>
<ul>
<xsl:call-template name="Browsing"/>
</ul>
</li>
</xsl:when>
<xsl:otherwise>
<xsl:call-template name="Browsing"/>
</xsl:otherwise>
</xsl:choose>

<xsl:if test="/page/pageResponse/collection/serviceList/service/@type='query'">
<xsl:variable name="default_search" select="/page/pageResponse/collection/serviceList/service[@type='query'][1]/@name"/>
<li>
<xsl:if test="page/pageRequest/@action='q'"><xsl:attribute name="class">current-menu-item</xsl:attribute></xsl:if>
<a href="{$library_name}/collection/{$collNameChecked}/search/{$default_search}"><span>Search</span></a>
<ul>
<xsl:for-each select="/page/pageResponse/collection/serviceList/service[@type='query']">
<xsl:variable name="search" select="@name"/>
<xsl:variable name="search_name" select="displayItem[@name='name']"/>
<li><a href="{$library_name}/collection/{$collNameChecked}/search/{$search}"><xsl:value-of select="$search_name"/></a></li>
</xsl:for-each>
<li><a href="{$library_name}?a=q&amp;amp;rt=d&amp;amp;s=TextQuery">Multi-Collection</a></li>
</ul>
</li>
</xsl:if>

</xsl:when>
<xsl:otherwise> </xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="Browsing">
<xsl:for-each select="/page/pageResponse/collection/serviceList/service[@name='ClassifierBrowse']/classifierList/classifier">
<li>
<xsl:choose>
<!-- If this tab is selected then colour it differently -->
<xsl:when test="util:contains(/page/pageRequest/paramList/param[@name = 'cl' and /page/pageRequest/@action = 'b']/@value, @name) and $count &lt; 5">
<xsl:attribute name='class'>current-menu-item</xsl:attribute>
</xsl:when>
<xsl:otherwise> </xsl:otherwise>
</xsl:choose>

<a>
<!-- Add a title element to the a tag if a description exists for this classifier -->
<xsl:if test="displayItem[@name='description']">
<xsl:attribute name='title'><xsl:value-of select="displayItem[@name='description']"/></xsl:attribute>
</xsl:if>

<!-- Add the href element to the a tag -->
<xsl:choose>
<xsl:when test="@name">
<xsl:attribute name="href"><xsl:value-of select="$library_name"/>/collection/<xsl:value-of select="/page/pageResponse/collection[@name=$collNameChecked]/@name"/>/browse/<xsl:value-of select="@name"/></xsl:attribute>
</xsl:when>
<xsl:otherwise>
<xsl:attribute name="href"><xsl:value-of select="$library_name"/>/collection/<xsl:value-of select="/page/pageResponse/collection[@name=$collNameChecked]/@name"/>/browse/1</xsl:attribute>
</xsl:otherwise>
</xsl:choose>

<!-- Add the actual text of the a tag -->

<xsl:choose>
<xsl:when test="$count &lt; 5">
<span><xsl:value-of select="displayItem[@name='name']"/></span>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="displayItem[@name='name']"/>
</xsl:otherwise>
</xsl:choose>

</a>
</li>
</xsl:for-each>
</xsl:template>
























	
<!-- Template controlling the footer. -->
<xsl:template name="gs_footer">
<!-- Put footer in here. -->
		<footer>
		
			<!-- text message -->
			<div id="twitter-holder">
				<div class="ribbon-left"><xsl:text> </xsl:text></div>
				<div class="ribbon">
					<div id="tweets-bar" class="tweet">
					
					<ul class="tweet_list">
						<li>
						<div class="tweet_time"><xsl:text> </xsl:text></div>
						</li>
					</ul>
					
					</div>
				</div>
				<div class="ribbon-right"><xsl:text> </xsl:text></div>
			</div>
			<!-- ENDS text message -->
			
			
			<!-- widgets -->
			<ul  class="widget-cols cf">
				<li class="first-col">
					<div class="widget-block">
						<h4>News</h4>
						<div class="recent-post cf">
							<a href="http://www.greenstone.org/download" class="thumb"><img src="interfaces/{$interface_name}/images/54x54.jpg" alt="Post" /></a>
							<div class="post-head">
								<a href="http://www.greenstone.org/download">Greenstone 3.07 Released!</a><span>September 2015</span>
							</div>
						</div>
						<div class="recent-post cf">
							<a href="http://wiki.greenstone.org" class="thumb"><img src="interfaces/{$interface_name}/images/54x54.jpg" alt="Post" /></a>
							<div class="post-head">
								<a href="http://wiki.greenstone.org">New Wiki Launched!</a><span>October 2013</span>
							</div>
						</div>
					</div>
				</li>
				<li class="second-col">
					<div class="widget-block">
						<h4>ABOUT</h4>
						<p>This interface was created using a free CSS template by <a href="http://luiszuno.com/">http://luiszuno.com/</a>. Be sure to keep the credit line in the footer intact.</p> 
						
						<p>Placeholder images by Jennifer L. Whisler (free for reuse under a <a href="http://creativecommons.org/licenses/by/3.0/">Creative Commons Attribution</a> license).</p>
						</div>
				</li>
				<li class="third-col">
					<div class="widget-block">
						<div id="tweets" class="footer-col tweet">
		     				<h4>Library</h4>
						<ul>
						<xsl:call-template name="loginLinks"/>
						<li class="cat-item"><a href="{$library_name}/collection/{$collNameChecked}/page/pref">Preferences</a></li>
						<!--<li class="cat-item"><a href="{$library_name}/collection/{$collNameChecked}/page/help">Help</a></li>-->
						</ul>
		     			</div>
		     		</div>	
				</li>
				<li class="fourth-col">
					<div class="widget-block">
						<h4>Links</h4>
						<ul>
							<li class="cat-item"><a href="http://www.greenstone.org" >Greenstone</a></li>
							<li class="cat-item"><a href="http://wiki.greenstone.org">Greenstone Wiki</a></li>							
							<li class="cat-item"><a href="http://www.nzdl.org/cgi-bin/library.cgi" >New Zealand Digital Library</a></li>
							<li class="cat-item"><a href="http://www.greenstone.org/download">Download Greenstone3</a></li>
						</ul>
					</div>
		     		
				</li>	
			</ul>
			<!-- ENDS widgets -->
			
			<!-- bottom -->
			<div id="bottom">
				<div id="widget-trigger-holder"><a id="widget-trigger" href="{$currentPage}#" title="View More" class="poshytip"><xsl:text> </xsl:text></a></div>
				<div id="content">HalfTone Theme by <a href="http://www.luiszuno.com" >luiszuno.com</a> </div>
			</div>
			<!-- ENDS bottom -->
			
		</footer>
</xsl:template>


	
	<xsl:template name="extra-script">
	<!-- scripts concatenated and minified via build script -->

	<script src="interfaces/{$interface_name}/js/custom.js"><xsl:text> </xsl:text></script>
	
	<!-- superfish -->
	<script  src="interfaces/{$interface_name}/js/superfish-1.4.8/js/hoverIntent.js"><xsl:text> </xsl:text></script>
	<script  src="interfaces/{$interface_name}/js/superfish-1.4.8/js/superfish.js"><xsl:text> </xsl:text></script>
	<script  src="interfaces/{$interface_name}/js/superfish-1.4.8/js/supersubs.js"><xsl:text> </xsl:text></script>
	<!-- ENDS superfish -->

	<script  src="interfaces/{$interface_name}/js/poshytip-1.1/src/jquery.poshytip.js"><xsl:text> </xsl:text></script>
	<!-- end scripts -->
	
	</xsl:template>
		
</xsl:stylesheet>
