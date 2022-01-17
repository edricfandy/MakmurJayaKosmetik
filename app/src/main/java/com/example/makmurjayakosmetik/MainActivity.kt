package com.example.makmurjayakosmetik

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.makmurjayakosmetik.classes.Account
import com.example.makmurjayakosmetik.dialogs.ManageAccountDialog
import com.example.makmurjayakosmetik.fragments.*
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var db : DBHelper
    private lateinit var drawer : DrawerLayout
    private lateinit var navigationView: NavigationView
    private var account : Account? = null

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "LOG_OUT") {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                AccountSharedPreferences(this@MainActivity).username = "Kosong"
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intentFilter = IntentFilter("LOG_OUT")
        registerReceiver(broadcastReceiver, intentFilter)

        db = DBHelper(this)
        account = db.getAccountByUsername(AccountSharedPreferences(this).username ?: "")

        drawer = findViewById(R.id.mainPage_drawer)
        navigationView = findViewById(R.id.mainPage_navigationView)

        val txtAvatar = findViewById<TextView>(R.id.mainPage_txtAvatar)
        txtAvatar.text = account?.username?.get(0).toString()
        txtAvatar.setOnClickListener {
            val dialog = ManageAccountDialog()
            val bundle = Bundle()
            bundle.putString("ACCOUNT_ID", account?.username)
            dialog.arguments = bundle
            dialog.show(supportFragmentManager.beginTransaction(), "ACCOUNT_INFO")
        }

        val toggle = ActionBarDrawerToggle(this, drawer, R.string.open_navigation_view, R.string.close_navigation_view)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val drawerOpener = findViewById<ImageButton>(R.id.mainPage_btnNavigationDrawer)
        drawerOpener.setOnClickListener {
            drawer.openDrawer(GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener {
            var fragment: Fragment? = null
            var fragTag = ""
            when (it.itemId) {
                R.id.menuDrawer_sales -> {
                    fragment = SalesFragment()
                    fragTag = "Manage Sales Fragment"
                }
                R.id.menuDrawer_purchase -> {
                    fragment = PurchaseFragment()
                    fragTag = "Manage Purchase Fragment"
                }
                R.id.menuDrawer_product -> {
                    fragment = ProductFragment()
                    fragTag = "Manage Product Fragment"
                }
                R.id.menuDrawer_category -> {
                    fragment = CategoryFragment()
                    fragTag = "Manage Category and Brand Fragment"
                }
                R.id.menuDrawer_store -> {
                    fragment = StoreFragment()
                    fragTag = "Manage Store Fragment"
                }
                R.id.menuDrawer_supplier -> {
                    fragment = SupplierFragment()
                    fragTag = "Manage Supplier Fragment"
                }
            }
            drawer.closeDrawer(GravityCompat.START)
            if (fragment != null && !fragment.isVisible())
                changeFragment(fragment, fragTag)
            true
        }
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

    private fun changeFragment(fragment: Fragment, fragTag: String) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.mainPage_fragmentContainer, fragment, fragTag)
            commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }
}