package com.tuixin11sms.tx.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.utils.Utils;

public class AutoUpdater 
{
	private static String TAG="AutoUpdater";
//	private static final String APPLICATION_PROPERTIES_URL = "https://privatewificonnect.googlecode.com/svn/trunk/WiFiConnect/application.properties";	
	private String DOWNLOAD_FILEPATH = "/download";
    private String APPLICATION_DATA_BASE_URL= "http://tx.tuixin11.com/r.php?url=http://tx.tuixin11.com/download/";
	private final Context mContext;
	private DownloadDialogHandler mDownloadHandler = null;
	private Resources mResources = null;
	private SharedPreferences prefs = null;
	private Editor editor;
	private Uri uri;
	private String filename;
	public static boolean isUping;
	public SessionManager mSess = null;
	private AutoUpdater(Context context,String url,String log,int ver)
	{
		isUping=true;
		this.mContext = context;
		this.mResources = context.getResources();
		mSess = SessionManager.getInstance();
//		this.APPLICATION_DATA_BASE_URL=url;
		uri= Uri.parse(url);
		if (Utils.debug)Log.i(TAG, "url"+url);
		filename=uri.getLastPathSegment();
		if (Utils.debug)Log.i(TAG, "filename"+filename);
		this.APPLICATION_DATA_BASE_URL=url.substring(0, url.length()-filename.length());
		if (Utils.debug)Log.i(TAG, this.APPLICATION_DATA_BASE_URL);
		mDownloadHandler = new DownloadDialogHandler(new DownloadDialog(context,
				mResources.getString(R.string.prompt),
				mResources.getString(R.string.downloading)));
		
		if(ver!=0){
//			prefs = context.getSharedPreferences(CommonData.MEME_PREFS,
//					Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
//			editor=prefs.edit();
//			editor.putInt(CommonData.OLD_VER, ver);
//			editor.commit();
			mSess.mPrefMeme.old_ver.setVal(ver).commit();
			}
		  
		
	}
	
	
    /*
     * Update checking. We go to a predefined URL and fetch a properties style file containing
     * information on the update. These properties are:
     * 
     * versionCode: An integer, version of the new update, as defined in the manifest. Nothing will
     *              happen unless the update properties version is higher than currently installed.
     * fileName: A string, URL of new update apk. If not supplied then download buttons
     *           will not be shown, but instead just a message and an OK button.
     * message: A string. A yellow-highlighted message to show to the user. Eg for important
     *          info on the update. Optional.
     * title: A string, title of the update dialog. Defaults to "Update available".
     * 
     * Only "versionCode" is mandatory.
     */
    private void checkForUpdate() 
    {
    	if(Utils.debug)Log.i(TAG,"checkForUpdate()");
    	
    	if (!isUpdateEnabled()) 
    	{
    		return;
    	}
    	Utils.executorService.submit(new Runnable() {
			@Override
			public void run() {
				if(Utils.debug)Log.i(TAG, "checkForUpdate().Thread.run()"+"Checking for updates...");
				
						displayUpdateDialog(filename);
			}
		}); 
    }
   
    private void downloadUpdate(final String downloadFileUrl, final String fileName) {
    	if(Utils.debug)Log.i(TAG, "downloadUpdate()");
    	Utils.executorService.submit(new Runnable() {
			@Override
			public void run() {
//				Looper.prepare();
				if(Utils.debug)Log.i(TAG, "downloadUpdate().Thread.run()"+"Downloading update..."+downloadFileUrl);
				Message msg = Message.obtain();
            	msg.obj = DownloadStates.MESSAGE_DOWNLOAD_STARTING;
            	Bundle data=new Bundle();
            	data.putString("msg", mResources.getString(R.string.download_starting));
            	msg.setData(data);
            	mDownloadHandler.sendMessage(msg);
            	if(downloadUpdateFile(downloadFileUrl, fileName)){
            		Intent intent = new Intent(Intent.ACTION_VIEW); 
    				String storagePath=Utils.getStoragePath(mContext);
    			    intent.setDataAndType(android.net.Uri.fromFile(new File(storagePath+DOWNLOAD_FILEPATH+"/"+fileName)),"application/vnd.android.package-archive"); 
    			    mContext.startActivity(intent);
//    			    TuixinService.notNeedCheckActivityState=true;
            	}else{
            		Message msg6 = new Message();
					msg6.what = 1;
					mHandler.sendMessage(msg6);
            	}			
//				Looper.loop();
			}
		}); 
//    	new Thread(new Runnable(){
//			public void run(){
//				Log.i("AutoUpdater", "downloadUpdate().Thread.run()"+"Downloading update..."+downloadFileUrl);
//				Message msg = Message.obtain();
//            	msg.obj = DownloadStates.MESSAGE_DOWNLOAD_STARTING;
//            	Bundle data=new Bundle();
//            	data.putString("msg", mResources.getString(R.string.download_starting));
//            	msg.setData(data);
//            	mDownloadHandler.sendMessage(msg);
//            	if(downloadUpdateFile(downloadFileUrl, fileName)){
//            		Intent intent = new Intent(Intent.ACTION_VIEW); 
//    				String storagePath=Utils.getStoragePath(mContext);
//    			    intent.setDataAndType(android.net.Uri.fromFile(new File(storagePath+DOWNLOAD_FILEPATH+"/"+fileName)),"application/vnd.android.package-archive"); 
//    			    mContext.startActivity(intent);
////    			    TuixinService.notNeedCheckActivityState=true;
//            	}else{
//            		Message msg6 = new Message();
//					msg6.what = 1;
//					mHandler.sendMessage(msg6);
//            	}
//			}
//    	}).start();
    }
    
