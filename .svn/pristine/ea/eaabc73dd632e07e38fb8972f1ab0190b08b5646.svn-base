package com.tuixin11sms.tx.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Toast;

import com.shenliao.group.activity.CreateQun;
import com.shenliao.group.activity.GroupSearch;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.utils.CachedPrefs.PrefsMeme;
import com.tuixin11sms.tx.utils.CommonData;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;


/**
 * 联系人tab页，点击添加按钮的添加页面
 * */
public class AddContactsActivity extends BaseActivity {

	protected static final String TAG = "SearchFriendActivity";
	private View addFriend;
	private View addSinaFriend;
    private Timer timer;
	private SharedPreferences prefs;
	private View search_add_qun;
	private View search_qun;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_add_contacts);
		prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
				Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		addFriend = findViewById(R.id.search_add_friend);
		addSinaFriend = findViewById(R.id.search_sina_friend);
		search_add_qun = findViewById(R.id.search_add_qun);
		search_qun = findViewById(R.id.search_qun);
		
		search_add_qun.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(thisContext,
						CreateQun.class);
				startActivity(intent);
			}
		});
		
		search_qun.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(thisContext,
						GroupSearch.class);
				startActivity(intent);
			}
		});
		
		View v_back = findViewById(R.id.tv_title_back);
		v_back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				thisContext.finish();
				
			}
		});
		
		//通过号码添加好友
		addFriend.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(Utils.debug)Log.i(TAG, "添加好友");
				Intent intent = new Intent(thisContext,
						FindTxFriendActivity.class);
				startActivity(intent);
			}
		});
		        
		
		addSinaFriend.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(!Utils.checkNetworkAvailable(thisContext)){
					Utils.startPromptDialog(thisContext, R.string.prompt, R.string.seach_network_title);
					return;
				}
				timer = new Timer();
				timer.schedule(new TimerTask() {
					
					@Override
					public void run() {
						Looper.prepare();
						Toast.makeText(thisContext, R.string.weibo_send_wait_long, Toast.LENGTH_SHORT).show();
						Looper.loop();
					}
				},10*1000);
				Utils.executorService.submit(new Runnable() {
					@Override
					public void run() {
						String userid = prefs.getString(CommonData.WEIBO_USER_ID+"�"+TX.tm.getUserID(), "");
				        Long overTime  = prefs.getLong(CommonData.WEIBO_OVER_TIME+"�"+TX.tm.getUserID(), 0);
						if((!"".equals(userid)) && overTime > System.currentTimeMillis()){
							timer.cancel();
							startActivity(new Intent(thisContext,WeiboCardActivity.class));
						}else{
							Intent iOauth = new Intent(thisContext, WebViewActivity.class);
							iOauth.putExtra(WebViewActivity.LOGIN_STATE, WebViewActivity.LOGIN_NORMAL);
							startActivity(iOauth);
//							Utils.inPhoto=true;
							timer.cancel();
						}
					}
				});
				
				
			}
		});
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
	

	public void onDestroy() {
		TxData.popActivityRemove(this);
		super.onDestroy();
	}
	
}
