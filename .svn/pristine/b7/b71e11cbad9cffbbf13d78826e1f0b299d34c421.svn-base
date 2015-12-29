package com.tuixin11sms.tx.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.group.util.GroupUtils;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

public class SearchFriendActivity extends BaseActivity {

	protected static final String TAG = "SearchFriendActivity";
//	View addFriend;
	private View seachNearlyFriend; 
//    View addSinaFriend;
    private View searchOnline;//看看谁在聊
   // View inviteContactsFriend;
    private View addConditionFriend;
    private TextView backBtn;
    private Timer timer;
    private int isqut;
	private Toast exitToast;
	private TX tx = new TX();
	public static  boolean isOnline=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search_friend);
//		addFriend = findViewById(R.id.search_add_friend);
        addConditionFriend=findViewById(R.id.search_add_condition_friend);
		seachNearlyFriend = findViewById(R.id.search_nearby_friend);
//		addSinaFriend = findViewById(R.id.search_sina_friend);
		searchOnline=findViewById(R.id.search_online);
		//inviteContactsFriend = findViewById(R.id.invite_contacts_friend);
		backBtn = (TextView) findViewById(R.id.back_btn);
		
		/* 看看谁在聊单击事件*/
		searchOnline.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SearchFriendActivity.this,
						SearchConditionResultActivity.class);
				intent.putExtra("goinpage", SearchConditionResultActivity.ISSEAERCHONLINE);
				isOnline=true;
				startActivity(intent);
			}
		});
		addConditionFriend.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SearchFriendActivity.this,
						FindConditionFriendActivity.class);
				startActivity(intent);
				
			}
		});
		
//		addFriend.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				if(Utils.debug)Log.i(TAG, "添加好友");
//				Intent intent = new Intent(SearchFriendActivity.this,
//						FindTxFriendActivity.class);
//				startActivity(intent);
//			}
//		});
		        
		seachNearlyFriend.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
//                final String city = prefs.getString(CommonData.AREA, "");
//			    boolean fristAlert = prefs.getBoolean(CommonData.FIRST_ALERT, false);
                final String city = mSess.mPrefMeme.area.getVal();
			    boolean fristAlert = mSess.mPrefMeme.first_alert.getVal();
			    
			    if(!fristAlert){
			    	AlertDialog.Builder promptDialog = new AlertDialog.Builder(SearchFriendActivity.this);
					promptDialog.setTitle(R.string.privacy_title);
					promptDialog.setMessage(R.string.privacy_content);
					promptDialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() { 
				        public void onClick(DialogInterface dialog, int whichButton) { 
//				        	prefs.edit().putBoolean(CommonData.FIRST_ALERT, true).commit();
				        	mSess.mPrefMeme.first_alert.setVal(true).commit();
				        	
//				         	if(!"".equals(city)&&!"".equals(prefs.getString(CommonData.AVATAR_URL, ""))&&!"".equals(prefs.getString(CommonData.NICKNAME, ""))){
				         	if(!"".equals(city)&&!"".equals(mSess.mPrefMeme.avatar_url.getVal())&&!"".equals(mSess.mPrefMeme.nickname.getVal())){
				         		if(Utils.opGpsOrNetwork(SearchFriendActivity.this)){
									Intent intent = new Intent(SearchFriendActivity.this,
									NearlyFriendActivity.class);
							           startActivity(intent);
				         		}
							}else{
								Intent iSupplement = new Intent(SearchFriendActivity.this,UserInfoSupplementActivity.class);
								iSupplement.putExtra(UserInfoSupplementActivity.pblicInfo, UserInfoSupplementActivity.SEARCHFRTOUSERINFO);
								startActivity(iSupplement); 
							}
				            } 
				            }); 
					promptDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {	
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
					promptDialog.show();
			    }else{
//			    	if(!"".equals(city)&&!"".equals(prefs.getString(CommonData.AVATAR_URL, ""))&&!"".equals(prefs.getString(CommonData.NICKNAME, ""))){
			    	if(!"".equals(city)&&!"".equals(mSess.mPrefMeme.avatar_url.getVal())&&!"".equals(mSess.mPrefMeme.nickname.getVal())){
			    		if(Utils.opGpsOrNetwork(SearchFriendActivity.this)){
							Intent intent = new Intent(SearchFriendActivity.this,
							NearlyFriendActivity.class);
					           startActivity(intent);
			    		}
					}else{
						Intent iSupplement = new Intent(SearchFriendActivity.this,UserInfoSupplementActivity.class);
						iSupplement.putExtra(UserInfoSupplementActivity.pblicInfo, UserInfoSupplementActivity.SEARCHFRTOUSERINFO);
						startActivity(iSupplement); 
					}
			    	
			    }
			    

			}
		});
		backBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//if(Utils.debug)BLog.i("TAG", "写短信");
				SearchFriendActivity.this.finish();
			}
		});
		
