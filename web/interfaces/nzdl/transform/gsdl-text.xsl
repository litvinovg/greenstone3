<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  extension-element-prefixes="java">

  <xsl:template name="aboutgs">about the greenstone software</xsl:template>
  <xsl:template name="textgreenstone1">
Greenstone is a suite of software which has the ability to serve digital
library collections and build new collections.  It provides a new way of
organizing information and publishing it on the Internet or on CD-ROM.
Greenstone is produced by the New Zealand Digital Library Project at the
University of Waikato, and distributed in cooperation with UNESCO and the
Human Info NGO. It is open-source software, available from
<i>http://greenstone.org</i> under the terms of the GNU General Public
License.
  </xsl:template>

  <xsl:template name="textgreenstone2">
The New Zealand Digital Library website (<a href="http://nzdl.org">http://nzdl.org</a>) contains numerous example
collections, all created with the Greenstone software, which are publicly
available for you to peruse. They exemplify various searching and browsing
options, and include collections in Arabic, Chinese, French, Maori, and
Spanish, as well as English. There are also some music collections.
  </xsl:template>

  <xsl:template name="textplatformtitle">platform</xsl:template>
  <xsl:template name="textgreenstone3">
Greenstone runs on Windows and Unix. The distribution includes ready-to-use
binaries for all versions of Windows, and for Linux. It also includes
complete source code for the system, which can be compiled using Microsoft
C++ or gcc.  Greenstone works with associated software that is also freely
available: the Apache Webserver and PERL. The user interface uses a Web
    browser: typically Netscape Navigator or Internet Explorer.
  </xsl:template>

  <xsl:template name="textgreenstone4">
Many document collections are distributed on CD-ROM using the Greenstone
software. For example, the <i>Humanity Development Library</i> contains
1,230 publications ranging from accounting to water sanitation. It runs on
minimal computing facilities such as those typically found in developing
countries. The information can be accessed by searching, browsing by
subject, browsing by titles, browsing by organisation, browsing a list of
how-tos, and by randomly viewing the book covers.
  </xsl:template>

  <xsl:template name="textcustomisationtitle">customisation</xsl:template>
  <xsl:template name="textgreenstone5">
Greenstone is specifically designed to be highly extensible and
customisable. New document and metadata formats are accommodated by writing
"plugins" (in Perl). Analogously, new metadata browsing structures can be
implemented by writing "classifiers." The user interface look-and-feel can
be altered using "macros" written in a simple macro language. A Corba
protocol allows agents (e.g. in Java) to use all the facilities associated
with document collections. Finally, the source code, in C++ and Perl, is
available and accessible for modification.
  </xsl:template>

<xsl:template name="textdocumentationtitle">documentation</xsl:template>
<xsl:template name="textdocuments">Extensive documentation for the Greenstone software is available.</xsl:template>

  <xsl:template name="textmailinglisttitle">mailing list</xsl:template>
  <xsl:template name="textmailinglist">
There is a mailing list intended primarily for discussions about the
Greenstone digital library software.  Active users of Greenstone should
consider joining the mailing list and contributing to the discussions.
To subscribe, go to  <a href="https://list.scms.waikato.ac.nz/mailman/listinfo/greenstone-users">https://list.scms.waikato.ac.nz/mailman/listinfo/greenstone-users</a>.

To send a message to the list, address it to <a
href="mailto:greenstone-users@list.scms.waikato.ac.nz"
>greenstone-users@list.scms.waikato.ac.nz</a>.
</xsl:template>

  <xsl:template name="textbugstitle">bugs</xsl:template>
  <xsl:template name="textreport">
We want to ensure that this software works well for you.  Please report any
bugs to <a href="mailto:greenstone@cs.waikato.ac.nz">greenstone@cs.waikato.ac.nz</a>
  </xsl:template>

<xsl:template name="textgs3title">in the works</xsl:template>

<xsl:template name="textgs3">Greenstone 3 is a complete redesign and 
reimplementation which 
retains all the advantages of Greenstone 2 (the current version)--for example, 
it is multilingual, multiplatform, and highly configurable. It 
incorporates all the features of the existing system, and is backwards 
compatible: that is, it can build and run existing collections without 
modification. Written in Java, it is structured as a network of 
independent modules that communicate using XML: thus it runs in a 
distributed fashion and can be spread across different servers as 
necessary. This modular design increases the flexibility and 
extensibility of Greenstone. The new version is expected to be 
available for experimental use by 23 December 2003. An initial design for 
the system is outlined in "The design of Greenstone 3: An agent based 
dynamic digital library" (download <a href="http://www.greenstone.org/manuals/gs3desig.pdf">PDF</a>).
</xsl:template>


  <xsl:template name="textcreditstitle">credits</xsl:template>

  <xsl:template name="textwhoswho">
The Greenstone software is a collaborative effort between many
people. Rodger McNab and Stefan Boddie are the principal architects and
implementors.  Contributions have been made by David Bainbridge, George
Buchanan, Michael Dewsnip, Katherine Don, Hong Chen, Elke Duncker, 
Carl Gutwin, Geoff Holmes, John McPherson, Craig Nevill-Manning, 
Dynal Patel, Gordon Paynter, Bernhard Pfahringer, Todd
Reed, Bill Rogers, John Thompson, and Stuart Yeates.
Other members of the New Zealand Digital Library project provided advice 
and inspiration in the design of
the system: Mark Apperley, Sally Jo Cunningham, Matt Jones, Steve Jones, 
Te Taka Keegan, Michel Loots, Malika Mahoui, Gary Marsden, Dave Nichols 
and Lloyd Smith. We would also like to
acknowledge all those who have contributed to the GNU-licensed packages
included in this distribution: MG, GDBM, PDFTOHTML, WGET, WVWARE and XLHTML.
  </xsl:template>

  
</xsl:stylesheet>