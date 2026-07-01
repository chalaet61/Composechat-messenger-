package com.example.data.model

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false
) {
    // No-arg constructor required for Firestore serialization
    constructor() : this("", "", "", "", 0L, false)
}
