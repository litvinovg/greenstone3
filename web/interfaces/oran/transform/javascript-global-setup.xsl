<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util">
	
	<xsl:template name="setup-gs-variable">
		<script type="text/javascript">
			gs = new Array();
			gs.cgiParams = new Array();
			gs.siteMetadata = new Array();
			gs.collectionMetadata = new Array();
			gs.documentMetadata = new Array();
			gs.imageURLs = new Array();
			gs.variables = new Array();
		</script>
		<xsl:call-template name="populate-cgi-param-values"/>
		<xsl:call-template name="populate-image-url-values"/>
		<xsl:call-template name="populate-metadata-values"/>
		<script type="text/javascript">
			alert(gs.cgiParams.c);
			alert(gs.cgiParams.book);
			alert(gs.siteMetadata.siteDescription);
			alert(gs.siteMetadata.fr.siteDescription);
			alert(gs.collectionMetadata.httpPath);
			alert(gs.documentMetadata.assocfilepath);
		</script>
	</xsl:template>
	
	<xsl:template name="populate-cgi-param-values">
		<xsl:for-each select="/page/pageRequest/paramList/param">
			<script type="text/javascript">
				<xsl:text disable-output-escaping="yes">var name = "</xsl:text><xsl:value-of select="@name"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				<xsl:text disable-output-escaping="yes">var value = "</xsl:text><xsl:value-of select="@value"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				<xsl:text disable-output-escaping="yes">name = name.replace(".", "_");</xsl:text>
				gs.cgiParams[name] = value;
			</script>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="populate-metadata-values">
		<script type="text/javascript">
			<xsl:text disable-output-escaping="yes">
				function addMetadataToList(name, value, list, lang)
				{
					name = name.replace(".", "_");
					if(lang == "" || lang == "en")
					{
						list[name] = value;
					}
					else
					{
						if (list[lang] == undefined)
						{
							list[lang] = new Array();
						}
						var langList = list[lang];
						langList[name] = value;
					}
				}
			</xsl:text>
		</script>
		<xsl:for-each select="/page/pageResponse/metadataList/metadata">
			<script type="text/javascript">
				<xsl:text disable-output-escaping="yes">var name = "</xsl:text><xsl:value-of select="@name"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				<xsl:text disable-output-escaping="yes">var value = "</xsl:text><xsl:value-of disable-output-escaping="yes" select="."/><xsl:text disable-output-escaping="yes">";</xsl:text>
				<xsl:text disable-output-escaping="yes">var lang = "</xsl:text><xsl:value-of select="@lang"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				addMetadataToList(name, value, gs.siteMetadata, lang);
			</script>
		</xsl:for-each>
		
		<xsl:for-each select="/page/pageResponse/collection/metadataList/metadata">
			<script type="text/javascript">
				<xsl:text disable-output-escaping="yes">var name = "</xsl:text><xsl:value-of select="@name"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				<xsl:text disable-output-escaping="yes">var value = "</xsl:text><xsl:value-of disable-output-escaping="yes" select="."/><xsl:text disable-output-escaping="yes">";</xsl:text>
				<xsl:text disable-output-escaping="yes">var lang = "</xsl:text><xsl:value-of select="@lang"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				addMetadataToList(name, value, gs.collectionMetadata, lang);
			</script>
		</xsl:for-each>
		
		<xsl:for-each select="/page/pageResponse/document/metadataList/metadata">
			<script type="text/javascript">
				<xsl:text disable-output-escaping="yes">var name = "</xsl:text><xsl:value-of select="@name"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				<xsl:text disable-output-escaping="yes">var value = "</xsl:text><xsl:value-of disable-output-escaping="yes" select="."/><xsl:text disable-output-escaping="yes">";</xsl:text>
				<xsl:text disable-output-escaping="yes">var lang = "</xsl:text><xsl:value-of select="@lang"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				addMetadataToList(name, value, gs.documentMetadata, lang);
			</script>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="populate-image-url-values">
		<script type="text/javascript">
			<xsl:text disable-output-escaping="yes">gs.imageURLs.expand = "</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'expand_image')"/><xsl:text disable-output-escaping="yes">";</xsl:text>
			<xsl:text disable-output-escaping="yes">gs.imageURLs.collapse = "</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'collapse_image')"/><xsl:text disable-output-escaping="yes">";</xsl:text>
			<xsl:text disable-output-escaping="yes">gs.imageURLs.page = "</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'page_image')"/><xsl:text disable-output-escaping="yes">";</xsl:text>
			<xsl:text disable-output-escaping="yes">gs.imageURLs.chapter = "</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'chapter_image')"/><xsl:text disable-output-escaping="yes">";</xsl:text>
			<xsl:text disable-output-escaping="yes">gs.imageURLs.realisticBook = "</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'realistic_books_image')"/><xsl:text disable-output-escaping="yes">";</xsl:text>
			<xsl:text disable-output-escaping="yes">gs.imageURLs.highlight = "</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'highlight_image')"/><xsl:text disable-output-escaping="yes">";</xsl:text>
			<xsl:text disable-output-escaping="yes">gs.imageURLs.bookshelf = "</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'bookshelf_image')"/><xsl:text disable-output-escaping="yes">";</xsl:text>
			<xsl:text disable-output-escaping="yes">gs.imageURLs.book = "</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'book_image')"/><xsl:text disable-output-escaping="yes">";</xsl:text>
			<xsl:text disable-output-escaping="yes">gs.imageURLs.loading = "</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'loading_image')"/><xsl:text disable-output-escaping="yes">";</xsl:text>
			<xsl:text disable-output-escaping="yes">gs.imageURLs.pageIcon = "</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'page_icon_image')"/><xsl:text disable-output-escaping="yes">";</xsl:text>
		</script>
	</xsl:template>
