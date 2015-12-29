package com.shenliao.set.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.set.activity.SetUpdateSignActivity.UpdateReceiver;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.BaseActivity;

/**
 * 聊天室名称修改
 * 
 * @author xch
 * 
 */
public class SetGroupNewNameAcitivity extends BaseActivity implements OnClickListener {
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
	
	public static final int REQUESTCODE_FOR_REQUSET_GROUPNAME = 11;// 昵称请求码
	public static final int RESULTCODE_FOR_RESULT_GROUPNAME = 12;// 昵称返回请求码

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
		title = (TextView) findViewById(R.id.title);
		title.setText("名称");
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
		Intent intent = getIntent();
		String name = intent.getStringExtra("name");
		nickNameEdit.setText(name);
		nickNameEdit.setSelection(name.length());
	}

	// 单击事件监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sl_tab_setting_usermname_sendBtn:
			String str_name=nickNameEdit.getText().toString();  
			if((str_name.length() > 24)){
				Toast.makeText(SetGroupNewNameAcitivity.this, "聊天室名称过长", Toast.LENGTH_SHORT).show();
				return;
			} 
            //判断空，我就不判断了。。。。  
            Intent data=new Intent();  
            data.putExtra("newname", str_name);  
            //请求代码可以自己设置，这里设置成20  
            setResult(RESULTCODE_FOR_RESULT_GROUPNAME, data);  
            //关闭掉这个Activity  
            finish();  
			
			break;
		case R.id.btn_back_left:
			finish();
			break;
		}
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
            	Toast.makeText(SetGroupNewNameAcitivity.this, "最多可以输入24个字符", Toast.LENGTH_SHORT).show();
                s.delete(editStart-1, editEnd);  
                int tempSelection = editStart;  
                nickNameEdit.setText(s);  
                nickNameEdit.setSelection(tempSelection);  
            }  
        }  
    };
	private LinearLayout back_left;
	private TextView title;  

    public boolean dispatchKeyEvent(android.view.KeyEvent event) {
	     if(event.getKeyCode()==KeyEvent.KEYCODE_ENTER){
	    	 
	          return true;
	    	 
	     }
	
	return super.dispatchKeyEvent(event);
	
};

}
