<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util">

	<xsl:template name="formatmanagerpre">
	
    <!-- <xsl:variable name="foo"> -->
    <!-- <xsl:value-of select="/page/pageRequest/paramList[@name='formatedit']"/> -->

    <xsl:if test="/page/pageRequest/paramList/param[(@name='formatedit') and (@value='1')]">

	<!-- Sam2's div code -->

		<script type="text/javascript" src="interfaces/oran/js/innerxhtml.js"><xsl:text> </xsl:text></script>
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

		<button id="updateFormatStatement" type="button" onclick="updateFormatStatement()">Update Format Statement</button>
		<button id="saveFormatStatement" type="button" onclick="saveFormatStatement()">Save Format Statement</button>

		<div id="formatStatement">
			<div id="formatRoot">
		
		<xsl:call-template name="xml-to-gui">
			<xsl:with-param name="node-set" select="//format"/> <!-- [@type='browse']"/>  -->
			<xsl:with-param name="metadataSets" select="//metadataSetList"/> 
		</xsl:call-template> 
			</div>
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

<!--<H2>Preview</H2>

                    <div id="my_categories"><xsl:text> </xsl:text></div>

                    <iframe name="preview" id="iframe" width="98%" height="300">Your browser does not support iframes</iframe> 

                    <xsl:variable name="preview"> -->
                    <!-- <button type="button" onclick="loadXMLDoc()">Change Content</button> -->
	</xsl:template>
	
	<xsl:template name="formatmanagerpost">
		   <!--                 </xsl:variable>

                    <script type="text/javascript">
                        preview_html = <xsl:text disable-output-escaping="yes">'</xsl:text><xsl:copy-of select="$preview"/><xsl:text disable-output-escaping="yes">';</xsl:text>
                    </script>

                    <div id="result">
                    Here
                    </div>-->
	</xsl:template>
</xsl:stylesheet>