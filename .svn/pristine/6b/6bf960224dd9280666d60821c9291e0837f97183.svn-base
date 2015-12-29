package com.tuixin11sms.tx.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.activity.explorer.FolderExplorerActivity;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 聊天室中选择文件的activity
 * 
 * @author SHC
 * 
 */
public class SelectFileActivity extends BaseActivity {
	private static final String TAG = "SelectFileActivity";
	private View v_received_files;
	private View v_sdcard_folder;
	private View v_device_folder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_select_file);
		initViews();
	}

	// 初始化各个View显示
	private void initViews() {
		v_received_files = findViewById(R.id.rl_received_files_folder);
		v_sdcard_folder = findViewById(R.id.rl_sdcard_folder);
		v_device_folder = findViewById(R.id.rl_device_folder);
		v_received_files.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(thisContext, ReceivedFilesActivity.class);
				intent.putExtra(FolderExplorerActivity.IS_SEND_FILES, true);//发送目录文件而不是查看
				startActivityForResult(intent,BaseMsgRoom.REQ_TAKE_BIG_FILES);
				
			}
		});
		v_sdcard_folder.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(Utils.debug)Log.i(TAG,"打开遍历SD卡");
				Intent intent = new Intent(thisContext, FolderExplorerActivity.class);
				String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
				intent.putExtra(FolderExplorerActivity.FOLDER_ROOT_PATH, sdcardPath);
				intent.putExtra(FolderExplorerActivity.TITLE_NAME, "SD卡文件");
				intent.putExtra(FolderExplorerActivity.IS_SEND_FILES, true);
				startActivityForResult(intent, BaseMsgRoom.REQ_TAKE_BIG_FILES);
			}
		});
		v_device_folder.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(Utils.debug)Log.i(TAG,"打开遍历手机内存");
				Intent intent = new Intent(thisContext, FolderExplorerActivity.class);
				intent.putExtra(FolderExplorerActivity.FOLDER_ROOT_PATH, "/");
				intent.putExtra(FolderExplorerActivity.TITLE_NAME, "手机内存文件");
				intent.putExtra(FolderExplorerActivity.IS_SEND_FILES, true);
				startActivityForResult(intent, BaseMsgRoom.REQ_TAKE_BIG_FILES);
				
			}
		});
		
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==BaseMsgRoom.REQ_TAKE_BIG_FILES
				&& resultCode == Activity.RESULT_OK){
			if (data != null) {
				setResult(resultCode, data);
				finish();
			}
		}
	}

	
}
