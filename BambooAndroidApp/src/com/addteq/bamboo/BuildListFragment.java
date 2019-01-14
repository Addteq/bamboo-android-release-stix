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
import java.util.StringTokenizer;

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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.addteq.stix.R;
import com.atlassian.jconnect.droid.Api;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;

public class BuildListFragment extends SherlockFragment {
	static int mNum;
	static String temp;
	ListView listView;
	private PullToRefreshListView mPullRefreshListView;
	String planKey;
	String buildJSONStr = "";
	private JSONObject tempJsonObject;
	private ArrayAdapter<BuildListItem> testingAdapter;
	private ArrayList<BuildListItem> testBuildList;
	private ArrayList<String> trigger_list;
	private ArrayList<String> completed_list;
	private ArrayList<String> duration_list;
	private ArrayList<String> relative_list;
	private ArrayList<String> buildkey_list;
	private ArrayList<String> revision_list;
	private ArrayList<Integer> status_list;
	private ArrayList<String> art_list;
	private ArrayList<String> art_download_list;
	private int[] buildDurations = new int[25];
	private int[] statusChecks = new int[25];
	private int successCount = 0, totalcount = 0;
	public String userType;
	private PersistentCookieStore myCookieStore;
	private static AsyncHttpClient client = new AsyncHttpClient();
	private static String IPPOST = "";
	private static String IPAdd = "";
	private static String POST_URL = "/rest/api/latest/queue/";
	private static String BASEURL = "/rest/api/latest/";
	private String IPBASEURL = "";
	private static String TAG = "BuildFragment";
	//private ImageView sendfeedback;
	private int LOGIN_TIMEOUT = 40 * 1000;
	private ProgressDialog mDialog;
	private Context context; 

	SharedPreferences pref;
	SharedPreferences.Editor editor;

	private String projectName = "";
	private String planName = "";

