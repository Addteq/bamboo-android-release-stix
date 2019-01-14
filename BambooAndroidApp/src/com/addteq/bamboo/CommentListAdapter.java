package com.addteq.bamboo;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.addteq.stix.R;


public class CommentListAdapter  extends ArrayAdapter<Comment>{
	private final Context context;
	private final ArrayList<Comment> comments;
	
	public CommentListAdapter(Context context, ArrayList<Comment> comments) {
		super(context, R.layout.comment_list, comments);
		this.context = context;
		this.comments = comments;
	}
	
	

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return comments.size();
	}



	static class ViewHolder {		
		//public TextView authorTV;
		public TextView commentTV;
		public TextView timeTV;
		public LinearLayout wrapper;
		//public ImageView startBuild;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		View v = convertView;
		
		Boolean left=true;
		
		
		if (v == null) {
			LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflator.inflate(R.layout.comment_bubble, null, false);
			//v.setLayoutParams(new AbsListView.LayoutParams(lv.getWidth(), AbsListView.LayoutParams.WRAP_CONTENT));
			holder = new ViewHolder();
			//holder.authorTV = (TextView)v.findViewById(R.id.authorTV);
			holder.commentTV = (TextView)v.findViewById(R.id.bubbleComment);
			holder.timeTV = (TextView)v.findViewById(R.id.tvDate);
			holder.wrapper = (LinearLayout) v.findViewById(R.id.wrapper);
			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}
		
		String email = comments.get(position).getAuthor();
		SharedPreferences pref =PreferenceManager.getDefaultSharedPreferences(context);
		String en_mine = pref.getString("encryptedid", "");
		String mine=null;
		try {
			mine = SimpleCrypto.decrypt(pref.getString("encryptKey", "@ddt3q"), en_mine);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(email.equalsIgnoreCase(mine)){
			left=false;
			Log.d("mine"+email, comments.get(position).getContent());
			
		}else{
			Log.d("yours", comments.get(position).getContent());
		}
		
		
		
		//String author = email.replace(".", "\n");
		String content = comments.get(position).getContent();
		String creationDate = comments.get(position).getCreationDate();

		//holder.authorTV.setText(author);
		holder.commentTV.setText(content);
		holder.timeTV.setText(creationDate);
		holder.timeTV.setGravity(left ? Gravity.LEFT : Gravity.RIGHT);
		//holder.commentTV.setBackgroundResource(left ? R.drawable.bubble_white : R.drawable.bubble_blue);
		holder.commentTV.setBackgroundResource(left ? R.drawable.bubble_your : R.drawable.bubble_mine);
		holder.wrapper.setGravity(left ? Gravity.LEFT : Gravity.RIGHT);
		return v;
	}
	
	
}
