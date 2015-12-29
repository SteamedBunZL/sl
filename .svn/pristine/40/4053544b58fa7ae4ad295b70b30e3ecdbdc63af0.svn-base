package com.shenliao.set.activity;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.data.TxDB.Tx;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 昵称修改
 * 
 * @author xch
 * 
 */
public class SetUserInfoUpdateNameAcitivity extends BaseActivity implements OnClickListener {
	private EditText nickNameEdit;// 昵称
	private TextView sendBtn;// 发送按钮
	private TextView countNum;//字数
//	private SharedPreferences prefs;
//	private Editor editor;
	private UpdateReceiver updatereceiver;
	private ContentResolver cr;
	public static final String USERNAME_FOR_RESULT="username";

	public static final int USERNAME_CHANGE_SUCCESS = 5;
	public static final int USERNAME_CHANGE_FAILED = 6;
	public static final int USERNAME_CHANGE_NOTRULE = 7;
	public static final int USERNAME_CHANGE_NOTCHANGE=8;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_userinfo_update_nick);
		TxData.addActivity(this);
		init();
		setData();
	}

	// 初始化
	private void init() {
		sendBtn = (TextView) findViewById(R.id.sl_tab_setting_usermname_sendBtn);
		nickNameEdit = (EditText) findViewById(R.id.userinfo_join_input_box);
		countNum=(TextView) findViewById(R.id.sl_tab_setting_userinfo_num);
		back_left = (LinearLayout) findViewById(R.id.btn_back_left);
		cr = this.getContentResolver();
//		prefs = getSharedPreferences(PrefsMeme.MEME_PREFS, Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
//		editor = prefs.edit();

		sendBtn.setOnClickListener(this);
		back_left.setOnClickListener(this);
		nickNameEdit.addTextChangedListener(mTextWatcher);
	}

	// 设置数据
	private void setData() {
		nickNameEdit.setText(TX.tm.getTxMe().getNick_name());
		nickNameEdit.setSelection(TX.tm.getTxMe().getNick_name().length());
	}

	// 单击事件监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sl_tab_setting_usermname_sendBtn:
			if (!nickNameEdit.getText().toString().trim().equals("") && nickNameEdit.getText().toString().trim() != null) {
//				prefs.edit().putString(CommonData.NICKNAME, nickNameEdit.getText().toString()).commit();
				mSess.mPrefMeme.nickname.setVal(nickNameEdit.getText().toString()).commit();
				TX.tm.reloadTXMe();//////
				showDialogTimer(SetUserInfoUpdateNameAcitivity.this, R.string.prompt, R.string.group_edit_save,
						10 * 1000).show();
				mSess.getSocketHelper().sendUserInforChange();
			}else{
			   Toast.makeText(SetUserInfoUpdateNameAcitivity.this, R.string.insert_name_prompt, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_back_left:
			finish();
			break;
		}
	}

	private void registReceiver() {
		if (updatereceiver == null) {
			updatereceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			// 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
			filter.addAction(Constants.INTENT_ACTION_CHANGE_USERNAME_RSP);
			this.registerReceiver(updatereceiver, filter);
		}
	}

	// 修改广播接收器
	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_CHANGE_USERNAME_RSP.equals(intent.getAction())) {
				dealUserNameChange(serverRsp);
			}
		}

		// 处理修改昵称返回结果方法
		private void dealUserNameChange(ServerRsp serverRsp) {
			cancelDialogTimer();
			if(serverRsp!=null&&serverRsp.getStatusCode(Tx.DISPLAY_NAME)!=null){
				
				switch (serverRsp.getStatusCode(Tx.DISPLAY_NAME)) {
				
				case CHANGE_NAME_SUCCESS:
					
					handler.sendEmptyMessage(USERNAME_CHANGE_SUCCESS);
					break;
				case CHANGE_NAME_FAILED:
					String fbret = serverRsp.getBundle().getString("fbret");
					Message message = handler.obtainMessage(USERNAME_CHANGE_FAILED, fbret);
					handler.sendMessage(message);
					
					break;
				case CHANGE_NAME_NOTCHANGE:
					handler.sendEmptyMessage(USERNAME_CHANGE_NOTCHANGE);
					break;
				}
			}

		}
	}
    
	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			
			int num = msg.what;
			switch (num) {
			case USERNAME_CHANGE_SUCCESS:
				Intent intent = new Intent(Constants.INTENT_ACTION_USERNAME_CHANGE_RSP);
				mSess.getContext().sendBroadcast(intent);
				Intent intentresult = new Intent();
				setResult(SetUserInfoActivity.RESULTCODE_FOR_RESULT_NICENAME, intentresult);
				SetUserInfoUpdateNameAcitivity.this.finish();
				break;
			case USERNAME_CHANGE_FAILED:
				String signfbret = (String) msg.obj;
//				prefs.edit().putString(CommonData.NICKNAME, "").commit();
				mSess.mPrefMeme.nickname.setVal("").commit();
				TX.tm.reloadTXMe();/////
				Toast.makeText(SetUserInfoUpdateNameAcitivity.this, signfbret, 1).show();
				break;
			case USERNAME_CHANGE_NOTCHANGE:
				Intent intentnotchange = new Intent(Constants.INTENT_ACTION_USERNAME_CHANGE_RSP);
				mSess.getContext().sendBroadcast(intentnotchange);
				Intent result = new Intent();
				setResult(SetUserInfoActivity.RESULTCODE_FOR_RESULT_NICENAME, result);
				SetUserInfoUpdateNameAcitivity.this.finish();
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onResume() {
		registReceiver();
		super.onResume();
	}

	@Override
	protected void onStop() {
		if (updatereceiver != null) {
			this.unregisterReceiver(updatereceiver);
			updatereceiver = null;
		}

		super.onStop();
	}
	TextWatcher mTextWatcher = new TextWatcher() {  
        private CharSequence temp;  
        private int editStart ;  
        private int editEnd ;  
        @Override  
        public void onTextChanged(CharSequence s, int start, int before, int count) {  
            //   
             temp = s;  
        }  
          
        @Override  
        public void beforeTextChanged(CharSequence s, int start, int count,  
                int after) {  
            //   
        	//countNum.setText(s);//将输入的内容实时显示   
        }  
          
        @Override  
        public void afterTextChanged(Editable s) {  
            //   
            editStart = nickNameEdit.getSelectionStart();  
            editEnd = nickNameEdit.getSelectionEnd();
            int len = 24-temp.length();
            countNum.setText(len+"");  
            if (temp.length() > 24) {  
            	Toast.makeText(SetUserInfoUpdateNameAcitivity.this, "最多可以输入24个字符", Toast.LENGTH_SHORT).show();
                s.delete(editStart-1, editEnd);  
                int tempSelection = editStart;  
                nickNameEdit.setText(s);  
                nickNameEdit.setSelection(tempSelection);  
            }  
        }  
    };
	private LinearLayout back_left;  

    public boolean dispatchKeyEvent(android.view.KeyEvent event) {
	     if(event.getKeyCode()==KeyEvent.KEYCODE_ENTER){
	    	 
	          return true;
	    	 
	     }
	
	return super.dispatchKeyEvent(event);
	
	
	
	
};

}
