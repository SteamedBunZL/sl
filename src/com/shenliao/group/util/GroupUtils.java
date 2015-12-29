package com.shenliao.group.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.utils.Utils;

public class GroupUtils {
	private static final String TAG = "GroupUtils";

	public static final int SHOW_AD = 100;
	public static final String AD_SETTING = "ad_setting";
	public static final String AD_LASTTIME = "ad_lasttime";
	public static final String REPORT_SETTING_BLOG="report_setting_blog";
	public static final String REPORT_SETTING="report_setting";
	public static final String REPORT_LASTTIME="report_lasttime";
	public static final String REPORT_LASTTIME_BLOG="report_lasttime_blog";
	public static final String  REPORT_UID_SETTING="report_uid_setting";
	public static final String  REPORT_MID_SETTING="report_mid_setting";
	public static  final String RPORT_UID_RESULT="report_uid_";
	public static  final String RPORT_MID_RESULT="report_mid_";
	public static final String  GROUPROOM_FLUSH_IMAGE_PARTNER_ID="grouproom_flush_image_partner_id";
	public static final String  GROUPROOM_FLUSH_IMAGE_PARTNER_ID_SETTING="grouproom_flush_image_partner_id_setting";
	public static final String  GROUP_PUBLIC_ID_LIST="id_list";
	private static SimpleDateFormat curDayFormat;
	private static SimpleDateFormat preDayFormat;
	private static String yearPrompt;
	private static String monthPrompt;
	private static String dayPrompt;

	/**
	 * 
	 * @param txData
	 * @param txGroup
	 * @return bitmap
	 * 获取聊天室头像的方法
	 */
	public static Bitmap getGroupHeadBitmap(Context txData, TxGroup txGroup) {
		String storagePath = Utils.getStoragePath(txData);
		File sddir = new File(storagePath, Utils.AVATAR_PATH);
		if (!sddir.exists() && !sddir.mkdirs()) {
			if (Utils.debug)
				Log.e(TAG,"bitmapFromUrl---Create dir failed");
			sddir.mkdir();
		}
		StringBuffer tempPath = new StringBuffer().append(sddir.getAbsolutePath()).append("/").append(
				"group_" + txGroup.group_id).append(".jpg");
		return BitmapFactory.decodeFile("" + tempPath);
	}

	/**
	 * 
	 * @param time
	 * @return 格式化时间
	 */
	public static String formatTime(long time) {
		Date date = new Date(time);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String formatTime = format.format(date);
		return formatTime;

	}

	/**
	 * 
	 * @param userId
	 * @param ownerId
	 * @param adminIds
	 * @return	0：群主 
	 * 		   	1：管理员 
	 * 		   	2：普通成员
	 */
	public static int userDignity(long userId, long ownerId, String adminIds) {
		if (Utils.debug)Log.i(TAG,"userDignity---userDignity:" + userId + "_" + ownerId + "_" + adminIds);
		if (userId == ownerId)
			return 0;
		String[] ids = adminIds.split("�");
		if (ids != null) {
			for (int i = 0; i < ids.length; i++) {
				if (ids[i].equals(userId + "")) {
					return 1;
				}
			}
		}
		return 2;
	}

	public static boolean inGroup(long userId, String userIds) {
		
		String[] ids = userIds.split("�");
		if (ids != null) {
			for (int i = 0; i < ids.length; i++) {
				if (ids[i].equals(userId + "")) {
					return true;
				}
			}
		}
		return false;
	}

	public static String adminNames(String adminNamesSource) {
		if (adminNamesSource == null)
			return "";
		String[] names = adminNamesSource.split("�");
		String tmpNamesString = "";
		for (int i = 0; i < names.length; i++) {
			tmpNamesString += names[i] + "  ";
		}
		return tmpNamesString;
	}

	public static String dealDate(Long time, Context context) {
		initDateStr(context);
		long currentTime = System.currentTimeMillis();
		// if(time>currentTime)time=time/1000;
		if (("" + time).length() >= 16)
			time = time / 1000;
		Date date = new Date(time);
		long d_count = time / 1000 / 86400;

		long c_count = currentTime / 1000 / 86400;
		long time_ = c_count - d_count;
		String curDate = curDayFormat.format(date);
		if (time_ == 0) {
			return curDate;
		} else {
			int d_year = date.getYear();
			int c_year = new Date(currentTime).getYear();
			int year_ = c_year - d_year;
			if (year_ == 0) {
				preDayFormat = new SimpleDateFormat("MM" + monthPrompt + "dd" + dayPrompt);
			} else {
				preDayFormat = new SimpleDateFormat("yyyy" + yearPrompt + "MM" + monthPrompt + "dd" + dayPrompt);
			}
			String preDate = preDayFormat.format(date) + curDate;
			return preDate;
		}
	}


	public static void initDateStr(Context context) {
		curDayFormat = new SimpleDateFormat("HH:mm");
		yearPrompt = context.getResources().getString(R.string.year_prompt);
		monthPrompt = context.getResources().getString(R.string.month_prompt);
		dayPrompt = context.getResources().getString(R.string.day_prompt);
		if ("-".equals(dayPrompt)) {
			dayPrompt = "";
		}
	}

	public static Bitmap getHeadImage(Context context, String id) {

		String storagePath = Utils.getStoragePath(context);
		File sddir = new File(storagePath, Utils.AVATAR_PATH);
		if (!sddir.exists() && !sddir.mkdirs()) {
			if (Utils.debug)
				Log.e(TAG,"bitmapFromUrl---Create dir failed");
			sddir.mkdir();
		}
		StringBuffer tempPath = new StringBuffer().append(sddir.getAbsolutePath()).append("/").append(id)
				.append(".jpg");
		return BitmapFactory.decodeFile("" + tempPath);
	}

	/**
	 * 弹出对话框
	 * @param context
	 * @param title
	 * @param ok
	 * @param no
	 * @param onclick
	 */
	public static void showDialog(Context context,String title,String content, int ok, int no, DialogInterface.OnClickListener onclick) {

		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(title);
		dialog.setMessage(content);
		dialog.setPositiveButton(ok, onclick);
		dialog.setNegativeButton(no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		dialog.show();
	}
	public static void showChangeFailedDialog(Context context, String content) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(R.string.prompt);
		dialog.setMessage(content);
		
		dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		dialog.show();
	}
	
	
	public static void showDialog(Context context, int content, int ok, int no, DialogInterface.OnClickListener onclick) {

		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(R.string.warn);
		dialog.setMessage(content);
		dialog.setPositiveButton(ok, onclick);
		dialog.setNegativeButton(no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		dialog.show();
	}
	
	public static void showDialog(Context context, int content, int ok, int no,int title, DialogInterface.OnClickListener onclick) {

		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(title);
		//dialog.setMessage(content);
		dialog.setPositiveButton(ok, onclick);
		dialog.setNegativeButton(no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		dialog.show();
	}

	/**
	 * 隐藏光标
	 */
	public static void hidInput() {

	}
}
