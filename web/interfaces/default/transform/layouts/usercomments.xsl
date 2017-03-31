<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	xmlns:gslib="http://www.greenstone.org/skinning"
	xmlns:gsf="http://www.greenstone.org/greenstone3/schema/ConfigFormat"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util gsf">

<xsl:template name="userCommentsSection">
 <xsl:if test="/page/pageResponse/format[@type='display']/gsf:option[@name='UserComments']/@value='true'">
  <!-- 1. Make some variables available to javascript that the usercomments related js functions need -->
  <gsf:variable name="d"><xsl:value-of select="/page/pageRequest/paramList/param[@name='d']/@value"/></gsf:variable>
  <gsf:variable name="c"><xsl:value-of select="/page/pageRequest/paramList/param[@name='c']/@value"/></gsf:variable>
  <gsf:variable name="site"><xsl:value-of select="/page/pageResponse/interfaceOptions/option[@name='site_name']/@value"/></gsf:variable>

  <gsf:variable name="textusercommentssection"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'usercomments.heading')"/></gsf:variable>
  <gsf:variable name="textisempty"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'usercomments.isempty')"/></gsf:variable>
  <gsf:variable name="textcommentsubmitted"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'usercomments.submitted')"/></gsf:variable>

  <!-- 2. Load the javascript, which will do stuff on window load/ready for which it needs the above gs.variables -->
  <script type="text/javascript" src="interfaces/{$interface_name}/js/gsajaxapi.js"><xsl:text> </xsl:text></script>
  <script type="text/javascript" src="interfaces/{$interface_name}/js/user_comments.js"><xsl:text> </xsl:text></script>

  
  <!-- 3. Set up the User comments section in the HTML -->
  <div id="commentssection" class="centrediv">    
    <div id="usercomments">
      <!-- A heading for the comment section will be added here dynamically either if
	   previously submitted comments exist, or if the form#usercommentform to add
	   a new comment is displayed. Otherwise only the "Add Comments" link is shown. -->
      <xsl:comment>Existing comments will be loaded dynamically loaded into this div#usercomments</xsl:comment>
    </div>

    <!-- If the user's logged in, show the comment form, else show the link to the login page -->
    <xsl:choose>
      <xsl:when test="/page/pageRequest/userInformation">	
	<!-- Logged in, allow user to add a comment by displaying a form -->
	<form name="AddUserCommentForm" id="usercommentform">
	  <input type="hidden" name="username"><xsl:attribute name="value"><xsl:value-of select="/page/pageRequest/userInformation/@username"/></xsl:attribute></input>
	  <div>
	    <p><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'usercomments.add')"/></p>
	    <!-- The textarea will be added in by javascript into div#commentarea to avoid the problem of XML turning empty tags into self-closing ones and a self-closing text-area becomes invalid HTML -->
	    <!--<textarea required="required" name="comment" rows="10" cols="64" placeholder="Add your comment here..."></textarea>-->
	    <div id="commentarea">Comment area to appear here</div>
	    <input type="hidden" name="d"><xsl:attribute name="value"><xsl:value-of select="/page/pageRequest/paramList/param[@name='d']/@value"/></xsl:attribute></input>
	  </div>
	  
	  <input type="submit" id="usercommentSubmitButton" onclick="addUserComment(document.AddUserCommentForm.username.value, document.AddUserCommentForm.comment.value, document.AddUserCommentForm.d.value, document); return false;"><xsl:attribute name="value"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'usercomments.submit')"/></xsl:attribute></input>
	  <label id="usercommentfeedback"><xsl:comment>Text to prevent empty tags from becoming self-closing tags</xsl:comment></label>

	  <div id="usercommentlogoutlink">
	    <a><xsl:attribute name="href"><xsl:call-template name="generateLogoutURL"/></xsl:attribute>
	    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'menu.logout')"/></a>
	  </div>
	</form>
      </xsl:when>

      <!-- User not logged in, "add comment" link allows user to login first -->
      <xsl:otherwise>
	<div id="usercommentlink">
	  <a><xsl:attribute name="href"><xsl:call-template name="generateLoginURL"/></xsl:attribute>
	  <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'usercomments.add')"/></a>
	</div>
      </xsl:otherwise>
    </xsl:choose>

  </div>
 </xsl:if>
</xsl:template>

</xsl:stylesheet>
