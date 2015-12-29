package com.tuixin11sms.tx.gif;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;

import android.R.integer;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.nfc.Tag;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.SessionManager.EmojiInfor;
import com.tuixin11sms.tx.activity.explorer.FileManager;
import com.tuixin11sms.tx.core.MyPopupWindow.newAdapter;
import com.tuixin11sms.tx.utils.Utils;
import com.tx.face.nativef.IParseFaceObserver;
import com.tx.face.nativef.NativeFace;

public class GifTranscoldMgr {

	private static final String TAG = GifTranscoldMgr.class.getSimpleName();
	private int gif_num;
	public String gif_bag_name = "";
	private ArrayList<String> nameList = new ArrayList<String>();
	private String mPath;
	private RandomAccessFile mFile;
	private HashMap<Bitmap, EmojiInfor> bdRepresendList = new HashMap<Bitmap, EmojiInfor>();
	private ExecutorService threadPools = Executors.newFixedThreadPool(Max);
	public SessionManager mSess = SessionManager.getInstance();
	public static final int Max = 5;
	public static final int DECODE_GIF_BAG_OVER = 1;
	public static final int DECODE_GIF_REPRESEND_OVER = 2;
	public static final int DECODE_GIF_SINGLE = 3;
	public static final int DECODE_GIF_PRE = 4;
	public static final int DECODE_GIF_REPRESEND_THUMB = 5;
	private ArrayList<gifOpenHelper> helpList = new ArrayList<gifOpenHelper>();
	private HashMap<Long, SoftReference<AnimationDrawable>> animationCache = new HashMap<Long, SoftReference<AnimationDrawable>>();
	private HashMap<Long, SoftReference<gifOpenHelper>> helperCache = new HashMap<Long, SoftReference<gifOpenHelper>>();

	private static GifTranscoldMgr gifTranscoldMgr = new GifTranscoldMgr();

	private GifTranscoldMgr() {

	}

	public static GifTranscoldMgr getInstance() {
		return gifTranscoldMgr;
	}

