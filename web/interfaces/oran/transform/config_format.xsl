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
				<xslt:value-of disable-output-escaping="yes" select="metadataList/metadata[@name = 'assocfilepath']"/>
				<xsl:text>/</xsl:text>
				<xsl:choose>
					<xsl:when test="@type = 'thumb'">
						<xslt:value-of disable-output-escaping="yes" select="/page/pageResponse/document/documentNode/metadataList/metadata[@name = 'Thumb']"/>
					</xsl:when>
					<xsl:when test="@type = 'screen'">
						<xslt:value-of disable-output-escaping="yes" select="/page/pageResponse/document/documentNode/metadataList/metadata[@name = 'Screen']"/>
					</xsl:when>
					<xsl:when test="@type = 'source'">
						<xslt:value-of disable-output-escaping="yes" select="/page/pageResponse/document/documentNode/metadataList/metadata[@name = 'SourceFile']"/>
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
						<xsl:text>?a=b&amp;rt=r&amp;s=</xsl:text>
						<xslt:value-of select='/page/pageResponse/service/@name'/>
						<xsl:text>&amp;c=</xsl:text>
						<xslt:value-of select='/page/pageResponse/collection/@name'/>
						<xsl:text>&amp;cl=</xsl:text>
						<xslt:value-of select='@nodeID'/>
						<xslt:if test="classifierNode|documentNode">
							<xsl:text>.pr</xsl:text>
						</xslt:if>
						<xslt:if test="parent::node()[@orientation='horizontal']">
							<xsl:text>&amp;sib=1</xsl:text>
						</xslt:if>
					</xslt:attribute>
					<xsl:apply-templates/>
				</a>
			</xsl:when>
			<xsl:when test="@type='source'">
				<a><xslt:attribute name='href'><xslt:value-of 
				   disable-output-escaping="yes" select="/page/pageResponse/collection/metadataList/metadata[@name='httpPath']" />/index/assoc/<xslt:value-of 
				   disable-output-escaping="yes" select="metadataList/metadata[@name='assocfilepath']" />/<xslt:value-of 
				   disable-output-escaping="yes" select="metadataList/metadata[@name='srclinkFile']" /></xslt:attribute>
				  <xsl:apply-templates/>
				</a>
			</xsl:when>
			
			<xsl:otherwise> <!-- a document link -->
				<xslt:variable name="bookswitch">
					<xslt:value-of select="/page/pageRequest/paramList/param[@name='book']/@value"/>
				</xslt:variable>
				<xslt:choose>
					<xslt:when test="$bookswitch = 'on' or $bookswitch = 'flashxml'">
						<a>
							<xslt:attribute name="href">
								<xslt:value-of select='$library_name'/>
								<xsl:text>?a=d&amp;ed=1&amp;book=on&amp;c=</xsl:text>
								<xslt:value-of select='/page/pageResponse/collection/@name'/>
								<xsl:text>&amp;d=</xsl:text>
								<xslt:value-of select='@nodeID'/>
								<xsl:text>&amp;dt=</xsl:text>
								<xslt:value-of select='@docType'/>
								<xslt:if test="@nodeType='leaf'">
									<xsl:text>&amp;sib=1</xsl:text>
								</xslt:if>
								<xsl:text>&amp;p.a=</xsl:text>
								<xslt:value-of select="/page/pageRequest/@action"/>
								<xsl:text>&amp;p.sa=</xsl:text>
								<xsl:value-of select="/page/pageRequest/@subaction"/>
								<xsl:text>&amp;p.s=</xsl:text>
								<xslt:value-of select="/page/pageResponse/service/@name"/>
							</xslt:attribute>
							<xsl:apply-templates/>
						</a>
					</xslt:when>
					<xslt:otherwise>
						<a>
							<xslt:attribute name="href">
								<xslt:value-of select='$library_name'/>
								<xsl:text>?a=d&amp;book=off</xsl:text>
								<xslt:if test="not(@docType = 'paged')">
									<xsl:text>&amp;ed=1</xsl:text>
								</xslt:if>
								<xsl:text>&amp;c=</xsl:text>								
								<xslt:value-of select='/page/pageResponse/collection/@name'/>
								<xsl:text>&amp;d=</xsl:text>
								<xslt:value-of select='@nodeID'/>
								<xsl:text>&amp;dt=</xsl:text>
								<xslt:value-of select='@docType'/>
								<xslt:if test="@nodeType='leaf'">
									<xsl:text>&amp;sib=1</xsl:text>
								</xslt:if>
								<xsl:text>&amp;p.a=</xsl:text>
								<xslt:value-of select="/page/pageRequest/@action"/>
								<xsl:text>&amp;p.sa=</xsl:text>
								<xsl:value-of select="/page/pageRequest/@subaction"/>
								<xsl:text>&amp;p.s=</xsl:text>
								<xslt:value-of select="/page/pageResponse/service/@name"/>
							</xslt:attribute>
							<xsl:apply-templates/>
						</a>
					</xslt:otherwise>
				</xslt:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="gsf:icon">
		<xsl:choose>
			<xsl:when test="@type='classifier'">
				<img style="border:0px"><xsl:attribute name="src"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'bookshelf_image')"/></xsl:attribute></img>
			</xsl:when>
			<xsl:when test="@type='document'">
				<img style="border:0px"><xslt:attribute name="id">documentBasketBook<xslt:value-of select="/page/pageResponse/collection/@name"/>:<xslt:value-of select="@nodeID"/></xslt:attribute><xslt:attribute name="src"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'page_icon_image')"/></xslt:attribute></img> 
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="gsf:metadata[@format]">
		<xslt:value-of disable-output-escaping="yes">
			<xsl:attribute name="select">
				<xsl:text>java:org.greenstone.gsdl3.util.XSLTUtil.</xsl:text>
				<xsl:value-of select="@format"/>
				<xsl:text>(metadataList/metadata[@name='</xsl:text>
				<xsl:apply-templates select="." mode="get-metadata-name"/>
				<xsl:text>'], /page/@lang )</xsl:text>
			</xsl:attribute>
		</xslt:value-of>
	</xsl:template>

	<!-- if this gsf:metadata is a child of a document node then we want to get the metadata for that node -->
	<xsl:template match="xsl:template[@match = 'documentNode' or @match = 'classifierNode']//gsf:metadata | gsf:template[@match = 'documentNode' or @match = 'classifierNode']//gsf:metadata">
		<xslt:if test="not(@hidden = 'true')">		
			<xslt:value-of disable-output-escaping="yes">
				<xsl:attribute name="select">
					<xsl:text>metadataList/metadata[@name='</xsl:text>
					<xsl:apply-templates select="." mode="get-metadata-name"/>
					<xsl:text>']</xsl:text>
				</xsl:attribute>
			</xslt:value-of>
		</xslt:if>
	</xsl:template>
	
	<xsl:template match="gsf:metadata">
		<xslt:if test="not(@hidden = 'true')">	
			<xslt:value-of disable-output-escaping="yes">
				<xsl:attribute name="select">
					<xsl:text>/page/pageResponse/document/documentNode/metadataList/metadata[@name='</xsl:text>
					<xsl:apply-templates select="." mode="get-metadata-name"/>
					<xsl:text>']</xsl:text>
				</xsl:attribute>
			</xslt:value-of>
		</xslt:if>
	</xsl:template>

	<xsl:template match="gsf:metadata" mode="get-metadata-name">
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
  
	<xsl:template match="gsf:metadata-old">
		<xslt:value-of disable-output-escaping="yes">
			<xsl:attribute name="select">
				<xsl:text>metadataList/metadata[@name="</xsl:text>
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
		<xslt:apply-templates select="nodeContent"/>
	</xsl:template>
  
	<xsl:template match="xsl:template[@match = 'documentNode' or @match = 'classifierNode']//gsf:choose-metadata | gsf:template[@match = 'documentNode' or @match = 'classifierNode']//gsf:choose-metadata">
		<xslt:choose>
			<xsl:for-each select="gsf:metadata">
				<xslt:when>
					<xsl:attribute name="test">metadataList/metadata[@name='<xsl:apply-templates select="." mode="get-metadata-name"/>']</xsl:attribute>
					<xsl:apply-templates select="."/>
				</xslt:when>
			</xsl:for-each>
			<xsl:if test="gsf:default">
				<xslt:otherwise><xsl:apply-templates select="gsf:default"/></xslt:otherwise>
			</xsl:if>
		</xslt:choose>
	</xsl:template>
	
	<xsl:template match="gsf:choose-metadata">
		<xslt:choose>
			<xsl:for-each select="gsf:metadata">
				<xslt:when>
					<xsl:attribute name="test">/page/pageResponse/document/documentNode/metadataList/metadata[@name='<xsl:apply-templates select="." mode="get-metadata-name"/>']</xsl:attribute>
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
		<xslt:variable name="meta"><xsl:choose><xsl:when test="@preprocess"><xslt:value-of select="util:{@preprocess}(metadataList/metadata[@name='{$meta-name}'])"/></xsl:when><xsl:otherwise><xslt:value-of select="metadataList/metadata[@name='{$meta-name}']"/></xsl:otherwise></xsl:choose></xslt:variable>
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


