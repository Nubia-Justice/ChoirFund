package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.receiver.SaturdayDuesReminderReceiver
import java.util.Calendar

object SaturdayReminderScheduler {

    const val ACTION_SATURDAY_DUES_REMINDER = "com.example.ACTION_SATURDAY_DUES_REMINDER"
    const val ACTION_TEST_REMINDER = "com.example.ACTION_TEST_REMINDER"
    const val EXTRA_OPEN_SATURDAY_REMINDERS = "extra_open_saturday_reminders"
    const val CHANNEL_ID = "choir_saturday_dues"
    const val NOTIFICATION_ID = 23701
    const val REQUEST_CODE_ALARM = 23702
    const val REQUEST_CODE_NOTIFICATION = 23703

    /**
     * Calculates the timestamp of the upcoming Saturday at 09:00:00 AM local time.
     * If today is Saturday and it's before 9am, returns today 9am.
     * If today is Saturday and it's after 9am, returns next Saturday 9am.
     */
    fun calculateNextSaturdayTimestamp(currentTime: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = currentTime
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek == Calendar.SATURDAY) {
            if (currentTime >= cal.timeInMillis) {
                // Already passed 9am on Saturday, schedule for 7 days ahead
                cal.add(Calendar.DAY_OF_YEAR, 7)
            }
        } else {
            val daysUntilSaturday = (Calendar.SATURDAY - dayOfWeek + 7) % 7
            cal.add(Calendar.DAY_OF_YEAR, daysUntilSaturday)
        }

        return cal.timeInMillis
    }

    /**
     * Schedules the next Saturday 9:00 AM alarm.
     */
    fun scheduleNextSaturdayAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val targetTime = calculateNextSaturdayTimestamp()

        val intent = Intent(context, SaturdayDuesReminderReceiver::class.java).apply {
            action = ACTION_SATURDAY_DUES_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_ALARM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetTime,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    targetTime,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Triggers an immediate reminder notification for testing purposes.
     */
    fun triggerImmediateTest(context: Context) {
        val intent = Intent(context, SaturdayDuesReminderReceiver::class.java).apply {
            action = ACTION_TEST_REMINDER
        }
        context.sendBroadcast(intent)
    }

    /**
     * Cancels any scheduled reminder alarm.
     */
    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, SaturdayDuesReminderReceiver::class.java).apply {
            action = ACTION_SATURDAY_DUES_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_ALARM,
            intent,
            PendingIntent.FLAG_NO_CREATE or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
