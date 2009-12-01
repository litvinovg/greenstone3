<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  extension-element-prefixes="java">

  <!--<xsl:include href="style.xsl"/>-->
<xsl:template name="textpagetitle">New Zealand Digital Library</xsl:template>
<xsl:template name="textprojhead">The New Zealand Digital Library Project</xsl:template>

<xsl:template name="textprojinfo">
<p />
The New Zealand Digital Library project is a research programme at
The University of Waikato whose aim is to develop the underlying
technology for digital libraries and make it available publicly so that
others can use it to create their own collections. 
<p />
Our web site provides several document collections, including historical
documents, humanitarian and development information, computer
science technical reports and bibliographies, literary works, and
magazines. All are available over the Web, and can be accessed
through searching and browsing interfaces provided by the
Greenstone digital library software. Behind the query interface lies a
huge collection providing gigabytes of information. We hope you find
what you want, or at least something intriguing!
</xsl:template>

<xsl:template name="titlesoftwareinfo">The Greenstone software</xsl:template>

<xsl:template name="textsoftwareinfo">
<p />
The <a href="http://www.greenstone.org">Greenstone Digital Library software</a> provides a new way of
organizing information and making it available over the Internet or on
CD-ROM. It is open-source software, available under the terms of the
Gnu public license. 
<p />
A digital library is made up of a set of collections. Each collection of
information comprises several (typically several thousand, or even
several million) documents, which share a uniform searching and
browsing interface. Collections can be organized in many different
ways while retaining a strong family resemblance. 

<p>To subscribe to the Greenstone mailing list, go to <a 
href="https://list.scms.waikato.ac.nz/mailman/listinfo/greenstone-users">https://list.scms.waikato.ac.nz/mailman/listinfo/greenstone-users</a>.</p>

<p align='right'/>
<a href="{$library_name}?a=p&amp;sa=gsdl">More...</a>
</xsl:template>

<xsl:template name="titleresearchinfo">Our research</xsl:template>

<xsl:template name="textresearchinfo">
<p />
The goal of our research program is to explore the potential of
internet-based digital libraries. Our vision is to develop systems that
automatically impose structure on anarchic, uncatalogued, distributed
repositories of information, thereby providing information consumers
with effective tools to locate what they need and to peruse it
conveniently and comfortably. 
<p />
Project members are actively working on techniques for creating,
managing, and and mainatining collections; extracting metadata from
legacy documents; analysing library usage and user needs; Maori,
Arabic and Chinese language systems; internationalising the library
interface; optical music recognition and musical collections; novel
interfaces for formulating queries and visualising results; novel
interfaces for browsing metadata; text mining for keyphrases,
acronyms, and other metadata; keyphrase extraction and
phrase-based browsing; and other research topics. 
<p align='right'/>
<a href="http://www.nzdl.org/html/research.html">More...</a>
</xsl:template>

<xsl:template name="titleaffiliateinfo">Our affiliates</xsl:template>

<xsl:template name="textaffiliatehumaninfo">
<p />
<a href="http://humaninfo.org">Human Info NGO</a>
is a registered charity responsible for the
provision of universal low-cost
information access through co-operation between UN Agencies,
universities and NGOs. Human Info NGO collaborates extensively
with the NZDL project, and use the Greenstone software. 
</xsl:template>

<xsl:template name="textaffiliateunesco">
<p />
<a href="http://www.unesco.org">United Nations Educational, Scientific and Cultural Organization</a>.
The dissemination of educational, scientific and cultural information
throughout the world, and particularly its availability
</xsl:template>

<xsl:template name="textpoem">
<br /><h2>Kia papapounamu te moana</h2>

<p />kia hora te marino,
<br />kia tere te karohirohi,
<br />kia papapounamu te moana

<p />may peace and calmness surround you,
<br />may you reside in the warmth of a summer's haze,
<br />may the ocean of your travels be as smooth as the polished greenstone.
</xsl:template>

