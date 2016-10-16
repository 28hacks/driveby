package com.github.a28hacks.driveby.ui;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.github.a28hacks.driveby.MainActivity;
import com.github.a28hacks.driveby.R;
import com.github.a28hacks.driveby.model.database.GeoItem;

public class NotificationController {
    private static final String TAG = "NotificationController";

    private static final int NOTIFICATION_ID = 100;
    private Context mContext;
    private NotificationManager mNotificationManager;

    public NotificationController(Context context) {
        mContext = context;
        mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void displayGeoItem(GeoItem geoItem) {
        Notification notification = createNotification(geoItem.getTitle());
        showNotification(notification);
    }

    public void dismissNotification() {
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    private void showNotification(Notification notification) {

        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    private Notification createNotification(String title) {
        RemoteViews remoteViews = new RemoteViews(
                mContext.getPackageName(),
                R.layout.notification_geo_item_near);
        remoteViews.setTextViewText(R.id.title, title);

        //open app when notification is clicked
        Intent resultIntent = new Intent(mContext, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        resultIntent.setAction(Intent.ACTION_MAIN);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContent(remoteViews)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setContentIntent(resultPendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setVibrate(new long[0]);
        }


        return mBuilder.build();
    }
}
