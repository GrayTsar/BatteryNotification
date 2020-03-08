package com.graytsar.batterynotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Switch
import androidx.lifecycle.MutableLiveData
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.util.concurrent.TimeUnit

@Entity(tableName = "ModelBattery")
class ModelBattery(@PrimaryKey(autoGenerate = true) var id:Long):BroadcastReceiver() {

    @Ignore val valStatus = MutableLiveData<String>()
    @Ignore val valHealth = MutableLiveData<String>()
    @Ignore val valTechnology = MutableLiveData<String>()
    @Ignore val valVoltage = MutableLiveData<String>()
    @Ignore val valTemperature = MutableLiveData<String>()
    @Ignore val valEstimated = MutableLiveData<String>()
    @Ignore val valLevel = MutableLiveData<String>()
    @Ignore val valPlugged = MutableLiveData<String>()
    @Ignore val valCapacity = MutableLiveData<String>()

    @ColumnInfo(name = "valMax") var valMax = MutableLiveData<Int>(8)
    @ColumnInfo(name = "valMin") var valMin = MutableLiveData<Int>(3)
    @ColumnInfo(name = "valSwitch") var valSwitch = MutableLiveData<Boolean>(false)

    constructor():this( 0)

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent!!.action != Intent.ACTION_BATTERY_CHANGED && intent.action != Intent.ACTION_POWER_CONNECTED && intent.action != Intent.ACTION_POWER_DISCONNECTED){
            return
        }

        val batteryManager = context!!.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        //val batteryLow = intent.getBooleanExtra(BatteryManager.EXTRA_BATTERY_LOW, false) //API 28
        val heal = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0)
        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)

        when(heal){
            BatteryManager.BATTERY_HEALTH_COLD -> {
                valHealth.value = " ${context.getString(R.string.healthCold)}"
            }
            BatteryManager.BATTERY_HEALTH_DEAD -> {
                valHealth.value = " ${context.getString(R.string.healthDead)}"
            }
            BatteryManager.BATTERY_HEALTH_GOOD -> {
                valHealth.value = " ${context.getString(R.string.healthGood)}"
            }
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> {
                valHealth.value = " ${context.getString(R.string.healthOverheat)}"
            }
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> {
                valHealth.value = " ${context.getString(R.string.healthOvervoltage)}"
            }
            BatteryManager.BATTERY_HEALTH_UNKNOWN -> {
                valHealth.value = " ${context.getString(R.string.healthUnknown)}"
            }
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> {
                valHealth.value = " ${context.getString(R.string.healthUnspecified)}"
            }
        }

        valLevel.value = "${batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)} %"

        val lev = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) / 100F

        if(context is MainActivity){
            val ctx = context as MainActivity
            ctx.constraintLayout.waveView.progress = lev

            if(valSwitch.value == true){
                when {
                    lev >= valMax.value!! / 10f -> {
                        //Log.d("DBG:", "max max $lev ${valMax.value}")
                        if(status == BatteryManager.BATTERY_STATUS_CHARGING){
                            ctx.pushNotify(context.getString(R.string.pushTitle), "${context.getString(R.string.pushMax)} ${valMax.value!! * 10}%")
                        }
                    }
                    valMin.value!! / 10f > lev -> {
                        //Log.d("DBG:", "min min $lev ${valMin.value}")
                        if(status != BatteryManager.BATTERY_STATUS_CHARGING) {
                            ctx.pushNotify(
                                context.getString(R.string.pushTitle),
                                "${context.getString(R.string.pushMin)} ${valMin.value!! * 10}%"
                            )
                        }
                    }
                    else -> {
                        //Log.d("DBG:", "else $lev ${valMax.value} ${valSwitch.value}")
                    }
                }
            }
        }

        when(plugged){
            BatteryManager.BATTERY_PLUGGED_AC -> {
                valPlugged.value = " ${context.getString(R.string.pluggedAC)}"
            }
            BatteryManager.BATTERY_PLUGGED_USB -> {
                valPlugged.value = " ${context.getString(R.string.pluggedUSB)}"
            }
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> {
                valPlugged.value = " ${context.getString(R.string.pluggedWireless)}"
            }
            else ->{
                valPlugged.value = " ${context.getString(R.string.pluggedNone)}"
            }
        }

        when(status){
            BatteryManager.BATTERY_STATUS_CHARGING -> {
                valStatus.value = " ${context.getString(R.string.statusCharging)}"
            }
            BatteryManager.BATTERY_STATUS_DISCHARGING -> {
                valStatus.value = " ${context.getString(R.string.statusDischarging)}"
            }
            BatteryManager.BATTERY_STATUS_FULL -> {
                valStatus.value = " ${context.getString(R.string.statusFull)}"
            }
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> {
                valStatus.value = " ${context.getString(R.string.statusNotCharging)}"
            }
            BatteryManager.BATTERY_STATUS_UNKNOWN -> {
                valStatus.value = " ${context.getString(R.string.statusUnknown)}"
            }
        }

        valTechnology.value = " $technology"

        valTemperature.value = " ${(temperature / 10f)} ${context.getString(R.string.siCelsius)}"

        valVoltage.value = " $voltage ${context.getString(R.string.siMilliVolt)}"

        if (Build.VERSION.SDK_INT >= 28) {
            val ms = batteryManager.computeChargeTimeRemaining()
            valEstimated.value =  String.format(" %02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(ms),
                TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(ms)),
                TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)))
        }

        if(Build.VERSION.SDK_INT >= 21){
           valCapacity.value = " ${((context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager).getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 1000)} ${context.getString(
                          R.string.siMilliAmpereHours)}"
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
        valSwitch.value = (view as Switch).isChecked
    }

}