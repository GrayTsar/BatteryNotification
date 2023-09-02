package com.graytsar.batterynotification

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module to provide the database.
 */
@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    /**
     * Provides the [AlarmDatabase].
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, AlarmDatabase::class.java, "alarm")
            .allowMainThreadQueries()
            .build()
}