</xsl:stylesheet>

	<!--
	USEFUL FOR TESTING ASSOCIATIVE ARRAYS
	function getKeys(obj)
	{
		var keys = [];
		for(var key in obj)
		{
			keys.push(key);
		}
		return keys;
	}
	-->

	<!-- CAN WE FIND SOME WAY TO MAKE THIS WORK?
	<xsl:call-template name="populate-metadata-values">
		<xsl:with-param name="path">/page/pageResponse/metadataList/metadata</xsl:with-param>
		<xsl:with-param name="metadataListName">siteMetadata</xsl:with-param>
	</xsl:call-template>
	
	<xsl:call-template name="populate-metadata-values">
		<xsl:with-param name="path">/page/pageResponse/collection/metadataList/metadata</xsl:with-param>
		<xsl:with-param name="metadataListName">collectionMetadata</xsl:with-param>
	</xsl:call-template>
	
	<xsl:call-template name="populate-metadata-values">
		<xsl:with-param name="path">/page/pageResponse/document/metadataList/metadata</xsl:with-param>
		<xsl:with-param name="metadataListName">documentMetadata</xsl:with-param>
	</xsl:call-template>
	-->

	<!-- NOT WORKING BUT IT WOULD BE TIDIER IF IT DID
	<xsl:template name="populate-metadata-values">
		<xsl:param name="path"/>
		<xsl:param name="metadataListName"/>
		
		<xsl:for-each select="$path">
			<script type="text/javascript">
				<xsl:text disable-output-escaping="yes">var name = "</xsl:text><xsl:value-of select="@name"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				<xsl:text disable-output-escaping="yes">var value = "</xsl:text><xsl:value-of select="."/><xsl:text disable-output-escaping="yes">";</xsl:text>
				<xsl:text disable-output-escaping="yes">var lang = "</xsl:text><xsl:value-of select="@lang"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				<xsl:text disable-output-escaping="yes">var metadataListName = "</xsl:text><xsl:value-of select="$metadataListName"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				var list = gs[metadataListName];
				if(lang == "")
				{
					list[name] = value;
				}
				else
				{
					if (list[lang] == undefined)
					{
						list[lang] = new Array();
					}
					var langList = list[lang];
					langList[name] = value;
				}
			</script>
		</xsl:for-each>
	</xsl:template>
	-->