<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
    xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
    
    xmlns:lxslt="http://xml.apache.org/xslt"
    xmlns:result="http://www.example.com/results"
    xmlns:exsl="http://exslt.org/common"

	extension-element-prefixes="java util result exsl"
	exclude-result-prefixes="util java util">

	<xsl:include href="xml-to-gui.xsl"/>
	<xsl:include href="xml-to-gui-templates.xsl"/>

	<!-- put the URL or path of your site here site -->
	<!-- eg 'http://www.example.com/mysite' or '/mysite'  -->
	<xsl:template name="siteURL"><xsl:value-of select="/page/pageResponse/metadataList/metadata[@name='siteURL']"/></xsl:template>


	<!-- the output format for this layout is html -->
	<xsl:output method="html" omit-xml-declaration="yes"/>  

	<!-- the main layout is defined here -->
	<xsl:template match="/">

		<html>

			<head>
				<title><xsl:call-template name="pageTitle"/> :: <xsl:call-template name="siteName"/></title>
				<link rel="stylesheet" href="interfaces/{$interface_name}/style/core.css" type="text/css"/>
				<!-- <script type="text/javascript" src="interfaces/oran/js/jquery.js"><xsl:text> </xsl:text></script> --> 
                <script type="text/javascript" src="interfaces/oran/js/jquery-1.4.2.js"><xsl:text> </xsl:text></script>
                <!-- <script type="text/javascript" src="interfaces/oran/js/jquery-ui-1.8.2.custom.min.js"><xsl:text> </xsl:text></script> -->
                <script type="text/javascript" src="interfaces/oran/js/jquery-ui-1.8rc1/ui/jquery-ui.js"><xsl:text> </xsl:text></script>
                <script type="text/javascript" src="interfaces/oran/js/jquery.selectboxes.js"><xsl:text> </xsl:text></script>
                <!-- <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.5/jquery-ui.js"><xsl:text> </xsl:text></script> -->
                <!-- <script type="text/javascript" src="http://code.jquery.com/jquery-1.4.2.min.js"><xsl:text> </xsl:text></script> -->
                <!-- <script type="text/javascript" src="interfaces/oran/js/jquery-ui-1.8rc1/jquery-1.4.1.js"><xsl:text> </xsl:text></script> -->

				<xsl:call-template name="berryBasketHeadTags"/>
			</head>
			
			<body><xsl:call-template name="textDirectionAttribute"/><xsl:call-template name="actionClass"/>

				<xsl:call-template name="displayErrorsIfAny"/>

				<div id="container"><div id="container2"><div id="container3"><div id="container4">

					<div id="gs_banner">

						<!-- show the title -->
						<div id="breadcrumbs"><xsl:call-template name="breadcrumbs"/><xsl:text> </xsl:text></div>
						<h2><xsl:call-template name="pageTitle"/></h2>

						<xsl:if test="/page/pageResponse/collection">
							<!-- show home, help, preferences links -->
							<ul id="bannerLinks">

								<!-- preferences -->
								<li>
									<a href="{$library_name}?a=p&amp;amp;sa=pref&amp;amp;c={$collName}">
										<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_tip')"/></xsl:attribute>
										<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'pref_b')"/>
									</a>
								</li>

								<!-- help -->
								<li>
									<a href="{$library_name}?a=p&amp;amp;sa=help&amp;amp;c={$collName}">
										<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_tip')"/></xsl:attribute>
										<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'help_b')"/>
									</a>
								</li>

								<li>
									<a href="{$library_name}?a=p&amp;amp;sa=home">
										<xsl:attribute name="title"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home_tip')"/></xsl:attribute>
										<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'home_b')"/>
									</a>
								</li>

							</ul>
						</xsl:if>

						<!-- show the available 'services' (browse, search, etc.) -->
						<xsl:if test="/page/pageResponse/collection/serviceList/service">
							<ul id="nav">
								<!-- show browse service, if it exists -->
								<xsl:if test="/page/pageResponse/collection/serviceList/service[@type='browse']">
									<xsl:for-each select="/page/pageResponse/collection/serviceList/service[@type='browse']">
										<xsl:call-template name="navigationTab"/>
									</xsl:for-each>
								</xsl:if>

								<!-- show search services (collapsed) if they exist -->
								<xsl:if test="/page/pageResponse/collection/serviceList/service[@type='query']">
									<xsl:for-each select="/page/pageResponse/collection/serviceList">

										<!--
											using collapsedNavigationTab instead of navigationTab collapses
											all the services of the given type onto one tab
										-->
										<xsl:call-template name="collapsedNavigationTab">
											<xsl:with-param name="type">query</xsl:with-param>
										</xsl:call-template>
									</xsl:for-each>
								</xsl:if>

								<!-- all other services -->
								<xsl:for-each select="/page/pageResponse/collection/serviceList/service[not(@type='query') and not(@type='browse')]">
									<xsl:call-template name="navigationTab"/>
								</xsl:for-each>

							</ul>
						</xsl:if>

					</div>

