<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">

  
  <xsl:template match="classifier">
    <xsl:param name="collName"/>
    <xsl:param name="serviceName"/>
    <div id="classifiers">
	<xsl:variable name="cl_name"><xsl:value-of select="@name"/></xsl:variable>
	<xsl:apply-templates select="." mode="default">
	    <xsl:with-param name="collName" select="$collName"/>
	    <xsl:with-param name="serviceName" select="$serviceName"/>
	</xsl:apply-templates>
    </div>
  </xsl:template>


  <xsl:template match="classifier" mode="default"> <!-- the default -->
    <xsl:param name="collName"/>
    <xsl:param name="serviceName"/>
    <table id="classifiernodelist">
      <xsl:call-template name="processNodeChildren">
	<xsl:with-param name='collName' select='$collName'/>
	<xsl:with-param name='serviceName' select='$serviceName'/>
      </xsl:call-template>
    </table>
  </xsl:template>

  
  <!-- this is a wrapper node, which the interface can use to add stuff into the query results that isn't part of and doesn't depend on the documentNode template which may come from the collection -->
  <xsl:template name="documentNodeWrapper">
    <xsl:param name="collName"/>
    <xsl:param name="serviceName"/> 
    <xsl:apply-templates select=".">
      <xsl:with-param name="collName" select="$collName"/>
      <xsl:with-param name="serviceName" select="$serviceName"/>
    </xsl:apply-templates>
  </xsl:template>


  <xsl:template match="documentNode">
    <xsl:param name="collName"/>
    <xsl:param name="serviceName"/>
    <a href="{$library_name}?a=d&amp;c={$collName}&amp;d={@nodeID}&amp;dt={@docType}&amp;p.a=b&amp;p.s={$serviceName}"><xsl:apply-templates select="." mode="displayNodeIcon"/></a><xsl:value-of disable-output-escaping="yes"  select="metadataList/metadata[@name='Title']"/>
	
    <!-- The berry (optional) -->
    <td>
        <xsl:call-template name="documentBerryForClassifierOrSearchPage"/>
    </td>
  </xsl:template>
  
  
  <!-- icon + title template-->
  <xsl:template match="classifierNode">
    <xsl:param name="collName"/>
    <xsl:param name="serviceName"/>
    <a><xsl:attribute name='href'><xsl:value-of select='$library_name'/>?a=b&amp;rt=r&amp;s=<xsl:value-of select='$serviceName'/>&amp;c=<xsl:value-of select='$collName'/>&amp;cl=<xsl:value-of select='@nodeID'/><xsl:if test="classifierNode|documentNode">.pr</xsl:if></xsl:attribute><xsl:call-template name="bookshelfimg"/></a><xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name='Title']"/>
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
    <xsl:for-each select='classifierNode|documentNode'><tr>
      <xsl:choose>
	<xsl:when test="name()='documentNode'">
	  <td><table><tr>
		<xsl:call-template name="documentNodeWrapper">
		  <xsl:with-param name='collName' select='$collName'/>
		  <xsl:with-param name='serviceName' select='$serviceName'/>
		</xsl:call-template>
	      </tr></table>
	  </td>
	</xsl:when>
	<xsl:otherwise>
	  <td><table><tr>
		<xsl:apply-templates select='.'>
		  <xsl:with-param name='collName' select='$collName'/>
		  <xsl:with-param name='serviceName' select='$serviceName'/>
		</xsl:apply-templates>
	      </tr></table>
	  </td>
	  <xsl:if test="child::classifierNode or child::documentNode">
	    <!--recurse into the children-->
	    <tr><td><table class="childrenlist">
		<xsl:apply-templates select='.' mode='process-all-children'>
		  <xsl:with-param name='collName' select='$collName'/>
		  <xsl:with-param name='serviceName' select='$serviceName'/>
		</xsl:apply-templates>
	    </table></td></tr>
	  </xsl:if>
	</xsl:otherwise>
      </xsl:choose>
    </tr></xsl:for-each>
  </xsl:template>


  <xsl:template name="bookshelfimg">
    <xsl:param name="alt"/>
    <xsl:param name="title"/>
    <img  border="0" width="20" height="16"
      src="interfaces/default/images/bshelf.gif"  
      alt="{$alt}" title="{$title}"/>
  </xsl:template>

</xsl:stylesheet>
