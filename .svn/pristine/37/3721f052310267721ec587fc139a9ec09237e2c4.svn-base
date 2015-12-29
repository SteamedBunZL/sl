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
import com.shenliao.group.util.GroupUtils;
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
import com.umeng.analytics.MobclickAgent;

/**
 * 举报activity
 * 
 * @author xuchunhui
 * 
 */
public class GroupTip extends BaseActivity implements OnClickListener {

	public static final String UID = "uid";// 神聊号
	public static final String GID = "gid";// 群id
	public static final String TXMSG = "txmsg";// 消息
	public static final String AGE = "age";// 消息
	public static final String REPORT_BLOG = "isReportBlog";
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
	// int defaltHeaderImg;
	// int defaltHeaderImgMan;
	// int defaltHeaderImgFemale;
	Button mSubmit;

	EditText eop7;

	CheckBox cop1;
	CheckBox cop2;
	CheckBox cop3;
	CheckBox cop4;
	CheckBox cop5;
	CheckBox cop6;
	CheckBox cop7;

	RelativeLayout top1;
	RelativeLayout top2;
	RelativeLayout top3;
	RelativeLayout top4;
	RelativeLayout top5;
	RelativeLayout top6;
	RelativeLayout top7;

	List<List<Integer>> opts = new ArrayList<List<Integer>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// prepairAsyncload();
		setContentView(R.layout.activity_group_tip);

