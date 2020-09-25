package jp.gr.java_conf.datingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class EmailSendActivity extends AppCompatActivity {

    private TextView passCodeText;
    private String passCode;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_send);

        preferences = getSharedPreferences("DATA", MODE_PRIVATE);
        passCodeText = findViewById(R.id.pass_code);
        passCode = preferences.getString("pass_code", null);

        if (passCode != null) {
            passCodeText.setText(passCode);
        }

    }
}
