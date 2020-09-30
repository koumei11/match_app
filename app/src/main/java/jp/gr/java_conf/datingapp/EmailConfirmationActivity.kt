package jp.gr.java_conf.datingapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import jp.gr.java_conf.datingapp.progressbar.SignInProgressButton
import jp.gr.java_conf.datingapp.utility.CloseKeyboard

class EmailConfirmationActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var mEmail: TextView? = null
    private var mPassCode: TextView? = null
    private var mSend: CardView? = null
    private var preferences: SharedPreferences? = null
    private var mContext: Context? = null
    private var mStore: FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_email)
        CloseKeyboard.setupUI(findViewById(R.id.constraint_email_conf), this)
        mAuth = FirebaseAuth.getInstance()
        mEmail = findViewById(R.id.signin_email)
        mPassCode = findViewById(R.id.signin_pass_code)
        mSend = findViewById(R.id.signin_card_view)
        preferences = getSharedPreferences("DATA", MODE_PRIVATE)
        mContext = this
        mStore = FirebaseFirestore.getInstance()
        mSend!!.setOnClickListener(View.OnClickListener { view ->
            val email = mEmail!!.getText().toString()
            val inputPassCode = mPassCode!!.getText().toString()
            val button = SignInProgressButton(view, true)
            button.buttonActivated()
            if (email == preferences!!.getString("email_address", null) && inputPassCode == preferences!!.getString("pass_code", null)) {
                var emailLink = ""
                if (intent.data != null) {
                    emailLink = intent.data.toString()
                }
                if (mAuth!!.isSignInWithEmailLink(emailLink)) {
                    mAuth!!.signInWithEmailLink(email, emailLink)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("HomeActivity", "メールリンクでのサインイン完了！")
                                    val result = task.result
                                    if (result != null && result.additionalUserInfo != null && result.user != null) {
                                        mStore!!.collection("Users").document(result.user!!.uid)
                                                .get().addOnCompleteListener { task ->
                                                    if (result.additionalUserInfo!!.isNewUser || task.result!!.getString("sex") == null) {
                                                        println("新しいユーザーなのでProfileSettingsActivityに遷移します")
                                                        val intent = Intent(this@EmailConfirmationActivity, ProfileSettingsActivity::class.java)
                                                        startActivity(intent)
                                                        Toast.makeText(mContext, getString(R.string.welcome), Toast.LENGTH_LONG).show()
                                                        finish()
                                                    } else {
                                                        println("既存ユーザーのためホームに遷移")
                                                        val intent = Intent(this@EmailConfirmationActivity, HomeActivity::class.java)
                                                        startActivity(intent)
                                                        Toast.makeText(mContext, getString(R.string.welcome_back), Toast.LENGTH_LONG).show()
                                                        finish()
                                                    }
                                                }
                                    }
                                    button.buttonFinished()
                                }
                            }
                }
            } else {
                Toast.makeText(mContext, getString(R.string.invalid_auth), Toast.LENGTH_SHORT).show()
                button.buttonFinished()
            }
        })
    }
}