		initViews();
		getData();
		initOpts();

	}

	private void getData() {
		Intent intent = getIntent();
		isreportblog = intent.getBooleanExtra(REPORT_BLOG, false);
		blogMid = intent.getStringExtra("mid");
		uid = intent.getLongExtra(UID, 0);
		int age = intent.getIntExtra("age", 0);
		txMsg = intent.getParcelableExtra(TXMSG);
		gid = intent.getLongExtra(GID, 0l);
		if (uid != 0) {

			// TX tx = TX.findTXByPartnerID4DB(uid);//不要直接访问数据库 2013.10.17 shc
			TX tx = TX.tm.getTx(uid);

			if (tx != null) {
				// tx.age = age;
				initTXView(tx);
			} else {
				TX txx = new TX();
				txx.partner_id = uid;
				if (txMsg != null) {
					txx.setNick_name(txMsg.nick_name);
					txx.setSex(txMsg.tcard_sex);
					txx.sign = txMsg.tcard_sign;
					txx.avatar_url = txMsg.tcard_avatar_url;

					// txx.setRemarkName(txdata.findTBTXbyPartnerID(txx.partner_id).getRemarkName());
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
		mSex.setText(tx2.age + "");
		if (!Utils.checkSDCard()) {// 无SD卡
			mHead.setImageResource(defaultHeaderImg);
		} else {

			mHead.setTag(tx2.partner_id);
			mHead.setImageResource(defaultHeaderImg);

			Bitmap avatar = mSess.avatarDownload.getAvatar(tx2.avatar_url,
					tx2.partner_id, mHead, avatarHandler);

			if (avatar != null) {
				mHead.setImageBitmap(avatar);
			}
		}
		// mName.setText(sysParser.addSmileySpans(tx2.getNick_name(), true, 0));
		mId.setText(Long.valueOf(tx2.partner_id).toString());
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
		// 昵称
		if (!Utils.isNull(tx2.getRemarkName())) {
			mName.setText(sysParser.addSmileySpans(tx2.getRemarkName(), true, 0));
		} else {
			if (!Utils.isNull(tx2.getNick_name())) {
				mName.setText(sysParser.addSmileySpans(tx2.getNick_name(),
						true, 0));
			} else {
				// mName.setText(sysParser.addSmileySpans(
				// tx2.getContacts_person_name(), true, 0));
				mName.setText(sysParser.addSmileySpans(tx2.getTxInfor()
						.getContacts_person_name(), true, 0));
			}
		}

		mSign.setText(sysParser.addSmileySpans(tx2.sign, true, 0));
	}

	private void initViews() {

		sysParser = Utils.getSmileyParser(this);
		mHead = (ImageView) this.findViewById(R.id.group_tip_head);
		mName = (TextView) this.findViewById(R.id.group_tip_name);
		mSex = (TextView) this.findViewById(R.id.group_tip_gender);
		mId = (TextView) this.findViewById(R.id.group_tip_idContent);
		mArea = (TextView) this.findViewById(R.id.group_tip_area);
		mSign = (TextView) this.findViewById(R.id.group_tip_signContent);

		btn_back_left = (LinearLayout) findViewById(R.id.btn_back_left);
		other = (LinearLayout) findViewById(R.id.other);
		btn_back_left.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				GroupTip.this.finish();
			}
		});

		mSubmit = (Button) this.findViewById(R.id.group_tip_btn);
		mSubmit.setOnClickListener(this);
		screen = (ScrollView) findViewById(R.id.group_tip_screen);
		screen.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				InputMethodManager imm = (InputMethodManager) GroupTip.this
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
		MobclickAgent.onResume(this);
		registReceiver();
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
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
			filter.addAction(Constants.INTENT_ACTION_REPORT_USER);
			filter.addAction(Constants.INTENT_ACTION_REPORT_BLOG);
			this.registerReceiver(updatareceiver, filter);
		}
	}

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_REPORT_USER.equals(intent.getAction())) {
				dealReport(serverRsp);
			} else if (Constants.INTENT_ACTION_REPORT_BLOG.equals(intent
					.getAction())) {
				dealBlogReport(serverRsp);
			}
		}
	}

	public void dealReport(ServerRsp serverRsp) {
		super.cancelDialogTimer();
		int result = R.string.report_faild;
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			result = R.string.report_success;
			long reportid = serverRsp.getBundle().getInt("uid");
			getSharedPreferences(GroupUtils.REPORT_UID_SETTING, 0).edit()
					.putLong(GroupUtils.RPORT_UID_RESULT + reportid, reportid)
					.commit();
			break;
		case USER_NO_EXIST:
			result = R.string.report_user_not_exists;
			break;
		}
		final int r1 = result;
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				AlertDialog.Builder promptDialog = new AlertDialog.Builder(
						GroupTip.this);
				promptDialog.setTitle(R.string.prompt);
				promptDialog.setMessage(r1);
				promptDialog.setCancelable(false);
				promptDialog.setNegativeButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
								if (r1 == R.string.report_success) {
									GroupTip.this.finish();
								}
							}
						});
				promptDialog.show();
			}

		});
	}

	public void dealBlogReport(ServerRsp serverRsp) {
		super.cancelDialogTimer();
		int result = R.string.report_faild;
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			result = R.string.report_success;
			long mid = serverRsp.getBundle().getLong("mid");
			getSharedPreferences(GroupUtils.REPORT_MID_SETTING, 0).edit()
					.putLong(GroupUtils.RPORT_MID_RESULT + mid, mid).commit();
			break;
		case USER_NO_EXIST:
			result = R.string.report_user_not_exists;
			break;
		case BOLG_NO_EXIT:
			result = R.string.no_blog;
			break;
		}
		final int r1 = result;
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				AlertDialog.Builder promptDialog = new AlertDialog.Builder(
						GroupTip.this);
				promptDialog.setTitle(R.string.prompt);
				promptDialog.setMessage(r1);
				promptDialog.setCancelable(false);
				promptDialog.setNegativeButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
								if (r1 == R.string.report_success) {
									GroupTip.this.finish();
								}
							}
						});
				promptDialog.show();
			}

		});
	}

	int index = -1;

	@Override
	public void onClick(View arg0) {

		if (arg0.getId() == R.id.group_tip_btn) {
			if (index == -1) {
				Toast.makeText(GroupTip.this, "请选择举报理由", Toast.LENGTH_SHORT)
						.show();
				return;
			} else if (index == 7
					&& eop7.getText().toString().trim().length() == 0) {
				Toast.makeText(GroupTip.this, "请填写理由", Toast.LENGTH_SHORT)
						.show();
				return;
			}

			if (isreportblog) {
				//举报瞬间
				String s = "";
				switch (index) {
				case 0:
					s = eop7.getText().toString();
					break;
				case 1:
					s = "骚扰他人";
					break;
				case 2:
					s = "淫秽色情";
					break;
				case 3:
					s = "垃圾广告";
					break;
				case 4:
					s = "虚假中奖";
					break;
				case 5:
					s = "人身攻击";
					break;
				case 6:
					s = "敏感信息";
					break;
				}

				if (Utils.isNull(blogMid)) {
					return;
				}
				int blogId = Integer.parseInt(blogMid);
				//TODO 举报瞬间记录目前存放在SharedPreferences，后期需做优化--wb
				if (getSharedPreferences(GroupUtils.REPORT_MID_SETTING, 0)
						.getLong(GroupUtils.RPORT_MID_RESULT + blogId, 0) == blogId) {
					if (checkTime()) {
						showDialogTimer(this, R.string.prompt,
								R.string.report_ing, 10 * 1000).show();
						mSess.getSocketHelper().sendReportBlogInfo(blogMid,
								uid, s);
					} else {
						Toast.makeText(GroupTip.this,
								"您今日已举报此瞬间，我们会进行处理，请耐心等待", Toast.LENGTH_SHORT)
								.show();
					}
				} else {
					getSharedPreferences(GroupUtils.REPORT_SETTING_BLOG, 0)
							.edit()
							.putLong(GroupUtils.REPORT_LASTTIME_BLOG,
									System.currentTimeMillis()).commit();
					showDialogTimer(this, R.string.prompt, R.string.report_ing,
							10 * 1000).show();
					mSess.getSocketHelper().sendReportBlogInfo(blogMid, uid, s);
				}

			} else {
				//举报用户
				if (getSharedPreferences(GroupUtils.REPORT_UID_SETTING, 0)
						.getLong(GroupUtils.RPORT_UID_RESULT + uid, 0) == uid) {

					if (checkTime()) {
						showDialogTimer(this, R.string.prompt,
								R.string.report_ing, 10 * 1000).show();
						mSess.getSocketHelper().sendReportUser(uid, gid,
								eop7.getText().toString(), index, txMsg, 11);

					} else {
						Toast.makeText(GroupTip.this,
								"您今日已举报此用户，我们会进行处理，请耐心等待", Toast.LENGTH_SHORT)
								.show();
					}
				} else {
					showDialogTimer(this, R.string.prompt, R.string.report_ing,
							10 * 1000).show();
					getSharedPreferences(GroupUtils.REPORT_SETTING, 0)
							.edit()
							.putLong(GroupUtils.REPORT_LASTTIME,
									System.currentTimeMillis()).commit();
					mSess.getSocketHelper().sendReportUser(uid, gid,
							eop7.getText().toString(), index, txMsg, 11);
					// getSharedPreferences("lastuid_setting",
					// 0).edit().putLong("lastuid_result", uid).commit();

				}
			}

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

					// if (i == 0) {
					// eop7.setCursorVisible(true);
					// } else {
					// eop7.setText("");
					// eop7.setCursorVisible(false);
					// }

					if (i == index) {
						if (v instanceof CheckBox) {
							((CheckBox) v).setChecked(true);
						}
						if (i == 7 || i == 0) {
							other.setVisibility(View.VISIBLE);
						} else {
							other.setVisibility(View.GONE);
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

	/**
	 * 
	 * @return true 可以举报
	 */
	private boolean checkTime() {
		long lastTime = 0;
		if (isreportblog) {
			lastTime = getSharedPreferences(GroupUtils.REPORT_SETTING_BLOG, 0)
					.getLong(GroupUtils.REPORT_LASTTIME_BLOG, 0);
		} else {
			lastTime = getSharedPreferences(GroupUtils.REPORT_SETTING, 0)
					.getLong(GroupUtils.REPORT_LASTTIME, 0);
		}

		long nextTime = 24 * 60 * 60 * 1000 + lastTime;
		if (Utils.debug) {
			// debug版本可以一直举报别人
			nextTime = 0;
		}
		long nowTime = System.currentTimeMillis();
		if (nowTime > nextTime) {
			return true;
		} else {
			return false;
		}

	}

	private void initOpts() {
		List<Integer> op1 = new ArrayList<Integer>();
		op1.add(R.id.group_tip_text_harass);
		op1.add(R.id.group_tip_check_harass);

		List<Integer> op2 = new ArrayList<Integer>();
		op2.add(R.id.group_tip_text_bawdy);
		op2.add(R.id.group_tip_check_bawdy);

		List<Integer> op3 = new ArrayList<Integer>();
		op3.add(R.id.group_tip_check_rubbishAd);
		op3.add(R.id.group_tip_text_rubbishAd);

		List<Integer> op4 = new ArrayList<Integer>();
		op4.add(R.id.group_tip_check_sham);
		op4.add(R.id.group_tip_text_sham);

		List<Integer> op5 = new ArrayList<Integer>();
		op5.add(R.id.group_tip_text_personAttack);
		op5.add(R.id.group_tip_check_personAttack);

		List<Integer> op6 = new ArrayList<Integer>();
		op6.add(R.id.group_tip_check_sensitiveInfo);
		op6.add(R.id.group_tip_text_sensitiveInfo);

		List<Integer> op8 = new ArrayList<Integer>();
		op8.add(R.id.group_tip_check_otherInfo);
		op8.add(R.id.group_tip_text_otherInfo);

		List<Integer> op7 = new ArrayList<Integer>();
		op7.add(R.id.group_tip_edit_otherCause);

		opts.add(op7);
		opts.add(op1);
		opts.add(op2);
		opts.add(op3);
		opts.add(op4);
		opts.add(op5);
		opts.add(op6);
		opts.add(op8);

		cop1 = (CheckBox) findViewById(R.id.group_tip_check_harass);
		cop2 = (CheckBox) findViewById(R.id.group_tip_check_bawdy);
		cop3 = (CheckBox) findViewById(R.id.group_tip_check_rubbishAd);
		cop4 = (CheckBox) findViewById(R.id.group_tip_check_sham);
		cop5 = (CheckBox) findViewById(R.id.group_tip_check_personAttack);
		cop6 = (CheckBox) findViewById(R.id.group_tip_check_sensitiveInfo);
		cop7 = (CheckBox) findViewById(R.id.group_tip_check_otherInfo);

		top1 = (RelativeLayout) findViewById(R.id.group_tip_text_harass);
		top2 = (RelativeLayout) findViewById(R.id.group_tip_text_bawdy);
		top3 = (RelativeLayout) findViewById(R.id.group_tip_text_rubbishAd);
		top4 = (RelativeLayout) findViewById(R.id.group_tip_text_sham);
		top5 = (RelativeLayout) findViewById(R.id.group_tip_text_personAttack);
		top6 = (RelativeLayout) findViewById(R.id.group_tip_text_sensitiveInfo);
		top7 = (RelativeLayout) findViewById(R.id.group_tip_text_otherInfo);

		eop7 = (EditText) findViewById(R.id.group_tip_edit_otherCause);
		eop7.setOnTouchListener(onTouch);

		cop1.setOnClickListener(this);
		cop2.setOnClickListener(this);
		cop3.setOnClickListener(this);
		cop4.setOnClickListener(this);
		cop5.setOnClickListener(this);
		cop6.setOnClickListener(this);
		cop7.setOnClickListener(this);

		top1.setOnClickListener(this);
		top2.setOnClickListener(this);
		top3.setOnClickListener(this);
		top4.setOnClickListener(this);
		top5.setOnClickListener(this);
		top6.setOnClickListener(this);
		top7.setOnClickListener(this);

		// eop7.setOnClickListener(this);

	}

	OnTouchListener onTouch = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			switch (v.getId()) {
			case R.id.group_tip_edit_otherCause:
				eop7.setCursorVisible(true);
				break;

			default:
				break;
			}
			return false;
		}
	};
	private LinearLayout btn_back_left;
	private LinearLayout other;
	private boolean isreportblog;
	private String blogMid;

	@Override
	protected void onDestroy() {
		// stopAsyncload();
		super.onDestroy();
	};
}
