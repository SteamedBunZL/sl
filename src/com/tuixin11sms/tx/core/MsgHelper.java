package com.tuixin11sms.tx.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.shenliao.data.DataContainer;
import com.tuixin11sms.tx.GroupListManager;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.SessionManager.LoginMode;
import com.tuixin11sms.tx.ShenliaoOtherLoginService;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.FriendManagerActivity;
import com.tuixin11sms.tx.activity.GroupMsgRoom;
import com.tuixin11sms.tx.activity.SearchConditionResultActivity;
import com.tuixin11sms.tx.activity.WarnDialogAcitvity;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.contact.TxInfor;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.data.TxDB.Messages;
import com.tuixin11sms.tx.data.TxDB.Tx;
import com.tuixin11sms.tx.db.TxDBContentProvider;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.message.MsgStat;
import com.tuixin11sms.tx.message.PraiseNotice;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.AlbumItem;
import com.tuixin11sms.tx.model.BlogMsg;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.model.StatusCode;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.task.FileTransfer.DownUploadListner;
import com.tuixin11sms.tx.task.FileTransfer.FileTaskInfo;
import com.tuixin11sms.tx.utils.CachedPrefs.PrefsMeme;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.StringUtils;
import com.tuixin11sms.tx.utils.Utils;
import com.weibo.net.AccessToken;
import com.weibo.net.AsyncWeiboRunner.RequestListener;
import com.weibo.net.Weibo;

public class MsgHelper {
	private boolean needSendUpApp;
	boolean needNickName;
	private static final String TAG = MsgHelper.class.getSimpleName();
	private ContentResolver cr;
	public static MsgHelper msghelper;
	private SharedPreferences prefsMeme = null;
	private Editor editorMeme;
	private int p;// 获取好友id时，记录当前id的pageNum页数的

	private static AtomicInteger blackListPageNum = new AtomicInteger(0);// 记录黑名单当前id的页数

	private ArrayList<Long> idlist = new ArrayList<Long>();
	private boolean isEndOfBlackList = false;
	private SessionManager mSess;// 用于存取
	private final Object lock = new Object();
	public String user_id;
	public static String GROUP_LIST = "groupList";
	public static String USER_LIST = "userList";

	/**
	 * 标记是否进行过微博好友比对
	 */
	private Map<String, Boolean> weiboFriendCompared = Collections
			.synchronizedMap(new HashMap<String, Boolean>());

	private MsgHelper(SessionManager sm) {
		mSess = sm;
		cr = mSess.getContentResolver();
		prefsMeme = mSess.getPrefMeme();
		editorMeme = mSess.getEditorMeme();
	}

	public static MsgHelper CreateMsgHelper(SessionManager sm) {
		if (msghelper == null) {
			msghelper = new MsgHelper(sm);
		}
		return msghelper;
	}

	public static String receiveSame(ContentResolver cr, String url) {
		Cursor c = cr.query(TxDB.Messages.CONTENT_URI, null, Messages.MSG_URL
				+ "=?", new String[] { url }, null);
		while (c != null && c.moveToNext()) {
			TXMessage cm = TXMessage.fetchOneMsg(c);
			if (!Utils.isNull(cm.msg_path)) {
				if (Utils.debug)
					Log.i(TAG, "是已经收到过的附件");
				c.close();
				return cm.msg_path;
			}
		}
		c.close();
		return null;
	}

	/** 应该是在获取所有好友id之后执行此方法,获取所有好友详细资料 */
	private void dealGetTBinfor() {
		ArrayList<TX> txList = mSess.getTxMgr().getTBTXList();

		int txListSize = txList.size();
		// 应该是去除本地数据库已经有的好友，idlist中剩下的是新的需要向服务器请求的好友集合
		for (int i = 0; i < txListSize; i++) {
			for (int j = 0; j < idlist.size(); j++) {
				if (idlist.get(j) == txList.get(i).partner_id) {
					idlist.remove(j);
					j--;
				}
			}
		}

		for (int i = 0; i < idlist.size(); i++) {
			mSess.getSocketHelper().sendGetUserInfor(idlist.get(i));
		}
	}

