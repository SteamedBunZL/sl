package com.shenliao.group.util;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.tuixin11sms.tx.message.TXMessage;

public class GroupHolder {
	
	public TextView groupName;
	public TextView memberNum;
	public TextView adminNames;
	public TextView admin;
	public TextView introl; // 简介
	public TextView creator; // 群主
	public TextView unReadCount; //未读数
	public ImageView avatar; // 头像
	public  TextView Gimage;
	public ImageView Limage;
	public ImageView Simage;
	public ImageView Pbimage;
	
	//群组动态
	
	public ImageView headimg;
	public TextView displayName;
	public TextView message;
	public TextView unreadmessagenum;
	public TextView messagetotalnum;
	public ImageView tuixinlogo;
//	public TextView messageusername;
	public TextView time;
	public CheckBox cBox;
	public ImageView addcontact_photo;
	public TextView message_state;
	public ImageView send_state;
	public ImageView message_marked;
	public Button agreeBtn;
	public Button refuseBtn;
	public TextView resultState;
	
	//神聊小卫士
	public TextView uid;
//	public LinearLayout	ll_time_layout;//时间条目的布局
	public TextView handlerContent;
	public View report;//举报消息
	public View handler; //处理信息
	public View reportContont;
	public TextView reportName;
	public TextView reportGroup;
	
	public ImageView failImg;
	public TextView v1_text;
	public View v2_img;
	public View v3_audio;
	public View v4_vcard;
	public View v5_geo;
	public View v6_bigFile;
	public TXMessage txmsg;
	
	//3.9
	public TextView manager;  //管理
	public TextView managername;  //管理名字
}
