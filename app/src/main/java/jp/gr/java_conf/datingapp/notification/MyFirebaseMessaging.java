package jp.gr.java_conf.datingapp.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import jp.gr.java_conf.datingapp.HomeActivity;
import jp.gr.java_conf.datingapp.enums.NotificationType;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "channel_1";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String sent = remoteMessage.getData().get("sent");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert sent != null;
        if (firebaseUser != null && sent.equals(firebaseUser.getUid())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sendOreoNotification(remoteMessage, Objects.requireNonNull(remoteMessage.getData().get("type")));
            } else {
                sendNotification(remoteMessage);
            }
        }
    }

    @Override
    public void onNewToken(String refreshToken) {
        super.onNewToken(refreshToken);
        System.out.println("新しいトークン");
        System.out.println(refreshToken);
        if (refreshToken != null) {
            updateToken(refreshToken);
        }
    }

    private void updateToken(String refreshToken) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Token");
            Token token = new Token(refreshToken);
            reference.child(firebaseUser.getUid()).setValue(token);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendOreoNotification(RemoteMessage remoteMessage, String type) {
        String userId = remoteMessage.getData().get("userId");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String image = remoteMessage.getData().get("image");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(userId.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, HomeActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);
        intent.putExtras(bundle);
        intent.putExtra("tabPos", 2);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        OreoNotification oreoNotification = new OreoNotification(this);
        NotificationCompat.Builder builder;
        switch (type) {
            case "MESSAGE":
                builder = oreoNotification.getOreoMessageNotification(title, body, pendingIntent, icon, image);
                break;
            case "MATCH":
                builder = oreoNotification.getOreoMatchNotification(title, body, pendingIntent, icon);
                break;
            default:
                builder = oreoNotification.getOreoMessageNotification(title, body, pendingIntent, icon, image);
        }

        int i = 0;

        if (j > 0) {
            i = j;
        }

        oreoNotification.getManager().notify(i, builder.build());
    }

    public void sendNotification(RemoteMessage remoteMessage) {
        String userId = remoteMessage.getData().get("userId");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String image = remoteMessage.getData().get("image");

        System.out.println("sendNotification in Messaging");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(userId.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, HomeActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);
        intent.putExtras(bundle);
        intent.putExtra("tabPos", 2);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder;
        if (image != null) {
            Bitmap bitmap = getBitmapfromUrl(image);
            System.out.println("ビットマップ");
            System.out.println(bitmap);
            notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(Integer.parseInt(icon))
                    .setContentTitle(title)
                    .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap).bigLargeIcon(null))
                    .setLargeIcon(bitmap)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);
        } else {
            notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(Integer.parseInt(icon))
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);
        }


        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0) {
            i = j;
        }
        manager.notify(i, notificationBuilder.build());
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
}
