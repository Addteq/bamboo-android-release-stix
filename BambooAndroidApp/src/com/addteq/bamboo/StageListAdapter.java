package com.addteq.bamboo;

import java.util.ArrayList;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.addteq.stix.R;

public class StageListAdapter extends ArrayAdapter<String>{
	private final Activity context; 
	ArrayList<String>list;
	
	public StageListAdapter(Activity context, ArrayList <String> list){
		super(context, R.layout.stage_item, list);
		this.context=context;
		this.list = list;
	}
	
	static class ViewHolder{
		protected TextView text;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view = null;
		if(convertView ==null){
			LayoutInflater inflater = context.getLayoutInflater();
			view = inflater.inflate(R.layout.stage_item, null);
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.text = (TextView) view.findViewById(R.id.stageName);
			view.setTag(viewHolder);
		}
		else{
			view=convertView;
		}
		ViewHolder holder = (ViewHolder) view.getTag();
		holder.text.setText(list.get(position));
		return view;
	}
}
