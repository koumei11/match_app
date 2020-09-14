package jp.gr.java_conf.datingapp.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Objects;

import jp.gr.java_conf.datingapp.HomeActivity;
import jp.gr.java_conf.datingapp.R;

public class MessageNotification {
    private static final int SEND_NOTIFICATION = 1;
    private static final int REQUEST_CODE = 100;
    private static final String CHANNEL_ID = "channel_1";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel1);
            String description = context.getString(R.string.notify_message);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = Objects.requireNonNull(context).getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void sendNotification(String senderName,
                                  String receivedMessage,
                                  Context context) {
        int notificationId = SEND_NOTIFICATION;
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE,
                new Intent(context, HomeActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.heart1)
                .setContentTitle(senderName)
                .setContentText(receivedMessage)
                .setAutoCancel(true);
        notificationBuilder.setContentIntent(pendingIntent);
        android.app.Notification notification = notificationBuilder.build();
        notification.flags = android.app.Notification.DEFAULT_LIGHTS | android.app.Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);
    }
}
