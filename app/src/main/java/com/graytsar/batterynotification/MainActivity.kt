package com.graytsar.batterynotification


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.databinding.DataBindingUtil
import androidx.room.Room
import com.google.android.material.switchmaterial.SwitchMaterial
import com.graytsar.batterynotification.databinding.ActivityMainBinding
import com.scwang.wave.MultiWaveHeader

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    val viewModelMain by viewModels<ViewModelMain>()

    private lateinit var waveView:MultiWaveHeader

    private var notificationManager:NotificationManager? = null
    private val channelID:String = "com.graytsar.batterynotification.Alarm"
    private val notificationID:Int = 101

    lateinit var model:ModelBattery
    var db:BatteryDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModelMain
        binding.lifecycleOwner = this

        setSupportActionBar(findViewById(R.id.toolbar))

        waveView = binding.waveView

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(channelID, "Alarm", "Notify Battery")

        db = Room.databaseBuilder(
            applicationContext,
            BatteryDatabase::class.java, "ModelBattery_Database"
        ).allowMainThreadQueries().build()

        val array = db!!.batteryDao().getModel()
        if(array.isNotEmpty()){
            model = array[0]
        } else {
            model = ModelBattery(0)
            model.id = db!!.batteryDao().insertModel(model)
        }

        viewModelMain.max.value = model.valMax
        viewModelMain.min.value = model.valMin
        viewModelMain.start.value = model.valSwitch



        //https://developer.android.com/reference/android/content/Intent#standard-broadcast-actions
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED)
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(BatteryReceiver(), intentFilter)

        /*
        val batteryReceiver = BatteryReceiver
        if(batteryReceiver.isRegistered){
            Log.d("DBG", "exists")
        } else {
            registerReceiver(batteryReceiver, intentFilter)
            Log.d("DBG", "no")
        }

         */


        if (Build.VERSION.SDK_INT < 28) {
            binding.tableRowEstimated.visibility = View.GONE
        }

        binding.seekBarMax.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModelMain.max.value = progress

                if(viewModelMain.max.value!! < viewModelMain.min.value!!){
                    viewModelMain.min.value = progress
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding.seekBarMin.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModelMain.min.value = progress

                if(viewModelMain.min.value!! > viewModelMain.min.value!!){
                    viewModelMain.max.value = progress
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        viewModelMain.waveViewProgress.observe(this, androidx.lifecycle.Observer {
            waveView.progress = it
        })

        viewModelMain.waveViewWaveHeight.observe(this, androidx.lifecycle.Observer {
            waveView.waveHeight = it
        })

        viewModelMain.max.observe(this, androidx.lifecycle.Observer {
            model.valMax = it
        })

        viewModelMain.min.observe(this, androidx.lifecycle.Observer {
            model.valMin = it
        })

        viewModelMain.start.observe(this, androidx.lifecycle.Observer {
            model.valSwitch = it
        })

        binding.switchStart.setOnClickListener { view ->
            viewModelMain.start.value = (view as SwitchMaterial).isChecked
        }
    }

    fun pushNotify(title:String, text:String){
        val launchIntent = packageManager.getLaunchIntentForPackage("com.graytsar.batterynotification")

        val pendingIntent: PendingIntent = if(Build.VERSION.SDK_INT >= 23) {
            PendingIntent.getActivity(this,0, launchIntent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT)
        } else {
            PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        }



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