<!-- *************************************************************************************** -->

    <!-- <xsl:variable name="foo"> -->
    <!-- <xsl:value-of select="/page/pageRequest/paramList[@name='formatedit']"/> -->

    <xsl:if test="/page/pageRequest/paramList/param[(@name='formatedit') and (@value='1')]">

<!-- Sam2's div code -->

    <script type="text/javascript" src="interfaces/oran/js/gui_div.js"><xsl:text> </xsl:text></script>
   
    <xsl:call-template name="xml-to-gui-templates">
        <xsl:with-param name="node-set" select="test"/>
        <xsl:with-param name="metadataSets" select="//metadataSetList"/>
    </xsl:call-template>

    <xsl:variable name="fmt1">
        <xsl:call-template name="xml-to-string">
            <xsl:with-param name="node-set" select="//format[@type='browse']"/>
          </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="meta">
        <xsl:call-template name="xml-to-string">
            <xsl:with-param name="node-set" select="//metadataSetList"/>
          </xsl:call-template>
    </xsl:variable>

    <style type="text/css">
        .placeholder{margin-left: 10px; border: dashed 1px #ccc; background-color:#FFFFCC; height:20px; }

        .placeholder_td{margin-left: 10px; border: dashed 1px #ccc; background-color:#FFFFCC; width:20px; }

        .header { background-color: #AFCCAF; border: solid 1px #117711; padding: 5px; padding-left: 10px;}

        .resizable { width: 150px; height: 150px; padding: 0.5em; } 
        
        .block { margin-left: 15px; border-left: dashed 1px black;}

        .droppable { background-color: #99dd99;}
        .droppable_hl { border: dashed 1px #ccc; background-color:#FFFFCC; }

        <!-- .gsf_metadata { border: solid 2px #0000BB; background-color: #440077; } -->

        <!-- .gsf_choose_metadata { border: solid 1px #000000; background-color: #223344; } -->
    </style>

    <link rel="stylesheet" type="text/css" href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.7.1/themes/base/jquery-ui.css"/> 

    <table width="100%" border="1"> 

    <td width="75%">

    <div id="formatStatement">

    <xsl:call-template name="xml-to-gui">
        <xsl:with-param name="node-set" select="//format"/> <!-- [@type='browse']"/>  -->
        <xsl:with-param name="metadataSets" select="//metadataSetList"/> 
    </xsl:call-template> 

    </div>
    </td>    


    <td width="25%" valign="top">
        <h2> Elements to add </h2>
        <!-- <div class="header element_type_gsf_template css_gsf_template" title="gsf:template">TEMPLATE</div> -->
        <div class="draggable_gsf_template css_gsf_template block" title="gsf:template"><table class="header"><tbody><tr><td class="header">MATCH=<input type="text" name="rawtextinput" size="10"/></td><td><a href="#" class="minmax ui-icon ui-icon-minusthick">[-]</a></td><td><a href="#" class="remove ui-icon ui-icon-closethick">[x]</a></td></tr></tbody></table><table border="1"><tr class="tr"><td class="droppable" width="10px"></td></tr></table></div><br/>

        <div class="draggable_gsf_choose_metadata css_gsf_choose_metadata block" title="gsf:choose-metadata"><table class="header"><tbody><tr><td class="header">CHOOSE</td><td class="header"><a href="#" class="minmax ui-icon ui-icon-minusthick" title="Click me to expand">[-]</a></td><td class="header"><a href="#" class="remove ui-icon ui-icon-closethick" title="Click me to remove"/></td></tr></tbody></table></div>

        <div class="draggable_gsf_metadata css_gsf_metadata block" title="gsf:metadata"><table class="header"><tbody><tr><td class="header"><xsl:call-template name="meta-to-combo">
                        <xsl:with-param name="metadataSets" select="//metadataSetList"/>
                        <xsl:with-param name="current" select="ex.Title"/>
                     </xsl:call-template></td><td class="header"><a href="#" class="remove ui-icon ui-icon-closethick" title="Click me to remove"/></td></tr></tbody></table></div>

        <div class="draggable_gsf_link css_gsf_link block" title="gsf:link"><table class="header"><tbody><tr><td class="header">LINK<select>
                            <option value = "document" selected = "document">Document</option>
                            <option value = "classifier">Classifier</option>
                            <option value = "source">Source</option>
                            <option value = "horizontal">Horizontal</option>
                </select></td><td><a href="#" class="minmax ui-icon ui-icon-minusthick">[-]</a></td><td><a href="#" class="remove ui-icon ui-icon-closethick">[x]</a></td></tr></tbody></table></div>

        <div class="draggable_gsf_switch css_gsf_switch block" title="gsf:switch"><table class="header"><tbody><tr><td class="header">SWITCH</td><td><a href="#" class="minmax ui-icon ui-icon-minusthick">[-]</a></td><td><a href="#" class="remove ui-icon ui-icon-closethick">[x]</a></td></tr></tbody></table></div>

        <div class="draggable_gsf_when css_gsf_when block" title="gsf:when"><table class="header"><tbody><tr><td class="header">WHEN<xsl:value-of select="@test"/></td><td class="header"><a href="#" class="minmax ui-icon ui-icon-minusthick">[-]</a></td><td class="header"><a href="[myhref]" class="ui-icon ui-icon-closethick"/></td></tr></tbody></table></div>

        <div class="draggable_gsf_otherwise css_gsf_otherwise block" title="gsf:otherwise"><table class="header"><tbody><tr><td class="header">OTHERWISE</td><td class="header"><a href="#" class="minmax ui-icon ui-icon-minusthick">[-]</a></td><td><a href="#" class="ui-icon ui-icon-closethick"/></td></tr></tbody></table></div>

        <div class="draggable_gsf_icon css_gsf_icon block" title="gsf:icon"><table class="header"><tbody><tr><td class="header">ICON<select><option value = "document" selected = "document">Document</option><option value = "classifier">Classifier</option><option value = "source">Source</option></select></td><td><a href="#" class="remove ui-icon ui-icon-closethick">[x]</a></td></tr></tbody></table></div>
        <br/>
        <div class="draggable_gsf_text css_text" title="text"><table class="header"><tbody><tr><td class="header"><input type="text" name="rawtextinput" size="10" value=""/></td><td class="header"><a href="[myhref]" class="ui-icon ui-icon-closethick"/></td></tr></tbody></table></div>
        <div class="draggable_table css_table" title="gsf:table">NEW TABLE</div>
        <div class="draggable_tr css_tr" title="gsf:row">NEW TABLE ROW</div>
        <div class="draggable_td css_td" title="gsf:column">NEW TABLE COLUMN</div>

    </td>
    </table>

    <!--
    <div id="format">
        <p>
          <b>Format string here</b>
          <i>
              <xsl:value-of select="$fmt1"/>
          </i>
        </p>
    </div> -->
    <!--    <p>
          <i>
              <xsl:value-of select="$meta"/>
          </i>
        </p>
      </div> -->
      </xsl:if>

<!-- *************************************************************************************** -->
<H2>Preview</H2>
                    <iframe name="preview" id="iframe" width="98%" height="300">Your browser does not support iframes</iframe> 

                    <xsl:variable name="preview"> 
                    <!-- <button type="button" onclick="loadXMLDoc()">Change Content</button> -->
					<div id="gs_content">
						<!--
							show the content of the page.
							to customise this part, edit the xsl file for the page you want to edit
						-->
						<xsl:apply-templates select="/page"/>

					</div>
                    </xsl:variable>

                    <script type="text/javascript">
                        preview_html = <xsl:text disable-output-escaping="yes">'</xsl:text><xsl:copy-of select="$preview"/><xsl:text disable-output-escaping="yes">';</xsl:text>
                    </script>

					<div id="gs_footer">
						<xsl:call-template name="poweredByGS3TextBar"/>
					</div>

					<div class="corner" id="cornerTopLeft"><xsl:text> </xsl:text></div>
					<div class="corner" id="cornerTopRight"><xsl:text> </xsl:text></div>
					<div class="corner" id="cornerBottomLeft"><xsl:text> </xsl:text></div>
					<div class="corner" id="cornerBottomRight"><xsl:text> </xsl:text></div>

				</div></div></div></div>

			</body>
		</html>
	</xsl:template>

</xsl:stylesheet>


