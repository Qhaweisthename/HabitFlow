package com.example.habitflow.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.habitflow.data.AppDatabase
import com.example.habitflow.data.User
import com.example.habitflow.databinding.ActivityRegisterBinding
import com.example.habitflow.repository.UserRepository
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var repository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Initialize Room database
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "habitflow_db"
        )
            .fallbackToDestructiveMigration()
            .build()

        repository = UserRepository(db.userDao())

        // ✅ Register button click
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // 🔹 Input validation
            when {
                name.isEmpty() || email.isEmpty() || password.isEmpty() -> {
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
                        val existingUser = repository.getUserByEmail(email)
                        if (existingUser != null) {
                            runOnUiThread {
                                showToast("Email already registered. Please log in.")
                            }
                        } else {
                            val newUser = User(
                                id = 0,
                                name = name,
                                email = email,
                                password = password
                            )

                            repository.register(newUser)
                            runOnUiThread {
                                showToast("Registration successful!")
                                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                                finish()
                            }
                        }
                    }
                }
            }
        }

        // ✅ Redirect to Login
        binding.tvLoginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}