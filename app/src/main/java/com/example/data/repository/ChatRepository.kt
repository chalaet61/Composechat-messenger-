package com.example.data.repository

import com.example.data.model.Chat
import com.example.data.model.Message
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getUsers(): Flow<Result<List<User>>>
    fun getChats(): Flow<Result<List<Chat>>>
    fun getMessages(otherUserId: String): Flow<Result<List<Message>>>
    fun sendMessage(receiverId: String, text: String): Flow<Result<Unit>>
    fun markMessagesAsRead(senderId: String): Flow<Result<Unit>>
}
