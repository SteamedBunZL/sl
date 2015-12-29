/*
    Open Manager For Tablets, an open source file manager for the Android system
    Copyright (C) 2011  Joe Berria <nexesdevelopment@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tuixin11sms.tx.activity.explorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.tuixin11sms.tx.gif.MD5FileUtil;
import com.tuixin11sms.tx.gif.gifOpenHelper;
import com.tuixin11sms.tx.net.TuiboSlienceManager;
import com.tuixin11sms.tx.utils.Utils;

public class FileManager {
	private static final String TAG = "FileManager";
	public static final int FILE_TYPE_PICTURE = 1;
	public static final int FILE_TYPE_AUDIO = 2;
	public static final int FILE_TYPE_VIDEO = 3;
	public static final int FILE_TYPE_DOC = 4;
	public static final int FILE_TYPE_UNKOWN = 5;

	private Stack<String> mPathStack;
	private ArrayList<String> mDirContent;
	private String mRootPath;

	private int mFileType = 0;// 要显示的文件类型,如果未0代表显示所有文件。
	private static Map<Integer, List<String>> formatsMap;

	// 显示指定文件类型的构造函数，fileType=0代表显示所有文件。
	public FileManager(Context context, int fileType, String rootPath) {
		mDirContent = new ArrayList<String>();
		mPathStack = new Stack<String>();
		mRootPath = rootPath;
		mPathStack.push(mRootPath);
		mFileType = fileType;
		try {
			if (formatsMap == null) {
				String formats = Utils.getAssetsString(context, "formats.json");
				formatsMap = convertStringToMap(formats);
			}
		} catch (Exception e) {
			if (Utils.debug) {
				Log.e(TAG, "读取资产文件的格式文件异常", e);
			}
		}

	}

	/** 转换json字符串为map */
	private static Map<Integer, List<String>> convertStringToMap(
			String jsonString) throws JSONException {
		Map<Integer, List<String>> formatsMap = new HashMap<Integer, List<String>>();

		JSONObject formatsJson = new JSONObject(jsonString);
		List<String> formatsList = new ArrayList<String>();
		JSONArray jarray = formatsJson.getJSONArray("picture");
		for (int i = 0, size = jarray.length(); i < size; i++) {
			formatsList.add(jarray.getString(i));
		}
		formatsMap.put(FILE_TYPE_PICTURE, formatsList);

		formatsList = new ArrayList<String>();
		jarray = formatsJson.getJSONArray("audio");
		for (int i = 0, size = jarray.length(); i < size; i++) {
			formatsList.add(jarray.getString(i));
		}
		formatsMap.put(FILE_TYPE_AUDIO, formatsList);

		formatsList = new ArrayList<String>();
		jarray = formatsJson.getJSONArray("video");
		for (int i = 0, size = jarray.length(); i < size; i++) {
			formatsList.add(jarray.getString(i));
		}
		formatsMap.put(FILE_TYPE_VIDEO, formatsList);

		formatsList = new ArrayList<String>();
		jarray = formatsJson.getJSONArray("doc");
		for (int i = 0, size = jarray.length(); i < size; i++) {
			formatsList.add(jarray.getString(i));
		}
		formatsMap.put(FILE_TYPE_DOC, formatsList);

		return formatsMap;

	}

	/** 根据后缀名，返回对应的文件类型 */
	public static int getFileType(Context context, String sub_ext) {
		if (TextUtils.isEmpty(sub_ext)) {
			// 后缀名为空，则直接为位置文件
			return FILE_TYPE_UNKOWN;
		}
		if (formatsMap == null) {
			try {
				String formats = Utils.getAssetsString(context, "formats.json");
				formatsMap = convertStringToMap(formats);
			} catch (Exception e) {
				if (Utils.debug) {
					Log.e(TAG, "读取资产文件的格式文件异常", e);
				}
			}
		}
		Set<Entry<Integer, List<String>>> set = formatsMap.entrySet();
		Iterator<Entry<Integer, List<String>>> iterator = set.iterator();
		Entry<Integer, List<String>> entry = null;
		while (iterator.hasNext()) {
			entry = iterator.next();
			if (entry.getValue().contains(sub_ext)) {
				return entry.getKey();
			}
		}
		return FILE_TYPE_UNKOWN;

	}

	/**
	 * check the file is gif packge or not
	 * 
	 * @param context
	 * @param sub_ext
	 * @return
	 */
	public static boolean isFileGifPackage(Context context, String sub_ext) {
		if (!TextUtils.isEmpty(sub_ext)
				&& (sub_ext.equals("eif") || sub_ext.equals("eip"))) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 得到文件的后缀
	 * 
	 * @param path
	 * @return
	 */
	public static String getFileSuffix(String path) {
		if (!TextUtils.isEmpty(path) && path.contains(".")) {
			return path.substring(path.lastIndexOf(".") + 1);
		}
		return null;
	}

	/**
	 * This will return a string of the current directory path
	 * 
	 * @return the current directory
	 */
	public String getCurrentDir() {
		return mPathStack.peek();
	}

	public String getRootDir() {
		return mRootPath;
	}

	/**
	 * This will return a string of the current home path.
	 * 
	 * @return the home directory
	 */
	public ArrayList<String> setHomeDir() {
		// This will eventually be placed as a settings item
		mPathStack.clear();
		mPathStack.push(mRootPath);

		return populate_list();
	}

	/**
	 * This will return a string that represents the path of the previous path
	 * 
	 * @return returns the previous path
	 */
	public ArrayList<String> getPreviousDir() {
		int size = mPathStack.size();

		if (size >= 2)
			mPathStack.pop();

		else if (size == 0)
			mPathStack.push(mRootPath);

		return populate_list();
	}

	/**
	 * 
	 * @param path
	 * @param isFullPath
	 * @return
	 */
	public ArrayList<String> getNextDir(String path, boolean isFullPath) {
		int size = mPathStack.size();

		// path与pathStack最上层的目录不一样
		if (!path.equals(mPathStack.peek())) {
			if (isFullPath) {
				mPathStack.push(path);
			} else {
				if (size == 1)
					if (mRootPath.equals("/")) {
						// 说明目前在遍历手机内存，根目录是"/"
						mPathStack.push(mRootPath + path);
					} else {
						// 说明目前在遍历手机SD卡
						mPathStack.push(mRootPath + "/" + path);
					}
				else
					mPathStack.push(mPathStack.peek() + "/" + path);
			}
		}

		return populate_list();
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public boolean isDirectory(String name) {
		return new File(mPathStack.peek() + "/" + name).isDirectory();
	}

	private final Comparator alph = new Comparator<String>() {
		@Override
		public int compare(String arg0, String arg1) {
			File file0 = new File(mPathStack.peek() + "/" + arg0);
			File file1 = new File(mPathStack.peek() + "/" + arg1);
			if (file0.isDirectory() && file1.isFile()) {
				return -1;
			} else if (file0.isFile() && file1.isDirectory()) {
				return 1;
			}
			return arg0.toLowerCase().compareTo(arg1.toLowerCase());
		}
	};

	// 文件大小排序
	private final Comparator size = new Comparator<String>() {
		@Override
		public int compare(String arg0, String arg1) {
			String dir = mPathStack.peek();
			Long first = new File(dir + "/" + arg0).length();
			Long second = new File(dir + "/" + arg1).length();

			return first.compareTo(second);
		}
	};

	// 文件类型排序
	private final Comparator type = new Comparator<String>() {
		@Override
		public int compare(String arg0, String arg1) {
			String ext = null;
			String ext2 = null;
			int ret;

			try {
				ext = arg0.substring(arg0.lastIndexOf(".") + 1, arg0.length())
						.toLowerCase();
				ext2 = arg1.substring(arg1.lastIndexOf(".") + 1, arg1.length())
						.toLowerCase();

			} catch (IndexOutOfBoundsException e) {
				return 0;
			}
			ret = ext.compareTo(ext2);

			if (ret == 0)
				return arg0.toLowerCase().compareTo(arg1.toLowerCase());

			return ret;
		}
	};

	/*
	 * 填充该目录下的所有文件或文件夹 (non-Javadoc) this function will take the string from the
	 * top of the directory stack and list all files/folders that are in it and
	 * return that list so it can be displayed. Since this function is called
	 * every time we need to update the the list of files to be shown to the
	 * user, this is where we do our sorting (by type, alphabetical, etc).
	 * 
	 * @return
	 */
	private ArrayList<String> populate_list() {

		if (!mDirContent.isEmpty())
			mDirContent.clear();

		File folderFile = new File(mPathStack.peek());// 要打开展示的目录

		if (folderFile.exists() && folderFile.canRead()) {
			String[] fileNameList = null;
			if (mFileType != 0) {
				// 加载文件夹下面的指定类型的文件
				final List<String> fileFormats;
				if (mFileType != FILE_TYPE_UNKOWN) {
					// 指定文件类型
					fileFormats = formatsMap.get(mFileType);// 目标格式
					fileNameList = folderFile.list(new FilenameFilter() {

						@Override
						public boolean accept(File dir, String filename) {
							if (fileFormats.contains(filename
									.substring(filename.lastIndexOf(".") + 1))) {
								// 该文件格式在指定的格式集合中
								return true;
							} else {
								return false;
							}
						}
					});
				} else {
					// 所有未知文件
					fileFormats = new ArrayList<String>();
					Collection<List<String>> coll = formatsMap.values();
					// 把所有支持的格式都放到待判定list中
					for (List<String> list : coll) {
						fileFormats.addAll(list);
					}
					fileNameList = folderFile.list(new FilenameFilter() {

						@Override
						public boolean accept(File dir, String filename) {
							if (!fileFormats.contains(filename
									.substring(filename.lastIndexOf(".") + 1))) {
								// 该文件格式不在指定的格式集合中
								return true;
							} else {
								return false;
							}
						}
					});
				}
			} else {
				// 没有文件类型限制，加载该目录下所有文件
				fileNameList = folderFile.list();
			}

			int len = fileNameList.length;

			/* add files/folder to arraylist depending on hidden status */
			for (int i = 0; i < len; i++) {
				// if (!mShowHiddenFiles) {
				if (fileNameList[i].toString().charAt(0) != '.')
					mDirContent.add(fileNameList[i]);

				// } else {
				// mDirContent.add(list[i]);
				// }
			}

			Object[] tt = mDirContent.toArray();
			mDirContent.clear();

			Arrays.sort(tt, alph);

			for (Object a : tt) {
				mDirContent.add((String) a);
			}

		} else {
			mDirContent.add("Emtpy");
		}

		return mDirContent;
	}

	/**
	 * 复制单个文件,这里用来复制表情包到/shenliao/gif/
	 * 
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf.txt
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	public static void copyFile(String oldPath) {
		String sub = getSub(oldPath);
		sub = createSaveGifPackageName(-1, sub, getFileMD5(oldPath));
		String newPath = getShenLiaoGifPackagePath() + sub;
		File newFile = new File(newPath);
		if (!newFile.exists()) {
			try {
				int bytesum = 0;
				int byteread = 0;
				File oldfile = new File(oldPath);
				if (oldfile.exists()) { // 文件不存在时
					InputStream inStream = new FileInputStream(oldPath); // 读入原文件
					FileOutputStream fs = new FileOutputStream(newPath);
					byte[] buffer = new byte[1444];
					int length;
					while ((byteread = inStream.read(buffer)) != -1) {
						bytesum += byteread; // 字节数 文件大小
						fs.write(buffer, 0, byteread);
					}
					fs.flush();
					fs.close();
					inStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();

			}
		}

	}

	/**
	 * 生成本地储存的包的名称
	 * 
	 * @param package_id
	 *            包的Id,本地导入的包的id为-1
	 * @param package_name
	 *            包的名称,带后缀
	 * @param package_md5
	 *            包的md5值
	 * @return
	 */
	public static String createSaveGifPackageName(int package_id,
			String package_name, String package_md5) {
		return package_id + "_" + package_md5 + "_" + package_name;
	}

	/**
	 * 获取文件的md5值
	 * 
	 * @param path
	 * @return
	 */
	public static String getFileMD5(String path) {
		return MD5FileUtil.getMD5Path(path);
	}

	public static String getFileMD5(File file) throws FileNotFoundException {
		return MD5FileUtil.getMd5ByFile(file);
	}

	// public static String getGifPackageID

	/**
	 * 得到神聊保存gif_package的路径 /shenliao/gif_package/
	 * 
	 * @return
	 */
	public static String getShenLiaoGifPackagePath() {
		File file = Environment.getExternalStorageDirectory();
		String path = file.getAbsolutePath() + "/shenliao";
		File giffile = new File(path, "gif_package");
		if (!giffile.exists())
			giffile.mkdirs();
		return giffile.getAbsolutePath() + "/";
	}

	public static String getShenLiaoGifImgPath() {
		File file = Environment.getExternalStorageDirectory();
		String path = file.getAbsolutePath() + "/shenliao";
		File giffile = new File(path, "gif_img");
		if (!giffile.exists())
			giffile.mkdirs();
		return giffile.getAbsolutePath() + "/";
	}

	public static String getShenLiaoGifPath() {
		File file = Environment.getExternalStorageDirectory();
		String path = file.getAbsolutePath() + "/shenliao";
		File giffile = new File(path, "gif");
		if (!giffile.exists())
			giffile.mkdirs();
		return giffile.getAbsolutePath() + "/";
	}

	public static String getShenLiaoGifOppositePath() {
		File file = Environment.getExternalStorageDirectory();
		String path = file.getAbsolutePath() + "/shenliao";
		File giffile = new File(path, "gif_opposite");
		if (!giffile.exists())
			giffile.mkdirs();
		return giffile.getAbsolutePath() + "/";
	}

	public static AnimationDrawable getGifFromLocal(String path) {
		AnimationDrawable mSmile = new AnimationDrawable();
		InputStream is = null;
		try {
			is = new FileInputStream(path);
			gifOpenHelper gHelper = new gifOpenHelper();
			gHelper.read(is);
			BitmapDrawable bd = new BitmapDrawable(gHelper.getImage());
			mSmile.addFrame(bd, gHelper.getDelay(0));
			for (int j = 1; j < gHelper.getFrameCount(); j++) {
				mSmile.addFrame(new BitmapDrawable(gHelper.nextBitmap()),
						gHelper.getDelay(j));
			}
			mSmile.setBounds(0, 0, bd.getIntrinsicWidth(),
					bd.getIntrinsicHeight());
			mSmile.setOneShot(false);

			bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return mSmile;
	}

	/**
	 * 得到路径的最后一级/shenliao/xxxxx 得到 xxxxxx
	 * 
	 * @param path
	 * @return
	 */
	public static String getSub(String path) {
		String sub = path.substring(path.lastIndexOf("/") + 1);
		return sub;
	}

	public static String getGifThumbnailName(String package_md5,
			int package_id, int index) {

		// return package_md5 + "_" + package_id + "_" + index + "_" +
		// gif_md5+".jpg";
		return package_md5 + "_" + package_id + "_" + index;
	}

	public static String getGifThumbnailNameFromSD(String package_md5,
			int package_id, int index) {

		// return package_md5 + "_" + package_id + "_" + index + "_" +
		// gif_md5+".jpg";
		return package_md5 + "_" + package_id + "_" + index + ".jpg";
	}

	public static String getGifDownloadName(int pkgid, String md5) {
		if (md5 != null) {
			return pkgid + "_" + md5 + ".gif";
		}
		return null;
	}

	/**
	 * 判断神聊文件下是否有该gif包
	 * 
	 * @return
	 */
	public static boolean hasShenliaoGifBag(String sub) {
		File file = Environment.getExternalStorageDirectory();
		String path = file.getAbsolutePath() + "/shenliao";
		File giffile = new File(path, "gif");
		if (!giffile.exists())
			giffile.mkdirs();
		String newPath = giffile.getAbsolutePath() + "/" + sub;
		File subFile = new File(newPath);
		if (subFile.exists()) {
			return true;
		} else {
			return false;
		}
	}
}
