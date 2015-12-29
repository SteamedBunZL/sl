package com.tuixin11sms.tx.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.tuixin11sms.tx.R;

public class WarnDialogAcitvity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.warndialog);
		
		Intent intent = getIntent();

		String stringExtra = intent.getStringExtra("rs");
		new AlertDialog.Builder(WarnDialogAcitvity.this)
				.setTitle("警告")
				.setCancelable(false)
				.setMessage("你由于“" + stringExtra + "”被警告了，请自觉遵守神聊管理规范并保护聊天室秩序。")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int i) {
						dialog.cancel();
						finish();
					}
				}).show();// 显示对话框
	}
}
