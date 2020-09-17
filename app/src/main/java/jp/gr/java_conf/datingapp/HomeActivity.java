package jp.gr.java_conf.datingapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import jp.gr.java_conf.datingapp.adapter.HomeViewPagerAdapter;
import jp.gr.java_conf.datingapp.fragment.ChatFragment;
import jp.gr.java_conf.datingapp.fragment.DiscoverFragment;
import jp.gr.java_conf.datingapp.fragment.ProfileFragment;
import jp.gr.java_conf.datingapp.notification.APIService;
import jp.gr.java_conf.datingapp.notification.Client;
import jp.gr.java_conf.datingapp.utility.CloseKeyboard;

public class HomeActivity extends AppCompatActivity implements ChatFragment.MessageListener {

    private ViewPager mViewPager;
    private TabLayout mHomeTabs;
    private HomeViewPagerAdapter adapter;
    private FirebaseAuth mAuth;
    private String uid;
    private FirebaseDatabase database;
    private TextView badge;
    private FirebaseFirestore mStore;
    private DatabaseReference reference;
    private ValueEventListener badgeListener;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private DatabaseReference chatsRef;
    private ChildEventListener notificationListener;
    private Context context;
    private APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        context = this;

        mViewPager = findViewById(R.id.home_view_pager);
        mHomeTabs = findViewById(R.id.home_tab);

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.commit();
        }

        CloseKeyboard.setupUI(findViewById(R.id.constraint_home), this);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        mStore = FirebaseFirestore.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Chats");
        preferences  = getSharedPreferences("DATA", Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.clear().apply();
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
        mHomeTabs.selectTab(mHomeTabs.getTabAt(1));
        chatsRef = FirebaseDatabase.getInstance().getReference("Chats");
        DatabaseReference myRef = database.getReference("/status/" + uid);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

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

        database.getReference("Switch").child(uid).child("on").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    editor.putBoolean("switchOn", (boolean) snapshot.getValue());
                    editor.apply();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (getIntent() != null) {
            if (getIntent().getIntExtra("tabPos", -1) >= 0) {
                mViewPager.setCurrentItem(getIntent().getIntExtra("tabPos", -1));
            }
        }
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

    @Override
    public void onMessageReceived() {
        badge.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAllMessageSeen() {
        badge.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
//        System.out.println("onResume in ChatFragment");
//        notificationListener = chatsRef.addChildEventListener(new ChildEventListener() {
//            private long attachTime = System.currentTimeMillis();
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                long receivedTime = (long) snapshot.child("time_stamp").getValue();
//                if (preferences.getBoolean("switch", true)) {
//                    if (receivedTime > attachTime) {
//                        if (snapshot.child("to").getValue().equals(uid) && !(boolean) snapshot.child("isSeen").getValue()) {
//                            mStore.collection("Users").document((String) snapshot.child("from").getValue())
//                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                @Override
//                                public void onSuccess(DocumentSnapshot documentSnapshot) {
//                                    Profile profile = documentSnapshot.toObject(Profile.class);
//                                    if (profile != null) {
//                                        MessageNotification.sendNotification(profile.getName(), (String) snapshot.child("message").getValue(), context);
//                                    }
//                                }
//                            });
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

    @Override
    public void onPause() {
        super.onPause();
//        System.out.println("onPause in ChatFragment");
//        chatsRef.removeEventListener(notificationListener);
    }
}
