<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:gslib="http://www.greenstone.org/XSL/Library"
exclude-result-prefixes="xalan gslib">

<!-- exclude-result-prefixes="xalan gslib" -->

<!-- general ............................................................................. -->

<xsl:variable name="librarian"></xsl:variable>

<xsl:variable name="action" select="/page/pageRequest/@action"/>

<xsl:variable name="subaction" select="/page/pageRequest/@subaction"/>

<xsl:variable name="lang" select="/page/pageRequest/@lang"/>

<xsl:variable name="css">
 <xsl:for-each select="/page/pageResponse/cssFileList/cssFile">
  <link rel="stylesheet" href="{@path}" type="text/css"/>
 </xsl:for-each>
</xsl:variable>

<xsl:variable name="js">
 <xsl:for-each select="/page/pageResponse/jsFileList/jsFile">
 	<script type="text/javascript" src="{@path}"><xsl:comment>IE needs to have an explicity start and end script tag.  This comment is here to ensure the end script tag does not get collapsed into a single open-close in one go element</xsl:comment></script>
 </xsl:for-each>
</xsl:variable>

<xsl:template name="metadataItem">
  <xsl:param name="name"/>

   <xsl:variable name="x" select="/page/pageResponse/metadataList/metadataItem[@name=$name]" />


  <xsl:choose>
   <xsl:when test="$x = ''">
    metadataItem '<xsl:value-of select="$name"/>' not defined.
   </xsl:when>
   <xsl:otherwise>
    <xsl:for-each select="$x">
     <xsl:call-template name="expandMetadataAndUiItems" />
    </xsl:for-each>
   </xsl:otherwise>
  </xsl:choose>
  
</xsl:template>

<!-- get the uiItem for the given name -->
<xsl:template name="uiItem">
  <xsl:param name="name"/>

  <xsl:variable name="x" select="/page/pageResponse/uiItemList/uiItem[@name=$name]" />
  
  <xsl:choose>
   <xsl:when test="$x = ''">
    uiItem '<xsl:value-of select="$name"/>' not defined.
   </xsl:when>
   <xsl:otherwise>
    <xsl:for-each select="$x">
     <xsl:call-template name="expandMetadataAndUiItems" />
    </xsl:for-each>
   </xsl:otherwise>
  </xsl:choose>

</xsl:template>

<!-- site level stuff ....................................................................... -->

<xsl:variable name="collections" select="/page/pageResponse/collectionList/collection"/>

<xsl:template name="collection_title">
	<xsl:value-of disable-output-escaping='yes' select="./displayItem[@name='name']"/>
</xsl:template>

<xsl:template name="collection_description">
	<xsl:value-of disable-output-escaping='yes' select="./displayItem[@name='description']"/>
</xsl:template>

<xsl:template name="collection_url">
<xsl:value-of select="$librarian" />?a=p&amp;sa=about&amp;
c=<xsl:value-of select="@name" />
</xsl:template>

<xsl:variable name="site_homeUrl">
<xsl:value-of select="$librarian" />?a=p&amp;sa=home
</xsl:variable>

<!-- collection level stuff ....................................................................... -->

<xsl:variable name="collection" select="/page/pageResponse/collection/@name"/>

<xsl:variable name="collection_homeUrl">
<xsl:value-of select="$librarian" />?a=p&amp;sa=about&amp;
c=<xsl:value-of select="$collection"/>
</xsl:variable>

<xsl:variable name="services" select="/page/pageResponse/collection/serviceList/service[@type='query' or @type='browse']"/>

<xsl:template name="service_title">
	<xsl:param name="serviceName"/>
	<xsl:value-of select="./displayItem[@name='name']"/>
</xsl:template>

<xsl:template name="service_description">
	<xsl:param name="serviceName"/>
	<xsl:value-of select="./displayItem[@name='description']"/>
</xsl:template>

