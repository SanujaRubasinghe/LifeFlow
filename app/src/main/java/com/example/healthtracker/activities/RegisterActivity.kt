package com.example.healthtracker.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.healthtracker.R
import com.example.healthtracker.utils.HealthPreferenceManager

class RegisterActivity : AppCompatActivity() {
    private lateinit var genderSpinner: Spinner
    private lateinit var nameEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerBtn: Button

    private lateinit var sharedPreferences: HealthPreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        sharedPreferences = HealthPreferenceManager.getInstance(this)

        genderSpinner = findViewById(R.id.spinner_gender)
        nameEditText = findViewById(R.id.name)
        ageEditText = findViewById(R.id.age)
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        registerBtn = findViewById(R.id.btn_register)

        ArrayAdapter.createFromResource(
            this,
            R.array.gender_options,
            android.R.layout.simple_spinner_item
        ).also {adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            genderSpinner.adapter = adapter
        }

        registerBtn.setOnClickListener {
            saveUserData()
        }
    }

    private fun saveUserData() {
        val name = nameEditText.text.toString()
        val age = ageEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val gender = genderSpinner.selectedItem.toString()

        if (name.isEmpty() || age.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userProfile = HealthPreferenceManager.UserProfile(
            name,
            age.toInt(),
            email,
            password,
            gender)

        sharedPreferences.saveUserProfile(userProfile)
        sharedPreferences.setFirstLoginDate()
        sharedPreferences.setUserLoggedIn(true)

        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}