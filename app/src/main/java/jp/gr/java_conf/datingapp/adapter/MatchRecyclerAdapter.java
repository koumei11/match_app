package jp.gr.java_conf.datingapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.gr.java_conf.datingapp.ChatActivity;
import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.listener.MessageSentListener;
import jp.gr.java_conf.datingapp.model.Chat;
import jp.gr.java_conf.datingapp.model.Match;
import jp.gr.java_conf.datingapp.model.Profile;
import jp.gr.java_conf.datingapp.model.SwitchButton;
import jp.gr.java_conf.datingapp.model.UserState;
import jp.gr.java_conf.datingapp.utility.AgeCalculation;

public class MatchRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements MessageSentListener {

    private Context mContext;
    private List<Match> mChatList;
    private List<Match> mMatchList;
    private final FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final String myId = mAuth.getCurrentUser().getUid();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    private DatabaseReference ref;
    private String theLastMessage;
    private String theSentDate;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public MatchRecyclerAdapter(Context context, List<Match> chatList, List<Match> matchList) {
        mContext = context;
        mChatList = chatList;
        mMatchList = matchList;
        preferences = context.getSharedPreferences("DATA", Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_matches, parent, false);
            return new MatchViewHolder(view);
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.single_match_user_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (position != 0) {
            String userId;
            if (mChatList.get(position - 1).getUser1().equals(myId)) {
                userId = mChatList.get(position - 1).getUser2();
            } else {
                userId = mChatList.get(position - 1).getUser1();
            }
            ChatViewHolder chatViewHolder = (ChatViewHolder) holder;
            ref = database.getReference("/status/" + userId);
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserState userState = snapshot.getValue(UserState.class);
                    Match match = new Match(userId, myId);
                    if (userState != null && mChatList.contains(match)) {
                        Date now = new Date();
                        long timePassed = (now.getTime() - userState.getLast_changed()) / 1000 / 60 / 60;
                        if (userState.getState().equals("online")) {
                            chatViewHolder.mStatus.setBorderColor(mContext.getResources().getColor(R.color.colorGreen));
                        } else if (timePassed < 24) {
                            chatViewHolder.mStatus.setBorderColor(mContext.getResources().getColor(R.color.colorYellow));
                        } else {
                            chatViewHolder.mStatus.setBorderColor(mContext.getResources().getColor(R.color.colorLightGrey));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            showMessage(userId, chatViewHolder.mSentDate, chatViewHolder.mNewestMessage);
            showNumberOfUnreadMessage(userId, chatViewHolder.mNotificationText);
            mStore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful() && task.getResult() != null) {
                        Profile profile = task.getResult().toObject(Profile.class);
                        chatViewHolder.mName.setText(profile.getName());
                        chatViewHolder.mAddress.setText(profile.getAddress());
                        if (profile.getImg_url() != null) {
                            Glide.with(mContext).load(profile.getImg_url()).into(chatViewHolder.mImg);
                        } else {
                            chatViewHolder.mImg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.avatornew));
                        }
                        try {
                            int age = AgeCalculation.calculate(profile.getDate());
                            chatViewHolder.mUserAge.setText(age + "歳");
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            chatViewHolder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.setEnabled(false);
                    Intent intent = new Intent(mContext, ChatActivity.class);
                    mStore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                Profile profile = task.getResult().toObject(Profile.class);
                                intent.putExtra("profile", profile);
                                intent.putExtra("doc_id", userId);
                                intent.putExtra("user_img", profile.getImg_url());
                                intent.putExtra("user_name", profile.getName());
                                intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                                mContext.startActivity(intent);
                                view.setEnabled(true);
                            }
                        }
                    });
                }
            });

            chatViewHolder.view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.block);
                    builder.setMessage(R.string.alert_block);
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            System.out.println("はい");
                            blockUser(userId);
                        }
                    });
                    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;
                }
            });
        } else {
            MatchViewHolder matchViewHolder = (MatchViewHolder) holder;
            if (mMatchList.size() != 0 || mChatList.size() != 0) {
                if (mMatchList.size() != 0) {
                    matchViewHolder.view.setVisibility(View.VISIBLE);
                    matchViewHolder.mMatchRecyclerView.setVisibility(View.VISIBLE);
                    matchViewHolder.mPlainText.setVisibility(View.GONE);
                    matchViewHolder.mMatchRecyclerView.setHasFixedSize(true);
                    matchViewHolder.mMatchRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
                    matchViewHolder.mMatchRecyclerView.setAdapter(matchViewHolder.mMatchRecyclerAdapter);
                } else {
                    matchViewHolder.mMatchRecyclerView.setVisibility(View.GONE);
                    matchViewHolder.mPlainText.setVisibility(View.VISIBLE);
                }

                matchViewHolder.mSwitch.setChecked(preferences.getBoolean("switchOn", true));
                matchViewHolder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        SwitchButton isOn = new SwitchButton(compoundButton.isChecked());
                        database.getReference("Switch").child(mAuth.getCurrentUser().getUid())
                                .setValue(isOn);
                        editor.putBoolean("switchOn", compoundButton.isChecked());
                        editor.apply();
                    }
                });

                if (mChatList.size() == 0) {
                    matchViewHolder.mChatText.setText("");
                } else {
                    matchViewHolder.mChatText.setText(mContext.getString(R.string.during_chat));
                }
            } else {
                System.out.println("マッチも会話もない");
                matchViewHolder.mMatchRecyclerView.setVisibility(View.GONE);
                matchViewHolder.view.setVisibility(View.GONE);
            }
        }
    }

    private void showNumberOfUnreadMessage(String uid, TextView notificationText) {
        int unread = 0;
        Gson gson = new Gson();
        String json = preferences.getString(uid, null);
        if (json != null) {
            Map map = gson.fromJson(json, Map.class);
            int stock = (int) (double)map.get("message_stock");
            unread = stock;
        }
        if (unread > 0) {
            notificationText.setVisibility(View.VISIBLE);
            notificationText.setText(String.valueOf(unread));
        } else {
            notificationText.setVisibility(View.GONE);
        }
    }

    private void blockUser(String blockUser) {
        DatabaseReference matchRef = FirebaseDatabase.getInstance().getReference().child("Match");
//        matchRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshots) {
//                for (DataSnapshot snapshot : snapshots.getChildren())  {
//                    if ((snapshot.child("user1").getValue().equals(mAuth.getCurrentUser().getUid()) && snapshot.child("user2").getValue().equals(blockUser)) ||
//                            ((snapshot.child("user1").getValue().equals(blockUser) && snapshot.child("user2").getValue().equals(mAuth.getCurrentUser().getUid()))))
//                    {
//                        if (snapshot.getKey() != null) {
//                            matchRef.child(snapshot.getKey()).child("block").setValue(true);
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        matchRef.child(myId).child(blockUser).child("block").setValue(true);
        matchRef.child(blockUser).child(myId).child("block").setValue(true);
        Map<String, Object> map = new HashMap<>();
        map.put("blocked_user_id", blockUser);
        map.put("block_user_id", myId);
        map.put("isBlock", true);
        map.put("time_stamp", System.currentTimeMillis());
        database.getReference("Block").push().setValue(map);
        database.getReference("Chats").child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshots) {
                for (DataSnapshot snapshot : snapshots.getChildren())  {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat != null) {
                        if (chat.getFrom().equals(blockUser) ||
                                chat.getTo().equals(blockUser)) {
                            snapshot.getRef().removeValue();
                        }
                    } else {
                        System.out.println("chatはnullです");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        database.getReference("Chats").child(blockUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshots) {
                for (DataSnapshot snapshot : snapshots.getChildren())  {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat != null) {
                        if (chat.getFrom().equals(myId) ||
                                chat.getTo().equals(myId)) {
                            snapshot.getRef().removeValue();
                        }
                    } else {
                        System.out.println("chatはnullです");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMessage(String uid, TextView mSentDate, TextView mNewestMessage) {
        theLastMessage = "default";
        theSentDate = "";
        Gson gson = new Gson();
        String json = preferences.getString(uid, null);
        if (json != null) {
            Map map = gson.fromJson(json, Map.class);
            theLastMessage = (String) map.get("last_message");
            theSentDate = (String) map.get("time_stamp");
        } else {
            System.out.println("null");
        }

        switch (theLastMessage) {
            case "default":
                mSentDate.setText("");
                mNewestMessage.setText(mContext.getString(R.string.no_message));
                break;
            default:
                mSentDate.setText(theSentDate);
                mNewestMessage.setText(theLastMessage);
                break;
        }

        theLastMessage = "default";
        theSentDate = "";
    }

    @Override
    public int getItemCount() {
        return mChatList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onMessageSent(DataSnapshot snapshot) {

    }

    public class TextViewHolder extends RecyclerView.ViewHolder {
        TextView plainText;
        public TextViewHolder(@NotNull View itemView) {
            super(itemView);
            plainText = itemView.findViewById(R.id.plain_text);
        }
    }

    public class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView mMatchText;
        TextView mChatText;
        RecyclerView mMatchRecyclerView;
        MatchOnlyRecyclerAdapter mMatchRecyclerAdapter;
        TextView mPlainText;
        Switch mSwitch;
        View view;
        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            mMatchText = view.findViewById(R.id.match_text);
            mChatText = view.findViewById(R.id.chat_text);
            mMatchRecyclerView = view.findViewById(R.id.match_recycler);
            mMatchRecyclerAdapter = new MatchOnlyRecyclerAdapter(mContext, mMatchList);
            mPlainText = view.findViewById(R.id.plain_text);
            mSwitch = view.findViewById(R.id.switch_notify);
        }
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView mName;
        TextView mNewestMessage;
        TextView mUserAge;
        TextView mAddress;
        TextView mSentDate;
        CircleImageView mImg;
        CircleImageView mStatus;
        TextView mNotificationText;
        View view;
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            mName = itemView.findViewById(R.id.match_name);
            mNewestMessage = itemView.findViewById(R.id.newest_message);
            mUserAge = itemView.findViewById(R.id.match_user_age);
            mImg = itemView.findViewById(R.id.match_img);
            mAddress = itemView.findViewById(R.id.match_user_address);
            mSentDate = itemView.findViewById(R.id.sent_date);
            mStatus = itemView.findViewById(R.id.online);
            mNotificationText = itemView.findViewById(R.id.notification_text_match);
        }
    }
}
