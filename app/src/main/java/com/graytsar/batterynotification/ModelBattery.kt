package com.graytsar.batterynotification

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ModelBattery")
class ModelBattery(@PrimaryKey(autoGenerate = true) var id:Long){
    @ColumnInfo(name = "valMax") var valMax:Int = 8
    @ColumnInfo(name = "valMin") var valMin:Int = 3
    @ColumnInfo(name = "valSwitch") var valSwitch:Boolean = false

    constructor():this( 0)

}