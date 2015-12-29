package com.shenliao.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.model.Area;
import com.tuixin11sms.tx.model.Hobby;
import com.tuixin11sms.tx.model.Language;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 数据容器
 * 
 * @author 郭伟洲
 * 
 */
public class DataContainer {
	private static final String TAG = DataContainer.class.getSimpleName();
	private static Context context;
	private static List<Area> areaList = new ArrayList<Area>();
	private static List<Hobby> hobbyList = new ArrayList<Hobby>();
	private static List<Language> langList = new ArrayList<Language>();
//	private static List<TX> blackList = new ArrayList<TX>();//黑名单list,黑名单管理用的是这个集合，现在改成用TX中的黑名单集合 2014.01.22 shc
//	private static AtomicInteger blackListPageNum = new AtomicInteger(0);
	private static List<TX> searchUserList = new ArrayList<TX>();//搜索用户的结果list，例如看看谁在聊
	private static AtomicInteger searchUserPageNum = new AtomicInteger(0);
	private static final int ADD_SEARCH_USER = 100;
//	private static final int ADD_BLACK_USER = 101;
	public static TX searchCondition;

	/**
	 * 容器初始化
	 */
	public static void init(Context txdata) {
		if (DataContainer.context == null && txdata != null) {
			DataContainer.context = txdata;
			initData();
		}
	}

	//没有引用，注掉 2013.09.17 shc
//	/**
//	 * 获取黑名单List
//	 */
//	public static List<TX> getBlackList() {
//		return blackList;
//	}

	/**
	 * 获取地区List
	 * 
	 * @return
	 */
	public static List<Area> getAreaList() {
		return areaList;
	}

	/**
	 * 获取爱好List
	 * 
	 * @return
	 */
	public static List<Hobby> getHobbyList() {
		return hobbyList;
	}

	/**
	 * 获取语言List
	 * 
	 * @return
	 */
	public static List<Language> getLangList() {
		return langList;
	}

	/**
	 * 初始化数据
	 */
	private static void initData() {
		Utils.executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					areaList = ja2AreaList(new JSONArray(is2String(context.getAssets().open("area.json"))));
					ja2HobbyList(new JSONArray(is2String(context.getAssets().open("hobby.json"))));
					ja2LangList(new JSONArray(is2String(context.getAssets().open("lang.json"))));
				} catch (JSONException e) {
					if(Utils.debug)e.printStackTrace();
				} catch (IOException e) {
					if(Utils.debug)e.printStackTrace();
				}
			}
		});
