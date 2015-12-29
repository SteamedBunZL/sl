package com.tuixin11sms.tx.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

public class UnkownFileActivity extends BaseActivity {
	private static final String TAG = "UnkownFileActivity";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_open_unkown_file);
		final String filePath = getIntent().getStringExtra(Constants.FILE_LOCAL_PATH);
		final RelativeLayout rl_unkownFile = (RelativeLayout) findViewById(R.id.rl_unkown_file);
		rl_unkownFile.setVisibility(View.VISIBLE);
		Button btn_openOtherApp = (Button) rl_unkownFile.findViewById(R.id.btn_open_with_other_app);
		final TextView tv_unkownFileName = (TextView) rl_unkownFile.findViewById(R.id.tv_unkown_file_name);
		tv_unkownFileName.setText(filePath.substring(filePath.lastIndexOf("/") + 1));
		btn_openOtherApp.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					if (!TextUtils.isEmpty(filePath)) {
						String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
						if (!TextUtils.isEmpty(fileName)) {
							if (fileName.substring(fileName.lastIndexOf(".")+1).equalsIgnoreCase("apk")) {
								//打开apk软件
								Utils.openAPKFile(thisContext, filePath);
							}else {
								//打开未知文件
								Utils.openUnkownFile(thisContext, filePath);
							}
						}
					}
				} catch (Exception e) {
					if (Utils.debug)
						Log.e(TAG, "打开文件异常", e);
					showToast("无法打开此文件");
				}
			}
		});

	}

}
