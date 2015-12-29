package com.tuixin11sms.tx.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TuixinService1;
import com.tuixin11sms.tx.TxData;
import com.umeng.analytics.MobclickAgent;

public class SellActivity extends BaseActivity {
	private static final String TAG = SellActivity.class.getSimpleName();
	private WebView sellView;
//	private MsgHelperReceiver msgreceiver;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	TxData.addActivity(this);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().requestFeature(Window.FEATURE_PROGRESS);     
        setContentView(R.layout.activity_sell);
        String sellPage = SellActivity.this.getResources().getString(R.string.sell_index);
        sellView = (WebView) findViewById(R.id.sell);
        sellView.getSettings().setJavaScriptEnabled(true);  
        sellView.requestFocus();
        sellView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        sellView.setWebChromeClient(new WebChromeClient()   
        {            
            public void onProgressChanged(WebView view, int progress)     
            {              
            	SellActivity.this.setTitle(R.string.page_loading);         
            	SellActivity.this.setProgress(progress * 100);       
                if(progress == 100)              
                	SellActivity.this.setTitle(R.string.app_name);         
                }        
            }  
        ); 
        sellView.setDownloadListener(new DownloadListener() {
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype,
					long contentLength) {
				// 实现下载的代码
				 Uri uri = Uri.parse(url);
				 Intent intent = new Intent(Intent.ACTION_VIEW,uri);
				 startActivity(intent);
//				 Utils.inPhoto=true;
//				if (contentDisposition == null
//						|| !contentDisposition.regionMatches(true, 0,
//								"attachment", 0, 10)) {
//					Intent intent = new Intent(Intent.ACTION_VIEW);
//					intent.setDataAndType(Uri.parse(url), mimetype);
//					startActivity(intent);
//					ResolveInfo info = getPackageManager().resolveActivity(
//							intent, PackageManager.MATCH_DEFAULT_ONLY);
//
//					if (info != null) {
//						ComponentName myName = getComponentName();
//						if (!myName.getPackageName().equals(
//								info.activityInfo.packageName)
//								|| !myName.getClassName().equals(
//										info.activityInfo.name)) {
//
//							try {
//								// 使用ANDROID内置播放器
//								startActivity(intent);
//								return;
//							} catch (ActivityNotFoundException ex) {
//							}
//						}
//					} else {
//						// 下载开始
//						// 自定义下载
//					}
//				}
			}
		});
        sellView.loadUrl(sellPage);
        sellView.setWebViewClient(new WebViewClient(){       

            public boolean shouldOverrideUrlLoading(WebView view, String url) {       

                view.loadUrl(url);       

                return true;       

            }      
        });  
    }
    protected void onResume() {
//		Utils.inPhoto=false;
	   super.onResume();
	   MobclickAgent.onResume(this);
	}
    @Override
    protected void onPause() {
    	super.onPause();
    	MobclickAgent.onPause(this);
    }
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {	
//			Intent intent = new Intent(PrivacyView.this, TuiXin.class);
//			startActivity(intent);
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}
    @Override
	protected void onStart() {
//		TuixinService.activityState.put(TAG, "onStart");
//		if (msgreceiver == null) {
//			msgreceiver = new MsgHelperReceiver();
//			IntentFilter filter = new IntentFilter();
//			// 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
//			filter.addAction(Constants.INTENT_ACTION_MSGHELPER_MSG);
//			this.registerReceiver(msgreceiver, filter);
//		}
		super.onStart();
	}
    @Override
	protected void onRestart() {
		super.onRestart();
		
	}
	protected void onStop() {
//		TuixinService.activityState.put(TAG, "onStop");
//		if (msgreceiver != null) {
//			this.unregisterReceiver(msgreceiver);
//			msgreceiver = null;
//		}
//		if(TuixinService.checkAllStop()){
//			showNotification();
//		}
		super.onStop();
	}
	protected void onDestroy() {
		TxData.popActivityRemove(this);
//		TuixinService1.activityState.remove(TAG);
		super.onDestroy();
	}
