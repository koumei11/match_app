package jp.gr.java_conf.datingapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import jp.gr.java_conf.datingapp.adapter.ViewPagerAdapter
import jp.gr.java_conf.datingapp.dialog.PlainDialog
import jp.gr.java_conf.datingapp.fragment.SignInFragment
import jp.gr.java_conf.datingapp.utility.CloseKeyboard
import org.json.JSONException
import java.util.*

class MainActivity : AppCompatActivity() {
    private var mViewPager: ViewPager? = null
    private var adapter: ViewPagerAdapter? = null
    private var mAuth: FirebaseAuth? = null
    private var mStore: FirebaseFirestore? = null
    private var userRef: DatabaseReference? = null
    private var mCallbackManager: CallbackManager? = null
    private var mContext: Context? = null
    private var mLoginButton: LoginButton? = null
    private var privacy: TextView? = null
    var progressBar: ProgressBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = this
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        progressBar = findViewById(R.id.progressbar_main)
        privacy = findViewById(R.id.privacy_policy)
        mCallbackManager = CallbackManager.Factory.create()
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.fb_button, null)
        mLoginButton = view.findViewById(R.id.login_button)
        setAuthCallback()
        privacy!!.setOnClickListener(View.OnClickListener {
            val uri = Uri.parse(getString(R.string.policy_url))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        })
        mViewPager = findViewById(R.id.viewPager)
        //        mTabs = findViewById(R.id.tabLayout);
        mAuth = FirebaseAuth.getInstance()
        progressBar!!.setVisibility(ProgressBar.GONE)
        mViewPager!!.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        mStore = FirebaseFirestore.getInstance()
        CloseKeyboard.setupUI(findViewById(R.id.constraint_main), this)
    }

    private fun setAuthCallback() {
        mLoginButton!!.setPermissions("email", "public_profile", "user_friends")
        mLoginButton!!.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                firebaseAuthWithFacebook(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d("Cancel", "キャンセルされました")
            }

            override fun onError(error: FacebookException) {
                val dialog = PlainDialog(getString(R.string.error))
                dialog.show(supportFragmentManager, "Error occurred.")
            }
        })
    }

    fun firebaseAuthWithFacebook(accessToken: AccessToken) {
        val request = GraphRequest.newGraphPathRequest(
                accessToken,
                "/me"
        ) { response ->
            try {
                val id = response.jsonObject["id"] as String
                val graphPath = "/$id/friends"
                val request = GraphRequest.newGraphPathRequest(
                        accessToken,
                        graphPath
                ) { response -> // Insert your code here
                    try {
                        val numFriends = response.jsonObject.getJSONObject("summary")["total_count"] as Int
                        if (numFriends >= 10) {
                            val credential = FacebookAuthProvider.getCredential(accessToken.token)
                            mAuth!!.signInWithCredential(credential)
                                    .addOnCompleteListener(this@MainActivity) { task ->
                                        if (task.isSuccessful) {
                                            val info = Objects.requireNonNull(task.result)!!.additionalUserInfo
                                            if (info != null) {
                                                if (info.isNewUser) {
                                                    editProfile()
                                                } else {
                                                    changeActivity()
                                                }
                                            } else {
                                                Toast.makeText(mContext, getString(R.string.user_none), Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            Toast.makeText(mContext, getString(R.string.signin_fail), Toast.LENGTH_LONG).show()
                                        }
                                    }
                        } else {
                            val dialog = PlainDialog(getString(R.string.fb_fail))
                            dialog.show(supportFragmentManager, "Failed.")
                            LoginManager.getInstance().logOut()
                        }
                    } catch (e: JSONException) {
                        println(e)
                    }
                }
                request.executeAsync()
            } catch (e: JSONException) {
                println(e)
            }
        }
        request.executeAsync()
    }

    private fun editProfile() {
        val intent = Intent(this@MainActivity, ProfileSettingsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun changeActivity() {
        val intent = Intent(this@MainActivity, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mCallbackManager!!.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        if (mAuth!!.currentUser != null) {
            progressBar!!.visibility = ProgressBar.VISIBLE
            privacy!!.visibility = View.GONE
            mStore!!.collection("Users").document(mAuth!!.currentUser!!.uid)
                    .get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val sex = task.result!!.getString("sex")
                            if (sex != null && sex != "") {
                                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                                startActivity(intent)
                            } else {
                                val intent = Intent(this@MainActivity, ProfileSettingsActivity::class.java)
                                startActivity(intent)
                            }
                            progressBar!!.visibility = ProgressBar.INVISIBLE
                            privacy!!.visibility = View.VISIBLE
                            finish()
                        }
                    }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mViewPager!!.background = resources.getDrawable(R.drawable.white_bg_with, null)
            } else {
                mViewPager!!.background = resources.getDrawable(R.drawable.white_bg_with)
            }
            adapter = ViewPagerAdapter(supportFragmentManager)
            adapter!!.addFragment(SignInFragment(), "サインイン")
            mViewPager!!.adapter = adapter
            //            mTabs.setupWithViewPager(mViewPager);
        }
    }
}