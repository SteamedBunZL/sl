package com.shenliao.group.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.tuixin11sms.tx.utils.Utils;

public class ADUtils extends AsyncTask<String, Void, Object> {
	public static final String TAG = "ADUtils";
	public static final String HOST_URL = "http://118.145.23.90:9080/siteOperation";// 广告请求基地址
	public static final String VERSION = "1.0.0";// 广告请求协议版本号
	public static final String PLATFORM_ID = "1";// 广告请求平台id
	public static final int DOWNLOAD_AD_INFOR = 1;// 下载广告信息
	public static final int DOWNLOAD_AD_IMG = 2;// 下载广告需要展示的图片
	public static final int REQUEST_AD_NO_DISPLAY = 3;// 不再播放该条广告
	public static final int FORWARD_TO_INNER = 1;// 点击广告图片跳转到内部展示页面  //暂时没这个需求，2014.01.02
	public static final int FORWARD_TO_BROWSER = 2;// 点击广告图片跳转到外部浏览器
	private int requestType = DOWNLOAD_AD_INFOR;// 默认请求类型为下载广告信息
	
	public static String SAVE_TIME_MILLIS = "current_time_millis";//广告信息在sp中存放的当前时间毫秒值
	public static String AD_INFOR = "ad_infor";//广告信息在sp中存放的key
	public static String AD_ID = "ads_id";//广告信息json中信息id
	public static String AD_IMG_URL = "img_url";//广告信息json中图片下载地址
	public static String AD_FORWARD_TYPE = "forward_type";//广告信息json中点击广告view的跳转类型
	public static String AD_FORWARD_URL = "forward_url";//广告信息json中点击广告view的跳转url
	public static String AD_TIMEOUT = "timeout";//广告信息json中广告展示的过期时间
	public static String AD_FOLDER_NAME = "/download";//广告下载资源目录
	public Context context;

	public ADUtils(Context context,int requestType) {
		this.requestType = requestType;
		this.context = context;
	}

	@Override
	protected Object doInBackground(String... params) {
		if (params.length < 1) {
			// 如果没有传递参数，则直接return
			return null;
		}
		HttpClient client = new DefaultHttpClient();
		if (requestType == DOWNLOAD_AD_INFOR) {
			// 下载广告Url信息
			HttpGet request = new HttpGet(HOST_URL+"/client/adsplay.do?ver="+VERSION+"&uid="
					+ params[0] + "&platid="+PLATFORM_ID);
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
								String resultInfor = sb.toString();
								if (!TextUtils.isEmpty(resultInfor)) {
									// 返回的信息不为空
									JSONObject jsonObject = new JSONObject(sb.toString());
									if (jsonObject.getString("status").equals("ok")) {
										if (jsonObject.optJSONObject("result")!=null) {
											return jsonObject.getJSONObject("result").getJSONObject(
													"ads_info");
										}else {
											return null;
										}
									}
								}

							}
						} catch (JSONException e) {
							if (Utils.debug)
								Log.e(TAG,"请求广告后解析异常",e);
						} finally {
							if (entity != null) {
								entity.consumeContent();
							}
						}
					}
				}
			} catch (Exception e) {
				if (Utils.debug) {
					Log.e(TAG, "请求广告信息异常", e);
				}
			}

		} else if (requestType == DOWNLOAD_AD_IMG) {
			// 下载广告图片
			HttpGet request = new HttpGet(params[0]);
			HttpResponse response;
			try {
				response = client.execute(request);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine != null && statusLine.getStatusCode() == 200) {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						FileOutputStream fos = null;
						File file = null;
						try {
							if (entity.getContentLength() > 0) {
								InputStream is = entity.getContent();
								
								String folderPath = Utils.getStoragePath(context)+AD_FOLDER_NAME;
								file = new File(folderPath);
								if (!file.exists()) {
									file.mkdirs();
								}
								file = new File(file,params[1]);
								if (file!=null&&file.exists()) {
									file.delete();
								}
								byte[] buffer = new byte[2048];
								fos = new FileOutputStream(file);
								int length = 0;
								while ((length = is.read(buffer))!=-1) {
									fos.write(buffer, 0, length);
								}
								fos.close();
								
								return Utils.fitSizeImg(file.getAbsolutePath());
							}
						} catch (OutOfMemoryError err) {
							if (Utils.debug) {
								Log.e(TAG, "请求广告图片OOM异常", err);
							}
							return Utils.fitSizeImg(file.getAbsolutePath(),3);
						} finally {
							if (entity != null) {
								entity.consumeContent();
							}
						}
					}
				}
			} catch (Exception e) {
				if (Utils.debug) {
					Log.e(TAG, "请求广告图片异常", e);
				}
			
			}
		
		}else if (requestType == REQUEST_AD_NO_DISPLAY) {
			// 请求不显示该条广告，params第一个参数是uid,第二个参数是
			HttpGet request = new HttpGet(HOST_URL+"/client/adsnoplay.do?ver="+VERSION+"&uid="
					+ params[0] + "&platid="+PLATFORM_ID+"&ads_id="+params[1]);
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
								String resultInfor = sb.toString();
								if (!TextUtils.isEmpty(resultInfor)) {
									// 返回的信息不为空
									JSONObject jsonObject = new JSONObject(sb.toString());
									if (jsonObject.getString("status").equals("ok")) {
										return "ok";
									}else {
										return "fail";//失败
									}
								}

							}
						} catch (JSONException e) {
							if (Utils.debug)
								Log.e(TAG,"请求不显示广告后解析异常",e);
						} finally {
							if (entity != null) {
								entity.consumeContent();
							}
						}
					}
				}
			} catch (Exception e) {
				if (Utils.debug) {
					Log.e(TAG, "请求不显示广告信息异常", e);
				}
			}

		
			
		}

		return null;
	}
	
	

}
