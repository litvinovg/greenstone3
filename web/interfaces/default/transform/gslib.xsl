<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xslt="http://www.w3.org/1999/XSL/Transform" 
  xmlns:gslib="http://www.greenstone.org/XSL/Library" 
  xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat" 
  xmlns:util="http://org.greenstone.gsdl3.util.XSLTUtil" 
  exclude-result-prefixes="util xalan gslib gsf xslt">


  <!-- force lower priority on xsl:output, so other files like rss.xsl can override with their own xsl:output stmt 
    If other files don't specify any, gslib.xsl specifies html output as default and defines a loose DOCTYPE for it. -->
  <xsl:import href="html-output.xsl" />

  <xsl:include href="xml-to-string.xsl"/>
	
  <!-- some global parameters - these are set by whoever is invoking the transformation -->
  <xsl:param name="interface_name"/>
  <xsl:param name="library_name"/>
  <xsl:param name="site_name"/>
  <xsl:param name="use_client_side_xslt"/>
  <!-- every pages ....................................................................... -->

  <xsl:template name="siteName">
    <xsl:value-of select="/page/pageResponse/displayItemList/displayItem[@name='siteName']"/>
  </xsl:template>

  <xsl:template name="siteDescription">
    <xsl:value-of select="/page/pageResponse/displayItemList/displayItem[@name='siteDescription']"/>
  </xsl:template>

  <xsl:template name="siteLink">
    <a href="./{$library_name}">
      <xsl:call-template name="siteName"/>
    </a>
	<xsl:text> </xsl:text>
  </xsl:template>
  
  <xsl:variable name="a">
    <xsl:value-of select="/page/pageRequest/paramList/param[@name='a']/@value"/>
  </xsl:variable>
  
  <xsl:variable name="collections" select="/page/pageResponse/collectionList/collection"/>
  
  <xsl:variable name="berrybasketswitch">
    <xsl:value-of select="/page/pageRequest/paramList/param[@name='berrybasket']/@value"/>
  </xsl:variable>
  
  <xsl:variable name="berryBasketOn" select="/page/pageRequest/paramList/param[@name='berrybasket' and @value='on']"/>
  <xsl:variable name="documentBasketOn" select="/page/pageRequest/paramList/param[@name='documentbasket' and @value='on']"/>
  
	<xsl:variable name="thisCollectionEditor">
		<xsl:value-of select="/page/pageRequest/paramList/param[@name = 'c']/@value"/>
		<xsl:text>-collection-editor</xsl:text>
	</xsl:variable>
  
  <!-- template to get the name of the current collection -->
  <xsl:template name="collectionName">
    <xsl:choose>
      <xsl:when test="/page/pageResponse/collection">
        <xsl:value-of select="/page/pageResponse/collection/displayItemList/displayItem[@name='name']"/>
      </xsl:when>
      <xsl:otherwise>All Collections</xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="collectionNameShort">
    <xsl:value-of select="/page/pageResponse/collection/@name"/>
  </xsl:template>
  <xsl:template name="collectionNameLinked">
    <xsl:if test="/page/pageResponse/collection">
      <a>
        <xsl:attribute name="href">./<xsl:value-of select="$library_name"/>/collection/<xsl:call-template name="collectionNameShort"/>/page/about</xsl:attribute>
        <xsl:call-template name="collectionName"/>
      </a>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="collectionMeta">
    <xsl:param name="name"/>
    <xsl:value-of select="/page/pageResponse/collection/metadataList/metadata[@name=$name]"/>
  </xsl:template>

  <!-- text to get the name of the current service ("Browse","Search" etc) -->
  <xsl:template name="serviceName">
    <xsl:value-of select="/page/pageResponse/service/displayItem[@name='name']"/>
  </xsl:template>
  
  <xsl:template name="textDirectionAttribute">
    <xsl:attribute name="dir">
      <xsl:choose>
        <xsl:when test="/page/@lang='ar' or /page/@lang='fa' or /page/@lang='he' or /page/@lang='ur' or /page/@lang='ps' or /page/@lang='prs'">rtl</xsl:when>
        <xsl:otherwise>ltr</xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>
  
  <xsl:template name="actionClass">
    <xsl:attribute name="class"><xsl:value-of select="/page/pageRequest/@action"/>Action <xsl:if test="/page/pageRequest/@subaction"><xsl:value-of select="/page/pageRequest/@subaction"/>Subaction</xsl:if></xsl:attribute>
  </xsl:template>
  <!-- username, if logged in -->
  <!--
	<xsl:template name="username">
		<xsl:if test="$un_s!=''">
			<xsl:if test="$asn!='' and $asn!='0'">
			<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.username')"/>  :  <xsl:value-of select="$un_s"/>
			</xsl:if>
		</xsl:if>
	</xsl:template>
	-->
  <xsl:template name="defaultDividerBar">
    <xsl:param name="text"/>
    <xsl:choose>
      <xsl:when test="$text">
        <div class="divbar">
          <xsl:value-of select="$text"/>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <div class="divbar">
          <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
        </div>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="error">
    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'error')"/> <xsl:value-of select="."/>
  </xsl:template>
  <xsl:template name="displayErrorsIfAny">
    <xsl:if test="descendant::error">
      <script language="Javascript">
        <xsl:text disable-output-escaping="yes">
	  function removeAllChildren(node) {
	    while (node.hasChildNodes()) {
	      node.removeChild(node.firstChild);
	    }
	  }

	  function toggleHideError(obj) {
	    if (obj.style.display == "none") {
	      obj.style.display = "";
	      hide_link = document.getElementById("hide");
	      removeAllChildren(hide_link);
	      hide_link.appendChild(document.createTextNode("</xsl:text>
        <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'hide_error')"/>
        <xsl:text disable-output-escaping="yes">"));
	    } else {
	      obj.style.display = "none";
	      hide_link = document.getElementById("hide");
	      removeAllChildren(hide_link);
	      hide_link.appendChild(document.createTextNode("</xsl:text>
        <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'show_error')"/>
        <xsl:text disable-output-escaping="yes">"));
	    }
	  }
	</xsl:text>
      </script>
      <p align="right">
        <a id="hide" href="javascript:toggleHideError(error);">
          <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'show_error')"/>
        </a>
      </p>
      <div id="error" style="display: none;">
        <xsl:apply-templates select="descendant::error"/>
      </div>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="noTextBar">
    <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text><!-- could also be expressed &#160; -->
  </xsl:template>
  
  <xsl:template name="poweredByGS3TextBar">
    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'gs3power')"/>
  </xsl:template>
  
  <xsl:template name="rightArrow">
    <xsl:text disable-output-escaping="yes"> &amp;raquo; </xsl:text> <!-- could also be expressed &#187; -->
  </xsl:template>
  
  <!-- site home ....................................................................... -->
  <xsl:template name="siteHomePageTitle">
    <!-- put a space in the title in case the actual value is missing - mozilla will not display a page with no title-->
    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'gsdl')"/>
    <xsl:text> </xsl:text>
  </xsl:template>
  
  <xsl:template name="selectACollectionTextBar">
    <xsl:choose>
      <xsl:when test="/page/pageResponse/groupList/group and /page/pageResponse/collectionList/collection">
      <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home.select_a_collection_or_group')"/>
      </xsl:when>
      <xsl:when test="/page/pageResponse/groupList/group">
	<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home.select_a_group')"/>
      </xsl:when>
      <xsl:when test="/page/pageResponse/collectionList/collection">
	<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home.select_a_collection')"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:choose>
	  <xsl:when test="/page/pageResponse/pathList/group">
	    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home.no_collections_group')"/>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home.no_collections')"/>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="crossCollectionQuickSearchForm">
    <xsl:apply-templates select="serviceList/service[@name='TextQuery']"/>
  </xsl:template>
  
  <xsl:template match="service[@name='TextQuery']">
    <form name="QuickSearch" method="get" action="{$library_name}">
      <input type="hidden" name="a" value="q"/>
      <input type="hidden" name="rt" value="rd"/>
      <input type="hidden" name="s" value="{@name}"/>
	  <xsl:choose>
		<xsl:when test="/page/pageRequest/paramList/param[@name='group']/@value">
			<input type="hidden" name="s1.group">
				<xsl:attribute name="value">
      				<xsl:value-of select="/page/pageRequest/paramList/param[@name='group']/@value" />
      			</xsl:attribute>
			</input>
		</xsl:when>
		<xsl:otherwise>
			<input type="hidden" name="s1.collection" value="all" />
		</xsl:otherwise>
	  </xsl:choose>
      <input type="text" name="s1.query" size="20"/>
      <input type="submit">
        <xsl:attribute name="value">
          <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home.quick_search')"/>
        </xsl:attribute>
      </input>
    </form>
  </xsl:template>
  
  <xsl:template name="groupName">
    <xsl:param name="path"/>
    <xsl:value-of select="/page/pageResponse/pathList/group[@path=$path]/title"/>
  </xsl:template>
  <xsl:template name="groupDescription">
    <xsl:param name="path"/>
    <xsl:value-of select="/page/pageResponse/pathList/group[@path=$path]/description"/>
  </xsl:template>

  <xsl:template name="groupHref">
    <xsl:param name="path"/>
    <xsl:value-of select="$library_name"/>?a=p&amp;sa=home&amp;group=<xsl:value-of select="@path"/>
  </xsl:template>

  <xsl:template name="groupLinkWithImage">
    <xsl:variable name="desc"><xsl:value-of select="description"/></xsl:variable>
    <xsl:variable name="group_href"><xsl:value-of select="$library_name"/>?a=p&amp;sa=home&amp;group=<xsl:value-of select="/page/pageRequest/paramList/param[@name='group']/@value"/>/<xsl:value-of select="@name"/></xsl:variable>
	
    <xsl:choose>
      <xsl:when test="util:checkFileExistence($site_name, backgroundImage)">
        <a href="{$group_href}" title="{$desc}">
          <img class="groupLinkImage">
            <xsl:attribute name="alt"><xsl:value-of select="displayItemList/displayItem[@name='name']"/></xsl:attribute>
            <xsl:attribute name="src">sites/<xsl:value-of select="$site_name"/>/<xsl:value-of select="backgroundImage"/></xsl:attribute>
          </img>
        </a>
      </xsl:when>
      <xsl:otherwise>
        <a href="{$group_href}" title="{$desc}">
          <div class="groupLink ui-corner-all">
			<div class="groupLinkText ui-widget-content ui-corner-top">
				<xsl:choose>
					<xsl:when test="boolean(title)">
						<xsl:value-of select="title"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="@name"/>
					</xsl:otherwise>
				</xsl:choose>
			</div>
			<div style="height:15px;" class="ui-state-default ui-corner-bottom"><xsl:text> </xsl:text></div>
          </div>
        </a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="collectionLinkWithImage">
    <xsl:variable name="desc"><xsl:value-of select="displayItemList/displayItem[@name='shortDescription']"/></xsl:variable>
    <xsl:variable name="coll_href"><xsl:value-of select="$library_name"/>/collection/<xsl:value-of select="@name"/>/page/about<xsl:choose><xsl:when test="libraryParamList/param">?<xsl:for-each select="libraryParamList/param"><xsl:value-of select="@name"/>=<xsl:value-of select="@default"/>&amp;</xsl:for-each></xsl:when></xsl:choose></xsl:variable>
    <xsl:choose>
      <xsl:when test="displayItemList/displayItem[@name='smallicon']">
        <a href="{$coll_href}" title="{$desc}">
          <img class="collectionLinkImage">
            <xsl:attribute name="alt"><xsl:value-of select="displayItemList/displayItem[@name='name']"/></xsl:attribute>
            <xsl:attribute name="src">sites/<xsl:value-of select="$site_name"/>/collect/<xsl:value-of select="@name"/>/images/<xsl:value-of select="displayItemList/displayItem[@name='smallicon']"/></xsl:attribute>
          </img>
        </a>
      </xsl:when>
      <xsl:otherwise>
        <a href="{$coll_href}" title="{$desc}">
          <div class="collectionLink ui-corner-all">
			<div class="collectionLinkText ui-widget-content ui-corner-top">
				<xsl:choose><xsl:when test="displayItemList/displayItem[@name='name']"><xsl:value-of select="displayItemList/displayItem[@name='name']"/></xsl:when>
				<xsl:otherwise>
				  <xsl:value-of select="@name"/>
				</xsl:otherwise>
			      </xsl:choose>
				  
			</div>
			<div style="height:15px;" class="ui-state-default ui-corner-bottom"><xsl:text> </xsl:text></div>
          </div>
        </a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="serviceClusterList">
    <xsl:apply-templates select="serviceClusterList"/>
  </xsl:template>
  
  <xsl:template match="serviceClusterList">
    <xsl:for-each select="serviceCluster">
      <a href="{$library_name}?a=p&amp;sa=about&amp;c={@name}">
        <xsl:value-of select="@name"/>
        <xsl:value-of select="displayItemList/displayItem[@name='name']"/>
      </a>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="serviceLink">
    <div class="paramLabel">
      <a href="{$library_name}?a=q&amp;rt=d&amp;s={@name}">
        <xsl:value-of select="displayItem[@name='name']"/>
      </a>
    </div>
    <div class="paramValue">
      <xsl:value-of select="displayItem[@name='description']"/>
    </div>
    <br class="clear"/>
  </xsl:template>
  
  <xsl:template name="authenticationLink">
    <xsl:for-each select="//serviceList/service[@type='authen']">
		<div class="paramLabel">
			<a href="{$library_name}/admin/ListUsers">
				<xsl:value-of select="displayItem[@name='name']"/>
			</a>
		</div>
		<div class="paramValue">
			<xsl:value-of select="displayItem[@name='description']"/>
		</div>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="registerLink">
    <xsl:for-each select="//serviceList/service[@type='authen']">
		<div class="paramLabel">
			<a href="{$library_name}/admin/Register"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.register')"/>
			</a>
		</div>
		<div class="paramValue">
