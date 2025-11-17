package com.example.habitflow.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.habitflow.MainActivity
import com.example.habitflow.data.AppDatabase
import com.example.habitflow.data.model.User
import com.example.habitflow.databinding.ActivityLoginBinding
import com.example.habitflow.repository.UserRepository
import com.example.habitflow.util.SessionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var repository: UserRepository
    private lateinit var sessionManager: SessionManager

    // Firebase + Google Sign-In
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // Biometrics
    private var biometricPrompt: BiometricPrompt? = null
    private var promptInfo: BiometricPrompt.PromptInfo? = null

    companion object {
        private const val RC_GOOGLE_SIGN_IN = 1001
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Room
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "habitflow_db"
        )
            .fallbackToDestructiveMigration()
            .build()

        repository = UserRepository(db.userDao())
        sessionManager = SessionManager(this)

        // Firebase Auth
        auth = FirebaseAuth.getInstance()
        setupGoogleSignIn()

        // Reconnect Firebase user if needed
        val firebaseUser = auth.currentUser
        if (firebaseUser != null && sessionManager.getUserSession().isNullOrEmpty()) {
            firebaseUser.email?.let { email ->
                Log.d(TAG, "Auto sync Firebase → Session: $email")
                sessionManager.saveUserSession(email)
                sessionManager.setBiometricEnabled(true)
            }
        }

        setupBiometrics()

        // EMAIL + PASSWORD LOGIN
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            when {
                email.isEmpty() || password.isEmpty() -> showToast("Please fill all fields")
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> showToast("Invalid email format")
                password.length < 6 -> showToast("Password must be at least 6 characters")
                else -> {
                    lifecycleScope.launch {
                        val user = repository.login(email, password)
                        runOnUiThread {
                            if (user != null) {
                                sessionManager.saveUserSession(email)
                                sessionManager.setBiometricEnabled(true)
                                goToMain()
                            } else {
                                showToast("Invalid email or password")
                            }
                        }
                    }
                }
            }
        }

        // GOOGLE SIGN IN BUTTON
        binding.btnGoogleSignIn.setOnClickListener {
            startActivityForResult(googleSignInClient.signInIntent, RC_GOOGLE_SIGN_IN)
        }

        // BIOMETRIC LOGIN
        binding.btnBiometricLogin.setOnClickListener {
            val hasHistory = sessionManager.getUserSession()?.isNotEmpty() == true
                    || auth.currentUser != null

            if (!hasHistory) {
                showToast("Login once before using biometrics")
            } else if (sessionManager.isBiometricEnabled()) {
                promptInfo?.let { biometricPrompt?.authenticate(it) }
            } else {
                showToast("Biometric login not enabled")
            }
        }

        // GO REGISTER
        binding.tvRegisterRedirect.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // GOOGLE SIGN-IN
    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.example.habitflow.R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                task.getResult(ApiException::class.java)?.let {
                    handleGoogleAccount(it)
                } ?: showToast("Google Sign-in failed")
            } catch (e: ApiException) {
                showToast("Google Error: ${e.message}")
            }
        }
    }

    private fun handleGoogleAccount(account: GoogleSignInAccount) {
        val idToken = account.idToken ?: return showToast("No token returned")

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener showToast("Auth failed")

            val user = auth.currentUser
            val email = user?.email ?: return@addOnCompleteListener

            lifecycleScope.launch {
                var localUser = repository.getUserByEmail(email)
                if (localUser == null) {
                    localUser = User(
                        id = 0,
                        name = user.displayName ?: "HabitFlow User",
                        email = email,
                        password = "",
                        photoUri = user.photoUrl?.toString(),
                        coins = 100
                    )
                    repository.register(localUser)
                }

                sessionManager.saveUserSession(email)
                sessionManager.setBiometricEnabled(true)

                runOnUiThread { goToMain() }
            }
        }
    }

    // BIOMETRICS
    private fun setupBiometrics() {
        val biometricManager = BiometricManager.from(this)
        val canAuth = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            binding.btnBiometricLogin.isEnabled = false
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    goToMain()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode !in listOf(
                            BiometricPrompt.ERROR_USER_CANCELED,
                            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                            BiometricPrompt.ERROR_CANCELED
                        )
                    ) {
                        showToast("Biometric error: $errString")
                    }
                }

                override fun onAuthenticationFailed() {
                    showToast("Try again")
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock HabitFlow")
            .setSubtitle("Use fingerprint or device PIN")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
    }

    private fun goToMain() {
        showToast("Login successful!")
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
