package jp.gr.java_conf.datingapp.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
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
import jp.gr.java_conf.datingapp.ImageActivity;
import jp.gr.java_conf.datingapp.MainActivity;
import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.dialog.PlainDialog;
import jp.gr.java_conf.datingapp.dialog.SelectionDialog;
import jp.gr.java_conf.datingapp.progressbar.SaveProgressButton;
import jp.gr.java_conf.datingapp.utility.CloseKeyboard;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    private static final int REQUEST_CODE1 = 100;
    private static final int REQUEST_CODE2 = 101;
    private static final int REQUEST_CODE3 = 102;

    private CircleImageView mImg;
    private CircleImageView mImg2;
    private CircleImageView mImg3;
    private EditText mName;
    private TextView mAddress;
    private TextView mJob;
    private EditText mHobby;
    private EditText mLang;
    private EditText mDesc;
    private TextView mQuit;
    private ChipGroup mHobbyGroup;
    private ChipGroup mLangGroup;
    private Set<String> mHobbyList;
    private Set<String> mLangList;
    private CardView mSaveProfile;
    private static final int RESULT_LOAD_IMG = 1212;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private StorageReference mStorage;
    private String uid;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        CloseKeyboard.setupUI(view.findViewById(R.id.constraint_profile), getActivity());

        mImg = view.findViewById(R.id.pro_image);
        mImg2 = view.findViewById(R.id.pro_image2);
        mImg3 = view.findViewById(R.id.pro_image3);
        mName = view.findViewById(R.id.pro_name);
        mAddress = view.findViewById(R.id.pro_address);
        mJob = view.findViewById(R.id.pro_job);
        mLang = view.findViewById(R.id.pro_lang);
        mHobby = view.findViewById(R.id.pro_hobby);
        mDesc = view.findViewById(R.id.pro_desc);
        mHobbyGroup = view.findViewById(R.id.chip_hobby);
        mLangGroup = view.findViewById(R.id.chip_lang);
        mSaveProfile = view.findViewById(R.id.save);
        mQuit = view.findViewById(R.id.quit);

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        uid = mAuth.getCurrentUser().getUid();

        mHobbyList = new HashSet<>();
        mLangList = new HashSet<>();

        getProfileData();

        mAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectionDialog selectionDialog = new SelectionDialog(getResources().getStringArray(R.array.address_list), getString(R.string.enter_address), mAddress);
                selectionDialog.show(getFragmentManager(), "Choose an address.");
            }
        });

        mJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectionDialog selectionDialog = new SelectionDialog(getResources().getStringArray(R.array.job_list), getString(R.string.enter_job), mJob);
                selectionDialog.show(getFragmentManager(), "Choose an job.");
            }
        });

        mQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getString(R.string.quit))
                        .setMessage(getString(R.string.quit_sentence))
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("account_flg", false);
                                mStore.collection("Users").document(uid).set(map, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        final DatabaseReference myRef = database.getReference("/status/" + mAuth.getCurrentUser().getUid());

                                        Map<String, Object> isOfflineForDatabase = new HashMap<>();
                                        isOfflineForDatabase.put("state", "offline");
                                        isOfflineForDatabase.put("last_changed", ServerValue.TIMESTAMP);

                                        myRef.setValue(isOfflineForDatabase);
                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("/account/" + uid);
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("account_flg", false);
                                        reference.setValue(map);

                                        FirebaseAuth.getInstance().signOut();
                                        LoginManager.getInstance().logOut();

                                        Intent intent = new Intent(getContext(), MainActivity.class);
                                        startActivity(intent);
                                        getActivity().finish();

                                        Toast.makeText(getContext(), getString(R.string.quit_done), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

                builder.create().show();
            }
        });

        mImg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ImageActivity.class);
                startActivityForResult(intent, REQUEST_CODE1);
            }
        });

        mImg2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ImageActivity.class);
                startActivityForResult(intent, REQUEST_CODE2);
            }
        });

        mImg2.setLongClickable(true);
        mImg2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mStore.collection("Users").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.get("img_url2") != null) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setItems(getResources().getStringArray(R.array.image_select), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int position) {
                                    if (position == 0) {
                                        mStore.collection("Users").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot.get("img_url") != null) {
                                                    Glide.with(getContext()).load(documentSnapshot.get("img_url")).into(mImg2);
                                                    Glide.with(getContext()).load(documentSnapshot.get("img_url2")).into(mImg);
                                                    Map<String, Object> map = new HashMap<>();
                                                    map.put("img_url", documentSnapshot.get("img_url2"));
                                                    map.put("img_url2", documentSnapshot.get("img_url"));
                                                    mStore.collection("Users").document(uid).set(map, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            System.out.println("入れ替えました");
                                                            replaceImageForChat(uid, (String) documentSnapshot.get("img_url2"));
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    } else if (position == 1) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setMessage(R.string.delete_image)
                                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        System.out.println("はい");
                                                        DocumentReference docRef = mStore.collection("Users").document(uid);
                                                        Map<String, Object> updates = new HashMap<>();
                                                        updates.put("img_url2", FieldValue.delete());

                                                        docRef.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                mStore.collection("Users").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                        if (documentSnapshot.get("img_url3") != null) {
                                                                            Glide.with(getContext()).load(documentSnapshot.get("img_url3")).into(mImg2);
                                                                            mImg3.setImageDrawable(getResources().getDrawable(R.drawable.avatornew));
                                                                            Map<String, Object> map = new HashMap<>();
                                                                            map.put("img_url2", documentSnapshot.get("img_url3"));
                                                                            map.put("img_url3", FieldValue.delete());
                                                                            mStore.collection("Users").document(uid).set(map, SetOptions.merge())
                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {
                                                                                            System.out.println("削除完了");
                                                                                        }
                                                                                    });
                                                                        } else {
                                                                            mImg2.setImageDrawable(getResources().getDrawable(R.drawable.avatornew));
                                                                            System.out.println("削除完了");
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                })
                                                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                    }
                                                });
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                });
                return true;
            }
        });

        mImg3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ImageActivity.class);
                startActivityForResult(intent, REQUEST_CODE3);
            }
        });
        mImg3.setLongClickable(true);
        mImg3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mStore.collection("Users").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.get("img_url3") != null) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setItems(getResources().getStringArray(R.array.image_select), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int position) {
                                    if (position == 0) {
                                        mStore.collection("Users").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot.get("img_url") != null) {
                                                    Glide.with(getContext()).load(documentSnapshot.get("img_url")).into(mImg3);
                                                    Glide.with(getContext()).load(documentSnapshot.get("img_url3")).into(mImg);
                                                    Map<String, Object> map = new HashMap<>();
                                                    map.put("img_url", documentSnapshot.get("img_url3"));
                                                    map.put("img_url3", documentSnapshot.get("img_url"));
                                                    mStore.collection("Users").document(uid).set(map, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            System.out.println("入れ替えました");
                                                            replaceImageForChat(uid, (String) documentSnapshot.get("img_url3"));
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    } else if (position == 1) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setMessage(R.string.delete_image)
                                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        System.out.println("はい");
                                                        DocumentReference docRef = mStore.collection("Users").document(uid);
                                                        Map<String, Object> updates = new HashMap<>();
                                                        updates.put("img_url3", FieldValue.delete());

                                                        docRef.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                mStore.collection("Users").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                        mImg3.setImageDrawable(getResources().getDrawable(R.drawable.avatornew));
                                                                        System.out.println("削除完了");
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                })
                                                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                    }
                                                });
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                });
                return true;
            }
        });

        mDesc.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mDesc.setRawInputType(InputType.TYPE_CLASS_TEXT);

        view.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