<xsl:template name="service_url">
	<xsl:variable name="serviceAction">
   		<xsl:choose>
			<!-- this hard coding doesnt seem like a good idea: why isnt the appropriate action given as an attribute of the service? -->
    		<xsl:when test="@type='query'">q</xsl:when>
    		<xsl:when test="@type='browse'">b</xsl:when>
    		<xsl:when test="@type='process'">pr</xsl:when>
    		<xsl:when test="@type='applet'">a</xsl:when>
    		<xsl:otherwise>DO_NOT_DISPLAY</xsl:otherwise>
   		</xsl:choose>
  	</xsl:variable>
	
<xsl:value-of select="$librarian"/>?
a=<xsl:value-of select="$serviceAction"/>&amp;rt=d&amp;
s=<xsl:value-of select="@name"/>&amp;
c=<xsl:value-of select="$collection"/>
</xsl:template>

<xsl:variable name="classifiers" select="/page/pageResponse/collection/serviceList//classifier"/>

<xsl:template name="classifier_title">
	<xsl:value-of select="./displayItem[@name='name']"/>
</xsl:template>

<xsl:template name="classifier_description">
	<xsl:value-of select="./displayItem[@name='description']"/>
</xsl:template>

<xsl:template name="classifier_url">
<xsl:value-of select="$librarian"/>?a=b&amp;rt=r&amp;
s=<xsl:value-of select="../../@name"/>&amp;
c=<xsl:value-of select="$collection"/>&amp;
cl=<xsl:value-of select="@name"/>
</xsl:template>

<!-- searching ............................................................................. -->

<xsl:variable name="search_hiddenInputs">
	<input name="a" type="hidden" value="q" />
    <input name="sa" type="hidden" value="" />
    <input name="rt" type="hidden" value="rd" />
    <input name="s" type="hidden" value="TextQuery" />
    <input name="c" type="hidden" value="{$collection}" />
    <input name="startPage" type="hidden" value="1" />
</xsl:variable>

<xsl:variable name="search_docsReturned" select="/page/pageResponse/metadataList/metadata[@name='numDocsReturned']" />

<xsl:variable name="search_startIndex" select="'1'"/>

<xsl:variable name="search_endIndex" select="'20'"/>



<xsl:variable name="search_query" select="/page/pageResponse/metadataList/metadata[@name='query']" />

<xsl:variable name="search_queryTerms" select="/page/pageResponse/termList/term"/>

<xsl:template name="search_queryTerm_text">
	<xsl:value-of select="./@name"/>
</xsl:template>

<xsl:template name="search_queryTerm_freq">
	<xsl:value-of select="./@freq"/>
</xsl:template>


<xsl:variable name="search_results" select="/page/pageResponse/documentNodeList/documentNode"/>

<xsl:template name="metadata">
	<xsl:param name="name"/>
	
	<xsl:value-of disable-output-escaping="yes" select="./metadataList/metadata[@name=$name]" />
</xsl:template>

<xsl:template name="search_param_li">
	<xsl:param name="search_param"/>
		<li>
			<xsl:for-each select="$search_param/displayItem[@name='name']">
				<xsl:value-of select="." />				
			</xsl:for-each>
			
			<xsl:choose>
				
				<xsl:when test="$search_param/@type='boolean' or $search_param/@type='enum_single'">
					<select name="s1.{$search_param/@name}">
						<xsl:for-each select="$search_param/option">
							<option name="{@name}">
								<xsl:for-each select="./displayItem[@name='name']">
									<xsl:value-of select="." />				
								</xsl:for-each>
							</option>
						</xsl:for-each>
					</select>
				</xsl:when>
				
		
				<xsl:when test="$search_param/@type='integer'">
					<input name="s1.{$search_param/@name}" value="{$search_param/@default}"/>			
				</xsl:when>
				
				<xsl:when test="$search_param/@type='invisible'">
					<input name="s1.{$search_param/@name}" value="{$search_param/@default}" type="hidden"/>			
				</xsl:when>
				
			</xsl:choose>
  	</li>
	
</xsl:template>










<!-- browsing ............................................................................. -->

