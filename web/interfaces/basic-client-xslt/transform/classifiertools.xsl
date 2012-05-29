<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  extension-element-prefixes="java"
  exclude-result-prefixes="java">
  
  <xsl:template match="classifier">
    <xsl:param name="collName"/>
    <xsl:param name="serviceName"/>
    <div id="classifiers">
      <xsl:variable name="cl_name"><xsl:value-of select="@name"/></xsl:variable>
      <xsl:choose>
	<xsl:when test="/page/pageResponse/service/classifierList/classifier[@name=$cl_name]/@horizontalAtTop">
	  <xsl:apply-templates select="." mode="horizontal-at-top">
	    <xsl:with-param name="collName" select="$collName"/>
	    <xsl:with-param name="serviceName" select="$serviceName"/>
	  </xsl:apply-templates>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:apply-templates select="." mode="default">
	    <xsl:with-param name="collName" select="$collName"/>
	    <xsl:with-param name="serviceName" select="$serviceName"/>
	  </xsl:apply-templates>
	</xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>
  

  <xsl:template match="classifier" mode="horizontal-at-top">
    <xsl:param name="collName"/>
    <xsl:param name="serviceName"/>
    <xsl:choose>
      <xsl:when test="classifierNode">
	<ul id="classifiernodelist-horizontal">
	  <xsl:for-each select='classifierNode'>
	    <xsl:apply-templates select='.' mode="horizontal"><xsl:with-param name='collName' select='$collName'/><xsl:with-param name='serviceName' select='$serviceName'/></xsl:apply-templates>
	  </xsl:for-each>
	</ul>
	<ul id="childrenlist">
	  <li>
            <table><tr>
		<xsl:for-each select='classifierNode'>
		  <xsl:apply-templates select='.' mode='process-all-children'><xsl:with-param name='collName' select='$collName'/><xsl:with-param name='serviceName' select='$serviceName'/></xsl:apply-templates>
		</xsl:for-each>
	      </tr></table>
	  </li>
	</ul>
      </xsl:when>
      <xsl:otherwise>
	<!-- there were no classifier nodes -->
	<ul id="childrenlist">
          <xsl:for-each select='documentNode'>
	    <li><table><tr><xsl:call-template name="documentNodeWrapper">
		    <xsl:with-param name='collName' select='$collName'/>
		    <xsl:with-param name='serviceName' select='$serviceName'/>
		  </xsl:call-template></tr></table></li>
	  </xsl:for-each>
	</ul>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="classifier" mode="default"> <!-- the default -->
    <xsl:param name="collName"/>
    <xsl:param name="serviceName"/>
    <ul id="classifiernodelist">
      <xsl:call-template name="processNodeChildren">
	<xsl:with-param name='collName' select='$collName'/>
	<xsl:with-param name='serviceName' select='$serviceName'/>
      </xsl:call-template>
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

  <xsl:template match="documentNode">
    <xsl:param name="collName"/>
    <xsl:param name="serviceName"/>
    <a href="{$library_name}?a=d&amp;c={$collName}&amp;d={@nodeID}&amp;dt={@docType}&amp;p.a=b&amp;p.s={$serviceName}"><xsl:apply-templates select="." mode="displayNodeIcon"/></a><xsl:text>Test</xsl:text><xsl:value-of disable-output-escaping="yes"  select="metadataList/metadata[@name='Title']"/>
  </xsl:template>

  
  
  <!-- icon + title template-->
  <xsl:template match="classifierNode">
    <xsl:param name="collName"/>
    <xsl:param name="serviceName"/>
    <a><xsl:attribute name='href'><xsl:value-of select='$library_name'/>?a=b&amp;rt=r&amp;s=<xsl:value-of select='$serviceName'/>&amp;c=<xsl:value-of select='$collName'/>&amp;cl=<xsl:value-of select='@nodeID'/><xsl:if test="classifierNode|documentNode">.pr</xsl:if></xsl:attribute><xsl:call-template name="bookshelfimg"/></a><xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name='Title']"/>

  </xsl:template>
  
  <!-- the title is a link: an alternative template -->
  <xsl:template match="classifierNode" mode="horizontal">
    <xsl:param name="collName"/>
    <xsl:param name="serviceName"/>
    <li><xsl:choose>
	<xsl:when test="classifierNode|documentNode"><b><xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name='Title']"/><xsl:text> </xsl:text></b></xsl:when>
	<xsl:otherwise>
	  <a><xsl:attribute name='href'><xsl:value-of select='$library_name'/>?a=b&amp;rt=r&amp;s=<xsl:value-of select='$serviceName'/>&amp;c=<xsl:value-of select='$collName'/>&amp;cl=<xsl:value-of select='@nodeID'/></xsl:attribute><xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name='Title']"/></a><xsl:text> </xsl:text></xsl:otherwise>
      </xsl:choose>
    </li>
  </xsl:template>
  

  <!-- processing for the recursive bit -->
  <xsl:template match="classifierNode" mode="process-all-children">
    <xsl:param name="collName"/>
    <xsl:param name="serviceName"/>
    <xsl:call-template name="processNodeChildren">
      <xsl:with-param name='collName' select='$collName'/>
      <xsl:with-param name='serviceName' select='$serviceName'/>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template name="processNodeChildren">
    <xsl:param name="collName"/>
    <xsl:param name="serviceName"/>
    <xsl:for-each select='classifierNode|documentNode'>
      <xsl:choose><xsl:when test="name()='documentNode'">
	  <li><table><tr>
		<xsl:call-template name="documentNodeWrapper">
		  <xsl:with-param name='collName' select='$collName'/>
		  <xsl:with-param name='serviceName' select='$serviceName'/>
		</xsl:call-template>
	      </tr></table>
	  </li>
	</xsl:when>
	<xsl:otherwise>
	  <li><table><tr>
		<xsl:apply-templates select='.'>
		  <xsl:with-param name='collName' select='$collName'/>
		  <xsl:with-param name='serviceName' select='$serviceName'/>
		</xsl:apply-templates>
	      </tr></table>
	  </li>
	  <xsl:if test="child::classifierNode or child::documentNode">
	    <!--recurse into the children-->
	    <li><ul class="childrenlist">
		<xsl:apply-templates select='.' mode='process-all-children'>
		  <xsl:with-param name='collName' select='$collName'/>
		  <xsl:with-param name='serviceName' select='$serviceName'/>
		</xsl:apply-templates>
	      </ul>
	    </li>
	  </xsl:if>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>