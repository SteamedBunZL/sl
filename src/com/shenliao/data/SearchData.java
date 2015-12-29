package com.shenliao.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.model.Area;
import com.tuixin11sms.tx.model.Language;
import com.tuixin11sms.tx.utils.CachedPrefs.PrefsMeme;
import com.tuixin11sms.tx.utils.CachedPrefs.PrefsSearch;
import com.tuixin11sms.tx.utils.Utils;

public class SearchData {
	private static Context context;
	private static SearchData sd = new SearchData();

	private SearchData() {

	}

	public static SearchData getInstance(Context context) {
		if (context != null) {
			SearchData.context = context;
		}
		return sd;
	}

	public void saveSearch(TX tx) {
		if (tx != null) {
			SharedPreferences sp = context.getSharedPreferences(
					String.format(PrefsSearch.SEARCH_PREFS, TX.tm.getUserID()), Context.MODE_PRIVATE);
			sp.edit().putInt(PrefsMeme.SEX, tx.getSex()).commit();
			sp.edit().putInt(PrefsMeme.AGE, tx.age).commit();
			sp.edit().putString(PrefsMeme.AREA, Utils.isNull(tx.area) ? String.valueOf(Area.UNLIMIT_ID) : tx.area)
					.commit();
			sp.edit().putString(PrefsMeme.CONSTELLATION, tx.constellation).commit();
			sp.edit().putInt(PrefsMeme.BLOODTYPE, tx.bloodType).commit();
			sp.edit()
					.putString(PrefsMeme.LANGUAGES,
							Utils.isNull(tx.getLanguages()) ? String.valueOf(Language.UNLIMIT_ID) : tx.getLanguages())
					.commit();
			sp.edit().putInt(PrefsMeme.IS_ONLINE, tx.getOnLine()).commit();
			TX.tm.reloadTXMe();/////
		}
	}

	public TX getSearch() {
		TX tx = new TX();
		SharedPreferences sp = context.getSharedPreferences(
				String.format(PrefsSearch.SEARCH_PREFS, TX.tm.getUserID()), Context.MODE_PRIVATE);
		tx.setSex(sp.getInt(PrefsMeme.SEX, 2));
		tx.setAge(sp.getInt(PrefsMeme.AGE, 0));
		tx.setArea(sp.getString(PrefsMeme.AREA, String.valueOf(Area.UNLIMIT_ID)));
		tx.setConstellation(sp.getString(PrefsMeme.CONSTELLATION, "0"));
		tx.setBloodType(sp.getInt(PrefsMeme.BLOODTYPE, 4));
		tx.setLanguages(sp.getString(PrefsMeme.LANGUAGES, String.valueOf(Language.UNLIMIT_ID)));
		tx.setOnLine(sp.getInt(PrefsMeme.IS_ONLINE, -1));
		return tx;
	}

}
