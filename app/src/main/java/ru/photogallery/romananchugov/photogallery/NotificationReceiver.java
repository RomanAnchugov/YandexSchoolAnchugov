package ru.photogallery.romananchugov.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by romananchugov on 10.01.2018.
 */

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "received result: " + getResultCode());
        if(getResultCode() != Activity.RESULT_OK){
            return;
        }

        int requestCode = intent.getIntExtra(PollService.REQUEST_CODE, 0);
        Notification notification = (Notification) intent.getParcelableExtra(PollService.NOTIFICATION);

        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("Channel_id", "channel_name", NotificationManager.IMPORTANCE_HIGH);
        }

        NotificationManagerCompat nm = NotificationManagerCompat.from(context);

        nm.notify(requestCode, notification);

    }
}
