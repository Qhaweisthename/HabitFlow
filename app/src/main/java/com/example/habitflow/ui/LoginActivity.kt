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
import com.example.habitflow.data.User
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

        // ✅ Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        setupGoogleSignIn()

        // ✅ If Firebase already has a user (from previous Google login),
        //     but SessionManager has no email yet, sync them.
        val firebaseUser = auth.currentUser
        if (firebaseUser != null && sessionManager.getUserSession().isNullOrEmpty()) {
            firebaseUser.email?.let { email ->
                Log.d(TAG, "Syncing Firebase user into SessionManager: $email")
                sessionManager.saveUserSession(email)
                sessionManager.setBiometricEnabled(true)
            }
        }

        // ✅ Setup biometrics (if device supports)
        setupBiometrics()

        // ✅ Email/password login (Room)
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            when {
                email.isEmpty() || password.isEmpty() -> {
                    showToast("Please fill in all fields")
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    showToast("Enter a valid email address")
                }
                password.length < 6 -> {
                    showToast("Password must be at least 6 characters")
                }
                else -> {
                    lifecycleScope.launch {
                        val user = repository.login(email, password)
                        runOnUiThread {
                            if (user != null) {
                                // Save session + enable biometrics by default
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

        // ✅ Google SSO button
        binding.btnGoogleSignIn.setOnClickListener {
            startActivityForResult(googleSignInClient.signInIntent, RC_GOOGLE_SIGN_IN)
        }

        // ✅ Biometric unlock button (user explicitly opts in by tapping this)
        binding.btnBiometricLogin.setOnClickListener {
            val existingSession = sessionManager.getUserSession()
            val currentFirebaseUser = auth.currentUser

            Log.d(
                TAG,
                "Biometric click - session=$existingSession, firebaseUser=${currentFirebaseUser?.email}"
            )

            // We allow biometrics if there is EITHER a session OR a Firebase user
            val hasAnyLoginHistory = !existingSession.isNullOrEmpty() || currentFirebaseUser != null

            if (!hasAnyLoginHistory) {
                showToast("Login once with email or Google before using biometrics.")
            } else if (sessionManager.isBiometricEnabled()) {
                promptInfo?.let { info ->
                    biometricPrompt?.authenticate(info)
                } ?: showToast("Biometric not available.")
            } else {
                showToast("Biometric login not enabled.")
            }
        }

        // ✅ Redirect to Register
        binding.tvRegisterRedirect.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // Google Sign-In + Firebase

    private fun setupGoogleSignIn() {
        @Suppress("DEPRECATION") // ok for now
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // ✅ default_web_client_id is generated by google-services.json
            .requestIdToken(getString(com.example.habitflow.R.string.default_web_client_id))
            .requestEmail()
            .build()

        @Suppress("DEPRECATION")
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    handleGoogleAccount(account)
                } else {
                    showToast("Google sign-in failed.")
                }
            } catch (e: ApiException) {
                showToast("Google sign-in error: ${e.message}")
            }
        }
    }

    private fun handleGoogleAccount(account: GoogleSignInAccount) {
        val idToken = account.idToken
        if (idToken == null) {
            showToast("No ID token from Google.")
            return
        }

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val email = firebaseUser?.email
                    val displayName = firebaseUser?.displayName ?: "HabitFlow User"
                    val photoUri = firebaseUser?.photoUrl?.toString()

                    if (email == null) {
                        showToast("No email returned from Google account.")
                        return@addOnCompleteListener
                    }

                    lifecycleScope.launch {
                        // Link Firebase user to local Room user
                        var localUser = repository.getUserByEmail(email)
                        if (localUser == null) {
                            localUser = User(
                                id = 0,
                                name = displayName,
                                email = email,
                                password = "", // no local password for Google users
                                photoUri = photoUri,
                                coins = 100
                            )
                            repository.register(localUser)
                        }

                        // ✅ Save session + enable biometrics
                        sessionManager.saveUserSession(email)
                        sessionManager.setBiometricEnabled(true)

                        runOnUiThread {
                            goToMain()
                        }
                    }
                } else {
                    showToast("Firebase authentication with Google failed.")
                }
            }
    }

    // Biometrics

    private fun setupBiometrics() {
        val biometricManager = BiometricManager.from(this)
        val canAuth = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            // Device doesn't support biometrics or not enrolled
            binding.btnBiometricLogin.isEnabled = false
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    goToMain()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // ✅ Treat user dismiss / back as "no-op", not an error
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_CANCELED -> {
                            // user just backed out / tapped outside – stay on login screen, no toast
                            return
                        }
                        else -> {
                            showToast("Biometric error: $errString")
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Optional: you can remove this toast if it feels spammy
                    showToast("Biometric authentication failed. Try again.")
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock HabitFlow")
            .setSubtitle("Use your fingerprint or device credential")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

    }

    // Helpers

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
