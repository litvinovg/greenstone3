<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:java="http://xml.apache.org/xslt/java"
    xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
    extension-element-prefixes="java"
    exclude-result-prefixes="java gsf">
    
    <xsl:output method="html" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" />
    
    <xsl:template match="page">
        <xsl:for-each select="//documentNode">
            <xsl:if test="nodeContent">
            <xsl:if test="metadataList/metadata[@name='Title']">
                <h3><xsl:value-of disable-output-escaping="yes" select="metadataList/metadata[@name='Title']"/></h3>
            </xsl:if>
            <xsl:value-of select="nodeContent" />
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
    
</xsl:stylesheet>
