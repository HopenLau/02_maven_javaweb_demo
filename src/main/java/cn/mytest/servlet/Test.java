package cn.mytest.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.css.lt.email.EmailUtil;
import com.css.lt.hcmService.Time;
import com.css.lt.hcmService.TimeBean;
import com.css.lt.hcmService.TimeBean.Category;
import com.css.lt.ldapService.GroupOrg;
import com.css.lt.ldapService.ManagedUser;

public class Test {

	public static String password = "";
	public static void main(String[] args) throws Exception{

		String file_path = "D:" + File.separator + "debug_log_20200918.txt";
		PrintStream tools = new PrintStream(new FileOutputStream(new File(file_path)));

			// ldap connection

			// prod
			ManagedUser ldap = new ManagedUser("101236", password);
			
			// dev
//			ManagedUser ldap = new ManagedUser("opendjuat.css.hk:1389", "dc=css,dc=hk", "95727", "123456");
//	        uat
//			ManagedUser ldap = new ManagedUser("lm-direct.chowsangsang.com","dc=css,dc=hk", "9070", "123456");
//			ManagedUser ldap = new ManagedUser("lm-direct.chowsangsang.com","dc=css,dc=hk","29999","123456");
	        System.out.println(ldap.isBound());
//	        ManagedUser ldap = new ManagedUser("101236",password);
	        
	        
			GroupOrg org;
//			org = new GroupOrg(ldap, "cn=844,ou=dpt,ou=org,ou=groups,dc=css,dc=hk");
			org = new GroupOrg(ldap, "cn=HK,ou=reg,ou=org,ou=groups,dc=css,dc=hk");


			// 用"com.css.lt.hcmService.yyncc.Time"呢個class。
			Time time = new Time("yyncc");
			Calendar nextWeek_Monday = getNextWeekMonday();
			Calendar nextWeek_Sunday = getNextWeekSunday();
			time.getByPeriod(Category.LEAVE, nextWeek_Monday.getTime(), nextWeek_Sunday.getTime());
			Calendar cal_lastYear = Calendar.getInstance();
			cal_lastYear.add(Calendar.YEAR, -1);
			Calendar cal_nextYear = Calendar.getInstance();
			cal_nextYear.add(Calendar.YEAR, 1);
//			time.getByPeriod(Category.LEAVE, cal_lastYear.getTime(), cal_nextYear.getTime());
			
			
			TimeBean timeBean;
			ManagedUser user;
			String user_uid;
			String user_ou;
			ManagedUser manager;
			String manager_dn;
			String manager_ou;
			String timeCode;


			timeBean = time.use(org);

			Iterator<String> iter_timeCode = timeBean.getTimeCodes().iterator();
			while (iter_timeCode.hasNext()) { 
				timeCode = iter_timeCode.next();

				// TimeBean.State.PENDING.name() , TimeBean.State.APPROVED.name()...
				user_uid = timeBean.getEmpId(timeCode);
				user = new ManagedUser(ldap, user_uid);
				user_ou = user.getOu();
				tools.println("user_ou -->" + user_ou + "<--");
				System.out.println("user_ou -->" + user_ou + "<--");
				tools.println("user.getDisplayName() -->" + user.getDisplayName() + "<--");

				manager_dn = user.getManager();
				while (!manager_dn.equals("")) {
					// 如果他有上司。
					manager = new ManagedUser(ldap, manager_dn);
					manager_ou = manager.getOu();
					tools.println("manager_ou -->" + manager_ou + "<--");
					tools.println("manager.getDisplayName() -->" + manager.getDisplayName() + "<--");
					
					manager_dn = manager.getManager();
				}

				tools.println("======================");
				tools.println("");

			}

			ldap.close();

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
