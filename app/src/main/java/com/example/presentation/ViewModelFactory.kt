package com.example.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.di.AppContainer
import com.example.presentation.auth.AuthViewModel
import com.example.presentation.chat.ChatViewModel

class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(container.authRepository) as T
            }
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                ChatViewModel(container.chatRepository, container.authRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
