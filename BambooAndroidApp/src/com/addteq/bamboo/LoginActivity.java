package com.addteq.bamboo;

import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
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

public class LoginActivity extends SherlockActivity {

	private CheckBox rememberMeCheckBox;
	private EditText edtId;
	private EditText edtPw;
	// private TextView verNum;
	private Button btnLogin;
	//private Button btnGuestLogin;
	private Button idClear;
	private Button pwClear;
	//private ImageView ivBug;
	private ImageView ivCog;
	private ImageView ivInfo;
	private String prefix = "http://";
	private String resolved;
	private String userID;
	private String password;
	private ProgressDialog pd;
	private AsyncHttpClient myClient;
	AsyncHttpClient asyncHttpClient;
	private PersistentCookieStore myCookieStore;
	

	
	String message="";
    String title=""; 
    String error="";
    URL urlLicence=null;
    HttpURLConnection uconLicense = null;

	private MyAsyncTask2 task;
	private static String BAMBOOIPADD = "";
	private String connect_str;
	SharedPreferences pref;
	SharedPreferences.Editor editor;
	KeyLayout layout;

	public static LoginActivity loginActivityContext;

	private String host = "";
	private String port = "";

	// timeout around 25 seconds. (40 does not mean 40 secs..)
	private int LOGIN_TIMEOUT = 40 * 1000;
	private boolean certificated = true;
	private static final String TAG = "Login";
	public static final String MyPREF = "MyPrefs";

	// Key used to encrypt and decrypt
	public static String encryptKey = null;
	Button submit;
	EditText newIPadd;
	EditText newPortadd;
	
	
	Date now;
	Calendar cal;
	Date yestarday;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		
		//getting yestardays time
		now = new Date();  
		cal = Calendar.getInstance();  
		cal.setTime(now);  
		cal.add(Calendar.DAY_OF_YEAR, -1);
		yestarday = cal.getTime();
		
		loginActivityContext = this;
		layout = new KeyLayout(this, null);
		
		setContentView(layout);
		//setContentView(R.layout.login);
		
		setupUI((View) findViewById(R.id.parent));
		
		asyncHttpClient = new AsyncHttpClient();

		// UncaughtExceptionHandler mUEHandler = new
		// Thread.UncaughtExceptionHandler() {
		//
		// @Override
		// public void uncaughtException(Thread t, Throwable e) {
		// e.printStackTrace();
		// Api.handleException(e);
		// LoginActivity.this.finish();
		// }
		// };

		// Thread.setDefaultUncaughtExceptionHandler(mUEHandler);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);

		idClear = (Button) findViewById(R.id.id_clear_button);
		pwClear = (Button) findViewById(R.id.pw_clear_button);
		idClear.setVisibility(idClear.INVISIBLE);
		pwClear.setVisibility(pwClear.INVISIBLE);
		//ivBug = (ImageView) findViewById(R.id.ivBug);
		/*ivBug.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				startActivity(Api.createFeedbackIntent(LoginActivity.this));
			}
		});*/