<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.register_as_new')"/>
		</div>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="libraryInterfaceLink">
    <li>
      <a href="{$library_name}?a=p&amp;sa=gli4gs3">
        <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home.librarian_interface')"/>
      </a>
    </li>
  </xsl:template>
  
  <xsl:template name="greenstoneLogoAlternateText">
    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'gsdl')"/>
  </xsl:template>
  
  <!-- about page - collection home ....................................................................... -->
  <xsl:variable name="collName" select="/page/pageRequest/paramList/param[@name='c']/@value"/>
  <xsl:variable name="httpPath" select="/page/pageResponse/collection/metadataList/metadata[@name='httpPath']"/>
  <xsl:variable name="siteName" select="$site_name"/>
  <!--
      Already defined above as a variable, not sure why if way being defined here again as a 'param'
      <xsl:param name="collName" select="/page/pageRequest/paramList/param[@name='c']/@value"/>
      -->
  <xsl:param name="pageType"/>
  <xsl:variable name="this-element" select="/page/pageResponse/collection|/page/pageResponse/serviceCluster"/>
  <xsl:variable name="this-service" select="/page/pageRequest/paramList/param[@name = 's']/@value"/>

<!--  
_httpimages_ {_httpweb_/images}
_httpscript_ {_httpweb_/script}
_httpstyle_ {_httpweb_/style}
_httpflash_ {_httpweb_/flash}
_httpjava_ {_httpweb_/java}
-->


  <xsl:template name="define-js-variable">
    <xsl:param name="name"/>
    <xsl:param name="value"/>
    <script type="text/javascript">
      gs.variables[<xslt:text disable-output-escaping="yes">"</xslt:text><xsl:value-of select="$name"/><xslt:text disable-output-escaping="yes">"</xslt:text>]
      <xslt:text disable-output-escaping="yes"> = "</xslt:text>
      <xsl:value-of select="$value"/>
      <xslt:text disable-output-escaping="yes">";</xslt:text>
    </script>
  </xsl:template>

  <xsl:variable name="_httpcollection_" select="$httpPath"/>
  <xsl:variable name="_httpbrowse_"><xsl:value-of select="$library_name"/>/collection/<xsl:value-of select="$collName"/>/browse</xsl:variable>
  <xsl:variable name="_httpquery_"><xsl:value-of select="$library_name"/>/collection/<xsl:value-of select="$collName"/>/search</xsl:variable>

  <xsl:template name="define-js-macro-variables">

    <xsl:call-template name="define-js-variable">
      <xsl:with-param name="name"  select="'_httpbrowse_'"/>
      <xsl:with-param name="value" select="$_httpbrowse_"/>
    </xsl:call-template>

    <xsl:call-template name="define-js-variable">
      <xsl:with-param name="name"  select="'_httpquery_'"/>
      <xsl:with-param name="value" select="$_httpquery_"/>
    </xsl:call-template>

  </xsl:template>


  <xsl:template name="aboutCollectionPageTitle">
    <!-- put a space in the title in case the actual value is missing - mozilla will not display a page with no title-->
    <xsl:value-of select="/page/pageResponse/collection/displayItemList/displayItem[@name='name']"/>
    <xsl:text> </xsl:text>
  </xsl:template>
  
  <xsl:template name="collectionHomeLinkWithLogoIfAvailable">
    <a href="{$library_name}?a=p&amp;sa=about&amp;c={$collName}">
      <xsl:choose>
        <xsl:when test="$this-element/displayItemList/displayItem[@name='icon']">
          <img border="0">
            <xsl:attribute name="src"><xsl:value-of select="$this-element/metadataList/metadata[@name='httpPath']"/>/images/<xsl:value-of select="$this-element/displayItemList/displayItem[@name='icon']"/></xsl:attribute>
            <xsl:attribute name="alt">
              <xsl:value-of select="$this-element/displayItemList/displayItem[@name='name']"/>
            </xsl:attribute>
            <xsl:attribute name="title">
              <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'aboutpage')"/>
            </xsl:attribute>
          </img>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$this-element/displayItemList/displayItem[@name='name']"/>
        </xsl:otherwise>
      </xsl:choose>
    </a>
  </xsl:template>
  
  <xsl:template name="homeButtonTop">
    <a href="{$library_name}?a=p&amp;sa=home">
      <xsl:attribute name="title">
        <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home_tip')"/>
      </xsl:attribute>
      <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home_b')"/>
    </a>
  </xsl:template>
  
  <xsl:template name="helpButtonTop">
    <xsl:choose>
      <xsl:when test="$pageType='help'">
        <li>
          <a>
            <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_b')"/>
          </a>
        </li>
      </xsl:when>
      <xsl:otherwise>
        <li>
          <a href="{$library_name}?a=p&amp;sa=help&amp;c={$collName}">
            <xsl:attribute name="title">
              <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_tip')"/>
            </xsl:attribute>
            <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_b')"/>
          </a>
        </li>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="preferencesButtonTop">
    <xsl:choose>
      <xsl:when test="$pageType='pref'">
        <li>
          <a>
            <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_b')"/>
          </a>
        </li>
      </xsl:when>
      <xsl:otherwise>
        <li>
          <a href="{$library_name}?a=p&amp;sa=pref&amp;c={$collName}">
            <xsl:attribute name="title">
              <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_tip')"/>
            </xsl:attribute>
            <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_b')"/>
          </a>
        </li>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="servicesNavigationBar">
    <xsl:for-each select="$this-element/serviceList/service">
      <xsl:variable name="action">
        <xsl:choose>
          <xsl:when test="@name=$this-service">CURRENT</xsl:when>
          <xsl:when test="@type='query'">q</xsl:when>
          <xsl:when test="@type='browse'">b</xsl:when>
          <xsl:when test="@type='process'">pr</xsl:when>
          <xsl:when test="@type='applet'">a</xsl:when>
          <xsl:otherwise>DO_NOT_DISPLAY</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$action='CURRENT'">
          <li>
            <a>
              <xsl:value-of select="displayItem[@name='name']"/>
            </a>
          </li>
        </xsl:when>
        <xsl:when test="$action !='DO_NOT_DISPLAY'">
          <li>
            <a href="{$library_name}?a={$action}&amp;rt=d&amp;s={@name}&amp;c={$collName}">
              <xsl:if test="displayItem[@name='description']">
                <xsl:attribute name="title">
                  <xsl:value-of select="displayItem[@name='description']"/>
                </xsl:attribute>
              </xsl:if>
              <xsl:value-of select="displayItem[@name='name']"/>
            </a>
          </li>
        </xsl:when>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="collectionDescriptionTextAndServicesLinks">
    <xsl:apply-templates select="pageResponse/collection|serviceCluster"/>
  </xsl:template>
  
  <xsl:template match="collection|serviceCluster">
    <xsl:value-of select="displayItemList/displayItem[@name='description']" disable-output-escaping="yes"/>
