package jp.gr.java_conf.datingapp.utility;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MatchHandler {
    private static FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static String current_user;
    private static String receiver_name;
    private static String current_image;
    private static String receiver_image;

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
                                current_image = task.getResult().getString("img_url");
                                mStore.collection("Users").document(docId).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    if (task.getResult() != null && task.getResult().getData() != null) {
                                                        receiver_name = task.getResult().getString("name");
                                                        receiver_image = task.getResult().getString("img_url");
                                                        map.put("name", receiver_name);
                                                        map.put("img_url", receiver_image);
                                                        map.put("time_stamp", new Date().getTime());
                                                        map.put("isBlock", false);
                                                        mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                                                                .collection("Match").document(docId).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                map.put("user_id", mAuth.getCurrentUser().getUid());
                                                                map.put("name", current_user);
                                                                map.put("img_url", current_image);
                                                                map.put("time_stamp", new Date().getTime());
                                                                map.put("isBlock", false);
                                                                if (task.isSuccessful()) {
                                                                    mStore.collection("Users").document(docId)
                                                                            .collection("Match").document(mAuth.getCurrentUser().getUid()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                System.out.println("マッチ");
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
