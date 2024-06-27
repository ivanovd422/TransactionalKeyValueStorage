package com.ivanovd422.transactionalkeyvaluestorage.main.di

import com.ivanovd422.transactionalkeyvaluestorage.main.data.KeyValueStorage
import com.ivanovd422.transactionalkeyvaluestorage.main.domain.MainInteractor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideKeyValueStorage(): KeyValueStorage {
        return KeyValueStorage()
    }

    @Provides
    @Singleton
    fun provideMainInteractor(storage: KeyValueStorage): MainInteractor {
        return MainInteractor(storage)
    }
}


