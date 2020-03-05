package com.graytsar.batterynotification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.graytsar.batterynotification.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var notificationManager:NotificationManager? = null
    private val channelID:String = "com.graytsar.batterynotification.Alarm"
    private val notificationID:Int = 101
    lateinit var model:ModelBattery

    var db:BatteryDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        db = Room.databaseBuilder(
            applicationContext,
            BatteryDatabase::class.java, "ModelBattery_Database"
        ).allowMainThreadQueries().build()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(channelID, "Alarm", "Notify Battery")

        val array = db!!.batteryDao().getModel()

        if(array.isNotEmpty()){
            model = array[0]
        } else {
            model = ModelBattery(0)

            model.id = db!!.batteryDao().insertModel(model)
        }

        binding.lifecycleOwner = this
        binding.modelBattery = model

        //https://developer.android.com/reference/android/content/Intent#standard-broadcast-actions
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED)
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(model, intentFilter)

        if (Build.VERSION.SDK_INT < 28) {
            tableRowEstimated.visibility = View.GONE
        }

        seekBar2.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                model.valMax.value = progress

                if(model.valMax.value!! < model.valMin.value!!){
                    model.valMin.value = progress
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        seekBar3.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                model.valMin.value = progress

                if(model.valMin.value!! > model.valMax.value!!){
                    model.valMax.value = progress
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

    }

    fun pushNotify(title:String, text:String){
        val launchIntent = packageManager.getLaunchIntentForPackage("com.graytsar.batterynotification")
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        //switch does not update with MutableLiveData change, remove this feature till i think of something different that works
        //val deleteIntent = Intent(this, ModelBattery::class.java)
        //deleteIntent.action = "notification_cancelled"
        //val pendingDeleteIntent = PendingIntent.getBroadcast(this, 1, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val notification = NotificationCompat.Builder(this, channelID).apply {
            setContentTitle(title)
            setContentText(text)
            setSmallIcon(R.drawable.ic_battery_alert_black_24dp)
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            setContentIntent(pendingIntent)
            //setDeleteIntent(pendingDeleteIntent)
            setChannelId(channelID)
        }.build()

        notificationManager!!.notify(notificationID, notification)
    }

    private fun createNotificationChannel(id: String, name: String, description: String) {
        if(Build.VERSION.SDK_INT < 26 )
            return

        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(id, name, importance)

        channel.description = description
        notificationManager!!.createNotificationChannel(channel)
    }

    override fun onPause() {
        db?.batteryDao()!!.updateModel(model)
        super.onPause()
    }
}
