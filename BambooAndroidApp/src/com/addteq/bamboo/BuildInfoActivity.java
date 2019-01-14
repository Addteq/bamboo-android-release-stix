package com.addteq.bamboo;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.addteq.bamboo.BuildListFragment.MySSLSocketFactory;
import com.addteq.stix.R;
import com.atlassian.jconnect.droid.Api;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;

public class BuildInfoActivity extends SherlockActivity {
	TextView revision, triggerVal, completed, duration, successSince, build;
	TextView artifact;
	LinearLayout banner;
	ListView artList;
	View ribbon1, ribbon2;
	ImageView stateImg;
	//private ImageView sendfeedback;
	PersistentCookieStore pcs;
	private DownloadManager dm;

	String projectName = "";
	String planName = "";
	ArrayList<String> triggers;
	ArrayList<String> completed_list;
	ArrayList<String> duration_list;
	ArrayList<String> relative_list;
	ArrayList<String> revision_list;
	ArrayList<String> key_list;
	ArrayList<Integer> state_list;
	String planKey;
	String buildItem;
	int posi;
	String[] html;
	String artname;
	boolean isDownloadable;
	ArrayList<String> art_list;
	private PersistentCookieStore myCookieStore;
	private static AsyncHttpClient client = new AsyncHttpClient();
	// private String key_pos;
	private static String IPPOST = "";
	private static String IPAdd = "";
	private static String POST_URL = "/rest/api/latest/queue/";
	private static String BAMBOOIPADD = "";
	SharedPreferences pref;
	SharedPreferences.Editor editor;
	int pos;
	private ViewPager pager;
	private String artifactURL = null;
	String user = null;
	String pass = null;
	
	private static String TAG = "BuildInfo";
	private int LOGIN_TIMEOUT = 40 * 1000;
	private long enqueue;


	Date now;
	Calendar cal;
	Date yestarday;

