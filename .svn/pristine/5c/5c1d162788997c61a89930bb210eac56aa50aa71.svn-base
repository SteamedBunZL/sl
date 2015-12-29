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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import ru.truba.touchgallery.GalleryWidget.BasePagerAdapter.OnItemChangeListener;
import ru.truba.touchgallery.GalleryWidget.FilePagerAdapter;
import ru.truba.touchgallery.GalleryWidget.GalleryViewPager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.utils.Utils;

public class GalleryFileActivity extends BaseActivity {
	private static final String TAG = "GalleryFileActivity";
	public static final String IMAGE_PATH_LIST = "image_path_list";
	public static final String CURRENT_ITEM = "current_item";

	private GalleryViewPager mViewPager;
	private TextView tv_currentPageNum;
	private ImageView iv_more;

	private int currentItem;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery_files);

		Intent intent = getIntent();
		currentItem = intent.getIntExtra(CURRENT_ITEM, 0);
		final ArrayList<String> imagePath = intent
				.getStringArrayListExtra(IMAGE_PATH_LIST);
		tv_currentPageNum = (TextView) findViewById(R.id.tv_current_page);
		iv_more = (ImageView) findViewById(R.id.rl_gallery_more);
		iv_more.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String[] items = { "保存到相册", "用其他应用打开", "取消" };
				Utils.creatDialogMenu(thisContext, items, "操作",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String imageFilePath = imagePath
										.get(currentItem);
								switch (which) {
								case 0:
									// 保存到相册
									
									try {
										Bitmap bm = mViewPager.getCurrentBitmap();
//										Bitmap bm = BitmapFactory.decodeStream(new FileInputStream(imageFilePath));
										if (bm!=null) {
											Utils.savePictureToGallery(thisContext, bm);
											showToast("保存成功");
										}else {
											if(Utils.debug)
											showToast("保存失败");
										}
									} catch (Exception e) {
										if(Utils.debug)Log.e(TAG,"保存图片到相册异常",e);
										showToast("保存失败");
									} catch (Error err) {
										System.gc();
										if(Utils.debug)Log.e(TAG,"保存图片到相册ERROR了,我的内存啊。",err);
										showToast("保存失败");
									}
									break;
								case 1:
									// 用其他应用打开
									try {
										Utils.openPicture(thisContext,
												imageFilePath);
									} catch (Exception e) {
										if (Utils.debug)
											Log.e(TAG, "打开图片【" + imageFilePath+ "】文件异常", e);
										showToast("没有找到其他应用");
									}
									break;
								case 2:
									// 取消
									dialog.dismiss();
									break;
								}

							}
						});

			}
		});

		FilePagerAdapter pagerAdapter = new FilePagerAdapter(this, imagePath);
		pagerAdapter.setOnItemChangeListener(new OnItemChangeListener() {
			@Override
			public void onItemChange(int currentPosition) {
				currentItem = currentPosition;// 给成员变量设置当前图片位置
				tv_currentPageNum.setText((currentPosition + 1) + "/"
						+ imagePath.size());
			}
		});

		mViewPager = (GalleryViewPager) findViewById(R.id.viewer);
		mViewPager.setOffscreenPageLimit(3);
		mViewPager.setAdapter(pagerAdapter);
		mViewPager.setCurrentItem(currentItem);
	}

	public void copy(InputStream in, File dst) throws IOException {

		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

}