<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  extension-element-prefixes="java"
  exclude-result-prefixes="java">
  
  <xsl:output method="html"/>  

  <xsl:template match="page">
    <html>
      <head>
      	
      </head>
	  <body>
      <frameset rows="68,*" noresize="" border="0">
	<frame frameborder="0"><xsl:attribute name="src">?a=p&amp;amp;sa=nav&amp;amp;c=<xsl:value-of select="/page/pageRequest/paramList/param[@name='c']/@value" /></xsl:attribute></frame>
	<frame frameborder="0"><xsl:attribute name="src"><xsl:value-of select="/page/pageRequest/paramList/param[@name='url']/@value"/></xsl:attribute></frame>
	<noframes>
	  <p class="getTextFor textframebrowser">&amp;amp;nbsp;</p>
	</noframes>
      </frameset>
		<script type="text/javascript" src="jquery.js"><!-- jQuery --></script>
		<script type="text/javascript" src="test.js"><!-- Scripts for client side XSL transformations --></script>
	  </body>
    </html>
  </xsl:template>

</xsl:stylesheet>
