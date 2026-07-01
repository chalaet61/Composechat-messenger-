package com.example.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val isOnline: Boolean = false
) {
    // No-arg constructor required for Firestore serialization
    constructor() : this("", "", "", null, false)
}
