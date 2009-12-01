<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output 
method="xml" 
encoding="UTF-8" 
doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" 
doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
omit-xml-declaration="yes"
indent="yes"/>

  
<xsl:template match="/">
 <html>
  <head>
    <title> Greenstone skinning exception </title>
  </head>
  <body>
   <h1> error </h1>
   <p> the current skin specified an xslt file that does not exist. Please check that the skin is complete and that it's configuration file (skin.xml) specifies suitable xslt files for all actions. </p>
  </body>
 </html>
</xsl:template>

</xsl:stylesheet>
