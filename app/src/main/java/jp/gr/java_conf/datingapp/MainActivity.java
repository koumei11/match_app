package jp.gr.java_conf.datingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;

import java.util.Objects;

import jp.gr.java_conf.datingapp.adapter.ViewPagerAdapter;
import jp.gr.java_conf.datingapp.dialog.PlainDialog;
import jp.gr.java_conf.datingapp.fragment.SignInFragment;
import jp.gr.java_conf.datingapp.utility.CloseKeyboard;


public class MainActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private ViewPagerAdapter adapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private DatabaseReference userRef;
    private CallbackManager mCallbackManager;
    private Context mContext;
    private LoginButton mLoginButton;
    private TextView privacy;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        progressBar = findViewById(R.id.progressbar_main);
        privacy = findViewById(R.id.privacy_policy);
        mCallbackManager = CallbackManager.Factory.create();
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.fb_button, null);
        mLoginButton = view.findViewById(R.id.login_button);
        setAuthCallback();

        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(getString(R.string.policy_url));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        mViewPager = findViewById(R.id.viewPager);
//        mTabs = findViewById(R.id.tabLayout);
        mAuth = FirebaseAuth.getInstance();
        progressBar.setVisibility(ProgressBar.GONE);

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
                PlainDialog dialog = new PlainDialog(getString(R.string.error));
                dialog.show(getSupportFragmentManager(), "Error occurred.");
            }
        });
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
                                                if (numFriends <= 10) {
                                                    AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
                                                    mAuth.signInWithCredential(credential)
                                                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                                    if (task.isSuccessful()) {
                                                                        AdditionalUserInfo info = Objects.requireNonNull(task.getResult()).getAdditionalUserInfo();
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
                                                    PlainDialog dialog = new PlainDialog(getString(R.string.fb_fail));
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
            progressBar.setVisibility(ProgressBar.VISIBLE);
            privacy.setVisibility(View.GONE);
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
                        privacy.setVisibility(View.VISIBLE);
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
            mViewPager.setAdapter(adapter);
//            mTabs.setupWithViewPager(mViewPager);
        }
    }
}