<!-- Uncomment this section if you want the collection service links and their descriptions to appear -->
	<!--<xsl:apply-templates select="serviceList">
      <xsl:with-param name="collName" select="$collName"/>
    </xsl:apply-templates>-->
  </xsl:template>
  
  <xsl:template match="serviceList">	
	<xsl:param name="collName"/>
		<h3>
		  <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'about.services')"/>
		</h3>
		<p>
		  <xsl:choose>
			<xsl:when test="service">
			  <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'about.servicehelp')"/>
			</xsl:when>
			<xsl:otherwise>
			  <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'about.noservices')"/>
			</xsl:otherwise>
		  </xsl:choose>
		</p>
		<xsl:if test="service">
		  <div id="servicelist">
			<xsl:for-each select="service">
			  <xsl:sort select="position()" order="descending" data-type="number"/>
			  <xsl:variable name="action">
				<xsl:choose>
				  <xsl:when test="@type='query'">q</xsl:when>
				  <xsl:when test="@type='process'">pr</xsl:when>
				  <xsl:when test="@type='applet'">a</xsl:when>
				  <xsl:otherwise>DO_NOT_DISPLAY</xsl:otherwise>
				</xsl:choose>
			  </xsl:variable>
			  <xsl:if test="$action != 'DO_NOT_DISPLAY'">
				<div class="paramLabel">
				  <a href="{$library_name}?a={$action}&amp;rt=d&amp;s={@name}&amp;c={$collName}">
					<xsl:value-of select="displayItem[@name='name']"/>
				  </a>
				</div>
				<div class="paramLabel">
				  <xsl:value-of select="displayItem[@name='description']"/>
				</div>
				<br class="clear"/>
			  </xsl:if>
			</xsl:for-each>
		  </div>
		</xsl:if>

  </xsl:template>
  
  <!-- classifier page ............................................................................ -->
  <xsl:template name="collapsedNavigationTab">
    <xsl:param name="type"/>
    <xsl:variable name="isCurrent" select="/page/pageResponse/service[@type=$type]"/>
    <li>
      <xsl:if test="$isCurrent">
        <xsl:attribute name="class">current</xsl:attribute>
      </xsl:if>
      <a>
        <xsl:if test="service[@name=$type]/displayItem[@name='description']">
          <xsl:attribute name="title">
            <xsl:value-of select="service[@name=$type]/displayItem[@name='description']"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:attribute name="href"><xsl:value-of select="$library_name"/>?a=q&amp;rt=d&amp;s=<xsl:value-of select="service[@type=$type]/@name"/>&amp;c=<xsl:value-of select="/page/pageResponse/collection/@name"/></xsl:attribute>
        <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, $type)"/>
      </a>
    </li>
  </xsl:template>
  
  <xsl:template name="navigationTab">
    <xsl:variable name="isCurrent" select="@name=/page/pageResponse/service/@name"/>
    <xsl:variable name="action">
      <xsl:choose>
        <xsl:when test="@type='query'">q</xsl:when>
        <xsl:when test="@type='browse'">b</xsl:when>
        <xsl:when test="@type='process'">pr</xsl:when>
        <xsl:when test="@type='applet'">a</xsl:when>
        <xsl:otherwise>DO_NOT_DISPLAY</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="$action!='DO_NOT_DISPLAY'">
      <li>
        <xsl:if test="$isCurrent">
          <xsl:attribute name="class">current</xsl:attribute>
        </xsl:if>
        <a>
          <xsl:if test="displayItem[@name='description']">
            <xsl:attribute name="title">
              <xsl:value-of select="displayItem[@name='description']"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:choose>
            <xsl:when test="classifierList/classifier/@name">
              <xsl:attribute name="href"><xsl:value-of select="$library_name"/>?a=<xsl:value-of select="$action"/>&amp;rt=s&amp;s=<xsl:value-of select="@name"/>&amp;c=<xsl:value-of select="/page/pageResponse/collection/@name"/>&amp;cl=<xsl:value-of select="classifierList/classifier/@name"/></xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
              <xsl:attribute name="href"><xsl:value-of select="$library_name"/>?a=<xsl:value-of select="$action"/>&amp;rt=d&amp;s=<xsl:value-of select="@name"/>&amp;c=<xsl:value-of select="/page/pageResponse/collection/@name"/></xsl:attribute>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:value-of select="displayItem[@name='name']"/>
        </a>
      </li>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="classifierLink">
    <xsl:if test="@name=/page/pageResponse/classifier/@name">
      <xsl:attribute name="class">current</xsl:attribute>
    </xsl:if>
    <a href="{$library_name}?a=b&amp;rt=r&amp;s={/page/pageResponse/service/@name}&amp;c={/page/pageResponse/collection/@name}&amp;cl={@name}">
      <xsl:value-of select="displayItem[@name='description']"/>
    </a>
  </xsl:template>
  
  <!-- query page ............................................................................ -->
