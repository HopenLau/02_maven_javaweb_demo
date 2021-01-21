package cn.mytest.servlet;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;

import com.css.lt.ldapService.ManagedUser;

public class Test3 {

	public static String password = "";

	public static void main(String[] args) throws Exception {
		LocalDateTime date;
		StringBuffer sb_errorMsg;
		sb_errorMsg = new StringBuffer();
		System.out.println("sb_errorMsg.toString().isEmpty() -->" + sb_errorMsg.toString().isEmpty() + "<--");
	}

	public void a() {
		boolean returnTrue = StringUtils.isBlank(" ");// 返回的是true，
		System.out.println(StringUtils.isNumeric("123"));
	}

	public void testLdap() {
		ManagedUser ldap = new ManagedUser("101236", password);
	}
}
