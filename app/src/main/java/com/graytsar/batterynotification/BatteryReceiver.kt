package com.graytsar.batterynotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import java.util.concurrent.TimeUnit

class BatteryReceiver(val model:ModelBattery):BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent == null || context == null){
            return
        }

        if(intent.action != Intent.ACTION_BATTERY_CHANGED && intent.action != Intent.ACTION_POWER_CONNECTED && intent.action != Intent.ACTION_POWER_DISCONNECTED){
            return
        }

        if(context is MainActivity) {
            context.viewModelMain.let { viewModelMain ->
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

                //val batteryLow = intent.getBooleanExtra(BatteryManager.EXTRA_BATTERY_LOW, false) //API 28
                val heal = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)
                val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0)
                val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
                val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)

                when(heal){
                    BatteryManager.BATTERY_HEALTH_COLD -> {
                        viewModelMain.health.value = " ${context.getString(R.string.healthCold)}"
                    }
                    BatteryManager.BATTERY_HEALTH_DEAD -> {
                        viewModelMain.health.value = " ${context.getString(R.string.healthDead)}"
                    }
                    BatteryManager.BATTERY_HEALTH_GOOD -> {
                        viewModelMain.health.value = " ${context.getString(R.string.healthGood)}"
                    }
                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> {
                        viewModelMain.health.value = " ${context.getString(R.string.healthOverheat)}"
                    }
                    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> {
                        viewModelMain.health.value = " ${context.getString(R.string.healthOvervoltage)}"
                    }
                    BatteryManager.BATTERY_HEALTH_UNKNOWN -> {
                        viewModelMain.health.value = " ${context.getString(R.string.healthUnknown)}"
                    }
                    BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> {
                        viewModelMain.health.value = " ${context.getString(R.string.healthUnspecified)}"
                    }
                }

                viewModelMain.level.value = "${batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)} %"

                val lev = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) / 100F

                viewModelMain.waveViewProgress.value = lev
                viewModelMain.waveViewWaveHeight.value = (40 * lev).toInt()

                if(model.valSwitch){
                    when {
                        lev >= model.valMax / 10f -> {
                            //Log.d("DBG:", "max max $lev ${valMax.value}")
                            if(status == BatteryManager.BATTERY_STATUS_CHARGING){
                                context.pushNotify(context.getString(R.string.pushTitle), "${context.getString(R.string.pushMax)} ${model.valMax * 10}%")
                            }
                        }
                        model.valMin / 10f > lev -> {
                            //Log.d("DBG:", "min min $lev ${valMin.value}")
                            if(status != BatteryManager.BATTERY_STATUS_CHARGING) {
                                context.pushNotify(
                                    context.getString(R.string.pushTitle),
                                    "${context.getString(R.string.pushMin)} ${model.valMin * 10}%"
                                )
                            }
                        }
                        else -> {
                            //Log.d("DBG:", "else $lev ${valMax.value} ${valSwitch.value}")
                        }
                    }
                }


                when(plugged){
                    BatteryManager.BATTERY_PLUGGED_AC -> {
                        viewModelMain.plugged.value = " ${context.getString(R.string.pluggedAC)}"
                    }
                    BatteryManager.BATTERY_PLUGGED_USB -> {
                        viewModelMain.plugged.value = " ${context.getString(R.string.pluggedUSB)}"
                    }
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> {
                        viewModelMain.plugged.value = " ${context.getString(R.string.pluggedWireless)}"
                    }
                    else ->{
                        viewModelMain.plugged.value = " ${context.getString(R.string.pluggedNone)}"
                    }
                }

                when(status){
                    BatteryManager.BATTERY_STATUS_CHARGING -> {
                        viewModelMain.status.value = " ${context.getString(R.string.statusCharging)}"
                    }
                    BatteryManager.BATTERY_STATUS_DISCHARGING -> {
                        viewModelMain.status.value = " ${context.getString(R.string.statusDischarging)}"
                    }
                    BatteryManager.BATTERY_STATUS_FULL -> {
                        viewModelMain.status.value = " ${context.getString(R.string.statusFull)}"
                    }
                    BatteryManager.BATTERY_STATUS_NOT_CHARGING -> {
                        viewModelMain.status.value = " ${context.getString(R.string.statusNotCharging)}"
                    }
                    BatteryManager.BATTERY_STATUS_UNKNOWN -> {
                        viewModelMain.status.value = " ${context.getString(R.string.statusUnknown)}"
                    }
                }

                viewModelMain.technology.value = " $technology"

                viewModelMain.temperature.value = " ${(temperature / 10f)} ${context.getString(R.string.siCelsius)}"

                viewModelMain.voltage.value = " $voltage ${context.getString(R.string.siMilliVolt)}"

                if (Build.VERSION.SDK_INT >= 28) {
                    val ms = batteryManager.computeChargeTimeRemaining()
                    viewModelMain.estimated.value =  String.format(" %02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(ms),
                        TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(ms)),
                        TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)))
                }

                if(Build.VERSION.SDK_INT >= 21){
                    viewModelMain.capacity.value = " ${((context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager).getLongProperty(
                        BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 1000)} ${context.getString(
                        R.string.siMilliAmpereHours)}"
                }
            }
        }



        /*
        val cCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) //Battery capacity in microampere-hours, as an integer.
        val cAverage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE) //Average battery current in microamperes
        val cNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) //Instantaneous battery current in microamperes
        val eCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER) //Battery remaining energy in nanowatt-hours
        Log.d("DBG-1:", "$cCounter   $cAverage   $cNow   $eCounter")
         */
    }
}