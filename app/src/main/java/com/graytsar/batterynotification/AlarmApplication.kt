package com.graytsar.batterynotification

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

const val channelID: String = "com.graytsar.batterynotification.alarm"
const val notificationID: Int = 101

@HiltAndroidApp
class AlarmApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    /**
     * Creates the notification channels for the app.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val loggingChannel = NotificationChannel(
            channelID,
            "Alarm",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notify Battery"
        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(loggingChannel)
    }
}