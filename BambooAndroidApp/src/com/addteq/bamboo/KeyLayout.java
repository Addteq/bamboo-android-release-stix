package com.addteq.bamboo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.addteq.stix.R;

public class KeyLayout extends RelativeLayout {

	public KeyLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.login, this);
	}

	public KeyLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.login, this);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		final int proposedheight = MeasureSpec.getSize(heightMeasureSpec);
		
		//below line fixed the bug BIP-1307. Instead of getting the height from the system using getHeight() it has been hard coded to 943 pixels .
		final int actualHeight = 943;
		//final int actualHeight = getHeight();
		
		LinearLayout images = (LinearLayout) findViewById(R.id.top);

		if (actualHeight > proposedheight) {
			// Keyboard is shown
			images.setVisibility(View.GONE);

		} else {
			// Keyboard is hidden
			images.setVisibility(View.VISIBLE);

		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}	 
}
