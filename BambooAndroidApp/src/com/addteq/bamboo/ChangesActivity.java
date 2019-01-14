package com.addteq.bamboo;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.addteq.stix.R;
import com.atlassian.jconnect.droid.Api;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class ChangesActivity extends SherlockActivity {
	String buildkey, reason;
	ListView changesLV;
	ListView commLV;
	ArrayList<Changes> comment_list = new ArrayList<Changes>();
	ChangeListAdapter adapter;
	//ArrayList<String> comm_list = new ArrayList<String>();
	int first=0;
	ArrayList<String> change_list = new ArrayList<String>();
	AsyncHttpClient client = new AsyncHttpClient();
	String URL = "/rest/api/latest/";
	PersistentCookieStore myCookieStore;
	SharedPreferences pref;
	SharedPreferences.Editor editor;
	private static String IPURL = "";
	private static String IPAdd = "";
	ProgressDialog mDialog;
	//private ImageView sendfeedback;
	private String tempJson ="";
	private String tempJson2 = "";
	private Context con;
	int stringLength;
	
	// timeout around 25 seconds. (40 does not mean 40 secs..)
		private int LOGIN_TIMEOUT=40*1000;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_list);
		con = this;
		DisplayMetrics metrics = getResources().getDisplayMetrics();

		stringLength =metrics.widthPixels/18-6;

		UncaughtExceptionHandler mUEHandler = new Thread.UncaughtExceptionHandler() {

	        @Override
	        public void uncaughtException(Thread t, Throwable e) {
	            e.printStackTrace();
	            Api.handleException(e);
	            ChangesActivity.this.finish();
	        }
	    };
	    Thread.setDefaultUncaughtExceptionHandler(mUEHandler);
		changesLV = (ListView) findViewById(R.id.changesLV);
		changesLV.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				if(!change_list.get(0).equalsIgnoreCase("No changed files")){
					AlertDialog.Builder builder = new AlertDialog.Builder(con);
					builder.setTitle("Changed Files").setMessage(change_list.get(position))
						.setNegativeButton("Close", null);
					AlertDialog dialog = builder.create();
					dialog.show();
				}
			}
		});
		commLV = (ListView) findViewById(R.id.commLV);
		commLV.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				String content = comment_list.get(position).getMessage();
				if(!content.equalsIgnoreCase("No commit message")){
					String author = comment_list.get(position).getAuthor();
					if(!author.equalsIgnoreCase("")){
						author = "\nby "+author;
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(con);
					builder.setTitle("Commit Messages").setMessage(content+author)
						.setNegativeButton("Close", null);
					AlertDialog dialog = builder.create();
					dialog.show();
				}
			}
			
		});
		//sendfeedback = (ImageView) findViewById(R.id.feedback);
		/*sendfeedback.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(Api.createFeedbackIntent(ChangesActivity.this));
			}
		});*/
		myCookieStore = new PersistentCookieStore(ChangesActivity.this);
		myCookieStore.getCookies();
		client.setCookieStore(myCookieStore);
		client.addHeader("X-Atlassian-Token", "nocheck");
		pref = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		editor = pref.edit();
		IPAdd = pref.getString("IPADDRESS", IPAdd);
		if(IPAdd.endsWith("/")){
			IPURL = IPAdd + "rest/api/latest/";
		}else{
			IPURL = IPAdd + URL;
		}
		Intent intent = getIntent();
		buildkey = intent.getStringExtra("key");
		reason = intent.getStringExtra("reason");
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setTitle(buildkey);
		adapter = new ChangeListAdapter(ChangesActivity.this, comment_list, stringLength);
		getChangeset();
	}

	public void parseCCommentJSON(String json) {
		JSONObject tempJsonObject = null;
		try {
			tempJsonObject = new JSONObject(json);
			JSONObject cObject = tempJsonObject.getJSONObject("changes");
			String size = cObject.getString("size");
			int sizeInt = Integer.parseInt(size);
			JSONArray cArray = cObject.getJSONArray("change");
			if(sizeInt == 0){
				Changes change = new Changes();
				String message = "No commit message";
				if(reason.contains("Manual")){
					message = reason;
				}
				change.setMessage(message);
				change.setAuthor("");
				comment_list.add(change);
			}
			for (int i = 0; i < sizeInt; i++) {
				Changes change = new Changes();
				JSONObject tempObject = cArray.getJSONObject(i);
				String message = tempObject.getString("comment");
				String username = "";
				try {
					username = tempObject.getString("userName");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				change.setMessage(message);
				if(username.equals("")){
					username = reason;
					change.setAuthor(username);
				} else {
					change.setAuthor(username);
				}
				comment_list.add(change);
			}
			adapter = new ChangeListAdapter(ChangesActivity.this, comment_list, stringLength);
			commLV.setAdapter(adapter);
			mDialog.dismiss();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void parseChangesJSON(String json) {
		JSONObject tempJsonObject = null;
		try {
			tempJsonObject = new JSONObject(json);
			JSONObject cObject = tempJsonObject.getJSONObject("changes");
			String size = cObject.getString("size");
			int sizeInt = Integer.parseInt(size);
			JSONArray cArray = cObject.getJSONArray("change");
			for (int i = 0; i < sizeInt; i++) {
				JSONObject tempObject = cArray.getJSONObject(i);
				JSONObject fObject = tempObject.getJSONObject("files");
				String fsize = fObject.getString("size");
				int fsizeInt = Integer.parseInt(fsize);
				if (fsizeInt != 0) {
					JSONArray fArray = fObject.getJSONArray("file");
					for (int i1 = 0; i1 < fsizeInt; i1++) {
						JSONObject file = fArray.getJSONObject(i1);
						String filename = file.getString("name");
						change_list.add(filename);
					}
				}
			}
			if (change_list.size() > 0) {
				ArrayList<String> short_change_list = new ArrayList<String>();
				for(String s : change_list){
					if(s.length() > stringLength){
					short_change_list.add(s.substring(0, stringLength)+"...");
					}else{
						short_change_list.add(s);
					}
				}
				changesLV.setAdapter(new ArrayAdapter<String>(this,
						R.layout.change_files,R.id.listTextView, short_change_list));
				mDialog.dismiss();
			} else {
				String noChange = "No changed files";
				change_list.add(noChange);
				changesLV.setAdapter(new ArrayAdapter<String>(this,
						R.layout.change_files,R.id.listTextView, change_list));
				mDialog.dismiss();
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.changes_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			ChangesActivity.this.finish();
			return true;
		} else if (itemId == R.id.help_menu) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					ChangesActivity.this);
			builder.setTitle("Help");
			builder.setMessage("Shows recent file changes for build. If build is unsuccessful, may not show changed files.");
			builder.setCancelable(true);
			builder.setPositiveButton("Dismiss",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
			alert.setCanceledOnTouchOutside(true);
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
			Intent loginIntent = new Intent(ChangesActivity.this,
					LoginActivity.class);
			startActivity(loginIntent);
			Intent intent3 = new Intent(ChangesActivity.this,
					NotificationService.class);
			intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			stopService(intent3);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}

	}
	
	private boolean isNetworkOnline() {
		boolean status = false;
		try {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getNetworkInfo(0);
			if (netInfo != null
					&& netInfo.getState() == NetworkInfo.State.CONNECTED) {
				status = true;
			} else {
				netInfo = cm.getNetworkInfo(1);
				if (netInfo != null
						&& netInfo.getState() == NetworkInfo.State.CONNECTED)
					status = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return status;

	}
	
	private void getChangeset(){
		if(isNetworkOnline()){
			mDialog = ProgressDialog.show(ChangesActivity.this, "", "Data is Loading...");
			// http://localhost:8085/rest/api/latest/result/OTP-ADDTEQOTP-8.json?expand=changes.change.comment
			String chngURL = IPURL + "result/" + buildkey + ".json?expand=changes.change.comment";
			client.get(chngURL, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String response) {
					if (response != null) {
						// parse JSON response
						tempJson = response;
						parseCCommentJSON(response);
						String serverURL = IPURL + "result/" + buildkey
								+ ".json?expand=changes.change.files";
						client.get(serverURL, new AsyncHttpResponseHandler() {
							@Override
							public void onSuccess(String response) {
								if (response != null) {
									// parse JSON response
									tempJson2 = response;
									parseChangesJSON(response);
								}
							}
							public void onFailure(Throwable e, String response) {
								try{
									e.printStackTrace();
									if(mDialog.isShowing())
									{
										try {
											mDialog.dismiss();
										} catch (Exception dialogException) {
										}
									}
									AlertDialog.Builder alt_bld = new AlertDialog.Builder(ChangesActivity.this);
									alt_bld.setTitle("Error");
									alt_bld.setMessage("Server Error or Unavailable, Please logout!");
									alt_bld.setCancelable(false);
									alt_bld.setNegativeButton("Logout", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											myCookieStore.clear();
											editor.remove("loggedin");
											editor.remove("GuestLogin");
											editor.commit();
											Intent loginIntent = new Intent(ChangesActivity.this, LoginActivity.class);
											startActivity(loginIntent);
											Intent intent3 = new Intent(ChangesActivity.this, NotificationService.class);
											intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
											stopService(intent3);
										}
									});
									alt_bld.setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
									alt_bld.show();
								}
								catch(Exception FailureException){
									FailureException.printStackTrace();
								}
							}
						});
					}
				}
				public void onFailure(Throwable e, String response) {
					try{
						e.printStackTrace();
						
						if(e.toString().contains("HttpResponseException") && e.toString().contains("Unauthorized")){
							loggingIn();
						}else{
						if(mDialog.isShowing())
						{
							try {
								mDialog.dismiss();
							} catch (Exception dialogException) {
							}
						}
						AlertDialog.Builder alt_bld = new AlertDialog.Builder(ChangesActivity.this);
						alt_bld.setTitle("Error");
						alt_bld.setMessage("Server Error or Unavailable, Please logout!");
						alt_bld.setCancelable(false);
						alt_bld.setNegativeButton("Logout", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								myCookieStore.clear();
								Intent loginIntent = new Intent(ChangesActivity.this, LoginActivity.class);
								startActivity(loginIntent);
								Intent intent3 = new Intent(ChangesActivity.this, NotificationService.class);
								intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								ChangesActivity.this.stopService(intent3);
							}
						});
						alt_bld.setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
						alt_bld.show();
						}
					}
					catch(Exception FailureException){
						FailureException.printStackTrace();
					}
				}
			});
			
		}
		else{
			Toast.makeText(ChangesActivity.this,
					"No Internet Connection",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		try{
			setContentView(R.layout.change_list);
			changesLV = (ListView) findViewById(R.id.changesLV);
			commLV = (ListView) findViewById(R.id.commLV);
			//sendfeedback = (ImageButton) findViewById(R.id.feedback);
			/*sendfeedback.setOnClickListener(new OnClickListener() {
			
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					startActivity(Api.createFeedbackIntent(ChangesActivity.this));
				}
			});*/
			ActionBar ab = getSupportActionBar();
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setTitle(buildkey);
			adapter.clear();
			if(!tempJson.equalsIgnoreCase("")){
				parseCCommentJSON(tempJson);
			}
			if(!tempJson2.equalsIgnoreCase("")){
				parseChangesJSON(tempJson2);
			}
		}catch(Exception error){
			error.printStackTrace();
		}
	}
	
	public void loggingIn(){
		AsyncHttpClient myClient = new AsyncHttpClient();
		myClient.setCookieStore(myCookieStore);
				
		String en_saved = pref.getString("encryptedid", "");
		String en_pass = pref.getString("encryptedpass", "");
		String username = null;
		String password = null; 
		try {
			username = SimpleCrypto.decrypt(pref.getString("encryptKey", "@ddt3q"), en_saved);
			password = SimpleCrypto.decrypt(pref.getString("encryptKey", "@ddt3q"), en_pass);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
		myClient.setBasicAuth(username, password);
		String BAMBOOIPADD = pref.getString("IPADDRESS", "No Value");
		String license = "";
		if (BAMBOOIPADD.endsWith("/")) {
			license = BAMBOOIPADD
					+ "rest/addteqrest/1.0/check.json?os_authType=basic";
		} else {
			license = BAMBOOIPADD
					+ "/rest/addteqrest/1.0/check.json?os_authType=basic";
		}
		String licenseLogin = license;
		myClient.setTimeout(LOGIN_TIMEOUT);
		myClient.get(licenseLogin, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				Log.e("at beginning of onSuccess of Login", "I am doing testing "+response);

				if (response.contains("check-code") && response.contains("success")) {
					
					Log.d("Login", "Success");
					retryGetChangeset();
				} else {
					Log.d("Login", "Fail");
					if(mDialog.isShowing())
					{
						try {
							mDialog.dismiss();
						} catch (Exception dialogException) {
						}
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(
								ChangesActivity.this);
					builder.setTitle("Error");
					builder.setMessage("Cannot connect to the server.");
					builder.setCancelable(true);
					builder.setNeutralButton("Dismiss",
							new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {
								dialog.dismiss();
							}
						});
					AlertDialog alert = builder.create();
					alert.show();
				}
			}

			@Override
			public void onFailure(Throwable e, String response) {
//						pd.dismiss();
				Log.e("Throwable e   ", e.toString());
				if(mDialog.isShowing())
				{
					try {
						mDialog.dismiss();
					} catch (Exception dialogException) {
					}
				}
				if(e.toString().contains("SocketTimeoutException") || e.toString().contains("ConnectException")){
					AlertDialog.Builder builder = new AlertDialog.Builder(
							ChangesActivity.this);
					builder.setTitle("Error");
					builder.setMessage("Request Timed out. Please try again.");
					builder.setCancelable(true);
					builder.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							mDialog = ProgressDialog.show(ChangesActivity.this, "", "Data is Loading...");
							loggingIn();
						}
					});
					
					builder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});

					AlertDialog alert = builder.create();
					alert.show();
					
				}else{ 
					AlertDialog.Builder builder = new AlertDialog.Builder(
							ChangesActivity.this);
					builder.setTitle("Error");
					builder.setMessage("Cannot connect to the server.");
					builder.setCancelable(true);
					builder.setNeutralButton("Dismiss",
							new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {
								dialog.dismiss();
							}
						});
					AlertDialog alert = builder.create();
					alert.show();
						
				}
			}
		});// end of get
	}
	
	private void retryGetChangeset(){
		if(isNetworkOnline()){
			//mDialog = ProgressDialog.show(ChangesActivity.this, "", "Data is Loading...");
			// http://localhost:8085/rest/api/latest/result/OTP-ADDTEQOTP-8.json?expand=changes.change.comment
			String chngURL = IPURL + "result/" + buildkey + ".json?expand=changes.change.comment";
			client.get(chngURL, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String response) {
					if (response != null) {
						// parse JSON response
						tempJson = response;
						parseCCommentJSON(response);
						String serverURL = IPURL + "result/" + buildkey
								+ ".json?expand=changes.change.files";
						client.get(serverURL, new AsyncHttpResponseHandler() {
							@Override
							public void onSuccess(String response) {
								if (response != null) {
									// parse JSON response
									tempJson2 = response;
									parseChangesJSON(response);
								}
							}
							public void onFailure(Throwable e, String response) {
								try{
									e.printStackTrace();
									if(mDialog.isShowing())
									{
										try {
											mDialog.dismiss();
										} catch (Exception dialogException) {
										}
									}
									AlertDialog.Builder alt_bld = new AlertDialog.Builder(ChangesActivity.this);
									alt_bld.setTitle("Error");
									alt_bld.setMessage("Server Error or Unavailable, Please logout!");
									alt_bld.setCancelable(false);
									alt_bld.setNegativeButton("Logout", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											myCookieStore.clear();
											Intent loginIntent = new Intent(ChangesActivity.this, LoginActivity.class);
											startActivity(loginIntent);
											Intent intent3 = new Intent(ChangesActivity.this, NotificationService.class);
											intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
											stopService(intent3);
										}
									});
									alt_bld.setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
									alt_bld.show();
								}
								catch(Exception FailureException){
									FailureException.printStackTrace();
								}
							}
						});
					}
				}
				public void onFailure(Throwable e, String response) {
					try{
						e.printStackTrace();
						
						
						if(mDialog.isShowing())
						{
							try {
								mDialog.dismiss();
							} catch (Exception dialogException) {
							}
						}
						AlertDialog.Builder alt_bld = new AlertDialog.Builder(ChangesActivity.this);
						alt_bld.setTitle("Error");
						alt_bld.setMessage("Server Error or Unavailable, Please logout!");
						alt_bld.setCancelable(false);
						alt_bld.setNegativeButton("Logout", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								myCookieStore.clear();
								Intent loginIntent = new Intent(ChangesActivity.this, LoginActivity.class);
								startActivity(loginIntent);
								Intent intent3 = new Intent(ChangesActivity.this, NotificationService.class);
								intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								ChangesActivity.this.stopService(intent3);
							}
						});
						alt_bld.setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
						alt_bld.show();
					}
					catch(Exception FailureException){
						FailureException.printStackTrace();
					}
				}
			});
			
		}
		else{
			Toast.makeText(ChangesActivity.this,
					"No Internet Connection",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	
}
