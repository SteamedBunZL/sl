package com.shenliao.search.adapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

//import net.youmi.android.banner.AdSize;
//import net.youmi.android.banner.AdView;

import org.json.JSONArray;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shenliao.data.DataContainer;
import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.download.AvatarDownload;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 搜索结果listview适配器
 * 
 * @author a
 * 
 */
public class SearchResultListViewAapter extends BaseAdapter {
	private static final String TAG = SearchResultListViewAapter.class
			.getSimpleName();
	private Context context;
	private LayoutInflater inflater;
	private List<TX> mList = new ArrayList<TX>();
	private LinearLayout listView;
	private LinearLayout noData;
	private SmileyParser sParser;// 解析表情
	private int n = 0;
	private int lastposition = 0;
	public int currposition = 10;
	private int selectpositon = 0;
	// int defaltHeaderImg;
	// int defaltHeaderImgFemale;
	// int defaltHeaderImgMan;
	LinkedList<ViewHolder> mViewLines = new LinkedList<ViewHolder>();// 记录创建的viewholder
	JSONArray areaJsonArray = null;
	private SessionManager mSess;

	public SearchResultListViewAapter(Context context, LinearLayout noData,
			LinearLayout liner, SessionManager mSess) {
		// prepairAsyncload();
		this.context = context;
		this.mSess = mSess;
		inflater = LayoutInflater.from(context);
		this.listView = liner;
		this.noData = noData;
		sParser = Utils.getSmileyParser(context);

		try {
			areaJsonArray = DataContainer.getAreaJsonArray();
		} catch (Exception e) {
			if (Utils.debug) {
				Log.w(TAG, "封裝地区json文件为jsonArray异常：", e);
			}
		}
	}

	public void setData(List<TX> list) {
		if (list != null) {
			this.mList = list;
		}
		if (mList.size() == 0) {
			this.listView.setVisibility(View.GONE);
			if (noData != null) {
				this.noData.setVisibility(View.VISIBLE);
			}

		} else {
			this.listView.setVisibility(View.VISIBLE);
			if (noData != null) {
				this.noData.setVisibility(View.GONE);
			}
		}
	}

	// @Override
	// public int getViewTypeCount() {
	// return 2;
	// }

	// @Override
	// public int getItemViewType(int position) {
	// if (getCount() < 10 && position == getCount() - 1) {
	// return Constants.ADD_VIEW;
	// } else {
	// if ((position - 10) % 11 == 0) {
	// return Constants.ADD_VIEW;
	// } else {
	// return Constants.NORMAL_VIEW;
	// }
	// }
	//
	// }

