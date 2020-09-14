package jp.gr.java_conf.datingapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.gr.java_conf.datingapp.adapter.ChatRecyclerAdapter;
import jp.gr.java_conf.datingapp.model.Chat;
import jp.gr.java_conf.datingapp.model.Profile;
import jp.gr.java_conf.datingapp.utility.WindowSizeGetter;

public class ChatActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 100;
    private static final int REQUEST_CODE2 = 200;
    private RecyclerView mChatRecyclerView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private Toolbar mToolbar;
    private CircleImageView mCircleImageView;
    private TextView mToolbarName;
    private ImageButton mShrink;
    private ImageButton mCamera;
    private ImageButton mLibrary;
    private EditText mChatText;
    private ImageView mSend;
    private DatabaseReference reference;
    private StorageReference mStorage;
    String toId = "";
    String imgUrl = "";
    String userName = "";
    String myImg = "";
    private ChatRecyclerAdapter mChatRecyclerAdapter;
    private ValueEventListener seenListener;
    List<Chat> mChatList;
    View view;
    Profile userProfile, myProfile;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getWindow().setStatusBarColor(getResources().getColor(R.color.colorGrey));

        mToolbar = findViewById(R.id.tool_bar);

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        Point windowSize = WindowSizeGetter.getDisplaySize(this.getWindowManager());

        mCircleImageView = findViewById(R.id.circle_image_chat);
        mToolbarName = findViewById(R.id.toolbar_name_chat);

        mChatRecyclerView = findViewById(R.id.chat_recycler);
        mChatText = findViewById(R.id.chat_msg);
        mShrink = findViewById(R.id.shrink);
        mCamera = findViewById(R.id.imageCamera);
        mLibrary = findViewById(R.id.imageLibrary);
        mSend = findViewById(R.id.chat_btn);
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        reference = FirebaseDatabase.getInstance().getReference();
        toId = getIntent().getStringExtra("doc_id");
        imgUrl = getIntent().getStringExtra("user_img");
        userName = getIntent().getStringExtra("user_name");
        userProfile = (Profile) getIntent().getSerializableExtra("profile");
        mChatList = new ArrayList<>();
        mChatRecyclerAdapter = new ChatRecyclerAdapter(ChatActivity.this, mChatList);

        mCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });

        mLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLibrary();
            }
        });

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mChatRecyclerView.setLayoutManager(linearLayoutManager);

        mStore.collection("Users").document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    myProfile = task.getResult().toObject(Profile.class);
                    myImg = myProfile.getImg_url();
                }
            }
        });

        if (imgUrl != null && !imgUrl.equals("")) {
            Uri url = Uri.parse(imgUrl);
            Glide.with(this).load(url).into(mCircleImageView);
        }

        if (userName != null && !userName.equals("")) {
            mToolbarName.setText(userName);
        }

        mCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setEnabled(false);
                mStore.collection("Users").document(toId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Profile profile = task.getResult().toObject(Profile.class);
                            Intent intent = new Intent(ChatActivity.this, UserDetailActivity.class);
                            intent.putExtra("profile", profile);
                            intent.putExtra("user_id", profile.getUser_id());
                            intent.putExtra("match_flg", true);
                            startActivity(intent);
                        }
                        view.setEnabled(true);
                    }
                });
            }
        });

        mChatText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (MotionEvent.ACTION_UP == motionEvent.getAction()) {
                    mCamera.setVisibility(View.GONE);
                    mLibrary.setVisibility(View.GONE);
                    mShrink.setVisibility(View.VISIBLE);
                    ViewGroup.LayoutParams layoutParams = mChatText.getLayoutParams();
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    mChatText.setLayoutParams(layoutParams);
                }
                return false;
             }
        });

        mChatText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mCamera.setVisibility(View.GONE);
                mLibrary.setVisibility(View.GONE);
                mShrink.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mShrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.setVisibility(View.VISIBLE);
                mLibrary.setVisibility(View.VISIBLE);
                mShrink.setVisibility(View.GONE);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                seenMessage(toId);
                readMessage(toId, userProfile);
            }
        }).start();


        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mChatText.getText().toString().isEmpty()) {
                    sendMessage(mChatText.getText().toString(), myImg, mAuth.getCurrentUser().getUid(), toId, null);
                }
            }
        });
    }

    private void openLibrary() {
        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra("fromChatActivity", true);
        intent.putExtra("requestCode", REQUEST_CODE2);
        startActivityForResult(intent, REQUEST_CODE2);
    }

    private void takePhoto() {
        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra("fromChatActivity", true);
        intent.putExtra("requestCode", REQUEST_CODE);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (data != null) {
                final Uri imageUri = Uri.parse(data.getStringExtra("image_uri"));
                sendMessage(null, myImg, mAuth.getCurrentUser().getUid(), toId, imageUri);
            }
        } else if (requestCode == REQUEST_CODE2) {
            if (data != null) {
                final Uri imageUri = Uri.parse(data.getStringExtra("image_uri"));
                sendMessage(null, myImg, mAuth.getCurrentUser().getUid(), toId, imageUri);
            }
        }
    }

    private void seenMessage(final String userId) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getTo().equals(mAuth.getCurrentUser().getUid()) && chat.getFrom().equals(userId)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessage(String toId, Profile profile) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChatList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat != null) {
                        if ((chat.getFrom().equals(mAuth.getCurrentUser().getUid()) || chat.getFrom().equals(toId)) &&
                                (chat.getTo().equals(mAuth.getCurrentUser().getUid()) || chat.getTo().equals(toId))) {
                            // isFirstMessageOfTheDayとisSeenが上手くマッピングできないためここで直接代入
                            chat.setFirstMessageOfTheDay((boolean) snapshot.child("isFirstMessageOfTheDay").getValue());
                            chat.setSeen((boolean) snapshot.child("isSeen").getValue());
                            chat.setProfile(profile);
                            mChatList.add(chat);
                        }
                    }
                }
                mChatRecyclerView.setAdapter(mChatRecyclerAdapter);
                mChatRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message, String image, String myId, String toId, Uri imageUri) {
        mChatText.setText("");
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("from", myId);
        notificationMap.put("name", myProfile.getName());
        if (message != null) {
            notificationMap.put("message", message);
        } else if (imageUri != null) {
            notificationMap.put("url", imageUri);
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Notification").child(toId).push().setValue(notificationMap);

        if (imageUri != null) {
            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();
            mStorage.child(ts + "/").putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> res = taskSnapshot.getStorage().getDownloadUrl();
                    res.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUri = uri.toString();
                            map.put("img_uri", downloadUri);
                            map.put("message", null);
                            map.put("my_img", image);
                            map.put("from", myId);
                            map.put("to", toId);
                            map.put("isSeen", false);
                            map.put("time_stamp", new Date().getTime());
                            if (mChatList.size() > 0) {
                                java.sql.Date date1 = new java.sql.Date(mChatList.get(mChatList.size() - 1).getTime_stamp());
                                java.sql.Date date2 = new java.sql.Date((Long) map.get("time_stamp"));
                                if (date2.toString().equals(date1.toString())) {
                                    map.put("isFirstMessageOfTheDay", false);
                                } else {
                                    map.put("isFirstMessageOfTheDay", true);
                                }
                            } else {
                                map.put("isFirstMessageOfTheDay", true);
                            }
                            mChatRecyclerView.smoothScrollToPosition(mChatRecyclerAdapter.getItemCount());
//                            reference.child("Chats").push().setValue(map);

                            mStore.collection("Chats").document().set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    System.out.println("Added to Firestore");
                                }
                            });
                        }
                    });
                }
            });
        } else {
            map.put("img_uri", null);
            map.put("message", message);
            map.put("my_img", image);
            map.put("from", myId);
            map.put("to", toId);
            map.put("time_stamp", new Date().getTime());
            map.put("isSeen", false);
            if (mChatList.size() > 0) {
                java.sql.Date date1 = new java.sql.Date(mChatList.get(mChatList.size() - 1).getTime_stamp());
                java.sql.Date date2 = new java.sql.Date((Long) map.get("time_stamp"));
                if (date2.toString().equals(date1.toString())) {
                    map.put("isFirstMessageOfTheDay", false);
                } else {
                    map.put("isFirstMessageOfTheDay", true);
                }
            } else {
                map.put("isFirstMessageOfTheDay", true);
            }
            mChatRecyclerView.smoothScrollToPosition(mChatRecyclerAdapter.getItemCount());
//            reference.child("Chats").push().setValue(map);

            mStore.collection("Chats").document().set(map);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        reference.removeEventListener(seenListener);
    }
}
