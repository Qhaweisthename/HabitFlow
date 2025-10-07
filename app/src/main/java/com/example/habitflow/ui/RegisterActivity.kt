package com.example.habitflow.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.habitflow.MainActivity
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

        // ✅ Initialize Room database correctly
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "habitflow_db"
        )
            .fallbackToDestructiveMigration() // allows rebuild when schema changes
            .build() // ✅ this must be called at the end

        repository = UserRepository(db.userDao()) // ✅ Now this works

        // ✅ Register button click
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newUser = User(
                id = 0,
                name = name,
                email = email,
                password = password
            )

            lifecycleScope.launch {
                repository.register(newUser)
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                }
            }
        }

        // ✅ Already have an account → go back to login
        binding.tvLoginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
