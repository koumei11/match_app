package jp.gr.java_conf.datingapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.gr.java_conf.datingapp.dialogs.AddressDialog;
import jp.gr.java_conf.datingapp.dialogs.DialogManager;
import jp.gr.java_conf.datingapp.fragments.DatePickFragment;
import jp.gr.java_conf.datingapp.progressbar.SaveProgressButton;
import jp.gr.java_conf.datingapp.utility.AgeCalculation;
import jp.gr.java_conf.datingapp.utility.CloseKeyboard;

public class ProfileSettingsActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 100;
    private CircleImageView mImg;
    private FirebaseAuth mAuth;
    private EditText mName;
    private TextView mDate;
    private TextView mAddress;
    private EditText mJob;
    private String mSex;
    private Uri url = null;
    private CardView mStartBtn;
    private ConstraintLayout mLayout;
    private Activity mActivity;
    private static final int RESULT_LOAD_IMG = 1212;
    private FirebaseFirestore mStore;
    private StorageReference mStorage;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        mImg = findViewById(R.id.new_image);
        mName = findViewById(R.id.new_name);
        mDate = findViewById(R.id.date);
        mSex = "woman";
        mAddress = findViewById(R.id.address);
        mJob = findViewById(R.id.new_job);
        mStartBtn = findViewById(R.id.save);
        mLayout = findViewById(R.id.pro_save_layout);

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();

        mActivity = this;

        checkFieldsForEmptyValues();
        CloseKeyboard.setupUI(findViewById(R.id.constraint_profile_settings), mActivity);

        mImg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
//                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//                photoPickerIntent.setType("image/*");
//                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
                Intent intent = new Intent(ProfileSettingsActivity.this, ImageActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        mName.addTextChangedListener(mTextWatcher);
        mJob.addTextChangedListener(mTextWatcher);

//        List<String> addresses = Arrays.asList(getResources().getStringArray(R.array.address_list));

        mDate.addTextChangedListener(new TextWatcher() {
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
        });

        mAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddressDialog addressDialog = new AddressDialog(getResources().getStringArray(R.array.address_list), mAddress);
                addressDialog.show(getSupportFragmentManager(), "Choose an address.");
            }
        });

        mAddress.addTextChangedListener(new TextWatcher() {
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
        });

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SaveProgressButton save = new SaveProgressButton(view, true);
                save.buttonActivated();
                Long tsLong = System.currentTimeMillis()/1000;
                String ts = tsLong.toString();
                if (url != null) {
                    mStorage.child(ts + "/").putFile(url).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        String downloadUrl = taskSnapshot.getStorage().getDownloadUrl().toString();
//                        Log.i("TAG", "onSuccess" + downloadUrl);
                            Task<Uri> res = taskSnapshot.getStorage().getDownloadUrl();
                            res.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    if (checkValidAge()) {
                                        String downloadUrl = uri.toString();
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("img_url", downloadUrl);
                                        map.put("name", mName.getText().toString());
                                        map.put("date", mDate.getText().toString());
                                        map.put("sex", mSex);
                                        map.put("address", mAddress.getText().toString());
                                        map.put("job", mJob.getText().toString());
                                        map.put("user_id", mAuth.getCurrentUser().getUid());
                                        map.put("account_flg", true);
                                        saveUserData(map, save);
                                    } else {
                                        DialogManager dialog = new DialogManager(getString(R.string.not_allowed));
                                        assert getFragmentManager() != null;
                                        dialog.show(getSupportFragmentManager(),"Not Allowed.");
                                        save.buttonFinished();
                                    }
                                }
                            });
                        }
                    });
                } else {
                    if (checkValidAge()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", mName.getText().toString());
                        map.put("date", mDate.getText().toString());
                        map.put("sex", mSex);
                        map.put("address", mAddress.getText().toString());
                        map.put("job", mJob.getText().toString());
                        map.put("user_id", mAuth.getCurrentUser().getUid());
                        map.put("account_flg", true);
                        saveUserData(map, save);
                    } else {
                        DialogManager dialog = new DialogManager(getString(R.string.not_allowed));
                        assert getFragmentManager() != null;
                        dialog.show(getSupportFragmentManager(),"Not Allowed.");
                        save.buttonFinished();
                    }
                }
            }
        });
    }

    public boolean checkValidAge() {
        try {
            return AgeCalculation.calculate(mDate.getText().toString()) >= 18;
        } catch (ParseException e) {
            DialogManager dialog = new DialogManager(getString(R.string.no_user));
            assert getFragmentManager() != null;
            dialog.show(getSupportFragmentManager(),"Error occurred.");
        }
        return false;
    }

    private void saveUserData(Map<String, Object> map, final SaveProgressButton save) {
        mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                .set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(ProfileSettingsActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        Toast.makeText(mActivity, getString(R.string.welcome), Toast.LENGTH_SHORT).show();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("/account/" + mAuth.getCurrentUser().getUid());
                        Map<String, Object> map = new HashMap<>();
                        map.put("account_flg", true);
                        reference.setValue(map);
                        save.buttonFinished();
                    }
                });
    }

    private void checkFieldsForEmptyValues() {
        String name = mName.getText().toString();
        boolean isDateSet = mDate.getText().toString().matches("^\\d{4}/\\d{2}/\\d{2}$");
        String address = mAddress.getText().toString();
        String job = mJob.getText().toString();

        if (name.equals("") || job.equals("") || !isDateSet || address.equals("居住地を入力してください")) {
            mStartBtn.setEnabled(false);
            mLayout.setBackgroundColor(mStartBtn.getResources().getColor(R.color.colorLightRed));
        } else {
            mStartBtn.setEnabled(true);
            mLayout.setBackgroundColor(mStartBtn.getResources().getColor(R.color.colorRed));
        }
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickFragment(mDate);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                if (data != null) {
                    final Uri imageUri = Uri.parse(data.getStringExtra("image_uri"));
                    if (imageUri != null) {
                        url = imageUri;
                        Glide.with(mActivity).load(imageUri).into(mImg);
                    }
                }
            }
        }else {
            Toast.makeText(mActivity, "写真を選択していません", Toast.LENGTH_LONG).show();
        }
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radio_woman:
                if (checked) {
                    mSex = "woman";
                }
                break;
            case R.id.radio_man:
                if (checked) {
                    mSex = "man";
                }
                break;
        }
    }
}
