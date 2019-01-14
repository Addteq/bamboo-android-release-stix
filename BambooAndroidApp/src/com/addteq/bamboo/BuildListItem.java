package com.addteq.bamboo;

public class BuildListItem{
	private String reasonString, buildTitle, lastBuild, buildKey;
	private String check;
	private int duration;
	public BuildListItem( ){
		this.reasonString ="Reason";
		this.buildTitle="Build Number";
		this.lastBuild = "Time";
		check = "Unknown";
		this.duration = 0;
	}
	public String getCheck() {
		return check;
	}
	public void setCheck(String check) {
		this.check = check;
	}
	public String getBuildTitle(){
		return this.buildTitle; 
	}
	public void setBuildTitle(String buildTitle){
		this.buildTitle = buildTitle;
	}
	public String getReasonString(){
		return this.reasonString; 
	}
	public void setReasonString(String inputReason){
		this.reasonString = inputReason;
	}
	public String getTime() {
		return this.lastBuild;
	}
	public void setTime(String lastBuild) {
		this.lastBuild = lastBuild;
	}
	public String getBuildKey() {
		return buildKey;
	}
	public void setBuildKey(String buildKey) {
		this.buildKey = buildKey;
	}
	public int getBuildDuration() {
		return duration;
	}
	public void setBuildDuration(int duration ) {
		this.duration = duration;
	}
}