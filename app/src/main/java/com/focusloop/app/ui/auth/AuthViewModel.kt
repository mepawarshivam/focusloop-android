package com.focusloop.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusloop.app.data.repository.AuthRepository
import com.focusloop.app.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    // Accounts live on the server now, so a fresh install can't know whether
    // this person already has one — default to Sign Up, they can switch.
    val isSignUpMode: Boolean = true,
    val email: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(private val authDataStore: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, errorMessage = null)
    }

    fun toggleMode() {
        _state.value = _state.value.copy(isSignUpMode = !_state.value.isSignUpMode, errorMessage = null)
    }

    fun submit(onSuccess: () -> Unit) {
        val email = _state.value.email
        val password = _state.value.password
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
            val result = if (_state.value.isSignUpMode) {
                authDataStore.signUp(email, password)
            } else {
                authDataStore.logIn(email, password)
            }
            when (result) {
                is AuthResult.Success -> {
                    _state.value = _state.value.copy(isSubmitting = false)
                    onSuccess()
                }
                is AuthResult.Failure -> {
                    _state.value = _state.value.copy(isSubmitting = false, errorMessage = result.message)
                }
            }
        }
    }
}
