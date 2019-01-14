package com.addteq.bamboo;

import java.io.IOException;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import java.util.Map;

import java.util.Locale;

import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.util.DisplayMetrics;

import android.text.Editable;
import android.text.TextWatcher;

import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.addteq.stix.R;
import com.atlassian.jconnect.droid.Api;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;

public class ProjectListActivity extends SherlockActivity implements 
SearchView.OnQueryTextListener, SearchView.OnCloseListener {
	
	//new expandable adapter varaibles
	HashMap<String, ArrayList<String>> project_plan_collection;
	ExpandableListView expListView;
	ArrayList<String> projectList;
	ArrayList<String> originalList;
	ArrayList<String> planList;
	private HashMap<String, String> newKeyMap=new HashMap<String, String>();
	RelativeLayout layout;
	ExpandableListAdapter expListAdapter;
	ListView projectLV;
	ArrayList<Project> projects;
	ArrayList<Plan> plans;
	ArrayList<String> keys;
	String projectName;
	ProjectListAdapter listAdapter;
	static final String LOG_STR = "ProjectListActivity";
	ProgressDialog mDialog;
	int requestCode;
	static final int PROJECT_CODE = 1;
	static final int PLAN_CODE = 2;
	static final int ALL_BUILDS_CODE = 4;
	static String projectJSONStr;
	static String planJSONStr;
	static String buildJSONStr;
	private PersistentCookieStore myCookieStore;
	AsyncHttpClient asyncHttpClient;
	public String IPAddy = "";
	public static String BASEURL = "/rest/api/latest/";
	public String IPBASEURL;
	//private ImageView sendfeedback;
	private static String encryptKey = null;
	private int LOGIN_TIMEOUT = 40 * 1000;
	private int noOfProjects;
	private int threshold = 25;
	private int thresholdPlans = 1000;
	private int firstVisible, startIndex;
	HashMap<String,String> map1;
	HashMap<String,String> map2;
    ArrayList<String> arr;
	SharedPreferences pref;
	SharedPreferences.Editor editor;
	private SearchView search;
	//Button btnProject;
	//EditText searchResult;
	String title;
	ArrayList<Project> arrayList=new ArrayList<Project>();
	TextView pinnedProjectName;
	
	
	Date now;
	Calendar cal;
	Date yestarday;
	
	LoginActivity la= new LoginActivity();
	Button submit;
	EditText newIPadd;
	EditText newPortadd;
	
	
	// testing
	// test
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.project);
		layout=(RelativeLayout)findViewById(R.id.layout);
		SearchManager searchManager=(SearchManager)getSystemService(Context.SEARCH_SERVICE);
		search=(SearchView)findViewById(R.id.search);
		search.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		search.setIconifiedByDefault(false);
		search.setOnQueryTextListener(ProjectListActivity.this);
		search.setOnCloseListener(ProjectListActivity.this);
		search.setQueryHint("Search Here");
		expListView = (ExpandableListView) findViewById(R.id.project_list); 
		expListView.setTextFilterEnabled(true);
		
		/*expListView.setVerticalFadingEdgeEnabled(true);
		expListView.setFadingEdgeLength(70);*/

		pinnedProjectName=(TextView) findViewById(R.id.projectName);
		
		pinnedProjectName.setVisibility(View.GONE);
		
		projects = new ArrayList<Project>();
		
		Context context =this;
		
		map1 = new HashMap<String,String>();
		map2 = new HashMap<String,String>();
		layout.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				hideKeyboard(v);
				return false;
			}
		});
		expListView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				hideKeyboard(v);
				return false;
			}
		});
		//sendfeedback = (ImageView) findViewById(R.id.feedback);
		/*sendfeedback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(Api
						.createFeedbackIntent(ProjectListActivity.this));
			}
		});*/
		/*searchResult=(EditText)findViewById(R.id.inputSearch);
		
        searchResult.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				  String text = searchResult.getText().toString().toLowerCase(Locale.getDefault());
	                adapter.filter(text);
			}
		});
		*/
	/*	btnProject=(Button)findViewById(R.id.button1);
		btnProject.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//Project p=new Project();
				JSONObject jObj = null;
				JSONObject jName = null;
				String name = null;
				String list[] = null;
				arr=new ArrayList<String>();

				for(int i=0;i<projects.size();i++){
					projectName=projects.get(i).getTitle();
					arr.add(projectName);
					
					 Toast.makeText(getApplicationContext(), "title :"+arr, Toast.LENGTH_SHORT).show();
				}
			}
		});*/
		
		
		asyncHttpClient = new AsyncHttpClient();
		myCookieStore = new PersistentCookieStore(ProjectListActivity.this);
		myCookieStore.getCookies();
		asyncHttpClient.setCookieStore(myCookieStore);
		pref = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		// pref = getSharedPreferences(LoginActivity.MyPREF,
		// Context.MODE_PRIVATE);
		editor = pref.edit();
		IPAddy = pref.getString("IPADDRESS", IPAddy);
		encryptKey = pref.getString("encryptKey", "@ddt3q");
		if (IPAddy.endsWith("/")) {
			IPBASEURL = IPAddy + "rest/api/latest/";
		} else {
			IPBASEURL = IPAddy + BASEURL;
		}
		// ActionBar actionBar = getSupportActionBar();

		
		//need to un comment the below line
		
		//projectLV = (ListView) findViewById(R.id.projectLV);
		projects = new ArrayList<Project>();
		plans = new ArrayList<Plan>();
		keys = new ArrayList<String>();

		List<Cookie> coo = myCookieStore.getCookies();
		for (Cookie c : coo) {
			Log.d("Cookie", "hey " + c.getValue());
		}
		getProjects();

	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
	/*	  InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                  INPUT_METHOD_SERVICE);
		  imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		  return true;*/
		 if(getCurrentFocus()!=null && getCurrentFocus() instanceof EditText){
		        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		        imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
		        
		    }
		  return true;
	}
	protected void hideKeyboard(View view){
		InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	    in.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    search.clearFocus();
	}
	@Override
	protected void onPause() {
		Log.e("On Paused", "Paused");
		super.onPause();
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
						ProjectListActivity.this);
				
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
						
									Intent intent = new Intent(ProjectListActivity.this, LoginActivity.class);
									startActivity(intent);
									
									//changeIpDialog.setCanceledOnTouchOutside(false);
									//changeIpDialog.show();
									
								/*	final Dialog changeIpDialog = new Dialog(ProjectListActivity.this);
									changeIpDialog.setContentView(R.layout.ipadd);
									changeIpDialog.setCanceledOnTouchOutside(true);
									changeIpDialog.setTitle(R.string.bamboo_server);

									submit = (Button) changeIpDialog.findViewById(R.id.change);
									final RadioButton http = (RadioButton) changeIpDialog
											.findViewById(R.id.http);
									final RadioButton https = (RadioButton) changeIpDialog
											.findViewById(R.id.https);
									
									changeIpDialog.setCanceledOnTouchOutside(false);
									changeIpDialog.show();*/
									
									//la.openSetting(ProjectListActivity.this);
									
									
									
									
									myCookieStore.clear();
									
									//below key-value pair is used to recogdnize whether from where the app comes to the LoginScreen..According to this scenario if the user
									//licence is expired then it brings to the login screen.. but we also need to pop up the settings page. So we use the "expire" value to
									//identify that the licence is expired and to show the settings dialog with out showing the welcome message.
									
									editor.putString("firsttime", "expire");
									if (pref.getBoolean("CHECK", false) == false) {
										editor.remove("encryptedpass");
									}
									editor.remove("loggedin");
									editor.remove("GuestLogin");
						            //editor.remove("SSLCHECK");
									editor.commit();
									ProjectListActivity.this.finish();
									Intent intent3 = new Intent(ProjectListActivity.this,
											NotificationService.class);
									intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									stopService(intent3);
									Intent goToLoginIntent = new Intent(ProjectListActivity.this,
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

	private void parseProjectJSON(JSONObject jProjectObject) {
		try {
		
			JSONObject jProject = jProjectObject.getJSONObject("projects");
			JSONArray jProjectArray = jProject.getJSONArray("project");

			Set<String> subtitles = new HashSet<String>();
			for (Iterator<Project> p = projects.iterator(); p.hasNext();) {
				if (!subtitles.add(p.next().getTitle())) {
					p.remove();
				}
			}

			noOfProjects = Integer.parseInt(jProject.getString("size"));
			
			String projectKey, shortName;
			
			//Toast.makeText(getApplicationContext(), "arr "+jProjectArray.length()+" project: "+projects.size(), Toast.LENGTH_SHORT).show();

			for (int i = 0; i < jProjectArray.length(); i++) {
				Project project = new Project();
				project.setTitle(jProjectArray.getJSONObject(i).getString(
						"name"));
				
				projectKey=jProjectArray.getJSONObject(i).getString("key");///consider to change
				project.setKey(projectKey);
				
				JSONObject jPlan = jProjectArray.getJSONObject(i)
						.getJSONObject("plans");
				JSONArray jPlanArray = jPlan.getJSONArray("plan");
				
				//an arraylist which contains plans of each project
			    ArrayList<String> plansArrayList = new ArrayList<String>();
					
				for (int j = 0; j < jPlanArray.length(); j++) {
					Plan plan = new Plan();
					plan.setTitle(jPlanArray.getJSONObject(j).getString("name"));
					plan.setKey(jPlanArray.getJSONObject(j).getString("key"));
					
					shortName = jPlanArray.getJSONObject(j).getString( //consider to change
							"shortName");
					
					plan.setShort_name(shortName); 
					plan.setProject(project.getTitle());
					
					plansArrayList.add(jPlanArray.getJSONObject(j).getString("shortName"));
					
					
					if (j == 0) {
						project.setPlan1(shortName);
						map1.put(shortName+"@"+projectKey, "");
					
					} 
					if (j == 1) {
						project.setPlan2(shortName);
						map2.put(shortName+"@"+projectKey, "");
					}
					
					
					plans.add(plan);
				}
				project.setPlansArrayList(plansArrayList);
				
				projects.add(project);
		
			}

		} catch (JSONException e) {
			Toast.makeText(getApplicationContext(), e.toString(),
					Toast.LENGTH_LONG).show();
		}
		
		
		//implement the load more projects functionality here
		
		
		
	   expListView.setOnScrollListener(new OnScrollListener() {
		
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			
			//Log.d("STATE","scroll state "+scrollState);
			
			int group_num=getFirstVisibleGroup();
			
			//boolean isLast=isLastChild(group_num);
			
			//Log.d("isLast","isLast "+isLast);
	
			
			//if(isLast){
				
			//	pinnedProjectName.setVisibility(View.GONE);
				
				
			//}else{
	
			
			if(group_num==0){
				
				pinnedProjectName.setVisibility(View.GONE);
			
			}else{
		   	
			   pinnedProjectName.setVisibility(View.VISIBLE);

			   String project_name=expListAdapter.getGroup(group_num);
						
			   pinnedProjectName.setText(project_name);
			
		   }
			
				
			//}
			
	
		}
		
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			
			

			if (visibleItemCount + firstVisibleItem == totalItemCount) {
				
				int size = projects.size();			
				firstVisible = firstVisibleItem;
				
				//make the project name visible at the end of listview
				//pinnedProjectName.setVisibility(View.VISIBLE);
				
				
				//Toast.makeText(getApplicationContext(), "size : "+size,Toast.LENGTH_SHORT).show();

				if (size < noOfProjects) {
					// set start index
					startIndex = (size / threshold) * (threshold);
					
					//show the loading sign
					//Toast.makeText(getApplicationContext(), "End of list , strat: "+startIndex,Toast.LENGTH_SHORT).show();
					
					BackgroundTask task = new BackgroundTask(ProjectListActivity.this);
					task.execute();
					
					Log.d("1","1");
					refreshProjects(startIndex, threshold);
					//Toast.makeText(getApplicationContext(), "End of list , size: "+projects.size(),Toast.LENGTH_SHORT).show();
				}
			}
			
		}
	  });
	
	  

		/*projectLV.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

				if (visibleItemCount + firstVisibleItem == totalItemCount) {
					
					firstVisible = firstVisibleItem;

					if (noOfProjects - totalItemCount > 0
							&& visibleItemCount >= 2) {
						// set start index
						startIndex = (totalItemCount / threshold) * (threshold);
						
						//show the loading sign
						
						BackgroundTask task = new BackgroundTask(ProjectListActivity.this);
						task.execute();
						
						ma
						refreshProjects(startIndex, threshold);
						listAdapter.addAll(projects);

						projectLV.setAdapter(listAdapter);
						projectLV.setSelection(firstVisibleItem);
	
					}
				}
			}
		});*/

	}
	
    public int getFirstVisibleGroup() {
        int firstVis = expListView.getFirstVisiblePosition();
    long packedPosition = expListView.getExpandableListPosition(firstVis);
    int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
    return groupPosition;
    }
		
