package com.tuixin11sms.tx.model;

import java.util.ArrayList;
import java.util.List;

public class Area {
	public static final int UNLIMIT_ID = 0;
	private int id;
	private String name;
	private List<Area> areaList = new ArrayList<Area>();

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

	public List<Area> getAreaList() {
		return areaList;
	}

	public void setAreaList(List<Area> areaList) {
		if (areaList != null)
			this.areaList = areaList;
	}
	
	public static Area createUnlimitArea(){
		Area area = new Area();
		area.id = UNLIMIT_ID;
		area.name="不限";//条件查找地区选择需要显示“不限” 2014.03.17 shc
		return area;
	}

}
