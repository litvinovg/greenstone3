<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util">
  
	<!-- use the 'main' layout -->
	<xsl:include href="layouts/main.xsl"/>

	<!-- set page title -->
	<xsl:template name="pageTitle"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_b')"/></xsl:template>

	<!-- set page breadcrumbs -->
	<xsl:template name="breadcrumbs">
		<gslib:siteLink/><gslib:rightArrow/>
		<xsl:if test="/page/pageResponse/collection">
			<gslib:collectionNameLinked/><gslib:rightArrow/>
		</xsl:if>
	</xsl:template>

	<!-- the page content -->
  <xsl:template match="/page">
    <xsl:call-template name="prefs-javascript"/>
    <xsl:variable name="collName" select="/page/pageRequest/paramList/param[@name='c']/@value"/>
    
    <div id="queryform">
      <form name="PrefForm" method="get" action="{$library_name}" id="prefform">
	
	<input type='hidden' name='a' value='p'/>
	<input type='hidden' name='sa' value='pref'/>
	<input type='hidden' name='c' value="{$collName}"/>

	<p id="SaveInstructions" style="color:red; text-align:center; display:none;"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.set_prefs_help')"/></p>
	<xsl:call-template name="presentation-prefs"/>
	<br/>
	
	<input type='submit' onclick="bypass=true;"><xsl:attribute name="value"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.set_prefs')"/></xsl:attribute></input>
	
      </form>
    </div>
    
  </xsl:template>
	<xsl:template name="presentation-prefs">
    <h3 class="formheading"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.prespref')"/></h3>
    <div id="presprefs">
      <xsl:call-template name="lang-param"/>
     <!-- <xsl:call-template name="encoding-param"/>-->
      <xsl:call-template name="format-edit-param"/>
    <xsl:call-template name="berry-basket-param"/>
    <!--<xsl:call-template name="document-maker-param"/> not working at moment-->
    <xsl:call-template name="book-param"/>
    <xsl:call-template name="theme-change-param"/>
      </div>
  </xsl:template>

    <xsl:template name="prefs-javascript">
		<!-- 
		Add some javascript to the page that notices when a preference 
		   is changed so that, if the user tries to navigate away from 
		   the page without clicking the "Set preferences" button, it 
		   gives a stay/leave dialog. -->
		<script type="text/javascript"><xsl:text disable-output-escaping="yes">
			var modified = false;
			var bypass = false;
					
			function unsavedChanges(e) {
			if (modified &amp;&amp; !bypass) {
			document.getElementById("SaveInstructions").style.display="block";
			return "Provide the prompt";
			}
			// no return statement - no dialog will be shown
			}
			
			function changed()
			{
				modified = true;
				bypass = false;
			}
			
			function pageLoad()
			{
				var formElem = document.getElementById("prefform");
			
				var selectNodes = formElem.getElementsByTagName("select");
				var inputNodes = formElem.getElementsByTagName("input");

				YAHOO.util.Event.addListener(selectNodes, 'change', changed);
				
				for(var i = 0; i &lt; inputNodes.length; i++)
				{
					current = inputNodes[i];
					if(current.getAttribute("type") == null)
					{
						YAHOO.util.Event.on(current, 'keyup', changed);
					}
				}
			}
			
			YAHOO.util.Event.addListener(window, 'load', pageLoad);
			$(window).bind("beforeunload", unsavedChanges);
		</xsl:text></script>
    </xsl:template>


				<xsl:template name="lang-param">
					<div class="paramLabel">
						<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.interfacelang')"/>
					</div>
					<div class="paramValue">
						<select name="l">
							<xsl:for-each select="/page/pageResponse/languageList/language">
								<option value="{@name}"><xsl:if test="@name=/page/@lang"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if><xsl:value-of select="displayItem[@name='name']"/></option>
							</xsl:for-each>
						</select>

					</div>
      <br class="clear"/>

</xsl:template>
					

				<xsl:template name="encoding-param">
					<div class="paramValue">
						<span class="rightspace"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.encoding')"/></span>
					</div>
      <br class="clear"/>

	</xsl:template>
					

                    <xsl:template name="format-edit-param">
					<xsl:if test="/page/pageResponse/collection/serviceList/service[@name='CoverageMetadataRetrieve']">
						<div id="formateditprefs">
							<xsl:variable name="formatedit"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='formatedit']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='formatedit']/@value"/></xsl:when><xsl:otherwise>off</xsl:otherwise></xsl:choose></xsl:variable>
							<div class="paramLabel">
								<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.formatedit')"/>
							</div>
							<div class="paramValue">
								<select name="formatedit">
									<option value="on"><xsl:if test="$formatedit='on'"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.formatedit.on')"/></option>
									<option value="off"><xsl:if test="$formatedit='off'"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.formatedit.off')"/></option>
								</select>
							</div>
							<br class="clear"/>
						</div>
					</xsl:if>