//	private void showNotification() {
////		Log.d(TAG, "shownotification" + msg);
//		String notifiyStr = this.getString(R.string.backmsg);
//		NotificationManager barmanager = (NotificationManager) this
//				.getSystemService(Context.NOTIFICATION_SERVICE);
//		long time=(System
//				.currentTimeMillis() / 1000
//				+ prefs.getLong(CommonData.ST, 0))*1000;
//		Notification notification = new Notification(
//				R.drawable.icon, notifiyStr, time);
//		Intent it = new Intent(this, PrivacyView.class);
//		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, it,
//				PendingIntent.FLAG_ONE_SHOT);
//		notification.flags |= Notification.FLAG_AUTO_CANCEL; 
////		boolean bSound = prefs.getBoolean(CommonData.SOUND, true);
////		if (bSound) {
////			notification.defaults |= Notification.DEFAULT_SOUND;
//////			notification.sound = Uri.withAppendedPath(
//////					Audio.Media.INTERNAL_CONTENT_URI, "2");
////		}
////		boolean bVibrate = prefs.getBoolean(CommonData.VIBRATE, false);
////		if (bVibrate) {
////			notification.defaults |= Notification.DEFAULT_VIBRATE;
//////			long[] vibrate = { 0, 100, 200, 300 }; // 0毫秒后开始振动，振动100毫秒后停止，再过200毫秒后再次振动300毫秒
//////			notification.vibrate = vibrate;
////		}
//		// //添加LED灯提醒
//		//
//		// notification.defaults |= Notification.DEFAULT_LIGHTS;
//		//
//		// //或者可以自己的LED提醒模式:
//		//
//		// notification.ledARGB = 0xff00ff00;
//		//
//		// notification.ledOnMS = 300; //亮的时间
//		//
//		// notification.ledOffMS = 1000; //灭的时间
//		//
//		// notification.flags |= Notification.FLAG_SHOW_LIGHTS;
//		String backmsg = this.getString(R.string.backmsg);
//		String appname = this.getString(R.string.app_name);
//		notification.setLatestEventInfo(this, appname, backmsg,
//					contentIntent);
////		ChatMsg receiver = null;
////		if (receiver != null) {
////			notification.setLatestEventInfo(context, "新消息", receiver
////					.getPartner_display_name()
////					+ ":" + receiver.getMessage(), contentIntent);
////		} else {
////			notification.setLatestEventInfo(context, "新消息", "新消息:" + msg,
////					contentIntent);
////		}
//		barmanager.notify(0, notification);
//	}
	
//	public class MsgHelperReceiver extends BroadcastReceiver {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			final String msg2 = intent.getStringExtra("msg");
//			if("otherlogin".equals(msg2)){
//				
//				 AlertDialog.Builder promptDialog = new
//				 AlertDialog.Builder(SellActivity.this);
//				 promptDialog.setTitle(R.string.other_login_title);
//				 promptDialog.setMessage(R.string.other_login);
//				 promptDialog.setPositiveButton(R.string.login_retry, new
//				 DialogInterface.OnClickListener() {
//				 @Override
//				 public void onClick(DialogInterface dialog, int which) {
//				 dialog.cancel();
////				    tuixinService.sendPing(); 
//				 }
//				 });
//			     promptDialog.setNegativeButton(R.string.confirm, new
//						 DialogInterface.OnClickListener() {
//						 @Override
//						 public void onClick(DialogInterface dialog, int which) {
//						 dialog.cancel();
////						 editor = prefs.edit();
////						 editor.putString(CommonData.USERNAME, "");
////						 editor.putString(CommonData.EMAIL, "");
////						 editor.putString(CommonData.PASSWORD, "");
////						 editor.putString(CommonData.REALNAME, "");
////						 editor.putString(CommonData.TELEPHONE, "");
////						 editor.commit();
////						 //System.exit(0);
////						 ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);    
////							manager.restartPackage(getPackageName());
////						 Intent intent = new Intent(MyChatRoom.this,
////						 LoginActivity.class);
////						 // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
////						 MyChatRoom.this.startActivity(intent);
//						 }
//						 });
//				 promptDialog.show();	
//				 }
//			
//		}
//		
//	}
}