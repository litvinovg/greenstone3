<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xslt="output.xsl"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	extension-element-prefixes="java">
	<xsl:param name="interface_name"/>
	<xsl:param name="library_name"/>
  
	<xsl:output method="xml"/>
	<xsl:namespace-alias stylesheet-prefix="xslt" result-prefix="xsl"/>

	<xsl:template match="format">
		<format>
			<xsl:apply-templates/>
		</format>
	</xsl:template>
  
	<xsl:template match="gsf:template">
		<xslt:template>
			<xsl:copy-of select="@*"/>
			<xsl:attribute name="priority">2</xsl:attribute> 
			<xsl:if test=".//gsf:link">
				<xslt:param name="serviceName"/>
				<xslt:param name="collName"/>
			</xsl:if>
			<xsl:apply-templates/>
		</xslt:template>
	</xsl:template>
	
	<xsl:template match="gsf:variable">
		<xslt:variable>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xslt:variable>
		<script type="text/javascript">
			gs.variables.<xsl:value-of select="@name"/><xslt:text disable-output-escaping="yes"> = "</xslt:text><xsl:apply-templates/><xslt:text disable-output-escaping="yes">";</xslt:text>
		</script>
	</xsl:template>
	
	<xsl:template match="gsf:defaultClassifierNode">
		<xslt:call-template name="defaultClassifierNode"/>
	</xsl:template>

	<xsl:template match="gsf:image">
		<img>
			<xslt:attribute name='src'>
				<xslt:value-of disable-output-escaping="yes" select="/page/pageResponse/collection/metadataList/metadata[@name = 'httpPath']"/>
				<xsl:text>/index/assoc/</xsl:text>
				<xslt:value-of disable-output-escaping="yes" select="/page/pageResponse/document/metadataList/metadata[@name = 'assocfilepath']"/>
				<xsl:text>/</xsl:text>
				<xsl:choose>
					<xsl:when test="@type = 'thumb'">
						<xslt:value-of disable-output-escaping="yes" select="(.//metadataList)[last()]/metadata[@name = 'Thumb']"/>
					</xsl:when>
					<xsl:when test="@type = 'screen'">
						<xslt:value-of disable-output-escaping="yes" select="(.//metadataList)[last()]/metadata[@name = 'Screen']"/>
					</xsl:when>
					<xsl:when test="@type = 'source'">
						<xslt:value-of disable-output-escaping="yes" select="(.//metadataList)[last()]/metadata[@name = 'SourceFile']"/>
					</xsl:when>
				</xsl:choose>
			</xslt:attribute>
		</img>
	</xsl:template>

	<xsl:template match="gsf:link">
		<xsl:choose>
			<xsl:when test="@type='classifier'">
				<a>
					<xslt:attribute name='href'>
						<xslt:value-of select='$library_name'/>
						<xsl:text>/collection/</xsl:text>
						<xslt:value-of select='/page/pageResponse/collection/@name'/>
						<xsl:text>/browse/</xsl:text>
						<xslt:value-of select='util:replace(@nodeID, ".", "/")'/>
					</xslt:attribute>
					<xsl:apply-templates/>
				</a>
			</xsl:when>
			<xsl:when test="@type='source'">
				<a><xslt:attribute name='href'><xslt:value-of 
				   disable-output-escaping="yes" select="/page/pageResponse/collection/metadataList/metadata[@name='httpPath']" />/index/assoc/<xslt:value-of 
				   disable-output-escaping="yes" select="(.//metadataList)[last()]/metadata[@name='assocfilepath']" />/<xslt:value-of 
				   disable-output-escaping="yes" select="(.//metadataList)[last()]/metadata[@name='srclinkFile']" /></xslt:attribute>
				  <xsl:apply-templates/>
				</a>
			</xsl:when>
			<xsl:otherwise> <!-- a document link -->
				<xslt:variable name="bookswitch">
					<xslt:value-of select="/page/pageRequest/paramList/param[@name='book']/@value"/>
				</xslt:variable>
				<a>
					<xslt:attribute name="href">
						<xslt:value-of select='$library_name'/>
						<xsl:text>/collection/</xsl:text>
						<xslt:value-of select='/page/pageResponse/collection/@name'/>
						<xsl:text>/document/</xsl:text>
						<xslt:value-of select='@nodeID'/>
						<xslt:choose>
							<xslt:when test="$bookswitch = 'on' or $bookswitch = 'flashxml'">
								<xsl:text>?book=on</xsl:text>
							</xslt:when>
							<xslt:otherwise>
								<xslt:if test="$opt-doc-link-args">?<xslt:value-of select="$opt-doc-link-args"/></xslt:if>
							</xslt:otherwise>
						</xslt:choose>
					</xslt:attribute>
					<xsl:apply-templates/>
				</a>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="gsf:icon">
		<xsl:choose>
			<xsl:when test="@type='classifier'">
				<img style="border:0px"><xsl:attribute name="src"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'bookshelf_image')"/></xsl:attribute></img>
			</xsl:when>
			<xsl:when test="not(@type) or @type='document'">
				<img style="border:0px"><xslt:attribute name="id">documentBasketBook<xslt:value-of select="/page/pageResponse/collection/@name"/>:<xslt:value-of select="@nodeID"/></xslt:attribute><xslt:attribute name="src"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'page_icon_image')"/></xslt:attribute></img> 
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<!-- gsf:cgiparam example, as used by the Enhanced PDF tutorial: 
		<a><xsl:attribute name="href"><gsf:metadata name="httpPath" type="collection"/>/index/assoc/<gsf:metadata name="archivedir"/>/<gsf:metadata name="srclinkFile"/>#search=&amp;quot;<gsf:cgi-param name="query"/>&amp;quot;</xsl:attribute>src doc link with query highlighting</a> -->
	<xsl:template match="gsf:cgi-param">
		<xslt:value-of disable-output-escaping="yes" select="/page/pageRequest/paramList/param[@name='s1.{@name}']/@value"/>		
	</xsl:template>

	<!-- A GLI user can use a gsf:displayText element in GS3's Format Features to retrieve
	  a string defined in either collectionConfig.xml or else the interface dictionaries. 
	  If the requested string occurs in neither, the request string itself will be output. -->
	<xsl:template match="gsf:displayText">
	  <xslt:variable name="displaytext">
	    <xsl:call-template name="gsf:displayItem"/>
	  </xslt:variable>

	  <xslt:choose>
	    <xslt:when test="$displaytext != ''">
	      <xslt:value-of disable-output-escaping="yes" select="$displaytext"/>	      
	    </xslt:when>
	    <xslt:otherwise>
	      <xslt:variable name="interfacetxt">
		<xsl:call-template name="gsf:interfaceText"/>
	      </xslt:variable>

	      <xslt:choose>
		<xslt:when test="$interfacetxt != ''">
		  <xslt:value-of disable-output-escaping="yes" select="$interfacetxt"/>
		</xslt:when>
		<xslt:otherwise>
		  <xslt:value-of disable-output-escaping="yes" select="'{@name}'"/>
		</xslt:otherwise>
	      </xslt:choose>
	    </xslt:otherwise>
	  </xslt:choose>
	</xsl:template>

	<!-- With gsf:displayItem, a user can request a displayItem from collectionConfig.xml -->
	<xsl:template match="gsf:displayItem" name="gsf:displayItem">
	  <xslt:value-of disable-output-escaping="yes" select="/page/pageResponse/collection/displayItem[@name='{@name}']"/>
	</xsl:template>

	<!-- With gsf:interfaceText, a user can request a string from the interface dictionaries in the current lang -->
	<xsl:template match="gsf:interfaceText" name="gsf:interfaceText">
	  <xslt:value-of disable-output-escaping="yes" select="util:getInterfaceText($interface_name, /page/@lang, '{@name}')"/>
	</xsl:template>

	<!-- if this gsf:metadata is a child of a document node then we want to get the metadata for that node -->
	<xsl:template match="gsf:metadata">
		<xslt:variable name="langAtt"><xsl:value-of select="@lang"/></xslt:variable>
		<xslt:if test="not(@hidden = 'true')">
			<xslt:value-of disable-output-escaping="yes">
				<xsl:attribute name="select">
					<xsl:if test="@format">
						<xsl:text>java:org.greenstone.gsdl3.util.XSLTUtil.</xsl:text>
						<xsl:value-of select="@format"/>
						<xsl:text>(</xsl:text>
					</xsl:if>
					<xsl:choose>
						<xsl:when test="@type = 'collection'">
							<xsl:text>/page/pageResponse/collection/metadataList/metadata[@name='</xsl:text>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text>(.//metadataList)[last()]/metadata[@name='</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:apply-templates select="." mode="get-metadata-name"/>
					<xsl:text>'</xsl:text>
					<xsl:if test="@lang">
						<xsl:text> and @lang=$langAtt</xsl:text>
					</xsl:if>
					<xsl:text>]</xsl:text>
					<xsl:if test="@format">
						<xsl:text>, /page/@lang )</xsl:text>
					</xsl:if>
				</xsl:attribute>
			</xslt:value-of>
		</xslt:if>
	</xsl:template>

	<xsl:template match="gsf:metadata" mode="get-metadata-name">
		<xsl:if test="@pos">
			<xsl:text>pos</xsl:text>
			<xsl:value-of select='@pos'/>
			<xsl:text>_</xsl:text>
		</xsl:if>
		<xsl:if test='@select'>
			<xsl:value-of select='@select'/>
			<xsl:text>_</xsl:text>
		</xsl:if>
		<xsl:if test="@separator">
		  	<xsl:text>*</xsl:text>
			<xsl:value-of select='@separator'/>
			<xsl:text>*_</xsl:text>
		</xsl:if>
		<xsl:value-of select="@name"/>
	</xsl:template>

	<xsl:template match="gsf:metadata-old" mode="get-metadata-name">
		<xsl:if test="@multiple='true'">
			<xsl:text>all_</xsl:text>
		</xsl:if>
		<xsl:if test='@select'>
			<xsl:value-of select='@select'/>
			<xsl:text>_</xsl:text>
		</xsl:if>
		<xsl:if test="@separator">
		  	<xsl:text>*</xsl:text>
			<xsl:value-of select='@separator'/>
			<xsl:text>*_</xsl:text>
		</xsl:if>
		<xsl:value-of select="@name"/>
	</xsl:template>
  
	<xsl:template match="gsf:metadata-older">
		<xslt:value-of disable-output-escaping="yes">
			<xsl:attribute name="select">
				<xsl:text>(.//metadataList)[last()]/metadata[@name="</xsl:text>
				<xsl:choose>
					<xsl:when test="@select='parent'">
						<xsl:text>parent_</xsl:text>
					</xsl:when>
					<xsl:when test="@select='root'">
						<xsl:text>root_</xsl:text>
					</xsl:when>
					<xsl:when test="@select='ancestors'">
						<xsl:text>ancestors'</xsl:text>
						<xsl:value-of select='@separator'/>
						<xsl:text>'_</xsl:text>
					</xsl:when>
					<xsl:when test="@select='siblings'">
						<xsl:text>siblings_'</xsl:text>
						<xsl:value-of select='@separator'/>
						<xsl:text>'_</xsl:text>
					</xsl:when>
				</xsl:choose>
				<xsl:value-of select="@name"/>
				<xsl:text>"]</xsl:text>
			</xsl:attribute>
		</xslt:value-of>
	</xsl:template>
  
	<xsl:template match="gsf:text">
    <xslt:call-template name="documentNodeText"/>
	</xsl:template>
  
	<xsl:template match="gsf:choose-metadata">
		<xslt:choose>
			<xsl:for-each select="gsf:metadata">
				<xslt:when>
					<xsl:attribute name="test">(.//metadataList)[last()]/metadata[@name='<xsl:apply-templates select="." mode="get-metadata-name"/>']</xsl:attribute>
					<xsl:apply-templates select="."/>
				</xslt:when>
			</xsl:for-each>
			<xsl:if test="gsf:default">
				<xslt:otherwise><xsl:apply-templates select="gsf:default"/></xslt:otherwise>
			</xsl:if>
		</xslt:choose>
	</xsl:template>
  
	<xsl:template match="gsf:switch">
		<xsl:variable name="meta-name"><xsl:apply-templates select="gsf:metadata" mode="get-metadata-name"/></xsl:variable>
		<xslt:variable name="meta"><xsl:choose><xsl:when test="@preprocess"><xslt:value-of select="util:{@preprocess}((.//metadataList)[last()]/metadata[@name='{$meta-name}'])"/></xsl:when><xsl:otherwise><xslt:value-of select="(.//metadataList)[last()]/metadata[@name='{$meta-name}']"/></xsl:otherwise></xsl:choose></xslt:variable>
		<xslt:choose>
			<xsl:for-each select="gsf:when">
				<xslt:when test="util:{@test}($meta, '{@test-value}')">
					<xsl:apply-templates/>
				</xslt:when>
			</xsl:for-each>
			<xsl:if test="gsf:otherwise">
				<xslt:otherwise>
					<xsl:apply-templates select="gsf:otherwise/node()"/>
				</xslt:otherwise>
			</xsl:if>
		</xslt:choose>
	</xsl:template>
  
	<xsl:template match="*">
		<xsl:copy>
		<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>


