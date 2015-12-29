package com.tuixin11sms.tx.activity;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.activity.explorer.FileManager;
import com.tuixin11sms.tx.activity.explorer.FolderExplorerActivity;
import com.tuixin11sms.tx.task.FileTransfer;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

/**
 * 查看本地接受到的文件列表
 * 
 * @author SHC
 * 
 */
public class ReceivedFilesActivity extends BaseActivity {
	private static final String TAG = "ReceiveedFilesActivity";

	public static final int FILE_TYPE_PICTURE = 1;
	public static final int FILE_TYPE_AUDIO = 2;
	public static final int FILE_TYPE_VIDEO = 3;
	public static final int FILE_TYPE_DOC = 4;
	public static final int FILE_TYPE_UNKOWN = 5;
	

//	private final String RECEIVE_FILE_FOLDER = "receiveedFiles";// 已接收的文件目录
	private String folderPath = null;
	private ListView lv_receiveFileList;
	private List<FileInfor> receiveFileTypeList = new ArrayList<ReceivedFilesActivity.FileInfor>();
	private static Map<Integer, List<String>> formatsMap = null;// 存储所有可以识别的格式
	private Map<Integer, Integer> newFileCountMap = null;// 新收到的文件总数
	private boolean isSendFiles;//是否是发送文件（或者是查看文件）

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		isSendFiles = intent.getBooleanExtra(FolderExplorerActivity.IS_SEND_FILES, false);
		
		setContentView(R.layout.activity_receive_files);
		lv_receiveFileList = (ListView) findViewById(R.id.lv_received_fils_list);
		btn_back_left = (LinearLayout) findViewById(R.id.btn_back_left);
		
		lv_receiveFileList.setAdapter(receiveFileAdapter);

		try {
			String formats = Utils.getAssetsString(mSess.getContext(), "formats.json");
			formatsMap = convertStringToMap(formats);
			newFileCountMap = Utils.getNewFileCount(mSess.getContext());
		} catch (Exception e) {
			if (Utils.debug) {
				Log.e(TAG, "读取资产文件的格式文件，或读取信收到的文件总数 异常", e);
			}
		}
		
		FileInfor fInfor = new FileInfor(FILE_TYPE_PICTURE, "照片", newFileCountMap.get(FILE_TYPE_PICTURE));
		receiveFileTypeList.add(fInfor);
		fInfor = new FileInfor(FILE_TYPE_AUDIO, "音乐", newFileCountMap.get(FILE_TYPE_AUDIO));
		receiveFileTypeList.add(fInfor);
		fInfor = new FileInfor(FILE_TYPE_VIDEO, "视频",newFileCountMap.get(FILE_TYPE_VIDEO));
		receiveFileTypeList.add(fInfor);
		fInfor = new FileInfor(FILE_TYPE_DOC, "文档", newFileCountMap.get(FILE_TYPE_DOC));
		receiveFileTypeList.add(fInfor);
		fInfor = new FileInfor(FILE_TYPE_UNKOWN, "其他", newFileCountMap.get(FILE_TYPE_UNKOWN));
		receiveFileTypeList.add(fInfor);

//		LoginSessionManager lsn = LoginSessionManager
//				.getManager(txdata);

		folderPath = mSess.mDownUpMgr.getStoragePath() + File.separator
				+ FileTransfer.RECEIVE_FILE_FOLDER;
		Utils.creatFolder(folderPath);// 创建目录

