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
import android.widget.TextView;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TuixinService1;
import com.tuixin11sms.tx.TxData;
import com.umeng.analytics.MobclickAgent;


//TODO 这个页面是不是废弃的？有入口吗？2014.03.17 shc
public class ADActivity extends BaseActivity {
	private static final String TAG = ADActivity.class.getSimpleName();
	private WebView privacy_view;
//	private MsgHelperReceiver msgreceiver;
	private TextView mTitle;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	TxData.addActivity(this);
        this.getWindow().requestFeature(Window.FEATURE_PROGRESS);     
        setContentView(R.layout.activity_privacy_prompt);
        Bundle bundle = getIntent().getExtras();
        mTitle = (TextView) findViewById(R.id.tv_priacy_title);
        mTitle.setText(R.string.shenliao_ad);
        String privacy_page = null;
        if(bundle != null && bundle.containsKey("url")){
        	privacy_page = bundle.getString("url");
        }
       
        privacy_view = (WebView) findViewById(R.id.privacy_view);
        privacy_view.getSettings().setJavaScriptEnabled(true);  
        privacy_view.requestFocus();
        privacy_view.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        privacy_view.setWebChromeClient(new WebChromeClient()   
        {            
            public void onProgressChanged(WebView view, int progress)     
            {              
            	ADActivity.this.setTitle(R.string.page_loading);         
            	ADActivity.this.setProgress(progress * 100);       
                if(progress == 100)              
                	ADActivity.this.setTitle(R.string.app_name);         
                }        
            }  
        ); 
        privacy_view.setDownloadListener(new DownloadListener() {
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype,
					long contentLength) {
				// 实现下载的代码
				 Uri uri = Uri.parse(url);
				 Intent intent = new Intent(Intent.ACTION_VIEW,uri);
				 startActivity(intent);
//				 Utils.inPhoto=true;
			}
		});
        privacy_view.loadUrl(privacy_page);
        privacy_view.setWebViewClient(new WebViewClient(){       

            public boolean shouldOverrideUrlLoading(WebView view, String url) {       

                view.loadUrl(url);       

                return true;       

            }      
        });  
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
//		Utils.inPhoto=false;
		super.onStart();
	}
    @Override
    protected void onResume() {
    	super.onResume();
    	MobclickAgent.onResume(this);
    }
    @Override
    protected void onPause() {
    	super.onPause();
    	MobclickAgent.onPause(this);
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
//	public class MsgHelperReceiver extends BroadcastReceiver {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			final String msg2 = intent.getStringExtra("msg");
//			if("otherlogin".equals(msg2)){
//				
//				 AlertDialog.Builder promptDialog = new
//				 AlertDialog.Builder(ADActivity.this);
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