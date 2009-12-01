<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:gslib="http://www.greenstone.org/XSL/Library" 
xmlns:xalan="http://xml.apache.org/xalan"
xmlns="http://www.w3.org/1999/xhtml">


<xsl:output 
method="xml" 
encoding="UTF-8" 
doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" 
doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
omit-xml-declaration="yes"
indent="yes"/>

<!-- general ............................................................................. -->

<xsl:variable name="librarian"></xsl:variable>

<xsl:variable name="action" select="/page/pageRequest/@action"/>

<xsl:variable name="subaction" select="/page/pageRequest/@subaction"/>

<xsl:variable name="lang" select="/page/pageRequest/@lang"/>

<xsl:variable name="css">
 <xsl:for-each select="/page/pageResponse/cssFileList/cssFile">
  <link rel="stylesheet" href="{@path}" type="text/css" />
 </xsl:for-each>
</xsl:variable>

<xsl:variable name="js">
 <xsl:for-each select="/page/pageResponse/jsFileList/jsFile">
 	<script type="text/javascript" src="{@path}"></script>
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

<xsl:variable name="collectionNames" select="/page/pageResponse/collectionList/collection/@name"/>

<xsl:template name="collection_title">
	<xsl:param name="name"/>
	<xsl:value-of select="/page/pageResponse/collectionList/collection[@name=$name]/displayItem[@name='name']"/>
</xsl:template>

<xsl:template name="collection_description">
	<xsl:param name="name"/>
	<xsl:value-of select="/page/pageResponse/collectionList/collection[@name=$name]/displayItem[@name='description']"/>
</xsl:template>

<xsl:template name="collection_url">
	<xsl:param name="name"/>
	<xsl:value-of select="$librarian" />
?a=p&amp;sa=about&amp;c=
	<xsl:value-of select="$name" />
</xsl:template>

<xsl:variable name="site_homeUrl">
	<xsl:value-of select="$librarian" />
?a=p&amp;sa=home
</xsl:variable>

<!-- collection level stuff ....................................................................... -->

<xsl:variable name="collection" select="/page/pageResponse/collection/@name"/>

<xsl:variable name="serviceNames" select="/page/pageResponse/collection/serviceList/service[@type='query' or @type='browse']/@name"/>

<xsl:template name="service_title">
	<xsl:param name="serviceName"/>
	<xsl:value-of select="/page/pageResponse/collection/serviceList/service[@name=$serviceName]/displayItem[@name='name']"/>
</xsl:template>

<xsl:template name="service_description">
	<xsl:param name="serviceName"/>
	<xsl:value-of select="/page/pageResponse/collection/serviceList/service[@name=$serviceName]/displayItem[@name='description']"/>
</xsl:template>

<xsl:template name="service_url">
	<xsl:param name="serviceName"/>
	
	
	<xsl:variable name="service" select="/page/pageResponse/collection/serviceList/service[@name=$serviceName]"/>
	<xsl:variable name="serviceAction">
   	<xsl:choose>
		<!-- this hard coding doesnt seem like a good idea: why isnt the appropriate action given as an attribute of the service? -->
    	<xsl:when test="$service/@type='query'">q</xsl:when>
    	<xsl:when test="$service/@type='browse'">b</xsl:when>
    	<xsl:when test="$service/@type='process'">pr</xsl:when>
    	<xsl:when test="$service/@type='applet'">a</xsl:when>
    	<xsl:otherwise>DO_NOT_DISPLAY</xsl:otherwise>
   		</xsl:choose>
  	</xsl:variable>
	
<xsl:value-of select="$librarian"/>?
a=<xsl:value-of select="$serviceAction"/>&amp;rt=d&amp;
s=<xsl:value-of select="$serviceName"/>&amp;
c=<xsl:value-of select="$collection"/>
</xsl:template>

<xsl:variable name="classifierNames" select="/page/pageResponse/collection/serviceList//classifier/@name"/>

<xsl:template name="classifier_title">
	<xsl:param name="clsName"/>
	<xsl:value-of select="/page/pageResponse/collection/serviceList//classifier[@name=$clsName]/displayItem[@name='name']"/>
</xsl:template>

<xsl:template name="classifier_description">
	<xsl:param name="clsName"/>
	<xsl:value-of select="/page/pageResponse/collection/serviceList//classifier[@name=$clsName]/displayItem[@name='description']"/>
</xsl:template>

<xsl:template name="classifier_url">
<xsl:param name="clsName"/>
<xsl:value-of select="$librarian"/>?a=b&amp;rt=r&amp;
s=<xsl:value-of select="/page/pageResponse/collection/serviceList//classifier[@name=$clsName]/../../@name"/>&amp;
c=<xsl:value-of select="$collection"/>&amp;
cl=<xsl:value-of select="$clsName"/>
</xsl:template>