<xsl:template name="textgreenstone"> 
<p />Greenstone is a semi-precious stone that (like this software) is sourced
in New Zealand.  In traditional Maori society it was the most highly prized
and sought after of all substances.  It can absorb and hold <i>wairua</i>,
which is a spirit or life force, and is endowed with traditional virtues
that make it an appropriate emblem for a public-domain digital library
project.  Its lustre shows charity; its translucence, honesty; its
toughness, courage; and the sharp edge it can take, justice.  The carved
piece used in the Greenstone Digital Library Software logo is a <i>patu</i>
or fighting club, and is a family heirloom of one of our project members.
In hand-to-hand combat its delivery is very quick, very accurate, and very
complete.  We like to think these qualities also apply to our software, the
razor sharp edge of the <i>patu</i> symbolizing the leading edge of
technology.
</xsl:template>

<xsl:template name="textaboutgreenstone">
<p />Greenstone is a suite of software for building and distributing digital
library collections. It provides a new way of organizing information and
publishing it on the Internet or on CD-ROM.  Greenstone is produced by the
<b>New Zealand Digital Library Project</b> at the <b>University of
Waikato</b>, and developed and distributed in cooperation with
<b>UNESCO</b> and the <b>Human Info NGO</b>.  It is open-source
software, available from <a href="http://greenstone.org">http://greenstone.org</a> under the terms of
the GNU General Public License.

<p />The aim of the software is to empower users, particularly in
universities, libraries, and other public service institutions, to
build their own digital libraries.  Digital libraries are radically
reforming how information is disseminated and acquired in UNESCO's
partner communities and institutions in the fields of education,
science and culture around the world, and particularly in developing
countries.  We hope that this software will encourage the effective
deployment of digital libraries to share information and place it in
the public domain.


<p />This software is developed and distributed as an international
cooperative effort established in August 2000 among three parties.

<table border="0">
<tr valign="top">
<td>
<a href="http://nzdl.org"><b>New Zealand Digital Library Project at the University of Waikato</b></a>
<br />
Greenstone software grew out of this project, and this initiative
has been endorsed by the Communication Sub-Commission of the New
Zealand National Commission for UNESCO as part of New Zealand's
contribution to UNESCO's programme.
</td>
<td></td>
</tr>
<tr valign="top">
<td>
<a href="http://www.unesco.org"><b>United Nations Educational, Scientific and Cultural Organization</b></a>
<br />
The dissemination of educational, scientific and cultural information
throughout the world, and particularly its availability in developing
countries, is central to UNESCO's goals as pursued within its
intergovernmental Information for All Programme, and appropriate,
accessible information and communication technology is seen as an important
tool in this context.
</td>
<td><a href="http://www.unesco.org"><img src="interfaces/nzdl/images/unesco.gif" border="0"/></a></td>
</tr>
<tr valign="top">
<td>
<a href="http://humaninfo.org"><b>The Human Info NGO, based in Antwerp, Belgium</b></a>
<br />
This project works with UN agencies and other NGOs, and has established
a worldwide reputation for digitizing documentation of interest to
human development and making it widely available, free of charge to
developing nations and on a cost-recovery basis to others.
</td>
<td><a href="http://humaninfo.org"><img src="interfaces/nzdl/images/ghproj2.jpg" border="0"/></a></td>
</tr>
</table>
</xsl:template>

  <xsl:template name="textimagegreenstone">Greenstone Digital Library Software</xsl:template>
  
  <xsl:template name="nzdlpagefooter">
      <p /><xsl:call-template name="dividerBar"/>
      <p /><a href="http://www.nzdl.org">New Zealand Digital Library Project</a>
      <br /><a href="http://www.cs.waikato.ac.nz/cs">Department of Computer Science</a>, <a href="http://www.waikato.ac.nz">University of Waikato</a>, New Zealand
    </xsl:template>
    
</xsl:stylesheet>