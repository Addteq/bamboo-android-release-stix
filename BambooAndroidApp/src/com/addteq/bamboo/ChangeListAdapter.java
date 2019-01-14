package com.addteq.bamboo;

import java.util.ArrayList;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.addteq.stix.R;

public class ChangeListAdapter extends ArrayAdapter<Changes> {
	Activity context;
	ArrayList<Changes> messlist;
	int length;
	
	
	public ChangeListAdapter(Activity context, ArrayList<Changes> list, int length) {
		super(context, R.layout.change_list, list);
		this.context = context;
		this.messlist = list;
		this.length = length;
	}

	static class ViewHolder {
		public TextView author;
		public TextView message;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		View v = convertView;

		if (v == null) {
			LayoutInflater inflator = context.getLayoutInflater();
			v = inflator.inflate(R.layout.change_item, null, false);
			holder = new ViewHolder();
			holder.author = (TextView) v.findViewById(R.id.change_author);
			holder.message = (TextView) v.findViewById(R.id.change_message);
			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}
		
		String author = messlist.get(position).getAuthor();
		
		String content = messlist.get(position).getMessage();
		if(content.length() > length){
			content = content.substring(0,length)+"...";
		}
		
		holder.author.setText(author);
		holder.message.setText(content);
		
		return v;
	}

}