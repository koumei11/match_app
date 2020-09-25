package jp.gr.java_conf.datingapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import jp.gr.java_conf.datingapp.EmailSendActivity;
import jp.gr.java_conf.datingapp.HomeActivity;
import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.dialog.PlainDialog;
import jp.gr.java_conf.datingapp.progressbar.SignInProgressButton;
import jp.gr.java_conf.datingapp.utility.CloseKeyboard;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignInFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignInFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText mEmail;
//    private EditText mPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private CardView mLoginBtn;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ConstraintLayout mLayout;
    private DatabaseReference userRef;
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            checkFieldsForEmptyValues();
        }
    };

    public SignInFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignInFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SignInFragment newInstance(String param1, String param2) {
        SignInFragment fragment = new SignInFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        CloseKeyboard.setupUI(view.findViewById(R.id.constraint_signin), getActivity());

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mEmail = view.findViewById(R.id.signin_email);
        mLoginBtn = view.findViewById(R.id.signin_card_view);
        mLayout = view.findViewById(R.id.signin_constraint_layout);
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        preferences = getActivity().getApplicationContext().getSharedPreferences("DATA", Context.MODE_PRIVATE);
        editor = preferences.edit();

        mEmail.addTextChangedListener(mTextWatcher);
        checkFieldsForEmptyValues();

        LoginButton mFBLogin = view.findViewById(R.id.login_button);
        mFBLogin.setLoginText(getString(R.string.fb_sign_in_text));
        mFBLogin.setLogoutText(getString(R.string.fb_sign_in_text));

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SignInProgressButton button = new SignInProgressButton(view, false);
                button.buttonActivated();
                if (validateEmail()) {
                    String email = mEmail.getText().toString();
                    mEmail.setText("");
                    System.out.println("メールリンク");
                    sendEmailLink(email, button);
                } else {
                    button.buttonFinished();
                }
            }
        });
        return view;
    }

    private void sendEmailLink(String email, SignInProgressButton button) {
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl(getString(R.string.deep_link))
                .setHandleCodeInApp(true)
                .setIOSBundleId(getString(R.string.package_name))
                .setAndroidPackageName(
                        getString(R.string.package_name),
                        true,
                        getString(R.string.minimum_sdk)
                )
                .build();
        mAuth.sendSignInLinkToEmail(email, actionCodeSettings)

                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        System.out.println("成功!");
                        editor.putString("email_address", email);
                        String passCode = UUID.randomUUID().toString().split("-")[0];
                        editor.putString("pass_code", passCode);
                        editor.apply();
                        button.buttonFinished();
                        Intent intent = new Intent(getContext(), EmailSendActivity.class);
                        startActivity(intent);
                    }
                });
    }

    private void checkFieldsForEmptyValues() {
        String email = mEmail.getText().toString();

        if (email.equals("")) {
            mLoginBtn.setEnabled(false);
            mLayout.setBackgroundColor(mLoginBtn.getResources().getColor(R.color.colorLightRed));
        } else {
            mLoginBtn.setEnabled(true);
            mLayout.setBackgroundColor(mLoginBtn.getResources().getColor(R.color.colorRed));
        }
    }

    private boolean validateEmail() {
        String emailInput = mEmail.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
           mEmail.setError(getString(R.string.error_invalid_email));
           return false;
        } else {
            mEmail.setError(null);
            return true;
        }
    }
}
