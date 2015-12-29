package com.shenliao.group.activity;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.activity.GroupMsgRoom;
import com.tuixin11sms.tx.activity.OAuthShareWeiboActivity;
import com.tuixin11sms.tx.activity.SelectFriendListActivity;
import com.tuixin11sms.tx.activity.WebViewActivity;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.utils.CachedPrefs.PrefsMeme;
import com.tuixin11sms.tx.utils.CommonData;
import com.tuixin11sms.tx.utils.Utils;

public class GroupCreateResult extends BaseActivity implements OnClickListener {

	TxGroup txGroup;
	Button mAddFriend;
	TextView groupNum;
	Button mJoin;
	RelativeLayout mPrompt;
	View mShareWeibo;
	View mShareEmail;
	View mShareSms;
	LinearLayout mRecommend;

	private SharedPreferences prefs;
	private SmileyParser smileyParser;
	private LinearLayout btn_back_left;
	private ImageView mTouxiang;
	private Bitmap avatar;
	private boolean isQun;
	private boolean hasDisplayAnim = false;//是否显示过创建成功的动画？
	private TextView group_create_success;
	private RelativeLayout group_index_title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		setContentView(R.layout.activity_group_create_result);

		Intent intent = getIntent();
		avatar = intent.getParcelableExtra("avatar");
		isQun = intent.getBooleanExtra("isQun", false);
	}

	private void init() {

		group_create_success = (TextView) findViewById(R.id.group_create_success);
		TextView group_create_number = (TextView) findViewById(R.id.group_create_number);
		LinearLayout ll_share = (LinearLayout) findViewById(R.id.ll_share);

		group_index_title = (RelativeLayout) findViewById(R.id.group_index_title);

		groupNum = (TextView) findViewById(R.id.group_create_groupid);
		mJoin = (Button) findViewById(R.id.group_create_join);
		mPrompt = (RelativeLayout) findViewById(R.id.group_success_prompt);
		mRecommend = (LinearLayout) findViewById(R.id.recommend_layout);
		mTouxiang = (ImageView) findViewById(R.id.creat_group_touxiang);

		btn_back_left = (LinearLayout) findViewById(R.id.btn_back_left);

		mShareWeibo = findViewById(R.id.group_shareweibo);
		mShareEmail = findViewById(R.id.group_email);
		mShareSms = findViewById(R.id.group_message);

		mAddFriend = (Button) findViewById(R.id.group_create_add_member);

		// 判断是否是群
		if (isQun) {
			group_create_success.setText("恭喜您，创建群成功");
			group_create_number.setText("群ID:");
			ll_share.setVisibility(View.GONE);
			mAddFriend.setVisibility(View.VISIBLE);
			mAddFriend.setText("添加好友到群");
		}

		smileyParser = new SmileyParser(this);
		prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
				Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

		Intent intent = getIntent();
		if (intent != null) {
			txGroup = intent.getParcelableExtra(Utils.MSGROOM_TX_GROUP);
			txGroup = TxGroup.getTxGroup(this.getContentResolver(),
					(int) txGroup.group_id);
			setGroupData(txGroup);
		}

		mAddFriend.setOnClickListener(this);
		mJoin.setOnClickListener(this);
		mShareSms.setOnClickListener(this);
		mShareEmail.setOnClickListener(this);
		mShareWeibo.setOnClickListener(this);
		btn_back_left.setOnClickListener(this);
	}

	/**
	 * view赋值
	 */
	private void setGroupData(TxGroup txGroup) {
		if (txGroup != null) {


			groupNum.setText(smileyParser.addSmileySpans(txGroup.group_id + "",
					true, 0));

			if (avatar != null) {
				mTouxiang.setImageBitmap(avatar);
			} else {
				mTouxiang.setImageResource(R.drawable.qun_default);
			}

			// if (TxGroup.isPublicGroup(txGroup)) {
			// RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)
			// mPrompt
			// .getLayoutParams();
			// lp.topMargin = Utils.dip2px(80f, this);
			// mPrompt.setLayoutParams(lp);
			// mAddFriend.setVisibility(View.GONE);
			//
			// mJoin.getLayoutParams().width = LayoutParams.FILL_PARENT;
			// mJoin.setTextSize(15);
			// mJoin.setText("进入聊天室");
			// mJoin.setTextColor(Color.WHITE);
			// mJoin.setBackgroundResource(R.drawable.green_normal);
			// // RelativeLayout.LayoutParams lp2 =
			// // (RelativeLayout.LayoutParams)mRecommend.getLayoutParams();
			// // lp2.bottomMargin = Utils.dip2px(150f, this);
			// // mRecommend.setLayoutParams(lp2);
			// //
			// //
			// findViewById(R.id.group_shareweibo).setBackgroundResource(R.drawable.preference_single_background);
			// // findViewById(R.id.group_email).setVisibility(View.GONE);
			// // findViewById(R.id.group_message).setVisibility(View.GONE);
			// }

		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		init();
	}

	@Override
	public void onClick(View v) {
		String text = "我在神聊创建了一个聊天室：【" + txGroup.group_title + "】，聊天室号是：【"
				+ txGroup.group_id + "】，快来加入和我一起群聊吧。手机下载 http://shenliao.com";
		switch (v.getId()) {
		case R.id.group_create_add_member:
			Intent i = new Intent(GroupCreateResult.this,
					SelectFriendListActivity.class);
			i.putExtra(SelectFriendListActivity.CHAT_TYPE,
					SelectFriendListActivity.CHAT_TYPE_GROUP);
			i.putExtra(SelectFriendListActivity.CHAT_TYPE_GROUP_OGJ, txGroup);
			startActivity(i);
			break;
		case R.id.group_create_join:
			TxData.finishOne(GroupCreateResult.class.getName());
			Intent intent = new Intent(this, GroupMsgRoom.class);
			intent.putExtra(Utils.MSGROOM_TX_GROUP, txGroup);
			startActivity(intent);
			break;
		case R.id.group_shareweibo:
			String userid = prefs.getString(CommonData.WEIBO_USER_ID + "�"
					+ TX.tm.getUserID(), "");
			Long overTime = prefs.getLong(CommonData.WEIBO_OVER_TIME + "�"
					+ TX.tm.getUserID(), 0);
			if ((!"".equals(userid)) && overTime > System.currentTimeMillis()) {
				i = new Intent(this, OAuthShareWeiboActivity.class);
			} else {
				i = new Intent(this, WebViewActivity.class);
				// Utils.inPhoto=true;
			}
			i.putExtra(WebViewActivity.LOGIN_STATE,
					WebViewActivity.TO_SHARE_GROUP);
			i.putExtra(WebViewActivity.SHARE_GROUP, txGroup);
			startActivity(i);
			break;
		case R.id.group_email:
			i = new Intent(Intent.ACTION_SEND);
			i.setType("message/rfc822");
			i.putExtra(Intent.EXTRA_SUBJECT, "欢迎加入我的神聊聊天室");
			i.putExtra(Intent.EXTRA_TEXT, text);
			startActivity(i);
			break;
		case R.id.group_message:
			Uri uri = Uri.parse("smsto:");
			i = new Intent(Intent.ACTION_SENDTO, uri);
			i.putExtra("sms_body", text);
			startActivity(i);
			break;
		case R.id.btn_back_left:
			GroupCreateResult.this.finish();
			break;
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (!hasDisplayAnim && hasFocus && group_create_success != null
				&& group_index_title != null) {// 界面全部加在完毕

			animate(group_create_success).x(0).y(group_index_title.getHeight())
					.setDuration(1000).setListener(new AnimatorListener() {

						@Override
						public void onAnimationStart(Animator animation) {

						}

						@Override
						public void onAnimationRepeat(Animator animation) {

						}

						@Override
						public void onAnimationEnd(Animator animation) {
							animate(group_create_success).x(0).y(0)
									.setDuration(1000).setStartDelay(1000);
							hasDisplayAnim = true;
						}

						@Override
						public void onAnimationCancel(Animator animation) {

						}
					});

		}
		super.onWindowFocusChanged(hasFocus);
	}

}