	static BuildListFragment newInstance(int index) {
		BuildListFragment f = new BuildListFragment();
		Bundle args = new Bundle();
		args.putInt("num", index);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle projectAndPlanName = getArguments();
		projectName = projectAndPlanName.getString("projectName");
		planName = projectAndPlanName.getString("planName");

		setHasOptionsMenu(true);
		planKey = getTag();
		testBuildList = new ArrayList<BuildListItem>();
		trigger_list = new ArrayList<String>();
		completed_list = new ArrayList<String>();
		duration_list = new ArrayList<String>();
		relative_list = new ArrayList<String>();
		buildkey_list = new ArrayList<String>();
		revision_list = new ArrayList<String>();
		status_list = new ArrayList<Integer>();
		art_list = new ArrayList<String>();
		art_download_list = new ArrayList<String>();
		testingAdapter = new BuildListAdapter(getActivity(), testBuildList);
		UncaughtExceptionHandler mUEHandler = new Thread.UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				e.printStackTrace();
				Api.handleException(e);
				getActivity().finish();
			}
		};
		Thread.setDefaultUncaughtExceptionHandler(mUEHandler);
		pref = PreferenceManager
				.getDefaultSharedPreferences(getSherlockActivity());
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
		myCookieStore = new PersistentCookieStore(getSherlockActivity());
		myCookieStore.getCookies();
		mDialog = new ProgressDialog(getSherlockActivity());
		client.setCookieStore(myCookieStore);
		client.addHeader("X-Atlassian-Token", "nocheck");
		
		if (this.isNetworkOnline()) {
			mDialog = ProgressDialog.show(getSherlockActivity(), "",
					"Data is Loading...");
			getJSONString();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					getSherlockActivity());
			builder.setTitle("Error");
			builder.setMessage("Internet connection is not available");
			builder.setCancelable(true);
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
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.buildlist, container, false);
		mPullRefreshListView = (PullToRefreshListView) v
				.findViewById(R.id.pull_refresh_list);

		//sendfeedback = (ImageView) v.findViewById(R.id.feedback);
		/*sendfeedback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(Api.createFeedbackIntent(getSherlockActivity()));
			}
		});*/

		// Set a listener to be invoked when the list should be refreshed.
		mPullRefreshListView
				.setOnRefreshListener(new OnRefreshListener<ListView>() {
					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						mPullRefreshListView.setLastUpdatedLabel(DateUtils
								.formatDateTime(getSherlockActivity(),
										System.currentTimeMillis(),
										DateUtils.FORMAT_SHOW_TIME
												| DateUtils.FORMAT_SHOW_DATE
												| DateUtils.FORMAT_ABBREV_ALL));
						if (isNetworkOnline()) {
							// Do work to refresh the list here.
							// Clear previous Array...
							successCount = 0;
							listView.setEnabled(false);
							getJSONString();
						} else {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									getSherlockActivity());
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
							testingAdapter.notifyDataSetChanged();
							mPullRefreshListView.onRefreshComplete();
						}
					}
				});
		// Add an end-of-list listener
		mPullRefreshListView
				.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

					@Override
					public void onLastItemVisible() {
						Toast.makeText(getSherlockActivity(), "End of List!",
								Toast.LENGTH_SHORT).show();
					}
				});

		listView = mPullRefreshListView.getRefreshableView();
		listView.setAdapter(testingAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
			//	Toast.makeText(context, "hello", Toast.LENGTH_SHORT).show();
				BuildListItem bli = (BuildListItem) parent
						.getItemAtPosition(position);
				// TODO here is where to insert
				Intent intent = new Intent();

				intent.setClass(getSherlockActivity(), BuildInfoActivity.class);
				
				// To send the project name and plan name into the
				// BuildInfoActivity
				intent.putExtra("projectName", projectName);
				intent.putExtra("planName", planName);

				intent.putExtra("trigger", trigger_list);
				intent.putExtra("build_no", bli.getBuildTitle());
				intent.putExtra("build_keys", buildkey_list);
				intent.putExtra("revisions", revision_list);
				intent.putExtra("completed_times", completed_list);
				intent.putExtra("durations", duration_list);
				intent.putExtra("relative_times", relative_list);
				intent.putExtra("status", status_list);
				intent.putExtra("artlinklist", art_list);
				intent.putExtra("artdownloadlist", art_download_list);
				intent.putExtra("plankey", planKey);
				Log.d(TAG, "planKey: " + planKey.toString());
			/*	if(status_list.get(position)==0){
					//sendfeedback.setImageResource(R.drawable.bug_small);
					Toast.makeText(context, "success", Toast.LENGTH_SHORT).show();
				}
				else if(status_list.get(position)==1){
					//sendfeedback.setImageResource(R.drawable.bug_red);
					Toast.makeText(context, "fail", Toast.LENGTH_SHORT).show();
				}
				else {
					//sendfeedback.setImageResource(R.drawable.bug_yellow);
				}
				intent.putExtra("status", status_list);*/

				// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				
			}
		});

		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {		
		inflater.inflate(R.menu.buildlistfragment_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.graph_menu) {
			Intent intent = new Intent();
			intent.setClass(getSherlockActivity(), BuildSummaryActivity.class);
			intent.putExtra("durations", buildDurations);
			intent.putExtra("successCount", successCount);
			intent.putExtra("status", statusChecks);
			intent.putExtra("plan_key", planKey);
			intent.putExtra("totalcount", totalcount);
			// intent.putExtra("title", );
			startActivity(intent);
			return true;
		} else if (itemId == R.id.startbuild_menu) {
			AlertDialog.Builder alt_bld = new AlertDialog.Builder(
					getSherlockActivity());
			alt_bld.setTitle("Confirm Build:");
			alt_bld.setMessage(
					"Are you sure you want to start " + planKey + "?")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									
									Log.d(TAG, "serverUrl :"+IPPOST + planKey);
									
									
									final String serverURL = IPPOST + planKey;
									if (isNetworkOnline()) {
										client.post(serverURL,
												new AsyncHttpResponseHandler() {
													@Override
													public void onSuccess(
															String response) {
														Toast.makeText(
																getSherlockActivity(),
																planKey
																		+ " build has been added to build queue",
																Toast.LENGTH_SHORT)
																.show();
													}

													@Override
													public void onFailure(
															Throwable e,
															String response) {
														
														if (e.toString().contains("Unauthorized")) {
																AlertDialog.Builder builder = new AlertDialog.Builder(
																		getSherlockActivity());
																builder.setTitle("Unauthorized Access.");
																builder.setMessage("You do not have permission to start this build.");
																builder.setCancelable(true);
																builder.setIcon(R.drawable.failure_icon);
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
														} else {
															Log.d(TAG, "error response :"+ e.toString());
															AlertDialog.Builder builder = new AlertDialog.Builder(
																	getSherlockActivity());
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
									} else {
										Toast.makeText(
												getSherlockActivity(),
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
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	public void parseJsonArt() {
		tempJsonObject = null;
		try {
			tempJsonObject = new JSONObject(buildJSONStr);
			String href = null;
			JSONObject temp1 = tempJsonObject.getJSONObject("results");
			Log.d(TAG, "temp1: " + temp1.toString());
			JSONArray temp2 = temp1.getJSONArray("result");
			Log.d(TAG, "temp2: " + temp2.toString());
			for (int count = 0; count < temp2.length(); count++) {
				JSONObject tmpObject = temp2.getJSONObject(count);
				JSONObject artObject = tmpObject.optJSONObject("artifacts");
				String size = artObject.optString("size");
				int sizeInt = Integer.parseInt(size);
				if (sizeInt > 0) {
					JSONArray artArray = artObject.optJSONArray("artifact");
					JSONObject tmpObject2 = artArray.optJSONObject(0);
					JSONObject linkObject = tmpObject2.optJSONObject("link");
					href = linkObject.optString("href");
					art_list.add(href);
					// TODO add to art_download_list
				}
				if (sizeInt == 0) {
					href = "0";
					art_list.add(href);
					// TODO add to art_download_list
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception error) {
			error.printStackTrace();
		}

	}

	public void parseJson() {
		tempJsonObject = null;
		testingAdapter.clear();
		try {
			tempJsonObject = new JSONObject(buildJSONStr);
			JSONObject temp1 = tempJsonObject.getJSONObject("results");
			JSONArray temp2 = temp1.getJSONArray("result");
			totalcount = temp2.length();
			for (int count = 0; count < temp2.length(); count++) {

				BuildListItem tempItem = new BuildListItem();
				String temp = "";
				temp = temp2.getJSONObject(count).getString("key")
						.replace(planKey + "-", "");
				tempItem.setBuildKey(temp);

				tempItem.setBuildTitle(temp2.getJSONObject(count).optString(
						"number"));

				buildkey_list.add(tempItem.getBuildTitle());
				revision_list.add(temp2.getJSONObject(count).optString("vcsRevisionKey"));

				completed_list.add(temp2.getJSONObject(count).optString(
						"prettyBuildCompletedTime"));
				duration_list.add(temp2.getJSONObject(count).optString(
						"buildDurationDescription"));
				relative_list.add(temp2.getJSONObject(count).optString(
						"buildRelativeTime"));

				tempItem.setBuildDuration(temp2.getJSONObject(count).optInt(
						"buildDurationInSeconds"));
				buildDurations[count] = tempItem.getBuildDuration();
				tempItem.setCheck(temp2.getJSONObject(count).optString("state"));
				if (temp2.getJSONObject(count).optString("state")
						.equals("Successful")) {
					successCount++;
					statusChecks[count] = 0;
					status_list.add(0);
				} else {
					if (temp2.getJSONObject(count).optString("state")
							.equals("Failed")) {
						statusChecks[count] = 1;
						status_list.add(1);
					} else {
						// unknowncount++;
						statusChecks[count] = 2;
						status_list.add(2);
					}
				}

				tempItem.setTime(temp2.getJSONObject(count).optString(
						"buildRelativeTime"));

				String totalString = temp2.getJSONObject(count).optString(
						"buildReason");
				String tempStr = "";
				String result = "";
				if (totalString.contains("Manual build by ")) {
					tempStr = totalString.replace("Manual build by ", "");
					result = "Manual build by ";
				} else {
					if (totalString.contains("Updated by")) {
						tempStr = totalString.replace("Updated by ", "");
						result = "Updated by ";
					} else {
						if (totalString.contains("<")) {
							int tempos = totalString.indexOf("<");
							result = totalString.substring(0, tempos);
							tempStr = totalString.substring(tempos);
						} else {
							result = temp2.getJSONObject(count).optString(
									"buildReason");
						}
					}
				}

				ArrayList<String> uniqueNames = new ArrayList<String>();
				if (!tempStr.equals("")) {
					StringTokenizer st = new StringTokenizer(tempStr, ",");
					while (st.hasMoreTokens()) {
						String noHTMLString = st.nextToken().replaceAll(
								"\\<.*?\\>", "");
						while (noHTMLString.substring(0, 1).equals(" ")) {
							noHTMLString = noHTMLString.substring(1);
						}
						if (noHTMLString.contains("&gt")) {
							noHTMLString = noHTMLString.replaceAll("&gt", "");
						}
						if (noHTMLString.contains("&lt")) {
							noHTMLString = noHTMLString.replaceAll("&lt", "");
						}
						String EMAIL_PATTERN = "([^.@\\s]+)(\\.[^.@\\s]+)*@([^.@\\s]+\\.)+([^.@\\s]+)";
						noHTMLString = noHTMLString.replaceAll(EMAIL_PATTERN,
								"");
						if (noHTMLString.contains(";")) {
							noHTMLString = noHTMLString.substring(0,
									noHTMLString.indexOf(";"));
						}
						if (!uniqueNames.contains(noHTMLString)) {
							uniqueNames.add(noHTMLString);
						}
					}
				}
				for (int count2 = 0; count2 < uniqueNames.size(); count2++) {
					result = result.concat(uniqueNames.get(count2)) + ", ";
				}
				if (result.length() > 2) {
					if (result.substring(result.length() - 2).equals(", ")) {
						result = result.substring(0, result.length() - 2);
					}
				}

				tempItem.setReasonString(result);
				trigger_list.add(tempItem.getReasonString());
				testingAdapter.add(tempItem);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// return result;
		
		//taking the dialogbox out when the build results are loaded to the fragment
		mDialog.dismiss();
	}

	public void getJSONString() {
		String temp = null;
		if (IPBASEURL.contains("rest/api/latest/")) {
			temp = IPBASEURL + "result/" + planKey
			+ ".json?expand=results.result";
			Log.d("url", ""+temp);
		}
		final String serverCallURL = temp;
				
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
			Log.d("IN PROGRESS LOGGING IN", "TURSTED ALL HOSTS");
			
			
		}
		

		client.get(serverCallURL, new AsyncHttpResponseHandler() {
			public void onSuccess(String response) {
				buildJSONStr = response;
				parseJson();
				getJSONArtString();
			}

			public void onFailure(Throwable e, String response) {
				try {
					e.printStackTrace();

					if (e.toString().contains("HttpResponseException")
							&& e.toString().contains("Unauthorized")) {
						loggingIn(0);
					} else {
						if (mDialog.isShowing()) {
							try {
								mDialog.dismiss();
							} catch (Exception dialogException) {
							}
						}
						AlertDialog.Builder alt_bld = new AlertDialog.Builder(
								getSherlockActivity());
						alt_bld.setTitle("Error");
						alt_bld.setMessage("Server Error or Unavailable, Please logout!");
						alt_bld.setCancelable(false);
						alt_bld.setNeutralButton("Logout",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										myCookieStore.clear();
										editor.remove("loggedin");
										editor.remove("GuestLogin");
										editor.commit();
										Intent loginIntent = new Intent(
												getSherlockActivity(),
												LoginActivity.class);
										startActivity(loginIntent);
										Intent intent3 = new Intent(
												getSherlockActivity(),
												NotificationService.class);
										intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										getSherlockActivity().stopService(
												intent3);
									}
								});
						alt_bld.setNegativeButton("Dismiss",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
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

	
	
	public void retryGetJSONString() {

		if (this.isNetworkOnline()) {
			mDialog = ProgressDialog.show(getSherlockActivity(), "",
					"Data is Loading...");
			String temp = null;
			if (IPBASEURL.contains("rest/api/latest/")) {
				temp = IPBASEURL + "result/" + planKey
				+ ".json?expand=results.result";
			}
			final String serverCallURL = temp;
			client.get(serverCallURL, new AsyncHttpResponseHandler() {
				public void onSuccess(String response) {
					buildJSONStr = response;
					parseJson();
					getJSONArtString();
				}

				public void onFailure(Throwable e, String response) {
					try {
						e.printStackTrace();

						if (mDialog.isShowing()) {
							try {
								mDialog.dismiss();
							} catch (Exception dialogException) {
							}
						}
						AlertDialog.Builder alt_bld = new AlertDialog.Builder(
								getSherlockActivity());
						alt_bld.setTitle("Error");
						alt_bld.setMessage("Server Error or Unavailable, Please logout!");
						alt_bld.setCancelable(false);
						alt_bld.setNeutralButton("Logout",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										myCookieStore.clear();
										editor.remove("loggedin");
										editor.remove("GuestLogin");
										editor.commit();
										Intent loginIntent = new Intent(
												getSherlockActivity(),
												LoginActivity.class);
										startActivity(loginIntent);
										Intent intent3 = new Intent(
												getSherlockActivity(),
												NotificationService.class);
										intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										getSherlockActivity().stopService(
												intent3);
									}
								});
						alt_bld.setNegativeButton("Dismiss",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
						alt_bld.show();
					} catch (Exception error) {
						error.printStackTrace();
					}
				}
			});
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					getSherlockActivity());
			builder.setTitle("Error");
			builder.setMessage("Internet connection is not available");
			builder.setCancelable(true);
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

	public void getJSONArtString() {
		String serverCallURL = IPBASEURL + "result/" + planKey
				+ ".json?expand=results.result.artifacts";
		Log.d(TAG, "CallURL: " + serverCallURL.toString());
		client.get(serverCallURL, new AsyncHttpResponseHandler() {
			public void onSuccess(String response) {
				buildJSONStr = response;
				Log.d(TAG, "response: " + response.toString());
				parseJsonArt();
				try {
					if (mDialog.isShowing()) {
						try {
							mDialog.dismiss();
						} catch (Exception dialogException) {
						}
					}
				} catch (Exception error) {
					error.printStackTrace();
				}
				testingAdapter.notifyDataSetChanged();
				mPullRefreshListView.onRefreshComplete();
				listView.setEnabled(true);
			}

			public void onFailure(Throwable e, String response) {
				try {
					e.printStackTrace();

					if (mDialog.isShowing()) {
						try {
							mDialog.dismiss();
						} catch (Exception dialogException) {
						}
					}
					AlertDialog.Builder alt_bld = new AlertDialog.Builder(
							getSherlockActivity());
					alt_bld.setTitle("Error");
					alt_bld.setMessage("Server Error or Unavailable, Please logout!");
					alt_bld.setCancelable(false);
					alt_bld.setNeutralButton("Logout",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									myCookieStore.clear();
									editor.remove("loggedin");
									editor.remove("GuestLogin");
									editor.commit();
									Intent loginIntent = new Intent(
											getSherlockActivity(),
											LoginActivity.class);
									startActivity(loginIntent);
									Intent intent3 = new Intent(
											getSherlockActivity(),
											NotificationService.class);
									intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									getSherlockActivity().stopService(intent3);
								}
							});
					alt_bld.setNegativeButton("Dismiss",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
					alt_bld.show();
				} catch (Exception error) {
					error.printStackTrace();
				}
			}
		});
	}

	private boolean isNetworkOnline() {
		boolean status = false;
		try {
			ConnectivityManager cm = (ConnectivityManager) getSherlockActivity()
					.getSystemService(Context.CONNECTIVITY_SERVICE);
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

	public void loggingIn(final int type) {

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
					+ "rest/addteqrest/1.0/check.json";
		} else {
			license = BAMBOOIPADD
					+ "/rest/addteqrest/1.0/check.json";
		}
		String licenseLogin = license;
		myClient.setTimeout(LOGIN_TIMEOUT);
		myClient.get(licenseLogin, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {

				if (response.contains("check-code")
						&& response.contains("success")) {

					if (type == 0) {
						retryGetJSONString();
					} else {
						retryPost();
					}
				} else {

					if (type == 0) {
						if (mDialog.isShowing()) {
							try {
								mDialog.dismiss();
							} catch (Exception dialogException) {
							}
						}
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(
							getSherlockActivity());
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
				Log.e("Throwable e   ", e.toString());
				if (type == 0) {
					if (mDialog.isShowing()) {
						try {
							mDialog.dismiss();
						} catch (Exception dialogException) {
						}
					}
				}
				if (e.toString().contains("SocketTimeoutException")
						|| e.toString().contains("ConnectException")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							getSherlockActivity());
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
							getSherlockActivity());
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

	protected void retryPost() {
		String serverURL = IPPOST + planKey;
		if (isNetworkOnline()) {
			client.post(serverURL, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String response) {
					Toast.makeText(getActivity(),
							planKey + " build has been added to build queue",
							Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onFailure(Throwable e, String response) {
					Log.d("ERROR for build", e.toString());
					if (e.toString().contains("Unauthorized")) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								getActivity());
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
								getActivity());
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
			Toast.makeText(getActivity(),
					"Internet connection is not available", Toast.LENGTH_SHORT)
					.show();
		}
	}
}
