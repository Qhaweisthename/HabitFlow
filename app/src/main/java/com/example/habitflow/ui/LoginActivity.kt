package com.example.habitflow.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
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
            .fallbackToDestructiveMigration()
            .build()

        repository = UserRepository(db.userDao())
        sessionManager = SessionManager(this)

        // ✅ Auto-login if session exists
        sessionManager.getUserSession()?.let {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // ✅ Login button click
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // 🔹 Validation checks
            when {
                email.isEmpty() || password.isEmpty() -> {
                    showToast("Please fill in all fields")
                    return@setOnClickListener
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    showToast("Enter a valid email address")
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    showToast("Password must be at least 6 characters")
                    return@setOnClickListener
                }
                else -> {
                    lifecycleScope.launch {
                        val user = repository.login(email, password)
                        runOnUiThread {
                            if (user != null) {
                                sessionManager.saveUserSession(email)
                                showToast("Login successful!")
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            } else {
                                showToast("Invalid email or password")
                            }
                        }
                    }
                }
            }
        }

        // ✅ Redirect to Register
        binding.tvRegisterRedirect.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}