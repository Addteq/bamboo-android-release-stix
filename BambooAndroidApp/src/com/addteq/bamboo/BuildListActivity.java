package com.addteq.bamboo;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.addteq.stix.R;
import com.atlassian.jconnect.droid.Api;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import android.R.integer;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class BuildListActivity extends SherlockFragmentActivity{
	final Context context = this;
	ArrayList<String> keys;
	private ArrayList<String> plans;
	private String projectName;
	private PersistentCookieStore myCookieStore;
	private static AsyncHttpClient client = new AsyncHttpClient();
	private static String mTag = "";

	SharedPreferences pref;
	SharedPreferences.Editor editor;
	
	
	Date now;
	Calendar cal;
	Date yestarday;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		UncaughtExceptionHandler mUEHandler = new Thread.UncaughtExceptionHandler() {

	        @Override
	        public void uncaughtException(Thread t, Throwable e) {
	            e.printStackTrace();
	            Api.handleException(e);
	            BuildListActivity.this.finish();
	        }
	    };
	    Thread.setDefaultUncaughtExceptionHandler(mUEHandler);
        myCookieStore = new PersistentCookieStore(BuildListActivity.this);
		myCookieStore.getCookies();
		client.setCookieStore(myCookieStore);
		
		pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		editor = pref.edit();
		
        ActionBar ab = getSupportActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(true);
        ab.setTitle("Project List");
        
        Intent incomingIntent = getIntent(); 
        projectName = incomingIntent.getStringExtra("projectName");

        keys = incomingIntent.getStringArrayListExtra("keys");
        plans = incomingIntent.getStringArrayListExtra("plans");
        
        int child=incomingIntent.getIntExtra("child",0);
        
           	
        	//TODO
        Bundle projectAndPlanName = new Bundle();
     	projectAndPlanName.putString("projectName",projectName );
       	projectAndPlanName.putString("planName", plans.get(child));
        	
	        ab.addTab(ab.newTab()
	        		.setText(plans.get(child))
	        		.setTag(keys.get(child))
	        		.setTabListener(new TabListener<BuildListFragment>
	        		(this, projectAndPlanName,keys.get(child), BuildListFragment.class)));
	        
	        Log.e("TESTING", plans.get(child));
	        
        //}
        

        if(savedInstanceState != null) {
        	ab.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }
    }
    /*
    public void onBackPressed() {
    	//Intent back = new Intent(this, ProjectListActivity.class);
    	//startActivity(back);
    }
    */
	

	@Override
	protected void onResume() {
	
  	super.onResume();
	
	
	//getting yestardays time
	now = new Date();  
	cal = Calendar.getInstance();  
	cal.setTime(now);  
	cal.add(Calendar.DAY_OF_YEAR, -1);
	yestarday = cal.getTime();
	
	long expireDate=pref.getLong("expiredate", -1);
	
	if(expireDate>0){
		
		if (yestarday.getTime() > expireDate){
			
			//Toast.makeText(getApplicationContext(), "expired", Toast.LENGTH_LONG).show();
			
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					BuildListActivity.this);
			
			alertDialogBuilder.setNegativeButton("Update",
					new DialogInterface.OnClickListener() {
						public void onClick(
								DialogInterface dialog,
								int id) {
							
							Intent webintent = new Intent(
									Intent.ACTION_VIEW,
									Uri.parse("https://marketplace.atlassian.com/plugins/com.addteq.bamboo.plugin.addteq-bamboo-plugin"));
							startActivity(webintent);

						}
					});
			
			 alertDialogBuilder.setPositiveButton("Settings",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog,
									int id) {
					
								myCookieStore.clear();
								editor.putString("firsttime", "expire");
								if (pref.getBoolean("CHECK", false) == false) {
									editor.remove("encryptedpass");
								}
								editor.remove("loggedin");
								editor.remove("GuestLogin");
					            //editor.remove("SSLCHECK");
								editor.commit();
								BuildListActivity.this.finish();
								Intent intent3 = new Intent(BuildListActivity.this,
										NotificationService.class);
								intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								stopService(intent3);
								Intent goToLoginIntent = new Intent(BuildListActivity.this,
										LoginActivity.class);
								startActivity(goToLoginIntent);
							}
						});
			 
		alertDialogBuilder.setTitle("Plugin licence expired");
		alertDialogBuilder.setMessage("Your plugin license in server is expired. Please renew.");
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.show();

		
		}
		
		
	}
	
	
	}
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getSupportActionBar().getSelectedNavigationIndex());
    }
    public static class TabListener<T extends SherlockFragment> implements ActionBar.TabListener {
    	private final SherlockFragmentActivity mActivity;
    	private final Class<T> mClass;
    	private final Bundle mArgs;
    	
    	private Bundle projectAndPlanName = new Bundle();
    	private com.actionbarsherlock.app.SherlockFragment mFragment;
    	
    	
    	public TabListener(SherlockFragmentActivity activity, Bundle infoBundle, String tag, Class<T> clz) {
    		this(activity, tag, clz, null);
    		projectAndPlanName = infoBundle;
    	}
    	
    	public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz) {
    		this(activity, tag, clz, null);
    	}
    	
    	public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz, Bundle args) {
    		mActivity = activity;
    		mTag = tag;
    		mClass = clz;
    		mArgs = args;
    		
    		mFragment =  (SherlockFragment) mActivity.getSupportFragmentManager().findFragmentByTag(mTag);

            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                ft.detach(mFragment);
                ft.commit();
            }
    	}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
                mFragment = (SherlockFragment) Fragment.instantiate(mActivity, mClass.getName(), mArgs);
        		mFragment.setArguments(projectAndPlanName);
                ft.add(android.R.id.content, mFragment, tab.getTag().toString());
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
                ft.detach(mFragment);
			
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			
		}
    	
    }
    
    //Enabling and Disabling Action Bars
  	@Override public boolean onPrepareOptionsMenu(Menu menu) { 
  		SharedPreferences pref;
		pref = PreferenceManager.getDefaultSharedPreferences(BuildListActivity.this);
		String userType=pref.getString("userType", "");
		if (userType.equals("guest")) {
			menu.getItem(1).setVisible(false);
			menu.getItem(2).setVisible(true);
			}
  		return super.onPrepareOptionsMenu(menu);
  	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.buildlist_menu, menu);
        return true;
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			//Intent intent = new Intent(BuildListActivity.this, ProjectListActivity.class);
			//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//startActivity(intent);
			finish();
			return true;
		} else if (itemId == R.id.logout_menu) {
			myCookieStore.clear();
			editor.putString("firsttime", "notfirst");
			if (pref.getBoolean("CHECK", false) == false) 
			{
				editor.remove("encryptedpass");
			}
			editor.remove("loggedin");
			editor.remove("GuestLogin");
			editor.commit();
			Intent loginIntent = new Intent(BuildListActivity.this, LoginActivity.class);
			startActivity(loginIntent);
			Intent intent3 = new Intent(BuildListActivity.this, NotificationService.class);
			intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			stopService(intent3);
			return true;
		} else if (itemId == R.id.help_menu) {
			AlertDialog.Builder builder = new AlertDialog.Builder(BuildListActivity.this);
			builder.setTitle("Help");
			builder.setMessage("This page contains a list of all the builds sorted by plan on your Bamboo server." + "\n" 
					+ "It shows the last 25 builds for each plan." + "\n"
					+ "Click on the build to see its information."
					+ "To refresh the list, pull down the list as if you were scrolling up" + "\n" 
					+ "The action bar contains links to a summary of builds in graph format, and you can start a new build.");
			builder.setCancelable(true);
			builder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.dismiss();
			           }
			       });
			AlertDialog alert = builder.create();
			alert.show();
			alert.setCanceledOnTouchOutside(true);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	}
}