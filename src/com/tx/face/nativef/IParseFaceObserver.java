package com.tx.face.nativef;

public interface IParseFaceObserver {
	/** get the face entry name */
	public static final int GET_NAME = 1;
	/** get the face category thumbnail */
	public static final int GET_THUMBNAIL = 2;
	/** read the gif file start */
	public static final int GIF_START = 3;
	/** reading the GIF file */
	public static final int GIF_READING = 4;
	/** read the GIF file finished */
	public static final int GIF_END		= 5;
	
	
	int onParseEvent(int event, String name, byte[] data);
}
