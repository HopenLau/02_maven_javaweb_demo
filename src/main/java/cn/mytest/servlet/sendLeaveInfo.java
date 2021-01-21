package cn.mytest.servlet;


import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.css.lt.hcmService.Time;
import com.css.lt.hcmService.TimeBean;
import com.css.lt.hcmService.TimeBean.Category;
import com.css.lt.ldapService.GroupOrg;
import com.css.lt.ldapService.ManagedUser;

public class sendLeaveInfo {

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
//		set_orgs.add(new GroupOrg(ldap, "cn=HQ,ou=reg,ou=org,ou=groups,dc=css,dc=hk"));
		set_orgs.add(new GroupOrg(ldap, "cn=HK,ou=reg,ou=org,ou=groups,dc=css,dc=hk"));
//		set_orgs.add(new GroupOrg(ldap, "cn=MO,ou=reg,ou=org,ou=groups,dc=css,dc=hk"));
//		set_orgs.add(new GroupOrg(ldap, "cn=C1,ou=reg,ou=org,ou=groups,dc=css,dc=hk"));
//		set_orgs.add(new GroupOrg(ldap, "cn=C2,ou=reg,ou=org,ou=groups,dc=css,dc=hk"));
//		set_orgs.add(new GroupOrg(ldap, "cn=C3,ou=reg,ou=org,ou=groups,dc=css,dc=hk"));
//		set_orgs.add(new GroupOrg(ldap, "cn=C4,ou=reg,ou=org,ou=groups,dc=css,dc=hk"));
//		set_orgs.add(new GroupOrg(ldap, "cn=C5,ou=reg,ou=org,ou=groups,dc=css,dc=hk"));
//		set_orgs.add(new GroupOrg(ldap, "cn=C6,ou=reg,ou=org,ou=groups,dc=css,dc=hk"));

		Map<String, String> map_managerName = new HashMap<String, String>();
		Map<String, Set<Subordinate>> map_setSubordinate = new HashMap<String, Set<Subordinate>>();
		Set<Subordinate> set_subordinate;
		Subordinate subordinate;
		
		ManagedUser user;
		String user_uid;
		String user_ou;
		int levelDiff;
		ManagedUser manager;
		String manager_dn;
		String manager_ou;

		SimpleDateFormat sdf_date = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm");
		Pattern pat_numberSpace = Pattern.compile("^\\d+ ");

		// 用"com.css.lt.hcmService.yyncc.Time"呢個class。
		Time time = new Time("yyncc");

		Calendar nextWeek_Monday = getNextWeekMonday();
		Calendar nextWeek_Sunday = getNextWeekSunday();
		time.getByPeriod(Category.LEAVE, nextWeek_Monday.getTime(), nextWeek_Sunday.getTime());
		TimeBean timeBean;
		String timeCode;
		String[] arr_leaveType;

		Iterator<GroupOrg> iter_orgs = set_orgs.iterator();
		while (iter_orgs.hasNext()) {
			org = iter_orgs.next();

			timeBean = time.use(org);

			Iterator<String> iter_timeCode = timeBean.getTimeCodes().iterator();
			while (iter_timeCode.hasNext()) { 
				timeCode = iter_timeCode.next();

				// TimeBean.State.PENDING.name() , TimeBean.State.APPROVED.name()...
				user_uid = timeBean.getEmpId(timeCode);
				user = new ManagedUser(ldap, user_uid);
				user_ou = user.getOu();

				levelDiff = 0;
				manager_dn = user.getManager();
				while (!manager_dn.equals("")) {
					// 如果他有上司。
					manager = new ManagedUser(ldap, manager_dn);
					levelDiff++;
					manager_ou = manager.getOu();
					
					if ((levelDiff == 1) || (user_ou.equals(manager_ou))) {
						// manager_levelUp == 1，代表直屬上司。
 
						if (map_setSubordinate.get(manager_dn) == null) {
							set_subordinate = new TreeSet<Subordinate>();
						} else {
							set_subordinate = map_setSubordinate.get(manager_dn);
						}
						subordinate = new Subordinate();

						subordinate.levelDiff = levelDiff;
						subordinate.name = user.getDisplayName();
						arr_leaveType = timeBean.getType(timeCode);
						subordinate.leaveType = arr_leaveType[arr_leaveType.length - 1];
						subordinate.startDate = timeBean.getStartDate(timeCode);
						subordinate.endDate = timeBean.getEndDate(timeCode);
						subordinate.startTime = timeBean.getStartTime(timeCode);
						subordinate.endTime = timeBean.getEndTime(timeCode);
						subordinate.quantityInHours = timeBean.getQuantityInHours(timeCode);
						subordinate.quantityInDays = timeBean.getQuantityInDays(timeCode);

						set_subordinate.add(subordinate);
						map_setSubordinate.put(manager_dn, set_subordinate);
						map_managerName.put(manager_dn, pat_numberSpace.matcher(manager.getDisplayName()).replaceFirst(""));
//						map_managerName.put(manager_dn, manager.getDisplayName());
						
					} else {
						break;
					}
					manager_dn = manager.getManager();
				}

			}
		}

		ldap.close();


