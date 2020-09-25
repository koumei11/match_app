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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import jp.gr.java_conf.datingapp.progressbar.SignInProgressButton;
import jp.gr.java_conf.datingapp.utility.CloseKeyboard;

public class EmailConfirmationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView mEmail;
    private TextView mPassCode;
    private CardView mSend;
    private SharedPreferences preferences;
    private Context mContext;
    private FirebaseFirestore mStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_email);

        CloseKeyboard.setupUI(findViewById(R.id.constraint_email_conf), this);

        mAuth = FirebaseAuth.getInstance();
        mEmail = findViewById(R.id.signin_email);
        mPassCode = findViewById(R.id.signin_pass_code);
        mSend = findViewById(R.id.signin_card_view);
        preferences = getSharedPreferences("DATA", MODE_PRIVATE);
        mContext = this;
        mStore = FirebaseFirestore.getInstance();

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString();
                String inputPassCode = mPassCode.getText().toString();
                final SignInProgressButton button = new SignInProgressButton(view, true);
                button.buttonActivated();
                if (email.equals(preferences.getString("email_address", null))
                        && inputPassCode.equals(preferences.getString("pass_code", null))) {
                    String emailLink = "";
                    if (getIntent().getData() != null) {
                        emailLink = getIntent().getData().toString();
                    }
                    if (mAuth.isSignInWithEmailLink(emailLink)) {
                        mAuth.signInWithEmailLink(email, emailLink)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("HomeActivity", "メールリンクでのサインイン完了！");
                                            AuthResult result = task.getResult();
                                            if (result != null && result.getAdditionalUserInfo() != null && result.getUser() != null) {
                                                mStore.collection("Users").document(result.getUser().getUid())
                                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (result.getAdditionalUserInfo().isNewUser() || task.getResult().getString("sex") == null) {
                                                            System.out.println("新しいユーザーなのでProfileSettingsActivityに遷移します");
                                                            Intent intent = new Intent(EmailConfirmationActivity.this, ProfileSettingsActivity.class);
                                                            startActivity(intent);
                                                            Toast.makeText(mContext, getString(R.string.welcome), Toast.LENGTH_LONG).show();
                                                            finish();
                                                        } else {
                                                            System.out.println("既存ユーザーのためホームに遷移");
                                                            Intent intent = new Intent(EmailConfirmationActivity.this, HomeActivity.class);
                                                            startActivity(intent);
                                                            Toast.makeText(mContext, getString(R.string.welcome_back), Toast.LENGTH_LONG).show();
                                                            finish();
                                                        }
                                                    }
                                                });
                                            }
                                            button.buttonFinished();
                                        }
                                    }
                                });
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.invalid_auth), Toast.LENGTH_SHORT).show();
                    button.buttonFinished();
                }
            }
        });
    }
}