<xsl:variable name="currentClassifier" select="/page/pageResponse/collection/serviceList//classifier[@name = /page/pageResponse/classifier/@name]" />

<xsl:variable name="topLevelClassifierNodes" select="/page/pageResponse/classifier/classifierNode"/>

<xsl:variable name="topLevelClassifierNodeIds" select="/page/pageResponse/classifier/classifierNode/@nodeID"/>

<xsl:template name="classifierNode_title">
	<xsl:value-of select="./metadataList/metadata[@name='Title']"/>
</xsl:template>


<xsl:template name="classifierNode_url">

<xsl:value-of select="$librarian"/>
?a=b&amp;rt=r&amp;
s=<xsl:value-of select="/page/pageResponse/service/@name"/>&amp;
c=<xsl:value-of select="$collection"/>&amp;
cl=<xsl:value-of select="@nodeID" /><xsl:if test="classifierNode|documentNode">.pr</xsl:if>

</xsl:template>


<xsl:template name="documentNode_title">

<xsl:value-of select="./metadataList/metadata[@name='Title']"/>
	
</xsl:template>


<xsl:template name="documentNode_url">

     
	  
<xsl:value-of select="$librarian"/>?a=d&amp;
c=<xsl:value-of select="$collection"/>&amp;
d=<xsl:value-of select="@nodeID"/>&amp;
<!-- dt=<xsl:value-of select="@docType"/>&amp; -->
sib=1&amp;
p.a=b&amp;
p.s=ClassifierBrowse

</xsl:template>







<!-- document viewing..................................................................... -->

<xsl:variable name="document_coverImage_exists" select="/page/pageResponse/document/@hasCoverImage" />

<xsl:variable name="document_coverImage_url">
	<xsl:if test="$document_coverImage_exists">
		<xsl:value-of select="/page/pageResponse/collection/metadataList/metadata[@name='httpPath']"/>/index/assoc/<xsl:value-of select="normalize-space(/page/pageResponse/document/metadataList/metadata[@name='archivedir'])"/>/cover.jpg
	</xsl:if>
</xsl:variable>

<xsl:variable name="document_type">
	<xsl:choose>
		<xsl:when test="count(/page/pageResponse/document//documentNode) = 1">simple</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="/page/pageResponse/document/@docType"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:variable>
		

<xsl:variable name="document_hash" select="/page/pageResponse/document/documentNode/@nodeID" />

<xsl:variable name="document_is_expanded" select="/page/pageRequest/paramList/param[@name='ed']/@value=1"/>

<xsl:variable name="document_expanded_url">
<xsl:value-of select="$librarian"/>?a=d&amp;
c=<xsl:value-of select="$collection"/>&amp;
d=<xsl:value-of select="$document_hash"/>&amp;
ed=1
</xsl:variable>

<xsl:variable name="document_collapsed_url">
<xsl:value-of select="$librarian"/>?a=d&amp;
c=<xsl:value-of select="$collection"/>&amp;
d=<xsl:value-of select="$document_hash"/>
</xsl:variable>

<xsl:variable name="document_title" >
	<xsl:choose>
		<xsl:when test="$document_type='simple'">
			<xsl:value-of disable-output-escaping='yes' select="/page/pageResponse/document/metadataList/metadata[@name='Title']" />
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of disable-output-escaping='yes' select="/page/pageResponse/document/documentNode/metadataList/metadata[@name='Title']"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:variable>

<xsl:variable name="document_content" >
	<xsl:value-of disable-output-escaping='yes' select="/page/pageResponse/document/documentNode/nodeContent" />
</xsl:variable>

<xsl:variable name="document_allSections" select="/page/pageResponse/document//documentNode[@nodeType!='root']" />

<xsl:variable name="document_topLevelNodes" select="/page/pageResponse/document/documentNode/documentNode" />


<xsl:variable name="document_topLevelSections" select="/page/pageResponse/document/documentNode/documentNode" />

<xsl:template name="document_section_id">
	<xsl:value-of select="./@nodeID"/>
</xsl:template>

