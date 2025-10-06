package com.example.healthtracker.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.healthtracker.R
import com.example.healthtracker.utils.HealthPreferenceManager
import com.google.android.material.button.MaterialButton
import kotlin.math.log

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var passwordStatusText: TextView
    private lateinit var registerBtn: MaterialButton
    private lateinit var loginBtn: TextView
    private lateinit var sharedPreferences: HealthPreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        sharedPreferences = HealthPreferenceManager.getInstance(this)

        nameEditText = findViewById(R.id.name)
        usernameEditText = findViewById(R.id.username)
        passwordEditText = findViewById(R.id.password)
        confirmPasswordEditText = findViewById(R.id.confirm_password)
        passwordStatusText = findViewById(R.id.password_match_status)
        registerBtn = findViewById(R.id.btn_register)
        loginBtn = findViewById(R.id.btn_login)

        confirmPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = passwordEditText.text.toString()
                val confirm = s.toString()

                if (confirm.isEmpty()) {
                    passwordStatusText.text = ""
                } else if (confirm == password) {
                    passwordStatusText.setTextColor(getColor(R.color.success_color))
                    passwordStatusText.text = "Passwords match"
                } else {
                    passwordStatusText.setTextColor(getColor(R.color.error_color))
                    passwordStatusText.text = "Passwords do not match"
                }
            }
        })

        registerBtn.setOnClickListener { saveUserData() }

        loginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveUserData() {
        val name = nameEditText.text.toString()
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        if (name.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val userProfile = HealthPreferenceManager.UserProfile(
            name,
            username,
            password
        )

        sharedPreferences.saveUserProfile(userProfile)
        sharedPreferences.setFirstLoginDate()
        sharedPreferences.setUserLoggedIn(true)

        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
