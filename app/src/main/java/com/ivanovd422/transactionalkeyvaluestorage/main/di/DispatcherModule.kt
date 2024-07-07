package com.ivanovd422.transactionalkeyvaluestorage.main.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @SingleThreadDispatcher
    @Provides
    fun provideSingleThreadDispatcher(): CoroutineDispatcher =
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class SingleThreadDispatcher