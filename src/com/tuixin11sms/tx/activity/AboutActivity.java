package com.tuixin11sms.tx.activity;

import java.util.Timer;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.download.AutoUpdater;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

public class AboutActivity extends BaseActivity implements OnClickListener{ 
	private final int CHECK_VER_TIMEOUT = 103;	
	private final int CHECK_VER=104;
	private final int CHECK_VER_NOT_NEEDUP=105;
	
	TextView versionview;
	TextView newverview;
	//ImageView lineview;
	View mAboutVersion;
	View mAboutCheckversion;
	View mAboutFeedback;
	View mAboutWork;
	private UpdateReceiver upReceiver;
//	SharedPreferences prefs;
//	Editor editor;
	private String appurl,applog;
	private int appver;
	private int newappver;
	private boolean checkver;
	private Timer outtime;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_about_info);
		//TextView mTitle = (TextView) findViewById(R.id.mTitle);
		//mTitle.setText("关于");
//		prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
//					Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
//		editor=prefs.edit();
		mAboutVersion = this.findViewById(R.id.about_version);
		mAboutCheckversion = this.findViewById(R.id.about_checkversion);
		mAboutWork = this.findViewById(R.id.about_work);
		mAboutFeedback = this.findViewById(R.id.about_feedback);
		versionview = (TextView) this.findViewById(R.id.mNowVersion);
		newverview = (TextView) this.findViewById(R.id.mNewVersion);
		back = (LinearLayout) this.findViewById(R.id.btn_back_left);
		
		
		mAboutCheckversion.setOnClickListener(this);
		mAboutFeedback.setOnClickListener(this);
		mAboutWork.setOnClickListener(this);
		back.setOnClickListener(this);
		/*lineview = (ImageView) this.findViewById(R.id.main_about_line);
		lineview.setBackgroundResource(R.drawable.about_line);*/
		
		

	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onDestroy() {
		TxData.popActivityRemove(this);
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.about_checkversion:
				if(!Utils.checkSDCardAndPrompt(AboutActivity.this)){//无SD卡
					 return ;
			     }
				checkver=true;
				AutoUpdater.isUping=false;
				showDialogTimer(AboutActivity.this, 0, R.string.check_new_versoin, 30 * 1000, new BaseTimerTask(){
					public void run() {
						super.cancel();
						Message msg = new Message();
						msg.what = CHECK_VER_TIMEOUT;
						mHandler.sendMessage(msg);
					}
				}).show();
				mSess.getSocketHelper().sendCheckVer();					
				break;
			case R.id.about_feedback:
//				if(prefs==null){
//	            	prefs = getSharedPreferences(CommonData.MEME_PREFS,
//	        				Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
//	            }
//				String realname = prefs.getString(CommonData.REALNAME, "");
//	            String feedbackSubject = "";
//				if(!"".equals(realname)){
//					feedbackSubject = AboutActivity1.this.getResources().getString(R.string.settings_feedback_subject);
//					feedbackSubject = feedbackSubject.replace("$", realname);
//				}else{
//					feedbackSubject = AboutActivity1.this.getResources().getString(R.string.feedback);
//				}
//				
//			    String feedbackText = AboutActivity1.this.getResources().getString(R.string.settings_feedback_text);
//			    //系统版本
//			    String sysVersion = android.os.Build.VERSION.RELEASE;
//			    //设备名称
//			    String mobileModel = android.os.Build.MODEL;
//			    //设备ID
//				TelephonyManager tm = (TelephonyManager) AboutActivity1.this.getSystemService(Service.TELEPHONY_SERVICE);
//				String device_id = tm.getDeviceId();
//				
//				//软件版本
//				String packageName = AboutActivity1.this.getPackageName();
//				String softVersion = "";
//				try {
//					softVersion = AboutActivity1.this.getResources().getString(R.string.soft_version);
//					String version = AboutActivity1.this.getPackageManager().getPackageInfo(packageName, 0).versionName;
//					softVersion = softVersion + version;
//				} catch (NameNotFoundException e2) {
//					softVersion = "";
//					e2.printStackTrace();
//				}
//				String hardwareCategory = AboutActivity1.this.getResources().getString(R.string.hardware_category);
//				String operatingSystem = AboutActivity1.this.getResources().getString(R.string.operating_system);
//				String deviceId = AboutActivity1.this.getResources().getString(R.string.device_id);
//				String feedbackUsername = AboutActivity1.this.getResources().getString(R.string.feedback_username);
//				StringBuffer sb = new StringBuffer();
//				if("".equals(softVersion)){
//					sb.append(feedbackText).append("\n").append(hardwareCategory).append(mobileModel)
//					.append("\n").append(operatingSystem).append("Android").append(" ").append(sysVersion).append("\n")
//						.append(deviceId).append(device_id).append("\n");
//				}else{
//					sb.append(feedbackText).append("\n").append(softVersion).append("\n")
//					.append(hardwareCategory).append(mobileModel).append("\n").append(operatingSystem).append("Android")
//					.append(" ").append(sysVersion).append("\n").append(deviceId).append(device_id).append("\n");
//				}
//				if(!"".equals(realname)){
//					sb.append(feedbackUsername).append(realname).append("\n");
//				}
//				String text = sb.toString();
//				String debug_email = AboutActivity1.this.getResources().getString(R.string.debug_email);
//				Intent feedbackIntent = new Intent(Intent.ACTION_SEND);
////				feedbackIntent.setType("message/rfc822");
//				feedbackIntent.setType("application/*");
//				feedbackIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{debug_email});
//				feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, feedbackSubject);
//				feedbackIntent.putExtra(Intent.EXTRA_TEXT, text);
//				
//	            
//	            File sddir = Utils.createDirectory(AboutActivity1.this);
//	            if(sddir!=null){
//	            	File console_file = new File(sddir, Constants.CRASH_LOG_NAME);
//	            	Uri[] uris = new Uri[2];
//		    		if(console_file.exists()){
//		    			feedbackIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(console_file)); //添加附件，附件为file对象
//		    			uris[0] = Uri.fromFile(console_file);
//		    		}
//		    		File recovery_file = new File(sddir, "recovery.log");
//		    		if(recovery_file.exists()){
//		    			FileInputStream fis;
//						try {
//							fis = new FileInputStream(recovery_file);
//							byte[] data = new byte[2048];
//			    			fis.read(data);
//			    			feedbackIntent.putExtra(Intent.EXTRA_TEXT, text + new String(data));
//						} catch (Exception e) {
//							
//						}
//		    		}
//	            }
//	            try {
//					startActivity(Intent.createChooser(feedbackIntent, this.getApplication().getText(R.string.about_choose_send)));//调用系统的mail客户端进行发送
//				} catch (ActivityNotFoundException e) {
//					Utils.startPromptDialog(AboutActivity1.this, R.string.prompt, R.string.prompt);
//					//startPromptDialog(R.string.prompt, R.string.send_email_failed);
//					e.printStackTrace();
//				}
				Intent i = new Intent(AboutActivity.this, TutorialTeachActivity.class);
				startActivity(i);
				break;
			case R.id.about_work:
				Intent iurl = new Intent(AboutActivity.this,PrivacyViewActivity.class);
				iurl.putExtra("url", "http://wap.shenliao.com/job");
				startActivity(iurl);
				break;
			case R.id.btn_back_left:
				AboutActivity.this.finish();
				break;
		}
	}
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			cancelDialog();
			switch (msg.what) {
			case CHECK_VER_TIMEOUT:
				startPromptDialog(R.string.prompt, R.string.request_check_ver_outtime);
				break;
			case CHECK_VER_NOT_NEEDUP:
				startPromptDialog(R.string.prompt, R.string.check_new_versoin_not_need_updata);
				break;
			case CHECK_VER:
				if(!AutoUpdater.isUping){
//					appurl = getPrefsMeme().getString(CommonData.UPDATA_URL, "");
//					applog = getPrefsMeme().getString(CommonData.UPDATA_LOG, "");	
					appurl = mSess.mPrefMeme.updata_url.getVal();
					applog = mSess.mPrefMeme.updata_log.getVal();	
					appver = Utils.appver;
	        		AutoUpdater.isUping=true;
	        		new AlertDialog.Builder(AboutActivity.this)
					.setTitle(R.string.up_prompt)
					.setCancelable(false) 
					.setMessage(applog)
					.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialoginterface, int i){
							AutoUpdater.CheckForUpdate(AboutActivity.this,appurl,applog,appver);
//							editor.putInt(CommonData.OLD_VER, appver);
//						 	editor.commit();
						 	mSess.mPrefMeme.old_ver.setVal(appver).commit();
						}
					})
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				        	  	 public void onClick(DialogInterface dialog, int id) {
				               			 dialog.cancel();
				               			AutoUpdater.isUping=false;
//				               			editor.putInt(CommonData.OLD_VER, appver);
//									 	editor.commit();
				               			mSess.mPrefMeme.old_ver.setVal(appver).commit();
				        	 	  }
				    	})
				   	 .show();//显示对话框
				}				
				break;
			}
		}
	};
	private LinearLayout back;
	private class UpdateReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			timeCancel();
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if(Constants.INTENT_ACTION_VERSION_UPDATE.equals(intent.getAction())){
				if(serverRsp != null){
					switch(serverRsp.getStatusCode()){
					case RSP_OK:{
						handleMessage(CHECK_VER);
						break;
					}
					case VERSION_NO_UP:{
						handleMessage(CHECK_VER_NOT_NEEDUP);
						break;
					}
					default:
					}
				}
				
			}
		}
		
	}
	private void handleMessage(int what){
		Message msg = mHandler.obtainMessage();
		msg.what = what;
		mHandler.sendMessage(msg);
	}
	
	private void startPromptDialog(int titleSource, int msg){
		AlertDialog.Builder promptDialog = new AlertDialog.Builder(AboutActivity.this);
		promptDialog.setTitle(titleSource);
		promptDialog.setMessage(msg);
		promptDialog.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {	
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		promptDialog.show();
	}
	private void registReceiver(){
		if(upReceiver == null){
			upReceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_VERSION_UPDATE);
			registerReceiver(upReceiver, filter);
		}
	}
	private void unregistReceiver(){
		if(upReceiver != null){
			unregisterReceiver(upReceiver);
			upReceiver = null;
		}
	}

	@Override
	protected void onResume() {
		String version = null;
		version = Utils.ChangVerString(Utils.appver);
		String softVersion = getResources().getString(R.string.version_show);
		versionview.setText(softVersion + version + "_"+ Utils.buildNo+"_"+Utils.cid);
//		newappver = getPrefsMeme().getInt(CommonData.UPDATA_VER, 0);
//		appurl = getPrefsMeme().getString(CommonData.UPDATA_URL, "");
//		applog = getPrefsMeme().getString(CommonData.UPDATA_LOG, "");
		newappver = mSess.mPrefMeme.updata_ver.getVal();
		appurl = mSess.mPrefMeme.updata_url.getVal();
		applog = mSess.mPrefMeme.updata_log.getVal();
		
		
		appver = Utils.appver;
		if (!Utils.isNull(appurl) && newappver > Utils.appver) {
			newverview.setText("(新版本"+softVersion+Utils.ChangVerString(newappver)+")");
			newverview.setVisibility(View.VISIBLE);
		} else {
			newverview.setVisibility(View.GONE);
		}
		registReceiver();
		super.onResume();
		MobclickAgent.onResume(this);
	}
	

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onPause() {
		unregistReceiver();
		super.onPause();
		MobclickAgent.onPause(this);
	}
	private void timeCancel(){
		if(outtime != null){
			outtime.cancel();
		}
	}
}
