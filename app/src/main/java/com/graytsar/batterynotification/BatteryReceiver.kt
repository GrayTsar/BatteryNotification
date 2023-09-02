package com.graytsar.batterynotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import java.util.concurrent.TimeUnit

class BatteryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context !is AlarmActivity) return

        if (intent.action != Intent.ACTION_BATTERY_CHANGED
            && intent.action != Intent.ACTION_POWER_CONNECTED
            && intent.action != Intent.ACTION_POWER_DISCONNECTED
        ) {
            return
        }

        val viewModel = context.viewModel
        val manager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val capacity = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val capacityLevel = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val chargeCounter = manager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)

        //val batteryLow = intent.getBooleanExtra(BatteryManager.EXTRA_BATTERY_LOW, false) //API 28
        val heal = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0)
        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)

        viewModel.health.value = when (heal) {
            BatteryManager.BATTERY_HEALTH_COLD -> context.getString(R.string.healthCold)
            BatteryManager.BATTERY_HEALTH_DEAD -> context.getString(R.string.healthDead)
            BatteryManager.BATTERY_HEALTH_GOOD -> context.getString(R.string.healthGood)
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> context.getString(R.string.healthOverheat)
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> context.getString(R.string.healthOvervoltage)
            BatteryManager.BATTERY_HEALTH_UNKNOWN -> context.getString(R.string.healthUnknown)
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> context.getString(R.string.healthUnspecified)
            else -> ""
        }

        viewModel.plugged.value = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> context.getString(R.string.pluggedAC)
            BatteryManager.BATTERY_PLUGGED_USB -> context.getString(R.string.pluggedUSB)
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> context.getString(R.string.pluggedWireless)
            else -> context.getString(R.string.pluggedNone)
        }

        viewModel.status.value = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> context.getString(R.string.statusCharging)
            BatteryManager.BATTERY_STATUS_DISCHARGING -> context.getString(R.string.statusDischarging)
            BatteryManager.BATTERY_STATUS_FULL -> context.getString(R.string.statusFull)
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> context.getString(R.string.statusNotCharging)
            BatteryManager.BATTERY_STATUS_UNKNOWN -> context.getString(R.string.statusUnknown)
            else -> ""
        }

        viewModel.level.value = "$capacity %"
        viewModel.technology.value = "$technology"
        viewModel.temperature.value =
            "${(temperature / 10f)} ${context.getString(R.string.siCelsius)}"
        viewModel.voltage.value = "$voltage ${context.getString(R.string.siMilliVolt)}"

        val lev = capacityLevel / 100F
        viewModel.waveViewProgress.value = lev
        viewModel.waveViewWaveHeight.value = (40 * lev).toInt()

        val start = viewModel.start.value!!
        val max = viewModel.max.value!!
        val min = viewModel.min.value!!
        if (start) {
            when {
                lev >= max / 10f -> {
                    //Log.d("DBG:", "max max $lev ${valMax.value}")
                    if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                        viewModel.notify(
                            context,
                            context.getString(R.string.pushTitle),
                            "${context.getString(R.string.pushMax)} ${max * 10}%"
                        )
                    }
                }

                min / 10f >= lev -> {
                    //Log.d("DBG:", "min min $lev ${valMin.value}")
                    if (status != BatteryManager.BATTERY_STATUS_CHARGING) {
                        viewModel.notify(
                            context,
                            context.getString(R.string.pushTitle),
                            "${context.getString(R.string.pushMin)} ${min * 10}%"
                        )
                    }
                }

                else -> {
                    //Log.d("DBG:", "else $lev ${valMax.value} ${valSwitch.value}")
                }
            }
        }

        if (Build.VERSION.SDK_INT >= 28) {
            val ms = manager.computeChargeTimeRemaining()
            val hours = TimeUnit.MILLISECONDS.toHours(ms)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(ms)
            viewModel.estimated.value = String.format(
                " %02d:%02d:%02d",
                hours,
                minutes - TimeUnit.HOURS.toMinutes(hours),
                seconds - TimeUnit.MINUTES.toSeconds(minutes)
            )
        }

        viewModel.capacity.value =
            " ${(chargeCounter / 1000)} ${context.getString(R.string.siMilliAmpereHours)}"


        /*
        val cCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) //Battery capacity in microampere-hours, as an integer.
        val cAverage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE) //Average battery current in microamperes
        val cNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) //Instantaneous battery current in microamperes
        val eCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER) //Battery remaining energy in nanowatt-hours
        Log.d("DBG-1:", "$cCounter   $cAverage   $cNow   $eCounter")
         */
    }
}