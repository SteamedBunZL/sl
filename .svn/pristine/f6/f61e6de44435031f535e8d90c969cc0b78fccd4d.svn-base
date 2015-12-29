package com.tuixin11sms.tx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONException;

import weibo4android.org.json.JSONObject;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.net.HttpHelper;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.task.FileTransfer.DownUploadListner;
import com.tuixin11sms.tx.task.FileTransfer.FileTaskInfo;
import com.tuixin11sms.tx.utils.Utils;
import com.weibo.net.AccessToken;
import com.weibo.net.AsyncWeiboRunner.RequestListener;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboException;

public class ShenliaoOtherLoginService extends Service {
	private static final String TAG = ShenliaoOtherLoginService.class.getSimpleName();
	int loginState;
	AccessToken sinaToken;
	
//	private SharedPreferences prefs = null;
//	private TxData txData;
//	private Editor editor;
	public static Handler mHandler;
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	@Override
	public void onCreate() {
		super.onCreate();
//		txData = (TxData) getApplication();
//		prefs = txData.getSharedPreferences(PrefsMeme.MEME_PREFS, Context.MODE_WORLD_READABLE
//				+ Context.MODE_WORLD_WRITEABLE);
//		editor = prefs.edit();
	}
	
	public static void registerHandler(Handler handler){
		mHandler = handler;
	}


	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
//		String token = prefs.getString(CommonData.WEIBO_TOKEN, "");
//		String tokenSecret = prefs.getString(CommonData.WEIBO_TOKEN_SECRET, "");
//		final String weiboUserId = prefs.getString(CommonData.WEIBO_USER_ID, "");
		String token = mSess.mPrefMeme.weibo_token.getVal();
		String tokenSecret = mSess.mPrefMeme.weibo_token_secret.getVal();
		final String weiboUserId = mSess.mPrefMeme.weibo_user_id.getVal();
		final Weibo weibo = Weibo.getInstance();
		
		AccessToken accessToken = new AccessToken(token, tokenSecret);
	    weibo.setAccessToken(accessToken);
		
		Utils.executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					weibo.showUser(ShenliaoOtherLoginService.this, weibo, Weibo.getAppKey(), weiboUserId, new ShowUserListener());
				} catch (WeiboException e) {
//					editor.putString(CommonData.NICKNAME, ShenliaoOtherLoginService.this.getString(R.string.login_sina_unknow)).commit();
					mSess.mPrefMeme.nickname.setVal(ShenliaoOtherLoginService.this.getString(R.string.login_sina_unknow)).commit();
					TX.tm.reloadTXMe();//////
				}
				//不管是否获得新浪数据 都为true  如果获取失败的话，显示默认的新浪微博用户名===》新浪微博用户
				Utils.SINA_SUCCESS_SYNC_DATA = true;
//				if(mHandler != null){
//					mHandler.sendEmptyMessage(UserInfoActivity.FLUSH_ADAPTER);
//				}
//				if(txData != null){
//					if(txData.outtime2 != null){
//						txData.outtime2.cancel();
//					}
//					if(txData.progress2 != null){
//						txData.progress2.cancel();
//					}
					
					//向神聊服务器上传新浪微博资料
					mSess.getSocketHelper().sendUserSinaInforChange();
