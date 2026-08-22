package com.focusloop.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class AuthState(
    val isLoggedIn: Boolean = false,
    val email: String = "",
    val uid: String? = null
)

sealed class AuthResult {
    object Success : AuthResult()
    data class Failure(val message: String) : AuthResult()
}

/**
 * FocusLoop accounts, backed by Firebase Authentication. Credentials are
 * verified server-side; this class never stores or hashes passwords itself.
 */
class AuthRepository(private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()) {

    val authState: Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            trySend(AuthState(isLoggedIn = user != null, email = user?.email ?: "", uid = user?.uid))
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    suspend fun signUp(email: String, password: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        if (!isValidEmail(normalizedEmail)) return AuthResult.Failure("Enter a valid email address")
        if (password.length < 6) return AuthResult.Failure("Password must be at least 6 characters")

        return try {
            firebaseAuth.createUserWithEmailAndPassword(normalizedEmail, password).await()
            AuthResult.Success
        } catch (e: FirebaseAuthWeakPasswordException) {
            AuthResult.Failure("Password is too weak — try a longer one")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Failure("Enter a valid email address")
        } catch (e: FirebaseAuthUserCollisionException) {
            AuthResult.Failure("An account already exists for that email — try logging in")
        } catch (e: Exception) {
            AuthResult.Failure(e.message ?: "Couldn't create your account — try again")
        }
    }

    suspend fun logIn(email: String, password: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        return try {
            firebaseAuth.signInWithEmailAndPassword(normalizedEmail, password).await()
            AuthResult.Success
        } catch (e: FirebaseAuthInvalidUserException) {
            AuthResult.Failure("No account found for that email")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Failure("Incorrect email or password")
        } catch (e: Exception) {
            AuthResult.Failure(e.message ?: "Couldn't log you in — try again")
        }
    }

    fun logOut() {
        firebaseAuth.signOut()
    }

    private fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
