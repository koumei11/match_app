package jp.gr.java_conf.datingapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mindorks.placeholderview.SwipePlaceHolderView;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import jp.gr.java_conf.datingapp.dialog.PlainDialog;
import jp.gr.java_conf.datingapp.model.Position;
import jp.gr.java_conf.datingapp.model.Profile;
import jp.gr.java_conf.datingapp.model.UserState;
import jp.gr.java_conf.datingapp.utility.AgeCalculation;
import jp.gr.java_conf.datingapp.utility.HeaderView;
import jp.gr.java_conf.datingapp.utility.WindowSizeGetter;

public class UserDetailActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private static final String RESULT_CODE = "result";

    private ImageView mImageView;
    private CircleImageView mCircleImageView;
    private TextView mToolbarName;
    private TextView mName;
    private TextView mAge;
    private TextView mAddress;
    private TextView mDetailAge;
    private TextView mDetailAddress;
    private TextView mDetailJob;
    private TextView mDescription;
    private ChipGroup mHobbies;
    private ChipGroup mLang;
    private TextView mNohobby;
    private TextView mNolang;
    private FrameLayout indicator;
    private CircleImageView online;
    private TextView mLastSeen;
    private SwipePlaceHolderView mSwipeView;
    private FirebaseFirestore mStore;
    private FirebaseAuth mAuth;
    private DatabaseReference ref;
    private FirebaseDatabase database;
    private DatabaseReference chatsRef;
    private ChildEventListener notificationListener;
    private Context context;
    private SharedPreferences preferences;

    @BindView(R.id.app_bar_layout)
    AppBarLayout appBarLayout;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbar_header_view)
    HeaderView toolbarHeaderView;

    private boolean isHideToolbarView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        context = this;
        mImageView = findViewById(R.id.detail_image);
        mCircleImageView = findViewById(R.id.circle_image);
        mToolbarName = findViewById(R.id.toolbar_name);
        mName = findViewById(R.id.name);
        mAge = findViewById(R.id.age);
        mAddress = findViewById(R.id.address);
        mDetailAge = findViewById(R.id.detail_age);
        mDetailAddress = findViewById(R.id.detail_address);
        mDetailJob = findViewById(R.id.detail_job);
        mDescription = findViewById(R.id.self_intro);
        mHobbies = findViewById(R.id.hobbies);
        mLang = findViewById(R.id.lang);
        mNohobby = findViewById(R.id.no_hobby);
        mNolang = findViewById(R.id.no_lang);
        online = findViewById(R.id.online_detail);
        mLastSeen = findViewById(R.id.last_seen);
        indicator = findViewById(R.id.indicator);
        chatsRef = FirebaseDatabase.getInstance().getReference("Chats");
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        findViewById(R.id.rejectBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(RESULT_CODE, false);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        findViewById(R.id.acceptBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(RESULT_CODE, true);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        appBarLayout.addOnOffsetChangedListener(this);

        Point windowSize = WindowSizeGetter.getDisplaySize(this.getWindowManager());

        Intent intent = getIntent();

        if (intent.getBooleanExtra("match_flg", false)) {
            findViewById(R.id.match_button).setVisibility(View.GONE);
        }
        Profile profile = (Profile) intent.getSerializableExtra("profile");
        String userId = intent.getStringExtra("user_id");

        int totalImage = 0;

        if (profile.getImg_url() != null && !profile.getImg_url().equals("")) {
            totalImage += 1;
        }

        if (profile.getImg_url2() != null && !profile.getImg_url2().equals("")) {
            totalImage += 1;
        }

        if (profile.getImg_url3() != null && !profile.getImg_url3().equals("")) {
            totalImage += 1;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        if (totalImage == 3) {
            android.view.View viewIndicator = inflater.inflate(R.layout.three_indicator, null);
            LinearLayout linearLayout = viewIndicator.findViewById(R.id.indicator_frame3);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            indicator.addView(linearLayout);
            System.out.println("3枚");
        } else if (totalImage == 2) {
            android.view.View viewIndicator = inflater.inflate(R.layout.two_indicator, null);
            LinearLayout linearLayout = viewIndicator.findViewById(R.id.indicator_frame2);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            indicator.addView(linearLayout);
            System.out.println("2枚");
        } else {
            android.view.View viewIndicator = inflater.inflate(R.layout.one_indicator, null);
            LinearLayout linearLayout = viewIndicator.findViewById(R.id.indicator_frame1);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            indicator.addView(linearLayout);
            System.out.println("1枚");
        }

        Position currentPos = new Position(0);

        database = FirebaseDatabase.getInstance();
        ref = database.getReference("/status/" + userId);
        ref.addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                  UserState userState = snapshot.getValue(UserState.class);
                  if (userState != null) {
                      Date now = new Date();
                      long timePassed = (now.getTime() - userState.getLast_changed()) / 1000 / 60 / 60;
                      if (userState.getState().equals("online")) {
                          online.setBorderColor(getResources().getColor(R.color.colorGreen));
                          mLastSeen.setText(getString(R.string.online));
                      } else if (timePassed < 24) {
                          online.setBorderColor(getResources().getColor(R.color.colorYellow));
                          mLastSeen.setText(getString(R.string.within_a_day));
                      } else if (timePassed / 24 < 3){
                          online.setBorderColor(getResources().getColor(R.color.colorLightGrey));
                          mLastSeen.setText(getString(R.string.within_a_three_day));
                      } else if (timePassed / 24 < 7) {
                          online.setBorderColor(getResources().getColor(R.color.colorLightGrey));
                          mLastSeen.setText(getString(R.string.within_a_week));
                      } else if (timePassed / 24 < 30) {
                          online.setBorderColor(getResources().getColor(R.color.colorLightGrey));
                          mLastSeen.setText(getString(R.string.within_a_month));
                      } else {
                          online.setBorderColor(getResources().getColor(R.color.colorLightGrey));
                          mLastSeen.setText(getString(R.string.over_a_month));
                      }
                  }
              }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        collapsingToolbarLayout.getLayoutParams().width = windowSize.x;
        collapsingToolbarLayout.getLayoutParams().height = windowSize.y - WindowSizeGetter.dpToPx(200);
        mImageView.getLayoutParams().width = windowSize.x;
        mImageView.getLayoutParams().height = windowSize.y - WindowSizeGetter.dpToPx(200);

        if (profile.getImg_url() != null && !profile.getImg_url().equals("")) {
            Uri url = Uri.parse(profile.getImg_url());
            if (profile.getPos() != 0) {
                if (profile.getPos() == 1) {
                    if (totalImage == 3) {
                        indicator.getChildAt(0)
                                .findViewById(R.id.indicator1)
                                .setBackground(getResources().getDrawable(R.drawable.textview_light_rounded));
                        indicator.getChildAt(0)
                                .findViewById(R.id.indicator2)
                                .setBackground(getResources().getDrawable(R.drawable.textview_rounded));
                        indicator.getChildAt(0)
                                .findViewById(R.id.indicator3)
                                .setBackground(getResources().getDrawable(R.drawable.textview_light_rounded));
                    } else {
                        indicator.getChildAt(0)
                                .findViewById(R.id.indicator1)
                                .setBackground(getResources().getDrawable(R.drawable.textview_light_rounded));
                        indicator.getChildAt(0)
                                .findViewById(R.id.indicator2)
                                .setBackground(getResources().getDrawable(R.drawable.textview_rounded));
                    }
                    Glide.with(this).load(profile.getImg_url2()).into(mImageView);
                    currentPos.setPos(1);
                } else {
                    indicator.getChildAt(0)
                            .findViewById(R.id.indicator1)
                            .setBackground(getResources().getDrawable(R.drawable.textview_light_rounded));
                    indicator.getChildAt(0)
                            .findViewById(R.id.indicator2)
                            .setBackground(getResources().getDrawable(R.drawable.textview_light_rounded));
                    indicator.getChildAt(0)
                            .findViewById(R.id.indicator3)
                            .setBackground(getResources().getDrawable(R.drawable.textview_rounded));
                    Glide.with(this).load(profile.getImg_url3()).into(mImageView);
                    currentPos.setPos(2);
                }
            } else {
                Glide.with(this).load(url).into(mImageView);
            }
            Glide.with(this).load(url).into(mCircleImageView);
        }
        if (profile.getName() != null && !profile.getName().equals("")) {
            mToolbarName.setText(profile.getName());
            mName.setText(profile.getName());
        }
        if (profile.getDate() != null) {
            String birthDayString = profile.getDate();
            int age = 0;
            try {
                age = AgeCalculation.calculate(birthDayString);
            } catch (ParseException e) {
                PlainDialog dialog = new PlainDialog(getString(R.string.no_user));
                assert getFragmentManager() != null;
                dialog.show(getSupportFragmentManager(),"Error occurred.");
            }
            mAge.setText(age + "歳");
            mDetailAge.setText(age + "歳");
        }
        if (profile.getAddress() != null && !profile.getAddress().equals("")) {
            mAddress.setText(profile.getAddress());
            mDetailAddress.setText(profile.getAddress());
        }
        if (profile.getJob() != null && !profile.getJob().equals("")) {
            mDetailJob.setText(profile.getJob());
        }
        if (profile.getDesc() != null && !profile.getDesc().equals("")) {
            mDescription.setText(profile.getDesc());
        }
        if (profile.getHobby() != null && !profile.getHobby().equals("")) {
            Set<String> hobbySet = new HashSet<>(Arrays.asList(profile.getHobby().split(",")));
            displayChipData(hobbySet, mHobbies);
            mNohobby.setVisibility(View.GONE);
        } else {
            mNohobby.setVisibility(View.VISIBLE);
            mNohobby.setText(getString(R.string.no_hobby));
        }
        if (profile.getLang() != null && !profile.getLang().equals("")) {
            Set<String> langSet = new HashSet<>(Arrays.asList(profile.getLang().split(",")));
            displayChipData(langSet, mLang);
            mNolang.setVisibility(View.GONE);
        } else {
            mNolang.setVisibility(View.VISIBLE);
            mNolang.setText(getString(R.string.no_lang));
        }


        mImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int totalImage = 0;
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                    if (profile.getImg_url() != null && !profile.getImg_url().equals("")) {
                        totalImage += 1;
                    }

                    if (profile.getImg_url2() != null && !profile.getImg_url2().equals("")) {
                        totalImage += 1;
                    }

                    if (profile.getImg_url3() != null && !profile.getImg_url3().equals("")) {
                        totalImage += 1;
                    }


                }

                if (motionEvent.getX() >= view.getWidth() / 2) {
                    if (totalImage == 2) {
                        if (currentPos.getPos() == 0) {
                            indicator.getChildAt(0)
                                    .findViewById(R.id.indicator1)
                                    .setBackground(getResources().getDrawable(R.drawable.textview_light_rounded));

                            indicator.getChildAt(0)
                                    .findViewById(R.id.indicator2)
                                    .setBackground(getResources().getDrawable(R.drawable.textview_rounded));

                            Glide.with(context).load(profile.getImg_url2()).into(mImageView);
                            currentPos.setPos(currentPos.getPos() + 1);
                        }
                    } else if (totalImage == 3) {
                        if (currentPos.getPos() == 0) {
                            indicator.getChildAt(0)
                                    .findViewById(R.id.indicator1)
                                    .setBackground(getResources().getDrawable(R.drawable.textview_light_rounded));

                            indicator.getChildAt(0)
                                    .findViewById(R.id.indicator2)
                                    .setBackground(getResources().getDrawable(R.drawable.textview_rounded));

                            indicator.getChildAt(0)
                                    .findViewById(R.id.indicator3)
                                    .setBackground(getResources().getDrawable(R.drawable.textview_light_rounded));
                            Glide.with(context).load(profile.getImg_url2()).into(mImageView);
                            currentPos.setPos(currentPos.getPos() + 1);
                            System.out.println("２枚目");
                        } else if (currentPos.getPos() == 1) {
                            indicator.getChildAt(0)
                                    .findViewById(R.id.indicator1)
                                    .setBackground(getResources().getDrawable(R.drawable.textview_light_rounded));

                            indicator.getChildAt(0)
                                    .findViewById(R.id.indicator2)
                                    .setBackground(getResources().getDrawable(R.drawable.textview_light_rounded));

                            indicator.getChildAt(0)
                                    .findViewById(R.id.indicator3)
                                    .setBackground(getResources().getDrawable(R.drawable.textview_rounded));
                            Glide.with(context).load(profile.getImg_url3()).into(mImageView);
                            currentPos.setPos(currentPos.getPos() + 1);
                            System.out.println("３枚目");
                        }
                    }
                    profile.setPos(currentPos.getPos());
                } else if (motionEvent.getX() < view.getWidth() / 2) {
                    if (totalImage == 2) {
                        if (currentPos.getPos() == 1) {

                            indicator.getChildAt(0)
                                    .findViewById(R.id.indicator1)
                                    .setBackground(getResources().getDrawable(R.drawable.textview_rounded));

                            indicator.getChildAt(0)
                                    .findViewById(R.id.indicator2)
                                    .setBackground(getResources().getDrawable(R.drawable.textview_light_rounded));

                            Glide.with(context).load(profile.getImg_url()).into(mImageView);
                            currentPos.setPos(currentPos.getPos() - 1);
                        }
                    } else if (totalImage == 3) {
                        if (currentPos.getPos() == 1) {

                            indicator.getChildAt(0)
                                    .findViewById(R.id.indicator1)
                                    .setBackground(getResources().getDrawable(R.drawable.textview_rounded));

                            indicator.getChildAt(0)
                                    .findViewById(R.id.indicator2)
                                    .setBackground(getResources().getDrawable(R.drawable.textview_light_rounded));

                            indicator.getChildAt(0)
                                    .findViewById(R.id.indicator3)
                                    .setBackground(getResources().getDrawable(R.drawable.textview_light_rounded));

                            Glide.with(context).load(profile.getImg_url()).into(mImageView);
                            currentPos.setPos(currentPos.getPos() - 1);
                        } else if (currentPos.getPos() == 2) {
                            indicator.getChildAt(0)
                                    .findViewById(R.id.indicator1)
                                    .setBackground(getResources().getDrawable(R.drawable.textview_light_rounded));

                            indicator.getChildAt(0)
                                    .findViewById(R.id.indicator2)
                                    .setBackground(getResources().getDrawable(R.drawable.textview_rounded));

                            indicator.getChildAt(0)
                                    .findViewById(R.id.indicator3)
                                    .setBackground(getResources().getDrawable(R.drawable.textview_light_rounded));

                            Glide.with(context).load(profile.getImg_url2()).into(mImageView);
                            currentPos.setPos(currentPos.getPos() - 1);
                        }
                    }
                    profile.setPos(currentPos.getPos());
                }
                return true;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;

        if (percentage == 1f && isHideToolbarView) {
            toolbarHeaderView.setVisibility(View.VISIBLE);
            mCircleImageView.animate().alpha(1f);
            mToolbarName.animate().alpha(1f);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorGrey));
            isHideToolbarView = !isHideToolbarView;
        } else if (percentage < 1f && !isHideToolbarView) {
            toolbarHeaderView.setVisibility(View.GONE);
            mCircleImageView.animate().alpha(0f);
            mToolbarName.animate().alpha(0f);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            isHideToolbarView = !isHideToolbarView;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void displayChipData(final Set<String> chipList, final ChipGroup chipGroup) {
        chipGroup.removeAllViews();
        for (String s : chipList) {
            Chip chip = (Chip) this.getLayoutInflater().inflate(R.layout.single_chip_item, null, false);
            chip.setText(s);
            chipGroup.addView(chip);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        super.onResume();
        mImageView.setTransitionName("");
    }

    @Override
    public void onPause() {
        super.onPause();
//        chatsRef.removeEventListener(notificationListener);
    }
}
