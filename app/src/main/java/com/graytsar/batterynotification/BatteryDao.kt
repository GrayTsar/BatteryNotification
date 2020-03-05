package com.graytsar.batterynotification

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BatteryDao {
    @Insert
    fun insertModel(battery:ModelBattery):Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateModel(battery:ModelBattery)

    @Delete
    fun deleteModel(battery: ModelBattery)

    @Query("SELECT * FROM ModelBattery")
    fun getModel():Array<ModelBattery>
}