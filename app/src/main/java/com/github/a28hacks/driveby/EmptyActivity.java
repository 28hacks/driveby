package com.github.a28hacks.driveby;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.github.a28hacks.driveby.audio.TextToSpeechService;
import com.github.a28hacks.driveby.location.DrivebyService;
import com.github.a28hacks.driveby.ui.widget.DriveByWidgetProvider;
import com.github.a28hacks.driveby.ui.widget.UpdateWidgetService;

/**
 * Created by stefan on 16.10.16.
 */

public class EmptyActivity extends AppCompatActivity {

    private static final String TAG = "EmptyActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "onCreate");
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            String action = extras.getString("ACTION");
            if(action != null) {
                Log.e(TAG, "onCreate: " + action);
                switch (action) {
                    case UpdateWidgetService.ACTION_START_SERVICES:
                        startService(new Intent(EmptyActivity.this, DrivebyService.class));
                        startService(new Intent(EmptyActivity.this, TextToSpeechService.class));
                        Toast.makeText(this, "Enabling DriveBy service", Toast.LENGTH_SHORT).show();
                        break;
                    case UpdateWidgetService.ACTION_STOP_SERVICES:
                        stopService(new Intent(EmptyActivity.this, DrivebyService.class));
                        stopService(new Intent(EmptyActivity.this, TextToSpeechService.class));
                        Toast.makeText(this, "Disabling DriveBy service", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }


        //send intent to update widget
        Intent in = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this,
                DriveByWidgetProvider.class);
        sendBroadcast(in);
    }
}
