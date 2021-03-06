<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util gsf">

  <!-- style includes global params interface_name, library_name -->
  <xsl:include href="style.xsl"/>
  <xsl:include href="service-params.xsl"/>
  <xsl:output method="html"/>
  
  <!-- the main page layout template is here -->
  <!-- override the one in style so we can display the doc over the whole page and get rid of the green bar down the side --> 
  <xsl:template match="page" priority='2'>
    <html>
      <head>
	<title>
	  <!-- put a space in the title in case the actual value is missing - mozilla will not display a page with no title-->
	  <xsl:call-template name="pageTitle"/><xsl:text> </xsl:text>
	</title>
	<xsl:call-template name="globalStyle"/>
	<xsl:call-template name="pageStyle"/>
      </head>
      <body style="background-image: none;">
        <xsl:attribute name="dir"><xsl:call-template name="direction"/></xsl:attribute>
        <div id="page-wrapper">
	  <xsl:apply-templates select="pageResponse"/>
	  <xsl:call-template name="greenstoneFooter"/>
	</div>  
      </body>
    </html>
  </xsl:template>
  
  <xsl:template name="pageTitle">
    <xsl:variable name="docID" select="/page/pageResponse/document/@selectedNode"/>
    <xsl:for-each select='/page/pageResponse/document/descendant::documentNode[@nodeID=$docID]/ancestor-or-self::documentNode'><xsl:if test='position()!=1'>::</xsl:if><xsl:value-of select="metadataList/metadata[@name='Title']"/></xsl:for-each>
  </xsl:template>

  <!-- this is hard coded for GATE, should somehow do it dynamically-->
  <xsl:template name="pageStyle">
    <style type="text/css">
      <xsl:text disable-output-escaping="yes">
	span.Location {  display:inline; color : red }
	span.Person {  display:inline; color : green }
	span.Organization {  display:inline; color : yellow }
	span.Date {  display:inline; color : blue }
	span.query_term {display: inline; background-color : yellow }
      </xsl:text>
    </style>
  </xsl:template>
  
  <xsl:template match="pageResponse">
    <xsl:variable name="collName"><xsl:value-of select="/page/pageRequest/paramList/param[@name='c']/@value"/></xsl:variable>
    <xsl:call-template name="standardPageBanner">
      <xsl:with-param name="collName" select="$collName"/>
    </xsl:call-template>
    <xsl:call-template name="navigationBar">
      <xsl:with-param name="collName" select="$collName"/>
    </xsl:call-template>
    <div class="document">     
      <!-- display the document -->
      <xsl:apply-templates select="document">
	<xsl:with-param name="collName" select="$collName"/>
      </xsl:apply-templates>
    </div>
  </xsl:template>
  
  <xsl:template match="document">
    <xsl:param name="collName"/>

    <xsl:variable name="external"><xsl:value-of select="/page/pageResponse/document/@external"/></xsl:variable>
    <xsl:choose><xsl:when test="$external != ''">
	<xsl:call-template name="externalPage">
	  <xsl:with-param name="external" select="$external"/>
	</xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
	
	<xsl:call-template name="documentHeading">
	  <xsl:with-param name="collName" select="$collName"/>
	</xsl:call-template>
	
	<xsl:call-template name="documentArrows">
	  <xsl:with-param name="collName" select="$collName"/>
	</xsl:call-template>         
	<xsl:call-template name="documentContent">
	  <xsl:with-param name="collName" select="$collName"/>
	</xsl:call-template>
	<xsl:call-template name="documentArrows">
	  <xsl:with-param name="collName" select="$collName"/>
	</xsl:call-template> 
	<br/>
      </xsl:otherwise></xsl:choose>
  </xsl:template>
  
  <xsl:template name="documentArrows">
    <xsl:param name="collName"/>    
    <xsl:variable name="ed" select="/page/pageRequest/paramList/param[@name='ed']/@value"/>    
    <xsl:variable name="dir"><xsl:call-template name="direction"/></xsl:variable>    
    <xsl:variable name="firstDir"><xsl:choose><xsl:when test="$dir='rtl'">forward</xsl:when><xsl:otherwise>back</xsl:otherwise></xsl:choose></xsl:variable>
    <xsl:variable name="secondDir"><xsl:choose><xsl:when test="$dir='rtl'">back</xsl:when><xsl:otherwise>forward</xsl:otherwise></xsl:choose></xsl:variable>    
    
    <div class="documentarrows">
      <xsl:if test="not(string($ed)='1')">
	<xsl:call-template name="documentArrow">
	  <xsl:with-param name="collName" select="$collName"/>
	  <xsl:with-param name="direction" select="$firstDir"/>
	  <xsl:with-param name="pageDir" select="$dir"/>
	</xsl:call-template>
	<xsl:call-template name="documentArrow">
	  <xsl:with-param name="collName" select="$collName"/>
	  <xsl:with-param name="direction" select="$secondDir"/>
	  <xsl:with-param name="pageDir" select="$dir"/>
	</xsl:call-template>
      </xsl:if>
    </div>    
  </xsl:template>
  
  <xsl:template name="documentArrow">
    <xsl:param name="collName"/>
    <xsl:param name="direction"/>
    <xsl:param name="pageDir"/>
    
    <xsl:variable name="disp">
      <xsl:choose>
	<xsl:when test="$direction ='back' and nodeStructureInfo/info[@name='siblingPosition']/@value = -1">n</xsl:when>
	<xsl:when test="$direction ='forward' and nodeStructureInfo/info[@name='siblingPosition']/@value = nodeStructureInfo/info[@name='numSiblings']/@value">n</xsl:when>		    
	<xsl:otherwise>y</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="request-params" select="/page/pageRequest/paramList"/>
    <xsl:if test='$request-params/param[@name="dt"]/@value != "simple" and $disp = "y"'>  
      <a><xsl:attribute name="href"><xsl:value-of select="$library_name"/>?a=d&amp;c=<xsl:value-of select="$collName"/>&amp;d=<xsl:value-of select="@selectedNode"/><xsl:choose><xsl:when test="$direction='back'">.pp</xsl:when><xsl:otherwise>.np</xsl:otherwise></xsl:choose>&amp;sib=1&amp;p.s=<xsl:value-of select='$request-params/param[@name="p.s"]/@value'/>&amp;p.sa=<xsl:value-of select='$request-params/param[@name="p.sa"]/@value'/>&amp;p.a=<xsl:value-of select='$request-params/param[@name="p.a"]/@value'/></xsl:attribute><xsl:choose>
	  <xsl:when test="$direction='back'"><img class="lessarrow{$pageDir}" src="interfaces/basic/images/less.gif" /></xsl:when><xsl:otherwise><img class="morearrow{$pageDir}" src="interfaces/basic/images/more.gif"  /></xsl:otherwise></xsl:choose></a>
    </xsl:if>
  </xsl:template>
  

  <xsl:template name="documentHeading">
    <xsl:param name="collName"/>
    <xsl:variable name="doCoverImage" select="/page/pageResponse/format/gsf:option[@name='coverImages']/@value"/>
    <xsl:variable name="doTOC" select="/page/pageResponse/format/gsf:option[@name='documentTOC']/@value"/>
    <xsl:variable name="p.a" select="/page/pageRequest/paramList/param[@name='p.a']/@value"/>
    <xsl:variable name="p.sa" select="/page/pageRequest/paramList/param[@name='p.sa']/@value"/>
    <xsl:variable name="p.s" select="/page/pageRequest/paramList/param[@name='p.s']/@value"/>
    <xsl:variable name="p.c"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='p.c']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='p.c']/@value"/></xsl:when><xsl:otherwise><xsl:value-of select="$collName"/></xsl:otherwise></xsl:choose></xsl:variable>
    <div id="documentheading">
      <a href="{$library_name}?a={$p.a}&amp;sa={$p.sa}&amp;s={$p.s}&amp;c={$p.c}&amp;rt=rd"><xsl:call-template name="openbookimg"><xsl:with-param name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'close_book')"/></xsl:with-param>
	</xsl:call-template></a>
      <xsl:choose><xsl:when test="@docType='simple'"><xsl:value-of select="metadataList/metadata[@name='Title']" disable-output-escaping="yes"/></xsl:when><xsl:otherwise><xsl:value-of select="documentNode/metadataList/metadata[@name='Title']" disable-output-escaping="yes"/></xsl:otherwise></xsl:choose>
    </div>

    <div id="docheadwrapper">
      <div id="documentinfo">
    	<xsl:if test="string($doCoverImage) != 'false' and (@docType='paged' or @docType='hierarchy')">
       	  <div id="headingimage">
	    <xsl:call-template name="coverImage"/> 
          </div>
        </xsl:if>
	
        <ul id="docbuttons" >
	  <xsl:call-template name="documentButtons">
	    <xsl:with-param name="collName" select="$collName"/>
	  </xsl:call-template>
	</ul>
      </div>
      
      <!--<div><xsl:call-template name="enrichServices">
      <xsl:with-param name="collName" select="$collName"/>
    </xsl:call-template></div>-->
      
      <xsl:if test="string($doTOC) != 'false'">
	<div id="toc">
	  <xsl:call-template name="TOC">
	    <xsl:with-param name="collName" select="$collName"/>
	  </xsl:call-template>
	</div>
      </xsl:if>
    </div>
  </xsl:template>

  <xsl:template name="coverImage">
    <img alt="" onError="src='interfaces/gs2/images/blank.gif'"><xsl:attribute name='src'><xsl:value-of select="/page/pageResponse/collection/metadataList/metadata[@name='httpPath']"/>/index/assoc/<xsl:value-of select="metadataList/metadata[@name='archivedir']"/>/cover.jpg</xsl:attribute></img>
  </xsl:template>

  <xsl:template name="documentButtons">
    <xsl:param name="collName"/>
    <xsl:variable name="docID" select="/page/pageRequest/paramList/param[@name='d']/@value"/>
    <xsl:variable name="sib" select="/page/pageRequest/paramList/param[@name='sib']/@value"/>
    <xsl:variable name="ec" select="/page/pageRequest/paramList/param[@name='ec']/@value"/>
    <xsl:variable name="ed" select="/page/pageRequest/paramList/param[@name='ed']/@value"/>
    
    <!-- added params, otherwise the bookicon link becomes broken after click one of these buttons -->
    <xsl:variable name="p.s" select="/page/pageRequest/paramList/param[@name='p.s']/@value"/>
    <xsl:variable name="p.sa" select="/page/pageRequest/paramList/param[@name='p.sa']/@value"/>
    <xsl:variable name="p.a" select="/page/pageRequest/paramList/param[@name='p.a']/@value"/>
    <xsl:variable name="p.c"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='p.c']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='p.c']/@value"/></xsl:when><xsl:otherwise><xsl:value-of select="$collName"/></xsl:otherwise></xsl:choose></xsl:variable>
    
    <!-- expand document -->
    <xsl:if test="@docType = 'hierarchy'">
      <div class="button">
        <span class="button">
	  <xsl:choose><xsl:when test="string($ed)='1'">
	      <a href="{$library_name}?a=d&amp;d={$docID}&amp;c={$collName}&amp;sib={$sib}&amp;ed=0&amp;p.s={$p.s}&amp;p.sa={$p.sa}&amp;p.a={$p.a}&amp;p.c={$p.c}"><xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.contract_doc_tip')"/></xsl:attribute>
		<xsl:value-of disable-output-escaping='yes' select="util:getInterfaceText($interface_name, /page/@lang, 'doc.contract_doc_b')"/>
	      </a>
	    </xsl:when>
	    <xsl:otherwise>
	      <a href="{$library_name}?a=d&amp;d={$docID}&amp;c={$collName}&amp;sib={$sib}&amp;ed=1&amp;p.s={$p.s}&amp;p.sa={$p.sa}&amp;p.a={$p.a}&amp;p.c={$p.c}"><xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.expand_doc_tip')"/></xsl:attribute>
		<xsl:value-of disable-output-escaping='yes' select="util:getInterfaceText($interface_name, /page/@lang, 'doc.expand_doc_b')"/>
	      </a>
	    </xsl:otherwise>
	  </xsl:choose>
	</span>
      </div>
    </xsl:if> 
    
    <!-- expand contents -->
    <xsl:if test="@docType = 'hierarchy' and string(/page/pageResponse/format/gsf:option[@name='documentTOC']/@value) != 'false'">
      <div class="button">
        <span class="button">
	  <xsl:choose><xsl:when test="string($ec)='1'">
	      <a  href="{$library_name}?a=d&amp;d={$docID}&amp;c={$collName}&amp;sib={$sib}&amp;ec=0&amp;p.s={$p.s}&amp;p.sa={$p.sa}&amp;p.a={$p.a}&amp;p.c={$p.c}"><xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.contract_contents_tip')"/></xsl:attribute>
		<xsl:value-of disable-output-escaping='yes' select="util:getInterfaceText($interface_name, /page/@lang, 'doc.contract_contents_b')"/>
	      </a>
	    </xsl:when>
	    <xsl:otherwise>
	      <a  href="{$library_name}?a=d&amp;d={$docID}&amp;c={$collName}&amp;sib={$sib}&amp;ec=1&amp;p.s={$p.s}&amp;p.sa={$p.sa}&amp;p.a={$p.a}&amp;p.c={$p.c}"><xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.expand_contents_tip')"/></xsl:attribute>
		<xsl:value-of disable-output-escaping='yes' select="util:getInterfaceText($interface_name, /page/@lang, 'doc.expand_contents_b')"/>
	      </a>
	    </xsl:otherwise>
	  </xsl:choose>
	</span>
      </div>
    </xsl:if>
    <!-- detach page -->
    <xsl:variable name="paramList" select="/page/pageRequest/paramList"/>
    <div class="button">
      <span class="button">
	<a  target="_blank"><xsl:attribute name="href"><xsl:value-of select="$library_name"/>?a=d&amp;d=<xsl:value-of select="$docID"/>&amp;c=<xsl:value-of select="$collName"/>&amp;sib=<xsl:value-of select="$sib"/>&amp;dt=<xsl:value-of select="$paramList/param[@name='dt']/@value"/>&amp;ec=<xsl:value-of select="$paramList/param[@name='ec']/@value"/>&amp;et=<xsl:value-of select="$paramList/param[@name='et']/@value"/>&amp;p.a=<xsl:value-of select="$paramList/param[@name='p.a']/@value"/>&amp;p.s=<xsl:value-of select="$paramList/param[@name='p.s']/@value"/>&amp;p.sa=<xsl:value-of select="$paramList/param[@name='p.sa']/@value"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.detach_page_tip')"/></xsl:attribute>
	  <xsl:value-of disable-output-escaping='yes' select="util:getInterfaceText($interface_name, /page/@lang, 'doc.detach_page_b')"/>
	</a>
      </span>
    </div>
  </xsl:template>

  <xsl:template name="TOC">
    <xsl:param name="collName"/>
    <xsl:choose>
      <xsl:when test="@docType='hierarchy'">
	<xsl:call-template name="hierarchicalContents">
	  <xsl:with-param name="collName" select="$collName"/>
	</xsl:call-template>
      </xsl:when>
      <xsl:when test="@docType='paged'">
	<xsl:call-template name="pagedContents">
	  <xsl:with-param name="collName" select="$collName"/>
	</xsl:call-template>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="hierarchicalContents">
    <xsl:param name="collName"/>
    <xsl:variable name="oc" select="/page/pageRequest/paramList/param[@name='oc']/@value"/>
    <xsl:variable name="d" select="/page/pageRequest/paramList/param[@name='d']/@value"/>
    <xsl:variable name="sib" select="/page/pageRequest/paramList/param[@name='sib']/@value"/>
    <xsl:variable name="ec" select="/page/pageRequest/paramList/param[@name='ec']/@value"/>
    
    <!-- added parameters -->
    <xsl:variable name="p.s" select="/page/pageRequest/paramList/param[@name='p.s']/@value"/>
    <xsl:variable name="p.sa" select="/page/pageRequest/paramList/param[@name='p.sa']/@value"/>
    <xsl:variable name="p.a" select="/page/pageRequest/paramList/param[@name='p.a']/@value"/>
    <xsl:variable name="p.c"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='p.c']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='p.c']/@value"/></xsl:when><xsl:otherwise><xsl:value-of select="$collName"/></xsl:otherwise></xsl:choose></xsl:variable>
    
    <xsl:if test="documentNode[@nodeID]">
      <ul id="tocnodes">
	<li>
	  <xsl:choose><xsl:when test="string($oc)='0'">
	      <a href="{$library_name}?a=d&amp;c={$collName}&amp;d={$d}&amp;sib={$sib}&amp;oc=1&amp;p.s={$p.s}&amp;p.sa={$p.sa}&amp;p.a={$p.a}&amp;p.c={$p.c}"><xsl:call-template name="closedfolderimg"><xsl:with-param name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.open_toc')"/></xsl:with-param></xsl:call-template></a>
	    </xsl:when>
	    <xsl:otherwise><a href="{$library_name}?a=d&amp;c={$collName}&amp;d={$d}&amp;sib={$sib}&amp;oc=0&amp;p.s={$p.s}&amp;p.sa={$p.sa}&amp;p.a={$p.a}&amp;p.c={$p.c}"><xsl:call-template name="openfolderimg"><xsl:with-param name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.close_toc')"/></xsl:with-param></xsl:call-template></a></xsl:otherwise></xsl:choose>
	  <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.table_of_contents')"/>
	  <xsl:if test="string($oc)!='0'">
	    <ul class="tocnode">
	      <xsl:for-each select="documentNode/documentNode[@nodeID]">
		<xsl:apply-templates select='.'>
		  <xsl:with-param name="collName" select="$collName"/><xsl:with-param name="ec" select="$ec"/>
		  
		  <!-- added parameters -->
		  <xsl:with-param name="p.a" select="$p.a"/>
		  <xsl:with-param name="p.s" select="$p.s"/>
		  <xsl:with-param name="p.sa" select="$p.sa"/>
		  <xsl:with-param name="p.c" select="$p.c"/>
		  
		</xsl:apply-templates>
	      </xsl:for-each>
	    </ul>
	  </xsl:if>
	</li> 
      </ul>
    </xsl:if>
  </xsl:template>
  
  <!-- each icon-title pair is a row in a table. children go in a table in another row -->
  <xsl:template match="documentNode">
    <xsl:param name="collName"/>
    <xsl:param name="ec"/>
    
    <!-- added these parameters to be carried when changing in toc -->
    <xsl:param name="p.a"/>
    <xsl:param name="p.s"/>
    <xsl:param name="p.sa"/>
    <xsl:param name="p.c"/>
    <!-- Display the appropriate image, depending on the node type -->
    
    <li>
      <!--
      <a><xsl:attribute name="href"><xsl:value-of select='$library_name'/>?a=d&amp;c=<xsl:value-of select='$collName'/>&amp;d=<xsl:value-of select='@nodeID'/><xsl:if test="documentNode">.pr</xsl:if><xsl:if test="@nodeType='leaf'">&amp;sib=1</xsl:if><xsl:if test="string($ec) = '1'">&amp;ec=1</xsl:if></xsl:attribute>
      -->	
      
      <a><xsl:attribute name="href"><xsl:value-of select='$library_name'/>?a=d&amp;c=<xsl:value-of select='$collName'/>&amp;d=<xsl:value-of select='@nodeID'/><xsl:if test="documentNode">.pr</xsl:if>&amp;sib=1<xsl:if test="string($ec) = '1'">&amp;ec=1</xsl:if>&amp;p.a=<xsl:value-of select="$p.a"/>&amp;p.sa=<xsl:value-of select="$p.sa"/>&amp;p.s=<xsl:value-of select="$p.s"/>&amp;p.c=<xsl:value-of select="$p.c"/></xsl:attribute>
	
	<xsl:apply-templates select="." mode="displayNodeIcon"/>
      </a>
      
      <!-- Display associated title, bolded if the node has content -->
      <xsl:choose>
	<xsl:when test="nodeContent">
	  <span class="bold"><xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name='Title']"/></span>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name='Title']"/>
	</xsl:otherwise>
      </xsl:choose>

      <!-- Apply recursively to the children of this node -->
      <xsl:if test="documentNode[@nodeID]">
	
	<ul class="tocnode"> <xsl:apply-templates select="documentNode[@nodeID]">
	    <xsl:with-param name="collName" select="$collName"/>
	    <xsl:with-param name="ec" select="$ec"/>
	    
	    <!-- added parameters -->
	    <xsl:with-param name="p.a" select="$p.a"/><xsl:with-param name="p.s" select="$p.s"/><xsl:with-param name="p.sa" select="$p.sa"/><xsl:with-param name="p.c" select="$p.c"/>
	    <!--<xsl:with-param name="depth" select="$depth + 1"/>-->
	  </xsl:apply-templates>
	</ul>
      </xsl:if>
    </li>
  </xsl:template>
  
  <!-- default content is to print the title and content of any documentNodes that have nodeContent -->
  <xsl:template name="documentContent">
    <div class="documenttext">
      <xsl:choose>
        <xsl:when test="@docType='simple'">
	  <xsl:apply-templates select="nodeContent"/>
	</xsl:when>
        <xsl:otherwise>
	  <xsl:apply-templates select="descendant-or-self::node()/documentNode" mode="content"/>
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>
  
  <xsl:template match="documentNode" mode="content">
    <xsl:if test="nodeContent and string(nodeContent)">
      <xsl:if test="metadataList/metadata[@name='Title']">
	<h3><xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name='Title']"/></h3>
      </xsl:if>
      <xsl:apply-templates select="nodeContent"/>
    </xsl:if>
    
  </xsl:template>

  <!-- the actual text/content -->
  <xsl:template match="nodeContent">
    <p/>
    <xsl:for-each select="node()">
      <xsl:choose>
	<xsl:when test="not(name())"><xsl:value-of select="." disable-output-escaping="yes"/></xsl:when>
	<xsl:otherwise><xsl:apply-templates select="."/></xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>
  
  <!-- match any file nodes -->
  <xsl:template match="file">
    <xsl:variable name="httpPath" select="/page/pageResponse/collection/metadataList/metadata[@name='httpPath']"/>
    <xsl:choose>
      <xsl:when test="util:isImage(@mimeType)">
	<img src="{$httpPath}/{@href}"/>
      </xsl:when>
      <xsl:otherwise>
	<a href="{$httpPath}/{@href}"><xsl:value-of select="@href"/></a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- match any link nodes -->
  <xsl:template match="link">
    <xsl:variable name="collName" select="/page/pageRequest/paramList/param[@name='c']/@value"/> 
    <xsl:variable name="actionargs">
      <xsl:choose>
	<xsl:when test="@type='document'">a=d</xsl:when>
	<xsl:when test="@type='query'">a=q&amp;s=<xsl:value-of select="@service"/>&amp;rt=r</xsl:when>
	<xsl:otherwise>
	  p
	</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="serviceargs">
      <xsl:for-each select="param">&amp;s1.<xsl:value-of select="@name"/>=<xsl:value-of select="@value"/></xsl:for-each>
    </xsl:variable>
    <a href="{$library_name}?{$actionargs}&amp;c={$collName}{$serviceargs}"><xsl:value-of disable-output-escaping="yes" select="."/></a>
  </xsl:template>

  <!-- match any annotations and make them span elements -->
  <xsl:template match="annotation">
    <span class="{@type}"><xsl:value-of disable-output-escaping="yes" select="."/></span>
  </xsl:template>
  
  <!-- paged naviagtion : INCOMPLETE!!-->
  <xsl:template name="pagedContents">
    <xsl:param name="collName"/>
    <xsl:variable name="pos" select="nodeStructureInfo/info[@name='siblingPosition']/@value"/>
    <xsl:variable name="length" select="nodeStructureInfo/info[@name='numSiblings']/@value"/>
    <xsl:variable name="children" select="nodeStructureInfo/info[@name='numChildren']/@value"/>
    <!-- parameters for the bookicon to work properly -->
    <xsl:variable name="p.s" select="/page/pageRequest/paramList/param[@name='p.s']/@value"/>
    <xsl:variable name="p.sa" select="/page/pageRequest/paramList/param[@name='p.sa']/@value"/>
    <xsl:variable name="p.a" select="/page/pageRequest/paramList/param[@name='p.a']/@value"/>
    <xsl:variable name="p.c"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='p.c']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='p.c']/@value"/></xsl:when><xsl:otherwise><xsl:value-of select="$collName"/></xsl:otherwise></xsl:choose></xsl:variable>

    <xsl:choose>
      <xsl:when test="$pos=-1"><!-- a doc -->
	<tr valign="top">
	  <td align="left">
	  </td>
	  <td align="center"><center>
	      <b><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.pages', $children)"/></b>
	    </center></td>
	  <td align="right">
	    <a href="{$library_name}?a=d&amp;c={$collName}&amp;d={@selectedNode}.fc&amp;p.s={$p.s}&amp;p.sa={$p.sa}&amp;p.a={$p.a}&amp;p.c={$p.c}"><img src="interfaces/basic/images/more.gif" border="0" align="absbottom" /></a>
	  </td>
	</tr>
      </xsl:when>
      
      <xsl:otherwise> <!-- an internal node -->	  
	<tr valign="top">
	  <td align="left">
	    <xsl:if test=" not ( $pos = 1 )">
	      <a href="{$library_name}?a=d&amp;c={$collName}&amp;d={@selectedNode}.ps&amp;p.s={$p.s}&amp;p.sa={$p.sa}&amp;p.a={$p.a}&amp;p.c={$p.c}"><img src="interfaces/basic/images/less.gif" border="0" align="absbottom" /></a>
	    </xsl:if>
	  </td>
	  <td align="center"><center>
	      <b><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.pageof', concat($pos, ';', $length))"/></b>
	    </center></td>
	  <td align="right">
	    <xsl:if test=" not($pos = $length)">
	      <a href="{$library_name}?a=d&amp;c={$collName}&amp;d={@selectedNode}.ns&amp;p.s={$p.s}&amp;p.sa={$p.sa}&amp;p.a={$p.a}&amp;p.c={$p.c}"><img src="interfaces/basic/images/more.gif" border="0" align="absbottom" /></a>
	    </xsl:if>
	  </td>
	</tr>
      </xsl:otherwise>
    </xsl:choose>
    <tr valign="middle">
      <td align='center' valign='top' colspan='3'>
	<form name="GotoForm" method="get" action="{$library_name}">
	  <input type='hidden' name='a' value='d'/>
	  <input type='hidden' name='c' value='{$collName}'/>
	  <!--<input type='hidden' name='d' value='{@selectedNode}'/>-->
	  <input type='hidden' name='d'>
	    <xsl:attribute name='value'>
	      <xsl:if test="contains(@selectedNode, '.')"><xsl:value-of select="@selectedNode"/></xsl:if>
	      <xsl:if test="not(contains(@selectedNode, '.'))"><xsl:value-of select="@selectedNode"/>.0</xsl:if>
	    </xsl:attribute>
	  </input>    	    	
	  
	  <input type='hidden' name='p.s' value='{$p.s}'/>
	  <input type='hidden' name='p.sa' value='{$p.sa}'/>
	  <input type='hidden' name='p.a' value='{$p.a}'/>
	  <input type='hidden' name='p.c' value='{$p.c}'/>    
	  
	  <input type="text" name="gp" size="3" maxlength="4"/>
	  <input type="submit"><xsl:attribute name='value'><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'doc.gotopage')"/></xsl:attribute></input>
	</form>
      </td>
    </tr>
  </xsl:template>
  
  <xsl:template name="enrichServices">
    <xsl:param name="collName"/>
    <xsl:variable name="docID" select="/page/pageRequest/paramList/param[@name='d']/@value"/>
    <xsl:variable name="request-params" select="/page/pageRequest/paramList"/>
    <xsl:for-each select="../serviceList/service">
      <table border='1' cellspacing='0'>
	<tr><td>
	    <p /><xsl:value-of select="displayItem[@name='name']"/><p/>
	    <form name="EnrichForm" method="get" action="{$library_name}">
	      <xsl:apply-templates select="paramList"/>
	      <input type='hidden' name='a' value='d'/>
	      <input type='hidden' name='d' value='{$docID}'/>
	      <input type='hidden' name='c' value='{$collName}'/>
	      <xsl:if test='$request-params/param[@name="sib"]'>
		<input type='hidden' name='sib'><xsl:attribute name='value'><xsl:value-of select='$request-params/param[@name="sib"]/@value'/></xsl:attribute></input></xsl:if>
	      <input type='hidden' name='s' value='{@name}'/>
	      <input type='hidden' name='p.a'><xsl:attribute name='value'><xsl:value-of select='$request-params/param[@name="p.a"]'/></xsl:attribute></input>
	      <input type='hidden' name='p.sa'><xsl:attribute name='value'><xsl:value-of select='$request-params/param[@name="p.sa"]'/></xsl:attribute></input>
	      <input type='hidden' name='p.s'><xsl:attribute name='value'><xsl:value-of select='$request-params/param[@name="p.s"]'/></xsl:attribute></input>
	      <input type='hidden' name='end' value='1'/>
	      <input type="submit"><xsl:attribute name="value"><xsl:value-of select="displayItem[@name='submit']"/></xsl:attribute></input>
	    </form>
	  </td></tr>
      </table>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="paramList" mode="hidden">
    <xsl:for-each select="param">
      <input type='hidden' name='{@name}' value='{@value}'/><xsl:text>
      </xsl:text>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="externalPage">
    <xsl:param name="external"/>
    <xsl:variable name="go_forward_link"><a><xsl:attribute name="href"><xsl:value-of select="$external"/></xsl:attribute><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'external.go_forward')"/></a></xsl:variable>
    <h2><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'external.title')"/></h2>
    <p><xsl:value-of select="util:getInterfaceTextWithDOM($interface_name, /page/@lang, 'external.text', $go_forward_link)" disable-output-escaping="yes"/>
    </p>
  </xsl:template>


</xsl:stylesheet>