</xsl:template>

<xsl:template name="berry-basket-param">
				<div id="berrybasketprefs">
					<xsl:variable name="berrybasket"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='berrybasket']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='berrybasket']/@value"/></xsl:when><xsl:otherwise>off</xsl:otherwise></xsl:choose></xsl:variable>
					<div class="paramLabel">
						<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.berrybasket')"/>
					</div>
					<div class="paramValue">
						<select name="berrybasket">
							<option value="on"><xsl:if test="$berrybasket='on'"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.berrybasket.on')"/></option> 
							<option value="off"><xsl:if test="$berrybasket='off'"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.berrybasket.off')"/></option>
						</select>
					</div>
					<br class="clear"/>
				</div>
</xsl:template>
				<xsl:template name="document-maker-param">
				<xsl:if test="/page/pageRequest/userInformation and (util:contains(/page/pageRequest/userInformation/@groups, 'administrator') or util:contains(/page/pageRequest/userInformation/@groups, 'all-collections-editor') or util:contains(/page/pageRequest/userInformation/@groups, $thisCollectionEditor))">
					<div id="documentmakerprefs">
						<xsl:variable name="documentbasket"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='documentbasket']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='documentbasket']/@value"/></xsl:when><xsl:otherwise>off</xsl:otherwise></xsl:choose></xsl:variable>
						<div class="paramLabel">Document basket</div>
						<div class="paramValue">
							<select name="documentbasket">
								<option value="on"><xsl:if test="$documentbasket='on'"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.berrybasket.on')"/></option> 
								<option value="off"><xsl:if test="$documentbasket='off'"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.berrybasket.off')"/></option>
							</select>
						</div>
						<br class="clear"/>
					</div>
				</xsl:if>
      </xsl:template>
      <xsl:template name="book-param">
				<!-- tidy (?) -->
		<xsl:variable name="tidyoption"><xsl:value-of select="/page/pageResponse/collection/metadataList/metadata[@name='tidyoption']"/></xsl:variable>
				<xsl:if test="$tidyoption='tidy'">
					<xsl:variable name="book">
					  <xsl:choose>
					    <xsl:when test="/page/pageRequest/paramList/param[@name='book']">
					      <xsl:value-of select="/page/pageRequest/paramList/param[@name='book']/@value"/>
					    </xsl:when>
					    <xsl:when test="/page/pageRequest/paramList/param[@name='s1.book']">
					      <xsl:value-of select="/page/pageRequest/paramList/param[@name='s1.book']/@value"/>
					    </xsl:when>
					    <xsl:otherwise>off</xsl:otherwise>
					  </xsl:choose>
					</xsl:variable>
					<div class="paramLabel">
						<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.book')"/>
					</div>
					<div class="paramValue">
						<select name="s1.book">
							<option value="on"><xsl:if test="$book='on'"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.berrybasket.on')"/></option> 
							<option value="off"><xsl:if test="$book='off'"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.berrybasket.off')"/></option>
						</select>
					</div>
					<br class="clear"/>
				</xsl:if>
      </xsl:template>
      <xsl:template name="theme-change-param">
				<!-- Theme Changer -->
				<xsl:if test="/page/pageRequest/userInformation and util:contains(/page/pageRequest/userInformation/@groups, 'administrator')">
					<div>
						<div class="paramLabel"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.theme')"/></div>
						<script type="text/javascript">
							<xsl:text disable-output-escaping="yes">
								$(document).ready(function(){
									$("#switcher").themeswitcher({
										imgpath: "interfaces/" + gs.xsltParams.interface_name + "/style/images/",
										additionalThemes: [
											{title:"Greenstone Default", name:"custom-theme0", icon:"theme_90_greenstone.png", url:"interfaces/" + gs.xsltParams.interface_name + "/style/themes/main/jquery-ui-1.8.16.custom.css"},
											{title:"Greenstone Custom 1", name:"custom-theme1", icon:"theme_90_start_menu.png", url:"interfaces/" + gs.xsltParams.interface_name + "/style/themes/alt_theme_1/jquery-ui-1.8.16.custom.css"},
											{title:"Greenstone Custom 2", name:"custom-theme2", icon:"theme_90_mint_choco.png", url:"interfaces/" + gs.xsltParams.interface_name + "/style/themes/alt_theme_2/jquery-ui-1.8.16.custom.css"},
											{title:"Greenstone Custom 3", name:"custom-theme3", icon:"theme_90_trontastic.png", url:"interfaces/" + gs.xsltParams.interface_name + "/style/themes/alt_theme_3/jquery-ui-1.8.16.custom.css"}
										]
									});
								});
							</xsl:text>
						</script>
						<div class="paramValue" id="switcher"><xsl:text> </xsl:text></div>
					</div>
					<br class="clear"/>
				</xsl:if>
</xsl:template>

</xsl:stylesheet>
