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
	    			<div id="subServiceList" class="linklist">
						<ul>
							<xsl:for-each select="$classifiers">
								<li>
									<xsl:choose>
										<xsl:when test=". = $currentClassifier">
											<gslib:classifier_title/>
										</xsl:when>
										<xsl:otherwise>
											<a>
												<xsl:attribute name="title"><gslib:classifier_description/></xsl:attribute>
												<xsl:attribute name="href"><gslib:classifier_url/></xsl:attribute>
										
												<gslib:classifier_title/>									
											</a>
										</xsl:otherwise>
									</xsl:choose>
								</li>						
							</xsl:for-each>
						</ul>
	    			</div>
			
					<div id="classifierData">
						<ul>
							<xsl:for-each select="$topLevelClassifierNodes">
								<xsl:call-template name="classifierNode"/>
							</xsl:for-each>
						</ul>
					</div>
	    		</div>
	
				<div id="footer" >
					<gslib:uiItem name="collectionFooter" /> 
				</div>
			</div>
		</body>
	</html>
</xsl:template>

<!-- default browsing format statements -->

<xsl:template name="classifierNode">
	<li>
		<xsl:attribute name="class">
			<xsl:choose>
				<xsl:when test="./classifierNode or ./documentNode">expandedNode</xsl:when>
				<xsl:otherwise>collapsedNode</xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
		
		<a>
			<xsl:attribute name="href"><gslib:classifierNode_url/></xsl:attribute>
			<gslib:classifierNode_title/>
		</a>
		
		<xsl:if test="./documentNode or ./classifierNode">
		<ul>
			<xsl:for-each select="./classifierNode">
				<xsl:call-template name="classifierNode"/>
			</xsl:for-each>
		
			<xsl:for-each select="./documentNode">
				<xsl:call-template name="documentNode"/>
			</xsl:for-each>
		</ul>
		</xsl:if>
	</li>
</xsl:template>

<xsl:template name="documentNode">
	<li class="leafNode">
		<a>
			<xsl:attribute name="href"><gslib:documentNode_url/>&amp;ec=1</xsl:attribute>
			<gslib:documentNode_title/>
		</a>
	</li>
</xsl:template>


</xsl:stylesheet>
