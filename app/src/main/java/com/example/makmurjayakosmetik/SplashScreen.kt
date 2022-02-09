package com.example.makmurjayakosmetik

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val db = DBHelper(this)
        db.getAllLog()
        findViewById<CoordinatorLayout>(R.id.splashScreenLayout).animate().setDuration(200).alpha(1f).withEndAction {
            if (AccountSharedPreferences(this).username != "Kosong")
                startActivity(Intent(this, MainActivity::class.java))
            else
                startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}