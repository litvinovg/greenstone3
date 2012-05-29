<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  extension-element-prefixes="java">

  <!-- handling of the different types of params on a service form 
  - these now only output the selection box/text box etc, not the name -->

  <!-- the default param list handling -->
  <xsl:template match="paramList">
    <xsl:param name="ns">s1.</xsl:param>
    <ul id="queryitemlist">
      <xsl:for-each select="param">
	<xsl:choose>
	  <xsl:when test="@type='multi'">
	    <li><xsl:apply-templates select='.'><xsl:with-param name="ns" select="$ns"/></xsl:apply-templates></li>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:variable name="pvalue"><xsl:apply-templates select="." mode="calculate-default"><xsl:with-param name="ns" select="$ns"/></xsl:apply-templates></xsl:variable>
	    <li><xsl:value-of select="displayItem[@name='name']"/><xsl:apply-templates select="."><xsl:with-param name="default" select="$pvalue"/><xsl:with-param name="ns" select="$ns"/></xsl:apply-templates></li>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:for-each>
    </ul>
  </xsl:template>
  
  <!-- puts all the params into a=p&p=h type form - need to change this if use 
  multi params  -->
  <xsl:template match="paramList" mode="cgi">
    <xsl:param name="ns">s1.</xsl:param>
    <xsl:for-each select="param">
      <xsl:variable name='pname' select="@name"/>
      <xsl:text>&amp;</xsl:text><xsl:value-of select="$ns"/><xsl:value-of select="@name"/>=<xsl:apply-templates select="." mode="calculate-default"><xsl:with-param name='ns' select='$ns'/></xsl:apply-templates>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="param" mode="calculate-default">
    <xsl:param name="ns">s1.</xsl:param>
    <xsl:variable name="pname"><xsl:value-of select="$ns"/><xsl:value-of select="@name"/></xsl:variable>
    <xsl:choose>
      <xsl:when test="/page/pageRequest/paramList/param[@name=$pname]">
	<xsl:choose>
	  <xsl:when test="@type='enum_multi'"><xsl:text>,</xsl:text>
	    <xsl:for-each select="/page/pageRequest/paramList/param[@name=$pname]">
	      <xsl:value-of select="@value"/>,
	    </xsl:for-each>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:value-of select="/page/pageRequest/paramList/param[@name=$pname]/@value"/>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:when>
      <xsl:otherwise>
	<xsl:value-of select="@default"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- invisible params - used by other stuff. in the query form, we set to teh default -->
  <xsl:template match="param[@type='invisible']">
    <xsl:param name="ns">s1.</xsl:param>
    <input type='hidden' name='{$ns}{@name}' value='{@default}'/>
  </xsl:template>
  <!-- boolean params -->
  <xsl:template match="param[@type='boolean']">
    <xsl:param name="ns">s1.</xsl:param>
    <xsl:param name="default"/>
    <select name='{$ns}{@name}'>
      <option value="0"><xsl:if test="$default='0'"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="option[@name='0']/displayItem[@name='name']"/></option>
      <option value="1"><xsl:if test="$default='1'"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="option[@name='1']/displayItem[@name='name']"/></option>
    </select>
  </xsl:template>
  
  <!-- integer params -->
  <xsl:template match="param[@type='integer']">
    <xsl:param name="ns">s1.</xsl:param>
    <xsl:param name="default"/>
    <input type="text" name="{$ns}{@name}" size="3" value="{$default}"/>
  </xsl:template>

  <!-- single selection enum params -->
  <xsl:template match="param[@type='enum_single']">
    <xsl:param name="ns">s1.</xsl:param>
    <xsl:param name="default"/>
    <xsl:choose>
      <xsl:when test="count(option) = 1">
	<xsl:value-of select="option/displayItem[@name='name']"/>
	<input type='hidden' name='{$ns}{@name}'><xsl:attribute name='value'><xsl:value-of  select='option/@name'/></xsl:attribute></input>
      </xsl:when>
      <xsl:otherwise>
	<select name="{$ns}{@name}">
	  <xsl:for-each select="option">
	    <option value="{@name}"><xsl:if test="@name=$default"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="displayItem[@name='name']"/></option>
	  </xsl:for-each>
	</select>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- multiple selection enum params -->
  <!-- how to do defaults for this?? -->
  <xsl:template match="param[@type='enum_multi']">
    <xsl:param name="ns">s1.</xsl:param>
    <xsl:param name="default"/>
    <select name="{$ns}{@name}" size='2'><xsl:attribute name="multiple"></xsl:attribute>
      <xsl:for-each select="option">
	<option value="{@name}"><xsl:if test="contains($default, concat(',', @name, ','))"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="displayItem[@name='name']"/></option>
      </xsl:for-each>
    </select>
  </xsl:template>
  
  <!-- string params -->
  <xsl:template match="param[@type='string']">
    <xsl:param name="ns">s1.</xsl:param>
    <xsl:param name="default"/>
    <input type="text" name="{$ns}{@name}" size="50" value="{$default}"/>
  </xsl:template>
  
  <!-- large string  params -->
  <xsl:template match="param[@type='text']">
    <xsl:param name="ns">s1.</xsl:param>
    <xsl:param name="default"/>
    <textarea name="{$ns}{@name}" cols="50" rows="3"><xsl:value-of select='$default'/></textarea>
  </xsl:template>

  <!-- multi params - params that are combinations of other params -->
  <xsl:template match="param[@type='multi']">
    <xsl:param name="ns">s1.</xsl:param>
    <xsl:variable name="parent" select="@name"/>
    <table>
      <tr class="queryfieldheading"><xsl:value-of select="displayItem[@name='name']"/>
      	<xsl:for-each select="param">
	  <td class="queryfieldname"><xsl:value-of select="displayItem[@name='name']"/></td>
	</xsl:for-each>
      </tr>
      
      <xsl:apply-templates select="." mode="contents"><xsl:with-param name="occurs" select="@occurs"/><xsl:with-param name="ns" select="$ns"/></xsl:apply-templates>
    </table>
  </xsl:template>

  <xsl:template match="param[@type='multi']" mode="contents">
    <xsl:param name="ns">s1.</xsl:param>
    <xsl:param name="occurs">1</xsl:param>
    <xsl:variable name="pos" select="@occurs - $occurs"/>	
    <tr class="queryfieldrow"><xsl:for-each select="param">
	<xsl:variable name="pname" select="@name"/>
	<xsl:variable name="values" select="/page/pageRequest/paramList/param[@name=$pname]/@value"/>
	<td class="queryfieldcell"><xsl:choose>
	    <xsl:when test="not(@ignore) or  @ignore != $pos">      
	      <xsl:apply-templates select='.'><xsl:with-param name="default" select="java:org.greenstone.gsdl3.util.XSLTUtil.getNumberedItem($values, $pos)"/><xsl:with-param name="ns" select="$ns"/></xsl:apply-templates>
	    </xsl:when>
	    <xsl:otherwise><!-- put in a hidden placeholder -->
	      <input type="hidden" name='{$ns}{@name}' value=''/>
	    </xsl:otherwise>
	  </xsl:choose></td>
      </xsl:for-each></tr>
    <!-- recursively call this template to get multiple entries -->
    <xsl:if test="$occurs &gt; 1">
      <xsl:apply-templates select="." mode="contents"><xsl:with-param name="occurs" select="$occurs - 1"/><xsl:with-param name="ns" select="$ns"/></xsl:apply-templates>
    </xsl:if>
  </xsl:template>

  
</xsl:stylesheet>  

  <!-- a param list that puts params in pairs- wont work as is with new
  param handling stuff -->
  <!--
  <xsl:template match="paramList">
    <p/><table width="537">
      <xsl:choose>
        <xsl:when test='count(param)>4'>
          <xsl:for-each select="param[position() mod 2 = 1]"> 
            <tr><xsl:apply-templates select="."/>
              <xsl:if test="following-sibling::param[1]"><xsl:apply-templates select='following-sibling::param[1]'/></xsl:if></tr>
          </xsl:for-each>
        </xsl:when>
	<xsl:otherwise>
	  <xsl:for-each select="param">
	    <tr><xsl:apply-templates select='.'/></tr>
	  </xsl:for-each>
	</xsl:otherwise>
      </xsl:choose>
    </table>
  </xsl:template>
  -->