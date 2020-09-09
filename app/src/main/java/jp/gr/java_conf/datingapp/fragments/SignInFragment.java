package jp.gr.java_conf.datingapp.fragments;

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

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import jp.gr.java_conf.datingapp.HomeActivity;
import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.dialogs.DialogManager;
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
    private EditText mPassword;
    private FirebaseAuth mAuth;
    private CardView mLoginBtn;
    private ConstraintLayout mLayout;
    private CallbackManager mCallbackManager;
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        CloseKeyboard.setupUI(view.findViewById(R.id.constraint_signin), getActivity());

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mEmail = view.findViewById(R.id.signin_email);
        mPassword = view.findViewById(R.id.signin_password);
        mLoginBtn = view.findViewById(R.id.signin_card_view);
        mLayout = view.findViewById(R.id.signin_constraint_layout);
        mAuth = FirebaseAuth.getInstance();

        mEmail.addTextChangedListener(mTextWatcher);
        mPassword.addTextChangedListener(mTextWatcher);
        checkFieldsForEmptyValues();

        LoginButton mFBLogin = view.findViewById(R.id.login_button);
        mFBLogin.setLoginText(getString(R.string.fb_sign_in_text));
        mFBLogin.setLogoutText(getString(R.string.fb_sign_in_text));

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SignInProgressButton button = new SignInProgressButton(view);
                button.buttonActivated();
                if (validateEmail() && validatePassword()) {
                    String email = mEmail.getText().toString();
                    String password = mPassword.getText().toString();
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                System.out.println("成功");
                                changeActivity();
                            } else {
                                System.out.println("失敗");
                                DialogManager dialog = new DialogManager(getString(R.string.no_user));
                                assert getFragmentManager() != null;
                                dialog.show(getFragmentManager(), "SignIn Failed.");
                                button.buttonFinished();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                            button.buttonFinished();
                        }
                    });
                } else {
                    button.buttonFinished();
                }
            }
        });
        return view;
    }

    private void changeActivity() {
        Intent intent = new Intent(getContext(), HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void checkFieldsForEmptyValues() {
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();

        if (email.equals("") || password.equals("")) {
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

    private boolean validatePassword() {
        String passwordInput = mPassword.getText().toString().trim();

        if (passwordInput.length() < 6) {
            mPassword.setError(getString(R.string.error_password));
            return false;
        } else {
            mPassword.setError(null);
            return true;
        }
    }
}
