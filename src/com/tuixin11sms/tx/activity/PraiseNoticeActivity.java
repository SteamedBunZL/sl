package com.tuixin11sms.tx.activity;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shenliao.user.activity.UserInformationActivity;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.activity.explorer.ThumbnailCreator;
import com.tuixin11sms.tx.audio.manager.ClientManager;
import com.tuixin11sms.tx.callbacks.RecordListener;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.dao.impl.PraiseNoticeImpl.IPraiseNoticeUpdate;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.message.PraiseNotice;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.utils.AsyncCallback;
import com.tuixin11sms.tx.utils.MessageUtil;
import com.tuixin11sms.tx.utils.MusicUtils;
import com.tuixin11sms.tx.utils.Utils;
import com.tuixin11sms.tx.view.PlayAdiouAnimation;
import com.tuixin11sms.tx.view.WaitAdiouAnimation;
import com.umeng.analytics.MobclickAgent;

/**
 * 消息被赞activity
 * 
 */
public class PraiseNoticeActivity extends BaseActivity {
	protected static final String TAG = "PraiseNoticeActivity";
	private ListView listView;
	private IPraiseNoticeUpdate mPraiseNoticesObserver;
	private PraiseNoticeAdapter adapter;
	private List<PraiseNotice> mPnList;// 所有被赞消息列表
	private ClientManager playManager;
	private MusicUtils musicUtils;
	private Display display = null;
	private SmileyParser smileyParser;
	private ViewGroup ll_loading_notices;// 加载中动画

	private ThumbnailCreator mThumbnail;

	private static final int FLUSH_ONE_LINE = 0x100;
	private static final int FLUSH_ROOM_FALSE = 0x101;
	// private static final int FLUSH_ADD_MSGS = 0x102;

	private final int PLAY_ADIOU_C0MPELET = 423;
	private static final int RECORD_PLAY = 1;
	private static final int RECORD_PAUSE = 0;

