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
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.gr.java_conf.datingapp.ChatActivity;
import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.model.Chat;
import jp.gr.java_conf.datingapp.model.Match;
import jp.gr.java_conf.datingapp.model.Profile;
import jp.gr.java_conf.datingapp.utility.AgeCalculation;
import jp.gr.java_conf.datingapp.utility.DateTimeConverter;

public class MatchRecyclerAdapter extends RecyclerView.Adapter<MatchRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private List<Match> mMatchList;
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

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
        mStore.collection("Message").orderBy("time_stamp", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                mStore.collection("Users").document(mMatchList.get(position).getUser_id()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Profile profile = task.getResult().toObject(Profile.class);
                            holder.mName.setText(mMatchList.get(position).getName());
                            holder.mAddress.setText(profile.getAddress());
                            Glide.with(mContext).load(mMatchList.get(position).getImg_url()).into(holder.mImg);
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

                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                    DocumentSnapshot snapshot = doc.getDocument();
                    Chat chat = snapshot.toObject(Chat.class);
                    if ((chat.getFrom().equals(mAuth.getCurrentUser().getUid()) || chat.getFrom().equals(mMatchList.get(position).getUser_id())) &&
                            (chat.getTo().equals(mAuth.getCurrentUser().getUid()) || chat.getTo().equals(mMatchList.get(position).getUser_id()))) {
                        String sentDate = DateTimeConverter.getSentTime(chat.getTime_stamp());
                        holder.mSentDate.setText(sentDate);
                        holder.mNewestMessage.setText(chat.getMessage());
                        break;
                    }
                }
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
        }
    }
}
