package com.truonganim.sms.ai.di

import com.truonganim.sms.ai.data.repository.ContactRepositoryImpl
import com.truonganim.sms.ai.data.repository.MessageRepositoryImpl
import com.truonganim.sms.ai.domain.repository.ContactRepository
import com.truonganim.sms.ai.domain.repository.MessageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        messageRepositoryImpl: MessageRepositoryImpl
    ): MessageRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(
        contactRepositoryImpl: ContactRepositoryImpl
    ): ContactRepository
} 