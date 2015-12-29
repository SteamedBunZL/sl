package com.tuixin11sms.tx.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * 字符串处理工具类
 * 
 * @author 郭伟洲
 * 
 */
public class StringUtils {
	/**
	 * JSON Array转字符串
	 * 
	 * @param ja
	 *            JSON ARRAY
	 * @return
	 */
	public static String jsonArray2Str(JSONArray ja) {
		StringBuilder sb = new StringBuilder();
		try {
			if (ja != null) {
				for (int i = 0; i < ja.length(); i++) {
					sb.append(ja.get(i).toString()).append(Constants.STRING_SEPARATOR);
				}
			}
		} catch (JSONException e) {
			if(Utils.debug)e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * 字符串转List
	 * 
	 * @param str
	 *            需要转的字符串
	 * @return List
	 */
	public static List<String> str2List(String str) {
		List<String> list = new ArrayList<String>();
		if (!isNull(str)) {
			list = new ArrayList<String>(Arrays.asList(str.split(Constants.STRING_SEPARATOR)));
		}
		return list;
	}

	/**
	 * List转成字符串
	 * 
	 * @param list
	 * @return
	 */
	public static String list2String(List<String> list) {
		StringBuilder sb = new StringBuilder();
		if (list != null) {
			for (int i = 0; i< list.size();i++) {
				sb.append(list.get(i));
				if(i != list.size() -1)
					sb.append(Constants.STRING_SEPARATOR);
			}
		}
		return sb.toString();
	}
	public static String intlist2String(List<Integer> list) {
		StringBuilder sb = new StringBuilder();
		if (list != null) {
			for (int i = 0; i< list.size();i++) {
				sb.append(list.get(i));
				if(i != list.size() -1)
					sb.append(Constants.STRING_SEPARATOR);
			}
		}
		return sb.toString();
	}

	/**
	 * 判断字符串是否NULL或者空
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNull(String str) {
		if (str == null || str.trim().equals(""))
			return true;
		return false;
	}

}
