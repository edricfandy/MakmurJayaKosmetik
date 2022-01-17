package com.example.makmurjayakosmetik

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.makmurjayakosmetik.fragments.ChooseAccountFragment
import com.example.makmurjayakosmetik.fragments.LoginFragment

class LoginActivity : AppCompatActivity() {
    private lateinit var db: DBHelper

    private val loginBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "CHOOSE_ACCOUNT") {
                val username = intent.extras?.getString("CHOOSED_ACCOUNT_USERNAME")
                if (username != null) {
                    val account = db.getAccountByUsername(username)
                    if (account?.hasPassword == false) {
                        AccountSharedPreferences(this@LoginActivity).username = account.username
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        return
                    }
                    replaceFragment(LoginFragment(username), "LOGIN")
                }
            }
            else if (intent?.action == "BACK_TO_CHOOSE_ACCOUNT")
                replaceFragment(ChooseAccountFragment(), "CHOOSE_ACCOUNT")
            else if (intent?.action == "LOGIN_SUCCESSFUL") {
                val intentLogin = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intentLogin)
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = DBHelper(this)

        val intentFilter = IntentFilter()
        intentFilter.addAction("CHOOSE_ACCOUNT")
        intentFilter.addAction("BACK_TO_CHOOSE_ACCOUNT")
        intentFilter.addAction("LOGIN_SUCCESSFUL")
        registerReceiver(loginBroadcastReceiver, intentFilter)

        val db = DBHelper(this)
        db.checkAdminAccount()

        replaceFragment(ChooseAccountFragment(), "CHOOSE_ACCOUNT")
    }

    private fun replaceFragment(fragment: Fragment, fragtag: String) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.login_fragmentContainer, fragment, fragtag)
            commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(loginBroadcastReceiver)
    }
}