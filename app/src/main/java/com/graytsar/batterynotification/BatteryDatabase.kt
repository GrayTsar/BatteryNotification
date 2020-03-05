package com.graytsar.batterynotification

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ModelBattery::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class BatteryDatabase: RoomDatabase() {
    abstract fun batteryDao():BatteryDao
}