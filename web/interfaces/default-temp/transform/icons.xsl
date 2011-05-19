<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 
  <!-- some global parameters - these are set by whoever is invoking the transformation -->
  <xsl:param name="interface_name"/>

  <!-- Display the appropriate image, depending on the node type -->
  <xsl:template match="documentNode" mode="displayNodeIcon">
    
    <!-- Root node: book icon (open or closed) -->
    <xsl:choose>
      <xsl:when test="@nodeType='root'">
	<xsl:choose>
	  <xsl:when test="documentNode">
	    <xsl:call-template name="openbookimg">
	    </xsl:call-template>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:call-template name="closedbookimg">
	    </xsl:call-template>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:when>
      
      <!-- Internal node: folder icon (open or closed) -->
      <xsl:when test="@nodeType='internal'">
	<xsl:choose>
	  <xsl:when test="documentNode">
	    <xsl:call-template name="openfolderimg">
	    </xsl:call-template>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:call-template name="closedfolderimg">
	    </xsl:call-template>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:when>
      
      <!-- Leaf node: page icon, and this is the default -->
      <xsl:otherwise>
	<xsl:call-template name="textpageimg">
	</xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="openbookimg">
  	<xsl:param name="title"/>
    <xsl:param name="alt"/>
    <img  border="0" width="28" height="23"
      src="interfaces/default/images/openbook.gif"
      alt="{$alt}" class="getTextFor null this.title.{$title}" />
  </xsl:template>

  <xsl:template name="closedbookimg">
    <xsl:param name="alt"/>
    <img border="0" width="18" height="11"
      src="interfaces/default/images/book.gif"
      alt="{$alt}" class="getTextFor null this.title.open_book" />
  </xsl:template>

  <xsl:template name="openfolderimg">
  	<xsl:param name="title"/>
    <xsl:param name="alt"/>
    <img border="0" width="23" height="15"
      src="interfaces/default/images/openfldr.gif"
      alt="{$alt}" class="getTextFor null this.title.{$title}" />
  </xsl:template>

  <xsl:template name="closedfolderimg">
  	<xsl:param name="title"/>
    <xsl:param name="alt"/>
    <img  border="0" width="23" height="15"
      src="interfaces/default/images/clsdfldr.gif"
      alt="{$alt}" class="getTextFor null this.title.{$title}" />
  </xsl:template>

  <xsl:template name="textpageimg">
    <xsl:param name="alt"/>
    <img  border="0" width="16" height="21"
      src="interfaces/default/images/itext.gif"
      alt="{$alt}" class="getTextFor null this.title.view_document,parent.class.text:'clientDocView'" />
    <span><xsl:value-of select="$alt" /></span>
  </xsl:template>

  <xsl:template name="bookshelfimg">
    <xsl:param name="alt"/>
    <xsl:param name="title"/>
    <img  border="0" width="20" height="16"
      src="interfaces/default/images/bshelf.gif"  
      alt="{$alt}" title="{$title}"/>
  </xsl:template>

  <xsl:template name="iconpdf">
    <xsl:param name="alt">PDF</xsl:param>
    <img border="0" width='26' height='26' 
      src='interfaces/default/images/ipdf.gif' 
      alt='{$alt}' class="getTextFor null this.title.texticonpdf" />
  </xsl:template>
  
  <xsl:template name="icondoc">
    <xsl:param name="alt">Word</xsl:param>
    <img border="0" width='26' height='26' 
      src='interfaces/default/images/imsword.gif' 
      alt='{$alt}' class="getTextFor null this.title.texticonmsword" />
  </xsl:template>

</xsl:stylesheet>

