package jp.gr.java_conf.datingapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.gr.java_conf.datingapp.ChatActivity;
import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.model.Chat;
import jp.gr.java_conf.datingapp.model.Match;
import jp.gr.java_conf.datingapp.model.Profile;

public class MatchOnlyRecyclerAdapter extends RecyclerView.Adapter<MatchOnlyRecyclerAdapter.ViewHolder> {
    private List<Match> mMatchList;
    private Context mContext;
    private final FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final String myId = mAuth.getCurrentUser().getUid();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference blockRef = database.getReference();

    public MatchOnlyRecyclerAdapter(Context context, List<Match> matchList) {
        mContext = context;
        mMatchList = matchList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.single_match_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String userId;
        if (mMatchList.get(position).getUser1().equals(myId)) {
            userId = mMatchList.get(position).getUser2();
        } else {
            userId = mMatchList.get(position).getUser1();
        }
        mStore.collection("Users").document(userId)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Profile profile = documentSnapshot.toObject(Profile.class);

                if (profile != null) {
                    if (profile.getImg_url() != null) {
                        Glide.with(mContext).load(profile.getImg_url()).into(holder.circleImageView);
                    } else {
                        holder.circleImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.avatornew));
                    }
                    holder.userName.setText(profile.getName());

                    Date now = new Date();
                    if (mMatchList.get(position).getTime_stamp() + 1000 * 60 * 60 * 24 < now.getTime()) {
                        holder.newText.setVisibility(View.INVISIBLE);
                    } else {
                        holder.newText.setVisibility(View.VISIBLE);
                    }
                    holder.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(mContext, ChatActivity.class);
                            intent.putExtra("profile", profile);
                            intent.putExtra("doc_id", userId);
                            intent.putExtra("user_img", profile.getImg_url());
                            intent.putExtra("user_name", profile.getName());
                            mContext.startActivity(intent);
                        }
                    });
                    holder.view.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setTitle(R.string.block);
                            builder.setMessage(R.string.alert_block);
                            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
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
                }
            }
        });
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
//                            matchRef.child().child("block").setValue(true);
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
                                (chat.getTo().equals(blockUser))) {
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
                                (chat.getTo().equals(myId))) {
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

    @Override
    public int getItemCount() {
        return mMatchList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        TextView newText;
        TextView userName;
        View view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            circleImageView = view.findViewById(R.id.match_circle_image);
            userName = view.findViewById(R.id.match_user_name);
            newText = view.findViewById(R.id.new_text);
        }
    }
}
