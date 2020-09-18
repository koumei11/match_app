package jp.gr.java_conf.datingapp.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
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

import java.text.ParseException;

import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.UserDetailActivity;
import jp.gr.java_conf.datingapp.listener.SelectedListener;
import jp.gr.java_conf.datingapp.utility.AgeCalculation;

@Layout(R.layout.swipe_card_view)
public class SwipeCard {

    private static final int REQUEST_CODE = 101;

    @View(R.id.profileImageView)
    private ImageView profileImageView;

    @View(R.id.nameAgeTxt)
    private TextView nameAgeTxt;

    @View(R.id.locationNameTxt)
    private TextView locationNameTxt;

    @View(R.id.ageTxt)
    private TextView ageTxt;

    @View(R.id.constraint_prof)
    private ConstraintLayout constraintLayout;

    @View(R.id.image_indicator)
    private FrameLayout imageIndicator;

    private Profile mProfile;
    private Context mContext;
    private SwipePlaceHolderView mSwipeView;
    private SelectedListener mSelectedListener;
    private String mUserId;

    public SwipeCard(Context context, Profile profile, SwipePlaceHolderView swipeView, SelectedListener selectedListener, String userId) {
        mContext = context;
        mProfile = profile;
        mSwipeView = swipeView;
        mSelectedListener = selectedListener;
        mUserId = userId;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Click(R.id.constraint_prof)
    private void viewDetail() {
        constraintLayout.setEnabled(false);
        Intent intent = new Intent(mContext, UserDetailActivity.class);
        intent.putExtra("profile", mProfile);
        intent.putExtra("user_id", mUserId);
        ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, profileImageView, "trans1");
        ((Activity) mContext).startActivityForResult(intent, REQUEST_CODE, compat.toBundle());
        constraintLayout.setEnabled(true);
    }

