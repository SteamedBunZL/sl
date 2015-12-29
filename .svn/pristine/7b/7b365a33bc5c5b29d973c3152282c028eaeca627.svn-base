package com.shenliao.set.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.shenliao.data.DataContainer;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 用户详细资料中兴趣爱好gridview适配器
 * 
 * @author xch
 * 
 */
public class UserFavouriteGridViewAdapter extends BaseAdapter {
	private static final String TAG = UserFavouriteGridViewAdapter.class
			.getSimpleName();
	private int[] colors = new int[] { R.drawable.sll_user_favourite_one,
			R.drawable.sll_user_favourite_two,
			R.drawable.sll_user_favourite_three };
	private int[] textColors;
	private List<String> list = new ArrayList<String>();
	// private Context con;
	private LayoutInflater inflater;

	public UserFavouriteGridViewAdapter(Context con, List<String> list) {
		// this.con = con;
		if (list != null) {
			this.list = list;
		} else {
			if (Utils.debug) {
				Log.e(TAG, "list==null,getView会报空指针");
			}
		}
		textColors = new int[] {
				con.getResources().getColor(R.color.user_fav_text_one),
				con.getResources().getColor(R.color.user_fav_text_two),
				con.getResources().getColor(R.color.user_fav_text_three) };
		inflater = LayoutInflater.from(con);
	}

	@Override
	public int getCount() {
		if (list != null) {
			return list.size();
		} else {
			return 0;
		}

	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(
					R.layout.sl_userinfo_favourite_gridview_items, null);
		}
		TextView textView = (TextView) convertView
				.findViewById(R.id.sl_userinfo_favourite_griview_textView);
		int colorNum = (position % 3 + position / 3) % 3;
		convertView.setBackgroundResource(colors[colorNum]);
		textView.setTextColor(textColors[colorNum]);
		textView.setText(DataContainer.getHobbyNameById(list.get(position)));

		return convertView;
	}

}
