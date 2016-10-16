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

        int mAppWidgetId = 0;

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            if(mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) finish();
        } else {
            finish();
        }

        //send intent to update widget
        Intent in = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this,
                DriveByWidgetProvider.class);
        in.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {mAppWidgetId});
        sendBroadcast(in);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }
}
