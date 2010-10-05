<!--
Copyright (c) 2001-2009, Evan Lenz
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of Lenz Consulting Group nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

	    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

Recent changes:

2010-06-10: Added the $force-exclude-all-namespaces parameter
2009-10-19: Added the $exclude-these-namespaces parameter 
2009-10-08: Added $att-value parameter and template name to template rule for attributes.

-->
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
    xmlns:exsl="http://exslt.org/common"
    extension-element-prefixes="exsl" >    

    <xsl:output omit-xml-declaration="yes"/>

    <xsl:template name="xml-to-gui">
        <xsl:param name="node-set" select="."/>
        <xsl:param name="metadataSets" select="."/> 

        <xsl:value-of select="metadataSets[@name]"/>
        <xsl:apply-templates select="$node-set" mode="xml-to-gui">
            <xsl:with-param name="depth" select="1"/>
            <xsl:with-param name="metadataSets" select="$metadataSets"/> 
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="/" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>
        
        <xsl:apply-templates mode="xml-to-gui">
            <xsl:with-param name="depth" select="$depth"/>
            <xsl:with-param name="metadataSets" select="$metadataSets"/>
        </xsl:apply-templates>
    </xsl:template>
    <!-- **************************************************************************** -->

    <!-- **************************************************************************** -->
    <!-- COMBO BOX CREATION                                                           -->
    <!-- **************************************************************************** -->

    <xsl:template name="meta-to-combo">
        <xsl:param name="metadataSets" select='.'/>
        <xsl:param name="current" select='.'/>

        <xsl:variable name="current_mod">
            <xsl:choose>
                <xsl:when test="contains($current,'.')"><xsl:value-of select="$current"/></xsl:when>
                <xsl:otherwise>ex.<xsl:value-of select="$current"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <select>
            <!-- <option value ="{$current_mod}" disabled="disabled" selected="{$current_mod}"><xsl:value-of select="$current_mod"/></option> -->

            <xsl:apply-templates select="$metadataSets" mode="meta-to-combo">
                <xsl:with-param name="current" select="$current_mod"/>
            </xsl:apply-templates>
        </select>
    </xsl:template>

    <xsl:template match="metadataSet" mode="meta-to-combo">
        <xsl:param name="current"/>

        <xsl:apply-templates mode="meta-to-combo">
            <xsl:with-param name="set" select="@name"/>
            <xsl:with-param name="current" select="$current"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="metadata" mode="meta-to-combo">
        <xsl:param name="set"/>
        <xsl:param name="current"/>

        <xsl:variable name="meta"><xsl:value-of select="$set"/>.<xsl:value-of select="@name"/></xsl:variable>

        <!-- if this is the current value, then set combo box to this value -->
        <xsl:choose>
            <xsl:when test="$current = $meta">
                <option value ="{$meta}" selected="{$meta}"><xsl:value-of select="$meta"/></option>
            </xsl:when>
            <xsl:otherwise>
                <option value ="{$meta}"><xsl:value-of select="$meta"/></option>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*" mode="meta-to-combo">
        <xsl:param name="current"/>
        <xsl:apply-templates mode="meta-to-combo">
            <xsl:with-param name="current" select="$current"/>
        </xsl:apply-templates>
    </xsl:template>


    <!-- **************************************************************************** -->

    <!-- **************************************************************************** -->
    <!-- GSF STATEMENTS                                                               -->
    <!-- **************************************************************************** -->

    
    <!-- ********** GSF:CHOOSE-METADATA ********** -->
    <xsl:template match="gsf:choose-metadata" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>

        <div class="gsf_choose_metadata css_gsf_choose_metadata block" title="gsf:choose-metadata">
        <table class="header"><tbody><tr><td class="header">CHOOSE</td><td class="header"><a href="#" class="minmax ui-icon ui-icon-minusthick" title="Click me to expand">[-]</a></td><td class="header"><a href="#" class="remove ui-icon ui-icon-closethick" title="Click me to remove"/></td></tr></tbody></table>
                <xsl:apply-templates mode="xml-to-gui">
                    <xsl:with-param name="depth" select="$depth"/>
                    <xsl:with-param name="metadataSets" select="$metadataSets"/> 
                </xsl:apply-templates>
        </div>   
    </xsl:template>


    <!-- ********** GSF:METADATA ********** -->
    <xsl:template match="gsf:metadata" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>

        <div class="gsf_metadata css_gsf_metadata block leaf" title="gsf:metadata">
            <xsl:variable name="combo">
                <xsl:call-template name="meta-to-combo">
                        <xsl:with-param name="metadataSets" select="$metadataSets"/>
                        <xsl:with-param name="current" select="@name"/>
                     </xsl:call-template>
            </xsl:variable>
        <table class="header"><tbody><tr><td class="header"><xsl:copy-of select="$combo"/></td><td class="header"><a href="#" class="remove ui-icon ui-icon-closethick" title="Click me to remove"/></td></tr></tbody></table>
        </div>
    </xsl:template>


    <!-- ********** GSF:LINK ********** -->
    <xsl:template match="gsf:link" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>

        <div class="gsf_link css_gsf_link block" title="gsf:link">
        <xsl:variable name="link">
                LINK<select>
                    <xsl:choose>
                        <xsl:when test="@type='document'">
        	                <option value = "document" selected = "document">Document</option>
	                        <option value = "classifier">Classifier</option>
                            <option value = "source">Source</option>
                        </xsl:when>
                        <xsl:when test="@type='classifier'">
	                        <option value = "classifier" selected = "classifier">Classifier</option>
                            <option value = "document">Document</option>
                            <option value = "source">Source</option>
                        </xsl:when>
                        <xsl:when test="@type='source'">
                	        <option value = "source" selected = "source">Source</option>
                            <option value ="document">Document</option>
                            <option value ="classifier">Classifier</option>
                        </xsl:when>
                        <xsl:when test="@type='horizontal'">
                            <option value = "horizontal" selected = "horizontal">Horizontal</option>
                            <option value = "source" selected = "source">Source</option>
                            <option value ="document">Document</option>
                            <option value ="classifier">Classifier</option>
                        </xsl:when>
                  </xsl:choose>
	            </select>
        </xsl:variable>
        <table class="header"><tbody><tr><td class="header"><xsl:copy-of select="$link"/></td><td class="header"><a href="#" class="minmax ui-icon ui-icon-minusthick" title="Click me to expand">[-]</a></td><td class="header"><a href="#" class="remove ui-icon ui-icon-closethick" title="Click me to remove"/></td></tr></tbody></table>
                <xsl:apply-templates mode="xml-to-gui">
                    <xsl:with-param name="depth" select="$depth"/>
                    <xsl:with-param name="metadataSets" select="$metadataSets"/> 
                </xsl:apply-templates>
        </div> 
    </xsl:template>


    <!-- ********** GSF:TEMPLATE ********** -->
    <xsl:template match="gsf:template" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>

            <!-- CHILD = <xsl:value-of select="child[1]/@name"/> -->
            <!-- CHILD = <xsl:value-of select="child::*[name()][1]"/> -->


            <!-- <xsl:for-each select="child::*"> -->
            <!-- <xsl:value-of select="name()"/> -->
            <!-- </xsl:for-each> -->


        <div class="gsf_template css_gsf_template block" title="gsf:template">
            <xsl:variable name="mode">
                <xsl:choose>
                    <xsl:when test="not(@mode)"> <!-- parameter has not been supplied -->
                    </xsl:when>
                    <xsl:otherwise>MODE=<input type="text" name="rawtextinput" size="10" value="{@mode}"/></xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
    
                    <!-- <a href="#" class="minmax">[+]</a><a href="#" class="remove">[x]</a><a href="#" class="remove"><img src="interfaces/oran/images/green_button_close.png" alt="[x]" title="Click me to remove"/></a><a href="[myhref]" class="ui-state-default ui-corner-all"><span class="ui-icon ui-icon-home" style="float:left;"></span>Home</a> -->
            <table class="header"><tbody><tr><td class="header">MATCH=<input type="text" name="rawtextinput" size="10" value="{@match}"/></td><td class="header"><xsl:copy-of select="$mode"/></td><td class="header"><a href="#" class="minmax ui-icon ui-icon-plusthick" title="Click me to expand">[+]</a></td><td class="header"><a href="#" class="remove ui-icon ui-icon-closethick" title="Click me to remove"/></td></tr></tbody></table>

                <table class="table" border="1">
                <tbody>
                <tr class="tr">
                <td class="droppable" width="10px"></td>
                <xsl:apply-templates mode="xml-to-gui">
                    <xsl:with-param name="depth" select="$depth"/>
                    <xsl:with-param name="metadataSets" select="$metadataSets"/> 
                </xsl:apply-templates>
                </tr>
                </tbody>
                </table>
        </div><br/>
    </xsl:template>

    <!-- ********** GSF:SWITCH ********** -->
    <xsl:template match="gsf:switch" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>

        <div class="gsf_switch css_gsf_switch block" title="gsf:switch">
                <table class="header"><tbody><tr><td class="header">SWITCH</td><td class="header"><a href="#" class="minmax ui-icon ui-icon-minusthick">[-]</a></td><td class="header"><a href="[myhref]" class="ui-icon ui-icon-closethick"/></td></tr></tbody></table>
                <xsl:apply-templates mode="xml-to-gui">
                    <xsl:with-param name="depth" select="$depth"/>
                    <xsl:with-param name="metadataSets" select="$metadataSets"/>
                </xsl:apply-templates>
        </div>
    </xsl:template>


    <!-- ********** GSF:WHEN ********** -->
    <xsl:template match="gsf:when" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>

        <div class="gsf_when css_gsf_when block" title="gsf:when"> 
                <table class="header"><tbody><tr><td class="header">WHEN<xsl:value-of select="@test"/></td><td class="header"><a href="#" class="minmax ui-icon ui-icon-minusthick">[-]</a></td><td class="header"><a href="[myhref]" class="ui-icon ui-icon-closethick"/></td></tr></tbody></table>
                <xsl:apply-templates mode="xml-to-gui">
                  <xsl:with-param name="depth" select="$depth"/>
                  <xsl:with-param name="metadataSets" select="$metadataSets"/>
                </xsl:apply-templates>
        </div>
    </xsl:template>


    <!-- ********** GSF:OTHERWISE ********** -->
    <xsl:template match="gsf:otherwise" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>

        <div class="gsf_otherwise css_gsf_otherwise block" title="gsf:otherwise">
                <table class="header"><tbody><tr><td class="header">OTHERWISE</td><td class="header"><a href="#" class="minmax ui-icon ui-icon-minusthick">[-]</a></td><td class="header"><a href="#" class="remove">[x]</a></td><td><a href="[myhref]" class="ui-icon ui-icon-closethick"/></td></tr></tbody></table>
                <xsl:apply-templates mode="xml-to-gui">
                    <xsl:with-param name="depth" select="$depth"/>
                    <xsl:with-param name="metadataSets" select="$metadataSets"/>
                </xsl:apply-templates>
        </div>
    </xsl:template>


    <!-- ********** GSF:ICON ********** -->
    <xsl:template match="gsf:icon" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>

        <div class="gsf_icon css_gsf_icon block leaf" title="gsf:icon">
            <xsl:variable name="icon">
                ICON<select>
                    <xsl:choose>
                        <xsl:when test="@type='document'">
                            <option value = "document" selected = "document">Document</option>
                            <option value = "classifier">Classifier</option>
                            <option value = "source">Source</option>
                        </xsl:when>
                        <xsl:when test="@type='classifier'">
                            <option value = "classifier" selected = "classifier">Classifier</option>
                            <option value = "document">Document</option>
                            <option value = "source">Source</option>
                        </xsl:when>
                        <xsl:when test="@type='source'">
                            <option value = "source" selected = "source">Source</option>
                            <option value ="document">Document</option>
                            <option value ="classifier">Classifier</option>
                        </xsl:when>
                  </xsl:choose>
                </select>
                </xsl:variable>
            <table class="header"><tbody><tr><td class="header"><xsl:copy-of select="$icon"/></td><td class="header"><a href="#" class="remove ui-icon ui-icon-closethick" title="Click me to remove"/></td></tr></tbody></table>
        </div>
    </xsl:template>


    <!-- ********** GSF:DEFAULT ********** -->
    <xsl:template match="gsf:default" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>

        <div class="block gsf_default" title="gsf:default">
                <table class="header"><tbody><tr><td class="header">DEFAULT</td><td class="header"><a href="#" class="minmax ui-icon ui-icon-minusthick" title="Click me to expand">[-]</a></td><td class="header"><a href="[myhref]" class="ui-icon ui-icon-closethick"/></td></tr></tbody></table>
                <xsl:apply-templates mode="xml-to-gui">
                    <xsl:with-param name="depth" select="$depth"/>
                    <xsl:with-param name="metadataSets" select="$metadataSets"/>
                </xsl:apply-templates>
        </div>
    </xsl:template>


    <!-- ********** TEXT ********** -->
    <xsl:template match="text()" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>
        
        <div class="block leaf gsf_text" title="gsf:text">
                <xsl:variable name="rawtext"><xsl:value-of select="."/></xsl:variable>
                <table class="header"><tbody><tr><td class="header"><input type="text" name="rawtextinput" size="10" value="{$rawtext}"/></td><td class="header"><a href="[myhref]" class="ui-icon ui-icon-closethick"/></td></tr></tbody></table>
        </div>
    </xsl:template>


    <!-- ********** TABLE ********** -->
    <xsl:template match="td" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>
        

        <!-- <td class="td block resizable" title="td" valign="{@valign}" style='overflow: hidden;'> -->
        <td class="column" valign="{@valign}" title="td">
            <div class="td-div block" title="td-div">
            <div class="neverempty block" style="height:50px">NEVER EMPTY</div>

            <!-- <xsl:text>&lt;td</xsl:text>valign=<xsl:value-of select="@valign"/><xsl:text>&gt;</xsl:text><a href="#" class="minmax">[-]</a><a href="#" class="remove">[x]</a> -->
                <xsl:apply-templates mode="xml-to-gui">
                    <xsl:with-param name="depth" select="$depth"/>
                    <xsl:with-param name="metadataSets" select="$metadataSets"/> 
                </xsl:apply-templates>
                <!-- <xsl:text>&lt;/td&gt;</xsl:text><br/><br/> -->
            </div>
        </td>
        <td class="droppable" width="10px" title="Drag a new column on to me"/>
    </xsl:template>


    <!-- ********** BREAK ********** -->
    <xsl:template match="br" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>

        <!-- <xsl:text>&lt;br/&gt;</xsl:text> <br/> -->
    
        <xsl:apply-templates mode="xml-to-gui">
            <xsl:with-param name="depth" select="$depth"/>
            <xsl:with-param name="metadataSets" select="$metadataSets"/>
        </xsl:apply-templates>
    </xsl:template>


    <!-- ********** MATCH ALL ********** -->
    <xsl:template match="*" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>

        <xsl:apply-templates mode="xml-to-gui"> 
            <xsl:with-param name="depth" select="$depth"/> 
            <xsl:with-param name="metadataSets" select="$metadataSets"/> 
        </xsl:apply-templates> 
    </xsl:template>

</xsl:stylesheet>
