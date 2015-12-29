package com.tx.face.nativef;

public class NativeFace {
	static {
		System.loadLibrary("face-jni");
	}
	
	/**
	 * get face category from the Face file
	 * @param name : the file name
	 * @param lis : the call back
	 * @return 0 is success, other is error
	 */
	public static native int getFaceIntro(String name, IParseFaceObserver lis);
	
	/**
	 * get the GIF file from face entry 
	 * @param name : the file name
	 * @param index : the entry in entry's index
	 * @param outfile : the out save file
	 * @return 0 is success, other is error
	 */
	public static native int getFaceEntryFile(String name, int index, String outfile);
	
	/**
	 * get the GIF file, not save to file, out from the listen, get file stream
	 * @param name
	 * @param index
	 * @param lis
	 * @return
	 */
	public static native int getFaceEntryStream(String name, int index, IParseFaceObserver lis);
	
	
}
