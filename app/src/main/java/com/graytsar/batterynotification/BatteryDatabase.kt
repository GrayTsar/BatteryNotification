package com.graytsar.batterynotification

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ModelBattery::class], version = 1, exportSchema = false)
abstract class BatteryDatabase: RoomDatabase() {
    abstract fun batteryDao():BatteryDao
}