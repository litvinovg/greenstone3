<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
  extension-element-prefixes="java util"
  exclude-result-prefixes="java util">

  <!-- style includes global params interface_name, library_name -->
  <xsl:include href="style.xsl"/>
  <xsl:include href="service-params.xsl"/>

  <xsl:output method="html"/> 

  <xsl:template name="pageTitle">
    <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.authentication')"/>
  </xsl:template>

  <!-- put a space in the title in case the actual value is missing - mozilla will not display a page with no title-->
  <xsl:template name="pageHead">
    <head>
      <title>
	<xsl:call-template name="pageTitle"/><xsl:text> </xsl:text>
      </title>
      <xsl:call-template name="globalStyle"/>
      <xsl:call-template name="pageStyle"/>
    </head>
  </xsl:template>

  <xsl:template name="pageStyle"/>

  <xsl:template match="page">
    <html>
      <xsl:call-template name="pageHead" />
      <xsl:call-template name="addGroup" />
      <body>
	<xsl:attribute name="dir"><xsl:call-template name="direction"/></xsl:attribute>
	<div id="page-wrapper">
 	  <xsl:variable name="authen_service" select="/page/pageRequest/paramList/param[@name='s']/@value"/>
	  <xsl:variable name="sub_action" select="/page/pageRequest/@subaction"/>
	  <xsl:variable name="asn_param" select ="/page/pageRequest/paramList/param[@name='s1.asn']/@value"/>
	  <xsl:variable name="uan" select ="/page/pageRequest/paramList/param[@name='s1.uan']/@value"/>
	  <xsl:variable name="pro_action" select ="/page/pageResponse/authenticationNode/service/@aup"/>
	  <xsl:variable name="au_node" select ="/page/pageResponse/authenticationNode"/>
	  <xsl:variable name="asn" select ="/page/pageResponse/authenticationNode/service/@asn"/>
	  <xsl:variable name="info" select ="/page/pageResponse/authenticationNode/service/@info"/>
	  <xsl:variable name="err" select ="/page/pageResponse/authenticationNode/service/@err"/>
	  <xsl:variable name="rt" select="/page/pageRequest/paramList/param[@name='rt']/@value"/>
	  <xsl:variable name="un_s" select="/page/pageResponse/authenticationNode/service/@un"/>
	  <xsl:variable name="pw_s" select="/page/pageResponse/authenticationNode/service/@pw"/>
	  
	  <div align="left" >
	    <table align="left" border="0" width="100%">
	      <tr align="left">
		<td><h2><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.authentication')"/></h2></td>
		<!--<td><xsl:if test="$un_s!=''">
		    <xsl:if test="$asn!='' and $asn!='0'">
		      <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.username')"/>  :  <xsl:value-of select="$un_s"/>
		    </xsl:if>
		  </xsl:if></td>-->
	      </tr>
	    </table>
	  </div>

	  <div id="navbar">
	    <ul id="navbarlist">
	      <!--greenstone home-->
	      <li><a href="{$library_name}"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.greenstone_home')"/></a></li>
	      
	      <!--list users-->
	      <li>
		<xsl:choose>
		  <xsl:when test="$asn!='' and $asn!='0' and $un_s!=''">
		    <a href="{$library_name}?a=g&amp;rt=r&amp;sa=authen&amp;s=Authentication&amp;s1.aup=ListUsers&amp;s1.asn={$asn}&amp;s1.uan=1&amp;s1.un={$un_s}&amp;s1.pw="><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.list_users')"/></a>
		  </xsl:when>
		  <xsl:otherwise>
		    <a href="{$library_name}?a=g&amp;rt=r&amp;sa=authen&amp;s=Authentication&amp;s1.asn=&amp;s1.aup=Login"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.list_users')"/></a>
		  </xsl:otherwise>
		</xsl:choose></li>
	      
	      <!--add a new user-->
	      <li>
		<xsl:choose>
		  <xsl:when test="$asn!='' and $asn!='0' and $un_s!=''">
		    <a href="{$library_name}?a=g&amp;rt=r&amp;sa=authen&amp;s=Authentication&amp;s1.aup=AddUser&amp;s1.asn={$asn}&amp;s1.uan=1&amp;s1.un={$un_s}&amp;s1.pw=&amp;s1.umun=&amp;s1.umpw=&amp;s1.umgp=&amp;s1.umas=&amp;s1.umc="><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.add_a_new_user')"/></a> 
		  </xsl:when>
		  <xsl:otherwise>
		    <a href="{$library_name}?a=g&amp;rt=r&amp;sa=authen&amp;s=Authentication&amp;s1.asn=&amp;s1.aup=Login"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.add_a_new_user')"/></a>
		  </xsl:otherwise>
		</xsl:choose>
	      </li>
	      
	      <!-- change password-->
	      <li>
		<xsl:choose>
		  <xsl:when test="$asn!='' and $asn!='0' and $un_s!=''">
		    <a href="{$library_name}?a=g&amp;rt=r&amp;sa=authen&amp;s=Authentication&amp;s1.aup=ModifyPassword&amp;s1.asn={$asn}&amp;s1.uan=1&amp;s1.un={$un_s}&amp;s1.pw=&amp;s1.umpw1=&amp;s1.umpw2=&amp;s1.umpw="><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.change_password')"/></a>
		  </xsl:when>
		  <xsl:otherwise>
		    <a href="{$library_name}?a=g&amp;rt=r&amp;sa=authen&amp;s=Authentication&amp;s1.asn=&amp;s1.aup=Login"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.change_password')"/></a>
		  </xsl:otherwise>
		</xsl:choose>
	      </li>

	      <li>
		<xsl:choose>
		  <xsl:when test="$un_s!='' and $asn!='' and $asn!='0'">
		    <a href="{$library_name}?a=g&amp;rt=r&amp;sa=authen&amp;s=Authentication&amp;s1.asn=&amp;s1.aup=Login&amp;s1.un=&amp;s1.pw="><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.logout')"/></a>
		  </xsl:when>
		  <xsl:otherwise>
		    <a href="{$library_name}?a=g&amp;rt=r&amp;sa=authen&amp;s=Authentication&amp;s1.asn=&amp;s1.aup=Login"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.login')"/></a>
		  </xsl:otherwise>
		</xsl:choose>
	      </li>
	    </ul>
	  </div>

	  <table align="left" border="0" width="100%">
	    <tr>
	      <td>
		<div id="content" align="center">
		  <!-- login -->
		  <xsl:if test="$pro_action='Login'">
		    <xsl:choose>
		      <xsl:when test="$asn!='' and $asn!='0'">
			<p align="left"> <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.login_successfully')"/></p>
		      </xsl:when>
		      <xsl:otherwise>

			<p align="left">
			  <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.login_descibe_line_3')"/><br/></p>
			<xsl:if test="$err='un-pw-err'">
			  <p align="left"><font color='red'><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.wrong_password_warning')"/></font></p>
			</xsl:if>
			<xsl:if test="$err='no-permission' and $info='Login'">
			  <p align="left"><font color='red'><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.no_permission')"/></font></p>
			</xsl:if>
			<xsl:if test="$err='as-false'">
			  <p align="left"><font color='red'><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.account_status_false')"/></font></p>
			</xsl:if>
			<xsl:call-template name="login">
			  <xsl:with-param name="sub_action" select="$sub_action"/>
			  <xsl:with-param name="authen_service" select="$authen_service"/>
			  <xsl:with-param name="pro_action" select="$pro_action"/>	
			</xsl:call-template>

		      </xsl:otherwise>
		    </xsl:choose>
		  </xsl:if>

		  <!-- list users-->
		  <xsl:if test="$pro_action='ListUsers'">
		    
		    <xsl:if test="$asn='1' and $info='all-un'">
		      <xsl:call-template name="listusers" >
		      </xsl:call-template>
		    </xsl:if>

		    <xsl:if test="$info='Login'">
		      <p align="left">
			<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.login_descibe_line_2')"/><br/></p>
		      <xsl:if test="$err='no-permission' and $info='Login'">
			<p align="left"><font color='red'><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.no_permission')"/></font></p>
		      </xsl:if>
		    </xsl:if>
		  </xsl:if>
		  
		  <!-- add a new user-->
		  <xsl:if test="$pro_action='AddUser'"> 
		    <xsl:if test="$err!='' and $err!='no-permission' and $err!='un-exist' and $err!='un-err' and $err!='pw-err'">
		      <p align="left"><font color='red'><xsl:value-of select="$err" /></font></p>
		    </xsl:if>

		    <xsl:if test="$asn!='1'">
		      <p align="left">
			<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.login_descibe_line_2')"/><br/>
		      </p>
		      <xsl:if test="$err='no-permission'">
			<p align="left"><font color='red'><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.no_permission')"/></font></p>
		      </xsl:if>
		    </xsl:if>
		    
		    <xsl:if test="count(/page/pageResponse/authenticationNode/service/userNodeList) = 1">
		      <xsl:call-template name="listusers" >
		      </xsl:call-template>
		    </xsl:if>
		    
		    <xsl:if test="$info='adduser_interface'">
		      <h2 align="left"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.add_a_new_user_title')"/></h2>
		      <xsl:if test="$err='pw-err'">
			<p align="left"><font color="red"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.password_err')"/></font></p>
		      </xsl:if>	
		      <xsl:if test="$err='un-err'">
			<p align="left"><font color="red"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.unsername_err')"/></font></p>
		      </xsl:if>	
		      <xsl:if test="$err='un-exist'">
			<p align="left"><font color='red'><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.user_has_existed')"/></font></p>
		      </xsl:if>
		      <xsl:call-template name="edituser" >
			<xsl:with-param name="sub_action" select="$sub_action"/>
			<xsl:with-param name="authen_service" select="$authen_service"/>
			<xsl:with-param name="pro_action" select="$pro_action"/>
			<xsl:with-param name="umun_s" />
			<xsl:with-param name="umpw_s" />
			<xsl:with-param name="umas_s" />
			<xsl:with-param name="umgp_s" />
			<xsl:with-param name="umc_s" />
			<xsl:with-param name="oumun_s" />
			<xsl:with-param name="un_s" select="$un_s"/>
			<xsl:with-param name="pw_s" select="$pw_s"/>
			<xsl:with-param name="asn" select="$asn"/>
		      </xsl:call-template>

		    </xsl:if> 
		  </xsl:if>
		  
		  <!-- modify the passwrod of a user-->
		  <xsl:if test="$pro_action='ModifyPassword'">
		    
		    <xsl:if test="$err!='' and $err!='umpw1-err' and $err!='umpw2-err' and $err!='pw-umpw-nm-err' and $err!='umpw-null-err' and $err!='umpw1-umpw2-nm-err' and $err!='umpw1-umpw2-null-err'"><p align="left"><font color='red'><xsl:value-of select="$err" /></font></p>
		    </xsl:if>

		    <xsl:if test="$err=''">
		      <h2 align="left"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.change_password_title')"/></h2>
		      <p align="left"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.change_password_successed_content')"/></p>
		    </xsl:if>
		    
		    <xsl:if test="$asn!='0' and $info='modify_interface'">
		      <h2 align="left"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.change_password_title')"/></h2>
		      <p align="left"><font color="grey"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.change_password_describe_line_1')"/></font></p>

		      <xsl:if test="$err='umpw1-err'">
			<p align="left"><font color="red"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.new_password_invalid')"/></font></p>
		      </xsl:if>
		      <xsl:if test="$err='umpw2-err'">
			<p align="left"><font color="red"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.retyped_password_invalid')"/></font></p>
		      </xsl:if>
		      <xsl:if test="$err='pw-umpw-nm-err' or $err='umpw-null-err'">
			<p align="left"><font color="red"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.old_password_warning')"/></font></p>
		      </xsl:if>  
		      <xsl:if test="$err='umpw1-umpw2-nm-err'">
			<p align="left"><font color="red"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.two_password_not_match')"/></font></p>
		      </xsl:if> 
		      <xsl:if test="$err='umpw1-umpw2-null-err'">
			<p align="left"><font color="red"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.password_empty_warning')"/></font></p>
		      </xsl:if> 
		      
		      <xsl:call-template name="modifypassword">
			<xsl:with-param name="sub_action" select="$sub_action"/>
			<xsl:with-param name="authen_service" select="$authen_service"/>
			<xsl:with-param name="pro_action" select="$pro_action"/>
			<xsl:with-param name="un_s" select="$un_s"/>
			<xsl:with-param name="pw_s" select="$pw_s"/>
			<xsl:with-param name="asn" select="$asn"/>
		      </xsl:call-template>
		    </xsl:if>
		  </xsl:if> 

		  <!-- delete a user -->
		  <xsl:if test="$pro_action='DeleteUser'"> 
		    <xsl:if test="$err!=''">
		      <p align="left"><font color='red'><xsl:value-of select="$err" /></font></p>
		    </xsl:if>

		    <xsl:if test="$info='confirm'">
		      <xsl:call-template name="deleteuser"> 
			<xsl:with-param name="sub_action" select="$sub_action"/>
			<xsl:with-param name="authen_service" select="$authen_service"/>
			<xsl:with-param name="pro_action" select="$pro_action"/>
			<xsl:with-param name="un_s" select="$un_s"/>
			<xsl:with-param name="umun_s" select="/page/pageResponse/authenticationNode/service/@umun"/>	
			<xsl:with-param name="asn" select="$asn"/>
		      </xsl:call-template>
		    </xsl:if>
		    <xsl:if test="count(/page/pageResponse/authenticationNode/service/userNodeList) = 1">
		      <xsl:call-template name="listusers" >
		      </xsl:call-template>
		    </xsl:if>
		  </xsl:if>
		  
		  <!-- edit the user's info -->
		  <xsl:if test="$pro_action='EditUser'"> 
		    <xsl:if test="$err!='' and $err!='umpw-err' and $err!='umun-err' and $err!='ini-umpw-err' and $err!='un-exist'">
		      <p align="left"><font color='red'><xsl:value-of select="$err" /></font></p>
		    </xsl:if>

		    <xsl:if test="$info='edituser-interface'"> 
		      <h2 align="left"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.edit_user_information')"/></h2>
		      <xsl:if test="$err='umpw-err'"> 
			<p align="left"><font color="red"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.password_err')"/></font></p>
		      </xsl:if>
		      <xsl:if test="$err='umun-err'"> 
			<p align="left"><font color="red"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.unsername_err')"/></font></p>
		      </xsl:if>
		      <xsl:if test="$err='ini-umpw-err'"> 
			<p align="left"><font color="red"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.password_initial')"/></font></p>
		      </xsl:if>
		      <xsl:if test="$err='un-exist'">
			<p align="left"><font color='red'><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.user_has_existed')"/></font></p>
		      </xsl:if>
		      <xsl:call-template name="edituser" >
			<xsl:with-param name="sub_action" select="$sub_action"/>
			<xsl:with-param name="authen_service" select="$authen_service"/>
			<xsl:with-param name="pro_action" select="$pro_action"/>
			<xsl:with-param name="umun_s" select="/page/pageResponse/authenticationNode/service/@umun"/>
			<xsl:with-param name="umpw_s" select="/page/pageResponse/authenticationNode/service/@umpw"/>
			<xsl:with-param name="umas_s" select="/page/pageResponse/authenticationNode/service/@umas"/>
			<xsl:with-param name="umgp_s" select="/page/pageResponse/authenticationNode/service/@umgp"/>
			<xsl:with-param name="umc_s" select="/page/pageResponse/authenticationNode/service/@umc"/>
			<xsl:with-param name="oumun_s" select="/page/pageResponse/authenticationNode/service/@oumun"/>
			<xsl:with-param name="un_s" select="$un_s"/>
			<xsl:with-param name="pw_s" select="$pw_s"/>
			<xsl:with-param name="asn" select="$asn"/>
		      </xsl:call-template>
		    </xsl:if> 
		    <xsl:if test="$info='all-un'">
		      <xsl:if test="count(/page/pageResponse/authenticationNode/service/userNodeList) = 1">
			<xsl:call-template name="listusers" >
			</xsl:call-template>
		      </xsl:if>
		    </xsl:if>
		  </xsl:if>
		  
		</div>
	      </td>
	    </tr>
	  </table>
	  <div id="navbar">	  
	    <xsl:call-template name="greenstoneFooter" />
	  </div>	  
	</div>
      </body>
    </html>
  </xsl:template>
  
  <!--template name="login"-->
  <xsl:template name="login">
    <xsl:param name="sub_action"/>
    <xsl:param name="authen_service"/>
    <xsl:param name="pro_action"/>
    <div>
      <form id="LoginForm" method="get" action="{$library_name}">
	<input type='hidden' name='a' value='g'/>
	<input type='hidden' name='sa' value='{$sub_action}'/>
	<input type='hidden' name='s' value='{$authen_service}'/>
	<input type='hidden' name='rt' value='r'/>
	<input type='hidden' name='s1.asn' value='0'/>
	<input type='hidden' name='s1.uan' value='1'/>
	<input type='hidden' name='s1.aup' value='{$pro_action}'/>
	<input type='hidden' name='s1.umpw1' value=''/>
	<input type='hidden' name='s1.umpw2' value=''/>
	<input type='hidden' name='s1.umc' value=''/>
	<input type='hidden' name='s1.umgp' value=''/>
	<input type='hidden' name='s1.umun' value=''/>
	<input type='hidden' name='s1.umpw' value=''/>
	<input type='hidden' name='s1.umas' value=''/>

	<table align="center">
	  <tr>
	    <td><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.username')"/></td><td><input type="string" name="s1.un" size="10" value=""/></td><td></td>
	  </tr>
	  <tr>
	    <td><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.password')"/></td><td><input type="password" name="s1.pw" size="10" value=""/></td><td>
	      <input type="submit" value="submit"></input></td>
	  </tr>
	</table>
      </form>
    </div>
  </xsl:template>

  <!--template name="listusers"-->
  <xsl:template name="listusers" >
    <h2 align="left"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.list_of_current_users_title')"/></h2>

    <table id='mainTable' align="left" border="0" cellspacing="1" cellpadding="3" width="100%" >
      <tr>
	<th bgcolor="#d0d0d0"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.username')"/></th>
	<th bgcolor="#d0d0d0"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.account_status')"/></th>
	<th bgcolor="#d0d0d0"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.groups')"/></th>
	<th bgcolor="#d0d0d0"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.comment')"/></th>
	<th bgcolor="#d0d0d0"></th>
      </tr>
      <xsl:for-each select="/page/pageResponse/authenticationNode/service/userNodeList/userNode">
	<script type="text/javascript">
	<xsl:text disable-output-escaping="yes">
	var username="</xsl:text><xsl:value-of select="@umun"/><xsl:text disable-output-escaping="yes">";
	</xsl:text>
	</script>
	<tr>
	  <td bgcolor="#eeeeee"><xsl:value-of select="@umun"/></td>
	  <!--<td id="status" bgcolor="#eeeeee">-->
	  <td bgcolor="#eeeeee"><xsl:attribute name="id">status<xsl:value-of select="@umun"/></xsl:attribute>
	    <script type="text/javascript">
	      <xsl:text disable-output-escaping="yes">
		var status="</xsl:text><xsl:value-of select="@umas"/><xsl:text disable-output-escaping="yes">";
		if (status=="true"){
		  document.getElementById("status"+username).innerHTML="enabled";
		}
		if (status=="false"){
		  document.getElementById("status"+username).innerHTML="disabled";
		}
	      </xsl:text>		 
	    </script>
	  </td>
	  <!--<td id="group" bgcolor="#eeeeee">-->
	  <td bgcolor="#eeeeee"><xsl:attribute name="id">group<xsl:value-of select="@umun"/></xsl:attribute>
	    <script type="text/javascript">
	      <xsl:text disable-output-escaping="yes">
		var groups="</xsl:text><xsl:value-of select="@umgp"/><xsl:text disable-output-escaping="yes">";
		var split_groups= groups.split(",");
		var new_groups="";
		for (j=0; j &lt; split_groups.length ; j++){
		  new_groups+=split_groups[j]+" &lt;br /&gt; ";
		}
		document.getElementById('group'+username).innerHTML=new_groups;
	      </xsl:text>		 
	    </script>
	  </td>
	  <td bgcolor="#eeeeee"><xsl:value-of select="@umc"/></td>
	  <td bgcolor="#eeeeee">
	    <form name="ListUsersForm" method="get" action="{$library_name}">
	      <input type='hidden' name='a' value='g'/>
	      <input type='hidden' name='sa' value='authen'/>
	      <input type='hidden' name='s' value='Authentication'/>
	      <input type='hidden' name='rt' value='r'/>
	      <input type='hidden' name='s1.asn' value='1'/>
	      <input type='hidden' name='s1.uan' value='1'/>

	      <input type='hidden'><xsl:attribute name="name">s1.oumun</xsl:attribute><xsl:attribute name="value"><xsl:value-of select="@umun"/></xsl:attribute></input>
	      <input type='hidden'><xsl:attribute name="name">s1.umun</xsl:attribute><xsl:attribute name="value"><xsl:value-of select="@umun"/></xsl:attribute></input>
	      <input type='hidden' name='s1.cm' value=''/>

	      <input type="submit"><xsl:attribute name="name">s1.aup</xsl:attribute><xsl:attribute name="value">EditUser</xsl:attribute></input>
	      <input type="submit"><xsl:attribute name="name">s1.aup</xsl:attribute><xsl:attribute name="value">DeleteUser</xsl:attribute></input>
	    </form>
	  </td>
	</tr>
      </xsl:for-each>
    </table>
  </xsl:template>

  <!--template name="modifypassword"-->
  <xsl:template name="modifypassword" >
    <xsl:param name="sub_action"/>
    <xsl:param name="authen_service"/>
    <xsl:param name="pro_action"/>
    <xsl:param name="un_s"/>
    <xsl:param name="pw_s"/>
    <xsl:param name="asn"/>
    <div>
      <form id="modifyForm" method="get" action="{$library_name}">
	<input type='hidden' name='a' value='g'/>
	<input type='hidden' name='sa' value='{$sub_action}'/>
	<input type='hidden' name='s' value='{$authen_service}'/>
	<input type='hidden' name='rt' value='r'/>
	<input type='hidden' name='s1.un' value='{$un_s}'/>
	<input type='hidden' name='s1.pw' value='{$pw_s}'/>
	<input type='hidden' name='s1.asn' value='{$asn}'/>
	<input type='hidden' name='s1.uan' value='1'/>
	<input type='hidden' name='s1.aup' value='{$pro_action}'/>
	
	<table align="center"><tr><td align="right">
	      <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.old_password')"/></td><td><input type="password" name="s1.umpw" size="10" value=""/></td></tr>
	  <tr><td align="right">
	      <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.new_password')"/></td><td><input type="password" name="s1.umpw1" size="10" value=""/></td></tr>
	  <tr><td align="right">
	      <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.retype_new_password')"/></td><td><input type="password" name="s1.umpw2" size="10" value=""/></td></tr>
	  <tr><td></td><td>
	      <input type="submit" value="submit"></input></td>
	  </tr>
	</table>
      </form>
    </div>
  </xsl:template>

  <!--template name="deleteuser"-->
  <xsl:template name="deleteuser">
    <xsl:param name="sub_action"/>
    <xsl:param name="authen_service"/>
    <xsl:param name="pro_action"/>
    <xsl:param name="un_s"/>
    <xsl:param name="umun_s"/>
    <xsl:param name="asn"/>
    <div >
      <form id="deleteForm" method="get" action="{$library_name}">
	<input type='hidden' name='a' value='g'/>
	<input type='hidden' name='sa' value='{$sub_action}'/>
	<input type='hidden' name='s' value='{$authen_service}'/>
	<input type='hidden' name='rt' value='r'/>
	<input type='hidden' name='s1.aup' value='{$pro_action}'/>
	<input type='hidden' name='s1.asn' value='{$asn}'/>
	<input type='hidden' name='s1.uan' value='1'/>
	<input type='hidden' name='s1.umun' value='{$umun_s}'/>
	<p><font color='red'><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.do_you_really_want_to_permanently_remove_user')"/> <xsl:value-of select="$umun_s"/>" ?</font></p>
	<input type="submit" value="yes"><xsl:attribute name="name">s1.cm</xsl:attribute></input>
	<input type="submit" value="no"><xsl:attribute name="name">s1.cm</xsl:attribute></input>
      </form>
    </div>
  </xsl:template>
  
  <!--template name="edituser"-->
  <xsl:template name="edituser" >
    <xsl:param name="sub_action"/>
    <xsl:param name="authen_service"/>
    <xsl:param name="pro_action"/>
    <xsl:param name="un_s"/>
    <xsl:param name="pw_s"/>
    <xsl:param name="umun_s"/>
    <xsl:param name="umpw_s"/>
    <xsl:param name="umas_s"/>
    <xsl:param name="umgp_s"/>
    <xsl:param name="umc_s"/>
    <xsl:param name="oumun_s"/>	
    <xsl:param name="asn"/>
    <div>
      <form id="editForm" method="get" action="{$library_name}">
	<input type='hidden' name='a' value='g'/>
	<input type='hidden' name='sa' value='{$sub_action}'/>
	<input type='hidden' name='s' value='{$authen_service}'/>
	<input type='hidden' name='rt' value='r'/>
	<input type='hidden' name='s1.asn' value='{$asn}'/>
	<input type='hidden' name='s1.uan' value='1'/>
	<input type='hidden' name='s1.aup' value='{$pro_action}'/>
	<input type='hidden' name='s1.un' value='{$un_s}'/>
	<input type='hidden' name='s1.pw' value='{$pw_s}'/>
	
	<table align="left" ><tr><td>
	      <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.username')"/></td><td><input type="text" name="s1.umun" size="15" value="{$umun_s}"/></td><td><font color="gray"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.username_describe')"/></font></td></tr>
	  <tr><td>
	      <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.password')"/></td><td><input type="password" name="s1.umpw" size="15" value="{$umpw_s}"/></td><td><font color="gray"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.password_describe')"/></font></td></tr>
	  <tr><td>
	      <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.account_status')"/></td><td>	
	      <select name="s1.umas">
		<xsl:if test="$umas_s=''">
		  <option value="true" selected="selected"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.enabled')"/></option>
		  <option value="false"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.disabled')"/></option>
		</xsl:if> 
		<xsl:if test="$umas_s='true'">
		  <option value="true" selected="selected"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.enabled')"/></option>
		  <option value="false"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.disabled')"/></option>
		</xsl:if> 
		<xsl:if test="$umas_s='false'">
		  <option value="true"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.enabled')"/></option>
		  <option value="false" selected="selected"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.disabled')"/></option>
		</xsl:if> 
	      </select></td><td></td></tr>
	  <tr><td>
	      <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.groups')"/></td><td><input type="text" id="group" name="s1.umgp" size="70" value="{$umgp_s}"/></td><td><font color="gray"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.groups_describe')"/></font></td></tr>
	  <tr>
	    <td></td>
	    <td  align="right"> <select id="groups" size="0">
		<script type="text/javascript">
		  <xsl:text disable-output-escaping="yes">
		    var group_arr=Array("administrator","all-collections-editor","personal-collections-editor");
		    var group_string=document.getElementById("group").value;
		    var split_group_string=group_string.split(",");
		    var find=0;
		    for (i=0; i &lt; group_arr.length; i++){
		      find=0;
		      for (j=0; j &lt; group_arr.length; j++){
		        if (split_group_string[j]==group_arr[i]){
		          find=1;
		        }
		      }
		      if (find==0){
		        op = document.createElement('option');
		        op.innerHTML = group_arr[i];
		        op.setAttribute("value", group_arr[i]);
		        document.getElementById("groups").appendChild(op);
		      }
	            }
		    
		  </xsl:text>		 
		</script>
	      </select></td><td><input type="button" name="addGroupButton" value="add" onClick="addGroup('groups')"/></td>
	  </tr>
	  <tr>
	    <td></td>
	    <td  align="right"> 
	      <select  id="collects" size="0">
		<xsl:for-each select="/page/pageResponse/collectionList/collection">
		  <script type="text/javascript">
		    <xsl:text disable-output-escaping="yes">
		      var group="</xsl:text><xsl:value-of select="@name"/>-collection-editor<xsl:text disable-output-escaping="yes">";
		      var group_string=document.getElementById("group").value;
		      var split_group_string=group_string.split(",");
		      var find=0;
		      for (i=0; i &lt; split_group_string.length; i++){
		        if (split_group_string[i]==group){
		          find=1;
		        }
		      }
		      if (find==0){
		        op = document.createElement('option');
		        op.innerHTML = group;
		        op.setAttribute("value", group);
		        document.getElementById("collects").appendChild(op);
		      }
		    </xsl:text>		 
		  </script>
		</xsl:for-each>
	      </select>
	    </td>
	    <td><input type="button" name="addGroupButton" value="add" onClick="addGroup('collects')"/></td>
	  </tr>
	  <tr><td>
	      <xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.comment')"/></td><td colspan="2"><div><textarea cols="40" rows="5" name="s1.umc"><xsl:value-of select="$umc_s"/>

		</textarea></div></td></tr> 
	  <tr>

	    <td></td><td>
	      <input type="submit" value="submit"><xsl:attribute name="name">s1.cm</xsl:attribute></input>
	      <xsl:if test="$pro_action!='AddUser'">
		<input type="submit" value="cancel"><xsl:attribute name="name">s1.cm</xsl:attribute></input>
	      </xsl:if>
	    </td><td></td></tr>
	</table>
	<input type='hidden' name='s1.oumun' value='{$oumun_s}'/>
      </form>
    </div>
  </xsl:template>

  <!-- addGroup() javascript -->
  <xsl:template name="addGroup">
    <script type="text/javascript">
      <xsl:text disable-output-escaping="yes">
	function addGroup(g){
	  var itemSelected;
	  var splitGroup;
	  var groupExit;

	  if (document.getElementById("editForm")!=null){
	    //itemSelected=document.getElementById("editForm").groups.options.selectedIndex;
	    itemSelected=document.getElementById(g).options.selectedIndex;
	    if (document.getElementById(g).options[itemSelected].text!=""){
	      if (document.getElementById("group").value==""){
	        document.getElementById("group").value=document.getElementById("group").value+document.getElementById(g).options[itemSelected].text;
	      }else{
	        splitGroup=document.getElementById("group").value.split(",");
	        for (var i=0; i &lt; splitGroup.length; i++){
	          if (splitGroup[i]==document.getElementById(g).options[itemSelected].text){
	            alert(document.getElementById(g).options[itemSelected].text + " has been added.");
	            groupExit=1;
	            break;
	          }
	        }
	        if (groupExit!=1) {
	          document.getElementById("group").value=document.getElementById("group").value+","+document.getElementById(g).options[itemSelected].text;
	        }
	      }
	      document.getElementById(g).options[itemSelected]=new Option("",itemSelected);
	    }
	  }
	}
      </xsl:text>
    </script>    
  </xsl:template>
  
</xsl:stylesheet>  