	public void dealMsg(final String msg) {
		if (msg.startsWith("{")) {
			JSONObject jo = null;
			try {
				jo = new JSONObject(msg);
			} catch (JSONException e) {
				if (Utils.debug)
					e.printStackTrace();
			}
			if (jo != null) {
				int type = 0;
				try {
					type = jo.getInt("mt");
				} catch (JSONException e) {
					if (Utils.debug)
						e.printStackTrace();
				}
				int d;
				switch (type) {
				case MsgInfor.SERVER_PING:
					try {
						boolean bl = jo.getBoolean("bl");
						if (bl) {
							if (SocketHelper.upCount == 0)
								if (!mSess.getSocketHelper().upAddr)
									mSess.getSocketHelper().upAddressBook(
											mSess.getContext());
							System.gc();
							// try {
							// Thread.sleep(5000);
							// } catch (InterruptedException e) {
							// if (Utils.debug)
							// e.printStackTrace();
							// }
							SocketHelper.upCount++;
						}
						// 上传微博好友
						mSess.uploadSinaFriend();
					} catch (JSONException e6) {
						e6.printStackTrace();
					}
					break;
				case MsgInfor.SERVER_REGSTER:
					try {
						d = jo.getInt("d");

						ServerRsp registRsp = new ServerRsp();
						if (d == 2) {
							String fbret = jo.getString("fbret");
							// editorMeme.putString(CommonData.REGIST_NAME, "");
							// editorMeme.commit();
							mSess.mPrefMeme.regist_name.setVal("").commit();
							Bundle b = registRsp.getBundle();
							b.putString("fbret", fbret);
							registRsp.setStatusCode(StatusCode.OPT_FAILED);
						}
						if (d == 4) {
							String fbret = jo.getString("fbret");
							String errmsg = jo.getString("errmsg");
							// editorMeme.putString(CommonData.REGIST_NAME, "");
							// editorMeme.commit();
							mSess.mPrefMeme.regist_name.setVal("").commit();
							Bundle b = registRsp.getBundle();
							b.putString("fbret", fbret);
							b.putString("errmsg", errmsg);
							registRsp.setStatusCode(StatusCode.MORE_REGIST);
						}
						if (d == 1) {
							// editorMeme.putString(CommonData.REGIST_NAME, "");
							// editorMeme.commit();
							mSess.mPrefMeme.regist_name.setVal("").commit();
							registRsp
									.setStatusCode(StatusCode.LOGIN_NAME_FAIELD);
						} else if (d == 0) {
							// 更新用户性别
							Utils.isOpenPlayAdiou = true;
							Utils.saveAutoPlayAdiouData(mSess.getContext());
							mSess.getSocketHelper().sendSexChange(
									mSess.getSex());
							registRsp.setStatusCode(StatusCode.RSP_OK);
							idlist.clear();
							String nickname = mSess.mPrefMeme.regist_name
									.getVal();
							final String id = jo.getString("i");

							mSess.mPrefMeme.user_id.setVal(id);
							mSess.mPrefMeme.is_sethead.setVal(false);
							mSess.mPrefMeme.avatar_url.setVal("");
							mSess.mPrefMeme.avatar_path.setVal("").commit();

							// TX.tm.reloadTXMe();// ////
							mSess.mMsgHandler.onReloadTXMe();

							user_id = id;
							String pas = jo.getString("pwd");
							if (Utils.isNull(pas)) {
								pas = id;
							}

							if (mSess.getSmallAvatar() != null) {

								String tempImgPath = Utils.getStoragePath(mSess
										.getContext())
										+ File.separator
										+ Utils.IMAGE_PATH
										+ File.separator
										+ System.currentTimeMillis() + ".jpg";
								try {
									// 生成合成的文件
									mSess.mDownUpMgr.getCompoundImgFile(
											tempImgPath,
											mSess.getSmallAvatar(),
											mSess.getBigAvatar());
								} catch (IOException e) {
									if (Utils.debug) {
										Log.w(TAG, "合成大小图文件异常", e);
									}
								}
								mSess.mDownUpMgr.uploadImg(tempImgPath, 0,
										true, false, new DownUploadListner() {

											@Override
											public void onFinish(
													FileTaskInfo taskInfo) {
												String fileUrl = taskInfo.mServerHost
														+ ":"
														+ taskInfo.mPath
														+ ":" + taskInfo.mTime;
												mSess.getSocketHelper()
														.sendUpAvatar(fileUrl);
												String storagePath = Utils
														.getStoragePath(mSess
																.getContext());

												File sddir = new File(
														storagePath,
														Utils.AVATAR_PATH);
												if (!sddir.exists()
														&& !sddir.mkdirs()) {
													if (Utils.debug)
														Log.e(TAG,
																"bitmapFromUrl---Create dir failed");
													sddir.mkdir();
												}

												StringBuffer tempPath = new StringBuffer()
														.append(sddir
																.getAbsolutePath())
														.append("/")
														.append(mSess
																.getUserid())
														.append(".jpg");
												mSess.mPrefMeme.is_sethead
														.setVal(true);
												mSess.mPrefMeme.avatar_url
														.setVal(fileUrl);
												try {
													mSess.mUserLoginInfor
															.updateUserAvatarSex(
																	Long.parseLong(id),
																	fileUrl);
												} catch (Exception e) {
													if (Utils.debug) {
														Log.e(TAG,
																"注册成功，更新登登陆头像和性别异常",
																e);
													}
												}
												mSess.mPrefMeme.avatar_path
														.setVal(tempPath
																.toString())
														.commit();
												mSess.mMsgHandler
														.onReloadTXMe();
											}

										}, null);

							}

							mSess.mUserLoginInfor.saveRegistUserPwds(id, pas);// 新注册用户默认不记住密码吧？2013.12.25

							if (Utils.debug)
								Log.e(TAG, "注册 creatDB");
							if (TxDBContentProvider.getmOpenHelper() != null)
								TxDBContentProvider.getmOpenHelper().close();
							TxDBContentProvider.setmOpenHelper(null);
							TxDBContentProvider.creatDB(mSess.getContext(),
									user_id);
							mSess.mPrefMeme.nickname.setVal(nickname);
							mSess.mPrefMeme.login_first.setVal("loginfirst");
							mSess.mPrefMeme.regist_name.setVal("");
							mSess.mPrefMeme.album.setVal("");
							mSess.mPrefMeme.album_version.setVal(0).commit();

							mSess.mMsgHandler.onReloadTXMe();

							String userid = mSess.mPrefMeme.user_id.getVal();
							if (!"".equals(userid)) {
								if (needSendUpApp) {
									mSess.getSocketHelper().sendCheckVer();
								}
							}
						}
						// 获取我的最新个人信息
						mSess.getSocketHelper().getNewUserinforForLevel();
						broadcastMsg(Constants.INTENT_ACTION_REGIST_RSP,
								registRsp);
					} catch (JSONException e3) {
						if (Utils.debug) {
							e3.printStackTrace();
						}
					}

					break;

				case MsgInfor.SERVER_LOGIN: {
					if (Utils.debug)
						Log.e(TAG, "收到服务器对请求登陆的响应");
					try {
						d = jo.getInt("d");
						ServerRsp loginRsp = new ServerRsp();
						if (Utils.debug)
							Log.i(TAG, "+++++++++++++" + d);
						if (d == 1) {
							loginRsp.setStatusCode(StatusCode.LOGIN_ACCOUNT_NO_EXIST);
						} else if (d == 2) {
							loginRsp.setStatusCode(StatusCode.LOGIN_NICK_PWD_ERROR);
						} else if (d == 3) {
							loginRsp.setStatusCode(StatusCode.LOGIN_OPT_FAIELD);
						} else if (d == 4) {
							loginRsp.setStatusCode(StatusCode.LOGIN_NICK_INVALID);
						} else if (d == 5) {
							loginRsp.setStatusCode(StatusCode.USER_BLOCK);
						} else if (d == 0) {
							loginRsp.setStatusCode(StatusCode.RSP_OK);
							if (msg.contains("bnew")) {
								mSess.mPrefMeme.weibo_new.setVal(jo
										.getBoolean("bnew"));
							}
							idlist.clear();
							final String id = jo.getString("i");
							long partner_id = Long.parseLong(id);// 当前用户id

							String nickname = jo.getString("n");
							if (Utils.debug) {
								Log.i(TAG, "返回用户昵称:" + jo.getString("n"));
							}
							boolean isReceiveReq = jo.getBoolean("frdrqstat");

							int auth = jo.getInt("auth");
							final String avatar_url = jo.getString("avatarurl");
							String email = jo.getString("e");
							String phone = jo.getString("o");
							if (!"".equals(phone)) {
								String conver_phone = Utils.GetNumber(phone);
								phone = conver_phone;
							}
							boolean obd = jo.getBoolean("obd");
							boolean ebd = jo.getBoolean("ebd");
							final int friendver = jo.optInt("friendver");
							int alubmVver = jo.getInt("aver");// 相册版本
							int sex = jo.getInt("sex");
							String area = StringUtils.jsonArray2Str(jo
									.getJSONArray("narea"));

							String sign = jo.getString("sign");
							if (Utils.debug) {
								Log.i(TAG, "返回用户性别:" + jo.getString("sex"));
								Log.i(TAG, "返回用户签名:" + jo.getString("sign"));
							}
							int birthday = jo.getInt("birthday");
							int blood = jo.getInt("blood");
							String hobby = StringUtils.jsonArray2Str(jo
									.getJSONArray("nhobby"));
							String job = jo.getString("job");

							String templan = StringUtils.jsonArray2Str(jo
									.getJSONArray("lang"));
							List<String> lanlist = StringUtils
									.str2List(templan);
							if (lanlist != null && lanlist.contains("0")) {
								lanlist.remove("0");
							}
							String language = StringUtils.list2String(lanlist);

							int inforVer = jo.getInt("iver");
							int avatarver = jo.getInt("avatarver");
							int level = Utils.level;
							if (Utils.lev)
								level = jo.getInt("level");// 个人等级

							{
								// 封装TX更新数据库 2014.06.06 shc

								TX txme = new TX();
								txme.setPartner_id(partner_id);
								txme.setNick_name(nickname);
								txme.setReceiveReq(isReceiveReq);
								txme.setPhone(phone);
								txme.setEmail(email);
								txme.setPhoneBind(obd);
								txme.setEmailBind(ebd);
								txme.setFriend_ver(friendver);
								txme.setAlbumVer(alubmVver);
								txme.setAvatar_url(avatar_url);
								txme.setSex(sex);
								txme.setArea(area);
								txme.setSign(sign);
								txme.setBirthday(birthday);// 没有生日返回0，有生日返回int型：19880101
								txme.setBloodType(blood);
								txme.setHobby(hobby);
								txme.setJob(job);
								txme.setLanguages(language);
								txme.setAvatar_ver(avatarver);
								txme.setInfoVer(inforVer);
								txme.setIsop(auth);
								txme.setLevel(level);

								mSess.mMsgHandler.onServerLogin_3(txme,
										new IExtraOpreater() {

											@Override
											public void operate() {
												// 处理91和92号协议
												if (mSess.getLoginMode() == LoginMode.OTHER_ACCOUNT_LOGIN
														&& mSess.getAccountType() == SessionManager.SINA_ACCOUNT) {
													Boolean compared = weiboFriendCompared.get(mSess
															.getWeiboUserUD());
													if (compared == null
															|| !compared) {
														weiboFriendCompared.put(
																mSess.getWeiboUserUD(),
																true);
														mSess.getSocketHelper()
																.sendWeiboFriendCompare(
																		mSess.getWeiboUserUD(),
																		mSess.getWeiboToken(),
																		null,
																		mSess.getAccountType(),
																		mSess.getAuthType());
													}
													// 表示此用户没有成功同步过资料
													if (Utils
															.isNull(avatar_url)) {
														mSess.getContext()
																.startService(
																		new Intent(
																				mSess.getContext(),
																				ShenliaoOtherLoginService.class));
													}

												}
											}
										}, new IExtraOpreater() {

											@Override
											public void operate() {
												// 如果本地好友版本号和Server版本号不同，那么请求好友列表
												int ver = mSess.mPrefMeme.friendver
														.getVal();//
												if (ver != friendver) {
													p = 0;
													mSess.getSocketHelper()
															.sendContactsId(p);
													mSess.mPrefMeme.friendver
															.setVal(friendver);//
												}
											}
										});
							}

							// mSess.mPrefMeme.user_id.setVal(id);//
							// mSess.mPrefMeme.nickname.setVal(nickname);//
							// mSess.mPrefMeme.auth.setVal(auth);
							// mSess.mPrefMeme.is_receive_req.setVal(isReceiveReq)
							// .commit();//
							//
							// if (Utils.debug) {
							// Log.i(TAG, "返回用户头像地址:" + avatar_url);
							// }
							//
							// mSess.getSocketHelper().sendUserQun(partner_id);
							//
							// if (Utils.debug)
							// Log.e(TAG, "登录 成功 creatDB ,创建数据库，发送广播");
							// mSess.userLogin(id);
							//
							// mSess.mPrefMeme.email.setVal(email);//
							// mSess.mPrefMeme.telephone.setVal(phone);//
							//
							// if (obd) {
							// mSess.mPrefMeme.tel_bind_state
							// .setVal(PrefsMeme.TEL_BIND_SUCCESS);
							// }
							// mSess.mPrefMeme.is_bind_phone.setVal(obd);//
							// mSess.mPrefMeme.is_bind_email.setVal(ebd);//
							//
							//
							//
							// // 本地相册版本号小于Server则请求相册
							// if (mSess.mPrefMeme.album_version.getVal() <
							// alubmVver) {
							// mSess.getSocketHelper().getAlbum(partner_id,
							// mSess.mPrefMeme.album_version.getVal());
							// mSess.mPrefMeme.album_version.setVal(alubmVver)
							// .commit();//
							// }
							//
							// mSess.mPrefMeme.avatar_url.setVal(avatar_url)
							// .commit();//
							// mSess.mPrefMeme.login_first.setVal("loginfirst");
							//
							// mSess.setSex(sex);//
							// 给LoginSessionManager设置sex属性，saveLoginSuccessUserInfor方法要用
							// // 2014.01.02
							// if (mSess.getAccountType() ==
							// SessionManager.SHEN_LIAO_ACCOUNT) {
							// // 如果是神聊登录，则保存账号信息，新浪登陆则不保存
							// mSess.mUserLoginInfor
							// .saveLoginSuccessUserInfor(id,
							// avatar_url);
							// }
							// mSess.mPrefMeme.sex.setVal(sex);//
							// mSess.mPrefMeme.first_commondata.setVal(true)
							// .commit();
							// mSess.mPrefMeme.area.setVal(area);//
							// mSess.mPrefMeme.sign.setVal(sign);//
							//
							// mSess.mPrefMeme.birthday.setVal(birthday);//
							// mSess.mPrefMeme.bloodtype.setVal(blood);//
							//
							// mSess.mPrefMeme.hobby.setVal(hobby);//
							// mSess.mPrefMeme.job.setVal(job);//
							//
							// mSess.mPrefMeme.languages.setVal(language).commit();//
							//
							// mSess.mMsgHandler.onReloadTXMe();
							//
							// mSess.getSocketHelper().sendGetSingleOfflineMsg(
							// (int) partner_id);
							// // try {
							// // Thread.sleep(1000);
							// // } catch (InterruptedException e) {
							// // if (Utils.debug)
							// // e.printStackTrace();
							// // }
							// mSess.getSocketHelper().sendGetOffLineReceipt(
							// (int) partner_id);
							// // try {
							// // Thread.sleep(1000);
							// // } catch (InterruptedException e) {
							// // if (Utils.debug)
							// // e.printStackTrace();
							// // }
							// mSess.getSocketHelper().sendGetOfflineSystemMsg(
							// (int) partner_id);
							// // try {
							// // Thread.sleep(1000);
							// // } catch (InterruptedException e) {
							// // if (Utils.debug)
							// // e.printStackTrace();
							// // }
							// // try {
							// // Thread.sleep(1000);
							// // } catch (InterruptedException e) {
							// // if (Utils.debug)
							// // e.printStackTrace();
							// // }
							// // 查询未发送成功的消息
							// Cursor c = cr.query(TxDB.Messages.CONTENT_URI,
							// null, TxDB.Messages.READ_STATE + "=? and ("
							// + TxDB.Messages.CHANNEL_ID
							// + " is NULL or "
							// + TxDB.Messages.CHANNEL_ID + "=0)",
							// new String[] { String
							// .valueOf(TXMessage.NOT_SENT) },
							// null);
							// ArrayList<TXMessage> list = new
							// ArrayList<TXMessage>();
							// if (c != null) {
							// list = TXMessage.fetchAllDBMsgs(c);
							// c.close();
							// }
							// if (Utils.debug) {
							// if (list != null)
							// Log.i(TAG,
							// "未发送成功的消息数？？no Send Msg Count is : "
							// + list.size());
							// }
							// mSess.getSocketHelper().sendNoSendMsg(list, id);
							// // try {
							// // Thread.sleep(2000);
							// // } catch (InterruptedException e) {
							// // if (Utils.debug)
							// // e.printStackTrace();
							// // }
							// mSess.getSocketHelper().sendGetUserInfor(
							// TX.TUIXIN_MAN);
							// mSess.getSocketHelper().sendGetUserInfor(
							// TX.TUIXIN_FRIEND);
							// 如果需要检测更新，发检测更新请求
							if (needSendUpApp) {
								mSess.getSocketHelper().sendCheckVer();
							}
						}
						broadcastMsg(Constants.INTENT_ACTION_LOGIN_RSP,
								loginRsp);// 发送广播，收到服务器请求登陆的响应

					} catch (JSONException e2) {
						if (Utils.debug) {
							Log.e(TAG, "登陆成功后Json异常", e2);
						}
					}
					break;
				}
				case MsgInfor.SERVER_GET_USER_QUN:
					dealUserQun(jo, msg);
					break;
				case MsgInfor.SERVER_JOIN_GROUP:
					dealJoinGroup(jo);
					break;
				case MsgInfor.SERVER_AGREE_GROUP_REQ:
					dealAgereeGroup(jo);
					break;
				case MsgInfor.SERVER_SET_ADMIN:
					dealSetAdminGroup(jo);
					break;
				case MsgInfor.SERVER_SEARCH_GROUP:
					dealSearchGroup(jo);
					break;
				case MsgInfor.SERVER_BLACK_GROUP:
					dealBlackGroup(jo);
					break;
				case MsgInfor.SERVER_SHUTUP_GROUP:
					// 处理禁言应答
					dealShutupGroup(jo);
					break;
				case MsgInfor.SERVER_IN_OUT_GROUP:
					dealInOutGroup(jo);
					break;
				case MsgInfor.SERVER_GET_PUBLIC_GROUP:
					dealPublicGroup(jo);
					break;
				case MsgInfor.SERVER_GET_BLACKLIST_QUN:
					dealGroupBlackList(jo);
					break;
				case MsgInfor.SERVER_GET_MORE_GROUP:
					dealMoreGroup(jo);
					break;
				case MsgInfor.SERVER_GROUP_REMIND:
					dealSettingGroup(jo);
					break;
				case MsgInfor.SERVER_PUBLIC_ONLINE_MEMBER:
					dealPubOnlineMember(jo);
					break;
				case MsgInfor.SERVER_REPORT_USER:
					dealReportUser(jo);
					break;
				case MsgInfor.SERVER_REPORT_BLOG:
					dealReportBlog(jo);
					break;
				case MsgInfor.SERVER_DELETE_GROUP_MESSAGE:
					// 删除群消息
					dealDeleteMsg(jo);
					break;
				case MsgInfor.SERVER_GET_GROUP_MESSAGE_IDS:
					dealGetGroupMessageIds(jo);
					break;
				case MsgInfor.SERVER_GROUP_WARN:
					dealUserWarn(jo);
					break;
				case MsgInfor.SERVER_GET_USERSINFO:
					dealMoreUser(jo);
					break;
				case MsgInfor.SERVER_SYSTEM:
					dealSystemMsg(jo);
					break;
				case MsgInfor.SERVER_KEY_MSG:
					try {
						d = jo.getInt("d");
						int ver = jo.optInt("lastv");
						String url = jo.optString("cliurl");
						if (d == 0) {
							if (ver > Utils.appver && !Utils.isNull(url)) {
								if (Utils.debug)
									Log.i(TAG, "url" + url);
								needSendUpApp = true;
							} else {
								needSendUpApp = false;
							}
						} else if (d == 4) {
							ServerRsp serverRsp = new ServerRsp();
							serverRsp
									.setStatusCode(StatusCode.THE_BLACK_DEVICE);
							Intent intent = new Intent(
									Constants.INTENT_ACTION_SYSTEM_MSG);
							intent.putExtra(Constants.EXTRA_SERVER_RSP_KEY,
									serverRsp.getBundle());
							intent.setExtrasClassLoader(ServerRsp.class
									.getClassLoader());
							mSess.getContext().sendBroadcast(intent);
						} else {
							// 强制升级
							// editorMeme.putInt(CommonData.UPDATA_VER, ver);
							// editorMeme.putString(CommonData.UPDATA_URL, url);
							// editorMeme.commit();
							mSess.mPrefMeme.updata_ver.setVal(ver);
							mSess.mPrefMeme.updata_url.setVal(url).commit();
							Intent forceIntent = new Intent(
									Constants.INTENT_ACTION_FORCE_VERSION_UPDATE);
							mSess.getContext().sendBroadcast(forceIntent);
						}
					} catch (JSONException e1) {
						if (Utils.debug)
							e1.printStackTrace();
					}
					break;
				case MsgInfor.SERVER_INPUT:
					break;
				case MsgInfor.SERVER_CHECK_VER:
					try {
						int ver = jo.getInt("ver");
						String url = jo.getString("url");
						JSONArray ja = jo.getJSONArray("log");
						StringBuffer sb = new StringBuffer().append("");
						if (ja != null) {
							for (int i = 0; i < ja.length(); i++) {
								String str = ja.getString(i);
								sb.append(str);
							}
						}
						ServerRsp verRsp = new ServerRsp();

						if (ver > Utils.appver && !Utils.isNull(url)) {
							verRsp.setStatusCode(StatusCode.RSP_OK);
							if (Utils.debug) {
								Log.i(TAG, "appver-" + Integer.toString(ver));
								Log.i(TAG, "appver-" + url);
								Log.i(TAG, "appver-" + sb.toString());
							}
							// editorMeme.putInt(CommonData.UPDATA_VER, ver);
							// editorMeme.putString(CommonData.UPDATA_URL, url);
							// editorMeme.putString(CommonData.UPDATA_LOG,
							// sb.toString());
							// editorMeme.commit();
							mSess.mPrefMeme.updata_ver.setVal(ver);
							mSess.mPrefMeme.updata_url.setVal(url);
							mSess.mPrefMeme.updata_log.setVal(sb.toString())
									.commit();
							if (needSendUpApp) {
								needSendUpApp = false;
								broadcastMsg(
										Constants.INTENT_ACTION_AUTO_VERSION_UPDATE,
										verRsp);
							} else {
								broadcastMsg(
										Constants.INTENT_ACTION_VERSION_UPDATE,
										verRsp);
							}
						} else {
							verRsp.setStatusCode(StatusCode.VERSION_NO_UP);
							broadcastMsg(
									Constants.INTENT_ACTION_VERSION_UPDATE,
									verRsp);
						}
					} catch (JSONException e3) {
						e3.printStackTrace();
					}

					break;
				case MsgInfor.SERVER_UPMSG:
					mSess.getSocketHelper().dealSingleMessageResponse(jo);
					break;
				case MsgInfor.SERVER_BLOCK:
					dealBlockUser(jo);
					break;
				case MsgInfor.SERVER_DOWNMSG:
					mSess.getSocketHelper().dealSingleReceiveMessage(jo, false);
					break;
				case MsgInfor.SERVER_MSG_DOWN_QUN:
					mSess.getSocketHelper()
							.dealGroupReceiveMessage(jo, 2010, 0);
					break;
				case MsgInfor.SERVER_JOIN_OUT_GROUP:
					mSess.getSocketHelper().dealJoinOutGroup(jo);
					break;
				case MsgInfor.SERVER_SHUTUP_NOTICE:
					mSess.getSocketHelper().dealShutupNotice(jo);
					break;
				case MsgInfor.SERVER_GET_GROUP_NOTICE:
					mSess.getSocketHelper().dealPersonGroupNotice(jo);
					break;
				case MsgInfor.SERVER_GET_BAN_LIST:
					// 处理禁言列表
					dealGroupBanList(jo);
					break;
				case MsgInfor.SERVER_GET_GROUP_LAST_WEEK_STARS:
					// 服务器返回对应群上周之星
					dealServerLastWeekStars(jo);
					break;
				case MsgInfor.SERVER_SMS_IDENTIFICATION: {
					ServerRsp serverRsp = new ServerRsp();
					try {
						String smsNumber = jo.getString("o");
						String smsContent = jo.getString("sn");
						if (!"".equals(smsNumber)) {
							long foreign_check_time = System
									.currentTimeMillis();
							// editorMeme = prefsMeme.edit();
							// editorMeme.putLong(CommonData.FOREIGN_CHECK_TIME,
							// foreign_check_time);
							// editorMeme.putString(CommonData.FOREIGN_CHECK_SMS_NUMBER,
							// smsNumber);
							// editorMeme.putString(CommonData.FOREIGN_CHECK_SMS_TEXT,
							// smsContent);
							// editorMeme.commit();
							mSess.mPrefMeme.foreign_check_time
									.setVal(foreign_check_time);
							mSess.mPrefMeme.foreign_check_sms_number
									.setVal(smsNumber);
							mSess.mPrefMeme.foreign_check_sms_text.setVal(
									smsContent).commit();
							if (Utils.checkSIMCardState(mSess.getContext())) {
								boolean sendState = Utils.sendSmsMessage(
										smsNumber, smsContent);
								if (sendState) {
									// editorMeme = prefsMeme.edit();
									// editorMeme.putString(CommonData.TELEPHONE,
									// "");
									// editorMeme.putInt(CommonData.TEL_BIND_STATE,
									// CommonData.TEL_NO_CHECK);
									// editorMeme.putBoolean(CommonData.IS_BIND_PHONE,
									// false);
									// editorMeme.commit();

									mSess.mPrefMeme.telephone.setVal("");
									mSess.mPrefMeme.tel_bind_state
											.setVal(PrefsMeme.TEL_NO_CHECK);
									mSess.mPrefMeme.is_bind_phone.setVal(false)
											.commit();

								} else {
									// editorMeme = prefsMeme.edit();
									// editorMeme.putString(CommonData.TELEPHONE,
									// "");
									// editorMeme.putInt(CommonData.TEL_BIND_STATE,
									// CommonData.TEL_BIND_FAILED);
									// editorMeme.putBoolean(CommonData.IS_BIND_PHONE,
									// false);
									// editorMeme.commit();
									mSess.mPrefMeme.telephone.setVal("");
									mSess.mPrefMeme.tel_bind_state
											.setVal(PrefsMeme.TEL_BIND_FAILED);
									mSess.mPrefMeme.is_bind_phone.setVal(false)
											.commit();
								}
								// TX.tm.reloadTXMe();// ///

								mSess.mMsgHandler.onReloadTXMe();

								serverRsp.setStatusCode(StatusCode.RSP_OK);
								serverRsp.putBoolean("sendState", sendState);
							} else {
								serverRsp.setStatusCode(StatusCode.OPT_FAILED);
							}

						}
					} catch (JSONException e4) {
						e4.printStackTrace();
					}
					broadcastMsg(
							Constants.INTENT_ACTION_SMS_IDENTIFICATION_RSP,
							serverRsp);

					break;
				}
				case MsgInfor.SERVER_CLENT_RECEIPT:
					mSess.getSocketHelper().dealSingleMessageReceipt(jo);
					break;
				case MsgInfor.SERVER_OFFLINEGET:
					mSess.getSocketHelper().dealSinleOfflineMessage(jo);
					break;
				case MsgInfor.SERVER_GET_OFFLINE_MEG_QUN:
					mSess.getSocketHelper().dealGroupOfflineMessage(jo);
					break;
				case MsgInfor.SERVER_PUBLIC_LAST_MESSAGE:
					// 处理服务器返回的聊天室最新十条消息
					boolean msgResult = mSess.getSocketHelper()
							.dealGroupLastMessage(jo);
					if (msgResult) {

						// 最新十条消息添加完毕后，开始获取上周活跃之星
						try {
							int groupId = jo.getInt("gid");
							TxGroup txGroup = TxGroup.getTxGroup(
									mSess.getContentResolver(), groupId);
							if (txGroup.isOfficialGroup()) {
								// 每次都强制去拉取上周之星，防止有人被封禁不能出现在列表中
								// 通过本地版本号去取上周之星
								int ver = getLaskWeekStarsVer(groupId);
								mSess.getSocketHelper()
										.sendGetGroupLastWeekStars(groupId, ver);
							}
						} catch (JSONException e) {
							if (Utils.debug)
								Log.e(TAG, "解析Json获取聊天室id异常：", e);
						}
					}
					break;
				case MsgInfor.SERVER_OFFLINERECEIPT:
					mSess.getSocketHelper().dealSingleOfflineReceipt(jo);
					break;
				case MsgInfor.SERVER_CONTACTSUP:
					try {
						int y = jo.getInt("y");
						if (Utils.debug)
							Log.i(TAG, y + "+++++++++++++++++size+++++++++++++"
									+ mSess.getSocketHelper().newPhones.size());
						if (Utils.debug)
							Log.i(TAG, y + "++++++++++++++++++++++++++++++"
									+ mSess.getSocketHelper().adcot);
						if (y == 0) {
							// try {
							// Thread.sleep(500);
							// } catch (InterruptedException e) {
							// }
							if (mSess.getSocketHelper().newPhones != null) {
								int s = mSess.getSocketHelper().newPhones
										.size();
								if (s > 10 && mSess.getSocketHelper().adcot < s) {
									Timer outtime = new Timer();
									outtime.schedule(new TimerTask() {
										public void run() {
											mSess.getSocketHelper()
													.sendAddressBook1(
															mSess.getSocketHelper().newPhones);
											int len = 0;
											for (int i = mSess
													.getSocketHelper().adcot - 1; i > 0; i--) {
												if (len < 10) {
													TxDBContentProvider
															.updateAid(
																	cr,
																	mSess.getSocketHelper().newPhones
																			.get(i));
													len++;
												}
											}
										}
									}, 30000);
								} else {
									for (int i = 0; i < s; i++) {
										TxDBContentProvider
												.updateAid(
														cr,
														mSess.getSocketHelper().newPhones
																.get(i));
									}
									mSess.getSocketHelper().upAddr = false;
									// editorMeme.putString(CommonData.CONTACTSUP,
									// CommonData.CONTACTSUP);
									// editorMeme.commit();
									mSess.mPrefMeme.contactsup.setVal(
											PrefsMeme.CONTACTSUP).commit();
									mSess.getSocketHelper().newPhones.clear();
									mSess.getSocketHelper().newPhones = null;
								}
							}
						}
					} catch (JSONException e) {
						if (Utils.debug)
							e.printStackTrace();
					}
					break;
				case MsgInfor.SERVER_USERNAMECHARGE: {
					/**
					 * 许春会修改
					 */
					ServerRsp serverRsp = new ServerRsp();
					int dnn;// 昵称
					int dsign;// 签名
					int djob;// 职业
					int dlang;
					int dhobby;
					int dnarea;
					if (Utils.debug) {
						Log.i(TAG, "17号协议回包-----------------------------");
					}
					try {
						d = jo.getInt("d");

						if (d == 0) {

							dnn = jo.getInt("dnn");
							dsign = jo.getInt("dsign");
							djob = jo.getInt("djob");
							dlang = jo.getInt("dlang");
							dhobby = jo.getInt("dnhobby");
							dnarea = jo.getInt("dnarea");
							if (dnn == 0) {
								serverRsp.putStatusCode(Tx.DISPLAY_NAME,
										StatusCode.CHANGE_NAME_SUCCESS);

								// 获取最新的个人信息，看是否升等级
								mSess.getSocketHelper()
										.getNewUserinforForLevel();
							} else if (dnn == 2) {
								serverRsp.putStatusCode(Tx.DISPLAY_NAME,
										StatusCode.LOGIN_NAME_FAIELD);
							} else if (dnn == 3) {
								String fbret = jo.getString("fbret");
								if (fbret != null) {
									serverRsp.putStatusCode(Tx.DISPLAY_NAME,
											StatusCode.CHANGE_NAME_FAILED);
									serverRsp.getBundle().putString("fbret",
											fbret);

								}
							} else if (dnn == 1) {
								serverRsp.putStatusCode(Tx.DISPLAY_NAME,
										StatusCode.CHANGE_NAME_NOTCHANGE);

							}
							if (dsign == 0) {
								serverRsp.putStatusCode(Tx.TX_SIGN,
										StatusCode.CHANGE_SIGN_SUCCESS);

								// 获取最新的个人信息，看是否升等级
								mSess.getSocketHelper()
										.getNewUserinforForLevel();
							} else if (dsign == 1) {
								serverRsp.putStatusCode(Tx.TX_SIGN,
										StatusCode.CHANGE_SIGN_NOTCHANGE);
							} else if (dsign == 2) {
								serverRsp.putStatusCode(Tx.TX_SIGN,
										StatusCode.CHANGE_SIGN_NOTRULE);
							} else if (dsign == 3) {
								String fbret = jo.getString("fbret");

								if (fbret != null) {
									serverRsp.putStatusCode(Tx.TX_SIGN,
											StatusCode.CHANGE_SIGN_FAILED);
									serverRsp.getBundle().putString("fbret",
											fbret);

								}
							}

							if (dhobby == 0) {
								serverRsp.putStatusCode(Tx.HOBBY,
										StatusCode.CHANGE_HOBBY_SUCCESS);

								// 获取最新的个人信息，看是否升等级
								mSess.getSocketHelper()
										.getNewUserinforForLevel();
							} else if (dhobby == 1) {
								serverRsp.putStatusCode(Tx.HOBBY,
										StatusCode.CHANGE_HOBBY_NOTCHANGE);
							} else if (dhobby == 2) {
								serverRsp.putStatusCode(Tx.HOBBY,
										StatusCode.CHANGE_HOBBY_NOTRULE);
							} else if (dhobby == 3) {
								String fbret = jo.getString("fbret");
								if (fbret != null) {
									serverRsp.putStatusCode(Tx.HOBBY,
											StatusCode.CHANGE_HOBBY_FAILED);
									serverRsp.getBundle().putString("fbret",
											fbret);

								}
							}

							if (djob == 0) {
								serverRsp.putStatusCode(Tx.PROFESSION,
										StatusCode.CHANGE_JOB_SUCCESS);

								// 获取最新的个人信息，看是否升等级
								mSess.getSocketHelper()
										.getNewUserinforForLevel();
							} else if (djob == 2) {
								serverRsp.putStatusCode(Tx.PROFESSION,
										StatusCode.CHANGE_JOB_NOTRULE);
							} else if (djob == 1) {
								serverRsp.putStatusCode(Tx.PROFESSION,
										StatusCode.CHANGE_JOB_NOTCHANGE);
							} else if (djob == 3) {
								String fbret = jo.getString("fbret");
								if (fbret != null) {
									serverRsp.putStatusCode(Tx.PROFESSION,
											StatusCode.CHANGE_JOB_FAILED);
									serverRsp.getBundle().putString("fbret",
											fbret);

								}

							}
							if (dlang == 0) {
								serverRsp.putStatusCode(Tx.LANGUAGES,
										StatusCode.CHANGE_LANG_SUCCESS);

								// 获取最新的个人信息，看是否升等级
								mSess.getSocketHelper()
										.getNewUserinforForLevel();
							} else if (dlang == 1) {
								serverRsp.putStatusCode(Tx.LANGUAGES,
										StatusCode.CHANGE_LANG_NOTCHANGE);

							} else if (dlang == 3) {
								serverRsp.putStatusCode(Tx.LANGUAGES,
										StatusCode.CHANGE_LANG_FAILED);
							}
							if (dnarea == 0) {
								serverRsp.putStatusCode(Tx.HOME,
										StatusCode.CHANGE_AREA_SUCCESS);

								// 获取最新的个人信息，看是否升等级
								mSess.getSocketHelper()
										.getNewUserinforForLevel();
							} else if (dnarea == 1) {
								serverRsp.putStatusCode(Tx.HOME,
										StatusCode.CHANGE_AREA_NOTCHANGE);
							} else if (dnarea == 3) {
								serverRsp.putStatusCode(Tx.HOME,
										StatusCode.CHANGE_AREA_FAILED);
							}

						} else {
							serverRsp.setStatusCode(StatusCode.OPT_FAILED);
						}

					} catch (JSONException e1) {
						e1.printStackTrace();
					}

					broadcastMsg(Constants.INTENT_ACTION_CHANGE_USERNAME_RSP,
							serverRsp);
					break;
				}
				case MsgInfor.SERVER_INFO_QUN:
					mSess.getSocketHelper().dealGroupInfo(jo);
					break;
				case MsgInfor.SERVER_DEAL_QUN: {
					try {
						d = jo.getInt("d");
						ServerRsp serverRsp = new ServerRsp();
						long gid = jo.getLong("gid");
						int op = jo.getInt("op");
						// TxGroup group = txdata.getTxGroupByGroupId4DB(gid);
						TxGroup group = TxGroup.getTxGroup(
								mSess.getContentResolver(), gid);
						String act = "";
						if (op == 0) {
							// 添加成员的结果
							act = Constants.INTENT_ACTION_ADD_GROUP_MEMBER;
						} else if (op == 1) {
							// 删除成员的结果
							act = Constants.INTENT_ACTION_DEL_GROUP_MEMBER;
						}

						if (d == 0) {

							int gver = jo.getInt("gver");// 群聊重要信息更改版本
							if (gver == 0) {
								if (group != null) {
									if (!TxGroup.isPublicGroup(group)) {// TODO
																		// 为什么要加上非公开聊天室这个限制？
																		// 2014.03.17
																		// shc
										group.group_tx_state = TxDB.QU_TX_STATE_OUT;
									}
									ContentValues values = new ContentValues();
									values.put(TxDB.Qun.QU_TX_STATE,
											group.group_tx_state);
									TxGroup.updateTxGroup(
											mSess.getContentResolver(),
											group.group_id, values);
								}
								serverRsp
										.setStatusCode(StatusCode.GROUP_DISSOLVED);
								broadcastMsg(act, serverRsp);
								return;
							}
							ArrayList<String> idsList = new ArrayList<String>();
							JSONArray array = jo.getJSONArray("idlist");
							boolean isContainMe = false;
							for (int i = 0; i < array.length(); i++) {
								JSONObject joo = array.getJSONObject(i);
								long uid = joo.getLong("uid");
								if (uid == mSess.getUserid()) {
									isContainMe = true;
								}
								idsList.add("" + uid);
							}
							if ((group == null)
									|| (group != null && group.group_tx_state == TxDB.QU_TX_STATE_OUT)) {
								serverRsp
										.setStatusCode(StatusCode.GROUP_NO_EXIST);
							}
							serverRsp.setStatusCode(StatusCode.RSP_OK);
							if (op == 0) {
								for (String uid : idsList) {
									group = group.changeMembers(group,
											Long.valueOf(uid), true);
								}
								serverRsp.getBundle().putStringArrayList("ids",
										idsList);
							} else {
								for (String uid : idsList) {
									group = group.changeMembers(group,
											Long.valueOf(uid), false);
								}
								serverRsp.getBundle().putStringArrayList("ids",
										idsList);
								if (isContainMe) {
									group.group_tx_state = TxDB.QU_TX_STATE_OUT;
									serverRsp
											.setStatusCode(StatusCode.GROUP_LEAVE);
								}
							}
							// TxGroup.saveTxGroupToDB(group, txdata);//用下面的方法
							// 2014.01.23 shc
							ContentValues values = new ContentValues();
							values.put(TxDB.Qun.QU_TX_IDS, group.group_tx_ids);
							values.put(TxDB.Qun.ALL_NUM, group.group_all_num);
							values.put(TxDB.Qun.QU_TX_STATE,
									group.group_tx_state);
							TxGroup.updateTxGroup(mSess.getContentResolver(),
									group.group_id, values);
							if (gver != group.group_ver) {
								// SocketHelper.getSocketHelper().sendGetGroup(gid);
							}
						} else if (d == 1) {
							serverRsp
									.setStatusCode(StatusCode.GROUP_MEMBER_SIZE_INVALID);
						} else if (d == 2) {
							serverRsp
									.setStatusCode(StatusCode.GROUP_MEMBER_OPT_NO_PERMISSION);
						} else if (d == 3) {
							serverRsp.setStatusCode(StatusCode.OPT_FAILED);
						} else if (d == 4) {
							serverRsp.setStatusCode(StatusCode.GROUP_NO_EXIST);
						} else if (d == 5) {
							serverRsp.setStatusCode(StatusCode.USER_IN_BLACK);
						}
						broadcastMsg(act, serverRsp);

					} catch (JSONException e3) {
						e3.printStackTrace();
					}
					break;
				}
				// case MsgInfor.SERVER_USERPHONCHARGE:
				// try {
				// d = jo.getInt("d");
				// if (d == 1) {
				// } else if (d == 0) {
				// }
				// } catch (JSONException e1) {
				// e1.printStackTrace();
				// }
				//
				// break;

				case MsgInfor.SERVER_FRIENDS_IDS:
					try {
						if (Utils.debug)
							Log.i(TAG, "返回搜索好友id集合：" + jo.toString());
						JSONArray ja = jo.getJSONArray("frdlist");
						int len = ja.length();

						ArrayList<TxInfor> tbtxIds = new ArrayList<TxInfor>();
						TxInfor tinfor = null;
						for (int i = 0; i < len; i++) {
							int friendId = ((JSONObject) ja.get(i))
									.getInt("id");
							int attr = ((JSONObject) ja.get(i)).getInt("attr");
							idlist.add((long) friendId);
							// TX.tm.addTBTXId(friendId, attr);
							tinfor = new TxInfor(friendId, TxInfor.TX_TYPE_TB);
							tinfor.setStarFriend(attr);
							tbtxIds.add(tinfor);
						}

						mSess.mMsgHandler.onServerFriendsIds_121(tbtxIds);

						if (jo.getInt("eof") == 0) {
							p++;
							mSess.getSocketHelper().sendContactsId(p);
						} else {
							p = 0;
							// dealGetTBinfor();
							// 现在改为先获取好友备注名，再获取好友详细资料，这样在存储好友资料的时候就可以去查找好友备注名
							// 批量获取好友备注名
							mSess.getSocketHelper().sendGetRemarkNames();
						}
					} catch (JSONException e) {
						if (Utils.debug) {
							Log.i(TAG, "新协议---解析好友id异常", e);
						}
					}
					break;
				case MsgInfor.SERVER_CONTACTSGET:
					// 返回联系人信息列表，此协议号已被废弃
					break;
				case MsgInfor.SERVER_SEARCH:
					try {
						ServerRsp serverRsp = new ServerRsp();
						Intent in = new Intent(
								Constants.INTENT_ACTION_FIND_FRIEND);
						d = jo.getInt("d");
						if (d == 0) {
							serverRsp.setStatusCode(StatusCode.RSP_OK);
							long id = jo.getLong("i");
							String nickname = jo.getString("n");
							String email = jo.getString("e");
							String phone = jo.getString("o");
							phone = Utils.filterNumber(phone);
							boolean obd = jo.getBoolean("obd");// 是否绑定手机
							boolean ebd = jo.getBoolean("ebd");// 是否绑定email
							int avatarver = jo.optInt("avatarver", 0);
							String avatar_url = jo.getString("avatarurl");
							int sex = jo.getInt("sex");
							String area = StringUtils.jsonArray2Str(jo
									.getJSONArray("narea"));
							String sign = jo.getString("sign");
							int birthday = jo.getInt("birthday");
							int blood = jo.getInt("blood");
							String hobby = StringUtils.jsonArray2Str(jo
									.getJSONArray("nhobby"));
							String lang = StringUtils.jsonArray2Str(jo
									.getJSONArray("lang"));
							String job = jo.getString("job");
							int level = Utils.level;
							if (Utils.lev)
								level = jo.getInt("level");// 个人等级

							TX tx = new TX();
							tx.setPartnerId(id);
							tx.setNick_name(nickname);
							tx.setEmail(email);
							tx.setPhoneBind(obd);
							tx.setEmailBind(ebd);
							tx.setArea(area);
							tx.setSex(sex);
							tx.setLanguages(lang);
							tx.setAvatar_url(avatar_url);
							tx.setAvatar_ver(avatarver);
							tx.setSign(sign);
							tx.setBirthday(birthday);
							tx.setBloodType(blood);
							tx.setHobby(hobby);
							tx.setJob(job);
							tx.setPhone(phone);
							tx.setLevel(level);

							mSess.mMsgHandler.onServerSearch_25(tx);

							Cursor c = cr.query(TxDB.Messages.CONTENT_URI,
									new String[] { TxDB.Messages.MSG_ID },
									TxDB.Messages.TCARD_ID + "=? ",
									new String[] { "" + id }, null);
							if (c != null) {
								if (c.moveToFirst()) {
									TXMessage tmsg = new TXMessage();
									tmsg.tcard_avatar_url = avatar_url;
									tmsg.tcard_name = nickname;
									tmsg.tcard_phone = phone;
									tmsg.tcard_sex = sex;
									tmsg.tcard_sign = sign;
									tmsg.msg_id = c.getLong(0);
									TXMessage.updateTcardTXMessage(cr, tmsg);
								}
								c.close();
							}
							// in.putExtra(TX.EXTRA_TX, tx.getBundle());
							in.putExtra(TX.EXTRA_TX, id);
							mSess.getSocketHelper().getAlbum(id, 0);

						} else if (d == 1) {
							serverRsp.setStatusCode(StatusCode.FIND_NO_FRIEND);
						} else if (d == 2) {
							serverRsp.setStatusCode(StatusCode.OPT_FAILED);
						}
						in.putExtra(Constants.EXTRA_SERVER_RSP_KEY,
								serverRsp.getBundle());
						mSess.getContext().sendBroadcast(in);
					} catch (JSONException e3) {
						if (Utils.debug) {
							Log.e(TAG, "解析搜索好友的json异常", e3);
						}
					}
					break;
				case MsgInfor.SERVER_PASSWORDGET: {
					ServerRsp serverRsp = new ServerRsp();
					try {
						d = jo.getInt("d");
						mSess.getSocketHelper().recovery();
						if (d == 0) {
							serverRsp.setStatusCode(StatusCode.RSP_OK);
						} else if (d == 1) {
							serverRsp.setStatusCode(StatusCode.MOBILE_INVALID);
						} else if (d == 2) {
							serverRsp
									.setStatusCode(StatusCode.MOBILE_NO_BINDED);
						} else if (d == 3) {
							serverRsp.setStatusCode(StatusCode.EMAIL_INVALID);
						} else if (d == 4) {
							serverRsp.setStatusCode(StatusCode.EMAIL_NO_BINDED);
						} else if (d == 5) {
							serverRsp.setStatusCode(StatusCode.OPT_FAILED);
						}
						broadcastMsg(Constants.INTENT_ACTION_GET_PASSWORD,
								serverRsp);
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
					break;
				}
				case MsgInfor.SERVER_MSGREAD:
					mSess.getSocketHelper().dealSingleReadList(jo);
					break;
				case MsgInfor.SERVER_CHARGE_PASSWORDGET: {
					ServerRsp serverRsp = new ServerRsp();
					try {
						d = jo.getInt("d");
						if (0 == d) {
							serverRsp.setStatusCode(StatusCode.RSP_OK);
						} else if (1 == d) {
							serverRsp.setStatusCode(StatusCode.PWD_INVALID);
						} else if (3 == d) {
							serverRsp.setStatusCode(StatusCode.REQ_THAN_LIMIT);
						} else if (5 == d) {
							serverRsp.setStatusCode(StatusCode.OPT_FAILED);
						}
					} catch (JSONException e) {
						if (Utils.debug)
							e.printStackTrace();
					}

					broadcastMsg(Constants.INTENT_ACTION_CHANGE_PWD_RSP,
							serverRsp);
					break;
				}
				case MsgInfor.SERVER_USERINFRO: {
					try {
						final int id = jo.getInt("i");
						if (id != 0) {
							TX txInfor = new TX();
							txInfor.setPartnerId(id);

							// 该协议可以返回的所有字段
							boolean phonebd = false;
							boolean emailbd = false;
							int friendver = 0;
							String area;
							String hobby;
							String language = "";
							int alubmVver = 0;
							int inforVer = 0;
							int isop = 0;

							String phone = jo.getString("o");
							String nickname = jo.getString("nn");
							String email = jo.getString("e");
							String avatar_url = jo.getString("avatarurl");
							int avatarmd5 = jo.optInt("avatarver", 0);
							int sex = jo.getInt("sex");
							int birthday = jo.getInt("birthday");// 没有生日返回0，有生日返回：19880101
							String job = jo.getString("job");
							int blood = jo.getInt("blood");
							String sign = jo.getString("sign");
							area = StringUtils.jsonArray2Str(jo
									.getJSONArray("narea"));
							int level = Utils.level;
							if (Utils.lev)
								level = jo.getInt("level");// 个人等级

							if (id == mSess.getUserid()) {
								// 如果是本人的详细信息

								phonebd = jo.optBoolean("obd");
								emailbd = jo.optBoolean("ebd");
								friendver = jo.optInt("friendver");
								hobby = StringUtils.jsonArray2Str(jo
										.getJSONArray("nhobby"));
								language = StringUtils.jsonArray2Str(jo
										.getJSONArray("lang"));
								alubmVver = jo.getInt("aver");

							} else if (id == TX.TUIXIN_MAN) {
								// 神聊客服的详细信息

								// area = jo.getString("area");
								hobby = jo.getString("hobby");
								if (Utils.debug) {
									Log.i(TAG, "msgHelper-dealMsg-->" + id
											+ "地区信息为：" + area);
								}

							} else if (id == TX.TUIXIN_FRIEND) {
								// 好友管家的详细信息

								// area = jo.getString("area");
								hobby = jo.getString("hobby");
								isop = jo.getInt("auth");

							} else {
								// 用户指定搜索的其它神聊用户的详细信息

								phonebd = jo.optBoolean("obd");
								emailbd = jo.optBoolean("ebd");
								friendver = jo.optInt("friendver");
								// int avatar =
								// mSess.mPrefMeme.avatarver.getVal();
								// area = StringUtils.jsonArray2Str(jo
								// .getJSONArray("narea"));
								hobby = StringUtils.jsonArray2Str(jo
										.getJSONArray("nhobby"));
								language = StringUtils.jsonArray2Str(jo
										.getJSONArray("lang"));
								alubmVver = jo.getInt("aver");// 个人相册信息版本号,何用？

								inforVer = jo.getInt("iver");// 个人信息版本号
								isop = jo.getInt("auth");

							}

							txInfor.setNick_name(nickname);
							txInfor.setPhone(phone);
							txInfor.setEmail(email);
							txInfor.setPhoneBind(phonebd);
							txInfor.setEmailBind(emailbd);
							txInfor.setFriend_ver(friendver);
							txInfor.setAvatar_ver(avatarmd5);
							txInfor.setAvatar_url(avatar_url);
							txInfor.setSex(sex);
							txInfor.setSign(sign);
							txInfor.setBirthday(birthday);// 没有生日返回0，有生日返回int型：19880101
							txInfor.setBloodType(blood);
							txInfor.setJob(job);
							txInfor.setHobby(hobby);
							txInfor.setArea(area);
							txInfor.setLanguages(language);
							txInfor.setAlbumVer(alubmVver);
							txInfor.setInfoVer(inforVer);
							txInfor.setIsop(isop);
							txInfor.setLevel(level);

							mSess.mMsgHandler.onServerUserinfor_33(txInfor);

						}

					} catch (JSONException e) {
						if (Utils.debug) {
							Log.e(TAG, "收到用户详细信息解析json时异常", e);
						}
					}

					break;
				}
				case MsgInfor.SERVER_DEL_PARTNER: {
					// 这里的处理没什么用，好友TuixinContactActivity的处理才是真正对删除好友的处理
					// 2014.01.20 shc
					// ServerRsp serverRsp = new ServerRsp();
					// try {
					// d = jo.getInt("d");
					// if (d == 0) {
					// String uc = jo.getString("friendver");
					// // editorMeme.putString(CommonData.UC, uc);
					// // editorMeme.commit();
					// //
					// mSess.mPrefMeme.uc.setVal(uc).commit();//只有写入操作，没有调用操作，暂注掉
					// 2014.01.20
					// serverRsp.setStatusCode(StatusCode.RSP_OK);
					// } else {
					// serverRsp.setStatusCode(StatusCode.OPT_FAILED);
					// }
					//
					// } catch (JSONException e1) {
					// e1.printStackTrace();
					// }
					// broadcastMsg(Constants.INTENT_ACTION_DEL_BUDDY_RSP,
					// serverRsp);

					break;
				}
				case MsgInfor.SERVER_ADD_PARTNER: {
					// 请求加为好友时，服务器的返回。例如“请求是否已经发出”
					ServerRsp serverRsp = new ServerRsp();
					try {
						d = jo.getInt("d");
						if (d == 0) {
							serverRsp.setStatusCode(StatusCode.RSP_OK);
							int uid = jo.getInt("uid");
							serverRsp.putInt("uid", uid);
							boolean bf = jo.getBoolean("bf");
							if (bf) {
								serverRsp.putBoolean("bf", bf);

								// 只有陌生人能被添加为好友
								TX ttx = mSess.getTxMgr().getTx(uid);
								if (ttx != null) {
									if (Utils.debug)
										Log.i(TAG,
												"SERVER_ADD_PARTNER"
														+ ttx.toString());

									mSess.mMsgHandler
											.onServerAddPartner_38(ttx);

								}

							}
						} else if (d == 1) {
							serverRsp.setStatusCode(StatusCode.OPT_FAILED);
						} else if (d == 2) {
							serverRsp
									.setStatusCode(StatusCode.BUDDY_THAN_LIMIT);
						} else if (d == 3) {
							serverRsp
									.setStatusCode(StatusCode.REFUSE_FRIEND_REQ);
						}
						broadcastMsg(Constants.INTENT_ACTION_ADD_BUDDY,
								serverRsp);
					} catch (JSONException e1) {
						if (Utils.debug)
							Log.e(TAG, "处理服务器添加好友成功返回json异常", e1);
					}
					break;
				}
				case MsgInfor.SERVER_AGREE_MSG: {
					ServerRsp serverRsp = new ServerRsp();
					try {
						d = jo.getInt("d");
						if (d == 0) {
							// 添加好友成功
							serverRsp.setStatusCode(StatusCode.RSP_OK);
							int uid = jo.getInt("uid");
							serverRsp.putInt("uid", uid);

							// 下面的代码就是改造上面被注释的
							// TX tx = TX.tm.getTx(uid);
							TX tx = mSess.getTxMgr().getTx(uid);
							if (tx != null) {
								if (Utils.debug)
									Log.i(TAG,
											"SERVER_AGREE_MSG" + tx.toString());

								mSess.mMsgHandler.onServerAgreeMsg_42(tx);

							}

						} else if (d == 1) {
							serverRsp.setStatusCode(StatusCode.OPT_FAILED);
						} else if (d == 3) {
							serverRsp
									.setStatusCode(StatusCode.REFUSE_FRIEND_REQ);
						}
						broadcastMsg(Constants.INTENT_ACTION_AGREE_ADD_BUDDY,
								serverRsp);
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
					break;
				}
				case MsgInfor.SERVER_SYSTEM_MSG:
					try {
						if (jo.getInt("eof") == 0) {
							// String id =
							// prefsMeme.getString(CommonData.USER_ID, "");
							String id = mSess.mPrefMeme.user_id.getVal();
							if (!"".equals(id)) {
								mSess.getSocketHelper()
										.sendGetOfflineSystemMsg(
												Integer.parseInt(id));
							}
						}
					} catch (JSONException e2) {
						e2.printStackTrace();
					}
					try {
						JSONArray ja = jo.getJSONArray("ms");
						dealSystemMsgs(ja);
					} catch (JSONException e) {
						if (Utils.debug)
							e.printStackTrace();
					}
					broadcastMsg("msg", "flushmsg");
					break;
				case MsgInfor.SERVER_CHECK_EMAIL: {
					ServerRsp serverRsp = new ServerRsp();
					try {
						d = jo.getInt("d");
						if (0 == d) {
							serverRsp.setStatusCode(StatusCode.RSP_OK);

							// 获取最新的个人信息，看是否升等级
							mSess.getSocketHelper().getNewUserinforForLevel();
						} else if (1 == d) {
							serverRsp.setStatusCode(StatusCode.EMAIL_INVALID);
						} else if (2 == d) {
							serverRsp
									.setStatusCode(StatusCode.OTHER_BIND_THIS_EMAIL);
						} else if (5 == d) {
							serverRsp
									.setStatusCode(StatusCode.EMAIL_HAS_BINDED);
						} else {
							serverRsp.setStatusCode(StatusCode.SERVER_BUSY);
						}
					} catch (JSONException e) {
						if (Utils.debug)
							e.printStackTrace();
					}
					broadcastMsg(Constants.INTENT_ACTION_BIND_EMAIL_RSP,
							serverRsp);

					break;

				}
				case MsgInfor.SERVER_QUERY_PHONE_EMAIL: {
					ServerRsp serverRsp = new ServerRsp();
					try {
						d = jo.getInt("d");
						if (0 == d) {
							/*
							 * p:电话号码 pv:电话验证状态，0为未验证，1为已验证 ml:邮箱 mv:邮箱验证状态
							 * 0为未验证，1为已验证
							 */
							String ml = jo.getString("ml");
							int mv = jo.getInt("mv");
							boolean ml_state = (mv == 1) ? true : false;
							serverRsp.putString("ml", ml);
							serverRsp.putBoolean("mlState", ml_state);
						} else if (1 == d) {
							serverRsp.setStatusCode(StatusCode.OPT_FAILED);
						} else if (2 == d) {
							serverRsp.setStatusCode(StatusCode.SERVER_BUSY);
						}
					} catch (JSONException e) {
						if (Utils.debug)
							e.printStackTrace();
					}
					broadcastMsg(
							Constants.INTENT_ACTION_MOBILE_EMAIL_STATE_RSP,
							serverRsp);

					break;
				}
				case MsgInfor.SERVER_BIND_PHONE: {
					ServerRsp serverRsp = new ServerRsp();
					try {
						d = jo.getInt("d");
						if (0 == d) {
							serverRsp.setStatusCode(StatusCode.RSP_OK);
						} else if (1 == d) {
							serverRsp.setStatusCode(StatusCode.MOBILE_INVALID);
						} else if (4 == d) {
							serverRsp
									.setStatusCode(StatusCode.MOBILE_HAS_BINDED);
						} else if (5 == d) {
							serverRsp
									.setStatusCode(StatusCode.OTHER_BIND_THIS_MOBILE);
						} else if (6 == d) {
							serverRsp.setStatusCode(StatusCode.REQ_THAN_LIMIT);
						} else {
							serverRsp.setStatusCode(StatusCode.SERVER_BUSY);
						}
					} catch (JSONException e) {
						if (Utils.debug)
							e.printStackTrace();
					}
					broadcastMsg(Constants.INTENT_ACTION_BIND_MOBILE_RSP,
							serverRsp);
					break;
				}
				case MsgInfor.SERVER_CHECK_BIND_PHONE: {
					break;
				}
				case MsgInfor.SERVER_UP_SNS: {
					ServerRsp serverRsp = new ServerRsp();
					try {
						d = jo.getInt("d");
						if (d == 0) {
							serverRsp.setStatusCode(StatusCode.RSP_OK);
							// prefsMeme.edit().putBoolean(CommonData.WEIBO_UPLOAD_FIRST,
							// false).commit();
							// prefsMeme.edit().putLong(CommonData.WEIBO_UPLOAD_LAST_TIME,
							// System.currentTimeMillis())
							// .commit();
							mSess.mPrefMeme.weibo_upload_first.setVal(false);
							mSess.mPrefMeme.weibo_upload_last_time.setVal(
									System.currentTimeMillis()).commit();
						} else if (d == 1) {
							serverRsp
									.setStatusCode(StatusCode.UP_THE_NUMBER_THAN_LIMIT);
							// 本次上传数量过多
						} else if (d == 2) {
							serverRsp
									.setStatusCode(StatusCode.THE_TOTAL_NUMBER_THAN_LIMIT);
						} else if (d == 3) {
							serverRsp.setStatusCode(StatusCode.OPT_FAILED);
						}
					} catch (JSONException e5) {
						e5.printStackTrace();
					}
					broadcastMsg(Constants.INTENT_ACTION_UP_SNS_RSP, serverRsp);
					break;
				}
				case MsgInfor.SERVER_GREET: {
					ServerRsp serverRsp = new ServerRsp();
					try {
						d = jo.getInt("d");
						if (d == 0) {
							serverRsp.setStatusCode(StatusCode.RSP_OK);
						} else {
							serverRsp.setStatusCode(StatusCode.OPT_FAILED);
						}
						broadcastMsg(Constants.INTENT_ACTION_GREET_RSP,
								serverRsp);
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
					break;
				}
				case MsgInfor.SERVER_CHECK_ONLINE: {
					break;
				}
				case MsgInfor.SERVER_ADDORDEL_BLACKLIST: {
					ServerRsp serverRsp = new ServerRsp();
					try {
						d = jo.getInt("d");
						if (0 == d) {
							serverRsp.setStatusCode(StatusCode.RSP_OK);
							int tp = jo.getInt("type");
							serverRsp.putInt("type", tp);
						} else if (1 == d) {
							serverRsp.setStatusCode(StatusCode.OPT_FAILED);
						} else if (2 == d) {
							serverRsp
									.setStatusCode(StatusCode.BUDDY_THAN_LIMIT);
						}
					} catch (JSONException e) {
						if (Utils.debug)
							e.printStackTrace();
					}
					broadcastMsg(Constants.INTENT_ACTION_OPT_BLACKLIST_RSP,
							serverRsp);
					break;
				}
				case MsgInfor.SERVER_SET_STAR_FRIEND: {
					ServerRsp serverRsp = new ServerRsp();
					try {
						d = jo.getInt("d");
						if (0 == d) {
							serverRsp.setStatusCode(StatusCode.RSP_OK);
							int attr = jo.getInt("attr");
							serverRsp.putInt("attr", attr & 1);
						} else if (1 == d) {
							serverRsp.setStatusCode(StatusCode.NOT_FRIEND);
						} else if (2 == d) {
							serverRsp.setStatusCode(StatusCode.OPT_FAILED);
						}
					} catch (JSONException e) {
						if (Utils.debug)
							Log.e(TAG, "服务器返回设置星标好友结果异常", e);
					}
					if (Utils.debug)
						Log.i(TAG, "发送广播处理星标好友结果");
					broadcastMsg(
							Constants.INTENT_ACTION_OPT_SET_STAR_FRIEND_RSP,
							serverRsp);
					break;
				}
				case MsgInfor.SERVER_CREATE_QUN: {
					ServerRsp serverRsp = new ServerRsp();
					try {
						d = jo.getInt("d");
						if (d == 1) {
							serverRsp
									.setStatusCode(StatusCode.GROUP_MEMBER_SIZE_INVALID);
						} else if (d == 2) {
							serverRsp
									.setStatusCode(StatusCode.GROUP_MEMBER_THAN_LIMIT);
						} else if (d == 3) {
							String fbret = jo.getString("fbret");
							if (fbret != null) {
								serverRsp.getBundle().putString("fbret", fbret);
								serverRsp.setStatusCode(StatusCode.OPT_FAILED);
							} else {
								serverRsp.setStatusCode(StatusCode.OPT_FAILED);
							}

						} else if (d == 4) {
							serverRsp
									.setStatusCode(StatusCode.GROUP_NAME_INTRO_SPECIAL_CHAR);
						} else if (d == 0) {
							serverRsp.setStatusCode(StatusCode.RSP_OK);
							long gid = jo.getLong("gid");
							int gvr = jo.getInt("gver");
							int sn = jo.getInt("sn");
							if (sn == TxDB.GROUP_TYPE_PUBLIC) {

								if (Utils.debug)
									Log.i(TAG, "创建聊天室的sn:" + sn);
							} else if (sn == TxDB.GROUP_TYPE_REQUEST) {

								if (Utils.debug)
									Log.i(TAG, "创建半公开群的sn:" + sn);
							} else if (sn == TxDB.GROUP_TYPE_SECRET) {

								if (Utils.debug)
									Log.i(TAG, "创建私密群的sn:" + sn);
							} else {
								if (Utils.debug)
									Log.i(TAG, "不可识别的sn类型:" + sn);
							}

							TxGroup txgroup = Utils.waitSaveTxGroup;
							txgroup.group_id = gid;
							txgroup.group_ver = gvr;
							txgroup.group_own_id = mSess.getUserid();
							txgroup.group_own_name = mSess.getTxMgr()
									.getTx(mSess.getUserid()).getNick_name();
							// txgroup.group_tx_state = TxDB.QU_TX_STATE_CM;
							txgroup.group_tx_state = TxDB.QU_TX_STATE_OWN;// 我创建的群，我是群主

							// TxGroup.saveTxGroupToDB(txgroup, txdata);//用下面的方法
							// 2014.01.23 shc
							TxGroup group = TxGroup.getTxGroup(
									mSess.getContentResolver(), gid);
							if (group == null) {
								// 缓存合数据库都没有这个群对象，添加
								TxGroup.addTxGroup(mSess.getContentResolver(),
										txgroup);

							} else {
								// 缓存或本地找到这群，更新
								ContentValues values = new ContentValues();
								values.put(TxDB.Qun.QU_VER, txgroup.group_ver);
								values.put(TxDB.Qun.QU_OWN_ID,
										txgroup.group_own_id);
								values.put(TxDB.Qun.QU_OWN_NAME,
										txgroup.group_own_name);
								values.put(TxDB.Qun.QU_TX_STATE,
										txgroup.group_tx_state);
								TxGroup.updateTxGroup(
										mSess.getContentResolver(),
										txgroup.group_id, values);
							}

							serverRsp.getBundle().putLong(
									Utils.MSGROOM_GROUP_ID, gid);

							Utils.txList = null;
							Utils.waitSaveTxGroup = null;
						}
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
					broadcastMsg(Constants.INTENT_ACTION_CREATE_GROUP,
							serverRsp);
					break;
				}
				case MsgInfor.SERVER_TITLE_QUN: {
					try {
						ServerRsp serverRsp = new ServerRsp();
						d = jo.getInt("d");
						if (d == 0) {
							long gid = jo.getLong("gid");
							int gver = jo.getInt("gver");
							// TxGroup tg = txdata.getTxGroupByGroupId4DB(gid);
							TxGroup tg = TxGroup.getTxGroup(
									mSess.getContentResolver(), gid);
							if (tg == null || tg.group_ver != gver) {
								mSess.getSocketHelper().sendGetGroup(gid);
							}
							serverRsp.setStatusCode(StatusCode.RSP_OK);
						} else if (d == 3) {
							serverRsp
									.setStatusCode(StatusCode.GROUP_MODIFY_NAME_FAILED);
						} else if (d == 4) {
							serverRsp
									.setStatusCode(StatusCode.GROUP_MODIFY_GROUP_NOT_EXIST);
						} else if (d == 5) {
							serverRsp
									.setStatusCode(StatusCode.GROUP_MODIFY_INTRO_FAILED);
						} else if (d == 6) {
							serverRsp
									.setStatusCode(StatusCode.GROUP_MODIFY_AVATAR_FAILED);
						} else if (d == 7) {
							serverRsp
									.setStatusCode(StatusCode.GROUP_MODIFY_TYPE_FAILED);
						} else if (d == 8) {
							serverRsp
									.setStatusCode(StatusCode.GROUP_MODIFY_BULLENTIN_FAILED);
						} else if (d == 2) {
							serverRsp.setStatusCode(StatusCode.OPT_FAILED);
							String fbret = jo.getString("fbret");
							if (fbret != null) {
								serverRsp.getBundle().putString("fbret", fbret);
							}

						}
						broadcastMsg(Constants.INTENT_ACTION_MODIFY_GROUP,
								serverRsp);
					} catch (JSONException e) {
						if (Utils.debug)
							e.printStackTrace();
					}
					break;
				}
				case MsgInfor.SERVER_MSG_UP_QUN: {
					mSess.getSocketHelper().dealGroupMessageResponse(jo);
					break;
				}
				case MsgInfor.SERVER_UNBIND_PHONE: {
					ServerRsp serverRsp = new ServerRsp();
					try {
						d = jo.getInt("d");
						if (d == 0) {
							serverRsp.setStatusCode(StatusCode.RSP_OK);
						} else {
							serverRsp.setStatusCode(StatusCode.OPT_FAILED);
						}
						broadcastMsg(Constants.INTENT_ACTION_UNBIND_MOBILE_RSP,
								serverRsp);
					} catch (JSONException e) {
						if (Utils.debug)
							e.printStackTrace();
					}
					break;
				}
				case MsgInfor.SERVER_UNBIND_EMAIL: {
					ServerRsp serverRsp = new ServerRsp();
					try {
						d = jo.getInt("d");
						if (d == 0) {
							serverRsp.setStatusCode(StatusCode.RSP_OK);
						} else {
							serverRsp.setStatusCode(StatusCode.OPT_FAILED);
						}
						broadcastMsg(Constants.INTENT_ACTION_UNBIND_EMAIL_RSP,
								serverRsp);
					} catch (JSONException e) {
						if (Utils.debug)
							e.printStackTrace();
					}
					break;
				}
				case MsgInfor.SERVER_WAP_SHARE: {
					ServerRsp serverRsp = new ServerRsp();
					try {
						d = jo.getInt("d");
						if (d == 0) {
							serverRsp.setStatusCode(StatusCode.RSP_OK);
							String url = jo.getString("url");
							serverRsp.putString("url", url);
						} else {
							serverRsp.setStatusCode(StatusCode.OPT_FAILED);
						}
						String sn = jo.getString("sn");
						serverRsp.putString("sn", sn);
						broadcastMsg(Constants.INTENT_ACTION_WAP_SHARE_RSP,
								serverRsp);
					} catch (JSONException e) {
						if (Utils.debug)
							e.printStackTrace();
					}
					break;
				}
				case MsgInfor.SERVER_WEIBO_FRIEND:
					// 无论是否失败, 微博好友比对只进行一次
					weiboFriendCompared.put(mSess.getWeiboUserUD(), true);
					break;
				case MsgInfor.SERVER_SET_ALBUM:
					dealSetAlbum(jo);
					break;
				case MsgInfor.SERVER_GET_ALBUM:
					dealGetAlbum(jo);
					break;
				case MsgInfor.SERVER_REFUSE_REQ:
					dealRefuseReq(jo);
					break;
				case MsgInfor.SERVER_SEARCH_USER:
					dealSearchUser(jo);
					break;
				case MsgInfor.SERVER_GET_BLACKLIST:
					dealGetBlackList(jo);
					break;
				case MsgInfor.SERVER_SET_REMARK_NAME:
					dealSetRemarkName(jo);
					break;
				case MsgInfor.SERVER_GET_REMARK_NAMES:
					dealGetRemarkNames(jo);
					break;
				case MsgInfor.SERVER_PRAISE_MESSAGE:
					dealPraiseMsgResult(jo);
					break;
				case MsgInfor.SERVER_PRAISED_NOTICE:
					dealReceivePraiseNotice(jo);
					break;
				case MsgInfor.SERVER_GET_PRAISED_MESSAGES:
					dealgetPraisedMsgs(jo);
					break;
				case MsgInfor.SERVER_GET_BLOG_MESSAGE:
					dealgetBlogMsg(jo);
					break;
				case MsgInfor.SERVER_GET_BLOG_MESLIST:
					dealgetBlogMsgs(jo);
					break;
				case MsgInfor.SERVER_GET_BLOG_INFO:
					dealgetBlogInfo(jo);
					break;
				case MsgInfor.SERVER_RELEASE_BLOG:
					dealreleaseBlog(jo);
					break;
				case MsgInfor.SERVER_DEL_BLOG:
					dealDelBlog(jo);
					break;
				case MsgInfor.SERVER_LIKE_BLOG:
					deallikeBlog(jo);
					break;
				case MsgInfor.SERVER_GET_USERINFOLIST:
					dealgetUserlist(jo);
					break;
				case MsgInfor.SERVER_ACTION_OPREATE:
					dealServerActionConfirm(jo);
					break;
				}

			}
		}
	}

	/**
	 * 处理Server返回获取备注名
	 * 
	 * @param jo
	 */
	private void dealGetRemarkNames(JSONObject jo) {
		try {
			JSONArray ja = jo.getJSONArray("nlist");

			ArrayList<TxInfor> remarkNameList = new ArrayList<TxInfor>();
			TxInfor infor = null;
			for (int i = 0; i < ja.length(); i++) {
				JSONObject jsonObj = ja.getJSONObject(i);
				Long partner_id = jsonObj.getLong("i");
				String remarkName = jsonObj.getString("n");

				infor = new TxInfor(partner_id, TxInfor.TX_TYPE_TB);
				infor.setRemarkName(remarkName);
				remarkNameList.add(infor);
				// TX.tm.addTBTXRemarkName(partner_id, remarkName);
			}

			mSess.mMsgHandler.onServerGetRemarkNames_82(remarkNameList);

			if (jo.getBoolean("eof") == false) {// eof等于0代表Server还有数据，继续获取
				mSess.getSocketHelper().sendGetRemarkNames();
			} else {
				// 备注名获取完毕，取好友资料
				dealGetTBinfor();
			}
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 处理赞消息的结果 */
	private void dealPraiseMsgResult(JSONObject jo) {
		if (Utils.debug) {
			Log.i(TAG, "处理赞消息结果：" + jo.toString());
		}

		try {
			int result = jo.getInt("d");
			int gid = jo.getInt("gid");// 聊天室id
			int uid = jo.getInt("uid");// 被点赞uid
			String gmid = jo.getString("gmid");// 消息id
			int flag = jo.getInt("flag");// 点赞标记,0为点赞，1为取消点赞
			switch (result) {
			case 0:
				// 成功
				mSess.getPraiseNoticeDao().onReceivePraiseResult(result, gid,
						gmid, uid, flag);

				break;
			case 1:
				// 无此消息
			case 2:
				// 用户被封禁
			case 3:
				// 你没有点赞过不能取消
			case 4:
				// 操作失败
				mSess.getPraiseNoticeDao().onReceivePraiseResult(result, gid,
						gmid, uid, flag);
				break;
			}

		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}

	}

	/** 处理收到消息被赞通知 */
	private void dealReceivePraiseNotice(JSONObject jo) {
		if (Utils.debug) {
			Log.i(TAG, "收到消息被赞通知:" + jo.toString());
		}

		try {
			int operate = jo.getInt("flag");
			if (operate == 0) {
				// 被点赞
				long uid = jo.getInt("uid");
				long groupId = jo.getInt("gid");
				long gmid = jo.getInt("gmid");
				long time = jo.getInt("time");
				PraiseNotice pn = new PraiseNotice(groupId, gmid, uid, operate,
						time);

				mSess.mMsgHandler.onReceivePraiseNotice_128(pn);
			} else if (operate == 1) {
				// 被取消点赞，不做操作
			}
		} catch (JSONException e) {
			if (Utils.debug) {
				e.printStackTrace();
			}
		}

	}

	// 获取到服务器返回的被赞消息列表
	private void dealgetPraisedMsgs(JSONObject jo) {
		if (Utils.debug) {
			Log.i(TAG, "收到所有被赞消息列表:" + jo.toString());
		}

		try {
			String requestGmid = jo.getString("gmid");// 请求消息时发送的gmid
			int eof = jo.getInt("eof");// 0：未取完，1：已取完
			JSONArray noticeArray = jo.getJSONArray("result");
			ArrayList<PraiseNotice> pnList = new ArrayList<PraiseNotice>();
			if (noticeArray != null && noticeArray.length() > 0) {
				// 有数据
				long groupid;
				long time;
				long gmid;
				long praiseTime;
				JSONArray ja;
				PraiseNotice pn;
				for (int i = 0; i < noticeArray.length(); i++) {
					// 倒叙添加，按gmid从大到小顺序
					JSONObject noticeObj = noticeArray.getJSONObject(i);
					// noticeObj.getInt("praisenum");//这个字段应该不用吧
					groupid = noticeObj.getInt("gid");
					time = noticeObj.getInt("time");
					gmid = Long.parseLong(noticeObj.getString("gmid"));
					praiseTime = noticeObj.getInt("praisetime");// 文档上写这个是消息发布时间？？
					ja = noticeObj.getJSONArray("idlist");
					// if(ja.length()==0){
					// //id集合为空
					// continue;
					// }
					// 逆序点赞id
					JSONArray sortJa = new JSONArray();
					for (int j = ja.length(); j > 0; j--) {
						sortJa.put(ja.get(j - 1));
					}
					pn = new PraiseNotice(groupid, gmid, sortJa,
							PraiseNotice.ACTION_PRAISE, praiseTime);

					pnList.add(pn);
				}
			}
			// 把该方法移到外面，避免出现result集合为空的情况
			mSess.getPraiseNoticeDao().addPraiseNotices(pnList, eof == 1);// 添加到本地数据库中

		} catch (Exception e) {
			if (Utils.debug) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 服务器返回某人瞬间
	 * 
	 * @param jo
	 */
	private void dealgetBlogMsg(JSONObject jo) {
		if (Utils.debug) {
			Log.i(TAG, "收到瞬间:" + jo.toString());
		}

		try {

			int d = jo.getInt("d");
			BlogMsg msg = new BlogMsg();
			msg.setUid(jo.getLong("uid"));
			msg.setBlogNums(jo.getInt("momnum"));
			msg.setLikedNums(jo.getInt("likenum"));
			if (d == 0) {
				msg.setAccessNums(jo.getInt("accessnum"));
			} else {
				mSess.getBlogOpea().getcallback().receiveError("没有瞬间");
				return;
			}

			if (mSess.getBlogOpea().getcallback() != null) {
				mSess.getBlogOpea().getcallback().receiveHead(msg);
			}
		} catch (Exception e) {
			if (Utils.debug) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 服务器批量返回某人瞬间
	 * 
	 * @param jo
	 */
	private void dealgetBlogMsgs(JSONObject jo) {
		if (Utils.debug) {
			Log.i(TAG, "批量收到瞬间:" + jo.toString());
		}
		try {
			long uid = jo.getLong("uid");
			JSONArray resultsArray = jo.getJSONArray("result");
			List<BlogMsg> blogs = new ArrayList<BlogMsg>();
			if (resultsArray != null && resultsArray.length() > 0) {
				for (int i = 0; i < resultsArray.length(); i++) {
					JSONObject obj = (JSONObject) resultsArray.get(i);
					BlogMsg result_info = new BlogMsg();
					result_info.setMid(Long.parseLong(obj.getString("mid")));
					result_info.setTime(obj.getLong("time"));
					result_info.setLikednum(obj.getInt("likenum"));
					result_info.setIsdel(obj.getBoolean("isdel"));
					result_info.setUid(uid);
					JSONArray jsonArray = obj.getJSONArray("idlist");
					List<Long> idlist = new ArrayList<Long>();
					for (int j = 0; j < jsonArray.length(); j++) {
						int id = (Integer) jsonArray.get(j);
						long priseId = id;
						idlist.add(priseId);
					}
					result_info.setIdlist(idlist);
					blogs.add(result_info);
				}
			}

			BlogMsg info = new BlogMsg();
			info.setUid(uid);
			info.setBlogMsgs(blogs);
			if (mSess.getBlogOpea().getcallback() != null) {
				mSess.getBlogOpea().getcallback().receiveList(info);
			}
		} catch (Exception e) {
			if (Utils.debug) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 136服务器返回瞬间
	 * 
	 * @param jo
	 */
	private void dealgetBlogInfo(JSONObject jo) {
		if (Utils.debug) {
			Log.i(TAG, "瞬间详情:" + jo.toString());
		}
		try {
			int block_d = jo.getInt("d");
			if (block_d == 0) {
				BlogMsg info = new BlogMsg();
				info.setMmsg(jo.getString("mmsg"));
				info.setMid(Long.parseLong(jo.getString("mid")));
				info.setTime(jo.getLong("time"));
				info.setIsdel(jo.getBoolean("isdel"));
				info.setLikednum(jo.getInt("likenum"));

				JSONObject objJson = jo.optJSONObject("mobj");
				if (objJson != null) {
					info.setMdiaInfo(objJson.toString());
				}
				if (objJson != null) {
					String imgUrl = objJson.getString("img");
					info.setImgUrl(imgUrl);
					if (!Utils.isNull(imgUrl) && !(imgUrl == JSONObject.NULL)) {
						info.setType(BlogMsg.IMG);
						// info.setImgPath(receiveSameImg(imgUrl));
					}
					// 2. adu:obj,音频
					// a) end:string
					// b) url:str,音频文件地址
					// c) l:uint32,音频文件长度,单位:字节
					// d) t:uint16,音频长度,单位:秒
					JSONObject aduJson = objJson.getJSONObject("adu");
					if (aduJson != null && aduJson.length() > 0) {
						String adu_url = aduJson.getString("url");
						info.setAduUrl(adu_url);
						if (!Utils.isNull(adu_url)
								&& !(adu_url == JSONObject.NULL)) {
							info.setType(BlogMsg.AUDIO);
							// info.setAduPath(receiveSameAudio(adu_url));
						}
						long time = aduJson.getInt("t");
						info.setAduTime(time);

						if (!Utils.isNull(adu_url)
								&& !(adu_url == JSONObject.NULL)
								&& !Utils.isNull(imgUrl)
								&& !(imgUrl == JSONObject.NULL)) {
							info.setType(BlogMsg.IMG_AUDIO);
						}

						if ((adu_url == JSONObject.NULL)
								&& (imgUrl == JSONObject.NULL)) {
							info.setType(BlogMsg.MSG);
						}
					}

					// 3. geo:obj,地理位置
					// a) lo:double,经度
					// b) la:double,纬度
					if (objJson.toString().contains("geo")) {
						JSONObject geoJson = objJson.getJSONObject("geo");

						if (geoJson != null && geoJson.length() > 0) {
							String geo = geoJson.getDouble("la") + ","
									+ geoJson.getDouble("lo");
							String city = geoJson.getString("ct");
							info.setCity(city);
							info.setGeo(geo);
						}
					}
				}

				JSONArray jsonArray = jo.getJSONArray("idlist");
				List<Long> idlist = new ArrayList<Long>();
				for (int j = 0; j < jsonArray.length(); j++) {
					int id = (Integer) jsonArray.get(j);
					long lid = id;
					idlist.add(lid);
				}
				info.setIdlist(idlist);
				if (mSess.getBlogOpea().getcallback() != null) {
					mSess.getBlogOpea().getcallback().receiveListInfor(info);
				}
			}

		} catch (Exception e) {
			if (Utils.debug) {
				e.printStackTrace();
			}
			if (mSess.getBlogOpea().getcallback() != null) {
				mSess.getBlogOpea().getcallback().receiveError("解析错误");
			}
		}
	}

	private void dealreleaseBlog(JSONObject jo) {
		if (Utils.debug) {
			Log.i(TAG, "瞬间详情:" + jo.toString());
		}
		Object[] obj = null;
		try {
			int block_d = jo.getInt("d");
			if (block_d == 0) {
				// 发布成功
				BlogMsg info = new BlogMsg();
				info.setMid(Long.parseLong(jo.getString("mid")));
				info.setTime(jo.getLong("mtime"));
				obj = new Object[] { true, info };
			} else {
				// 发布失败
				obj = new Object[] { false, null };
			}
		} catch (Exception e) {
			obj = new Object[] { false, null };
			e.printStackTrace();
		}
		if (mSess.getReleaseBlogOpea().getcallback() != null)
			mSess.getReleaseBlogOpea().getcallback().receive(obj);
	}

	private void dealDelBlog(JSONObject jo) {
		if (Utils.debug) {
			Log.i(TAG, "删除瞬间详情:" + jo.toString());
		}
		Object[] obj = null;
		try {
			int block_d = jo.getInt("d");
			// 发布成功
			BlogMsg info = new BlogMsg();
			info.setMid(Long.parseLong(jo.getString("mid")));
			info.setUid(jo.getLong("uid"));
			obj = new Object[] { block_d, info };
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mSess.getBlogOpea().getcallback() != null)
			mSess.getBlogOpea().getcallback().receiveDel(obj);
	}

	// 喜欢瞬间返回
	private void deallikeBlog(JSONObject jo) {
		if (Utils.debug) {
			Log.i(TAG, "喜欢瞬间详情:" + jo.toString());
		}
		Object[] obj = null;
		try {
			int block_d = jo.getInt("d");
			// 发布成功
			BlogMsg info = new BlogMsg();
			info.setMid(Long.parseLong(jo.getString("mid")));
			info.setUid(jo.getLong("uid"));

			if (jo.getInt("flag") == 0) {
				info.setLikedType(BlogMsg.LIKED);
			} else if (jo.getInt("flag") == 1) {
				info.setLikedType(BlogMsg.UNLIKED);
			}
			obj = new Object[] { block_d, info };
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mSess.getBlogOpea().getcallback() != null)
			mSess.getBlogOpea().getcallback().receiveLike(obj);
	}

	// 瞬间批量获取用户信息返回
	private void dealgetUserlist(JSONObject jo) {
		if (Utils.debug) {
			Log.i(TAG, "批量获取用户基本信息详情:" + jo.toString());
		}
		List<TX> usersList = new ArrayList<TX>();
		try {
			int block_d = jo.getInt("d");

			if (block_d == 0) {
				// 成功
				/**
				 * users:array[obj,obj,...]
				 * obj:{"i":用户id,"nn":"用户昵称","sex":性别,"avatarurl"
				 * :"头像url","avatarver":头像版本,"narea":[所在地区], "sign":"用户签名"}
				 * fields:array,要获取的字段名称列表
				 */
				JSONArray jsonArray = jo.getJSONArray("users");
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject userJson = (JSONObject) jsonArray.get(i);

					TX txInfor = new TX();
					long id = userJson.getLong("i");
					txInfor.setPartnerId(id);
					String nickname = userJson.getString("nn");
					int sex = userJson.getInt("sex");
					int avatarmd5 = userJson.optInt("avatarver", 0);
					String avatar_url = userJson.getString("avatarurl");
					int level = 1;
					if (Utils.lev)
						level = userJson.optInt("level", 0);

					String narea = StringUtils.jsonArray2Str(userJson
							.getJSONArray("narea"));
					String sign = userJson.optString("sign", "");
					txInfor.setNick_name(nickname);
					txInfor.setAvatar_ver(avatarmd5);
					txInfor.setAvatar_url(avatar_url);
					txInfor.setSex(sex);
					txInfor.setLevel(level);
					txInfor.setArea(narea);
					txInfor.setSign(sign);

					mSess.mMsgHandler.onServerGetUserList_146(txInfor);
					usersList.add(txInfor);
				}
			}
		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
		}
		if (mSess.getBlogOpea().getcallback() != null)
			mSess.getBlogOpea().getcallback().receiveUserList(usersList);
	}

	/** 处理服务器对客户端进行一些操作的通知的确认，例如查看了附近的人、微博转发 */
	private void dealServerActionConfirm(JSONObject jo) {
		if (Utils.debug) {
			Log.i(TAG, "对客户端操作的确认:" + jo.toString());
		}
		try {
			int result = jo.getInt("d");
			if (result == 0) {
				// 收到成功的应答后什么都不用做
				int sn = jo.getInt("sn");
				if (sn == 1) {
					// 访问附近的人
					if (Utils.debug) {
						Log.i(TAG, "访问附近的人");
					}
				} else if (sn == 2) {
					// 访问的微博分享名片
					if (Utils.debug) {
						Log.i(TAG, "访问的微博分享名片");
					}
				}

			} else if (result == 1) {

			}
		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
		}

	}

	/**
	 * 处理Server返回设置备注名
	 * 
	 * @param jo
	 */
	private void dealSetRemarkName(JSONObject jo) {
		try {
			ServerRsp sp = new ServerRsp();
			int d = jo.getInt("d");
			if (d == 0) {
				sp.setStatusCode(StatusCode.RSP_OK);
			} else if (d == 1) {
				sp.setStatusCode(StatusCode.NOT_FRIEND);
			} else if (d == 2) {
				sp.setStatusCode(StatusCode.CHANGE_REMARK_NAME_NOTRULE);
			} else if (d == 3) {
				sp.setStatusCode(StatusCode.OPT_FAILED);
			}
			broadcastMsg(Constants.INTENT_ACTION_SET_REMARK_NAME_RSP, sp);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 处理Server返回的黑名单列表(79)
	 * 
	 * @param jo
	 */
	private void dealGetBlackList(JSONObject jo) {
		try {
			if (jo.getInt("eof") == 0) {
				isEndOfBlackList = false;
				// TODO 黑名单数据未取完，应该再去获取吧？ 2014.01.21 shc
				mSess.getSocketHelper().sendGetBlackList(
						blackListPageNum.getAndIncrement());
			} else {
				// 黑名单list已经取完
				// DataContainer.resetBlackListPageNum();//用TX管理黑名单吧，2014.01.21
				// shc
				// TX.tm.resetBlackListPageNum();
				blackListPageNum.set(0);// 重置黑名单页数
				isEndOfBlackList = true;
			}
			JSONArray ja = jo.getJSONArray("blk");
			if (ja.length() == 0 && isEndOfBlackList) {
				ServerRsp serverRsp = new ServerRsp();
				serverRsp.setStatusCode(StatusCode.RSP_OK);
				serverRsp.putBoolean("isEnd", true);
				broadcastMsg(Constants.INTENT_ACTION_GET_BLACKlIST_RSP,
						serverRsp);
			} else {
				List<String> ids = new ArrayList<String>();// 服务器返回的一页黑名单list,不一定是全部

				ArrayList<TxInfor> blackTXList = new ArrayList<TxInfor>();// 存放黑名单tx供传递使用
				TxInfor tinfor = null;
				for (int i = 0; i < ja.length(); i++) {
					long id = ja.getJSONObject(i).getLong("i");
					long inBlackTime = ja.getJSONObject(i).getLong("t") * 1000;
					ids.add(String.valueOf(id));
					tinfor = new TxInfor(id, TxInfor.TX_TYPE_BLACK);
					tinfor.setInBlackTime(inBlackTime);
					blackTXList.add(tinfor);
					// TX.tm.addBlackTX(id,inBlackTime);
				}
				mSess.mMsgHandler.onServerGetBlacklist_79(blackTXList);

				mSess.getSocketHelper().sendGetMoreUsers(ids,
						MsgInfor.SERVER_GET_BLACKLIST);
			}
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 处理Server返回的搜索用户(117)
	 * 
	 * @param jo
	 *            JSON对象
	 */
	private void dealSearchUser(JSONObject jo) {
		boolean isEnd = false;
		try {
			if (jo.getInt("cp") < jo.getInt("ep")) {
				mSess.getSocketHelper().sendSearchUser(
						DataContainer.searchCondition,
						SearchConditionResultActivity.oldeadline);
			} else {
				DataContainer.resetSearchUserPageNum();
				isEnd = true;
			}
			SearchConditionResultActivity.oldeadline = jo.getInt("oldeadline");
			JSONArray ja = jo.getJSONArray("results");
			ArrayList<TX> txList = new ArrayList<TX>();
			for (int i = 0; i < ja.length(); i++) {
				TX tx = new TX();
				if ((ja.getJSONObject(i).getLong("i")) != TX.TUIXIN_MAN) {
					tx.partner_id = ja.getJSONObject(i).getLong("i");
					tx.setNick_name(ja.getJSONObject(i).getString("n"));
					tx.setSex(ja.getJSONObject(i).getInt("sex"));
					tx.age = ja.getJSONObject(i).getInt("age");
					tx.setSign(ja.getJSONObject(i).getString("sign"));
					// tx.setArea(ja.getJSONObject(i).getJSONArray("narea").toString());//对于narea统一用下面的方法处理
					// 2014.01.22 shc
					tx.setArea(StringUtils.jsonArray2Str(ja.getJSONObject(i)
							.getJSONArray("narea")));

					tx.haveAlbum = ja.getJSONObject(i).getBoolean("havealbum");
					tx.setAvatar_url(ja.getJSONObject(i).getString("avatarurl"));
					tx.setLevel(1);
					if (Utils.lev)
						tx.setLevel(ja.getJSONObject(i).getInt("level"));
					txList.add(tx);
				}

			}

			mSess.mMsgHandler.onServerSearchUser_117(txList, isEnd);

		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}

	}

	/**
	 * 处理Server返回拒绝接收好友(115)
	 * 
	 * @param jo
	 *            Server返回JSON对象
	 */
	private void dealRefuseReq(JSONObject jo) {
		try {
			int d = jo.getInt("d");
			int op = jo.getInt("op");
			ServerRsp serverRsp = new ServerRsp();
			Bundle bundle = new Bundle();
			bundle.putInt("op", op);
			if (d == 0) {
				serverRsp.setBundle(bundle);
				serverRsp.setStatusCode(StatusCode.RSP_OK);
				// editorMeme.putBoolean(CommonData.IS_RECEIVE_REQ, op == 0 ?
				// true : false).commit();
				mSess.mPrefMeme.is_receive_req.setVal(op == 0 ? true : false)
						.commit();
				// TX.tm.reloadTXMe();// ////////
				mSess.mMsgHandler.onReloadTXMe();

			} else {
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			}
			broadcastMsg(Constants.INTENT_ACTION_REFUSE_REQ_RSP, serverRsp);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 获取相册结果(111)
	 * 
	 * @param jo
	 *            Server返回JSON对象
	 */
	private void dealGetAlbum(JSONObject jo) {
		try {
			int d = jo.getInt("d");
			long uid = jo.getLong("uid");
			ServerRsp serverRsp = new ServerRsp();
			if (d == 0) {
				serverRsp.setStatusCode(StatusCode.RSP_OK);
				if (Utils.debug) {
					Log.i(TAG, "返回的数据是d=0?：" + d);
				}
				// 自己相册处理
				// if (uid == TX.tm.getTxMe().partner_id) {
				if (uid == mSess.getUserid()) {
					// editorMeme.putString(CommonData.ALBUM,
					// StringUtils.jsonArray2Str(jo.getJSONArray("album")))
					// .commit();
					// editorMeme.putInt(CommonData.ALBUM_VERSION,
					// jo.getInt("aver")).commit();
					mSess.mPrefMeme.album.setVal(StringUtils.jsonArray2Str(jo
							.getJSONArray("album")));
					mSess.mPrefMeme.album_version.setVal(jo.getInt("aver"))
							.commit();
					// TX.tm.reloadTXMe();// ///////

					mSess.mMsgHandler.onReloadTXMe();

				} else {
					// TX tx = TX.tm.getTx(uid);
					TX tx = mSess.getTxMgr().getTx(uid);

					ArrayList<AlbumItem> aiList = new ArrayList<AlbumItem>();
					for (int i = 0; i < jo.getJSONArray("album").length(); i++) {
						AlbumItem ai = new AlbumItem();
						ai.setUrl(jo.getJSONArray("album").getString(i));
						aiList.add(ai);
					}
					if (tx != null) {
						tx.setAlbumVer(jo.getInt("aver"));
						tx.setAlbum(aiList);
						// TX.saveTXtoDB(tx, mSess.getContentResolver());
					}
					serverRsp.getBundle().putSerializable("aiList", aiList);
					serverRsp.getBundle().putLong("uid", uid);
				}
			} else if (d == 1) {
				if (Utils.debug) {
					Log.i(TAG, "返回的数据是d=1?：" + d);
				}
				serverRsp.getBundle().putLong("uid", uid);
				serverRsp.setStatusCode(StatusCode.USERALBUM_NO_EXIST);
			} else if (d == 2) {
				if (Utils.debug) {
					Log.i(TAG, "返回的数据是d=2?：" + d);
				}
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			}
			broadcastMsg(Constants.INTENT_ACTION_GET_ALBUM_RSP, serverRsp);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 设置相册结果(113)
	 * 
	 * @param jo
	 *            Server返回JSON对象
	 */
	private void dealSetAlbum(JSONObject jo) {
		try {
			int d = jo.getInt("d");
			ServerRsp serverRsp = new ServerRsp();
			if (d == 0) {
				serverRsp.setStatusCode(StatusCode.RSP_OK);
				// editorMeme.putInt(CommonData.ALBUM_VERSION,
				// jo.getInt("aver")).commit();
				mSess.mPrefMeme.album_version.setVal(jo.getInt("aver"))
						.commit();
				// TX.tm.reloadTXMe();// //////////
				mSess.mMsgHandler.onReloadTXMe();

			} else {
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			}
			broadcastMsg(Constants.INTENT_ACTION_SET_ALBUM_RSP, serverRsp);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 96号协议，封设备服务器返回 */
	private void dealBlockUser(JSONObject jo) {
		try {
			ServerRsp serverRsp = new ServerRsp();
			int d = jo.getInt("d");
			long uid = jo.getLong("uid");
			int sn = jo.getInt("sn");
			if (d == 0) {
				TXMessage txMsg = null;
				// TX tx = mSess.getTxMgr().getTx(uid);
				//
				// if (tx == null)
				// mSess.getSocketHelper().sendGetUserInfor(uid);

				// 用户被封禁之后就去服务器拉取最新资料信息
				mSess.getSocketHelper().sendGetUserInfor(uid);

				if (sn == 1) {
					txMsg = TXMessage.createSealMoible4Admin(
							mSess.getContext(), uid,
							System.currentTimeMillis() / 1000);
				} else {
					txMsg = TXMessage.createSealId4Admin(mSess.getContext(),
							uid, System.currentTimeMillis() / 1000);
				}
				TXMessage.saveTXMessagetoDB(txMsg, mSess.getContentResolver(),
						true);

				serverRsp.putInt("sn", sn);
				serverRsp.setStatusCode(StatusCode.RSP_OK);

			} else if (d == 1) {
				serverRsp.setStatusCode(StatusCode.NO_PERMISSION);
			} else if (d == 2) {
				serverRsp.setStatusCode(StatusCode.USER_NO_EXIST);
			} else if (d == 3) {
				serverRsp.setStatusCode(StatusCode.DONE);
			} else if (d == 4) {
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			} else if (d == 5) {
				serverRsp.setStatusCode(StatusCode.BLOCK_FAILED);
			}
			broadcastMsg(Constants.INTENT_ACTION_BLOCK_USER, serverRsp);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}

	}

	/**
	 * 返回一批聊天室消息ID (2051号协议)
	 * 
	 * @param jo
	 */
	private void dealGetGroupMessageIds(JSONObject jo) {
		try {
			// ServerRsp serverRsp = new ServerRsp();
			int d = jo.getInt("d");
			int gid = jo.optInt("gid");
			// long uid = jo.optLong("uid",0);
			if (d == 0) {
				JSONArray gmidJsonArray = jo.getJSONArray("gmids");
				ArrayList<Long> gmidList = new ArrayList<Long>();
				for (int i = 0; i < gmidJsonArray.length(); i++) {
					gmidList.add(gmidJsonArray.getLong(i));
				}

				if (gmidJsonArray.length() > 0) {
					ArrayList<TXMessage> msglist = TXMessage
							.filterGroupMessageUnreadList(
									mSess.getContentResolver(), gid, 0);
					// TODO 这里什么时候会执行到？聊天室的消息是不进入消息会话列表的，所以不应该更新吧？ 2014.06.23
					// shc
					if (msglist.size() > 0) {
						TXMessage msg = msglist.get(0);// 本地最新的gmid
						if (Utils.debug)
							Log.i(TAG, gmidList.get(0) > msg.gmid ? "有新ID"
									: "没有新ID");
						if (gmidList.get(0) > msg.gmid) {
							// 服务器的最新gmid大于本地数据库中最新的gmid
							System.out.println("gmid = msg.gmid" + msg.gmid);
							int index = gmidList.indexOf(msg.gmid);
							System.out.println("index==" + index);
							if (index == -1) {
								index = gmidJsonArray.length();
							}
							// 这里的msg应该取最新的一条未读消息
							MsgStat ms = MsgStat.updateMsgStatByTxmsg(msg,
									mSess.getContentResolver(),
									TxDB.MS_TYPE_QU, gmidList.get(0), index,
									false);
							SessionManager.broadcastMsg(TxData.FLUSH_MSGS);// 发送刷新list会话列表广播

						}

					}
				}

			} else if (d == 1) {
				// serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			}
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	private void dealUserWarn(JSONObject jo) {
		try {
			ServerRsp serverRsp = new ServerRsp();
			int d = jo.getInt("d");
			long uid = jo.optLong("uid", 0);
			if (d == 0) {
				// TX tx = TX.tm.getTx(uid);
				TX tx = mSess.getTxMgr().getTx(uid);

				if (tx == null)
					mSess.getSocketHelper().sendGetUserInfor(uid);

				TXMessage txMsg = TXMessage.createWarn4Admin(
						mSess.getContext(), uid,
						System.currentTimeMillis() / 1000);
				TXMessage.saveTXMessagetoDB(txMsg, mSess.getContentResolver(),
						true);

				serverRsp.setStatusCode(StatusCode.RSP_OK);
			} else if (d == 1) {
				serverRsp.setStatusCode(StatusCode.USER_NO_EXIST);
			} else if (d == 2) {
				serverRsp.setStatusCode(StatusCode.NO_PERMISSION);
			} else {
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			}
			broadcastMsg(Constants.INTENT_ACTION_WARN_USER, serverRsp);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 2049删除群消息
	 * 
	 * @param jo
	 */
	private void dealDeleteMsg(JSONObject jo) {
		try {
			ServerRsp serverRsp = new ServerRsp();
			int d = jo.getInt("d");
			long gmid = jo.optLong("gmid", 0);
			if (d == 0) {
				serverRsp.getBundle().putLong("gmid", gmid);

				serverRsp.setStatusCode(StatusCode.RSP_OK);
			} else if (d == 1) {
				serverRsp.setStatusCode(StatusCode.NO_PERMISSION);
			} else if (d == 2) {
				serverRsp.setStatusCode(StatusCode.MSG_NO_EXIST);
			} else {
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			}
			broadcastMsg(Constants.INTENT_ACTION_DELETE_GROUP_MSG, serverRsp);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}

	}

	/**
	 * 2046 举报结果
	 * 
	 * @param jo
	 */
	private void dealReportUser(JSONObject jo) {
		try {
			ServerRsp serverRsp = new ServerRsp();
			int d = jo.getInt("d");
			int ep = jo.getInt("uid");

			if (d == 0) {
				Bundle b = serverRsp.getBundle();
				b.putInt("uid", ep);
				serverRsp.setStatusCode(StatusCode.RSP_OK);
			} else if (d == 1) {
				serverRsp.setStatusCode(StatusCode.USER_NO_EXIST);
			} else {
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			}
			broadcastMsg(Constants.INTENT_ACTION_REPORT_USER, serverRsp);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 2046 举报结果
	 * 
	 * @param jo
	 */
	private void dealReportBlog(JSONObject jo) {
		try {
			ServerRsp serverRsp = new ServerRsp();
			int d = jo.getInt("d");
			int ep = jo.getInt("uid");
			String md = jo.getString("mid");
			long mid = 0;
			if (!Utils.isNull(md)) {
				mid = Long.parseLong(md);
			}

			if (d == 0) {
				Bundle b = serverRsp.getBundle();
				b.putLong("mid", mid);
				serverRsp.setStatusCode(StatusCode.RSP_OK);
			} else if (d == 2) {
				serverRsp.setStatusCode(StatusCode.USER_NO_EXIST);
			} else if (d == 3) {
				serverRsp.setStatusCode(StatusCode.BOLG_NO_EXIT);
			} else {
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			}
			broadcastMsg(Constants.INTENT_ACTION_REPORT_BLOG, serverRsp);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 2043 server返回在线人员 */
	private void dealPubOnlineMember(JSONObject jo) {
		try {
			ServerRsp serverRsp = new ServerRsp();
			int d = jo.getInt("d");
			int cp = jo.getInt("cp");
			int ep = jo.getInt("ep");
			ep += 1;
			if (d == 0) {
				ArrayList<TX> usersList = new ArrayList<TX>();
				JSONArray grps = jo.getJSONArray("us");
				for (int i = 0; i < grps.length(); i++) {
					JSONObject joo = grps.getJSONObject(i);
					long partner_id = joo.getLong("i");

					TX tx = new TX();
					tx.setPartnerId(partner_id);
					tx.setNick_name(joo.getString("n"));
					tx.setAvatar_url(joo.getString("avatar"));
					tx.setSex(joo.getInt("sx"));
					tx.setArea(StringUtils.jsonArray2Str(joo
							.getJSONArray("narea")));
					tx.sign = joo.optString("sign", "");

					tx.setLevel(1);
					if (Utils.lev)
						tx.setLevel(joo.getInt("level"));

					mSess.mMsgHandler.onServerPublicOnlineMember_2043(tx);

					usersList.add(tx);
				}
				Bundle b = serverRsp.getBundle();
				b.putInt("cp", cp);
				b.putInt("ep", ep);
				b.putParcelableArrayList(USER_LIST, usersList);
				serverRsp.setStatusCode(StatusCode.RSP_OK);
			} else {
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			}
			broadcastMsg(Constants.INTENT_ACTION_GET_PUBLIC_ONLINE_MEMBER,
					serverRsp);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}

	}

	/**
	 * 2040
	 * 
	 * @param jo
	 */
	private void dealSettingGroup(JSONObject jo) {
		try {
			ServerRsp serverRsp = new ServerRsp();
			int d = jo.getInt("d");
			long gid = jo.getLong("gid");
			int sn = jo.getInt("sn");

			if (d == 0) {
				TxGroup txGroup = TxGroup.getTxGroup(
						mSess.getContentResolver(), (int) gid);
				if (txGroup != null) {
					txGroup = setGroupNotice(sn, txGroup);
					// TxGroup.saveTxGroupToDB(txGroup, txdata);//用下面的方法
					// 2014.01.23 shc
					ContentValues values = new ContentValues();
					values.put(TxDB.Qun.QU_RCV_MSG, txGroup.rcv_msg);
					values.put(TxDB.Qun.QU_RCV_PUSH, txGroup.rcv_push);
					TxGroup.updateTxGroup(mSess.getContentResolver(),
							txGroup.group_id, values);
				}
				serverRsp.setStatusCode(StatusCode.RSP_OK);
			} else {
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			}
			String act = Constants.INTENT_ACTION_SETTING_GROUP_RESULT;
			broadcastMsg(act, serverRsp);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 批量获得用户信息 94
	 * 
	 * @param jo
	 *            区别1群成员2黑名单
	 */
	private void dealMoreUser(JSONObject jo) {
		try {
			int d = jo.getInt("d");
			int sn = jo.getInt("sn");
			ArrayList<TX> usersList = new ArrayList<TX>();
			if (d == 0) {
				JSONArray grps = jo.getJSONArray("us");
				for (int i = 0; i < grps.length(); i++) {
					JSONObject joo = grps.getJSONObject(i);
					TX tx = new TX();
					tx.partner_id = joo.getLong("i");
					tx.setNick_name(joo.getString("n"));
					tx.avatar_url = joo.getString("avatar");
					tx.setSex(joo.getInt("sx"));
					tx.sign = joo.optString("sign", "");
					tx.area = StringUtils.jsonArray2Str(joo
							.getJSONArray("narea"));
					tx.setLevel(Utils.level);
					if (Utils.lev)
						tx.setLevel(joo.getInt("level"));
					usersList.add(tx);
				}
			}

			mSess.mMsgHandler.onServerGetUsersinfo_94(usersList,
					isEndOfBlackList, sn, d == 0 ? SessionManager.OPT_OK
							: SessionManager.OPT_FAILED);

		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 批量获得群结果列表 2038
	 * 
	 * @param jo
	 *            区别 101公共/102搜索 /103我的群
	 */
	private void dealMoreGroup(JSONObject jo) {
		try {
			ServerRsp serverRsp = new ServerRsp();
			int d = jo.getInt("d");
			int sn = jo.getInt("sn");
			String snStr = String.valueOf(sn);
			String act = null;
			int groupType = -1;// 区别 101公共/102搜索 /103我的群
			if (snStr.startsWith("101")) {
				groupType = TxDB.QU_GET_TYPE_PUBLIC;
			} else if (snStr.startsWith("102")) {
				groupType = TxDB.QU_GET_TYPE_SEARCH;
			} else if (snStr.startsWith("103")) {
				groupType = TxDB.QU_GET_TYPE_OWN;
			}
			if (d == 0) {
				ArrayList<TxGroup> groupList = new ArrayList<TxGroup>();
				JSONArray grps = jo.getJSONArray("grps");
				for (int i = 0; i < grps.length(); i++) {
					JSONObject group = grps.getJSONObject(i);
					int gid = group.getInt("i");
					TxGroup txGroup = TxGroup.getTxGroup(
							mSess.getContentResolver(), gid);
					if (txGroup == null) {
						txGroup = new TxGroup();
					}
					txGroup.group_id = gid;
					txGroup.group_sign = group.getString("intro");
					txGroup.group_title = group.getString("name");
					txGroup.group_all_num = group.getInt("n");
					txGroup.group_ol_num = group.getInt("ol");
					txGroup.group_avatar = group.getString("avatar");
					// String avatar = group.getString("avatar");
					// if (!txGroup.group_avatar.equals(avatar)) {
					// Utils.delAvatar(mSess.getContext(), gid,
					// txGroup.group_avatar);
					// txGroup.group_avatar = avatar;
					// } else {
					// txGroup.group_avatar = txGroup.group_avatar;
					// }

					txGroup.group_own_id = group.getLong("owi");
					txGroup.group_own_name = group.getString("own");
					txGroup.group_type_channel = group.getInt("t");
					txGroup.group_time = group.getLong("ct");
					if (groupType == TxDB.QU_GET_TYPE_OWN) {
						txGroup.group_tx_state = TxDB.QU_TX_STATE_CM;
					}
					JSONArray admins = group.getJSONArray("adm");
					StringBuffer group_adm_ids = new StringBuffer();
					StringBuffer group_adm_names = new StringBuffer();
					long currentId = mSess.getUserid();

					for (int j = 0; j < admins.length(); j++) {
						JSONObject admin = admins.getJSONObject(j);
						group_adm_names.append(admin.optString("n", ""))
								.append("�");
						long id = admin.optLong("i", 0);
						group_adm_ids.append(id).append("�");
						if (currentId == id) {
							txGroup.group_tx_state = TxDB.QU_TX_STATE_GM;
						}
					}

					if (txGroup.group_own_id == currentId) {
						txGroup.group_tx_state = TxDB.QU_TX_STATE_OWN;
					}

					txGroup.group_tx_admin_ids = group_adm_ids.toString();
					txGroup.group_tx_admin_names = group_adm_names.toString();

					groupList.add(txGroup);
					// TxGroup.saveTxGroupToDB(txGroup, txdata);//用下面的方法
					// 2014.01.23 shc
					TxGroup tempGroup = TxGroup.getTxGroup(
							mSess.getContentResolver(), txGroup.group_id);
					if (tempGroup == null) {
						// 缓存合数据库中都没有此群
						TxGroup.addTxGroup(mSess.getContentResolver(), txGroup);
					} else {
						// 缓存或数据库中有此群，更新此群
						ContentValues values = new ContentValues();
						values.put(TxDB.Qun.QU_SIGN, txGroup.group_sign);
						values.put(TxDB.Qun.QU_DISPLAY_NAME,
								txGroup.group_title);
						values.put(TxDB.Qun.ALL_NUM, txGroup.group_all_num);
						values.put(TxDB.Qun.OL_NUM, txGroup.group_ol_num);
						values.put(TxDB.Qun.QU_AVATAR, txGroup.group_avatar);
						values.put(TxDB.Qun.QU_OWN_ID, txGroup.group_own_id);
						values.put(TxDB.Qun.QU_OWN_NAME, txGroup.group_own_name);
						values.put(TxDB.Qun.QU_TYPE_CHANNEL,
								txGroup.group_type_channel);
						values.put(TxDB.Qun.QU_TIME, txGroup.group_time);
						values.put(TxDB.Qun.QU_TX_STATE, txGroup.group_tx_state);
						values.put(TxDB.Qun.QU_TX_ADMIN_IDS,
								txGroup.group_tx_admin_ids);
						values.put(TxDB.Qun.QU_TX_ADMIN_NAMES,
								txGroup.group_tx_admin_names);

						TxGroup.updateTxGroup(mSess.getContentResolver(),
								txGroup.group_id, values);
					}
				}
				Bundle b = serverRsp.getBundle();
				b.putParcelableArrayList(MsgHelper.GROUP_LIST, groupList);
				serverRsp.setStatusCode(StatusCode.RSP_OK);
			} else {
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			}
			switch (groupType) {
			case TxDB.QU_GET_TYPE_PUBLIC:
				act = Constants.INTENT_ACTION_PUBLIC_GROUP_2032;
				break;
			case TxDB.QU_GET_TYPE_SEARCH:
				act = Constants.INTENT_ACTION_SEARCH_GROUP;
				break;
			case TxDB.QU_GET_TYPE_OWN:
				act = Constants.INTENT_ACTION_SEARCH_USER;
				break;
			}
			broadcastMsg(act, serverRsp);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 获取黑名单列表 2036
	 * 
	 * @param jo
	 */
	private void dealGroupBlackList(JSONObject jo) {

		try {
			ServerRsp serverRsp = new ServerRsp();
			int d = jo.getInt("d");
			int cp = jo.getInt("cp");
			int ep = jo.getInt("ep");
			ep += 1;
			if (d == 0) {
				ArrayList<String> idList = new ArrayList<String>();
				JSONArray ids = jo.getJSONArray("ids");
				for (int i = 0; i < ids.length(); i++) {
					idList.add(ids.getString(i));
				}
				if (idList.size() == 0) {
					serverRsp.setStatusCode(StatusCode.GET_OVER);
				} else {
					serverRsp.getBundle().putInt("cp", cp);
					serverRsp.getBundle().putInt("ep", ep);
					serverRsp.getBundle().putStringArrayList("idsList", idList);
					serverRsp.setStatusCode(StatusCode.GROUP_FOR_PAGE);
				}
			} else {
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			}
			broadcastMsg(Constants.INTENT_ACTION_BLACK_LIST_GROUP_2036,
					serverRsp);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}

	}

	/**
	 * 获取公共群列表 2032
	 * 
	 * @param jo
	 */
	private synchronized void dealPublicGroup(JSONObject jo) {
		try {
			ServerRsp serverRsp = new ServerRsp();
			int d = jo.getInt("d");
			if (d == 0) {
				ArrayList<String> gidsList = new ArrayList<String>();
				int cp = jo.optInt("cp", 0);
				int ep = jo.getInt("ep");
				if (Utils.debug) {
					Log.i(TAG, "获取公共群列表 ---> cp:" + cp + ",ep:" + ep);
				}
				ep += 1;
				JSONArray gids = jo.getJSONArray("gids");
				if (gids.length() > 0) {
					for (int i = 0; i < gids.length(); i++) {
						long gid = gids.getLong(i);
						TxGroup txGroup = TxGroup.getTxGroup(
								mSess.getContentResolver(), (int) gid);
						if (txGroup == null) {
							txGroup = new TxGroup();
							txGroup.group_id = gid;
						}
						txGroup.group_type = TxDB.QU_GET_TYPE_PUBLIC;
						txGroup.group_index = Integer.valueOf(""
								+ (((cp * 10) + i) + 1));
						// TxGroup.saveTxGroupToDB(txGroup, txdata);//用下面的方法
						// 2014.01.23 shc
						ContentValues values = new ContentValues();
						values.put(TxDB.Qun.QU_TYPE, txGroup.group_type);
						values.put(TxDB.Qun.QU_INDEX, txGroup.group_index);
						if (TxGroup.updateTxGroup(mSess.getContentResolver(),
								txGroup.group_id, values) == null) {
							// 更新失败，插入
							TxGroup.addTxGroup(mSess.getContentResolver(),
									txGroup);
						}
						gidsList.add("" + gid);
					}
					serverRsp.getBundle().putStringArrayList("idsList",
							gidsList);
					serverRsp.getBundle().putInt("cp", cp);
					serverRsp.getBundle().putInt("ep", ep);
					serverRsp.setStatusCode(StatusCode.GROUP_FOR_PAGE);
				} else {
					serverRsp.setStatusCode(StatusCode.GET_OVER);
				}
			} else {
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			}
			broadcastMsg(Constants.INTENT_ACTION_PUBLIC_GROUP_2032, serverRsp);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 处理2030号协议 进入离开群
	 * 
	 * @param jo
	 */
	private void dealInOutGroup(JSONObject jo) {
		try {
			ServerRsp serverRsp = new ServerRsp();
			int d = jo.getInt("d");
			int op = jo.getInt("op");
			int gid = jo.getInt("gid");
			int ol = jo.getInt("ol");
			TxGroup txGroup = TxGroup.getTxGroup(mSess.getContentResolver(),
					(int) gid);
			if (txGroup != null) {
				txGroup.group_ol_num = ol;
				// TxGroup.saveTxGroupToDB(txGroup, txdata);//用下面的方法 2014.01.23
				// shc
				ContentValues values = new ContentValues();
				values.put(TxDB.Qun.OL_NUM, txGroup.group_ol_num);
				TxGroup.updateTxGroup(mSess.getContentResolver(),
						txGroup.group_id, values);
			} else {
				mSess.getSocketHelper().sendGetGroup(gid);
			}
			if (d == 0) {
				if (op == 0) {
					serverRsp.setStatusCode(StatusCode.GROUP_OP_0_SUCCESS);
				} else {
					serverRsp.setStatusCode(StatusCode.GROUP_OP_1_SUCCESS);
				}
			} else if (d == 1) {
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			} else if (d == 2) {
				serverRsp.setStatusCode(StatusCode.GROUP_FULL);
			} else if (d == 3) {
				if (txGroup != null) {
					txGroup.group_tx_state = TxDB.QU_TX_STATE_OUT;
					// TxGroup.saveTxGroupToDB(txGroup, txdata);//用下面的方法
					// 2014.01.23 shc
					ContentValues values = new ContentValues();
					values.put(TxDB.Qun.QU_TX_STATE, txGroup.group_tx_state);
					TxGroup.updateTxGroup(mSess.getContentResolver(),
							txGroup.group_id, values);
				}
				serverRsp.setStatusCode(StatusCode.GROUP_NO_EXIST);
			} else if (d == 4) {
				serverRsp.setStatusCode(StatusCode.USER_IN_BLACK);
			}
			broadcastMsg(Constants.INTENT_ACTION_INOUT_GROUP_2030, serverRsp);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 处理2028号协议 禁言或解除禁言
	 * 
	 * @param jo
	 */
	private void dealShutupGroup(JSONObject jo) {
		try {
			ServerRsp serverRsp = new ServerRsp();
			int d = jo.getInt("d");
			int op = jo.getInt("op");
			long uid = jo.getLong("uid");
			int gid = jo.getInt("gid");
			if (d == 0 || d == 5) {
				// TX tx = TX.tm.getTx(uid);
				TX tx = mSess.getTxMgr().getTx(uid);

				if (tx == null)
					mSess.getSocketHelper().sendGetUserInfor(uid);
				/*
				 * if(op == 0){ txMsg =
				 * TXMessage.createShutup4Admin(mSess.getContext(), uid,
				 * System.currentTimeMillis() / 1000); }else{ txMsg =
				 * TXMessage.createShutup4Admin_clear(mSess.getContext(), uid,
				 * System.currentTimeMillis() / 1000); }
				 * TXMessage.saveTXMessagetoDB(txMsg,
				 * mSess.getContentResolver());
				 */
				serverRsp.setStatusCode(StatusCode.RSP_OK);
				if (d == 5) {
					serverRsp.getBundle().putBoolean("did", true);
				} else {
					serverRsp.getBundle().putBoolean("did", false);
				}
				serverRsp.getBundle().putInt("op", op);
				serverRsp.getBundle().putLong("uid", uid);
			} else if (d == 1) {
				serverRsp.setStatusCode(StatusCode.NO_PERMISSION);
			} else if (d == 2) {
				mSess.getSocketHelper().sendGetGroup(gid);
				serverRsp.setStatusCode(StatusCode.GROUP_NOT_MEMBER);
			} else if (d == 3) {
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			} else if (d == 4) {
				TxGroup txGroup = TxGroup.getTxGroup(
						mSess.getContentResolver(), gid);
				if (txGroup != null) {
					txGroup.group_tx_state = TxDB.QU_TX_STATE_OUT;
					// TxGroup.saveTxGroupToDB(txGroup, txdata);//用下面的方法
					// 2014.01.23 shc
					ContentValues values = new ContentValues();
					values.put(TxDB.Qun.QU_TX_STATE, txGroup.group_tx_state);
					TxGroup.updateTxGroup(mSess.getContentResolver(),
							txGroup.group_id, values);
				}
				serverRsp.setStatusCode(StatusCode.GROUP_NO_EXIST);
			}
			broadcastMsg(Constants.INTENT_ACTION_SHUTUP_GROUP_2028, serverRsp);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}

	}

	/**
	 * 处理2026号协议 加黑或取消 结果
	 * 
	 * @param jo
	 */
	private void dealBlackGroup(JSONObject jo) {
		try {
			ServerRsp serverRsp = new ServerRsp();
			int d = jo.getInt("d");
			int op = jo.getInt("op");
			long uid = jo.getLong("uid");
			int gid = jo.getInt("gid");
			if (d == 0 || d == 5) {
				serverRsp.setStatusCode(StatusCode.RSP_OK);
				if (d == 5) {
					serverRsp.getBundle().putBoolean("did", true);
				} else {
					serverRsp.getBundle().putBoolean("did", false);
				}
				serverRsp.getBundle().putInt("op", op);
				serverRsp.getBundle().putLong("uid", uid);
			} else if (d == 1) {
				serverRsp.setStatusCode(StatusCode.NO_PERMISSION);
			} else if (d == 2) {
				serverRsp.setStatusCode(StatusCode.GROUP_BLACK_LIST_TO_MORE);
			} else if (d == 3) {
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			} else if (d == 4) {
				TxGroup txGroup = TxGroup.getTxGroup(
						mSess.getContentResolver(), gid);
				if (txGroup != null) {
					txGroup.group_tx_state = TxDB.QU_TX_STATE_OUT;
					// TxGroup.saveTxGroupToDB(txGroup, txdata);//用下面的方法
					// 2014.01.23 shc

					ContentValues values = new ContentValues();
					values.put(TxDB.Qun.QU_TX_STATE, txGroup.group_tx_state);
					txGroup.changeAdmin(txGroup, "" + uid, false);// 避免是管理员，加黑后没有删除
					values.put(TxDB.Qun.QU_TX_ADMIN_IDS,
							txGroup.group_tx_admin_ids);
					TxGroup.updateTxGroup(mSess.getContentResolver(),
							txGroup.group_id, values);
				}
				serverRsp.setStatusCode(StatusCode.GROUP_NO_EXIST);
			}
			broadcastMsg(Constants.INTENT_ACTION_ADD_BLACK_GROUP_2026,
					serverRsp);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}

	}

	/**
	 * 处理2024号协议 搜索群
	 * 
	 * @param jo
	 */
	private void dealSearchGroup(JSONObject jo) {
		try {
			int d = jo.getInt("d");
			ServerRsp serverRsp = new ServerRsp();
			if (d == 0) {
				ArrayList<String> gids = new ArrayList<String>();
				JSONArray array = jo.getJSONArray("gids");
				for (int i = 0; i < array.length(); i++) {
					gids.add(array.getString(i));
				}
				if (gids.size() == 0) {
					serverRsp.setStatusCode(StatusCode.GROUP_NO_EXIST);
				} else {
					GroupListManager.getInstance().init();
					GroupListManager.getInstance().addGroupIds(gids);
					serverRsp.setStatusCode(StatusCode.GROUP_FOR_PAGE);
				}
			} else {
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			}
			broadcastMsg(Constants.INTENT_ACTION_SEARCH_GROUP, serverRsp);

		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 处理2022号协议 设置群管理员应答
	 * 
	 * @param jo
	 */
	private void dealSetAdminGroup(JSONObject jo) {
		try {
			ServerRsp serverRsp = new ServerRsp();

			int d = jo.getInt("d");
			int uid = jo.getInt("uid");
			int op = jo.getInt("op");
			int gid = jo.getInt("gid");
			if (d == 0) {
				TxGroup txGroup = TxGroup.getTxGroup(
						mSess.getContentResolver(), gid);
				if (txGroup != null) {
					if (op == 0) {
						txGroup.changeAdmin(txGroup, "" + uid, true);
					} else {
						txGroup.changeAdmin(txGroup, "" + uid, false);
					}
					// TxGroup.saveTxGroupToDB(txGroup, txdata);//用下面的方法
					// 2014.01.23 shc
					ContentValues values = new ContentValues();
					values.put(TxDB.Qun.QU_TX_ADMIN_IDS,
							txGroup.group_tx_admin_ids);
					TxGroup.updateTxGroup(mSess.getContentResolver(),
							txGroup.group_id, values);
				} else {
					// 重新获取群组信息
					mSess.getSocketHelper().sendGetGroup(gid);
				}
				serverRsp.getBundle().putInt("op", op);
				serverRsp.getBundle().putLong("uid", uid);
				serverRsp.setStatusCode(StatusCode.RSP_OK);
			} else if (d == 1) {
				serverRsp.setStatusCode(StatusCode.NO_PERMISSION);
			} else if (d == 2) {
				serverRsp.getBundle().putLong("uid", uid);
				mSess.getSocketHelper().sendGetGroup(gid);
				serverRsp.setStatusCode(StatusCode.GROUP_NOT_MEMBER);
			} else if (d == 3) {
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			} else if (d == 4) {
				serverRsp.setStatusCode(StatusCode.ADMIN_FUll);
			}
			broadcastMsg(Constants.INTENT_ACTION_SET_ADMIN_GROUP_2022,
					serverRsp);

		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 处理2020号协议 同意加入群
	 * 
	 * @param jo
	 */
	private void dealAgereeGroup(JSONObject jo) {
		try {
			ServerRsp serverRsp = new ServerRsp();

			int d = jo.getInt("d");
			int gid = jo.getInt("gid");
			long uid = jo.getLong("uid");
			long clisn = jo.getLong("clisn");
			boolean agree = jo.getBoolean("agree");
			if (d == 0) {
				serverRsp.getBundle().putBoolean("agree", agree);
				serverRsp.getBundle().putInt("gid", gid);
				serverRsp.getBundle().putLong("uid", uid);
				serverRsp.getBundle().putLong("clisn", clisn);
				serverRsp.setStatusCode(StatusCode.RSP_OK);
				TxGroup txGroup = TxGroup.getTxGroup(
						mSess.getContentResolver(), gid);
				if (txGroup != null) {
					txGroup = txGroup.changeMembers(txGroup, uid, true);// 用下面的方法，这个方法只修改了group_tx_ids、group_tx_ids两个值
																		// 2014.01.23
																		// shc
					// TxGroup.saveTxGroupToDB(txGroup, txdata);

					ContentValues values = new ContentValues();
					values.put(TxDB.Qun.QU_TX_IDS, txGroup.group_tx_ids);
					values.put(TxDB.Qun.ALL_NUM, txGroup.group_all_num);
					TxGroup.updateTxGroup(mSess.getContentResolver(),
							txGroup.group_id, values);
				} else {
					mSess.getSocketHelper().sendGetGroup(gid);
				}
			} else if (d == 1) {
				serverRsp.setStatusCode(StatusCode.GROUP_FULL);
			} else if (d == 2) {
				serverRsp
						.setStatusCode(StatusCode.GROUP_MEMBER_OPT_NO_PERMISSION);
			} else if (d == 3) {
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			} else if (d == 4) {
				serverRsp
						.setStatusCode(StatusCode.GROUP_MODIFY_GROUP_NOT_EXIST);
			}
			broadcastMsg(Constants.INTENT_ACTION_AGREE_GROUP_2020, serverRsp);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 处理2018号协议 加入群
	 * 
	 * @param jo
	 */
	private void dealJoinGroup(JSONObject jo) {
		try {
			int d = jo.getInt("d");
			int gid = jo.getInt("gid");
			ServerRsp serverRsp = new ServerRsp();
			TxGroup txGroup = TxGroup.getTxGroup(mSess.getContentResolver(),
					gid);
			if (d == 0) {
				if (txGroup != null) {
					synchronized (txGroup) {
						TX me = mSess.getTxMgr().getTx(mSess.getUserid());
						txGroup.changeMembers(txGroup, me.partner_id, true);
						txGroup.group_tx_state = TxDB.QU_TX_STATE_CM;
						// TxGroup.saveTxGroupToDB(txGroup, txdata);//用下面的方法
						// 2014.01.23 shc
						ContentValues values = new ContentValues();
						values.put(TxDB.Qun.QU_TX_IDS, txGroup.group_tx_ids);
						values.put(TxDB.Qun.ALL_NUM, txGroup.group_all_num);
						values.put(TxDB.Qun.QU_TX_STATE, txGroup.group_tx_state);
						TxGroup.updateTxGroup(mSess.getContentResolver(),
								txGroup.group_id, values);
					}
				} else {
					mSess.getSocketHelper().sendGetGroup(gid);
				}
				serverRsp.getBundle().putParcelable("txGroup", txGroup);
				serverRsp.setStatusCode(StatusCode.RSP_OK);
			} else if (d == 1) {
				serverRsp.setStatusCode(StatusCode.GROUP_REQUEST_SUCCESS);
			} else if (d == 2) {
				serverRsp.setStatusCode(StatusCode.GROUP_FULL);
			} else if (d == 3) {
				serverRsp.setStatusCode(StatusCode.GROUP_IN_BLACK_LIST);
			} else if (d == 4) {
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			}
			broadcastMsg(Constants.INTENT_ACTION_JOIN_GROUP_2018, serverRsp);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	// 无引用 2014.04.23 shc
	// private void notificationSound() {
	//
	// // boolean bSound = prefsMeme.getBoolean(CommonData.SOUND, true);
	// boolean bSound = mSess.mPrefMeme.sound.getVal();
	// if (Utils.debug)
	// Log.i(TAG, "++++++++++++++++++++++++++++++++++++++++" + bSound);
	// if (bSound) {
	// Uri alert =
	// RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	// MediaPlayer player = new MediaPlayer();
	// try {
	// player.setDataSource(mSess.getContext(), alert);
	// final AudioManager audioManager = (AudioManager)
	// mSess.getContext().getSystemService(Context.AUDIO_SERVICE);
	// if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
	// player.setAudioStreamType(AudioManager.STREAM_ALARM);
	// // player.setLooping(true);
	// player.prepare();
	// player.start();
	// }
	// } catch (IllegalArgumentException e) {
	// if (Utils.debug)
	// e.printStackTrace();
	// } catch (SecurityException e) {
	// if (Utils.debug)
	// e.printStackTrace();
	// } catch (IllegalStateException e) {
	// if (Utils.debug)
	// e.printStackTrace();
	// } catch (IOException e) {
	// if (Utils.debug)
	// e.printStackTrace();
	// }
	// }
	// boolean bVibrate = prefsMeme.getBoolean(PrefsMeme.VIBRATE, false);
	// if (bVibrate) {
	// Vibrator vibrator = (Vibrator)
	// tuixinService.getSystemService(Context.VIBRATOR_SERVICE);
	// long[] pattern = { 0, 100, 200, 300 };
	// vibrator.vibrate(pattern, -1);
	// }
	// }

	public void dealSystemMsgs(JSONArray ja) {
		JSONObject jo = null;
		int len = ja.length();
		for (int i = 0; i < len; i++) {
			try {
				jo = ja.getJSONObject(i);
				dealSystemMsg(jo);
			} catch (JSONException e) {
				if (Utils.debug)
					e.printStackTrace();
			}
		}
	}

	public static String TOTAL_SIZE = "total_size";

	/**
	 * 2014 获得用户的群
	 * 
	 * @param jo
	 * @param msg
	 */
	public void dealUserQun(JSONObject jo, String msg) {// TODO
														// 这个方法怎么回事儿？没有返回群的详细资料啊
		JSONArray ja = jo.optJSONArray("grouplist");
		if (ja != null) {
			JSONObject joo = null;
			ArrayList<String> ids = new ArrayList<String>();
			for (int i = 0; i < ja.length(); i++) {
				try {
					joo = ja.getJSONObject(i);
					long id = joo.optInt("gid", 0);
					TxGroup txGroup = TxGroup.getTxGroup(
							mSess.getContentResolver(), (int) id);
					if (txGroup == null) {
						txGroup = new TxGroup();
						txGroup.group_id = id;
						// TxGroup.saveTxGroupToDB(txGroup, txdata);//用下面的方法
						// 2014.01.23 shc
						TxGroup.addTxGroup(mSess.getContentResolver(), txGroup);
					}

					ids.add(String.valueOf(id));
				} catch (JSONException e) {
					if (Utils.debug)
						e.printStackTrace();
				}
			}
			if (ids.size() != 0) {
				GroupListManager.getInstance().init();
				GroupListManager.getInstance().addGroupIds(ids);
				GroupListManager.getInstance().getUserGroups4Server();

				ServerRsp serverRsp = new ServerRsp();
				serverRsp.getBundle().putInt(TOTAL_SIZE, ids.size());
				serverRsp.setStatusCode(StatusCode.GROUP_FOR_PAGE);
				broadcastMsg(Constants.INTENT_ACTION_SEARCH_USER, serverRsp);
			}
		}
	}

	private TxGroup setGroupNotice(int flag, TxGroup txGroup) {
		switch (flag) {
		case 0:
			txGroup.rcv_msg = 1;
			txGroup.rcv_push = 1;
			break;
		case 1:
			txGroup.rcv_msg = 0;
			txGroup.rcv_push = 1;
			break;
		case 2:
			txGroup.rcv_msg = 1;
			txGroup.rcv_push = 0;
			break;
		case 3:
			txGroup.rcv_msg = 0;
			txGroup.rcv_push = 0;
			break;
		}
		return txGroup;

	}

	/** 收到系统推送消息 34号协议 */
	public void dealSystemMsg(JSONObject jo) {
		try {
			int st = jo.getInt("s");// 子协议
			ServerRsp serverRsp = new ServerRsp();
			if (st == 0) {
				// 你可能认识某人，通过通讯录匹配

				// 这个子协议应该没有用，没见过系统推送通讯录好友，这方法内代码逻辑可能有问题，用时再修改 2014.01.21 shc

				serverRsp.setStatusCode(StatusCode.SYSTEM_MSG_MAYBE_KNOW);
				String time = jo.getString("t");
				Long ltime = Long.parseLong(time);
				JSONObject jooo = jo.getJSONObject("obj");
				String ac = jooo.optString("ac", "");
				JSONObject joo = jooo.getJSONObject("um");
				if (joo != null) {
					String id = joo.optString("id", "");
					String un = joo.optString("un", "");
					String ph = joo.optString("ph", "");
					ph = Utils.get11Number(ph);
					mSess.getSocketHelper().sendAddPartener(Long.parseLong(id),
							ac, "");
					String em = joo.optString("em", "");
					boolean obd = joo.getBoolean("obd");
					boolean ebd = joo.getBoolean("ebd");
					String avatarurl = joo.optString("avatarurl", "");
					int sex = joo.optInt("sex", TX.DEFAULT_SEX);
					String area = joo.optString("area", "");
					String sign = joo.optString("sign", "");

					long partner_id = Long.parseLong(id);

					TX localTx = new TX();
					localTx.partner_id = partner_id;
					localTx.setNick_name(un);
					localTx.setEmail(em);
					localTx.setPhoneBind(obd);
					localTx.setEmailBind(ebd);
					localTx.setArea(area);
					localTx.setSex(sex);
					localTx.setAvatar_url(avatarurl);
					localTx.setSign(sign);
					localTx.setPhone(ph);

					mSess.mMsgHandler.onServerSystem_34_0(localTx);

					if ((ltime + "".length()) <= 10) {
						ltime = ltime * 1000;
					}
					TXMessage tmsg = TXMessage.creatContactssms(
							mSess.getContext(), un, TX.TUIXIN_FRIEND, "好友管家",
							"通讯录好友:" + un, un, Long.parseLong(id), sign, sex,
							ph, avatarurl, false, 2, ltime);
					boolean isNew = true;
					Cursor c = cr.query(TxDB.Messages.CONTENT_URI,
							new String[] { TxDB.Messages.MSG_ID },
							TxDB.Messages.TCARD_ID + "=? AND "
									+ TxDB.Messages.MSG_PARTNER_ID + " = "
									+ TX.TUIXIN_FRIEND, new String[] { ""
									+ tmsg.tcard_id }, null);
					if (c != null) {
						if (c.moveToFirst()) {
							tmsg.msg_id = c.getLong(0);
							isNew = false;
						}
						c.close();
					}
					tmsg.ac = ac;
					if (isNew) {
						TXMessage.saveTXMessagetoDB(tmsg,
								mSess.getContentResolver(), true);
					} else {
						TXMessage.updateTcardTXMessage(cr, tmsg);
					}
					int l = jo.toString().getBytes().length;
				}
			} else if (st == 2) {
				// 服务器忙
				serverRsp.setStatusCode(StatusCode.SERVER_BUSY);
			} else if (st == 3) {
				// 帐号在其他地方登录,当前登录点被踢下线
				serverRsp.setStatusCode(StatusCode.SYSTEM_MSG_LOGIN_OTHER);
				mSess.getSocketHelper().recovery();
				mSess.clear();
				mSess.mPrefMeme.exit.setVal(PrefsMeme.USER_EXIT).commit();
			} else if (st == 4) {
				// 弹出系统对话框
				serverRsp.setStatusCode(StatusCode.SYSTEM_MSG_SYS_DIALOG);
				JSONObject jooo = jo.getJSONObject("obj");
				String msg = jooo.getString("msg");
				String title = jooo.getString("title");
				serverRsp.putString("msg", msg);
				serverRsp.putString("title", title);
			} else if (st == 5) {
				// 手机号绑定成功
				serverRsp.setStatusCode(StatusCode.SYSTEM_MSG_PHONE_BINDED);
				// if (prefsMeme == null) {
				// prefsMeme =
				// mSess.getContext().getSharedPreferences(PrefsMeme.MEME_PREFS,
				// Context.MODE_PRIVATE);
				// }
				// boolean tel_bind_state =
				// prefsMeme.getBoolean(CommonData.IS_BIND_PHONE, false);
				boolean tel_bind_state = mSess.mPrefMeme.is_bind_phone.getVal();
				if (!tel_bind_state) {
					JSONObject jooo = jo.getJSONObject("obj");
					String tel = jooo.getString("o");
					int tel_bind = jooo.getInt("d");
					if (!"".equals(tel)) {
						// Editor e = prefsMeme.edit();
						switch (tel_bind) {
						case 0:
							// e.putBoolean(CommonData.IS_BIND_PHONE, true);
							// e.putString(CommonData.TELEPHONE, tel);
							// e.putInt(CommonData.TEL_BIND_STATE,
							// CommonData.TEL_BIND_SUCCESS);
							mSess.mPrefMeme.is_bind_phone.setVal(true);
							mSess.mPrefMeme.telephone.setVal(tel);
							mSess.mPrefMeme.tel_bind_state
									.setVal(PrefsMeme.TEL_BIND_SUCCESS);

							// 获取我的最新个人信息
							mSess.getSocketHelper().getNewUserinforForLevel();

							break;
						case 1:
							// e.putBoolean(CommonData.IS_BIND_PHONE, false);
							// e.putInt(CommonData.TEL_BIND_STATE,
							// CommonData.TEL_HAVE_BINDED);
							mSess.mPrefMeme.is_bind_phone.setVal(false);
							mSess.mPrefMeme.tel_bind_state
									.setVal(PrefsMeme.TEL_HAVE_BINDED);

							break;
						case 2:
							// e.putBoolean(CommonData.IS_BIND_PHONE, true);
							// e.putString(CommonData.TELEPHONE, tel);
							// e.putInt(CommonData.TEL_BIND_STATE,
							// CommonData.TEL_BIND_SUCCESS);

							mSess.mPrefMeme.is_bind_phone.setVal(true);
							mSess.mPrefMeme.telephone.setVal(tel);
							mSess.mPrefMeme.tel_bind_state
									.setVal(PrefsMeme.TEL_BIND_SUCCESS);

							// 获取我的最新个人信息
							mSess.getSocketHelper().getNewUserinforForLevel();

							break;
						case 3:
							// e.putBoolean(CommonData.IS_BIND_PHONE, false);
							// e.putInt(CommonData.TEL_BIND_STATE,
							// CommonData.TEL_BIND_FAILED);

							mSess.mPrefMeme.is_bind_phone.setVal(false);
							mSess.mPrefMeme.tel_bind_state
									.setVal(PrefsMeme.TEL_BIND_FAILED);

							break;
						}
						mSess.mPrefMeme.commit();
						// e.commit();
						// TX.tm.reloadTXMe();// //

						mSess.mMsgHandler.onReloadTXMe();

					}
				}
			} else if (st == 6) {
				// 邮箱绑定成功
				serverRsp.setStatusCode(StatusCode.SYSTEM_MSG_EMAIL_BINDED);
				// if (prefsMeme == null) {
				// prefsMeme = txdata.getSharedPreferences(PrefsMeme.MEME_PREFS,
				// Context.MODE_PRIVATE);
				// }
				// boolean email_bind_state =
				// prefsMeme.getBoolean(CommonData.IS_BIND_EMAIL, false);
				boolean email_bind_state = mSess.mPrefMeme.is_bind_email
						.getVal();
				if (!email_bind_state) {
					JSONObject jooo = jo.getJSONObject("obj");
					String em = jooo.getString("em");
					if (Utils.isNull(em)) {
						// Editor e = prefsMeme.edit();
						// e.putString(CommonData.EMAIL, em);
						// e.putBoolean(CommonData.IS_BIND_EMAIL, true);
						// e.commit();
						mSess.mPrefMeme.email.setVal(em);
						mSess.mPrefMeme.is_bind_email.setVal(true).commit();
						// TX.tm.reloadTXMe();// ///
						mSess.mMsgHandler.onReloadTXMe();

						// 获取我的最新个人信息
						mSess.getSocketHelper().getNewUserinforForLevel();

					}
				}
			} else if (st == 7) {
				// 某某群组邀请你参加/你被某某群组删除了
				serverRsp.setStatusCode(StatusCode.SYSTEM_MSG_GROUP_OPT_INFO);
				mSess.getSocketHelper().dealGroupNotice(jo);
			} else if (st == 8) {
				// 某人向你打招呼
				serverRsp.setStatusCode(StatusCode.SYSTEM_MSG_GREET);
				dealSystemMsgs34_08(jo);
			} else if (st == 9) {
				// SNS好友推送
				serverRsp.setStatusCode(StatusCode.SYSTEM_MSG_SNS_FRIEND);
				dealSystemMsgs34_09(jo);
			} else if (st == 10) {
				// 加好友请求
				serverRsp.setStatusCode(StatusCode.SYSTEM_MSG_JOIN_FRIEND);
				dealSystemMsgs34_10(jo);
			} else if (st == 11) {
				// 好友验证结果
				serverRsp.setStatusCode(StatusCode.SYSTEM_MSG_VERIFY_FRIEND);
				dealSystemMsgs34_11(jo, serverRsp);
			} else if (st == 12) {
				// 你被某人加入黑名单或从黑名单移除
				serverRsp.setStatusCode(StatusCode.SYSTEM_MSG_BLACK_LIST_OPT);
				JSONObject jooo = jo.getJSONObject("obj");
				Long id = jooo.getLong("i");
				int type = jooo.getInt("type");

				// 应该不能用TX_ME_IN_BLACK_LIST标记值，如果对方在我黑名单里面，这时对方把我加入黑名单，我设置值为TX_ME_IN_BLACK_LIST，那么对方把我从黑名单移除，我又设置值为TX_NOT_IN_BLACK_LIST就相当于我自动把对方从黑名单里面移除了。
				if (type == 0) {
					// type == 0，我被该tx加入黑名单

					mSess.mMsgHandler.onServerSystem_34_12(id, 0);

				} else {
					// 我被该tx移除黑名单

					// 应该不需要做操作 2013.11.05 shc
				}

			} else if (st == 13) {
				// 你被群禁言或解除禁言
				serverRsp.setStatusCode(StatusCode.SYSTEM_MSG_SHUTUP);
				dealSystemMsgs34_13(jo);
			} else if (st == 14) {
				// 加群申请（给管理员）
				dealSystemMsgs34_14(jo);
				serverRsp.setStatusCode(StatusCode.SYSTEM_MSG_REQUEST_GROUP);
			} else if (st == 15) {
				// 你被设置为群管理员或被取消管理员权限
				// 0设置,1取消
				dealSystemMsgs34_15(jo);
				serverRsp.setStatusCode(StatusCode.SYSTEM_MSG_SET_ADMIN);
				// 加群结果 给申请人
			} else if (st == 16) {
				// 加群申请结果（给申请者）
				serverRsp.setStatusCode(StatusCode.RSP_OK);
				dealSystemMsgs34_16(jo, serverRsp);
				broadcastMsg(Constants.INTENT_ACTION_AGREE_GROUP_JOIN,
						serverRsp);
			} else if (st == 17) {
				// 警告
				serverRsp.setStatusCode(StatusCode.SYSTEM_MSG_WARN);
				dealSystemMsgs34_17(jo, serverRsp);
			} else if (st == 18) {
				// 举报
				serverRsp.setStatusCode(StatusCode.SYSTEM_MSG_REPORT);
				dealSystemMsgs34_18(jo, serverRsp);
			} else if (st == 19) {
				// 聊天室被官方解散
				serverRsp.setStatusCode(StatusCode.RSP_OK);
				dealSystemMsgs34_19(jo, serverRsp);
			} else if (st == 20) {
				// 神聊小卫士推送消息
				serverRsp.setStatusCode(StatusCode.SYSTEM_MSG_WARN);
				dealSystemMsgs34_20(jo, serverRsp);
			} else if (st == 21) {
				// 被喜欢的瞬间提醒通知消息
				serverRsp.setStatusCode(StatusCode.NOTICE_BLOG_LIKED);
				dealSystemMsgs34_21(jo);
			} else if (st == 22) {
				serverRsp.setStatusCode(StatusCode.BLOG_DELETE_BY_OP);
				dealSystemMsgs34_22(jo);
			}
			broadcastMsg(Constants.INTENT_ACTION_SYSTEM_MSG, serverRsp);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
	}

	private void dealSystemMsgs34_19(JSONObject jo, ServerRsp serverRsp) {
		try {

			JSONObject joo = jo.getJSONObject("obj");
			int gid = joo.getInt("gid");
			String topic = joo.optString("topic", "");
			String rs = joo.optString("rs", "");

			TxGroup txGroup = TxGroup.getTxGroup(mSess.getContentResolver(),
					gid);
			if (txGroup != null) {
				txGroup.group_tx_state = TxDB.QU_TX_STATE_OUT;
				// TxGroup.saveTxGroupToDB(txGroup, txdata);//用下面的方法 2014.01.23
				// shc
				ContentValues values = new ContentValues();
				values.put(TxDB.Qun.QU_TX_STATE, txGroup.group_tx_state);
				TxGroup.updateTxGroup(mSess.getContentResolver(),
						txGroup.group_id, values);
			}

			TXMessage txMsg = TXMessage.createGuanDismiss(mSess.getContext(),
					gid, topic, rs, System.currentTimeMillis() / 1000);
			TXMessage
					.saveTXMessagetoDB(txMsg, mSess.getContentResolver(), true);
			mSess.getSocketHelper().showNotification(txMsg, false);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}

	}

	/**
	 * 禁言或解禁
	 * 
	 * @param jo
	 */
	private void dealSystemMsgs34_13(JSONObject jo) {
		try {
			JSONObject joo = jo.getJSONObject("obj");
			int gid = joo.getInt("gid");
			int op = joo.getInt("op");
			int opId = joo.getInt("opid");
			String opName = joo.getString("opn");
			TXMessage txMsg = null;
			if (op == 0) {
				long st = joo.getLong("st");
				long du = joo.getLong("du");
				txMsg = TXMessage.createShutup(mSess.getContext(), gid, st, du,
						opId, opName, System.currentTimeMillis() / 1000);
			} else {
				txMsg = TXMessage.createShutupOver(mSess.getContext(), gid,
						opId, opName, System.currentTimeMillis() / 1000);
			}

			TXMessage
					.saveTXMessagetoDB(txMsg, mSess.getContentResolver(), true);
			mSess.getSocketHelper().showNotification(txMsg, false);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}

	}

	/**
	 * 警告给用户
	 */
	private void dealSystemMsgs34_17(JSONObject jo, ServerRsp serverRsp) {

		JSONObject joo;
		try {

			joo = jo.getJSONObject("obj");
			String warnContext = joo.optString("rs");
			TXMessage txMessage = TXMessage.createWarn(mSess.getContext(), 0,
					"", System.currentTimeMillis() / 1000);
			txMessage.msg_body = warnContext;
			TXMessage.saveTXMessagetoDB(txMessage, mSess.getContentResolver(),
					true);
			mSess.getSocketHelper().showNotification(txMessage, false);
			Intent i = new Intent(Constants.INTENT_ACTION_WARN_USER_OTHER);
			i.putExtra("warnMsg", txMessage);

			mSess.getContext().sendBroadcast(i);

			Intent intent = new Intent(mSess.getContext(),
					WarnDialogAcitvity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("rs", warnContext);
			mSess.getContext().startActivity(intent);

		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
		}

	}

	/**
	 * 系统警告给用户
	 */
	private void dealSystemMsgs34_20(JSONObject jo, ServerRsp serverRsp) {

		JSONObject joo;
		try {
			joo = jo.getJSONObject("obj");
			String warnContext = joo.optString("sysmsg");
			TXMessage txMessage = TXMessage.createWarn(mSess.getContext(), 0,
					"", System.currentTimeMillis() / 1000);
			txMessage.msg_body = warnContext;
			TXMessage.saveTXMessagetoDB(txMessage, mSess.getContentResolver(),
					true);
			mSess.getSocketHelper().showNotification(txMessage, false);

		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}

	}

	/** 被喜欢的瞬间提醒通知消息 */
	private void dealSystemMsgs34_21(JSONObject jo) {

		JSONObject joo;
		try {
			joo = jo.getJSONObject("obj");
			String blogId = joo.optString("mid");// 瞬间id
			long uid = joo.optLong("uid");// 喜欢此瞬间的用户id
			int flag = joo.optInt("flag");// 事件标记
			int time = joo.optInt("time");// 喜欢事件发生的时间

			mSess.mMsgHandler.onServerSystem_34_21(Long.parseLong(blogId), uid,
					time);

		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 瞬间被管理员删除的通知消息 */
	private void dealSystemMsgs34_22(JSONObject jo) {

		JSONObject joo;
		try {
			joo = jo.getJSONObject("obj");
			String blogId = joo.optString("mid");// 瞬间id
			int uid = joo.optInt("uid");// 删除此瞬间的用户id
			int time = joo.optInt("time");// 删除事件发生的时间？

			mSess.mMsgHandler.onServerSystem_34_22(Long.parseLong(blogId), uid,
					time);

		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 收到服务器推送的举报信息 */
	private void dealSystemMsgs34_18(JSONObject jo, ServerRsp serverRsp) {
		try {
			JSONObject joo = jo.getJSONObject("obj");
			// from 举报人
			JSONObject from = joo.getJSONObject("from");
			String fName = from.optString("n");// 举报人昵称
			Long fId = from.optLong("i", 0);// 举报人id
			// desc 被举报者
			JSONObject dest = joo.getJSONObject("dest");

			// 默认一定要先存成-1
			long groupid = -1;

			String name = dest.optString("n");
			long senderid = dest.optLong("i");// 被举报人的ID
			String avatar = dest.optString("a");
			int sex = dest.optInt("x");

			TX txDest = new TX();
			txDest.partner_id = senderid;
			txDest.setSex(sex);
			txDest.setNick_name(name);
			txDest.avatar_url = avatar;

			mSess.mMsgHandler.onServerSystem_34_18(txDest);

			mSess.getSocketHelper().sendGetUserInfor(senderid);

			Long ltime = System.currentTimeMillis() / 1000;

			JSONObject msg = joo.getJSONObject("msg");

			TXMessage txmsg = null;
			// 说明有具体消息被举报
			if (msg != null) {
				JSONObject obj = msg.optJSONObject("obj");
				String groupname = "";
				String img_downlod = "";
				String img = "";
				String audio_path = "";
				String audio_url = "";
				String audioend = "";
				long audio_length = 0;
				long audio_times = 0;
				String geo = "";
				int msg_type = TxDB.MSG_TYPE_QU_COMMON_SMS;
				String cardUrl = "";
				String cardfile = "";
				String cardName = "";
				long tcard_id = -1;
				String tcard_name = "";// 名片姓名
				String tcard_sign = "";// 名片签名
				int tcard_sex = TX.MALE_SEX;// 名片性别
				String tcard_avatar_url = "";
				String tcard_phone = "";// 名片电话
				String file_url = "";// 大文件url
				int file_length = 0;// 大文件文件大小
				int pkgid = -1;//gif所属的包id
				String emomd5 = "";//gif的md5值

				if (obj != null) {
					img_downlod = obj.optString("img", "");
					if (!Utils.isNull(img_downlod)) {
						img = receiveSameImg(img_downlod);
						msg_type = TxDB.MSG_TYPE_QU_IMAGE_EMS;
					}
					JSONObject vcard = obj.optJSONObject("card");
					if (vcard != null) {
						cardUrl = vcard.optString("u", "");
						cardName = vcard.optString("d", "");
						if (!Utils.isNull(cardUrl)) {
							cardfile = receiveSameCard(cardUrl);
							msg_type = TxDB.MSG_TYPE_QU_CARD_EMS;
						}
					}
					JSONObject tcard = obj.optJSONObject("tcard");
					if (tcard != null) {
						msg_type = TxDB.MSG_TYPE_QU_TCARD_SMS;
						tcard_name = tcard.optString("nn", "");
						tcard_id = tcard.optInt("id", -1);
						tcard_sign = tcard.optString("sign", "");
						tcard_sex = tcard.optInt("sex", TX.MALE_SEX);
						tcard_avatar_url = tcard.optString("avatar", "");
						tcard_phone = tcard.optString("phone", "");
					}
					JSONObject adu = obj.optJSONObject("adu");
					if (adu != null) {
						audio_url = adu.optString("url", "");
						if (!Utils.isNull(audio_url)) {
							audio_path = receiveSameAudio(audio_url);
							msg_type = TxDB.MSG_TYPE_QU_AUDIO_EMS;
						}
						audioend = adu.optString("end", "");
						audio_length = adu.optLong("l", 0);
						audio_times = adu.optLong("t", 0);
					}
					JSONObject geo_jsonObj = obj.optJSONObject("geo");
					if (geo_jsonObj != null) {
						double la = geo_jsonObj.optDouble("la", 0);
						double lo = geo_jsonObj.optDouble("lo", 0);
						geo = la + "," + lo;
						if (la == 0 && lo == 0)
							geo = "";
						msg_type = TxDB.MSG_TYPE_QU_GEO_SMS;
					}
					JSONObject bigFile_jsonObj = obj.optJSONObject("file");
					if (bigFile_jsonObj != null) {
						file_url = bigFile_jsonObj.optString("url");
						file_length = bigFile_jsonObj.optInt("l");
						msg_type = TxDB.MSG_TYPE_QU_BIG_FILE_SMS;
					}
					JSONObject gif_jsonObj = obj.optJSONObject("emot");
					if (bigFile_jsonObj != null) {
						pkgid = gif_jsonObj.optInt("pkgid");
						emomd5 = gif_jsonObj.optString("emomd5");
						msg_type = TxDB.MSG_TYPE_QU_GIF_SMS;
					}
				}

				// String partner_phone = "";// 不给新创建的消息设置的电话号码了 2014.01.23 shc
				// // 从本地查询电话号码和昵称
				// TX tempTx = TX.tm.getTx(senderid);
				// if (tempTx != null) {
				// partner_phone = tempTx.getPhone();
				// }

				JSONObject sms = msg.getJSONObject("sm");
				if (sms != null) {
					String smsid = sms.getString("id");
					Long sms_id = Long.parseLong(smsid);
					String sms_content = sms.getString("ct");
					switch (msg_type) {
					case TxDB.MSG_TYPE_COMMON_SMS:
						txmsg = TXMessage.creatCommonSmsWhenReceive(
								mSess.getContext(), sms_id, senderid, name,
								sms_content, false, 2, ltime);
						txmsg.msg_id2 = sms_id;
						txmsg.msg_type2 = TxDB.MSG_TYPE_COMMON_SMS;
						break;
					case TxDB.MSG_TYPE_AUDIO_EMS:
						txmsg = TXMessage.creatAudioEms(mSess.getContext(),
								sms_id, senderid, name, audio_path, audio_url,
								audio_length, audio_times, audioend, false,
								TXMessage.UNREAD, ltime);
						txmsg.msg_id2 = sms_id;
						txmsg.msg_type2 = TxDB.MSG_TYPE_AUDIO_EMS;
						break;
					case TxDB.MSG_TYPE_CARD_EMS:
						txmsg = TXMessage.creatCardEms(mSess.getContext(),
								sms_id, cardName, senderid, name, cardfile,
								cardUrl, false, 2, ltime);
						txmsg.msg_id2 = sms_id;
						txmsg.msg_type2 = TxDB.MSG_TYPE_CARD_EMS;
						break;
					case TxDB.MSG_TYPE_IMAGE_EMS:
						txmsg = TXMessage.creatImageEms(mSess.getContext(),
								sms_id, senderid, name, img, img_downlod,
								false, 2, ltime);
						txmsg.msg_id2 = sms_id;
						txmsg.msg_type2 = TxDB.MSG_TYPE_IMAGE_EMS;
						break;
					case TxDB.MSG_TYPE_GEO_SMS:
						txmsg = TXMessage.creatGeoSms(mSess.getContext(),
								sms_id, senderid, name, geo, false, 2, ltime);
						txmsg.msg_id2 = sms_id;
						txmsg.msg_type2 = TxDB.MSG_TYPE_GEO_SMS;
						break;
					case TxDB.MSG_TYPE_BIG_FILE_SMS:// TODO 这里有问题吧，举报消息到底怎么生成的？
						txmsg = TXMessage.creatBigFileSmsWhenReceive(
								mSess.getContext(), sms_id, senderid, name,
								file_url, file_length, ltime);
						txmsg.msg_id2 = sms_id;
						txmsg.msg_type2 = TxDB.MSG_TYPE_BIG_FILE_SMS;
						break;
					case TxDB.MSG_TYPE_TCARD_SMS:
						txmsg = TXMessage.creatTCardEms(mSess.getContext(),
								sms_id, tcard_name, senderid, name, tcard_name,
								tcard_id, tcard_sign, tcard_sex, tcard_phone,
								tcard_avatar_url, false, 2, ltime);
						txmsg.msg_id2 = sms_id;
						txmsg.msg_type2 = TxDB.MSG_TYPE_TCARD_SMS;
						break;
					case TxDB.MSG_TYPE_QU_COMMON_SMS:
						txmsg = TXMessage.creatGroupCommonSmsWhenReceive(
								this.mSess.getContext(), sms_id, senderid,
								name, groupid, groupname, avatar, sms_content,
								false, 2, ltime);
						txmsg.msg_id2 = sms_id;
						txmsg.msg_type2 = TxDB.MSG_TYPE_QU_COMMON_SMS;
						break;
					case TxDB.MSG_TYPE_QU_AUDIO_EMS:
						txmsg = TXMessage.creatGroupAudioEmsWhenReceive(
								this.mSess.getContext(), sms_id, senderid,
								name, groupid, groupname, avatar, audio_path,
								audio_url, audio_length, audio_times, audioend,
								false, 2, ltime);
						txmsg.msg_id2 = sms_id;
						txmsg.msg_type2 = TxDB.MSG_TYPE_QU_AUDIO_EMS;
						break;
					case TxDB.MSG_TYPE_QU_CARD_EMS:
						txmsg = TXMessage.creatGroupCardEmsWhenReceive(
								this.mSess.getContext(), sms_id, cardName,
								senderid, name, groupid, groupname, avatar,
								cardfile, cardUrl, false, 2, ltime);
						txmsg.msg_id2 = sms_id;
						txmsg.msg_type2 = TxDB.MSG_TYPE_QU_CARD_EMS;
						break;
					case TxDB.MSG_TYPE_QU_IMAGE_EMS:
						txmsg = TXMessage.creatGroupImageEmsWhenReceive(
								this.mSess.getContext(), sms_id, senderid,
								name, groupid, groupname, avatar, img,
								img_downlod, false, 2, ltime);
						txmsg.msg_id2 = sms_id;
						txmsg.msg_type2 = TxDB.MSG_TYPE_QU_IMAGE_EMS;
						break;
					case TxDB.MSG_TYPE_QU_GEO_SMS:
						txmsg = TXMessage.creatGroupGeoSmsWhenReceive(
								this.mSess.getContext(), sms_id, senderid,
								name, groupid, groupname, avatar, geo, false,
								2, ltime);
						txmsg.msg_id2 = sms_id;
						txmsg.msg_type2 = TxDB.MSG_TYPE_QU_GEO_SMS;
						break;
					case TxDB.MSG_TYPE_QU_TCARD_SMS:
						txmsg = TXMessage.creatGroupTCardEmsWhenReceive(
								this.mSess.getContext(), sms_id, tcard_name,
								senderid, name, groupid, groupname, avatar,
								tcard_name, tcard_id, tcard_sign, tcard_sex,
								tcard_phone, tcard_avatar_url, false, 2, ltime);
						txmsg.msg_id2 = sms_id;
						txmsg.msg_type2 = TxDB.MSG_TYPE_QU_TCARD_SMS;
						break;
					case TxDB.MSG_TYPE_QU_BIG_FILE_SMS:
						txmsg = TXMessage.creatGroupBigFileSmsWhenReceive(
								mSess.getContext(), sms_id, senderid, name,
								groupid, groupname, avatar, file_url,
								file_length, ltime);
						txmsg.msg_id2 = sms_id;
						txmsg.msg_type2 = TxDB.MSG_TYPE_QU_BIG_FILE_SMS;
						break;
					case TxDB.MSG_TYPE_QU_GIF_SMS:
						txmsg = TXMessage.creatGroupGIFSmsWhenReceive(mSess.getContext(), sms_id, senderid, name,
								groupid, groupname, avatar, emomd5,
								pkgid, ltime);
						txmsg.msg_id2 = sms_id;
						txmsg.msg_type2 = TxDB.MSG_TYPE_QU_GIF_SMS;
					}
				}
			} else {
				txmsg = new TXMessage();
				txmsg.nick_name = name;
			}
			groupid = joo.optLong("gid", 0);
			txmsg.msg_id = Utils.createMsgId("" + mSess.getUserid());
			txmsg.msg_type = TxDB.MSG_TYPE_MANAGER_REPORT_4_ADMIN;
			txmsg.contacts_person_id = Integer.valueOf("" + senderid);// 举报者id??2013.12.20
			txmsg.partner_id = TX.SL_SAFE;// 发送者是神聊小卫士
			txmsg.sex = sex;// 被举报者性别，这么写总感觉不合理 2013.12.20
			txmsg.group_id_notice = groupid;
			txmsg.reportId = fId;
			txmsg.reportName = fName;
			txmsg.reportContext = "";
			String reportContext = "";
			int t = joo.optInt("t", 0);
			if (t != 0) {
				switch (t) {
				case 1:
					reportContext = "骚扰他人";
					break;
				case 2:
					reportContext = "淫秽色情";
					break;
				case 3:
					reportContext = "垃圾广告";
					break;
				case 4:
					reportContext = "虚假中奖";
					break;
				case 5:
					reportContext = "人身攻击";
					break;
				case 6:
					reportContext = "敏感信息";
					break;
				case 7:
					reportContext = joo.optString("desc", "");
					break;
				}
			} else {
				// 这个不对吧？ 2014.03.14 shc
				reportContext = joo.optString("desc", "");
			}
			txmsg.reportContext = reportContext;
			if (groupid != 0) {
				mSess.getSocketHelper().sendGetGroup(groupid);
			}
			TXMessage
					.saveTXMessagetoDB(txmsg, mSess.getContentResolver(), true);

			mSess.getSocketHelper().showNotification(txmsg, false);

		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	private void dealSystemMsgs34_16(JSONObject jo, ServerRsp serverRsp) {
		try {
			JSONObject joo = jo.getJSONObject("obj");
			int gid = joo.getInt("gid");
			boolean agree = joo.getBoolean("agree");
			int opid = joo.getInt("opid");
			String opn = joo.getString("opn");
			String body = "";
			if (agree) {
				body = "你加入聊天室【�slgroup�】" + "(" + gid + ")"
						+ "的申请已经通过，现在可以去体验聊天室功能了";
			} else {
				body = "你加入聊天室【�slgroup�】" + "(" + gid + ")" + "的申请被拒绝了";
			}
			TxGroup txGroup = TxGroup.getTxGroup(mSess.getContentResolver(),
					gid);
			if (txGroup != null && agree) {
				txGroup.changeMembers(txGroup, mSess.getUserid(), true);
				txGroup.group_tx_state = TxDB.QU_TX_STATE_CM;
				// TxGroup.saveTxGroupToDB(txGroup, txdata);//用下面的方法 2014.01.23
				// shc
				ContentValues values = new ContentValues();
				values.put(TxDB.Qun.QU_TX_IDS, txGroup.group_tx_ids);
				values.put(TxDB.Qun.ALL_NUM, txGroup.group_all_num);
				values.put(TxDB.Qun.QU_TX_STATE, txGroup.group_tx_state);
				TxGroup.updateTxGroup(mSess.getContentResolver(),
						txGroup.group_id, values);
				Intent i = new Intent(Constants.INTENT_ACTION_FLUSH_GROUP);
				mSess.getContext().sendBroadcast(i);
			} else {
				mSess.getSocketHelper().sendGetGroup(gid);
			}
			String name = "";
			String groupUrl = "";
			if (txGroup != null) {
				name = txGroup.group_title;
				groupUrl = txGroup.group_avatar;
			}

			TXMessage tmsg = TXMessage.createRequestJoinGroup4Member(
					mSess.getContext(), body, gid, name, groupUrl, agree, opid,
					opn, System.currentTimeMillis() / 1000);
			TXMessage.saveTXMessagetoDB(tmsg, mSess.getContentResolver(), true);

			mSess.getSocketHelper().showNotification(tmsg, false);
			serverRsp.getBundle().putBoolean("agree", agree);

			mSess.getSocketHelper().sendGetUserInfor(joo.getLong("opid"));
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 申请加入群
	 * 
	 * @param jo
	 */
	private void dealSystemMsgs34_14(JSONObject jo) {
		try {
			JSONObject joo = jo.getJSONObject("obj");
			int gid = joo.getInt("gid");
			int uid = joo.getInt("uid");
			String nn = joo.getString("nn");
			String sn = joo.getString("sn");
			String ac = joo.getString("ac");
			String rs = joo.getString("rs");
			TxGroup txGroup = TxGroup.getTxGroup(mSess.getContentResolver(),
					gid);
			String groupName = "";
			String groupUrl = "";
			if (txGroup != null) {
				groupName = txGroup.group_title;
				groupUrl = txGroup.group_avatar;
			}
			TXMessage tmsg = TXMessage.createRequestJoinGroup4Admin(
					mSess.getContext(), uid, nn, gid, groupName, groupUrl, sn,
					ac, rs, System.currentTimeMillis() / 1000);

			boolean isNew = true;
			Cursor c = cr.query(TxDB.Messages.CONTENT_URI,
					new String[] { TxDB.Messages.MSG_ID },
					TxDB.Messages.TCARD_ID + " = ? AND "
							+ TxDB.Messages.GROUP_ID_NOTICE + " = ? AND "
							+ TxDB.Messages.MSG_PARTNER_ID + " = ? AND "
							+ TxDB.Messages.MSG_TYPE + " = ?", new String[] {
							"" + tmsg.tcard_id, "" + tmsg.group_id_notice,
							"" + tmsg.partner_id, "" + tmsg.msg_type }, null);
			if (c != null) {
				if (c.moveToFirst()) {
					tmsg.msg_id = c.getLong(0);
					isNew = false;
				}
				c.close();
			}
			// if (isNew) {
			mSess.getSocketHelper().showNotification(tmsg, false);
			// }

			TXMessage.saveTXMessagetoDB(tmsg, mSess.getContentResolver(), true);

			mSess.getSocketHelper().sendGetUserInfor(uid);
			mSess.getSocketHelper().sendGetGroup(gid);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}

	}

	private void dealSystemMsgs34_15(JSONObject jo) {
		try {
			JSONObject joo = jo.getJSONObject("obj");
			int gid = joo.getInt("gid");
			int op = joo.getInt("op");
			int opId = joo.getInt("opid");
			String opName = joo.getString("opn");
			String body = "";
			if (op == 0) {
				body = opName + "(" + opId + ")" + "设置您为聊天室【�slgroup�】" + "("
						+ gid + ")" + "的管理员";
			} else {

				body = opName + "(" + opId + ")" + "取消了您在聊天室【�slgroup�】" + "("
						+ gid + ")" + "的管理员权限";
			}
			// 群相关数据更改
			TxGroup txGroup = TxGroup.getTxGroup(mSess.getContentResolver(),
					gid);
			String groupName = "";
			String groupUrl = "";
			if (txGroup != null) {
				groupName = txGroup.group_title;
				groupUrl = txGroup.group_avatar;
				if (op == 0) {
					txGroup.changeAdmin(txGroup, "" + mSess.getUserid(), true);
					txGroup.group_tx_state = TxDB.QU_TX_STATE_GM;
				} else {
					txGroup.changeAdmin(txGroup, "" + mSess.getUserid(), false);
					txGroup.group_tx_state = TxDB.QU_TX_STATE_CM;
				}
				// TxGroup.saveTxGroupToDB(txGroup, txdata);//用下面的方法 2014.01.23
				// shc
				ContentValues values = new ContentValues();
				values.put(TxDB.Qun.QU_TX_ADMIN_IDS, txGroup.group_tx_admin_ids);
				values.put(TxDB.Qun.QU_TX_STATE, txGroup.group_tx_state);
				TxGroup.updateTxGroup(mSess.getContentResolver(),
						txGroup.group_id, values);
			} else {
				mSess.getSocketHelper().sendGetGroup(gid);
			}

			TXMessage tmsg = TXMessage.createSetGroupAdmin(mSess.getContext(),
					body, gid, groupName, groupUrl, op, opId, opName,
					System.currentTimeMillis() / 1000);

			mSess.getSocketHelper().showNotification(tmsg, false);
			TXMessage.saveTXMessagetoDB(tmsg, mSess.getContentResolver(), true);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}

	}

	/** 34号协议，11号子协议：好友验证结果 */
	// 这个是对方同意你加为好友时，服务器推送过来的消息（同意或者拒绝）。
	private synchronized void dealSystemMsgs34_11(JSONObject jo,
			ServerRsp serverRsp) {// 发起方
		try {
			JSONObject jooo = jo.getJSONObject("obj");
			Long id = jooo.getLong("i");
			Boolean agree = jooo.getBoolean("agree");
			serverRsp.putBoolean("agree", agree);
			if (agree) {
				// 对方同意将你加为好友

				mSess.mMsgHandler.onServerSystem_34_11(id);

			}
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 好友请求信息
	 * 
	 * @param jo
	 */
	private synchronized void dealSystemMsgs34_10(JSONObject jo) {

		try {
			Long ltime = jo.getLong("t");
			JSONObject jooo = jo.getJSONObject("obj");
			Long id = jooo.getLong("i");
			String nickname = jooo.getString("n");
			String avatarurl = jooo.getString("avatarurl");
			// String area = jooo.getString("area");
			String area = StringUtils.jsonArray2Str(jooo.getJSONArray("narea"));
			Integer sex = jooo.getInt("sex");
			String sign = jooo.getString("sign");
			String ac = jooo.getString("ac");
			String desc = jooo.optString("desc", "");

			TX tx = new TX();
			tx.partner_id = id;
			tx.setNick_name(nickname);
			tx.setArea(area);
			tx.setSex(sex);
			tx.setAvatar_url(avatarurl);
			tx.setSign(sign);

			mSess.mMsgHandler.onServerSystem_34_10(tx, ltime, ac, desc);

		} catch (JSONException e) {
			if (Utils.debug)
				Log.e(TAG, "收到好友请求json异常", e);
		}
	}

	/**
	 * sns信息 SNS好友推送
	 * 
	 * @param jo
	 */
	private synchronized void dealSystemMsgs34_09(JSONObject jo) {
		try {
			Long ltime = jo.getLong("t");
			JSONObject jooo = jo.getJSONObject("obj");
			final Long id = jooo.getLong("i");
			String nickname = jooo.getString("n");
			String avatarurl = jooo.getString("avatarurl");
			// 这个handler对msg.what = DOWNLOADHEADER;什么也没有操作 2014.01.20
			// if (!Utils.isNull(avatarurl)) {
			// Message msg1 = new Message();
			// msg1.what = DOWNLOADHEADER;
			// msg1.getData().putLong("id", id);
			// msg1.getData().putString("url", avatarurl);
			// handler.sendMessage(msg1);
			// }
			String area = jooo.getString("area");
			Integer sex = jooo.getInt("sex");
			String sign = jooo.getString("sign");
			Integer sns = jooo.getInt("sns");
			final String snsid = jooo.getString("snsid");

			// if (TX.tm.isInBlack(id)) {
			// // 在黑名单中，还不确定有这个可能没有？
			// } else {
			// // 是陌生人
			//
			// ContentValues values = new ContentValues();
			// values.put(TxDB.Tx.DISPLAY_NAME, nickname);
			// values.put(TxDB.Tx.SECOND_CHAR,
			// CnToSpell.getFullSpell(nickname));
			// values.put(TxDB.Tx.HOME, area);
			// values.put(TxDB.Tx.SEX, sex);
			// values.put(TxDB.Tx.AVATAR_URL, avatarurl);
			// values.put(TxDB.Tx.TX_SIGN, sign);
			//
			// if (TX.tm.updateTx(id, values) == null) {
			// // 更新失败，添加tx
			// TX tx = new TX();
			// tx.partner_id = id;
			// tx.setNick_name(nickname);
			// tx.setArea(area);
			// tx.setSex(sex);
			// tx.setAvatar_url(avatarurl);
			// tx.setSign(sign);
			//
			// TX.tm.addTx(tx);
			// }
			// }

			TX tx = new TX();
			tx.partner_id = id;
			tx.setNick_name(nickname);
			tx.setArea(area);
			tx.setSex(sex);
			tx.setAvatar_url(avatarurl);
			tx.setSign(sign);

			mSess.mMsgHandler.onServerSystem_34_9(tx);

			boolean isNew = true;
			if ((ltime + "".length()) <= 10) {
				ltime = ltime * 1000;
			}
			final TXMessage tmsg = TXMessage.createSNSsms(
					mSess.getContext(),
					nickname,
					TX.TUIXIN_FRIEND,
					mSess.getResources().getString(R.string.tuixinfriend),
					mSess.getResources().getString(
							R.string.recommended_sina_friend)
							+ nickname, nickname, id, sign, sex, avatarurl,
					sns, snsid, false, 2, ltime);
			Cursor c = cr.query(TxDB.Messages.CONTENT_URI,
					new String[] { TxDB.Messages.MSG_ID },
					TxDB.Messages.TCARD_ID + "=? AND "
							+ TxDB.Messages.MSG_PARTNER_ID + " = "
							+ TX.TUIXIN_FRIEND, new String[] { ""
							+ tmsg.tcard_id }, null);
			if (c != null) {
				if (c.moveToFirst()) {
					tmsg.msg_id = c.getLong(0);
					isNew = false;
				}
				c.close();
			}
			final boolean isnew = isNew;
			// 取得微博昵称
			if (sns == 0) {

				// if (prefsMeme.getString(CommonData.WEIBO_USER_ID,
				// "").equals("")) {
				if (mSess.mPrefMeme.weibo_user_id.getVal().equals("")) {
					return;
				}
				Utils.executorService.submit(new Runnable() {
					@Override
					public void run() {
						Weibo weibo = Weibo.getInstance();
						// String token =
						// prefsMeme.getString(CommonData.WEIBO_TOKEN, "");
						// String tokenSecret =
						// prefsMeme.getString(CommonData.WEIBO_TOKEN_SECRET,
						// "");
						String token = mSess.mPrefMeme.weibo_token.getVal();
						String tokenSecret = mSess.mPrefMeme.weibo_token_secret
								.getVal();
						AccessToken accessToken = new AccessToken(token,
								tokenSecret);
						weibo.setAccessToken(accessToken);
						try {
							weibo.showUser(mSess.getContext(), weibo,
									Weibo.getAppKey(), snsid,
									new RequestListener() {

										@Override
										public void onIOException(IOException e) {
											tmsg.sns_name = mSess
													.getResources()
													.getString(
															R.string.recommended_sina_friend3);
											saveSns(tmsg, isnew);
										}

										@Override
										public void onError(
												com.weibo.net.WeiboException e) {
											tmsg.sns_name = mSess
													.getResources()
													.getString(
															R.string.recommended_sina_friend3);
											saveSns(tmsg, isnew);
										}

										@Override
										public void onComplete(String response) {
											try {
												JSONObject json = new JSONObject(
														response);
												tmsg.sns_name = json
														.getString("screenName");
											} catch (Exception e) {
												tmsg.sns_name = mSess
														.getResources()
														.getString(
																R.string.recommended_sina_friend3);
											}
											saveSns(tmsg, isnew);

										}
									});
						} catch (com.weibo.net.WeiboException e) {
							tmsg.sns_name = mSess.getResources().getString(
									R.string.recommended_sina_friend3);
							saveSns(tmsg, isnew);
						}
					}
				});

			}
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}

	}

	boolean temp = true;

	private void saveSns(TXMessage tmsg, boolean isnew) {
		if (!temp)
			return;
		temp = false;
		TXMessage.saveTXMessagetoDB(tmsg, mSess.getContentResolver(), true);
		Intent i = new Intent(FriendManagerActivity.RECEIVER_FOR_RECOMMENDINFO);
		i.putExtra("newMsg", tmsg);
		i.putExtra("isNew", isnew);
		mSess.getContext().sendBroadcast(i);
		if (isnew) {
			mSess.getSocketHelper().showNotification(tmsg, false);
			TXMessage.saveTXMessagetoDB(tmsg, mSess.getContentResolver(), true);
		} else {
			TXMessage.updateTcardTXMessage(cr, tmsg);
		}
	}

	/**
	 * 打招呼
	 * 
	 * @param jo
	 */
	private synchronized void dealSystemMsgs34_08(JSONObject jo) {

		try {
			Long ltime = jo.getLong("t");
			JSONObject jooo = jo.getJSONObject("obj");
			Long id = jooo.getLong("i");
			String nickname = jooo.getString("n");
			String avatarurl = jooo.getString("avatarurl");
			String area = jooo.getString("area");
			Integer sex = jooo.getInt("sex");
			String sign = jooo.getString("sign");
			String msg = jooo.getString("msg");
			String ac = jooo.optString("ac", "");

			TX tx = new TX();
			tx.partner_id = id;
			tx.setNick_name(nickname);
			tx.setArea(area);
			tx.setSex(sex);
			tx.setAvatar_url(avatarurl);
			tx.setSign(sign);

			mSess.mMsgHandler.onServerSystem_34_8(tx);

			boolean isNew = true;
			if ((ltime + "".length()) <= 10) {
				ltime = ltime * 1000;
			}

			TXMessage tmsg = TXMessage.creatGreetsms(mSess.getContext(),
					nickname, TX.TUIXIN_FRIEND, "好友管家", nickname + "向你打招呼",
					nickname, id, avatarurl, sign, sex, msg, false, 2, ltime);
			tmsg.ac = ac;
			Cursor c = cr.query(TxDB.Messages.CONTENT_URI,
					new String[] { TxDB.Messages.MSG_ID },
					TxDB.Messages.TCARD_ID + "=?  AND "
							+ TxDB.Messages.MSG_PARTNER_ID + " = "
							+ TX.TUIXIN_FRIEND, new String[] { ""
							+ tmsg.tcard_id }, null);
			if (c != null) {
				if (c.moveToFirst()) {
					tmsg.msg_id = c.getLong(0);
					isNew = false;
				}
				c.close();
			}
			if (isNew) {
				mSess.getSocketHelper().showNotification(tmsg, false);
				TXMessage.saveTXMessagetoDB(tmsg, mSess.getContentResolver(),
						true);
			} else {
				TXMessage.updateTcardTXMessage(cr, tmsg);
			}

			TXMessage.saveTXMessagetoDB(tmsg, mSess.getContentResolver(), true);
			if (id != mSess.getUserid()) {
				Intent i = new Intent(
						FriendManagerActivity.RECEIVER_FOR_RECOMMENDINFO);
				i.putExtra("newMsg", tmsg);
				i.putExtra("isNew", isNew);
				mSess.getContext().sendBroadcast(i);
			}

		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}

	}

	private void broadcastMsg(String key, String msg) {
		Intent intent = new Intent(Constants.INTENT_ACTION_MSGHELPER_MSG);
		intent.putExtra(key, msg);
		mSess.getContext().sendBroadcast(intent);
	}

	/**
	 * 发送广播消息
	 * 
	 * @param action
	 *            Intent's action
	 * @param serverRsp
	 *            Server响应数据
	 */
	private void broadcastMsg(String action, ServerRsp serverRsp) {
		Intent intent = new Intent(action);
		intent.putExtra(Constants.EXTRA_SERVER_RSP_KEY, serverRsp.getBundle());
		intent.setExtrasClassLoader(ServerRsp.class.getClassLoader());
		mSess.getContext().sendBroadcast(intent);
	}

	private String receiveSameCard(String carddown) {
		Cursor c = cr.query(TxDB.Messages.CONTENT_URI, null, Messages.MSG_URL
				+ "=?", new String[] { carddown }, null);
		while (c.moveToNext()) {
			TXMessage cm = TXMessage.fetchOneMsg(c);
			if (!Utils.isNull(cm.msg_path)) {
				c.close();
				return cm.msg_path;
			}
		}
		c.close();
		return null;
	}

	private String receiveSameAudio(String audiodown) {
		Cursor c = cr.query(TxDB.Messages.CONTENT_URI, null, Messages.MSG_URL
				+ "=?", new String[] { audiodown }, null);
		if (c == null) {
			return null;
		}
		while (c.moveToNext()) {
			TXMessage cm = TXMessage.fetchOneMsg(c);
			if (!Utils.isNull(cm.msg_path)) {
				c.close();
				return cm.msg_path;
			}
		}
		c.close();
		return null;
	}

	private String receiveSameImg(String imgdown) {
		Cursor c = cr.query(TxDB.Messages.CONTENT_URI, null, Messages.MSG_URL
				+ "=?", new String[] { imgdown }, null);
		while (c.moveToNext()) {
			TXMessage cm = TXMessage.fetchOneMsg(c);
			if (!Utils.isNull(cm.msg_path)) {
				c.close();
				return cm.msg_path;
			}
		}
		c.close();
		return null;
	}

	/** 处理服务器返回的上周之星 */
	private void dealServerLastWeekStars(JSONObject json) {
		try {
			int groupId = json.getInt("gid");// 群id
			int mt = json.getInt("mt");// 协议号
			int result = json.getInt("d");// 0:成功，1：失败，2：没有数据
			int ver = json.optInt("ver");// 活跃之星的版本号，服务器ver>0
			if (result == 1) {
				// 如果返回失败则不添加显示
				return;
			}
			ArrayList<Long> starsIdList = new ArrayList<Long>();// 这个是待添加到聊天室map的list
			if (result == 2) {
				// 活跃之星数据不需要更新，直接从本地sp中取

				String laskWeekStar = prefsMeme.getString(
						Constants.LAST_WEEK_STARS, "");
				JSONObject saveJsonObj = null;// 保存到本地SharedPreference的json
				if (!TextUtils.isEmpty(laskWeekStar)) {
					saveJsonObj = new JSONObject(laskWeekStar);// 保存到本地SharedPreference的json

					JSONObject subJsonObj = saveJsonObj.optJSONObject(""
							+ groupId);// 一个group的json

					JSONArray starsArray = null;
					if (subJsonObj != null) {
						starsArray = subJsonObj.getJSONArray("stars");
					}
					ArrayList<TX> starsTxList = getStarsJsonToList(starsArray,
							false);
					if (starsTxList != null && starsTxList.size() > 0) {
						for (TX ttx : starsTxList) {
							starsIdList.add(ttx.partner_id);// 把id都添加到list中，再添加到Groupmsgmap
						}
					}
					if (starsIdList.size() == 0) {
						// 服务器返回的上周之星集合为空集，则不添加到聊天室map中
						return;
					}
				} else {
					return;// 本地没有，则不做操作
				}

			} else if (result == 0) {
				// 获取活跃之星成功
				int deadTime = json.getInt("time");// 客户端存此结果失效时间

				JSONObject subJsonObj = new JSONObject();// 一个group的json

				JSONArray starsJsonArray = json.getJSONArray("us");
				ArrayList<TX> starsList = getStarsJsonToList(starsJsonArray,
						true);

				if (starsList == null || starsList.size() == 0) {
					// 服务器返回的上周之星集合为空集，则不添加到聊天室map中
					return;
				}
				for (TX ttx : starsList) {
					starsIdList.add(ttx.partner_id);
				}

				subJsonObj.put("stars", starsJsonArray);
				subJsonObj.put("dt", deadTime);// 过期时间
				subJsonObj.put("ver", ver);// 活跃之星版本号
				String laskWeekStar = prefsMeme.getString(
						Constants.LAST_WEEK_STARS, "");
				JSONObject saveJsonObj = null;// 保存到本地SharedPreference的json
				if (TextUtils.isEmpty(laskWeekStar)) {
					saveJsonObj = new JSONObject();
				} else {
					saveJsonObj = new JSONObject(laskWeekStar);// 保存到本地SharedPreference的json
				}

				if (saveJsonObj.has("" + groupId)) {
					saveJsonObj.remove("" + groupId);
				}
				saveJsonObj.put("" + groupId, subJsonObj);
				editorMeme.putString(Constants.LAST_WEEK_STARS,
						saveJsonObj.toString()).commit();
				if (Utils.debug)
					Log.i(TAG, "保存的上周之星Json串为：" + saveJsonObj.toString());
			}

			// 添加到GroupMsgRoom中map，感觉这样处理不优雅，待修改 2013.10.24
			GroupMsgRoom.mGroupStarsMap.put((long) groupId, starsIdList);

			// 发送虚拟消息添加上周之星条目
			mSess.getSocketHelper().sendShamMsgToShowLaskWeekStars(groupId);

		} catch (JSONException e) {
			if (Utils.debug)
				Log.e(TAG, "解析处理上周之星异常", e);
		}

	}

	/** 把上周活跃之星的jsonArray数据转换为list */
	private ArrayList<TX> getStarsJsonToList(JSONArray starsJsonArray,
			boolean isFromServer) {
		if (starsJsonArray == null) {
			return null;
		}
		try {
			ArrayList<TX> txList = new ArrayList<TX>();// 这个是待添加到聊天室map的list
			for (int i = 0, size = starsJsonArray.length(); i < size; i++) {
				JSONObject starJson = starsJsonArray.getJSONObject(i);
				int partner_id = starJson.getInt("i");
				int sex = starJson.getInt("sx");
				String nickName = starJson.getString("n");
				int avatarVer = starJson.getInt("avatarver");
				String avatarUrl = starJson.getString("avatar");

				if (Utils.isIdValid(partner_id)) {

					TX starTx = new TX();
					starTx.setPartnerId(partner_id);
					starTx.setSex(sex);
					starTx.setNick_name(nickName);
					starTx.setAvatar_ver(avatarVer);
					starTx.setAvatar_url(avatarUrl);

					mSess.mMsgHandler.onServerGetGroupLastWeekStars_2060(
							isFromServer, starTx);

					txList.add(starTx);// 把tx都添加到list中
				}
			}
			return txList;

		} catch (Exception e) {
			if (Utils.debug)
				Log.e(TAG, "解析处理上周之星数组异常", e);
			return null;
		}

	}

	/**
	 * 获取上周之星版本号,比对是否更新
	 * 
	 * @throws JSONException
	 */
	private int getLaskWeekStarsVer(long groupId) throws JSONException {
		int ver = 0;
		String laskWeekStar = prefsMeme
				.getString(Constants.LAST_WEEK_STARS, "");
		if (TextUtils.isEmpty(laskWeekStar)) {
			return ver;
		}
		JSONObject saveJsonObj = new JSONObject(laskWeekStar);// 保存到本地SharedPreference的json

		JSONObject groupJsonObj = saveJsonObj.optJSONObject("" + groupId);

		if (groupJsonObj == null) {
			return ver;
		}
		return groupJsonObj.optInt("ver");

	}

	/** 处理群禁言列表 */
	private void dealGroupBanList(JSONObject json) {
		try {
			HashSet<Long> banList = new HashSet<Long>();// 禁言列表
			ServerRsp serverRsp = new ServerRsp();
			int result = json.getInt("d");// 0:成功，1：失败
			if (result == 0) {
				int groupId = json.getInt("gid");// 群id
				int mt = json.getInt("mt");// 协议号
				int currentPageNum = json.getInt("cp");// 禁言列表当前页码
				int endPageNum = json.getInt("ep");// 禁言列表最后一页页码

				JSONArray banJsonArray = json.getJSONArray("ban");
				for (int i = 0, size = banJsonArray.length(); i < size; i++) {
					banList.add((long) banJsonArray.getInt(i));
				}
				if (currentPageNum < endPageNum) {
					mSess.getSocketHelper().sendGetGroupBanList(groupId,
							++currentPageNum);
				} else {
					serverRsp.setStatusCode(StatusCode.RSP_OK);
					serverRsp.getBundle().putSerializable(
							GroupMsgRoom.GROUP_BAN_LIST, banList);
					broadcastMsg(Constants.INTENT_ACTION_GROUP_BAN_LIST,
							serverRsp);
				}

			} else if (result == 1) {
				// 如果返回失败则不添加显示
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
				broadcastMsg(Constants.INTENT_ACTION_GROUP_BAN_LIST, serverRsp);
			}

		} catch (JSONException e) {
			if (Utils.debug)
				Log.e(TAG, "解析聊天室禁言异常", e);
		}

	}

	/** 附加操作的接口 */
	public interface IExtraOpreater {
		void operate();
	}

}