package com.example.di

import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.ChatRepository
import com.example.data.repository.ChatRepositoryImpl

interface AppContainer {
    val authRepository: AuthRepository
    val chatRepository: ChatRepository
}

class AppContainerImpl : AppContainer {
    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl()
    }

    override val chatRepository: ChatRepository by lazy {
        ChatRepositoryImpl(authRepository)
    }
}
