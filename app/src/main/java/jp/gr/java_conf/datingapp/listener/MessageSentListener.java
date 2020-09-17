package jp.gr.java_conf.datingapp.listener;

import com.google.firebase.database.DataSnapshot;

public interface MessageSentListener {
    void onMessageSent(DataSnapshot snapshot);
}
