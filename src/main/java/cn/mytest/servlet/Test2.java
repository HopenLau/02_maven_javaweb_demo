package cn.mytest.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.css.lt.hcmService.Time;
import com.css.lt.hcmService.TimeBean;
import com.css.lt.hcmService.TimeBean.Category;
import com.css.lt.ldapService.GroupOrg;
import com.css.lt.ldapService.ManagedUser;

public class Test2 {

	public static String password = "";
	public static void main(String[] args) throws Exception {

		int index = 0;
		String file_path = "D:" + File.separator + "debug_log_20200918.html";
		PrintStream tools = new PrintStream(new FileOutputStream(new File(file_path)));

		// [William_mark] ===== [Start: ldap connection] =====
		// prod
		ManagedUser ldap = new ManagedUser("101236", password);
		
		ManagedUser managedUser = new ManagedUser(ldap, "101235");
		System.out.println("managedUser.getDisplayName() -->" + managedUser.getDisplayName() + "<--");		
		

		// dev
		// ManagedUser ldap = new ManagedUser("opendjuat.css.hk:1389", "dc=css,dc=hk",
		// "95727", "123456");

		// uat
//      ManagedUser ldap = new ManagedUser("lm-direct.chowsangsang.com","dc=css,dc=hk", "9070", "123456");
//		ManagedUser ldap = new ManagedUser("lm-direct.chowsangsang.com","dc=css,dc=hk","29999","123456");
		System.out.println("ldap.isBound() -->" + ldap.isBound() + "<--");
		// [William_mark] ===== [End: ldap connection] =====

		Set<GroupOrg> set_orgs = new HashSet<GroupOrg>();

		// 對應於"ou=reg,ou=org,ou=groups,dc=css,dc=hk"下面的管理區。
		GroupOrg HQ = new GroupOrg(ldap, "cn=HQ,ou=reg,ou=org,ou=groups,dc=css,dc=hk");
		set_orgs.add(HQ);

		GroupOrg HK = new GroupOrg(ldap, "cn=HK,ou=reg,ou=org,ou=groups,dc=css,dc=hk");
		set_orgs.add(HK);
		GroupOrg MO = new GroupOrg(ldap, "cn=MO,ou=reg,ou=org,ou=groups,dc=css,dc=hk");
		set_orgs.add(MO);
		GroupOrg C1 = new GroupOrg(ldap, "cn=C1,ou=reg,ou=org,ou=groups,dc=css,dc=hk");
		set_orgs.add(C1);
		GroupOrg C2 = new GroupOrg(ldap, "cn=C2,ou=reg,ou=org,ou=groups,dc=css,dc=hk");
		set_orgs.add(C2);
		GroupOrg C3 = new GroupOrg(ldap, "cn=C3,ou=reg,ou=org,ou=groups,dc=css,dc=hk");
		set_orgs.add(C3);
		GroupOrg C4 = new GroupOrg(ldap, "cn=C4,ou=reg,ou=org,ou=groups,dc=css,dc=hk");
		set_orgs.add(C4);
		GroupOrg C5 = new GroupOrg(ldap, "cn=C5,ou=reg,ou=org,ou=groups,dc=css,dc=hk");
		set_orgs.add(C5);
		GroupOrg C6 = new GroupOrg(ldap, "cn=C6,ou=reg,ou=org,ou=groups,dc=css,dc=hk");
		set_orgs.add(C6);

		Map<String, String> map_managerDn_name = new HashMap<String, String>();
		Map<String, List<Map<String, String>>> map_managerDn_lst_userInfo = new HashMap<String, List<Map<String, String>>>();
		List<Map<String, String>> lst_map_userInfo;
		Map<String, String> map_userInfo;

		ManagedUser user;
		String user_uid;
		String user_ou;
		int manager_levelUp;
		ManagedUser manager;
		String manager_dn;
		String manager_ou;

		SimpleDateFormat sdf_date = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm");
		Pattern pat_numberSpace = Pattern.compile("^\\d+ ");

		// [William_mark] ===== [Start: 輸出時間。] =====
		// 用"com.css.lt.hcmService.yyncc.Time"呢個class。
		Time time = new Time("yyncc");

		Calendar nextWeekFirstDay = getNextWeekFirstDay();
		Calendar nextWeekLastDay = getNextWeekLastDay();
		time.getByPeriod(Category.LEAVE, nextWeekFirstDay.getTime(), nextWeekLastDay.getTime());
		String[] arr_leaveType;
		double dou_quantityInHours;

		Iterator<GroupOrg> iter_orgs = set_orgs.iterator();
		while (iter_orgs.hasNext()) {
			GroupOrg org = iter_orgs.next();

			TimeBean timeBean = time.use(org);

			timeBean.getTimeCodes();

			Iterator<String> iter_timeCode = timeBean.getTimeCodes().iterator();
			while (iter_timeCode.hasNext()) {
				String timeCode = iter_timeCode.next();

				index++;
				tools.println("-----");
				tools.println("index -->" + index + "<--");
				// leave info
				tools.println("timeCode -->" + timeCode + "<--");
				tools.println("Arrays.toString(timeBean.getType(timeCode)) -->"
						+ Arrays.toString(timeBean.getType(timeCode)) + "<--");
				tools.println("timeBean.getStartDate(timeCode) -->" + timeBean.getStartDate(timeCode) + "<--");
				tools.println("timeBean.getEndDate(timeCode) -->" + timeBean.getEndDate(timeCode) + "<--");
				tools.println("timeBean.getStartTime(timeCode) -->" + timeBean.getStartTime(timeCode) + "<--");
				tools.println("timeBean.getEndTime(timeCode) -->" + timeBean.getEndTime(timeCode) + "<--");
				tools.println(
						"timeBean.getQuantityInDays(timeCode) -->" + timeBean.getQuantityInDays(timeCode) + "<--");
				tools.println(
						"timeBean.getQuantityInHours(timeCode) -->" + timeBean.getQuantityInHours(timeCode) + "<--");

				// TimeBean.State.PENDING.name() , TimeBean.State.APPROVED.name()...
				tools.println(
						"timeBean.getApprovalStatus(timeCode) -->" + timeBean.getApprovalStatus(timeCode) + "<--");

				user_uid = timeBean.getEmpId(timeCode);
				user = new ManagedUser(ldap, user_uid);
				user_ou = user.getOu();
				tools.println("user_uid -->" + user_uid + "<--");
				tools.println("user.getDisplayName() -->" + user.getDisplayName() + "<--");

				manager_levelUp = 0;
				manager_dn = user.getManager();
				while (!manager_dn.equals("")) {
					manager = new ManagedUser(ldap, manager_dn);
					manager_levelUp++;
					manager_ou = manager.getOu();
					if ((manager_levelUp == 1) || (user_ou.equals(manager_ou))) {
						// 求直属上司。
						tools.println("manager_dn -->" + manager_dn + "<--");
						tools.println("manager.getDisplayName() -->" + manager.getDisplayName() + "<--");

						if (map_managerDn_lst_userInfo.get(manager_dn) == null) {
							lst_map_userInfo = new ArrayList<Map<String, String>>();
						} else {
							lst_map_userInfo = map_managerDn_lst_userInfo.get(manager_dn);
						}
						map_userInfo = new HashMap<String, String>();
						// 添加属性。
//							map_userInfo.put("userName", pat_numberSpace.matcher(user.getDisplayName()).replaceFirst(""));
						map_userInfo.put("userName", user.getDisplayName());
						arr_leaveType = timeBean.getType(timeCode);
//							map_userInfo.put("leaveType", Arrays.toString(timeBean.getType(timeCode)));
						map_userInfo.put("leaveType", arr_leaveType[arr_leaveType.length - 1]);
						map_userInfo.put("startDate", sdf_date.format(timeBean.getStartDate(timeCode)));
						map_userInfo.put("endDate", sdf_date.format(timeBean.getEndDate(timeCode)));

						if (timeBean.getStartTime(timeCode) != null) {
							map_userInfo.put("startTime", sdf_time.format(timeBean.getStartTime(timeCode)));
						} else {
							map_userInfo.put("startTime", "-");
						}
						if (timeBean.getEndTime(timeCode) != null) {
							map_userInfo.put("endTime", sdf_time.format(timeBean.getEndTime(timeCode)));
						} else {
							map_userInfo.put("endTime", "-");
						}
						map_userInfo.put("ou", user_ou);

						dou_quantityInHours = timeBean.getQuantityInHours(timeCode);
						if (dou_quantityInHours == 0.0) {
							map_userInfo.put("QuantityInHours", "-");
						} else {
							map_userInfo.put("QuantityInHours", String.valueOf(timeBean.getQuantityInHours(timeCode)));
						}
//							map_userInfo.put("ApprovalStatus", timeBean.getApprovalStatus(timeCode));
						lst_map_userInfo.add(map_userInfo);
						map_managerDn_lst_userInfo.put(manager_dn, lst_map_userInfo);
//						map_managerDn_name.put(manager_dn, pat_numberSpace.matcher(manager.getDisplayName()).replaceFirst(""));
						map_managerDn_name.put(manager_dn,
								manager_ou + ", " + pat_numberSpace.matcher(manager.getDisplayName()).replaceFirst(""));

						if (manager_levelUp > 1) {
							System.out.println("user.getDisplayName() -->" + user.getDisplayName() + "<--");
						}

					} else {
						break;
					}
					manager_dn = manager.getManager();
				}

				tools.println("-----");
			}
		}
		// [William_mark] ===== [End: 輸出時間。] =====

//        String uid2 = "94716";
//        ManagedUser user2 = new ManagedUser(ldap,uid2);

//        //dn: uid=9070,ou=users,dc=css,dc=hk
//        String managerDn = user.getManager();
//        ManagedUser manager = new ManagedUser(ldap,managerDn);
//       
//        //dept:
//        tools.println(user2.getDisplayName() + " ,dept: " +user2.getOu());
//        tools.println(manager.getDisplayName() + " ,dept: " +manager.getOu());

		tools.println("Done!");
		System.out.println("Done!");

		ldap.close();

//		EmailUtil.sendToEmployeeWithHtml(receiptantUid,title, htmlMail, false);

		// [William_mark] ===== [Start: 發出email。] =====
		tools = new PrintStream(new FileOutputStream(new File(file_path)));
		String email_head = "Dear ";
		String email_tail = "Best regards";
		String str_tabReturn = "<br>";
		String openTab_table = "<table border=\"1\" width=\"600px\" style=\"border-collapse:collapse\">";
		String closeTab_table = "</table>";
		String openTab_tr = "<tr>";
		String closeTab_tr = "</tr>";
		String openTab_td = "<td>";
		String closeTab_td = "</td>";

		String content_html;
		String str_nextFirstDay = new SimpleDateFormat("MM月dd日").format(getNextWeekFirstDay().getTime());
		String str_nextLastDay = new SimpleDateFormat("MM月dd日").format(getNextWeekLastDay().getTime());

		Set<Map.Entry<String, List<Map<String, String>>>> set_me_lst_managerDn_userInfo = map_managerDn_lst_userInfo
				.entrySet();

		Iterator<Map.Entry<String, List<Map<String, String>>>> iter_me_lst_managerDn_userInfo = set_me_lst_managerDn_userInfo
				.iterator();
		while (iter_me_lst_managerDn_userInfo.hasNext()) {
			Map.Entry<String, List<Map<String, String>>> me_lst_managerDn_userInfo = iter_me_lst_managerDn_userInfo
					.next();

			content_html = "";
			content_html += email_head + "<span style=\"color:Blue;\">"
					+ map_managerDn_name.get(me_lst_managerDn_userInfo.getKey()) + ",</span>" + str_tabReturn
					+ str_tabReturn;
			content_html += openTab_table;
			content_html += "<caption>下星期 ( " + str_nextFirstDay + " to " + str_nextLastDay + ")<br>請假的同事</caption>";
//			content_html += "<tr><th>姓名</th><th>請假類型</th><th>開始日期</th><th>結束日期</th><th>開始時間</th><th>結束時間</th><th>請假天數</th><th>請假小時數</th><th>status</th></tr>";
//			content_html += "<tr><th>姓名</th><th>請假類型</th><th>開始時間</th><th>結束時間</th><th>請假小時數</th></tr>";
			content_html += "<tr><th>姓名</th><th>請假類型</th><th>開始時間()</th><th>結束時間()</th><th>請假小時數</th><th>ou</th></tr>";

			lst_map_userInfo = me_lst_managerDn_userInfo.getValue();
			Iterator<Map<String, String>> iter_map_userInfo = lst_map_userInfo.iterator();
			while (iter_map_userInfo.hasNext()) {
				map_userInfo = iter_map_userInfo.next();

				content_html += openTab_tr;
				content_html += openTab_td + map_userInfo.get("userName") + closeTab_td;
				content_html += openTab_td + map_userInfo.get("leaveType") + closeTab_td;
				content_html += openTab_td + map_userInfo.get("startDate") + " (" + getDayOfWeek(new SimpleDateFormat("yyyy-MM-dd").parse(map_userInfo.get("startDate"))) + ")" + "<br>" + map_userInfo.get("startTime")
						+ closeTab_td;
				content_html += openTab_td + map_userInfo.get("endDate") + " (" + getDayOfWeek(new SimpleDateFormat("yyyy-MM-dd").parse(map_userInfo.get("endDate"))) + ")" + "<br>" + map_userInfo.get("endTime")
				+ closeTab_td;
				content_html += openTab_td + map_userInfo.get("QuantityInHours") + closeTab_td;
				content_html += openTab_td + map_userInfo.get("ou") + closeTab_td;
				content_html += closeTab_tr;

			}
			content_html += closeTab_table;
			content_html += str_tabReturn + email_tail + str_tabReturn + str_tabReturn;
			tools.println("");
			tools.println(content_html);

		}
		// [William_mark] ===== [End: 發出email。] =====

		tools.close();

		tools.println("Done!");
		System.out.println("Done!");

	}

//	public static Calendar getNextWeekFirstDay() {
//		Calendar cal = Calendar.getInstance();
//		cal.add(Calendar.WEEK_OF_MONTH, 1);
//		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
//		cal.set(Calendar.HOUR_OF_DAY, 0);
//		cal.set(Calendar.MINUTE, 0);
//		cal.set(Calendar.SECOND, 0);
//		return cal;
//	}
//
//	public static Calendar getNextWeekLastDay() {
//		Calendar cal = Calendar.getInstance();
//		cal.setFirstDayOfWeek(Calendar.MONDAY);
//		cal.add(Calendar.WEEK_OF_MONTH, 1);
//		cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
//		cal.set(Calendar.HOUR_OF_DAY, 24);
//		cal.set(Calendar.MINUTE, 0);
//		cal.set(Calendar.SECOND, 0);
//		return cal;
//	}

// 获得当前日期与本周一相差的天数   
	private static int getMondayPlus() {
		Calendar cd = Calendar.getInstance();
		// 获得今天是一周的第几天，星期日是第一天，星期一是第二天......
		int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK) - 1; // 因为按中国礼拜一作为第一天所以这里减1

		if (dayOfWeek == 1) {
			return 0; // 如果是周一 返回0(无相差天数)
		} else {
			// 返回与周一相差的天数(用以当天和相差天数计算)
			// 如果今天为周五 首先dayOfWeek == 5, 1-5则返回-4,当前减去4天就为周一的日期
			return 1 - dayOfWeek;
		}
	}

// 求下星期嘅星期一。
	public static Calendar getNextWeekFirstDay() {

		int mondayPlus = getMondayPlus();

		GregorianCalendar currentDate = new GregorianCalendar();

		currentDate.add(GregorianCalendar.DATE, mondayPlus + 7);// 本周周一的天数加7天

		return currentDate;
	}

// 求下星期嘅星期日。
	public static Calendar getNextWeekLastDay() {

		int mondayPlus = getMondayPlus();

		GregorianCalendar currentDate = new GregorianCalendar();

		currentDate.add(GregorianCalendar.DATE, mondayPlus + 6 + 7);

		return currentDate;
	}

	public static String getDayOfWeek(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		String[] arr = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
		
		return arr[cal.get(Calendar.DAY_OF_WEEK) - 1];

	}

}