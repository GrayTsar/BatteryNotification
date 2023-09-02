package com.graytsar.batterynotification

import androidx.room.*

@Dao
interface BatteryDao {
    @Insert
    fun insertModel(battery: BatteryEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateModel(battery: BatteryEntity)

    @Delete
    fun deleteModel(battery: BatteryEntity)

    @Query("SELECT * FROM alarm")
    fun getModel(): BatteryEntity?
}