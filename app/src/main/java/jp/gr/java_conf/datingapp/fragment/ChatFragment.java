
package jp.gr.java_conf.datingapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jp.gr.java_conf.datingapp.HomeActivity;
import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.adapter.MatchRecyclerAdapter;
import jp.gr.java_conf.datingapp.model.Chat;
import jp.gr.java_conf.datingapp.model.Match;
import jp.gr.java_conf.datingapp.model.Profile;
import jp.gr.java_conf.datingapp.notification.MessageNotification;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private RecyclerView mChatRecyclerView;
    private List<Match> mChatList;
    private List<Match> mMatchList;
    private MatchRecyclerAdapter mChatRecyclerAdapter;
    private List<String> mUsers;
    private List<String> mMatchUsers;
    private List<String> mInvalidUsers;
    private QuerySnapshot snapshots;
    private FirebaseAuth mAuth;
    private String uid;
    private FirebaseFirestore mStore;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        System.out.println("onCreateView in ChatFragment.");
        mChatRecyclerView = view.findViewById(R.id.chat_recycler);
        mChatList = new ArrayList<>();
        mMatchList = new ArrayList<>();
        mUsers = new ArrayList<>();
        mMatchUsers = new ArrayList<>();
        mInvalidUsers = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        mStore = FirebaseFirestore.getInstance();
        mChatRecyclerView.setHasFixedSize(true);
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mChatRecyclerAdapter = new MatchRecyclerAdapter(getContext(), mChatList, mMatchList);

        mChatRecyclerView.setAdapter(mChatRecyclerAdapter);

        MessageNotification.createNotificationChannel(getContext());

        attachChatListener(view);
        attachBlockListener(view);

        mStore.collection("Users").document(uid)
                .collection("Match").orderBy("time_stamp", Query.Direction.DESCENDING).whereEqualTo("isBlock", false).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        snapshots = task.getResult();
                        createMatchUsers(snapshots);
                    }
                } else {
                    System.out.println(task.getResult());
                }
            }
        });

        DatabaseReference profileRef = FirebaseDatabase.getInstance().getReference("ProfileImage");
        profileRef.addChildEventListener(new ChildEventListener() {
            private long attachTIme = System.currentTimeMillis();
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                long changeTime = (long) snapshot.child("time_stamp").getValue();
                if (changeTime > attachTIme) {
                    if (mMatchUsers.contains(snapshot.getKey())) {
                        updateMatchingUsers(view);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (mMatchUsers.contains(snapshot.getKey())) {
                    updateMatchingUsers(view);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return view;
    }

    private void createMatchUsers(QuerySnapshot snapshotsData) {
        for (DocumentSnapshot documentSnapshot : snapshotsData) {
        Match match = documentSnapshot.toObject(Match.class);
        if (match != null) {
            mMatchUsers.add(match.getUser_id());
        }
    }
    }

    private void attachChatListener(View view) {
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("Chats");
        // 最初の一回のみ
        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    addChatUsers(snapshot, view);
                }
                attachAccountListener(view);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        // リスナーアタッチ
        chatsRef.orderByChild("time_stamp").addChildEventListener(new ChildEventListener() {

            private long attachTime = System.currentTimeMillis();

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                long receivedTime = (long) snapshot.child("time_stamp").getValue();
                if (receivedTime > attachTime) {
                    addChatUsers(snapshot, view);
                    if (snapshot.child("to").getValue().equals(uid)) {
                        mStore.collection("Users").document((String) snapshot.child("from").getValue())
                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Profile profile = documentSnapshot.toObject(Profile.class);
                                if (profile != null) {
                                    MessageNotification.sendNotification(profile.getName(), (String) snapshot.child("message").getValue(), getContext());
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void attachAccountListener(View view) {
        DatabaseReference accountRef = FirebaseDatabase.getInstance().getReference("account");
        // 最初の一回のみ
        accountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    boolean isValid = (boolean) snapshot.child("account_flg").getValue();
                    if (!isValid && mMatchUsers.contains(snapshot.getKey())) {
                        mInvalidUsers.add(snapshot.getKey());
                    }
                }
                updateMatchingUsers(view);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        // リスナーアタッチ
        accountRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                boolean isValid = (boolean) snapshot.child("account_flg").getValue();
                if (!isValid && mMatchUsers.contains(snapshot.getKey())) {
                    if (!mInvalidUsers.contains(snapshot.getKey())) {
                        mInvalidUsers.add(snapshot.getKey());
                        updateMatchingUsers(view);
                    }
                } else if (isValid && mMatchUsers.contains(snapshot.getKey())){
                    mInvalidUsers.remove(snapshot.getKey());
                    updateMatchingUsers(view);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void attachBlockListener(View view) {
        // リスナーアタッチ
        DatabaseReference blockRef = FirebaseDatabase.getInstance().getReference("Block");
        blockRef.addChildEventListener(new ChildEventListener() {
            private long attachTime = System.currentTimeMillis();
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if ((long) snapshot.child("time_stamp").getValue() > attachTime) {
                    if (snapshot.child("block_user_id").getValue().equals(uid)) {
                        updateMatchingUsers(view);
                    } else if (snapshot.child("blocked_user_id").getValue().equals(uid)) {
                        updateMatchingUsers(view);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addChatUsers(DataSnapshot snapshot, View view) {
        boolean isLatest = false;
        if (snapshot.child("from").getValue() != null && snapshot.child("to").getValue() != null) {
            if (snapshot.child("from").getValue().equals(uid)) {
                if (mUsers.size() > 1) {
                    if (!mUsers.get(mUsers.size() - 1).equals(snapshot.child("to").getValue())) {
                        isLatest = true;
                    }
                }
                if (mUsers.contains((String)snapshot.child("to").getValue())) {
                    mUsers.remove((String)snapshot.child("to").getValue());
                    mUsers.add((String)snapshot.child("to").getValue());
                } else {
                    mUsers.add((String)snapshot.child("to").getValue());
                }
            }
            if (snapshot.child("to").getValue().equals(uid)) {
                if (mUsers.size() > 1) {
                    if (!mUsers.get(mUsers.size() - 1).equals(snapshot.child("from").getValue())) {
                        isLatest = true;
                    }
                }
                if (mUsers.contains((String)snapshot.child("from").getValue())) {
                    mUsers.remove((String)snapshot.child("from").getValue());
                    mUsers.add((String)snapshot.child("from").getValue());
                } else {
                    mUsers.add((String)snapshot.child("from").getValue());
                }
            }
            if (isLatest) {
                System.out.println("Latest");
                updateMatchingUsers(view);
            }
        }
    }

    private void updateMatchingUsers(View view) {
        mChatList.clear();
        mMatchList.clear();
        validateUsers(snapshots, view);
    }

    private void validateUsers(QuerySnapshot snapshots, View view) {
        if (hasTalkUsers(mUsers)) {
            System.out.println("hasTalkUsers");
            for (String userId : mUsers) {
                for (DocumentSnapshot documentSnapshot : snapshots) {
                    Match match = documentSnapshot.toObject(Match.class);
                    assert match != null;
                    if (!mInvalidUsers.contains(match.getUser_id())) {
                        if (userId.equals(match.getUser_id()) && !mChatList.contains(match)) {
                            mChatList.add(match);
                        } else if (!mUsers.contains(match.getUser_id()) && !mMatchList.contains(match)) {
                            mMatchList.add(match);
                        }
                        mChatRecyclerAdapter.notifyDataSetChanged();
                    }
                }
            }
        } else {
            System.out.println("nohasTalkUsers");
            for (DocumentSnapshot documentSnapshot : snapshots) {
                Match match = documentSnapshot.toObject(Match.class);
                assert match != null;
                if (!mInvalidUsers.contains(match.getUser_id())) {
                    mMatchList.add(match);
                    mChatRecyclerAdapter.notifyDataSetChanged();
                }
            }
        }

        Collections.reverse(mChatList);

        if (mMatchList.size() == 0 && mChatList.size() == 0) {
            view.findViewById(R.id.no_match).setVisibility(View.VISIBLE);
            view.findViewById(R.id.chat_recycler).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.no_match).setVisibility(View.GONE);
            view.findViewById(R.id.chat_recycler).setVisibility(View.VISIBLE);
        }
    }

    private boolean hasTalkUsers(List<String> mUsers) {
        return mUsers.size() != 0;
    }
}