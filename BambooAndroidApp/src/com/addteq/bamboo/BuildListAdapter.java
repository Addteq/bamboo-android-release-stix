package com.addteq.bamboo;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.addteq.stix.R;

public class BuildListAdapter extends ArrayAdapter<BuildListItem>{
	private final Activity context; 
	private List<BuildListItem> list;

	public BuildListAdapter(Activity context, List<BuildListItem> list){
		super(context, R.layout.buildlist_item, list);
		this.context=context;
		this.list = list;
	}
	
	static class ViewHolder{
		protected TextView text;
		protected TextView text2; 
		protected TextView text3;
		protected ImageView icon;	
		protected ImageView iconBug;	

	}
	
	@SuppressLint("ResourceAsColor")
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view = null;
		if(convertView ==null){
			LayoutInflater inflater = context.getLayoutInflater();
			view = inflater.inflate(R.layout.buildlist_item, null);
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.text = (TextView) view.findViewById(R.id.buildTitle);
			viewHolder.text2 = (TextView) view.findViewById(R.id.buildtime);
			viewHolder.text3 = (TextView) view.findViewById(R.id.buildreason);
			viewHolder.text3.setTextSize(10);
			viewHolder.icon = (ImageView) view.findViewById(R.id.buildstatus);
			viewHolder.iconBug = (ImageView) view.findViewById(R.id.feedback);

			view.setTag(viewHolder);
		}
		else{
			view=convertView;
		}
		ViewHolder holder = (ViewHolder) view.getTag();
		holder.text.setText(list.get(position).getBuildTitle());
		holder.text2.setText(list.get(position).getTime());
		holder.text3.setText(list.get(position).getReasonString());
		if(list.get(position).getCheck().equalsIgnoreCase("successful")){

			holder.icon.setImageResource(R.drawable.green_small);
			holder.text.setTextColor(Color.parseColor("#1DD48C"));
		}
		else{
			if(list.get(position).getCheck().equalsIgnoreCase("failed")){
				holder.icon.setImageResource(R.drawable.failure_red);
				holder.text.setTextColor(Color.parseColor("#D11D1B"));
			}
			else{
				holder.icon.setImageResource(R.drawable.unknown_small);
				holder.text.setTextColor(Color.parseColor("#F7A60A"));

			
			}
			
		}
		return view;
	}
}
