package com.example.purrytify.di

import com.example.purrytify.data.repository.NetworkRepositoryImpl
import com.example.purrytify.domain.repository.NetworkRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindNetworkRepository(
        impl: NetworkRepositoryImpl
    ): NetworkRepository
}