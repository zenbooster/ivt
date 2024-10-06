package net.zenbooster.wrc.presentation

import android.app.AlarmManager
import android.app.Service
import android.app.Notification
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Intent
import android.os.IBinder
import android.os.Build
import android.widget.Toast
import android.os.Vibrator
import android.os.VibrationEffect;
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.lang.Math.sqrt
import android.graphics.Color
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.os.CountDownTimer
import android.os.PowerManager
import net.zenbooster.wrc.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.round

class MyService : Service() {
    var wakeLock: PowerManager.WakeLock? = null

    private fun createNotification(): Notification {
        val notificationChannelId = "ENDLESS SERVICE CHANNEL"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager;
            val channel = NotificationChannel(
                notificationChannelId,
                "Endless Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Endless Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            putExtra("type", type)
        }
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(this, 0, intent, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(this, 0, intent, FLAG_UPDATE_CURRENT)
        }

        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
            this,
            notificationChannelId
        ) else Notification.Builder(this)

        return builder
            .setContentTitle("WRC service")
            .setContentText("interval timer")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker("Ticker text")
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }

    override fun onDestroy()
    {
        Core.timer?.cancel()
        Core.timer = null

        wakeLock!!.release()
        Toast.makeText(this, "service destroyed", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate() {
        startForeground(1, createNotification())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        wakeLock = (getSystemService(POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WRC::MyWakelockTag").apply {
                    acquire()
                }
            }

        Core.timer = object : CountDownTimer(Core.totalSeconds * 1000, 1000) {
            override fun onTick(millisRemaining: Long) {
                val sec = round(millisRemaining / 1000.0).toInt()

                if (sec == 1)
                {
                    Core.timerColor.value = androidx.compose.ui.graphics.Color.Red

                    val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
                    val vibrationEffect1: VibrationEffect
                    vibrationEffect1 =
                        VibrationEffect.createOneShot(Core.VibrationDuration.value.toLong(), Core.VibrationLevel.value.toInt())

                    // it is safe to cancel other vibrations currently taking place
                    vibrator.cancel()
                    vibrator.vibrate(vibrationEffect1)
                }
                else
                {
                    Core.timerColor.value = Core.def_timerColor
                }

                val hours = sec / 3600 // Получение часов
                val minutes = (sec / 60) % 60 // Получение минут
                val seconds = sec % 60 // Получение секунд

                // Создаем LocalTime из частей
                val time = LocalTime.of(hours, minutes, seconds)
                Core.s_time.value = time.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            }

            override fun onFinish() {
                start()
            }
        }.start()

        Toast.makeText(this, "service started", Toast.LENGTH_SHORT).show()
        // If we get killed, after returning from here, restart
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}