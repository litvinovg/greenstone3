<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:gslib="http://www.greenstone.org/skinning"
  xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xalan">

<xsl:import href="./util.xsl"/>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xalan">

<xsl:import href="./util.xsl"/>

<xsl:output method="html"/>  

<xsl:template match="/">
 <html>
  <head>
    <title>
     <xsl:call-template name="metadataItem">
      <xsl:with-param name="name">siteName</xsl:with-param>
     </xsl:call-template>
	/
     <xsl:call-template name="metadataItem">
      <xsl:with-param name="name">collectionName</xsl:with-param>
     </xsl:call-template>
    </title>

	<xsl:copy-of select="$js"/>
  </head>
  <body>
  
  	<xsl:if test="$document_currentSection/@nodeType = 'root'">
  		<xsl:attribute name="onload">showContents()</xsl:attribute>
	</xsl:if>		
  
   <div id="container">

    <div id="header">
     <div class="title_minor">
	 


      <a href="{$library}?a=p&amp;sa=home">
       <xsl:call-template name="metadataItem">
        <xsl:with-param name="name">siteName</xsl:with-param>
       </xsl:call-template>
      </a>
      /
     </div>
     <div class="title_major">
      <xsl:call-template name="metadataItem">
       <xsl:with-param name="name">collectionName</xsl:with-param>
      </xsl:call-template>
     </div>
		 
		 <div id="tablist" class="linklist">
     		<xsl:call-template name="service_list" />
		 </div>
 
    </div>

    <div id="content" class="noSidebar">
	
		<br/>	
		
		<xsl:if test="$document_coverImage_exists='true'">
			<div class="coverImage">
				<img src="{$document_coverImage_url}"> </img>
			</div>
		</xsl:if>
		
		<h1>
			<xsl:value-of select="$document_title"/>
		</h1>

		<div id="subServiceList" class="linklist">
			<ul>
     			<li> <a href="" target="_blank">view entire document</a></li>
				<li> <a href="" target="_blank">open in new window</a></li>
			</ul>
    	</div>
		
		<xsl:choose>
			<xsl:when test="$document_type='simple'">
				<div class="documentBody">
				<xsl:value-of disable-output-escaping='yes' select="$document_currentSection_content" />
				</div>			
			</xsl:when>
			
			<xsl:when test="$document_type='hierarchy' and $document_is_expanded != 'true'">
				<a id="contentsLink" onclick="showContents()">contents</a>
		
				<div id="contents">				
					<xsl:call-template name="list_document_content" />
				</div>
		
				<br/><br/>
		
				<div class="documentBody">
						
					<xsl:choose>
						<xsl:when test="$document_ancestorSections!=''">
							<p class="ancestorSections">
								<xsl:for-each select="$document_ancestorSections_titles">
									<xsl:value-of select="."/> /
								</xsl:for-each>
							</p>
				
							<h2 class="subsectionHeader"> <xsl:value-of disable-output-escaping='yes' select="$document_currentSection_title"/> </h2>
				
						</xsl:when>
						<xsl:otherwise>
							<xsl:if test="$document_currentSection/@nodeType != 'root'">
								<h2> <xsl:value-of disable-output-escaping='yes' select="$document_currentSection_title"/> </h2>
							</xsl:if>
						</xsl:otherwise>
					</xsl:choose>
		
					<xsl:call-template name="section_navigation" />
		
					<xsl:value-of disable-output-escaping='yes' select="$document_currentSection_content" />
					
					<xsl:call-template name="section_navigation" />
				</div>
			</xsl:when>
			
			
			
			<xsl:when test="$document_type='hierarchy' and $document_is_expanded">
			
				<xsl:for-each select="$document_allSections">
					<xsl:variable name="pos" select="position()"/>
					<xsl:variable name="depth">
						<xsl:call-template name="document_section_depth">
							<xsl:with-param name="index" select="$pos" />
						</xsl:call-template>
					</xsl:variable>
			
					<xsl:element name="h{$depth + 1}">
					<xsl:call-template name="document_section_title">
						<xsl:with-param name="index" select="$pos" />
					</xsl:call-template>				
					</xsl:element>
					
					<xsl:call-template name="document_section_content">
						<xsl:with-param name="index" select="$pos" />
					</xsl:call-template>		
				</xsl:for-each>
			
			</xsl:when>
			
		</xsl:choose>
    </div>

   <div id="footer" >
    <xsl:call-template name="uiItem">
     <xsl:with-param name="name">collectionFooter</xsl:with-param>
    </xsl:call-template>
   </div>
  </div>

  </body>
 </html>
</xsl:template>

<xsl:template name="section_navigation">
	<xsl:if test="$document_previousSection!='' or $document_nextSection!=''">
		<div class="sectionNav">
			<xsl:if test="$document_previousSection!=''">
				<a class="prevSection" href="{$document_previousSection_url}" title="{$document_previousSection_title}"> &lt; Previous Section</a>
			</xsl:if>
			<xsl:if test="$document_nextSection!=''">
				<a class="nextSection" href="{$document_nextSection_url}" title="{$document_nextSection_title}">Next Section &gt;</a>
			</xsl:if>
		</div>
	</xsl:if>
</xsl:template>


</xsl:stylesheet>
