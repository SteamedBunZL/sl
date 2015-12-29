package com.tuixin11sms.tx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tuixin11sms.tx.utils.Constants;
/**
 * 接收开机启动广播
 * @author 郭伟洲
 *
 */
public class BootReceiver extends BroadcastReceiver {
    
    public void onReceive(Context context, Intent intent) 
    {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) 
        {
            Intent in = new Intent(context,TuixinService1.class);
            in.putExtra(Constants.EXTRA_BOOT_START_KEY, Constants.EXTRA_BOOT_START_VALUE);
            context.startService(in);//启动服务
            //这边可以添加开机自动启动的应用程序代码
        } 
    }
     
}
