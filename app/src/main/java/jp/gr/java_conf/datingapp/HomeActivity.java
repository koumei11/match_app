package jp.gr.java_conf.datingapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.gr.java_conf.datingapp.adapters.HomeViewPagerAdapter;
import jp.gr.java_conf.datingapp.fragments.ChatFragment;
import jp.gr.java_conf.datingapp.fragments.DiscoverFragment;
import jp.gr.java_conf.datingapp.fragments.ProfileFragment;
import jp.gr.java_conf.datingapp.models.Chat;
import jp.gr.java_conf.datingapp.utility.CloseKeyboard;
import jp.gr.java_conf.datingapp.utility.DateTimeConverter;

public class HomeActivity extends AppCompatActivity {

    private static final int SEND_NOTIFICATION = 1;
    private static final int REQUEST_CODE = 100;
    private static final String CHANNEL_ID = "channel_1";

    private ViewPager mViewPager;
    private TabLayout mHomeTabs;
    private HomeViewPagerAdapter adapter;
    private ImageButton mAcceptButton;
    private ImageButton mRejectButton;
    private DiscoverFragment fragment;
    private boolean isOnline = true;
    private boolean isFirstLoading = true;
    private FirebaseAuth mAuth;
    private String uid;
    private String senderName;
    private String receivedMessage;
    private String receivedTime;
    private String tempKey = "";
    private String key = "";
    private FirebaseDatabase database;
    private TextView badge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mViewPager = findViewById(R.id.home_view_pager);
        mHomeTabs = findViewById(R.id.home_tab);

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.commit();
        }

        CloseKeyboard.setupUI(findViewById(R.id.constraint_home), this);
        createNotificationChannel();

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        adapter = new HomeViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ProfileFragment(), "マイプロフィール");
        adapter.addFragment(new DiscoverFragment(), "さがす");
        adapter.addFragment(new ChatFragment(), "チャット");
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(adapter);
        mHomeTabs.setupWithViewPager(mViewPager);
        mHomeTabs.getTabAt(0).setIcon(R.drawable.profile);
        mHomeTabs.getTabAt(1).setIcon(R.drawable.discover);
        mHomeTabs.getTabAt(2).setCustomView(R.layout.notification_badge);
        badge = mHomeTabs.getTabAt(2).getCustomView().findViewById(R.id.notification_text);
        setBadge();
        mHomeTabs.selectTab(mHomeTabs.getTabAt(1));
        DatabaseReference myRef = database.getReference("/status/" + uid);

        Map<String, Object> isOfflineForDatabase = new HashMap<>();
        isOfflineForDatabase.put("state", "offline");
        isOfflineForDatabase.put("last_changed", ServerValue.TIMESTAMP);

        Map<String, Object> isOnlineForDatabase = new HashMap<>();
        isOnlineForDatabase.put("state", "online");
        isOnlineForDatabase.put("last_changed", ServerValue.TIMESTAMP);

        database.getReference(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myRef.onDisconnect().setValue(isOfflineForDatabase).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            myRef.setValue(isOnlineForDatabase);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mHomeTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

//        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference();
//        chatsRef.child("Chats").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                System.out.println("Single");
//                List<String> chatList = new ArrayList<>();
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                   chatList.add(snapshot.getKey());
//                }
//                chatsRef.child("Chats").addChildEventListener(new ChildEventListener() {
//                    @Override
//                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                        System.out.println(snapshot.child("message").getValue());
//                        if (!chatList.contains(snapshot.getKey())) {
//                            System.out.println("含まれていません");
//                        }
//                    }
//
//                    @Override
//                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                        System.out.println("データが変わりました");
//                    }
//
//                    @Override
//                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//                    }
//
//                    @Override
//                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//                addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (!isFirstLoading) {
//
//                        System.out.println("naka");
//                        if (snapshot.child("to").getValue().equals(uid)) {
//                            tempKey = snapshot.getKey();
//                            senderName = (String) snapshot.child("from").getValue();
//                            receivedMessage = (String) snapshot.child("message").getValue();
//                            receivedTime = DateTimeConverter.getSentTime((Long) snapshot.child("time_stamp").getValue());
//                        }

//                    if (!tempKey.equals(key)) {
//                        key = tempKey;
//                        System.out.println(key);
//                        System.out.println(senderName);
//                        System.out.println(receivedMessage);
//                        System.out.println(receivedTime);
//                        sendNotification(senderName, receivedMessage, receivedTime);
//                    }
//                }
//                isFirstLoading = false;
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel1);
            String description = getString(R.string.notify_message);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String senderName,
                                  String receivedMessage,
                                  String receivedTime) {
        int notificationId = SEND_NOTIFICATION;
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                REQUEST_CODE,
                new Intent(HomeActivity.this, HomeActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.heart1)
                .setContentTitle(senderName)
                .setContentText(receivedMessage)
                .setAutoCancel(true);
        notificationBuilder.setContentIntent(pendingIntent);
        Notification notification = notificationBuilder.build();
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 101 : {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        boolean isAccept = data.getBooleanExtra("result", false);
                        if (isAccept) {
                            DiscoverFragment.accept();
                        } else {
                            DiscoverFragment.reject();
                        }
                    }
                }
            }
            break;
        }
    }

    private void setBadge() {
        DatabaseReference ref = database.getReference("Chats");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int unread = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    chat.setSeen((boolean)snapshot.child("isSeen").getValue());
                    if (chat.getTo().equals(uid) && !chat.isSeen()) {
                        unread++;
                    }
                }

                if (unread > 0) {
                    badge.setVisibility(View.VISIBLE);
                    badge.setText(String.valueOf(unread));
                } else {
                    badge.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