		btn_back_left.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		lv_receiveFileList
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// list上条目点击事件
						Intent intent = new Intent(thisContext,FolderExplorerActivity.class);
						intent.putExtra(FolderExplorerActivity.FILE_TYPE,receiveFileTypeList.get(position).fileType);
						intent.putExtra(FolderExplorerActivity.TITLE_NAME,receiveFileTypeList.get(position).fileTypeName);
						intent.putExtra(FolderExplorerActivity.FOLDER_ROOT_PATH,folderPath);
						intent.putExtra(FolderExplorerActivity.IS_SEND_FILES,isSendFiles);
						startActivityForResult(intent, BaseMsgRoom.REQ_TAKE_BIG_FILES);
					}
				});

		
		loadFileTask.execute();

	}
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		//重新读取新收到的文件，刷新adapter
		try {
			newFileCountMap = Utils.getNewFileCount(mSess.getContext());
			for(ReceivedFilesActivity.FileInfor fi : receiveFileTypeList){
				fi.newFileCount = newFileCountMap.get(fi.fileType);
			}
		} catch (Exception e) {
			if (Utils.debug) {
				Log.e(TAG, "重新读取新收到的文件总数 异常", e);
			}
		}
		receiveFileAdapter.notifyDataSetChanged();//刷新adapter
	}

	// 转换json字符串为map
	private Map<Integer, List<String>> convertStringToMap(String jsonString)
			throws JSONException {
		Map<Integer, List<String>> formatsMap = new HashMap<Integer, List<String>>();

		JSONObject formatsJson = new JSONObject(jsonString);
		List<String> formatsList = new ArrayList<String>();
		JSONArray jarray = formatsJson.getJSONArray("picture");
		for (int i = 0, size = jarray.length(); i < size; i++) {
			formatsList.add(jarray.getString(i));
		}
		formatsMap.put(FileManager.FILE_TYPE_PICTURE, formatsList);

		formatsList = new ArrayList<String>();
		jarray = formatsJson.getJSONArray("audio");
		for (int i = 0, size = jarray.length(); i < size; i++) {
			formatsList.add(jarray.getString(i));
		}
		formatsMap.put(FileManager.FILE_TYPE_AUDIO, formatsList);

		formatsList = new ArrayList<String>();
		jarray = formatsJson.getJSONArray("video");
		for (int i = 0, size = jarray.length(); i < size; i++) {
			formatsList.add(jarray.getString(i));
		}
		formatsMap.put(FileManager.FILE_TYPE_VIDEO, formatsList);

		formatsList = new ArrayList<String>();
		jarray = formatsJson.getJSONArray("doc");
		for (int i = 0, size = jarray.length(); i < size; i++) {
			formatsList.add(jarray.getString(i));
		}
		formatsMap.put(FileManager.FILE_TYPE_DOC, formatsList);

		return formatsMap;

	}

	private AsyncTask<Void, Void, Void> loadFileTask = new AsyncTask<Void, Void, Void>() {

		@Override
		protected Void doInBackground(Void... params) {
			// 加载文件夹下面的指定类型的文件
			// 所有未知文件
			Set<Integer> keySet = formatsMap.keySet();
			Iterator<Integer> iter = keySet.iterator();
			final List<Integer> keyList = new ArrayList<Integer>();
			while (iter.hasNext()) {
				keyList.add(iter.next());
			}

			// 把所有支持的格式都放到待判定list中
			File folderFile = new File(folderPath);
			folderFile.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String filename) {
					boolean isUnkown = true;
					for (Integer key : keyList) {
						if (formatsMap.get(key)
								.contains(
										filename.substring(filename
												.lastIndexOf(".") + 1))) {
							isUnkown = false;//非未知类型文件
							// 该文件格式在指定的格式集合中
							for (FileInfor infor : receiveFileTypeList) {
								if (infor.fileType == key) {
									infor.fileCount++;
									break;
								}
							}
						} 

					}
					if(isUnkown) {
						//文件类型不识别
						for (FileInfor infor : receiveFileTypeList) {
							if (infor.fileType == FILE_TYPE_UNKOWN) {
								infor.fileCount++;
								break;
							}
						}
					}
					return false;
				}
			});
			return null;
		}
		
		protected void onPostExecute(Void result) {
			//如果在doInBackground中调用更新adapter方法可能会报下面的异常
			//android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
			receiveFileAdapter.notifyDataSetChanged();
			
		};

	};

	private BaseAdapter receiveFileAdapter = new BaseAdapter() {

		@Override
		public int getCount() {
			return receiveFileTypeList.size();
		}

		@Override
		public Object getItem(int position) {
			return receiveFileTypeList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FileInfor finfo = receiveFileTypeList.get(position);
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(getApplicationContext(),
						R.layout.sll_item_receive_files, null);
				View view = convertView.findViewById(R.id.rl_item_infors);
				holder.iv_file_icon = (ImageView) view
						.findViewById(R.id.iv_file_thumb);
				holder.tv_file_name = (TextView) view
						.findViewById(R.id.tv_item_name);
				holder.tv_file_count = (TextView) view
						.findViewById(R.id.tv_item_file_count);
				holder.tv_new_file_count = (TextView) view
						.findViewById(R.id.tv_item_newFileCount);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

//			View line = convertView.findViewById(R.id.v_divide_line);
//			if ((position+1)<receiveFileTypeList.size()) {
//				line.setVisibility(View.VISIBLE);
//			}else {
//				line.setVisibility(View.INVISIBLE);
//			}
			
			if (finfo.fileType == FILE_TYPE_PICTURE) {
				holder.iv_file_icon.setImageResource(R.drawable.in_03);
			} else if (finfo.fileType == FILE_TYPE_AUDIO) {
				holder.iv_file_icon.setImageResource(R.drawable.in_06);
			} else if (finfo.fileType == FILE_TYPE_VIDEO) {
				holder.iv_file_icon.setImageResource(R.drawable.in_08);
			} else if (finfo.fileType == FILE_TYPE_DOC) {
				holder.iv_file_icon.setImageResource(R.drawable.in_10);
			} else if (finfo.fileType == FILE_TYPE_UNKOWN) {
				holder.iv_file_icon.setImageResource(R.drawable.in_12);
			}
			holder.tv_file_name.setText(finfo.fileTypeName);
			if (finfo.fileCount == 0) {
				holder.tv_file_count.setText(null);
			} else {
				holder.tv_file_count.setText("(" + finfo.fileCount+")");
			}

			if (isSendFiles) {
				holder.tv_new_file_count.setVisibility(View.INVISIBLE);
			}else {
				holder.tv_new_file_count.setVisibility(View.VISIBLE);
				if (finfo.newFileCount == 0) {
					holder.tv_new_file_count.setText(null);
					holder.tv_new_file_count.setVisibility(View.INVISIBLE);
				} else {
					holder.tv_new_file_count.setText("" + finfo.newFileCount);
				}
			}

			return convertView;
		}

	};

	private LinearLayout btn_back_left;

	private class ViewHolder {
		public ImageView iv_file_icon;
		public TextView tv_file_name;
		public TextView tv_file_count;
		public TextView tv_new_file_count;
	}

	/**
	 * 保存每个文件类型条目的上需要显示的信息的对象
	 * 
	 * @author SHC
	 * 
	 */
	private class FileInfor {

		public FileInfor(int fileType, String fileTypeName,
				int newFileCount) {
			this.fileType = fileType;
			this.fileTypeName = fileTypeName;
			this.newFileCount = newFileCount;
		}

		public int fileType;
		public String fileTypeName;
		public int fileCount;
		public int newFileCount;
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
