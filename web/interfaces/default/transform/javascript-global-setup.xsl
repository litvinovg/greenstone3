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
			gs.xsltParams = new Array();
			gs.siteMetadata = new Array();
			gs.collectionMetadata = new Array();
			gs.documentMetadata = new Array();
			gs.imageURLs = new Array();
			gs.variables = new Array();
			gs.requestInformation = new Array();
		</script>
		<xsl:call-template name="populate-cgi-param-values"/>
		<xsl:call-template name="populate-xslt-param-values"/>
		<xsl:call-template name="populate-image-url-values"/>
		<xsl:call-template name="populate-metadata-values"/>
		<xsl:call-template name="populate-request-information-values"/>
		<xsl:call-template name="include-global-javascript-functions"/>
	</xsl:template>
	
	<xsl:template name="populate-cgi-param-values">
		<script type="text/javascript">
			var name;
			var value;
			<xsl:for-each select="/page/pageRequest/paramList/param">
				<xsl:text disable-output-escaping="yes">name = "</xsl:text><xsl:value-of select="@name"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				<xsl:text disable-output-escaping="yes">value = "</xsl:text><xsl:value-of select="util:escapeNewLinesAndQuotes(@value)"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				<xsl:text disable-output-escaping="yes">name = name.replace(".", "_");</xsl:text>
				gs.cgiParams[name] = value;
			</xsl:for-each>
		</script>
	</xsl:template>
	
	<xsl:template name="populate-xslt-param-values">
		<script type="text/javascript">
			<xsl:text disable-output-escaping="yes">gs.xsltParams.library_name = "</xsl:text><xsl:value-of select="$library_name"/><xsl:text disable-output-escaping="yes">";</xsl:text>
			<xsl:text disable-output-escaping="yes">gs.xsltParams.interface_name = "</xsl:text><xsl:value-of select="$interface_name"/><xsl:text disable-output-escaping="yes">";</xsl:text>
			<xsl:text disable-output-escaping="yes">gs.xsltParams.site_name = "</xsl:text><xsl:value-of select="$site_name"/><xsl:text disable-output-escaping="yes">";</xsl:text>
		</script>
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
				var name;
				var value;
				var lang;
			</xsl:text>
			<xsl:for-each select="/page/pageResponse/metadataList/metadata">
				<xsl:text disable-output-escaping="yes">name = "</xsl:text><xsl:value-of select="@name"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				<xsl:text disable-output-escaping="yes">value = "</xsl:text><xsl:value-of disable-output-escaping="yes" select="util:escapeNewLinesAndQuotes(.)"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				<xsl:text disable-output-escaping="yes">lang = "</xsl:text><xsl:value-of select="@lang"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				addMetadataToList(name, value, gs.siteMetadata, lang);
			</xsl:for-each>
		
			<xsl:for-each select="/page/pageResponse/collection/metadataList/metadata">
				<xsl:text disable-output-escaping="yes">name = "</xsl:text><xsl:value-of select="@name"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				<xsl:text disable-output-escaping="yes">value = "</xsl:text><xsl:value-of disable-output-escaping="yes" select="util:escapeNewLinesAndQuotes(.)"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				<xsl:text disable-output-escaping="yes">lang = "</xsl:text><xsl:value-of select="@lang"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				addMetadataToList(name, value, gs.collectionMetadata, lang);
			</xsl:for-each>
		
			<xsl:for-each select="/page/pageResponse/document/metadataList/metadata">
				<xsl:text disable-output-escaping="yes">name = "</xsl:text><xsl:value-of select="@name"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				<xsl:text disable-output-escaping="yes">value = "</xsl:text><xsl:value-of disable-output-escaping="yes" select="util:escapeNewLinesAndQuotes(.)"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				<xsl:text disable-output-escaping="yes">lang = "</xsl:text><xsl:value-of select="@lang"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				addMetadataToList(name, value, gs.documentMetadata, lang);
			</xsl:for-each>
			
			<xsl:for-each select="/page/pageResponse/classifier/documentNode">
				{
				<xsl:text disable-output-escaping="yes">var nodeID = "</xsl:text><xsl:value-of select="@nodeID"/><xsl:text disable-output-escaping="yes">";</xsl:text>
				<xsl:text disable-output-escaping="yes">var emptyLang = "";</xsl:text>
				<xsl:text disable-output-escaping="yes">var metaList = new Array();</xsl:text>
				<xsl:for-each select="metadataList/metadata">
					<xsl:text disable-output-escaping="yes">name = "</xsl:text><xsl:value-of select="@name"/><xsl:text disable-output-escaping="yes">";</xsl:text>
					<xsl:text disable-output-escaping="yes">value = "</xsl:text><xsl:value-of disable-output-escaping="yes" select="util:escapeNewLinesAndQuotes(.)"/><xsl:text disable-output-escaping="yes">";</xsl:text>
					<xsl:text disable-output-escaping="yes">lang = "</xsl:text><xsl:value-of select="@lang"/><xsl:text disable-output-escaping="yes">";</xsl:text>
					addMetadataToList(name, value, metaList, lang);
				</xsl:for-each>
				addMetadataToList(nodeID, metaList, gs.documentMetadata, emptyLang);
				}
			</xsl:for-each>
			
			<xsl:text disable-output-escaping="yes">addMetadataToList("docType", "</xsl:text><xsl:value-of select="/page/pageResponse/document/@docType"/><xsl:text disable-output-escaping="yes">", gs.documentMetadata, "</xsl:text><xsl:value-of select="@lang"/><xsl:text disable-output-escaping="yes">");</xsl:text>
		</script>
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
			<xsl:text disable-output-escaping="yes">gs.imageURLs.trashFull = "</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'trash_full_image')"/><xsl:text disable-output-escaping="yes">";</xsl:text>
			<xsl:text disable-output-escaping="yes">gs.imageURLs.blank = "</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'blank_image')"/><xsl:text disable-output-escaping="yes">";</xsl:text> 
			<xsl:text disable-output-escaping="yes">gs.imageURLs.next = "</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'next_image')"/><xsl:text disable-output-escaping="yes">";</xsl:text>
			<xsl:text disable-output-escaping="yes">gs.imageURLs.prev = "</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'prev_image')"/><xsl:text disable-output-escaping="yes">";</xsl:text>
			<xsl:text disable-output-escaping="yes">gs.imageURLs.trashEmpty = "</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'trash_empty_image')"/><xsl:text disable-output-escaping="yes">";</xsl:text>
			<xsl:text disable-output-escaping="yes">gs.imageURLs.trashFull = "</xsl:text><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'trash_full_image')"/><xsl:text disable-output-escaping="yes">";</xsl:text>
		</script>
	</xsl:template>
	
	<xsl:template name="populate-request-information-values">
		<script type="text/javascript">
			<xsl:text disable-output-escaping="yes">
				gs.requestInformation.fullURL = "</xsl:text><xsl:value-of select="/page/pageRequest/@fullURL"/><xsl:text disable-output-escaping="yes">";
			</xsl:text>
		</script>
	</xsl:template>
	
	<xsl:template name="include-global-javascript-functions">
		<script type="text/javascript" src="interfaces/default/js/javascript-global-functions.js"><xsl:text> </xsl:text></script>
		<script type="text/javascript" src="interfaces/default/js/GSMetadata.js"><xsl:text> </xsl:text></script>
	</xsl:template>
</xsl:stylesheet>

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