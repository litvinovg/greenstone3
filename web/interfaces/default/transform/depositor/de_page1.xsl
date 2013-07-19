<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util">
	
	<!-- set page title -->
	<xsl:variable name="title">Specify Metadata</xsl:variable>

	<!-- the page content -->
	<xsl:template name="wizardPage">
		<table>
			<tr>
				<td>Title:</td><td><input type="text" name="md___dc.Title" size="74"/></td>
			</tr>
			<tr>
				<td>Creator:</td><td><input type="text" name="md___dc.Creator"/></td>
			</tr>
			<tr>
				<td>Description:</td><td><textarea cols="40" rows="5" name="md___dc.Description"><xsl:text> </xsl:text></textarea></td>
			</tr>
			<tr>
				<td><div id="clearSaved" style="padding:0 10px; cursor:pointer;" class="ui-state-default ui-corner-all">Clear all saved data</div></td>
				<script type="text/javascript">
					<xsl:text disable-output-escaping="yes">
						$("#clearSaved").click(function()
						{
							$.ajax(gs.xsltParams.library_name + "?a=de&amp;sa=clearcache")
							.success(function()
							{
								console.log("CLEARED CACHE");
								document.location.href = gs.xsltParams.library_name + "?a=de&amp;sa=getwizard&amp;depage=1&amp;c=" + gs.cgiParams.c;
							});
						});
					</xsl:text>
				</script>
			</tr>
		</table>
	</xsl:template>
</xsl:stylesheet>  

