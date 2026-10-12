package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.local.AlertHistoryEntity
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationHelper {

    const val CHANNEL_PRICE_DROPS = "channel_price_drops"
    const val CHANNEL_RELEASES = "channel_upcoming_releases"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val priceDropChannel = NotificationChannel(
                CHANNEL_PRICE_DROPS,
                "Price Drop Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time alerts when tracked games drop below your target price"
                enableLights(true)
                enableVibration(true)
            }

            val releaseChannel = NotificationChannel(
                CHANNEL_RELEASES,
                "Upcoming Game Releases",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders and alerts for upcoming game launch dates"
            }

            notificationManager.createNotificationChannel(priceDropChannel)
            notificationManager.createNotificationChannel(releaseChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showPriceDropNotification(
        context: Context,
        gameId: String,
        gameTitle: String,
        oldPrice: Double,
        newPrice: Double,
        storeName: String,
        currencySymbol: String = "$"
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("target_game_id", gameId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            gameId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val savings = oldPrice - newPrice
        val savingsPercent = if (oldPrice > 0) ((savings / oldPrice) * 100).toInt() else 0

        val contentTitle = "🔥 Price Drop: $gameTitle!"
        val contentText = "Now $currencySymbol${"%.2f".format(newPrice)} (was $currencySymbol${"%.2f".format(oldPrice)}, saved $savingsPercent%) at $storeName"

        val notification = NotificationCompat.Builder(context, CHANNEL_PRICE_DROPS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$contentText.\nTap to view physical & digital purchase links.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (hasNotificationPermission(context)) {
            notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
        }

        // Persist to in-app alert history
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getInstance(context).alertHistoryDao().insertAlert(
                AlertHistoryEntity(
                    gameId = gameId,
                    gameTitle = gameTitle,
                    alertType = "PRICE_DROP",
                    oldPrice = oldPrice,
                    newPrice = newPrice,
                    storeName = storeName,
                    message = contentText,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun showReleaseAlertNotification(
        context: Context,
        gameId: String,
        gameTitle: String,
        releaseDate: String,
        platforms: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("target_game_id", gameId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            gameId.hashCode() + 1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentTitle = "🚀 Launch Alert: $gameTitle"
        val contentText = "Releases on $releaseDate for $platforms! Pre-orders & digital editions live."

        val notification = NotificationCompat.Builder(context, CHANNEL_RELEASES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$contentText\nCompare physical & digital retail prices now.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (hasNotificationPermission(context)) {
            notificationManager.notify((System.currentTimeMillis() % 100000).toInt() + 1, notification)
        }

        // Persist to alert history
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getInstance(context).alertHistoryDao().insertAlert(
                AlertHistoryEntity(
                    gameId = gameId,
                    gameTitle = gameTitle,
                    alertType = "UPCOMING_RELEASE",
                    storeName = "Global Launch",
                    message = contentText,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
