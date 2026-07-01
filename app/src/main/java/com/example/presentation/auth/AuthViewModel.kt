package com.example.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.User
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeCurrentUser().collect { user ->
                _currentUser.value = user
            }
        }
    }

    fun login(email: String, javaPasswordState: String) {
        if (email.isBlank() || javaPasswordState.length < 6) {
            _errorMessage.value = "Please enter valid email and 6+ character password."
            return
        }
        _isLoading.value = true
        _errorMessage.value = null
        _isSuccess.value = false

        viewModelScope.launch {
            authRepository.login(email, javaPasswordState).collect { result ->
                _isLoading.value = false
                result.fold(
                    onSuccess = { user ->
                        _currentUser.value = user
                        _isSuccess.value = true
                    },
                    onFailure = { throwable ->
                        _errorMessage.value = throwable.localizedMessage ?: "Login failed"
                    }
                )
            }
        }
    }

    fun register(email: String, username: String, javaPasswordState: String) {
        if (email.isBlank() || username.isBlank() || javaPasswordState.length < 6) {
            _errorMessage.value = "Complete all fields. Password must be 6+ characters."
            return
        }
        _isLoading.value = true
        _errorMessage.value = null
        _isSuccess.value = false

        viewModelScope.launch {
            authRepository.register(email, username, javaPasswordState).collect { result ->
                _isLoading.value = false
                result.fold(
                    onSuccess = { user ->
                        _currentUser.value = user
                        _isSuccess.value = true
                    },
                    onFailure = { throwable ->
                        _errorMessage.value = throwable.localizedMessage ?: "Registration failed"
                    }
                )
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun getCurrentUser(): User? {
        return authRepository.getCurrentUser()
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout().collect { result ->
                if (result.isSuccess) {
                    _currentUser.value = null
                    _isSuccess.value = false
                }
            }
        }
    }
}
