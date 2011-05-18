<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">

  <!--<xsl:include href="service-params.xsl"/>-->
  <xsl:template match="service">
    <xsl:param name="collName"/>
    <xsl:variable name="subaction" select="../pageRequest/@subaction"/>
    <div class="queryform">
      <p> 
	<form name="QueryForm" method="get" action="{$library_name}">
	  <input type="hidden" name="a" value="q"/>
	  <input type="hidden" name="sa" value="{$subaction}"/>
	  <input type="hidden" name="rt" value="rd"/>
	  <input type="hidden" name="s" value="{@name}"/>
	  <input type="hidden" name="c" value="{$collName}"/>
	  <input type="hidden" name="startPage" value="1"/>
	  <xsl:apply-templates select="paramList"/>
	</form>
      </p> 
    </div>
  </xsl:template>
  
  <!-- a special handling of the param list - we override the one in service-params -->
  <xsl:template match="paramList" priority='2'>  
	<xsl:choose>
		<xsl:when test="/page/pageResponse/collection/@type = 'lucene'">
			<span class="textselect">
				Search in
				<xsl:apply-templates select="param[@name='index']"><xsl:with-param name="default"><xsl:apply-templates select="param[@name='index']" mode="calculate-default"/></xsl:with-param></xsl:apply-templates>
				<xsl:if test="param[@name='indexSubcollection']">
					of <xsl:apply-templates select="param[@name='indexSubcollection']"><xsl:with-param name="default"><xsl:apply-templates select="param[@name='indexSubcollection']" mode="calculate-default"/></xsl:with-param></xsl:apply-templates>
				</xsl:if>
				<xsl:if test="param[@name='indexLanguage']">
					in <xsl:apply-templates select="param[@name='indexLanguage']"><xsl:with-param name="default"><xsl:apply-templates select="param[@name='indexLanguage']" mode="calculate-default"/></xsl:with-param></xsl:apply-templates>
				</xsl:if>
				<xsl:if test="param[@name='sortBy']">
					, sorting results by <xsl:apply-templates select="param[@name='sortBy']"><xsl:with-param name="default"><xsl:apply-templates select="param[@name='sortBy']" mode="calculate-default"/></xsl:with-param></xsl:apply-templates>
				</xsl:if>

				<xsl:call-template name='query_mode'/>
			</span>
		</xsl:when>
		<xsl:otherwise>
			<span class="textselect">
			  Search for
			  <xsl:apply-templates select="param[@name='index']"><xsl:with-param name="default"><xsl:apply-templates select="param[@name='index']" mode="calculate-default"/></xsl:with-param></xsl:apply-templates>
			  <xsl:if test="param[@name='indexSubcollection']">
				of <xsl:apply-templates select="param[@name='indexSubcollection']"><xsl:with-param name="default"><xsl:apply-templates select="param[@name='indexSubcollection']" mode="calculate-default"/></xsl:with-param></xsl:apply-templates>
			  </xsl:if>
			  <xsl:if test="param[@name='indexLanguage']">
				in <xsl:apply-templates select="param[@name='indexLanguage']"><xsl:with-param name="default"><xsl:apply-templates select="param[@name='indexLanguage']" mode="calculate-default"/></xsl:with-param></xsl:apply-templates>
			  </xsl:if>

			  <xsl:if test="param[@name='level' and not(@type='invisible')]">
				at <xsl:apply-templates select="param[@name='level']"><xsl:with-param name="default"><xsl:apply-templates select="param[@name='level']" mode="calculate-default"/></xsl:with-param></xsl:apply-templates> level
			  </xsl:if>

			  <xsl:call-template name='query_mode'/>

			</span>	
		</xsl:otherwise>
	</xsl:choose>
    <span class="querybox"> 
      <xsl:call-template name="query-and-submit"/>
    </span>
  </xsl:template>
  
  <!-- new template for match mode -->
  <xsl:template name='query_mode'>
    <xsl:variable name="qt" select="/page/pageRequest/paramList/param[@name='qt']/@value"/>
    <xsl:choose>
      <xsl:when test="$qt=1">
	<xsl:variable name="qfm" select="/page/pageRequest/paramList/param[@name='qfm']/@value"/>
	<xsl:choose>
	  <xsl:when test="$qfm=1">
	    <!-- and display results in <xsl:apply-templates select="param[@name='sortBy']"><xsl:with-param name="default"><xsl:apply-templates select="param[@name='sortBy']" mode="calculate-default"/></xsl:with-param></xsl:apply-templates> order -->
	  </xsl:when>
	  <xsl:otherwise>
	    which contain <xsl:apply-templates select="param[@name='matchMode']"><xsl:with-param name="default"><xsl:apply-templates select="param[@name='matchMode']" mode="calculate-default"/></xsl:with-param></xsl:apply-templates> of
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:when>
      <xsl:otherwise>
	<xsl:variable name="ct" select="/page/pageRequest/paramList/param[@name='ct']/@value"/>
	<xsl:variable name="qm" select="/page/pageRequest/paramList/param[@name='qm']/@value"/>
	<xsl:choose>
	  <xsl:when test="$qm=1 and $ct=0">
	    using ranked/bool query
	  </xsl:when>
	  <xsl:when test="$qm=1 and $ct=1">
	    and display results in <xsl:apply-templates select="param[@name='sortBy']"><xsl:with-param name="default"><xsl:apply-templates select="param[@name='sortBy']" mode="calculate-default"/></xsl:with-param></xsl:apply-templates> order
	  </xsl:when>
	  <xsl:otherwise>
	    which contain <xsl:apply-templates select="param[@name='matchMode']"><xsl:with-param name="default"><xsl:apply-templates select="param[@name='matchMode']" mode="calculate-default"/></xsl:with-param></xsl:apply-templates> of the words
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="param[@name='matchMode']" mode='query1'>
    <xsl:param name='default'/>
    <xsl:variable name="qfm" select="/page/pageRequest/paramList/param[@name='qfm']/@value"/>
    <xsl:choose>
      <xsl:when test="$qfm=1">
	and display results in <xsl:apply-templates select="../param[@name='sortBy']"><xsl:with-param name="default" select="$default"/></xsl:apply-templates> order
      </xsl:when>
      <xsl:otherwise>
	which contain <xsl:apply-templates select='.'><xsl:with-param name="default" select="$default"/></xsl:apply-templates> of the words
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="query-and-submit">
    <xsl:variable name="qt" select="/page/pageRequest/paramList/param[@name='qt']/@value"/>
    <xsl:choose>
      <xsl:when test="$qt=1"> <!-- doing form query -->
	<xsl:variable name="qfm" select="/page/pageRequest/paramList/param[@name='qfm']/@value"/>
	<xsl:choose>
	  <xsl:when test="$qfm=1">
	    <xsl:apply-templates select="param[@name='complexField']"/>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:apply-templates select="param[@name='simpleField']"/>
	  </xsl:otherwise>
	</xsl:choose>
	<input type="submit"><xsl:attribute name="value"><xsl:value-of select="../displayItem[@name='submit']"/></xsl:attribute></input>
	
      </xsl:when>
      <xsl:otherwise> <!-- doing text query -->
	<xsl:variable name="qb" select="/page/pageRequest/paramList/param[@name='qb']/@value"/>
	<xsl:choose>
	  <xsl:when test="$qb=1"><!-- large query box -->
	    <textarea name="s1.query" cols='63' rows='10'>
	      <xsl:apply-templates select="param[@name='query']" mode="calculate-default"/><xsl:text> </xsl:text><!-- put a space here just in case there is no value- mozilla craps out if have a <textarea /> element -->
	    </textarea>
	    <input type="submit"><xsl:attribute name="value"><xsl:value-of select="../displayItem[@name='submit']"/></xsl:attribute></input>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:variable name="qs"><xsl:apply-templates select="param[@name='query']" mode="calculate-default"/></xsl:variable>
	    <nobr><xsl:apply-templates select="param[@name='query']"><xsl:with-param name="default" select="java:org.greenstone.gsdl3.util.XSLTUtil.tidyWhitespace($qs)"/></xsl:apply-templates><input type="submit"><xsl:attribute name="value"><xsl:value-of select="../displayItem[@name='submit']"/></xsl:attribute></input></nobr>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- overwrite the multi param to use our value of occurs, not the one with the param -->
  <xsl:template match="param[@type='multi']" priority='2'>
    <xsl:variable name="parent" select="@name"/>
    <table>
      <tr>
	<xsl:for-each select="param">
	  <xsl:variable name='pname' select='@name'/>
	  <td><xsl:value-of select="displayItem[@name='name']"/></td>
	</xsl:for-each>
      </tr>
      <!-- the number of times to display this is the qfn variable -->
      <xsl:variable name="numbox"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='qfn']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='qfn']/@value"/></xsl:when><xsl:otherwise>4</xsl:otherwise></xsl:choose></xsl:variable>
      <xsl:apply-templates select="." mode="contents"><xsl:with-param name="occurs" select="$numbox"/><xsl:with-param name="pos" select="0"/></xsl:apply-templates>
    </table>
  </xsl:template>

  <xsl:template match="param[@type='multi']" mode="contents">
    <xsl:param name="occurs"/>
    <xsl:param name="ns">s1.</xsl:param>
    <xsl:param name="pos"/>
    <!--   <xsl:variable name="pos" select="@occurs - $occurs"/>-->
    <tr><xsl:for-each select="param">
	<xsl:variable name="pname" select="@name"/>
	<xsl:variable name="values" select="/page/pageRequest/paramList/param[@name=concat($ns,$pname)]/@value"/>
	<td><xsl:choose>
	    <xsl:when test="not(@ignore) or  @ignore != $pos">      
	      <xsl:apply-templates select='.'><xsl:with-param name="default" select="java:org.greenstone.gsdl3.util.XSLTUtil.getNumberedItem($values, $pos)"/></xsl:apply-templates>
	    </xsl:when>
	    <xsl:otherwise><!-- put in a hidden placeholder -->
	      <input type="hidden" name='{$ns}{@name}' value=''/>
	    </xsl:otherwise>
	  </xsl:choose></td>
      </xsl:for-each></tr> 
    <!-- recursively call this template to get multiple entries -->
    <xsl:if test="$pos &lt; ($occurs - 1)">
      <xsl:apply-templates select="." mode="contents"><xsl:with-param name="occurs" select="$occurs"/><xsl:with-param name="pos" select="$pos+1"/></xsl:apply-templates>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
