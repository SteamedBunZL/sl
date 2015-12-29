package com.shenliao.group.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tuixin11sms.tx.utils.Utils;

public class DownLoadAD {

	 protected static final String TAG = "DownLoadAD";

	public  static  void getAdURL(boolean  checkTime,final Handler handler,final Context context){
		 if(!checkTime)return;
		 Utils.executorService.submit(new Runnable() {
				@Override
				public void run() {
					 HttpClient client = null;
				        if (client == null)
							client = new DefaultHttpClient();
						HttpGet request = new HttpGet("http://118.145.23.90:9080/siteOperation/adview.jsp");
						HttpResponse response;
						try {
							response = client.execute(request);
							StatusLine statusLine = response.getStatusLine();
							if (statusLine != null && statusLine.getStatusCode() == 200) {
								HttpEntity entity = response.getEntity();
								if (entity != null) {
									try {
										if (entity.getContentLength() > 0) {
											BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
											String s = null;
											StringBuilder sb = new StringBuilder();
											while ((s = br.readLine()) != null) {
												sb.append(s);
											}
											JSONObject jsonObject = new JSONObject(sb.toString());
											String x = jsonObject.get("adsiteurl").toString() + "___" + jsonObject.get("imgurl").toString() + "__" + jsonObject.getInt("imgflag");  
											//setStatus(x);  
											String local = downImg(jsonObject.get("imgurl").toString(),100000000,context);
											if(local != null){
												HashMap<String,String> map = new HashMap<String,String>();
												map.put("url", jsonObject.get("adsiteurl").toString());
												map.put("local", local);
												Message.obtain(handler, GroupUtils.SHOW_AD, map).sendToTarget();
											}
											
										}
									} catch (JSONException e) {
										if(Utils.debug)Log.e(TAG,"广告异常",e);
									} finally {
										if (entity != null) {
											entity.consumeContent();
										}
									}
								}
							}
						} catch (Exception e) {
						
						}
				}
		 });
		
	        
	 }  
	 
	 public static String downImg(String sinaUrl, int s,Context context) {
		 HttpClient client = null;
			HttpGet request = new HttpGet(sinaUrl);
			if (client == null)
				client = new DefaultHttpClient();
			try {
				HttpResponse response = client.execute(request);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine != null && statusLine.getStatusCode() == 200) {
					HttpEntity entity = response.getEntity();
					if (entity != null && entity.getContentLength() > 0) {
						File file = new File(Utils.getStoragePath(context) + "/" + Utils.AVATAR_PATH, "ad.jpg");
						byte[] buffer = new byte[1024];
						InputStream in = entity.getContent();
						OutputStream out = new FileOutputStream(file);
						int len = 0;
						while ((len = in.read(buffer)) > 0) {
							out.write(buffer, 0, len);
						}
						in.close();
						out.close();
						return file.length() > 0 ? file.getAbsolutePath() : null;
					}
				}
				return null;
			} catch (Exception e) {
				if(Utils.debug)e.printStackTrace();
				return null;
			}
		}
}
