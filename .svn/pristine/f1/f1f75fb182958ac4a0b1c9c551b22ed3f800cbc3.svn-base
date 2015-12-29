/*
 Copyright (c) 2012 Roman Truba

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial
 portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.tuixin11sms.tx.activity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.truba.touchgallery.GalleryWidget.BasePagerAdapter.OnItemChangeListener;
import ru.truba.touchgallery.GalleryWidget.GalleryViewPager;
import ru.truba.touchgallery.GalleryWidget.UrlPagerAdapter;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shenliao.group.activity.GroupTip;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.MD5;
import com.tuixin11sms.tx.model.AlbumItem;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

/**查看相册大图页面*/
public class GalleryUrlActivity extends BaseActivity {
	private static final String TAG = "GalleryUrlActivity";
	public static final String ALBUM_LIST = "aiList";
	public static final String POSITION = "pos";
	public static final String UID = "uid";
	private PopupWindow upMorePopWindow;
	private long userId;
	private ArrayList<AlbumItem> aiList;
	private int currentPos;
	private GalleryViewPager mViewPager;
	private TextView tv_currentPageNum;
	private ImageView iv_more;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent startIntent = getIntent();
		userId = startIntent.getLongExtra(UID, 0);
		aiList = (ArrayList<AlbumItem>) startIntent
				.getSerializableExtra(ALBUM_LIST);
		currentPos = startIntent.getIntExtra(POSITION, 0);
		setContentView(R.layout.activity_album_gallery);
		
		View v_back_btn = findViewById(R.id.btn_back_left);
		v_back_btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 返回按钮
				thisContext.finish();
			}
		});
		
		tv_currentPageNum = (TextView) findViewById(R.id.tv_current_album_page);
		iv_more = (ImageView) findViewById(R.id.iv_moreBtn);
		if (userId == TX.tm.getUserID()) {
			iv_more.setVisibility(View.GONE);
		}
		iv_more.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				createMoreMenu();

			}
		});
		final List<String> items = new ArrayList<String>();
		for (AlbumItem abItem : aiList) {
			if (!abItem.isAdd()) {
				// 如果isAdd等于true,代表是从设置页面传递过来的，那个item是用户点击上传相册用的
				items.add(abItem.getUrl());
			}
		}

		UrlPagerAdapter pagerAdapter = new UrlPagerAdapter(this, items);
		pagerAdapter.setOnItemChangeListener(new OnItemChangeListener() {
			@Override
			public void onItemChange(int currentPosition) {
				currentPos = currentPosition;// 给成员变量设置当前图片位置
				tv_currentPageNum.setText((currentPosition + 1) + "/"
						+ items.size());
			}
		});

		mViewPager = (GalleryViewPager) findViewById(R.id.vp_content);
		if (Utils.debug)
			Log.i(TAG, "mViewPager.hashCode():" + mViewPager.hashCode());
		mViewPager = (GalleryViewPager) findViewById(R.id.vp_content);
		if (Utils.debug)
			Log.i(TAG, "第二次的哈希值mViewPager.hashCode():" + mViewPager.hashCode());
		mViewPager.setOffscreenPageLimit(3);
		mViewPager.setAdapter(pagerAdapter);
		mViewPager.setCurrentItem(currentPos);

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

	private void createMoreMenu() {
		if (upMorePopWindow == null) {
			View popView = getLayoutInflater().inflate(
					R.layout.user_album_more_pop, null);
			upMorePopWindow = new PopupWindow(popView,
					(int) (Utils.SreenW * 0.55),
					LinearLayout.LayoutParams.WRAP_CONTENT, true);// 创建PopupWindow实例

			upMorePopWindow.setAnimationStyle(R.style.chat_up_anim);

			upMorePopWindow.setFocusable(true);

			upMorePopWindow.setBackgroundDrawable(new BitmapDrawable());
			RelativeLayout uplistreport = (RelativeLayout) popView
					.findViewById(R.id.userinfo_more_item1);
			RelativeLayout uplistcancle = (RelativeLayout) popView
					.findViewById(R.id.userinfo_more_item_cancle);
			RelativeLayout saveImgArea = (RelativeLayout) popView
					.findViewById(R.id.save_img_relayout);
			saveImgArea.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (upMorePopWindow != null) {
						upMorePopWindow.dismiss();
					}
					try {
						saveImage();
					} catch (IOException e) {
						if (Utils.debug) {
							Log.e(TAG, "保存相册大图异常", e);
						}
						showToast("保存失败");
					}
				}
			});
			// 举报用户
			uplistreport.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (upMorePopWindow != null) {
						upMorePopWindow.dismiss();
					}
					Intent intent = new Intent(thisContext, GroupTip.class);
					intent.putExtra(GroupTip.UID, userId);
					startActivity(intent);

				}
			});
			// 取消
			uplistcancle.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (upMorePopWindow != null) {
						upMorePopWindow.dismiss();
					}
				}
			});
			upMorePopWindow.update();

			upMorePopWindow.showAsDropDown(iv_more,
					-(int) (Utils.SreenW * 0.4), 0);
		} else {
			if (upMorePopWindow.isShowing()) {

				upMorePopWindow.dismiss();

			} else {
				upMorePopWindow.update();

				upMorePopWindow.showAsDropDown(iv_more,
						-(int) (Utils.SreenW * 0.4), 0);

			}

		}
	}

	// 保存相册大图片到shenliao目录下
	private void saveImage() throws IOException {
		String albumUrl = aiList.get(mViewPager.getCurrentItem()).getUrl();
		if (albumUrl != null) {
			String sourcefile = mSess.mDownUpMgr.getAlbumFile(albumUrl, true);
			if (Utils.debug) {
				Log.i(TAG, "相册图片路径file:" + sourcefile);
			}
			if (sourcefile != null) {
				File avatar = new File(sourcefile);
				if (avatar.exists()) {
					// 拷贝到shenliao目录下
					StringBuffer tempPath = new StringBuffer().append(Utils
							.getStoragePath(this));
					String mLastPath = tempPath + "/" + Utils.PHOTO_IMAGE_PATH;
					File mLastFolder = new File(mLastPath);
					if (!mLastFolder.exists()) {
						mLastFolder.mkdirs();
					}
					String filePath = mLastPath
							+ "/"
							+ new MD5().getMD5ofStr(aiList.get(currentPos)
									.getUrl()) + ".jpg";
					copyFile(sourcefile, filePath);// 复制文件到shenliao目录
					Uri localUri = Uri.fromFile(new File(filePath));
					Intent localIntent = new Intent(
							Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
					sendBroadcast(localIntent);
					showToast(R.string.save_img);
				}
			}
		}

	}

	// 复制文件
	public static void copyFile(String sourceFile, String targetFile)
			throws IOException {
		BufferedInputStream inBuff = null;
		BufferedOutputStream outBuff = null;
		try {
			// 新建文件输入流并对它进行缓冲
			inBuff = new BufferedInputStream(new FileInputStream(sourceFile));

			// 新建文件输出流并对它进行缓冲
			outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

			// 缓冲数组
			byte[] b = new byte[1024 * 5];
			int len;
			while ((len = inBuff.read(b)) != -1) {
				outBuff.write(b, 0, len);
			}
			// 刷新此缓冲的输出流
			outBuff.flush();
		} finally {
			// 关闭流
			if (inBuff != null)
				inBuff.close();
			if (outBuff != null)
				outBuff.close();
		}
	}

}