	@Override
	public int getCount() {
		if (mList != null) {
			// if (mList.size()==10) {
			return mList.size();
			// }else {
			// return mList.size() < 10 ? (mList.size() + 1) :
			// mList.size()+((currposition-10)/11+1);
			// }
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

	// public int getNewPosition(int position) {
	// int n = (currposition - 10) / 11;
	// if (position - currposition < 0) {
	// return position - n;
	// } else {
	// return position - (n + 1);
	// }
	// }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		// if (getItemViewType(position) == Constants.ADD_VIEW) {
		// currposition = position;
		// if (convertView == null) {
		// AdView adView = new AdView(context, AdSize.FIT_SCREEN);
		// convertView = adView;
		// }
		// } else {
		if (convertView == null) {
			convertView = inflater.inflate(
					R.layout.sll_search_add_condition_result_listview_items,
					null);
			viewHolder = new ViewHolder();
			mViewLines.add(viewHolder);
			viewHolder.headImage = (ImageView) convertView
					.findViewById(R.id.search_add_result_listview__photo);
			viewHolder.nickName = (TextView) convertView
					.findViewById(R.id.search_add_result_listview_nickName);
			viewHolder.age = (TextView) convertView
					.findViewById(R.id.search_add_result_listview_text_age);
			viewHolder.sign = (TextView) convertView
					.findViewById(R.id.search_add_result_listview_text_sign);
			viewHolder.linear = (RelativeLayout) convertView
					.findViewById(R.id.search_listview_linear);
			viewHolder.mHasAlbum = (ImageView) convertView
					.findViewById(R.id.search_add_result_listview_hasalbum);
			viewHolder.mArea = (TextView) convertView
					.findViewById(R.id.search_add_result_listview_text_area);
			viewHolder.tv_level = (TextView) convertView
					.findViewById(R.id.tv_level);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if (Utils.debug) {
			Log.e("Zzl3", position + "");
		}
		final TX tx = mList.get(position);

		viewHolder.tx = tx;

		readHeadImg(viewHolder.headImage, tx.partner_id, tx.avatar_url, tx);

		if (!Utils.isNull(tx.getNick_name())) {
			viewHolder.nickName.setText(sParser.addSmileySpans(
					tx.getNick_name(), true, 0));
		} else {
			// viewHolder.nickName.setText(sParser.addSmileySpans(
			// tx.getContacts_person_name(), true, 0));
			viewHolder.nickName.setText(sParser.addSmileySpans(tx.getTxInfor()
					.getContacts_person_name(), true, 0));
		}

		if (tx.getSex() == TX.MALE_SEX) {
			viewHolder.linear
					.setBackgroundResource(R.drawable.user_infor_sex_boy);
		} else {
			viewHolder.linear
					.setBackgroundResource(R.drawable.user_infor_sex_girl);
		}
		viewHolder.age.setText(String.valueOf(tx.age));
		viewHolder.sign.setText(tx.sign);
		if (tx.isDispalyLevel()) {
			viewHolder.tv_level.setVisibility(View.VISIBLE);
			viewHolder.tv_level.setText(context.getString(R.string.level)
					+ tx.getLevel());
		} else {
			viewHolder.tv_level.setVisibility(View.INVISIBLE);
		}

		if (tx.haveAlbum) {
			viewHolder.mHasAlbum.setVisibility(View.VISIBLE);
		} else {
			viewHolder.mHasAlbum.setVisibility(View.GONE);
		}
		String areaStr = null;
		try {
			// JSONArray areaArray = new JSONArray();
			String[] areaArray = tx.getArea().split(",");
			if (areaArray.length > 1) {
				areaStr = DataContainer.parseAreaArray(areaJsonArray,
						Integer.parseInt(areaArray[0]),
						Integer.parseInt(areaArray[1]));// 这里可能数据越界
			}
		} catch (Exception e) {
			if (Utils.debug) {
				Log.w(TAG, "解析地区信息异常：", e);
			}
		}
		viewHolder.mArea.setText(areaStr != null ? areaStr : "");
		// }

		return convertView;
	}

	class ViewHolder {
		ImageView headImage;// 头像
		TextView nickName;// 昵称
		TextView age;// 年龄
		TextView sign;// 签名
		TextView mArea;// 地区
		TextView tv_level;// 等级
		ImageView mHasAlbum;// 是否有相册
		RelativeLayout linear;
		TX tx;
	}

	// 读取头像
	public void readHeadImg(ImageView headView1, long tx_partner_id,
			String tx_avatar, TX tx) {
		if (Utils.debug) {
			Log.i(TAG, "readHeadImg");
		}
		// Bitmap bm = null;
		if (headView1 != null) {
			if (!Utils.isIdValid(tx_partner_id))
				return;
			if (TX.TUIXIN_MAN == tx_partner_id) {
				headView1.setImageResource(R.drawable.tuixin_manage);
				headView1.setTag(tx_partner_id);
				return;
			}

			headView1.setTag(tx_partner_id);
			if (Utils.debug) {
				Log.i(TAG, "tx_partner_id=" + tx_partner_id);
			}

			if (mSess != null) {
				Bitmap avatar = mSess.avatarDownload.getAvatar(tx_avatar,
						tx_partner_id, headView1, avatarHandler);
				if (avatar != null) {
					headView1.setImageBitmap(avatar);
					return;
				}
			}

			if (tx.getSex() == TX.MALE_SEX) {
				headView1.setImageResource(R.drawable.user_infor_head_boy);
			} else {
				headView1.setImageResource(R.drawable.user_infor_head_girl);
			}
			if (!Utils.checkSDCard()) {// 无SD卡
				// headView1.setImageResource(R.drawable.no_sd_img);
				if (tx.getSex() == TX.MALE_SEX) {
					headView1.setImageResource(R.drawable.user_infor_head_boy);
				} else {
					headView1.setImageResource(R.drawable.user_infor_head_girl);
				}
				return;
			}
			if (Utils.isNull(tx_avatar)) {
				return;
			}

			// BaseActivity.loadHeadImg(tx_avatar, tx_partner_id, callback);

		}
	}

	// handler
	private Handler avatarHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.AVATAR_RESULT:
				Object[] result = (Object[]) msg.obj;
				for (ViewHolder holder : mViewLines) {
					if (holder.tx != null
							&& holder.tx.partner_id == (Long) result[1]
							&& holder.headImage != null) {
						holder.headImage.setImageBitmap((Bitmap) result[0]);
						break;
					}
				}
				break;

			}
			super.handleMessage(msg);
		}
	};

}
