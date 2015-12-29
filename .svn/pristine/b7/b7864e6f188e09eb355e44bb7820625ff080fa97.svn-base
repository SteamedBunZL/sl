package com.shenliao.set.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.model.Area;

public class SetUpdateAreaAdapter extends BaseAdapter {
	private List<Area> aList = new ArrayList<Area>();

	private Context context;
	private LayoutInflater inflater;

	public SetUpdateAreaAdapter(Context context) {
		inflater = LayoutInflater.from(context);

	}

	public void setData(List<Area> list) {
		if (list != null && list.size() > 0) {
			this.aList = list;
		}
	}

	@Override
	public int getCount() {
		if (aList != null && aList.size() > 0) {
			return aList.size()+1;
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
		if (convertView == null) {
			convertView = inflater.inflate(
					R.layout.sll_userinfo_area_listview_items, null);
		}
		if (position == 0) {
			// linear.setBackgroundResource(R.drawable.sll_more_item_select);
			View inflate = inflater.inflate(
					R.layout.sll_userinfo_area_listview_0item, null);
			convertView = inflate;
		} else if (position == aList.size()+1) {
//			linear.setBackgroundResource(R.drawable.sll_more_item_select);
//			View inflate = inflater.inflate(
//					R.layout.sll_userinfo_area_listview_0item, null);
//			convertView = inflate;
		} else {
			convertView = inflater.inflate(
					R.layout.sll_userinfo_area_listview_items, null);
			LinearLayout linear = (LinearLayout) convertView
					.findViewById(R.id.area_linear);
			TextView textView = (TextView) convertView.findViewById(R.id.area_text);
			ImageView imageView = (ImageView) convertView
					.findViewById(R.id.area_image);
			
			textView.setText(aList.get(position-1).getName());
			linear.setBackgroundResource(R.drawable.sll_more_item_select);
			if (aList.get(position-1).getAreaList().size() > 0) {
				imageView.setVisibility(View.VISIBLE);
			} else {
				imageView.setVisibility(View.GONE);
			}
		}

		return convertView;
	}

}
