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
	<xsl:template name="pageTitle">Depositor</xsl:template>

	<!-- set page breadcrumbs -->
	<xsl:template name="breadcrumbs"/>

	<xsl:template match="/page">
		<div style="display:table; margin: 0 auto">
			Please select a collection:
			<select id="colSelect">
				<option>-- Select a collection --</option>
				<xsl:for-each select="/page/pageResponse/depositorPage/collectionList/collection">
					<option>
						<xsl:value-of select="@name"/>
					</option>
				</xsl:for-each>
			</select>

			<script type="text/javascript">
				<xsl:text disable-output-escaping="yes">
					$("#colSelect").change(function()
					{
						var selected = $("#colSelect").find(":selected");
						if (selected.text() != "-- Select a collection --")
						{
							document.location.href = gs.xsltParams.library_name + "?a=de&amp;sa=getwizard&amp;c=" + selected.text();
						}
					});
				</xsl:text>
			</script>
		</div>
	</xsl:template>
</xsl:stylesheet>  

