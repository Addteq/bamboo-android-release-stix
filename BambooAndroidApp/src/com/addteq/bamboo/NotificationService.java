package com.addteq.bamboo;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.addteq.stix.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;

public class NotificationService extends Service {
	private static Timer timer;
	private Context ctx;
	AsyncHttpClient httpClient = new AsyncHttpClient();
	String IPAdd = "";
	String server = "";
	private final String URL = "/rest/api/latest/result.json?expand=results.result";
	//private final String LOGGING_TAG = "NotificationService";
	public static String BASEURL = "/rest/api/latest/";
	SharedPreferences pref;
	SharedPreferences.Editor editor;
	ArrayList<String> keys;
	int failNo = 0;
	int successNo = 0;
	Integer total = 0;
	String planName = "";

	// we probably don't need this 'keys' arraylist.
	// ArrayList<String> keys = new ArrayList<String>();

	// HashMap<String, Integer> map = new HashMap<String, Integer>();
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public void onCreate() {
		timer = new Timer();
		super.onCreate();
		ctx = this;
		startService();
	}

	private void startService() {
		pref = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		editor = pref.edit();
		IPAdd = pref.getString("IPADDRESS", IPAdd);
		editor.putString("IPADDRESS", IPAdd);
		editor.commit();
		if(IPAdd.endsWith("/")){
			server = IPAdd + "rest/api/latest/result.json?expand=results.result";
		}else{
			server = IPAdd + URL;
		}
		PersistentCookieStore myCookieStore = new PersistentCookieStore(ctx);
		myCookieStore.getCookies();
		httpClient.setCookieStore(myCookieStore);
		timer.scheduleAtFixedRate(new mainTask(), 0, 5 * 60 * 1000);
		// timer.scheduleAtFixedRate(new mainTask(), 0, 2 * 60 * 1000); //
		// 60*1000 = 1
		// min
	}

	private class mainTask extends TimerTask {
		String intentKey = "";

