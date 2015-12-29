package com.shenliao.user.activity;

import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.data.DataContainer;
import com.shenliao.set.activity.SetUpdateAreaActivity;
import com.shenliao.set.activity.SetUpdateLanguageActivity;
import com.shenliao.set.activity.SetUserInfoActivity;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.activity.FindConditionFriendActivity;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.StringUtils;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 完善个人资料页
 * 
 * @author xch
 * 
 */
public class UserInfoPerfectActivity extends BaseActivity implements
		OnClickListener {
	private static final int FLUSH_ADAPTER = 1;// 刷新
	private static final int FLUSH_BIRTH = 4;
	private LinearLayout mUserSexLinear;// 性别模块
	private LinearLayout mUserAreaLinear;// 地区模块
	private LinearLayout mUserBloodTypeLinear;// 血型模块
	// private LinearLayout mUserProfessionLinear;// 职业模块
	private LinearLayout mUserFavouriteLinear;// 兴趣爱好模块
	private LinearLayout mUserBirthLinear;// 出生日期模块
	public static final int REQUESTCODE_FOR_REQUSET_NICKNAME = 1;// 昵称请求码
	public static final int FLUSH_SEX = 2;
	public static final int FLUSH_BLOOD = 3;
	public static final int RESULTCODE_FOR_RESULT_NICENAME = 2;// 昵称返回请求码
	public static final int REQUESTCODE_FOR_REQUSET_PROSESSION = 3;// 职业请求码
	public static final int RESULTCODE_FOR_RESULT_PROSESSION = 4;// 职业返回码
	public static final int REQUESTCODE_FOR_REQUSET_LANGUAGE = 5;// 语言请求码
	public static final int RESULTCODE_FOR_RESULT_LANGUAGE = 6;// 语言返回码
	public static final int REQUESTCODE_FOR_REQUSET_FAVOURITE = 7;// 语言请求码
	public static final int RESULTCODE_FOR_RESULT_FAVOURITE = 8;// 语言返回码
	public static final int REQUESTCODE_FOR_REQUSET_AREA = 9;// 语言请求码
	public static final int RESULTCODE_FOR_RESULT_AREA = 10;// 语言返回码

	private int goinpage = 100;
	private TextView mUserSex;// 性别
	private TextView mUserArea;// 地区
	private TextView mUserBloodType;// 血型
	private TextView mUserProfession;// 职业
	private TextView mUserFavourite;// 兴趣爱好
	private TextView mUserBirth;// 出生日期
	// private SharedPreferences prefs;
	// private Editor editor;
	private SmileyParser sParser;// 解析表情
	private String[] bloodtypes;// 血型集合 ,0是A，1是B，2是AB，3是O
	private String[] sexs;// 性别集合，0是男，1是女
	private TX myTx;
	private Button commitBtn;
	// 生日参数
	private int year;
	private int month;
	private int day;
	private int currentYear;
	private int currentMonth;
	private int currentDay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userinfo_perfect_info);
		init();
		setData();
	}

	private void init() {
		mUserSexLinear = (LinearLayout) findViewById(R.id.search_add_perfect_sex);
		mUserAreaLinear = (LinearLayout) findViewById(R.id.search_add_perfect_area);
		mUserBloodTypeLinear = (LinearLayout) findViewById(R.id.search_add_perfect_bloodtype);
		btn_back_left = (LinearLayout) findViewById(R.id.btn_back_left);
		// mUserProfessionLinear = (LinearLayout)
		// findViewById(R.id.search_add_perfect_age);
		mUserBirthLinear = (LinearLayout) findViewById(R.id.sl_userinfo_perfect_info_include_birth);
		mUserFavouriteLinear = (LinearLayout) findViewById(R.id.search_add_perfect_fav);
		mUserSex = (TextView) findViewById(R.id.sl_tab_setting_perfect_include_sex);
		mUserArea = (TextView) findViewById(R.id.sl_tab_setting_perfect_include_area);
		mUserBloodType = (TextView) findViewById(R.id.sl_tab_setting_perfect_include_bloodtype);
		mUserFavourite = (TextView) findViewById(R.id.sl_tab_setting_perfect_include_fav);
		mUserProfession = (TextView) findViewById(R.id.sl_tab_setting_perfect_include_profession);
		mUserBirth = (TextView) findViewById(R.id.sl_tab_setting_userinfo_include_birth);
		commitBtn = (Button) findViewById(R.id.search_add_perfect_sendBtn);

		bloodtypes = this.getResources().getStringArray(
				R.array.bloodtype_dialog_items);
		sexs = this.getResources().getStringArray(R.array.sex_dialog_items);
		// prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		// editor = prefs.edit();
		sParser = Utils.getSmileyParser(this);
		myTx = TX.tm.getTxMe();
		mUserBirthLinear.setOnClickListener(this);
		mUserSexLinear.setOnClickListener(this);
		mUserAreaLinear.setOnClickListener(this);
		mUserBloodTypeLinear.setOnClickListener(this);
		// mUserProfessionLinear.setOnClickListener(this);
		mUserFavouriteLinear.setOnClickListener(this);
		commitBtn.setOnClickListener(this);

		btn_back_left.setOnClickListener(this);

	}

	private void setData() {

		// 性别
		mUserSex.setText(myTx.getSex() == TX.MALE_SEX ? this.getResources()
				.getString(R.string.user_male) : this.getResources().getString(
				R.string.user_female));
		// 地区
		if (myTx.area != null && !"".equals(myTx.area)) {
			List<String> mlist = StringUtils.str2List(myTx.area);
			mUserArea.setText(DataContainer.getAreaNameByIds(mlist
					.toArray(new String[0])));
		} else {
			mUserArea.setTextColor(this.getResources().getColor(R.color.red));
			mUserArea.setText("未填写");
		}
		// 生日
		showBirthDay(""+myTx.birthday);
		// 血型
		showBloodType(myTx.bloodType);
		// 语言
		if (!Utils.isNull(myTx.getLanguages())) {
			List<String> mlist = StringUtils.str2List(myTx.getLanguages());
			if (mlist.contains("0")) {
				mlist.remove("0");
			}
			if (mlist != null && mlist.size() > 0) {
				mUserFavourite.setText(DataContainer.getLangNameByIds(mlist
						.toArray(new String[0])));
			} else {
				mUserFavourite.setTextColor(this.getResources().getColor(
						R.color.red));
				mUserFavourite.setText("未填写");
			}
		} else {
			mUserFavourite.setTextColor(this.getResources().getColor(
					R.color.red));
			mUserFavourite.setText("未填写");
		}
	}

	// 显示生日方法
	private void showBirthDay(String birthday) {

		if (!"0".equals(birthday) && birthday.length() == 8) {
			int nowyear = Integer.valueOf(birthday.substring(0, 4));
			int nowmonth = Integer.valueOf(birthday.substring(4, 6));
			int nowday = Integer.valueOf(birthday.substring(6, 8));
			mUserBirth.setTextColor(UserInfoPerfectActivity.this.getResources()
					.getColor(R.color.sub_title_text_color));
			mUserBirth.setText(nowyear
					+ UserInfoPerfectActivity.this.getResources().getString(
							R.string.year_prompt)
					+ nowmonth
					+ UserInfoPerfectActivity.this.getResources().getString(
							R.string.month_prompt)
					+ nowday
					+ UserInfoPerfectActivity.this.getResources().getString(
							R.string.day_prompt));
		} else {
			mUserBirth.setTextColor(this.getResources().getColor(R.color.red));
			mUserBirth.setText("未填写");
		}

	}

	// 显示血型
	private void showBloodType(int blood_type) {
		int ibloodType = blood_type;
		if (ibloodType == -1) {
			mUserBloodType.setTextColor(this.getResources().getColor(
					R.color.red));
			mUserBloodType.setText("未填写");
		} else if (ibloodType >= bloodtypes.length) {
			mUserBloodType.setText(bloodtypes[4]);
		} else {
			mUserBloodType.setText(bloodtypes[ibloodType]);
		}
	}

	// 单击事件监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 性别
		case R.id.search_add_perfect_sex:
			Builder dialogSex = new AlertDialog.Builder(
					UserInfoPerfectActivity.this).setItems(sexs,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which < sexs.length) {
								// editor.putInt(CommonData.SEX,
								// which).commit();
								mSess.mPrefMeme.sex.setVal(which).commit();
								TX.tm.reloadTXMe();// //////
								// mUserBloodType.setText(bloodtypes[which]);
								Message msg = new Message();
								msg.what = FLUSH_SEX;
								handler.sendMessage(msg);
								mSess.getSocketHelper()
										.sendUserInforChange();
							}
						}
					});
			dialogSex.setTitle(this.getResources().getString(
					R.string.user_sel_sex));
			dialogSex.show();
			break;
		// 地区
		case R.id.search_add_perfect_area:
			Intent intentArea = new Intent(UserInfoPerfectActivity.this,
					SetUpdateAreaActivity.class);
			intentArea.putExtra(SetUpdateAreaActivity.GOINPAGE,
					SetUpdateAreaActivity.PERFECTINFO);
			startActivityForResult(intentArea, REQUESTCODE_FOR_REQUSET_AREA);
			break;
		// 血型
		case R.id.search_add_perfect_bloodtype:
			Builder dialogBlood = new AlertDialog.Builder(
					UserInfoPerfectActivity.this).setItems(bloodtypes,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which < bloodtypes.length) {
								// editor.putInt(CommonData.BLOODTYPE,
								// which).commit();
								mSess.mPrefMeme.bloodtype.setVal(which)
										.commit();
								TX.tm.reloadTXMe();// //
								// mUserBloodType.setText(bloodtypes[which]);
								Message msg = new Message();
								msg.what = FLUSH_BLOOD;
								handler.sendMessage(msg);
								mSess.getSocketHelper()
										.sendUserInforChange();
							}
						}
					});
			dialogBlood.setTitle(this.getResources().getString(
					R.string.user_sel_bloodtype));
			dialogBlood.show();
			break;
		// // 职业
		// case R.id.search_add_perfect_age:
		// Intent intentPro = new Intent(UserInfoPerfectActivity.this,
		// SetUpdateProfessionActivity.class);
		// intentPro.putExtra(SetUpdateProfessionActivity.GOINPAGE,
		// SetUpdateProfessionActivity.PERFECTNIFO);
		// startActivityForResult(intentPro,
		// REQUESTCODE_FOR_REQUSET_PROSESSION);
		// break;
		// 语言
		case R.id.search_add_perfect_fav:
			Intent intentFavor = new Intent(UserInfoPerfectActivity.this,
					SetUpdateLanguageActivity.class);
			intentFavor.putExtra(SetUpdateLanguageActivity.GOINPAGE,
					SetUpdateLanguageActivity.NOTFINDFRIEND);
			startActivityForResult(intentFavor,
					SetUserInfoActivity.REQUESTCODE_FOR_REQUSET_LANGUAGE);
			break;
		// 完成按钮事件
		case R.id.search_add_perfect_sendBtn:

			if (Utils.isNull(TX.tm.getTxMe().getLanguages())
					|| Utils.isNull(String.valueOf(TX.tm.getTxMe().getSex()))
					|| Utils.isNull(String.valueOf(TX.tm.getTxMe().bloodType))
					|| TX.tm.getTxMe().birthday==0
					|| Utils.isNull(TX.tm.getTxMe().area)) {
				Toast.makeText(UserInfoPerfectActivity.this, "请完善个人资料",
						Toast.LENGTH_LONG).show();

			} else {
				Intent intent = new Intent(UserInfoPerfectActivity.this,
						FindConditionFriendActivity.class);
				startActivity(intent);
				UserInfoPerfectActivity.this.finish();
			}
			// UserInfoPerfectActivity.this.finish();

			break;
		case R.id.sl_userinfo_perfect_info_include_birth:
			String birthStr = String.valueOf(myTx.birthday);
			if (myTx.birthday!=0 && birthStr.length() == 8) {
				year = Integer.valueOf(birthStr.substring(0, 4));
				month = Integer.valueOf(birthStr.substring(4, 6)) - 1;
				day = Integer.valueOf(birthStr.substring(6, 8));
			} else {
				year = 1990;
				month = 0;
				day = 1;

			}
			// if (year == 0 && month == 0 && day == 0) {
			// year = currentYear - 20;
			// month = currentMonth;
			// day = currentDay;
			// if (year < 0)
			// year = 0;
			// }
			new DatePickerDialog(UserInfoPerfectActivity.this,
					onDateSetListener, year, month, day).show();
			break;
		case R.id.btn_back_left:
			UserInfoPerfectActivity.this.finish();
			break;
		}
	}

	DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int month, int day) {
			String tempMonth = "";
			if (month + 1 < 10) {
				tempMonth = "0" + (month + 1);
			} else {
				tempMonth = "" + (month + 1);
			}
			String tempDay = "";
			if (day < 10) {
				tempDay = "0" + day;
			} else {
				tempDay = "" + day;
			}
			// editor.putInt(CommonData.BIRTHDAY, Integer.valueOf("" + year +
			// tempMonth + tempDay)).commit();
			mSess.mPrefMeme.birthday.setVal(
					Integer.valueOf("" + year + tempMonth + tempDay)).commit();
			TX.tm.reloadTXMe();// //
			UserInfoPerfectActivity.this.year = year;
			UserInfoPerfectActivity.this.month = month;
			UserInfoPerfectActivity.this.day = day;
			Message msg = new Message();
			msg.what = FLUSH_BIRTH;
			handler.sendMessage(msg);
			mSess.getSocketHelper().sendUserInforChange();
		}
	};
	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			int num = msg.what;
			switch (num) {
			case FLUSH_SEX:
				// int isex = prefs.getInt(CommonData.SEX, -1);
				int isex = mSess.mPrefMeme.sex.getVal();
				mUserSex.setTextColor(UserInfoPerfectActivity.this
						.getResources().getColor(R.color.sub_title_text_color));
				mUserSex.setText(isex == TX.MALE_SEX ? UserInfoPerfectActivity.this
						.getResources().getString(R.string.user_male)
						: UserInfoPerfectActivity.this.getResources()
								.getString(R.string.user_female));

				break;
			case FLUSH_BLOOD:
				// int ibloodType = prefs.getInt(CommonData.BLOODTYPE, -1);
				int ibloodType = mSess.mPrefMeme.bloodtype.getVal();

				if (ibloodType == -1) {
					mUserBloodType.setTextColor(UserInfoPerfectActivity.this
							.getResources().getColor(R.color.red));
					mUserBloodType.setText("未填写");
				} else if (ibloodType >= bloodtypes.length) {
					mUserBloodType.setTextColor(UserInfoPerfectActivity.this
							.getResources()
							.getColor(R.color.sub_title_text_color));
					mUserBloodType.setText(bloodtypes[4]);
				} else {
					mUserBloodType.setTextColor(UserInfoPerfectActivity.this
							.getResources()
							.getColor(R.color.sub_title_text_color));
					mUserBloodType.setText(bloodtypes[ibloodType]);
				}
				break;
			case FLUSH_BIRTH:
				// showBirthDay(prefs.getInt(CommonData.BIRTHDAY, 0) + "");
				showBirthDay(mSess.mPrefMeme.birthday.getVal() + "");
				break;

			}
		}
	};
	private LinearLayout btn_back_left;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUESTCODE_FOR_REQUSET_PROSESSION
				&& resultCode == RESULTCODE_FOR_RESULT_PROSESSION) {
			mUserProfession.setTextColor(UserInfoPerfectActivity.this
					.getResources().getColor(R.color.sub_title_text_color));
			// mUserProfession.setText(sParser.addSmileySpans(prefs.getString(CommonData.JOB,
			// ""), true, 0));
			mUserProfession.setText(sParser.addSmileySpans(
					mSess.mPrefMeme.job.getVal(), true, 0));

		} else if (requestCode == REQUESTCODE_FOR_REQUSET_AREA
				&& resultCode == RESULTCODE_FOR_RESULT_AREA) {
			mUserArea.setTextColor(UserInfoPerfectActivity.this.getResources()
					.getColor(R.color.sub_title_text_color));
			// mUserArea.setText(DataContainer.getAreaNameByIds(StringUtils.str2List(prefs.getString(CommonData.AREA,
			// ""))
			// .toArray(new String[0])));
			mUserArea.setText(DataContainer.getAreaNameByIds(StringUtils
					.str2List(mSess.mPrefMeme.area.getVal()).toArray(
							new String[0])));
		} else if (requestCode == SetUserInfoActivity.REQUESTCODE_FOR_REQUSET_LANGUAGE
				&& resultCode == SetUserInfoActivity.RESULTCODE_FOR_RESULT_LANGUAGE) {
			// List<String> mlist =
			// StringUtils.str2List(prefs.getString(CommonData.LANGUAGES, ""));
			List<String> mlist = StringUtils.str2List(mSess.mPrefMeme.languages
					.getVal());
			if (mlist != null && mlist.contains("0")) {
				mlist.remove("0");
			}
			if(mlist!=null){
				mUserFavourite.setTextColor(UserInfoPerfectActivity.this
						.getResources().getColor(R.color.sub_title_text_color));
				mUserFavourite.setText(DataContainer.getLangNameByIds(mlist
						.toArray(new String[0])));
			}
		}
	}

}
