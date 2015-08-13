package com.ubuntuvim.util;

public class Utils {
	
	/**
	 * 首字母大写
	 * @param c 被转换的字符串
	 * @return 首字母大写后的字符串
	 */
	public static String toUpperCase(String c) {
		char[] cs = c.toCharArray();
		cs[0] -= 32;  //第一个字符转为大写
		return String.valueOf(cs);
	}
	
	/**
	 * 删除最后一个指定的字符
	 * @param s
	 * @return
	 */
	public static String removeLastChar(String s, String removeChar) {
		String tmp = s;
		if (null != s && !"".equals(s) 
			&& s.substring(s.length() - 1, s.length()).equals(removeChar)) {
			return tmp.substring(0, tmp.length() - 1);
		}
		
		return "";
	}
}
