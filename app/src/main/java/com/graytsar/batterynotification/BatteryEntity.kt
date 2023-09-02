package com.graytsar.batterynotification

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm")
data class BatteryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = "max") val max: Int = 8,
    @ColumnInfo(name = "min") val min: Int = 3,
    @ColumnInfo(name = "enabled") val enabled: Boolean = false
)