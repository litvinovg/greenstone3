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
								<a>
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
						<li>
							<a href="http://www.greenstone.org">
								Greenstone Home
							</a>
						</li>
						
					</ul>
				</div>
		    
		    </div>

    <div id="content" class="noSidebar">
		<gslib:uiItem name="collectionAbout" />
    </div>

   <div id="footer" >
    	<gslib:uiItem name="collectionFooter" />
   </div>
  </div>

  </body>
 </html>
</xsl:template>


</xsl:stylesheet>
