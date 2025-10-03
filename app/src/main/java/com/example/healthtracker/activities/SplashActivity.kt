package com.example.healthtracker.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.healthtracker.R
import com.example.healthtracker.utils.HealthPreferenceManager

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY: Long = 2500
    private lateinit var preferenceManager: HealthPreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        preferenceManager = HealthPreferenceManager.Companion.getInstance(this)
        supportActionBar?.hide()

        Handler(Looper.getMainLooper()).postDelayed({
            navigateHandler()
        }, SPLASH_DELAY)
    }

    private fun navigateHandler() {
        val intent = when {
            preferenceManager.isFirstLaunch() -> {
                Intent(this, OnboardingActivity::class.java)
            }
            !preferenceManager.isUserLoggedIn() -> {
                Intent(this, LoginActivity::class.java)
            }
            else -> {
                Intent(this, MainActivity::class.java)
            }
        }

        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}