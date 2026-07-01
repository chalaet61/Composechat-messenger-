package com.example.data.repository

import android.util.Log
import com.example.data.model.Chat
import com.example.data.model.Message
import com.example.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class ChatRepositoryImpl(
    private val authRepository: AuthRepository
) : ChatRepository {

    private val tag = "ChatRepositoryImpl"

    // Guarded check to see if Firebase is configured
    private val useFirebase: Boolean by lazy {
        try {
            FirebaseAuth.getInstance()
            FirebaseFirestore.getInstance()
            true
        } catch (e: Exception) {
            Log.w(tag, "Firebase Firestore not configured or initialized. Switching to Local Simulation Mode.", e)
            false
        }
    }

    private val firestore: FirebaseFirestore?
        get() = if (useFirebase) FirebaseFirestore.getInstance() else null

    // Fallback Mock State Management
    private val mockUsers = mutableListOf<User>().apply {
        add(User("user_alice", "alice@gmail.com", "Alice Johnson", null, true))
        add(User("user_bob", "bob@gmail.com", "Bob Smith", null, false))
        add(User("user_charlie", "charlie@gmail.com", "Charlie Green", null, true))
        add(User("user_diana", "diana@gmail.com", "Diana Prince", null, false))
    }

    private val mockMessages = mutableListOf<Message>().apply {
        val now = System.currentTimeMillis()
        // Pre-populate some historical chats to make the UI look premium and active
        add(Message("m1", "user_alice", "user_me", "Hey there! How is the real-time chat application going?", now - 3600000, true))
        add(Message("m2", "user_me", "user_alice", "It is going super well! I am writing highly modular architecture right now.", now - 3000000, true))
        add(Message("m3", "user_alice", "user_me", "That is awesome! Does it support real-time fallback mode too?", now - 2400000, true))
        add(Message("m4", "user_me", "user_alice", "Yes, fully responsive fallback is integrated so everything stays fast and stable.", now - 1800000, true))
        add(Message("m5", "user_alice", "user_me", "Perfect! Let me know when the build is complete.", now - 1200000, false))
        
        add(Message("m6", "user_charlie", "user_me", "Are we still playing football tomorrow evening?", now - 7200000, true))
        add(Message("m7", "user_me", "user_charlie", "Count me in! What time?", now - 7000000, true))
        add(Message("m8", "user_charlie", "user_me", "7 PM at the central stadium. See you there!", now - 6800000, true))

        add(Message("m9", "user_bob", "user_me", "Can you send me the API endpoints when you have a minute?", now - 18000000, true))
    }

    private val mockUsersFlow = MutableStateFlow<List<User>>(mockUsers)
    private val mockMessagesFlow = MutableStateFlow<List<Message>>(mockMessages)

    // Trigger update of mock chats list dynamically based on latest messages
    private val mockChatsFlow = MutableStateFlow<List<Chat>>(emptyList())

    init {
        if (!useFirebase) {
            updateMockChats()
        }
    }

    private fun updateMockChats() {
        val currentUserId = authRepository.getCurrentUser()?.uid ?: "user_me"
        
        // Group messages by contact
        val chatsMap = mutableMapOf<String, Chat>()
        
        for (msg in mockMessages) {
            val contactId = if (msg.senderId == currentUserId) msg.receiverId else msg.senderId
            if (contactId == currentUserId) continue
            
            val contact = mockUsers.find { it.uid == contactId } ?: User(contactId, "$contactId@example.com", contactId.substringAfter("user_").replaceFirstChar { it.uppercase() })
            
            val existingChat = chatsMap[contactId]
            val isUnread = !msg.isRead && msg.receiverId == currentUserId
            
            if (existingChat == null || msg.timestamp > existingChat.lastMessageTimestamp) {
                val currentUnreadCount = (existingChat?.unreadCount ?: 0) + (if (isUnread) 1 else 0)
                chatsMap[contactId] = Chat(
                    chatId = getConversationId(currentUserId, contactId),
                    otherUser = contact,
                    lastMessage = msg.text,
                    lastMessageTimestamp = msg.timestamp,
                    unreadCount = currentUnreadCount
                )
            } else {
                if (isUnread) {
                    chatsMap[contactId] = existingChat.copy(unreadCount = existingChat.unreadCount + 1)
                }
            }
        }
        mockChatsFlow.value = chatsMap.values.sortedByDescending { it.lastMessageTimestamp }
    }

    override fun getUsers(): Flow<Result<List<User>>> {
        return if (useFirebase) {
            flow {
                try {
                    val currentUserId = authRepository.getCurrentUser()?.uid ?: ""
                    val querySnapshot = firestore!!.collection("users").get().safeAwait()
                    val userList = querySnapshot.toObjects(User::class.java).filter { it.uid != currentUserId }
                    emit(Result.success(userList))
                } catch (e: Exception) {
                    emit(Result.failure(e))
                }
            }.flowOn(Dispatchers.IO)
        } else {
            flow {
                val currentUserId = authRepository.getCurrentUser()?.uid ?: "user_me"
                emit(Result.success(mockUsersFlow.value.filter { it.uid != currentUserId }))
            }
        }
    }

    override fun getChats(): Flow<Result<List<Chat>>> {
        return if (useFirebase) {
            flow {
                try {
                    val currentUserId = authRepository.getCurrentUser()?.uid ?: ""
                    // Query conversations where current user is a participant
                    val querySnapshot = firestore!!.collection("conversations")
                        .whereArrayContains("participants", currentUserId)
                        .get().safeAwait()
                    
                    val chats = querySnapshot.documents.mapNotNull { doc ->
                        val participants = doc.get("participants") as? List<*> ?: return@mapNotNull null
                        val otherUserId = participants.firstOrNull { it != currentUserId } as? String ?: return@mapNotNull null
                        
                        // Fetch other user profile
                        val otherUserSnapshot = firestore!!.collection("users").document(otherUserId).get().safeAwait()
                        val otherUser = otherUserSnapshot.toObject(User::class.java) ?: User(uid = otherUserId, displayName = "User")
                        
                        Chat(
                            chatId = doc.id,
                            otherUser = otherUser,
                            lastMessage = doc.getString("lastMessage") ?: "",
                            lastMessageTimestamp = doc.getLong("lastMessageTimestamp") ?: 0L,
                            unreadCount = doc.getLong("unreadCount_$currentUserId")?.toInt() ?: 0
                        )
                    }.sortedByDescending { it.lastMessageTimestamp }
                    
                    emit(Result.success(chats))
                } catch (e: Exception) {
                    emit(Result.failure(e))
                }
            }.flowOn(Dispatchers.IO)
        } else {
            mockChatsFlow.mapResult()
        }
    }

    override fun getMessages(otherUserId: String): Flow<Result<List<Message>>> {
        return if (useFirebase) {
            flow {
                try {
                    val currentUserId = authRepository.getCurrentUser()?.uid ?: ""
                    val chatId = getConversationId(currentUserId, otherUserId)
                    val querySnapshot = firestore!!.collection("conversations")
                        .document(chatId)
                        .collection("messages")
                        .orderBy("timestamp", Query.Direction.ASCENDING)
                        .get().safeAwait()
                    
                    val messages = querySnapshot.toObjects(Message::class.java)
                    emit(Result.success(messages))
                } catch (e: Exception) {
                    emit(Result.failure(e))
                }
            }.flowOn(Dispatchers.IO)
        } else {
            flow {
                val currentUserId = authRepository.getCurrentUser()?.uid ?: "user_me"
                val chatId = getConversationId(currentUserId, otherUserId)
                
                // Fetch relevant messages
                val filtered = mockMessagesFlow.value.filter {
                    (it.senderId == currentUserId && it.receiverId == otherUserId) ||
                    (it.senderId == otherUserId && it.receiverId == currentUserId)
                }.sortedBy { it.timestamp }
                
                emit(Result.success(filtered))
            }
        }
    }

    override fun sendMessage(receiverId: String, text: String): Flow<Result<Unit>> = flow {
        val currentUserId = authRepository.getCurrentUser()?.uid ?: "user_me"
        val chatId = getConversationId(currentUserId, receiverId)
        val timestamp = System.currentTimeMillis()
        val messageId = "msg_${timestamp}_${(100..999).random()}"
        
        val message = Message(
            messageId = messageId,
            senderId = currentUserId,
            receiverId = receiverId,
            text = text,
            timestamp = timestamp,
            isRead = false
        )

        if (useFirebase) {
            try {
                val conversationRef = firestore!!.collection("conversations").document(chatId)
                
                // Add message to subcollection
                conversationRef.collection("messages").document(messageId).set(message).safeAwait()
                
                // Update parent conversation meta
                val conversationMeta = mapOf(
                    "participants" to listOf(currentUserId, receiverId),
                    "lastMessage" to text,
                    "lastMessageTimestamp" to timestamp,
                    "unreadCount_$receiverId" to com.google.firebase.firestore.FieldValue.increment(1)
                )
                conversationRef.set(conversationMeta, com.google.firebase.firestore.SetOptions.merge()).safeAwait()
                
                emit(Result.success(Unit))
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
        } else {
            // Simulated local send
            mockMessages.add(message)
            mockMessagesFlow.value = mockMessages.toList()
            updateMockChats()
            emit(Result.success(Unit))

            // Trigger an automatic simulated response so the chat feels completely alive!
            simulateAutomatedResponse(receiverId, text)
        }
    }.flowOn(Dispatchers.IO)

    override fun markMessagesAsRead(senderId: String): Flow<Result<Unit>> = flow {
        val currentUserId = authRepository.getCurrentUser()?.uid ?: "user_me"
        val chatId = getConversationId(currentUserId, senderId)

        if (useFirebase) {
            try {
                val conversationRef = firestore!!.collection("conversations").document(chatId)
                
                // Mark messages in subcollection where sender == senderId and isRead == false as read
                val unreadQuery = conversationRef.collection("messages")
                    .whereEqualTo("senderId", senderId)
                    .whereEqualTo("isRead", false)
                    .get().safeAwait()
                
                val batch = firestore!!.batch()
                for (doc in unreadQuery.documents) {
                    batch.update(doc.reference, "isRead", true)
                }
                
                // Reset unread counter for current user in conversation document
                batch.update(conversationRef, "unreadCount_$currentUserId", 0)
                batch.commit().safeAwait()
                
                emit(Result.success(Unit))
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
        } else {
            // Simulated read marking
            var updatedAny = false
            mockMessages.forEachIndexed { index, msg ->
                if (msg.senderId == senderId && msg.receiverId == currentUserId && !msg.isRead) {
                    mockMessages[index] = msg.copy(isRead = true)
                    updatedAny = true
                }
            }
            if (updatedAny) {
                mockMessagesFlow.value = mockMessages.toList()
                updateMockChats()
            }
            emit(Result.success(Unit))
        }
    }.flowOn(Dispatchers.IO)

    private fun simulateAutomatedResponse(receiverId: String, userText: String) {
        // Spawn asynchronous simulation response
        CoroutineScope(Dispatchers.IO).launch {
            delay(1500) // Delay for "typing..." feel
            
            val responseText = when {
                userText.contains("hello", ignoreCase = true) || userText.contains("hi", ignoreCase = true) -> {
                    "Hey there! Ready to build something incredible with modern Jetpack Compose today?"
                }
                userText.contains("how are you", ignoreCase = true) -> {
                    "I am doing excellent! Just checking out our real-time MVVM Clean Architecture layout here."
                }
                userText.contains("meeting", ignoreCase = true) || userText.contains("time", ignoreCase = true) -> {
                    "Perfect! Let's connect shortly to sync on the deliverables."
                }
                userText.contains("firebase", ignoreCase = true) || userText.contains("setup", ignoreCase = true) -> {
                    "Configuring Firebase is extremely easy: just place your 'google-services.json' file under /app and register the app on the Firebase console."
                }
                else -> {
                    "Got it! That sounds super interesting. Tell me more about your Android project development goals!"
                }
            }

            val timestamp = System.currentTimeMillis()
            val replyMessage = Message(
                messageId = "msg_${timestamp}_reply",
                senderId = receiverId,
                receiverId = authRepository.getCurrentUser()?.uid ?: "user_me",
                text = responseText,
                timestamp = timestamp,
                isRead = false
            )

            mockMessages.add(replyMessage)
            mockMessagesFlow.value = mockMessages.toList()
            updateMockChats()
        }
    }

    private fun getConversationId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    // Helper Extension for flow conversion to Result
    private fun <T> Flow<T>.mapResult(): Flow<Result<T>> = flow {
        try {
            collect { value ->
                emit(Result.success(value))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // High-performance await helper for Task objects
    private suspend fun <T> com.google.android.gms.tasks.Task<T>.safeAwait(): T {
        return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    cont.resume(task.result, null)
                } else {
                    cont.resumeWith(Result.failure(task.exception ?: Exception("Unknown Firebase error")))
                }
            }
        }
    }
}
