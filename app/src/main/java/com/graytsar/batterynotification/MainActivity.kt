package com.graytsar.batterynotification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.graytsar.batterynotification.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var notificationManager:NotificationManager? = null
    private val channelID:String = "com.graytsar.batterynotification.Alarm"
    private val notificationID:Int = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(channelID, "Alarm", "Notify Battery Constraints")

        val model = ModelBattery(
            getString(R.string.status) + " ",
            getString(R.string.health) + " ",
            getString(R.string.technology) + " ",
            getString(R.string.voltage) + " ",
            getString(R.string.capacity),
            getString(R.string.temperature) + " ",
            getString(R.string.plugged) + " ",
            getString(R.string.estimated) + " "
        )

        binding.lifecycleOwner = this
        binding.modelBattery = model

        //https://developer.android.com/reference/android/content/Intent#standard-broadcast-actions
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED)
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(model, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        if (Build.VERSION.SDK_INT < 28) {
            tableRowEstimated.visibility = View.GONE
        }

        (imageViewAnimLevelBattery.drawable as AnimatedVectorDrawable).registerAnimationCallback( object:Animatable2.AnimationCallback(){
            override fun onAnimationEnd(drawable: Drawable?) {
                (imageViewAnimLevelBattery.drawable as AnimatedVectorDrawable).start()
            }
        })
        (imageViewAnimLevelBattery.drawable as AnimatedVectorDrawable).start()

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
        val notification = NotificationCompat.Builder(this, channelID).apply {
            setContentTitle(title)
            setContentText(text)
            setSmallIcon(R.drawable.ic_launcher_background)
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            setChannelId(channelID)
        }.build()

        notificationManager!!.notify(notificationID, notification)
    }

    private fun createNotificationChannel(id: String, name: String, description: String) {
        if(Build.VERSION.SDK_INT < 26 ){
            return
        }

        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(id, name, importance)

        channel.description = description
        notificationManager!!.createNotificationChannel(channel)
    }
}
