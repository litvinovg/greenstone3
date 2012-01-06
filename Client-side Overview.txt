Greenstone 3 now includes support for client-side XSLT, i.e. the
user's web browser is responsible for converting the raw XML message
returned by the web server into a web page (using the XSL file also
returned from the web server).  The seeds of this idea came from work
on supporting Greenstone installed on an Android device.  Other work
done at this time to make the Greenstone 3 server run more efficiently
was profiling the code for the most expensive methods, resulting in
principally changes in how strings were handled.


More specifically the main modifications were:

* The use of Apache Commons StringUtils class for text replacement and
  splitting;

* The use of StringUtils.contains over the *.text.* regular expression;

* Plus: client-side XSLT support.

Note that the XML Texts collection had to be modified in certain areas
due to it using four XSL overrides in its transform directory. This
may or may not be necessary depending on the XSL overrides in place
for other collections. For an example of this, see the
about-clientside.xsl file in the gberg collection.

Steven McTainsh
February 2011

