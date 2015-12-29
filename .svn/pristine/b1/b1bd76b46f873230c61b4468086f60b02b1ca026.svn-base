package com.shenliao.set.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.group.util.GroupUtils;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.activity.SettingsPreference;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

/**
 * 辅助功能
 * 
 * @author xch
 * 
 */
public class SetAssistFunctionActivity extends BaseActivity implements OnCheckedChangeListener, OnClickListener {
	private CheckBox refuseReqChk;//拒绝好友请求
	private CheckBox downLoadAudioCheck;// 自动下载音频
	private CheckBox downLoadImageCheck;// 自动下载图片
	private CheckBox backGroudWireCheck;//开启后台线控
	private LinearLayout clearCacheLinear;// 清除缓存
	private LinearLayout clearRecordLinear;// 清除聊天记录
	private TextView fileSize;//缓存文件大小
	private SharedPreferences prefs;
	private Editor editor;
	private DeleteReceiver deletereceiver;
	private LinearLayout btn_back_left;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_update_assist_function);
		TxData.addActivity(this);

		init();
	}

	// 初始化
	private void init() {
		clearRecordLinear = (LinearLayout) findViewById(R.id.sl_tab_setting_update_assist_function_clearRecord);
		btn_back_left = (LinearLayout) findViewById(R.id.btn_back_left);
		clearCacheLinear = (LinearLayout) findViewById(R.id.sl_tab_setting_update_assist_function_clearCache);
		downLoadAudioCheck = (CheckBox) findViewById(R.id.sl_tab_setting_update_assist_function_checkBox_downloadAudio);
		downLoadImageCheck = (CheckBox) findViewById(R.id.sl_tab_setting_update_assist_function_checkBox_downloadImage);
        backGroudWireCheck=(CheckBox) findViewById(R.id.sl_tab_setting_update_assist_function_checkBox_wire);
		refuseReqChk = (CheckBox) findViewById(R.id.sl_tab_setting_update_assist_function_checkBox_noJoin);
		refuseReqChk.setChecked(!TX.tm.getTxMe().isReceiveReq());
		fileSize = (TextView) findViewById(R.id.sl_tab_setting_inculde_signText);
		prefs = getSharedPreferences(SettingsPreference.TUIXIN_SETTING, Context.MODE_WORLD_READABLE
				+ Context.MODE_WORLD_WRITEABLE);
		editor = prefs.edit();

		downLoadAudioCheck.setChecked(prefs.getBoolean(
				SetAssistFunctionActivity.this.getString(R.string.pref_key_save_download_audio), true));
		downLoadImageCheck.setChecked(prefs.getBoolean(
				SetAssistFunctionActivity.this.getString(R.string.pref_key_save_download_pic), true));
		
		backGroudWireCheck.setChecked(prefs.getBoolean(
				SetAssistFunctionActivity.this.getString(R.string.pref_key_save_backgroud_wire), false));

		clearRecordLinear.setOnClickListener(this);
		clearCacheLinear.setOnClickListener(this);
		btn_back_left.setOnClickListener(this);
		downLoadAudioCheck.setOnCheckedChangeListener(this);
		downLoadImageCheck.setOnCheckedChangeListener(this);
		backGroudWireCheck.setOnCheckedChangeListener(this);
		refuseReqChk.setOnCheckedChangeListener(this);
		Utils.scanFilesSize();
	}

	// checkbox监听事件
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (downLoadAudioCheck == (CheckBox) buttonView) {
			prefs.edit()
					.putBoolean(this.getString(R.string.pref_key_save_download_audio), downLoadAudioCheck.isChecked())
					.commit();

		} else if (downLoadImageCheck == (CheckBox) buttonView) {
			prefs.edit()
					.putBoolean(this.getString(R.string.pref_key_save_download_pic), downLoadImageCheck.isChecked())
					.commit();

		} else if (refuseReqChk == (CheckBox) buttonView) {
			mSess.getSocketHelper().sendRefuseReq(refuseReqChk.isChecked() ? 1 : 0);
		}else if(backGroudWireCheck==(CheckBox) buttonView){
			prefs.edit().putBoolean(this.getString(R.string.pref_key_save_backgroud_wire), backGroudWireCheck.isChecked()).commit();
		}

	}

	// 点击事件监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sl_tab_setting_update_assist_function_clearCache:
			GroupUtils.showDialog(SetAssistFunctionActivity.this, 0, R.string.dialog_clearbtn, R.string.dialog_nobtn,
					R.string.dialog_title, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							showDialogTimer(SetAssistFunctionActivity.this, 0, R.string.delele_file_ing,
									Integer.MAX_VALUE, new BaseTimerTask() {
										public void run() {
											super.run();
											Looper.prepare();
											Toast.makeText(SetAssistFunctionActivity.this, R.string.delele_failed,
													Toast.LENGTH_SHORT).show();
											Looper.loop();
										}
									}).show();
							Utils.clearSdFile();
						}
					});

			break;
		case R.id.sl_tab_setting_update_assist_function_clearRecord:
			GroupUtils.showDialog(SetAssistFunctionActivity.this, 0, R.string.dialog_clearbtn, R.string.dialog_nobtn,
					R.string.dialog_title_clear_record, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							showDialogTimer(SetAssistFunctionActivity.this, 0, R.string.delele_file_ing, 30 * 1000,
									new BaseTimerTask() {
										public void run() {
											super.run();
											Looper.prepare();
											Toast.makeText(SetAssistFunctionActivity.this, R.string.delele_failed,
													Toast.LENGTH_SHORT).show();
											Looper.loop();
										}
									}).show();
							Utils.clearAllMsgs();
						}
					});
			break;
		case R.id.btn_back_left:
			finish();
			break;
		}
	}

	public class DeleteReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			cancelDialogTimer();
			if (Constants.INTENT_ACTION_CLEAR_AVATAR_FINISH.equals(intent.getAction())) {
				Toast.makeText(SetAssistFunctionActivity.this, R.string.delele_success, Toast.LENGTH_SHORT).show();
				fileSize.setText("0MB");
			} else if (Constants.INTENT_ACTION_CLEAR_ALL_MSGS_FINISH.equals(intent.getAction())) {

				Toast.makeText(SetAssistFunctionActivity.this, R.string.delele_success, Toast.LENGTH_SHORT).show();
			} else if (Constants.INTENT_ACTION_SCAN_FILE_FINISH.equals(intent.getAction())) {
				fileSize.setText(((intent.getLongExtra("fileSize", 0) - 5) < 0 ? 0 : (intent
						.getLongExtra("fileSize", 0) - 5)) + "MB");
			}

		}
	}

	private void registReceiver() {

		if (deletereceiver == null) {
			deletereceiver = new DeleteReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_CLEAR_AVATAR_FINISH);
			filter.addAction(Constants.INTENT_ACTION_CLEAR_ALL_MSGS_FINISH);
			filter.addAction(Constants.INTENT_ACTION_SCAN_FILE_FINISH);
			this.registerReceiver(deletereceiver, filter);
		}
	}

	@Override
	protected void onStop() {
		if (deletereceiver != null) {
			this.unregisterReceiver(deletereceiver);
			deletereceiver = null;
		}
		super.onStop();
	}

	@Override
	protected void onResume() {
		registReceiver();
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

}
