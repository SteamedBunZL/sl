package com.tuixin11sms.tx.activity.explorer;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Future;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.activity.BaseMsgRoom;
import com.tuixin11sms.tx.activity.GalleryFileActivity;
import com.tuixin11sms.tx.utils.Utils;


/**
 * 此类功能已经被整合到FolderExplorer中，待删除  2013.12.25  shc
 */
public class EventHandler implements OnClickListener {
	private static final String TAG = "EventHandler";
	public static final String IS_SEND_FILES = "is_send_files";
	public static final int ERROR_CANNOT_READ_DERECTORY = 1;// 无法打开文件夹
	public static final int ERROR_REACH_MAX_SELECTED_NUM = 2;// 达到文件选择上限

	public static final int MAX_MAX_SELECTED_NUM = 3;// 可以同时勾选的文件上限个数
	private final Activity mActivity;
	private final FileManager mFileMang;
	private final boolean mIsSendFiles;// 值为true代表是发送文件，否则是查看浏览文件
	private ThumbnailCreator mThumbnail;
	private TableRowAdapter mDelegateAdapter;
	private ListView mlv;
	private int mLvScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;// 默认是暂停状态

	private Future<?> future = null;// 加载缩略图的线程执行future

	// private int mColor = Color.WHITE;

	// the list used to feed info into the array adapter and when multi-select
	// is on
	private ArrayList<String> mDataSource;// 存的可能是当前列表的文件或文件夹名
	private ArrayList<String> mMultiSelectData;// 被选中的文件路径
	private TextView mPathLabel;// 路径显示框
	private TextView mSendFilesBtn;
	private TextView mTitleBar;
	private final String mTitleText;// 标题栏原来的标题（因为随着选择文件标题栏会变）
	private View mDerectoryPath;
	private ImageView mBackDerectory;
	private ProgressDialog pd;

	/**
	 * Creates an EventHandler object. This object is used to communicate most
	 * work from the Main activity to the FileManager class.
	 * 
	 * @param context
	 *            The context of the main activity e.g Main
	 * @param isSendFiles
	 *            是发送文件还是查看文件
	 * @param manager
	 *            The FileManager object that was instantiated from Main
	 */
	public EventHandler(Activity activity, boolean isSendFiles,
			final FileManager manager, String titleText) {
		mActivity = activity;
		mFileMang = manager;
		mIsSendFiles = isSendFiles;
		mDataSource = new ArrayList<String>(mFileMang.setHomeDir());
		mTitleText = titleText;
		
		pd = new ProgressDialog(mActivity);
	}

	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			String item = (String) msg.obj;
			
			/*** 打开文件夹 **/
			mDelegateAdapter.killMultiSelect();
			if (Utils.debug)
				Log.i(TAG, "进入文件夹，目录【" + mFileMang.getCurrentDir()
						+ "】的多选文件被取消掉");
			updateDirectory(mFileMang.getNextDir(item, false));
			mPathLabel.setText(mFileMang.getCurrentDir());
			
