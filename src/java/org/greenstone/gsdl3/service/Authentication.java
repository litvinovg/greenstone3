package org.greenstone.gsdl3.service;

import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.DerbyWrapper;
import org.greenstone.gsdl3.util.UserQueryResult;
import org.greenstone.gsdl3.util.UserTermInfo;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Vector;
import java.sql.SQLException;
import java.util.regex.Pattern;
import java.io.File;
import java.io.UnsupportedEncodingException;

public class Authentication
extends ServiceRack {
	//the services on offer
	protected static final String AUTHENTICATION_SERVICE="Authentication";

	/** constructor */
	public Authentication()
	{ }

	public boolean configure(Element info, Element extra_info) 
	{
		logger.info("Configuring Authentication...");
		this.config_info = info;

		// set up Authentication service info - for now just has name and type
		Element authentication_service= this.doc.createElement(GSXML.SERVICE_ELEM);
		authentication_service.setAttribute(GSXML.TYPE_ATT, "authen"); 
		authentication_service.setAttribute(GSXML.NAME_ATT, AUTHENTICATION_SERVICE);
		this.short_service_info.appendChild(authentication_service);

		return true;
	}

	protected Element getServiceDescription(String service_id, String lang, String subset) 
	{

		Element authen_service=this.doc.createElement(GSXML.SERVICE_ELEM);

		if (service_id.equals(AUTHENTICATION_SERVICE)) {
			authen_service.setAttribute(GSXML.TYPE_ATT,"authen");
			authen_service.setAttribute(GSXML.NAME_ATT, AUTHENTICATION_SERVICE);
		} else {
			return null;
		}

		if (subset==null || subset.equals(GSXML.DISPLAY_TEXT_ELEM+GSXML.LIST_MODIFIER)) {
			authen_service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_NAME, getServiceName(service_id, lang) ));
			authen_service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_DESCRIPTION, getServiceDescription(service_id, lang)));
		}
		return authen_service;  
	}

	protected String getServiceName(String service_id, String lang) {
		return getTextString(service_id+".name", lang);
	}

	protected String getServiceSubmit(String service_id, String lang) {
		return getTextString(service_id+".submit", lang);
	}

	protected String getServiceDescription(String service_id, String lang) {
		return getTextString(service_id+".description", lang);
	}

	protected void addCustomParams(String service, Element param_list, String lang) {
	}

	protected void createParameter(String name, Element param_list, String lang) {
	}

	protected Element processAuthentication(Element request) throws SQLException, UnsupportedEncodingException{

		// Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);

		result.setAttribute(GSXML.FROM_ATT, AUTHENTICATION_SERVICE);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

		String lang = request.getAttribute(GSXML.LANG_ATT);
		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);

		if (param_list == null) {
			logger.error("AddUsers request had no paramList.");
			return result;  // Return the empty result
		}

		String aup=null; //Actions: ListUsers, AddUser, ModifyPassword, DeleteUser, Login
		String un=""; //login user's name
		String pw=""; //login user's password
		String asn=""; //whether a user is authenticated
		String uan=""; //whether a authentication for a particular action is needed
		String cm=""; //whether the action is confirmed

		String umun=""; //the new user name
		String umpw=""; //user's new password
		String umas=""; //user account status
		String umgp=""; //user greoups
		String umc=""; // comments for the user

		String oumun=""; //the original user's name
		String umpw1=""; //user's new password
		String umpw2=""; //user's retyped new password

		//used for adding a list of users at one time. Format: name,password,role]name,password,role]...
		//in which, role may be in the format: student:[teacher's username]
		String unpwlist=""; 
		String service = "";
		
		// get parameters from the request
		NodeList params = param_list.getElementsByTagName(GSXML.PARAM_ELEM);
		for (int i=0; i<params.getLength();i++) {
			Element param = (Element)params.item(i);
			String p_name = param.getAttribute(GSXML.NAME_ATT);
			String p_value = GSXML.getValue(param);

			if (p_name.equals("aup")){
				aup = p_value;
			}else if (p_name.equals("un")) {
				un = p_value;
			}else if(p_name.equals("pw")) {
				pw = p_value;
			}else if(p_name.equals("umun")) {
				umun = p_value;
			}else if(p_name.equals("umpw")) {
				umpw = p_value;
			}else if (p_name.equals("umas")){
				umas = p_value;
			}else if (p_name.equals("umgp")){
				umgp = p_value;
			}else if (p_name.equals("umc")){
				umc = p_value;
			}else if (p_name.equals("asn")){
				asn = p_value;
			}else if (p_name.equals("uan")){
				uan = p_value;
			}else if (p_name.equals("cm")){
				cm = p_value;
			}else if(p_name.equals("umpw1")) {
				umpw1 = p_value;
			}else if(p_name.equals("umpw2")) {
				umpw2 = p_value;
			}else if(p_name.equals("oumun")) {
				oumun = p_value;
			}else if(p_name.equals("unpwlist")) {
				unpwlist = p_value;
			}

		}

		// create a Authentication node put into the result
		Element authen_node = this.doc.createElement(GSXML.AUTHEN_NODE_ELEM);
		result.appendChild(authen_node);
		result.appendChild(getCollectList(this.site_home + File.separatorChar + "collect"));
		// create a service node added into the Authentication node
		Element service_node = this.doc.createElement(GSXML.SERVICE_ELEM);
		authen_node.appendChild(service_node);
		service_node.setAttribute("aup",aup);
		// user's info 
		UserQueryResult userQueryResult=null;

		// check the usersDb database, if it isn't existing, check the etc dir, create the etc dir if it isn't existing, then create the  user database and add a "admin" user
		String usersDB_dir = this.site_home + File.separatorChar + "etc" + File.separatorChar + "usersDB";
		DerbyWrapper derbyWrapper=new DerbyWrapper();
		File usersDB_file =new File(usersDB_dir);
		if (!usersDB_file.exists()){
			String etc_dir = this.site_home + File.separatorChar + "etc";
			File etc_file =new File(etc_dir);
			if (!etc_file.exists()){
				boolean success = etc_file.mkdir();
				if (!success){
					logger.error("Couldn't create the etc dir under "+this.site_home + ".");
					return result;
				}
			}
			derbyWrapper.connectDatabase(usersDB_dir,true);
			derbyWrapper.createDatabase();
		}else{
			derbyWrapper.connectDatabase(usersDB_dir, false);
		}

		// Action: login
		if (aup.equals("Login")){
			if (uan.equals("")){ //return a login page, if the user's name is not given
				service_node.setAttribute("info","Login");
				derbyWrapper.closeDatabase();
				return result;
			}
			String groups = "";
			// if the authentication(uan=1) is required,but the user hasn't been authenticated(asn=0),the user is asked to login first
			if ((uan.equals("1") && asn.equals("0"))) {
				if ((un.length()==0) && (pw.length()==0)){
					service_node.setAttribute("asn","0");
					service_node.setAttribute("info","Login");
					derbyWrapper.closeDatabase();
					return result;
				}
				if ((un.length()==0) || (pw.length()==0)){
					service_node.setAttribute("asn","0");
					service_node.setAttribute("info","Login");
					service_node.setAttribute("err","un-pw-err");
					derbyWrapper.closeDatabase();
					return result;
				}else{  
					userQueryResult=derbyWrapper.findUser(un,pw);//looking for the user from the users table
					service_node.setAttribute(GSXML.NAME_ATT,"Authentication");
					service_node.setAttribute("un",un);
					if (userQueryResult==null){
						//the user isn't a vaild user
						service_node.setAttribute("asn","0");
						service_node.setAttribute("err","un-pw-err");// either unsername or password is wrong
						service_node.setAttribute("info","Login");
						derbyWrapper.closeDatabase();
						return result;
					}else{
						// asn="1"; //the user is a member of the "administrator" group
						Vector userInfo=userQueryResult.users_;
						groups=((UserTermInfo)userInfo.get(0)).groups_;
						String accountstatus=((UserTermInfo)userInfo.get(0)).accountstatus_;
						if (accountstatus.trim().equals("false")){
							service_node.setAttribute("asn","0");
							service_node.setAttribute("err","as-false");//the account status is false
							service_node.setAttribute("info","Login");
							derbyWrapper.closeDatabase();
							return result;
						}
						String[] groups_array=groups.split(",");
						for (int i=0; i<groups_array.length;i++){
							if ((groups_array[i].trim().toLowerCase()).equals("administrator")){// check whether the user is in the administrator group
								asn="1";
								service_node.setAttribute("asn","1");
								break;
							}
						}
						if (!asn.equals("1")){
							asn="2";
							service_node.setAttribute("asn","2");//the user is authenticated							
						}
					}
				}
			}

			//asn!=0 This is a valid user 
			if (!asn.equals("0")){
				service_node.setAttribute("info","Login");
				service_node.setAttribute("un",un);
				service_node.setAttribute("pw",pw);
				service_node.setAttribute("asn",asn);
				service_node.setAttribute("umgp",groups);
				derbyWrapper.closeDatabase();
				return result;
			}
		}

		//Action: listuser
		if (aup.equals("ListUsers")){
			if (asn.equals("") && un.equals("")){
				service_node.setAttribute("info","Login");
				derbyWrapper.closeDatabase();
				return result;
			}

			//valid users but not in the administrator group(asn=2), they cannot list all users
			if (asn.equals("2")){
				service_node.setAttribute("info","Login");
				service_node.setAttribute("err","no-permission"); 
				service_node.setAttribute("un",un);
				service_node.setAttribute("asn",asn);
				derbyWrapper.closeDatabase();
				return result;
			}
			//valid users belong to the administrator group(asn=1), they can list all users
			if (asn.equals("1")){
				userQueryResult=derbyWrapper.findUser(null,null);
				derbyWrapper.closeDatabase();
				service_node.setAttribute(GSXML.NAME_ATT,"Authentication");
				service_node.setAttribute("un",un);
				service_node.setAttribute("asn",asn);

				if (userQueryResult!=null && userQueryResult.getSize()>0){
					service_node.setAttribute("info","all-un"); // got a user list
					Element user_node=getUserNode(userQueryResult);
					service_node.appendChild(user_node);
					derbyWrapper.closeDatabase();
					return result;
				}else {
					service_node.setAttribute("err","no-un"); // no user returned
					derbyWrapper.closeDatabase();
					return result;
				}    
			}
		}
		//TODO: Action : addStudents (bulk adding)
		if (aup.equals("AddStudents")){
			String[] users = unpwlist.split("]");
			for(int i=0; i<users.length; i++) {
				String[] user = users[i].split(",");
				String uname = user[0];
				String password = user[1];
				String group = user[2].split(":")[0];
				String add_user=derbyWrapper.addUser(uname, password, group,"true","");
				if (add_user.equals("succeed")){
					userQueryResult=derbyWrapper.findUser(null,null);
					derbyWrapper.closeDatabase();
					service_node.setAttribute("info","all-un"); // return a list of all users if the user has been added
					Element user_node=getUserNode(userQueryResult);
					service_node.appendChild(user_node);
					derbyWrapper.closeDatabase();
					return result;
				}			
			}
		}
		
		//Action : adduder
		if (aup.equals("AddUser")){
			if (asn.equals("") && un.equals("")){
				service_node.setAttribute("info","Login");
				derbyWrapper.closeDatabase();
				return result;
			}
			//valid users can't add a new user because they aren't in the administrator group(asn=2)
			if (asn.equals("2")){
				service_node.setAttribute("info","Login");
				service_node.setAttribute("err","no-permission"); 
				service_node.setAttribute("un",un);
				service_node.setAttribute("asn",asn);
				derbyWrapper.closeDatabase();
				return result;
			}
			//valid users are in the administrator group, they can add a new user(asn=1)
			if (asn.equals("1")){
				service_node.setAttribute(GSXML.NAME_ATT,"Authentication");
				service_node.setAttribute("un",un);
				service_node.setAttribute("asn",asn);

				if (umun.length()==0 && umpw.length()==0 && umgp.length()==0 && umas.length()==0 && umc.length()==0){
					service_node.setAttribute("info","adduser_interface");
					derbyWrapper.closeDatabase();
					return result;
				}

				//check the strings of username and password
				if ((umun==null) || (umun.length()<2) || (umun.length()>30) || (!(Pattern.matches("[a-zA-Z0-9//_//.]+",umun)))){
					service_node.setAttribute("err","un-err"); //the input username string is illegal
					service_node.setAttribute("info","adduser_interface");
					derbyWrapper.closeDatabase();
					return result;
				}

				if ((umpw==null) || (umpw.length()<3) || (umpw.length()>8) || (!(Pattern.matches("[\\p{ASCII}]+",umpw)))){
					service_node.setAttribute("err","pw-err"); //the input passwrod string is illegal
					service_node.setAttribute("info","adduser_interface");
					derbyWrapper.closeDatabase();
					return result;
				}

				// add the new users into the users table
				umgp=umgp.replaceAll(" ","");//get rid of the space of the groups string
				userQueryResult=derbyWrapper.findUser(umun,null);// check whether the new user name has existed in the table.
				if (userQueryResult!=null){
					service_node.setAttribute("err","un-exist"); //the new username string is duplicated
					service_node.setAttribute("info","adduser_interface");
					derbyWrapper.closeDatabase();
					return result;
				}else{
					String add_user=derbyWrapper.addUser(umun,umpw,umgp,umas,umc);
					if (add_user.equals("succeed")){
						userQueryResult=derbyWrapper.findUser(null,null);
						derbyWrapper.closeDatabase();
						service_node.setAttribute("info","all-un"); // return a list of all users if the user has been added
						Element user_node=getUserNode(userQueryResult);
						service_node.appendChild(user_node);
						derbyWrapper.closeDatabase();
						return result;
					}else{
						derbyWrapper.closeDatabase();
						service_node.setAttribute("err",add_user);// return the error message if the user couldn't be added 
						derbyWrapper.closeDatabase();
						return result;
					}
				}
			}
		}

		//Action: edituser
		if (aup.equals("EditUser")){
			service_node.setAttribute(GSXML.NAME_ATT,"Authentication");
			service_node.setAttribute("un",un);
			service_node.setAttribute("asn",asn);

			//Get the user's info from the database
			if (cm.length()==0){
				service_node.setAttribute("info","edituser-interface");
				userQueryResult=derbyWrapper.findUser(umun,null);
				derbyWrapper.closeDatabase();
				Vector userInfo=userQueryResult.users_;
				String username=((UserTermInfo)userInfo.get(0)).username_;
				String password=((UserTermInfo)userInfo.get(0)).password_;
				String groups=((UserTermInfo)userInfo.get(0)).groups_;
				String accountstatus=((UserTermInfo)userInfo.get(0)).accountstatus_;
				String comment=((UserTermInfo)userInfo.get(0)).comment_;

				service_node.setAttribute("oumun",oumun);
				service_node.setAttribute("umun",username);
				service_node.setAttribute("umpw",password);
				service_node.setAttribute("umgp",groups);
				service_node.setAttribute("umas",accountstatus);
				service_node.setAttribute("umc",comment);
				derbyWrapper.closeDatabase();
				return result;
			}

			//Commit the modified user's info to the database
			if (cm.toLowerCase().equals("submit")){
				if (oumun.equals(umun)){// the user's name hasn't been changed, update the user's info
					if (umpw.length()==0){
						derbyWrapper.modifyUserInfo(umun,null,umgp,umas,umc);
						userQueryResult=derbyWrapper.findUser(null,null);
						derbyWrapper.closeDatabase();
						service_node.setAttribute("info","all-un"); // the user's info has been updated, return a list of all users 
						Element user_node=getUserNode(userQueryResult);
						service_node.appendChild(user_node);
						derbyWrapper.closeDatabase();
						return result;
					}else{
						if ((umpw.length()==0) || (umpw.length()<3) || (umpw.length()>8) || (!(Pattern.matches("[\\p{ASCII}]+",umpw)))){
							service_node.setAttribute("err","umpw-err"); //the input passwrod string is illegal
							service_node.setAttribute("info","edituser-interface");
							service_node.setAttribute("umun",umun);
							service_node.setAttribute("umpw",umpw);
							service_node.setAttribute("umgp",umgp);
							service_node.setAttribute("umas",umas);
							service_node.setAttribute("umc",umc);
							service_node.setAttribute("oumun",oumun);
							derbyWrapper.closeDatabase();
							return result;
						}
						umgp=umgp.replaceAll(" ","");// get rid of the space
						derbyWrapper.modifyUserInfo(umun,umpw,umgp,umas,umc);
						userQueryResult=derbyWrapper.listAllUser();
						derbyWrapper.closeDatabase();
						service_node.setAttribute("info","all-un"); // if the new user has been added successfully, return a list of all users 
						Element user_node=getUserNode(userQueryResult);
						service_node.appendChild(user_node);
						derbyWrapper.closeDatabase();
						return result;
					}
				}
				// The user's name has been changed, add a new user record to the database
				else{
					if ((umun.length()==0) || (umun.length()<2) || (umun.length()>30) || (!(Pattern.matches("[a-zA-Z0-9//_//.]+",umun)))){
						service_node.setAttribute("err","umun-err"); //the input username string is illegal
						service_node.setAttribute("umun",umun);
						service_node.setAttribute("umpw",umpw);
						service_node.setAttribute("umgp",umgp);
						service_node.setAttribute("umas",umas);
						service_node.setAttribute("umc",umc);
						service_node.setAttribute("oumun",oumun);
						service_node.setAttribute("info","edituser-interface");
						derbyWrapper.closeDatabase();
						return result;
					}
					if (umpw.length()==0){
						service_node.setAttribute("err","ini-umpw-err"); //the input passwrod string is illegal
						service_node.setAttribute("info","edituser-interface");
						service_node.setAttribute("umun",umun);
						service_node.setAttribute("umpw",umpw);
						service_node.setAttribute("umgp",umgp);
						service_node.setAttribute("umas",umas);
						service_node.setAttribute("umc",umc);
						service_node.setAttribute("oumun",oumun);
						derbyWrapper.closeDatabase();
						return result;
					}
					if ((umpw.length()<3) || (umpw.length()>8) || (!(Pattern.matches("[\\p{ASCII}]+",umpw)))){
						service_node.setAttribute("err","umpw-err"); //the input passwrod string is illegal
						service_node.setAttribute("info","edituser-interface");
						service_node.setAttribute("umun",umun);
						service_node.setAttribute("umpw",umpw);
						service_node.setAttribute("umgp",umgp);
						service_node.setAttribute("umas",umas);
						service_node.setAttribute("umc",umc);
						service_node.setAttribute("oumun",oumun);
						derbyWrapper.closeDatabase();
						return result;
					}
					umgp=umgp.replaceAll(" ","");// get rid of the space
					userQueryResult=derbyWrapper.findUser(umun,null);// check whether the new user name has existed in the table.
					if (userQueryResult!=null){
						service_node.setAttribute("err","un-exist"); //the new username string is duplicated
						service_node.setAttribute("info","edituser-interface");
						service_node.setAttribute("umun","");
						service_node.setAttribute("umpw","");
						service_node.setAttribute("umgp",umgp);
						service_node.setAttribute("umas",umas);
						service_node.setAttribute("umc",umc);
						service_node.setAttribute("oumun",oumun);
						derbyWrapper.closeDatabase();
						return result;
					}else{
						derbyWrapper.addUser(umun,umpw,umgp,umas,umc);
						userQueryResult=derbyWrapper.listAllUser();
						derbyWrapper.closeDatabase();
						service_node.setAttribute("info","all-un"); // if the new user has been added successfully, return a list of all users 
						Element user_node=getUserNode(userQueryResult);
						service_node.appendChild(user_node);
						derbyWrapper.closeDatabase();
						return result;
					}
				}
			}

			if (cm.toLowerCase().equals("cancel")){
				userQueryResult=derbyWrapper.listAllUser();
				derbyWrapper.closeDatabase();
				service_node.setAttribute("info","all-un"); // if the new user has been added successfully, return a list of all users 
				Element user_node=getUserNode(userQueryResult);
				service_node.appendChild(user_node);
				derbyWrapper.closeDatabase();
				return result;
			}
		}

		//Action: modifypassword
		if (aup.equals("ModifyPassword")){
			if (un.equals("")){
				service_node.setAttribute("info","Login");
				derbyWrapper.closeDatabase();
				return result;
			}

			service_node.setAttribute(GSXML.NAME_ATT,"Authentication");
			service_node.setAttribute("un",un);
			service_node.setAttribute("asn",asn);

			userQueryResult=derbyWrapper.findUser(un,null);
			Vector userInfo=userQueryResult.users_;
			pw=((UserTermInfo)userInfo.get(0)).password_;

			if ((umpw1.length()==0) && (umpw2.length()==0) && (umpw.length()==0)){
				service_node.setAttribute("info","modify_interface");// call the interface of the modifying password 
				derbyWrapper.closeDatabase();
				return result;
			}

			if (!pw.equals(umpw) && umpw.length()>0){
				service_node.setAttribute("info","modify_interface");
				service_node.setAttribute("err","pw-umpw-nm-err");//if the original password is not match
				derbyWrapper.closeDatabase();
				return result;
			}

			if ((umpw1.length()==0) || (umpw2.length()==0)){
				service_node.setAttribute("info","modify_interface");
				service_node.setAttribute("err","umpw1-umpw2-null-err");//if one of the password strings is none,return the err info back
				derbyWrapper.closeDatabase();
				return result;
			}

			if(!umpw1.equals(umpw2)){
				service_node.setAttribute("info","modify_interface");
				service_node.setAttribute("err","umpw1-umpw2-nm-err");//if one of the password strings is none,return the err info back
				derbyWrapper.closeDatabase();
				return result;
			}

			if (umpw.length()==0){
				service_node.setAttribute("info","modify_interface");
				service_node.setAttribute("err","umpw-null-err");//if one of the password strings is none,return the err info back
				derbyWrapper.closeDatabase();
				return result;
			}
			//check the new password and the retyped password
			if ((umpw1==null) || (umpw1.length()<3) || (umpw1.length()>8) || (!(Pattern.matches("[\\p{ASCII}]+",umpw1)))){
				service_node.setAttribute("info","modify_interface");
				service_node.setAttribute("err","umpw1-err");// the new password is illegal
				derbyWrapper.closeDatabase();
				return result;
			}  

			if ((umpw2==null) || (umpw2.length()<3) || (umpw2.length()>8) || (!(Pattern.matches("[\\p{ASCII}]+",umpw2)))){
				service_node.setAttribute("info","modify_interface");
				service_node.setAttribute("err","umpw2-err"); // the retyped password is illegal
				derbyWrapper.closeDatabase();
				return result;
			}  
			String modify_user_info=derbyWrapper.modifyUserInfo(un,umpw1,null,null,null);
			if (modify_user_info.equals("succeed")){
				service_node.setAttribute("err","");// the passsword has been changed successfully
				derbyWrapper.closeDatabase();
				return result;
			}else{
				service_node.setAttribute("err",modify_user_info);// return the error message of the pasword couldn't be modified 
				derbyWrapper.closeDatabase();
				return result;
			}
		}

		//Action: deleteuser
		if (aup.equals("DeleteUser")){
			service_node.setAttribute("un",un);
			service_node.setAttribute("asn",asn);
			service_node.setAttribute("umun",umun);
			if (cm.equals("yes")){
				String delete_user=derbyWrapper.deleteUser(umun);
				if (delete_user.equals("succeed")){
					service_node.setAttribute("err","");
					userQueryResult=derbyWrapper.listAllUser();
					service_node.setAttribute("info","all-un"); //  return a list of all users 
					Element user_node=getUserNode(userQueryResult);
					service_node.appendChild(user_node);
				}else{
					service_node.setAttribute("err",delete_user);//return the error message
					derbyWrapper.closeDatabase();
					return result;
				}
			}else if (cm.equals("no")){
				service_node.setAttribute("err","");
				userQueryResult=derbyWrapper.listAllUser();
				service_node.setAttribute("info","all-un"); //  return a list of all users 
				Element user_node=getUserNode(userQueryResult);
				service_node.appendChild(user_node);
				derbyWrapper.closeDatabase();
				return result;
			}else{
				service_node.setAttribute("info","confirm");
				derbyWrapper.closeDatabase();
				return result;
			}
		}

		return result;
	}

	private Element getUserNode(UserQueryResult userQueryResult){
		Element user_list_node= this.doc.createElement(GSXML.USER_NODE_ELEM+"List");

		Vector userInfo=userQueryResult.users_;

		for (int i=0; i<userQueryResult.getSize(); i++){
			Element user_node= this.doc.createElement(GSXML.USER_NODE_ELEM);
			String username=((UserTermInfo)userInfo.get(i)).username_;
			String password=((UserTermInfo)userInfo.get(i)).password_;
			String groups=((UserTermInfo)userInfo.get(i)).groups_;
			String accountstatus=((UserTermInfo)userInfo.get(i)).accountstatus_;
			String comment=((UserTermInfo)userInfo.get(i)).comment_;
			user_node.setAttribute("umun",username);
			user_node.setAttribute("umpw",password);
			user_node.setAttribute("umgp",groups);
			user_node.setAttribute("umas",accountstatus);
			user_node.setAttribute("umc",comment);

			user_list_node.appendChild(user_node);
		}
		return user_list_node;	
	}

	private Element getCollectList(String collect){
		Element collect_list_node = this.doc.createElement(GSXML.COLLECTION_ELEM+"List");
		File[] collect_dir= (new File(collect)).listFiles();
		if(collect_dir!=null && collect_dir.length > 0){
			for (int i=0;i<collect_dir.length;i++){
				if (collect_dir[i].isDirectory() && (!collect_dir[i].getName().startsWith(".svn"))){
					Element collect_node = this.doc.createElement(GSXML.COLLECTION_ELEM);
					collect_node.setAttribute("name",collect_dir[i].getName());
					collect_list_node.appendChild(collect_node);
				}
			}
		}
		return collect_list_node;
	}
}