//		SocketHelper.getSocketHelper(txData).sendGetBlackList();//获取黑名单列表  2013.09.17
	}

	/**
	 * Inputstream转成String
	 * 
	 * @param is
	 *            输入流
	 * @return
	 * @throws IOException
	 */
	private static String is2String(InputStream is) throws IOException {
		byte[] buffer = new byte[8192 * 10];
		StringBuilder sb = new StringBuilder();
		int len = 0;
		while ((len = is.read(buffer)) != -1) {
			sb.append(new String(buffer, 0, len));
		}
		return sb.toString();
	}

	/**
	 * JSON转Area对象
	 * 
	 * @param jo
	 * @return
	 * @throws JSONException
	 */
	private static Area jo2Area(JSONObject jo) throws JSONException {
		Area area = new Area();
		area.setId(jo.getInt("id"));
		area.setName(jo.getString("name"));
		if (jo.has("sublist")) {
			area.setAreaList(ja2AreaList(jo.getJSONArray("sublist")));
		}
		return area;
	}

	/**
	 * JSON转Hobby对象
	 * 
	 * @param jo
	 * @return
	 * @throws JSONException
	 */
	private static Hobby jo2Hobby(JSONObject jo) throws JSONException {
		Hobby hobby = new Hobby();
		hobby.setId(jo.getInt("id"));
		hobby.setName(jo.getString("name"));
		return hobby;
	}

	/**
	 * JSON转Language对象
	 * 
	 * @param jo
	 * @return
	 * @throws JSONException
	 */
	private static Language jo2Lang(JSONObject jo) throws JSONException {
		Language lang = new Language();
		lang.setId(jo.getInt("id"));
		lang.setName(jo.getString("name"));
		return lang;
	}

	/**
	 * JSONArray转Area List
	 * 
	 * @param ja
	 * @return
	 * @throws JSONException
	 */
	private static List<Area> ja2AreaList(JSONArray ja) throws JSONException {
		List<Area> areaList = new ArrayList<Area>();
		for (int i = 0; i < ja.length(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			areaList.add(jo2Area(jo));
		}
		return areaList;
	}

	private static List<Hobby> ja2HobbyList(JSONArray ja) throws JSONException {
		for (int i = 0; i < ja.length(); i++) {
			hobbyList.add(jo2Hobby(ja.getJSONObject(i)));
		}
		return hobbyList;
	}

	private static List<Language> ja2LangList(JSONArray ja) throws JSONException {
		for (int i = 0; i < ja.length(); i++) {
			langList.add(jo2Lang(ja.getJSONObject(i)));
		}
		return langList;
	}

	public static String getAreaNameByIds(String[] ids) {
		Area area = null;
		StringBuilder sb = new StringBuilder();
		try {
			for (int i = 0; i < ids.length; i++) {
				if (i == 0) {
					area = findAreaById(areaList, Integer.parseInt(ids[i]));
				} else {
					if (area != null)
						area = findAreaById(area.getAreaList(), Integer.parseInt(ids[i]));
				}
				if (area != null) {
					sb.append(area.getName());
					if (i != ids.length - 1)
						sb.append(",");
				}
			}
		} catch (NumberFormatException e) {
			if(Utils.debug)Log.i(TAG, "数字格式化异常",e);
		}
		return sb.toString();
	}

	//没有引用，注掉 2013.09.17 shc
//	public static void setBlackList(List<TX> blackList) {
//		if (blackList != null)
//			DataContainer.blackList = blackList;
//	}

	private static String getLangNameById(String id) {
		if (Integer.parseInt(id) == Language.UNLIMIT_ID)
			return "不限";
		for (Language lang : langList) {
			if (lang.getId() == Integer.parseInt(id))
				return lang.getName().replace("中文-", "");
		}
		return "";
	}

	private static Area findAreaById(List<Area> list, int id) {
		if (id == Area.UNLIMIT_ID)
			return Area.createUnlimitArea();
		for (Area area : list) {
			if (area.getId() == id) {
				return area;
			}
		}
		return null;
	}

	/**
	 * 通过爱好id获取爱好名称
	 * 
	 * @param id
	 * @return
	 */
	public static String getHobbyNameById(String id) {
		
		
		if(Integer.parseInt(id) == 0){
			return "";
		}
		try {
			for (Hobby hobby : hobbyList) {
				if (hobby.getId() == Integer.parseInt(id))
					return hobby.getName();
			}
		} catch (NumberFormatException e) {

		}
		return id;
	}

	/**
	 * 通过语言id集合获取语言名称
	 * 
	 * @param ids
	 *            语言id集合
	 * @return
	 */
	public static String getLangNameByIds(String[] ids) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ids.length; i++) {
			String name = getLangNameById(ids[i]);
			if (!TextUtils.isEmpty(name)) {
				sb.append(name);
				if (i != ids.length - 1)
					sb.append(",");
			}
		}
		return sb.toString();
	}

	/**
	 * 通过爱好id集合获取爱好名称
	 * 
	 * @param ids
	 * @return
	 */
	public static String getHobbyNameByIds(String[] ids) {
		StringBuilder sb = new StringBuilder();
		try {
			for (int i = 0; i < ids.length; i++) {
				String name = getHobbyNameById(ids[i]);
				if (!TextUtils.isEmpty(name)) {
					sb.append(name);
					if (i != ids.length - 1)
						sb.append(",");
				}
			}
		} catch (NumberFormatException e) {

		}
		return sb.toString();
	}

	//用TX管理黑名单吧，2014.01.21 shc
