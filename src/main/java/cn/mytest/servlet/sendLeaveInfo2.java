package cn.mytest.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

public class sendLeaveInfo2 {

	private static String password = "";

	public static void main(String[] args) throws Exception {

		String file_path = "D:" + File.separator + "debug_log_20200918.html";
		PrintStream tools = new PrintStream(new FileOutputStream(new File(file_path)));

		// [William_mark] ===== [Start: ldap connection] =====
		// prod
		ManagedUser ldap = new ManagedUser("101236", password);

		// dev
		// ManagedUser ldap = new ManagedUser("opendjuat.css.hk:1389", "dc=css,dc=hk",
		// "95727", "123456");

		// uat
//      ManagedUser ldap = new ManagedUser("lm-direct.chowsangsang.com","dc=css,dc=hk", "9070", "123456");
//		ManagedUser ldap = new ManagedUser("lm-direct.chowsangsang.com","dc=css,dc=hk","29999","123456");
		System.out.println("ldap.isBound() -->" + ldap.isBound() + "<--");
		// [William_mark] ===== [End: ldap connection] =====
		

		Set<GroupOrg> set_orgs = new HashSet<GroupOrg>();
		
		GroupOrg org;
	

		// 對應於"ou=reg,ou=org,ou=groups,dc=css,dc=hk"下面的管理區。
		GroupOrg HQ = new GroupOrg(ldap, "cn=HQ,ou=reg,ou=org,ou=groups,dc=css,dc=hk");
		System.out.println(HQ.getDisplayName()); // 輸出 "集團總部"。
		// 
		set_orgs.add(HQ);
		System.out.println("HQ.getDisplayName() -->" + HQ.getDisplayName() + "<--");
		 

		GroupOrg HK = new GroupOrg(ldap, "cn=HK,ou=reg,ou=org,ou=groups,dc=css,dc=hk");
		set_orgs.add(HK);
		GroupOrg MO = new GroupOrg(ldap, "cn=MO,ou=reg,ou=org,ou=groups,dc=css,dc=hk");
//		set_orgs.add(MO);
//		GroupOrg C1 = new GroupOrg(ldap, "cn=C1,ou=reg,ou=org,ou=groups,dc=css,dc=hk");
//		set_orgs.add(C1);
//		GroupOrg C2 = new GroupOrg(ldap, "cn=C2,ou=reg,ou=org,ou=groups,dc=css,dc=hk");
//		set_orgs.add(C2);
//		GroupOrg C3 = new GroupOrg(ldap, "cn=C3,ou=reg,ou=org,ou=groups,dc=css,dc=hk");
//		set_orgs.add(C3);
//		GroupOrg C4 = new GroupOrg(ldap, "cn=C4,ou=reg,ou=org,ou=groups,dc=css,dc=hk");
//		set_orgs.add(C4);
//		GroupOrg C5 = new GroupOrg(ldap, "cn=C5,ou=reg,ou=org,ou=groups,dc=css,dc=hk");
//		set_orgs.add(C5);
//		GroupOrg C6 = new GroupOrg(ldap, "cn=C6,ou=reg,ou=org,ou=groups,dc=css,dc=hk");
//		set_orgs.add(C6);

		Map<String, String> map_managerName = new HashMap<String, String>();
		Map<String, List<Map<String, String>>> map_subordinate_leaveInfo = new HashMap<String, List<Map<String, String>>>();
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

		// 用"com.css.lt.hcmService.yyncc.Time"呢個class。
		Time time = new Time("yyncc");

		Calendar nextWeekMonday = getNextWeekMonday();
		Calendar nextWeekSunday = getNextWeekSunday();
		time.getByPeriod(Category.LEAVE, nextWeekMonday.getTime(), nextWeekSunday.getTime());
		String[] arr_leaveType;

		Iterator<GroupOrg> iter_orgs = set_orgs.iterator();
		while (iter_orgs.hasNext()) {
			org = iter_orgs.next();

			TimeBean timeBean = time.use(org);

			Iterator<String> iter_timeCode = timeBean.getTimeCodes().iterator();
			while (iter_timeCode.hasNext()) {
				String timeCode = iter_timeCode.next();

				// TimeBean.State.PENDING.name() , TimeBean.State.APPROVED.name()...

				user_uid = timeBean.getEmpId(timeCode);
				user = new ManagedUser(ldap, user_uid);
				user_ou = user.getOu();

				manager_levelUp = 0;
				manager_dn = user.getManager();
				while (!manager_dn.equals("")) {
					// 如果他有上司。
					manager = new ManagedUser(ldap, manager_dn);
					manager_levelUp++;
					manager_ou = manager.getOu();
					
					if ((manager_levelUp == 1) || (user_ou.equals(manager_ou))) {
						// manager_levelUp == 1，代表直屬上司。
 
						if (map_subordinate_leaveInfo.get(manager_dn) == null) {
							lst_map_userInfo = new ArrayList<Map<String, String>>();
						} else {
							lst_map_userInfo = map_subordinate_leaveInfo.get(manager_dn);
						}
						map_userInfo = new HashMap<String, String>();

						map_userInfo.put("userName", user.getDisplayName());
						arr_leaveType = timeBean.getType(timeCode);
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

						if (timeBean.getQuantityInHours(timeCode) == 0.0) {
							map_userInfo.put("QuantityInHours", "-");
						} else {
							map_userInfo.put("QuantityInHours", String.valueOf(timeBean.getQuantityInHours(timeCode)));
						}

						lst_map_userInfo.add(map_userInfo);
						map_subordinate_leaveInfo.put(manager_dn, lst_map_userInfo);
//						map_managerDn_name.put(manager_dn, pat_numberSpace.matcher(manager.getDisplayName()).replaceFirst(""));
						map_managerName.put(manager_dn, manager.getDisplayName());
						
					} else {
						break;
					}
					manager_dn = manager.getManager();
				}

			}
		}

		ldap.close();


		// [William_mark] ===== [Start: 發出email。] =====
		String email_head = "Dear ";
		String email_tail = "Best regards";
		String str_tabReturn = "<br>";
		String openTab_table = "<table border=\"1\" width=\"700px\" style=\"border-collapse:collapse\">";
		String closeTab_table = "</table>";
		String openTab_tr = "<tr>";
		String closeTab_tr = "</tr>";
		String openTab_td = "<td>";
		String closeTab_td = "</td>";

		String content_html;
//		String str_nextFirstDay = new SimpleDateFormat("MM月dd日").format(getNextWeekMonday().getTime());
//		String str_nextLastDay = new SimpleDateFormat("MM月dd日").format(getNextWeekSunday().getTime());
		String str_nextFirstDay = sdf_date.format(getNextWeekMonday().getTime());
		String str_nextLastDay = sdf_date.format(getNextWeekSunday().getTime());
		
		
		Set<Map.Entry<String, List<Map<String, String>>>> set_me_subordinate_leaveInfo = map_subordinate_leaveInfo.entrySet();

		Iterator<Map.Entry<String, List<Map<String, String>>>> iter_me_subordinate_leaveInfo = set_me_subordinate_leaveInfo.iterator();
		while (iter_me_subordinate_leaveInfo.hasNext()) {
			Map.Entry<String, List<Map<String, String>>> me_lst_map_userInfo = iter_me_subordinate_leaveInfo.next();

			content_html = "";
			content_html += email_head + "<span style=\"color:Blue;\">"
					+ map_managerName.get(me_lst_map_userInfo.getKey()) + ",</span>" + str_tabReturn
					+ str_tabReturn;
			content_html += openTab_table;
			content_html += "<caption>下星期 (" + str_nextFirstDay + " 到 " + str_nextLastDay + ")<br>請假的同事</caption>";
			content_html += "<tr><th>姓名</th><th width=\"120px\">請假類型</th><th width=\"170px\">開始時間</th><th width=\"170px\">結束時間</th><th width=\"50px\">小時</th></tr>";

			lst_map_userInfo = me_lst_map_userInfo.getValue();
			Iterator<Map<String, String>> iter_map_userInfo = lst_map_userInfo.iterator();
			while (iter_map_userInfo.hasNext()) {
				map_userInfo = iter_map_userInfo.next();

				content_html += openTab_tr;
				content_html += openTab_td + map_userInfo.get("userName") + closeTab_td;
				content_html += openTab_td + map_userInfo.get("leaveType") + closeTab_td;
				content_html += openTab_td + map_userInfo.get("startDate") + " " + getDayOfWeek(sdf_date.parse(map_userInfo.get("startDate"))) + "<br>" + map_userInfo.get("startTime") + closeTab_td;
				content_html += openTab_td + map_userInfo.get("endDate") + " " + getDayOfWeek(sdf_date.parse(map_userInfo.get("endDate"))) + "<br>" + map_userInfo.get("endTime") + closeTab_td;
				content_html += openTab_td + map_userInfo.get("QuantityInHours") + closeTab_td;
				content_html += closeTab_tr;

			}
			content_html += closeTab_table;
			content_html += str_tabReturn + email_tail + str_tabReturn + str_tabReturn;
			tools.println("");
			tools.println(content_html);

		}
		// [William_mark] ===== [End: 發出email。] =====
		
		tools.close();

		System.out.println("Done!");

	}


	// 求下星期的星期一。
	public static Calendar getNextWeekMonday() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, (Calendar.MONDAY - cal.get(Calendar.DAY_OF_WEEK)) + 7);
		return cal;
	}
	// 求下星期的星期日。
	public static Calendar getNextWeekSunday() {
		Calendar cal = getNextWeekMonday();
		cal.add(Calendar.DATE, 6);
		return cal;
	}

	public static String getDayOfWeek(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		String[] arr = { "週日", "週一", "週二", "週三", "週四", "週五", "週六" };
		return arr[cal.get(Calendar.DAY_OF_WEEK) - 1];
	}

}







class user{
	String name;
	String leaveType;
	Date startDate;
	Date endDate;
	Date startTime;
	Date endTime;
	double quantityInHours;
	
}





