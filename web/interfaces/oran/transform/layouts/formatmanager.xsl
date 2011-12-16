<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:java="http://xml.apache.org/xslt/java"
    xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
    xmlns:gslib="http://www.greenstone.org/skinning"
    xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
    extension-element-prefixes="java util"
    exclude-result-prefixes="java util">

    <xsl:include href="xml-to-gui.xsl"/>
    <xsl:include href="xml-to-gui-templates.xsl"/>
	
	<xsl:output method="html" omit-xml-declaration="yes"/> 

    <xsl:template name="formatmanagerpre">
    
        <!-- <xsl:variable name="foo"> -->
        <!-- <xsl:value-of select="/page/pageRequest/paramList[@name='formatedit']"/> -->

        <!--<xsl:if test="/page/pageRequest/paramList/param[(@name='formatedit') and (@value='1')]">-->

        <!-- Sam2's div code -->

        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"><xsl:text> </xsl:text></script>
        <!--<script type="text/javascript" src="interfaces/oran/js/jquery-1.4.2.js"><xsl:text> </xsl:text></script>-->
        <!-- XML parsing doesn't seem to work properly jquery so jquery.xml.js is a fix for this -->
        <script type="text/javascript" src="interfaces/oran/js/jquery.xml.js"><xsl:text> </xsl:text></script>
        <script type="text/javascript" src="interfaces/oran/js/jquery-ui-1.8.15/ui/jquery-ui-1.8.15.custom.js"><xsl:text> </xsl:text></script>
        <script type="text/javascript" src="interfaces/oran/js/jquery.selectboxes.js"><xsl:text> </xsl:text></script>
        <script type="text/javascript" src="interfaces/oran/js/innerxhtml.js"><xsl:text> </xsl:text></script>
        <!--<script type="text/javascript" src="interfaces/oran/js/gui_div.js"><xsl:text> </xsl:text></script>-->
        <script type="text/javascript" src="interfaces/oran/js/format_browse.js"><xsl:text> </xsl:text></script>
        <script type="text/javascript" src="interfaces/oran/js/format_document.js"><xsl:text> </xsl:text></script>
        <script type="text/javascript" src="interfaces/oran/js/format_util.js"><xsl:text> </xsl:text></script>
        <script type="text/javascript" src="interfaces/oran/js/format_jquery_prototypes.js"><xsl:text> </xsl:text></script>
       
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

            .header { background-color: #AFCCAF; border: solid 1px #117711; padding: 5px; padding-left: 10px; }

            .resizable { width: 150px; height: 150px; padding: 0.5em; } 
            
			.indent { margin-left: 15px; }
            .block { margin-left: 15px; border-left: dashed 1px black;}

            .droppable { background-color: #99dd99;}
            .droppable_hl { border: dashed 1px #ccc; background-color:#FFFFCC; }

            #XSLTcode {width: 99%; }

            .elementToolBox {position: fixed; top: 25%; right: 0px; background: white; border: 2px solid; padding: 10px 10px 10px 0px;}
			
			.elementToolBoxHeader { font-weight:bold; }
			
            .visible {display: block;}
            .hidden {display: none;}
            <!-- .gsf_metadata { border: solid 2px #0000BB; background-color: #440077; } -->

            <!-- .gsf_choose_metadata { border: solid 1px #000000; background-color: #223344; } -->
        </style>

        <link rel="stylesheet" type="text/css" href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.7.1/themes/base/jquery-ui.css"/> 

        <table width="100%" border="1"> 

            <td width="100%">
                <xsl:choose>
                    <xsl:when test="/page/pageRequest/@action = 'd'">
                        <!-- TOC on or off -->
                        <xsl:choose>
                            <xsl:when test="/page/pageResponse/format[@type='display']/gsf:option[@name='TOC']/@value='true'">
                                <input type="checkbox" name="TOC" checked="checked" onclick="displayTOC(this)">Display Table of Contents (set to true)</input>
                            </xsl:when>
                            <xsl:otherwise>
                                <input type="checkbox" name="TOC" onclick="displayTOC(this)">Display Table of Contents (set to false)</input>
                            </xsl:otherwise>
                        </xsl:choose> <br/>

                        <!-- book cover image on or off -->
                        <xsl:choose>
                            <xsl:when test="/page/pageResponse/format[@type='display']/gsf:option[@name='coverImage']/@value='true'">
                                <input type="checkbox" name="bookCover" checked="checked" onclick="displayBookCover(this)">Display Book Cover Image (set to true)</input>
                            </xsl:when>
                            <xsl:otherwise>
                                <input type="checkbox" name="bookCover" onclick="displayBookCover(this)">Display Book Cover Image (set to false)</input>
                            </xsl:otherwise>
                        </xsl:choose> <br/>

                        <textarea id="XSLTcode" rows="5">
                        The XSLT code for the relevant part of the page will be displayed here.
                        </textarea>
                        <!-- What are we doing?  It might be possible to tweak saveFormatStatement() if we are only dealing with format stuff but are we? -->
                        <br/>
                        <table>
                            <td>
                                <button id="saveDocumentChanges" type="button" onclick="saveDocumentChanges()">Save Changes</button>
                            </td>
                            <td>
                                <form>
                                    <input name="documentChanges" type="radio" id="applyToDocument" value="document" checked="true"/>Apply to this document only
                                    <input name="documentChanges" type="radio" id="applyToCollection" value="collection" />Apply to collection
                                </form>
                            </td>
                        </table>
                    </xsl:when>
                    <xsl:otherwise>
                        <table>
                            <td>
                                <button id="updateFormatStatement" type="button" onclick="updateFormatStatement()">Update Format Statement</button>
                            </td>
                            <td>
                                <button id="saveFormatStatement" type="button" onclick="saveFormatStatement()">Save Format Statement</button>
                            </td>
                            <td>
                                <form>
                                    <input name="classifiers" type="radio" id="applyToThis" value="this" checked="true"/>This Classifier
                                    <input name="classifiers" type="radio" id="applyToAll" value="all" />All Classifiers
                                </form> 
                            </td>
                        </table>

                        <div id="formatStatement">
                            <div id="formatRoot">
                                <xsl:call-template name="xml-to-gui">
                                    <xsl:with-param name="node-set" select="//format"/> <!-- [@type='browse']"/>  -->
                                    <xsl:with-param name="metadataSets" select="//metadataSetList"/> 
                                </xsl:call-template> 
                            </div>
                        </div>
                    </xsl:otherwise>
                </xsl:choose>
            </td>    
        </table>

        <div class="elementToolBox">
            <p class="indent elementToolBoxHeader">Elements to add</p>
            <!-- <div class="header element_type_gsf_template css_gsf_template" title="gsf:template">TEMPLATE</div> -->
                <div class="draggable_gsf_template css_gsf_template block" title="gsf:template">
                    <table class="header">
                        <tbody>
                            <tr>
                                <td class="header">MATCH=<input type="text" name="rawtextinput" size="10"/></td>
                                <td class="header"><a href="#" class="minmax ui-icon ui-icon-minusthick">[-]</a></td>
                                <td class="header"><a href="#" class="ui-icon ui-icon-closethick">[x]</a></td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div class="draggable_gsf_choose_metadata css_gsf_choose_metadata block" title="gsf:choose-metadata">
                    <table class="header">
                        <tbody>
                            <tr>
                                <td class="header">CHOOSE</td>
                                <td class="header"><a href="#" class="minmax ui-icon ui-icon-minusthick" title="Click me to expand">[-]</a></td>
                                <td class="header"><a href="#" class="ui-icon ui-icon-closethick" title="Click me to remove"/></td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div class="draggable_gsf_metadata css_gsf_metadata block" title="gsf:metadata">
                    <table class="header">
                        <tbody>
                            <tr>
								<td class="header" style="font-size: 0.8em;">METADATA</td>
                                <td class="header" id="metadataSelector">
                                    <!--<xsl:call-template name="meta-to-combo">
                                        <xsl:with-param name="metadataSets" select="//metadataSetList"/>
                                        <xsl:with-param name="current" select="ex.Title"/>
                                    </xsl:call-template>-->
                                </td>
                                <td class="header"><a href="#" class="ui-icon ui-icon-closethick" title="Click me to remove"/></td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div class="draggable_gsf_link css_gsf_link block" title="gsf:link">
                    <table class="header">
                        <tbody>
                            <tr>
								<td class="header">LINK</td>
                                <td class="header"><select>
                                    <option value = "document" selected = "document">Document</option>
                                    <option value = "classifier">Classifier</option>
                                    <option value = "source">Source</option>
                                    <option value = "horizontal">Horizontal</option>
                                </select></td>
                                <td class="header"><a href="#" class="minmax ui-icon ui-icon-minusthick">[-]</a></td>
                                <td class="header"><a href="#" class="ui-icon ui-icon-closethick">[x]</a></td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div class="draggable_gsf_switch css_gsf_switch block" title="gsf:switch">
                    <table class="header">
                        <tbody>
                            <tr>
                                <td class="header">SWITCH</td>
                                <td class="header"><a href="#" class="minmax ui-icon ui-icon-minusthick">[-]</a></td>
                                <td class="header"><a href="#" class="ui-icon ui-icon-closethick">[x]</a></td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div class="draggable_gsf_when css_gsf_when block" title="gsf:when">
                    <table class="header">
                        <tbody>
                            <tr>
                                <td class="header">WHEN <xsl:value-of select="@test"/></td>
                                <td class="header"><a href="#" class="minmax ui-icon ui-icon-minusthick">[-]</a></td>
                                <td class="header"><a href="#" class="ui-icon ui-icon-closethick"/></td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div class="draggable_gsf_otherwise css_gsf_otherwise block" title="gsf:otherwise">
                    <table class="header">
                        <tbody>
                            <tr>
                                <td class="header">OTHERWISE</td>
                                <td class="header"><a href="#" class="minmax ui-icon ui-icon-minusthick">[-]</a></td>
                                <td class="header"><a href="#" class="ui-icon ui-icon-closethick"/></td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div class="draggable_gsf_icon css_gsf_icon block" title="gsf:icon">
                    <table class="header">
                        <tbody>
                            <tr>
                                <td class="header">ICON</td>
								<td class="header"><select>
                                    <option value="document" selected="document">Document</option>
                                    <option value="classifier">Classifier</option>
                                    <option value="source">Source</option>
                                </select></td>
                                <td class="header"><a href="#" class="ui-icon ui-icon-closethick">[x]</a></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="draggable_gsf_text css_text block" title="text">
                    <table class="header">
                        <tbody>
                            <tr>
								<td class="header">TEXT</td>
                                <td class="header"><input type="text" name="rawtextinput" size="10" value=""/></td>
                                <td class="header"><a href="#" class="ui-icon ui-icon-closethick"/></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="draggable_table css_table block" title="gsf:table">
					<table class="header">
                        <tbody>
                            <tr>
                                <td class="header">NEW TABLE</td>
							</tr>
						</tbody>
					</table>
				</div>
				<div class="draggable_tr css_tr block" title="gsf:table">
					<table class="header">
                        <tbody>
                            <tr>
                                <td class="header">NEW TABLE ROW</td>
							</tr>
						</tbody>
					</table>
				</div>
				<div class="draggable_td css_td block" title="gsf:table">
					<table class="header">
                        <tbody>
                            <tr>
                                <td class="header">NEW TABLE COLUMN</td>
							</tr>
						</tbody>
					</table>
				</div>
         </div>

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
      <!--</xsl:if>-->

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
