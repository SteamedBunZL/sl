package com.tuixin11sms.tx.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.os.Environment;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.utils.Utils;

public class LogUtils {
	
	
	/**
	 * 调用方法
	 * String data = LogUtils.makeLogStr(str);    //形参为字符串
	   String data = LogUtils.makeLogStr(new String[]{});   //形参为字符串数组
       LogUtils.logFileOperate(context, data);
	 */
	/**
	 * 日志文件的创建写入操作
	 */
	public static void logFileOperate(Context context, String data){
		File sddir = createDirectory(context);
		if(sddir!=null){
			File console_file = new File(sddir, "Console.log");
			if(!console_file.exists()){
				try {
					console_file.createNewFile();
				} catch (IOException e) {
					if(Utils.debug)e.printStackTrace();
				}
			}
	    	LogUtils.writeLogToFile(console_file, data);
		}
	}
	/**
	 * 拼装log打印字符串
	 */
	public static String makeLogStr(String str){
		StringBuffer sb = new StringBuffer();
		long time = System.currentTimeMillis();
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String dateFormat = sdf.format(date);
		sb.append(dateFormat).append(" ").append("shenliao["+Utils.apptype+"] ").append(str).append("\r\n")
		.append(str);
		String data = sb.toString();
		return data;
	}
	public static String makeLogStr(String[] strs){
		StringBuffer sb = new StringBuffer();
		long time = System.currentTimeMillis();
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String dateFormat = sdf.format(date);
		int len = strs.length;
		for(int i=0; i<len; i++){
			String picStr = strs[i];
			sb.append(dateFormat).append(" ").append("shenliao["+Utils.apptype+"] ").append(picStr).append("\r\n");
		}
		String data = sb.toString();
		return data;
	}
	/**
	 * 写文件操作
	 */
	public static void writeLogToFile(File file, String data){
		FileOutputStream outStream = null;
		try {
			outStream = new FileOutputStream(file, true);
			String str = data;
			byte[] dataByte = str.getBytes();
			outStream.write(dataByte);
			outStream.flush();
			outStream.close();
		} catch (FileNotFoundException e) {
			if(Utils.debug)e.printStackTrace();
		}catch (IOException e) {
			//
			if(Utils.debug)e.printStackTrace();
		}finally{
			try {
				if(outStream!=null){
					outStream.close();
				}
			} catch (IOException e) {
				if(Utils.debug)e.printStackTrace();
			}
		}
	}
	/**
	 * 创建日志文件目录
	 */
	public static File createDirectory(Context context){
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			String storagePath = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/shenliao/logs";
			File sddir = new File(storagePath);
			if (!sddir.exists()) {
				sddir.mkdirs();
			}
			return sddir;			
		}else{
			return null;
		}
		/*
		if(Utils.storagePath.equals("")){
			Utils.initStoragePath(context);
		}
		File sddir = new File(Utils.storagePath);
		if (!sddir.exists() && !sddir.mkdirs()) {
			return null;
		}
		String storagePath = Utils.storagePath+"/logs";
		File sddir1 = new File(storagePath);
		if (!sddir1.exists()) {
			sddir1.mkdirs();
		}
		return sddir1;*/
	}
//	/**
//	 * 生成log日志文件目录路径
//	 */
//	public static String getDirectoryPath(Context context){
//    	String storagePath;
//		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//			storagePath = Environment.getExternalStorageDirectory()
//					.getAbsolutePath() + "/tuixin11/logs";
//		}else{
//			storagePath = context.getFilesDir().getAbsolutePath()
//					+ "/tuixin11/logs";
//		}
//		return storagePath;
//    }
    /**
     * 记录登录时间，是否为第一次登录
     */
	public static void markDay(Context context){
		SessionManager mSess = SessionManager.getInstance();
//		SharedPreferences prefs = context.getSharedPreferences(CommonData.MEME_PREFS,
//				Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
//		Editor editor = prefs.edit();
		//boolean isInstallDay = prefs.getBoolean(CommonData.IS_INSTALLDAY, false);
//		long dayMark = prefs.getLong(CommonData.DAY_MARK, 0);
		long dayMark = mSess.mPrefMeme.day_mark.getVal();
		
    	long currentTime = System.currentTimeMillis();
		long currentDay = currentTime/1000/86400;
		File sddir = createDirectory(context);
		if(sddir!=null){
			if(dayMark==0){
				//安装后第一次登录
//				editor.putBoolean(CommonData.IS_INSTALLDAY, true);
//				editor.putLong(CommonData.DAY_MARK, currentDay);
//				editor.commit();
				mSess.mPrefMeme.is_installday.setVal(true);
				mSess.mPrefMeme.day_mark.setVal(currentDay).commit();
				
				//CommonData.DAY_MARK = currentDay;
				//CommonData.IS_INSTALLDAY = true;
				
				File console_file = new File(sddir, "Console.log");
				try {
					console_file.createNewFile();
				} catch (IOException e) {
					if(Utils.debug)e.printStackTrace();
				}
			}else{
				//非第一次登录
				if(dayMark == currentDay){
					File console_file = new File(sddir, "Console.log");
					if(!console_file.exists()){
						try {
							console_file.createNewFile();
						} catch (IOException e) {
							if(Utils.debug)e.printStackTrace();
						}
					}
				}else if(dayMark != currentDay){
					//非安装当天登录
//					editor.putBoolean(CommonData.IS_INSTALLDAY, false);
//					editor.putLong(CommonData.DAY_MARK, currentDay);
//					editor.commit();
					
					mSess.mPrefMeme.is_installday.setVal(false);
					mSess.mPrefMeme.day_mark.setVal(currentDay).commit();
					
					File console_file = null; 
					console_file = new File(sddir, "Console.log");
					if(console_file.exists()){
						console_file.delete();
					}
					console_file = new File(sddir, "Console.log");
					try {
						console_file.createNewFile();
					} catch (IOException e) {
						if(Utils.debug)e.printStackTrace();
					}
				}
			}
		}
    }
	
	
	
//	/**
//	 * 创建发送当天的日志文件Console.log
//	 */
//	public static File createConsoleFile(Context context, boolean isInstallDay, long dayMark){
//		//if(CommonData.DAY_MARK!=0){
//		//}
//		File console_file = null; 
//		File sddir = createDirectory(context);
//		if(isInstallDay){
//			return null;
//			//console_file = new File(sddir, "Console.log");
//		}else{
//			long currentTime = System.currentTimeMillis();
//			long currentDay = currentTime/1000/86400;
//			console_file = new File(sddir, "Console.log");
//			if(dayMark!=currentDay){
//				if(console_file.exists()){
//					console_file.delete();
//				}
//				console_file = new File(sddir, "Console.log");
//			}
//			return console_file;
//		}
//	}
//	/**
//	 * 创建安装神聊当天的日志文件Previous.log,由于android Gmail只能发送一个附件，故不用创建此文件
//	 */
//	public static File createPreviousFile(Context context, boolean isInstallDay, long dayMark){
//		//if(CommonData.DAY_MARK!=0){
//		//}
//		File previous_file = null; 
//		File sddir = createDirectory(context);
//		if(isInstallDay){
//			previous_file = new File(sddir, "Previous.log");
//			return previous_file;
//		}else{
//			return null;
//		}
//	}
}
