package jp.gr.java_conf.datingapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import jp.gr.java_conf.datingapp.adapter.HomeViewPagerAdapter
import jp.gr.java_conf.datingapp.fragment.ChatFragment
import jp.gr.java_conf.datingapp.fragment.ChatFragment.MessageListener
import jp.gr.java_conf.datingapp.fragment.DiscoverFragment
import jp.gr.java_conf.datingapp.fragment.ProfileFragment
import jp.gr.java_conf.datingapp.notification.APIService
import jp.gr.java_conf.datingapp.notification.Client
import jp.gr.java_conf.datingapp.notification.OreoNotification
import jp.gr.java_conf.datingapp.utility.CloseKeyboard
import java.util.*

class HomeActivity : AppCompatActivity(), MessageListener {
    private var mViewPager: ViewPager? = null
    private var mHomeTabs: TabLayout? = null
    private var adapter: HomeViewPagerAdapter? = null
    private var mAuth: FirebaseAuth? = null
    private var uid: String? = null
    private var database: FirebaseDatabase? = null
    private var badge: TextView? = null
    private var mStore: FirebaseFirestore? = null
    private val badgeListener: ValueEventListener? = null
    private var preferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private val notificationListener: ChildEventListener? = null
    private var context: Context? = null
    private var apiService: APIService? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        context = this
        mViewPager = findViewById(R.id.home_view_pager)
        mHomeTabs = findViewById(R.id.home_tab)
        if (savedInstanceState == null) {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.commit()
        }

//        FirebaseAuth.getInstance().signOut();
        CloseKeyboard.setupUI(findViewById(R.id.constraint_home), this)
        val oreoNotification = OreoNotification(this)
        oreoNotification.createNotificationChannel(this)
        mAuth = FirebaseAuth.getInstance()
        uid = mAuth!!.currentUser!!.uid
        println("ユーザーID")
        println(uid)
        mStore = FirebaseFirestore.getInstance()
        database = FirebaseDatabase.getInstance()
        preferences = getSharedPreferences("DATA", MODE_PRIVATE)
        editor = preferences!!.edit()
        editor!!.clear().apply()
        val myRef = database!!.getReference("/status/$uid")
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService::class.java)
        val isOfflineForDatabase: MutableMap<String, Any> = HashMap()
        isOfflineForDatabase["state"] = "offline"
        isOfflineForDatabase["last_changed"] = ServerValue.TIMESTAMP
        val isOnlineForDatabase: MutableMap<String, Any> = HashMap()
        isOnlineForDatabase["state"] = "online"
        isOnlineForDatabase["last_changed"] = ServerValue.TIMESTAMP
        database!!.getReference(".info/connected").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                myRef.onDisconnect().setValue(isOfflineForDatabase).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        myRef.setValue(isOnlineForDatabase)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        if (mAuth!!.currentUser != null) {
            adapter = HomeViewPagerAdapter(supportFragmentManager)
            adapter!!.addFragment(ProfileFragment(), "マイプロフィール")
            adapter!!.addFragment(DiscoverFragment(), "さがす")
            adapter!!.addFragment(ChatFragment(), "チャット")
            mViewPager!!.setAdapter(adapter)
            mHomeTabs!!.setupWithViewPager(mViewPager)
            mHomeTabs!!.getTabAt(0)!!.setIcon(R.drawable.profile)
            mHomeTabs!!.getTabAt(1)!!.setIcon(R.drawable.discover)
            mHomeTabs!!.getTabAt(2)!!.setCustomView(R.layout.notification_badge)
            badge = mHomeTabs!!.getTabAt(2)!!.customView!!.findViewById(R.id.notification_text)
            mViewPager!!.setOffscreenPageLimit(2)
            mHomeTabs!!.selectTab(mHomeTabs!!.getTabAt(1))
            val map: MutableMap<String, Any> = HashMap()
            map["on"] = true
            database!!.getReference("Switch").child(uid!!).setValue(map)
            database!!.getReference("Switch").child(uid!!).child("on").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value != null) {
                        editor!!.putBoolean("switchOn", snapshot.value as Boolean)
                        editor!!.apply()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
            if (intent != null) {
                if (intent.getIntExtra("tabPos", -1) >= 0) {
                    mViewPager!!.setCurrentItem(intent.getIntExtra("tabPos", -1))
                }
            }
            mHomeTabs!!.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    if (tab.position == 2) {
                        if (preferences!!.getInt("new_match", 0) > 0) {
                            editor!!.putInt("new_match", preferences!!.getInt("new_match", 0) - 1)
                            editor!!.apply()
                            badge!!.setVisibility(View.GONE)
                        }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            101 -> {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        val isAccept = data.getBooleanExtra("result", false)
                        if (isAccept) {
                            DiscoverFragment.accept()
                        } else {
                            DiscoverFragment.reject()
                        }
                    }
                }
            }
            202 -> mViewPager!!.currentItem = 2
        }
    }

    override fun onMessageReceived() {
        badge!!.visibility = View.VISIBLE
    }

    override fun onAllMessageSeen() {
        badge!!.visibility = View.GONE
    }

    override fun onMatchCreated() {
        badge!!.visibility = View.VISIBLE
    }

    public override fun onResume() {
        super.onResume()
    }

    public override fun onPause() {
        super.onPause()
    }
}