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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.gr.java_conf.datingapp.ChatActivity;
import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.models.Match;
import jp.gr.java_conf.datingapp.models.Profile;

public class MatchOnlyRecyclerAdapter extends RecyclerView.Adapter<MatchOnlyRecyclerAdapter.ViewHolder> {
    private List<Match> mMatchList;
    private Context mContext;
    private final FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
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
        if (mMatchList.get(position).getImg_url() != null) {
            Glide.with(mContext).load(mMatchList.get(position).getImg_url()).into(holder.circleImageView);
        } else {
            holder.circleImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.avatornew));
        }
        holder.userName.setText(mMatchList.get(position).getName());

        Date now = new Date();
        if (mMatchList.get(position).getTime_stamp() + 1000 * 60 * 60 * 24 * 3 < now.getTime()) {
            holder.newText.setVisibility(View.INVISIBLE);
        } else {
            holder.newText.setVisibility(View.VISIBLE);
        }
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
        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.block);
                builder.setMessage(R.string.alert_block);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.out.println("はい");
                        blockUser(mMatchList.get(position).getUser_id());
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

    private void blockUser(String uid) {

        mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                .collection("Match").document(uid)
                .update("isBlock", true).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mStore.collection("Users").document(uid)
                        .collection("Match").document(mAuth.getCurrentUser().getUid())
                        .update("isBlock", true).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("blocked_user_id", uid);
                        map.put("block_user_id", mAuth.getCurrentUser().getUid());
                        map.put("isBlock", true);

                        blockRef.child("Block").push().setValue(map);
                    }
                });
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
