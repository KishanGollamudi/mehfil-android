package com.lostf1sh.pixelplayeross.data.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import com.lostf1sh.pixelplayeross.R

@UnstableApi
class MehfilDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    CHANNEL_ID,
    R.string.app_name,
    0
) {
    override fun getDownloadManager(): DownloadManager {
        return MehfilDownloadProvider.get(this).manager
    }

    override fun getScheduler(): androidx.media3.exoplayer.scheduler.Scheduler? = null

    override fun getForegroundNotification(
        downloads: List<androidx.media3.exoplayer.offline.Download>,
        notMetRequirements: Int
    ): Notification {
        createChannel(applicationContext)
        val completed = downloads.count { it.state == androidx.media3.exoplayer.offline.Download.STATE_COMPLETED }
        val active = downloads.count { it.state == androidx.media3.exoplayer.offline.Download.STATE_DOWNLOADING }
        return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Mehfil downloads")
            .setContentText("$completed completed, $active active")
            .setOngoing(active > 0)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "mehfil_downloads"
        const val FOREGROUND_NOTIFICATION_ID = 7412

        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(NotificationManager::class.java)
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Media downloads",
                    NotificationManager.IMPORTANCE_LOW
                )
                manager.createNotificationChannel(channel)
            }
        }
    }
}

object MehfilDownloadProvider {
    private var manager: MehfilDownloadManager? = null

    fun get(context: Context): MehfilDownloadManager {
        return synchronized(this) {
            manager ?: MehfilDownloadManager(context.applicationContext).also {
                manager = it
            }
        }
    }
}
