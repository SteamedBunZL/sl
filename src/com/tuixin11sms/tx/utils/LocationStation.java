package com.tuixin11sms.tx.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.tuixin11sms.tx.TxData;

public class LocationStation {
	private static final String TAG = "LocationStation";
	TelephonyManager manager;
	int mcc, mnc, lac[], ci[];
	String mac;
	int count;
	PhoneStateListener listener;
	private static final int CHECK_INTERVAL = 1000 * 30;
	Context txdata;
	// 变量定义
	public static LocationManager locationManager;
	public static LocationListener gpsListener = null;
	public static LocationListener networkListner = null;
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = null;
	// private Context context;
	private static LocationStation instance;
	private Timer outtime;
	private Timer gpsTimeOut;// GPS定位超时
	Location currentLocation;
	String result_location;
	double la, lo;
	double accuracy;
	private String moblicKey = "1CR2lCDKg22FIOkSRwDQMAnF";
	private String severKey = "A23e78da413b1819a422d1fb4c361cf4";

	public static LocationStation getInstance(Context myTxdata) {
		if (instance == null) {
			instance = new LocationStation();
		}
		instance.txdata = myTxdata;
		return instance;
	}

	private LocationStation() {
	}

	public LocationStation(Context myTxdata) {
		txdata = myTxdata;
	}

	public void timeOut(long outTime, final Context ct) {
		if (outtime != null) {
			try {
				outtime.cancel();
			} catch (Exception e) {

			}
			outtime = null;
		}
		outtime = new Timer();
		outtime.schedule(new TimerTask() {
			public void run() {

				broadcastGetLocFailed(Constants.INTENT_ACTION_GET_LOCATION_FAILED);
				Utils.saveLocationData(ct, 0, 0);
				// broadcastGetLocFailed(Constants.LOAD_LBS_INTENT_ACTION_NETWORK_LOCATION_FAILED);
			}
		}, outTime);
	}

	public void timeCancel() {
		if (outtime != null) {
			try {
				outtime.cancel();
			} catch (Exception e) {

			}
			outtime = null;
		}
	}

	public static void gpsCancel() {
		if (gpsListener != null) {
			locationManager.removeUpdates(gpsListener);
			gpsListener = null;
		}
		if (networkListner != null) {
			locationManager.removeUpdates(networkListner);
			networkListner = null;
		}
		if (locationManager != null) {
			locationManager = null;
		}
		if (Utils.debug)
			Log.v(TAG, "gpsCancel");
	}

	public void initGpsTimeOut(long outTime) {
		gpsTimeOut = new Timer();
		gpsTimeOut.schedule(new TimerTask() {
			public void run() {
				gpsCancel();
			}
		}, outTime);
	}

