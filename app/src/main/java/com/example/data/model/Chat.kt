package com.example.data.model

data class Chat(
    val chatId: String = "",
    val otherUser: User = User(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0L,
    val unreadCount: Int = 0
)
