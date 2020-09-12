package jp.gr.java_conf.datingapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.adapters.MatchRecyclerAdapter;
import jp.gr.java_conf.datingapp.models.Match;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private RecyclerView mChatRecyclerView;
    private List<Match> mChatList;
    private List<Match> mMatchList;
    private MatchRecyclerAdapter mChatRecyclerAdapter;
    private QuerySnapshot snapshotsData;
    private boolean isFirstLoadingChats = true;
    private boolean isFirstLoadingAccounts = true;
    private List<String> mUsers;
    private List<String> mMatchUsers;
    private List<String> mInvalidUsers;
    private Set<String> mBlockUsers;
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

        mChatRecyclerView = view.findViewById(R.id.chat_recycler);
        mChatList = new ArrayList<>();
        mMatchList = new ArrayList<>();
        mUsers = new ArrayList<>();
        mMatchUsers = new ArrayList<>();
        mInvalidUsers = new ArrayList<>();
        mBlockUsers = new HashSet<>();
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        mStore = FirebaseFirestore.getInstance();
        mChatRecyclerView.setHasFixedSize(true);
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mChatRecyclerAdapter = new MatchRecyclerAdapter(getContext(), mChatList, mMatchList);

        mChatRecyclerView.setAdapter(mChatRecyclerAdapter);

        mStore.collection("Users").document(uid)
                .collection("Match").orderBy("time_stamp", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    snapshotsData = task.getResult();
                    if (snapshotsData != null) {
                        createMatchUsers(snapshotsData);
                        attachChatListener(view);
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
                long changeTime = (long) snapshot.child("change_time").getValue();
                if (changeTime > attachTIme) {
                    if (mMatchUsers.contains(snapshot.getKey())) {
//                        updateMatchingUsers(snapshotsData, view);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (mMatchUsers.contains(snapshot.getKey())) {
//                    updateMatchingUsers(snapshotsData, view);
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
        chatsRef.orderByChild("time_stamp").addChildEventListener(new ChildEventListener() {

            private long attachTime = System.currentTimeMillis();

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                addChatUsers(snapshot);
                long receivedTime = (long) snapshot.child("time_stamp").getValue();
                if (receivedTime > attachTime) {
                    System.out.println("not attach");
//                    updateMatchingUsers(snapshotsData, view);
                } else {
                    System.out.println("attach");
                    attachAccountListener(view);
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
        accountRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                boolean isValid = (boolean) snapshot.child("account_flg").getValue();
                if (!isValid && mMatchUsers.contains(snapshot.getKey())) {
                    mInvalidUsers.add(snapshot.getKey());
                }
                attachBlockListener(view);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                boolean isValid = (boolean) snapshot.child("account_flg").getValue();
                if (!isValid && mMatchUsers.contains(snapshot.getKey())) {
                    if (!mInvalidUsers.contains(snapshot.getKey())) {
                        mInvalidUsers.add(snapshot.getKey());
//                        updateMatchingUsers(snapshotsData, view);
                    }
                } else if (isValid && mMatchUsers.contains(snapshot.getKey())){
                    mInvalidUsers.remove(snapshot.getKey());
//                    updateMatchingUsers(snapshotsData, view);
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
        // ブロックが皆無の時
//        updateMatchingUsers(snapshotsData, view, "from 外");
        DatabaseReference blockRef = FirebaseDatabase.getInstance().getReference("Block");
        blockRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                System.out.println("ChildEventListener in block");
                if (snapshot.child("block_user_id").getValue().equals(uid)) {
                    mBlockUsers.add((String) snapshot.child("blocked_user_id").getValue());
                } else if (snapshot.child("blocked_user_id").getValue().equals(uid)) {
                    mBlockUsers.add((String) snapshot.child("block_user_id").getValue());
                }
//                updateMatchingUsers(snapshotsData, view, "from内");
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

    private void addChatUsers(DataSnapshot snapshot) {
        if (snapshot.child("from").getValue() != null && snapshot.child("to").getValue() != null) {
            if (snapshot.child("from").getValue().equals(uid)) {
                if (mUsers.contains((String)snapshot.child("to").getValue())) {
                    mUsers.remove((String)snapshot.child("to").getValue());
                    mUsers.add((String)snapshot.child("to").getValue());
                } else {
                    mUsers.add((String)snapshot.child("to").getValue());
                }
            }
            if (snapshot.child("to").getValue().equals(uid)) {
                if (mUsers.contains((String)snapshot.child("from").getValue())) {
                    mUsers.remove((String)snapshot.child("from").getValue());
                    mUsers.add((String)snapshot.child("from").getValue());
                } else {
                    mUsers.add((String)snapshot.child("from").getValue());
                }
            }
        }
    }

    private void updateMatchingUsers(QuerySnapshot snapshots, View view, String from) {
        System.out.println(from);
        System.out.println("退会済みユーザー");
        System.out.println(mInvalidUsers);
        System.out.println("ブロックユーザー");
        System.out.println(mBlockUsers);
        mChatList.clear();
        mMatchList.clear();
        if (hasTalkUsers(mUsers)) {
            System.out.println("hasTalkUsers");
            for (String userId : mUsers) {
                for (DocumentSnapshot documentSnapshot : snapshots) {
                    Match match = documentSnapshot.toObject(Match.class);
                    assert match != null;
                    if (!mInvalidUsers.contains(match.getUser_id()) && !mBlockUsers.contains(match.getUser_id())) {
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
                if (!mInvalidUsers.contains(match.getUser_id()) && !mBlockUsers.contains(match.getUser_id())) {
                    mMatchList.add(match);
                    mChatRecyclerAdapter.notifyDataSetChanged();
                }
            }
        }

        System.out.println(mMatchList);
        System.out.println(mChatList);

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
