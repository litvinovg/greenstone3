<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util">
	
	<!-- set page title -->
	<xsl:variable name="title">Deposit Item</xsl:variable>

	<!-- the page content -->
	<xsl:template name="wizardPage">
		<div id="progressBar">Please wait...</div>
		<script type="text/javascript">
			<xsl:text disable-output-escaping="yes">
				{
					$(window).load(function()
					{
						$("#progressBar").html($("#progressBar").html() + "&lt;br/&gt;Moving file into collection...");
						var url = gs.xsltParams.library_name + "?a=de&amp;sa=depositFile&amp;ro=1&amp;c=" + gs.cgiParams.c + "&amp;fileToAdd=" + gs.deSavedMetadata["md___ex.Filename"];
						$.ajax(url).success(function(response)
						{
							$("#progressBar").html($("#progressBar").html() + "&lt;br/&gt;Importing collection...");
							var xml;
							try
							{
								xml = $.parseXML(response.replace(/&amp;/g, "&amp;amp;"));
							}
							catch(e)
							{
								console.log("ERROR: " + e);
								$("#progressBar").html($("#progressBar").html() + "&lt;br/&gt;There was an error on the server.");
								return;
							}							
							
							if(!($(xml).find("error").length))
							{
								var pid = $(xml).find("status").attr("pid");
								gs.functions.startCheckLoop(pid, "ImportCollection", function()
								{
									$("#progressBar").html($("#progressBar").html() + "&lt;br/&gt;Building collection...");
									gs.functions.buildCollections([gs.cgiParams.c], function()
									{
										$("#progressBar").html($("#progressBar").html() + "&lt;br/&gt;Collection built...");
										$("#progressBar").html($("#progressBar").html() + "&lt;br/&gt;Reloading Greenstone...");
										$.ajax(gs.xsltParams.library_name + "?a=s&amp;sa=c").success(function()
										{
											$("#progressBar").html($("#progressBar").html() + "&lt;br/&gt;Done!");
										})
										.error(function()
										{
											$("#progressBar").html($("#progressBar").html() + "&lt;br/&gt;Reload failed, please restart Greenstone manually.");
										});
									});
								});
							}
							else
							{
								$("#progressBar").html($("#progressBar").html() + "&lt;br/&gt;There was an error depositing your file:");
								$(xml).find("error").each(function()
								{
									$("#progressBar").html($("#progressBar").html() + "&lt;br/&gt;" + $(this).text());
								});
							}
						});
					});
				}
			</xsl:text>
		</script>
	</xsl:template>
</xsl:stylesheet>  

