package jp.gr.java_conf.datingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class EmailConfirmationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView mEmail;
    private CardView mSend;
    private SharedPreferences preferences;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_email);

        mAuth = FirebaseAuth.getInstance();
        mEmail = findViewById(R.id.signin_email);
        mSend = findViewById(R.id.signin_card_view);
        preferences = getSharedPreferences("DATA", MODE_PRIVATE);
        mContext = this;

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString();
                if (email.equals(preferences.getString("email_address", null))) {
                    String emailLink = "";
                    if (getIntent().getData() != null) {
                        emailLink = getIntent().getData().toString();
                    }
                    if (mAuth.isSignInWithEmailLink(emailLink)) {
                        System.out.println("ユーザーメール");
                        System.out.println(email);
                        System.out.println("検証終了");
                        mAuth.signInWithEmailLink(email, emailLink)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("HomeActivity", "メールリンクでのサインイン完了！");
                                            AuthResult result = task.getResult();
                                            System.out.println(result.getUser());
                                            System.out.println(result.getAdditionalUserInfo().getProfile());
                                            if (result.getAdditionalUserInfo().isNewUser()) {
                                                System.out.println("新しいユーザーなのでProfileSettingsActivityに遷移します");
                                                Intent intent = new Intent(EmailConfirmationActivity.this, ProfileSettingsActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                System.out.println("既存ユーザーのためホームに遷移");
                                                Intent intent = new Intent(EmailConfirmationActivity.this, HomeActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                    }
                                });
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
