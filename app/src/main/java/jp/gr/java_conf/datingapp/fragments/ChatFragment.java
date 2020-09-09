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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    List<String> mUsers;
    FirebaseAuth mAuth;
    FirebaseFirestore mStore;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        mChatRecyclerView = view.findViewById(R.id.chat_recycler);
        mChatList = new ArrayList<>();
        mMatchList = new ArrayList<>();
        mUsers = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mChatRecyclerView.setHasFixedSize(true);
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mChatRecyclerAdapter = new MatchRecyclerAdapter(getContext(), mChatList, mMatchList);

        mChatRecyclerView.setAdapter(mChatRecyclerAdapter);

        mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                .collection("Match").whereEqualTo("isBlock", false).orderBy("time_stamp", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    snapshotsData = task.getResult();
                    System.out.println("onComplete");
                    updateMatchingUsers(snapshotsData, view);
                } else {
                    System.out.println(task.getResult());
                }
            }
        });

        DatabaseReference ChatsReference = FirebaseDatabase.getInstance().getReference("Chats");
        ChatsReference.orderByChild("time_stamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.child("from").getValue() != null && snapshot.child("to").getValue() != null) {
                    if (snapshot.child("from").getValue().equals(mAuth.getCurrentUser().getUid())) {
                        if (mUsers.contains((String)snapshot.child("to").getValue())) {
                            mUsers.remove((String)snapshot.child("to").getValue());
                            mUsers.add((String)snapshot.child("to").getValue());
                        } else {
                            mUsers.add((String)snapshot.child("to").getValue());
                        }
                    }
                    if (snapshot.child("to").getValue().equals(mAuth.getCurrentUser().getUid())) {
                        if (mUsers.contains((String)snapshot.child("from").getValue())) {
                            mUsers.remove((String)snapshot.child("from").getValue());
                            mUsers.add((String)snapshot.child("from").getValue());
                        } else {
                            mUsers.add((String)snapshot.child("from").getValue());
                        }
                    }
                }
                if (snapshotsData != null) {
                    updateMatchingUsers(snapshotsData, view);
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

        DatabaseReference blockReference = FirebaseDatabase.getInstance().getReference("Block");
        blockReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                System.out.println("onChildAdded");
                mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                        .collection("Match").whereEqualTo("isBlock", false).orderBy("time_stamp", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            snapshotsData = task.getResult();
                            updateMatchingUsers(snapshotsData, view);
                        } else {
                            System.out.println(task.getResult());
                        }
                    }
                });
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

        return view;
    }

    private void updateMatchingUsers(QuerySnapshot snapshots, View view) {
        mChatList.clear();
        mMatchList.clear();
        if (hasTalkUsers(mUsers)) {
            for (DocumentSnapshot documentSnapshot : snapshots) {
                Match match = documentSnapshot.toObject(Match.class);
                assert match != null;
                mMatchList.add(match);
                mChatRecyclerAdapter.notifyDataSetChanged();
            }
        } else {
            for (String userId : mUsers) {
                for (DocumentSnapshot documentSnapshot : snapshots) {
                    Match match = documentSnapshot.toObject(Match.class);
                    assert match != null;
                    if (userId.equals(match.getUser_id()) && !mChatList.contains(match)) {
                        mChatList.add(match);
                    } else if (!mUsers.contains(match.getUser_id()) && !mMatchList.contains(match)){
                        mMatchList.add(match);
                    }
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
        return mUsers.size() == 0;
    }
}