/*    
    public boolean isLastChild(int position) {
    	
    int childCount=expListAdapter.getChildrenCount(position);//numberOfChildren in the currentGroup
    int childCount_previous=0;
    
    if(position>0){
       childCount_previous=expListAdapter.getChildrenCount(position-1); //numberOfChildren in the lastGroup
    }
    
    int firstVis = expListView.getFirstVisiblePosition();//currentGroupID
   
    int lastParent_ID=firstVis-(childCount_previous); //lastGroupID
    
    int current_visible_cild=firstVis-lastParent_ID; 

    
    Log.d("FIRSTVIS-CHILDCOUNT", "firstvis : "+current_visible_cild+" lastParent "+lastParent_ID);
    
    
    if(firstVis==lastParent_ID){
    	return true;
    	
    }
    
    return false;
   
    }*/
	
    
    

	private void parseBuildJSON(JSONObject jBuildObject) {
		try {
			

			JSONObject jResult = jBuildObject.getJSONObject("results");
			JSONArray jResultArray = jResult.getJSONArray("result");
			ArrayList<Project> countProjects = projects;

			for (int i = 0; i < jResultArray.length(); i++) {
				String state = jResultArray.getJSONObject(i).getString(
						"state");
				
				String key = jResultArray.getJSONObject(i).getString("key");
	
				key = key.substring(0, key.indexOf("-"));

				String plan = jResultArray.getJSONObject(i)
						.getJSONObject("plan").getString("shortName"); 
				
				String newKey=jResultArray.getJSONObject(i)
						.getJSONObject("plan").getString("name");
				
				newKeyMap.put(newKey, state);
				
				
				String mapKey;

				mapKey=plan+"@"+key; 

				if(map1.get(mapKey)!=null)
				{
					map1.put(mapKey, state);
				}
				else if(map2.get(mapKey)!=null)
				{
					map2.put(mapKey, state);
				}
		         
			}
			Collections.sort(countProjects, new CustomComparator());
			final ArrayList<Project> newProjects = countProjects;
			
			//this is the pace where we are planning to use the expandable  listview adapter
			

			createCollection();
			
			//createprojectList();


			//expListView = (ExpandableListView) findViewById(R.id.project_list); 
			expListAdapter = new ExpandableListAdapter(
					this, projectList, project_plan_collection,newKeyMap);
			expListView.setAdapter(expListAdapter);

			for(int i =0; i<expListAdapter.getGroupCount();i++){
				
				expListView.expandGroup(i);
			}
			
			
			expListView.setOnChildClickListener(new OnChildClickListener() {
				
				@Override
				public boolean onChildClick(ExpandableListView parent, View v,
						int groupPosition, int childPosition, long id) {

					 /*Toast.makeText(getApplicationContext(), "project Name :"+projects.get(groupPosition)
								.getTitle(), Toast.LENGTH_LONG).show();*/
					  
					  ArrayList<String> plan_name = new ArrayList<String>();
						ArrayList<String> project_name_list = new ArrayList<String>();
						final ArrayList<String> selectedPlans = new ArrayList<String>();

						for (Plan p : plans) {
							
							//Toast.makeText(getApplicationContext(), "plan : "+p.getProject()+" prjoect : "+v.getTag().toString(), Toast.LENGTH_SHORT).show();
							
							if (p.getProject().equalsIgnoreCase(
									v.getTag().toString())) {
								selectedPlans.add(p.getKey());
								keys.add(p.getKey());
								plan_name.add(p.getShort_name());
								project_name_list.add(p.getProject());

							}
						}
						
						Intent intent = new Intent(ProjectListActivity.this,
								BuildListActivity.class); 

						// store project, list item.get view.getText convert to
						// string
						intent.putExtra("projectName", projects.get(groupPosition)
								.getTitle());
						
						//Toast.makeText(getApplicationContext(), "plan : "+selectedPlans.get(childPosition), Toast.LENGTH_SHORT).show();
						
						intent.putExtra("plan_key", selectedPlans.get(childPosition));
						intent.putExtra("child", childPosition);
						intent.putExtra("keys", keys);
						intent.putExtra("plans", plan_name);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent); 
						keys.clear();
						
				
					
					return false;
				}
			});
			
			
			
			expListView.setOnGroupClickListener(new OnGroupClickListener() {
				  @Override
				  public boolean onGroupClick(ExpandableListView parent, View v,
				                              int groupPosition, long id) { 

					 // expListView.setSelectionFromTop(groupPosition, 0);
					  
					 // expListView.setSelectionAfterHeaderView();
					  
					  Log.d("GROUP-POSITION","position "+groupPosition);
				    return true; // This way the expander cannot be collapsed
				  }
				});

			/*listAdapter = new ProjectListAdapter(this, newProjects, map1,map2);
			projectLV.setAdapter(listAdapter);
			projectLV.setSelection(firstVisible);*/

			/*projectLV.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View v, int arg2,
						long arg3) {
					ArrayList<String> plan_name = new ArrayList<String>();
					ArrayList<String> project_name_list = new ArrayList<String>();
					final ArrayList<Plan> selectedPlans = new ArrayList<Plan>();

					for (Plan p : plans) {
						if (p.getProject().equalsIgnoreCase(
								v.getTag().toString())) {
							selectedPlans.add(p);
							keys.add(p.getKey());
							plan_name.add(p.getShort_name());
							project_name_list.add(p.getProject());

						}
					}
					Intent intent = new Intent(ProjectListActivity.this,
							BuildListActivity.class);

					// store project, list item.get view.getText convert to
					// string
					intent.putExtra("projectName", newProjects.get(arg2)
							.getTitle());
					intent.putExtra("plan_key", selectedPlans.get(0).getKey());
					intent.putExtra("keys", keys);
					intent.putExtra("plans", plan_name);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					keys.clear();
				}
			});*/
			if (mDialog.isShowing()) {
				try {
					mDialog.dismiss();
				} catch (Exception dialogException) {
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/*	private void parseBuildJSON(JSONObject jBuildObject) {
	try {

		JSONObject jResult = jBuildObject.getJSONObject("results");
		JSONArray jResultArray = jResult.getJSONArray("result");
		ArrayList<Project> countProjects = projects;

		for (int i = 0; i < jResultArray.length(); i++) {
			String state = jResultArray.getJSONObject(i).getString("state");
			String key = jResultArray.getJSONObject(i).getString("key");
			int number = jResultArray.getJSONObject(i).getInt("number");
			key = key.substring(0, key.indexOf("-"));
			
			for (Project project : countProjects) {
				if (key.equalsIgnoreCase(project.getKey())) {
					if (state.equalsIgnoreCase("successful")) {
						int successnum = project.getSuccess();
						successnum += number;
						project.setSuccess(successnum);
					} else {
						int failnum = project.getFailure();
						failnum += number;
						project.setFailure(failnum);
					}
				}

			}
		}
		//Collections.sort(countProjects, new CustomComparator());
		final ArrayList<Project> newProjects = countProjects;
				
		listAdapter = new ProjectListAdapter(this, newProjects);
		projectLV.setAdapter(listAdapter);
		projectLV.setSelection(firstVisible);

		projectLV.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int arg2,
					long arg3) {
				ArrayList<String> plan_name = new ArrayList<String>();
				ArrayList<String> project_name_list = new ArrayList<String>();
				final ArrayList<Plan> selectedPlans = new ArrayList<Plan>();

				for (Plan p : plans) {
					if (p.getProject().equalsIgnoreCase(
							v.getTag().toString())) {
						selectedPlans.add(p);
						keys.add(p.getKey());
						plan_name.add(p.getShort_name());
						project_name_list.add(p.getProject());

					}
				}
				Intent intent = new Intent(ProjectListActivity.this,
						BuildListActivity.class);

				// store project, list item.get view.getText convert to
				// string
				intent.putExtra("projectName", newProjects.get(arg2)
						.getTitle());
				intent.putExtra("plan_key", selectedPlans.get(0).getKey());
				intent.putExtra("keys", keys);
				intent.putExtra("plans", plan_name);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				keys.clear();
			}
		});
		if (mDialog.isShowing()) {
			try {
				mDialog.dismiss();
			} catch (Exception dialogException) {
			}
		}
	} catch (JSONException e) {
		e.printStackTrace();
	}
}*/
	
	private void createCollection() {
		
		projectList = new ArrayList<String>();
		project_plan_collection = new HashMap<String, ArrayList<String>>() ;
		
		int noOfProjects=projects.size();
		
		//Toast.makeText(getApplicationContext(), "size "+noOfProjects, Toast.LENGTH_SHORT).show();
		
		for(int i=0; i<noOfProjects;i++){
			
			projectList.add(projects.get(i).getTitle());
			project_plan_collection.put(projects.get(i).getTitle(), projects.get(i).getPlansArrayList());
			
		}
		
	}

/*	private void createprojectList() {

		// preparing laptops collection(child)
				String[] hpModels = { "HP Pavilion G6-2014TX", "ProBook HP 4540",
						"HP Envy 4-1025TX" };
				String[] hclModels = { "HCL S2101", "HCL L2102", "HCL V2002" };
				String[] lenovoModels = { "IdeaPad Z Series", "Essential G Series",
						"ThinkPad X Series", "Ideapad Z Series" };
				String[] sonyModels = { "VAIO E Series", "VAIO Z Series",
						"VAIO S Series", "VAIO YB Series" };
				String[] dellModels = { "Inspiron", "Vostro", "XPS" };
				String[] samsungModels = { "NP Series", "Series 5", "SF Series" };

				project_plan_collection = new HashMap<String, ArrayList<String>>() ;

				for (String laptop : projectList) {
					if (laptop.equals("HP")) {
						loadChild(hpModels);
					} else if (laptop.equals("Dell"))
						loadChild(dellModels);
					else if (laptop.equals("Sony"))
						loadChild(sonyModels);
					else if (laptop.equals("HCL"))
						loadChild(hclModels);
					else if (laptop.equals("Samsung"))
						loadChild(samsungModels);
					else
						loadChild(lenovoModels);

					project_plan_collection.put(laptop, planList);
				}
		
		
	}*/
	
/*	private void loadChild(String[] laptopModels) {
		planList = new ArrayList<String>();
		for (String model : laptopModels)
			planList.add(model);
	}

	private void setGroupIndicatorToRight() {
		 Get the screen width 
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;

		expListView.setIndicatorBounds(width - getDipsFromPixel(35), width
				- getDipsFromPixel(5));
	}*/
	
	public int getDipsFromPixel(float pixels) {
		// Get the screen's density scale
		final float scale = getResources().getDisplayMetrics().density;
		// Convert the dps to pixels, based on density scale
		return (int) (pixels * scale + 0.5f);
	}

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.projectlist_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			Intent k = new Intent(ProjectListActivity.this, LoginActivity.class);
			k.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(k);
			return true;
		} else if (itemId == R.id.logout_menu) {
			myCookieStore.clear();
			editor.putString("firsttime", "notfirst");
			if (pref.getBoolean("CHECK", false) == false) {
				editor.remove("encryptedpass");
			}
			editor.remove("loggedin");
			editor.remove("GuestLogin");
            //editor.remove("SSLCHECK");
			editor.commit();
			ProjectListActivity.this.finish();
			Intent intent3 = new Intent(ProjectListActivity.this,
					NotificationService.class);
			intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			stopService(intent3);
			Intent goToLoginIntent = new Intent(ProjectListActivity.this,
					LoginActivity.class);
			startActivity(goToLoginIntent);
			return true;
		} else if (itemId == R.id.help_menu) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					ProjectListActivity.this);
			builder.setTitle("Help");
			builder.setMessage("This page contains a list of all the projects on your Bamboo server."
					+ "\n"
					+ "It shows a count of the successful and failed plans for each project."
					+ "\n"
					+ "\n\n"
					+ "Click on the project to see its containing plans."
					+ "\n"
					+ "A new build can be started from the plan dialog or you can long click to view the list of builds.");
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

	public void refreshProjects(int startIndex, int maxResult) {
		if (isNetworkOnline()) {
			String serverCallURL = IPBASEURL
					+ "project.json?expand=projects.project.plans&start-index="
					+ startIndex + "&max-result=" + maxResult;
			
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
				//Log.d("IN PROGRESS LOGGING IN", "TURSTED ALL HOSTS");
				
				
			}
			
			
			
			final int start,max;
			start=startIndex;
			max=maxResult;

			mDialog = new ProgressDialog(ProjectListActivity.this);
			mDialog.setMessage("Fetching Projects...");
			mDialog.setCancelable(false);
			//mDialog.show();
			asyncHttpClient.get(serverCallURL, new AsyncHttpResponseHandler() {

				public void onSuccess(String response) {

					// parse response
					try {
						Log.d("situation", "after success = "
								+ myCookieStore.getCookies().size());
						JSONObject projectJson = new JSONObject(response);
						parseProjectJSON(projectJson);
						//getAllBuilds();
						getAllBuildsRefresh(start,thresholdPlans);
					} catch (JSONException e) {
						e.printStackTrace();
					}

				}

				public void onFailure(Throwable e, String response) {
					try {
						e.printStackTrace();
						Log.d("ERROR", "THROWS " + e.toString());
						if (e.toString().contains("HttpResponseException")
								&& e.toString().contains("Unauthorized")) {
							loggingIn();
						} else {
							if (mDialog.isShowing()) {
								try {
									mDialog.dismiss();
								} catch (Exception dialogException) {
								}
							}
							AlertDialog.Builder alt_bld = new AlertDialog.Builder(
									ProjectListActivity.this);
							alt_bld.setTitle("Error");
							alt_bld.setMessage("Server Error or Unavailable, Please logout!");
							alt_bld.setCancelable(false);
							alt_bld.setNeutralButton("Logout",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											myCookieStore.clear();
											editor.remove("loggedin");
											editor.remove("GuestLogin");
											editor.remove("SSLCHECK");
											
											editor.commit();
											Intent loginIntent = new Intent(
													ProjectListActivity.this,
													LoginActivity.class);
											startActivity(loginIntent);
											Intent intent3 = new Intent(
													ProjectListActivity.this,
													NotificationService.class);
											intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
											ProjectListActivity.this
													.stopService(intent3);
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

	}

	public void getProjects() {
		if (isNetworkOnline()) {

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
				//Log.d("IN PROGRESS LOGGING IN", "TURSTED ALL HOSTS");
				
				
			}
			

			mDialog = new ProgressDialog(ProjectListActivity.this);
			mDialog.setMessage("Fetching Projects...");
			mDialog.setCancelable(false);
			mDialog.show();
			asyncHttpClient.get(serverCallURL, new AsyncHttpResponseHandler() {

				public void onSuccess(String response) {

					// parse response
					try {
						Log.d("situation", "after success = "
								+ myCookieStore.getCookies().size());
						JSONObject projectJson = new JSONObject(response);
						mDialog.dismiss();
						parseProjectJSON(projectJson);

						getAllBuildsRefresh(0,thresholdPlans);
						

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				public void onFailure(Throwable e, String response) {
					try {
						e.printStackTrace();
						Log.d("ERROR", "THROWS " + e.toString());
						if (e.toString().contains("HttpResponseException")
								&& e.toString().contains("Unauthorized")) {
							loggingIn();
						} else {
							if (mDialog.isShowing()) {
								try {
									mDialog.dismiss();
								} catch (Exception dialogException) {
								}
							}
							AlertDialog.Builder alt_bld = new AlertDialog.Builder(
									ProjectListActivity.this);
							alt_bld.setTitle("Error");
							alt_bld.setMessage("Server Error or Unavailable, Please logout!");
							alt_bld.setCancelable(false);
							alt_bld.setNeutralButton("Logout",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											myCookieStore.clear();
											editor.remove("loggedin");
											editor.remove("GuestLogin");
											editor.remove("SSLCHECK");
											editor.commit();
											Intent loginIntent = new Intent(
													ProjectListActivity.this,
													LoginActivity.class);
											startActivity(loginIntent);
											Intent intent3 = new Intent(
													ProjectListActivity.this,
													NotificationService.class);
											intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
											ProjectListActivity.this
													.stopService(intent3);
										}
									});
							
							alt_bld.show();
						}
					} catch (Exception error) {
						error.printStackTrace();
					}
				}

			});
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					ProjectListActivity.this);
			builder.setTitle("Error");
			builder.setMessage("Internet connection is not available");
			builder.setCancelable(false);
			builder.setPositiveButton("Logout",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							myCookieStore.clear();
							editor.remove("loggedin");
							editor.remove("GuestLogin");
							editor.remove("SSLCHECK");
							editor.commit();
							Intent loginIntent = new Intent(
									ProjectListActivity.this,
									LoginActivity.class);
							startActivity(loginIntent);
							Intent intent3 = new Intent(
									ProjectListActivity.this,
									NotificationService.class);
							intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							ProjectListActivity.this.stopService(intent3);
						}
					});
			builder.setNeutralButton("Retry",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							getProjects();
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
                      asyncHttpClient.setSSLSocketFactory(sf);
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
    

	public void retryGetProjects() {
		if (isNetworkOnline()) {
			String serverCallURL = IPBASEURL
					+ "project.json?expand=projects.project.plans";

			asyncHttpClient.get(serverCallURL, new AsyncHttpResponseHandler() {

				public void onSuccess(String response) {

					// parse response
					try {
						JSONObject projectJson = new JSONObject(response);
						parseProjectJSON(projectJson);

						getAllBuilds();

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

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
								ProjectListActivity.this);
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
										editor.remove("SSLCHECK");
										editor.commit();
										Intent loginIntent = new Intent(
												ProjectListActivity.this,
												LoginActivity.class);
										startActivity(loginIntent);
										Intent intent3 = new Intent(
												ProjectListActivity.this,
												NotificationService.class);
										intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										ProjectListActivity.this
												.stopService(intent3);
									}
								});
						alt_bld.setNegativeButton("Retry",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										retryGetProjects();
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
					ProjectListActivity.this);
			builder.setTitle("Error");
			builder.setMessage("Internet connection is not available");
			builder.setCancelable(false);
			builder.setPositiveButton("Logout",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							myCookieStore.clear();
							editor.remove("loggedin");
							editor.remove("GuestLogin");
							editor.remove("SSLCHECK");
							editor.commit();
							Intent loginIntent = new Intent(
									ProjectListActivity.this,
									LoginActivity.class);
							startActivity(loginIntent);
							Intent intent3 = new Intent(
									ProjectListActivity.this,
									NotificationService.class);
							intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							ProjectListActivity.this.stopService(intent3);
						}
					});
			builder.setNeutralButton("Retry",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							retryGetProjects();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	//new method which fetch the next set of plans from the bamboo server
	public void getAllBuildsRefresh(int stratIndex, int maxResult) {
				
		String serverCallURL = IPBASEURL + "result.json?start-index="+stratIndex*40+"&max-result="+maxResult;
		/* the start index of the projects list is multiplied by 40 to get the start index
		   of plans. thresholdForPlans(1000) = thresholdForProjects(25)*40
       */
		
		
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
		
		
		
		
		
		
		asyncHttpClient.get(serverCallURL, new AsyncHttpResponseHandler() {
			public void onSuccess(String response) {
				try {
					JSONObject buildJson = new JSONObject(response);
					parseBuildJSON(buildJson);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

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
							ProjectListActivity.this);
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
									editor.remove("SSLCHECK");
									editor.commit();
									Intent loginIntent = new Intent(
											ProjectListActivity.this,
											LoginActivity.class);
									startActivity(loginIntent);
									Intent intent3 = new Intent(
											ProjectListActivity.this,
											NotificationService.class);
									intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									ProjectListActivity.this
											.stopService(intent3);
								}
							});
					alt_bld.show();
				} catch (Exception error) {
					error.printStackTrace();
				}
			}
		});
	}

	
	public void getAllBuilds() {
		String serverCallURL = IPBASEURL + "result.json";
		
		
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
		
		

		asyncHttpClient.get(serverCallURL, new AsyncHttpResponseHandler() {
			public void onSuccess(String response) {
				try {
					JSONObject buildJson = new JSONObject(response);
					parseBuildJSON(buildJson);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

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
							ProjectListActivity.this);
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
									editor.remove("SSLCHECK");
									editor.commit();
									Intent loginIntent = new Intent(
											ProjectListActivity.this,
											LoginActivity.class);
									startActivity(loginIntent);
									Intent intent3 = new Intent(
											ProjectListActivity.this,
											NotificationService.class);
									intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									ProjectListActivity.this
											.stopService(intent3);
								}
							});
					alt_bld.show();
				} catch (Exception error) {
					error.printStackTrace();
				}
			}
		});
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
				Log.e("at beginning of onSuccess of Login",
						"I am doing testing " + response);

				if (response.contains("check-code")
						&& response.contains("success")) {

					Log.d("Login", "Success");
					retryGetProjects();
				} else {
					Log.d("Login", "Fail");
					if (mDialog.isShowing()) {
						try {
							mDialog.dismiss();
						} catch (Exception dialogException) {
						}
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(
							ProjectListActivity.this);
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
									editor.remove("SSLCHECK");
									editor.commit();
									Intent loginIntent = new Intent(
											ProjectListActivity.this,
											LoginActivity.class);
									startActivity(loginIntent);
									Intent intent3 = new Intent(
											ProjectListActivity.this,
											NotificationService.class);
									intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									ProjectListActivity.this
											.stopService(intent3);
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
				if (mDialog.isShowing()) {
					try {
						mDialog.dismiss();
					} catch (Exception dialogException) {
					}
				}
				if (e.toString().contains("SocketTimeoutException")
						|| e.toString().contains("ConnectException")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							ProjectListActivity.this);
					builder.setTitle("Error");
					builder.setMessage("Request Timed out. Please try again.");
					builder.setCancelable(true);
					builder.setNeutralButton("Logout",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									myCookieStore.clear();
									editor.remove("loggedin");
									editor.remove("GuestLogin");
									editor.remove("SSLCHECK");
									editor.commit();
									Intent loginIntent = new Intent(
											ProjectListActivity.this,
											LoginActivity.class);
									startActivity(loginIntent);
									Intent intent3 = new Intent(
											ProjectListActivity.this,
											NotificationService.class);
									intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									ProjectListActivity.this
											.stopService(intent3);
								}
							});
					builder.setNegativeButton("Retry",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									loggingIn();
								}
							});

					AlertDialog alert = builder.create();
					alert.show();

				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							ProjectListActivity.this);
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
									editor.remove("SSLCHECK");
									editor.commit();
									Intent loginIntent = new Intent(
											ProjectListActivity.this,
											LoginActivity.class);
									startActivity(loginIntent);
									Intent intent3 = new Intent(
											ProjectListActivity.this,
											NotificationService.class);
									intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									ProjectListActivity.this
											.stopService(intent3);
								}
							});
					AlertDialog alert = builder.create();
					alert.show();

				}
			}
		});// end of get
	}

	public class CustomComparator implements Comparator<Project> {

		@Override
		public int compare(Project p1, Project p2) {
			// TODO Auto-generated method stub
			return p1.getTitle().compareTo(p2.getTitle());
		}

	}
	private class BackgroundTask extends AsyncTask <Void, Void, Void> {
	    private ProgressDialog dialog;
	     
	    public BackgroundTask(ProjectListActivity activity) {
	        dialog = new ProgressDialog(activity);
	    }
	 
	    @Override
	    protected void onPreExecute() {
	        dialog.setMessage("Fetching projects...");
	        dialog.show();
	    }
	     
	    @Override
	    protected void onPostExecute(Void result) {
	        if (dialog.isShowing()) {
	            dialog.dismiss();
	        }
	    }
	     
	    @Override
	    protected Void doInBackground(Void... params) {
	        try {
	            Thread.sleep(2000);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	 
	        return null;
	    }
	     
	}


	@Override
	public boolean onClose() {
	//	final ExpandableListAdapter expListAdapter = new ExpandableListAdapter(
		//		this, projectList, project_plan_collection,newKeyMap);
		expListAdapter.filterData("");
		int count=expListAdapter.getGroupCount();
		for(int i=0;i<count;i++){
			expListView.expandGroup(i);
		}
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		Log.d("TAG CHANGE", "" + query);
	//	final ExpandableListAdapter expListAdapter = new ExpandableListAdapter(
		//					this, projectList, project_plan_collection,newKeyMap);
		expListAdapter.filterData(query);
		int count=expListAdapter.getGroupCount();
		for(int i=0;i<count;i++){
			expListView.expandGroup(i);
		}
		return false;
	}

	@Override
	public boolean onQueryTextChange(String query) {
		Log.d("TAG CHANGE", "" + query);
	
	//	final ExpandableListAdapter expListAdapter = new ExpandableListAdapter(
		//					this, projectList, project_plan_collection,newKeyMap);
		expListAdapter.filterData(query);
		int count=expListAdapter.getGroupCount();
		for(int i=0;i<count;i++){
			expListView.expandGroup(i);
		}
		return false;
	}

}