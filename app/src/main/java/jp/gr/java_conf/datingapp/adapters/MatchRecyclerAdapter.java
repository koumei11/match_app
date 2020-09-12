package jp.gr.java_conf.datingapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.gr.java_conf.datingapp.ChatActivity;
import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.models.Chat;
import jp.gr.java_conf.datingapp.models.Match;
import jp.gr.java_conf.datingapp.models.Profile;
import jp.gr.java_conf.datingapp.models.UserState;
import jp.gr.java_conf.datingapp.utility.AgeCalculation;
import jp.gr.java_conf.datingapp.utility.DateTimeConverter;

public class MatchRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Match> mChatList;
    private List<Match> mMatchList;
    private final FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final String myId = mAuth.getCurrentUser().getUid();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref;
    private DatabaseReference blockRef = database.getReference();
    private String theLastMessage;
    private String theSentDate;

    public MatchRecyclerAdapter(Context context, List<Match> chatList, List<Match> matchList) {
        mContext = context;
        mChatList = chatList;
        mMatchList = matchList;
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
            ChatViewHolder chatViewHolder = (ChatViewHolder) holder;
            ref = database.getReference("/status/" + mChatList.get(position - 1).getUser_id());
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserState userState = snapshot.getValue(UserState.class);
                    if (userState != null) {
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

            showMessage(mChatList.get(position - 1).getUser_id(), chatViewHolder.mSentDate, chatViewHolder.mNewestMessage);
            showNumberOfUnreadMessage(mChatList.get(position - 1).getUser_id(), chatViewHolder.mNotificationText);
            mStore.collection("Users").document(mChatList.get(position - 1).getUser_id()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful() && task.getResult() != null) {
                        Profile profile = task.getResult().toObject(Profile.class);
                        chatViewHolder.mName.setText(mChatList.get(position - 1).getName());
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
                    mStore.collection("Users").document(mChatList.get(position - 1).getUser_id()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                Profile profile = task.getResult().toObject(Profile.class);
                                intent.putExtra("profile", profile);
                                intent.putExtra("doc_id", mChatList.get(position - 1).getUser_id());
                                intent.putExtra("user_img", profile.getImg_url());
                                intent.putExtra("user_name", mChatList.get(position - 1).getName());
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
                            blockUser(mChatList.get(position - 1).getUser_id());
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
            if (mMatchList.size() != 0) {
                matchViewHolder.mMatchRecyclerView.setVisibility(View.VISIBLE);
                matchViewHolder.mPlainText.setVisibility(View.GONE);
                matchViewHolder.mMatchRecyclerView.setHasFixedSize(true);
                matchViewHolder.mMatchRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
                matchViewHolder.mMatchRecyclerView.setAdapter(matchViewHolder.mMatchRecyclerAdapter);
            } else {
                matchViewHolder.mMatchRecyclerView.setVisibility(View.GONE);
                matchViewHolder.mPlainText.setVisibility(View.VISIBLE);
            }

            if (mChatList.size() == 0) {
                matchViewHolder.mChatText.setText("");
            } else {
                matchViewHolder.mChatText.setText(mContext.getString(R.string.during_chat));
            }

        }
    }

    private void showNumberOfUnreadMessage(String uid, TextView notificationText) {
        DatabaseReference ref = database.getReference("Chats");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int unread = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    chat.setSeen((boolean)snapshot.child("isSeen").getValue());
                    if (chat.getTo().equals(myId) && !chat.isSeen() && chat.getFrom().equals(uid)) {
                        unread++;
                    }
                }

                if (unread > 0) {
                    notificationText.setVisibility(View.VISIBLE);
                    notificationText.setText(String.valueOf(unread));
                } else {
                    notificationText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void blockUser(String uid) {
        Map<String, Object> map = new HashMap<>();
        map.put("blocked_user_id", uid);
        map.put("block_user_id", myId);
        map.put("isBlock", true);
        blockRef.child("Block").push().setValue(map);
    }

    private void showMessage(String uid, TextView mSentDate, TextView mNewestMessage) {
        theLastMessage = "default";
        theSentDate = "";
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if ((chat.getFrom().equals(myId) || chat.getFrom().equals(uid)) &&
                            (chat.getTo().equals(myId) || chat.getTo().equals(uid))) {
                        if (chat.getMessage() == null && chat.getImg_uri() != null) {
                            theLastMessage = mContext.getString(R.string.send_image);
                        } else {
                            theLastMessage = chat.getMessage();
                        }

                        theSentDate = DateTimeConverter.getSentTime(chat.getTime_stamp());
                    }
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
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mChatList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
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
        View view;
        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            mMatchText = view.findViewById(R.id.match_text);
            mChatText = view.findViewById(R.id.chat_text);
            mMatchRecyclerView = view.findViewById(R.id.match_recycler);
            mMatchRecyclerAdapter = new MatchOnlyRecyclerAdapter(mContext, mMatchList);
            mPlainText = view.findViewById(R.id.plain_text);
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