		@SuppressWarnings("deprecation")
		public void run() {
			httpClient.get(server, new AsyncHttpResponseHandler() {

				@Override
				public void onFailure(Throwable error, String content) {
					// TODO Auto-generated method stub
					super.onFailure(error, content);
				}
				@Override
				public void onSuccess(String response) {
					JSONObject jBuildObject;
					try {
						jBuildObject = new JSONObject(response);
						// parseAllBuildsJSON(jObject);
						try {
							JSONObject jResult = jBuildObject
									.getJSONObject("results");
							JSONArray jResultArray = jResult
									.getJSONArray("result");
							for (int i = 0; i < jResultArray.length(); i++) {
								JSONObject jObject = jResultArray
										.getJSONObject(i);
								//Integer no = jObject.getInt("number");

								String state = jObject.getString("state");
								String buildRelativeTime = jObject
										.getString("buildRelativeTime");
								if (buildRelativeTime.contains("minute")
										&& Integer.parseInt(buildRelativeTime
												.substring(0, buildRelativeTime
														.indexOf(" "))) <= 60) {
									String tmp = jObject.getString("planName");
									planName = planName + "," + tmp;
									total++;
									intentKey = jObject.getString("key");
									if (state.equalsIgnoreCase("Successful")) {
										successNo++;
									}
									if (state.equalsIgnoreCase("Failed")) {
										failNo++;
									}
								}
							}
							planName = planName.substring(1, planName.length());

							// key = key.substring(0,key.length()-1);
							if (total == 0)
								return;
							if (total == 1) {
								String IPBASEURL;
								if(IPAdd.endsWith("/")){
									IPBASEURL = IPAdd + "rest/api/latest/";
								}else{
									IPBASEURL = IPAdd + BASEURL;
								}
								String serverCallURL = IPBASEURL
										+ "project.json?expand=projects.project.plans";
								httpClient.get(serverCallURL,
										new AsyncHttpResponseHandler() {
											
											public void onSuccess(
													String response) {
												try {//
													JSONObject projectJson = new JSONObject(
															response);
													parseProjectJSON(projectJson);
													Intent notificationIntent;
													PendingIntent contentIntent;
													notificationIntent = new Intent(
															ctx,
															BuildListActivity.class);

													notificationIntent
															.putExtra(
																	"plan_key",
																	intentKey);
													notificationIntent
															.putExtra("keys",
																	keys);
													contentIntent = PendingIntent
															.getActivity(
																	ctx,
																	0,
																	notificationIntent,
																	PendingIntent.FLAG_CANCEL_CURRENT);
													NotificationManager nm = (NotificationManager) ctx
															.getSystemService(Context.NOTIFICATION_SERVICE);
													if (planName.length() > 30)
														planName = planName
																.substring(0,
																		30)
																+ "...";
													String contentTitle = total
															.toString()
															+ " new builds, "
															+ failNo
															+ " failed";
													String contentText = planName;
													String tickerText = "new builds.";
													if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
														NotificationCompat.Builder mbuilder =
																new NotificationCompat.Builder(ctx)
															.setContentIntent(contentIntent)
															.setSmallIcon(R.drawable.stixnotify)
															.setTicker(tickerText)
															.setWhen(System.currentTimeMillis())
															.setAutoCancel(true)
															.setContentTitle(contentTitle)
															.setContentText(contentText);
														//mbuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
														Notification notification = mbuilder.build();
														nm.notify(0, notification);
													} else {
														Notification notification = new Notification();
														notification.icon = R.drawable.stixnotify;
														notification.tickerText = tickerText;
														notification.when = System
																.currentTimeMillis();
														notification.flags = Notification.FLAG_AUTO_CANCEL;
														notification
																.setLatestEventInfo(
																		ctx,
																		contentTitle,
																		contentText,
																		contentIntent);
														nm.notify(0,
																notification);
													}

												} catch (JSONException e) {
													// TODO Auto-generated catch
													// block
													e.printStackTrace();
												}

											}
										});

							} else {
								Intent notificationIntent;
								PendingIntent contentIntent;
								notificationIntent = new Intent(ctx,
										ProjectListActivity.class);
								contentIntent = PendingIntent.getActivity(ctx,
										0, notificationIntent,
										PendingIntent.FLAG_CANCEL_CURRENT);
								NotificationManager nm = (NotificationManager) ctx
										.getSystemService(Context.NOTIFICATION_SERVICE);
								if (planName.length() > 30)
									planName = planName.substring(0, 30)
											+ "...";
								String contentTitle = total.toString()
										+ " new builds, " + failNo + " failed";
								String contentText = planName;
								String tickerText = "new builds.";
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
										NotificationCompat.Builder mbuilder =
											new NotificationCompat.Builder(ctx)
										.setContentIntent(contentIntent)
										.setSmallIcon(R.drawable.stixnotify)
										.setTicker(tickerText)
										.setWhen(System.currentTimeMillis())
										.setAutoCancel(true)
										.setContentTitle(contentTitle)
										.setContentText(contentText);
									mbuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
									Notification notification = mbuilder.build();
									nm.notify(0, notification);
								}
								else {
									Notification notification = new Notification();
									notification.icon = R.drawable.stixnotify;
									notification.tickerText = tickerText;
									notification.when = System
											.currentTimeMillis();
									notification.flags = Notification.FLAG_AUTO_CANCEL;
									notification
											.setLatestEventInfo(
													ctx,
													contentTitle,
													contentText,
													contentIntent);
									nm.notify(0,
											notification);
								}
							}

						} catch (JSONException e) {
							e.printStackTrace();
						}

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			});

		}
		
		private void parseProjectJSON(JSONObject jProjectObject) {
			try {
				JSONObject jProject = jProjectObject.getJSONObject("projects");
				JSONArray jProjectArray = jProject.getJSONArray("project");
				keys = new ArrayList<String>();
				for (int i = 0; i < jProjectArray.length(); i++) {
					JSONObject jPlan = jProjectArray.getJSONObject(i)
							.getJSONObject("plans");
					JSONArray jPlanArray = jPlan.getJSONArray("plan");

					for (int j = 0; j < jPlanArray.length(); j++) {
						String key = jPlanArray.getJSONObject(j).getString("key");
						String tmp1 = intentKey.substring(0, intentKey.indexOf('-'));
						String tmp2 = key.substring(0, key.indexOf('-'));
						if (tmp1.equalsIgnoreCase(tmp2)) {
							keys.add(key);
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void onDestroy() {
		super.onDestroy();
		timer.cancel();
		// timer.purge();
	}

	

}
