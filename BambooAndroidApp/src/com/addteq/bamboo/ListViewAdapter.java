package com.addteq.bamboo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.addteq.stix.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListViewAdapter extends BaseAdapter {
	 // Declare Variables
    Context mContext;
    LayoutInflater inflater;
    private List<Project> projectList=null;
    private ArrayList<Project> arrayList;
	

	public ListViewAdapter(Context context, List<Project> projectList) {

		mContext = context;
		this.projectList = projectList;
		inflater = LayoutInflater.from(mContext);
		this.arrayList = new ArrayList<Project>(projectList);
		this.arrayList.addAll(projectList);
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


	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return projectList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return projectList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		// TODO Auto-generated method stub
		String projectKey, planName1, planName2, keyPlan1, statePlan1, keyPlan2, statePlan2;

		 final ViewHolder holder;
	        if (view == null) {
	            holder = new ViewHolder();
	            view = inflater.inflate(com.addteq.stix.R.layout.project_item, null);

				holder.titleTV = (TextView) view.findViewById(com.addteq.stix.R.id.project_title_tv);
				holder.successNoTV = (TextView) view.findViewById(com.addteq.stix.R.id.success_no_tv);
				holder.failureNoTV = (TextView) view.findViewById(com.addteq.stix.R.id.failure_no_tv);
				holder.namePlan1 = (TextView) view
						.findViewById(com.addteq.stix.R.id.successful_plans_tv);
				holder.namePlan2 = (TextView) view.findViewById(com.addteq.stix.R.id.failed_plans_tv);
				holder.successIV = (ImageView) view.findViewById(com.addteq.stix.R.id.success);
				holder.failureIV = (ImageView) view.findViewById(com.addteq.stix.R.id.failure);
				view.setTag(holder); } else {
		            holder = (ViewHolder) view.getTag();
		        }
	        // Set the results into TextViews
	        holder.titleTV.setText(projectList.get(position).getTitle());
	        holder.namePlan1.setText(projectList.get(position).getPlan1());
	        holder.namePlan2.setText(projectList.get(position).getPlan2());

			projectKey = projectList.get(position).getKey();
			planName1 = projectList.get(position).getPlan1();
			planName2 = projectList.get(position).getPlan2();
			
	            return view;
	}
	 // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        projectList.clear();
        if (charText.length() == 0) {
            projectList.addAll(arrayList);
        } 
        else 
        {
            for (Project p : arrayList) 
            {
                if (p.getTitle().toLowerCase(Locale.getDefault()).contains(charText)) 
                {
                    projectList.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }
}
