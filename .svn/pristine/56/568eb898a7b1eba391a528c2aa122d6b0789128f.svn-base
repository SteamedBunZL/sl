package com.tuixin11sms.tx.core;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;

import u.aly.cu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.shenliao.user.expressionpacage.ExpressionPackage;
import com.tuixin11sms.tx.ExpressBackView;
import com.tuixin11sms.tx.ExpressOuterView;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.SessionManager.EmojiInfor;
import com.tuixin11sms.tx.activity.BaseMsgRoom;
import com.tuixin11sms.tx.activity.GifPackageDownActivity;
import com.tuixin11sms.tx.activity.explorer.FileManager;
import com.tuixin11sms.tx.gif.ClickGifMessage;
import com.tuixin11sms.tx.gif.GifDownActivity;
import com.tuixin11sms.tx.gif.GifOrderActivity;
import com.tuixin11sms.tx.gif.GifTranscoldMgr;
import com.tuixin11sms.tx.gif.MD5FileUtil;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.utils.Utils;

public class MyPopupWindow implements Serializable {
	public static String TAG = "MyPopupWindow";
	private SmileyParser sParser;
	private int face_page = 1;
	private int densityDpi;
	private SmileyParser sParser_edittext_hdpi;
	private final int Num = 17;
	private int pageNum = Num;// 每页显示数量
	Activity con;
	private ArrayList<EmojiInfor> mList = new ArrayList<EmojiInfor>();
	private ArrayList<View> mViewList = new ArrayList<View>();
	private View lastView;
	private RelativeLayout Pop;
	public HashMap<String, SoftReference<Bitmap>> btCache = new HashMap<String, SoftReference<Bitmap>>();
	// 只保存当前包的解析的图片
	public HashMap<String, SoftReference<Bitmap>> bdList = new HashMap<String, SoftReference<Bitmap>>();
	public ArrayList<AnimationDrawable> adList = new ArrayList<AnimationDrawable>();
	public SessionManager mSess = SessionManager.getInstance();
	public GifTranscoldMgr gifTranscoldMgr;
	public HashMap<Bitmap, EmojiInfor> bdReList = new HashMap<Bitmap, SessionManager.EmojiInfor>();
	public ArrayList<EmojiInfor> emojiInforList = new ArrayList<SessionManager.EmojiInfor>();
	Object obj = new Object();
	private TxGroup txGroup;
	private ClickGifMessage mClickGifMessage = new ClickGifMessage();
//	public MyPopupWindow pop;
	private HashMap<String, SoftReference<Bitmap>> packageEmojiRepresentPicCache = new HashMap<String, SoftReference<Bitmap>>();
	public Handler gifHandler = new Handler() {

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GifTranscoldMgr.DECODE_GIF_REPRESEND_OVER:
				bdReList = (HashMap<Bitmap, EmojiInfor>) msg.obj;
				Iterator i = bdReList.entrySet().iterator();
				while (i.hasNext()) {
					Map.Entry entry = (Entry) i.next();
					Bitmap bd = (Bitmap) entry.getKey();
					addViewToTab(bd);
				}
				// 添加最后一个加号
				addAssetEmoji(R.drawable.di_on_11);
				addAssetEmoji(R.drawable.add_contacts);
				ll.setVisibility(View.GONE);
				break;

			case GifTranscoldMgr.DECODE_GIF_SINGLE:
				AnimationDrawable ad = (AnimationDrawable) msg.obj;
				img.setImageDrawable(ad);
				ad.start();
				break;
			case GifTranscoldMgr.DECODE_GIF_REPRESEND_THUMB:
				HashMap<String, SoftReference<Bitmap>> cache = (HashMap<String, SoftReference<Bitmap>>) msg.obj;
				btCache.putAll(cache);
				EmojiInfor emoji = getmCurEmoji();
				// 把map中的图片遍历出来，放到list中
				bdList.clear();
				for (int j = 0; j < emoji.emoji_num; j++) {
					String thumbName = FileManager.getGifThumbnailName(
							emoji.emoji_md5, emoji.emoji_id, j);
					if (btCache.containsKey(thumbName)) {
						bdList.put(thumbName, btCache.get(thumbName));
					}
				}
				showGridView();
				break;
			}
		};
	};

	// public void addSingleView(String path, int package_id) throws IOException
	// {
	//
	// BitmapDrawable new_bd = gifTranscoldMgr.getSingleEmojiRepresend(path);
	// if (new_bd != null) {
	// EmojiInfor emojiInfor = new EmojiInfor();
	// emojiInfor.emoji_path = FileManager.getSub(path);
	// emojiInfor.emoji_md5 = FileManager.getFileMD5(path);
	// emojiInfor.emoji_id = package_id;
	// bdReList.put(new_bd.getBitmap(), emojiInfor);
	// ll.removeAllViews();
	// addAssetEmoji(R.drawable.e415);
	// Iterator i = bdReList.entrySet().iterator();
	// while (i.hasNext()) {
	// Map.Entry entry = (Entry) i.next();
	// BitmapDrawable bd = (BitmapDrawable) entry.getKey();
	// addViewToTab(bd.getBitmap());
	// }
	// addAssetEmoji(R.drawable.di_on_11);
	// addAssetEmoji(R.drawable.add_contacts);
	// }
	//
	// }

	public void setViews(List<EmojiInfor> list) {
		ll.removeAllViews();
		bdReList.clear();
		addAssetEmoji(R.drawable.e415);
		for (EmojiInfor emoji : list) {
			SoftReference<Bitmap> soft = packageEmojiRepresentPicCache
					.get(emoji.emoji_md5);
			Bitmap bitmap = null;
			if (soft != null) {
				bitmap = soft.get();
			}
			if (bitmap == null) {
				String path = FileManager.getShenLiaoGifPackagePath()
						+ emoji.emoji_id + ".jpg";
				File file = new File(path);
				if (file.exists()) {
					bitmap = BitmapFactory.decodeFile(path);
				} else {
					bitmap = gifTranscoldMgr
							.getSingleEmojiRepresend(mSess.mUserEmojiInforsMgr
									.getShenLiaoLocalGifPath(emoji.emoji_path));
				}
				if (bitmap != null) {
					packageEmojiRepresentPicCache.put(emoji.emoji_md5,
							new SoftReference<Bitmap>(bitmap));
				}
			}
			if (bitmap != null) {
				bdReList.put(bitmap, emoji);
				addViewToTab(bitmap);
			}
		}
		addAssetEmoji(R.drawable.di_on_11);
		addAssetEmoji(R.drawable.add_contacts);
	}

	public class SmileyAdapter extends BaseAdapter {
		public int getCount() {
			return pageNum + 1;
		}

		public Object getItem(int i) {
			return null;
		}

		public long getItemId(int i) {
			return 0L;
		}

		public View getView(int i, View view, ViewGroup viewgroup) {
			ImageView imageview;
			if (view == null) {
				imageview = new ImageView(mContext);
				android.widget.AbsListView.LayoutParams layoutparams = //
				new android.widget.AbsListView.LayoutParams(
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT,//
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
				imageview.setLayoutParams(layoutparams);
				android.widget.ImageView.ScaleType scaletype = android.widget.ImageView.ScaleType.CENTER;
				imageview.setScaleType(scaletype);
				imageview.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						gridview_layout.isIntercept = false;
						float dx;
						switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							gridview_layout.b_x = gridview_layout.e_x = event
									.getX();
							break;
						case MotionEvent.ACTION_UP:
							gridview_layout.e_x = event.getX();
							dx = gridview_layout.b_x - gridview_layout.e_x;
							gridview_layout.b_x = -1;
							gridview_layout.e_x = -1;
							if (Math.abs(dx) > 50) {
								turnPage(dx);
								return true;
							}

							int id = (Integer) v.getTag();
							if (id < 0) {
								View focusView = con.getCurrentFocus();
								focusView.dispatchKeyEvent(new KeyEvent(
										KeyEvent.ACTION_DOWN,
										KeyEvent.KEYCODE_DEL));
							} else {
								String s1 = SmileyParser.emots[id
										+ (face_page - 1) * Num].mEsc1;

								int index = editText.getSelectionStart();
								Editable edit = editText.getEditableText();
								if (index < 0 || index >= edit.length()) {
									if (densityDpi > 160)
										edit.append(sParser_edittext_hdpi
												.addSmileySpans(s1, true, 0));
									else
										edit.append(sParser.addSmileySpans(s1,
												true, 0));
								} else {
									if (densityDpi > 160)
										edit.insert(index,
												sParser_edittext_hdpi
														.addSmileySpans(s1,
																true, 0));
									else
										edit.insert(index, sParser
												.addSmileySpans(s1, true, 0));
								}
							}
							isShow = true;
							break;
						case MotionEvent.ACTION_MOVE:
							gridview_layout.e_x = event.getX();
							break;
						case MotionEvent.ACTION_CANCEL:
							dx = gridview_layout.b_x - gridview_layout.e_x;
							gridview_layout.b_x = -1;
							gridview_layout.e_x = -1;
							if (Math.abs(dx) > 50) {
								turnPage(dx);
								return true;
							}
						}
						return true;
					}
				});
			} else {
				imageview = (ImageView) view;
			}
			if (i == pageNum) {
				imageview.setImageResource(R.drawable.smile_deleat);
				imageview.setTag(-1);
			} else {
				imageview.setImageBitmap(sParser.getSmileyBitmap(i
						+ (face_page - 1) * Num));
				imageview.setTag(i);
			}
			return imageview;
		}

		private Context mContext;

		public SmileyAdapter(Context context) {
			mContext = context;
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				MyPopupWindow.this.editText.requestFocus();
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.FILL_PARENT, bmpHeight);
				mPop.setLayoutParams(lp);
				isShow = false;
			}
		}
	};
	private int bmpWidth;
	private int bmpHeight;
	private LinearLayout ll;
	ImageView img;

	public MyPopupWindow(Activity chatRoom, RelativeLayout Pop, int screenW,
			int screenH, EditText editText, int densityDpi,
			HorizontalScrollView hsv, LinearLayout ll, ImageView img,
			TxGroup txGroup) {
		gifTranscoldMgr = GifTranscoldMgr.getInstance();
		this.densityDpi = densityDpi;
		this.chatRoom = chatRoom;
		bmpWidth = screenW;
		bmpHeight = screenH / 3;
		con = chatRoom;
		this.editText = editText;
		sParser = new SmileyParser(this.chatRoom);
		sParser_edittext_hdpi = new SmileyParser(this.chatRoom);
		sParser_edittext_hdpi.setInChatView(true);
		this.ll = ll;
		initExpressionPackageData(ll);
		this.Pop = Pop;
		initPopMenu(Pop);
		this.img = img;
		if (txGroup != null) {
			this.txGroup = txGroup;
		}
	}

	public ClickGifMessage getCurClickGifMessage() {
		return mClickGifMessage;
	}

	ArrayList<EmojiInfor> emojiListUser;

	// 初始化的时候，要读取sp中的表情包数据
	public void initExpressionPackageData(LinearLayout ll) {
		ExpressionPackage defaultExrepssion1 = new ExpressionPackage(
				R.drawable.e415);
		ExpressionPackage defaultExpression2 = new ExpressionPackage(
				R.drawable.add_icon);
		ll.removeAllViews();
		// 添加第一个标准表情栏
		addAssetEmoji(R.drawable.e415);
		try {
			emojiListUser = mSess.mUserEmojiInforsMgr
					.getCurUserEmoji(mSess.mPrefMeme.user_id.getVal());
			gifTranscoldMgr.getGifRepresendList(
					mSess.mPrefMeme.user_id.getVal(), gifHandler);
		} catch (JSONException e) {
			ll.removeAllViews();
			addAssetEmoji(R.drawable.e415);
			addAssetEmoji(R.drawable.di_on_11);
			addAssetEmoji(R.drawable.add_contacts);
			ll.setVisibility(View.GONE);
			e.printStackTrace();
		}
	}

	RandomAccessFile mFile;
	String newName;

	/**
	 * 从资源中添加表情栏
	 */
	// mViewList.get(0).setBackgroundColor(
	// con.getResources()
	// .getColor(R.color.content_line_color_gray));
	public void addAssetEmoji(final int res) {
		final View view = View.inflate(con, R.layout.item_horizable_expression,
				null);
		ImageView img = (ImageView) view
				.findViewById(R.id.img_item_horizable_expression);
		img.setImageResource(res);
		mViewList.add(view);
		if (res == R.drawable.e415) {
			setOneViewClick(view);
			lastView = view;
		}
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				clearBlackground();
				setOneViewClick(v);
				if (res == R.drawable.e415) {
					mPop.removeAllViews();
					initPopMenu(Pop);
					mPop.setVisibility(View.VISIBLE);
					lastView = v;
				} else if (res == R.drawable.add_contacts) {
					// 表情添加按钮的跳转
					Intent intent = new Intent(con,
							GifPackageDownActivity.class);
					con.startActivityForResult(intent,
							BaseMsgRoom.REQ_TAKE_GIF_FILE);

				} else if (res == R.drawable.di_on_11) {
					// 设置按钮的跳转
					Intent intent = new Intent(con, GifOrderActivity.class);
					con.startActivityForResult(intent,
							BaseMsgRoom.REQ_TAKE_GIF_SETTING);
				}
			}
		});
		View view_line = View.inflate(con,
				R.layout.item_horizable_expreission_vertical_line, null);
		ll.addView(view);
		ll.addView(view_line);
		// ll.setVisibility(View.GONE);
	}

	public void initLastView() {
		if (lastView != null) {
			clearBlackground();
			setOneViewClick(lastView);
		}
		mPop.removeAllViews();
		initPopMenu(Pop);
		mPop.setVisibility(View.VISIBLE);
	}

	public void addViewToTab(final Bitmap bd) {
		final View view = View.inflate(con, R.layout.item_horizable_expression,
				null);
		ImageView img = (ImageView) view
				.findViewById(R.id.img_item_horizable_expression);
		img.setImageBitmap(bd);
		mViewList.add(view);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				clearBlackground();
				view.setBackgroundColor(con.getResources().getColor(
						R.color.content_line_color_gray));
				mPop.removeAllViews();
				EmojiInfor emoji = bdReList.get(bd);
				setmCurEmoji(emoji);
				// 先从缓存读，没有再从本地加载
				// 改为从本地加载，也是先进缓存 TODO
				// 本地再没有，现从本地包中解析第一张图片

				// 从缓存中读，首先看是否是对应的图片？
				// if (bdList.size() != 0) {
				// showGridView();
				// }else {
				//
				// }
				//
				//
				//
				// gifTranscoldMgr.getRepresendThumb(
				// FileManager.getShenLiaoGifPackagePath()
				// + emoji.emoji_path, emoji.emoji_md5,
				// emoji.emoji_id, gifHandler);
				showGridView();
				lastView = v;

			}
		});
		View view_line = View.inflate(con,
				R.layout.item_horizable_expreission_vertical_line, null);
		ll.addView(view);
		ll.addView(view_line);
	}

	public void onClick() {
		if (mPop.getVisibility() == View.GONE) {
			mPop.setVisibility(View.VISIBLE);
			// TODO 表情显示，下部添加栏也有view
			ll.setVisibility(View.VISIBLE);
		} else {
		}
	}

	View mView;
	GridView mGridView;
	LinearLayout mLayout;
	ImageView mImageView[];
	newAdapter mNeAdapter;
	RelativeLayout smiley_main_layout_1;
	ExpressOuterView outView;

	public synchronized void showGridView() {
		if (mPop == null || mPop.getChildCount() == 0) {
			face_page = 1;
			mPageNum = mNum;
			inflater = (LayoutInflater) chatRoom
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mView = inflater.inflate(R.layout.smiley_gridview, null);
			outView = (ExpressOuterView) mView
					.findViewById(R.id.smiley_gridview_layout_1);
			smiley_main_layout_1 = (RelativeLayout) mView
					.findViewById(R.id.smiley_main_layout_1);
			mGridView = (GridView) mView.findViewById(R.id.smiley_gridview11_1);
			mLayout = (LinearLayout) mView
					.findViewById(R.id.smiley_page_layout_1);
			int pagenum = mCurEmoji.emoji_num / 18 + 1;
			mImageView = new ImageView[pagenum];
			for (int i = 0; i < mImageView.length; i++) {
				mImageView[i] = new ImageView(con);
				LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(
						android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
						android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
				mImageView[i].setPadding(5, 5, 0, 5);
				mImageView[i].setImageResource(R.drawable.presence_invisible);
				mImageView[0].setImageResource(R.drawable.presence_online);
				mLayout.addView(mImageView[i], ll);
			}
			mNeAdapter = new newAdapter();
			mGridView.setAdapter(mNeAdapter);
			mPop = Pop;
			mPop.removeAllViews();// 先清空该layout所有的view,否则会可能出现表情重叠的问题。2014.01.14
									// shc
			View v_line = new View(chatRoom);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, Utils.dip2px(0.5f, chatRoom));
			v_line.setBackgroundColor(chatRoom.getResources().getColor(
					R.color.content_line_color_gray));
			v_line.setLayoutParams(params);
			mPop.addView(v_line);
			mPop.addView(mView);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.FILL_PARENT, bmpHeight);
			mPop.setLayoutParams(lp);

			flipperListener = new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						outView.touch_x1 = outView.touch_x2 = event.getX();
						break;

					case MotionEvent.ACTION_UP:
						outView.touch_x2 = event.getX();
					case MotionEvent.ACTION_CANCEL:
						float dx = outView.touch_x1 - outView.touch_x2;
						outView.touch_x1 = -1;
						outView.touch_x2 = -1;
						if (Math.abs(dx) > 50)
							turnPage2(dx);
						break;
					case MotionEvent.ACTION_MOVE:
						outView.touch_x2 = event.getX();
						break;
					}
					return true;
				}
			};

			mPop.setOnTouchListener(flipperListener);
			outView.setOnTouchListener(flipperListener);

		}

	}

	float touch_x1 = -1;
	float touch_x2 = -1;
	float mDx;

	public class newAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			if (Utils.debug) {
				Log.d(TAG, TAG + "2015/4/12 mPageNum:" + mPageNum);
				Log.d(TAG, TAG + "2015/4/12 size:" + bdList.size());
			}
			return mCurEmoji.emoji_num > 17 ? mPageNum : mCurEmoji.emoji_num;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final ImageView imageview;
			if (convertView == null) {
				imageview = new ImageView(con);

				android.widget.AbsListView.LayoutParams layoutparams = //
				new android.widget.AbsListView.LayoutParams(70, 70);
				imageview.setLayoutParams(layoutparams);
				android.widget.ImageView.ScaleType scaletype = android.widget.ImageView.ScaleType.FIT_CENTER;
				imageview.setScaleType(scaletype);
				imageview.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						// TODO
						outView.isIntercept = false;
						switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							outView.touch_x1 = outView.touch_x2 = event.getX();
							Log.i(TAG, "====" + "donw = " + touch_x1);
							break;
						case MotionEvent.ACTION_UP:
							outView.touch_x2 = event.getX();
							Log.i(TAG, "====" + "up = " + touch_x2);
							mDx = outView.touch_x1 - outView.touch_x2;
							outView.touch_x1 = -1;
							outView.touch_x2 = -1;
							if (Math.abs(mDx) > 50) {
								turnPage2(mDx);
								return true;
							}
							mClickGifMessage.setI(position + (face_page - 1)
									* mNum);
							mClickGifMessage.setPath(getmCurEmoji().emoji_path);
							((BaseMsgRoom) chatRoom).sendGifMessage(position
									+ (face_page - 1) * mNum);
							break;
						case MotionEvent.ACTION_MOVE:
							outView.touch_x2 = event.getX();
							break;
						case MotionEvent.ACTION_CANCEL:
							mDx = outView.touch_x1 - outView.touch_x2;
							outView.touch_x1 = -1;
							outView.touch_x2 = -1;
							if (Math.abs(mDx) > 50) {
								turnPage2(mDx);
								return true;
							}
							break;
						}
						return true;
					}
				});
			} else {
				imageview = (ImageView) convertView;
			}

			final String cur = FileManager.getGifThumbnailName(
					mCurEmoji.emoji_md5, mCurEmoji.emoji_id, position
							+ (face_page - 1) * mNum);
			SoftReference<Bitmap> soft = bdList.get(cur);
			Bitmap bitmap = null;
			if (soft != null) {
				bitmap = soft.get();
			}
			if (bitmap != null) {
				imageview.setImageBitmap(bitmap);
			} else {
				// 从本地文件中读取
				new Thread() {
					public void run() {
						String path = FileManager.getShenLiaoGifImgPath() + cur
								+ ".jpg";
						File file = new File(path);
						if (file.exists()) {
							final Bitmap bitmap = BitmapFactory
									.decodeFile(path);
							// btCache.put(cur, new
							// SoftReference<Bitmap>(bitmap));
							bdList.put(cur, new SoftReference<Bitmap>(bitmap));
							gifHandler.post(new Runnable() {

								@Override
								public void run() {
									imageview.setImageBitmap(bitmap);
								}
							});
						} else {
							gifHandler.post(new Runnable() {

								@Override
								public void run() {
									imageview.setImageBitmap(null);
								}
							});
						}
					};
				}.start();
			}
			imageview.setTag(position);
			return imageview;
		}

	}

	private EmojiInfor mCurEmoji;

	public EmojiInfor getmCurEmoji() {
		return mCurEmoji;
	}

	public void setmCurEmoji(EmojiInfor mCurEmoji) {
		this.mCurEmoji = mCurEmoji;
	}

	/**
	 * 清除所有view的背景 #BFBFBF
	 */
	public void clearBlackground() {
		for (View view : mViewList) {
			view.setBackgroundColor(con.getResources().getColor(
					R.color.expression_view_normal));
		}
	}

	public void setOneViewClick(View v) {
		if (v != null) {
			v.setBackgroundColor(con.getResources().getColor(
					R.color.expression_gray));
		}
	}

	GridView gridview;
	SmileyAdapter smileyadapter;
	OnTouchListener flipperListener;
	RelativeLayout main_layout;
	ExpressBackView gridview_layout;

	private void initPopMenu(RelativeLayout Pop) {
		if (mPop == null || mPop.getChildCount() == 0) {
			inflater = (LayoutInflater) chatRoom
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			view = inflater.inflate(R.layout.smiley_dialog, null);
			gridview = (GridView) view.findViewById(R.id.smiley_gridview11);
			main_layout = (RelativeLayout) view
					.findViewById(R.id.smiley_main_layout);
			gridview_layout = (ExpressBackView) view
					.findViewById(R.id.smiley_gridview_layout);
			smileyadapter = new SmileyAdapter(chatRoom.getApplicationContext());
			gridview.setAdapter(smileyadapter);
			ivs = new ImageView[8];
			ivs[0] = (ImageView) view.findViewById(R.id.tab_point);
			ivs[1] = (ImageView) view.findViewById(R.id.tab_point2);
			ivs[2] = (ImageView) view.findViewById(R.id.tab_point3);
			ivs[3] = (ImageView) view.findViewById(R.id.tab_point4);
			ivs[4] = (ImageView) view.findViewById(R.id.tab_point5);
			ivs[5] = (ImageView) view.findViewById(R.id.tab_point6);
			ivs[6] = (ImageView) view.findViewById(R.id.tab_point7);
			ivs[7] = (ImageView) view.findViewById(R.id.tab_point8);
			// ivs[8] = (ImageView) view.findViewById(R.id.tab_point9);
			mPop = Pop;
			mPop.removeAllViews();// 先清空该layout所有的view,否则会可能出现表情重叠的问题。2014.01.14
									// shc
			View v_line = new View(chatRoom);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, Utils.dip2px(0.5f, chatRoom));
			v_line.setBackgroundColor(chatRoom.getResources().getColor(
					R.color.content_line_color_gray));
			v_line.setLayoutParams(params);
			mPop.addView(v_line);
			mPop.addView(view);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.FILL_PARENT, bmpHeight);
			mPop.setLayoutParams(lp);
			flipperListener = new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						gridview_layout.b_x = gridview_layout.e_x = event
								.getX();
						break;

					case MotionEvent.ACTION_UP:
						gridview_layout.e_x = event.getX();
					case MotionEvent.ACTION_CANCEL:
						float dx = gridview_layout.b_x - gridview_layout.e_x;
						gridview_layout.b_x = -1;
						gridview_layout.e_x = -1;
						if (Math.abs(dx) > 50)
							turnPage(dx);
						break;
					case MotionEvent.ACTION_MOVE:
						gridview_layout.e_x = event.getX();
						break;
					}
					return true;
				}
			};
			mPop.setOnTouchListener(flipperListener);
			gridview_layout.setOnTouchListener(flipperListener);
		}
		if (mPop.getVisibility() == View.VISIBLE) {
			mPop.setVisibility(View.GONE);
		}
	}

	public RelativeLayout mPop;// 显示表情的layout？
	Activity chatRoom;
	LayoutInflater inflater;
	private boolean isShow = true;
	View view;
	EditText editText;

	public RelativeLayout getPopupWindow() {
		return mPop;
	}

	public SmileyParser getSParser() {
		return sParser;
	}

	public SmileyParser getSParser_edittext_hdpi() {
		return sParser_edittext_hdpi;
	}

	int x = 1;
	ImageView[] ivs;

	public void turnPage(float p) {
		if (p > 0) {
			face_page++;
			if (face_page > ivs.length) {
				face_page = 1;
			}

		} else {
			face_page--;
			if (face_page < 1) {
				face_page = ivs.length;
			}
		}
		if ((face_page * Num) > smileyLen()) {
			pageNum = smileyLen() % Num;
		} else {
			pageNum = Num;
		}
		smileyadapter.notifyDataSetChanged();
		for (int i = 0; i < ivs.length; i++) {
			if (i + 1 == face_page) {
				ivs[i].setImageResource(R.drawable.presence_online);
			} else {
				ivs[i].setImageResource(R.drawable.presence_invisible);
			}
		}

	};

	public static final int mNum = 18;
	public int mPageNum = mNum;

	public void turnPage2(float p) {
		if (p > 0) {
			face_page++;
			if (face_page > mImageView.length) {
				face_page = 1;
			}

		} else {
			face_page--;
			if (face_page < 1) {
				face_page = mImageView.length;
			}
		}
		Log.i(TAG, "===" + "p = " + p + ",facepacge = " + face_page);
		if ((face_page * mNum) > mCurEmoji.emoji_num) {
			mPageNum = mCurEmoji.emoji_num % mNum;
		} else {
			mPageNum = mNum;
		}
		mNeAdapter.notifyDataSetChanged();
		for (int i = 0; i < mImageView.length; i++) {
			if (i + 1 == face_page) {
				mImageView[i].setImageResource(R.drawable.presence_online);
			} else {
				mImageView[i].setImageResource(R.drawable.presence_invisible);
			}
		}

	};

	public void reset() {
	}

	private int smileyLen() {
		return SmileyParser.emots.length;
	}
}
