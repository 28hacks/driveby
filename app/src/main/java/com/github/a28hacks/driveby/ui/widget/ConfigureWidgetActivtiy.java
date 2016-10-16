package com.github.a28hacks.driveby.ui.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;


public class ConfigureWidgetActivtiy extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawable(null);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        //send intent to update widget
        Intent in = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this,
                DriveByWidgetProvider.class);
        sendBroadcast(in);

        Intent resultValue = new Intent();
        setResult(RESULT_OK, resultValue);
        finish();

    }
}