    @Resolve
    private void onResolved() throws ParseException {

        if (mProfile.getImg_url() == null || mProfile.getImg_url().equals("")) {
            profileImageView.setImageResource(R.drawable.avatornew);
        } else {
            Glide.with(mContext).load(mProfile.getImg_url()).into(profileImageView);
        }

        nameAgeTxt.setText(mProfile.getName());
        locationNameTxt.setText(mProfile.getAddress());
        int age = AgeCalculation.calculate(mProfile.getDate());
        String ageStr = age + "歳";
        ageTxt.setText(ageStr);

        int totalImage = 0;
        if (mProfile.getImg_url() != null && !mProfile.getImg_url().equals("")) {
            totalImage += 1;
        }

        if (mProfile.getImg_url2() != null && !mProfile.getImg_url2().equals("")) {
            totalImage += 1;
        }

        if (mProfile.getImg_url3() != null && !mProfile.getImg_url3().equals("")) {
            totalImage += 1;
        }

        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (totalImage == 3) {
            android.view.View viewIndicator = inflater.inflate(R.layout.three_indicator, null);
            LinearLayout linearLayout = viewIndicator.findViewById(R.id.indicator_frame3);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            imageIndicator.addView(linearLayout);
            System.out.println("3枚");
        } else if (totalImage == 2) {
            android.view.View viewIndicator = inflater.inflate(R.layout.two_indicator, null);
            LinearLayout linearLayout = viewIndicator.findViewById(R.id.indicator_frame2);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            imageIndicator.addView(linearLayout);
            System.out.println("2枚");
        } else {
            android.view.View viewIndicator = inflater.inflate(R.layout.one_indicator, null);
            LinearLayout linearLayout = viewIndicator.findViewById(R.id.indicator_frame1);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            imageIndicator.addView(linearLayout);
            System.out.println("1枚");
        }

        Position currentPos = new Position(0);
        profileImageView.setOnTouchListener(new android.view.View.OnTouchListener() {
            @Override
            public boolean onTouch(android.view.View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    int totalImage = 0;

                    if (mProfile.getImg_url() != null && !mProfile.getImg_url().equals("")) {
                        totalImage += 1;
                    }

                    if (mProfile.getImg_url2() != null && !mProfile.getImg_url2().equals("")) {
                        totalImage += 1;
                    }

                    if (mProfile.getImg_url3() != null && !mProfile.getImg_url3().equals("")) {
                        totalImage += 1;
                    }

                    if (motionEvent.getX() >= view.getWidth() / 2) {
                        if (totalImage == 2) {
                            if (currentPos.getPos() == 0) {
                                imageIndicator.getChildAt(0)
                                        .findViewById(R.id.indicator1)
                                        .setBackground(mContext.getResources().getDrawable(R.drawable.textview_light_rounded));

                                imageIndicator.getChildAt(0)
                                        .findViewById(R.id.indicator2)
                                        .setBackground(mContext.getResources().getDrawable(R.drawable.textview_rounded));

                                Glide.with(mContext).load(mProfile.getImg_url2()).into(profileImageView);
                                currentPos.setPos(currentPos.getPos() + 1);
                            }
                        } else if (totalImage == 3) {
                            if (currentPos.getPos() == 0) {
                                imageIndicator.getChildAt(0)
                                        .findViewById(R.id.indicator1)
                                        .setBackground(mContext.getResources().getDrawable(R.drawable.textview_light_rounded));

                                imageIndicator.getChildAt(0)
                                        .findViewById(R.id.indicator2)
                                        .setBackground(mContext.getResources().getDrawable(R.drawable.textview_rounded));

                                imageIndicator.getChildAt(0)
                                        .findViewById(R.id.indicator3)
                                        .setBackground(mContext.getResources().getDrawable(R.drawable.textview_light_rounded));
                                Glide.with(mContext).load(mProfile.getImg_url2()).into(profileImageView);
                                currentPos.setPos(currentPos.getPos() + 1);
                                System.out.println("２枚目");
                            } else if (currentPos.getPos() == 1) {
                                imageIndicator.getChildAt(0)
                                        .findViewById(R.id.indicator1)
                                        .setBackground(mContext.getResources().getDrawable(R.drawable.textview_light_rounded));

                                imageIndicator.getChildAt(0)
                                        .findViewById(R.id.indicator2)
                                        .setBackground(mContext.getResources().getDrawable(R.drawable.textview_light_rounded));

                                imageIndicator.getChildAt(0)
                                        .findViewById(R.id.indicator3)
                                        .setBackground(mContext.getResources().getDrawable(R.drawable.textview_rounded));
                                Glide.with(mContext).load(mProfile.getImg_url3()).into(profileImageView);
                                currentPos.setPos(currentPos.getPos() + 1);
                                System.out.println("３枚目");
                            }
                        }
                        mProfile.setPos(currentPos.getPos());
                    } else if (motionEvent.getX() < view.getWidth() / 2) {
                        if (totalImage == 2) {
                            if (currentPos.getPos() == 1) {

                                imageIndicator.getChildAt(0)
                                        .findViewById(R.id.indicator1)
                                        .setBackground(mContext.getResources().getDrawable(R.drawable.textview_rounded));

                                imageIndicator.getChildAt(0)
                                        .findViewById(R.id.indicator2)
                                        .setBackground(mContext.getResources().getDrawable(R.drawable.textview_light_rounded));

                                Glide.with(mContext).load(mProfile.getImg_url()).into(profileImageView);
                                currentPos.setPos(currentPos.getPos() - 1);
                            }
                        } else if (totalImage == 3) {
                            if (currentPos.getPos() == 1) {

                                imageIndicator.getChildAt(0)
                                        .findViewById(R.id.indicator1)
                                        .setBackground(mContext.getResources().getDrawable(R.drawable.textview_rounded));

                                imageIndicator.getChildAt(0)
                                        .findViewById(R.id.indicator2)
                                        .setBackground(mContext.getResources().getDrawable(R.drawable.textview_light_rounded));

                                imageIndicator.getChildAt(0)
                                        .findViewById(R.id.indicator3)
                                        .setBackground(mContext.getResources().getDrawable(R.drawable.textview_light_rounded));

                                Glide.with(mContext).load(mProfile.getImg_url()).into(profileImageView);
                                currentPos.setPos(currentPos.getPos() - 1);
                            } else if (currentPos.getPos() == 2) {
                                imageIndicator.getChildAt(0)
                                        .findViewById(R.id.indicator1)
                                        .setBackground(mContext.getResources().getDrawable(R.drawable.textview_light_rounded));

                                imageIndicator.getChildAt(0)
                                        .findViewById(R.id.indicator2)
                                        .setBackground(mContext.getResources().getDrawable(R.drawable.textview_rounded));

                                imageIndicator.getChildAt(0)
                                        .findViewById(R.id.indicator3)
                                        .setBackground(mContext.getResources().getDrawable(R.drawable.textview_light_rounded));

                                Glide.with(mContext).load(mProfile.getImg_url2()).into(profileImageView);
                                currentPos.setPos(currentPos.getPos() - 1);
                            }
                        }
                        mProfile.setPos(currentPos.getPos());
                    }
                }
                return true;
            }
        });
    }

    @SwipeOut
    private void onSwipedOut(){
        mSelectedListener.setSwipedDocumentId(mProfile.profileId, mProfile.getName(), false);
        Log.d("EVENT", "onSwipedOut");
    }

    @SwipeCancelState
    private void onSwipeCancelState() {
        Log.d("EVENT", "onSwipeCancelState");
    }

    @SwipeIn
    private void onSwipeIn(){
        mSelectedListener.setSwipedDocumentId(mProfile.profileId, mProfile.getName(), true);
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