<!--  <xsl:template name="indexName">
    <xsl:value-of select="/page/pageResponse/service/displayItem[@name='name']"/>
  </xsl:template>

  <xsl:template name="queryPageCollectionName">
    <xsl:choose>
      <xsl:when test="/page/pageResponse/collection">
        <gslib:aboutCollectionPageTitle/>
      </xsl:when>
      <xsl:otherwise>Cross-Collection</xsl:otherwise>
    </xsl:choose>
  </xsl:template>-->
  
  <!--
BERRY BASKET TEMPLATES
These get used on many different pages to add the
berry basket function to the site
-->
  <!-- put the drag&drop berry basket on the page -->
  <xsl:template name="berryBasket">
    <xsl:if test="$berryBasketOn">
      <div id="berrybasket" class="hide">
        <span><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'berry.title')"/><xsl:text> </xsl:text></span>
        <span id="berryBasketExpandCollapseLinks" style="display: none;">
          <a id="berryBasketExpandLink" href="javascript:showBasket()"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'basket.expand')"/></a>
          <a id="berryBasketCollapseLink" style="display: none;" href="javascript:hideBasket()"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'basket.collapse')"/></a>
        </span>
        <div id="baskethandle">
          <span>
            <xsl:text> </xsl:text>
          </span>
        </div>
        <div id="berries">
          <span>
            <xsl:text> </xsl:text>
          </span>
	</div>
	<div><a id="berryFullViewLink" style="display: none;"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'berry.full')"/></a></div>
	<div id="berryHelpMsg"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'berry.help')"/></div>
        </div>
     
    </xsl:if>
  </xsl:template>
  
	<!-- put the drag&drop document basket on the page -->
	<xsl:template name="documentBasket">
		<xsl:if test="$documentBasketOn and /page/pageRequest/userInformation and /page/pageRequest/userInformation/@editEnabled = 'true' and (util:contains(/page/pageRequest/userInformation/@groups, 'administrator') or util:contains(/page/pageRequest/userInformation/@groups, 'all-collections-editor') or util:contains(/page/pageRequest/userInformation/@groups, $thisCollectionEditor))">
			<div id="documentbasket" class="hide">
				<span><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'db.doc_basket')"/><xsl:text> </xsl:text></span>
				<span id="documentBasketExpandCollapseLinks" style="display: none;">
					<a id="documentBasketExpandLink" href="javascript:showDocumentBox()"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'basket.expand')"/></a>
					<a id="documentBasketCollapseLink" style="display: none;" href="javascript:hideDocumentBox()"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'basket.collapse')"/></a>
				</span>
				<div id="documenthandle">
					<span>
						<xsl:text> </xsl:text>
					</span>
				</div>
				<div id="documentpages">
					<span>
						<xsl:text> </xsl:text>
					</span>
				</div>
				<div>
					<a href="javascript:clearBasket();"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'basket.clear')"/></a>
				</div>
			</div>
		</xsl:if>
	</xsl:template>
  
  <!-- include the required javascript and css for berry baskets -->
  <xsl:template name="berryBasketHeadTags">
    <script type="text/javascript" src="interfaces/{$interface_name}/js/yui/yahoo-min.js"><xsl:text> </xsl:text></script>
    <script type="text/javascript" src="interfaces/{$interface_name}/js/yui/event-min.js"><xsl:text> </xsl:text></script>
    <script type="text/javascript" src="interfaces/{$interface_name}/js/yui/connection-min.js"><xsl:text> </xsl:text></script>
    <script type="text/javascript" src="interfaces/{$interface_name}/js/yui/dom-min.js"><xsl:text> </xsl:text></script>
    <script type="text/javascript" src="interfaces/{$interface_name}/js/yui/dragdrop-min.js"><xsl:text> </xsl:text></script>
	<script type="text/javascript" src="interfaces/{$interface_name}/js/yui/cookie-min.js"><xsl:text> </xsl:text></script>
	<script type="text/javascript" src="interfaces/{$interface_name}/js/yui/animation-min.js"><xsl:text> </xsl:text></script>
	
	<script type="text/javascript" src="interfaces/{$interface_name}/js/berrybasket/ygDDPlayer.js"><xsl:text> </xsl:text></script>
    <script type="text/javascript" src="interfaces/{$interface_name}/js/berrybasket/ygDDOnTop.js"><xsl:text> </xsl:text></script>
    <script type="text/javascript" src="interfaces/{$interface_name}/js/berrybasket/berrybasket.js"><xsl:text> </xsl:text></script>
    <link rel="stylesheet" href="interfaces/{$interface_name}/style/berry.css" type="text/css"/>
	
	<!-- Combo-handled YUI CSS files: --> 
	<link rel="stylesheet" type="text/css" href="interfaces/{$interface_name}/style/skin.css"/>

	<script type="text/javascript" src="interfaces/{$interface_name}/js/documentbasket/documentbasket.js"><xsl:text> </xsl:text></script>
	<script type="text/javascript" src="interfaces/{$interface_name}/js/documentbasket/documentBasketDragDrop.js"><xsl:text> </xsl:text></script>
	<!-- Combo-handled YUI JS files: --> 
	<script type="text/javascript" src="interfaces/{$interface_name}/js/documentbasket/yahoo-dom-event.js"><xsl:text> </xsl:text></script>
	<script type="text/javascript" src="interfaces/{$interface_name}/js/documentbasket/container_core-min.js"><xsl:text> </xsl:text></script>
	<script type="text/javascript" src="interfaces/{$interface_name}/js/documentbasket/element-min.js"><xsl:text> </xsl:text></script>
	<script type="text/javascript" src="interfaces/{$interface_name}/js/documentbasket/menu-min.js"><xsl:text> </xsl:text></script>
	<script type="text/javascript" src="interfaces/{$interface_name}/js/documentbasket/button-min.js"><xsl:text> </xsl:text></script>
	<script type="text/javascript" src="interfaces/{$interface_name}/js/documentbasket/editor-min.js"><xsl:text> </xsl:text></script> 
	<script type="text/javascript" src="interfaces/{$interface_name}/js/documentbasket/yuiloader-min.js"><xsl:text> </xsl:text></script>
	<!--<script type="text/javascript" src="interfaces/{$interface_name}/js/documentbasket/editor-dialog.js"><xsl:text> </xsl:text></script>-->
	
    <link rel="stylesheet" href="interfaces/{$interface_name}/style/documentbasket.css" type="text/css"/>
  </xsl:template>
  
  <!--
