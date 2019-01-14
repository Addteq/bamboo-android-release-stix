package com.addteq.bamboo;

import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

import com.addteq.stix.R;
import com.atlassian.jconnect.droid.Api;

@ReportsCrashes(
        formKey = "",
        mode = ReportingInteractionMode.NOTIFICATION,
        resDialogEmailPrompt = R.string.crash_notification_dialog_request_user_email,
        resNotifTickerText = R.string.crash_notification_title,
        resNotifTitle = R.string.crash_notification_title,
        resNotifText = R.string.crash_notification_text,
        resDialogText = R.string.crash_notification_dialog_text)
public class BambooAndroidApp extends Application {
    @Override
    public void onCreate() {
        Api.init(this);
        super.onCreate();
    }
}
