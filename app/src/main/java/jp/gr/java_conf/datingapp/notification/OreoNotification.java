package jp.gr.java_conf.datingapp.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import jp.gr.java_conf.datingapp.R;

public class OreoNotification extends ContextWrapper {
    private static final String CHANNEL_MESSAGE_ID = "message_channel";
    private static final String CHANNEL_MESSAGE = "メッセージ通知";
    private static final String CHANNEL_MATCH_ID = "match_channel";
    private static final String CHANNEL_MATCH = "マッチ通知";

    private NotificationManager notificationManager;

    public OreoNotification(Context base) {
        super(base);
    }

    public void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createMessageChannel();
            createMatchChannel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createMessageChannel() {
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_MESSAGE_ID, CHANNEL_MESSAGE, importance);
        channel.enableLights(false);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createMatchChannel() {
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_MATCH_ID, CHANNEL_MATCH, importance);
        channel.enableLights(false);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationCompat.Builder getOreoMessageNotification(String title, String body, PendingIntent pendingIntent, String icon, String image) {
        System.out.println("getOreoMessageNotification");
        if (image != null) {
            Bitmap bitmap = getBitmapfromUrl(image);
            System.out.println("ビットマップ");
            System.out.println(bitmap);
            return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_MESSAGE_ID)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(title)
                    .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap).bigLargeIcon(null))
                    .setLargeIcon(bitmap)
                    .setSmallIcon(Integer.parseInt(icon))
                    .setAutoCancel(true);
        } else {
            return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_MESSAGE_ID)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(Integer.parseInt(icon))
                    .setAutoCancel(true);
        }
    }

    private Bitmap getBitmapfromUrl(String image) {
        try {
            URL url = new URL(image);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);

        } catch (Exception e) {
            Log.e("Send Error.", "画像を送信できませんでした。" + e.getLocalizedMessage());
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationCompat.Builder getOreoMatchNotification(String title, String body, PendingIntent pendingIntent, String icon) {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_MATCH_ID)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(Integer.parseInt(icon))
                .setAutoCancel(true);
    }
}