    private boolean isUpdateEnabled()
    {
    	if(Utils.debug)Log.i(TAG, "isUpdateEnabled()");
    	return true;
    }
	
	private boolean downloadUpdateFile(String downloadFileUrl, String destinationFilename) {
		if(Utils.debug)Log.i(TAG, "downloadUpdateFile()");
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) == false) {
			return false;
		}
		String storagePath=Utils.getStoragePath(mContext);
		File downloadDir = new File(storagePath+DOWNLOAD_FILEPATH);
		if (downloadDir.exists() == false) {
			downloadDir.mkdirs();
		}
		else {
			File downloadFile = new File(storagePath+DOWNLOAD_FILEPATH+"/"+destinationFilename);
			if (downloadFile.exists()) {
				downloadFile.delete();
			}
		}
		return this.downloadFile(downloadFileUrl, storagePath+DOWNLOAD_FILEPATH, destinationFilename);
	}
	
	private boolean downloadFile(String url, String destinationDirectory, String destinationFilename) {
		if(Utils.debug)Log.i(TAG, "downloadFile()");
		boolean filedownloaded = true;
		HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(String.format(url));
        Message msg = Message.obtain();
        try {
            HttpResponse response = client.execute(request);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() == 200) {
	            HttpEntity entity = response.getEntity();
	            InputStream instream = entity.getContent();
	            int fileSize = (int)entity.getContentLength();
	            if(Utils.debug)Log.i(TAG, "downloadFile()"+fileSize);
	            FileOutputStream out = new FileOutputStream(new File(destinationDirectory+"/"+destinationFilename));
	            byte buf[] = new byte[8192];
	            int len;
	            int totalRead = 0;
	            while((len = instream.read(buf)) > 0) {
	            	msg = Message.obtain();
	            	msg.obj = DownloadStates.MESSAGE_DOWNLOAD_PROGRESS;
	            	totalRead += len;
	            	msg.arg1 = totalRead;// / 1024;
	            	msg.arg2 = fileSize;// / 1024;
	            	float fileLen = (float)msg.arg2/1024;
	            	float readLen = (float)msg.arg1/1024;
	            	DecimalFormat df  = new DecimalFormat( "0.00"); 
	            	String fileLen_str = df.format(fileLen);
	            	String readLen_str = df.format(readLen);
	            	if(Utils.debug)Log.i(TAG, "msg.arg1"+msg.arg1+"msg.arg2"+msg.arg2);
	            	String title=mResources.getString(R.string.downloading)+"\n"+readLen_str+"K"+"/"+fileLen_str+"K";
//	            	System.out.println("fileLen="+fileLen);
//	            	System.out.println("fileLen_str="+fileLen_str);
	            	Bundle data=new Bundle();
	            	data.putString("msg", title);
	            	msg.setData(data);
	            	mDownloadHandler.sendMessage(msg);
	            	out.write(buf,0,len);
            	}
	            out.close();
            }
            else {
            	throw new IOException();
            }
        } 
        catch (IOException e) 
        {
        	if(Utils.debug)Log.i(TAG, "downloadFile()"+e);
        	filedownloaded = false;
        }
        msg = Message.obtain();
        msg.obj = DownloadStates.MESSAGE_DOWNLOAD_COMPLETE;
        mDownloadHandler.sendMessage(msg);
        return filedownloaded;
	}
	private void displayUpdateDialog(final String fileName)
	{
		if(Utils.debug)Log.i(TAG, "displayUpdateDialog()");
//		String msg=mResources.getString(R.string.new_version_update_question);
////		String oldver=""+ver;
////		oldver.replaceAll("0", ".");
//		msg=msg.replace("$", ""+ver);
//		msg=msg.replace("*", ""+Utils.appver);
//		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//		builder.setTitle(R.string.up_prompt)
//		.setCancelable(false) .setMessage(slog)
//		       .setCancelable(false)
//		       .setPositiveButton(R.string.up_new, new DialogInterface.OnClickListener() {
//		           public void onClick(DialogInterface dialog, int id) 
//		           {
//		        	   Log.i("AutoUpdater", "displayUpdateDialog().AlertDialog.Builder.PositiveButton.onClick()");
//		        	   dialog.cancel();
		        	   downloadUpdate(APPLICATION_DATA_BASE_URL + fileName, fileName);
//		           }
//		       })
//		       .setNegativeButton(R.string.up_later, new DialogInterface.OnClickListener() {
//		           public void onClick(DialogInterface dialog, int id) {
//		        	   Log.i("AutoUpdater", "displayUpdateDialog().AlertDialog.Builder.NegativeButton.onClick()");
//		                dialog.cancel();
//		                isUping=false;
//		           }
//		       });
//		AlertDialog alert = builder.create();
//		alert.show();
	}
	Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			int n = msg.what;
			switch(n){
			case 1:
//				 Uri uri = Uri.parse("http://"+editText.getText().toString());   
			        //通过Uri获得编辑框里的//地址，加上http://是为了用户输入时可以不要输入   
			     Intent intent = new Intent(Intent.ACTION_VIEW,uri);    
			        //建立Intent对象，传入uri   
			     mContext.startActivity(intent);   
//				new AlertDialog.Builder(mContext)
//				.setTitle(R.string.error)
//				.setMessage(R.string.download_fail)
//				.setPositiveButton(R.string.confirm,
//						new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog,
//									int which) {
//								dialog.cancel();
//							}
//						}).show();
				break;
			}
			super.handleMessage(msg);
		}
	};
	public static void CheckForUpdate(Context context,String url,String log,int ver)
	{
		if(Utils.debug)Log.i(TAG, "CheckForUpdate()");
		AutoUpdater autoUpdater = new AutoUpdater(context,url,log,ver);
		autoUpdater.checkForUpdate();
	}
}