<!-- searching ............................................................................. -->

<xsl:template name="hidden_search_params">
	<input name="c" value="{$collection}" type="hidden"/> 
	<input name="a" value="q" type="hidden"/> 
	<input name="s" value="TextQuery" type="hidden"/> 
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

<xsl:variable name="currentClassifierName" select="/page/pageResponse/classifier/@name" />

<xsl:variable name="topLevelClassifierNodes" select="/page/pageResponse/classifier/classifierNode"/>


<xsl:variable name="topLevelClassifierNodeIds" select="/page/pageResponse/classifier/classifierNode/@nodeID"/>

<!--
<xsl:template name="classifierNode_title">
	<xsl:param name="id"/>
	<xsl:value-of select="/page/pageResponse/classifier//classifierNode[@nodeID=$id]/metadataList/metadata[@name='Title']"/>
</xsl:template>
-->

<xsl:template name="classifierNode_url">
	<xsl:param name="id"/>

<xsl:value-of select="$librarian"/>?a=b&amp;rt=r&amp;
s=<xsl:value-of select="/page/pageResponse/service/@name"/>&amp;
c=<xsl:value-of select="$collection"/>&amp;
cl=<xsl:value-of select="$id" /><xsl:if test="classifierNode|documentNode">.pr</xsl:if>

</xsl:template>

<xsl:template name="classifierNode_childNodeIds">
	<xsl:param name="id"/>
	<xsl:value-of select="xalan:nodeset(/page/pageResponse/classifier//classifierNode[@nodeID=$id])/classifierNode/@nodeID"/>
</xsl:template>





<xsl:template name="list_classifer_data">
	<ul>
		<xsl:for-each select="/page/pageResponse/classifier/classifierNode">
			<xsl:call-template name="expand_classifier_node" />
		</xsl:for-each>
		<xsl:for-each select="/page/pageResponse/classifier/documentNode">
			<li class="leafNode">
				<!-- call format statement for docuement node -->
				<xsl:call-template name="documentNode" />
			</li>
		</xsl:for-each>
	</ul>
</xsl:template>

<xsl:template name="expand_classifier_node">
	<xsl:choose>
		
		<xsl:when test="classifierNode or documentNode">
			<!-- if there are children -->
	 		<li class="expandedNode">
				<!-- call format statement for classifier node -->
				<xsl:call-template name="classifierNode" />
			 	
				<xsl:if test="classifierNode">
				<ul>
					<xsl:for-each select="classifierNode">
						<xsl:call-template name="expand_classifier_node"/>
					</xsl:for-each>	
				</ul>
				</xsl:if>
				<xsl:if test="documentNode">
					<ul>
						<xsl:for-each select="documentNode">
							<li class="leafNode">
								<!-- call format statement for docuement node -->
								<xsl:call-template name="documentNode" />
							</li>
						</xsl:for-each>
					</ul>
				</xsl:if>			
			</li>
		</xsl:when>
		<xsl:otherwise>
			<li class="collapsedNode">
				<!-- call format statement for classifier node -->
				<xsl:call-template name="classifierNode" />
			</li>
		</xsl:otherwise>
	</xsl:choose> 
</xsl:template>

<!-- document viewing..................................................................... -->

<xsl:variable name="document_coverImage_exists" select="/page/pageResponse/document/@hasCoverImage" />

<xsl:variable name="document_coverImage_url">
	<xsl:if test="$document_coverImage_exists">
		<xsl:value-of select="/page/pageResponse/collection/metadataList/metadata[@name='httpPath']"/>/index/assoc/<xsl:value-of select="normalize-space(/page/pageResponse/document/metadataList/metadata[@name='archivedir'])"/>/cover.jpg
	</xsl:if>
</xsl:variable>

<xsl:variable name="document_type" select="/page/pageResponse/document/@docType"/>

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
	<xsl:choose>
		<xsl:when test="$document_type='simple'">
			<xsl:value-of select="$document_currentSection_content" />
		</xsl:when>
	</xsl:choose>
</xsl:variable>

<xsl:variable name="document_allSectionIds" select="/page/pageResponse/document//documentNode[@nodeType!='root']" />

<xsl:variable name="document_topLevelSectionIds" select="/page/pageResponse/document/documentNode/documentNode" />

<xsl:template name="document_section_title">
	<xsl:param name="sectionId" />
	<xsl:value-of disable-output-escaping='yes' select="//page/pageResponse/document//documentNode[@nodeID=$sectionId]/metadataList/metadata[@name='Title']"/>
</xsl:template>

<!--
<xsl:template name="document_section_depth">
	<xsl:param name="index" />
	<xsl:value-of select="count($document_allSections[$index]/ancestor::documentNode)"/>
