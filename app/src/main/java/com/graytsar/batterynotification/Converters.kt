package com.graytsar.batterynotification
import androidx.room.TypeConverter
import androidx.lifecycle.MutableLiveData

class Converters {
    @TypeConverter
    fun mutableLiveDataFromInt(num:Int): MutableLiveData<Int> {
        return MutableLiveData<Int>(num)
    }

    @TypeConverter
    fun mutableLiveDataToInt(data: MutableLiveData<Int>):Int{
        return data.value!!
    }

    @TypeConverter
    fun mutableLiveDataFromBoolean(num:Boolean): MutableLiveData<Boolean> {
        return MutableLiveData<Boolean>(num)
    }

    @TypeConverter
    fun mutableLiveDataToBoolean(data: MutableLiveData<Boolean>):Boolean{
        return data.value!!
    }
}