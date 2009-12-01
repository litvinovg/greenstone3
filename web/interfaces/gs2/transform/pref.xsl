<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">
  
  <!-- style includes global params interface_name, library_name -->
  <xsl:include href="style.xsl"/>
  <xsl:output method="html"/>  
  
  <!-- the main page layout template is here -->
  <xsl:template match="page">
    <html>
      <head>
	<title>
	  <!-- put a space in the title in case the actual value is missing - mozilla will not display a page with no title-->
	  <xsl:call-template name="pageTitle"/><xsl:text> </xsl:text>
	</title>
	<xsl:call-template name="globalStyle"/>
	<xsl:call-template name="pageStyle"/>
      </head>
      <body>
	<xsl:attribute name="dir"><xsl:call-template name="direction"/></xsl:attribute>
	<div id="page-wrapper">
	  <xsl:apply-templates select="pageResponse"/>
	  <xsl:call-template name="greenstoneFooter"/>                	   
	</div>
      </body>
    </html>
  </xsl:template>
  
  <xsl:template name="pageTitle">
    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_tip')"/>
  </xsl:template>

  <!-- page specific style goes here -->
  <xsl:template name="pageStyle">
    <script type="text/javascript" src="interfaces/gs2/js/pref.js"><xsl:text disable-output-escaping="yes"> </xsl:text></script>
  </xsl:template>

  <xsl:template match="pageResponse">
    <xsl:variable name="collName" select="/page/pageRequest/paramList/param[@name='c']/@value"/>
    <xsl:variable name="collType" select="/page/pageRequest/paramList/param[@name='ct']/@value"/>
    <!-- check whether these search modes are supported -->    
    <xsl:variable name="simplestatus"><xsl:choose><xsl:when test="/page/pageResponse//service[@name='TextQuery']">y</xsl:when><xsl:otherwise>n</xsl:otherwise></xsl:choose></xsl:variable>
    <xsl:variable name="formstatus"><xsl:choose><xsl:when test="/page/pageResponse//service[@name='FieldQuery']">y</xsl:when><xsl:otherwise>n</xsl:otherwise></xsl:choose></xsl:variable>
    <xsl:variable name="advancedstatus"><xsl:choose><xsl:when test="/page/pageResponse//service[@name='AdvancedFieldQuery']">y</xsl:when><xsl:otherwise>n</xsl:otherwise></xsl:choose></xsl:variable>
    
    <center>
      <xsl:call-template name="standardPageBanner">
	<xsl:with-param name="collName" select="$collName"/>
	<xsl:with-param name="pageType">pref</xsl:with-param>
      </xsl:call-template>
      <xsl:call-template name="navigationBar">
	<xsl:with-param name="collName" select="$collName"/>
      </xsl:call-template>
      <form name="PrefForm" method="get" action="{$library_name}" onsubmit="return checkForm();">        
	<input type='hidden' name='a' value='p'/>
	<input type='hidden' name='sa' value='pref'/>
        <input type='hidden' name='c' value="{$collName}"/>
	<input type='hidden' name='ct' value="{$collType}"/>
        
	<table width="800">
          <tr><td><div class="formheading"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.prespref')"/></div></td></tr>
          <tr><td><xsl:call-template name="pres-prefs"/></td></tr>
	  <tr><td><div class="formheading"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.searchpref')"/></div></td></tr>
          <tr><td><xsl:call-template name="search-mode-prefs"><xsl:with-param name="simplestatus" select="$simplestatus"/><xsl:with-param name="formstatus" select="$formstatus"/><xsl:with-param name="advancedstatus" select="$advancedstatus"/></xsl:call-template></td></tr>
	</table>
      </form>
    </center>
  </xsl:template>

  
  <xsl:template name="pres-prefs">
    <table>
      <tr><td><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.interfacelang')"/></td><td align='left'><xsl:call-template name="lang-list"/><!--English--></td></tr>      
      <!--<tr><td><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.encoding')"/></td><td align='left'>UTF-8</td></tr> -->
    </table>
  </xsl:template>
  
  <xsl:template name="lang-list">
    <xsl:variable name="current" select="/page/@lang"/>
    <select name="l">
      <xsl:for-each select="/page/pageResponse/languageList/language">        
	<option value="{@name}"><xsl:if test="@name=$current"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:if test="displayItem"><xsl:value-of select="displayItem[@name='name']"/></xsl:if><xsl:if test="not(displayItem)"><xsl:value-of select="@displayname"/></xsl:if></option>
      </xsl:for-each>
    </select>
  </xsl:template>
  
  <xsl:template name="search-mode-prefs">
    <xsl:param name="ns">s1.</xsl:param>
    <!-- variables that indicates whether these mode are supported by current collection -->
    <xsl:param name="simplestatus"/>
    <xsl:param name="formstatus"/>
    <xsl:param name="advancedstatus"/>
    
    <!-- search type : 0(default, simple); 1(form) -->
    <xsl:variable name="qt"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='qt']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='qt']/@value"/></xsl:when><xsl:otherwise>0</xsl:otherwise></xsl:choose></xsl:variable>    
    <!-- search form type : 0(default, simple form); 1(advanced form) -->
    <xsl:variable name="queryfmode"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='qfm']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='qfm']/@value"/></xsl:when><xsl:otherwise>0</xsl:otherwise></xsl:choose></xsl:variable>    
    <!-- search mode: text, form, advanced -->
    <xsl:variable name="mode"><xsl:choose><xsl:when test="$qt=0">text</xsl:when><xsl:when test="$queryfmode=0">form</xsl:when><xsl:when test="$queryfmode=1">advanced</xsl:when><xsl:otherwise>text</xsl:otherwise></xsl:choose></xsl:variable>
    
    <table>
      <tr><td> </td><td> </td><td align='right'><input type='submit' name="submit"><xsl:attribute name="value"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.set_prefs')"/></xsl:attribute></input></td></tr>
      <!-- search type -->       
      <!-- values are updated by either xslt or javascript -->      
      <input type="hidden" name="qt" value="{$qt}"/>
      
      <tr><td valign='baseline' class='col1'><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.search_type')"/></td><td class='col2'><input type='radio' name='qfm' value='2' onclick="javascript:changePref(2)"><xsl:if test="$qt=0 or $queryfmode=2"><xsl:attribute name="checked"/></xsl:if></input></td><td class='col3'> <font><xsl:if test="$qt=0 or $queryfmode=2"><xsl:attribute name="style">font-weight: bold;</xsl:attribute></xsl:if><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.search_type_text')"/></font> 
	  
	  <xsl:if test="$formstatus='y'"><input name="qfm" onclick="javascript:changePref(0)" type="radio" value="0"><xsl:if test="$mode='form'"><xsl:attribute name="checked"/></xsl:if></input> <font><xsl:if test="$mode='form'"><xsl:attribute name="style">font-weight: bold;</xsl:attribute></xsl:if><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.search_type_simple_form')"/></font></xsl:if>
	  
	  <xsl:if test="$advancedstatus='y'"><input name="qfm" onclick="javascript:changePref(1)" type="radio" value="1" ><xsl:if test="$mode='advanced'"><xsl:attribute name="checked"/></xsl:if></input> <font><xsl:if test="$mode='advanced'"><xsl:attribute name="style">font-weight: bold;</xsl:attribute></xsl:if><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.search_type_advanced_form')"/></font></xsl:if>
	</td></tr>
      
      <!-- search options -->
      <tr><td><div class="formheading"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.search_options')"/></div></td></tr>
      <xsl:call-template name="search-options-prefs"><xsl:with-param name="mode" select="$mode"/><xsl:with-param name="simplestatus" select="$simplestatus"/><xsl:with-param name="formstatus" select="$formstatus"/><xsl:with-param name="advancedstatus" select="$advancedstatus"/></xsl:call-template>
    </table>
  </xsl:template>
  
  <xsl:template name="search-options-prefs">
    <xsl:param name="ns">s1.</xsl:param>
    <xsl:param name="mode">text</xsl:param>
    <xsl:param name="simplestatus"/>
    <xsl:param name="formstatus"/>
    <xsl:param name="advancedstatus"/>
    
    <!-- extract service parameters, avoid hard coding params in this xslt --> 
    <!-- mode specific options -->
    <xsl:call-template name="text-search-prefs"><xsl:with-param name="display"><xsl:if test="$mode='text'">y</xsl:if><xsl:if test="$mode='advanced' or $mode='form'">n</xsl:if></xsl:with-param></xsl:call-template>
    <!-- simple form search mode doesn't have any specific options -->
    <!-- <xsl:if test="$mode='form'"><xsl:apply-templates select="/page/pageResponse//service[@name='FieldQuery']"/></xsl:if> -->    
    <xsl:call-template name="advanced-search-prefs"><xsl:with-param name="display"><xsl:if test="$mode='advanced'">y</xsl:if><xsl:if test="$mode='text' or $mode='form'">n</xsl:if></xsl:with-param></xsl:call-template>
    
    <!-- search type specific options -->
    <xsl:apply-templates select="/page/pageResponse//service[@name='TextQuery']">
      <xsl:with-param name="display"><xsl:if test="$mode='text' or $mode='form'">y</xsl:if><xsl:if test="$mode='advanced'">n</xsl:if></xsl:with-param>
      <xsl:with-param name="prefix">tf</xsl:with-param>
    </xsl:apply-templates>
    <xsl:apply-templates select="/page/pageResponse//service[@name='AdvancedFieldQuery']">
      <xsl:with-param name="display"><xsl:if test="$mode='advanced'">y</xsl:if><xsl:if test="$mode='text' or $mode='form'">n</xsl:if></xsl:with-param>
      <xsl:with-param name="prefix">adv</xsl:with-param>
    </xsl:apply-templates>
    
    <!-- General options -->
    <!-- sort order -->
    <xsl:variable name="sort"><xsl:choose><xsl:when test="/page/pageResponse//service[@name='TextQuery']//param[@name='sortBy']">y</xsl:when><xsl:otherwise>n</xsl:otherwise></xsl:choose></xsl:variable>
    <xsl:if test="$sort='y'"> 
      <xsl:variable name="sortvar" select="concat($ns,'sortBy')"/>    
      <xsl:variable name="sortparam" select="//param[@name='sortBy']"/>
      <xsl:variable name="cachedValue" select="/page/pageRequest/paramList/param[@name=$sortvar]/@value"/>      
      <xsl:variable name="defaultValue" select="$sortparam/@default"/>
      <xsl:variable name="optionVal" select="$sortparam/option[1]/@name"/>
      
      <tr><td rowspan='2' valign='baseline'><xsl:value-of select="$sortparam/displayItem"/></td><td><input type='radio' name='{$sortvar}' value='{$optionVal}'><xsl:choose><xsl:when test="$cachedValue=$optionVal"><xsl:attribute name="checked"/></xsl:when><xsl:when test="normalize-space($cachedValue)='' and $optionVal=$defaultValue"><xsl:attribute name="checked"/></xsl:when></xsl:choose></input></td>
	<td><xsl:value-of select="$sortparam/option[1]/displayItem"/></td></tr>
      
      <xsl:variable name="optionVal" select="$sortparam/option[2]/@name"/>
      <tr><td><input type='radio' name='{$sortvar}' value='{$optionVal}'><xsl:choose><xsl:when test="$cachedValue=$optionVal"><xsl:attribute name="checked"/></xsl:when><xsl:when test="normalize-space($cachedValue)='' and $optionVal=$defaultValue"><xsl:attribute name="checked"/></xsl:when></xsl:choose></input></td><td><xsl:value-of select="$sortparam/option[2]/displayItem"/></td></tr>
    </xsl:if>
    
    <!-- hit display -->
    <!-- normally all the collections support customized number of returned records and displayed records. Just to be consistent with the other two options. -->
    <xsl:variable name="maxstatus"><xsl:choose><xsl:when test="/page/pageResponse//service[@name='TextQuery']//param[@name='maxDocs']">y</xsl:when><xsl:otherwise>n</xsl:otherwise></xsl:choose></xsl:variable>
    <xsl:if test="$maxstatus='y'">      
      <xsl:variable name="maxvar" select="concat($ns,'maxDocs')"/>
      <xsl:variable name="maxdocs"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name=$maxvar]"><xsl:value-of select="/page/pageRequest/paramList/param[@name=$maxvar]/@value"/></xsl:when><xsl:otherwise>50</xsl:otherwise></xsl:choose></xsl:variable>
      <xsl:variable name="hits"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='hitsPerPage']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='hitsPerPage']/@value"/></xsl:when><xsl:otherwise>20</xsl:otherwise></xsl:choose></xsl:variable>
      <xsl:variable name="maxdocs_param"><select name="{$ns}maxDocs">
	  <option value="50"><xsl:if test="$maxdocs=50"><xsl:attribute name="selected"></xsl:attribute></xsl:if>50</option>
	  <option value="100"><xsl:if test="$maxdocs=100"><xsl:attribute name="selected"></xsl:attribute></xsl:if>100</option>
	  <option value="200"><xsl:if test="$maxdocs=200"><xsl:attribute name="selected"></xsl:attribute></xsl:if>200</option>
	  <option value="-1"><xsl:if test="$maxdocs=-1"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.all')"/></option>
	</select></xsl:variable>
      <xsl:variable name="hits_param"><select name="hitsPerPage">
	  <option value="10"><xsl:if test="$hits=10"><xsl:attribute name="selected"></xsl:attribute></xsl:if>10 </option> 
	  <option value="20"><xsl:if test="$hits=20"><xsl:attribute name="selected"></xsl:attribute></xsl:if>20</option>
	  <option value="50"><xsl:if test="$hits=50"><xsl:attribute name="selected"></xsl:attribute></xsl:if>50</option>
	  <option value="-1"><xsl:if test="$hits=-1"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.all')"/></option>
	</select></xsl:variable>
      <tr><td colspan='3'><xsl:value-of select="util:getInterfaceTextWithDOM($interface_name, /page/@lang, 'pref.hits', $maxdocs_param, $hits_param)" disable-output-escaping="yes"/></td></tr>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="advanced-search-prefs">
    <xsl:param name="display">n</xsl:param>    
    <xsl:variable name="numbox"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='qfn']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='qfn']/@value"/></xsl:when><xsl:otherwise>4</xsl:otherwise></xsl:choose></xsl:variable>
    <tr id="adv-qnb"><xsl:if test="$display='n'"><xsl:attribute name="style">display:none</xsl:attribute></xsl:if>
      <td colspan='2'><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.num_fields')"/></td><td><select name="qfn">
	  <option value="2"><xsl:if test="$numbox=2"><xsl:attribute name="selected"></xsl:attribute></xsl:if>2</option> 
	  <option value="4"><xsl:if test="$numbox=4"><xsl:attribute name="selected"></xsl:attribute></xsl:if>4</option>
	  <option value="6"><xsl:if test="$numbox=6"><xsl:attribute name="selected"></xsl:attribute></xsl:if>6</option>
	  <option value="8"><xsl:if test="$numbox=8"><xsl:attribute name="selected"></xsl:attribute></xsl:if>8</option>
	</select></td><td/></tr>
  </xsl:template>

  <xsl:template name="text-search-prefs">
    <xsl:param name="display">n</xsl:param>
    <!-- query mode : 0(default, simple); 1(advanced, boolean)-->
    <xsl:variable name="querymode"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='qm']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='qm']/@value"/></xsl:when><xsl:otherwise>0</xsl:otherwise></xsl:choose></xsl:variable>
    <tr id="text-qm1"><xsl:if test="$display='n'"><xsl:attribute name="style">display:none</xsl:attribute></xsl:if><td rowspan='2' valign='baseline'>Query mode </td><td><input type='radio' name='qm' value='0'><xsl:if test="$querymode=0"><xsl:attribute name="checked"></xsl:attribute></xsl:if></input></td><td><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.query_mode_simple')"/></td></tr>
    <tr id="text-qm2"><xsl:if test="$display='n'"><xsl:attribute name="style">display:none</xsl:attribute></xsl:if><td><input type='radio' name='qm' value='1'><xsl:if test="$querymode=1"><xsl:attribute name="checked"/></xsl:if></input></td><td><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.query_mode_adv')"/></td></tr>
    
    <!-- query box size -->
    <xsl:variable name="boxsize"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='qb']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='qb']/@value"/></xsl:when><xsl:otherwise>0</xsl:otherwise></xsl:choose></xsl:variable>
    <tr id="text-qb1"><xsl:if test="$display='n'"><xsl:attribute name="style">display:none</xsl:attribute></xsl:if><td rowspan='2' valign='baseline'><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.query_box_size')"/></td><td><input type='radio' name='qb' value='0'><xsl:if test="$boxsize=0"><xsl:attribute name="checked"></xsl:attribute></xsl:if></input></td><td><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.query_box_size_reg')"/></td></tr>
    <tr id="text-qb2"><xsl:if test="$display='n'"><xsl:attribute name="style">display:none</xsl:attribute></xsl:if><td><input type='radio' name='qb' value='1'><xsl:if test="$boxsize=1"><xsl:attribute name="checked"></xsl:attribute></xsl:if></input></td><td><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.query_box_size_large')"/></td></tr>    
  </xsl:template>
  
  
  <xsl:template match="service">
    <xsl:param name="display">n</xsl:param>
    <xsl:param name="prefix"/>    
    <xsl:apply-templates select="paramList/param[not(@type='invisible')]"><xsl:with-param name="display" select="$display"/><xsl:with-param name="prefix" select="$prefix"/></xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="param"> 
    <xsl:param name="ns">s1.</xsl:param>
    <xsl:param name="display">n</xsl:param>
    <xsl:param name="prefix"/>
    
    <xsl:if test="not(@name='matchMode') and not(@name='level') and not(@name='index') and not(@name='sortBy') and not(@name='indexSubcollection') and not(@name='indexLanguage') and (@type='boolean' or @type='enum_single')"><xsl:apply-templates select="." mode="radio"><xsl:with-param name="display" select="$display"/><xsl:with-param name="prefix" select="$prefix"/><xsl:with-param name="paramIdx" select="position()"/></xsl:apply-templates></xsl:if>    
  </xsl:template>
  
  <xsl:template match="param" mode="radio"> 
    <xsl:param name="ns">s1.</xsl:param>
    <xsl:param name="display">n</xsl:param>
    <xsl:param name="prefix"/>
    <xsl:param name="paramIdx"/>
    
    <xsl:variable name="cachedName" select="concat($ns, @name)"/>
    <xsl:variable name="cachedValue"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name=$cachedName]"><xsl:value-of select="/page/pageRequest/paramList/param[@name=$cachedName]/@value"/></xsl:when><xsl:otherwise><xsl:value-of select="@default"/></xsl:otherwise></xsl:choose></xsl:variable>
    <!-- number of options, for rowspan attribute -->
    <xsl:variable name="numOptions" select="count(option)"/>
    <xsl:variable name="displayName"><xsl:value-of select="displayItem"/></xsl:variable>
    <xsl:variable name="paramName"><xsl:value-of select="@name"/></xsl:variable>    
    
    <xsl:for-each select="option">     
      <xsl:variable name="value" select="@name"/>
      <tr><xsl:attribute name="id"><xsl:value-of select="concat($prefix, '-', $paramIdx, '-', position())"/></xsl:attribute><xsl:if test="$display='n'"><xsl:attribute name="style">display:none</xsl:attribute></xsl:if>
	<xsl:if test="position()=1"><td rowspan='{$numOptions}' valign='baseline'><xsl:value-of select="../displayItem"/></td></xsl:if>
	<td><input type='radio' name='{$cachedName}' value="{$value}"><xsl:if test="$cachedValue=$value"><xsl:attribute name="checked">true</xsl:attribute></xsl:if></input></td>
	<td><xsl:value-of select="displayItem"/></td>
      </tr>
    </xsl:for-each>
  </xsl:template>
  
</xsl:stylesheet>