	Handler locationHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case 0:
				registerLocationListener();
				break;
			case 1:
				if (gpsListener != null) {
					locationManager.removeUpdates(gpsListener);
					gpsListener = null;
				}
				if (networkListner != null) {
					locationManager.removeUpdates(networkListner);
					networkListner = null;
				}
				break;
			}
		}

	};

	public void getLocation(Context ct, long outTime) {
		locationManager = (LocationManager) txdata
				.getSystemService(Context.LOCATION_SERVICE);
		if (!Utils.isReLocation()) {
			Location location = new Location("public");
			location.setLatitude(TxData.public_latitude);
			location.setLongitude(TxData.public_longitude);
			showLocation(location);
			return;
		}
		manager = (TelephonyManager) txdata
				.getSystemService(Context.TELEPHONY_SERVICE);
		listener = new CellStateListener();
		ci = new int[10];
		lac = new int[10];
		
		myListener = new MyLocationListener();
		mLocationClient = new LocationClient(ct); // 声明LocationClient类
		
		locationHandler.sendEmptyMessage(0);
		timeOut(outTime, ct);
		if (Utils.getNetworkType(txdata) != Utils.NET_NOT_AVAILABLE) {
			update(ct);
		} else {
			broadcastGetLocFailed(Constants.LBS_INTENT_ACTION_NETWORK_LOCATION_FAILED);
		}

	}

	private class MyLocationListner implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			// Called when a new location is found by the location provider.
			if (Utils.debug)
				Log.v(TAG,
						"Got New Location of provider:"
								+ location.getProvider());
			if (currentLocation != null) {
				if (isBetterLocation(location, currentLocation)) {
					if (Utils.debug)
						Log.v(TAG, "It's a better location");
					currentLocation = location;
					showLocation(location);

				} else {
					if (Utils.debug)
						Log.v(TAG, "Not very good!");
				}
			} else {
				if (Utils.debug)
					Log.v(TAG, "It's first location");
				currentLocation = location;
				showLocation(location);

			}
			/*
			 * // 移除基于LocationManager.NETWORK_PROVIDER的监听器 if
			 * (LocationManager.NETWORK_PROVIDER.equals(location.getProvider()))
			 * { locationManager.removeUpdates(this); }
			 */

			// locationManager.removeUpdates(this);
		}

		// 后3个方法此处不做处理
		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (Utils.debug)
				Log.i(TAG, "onStatusChanged-provider:" + provider);
		}
		public void onProviderEnabled(String provider) {
			if (Utils.debug)
				Log.i(TAG, "onProviderEnabled-provider:" + provider);

		}

		public void onProviderDisabled(String provider) {
			if (Utils.debug)
				Log.i(TAG, "onProviderDisabled-provider:" + provider);

		}
	};

	private void showLocation(Location location) {
		if (location != null) {
			// 纬度
			if (Utils.debug)
				Log.i(TAG, "Latitude:" + location.getLatitude());
			// 经度
			if (Utils.debug)
				Log.i(TAG, "Longitude:" + location.getLongitude());
			// 精确度
			if (Utils.debug)
				Log.i(TAG, "Accuracy:" + location.getAccuracy());
			if (location.getLatitude() != 0 && location.getLongitude() != 0) {
				TxData.public_latitude = location.getLatitude();
				TxData.public_longitude = location.getLongitude();
				Utils.saveLocationData(txdata, TxData.public_latitude,
						TxData.public_longitude);
			}
		} else {

		}
		timeCancel();
		broadcast(location);
	}

	private void registerLocationListener() {
		if (networkListner == null && locationManager != null) {
			if (Utils.debug)
				Log.v(TAG, "registerLocationListener");
			try {
				networkListner = new MyLocationListner();
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 0, 0, networkListner);
				gpsListener = new MyLocationListner();
				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 0, 0, gpsListener);
				initGpsTimeOut(120 * 1000);
			} catch (Exception e) {
				//requestLocationUpdates()可能会异常 java.lang.IllegalArgumentException: provider=network
				if(Utils.debug)Log.e(TAG,"注册定位listener异常",e);
			}
		}

	}

	/**
	 * 设置百度定位参数
	 * 
	 * @param coorType
	 */
	public void setOption(String coorType) {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(false);
		option.setAddrType("all");// 返回的定位结果包含地址信息 all表示返回地址值，其他值都不返回
		option.setCoorType(coorType);// 返回的定位结果是百度经纬度,默认值gcj02
		// option.setScanSpan(5000);// 设置发起定位请求的间隔时间为5000ms
		option.setProdName("tuibo"); // 设置产品线名称（可选）
		option.disableCache(true);// 禁止启用缓存定位
		mLocationClient.setLocOption(option);
	}

	/**
	 * 百度位置监听器
	 * 
	 * @author Administrator
	 * 
	 */
	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			
			if (location == null){
				return;
			}
			la = location.getLatitude();
			lo = location.getLongitude();
			int locType = location.getLocType();
			TxData.public_location_info = location.getAddrStr();

			if (Utils.debug) {
				Log.i(TAG, "type-------"+locType+"la:" + la + "lo" + lo + "具体位置信息："
						+ location.getAddrStr());
			}
			Location gsmLocation = new Location("net_work");
			gsmLocation.setAccuracy((float) accuracy);
			gsmLocation.setLatitude(la);
			gsmLocation.setLongitude(lo);
			if (currentLocation != null) {
				if (isBetterLocation(gsmLocation, currentLocation)) {
					currentLocation = gsmLocation;
					TxData.public_latitude = gsmLocation.getLatitude();
					TxData.public_longitude = gsmLocation.getLongitude();
					showLocation(gsmLocation);
				} else {
					showLocation(currentLocation);
				}
			} else {
				currentLocation = gsmLocation;
				showLocation(gsmLocation);
			}
		}

		@Override
		public void onReceivePoi(BDLocation arg0) {
			// TODO Auto-generated method stub
		}
	}

	protected boolean isBetterLocation(Location location,
			Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > CHECK_INTERVAL;
		boolean isSignificantlyOlder = timeDelta < -CHECK_INTERVAL;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location,
		// use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must
			// be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	void update(final Context ct) {

		// count = 0;
		if (!Utils.isNull(manager.getNetworkOperator())) {
			// broadcastGetLocFailed(MsgInfor.INTENT_ACTION_GET_LOCATION_FAILED);
			// return;
			mcc = Integer.valueOf(manager.getNetworkOperator().substring(0, 3));
			mnc = Integer.valueOf(manager.getNetworkOperator().substring(3, 5));
			double cdma_lat = 0;
			double cdma_lng = 0;
			manager.listen(listener, 0);
			CellLocation cellLocation = manager.getCellLocation();
			if (cellLocation instanceof GsmCellLocation) {
				List<NeighboringCellInfo> list = manager
						.getNeighboringCellInfo();
				count = list.size();
				if (count > 2)
					count = 2;
				GsmCellLocation sm = (GsmCellLocation) cellLocation;
				for (int i = 0; i < count; i++) {
					lac[i] = sm.getLac();
					ci[i] = list.get(i).getCid();
				}
				lac[count] = sm.getLac();
				ci[count] = sm.getCid();
				count++;
			} else if (Integer.parseInt(Build.VERSION.SDK) >= 5
					&& cellLocation instanceof android.telephony.cdma.CdmaCellLocation) {
				android.telephony.cdma.CdmaCellLocation cdmaCl = (android.telephony.cdma.CdmaCellLocation) cellLocation;
				lac[0] = cdmaCl.getBaseStationId();
				ci[0] = cdmaCl.getSystemId();
				int networkId = cdmaCl.getNetworkId();
				if (Utils.debug)
					Log.i(TAG, "networkid:-11111111111111" + networkId);
				cdma_lat = (double) cdmaCl.getBaseStationLatitude() / 14400;
				if (Utils.debug)
					Log.i(TAG, "lat is:-11111111111111" + cdma_lat);
				cdma_lng = (double) cdmaCl.getBaseStationLongitude() / 14400;
				if (Utils.debug)
					Log.i(TAG, "lng is:-11111111111111" + cdma_lng);
				Location location = new Location("cdma");
				location.setLatitude(cdma_lat);
				location.setLongitude(cdma_lng);

//				showLocation(location);
				// if (lat != 0 && lng != 0) {
				// Utils.saveLocationData(ct, lat, lng);
				// }

				// return;
			}
			// 手机不支持基站定位
		}

		// /**
		// * 注册百度监听器，设置参数，并开启服务
		// */
		 mLocationClient.registerLocationListener(myListener); // 注册监听函数
		 mLocationClient.setAK(moblicKey);
		 setOption("bd09ll");
		 mLocationClient.start();

		Utils.executorService.submit(new Runnable() {
			@Override
			public void run() {
				// Looper.prepare();
				HttpEntity entity = null;
				try {
					DefaultHttpClient client = new DefaultHttpClient();
					HttpParams params = client.getParams();
					HttpConnectionParams.setConnectionTimeout(params, 20000);
					HttpConnectionParams.setSoTimeout(params, 3000);
					HttpPost post = new HttpPost(
							"http://www.google.com/loc/json");// http://www.google.com/glm/mmap");//
					JSONObject holder = new JSONObject();

					holder.put("version", "1.1.0");
					holder.put("request_address", true);
					holder.put("address_language", "zh_CN");

					JSONObject data;
					JSONArray array;
					array = new JSONArray();
					for (int i = 0; i < count; i++) {
						data = new JSONObject();
						data.put("cell_id", ci[i]); // 9457, 8321,8243
						data.put("location_area_code", lac[i]);// 28602
						data.put("mobile_country_code", mcc);// 208
						data.put("mobile_network_code", mnc);// 0
						data.put("age", 0);
						array.put(data);
					}
					holder.put("cell_towers", array);

					// ////////////////////WIFI定位//////////////////////////////////////
					if (array.length() == 0) {
						WifiManager wm = (WifiManager) txdata
								.getSystemService(Context.WIFI_SERVICE);
						List<ScanResult> wifiList = wm.getScanResults();
						for (int i = 0; i < wifiList.size(); i++) {
							if (Utils.debug)
								Log.e("wifi", "wifi"+wifiList.get(i).toString());
						}
						// WifiInfo info = wm.getConnectionInfo();
						// String mac = info.getMacAddress();
						// if(Utils.debug)Log.e("LocationMe","mac:"+mac);
						// data = new JSONObject();
						// data.put("mac_address", info.getBSSID());
						// data.put("signal_strength",
						// wm.getScanResults().get(0).level);
						// data.put("age", 0);
						// array = new JSONArray();
						// array.put(data);
						// holder.put("wifi_towers", array);
						for (int i = 0; i < wifiList.size(); i++) {
							data = new JSONObject();
							data.put("mac_address", wifiList.get(i).BSSID);
							data.put("ssid", wifiList.get(i).SSID);
							data.put("signal_strength", wifiList.get(i).level);
							array.put(data);
						}
						holder.put("wifi_towers", array);
					}
					// ////////////////////////////////

					StringEntity se = new StringEntity(holder.toString());
					post.setEntity(se);
					long b = System.currentTimeMillis();
					HttpResponse resp = client.execute(post);
					long rspe = System.currentTimeMillis();
					if (resp.getStatusLine().getStatusCode() == 200) {
						entity = resp.getEntity();
					}

					BufferedReader br = new BufferedReader(
							new InputStreamReader(entity.getContent()));
					StringBuffer sb = new StringBuffer();
					String result = br.readLine();
					while (result != null) {
						sb.append(result);
						result = br.readLine();
					}
					result_location = sb.toString();

					data = new JSONObject(sb.toString());
					data = (JSONObject) data.opt("location");
					if (data != null) {
						la = (Double) data.opt("latitude");
						lo = (Double) data.opt("longitude");
						accuracy = (Double) data.opt("accuracy");
						data = (JSONObject) data.opt("address");
						if (data != null) {
							String str = "";
							if (accuracy <= 800) {
								str = (String) data.opt("country")
										+ (String) data.opt("region")
										+ (String) data.opt("city")
										+ (String) data.opt("street");
							} else if (accuracy <= 2000) {
								str = (String) data.opt("country")
										+ (String) data.opt("region")
										+ (String) data.opt("city");
							} else {
								str = (String) data.opt("country")
										+ (String) data.opt("region");
							}
							TxData.public_location_info = str;
						}
					}
					Location gsmLocation = new Location("net_work");
					gsmLocation.setAccuracy((float) accuracy);
					gsmLocation.setLatitude(la);
					gsmLocation.setLongitude(lo);
					
					if (currentLocation != null) {
						if (isBetterLocation(gsmLocation, currentLocation)) {
							currentLocation = gsmLocation;
							TxData.public_latitude = gsmLocation.getLatitude();
							TxData.public_longitude = gsmLocation
									.getLongitude();
							showLocation(gsmLocation);
						} else {
							showLocation(currentLocation);
						}
					} else {
						currentLocation = gsmLocation;
						showLocation(gsmLocation);
					}
					 if(la != 0&& lo != 0){
					 Utils.saveLocationData(ct, la, lo);
					 }
					if (Utils.debug)
						Log.i(TAG,
								"handle data cost time is:"
										+ (System.currentTimeMillis() - rspe)
										+ "ms");
					// Message m = mHandler.obtainMessage();
					// m.what = 102;
					// m.sendToTarget();

				} catch (ClientProtocolException e) {
					if (Utils.debug)
						e.printStackTrace();
				} catch (IOException e) {
					// Location location = null;
					// showLocation(location);
					if (Utils.debug)
						e.printStackTrace();
					// if(Utils.debug)System.out.println("error:"+e.toString());
					// broadcastGetLocFailed(Constants.LOAD_LBS_INTENT_ACTION_NETWORK_LOCATION_FAILED);
				} catch (JSONException e) {
					if (Utils.debug)
						e.printStackTrace();
				} finally {
					if (entity != null) {
						try {
							entity.consumeContent();
						} catch (IOException e) {
							//
							if (Utils.debug)
								e.printStackTrace();
						}
					}
				}

				// Looper.loop();
			}
		});
	}

	class CellStateListener extends PhoneStateListener {
		public void onCellLocationChanged(CellLocation location) {
			GsmCellLocation gsmL = (GsmCellLocation) location;
			if (count < 2) {
				ci[count - 1] = gsmL.getCid();
				lac[count - 1] = gsmL.getLac();
				// count++;
				GsmCellLocation.requestLocationUpdate();
				// if (count == 2) {
				manager.listen(this, 0);
				// }
			}
		}
	}

	private void broadcast(Location location) {
		Intent intent = new Intent(Constants.INTENT_ACTION_GET_LOCATION_OK);
		intent.putExtra(Constants.EXTRA_LOCATION_KEY, location);
		txdata.sendBroadcast(intent);
		timeCancel();
		locationHandler.sendEmptyMessage(1);
	}

	private void broadcastGetLocFailed(String action) {
		Intent intent = new Intent(action);
		txdata.sendBroadcast(intent);
		timeCancel();
		locationHandler.sendEmptyMessage(1);
	}
}
