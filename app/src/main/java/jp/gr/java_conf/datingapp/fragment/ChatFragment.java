
package jp.gr.java_conf.datingapp.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.adapter.MatchRecyclerAdapter;
import jp.gr.java_conf.datingapp.model.Chat;
import jp.gr.java_conf.datingapp.model.Match;
import jp.gr.java_conf.datingapp.notification.Token;
import jp.gr.java_conf.datingapp.utility.DateTimeConverter;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private RecyclerView mMatchRecyclerView;
    private List<Match> mChatList;
    private List<Match> mMatchList;
    private MatchRecyclerAdapter mMatchRecyclerAdapter;
    private List<String> mUsers;
    private List<String> mMatchUsers;
    private List<String> mInvalidUsers;
    private QuerySnapshot snapshotsData;
    private FirebaseAuth mAuth;
    private String uid;
    private FirebaseFirestore mStore;
    private View view;
    private ChildEventListener chatListener;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private DatabaseReference chatsRef;
    private ProgressBar progressBar;

    public ChatFragment() {
        // Required empty public constructor
    }

    public interface MessageListener {
        void onMessageReceived();
        void onAllMessageSeen();
    }

    private MessageListener mListener;

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);

        if (!(context instanceof MessageListener)) {
            throw new ClassCastException("activity が MessageListener を実装していません.");
        }

        mListener = ((MessageListener) context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_chat, container, false);
        mMatchRecyclerView = view.findViewById(R.id.chat_recycler);
        mChatList = new ArrayList<>();
        mMatchList = new ArrayList<>();
        mUsers = new ArrayList<>();
        mMatchUsers = new ArrayList<>();
        mInvalidUsers = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        mStore = FirebaseFirestore.getInstance();
        mMatchRecyclerView.setHasFixedSize(true);
        progressBar = view.findViewById(R.id.progress_bar_chat);
        preferences  = getContext().getSharedPreferences("DATA", Context.MODE_PRIVATE);
        editor = preferences.edit();
        mMatchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatsRef = FirebaseDatabase.getInstance().getReference("Chats");
        mMatchRecyclerView.setVisibility(View.GONE);

        mMatchRecyclerAdapter = new MatchRecyclerAdapter(getContext(), mChatList, mMatchList);

        mMatchRecyclerView.setAdapter(mMatchRecyclerAdapter);

        retrieveMatchUserData();

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

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                System.out.println("トークンが変わりました");
                System.out.println(instanceIdResult.getToken());
                updateToken(instanceIdResult.getToken());
            }
        });

        return view;
    }

    private void retrieveMatchUserData() {
        mStore.collection("Users").document(uid)
                .collection("Match").orderBy("time_stamp", Query.Direction.DESCENDING).whereEqualTo("isBlock", false).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        snapshotsData = task.getResult();
                        createMatchUsers(snapshotsData);
                        attachChatListener(view);
                        attachBlockListener(view);
                        mMatchRecyclerView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                } else {
                    System.out.println(task.getResult());
                }
            }
        });
    }

    private void createMatchUsers(QuerySnapshot snapshotsData) {
        for (DocumentSnapshot documentSnapshot : snapshotsData) {
            Match match = documentSnapshot.toObject(Match.class);
            if (match != null) {
                mMatchUsers.add(match.getUser_id());
            }
        }
    }

    private void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Token");
        Token tokenInstance = new Token(token);
        reference.child(uid).setValue(tokenInstance);
    }

    private void attachChatListener(View view) {
        // 最初の一回のみ
        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalUnreadMessages = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    addChatUsers(snapshot, view);
                    if (snapshot.child("to").getValue().equals(uid)
                            && !(boolean)snapshot.child("isSeen").getValue()) {
                        totalUnreadMessages += 1;
                    }
                }
                if (totalUnreadMessages != 0) {
                    mListener.onMessageReceived();
                }
                attachAccountListener(view);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // リスナーアタッチ
        chatListener = chatsRef.orderByChild("time_stamp").addChildEventListener(new ChildEventListener() {
            private long attachTime = System.currentTimeMillis();
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                setChatMetaData(snapshot);
                long receivedTime = (long) snapshot.child("time_stamp").getValue();
                if (receivedTime > attachTime) {
                    if (snapshot.child("to").getValue().equals(uid)
                            || snapshot.child("from").getValue().equals(uid)) {
                        mListener.onMessageReceived();
                        addChatUsers(snapshot, view);
                        updateMatchingUsers(view);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.child("to").getValue().equals(uid)
                        || snapshot.child("from").getValue().equals(uid)) {
                    setChatMetaData(snapshot);
                    updateMatchingUsers(view);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                System.out.println("Chat Removed!!");
                System.out.println(preferences.getInt("totalUnreadMessages", -1));
                if (!(boolean)snapshot.child("isSeen").getValue()) {
                    chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshots) {
                            int totalUnreadMessages = 0;
                            for (DataSnapshot snapshot : snapshots.getChildren()) {
                                if (snapshot.child("to").getValue().equals(uid) && !(boolean) snapshot.child("isSeen").getValue()) {
                                    totalUnreadMessages += 1;
                                }
                            }
                            if (totalUnreadMessages == 0) {
                                mListener.onAllMessageSeen();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
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
                        retrieveMatchUserData();
                    } else if (snapshot.child("blocked_user_id").getValue().equals(uid)) {
                        retrieveMatchUserData();
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

    private void updateMatchingUsers(View view) {
        System.out.println("updateMatchingUsers");
        mChatList.clear();
        mMatchList.clear();
        validateUsers(snapshotsData, view);
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
                }
            }
        }
        mMatchRecyclerAdapter.notifyDataSetChanged();

        Collections.reverse(mChatList);

        if (mMatchList.size() == 0 && mChatList.size() == 0) {
            view.findViewById(R.id.no_match).setVisibility(View.VISIBLE);
            view.findViewById(R.id.chat_recycler).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.no_match).setVisibility(View.GONE);
            view.findViewById(R.id.chat_recycler).setVisibility(View.VISIBLE);
        }
    }

    private void setChatMetaData(DataSnapshot snapshot) {
        Chat chat = snapshot.getValue(Chat.class);
        editor.putInt("totalUnreadMessages", 0).apply();
        int totalUnreadMessages = 0;
        assert chat != null;
        chat.setFirstMessageOfTheDay((boolean) snapshot.child("isFirstMessageOfTheDay").getValue());
        chat.setSeen((boolean) snapshot.child("isSeen").getValue());
        Map<String, Object> map = new HashMap<>();
        map.put("message_stock", 0);
        Gson gson = new Gson();
        if (chat.getTo().equals(uid) && !chat.isSeen()) {
            totalUnreadMessages += 1;
            if (preferences.getString(chat.getFrom(), null) != null) {
                System.out.println("あり");
                String previousJson = preferences.getString(chat.getFrom(), "");
                Map previousMap = gson.fromJson(previousJson, Map.class);
                int stock = (int) (double)previousMap.get("message_stock") + 1;
                map.put("message_stock", stock);
            } else {
                System.out.println("なし");
                map.put("message_stock", 1);
            }
        }
        if (chat.getTo().equals(uid)) {
            if (chat.getMessage() != null) {
                map.put("last_message", chat.getMessage());
            } else {
                map.put("last_message", getString(R.string.send_image));
            }
            map.put("time_stamp", DateTimeConverter.getSentTime(chat.getTime_stamp()));
            String json = gson.toJson(map);
            editor.putString(chat.getFrom(), json);
        }

        if (chat.getFrom().equals(uid)) {
            if (chat.getMessage() != null) {
                map.put("last_message", chat.getMessage());
            } else {
                map.put("last_message", getString(R.string.send_image));
            }
            map.put("time_stamp", DateTimeConverter.getSentTime(chat.getTime_stamp()));
            String json = gson.toJson(map);
            editor.putString(chat.getTo(), json);
        }

        editor.apply();
    }


    private boolean hasTalkUsers(List<String> mUsers) {
        return mUsers.size() != 0;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (preferences.getInt("totalUnreadMessages", -1) == 0) {
            mListener.onAllMessageSeen();
        }
        System.out.println("onResume");
        System.out.println(preferences.getInt("totalUnreadMessages", 0));
    }
}