		// [William_mark] ===== [Start: 生成email。] =====
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
		String str_nextFirstDay = sdf_date.format(getNextWeekMonday().getTime());
		String str_nextLastDay = sdf_date.format(getNextWeekSunday().getTime());
		Map.Entry<String, Set<Subordinate>> me_setSubordinate;
		Iterator<Subordinate> iter_subordinate;
		
		Set<Map.Entry<String, Set<Subordinate>>> set_me_setSubordinate = map_setSubordinate.entrySet();

		Iterator<Map.Entry<String, Set<Subordinate>>> iter_me_setSubordinate = set_me_setSubordinate.iterator();
		while (iter_me_setSubordinate.hasNext()) {
			me_setSubordinate = iter_me_setSubordinate.next();
			
			
//			if(map_managerName.get(me_setSubordinate.getKey()).equals("109 杜思允 Wallace")) {
//				System.out.println(map_managerName.get(me_setSubordinate.getKey()));
//			}

			content_html = "";
			content_html += email_head + map_managerName.get(me_setSubordinate.getKey()) + "," + str_tabReturn + str_tabReturn;
			content_html += openTab_table;
			content_html += "<caption>下星期 (" + str_nextFirstDay + " 到 " + str_nextLastDay + ")<br>請假的同事</caption>";
			content_html += "<tr><th width=\"190px\">姓名</th><th width=\"130px\">事件</th><th width=\"160px\">開始時間</th><th width=\"160px\">結束時間</th><th width=\"60px\">長度</th></tr>";

			set_subordinate = me_setSubordinate.getValue();
			iter_subordinate = set_subordinate.iterator();
			while (iter_subordinate.hasNext()) {
				subordinate = iter_subordinate.next();
				
				content_html += openTab_tr;
				content_html += openTab_td + subordinate.name + closeTab_td;
				content_html += openTab_td + subordinate.leaveType + closeTab_td;
				
				if(subordinate.startTime!=null) {
					content_html += openTab_td + sdf_date.format(subordinate.startDate) + " " + getDayOfWeek(subordinate.startDate) + "<br>" + sdf_time.format(subordinate.startTime) + closeTab_td;
				} else {
					content_html += openTab_td + sdf_date.format(subordinate.startDate) + " " + getDayOfWeek(subordinate.startDate) + "<br>" + "-" + closeTab_td;
				}
				if(subordinate.endDate!=null) {
					content_html += openTab_td + sdf_date.format(subordinate.endDate) + " " + getDayOfWeek(subordinate.endDate) + "<br>" + sdf_time.format(subordinate.endDate) + closeTab_td;
				} else {
					content_html += openTab_td + sdf_date.format(subordinate.endDate) + " " + getDayOfWeek(subordinate.endDate) + "<br>" + "-" + closeTab_td;
				}
				
				if (subordinate.quantityInHours != 0.0) {
					content_html += openTab_td + String.valueOf(subordinate.quantityInHours) + "<br>小時" + closeTab_td;
				} else {
					content_html += openTab_td + String.valueOf(subordinate.quantityInDays) + "<br>天" + closeTab_td;
				}
				
				content_html += closeTab_tr;

			}
			content_html += closeTab_table;
			content_html += str_tabReturn + email_tail + str_tabReturn + str_tabReturn;
			tools.println("");
			tools.println(content_html);

		}
		// [William_mark] ===== [End: 生成email。] =====
		
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





class Subordinate implements Comparable<Subordinate> {

	int levelDiff;
	String name;
	String leaveType;
	Date startDate;
	Date endDate;
	Date startTime;
	Date endTime;
	double quantityInHours;
	double quantityInDays;
	
	

	@Override
	public int compareTo(Subordinate subordinate) {
		// 首先 按上司等級排序。
		if (this.levelDiff > subordinate.levelDiff) {
			return 1;
		} else if (this.levelDiff < subordinate.levelDiff) {
			return -1;
		} else {
			if (this.name.compareTo(subordinate.name) > 0) {
				return 1;
			} else if (this.name.compareTo(subordinate.name) < 0) {
				return -1;
			} else {
				if(this.startDate.compareTo(subordinate.startDate) !=0) {
					return this.startDate.compareTo(subordinate.startDate);
				}else {
					return this.endDate.compareTo(subordinate.endDate);
				}
			}
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
		result = prime * result + ((leaveType == null) ? 0 : leaveType.hashCode());
		result = prime * result + levelDiff;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		long temp;
		temp = Double.doubleToLongBits(quantityInHours);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
		result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Subordinate other = (Subordinate) obj;
		if (endDate == null) {
			if (other.endDate != null)
				return false;
		} else if (!endDate.equals(other.endDate))
			return false;
		if (endTime == null) {
			if (other.endTime != null)
				return false;
		} else if (!endTime.equals(other.endTime))
			return false;
		if (leaveType == null) {
			if (other.leaveType != null)
				return false;
		} else if (!leaveType.equals(other.leaveType))
			return false;
		if (levelDiff != other.levelDiff)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (Double.doubleToLongBits(quantityInHours) != Double.doubleToLongBits(other.quantityInHours))
			return false;
		if (startDate == null) {
			if (other.startDate != null)
				return false;
		} else if (!startDate.equals(other.startDate))
			return false;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		return true;
	}

}

