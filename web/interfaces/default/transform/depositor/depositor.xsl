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

	<!-- Used to store how many pages are present in the depositor -->
	<xsl:variable name="numOfPages">5</xsl:variable>
	
	<!-- set page title -->
	<xsl:template name="pageTitle">Depositor</xsl:template>

	<!-- set page breadcrumbs -->
	<xsl:template name="breadcrumbs"></xsl:template>

	<!-- the page content -->
	<xsl:template match="/page">
		<xsl:call-template name="depositorJavascript"/>
		<div id="wizardContainer"><xsl:text> </xsl:text>
			<form enctype="multipart/form-data" method="post" name="depositorform" id="depositorform">
				<xsl:attribute name="action"><xsl:value-of select="$library_name"/></xsl:attribute>
				<input type="hidden" name="a" value="de"/>
				<input type="hidden" name="sa" value="getwizard"/>
				<input type="hidden" name="c" value="{/page/pageResponse/collection/@name}"/>
				<input type="hidden" name="currentPage" value="{/page/pageRequest/paramList/param[@name='dePage']/@value}"/>
				<!-- A CALL TO THE APPROPRIATE PAGE ADDED ON THE SERVER -->
			</form>
		</div>
		<xsl:call-template name="wizardBar"/>
	</xsl:template>
	
	<xsl:template name="wizardBar">
		<!-- CREATED ON THE SERVER -->
	</xsl:template>
	
	<xsl:template name="depositorJavascript">
		<script type="text/javascript">
			<xsl:text disable-output-escaping="yes">
				{
					$(window).load(function()
					{
						var cachedMetadata = [];
						var cachedPageMetaString;

						</xsl:text>
						<xsl:for-each select="/page/pageResponse/cachedValues/pageCache">
							<xsl:text disable-output-escaping="yes">cachedPageMetaString = '</xsl:text><xsl:value-of select="."/><xsl:text disable-output-escaping="yes">';
							cachedPageMetaString = cachedPageMetaString.replace(/&amp;lt;/g, "&lt;").replace(/&amp;gt;/g, "&gt;").replace(/&amp;amp;/g, "&amp;").replace(/&amp;quot;/g, "\"");
							cachedMetadata.push(eval(cachedPageMetaString));</xsl:text>
						</xsl:for-each>
						<xsl:text disable-output-escaping="yes">

						//console.log(cachedMetadata);
						gs.deSavedMetadata = [];
						for(var j = 0; j &lt; cachedMetadata.length; j++)
						{
							var currentPageCache = cachedMetadata[j];
							for(var i = 0; i &lt; currentPageCache.length; i++)
							{
								gs.deSavedMetadata[currentPageCache[i].name] = currentPageCache[i].value;
								$('form [name="' + currentPageCache[i].name + '"]').val(currentPageCache[i].value);
							}
						}

						$(".wizardStepLink a").click(function()
						{
							var requestedPage = $(this).attr("page");
							var form = $("#depositorform");
							form.append($("&lt;input&gt;").attr({type:"hidden", name:"dePage", value:requestedPage}));
							form.submit();
						});
						
						$("[gs-metadata]").each(function()
						{
							var metaVal = gs.deSavedMetadata["md___" + $(this).attr("gs-metadata")];
							if(metaVal)
							{
								$(this).text(metaVal);
							}
						});
					});
				}
			</xsl:text>
		</script>
	</xsl:template>
</xsl:stylesheet>  

