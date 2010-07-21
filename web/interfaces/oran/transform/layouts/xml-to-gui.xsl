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
    xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat">

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

    <xsl:template match="gsf:choose-metadata" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>
        
        <div class="block" title="gsf:link">
            <div class="header">
                CHOOSE <a href="#" class="minmax">[-]</a><a href="#" class="remove">[x]</a>
            </div>
            <div class="content">
                <xsl:apply-templates mode="xml-to-gui">
                    <xsl:with-param name="depth" select="$depth"/>
                    <xsl:with-param name="metadataSets" select="$metadataSets"/> 
                </xsl:apply-templates>
            </div>
            <div class="footer">
                END CHOOSE
            </div>
        </div>    
    </xsl:template>

    <xsl:template match="gsf:metadata" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>

        <div class="block" title="gsf:link">
            <div class="header">
                METADATA <a href="#" class="minmax">[-]</a><a href="#" class="remove">[x]</a>
            </div>
            <div class="content">
                <xsl:call-template name="meta-to-combo">
                    <xsl:with-param name="metadataSets" select="$metadataSets"/>
                    <xsl:with-param name="current" select="@name"/>
                </xsl:call-template>
            </div>
        </div>
    </xsl:template>

<!-- **************************************************************** -->

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
            <option value ="{$current_mod}" disabled="disabled" selected="{$current_mod}"><xsl:value-of select="$current_mod"/></option>

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

<!-- **************************************************************** -->

    <xsl:template match="gsf:link" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>
        
        <div class="block" title="gsf:link">
            <div class="header">
                LINK[type=
    	        <select>
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
	            </select>]<a href="#" class="minmax">[-]</a><a href="#" class="remove">[x]</a>
            </div>
            <div class="content">
                <xsl:apply-templates mode="xml-to-gui">
                    <xsl:with-param name="depth" select="$depth"/>
                    <xsl:with-param name="metadataSets" select="$metadataSets"/> 
                </xsl:apply-templates>
            </div>
            <div class="footer">
                END LINK <br/>
            </div>
        </div> 
    </xsl:template>

    <xsl:template match="gsf:template" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>

        <div class="block" title="gsf:template">
            <div class="header">
                TEMPLATE[match=<xsl:value-of select="@match"/>]<a href="#" class="minmax">[-]</a><a href="#" class="remove">[x]</a>
            </div>
            <div class="content">
                <xsl:apply-templates mode="xml-to-gui">
                    <xsl:with-param name="depth" select="$depth"/>
                    <xsl:with-param name="metadataSets" select="$metadataSets"/> 
                </xsl:apply-templates>
            </div>
            <div class="footer">
                END TEMPLATE <br/>
            </div>
        </div>
    </xsl:template>

    <xsl:template match="gsf:when" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>

        <div class="block" title="gsf:when"> 
            <div class="header">
                WHEN[test=<xsl:value-of select="@test"/>]<button>x</button> <br/><a href="#" class="minmax">[-]</a><a href="#" class="remove">[x]</a>
            </div>
            <div class="content">
                <xsl:apply-templates mode="xml-to-gui">
                  <xsl:with-param name="depth" select="$depth"/>
                  <xsl:with-param name="metadataSets" select="$metadataSets"/>
                </xsl:apply-templates>
            </div>
            <div class="footer">
                END WHEN
            </div>
        </div>
    </xsl:template>

    <xsl:template match="gsf:otherwise" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>

        <div class="block" title="gsf:otherwise">
            <div class="header">
                OTHERWISE <button>x</button><br/><a href="#" class="minmax">[-]</a><a href="#" class="remove">[x]</a>
            </div>
            <div class="content">
                <xsl:apply-templates mode="xml-to-gui">
                    <xsl:with-param name="depth" select="$depth"/>
                    <xsl:with-param name="metadataSets" select="$metadataSets"/>
                </xsl:apply-templates>
            </div>
            <div class="footer">
                END OTHERWISE
            </div>       
        </div>
    </xsl:template>

    <xsl:template match="gsf:icon" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>

        <div class="block" title="gsf:link">
            <div class="header">
                ICON
            </div>
            <div class="content">
              ICON[type=<xsl:value-of select="@type"/>]<a href="#" class="minmax">[-]</a><a href="#" class="remove">[x]</a>
            </div>
        </div>
    </xsl:template>

    <xsl:template match="gsf:switch" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>
        
        <div class="block" title="gsf:switch">
            <div class="content">
                SWITCH <button>x</button><br/><a href="#" class="minmax">[-]</a><a href="#" class="remove">[x]</a>
            </div>
            <div class="content">
                <xsl:apply-templates mode="xml-to-gui">
                    <xsl:with-param name="depth" select="$depth"/>
                    <xsl:with-param name="metadataSets" select="$metadataSets"/> 
                </xsl:apply-templates>
            </div>
            <div class="footer">
                END SWITCH
            </div>
        </div>
    </xsl:template>

    <xsl:template match="gsf:default" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>

        <div class="block" title="gsf:link">
            <div class="header">
                DEFAULT <a href="#" class="minmax">[-]</a><a href="#" class="remove">[x]</a>
            </div>
            <div class="content">
                <xsl:apply-templates mode="xml-to-gui">
                    <xsl:with-param name="depth" select="$depth"/>
                    <xsl:with-param name="metadataSets" select="$metadataSets"/>
                </xsl:apply-templates>
            </div>
            <div class="footer">
                END DEFAULT
            </div>
        </div>
    </xsl:template>

    <xsl:template match="text()" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>
        
        <div class="block" title="gsf:link">
            <div class="header">
                TEXT <a href="#" class="minmax">[-]</a><a href="#" class="remove">[x]</a>
            </div>
            <div class="content">
                <xsl:variable name="rawtext"><xsl:value-of select="."/></xsl:variable>
                TEXT = <input type="text" name="rawtextinput" size="10" value="{$rawtext}"/><br/>
            </div>
        </div>
    </xsl:template>

    <xsl:template match="td" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>

        <xsl:text>&lt;td</xsl:text>valign=<xsl:value-of select="@valign"/><xsl:text>&gt;</xsl:text><br/>

        <xsl:apply-templates mode="xml-to-gui">
            <xsl:with-param name="depth" select="$depth"/>
            <xsl:with-param name="metadataSets" select="$metadataSets"/> 
        </xsl:apply-templates>
        
        <xsl:text>&lt;/td&gt;</xsl:text><br/><br/>
    </xsl:template>

    <xsl:template match="br" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>

        <xsl:text>&lt;br/&gt;</xsl:text> <br/>
    
        <xsl:apply-templates mode="xml-to-gui">
            <xsl:with-param name="depth" select="$depth"/>
            <xsl:with-param name="metadataSets" select="$metadataSets"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="*" mode="xml-to-gui">
        <xsl:param name="depth"/>
        <xsl:param name="metadataSets"/>

        <xsl:apply-templates mode="xml-to-gui"> 
            <xsl:with-param name="depth" select="$depth"/> 
            <xsl:with-param name="metadataSets" select="$metadataSets"/> 
        </xsl:apply-templates> 
    </xsl:template>

</xsl:stylesheet>
