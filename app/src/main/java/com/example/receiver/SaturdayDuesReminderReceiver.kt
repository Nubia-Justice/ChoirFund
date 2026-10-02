package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.ChoirDatabase
import com.example.ui.util.ChoirStrings
import com.example.util.SaturdayReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SaturdayDuesReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        if (action == Intent.ACTION_BOOT_COMPLETED) {
            SaturdayReminderScheduler.scheduleNextSaturdayAlarm(context)
            return
        }

        if (action == SaturdayReminderScheduler.ACTION_SATURDAY_DUES_REMINDER ||
            action == SaturdayReminderScheduler.ACTION_TEST_REMINDER
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = ChoirDatabase.getDatabase(context)
                    val dao = db.choirDao()
                    val members = dao.getAllMembers().firstOrNull() ?: emptyList()
                    val contributions = dao.getAllContributions().firstOrNull() ?: emptyList()
                    val settings = dao.getSettings().firstOrNull()

                    val duesWeekStart = ChoirStrings.getDuesWeekStart()
                    val activeMembers = members.filter { it.isActive }
                    
                    // Unpaid active members this week (< 200 FCFA)
                    val unpaidMembers = activeMembers.filter { member ->
                        val duesPaid = contributions
                            .filter { it.memberId == member.id && it.type == "DUES" && it.date >= duesWeekStart }
                            .sumOf { it.amount }
                        duesPaid < 200L
                    }

                    val isFr = settings?.language == "FR"
                    val choirName = settings?.choirName ?: "Choir Funds"

                    showNotification(
                        context = context,
                        unpaidCount = unpaidMembers.size,
                        totalActiveCount = activeMembers.size,
                        choirName = choirName,
                        isFr = isFr
                    )

                    // Re-schedule next Saturday alarm
                    SaturdayReminderScheduler.scheduleNextSaturdayAlarm(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private fun showNotification(
        context: Context,
        unpaidCount: Int,
        totalActiveCount: Int,
        choirName: String,
        isFr: Boolean
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SaturdayReminderScheduler.CHANNEL_ID,
                if (isFr) "Rappels cotisations du samedi" else "Saturday Dues Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = if (isFr)
                    "Notifications pour rappeler aux membres leurs cotisations le samedi"
                else
                    "Notifications to remind choir members of weekly dues on Saturdays"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap action -> open app with Saturday Reminders dialog
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(SaturdayReminderScheduler.EXTRA_OPEN_SATURDAY_REMINDERS, true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            SaturdayReminderScheduler.REQUEST_CODE_NOTIFICATION,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val title = if (isFr) {
            "🔔 $choirName : Cotisations du samedi"
        } else {
            "🔔 $choirName: Saturday Weekly Dues"
        }

        val content = if (isFr) {
            if (unpaidCount > 0) {
                "$unpaidCount sur $totalActiveCount membre(s) n'ont pas encore payé (min. 200 FCFA). Touchez pour envoyer les rappels WhatsApp/SMS."
            } else {
                "Tous les membres actifs sont à jour de cotisation cette semaine ! 🎉"
            }
        } else {
            if (unpaidCount > 0) {
                "$unpaidCount of $totalActiveCount member(s) have unpaid dues (min. 200 FCFA). Tap to send WhatsApp or SMS reminders."
            } else {
                "All active members have paid their weekly dues! 🎉"
            }
        }

        val notification = NotificationCompat.Builder(context, SaturdayReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(SaturdayReminderScheduler.NOTIFICATION_ID, notification)
    }
}
