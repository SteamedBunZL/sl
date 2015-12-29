package com.shenliao.search.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tuixin11sms.tx.R;

public class SearchConditionFriendGridAdapter extends BaseAdapter {
	
	private Context context;
	private LayoutInflater mInflater;
	private List<String> mList=new ArrayList<String>();
	public SearchConditionFriendGridAdapter(Context context){
		this.context=context;
		mInflater=LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		if(mList!=null&&mList.size()>0){
			return mList.size();
		}else{
			return 0;
		} 
		
	}
	public void setData(List<String> list){
      this.mList=list;		
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
		if(convertView==null){
			convertView=mInflater.inflate(R.layout.search_add_condition_gridview_items, null);
		}
		
		TextView textView=(TextView) convertView.findViewById(R.id.gridview_Text);
		if(mList.get(position).equals("男")){
            textView.setText("");
            textView.setBackgroundResource(R.drawable.ic_sex_male);
		}else if(mList.get(position).equals("女")){
			textView.setText(null);
			textView.setBackgroundResource(R.drawable.ic_sex_female);
		}else{
			textView.setText(mList.get(position));
		}
		
		return convertView;
	}

}
