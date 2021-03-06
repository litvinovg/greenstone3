package org.greenstone.gsdl3.service;

import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.Pattern;

import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.greenstone.gsdl3.util.DerbyWrapper;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.UserQueryResult;
import org.greenstone.gsdl3.util.UserTermInfo;
import org.greenstone.gsdl3.util.XMLConverter;
import org.greenstone.util.GlobalProperties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Authentication extends ServiceRack
{
	//Some useful constants
	protected static final int USERNAME_MIN_LENGTH = 2;
	protected static final int USERNAME_MAX_LENGTH = 30;
	protected static final int PASSWORD_MIN_LENGTH = 3;
	protected static final int PASSWORD_MAX_LENGTH = 64;

	//Error codes
	protected static final int NO_ERROR = 0;
	protected static final int ERROR_REQUEST_HAS_NO_PARAM_LIST = -1;
	protected static final int ERROR_NOT_LOGGED_IN = -2;
	protected static final int ERROR_ADMIN_NOT_LOGGED_IN = -3;
	protected static final int ERROR_COULD_NOT_GET_USER_INFO = -4;
	protected static final int ERROR_USERNAME_NOT_SPECIFIED = -5;
	protected static final int ERROR_REQUESTED_USER_NOT_FOUND = -6;
	protected static final int ERROR_SQL_EXCEPTION = -7;
	protected static final int ERROR_INVALID_USERNAME = -8;
	protected static final int ERROR_PASSWORD_NOT_ENTERED = -9;
	protected static final int ERROR_PASSWORD_TOO_SHORT = -10;
	protected static final int ERROR_PASSWORD_TOO_LONG = -11;
	protected static final int ERROR_PASSWORD_USES_ILLEGAL_CHARACTERS = -12;
	protected static final int ERROR_INCORRECT_PASSWORD = -13;
	protected static final int ERROR_USER_ALREADY_EXISTS = -14;
	protected static final int ERROR_ADDING_USER = -15;
	protected static final int ERROR_REMOVING_USER = -16;
	protected static final int ERROR_CAPTCHA_DOES_NOT_MATCH = -17;
	protected static final int ERROR_CAPTCHA_MISSING = -18;
	protected static final int ERROR_NOT_AUTHORISED = -19;

	protected static final HashMap<Integer, String> _errorMessageMap;
	static
	{
		//Corresponding error messages
		HashMap<Integer, String> errorMessageMap = new HashMap<Integer, String>();
		errorMessageMap.put(ERROR_REQUEST_HAS_NO_PARAM_LIST, "The list of parameters for this request was empty.");
		errorMessageMap.put(ERROR_NOT_LOGGED_IN, "You must be logged in to access this page.");
		errorMessageMap.put(ERROR_ADMIN_NOT_LOGGED_IN, "You must be logged in as an administrator to access this page.");
		errorMessageMap.put(ERROR_COULD_NOT_GET_USER_INFO, "There was a error getting the user information.");
		errorMessageMap.put(ERROR_USERNAME_NOT_SPECIFIED, "No username was specified.");
		errorMessageMap.put(ERROR_REQUESTED_USER_NOT_FOUND, "The requested user was not found in the database.");
		errorMessageMap.put(ERROR_SQL_EXCEPTION, "There was an SQL exception while accessing the database.");
		errorMessageMap.put(ERROR_INVALID_USERNAME, "The username specified was invalid.");
		errorMessageMap.put(ERROR_PASSWORD_NOT_ENTERED, "No password was entered.");
		errorMessageMap.put(ERROR_PASSWORD_TOO_SHORT, "The password you entered was too short (minimum of 3 characters).");
		errorMessageMap.put(ERROR_PASSWORD_TOO_LONG, "The password you entered was too long (maximum of 64 characters).");
		errorMessageMap.put(ERROR_PASSWORD_USES_ILLEGAL_CHARACTERS, "The password you entered contains illegal characters.");
		errorMessageMap.put(ERROR_INCORRECT_PASSWORD, "The password specified was incorrect.");
		errorMessageMap.put(ERROR_USER_ALREADY_EXISTS, "This user already exists and therefore cannot be added.");
		errorMessageMap.put(ERROR_ADDING_USER, "There was an error adding this user to the database.");
		errorMessageMap.put(ERROR_REMOVING_USER, "There was an error removing this user from the database.");
		errorMessageMap.put(ERROR_CAPTCHA_DOES_NOT_MATCH, "The words you entered did not match the image, please try again.");
		errorMessageMap.put(ERROR_CAPTCHA_MISSING, "The information from the captcha is missing.");
		errorMessageMap.put(ERROR_NOT_AUTHORISED, "You are not authorised to access this page.");

		_errorMessageMap = errorMessageMap;
	}

	//Admin-required operations
	protected static final String LIST_USERS = "ListUsers";
	protected static final String PERFORM_ADD = "PerformAdd";
	protected static final String PERFORM_EDIT = "PerformEdit";
	protected static final String ADD_USER = "AddUser";
	protected static final String EDIT_USER = "EditUser";
	protected static final String PERFORM_DELETE_USER = "PerformDeleteUser";

	protected static final ArrayList<String> _adminOpList;
	static
	{
		ArrayList<String> opList = new ArrayList<String>();
		opList.add(LIST_USERS);
		opList.add(PERFORM_ADD);
		opList.add(PERFORM_EDIT);
		opList.add(EDIT_USER);
		opList.add(PERFORM_DELETE_USER);

		_adminOpList = opList;
	}

	//User-required operations
	protected static final String ACCOUNT_SETTINGS = "AccountSettings";
	protected static final String PERFORM_ACCOUNT_EDIT = "PerformAccEdit";
	protected static final String PERFORM_RESET_PASSWORD = "PerformResetPassword";
	protected static final String PERFORM_CHANGE_PASSWORD = "PerformChangePassword";
	protected static final String PERFORM_RETRIEVE_PASSWORD = "PerformRetrievePassword";
	protected static final ArrayList<String> _userOpList;
	static
	{
		ArrayList<String> opList = new ArrayList<String>();
		opList.add(ACCOUNT_SETTINGS);
		opList.add(PERFORM_ACCOUNT_EDIT);
		opList.add(PERFORM_RESET_PASSWORD);
		opList.addAll(_adminOpList);
		_userOpList = opList;
	}

	//Other operations
	protected static final String REGISTER = "Register";
	protected static final String PERFORM_REGISTER = "PerformRegister";
	protected static final String LOGIN = "Login";

	//the services on offer
	protected static final String AUTHENTICATION_SERVICE = "Authentication";
	protected static final String GET_USER_INFORMATION_SERVICE = "GetUserInformation";
	protected static final String CHANGE_USER_EDIT_MODE_SERVICE = "ChangeUserEditMode";
	protected static final String REMOTE_AUTHENTICATION_SERVICE = "RemoteAuthentication";

	protected static boolean _derbyWrapperDoneForcedShutdown = false;

	protected String _recaptchaPrivateKey = null;
	protected String _recaptchaPublicKey = null;

	/** constructor */
	public Authentication()
	{
	}

	public void cleanUp()
	{
		super.cleanUp();

		if (!_derbyWrapperDoneForcedShutdown)
		{

			// This boolean is used to ensure we always shutdown the derby server, even if it is never
			// used by the Authentication server.  This is because the Tomcat greenstone3.xml
			// config file also specifies a connection to the database, which can result in the
			// server being initialized when the servlet is first accessed.  Note also, 
			// Authentication is a ServiceRack, meaning cleanUp() is called for each service
			// supported, however we only need to shutdown the Derby server once.  Again
			// this boolean variable helps achieve this.

			logger.info("Authentication Service performing forced shutdown of Derby Server ...");

			DerbyWrapper.shutdownDatabaseServer();
			_derbyWrapperDoneForcedShutdown = true;
		}
	}

	public boolean configure(Element info, Element extra_info)
	{
		logger.info("Configuring Authentication...");
		this.config_info = info;

		// set up Authentication service info - for now just has name and type
		Element authentication_service = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
		authentication_service.setAttribute(GSXML.TYPE_ATT, "authen");
		authentication_service.setAttribute(GSXML.NAME_ATT, AUTHENTICATION_SERVICE);
		this.short_service_info.appendChild(authentication_service);

		Element getUserInformation_service = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
		getUserInformation_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
		getUserInformation_service.setAttribute(GSXML.NAME_ATT, GET_USER_INFORMATION_SERVICE);
		this.short_service_info.appendChild(getUserInformation_service);

		Element changeEditMode_service = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
		changeEditMode_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
		changeEditMode_service.setAttribute(GSXML.NAME_ATT, CHANGE_USER_EDIT_MODE_SERVICE);
		this.short_service_info.appendChild(changeEditMode_service);
		
		Element remoteAuthentication_service = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
		remoteAuthentication_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
		remoteAuthentication_service.setAttribute(GSXML.NAME_ATT, REMOTE_AUTHENTICATION_SERVICE);
		this.short_service_info.appendChild(remoteAuthentication_service);
		

		DerbyWrapper.createDatabaseIfNeeded();

		NodeList recaptchaElems = info.getElementsByTagName("recaptcha");
		for (int i = 0; i < recaptchaElems.getLength(); i++)
		{
			Element currentElem = (Element) recaptchaElems.item(i);
			if (currentElem.getAttribute(GSXML.NAME_ATT) != null && currentElem.getAttribute(GSXML.NAME_ATT).equals("public_key"))
			{
				if (currentElem.getAttribute(GSXML.VALUE_ATT) != null)
				{
					_recaptchaPublicKey = currentElem.getAttribute(GSXML.VALUE_ATT);
				}
			}
			else if (currentElem.getAttribute(GSXML.NAME_ATT) != null && currentElem.getAttribute(GSXML.NAME_ATT).equals("private_key"))
			{
				if (currentElem.getAttribute(GSXML.VALUE_ATT) != null)
				{
					_recaptchaPrivateKey = currentElem.getAttribute(GSXML.VALUE_ATT);
				}
			}
		}

		return true;
	}

  protected Element getServiceDescription(Document doc, String service_id, String lang, String subset)
	{

		Element authen_service = doc.createElement(GSXML.SERVICE_ELEM);

		if (service_id.equals(AUTHENTICATION_SERVICE))
		{
			authen_service.setAttribute(GSXML.TYPE_ATT, "authen");
			authen_service.setAttribute(GSXML.NAME_ATT, AUTHENTICATION_SERVICE);
		}
		else if (service_id.equals(GET_USER_INFORMATION_SERVICE))
		{
			authen_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
			authen_service.setAttribute(GSXML.NAME_ATT, GET_USER_INFORMATION_SERVICE);
		}
		else if (service_id.equals(CHANGE_USER_EDIT_MODE_SERVICE))
		{
			authen_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
			authen_service.setAttribute(GSXML.NAME_ATT, CHANGE_USER_EDIT_MODE_SERVICE);
		}
		else if (service_id.equals(REMOTE_AUTHENTICATION_SERVICE))
		{
			authen_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
			authen_service.setAttribute(GSXML.NAME_ATT, REMOTE_AUTHENTICATION_SERVICE);
		}		
		else
		{
			return null;
		}

		if (service_id.equals(AUTHENTICATION_SERVICE) && (subset == null || subset.equals(GSXML.DISPLAY_TEXT_ELEM + GSXML.LIST_MODIFIER)))
		{
			authen_service.appendChild(GSXML.createDisplayTextElement(doc, GSXML.DISPLAY_TEXT_NAME, getServiceName(service_id, lang)));
			authen_service.appendChild(GSXML.createDisplayTextElement(doc, GSXML.DISPLAY_TEXT_DESCRIPTION, getServiceDescription(service_id, lang)));
		}
		return authen_service;
	}

	protected String getServiceName(String service_id, String lang)
	{
		return getTextString(service_id + ".name", lang);
	}

	protected String getServiceSubmit(String service_id, String lang)
	{
		return getTextString(service_id + ".submit", lang);
	}

	protected String getServiceDescription(String service_id, String lang)
	{
		return getTextString(service_id + ".description", lang);
	}

	protected Element processChangeUserEditMode(Element request)
	{
		// Create a new (empty) result message
	  Document result_doc = XMLConverter.newDOM();
		Element result = result_doc.createElement(GSXML.RESPONSE_ELEM);

		result.setAttribute(GSXML.FROM_ATT, CHANGE_USER_EDIT_MODE_SERVICE);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

		Element paramList = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (paramList == null)
		{
			GSXML.addError(result, _errorMessageMap.get(ERROR_REQUEST_HAS_NO_PARAM_LIST));
			return result;
		}

		HashMap<String, Serializable> params = GSXML.extractParams(paramList, true);

		String username = (String) params.get("username");
		String editMode = (String) params.get("enabled");

		if (!editMode.toLowerCase().equals("true") && !editMode.toLowerCase().equals("false"))
		{
			editMode = "false";
		}

		DerbyWrapper dw = openDatabase();
		dw.addUserData(username, "USER_EDIT_ENABLED", editMode);
		dw.closeDatabase();

		return result;
	}

	/**
	 * This method replaces the gliserver.pl code for authenticating a user against the derby database
	 * gliserver.pl needed to instantiate its own JVM to access the derby DB, but the GS3 already has
	 * the Derby DB open and 2 JVMs are not allowed concurrent access to an open embedded Derby DB.
	 * Gliserver.pl now goes through this method (via ServletRealmCheck.java), thereby using the same 
	 * connection to the DerbyDB. This method reproduces the same behaviour as gliserver.pl used to,
	 * by returning the user_groups on successful authentication, else returns the specific 
	 * "Authentication failed" messages that glisever.pl would produce.
	 * http://remote-host-name:8383/greenstone3/library?a=s&sa=authenticated-ping&excerptid=gs_content&un=admin&pw=<PW>&col=demo
	*/
	protected Element processRemoteAuthentication(Element request) {
		//logger.info("*** Authentication::processRemoteAuthentication");	
		
		String message = "";
		
		Element system = (Element) GSXML.getChildByTagName(request, GSXML.REQUEST_TYPE_SYSTEM);		
		String username = system.hasAttribute("username") ? system.getAttribute("username") : "";
		String password = system.hasAttribute("password") ? system.getAttribute("password") : "";
		
		
		// If we're not editing a collection then the user doesn't need to be in a particular group
		String collection = system.hasAttribute("collection") ? system.getAttribute("collection") : "";
				
		
		if(username.equals("") || password.equals("")) {
			message = "Authentication failed: no (username or) password specified.";
			//logger.error("*** Remote login failed. No username or pwd provided");
		}		
		else {		
			String storedPassword = retrieveDataForUser(username, "password");
			if(storedPassword != null && (password.equals(storedPassword) || hashPassword(password).equals(storedPassword))) {
				
				// gliserver.pl used to return the groups when authentication succeeded
				String groups = retrieveDataForUser(username, "groups"); //comma-separated list
				
				if(collection.equals("")) {
					message = groups;
				} else { 					
					
					if(groups.indexOf("all-collections-editor") != -1) { // Does this user have access to all collections?
						message = groups;
					} else if(groups.indexOf("personal-collections-editor") != -1 && collection.startsWith(username+"-")) { // Does this user have access to personal collections, and is this one?
						message = groups;
					} else if(groups.indexOf(collection+"-collection-editor") != -1) { //  Does this user have access to this collection?
						message = groups;
					}
					else {
						message = "Authentication failed: user is not in the required group.";
						//logger.error("*** Remote login failed. Groups did not match for the collection specified");
					}
				}
				
			} else {
				
				if(storedPassword == null) {
					message = "Authentication failed: no account for user '" + username + "'";
					//logger.error("*** Remote login failed. User not found or password not set for user.");
				} else {
					message = "Authentication failed: incorrect password.";
					//logger.error("*** Remote login failed. Password did not match for user");
				}
			}
		}
		Document result_doc = XMLConverter.newDOM();
		Element result = result_doc.createElement(GSXML.RESPONSE_ELEM);
		result.setAttribute(GSXML.FROM_ATT, REMOTE_AUTHENTICATION_SERVICE);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);		
		Element s = GSXML.createTextElement(result_doc, GSXML.STATUS_ELEM, message);
		result.appendChild(s);
		return result;
	}
	
	protected Element processGetUserInformation(Element request)
	{
		// Create a new (empty) result message
	  Document result_doc = XMLConverter.newDOM();
		Element result = result_doc.createElement(GSXML.RESPONSE_ELEM);

		result.setAttribute(GSXML.FROM_ATT, GET_USER_INFORMATION_SERVICE);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

		Element paramList = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (paramList == null)
		{
			GSXML.addError(result, _errorMessageMap.get(ERROR_REQUEST_HAS_NO_PARAM_LIST));
			return result;
		}

		HashMap<String, Serializable> params = GSXML.extractParams(paramList, true);

		String username = (String) params.get("username");

		if (username == null)
		{
			GSXML.addError(result, _errorMessageMap.get(ERROR_USERNAME_NOT_SPECIFIED));
			return result;
		}

		DerbyWrapper derbyWrapper = openDatabase();

		UserQueryResult userQueryResult = derbyWrapper.findUser(username);
		String editEnabled = derbyWrapper.getUserData(username, "USER_EDIT_ENABLED");

		Vector<UserTermInfo> terms = userQueryResult.getUserTerms();

		if (terms.size() == 0)
		{
			GSXML.addError(result, _errorMessageMap.get(ERROR_REQUESTED_USER_NOT_FOUND));
			return result;
		}

		UserTermInfo userInfo = terms.get(0);
		Element userInfoList = result_doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		result.appendChild(userInfoList);

		Element usernameField = GSXML.createParameter(result_doc, "username", userInfo.username);
		Element passwordField = GSXML.createParameter(result_doc, "password", userInfo.password);
		Element groupsField = GSXML.createParameter(result_doc, "groups", userInfo.groups);
		Element accountStatusField = GSXML.createParameter(result_doc, "accountstatus", userInfo.accountstatus);
		Element commentField = GSXML.createParameter(result_doc, "comment", userInfo.comment);

		if (editEnabled != null)
		{
			Element editEnabledElem = GSXML.createParameter(result_doc, "editEnabled", editEnabled);
			userInfoList.appendChild(editEnabledElem);
		}

		userInfoList.appendChild(usernameField);
		userInfoList.appendChild(passwordField);
		userInfoList.appendChild(groupsField);
		userInfoList.appendChild(accountStatusField);
		userInfoList.appendChild(commentField);

		derbyWrapper.closeDatabase();

		return result;
	}

	protected Element processAuthentication(Element request)
	{
		checkAdminUserExists();

		// Create a new (empty) result message
		Document result_doc = XMLConverter.newDOM();
		Element result = result_doc.createElement(GSXML.RESPONSE_ELEM);
		result.setAttribute(GSXML.FROM_ATT, AUTHENTICATION_SERVICE);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

		// Create an Authentication node put into the result
		Element authenNode = result_doc.createElement(GSXML.AUTHEN_NODE_ELEM);
		result.appendChild(authenNode);
		result.appendChild(getCollectList(result_doc, this.site_home + File.separatorChar + "collect"));

		// Create a service node added into the Authentication node
		Element serviceNode = result_doc.createElement(GSXML.SERVICE_ELEM);
		authenNode.appendChild(serviceNode);

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (param_list == null)
		{
			serviceNode.setAttribute("operation", LOGIN);
			GSXML.addError(result, _errorMessageMap.get(ERROR_REQUEST_HAS_NO_PARAM_LIST));
			return result; // Return the empty result
		}
		HashMap<String, Serializable> paramMap = GSXML.extractParams(param_list, false);
		String op = (String) paramMap.get("authpage");
		serviceNode.setAttribute("operation", op);

		String username = null;
		String groups = null;

		Element userInformation = (Element) GSXML.getChildByTagName(request, GSXML.USER_INFORMATION_ELEM);
		if (userInformation == null && _userOpList.contains(op))
		{
			serviceNode.setAttribute("operation", LOGIN);
			GSXML.addError(result, _errorMessageMap.get(ERROR_NOT_LOGGED_IN));
			return result;
		}

		if (userInformation != null)
		{
			username = userInformation.getAttribute(GSXML.USERNAME_ATT);
			groups = userInformation.getAttribute(GSXML.GROUPS_ATT);
		}

		if (username == null && _userOpList.contains(op))
		{
			serviceNode.setAttribute("operation", LOGIN);
			GSXML.addError(result, _errorMessageMap.get(ERROR_NOT_LOGGED_IN));
			return result;
		}

		if (_adminOpList.contains(op) && (groups == null || !groups.matches(".*\\badministrator\\b.*")))
		{
			serviceNode.setAttribute("operation", LOGIN);
			GSXML.addError(result, _errorMessageMap.get(ERROR_ADMIN_NOT_LOGGED_IN));
			return result;
		}

		if (op.equals(LIST_USERS))
		{
			int error = addUserInformationToNode(null, serviceNode);
			if (error != NO_ERROR)
			{
				GSXML.addError(result, _errorMessageMap.get(error));
			}
		}
		else if (op.equals(PERFORM_ADD))
		{
			String newUsername = (String) paramMap.get("username");
			String newPassword = (String) paramMap.get("password");
			String newGroups = (String) paramMap.get("groups");
			String newStatus = (String) paramMap.get("status");
			String newComment = (String) paramMap.get("comment");
			String newEmail = (String) paramMap.get("email");

			//Check the given user name
			int error;
			if ((error = checkUsername(newUsername)) != NO_ERROR)
			{
				GSXML.addError(result, _errorMessageMap.get(error));
				return result;
			}

			//Check the given password
			if ((error = checkPassword(newPassword)) != NO_ERROR)
			{
				GSXML.addError(result, _errorMessageMap.get(error));
				return result;
			}

			newPassword = hashPassword(newPassword);

			error = addUser(newUsername, newPassword, newGroups, newStatus, newComment, newEmail);
			if (error != NO_ERROR)
			{
				serviceNode.setAttribute("operation", ADD_USER);
				GSXML.addError(result, _errorMessageMap.get(error));
			}
			else
			{
				addUserInformationToNode(null, serviceNode);
				serviceNode.setAttribute("operation", LIST_USERS);
			}
		}
		else if (op.equals(PERFORM_REGISTER))
		{
			String newUsername = (String) paramMap.get("username");
			String newPassword = (String) paramMap.get("password");
			String newEmail = (String) paramMap.get("email");

			//Check the given user name
			int error;
			if ((error = checkUsername(newUsername)) != NO_ERROR)
			{
				GSXML.addError(result, _errorMessageMap.get(error));
				return result;
			}

			//Check the given password
			if ((error = checkPassword(newPassword)) != NO_ERROR)
			{
				GSXML.addError(result, _errorMessageMap.get(error));
				return result;
			}

			newPassword = hashPassword(newPassword);

			if (_recaptchaPrivateKey != null && _recaptchaPrivateKey.length() > 0)
			{
				ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
				reCaptcha.setPrivateKey(_recaptchaPrivateKey);

				try
				{
					//If this line throws an exception then we'll assume the user has a firewall that is too restrictive
					//(or that they're not connected to the Internet) to allow access to google services.
					//In this situation we won't use the recaptcha test.
					reCaptcha.checkAnswer(request.getAttribute("remoteAddress"), "", "");

					String challenge = (String) paramMap.get("recaptcha_challenge_field");
					String uResponse = (String) paramMap.get("recaptcha_response_field");

					if (challenge == null || uResponse == null)
					{
						serviceNode.setAttribute("operation", REGISTER);
						GSXML.addError(result, _errorMessageMap.get(ERROR_CAPTCHA_MISSING));
						return result;
					}

					ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(request.getAttribute("remoteAddress"), challenge, uResponse);

					if (!reCaptchaResponse.isValid())
					{
						serviceNode.setAttribute("operation", REGISTER);
						GSXML.addError(result, _errorMessageMap.get(ERROR_CAPTCHA_DOES_NOT_MATCH));
						return result;
					}
				}
				catch (Exception ex)
				{
				}
			}

			error = addUser(newUsername, newPassword, "", "true", "", newEmail);
			if (error != NO_ERROR)
			{
				serviceNode.setAttribute("operation", REGISTER);
				GSXML.addError(result, _errorMessageMap.get(error));
			}
		}
		else if (op.equals(PERFORM_EDIT))
		{
			String previousUsername = (String) paramMap.get("prevUsername");
			String newUsername = (String) paramMap.get("newUsername");
			String newPassword = (String) paramMap.get("password");
			String newGroups = (String) paramMap.get("groups");
			String newStatus = (String) paramMap.get("status");
			String newComment = (String) paramMap.get("comment");
			String newEmail = (String) paramMap.get("newEmail");

			//Check the given user name
			int error;
			if ((error = checkUsername(newUsername)) != NO_ERROR)
			{
				GSXML.addError(result, _errorMessageMap.get(error));
				return result;
			}

			if (newPassword == null)
			{
				newPassword = retrieveDataForUser(previousUsername, "password");
			}
			else
			{
				//Check the given password
				if ((error = checkPassword(newPassword)) != NO_ERROR)
				{
					GSXML.addError(result, _errorMessageMap.get(error));
					return result;
				}

				newPassword = hashPassword(newPassword);
			}

			error = removeUser(previousUsername);
			if (error != NO_ERROR)
			{
				if (error == ERROR_USERNAME_NOT_SPECIFIED)
				{
					addUserInformationToNode(null, serviceNode);
					serviceNode.setAttribute("operation", LIST_USERS);
				}
				else
				{
					serviceNode.setAttribute("operation", EDIT_USER);
					GSXML.addError(result, _errorMessageMap.get(error));
				}
				return result;
			}

			error = addUser(newUsername, newPassword, newGroups, newStatus, newComment, newEmail);
			if (error != NO_ERROR)
			{
				serviceNode.setAttribute("operation", EDIT_USER);
				GSXML.addError(result, _errorMessageMap.get(error));
			}
			else
			{
				addUserInformationToNode(null, serviceNode);
				serviceNode.setAttribute("operation", LIST_USERS);
			}
		}
		else if (op.equals(PERFORM_ACCOUNT_EDIT))
		{
			String previousUsername = (String) paramMap.get("prevUsername");
			String newUsername = (String) paramMap.get("newUsername");
			String oldPassword = (String) paramMap.get("oldPassword");
			String newPassword = (String) paramMap.get("newPassword");
			String newEmail = (String) paramMap.get("newEmail");

			//Make sure the user name does not already exist
			if (!previousUsername.equals(newUsername) && checkUserExists(newUsername))
			{
				addUserInformationToNode(previousUsername, serviceNode);
				serviceNode.setAttribute("operation", ACCOUNT_SETTINGS);
				GSXML.addError(result, _errorMessageMap.get(ERROR_USER_ALREADY_EXISTS));
				return result;
			}

			String prevPassword = retrieveDataForUser(previousUsername, "password");

			if (newPassword != null)
			{
				oldPassword = hashPassword(oldPassword);

				if (oldPassword == null || !oldPassword.equals(prevPassword))
				{
					addUserInformationToNode(previousUsername, serviceNode);
					serviceNode.setAttribute("operation", ACCOUNT_SETTINGS);
					GSXML.addError(result, _errorMessageMap.get(ERROR_INCORRECT_PASSWORD), "Incorrect Password");
					return result;
				}

				//Check the given password
				int error;
				if ((error = checkPassword(newPassword)) != NO_ERROR)
				{
					GSXML.addError(result, _errorMessageMap.get(error));
					return result;
				}

				newPassword = hashPassword(newPassword);
			}
			else
			{
				newPassword = prevPassword;
			}

			//Check the given user name
			int error;
			if ((error = checkUsername(newUsername)) != NO_ERROR)
			{
				GSXML.addError(result, _errorMessageMap.get(error));
				return result;
			}

			String prevGroups = retrieveDataForUser(previousUsername, "groups");
			String prevStatus = retrieveDataForUser(previousUsername, "status");
			String prevComment = retrieveDataForUser(previousUsername, "comment");

			error = removeUser(previousUsername);
			if (error != NO_ERROR)
			{
				if (error == ERROR_USERNAME_NOT_SPECIFIED)
				{
					addUserInformationToNode(null, serviceNode);
					serviceNode.setAttribute("operation", LIST_USERS);
				}
				else
				{
					addUserInformationToNode(previousUsername, serviceNode);
					serviceNode.setAttribute("operation", ACCOUNT_SETTINGS);
					GSXML.addError(result, _errorMessageMap.get(error));
				}
				return result;
			}

			error = addUser(newUsername, newPassword, prevGroups, prevStatus, prevComment, newEmail);
			if (error != NO_ERROR)
			{
				GSXML.addError(result, _errorMessageMap.get(error));
			}

			addUserInformationToNode(null, serviceNode);
			serviceNode.setAttribute("operation", LIST_USERS);
		}
		else if (op.equals(PERFORM_RETRIEVE_PASSWORD))
		{

		}
		else if (op.equals(PERFORM_CHANGE_PASSWORD))
		{
			serviceNode.setAttribute("operation", PERFORM_CHANGE_PASSWORD);
			String user_name = (String) paramMap.get("username");
			String oldPassword = (String) paramMap.get("oldPassword");
			String newPassword = (String) paramMap.get("newPassword");
			if (user_name == null || oldPassword == null || newPassword == null)
			{
				GSXML.addError(result, _errorMessageMap.get("missing compulsory parameters: username, oldPassword, or newPassword"));
				return result;
			}

			String prevPassword = retrieveDataForUser(user_name, "password");
			if (!hashPassword(oldPassword).equals(prevPassword))
			{
				addUserInformationToNode(user_name, serviceNode);
				GSXML.addError(result, _errorMessageMap.get(ERROR_INCORRECT_PASSWORD), "Incorrect Password");
				return result;
			}

			//Check the given password
			int error;
			if ((error = checkPassword(newPassword)) != NO_ERROR)
			{
				GSXML.addError(result, _errorMessageMap.get(error));
				return result;
			}

			DerbyWrapper derbyWrapper = openDatabase();
			String chpa_groups = retrieveDataForUser(user_name, "groups");
			String chpa_comment = "password_changed_by_user";
			String info = derbyWrapper.modifyUserInfo(user_name, hashPassword(newPassword), chpa_groups, null, chpa_comment, null);
			derbyWrapper.closeDatabase();
			if (info != "succeed")
			{//see DerbyWrapper.modifyUserInfo
				GSXML.addError(result, _errorMessageMap.get(info));
				return result;
			}
		}
		else if (op.equals(EDIT_USER))
		{
			String editUsername = (String) paramMap.get("username");
			int error = addUserInformationToNode(editUsername, serviceNode);
			if (error != NO_ERROR)
			{
				GSXML.addError(result, _errorMessageMap.get(error));
			}
		}
		else if (op.equals(ACCOUNT_SETTINGS))
		{
			String editUsername = (String) paramMap.get("username");

			if (editUsername == null)
			{
				serviceNode.setAttribute("operation", "");
				GSXML.addError(result, _errorMessageMap.get(ERROR_USERNAME_NOT_SPECIFIED));
				return result;
			}

			if (!editUsername.equals(username))
			{
				serviceNode.setAttribute("operation", LOGIN);
				GSXML.addError(result, _errorMessageMap.get(ERROR_NOT_AUTHORISED));
				return result;
			}
			int error = addUserInformationToNode(editUsername, serviceNode);
			if (error != NO_ERROR)
			{
				GSXML.addError(result, _errorMessageMap.get(error));
			}
		}
		else if (op.equals(PERFORM_RESET_PASSWORD))
		{
			String passwordResetUser = (String) paramMap.get("username");

			String newPassword = UUID.randomUUID().toString();
			newPassword = newPassword.substring(0, newPassword.indexOf("-"));

			String email = retrieveDataForUser(passwordResetUser, "email");
			String from = "admin@greenstone.org";
			String host = request.getAttribute("remoteAddress");

			//TODO: FINISH THIS
		}
		else if (op.equals(REGISTER))
		{
			if (_recaptchaPrivateKey != null && _recaptchaPrivateKey.length() > 0)
			{
				try
				{
					ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
					reCaptcha.setPrivateKey(_recaptchaPrivateKey);
					reCaptcha.checkAnswer(request.getAttribute("remoteAddress"), "", "");
				}
				catch (Exception ex)
				{
					return result;
				}
			}

			if (_recaptchaPublicKey != null && _recaptchaPrivateKey != null)
			{
				Element recaptchaElem = result_doc.createElement("recaptcha");
				recaptchaElem.setAttribute("publicKey", _recaptchaPublicKey);
				recaptchaElem.setAttribute("privateKey", _recaptchaPrivateKey);
				result.appendChild(recaptchaElem);
			}
		}
		else if (op.equals(PERFORM_DELETE_USER))
		{
			String usernameToDelete = (String) paramMap.get("username");
			int error = removeUser(usernameToDelete);
			if (error != NO_ERROR)
			{
				GSXML.addError(result, _errorMessageMap.get(error));
			}
			addUserInformationToNode(null, serviceNode);
			serviceNode.setAttribute("operation", LIST_USERS);
		}

		return result;
	}

	public int checkUsernameAndPassword(String username, String password)
	{
		int uResult = checkUsername(username);
		int pResult = checkPassword(password);

		return (uResult != NO_ERROR ? uResult : (pResult != NO_ERROR ? pResult : NO_ERROR));
	}

	public int checkUsername(String username)
	{
		//Check the given user name
		if ((username == null) || (username.length() < USERNAME_MIN_LENGTH) || (username.length() > USERNAME_MAX_LENGTH) || (!(Pattern.matches("[a-zA-Z0-9//_//.]+", username))))
		{
			return ERROR_INVALID_USERNAME;
		}
		return NO_ERROR;
	}

	public int checkPassword(String password)
	{
		//Check the given password
		if (password == null)
		{
			return ERROR_PASSWORD_NOT_ENTERED;
		}
		else if (password.length() < PASSWORD_MIN_LENGTH)
		{
			return ERROR_PASSWORD_TOO_SHORT;
		}
		else if (password.length() > PASSWORD_MAX_LENGTH)
		{
			return ERROR_PASSWORD_TOO_LONG;
		}
		else if (!(Pattern.matches("[\\p{ASCII}]+", password)))
		{
			return ERROR_PASSWORD_USES_ILLEGAL_CHARACTERS;
		}
		return NO_ERROR;
	}

	public static String hashPassword(String password)
	{
		return DigestUtils.sha1Hex(password);
	}

	// This method can also be used for printing out the password in hex (in case
	// the password used the UTF-8 Charset), or the hex values in any unicode string.
	// From http://stackoverflow.com/questions/923863/converting-a-string-to-hexadecimal-in-java
	public static String toHex(String arg)
	{
		try
		{
			return String.format("%x", new BigInteger(arg.getBytes("US-ASCII"))); // set to same charset as used by hashPassword
		}
		catch (Exception e)
		{ // UnsupportedEncodingException
			e.printStackTrace();
		}
		return "Unable to print";
	}

	private void checkAdminUserExists()
	{
		DerbyWrapper derbyWrapper = openDatabase();
		UserQueryResult userQueryResult = derbyWrapper.findUser(null, null);
		derbyWrapper.closeDatabase();

		if (userQueryResult != null)
		{
			Vector userInfo = userQueryResult.users;

			boolean adminFound = false;
			for (int i = 0; i < userQueryResult.getSize(); i++)
			{
				if (((UserTermInfo) userInfo.get(i)).groups != null && ((UserTermInfo) userInfo.get(i)).groups.matches(".*\\badministrator\\b.*"))
				{
					adminFound = true;
				}
			}

			if (!adminFound)
			{
				addUser("admin", "admin", "administrator", "true", "Change the password for this account as soon as possible", "");
			}
		}
	}

	private DerbyWrapper openDatabase()
	{
		// check the usersDb database, if it isn't existing, check the etc dir, create the etc dir if it isn't existing, then create the  user database and add a "admin" user
		String usersDB_dir = GlobalProperties.getGSDL3Home() + File.separatorChar + "etc" + File.separatorChar + "usersDB";
		DerbyWrapper derbyWrapper = new DerbyWrapper(usersDB_dir);
		return derbyWrapper;
	}

	private int addUserInformationToNode(String username, Element serviceNode)
	{
		DerbyWrapper derbyWrapper = openDatabase();
		UserQueryResult userQueryResult = derbyWrapper.findUser(username, null);
		derbyWrapper.closeDatabase();

		if (userQueryResult != null)
		{
		  Element user_node = getUserNodeList(serviceNode.getOwnerDocument(), userQueryResult);
			serviceNode.appendChild(user_node);
			return NO_ERROR;
		}

		return ERROR_COULD_NOT_GET_USER_INFO;
	}

	private int removeUser(String username)
	{
		if (username == null)
		{
			return ERROR_USERNAME_NOT_SPECIFIED;
		}

		DerbyWrapper derbyWrapper = openDatabase();
		boolean success = derbyWrapper.deleteUser(username);
		derbyWrapper.closeDatabase();

		if (success)
		{
			return NO_ERROR;
		}

		return ERROR_REMOVING_USER;
	}

	private int addUser(String newUsername, String newPassword, String newGroups, String newStatus, String newComment, String newEmail)
	{
		newGroups = newGroups.replaceAll(" ", "");

		//Check if the user already exists
		DerbyWrapper derbyWrapper = openDatabase();
		UserQueryResult userQueryResult = derbyWrapper.findUser(newUsername, null);

		if (userQueryResult != null)
		{
			derbyWrapper.closeDatabase();
			return ERROR_USER_ALREADY_EXISTS;
		}
		else
		{
			boolean success = derbyWrapper.addUser(newUsername, newPassword, newGroups, newStatus, newComment, newEmail);
			derbyWrapper.closeDatabase();

			if (!success)
			{
				return ERROR_ADDING_USER;
			}
		}

		return NO_ERROR;
	}

	private boolean checkUserExists(String username)
	{
		boolean check_status = false;

		DerbyWrapper derbyWrapper = openDatabase();
		try
		{
			UserQueryResult result = derbyWrapper.findUser(username);

			if (result != null)
			{
				check_status = true;
			}

		}
		catch (Exception ex)
		{
			// some error occurred accessing the database
			ex.printStackTrace();
		}
		derbyWrapper.closeDatabase();

		return check_status;
	}

	private String retrieveDataForUser(String username, String dataType)
	{
		openDatabase();

		String data = null;

		try
		{
			DerbyWrapper derbyWrapper = openDatabase();
			UserQueryResult result = derbyWrapper.findUser(username);
			derbyWrapper.closeDatabase();
			Vector userInfo = result.users;

			for (int i = 0; i < result.getSize(); i++)
			{
				if (dataType.equals("password"))
				{
					data = ((UserTermInfo) userInfo.get(i)).password;
					break;
				}
				else if (dataType.equals("groups"))
				{
					data = ((UserTermInfo) userInfo.get(i)).groups;
					break;
				}
				else if (dataType.equals("status"))
				{
					data = ((UserTermInfo) userInfo.get(i)).accountstatus;
					break;
				}
				else if (dataType.equals("comment"))
				{
					data = ((UserTermInfo) userInfo.get(i)).comment;
					break;
				}
				else if (dataType.equals("email"))
				{
					data = ((UserTermInfo) userInfo.get(i)).email;
					break;
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		return data;
	}

  private Element getUserNodeList(Document doc, UserQueryResult userQueryResult)
	{
		Element user_list_node = doc.createElement(GSXML.USER_NODE_ELEM + GSXML.LIST_MODIFIER);

		Vector userInfo = userQueryResult.users;

		for (int i = 0; i < userQueryResult.getSize(); i++)
		{
			Element user_node = doc.createElement(GSXML.USER_NODE_ELEM);
			String username = ((UserTermInfo) userInfo.get(i)).username;
			String groups = ((UserTermInfo) userInfo.get(i)).groups;
			String accountstatus = ((UserTermInfo) userInfo.get(i)).accountstatus;
			String comment = ((UserTermInfo) userInfo.get(i)).comment;
			String email = ((UserTermInfo) userInfo.get(i)).email;
			user_node.setAttribute("username", username);
			user_node.setAttribute("groups", groups);
			user_node.setAttribute("status", accountstatus);
			user_node.setAttribute("comment", comment);
			user_node.setAttribute("email", email);

			user_list_node.appendChild(user_node);
		}
		return user_list_node;
	}

  private Element getCollectList(Document doc, String collect)
	{
		Element collect_list_node = doc.createElement(GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER);
		File[] collect_dir = (new File(collect)).listFiles();
		if (collect_dir != null && collect_dir.length > 0)
		{
			for (int i = 0; i < collect_dir.length; i++)
			{
				if (collect_dir[i].isDirectory() && (!collect_dir[i].getName().startsWith(".svn")))
				{
					Element collect_node = doc.createElement(GSXML.COLLECTION_ELEM);
					collect_node.setAttribute(GSXML.NAME_ATT, collect_dir[i].getName());
					collect_list_node.appendChild(collect_node);
				}
			}
		}
		return collect_list_node;
	}

	// main() method - calls hashPassword() on any String argument, printing this to stdout
	// This main() is invoked by gliserver.pl perl code to encrypt passwords identically to Java code.
	public static void main(String[] args)
	{
		if (args.length < 1)
		{
			System.err.println("Usage: Authentication <string to encrypt>");
			System.exit(-1);
		}
		// just hash the first argument
		String hash = Authentication.hashPassword(args[0]);
		System.out.println(hash);
	}
}
