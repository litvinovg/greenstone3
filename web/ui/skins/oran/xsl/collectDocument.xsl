<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:gslib="http://www.greenstone.org/skinning">

<xsl:import href="./util.xsl"/>

<xsl:template match="/">
<html>
	<head>
		<title>
			<gslib:metadataItem name="siteName" /> 
			/ 
			<gslib:metadataItem name="collectionName" />
		</title>

		<gslib:css/>
		<gslib:js/>
  	</head>
  	<body>
		<xsl:if test="$document_currentSection/@nodeType = 'root'">
  			<xsl:attribute name="onload">showContents()</xsl:attribute>
		</xsl:if>		
  
   		<div id="container">
			<div id="header">
    			<div class="title_minor">
					<a href="{$site_homeUrl}">
						<gslib:metadataItem name="siteName" />
					</a>
				</div>
				
				<div class="title_major">
					<gslib:metadataItem name="collectionName" />
				</div>
		 
				<div id="tablist" class="linklist">
					<ul>
						<xsl:for-each select="$services">
							<li>
								<a href="">
									<xsl:attribute name="href"> <gslib:service_url/> </xsl:attribute>
									<xsl:attribute name="title"> <gslib:service_description/> </xsl:attribute>
									<gslib:service_title/>
								</a>						
							</li>
						</xsl:for-each>
						
						<li>
							<a href="{$collection_homeUrl}">
								Home
							</a>
						</li>
					</ul>
				</div>
				
				<div class="quicksearchBox" >
						<form method="get" name="quickSearch">
							<input type="text" name="s1.query" value="{$search_query}"/>
						
							<input type="submit" value="Quick Search"/>
							<gslib:search_hiddenInputs/>
						</form>
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
					<gslib:document_title/>
				</h1>
		
				<xsl:choose>
					<xsl:when test="$document_type='simple'">
						<div id="subServiceList" class="linklist">
							<ul>
     							<li> 
									<a href="" target="_blank" title="open in new window">detach</a>
								</li>
							</ul>
    					</div>
						
						<br/><br/>
					
						<div class="documentBody">
							<gslib:document_content/>
						</div>			
					</xsl:when>
			
					<xsl:when test="$document_type='hierarchy' and $document_is_expanded != 'true'">
						<div id="subServiceList" class="linklist">
							<ul>
     							<li> 
									<a href="{$document_expanded_url}" title="view entire document">expand</a>
								</li>
								<li> 
									<a href="" target="_blank" title="open in new window">detach</a>
								</li>
							</ul>
    					</div>
					
						<a id="contentsLink" onClick="showContents()" onMouseOver="showContents()">Show contents</a>
		
						<div id="contents">
							<ul>				
							<xsl:for-each select="$document_topLevelNodes">
								<xsl:call-template name="addDocNodeToContents"/>							
							</xsl:for-each>
							</ul>
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
		
							<gslib:document_currentSection_content/>
											
							<xsl:call-template name="section_navigation" />
						</div>
					</xsl:when>

					<xsl:when test="$document_type='hierarchy' and $document_is_expanded">
						<div id="subServiceList" class="linklist">
							<ul>
     							<li> 
									<a href="{$document_collapsed_url}" title="view document as sections">collapse</a>
								</li>
								<li> 
									<a href="" target="_blank" title="open in new window">detach</a>
								</li>
							</ul>
    					</div>
						
						
						<br/><br/>
						<xsl:for-each select="$document_allSections">
					
							<xsl:variable name="depth"><gslib:document_section_depth/></xsl:variable>
							
							<xsl:element name="h{$depth + 1}">
								<gslib:document_section_title/>				
							</xsl:element>
					
							<gslib:document_section_content/>		
						</xsl:for-each>
					</xsl:when>
			
				</xsl:choose>
   			</div>
			
   			<div id="footer" >
    			<gslib:uiItem name="collectionFooter"/>
   			</div>
 		</div>
  	</body>
</html>
</xsl:template>

<xsl:template name="addDocNodeToContents">

	<xsl:variable name="sId"> <gslib:document_section_id/></xsl:variable>
	
	<li class="expanded" id="{$sId}">
		<xsl:choose>
			<xsl:when test="@nodeType='leaf'">
				<xsl:attribute name="class">leaf</xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
				<xsl:attribute name="class">collapsed</xsl:attribute>
			</xsl:otherwise>
		</xsl:choose>
		<a>
			<xsl:choose>
				<xsl:when test=".=$document_currentSection">
					<xsl:attribute name="id">currentSection</xsl:attribute>
				</xsl:when>
				<xsl:when test="@nodeType='leaf'">
					<xsl:attribute name="href"><gslib:documentNode_url/>&amp;ec=1</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="onclick">toggleNode('<gslib:document_section_id/>')</xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			
			<gslib:document_section_title/>
		</a>
		
		<xsl:if test="./documentNode">
			<ul>
				<xsl:for-each select="./documentNode">
					<xsl:call-template name="addDocNodeToContents"/>
				</xsl:for-each>
			</ul>
		</xsl:if>
	</li>
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