//                LogoutProgressButton button = new LogoutProgressButton(view);
//                button.buttonActivated();
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

                Toast.makeText(getContext(), getString(R.string.logout_done), Toast.LENGTH_SHORT).show();
//                button.buttonFinished();
            }
        });

        mSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SaveProgressButton save = new SaveProgressButton(view, false);
                save.buttonActivated();
                Map<String, Object> map = new HashMap<>();
                map.put("name", mName.getText().toString());
                map.put("address", mAddress.getText().toString());
                map.put("job", mJob.getText().toString());
                map.put("hobby", joinString(mHobbyList));
                map.put("lang", joinString(mLangList));
                map.put("desc", mDesc.getText().toString());
                saveUserData(map, save);
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
                    String address = task.getResult().getString("address");
                    String job = task.getResult().getString("job");
                    String desc = task.getResult().getString("desc");
                    String hobby = task.getResult().getString("hobby");
                    String img_url = task.getResult().getString("img_url");
                    String img_url2 = task.getResult().getString("img_url2");
                    String img_url3 = task.getResult().getString("img_url3");
                    String lang = task.getResult().getString("lang");
                    mName.setText(name);
                    mAddress.setText(address);
                    mJob.setText(job);
                    mDesc.setText(desc);
                    mHobbyList = hobby == null ? mHobbyList : new HashSet<>(Arrays.asList(hobby.split("\\s*,\\s*")));
                    mLangList = lang == null ? mLangList : new HashSet<>(Arrays.asList(lang.split("\\s*,\\s*")));
                    if (img_url != null) {
                        Glide.with(getContext()).load(img_url).into(mImg);
                    }
                    if (img_url2 != null) {
                        Glide.with(getContext()).load(img_url2).into(mImg2);
                    }
                    if (img_url3 != null) {
                        Glide.with(getContext()).load(img_url3).into(mImg3);
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
            if (requestCode == REQUEST_CODE1) {
                if (data != null) {
                    final Uri imageUri = Uri.parse(data.getStringExtra("image_uri"));
                    if (imageUri != null) {
                        Glide.with(getContext()).load(imageUri).into(mImg);
                        saveImage("img_url", imageUri, uid, true);
                    }
                }
            } else if (requestCode == REQUEST_CODE2) {
                if (data != null) {
                    final Uri imageUri = Uri.parse(data.getStringExtra("image_uri"));
                    if (imageUri != null) {
                        mStore.collection("Users").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.get("img_url") == null) {
                                    Glide.with(getContext()).load(imageUri).into(mImg);
                                    saveImage("img_url", imageUri, uid, true);
                                } else {
                                    Glide.with(getContext()).load(imageUri).into(mImg2);
                                    saveImage("img_url2", imageUri, uid, false);
                                }
                            }
                        });
                    }

                }
            } else if (requestCode == REQUEST_CODE3) {
                if (data != null) {
                    final Uri imageUri = Uri.parse(data.getStringExtra("image_uri"));
                    if (imageUri != null) {
                        mStore.collection("Users").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.get("img_url") == null) {
                                    Glide.with(getContext()).load(imageUri).into(mImg);
                                    saveImage("img_url", imageUri, uid, true);
                                } else if (documentSnapshot.get("img_url2") == null) {
                                    Glide.with(getContext()).load(imageUri).into(mImg2);
                                    saveImage("img_url2", imageUri, uid, false);
                                } else {
                                    Glide.with(getContext()).load(imageUri).into(mImg3);
                                    saveImage("img_url3", imageUri, uid, false);
                                }
                            }
                        });
                    }

                }
            }
        }else {
            Toast.makeText(getContext(), getString(R.string.no_image_selected), Toast.LENGTH_SHORT).show();
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

    private void replaceImageForChat(String uid, String stringUri) {
        DatabaseReference profileRef = FirebaseDatabase.getInstance().getReference("/ProfileImage/" + uid);
        Map<String, Object> map = new HashMap<>();
        map.put("img_url", stringUri);
        map.put("time_stamp", System.currentTimeMillis());
        profileRef.setValue(map);
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
        mStore.collection("Users").document(uid)
                .set(map, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getContext(), getString(R.string.complete_save), Toast.LENGTH_SHORT).show();
                        save.buttonFinished();
                    }
                });
    }

    private void saveImage(String key, Uri value, String uid, boolean isMainChanged) {
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        mStorage.child(ts + "/").putFile(value).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> res = taskSnapshot.getStorage().getDownloadUrl();
                res.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadUrl = uri.toString();
                        Map<String, String> map = new HashMap<>();
                        map.put(key, downloadUrl);
                        mStore.collection("Users").document(uid)
                                .set(map, SetOptions.merge())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getContext(), getString(R.string.save_image), Toast.LENGTH_SHORT).show();
                                            if (isMainChanged) {
                                                replaceImageForChat(uid, downloadUrl);
                                            }
                                        } else {
                                            PlainDialog dialog = new PlainDialog(getString(R.string.not_save_image));
                                            assert getFragmentManager() != null;
                                            dialog.show(getFragmentManager(),"Image not saved.");
                                        }
                                    }
                                });
                    }
                });
            }
        });
    }
}