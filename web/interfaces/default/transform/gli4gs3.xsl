<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">
  
  <xsl:output method="xml"/>    
  
  <!--
	1. This XSLT rewrites the applet page generated by PageAction, from the top node of / down,
	to replace it with a JNLP Java Web Start file to launch GLI (web/applet/SignedGatherer.java).
	2. Putting in an xml processing instruction doesn't seem to go through and causes stylesheet transform errors
	after following https://www.w3schools.com/xml/ref_xsl_el_processing-instruction.asp 
  -->
  <xsl:template match="/">
	  <!--
		/*
		 *    Java Web Start support for Greenstone Librarian Interface (GLI) applet.
		 *    Copyright (C) 2002 New Zealand Digital Library, http://www.nzdl.org
		 *
		 *    This program is free software; you can redistribute it and/or modify
		 *    it under the terms of the GNU General Public License as published by
		 *    the Free Software Foundation; either version 2 of the License, or
		 *    (at your option) any later version.
		 *
		 *    This program is distributed in the hope that it will be useful,
		 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
		 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
		 *    GNU General Public License for more details.
		 *
		 *    You should have received a copy of the GNU General Public License
		 *    along with this program; if not, write to the Free Software
		 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
		 */
	  -->
	  <jnlp
		  spec="1.0+"		  
		  href=""><!-- don't set href to GLIappWebStart.jnlp, because when downloading it will then expect a static file by that name to exist in the location given by codebase-->
		  <xsl:attribute name="codebase"><xsl:value-of select='/page/pageRequest/@baseURL'/>applet</xsl:attribute><!--set codebase to location of jar file-->
		  
		  <information>
			<title>Greenstone Librarian Interface</title>
			<vendor>New Zealand Digital Library</vendor>
			<homepage href="http://www.greenstone.org/"/>
			<description>GLI</description>
			<description kind="short">Greenstone Librarian Interface (GLI) as Web Start Application over JNLP</description>
			<offline-allowed/>
		  </information>
		  <resources>
			<j2se version="1.7+"/>
			<jar href="SignedGatherer.jar"/>
		  </resources>
		  <application-desc main-class="org.greenstone.gatherer.WebGatherer">
			<!--<argument>-gwcgi=/greenstone3</argument>
			<argument>-gsdl3=true</argument>-->
			<argument>-gwcgi=<xsl:value-of select="/page/pageResponse/Applet/PARAM[@name='gwcgi']/@value" /></argument>
			<argument>-gsdl3=<xsl:value-of select="/page/pageResponse/Applet/PARAM[@name='gsdl3']/@value" /></argument>
		  </application-desc>		 
		  <!-- https://stackoverflow.com/questions/12600076/getting-user-home-folder-from-jws-signed-jar -->
		  <security>
			<all-permissions/>
		  </security>
		</jnlp>
	 
  </xsl:template>

</xsl:stylesheet>