	private Handler msgHandler = new WrappedHandler(thisContext) {

		public void handleMessage(Message msg) {

			switch (msg.what) {
			case PLAY_ADIOU_C0MPELET:
				if (mPnList != null) {
					for (PraiseNotice pn : mPnList) {
						TXMessage temptxmsg1 = pn.getTxmsg();
						temptxmsg1.PlayAudio = RECORD_PAUSE;
					}
				}
				musicUtils.PlaySound(3, 1, 2);
				flushOneLine(msg.obj);
				break;
			case FLUSH_ROOM_FALSE:
				// 全部adapter刷新
				adapter.notifyDataSetChanged();
				ll_loading_notices.setVisibility(View.GONE);
				break;
			case FLUSH_ONE_LINE:
				// 单条刷新
				flushOneLine(msg.obj);
				break;
			// case FLUSH_ADD_MSGS:
			// // 添加了数据，刷新
			// if (Utils.debug) {
			// showToast("添加了数据，刷新显示");
			// }
			// adapter.notifyDataSetChanged();
			// break;
			}
			super.handleMessage(msg);
		}
	};

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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_praise_messages);

		View v_title_back = findViewById(R.id.tv_title_back);
		v_title_back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				thisContext.finish();
			}
		});

		mCurrentActivity = this;

		playManager = ClientManager.getPlayManager();
		display = getWindowManager().getDefaultDisplay();
		smileyParser = new SmileyParser(mSess.getContext());

		musicUtils = new MusicUtils();
		musicUtils.CreateSoundpool(mSess.getContext());
		musicUtils.LoadSound(mSess.getContext(), 1, R.raw.begin_record, 0);
		musicUtils.LoadSound(mSess.getContext(), 2, R.raw.finish_record, 1);
		musicUtils.LoadSound(mSess.getContext(), 3, R.raw.play_finish, 2);
		musicUtils.LoadSound(mSess.getContext(), 4, R.raw.send_sucess, 3);
		init();
	}

	private void init() {
		listView = (ListView) findViewById(R.id.lv_praise_messages);

		mPraiseNoticesObserver = new IPraiseNoticeUpdate() {

			@Override
			public void update() {
				// 更新adapter
				mPnList = mSess.getPraiseNoticeDao().getNoticeList();
				if (mPnList.size() > 0) {
					msgHandler.sendEmptyMessage(FLUSH_ROOM_FALSE);
				} else {
					// 加载完毕，没有取到数据，隐藏加载中进度条
					ll_loading_notices.setVisibility(View.GONE);
				}

			}
		};

		mSess.getPraiseNoticeDao().registObserver(mPraiseNoticesObserver);
		mSess.getPraiseNoticeDao().onEnterNoticePage();

		adapter = new PraiseNoticeAdapter();
		listView.setAdapter(adapter);

		listView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					// 停止滚动时
					if (adapter.getCount() > 0
							&& listView.getLastVisiblePosition() + 1 == adapter
									.getCount()) {
						// 最后一条已经显示
						if (!mSess.getPraiseNoticeDao().isEndOfList()) {
							mSess.getPraiseNoticeDao().getNoticesFromServer();
							ll_loading_notices.setVisibility(View.VISIBLE);
						} else {
							ll_loading_notices.setVisibility(View.GONE);
						}
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub

			}

		});

		ll_loading_notices = (ViewGroup) findViewById(R.id.ll_loading_notices);
		ll_loading_notices.setVisibility(View.VISIBLE);// 刚进来先显示加载中进度条
	}

	// 刷新单条adapter
	private void flushOneLine(Object obj) {
		if (obj != null && (obj instanceof TXMessage)) {
			TXMessage txmsg = (TXMessage) obj;
			adapter.updateView(txmsg);
		}

	}

	/** 单条刷新 */
	public void flush(TXMessage txmsg) {
		msgHandler.obtainMessage(FLUSH_ONE_LINE, txmsg).sendToTarget();
	}

	/**
	 * 播放音频
	 */
	public void playAudioRecord(TXMessage txmsg) {
		playManager.startToPlay(txmsg, recordListener);
		if (!txmsg.was_me) {
			txmsg.PlayAudio = RECORD_PLAY;
			txmsg.read_state = TXMessage.READ;
			Utils.saveTxMsgToDB(txmsg);
		}
	}

	RecordListener recordListener = new RecordListener() {

		@Override
		public void uploadFinish(TXMessage txMsg) {
		}

		@Override
		public void onPlayFinish(TXMessage txMsg) {
			playManager.stopPlay();
			Message message = new Message();// 生成消息，并赋予ID值
			message.what = PLAY_ADIOU_C0MPELET;
			message.obj = txMsg;
			msgHandler.sendMessage(message);// 投递消息
		}

		@Override
		public void doRecordVolume(float volume) {

		}

		@Override
		public void deviceInitOK() {
		}

		@Override
		public void recordError(int errcode) {

		}
	};

	// 停止播放录音
	public void stopPlayAudioRecord() {
		if (playManager != null) {
			playManager.stopPlay();
		}
	}

	private void flush(int what) {
		msgHandler.sendEmptyMessage(what);

	}

	public class IntentSpan extends ClickableSpan {

		private final OnClickListener listener;

		public IntentSpan(View.OnClickListener listener) {
			this.listener = listener;
		}

		@Override
		public void onClick(View view) {
			listener.onClick(view);
		}
	}

	protected static PraiseNoticeActivity mCurrentActivity = null;// 当前activity对象

	public class PraiseNoticeAdapter extends BaseAdapter {

		LinkedList<PraiseHolder> mViewHolderList = new LinkedList<PraiseHolder>();

		@Override
		public int getCount() {
			return mPnList == null ? 0 : mPnList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			final PraiseHolder viewHolder;
			if (convertView == null) {
				viewHolder = new PraiseHolder();
				mViewHolderList.add(viewHolder);
				convertView = View.inflate(thisContext,
						R.layout.sll_item_praise_message, null);
				viewHolder.ll_head_imgs = (LinearLayout) convertView
						.findViewById(R.id.ll_head_imgs);
				viewHolder.ll_praised_msg = (LinearLayout) convertView
						.findViewById(R.id.ll_praised_msg);
				viewHolder.ll_praise_msg_infor = (ViewGroup) convertView
						.findViewById(R.id.ll_praise_msg_infor);
				viewHolder.failImg = (ImageView) convertView
						.findViewById(R.id.iv_msg_failImg);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (PraiseHolder) convertView.getTag();
			}

			if (mThumbnail == null)
				mThumbnail = new ThumbnailCreator();

			PraiseNotice txMsg = mPnList.get(position);
			viewHolder.pn = txMsg;

			updateView(viewHolder);

			return convertView;
		}

		protected void updateView(final PraiseHolder viewHolder) {

			final PraiseNotice pNotice = viewHolder.pn;

			TextView tv_praise_count = (TextView) viewHolder.ll_head_imgs
					.findViewById(R.id.tv_praise_count);

			String praiseCount = "等" + pNotice.getUidSet().size() + "人赞了";
			SpannableString msp = new SpannableString(praiseCount);
			msp.setSpan(new AbsoluteSizeSpan(20, true), 1,
					praiseCount.length() - 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

			tv_praise_count.setText(msp);

			// 加载赞过此消息的人的头像
			loadPraiseHeadIcon(viewHolder.ll_head_imgs, pNotice.getUidSet());

			TextView tv_notice_time = (TextView) viewHolder.ll_praise_msg_infor
					.findViewById(R.id.tv_notice_time);
			tv_notice_time.setText(Utils.formatTimeStr(pNotice.getTime()));
			TextView tv_msgroom_name = (TextView) viewHolder.ll_praise_msg_infor
					.findViewById(R.id.tv_msgroom_name);
			TxGroup txGroup = TxGroup.getTxGroup(mSess.getContentResolver(),
					pNotice.getGroupId());
			if (txGroup != null) {
				tv_msgroom_name.setText("来自   "
						+ smileyParser.addSmileySpans(txGroup.group_title,
								true, 0));
			} else {
				tv_msgroom_name.setText("来自未知聊天室");
			}
			View iv_forward = viewHolder.ll_praise_msg_infor
					.findViewById(R.id.iv_forward);
			iv_forward.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					// 如果语音正在播放，则停止
					if (playManager.getPlayingMsg() != null) {
						stopPlayAudioRecord();
						flush(FLUSH_ROOM_FALSE);
					}

					if (Utils.debug) {
						showToast("跳转到瞬间发布页面");
					}
					TXMessage txmsg = pNotice.getTxmsg();
					if (txmsg != null) {
						Intent intent = new Intent(thisContext,
								AddMyBlogActivity.class);
						if (txmsg.msg_type == TxDB.MSG_TYPE_QU_IMAGE_EMS) {
							// 群图片消息
							intent.putExtra(AddMyBlogActivity.BLOGIMG_URL,
									txmsg.msg_url);
							intent.putExtra(AddMyBlogActivity.BLOGIMG_PATH,
									txmsg.msg_path);
						} else if (txmsg.msg_type == TxDB.MSG_TYPE_QU_AUDIO_EMS) {
							// 群语音消息
							intent.putExtra(AddMyBlogActivity.BLOGADU_URL,
									txmsg.msg_url);
							intent.putExtra(AddMyBlogActivity.BLOGADU_PATH,
									txmsg.msg_path);
							intent.putExtra(AddMyBlogActivity.BLOGADU_TIME,
									txmsg.audio_times);
						}
						startActivity(intent);
					} else {
						if (Utils.debug) {
							showToast("被赞消息中的txmsg为空,异常情况");
						}
					}

				}
			});

			viewHolder.failImg.setVisibility(View.GONE);

			// 没有下载附件的操作，只有获取显示
			final TXMessage txmsg = pNotice.getTxmsg();
			if (txmsg == null) {
				// TODO 消息为空该怎么办？
			} else {
				showView(viewHolder.ll_praised_msg, txmsg.msg_type);
				// 根据具体信息类型 进行显示
				switch (txmsg.msg_type) {
				case TxDB.MSG_TYPE_QU_IMAGE_EMS:
					View loadingView = (ProgressBar) viewHolder.ll_praised_msg
							.findViewById(R.id.msgroomitem_List2_loading);
					loadingView.setVisibility(View.GONE);
					viewHolder.failImg.setVisibility(View.GONE);
					final ImageView imgView = (ImageView) viewHolder.ll_praised_msg
							.findViewById(R.id.msgroomitem_List2_MsgImg);// 显示图片消息内容的ImageView
					Bitmap bitmap = null;
					if (txmsg.cacheImg != null) {// 从缓存读图片
						bitmap = txmsg.cacheImg.get();
						imgView.setImageBitmap(bitmap);
					}
					if (bitmap == null) {
						txmsg.updateState = TXMessage.UPDATING;
						Bitmap bitmap1 = Utils.getImgByPath(mSess.getContext(),
								txmsg.msg_path, Utils.msgroom_list_resolution);
						if (bitmap1 != null) {
							txmsg.cacheImg = new SoftReference<Bitmap>(bitmap1);
							txmsg.updateState = TXMessage.UPDATE_OK;
							flush(FLUSH_ROOM_FALSE);
						}
					}

					imgView.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (!Utils.checkSDCard()) {// 无SD卡
								Utils.creatNoSdCardInfo(mSess.getContext())
										.show();
								return;
							}
							Intent picIntent = new Intent();
							picIntent.setClass(thisContext, EditSendImg.class);
							picIntent.putExtra(EditSendImg.TXMESSAGE, txmsg);
							picIntent.putExtra(EditSendImg.USER_ID,
									TX.tm.getTxMe().partner_id);
							startActivity(picIntent);
						}

					});
					break;
				case TxDB.MSG_TYPE_QU_AUDIO_EMS:
					final WaitAdiouAnimation waitAudioAnim = (WaitAdiouAnimation) viewHolder.ll_praised_msg
							.findViewById(R.id.v3_audio_anti);
					PlayAdiouAnimation playAudioAnim = (PlayAdiouAnimation) viewHolder.ll_praised_msg
							.findViewById(R.id.msgroomitem_List3_PlayRecordImg);
					waitAudioAnim
							.setBackgroundResource(R.drawable.msg_bg_other);
					playAudioAnim.setVisibility(View.VISIBLE);

					if (txmsg != playManager.getPlayingMsg()) {
						playAudioAnim.stopAdiouPlayAn(false);
					} else {
						playAudioAnim.startAdiouPlayAn(false);
					}
					waitAudioAnim.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (!Utils.checkSDCard()) {// 无SD卡
								Utils.creatNoSdCardInfo(mSess.getContext())
										.show();
								return;
							}
							if (!Utils.isNull(txmsg.msg_path)
									&& txmsg.updateState != TXMessage.UPDATING
									&& txmsg.updateState != TXMessage.UPDATE_FAILE) {

								// 播放音频
								if (txmsg != playManager.getPlayingMsg()) {
									playManager.stopPlay();
									playAudioRecord(txmsg);
								} else {
									stopPlayAudioRecord();
								}
							}
							flush(FLUSH_ROOM_FALSE);
						}
					});

					TextView voiceTime = (TextView) viewHolder.ll_praised_msg
							.findViewById(R.id.tv_audio_msg_recordTime);

					voiceTime.setText(MessageUtil
							.getRecordTime(txmsg.audio_times));

					// 更新时长
					// if (txmsg.updateState != TXMessage.UPDATE
					// && txmsg.updateState != TXMessage.UPDATING) {
					// long timeWidth = 80 + txmsg.audio_times * 10;
					// int jianWidth = 170 * display.getWidth() / 480;
					// if (timeWidth > display.getWidth() - jianWidth) {
					// timeWidth = display.getWidth() - jianWidth;
					// }
					// RelativeLayout.LayoutParams lp = new
					// RelativeLayout.LayoutParams(
					// (int) (timeWidth),
					// RelativeLayout.LayoutParams.WRAP_CONTENT);
					// waitAudioAnim.setLayoutParams(lp);
					// } else {
					// RelativeLayout.LayoutParams lp = new
					// RelativeLayout.LayoutParams(
					// RelativeLayout.LayoutParams.WRAP_CONTENT,
					// RelativeLayout.LayoutParams.WRAP_CONTENT);
					// waitAudioAnim.setLayoutParams(lp);
					// }

					// 注释掉上面的更新时长和音频条目长度的方法 2014.05.28 shc
					if (txmsg.updateState != TXMessage.UPDATE
							&& txmsg.updateState != TXMessage.UPDATING) {

						long timeWidth = 30 + txmsg.audio_times * 10;
						int jianWidth = 170 * display.getWidth() / 480;
						if (timeWidth > display.getWidth() - jianWidth) {
							timeWidth = display.getWidth() - jianWidth;
						}

						voiceTime.setWidth((int) timeWidth);

					}

					break;
				}
			}
		}

		/** 加载赞过此消息的人的头像 */
		private void loadPraiseHeadIcon(ViewGroup ll_head_imgs,
				LinkedHashSet<Long> uidSet) {
			ImageView iv_head_0 = (ImageView) ll_head_imgs
					.findViewById(R.id.iv_head_img_0);
			ImageView iv_head_1 = (ImageView) ll_head_imgs
					.findViewById(R.id.iv_head_img_1);
			ImageView iv_head_2 = (ImageView) ll_head_imgs
					.findViewById(R.id.iv_head_img_2);
			ImageView iv_head_3 = (ImageView) ll_head_imgs
					.findViewById(R.id.iv_head_img_3);
			ImageView iv_head_4 = (ImageView) ll_head_imgs
					.findViewById(R.id.iv_head_img_4);
			ImageView iv_head_5 = (ImageView) ll_head_imgs
					.findViewById(R.id.iv_head_img_5);
			iv_head_0.setVisibility(View.INVISIBLE);
			iv_head_1.setVisibility(View.INVISIBLE);
			iv_head_2.setVisibility(View.INVISIBLE);
			iv_head_3.setVisibility(View.INVISIBLE);
			iv_head_4.setVisibility(View.INVISIBLE);
			iv_head_5.setVisibility(View.INVISIBLE);

			ImageView[] ivs = new ImageView[] { iv_head_0, iv_head_1,
					iv_head_2, iv_head_3, iv_head_4, iv_head_5 };

			if (uidSet != null) {
				Iterator<Long> iter = uidSet.iterator();
				int i = 0;
				while (iter.hasNext()) {
					final long uid = iter.next();
					final ImageView iv = ivs[i];
					iv.setTag(uid);
					Bitmap bmHead = mSess.avatarDownload.getHeadIcon(uid,
							new AsyncCallback<Bitmap>() {

								@Override
								public void onFailure(Throwable t, long id) {

								}

								@Override
								public void onSuccess(Bitmap result, long id) {
									if (((Long) iv.getTag()) == id
											&& result != null) {
										iv.setBackgroundDrawable(new BitmapDrawable(
												result));
									}
								}
							});
					if (bmHead != null) {
						iv.setBackgroundDrawable(new BitmapDrawable(bmHead));
					} else {
						iv.setBackgroundResource(R.drawable.user_infor_head_girl);
					}
					iv.setVisibility(View.VISIBLE);
					iv.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// 点击进入个人资料页面
							Intent intent = new Intent(thisContext,
									UserInformationActivity.class);
							intent.putExtra(UserInformationActivity.UID, uid);
							startActivity(intent);
						}
					});
					i++;
					if (i > 5) {
						// 当超过6个人的时候，就结束遍历显示
						break;
					}
				}
			} else {
				// 这种情况应该没有
				ll_head_imgs.setVisibility(View.INVISIBLE);
			}

		}

		/** 单条刷新，通过【消息id】判定holder(因为退出聊天室再进入消息对象都是重新创建的),然后从新设置该holder的显示view */
		public boolean updateView(TXMessage txmsg) {
			for (PraiseHolder hldr : mViewHolderList) {
				// if(Utils.debug)Log.d(TAG,"需要更新的txmsg.msg_id="+txmsg.msg_id+",hldr.txmsg.msg_id="+hldr.txmsg.msg_id);
				TXMessage txMsg = hldr.pn.getTxmsg();
				if (txMsg != null) {
					if (txMsg.msg_id == txmsg.msg_id) {
						if (Utils.debug)
							Log.e(TAG, "msg_id相等：" + txmsg.msg_id + ",文件本地路径："
									+ txmsg.msg_path + ",更新view");
						txMsg.updateCount = txmsg.updateCount;// 把count值赋给当前对象的txmsg
						txMsg.updateState = txmsg.updateState;
						txMsg.msg_url = txmsg.msg_url;
						txMsg.read_state = txmsg.read_state;
						updateView(hldr);
						return true;
					}
				}
			}
			return false;
		}

		private void showView(ViewGroup viewGroup, int msgType) {
			ViewGroup vg_imgMsgContent = (ViewGroup) viewGroup
					.findViewById(R.id.v2_img);
			ViewGroup vg_audioMsgContent = (ViewGroup) viewGroup
					.findViewById(R.id.v3_audio);

			vg_imgMsgContent.setVisibility(View.GONE);
			vg_audioMsgContent.setVisibility(View.GONE);
			switch (msgType) {
			case TxDB.MSG_TYPE_QU_IMAGE_EMS:
				vg_imgMsgContent.setVisibility(View.VISIBLE);
				break;
			case TxDB.MSG_TYPE_QU_AUDIO_EMS:
				vg_audioMsgContent.setVisibility(View.VISIBLE);
				break;
			}
		}

	}

	public class PraiseHolder {

		private ViewGroup ll_head_imgs;
		private ViewGroup ll_praised_msg;
		private ViewGroup ll_praise_msg_infor;
		private View failImg;

		private PraiseNotice pn;

	}

	@Override
	protected void onStop() {
		// 发送更新消息红点的广播
		mSess.getSocketHelper().sendNoReadMsg();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (mCurrentActivity == thisContext) {
			// 说明是自己销毁的自己，可以置空
			mCurrentActivity = null;
		}
		mSess.getPraiseNoticeDao().unregistObserver(mPraiseNoticesObserver);
		mSess.getPraiseNoticeDao().onExitNoticePage();
		super.onDestroy();
	}

}
