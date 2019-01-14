package com.addteq.bamboo;

import java.io.UnsupportedEncodingException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
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
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

public class CommentListActivity extends SherlockActivity {
	Context con;
	ListView commentLV;
	ArrayList<Comment> comments;
	AsyncHttpClient client = new AsyncHttpClient();
	String URL = "/rest/api/latest/result/PROJECTKEY-PLANKEY-BUILDNUMBER/comment.json?expand=comments.comment";
	String postURL = "/rest/api/latest/result/PROJECTKEY-PLANKEY-BUILDNUMBER/comment";
	String key;
	TextView commentNumTV;
	CommentListAdapter adapter;
	Button addBtn;
	PersistentCookieStore myCookieStore;
	private static String IPPOST = "";
	private static String IPURL = "";
	private static String IPAdd = "";
	SharedPreferences pref;
	SharedPreferences.Editor editor;
	String username="";
	ProgressDialog mDialog;
	//private ImageView sendfeedback;
	//private String tempjson = "";
	private EditText et;
	// http://bamboo.addteq.com:8085/rest/api/latest/result/BAKEY-BAPLANKEY-123/comment.json?expand=comments.comment
	private String contentType="";
	private StringEntity data = null;
	private int LOGIN_TIMEOUT=40*1000;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comment_list);
		con = this;
		
		setupUI((View) findViewById(R.id.parent));
		
