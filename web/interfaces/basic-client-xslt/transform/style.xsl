<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="icons.xsl"/>

  <!-- some global parameters - these are set by whoever is invoking the transformation -->
  <xsl:param name="interface_name"/>
  <xsl:param name="library_name"/>

  <!-- global style info goes here  -->
  <xsl:template name="globalStyle">
    <link rel="stylesheet" href="interfaces/basic/style/core.css" type="text/css"/>
  </xsl:template>

  <xsl:template name="response">
    <xsl:apply-templates select="pageResponse"/>
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
	  var theText = getText('hide_error', null);
	  alert(theText);
	  var hide_text = document.createTextNode(theText);
	  hide_link.appendChild(hide_text));
	  } else {
	  obj.style.display = "none";
	  hide_link = document.getElementById("hide");
	  removeAllChildren(hide_link);
	  var theText = getText('show_error', null);
	  alert(theText);
	  var show_text = document.createTextNode(theText);
	  hide_link.appendChild(show_text);
	  } 
	  }
	</xsl:text>
      </script>
      <p align='right'><a id="hide" href="javascript:toggleHideError($('#error'));"><xsl:call-template name="getTextFor"><xsl:with-param name="key" select="'show_error'" /><xsl:with-param name="affect" select="''" /></xsl:call-template></a></p>
      <div id="error" style="display: none;">
	<xsl:apply-templates select="descendant::error"/>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template name="greenstoneFooter">
    <div id="footer">
      <xsl:call-template name="dividerBar">
      	<xsl:with-param name="text"><xsl:text>powered-by</xsl:text></xsl:with-param>
      </xsl:call-template>
	  	<script type="text/javascript">var placeholder = false;</script>
	<script type="text/javascript" src="jquery.js">
		<xsl:comment>Filler for browser</xsl:comment>
	</script>
	<script type="text/javascript" src="test.js">
		<xsl:comment>Filler for browser</xsl:comment>
	</script>
    </div>
  </xsl:template>
  
  <xsl:template match="error">
    <span class="getTextFor error"></span> <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template name="standardPageBanner">
    <xsl:param name="collName"/>
    <xsl:param name="pageType"/>
    <xsl:variable name="this-element" select="/page/pageResponse/collection|/page/pageResponse/serviceCluster"/>
    <div id="loading" style="display: none; position: fixed; top: 0; right: 0; padding: 10px;"><img src="interfaces/basic/images/loading.gif" alt="Loading interface..." /></div>
  	<span id="language" style="display: none;"><xsl:value-of select="/page/@lang" /></span>
  	<span id="interface" style="display: none;"><xsl:value-of select="$interface_name" /></span>   
  	<div id="banner">    
      <p>
	<a href="{$library_name}?a=p&amp;sa=about&amp;c={$collName}">
	  <xsl:choose>
	    <xsl:when test="$this-element/displayItem[@name='icon']">
	      <img border="0">
		<xsl:attribute name="src">
		  <xsl:value-of select="$this-element/metadataList/metadata[@name='httpPath']"/>/images/<xsl:value-of select="$this-element/displayItem[@name='icon']"/>
		</xsl:attribute>	
		<xsl:attribute name="alt">
		  <xsl:value-of select="$this-element/displayItem[@name='name']"/>
		</xsl:attribute></img>
	    </xsl:when>
	    <xsl:otherwise>
	      <xsl:value-of select="$this-element/displayItem[@name='name']"/>
	    </xsl:otherwise>
	  </xsl:choose>
	</a>
      </p>
	  	<xsl:call-template name="getTextFor">
			<xsl:with-param name="key" select="'null'" />
			<xsl:with-param name="affect" select="'parent.title.aboutpage'" />
			<xsl:with-param name="display" select="'none'" />
		</xsl:call-template>
      <ul id="bannerlist"> 
	<xsl:call-template name="top-buttons">
	  <xsl:with-param name="collName" select="$collName"/>
	  <xsl:with-param name="pageType" select="$pageType"/>
	</xsl:call-template>
      </ul>
    </div>
  </xsl:template>
  
  <xsl:template name="top-buttons">
    <xsl:param name="collName"/>
    <xsl:param name="pageType"/>
    <li><a href="{$library_name}?a=p&amp;sa=home">
	  	<xsl:call-template name="getTextFor">
  			<xsl:with-param name="key" select="'home_b'" />
  			<xsl:with-param name="parentTip" select="'home_tip'" />
  		</xsl:call-template>
      </a></li>
    <xsl:choose>
      <xsl:when test="$pageType='help'">
	<li><a>
		  <xsl:call-template name="getTextFor">
  			  <xsl:with-param name="key" select="'help_b'" />
  			  <xsl:with-param name="affect" select="'parent.title.help_tip'" />
  		  </xsl:call-template>	
	</a></li>
      </xsl:when>
      <xsl:otherwise>
	<li><a href="{$library_name}?a=p&amp;sa=help&amp;c={$collName}">
	      <xsl:call-template name="getTextFor">
  				<xsl:with-param name="key" select="'help_b'" />
  				<xsl:with-param name="affect" select="'parent.title.help_tip'" />
  			</xsl:call-template>
	  </a></li>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="$pageType='pref'">
	<li><a><xsl:call-template name="getTextFor"><xsl:with-param name="key" select="'pref_b'" /><xsl:with-param name="affect" select="''" /></xsl:call-template></a></li>
      </xsl:when>
      <xsl:otherwise>
	<li><a href="{$library_name}?a=p&amp;sa=pref&amp;c={$collName}">
	      <xsl:call-template name="getTextFor"><xsl:with-param name="key" select="'pref_b'" /><xsl:with-param name="affect" select="'parent.title.pref_tip'" /></xsl:call-template>
	  </a></li>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="getTextFor">
  	<xsl:param name="key" />
  	<xsl:param name="affect" />
  	<xsl:param name="display" select="'yes'" />

	<xsl:variable name="extra">
	<xsl:choose>
	<xsl:when test="$affect!=''">
		<xsl:text> </xsl:text>
		<xsl:value-of select="$affect" />  	
	</xsl:when>  	
  	<xsl:otherwise>
		<xsl:value-of select="' '" />  	
  	</xsl:otherwise>
  	</xsl:choose>
  	</xsl:variable>

  	<span class="getTextFor {$key}{$extra}"><xsl:if test="$display='none'"><xsl:attribute name="style"><xsl:text>display: </xsl:text><xsl:value-of select="$display" /><xsl:text>;</xsl:text></xsl:attribute></xsl:if><xsl:value-of select="$key" /></span>
  </xsl:template>

  <xsl:template name="navigationBar">
    <xsl:param name="collName"/>
    <xsl:variable name="this-element" select="/page/pageResponse/collection|/page/pageResponse/serviceCluster"/>
    <xsl:variable name="this-service" select="/page/pageResponse/service/@name"/>
    <xsl:choose>
      <xsl:when test="$this-element/serviceList/service">
	<div id="navbar">
	  <ul id="navbarlist">
	    <xsl:for-each select="$this-element/serviceList/service">
	      <xsl:variable name="action"><xsl:choose>
		  <xsl:when test="@hidden='true'">DO_NOT_DISPLAY</xsl:when>
		  <xsl:when test="@name=$this-service">CURRENT</xsl:when>
		  <xsl:when test="@type='query'">q</xsl:when>
		  <xsl:when test="@type='browse'">b</xsl:when>
		  <xsl:when test="@type='process'">pr</xsl:when>
		  <xsl:when test="@type='applet'">a</xsl:when>
		  <xsl:otherwise>DO_NOT_DISPLAY</xsl:otherwise>
		</xsl:choose></xsl:variable>
	      <xsl:choose>
		<xsl:when test="$action='CURRENT'">
		  <li><a ><xsl:value-of select="displayItem[@name='name']"/></a></li>
		</xsl:when>
		<xsl:when test="$action !='DO_NOT_DISPLAY'">
		  <li><a href="{$library_name}?a={$action}&amp;rt=d&amp;s={@name}&amp;c={$collName}"><xsl:if test="displayItem[@name='description']"><xsl:attribute name='title'><xsl:value-of select="displayItem[@name='description']"/></xsl:attribute></xsl:if><xsl:value-of select="displayItem[@name='name']"/></a></li>
		</xsl:when>
	      </xsl:choose>
	    </xsl:for-each>
	  </ul>
	</div> 
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="dividerBar">
	  <xsl:with-param name="text" select="'&#160;'"/> 
        </xsl:call-template>            
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  
  <xsl:template name="dividerBar">
    <xsl:param name='text'/>
    <xsl:choose>
      <xsl:when test="$text">
		<div class="divbar"><xsl:choose><xsl:when test="$text='powered-by'"><span class="getTextFor gs3power">&amp;nbsp;</span></xsl:when><xsl:when test="$text='query.results'"><span class="getTextFor query.results">&amp;nbsp;</span></xsl:when><xsl:otherwise><xsl:value-of select="$text"/></xsl:otherwise></xsl:choose></div>
      </xsl:when>
      <xsl:otherwise>
	<div class="divbar"><xsl:text disable-output-escaping="yes">&#160;</xsl:text></div>
      </xsl:otherwise>
    </xsl:choose> 
  </xsl:template>
	
  <xsl:template name="direction">
    <xsl:if test="/page/@lang='ar' or /page/@lang='fa' or /page/@lang='he' or /page/@lang='ur' or /page/@lang='ps' or /page/@lang='prs'">rtl</xsl:if>
  </xsl:template>

</xsl:stylesheet>  


