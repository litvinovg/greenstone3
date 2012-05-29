<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">

  <xsl:param name="berryBaskets"/>
  
  <xsl:template name="berrybasket">
    <div id="berrybasket" class="hide" >
      <div id="baskethandle"><span></span></div>
      <div id ="berries"><span></span></div>
    </div>
  </xsl:template>
  
  <xsl:template name="documentBerryBasket">
    <xsl:param name="collName"/>
    <xsl:param name="selectedNode"/>
    <xsl:param name="rootNode"/>
    <xsl:param name="docType" />
    <div id="berrybasket" class="hide" >
      <div id="baskethandle"><span></span></div>
      <div id ="berries" ><span></span></div>
    </div>
    <xsl:choose>
      <xsl:when test="$selectedNode = $rootNode">
	<p id="documentberries">    
	  <img class='pick'  id="{$collName}:{$rootNode}" src="interfaces/default/images/berry3.png" alt="in basket" width="15" height="15" border="0"/><span id="{$collName}:{$rootNode}:root" class="documentberry">the whole document</span></p>       
      </xsl:when>
      <xsl:otherwise>
	<p id="documentberries">    
	  <img class='pick'  id="{$collName}:{$rootNode}" src="interfaces/default/images/berry3.png" alt="in basket" width="15" height="15" border="0"/><span id="{$collName}:{$rootNode}:root" class="documentberry">the whole document</span><img class='pick'  id="{$collName}:{$selectedNode}" src="interfaces/default/images/berry3.png" alt="in basket" width="15" height="15" border="0"/><span id="{$collName}:{$selectedNode}:section" class="documentberry">the current section</span></p>
      </xsl:otherwise> 
    </xsl:choose>
  </xsl:template>

  
  <!-- done
  <xsl:template name="loadLibrary">
    <script type="text/javascript" src="interfaces/basic/js/YAHOO.js"><xsl:text disable-output-escaping="yes"> </xsl:text></script>
    <script type="text/javascript" src="interfaces/basic/js/event.js"><xsl:text disable-output-escaping="yes"> </xsl:text></script>
    <script type="text/javascript" src="interfaces/basic/js/connection.js"><xsl:text disable-output-escaping="yes"> </xsl:text></script>
    <script type="text/javascript" src="interfaces/basic/js/dom.js"><xsl:text disable-output-escaping="yes"> </xsl:text></script>
    <script type="text/javascript" src="interfaces/basic/js/dragdrop.js"><xsl:text disable-output-escaping="yes"> </xsl:text></script>
    <script type="text/javascript" src="interfaces/basic/js/ygDDPlayer.js"><xsl:text disable-output-escaping="yes"> </xsl:text></script>
    <script type="text/javascript" src="interfaces/basic/js/ygDDOnTop.js"><xsl:text disable-output-escaping="yes"> </xsl:text></script>
   </xsl:template>
	-->
  
	<!-- done
  <xsl:template name="js-library">
    <xsl:call-template name="loadLibrary" />	
    <xsl:call-template name="basketCheckout" />
  </xsl:template>
	-->
  
  <!-- should be called for a documentNode -->
  <xsl:template name="addBerry">
    <xsl:param name="collName"/>
    <td valign="top"><img class='pick' id="{$collName}:{@nodeID}" src="interfaces/default/images/berry3.png" alt="in basket" width="15" height="15" border="0"/></td>
  </xsl:template>
  
  <!-- done
  <xsl:template name="berryStyleSheet">
    <link rel="stylesheet" href="interfaces/basic/style/berry.css" type="text/css"/>  
  </xsl:template>
  -->
  
</xsl:stylesheet>