		ivCog = (ImageView) findViewById(R.id.ivCog);
		ivCog.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				openSetting();
			}
		});

		ivInfo = (ImageView) findViewById(R.id.ivInfo);
		ivInfo.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				openHelp();
			}
		});

		edtId = (EditText) findViewById(R.id.idEditText);
		edtId.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		if (VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
			// edtId.setTextColor(Color.WHITE);
			edtId.setTextColor(Color.BLACK);
		} else {
			edtId.setTextColor(Color.BLACK);
		}
		edtPw = (EditText) findViewById(R.id.passwordEditText);
		if (VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
			// edtPw.setTextColor(Color.WHITE);
			edtPw.setTextColor(Color.BLACK);
		} else {
			edtPw.setTextColor(Color.BLACK);
		}

		edtId.addTextChangedListener(checkIDText);
		edtPw.addTextChangedListener(checkpwText);
		edtId.setOnFocusChangeListener(focus);
		edtPw.setOnFocusChangeListener(focus);
		idClear.setOnClickListener(clear);
		pwClear.setOnClickListener(clear);

		btnLogin = (Button) findViewById(R.id.loginButton);
		//btnGuestLogin = (Button) findViewById(R.id.guestLoginButton);
		rememberMeCheckBox = (CheckBox) findViewById(R.id.rememberMeCheckBox);
		pref = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		editor = pref.edit();
		// editor.clear();
		// editor.commit();

		// initializing key
		encryptKey = Secure.getString(getBaseContext().getContentResolver(),
				Secure.ANDROID_ID);

		editor.putString("encryptKey", encryptKey);

		String firstTime = pref.getString("firsttime", "");
		if (firstTime.equals("")) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					LoginActivity.this);
			builder.setTitle("Welcome");
			builder.setMessage("This application requires a license. "
					+ "\n"
					+ "You will not be able to login if you do not have a license. "
					+ "Please contact your System Administrator for more information.");
			builder.setCancelable(false);
			builder.setPositiveButton("Settings",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							//dialog.dismiss();
							settingdialog();
						}
					});

			AlertDialog alert = builder.create();
			alert.show();
			editor.putString("firsttime", "notfirst");
		}
		
		if (firstTime.equals("expire")) {
	
			openSetting();
			editor.putString("firsttime", "notfirst");
		}
		
		

		BAMBOOIPADD = pref.getString("IPADDRESS", BAMBOOIPADD);
		editor.putString("IPADDRESS", BAMBOOIPADD);
		editor.commit();

		Boolean checked = pref.getBoolean("CHECK", false);
		rememberMeCheckBox.setChecked(checked);

		if (rememberMeCheckBox.isChecked()) {
			String en_saved = pref.getString("encryptedid", "");
			String en_pass = pref.getString("encryptedpass", "");
			String saved = null;
			String saved2 = null;
			try {
				// old key = @ddt3q
				saved = SimpleCrypto.decrypt(
						pref.getString("encryptKey", "@ddt3q"), en_saved);
				saved2 = SimpleCrypto.decrypt(
						pref.getString("encryptKey", "@ddt3q"), en_pass);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			edtId.setText(saved);
			edtPw.setText(saved2);
		}

		myClient = new AsyncHttpClient();
		myCookieStore = new PersistentCookieStore(LoginActivity.this);
		myClient.setCookieStore(myCookieStore);

		connect_str = null;

		edtId.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					if (edtId.hasFocus()) {
						edtPw.setFocusable(true);
						edtPw.requestFocus();
						return true;
					}
				}
				return false;
			}

		});

		edtPw.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {

					userID = edtId.getText().toString().trim();
					password = edtPw.getText().toString().trim();
					if (userID.contains(" ") || password.contains(" ")) {
						// Dialog
						AlertDialog.Builder builder = new AlertDialog.Builder(
								LoginActivity.this);
						builder.setTitle("Error:");
						builder.setMessage("Your username and/or password contains a space.");
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
					} else if (userID.equals("") || password.equals("")) {
						if (userID.equals("") && password.equals("")) {
							Toast.makeText(getApplicationContext(),
									"Please Enter Username & Password",
									Toast.LENGTH_SHORT).show();
						}
						if (userID.equals("")) {
							Toast.makeText(getApplicationContext(),
									"Please Enter Username", Toast.LENGTH_SHORT)
									.show();
						}
						if (password.equals("")) {
							Toast.makeText(getApplicationContext(),
									"Please Enter Password", Toast.LENGTH_SHORT)
									.show();
						}
					} else {
						if (isNetworkOnline()) {
							// login();
							AsyncHttpClient tmpclient = new AsyncHttpClient();
							tmpclient.get("http://www.google.com",
									new AsyncHttpResponseHandler() {
										@Override
										public void onSuccess(String response) {

											LoginTask loginTask = new LoginTask();
											loginTask.execute();
										}

										@Override
										public void onFailure(Throwable arg0,
												String arg1) {

											super.onFailure(arg0, arg1);

											AlertDialog.Builder builder = new AlertDialog.Builder(
													LoginActivity.this);
											builder.setTitle("Error");
											builder.setMessage("");
											builder.setCancelable(true);
											builder.setNeutralButton("Dismiss",
													null);
											AlertDialog alert = builder
													.create();

											alert.show();
										}

									});

						} else {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									LoginActivity.this);
							builder.setTitle("Error");
							builder.setMessage("Internet connection is not available");
							builder.setCancelable(true);
							builder.setNeutralButton("Dismiss",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.dismiss();
										}
									});
							AlertDialog alert = builder.create();
							alert.show();
						}
					}

					return true;
				}
				return false;
			}
		});
	/*	btnGuestLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (edtId.hasFocus()) {
					edtId.clearFocus();
				} else if (edtPw.hasFocus()) {
					edtPw.clearFocus();
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(
						LoginActivity.this);
				builder.setTitle("Guest Login:");
				builder.setMessage("If guest access is allowed on the provided Bamboo instance, continue. If not, please use your username and password.");
				builder.setCancelable(true);
				builder.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								AsyncHttpClient tmpclient = new AsyncHttpClient();
								tmpclient.get("http://www.google.com",
										new AsyncHttpResponseHandler() {
											@Override
											public void onSuccess(
													String response) {

												GuestLoginTask gt = new GuestLoginTask();

												gt.execute();
											}

											@Override
											public void onFailure(
													Throwable arg0, String arg1) {

												super.onFailure(arg0, arg1);

												AlertDialog.Builder builder = new AlertDialog.Builder(
														LoginActivity.this);
												builder.setTitle("Error");
												builder.setMessage("Internet connection is not available");
												builder.setCancelable(true);
												builder.setNeutralButton(
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

										});

							}
						});
				builder.setNeutralButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
						});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});*/

		btnLogin.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				pref.edit();
				editor.putString("userType", "user");
				editor.commit();
				userID = edtId.getText().toString().trim();
				password = edtPw.getText().toString().trim();
				if (userID.contains(" ") || password.contains(" ")) {
					// Dialog
					AlertDialog.Builder builder = new AlertDialog.Builder(
							LoginActivity.this);
					builder.setTitle("Error");
					builder.setMessage("Your username or password contains a space.");
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
				} else {
					if (userID.equals("") || password.equals("")) {
						if (userID.equals("") && password.equals("")) {
							Toast.makeText(getApplicationContext(),
									"Please Enter Username & Password",
									Toast.LENGTH_SHORT).show();
						}
						if (userID.equals("")) {
							Toast.makeText(getApplicationContext(),
									"Please Enter Username", Toast.LENGTH_SHORT)
									.show();
						}
						if (password.equals("")) {
							Toast.makeText(getApplicationContext(),
									"Please Enter Password", Toast.LENGTH_SHORT)
									.show();
						}
					} else {
						// task = new MyAsyncTask2();
						// task.execute();
						if (isNetworkOnline()) {
							// login();
							AsyncHttpClient tmpclient = new AsyncHttpClient();
							tmpclient.get("http://www.google.com",
									new AsyncHttpResponseHandler() {
										@Override
										public void onSuccess(String response) {

											LoginTask loginTask = new LoginTask();
											loginTask.execute();
										}

										@Override
										public void onFailure(Throwable arg0,
												String arg1) {

											super.onFailure(arg0, arg1);

											AlertDialog.Builder builder = new AlertDialog.Builder(
													LoginActivity.this);
											builder.setTitle("Error");
											builder.setMessage("Internet connection is not available");
											builder.setCancelable(true);
											builder.setNeutralButton(
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

									});
						} else {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									LoginActivity.this);
							builder.setTitle("Error");
							builder.setMessage("Internet connection is not available");
							builder.setCancelable(true);
							builder.setNeutralButton("Dismiss",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.dismiss();
										}
									});
							AlertDialog alert = builder.create();
							alert.show();
						}
					}
				}
			}// end of onClick
		});

	}// end of onCreate

	// Implementation for idEditText focus change
	private OnFocusChangeListener focus = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
					
			if (v.equals(edtId)) {
				if (hasFocus && edtId.getText().toString().length() > 0) {
					idClear.setVisibility(idClear.VISIBLE);
				} else {
					idClear.setVisibility(idClear.INVISIBLE);
				}
			} else if (v.equals(edtPw)) {
				if (hasFocus && edtPw.getText().toString().length() > 0) {
					pwClear.setVisibility(pwClear.VISIBLE);
				} else {
					pwClear.setVisibility(pwClear.INVISIBLE);
				}
			}
		}

	};

	// Implementation for ID Text Watcher
	private TextWatcher checkIDText = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if (edtId.getText().length() > 0) {
				idClear.setVisibility(idClear.VISIBLE);
			} else {
				idClear.setVisibility(idClear.INVISIBLE);
			}
		}

	};

	// Implementation for Password Text Watcher
	private TextWatcher checkpwText = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if (edtPw.getText().length() > 0) {
				pwClear.setVisibility(pwClear.VISIBLE);
			} else {
				pwClear.setVisibility(pwClear.INVISIBLE);
			}
		}

	};

	// Implementation for clear button
	private OnClickListener clear = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.equals(idClear)) {
				edtId.setText("");
			} else if (v.equals(pwClear)) {
				edtPw.setText("");
			}
		}

	};

	protected void openHelp() {

		AlertDialog.Builder builder = new AlertDialog.Builder(
				LoginActivity.this);
		builder.setTitle("Help");
		builder.setMessage("Username: Bamboo ID"
				+ "\n"
				+ "Password: Bamboo Password"
				+ "\n"
				+ "\n\n"
				+ "This app makes frequent network access, please make sure you have an internet connection.");
		builder.setCancelable(true);
		builder.setPositiveButton("Dismiss",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		AlertDialog alert = builder.create();
		alert.setCanceledOnTouchOutside(true);
		alert.show();
	}
	
	public void message(Context context){
		
		Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show();
		
	}

	public void openSetting() {
					
		final Dialog changeIpDialog = new Dialog(LoginActivity.this);
		changeIpDialog.setContentView(R.layout.ipadd);
		changeIpDialog.setCanceledOnTouchOutside(true);
		changeIpDialog.setTitle(R.string.bamboo_server);

		submit = (Button) changeIpDialog.findViewById(R.id.change);
		final RadioButton http = (RadioButton) changeIpDialog
				.findViewById(R.id.http);
		final RadioButton https = (RadioButton) changeIpDialog
				.findViewById(R.id.https);
		http.setChecked(true);
		http.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					prefix = "http://";
				}
			}
		});
		https.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					prefix = "https://";
				}
			}
		});
		newIPadd = (EditText) changeIpDialog.findViewById(R.id.newIP);
		newPortadd = (EditText) changeIpDialog.findViewById(R.id.newPort);
		final TextView ipAddress = (TextView) changeIpDialog
				.findViewById(R.id.current);
		final ImageView porterror = (ImageView) changeIpDialog
				.findViewById(R.id.porterror);
		final ImageView hosterror = (ImageView) changeIpDialog
				.findViewById(R.id.hosterror);
		porterror.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				newPortadd.setText("");
			}
		});
		hosterror.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				newIPadd.setText("");
			}
		});
				
		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				hideSoftKeyboard();
				
			
				
				certificated = true;
				String hostname = newIPadd.getText().toString();
				String pportnum = "";
				boolean portnumflag = false;
				String separator = "";
				if (hostname.endsWith("/")) {
					hostname = hostname.substring(0, hostname.length() - 1);
				}

				String[] tk = hostname.split("/", 2);
				if (tk.length == 2) {
					// address has path
					String[] tken = tk[0].split(":", 2);
					if (tken.length == 2) {
						// has port number in address
						portnumflag = true;
						hostname = tken[0];
						pportnum = tken[1];
						separator = "/" + tk[1] + "/";
					} else if (tken.length == 1) {
						// does not have port number in address
						hostname = tk[0];
						separator = "/" + tk[1] + "/";
					}
				} else if (tk.length == 1) {
					// address does not have path
					String[] token = hostname.split(":", 2);
					if (token.length == 2) {
						// has port number in address
						portnumflag = true;
						hostname = token[0];
						pportnum = token[1];
					}
				}

				if (portnumflag == false) {
					pportnum = newPortadd.getText().toString();
				}

				final String newpath = separator;
				final String newhost = hostname;
				final String newport = pportnum;
				

				
	           String license="";
	           String newURL="";
	           
	           if(https.isChecked()){
	        	   
	        		newURL="https://" + newhost + ":" + newport+ newpath;
		        	  
					if(newURL.endsWith("/")){
						license = newURL+"rest/addteqrest/latest/check.json";
						
					}else{
						
						license = newURL+"/rest/addteqrest/latest/check.json";
						
					}
	        	   
	           }
	           
	           
	           if(http.isChecked()){
	        	   
	        	newURL="http://" + newhost + ":" + newport+ newpath;
	        	  
				if(newURL.endsWith("/")){
					license = newURL+"rest/addteqrest/latest/check.json";
					
				}else{
					
					license = newURL+"/rest/addteqrest/latest/check.json";
					
				}
				
				Log.d("license","license-url"+license);
				
				asyncHttpClient.get(license, new AsyncHttpResponseHandler(){
					
					public void onSuccess(String response) {

						Log.d("success"," success response 1 "+response);
						
						//call function to do license check and display appropriate messages
						
						doLicenseCheck(response);

					}
					
					public void onFailure(Throwable e, String response) {
						
						if(response.contains("401")){
							
							AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
									LoginActivity.this);

							// set title
							alertDialogBuilder
									.setTitle("Update Plugin");
							
							//set message
							alertDialogBuilder.setMessage("Please update your plugin. Click Update to get the latest plugin");
							
							alertDialogBuilder.setCancelable(false);
							
							alertDialogBuilder.setPositiveButton("Settings",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int id) {
								
											changeIpDialog.setCanceledOnTouchOutside(false);
											changeIpDialog.show();

										}
									});
							
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
							
							
							alertDialogBuilder.show();
							
						}
						
					}

					public void doLicenseCheck(
							String response) {
						
						String title="";
						String message="";
						String token="";
						
						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
								LoginActivity.this);
						
								
						try {
							JSONObject jsonObject = new JSONObject(response);
							String type = jsonObject.getString("Type");
							String expiration = jsonObject.getString("Expiration");
							//expiration=expiration.substring(0,10);
							String version = jsonObject.getString("Version");
						
							//check for the lates plugin
						if(version.equalsIgnoreCase("1.1")){
							
							
							
						}else{
							
							 title = "Update Plugin";
							 message="It appears that the plugin in the server is not updated for latest version. Please update the plugin";
							 
							 alertDialogBuilder.setNegativeButton("Cancel",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
									

											}
										});
							
						}	
							
						//check for the licesnse type
						if(type.equalsIgnoreCase("Commercial")){
							
					/*		title="Commercial License";
							message="You have a commerical license you are good to go";
							
							alertDialogBuilder.setNegativeButton("Cancel",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int id) {
								

										}
									});*/
							
							token="true";
							
							
						}else if(type.equalsIgnoreCase("Evaluation")){
								
							try {
								
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");
								Date expireDate = sdf.parse(expiration);		
								
								//todays date
								//System.currentTimeMillis();
								
	
								Log.d("DATES","current : "+System.currentTimeMillis() +"yestarday : "+/*yestarday.getTime()+*/" expire : "+expireDate.getTime());
								
								if (yestarday.getTime() > expireDate.getTime()) {


									title="Plugin licence expired";
									message ="Your plugin license in server is expired. Please renew.";
									
									
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
											
														changeIpDialog.setCanceledOnTouchOutside(false);
														changeIpDialog.show();
													}
												});
									
									
									Log.d(" EXPIRED"," EXPIRED 4");
									
			
								}else{
									token="true";
									
								}
								

							} catch (Exception e2) {
								Log.e("ERROR",e2.toString());
							}
							

							
						}else if(type.equalsIgnoreCase("unlicensed")){
							
							title="Invalid License";
							message="No valid license found for the STIX plugin on the server.";
							
							alertDialogBuilder.setNegativeButton("Settings",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int id) {
								
											changeIpDialog.setCanceledOnTouchOutside(false);
											changeIpDialog.show();
											
										}
									});

						}
						
						
					

						// set title
						alertDialogBuilder
								.setTitle(title);
						
						//set message
						alertDialogBuilder.setMessage(message);
						
						alertDialogBuilder.setCancelable(false);
						
						
						if(!token.contains("true")){
							
							
							alertDialogBuilder.show();
							token="";

						}

						} catch (JSONException e) {
							
							Log.d("ERROR","ERROR "+encryptKey.toString());
						}
						
						
					}

					
				});
			   }
				
				if (https.isChecked()) {
					// Check certificate here
					// String path = "";		
					
					  Log.d("ssl","ssl "+pref.getString("ssl", "false"));
					  
					  
					  if(!pref.getString("ssl", "false").equalsIgnoreCase(newURL)){
						  
							 asyncHttpClient.get(license, new AsyncHttpResponseHandler(){
								
								public void onSuccess(String response) {

									Log.d("success"," success response 2 "+response);
									
									//call function to do license check and display appropriate messages
									
									doLicenseCheck(response);

								}

								public void onFailure(Throwable e, String response) {
									
									if(response.contains("401")){
										
										AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
												LoginActivity.this);

										// set title
										alertDialogBuilder
												.setTitle("Update Plugin");
										
										//set message
										alertDialogBuilder.setMessage("Please update your plugin. Click Update to get the latest plugin");
										
										alertDialogBuilder.setCancelable(false);
										
										alertDialogBuilder.setPositiveButton("Cancel",
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int id) {
											
														changeIpDialog.setCanceledOnTouchOutside(false);
														changeIpDialog.show();

													}
												});
										
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
										
										
										alertDialogBuilder.show();
										
									}
									
									
									Log.d("failed"," failed response "+response);
									
								}
								
								public void doLicenseCheck(
										String response) {
									
									String title="";
									String message="";
									String token="";
									AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
											LoginActivity.this);
											
									try {
										JSONObject jsonObject = new JSONObject(response);
										String type = jsonObject.getString("Type");
										String expiration = jsonObject.getString("Expiration");
										//expiration=expiration.substring(0,10);
										String version = jsonObject.getString("Version");
									
										//check for the lates plugin
									if(version.equalsIgnoreCase("1.1")){
										
										
										
									}else{
										
										 title = "Update Plugin";
										 message="It appears that the plugin in the server is not updated for latest version. Please update the plugin";
										 
											alertDialogBuilder.setNegativeButton("Cancel",
													new DialogInterface.OnClickListener() {
														public void onClick(
																DialogInterface dialog,
																int id) {
												

														}
													});
										
									}	
										
									//check for the licesnse type
									if(type.equalsIgnoreCase("Commercial")){
										
									/*	title="Valid License";
										message="You have a commerical license you are good to go";
										
										alertDialogBuilder.setNegativeButton("Cancel",
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int id) {
											

													}
												});*/
										
										token="true";
										
										
									}else if(type.equalsIgnoreCase("Evaluation")){
											
										try {
											
											SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");
											Date expireDate = sdf.parse(expiration);	

											
											if (yestarday.getTime() > expireDate.getTime()) {

												title="Plugin licence expired";
												message ="Your plugin license in server is expired. Please renew.";
												
												Log.d(" EXPIRED"," EXPIRED 5");
												
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
																	
																	changeIpDialog.setCanceledOnTouchOutside(false);
																	changeIpDialog.show();

																}
															});
												
												
											}
											else{
												
												token="true";
											}
											

										} catch (Exception e2) {
											Log.e("ERROR",e2.toString());
										}
										

										
									}else if(type.equalsIgnoreCase("unlicensed")){
										
										title="Invalid License";
										message="No valid license found for the STIX plugin on the server.";
										
										alertDialogBuilder.setNegativeButton("Settings",
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int id) {
														
														changeIpDialog.setCanceledOnTouchOutside(false);
														changeIpDialog.show();
											

													}
												});
									
									}
									
									
								

									// set title
									alertDialogBuilder
											.setTitle(title);
									
									//set message
									alertDialogBuilder.setMessage(message);
									
									alertDialogBuilder.setCancelable(false);
									
									
									if(!token.contains("true")){
										
										
										alertDialogBuilder.show();
										token="";

									}

										
									
									} catch (JSONException e) {
										
										Log.d("ERROR","ERROR "+encryptKey.toString());
									}
									
									
								}

								
								
							});
						  
						  
						  
					  }
					  

					
					URL url = null;

					try {
						url = new URL("https://" + newhost + ":" + newport
								+ newpath);
					} catch (MalformedURLException e2) {

						e2.printStackTrace();
					}
					HttpURLConnection ucon = null;
					Log.d("url","url "+url.toString());
					try {
						
						URL link = new URL(url.toString());		
						try {
							HttpURLConnection httpCon = (HttpURLConnection) link.openConnection();

							urlLicence =url;
							Log.d("code","code "+httpCon.getResponseCode());
							
						} catch (Exception e) {
							
							error=e.toString();
							Log.d("ERROR","error : "+error);
							if(error.contains("SSLProtocolException")){
								
								title="Error";
								message="Server return an unsecure response but it looks like you are using https. Please check your settings and try again.";
								
							
							}
							if(error.contains("CertPathValidatorException")){
			
								title="warning";
								message="You attempted to reach "
												+ url.toString()
												+ ", but the server presented a certificate issued by an entity that is not trusted by your system. Do you want to continue?";
								
				
							}
	
						}
			
						Log.e("IP Address Testing in certificate method",
								"Before exception happens");

						ucon = (HttpURLConnection) url.openConnection();
						ucon.setInstanceFollowRedirects(true);
						int responseCode = ucon.getResponseCode();
						
						uconLicense=ucon;
	
						ucon.disconnect();
						
						editor.putString("SSLCHECK", "false");
						editor.commit();
		
					}

					catch (SSLHandshakeException e) {
						Log.e("Exception testing in certificate method", "1");
						certificated = false;
				

						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
								LoginActivity.this);

						// set title
						alertDialogBuilder
								.setTitle(title);   

						//checking for sslProtocolError or CertPathValidatorException
						if(error.contains("SSLProtocolException")){
							
							alertDialogBuilder
							.setMessage(
									message)
							.setCancelable(false)

							.setPositiveButton("Cancel",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int id) {

										}
									});
						}else{
							
						// set dialog message
						alertDialogBuilder
								.setMessage(
										message)
								.setCancelable(false)

								.setPositiveButton("Cancel",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												newIPadd.setText("");
												newPortadd.setText("");

											}
										})

								.setNegativeButton("Continue",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												certificated = true;
												editor.putString("SSLCHECK", "true");
												editor.commit();
												dialog.cancel();
												
													if (urlLicence.getProtocol().toLowerCase().equals("https")) {
												        trustAllHosts();
												        HttpsURLConnection https = null;
														try {
															https = (HttpsURLConnection) urlLicence.openConnection();
														} catch (IOException e) {
															// TODO Auto-generated catch block
															e.printStackTrace();
														}
												        https.setHostnameVerifier(DO_NOT_VERIFY);
												        uconLicense = https;
												    } else {
												        try {
															uconLicense = (HttpURLConnection) urlLicence.openConnection();
														} catch (IOException e) {
															// TODO Auto-generated catch block
															e.printStackTrace();
														}
												    }
													
		
											           String license="";
											           String newURL="";
											           
											          
											           if(https.isChecked()){
											        	   
											        	   newURL="https://" + newhost + ":" + newport+ newpath;
											        	   
											           }

														if(newURL.endsWith("/")){
															license = newURL+"rest/addteqrest/latest/check.json";
															
														}else{
															
															license = newURL+"/rest/addteqrest/latest/check.json";
															
														}
	
														final String editor_url=newURL;
													
													asyncHttpClient.get(license, new AsyncHttpResponseHandler(){
														
														public void onSuccess(String response) {

															Log.d("success"," success response 3 "+response);
															
															//call function to do license check and display appropriate messages
															
															doLicenseCheck(response);

														}
	
														public void onFailure(Throwable e, String response) {
															
															
															if(response.contains("401")){
																
																AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
																		LoginActivity.this);

																// set title
																alertDialogBuilder
																		.setTitle("Update Plugin");
																
																//set message
												
																alertDialogBuilder.setMessage("It appears that the plugin in the server is not updated for latest version. Please update the plugin");
																
																alertDialogBuilder.setCancelable(false);
																
																alertDialogBuilder.setPositiveButton("Cancel",
																		new DialogInterface.OnClickListener() {
																			public void onClick(
																					DialogInterface dialog,
																					int id) {
																	
																				changeIpDialog.setCanceledOnTouchOutside(false);
																				changeIpDialog.show();

																			}
																		});
																
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
																
																
																alertDialogBuilder.show();
																
															}
															
															Log.d("failed"," failed response "+response);
															
														}
														
														public void doLicenseCheck(
																String response) {
															
															String title="";
															String message="";
															String token="";
		
															AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
																	LoginActivity.this);
																	
															try {
																JSONObject jsonObject = new JSONObject(response);
																String type = jsonObject.getString("Type");
																String expiration = jsonObject.getString("Expiration");
																//expiration=expiration.substring(0,10);
																String version = jsonObject.getString("Version");
															
																//check for the lates plugin
															if(version.equalsIgnoreCase("1.1")){
																
																
																
															}else{
																
																 title = "Update Plugin";
																 message="It appears that the plugin in the server is not updated for latest version. Please update the plugin";
																 
																	alertDialogBuilder.setNegativeButton("Cancel",
																			new DialogInterface.OnClickListener() {
																				public void onClick(
																						DialogInterface dialog,
																						int id) {
																		

																				}
																			});
																
																
															}	
																
															//check for the licesnse type
															if(type.equalsIgnoreCase("Commercial")){
																
															/*	title="Valid License";
																message="You have a commerical license you are good to go";
																
																alertDialogBuilder.setNegativeButton("Cancel",
																		new DialogInterface.OnClickListener() {
																			public void onClick(
																					DialogInterface dialog,
																					int id) {
																	

																			}
																		});*/
																token="true";
																
																
															}else if(type.equalsIgnoreCase("Evaluation")){
																	
																try {
																	
																	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");
																	Date expireDate = sdf.parse(expiration);				
																	
																	if (yestarday.getTime() > expireDate.getTime()) {
		
																		title="Plugin licence expired";
																		message ="Your plugin license in server is expired. Please renew.";
																		
																		Log.d(" EXPIRED"," EXPIRED 6");
																		
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
																				
																							changeIpDialog.setCanceledOnTouchOutside(false);
																							changeIpDialog.show();
																						}
																					});
																		
																	}
																	else{
																		
																		token ="true";
																	}
																	
	
																} catch (Exception e2) {
																	Log.e("ERROR",e2.toString());
																}
																
				
																
															}else if(type.equalsIgnoreCase("unlicensed")){
																
																title="Invalid License";
																message="No valid license found for the STIX plugin on the server.";
																
																alertDialogBuilder.setNegativeButton("Settings",
																		new DialogInterface.OnClickListener() {
																			public void onClick(
																					DialogInterface dialog,
																					int id) {
																	
																				changeIpDialog.setCanceledOnTouchOutside(false);
																				changeIpDialog.show();

																			}
																		});
															
															}
													

															// set title
															alertDialogBuilder
																	.setTitle(title);
															
															//set message
															alertDialogBuilder.setMessage(message);
															
															alertDialogBuilder.setCancelable(false);
															
															alertDialogBuilder.setIcon(R.id.btAudio);
															
													
															
															if(!token.contains("true")){
																
																
																alertDialogBuilder.show();
																token="";

															}
															
								
															editor.putString("ssl",editor_url);
															editor.commit();															
															
															} catch (JSONException e) {
																
																Log.d("ERROR","ERROR "+encryptKey.toString());
															}
															
															
														}

														
														
													});
													
		

												if (!newhost.isEmpty()
														&& !newport.isEmpty()) {
													if (matchPort(newport)
															&& matchHost(newhost)) {
														String newaddy = prefix
																+ newhost + ":"
																+ newport
																+ newpath;
														SharedPreferences.Editor editor = pref
																.edit();
														editor.putString(
																"IPADDRESS",
																newaddy);
														editor.putString(
																"HOST",
																newhost
																		+ newpath);
														editor.putString(
																"PORT", newport);
														editor.commit();
														ipAddress
																.setText(newaddy);
														// contextcheck = false;
														if (isNetworkOnline()) {
															task = new MyAsyncTask2();
															task.execute();
														}
														changeIpDialog
																.dismiss();
													}
													if (!matchPort(newport)) {
														porterror
																.setVisibility(View.VISIBLE);
													}
													if (!matchHost(newhost)) {
														hosterror
																.setVisibility(View.VISIBLE);
													}
													if (matchPort(newport)) {
														porterror
																.setVisibility(View.INVISIBLE);
													}
													if (matchHost(newhost)) {
														hosterror
																.setVisibility(View.INVISIBLE);
													}
												} else if (!newhost.isEmpty()) {
													if (matchHost(newhost)) {
														String newaddy = prefix
																+ newhost
																+ newpath;
														SharedPreferences.Editor editor = pref
																.edit();
														editor.putString(
																"IPADDRESS",
																newaddy);
														editor.putString(
																"HOST",
																newhost
																		+ newpath);
														editor.putString(
																"PORT", newport);
														editor.commit();
														ipAddress
																.setText(newaddy);
														// contextcheck = false;
														if (isNetworkOnline()) {
															task = new MyAsyncTask2();
															task.execute();
														}
														changeIpDialog
																.dismiss();
													}
													if (!matchHost(newhost)) {
														hosterror
																.setVisibility(View.VISIBLE);
													}
													if (matchHost(newhost)) {
														hosterror
																.setVisibility(View.INVISIBLE);
													}
												} else if (newhost.isEmpty()
														&& newport.isEmpty()) {
													changeIpDialog.dismiss();
												}
											}
										});
					}//end of the if statement checking for title
					
						// create alert dialog
						AlertDialog alertDialog = alertDialogBuilder.create();

						// show it
						alertDialog.show();
					} catch (UnknownHostException e) {
						
						new AlertDialog.Builder(LoginActivity.this)
					    .setTitle("Warning")
					   .setMessage("The url you trying to  access dosent exist")
					      .setCancelable(true).create().show();      
						
						
						Log.e("Exception testing in certificate method", "2");
						e.printStackTrace();
					} catch (Exception e3) {
						
						Log.e("Exception testing in certificate method", "4");
						e3.printStackTrace();

					}
				} else {
					certificated = true;	
					
				}


				// if(http.isChecked() ||
				// ((certificated==true)&&(https.isChecked())))
				if (certificated == true) {
					if (!newhost.isEmpty() && !newport.isEmpty()) {
						if (matchPort(newport) && matchHost(newhost)) {
							String newaddy = prefix + newhost + ":" + newport
									+ newpath;
							SharedPreferences.Editor editor = pref.edit();
							editor.putString("IPADDRESS", newaddy);
							editor.putString("HOST", newhost + newpath);
							editor.putString("PORT", newport);
							editor.commit();
							ipAddress.setText(newaddy);
							// contextcheck = false;
							if (isNetworkOnline()) {
								task = new MyAsyncTask2();
								task.execute();
							}
							changeIpDialog.dismiss();
						}
						if (!matchPort(newport)) {
							porterror.setVisibility(View.VISIBLE);
						}
						if (!matchHost(newhost)) {
							hosterror.setVisibility(View.VISIBLE);
						}
						if (matchPort(newport)) {
							porterror.setVisibility(View.INVISIBLE);
						}
						if (matchHost(newhost)) {
							hosterror.setVisibility(View.INVISIBLE);
						}
					} else if (!newhost.isEmpty()) {
						if (matchHost(newhost)) {
							String newaddy = prefix + newhost + newpath;
							SharedPreferences.Editor editor = pref.edit();
							editor.putString("IPADDRESS", newaddy);
							editor.putString("HOST", newhost + newpath);
							editor.putString("PORT", newport);
							editor.commit();
							ipAddress.setText(newaddy);
							// contextcheck = false;
							if (isNetworkOnline()) {
								task = new MyAsyncTask2();
								task.execute();
							}
							changeIpDialog.dismiss();
						}
						if (!matchHost(newhost)) {
							hosterror.setVisibility(View.VISIBLE);
						}
						if (matchHost(newhost)) {
							hosterror.setVisibility(View.INVISIBLE);
						}
					} else if (newhost.isEmpty() && newport.isEmpty()) {
						changeIpDialog.dismiss();
					}
				}
				getWindow()
						.setSoftInputMode(
								WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			}

		});
			
		// We will be back here every time we click the IP setting
		String IPADDRESS = pref.getString("IPADDRESS", "No Value");

		host = pref.getString("HOST", "No Value");
		port = pref.getString("PORT", "No Value");
		Log.e("IP Address testing I", host + " " + port);

		if (IPADDRESS != null) {
			try {
				URL temp = new URL(IPADDRESS);
				ipAddress.setText(IPADDRESS);
				//Log.d("IPSET try","ip set "+IPADDRESS);
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
				ipAddress.setText(IPADDRESS);
				//Log.d("IPSET catch","ip set "+IPADDRESS);
			}
		} else {
			ipAddress.setText(IPADDRESS);
			//Log.d("IPSET else","ip set "+IPADDRESS);
		}
		// Change here
		if (IPADDRESS.contains("https://")) {
			https.performClick();
			newPortadd.setText(port);
			newIPadd.setText(host);

		} else if (IPADDRESS.contains("http://")) {
			http.performClick();
			newPortadd.setText(port);
			newIPadd.setText(host);

		}
		changeIpDialog.show();
	}

	protected Context getActivity() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * private class MyAsyncTask extends AsyncTask<Void, Void, String> {
	 * 
	 * @Override protected void onPostExecute(String result) { loginExecute(); }
	 * 
	 * @Override protected void onPreExecute() {
	 * 
	 * }
	 * 
	 * @Override protected String doInBackground(Void... params) { URL url =
	 * null; try { url = new URL(BAMBOOIPADD); } catch (MalformedURLException
	 * e2) { / e2.printStackTrace(); } HttpURLConnection ucon = null; try { ucon
	 * = (HttpURLConnection) url.openConnection(); } catch (IOException e1) {
	 * e1.printStackTrace(); } ucon.setInstanceFollowRedirects(false); URL
	 * secondURL = null; try { secondURL = new
	 * URL(ucon.getHeaderField("Location")); } catch (MalformedURLException e) {
	 * e.printStackTrace(); } if (secondURL == null) { connect_str = null;
	 * Log.d("Connect_str", "null"); return null; } else { connect_str =
	 * secondURL.toString(); Log.d("Connect_str", "Connect to "+connect_str);
	 * return (secondURL.toString()); }
	 * 
	 * 
	 * } }
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.login_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// final Dialog changeIpDialog;
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
			finish();
			System.exit(0);

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	void settingdialog() {
		final Dialog changeIpDialog;
		changeIpDialog = new Dialog(LoginActivity.this);
		changeIpDialog.setContentView(R.layout.ipadd);
		changeIpDialog.setCanceledOnTouchOutside(false);
		changeIpDialog.setTitle(R.string.bamboo_server);
		submit = (Button) changeIpDialog.findViewById(R.id.change);
		final RadioButton http = (RadioButton) changeIpDialog
				.findViewById(R.id.http);
		final RadioButton https = (RadioButton) changeIpDialog
				.findViewById(R.id.https);
		http.setChecked(true);
		http.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					prefix = "http://";
				}
			}
		});
		https.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					prefix = "https://";
				}
			}
		});
		final EditText newIPadd = (EditText) changeIpDialog
				.findViewById(R.id.newIP);
		final EditText newPortadd = (EditText) changeIpDialog
				.findViewById(R.id.newPort);
		final TextView ipAddress = (TextView) changeIpDialog
				.findViewById(R.id.current);
		final ImageView porterror = (ImageView) changeIpDialog
				.findViewById(R.id.porterror);
		final ImageView hosterror = (ImageView) changeIpDialog
				.findViewById(R.id.hosterror);
		porterror.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				newPortadd.setText("");
			}
		});
		hosterror.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				newIPadd.setText("");
			}
		});
		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Log.e("IP address testing under setting dialog", "");

				certificated = true;
				String hostname = newIPadd.getText().toString();
				String pportnum = "";
				boolean portnumflag = false;
				String separator = "";
				if (hostname.endsWith("/")) {
					hostname = hostname.substring(0, hostname.length() - 1);
				}

				String[] tk = hostname.split("/", 2);
				if (tk.length == 2) {
					// address has path
					String[] tken = tk[0].split(":", 2);
					if (tken.length == 2) {
						// has port number in address
						portnumflag = true;
						hostname = tken[0];
						pportnum = tken[1];
						separator = "/" + tk[1] + "/";
					} else if (tken.length == 1) {
						// does not have port number in address
						hostname = tk[0];
						separator = "/" + tk[1] + "/";
					}
				} else if (tk.length == 1) {
					// address does not have path
					String[] token = hostname.split(":", 2);
					if (token.length == 2) {
						// has port number in address
						portnumflag = true;
						hostname = token[0];
						pportnum = token[1];
					}
				}

				if (portnumflag == false) {
					pportnum = newPortadd.getText().toString();
				}

				final String newpath = separator;
				final String newhost = hostname;
				final String newport = pportnum;
				
				
				
				   String license="";
		           String newURL="";
		           
		           if(https.isChecked()){
		        	   
		        		newURL="https://" + newhost + ":" + newport+ newpath;
			        	  
						if(newURL.endsWith("/")){
							license = newURL+"rest/addteqrest/latest/check.json";
							
						}else{
							
							license = newURL+"/rest/addteqrest/latest/check.json";
							
						}
		        	   
		           }
		           
		           
		           if(http.isChecked()){
		        	   
		        	newURL="http://" + newhost + ":" + newport+ newpath;
		        	  
					if(newURL.endsWith("/")){
						license = newURL+"rest/addteqrest/latest/check.json";
						
					}else{
						
						license = newURL+"/rest/addteqrest/latest/check.json";
						
					}
					
					Log.d("license","license-url"+license);
					
					asyncHttpClient.get(license, new AsyncHttpResponseHandler(){
						
						public void onSuccess(String response) {

							Log.d("success"," success response 4 "+response);
							
							//call function to do license check and display appropriate messages
							
							doLicenseCheck(response);

						}
						
						public void onFailure(Throwable e, String response) {
							
							if(response.contains("401")){
								
								AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
										LoginActivity.this);

								// set title
								alertDialogBuilder
										.setTitle("Update Plugin");
								
								//set message
								alertDialogBuilder.setMessage("Please update your plugin. Click Update to get the latest plugin");
								
								alertDialogBuilder.setCancelable(false);
								
								alertDialogBuilder.setPositiveButton("Cancel",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												
												changeIpDialog.setCanceledOnTouchOutside(false);
												changeIpDialog.show();
									

											}
										});
								
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
								
								
								alertDialogBuilder.show();
								
							}
							
						}

						public void doLicenseCheck(
								String response) {
							
							String title="";
							String message="";
							String token="";
							
							
							AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
									LoginActivity.this);
									
							try {
								JSONObject jsonObject = new JSONObject(response);
								String type = jsonObject.getString("Type");
								String expiration = jsonObject.getString("Expiration");
								//expiration=expiration.substring(0,10);
								String version = jsonObject.getString("Version");
							
								//check for the lates plugin
							if(version.equalsIgnoreCase("1.1")){
								
								
								
							}else{
								
								 title = "Update Plugin";
								 message="It appears that the plugin in the server is not updated for latest version. Please update the plugin";
								 
									alertDialogBuilder.setNegativeButton("Cancel",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int id) {
										

												}
											});
								
							}	
								
							//check for the licesnse type
							if(type.equalsIgnoreCase("Commercial")){
								
						/*		title="Commercial License";
								message="You have a commerical license you are good to go";
								
								alertDialogBuilder.setNegativeButton("Cancel",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
									

											}
										});*/
								token="true";
								
								
							}else if(type.equalsIgnoreCase("Evaluation")){
									
								try {
									
									SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");
									Date expireDate = sdf.parse(expiration);		
									
									
									Log.d("DATES","current : "+System.currentTimeMillis() +" expire : "+expireDate.getTime());
									
									if (yestarday.getTime() > expireDate.getTime()) {


										title="Plugin licence expired";
										message ="Your plugin license in server is expired. Please renew.";
										
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
															
															changeIpDialog.setCanceledOnTouchOutside(false);
															changeIpDialog.show();

														}
													});
										
										
										Log.d(" EXPIRED"," EXPIRED 1");
										
				
									}else{
										token="true";
										
									}
									

								} catch (Exception e2) {
									Log.e("ERROR",e2.toString());
								}
								

								
							}else if(type.equalsIgnoreCase("unlicensed")){
								
								title="Invalid License";
								message="No valid license found for the STIX plugin on the server.";
								
								alertDialogBuilder.setNegativeButton("Settings",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												
												changeIpDialog.setCanceledOnTouchOutside(false);
												changeIpDialog.show();
									

											}
										});
							
							}
							
					

							// set title
							alertDialogBuilder
									.setTitle(title);
							
							//set message
							alertDialogBuilder.setMessage(message);
							
							alertDialogBuilder.setCancelable(false);
							
							
							if(!token.contains("true")){
								
								
								alertDialogBuilder.show();
								token="";

							}

							} catch (JSONException e) {
								
								Log.d("ERROR","ERROR "+encryptKey.toString());
							}
							
							
						}

						
					});
				   }
				
				
				

				Log.e("IP Address Testing in submit under case", newhost + " "
						+ newport + newpath);
				
				
				if (https.isChecked()) {
					
					  
					  if(!pref.getString("ssl", "false").equalsIgnoreCase(newURL)){
						  
							 asyncHttpClient.get(license, new AsyncHttpResponseHandler(){
								
								public void onSuccess(String response) {

									Log.d("success"," success response 5 "+response);
									
									//call function to do license check and display appropriate messages
									
									doLicenseCheck(response);

								}

								public void onFailure(Throwable e, String response) {
									
									if(response.contains("401")){
										
										AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
												LoginActivity.this);

										// set title
										alertDialogBuilder
												.setTitle("Update Plugin");
										
										//set message
										alertDialogBuilder.setMessage("Please update your plugin. Click Update to get the latest plugin");
										
										alertDialogBuilder.setCancelable(false);
										
										alertDialogBuilder.setPositiveButton("Cancel",
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int id) {
											
														changeIpDialog.setCanceledOnTouchOutside(false);
														changeIpDialog.show();

													}
												});
										
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
										
										alertDialogBuilder.show();
										
									}
									
									
									Log.d("failed"," failed response "+response);
									
								}
								
								public void doLicenseCheck(
										String response) {
									
									String title="";
									String message="";
									String token="";
									
									AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
											LoginActivity.this);
											
									try {
										JSONObject jsonObject = new JSONObject(response);
										String type = jsonObject.getString("Type");
										String expiration = jsonObject.getString("Expiration");
										//expiration=expiration.substring(0,10);
										String version = jsonObject.getString("Version");
									
										//check for the lates plugin
									if(version.equalsIgnoreCase("1.1")){
										
										
										
									}else{
										
										 title = "Update Plugin";
										 message="It appears that the plugin in the server is not updated for latest version. Please update the plugin";
										
									}	
										
									//check for the licesnse type
									if(type.equalsIgnoreCase("Commercial")){
										
									/*	title="Valid License";
										message="You have a commerical license you are good to go";
										
										alertDialogBuilder.setNegativeButton("Cancel",
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int id) {
											

													}
												});*/
										token="true";
										
									}else if(type.equalsIgnoreCase("Evaluation")){
											
										try {
											
											SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");
											Date expireDate = sdf.parse(expiration);				
											
											if (yestarday.getTime() > expireDate.getTime()) {

												title="Plugin licence expired";
												message ="Your plugin license in server is expired. Please renew.";
												
												Log.d(" EXPIRED"," EXPIRED 2");
												
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
																
																changeIpDialog.setCanceledOnTouchOutside(false);
																changeIpDialog.show();

															}
														});
												
											}
											else{
												
												token="true";
											}
											

										} catch (Exception e2) {
											Log.e("ERROR",e2.toString());
										}
										

										
									}else if(type.equalsIgnoreCase("unlicensed")){
										
										title="Invalid License";
										message="No valid license found for the STIX plugin on the server.";
										
										alertDialogBuilder.setNegativeButton("Settings",
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int id) {
											
														changeIpDialog.setCanceledOnTouchOutside(false);
														changeIpDialog.show();

													}
												});
										
									
									}
									
								

									// set title
									alertDialogBuilder
											.setTitle(title);
									
									//set message
									alertDialogBuilder.setMessage(message);
									
									alertDialogBuilder.setCancelable(false);
									
									
									
									if(!token.contains("true")){
										
										
										alertDialogBuilder.show();
										token="";

									}

										
									
									} catch (JSONException e) {
										
										Log.d("ERROR","ERROR "+encryptKey.toString());
									}
									
									
								}

								
							});
						  
						  
						  
					  }
					
					
					

					// Check certificate here
					// String path = "";
					URL url = null;

					try {
						url = new URL("https://" + newhost + ":" + newport
								+ newpath);
					} catch (MalformedURLException e2) {

						e2.printStackTrace();
					}
					HttpURLConnection ucon = null;
					try {
						
						URL link = new URL(url.toString());		
						try {
							HttpURLConnection httpCon = (HttpURLConnection) link.openConnection();

							Log.d("code","code "+httpCon.getResponseCode());
							
						} catch (Exception e) {
							
							error=e.toString();
							Log.d("ERROR","error : "+error);
							if(error.contains("SSLProtocolException")){
								
								title="Error";
								message="Server return an unsecure response but it looks like you are using https. Please check your settings and try again.";
								
							
							}
							if(error.contains("CertPathValidatorException")){
			
								title="warning";
								message="You attempted to reach "
										+ url.toString()
										+ ", but the server presented a certificate issued by an entity that is not trusted by your system. Do you want to continue?";
				
							}
	
						}
						
							
						Log.e("IP Address Testing in certificate method",
								"Before exception happens");
						ucon = (HttpURLConnection) url.openConnection();
						ucon.setInstanceFollowRedirects(true);

						int responseCode = ucon.getResponseCode();
					
						ucon.disconnect();
						
						editor.putString("SSLCHECK", "false");
						editor.commit();

					}

					catch (SSLHandshakeException e) {
						Log.e("Exception testing in certificate method", "1");
						certificated = false;

						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
								LoginActivity.this);

						// set title
						alertDialogBuilder
								.setTitle(title);
						
						
						//checking for sslProtocolError or CertPathValidatorException
						if(error.contains("SSLProtocolException")){
							
							alertDialogBuilder
							.setMessage(
									message)
							.setCancelable(false)

							.setPositiveButton("Cancel",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int id) {

										}
									});
						}
						else{
						// set dialog message
						alertDialogBuilder
								.setMessage(message)
								.setCancelable(false)

								.setPositiveButton("Cancel",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												newIPadd.setText("");
												newPortadd.setText("");

											}
										})

								.setNegativeButton("Continue",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												
												
												
											    String license="";
										           String newURL="";
										           
										          
										           if(https.isChecked()){
										        	   
										        	   newURL="https://" + newhost + ":" + newport+ newpath;
										        	   
										           }

													if(newURL.endsWith("/")){
														license = newURL+"rest/addteqrest/latest/check.json";
														
													}else{
														
														license = newURL+"/rest/addteqrest/latest/check.json";
														
													}

													final String editor_url=newURL;
												
												asyncHttpClient.get(license, new AsyncHttpResponseHandler(){
													
													public void onSuccess(String response) {

														Log.d("success"," success response 6 "+response);
														
														//call function to do license check and display appropriate messages
														
														doLicenseCheck(response);

													}

													public void onFailure(Throwable e, String response) {
														
														
														if(response.contains("401")){
															
															AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
																	LoginActivity.this);

															// set title
															alertDialogBuilder
																	.setTitle("Update Plugin");
															
															//set message
															alertDialogBuilder.setMessage("Please update your plugin. Click Update to get the latest plugin");
															
															alertDialogBuilder.setCancelable(false);
															
															alertDialogBuilder.setPositiveButton("Cancel",
																	new DialogInterface.OnClickListener() {
																		public void onClick(
																				DialogInterface dialog,
																				int id) {
																
																			changeIpDialog.setCanceledOnTouchOutside(false);
																			changeIpDialog.show();

																		}
																	});
															
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
															
															
															alertDialogBuilder.show();
															
														}
														
														Log.d("failed"," failed response "+response);
														
													}
													
													public void doLicenseCheck(
															String response) {
														
														String title="";
														String message="";
														String token="";
														
														
														AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
																LoginActivity.this);
																
														try {
															JSONObject jsonObject = new JSONObject(response);
															String type = jsonObject.getString("Type");
															String expiration = jsonObject.getString("Expiration");
															//expiration=expiration.substring(0,10);
															String version = jsonObject.getString("Version");
														
															//check for the lates plugin
														if(version.equalsIgnoreCase("1.1")){
															
															
															
														}else{
															
															 title = "Update Plugin";
															 message="It appears that the plugin in the server is not updated for latest version. Please update the plugin";
															 
															 
																alertDialogBuilder.setNegativeButton("Cancel",
																		new DialogInterface.OnClickListener() {
																			public void onClick(
																					DialogInterface dialog,
																					int id) {
																	

																			}
																		});
															
														}	
															
														//check for the licesnse type
														if(type.equalsIgnoreCase("Commercial")){
															
															/*title="Valid License";
															message="You have a commerical license you are good to go";
															
															alertDialogBuilder.setNegativeButton("Cancel",
																	new DialogInterface.OnClickListener() {
																		public void onClick(
																				DialogInterface dialog,
																				int id) {
																

																		}
																	});*/
															token="true";
															
															
															
														}else if(type.equalsIgnoreCase("Evaluation")){
																
															try {
																
																SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");
																Date expireDate = sdf.parse(expiration);				
																
																if (yestarday.getTime() > expireDate.getTime()) {
	
																	title="Plugin licence expired";
																	message ="Your plugin license in server is expired. Please renew.";
																	
																	Log.d(" EXPIRED"," EXPIRED 3");
																	
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
																		
																					changeIpDialog.setCanceledOnTouchOutside(false);
																					changeIpDialog.show();
																				}
																			});
																	
																	
																	
																}
																else{
																	
																	token="true";
																}
																

															} catch (Exception e2) {
																Log.e("ERROR",e2.toString());
															}
															
			
															
														}else if(type.equalsIgnoreCase("unlicensed")){
															
															title="Invalid License";
															message="No valid license found for the STIX plugin on the server.";
															
															alertDialogBuilder.setNegativeButton("Settings",
																	new DialogInterface.OnClickListener() {
																		public void onClick(
																				DialogInterface dialog,
																				int id) {
																			
																			changeIpDialog.setCanceledOnTouchOutside(false);
																			changeIpDialog.show();

																		}
																	});
															
														
														}
														
												

														// set title
														alertDialogBuilder
																.setTitle(title);
														
														//set message
														alertDialogBuilder.setMessage(message);
														
														alertDialogBuilder.setCancelable(false);
														
														
														if(!token.contains("true")){
															
															
															alertDialogBuilder.show();
															token="";

														}

														editor.putString("ssl",editor_url);
														editor.commit();	
														
														} catch (JSONException e) {
															
															Log.d("ERROR","ERROR "+encryptKey.toString());
														}
														
														
													}

													
													
												});
												
	
												certificated = true;
												editor.putString("SSLCHECK", "true");
												editor.commit();
												dialog.cancel();

												if (!newhost.isEmpty()
														&& !newport.isEmpty()) {
													if (matchPort(newport)
															&& matchHost(newhost)) {
														String newaddy = prefix
																+ newhost + ":"
																+ newport
																+ newpath;
														SharedPreferences.Editor editor = pref
																.edit();
														editor.putString(
																"IPADDRESS",
																newaddy);
														editor.putString(
																"HOST",
																newhost
																		+ newpath);
														editor.putString(
																"PORT", newport);
														editor.commit();
														ipAddress
																.setText(newaddy);
														// contextcheck = false;
														if (isNetworkOnline()) {
															task = new MyAsyncTask2();
															task.execute();
														}
														changeIpDialog
																.dismiss();
													}
													if (!matchPort(newport)) {
														porterror
																.setVisibility(View.VISIBLE);
													}
													if (!matchHost(newhost)) {
														hosterror
																.setVisibility(View.VISIBLE);
													}
													if (matchPort(newport)) {
														porterror
																.setVisibility(View.INVISIBLE);
													}
													if (matchHost(newhost)) {
														hosterror
																.setVisibility(View.INVISIBLE);
													}
												} else if (!newhost.isEmpty()) {
													if (matchHost(newhost)) {
														String newaddy = prefix
																+ newhost
																+ newpath;
														SharedPreferences.Editor editor = pref
																.edit();
														editor.putString(
																"IPADDRESS",
																newaddy);
														editor.putString(
																"HOST",
																newhost
																		+ newpath);
														editor.putString(
																"PORT", newport);
														editor.commit();
														ipAddress
																.setText(newaddy);
														// contextcheck = false;
														if (isNetworkOnline()) {
															task = new MyAsyncTask2();
															task.execute();
														}
														changeIpDialog
																.dismiss();
													}
													if (!matchHost(newhost)) {
														hosterror
																.setVisibility(View.VISIBLE);
													}
													if (matchHost(newhost)) {
														hosterror
																.setVisibility(View.INVISIBLE);
													}
												} else if (newhost.isEmpty()
														&& newport.isEmpty()) {
													changeIpDialog.dismiss();
												}

											}

										});
						}//end of checking if condition
						
						// create alert dialog
						AlertDialog alertDialog = alertDialogBuilder.create();

						// show it
						alertDialog.show();

					} catch (UnknownHostException e) {
						Log.e("Exception testing in certificate method", "2");
						e.printStackTrace();
					}

					catch (Exception e3) {
						Log.e("Exception testing in certificate method", "4");
						e3.printStackTrace();

					}

				}
				// else
				// {
				// certificated=true;
				// }

				// if(certificated==true)
				// {
				if (!newhost.isEmpty() && !newport.isEmpty()) {
					if (matchPort(newport) && matchHost(newhost)) {
						String newaddy = prefix + newhost + ":" + newport
								+ newpath;
						SharedPreferences.Editor editor = pref.edit();
						editor.putString("IPADDRESS", newaddy);
						editor.putString("HOST", newhost + newpath);
						editor.putString("PORT", newport);
						editor.commit();
						ipAddress.setText(newaddy);
						// contextcheck = false;
						if (isNetworkOnline()) {
							task = new MyAsyncTask2();
							task.execute();
						}
						changeIpDialog.dismiss();
					}
					if (!matchPort(newport)) {
						porterror.setVisibility(View.VISIBLE);
					}
					if (!matchHost(newhost)) {
						hosterror.setVisibility(View.VISIBLE);
					}
					if (matchPort(newport)) {
						porterror.setVisibility(View.INVISIBLE);
					}
					if (matchHost(newhost)) {
						hosterror.setVisibility(View.INVISIBLE);
					}
				} else if (!newhost.isEmpty()) {
					if (matchHost(newhost)) {
						String newaddy = prefix + newhost + newpath;
						SharedPreferences.Editor editor = pref.edit();
						editor.putString("IPADDRESS", newaddy);
						// editor.putString("firsttime", "notfirst");
						editor.putString("HOST", newhost + newpath);
						editor.putString("PORT", newport);
						editor.commit();
						ipAddress.setText(newaddy);
						// contextcheck = false;
						if (isNetworkOnline()) {
							task = new MyAsyncTask2();
							task.execute();
						}
						changeIpDialog.dismiss();
					}
					if (!matchHost(newhost)) {
						hosterror.setVisibility(View.VISIBLE);
					}
					if (matchHost(newhost)) {
						hosterror.setVisibility(View.INVISIBLE);
					}
				}
			}
			// }
		});

		String IPADDRESS = pref.getString("IPADDRESS", "No Value");
		host = pref.getString("HOST", "No Value");
		port = pref.getString("PORT", "No Value");
		Log.e("IP Address testing II", host + " " + port);
		ipAddress.setText(IPADDRESS);

		// change here
		if (BAMBOOIPADD.contains("https://")) {
			https.performClick();
			newPortadd.setText(port);
			newIPadd.setText(host);

		} else if (BAMBOOIPADD.contains("http://")) {
			http.performClick();
			newPortadd.setText(port);
			newIPadd.setText(host);
		}

		changeIpDialog.show();
	}

	boolean matchHost(String input) {
		// Match bamboo.(name).com or bamboo.(name).org
		// Pattern bam = Pattern.compile("(bamboo)");
		// Pattern add = Pattern.compile("(\\.[a-zA-Z]+\\.)");
		// Pattern com = Pattern.compile("(com)|(org)");
		// Matcher m1 = bam.matcher(input);
		// Matcher m2 = add.matcher(input);
		// Matcher m3 = com.matcher(input);
		// while (m1.find()) {
		// while (m2.find()) {
		// while (m3.find()) {
		// return true;
		// }
		// }
		// }
		if (Patterns.WEB_URL.matcher(input).matches()) {
			return true;
		}
		if (matchIP(input)) {
			return true;
		} else {
			return false;
		}
	}

	boolean matchPort(String input) {
		// Pattern port = Pattern.compile("([0-9]{4})");
		// Matcher m = port.matcher(input);
		// while (m.find()) {
		// return true;
		// }
		// return false;
		// Match 16 bit port number
		int temp = Integer.parseInt(input);
		if (temp >= 0 || temp <= 65535) {
			return true;
		} else {
			return false;
		}
	}

	boolean matchIP(String input) {
		// Match IP string
		Pattern ip = Pattern
				.compile("([0-9]{2,})\\.([0-9]{2,})\\.([0-9]{1,})\\.([0-9]{1,})");
		Matcher m = ip.matcher(input);
		while (m.find()) {
			return true;
		}
		return false;
	}

	void login() {
		userID = edtId.getText().toString().trim();
		password = edtPw.getText().toString().trim();
		BAMBOOIPADD = pref.getString("IPADDRESS", BAMBOOIPADD);
		if (BAMBOOIPADD.equals("")) {
			pd.dismiss();

			settingdialog();
		} else {
			BAMBOOIPADD = pref.getString("IPADDRESS", BAMBOOIPADD);
			String license = "";
			if (BAMBOOIPADD.endsWith("/")) {
				license = BAMBOOIPADD
						+ "rest/addteqrest/1.0/check.json?os_authType=basic";
			} else {
				license = BAMBOOIPADD
						+ "/rest/addteqrest/1.0/check.json?os_authType=basic";
			}
			String licenseLogin = license;

			//Log.d("IN LOGIN", licenseLogin);
			// pd = new ProgressDialog(LoginActivity.this);
			// pd.setMessage("Signing In...");
			// pd.setCancelable(false);
			// pd.show();
			myClient.setTimeout(LOGIN_TIMEOUT);
			myClient.setBasicAuth(userID, password);
			
			Log.d("licenseLogin","licenseLogin "+licenseLogin);
			
			myClient.get(licenseLogin, new AsyncHttpResponseHandler() {
				

				@Override
				public void onSuccess(String response) {
					Log.d("LOGIN SUCCESS", response);
					
					
						if (response.contains("Commercial") || response.contains("Evaluation")) {
							
							//getting the expiration date and saving it in shared preference
							
							try {

								JSONObject jsonObject = new JSONObject(response);
								String expiration = jsonObject.getString("Expiration");
						
								
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");
								Date expireDate = sdf.parse(expiration);
								
								 editor.putLong("expiredate", expireDate.getTime());
								 editor.commit();
								
								
							} catch (Exception e) {
								
								Log.e("LOGINACTIVITY","Error : "+e.toString());
								
							}
							

							String crypto1 = null;
							try {
								crypto1 = SimpleCrypto.encrypt(
										pref.getString("encryptKey", "@ddt3q"),
										password);
							} catch (Exception e1) {

								e1.printStackTrace();
							}
							String crypto = null;
							try {
								crypto = SimpleCrypto.encrypt(
										pref.getString("encryptKey", "@ddt3q"),
										userID);
							} catch (Exception e1) {

								e1.printStackTrace();
							}
							if (rememberMeCheckBox.isChecked()) {
								editor.putString("encryptedid", crypto);
								editor.putString("encryptedpass", crypto1);
								editor.putBoolean("CHECK", true);
								editor.putBoolean("loggedin", true);
								editor.commit();
							} else {
								editor.putString("encryptedid", crypto);
								editor.putString("encryptedpass", crypto1);
								editor.putBoolean("CHECK", false);
								editor.putBoolean("loggedin", true);
								editor.commit();
							}

							Intent intent = new Intent(LoginActivity.this,
									ProjectListActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							Intent intent2 = new Intent(LoginActivity.this,
									NotificationService.class);
							intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startService(intent2);
							pd.dismiss();
						} else {
							Log.e("before showAlert at onSuccess of Login",
									"I am doing testing");
							showAlert(response);
						}
					
				   
				}
				@Override
				public void onFailure(Throwable e, String response) {
					// pd.dismiss();
					//Log.d(TAG, "onFailure response: " + response);
					pd.dismiss();
					myCookieStore.clear();
					
					Log.d("login-error","response "+response);
					
					if (e.toString().contains("SocketTimeoutException")
							|| e.toString().contains("ConnectException")) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								LoginActivity.this);
						builder.setTitle("Error");
						builder.setMessage("Request Timed out. Please try again.");
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
					} else if (e.toString().contains("AUTHENTICATED_FAILED")) {

						AlertDialog.Builder builder = new AlertDialog.Builder(
								LoginActivity.this);
						builder.setTitle("Authentication Failed");
						// TODO Stix plugin

						// builder.setMessage("Couldn't find the STIX plugin on server for authentication. Please download the plugin to enable functionality.");
						builder.setMessage("Username or Password incorrect. Please try again or check with the Administrator.");
						// builder.setMessage("You were unable to login, please ensure you are authorized to access this server and your credentials are correct.");
						builder.setCancelable(true);
						builder.setNeutralButton("Dismiss", null);
						/*
						 * builder.setPositiveButton("Download", new
						 * DialogInterface.OnClickListener() {
						 * 
						 * @Override public void onClick(DialogInterface dialog,
						 * int which) { // open the download page. Intent
						 * webintent = new Intent(Intent.ACTION_VIEW, Uri.parse(
						 * "https://marketplace.atlassian.com/plugins/com.addteq.bamboo.plugin.addteq-bamboo-plugin"
						 * )); startActivity(webintent); } });
						 */
						AlertDialog alert = builder.create();
						alert.show();
					} else if (e.toString().contains("AUTHENTICATION_DENIED")) {

						AlertDialog.Builder builder = new AlertDialog.Builder(
								LoginActivity.this);
						builder.setTitle("Update Plugin");
						builder.setMessage("Please update your plugin. Click Download to get the latest plugin.");
						builder.setCancelable(true);
						builder.setNeutralButton("Dismiss",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.dismiss();
									}
								});
						builder.setPositiveButton("Download",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

										Intent webintent = new Intent(
												Intent.ACTION_VIEW,
												Uri.parse("https://marketplace.atlassian.com/plugins/com.addteq.bamboo.plugin.addteq-bamboo-plugin"));
										startActivity(webintent);
									}
								});
						AlertDialog alert = builder.create();
						alert.show();

					} else if (e.toString().contains("HttpResponseException")) {

						if (e.toString().contains("Not Found")) {

							if (response.contains("Page not found")) {
								AlertDialog.Builder builder = new AlertDialog.Builder(
										LoginActivity.this);
								builder.setTitle("Update Plugin");
								builder.setMessage("Couldn't find the STIX plugin on the server for authentication. Please download the plugin to enable functionality.");
								builder.setCancelable(true);
								builder.setNeutralButton("Dismiss",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												dialog.dismiss();
											}
										});
								builder.setPositiveButton("Download",
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {

												Intent webintent = new Intent(
														Intent.ACTION_VIEW,
														Uri.parse("https://marketplace.atlassian.com/plugins/com.addteq.bamboo.plugin.addteq-bamboo-plugin"));
												startActivity(webintent);
											}
										});
								AlertDialog alert = builder.create();
								alert.show();
							} else {
								
							
								
								AlertDialog.Builder builder = new AlertDialog.Builder(
										LoginActivity.this);
								builder.setTitle("Error");
								builder.setMessage("The Bamboo server that you are trying to access is not a valid Bamboo server.");
								builder.setCancelable(true);
								builder.setNeutralButton("Dismiss",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												dialog.dismiss();
											}
										});
								AlertDialog alert = builder.create();
								alert.show();
							}
						} else if (e.toString().contains("Unauthorized")) {
							pd.dismiss();

							AlertDialog.Builder builder = new AlertDialog.Builder(
									LoginActivity.this);
							builder.setTitle("Authentication Denied");
							builder.setMessage("Username or Password incorrect. Please try again or check with the Administrator.");
							// builder.setMessage("You were unable to login, please ensure you are authorized to access this server and your credentials are correct.");
							builder.setCancelable(true);
							builder.setNeutralButton("Dismiss", null);
							AlertDialog alert = builder.create();
							alert.show();

						} else {
							//e.toString() == "Bad Request"
							pd.dismiss();
							AlertDialog.Builder builder = new AlertDialog.Builder(
									LoginActivity.this);
							builder.setTitle("Error");
							builder.setMessage("Cannot reach server (Server returned a secure response but it looks like you are using http. Please check your settings and try again. )");
							builder.setCancelable(true);
							builder.setNeutralButton("Dismiss",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.dismiss();
										}
									});
							AlertDialog alert = builder.create();
							alert.show();
						}
					} else if (!e.toString().contains("Exception")) {
						if (response.contains("status-code")
								&& response.contains("500")) {

							showAlert(response);
						} else {

							pd.dismiss();
							AlertDialog.Builder builder = new AlertDialog.Builder(
									LoginActivity.this);
							builder.setTitle("Error");
							builder.setMessage("Cannot connect to server 500 else");
							builder.setCancelable(true);
							builder.setNeutralButton("Dismiss",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.dismiss();
										}
									});
							AlertDialog alert = builder.create();
							alert.show();
						}
					} else {
						pd.dismiss();

						AlertDialog.Builder builder = new AlertDialog.Builder(
								LoginActivity.this);
						builder.setTitle("Error");
						builder.setMessage("Cannot connect to server exception else");
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
	}

	void loginExecute() {
		resolved = connect_str;
		if (resolved != null) {
			String license = "";
			if (resolved.endsWith("/")) {
				license = resolved
						+ "rest/addteqrest/1.0/check.json?os_authType=basic";
			} else {
				license = resolved
						+ "/rest/addteqrest/1.0/check.json?os_authType=basic";
			}
			String licenseLogin = license + "&os_username=" + userID
					+ "&os_password=" + password;

			myClient.get(licenseLogin, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String response) {
					if (response.contains("check-code")) {
						if (response.contains("success")) {
							editor.putString("IPADDRESS", resolved);
							editor.commit();
							Intent intent = new Intent(LoginActivity.this,
									ProjectListActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							Intent intent2 = new Intent(LoginActivity.this,
									NotificationService.class);
							intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startService(intent2);
							pd.dismiss();
						} else {

							Log.e("before showAlert at onSuccess of loginExecute",
									"I am doing testing");
							showAlert(response);
						}
					}
				}

				@Override
				public void onFailure(Throwable e, String response) {
					// pd.dismiss();
					myCookieStore.clear();

					showAlert(response);
					Toast.makeText(getApplicationContext(), "Login Fail",
							Toast.LENGTH_SHORT).show();

				}
			});// end of get
		} else {
			pd.dismiss();
			myCookieStore.clear();
			Toast.makeText(getApplicationContext(), "Login Fail",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		myClient = new AsyncHttpClient();
		myCookieStore = new PersistentCookieStore(LoginActivity.this);

		myClient.setCookieStore(myCookieStore);
		edtPw.setText("");
		Boolean checked = pref.getBoolean("CHECK", false);
		rememberMeCheckBox.setChecked(checked);

		// loggedin shared pref
		Boolean loggedin = pref.getBoolean("loggedin", false);
		Boolean GuestLogin = pref.getBoolean("GuestLogin", false);
		
		

		// old condtion
		// if (rememberMeCheckBox.isChecked()) {

		// new condition
		if (loggedin == true || GuestLogin == true) {

			Intent intent = new Intent(LoginActivity.this,
					ProjectListActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			Intent intent2 = new Intent(LoginActivity.this,
					NotificationService.class);
			intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startService(intent2);

		} else {
			String en_saved = pref.getString("encryptedid", "");
			String en_pass = pref.getString("encryptedpass", "");
			String saved = null;
			String saved2 = null;
			try {
				saved = SimpleCrypto.decrypt(
						pref.getString("encryptKey", "@ddt3q"), en_saved);
				saved2 = SimpleCrypto.decrypt(
						pref.getString("encryptKey", "@ddt3q"), en_pass);
			} catch (Exception e) {
				e.printStackTrace();
			}
			edtId.setText(saved);
			edtId.setFocusable(true);
			edtId.requestFocus();
			if (rememberMeCheckBox.isChecked()) {
				edtPw.setText(saved2);
			}
		}

	}

	public void hideSoftKeyboard() {
		InputMethodManager inputMethodManager = (InputMethodManager) this
				.getSystemService(Activity.INPUT_METHOD_SERVICE);
		if (inputMethodManager.isActive()) {
			inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus()
					.getWindowToken(), 0);
		}
	}

	public void setupUI(View view) {
		if (!(view instanceof EditText) && !(view instanceof Button)) {
			view.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					hideSoftKeyboard();
					return false;
				}
			});
		}

		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				View inner = ((ViewGroup) view).getChildAt(i);
				setupUI(inner);
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		String user_box = edtId.getText().toString();
		String pass_box = edtPw.getText().toString();
		boolean box_check = false;
		if (rememberMeCheckBox.isChecked()) {
			box_check = true;
		}

		layout = new KeyLayout(this, null);
		setContentView(layout);

		edtId = (EditText) findViewById(R.id.idEditText);
		edtId.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		edtPw = (EditText) findViewById(R.id.passwordEditText);
		btnLogin = (Button) findViewById(R.id.loginButton);
		rememberMeCheckBox = (CheckBox) findViewById(R.id.rememberMeCheckBox);

		// Re-enter input before user rotate
		edtId.setText(user_box);
		if (VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
			// edtId.setTextColor(Color.WHITE);
			edtId.setTextColor(Color.BLACK);
		} else {
			edtId.setTextColor(Color.BLACK);
		}
		edtPw.setText(pass_box);
		if (VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
			// edtPw.setTextColor(Color.WHITE);
			edtPw.setTextColor(Color.BLACK);
		} else {
			edtPw.setTextColor(Color.BLACK);
		}
		if (box_check) {
			rememberMeCheckBox.setChecked(true);
		}

		edtId.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					if (edtId.hasFocus()) {
						edtPw.setFocusable(true);
						edtPw.requestFocus();
						return true;
					}
				}
				return false;
			}

		});

		edtPw.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					userID = edtId.getText().toString().trim();
					password = edtPw.getText().toString().trim();
					if (userID.contains(" ") || password.contains(" ")) {
						// Dialog
						AlertDialog.Builder builder = new AlertDialog.Builder(
								LoginActivity.this);
						builder.setTitle("Error:");
						builder.setMessage("Your username or password contains a space.");
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
					} else {
						if (userID.equals("") || password.equals("")) {
							if (edtId.getText().toString().equals("")) {
								Toast.makeText(getApplicationContext(),
										"Please Enter Username",
										Toast.LENGTH_SHORT).show();
							}
							if (edtPw.getText().toString().equals("")) {
								Toast.makeText(getApplicationContext(),
										"Please Enter Password",
										Toast.LENGTH_SHORT).show();
							}
						} else {
							if (isNetworkOnline()) {
								// login();
								AsyncHttpClient tmpclient = new AsyncHttpClient();
								tmpclient.get("http://www.google.com",
										new AsyncHttpResponseHandler() {
											@Override
											public void onSuccess(
													String response) {

												LoginTask loginTask = new LoginTask();
												loginTask.execute();
											}

											@Override
											public void onFailure(
													Throwable arg0, String arg1) {

												super.onFailure(arg0, arg1);

												AlertDialog.Builder builder = new AlertDialog.Builder(
														LoginActivity.this);
												builder.setTitle("Error");
												builder.setMessage("Internet connection is not available");
												builder.setCancelable(true);
												builder.setNeutralButton(
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

										});
							} else {
								AlertDialog.Builder builder = new AlertDialog.Builder(
										LoginActivity.this);
								builder.setTitle("Error");
								builder.setMessage("Internet connection is not available");
								builder.setCancelable(true);
								builder.setNeutralButton("Dismiss",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												dialog.dismiss();
											}
										});
								AlertDialog alert = builder.create();
								alert.show();
							}
							return true;
						}
					}
				}
				return false;
			}
		});

		btnLogin.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				if (edtId.getText().toString().equals("")
						|| edtPw.getText().toString().equals("")) {
					if (edtId.getText().toString().equals("")) {
						Toast.makeText(getApplicationContext(),
								"Please Enter Username", Toast.LENGTH_SHORT)
								.show();
					}
					if (edtPw.getText().toString().equals("")) {
						Toast.makeText(getApplicationContext(),
								"Please Enter Password", Toast.LENGTH_SHORT)
								.show();
					}
				} else {
					if (isNetworkOnline()) {
						// login();
						AsyncHttpClient tmpclient = new AsyncHttpClient();
						tmpclient.get("http://www.google.com",
								new AsyncHttpResponseHandler() {
									@Override
									public void onSuccess(String response) {

										Log.d("SUCCESS TMP CLIENT", response);
										LoginTask loginTask = new LoginTask();
										loginTask.execute();
									}

									@Override
									public void onFailure(Throwable arg0,
											String arg1) {

										super.onFailure(arg0, arg1);

										AlertDialog.Builder builder = new AlertDialog.Builder(
												LoginActivity.this);
										builder.setTitle("Error");
										builder.setMessage("Internet connection is not available");
										builder.setCancelable(true);
										builder.setNeutralButton(
												"Dismiss",
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int id) {
														dialog.dismiss();
													}
												});
										AlertDialog alert = builder.create();
										alert.show();
									}

								});
					} else {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								LoginActivity.this);
						builder.setTitle("Error");
						builder.setMessage("Internet connection is not available");
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
			}// end of onClick
		});
	}

	public void showAlert(String response) {
		pd.dismiss();
		AlertDialog.Builder builder = new AlertDialog.Builder(
				LoginActivity.this);
		
		Log.d("response","response "+response);

		if (response.contains("status-code") || response.contains("check-code")) {
			if (response.contains("status-code") && response.contains("500")) {
				builder.setTitle("Error Code 500");
			}
			String message = "This application requires a valid license."
					+ "Please contact your System Administrator for more information.";
			
/*			if(response.contains("success")){
			
				builder.setTitle("Update Plugin");
			    message = "Please update your plugin. Click Download to get the latest plugin";
			    
			    builder.setPositiveButton("Download",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
								
								Intent webintent = new Intent(
										Intent.ACTION_VIEW,
										Uri.parse("https://marketplace.atlassian.com/plugins/com.addteq.bamboo.plugin.addteq-bamboo-plugin"));
								startActivity(webintent);
								
								
							}
						});
			    
			    
			}*/
			
			if (response.contains("check-code")) {

				if (response.contains("110")) {
					// No License
					builder.setTitle("No License");
				}
				if (response.contains("120")) {
					// UPM version require update
					builder.setTitle("UPM update required");
				}
				if (response.contains("130")) {
					// Plugin remote agent check(if plugin allow remote agent
					// less
					// than 1 return error code 130)

					builder.setTitle("Invalid License");
					message = "Invalid License. Please enable the plugin and try again.";
				}
				if (response.contains("140")) {
					// Expired License
					builder.setTitle("Expired License");
				}
				// Type Mismatch (151-161)
				if (response.contains("151")) {
					// ACADEMIC 151
					builder.setTitle("License Type Mismatch");
				}
				if (response.contains("152")) {
					// COMMERCIAL 152
					builder.setTitle("License Type Mismatch");
				}
				if (response.contains("153")) {
					// COMMUNITY 153
					builder.setTitle("License Type Mismatch");
				}
				if (response.contains("154")) {
					// DEMONSTRATION 154
					builder.setTitle("License Type Mismatch");
				}
				if (response.contains("155")) {
					// DEVELOPER 155
					builder.setTitle("License Type Mismatch");
				}
				if (response.contains("156")) {
					// NON_PROFIT 156
					builder.setTitle("License Type Mismatch");
				}
				if (response.contains("157")) {
					// OPEN_SOURCE 157
					builder.setTitle("License Type Mismatch");
				}
				if (response.contains("158")) {
					// PERSONAL 158
					builder.setTitle("License Type Mismatch");
				}
				if (response.contains("159")) {
					// STARTER 159
					builder.setTitle("License Type Mismatch");
				}
				if (response.contains("160")) {
					// HOSTED 160
					builder.setTitle("License Type Mismatch");
				}
				if (response.contains("161")) {
					// TESTING 161
					builder.setTitle("License Type Mismatch");
				}
				if (response.contains("170")) {
					// User Mismatch 170
					builder.setTitle("User Mismatch");
				}
				if (response.contains("180")) {
					// Edition Mismatch 180
					builder.setTitle("Edition Mismatch");
				}
				if (response.contains("190")) {
					// Version Mismatch 190
					builder.setTitle("Version Mismatch");
				}
				if (response.contains("200")) {
					// Unknown 200
					builder.setTitle("Unknown License Error");
				}
			}
			builder.setMessage(message);
			builder.setCancelable(false);
			builder.setNeutralButton("Dismiss",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	/*
	 * private void checkCertificate(String ip) {
	 * 
	 * 
	 * String path = ""; URL url = null;
	 * 
	 * try { url = new URL(ip); } catch (MalformedURLException e2) {
	 * e2.printStackTrace(); } HttpURLConnection ucon = null; try {
	 * Log.e("IP Address Testing in certificate method",
	 * "ip is "+ip+"Before exception happens"); ucon = (HttpURLConnection)
	 * url.openConnection(); ucon.setInstanceFollowRedirects(true);
	 * 
	 * int responseCode = ucon.getResponseCode(); ucon.disconnect();
	 * 
	 * }
	 * 
	 * 
	 * catch(SSLHandshakeException e) {
	 * Log.e("Exception testing in certificate method", "1"); certificated =
	 * false;
	 * 
	 * 
	 * AlertDialog.Builder alertDialogBuilder = new
	 * AlertDialog.Builder(LoginActivity.this);
	 * 
	 * // set title
	 * alertDialogBuilder.setTitle("The site's security certificate is not trusted!"
	 * );
	 * 
	 * // set dialog message alertDialogBuilder
	 * .setMessage("You attempted to reach \n" + BAMBOOIPADD +
	 * "\n, but the server presented a certificate issued by an entity that is not trusted by your system. Do you want to continue? "
	 * ) .setCancelable(false) .setNegativeButton("Continue",new
	 * DialogInterface.OnClickListener() { public void onClick(DialogInterface
	 * dialog,int id) { certificated=true; dialog.cancel();
	 * 
	 * } }) .setPositiveButton("Cancel",new DialogInterface.OnClickListener() {
	 * public void onClick(DialogInterface dialog,int id) {
	 * newIPadd.setText(""); newPortadd.setText("");
	 * 
	 * } });
	 * 
	 * // create alert dialog AlertDialog alertDialog =
	 * alertDialogBuilder.create();
	 * 
	 * // show it alertDialog.show();
	 * 
	 * 
	 * 
	 * } catch(UnknownHostException e) {
	 * Log.e("Exception testing in certificate method", "2");
	 * e.printStackTrace(); }
	 * 
	 * catch (Exception e3){ Log.e("Exception testing in certificate method",
	 * "4"); e3.printStackTrace();
	 * 
	 * }
	 * 
	 * }
	 */

	private class MyAsyncTask2 extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPostExecute(String path) {
			// System.out.println("onPostExecute");
			// login();
			// Log.e("path", path);
			/*
			 * BAMBOOIPADD = pref.getString("IPADDRESS", BAMBOOIPADD); //
			 * Log.e("BAMBOOIPADD", BAMBOOIPADD); String check = "";
			 * if(BAMBOOIPADD.length()>path.length()){ check =
			 * BAMBOOIPADD.substring(BAMBOOIPADD.length()-path.length()); } //
			 * Log.e("check", check);
			 * if(!check.equalsIgnoreCase(path)&&!path.equalsIgnoreCase("")){ //
			 * Log.e("Added", "Added"); editor.putString("IPADDRESS",
			 * BAMBOOIPADD+path); editor.commit(); contextcheck = true; }
			 */

			pd.dismiss();
		}

		@Override
		protected void onPreExecute() {
			// System.out.println("onPreExecute");
			pd = new ProgressDialog(LoginActivity.this);
			pd.setMessage("Saving...");
			pd.setCancelable(false);
			pd.show();
		}

		@Override
		protected String doInBackground(Void... params) {
			BAMBOOIPADD = pref.getString("IPADDRESS", BAMBOOIPADD);
			String path = "";
			URL url = null;
			try {
				url = new URL(BAMBOOIPADD);
			} catch (MalformedURLException e2) {

				e2.printStackTrace();
			}
			HttpURLConnection ucon = null;
			try {
				ucon = (HttpURLConnection) url.openConnection();
				ucon.setInstanceFollowRedirects(true);
				System.out
						.println("path=" + ucon.getURL().getPath() + "\nIndex="
								+ ucon.getURL().getPath().lastIndexOf('/'));
				Log.e("IP Address Testing I-II", "IP is " + BAMBOOIPADD
						+ "Before exception happens ");

				// int responseCode = ucon.getResponseCode();
				// System.out.println("path="+ucon.getURL().getPath()+"\nIndex="+ucon.getURL().getPath().lastIndexOf('/'));
				if (ucon.getURL().getPath().lastIndexOf('/') > 0) {
					path = ucon
							.getURL()
							.getPath()
							.substring(0,
									ucon.getURL().getPath().lastIndexOf('/'));

				}

				ucon.disconnect();

			} catch (SSLHandshakeException e) {
				Log.e("Exception testing", "1");
				e.printStackTrace();

			} catch (UnknownHostException e) {
				Log.e("Exception testing", "2");
				e.printStackTrace();
			}

			catch (IOException e1) {

				Log.e("Exception testing", "3");
				e1.printStackTrace();
			} catch (Exception e3) {
				Log.e("Exception testing", "4");
				e3.printStackTrace();
			}

			return path;
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
	}

	private boolean isNetworkOnline() {
		boolean status = false;

		try {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getNetworkInfo(0);
			if ((netInfo != null)
					&& (netInfo.getState() == NetworkInfo.State.CONNECTED)) {
				status = true;
			} else {
				netInfo = cm.getNetworkInfo(1);
				if ((netInfo != null)
						&& (netInfo.getState() == NetworkInfo.State.CONNECTED))
					status = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return status;

	}

	public class LoginTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPostExecute(String path) {

			hideSoftKeyboard();
			if (path.startsWith("ERROR_")) {
				pd.dismiss();
				myCookieStore.clear();
				String errormessage = "Server is unavailable.";
				if (path.equalsIgnoreCase("ERROR_SSL")) {
					errormessage = "Server returned an unsecure response but it looks like you are using https. Please check your settings and try again.";
				} else if (path.equalsIgnoreCase("ERROR_NOT_VERIFIED")) {
					errormessage = "The Bamboo server that you are trying to access is not a valid Bamboo server.";
				} else if (path.equalsIgnoreCase("ERROR_HOST")) {
					errormessage = "The Server with the specified Hostname could not be found.";
				} else if (path.equalsIgnoreCase("ERROR_EOF")) {
					errormessage = "Server returned a secure response but it looks like you are using http. Please check your settings and try again.";
				} else if (path.equalsIgnoreCase("ERROR_CONNECT")) {
					errormessage = "Fail to connect to a remote address and port. Check your address and port number.";
				} else if (path.equalsIgnoreCase("ERROR_TIMEOUT")) {
					errormessage = "Request Timed out. Please try again.";
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(
						LoginActivity.this);
				builder.setTitle("Cannot reach the server");
				builder.setMessage(errormessage);
				builder.setCancelable(true);
				builder.setNeutralButton("Dismiss", null);
				AlertDialog alert = builder.create();
				alert.show();
			} else {
				String newaddress = path;
				String parseadd = "";
				if (newaddress.endsWith("/")) {
					parseadd = newaddress.substring(0,
							newaddress.lastIndexOf('/'));
				} else {
					parseadd = newaddress;
				}
				editor.putString("IPADDRESS", parseadd);

				editor.commit();
				//Log.d("ON POST EXECUTE 2", parseadd);
				// //contextcheck = true;
				login();
			}
		}

		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(LoginActivity.this);
			pd.setMessage("Signing In...");
			pd.setCancelable(false);
			pd.show();
		}

		@Override
		protected String doInBackground(Void... params) {
			String path = "";
			String SSLCHECK="";
			// if(contextcheck == false){

			Log.d("IN PROGRESS LOGGING IN", "start");
			BAMBOOIPADD = pref.getString("IPADDRESS", BAMBOOIPADD);
			URL url = null;
			try {
				url = new URL(BAMBOOIPADD);
				path = url.toString();
				HttpURLConnection ucon = null;
				try {

					SSLCHECK=pref.getString("SSLCHECK", "false");
					//description : if the sslcertificate for the bamboo url is not verified this SSLCHECK will be true. If SSLCHECK is true we do the SSL suppression. 
					//If not just get the path from the url and assign it to the path variable
					
					//Log.d("SSLCHECK","sslcheck : "+SSLCHECK);
					
					if(SSLCHECK.contains("true")){

						if (url.getProtocol().toLowerCase().equals("https")) {
					        trustAllHosts();
					        HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
					        https.setHostnameVerifier(DO_NOT_VERIFY);
					        ucon = https;
					    } else {
					        ucon = (HttpURLConnection) url.openConnection();
					    }

						
						//Log.d("IN PROGRESS LOGGING IN", "TURSTED ALL HOSTS");
						
						
					}
					else{
						
						
						//Log.d("PATH","path top : "+path);
						
						ucon = (HttpURLConnection) url.openConnection();
						ucon.setInstanceFollowRedirects(true);
						ucon.setConnectTimeout(LOGIN_TIMEOUT);
						int responseCode = ucon.getResponseCode();
						path = ucon.getURL().toString();
						if (responseCode == 301 || responseCode == 302) {
							URL send = ucon.getURL();
							ucon.disconnect();
							path = redirecturl(send).toString();
						}
						
						//Log.d("LOGINTASK","path doin background , before session id  : "+path);
						
						
						if(path.contains("sessionid")){
							path=BAMBOOIPADD+"/";
							//path=getPath(path);
						}
						
						
						Log.d("PATH","path bottom : "+path);
						
					}
						
					
				} catch (SocketTimeoutException e) {
					path = "ERROR_TIMEOUT";
					//Log.d("error","error + "+e.toString());
		
				} catch (SSLException e) {
					path = "ERROR_SSL";
					e.printStackTrace();
				} catch (ConnectException e) {
					path = "ERROR_CONNECT";
				} catch (EOFException e) {
					path = "ERROR_EOF";
				} catch (ProtocolException e) {
					path = "ERROR_EOF";
				} catch (UnknownHostException e) {
					path = "ERROR_HOST";
					e.printStackTrace();
				} catch (IOException e1) {

					if (e1.toString().contains("was not verified")) {
						path = "ERROR_NOT_VERIFIED";
					}
					Log.d("ERROR","error "+e1.toString());
					//e1.printStackTrace();
				} catch (Exception e3) {
					e3.printStackTrace();
				}
			} catch (MalformedURLException e2) {

				e2.printStackTrace();
			}

			return path;
		}
		
		//extract the path from the regenrated url
	/*	 private String getPath(String path) {
			 
			 int pathSize=path.length();
		     int count=0;
		     String temp="";
		     String returnPath="";
			 
			 char c[]=path.toCharArray();
			 
			 for(int i=0;i<pathSize;i++){
				 
				  if(c[i]=='/'){
                       					  
					  count=count+1;
					  if(count>=3){
						  temp=path.substring(i+1, i+9);
						  Log.d("temp path","temp path "+temp);
						  if(temp.equalsIgnoreCase("allPlans")){
							  
							    returnPath=path.substring(0, i);
							    Log.d("return path","return path "+returnPath);
							    return returnPath;	  
						  }	  	  
					  }
					  
				  } 	 
			 }
			 Log.d("return path","return path "+BAMBOOIPADD);
			 return BAMBOOIPADD;

		}*/

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
	                      myClient.setSSLSocketFactory(sf);
	              } catch (Exception e) {
	                      e.printStackTrace();
	              }
	      }

		
	}

	private class GuestLoginTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPostExecute(String path) {
			hideSoftKeyboard();
			
			Log.d("path-onPost","path "+path);
			

			if (path.startsWith("ERROR_")) {
				pd.dismiss();
				myCookieStore.clear();
				String errormessage = "Guest Login is not available for this server.";
				if (path.equalsIgnoreCase("ERROR_SSL")) {
					errormessage = "Server returned an unsecure response but it looks like you are using https. Please check your settings and try again.";
				} else if (path.equalsIgnoreCase("ERROR_NOT_VERIFIED")) {
					errormessage = "The Bamboo server that you are trying to access is not a valid Bamboo server.";
				} else if (path.equalsIgnoreCase("ERROR_HOST")) {
					errormessage = "The Server with the specified Hostname could not be found.";
				} else if (path.equalsIgnoreCase("ERROR_EOF")) {
					errormessage = "Server returned a secure response but it looks like you are using http. Please check your settings and try again.";
				} else if (path.equalsIgnoreCase("ERROR_CONNECT")) {
					errormessage = "Fail to connect to a remote address and port. Check your address and port number.";
				} else if (path.equalsIgnoreCase("ERROR_TIMEOUT")) {
					errormessage = "Request Timed out. Please try again.";
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(
						LoginActivity.this);
				builder.setTitle("Error");
				builder.setMessage(errormessage);
				builder.setCancelable(true);
				builder.setNeutralButton("Dismiss", null);
				AlertDialog alert = builder.create();
				alert.show();
			} else {
				String parseadd = "";
				if (path.endsWith("/")) {
					parseadd = path.substring(0, path.lastIndexOf('/'));
				} else {
					parseadd = path;
				}

				editor = pref.edit();
				editor.putString("IPADDRESS", parseadd);
				editor.putString("userType", "guest");
				editor.remove("encryptedid");

				// editor.putBoolean("GuestLogin", true);
				editor.commit();
				// contextcheck = true;
				
				Log.d("checkGuestLogin","checkGuestLogin" +parseadd);

				checkGuestLogin(parseadd);

			}
		}

		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(LoginActivity.this);
			pd.setMessage("Signing In...");
			pd.setCancelable(false);
			pd.show();
		}

		@Override
		protected String doInBackground(Void... params) {
			String path = "";
			String SSLCHECK="";
			// if(contextcheck == false){
			BAMBOOIPADD = pref.getString("IPADDRESS", BAMBOOIPADD);
			URL url = null;
			try {
				url = new URL(BAMBOOIPADD);
				path = url.toString();
				HttpURLConnection ucon = null;
				try {
					
					SSLCHECK=pref.getString("SSLCHECK", "false");
					//description : if the sslcertificate for the bamboo url is not verified this SSLCHECK will be true. If SSLCHECK is true we do the SSL suppression. 
					//If not just get the path from the url and assign it to the path variable
					
					//Log.d("SSLCHECK","sslcheck "+SSLCHECK);
					
					if(SSLCHECK.contains("true")){

						if (url.getProtocol().toLowerCase().equals("https")) {
					        trustAllHosts();
					        HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
					        https.setHostnameVerifier(DO_NOT_VERIFY);
					        ucon = https;
					    } else {
					        ucon = (HttpURLConnection) url.openConnection();
					    }
						
						Log.d("path-session","path with sesssion-ssl true "+path);
						
						if(path.contains("sessionid")){
							
							path=BAMBOOIPADD;
							
						}
						
						//Log.d("IN PROGRESS LOGGING IN - GuestLoginTask", "TURSTED ALL HOSTS doInBackground");
	
					}
					else{
						
						ucon = (HttpURLConnection) url.openConnection();
						ucon.setInstanceFollowRedirects(true);
						ucon.setConnectTimeout(LOGIN_TIMEOUT);
						int responseCode = ucon.getResponseCode();
						path = ucon.getURL().toString();

						if (responseCode == 301 || responseCode == 302) {
							URL send = ucon.getURL();
							ucon.disconnect();
							path = redirecturl(send).toString();
						} else if (responseCode == 504) {
							path = "ERROR_HTTPS_SITE";
						}
						
						Log.d("path-session","path with sesssion-ssl false "+path);
						
						if(path.contains("sessionid")){
							
							path=getPath(path);
							
							//path=BAMBOOIPADD;
							
						}
						
					}

				} catch (SocketTimeoutException e) {
					path = "ERROR_TIMEOUT";
				} catch (SSLException e) {
					path = "ERROR_SSL";
					e.printStackTrace();
				} catch (ConnectException e) {
					path = "ERROR_CONNECT";
				} catch (EOFException e) {
					path = "ERROR_EOF";
				} catch (UnknownHostException e) {
					path = "ERROR_HOST";
				} catch (IOException e1) {

					if (e1.toString().contains("was not verified")) {
						path = "ERROR_NOT_VERIFIED";
					}
					e1.printStackTrace();
				} catch (Exception e3) {
					Log.d("EXCEPTION","ex "+e3.toString());
					e3.printStackTrace();
				}
			} catch (MalformedURLException e2) {
				e2.printStackTrace();
			}
			
			//Log.d("path-doInBackground","path "+path);
			
			return path;
		}

		//extract the path from the regenrated url
		 private String getPath(String path) {
			 
			 int pathSize=path.length();
		     int count=0;
		     String temp="";
		     String returnPath="";
			 
			 char c[]=path.toCharArray();
			 
			 for(int i=0;i<pathSize;i++){
				 
				  if(c[i]=='/'){
                        					  
					  count=count+1;
					  if(count>=3){
						  temp=path.substring(i+1, i+9);
						  Log.d("temp path","temp path "+temp);
						  if(temp.equalsIgnoreCase("allPlans")){
							  
							    returnPath=path.substring(0, i);
							    Log.d("return path","return path "+returnPath);
							    return returnPath;	  
						  }	  	  
					  }
					  
				  } 	 
			 }
			 Log.d("return path","return path "+BAMBOOIPADD);
			 return BAMBOOIPADD;

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
	                      myClient.setSSLSocketFactory(sf);
	              } catch (Exception e) {
	                      e.printStackTrace();
	              }
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
	
	

	protected URL redirecturl(final URL oldurl) {

		URL resulturl = null;
		URL send = null;
		HttpURLConnection ucon;
		try {
			ucon = (HttpURLConnection) oldurl.openConnection();
			ucon.setInstanceFollowRedirects(true);
			int responseCode2 = ucon.getResponseCode();
			resulturl = ucon.getURL();
			if (responseCode2 == 301 || responseCode2 == 302) {
				send = ucon.getURL();
				ucon.disconnect();
				resulturl = redirecturl(send);
			}

		} catch (IOException e) {

			e.printStackTrace();
		}

		return resulturl;
	}

	public void checkGuestLogin(String IPAddy) {

		String IPBASEURL = "";
		String BASEURL = "/rest/api/latest/";
		if (IPAddy.endsWith("/")) {
			IPBASEURL = IPAddy + "rest/api/latest/";
		} else {
			IPBASEURL = IPAddy + BASEURL;
		}
		String serverCallURL = IPBASEURL
				+ "project.json?expand=projects.project.plans";
		
		URL url = null;
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
			//Log.d("IN PROGRESS LOGGING IN -checkGuestLogin", "TURSTED ALL HOSTS");
			
			
		}
		
		Log.d("serverCallUrl","serverCallUrl : "+serverCallURL);
		
		asyncHttpClient.setTimeout(LOGIN_TIMEOUT);
		asyncHttpClient.get(serverCallURL, new AsyncHttpResponseHandler() {

			public void onSuccess(String response) {
				Intent myintent = new Intent(LoginActivity.this,
						ProjectListActivity.class);

				// check GuestLogin SharedPref to true to preserve session
				editor = pref.edit();
				editor.putBoolean("GuestLogin", true);
				editor.commit();

				myintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(myintent);
				Intent myintent2 = new Intent(LoginActivity.this,
						NotificationService.class);
				myintent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startService(myintent2);
			}

			public void onFailure(Throwable e, String response) {
				try {
					pd.dismiss();
					myCookieStore.clear();
					hideSoftKeyboard();
					e.printStackTrace();
					
					Log.d("inside-checkGuestLogin","error "+e.toString());

					if (e.toString().contains("SocketTimeoutException")) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								LoginActivity.this);
						builder.setTitle("Error");
						builder.setMessage("Request Timed out. Please try again.");
						builder.setCancelable(true);
						builder.setNeutralButton("Dismiss", null);
						AlertDialog alert = builder.create();
						alert.show();
					} else if (e.toString().contains("HttpResponseException")) {

						if (e.toString().contains("Not Found")) {

							AlertDialog.Builder alt_bld = new AlertDialog.Builder(
									LoginActivity.this);
							alt_bld.setTitle("Error");
							alt_bld.setMessage("The Bamboo server that you are trying to access is not a valid Bamboo server.");
							alt_bld.setCancelable(false);
							alt_bld.setNeutralButton("Dismiss", null);
							alt_bld.show();
						} else if (e.toString().contains("Unauthorized")) {
							AlertDialog.Builder alt_bld = new AlertDialog.Builder(
									LoginActivity.this);
							alt_bld.setTitle("Error");
							alt_bld.setMessage("Guest Login is not available for this server.");
							alt_bld.setCancelable(false);
							alt_bld.setNegativeButton("Dismiss", null);
							alt_bld.show();
						} else {
							AlertDialog.Builder alt_bld = new AlertDialog.Builder(
									LoginActivity.this);
							alt_bld.setTitle("Error");
							alt_bld.setMessage("Guest Login is not available for this server.");
							alt_bld.setCancelable(false);
							alt_bld.setNegativeButton("Dismiss", null);
							alt_bld.show();
						}
					} else {
						AlertDialog.Builder alt_bld = new AlertDialog.Builder(
								LoginActivity.this);
						alt_bld.setTitle("Error");
						alt_bld.setMessage("Guest Login is not available for this server.");
						alt_bld.setCancelable(false);
						alt_bld.setNegativeButton("Dismiss", null);
						alt_bld.show();
					}
				} catch (Exception error) {
					error.printStackTrace();
				}
			}

		});
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
    public void trustAllHosts() {

              // Install the all-trusting trust manager
              try {
            	  KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                  trustStore.load(null, null);
                  MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
                  sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                      asyncHttpClient.setSSLSocketFactory(sf);
              } catch (Exception e) {
                      e.printStackTrace();
              }
      }

}