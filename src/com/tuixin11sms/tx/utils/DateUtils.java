package com.tuixin11sms.tx.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
/**
 * 日期工具类（格式化日期、获取当前时间等）
 * @author 郭伟洲
 *
 */
public class DateUtils {
	/**
	 * 通用日期格式化方法
	 * @param date		传入的日期
	 * @param strFormat	格式化字符串（yyyy年MM月dd日hh时mm分ss秒）
	 * @return
	 */
	public static String dateFormat(Date date , String strFormat){
		if(date != null){
			DateFormat format = new SimpleDateFormat(strFormat);
			return format.format(date);
		}else{
			return "";
		}
	}
	
	/**
	 * 把日期转换为字符串,
	 * 
	 * @param date
	 *            要转换的日期,如果date时间参数为null,默认取当前时间；
	 * @param par
	 *            日期格式,如果参数为null，默认取"yyyy-MM-dd HH:mm:ss"
	 * @return 转换后的字符串
	 * @since 2009-11-9
	 */
	public static String dateToString(Date date, String par) {
		String string = "";

		if (date == null) {
			Calendar cal = Calendar.getInstance();
			date = cal.getTime();
		}
		if (par == null)
			par = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat dateformat = new SimpleDateFormat(par);
		string = dateformat.format(date);
		return string;
	}
	/**
	 * 功能：获得当前时间
	 * @return yyyy-MM-dd HH:mm:ss Date
	 */
	public static Date getCurrentDateTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now = new Date();
		String tmp = sdf.format(now);
		try {
			return sdf.parse(tmp);
		} catch (ParseException e) {
			//
			if(Utils.debug)e.printStackTrace();
			return null;
		}

	}
	/**
	 * 字符串日期转为Date类型
	 * @param strdate  需转换的字符串
	 * @return  转换后的java.util.Date
	 */
	public static Date parseYMDHM (String strdate)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date date = new Date();
		try {
			date = sdf.parse(strdate);
		} catch (ParseException e) {
			//
			if(Utils.debug)e.printStackTrace();
		}
		return date;
	}
	/**
	 * 字符串日期转为Date
	 * @param sDate 需转换的字符串
	 * @param format  格式，如:yyyy-MM-dd HH:mm:ss
	 * @return  java.util.Date
	 */
	public static Date parseDate(String sDate,String format){
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date date = new Date();
		try {
			date = sdf.parse(sDate);
		} catch (ParseException e) {
			if(Utils.debug)e.printStackTrace();
		}
		return date;
	}
	
	
	//把时间毫秒值变为年月日
	public static String parseDateToLocal(Long time) {
		Date date = new Date(time);
		long currentTime = System.currentTimeMillis();
		int d_year = date.getYear();
		int c_year = new Date(currentTime).getYear();
		int year_ = c_year - d_year;
		SimpleDateFormat preDayFormat;
		String yearPrompt = "年", monthPrompt = "月", dayPrompt = "日";
		if (year_ == 0) {
			preDayFormat = new SimpleDateFormat("MM" + monthPrompt + "dd"
					+ dayPrompt + " HH:mm");
		} else {
			preDayFormat = new SimpleDateFormat("yyyy" + yearPrompt + "MM"
					+ monthPrompt + "dd" + dayPrompt + " HH:mm");
		}
		String preDate = preDayFormat.format(date);
		return preDate;
	}
	
}
