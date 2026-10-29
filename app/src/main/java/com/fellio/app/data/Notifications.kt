package com.fellio.app.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.fellio.app.DidamApp
import com.fellio.app.MainActivity
import com.fellio.app.R
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit

object Notifier {
    const val CHANNEL = "new_episodes"
    private const val WORK = "episodes"

    fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = ctx.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL, "New episodes", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
    }

    fun schedule(ctx: Context) {
        val req = PeriodicWorkRequestBuilder<EpisodeWorker>(6, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.KEEP, req)
    }

    fun cancel(ctx: Context) {
        WorkManager.getInstance(ctx).cancelUniqueWork(WORK)
    }
}

class EpisodeWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as DidamApp
        val repo = app.repo
        return try {
            repo.refreshAll()
            val nm = NotificationManagerCompat.from(applicationContext)
            if (!nm.areNotificationsEnabled()) return Result.success()
            val fa = app.settings.lang.value == com.fellio.app.data.Lang.FA
            val today = java.time.LocalDate.now().toString()
            val groups = repo.pendingNotifications(today)
            val open = PendingIntent.getActivity(
                applicationContext, 0, Intent(applicationContext, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            for ((name, eps) in groups) {
                val text = if (eps.size == 1) {
                    val e = eps[0]
                    (if (fa) "فصل ${e.season} قسمت ${e.episode}" else "S${e.season} E${e.episode}") +
                        (if (e.name.isNotBlank()) " — ${e.name}" else "")
                } else {
                    if (fa) "${eps.size} قسمت جدید" else "${eps.size} new episodes"
                }
                val n = NotificationCompat.Builder(applicationContext, Notifier.CHANNEL)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(name)
                    .setContentText(text)
                    .setContentIntent(open)
                    .setAutoCancel(true)
                    .build()
                nm.notify(name.hashCode(), n)
                repo.markNotified(eps)
            }
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
