package com.graytsar.batterynotification

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelMain: ViewModel() {
    val status = MutableLiveData<String>()
    val health = MutableLiveData<String>()
    val technology = MutableLiveData<String>()
    val voltage = MutableLiveData<String>()
    val temperature = MutableLiveData<String>()
    val estimated = MutableLiveData<String>()
    val level = MutableLiveData<String>()
    val plugged = MutableLiveData<String>()
    val capacity = MutableLiveData<String>()

    val max = MutableLiveData<Int>(8)
    val min = MutableLiveData<Int>(3)
    val start = MutableLiveData<Boolean>(false)

    val waveViewProgress = MutableLiveData<Float>(0F)
    val waveViewWaveHeight = MutableLiveData<Int>(40)

}