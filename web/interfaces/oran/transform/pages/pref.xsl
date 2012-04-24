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

		<!-- 
		Add some javascript to the page that notices when a preference is changed 
		so that, if the user tries to navigate away from the page without clicking
		the "Set preferences" button, it asks them if they want their preferences
		saved or not 
		-->
		<script type="text/javascript"><xsl:text disable-output-escaping="yes">
			var modified = false;
			var bypass = false;
					
			function assembleURLFromForm(formElem)
			{
				var url = "dev";
				var selectNodes = formElem.getElementsByTagName("select");
				var inputNodes = formElem.getElementsByTagName("input");

				for (var i = 0; i &lt; selectNodes.length; i++)
				{
					var current = selectNodes[i];
					url += (url == "dev") ? "?" : "&amp;";
					url += current.name + "=";
					url += current.options[current.selectedIndex].text;
				}
				
				for (var i = 0; i &lt; inputNodes.length; i++)
				{
					var current = inputNodes[i];
					if (current.type == "hidden" || current.type == "text")
					{
						url += (url == "dev") ? "?" : "&amp;";
						url += current.name + "=";
						url += current.value;
					}
				}
				return url;
			}
					
			function checkModified(e)
			{
				if (modified &amp;&amp; !bypass)
				{
					var ok = confirm("Would you like to save your preferences?");
					
					if (ok)
					{
						var formElem = document.getElementById("prefform");
						formElem.submit();
						var xmlhttp;
						if (window.XMLHttpRequest)
						{
							xmlhttp=new XMLHttpRequest();
						}
						else
						{
							xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
						}

						xmlhttp.open("GET",assembleURLFromForm(formElem),false);
						xmlhttp.send();
					}
				}
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
			YAHOO.util.Event.addListener(window, 'beforeunload', checkModified);
		</xsl:text></script>

		<xsl:variable name="collName" select="/page/pageRequest/paramList/param[@name='c']/@value"/>
		<xsl:variable name="tidyoption"><xsl:value-of select="/page/pageResponse/collection/metadataList/metadata[@name='tidyoption']"/></xsl:variable>

		<div id="queryform">
			<form name="PrefForm" method="get" action="{$library_name}" id="prefform">

				<input type='hidden' name='a' value='p'/>
				<input type='hidden' name='sa' value='pref'/>
				<input type='hidden' name='c' value="{$collName}"/>

				<!-- presentation preferences -->
				<h3 class="formheading"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.prespref')"/></h3>
				<div id="presprefs">

					<!-- language -->
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

					<!-- encoding -->
					<div class="paramValue">
						<span class="rightspace"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.encoding')"/></span>
					</div>
					<br class="clear"/>

                    <!-- format editing -->
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
				</div>

				<!-- berry baskets -->
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
				
				<!-- document maker -->
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

				<!-- tidy (?) -->
				<xsl:if test="$tidyoption='tidy'">
					<xsl:variable name="book"><xsl:choose><xsl:when test="/page/pageRequest/paramList/param[@name='book']"><xsl:value-of select="/page/pageRequest/paramList/param[@name='book']/@value"/></xsl:when><xsl:otherwise>off</xsl:otherwise></xsl:choose></xsl:variable>
					<div class="paramLabel">
						<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.book')"/>
					</div>
					<div class="paramValue">
						<select name="book">
							<option value="on"><xsl:if test="$book='on'"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.berrybasket.on')"/></option> 
							<option value="off"><xsl:if test="$book='off'"><xsl:attribute name="selected"></xsl:attribute></xsl:if><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.berrybasket.off')"/></option>
						</select>
					</div>
					<br class="clear"/>
				</xsl:if>
				
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

				<!-- search preferences -->
				<h3><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.searchpref')"/></h3>
				<div id="searchprefs">
					<xsl:variable name="hits">
						<xsl:choose>
							<xsl:when test="/page/pageRequest/paramList/param[@name='hitsPerPage']">
								<xsl:value-of select="/page/pageRequest/paramList/param[@name='hitsPerPage']/@value"/>
							</xsl:when>
							<xsl:when test="/page/pageRequest/paramList/param[@name='s1.hitsPerPage']">
								<xsl:value-of select="/page/pageRequest/paramList/param[@name='s1.hitsPerPage']/@value"/>
							</xsl:when>
							<xsl:otherwise>
								20
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable> 
					<div class="paramLabel">
						<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.hitsperpage')"/>
					</div>
					<div class="paramValue">
						<select name="s1.hitsPerPage">
							<option value="20">
								<xsl:if test="$hits=20">
									<xsl:attribute name="selected" />
								</xsl:if>
								20
							</option> 
							<option value="50">
								<xsl:if test="$hits=50">
									<xsl:attribute name="selected" />
								</xsl:if>
								50
							</option>
							<option value="100">
								<xsl:if test="$hits=100">
									<xsl:attribute name="selected" />
								</xsl:if>
								100
							</option>
							<option value="-1">
								<xsl:if test="$hits=-1">
									<xsl:attribute name="selected" />
								</xsl:if>
								<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.all')"/>
							</option>
						</select>
					</div>
					
					<br class="clear"/>
					
					<xsl:variable name="mdocs">
						<xsl:choose>
							<xsl:when test="/page/pageRequest/paramList/param[@name='maxDocs']">
								<xsl:value-of select="/page/pageRequest/paramList/param[@name='maxDocs']/@value"/>
							</xsl:when>
							<xsl:when test="/page/pageRequest/paramList/param[@name='s1.maxDocs']">
								<xsl:value-of select="/page/pageRequest/paramList/param[@name='s1.maxDocs']/@value"/>
							</xsl:when>
							<xsl:otherwise>100</xsl:otherwise>
						</xsl:choose>
					</xsl:variable> 
					<div class="paramLabel">
						<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.maxDocs')"/>
					</div>
					<div class="paramValue">
						<input name="s1.maxDocs" size="3" value="{$mdocs}" />
					</div>
					
					<br class="clear"/>
				</div>

				<br/>

				<input type='submit' onclick="bypass=true;"><xsl:attribute name="value"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref.set_prefs')"/></xsl:attribute></input>

			</form>
		</div>

	</xsl:template>

</xsl:stylesheet>
