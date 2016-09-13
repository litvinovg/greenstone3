<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util">
  
	<!-- use the 'main' layout -->
	<xsl:include href="layouts/main.xsl"/>

	<!-- set page title -->
	<xsl:template name="pageTitle"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'login_b')"/></xsl:template>

	<!-- set page breadcrumbs -->
	<xsl:template name="breadcrumbs"><gslib:siteLink/></xsl:template>

	<!-- the page content -->
	<xsl:template match="/page">
		<xsl:if test="/page/pageRequest/paramList/param[@name = 'loginMessage']/@value">
			<div id="gs_error" class="ui-state-error ui-corner-all">
				<span class="ui-icon ui-icon-alert" style="float: left;"><xsl:text> </xsl:text></span><xsl:value-of select="/page/pageRequest/paramList/param[@name = 'loginMessage']/@value"/>
			</div>
		<br/>
		</xsl:if>
		<form method="POST" action="{/page/pageRequest/paramList/param[@name = 'redirectURL']/@value}">
			<table id="loginTable">
				<tr><td><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.username')"/>: </td><td><input type="text" name="username"/></td></tr>
				<tr><td><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.password')"/>: </td><td><input type="password" name="password"/></td></tr>
				<tr><td><input type="submit"><xsl:attribute name="value"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'login_b')"/></xsl:attribute></input></td><td><xsl:text> </xsl:text></td></tr>
			</table>
		</form>
		<script type="text/javascript">
			<xsl:text disable-output-escaping="yes">
			{
				$("#loginTable input[name=\"username\"]").focus();
			}
			</xsl:text>
		</script>
	</xsl:template>
</xsl:stylesheet>
