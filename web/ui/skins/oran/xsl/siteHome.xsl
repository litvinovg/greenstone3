<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:gslib="http://www.greenstone.org/skinning">

<xsl:import href="./util.xsl"/>

<xsl:template match="/">
	<html>
 		<head>
			<title>
				<gslib:metadataItem name="siteName" /> / <gslib:metadataItem name="collectionName" />
			</title>
			
			<gslib:css/>
			<gslib:js/>
  		</head>
  		<body>
			<div id="container">

				<div id="header">
					<div class="title">
						<gslib:uiItem name="siteHeader" />
					</div>
				</div>

				<div id="sidebar" class="index">
					<xsl:for-each select="$collections">						
						<div class="itemName">		
							<a>
								<xsl:attribute name="href"><gslib:collection_url/></xsl:attribute>
								<gslib:collection_title/>
							</a>
						</div>

					</xsl:for-each>	
				</div>

    			<div id="content" class="withSidebar">
     				<gslib:uiItem name="siteAbout" />
    			</div>

   				<div class="centering" id="footer">
    				<gslib:uiItem name="siteFooter" />
  				</div>
  			</div>
		</body>
 	</html>
</xsl:template>

</xsl:stylesheet>
