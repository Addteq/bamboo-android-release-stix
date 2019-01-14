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
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.apache.http.conn.ssl.SSLSocketFactory;

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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BuildSummaryActivity extends SherlockActivity {
	TextView builds, suc, avg;
	private GraphicalView barView;
	private GraphicalView pieView;
	
	PersistentCookieStore myCookieStore;
	private int[] durations = new int[25];
	private int[] statusChecks = new int[25];
	int successBuilds;
	int total = 0;
	double count = 0;
	private int totalcount;
	String planKey = "";
	int anothercount;
	//private ArrayList<Integer> s;
	//private ArrayList<Integer> f;
	//private ArrayList<Integer> unknown;
	AsyncHttpClient client = new AsyncHttpClient();
	private static String IPPOST = "";
	private static String IPAdd = "";
	private static String POST_URL = "/rest/api/latest/queue/";
	SharedPreferences pref;
	SharedPreferences.Editor editor;
	//private ImageView sendfeedback;
	
	
	Date now;
	Calendar cal;
	Date yestarday;

	// timeout around 25 seconds. (40 does not mean 40 secs..)
		private int LOGIN_TIMEOUT=40*1000;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.summary);		
		UncaughtExceptionHandler mUEHandler = new Thread.UncaughtExceptionHandler() {

	        @Override
	        public void uncaughtException(Thread t, Throwable e) {
	            e.printStackTrace();
	            Api.handleException(e);
	            BuildSummaryActivity.this.finish();
	            
	           
	        }
	    };
	    Thread.setDefaultUncaughtExceptionHandler(mUEHandler);
		Intent incomingIntent = getIntent();
		durations = incomingIntent.getIntArrayExtra("durations");
		successBuilds = incomingIntent.getIntExtra("successCount", 0);
		statusChecks = incomingIntent.getIntArrayExtra("status");
		planKey = incomingIntent.getStringExtra("plan_key");
		totalcount = incomingIntent.getIntExtra("totalcount", 0);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(planKey);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayUseLogoEnabled(false);
		
		//sendfeedback = (ImageView) findViewById(R.id.feedback);
		/*sendfeedback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(Api.createFeedbackIntent(BuildSummaryActivity.this));
			}
		});*/
		
		myCookieStore = new PersistentCookieStore(BuildSummaryActivity.this);
		myCookieStore.getCookies();
		client.setCookieStore(myCookieStore);
		client.addHeader("X-Atlassian-Token", "nocheck");
		pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		editor = pref.edit();
		IPAdd = pref.getString("IPADDRESS", IPAdd);
		IPPOST = IPAdd + POST_URL;
		if(IPAdd.endsWith("/")){
			IPPOST = IPAdd + "rest/api/latest/queue";
		}else{
			IPPOST = IPAdd + POST_URL;
		}
		int[] tempdurations = new int[25];
		int[] tempstatusChecks = new int[25];
		int tempcount=0;
		for(int count= totalcount-1; count>=0;count--){
			tempdurations[count]=durations[tempcount];
			tempstatusChecks[count] = statusChecks[tempcount]; 
			tempcount++;
		}
		
		// calculation for barchart
		getBar(this, tempdurations, tempstatusChecks);

		// calculation for piechart
		// only consider the plan which has duration more than zero.
		for (int i = 0; i < durations.length; i++) {
			if (durations[i] != 0) {
				count++;
			}
		}
		
		// to calculate average duration
		for (int i = 0; i < durations.length; i++) {
			total = total + durations[i];
		}
		
		int tempfailcount =0, tempunknowcount=0;
		for (int i = 0; i < statusChecks.length; i++) {
			if (statusChecks[i] == 1) {
				tempfailcount++;
			}
			if(statusChecks[i]==2){
				tempunknowcount++;
			}
		}
		
		getPie(this, (double) successBuilds / (double) totalcount, (double) tempfailcount/(double) totalcount, (double)tempunknowcount/(double)totalcount);
		// set text
		builds = (TextView) findViewById(R.id.builds);
		suc = (TextView) findViewById(R.id.suc);
		avg = (TextView) findViewById(R.id.average);
		builds.setText("Last " + totalcount + " builds");
		suc.setText( 100 * round((double) successBuilds / (double) totalcount) + "%  successful");
		avg.setText(round((double) total / (double) count) + " secs average duration");
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
						BuildSummaryActivity.this);
				
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
									BuildSummaryActivity.this.finish();
									Intent intent3 = new Intent(BuildSummaryActivity.this,
											NotificationService.class);
									intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									stopService(intent3);
									Intent goToLoginIntent = new Intent(BuildSummaryActivity.this,
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
	
	
	//Enabling and Disabling Action Bars
  	@Override public boolean onPrepareOptionsMenu(Menu menu) {  
  		SharedPreferences pref;
		pref = PreferenceManager.getDefaultSharedPreferences(BuildSummaryActivity.this);
		String userType=pref.getString("userType", "");
		if (userType.equals("guest")) {
	  		menu.getItem(0).setVisible(false);
		}
  		return super.onPrepareOptionsMenu(menu);
  	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.buildsummary_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			
			BuildSummaryActivity.this.finish();
			return true;
			
		} else if (itemId == R.id.startbuild_menu) {
			
			AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
			alt_bld.setTitle("Confirm Build:");
			alt_bld.setMessage("Are you sure you want to start a build?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int id) {
					
					String serverURL = IPPOST + planKey;
					
					Log.d("serverURL","serverURL hellooo: "+serverURL);
					
					
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
						Log.d("IN PROGRESS LOGGING IN", "TURSTED ALL HOSTS");
						
						
					}
					
					
					
					
					
					
					if(isNetworkOnline()){
						client.addHeader("X-Atlassian-Token", "nocheck");
						Log.d("Header added", "XSRF check bypass");
						client.post(serverURL, new AsyncHttpResponseHandler() {
							@Override
							public void onSuccess(String response) {
								Toast.makeText(BuildSummaryActivity.this, planKey + " build has been added to build queue", Toast.LENGTH_SHORT).show();
							}
							@Override
							public void onFailure(Throwable e, String response) {
								Log.d("ERROR for build", e.toString());
								if(e.toString().contains("Unauthorized")){
									/*
									final Toast t = Toast.makeText(getSherlockActivity(), "Unauthorized Access. You do not have permission to start this build.", Toast.LENGTH_SHORT);
									t.getView().setBackgroundColor(Color.RED);
									t.show();
									
								    Handler handler = new Handler();
							        handler.postDelayed(new Runnable() {
							           @Override
							           public void run() {
							               t.cancel(); 
							           }
							    }, 500);
									*/
									if(response.contains("Access is denied")){
										AlertDialog.Builder builder = new AlertDialog.Builder(
												BuildSummaryActivity.this);
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
										loggingIn();
									}
									
								}else{
									AlertDialog.Builder builder = new AlertDialog.Builder(
											BuildSummaryActivity.this);
									builder.setTitle("Error");
									builder.setMessage("Cannot connect to the server.");
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
						});
					}
					else{
						Toast.makeText(BuildSummaryActivity.this, "Internet connection is not available", Toast.LENGTH_SHORT).show();
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
							  client.addHeader("X-Atlassian-Token", "nocheck");
			              } catch (Exception e) {
			                      e.printStackTrace();
			              }
			      }
			    
			    
				class MySSLSocketFactory extends SSLSocketFactory {
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

				
				
				
				
			}).setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// Action for No Button
					dialog.cancel();
				}
			}).show();
			return true;
		} else if (itemId == R.id.help_menu) {
			AlertDialog.Builder builder = new AlertDialog.Builder(BuildSummaryActivity.this);
			builder.setTitle("Help");
			builder.setMessage("This page contains reports on the last 25 builds." + "\n" 
					+ "It gives visuals on the percentage of successful and unsuccessful builds.");
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
			Intent loginIntent = new Intent(BuildSummaryActivity.this, LoginActivity.class);
			startActivity(loginIntent);
			Intent intent3 = new Intent(BuildSummaryActivity.this, NotificationService.class);
			intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			stopService(intent3);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}

	}

	// bar graph

	public void getBar(Context context, int[] val, int[] sf) {
		LinearLayout barlayout = (LinearLayout) findViewById(R.id.bar);
		barlayout.setBackgroundColor(Color.argb(255, 255, 255, 255));

		// data management

		int max = 0;

		for (int i = 0; i < val.length; i++) {
			while (val[i] > max) {
				max = val[i];
			}
		}

		// Bar 1

		CategorySeries series = new CategorySeries(" Successful   ");
		CategorySeries series2 = new CategorySeries(" Unknown   ");
		CategorySeries series3 = new CategorySeries(" Failed   ");
		
		for (int i = 0; i < val.length; i++) {
			if (sf[i] == 0) {
				series.add("Bar " + (i + 1), val[i]);
				series2.add("Bar " + (i+1), 0);
				series3.add("Bar " + (i+1), 0);
			} else if (sf[i] == 1) {
				series.add("Bar " + (i + 1), 0);
				series2.add("Bar " + (i+1), 0);
				series3.add("Bar " + (i + 1), val[i]);
			}
			else{
				//unknown
				series.add("Bar " + (i+1), 0);
				if(max > 500){
					series2.add("Bar " + (i+1), 10);
				}else if(max > 100){
					series2.add("Bar " + (i+1), 5);
				}else{
					series2.add("Bar " + (i+1), 1);
				}
				series3.add("Bar " + (i+1), 0);			
			}
		}

		// Bar 2
		// int[] y2 = { 224, 235};
		// CategorySeries series2 = new CategorySeries("Failed Builds");
		// for (int i = 0; i < y2.length; i++) {
		// series2.add("Bar " + (i+1), y2[i]);
		// }

		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(series.toXYSeries());
		dataset.addSeries(series2.toXYSeries());
		dataset.addSeries(series3.toXYSeries());


		// Customize bar 1
		XYSeriesRenderer renderer = new XYSeriesRenderer();
		renderer.setDisplayChartValues(false);
		renderer.setChartValuesSpacing((float) 0.5);
		renderer.setColor(Color.GREEN);

		// Customize bar 2
		XYSeriesRenderer renderer2 = new XYSeriesRenderer();
		renderer2.setDisplayChartValues(false);
		renderer2.setChartValuesSpacing((float) 0.5);
		renderer2.setColor(Color.YELLOW);
		
		// Customize bar 2
		XYSeriesRenderer renderer3 = new XYSeriesRenderer();
		renderer3.setDisplayChartValues(false);
		renderer3.setChartValuesSpacing((float) 0.5);
		renderer3.setColor(Color.RED);

		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		mRenderer.addSeriesRenderer(renderer);
		mRenderer.addSeriesRenderer(renderer2);
		mRenderer.addSeriesRenderer(renderer3);
		mRenderer.setYTitle("Build Time");
		mRenderer.setXTitle("Builds");
		mRenderer.setAxisTitleTextSize(25);
		mRenderer.setYAxisMax(max);
		mRenderer.setYAxisMin(0);
		mRenderer.setXAxisMax(25);
		mRenderer.setXAxisMin(0);
		mRenderer.setBackgroundColor(Color.BLACK);
		mRenderer.setPanEnabled(false, false);
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setLabelsTextSize(20);
		mRenderer.setMargins(new int[]{0,60,60,40});
		mRenderer.setLegendTextSize(30);
		barView = ChartFactory.getBarChartView(context, dataset, mRenderer, Type.DEFAULT);
		barlayout.addView(barView);
	}

	// pie graph

	public void getPie(Context context, double s, double f, double unknown) {

		LinearLayout pielayout = (LinearLayout) findViewById(R.id.pie);
		pielayout.setBackgroundColor(Color.argb(255, 255, 255, 255));
		CategorySeries series = new CategorySeries("Pie Graph");
		
		DefaultRenderer renderer = new DefaultRenderer();
		//int[] colors = new int[] { Color.GREEN, Color.RED, Color.parseColor("#FF8040")};
		int[] colors = new int[] { Color.GREEN, Color.YELLOW, Color.RED};
		
		if(s!=0){
			series.add("Successful", s);
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(colors[0]);
			renderer.addSeriesRenderer(r);
		}
		if(unknown!=0){
			series.add("Unknown", unknown);
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(colors[1]);
			renderer.addSeriesRenderer(r);
		}
		if(f!=0){
			series.add("Failed", f);
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(colors[2]);
			renderer.addSeriesRenderer(r);
		}
		
		
		renderer.setShowLegend(false);
		renderer.setLabelsTextSize(25);
		//renderer.setLegendTextSize(20);
		renderer.setZoomButtonsVisible(false);
		renderer.setBackgroundColor(Color.BLACK);
		renderer.setApplyBackgroundColor(true);
		renderer.setPanEnabled(false);
		
		pieView = ChartFactory.getPieChartView(context, series, renderer);
		pielayout.addView(pieView);
	}

	double round(double d) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format((Math.floor(d*100)/100)));
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
	public void loggingIn(){
		AsyncHttpClient myClient = new AsyncHttpClient();
		myClient.setCookieStore(myCookieStore);
		myClient.addHeader("X-Atlassian-Token", "nocheck");
				
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
					retryPost();
				} else {
					Log.d("Login", "Fail");
					
					AlertDialog.Builder builder = new AlertDialog.Builder(
							BuildSummaryActivity.this);
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
				
				if(e.toString().contains("SocketTimeoutException") || e.toString().contains("ConnectException")){
					AlertDialog.Builder builder = new AlertDialog.Builder(
							BuildSummaryActivity.this);
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
					builder.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});

					AlertDialog alert = builder.create();
					alert.show();
					
				}else{ 
					AlertDialog.Builder builder = new AlertDialog.Builder(
							BuildSummaryActivity.this);
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
		String serverURL = IPPOST + planKey;
		if(isNetworkOnline()){
			client.addHeader("X-Atlassian-Token", "nocheck");
			client.post(serverURL,
					new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
							Toast.makeText(
									BuildSummaryActivity.this,
									planKey
									+ " build has been added to build queue",
									Toast.LENGTH_SHORT).show();
						}
					@Override
					public void onFailure(Throwable e, String response) {
						Log.d("ERROR for build", e.toString());
						if(e.toString().contains("Unauthorized")){
							Log.d("ERROR RESPONSE", response+" YEP");
							AlertDialog.Builder builder = new AlertDialog.Builder(
									BuildSummaryActivity.this);
							builder.setTitle("Unauthorized Access.");
							builder.setMessage("You do not have permission to start this build.");
							//builder.setCancelable(true);
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
							
						}else{
							AlertDialog.Builder builder = new AlertDialog.Builder(
									BuildSummaryActivity.this);
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
			
			
		}
		else{
			Toast.makeText(BuildSummaryActivity.this, "Internet connection is not available", Toast.LENGTH_SHORT).show();
		}
	}
}