create a little berry which can be drag&dropped onto the berry basket
used on classifier and search result pages
-->
  <xsl:template name="documentBerryForClassifierOrSearchPage">
    <xsl:if test="$berryBasketOn">
      <img class="pick" src="interfaces/{$interface_name}/images/berry.png" alt="in basket" width="15" height="15" border="0">
        <xsl:attribute name="id"><xsl:value-of select="/page/pageResponse/collection/@name"/>:<xsl:value-of select="@nodeID"/></xsl:attribute>
      </img>
    </xsl:if>
  </xsl:template>
  
  <!--
create little berrys which can be drag&dropped onto the berry basket
used on the document page
-->
  <xsl:template name="documentBerryForDocumentPage">
    <xsl:variable name="selectedNode">
      <xsl:value-of select="/page/pageResponse/document/@selectedNode"/>
    </xsl:variable>
    <xsl:variable name="rootNode">
      <xsl:value-of select="/page/pageResponse/document/documentNode[@nodeType='root']/@nodeID"/>
    </xsl:variable>
    <xsl:if test="$berryBasketOn">
      <div id="documentberries">
        <img class="pick" id="{/page/pageResponse/collection/@name}:{$rootNode}" src="interfaces/{$interface_name}/images/berry.png" alt="in basket" width="15" height="15" border="0"/>
        <span id="{/page/pageResponse/collection/@name}:{$rootNode}:root" class="documentberry">the whole document</span>
        <!--<xsl:if test="$selectedNode != $rootNode">
          <img class="pick" id="{/page/pageResponse/collection/@name}:{$selectedNode}" src="interfaces/{$interface_name}/images/berry.png" alt="in basket" width="15" height="15" border="0"/>
          <span id="{/page/pageResponse/collection/@name}:{$selectedNode}:section" class="documentberry">the current section</span>
        </xsl:if>-->
      </div>
    </xsl:if>
  </xsl:template>
  
  <!-- document page -->
  <xsl:template name="documentTitle">
    <xsl:value-of select="/page/pageResponse/document/documentNode/metadataList/metadata[@name='Title']"/>
  </xsl:template>
  <xsl:template name="coverImage">
    <img>
      <xsl:attribute name="src"><xsl:value-of select="/page/pageResponse/collection/metadataList/metadata[@name='httpPath']"/>/index/assoc/<xsl:value-of select="metadataList/metadata[@name='assocfilepath']"/>/cover.jpg</xsl:attribute>
    </img>
  </xsl:template>
  
  <xsl:template name="previousNextButtons">
    <!-- prev -->
    <a>
      <xsl:attribute name="href"><xsl:value-of select="$library_name"/>?a=d&amp;c=<xsl:value-of select="/page/pageResponse/collection/@name"/>&amp;d=<xsl:value-of select="@selectedNode"/>.pp&amp;sib=1&amp;p.s=<xsl:value-of select="/page/pageRequest/paramList/param[@name='p.s']/@value"/>&amp;p.sa=<xsl:value-of select="/page/pageRequest/paramList/param[@name='p.sa']/@value"/>&amp;p.a=<xsl:value-of select="/page/pageRequest/paramList/param[@name='p.a']/@value"/></xsl:attribute>
      <img class="lessarrow" src="interfaces/{$interface_name}/images/previous.png"/>
    </a>
    <!-- next -->
    <a>
      <xsl:attribute name="href"><xsl:value-of select="$library_name"/>?a=d&amp;c=<xsl:value-of select="/page/pageResponse/collection/@name"/>&amp;d=<xsl:value-of select="@selectedNode"/>.np&amp;sib=1&amp;p.s=<xsl:value-of select="/page/pageRequest/paramList/param[@name='p.s']/@value"/>&amp;p.sa=<xsl:value-of select="/page/pageRequest/paramList/param[@name='p.sa']/@value"/>&amp;p.a=<xsl:value-of select="/page/pageRequest/paramList/param[@name='p.a']/@value"/></xsl:attribute>
      <img class="morearrow" src="interfaces/{$interface_name}/images/next.png"/>
    </a>
  </xsl:template>


  <!-- This next template expands gslib:langfrag (used by document.xsl and documentbasket.xsl).
       When debugging with o=skinandlibdoc, it's seen that <gslib:langfrag name='dse' /> gets expanded to:
       <xsl:call-template name="langfrag">
	 <xsl:with-param name="name">dse</xsl:with-param>
       </xsl:call-template>
       Before the param can be used in this template, need to retrieve it by name with <xsl:param/>
       as explained in http://www.maconstateit.net/tutorials/XML/XML05/xml05-05.aspx
       -->
  <xsl:template name="langfrag">
    <xsl:param name="name"/>
    <script type="text/javascript">
      <xsl:value-of disable-output-escaping="yes" select="util:getInterfaceStringsAsJavascript($interface_name, /page/@lang, $name)"/>
    </script>
  </xsl:template>


  <!-- builds up links to available document types equivalent to the default source
       document with as anchor the equivalent documents' doctype icons. -->
  <xsl:template name="equivDocLinks">
    <xsl:param name="count"/>

      <xsl:variable name="docicon" select="metadataList/metadata[contains(@name, 'equivDocIcon')]"/>
      <xsl:variable name="docEndlink" select="metadataList/metadata[contains(@name, '/equivDocLink')]"/>

      <!--<xsl:variable name="docStartlink" select="metadataList/metadata[contains(@name, 'all_*,*_equivDocLink')]"/>-->	
      <!-- In the following variable statement, we're trying to set the docStartlink to any metadata whose value 
	ends on equivDocLink but NOT /equivDocLink. Unfortunately, the xslt function fn:ends-with only exists from
	xslt 2.0 onwards. So need to use substring() and string-lenth() functions now to check whether the 13th char
	from the end is a slash or not, in order to distinguish between the start of a doclink and end of one. 
	If this 13th char from the end is not a slash, then we found (the string we want to store in) docStartlink. -->
      <xsl:variable name="docStartlink">
	<xsl:for-each select="metadataList/metadata">
	  <xsl:if test="contains(@name, 'equivDocLink')">	
	    <xsl:variable name="tmpvar" select="substring(@name, string-length(@name)-12, 1)"/>	
	      <xsl:if test="not($tmpvar='/')">
	        <xsl:value-of select="self::node()[@name]"/> 	        
	      </xsl:if>		
	  </xsl:if>
	</xsl:for-each>	
      </xsl:variable>

      <xsl:variable name="equivDocIcon" select="java:org.greenstone.gsdl3.util.XSLTUtil.getNumberedItem($docicon, $count)" />
      <xsl:variable name="equivStartlink" select="java:org.greenstone.gsdl3.util.XSLTUtil.getNumberedItem($docStartlink, $count)" />
      <xsl:variable name="equivEndlink" select="java:org.greenstone.gsdl3.util.XSLTUtil.getNumberedItem($docEndlink, $count)" />

      <xsl:if test="$equivDocIcon != ''">
        <xsl:value-of disable-output-escaping="yes" select="$equivStartlink"/>
        <xsl:value-of disable-output-escaping="yes" select="$equivDocIcon"/>
        <xsl:value-of disable-output-escaping="yes" select="$equivEndlink"/>

        <!-- recursively call this template to get multiple entries -->
        <xsl:call-template name="equivDocLinks">
          <xsl:with-param name="count"><xsl:value-of select="$count + 1"/></xsl:with-param>
        </xsl:call-template>
      </xsl:if>

  </xsl:template>

</xsl:stylesheet>
