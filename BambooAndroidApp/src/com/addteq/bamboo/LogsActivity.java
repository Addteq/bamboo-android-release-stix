package com.addteq.bamboo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class LogsActivity extends SherlockActivity {

	private TextView Logtitle;
	private ListView Loglist;
	//private ImageView sendfeedback;
	private PersistentCookieStore myCookieStore;
	AsyncHttpClient client;
	private static String IPLOG = "";
	private static String IPAdd = "";
	SharedPreferences pref;
	SharedPreferences.Editor editor;
	private ArrayAdapter<String> adapter;
	private String infoList_str, infoList_short, errorList_str, errorList_full,
			summary_short;
	private ArrayList<String> fullList;
	private ArrayList<String> infoList;
	private ArrayList<String> errorList;
	private ArrayList<String> summaryList;
	private String jobuildkey;
	private File localFile;
	private Context context;
	private boolean errorListcheck, infoListcheck, listviewEnable;
	ProgressDialog pd;
	private int LOGIN_TIMEOUT=40*1000;
	private static String encryptKey = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log_listview);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayUseLogoEnabled(false);
		
		//sendfeedback = (ImageView) findViewById(R.id.feedback);
		/*sendfeedback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(Api.createFeedbackIntent(LogsActivity.this));
			}
		});*/
		
		UncaughtExceptionHandler mUEHandler = new Thread.UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				e.printStackTrace();
				Api.handleException(e);
				LogsActivity.this.finish();
			}
		};
		Thread.setDefaultUncaughtExceptionHandler(mUEHandler);
		Intent in = getIntent();
		final String jobKey = in.getStringExtra("jobkey");
		jobuildkey = in.getStringExtra("jobbuildkey");

		actionBar.setTitle(jobuildkey);

		client = new AsyncHttpClient();
		myCookieStore = new PersistentCookieStore(LogsActivity.this);
		myCookieStore.getCookies();
		client.setCookieStore(myCookieStore);
		client.addHeader("X-Atlassian-Token", "nocheck");

		Logtitle = (TextView) findViewById(R.id.logTitle);
		Logtitle.setText("LOG:");
		infoListcheck = false;
		errorListcheck = false;
		listviewEnable = true;
		context = this;
		
		Loglist = (ListView) findViewById(R.id.log_listView);
		pref = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		editor = pref.edit();
		IPAdd = pref.getString("IPADDRESS", IPAdd);
		encryptKey = pref.getString("encryptKey", "");
		if (IPAdd.endsWith("/")) {
			IPLOG = IPAdd + "download/" + jobKey + "/build_logs/" + jobuildkey
					+ ".log";
		} else {
			IPLOG = IPAdd + "/download/" + jobKey + "/build_logs/" + jobuildkey
					+ ".log";
		}

		pd = ProgressDialog.show(LogsActivity.this, "",
				"Loading Log...");

		localFile = null;
		try {
			localFile = new File(Environment.getExternalStorageDirectory()
					+ "/BAMBOO_LOG", jobuildkey + "_log.txt");
		} catch (Exception e) {
			Log.e("File load error", e.toString());
		}

		if (localFile.exists()) {
			parseLog();
			pd.dismiss();
		} else {
			if (isOnline()) {
				//Log.d("URL", IPLOG);
				client.get(IPLOG, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						try{
							parseLog(response);
							pd.dismiss();
						}
						catch(Exception error){
							error.printStackTrace();
						}
					}

					@Override
					public void onFailure(Throwable e, String response) {
						
						if(e.toString().contains("HttpResponseException") && e.toString().contains("Unauthorized")){
							loggingIn();
						}else{
						
							try{
								ArrayList<String> resultList = new ArrayList<String>();
								listviewEnable = false;
								infoList_str = "";
								infoList_short = "";
								infoList_short = infoList_short.concat("No log was found for this job");
								infoList_str = infoList_str.concat("No log was found for this job");
								resultList.add(infoList_short);
								adapter = new ArrayAdapter<String>(context, R.layout.log_item, resultList);
								Loglist.setAdapter(adapter);
								pd.dismiss();
							}
							catch(Exception error){
								error.printStackTrace(); 
							}
						}
					}
				});
			} else {
				pd.dismiss();
				Toast.makeText(this, "Internet is not available",
						Toast.LENGTH_SHORT).show();
			}
		}

		Loglist.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(listviewEnable){
					adapter.clear();
					if (position == 0) {
						if (infoListcheck == false) {
							adapter.add(infoList_str);
						} else {
							adapter.add(infoList_short);
						}
						infoListcheck = !infoListcheck;
					} else {
						if (infoListcheck == false) {
							adapter.add(infoList_short);
						} else {
							adapter.add(infoList_str);
						}
					}

					if (position == 1) {
						if (errorListcheck == false) {
							adapter.add(errorList_full);
						} else {
							adapter.add(errorList_str);
						}
						errorListcheck = !errorListcheck;
					} else {
						if (errorListcheck == false) {
							adapter.add(errorList_str);
						} else {
							adapter.add(errorList_full);
						}
					}
					adapter.add(summary_short);
				}
			}
		});
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.logs_menu, menu);
		return true;
	}

	private void parseLog() {
		fullList = new ArrayList<String>();
		infoList = new ArrayList<String>();
		errorList = new ArrayList<String>();
		summaryList = new ArrayList<String>();
		errorList_str = "Error Log:\n";
		errorList_full = "Error Log:\n";
		infoList_str = "Info Log:\n";
		infoList_short = "Info Log:\n";
		summary_short = "Summary Log:\n";
		ArrayList<String> resultList = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(localFile));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.substring(0, 5).equalsIgnoreCase("error")) {
					errorList.add(line + "\n");
				} else {
					infoList.add(line + "\n");
				}
				fullList.add(line + "\n");
				if (line.contains("completed.")
						|| line.contains("Finished building")) {
					summaryList.add(line + "\n");
				} else {
					if (line.contains("[INFO] BUILD")) {
						summaryList.add(line + "\n");
						for (int tempcount = 1; tempcount < 6; tempcount++) {
							line = br.readLine();
							summaryList.add(line + "\n");

							if (line.substring(0, 5).equalsIgnoreCase("error")) {
								errorList.add(line + "\n");
							} else {
								infoList.add(line + "\n");
							}
							fullList.add(line + "\n");
						}
					}
				}
			}
			br.close();

			if (infoList.size() > 0) {
				if (infoList.size() < 50) {
					infoList_short = infoList_short.concat(infoList.size()
							+ " of lines available of total " + infoList.size()
							+ "\n");
					infoList_str = infoList_str.concat(infoList.size()
							+ " of lines available of total " + infoList.size()
							+ "\n");
					for (int count = 0; count < infoList.size(); count++) {
						infoList_str = infoList_str.concat(infoList.get(count));
					}
				} else {
					infoList_short = infoList_short
							.concat("50 of lines available of total "
									+ infoList.size() + "\n");
					infoList_str = infoList_str
							.concat("50 of lines available of total "
									+ infoList.size() + "\n");
					for (int count = 0; count < 50; count++) {
						infoList_str = infoList_str.concat(infoList.get(count));
					}
				}
			} else {
				infoList_short = infoList_short
						.concat("0 of lines available of total "
								+ infoList.size() + "\n");
				infoList_str = infoList_str
						.concat("0 of lines available of total "
								+ infoList.size() + "\n");
			}

			if (errorList.size() > 0) {
				if (errorList.size() < 50) {
					errorList_str = errorList_str.concat(errorList.size()
							+ " of lines available of total "
							+ errorList.size() + "\n");
					errorList_full = errorList_full.concat(errorList.size()
							+ " of lines available of total "
							+ errorList.size() + "\n");
				} else {
					errorList_str = errorList_str
							.concat("50 of lines available of total "
									+ errorList.size() + "\n");
					errorList_full = errorList_full
							.concat("50 of lines available of total "
									+ errorList.size() + "\n");
				}
				for (int count = 0; count < errorList.size(); count++) {
					errorList_full = errorList_full
							.concat(errorList.get(count));
				}
			} else {
				errorList_str = errorList_str
						.concat("0 of lines available of total "
								+ errorList.size() + "\n");
				errorList_full = errorList_full
						.concat("0 of lines available of total "
								+ errorList.size() + "\n");
			}

			if (summaryList.size() > 0) {
				for (int count = 0; count < summaryList.size(); count++) {
					summary_short = summary_short
							.concat(summaryList.get(count));
				}
			}
			if(fullList.size()==0){
				infoList_short = infoList_short.concat("No log was found for this job");
				infoList_str = infoList_str.concat("No log was found for this job");
			}
			resultList.add(infoList_short);
			resultList.add(errorList_str);
			resultList.add(summary_short);
			adapter = new ArrayAdapter<String>(this, R.layout.log_item,
					resultList);
			Loglist.setAdapter(adapter);
		} catch (IOException e) {
			// TODO: handle exception
		}
	}

	private void parseLog(String inputStr) {
		fullList = new ArrayList<String>();
		infoList = new ArrayList<String>();
		errorList = new ArrayList<String>();
		summaryList = new ArrayList<String>();
		errorList_str = "Error Log:\n";
		errorList_full = "Error Log:\n";
		infoList_str = "Info Log:\n";
		infoList_short = "Info Log:\n";
		summary_short = "Summary Log:\n";
		ArrayList<String> resultList = new ArrayList<String>();
		String[] temp = inputStr.split("\n");
		System.out.println("nextLineSize=" + temp.length);
		for (int count = 0; count < temp.length; count++) {
			if (temp[count].substring(0, 5).equalsIgnoreCase("error")) {
				errorList.add(temp[count] + "\n");
			} else {
				infoList.add(temp[count] + "\n");
			}
			fullList.add(temp[count] + "\n");
			if (temp[count].contains("completed.")
					|| temp[count].contains("Finished building")) {
				summaryList.add(temp[count] + "\n");
			} else {
				if (temp[count].contains("[INFO] BUILD")) {
					for (int tempcount = 0; tempcount < 6; tempcount++) {
						summaryList.add(temp[count + tempcount] + "\n");
					}
				}
			}
		}

		if (infoList.size() > 0) {
			if (infoList.size() < 50) {
				infoList_short = infoList_short.concat(infoList.size()
						+ " of lines available of total " + infoList.size()
						+ "\n");
				infoList_str = infoList_str.concat(infoList.size()
						+ " of lines available of total " + infoList.size()
						+ "\n");
				for (int count = 0; count < infoList.size(); count++) {
					infoList_str = infoList_str.concat(infoList.get(count));
				}
			} else {
				infoList_short = infoList_short
						.concat("50 of lines available of total "
								+ infoList.size() + "\n");
				infoList_str = infoList_str
						.concat("50 of lines available of total "
								+ infoList.size() + "\n");
				for (int count = 0; count < 50; count++) {
					infoList_str = infoList_str.concat(infoList.get(count));
				}
			}
		} else {
			infoList_short = infoList_short
					.concat("0 of lines available of total " + infoList.size()
							+ "\n");
			infoList_str = infoList_str.concat("0 of lines available of total "
					+ infoList.size() + "\n");
		}

		if (errorList.size() > 0) {
			if (errorList.size() < 50) {
				errorList_str = errorList_str.concat(errorList.size()
						+ " of lines available of total " + errorList.size()
						+ "\n");
				errorList_full = errorList_full.concat(errorList.size()
						+ " of lines available of total " + errorList.size()
						+ "\n");
			} else {
				errorList_str = errorList_str
						.concat("50 of lines available of total "
								+ errorList.size() + "\n");
				errorList_full = errorList_full
						.concat("50 of lines available of total "
								+ errorList.size() + "\n");
			}
			for (int count = 0; count < errorList.size(); count++) {
				errorList_full = errorList_full.concat(errorList.get(count));
			}
		} else {
			errorList_str = errorList_str
					.concat("0 of lines available of total " + errorList.size()
							+ "\n");
			errorList_full = errorList_full
					.concat("0 of lines available of total " + errorList.size()
							+ "\n");
		}

		if (summaryList.size() > 0) {
			for (int count = 0; count < summaryList.size(); count++) {
				summary_short = summary_short.concat(summaryList.get(count));
			}
		}
		if(fullList.size()==0){
			infoList_short = infoList_short.concat("No log was found for this job");
			infoList_str = infoList_str.concat("No log was found for this job");
		}
		resultList.add(infoList_short);
		resultList.add(errorList_str);
		resultList.add(summary_short);
		adapter = new ArrayAdapter<String>(this, R.layout.log_item, resultList);
		Loglist.setAdapter(adapter);
	}

	private boolean isOnline() {
		ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connect.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	private void downloadLog() {
		try {
			File LogFolder = new File(
					Environment.getExternalStorageDirectory(), "BAMBOO_LOG");
			LogFolder.mkdirs();
			File tempLog = new File(LogFolder, jobuildkey + "_log.txt");
			System.out.println("file name = " + tempLog.getName());
			FileOutputStream fout = new FileOutputStream(tempLog);
			OutputStreamWriter osw = new OutputStreamWriter(fout);
			for (int count = 0; count < fullList.size(); count++) {
				osw.write(fullList.get(count));
			}
			osw.flush();
			osw.close();
			Toast.makeText(this, "Download Successful", Toast.LENGTH_SHORT)
					.show();
			Uri uri = Uri.fromFile(tempLog);
			Toast.makeText(this, "File Located at: " + uri.toString(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Boolean isSDPresent = android.os.Environment
					.getExternalStorageState().equals(
							android.os.Environment.MEDIA_MOUNTED);
			if (isSDPresent) {
				Toast.makeText(this, "Could not write file", Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(this, "Could not find SD card",
						Toast.LENGTH_SHORT).show();
			}
			Log.e("TAG", "Could not write file" + e.getMessage());
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			System.gc();
			LogsActivity.this.finish();
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
			Intent loginIntent = new Intent(LogsActivity.this,
					LoginActivity.class);
			startActivity(loginIntent);
			Intent intent3 = new Intent(LogsActivity.this,
					NotificationService.class);
			intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			stopService(intent3);
			return true;
		} else if (itemId == R.id.help_menu) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					LogsActivity.this);
			builder.setTitle("Help");
			builder.setMessage("This page contains a summary of the Log of "
					+ jobuildkey
					+ " on "
					+ "your Bamboo server. Info Log tab consists of build steps, commands, warning."
					+ "Error Log tab consists of error message. Summary Log Tab shows summary of job such as build status, total run time"
					+ ", final memeory\n"
					+ "Log Tab only retrieve first 50 line of Log file\nFor more information on Log,"
					+ " you can download the log file, which function locate at toolbar. Supported Textviewer apps: 920 Text Editor, "
					+ "Document Viewer, HTMLViewr, Jota Text Editor");
			builder.setCancelable(true);
			builder.setPositiveButton("Dismiss",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
			return true;
		} else if (itemId == R.id.download_menu) {
			if (isOnline()) {
				downloadLog();
			} else {
				Toast.makeText(this, "Internet is not available",
						Toast.LENGTH_SHORT).show();
			}
			return true;
		} else if (itemId == R.id.open_menu) {
			// Action bar open_menu button visibility check
			File file = null;
			try {
				file = new File(Environment.getExternalStorageDirectory()
						+ "/BAMBOO_LOG", jobuildkey + "_log.txt");
			} catch (Exception e) {
				Toast.makeText(this, "File Load Error", Toast.LENGTH_SHORT)
						.show();
			}
			if (file.exists()) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				File tempLog = new File(
						Environment.getExternalStorageDirectory()
								+ "/BAMBOO_LOG", jobuildkey + "_log.txt");
				Uri uri = Uri.fromFile(tempLog);
				intent.setDataAndType(uri, "text/plain");
				startActivity(Intent.createChooser(intent,
						"Choose App to open file"));
			} else {
				Toast.makeText(this,
						"File Does Not Exist. Please Download Log File",
						Toast.LENGTH_SHORT).show();
			}
			return true;
		} else {
			return true;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
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
					retryGetLogs();
				} else {
					Log.d("Login", "Fail");
					if(pd.isShowing())
					{
						try {
							pd.dismiss();
						} catch (Exception dialogException) {
						}
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(
								LogsActivity.this);
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
				if(pd.isShowing())
				{
					try {
						pd.dismiss();
					} catch (Exception dialogException) {
					}
				}
				if(e.toString().contains("SocketTimeoutException") || e.toString().contains("ConnectException")){
					AlertDialog.Builder builder = new AlertDialog.Builder(
							LogsActivity.this);
					builder.setTitle("Error");
					builder.setMessage("Request Timed out. Please try again.");
					builder.setCancelable(true);
					builder.setNeutralButton("Retry",
							new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {
								pd.show();
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
							LogsActivity.this);
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
	
	protected void retryGetLogs(){
		if (isOnline()) {
			//Log.d("URL", IPLOG);
			client.get(IPLOG, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String response) {
					try{
						parseLog(response);
						pd.dismiss();
					}
					catch(Exception error){
						error.printStackTrace();
					}
				}

				@Override
				public void onFailure(Throwable e, String response) {
					try{
						ArrayList<String> resultList = new ArrayList<String>();
						listviewEnable = false;
						infoList_str = "";
						infoList_short = "";
						infoList_short = infoList_short.concat("No log was found for this job");
						infoList_str = infoList_str.concat("No log was found for this job");
						resultList.add(infoList_short);
						adapter = new ArrayAdapter<String>(context, R.layout.log_item, resultList);
						Loglist.setAdapter(adapter);
						pd.dismiss();
					}
					catch(Exception error){
						error.printStackTrace(); 
					}
				}
				
			});
		} else {
			pd.dismiss();
			Toast.makeText(this, "Internet is not available",
					Toast.LENGTH_SHORT).show();
		}
	}
}
