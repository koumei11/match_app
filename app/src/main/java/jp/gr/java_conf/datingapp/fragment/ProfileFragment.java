package jp.gr.java_conf.datingapp.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.gr.java_conf.datingapp.MainActivity;
import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.progressbar.LogoutProgressButton;
import jp.gr.java_conf.datingapp.progressbar.SaveProgressButton;
import jp.gr.java_conf.datingapp.utility.CloseKeyboard;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    private CircleImageView mImg;
    private EditText mName;
    private EditText mJob;
    private EditText mHobby;
    private EditText mLang;
    private EditText mDesc;
    private ChipGroup mHobbyGroup;
    private ChipGroup mLangGroup;
    private Set<String> mHobbyList;
    private Set<String> mLangList;
    private CardView mSaveProfile;
    private Uri url = null;
    private static final int RESULT_LOAD_IMG = 1212;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private StorageReference mStorage;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        CloseKeyboard.setupUI(view.findViewById(R.id.constraint_profile), getActivity());

        mImg = view.findViewById(R.id.pro_image);
        mName = view.findViewById(R.id.pro_name);
        mJob = view.findViewById(R.id.pro_job);
        mLang = view.findViewById(R.id.pro_lang);
        mHobby = view.findViewById(R.id.pro_hobby);
        mDesc = view.findViewById(R.id.pro_desc);
        mHobbyGroup = view.findViewById(R.id.chip_hobby);
        mLangGroup = view.findViewById(R.id.chip_lang);
        mSaveProfile = view.findViewById(R.id.save);

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();

        mHobbyList = new HashSet<>();
        mLangList = new HashSet<>();

        getProfileData();

        mImg.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
            }
        });

        mDesc.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mDesc.setRawInputType(InputType.TYPE_CLASS_TEXT);

        view.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                LogoutProgressButton button = new LogoutProgressButton(view);
                button.buttonActivated();
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference myRef = database.getReference("/status/" + mAuth.getCurrentUser().getUid());

                Map<String, Object> isOfflineForDatabase = new HashMap<>();
                isOfflineForDatabase.put("state", "offline");
                isOfflineForDatabase.put("last_changed", ServerValue.TIMESTAMP);

                myRef.setValue(isOfflineForDatabase);

                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();

                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
                getActivity().finish();

                Toast.makeText(getContext(), "ログアウトしました", Toast.LENGTH_SHORT).show();
                button.buttonFinished();
            }
        });

        mSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SaveProgressButton save = new SaveProgressButton(view, false);
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
                                    String downloadUrl = uri.toString();
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("name", mName.getText().toString());
                                    map.put("job", mJob.getText().toString());
                                    map.put("hobby", joinString(mHobbyList));
                                    map.put("lang", joinString(mLangList));
                                    map.put("desc", mDesc.getText().toString());
                                    map.put("img_url", downloadUrl);
                                    saveUserData(map, save);
                                }
                            });
                        }
                    });
                } else {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", mName.getText().toString());
                    map.put("job", mJob.getText().toString());
                    map.put("hobby", joinString(mHobbyList));
                    map.put("lang", joinString(mLangList));
                    map.put("desc", mDesc.getText().toString());
                    saveUserData(map, save);
                }
            }
        });


        return view;
    }

    private void getProfileData() {
        mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String name = task.getResult().getString("name");
                    String job = task.getResult().getString("job");
                    String desc = task.getResult().getString("desc");
                    String hobby = task.getResult().getString("hobby");
                    String img_url = task.getResult().getString("img_url");
                    String lang = task.getResult().getString("lang");
                    mName.setText(name);
                    mJob.setText(job);
                    mDesc.setText(desc);
                    mHobbyList = hobby == null ? mHobbyList : new HashSet<>(Arrays.asList(hobby.split("\\s*,\\s*")));
                    mLangList = lang == null ? mLangList : new HashSet<>(Arrays.asList(lang.split("\\s*,\\s*")));
                    if (img_url != null && getContext() != null) {
                        Glide.with(getContext()).load(img_url).into(mImg);
                    }

                    setListener(mHobby, mHobbyList, mHobbyGroup);
                    setListener(mLang, mLangList, mLangGroup);

                    displayChipData(mHobbyList, mHobbyGroup);
                    displayChipData(mLangList, mLangGroup);
                }
            }
        });
    }

    private String joinString(Set<String> chipList) {
        StringBuilder strings = null;
        if (chipList.size() != 0) {
            strings = new StringBuilder();
            for (String string : chipList)
            {
                strings.append(string).append(",");
            }
        }

        return strings == null ? null : strings.substring(0, strings.length() - 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            if (imageUri != null) {
                url = imageUri;
                Glide.with(getContext()).load(imageUri).into(mImg);
            }
        }else {
            Toast.makeText(getContext(), "写真を選択していません", Toast.LENGTH_LONG).show();
        }
    }

    private void setListener(final EditText editText,
                             final Set<String> chipList,
                             final ChipGroup chipGroup) {

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_GO && !editText.getText().toString().equals("")) {
                    chipList.add(editText.getText().toString());
                    displayChipData(chipList, chipGroup);
                    editText.setText("");
                    return true;
                }
                return false;
            }
        });

    }

    private void displayChipData(final Set<String> chipList, final ChipGroup chipGroup) {
        chipGroup.removeAllViews();
        for (String s : chipList) {
            Chip chip = (Chip) getActivity().getLayoutInflater().inflate(R.layout.single_chip_item, null, false);
            chip.setText(s);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chipGroup.removeView(view);
                    Chip c = (Chip) view;
                    chipList.remove(c.getText().toString());
                }
            });
            chipGroup.addView(chip);
        }
    }

    private void saveUserData(Map<String, Object> map, final SaveProgressButton save) {
        mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                .set(map, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getContext(), getString(R.string.complete_save), Toast.LENGTH_SHORT).show();
                        save.buttonFinished();
                    }
                });
    }
}
