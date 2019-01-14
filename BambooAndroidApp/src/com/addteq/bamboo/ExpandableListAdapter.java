package com.addteq.bamboo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.addteq.stix.R;

import android.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.opengl.Visibility;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

	private Activity context;
	private HashMap<String, ArrayList<String>> project_plan_collection;
	private ArrayList<String> projectList;
	public ArrayList<String> originalList;
	private HashMap<String, String> statusMap;

	public ExpandableListAdapter(Activity context, ArrayList<String> projectList,
			HashMap<String, ArrayList<String>> project_plan_collection,HashMap<String, String> statusMap) {
		
		this.context = context;
		this.project_plan_collection = project_plan_collection;
		this.projectList = projectList;
	//	this.projectList.addAll(projectList);
		this.originalList=new ArrayList<String>();
		this.originalList.addAll(projectList);
		this.statusMap=statusMap;
	}

	public Object getChild(int groupPosition, int childPosition) {
		return project_plan_collection.get(projectList.get(groupPosition)).get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}
	
	
	public View getChildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		
		
		final String project = (String) getGroup(groupPosition);
		final String plan = (String) getChild(groupPosition, childPosition);
		
		String key = project+" - "+plan;
	
		
		LayoutInflater inflater = context.getLayoutInflater();
		
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.project_item_lv, null);
		}
		
		TextView item = (TextView) convertView.findViewById(R.id.project);
		
		ImageView icon = (ImageView) convertView.findViewById(R.id.delete);
		
	
		
		String status=statusMap.get(key);
		
		
		if(status!=null){
		
		if(status.equalsIgnoreCase("Successful")){
			
			icon.setImageResource(R.drawable.green_small);
			
		}else if(status.equalsIgnoreCase("Failed")){
			
			icon.setImageResource(R.drawable.failure_red);
			
		}else{
			
			icon.setImageResource(R.drawable.unknown_small);
		}

		}
		
		item.setText(plan);
		
		
		//Log.d("laptop","laptop "+project);
		
		convertView.setTag(project);
		

		return convertView;
	}

	public int getChildrenCount(int groupPosition) {
		return project_plan_collection.get(projectList.get(groupPosition)).size();
	}

	public String getGroup(int groupPosition) {
		
		String groupNameString=projectList.get(groupPosition);
		return groupNameString;
	}

	public int getGroupCount() {
		return projectList.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		
		String groupName = (String) getGroup(groupPosition);
		
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.group_item,
					null);
		}
		
		TextView item = (TextView) convertView.findViewById(R.id.project);
		item.setTypeface(null, Typeface.BOLD);

		item.setText(groupName);
		
	    return convertView;
	}

	public boolean hasStableIds() {
		return true;
	}
	

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	public void filterData(String query){
		query=query.toLowerCase(Locale.getDefault());
		projectList.clear();
		if(query.length()==0){
			projectList.addAll(originalList);
		}
		else{
			for(String projectName : originalList){
				if(projectName.toLowerCase(Locale.getDefault()).contains(query)){
				//	projectList.add(projectName);
					projectList.add(projectName);
				}
			}
		}
		notifyDataSetChanged();
	/*	query=query.toLowerCase();
		//projectList.clear();
		Log.d("project list", "" +projectList.size());
		if(query.isEmpty()){
			projectList.addAll(originalList);
		}
		else{
			/*Log.d("original list", "" +projectList.addAll(originalList));
			for(String projectName: originalList){
				ArrayList<String> newList=new ArrayList<String>();
				ArrayList<String> planlist=new ArrayList<String>();
				for(String plan: planlist){
					if(plan.toLowerCase().contains(query)){
						newList.add(query);
					}
				}
				if(newList.size()>0){
					Toast.makeText(context, "list is empty", Toast.LENGTH_SHORT).show();
				}
				}*/
		/*	for(int i=0;i<projectList.size();i++){
				Log.d("result", ""+projectList.get(i).toString());
				if(projectList.get(i).toString().contains(query)){
				//	Toast.makeText(context, "match", Toast.LENGTH_SHORT).show();
					originalList.addAll(projectList);
					}
				else{
					//Toast.makeText(context, "not match", Toast.LENGTH_SHORT).show();
				}
			}*/
		//}
		
		/*for(int i=0;i<projectList.size();i++){
			Log.d("result", ""+projectList.get(i).toString());
			if(projectList.get(i).toString().contains(query)){
				Toast.makeText(context, "match", Toast.LENGTH_SHORT).show();
			//	originalList.addAll(projectList);
				}
			else{
				Toast.makeText(context, "not match", Toast.LENGTH_SHORT).show();
			}
		}*/
	}
		
	
}