<xsl:template name="document_section_title">
	<xsl:value-of disable-output-escaping='yes' select="./metadataList/metadata[@name='Title']"/>
</xsl:template>

<xsl:template name="document_section_depth">
	<xsl:value-of select="count(./ancestor::documentNode)"/>
</xsl:template>

<xsl:template name="document_section_content">
	<xsl:value-of disable-output-escaping='yes' select="./nodeContent"/>
</xsl:template>


<xsl:variable name="document_currentSection" select="/page/pageResponse/document//nodeContent/.." />

<xsl:variable name="document_currentSection_title" select="$document_currentSection/metadataList/metadata[@name='Title']" />

<xsl:variable name="document_currentSection_content">
	<xsl:value-of disable-output-escaping='yes' select="$document_currentSection/nodeContent" />
</xsl:variable>

<xsl:variable name="document_previousSection" select="$document_currentSection/preceding::documentNode[@nodeType='leaf'][1]"/>

<xsl:variable name="document_previousSection_title" select="$document_previousSection/metadataList/metadata[@name='Title']"/>

<xsl:variable name="document_previousSection_url">
<xsl:value-of select="$librarian"/>?a=d&amp;
c=<xsl:value-of select="$collection"/>&amp;
d=<xsl:value-of select="$document_previousSection/@nodeID"/>&amp;
sib=1&amp;
ec=1
</xsl:variable>

<xsl:variable name="document_nextSection" select="$document_currentSection/following::documentNode[@nodeType='leaf'][1]"/>

<xsl:variable name="document_nextSection_title" select="$document_nextSection/metadataList/metadata[@name='Title']"/>

<xsl:variable name="document_nextSection_url">
<xsl:value-of select="$librarian"/>?a=d&amp;
c=<xsl:value-of select="$collection"/>&amp;
d=<xsl:value-of select="$document_nextSection/@nodeID"/>&amp;
sib=1&amp;
ec=1
</xsl:variable>

<xsl:variable name="document_ancestorSections" select="$document_currentSection/ancestor::documentNode[@nodeType='internal']"/>

<xsl:variable name="document_ancestorSections_titles" select="$document_ancestorSections/metadataList/metadata[@name='Title']"/>








<!-- stuff that no-one should need to know about........................................ -->

<!-- produce an exact copy of the current node, but expand and replace all metadataItems and uiItems found within. -->
<xsl:template name="expandMetadataAndUiItems">
 <xsl:for-each select="*|text()">
  <xsl:choose>

    <!-- if node is a metadataItem, expand it -->
    <xsl:when test="self::node()[self::metadataItem]">
      <xsl:call-template name="metadataItem">
       <xsl:with-param name="name"><xsl:value-of select="@name" disable-output-escaping="yes"/></xsl:with-param>
      </xsl:call-template>
    </xsl:when>

    <!-- if node is a uiItem, expand it -->
    <xsl:when test="self::node()[self::uiItem]">
      <xsl:call-template name="uiItem">
       <xsl:with-param name="name"><xsl:value-of select="@name" disable-output-escaping="yes"/></xsl:with-param>
      </xsl:call-template>
    </xsl:when>

    <xsl:when test="self::text()">
     <xsl:value-of select="." disable-output-escaping="yes"/>
    </xsl:when>

    <!-- if a regular node -->
    <xsl:otherwise>
     <xsl:variable name="element-name" select="name()"/>
     <xsl:element name="{$element-name}">
      <xsl:for-each select="@*">
       <xsl:variable name="attribute-name" select="name()"/>
       <xsl:attribute name="{$attribute-name}">
        <xsl:call-template name="attribute-filter"/>
       </xsl:attribute>
      </xsl:for-each>
      <xsl:call-template name="expandMetadataAndUiItems" />
     </xsl:element>
    </xsl:otherwise>
  </xsl:choose>
 </xsl:for-each>

</xsl:template>

<xsl:template name="attribute-filter"><xsl:value-of select="."/></xsl:template>

</xsl:stylesheet>


