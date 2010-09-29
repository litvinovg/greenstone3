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

    <xsl:template name="xml-to-gui-templates">
        <xsl:param name="node-set" select="."/>
        <xsl:param name="metadataSets" select="."/>
    <!-- **************************************************************************** -->
    <!-- GSF STATEMENTS                                                               -->
    <!-- **************************************************************************** -->

    
    <!-- ********** GSF:CHOOSE-METADATA ********** -->
        <xsl:variable name="choose_metadata">
            <table class="header"><tbody><tr><td>CHOOSE</td><td><a href="#" class="minmax ui-icon ui-icon-minusthick" title="Click me to expand">[-]</a></td><td><a href="#" class="remove ui-icon ui-icon-closethick" title="Click me to remove"/></td></tr></tbody></table>
            <div class="gsf_choose_metadata css_gsf_choose_metadata block" title="gsf:choose-metadata"><table class="header"><tbody><tr><td class="header">CHOOSE</td><td class="header"><a href="#" class="minmax ui-icon ui-icon-minusthick" title="Click me to expand">[-]</a></td><td class="header"><a href="#" class="remove ui-icon ui-icon-closethick" title="Click me to remove"/></td></tr></tbody></table></div>
        </xsl:variable>

        <script type="text/javascript">
            gsf_choose_metadata_element = <xsl:text disable-output-escaping="yes">'</xsl:text><xsl:copy-of select="$choose_metadata"/><xsl:text disable-output-escaping="yes">';</xsl:text>   
        </script>   


    <!-- ********** GSF:METADATA ********** -->
        <xsl:variable name="metadata">
        <div class="gsf_metadata css_gsf_metadata block leaf" title="gsf:metadata"><table class="header"><tbody><tr><td class="header"><xsl:call-template name="meta-to-combo">
                        <xsl:with-param name="metadataSets" select="$metadataSets"/>
                        <xsl:with-param name="current" select="ex.title"/>
                     </xsl:call-template></td><td class="header"><a href="#" class="remove ui-icon ui-icon-closethick" title="Click me to remove"/></td></tr></tbody></table></div>
                    <!-- <a href="#" class="remove">[x]</a></div> -->
        </xsl:variable>

        <script type="text/javascript">
            gsf_metadata_element = <xsl:text disable-output-escaping="yes">'</xsl:text><xsl:copy-of select="$metadata"/><xsl:text disable-output-escaping="yes">';</xsl:text> 
        </script>


    <!-- ********** GSF:LINK ********** -->
        <xsl:variable name="link">
        <div class="gsf_link css_gsf_link block" title="gsf:link"><table class="header"><tbody><tr><td class="header">LINK<select>
        	                <option value = "document" selected = "document">Document</option>
	                        <option value = "classifier">Classifier</option>
                            <option value = "source">Source</option>
                            <option value = "horizontal">Horizontal</option>
	            </select></td><td><a href="#" class="minmax ui-icon ui-icon-minusthick">[-]</a><a href="#" class="remove ui-icon ui-icon-closethick">[x]</a></td></tr></tbody></table></div> 
        </xsl:variable>

        <script type="text/javascript">
            gsf_link_element = <xsl:text disable-output-escaping="yes">'</xsl:text><xsl:copy-of select="$link"/><xsl:text disable-output-escaping="yes">';</xsl:text>   
        </script>


    <!-- ********** GSF:TEMPLATE ********** -->

        <xsl:variable name="template">
        <div class="gsf_template block" title="gsf:template"><table class="header"><tbody><tr><td class="header">MATCH=<input type="text" name="rawtextinput" size="10"/></td><td><a href="#" class="minmax ui-icon ui-icon-minusthick">[-]</a></td><td><a href="#" class="remove ui-icon ui-icon-closethick">[x]</a></td></tr></tbody></table><table border="1"><tr class="tr"></tr></table></div><br/>
        </xsl:variable>

        <script type="text/javascript">
            gsf_template_element = <xsl:text disable-output-escaping="yes">'</xsl:text><xsl:copy-of select="$template"/><xsl:text disable-output-escaping="yes">';</xsl:text>      
        </script>


    <!-- ********** GSF:SWITCH ********** -->

        <xsl:variable name="switch">
        <div class="gsf_switch css_gsf_switch block" title="gsf:switch"><table class="header"><tbody><tr><td class="header">SWITCH</td><td><a href="#" class="minmax ui-icon ui-icon-minusthick">[-]</a></td><td><a href="#" class="remove ui-icon ui-icon-closethick">[x]</a></td></tr></tbody></table></div>
        </xsl:variable>

        <script type="text/javascript">
            gsf_switch_element = <xsl:text disable-output-escaping="yes">'</xsl:text><xsl:copy-of select="$switch"/><xsl:text disable-output-escaping="yes">';</xsl:text>
        </script>


    <!-- ********** GSF:WHEN ********** -->

        <xsl:variable name="when">
        <div class="gsf_when css_gsf_when block" title="gsf:when"><table class="header"><tbody><tr><td class="header">WHEN<input type="text" name="rawtextinput" size="10"/></td><td><a href="#" class="minmax ui-icon ui-icon-minusthick">[-]</a></td><td><a href="#" class="remove ui-icon ui-icon-closethick">[x]</a></td></tr></tbody></table></div>
        </xsl:variable>

        <script type="text/javascript">
            gsf_when_element = <xsl:text disable-output-escaping="yes">'</xsl:text><xsl:copy-of select="$when"/><xsl:text disable-output-escaping="yes">';</xsl:text>
        </script>

    <!-- ********** GSF:OTHERWISE ********** -->

        <xsl:variable name="otherwise">
        <div class="gsf_otherwise css_gsf_otherwise block" title="gsf:otherwise"><table class="header"><tbody><tr><td class="header">OTHERWISE</td><td><a href="#" class="minmax ui-icon ui-icon-minusthick">[-]</a></td><td><a href="#" class="remove ui-icon ui-icon-closethick">[x]</a></td></tr></tbody></table></div>
        </xsl:variable>

        <script type="text/javascript">
            gsf_otherwise_element = <xsl:text disable-output-escaping="yes">'</xsl:text><xsl:copy-of select="$otherwise"/><xsl:text disable-output-escaping="yes">';</xsl:text>
        </script>

    <!-- ********** GSF:ICON ********** -->
    
        <xsl:variable name="icon">
        <div class="gsf_icon css_gsf_icon block leaf" title="gsf:icon"><table class="header"><tbody><tr><td class="header">ICON<select><option value = "document" selected = "document">Document</option><option value = "classifier">Classifier</option><option value = "source">Source</option></select></td><td><a href="#" class="remove ui-icon ui-icon-closethick">[x]</a></td></tr></tbody></table></div>
        </xsl:variable>

        <script type="text/javascript">
            gsf_icon_element = <xsl:text disable-output-escaping="yes">'</xsl:text><xsl:copy-of select="$icon"/><xsl:text disable-output-escaping="yes">';</xsl:text>
        </script>
    </xsl:template>

</xsl:stylesheet>
