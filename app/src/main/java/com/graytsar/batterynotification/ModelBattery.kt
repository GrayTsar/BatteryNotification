package com.graytsar.batterynotification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*
import java.util.concurrent.TimeUnit

class ModelBattery(val tStatus:String,
                   val tHealth:String,
                   val tTechnology: String,
                   val tVoltage:String,
                   val tCapacity:String,
                   val tTemperature:String,
                   val tPlugged:String,
                   val tEstimated:String):BroadcastReceiver() {

    val valStatus = MutableLiveData<String>()
    val valHealth = MutableLiveData<String>()
    val valTechnology = MutableLiveData<String>()
    val valVoltage = MutableLiveData<String>()
    val valTemperature = MutableLiveData<String>()
    val valEstimated = MutableLiveData<String>()
    val valLevel = MutableLiveData<String>()
    val valPlugged = MutableLiveData<String>()
    val valCapacity = MutableLiveData<String>()

    var valMax = MutableLiveData<Int>(8)
    var valMin = MutableLiveData<Int>(3)
    var valSwitch = MutableLiveData<Boolean>(false)


    override fun onReceive(context: Context?, intent: Intent?) {
        val batteryManager = context!!.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        //val batteryLow = intent.getBooleanExtra(BatteryManager.EXTRA_BATTERY_LOW, false) //API 28
        val heal = intent!!.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0)
        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)

        when(heal){
            BatteryManager.BATTERY_HEALTH_COLD -> {
                valHealth.value = "Cold"
            }
            BatteryManager.BATTERY_HEALTH_DEAD -> {
                valHealth.value = "Dead"
            }
            BatteryManager.BATTERY_HEALTH_GOOD -> {
                valHealth.value = "Good"
            }
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> {
                valHealth.value = "Overheat"
            }
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> {
                valHealth.value = "Overvoltage"
            }
            BatteryManager.BATTERY_HEALTH_UNKNOWN -> {
                valHealth.value = "Unknown"
            }
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> {
                valHealth.value = "Unspecified Failure"
            }
        }

        valLevel.value = "${batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)} %"

        val lev = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) / 100F

        val ctx = context as MainActivity
        ctx.constraintLayout.imageViewAnimLevelBattery.layoutParams.height = (ctx.constraintLayout.height * lev).toInt()


        when {
            lev > valMax.value!! / 10f -> {
                Log.d("DBG:", "max max $lev ${valMax.value}")
                //ctx.pushNotify("Battery", "max max")
            }
            valMin.value!! / 10f > lev -> {
                Log.d("DBG:", "min min $lev ${valMin.value}")
                //ctx.pushNotify("Battery", "min min")
            }
            else -> {
                Log.d("DBG:", "else $lev ${valMax.value}")
            }
        }

        when(plugged){
            BatteryManager.BATTERY_PLUGGED_AC -> {
                valPlugged.value = "AC"
            }
            BatteryManager.BATTERY_PLUGGED_USB -> {
                valPlugged.value = "USB"
            }
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> {
                valPlugged.value = "Wireless"
            }
            else ->{
                valPlugged.value = "None"
            }
        }

        when(status){
            BatteryManager.BATTERY_STATUS_CHARGING -> {
                valStatus.value = "Charging"
            }
            BatteryManager.BATTERY_STATUS_DISCHARGING -> {
                valStatus.value = "Discharging"
            }
            BatteryManager.BATTERY_STATUS_FULL -> {
                valStatus.value = "Full"
            }
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> {
                valStatus.value = "Not Charging"
            }
            BatteryManager.BATTERY_STATUS_UNKNOWN -> {
                valStatus.value = "Unknown"
            }
        }

        valTechnology.value = technology

        valTemperature.value = "${temperature / 10f} °C"

        valVoltage.value = "$voltage mV"

        if (Build.VERSION.SDK_INT >= 28) {
            val ms = batteryManager.computeChargeTimeRemaining()
            valEstimated.value =  String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(ms),
                TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(ms)),
                TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)))
        }

        if(Build.VERSION.SDK_INT >= 21){
           valCapacity.value = ((context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager).getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 1000).toString() + " mAh"
        }

        /*
        val cCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) //Battery capacity in microampere-hours, as an integer.
        val cAverage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE) //Average battery current in microamperes
        val cNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) //Instantaneous battery current in microamperes
        val eCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER) //Battery remaining energy in nanowatt-hours
        Log.d("DBG-1:", "$cCounter   $cAverage   $cNow   $eCounter")
         */
    }

    fun onSwitchClick(view: View){

    }

}