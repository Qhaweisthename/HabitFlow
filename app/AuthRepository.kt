package com.example.habitflow

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * A repository class that abstracts all Firebase Authentication data sources and operations.
 * Your Activities and ViewModels will interact with this class, not directly with Firebase.
 */
class AuthRepository(private val firebaseAuth: FirebaseAuth) {

    /**
     * Registers a new user with email and password.
     */
    suspend fun register(email: String, password: String): AuthResult {
        return firebaseAuth.createUserWithEmailAndPassword(email, password).await()
    }

    /**
     * Logs in an existing user with email and password.
     */
    suspend fun login(email: String, password: String): AuthResult {
        return firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }

    /**
     * Authenticates the user with Firebase using a Google Sign-In account.
     */
    suspend fun loginWithGoogle(account: GoogleSignInAccount): AuthResult {
        val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
        return firebaseAuth.signInWithCredential(credential).await()
    }

    /**
     * Signs out the current user.
     */
    fun signOut() {
        firebaseAuth.signOut()
    }
}
