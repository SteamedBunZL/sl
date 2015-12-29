package com.shenliao.group.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.data.DataContainer;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.download.AvatarDownload;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.StringUtils;
import com.tuixin11sms.tx.utils.Utils;

public class GroupWarn extends BaseActivity implements OnClickListener {
	UpdateReceiver updatareceiver;
	private SmileyParser sysParser;
	private ScrollView screen;
	private List<Long> ids = new ArrayList<Long>();
	long uid;
	long lastuid;
	long gid;
	TXMessage txMsg;
	ImageView mHead;
	TextView mName;
	TextView mSex;
	TextView mId;
	TextView mArea;
	TextView mSign;

	Button mSubmit;

	EditText eop7;

	CheckBox cop1;
	CheckBox cop2;
	CheckBox cop3;
	CheckBox cop4;
	CheckBox cop5;
	CheckBox cop6;
	// CheckBox cop8;

	RelativeLayout top1;
	RelativeLayout top2;
	RelativeLayout top3;
	RelativeLayout top4;
	RelativeLayout top5;
	// int defaltHeaderImg;
	// int defaltHeaderImgMan;
	// int defaltHeaderImgFemale;
	RelativeLayout top6;
	// TextView top8;

	List<List<Integer>> opts = new ArrayList<List<Integer>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// prepairAsyncload();
		setContentView(R.layout.activity_group_warn);
		initViews();
		getData();
		initOpts();
	}

	private void getData() {
		Intent intent = getIntent();
		uid = intent.getLongExtra("uid", 0);
		txMsg = intent.getParcelableExtra("txmsg");
		gid = intent.getLongExtra("gid", 0l);
		age = intent.getIntExtra("age", 0);
		if (uid != 0) {

			// TX tx = TX.findTXByPartnerID4DB(uid);//不要直接访问数据库 2013.10.17 shc
			TX tx = TX.tm.getTx(uid);

			if (tx != null) {
//				tx.age = age;
				initTXView(tx);
			} else {
				TX txx = new TX();
				txx.partner_id = uid;
				if (txMsg != null) {
					txx.setNick_name(txMsg.nick_name);
					txx.setSex(txMsg.tcard_sex);
					txx.sign = txMsg.tcard_sign;
					txx.avatar_url = txMsg.tcard_avatar_url;
					TX ttx = TX.tm.getTx(txx.partner_id);
					if (txx != null && ttx != null
							&& ttx.getRemarkName() != null) {
						txx.setRemarkName(ttx.getRemarkName());
					}
				} else {
					txx.setNick_name("" + uid);
				}
				txx.age = age;
				initTXView(txx);
			}
			mSess.getSocketHelper().sendGetUserInfor(uid);
		} else {
			this.finish();
			return;
		}
	}

	private Handler avatarHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.AVATAR_RESULT:
				Object[] result = (Object[]) msg.obj;
				long tag = (Long) mHead.getTag();
				long id = (Long) result[1];
				if (result[0] != null && tag == id) {
					mHead.setImageBitmap((Bitmap) result[0]);
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	private void initTXView(TX tx2) {
		if (tx2.getSex() == TX.MALE_SEX) {
			mSex.setBackgroundResource(R.drawable.user_infor_sex_boy);
			defaultHeaderImg = defaultHeaderImgMan;
		} else {
			mSex.setBackgroundResource(R.drawable.user_infor_sex_girl);
			defaultHeaderImg = defaultHeaderImgFemale;
		}
		mSex.setText("" + age);
		if (!Utils.checkSDCard()) {// 无SD卡
			mHead.setImageResource(defaultHeaderImg);
		} else {
			mHead.setTag(tx2.partner_id);
			mHead.setImageResource(defaultHeaderImg);

			Bitmap avatar = mSess.avatarDownload.getAvatar(tx2.avatar_url,
					tx2.partner_id, mHead, avatarHandler);

			if (avatar != null)
				mHead.setImageBitmap(avatar);

		}
		mName.setText(sysParser.addSmileySpans(
				Utils.isNull(tx2.getRemarkName()) ? tx2.getNick_name() : tx2
						.getRemarkName(), true, 0));
		mId.setText(Long.valueOf(tx2.partner_id).toString());
		// 地区
		if (tx2.partner_id == TX.TUIXIN_MAN) {
			mArea.setText(tx2.area);
		} else {
			if (tx2.area != null && !"".equals(tx2.area)) {
				List<String> mlist = StringUtils.str2List(tx2.area);
				mArea.setText(DataContainer.getAreaNameByIds(mlist
						.toArray(new String[0])));
			} else {
				mArea.setText("");
			}
		}

		mSign.setText(sysParser.addSmileySpans(tx2.sign, true, 0));
	}

	private void initViews() {

		sysParser = Utils.getSmileyParser(this);
		mHead = (ImageView) this.findViewById(R.id.group_warn_head);
		mName = (TextView) this.findViewById(R.id.group_warn_name);
		mSex = (TextView) this.findViewById(R.id.group_warn_gender);
		mId = (TextView) this.findViewById(R.id.group_warn_idContent);
		mArea = (TextView) this.findViewById(R.id.group_warn_area);
		mSign = (TextView) this.findViewById(R.id.group_warn_signContent);

		btn_back_left = (LinearLayout) findViewById(R.id.btn_back_left);
		other = (LinearLayout) findViewById(R.id.other);
		btn_back_left.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				GroupWarn.this.finish();
			}
		});
		mSubmit = (Button) this.findViewById(R.id.group_warn_btn);
		mSubmit.setOnClickListener(this);
		screen = (ScrollView) findViewById(R.id.group_warn_screen);
		screen.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				InputMethodManager imm = (InputMethodManager) GroupWarn.this
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				return false;
			}
		});
		// defaltHeaderImgMan = R.drawable.tui_con_photo;
		// defaltHeaderImgFemale = R.drawable.sl_regist_head_femal;
	}

	@Override
	protected void onResume() {
		super.onResume();
		registReceiver();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (updatareceiver != null) {
			unregisterReceiver(updatareceiver);
			updatareceiver = null;
		}
	}

	private void registReceiver() {
		if (updatareceiver == null) {
			updatareceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_WARN_USER);
			this.registerReceiver(updatareceiver, filter);
		}
	}

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {

			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_WARN_USER.equals(intent.getAction())) {
				dealWarn(serverRsp);
			}
		}
	}

	public void dealWarn(ServerRsp serverRsp) {
		super.cancelDialogTimer();
		int result = R.string.warn_faild;
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			result = R.string.warn_sucess;

			break;
		case USER_NO_EXIST:
			result = R.string.warn_user_not_exists;
			break;
		}
		final int r1 = result;
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				AlertDialog.Builder promptDialog = new AlertDialog.Builder(
						GroupWarn.this);
				promptDialog.setTitle(R.string.prompt);
				promptDialog.setMessage(r1);
				promptDialog.setCancelable(false);
				promptDialog.setNegativeButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
								if (r1 == R.string.warn_sucess) {
									GroupWarn.this.finish();
								}
							}
						});
				promptDialog.show();
			}

		});
	}

	int index = -1;
	String str;

	@Override
	public void onClick(View arg0) {

		if (arg0.getId() == R.id.group_warn_btn) {

			if (index == -1) {
				Toast.makeText(GroupWarn.this, "请选择警告理由", Toast.LENGTH_SHORT)
						.show();
				return;
			} else if (index == 6
					&& eop7.getText().toString().trim().length() == 0) {
				Toast.makeText(GroupWarn.this, "请填写警告理由", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			if (index == 1) {
				str = tv1.getText().toString();
			} else if (index == 2) {
				str = tv2.getText().toString();
			} else if (index == 3) {
				str = tv3.getText().toString();
			} else if (index == 4) {
				str = tv4.getText().toString();

			} else if (index == 5) {
				str = tv5.getText().toString();
			} else if (index == 6) {
				str = eop7.getText().toString();

			}

			showDialogTimer(this, R.string.prompt, R.string.warn_ing, 10 * 1000)
					.show();
			mSess.getSocketHelper().sendUserWarn(uid, 1, str);

			return;
		}
		for (int i = 0; i < opts.size(); i++) {
			List<Integer> op = opts.get(i);
			for (int j = 0; j < op.size(); j++) {
				if (arg0.getId() == op.get(j)) {
					index = i;

					break;
				}
			}
		}
		if (index != -1) {
			for (int i = 0; i < opts.size(); i++) {
				List<Integer> op = opts.get(i);
				for (int j = 0; j < op.size(); j++) {
					View v = findViewById(op.get(j));

					if (i == index) {
						if (v instanceof CheckBox) {
							((CheckBox) v).setChecked(true);
							eop7.setText("");

							if (i == 6 || i == 0) {
								other.setVisibility(View.VISIBLE);
							} else {
								other.setVisibility(View.GONE);
							}

						}
					} else {
						if (v instanceof CheckBox) {
							((CheckBox) v).setChecked(false);
							// eop7.setCursorVisible(false);
						}
					}
				}
			}
		}

	}

	private void initOpts() {
		List<Integer> op1 = new ArrayList<Integer>();
		op1.add(R.id.group_warn_check_fast);
		op1.add(R.id.group_warn_text_fast);

		List<Integer> op2 = new ArrayList<Integer>();
		op2.add(R.id.group_warn_check_image);
		op2.add(R.id.group_warn_text_image);

		List<Integer> op3 = new ArrayList<Integer>();
		op3.add(R.id.group_warn_check_name);
		op3.add(R.id.group_warn_text_name);

		List<Integer> op4 = new ArrayList<Integer>();
		op4.add(R.id.group_warn_check_info);
		op4.add(R.id.group_warn_text_info);

		List<Integer> op5 = new ArrayList<Integer>();
		op5.add(R.id.group_warn_check_yycs);
		op5.add(R.id.group_warn_text_yycs);

		List<Integer> op6 = new ArrayList<Integer>();
		op6.add(R.id.group_tip_check_otherInfo);
		op6.add(R.id.group_tip_text_otherInfo);

		// List<Integer> op6 = new ArrayList<Integer>();
		// op6.add(R.id.group_warn_check_sendaudio);
		// op6.add(R.id.group_warn_text_sendaudio);
		//
		// List<Integer> op8=new ArrayList<Integer>();
		// op8.add(R.id.group_warn_check_yycs);
		// op8.add(R.id.group_warn_text_yycs);

		List<Integer> op7 = new ArrayList<Integer>();
		op7.add(R.id.group_warn_edit_otherCause);

		opts.add(op7);
		opts.add(op1);
		opts.add(op2);
		opts.add(op3);
		opts.add(op4);
		opts.add(op5);
		opts.add(op6);
		// opts.add(op8);

		cop1 = (CheckBox) findViewById(R.id.group_warn_check_fast);
		cop2 = (CheckBox) findViewById(R.id.group_warn_check_image);
		cop3 = (CheckBox) findViewById(R.id.group_warn_check_name);
		cop4 = (CheckBox) findViewById(R.id.group_warn_check_info);
		cop5 = (CheckBox) findViewById(R.id.group_warn_check_yycs);
		cop6 = (CheckBox) findViewById(R.id.group_tip_check_otherInfo);
		// cop8=(CheckBox) findViewById(R.id.group_warn_check_yycs);

		top1 = (RelativeLayout) findViewById(R.id.group_warn_text_fast);
		top2 = (RelativeLayout) findViewById(R.id.group_warn_text_image);
		top3 = (RelativeLayout) findViewById(R.id.group_warn_text_name);
		top4 = (RelativeLayout) findViewById(R.id.group_warn_text_info);
		top5 = (RelativeLayout) findViewById(R.id.group_warn_text_yycs);
		top6 = (RelativeLayout) findViewById(R.id.group_tip_text_otherInfo);
		tv1 = (TextView) findViewById(R.id.tv_fast);
		tv2 = (TextView) findViewById(R.id.tv_image);
		tv3 = (TextView) findViewById(R.id.tv_name);
		tv4 = (TextView) findViewById(R.id.tv_info);
		tv5 = (TextView) findViewById(R.id.tv_yycs);

		eop7 = (EditText) findViewById(R.id.group_warn_edit_otherCause);
		eop7.setOnTouchListener(onTouch);

		cop1.setOnClickListener(this);
		cop2.setOnClickListener(this);
		cop3.setOnClickListener(this);
		cop4.setOnClickListener(this);
		cop5.setOnClickListener(this);
		cop6.setOnClickListener(this);
		// cop8.setOnClickListener(this);

		top1.setOnClickListener(this);
		top2.setOnClickListener(this);
		top3.setOnClickListener(this);
		top4.setOnClickListener(this);
		top5.setOnClickListener(this);
		top6.setOnClickListener(this);
		// top8.setOnClickListener(this);

		// eop7.setOnClickListener(this);

	}

	OnTouchListener onTouch = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			switch (v.getId()) {
			case R.id.group_warn_edit_otherCause:
				eop7.setCursorVisible(true);
				break;

			default:
				break;
			}
			return false;
		}
	};
	private TextView tv1;
	private TextView tv2;
	private TextView tv3;
	private TextView tv4;
	private TextView tv5;
	private LinearLayout btn_back_left;
	private LinearLayout other;
	private int age;

	@Override
	protected void onDestroy() {
		// stopAsyncload();
		super.onDestroy();
	};
}
