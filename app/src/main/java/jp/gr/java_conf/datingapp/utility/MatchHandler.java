package jp.gr.java_conf.datingapp.utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jp.gr.java_conf.datingapp.ChatActivity;
import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.dialog.MatchDialog;
import jp.gr.java_conf.datingapp.enums.NotificationType;
import jp.gr.java_conf.datingapp.model.Profile;
import jp.gr.java_conf.datingapp.notification.APIService;
import jp.gr.java_conf.datingapp.notification.Client;
import jp.gr.java_conf.datingapp.notification.Data;
import jp.gr.java_conf.datingapp.notification.MyResponse;
import jp.gr.java_conf.datingapp.notification.Sender;
import jp.gr.java_conf.datingapp.notification.Token;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchHandler {
    private static FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private static DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    private static APIService apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
    private static String current_user;
    private static Profile profile;
    private static String myImg;
    private static String sex;

    public static void storeMatchInDatabase(final String uid, final String docId, final String name, final Context context) {
        mStore.collection("Users").document(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult() != null && task.getResult().getData() != null) {
                                current_user = task.getResult().getString("name");
                                myImg = task.getResult().getString("img_url");
                                sex = task.getResult().getString("sex");
                                mStore.collection("Users").document(docId).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    if (task.getResult() != null && task.getResult().getData() != null) {
                                                        profile = task.getResult().toObject(Profile.class);
                                                        MatchDialog.showDialog(context, profile, myImg, sex);
                                                        Map<String, Object> map = new HashMap<>();
                                                        map.put("user1", docId);
                                                        map.put("user2", uid);
                                                        map.put("block", false);
                                                        map.put("time_stamp", System.currentTimeMillis());
                                                        reference.child("Match").child(uid).child(docId).setValue(map);
                                                        reference.child("Match").child(docId).child(uid).setValue(map);
                                                        reference.child("Switch").child(docId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                if (snapshot.child("on").getValue() != null && (boolean)snapshot.child("on").getValue()) {
                                                                    sendNotification(uid, docId, current_user);
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private static void sendNotification(String uid, String receiver, String userName) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Token");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshots) {
                for (DataSnapshot snapshot : snapshots.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(uid, R.drawable.heart1, NotificationType.MATCH, userName + "さんとマッチングしました！", "新しいマッチ", receiver, null);
                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(@NotNull Call<MyResponse> call, @NotNull Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        assert response.body() != null;
                                        if (response.body().success != 1) {
                                            System.out.println("失敗");
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
