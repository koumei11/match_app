package jp.gr.java_conf.datingapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
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
import jp.gr.java_conf.datingapp.model.UserState;
import jp.gr.java_conf.datingapp.utility.AgeCalculation;
import jp.gr.java_conf.datingapp.utility.DateTimeConverter;

public class MatchRecyclerAdapter extends RecyclerView.Adapter<MatchRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private List<Match> mMatchList;
    private final FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref;
    private String theLastMessage;
    private String theSentDate;

    public MatchRecyclerAdapter(Context context, List<Match> matchList) {
        mContext = context;
        mMatchList = matchList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.single_match_user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        ref = database.getReference("/status/" + mMatchList.get(position).getUser_id());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserState userState = snapshot.getValue(UserState.class);
                if (userState != null) {
                    Date now = new Date();
                    long timePassed = (now.getTime() - userState.getLast_changed()) / 1000 / 60 / 60;
                    if (userState.getState().equals("online")) {
                        holder.mStatus.setBorderColor(mContext.getResources().getColor(R.color.colorGreen));
                    } else if (timePassed < 24){
                        holder.mStatus.setBorderColor(mContext.getResources().getColor(R.color.colorYellow));
                    } else {
                        holder.mStatus.setBorderColor(mContext.getResources().getColor(R.color.colorLightGrey));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        showMessage(mMatchList.get(position).getUser_id(), holder.mSentDate, holder.mNewestMessage);

        mStore.collection("Users").document(mMatchList.get(position).getUser_id()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful() && task.getResult() != null) {
                    Profile profile = task.getResult().toObject(Profile.class);
                    holder.mName.setText(mMatchList.get(position).getName());
                    holder.mAddress.setText(profile.getAddress());
                    if (mMatchList.get(position).getImg_url() != null) {
                        Glide.with(mContext).load(mMatchList.get(position).getImg_url()).into(holder.mImg);
                    } else {
                        holder.mImg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.avatornew));
                    }
                    try {
                        int age = AgeCalculation.calculate(profile.getDate());
                        holder.mUserAge.setText(age + "歳");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                mStore.collection("Users").document(mMatchList.get(position).getUser_id()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Profile profile = task.getResult().toObject(Profile.class);
                            intent.putExtra("profile", profile);
                            intent.putExtra("doc_id", mMatchList.get(position).getUser_id());
                            intent.putExtra("user_img", mMatchList.get(position).getImg_url());
                            intent.putExtra("user_name", mMatchList.get(position).getName());
                            mContext.startActivity(intent);
                        }
                    }
                });
            }
        });

    }

    private void showMessage(String uid, TextView mSentDate, TextView mNewestMessage) {
        theLastMessage = "default";
        theSentDate = "";
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        System.out.println("HElooo");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if ((chat.getFrom().equals(firebaseUser.getUid()) || chat.getFrom().equals(uid)) &&
                            (chat.getTo().equals(firebaseUser.getUid()) || chat.getTo().equals(uid))) {
                        theLastMessage = chat.getMessage();
                        theSentDate = DateTimeConverter.getSentTime(chat.getTime_stamp());
                    }
                }

                switch (theLastMessage) {
                    case "default":
                        mSentDate.setText("");
                        mNewestMessage.setText("メッセージがありません。");
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
        return mMatchList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mName;
        TextView mNewestMessage;
        TextView mUserAge;
        TextView mAddress;
        TextView mSentDate;
        CircleImageView mImg;
        CircleImageView mStatus;
        View view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            mName = itemView.findViewById(R.id.match_name);
            mNewestMessage = itemView.findViewById(R.id.newest_message);
            mUserAge = itemView.findViewById(R.id.match_user_age);
            mImg = itemView.findViewById(R.id.match_img);
            mAddress = itemView.findViewById(R.id.match_user_address);
            mSentDate = itemView.findViewById(R.id.sent_date);
            mStatus = itemView.findViewById(R.id.online);
        }
    }
}