//				}
				
				
				ShenliaoOtherLoginService.this.stopSelf();
			}
		});
	}
	
	private class ShowUserListener implements RequestListener{

		@Override
		public void onComplete(String response) {
			try{
				JSONObject json = new JSONObject(response);
				String sex = json.getString("gender");
				String des = json.getString("description");
				String nickName = json.getString("screen_name");
				String loc = json.getString("location");
				String picUrl = json.getString("profile_image_url");
				initSinaUser(nickName,loc,des,sex);
//				{"id":1409928370,"idstr":"1409928370","screen_name":"最后问你个问题",
//				"name":"最后问你个问题","province":"33","city":"3","location":"浙江 温州",
//				"description":"时间是块磨刀石，平了山峰，蔫了黄瓜，残了菊花。",
//				"url":"","profile_image_url":"http://tp3.sinaimg.cn/1409928370/50/1285662228/1",
//				"profile_url":"437308850","domain":"yumingyougexing","weihao":"437308850",
//				"gender":"m","followers_count":200,"friends_count":302,"statuses_count":2407,
//				"favourites_count":114,"created_at":"Sat Apr 24 07:23:15 +0800 2010",
//				"following":false,"allow_all_act_msg":false,"geo_enabled":true,
//				"verified":false,"verified_type":-1,"remark":"",
//				"status":{"created_at":"Mon Aug 20 11:05:07 +0800 2012","id":3481056088926103,"mid":"3481056088926103","idstr":"3481056088926103","text":"//@微天下: 转发微博","source":"<a href=\"http://weibo.com/\" rel=\"nofollow\">新浪微博</a>","favorited":false,"truncated":false,"in_reply_to_status_id":"","in_reply_to_user_id":"","in_reply_to_screen_name":"","geo":null,"retweeted_status":{"created_at":"Mon Aug 20 10:50:16 +0800 2012","id":3481052348132758,"mid":"3481052348132758","idstr":"3481052348132758","text":"快讯：法新社报道，英国驻华使馆在一份声明中，对判决表示欢迎。","source":"<a href=\"http://media.weibo.com\" rel=\"nofollow\">媒体版微博</a>","favorited":false,"truncated":false,"in_reply_to_status_id":"","in_reply_to_user_id":"","in_reply_to_screen_name":"","geo":null,"user":{"id":2641686425,"idstr":"2641686425","screen_name":"新国际","name":"新国际","province":"11","city":"2","location":"北京 西城区","description":"国际新闻直通车——快捷、权威、灵通、快乐。","url":"","profile_image_url":"http://tp2.sinaimg.cn/2641686425/50/5630890050/0","profile_url":"topnewsinternational","domain":"topnewsinternational","weihao":"","gender":"f","followers_count":192038,"friends_count":226,"statuses_count":5188,"favourites_count":4,"created_at":"Thu Feb 23 20:15:21 +0800 2012","following":false,"allow_all_act_msg":false,"geo_enabled":true,"verified":true,"verified_type":3,"allow_all_comment":true,"avatar_large":"http://tp2.sinaimg.cn/2641686425/180/5630890050/0","verified_reason":"新华社国际新闻即时播报","follow_me":false,"online_status":0,"bi_followers_count":160,"lang":"zh-cn"},"reposts_count":0,"comments_count":0,"mlevel":0,"visible":{"type":0,"list_id":0}},"reposts_count":0,"comments_count":0,"mlevel":0,"visible":{"type":0,"list_id":0}},"allow_all_comment":true,"avatar_large":"http://tp3.sinaimg.cn/1409928370/180/1285662228/1","verified_reason":"","follow_me":false,"online_status":1,"bi_followers_count":73,"lang":"zh-cn"}

				if(!Utils.isNull(picUrl)){
					String local = HttpHelper.getInstance(ShenliaoOtherLoginService.this).downSinaAvatar(picUrl,0);
					if(!Utils.isNull(local)){
						
						Drawable draw = Drawable.createFromPath(local);
						
						if(draw != null){
							Bitmap bitmap = ((BitmapDrawable) draw).getBitmap(); 
							Bitmap smallDstBitmap=Utils.ResizeBitmap(bitmap, 92);
			    			Bitmap bigdstBitmap=Utils.ResizeBitmap(bitmap, 400);
			    			createFileImage(smallDstBitmap,bigdstBitmap);
						}
					}
				}
			}catch (Exception e) {
			}
			
		}

		@Override
		public void onError(WeiboException e) {
			
		}

		@Override
		public void onIOException(IOException e) {
			
		}
		
	}
	
	
	
	
	SessionManager mSess = SessionManager
			.getInstance();
	
	private void createFileImage(final Bitmap Sbp,final Bitmap Bbp) {
		String tempImgPath = Utils.getStoragePath(this.getApplicationContext()) + File.separator
				+ Utils.IMAGE_PATH + File.separator + System.currentTimeMillis() + ".jpg";
		try {
			// 生成合成的文件
			mSess.mDownUpMgr.getCompoundImgFile(tempImgPath, Sbp,
					Bbp);
		} catch (IOException e) {
			if (Utils.debug) {
				Log.w(TAG, "合成大小图文件异常", e);
			}
		}
		mSess.mDownUpMgr.uploadImg(tempImgPath, 0, true, false,	new DownUploadListner() {
			
			@Override
			public void onStart(FileTaskInfo taskInfo) {
				
			}
			
			@Override
			public void onProgress(FileTaskInfo taskInfo, int curlen, int totallen) {
				
			}
			
			@Override
			public void onFinish(FileTaskInfo taskInfo) {
			String fileUrl = taskInfo.mServerHost+":"+taskInfo.mPath+":"+taskInfo.mTime;
			if(Utils.debug){
				if(Utils.debug)Log.i(TAG, "FileTsUrl():"+fileUrl);
			}
			String storagePath=Utils.getStoragePath(ShenliaoOtherLoginService.this);
	   	    if(Utils.isNull(storagePath)){
	   	    	return ;
	    	}
	    	File sddir = new File(storagePath,Utils.AVATAR_PATH);
			if (!sddir.exists() && !sddir.mkdirs()) {
				if(Utils.debug)Log.e(TAG, "bitmapFromUrl-----Create dir failed");
				 sddir.mkdir();
			}
		    long user_id;
		    try{
//				user_id=Long.parseLong(prefs.getString(CommonData.USER_ID, "-1"));
				user_id=Long.parseLong(mSess.mPrefMeme.user_id.getVal());
			}catch(NumberFormatException e){
				user_id=-1;
			}
		    StringBuffer tempPath=new StringBuffer().append(sddir.getAbsolutePath()).append("/").append(TX.tm.getTxMe().partner_id).append(".jpg");
		    StringBuffer tempPath1=new StringBuffer().append(sddir.getAbsolutePath()).append("/").append(TX.tm.getTxMe().partner_id).append("_big.jpg");
		    createFile(tempPath.toString(),Sbp);
		    createFile(tempPath1.toString(),Bbp); 
		    if(Utils.debug){
		    	if(Utils.debug)Log.i(TAG, "stempPath>:"+tempPath);
		    	if(Utils.debug)Log.i(TAG, "btempPath>:"+tempPath1);
	    	 }
//		    ImageLoader.getInstance(ShenliaoOtherLoginService.this).removeImage(TX.tm.getTxMe().partner_id);
//		    editor = prefs.edit();
//		    editor.putBoolean(CommonData.IS_SETHEAD, true);
//		    editor.putString(CommonData.AVATAR_URL, fileUrl);
		    mSess.mPrefMeme.is_sethead.setVal(true);
		    mSess.mPrefMeme.avatar_url.setVal(fileUrl);
		    
//		    editor.putString(CommonData.AVATAR_URL_USER+TX.tm.getTxMe().partner_id, fileUrl);
		    try {
				mSess.mUserLoginInfor.updateUserAvatarSex(TX.tm.getUserID(), fileUrl);
			} catch (JSONException e) {
				if(Utils.debug)Log.e(TAG,"更新头像json异常",e);
			}
		    if(user_id!=-1){
//		    	editor.putString(CommonData.AVATAR_PATH, tempPath.toString());
		    	mSess.mPrefMeme.avatar_path.setVal(tempPath.toString()).commit();
		    }
//		    editor.commit();
		    mSess.getSocketHelper().sendUserSinaInforChange();
		    TX.tm.reloadTXMe();/////////////
		    }
			
			@Override
			public void onError(FileTaskInfo taskInfo, int iCode, Object iPara) {
				
			}
		}, null);

	}
	
	
	
	
	public void createFile(String path,Bitmap bitmap ){
		File file = new File(path);
		if (file.exists())
			file.delete();
		if (!file.exists()) {
			// 创建文件输出流
			OutputStream os = null;
			try {
				os = new FileOutputStream(file);
				// 存储
				bitmap.compress(CompressFormat.JPEG, 100, os);
				// 关闭流
			} catch (FileNotFoundException e) {
				//
				if(Utils.debug)e.printStackTrace();
			} finally {
				try {
					if (os != null)
						os.close();
				} catch (IOException e) {
					//
					if(Utils.debug)e.printStackTrace();
				}
			}
		}
	}
	
	private void initSinaUser(String nickName, String loc, String des, String gender){
//		if(editor == null){
//			editor = prefs.edit();
//		}
		int sex = TX.FEMAL_SEX;
		if(gender != null && gender.equals("m")){
			sex = TX.MALE_SEX;
		}
//		editor.putString(CommonData.NICKNAME, nickName)
//			.putString(CommonData.AREA, loc)
//			.putString(CommonData.SIGN, des)
//			.putInt(CommonData.SEX, sex)
//			.commit();
		mSess.mPrefMeme.nickname.setVal(nickName);
		mSess.mPrefMeme.area.setVal(loc);
		mSess.mPrefMeme.sign.setVal(des);
		mSess.mPrefMeme.sex.setVal(sex).commit();
		TX.tm.reloadTXMe();/////
		
		
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
	}	
	
	
	
	
}
