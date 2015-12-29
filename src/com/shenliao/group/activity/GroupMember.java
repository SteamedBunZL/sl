package com.shenliao.group.activity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

//import net.youmi.android.banner.AdSize;
//import net.youmi.android.banner.AdView;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.data.DataContainer;
import com.shenliao.group.util.GroupUtils;
import com.shenliao.set.activity.SetUserInfoActivity;
import com.shenliao.user.activity.UserInformationActivity;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.activity.SelectFriendListActivity;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.MsgHelper;
import com.tuixin11sms.tx.core.MsgInfor;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.download.AvatarDownload;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.AsyncCallback;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.StringUtils;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 查看群成员或在线成员
 * 
 */
public class GroupMember extends BaseActivity implements View.OnClickListener {
	private static final String TAG = "GroupMember";
	public static final String VIEW_STATE = "c";
	public static final int DISPLAY_MEMBER = 1;// 显示群成员或者在线成员
	public static final int DISPLAY_BLACK = 2;// 显示黑名单
	// public static final int FROM_MSGROOM = 3;//从聊天室跳转过来 //TODO
	// 抽时间去掉此字段，因为现在进入群成员只有聊天室一个入口 2014.03.07 shc
	public static final int DISPLAY_ONLINE = 4;// 显示在线成员
	private ListView mMemberListView;
	private ListView mBlackListView;
	private List<TX> membersList = new ArrayList<TX>();
	private List<TX> blackList = new ArrayList<TX>();
	// private Button mAddMember;
	private View mAdminLayout;
	private MembersAdapter membersAdapter;
	private BlacklistAdapter blackListAdapter;
	private View mLoading;
	private int groupType;
	private List<String> auths = new ArrayList<String>();

	private TextView mMember;
	private TextView mBlackList;

	private UpdateReceiver updatareceiver;
	private int currentView = 1;// 标记当前页面是显示在线人员，群成员(currentView = 3)，还是黑名单的？

	private long groupId;
	private TxGroup txGroup;
	private LayoutInflater mInflater;
	private Set<Long> adminsList;
	private Set<Long> banList;
	private ArrayList<Long> totalList = new ArrayList<Long>(); // 包括管理员和群成员
	private ArrayList<String> blackIdsList = new ArrayList<String>();
	private SmileyParser smileyParser;
	// private int defaltHeaderImg;
	// private int defaltHeaderImgMan;
	// private int defaltHeaderImgFemale;

