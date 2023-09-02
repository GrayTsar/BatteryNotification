package com.graytsar.batterynotification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ViewModelAlarm @Inject constructor(
    val db: AlarmDatabase,
    private val notificationManager: NotificationManager
) : ViewModel() {
    val status = MutableLiveData("")
    val health = MutableLiveData("")
    val technology = MutableLiveData("")
    val voltage = MutableLiveData("")
    val temperature = MutableLiveData("")
    val estimated = MutableLiveData("")
    val level = MutableLiveData("")
    val plugged = MutableLiveData("")
    val capacity = MutableLiveData("")

    val max = MutableLiveData(8)
    val min = MutableLiveData(3)
    val start = MutableLiveData(false)

    val waveViewProgress = MutableLiveData(0F)
    val waveViewWaveHeight = MutableLiveData(40)

    fun notify(context: Context, title: String, text: String) {
        val launchIntent =
            context.packageManager.getLaunchIntentForPackage("com.graytsar.batterynotification")
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )

        //switch does not update with MutableLiveData change, remove this feature till i think of something different that works
        //val deleteIntent = Intent(this, ModelBattery::class.java)
        //deleteIntent.action = "notification_cancelled"
        //val pendingDeleteIntent = PendingIntent.getBroadcast(this, 1, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val notification = NotificationCompat.Builder(context, channelID).apply {
            setContentTitle(title)
            setContentText(text)
            setSmallIcon(R.drawable.ic_battery_alert_black_24dp)
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            setContentIntent(pendingIntent)
            setChannelId(channelID)
        }.build()

        notificationManager.notify(notificationID, notification)
    }
}