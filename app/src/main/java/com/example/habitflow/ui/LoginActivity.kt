package com.example.habitflow.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.habitflow.MainActivity
import com.example.habitflow.data.AppDatabase
import com.example.habitflow.databinding.ActivityLoginBinding
import com.example.habitflow.repository.UserRepository
import com.example.habitflow.util.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var repository: UserRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Initialize Room database properly
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "habitflow_db"
        )
            .fallbackToDestructiveMigration() // allows automatic rebuild if schema changes
            .build() // ✅ This must be here to create an actual DB instance

        // ✅ Repository and session
        repository = UserRepository(db.userDao())
        sessionManager = SessionManager(this)

        // ✅ If session already exists, go straight to MainActivity
        sessionManager.getUserSession()?.let {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // ✅ Login button click
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = repository.login(email, password)
                runOnUiThread {
                    if (user != null) {
                        // ✅ Save session
                        sessionManager.saveUserSession(email)
                        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // ✅ Redirect to Register page
        binding.tvRegisterRedirect.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
