package com.cem.todo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)

        val buttonRegister = findViewById<Button>(R.id.loginRegister)
        buttonRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                val dbHelper = TodoDatabaseHelper(this)
                val isValidUser = dbHelper.checkUser(username, password)
                if (isValidUser) {
                    val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("isLoggedIn", true)
                    editor.apply()

                    // kullanıcıyı ana ekrana yönlendir
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // hata ver
                    Toast.makeText(this, "Kullanıcı adı veya şifre hatalı", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Kullanıcı adı ve şifre boş bırakılamaz", Toast.LENGTH_SHORT).show()
            }
        }

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                val dbHelper = TodoDatabaseHelper(this)
                val isValidUser = dbHelper.checkUser(username, password)
                if (isValidUser) {
                    // Kullanıcı ID'sini al
                    val userId = dbHelper.getUserId(username)

                    // Kullanıcı ID'sini SharedPreferences'a kaydet
                    val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putInt("userId", userId)
                    editor.putBoolean("isLoggedIn", true)
                    editor.apply()

                    // Ana ekrana yönlendir
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Kullanıcı adı veya şifre hatalı", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Kullanıcı adı ve şifre boş bırakılamaz", Toast.LENGTH_SHORT).show()
            }
        }

    }

}