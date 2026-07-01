package com.example.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Chat
import com.example.data.model.Message
import com.example.data.model.User
import com.example.data.repository.AuthRepository
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoadingChats = MutableStateFlow(false)
    val isLoadingChats: StateFlow<Boolean> = _isLoadingChats.asStateFlow()

    private val _isLoadingUsers = MutableStateFlow(false)
    val isLoadingUsers: StateFlow<Boolean> = _isLoadingUsers.asStateFlow()

    private val _isLoadingMessages = MutableStateFlow(false)
    val isLoadingMessages: StateFlow<Boolean> = _isLoadingMessages.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadChats() {
        _isLoadingChats.value = true
        _error.value = null
        viewModelScope.launch {
            chatRepository.getChats().collect { result ->
                _isLoadingChats.value = false
                result.fold(
                    onSuccess = { _chats.value = it },
                    onFailure = { _error.value = it.localizedMessage ?: "Failed to load chats" }
                )
            }
        }
    }

    fun loadUsers() {
        _isLoadingUsers.value = true
        _error.value = null
        viewModelScope.launch {
            chatRepository.getUsers().collect { result ->
                _isLoadingUsers.value = false
                result.fold(
                    onSuccess = { _users.value = it },
                    onFailure = { _error.value = it.localizedMessage ?: "Failed to load contacts" }
                )
            }
        }
    }

    fun loadMessages(otherUserId: String) {
        _isLoadingMessages.value = true
        _error.value = null
        viewModelScope.launch {
            chatRepository.getMessages(otherUserId).collect { result ->
                _isLoadingMessages.value = false
                result.fold(
                    onSuccess = { _messages.value = it },
                    onFailure = { _error.value = it.localizedMessage ?: "Failed to load messages" }
                )
            }
        }
        markAsRead(otherUserId)
    }

    fun sendMessage(receiverId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendMessage(receiverId, text).collect { result ->
                result.fold(
                    onSuccess = {
                        // Immediately refresh local message stream to feel responsive
                        loadMessages(receiverId)
                    },
                    onFailure = { _error.value = it.localizedMessage ?: "Failed to send message" }
                )
            }
        }
    }

    fun markAsRead(senderId: String) {
        viewModelScope.launch {
            chatRepository.markMessagesAsRead(senderId).collect { }
        }
    }

    fun getMyUserId(): String {
        return authRepository.getCurrentUser()?.uid ?: "user_me"
    }
}
