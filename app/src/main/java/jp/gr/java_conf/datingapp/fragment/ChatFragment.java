package jp.gr.java_conf.datingapp.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.adapter.MatchRecyclerAdapter;
import jp.gr.java_conf.datingapp.model.Chat;
import jp.gr.java_conf.datingapp.model.Match;
import jp.gr.java_conf.datingapp.utility.DateTimeConverter;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private RecyclerView mMatchRecyclerView;
    private List<Match> mChatList;
    private List<Match> mMatchList;
    private MatchRecyclerAdapter mMatchRecyclerAdapter;
    private DatabaseReference databaseReference;
    private QuerySnapshot snapshotsData;
    List<String> mUsers;
    FirebaseAuth mAuth;
    FirebaseFirestore mStore;
    View messageView;
    TextView newMessage;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        mMatchRecyclerView = view.findViewById(R.id.match_recycler);
        mChatList = new ArrayList<>();
        mMatchList = new ArrayList<>();
        mUsers = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mMatchRecyclerView.setHasFixedSize(true);
        mMatchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        System.out.println("onCreateView in ChatFragment");

        messageView = LayoutInflater.from(getContext()).inflate(R.layout.single_match_user_item, null);
        newMessage = messageView.findViewById(R.id.newest_message);

        mMatchRecyclerAdapter = new MatchRecyclerAdapter(getContext(), mChatList);
        mMatchRecyclerView.setAdapter(mMatchRecyclerAdapter);

        mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                .collection("Match").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    snapshotsData = task.getResult();
                    updateMatchingUsers(snapshotsData);
                } else {
                    System.out.println("シッパイ");
                    System.out.println(task.getResult());
                }
            }
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.orderByChild("time_stamp").addChildEventListener(new ChildEventListener() {
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
                    updateMatchingUsers(snapshotsData);
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

        return view;
    }

    private void updateMatchingUsers(QuerySnapshot snapshots) {
        mChatList.clear();
        mMatchList.clear();
        Match match;
        for (String userId : mUsers) {
            for (DocumentSnapshot documentSnapshot : snapshots) {
                match = documentSnapshot.toObject(Match.class);
                assert match != null;
                System.out.println(mChatList.contains(match));
                if (userId.equals(match.getUser_id()) && !mChatList.contains(match)) {
                    mChatList.add(match);
                } else if (!userId.equals(match.getUser_id()) && !mMatchList.contains(match)){
                    mMatchList.add(match);
                }
                mMatchRecyclerAdapter.notifyDataSetChanged();
            }
        }
        Collections.reverse(mChatList);
        System.out.println(mChatList);
    }
}
