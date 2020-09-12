package jp.gr.java_conf.datingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AdditionalUserInfo;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;

import jp.gr.java_conf.datingapp.adapters.ViewPagerAdapter;
import jp.gr.java_conf.datingapp.dialogs.DialogManager;
import jp.gr.java_conf.datingapp.fragments.SignInFragment;
import jp.gr.java_conf.datingapp.fragments.SignUpFragment;
import jp.gr.java_conf.datingapp.utility.CloseKeyboard;


public class MainActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private TabLayout mTabs;
    private int mTabPositionInt;
    private SharedPreferences mTabPosition;
    private SharedPreferences.Editor mPositionEditor;
    private ViewPagerAdapter adapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private CallbackManager mCallbackManager;
    private Context mContext;
    private LoginButton mLoginButton;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mTabPosition = getSharedPreferences("Data", MODE_PRIVATE);
        mPositionEditor = mTabPosition.edit();
        mPositionEditor.putInt("position", 10);
        mPositionEditor.apply();

        mCallbackManager = CallbackManager.Factory.create();
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.fb_button, null);
        mLoginButton = view.findViewById(R.id.login_button);
        setAuthCallback();

        mViewPager = findViewById(R.id.viewPager);
        mTabs = findViewById(R.id.tabLayout);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressbar2);
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        mViewPager.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mStore = FirebaseFirestore.getInstance();
        CloseKeyboard.setupUI(findViewById(R.id.constraint_main), this);
    }

    private void setAuthCallback() {
        mLoginButton.setPermissions("email", "public_profile", "user_friends");
        mLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                firebaseAuthWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("Cancel", "キャンセルされました");
            }

            @Override
            public void onError(FacebookException error) {
                DialogManager dialog = new DialogManager(getString(R.string.error));
                dialog.show(getSupportFragmentManager(), "Error occurred.");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        int position = mTabPosition.getInt("position", -1);
        if (mTabs.getTabAt(0) != null && mTabs.getTabAt(1) != null) {
            if (position == 0) {
                mTabs.getTabAt(0).select();
            } else if (position == 1) {
                mTabs.getTabAt(1).select();
            } else {
                mTabs.getTabAt(0).select();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPositionEditor.putInt("position", mTabPositionInt);
        mPositionEditor.commit();
    }

    public void firebaseAuthWithFacebook(final AccessToken accessToken) {
        GraphRequest request = GraphRequest.newGraphPathRequest(
                accessToken,
                "/me",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        try {
                            String id = (String) response.getJSONObject().get("id");
                            String graphPath = "/" + id + "/friends";

                            GraphRequest request = GraphRequest.newGraphPathRequest(
                                    accessToken,
                                    graphPath,
                                    new GraphRequest.Callback() {
                                        @Override
                                        public void onCompleted(GraphResponse response) {
                                            // Insert your code here
                                            try {
                                                int numFriends = (int)response.getJSONObject().getJSONObject("summary").get("total_count");
                                                if (numFriends >= 10) {
                                                    AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
                                                    mAuth.signInWithCredential(credential)
                                                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                                    if (task.isSuccessful()) {
                                                                        AdditionalUserInfo info = task.getResult().getAdditionalUserInfo();
                                                                        if (info != null) {
                                                                            if (info.isNewUser()) {
                                                                                editProfile();
                                                                            } else {
                                                                                changeActivity();
                                                                            }
                                                                        } else {
                                                                            Toast.makeText(mContext, getString(R.string.user_none), Toast.LENGTH_LONG).show();
                                                                        }
                                                                    } else {
                                                                        Toast.makeText(mContext, getString(R.string.signin_fail), Toast.LENGTH_LONG).show();
                                                                    }
                                                                }
                                                            });
                                                } else {
                                                    DialogManager dialog = new DialogManager(getString(R.string.fb_fail));
                                                    dialog.show(getSupportFragmentManager(), "Failed.");
                                                    LoginManager.getInstance().logOut();
                                                }
                                            } catch (JSONException e) {
                                                System.out.println(e);
                                            }
                                        }
                                    });
                            request.executeAsync();
                        } catch (JSONException e) {
                            System.out.println(e);
                        }

                    }
                });
        request.executeAsync();
    }

    private void editProfile() {
        Intent intent = new Intent(MainActivity.this, ProfileSettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void changeActivity() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            System.out.println(mAuth.getCurrentUser().getUid());
            progressBar.setVisibility(ProgressBar.VISIBLE);
            mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        String sex = task.getResult().getString("sex");
                        if (sex != null && !sex.equals("")) {
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                        }else {
                            Intent intent = new Intent(MainActivity.this, ProfileSettingsActivity.class);
                            startActivity(intent);
                        }
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                        finish();
                    }
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mViewPager.setBackground(getResources().getDrawable(R.drawable.white_bg_with, null));
            } else {
                mViewPager.setBackground(getResources().getDrawable(R.drawable.white_bg_with));
            }
            adapter = new ViewPagerAdapter(getSupportFragmentManager());

            adapter.addFragment(new SignInFragment(), "サインイン");
            adapter.addFragment(new SignUpFragment(), "サインアップ");
            mViewPager.setAdapter(adapter);
            mTabs.setupWithViewPager(mViewPager);

            mTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    mTabPositionInt = tab.getPosition();
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    System.out.println(tab);
                }
            });
        }
    }
}
