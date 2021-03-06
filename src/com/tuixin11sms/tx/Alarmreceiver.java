package com.tuixin11sms.tx;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tuixin11sms.tx.utils.Constants;


//这个广播也许是有用的，因为有引用，并且有地方开启这个服务 2014.01.21  shc
public class Alarmreceiver extends BroadcastReceiver{
	public static boolean isexit;
//	private SharedPreferences prefs = null;
	private void broadcastMsg(Context context, String msg) {
		if(!msg.trim().equals("")){
//			Log.i(TAG, "broadcastMsg"+msg);
			// 指定广播目标的 action （注：指定了此 action 的 receiver 会接收此广播）
			Intent intent = new Intent(Constants.INTENT_ACTION_SEND_MSG);
			// 需要传递的参数
			intent.putExtra("alarm", msg);
			// 发送广播
			context.sendBroadcast(intent);
		}
	}
	
	public void onReceive(Context context, Intent intent) {
		//心跳周期性检测是否在线
		 if(intent.getAction().equals("TuixinServicecheck")){
			if(!isexit&&System.currentTimeMillis()-TuixinService1.startTime>180 * 1000){ 
				broadcastMsg(context,"alarm");  
				 ActivityManager mActivityManager =    
			            (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);   
			           
			        List<ActivityManager.RunningServiceInfo> mServiceList = mActivityManager.getRunningServices(30);   
			        final String tuixinClassName = "com.tuixin11sms.tx.TuixinService1";   
			                 
			        boolean b = TuixinServiceIsStart(mServiceList, tuixinClassName);  
			        if(!b){
			        	context.startService(new Intent(context, 
								TuixinService1.class));
			        }
			}			 			
         }
	}
	 //通过Service的类名来判断是否启动某个服务   
    private boolean TuixinServiceIsStart(List<ActivityManager.RunningServiceInfo> mServiceList,String className){   
           
        for(int i = 0; i < mServiceList.size(); i ++){   
            if(className.equals(mServiceList.get(i).service.getClassName())){ 
                return true;   
            }   
        }   
        return false;   
    }
}
