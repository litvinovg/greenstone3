<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">
  
  <xsl:output method="html"/>  

  <xsl:template match="page">
    <html>
      <head></head>
      <frameset rows="68,*" noresize="" border="0">
	<frame frameborder="0"><xsl:attribute name="src">?a=p&amp;sa=nav&amp;c=<xsl:value-of select="/page/pageRequest/paramList/param[@name='c']/@value" /></xsl:attribute></frame>
	<frame frameborder="0"><xsl:attribute name="src"><xsl:value-of select="/page/pageRequest/paramList/param[@name='url']/@value"/></xsl:attribute></frame>
	<noframes>
	  <p><xsl:value-of select="util:getInterfaceText('default', /page/@lang, 'textframebrowser')"/></p>
	</noframes>
      </frameset>
    </html>
  </xsl:template>

</xsl:stylesheet>