//	public static int getBlackListPageNum() {
//		return blackListPageNum.getAndIncrement();
//	}
//
//	public static void resetBlackListPageNum() {
//		blackListPageNum.set(0);
//	}

	public static int getSearchUserPageNum() {
		return searchUserPageNum.getAndIncrement();
	}

	public static void resetSearchUserPageNum() {
		searchUserPageNum.set(0);
	}

	public synchronized static void addSearchUser(List<TX> txList) {
		if (txList != null) {
			handler.obtainMessage(ADD_SEARCH_USER, txList).sendToTarget();
		}
	}

	//用TX管理黑名单吧，2014.01.21 shc
//	public synchronized static void addBlackUser(List<TX> txList) {
//		if (txList != null) {
//			if (Utils.debug) {
//				Log.i(TAG, "黑名单列表addBlackUser()为："+txList.toString());
//			}
//			handler.obtainMessage(ADD_BLACK_USER, txList).sendToTarget();
//		}
//	}

	//用TX管理黑名单吧，2014.01.21 shc
//	public static void removeBlackUser(TX tx) {
//		blackList.remove(tx);
//	}

	public static List<TX> getSearchList() {
		return new ArrayList<TX>(searchUserList);
	}

	/** 清除搜索到的用户 */
	public static void clearSearchUserList() {
		searchUserList.clear();
		resetSearchUserPageNum();
	}

	//用TX管理黑名单吧，2014.01.21 shc
//	/** 清除黑名单列表 */
//	public static void clearBlackList() {
//		blackList.clear();
//		resetBlackListPageNum();
//	}

	private static Handler handler = new Handler(Looper.getMainLooper()) {

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ADD_SEARCH_USER:
				searchUserList.addAll((List<TX>) msg.obj);
				break;
//			case ADD_BLACK_USER:
//				blackList.addAll((List<TX>) msg.obj);
//				if (Utils.debug) {
//					Log.e(TAG, "添加黑名单list");
//				}
				
			}
			
//			String s = "xxxx-yyyy-zzz-ddd-rrrr";
//			String[] split = s.split("-");

		}

	};
	
	/**
	 * 加载本地area.json文件的地区信息数组
	 * @param countryId
	 * @param provinceId
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONArray getAreaJsonArray() throws IOException, JSONException{
		
//		File file = new File("area.json");
//		FileInputStream fis = new FileInputStream(file);//这里改为assets的输入流
		InputStream is = context.getAssets().open("area.json");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024*8];
//		StringBuilder sb = new StringBuilder();
		int i = 0;
		while((i = is.read(buffer))!=-1){
			baos.write(buffer,0,i);
		}
		String jsonStr = baos.toString("utf-8");
		is.close();
		baos.close();
		if(Utils.debug){
			Log.i(TAG, "所有城区的json串："+jsonStr);
		}
		JSONArray areaJsonArray = new JSONArray(jsonStr);
//		String aimStr = parseJsonArray(areaJsonArray, countryId, provinceId);
		
		return areaJsonArray;
		
	}
	
	
	/**
	 * 取得“看看谁在线”等搜索结果条目上显示的区域名字
	 * @param jsonArrayObj
	 * @param countryId
	 * @param provinceId
	 * @return
	 * @throws JSONException
	 */
	public static String parseAreaArray(JSONArray jsonArrayObj,int countryId,int provinceId) throws JSONException{
		JSONObject countryJsonObj = null;
		for (int j = 0,k = jsonArrayObj.length(); j < k; j++) {
			countryJsonObj = jsonArrayObj.getJSONObject(j);
			if(countryId ==countryJsonObj.getInt("id")){
				//找到的对应的国家
				if (countryJsonObj.has("sublist")&&provinceId!=-1) {
					//有二级省字段
					return parseAreaArray(countryJsonObj.getJSONArray("sublist"), provinceId, -1);
				}else{
					//只有一级国家字段
					return countryJsonObj.getString("name");
				}
			}
		}
		return null;
		
	}


}
