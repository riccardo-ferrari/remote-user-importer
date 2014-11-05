package com.liferay.kickstart.soap.remote.user.importer;

import au.com.bytecode.opencsv.CSVReader;

import com.liferay.client.soap.portal.model.UserGroupRoleSoap;
import com.liferay.client.soap.portal.model.UserSoap;
import com.liferay.client.soap.portal.service.ServiceContext;
import com.liferay.client.soap.portal.service.http.UserServiceSoap;
import com.liferay.client.soap.portal.service.http.UserServiceSoapServiceLocator;
import com.liferay.client.soap.portlet.expando.service.http.ExpandoValueServiceSoap;
import com.liferay.client.soap.portlet.expando.service.http.ExpandoValueServiceSoapServiceLocator;

import java.io.FileReader;
import java.net.URL;

/**
 * Hello world!
 *
 */
public class CliUserUploaderFromCSV 
{
	private static final String PASSWORD = "tester";
	private static final String SCREEN_NAME = "joebloggs";
	private static final long COMPANY_ID = 10155;

	private static URL _getURL(String remoteUser, String password,
			String serviceName, boolean authenicate)
					throws Exception {
		//Unauthenticated url
		String url = "http://localhost:8080/api/axis/" + serviceName;

		//Authenticated url
		if (authenicate) {
			url = "http://" + remoteUser + ":" + password +
					"@localhost:8080/api/axis/" + serviceName;
		}
		return new URL(url);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String sourceFile = args[0];
		System.out.println("Start");


		try {
			UserServiceSoapServiceLocator userServiceSoapService = new UserServiceSoapServiceLocator();
			ExpandoValueServiceSoapServiceLocator expandoValueServiceSoapService = new ExpandoValueServiceSoapServiceLocator();

			UserServiceSoap userServiceSoap = userServiceSoapService.getPortal_UserService(_getURL(SCREEN_NAME, PASSWORD, "Portal_UserService", true));
			ExpandoValueServiceSoap expandoServiceSoap = expandoValueServiceSoapService.getPortlet_Expando_ExpandoValueService(_getURL(SCREEN_NAME, PASSWORD, "Portlet_Expando_ExpandoValueService", true));

			//Portal_UserServiceSoapBindingStub userServiceSoap = new Portal_UserServiceSoapBindingStub((Service) userServiceSoapService.getPortal_UserService());


			CSVReader reader = new CSVReader(new FileReader(sourceFile), ',', '"', 1);
			String [] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				// nextLine[] is an array of values from the line

				long companyId = COMPANY_ID;
				boolean autoPassword = Boolean.getBoolean(nextLine[0]);
				String password1 = nextLine[1];
				String password2 = nextLine[2];
				boolean autoScreenName = Boolean.getBoolean(nextLine[3]);;
				String screenName = nextLine[4];
				String emailAddress = nextLine[5];
				long facebookId = Long.parseLong(nextLine[6]);
				String openId = nextLine[7];
				String locale = nextLine[8];
				String firstName = nextLine[9];
				String middleName = nextLine[10];
				String lastName = nextLine[11];
				int prefixId = 0;
				int suffixId = 0;
				boolean male = Boolean.getBoolean(nextLine[14]);
				int birthdayMonth = Integer.parseInt(nextLine[15]);
				int birthdayDay = Integer.parseInt(nextLine[16]);
				int birthdayYear = Integer.parseInt(nextLine[17]);
				String jobTitle = nextLine[18];
				long[] groupIds = null;
				long[] organizationIds = null;
				long[] roleIds = null;
				long[] userGroupIds = null;
				boolean sendEmail = false;

				ServiceContext serviceContext = new ServiceContext();
				//approve user
				serviceContext.setWorkflowAction(1);
/*
				HashMap<String, String> expandoBridgeMap = new HashMap<String,String>();
				expandoBridgeMap.put("test", nextLine[24]);
				serviceContext.setExpandoBridgeAttributes(expandoBridgeMap);
*/
				UserSoap user = null;
				try {
					user = userServiceSoap.getUserByScreenName(COMPANY_ID, screenName);
				} catch (Exception e){
					//NOP
				}

				if (user != null){
					long userId = user.getUserId();
					String oldPassword = null;
					String newPassword1 = null;
					String newPassword2 = null;
					boolean passwordReset = false;
					String reminderQueryQuestion = user.getReminderQueryQuestion();
					String reminderQueryAnswer = user.getReminderQueryAnswer();
					String languageId = user.getLanguageId();
					String timeZoneId = user.getTimeZoneId();
					String greeting = user.getGreeting();
					String comments = user.getComments();
					String smsSn = "";
					String aimSn = "";
					String facebookSn = "";
					String icqSn = "";
					String jabberSn = "";
					String msnSn = "";
					String mySpaceSn = "";
					String skypeSn = "";
					String twitterSn = "";
					String ymSn = "";
					UserGroupRoleSoap[] userGroupRoles = new UserGroupRoleSoap[]{};
					
					System.out.println("Update user "+ screenName);

					user = userServiceSoap.updateUser(userId,
							oldPassword,
							newPassword1,
							newPassword2,
							passwordReset,
							reminderQueryQuestion,
							reminderQueryAnswer,
							screenName,
							emailAddress,
							facebookId,
							openId,
							languageId,
							timeZoneId,
							greeting,
							comments,
							firstName,
							middleName,
							lastName,
							prefixId,
							suffixId,
							male,
							birthdayMonth,
							birthdayDay,
							birthdayYear,
							smsSn,
							aimSn,
							facebookSn,
							icqSn,
							jabberSn,
							msnSn,
							mySpaceSn,
							skypeSn,
							twitterSn,
							ymSn,
							jobTitle,
							groupIds,
							organizationIds,
							roleIds,
							userGroupRoles,
							userGroupIds,
							serviceContext);
				} else {
					System.out.println("Add user "+ screenName);
					user = userServiceSoap.addUser(companyId,
							autoPassword,
							password1,
							password2,
							autoScreenName,
							screenName,
							emailAddress,
							facebookId,
							openId,
							locale,
							firstName,
							middleName,
							lastName,
							prefixId,
							suffixId,
							male,
							birthdayMonth,
							birthdayDay,
							birthdayYear,
							jobTitle,
							groupIds,
							organizationIds,
							roleIds,
							userGroupIds,
							sendEmail,
							serviceContext);
				}

				String className= "com.liferay.portal.model.User";
				String tableName = "CUSTOM_FIELDS";
				String columnName = SCREEN_NAME;
				long classPK = user.getPrimaryKey();
				String data = nextLine[24];
				
				try {					
					expandoServiceSoap.addValue(companyId, className, tableName, columnName, classPK, data);
				} catch (Exception e){
					System.err.println(e.getMessage() + "Not able to update user custom field");
				}

			}
			reader.close();

		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		System.out.println("Complete");
	}
}
