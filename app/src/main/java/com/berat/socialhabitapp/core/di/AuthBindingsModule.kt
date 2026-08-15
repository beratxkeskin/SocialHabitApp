package com.berat.socialhabitapp.core.di

import com.berat.socialhabitapp.core.util.DefaultTimezoneProvider
import com.berat.socialhabitapp.core.util.TimezoneProvider
import com.berat.socialhabitapp.data.repository.AuthRepositoryImpl
import com.berat.socialhabitapp.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthBindingsModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTimezoneProvider(
        impl: DefaultTimezoneProvider
    ): TimezoneProvider
}
