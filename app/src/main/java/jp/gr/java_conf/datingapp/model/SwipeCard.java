package jp.gr.java_conf.datingapp.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityOptionsCompat;

import com.bumptech.glide.Glide;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.mindorks.placeholderview.annotations.swipe.SwipeCancelState;
import com.mindorks.placeholderview.annotations.swipe.SwipeIn;
import com.mindorks.placeholderview.annotations.swipe.SwipeInState;
import com.mindorks.placeholderview.annotations.swipe.SwipeOut;
import com.mindorks.placeholderview.annotations.swipe.SwipeOutState;

import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.UserDetailActivity;
import jp.gr.java_conf.datingapp.utility.SelectedListener;

@Layout(R.layout.swipe_card_view)
public class SwipeCard {

    private static final int REQUEST_CODE = 101;

    @View(R.id.profileImageView)
    private ImageView profileImageView;

    @View(R.id.nameAgeTxt)
    private TextView nameAgeTxt;

    @View(R.id.locationNameTxt)
    private TextView locationNameTxt;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Click(R.id.constraint_prof)
    private void viewDetail() {
        Intent intent = new Intent(mContext, UserDetailActivity.class);
        intent.putExtra("profile", mProfile);
        ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, profileImageView, "trans1");
        ((Activity) mContext).startActivityForResult(intent, REQUEST_CODE, compat.toBundle());
    }

    private Profile mProfile;
    private Context mContext;
    private SwipePlaceHolderView mSwipeView;
    private SelectedListener mSelectedListener;

    public SwipeCard(Context context, Profile profile, SwipePlaceHolderView swipeView, SelectedListener selectedListener) {
        mContext = context;
        mProfile = profile;
        mSwipeView = swipeView;
        mSelectedListener = selectedListener;
    }

    @Resolve
    private void onResolved(){
        if (mProfile.getImg_url() == null || mProfile.getImg_url().equals("")) {
            profileImageView.setImageResource(R.drawable.avatornew);
        } else {
            Glide.with(mContext).load(mProfile.getImg_url()).into(profileImageView);
        }

        nameAgeTxt.setText(mProfile.getName());
        locationNameTxt.setText(mProfile.getAddress());
    }

    @SwipeOut
    private void onSwipedOut(){
        Log.d("EVENT", "onSwipedOut");
    }

    @SwipeCancelState
    private void onSwipeCancelState(){
        Log.d("EVENT", "onSwipeCancelState");
    }

    @SwipeIn
    private void onSwipeIn(){
        mSelectedListener.setSwipedDocumentId(mProfile.profileId, mProfile.getName());
        Log.d("EVENT", "onSwipedIn");
    }

    @SwipeInState
    private void onSwipeInState(){
        Log.d("EVENT", "onSwipeInState");
    }

    @SwipeOutState
    private void onSwipeOutState(){
        Log.d("EVENT", "onSwipeOutState");
    }

}
