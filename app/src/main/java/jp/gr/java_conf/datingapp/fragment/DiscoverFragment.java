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
import jp.gr.java_conf.datingapp.model.Profile;
import jp.gr.java_conf.datingapp.model.SwipeCard;
import jp.gr.java_conf.datingapp.utility.MatchHandler;
import jp.gr.java_conf.datingapp.utility.SelectedListener;
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
    private SelectedListener mSelectedListener;
    private ProgressBar progressBar;
    private ImageButton mRejectButton;
    private ImageButton mAcceptButton;
    private static boolean rejectFlg;
    private static boolean acceptFlg;

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

        int bottomMargin = WindowSizeGetter.dpToPx(200);
        Point windowSize = WindowSizeGetter.getDisplaySize(getActivity().getWindowManager());

        mSwipeView.getBuilder()
                .setSwipeDecor(new SwipeDecor()
                        .setViewWidth(windowSize.x - 150)
                        .setViewHeight(windowSize.y - bottomMargin)
                        .setViewGravity(Gravity.CENTER_HORIZONTAL)
                        .setRelativeScale(0.01f)
                        .setMarginTop(30)
                        .setSwipeInMsgLayoutId(R.layout.swipe_in_msg_view)
                        .setSwipeOutMsgLayoutId(R.layout.swipe_out_msg_view));

        mStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mProfileList = new ArrayList<>();

        mStore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                        String docId = documentSnapshot.getId();
                        if (!docId.equals(mAuth.getCurrentUser().getUid())) {
                            Profile profile = documentSnapshot.toObject(Profile.class).withId(docId);
                            mProfileList.add(profile);
                        }
                    }

                    Collections.shuffle(mProfileList);

                    for(Profile profile : mProfileList){
                        mSwipeView.addView(new SwipeCard(mContext, profile, mSwipeView, mSelectedListener));
                    }
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                }
            }
        });

        mRejectButton = view.findViewById(R.id.rejectBtn);
        mAcceptButton = view.findViewById(R.id.acceptBtn);

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
            public void setSwipedDocumentId(final String docId, final String name) {

                mStore.collection("Users").document(docId).collection("Likes")
                        .document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && task.getResult().getData() != null) {
                                mStore.collection("Users").document(docId).collection("Likes")
                                        .document(mAuth.getCurrentUser().getUid()).delete();
                                Toast.makeText(getContext(), getString(R.string.match_found), Toast.LENGTH_SHORT).show();
                                MatchHandler.storeMatchInDatabase(docId, name, getContext());
                            } else {
                                Map<String, Object> map = new HashMap<>();
                                map.put("like", true);
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
                    }
                });

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
