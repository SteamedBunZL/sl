package com.tuixin11sms.tx.utils;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

public class MusicUtils {
	//背景音乐
	private MediaPlayer mp;
	
	//音效
	private SoundPool sp;
	
	//定义音乐池的hashmap
	private HashMap<Integer,Integer> spMap;
	
	AudioManager am;
	public MusicUtils(){
		
	}
	//创建背景音乐
    public void CreatMusic(Context ct,int path,boolean isloop){
    	mp = MediaPlayer.create(ct, path);
    	mp.setLooping(isloop);
//    	mp.setVolume(2, 2);
    }
    public void release(){
    	if(mp!=null)
    	   mp.release();
    	if(sp!=null)
    	   sp.release();
    }
    //播放背景音乐
    public void PlayMusic(float volume){
       if(mp != null){
//    	try{
//    	 mp.prepare();
//    	 mp.start();
//    	}
//    	 catch(Exception e){
//             e.printStackTrace();
//         }
    	   mp.setVolume(volume, volume);
    	   mp.start();
       }
    }
    public void SetMusicVol(float volume){
    	mp.setVolume(volume, volume);
    }
    //音乐暂停
    public void PauseMusic(){
    	if(mp != null){
    	   mp.pause();
    	}
    	
    }
    
    //释放背景音乐
    public void RemoveMusic() {
        //
        mp.release();
    }
   //初始化 音乐池
    public void CreateSoundpool(Context ct){
    	//第一个参数是允许有多少个声音同时播放  第二个参数是声音播放的类型 第三个参数是声音播放的品质
    	sp = new SoundPool(10,AudioManager.STREAM_MUSIC,10);
    	spMap = new HashMap<Integer,Integer>();
    	//获得声音设备和设备音量
    	am = (AudioManager)ct.getSystemService(Context.AUDIO_SERVICE);
    	
    }
   
    //加载音效
    public void LoadSound(Context ct,int putid,int path,int lvid){
    	//id甚至音效放入的ID值 path音效路径  lvid优先级
    	spMap.put(putid,  sp.load(ct, path, lvid));
    }
    //播放音效
    public void PlaySound(int id,int uloop,int lv){
    	float volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
    	//uloop 循环几次 lv 优先级
     if(sp != null){
    	  sp.play(spMap.get(id), volume, volume, lv, uloop-1, 1f);
    	
     }
    }
    //暂停播放音效
//    public void PauseSound(int streamID){
//    	if(sp != null){
//    		sp.pause(spMap.get(streamID));
//    		
//    	}
//    }
//    public void ResumeSound(int streamID){
//    	sp.resume(streamID);
//    }
}
