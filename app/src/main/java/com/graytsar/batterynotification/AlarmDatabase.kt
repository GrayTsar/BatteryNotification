package com.graytsar.batterynotification

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        BatteryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun batteryDao(): BatteryDao
}