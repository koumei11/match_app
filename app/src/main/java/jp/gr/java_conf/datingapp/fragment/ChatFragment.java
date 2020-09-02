package jp.gr.java_conf.datingapp.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.adapter.MatchRecyclerAdapter;
import jp.gr.java_conf.datingapp.model.Match;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private RecyclerView mMatchRecyclerView;
    private List<Match> mMatchList;
    private MatchRecyclerAdapter mMatchRecyclerAdapter;
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
        mMatchList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mMatchRecyclerView.setHasFixedSize(true);
        mMatchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mMatchRecyclerAdapter = new MatchRecyclerAdapter(getContext(), mMatchList);
        mMatchRecyclerView.setAdapter(mMatchRecyclerAdapter);

        messageView = LayoutInflater.from(getContext()).inflate(R.layout.single_match_user_item, null);
        newMessage = messageView.findViewById(R.id.newest_message);

        mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                .collection("Match").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                        Match match = documentSnapshot.toObject(Match.class);
                        mMatchList.add(match);
                        mMatchRecyclerAdapter.notifyDataSetChanged();
                    }
                }
             }
        });

        final Handler refreshHandler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // do updates for imageview
                refreshHandler.postDelayed(this, 10 * 1000);
                mMatchRecyclerAdapter.notifyDataSetChanged();
            }
        };
        refreshHandler.postDelayed(runnable, 10 * 1000);
        return view;
    }
}
