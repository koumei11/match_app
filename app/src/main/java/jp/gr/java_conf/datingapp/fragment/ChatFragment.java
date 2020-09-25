
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
    private DataSnapshot snapshotsData;
    private FirebaseAuth mAuth;
    private String uid;
    private FirebaseFirestore mStore;
    private View view;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private DatabaseReference chatsRef;
    private ProgressBar progressBar;
    private String tempVal;
    private DatabaseReference reference;

    public ChatFragment() {
        // Required empty public constructor
    }

    public interface MessageListener {
        void onMessageReceived();
        void onAllMessageSeen();
        void onMatchCreated();
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
        reference = FirebaseDatabase.getInstance().getReference();
        mMatchRecyclerView.setHasFixedSize(true);
        progressBar = view.findViewById(R.id.progress_bar_chat);
        preferences = getContext().getSharedPreferences("DATA", Context.MODE_PRIVATE);
        editor = preferences.edit();
        mMatchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatsRef = reference.child("Chats");

        progressBar.setVisibility(View.VISIBLE);
        mMatchRecyclerView.setVisibility(View.GONE);

        mMatchRecyclerAdapter = new MatchRecyclerAdapter(getContext(), mChatList, mMatchList);

        mMatchRecyclerView.setAdapter(mMatchRecyclerAdapter);

        retrieveMatchUserData();

        DatabaseReference profileRef = reference.child("ProfileImage");
        profileRef.addChildEventListener(new ChildEventListener() {
            private long attachTIme = System.currentTimeMillis();
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                long changeTime = (long) snapshot.child("time_stamp").getValue();
                if (changeTime >= attachTIme) {
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

        reference.child("Match").child(uid).addChildEventListener(new ChildEventListener() {

            private long attachTime = System.currentTimeMillis();

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                long matchTime = (long) snapshot.child("time_stamp").getValue();
                if (matchTime > attachTime) {
                    retrieveMatchUserData();
                    if (preferences.getInt("new_match", 0) <= 0) {
                        editor.putInt("new_match", 1).apply();
                    }
                    mListener.onMatchCreated();
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
        reference.child("Match").child(uid).orderByChild("block").equalTo(false).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshots) {
                snapshotsData = snapshots;
                createMatchUsers(snapshotsData);
                attachChatListener(view);
                attachBlockListener(view);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createMatchUsers(DataSnapshot snapshotsData) {
        for (DataSnapshot snapshot : snapshotsData.getChildren()) {
//            if (snapshot.child("user1").getValue().equals(uid) || snapshot.child("user2").getValue().equals(uid)) {
                Match match = snapshot.getValue(Match.class);
                if (match != null) {
                    if (match.getUser1().equals(uid)) {
                        mMatchUsers.add(match.getUser2());
                    } else {
                        mMatchUsers.add(match.getUser1());
                    }
                }
//            }
        }
    }

    private void updateToken(String token) {
        DatabaseReference tokenRef = reference.child("Token");
        Token tokenInstance = new Token(token);
        tokenRef.child(uid).setValue(tokenInstance);
    }

    private void attachChatListener(View view) {
        // 最初の一回のみ
        chatsRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalUnreadMessages = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    setChatMetaData(snapshot);
                    addChatUsers(snapshot);
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
        chatsRef.child(uid).orderByChild("time_stamp").addChildEventListener(new ChildEventListener() {

            private long attachTime = System.currentTimeMillis();

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                long receivedTime = (long) snapshot.child("time_stamp").getValue();
                if (!snapshot.getKey().equals(tempVal))
                    setChatMetaData(snapshot);
                    tempVal = snapshot.getKey();
                    if (receivedTime > attachTime) {
                        mListener.onMessageReceived();
                        addChatUsers(snapshot);
                        updateMatchingUsers(view);
                    }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                setChatMetaData(snapshot);
                updateMatchingUsers(view);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (!(boolean)snapshot.child("isSeen").getValue()) {
                    chatsRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
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
        DatabaseReference accountRef = reference.child("account");
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
        DatabaseReference blockRef = reference.child("Block");
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

    private void updateMatchingUsers(View view) {
        mChatList.clear();
        mMatchList.clear();
        validateUsers(snapshotsData, view);
    }

    private void validateUsers(DataSnapshot snapshots, View view) {
        if (hasTalkUsers(mUsers)) {
            for (String userId : mUsers) {
                for (DataSnapshot snapshot : snapshots.getChildren()) {
                    if (snapshot.child("user1").getValue().equals(uid) || snapshot.child("user2").getValue().equals(uid)) {
                        Match match = snapshot.getValue(Match.class);
                        assert match != null;
                        if (!mInvalidUsers.contains(match.getUser1()) && !mInvalidUsers.contains(match.getUser2())) {
                            if ((userId.equals(match.getUser1()) || userId.equals(match.getUser2())) && !mChatList.contains(match)) {
                                mChatList.add(match);
                            } else if ((!mUsers.contains(match.getUser1()) && !mUsers.contains(match.getUser2())) && !mMatchList.contains(match)) {
                                mMatchList.add(match);
                            }
                        }
                    }
                }
            }
        } else {
            for (DataSnapshot snapshot : snapshots.getChildren()) {
                if (snapshot.child("user1").getValue().equals(uid) || snapshot.child("user2").getValue().equals(uid)) {
                    Match match = snapshot.getValue(Match.class);
                    assert match != null;
                    if (!mInvalidUsers.contains(match.getUser1()) && !mInvalidUsers.contains(match.getUser2())) {
                        mMatchList.add(match);
                    }
                }
            }
        }
        mMatchRecyclerAdapter.notifyDataSetChanged();

        Collections.reverse(mChatList);
        Collections.reverse(mMatchList);
        if (mMatchList.size() == 0 && mChatList.size() == 0) {
            System.out.println("ここ");
            view.findViewById(R.id.no_match).setVisibility(View.VISIBLE);
            mMatchRecyclerView.setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.no_match).setVisibility(View.GONE);
            mMatchRecyclerView.setVisibility(View.VISIBLE);
        }

        progressBar.setVisibility(View.GONE);
        mMatchRecyclerView.setVisibility(View.VISIBLE);
    }

    private void setChatMetaData(DataSnapshot snapshot) {
        Chat chat = snapshot.getValue(Chat.class);
        editor.putInt("totalUnreadMessages", 0).apply();
//        int totalUnreadMessages = 0;
        assert chat != null;
        chat.setFirstMessageOfTheDay((boolean) snapshot.child("isFirstMessageOfTheDay").getValue());
        chat.setSeen((boolean) snapshot.child("isSeen").getValue());
        Map<String, Object> map = new HashMap<>();
        map.put("message_stock", 0);
        Gson gson = new Gson();
        if (chat.getTo().equals(uid) && !chat.isSeen()) {
//            totalUnreadMessages += 1;
            if (preferences.getString(chat.getFrom(), null) != null) {
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
                map.put("last_message", "画像が送信されました。");
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
    }
}