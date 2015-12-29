package com.shenliao.set.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shenliao.data.DataContainer;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.model.Language;

public class SetUpdateLanguageAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater inflater;
	private List<Language> list = DataContainer.getLangList();

	private List<String> selectList = new ArrayList<String>();
	// 用来控制CheckBox的选中状况
	private static HashMap<Integer, Boolean> isSelected;

	public SetUpdateLanguageAdapter(Context context) {
		this.context = context;
		inflater = LayoutInflater.from(context);
		isSelected = new HashMap<Integer, Boolean>();
		// initDate();
	}

	// 初始化isSelected的数据
	private void initDate() {
		for (int i = 0; i < list.size(); i++) {
			getIsSelected().put(i, false);
		}
	}

	@Override
	public int getCount() {
		if (list != null && list.size() > 0) {
			return list.size()+1;
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		Holder viewHolder = null;
		if (convertView != null) {
			viewHolder = (Holder) convertView.getTag();
		} 
		if (position == 0) {
			// viewHolder.linear.setBackgroundResource(R.drawable.sll_more_item_select);
			View inflate = inflater.inflate(
					R.layout.sll_userinfo_area_listview_0item, null);
			convertView = inflate;
		} else if (position == list.size() +1) {
			// viewHolder.linear.setBackgroundResource(R.drawable.sll_more_item_select);
		} else {
			convertView = inflater.inflate(
					R.layout.sll_setting_update_language_listview_items, null);
			viewHolder = new Holder();
			viewHolder.imageView = (ImageView) convertView
					.findViewById(R.id.sl_tab_language_listview_select_image);
			viewHolder.linear = (RelativeLayout) convertView
					.findViewById(R.id.linear_language_list);
			viewHolder.textView = (TextView) convertView
					.findViewById(R.id.sl_tab_language_listview_name);
			convertView.setTag(viewHolder);
			viewHolder.linear
					.setBackgroundResource(R.drawable.sll_more_item_select);
			if (selectList.contains(String.valueOf(list.get(position-1).getId()))) {
				viewHolder.imageView
						.setBackgroundResource(R.drawable.right_ok_lan);
				viewHolder.textView.setTextColor(context.getResources()
						.getColor(R.color.select_lan));

			} else {
				viewHolder.textView.setTextColor(context.getResources()
						.getColor(R.color.searchtext_default_color));
				viewHolder.imageView.setBackgroundDrawable(null);
			}

			viewHolder.textView.setText(list.get(position-1).getName());
		}

		return convertView;
	}

	public void setSelectData(List<String> slist) {
		this.selectList = slist;
	}

	public void setData(List<Language> langList) {
		list = langList;
	}

	public static HashMap<Integer, Boolean> getIsSelected() {
		return isSelected;
	}

	public static void setIsSelected(HashMap<Integer, Boolean> isSelected) {
		SetUpdateLanguageAdapter.isSelected = isSelected;
	}

	public class Holder {
		public ImageView imageView;
		public TextView textView;
		public RelativeLayout linear;
	}
}