//		addSinaFriend.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				
//				if(!Utils.checkNetworkAvailable(SearchFriendActivity.this)){
//					Utils.startPromptDialog(SearchFriendActivity.this, R.string.prompt, R.string.seach_network_title);
//					return;
//				}
//				timer = new Timer();
//				timer.schedule(new TimerTask() {
//					
//					@Override
//					public void run() {
//						Looper.prepare();
//						Toast.makeText(SearchFriendActivity.this, R.string.weibo_send_wait_long, Toast.LENGTH_SHORT).show();
//						Looper.loop();
//					}
//				},10*1000);
//				Utils.executorService.submit(new Runnable() {
//					@Override
//					public void run() {
//						String userid = prefs.getString(CommonData.WEIBO_USER_ID+"�"+TX.tm.getUserID(), "");
//				        Long overTime  = prefs.getLong(CommonData.WEIBO_OVER_TIME+"�"+TX.tm.getUserID(), 0);
//						if((!"".equals(userid)) && overTime > System.currentTimeMillis()){
//							timer.cancel();
//							startActivity(new Intent(SearchFriendActivity.this,WeiboCardActivity.class));
//						}else{
//							Intent iOauth = new Intent(SearchFriendActivity.this, WebViewActivity.class);
//							iOauth.putExtra(WebViewActivity.LOGIN_STATE, WebViewActivity.LOGIN_NORMAL);
//							startActivity(iOauth);
////							Utils.inPhoto=true;
//							timer.cancel();
//						}
//					}
//				});
//				
//				
//			}
//		});
	//	inviteContactsFriend.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				Intent intent = new Intent(SearchFriendActivity.this,
//						InviteContactsActivity.class);
//				startActivity(intent);
//			}
//		});
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	

	@Override
	protected void onStop() {
		super.onStop();
	}

	protected void onResume() {
//		Utils.inPhoto=false;
	   super.onResume();
	   MobclickAgent.onResume(this);
	}
	@Override
	protected void onRestart() {
		super.onRestart();
	}

	public void onDestroy() {
		TxData.popActivityRemove(this);
		super.onDestroy();
	}
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater mInflater = getMenuInflater();
		mInflater.inflate(R.menu.contacts_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	public boolean onPrepareOptionsMenu(Menu menu) { // 更新menu
		super.onPrepareOptionsMenu(menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.texit_menu:
			GroupUtils.showDialog(SearchFriendActivity.this, R.string.logout_prompt,
					R.string.dialog_okbtn, R.string.dialog_nobtn,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Utils.exitProcessLogic(SearchFriendActivity.this);
						}
					});

			break;
		}
		return super.onOptionsItemSelected(item);
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			TuiXinMainTab.tHost.setCurrentTab(TuiXinMainTab.MESSAGES_PAGE);
//			TuiXinMainTab.updateTabBackground(TuiXinMainTab.tHost);
//			return true;
			if(keyCode == KeyEvent.KEYCODE_BACK){
				if (isqut == 0) {
					isqut = 1;
					exitToast = new Toast(getApplicationContext());
					LayoutInflater mInflater = LayoutInflater.from(getApplicationContext());
					View toastView = mInflater.inflate(R.layout.message_exit_toast, null);
					TextView exitText = (TextView) toastView.findViewById(R.id.message_exit_text);
					exitText.setText(R.string.message_exit_str);
					exitToast.setDuration(Toast.LENGTH_SHORT);
					exitToast.setView(toastView);
					exitToast.show();
					new Timer().schedule(new TimerTask() {
						public void run() {
							isqut = 0;
						}
					}, 2000);
				} else if (isqut == 1) {
					isqut = 0;
					if (exitToast != null)
						exitToast.cancel();
					TxData.finishAll();

				}
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
