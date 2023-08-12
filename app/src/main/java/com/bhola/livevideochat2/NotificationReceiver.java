package com.bhola.livevideochat2;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "notification_channel_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        String userName = intent.getStringExtra("USERNAME");

        showNotification(context,userName);
    }

    @SuppressLint("MissingPermission")
    private void showNotification(Context context, String userName) {

        Intent notificationIntent = new Intent(context, VipMembership.class); // Replace TargetActivity with your desired activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle("\uD83D\uDC8B Your 20% offer is expiring soon!!⏰⏰")
                .setContentText("Hi! "+userName+" \uD83D\uDC59 your 20% offer will expiring in 30 minutes \uD83D\uDC8B \nBuy Desi Girls Premium at 79 only now and talk to unlimited girls")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // Set the PendingIntent
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, builder.build());
    }
}
