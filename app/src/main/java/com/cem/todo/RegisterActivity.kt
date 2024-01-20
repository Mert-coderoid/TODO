package com.cem.todo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity: AppCompatActivity() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        registerButton = findViewById(R.id.registerButton)

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                val dbHelper = TodoDatabaseHelper(this)

                if (!dbHelper.userExists(username)) {
                    val registerSuccess = dbHelper.addUser(username, password)
                    if (registerSuccess > 0) {
                        Toast.makeText(this, "Kayıt başarılı", Toast.LENGTH_SHORT).show()
                        // Burada giriş ekranına veya ana ekrana yönlendirme yapabilirsiniz
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Kayıt sırasında bir hata oluştu", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Bu kullanıcı adı zaten alınmış", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Kullanıcı adı ve şifre boş bırakılamaz", Toast.LENGTH_SHORT).show()
            }
        }

    }
}