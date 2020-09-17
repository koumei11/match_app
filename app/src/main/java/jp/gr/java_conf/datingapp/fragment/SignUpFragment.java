package jp.gr.java_conf.datingapp.fragment;

import android.content.Intent;
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

import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;

import jp.gr.java_conf.datingapp.ProfileSettingsActivity;
import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.dialog.PlainDialog;
import jp.gr.java_conf.datingapp.progressbar.SignUpProgressButton;
import jp.gr.java_conf.datingapp.utility.CloseKeyboard;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FirebaseAuth mAuth;
    private EditText mEmail;
    private EditText mPassword;
    private EditText mPasswordRe;
    private CardView mSignUpBtn;
    private ConstraintLayout mLayout;
    private FirebaseFirestore mStore;
    private CallbackManager mCallbackManager;
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



    public SignUpFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignUpFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SignUpFragment newInstance(String param1, String param2) {
        SignUpFragment fragment = new SignUpFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        CloseKeyboard.setupUI(view.findViewById(R.id.constraint_signup), getActivity());

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mAuth = FirebaseAuth.getInstance();
        mEmail = view.findViewById(R.id.signup_email);
        mPassword = view.findViewById(R.id.signup_password);
        mPasswordRe = view.findViewById(R.id.signup_password_re);
        mSignUpBtn = view.findViewById(R.id.signup_card_view);
        mLayout = view.findViewById(R.id.signup_constraint_layout);
        mStore = FirebaseFirestore.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mEmail.addTextChangedListener(mTextWatcher);
        mPassword.addTextChangedListener(mTextWatcher);
        mPasswordRe.addTextChangedListener(mTextWatcher);

        checkFieldsForEmptyValues();

        LoginButton mFBSignUp = view.findViewById(R.id.login_button);
        mFBSignUp.setLoginText(getString(R.string.fb_sign_up_text));
        mFBSignUp.setLogoutText(getString(R.string.fb_sign_up_text));

        mSignUpBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                final SignUpProgressButton button = new SignUpProgressButton(getContext(), view);
                button.buttonActivated();
                if (validEmail()  && validPassword()) {
                    String email = mEmail.getText().toString();
                    String password = mPassword.getText().toString();
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("email", mEmail.getText().toString());
                                mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                                        .set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            editProfile();
                                        } else {
                                            Toast.makeText(getContext(), "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }else {
                                PlainDialog dialog = new PlainDialog(getString(R.string.already_exists));
                                assert getFragmentManager() != null;
                                dialog.show(getFragmentManager(), "SignUp Failed.");
                                button.buttonFinished();
                            }
                        }
                    });
                }else {
                    button.buttonFinished();
                }
            }
        });

        return view;
    }

    private void editProfile() {
        Intent intent = new Intent(getContext(), ProfileSettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void checkFieldsForEmptyValues() {
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();
        String passwordRe = mPasswordRe.getText().toString();

        if (email.equals("") || password.equals("") || passwordRe.equals("")) {
            mSignUpBtn.setEnabled(false);
            mLayout.setBackgroundColor(mSignUpBtn.getResources().getColor(R.color.colorLightRed));
        } else {
            mSignUpBtn.setEnabled(true);
            mLayout.setBackgroundColor(mSignUpBtn.getResources().getColor(R.color.colorRed));
        }
    }

    private boolean validEmail() {
        String emailInput = mEmail.getText().toString().trim();
        if(!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            mEmail.setError(getString(R.string.error_invalid_email));
            return false;
        } else {
            mEmail.setError(null);
            return true;
        }
    }

    private boolean validPassword() {
        String passwordInput = mPassword.getText().toString().trim();
        String passwordInputRe = mPasswordRe.getText().toString().trim();

        if (passwordInput.length() < 6) {
            mPassword.setError(getString(R.string.error_password));
            return false;
        }else if (!passwordInput.equals(passwordInputRe)) {
            mPasswordRe.setError(getString(R.string.error_password_re));
            return false;
        }else {
            mPassword.setError(null);
            return true;
        }
    }
}
