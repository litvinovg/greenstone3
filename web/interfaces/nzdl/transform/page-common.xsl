<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template name="greenstone_info">
    <p />
    <table>
      <tr valign='top'>
	<td><xsl:call-template name="textpoem"/></td>
	<td><xsl:call-template name="imagegreenstone"/></td>
      </tr></table>
    <p />
    <table><tr><td><xsl:call-template name="textgreenstone"/></td></tr></table>
  </xsl:template>

  <xsl:template name="imagegreenstone">
    <img src="interfaces/nzdl/images/gsdl.gif" width="140" height="77" border="0" hspace='0'><xsl:attribute name='alt'><xsl:call-template name='textimagegreenstone'/></xsl:attribute></img>
  </xsl:template>

</xsl:stylesheet>