</xsl:template>

<xsl:template name="document_section_content">
	<xsl:param name="index" />
	<xsl:value-of disable-output-escaping='yes' select="$document_allSections[$index]/nodeContent"/>
</xsl:template>

-->

<xsl:variable name="document_currentSection" select="/page/pageResponse/document//nodeContent/.." />

<xsl:variable name="document_currentSection_title" select="$document_currentSection/metadataList/metadata[@name='Title']" />

<xsl:variable name="document_currentSection_content" select="$document_currentSection/nodeContent" />

<xsl:variable name="document_previousSection" select="$document_currentSection/preceding::documentNode[@nodeType='leaf'][1]"/>

<xsl:variable name="document_previousSection_title" select="$document_previousSection/metadataList/metadata[@name='Title']"/>

<xsl:variable name="document_previousSection_url">
<xsl:value-of select="$librarian"/>?a=d&amp;
c=<xsl:value-of select="$collection"/>&amp;
d=<xsl:value-of select="$document_previousSection/@nodeID"/>&amp;
sib=1&amp;ec=1
</xsl:variable>

<xsl:variable name="document_nextSection" select="$document_currentSection/following::documentNode[@nodeType='leaf'][1]"/>

<xsl:variable name="document_nextSection_title" select="$document_nextSection/metadataList/metadata[@name='Title']"/>

<xsl:variable name="document_nextSection_url">
<xsl:value-of select="$librarian"/>?a=d&amp;
c=<xsl:value-of select="$collection"/>&amp;
d=<xsl:value-of select="$document_nextSection/@nodeID"/>&amp;
sib=1&amp;ec=1
</xsl:variable>

<xsl:variable name="document_ancestorSections" select="$document_currentSection/ancestor::documentNode[@nodeType='internal']"/>
<xsl:variable name="document_ancestorSections_titles" select="$document_ancestorSections/metadataList/metadata[@name='Title']"/>

<xsl:template name="list_document_content">
	<ul>
		<xsl:for-each select="/page/pageResponse/document/documentNode/documentNode">
			<xsl:call-template name="expand_doc_content_node" />
		</xsl:for-each>
	</ul>
</xsl:template>

<xsl:template name="expand_doc_content_node">
	<xsl:variable name="class">
		<xsl:choose>
			<xsl:when test="@nodeType='leaf'">leaf</xsl:when>
			<xsl:otherwise>collapsed</xsl:otherwise>
		</xsl:choose>	
	</xsl:variable>

	<li class="{$class}" id="{@nodeID}" >
		<xsl:choose>
			<xsl:when test="nodeContent">
				<span id="currentSection">
					<xsl:value-of disable-output-escaping='yes' select="metadataList/metadata[@name='Title']"/>
				</span>
			</xsl:when>
			<xsl:when test="@nodeType='leaf'">
				<a href="{$librarian}?a=d&amp;c={$collection}&amp;d={@nodeID}&amp;sib=1&amp;ec=1">
					<xsl:value-of disable-output-escaping='yes' select="metadataList/metadata[@name='Title']"/>
				</a>
			</xsl:when>
			<xsl:otherwise>
				<a onclick="toggleNode('{@nodeID}')">
					<xsl:value-of disable-output-escaping='yes' select="metadataList/metadata[@name='Title']"/>
				</a>
			</xsl:otherwise>
		</xsl:choose>
		
		<xsl:if test="documentNode">
			<ul>
				<xsl:for-each select="documentNode">
					<xsl:call-template name="expand_doc_content_node" />
				</xsl:for-each>
			</ul>	
		</xsl:if>
	</li>
</xsl:template>















<!-- stuff that no-one should need to know about........................................ -->

<!-- produce an exact copy of the current node, but expand and replace all metadataItems and uiItems found within. -->
<xsl:template name="expandMetadataAndUiItems">
 <xsl:for-each select="*|text()">
  <xsl:choose>

    <!-- if node is a metadataItem, expand it -->
    <xsl:when test="self::node()[self::metadataItem]">
      <xsl:call-template name="metadataItem">
       <xsl:with-param name="name"><xsl:value-of select="@name"/></xsl:with-param>
      </xsl:call-template>
    </xsl:when>

    <!-- if node is a uiItem, expand it -->
    <xsl:when test="self::node()[self::uiItem]">
      <xsl:call-template name="uiItem">
       <xsl:with-param name="name"><xsl:value-of select="@name"/></xsl:with-param>
      </xsl:call-template>
    </xsl:when>

    <xsl:when test="self::text()">
     <xsl:value-of select="."/>
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

<xsl:template match="gslib:blah">
	 <p> Blah </p> 
</xsl:template>



</xsl:stylesheet>


