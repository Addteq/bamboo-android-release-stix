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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

import com.actionbarsherlock.view.MenuItem;
import com.addteq.bamboo.BuildListFragment.MySSLSocketFactory;
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
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class StagesActivity extends SherlockActivity {

	TextView revision, triggerVal, completed, duration, successSince, build,
			title;
	LinearLayout banner;
	View ribbon1, ribbon2;
	ImageView stateImg;
	Button buildAll;
	//private ImageView sendfeedback;
	String planKey;
	public String userType;
	ListView lv;
	ArrayList<Integer> state_list;
	ArrayList<String> key_list;
	ArrayList<String> stageList;
	ArrayList<String> stagekeyList;
	StageListAdapter stageAdapter;
	private PersistentCookieStore myCookieStore;
	AsyncHttpClient client;
	private static String IPStage = "";
	private static String IPPOST = "";
	private static String IPAdd = "";
	private static String POST_URL = "/rest/api/latest/queue/";
	private static String STAGE_URL = "/rest/api/latest/result/";
	private static String BASEURL = "/rest/api/latest/";
	private String IPBASEURL = "";

	SharedPreferences pref;
	SharedPreferences.Editor editor;
	ProgressDialog pd;
	private int LOGIN_TIMEOUT=40*1000;
	private static String encryptKey = null;
	private String keyforpost="";
	
	
	Date now;
	Calendar cal;
	Date yestarday;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stages);
		
		UncaughtExceptionHandler mUEHandler = new Thread.UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				e.printStackTrace();
				Api.handleException(e);
				StagesActivity.this.finish();
			}
		};
		Thread.setDefaultUncaughtExceptionHandler(mUEHandler);
		pref=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		editor = pref.edit();
		userType = pref.getString("userType", "");
		IPAdd = pref.getString("IPADDRESS", IPAdd);
		if (IPAdd.endsWith("/")) {
			IPPOST = IPAdd + "rest/api/latest/queue/";
			IPBASEURL = IPAdd + "rest/api/latest/";
		} else {
			IPPOST = IPAdd + POST_URL;
			IPBASEURL = IPAdd + BASEURL;
		}
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayUseLogoEnabled(false);

		Intent in = getIntent();
		final String planKey = in.getStringExtra("key");
		final String planKey1 = in.getStringExtra("pkey");
		int position = in.getIntExtra("position", 0);
		key_list = in.getStringArrayListExtra("keylist");
		state_list = in.getIntegerArrayListExtra("states");
		stageList = new ArrayList<String>();
		stagekeyList = new ArrayList<String>();
		actionBar.setTitle(planKey);

		//sendfeedback = (ImageView) findViewById(R.id.feedback);
		/*sendfeedback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(Api.createFeedbackIntent(StagesActivity.this));
			}
		});*/
		

		client = new AsyncHttpClient();
		myCookieStore = new PersistentCookieStore(StagesActivity.this);
		myCookieStore.getCookies();
		client.setCookieStore(myCookieStore);
		client.addHeader("X-Atlassian-Token", "nocheck");
		pref = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		editor = pref.edit();
		IPAdd = pref.getString("IPADDRESS", IPAdd);
		encryptKey = pref.getString("encryptKey", "@ddt3q");
		if (IPAdd.endsWith("/")) {
			IPPOST = IPAdd + "rest/api/latest/queue/";
			IPStage = IPAdd + "rest/api/latest/result/" + planKey
					+ ".json?expand=stages.stage.results.result";
		} else {
			IPPOST = IPAdd + POST_URL;
			IPStage = IPAdd + STAGE_URL + planKey
					+ ".json?expand=stages.stage.results.result";
		}
		
		getStage();
		
		build = (TextView) findViewById(R.id.buildNo);
		stateImg = (ImageView) findViewById(R.id.imageView1);
		banner = (LinearLayout) findViewById(R.id.banner);
	//	ribbon1 = (View) findViewById(R.id.line);
	//	ribbon2 = (View) findViewById(R.id.line1);
		build.setText("#" + key_list.get(position));

		if (state_list.get(position) == 0) {
			//sendfeedback.setImageResource(R.drawable.bug_green);
			stateImg.setImageResource(R.drawable.green_white);
			banner.setBackgroundResource(R.drawable.green_back_small);
		//	banner.setBackgroundResource(R.color.green);
		//	ribbon1.setBackgroundResource(R.color.green);
		//	ribbon2.setBackgroundResource(R.color.green);
		} else {
			if (state_list.get(position) == 1) {
				//sendfeedback.setImageResource(R.drawable.bug_red);
				stateImg.setImageResource(R.drawable.red_white);
				banner.setBackgroundResource(R.drawable.red_back);

			//	banner.setBackgroundResource(R.color.dark_red);
			//	ribbon1.setBackgroundResource(R.color.dark_red);
			//	ribbon2.setBackgroundResource(R.color.dark_red);
			} else {
				//sendfeedback.setImageResource(R.drawable.bug_yellow);
				stateImg.setImageResource(R.drawable.yellow_white);
				banner.setBackgroundResource(R.drawable.yellow_back);
			//	banner.setBackgroundResource(R.color.yellow);
			//	ribbon1.setBackgroundResource(R.color.yellow);
			//	ribbon2.setBackgroundResource(R.color.yellow);
			}
		}
		title = (TextView) findViewById(R.id.stageTitle);
		buildAll = (Button) findViewById(R.id.buildAll);
		buildAll.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				buildDialog(planKey1);
			}
		});
		lv = (ListView) findViewById(R.id.stagesLV);

		// TODO - Add correct functionality when possible

		// lv.setOnItemClickListener(new OnItemClickListener() {
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view,final int
		// position, long id) {
		// // TODO Auto-generated method stub
		// // Start build with plankey/stagekey
		// String testkey = stageList.get(position);
		// buildDialog1(planKey1, testkey);
		// }
		// });
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
						StagesActivity.this);
				
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
									StagesActivity.this.finish();
									Intent intent3 = new Intent(StagesActivity.this,
											NotificationService.class);
									intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									stopService(intent3);
									Intent goToLoginIntent = new Intent(StagesActivity.this,
											LoginActivity.class);
									startActivity(goToLoginIntent);
									
									//changeIpDialog.setCanceledOnTouchOutside(false);
									//changeIpDialog.show();
								}
							});
				 
			alertDialogBuilder.setTitle("Plugin licence expired");
			alertDialogBuilder.setMessage("Your plugin license in server is expired. Please renew.");
			alertDialogBuilder.setCancelable(false);
			alertDialogBuilder.show();

			
			}
			
			
		}
		
	}

	public void parseStages(String response) {
		JSONObject tempJsonObject = null;
		try {
			tempJsonObject = new JSONObject(response);
			JSONObject stages1 = tempJsonObject.getJSONObject("stages");
			JSONArray stages2 = stages1.getJSONArray("stage");
			for (int i = 0; i < stages2.length(); i++) {
				JSONObject stages3 = stages2.getJSONObject(i);
				String stageName = stages3.getString("name");
				stageList.add(stageName);
				JSONObject stageResults = stages3.getJSONObject("results");
				JSONArray result = stageResults.getJSONArray("result");
				for (int j = 0; j < result.length(); j++) {
					JSONObject res = result.getJSONObject(j);
					String key = res.getString("key");
					stagekeyList.add(key);
				}
			}
			if (stageList.size() >= 1) {
				title.setText("Stages:");
			}
			stageAdapter = new StageListAdapter(this, stageList);
			lv.setAdapter(stageAdapter);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	//Enabling and Disabling Action Bars
  	@Override 
  	public boolean onPrepareOptionsMenu(Menu menu) {  
  		SharedPreferences pref;
		pref = PreferenceManager.getDefaultSharedPreferences(StagesActivity.this);
		String userType=pref.getString("userType", "");
		if (userType.equals("guest")) {
			buildAll.setClickable(false);
	  		menu.getItem(0).setVisible(false);
		} 
  		return super.onPrepareOptionsMenu(menu);
  	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.stages_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			StagesActivity.this.finish();
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
			Intent loginIntent = new Intent(StagesActivity.this,
					LoginActivity.class);
			startActivity(loginIntent);
			Intent intent3 = new Intent(StagesActivity.this,
					NotificationService.class);
			intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			stopService(intent3);
			return true;
		} else if (itemId == R.id.help_menu) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					StagesActivity.this);
			builder.setTitle("Help");
			builder.setMessage("This page contains a list of the stages of the current plan on "
					+ "your Bamboo server. You can start a build for the whole plan by selecting the "
					+ "Build All button.");
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
		} else if (itemId == R.id.log_menu) {
			CharSequence[] cs = stagekeyList
					.toArray(new CharSequence[stagekeyList.size()]);
			AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
			builder2.setTitle("Job List");
			builder2.setItems(cs, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					String[] temp = stagekeyList.get(item).split("-");
					String output = "";
					for (int count = 0; count < 3; count++) {
						if (count == 2) {
							output = output.concat(temp[count]);
						} else {
							output = output.concat(temp[count] + "-");
						}
					}
					Intent LogsIntent = new Intent(StagesActivity.this,
							LogsActivity.class);
					LogsIntent.putExtra("jobkey", output);
					LogsIntent.putExtra("jobbuildkey", stagekeyList.get(item));
					LogsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(LogsIntent);
				}
			});
			AlertDialog alert2 = builder2.create();
			alert2.show();
			alert2.setCanceledOnTouchOutside(true);
			return true;
		} else {
			return true;
		}
	}

	// Build button for all stages
	public void buildDialog(final String key) {
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(
				StagesActivity.this);
		alt_bld.setTitle("Confirm Build:");
		alt_bld.setMessage("Are you sure you want to start " + key + "?");
		alt_bld.setCancelable(false);
		alt_bld.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

				String serverURL = IPPOST + key + "?stage&executeAllStages";
				Log.d("TAG","The server url is: " + serverURL );
				keyforpost = key;
				if(isNetworkOnline()){
					client.addHeader("X-Atlassian-Token", "nocheck");
					client.post(serverURL, new AsyncHttpResponseHandler() {
						@Override
						public void onFailure(Throwable e, String response) {
							// TODO Auto-generated method stub
							
							if(e.toString().contains("Unauthorized")){
									AlertDialog.Builder builder = new AlertDialog.Builder(
										StagesActivity.this);
									builder.setTitle("Unauthorized Access.");
									builder.setMessage("You do not have permission to start this build.");
									builder.setCancelable(true);
									builder.setIcon(R.drawable.failure_icon);
									builder.setNeutralButton("Dismiss",
										new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,
												int id) {
											dialog.dismiss();
										}
									});
									AlertDialog alert = builder.create();
									alert.show();
							}else{
								Toast.makeText(StagesActivity.this,
									key + " - Unable to add to queue.",
									Toast.LENGTH_SHORT).show();
							}
						}

						@Override
						public void onSuccess(String response) {
							Toast.makeText(StagesActivity.this,
									key + " build has been added to build queue",
									Toast.LENGTH_SHORT).show();
							
						}
					});
				}
				else{
					Toast.makeText(StagesActivity.this,
							"No Internet Connection",
							Toast.LENGTH_SHORT).show();
				}
			}
		}).setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		}).show();
	}

	// Build function for building individual stages
	public void buildDialog1(final String key, final String key1) {
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(
				StagesActivity.this);
		alt_bld.setTitle("Confirm Build:");
		alt_bld.setMessage("Are you sure you want to start \"" + key1 + "\"?");
		alt_bld.setCancelable(false);
		alt_bld.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				if(isNetworkOnline()){
					String serverURL = IPPOST + key + "?stage=" + key1;
					client.addHeader("X-Atlassian-Token", "nocheck");
					client.post(serverURL, new AsyncHttpResponseHandler() {
						@Override
						public void onFailure(Throwable e, String response) {
							// TODO Auto-generated method stub
							Toast.makeText(StagesActivity.this,
									key + " - Unable to add to queue.",
									Toast.LENGTH_SHORT).show();
						}

						@Override
						public void onSuccess(String response) {
							Toast.makeText(StagesActivity.this,
									key + " build has been added to build queue",
									Toast.LENGTH_SHORT).show();
						}
					});
				}
				else{
					Toast.makeText(StagesActivity.this,
							"No Internet Connection",
							Toast.LENGTH_SHORT).show();
				}
			}
		}).setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		}).show();
	}
	
	private void getStage(){
		if(isNetworkOnline()){
			pd = ProgressDialog.show(StagesActivity.this, "",
					"Loading Stages...");
			URL url = null;
			String temp = null;
			if (IPBASEURL.contains("rest/api/latest/")) {
				if (userType.equalsIgnoreCase("guest")) {
					temp = IPBASEURL + "result/" + planKey
							+ ".json?expand=results.result";
					
				} else {
					temp = IPBASEURL.replace("rest/api/latest/", "")
							+ "rest/addteqrest/latest/buildlist/" + planKey + ".json";
				}

			}
			final String serverCallURL = temp;
			try {
				url = new URL(serverCallURL);
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
				Log.d("IN PROGRESS LOGGING IN", "TURSTED ALL HOSTS");
				
				
			}
			client.get(IPStage, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String response) {
					// TODO Auto-generated method stub
					parseStages(response);
					pd.dismiss();
				}

				public void onFailure(Throwable e, String response) {
					try{
						e.printStackTrace();
						/*
						try{
							Log.d("Failure", response);
						}
						catch(Exception logError){
							logError.printStackTrace(); 
						}
						*/
						
						if(e.toString().contains("HttpResponseException") && e.toString().contains("Unauthorized")){
							loggingIn(0);
						}else{
							if (pd.isShowing()) {
								try {
									pd.dismiss();
								} catch (Exception dialogException) {
								}
							}
							AlertDialog.Builder alt_bld = new AlertDialog.Builder(
									StagesActivity.this);
							alt_bld.setTitle("Error");
							alt_bld.setMessage("Server Error or Unavailable, Please logout!");
							alt_bld.setCancelable(false);
							alt_bld.setNeutralButton("Logout",
									new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										myCookieStore.clear();
										editor.remove("loggedin");
										editor.remove("GuestLogin");
										editor.commit();
										Intent loginIntent = new Intent(
											StagesActivity.this,
											LoginActivity.class);
									startActivity(loginIntent);
									Intent intent3 = new Intent(
											StagesActivity.this,
											NotificationService.class);
									intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									StagesActivity.this.stopService(intent3);
								}
							});
							alt_bld.setNegativeButton("Dismiss",
									new DialogInterface.OnClickListener() {
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
			AlertDialog.Builder builder = new AlertDialog.Builder(
					StagesActivity.this);
			builder.setTitle("Error");
			builder.setMessage("Internet connection is not available");
			builder.setCancelable(false);
			builder.setPositiveButton("Retry",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int id) {
						getStage();
					}
				});
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

	private void retryGetStage(){
		if(isNetworkOnline()){
			client.get(IPStage, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String response) {
					// TODO Auto-generated method stub
					parseStages(response);
					pd.dismiss();
				}

				public void onFailure(Throwable e, String response) {
					try{
						e.printStackTrace();
						/*
						try{
							Log.d("Failure", response);
						}
						catch(Exception logError){
							logError.printStackTrace(); 
						}
						*/
						if (pd.isShowing()) {
							try {
								pd.dismiss();
							} catch (Exception dialogException) {
							}
						}
						AlertDialog.Builder alt_bld = new AlertDialog.Builder(
								StagesActivity.this);
						alt_bld.setTitle("Error");
						alt_bld.setMessage("Server Error or Unavailable, Please logout!");
						alt_bld.setCancelable(false);
						alt_bld.setNeutralButton("Logout",
								new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									myCookieStore.clear();
									editor.remove("loggedin");
									editor.remove("GuestLogin");
									editor.commit();
									Intent loginIntent = new Intent(
										StagesActivity.this,
										LoginActivity.class);
								startActivity(loginIntent);
								Intent intent3 = new Intent(
										StagesActivity.this,
										NotificationService.class);
								intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								StagesActivity.this.stopService(intent3);
							}
						});
						alt_bld.setNegativeButton("Dismiss",
								new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									dialog.cancel();
							}
						});
						alt_bld.show();
					}
					catch(Exception error){
						error.printStackTrace();
					}	
				}
			});
		}
		else{
			AlertDialog.Builder builder = new AlertDialog.Builder(
					StagesActivity.this);
			builder.setTitle("Error");
			builder.setMessage("Internet connection is not available");
			builder.setCancelable(false);
			builder.setPositiveButton("Retry",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int id) {
						retryGetStage();
					}
				});
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
	
	
	public void loggingIn(final int type){
		
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
						retryGetStage();
					}else{
						retryPost();
					}
				} else {
					Log.d("Login", "Fail");
					if(type==0){
						if (pd.isShowing()) {
							try {
								pd.dismiss();
							} catch (Exception dialogException) {
							}
						}
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(
								StagesActivity.this);
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
					if (pd.isShowing()) {
				
						try {
							pd.dismiss();
						} catch (Exception dialogException) {
						}
					}
				}
				if(e.toString().contains("SocketTimeoutException") || e.toString().contains("ConnectException")){
					AlertDialog.Builder builder = new AlertDialog.Builder(
							StagesActivity.this);
					builder.setTitle("Error");
					builder.setMessage("Request Timed out. Please try again.");
					builder.setCancelable(true);
					builder.setNeutralButton("Retry",
							new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {
								loggingIn(type);
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
							StagesActivity.this);
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
	
	protected void retryPost(){
		final String key = keyforpost;
		String serverURL = IPPOST + key + "?stage&executeAllStages";
		if(isNetworkOnline()){
			
			client.post(serverURL, new AsyncHttpResponseHandler() {
				@Override
				public void onFailure(Throwable e, String response) {
					// TODO Auto-generated method stub
					
					if(e.toString().contains("Unauthorized")){
						if(response.contains("Access is denied")){
							AlertDialog.Builder builder = new AlertDialog.Builder(
								StagesActivity.this);
							builder.setTitle("Unauthorized Access.");
							builder.setMessage("You do not have permission to start this build.");
							builder.setCancelable(true);
							builder.setIcon(R.drawable.failure_icon);
							builder.setNeutralButton("Dismiss",
								new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.dismiss();
								}
							});
							AlertDialog alert = builder.create();
							alert.show();
						}else{
							Toast.makeText(StagesActivity.this,
									key + " - Unable to add to queue.",
									Toast.LENGTH_SHORT).show();
						}
					}else{
						Toast.makeText(StagesActivity.this,
							key + " - Unable to add to queue.",
							Toast.LENGTH_SHORT).show();
					}
				}

				@Override
				public void onSuccess(String response) {
					Toast.makeText(StagesActivity.this,
							key + " build has been added to build queue",
							Toast.LENGTH_SHORT).show();
					
				}
			});
		}
		else{
			Toast.makeText(StagesActivity.this,
					"No Internet Connection",
					Toast.LENGTH_SHORT).show();
		}
	}
}
