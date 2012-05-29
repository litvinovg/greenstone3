<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util">
	
	<xsl:template match="param" mode="calculate-default">
		<xsl:param name="ns">s1.</xsl:param>
		<xsl:variable name="pname"><xsl:value-of select="$ns"/><xsl:value-of select="@name"/></xsl:variable>
		<xsl:choose>
			<xsl:when test="/page/pageRequest/paramList/param[@name=$pname]">
				<xsl:choose>
					<xsl:when test="@type='enum_multi'"><xsl:text>,</xsl:text>
						<xsl:for-each select="/page/pageRequest/paramList/param[@name=$pname]">
							<xsl:value-of select="@value"/>,
						</xsl:for-each>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="/page/pageRequest/paramList/param[@name=$pname]/@value"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="@default"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- single selection enum params -->
	<xsl:template match="param[@type='enum_single']">
		<xsl:param name="ns">s1.</xsl:param>
		<xsl:param name="default"/>
		<xsl:choose>
			<xsl:when test="count(option) = 1">
				<xsl:value-of select="option/displayItem[@name='name']"/>
				<input type='hidden' name='{$ns}{@name}'><xsl:attribute name='value'><xsl:value-of  select='option/@name'/></xsl:attribute></input>
			</xsl:when>
			<xsl:otherwise>
				<select name="{$ns}{@name}">
					<xsl:for-each select="option">
						<option value="{@name}"><xsl:if test="@name=$default"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="displayItem[@name='name']"/></option>
					</xsl:for-each>
				</select>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="param[@type='string']">
		<xsl:param name="ns">s1.</xsl:param>
		<xsl:param name="default"/>
		<input type="text" name="{$ns}{@name}" size="30" value="{$default}"/>
	</xsl:template>
</xsl:stylesheet>