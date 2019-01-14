package com.atlassian.jconnect.droid.activity;

import static com.atlassian.jconnect.droid.ui.UiUtil.getTextFromView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.atlassian.jconnect.droid.Api;
import com.atlassian.jconnect.droid.R;
import com.atlassian.jconnect.droid.config.BaseConfig;
import com.atlassian.jconnect.droid.config.JmcInit;
import com.atlassian.jconnect.droid.dialog.AudioRecordingDialog;
import com.atlassian.jconnect.droid.service.FeedbackAttachment;
import com.atlassian.jconnect.droid.service.RemoteFeedbackServiceBinder;
import com.atlassian.jconnect.droid.ui.UiUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * Activity to create feedback item (which is a JIRA issue).
 * 
 * @since 1.0
 */
@SuppressLint("SimpleDateFormat")
public class FeedbackActivity extends SherlockActivity{
    private final static int ATTACHMENT_IMAGE = 1;
    private final static int ATTACHMENT_AUDIO = 2;

    private volatile AudioRecordingDialog audioRecordingDialog;

    private volatile String selectedImage = null;
    private volatile String selectedAudio = null;
    private volatile String selectedRecording = null;

    private volatile BaseConfig baseConfig;
    private volatile RemoteFeedbackServiceBinder feedbackServiceBinder;
    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        JmcInit.start(this);
        setContentView(R.layout.jconnect_droid_feedback);
        baseConfig = new BaseConfig(this);
        feedbackServiceBinder = new RemoteFeedbackServiceBinder(this);
        if (baseConfig.hasError()) {
            Toast.makeText(getApplicationContext(), baseConfig.getError(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        feedbackServiceBinder.init();
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        initSubmitButton();
        initRecordingDialog();
        EditText replyText = (EditText) findViewById(R.id.feedback_text);
        if (VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
        	//replyText.setTextColor(Color.WHITE);
        	replyText.setTextColor(Color.BLACK);
	    }
	    else{
	    	replyText.setTextColor(Color.BLACK);
	    }
        replyText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(hasFocus){
					getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
			}
		});
    }

    private void initSubmitButton() {
        /*final Button submit = (Button) findViewById(R.id.submit_button);
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                final String feedback = getTextFromView(FeedbackActivity.this, R.id.feedback_text);
                createIssue(feedback);
                finish();
            }
        });
        */
        final Button audio = (Button) findViewById(R.id.btAudio);
        audio.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				audioRecordingDialog.show();
			}
		});
        final Button gall = (Button) findViewById(R.id.btGallery);
        gall.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
		        intent.setType("image/*");
		        intent.setAction(Intent.ACTION_GET_CONTENT);
		        startActivityForResult(Intent.createChooser(intent,	"Select Picture"), ATTACHMENT_IMAGE);
		   }
		});
    }

    private void initRecordingDialog() {
    	Date date = Calendar.getInstance().getTime();
    	SimpleDateFormat sdf = new SimpleDateFormat("MM_dd_yyyy", Locale.getDefault());
    	String date_str = sdf.format(date);
        audioRecordingDialog = AudioRecordingDialog.forRecordingInTempDir(this, date_str+"_"+"recording.mp3");
        audioRecordingDialog.setCancelable(false);
    }

    
    
    
    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
		super.onPause();
	}

	@Override
    protected void onDestroy() {
        feedbackServiceBinder.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.jconnect_feedback_mainmenu, menu);
//        return true;
        MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.jconnect_feedback_mainmenu, menu);
		return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int id = item.getItemId();
        // if (id == R.id.jconnect_droid_attach_image) {
        // startAttachment(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        // ATTACHMENT_IMAGE);
        // } else if (id == R.id.jconnect_droid_attach_audio) {
        // startAttachment(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        // ATTACHMENT_AUDIO);
        // } else
        /*
        if (id == R.id.jconnect_droid_attach_record_audio) {
            audioRecordingDialog.show();
        }else if(id == R.id.jconnect_droid_attach_image){
        	Intent intent = new Intent();
        	intent.setType("image/*");
        	intent.setAction(Intent.ACTION_GET_CONTENT);
        	startActivityForResult(Intent.createChooser(intent,	"Select Picture"), ATTACHMENT_IMAGE);
        }
        */
        if (id == android.R.id.home){
        	FeedbackActivity.this.finish();
        }else if(id == R.id.jconnect_droid_send){
        	String feedback = getTextFromView(FeedbackActivity.this, R.id.feedback_text);
        	if(feedback.length() == 0){
        		Toast.makeText(getApplicationContext(), "Please enter some text", Toast.LENGTH_SHORT).show();
        	}
        	else{
        	createIssue(feedback);
            finish();
        	}
        }else if(id == R.id.feedback_inbox){
        	Intent intent = new Intent(this, FeedbackInboxActivity.class);
        	startActivity(intent);
        }
        
        return super.onOptionsItemSelected(item);
