package jp.gr.java_conf.datingapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import jp.gr.java_conf.datingapp.adapters.HomeViewPagerAdapter;
import jp.gr.java_conf.datingapp.fragments.ChatFragment;
import jp.gr.java_conf.datingapp.fragments.DiscoverFragment;
import jp.gr.java_conf.datingapp.fragments.ProfileFragment;
import jp.gr.java_conf.datingapp.models.Chat;
import jp.gr.java_conf.datingapp.utility.CloseKeyboard;

public class HomeActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private TabLayout mHomeTabs;
    private HomeViewPagerAdapter adapter;
    private ImageButton mAcceptButton;
    private ImageButton mRejectButton;
    private DiscoverFragment fragment;
    private boolean isOnline = true;
    private FirebaseAuth mAuth;
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

        mAuth = FirebaseAuth.getInstance();
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
        DatabaseReference myRef = database.getReference("/status/" + mAuth.getCurrentUser().getUid());

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
                    if (chat.getTo().equals(mAuth.getCurrentUser().getUid()) && !chat.isSeen()) {
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