	public static final String CURRENT_PAGE_NUM = "currentPageNum";// 当前成员列表的页数
	public static final String END_PAGE_NUM = "endPageCount";// 末尾成员列表的页数

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// prepairAsyncload();
		TxData.addActivity(this);
		setContentView(R.layout.activity_group_members);
		init();
	}

	private void init() {
		// defaltHeaderImgMan = R.drawable.tui_con_photo;
		// defaltHeaderImgFemale = R.drawable.sl_regist_head_femal;
		smileyParser = new SmileyParser(this);
		auths.clear();
		Intent intent = getIntent();
		if (intent != null) {
			groupId = intent.getLongExtra(Utils.MSGROOM_GROUP_ID, -1);
			currentView = intent.getIntExtra(VIEW_STATE, DISPLAY_MEMBER);
			if (groupId == -1) {
				finish();
				return;
			}
			showDialogTimer(GroupMember.this, R.string.prompt,
					R.string.group_getting_member, 15 * 1000,
					new BaseTimerTask() {

						@Override
						public void run() {
							super.run();
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									AlertDialog.Builder promptDialog = new AlertDialog.Builder(
											GroupMember.this);
									promptDialog.setTitle(R.string.prompt);
									promptDialog.setMessage("获取聊天室成员信息超时，请重试");
									promptDialog.setCancelable(false);
									promptDialog
											.setNegativeButton(
													R.string.confirm,
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(
																DialogInterface dialog,
																int which) {
															dialog.cancel();
															GroupMember.this
																	.finish();
														}
													});
									promptDialog.show();

								}
							});
						}
					}).show();
			mSess.getSocketHelper().sendGetGroup(groupId);
		}
		mInflater = LayoutInflater.from(this);
		mMemberListView = (ListView) findViewById(R.id.group_member_list);
		mBlackListView = (ListView) findViewById(R.id.group_black_list);
		mAdminLayout = findViewById(R.id.rl_group_switch_tab_title);
		mLoading = findViewById(R.id.group_loading);
		mMember = (TextView) findViewById(R.id.group_member);
		mBlackList = (TextView) findViewById(R.id.group_black);

		mMember.setOnClickListener(this);
		mBlackList.setOnClickListener(this);

		mMemberListView.setOnScrollListener(new ScrollList());
		mBlackListView.setOnScrollListener(new ScrollList());

	}

	class MembersAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context context;
		private List<TX> membersList = new ArrayList<TX>();
		public int currposition = 10;

		public MembersAdapter(Context context) {
			this.context = context;
			mInflater = LayoutInflater.from(context);
		}

		public void setData(List<TX> membersList) {
			this.membersList = membersList;
		}

		@Override
		public int getCount() {
			// if (currentView == DISPLAY_ONLINE) {
			// if (this.membersList.size() >= 0
			// && this.membersList.size() <= 10) {
			return this.membersList.size();
			// } else {
			// return this.membersList.size() + (currposition - 10) / 11
			// + 1;
			// }
			// } else {
			// return this.membersList.size();
			// }

		}

		// @Override
		// public int getViewTypeCount() {
		// if (currentView == DISPLAY_ONLINE) {
		// return 2;
		// } else {
		// return 1;
		// }
		// }

		// @Override
		// public int getItemViewType(int position) {
		// if (currentView == DISPLAY_ONLINE) {
		// if (getCount() < 10 && position == getCount() - 1) {
		// return Constants.ADD_VIEW;
		// }else {
		// if ((position - 10) % 11 == 0) {
		// return Constants.ADD_VIEW;
		// } else {
		// return Constants.NORMAL_VIEW;
		// }
		// }
		// } else {
		// return Constants.NORMAL_VIEW;
		// }
		//
		// }

		// public int getNewPosition(int position) {
		// int n = (currposition - 10) / 11;
		// if (position - currposition < 0) {
		// return position - n;
		// } else {
		// return position - (n + 1);
		// }
		// }

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TXHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.sll_group_members_item, null);
				holder = new TXHolder();
				holder.nickName = (TextView) convertView
						.findViewById(R.id.member_nick);
				holder.sex = (ImageView) convertView
						.findViewById(R.id.iv_online_memeber_sex);
				holder.area = (TextView) convertView
						.findViewById(R.id.tv_online_memeber_area);
				holder.introl = (TextView) convertView
						.findViewById(R.id.member_introl);
				// holder.gender = (ImageView) convertView
				// .findViewById(R.id.member_type);
				holder.avatar = (ImageView) convertView
						.findViewById(R.id.member_head);
				holder.level = (TextView) convertView
						.findViewById(R.id.tv_level);

				convertView.setTag(holder);
			} else {
				holder = (TXHolder) convertView.getTag();
			}

			final TX tx = membersList.get(position);
			holder.tx = tx;
			TX ttx = TX.tm.getTx(tx.partner_id);
			if (tx != null && ttx != null && ttx.getRemarkName() != null) {
				tx.setRemarkName(ttx.getRemarkName());
			}
			if (!Utils.isNull(tx.getRemarkName())) {
				holder.nickName.setText(smileyParser.addSmileySpans(
						tx.getRemarkName(), true, 0));
			} else {
				if (!Utils.isNull(tx.getNick_name())) {
					holder.nickName.setText(smileyParser.addSmileySpans(
							tx.getNick_name(), true, 0));
				} else {
					holder.nickName.setText(smileyParser.addSmileySpans(tx
							.getTxInfor().getContacts_person_name(), true, 0));
				}
			}

			// holder.sex.setText(tx.sex == 0? "男":"女");
			// holder.area.setText(tx.area);

			if (tx.sign != null && !"".equals(tx.sign)) {
				holder.introl.setText(smileyParser.addSmileySpans(tx.sign,
						true, 0));
			} else {
				holder.introl.setText("");// 没有个性签名，则置空
			}
			if (tx.isDispalyLevel()) {
				holder.level.setVisibility(View.VISIBLE);
				holder.level.setText(getString(R.string.level) + tx.getLevel());
			} else {
				holder.level.setVisibility(View.INVISIBLE);
			}
			if (currentView == DISPLAY_ONLINE) {
				// 查看在线列表时显示性别和地址
				holder.sex.setVisibility(View.VISIBLE);
				if (tx.getSex() == TX.MALE_SEX) {
					holder.sex.setImageResource(R.drawable.online_sex_boy);
					// defaultHeaderImg=defaultHeaderImgMan;
				} else {
					holder.sex.setImageResource(R.drawable.online_sex_girl);
					// defaultHeaderImg=defaultHeaderImgFemale;
				}

				if (!TextUtils.isEmpty(ttx.area)) {
					holder.area.setVisibility(View.VISIBLE);
					List<String> mlist = StringUtils.str2List(ttx.area);
					String areaText = DataContainer.getAreaNameByIds(mlist
							.toArray(new String[0]));
					if (areaText.equals("不限")) {
						holder.area.setText("");
					} else {
						holder.area.setText(areaText);
					}
				}
			}

			loadMemberHeadImage(tx, holder.avatar);

			int tmpSign = GroupUtils.userDignity(tx.partner_id,
					txGroup.group_own_id, txGroup.group_tx_admin_ids);

			ImageView iv_member_title = ((ImageView) convertView
					.findViewById(R.id.group_member_image));

			switch (tmpSign) {
			case 0:
				iv_member_title.setVisibility(View.VISIBLE);
				iv_member_title.setImageResource(R.drawable.group_host);

				break;
			case 1:
				iv_member_title.setVisibility(View.VISIBLE);
				iv_member_title.setImageResource(R.drawable.group_admin);
				break;
			case 2:
				iv_member_title.setVisibility(View.GONE);
				break;
			default:
				break;
			}
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					toUserInfo(tx);
				}
			});

			convertView.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					if (tx.partner_id == TX.tm.getTxMe().partner_id)
						return false;
					handlerTX(tx, false);
					/*
					 * switch (meType) { case TxDB.QU_TX_STATE_OWN:
					 * createDialogCreator(tx); break; case TxDB.QU_TX_STATE_GM:
					 * createDialogAdmin(tx); break; default: break; }
					 */

					return false;
				}
			});

			return convertView;
		}

	}

	private void handlerTX(final TX tx, boolean isBlackTX) {
		// 设置各种权限
		if (txGroup != null) {
			auths = TxGroup.initAuth(txGroup);
			if (isBlackTX) {
				// 黑名单中的用户没有“踢出群”、“设置管理员”、“禁言”选项
				if (auths.contains("踢出群")) {
					auths.remove("踢出群");
				}
				if (auths.contains("设置管理员")) {
					auths.remove("设置管理员");
				}
				if (auths.contains("禁言")) {
					auths.remove("禁言");
				}
			}
		} else {
			return;
		}
		if (auths.size() == 0 || tx.partner_id == TX.tm.getUserID())
			return;
		boolean isAdmin = false;
		boolean isBan = false;
		boolean isBlack = false;
		// 操作对象是群主
		if (tx.partner_id == txGroup.group_own_id) {
			// 不是op
			// 管理员不能对群主进行任何操作
			if (!TxData.isOP()) {
				return;
			}

		}
		if (adminsList.contains(tx.partner_id)) {
			if (auths.contains("设置管理员")) {
				int index = auths.indexOf("设置管理员");
				auths.remove(index);
				auths.add(index, "取消管理员");
				isAdmin = true;
			}
		}

		if (banList.contains(tx.partner_id)) {
			if (auths.contains("禁言")) {
				int index = auths.indexOf("禁言");
				auths.remove(index);
				auths.add(index, "取消禁言");
				isBan = true;
			}

		}
		if (blackList.contains(tx)) {
			if (auths.contains("加入黑名单")) {
				int index = auths.indexOf("加入黑名单");
				auths.remove(index);
				auths.add(index, "移出黑名单");
				isBlack = true;
			}

		}
		String[] items = auths.toArray(new String[] {});

		final boolean isAdmin2 = isAdmin;
		final boolean isBan2 = isBan;
		final boolean isBlack2 = isBlack;

		new AlertDialog.Builder(GroupMember.this).setItems(items,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						String s = auths.get(which);
						if (s.contains("管理员")) {
							if (isAdmin2) {
								GroupUtils.showDialog(
										GroupMember.this,
										"取消管理员",
										"是否确认取消 "
												+ smileyParser.addSmileySpans(
														Utils.isNull(tx
																.getRemarkName()) ? tx
																.getNick_name()
																: tx.getRemarkName(),
														true, 0) + " 的管理员身份?",
										R.string.dialog_okbtn,
										R.string.dialog_nobtn,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												mSess.getSocketHelper()
														.sendSetGroupAdmin(
																txGroup.group_id,
																tx.partner_id,
																1);

											}
										});

							} else {
								GroupUtils.showDialog(
										GroupMember.this,
										"设置管理员",
										"是否确认将 "
												+ smileyParser.addSmileySpans(
														Utils.isNull(tx
																.getRemarkName()) ? tx
																.getNick_name()
																: tx.getRemarkName(),
														true, 0) + " 设置为管理员?",
										R.string.dialog_okbtn,
										R.string.dialog_nobtn,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												mSess.getSocketHelper()
														.sendSetGroupAdmin(
																txGroup.group_id,
																tx.partner_id,
																0);
											}
										});

							}
						} else if (s.contains("禁言")) {
							if (isBan2) {
								GroupUtils
										.showDialog(
												GroupMember.this,
												"警告",
												"是否取消 "
														+ smileyParser
																.addSmileySpans(
																		Utils.isNull(tx
																				.getRemarkName()) ? tx
																				.getNick_name()
																				: tx.getRemarkName(),
																		true, 0)
														+ " 的禁言处罚?",
												R.string.dialog_okbtn,
												R.string.dialog_nobtn,
												new DialogInterface.OnClickListener() {

													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														mSess.getSocketHelper()
																.sendShutupGroup(
																		txGroup.group_id,
																		tx.partner_id,
																		1, 0);
													}
												});

							} else {
								shutupOpt(tx);
							}
						} else if (s.equals("踢出群")) {
							final List<Long> id = new ArrayList<Long>();
							id.add(tx.partner_id);
							GroupUtils
									.showDialog(
											GroupMember.this,
											" 踢出群",
											"是否确定将 "
													+ smileyParser.addSmileySpans(
															Utils.isNull(tx
																	.getRemarkName()) ? tx
																	.getNick_name()
																	: tx.getRemarkName(),
															true, 0) + " 移除此群?",
											R.string.dialog_okbtn,
											R.string.dialog_nobtn,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													mSess.getSocketHelper()
															.sendDealGroup(
																	txGroup.group_id,
																	false, id);

												}
											});

						} else if (s.contains("黑名单")) {
							if (isBlack2) {
								GroupUtils.showDialog(
										GroupMember.this,
										"取消黑名单",
										"是否确定将 "
												+ smileyParser.addSmileySpans(
														Utils.isNull(tx
																.getRemarkName()) ? tx
																.getNick_name()
																: tx.getRemarkName(),
														true, 0) + "移除黑名单?",
										R.string.dialog_okbtn,
										R.string.dialog_nobtn,
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												mSess.getSocketHelper()
														.sendSetBlackInGroup(
																txGroup.group_id,
																tx.partner_id,
																1);

											}
										});

							} else {
								GroupUtils.showDialog(
										GroupMember.this,
										"加入黑名单",
										"是否确定将 "
												+ smileyParser.addSmileySpans(
														Utils.isNull(tx
																.getRemarkName()) ? tx
																.getNick_name()
																: tx.getRemarkName(),
														true, 0) + " 移至黑名单?",
										R.string.dialog_okbtn,
										R.string.dialog_nobtn,
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												mSess.getSocketHelper()
														.sendSetBlackInGroup(
																txGroup.group_id,
																tx.partner_id,
																0);
											}
										});

							}
						} else if (s.equals("警告")) {
							Intent i = new Intent(GroupMember.this,
									GroupWarn.class);
							i.putExtra("uid", tx.partner_id);
							startActivity(i);
						} else if (s.equals("封ID")) {
							GroupUtils
									.showDialog(
											GroupMember.this,
											"警告",
											"是否确定将 "
													+ smileyParser.addSmileySpans(
															Utils.isNull(tx
																	.getRemarkName()) ? tx
																	.getNick_name()
																	: tx.getRemarkName(),
															true, 0)
													+ " 处以封id的处罚? 该用户id将被永久封停!",
											R.string.dialog_okbtn,
											R.string.dialog_nobtn,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													mSess.getSocketHelper()
															.sendBlock(
																	tx.partner_id,
																	false);
												}
											});

						} else if (s.equals("封设备")) {
							GroupUtils.showDialog(
									GroupMember.this,
									"警告",
									"是否封锁 "
											+ smileyParser.addSmileySpans(
													Utils.isNull(tx
															.getRemarkName()) ? tx
															.getNick_name()
															: tx.getRemarkName(),
													true, 0)
											+ " 的设备?该用户设备将永久无法使用神聊!",
									R.string.dialog_okbtn,
									R.string.dialog_nobtn,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											mSess.getSocketHelper().sendBlock(
													tx.partner_id, true);
										}
									});

						}
					}
				}).show();
	}

	private void shutupOpt(final TX tx) {
		final String[] items = new String[] { "5分钟", "30分钟", "24小时", "永久" };
		new AlertDialog.Builder(GroupMember.this).setItems(items,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						int time = 5 * 60;
						String disTime = null;
						switch (which) {
						case 0:
							time = 5 * 60;
							disTime = items[0];
							break;
						case 1:
							time = 30 * 60;
							disTime = items[1];
							break;
						case 2:
							time = 24 * 60 * 60;
							disTime = items[2];
							break;
						case 3:
							time = 0;
							disTime = items[3];
							break;
						}
						final int t = time;
						GroupUtils.showDialog(
								GroupMember.this,
								"警告",
								"是否将 "
										+ smileyParser.addSmileySpans(
												Utils.isNull(tx.getRemarkName()) ? tx
														.getNick_name() : tx
														.getRemarkName(), true,
												0) + "(" + tx.partner_id + ")"
										+ " 处以 " + disTime + " 禁言的处罚?",
								R.string.dialog_okbtn, R.string.dialog_nobtn,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										mSess.getSocketHelper()
												.sendShutupGroup(
														txGroup.group_id,
														tx.partner_id, 0, t);

									}
								});

					}
				}).show();
	}

	private void toUserInfo(TX tx) {
		Intent intent = new Intent(GroupMember.this,
				UserInformationActivity.class);
		if (tx.partner_id == TX.tm.getTxMe().partner_id) {
			Intent myIntent = new Intent(GroupMember.this,
					SetUserInfoActivity.class);
			startActivity(myIntent);
		} else {
			intent.putExtra(UserInformationActivity.UID, tx.partner_id);
			startActivity(intent);
		}

	}

	private int lastPos;
	private int lastViewPosition;
	private boolean isLoadingNextPage = false;// 是否正在加载

	private class ScrollList implements OnScrollListener {

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			lastPos = view.getLastVisiblePosition();
			lastViewPosition = firstVisibleItem + visibleItemCount;
			int totalItemCount2 = totalItemCount;
			if (currentView == DISPLAY_ONLINE) {
				// totalCount2 = totalCount2 +
			}
			if (lastViewPosition == totalItemCount2 && totalItemCount2 > 0) {
				// lastViewPosition == totalItemCount说明滑到了底部
				// if (currentView == FROM_MSGROOM||currentView==DISPLAY_MEMBER)
				// {
				if (currentView == DISPLAY_ONLINE
						|| currentView == DISPLAY_MEMBER) {
					if (isLoadingNextPage) {
						// 正在加载的状态
						return;
					}
					if (!isGetOverMember && !getNextIds()) {

						mLoading.setVisibility(View.VISIBLE);
					} else {
						mLoading.setVisibility(View.GONE);
					}
				}
			}
			if (lastViewPosition < firstVisibleItem && totalItemCount2 > 0) {
				mLoading.setVisibility(View.GONE);
			}

		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			int tempTotal = 0;
			switch (currentView) {
			case 1:
				tempTotal = membersList.size() - 1;
			case 3:
				tempTotal = membersList.size() - 1;
				break;
			case 2:
				tempTotal = blackList.size() - 1;
				break;
			}
			// if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && lastPos
			// == tempTotal) {
			if (lastPos == tempTotal) {
				if (mLoading.getVisibility() == View.VISIBLE)
					return;
				if (currentView == DISPLAY_MEMBER) {
					// if (!isGetOverMember && !getNextIds()) {
					// mLoading.setVisibility(View.VISIBLE);
					// } else {
					// mLoading.setVisibility(View.GONE);
					// }
				} else if (currentView == DISPLAY_BLACK) {
					if (!isGetOverBlack && !getNextBlackList()) {
						mLoading.setVisibility(View.VISIBLE);
					} else {
						mLoading.setVisibility(View.GONE);
					}
					// } else if (currentView == FROM_MSGROOM) {
					// if (Utils.debug) {
					// Log.i("op", "--onScrollStateChanged-->"
					// + isLoadingNextPage);
					//
					// }
					// if (isLoadingNextPage) {
					// getNextIds();
					// mLoading.setVisibility(View.VISIBLE);
					// } else {
					// mLoading.setVisibility(View.GONE);
					// }
					// if (!isGetOverMember && !getNextIds()) {
					// mLoading.setVisibility(View.VISIBLE);
					// } else {
					// mLoading.setVisibility(View.GONE);
					// }
				}
			}
		}

	}

	class BlacklistAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context context;
		private List<TX> blackList = new ArrayList<TX>();

		public BlacklistAdapter(Context context) {
			this.context = context;
			mInflater = LayoutInflater.from(context);
		}

		public void setData(List<TX> blackList) {
			this.blackList = blackList;
		}

		@Override
		public int getCount() {
			return blackList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TXHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.sll_group_members_item, null);
				holder = new TXHolder();
				holder.nickName = (TextView) convertView
						.findViewById(R.id.member_nick);
				holder.introl = (TextView) convertView
						.findViewById(R.id.member_introl);
				holder.level = (TextView) convertView
						.findViewById(R.id.tv_level);

				// holder.sex =
				// (TextView)convertView.findViewById(R.id.member_gendertext);
				// holder.area =
				// (TextView)convertView.findViewById(R.id.member_area);
				holder.avatar = (ImageView) convertView
						.findViewById(R.id.member_head);
				convertView.setTag(holder);
			} else {
				holder = (TXHolder) convertView.getTag();
			}
			final TX tx = blackList.get(position);
			TX ttx = TX.tm.getTx(tx.partner_id);
			if (tx != null && ttx != null && ttx.getRemarkName() != null) {
				tx.setRemarkName(ttx.getRemarkName());
			}
			if (!Utils.isNull(tx.getRemarkName())) {
				holder.nickName.setText(smileyParser.addSmileySpans(
						tx.getRemarkName(), true, 0));
			} else {
				if (!Utils.isNull(tx.getNick_name())) {
					holder.nickName.setText(smileyParser.addSmileySpans(
							tx.getNick_name(), true, 0));
				} else {
					holder.nickName.setText(smileyParser.addSmileySpans(tx
							.getTxInfor().getContacts_person_name(), true, 0));
				}
			}
			// holder.sex.setText(tx.sex == 0? "男":"女");
			// if (tx.sex == TX.MALE_SEX) {
			// defaultHeaderImg=defaultHeaderImgMan;
			// holder.gender.setImageResource(R.drawable.ic_sex_male);
			//
			// } else {
			// defaultHeaderImg=defaultHeaderImgFemale;
			// holder.gender.setImageResource(R.drawable.ic_sex_female);
			//
			// }
			if (tx.sign != null && !"".equals(tx.sign)) {
				holder.introl.setText(tx.sign);
			} else {
				holder.introl.setText("");// 没有个性签名的给签名置空
			}

			if (tx.isDispalyLevel()) {
				holder.level.setVisibility(View.VISIBLE);
				holder.level.setText(getString(R.string.level) + tx.getLevel());
			} else {
				holder.level.setVisibility(View.INVISIBLE);
			}
			// if (tx. == 0 && tx.partner_id == CommonData.TUIXIN_MAN) {
			// Drawable draw_tuixin = TuixinContactsActivity.this
			// .getResources().getDrawable(
			// R.drawable.tuixin_manage);
			// holder.con_photo.setBackgroundDrawable(draw_tuixin);
			// } else if (id == 1 && tx.partner_id == CommonData.TUIXIN_FRIEND)
			// {
			// Drawable draw_friend = TuixinContactsActivity.this
			// .getResources().getDrawable(
			// R.drawable.friend_manage);
			// holder.con_photo.setBackgroundDrawable(draw_friend);
			// } else {

			loadMemberHeadImage(tx, holder.avatar);

			int tmpSign = GroupUtils.userDignity(tx.partner_id,
					txGroup.group_own_id, txGroup.group_tx_admin_ids);
			ImageView iv_member_title = ((ImageView) convertView
					.findViewById(R.id.group_member_image));
			switch (tmpSign) {
			case 0:
				iv_member_title.setVisibility(View.VISIBLE);
				iv_member_title.setImageResource(R.drawable.group_host);

				break;
			case 1:
				iv_member_title.setVisibility(View.VISIBLE);
				iv_member_title.setImageResource(R.drawable.group_admin);
				break;
			case 2:
				iv_member_title.setVisibility(View.GONE);
				break;
			default:
				break;
			}

			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					toUserInfo(tx);
				}
			});
			convertView.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					handlerTX(tx, true);
					return false;
				}
			});

			return convertView;
		}

	}

	protected void loadMemberHeadImage(TX tx, ImageView avatar) {
		if (tx.getSex() == TX.MALE_SEX) {
			defaultHeaderImg = defaultHeaderImgMan;
		} else {
			defaultHeaderImg = defaultHeaderImgFemale;
		}

		if (!Utils.checkSDCard()) {// 无SD卡
			avatar.setImageResource(defaultHeaderImg);
		} else if (Utils.isIdValid(tx.partner_id)) {
			avatar.setTag(tx.partner_id);
			if (tx.partner_id == TX.TUIXIN_MAN) {
				avatar.setImageResource(R.drawable.tuixin_manage);
			} else {
				avatar.setImageResource(defaultHeaderImg);
				Bitmap avatar_bm = mSess.avatarDownload.getAvatar(
						tx.avatar_url, tx.partner_id, avatar, avatarHandler);
				if (avatar_bm != null) {
					avatar.setImageBitmap(avatar_bm);
				}
			}
		} else {
			avatar.setImageResource(defaultHeaderImg);
		}
	}

	private Handler avatarHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.AVATAR_RESULT:
				Object[] result = (Object[]) msg.obj;
				ImageView iv = null;
				if (isblack) {
					iv = (ImageView) mBlackListView
							.findViewWithTag((Long) result[1]);
					if (iv != null && result != null
							&& (Bitmap) result[0] != null) {
						iv.setImageBitmap((Bitmap) result[0]);
					}
				} else {
					iv = (ImageView) mMemberListView
							.findViewWithTag((Long) result[1]);
					if (iv != null) {
						if (result != null && (Bitmap) result[0] != null) {
							iv.setImageBitmap((Bitmap) result[0]);
						}
					}
				}
				break;

			}
			super.handleMessage(msg);
		}
	};

	private class TXHolder {
		TextView nickName;
		ImageView sex;
		TextView area;
		ImageView avatar;
		TextView level;
		TextView introl;
		TX tx;
	}

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			// cancelDialogTimer();
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_GET_MORE_GROUP_USER.equals(intent
					.getAction())) {
				cancelDialogTimer();
				dealGroupUserListMsg(serverRsp);
			} else if (Constants.INTENT_ACTION_BLACK_LIST_GROUP_2036
					.equals(intent.getAction())) {
				dealGroupBlackListMsg(serverRsp);
			} else if (Constants.INTENT_ACTION_GET_GROUP.equals(intent
					.getAction())) {
				dealGroupInfo(serverRsp);
			} else if (Constants.INTENT_ACTION_SET_ADMIN_GROUP_2022
					.equals(intent.getAction())) {
				cancelDialogTimer();
				dealSetAdmin(serverRsp);
			} else if (Constants.INTENT_ACTION_SHUTUP_GROUP_2028.equals(intent
					.getAction())) {
				cancelDialogTimer();
				dealShutup(serverRsp);
			} else if (Constants.INTENT_ACTION_ADD_BLACK_GROUP_2026
					.equals(intent.getAction())) {
				cancelDialogTimer();
				dealSetBlack(serverRsp);
			} else if (Constants.INTENT_ACTION_DEL_GROUP_MEMBER.equals(intent
					.getAction())) {
				cancelDialogTimer();
				dealDelMember(serverRsp);
			} else if (Constants.INTENT_ACTION_GET_PUBLIC_ONLINE_MEMBER
					.equals(intent.getAction())) {
				cancelDialogTimer();
				dealOnlineGroupMember(serverRsp);
			} else if (Constants.INTENT_ACTION_WARN_USER.equals(intent
					.getAction())) {
				cancelDialogTimer();
				dealWarnUser(serverRsp);
			} else if (Constants.INTENT_ACTION_BLOCK_USER.equals(intent
					.getAction())) {
				cancelDialogTimer();
				dealBlock(serverRsp);
			}
		}
	}

	private int currentPage = 0;
	private int totalPage = 1;
	private int totalSize = 0;
	private int pageSize = 10;

	private boolean getNextIds() {
		if (TxGroup.isPublicGroup(txGroup)) {
			return getNextOnline();
		}

		isLoadingNextPage = true;
		if (currentPage < totalPage) {
			if (currentPage == totalPage - 1) {
				mSess.getSocketHelper().sendGetMoreUsers(
						getTempList(totalSize), MsgInfor.SERVER_INFO_QUN);
			} else {
				mSess.getSocketHelper().sendGetMoreUsers(
						getTempList((currentPage + 1) * 10),
						MsgInfor.SERVER_INFO_QUN);
			}
			return false;
		} else {
			if (currentPageNum < endPageNum) {
				// 群成员没有显示全
				mSess.getSocketHelper().sendGetGroup(txGroup.group_id,
						++currentPageNum);
				return false;
			} else {
				isGetOverMember = true;
				return true;
			}
		}

	}

	public void dealBlock(ServerRsp serverRsp) {
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			int sn = serverRsp.getInt("sn", 0);
			if (sn == 0) {
				Utils.startPromptDialog(GroupMember.this, R.string.prompt,
						R.string.seal_id_success);
			} else {
				Utils.startPromptDialog(GroupMember.this, R.string.prompt,
						R.string.seal_moible_success);
			}
			break;
		case NO_PERMISSION:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.cannot_slience);
			break;
		case USER_NO_EXIST:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.user_no_exists);
			break;
		case DONE:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.seal_done);
			break;
		case OPT_FAILED:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.opt_failed);
			break;
		case BLOCK_FAILED:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.seal_moible_failed);
			break;
		}
	}

	public void dealWarnUser(ServerRsp serverRsp) {
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.opt_success);
			break;
		case USER_NO_EXIST:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.user_no_exists);
			break;
		case NO_PERMISSION:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.cannot_slience);
			break;
		case OPT_FAILED:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.opt_failed);
			break;
		}
	}

	private boolean getNextOnline() {
		if (onlineCp < onlineEp) {
			isLoadingNextPage = true;
			mSess.getSocketHelper().sendGetGroupOnlineMember(groupId, onlineCp);
			return false;
		} else {
			isGetOverMember = true;
			return true;
		}
	}

	int onlineEp = 1;
	int onlineCp = 0;

	public void dealOnlineGroupMember(ServerRsp serverRsp) {
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			onlineEp = serverRsp.getBundle().getInt("ep");
			onlineCp = serverRsp.getBundle().getInt("cp");
			onlineCp = onlineCp + 1;
			Bundle bundle = serverRsp.getBundle();
			ArrayList<TX> txList = bundle
					.getParcelableArrayList(MsgHelper.USER_LIST);
			membersList.addAll(txList);
			membersList = TX.tm.listUniq(membersList);
			membersAdapter.setData(membersList);
			membersAdapter.notifyDataSetChanged();
			isLoadingNextPage = false;
			mLoading.setVisibility(View.GONE);
			break;
		case OPT_FAILED:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.opt_failed);
			break;
		}

	}

	private List<String> getTempList(int size) {
		List<Long> temp = totalList.subList(currentPage * 10, size);
		List<String> temp2 = new ArrayList<String>();
		for (Long id : temp) {
			temp2.add(String.valueOf(id));
		}
		return temp2;
	}

	boolean isGetOverMember = false;// 应该是标记是否已经获取全部成员
	boolean isGetOverBlack = false;

	public void dealGroupUserListMsg(ServerRsp serverRsp) {
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:

			Bundle bundle = serverRsp.getBundle();
			ArrayList<TX> txList = bundle
					.getParcelableArrayList(MsgHelper.USER_LIST);
			/*
			 * if(TxGroup.isPublicGroup(txGroup)){ membersList.addAll(0,txList);
			 * }else{
			 */
			membersList.addAll(txList);
			// }

			membersList = TX.tm.listUniq(membersList);
			for (TX tx : membersList) {
				if (adminsList.contains(tx.partner_id)
						|| tx.partner_id == txGroup.group_own_id) {
					// 管理员和群主排序
					membersList = orderTXByOuth(membersList,
							txGroup.group_own_id);
				}
			}

			currentPage++;
			membersAdapter.setData(membersList);
			membersAdapter.notifyDataSetChanged();
			isLoadingNextPage = false;
			mLoading.setVisibility(View.GONE);
			break;
		case OPT_FAILED:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.opt_failed);
			break;
		}
	}

	private List<TX> orderTXByOuth(List<TX> txs, long oid) {
		List<TX> lists = new ArrayList<TX>();
		for (TX tx : txs) {
			if (tx.partner_id == oid) {
				lists.add(0, tx);
			} else if (adminsList.contains(tx.partner_id)) {
				if (lists.size() > 0) {
					lists.add(1, tx);
				} else {
					lists.add(0, tx);
				}
			} else {
				lists.add(tx);
			}
		}
		return lists;
	}

	private void getTotalPage(boolean isFirstAdd, TxGroup txGroup) {
		if (isFirstAdd) {
			// 第一次添加时，添加群主和管理员
			totalList.add(txGroup.group_own_id);
			totalList.addAll(adminsList);
		}
		if (!TxGroup.isPublicGroup(txGroup)) {
			totalList.addAll(txGroup.group_ids_list);
		}
		// 去重
		totalList = listUniq(totalList);
		totalSize = totalList.size();
		int temp = totalSize % pageSize;
		if (temp == 0) {
			totalPage = totalSize / pageSize;
		} else {
			totalPage = totalSize / pageSize + 1;
		}
	}

	public ArrayList<Long> listUniq(List<Long> list) {
		Set<Long> set = new LinkedHashSet<Long>();
		set.addAll(list);
		ArrayList<Long> newlist = new ArrayList<Long>();
		newlist.addAll(set);
		return newlist;
	}

	public void dealDelMember(ServerRsp serverRsp) {
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			ArrayList<String> uids = serverRsp.getBundle().getStringArrayList(
					"ids");
			if (uids != null) {
				for (String id : uids) {
					for (TX tx : membersList) {
						if (tx.partner_id == Long.valueOf(id)) {
							membersList.remove(tx);
							break;
						}
					}
				}
			}
			membersAdapter.notifyDataSetChanged();
			showToastText(R.string.opt_success);
			break;
		case OPT_FAILED:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.opt_failed);
			break;
		case GROUP_MEMBER_SIZE_INVALID:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.group_del_to_more);
			break;
		}
	}

	public void dealSetBlack(ServerRsp serverRsp) {
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			long uid = serverRsp.getBundle().getLong("uid");
			boolean did = serverRsp.getBundle().getBoolean("did");
			int op = serverRsp.getBundle().getInt("op");
			if (op == 0) {
				for (TX tx : membersList) {
					if (tx.partner_id == uid) {
						membersList.remove(tx);
						membersAdapter.notifyDataSetChanged();
						blackList.add(tx);
						blackListAdapter.setData(blackList);
						blackListAdapter.notifyDataSetChanged();
						break;
					}
				}
			} else {
				for (TX tx : blackList) {
					if (tx.partner_id == uid) {
						blackList.remove(tx);
						blackListAdapter.notifyDataSetChanged();
						break;
					}
				}
			}
			if (did) {
				if (op == 0) {
					showToastText(R.string.black_done);
				} else {
					showToastText(R.string.black_done2);
				}
			} else {
				showToastText(R.string.opt_success);
			}
			break;
		case OPT_FAILED:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.opt_failed);
			break;
		case NO_PERMISSION:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.cannot_slience);
			break;
		case GROUP_BLACK_LIST_TO_MORE:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.group_black_list_to_more);
			break;
		case GROUP_NO_EXIST:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.group_not_exists);
			break;

		}

	}

	private void showToastText(final int msg) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(GroupMember.this, msg, Toast.LENGTH_SHORT)
						.show();
			}
		});
	}

	public void dealShutup(ServerRsp serverRsp) {
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			int op = serverRsp.getBundle().getInt("op");
			boolean did = serverRsp.getBundle().getBoolean("did");
			Long uid = serverRsp.getBundle().getLong("uid");
			if (op == 0) {
				banList.add(uid);
			} else {
				banList.remove(uid);
			}
			if (did) {
				if (op == 0) {
					showToastText(R.string.shutup_done);
				} else {
					showToastText(R.string.shutup_cancel);
				}
			} else {
				showToastText(R.string.opt_success);
			}

			break;
		case OPT_FAILED:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.opt_failed);
			break;
		case NO_PERMISSION:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.cannot_slience);
			break;
		case GROUP_NOT_MEMBER:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.group_not_member);
			break;
		case GROUP_NO_EXIST:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.group_not_exists);
			break;
		}

	}

	public void dealSetAdmin(ServerRsp serverRsp) {
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			int op = serverRsp.getBundle().getInt("op");
			Long uid = serverRsp.getBundle().getLong("uid");
			if (op == 0) {
				adminsList.add(uid);
			} else {
				adminsList.remove(uid);
			}
			txGroup = TxGroup.getTxGroup(mSess.getContentResolver(),
					(int) txGroup.group_id);
			membersList = orderTXByOuth(membersList, txGroup.group_own_id);
			membersAdapter.setData(membersList);
			membersAdapter.notifyDataSetInvalidated();
			showToastText(R.string.opt_success);
			break;
		case OPT_FAILED:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.opt_failed);
			break;
		case NO_PERMISSION:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.cannot_slience);
			break;
		case GROUP_NOT_MEMBER:
			uid = serverRsp.getBundle().getLong("uid");
			for (TX tx : membersList) {
				if (uid == tx.partner_id) {
					membersList.remove(tx);
					break;
				}
			}
			membersAdapter.notifyDataSetChanged();
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.group_not_member);
			break;
		case ADMIN_FUll:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.group_admin_full);
			break;
		}

	}

	int blackCp = 0;
	int blackEp = 0;
	int totalSizeBlack;
	int totalPageBlack;
	int pageSizeBlack = 10;
	int currentPageBlack;

	public void dealGroupBlackListMsg(ServerRsp serverRsp) {
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			Bundle bundle = serverRsp.getBundle();
			ArrayList<TX> tempBackList = bundle
					.getParcelableArrayList(MsgHelper.USER_LIST);
			currentPageBlack++;
			blackList.addAll(tempBackList);
			blackList = TX.tm.listUniq(blackList);
			blackListAdapter.setData(blackList);
			blackListAdapter.notifyDataSetChanged();
			mLoading.setVisibility(View.GONE);
			break;
		case OPT_FAILED:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.opt_failed);
			break;
		case GET_OVER:
			isGetOverBlack = true;
			mLoading.setVisibility(View.GONE);
			break;
		case GROUP_FOR_PAGE:
			blackEp = serverRsp.getBundle().getInt("ep");
			blackCp = serverRsp.getBundle().getInt("cp");

			ArrayList<String> tempIds = serverRsp.getBundle()
					.getStringArrayList("idsList");
			blackIdsList.addAll(tempIds);
			totalSizeBlack = blackIdsList.size();
			int temp = totalSizeBlack % pageSizeBlack;
			if (temp == 0) {
				totalPageBlack = totalSizeBlack / pageSizeBlack;
			} else {
				totalPageBlack = totalSizeBlack / pageSizeBlack + 1;
			}
			getNextBlackList();
			break;
		}

	}

	private boolean getNextBlackList() {
		if (currentPageBlack < totalPageBlack) {
			if (currentPageBlack == totalPageBlack - 1) {
				mSess.getSocketHelper().sendGetMoreUsers(
						blackIdsList.subList(currentPageBlack * 10,
								totalSizeBlack),
						MsgInfor.SERVER_GET_BLACKLIST_QUN);
			} else {
				mSess.getSocketHelper().sendGetMoreUsers(
						blackIdsList.subList(currentPageBlack * 10,
								(currentPageBlack + 1) * 10),
						MsgInfor.SERVER_GET_BLACKLIST_QUN);
			}
			return false;
		} else if ((blackCp + 1) >= blackEp) {
			isGetOverBlack = true;
			return true;
		} else {
			mSess.getSocketHelper().sendGetBlackListGroup(txGroup.group_id,
					blackCp + 1);
			return false;
		}
	}

	private void initAdminBan(TxGroup txGroup) {
		adminsList = new HashSet<Long>();
		for (String id : txGroup.group_tx_admin_ids.split("�")) {
			if (!Utils.isNull(id)) {
				adminsList.add(Long.valueOf(id));
			}
		}
		banList = new HashSet<Long>();
		for (String id : txGroup.ban_ids.split("�")) {
			if (!Utils.isNull(id)) {
				banList.add(Long.valueOf(id));
			}
		}
	}

	private int meType;
	private int currentPageNum;// 服务器返回的当前群成员的页数
	private int endPageNum;// 服务器返回的当前群成员的总页数

	public void dealGroupInfo(ServerRsp serverRsp) {
		// cancelDialogTimer();
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			Bundle bundle = serverRsp.getBundle();
			txGroup = bundle.getParcelable(Utils.MSGROOM_TX_GROUP);
			if (membersList.size() != 0) {
				// if (currentView == FROM_MSGROOM) {
				if (currentView == DISPLAY_MEMBER
						|| currentView == DISPLAY_ONLINE) {
					// 查询群成员
					currentPageNum = bundle.getInt(CURRENT_PAGE_NUM);
					endPageNum = bundle.getInt(END_PAGE_NUM);

					initAdminBan(txGroup);// 获得管理员列表
					getTotalPage(false, txGroup);

					// 是递增页数的在线人员或群成员
					getNextIds();
				}
				return;
			}

			currentPageNum = bundle.getInt(CURRENT_PAGE_NUM);
			endPageNum = bundle.getInt(END_PAGE_NUM);

			View v_addMember = findViewById(R.id.iv_add_member);
			if (!TxGroup.isPublicGroup(txGroup)) {
				// 增加添加成员功能
				v_addMember.setVisibility(View.VISIBLE);
				v_addMember.setOnClickListener(this);
			} else {
				v_addMember.setVisibility(View.GONE);
			}
			membersAdapter = new MembersAdapter(GroupMember.this);
			blackListAdapter = new BlacklistAdapter(GroupMember.this);

			mMemberListView.setAdapter(membersAdapter);
			mBlackListView.setAdapter(blackListAdapter);
			mMemberListView.setVisibility(View.VISIBLE);
			mBlackListView.setVisibility(View.GONE);
			isblack = false;

			groupType = txGroup.group_type_channel;
			if (txGroup != null) {
				initAdminBan(txGroup);// 获得管理员列表
				getTotalPage(true, txGroup);
				meType = TxGroup.checkAdminCreator(txGroup);

				// if (currentView != FROM_MSGROOM) {
				if (meType != TxDB.QU_TX_STATE_CM) {
					mAdminLayout.setVisibility(View.VISIBLE);
				}
				// }
				// 取在线人员或群成员
				getNextIds();
				// 群资料界面进入的群成员列表时 才 查询黑名单
				// if (currentView != FROM_MSGROOM) {
				mSess.getSocketHelper().sendGetBlackListGroup(groupId, 0);
				// }

			}
			break;
		case OPT_FAILED:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.opt_failed);
			break;
		case GROUP_MODIFY_GROUP_NOT_EXIST:
			Utils.startPromptDialog(GroupMember.this, R.string.prompt,
					R.string.group_not_exists);
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mLoading.getVisibility() == View.VISIBLE) {
			mLoading.setVisibility(View.GONE);
		}
		registReceiver();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregistReceiver();
	}

	private void unregistReceiver() {
		if (updatareceiver != null) {
			this.unregisterReceiver(updatareceiver);
			updatareceiver = null;
		}
	}

	private void registReceiver() {
		if (updatareceiver == null) {
			updatareceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_GET_MORE_GROUP_USER);
			filter.addAction(Constants.INTENT_ACTION_GET_GROUP);
			filter.addAction(Constants.INTENT_ACTION_BLACK_LIST_GROUP_2036);
			filter.addAction(Constants.INTENT_ACTION_SET_ADMIN_GROUP_2022);
			filter.addAction(Constants.INTENT_ACTION_GET_PUBLIC_ONLINE_MEMBER);
			filter.addAction(Constants.INTENT_ACTION_SHUTUP_GROUP_2028);
			filter.addAction(Constants.INTENT_ACTION_ADD_BLACK_GROUP_2026);
			filter.addAction(Constants.INTENT_ACTION_DEL_GROUP_MEMBER);
			filter.addAction(Constants.INTENT_ACTION_WARN_USER);
			filter.addAction(Constants.INTENT_ACTION_BLOCK_USER);
			this.registerReceiver(updatareceiver, filter);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 100) {
			if (mLoading.getVisibility() == View.VISIBLE) {
				mLoading.setVisibility(View.GONE);
			}
			ArrayList<TX> temp = data.getParcelableArrayListExtra("txs");
			membersList.addAll(temp);
			membersList = TX.tm.listUniq(membersList);
			membersAdapter.setData(membersList);
			membersAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_add_member:
			Intent i = new Intent(GroupMember.this,
					SelectFriendListActivity.class);
			i.putExtra(SelectFriendListActivity.CHAT_TYPE,
					SelectFriendListActivity.CHAT_TYPE_GROUP);
			ArrayList<Long> temp2 = new ArrayList<Long>();
			temp2.addAll(totalList);
			temp2.addAll(banList);
			for (int ii = 0; ii < blackList.size(); ii++) {
				temp2.add(blackList.get(ii).partner_id);
			}
			TxGroup temp = new TxGroup();
			temp = txGroup;
			StringBuffer sb = new StringBuffer();
			for (Long id : temp2) {
				sb.append(id).append("�");
			}
			temp.group_tx_ids = sb.toString();
			i.putExtra(SelectFriendListActivity.CHAT_TYPE_GROUP_OGJ, temp);
			i.putExtra(SelectFriendListActivity.FROM,
					SelectFriendListActivity.FROM_GROUP_MEMBER);
			startActivityForResult(i, 0);
			break;
		case R.id.group_member:
			if (Utils.debug)
				Log.i(TAG, "显示群成员");
			mMember.setBackgroundResource(R.drawable.contacts_tab_left_selected);
			mMember.setTextColor(Color.WHITE);
			mBlackList
					.setBackgroundResource(R.drawable.contacts_tab_right_unselected);
			mBlackList.setTextColor(getResources().getColor(
					R.color.content_color_blue));

			currentView = DISPLAY_MEMBER;
			mMemberListView.setVisibility(View.VISIBLE);
			mBlackListView.setVisibility(View.GONE);
			isblack = false;
			mLoading.setVisibility(View.GONE);
			break;
		case R.id.group_black:
			if (Utils.debug)
				Log.i(TAG, "显示黑名单");
			mMember.setBackgroundResource(R.drawable.contacts_tab_left_unselected);
			mMember.setTextColor(getResources().getColor(
					R.color.content_color_blue));
			mBlackList
					.setBackgroundResource(R.drawable.contacts_tab_right_selected);
			mBlackList.setTextColor(Color.WHITE);

			currentView = DISPLAY_BLACK;
			mBlackListView.setVisibility(View.VISIBLE);
			mMemberListView.setVisibility(View.GONE);
			isblack = true;
			mLoading.setVisibility(View.GONE);
			break;
		}
	}

	AsyncCallback<Bitmap> avatarCallback_b = new AsyncCallback<Bitmap>() {
		@Override
		public void onFailure(Throwable t, long id) {
		}

		@Override
		public void onSuccess(Bitmap result, long id) {
			ImageView iv = (ImageView) mBlackListView.findViewWithTag(id);
			if (iv != null && result != null) {
				iv.setImageBitmap(result);
			}
		}
	};

	AsyncCallback<Bitmap> avatarCallback = new AsyncCallback<Bitmap>() {
		@Override
		public void onSuccess(Bitmap result, long id) {
			ImageView iv = null;
			iv = (ImageView) mMemberListView.findViewWithTag(id);
			if (result != null && iv != null) {
				iv.setImageBitmap(result);
			}
		}

		@Override
		public void onFailure(Throwable t, long id) {

		}
	};
	private boolean isblack;

	@Override
	protected void onDestroy() {
		// stopAsyncload();
		super.onDestroy();
	};

}
