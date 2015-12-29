package com.tuixin11sms.tx;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.tuixin11sms.tx.core.MsgInfor;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.net.SocketHelper;

public class GroupListManager {
	
	
	int pageSize = 10;
	int totalSize = 0;
	int totalPage = 0;
	int currentPage = 0;
	
	ArrayList<String> groupIds = new ArrayList<String>();
	ArrayList<TxGroup> groupList = new ArrayList<TxGroup>();
	
	
	private static GroupListManager groupListManager;
	private static SessionManager mSess;
	
	public static GroupListManager getInstance(){
		if(groupListManager == null){
			groupListManager = new GroupListManager();
			mSess = SessionManager.getInstance();
		}
		return groupListManager;
	}
	
	public void init(){
		totalSize = 0;
		totalPage = 0;
		currentPage = 0;
		synchronized (groupIds) {
			groupIds.clear();
		}
		groupList.clear();
	}
	
	
	/**
	 * 用于搜索群和我的群
	 * @param ids
	 */
	public void addGroupIds(ArrayList<String> ids){
		synchronized (groupIds) {
			groupIds.addAll(ids);
		}
		calculatePage();
	}
	/**
	 * 计算页数
	 */
	public void calculatePage(){
		totalSize = groupIds.size();
		int temp = totalSize % pageSize;
		if(temp == 0){
			totalPage = totalSize / pageSize;
		}else{
			totalPage = totalSize / pageSize + 1; 
		}
	}
	public List<TxGroup> getUserGroups4DB(Context txData){
		return TxGroup.getMyGroupsByUnreadCount(txData);
	}
	
	
	public void getUserGroups4Server(){
		for(int i=0; i< totalPage; i++){
			synchronized (groupIds) {
				if(i == totalPage - 1){
					mSess.getSocketHelper().sendGetMoreGroup(groupIds.subList(i * pageSize, totalSize),MsgInfor.SERVER_GET_USER_QUN);
				}else{
					mSess.getSocketHelper().sendGetMoreGroup(groupIds.subList(i * pageSize, (i + 1) * 10), MsgInfor.SERVER_GET_USER_QUN);
				}
			}
		}
	}
	/**
	 * 
	 * @param currentPage
	 * @return boolean  是否已经取完  true取完 false没有
	 */
	public boolean getSearchGroups4Server(int currentPage){
		if(currentPage >= totalPage){
			return true;
		}
		synchronized (groupIds) {
			
			if(currentPage == totalPage - 1){
				mSess.getSocketHelper().sendGetMoreGroup(groupIds.subList(currentPage * pageSize, totalSize),MsgInfor.SERVER_SEARCH_GROUP);
			}else{
				mSess.getSocketHelper().sendGetMoreGroup(groupIds.subList(currentPage * pageSize, (currentPage + 1) * 10), MsgInfor.SERVER_SEARCH_GROUP);
			}
		}
		return false;
	}
	
}
