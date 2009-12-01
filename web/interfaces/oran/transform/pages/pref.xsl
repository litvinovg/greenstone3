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
	<xsl:template name="pageTitle"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_b')"/></xsl:template>

	<!-- set page breadcrumbs -->
	<xsl:template name="breadcrumbs"><gslib:siteLink/><gslib:rightArrow/> <gslib:collectionNameLinked/><gslib:rightArrow/></xsl:template>

	<!-- the page content -->
	<xsl:template match="/page">

		<xsl:variable name="collName" select="/page/pageRequest/paramList/param[@name='c']/@value"/>
		<xsl:variable name="tidyoption"><xsl:value-of select="/page/pageResponse/collection/metadataList/metadata[@name='tidyoption']"/></xsl:variable>

		<div id="queryform">
			<form name="PrefForm" method="get" action="{$library_name}">

				<input type='hidden' name='a' value='p'/>
				<input type='hidden' name='sa' value='pref'/>
				<input type='hidden' name='c' value="{$collName}"/>

				<!-- presentation preferences -->
				<h3 class="formheading"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.prespref')"/></h3>
				<div id="presprefs">

					<!-- language -->
					<div class="paramLabel">
						<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.interfacelang')"/>
					</div>
					<div class="paramValue">
						<select name="l">
							<xsl:for-each select="/page/pageResponse/languageList/language">
								<option value="{@name}"><xsl:if test="@name=/page/@lang"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if><xsl:value-of select="displayItem[@name='name']"/></option>
							</xsl:for-each>
						</select>

					</div>
					<br class="clear"/>

					<!-- encoding -->
					<div class="paramValue">
						<span class="rightspace"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.encoding')"/></span>
					</div>
					<br class="clear"/>

				</div>

				<!-- berry baskets -->
				<div id="berrybasketprefs">
					<xsl:variable name="berrybasket"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='berrybasket']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='berrybasket']/@value"/></xsl:when><xsl:otherwise>off</xsl:otherwise></xsl:choose></xsl:variable>
					<div class="paramLabel">
						<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.berrybasket')"/>
					</div>
					<div class="paramValue">
						<select name="berrybasket">
							<option value="on"><xsl:if test="$berrybasket='on'"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.berrybasket.on')"/></option> 
							<option value="off"><xsl:if test="$berrybasket='off'"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.berrybasket.off')"/></option>
						</select>
					</div>
					<br class="clear"/>
				</div>

				<!-- tidy (?) -->
				<xsl:if test="$tidyoption='tidy'">
					<xsl:variable name="book"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='book']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='book']/@value"/></xsl:when><xsl:otherwise>off</xsl:otherwise></xsl:choose></xsl:variable>
					<div class="paramLabel">
						<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.book')"/>
					</div>
					<div class="paramValue">
						<select name="book">
							<option value="on"><xsl:if test="$book='on'"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.berrybasket.on')"/></option> 
							<option value="off"><xsl:if test="$book='off'"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.berrybasket.off')"/></option>
						</select>
					</div>
					<br class="clear"/>
				</xsl:if>

				<!-- search preferences -->
				<h3><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.searchpref')"/></h3>
				<div id="searchprefs">
					<xsl:variable name="hits"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='hitsPerPage']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='hitsPerPage']/@value"/></xsl:when><xsl:otherwise>20</xsl:otherwise></xsl:choose></xsl:variable> 
					<div class="paramLabel">
						<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.hitsperpage')"/>
					</div>
					<div class="paramValue">
						<select name="hitsPerPage">
							<option value="20"><xsl:if test="$hits=20"><xsl:attribute name="selected"></xsl:attribute></xsl:if>20</option> 
							<option value="50"><xsl:if test="$hits=50"><xsl:attribute name="selected"></xsl:attribute></xsl:if>50</option>
							<option value="100"><xsl:if test="$hits=100"><xsl:attribute name="selected"></xsl:attribute></xsl:if>100</option>
							<option value="-1"><xsl:if test="$hits=-1"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.all')"/></option>
						</select>
					</div>
					<br class="clear"/>
				</div>

				<br/>

				<input type='submit'><xsl:attribute name="value"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.set_prefs')"/></xsl:attribute></input>

			</form>
		</div>

	</xsl:template>

</xsl:stylesheet>
