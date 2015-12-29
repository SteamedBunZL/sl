package com.tuixin11sms.tx.download;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.util.Log;

import com.tuixin11sms.tx.utils.Utils;

class DownloadDialog 
{
	private static final String TAG = "DownloadDialog";
	private ProgressDialog mProgressDialog = null;
	private Context mContext = null;
	
	public DownloadDialog(Context context,String title,String message) 
	{
		if(Utils.debug)Log.i(TAG, "C'tor()");
		
		mContext = context;
		
		mProgressDialog = new ProgressDialog(mContext);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setTitle(title);
		mProgressDialog.setMessage(message);
		mProgressDialog.setCancelable(false);	
		mProgressDialog.setOnDismissListener(new OnDismissListener(){
			@Override
			public void onDismiss(DialogInterface dialog) {
				//AutoSyncContactsAndSms已被删除，2013/08/15
//				AutoSyncContactsAndSms.Syncing=false;				
			}
			
		});
	}
	
	public void show()
	{
		if(Utils.debug)Log.i(TAG, "show()");
		
		mProgressDialog.show();
	}
	
	public void cancel()
	{
		if(Utils.debug)Log.i(TAG, "cancel()");
		
		mProgressDialog.cancel();
//		AutoSyncContactsAndSms.Syncing=false;
	}
	
	public void updateState(DownloadStates downloadState,String message)
	{
		if(Utils.debug)Log.i(TAG, "updateState(DownloadStates )");
		
		updateState(downloadState, -1,null,message);
	}
	
	public void updateState(DownloadStates downloadState, int progressValue,String data,String message)
	{
		if(Utils.debug)Log.i(TAG, downloadState+"updateState(DownloadStates, progressValue)"+data);
		
	    switch(downloadState) 
	    {
        	case MESSAGE_DOWNLOAD_STARTING :
        		mProgressDialog.setIndeterminate(true);
        		mProgressDialog.setMessage(message);
        		break;
        	case MESSAGE_DOWNLOAD_PROGRESS :
        		mProgressDialog.setIndeterminate(false);
        		mProgressDialog.setMessage(message);
        		mProgressDialog.setProgress(progressValue);
        		break;
        	case MESSAGE_DOWNLOAD_COMPLETE :
        		if(mProgressDialog.isShowing())
        		 mProgressDialog.cancel();
        		//AutoSyncContactsAndSms被删除，2013/08/15
//        		AutoSyncContactsAndSms.Syncing=false;
        		AutoUpdater.isUping=false;
        		break;
        	case MESSAGE_DOWNLOAD_ERROR :
        		mProgressDialog.setIndeterminate(false);
        		mProgressDialog.setMessage(message);
        		mProgressDialog.setProgress(progressValue);
        		break;
        	case MESSAGE_DOWNLOAD_NET_ERROR :
        		mProgressDialog.setIndeterminate(false);
        		mProgressDialog.setMessage(message);
        		mProgressDialog.setProgress(progressValue);
        		break;
        	default:
    	}
	}
}
