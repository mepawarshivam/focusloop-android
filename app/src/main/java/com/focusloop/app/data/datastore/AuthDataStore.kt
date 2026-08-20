package com.focusloop.app.data.datastore

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

val Context.authDataStore by preferencesDataStore(name = "focusloop_auth")

data class AuthState(
    val isLoggedIn: Boolean = false,
    val email: String = ""
)

sealed class AuthResult {
    object Success : AuthResult()
    data class Failure(val message: String) : AuthResult()
}

/**
 * Local-only account store. Credentials never leave the device: the password
 * is stretched with PBKDF2 and only the salted hash is persisted, never the
 * plaintext password.
 */
class AuthDataStore(private val context: Context) {

    private object Keys {
        val EMAIL = stringPreferencesKey("account_email")
        val PASSWORD_HASH = stringPreferencesKey("account_password_hash")
        val PASSWORD_SALT = stringPreferencesKey("account_password_salt")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    val authState: Flow<AuthState> = context.authDataStore.data.map { prefs ->
        AuthState(
            isLoggedIn = prefs[Keys.IS_LOGGED_IN] ?: false,
            email = prefs[Keys.EMAIL] ?: ""
        )
    }

    suspend fun hasAccount(): Boolean {
        val email = context.authDataStore.data.first()[Keys.EMAIL]
        return !email.isNullOrBlank()
    }

    suspend fun signUp(email: String, password: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        if (!isValidEmail(normalizedEmail)) return AuthResult.Failure("Enter a valid email address")
        if (password.length < 6) return AuthResult.Failure("Password must be at least 6 characters")

        val salt = generateSalt()
        val hash = hashPassword(password, salt)
        context.authDataStore.edit { prefs ->
            prefs[Keys.EMAIL] = normalizedEmail
            prefs[Keys.PASSWORD_HASH] = hash
            prefs[Keys.PASSWORD_SALT] = Base64.encodeToString(salt, Base64.NO_WRAP)
            prefs[Keys.IS_LOGGED_IN] = true
        }
        return AuthResult.Success
    }

    suspend fun logIn(email: String, password: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        val prefs = context.authDataStore.data.first()
        val storedEmail = prefs[Keys.EMAIL]
        val storedHash = prefs[Keys.PASSWORD_HASH]
        val storedSalt = prefs[Keys.PASSWORD_SALT]
        if (storedEmail == null || storedEmail != normalizedEmail) {
            return AuthResult.Failure("No account found for that email")
        }
        val salt = Base64.decode(storedSalt, Base64.NO_WRAP)
        val hash = hashPassword(password, salt)
        if (hash != storedHash) {
            return AuthResult.Failure("Incorrect password")
        }
        context.authDataStore.edit { it[Keys.IS_LOGGED_IN] = true }
        return AuthResult.Success
    }

    suspend fun logOut() {
        context.authDataStore.edit { it[Keys.IS_LOGGED_IN] = false }
    }

    private fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private fun hashPassword(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, 120_000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
}
