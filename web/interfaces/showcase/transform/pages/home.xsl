<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util">

	<!-- the custom list of collections -->
	<xsl:template name="collectionAndGroupLinks">
		<div id="collectionLinks">
			<xsl:if test="count(collectionList/collection) = 0">
				<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home.no_collections')"/>
				<br/>
			</xsl:if>
			<p>These collections demonstrate different aspects of Greenstone3. Click on a collection to see its description.</p>
			<xsl:for-each select="collectionList/collection[@name = 'paradise-gardens' or @name = 'image-demo' or @name = 'twso' or @name = 'paged-image' or @name = 'fedora' or @name= 'editdemo' or @name = 'kjcoll' or @name='niupepa']">
			<gslib:collectionLinkWithImage/>
			</xsl:for-each>		 
			<br class="clear"/>
			<p>The next collections all use a different indexing tool, but otherwise have the same content. Do some searches to see the what the different tools offer.</p>
			<xsl:for-each select="collectionList/collection[@name = 'lucene-jdbm-demo' or @name = 'solr-jdbm-demo' or @name = 'gs2mgppdemo' or @name='gs2mgdemo']">
			  <gslib:collectionLinkWithImage/>
			</xsl:for-each>
			<br class="clear"/>
		</div>
	</xsl:template>


</xsl:stylesheet>
