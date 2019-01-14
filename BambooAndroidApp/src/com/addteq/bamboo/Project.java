package com.addteq.bamboo;

import java.util.ArrayList;
import java.util.HashMap;

public class Project {
	private String title;
	private String key;
	private String plan1;
	private String plan2;
	private ArrayList<String> plansArrayList;
	private HashMap<String, String> statusMap;
	
	
	
	
	public String getPlan1() {
		return plan1;
	}
	public void setPlan1(String plan1) {
		this.plan1 = plan1;
	}
	public String getPlan2() {
		return plan2;
	}
	public void setPlan2(String plan2) {
		this.plan2 = plan2;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	public ArrayList<String> getPlansArrayList() {
		return plansArrayList;
	}
	public void setPlansArrayList(ArrayList<String> plansArrayList) {
		this.plansArrayList = plansArrayList;
	}
	public HashMap<String, String> getStatusMap() {
		return statusMap;
	}
	public void setStatusMap(HashMap<String, String> statusMap) {
		this.statusMap = statusMap;
	}
	
	
	
}
