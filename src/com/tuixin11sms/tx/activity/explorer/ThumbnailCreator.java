/*
    Open Manager, an open source file manager for the Android system
    Copyright (C) 2009, 2010, 2011  Joe Berria <nexesdevelopment@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.tuixin11sms.tx.activity.explorer;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.graphics.Bitmap;


public class ThumbnailCreator {	
	private static HashMap<String, SoftReference<Bitmap>> mCacheMap = null;

	public ThumbnailCreator() {
		if(mCacheMap == null)
			mCacheMap = new HashMap<String, SoftReference<Bitmap>>();
	}
	
	public Bitmap isBitmapCached(String name) {
		SoftReference<Bitmap> soft=mCacheMap.get(name);
		return (soft!=null) ? soft.get() : null;
	}
	
	//缓存bitmap
	public Bitmap cacheThumbBitmap(String filePath,Bitmap bitmap){
		mCacheMap.put(filePath, new SoftReference<Bitmap>(bitmap));
		return bitmap;
	}

	
	
}