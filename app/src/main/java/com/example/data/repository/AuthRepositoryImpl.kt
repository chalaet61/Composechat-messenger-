package com.example.data.repository

import android.util.Log
import com.example.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AuthRepositoryImpl : AuthRepository {

    private val tag = "AuthRepositoryImpl"

    // Guarded check to see if Firebase is configured on this device/emulator
    private val useFirebase: Boolean by lazy {
        try {
            FirebaseAuth.getInstance()
            FirebaseFirestore.getInstance()
            true
        } catch (e: Exception) {
            Log.w(tag, "Firebase Auth/Firestore not configured or initialized. Switching to Local Simulation Mode.", e)
            false
        }
    }

    private val firebaseAuth: FirebaseAuth?
        get() = if (useFirebase) FirebaseAuth.getInstance() else null

    private val firestore: FirebaseFirestore?
        get() = if (useFirebase) FirebaseFirestore.getInstance() else null

    // Fallback Mock State Management
    private val mockUsers = mutableMapOf<String, User>().apply {
        // Seed initial simulation users
        put("user_alice", User("user_alice", "alice@gmail.com", "Alice Johnson", null, true))
        put("user_bob", User("user_bob", "bob@gmail.com", "Bob Smith", null, false))
        put("user_charlie", User("user_charlie", "charlie@gmail.com", "Charlie Green", null, true))
        put("user_diana", User("user_diana", "diana@gmail.com", "Diana Prince", null, false))
    }
    private val mockCurrentUserState = MutableStateFlow<User?>(null)

    init {
        // Auto-login a mock user if using simulation, so user doesn't have to register every restart
        if (!useFirebase) {
            val defaultMock = User("user_me", "me@example.com", "You (Simulator User)", null, true)
            mockUsers["user_me"] = defaultMock
            mockCurrentUserState.value = defaultMock
        }
    }

    override fun login(email: String, password: String): Flow<Result<User>> = flow {
        if (useFirebase) {
            try {
                val authResult = firebaseAuth!!.signInWithEmailAndPassword(email, password).safeAwait()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val uid = firebaseUser.uid
                    // Fetch user details from Firestore
                    val docSnapshot = firestore!!.collection("users").document(uid).get().safeAwait()
                    val user = docSnapshot.toObject(User::class.java) ?: User(
                        uid = uid,
                        email = firebaseUser.email ?: email,
                        displayName = firebaseUser.displayName ?: email.substringBefore("@")
                    )
                    emit(Result.success(user))
                } else {
                    emit(Result.failure(Exception("Authentication failed: User is null")))
                }
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
        } else {
            // Simulated login
            val matchedUser = mockUsers.values.find { it.email.equals(email, ignoreCase = true) }
            if (matchedUser != null) {
                mockCurrentUserState.value = matchedUser
                emit(Result.success(matchedUser))
            } else if (email.isNotEmpty() && password.length >= 6) {
                // If not seeded, dynamically register/log in anyway to make testing easy!
                val newUser = User(
                    uid = "user_" + email.hashCode().toString(),
                    email = email,
                    displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    isOnline = true
                )
                mockUsers[newUser.uid] = newUser
                mockCurrentUserState.value = newUser
                emit(Result.success(newUser))
            } else {
                emit(Result.failure(Exception("Invalid credentials. Try email 'alice@gmail.com' or input any email and password of 6+ characters.")))
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun register(email: String, username: String, password: String): Flow<Result<User>> = flow {
        if (useFirebase) {
            try {
                val authResult = firebaseAuth!!.createUserWithEmailAndPassword(email, password).safeAwait()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val uid = firebaseUser.uid
                    val user = User(
                        uid = uid,
                        email = email,
                        displayName = username,
                        isOnline = true
                    )
                    // Save to Firestore
                    firestore!!.collection("users").document(uid).set(user).safeAwait()
                    emit(Result.success(user))
                } else {
                    emit(Result.failure(Exception("Registration failed: User is null")))
                }
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
        } else {
            // Simulated register
            if (email.isEmpty() || username.isEmpty() || password.length < 6) {
                emit(Result.failure(Exception("Please complete all fields. Password must be at least 6 characters.")))
            } else {
                val existing = mockUsers.values.find { it.email.equals(email, ignoreCase = true) }
                if (existing != null) {
                    emit(Result.failure(Exception("User already exists with this email.")))
                } else {
                    val newUser = User(
                        uid = "user_" + email.hashCode().toString(),
                        email = email,
                        displayName = username,
                        isOnline = true
                    )
                    mockUsers[newUser.uid] = newUser
                    mockCurrentUserState.value = newUser
                    emit(Result.success(newUser))
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun logout(): Flow<Result<Unit>> = flow {
        if (useFirebase) {
            try {
                val uid = firebaseAuth?.currentUser?.uid
                if (uid != null) {
                    // Set online status to false
                    firestore?.collection("users")?.document(uid)?.update("isOnline", false)?.safeAwait()
                }
                firebaseAuth!!.signOut()
                emit(Result.success(Unit))
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
        } else {
            mockCurrentUserState.value = null
            emit(Result.success(Unit))
        }
    }.flowOn(Dispatchers.IO)

    override fun getCurrentUser(): User? {
        return if (useFirebase) {
            val fUser = firebaseAuth?.currentUser
            if (fUser != null) {
                User(
                    uid = fUser.uid,
                    email = fUser.email ?: "",
                    displayName = fUser.displayName ?: fUser.email?.substringBefore("@") ?: "User"
                )
            } else null
        } else {
            mockCurrentUserState.value
        }
    }

    override fun observeCurrentUser(): Flow<User?> {
        return if (useFirebase) {
            flow {
                val fUser = firebaseAuth?.currentUser
                if (fUser != null) {
                    emit(
                        User(
                            uid = fUser.uid,
                            email = fUser.email ?: "",
                            displayName = fUser.displayName ?: fUser.email?.substringBefore("@") ?: "User"
                        )
                    )
                } else {
                    emit(null)
                }
            }
        } else {
            mockCurrentUserState
        }
    }

    // High-performance await helper for Task objects to bridge callback flow to Coroutines safely
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
