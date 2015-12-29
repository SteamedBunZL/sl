package com.shenliao.set.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.shenliao.set.activity.SetUpdateFavouriteActivity;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.model.Hobby;

public class UserFavouriteSelectAdapter extends BaseAdapter {

	private List<Hobby> list = new ArrayList<Hobby>();
	private List<String> selectList=new ArrayList<String>();
	private Context con;
	private LayoutInflater inflater;
	private Map<String,View> viewMap=new HashMap<String, View>();

	public UserFavouriteSelectAdapter(Context con) {
		this.con = con;
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
			convertView = inflater.inflate(R.layout.sll_set_userinfo_favourite_select_gridview_items, null);
		}
		TextView textView = (TextView) convertView.findViewById(R.id.sl_tab_userinfo_favourite_griview_textView);
		textView.setText(list.get(position).getName());
		if(selectList.contains(String.valueOf(list.get(position).getId()))){
			convertView.setBackgroundResource(R.drawable.sll_favourite_textbackgroud_press);
			SetUpdateFavouriteActivity.viewMap.put(String.valueOf(list.get(position).getId()), convertView);
			
		}else{
			convertView.setBackgroundResource(R.drawable.sll_favourite_textbackgroud_selector);
		}
		return convertView;
	}
	public void setList(List<Hobby> list) {
		this.list = list;
	}
	public void setSelectList(List<String> list){
		this.selectList=list;
		
	}
	public void setMap(Map<String,View> map){
		this.viewMap=map;
	}
}