//    	switch (item.getItemId()) {
//    	case android.R.id.home:
//    				FeedbackActivity.this.finish();
//    				return true;
//    	case R.id.jconnect_droid_send:
//    				String feedback = getTextFromView(FeedbackActivity.this, R.id.feedback_text);
//    	            createIssue(feedback);
//    	            finish();
//    				return true;
//    	case R.id.feedback_inbox:
//    	            Intent intent = new Intent(this, FeedbackInboxActivity.class);
//    		        startActivity(intent);
//    	            return true;
//
//    			default:
//    				return super.onOptionsItemSelected(item);
//    			}
    }


    // private void startAttachment(Uri attachmentUri, int requestCode) {
    // final Intent pick = new Intent(Intent.ACTION_PICK, attachmentUri);
    // startActivityForResult(pick, requestCode);
    // }

   

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
        super.onActivityResult(requestCode, resultCode, returnedIntent);

        Log.d("FEEDBACK", "AFTER DISMISS");
        switch (requestCode) {
      
        case ATTACHMENT_IMAGE:
            if (resultCode == RESULT_OK) {
                Uri imageUri = returnedIntent.getData();
                if(imageUri != null){
                	String[] filePathColumn = { MediaStore.Images.Media.DATA };
                    Cursor cursor = getContentResolver().query(imageUri, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    this.selectedImage = cursor.getString(columnIndex);
                    cursor.close();
                    UiUtil.alert(getApplicationContext(), "Image selected!");
                }
            }
            break;
        case ATTACHMENT_AUDIO:
            if (resultCode == RESULT_OK) {
            	/*
            	Log.d("FEEDBACK Activity", "okay");
                Uri audioUri = returnedIntent.getData();
                String[] filePathColumn = { MediaStore.Audio.Media.DATA };
                this.selectedAudio = getFilePath(audioUri, filePathColumn);
                */
            }
            break;
        }
    }

    /*
    // Get path for image attachment
    private String getFilePath(Uri audioUri, String[] filePathColumn) {
    	Log.d("FEEDBACK", "GET FILE PATH");
        Cursor cursor = getContentResolver().query(audioUri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }
     */
    private void createIssue(final String feedback) {
        setSelectedRecording();
        feedbackServiceBinder.getService().createFeedback(feedback, getAttachments());
        //Log.d("FEEDBACK", "getattachment");
    }

    private Iterable<FeedbackAttachment> getAttachments() {
        final ImmutableList.Builder<FeedbackAttachment> builder = ImmutableList.builder();
        //Log.d("FEEDBACK", "START GET ATTACHMENTS");
        addPersistentAttachments(builder);
        addTemporaryAttachments(builder);
        //Log.d("FEEDBACK", "END");
        return builder.build();
    }

    private Map<String, String> persistentAttachments() {
    	final Map<String, String> attachments = Maps.newHashMap();
    	//Log.d("FEEDBACK", "PERSISTENT ATTACH");
    	attachments.put("screenshot", selectedImage);
        attachments.put("audioFeedback", selectedAudio);
        return attachments;
    }

    private Map<String, String> temporaryAttachments() {
    	//Log.d("FEEDBACK", "TEMP ATTACH");
        final Map<String, String> attachments = Maps.newHashMap();
        attachments.put("recordedAudioFeedback", selectedRecording);
        return attachments;
    }

    private void addPersistentAttachments(ImmutableList.Builder<FeedbackAttachment> builder) {
    	//Log.d("FEEDBACK", "PERSISTENT");
    	for (Map.Entry<String, String> attachment : persistentAttachments().entrySet()) {
            if (attachment.getValue() != null) {
                builder.add(FeedbackAttachment.persistent(attachment.getKey(), attachment.getValue()));
            }
        }
    }

    private void addTemporaryAttachments(ImmutableList.Builder<FeedbackAttachment> builder) {
       //Log.d("FEEDBACK", "TEMP");
    	for (Map.Entry<String, String> attachment : temporaryAttachments().entrySet()) {
            if (attachment.getValue() != null) {
            	
            	Log.d("FEEDBACK", attachment.getKey()+" and "+attachment.getValue());
                builder.add(FeedbackAttachment.temporary(attachment.getKey(), attachment.getValue()));
            }
        }
    }

    private void setSelectedRecording() {
        if (audioRecordingDialog.hasRecording()) {
        	//Log.d("FEEDBACK", "SELECT AUDIO");
            selectedRecording = audioRecordingDialog.getRecording().getAbsolutePath();
        } else {
            selectedRecording = null;
            
        }
    }

}
