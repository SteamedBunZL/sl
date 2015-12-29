package com.tuixin11sms.tx.model;

public class Language {
	public static final int UNLIMIT_ID = 0;
	private int id;
	private String name;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public static Language createUnlimitLang(){
		Language lang = new Language();
		lang.id = UNLIMIT_ID;
		lang.name = "不限";
		return lang;
	}

}
