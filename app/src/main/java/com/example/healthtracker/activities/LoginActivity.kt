package com.example.healthtracker.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.healthtracker.R
import com.example.healthtracker.utils.HealthPreferenceManager

class LoginActivity : AppCompatActivity() {

    private lateinit var tvLoginData: TextView
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginBtn: Button
    private lateinit var tvSignUp: TextView
    private lateinit var tvErrorMessage: TextView
    private lateinit var sharedPreferenceManager: HealthPreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        email = findViewById(R.id.emailEditText)
        password = findViewById(R.id.passwordEditText)
        loginBtn = findViewById(R.id.loginButton)
        tvSignUp = findViewById(R.id.signUpLink)
        tvErrorMessage = findViewById(R.id.errorMessage)
        sharedPreferenceManager = HealthPreferenceManager.getInstance(this)

        val user = sharedPreferenceManager.getUserProfile()

        tvLoginData = findViewById(R.id.login_data)
        tvLoginData.text = "Email: ${user?.email}, Password: ${user?.password}"

        loginBtn.setOnClickListener {
            validateLogin(email.text.toString(), password.text.toString())
        }

        tvSignUp.setOnClickListener {
            redirectToSignUp()
        }
    }

    private fun validateLogin(email: String, password: String) {
        val userProfile = sharedPreferenceManager.getUserProfile()

        if (email != userProfile?.email || password != userProfile.password) {
            tvErrorMessage.visibility = View.VISIBLE
        } else {
            tvErrorMessage.visibility = View.GONE
            sharedPreferenceManager.setUserLoggedIn(true)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun redirectToSignUp() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }
}