package jp.gr.java_conf.datingapp

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class EmailSendActivity : AppCompatActivity() {
    private var passCodeText: TextView? = null
    private var passCode: String? = null
    private var preferences: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_send)
        preferences = getSharedPreferences("DATA", MODE_PRIVATE)
        passCodeText = findViewById(R.id.pass_code)
        passCode = preferences!!.getString("pass_code", null)
        if (passCode != null) {
            passCodeText!!.setText(passCode)
        }
    }
}