package com.shenliao.set.adapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shenliao.group.util.GroupUtils;
import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.contact.TxInfor;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.download.AvatarDownload;
import com.tuixin11sms.tx.utils.Utils;

public class SetBlackManageAdapter extends BaseAdapter {
//	private Context context;
//	private TxData txData;
	private LayoutInflater inflater;
	private List<TX> tList = new ArrayList<TX>();
//	private ListView listView;
	private SmileyParser sysParser;
	private SessionManager mSess;

	public SetBlackManageAdapter(SessionManager mSess) {
		// prepairAsyncload();
//		this.context = context;
//		this.txData = txData;
		this.mSess = mSess;
		inflater = LayoutInflater.from(mSess.getContext());
//		this.listView = listView;
		sysParser = Utils.getSmileyParser(mSess.getContext());
	}

	@Override
	public int getCount() {
		if (tList != null) {
			return tList.size();
		} else {
			return 0;
		}
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
		ViewHolder viewHolder = null;

		if (convertView == null) {
			convertView = inflater.inflate(
					R.layout.sll__setting_update_blacklistview_items, null);
			viewHolder = new ViewHolder();
			mViewLines.add(viewHolder);
			viewHolder.headIamge = (ImageView) convertView
					.findViewById(R.id.sl_tab_setting_black_listview_photo);
			viewHolder.nickName = (TextView) convertView
					.findViewById(R.id.sl_tab_setting_black_listview_nickName);
			viewHolder.blackTime = (TextView) convertView
					.findViewById(R.id.sl_tab_setting_black_listview_blackTime);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final TX tx = tList.get(position);
		viewHolder.tx = tx;

		readHeadImg(viewHolder.headIamge, tx.partner_id, tx.avatar_url, tx.getSex());
//		TX ttx = TX.tm.getTx(tx.partner_id);
//		if (tx != null && ttx != null && ttx.getRemarkName() != null) {
//			tx.setRemarkName(ttx.getRemarkName());
//		}
		if (tx != null && tx.getTxInfor().getRemarkName()!= null) {
			tx.setRemarkName(tx.getTxInfor().getRemarkName());
		}

		if (!Utils.isNull(tx.getRemarkName())) {
			viewHolder.nickName.setText(sysParser.addSmileySpans(
					tx.getRemarkName(), true, 0));
		} else {
			if (!Utils.isNull(tx.getNick_name())) {
				viewHolder.nickName.setText(sysParser.addSmileySpans(
						tx.getNick_name(), true, 0));
			} else {
				// viewHolder.nickName.setText(sysParser.addSmileySpans(
				// tx.getContacts_person_name(), true, 0));
				viewHolder.nickName.setText(sysParser.addSmileySpans(tx
						.getTxInfor().getContacts_person_name(), true, 0));
			}
		}

		viewHolder.blackTime.setText("");
		TxInfor tinfor = null;
		if ((tinfor = tx.getTxInfor()) != null) {
			if (tinfor.getInBlackTime() > 0) {
				viewHolder.blackTime.setText(GroupUtils.formatTime(tinfor
						.getInBlackTime()) + "加入黑名单");
			}
		}
		// if (TX.tm.getBlackTX(tx.partner_id).getInBlackTime() > 0) {
		// viewHolder.blackTime.setText(GroupUtils.formatTime(TX.tm.getBlackTX(tx.partner_id).getInBlackTime())
		// + "加入黑名单");
		// } else {
		// viewHolder.blackTime.setText("");
		// }
		return convertView;
	}

	public void setData(List<TX> tx) {
		this.tList = tx;
	}

	class ViewHolder {
		ImageView headIamge;
		TextView nickName;
		TextView blackTime;
		TX tx;
	}

	// private HashMap<Long, SoftReference<Drawable>> mHeadDrCache = new
	// HashMap<Long, SoftReference<Drawable>>();
	//
	// Drawable getCachedDr(long partnerId) {
	// Drawable dr = null;
	// SoftReference<Drawable> reference = mHeadDrCache.get(partnerId);
	// if (reference != null)
	// dr = reference.get();
	// return dr;
	// }
	//
	// void cacheDr(long partnerId, Drawable dr) {
	// mHeadDrCache.put(partnerId, new SoftReference<Drawable>(dr));
	// }

	// 读取头像
	public void readHeadImg(ImageView headView1, long tx_partner_id,
			String tx_avatar, int sex) {
		Bitmap bm = null;
		if (headView1 != null) {
			if (!Utils.isIdValid(tx_partner_id))
				return;
			if (TX.TUIXIN_MAN == tx_partner_id) {
				headView1.setImageResource(R.drawable.tuixin_manage);
				headView1.setTag(tx_partner_id);
				return;
			}
			if (!Utils.checkSDCard()) {// 无SD卡
				if (sex == TX.MALE_SEX) {
					headView1.setImageResource(R.drawable.user_infor_head_boy);
				} else {
					headView1.setImageResource(R.drawable.user_infor_head_girl);
				}
				return;
			}

			headView1.setTag(tx_partner_id);
			bm = mSess.avatarDownload.getAvatar(tx_avatar,
					tx_partner_id, headView1, avatarHandler);
			if (bm != null) {
				headView1.setImageBitmap(bm);
			} else {
				if (sex == TX.MALE_SEX) {
					headView1.setImageResource(R.drawable.user_infor_head_boy);
				} else {
					headView1.setImageResource(R.drawable.user_infor_head_girl);
				}

				if (Utils.isNull(tx_avatar)) {
					return;
				}
			}

		}
	}

	LinkedList<ViewHolder> mViewLines = new LinkedList<ViewHolder>();

	private Handler avatarHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.AVATAR_RESULT:
				Object[] result = (Object[]) msg.obj;

				if (result == null) {
					return;
				}
				for (ViewHolder holder : mViewLines) {
					if (holder.tx != null
							&& holder.tx.partner_id == (Long) result[1]
							&& holder.headIamge != null)
						holder.headIamge.setImageBitmap((Bitmap) result[0]);
				}

				break;

			}
			super.handleMessage(msg);
		}
	};
}
