<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl">
		<!-- exclude-result-prefixes="#default">-->

  <!-- Adding the xml processing instruction, see http://www.xml.com/lpt/a/1027 -->
  <xsl:output method="xml" version="1.0" encoding="utf-8"/>
  

  <!-- select only the rss child of /page/pageResponse and then copy all its attributes and 
       descendant elements and their attributes too into the output 
       http://stackoverflow.com/questions/1141216/xsl-copy-the-entire-xml-except-a-parent-node-but-keep-its-child-node
       http://stackoverflow.com/questions/5378610/copy-xml-document-with-all-elements-except-a-black-list-of-elements
       http://stackoverflow.com/questions/5876382/using-xslt-to-copy-all-nodes-in-xml

       To do a straightforward copy of input into output:
       <xsl:template match="/">
	 <xsl:copy-of select="." />
       </xsl:template>
    -->

  <xsl:template match="/page">
    <xsl:choose>
      <xsl:when test="pageResponse/rss">
	<xsl:apply-templates select="pageResponse/rss"/>
      </xsl:when>
      <xsl:when test="descendant::error">
	  <div id="error">
            <xsl:apply-templates select="descendant::error"/>
	  </div>
      </xsl:when>
      <xsl:otherwise><!-- do what?-->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--Identity template, copies all content of matching template(s) into the output -->
  <xsl:template match="@* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>  

