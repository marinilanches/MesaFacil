package com.example.mesafacil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mesafacil.data.models.User
import com.example.mesafacil.data.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        loadCurrentUser()
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = authRepository.login(email, password)

            result.onSuccess { user ->
                _currentUser.value = user
                _authState.value = AuthState.Success
            }

            result.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Erro")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _currentUser.value = null
            _authState.value = AuthState.Idle
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _currentUser.value = user
            if (user != null) {
                _authState.value = AuthState.Success
            }
        }
    }

    fun isUserLoggedIn(): Boolean = authRepository.isUserLoggedIn()

    fun getCurrentUserId(): String? = authRepository.getCurrentUserId()

    fun register(email: String, password: String, name: String, role: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = authRepository.register(email, password)

            result.onSuccess { uid ->

                val user = User(
                    id = uid,
                    email = email,
                    name = name,
                    role = role,
                    isActive = true
                )

                _currentUser.value = user
                _authState.value = AuthState.Success
            }

            result.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "Erro desconhecido")
            }
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
