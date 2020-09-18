package jp.gr.java_conf.datingapp.utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.dialog.MatchDialog;
import jp.gr.java_conf.datingapp.model.Profile;

public class MatchHandler {
    private static FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Match");
    private static String current_user;
    private static String receiver_name;
    private static Profile profile;
    private static String myImg;
    private static String sex;

    public static void storeMatchInDatabase(final String docId, final String name, final Context context) {
        final Map<String, Object> map = new HashMap<>();
        map.put("user_id", docId);
        mStore.collection("Users").document(mAuth.getCurrentUser().getUid()).get()
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
                                                        receiver_name = task.getResult().getString("name");
                                                        map.put("name", receiver_name);
                                                        map.put("time_stamp", new Date().getTime());
                                                        map.put("isBlock", false);
                                                        mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                                                                .collection("Match").document(docId).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                map.put("user_id", mAuth.getCurrentUser().getUid());
                                                                map.put("name", current_user);
                                                                map.put("time_stamp", new Date().getTime());
                                                                map.put("isBlock", false);
                                                                if (task.isSuccessful()) {
                                                                    mStore.collection("Users").document(docId)
                                                                            .collection("Match").document(mAuth.getCurrentUser().getUid()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                System.out.println("マッチ");
                                                                                MatchDialog.showDialog(context, profile, myImg, sex);
                                                                                Map<String, Object> map = new HashMap<>();
                                                                                map.put("user1", docId);
                                                                                map.put("user2", mAuth.getCurrentUser().getUid());
                                                                                map.put("time_stamp", System.currentTimeMillis());
                                                                                reference.push().setValue(map);
                                                                            } else {
                                                                                System.out.println("マッチできませんでした。");
                                                                            }
                                                                        }
                                                                    });
                                                                }
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
}
