package com.intervaltimer.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.intervaltimer.app.MainActivity
import com.intervaltimer.app.R
import com.intervaltimer.app.ui.screens.formatTime
import com.intervaltimer.app.viewmodel.TimerPhase
import com.intervaltimer.app.viewmodel.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TimerService : Service() {

    companion object {
        const val CHANNEL_ID = "interval_timer_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_PAUSE_RESUME = "com.intervaltimer.app.PAUSE_RESUME"
        const val ACTION_SKIP_FORWARD = "com.intervaltimer.app.SKIP_FORWARD"
        const val ACTION_SKIP_BACKWARD = "com.intervaltimer.app.SKIP_BACKWARD"
        const val ACTION_STOP = "com.intervaltimer.app.STOP"

        fun start(context: Context) {
            context.startForegroundService(Intent(context, TimerService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TimerService::class.java))
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var wakeLock: PowerManager.WakeLock? = null
    private var observeJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE_RESUME -> TimerManager.togglePause()
            ACTION_SKIP_FORWARD -> TimerManager.skipForward()
            ACTION_SKIP_BACKWARD -> TimerManager.skipBackward()
            ACTION_STOP -> {
                TimerManager.stopWorkout()
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                startForeground(NOTIFICATION_ID, buildNotification(TimerManager.state.value))
                observeState()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        observeJob?.cancel()
        serviceScope.cancel()
        releaseWakeLock()
        super.onDestroy()
    }

    private fun observeState() {
        observeJob?.cancel()
        observeJob = serviceScope.launch {
            TimerManager.state.collectLatest { state ->
                if (!state.isRunning && !state.isPaused) {
                    stopSelf()
                    return@collectLatest
                }
                updateNotification(state)
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Interval Timer",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Shows timer progress during workouts"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(state: TimerState): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val phaseName = if (state.currentPhase == TimerPhase.WORK) "WORK" else "REST"
        val title = "$phaseName ${state.currentSet}/${state.sets}"
        val text = "${formatTime(state.remainingSeconds)} remaining"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)

        // Skip backward action
        builder.addAction(
            NotificationCompat.Action.Builder(
                null,
                "Prev",
                actionPendingIntent(ACTION_SKIP_BACKWARD, 1),
            ).build()
        )

        // Pause/Resume action
        val pauseText = if (state.isPaused) "Resume" else "Pause"
        builder.addAction(
            NotificationCompat.Action.Builder(
                null,
                pauseText,
                actionPendingIntent(ACTION_PAUSE_RESUME, 2),
            ).build()
        )

        // Skip forward action
        builder.addAction(
            NotificationCompat.Action.Builder(
                null,
                "Next",
                actionPendingIntent(ACTION_SKIP_FORWARD, 3),
            ).build()
        )

        // Stop action
        builder.addAction(
            NotificationCompat.Action.Builder(
                null,
                "Stop",
                actionPendingIntent(ACTION_STOP, 4),
            ).build()
        )

        return builder.build()
    }

    private fun actionPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, TimerService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun updateNotification(state: TimerState) {
        val notification = buildNotification(state)
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(PowerManager::class.java)
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "IntervalTimer::TimerWakeLock").apply {
            acquire(60 * 60 * 1000L) // 1 hour max
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }
}
