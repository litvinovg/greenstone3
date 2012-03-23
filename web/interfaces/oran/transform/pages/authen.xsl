<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:gslib="http://www.greenstone.org/skinning"
	xmlns:util="xalan://org.greenstone.gsdl3.util.XSLTUtil"
	extension-element-prefixes="java util"
	exclude-result-prefixes="java util">
	
	<!-- use the 'main' layout -->
	<xsl:import href="layouts/main.xsl"/>
	<xsl:output method="html"/>
	
	<!-- set page title -->
	<xsl:template name="pageTitle">
		<xsl:choose>
			<xsl:when test="/page/pageResponse/authenticationNode/service/@operation = 'AddUser'">
				<xsl:text>Add a new user</xsl:text>
			</xsl:when>
			<xsl:when test="/page/pageResponse/authenticationNode/service/@operation = 'Register'">
				<xsl:text>Register</xsl:text>
			</xsl:when>
			<xsl:when test="/page/pageResponse/authenticationNode/service/@operation = 'EditUser'">
				<xsl:text>Edit user</xsl:text>
			</xsl:when>
			<xsl:when test="/page/pageResponse/authenticationNode/service/@operation = 'AccountSettings'">
				<xsl:text>Account settings</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.authentication')"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- set page breadcrumbs -->
	<xsl:template name="breadcrumbs">
		<gslib:siteLink/><gslib:rightArrow/><a href="{$library_name}/admin/ListUsers"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.authentication')"/></a>
	</xsl:template>
	
	<xsl:template match="/page">
		<xsl:if test="/page/pageResponse/error">
			<div id="gs_error" class="ui-state-error ui-corner-all">
				<span class="ui-icon ui-icon-alert" style="float: left;"><xsl:text> </xsl:text></span><xsl:value-of select="/page/pageResponse/error"/>
			</div>
			<br/>
		</xsl:if>
		<div id="main">
			<xsl:choose>
				<!-- ********** -->
				<!-- LOGIN PAGE -->
				<!-- ********** -->
				<xsl:when test="/page/pageResponse/authenticationNode/service/@operation = 'Login'">
					<form method="POST" action="{$library_name}/admin/{/page/pageRequest/paramList/param[@name = 's1.authpage']/@value}">
						<table id="loginTable">
							<tr><td>Username: </td><td><input type="text" name="username"/></td></tr>
							<tr><td>Password: </td><td><input type="password" name="password"/></td></tr>
							<tr><td><input type="submit" value="Login"/></td><td><xsl:text> </xsl:text></td></tr>
						</table>
					</form>
				</xsl:when>

				<!-- *************** -->
				<!-- LIST USERS PAGE -->
				<!-- *************** -->
				<xsl:when test="/page/pageResponse/authenticationNode/service/@operation = 'ListUsers'">
					<table id="userListTable">
						<tr class="ui-widget-header">
							<td colspan="7"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.list_of_current_users_title')"/></td>
						</tr>
						<tr class="ui-state-default">
							<td><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.username')"/></td>
							<td><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.account_status')"/></td>
							<td><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.groups')"/></td>
							<td><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.comment')"/></td>
							<td><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.email')"/></td>
							<td><xsl:text> </xsl:text></td>
							<td><xsl:text> </xsl:text></td>
						</tr>
						<xsl:for-each select="/page/pageResponse/authenticationNode/service/userNodeList/userNode">
							<tr class="ui-widget-content">
								<td><xsl:value-of select="@username"/></td>
								<td>
									<xsl:choose>
										<xsl:when test="@status = 'true'">
											<xsl:text>enabled</xsl:text>
										</xsl:when>
										<xsl:otherwise>
											<xsl:text>disabled</xsl:text>
										</xsl:otherwise>
									</xsl:choose>
								</td>
								<td><xsl:value-of select='util:replace(@groups, ",", " ")'/></td>
								<td><xsl:value-of select="@comment"/></td>
								<td><xsl:value-of select="@email"/></td>
								<td><a href="{$library_name}/admin/EditUser?s1.username={@username}"><button>Edit</button></a></td>
								<td><a href="{$library_name}/admin/PerformDeleteUser?s1.username={@username}"><button>Delete</button></a></td>
							</tr>
						</xsl:for-each>
					</table>
					<div id="auth_navbar">
						<!-- Add User link-->
						<span class="ui-state-default ui-corner-all">
							<a href="{$library_name}/admin/AddUser"><xsl:value-of select="util:getInterfaceText($interface_name, /page/@lang, 'authen.add_a_new_user')"/></a>
						</span>
					</div>
				</xsl:when>

				<!-- ********************************* -->
				<!-- EDIT USER / ACCOUNT SETTINGS PAGE -->
				<!-- ********************************* -->
				<xsl:when test="/page/pageResponse/authenticationNode/service/@operation = 'EditUser' or /page/pageResponse/authenticationNode/service/@operation = 'AccountSettings'">
					<form method="POST">
						<xsl:attribute name="action">
							<xsl:choose>
								<xsl:when test="/page/pageResponse/authenticationNode/service/@operation = 'EditUser'">
									<xsl:value-of select="$library_name"/><xsl:text>/admin/PerformEdit</xsl:text>
								</xsl:when>
								<xsl:when test="/page/pageResponse/authenticationNode/service/@operation = 'AccountSettings'">
									<xsl:value-of select="$library_name"/><xsl:text>/admin/PerformAccEdit</xsl:text>
								</xsl:when>
							</xsl:choose>
						</xsl:attribute>
						<input type="hidden" name="s1.prevUsername" value="{/page/pageResponse/authenticationNode/service/userNodeList/userNode/@username}"/>
						<table>
							<tr>
								<td><xsl:text>Username: </xsl:text></td>
								<td>
									<input type="text" name="s1.newUsername" value="{/page/pageResponse/authenticationNode/service/userNodeList/userNode/@username}"/>
								</td>
							</tr>
							<xsl:choose>
								<xsl:when test="/page/pageResponse/authenticationNode/service/@operation = 'EditUser'">
									<tr>
										<td id="passwordLabelCell">
											<button id="changePasswordButton">Change Password...</button>
											<script type="text/javascript">
												<xsl:text disable-output-escaping="yes">
													var changePasswordButton = document.getElementById("changePasswordButton");
													changePasswordButton.onclick = function()
													{
														changePasswordButton.parentNode.removeChild(changePasswordButton);
														
														var passwordLabel = document.createTextNode("Password: ");
														document.getElementById("passwordLabelCell").appendChild(passwordLabel);
														
														var passwordInput = document.createElement("INPUT");
														passwordInput.setAttribute("name", "s1.password");
														passwordInput.setAttribute("type", "password");
														document.getElementById("passwordInputCell").appendChild(passwordInput);
													}
												</xsl:text>
											</script>
										</td>
										<td id="passwordInputCell">
											<xsl:text> </xsl:text>
										</td>
									</tr>
								</xsl:when>
								<xsl:when test="/page/pageResponse/authenticationNode/service/@operation = 'AccountSettings'">
									<tr>
										<td><button id="changePasswordButton">Change Password...</button></td>
										<script type="text/javascript">
											<xsl:text disable-output-escaping="yes">
												var changePasswordButton = document.getElementById("changePasswordButton");
												changePasswordButton.onclick = function()
												{
													changePasswordButton.parentNode.removeChild(changePasswordButton);
													
													var names = ["s1.oldPassword", "s1.newPassword", ""];
													var ids = ["oldPassword", "passwordOne", "passwordTwo"];
													var labels = ["Old password: ", "New password: ", "Retype new password: "];
													var parents = ["oldPassword", "password", "retypePassword"];
													
													for(var i = 0; i &lt; names.length; i++)
													{
														$("#" + parents[i] + "LabelCell").append(labels[i]);
														
														var input = $("&lt;input&gt;");
														input.attr("name", names[i]);
														input.attr("type", "password");
														input.attr("id", ids[i]);
														
														$("#" + parents[i] + "InputCell").append(input);
													}
												}
											</xsl:text>
										</script>
									</tr>
									<tr>
										<td id="oldPasswordLabelCell"><xsl:text> </xsl:text></td>
										<td id="oldPasswordInputCell"><xsl:text> </xsl:text></td>
									</tr>
									<tr>
										<td id="passwordLabelCell"><xsl:text> </xsl:text></td>
										<td id="passwordInputCell"><xsl:text> </xsl:text></td>
									</tr>
									<tr>
										<td id="retypePasswordLabelCell"><xsl:text> </xsl:text></td>
										<td id="retypePasswordInputCell"><xsl:text> </xsl:text></td>
									</tr>
								</xsl:when>
							</xsl:choose>						
							<tr>
								<td><xsl:text>Email address: </xsl:text></td>
								<td>
									<input type="text" name="s1.newEmail" value="{/page/pageResponse/authenticationNode/service/userNodeList/userNode/@email}"/>
								</td>
							</tr>
							<xsl:if test="/page/pageResponse/authenticationNode/service/@operation = 'EditUser'">
								<tr>
									<td><xsl:text>Groups: </xsl:text></td>
									<td>
										<input id="groupsInput" size="50" type="text" name="s1.groups" value="{/page/pageResponse/authenticationNode/service/userNodeList/userNode/@groups}"/>
									</td>
									<td>
										<select id="groupSelector">
											<option>administrator</option>
											<option>all-collections-editor</option>
											<option>personal-collections-editor</option>
										</select>
									</td>
									<td>
										<button id="addGroupButton">Add Group</button>
										<script type="text/javascript">
											<xsl:text disable-output-escaping="yes">
												var addGroupButton = document.getElementById("addGroupButton");
												addGroupButton.onclick = function()
												{
													var groupsInput = document.getElementById("groupsInput");
													var groupSelector = document.getElementById("groupSelector");
													var options = groupSelector.getElementsByTagName("OPTION");
													var selectedGroup = options[groupSelector.selectedIndex].innerHTML;
													var currentValue = groupsInput.value;
													
													if(currentValue &amp;&amp; currentValue.search(/\w/) != -1)
													{
														groupsInput.value = currentValue + "," + selectedGroup;
													}
													else
													{
														groupsInput.value = selectedGroup;
													}
													return false;
												}
											</xsl:text>
										</script>
									</td>
								</tr>
								<tr>
									<td><xsl:text>Comment: </xsl:text></td>
									<td>
										<textarea name="s1.comment" style="width:100%;"><xsl:value-of select="/page/pageResponse/authenticationNode/service/userNodeList/userNode/@comment"/></textarea>
									</td>
								</tr>
								<tr>
									<td><xsl:text>Status: </xsl:text></td>
									<td>
										<input type="radio" name="s1.status" value="true">
											<xsl:if test="/page/pageResponse/authenticationNode/service/userNodeList/userNode/@status = 'true'">
												<xsl:attribute name="checked">true</xsl:attribute>
											</xsl:if>
										</input>
										<xsl:text>Enabled </xsl:text>
										
										<input type="radio" name="s1.status" value="false">
											<xsl:if test="/page/pageResponse/authenticationNode/service/userNodeList/userNode/@status = 'false'">
												<xsl:attribute name="checked">true</xsl:attribute>
											</xsl:if>
										</input>
										<xsl:text>Disabled </xsl:text>
									</td>
								</tr>
							</xsl:if>
							<tr>
								<td>
									<input id="submitButton" type="submit" value="Submit"/>
									<xsl:if test="/page/pageResponse/authenticationNode/service/@operation = 'AccountSettings'">
										<script type="text/javascript">
											<xsl:text disable-output-escaping="yes">
												$("#submitButton").click(function()
												{
													var passwordOne = $("#passwordOne");
													var passwordTwo = $("#passwordTwo");
													
													if(passwordOne.val() != passwordTwo.val())
													{
														alert("The passwords you have entered do not match");
														return false;
													}
													else
													{
														return true;
													}
												});
											</xsl:text>
										</script>
									</xsl:if>
								</td>
							</tr>
							<tr>
								<td style="font-size:0.6em;"><a href="{$library_name}/admin/PerformResetPassword?s1.username={/page/pageResponse/authenticationNode/service/userNodeList/userNode/@username}"><br/>Reset Password...</a></td>
							</tr>
						</table>
					</form>
				</xsl:when>
				
				<!-- ************************ -->
				<!-- ADD USER / REGISTER PAGE -->
				<!-- ************************ -->
				<xsl:when test="/page/pageResponse/authenticationNode/service/@operation = 'AddUser' or /page/pageResponse/authenticationNode/service/@operation = 'Register'">
					<form method="POST">
						<xsl:attribute name="action">
							<xsl:choose>
								<xsl:when test="/page/pageResponse/authenticationNode/service/@operation = 'AddUser'">
									<xsl:value-of select="$library_name"/><xsl:text>/admin/PerformAdd</xsl:text>
								</xsl:when>
								<xsl:when test="/page/pageResponse/authenticationNode/service/@operation = 'Register'">
									<xsl:value-of select="$library_name"/><xsl:text>/admin/PerformRegister</xsl:text>
								</xsl:when>
							</xsl:choose>
						</xsl:attribute>
						<table>
							<tr>
								<td><xsl:text>Username: </xsl:text></td>
								<td>
									<input type="text" name="s1.username"/>
								</td>
							</tr>
							<tr>
								<td><xsl:text>Password: </xsl:text></td>
								<td>
									<input id="passwordOne" type="password" name="s1.password"/>
								</td>
							</tr>
							<tr>
								<td><xsl:text>Retype password: </xsl:text></td>
								<td>
									<input id="passwordTwo" type="password"/>
								</td>
							</tr>
							<tr>
								<td><xsl:text>Email address: </xsl:text></td>
								<td>
									<input type="text" name="s1.email"/>
								</td>
							</tr>
							<xsl:if test="/page/pageResponse/authenticationNode/service/@operation = 'AddUser'">
								<tr>
									<td><xsl:text>Groups: </xsl:text></td>
									<td>
										<input id="groupsInput" size="50" type="text" name="s1.groups"/>
									</td>
									<td>
										<select id="groupSelector">
											<option>administrator</option>
											<option>all-collections-editor</option>
											<option>personal-collections-editor</option>
										</select>
									</td>
									<td>
										<button id="addGroupButton">Add Group</button>
										<script type="text/javascript">
											<xsl:text disable-output-escaping="yes">
												var addGroupButton = document.getElementById("addGroupButton");
												addGroupButton.onclick = function()
												{
													var groupsInput = document.getElementById("groupsInput");
													var groupSelector = document.getElementById("groupSelector");
													var options = groupSelector.getElementsByTagName("OPTION");
													var selectedGroup = options[groupSelector.selectedIndex].innerHTML;
													var currentValue = groupsInput.getAttribute("value");
													
													if(currentValue &amp;&amp; currentValue.search(/\w/) != -1)
													{
														groupsInput.setAttribute("value", currentValue + "," + selectedGroup);
													}
													else
													{
														groupsInput.setAttribute("value", selectedGroup);
													}
													return false;
												}
											</xsl:text>
										</script>
									</td>
								</tr>
								<tr>
									<td><xsl:text>Comment: </xsl:text></td>
									<td>
										<textarea name="s1.comment" style="width:100%;"><xsl:text> </xsl:text></textarea>
									</td>
								</tr>
								<tr>
									<td><xsl:text>Status: </xsl:text></td>
									<td>
										<input type="radio" name="s1.status" value="true" checked="true"/><xsl:text>Enabled </xsl:text>
										<input type="radio" name="s1.status" value="false"/><xsl:text>Disabled </xsl:text>
									</td>
								</tr>
							</xsl:if>
							<xsl:if test="/page/pageResponse/authenticationNode/service/@operation = 'Register'">
								<tr>
									<td colspan="2">
										<xsl:value-of disable-output-escaping="yes" select="util:reCAPTCHAimage()"/>
									</td>
									<script type="text/javascript">
										<xsl:text disable-output-escaping="yes">
											function changeParams()
											{
												$("#recaptcha_challenge_field").attr("name", "s1.recaptcha_challenge_field");
												$("#recaptcha_response_field").attr("name", "s1.recaptcha_response_field");
											}
											$(window).load(changeParams());
										</xsl:text>
									</script>
								</tr>
							</xsl:if>
							<tr>
								<td>
									<input id="submitButton" type="submit" value="Submit"/>
									<script type="text/javascript">
										<xsl:text disable-output-escaping="yes">
											$("#submitButton").click(function()
											{
												var passwordOne = $("#passwordOne");
												var passwordTwo = $("#passwordTwo");
												
												if(passwordOne.val() != passwordTwo.val())
												{
													alert("The passwords you have entered do not match");
													return false;
												}
												else
												{
													return true;
												}
											});
										</xsl:text>
									</script>
								</td>
							</tr>
						</table>
					</form>
				</xsl:when>
				
				<!-- ********************** -->
				<!-- REGISTER RESPONSE PAGE -->
				<!-- ********************** -->
				<xsl:when test="/page/pageResponse/authenticationNode/service/@operation = 'PerformRegister'">
					<xsl:choose>
						<xsl:when test="/page/pageResponse/error">
							<xsl:text>Error: Failed to add user</xsl:text>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text>User added successfully</xsl:text>
							<meta http-equiv="refresh" content="2; url={/page/pageRequest/@baseURL}{$library_name}"/> 
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				
				<xsl:otherwise>
					<xsl:text>Page not found.</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</div>
	</xsl:template>
	
</xsl:stylesheet>