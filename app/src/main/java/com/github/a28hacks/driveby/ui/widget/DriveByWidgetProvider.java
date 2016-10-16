package com.github.a28hacks.driveby.ui.widget;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.github.a28hacks.driveby.EmptyActivity;
import com.github.a28hacks.driveby.R;
import com.github.a28hacks.driveby.location.DrivebyService;

import static com.github.a28hacks.driveby.ui.widget.UpdateWidgetService.ACTION_START_SERVICES;
import static com.github.a28hacks.driveby.ui.widget.UpdateWidgetService.ACTION_STOP_SERVICES;

public class DriveByWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "DBWidgetProvider";

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i(TAG, "onUpdate method called");
        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                DriveByWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);


        final int N = allWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, EmptyActivity.class);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            if(isMyServiceRunning(DrivebyService.class, context)) {
                views.setImageViewResource(R.id.imageviewBtn, R.drawable.ic_hearing_white_36dp);
                intent.putExtra("ACTION",ACTION_STOP_SERVICES);
                Log.e(TAG, "onStartCommand: Widget will stop services");
            } else {
                views.setImageViewResource(R.id.imageviewBtn, R.drawable.ic_hearing_crossed_white_24px);
                intent.putExtra("ACTION",ACTION_START_SERVICES);
                Log.e(TAG, "onStartCommand: Widget will start services");
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