//		UncaughtExceptionHandler mUEHandler = new Thread.UncaughtExceptionHandler() {
//
//	        @Override
//	        public void uncaughtException(Thread t, Throwable e) {
//	            e.printStackTrace();
//	            Api.handleException(e);
//	            CommentListActivity.this.finish();
//	        }
//	    };
//	    Thread.setDefaultUncaughtExceptionHandler(mUEHandler);
		Intent intent = getIntent();
		key = intent.getStringExtra("key");
		commentLV = (ListView) findViewById(R.id.commentLV);
		commentLV.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		commentLV.setStackFromBottom(true);
		commentLV.setDivider(null);
		commentNumTV = (TextView) findViewById(R.id.commentNumTV);
		addBtn = (Button) findViewById(R.id.addBtn);
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayUseLogoEnabled(false);
		actionBar.setTitle(key);
		
		myCookieStore = new PersistentCookieStore(this);
		myCookieStore.getCookies();
		client.setCookieStore(myCookieStore);
		pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		editor = pref.edit();
		IPAdd = pref.getString("IPADDRESS", IPAdd);
		
		if(IPAdd.endsWith("/")){ 
			IPPOST = IPAdd + "rest/api/latest/result/PROJECTKEY-PLANKEY-BUILDNUMBER/comment";
			IPURL = IPAdd + "rest/api/latest/result/PROJECTKEY-PLANKEY-BUILDNUMBER/comment.json?expand=comments.comment";
		}else{
			IPPOST = IPAdd + postURL;
			IPURL = IPAdd + URL;
		}

		String en_saved = pref.getString("encryptedid", "");
		String en_pass = pref.getString("encryptedpass", "");
		try {
			username = SimpleCrypto.decrypt(pref.getString("encryptKey", "@ddt3q"), en_saved);
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//String[] keys = key.split("-"); 
		//actionBar.setTitle("Build #"+keys[keys.length-1]);
		IPURL = IPURL.replace("PROJECTKEY-PLANKEY-BUILDNUMBER", key);
		IPPOST = IPPOST.replace("PROJECTKEY-PLANKEY-BUILDNUMBER", key);
		// String key = planKey + "-" +
		//08-15 19:41:10.193: V/TAG(653): planKey BAKEY-BAPLANKEY
		//08-15 19:43:58.773: V/TAG(756): BAKEY-BAPLANKEY-204
		
		getComment();
		
		et = (EditText) CommentListActivity.this
				.findViewById(R.id.commentET);
		
		addBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				EditText et = (EditText) CommentListActivity.this
						.findViewById(R.id.commentET);
				String commentStr = et.getText().toString();
				if (commentStr.isEmpty() || commentStr == null) {
					Toast.makeText(getApplicationContext(),
							"Please enter your comment.", Toast.LENGTH_LONG)
							.show();
					return;
				}

				String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>"
						+ "<comment author=\"whoever\">"
						+ "<content>"
						+ commentStr + "</content></comment>";
				contentType = "application/xml";
				
				try {
					data = new StringEntity(xmlStr, "UTF-8");
					postRequest(contentType, data);	
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
	}
	
	public void setupUI(View view){
		if(!(view instanceof EditText)){
			view.setOnTouchListener(new OnTouchListener(){
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					// TODO Auto-generated method stub
					hideSoftKeyboard();
					return false;
				}
			});
		}
		if(view instanceof ViewGroup){
			for(int i=0; i<((ViewGroup)view).getChildCount(); i++){
				View inner = ((ViewGroup)view).getChildAt(i);
				setupUI(inner);
			}
		}
	}
	
	public void hideSoftKeyboard(){
		InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
	}


	private void parseCommentJSON(String jStr) {
		try {
			JSONObject json = new JSONObject(jStr);
			JSONObject jComments = json.getJSONObject("comments");
			JSONArray jComment = jComments.getJSONArray("comment");
			comments = new ArrayList<Comment>();
			if (jComment.length() == 0) {
				commentNumTV.setText("No comments");
			} else {
				commentNumTV.setText("Total comments: " + jComment.length());
			}
			for (int i = 0; i < jComment.length(); i++) {
				String author = jComment.getJSONObject(i).getString("author");
				String content = jComment.getJSONObject(i).getString("content");
				String creationDate = jComment.getJSONObject(i).getString(
						"creationDate");
				Comment comment = new Comment();
				comment.setAuthor(author);
				comment.setContent(content);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
				Date date = (Date)sdf.parse(creationDate);
				SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy, HH:mm", Locale.getDefault());
				String newDate = formatter.format(date);
				comment.setCreationDate(newDate);
				comments.add(comment);
			}
			adapter = new CommentListAdapter(con, comments);
			commentLV.setAdapter(adapter);
			commentLV.setSelection(comments.size()-1);
		} catch (JSONException e) {
			e.printStackTrace();
		} 
			catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		client = null;
		// myClient
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.comment_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId(); {
		if(itemId == android.R.id.home) {
			CommentListActivity.this.finish();
			return true;
			}
		else if (itemId == R.id.logout_menu) {
			myCookieStore.clear();
			editor.remove("loggedin");
			editor.remove("GuestLogin");
			editor.commit();
			Intent intent2 = new Intent(CommentListActivity.this, LoginActivity.class);
			startActivity(intent2);
			Intent intent3 = new Intent(CommentListActivity.this, NotificationService.class);
			intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			stopService(intent3);
			return true;
		}
		else if (itemId == R.id.help_menu) {
			AlertDialog.Builder builder = new AlertDialog.Builder(CommentListActivity.this);
			builder.setTitle("Help");
			builder.setMessage("This page contains a list of all the comments of a particular build." + "\n" 
					+ "It shows a number of the comments." + "\n"
					+ "\n\n"
					+ "You can add your comment by enter the text and click the Send button.");
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
		}
		else if (itemId == R.id.feedback_menu) {
			startActivity(Api.createFeedbackIntent(CommentListActivity.this));
		}
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
	
	private void postRequest(final String contentType, final StringEntity data){
		try{
			if(isNetworkOnline()){
				client.post(CommentListActivity.this, IPPOST, data,
						contentType, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String response) {
								client.get(IPURL,
										new AsyncHttpResponseHandler() {
											@Override
											public void onSuccess(
													String response) {
													parseCommentJSON(response);
													EditText et = (EditText) CommentListActivity.this
															.findViewById(R.id.commentET);
													et.setText("");
											}
										});
						}

						@Override
						public void onFailure(Throwable t, String err) {
							// TODO Auto-generated method stub
							super.onFailure(t, err);
						}

					});
			}
			else{
				Toast.makeText(CommentListActivity.this,
						"No Internet Connection",
						Toast.LENGTH_SHORT).show();
			}
		}
		catch(Exception error){
			error.printStackTrace();
		}
	}
	
	private void getComment(){
		try{
			if(isNetworkOnline()){
				mDialog = ProgressDialog.show(CommentListActivity.this, "", "Data is Loading...");
				client.get(IPURL, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						if (response != null) {
							// parse JSON response
							parseCommentJSON(response);
							//tempjson = response;
							if(mDialog.isShowing())
							{
								try {
									mDialog.dismiss();
								} catch (Exception dialogException) {
								}
							}
						}
					}
					public void onFailure(Throwable e, String response) {
						try{
							e.printStackTrace();
							/*
							try{
								Log.d("Failure", response);
							}
							catch(Exception logException){
								logException.printStackTrace();
							}
							*/
							if(e.toString().contains("HttpResponseException") && e.toString().contains("Unauthorized")){
								loggingIn(0);
							}else{
								if(mDialog.isShowing())
								{
									try {
										mDialog.dismiss();
									} catch (Exception dialogException) {
									}
								}
								AlertDialog.Builder alt_bld = new AlertDialog.Builder(CommentListActivity.this);
								alt_bld.setTitle("Error");
								alt_bld.setMessage("Server Error or Unavailable, Please logout!");
								alt_bld.setCancelable(false);
								alt_bld.setNeutralButton("Logout", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										myCookieStore.clear();
										editor.remove("loggedin");
										editor.remove("GuestLogin");
										editor.commit();
										Intent loginIntent = new Intent(CommentListActivity.this, LoginActivity.class);
										startActivity(loginIntent);
										Intent intent3 = new Intent(CommentListActivity.this, NotificationService.class);
										intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										CommentListActivity.this.stopService(intent3);
									}
								});
								alt_bld.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.cancel();
									}
								});
								alt_bld.show();
							}
						}
						catch(Exception error){
							error.printStackTrace();
						}	
					}	
				});
			}
			else{
				Toast.makeText(CommentListActivity.this,
						"No Internet Connection",
						Toast.LENGTH_SHORT).show();
			}
		}
		catch(Exception error){
			error.printStackTrace();
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		try{
			String temp = et.getText().toString();
			setContentView(R.layout.comment_list);
			commentLV = (ListView) findViewById(R.id.commentLV);
			commentNumTV = (TextView) findViewById(R.id.commentNumTV);
			addBtn = (Button) findViewById(R.id.addBtn);
			et = (EditText) CommentListActivity.this
					.findViewById(R.id.commentET);
			
			ActionBar actionBar = getSupportActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			
			actionBar.setDisplayShowTitleEnabled(true);
			actionBar.setDisplayUseLogoEnabled(false);
			actionBar.setTitle(key);
			//sendfeedback = (ImageButton) findViewById(R.id.feedback);
			/*sendfeedback.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					startActivity(Api.createFeedbackIntent(CommentListActivity.this));
				}
			});*/
			
//			parseCommentJSON(tempjson);
			commentLV.setAdapter(adapter);
			
			EditText et = (EditText) CommentListActivity.this
					.findViewById(R.id.commentET);
			et.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
			et.setText(temp);
			
			addBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					EditText et = (EditText) CommentListActivity.this
							.findViewById(R.id.commentET);
					String commentStr = et.getText().toString();
					if (commentStr.isEmpty() || commentStr == null) {
						Toast.makeText(getApplicationContext(),
								"Please enter your comment.", Toast.LENGTH_LONG)
								.show();
						return;
					}

					String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>"
							+ "<comment author=\"whoever\">"
							+ "<content>"
							+ commentStr + "</content></comment>";
					contentType = "application/xml";
					try {
						data = new StringEntity(xmlStr, "UTF-8");
						postRequest(contentType, data);	
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			});
		}
		catch(Exception error){
			error.printStackTrace();
		}
	}
	
	protected void loggingIn(final int type){
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
					if(type==0){
						retryGetComment();
					}else{
						retrySendComment();
					}
				} else {
					Log.d("Login", "Fail");
					
					if(type==0){
						if(mDialog.isShowing())
						{
							try {
								mDialog.dismiss();
							} catch (Exception dialogException) {
							}
						}
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(
								CommentListActivity.this);
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
				if(type==0){
				if(mDialog.isShowing())
				{
					try {
						mDialog.dismiss();
					} catch (Exception dialogException) {
					}
				}
				}
				if(e.toString().contains("SocketTimeoutException") || e.toString().contains("ConnectException")){
					AlertDialog.Builder builder = new AlertDialog.Builder(
							CommentListActivity.this);
					builder.setTitle("Error");
					builder.setMessage("Request Timed out. Please try again.");
					builder.setCancelable(true);
					builder.setNeutralButton("Retry",
							new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {
								if(type==0){
									mDialog = ProgressDialog.show(CommentListActivity.this, "", "Data is Loading...");
								}
								loggingIn(type);
							}
						});
					builder.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});

					AlertDialog alert = builder.create();
					alert.show();
					
				}else{ 
					AlertDialog.Builder builder = new AlertDialog.Builder(
							CommentListActivity.this);
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
	
	protected void retryGetComment(){
		try{
			if(isNetworkOnline()){
				//mDialog = ProgressDialog.show(CommentListActivity.this, "", "Data is Loading...");
				client.get(IPURL, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						if (response != null) {
							// parse JSON response
							parseCommentJSON(response);
							//tempjson = response;
							if(mDialog.isShowing())
							{
								try {
									mDialog.dismiss();
								} catch (Exception dialogException) {
								}
							}
						}
					}
					public void onFailure(Throwable e, String response) {
						try{
							e.printStackTrace();
							/*
							try{
								Log.d("Failure", response);
							}
							catch(Exception logException){
								logException.printStackTrace();
							}
							*/
							if(e.toString().contains("HttpResponseException") && e.toString().contains("Unauthorized")){
								loggingIn(0);
							}else{
								if(mDialog.isShowing())
								{
									try {
										mDialog.dismiss();
									} catch (Exception dialogException) {
									}
								}
								AlertDialog.Builder alt_bld = new AlertDialog.Builder(CommentListActivity.this);
								alt_bld.setTitle("Error");
								alt_bld.setMessage("Server Error or Unavailable, Please logout!");
								alt_bld.setCancelable(false);
								alt_bld.setNeutralButton("Logout", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										myCookieStore.clear();
										editor.remove("loggedin");
										editor.remove("GuestLogin");
										editor.commit();
										Intent loginIntent = new Intent(CommentListActivity.this, LoginActivity.class);
										startActivity(loginIntent);
										Intent intent3 = new Intent(CommentListActivity.this, NotificationService.class);
										intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										CommentListActivity.this.stopService(intent3);
									}
								});
								alt_bld.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.cancel();
									}
								});
								alt_bld.show();
							}
						}
						catch(Exception error){
							error.printStackTrace();
						}	
					}	
				});
			}
			else{
				Toast.makeText(CommentListActivity.this,
						"No Internet Connection",
						Toast.LENGTH_SHORT).show();
			}
		}
		catch(Exception error){
			error.printStackTrace();
		}
	}
	protected void retrySendComment(){
		try{
			if(isNetworkOnline()){
				client.post(CommentListActivity.this, IPPOST, data,
						contentType, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String response) {
								client.get(IPURL,
										new AsyncHttpResponseHandler() {
											@Override
											public void onSuccess(
													String response) {
													parseCommentJSON(response);
													EditText et = (EditText) CommentListActivity.this
															.findViewById(R.id.commentET);
													et.setText("");
											}
										});
						}

						@Override
						public void onFailure(Throwable t, String err) {
							// TODO Auto-generated method stub
							super.onFailure(t, err);
						}

					});
			}
			else{
				Toast.makeText(CommentListActivity.this,
						"No Internet Connection",
						Toast.LENGTH_SHORT).show();
			}
		}
		catch(Exception error){
			error.printStackTrace();
		}
	}
}