	@Override
	public void onCreate(Bundle bundle) {

		super.onCreate(bundle);
		setContentView(R.layout.pager);
		UncaughtExceptionHandler mUEHandler = new Thread.UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				e.printStackTrace();
				Api.handleException(e);
				BuildInfoActivity.this.finish();
			}
		};
		
		
		Thread.setDefaultUncaughtExceptionHandler(mUEHandler);
	
		pref = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		editor = pref.edit();
		IPAdd = pref.getString("IPADDRESS", IPAdd);
		if (IPAdd.endsWith("/")) {
			IPPOST = IPAdd + "rest/api/latest/queue/";
		} else {
			IPPOST = IPAdd + POST_URL;
		}
		
		myCookieStore = new PersistentCookieStore(BuildInfoActivity.this);
		myCookieStore.getCookies();
		client.setCookieStore(myCookieStore);
		client.addHeader("X-Atlassian-Token", "nocheck");
		
		Intent incomingIntent = getIntent();

		// Change here
		// These two strings are used for storing the project name and plan name
		// obtained from intent.
		projectName = incomingIntent.getStringExtra("projectName");
		planName = incomingIntent.getStringExtra("planName");

		triggers = incomingIntent.getStringArrayListExtra("trigger");
		buildItem = incomingIntent.getStringExtra("build_no");
		key_list = incomingIntent.getStringArrayListExtra("build_keys");

		art_list = incomingIntent.getStringArrayListExtra("artlinklist");
		completed_list = incomingIntent.getStringArrayListExtra("completed_times");
		duration_list = incomingIntent.getStringArrayListExtra("durations");
		relative_list = incomingIntent.getStringArrayListExtra("relative_times");
		revision_list = incomingIntent.getStringArrayListExtra("revisions");
		state_list = incomingIntent.getIntegerArrayListExtra("status");
		planKey = incomingIntent.getStringExtra("plankey");

		pos = 0;
		int position = incomingIntent.getIntExtra("position", 0);

		isDownloadable = false;
		
	/*	if(state_list.get(position)==0){
			//sendfeedback.setImageResource(R.drawable.bug_small);
			Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
		}
		else if(state_list.get(position)==1){
		//	sendfeedback.setImageResource(R.drawable.bug_red);
			Toast.makeText(getApplicationContext(), "fail", Toast.LENGTH_SHORT).show();

		}
		else {
			//sendfeedback.setImageResource(R.drawable.bug_yellow);
		}*/
		for (int i = 0; i < key_list.size(); i++) {
			if (key_list.get(i).equals(buildItem)) {
				pos = i;
			}
		}

		BuildPagerAdapter bpa = new BuildPagerAdapter();
		pager = (ViewPager) findViewById(R.id.pagerView);
		pager.setAdapter(bpa);
		pager.setCurrentItem(pos);
		// key_pos = key_list.get(position);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		// changed here
		// show the project name and plan name
		actionBar.setTitle(projectName);
		actionBar.setSubtitle(planName);

	}
	
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
						BuildInfoActivity.this);
				
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
						
									//changeIpDialog.setCanceledOnTouchOutside(false);
									//changeIpDialog.show();
									
									myCookieStore.clear();
									editor.putString("firsttime", "expire");
									if (pref.getBoolean("CHECK", false) == false) {
										editor.remove("encryptedpass");
									}
									editor.remove("loggedin");
									editor.remove("GuestLogin");
						            //editor.remove("SSLCHECK");
									editor.commit();
									BuildInfoActivity.this.finish();
									Intent intent3 = new Intent(BuildInfoActivity.this,
											NotificationService.class);
									intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									stopService(intent3);
									Intent goToLoginIntent = new Intent(BuildInfoActivity.this,
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

	// Enabling and Disabling Action Bars
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		SharedPreferences pref;
		pref = PreferenceManager
				.getDefaultSharedPreferences(BuildInfoActivity.this);
		String userType = pref.getString("userType", "");
		if (userType.equals("guest")) {
			menu.getItem(1).setVisible(false);
			menu.getItem(2).setVisible(true);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.buildinfo_menu, menu);
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			BuildInfoActivity.this.finish();
			return true;
		} else if (itemId == R.id.startbuild_menu) {
			AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
			alt_bld.setTitle("Confirm Build:");
			alt_bld.setMessage("Are you sure you want to start " + planKey
					+ "?");
			alt_bld.setCancelable(false);
			alt_bld.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							
							final String serverURL = IPPOST + planKey;
							
							Log.d(TAG,"server url "+IPPOST + planKey);
							
							if (isNetworkOnline()) {
								
								Log.d(TAG,"inside");
								
								
								
								///SSl CHECK - Server returns an https reponse.. And check for a valid certificate. Since the user agreed that he/she takes the risk 
								//we allow users to run the build though it is not secure...
								
								
								URL url = null;
								try {
									url = new URL(serverURL);
								} catch (MalformedURLException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								HttpURLConnection ucon = null;
								
								String SSLCHECK=pref.getString("SSLCHECK", "false");
								
								if(SSLCHECK.contains("true")){

									if (url.getProtocol().toLowerCase().equals("https")) {
								        trustAllHosts();
								        HttpsURLConnection https = null;
										try {
											https = (HttpsURLConnection) url.openConnection();
										} catch (IOException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
								        https.setHostnameVerifier(DO_NOT_VERIFY);
								        ucon = https;
								    } else {
								        try {
											ucon = (HttpURLConnection) url.openConnection();
										} catch (IOException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
								    }
									Log.d("BEFOR RUNNING BUILD - BuildInfoActivity", "TURSTED ALL HOSTS");
									
									
								}
								
									
								client.post(serverURL,
										new AsyncHttpResponseHandler() {
											@Override
											public void onSuccess(
													String response) {
												
												Log.d(TAG,"success");
												
												Toast.makeText(
														BuildInfoActivity.this,
														planKey
																+ " build has been added to build queue",
														Toast.LENGTH_SHORT)
														.show();
											}

											@Override
											public void onFailure(Throwable e, String response) {
												//Log.d(TAG,"this is what failed: " + response);
												//Log.d(TAG,"the error is : " + e.toString());
												if (e.toString().contains("Unauthorized")) {
														AlertDialog.Builder builder = new AlertDialog.Builder(
																BuildInfoActivity.this);
														builder.setTitle("Unauthorized Access.");
														builder.setMessage("You do not have permission to start this build.");
														builder.setIcon(R.drawable.failure_icon);
														builder.setNegativeButton(
																"Dismiss",
																new DialogInterface.OnClickListener() {
																	public void onClick(
																			DialogInterface dialog,
																			int id) {
																		dialog.dismiss();
																	}
																});
														AlertDialog alert = builder
																.create();
														alert.show();
												} else {
													AlertDialog.Builder builder = new AlertDialog.Builder(
															BuildInfoActivity.this);
													builder.setTitle("Error");
													builder.setMessage("Cannot connect to the server.");
													builder.setNegativeButton(
															"Dismiss",
															new DialogInterface.OnClickListener() {
																public void onClick(
																		DialogInterface dialog,
																		int id) {
																	dialog.dismiss();
																}
															});
													AlertDialog alert = builder
															.create();
													alert.show();
												}
											}
										});
								
								Log.d(TAG,"after post request");

							} else {
								Toast.makeText(BuildInfoActivity.this,
										"Internet connection is not available",
										Toast.LENGTH_SHORT).show();
							}
						}
					})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							}).show();
			return true;
		} else if (itemId == R.id.stages_menu) {
			Intent stageIntent = new Intent(BuildInfoActivity.this, StagesActivity.class);
			stageIntent.putExtra("key",
					planKey + "-" + key_list.get(pager.getCurrentItem()));
			stageIntent.putExtra("position", pager.getCurrentItem());
			stageIntent.putExtra("pkey", planKey);
			stageIntent.putExtra("keylist", key_list);
			stageIntent.putExtra("states", state_list);
			startActivity(stageIntent);
			return true;
			
		} else if (itemId == R.id.share_menu) {
			String fileSrc = art_list.get(pager.getCurrentItem());
			Log.d(TAG, "artlist: " +art_list.get(pager.getCurrentItem()));
			Log.d(TAG, "statelist: " +state_list.get(pager.getCurrentItem()));
			String en_saved = pref.getString("encryptedid", "");
			String username = null;
			try {
				username = SimpleCrypto.decrypt(pref.getString("encryptKey", "@ddt3q"), en_saved);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
				 if(art_list.get(pager.getCurrentItem()).contains("artifact") && state_list.get(pager.getCurrentItem()).equals(0)) {
				 Log.d(TAG, "artname " +artname);
				 Intent i=new Intent(android.content.Intent.ACTION_SEND);
					i.setType("text/plain");  
					i.putExtra(android.content.Intent.EXTRA_SUBJECT,"New build bamboo by " +username);   
					i.putExtra(android.content.Intent.EXTRA_TEXT,  Uri.decode("This is a link to the required .apk build file " + "\n" + fileSrc));
					startActivity(Intent.createChooser(i,"Share via"));
			}
//			
			else {
				Toast.makeText(BuildInfoActivity.this,
						"No artifacts available to share!",
						Toast.LENGTH_SHORT).show();
			} 
			return true;
			
		} else if (itemId == R.id.help_menu) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					BuildInfoActivity.this);
			builder.setTitle("Help");
			builder.setMessage("This page contains the detail of the particular built you selected");
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
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	

	private class BuildPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return key_list.size();
		}

		public Object instantiateItem(View collection, final int position) {

			LayoutInflater inflater = (LayoutInflater) collection.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.build_info, null);

			build = (TextView) view.findViewById(R.id.buildNo);
			revision = (TextView) view.findViewById(R.id.revision);
			triggerVal = (TextView) view.findViewById(R.id.trigger);
			completed = (TextView) view.findViewById(R.id.completed);
			duration = (TextView) view.findViewById(R.id.duration);
			successSince = (TextView) view.findViewById(R.id.successSince);
			artifact = (TextView) view.findViewById(R.id.artifact);
			stateImg = (ImageView) view.findViewById(R.id.imageView1);
			banner = (LinearLayout) view.findViewById(R.id.banner);
			//ribbon1 = (View) view.findViewById(R.id.line);
			//ribbon2 = (View) view.findViewById(R.id.line1);
			/*if(state_list.get(position)==0){
				sendfeedback.setImageResource(R.drawable.bug_small);
				stateImg.setImageResource(R.drawable.green_white);
				banner.setBackgroundResource(R.color.dark_green);
				ribbon1.setBackgroundResource(R.color.light_green);
				ribbon2.setBackgroundResource(R.color.light_green);
			}
			else if(state_list.get(position)==1){
				sendfeedback.setImageResource(R.drawable.bug_red);
				stateImg.setImageResource(R.drawable.green_white);
				banner.setBackgroundResource(R.color.dark_green);
				ribbon1.setBackgroundResource(R.color.light_green);
				ribbon2.setBackgroundResource(R.color.light_green);
			}
			else{
				sendfeedback.setImageResource(R.drawable.bug_small);
				stateImg.setImageResource(R.drawable.green_white);
				banner.setBackgroundResource(R.color.dark_green);
				ribbon1.setBackgroundResource(R.color.light_green);
				ribbon2.setBackgroundResource(R.color.light_green);
			}*/
			/*sendfeedback = (ImageView) view.findViewById(R.id.feedback);
			sendfeedback.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					startActivity(Api
							.createFeedbackIntent(BuildInfoActivity.this));
				}
			});*/
			
			

			build.setText("#" + key_list.get(position));
			if (!revision_list.get(position).equals("")) {
				String revise = revision_list.get(position);
				if (revise.length() > 15) {
					revision.setText(revise.substring(0, 15) + "...");
				} else {
					revision.setText(revise);
				}
			} else {
				revision.setText("");
			}
			triggerVal.setText(triggers.get(position));
			completed.setText(completed_list.get(position));
			duration.setText(duration_list.get(position));
			successSince.setText(relative_list.get(position));
			posi = position - 1;
			if (state_list.get(position) == 0) {
				//sendfeedback.setImageResource(R.drawable.bug_green);
				stateImg.setImageResource(R.drawable.green_white);
				 //Resources res = getResources();
				 //Drawable drawable = res.getDrawable(R.drawable.green_white);
				banner.setBackgroundResource(R.drawable.green_back_small);
			//	banner.setBackgroundDrawable(R.drawable.green_white);
			//	banner.setBackgroundResource(R.color.green);
			//	ribbon1.setBackgroundResource(R.color.green);
			//	ribbon2.setBackgroundResource(R.color.green);
				
				int arrList_size=art_list.size();
				
				
				if (art_list.isEmpty()) {
					
					artifact.setText("No Artifacts");
					
				} else if (arrList_size>position && art_list.get(position).equals("0")) {
					
					artifact.setText("No Artifacts");
				} 
				else {
					
					if(arrList_size>position ){
					
					html = (art_list.get(position)).split("/");
					int size = html.length;
					artname = html[size - 2];
					artifact.setText(artname);
					
					}
				}
				
			} else {
				if (state_list.get(position) == 1) {

					//sendfeedback.setImageResource(R.drawable.bug_red);
					stateImg.setImageResource(R.drawable.red_white);
					banner.setBackgroundResource(R.drawable.red_back);
				//	banner.setBackgroundResource(R.color.dark_red);
				//	ribbon1.setBackgroundResource(R.color.dark_red);
				//	ribbon2.setBackgroundResource(R.color.dark_red);
					artifact.setText("No Artifacts");
				} else {
					//sendfeedback.setImageResource(R.drawable.bug_yellow);
					stateImg.setImageResource(R.drawable.yellow_white);
					banner.setBackgroundResource(R.drawable.yellow_back);
				//	banner.setBackgroundResource(R.color.yellow);
				//	ribbon1.setBackgroundResource(R.color.yellow);
				//	ribbon2.setBackgroundResource(R.color.yellow);
					artifact.setText("No Artifacts");
				}
			}

			// Code to download artifact
			if (!artifact.getText().toString().equals("No Artifacts")) {
					SpannableString content = new SpannableString(artname);
					content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
					artifact.setTextColor(android.graphics.Color.BLUE);
					artifact.setText(content);
					artifact.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							AlertDialog.Builder alt_bld = new AlertDialog.Builder(
									BuildInfoActivity.this);
							alt_bld.setTitle("Download " + artname + ":");
							alt_bld.setMessage("This file will be downloaded in your download folder "
									+ ". Are you sure you want to do this?");
							alt_bld.setCancelable(false);
							alt_bld.setPositiveButton("Yes",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											String en_savedid = pref.getString(
													"encryptedid", "");
											String en_savedpass = pref
													.getString("encryptedpass",
															"");

											try {
												if (en_savedid != "") {
													user = SimpleCrypto.decrypt(
															pref.getString(
																	"encryptKey",
																	"@ddt3q"),
															en_savedid);
												}
												if (en_savedpass != "") {
													pass = SimpleCrypto.decrypt(
															pref.getString(
																	"encryptKey",
																	"@ddt3q"),
															en_savedpass);
												}
											} catch (Exception e1) {
												e1.printStackTrace();
											}
										//	 Intent download = new Intent(
										//	 Intent.ACTION_VIEW);
											String url = art_list.get(position);
											Log.d(TAG, "url: " + url
													+ " art_list:" + art_list);
											if (user != null && pass != null) {

												//url += "/bin";
												// url += artname;
												url += "?os_authType=basic&os_username=";
												url += user;
												url += "&os_password=";
												url += pass;
												}
											Log.d(TAG, "The download url is: " + url);
											artifactURL = url;

											// run file download in background
											// thread
											new FileDownload().execute();

//											 download.setData(Uri.parse(url));
//											 startActivity(download);
										}
									})
									.setNegativeButton(
											"No",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int id) {
													dialog.cancel();
												}
											}).show();
						}
					});
			}
			((ViewPager) collection).addView(view, 0);

			return view;
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == ((View) obj);
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView((View) arg2);
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

	public void loggingIn() {
		AsyncHttpClient myClient = new AsyncHttpClient();
		myClient.setCookieStore(myCookieStore);

		String en_saved = pref.getString("encryptedid", "");
		String en_pass = pref.getString("encryptedpass", "");
		String username = null;
		String password = null;
		try {
			username = SimpleCrypto.decrypt(
					pref.getString("encryptKey", "@ddt3q"), en_saved);
			password = SimpleCrypto.decrypt(
					pref.getString("encryptKey", "@ddt3q"), en_pass);
		} catch (Exception e1) {
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
				Log.e("at beginning of onSuccess of Login",
						"I am doing testing " + response);

				if (response.contains("check-code")
						&& response.contains("success")) {

					Log.d("Login", "Success");
					retryPost();
				} else {
					Log.d("Login", "Fail");

					AlertDialog.Builder builder = new AlertDialog.Builder(
							BuildInfoActivity.this);
					builder.setTitle("Error");
					builder.setMessage("Cannot connect to the server.");
					builder.setCancelable(true);
					builder.setNeutralButton("Logout",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									myCookieStore.clear();
									editor.remove("loggedin");
									editor.remove("GuestLogin");
									editor.commit();
									Intent loginIntent = new Intent(
											BuildInfoActivity.this,
											LoginActivity.class);
									startActivity(loginIntent);
									Intent intent3 = new Intent(
											BuildInfoActivity.this,
											NotificationService.class);
									intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									BuildInfoActivity.this.stopService(intent3);
								}
							});
					builder.setNegativeButton("Dismiss",
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
				// pd.dismiss();
				Log.e("Throwable e   ", e.toString());

				if (e.toString().contains("SocketTimeoutException")
						|| e.toString().contains("ConnectException")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							BuildInfoActivity.this);
					builder.setTitle("Error");
					builder.setMessage("Request Timed out. Please try again.");
					builder.setCancelable(true);
					builder.setNeutralButton("Retry",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									loggingIn();
								}
							});
					builder.setNegativeButton("Dismiss",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.dismiss();
								}
							});

					AlertDialog alert = builder.create();
					alert.show();

				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							BuildInfoActivity.this);
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

	protected void retryPost() {
		String serverURL = IPPOST + planKey;
		if (isNetworkOnline()) {
			client.post(serverURL, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String response) {
					Toast.makeText(BuildInfoActivity.this,
							planKey + " build has been added to build queue",
							Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onFailure(Throwable e, String response) {
					Log.d("ERROR for build", e.toString());
					if (e.toString().contains("Unauthorized")) {
						Log.d("ERROR RESPONSE", response + " YEP");
						AlertDialog.Builder builder = new AlertDialog.Builder(
								BuildInfoActivity.this);
						builder.setTitle("Unauthorized Access.");
						builder.setMessage("You do not have permission to start this build.");
						// builder.setCancelable(true);
						builder.setIcon(R.drawable.failure_icon);
						builder.setNegativeButton("Dismiss",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.dismiss();
									}
								});
						AlertDialog alert = builder.create();
						alert.show();

					} else {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								BuildInfoActivity.this);
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
			});

		} else {
			Toast.makeText(BuildInfoActivity.this,
					"Internet connection is not available", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private class FileDownload extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			Toast.makeText(getApplicationContext(), "Starting Download",
					Toast.LENGTH_SHORT).show();
		}

		@SuppressLint({ "NewApi", "InlinedApi" })
		@Override
		protected Void doInBackground(Void... params) {
			try {
				// connect to url
				//Document document = Jsoup.connect(artifactURL).ignoreContentType(true).get();
				// using elements to get class data
				//Elements artfile = document.getElementsByTag("a");
				// artfile.size()
				//String apkUrlString = "";
				/*for (int i = 0; i < artfile.size(); i++) {
					apkUrlString = !artfile.get(i).attr("href")
							.endsWith(".apk") ? "" : artfile.get(i)
							.attr("href");
				}
				
				apkUrlString += "?os_authType=basic&os_username=";
				apkUrlString += user;
				apkUrlString += "&os_password=";
				apkUrlString += pass;

				String fileSrc = BAMBOOIPADD + apkUrlString;
				Log.d(TAG, "src after: " + fileSrc);
				*/
				// get the URL and pass in the Uri and accessing it through download manager
				Uri src_uri = Uri.parse(artifactURL);
				DownloadManager.Request req = new DownloadManager.Request(src_uri);
				req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, artname);
				dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
				req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
				req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI|DownloadManager.Request.NETWORK_MOBILE);
				enqueue = dm.enqueue(req);
				
			} catch (Exception e) {
				e.printStackTrace();
				Log.d(TAG, "exception " + e);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			 Toast.makeText(getApplicationContext(), "Download Completed",
					Toast.LENGTH_SHORT).show();
		}

	}
	
	
	
	 // always verify the host - dont check for certificate
    final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
          public boolean verify(String hostname, SSLSession session) {
              return true;
          }
   };
	/**
     * Trust every server - dont check for any certificate
     */
    private void trustAllHosts() {

              // Install the all-trusting trust manager
              try {
            	  KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                  trustStore.load(null, null);
                  MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
                  sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                      client.setSSLSocketFactory(sf);
              } catch (Exception e) {
                      e.printStackTrace();
              }
      }
    
    
	public class MySSLSocketFactory extends SSLSocketFactory {
	    SSLContext sslContext = SSLContext.getInstance("TLS");

	    public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
	        super(truststore);

	        TrustManager tm = new X509TrustManager() {
	            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	        };

	        sslContext.init(null, new TrustManager[] { tm }, null);
	    }

	    @Override
	    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
	        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	    }

	    @Override
	    public Socket createSocket() throws IOException {
	        return sslContext.getSocketFactory().createSocket();
	    }
	}

	
	
}




