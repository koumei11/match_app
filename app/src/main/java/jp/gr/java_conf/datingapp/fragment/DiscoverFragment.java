package jp.gr.java_conf.datingapp.fragment;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mindorks.placeholderview.SwipeDecor;
import com.mindorks.placeholderview.SwipePlaceHolderView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.dialog.MatchDialog;
import jp.gr.java_conf.datingapp.model.Profile;
import jp.gr.java_conf.datingapp.model.SwipeCard;
import jp.gr.java_conf.datingapp.utility.MatchHandler;
import jp.gr.java_conf.datingapp.listener.SelectedListener;
import jp.gr.java_conf.datingapp.utility.WindowSizeGetter;

/**
 * A simple {@link Fragment} subclass.
 */
public class DiscoverFragment extends Fragment {

    private SwipePlaceHolderView mSwipeView;
    private Context mContext;
    private FirebaseFirestore mStore;
    private FirebaseAuth mAuth;
    private List<Profile> mProfileList;
    private TextView mNoMember;
    private SelectedListener mSelectedListener;
    private ProgressBar progressBar;
    private ImageButton mRejectButton;
    private ImageButton mAcceptButton;
    private static boolean rejectFlg;
    private static boolean acceptFlg;
    private int totalMember = 0;

    public DiscoverFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_discover, container, false);
        mSwipeView = view.findViewById(R.id.swipeView);
        progressBar = view.findViewById(R.id.progressbar1);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        mContext = getContext();
        mRejectButton = view.findViewById(R.id.rejectBtn);
        mAcceptButton = view.findViewById(R.id.acceptBtn);
        mNoMember = view.findViewById(R.id.no_member);
        mNoMember.setVisibility(View.GONE);

        int bottomMargin = WindowSizeGetter.dpToPx(200);
        int marginTop = WindowSizeGetter.dpToPx(40);
        Point windowSize = WindowSizeGetter.getDisplaySize(getActivity().getWindowManager());

        mSwipeView.getBuilder()
                .setSwipeDecor(new SwipeDecor()
                        .setViewWidth(windowSize.x - 150)
                        .setViewHeight(windowSize.y - bottomMargin)
                        .setViewGravity(Gravity.CENTER_HORIZONTAL)
                        .setRelativeScale(0.01f)
                        .setMarginTop(marginTop)
                        .setSwipeInMsgLayoutId(R.layout.swipe_in_msg_view)
                        .setSwipeOutMsgLayoutId(R.layout.swipe_out_msg_view));

        mStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mProfileList = new ArrayList<>();

        mStore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> taskUsers) {
                if (taskUsers.isSuccessful()) {
                    mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                            .collection("Likes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> taskLikes) {
                            if (taskLikes.isSuccessful()) {
                                mStore.collection("Users").document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            List<String> userIdList = new ArrayList<>();
                                            String mySex = (String) task.getResult().get("sex");
                                            for (DocumentSnapshot documentSnapshot : taskLikes.getResult()) {
                                                userIdList.add((String) documentSnapshot.get("user_id"));
                                            }
                                            for (DocumentSnapshot documentSnapshot : taskUsers.getResult()) {
                                                String docId = documentSnapshot.getId();
                                                String sex = (String) documentSnapshot.get("sex");
                                                if (!docId.equals(mAuth.getCurrentUser().getUid())
                                                        && !sex.equals(mySex)
                                                        && !userIdList.contains(docId)
                                                        && (boolean) documentSnapshot.get("account_flg")) {
                                                    Profile profile = documentSnapshot.toObject(Profile.class).withId(docId);
                                                    mProfileList.add(profile);
                                                }
                                            }
                                            totalMember  = mProfileList.size();
                                            Collections.shuffle(mProfileList);

                                            for (Profile profile : mProfileList) {
                                                mSwipeView.addView(new SwipeCard(mContext, profile, mSwipeView, mSelectedListener, profile.getUser_id()));
                                            }
                                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                                            if (totalMember == 0) {
                                                mNoMember.setVisibility(View.VISIBLE);
                                                mAcceptButton.setVisibility(View.GONE);
                                                mRejectButton.setVisibility(View.GONE);
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

        mRejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeView.doSwipe(false);
            }
        });

        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeView.doSwipe(true);
            }
        });

        mSelectedListener = new SelectedListener() {
            @Override
            public void setSwipedDocumentId(final String docId, final String name, final boolean isLike) {
                if (isLike) {
                    totalMember--;
                    mStore.collection("Users").document(docId).collection("Likes")
                            .document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null && task.getResult().getData() != null) {
                                    MatchHandler.storeMatchInDatabase(docId, name, getContext());
                                }
                                Map<String, Object> map = new HashMap<>();
                                map.put("like", true);
                                map.put("user_id", docId);
                                mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                                        .collection("Likes")
                                        .document(docId).set(map)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                }
                                            }
                                        });
                            }
                        }
                    });
                } else {
                    totalMember--;
                    Map<String, Object> map = new HashMap<>();
                    map.put("user_id", docId);
                    map.put("like", false);
                    mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                            .collection("Likes")
                            .document(docId).set(map)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                    }
                                }
                            });
                }

                if (totalMember <= 0) {
                    mNoMember.setVisibility(View.VISIBLE);
                    mRejectButton.setVisibility(View.GONE);
                    mAcceptButton.setVisibility(View.GONE);
                }
            }
        };

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (rejectFlg) {
            mRejectButton.performClick();
            rejectFlg = false;
        }

        if (acceptFlg) {
            mAcceptButton.performClick();
            acceptFlg = false;
        }
    }

    public static void reject() {
        rejectFlg = true;
    }

    public static void accept() {
        acceptFlg = true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

}
