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
					
					<xsl:if test="$search_query != ''">
						<xsl:choose>
							<xsl:when test="$search_docsReturned = 0">
								<div class="searchSummary">    
									Your search - <em><gslib:search_query/></em> - did not return any results.
		    					</div> 
							</xsl:when>
							<xsl:otherwise>
		     					<div class="searchSummary">    
									Results <em><gslib:search_startIndex/></em> to <em><gslib:search_endIndex/></em> 
									of about <em><gslib:search_docsReturned/></em>
									for
									<xsl:for-each select="$search_queryTerms">
										<a>
											<xsl:attribute name="title">
												found in <gslib:search_queryTerm_freq/> documents								
											</xsl:attribute>
											<gslib:search_queryTerm_text />
										</a>
									</xsl:for-each>
		    					</div> 
								
								<div class="searchResults">
								<ul>
									<xsl:for-each select="$search_results">
										<li class="leafNode">
											<gslib:metadata name="ancestors_*: *_Title"/> / 
											<br/>
											<a>
												<xsl:attribute name="href"><gslib:documentNode_url/>&amp;ec=1</xsl:attribute>
												
												<gslib:documentNode_title/>
											</a>
										</li>
									</xsl:for-each>
								</ul>
								</div>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:if>
				</div>

				<div id="footer" >
					<gslib:uiItem name="collectionFooter" />
				</div>
			</div>
		</body>
	</html>
</xsl:template>



</xsl:stylesheet>
