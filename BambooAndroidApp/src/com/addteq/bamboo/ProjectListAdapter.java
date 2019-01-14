package com.addteq.bamboo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.R.drawable;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.addteq.stix.R;

public class ProjectListAdapter extends ArrayAdapter<Project> {
	Activity context;
	ArrayList<Project> projects;
	HashMap<String, String> mapPlan1;
	HashMap<String, String> mapPlan2;
	ProjectListActivity  plActivity;
    private ArrayList<Project> arrayList;
	private static LayoutInflater inflater = null;
	final int thresholdPlans=1000;

	public ProjectListAdapter(Activity context, ArrayList<Project> projects,
	    HashMap<String, String> map1, HashMap<String, String> map2) {
		super(context, R.layout.project_item, projects);
		this.context = context;
		this.projects = projects;
		this.mapPlan1 = map1;
		this.mapPlan2 = map2;
		this.arrayList = new ArrayList<Project>(projects);
		this.arrayList.addAll(projects);
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	static class ViewHolder {
		public TextView titleTV;
		// public TextView successTV;
		public TextView successNoTV;
		// public TextView failureTV;
		public TextView failureNoTV;

		public TextView namePlan1;
		public TextView namePlan2;

		protected ImageView successIV;
		protected ImageView failureIV;
	}
	
	public int getCount() {
		return projects.size();
	}

	/*
	 * public Object getItem(int position) { return position; }
	 */

	public long getItemId(int position) {
		return position;
	}
	
	///custom adapter for sub list view which displays plans for each project
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		View v = convertView;
		String projectKey, planName1, planName2, keyPlan1, statePlan1, keyPlan2, statePlan2;

		try {
			inflater = context.getLayoutInflater();
			v = inflater.inflate(R.layout.project_item, null, false);
			holder = new ViewHolder();

			holder.titleTV = (TextView) v.findViewById(R.id.project_title_tv);
			holder.successNoTV = (TextView) v.findViewById(R.id.success_no_tv);
			holder.failureNoTV = (TextView) v.findViewById(R.id.failure_no_tv);
			holder.namePlan1 = (TextView) v
					.findViewById(R.id.successful_plans_tv);
			holder.namePlan2 = (TextView) v.findViewById(R.id.failed_plans_tv);
			holder.successIV = (ImageView) v.findViewById(R.id.success);
			holder.failureIV = (ImageView) v.findViewById(R.id.failure);

			holder.titleTV.setText(projects.get(position).getTitle());

			holder.namePlan1.setText(projects.get(position).getPlan1());
			holder.namePlan2.setText(projects.get(position).getPlan2());

			projectKey = projects.get(position).getKey();
			planName1 = projects.get(position).getPlan1();
			planName2 = projects.get(position).getPlan2();
			

			if (projects.get(position).getPlan2() == null) {
				holder.failureIV.setVisibility(v.GONE);
			} else {

				// display the second plan of each project
				keyPlan2 = planName2 + "@" + projectKey;
	
				statePlan2 = mapPlan2.get(keyPlan2);
				
				if(statePlan2==null){
					plActivity.getAllBuildsRefresh(mapPlan2.size()+mapPlan1.size(), thresholdPlans);
					//make a json request to get the next set of plans
					
				}
				else{
					
					if (statePlan2.equalsIgnoreCase("successful")) {
						holder.failureIV.setImageResource(R.drawable.success);

					} else if (statePlan2.equalsIgnoreCase("failed")) {
						holder.failureIV.setImageResource(R.drawable.failure);
					} else {
						holder.failureIV.setImageResource(R.drawable.unknown);
					}
				}
			}

			
			keyPlan1 = planName1 + "@" + projectKey;
			
			statePlan1 = mapPlan1.get(keyPlan1);
			
			if(statePlan1 == null){
				plActivity.getAllBuildsRefresh(mapPlan2.size()+mapPlan1.size(), thresholdPlans);
				//make a json request to get the next set of plans
			}else{

				if (statePlan1.equalsIgnoreCase("successful")) {
					holder.successIV.setImageResource(R.drawable.success);

				} else if (statePlan1.equalsIgnoreCase("failed")) {
					holder.successIV.setImageResource(R.drawable.failure);
				} else {

					holder.successIV.setImageResource(R.drawable.unknown);
				}
			}
			v.setTag(projects.get(position).getKey());

		} catch (Exception e) {
		}

		return v;
	}


}