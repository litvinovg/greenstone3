<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">

  <!-- have changed this so it uses service hitsPerPage and startPage if the service description has a param called hitsPerPage,  otherwise uses interface ones -->
  <xsl:template name="resultNavigation">
    <xsl:param name="collName"/>
    <div class="resultnavigation">  
      <!-- hits type -->
      <xsl:variable name='ht'>
	<xsl:choose>
	  <xsl:when test="/page/pageResponse/service/paramList/param[@name='hitsPerPage']">s</xsl:when>
	  <xsl:otherwise>i</xsl:otherwise>
	</xsl:choose>
      </xsl:variable>
      
      <xsl:variable name="param-list" select="/page/pageRequest/paramList"/>
      <!-- hits per page -->
      <xsl:variable name="hpp">
	<xsl:choose>
	  <xsl:when test="$ht='s'"><xsl:value-of select="$param-list/param[@name='s1.hitsPerPage']/@value"/></xsl:when>
	  <xsl:when test="$param-list/param[@name='hitsPerPage']"><xsl:value-of select="$param-list/param[@name='hitsPerPage']/@value"/></xsl:when>
	  <xsl:otherwise>20</xsl:otherwise>
	</xsl:choose>
      </xsl:variable>
      <!-- total docs - this may be in numDocsMatched or numDocsReturned metadata -->
      <xsl:variable name="td">
	<xsl:choose>
	  <xsl:when test="/page/pageResponse/metadataList/metadata[@name='numDocsReturned']">
	    <xsl:value-of select="/page/pageResponse/metadataList/metadata[@name='numDocsReturned']"/>
	  </xsl:when>
	  <xsl:when test="/page/pageResponse/metadataList/metadata[@name='numDocsMatched']">
	    <xsl:value-of select="/page/pageResponse/metadataList/metadata[@name='numDocsMatched']"/>
	  </xsl:when>
	  <xsl:otherwise> <!-- this is just a fall back - should always have the metadata -->
	    <xsl:value-of select="count(/page/pageResponse/documentNodeList/documentNode)"/>
	  </xsl:otherwise>	
	</xsl:choose>
      </xsl:variable>

      <!-- only continue if hpp != -1 and td > hpp -->
      <xsl:if test="not($hpp=-1) and $td &gt; $hpp">
	<!-- start page -->
	<xsl:variable name="here">
	  <xsl:choose>
	    <xsl:when test="$ht='s'"><xsl:value-of select="$param-list/param[@name='s1.startPage']/@value"/></xsl:when>
	    <xsl:when test="$param-list/param[@name='startPage']"><xsl:value-of select="$param-list/param[@name='startPage']/@value"/></xsl:when>
	    <xsl:otherwise>1</xsl:otherwise>
	  </xsl:choose>
	</xsl:variable>
	<xsl:variable name="sa" select="/page/pageRequest/@subaction"/>
	<xsl:variable name="service" select="$param-list/param[@name='s']/@value"/>
	<xsl:variable name="prev" select="$here - 1"/>
	<xsl:variable name="next" select="$here + 1"/>
	<xsl:variable name="page-param"><xsl:if test="$ht='s'">s1.</xsl:if>startPage</xsl:variable>
	
	<div>   
	  <div>
	    <xsl:if test="$here &gt; 1"><a href="{$library_name}?a=q&amp;sa={$sa}&amp;c={$collName}&amp;s={$service}&amp;rt=rd&amp;{$page-param}={$prev}"><img src="interfaces/basic/images/less.gif" width='30' height='16' border='0' align='top'/><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.matches')"/><xsl:text> </xsl:text><xsl:value-of select="number(($prev - 1)*$hpp + 1)"/> - <xsl:value-of select="number(($prev * $hpp))"/></a></xsl:if>
	    
	    
	    <xsl:if test="(($here * $hpp) + 1)  &lt; $td">
	      <xsl:variable name='m' select="number($next * $hpp)"/>
	      <xsl:variable name='mm'><xsl:choose><xsl:when test="$m &lt; $td"><xsl:value-of select='$m'/></xsl:when><xsl:otherwise><xsl:value-of select='$td'/></xsl:otherwise></xsl:choose></xsl:variable>
	      <a href="{$library_name}?a=q&amp;sa={$sa}&amp;c={$collName}&amp;s={$service}&amp;rt=rd&amp;{$page-param}={$next}"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.matches')"/><xsl:text> </xsl:text><xsl:value-of select="number(($next - 1)*$hpp + 1)"/> - <xsl:value-of select="$mm"/><img src="interfaces/basic/images/more.gif" width='30' height='16' border='0' align='top'/></a></xsl:if></div>
	</div>
      </xsl:if>
    </div>
  </xsl:template>
  
  <xsl:template name="matchDocs">
    <!-- If the number of matching documents is known, display it -->
    <div id="matchdocs">
      <xsl:variable name="numDocsMatched" select="metadataList/metadata[@name='numDocsMatched']"/>
      <xsl:variable name="numDocsReturned" select="metadataList/metadata[@name='numDocsReturned']"/>
      <xsl:choose>
	<xsl:when test="$numDocsMatched='0' or $numDocsReturned='0'">
	  <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.nodocsmatch')"/>
	</xsl:when>
	<xsl:when test="$numDocsMatched='1' or $numDocsReturned='1'">
	  <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.onedocsmatch')"/>
	</xsl:when>
	<xsl:when test="$numDocsMatched">
	  <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.manydocsmatch', $numDocsMatched)"/>
	  <xsl:if test="$numDocsReturned"> (<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.docsreturned', $numDocsReturned)"/>)</xsl:if>
	</xsl:when>
	<xsl:when test="$numDocsReturned">
	  <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.atleastdocsmatch', $numDocsReturned)"/>
	</xsl:when>
      </xsl:choose>
    </div>
  </xsl:template>

  <!-- paging is now done by the query action, so here we just print out all the docs that we have -->
  <xsl:template name="resultList">
    <xsl:param name="collName"/>
    <ul id="resultlist"> 
      <xsl:for-each select="documentNodeList/documentNode">
	<li><table>
	    <xsl:call-template name="documentNodeWrapper">
	      <xsl:with-param name="collName" select="$collName"/>
	      <xsl:with-param name="serviceName" select="/page/pageResponse/service/@name"/>
	    </xsl:call-template>
	  </table>
	</li>	    
      </xsl:for-each>  
    </ul>
  </xsl:template>
  
  <!-- this is a wrapper node, which the interface can use to add stuff into the query results that isn't part of and doesn't depend on the documentNode template which may come from the collection -->
  <xsl:template name="documentNodeWrapper">
    <xsl:param name="collName"/>
    <xsl:param name="serviceName"/>
    <xsl:variable name="berrybasketswitch"><xsl:value-of select="/page/pageRequest/paramList/param[@name='berrybasket']/@value"/></xsl:variable>
    <!--<xsl:if test="$berryBaskets = 'true'">-->
    <xsl:if test="$berrybasketswitch = 'on'">
      <xsl:call-template name="addBerry">
	<xsl:with-param name="collName" select="$collName"/>
      </xsl:call-template>
    </xsl:if>
    <xsl:apply-templates select=".">
      <xsl:with-param name="collName" select="$collName"/>
      <xsl:with-param name="serviceName" select="$serviceName"/>
    </xsl:apply-templates>
  </xsl:template>

  
  <!-- the default doc node template for the query results -->
  <!-- eventually shouldn't need sib arg here -->
  <xsl:template match="documentNode">
    <xsl:param name="collName"/>
    <xsl:param name="serviceName"/>
    <a><xsl:attribute name="href"><xsl:value-of select='$library_name'/>?a=d&amp;c=<xsl:value-of select='$collName'/>&amp;d=<xsl:value-of select='@nodeID'/><xsl:if test="@nodeType='leaf'">&amp;sib=1</xsl:if>&amp;dt=<xsl:value-of select='@docType'/>&amp;p.a=q&amp;&amp;p.sa=<xsl:value-of select="/page/pageRequest/@subaction"/>&amp;p.s=<xsl:value-of select="$serviceName"/></xsl:attribute>
      <xsl:apply-templates select="." mode="displayNodeIcon"/>
    </a>
    <span><xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name='Title']"/></span>
  </xsl:template>

	<xsl:template name="termInfo">
		<div class="terminfo">
			<xsl:if test="count(termList/stopword) > 0">
				<span class="getTextFor query.wordcount"></span>
				<xsl:text>The following terms are too common and have been excluded from the search: </xsl:text>
				<xsl:for-each select="termList/stopword">
					<xsl:value-of select="@name"/><xsl:text> </xsl:text>
				</xsl:for-each>
				<br />
			</xsl:if>
			<xsl:if test="count(termList/term) > 0">
				<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'query.wordcount')"/>
				<xsl:for-each select="termList/term">
					<xsl:if test="position() > 1">, </xsl:if>
					<xsl:value-of select="@name"/>: <xsl:value-of select="@freq"/> 
				</xsl:for-each>
			</xsl:if>
		</div>
	</xsl:template>

</xsl:stylesheet>


