package com.github.a28hacks.driveby.ui.widget;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.github.a28hacks.driveby.EmptyActivity;
import com.github.a28hacks.driveby.R;
import com.github.a28hacks.driveby.location.DrivebyService;

public class UpdateWidgetService extends Service {

    private static final String TAG = "UpdateWidgetService";
    public static final String ACTION_STOP_SERVICES = "StopServices";
    public static final String ACTION_START_SERVICES = "StartServices";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "STARTING WIDGET UPDATE");

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
                .getApplicationContext());

        int[] allWidgetIds;

        Bundle extras = intent.getExtras();
        if (extras != null) {
            allWidgetIds = extras.getIntArray(
                    AppWidgetManager.EXTRA_APPWIDGET_IDS);
            if(allWidgetIds == null || allWidgetIds.length == 0){
                return super.onStartCommand(intent, flags, startId);
            }
        } else {
            return super.onStartCommand(intent, flags, startId);
        }


        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : allWidgetIds) {

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            Intent in = new Intent(this, EmptyActivity.class);
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_layout);
            if(isMyServiceRunning(DrivebyService.class)) {
                views.setImageViewResource(R.id.imageviewBtn, R.drawable.ic_hearing_white_36dp);
                in.putExtra("ACTION",ACTION_STOP_SERVICES);
                Log.e(TAG, "onStartCommand: Widget will stop services");
            } else {
                views.setImageViewResource(R.id.imageviewBtn, R.drawable.ic_hearing_crossed_white_24px);
                in.putExtra("ACTION",ACTION_START_SERVICES);
                Log.e(TAG, "onStartCommand: Widget will start services");
            }

            PendingIntent clickPenIntent = PendingIntent.getActivities(this, 0,
                    new Intent[] {in}, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widget_layout,clickPenIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        Log.i(TAG, "WIDGET UPDATE SUCCESSFUL");
        stopSelfResult(startId);

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
