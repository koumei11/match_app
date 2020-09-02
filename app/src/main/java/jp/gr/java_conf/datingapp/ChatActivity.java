package jp.gr.java_conf.datingapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

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
    String toId = "";
    String imgUrl = "";
    String userName = "";
    String myImg = "";
    private ChatRecyclerAdapter mChatRecyclerAdapter;
    List<Chat> mChatList;
    View view;
    TextView newestMessage;
    Profile userProfile;

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
        mChatList = new ArrayList<>();
        mChatRecyclerView.setHasFixedSize(true);
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        toId = getIntent().getStringExtra("doc_id");
        imgUrl = getIntent().getStringExtra("user_img");
        userName = getIntent().getStringExtra("user_name");
        userProfile = (Profile) getIntent().getSerializableExtra("profile");
        mChatRecyclerAdapter = new ChatRecyclerAdapter(this, mChatList);

        LayoutInflater inflater = LayoutInflater.from(this);
        View matchView = inflater.inflate(R.layout.single_match_user_item, null);
        newestMessage = matchView.findViewById(R.id.newest_message);

        mChatRecyclerView.setAdapter(mChatRecyclerAdapter);
        mChatRecyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Click");
            }
        });

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mChatRecyclerView.setLayoutManager(linearLayoutManager);

        mStore.collection("Users").document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Profile profile = task.getResult().toObject(Profile.class);
                    myImg = profile.getImg_url();
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
                mStore.collection("Users").document(toId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Profile profile = task.getResult().toObject(Profile.class);
                            Intent intent = new Intent(ChatActivity.this, UserDetailActivity.class);
                            intent.putExtra("profile", profile);
                            intent.putExtra("match_flg", true);
                            startActivity(intent);
                        }
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

        mStore.collection("Message").orderBy("time_stamp", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                    DocumentSnapshot snapshot = doc.getDocument();
                    Chat chat = snapshot.toObject(Chat.class);
                    if (snapshot.getData() != null) {
                        chat.setFirstMessageOfTheDay((boolean) snapshot.getData().get("isFirstMessageOfTheDay"));
                    }
                    if ((chat.getFrom().equals(mAuth.getCurrentUser().getUid()) || chat.getFrom().equals(toId)) &&
                            (chat.getTo().equals(mAuth.getCurrentUser().getUid()) || chat.getTo().equals(toId))) {
                        chat.setProfile(userProfile);
                        mChatList.add(chat);
                        newestMessage.setText(chat.getMessage());
                        mChatRecyclerAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mChatText.getText().toString().isEmpty()) {

                    Map<String, Object> map = new HashMap<>();
                    map.put("message", mChatText.getText().toString());
                    map.put("my_img", myImg);
                    map.put("from", mAuth.getCurrentUser().getUid());
                    map.put("to", toId);
                    map.put("time_stamp", new Date());
                    if (mChatList.size() > 0) {
                        java.sql.Date date1 = new java.sql.Date(mChatList.get(mChatList.size() - 1).getTime_stamp().toDate().getTime());
                        java.sql.Date date2 = new java.sql.Date(((Date) map.get("time_stamp")).getTime());
                        if (date2.toString().equals(date1.toString())) {
                            map.put("isFirstMessageOfTheDay", false);
                        } else {
                            map.put("isFirstMessageOfTheDay", true);
                        }
                    } else {
                        map.put("isFirstMessageOfTheDay", true);
                    }
                    mStore.collection("Message").add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                mChatText.setText("");
                                mChatRecyclerView.smoothScrollToPosition(mChatRecyclerAdapter.getItemCount());
                            }
                        }
                    });
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
}
