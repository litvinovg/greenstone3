<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">


  <xsl:template match="collection" mode="simplehelp">
    <xsl:variable name="longCollName"><xsl:value-of select="displayItem[@name='name']" disable-output-escaping='yes'/></xsl:variable>
    <!--<xsl:variable name="numoptions">5</xsl:variable>-->
    <xsl:variable name="has_search"><xsl:choose><xsl:when test="serviceList/service[@name='TextQuery']">1</xsl:when><xsl:otherwise>0</xsl:otherwise></xsl:choose></xsl:variable>
    <xsl:variable name="has_phrase"><xsl:choose><xsl:when test="serviceList/service[@name='PhindApplet']">1</xsl:when><xsl:otherwise>0</xsl:otherwise></xsl:choose></xsl:variable>
    <xsl:variable name="numoptions" select="count(serviceList/service[@name='ClassifierBrowse']/classifierList[position() mod 2 = 1]/classifier)"/>
    <div class="simplehelp">
      <h3><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help.simplehelpheading', $longCollName)"/></h3>
      <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help.numbrowseoptions', $numoptions+$has_search+$has_phrase)"/>
      <ul>
	<xsl:if test="$has_search =1">
	  <li><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help.Searchshort')" disable-output-escaping='yes'/></li>
	</xsl:if>
	<xsl:for-each select="serviceList/service[@name='ClassifierBrowse']/classifierList/classifier">
	  <xsl:variable name="title">help.<xsl:value-of select="@content"/>short</xsl:variable>
	  <li><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, $title)" disable-output-escaping='yes'/></li>
	</xsl:for-each>
	<xsl:if test="$has_phrase =1">
	  <li><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help.Phraseshort')" disable-output-escaping='yes'/></li>
	</xsl:if>
      </ul>
      <xsl:if test="$has_search =1">
	<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help.Searchlong')" disable-output-escaping='yes'/>
      </xsl:if>
      <xsl:for-each select="serviceList/service[@name='ClassifierBrowse']/classifierList/classifier">
	<xsl:variable name="title">help.<xsl:value-of select="@content"/>long</xsl:variable>
	<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, $title)" disable-output-escaping='yes'/>
      </xsl:for-each>
      <xsl:if test="$has_phrase =1">
	<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help.Phraselong')" disable-output-escaping='yes'/>
      </xsl:if>
    </div>
  </xsl:template>
  
  
</xsl:stylesheet>