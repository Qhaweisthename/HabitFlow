package com.example.habitflow

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var googleClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var repo: AuthRepository

    // UI Views
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var btnGoogle: Button
    private lateinit var progress: ProgressBar
    private lateinit var tvError: TextView

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                uiLoading(true)
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        repo.loginWithGoogle(account)
                        uiLoading(false)
                        Toast.makeText(this@LoginActivity, "Google Sign-In Success", Toast.LENGTH_SHORT).show()
                        // TODO: Navigate to home activity
                    } catch (e: Exception) {
                        uiLoading(false)
                        showError(e.message ?: "Google Sign-In failed")
                    }
                }
            } catch (e: ApiException) {
                uiLoading(false)
                showError("Google sign-in failed: ${e.statusCode}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialization
        firebaseAuth = FirebaseAuth.getInstance()
        repo = AuthRepository(firebaseAuth)

        // Find Views
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        btnGoogle = findViewById(R.id.btnGoogle)
        progress = findViewById(R.id.progress)
        tvError = findViewById(R.id.tvError)

        // Google Sign-In Options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleClient = GoogleSignIn.getClient(this, gso)

        // Click Listeners
        btnLogin.setOnClickListener { handleLogin() }
        btnRegister.setOnClickListener { handleRegister() }
        btnGoogle.setOnClickListener { googleSignInLauncher.launch(googleClient.signInIntent) }
    }

    private fun handleLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Enter a valid email"); return
        }
        if (password.length < 6) {
            showError("Password must be at least 6 characters"); return
        }

        uiLoading(true)
        CoroutineScope(Dispatchers.Main).launch {
            try {
                repo.login(email, password)
                uiLoading(false)
                Toast.makeText(this@LoginActivity, "Login success", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to your home activity
            } catch (e: Exception) {
                uiLoading(false)
                showError(e.message ?: "Login failed")
            }
        }
    }

    private fun handleRegister() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Enter a valid email"); return
        }
        if (password.length < 6) {
            showError("Password must be at least 6 characters"); return
        }

        uiLoading(true)
        CoroutineScope(Dispatchers.Main).launch {
            try {
                repo.register(email, password)
                uiLoading(false)
                Toast.makeText(this@LoginActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                uiLoading(false)
                showError(e.message ?: "Registration failed")
            }
        }
    }

    private fun uiLoading(isLoading: Boolean) {
        progress.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }
}