	/**
	 * 解码gif包，并且得到gif数量和包名,调用其他方法之前要调用这个，是同步方法
	 */
	public synchronized String getGifMessage(String path) {
		gif_num = -1;
		nameList.clear();
		NativeFace.getFaceIntro(path, new IParseFaceObserver() {

			@Override
			public int onParseEvent(int event, String name, byte[] data) {
				if (event == 1) {
					gif_bag_name = name;
				} else if (event == 2) {
					if (name != null) {
						nameList.add(name);
					}
				}
				return 0;
			}
		});
		return gif_bag_name;
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public synchronized void getGifMessage2(final String path) {
		gif_num = -1;
		nameList.clear();
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				NativeFace.getFaceIntro(path, new IParseFaceObserver() {

					@Override
					public int onParseEvent(int event, String name, byte[] data) {
						if (event == 1) {
							gif_bag_name = name;
						} else if (event == 2) {
							if (name != null) {
								nameList.add(name);
							}
						}
						return 0;
					}
				});
				EmojiInfor emoji = new EmojiInfor();
				emoji.emoji_name = gif_bag_name;
				emoji.emoji_path = FileManager.createSaveGifPackageName(-1,
						FileManager.getSub(path), FileManager.getFileMD5(path));
				emoji.emoji_md5 = FileManager.getFileMD5(path);
				emoji.emoji_id = -1;
				emoji.emoji_num = nameList.size();
				try {
					ArrayList<EmojiInfor> list = mSess.mUserEmojiInforsMgr
							.getCurUserEmoji(mSess.mPrefMeme.user_id.getVal());
					if (!list.contains(emoji)) {
						list.add(emoji);
					}
					mSess.mUserEmojiInforsMgr.saveOrUpdateCurUserEmoji(
							mSess.mPrefMeme.user_id.getVal(), list);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
		threadPools.execute(runnable);

	}

	public String getSingleGifMD5(String path, int i) {
		nameList.clear();
		path = FileManager.getShenLiaoGifPackagePath() + path;
		NativeFace.getFaceIntro(path, new IParseFaceObserver() {

			@Override
			public int onParseEvent(int event, String name, byte[] data) {
				if (event == 1) {
					gif_bag_name = name;
				} else if (event == 2) {
					if (name != null) {
						nameList.add(name);
					}
				}
				return 0;
			}
		});
		if (nameList.size() > 0 && nameList != null && nameList.get(i) != null) {
			if (Utils.debug) {
				Log.e(TAG, "2015/1/27 namelist :" + nameList.get(i).toString());
			}
			return nameList.get(i);
		}
		return null;
	}

	/**
	 * 得到包内每个gif文件代表图片的集合,在handler中获取到bdlist path 传入的是gif包的上级文件
	 */
	private String shortName;
	private String gifName;
	private ArrayList<BitmapDrawable> bdList = new ArrayList<BitmapDrawable>();

	public synchronized String getGifList(final String path,
			final Handler handler) {
		getGifMessage(localName + "/" + path);
		// name和list都不为空了
		// bdMap.clear();
		bdList.clear();
		Runnable runnable = new Runnable() {
			public void run() {
				for (int i = 0; i < nameList.size(); i++) {
					gifName = localName + "/" + "index.gif";
					NativeFace.getFaceEntryStream(localName + "/" + path, i,
							new IParseFaceObserver() {

								@Override
								public int onParseEvent(int event, String name,
										byte[] data) {
									if (3 == event) {
										try {
											mFile = new RandomAccessFile(
													gifName, "rw");
										} catch (FileNotFoundException e) {
											e.printStackTrace();
										}
									} else if (4 == event) {
										try {
											mFile.write(data);
										} catch (IOException e) {
											e.printStackTrace();
										}
									} else if (5 == event) {
										try {
											mFile.close();
											InputStream is = null;
											is = new FileInputStream(gifName);
											gifOpenHelper gHelper = new gifOpenHelper();
											gHelper.read(is);
											BitmapDrawable bd = new BitmapDrawable(
													gHelper.getImage());
											bd.setBounds(0, 0,
													bd.getIntrinsicWidth(),
													bd.getIntrinsicHeight());
											bdList.add(bd);

										} catch (IOException e) {
											e.printStackTrace();
										}
									}
									return 0;
								}
							});
				}

				Message msg = handler.obtainMessage();
				msg.what = DECODE_GIF_BAG_OVER;
				msg.obj = bdList;
				handler.sendMessage(msg);
			};
		};
		threadPools.execute(runnable);
		return path;
	}

	private int j = 0;

	public void saveEmojiToSP(boolean isDownload, String path, int package_id,
			int num) {
		if (nameList != null && nameList.size() > 0) {
			EmojiInfor emoji = new EmojiInfor();
			emoji.emoji_name = gif_bag_name;
			if (isDownload) {
				emoji.emoji_path = FileManager.getSub(path);
			} else {
				emoji.emoji_path = FileManager.createSaveGifPackageName(-1,
						FileManager.getSub(path), FileManager.getFileMD5(path));
			}
			emoji.emoji_md5 = FileManager.getFileMD5(path);
			emoji.emoji_id = package_id;
			if (Utils.debug) {
				Log.d(TAG, "4/16 : num:" + num);
			}
			emoji.emoji_num = num;
			try {
				ArrayList<EmojiInfor> list = mSess.mUserEmojiInforsMgr
						.getCurUserEmoji(mSess.mPrefMeme.user_id.getVal());
				if (!list.contains(emoji)) {
					list.add(emoji);
				}
				mSess.mUserEmojiInforsMgr.saveOrUpdateCurUserEmoji(
						mSess.mPrefMeme.user_id.getVal(), list);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void analysisFirstGifBitmap(final String path,
			final String save_path, final String package_md5,
			final int package_id, boolean isDownload) {
		getGifMessage(path);

		saveEmojiToSP(isDownload, path, package_id, nameList.size());
		for (int i = 0; i < nameList.size(); i++) {
			gifName = localName + "/" + "index.gif";
			j = i;
			NativeFace.getFaceEntryStream(path, i, new IParseFaceObserver() {

				@Override
				public int onParseEvent(int event, String name, byte[] data) {
					if (3 == event) {
						try {
							mFile = new RandomAccessFile(gifName, "rw");
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					} else if (4 == event) {
						try {
							mFile.write(data);
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else if (5 == event) {
						InputStream is = null;
						FileOutputStream fos = null;
						gifOpenHelper gHelper = null;
						try {
							mFile.close();
							is = new FileInputStream(gifName);
							gHelper = new gifOpenHelper();
							gHelper.read(is);
							Bitmap bitmap = gHelper.getImage();
							// 把bitmap保存在缩略图文件下

							String cur_thumb_name = FileManager
									.getGifThumbnailNameFromSD(package_md5,
											package_id, j);
							File file = new File(save_path + cur_thumb_name);
							if (bitmap == null) {
								bitmap = BitmapFactory.decodeFile(gifName);
								// return 0;
							}
							if (bitmap == null) {
								return 0;
							}
							if (!file.exists()) {
								// file.mkdir();
								fos = new FileOutputStream(file);
								bitmap.compress(CompressFormat.JPEG, 100, fos);
							} else {
								file.delete();
								fos = new FileOutputStream(file);
								bitmap.compress(CompressFormat.JPEG, 100, fos);
							}
							// 回收
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							if (gHelper != null) {
								gHelper.recycle();
								gHelper = null;
							}
							if (is != null) {
								try {
									is.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							if (fos != null) {
								try {
									fos.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
					return 0;
				}
			});
		}
		// saveEmojiToSP(isDownload, save_path, package_id, j);
		j = 0;

	}

	public String preGetGifList(final String path, final Handler handler,
			final int arg) {
		getGifMessage(localName + "/" + path);
		// name和list都不为空了
		// bdMap.clear();
		bdList.clear();
		Runnable runnable = new Runnable() {
			public void run() {
				for (int i = 0; i < nameList.size(); i++) {
					gifName = localName + "/" + "index.gif";
					NativeFace.getFaceEntryStream(localName + "/" + path, i,
							new IParseFaceObserver() {

								@Override
								public int onParseEvent(int event, String name,
										byte[] data) {
									if (3 == event) {
										try {
											mFile = new RandomAccessFile(
													gifName, "rw");
										} catch (FileNotFoundException e) {
											e.printStackTrace();
										}
									} else if (4 == event) {
										try {
											mFile.write(data);
										} catch (IOException e) {
											e.printStackTrace();
										}
									} else if (5 == event) {
										try {
											mFile.close();
											InputStream is = null;
											is = new FileInputStream(gifName);
											gifOpenHelper gHelper = new gifOpenHelper();
											gHelper.read(is);
											BitmapDrawable bd = new BitmapDrawable(
													gHelper.getImage());
											bd.setBounds(0, 0,
													bd.getIntrinsicWidth(),
													bd.getIntrinsicHeight());
											bdList.add(bd);
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
									return 0;
								}
							});
				}
				// if (bdMap.get(MD5FileUtil.getMD5Path(localName + "/" +
				// path)).get()==null) {
				// bdMap.put(MD5FileUtil.getMD5Path(localName + "/" + path),
				// new SoftReference<ArrayList<BitmapDrawable>>(bdList));
				// }
				// if (bdMap.containsKey(MD5FileUtil.getMD5Path(localName + "/"
				// + path))) {
				// if (bdMap.get(MD5FileUtil
				// .getMD5Path(localName + "/" + path)) == null) {
				// bdMap.put(MD5FileUtil
				// .getMD5Path(localName + "/" + path),
				// new SoftReference<ArrayList<BitmapDrawable>>(
				// bdList));
				// }
				// } else {
				// bdMap.put(
				// MD5FileUtil.getMD5Path(localName + "/" + path),
				// new SoftReference<ArrayList<BitmapDrawable>>(bdList));
				// }
				// Message msg1 = handler.obtainMessage();
				// msg1.what = DECODE_GIF_PRE;
				// msg1.obj = bdMap;
				// handler.sendMessage(msg1);

				Message msg2 = handler.obtainMessage();
				msg2.what = DECODE_GIF_PRE;
				msg2.arg1 = arg;
				msg2.obj = bdList;
				handler.sendMessage(msg2);

			};
		};
		threadPools.execute(runnable);
		return path;
	}

	// public void saveGifList(final String path) {
	// getGifMessage(path);
	// Runnable runnable = new Runnable() {
	//
	// @Override
	// public void run() {
	// for (int i = 0; i < nameList.size(); i++) {
	// NativeFace.getFaceEntryFile(path, i,
	// localName + "/" + i + "_" + nameList.get(i)
	// + ".gif");
	// }
	// }
	// };
	// threadPools.execute(runnable);
	// }

	/**
	 * 得到用户所持有的gif包的代表图片,直接解压第一张就可以
	 * 
	 */
	String localName = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/shenliao/gif_package";
	String earchName;
	String emojiname = "";
	EmojiInfor emoji = null;

	public void getGifRepresendList(String cur_id, final Handler handler) {
		// 获取用户sp中的表情集合
		bdRepresendList.clear();
		try {
			final ArrayList<EmojiInfor> curEmojiList = mSess.mUserEmojiInforsMgr
					.getCurUserEmoji(cur_id);
			Runnable runnable = new Runnable() {

				@Override
				public void run() {
					for (int i = 0; i < curEmojiList.size(); i++) {
						emoji = curEmojiList.get(i);
						earchName = localName + "/"
								+ curEmojiList.get(i).emoji_path;
						File file = new File(earchName);
						if (file.exists()) {
							emojiname = curEmojiList.get(i).emoji_path;
							NativeFace.getFaceEntryStream(earchName, 0,
									new IParseFaceObserver() {

										@Override
										public int onParseEvent(int event,
												String name, byte[] data) {
											if (3 == event) {
												try {
													mFile = new RandomAccessFile(
															"/sdcard/index.gif",
															"rw");
												} catch (FileNotFoundException e) {
													e.printStackTrace();
												}
											} else if (4 == event) {
												try {
													mFile.write(data);
												} catch (IOException e) {
													e.printStackTrace();
												}
											} else if (5 == event) {
												try {
													mFile.close();
													InputStream is = null;
													is = new FileInputStream(
															"/sdcard/index.gif");
													gifOpenHelper gHelper = new gifOpenHelper();
													gHelper.read(is);
													Bitmap bitmap = gHelper
															.getImage();
													bdRepresendList.put(bitmap,
															emoji);
												} catch (IOException e) {
													e.printStackTrace();
												}
											}
											return 0;
										}
									});
						}

					}
					Message msg = handler.obtainMessage();
					msg.what = DECODE_GIF_REPRESEND_OVER;
					msg.obj = bdRepresendList;
					handler.sendMessage(msg);
				}
			};
			threadPools.execute(runnable);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 保存或更新sp中的List
	 * 
	 * @param cur_id
	 * @param list
	 */
	public void saveCurEmojiList(String cur_id, List<EmojiInfor> list) {
		try {
			mSess.mUserEmojiInforsMgr.saveOrUpdateCurUserEmoji(cur_id, list);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	Bitmap bd = null;

	public Bitmap getSingleEmojiRepresend(String name) {
		NativeFace.getFaceEntryStream(name, 0, new IParseFaceObserver() {

			@Override
			public int onParseEvent(int event, String name, byte[] data) {
				if (3 == event) {
					try {
						mFile = new RandomAccessFile("/sdcard/index.gif", "rw");
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				} else if (4 == event) {
					try {
						mFile.write(data);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if (5 == event) {
					try {
						mFile.close();
						InputStream is = null;
						is = new FileInputStream("/sdcard/index.gif");
						gifOpenHelper gHelper = new gifOpenHelper();
						gHelper.read(is);
						bd = gHelper.getImage();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return 0;
			}
		});
		return bd;
	}

	public AnimationDrawable getAnimationDrawable(long msg_id, final int i,
			final String path, String emomd5, int pkgid) {
		gifName = FileManager.getShenLiaoGifPath() + pkgid + "_" + emomd5
				+ ".gif";

		NativeFace.getFaceEntryStream(localName + "/" + path, i,
				new IParseFaceObserver() {

					@Override
					public int onParseEvent(int event, String name, byte[] data) {
						if (3 == event) {
							try {
								mFile = new RandomAccessFile(gifName, "rw");
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
						} else if (4 == event) {
							try {
								mFile.write(data);
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else if (5 == event) {
							try {
								mFile.close();

							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						return 0;
					}
				});

		AnimationDrawable mSmile = null;
		if (animationCache.containsKey(msg_id)
				&& animationCache.get(msg_id).get() != null) {
			mSmile = animationCache.get(msg_id).get();
			if (Utils.debug) {
				Log.d(TAG, "这里复用了一个animationdrawable");
			}
			return mSmile;
		} else {
			mSmile = new AnimationDrawable();
			if (Utils.debug) {
				Log.d(TAG, "这里创建了一个animationdrawable");
			}
			if (helperCache.containsKey(msg_id)
					&& helperCache.get(msg_id).get() != null) {
				if (Utils.debug) {
					Log.e(TAG, "这里回收了一个gifhelper");
				}
				gifOpenHelper helper = helperCache.get(msg_id).get();
				helper.recycle();
				helper = null;
				helperCache.remove(msg_id);
			}
		}
		gifOpenHelper gHelper = null;
		gHelper = new gifOpenHelper();
		InputStream is = null;
		try {
			is = new FileInputStream(gifName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		gHelper.read(is);
		if (gHelper.getImage() != null) {
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

		} else {
			Bitmap bitmap = BitmapFactory.decodeFile(gifName);
			BitmapDrawable bd = new BitmapDrawable(bitmap);
			mSmile.addFrame(bd, 0);
			mSmile.setBounds(0, 0, bd.getIntrinsicWidth(),
					bd.getIntrinsicHeight());
			mSmile.setOneShot(false);

			bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
		}
		animationCache
				.put(msg_id, new SoftReference<AnimationDrawable>(mSmile));
		helperCache.put(msg_id, new SoftReference<gifOpenHelper>(gHelper));
		// helpList.add(gHelper);
		// addHelper(gHelper);
		return mSmile;
	}

	public AnimationDrawable getAnimationDrawable2(Long msg_id, String emomd5,
			String fullname) {
		AnimationDrawable mSmile = null;
		if (animationCache.containsKey(msg_id)
				&& animationCache.get(msg_id).get() != null) {
			mSmile = animationCache.get(msg_id).get();
			if (Utils.debug) {
				Log.d(TAG, "这里复用了一个animationdrawable");
			}
			return mSmile;
		} else {
			mSmile = new AnimationDrawable();
			if (Utils.debug) {
				Log.d(TAG, "这里创建了一个animationdrawable");
			}
			if (helperCache.containsKey(msg_id)
					&& helperCache.get(msg_id).get() != null) {
				if (Utils.debug) {
					Log.e(TAG, "这里回收了一个gifhelper");
				}
				gifOpenHelper helper = helperCache.get(msg_id).get();
				helper.recycle();
				helper = null;
				helperCache.remove(msg_id);
			}
		}
		gifOpenHelper gHelper = null;
		gHelper = new gifOpenHelper();
		InputStream is = null;
		try {
			is = new FileInputStream(fullname);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		gHelper.read(is);
		if (gHelper.getImage() != null) {
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

		} else {
			Bitmap bitmap = BitmapFactory.decodeFile(fullname);
			BitmapDrawable bd = new BitmapDrawable(bitmap);
			mSmile.addFrame(bd, 0);
			mSmile.setBounds(0, 0, bd.getIntrinsicWidth(),
					bd.getIntrinsicHeight());
			mSmile.setOneShot(false);

			bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
		}
		animationCache
				.put(msg_id, new SoftReference<AnimationDrawable>(mSmile));
		helperCache.put(msg_id, new SoftReference<gifOpenHelper>(gHelper));
		return mSmile;
	}

	public void getSingleGif(final int i, final String path) {
		Runnable runnable = new Runnable() {
			public void run() {
				gifName = localName + "/" + "index.gif";
				NativeFace.getFaceEntryStream(localName + "/" + path, i,
						new IParseFaceObserver() {

							@Override
							public int onParseEvent(int event, String name,
									byte[] data) {
								if (3 == event) {
									try {
										mFile = new RandomAccessFile(gifName,
												"rw");
									} catch (FileNotFoundException e) {
										e.printStackTrace();
									}
								} else if (4 == event) {
									try {
										mFile.write(data);
									} catch (IOException e) {
										e.printStackTrace();
									}
								} else if (5 == event) {
									try {
										mFile.close();
										InputStream is = null;
										is = new FileInputStream(gifName);
										gifOpenHelper gHelper = new gifOpenHelper();
										final AnimationDrawable mSmile = new AnimationDrawable();
										gHelper.read(is);
										BitmapDrawable bd = new BitmapDrawable(
												gHelper.getImage());
										mSmile.addFrame(bd, gHelper.getDelay(0));
										for (int i = 1; i < gHelper
												.getFrameCount(); i++) {
											mSmile.addFrame(new BitmapDrawable(
													gHelper.nextBitmap()),
													gHelper.getDelay(i));
										}
										mSmile.setBounds(0, 0,
												bd.getIntrinsicWidth(),
												bd.getIntrinsicHeight());
										mSmile.setOneShot(false);

										bd.setBounds(0, 0,
												bd.getIntrinsicWidth(),
												bd.getIntrinsicHeight());
										// handler.post(new Runnable() {
										//
										// @Override
										// public void run() {
										// imageView
										// .setImageDrawable(mSmile);
										// mSmile.start();
										// }
										// });
										// Message msg =
										// handler.obtainMessage();
										// msg.what = DECODE_GIF_SINGLE;
										// msg.obj = mSmile;
										// handler.sendMessage(msg);

									} catch (IOException e) {
										e.printStackTrace();
									}
								}
								return 0;
							}
						});

			};
		};
		threadPools.execute(runnable);

	}

	// 记录gif位置的值
	int i = 0;

	/**
	 * 保存所有的缩略图到gif文件夹下，保存的格式为包md5值_包id_gif位置_gif的md5值.jpg
	 */
	public void saveGifThumbnailToSDCard(final String package_path,
			final String save_path, final String package_md5,
			final int package_id, final boolean isDownload) {
		nameList.clear();
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				// NativeFace.getFaceIntro(package_path, new
				// IParseFaceObserver() {
				//
				// @Override
				// public int onParseEvent(int event, String name, byte[] data)
				// {
				// if (event == 1) {
				// gif_bag_name = name;
				// } else if (event == 2) {
				// if (name != null) {
				// nameList.add(name);
				// }
				// String cur_thumb_name = FileManager
				// .getGifThumbnailNameFromSD(package_md5,
				// package_id, i);
				// if (Utils.debug) {
				// Log.i("shenliao", cur_thumb_name);
				// }
				// File f = new File(save_path + cur_thumb_name);
				// try {
				// RandomAccessFile raf = new RandomAccessFile(f,
				// "rw");
				// raf.write(data);
				// i++;
				// raf.close();
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
				// }
				// return 0;
				// }
				// });
				// i = 0;
				analysisFirstGifBitmap(package_path, save_path, package_md5,
						package_id, isDownload);

			}
		};
		threadPools.execute(runnable);
	}

	/**
	 * 解析得到表情包的代表图片
	 */
	int k = 0;
	HashMap<String, SoftReference<Bitmap>> cache = new HashMap<String, SoftReference<Bitmap>>();

	public void getRepresendThumb(final String package_path,
			final String package_md5, final int package_id,
			final Handler handler) {
		cache.clear();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				NativeFace.getFaceIntro(package_path, new IParseFaceObserver() {

					@Override
					public int onParseEvent(int event, String name, byte[] data) {
						if (event == 1) {

						} else if (event == 2) {
							String cur_thumb_name = FileManager
									.getGifThumbnailName(package_md5,
											package_id, k);
							if (Utils.debug) {
								Log.i("shenliao", cur_thumb_name);
							}
							Bitmap bitmap = BitmapFactory.decodeByteArray(data,
									0, data.length);
							SoftReference<Bitmap> soft = new SoftReference<Bitmap>(
									bitmap);
							cache.put(cur_thumb_name, soft);
							k++;

						}
						return 0;
					}

				});
				k = 0;
				handler.obtainMessage(DECODE_GIF_REPRESEND_THUMB, cache)
						.sendToTarget();

			}
		};
		threadPools.execute(runnable);
	}

	public interface GifDecodeListener {
		void onFinish(AnimationDrawable amd);
	}

	public void recycleHelper() {
		animationCache.clear();
		if (Utils.debug) {
			Log.e(TAG, "helperCache的大小" + helperCache.size());
		}
		Iterator i = helperCache.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry entry = (Entry) i.next();
			SoftReference<gifOpenHelper> sf = (SoftReference<gifOpenHelper>) entry
					.getValue();
			gifOpenHelper helper = sf.get();
			if (helper != null) {
				helper.recycle();
			}
		}
		helperCache.clear();
	}

	public void addHelper(gifOpenHelper h) {
		// if (helpList.size() > 10) {
		// gifOpenHelper helper = helpList.get(0);
		// if (helper != null) {
		// helper.recycle();
		// helpList.remove(helper);
		// if (Utils.debug) {
		// Log.e(TAG, "2015/3/23 : 回收了一个ghelper");
		// }
		// helper = null;
		// }
		// }
		helpList.add(h);
		if (Utils.debug) {
			Log.d(TAG, "2015/3/23 : 新添加了一个helper size :" + helpList.size());
		}

	}
}
