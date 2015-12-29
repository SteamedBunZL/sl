package com.tuixin11sms.tx.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.data.DataContainer;
import com.shenliao.data.SearchData;
import com.shenliao.set.activity.SetUpdateAreaActivity;
import com.shenliao.set.activity.SetUpdateLanguageActivity;
import com.shenliao.user.activity.UserInfoPerfectActivity;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.utils.StringUtils;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

/**
 * 通过条件查找好友
 * 
 * @author xch
 * 
 */
public class FindConditionFriendActivity extends BaseActivity implements
		OnClickListener {
	private LinearLayout sex;// 性别区域
	private LinearLayout age;// 年龄区域
	private LinearLayout area;// 地区区域
	private LinearLayout constellation;// 星座区域
	private LinearLayout bloodType;// 血型区域
	private LinearLayout language;// 语言区域
	private TextView sexText;// 性别
	private TextView ageText;// 年龄
	private TextView constellationText;// 星座
	private TextView bloodTypeText;// 血型
	private TextView languageText;// 语言
	private TextView areaText;// 地区
	private EditText edit;// 昵称
	private TextView searchBtn;// 搜索按钮
	private CheckBox isOnlineCheck;// 是否在线
	private String[] sexs;// 性别集合，0是男，1是女
	private String[] ages;// 年龄集合,0是不限，1是17-22,2是23-30,3是31-40,4是40以上
	private String[] constellations;// 星座集合
	private String[] bloodTypes;// 血型集合
	private Dialog dialog;
	private View dialogView;
	private LayoutInflater inflater;
	public static final int REQUESTCODE_FOR_REQUSET_LANGUAGE_FIND = 5;// 语言请求码
	public static final int RESULTCODE_FOR_RESULT_LANGUAGE_FIND = 6;// 语言返回码
	public static final int REQUESTCODE_FOR_REQUSET_AREA = 9;// 地区请求码
	public static final int RESULTCODE_FOR_RESULT_AREA = 10;// 地区返回码
	private TX tx = new TX();
	private float al;
	private List<String> list = new ArrayList<String>();
	// private SharedPreferences prefs;
	// private Editor editor;
	private static final int FLUSH_SEX = 1;// 刷新性别
	private static final int FLUSH_AGE = 2;// 刷新年龄
	private static final int FLUSH_CONSTELLLATIONS = 3;// 刷新星座
	private static final int FLUSH_BLOODTYPE = 4;// 刷新血型
	private boolean isbtn = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_add_condition_detail);
		init();
		setData();
	}

	// 初始化
	private void init() {
		inflater = LayoutInflater.from(this);
		sex = (LinearLayout) findViewById(R.id.search_add_condition_sex);
		age = (LinearLayout) findViewById(R.id.search_add_condition_age);
		area = (LinearLayout) findViewById(R.id.search_add_condition_area);
		constellation = (LinearLayout) findViewById(R.id.search_add_condition_constellation);
		bloodType = (LinearLayout) findViewById(R.id.search_add_condition_bloodtype);
		language = (LinearLayout) findViewById(R.id.search_add_condition_language);
		sexText = (TextView) findViewById(R.id.search_add_sex);
		ageText = (TextView) findViewById(R.id.search_add_user_age);
		constellationText = (TextView) findViewById(R.id.search_add_user_constellation);
		bloodTypeText = (TextView) findViewById(R.id.search_add_user_bloodtype);
		languageText = (TextView) findViewById(R.id.search_add_user_language);
		areaText = (TextView) findViewById(R.id.search_add_user_area);
		edit = (EditText) findViewById(R.id.userinfo_search_input_box);
		searchBtn = (TextView) findViewById(R.id.search_add_condition_sendBtn);
		isOnlineCheck = (CheckBox) findViewById(R.id.sl_search_include_check);

		btn_back_left = (LinearLayout) findViewById(R.id.btn_back_left);
		btn_back_left.setOnClickListener(this);
		sex.setOnClickListener(this);
		age.setOnClickListener(this);
		area.setOnClickListener(this);
		constellation.setOnClickListener(this);
		bloodType.setOnClickListener(this);
		language.setOnClickListener(this);
		searchBtn.setOnClickListener(this);

		bloodTypes = this.getResources().getStringArray(
				R.array.search_bloodtype_dialog_items);
		constellations = this.getResources().getStringArray(
				R.array.search_constellation_dialog_items);
		ages = this.getResources().getStringArray(
				R.array.search_age_dialog_items);
		sexs = this.getResources().getStringArray(
				R.array.search_sex_dialog_items);
		// prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		// editor = prefs.edit();

		// if (Utils.isNull(TX.tm.getTxMe().area) ||
		// Utils.isNull(String.valueOf(TX.tm.getTxMe().sex))
		// || String.valueOf(TX.tm.getTxMe().blood_type) == null ||
		// TX.tm.getTxMe().job == null
		// || TX.tm.getTxMe().hobby == null) {
		// showDialog();
		// }
	}

	private void setData() {
		tx = SearchData.getInstance(mSess.getContext()).getSearch();
		if (tx != null) {
			sexText.setText(sexs[tx.getSex()]);
			ageText.setText(ages[tx.age]);
			List<String> mlist = StringUtils.str2List(tx.area);
			areaText.setText(DataContainer.getAreaNameByIds(mlist
					.toArray(new String[0])));
			constellationText.setText(constellations[Integer
					.parseInt(tx.constellation)]);
			bloodTypeText.setText(bloodTypes[tx.bloodType]);
			List<String> langList = StringUtils.str2List(tx.getLanguages());
			languageText.setText(DataContainer.getLangNameByIds(langList
					.toArray(new String[0])));

			if (tx.getOnLine() == 1) {
				isOnlineCheck.setChecked(true);
			}

		}

	}

	@Override
	protected void onResume() {
		if (Utils.debug) {
			Log.e("Zzl7", "languarge + " + TX.tm.getTxMe().getLanguages());
			Log.e("Zzl7", "sex + " + String.valueOf(TX.tm.getTxMe().getSex()));
			Log.e("Zzl7",
					"bloodtype + " + String.valueOf(TX.tm.getTxMe().bloodType));
			Log.e("Zzl7", "area + " + TX.tm.getTxMe().area);
		}
		if (Utils.isLangNull(TX.tm.getTxMe().getLanguages())
		|| Utils.isNull(String.valueOf(TX.tm.getTxMe().getSex()))
				|| Utils.isNull(String.valueOf(TX.tm.getTxMe().bloodType))
				|| TX.tm.getTxMe().birthday == 0
				|| Utils.isNull(TX.tm.getTxMe().area)) {

			showDialog();
		}
		list.clear();
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	// 单击事件监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 性别
		case R.id.search_add_condition_sex:
			final Builder dialogSex = new AlertDialog.Builder(
					FindConditionFriendActivity.this).setItems(sexs,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which < sexs.length) {
								tx.setSex(which);
								Message msg = new Message();
								msg.obj = which;
								msg.what = FLUSH_SEX;
								handler.sendMessage(msg);
							}
							if (dialog != null && ((Dialog) dialog).isShowing()) {
								dialog.cancel();

							}
						}
					});
			dialogSex.setTitle(this.getResources().getString(
					R.string.user_sel_sex));
			dialogSex.show();
			break;
		// 年龄
		case R.id.search_add_condition_age:
			Builder dialogAge = new AlertDialog.Builder(
					FindConditionFriendActivity.this).setItems(ages,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which < ages.length) {
								tx.age = which;
								Message msg = new Message();
								msg.obj = which;
								msg.what = FLUSH_AGE;
								handler.sendMessage(msg);

							}
						}
					});
			dialogAge.setTitle(this.getResources().getString(
					R.string.user_sel_age));
			dialogAge.show();
			break;
		// 地区
		case R.id.search_add_condition_area:
			Intent intentArea = new Intent(FindConditionFriendActivity.this,
					SetUpdateAreaActivity.class);
			intentArea.putExtra(SetUpdateAreaActivity.GOINPAGE,
					SetUpdateAreaActivity.FINDFIEND);
			startActivityForResult(intentArea, REQUESTCODE_FOR_REQUSET_AREA);
			break;
		// 星座
		case R.id.search_add_condition_constellation:
			Builder dialogconstellation = new AlertDialog.Builder(
					FindConditionFriendActivity.this).setItems(constellations,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which < constellations.length) {
								tx.constellation = String.valueOf(which);
								Message msg = new Message();
								msg.obj = which;
								msg.what = FLUSH_CONSTELLLATIONS;
								handler.sendMessage(msg);

							}
						}
					});
			dialogconstellation.setTitle(this.getResources().getString(
					R.string.user_sel_con));
			dialogconstellation.show();
			break;

		// 血型
		case R.id.search_add_condition_bloodtype:
			Builder dialogBlood = new AlertDialog.Builder(
					FindConditionFriendActivity.this).setItems(bloodTypes,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which < bloodTypes.length) {
								tx.bloodType = which;
								Message msg = new Message();
								msg.obj = which;
								msg.what = FLUSH_BLOODTYPE;
								handler.sendMessage(msg);

							}
						}
					});
			dialogBlood.setTitle(this.getResources().getString(
					R.string.user_sel_con));
			dialogBlood.show();
			break;

		// 语言
		case R.id.search_add_condition_language:
			Intent intent = new Intent(FindConditionFriendActivity.this,
					SetUpdateLanguageActivity.class);
			intent.putExtra(SetUpdateLanguageActivity.GOINPAGE,
					SetUpdateLanguageActivity.FINDFIEND);
			startActivityForResult(intent,
					REQUESTCODE_FOR_REQUSET_LANGUAGE_FIND);
			break;
		// 搜索按钮事件
		case R.id.search_add_condition_sendBtn:
			if (edit.getText().toString().equals("")
					&& sexText.getText().toString().equals("不限")
					&& ageText.getText().toString().equals("不限")
					&& constellationText.getText().toString().equals("不限")
					&& bloodTypeText.getText().toString().equals("不限")
					&& languageText.getText().toString().equals("不限")
					&& areaText.getText().toString().equals("不限")
					&& !isOnlineCheck.isChecked()) {
				Toast.makeText(FindConditionFriendActivity.this, "您没有选择搜索条件",
						Toast.LENGTH_SHORT).show();

			} else if (edit.getText().toString().length() > 24) {
				Toast.makeText(FindConditionFriendActivity.this, "您输入的昵称过长",
						Toast.LENGTH_SHORT).show();
			} else {
				tx.setNick_name(edit.getText().toString());
				Intent intentbtn = new Intent(FindConditionFriendActivity.this,
						SearchConditionResultActivity.class);

				if (edit.getText().toString() != null
						&& !edit.getText().toString().equals("")) {
					list.add(edit.getText().toString());

				}
				if (sexText.getText().toString() != null
						&& !sexText.getText().toString().equals("不限")) {
					list.add(sexText.getText().toString());
				}
				if (ageText.getText().toString() != null
						&& !ageText.getText().toString().equals("不限")) {
					list.add(ageText.getText().toString());
				}
				if (constellationText.getText().toString() != null
						&& !constellationText.getText().toString().equals("不限")) {
					list.add(constellationText.getText().toString());
				}
				if (bloodTypeText.getText().toString() != null
						&& !bloodTypeText.getText().toString().equals("不限")) {
					list.add(bloodTypeText.getText().toString() + "型");
				}
				if (languageText.getText().toString() != null
						&& !languageText.getText().toString().equals("不限")) {
					list.add(languageText.getText().toString());
				}
				if (areaText.getText().toString() != null
						&& !areaText.getText().toString().equals("不限")) {
					list.add(areaText.getText().toString());
				}
				if (isOnlineCheck.isChecked()) {
					tx.setOnLine(1);
					list.add("在线");
				} else {
					tx.setOnLine(-1);
				}
				intentbtn.putStringArrayListExtra(
						SearchConditionResultActivity.GRIDLIST,
						(ArrayList<String>) list);
				SearchFriendActivity.isOnline = false;
				intentbtn.putExtra("tx", tx);
				startActivity(intentbtn);
			}
			break;
		case R.id.btn_back_left:
			Utils.hideSoftInput(thisContext);
			FindConditionFriendActivity.this.finish();
			break;
		}

	}

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			int num = msg.what;
			String str = null;
			switch (num) {
			case FLUSH_SEX:

				int isex = (Integer) msg.obj;

				switch (isex) {
				case 0:
					sexText.setText(FindConditionFriendActivity.this
							.getResources().getString(R.string.user_male));
					str = "男";
					break;
				case 1:
					sexText.setText(FindConditionFriendActivity.this
							.getResources().getString(R.string.user_female));
					str = "女";
					break;
				case 2:
					sexText.setText("不限");
					break;
				}

				break;
			case FLUSH_AGE:
				int iage = (Integer) msg.obj;
				ageText.setText(ages[iage]);

				break;
			case FLUSH_CONSTELLLATIONS:
				int iconstellation = (Integer) msg.obj;
				constellationText.setText(constellations[iconstellation]);

				break;
			case FLUSH_BLOODTYPE:
				int ibloodtype = (Integer) msg.obj;
				bloodTypeText.setText(bloodTypes[ibloodtype]);

				break;
			}
		}
	};
	private LinearLayout btn_back_left;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUESTCODE_FOR_REQUSET_LANGUAGE_FIND
				&& resultCode == RESULTCODE_FOR_RESULT_LANGUAGE_FIND) {

			List<String> mlist = StringUtils.str2List(data
					.getStringExtra("language"));
			languageText.setText(DataContainer.getLangNameByIds(mlist
					.toArray(new String[0])));
			tx.setLanguages(data.getStringExtra("language"));
		} else if (requestCode == REQUESTCODE_FOR_REQUSET_AREA
				&& resultCode == RESULTCODE_FOR_RESULT_AREA) {
			List<String> mlist = StringUtils.str2List(data
					.getStringExtra("area"));
			areaText.setText(DataContainer.getAreaNameByIds(mlist
					.toArray(new String[0])));
			tx.setArea(data.getStringExtra("area"));
		}
		//

	}

	private void showDialog() {
		dialog = new Dialog(FindConditionFriendActivity.this,
				R.style.searchdialog);
		dialogView = inflater.inflate(R.layout.sll_search_dialog, null);
		dialog.setContentView(dialogView);
		TextView imageView = (TextView) dialogView
				.findViewById(R.id.dialog_imageView);
		Button button = (Button) dialogView.findViewById(R.id.dialog_btn);
		LayoutParams lay = dialog.getWindow().getAttributes();
		setParams(lay);
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		al = lp.alpha;
		lp.alpha = 0.2f;
		dialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {

				if (!isbtn) {
					FindConditionFriendActivity.this.finish();
				} else {
					WindowManager.LayoutParams lp = getWindow().getAttributes();
					lp.alpha = al;
					getWindow().setAttributes(lp);
				}

			}
		});
		getWindow().setAttributes(lp);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				isbtn = true;
				Intent intent = new Intent(FindConditionFriendActivity.this,
						UserInfoPerfectActivity.class);
				startActivity(intent);
				FindConditionFriendActivity.this.finish();
				if (dialog != null && dialog.isShowing()) {
					dialog.cancel();

				}
			}
		});
		imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (dialog != null && dialog.isShowing()) {
					dialog.cancel();
				}
			}
		});
		dialog.show();

	}

	private void setParams(LayoutParams lay) {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Rect rect = new Rect();
		View view = getWindow().getDecorView();
		view.getWindowVisibleDisplayFrame(rect);
		lay.height = dm.heightPixels - rect.top;
		lay.width = dm.widthPixels;

	}

	@Override
	protected void onStop() {
		if (dialog != null && dialog.isShowing()) {
			dialog.cancel();
		}

		super.onStop();
	}

}
