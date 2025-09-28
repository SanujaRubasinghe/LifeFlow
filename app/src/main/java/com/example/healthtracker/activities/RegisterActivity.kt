package com.example.healthtracker.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.healthtracker.R

class RegisterActivity : AppCompatActivity() {
    private lateinit var genderSpinner: Spinner
    private lateinit var nameEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

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
        val email = emailEditText.toString()
        val password = passwordEditText.toString()
        val gender = genderSpinner.selectedItem.toString()

        if (name.isEmpty() || age.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        sharedPref.edit {
            putString("NAME", name)
            putInt("AGE", age.toInt())
            putString("EMAIL", email)
            putString("PASSWORD", password)
            putString("GENDER", gender)
            putBoolean("IS_REGISTERED", true)
            putBoolean("IS_LOGGED", true)
        }

        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}