			pd.dismiss();
		};
	};

	/**
	 * This method is called from the Main activity and this has the same
	 * reference to the same object so when changes are made here or there they
	 * will display in the same way.
	 * 
	 * @param adapter
	 *            The TableRow object
	 */
	public void setListAdapter(TableRowAdapter adapter) {
		mDelegateAdapter = adapter;
	}

	public void setListView(ListView lv) {
		mlv = lv;
		mlv.setOnScrollListener(new AbsListView.OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// 设置listView的滑动状态
				mLvScrollState = scrollState;
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// 在滑动状态下

			}
		});
	}

	// /**
	// * This method is called from the Main activity and is passed the TextView
	// * that should be updated as the directory changes so the user knows which
	// * folder they are in.
	// *
	// * @param path
	// * The label to update as the directory changes
	// * @param label
	// * the label to update information
	// */
	// public void setUpdateLabels(TextView path) {
	// mPathLabel = path;
	// }

	// 设置发送文件的按钮
	public void setSendFilesButton(TextView sendFiles) {
		mSendFilesBtn = sendFiles;
	}

	// 设置标题显示文字
	public void setTitleBarText(TextView titleBar) {
		mTitleBar = titleBar;
	}

	// 设置目录栏和后退按钮
	public void setDerectoryPathView(View derectoryPath) {
		mDerectoryPath = derectoryPath;
		mBackDerectory = (ImageView) derectoryPath
				.findViewById(R.id.iv_back_derectory_button);
		mBackDerectory.setOnClickListener(this);
		mPathLabel = (TextView) derectoryPath
				.findViewById(R.id.tv_derectory_path_label);
		mPathLabel.setText(mFileMang.getCurrentDir());
	}

	// /**
	// * Indicates whether the user wants to select
	// * multiple files or folders at a time.
	// * <br><br>
	// * false by default
	// *
	// * @return true if the user has turned on multi selection
	// */
	// public boolean isMultiSelected() {
	// return multi_select_flag;
	// }

	/**
	 * Use this method to determine if the user has selected multiple
	 * files/folders
	 * 
	 * @return returns true if the user is holding multiple objects
	 *         (multi-select)
	 */
	public boolean hasMultiSelectData() {
		return (mMultiSelectData != null && mMultiSelectData.size() > 0);
	}

	/**
	 * This method, handles the button presses of the top buttons found in the
	 * Main activity.
	 */
	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.iv_back_derectory_button:
			onBackKeyPressed();
			break;
		case R.id.tv_send_selected_files:
			// Toast.makeText(mActivity,
			// "被选中的文件的路径集合：" + mMultiSelectData.toString(),
			// Toast.LENGTH_LONG).show();
			if (mMultiSelectData != null && mMultiSelectData.size() != 0) {
				Intent data = new Intent();
				data.putStringArrayListExtra(BaseMsgRoom.MSG_FILE_LIST,
						mMultiSelectData);
				mActivity.setResult(Activity.RESULT_OK, data);
				mActivity.finish();
			}
			break;
		}
	}

	/** 点击了返回键或者上一级按钮 */
	public void onBackKeyPressed() {
		if (!mFileMang.getCurrentDir().equals(mFileMang.getRootDir())) {
			mDelegateAdapter.killMultiSelect();
			if (Utils.debug)
				Log.i(TAG, "点击返回，目录【" + mFileMang.getCurrentDir()
						+ "】的多选文件被取消掉");
			updateDirectory(mFileMang.getPreviousDir());
			if (mPathLabel != null)
				mPathLabel.setText(mFileMang.getCurrentDir());
		}
	}

	/** 目录文件夹或者文件的的一个条目的点击事件 */
	public int onListItemClick(int position) {
		final String item = getData(position);
		File file = new File(mFileMang.getCurrentDir() + "/" + item);
		if (file.isDirectory()) {
			if (file.canRead()) {
				/*** 创建dialog **/
				
				pd.setMessage("正在打开文件夹，请稍后……");
				pd.show();

				Message msg = Message.obtain();
				msg.obj = item;
				handler.sendMessage(msg);
				
//				/*** 打开文件夹 **/
//				mDelegateAdapter.killMultiSelect();
//				if (Utils.debug)
//					Log.i(TAG, "进入文件夹，目录【" + mFileMang.getCurrentDir()
//							+ "】的多选文件被取消掉");
//				updateDirectory(mFileMang.getNextDir(item, false));
//				mPathLabel.setText(mFileMang.getCurrentDir());
//
//				/*** 隐藏dialog **/
//				pd.dismiss();

			} else {
				return ERROR_CANNOT_READ_DERECTORY;
			}
		} else {
			if (mIsSendFiles) {
				return mDelegateAdapter.addMultiPosition(position,
						file.getPath());
			} else {
				// 查看文件
				String sub_ext = item.substring(item.lastIndexOf(".") + 1);
				int fileType = FileManager.getFileType(mActivity, sub_ext);

				try {
					if (fileType == FileManager.FILE_TYPE_PICTURE) {
						// 用图库浏览图片
						ArrayList<String> imagePathList = new ArrayList<String>();// 存储图片路径集合
						for (String fileName : mDataSource) {
							imagePathList.add(mFileMang.getCurrentDir() + "/"
									+ fileName);
						}

						Intent intent = new Intent(mActivity,
								GalleryFileActivity.class);
						intent.putExtra(GalleryFileActivity.IMAGE_PATH_LIST,
								imagePathList);
						intent.putExtra(GalleryFileActivity.CURRENT_ITEM,
								position);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						mActivity.startActivity(intent);

					} else if (fileType == FileManager.FILE_TYPE_AUDIO) {
						Utils.playAudio(mActivity, file.getAbsolutePath());
					} else if (fileType == FileManager.FILE_TYPE_VIDEO) {
						Utils.playVideo(mActivity, file.getAbsolutePath());
					} else if (fileType == FileManager.FILE_TYPE_DOC) {
						Utils.openDocFile(mActivity, file.getAbsolutePath());
					} else {
						Utils.openUnkownFile(mActivity, file.getAbsolutePath());
					}
				} catch (Exception e) {
					if (Utils.debug)
						Log.e(TAG, "打开文件异常", e);
					Toast.makeText(mActivity, "无法打开此文件", Toast.LENGTH_SHORT)
							.show();
				}
			}
		}
		return 0;
	}

	/** 目录文件夹或者文件的的一个条目的删除事件 */
	public int onListItemLongClick(final int position) {
		final String item = getData(position);
		final File file = new File(mFileMang.getCurrentDir() + "/" + item);
		if (!file.isDirectory()) {
			String[] items = new String[] { "删除文件" };
			Utils.creatDialogMenu(mActivity, items, "操作",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Utils.creatDialog(mActivity, "确定删除此文件？",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// 删除文件，刷新adapter
											file.delete();
											mDataSource.remove(position);
											mDelegateAdapter
													.notifyDataSetChanged();

										}
									}, new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();

										}
									});

						}
					});
		}
		return 0;
	}

	/**
	 * will return the data in the ArrayList that holds the dir contents.
	 * 
	 * @param position
	 *            the indext of the arraylist holding the dir content
	 * @return the data in the arraylist at position (position)
	 */
	public String getData(int position) {

		if (position > mDataSource.size() - 1 || position < 0)
			return null;

		return mDataSource.get(position);
	}

	/**
	 * called to update the file contents as the user navigates there phones
	 * file system.
	 * 
	 * @param content
	 *            an ArrayList of the file/folders in the current directory.
	 */
	public void updateDirectory(ArrayList<String> content) {
		if (!mDataSource.isEmpty())
			mDataSource.clear();

		for (String data : content)
			mDataSource.add(data);

		mDelegateAdapter.notifyDataSetChanged();
	}

	/**
	 * This private method is used to display options the user can select when
	 * the tool box button is pressed. The WIFI option is commented out as it
	 * doesn't seem to fit with the overall idea of the application. However to
	 * display it, just uncomment the below code and the code in the
	 * AndroidManifest.xml file.
	 */

	private static class ViewHolder {
		TextView topView;
		TextView bottomView;
		ImageView icon;
		ImageView mSelect; // multi-select check mark icon
	}

	/**
	 * A nested class to handle displaying a custom view in the ListView that is
	 * used in the Main activity. If any icons are to be added, they must be
	 * implemented in the getView method. This class is instantiated once in
	 * Main and has no reason to be instantiated again.
	 * 
	 * @author Joe Berria
	 */
	public class TableRowAdapter extends ArrayAdapter<String> {
		// private final int KB = 1024;
		// private final int MG = KB * KB;
		// private final int GB = MG * KB;
		// private String display_size;

		// private ArrayList<Integer> positions;

		public TableRowAdapter() {
			super(mActivity, R.layout.sll_item_sdcard, mDataSource);
		}

		// 添加到list中
		public int addMultiPosition(int index, String path) {
			int resultCode = 0;
			if (mMultiSelectData == null) {
				resultCode = add_multiSelect_file(path);

			} else if (mMultiSelectData.contains(path)) {
				mMultiSelectData.remove(path);
			} else {
				resultCode = add_multiSelect_file(path);
			}

			if (mMultiSelectData.size() > 0) {
				mSendFilesBtn.setEnabled(true);
				mTitleBar.setText("已选择 （" + mMultiSelectData.size() + "）");
			} else {
				mSendFilesBtn.setEnabled(false);
				mTitleBar.setText(mTitleText);
			}
			notifyDataSetChanged();
			return resultCode;
		}

		/**
		 * This will turn off multi-select and hide the multi-select buttons at
		 * the bottom of the view.
		 * 
		 * @param clearData
		 *            if this is true any files/folders the user selected for
		 *            multi-select will be cleared. If false, the data will be
		 *            kept for later use. Note: multi-select copy and move will
		 *            usually be the only one to pass false, so we can later
		 *            paste it to another folder.
		 */
		public void killMultiSelect() {
			if (mMultiSelectData != null && !mMultiSelectData.isEmpty())
				mMultiSelectData.clear();
			mSendFilesBtn.setEnabled(false);// 存选中文件的集合空了，发送不可用
			mTitleBar.setText(mTitleText);
			notifyDataSetChanged();
		}

		final Handler handle = new Handler(new Handler.Callback() {
			public boolean handleMessage(Message msg) {
				notifyDataSetChanged();
				return true;
			}
		});

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder mViewHolder;
			int num_items = 0;
			String temp = mFileMang.getCurrentDir();
			String currentFileName = null;
			if (temp.equals("/")) {
				currentFileName = temp + mDataSource.get(position);
			} else {
				currentFileName = temp + "/" + mDataSource.get(position);
			}
			File file = new File(currentFileName);
			String[] list = file.list();

			if (list != null)
				num_items = list.length;

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) mActivity
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.sll_item_sdcard, parent,
						false);

				mViewHolder = new ViewHolder();
				mViewHolder.topView = (TextView) convertView
						.findViewById(R.id.top_view);
				mViewHolder.bottomView = (TextView) convertView
						.findViewById(R.id.bottom_view);
				mViewHolder.icon = (ImageView) convertView
						.findViewById(R.id.row_image);
				mViewHolder.mSelect = (ImageView) convertView
						.findViewById(R.id.multiselect_icon);

				convertView.setTag(mViewHolder);

			} else {
				mViewHolder = (ViewHolder) convertView.getTag();
			}

			// if (positions != null && positions.contains(position))
			if (mIsSendFiles) {
				if (mMultiSelectData != null
						&& mMultiSelectData.contains(currentFileName))
					mViewHolder.mSelect
							.setImageResource(R.drawable.cb_select_folder_file);
				else
					mViewHolder.mSelect
							.setImageResource(R.drawable.cb_unselect_folder_file);
			} else {
				mDerectoryPath.setVisibility(View.GONE);
				mViewHolder.mSelect.setVisibility(View.GONE);
			}

			if (mThumbnail == null)
				mThumbnail = new ThumbnailCreator();

			if (file != null && file.isFile()) {
				String ext = file.toString();
				String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);

				/*
				 * 下面的else if 语句判断主要是为了指定对应的缩略图显示
				 */
				int fileType = FileManager.getFileType(mActivity, sub_ext);
				if (fileType == FileManager.FILE_TYPE_PICTURE) {
					if (mLvScrollState != AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
						// 不是在fling状态下，则去加载缩略图
						Bitmap thumb = mThumbnail
								.isBitmapCached(file.getPath());

						if (thumb == null && file.length() != 0) {
							mViewHolder.icon.setTag(currentFileName);// 把文件路径设置为头像view的tag

							new AsyncTask<String, Void, Bitmap>() {
								private String imagePath = null;

								@Override
								protected Bitmap doInBackground(
										String... params) {
									// 加载文件缩略图
									imagePath = params[0];
									if (!TextUtils.isEmpty(params[0])) {
										File imgFile = new File(params[0]);

										if (imgFile != null && imgFile.exists()) {
											Bitmap bm = Utils
													.getImageThumbnail(
															imgFile.getPath(),
															52, 52);
											mThumbnail.cacheThumbBitmap(
													imgFile.getPath(), bm);
											return bm;
										}
									}
									return null;
								}

								@Override
								protected void onPostExecute(Bitmap result) {
									// 刷新holder
									if (result != null) {
										if (((String) mViewHolder.icon.getTag())
												.equals(imagePath)) {
											mViewHolder.icon
													.setImageBitmap(result);
										}
									} else {
										mViewHolder.icon
												.setImageResource(R.drawable.thumb_receive_img);
									}
								};

							}.execute(currentFileName);

						} else {
							mViewHolder.icon.setImageBitmap(thumb);
						}

					} else {
						mViewHolder.icon
								.setImageResource(R.drawable.thumb_receive_img);
					}

				} else if (fileType == FileManager.FILE_TYPE_AUDIO) {
					mViewHolder.icon
							.setImageResource(R.drawable.thumb_receive_music);
				} else if (fileType == FileManager.FILE_TYPE_VIDEO) {
					mViewHolder.icon
							.setImageResource(R.drawable.thumb_receive_video);
				} else if (fileType == FileManager.FILE_TYPE_DOC) {
					mViewHolder.icon
							.setImageResource(R.drawable.thumb_receive_doc);
				} else {
					mViewHolder.icon
							.setImageResource(R.drawable.thumb_receive_unkown);
				}

			} else if (file != null && file.isDirectory()) {
				mViewHolder.icon
						.setImageResource(R.drawable.thumb_receive_folder);
			}

			if (file.isFile()) {
				// double size = file.length();
				// if (size > GB)
				// display_size = String
				// .format("%.2f Gb ", (double) size / GB);
				// else if (size < GB && size > MG)
				// display_size = String
				// .format("%.2f Mb ", (double) size / MG);
				// else if (size < MG && size > KB)
				// display_size = String
				// .format("%.2f Kb ", (double) size / KB);
				// else
				// display_size = String.format("%.2f bytes ", (double) size);

				// if (file.isHidden())
				// mViewHolder.bottomView
				// .setText("(hidden) | " + display_size);
				// else
				mViewHolder.bottomView.setText(Utils.formatFileLength(file
						.length()));

			} else {
				// if (file.isHidden())
				// mViewHolder.bottomView.setText("(hidden) | " + num_items
				// + " items ");
				// else
				mViewHolder.bottomView.setText(num_items + " items ");
			}

			mViewHolder.topView.setText(file.getName());

			return convertView;
		}

		private int add_multiSelect_file(String src) {
			if (mMultiSelectData == null)
				mMultiSelectData = new ArrayList<String>();
			if (mMultiSelectData.size() == MAX_MAX_SELECTED_NUM) {
				return ERROR_REACH_MAX_SELECTED_NUM;
			} else {
				// 选择的文件总数小于3，则添加
				mMultiSelectData.add(src);
			}
			return 0;
		}

	}

}
