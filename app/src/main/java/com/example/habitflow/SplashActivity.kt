package com.example.habitflow

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This activity has no layout, it just decides where to go.

        val firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null) {
            // User is already signed in, go to MainActivity
            goToActivity(MainActivity::class.java)
        } else {
            // User is not signed in, go to LoginActivity
            goToActivity(LoginActivity::class.java)
        }
    }

    private fun <T> goToActivity(targetActivity: Class<T>) {
        val intent = Intent(this, targetActivity)
        startActivity(intent)
        // Finish this SplashActivity so the user cannot navigate back to it
        finish()
    }
}