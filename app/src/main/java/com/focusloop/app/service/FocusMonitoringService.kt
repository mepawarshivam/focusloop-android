package com.focusloop.app.service

import android.app.*
import android.app.usage.UsageStatsManager
import android.content.*
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.focusloop.app.FocusLoopApplication
import com.focusloop.app.R
import com.focusloop.app.domain.model.DistractionSession
import com.focusloop.app.ui.intervention.InterventionActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

/**
 * FocusMonitoringService runs as a foreground service so Android allows
 * it to remain alive and poll UsageStatsManager at a reasonable interval.
 *
 * Why a foreground service?
 * UsageStatsManager requires the app to be running. Background processes
 * on modern Android are aggressively killed — a foreground service with a
 * persistent notification is the supported way to keep detection alive.
 *
 * Battery consciousness:
 * We poll every 15 seconds — not 1 second. Between polls the CPU is idle.
 * This is sufficient to detect sessions that have reached the threshold.
 */
class FocusMonitoringService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "focusloop_monitoring"
        const val ACTION_STOP = "com.focusloop.app.STOP_MONITORING"
        const val ACTION_TRIGGER_DEMO = "com.focusloop.app.TRIGGER_DEMO"
        private const val POLL_INTERVAL_MS = 15_000L // 15 seconds
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var monitoringJob: Job? = null

    // Tracks the current distraction session state
    private var currentSession: DistractionSession? = null
    private var lastInterventionTime: Long = 0L
    private var cooldownUntil: Long = 0L

    private val demoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_TRIGGER_DEMO -> triggerDemoIntervention()
                ACTION_STOP -> stopSelf()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        ContextCompat.registerReceiver(
            this,
            demoReceiver,
            IntentFilter().apply {
                addAction(ACTION_TRIGGER_DEMO)
                addAction(ACTION_STOP)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startMonitoring()
        return START_STICKY // Re-start if killed by system
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        monitoringJob?.cancel()
        serviceScope.cancel()
        try { unregisterReceiver(demoReceiver) } catch (_: Exception) {}
        super.onDestroy()
    }

    private fun startMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            while (isActive) {
                checkForegroundApp()
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private suspend fun checkForegroundApp() {
        val app = application as FocusLoopApplication
        val settings = app.settingsDataStore.settings.first()

        if (!settings.monitoringEnabled) return
        if (System.currentTimeMillis() < cooldownUntil) return

        val foregroundPackage = getForegroundPackage() ?: return

        // Use demo threshold if developer mode is enabled
        val threshold = if (settings.developerModeEnabled) settings.demoThresholdMs
                        else settings.interventionThresholdMs

        if (foregroundPackage in settings.monitoredPackages) {
            val now = System.currentTimeMillis()

            if (currentSession == null || currentSession?.packageName != foregroundPackage) {
                // New distraction session started
                currentSession = DistractionSession(
                    packageName = foregroundPackage,
                    appName = getAppName(foregroundPackage),
                    startedAt = now
                )
                val sessionId = app.sessionRepository.saveDistractionSession(currentSession!!)
                currentSession = currentSession?.copy(id = sessionId)
            } else {
                // Existing session — check duration
                val duration = now - (currentSession?.startedAt ?: now)
                currentSession = currentSession?.copy(durationMs = duration)

                if (duration >= threshold && !currentSession!!.interventionTriggered) {
                    triggerIntervention(currentSession!!, settings, app)
                }
            }
        } else {
            // User left the monitored app — end session
            currentSession?.let { session ->
                val ended = session.copy(
                    endedAt = System.currentTimeMillis(),
                    durationMs = System.currentTimeMillis() - session.startedAt
                )
                app.sessionRepository.updateDistractionSession(ended)
            }
            currentSession = null
        }
    }

    private suspend fun triggerIntervention(
        session: DistractionSession,
        settings: com.focusloop.app.domain.model.UserSettings,
        app: FocusLoopApplication
    ) {
        // Mark session as having triggered intervention
        app.sessionRepository.updateDistractionSession(
            session.copy(interventionTriggered = true)
        )
        currentSession = currentSession?.copy(interventionTriggered = true)

        // Set cooldown
        cooldownUntil = System.currentTimeMillis() + settings.cooldownDurationMs
        lastInterventionTime = System.currentTimeMillis()

        // Record intervention count
        app.settingsDataStore.recordIntervention()

        // Get top goal
        val topGoal = app.goalRepository.getTopGoal()

        // Launch intervention activity
        val intent = InterventionActivity.createIntent(
            context = this,
            packageName = session.packageName,
            appName = session.appName,
            durationMs = session.durationMs,
            goalTitle = topGoal?.title ?: "",
            goalId = topGoal?.id ?: -1L
        )
        startActivity(intent)

        // Update stats
        app.sessionRepository.updateDailyStats(
            date = getDayStart(),
            distractionMs = session.durationMs,
            interventions = 1
        )
    }

    private fun triggerDemoIntervention() {
        serviceScope.launch {
            val app = application as FocusLoopApplication
            val settings = app.settingsDataStore.settings.first()
            val topGoal = app.goalRepository.getTopGoal()
            val intent = InterventionActivity.createIntent(
                context = this@FocusMonitoringService,
                packageName = "com.instagram.android",
                appName = "Instagram",
                durationMs = settings.demoThresholdMs,
                goalTitle = topGoal?.title ?: "Finish my project",
                goalId = topGoal?.id ?: -1L
            )
            startActivity(intent)
        }
    }

    private fun getForegroundPackage(): String? {
        return try {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val now = System.currentTimeMillis()
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                now - 1000 * 10, // Last 10 seconds
                now
            )
            stats?.filter { it.lastTimeUsed > 0 }
                ?.maxByOrNull { it.lastTimeUsed }
                ?.packageName
        } catch (e: Exception) {
            null
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        } catch (_: Exception) {
            packageName.substringAfterLast(".")
        }
    }

    private fun getDayStart(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "FocusLoop Monitoring",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps FocusLoop running to detect distraction"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val mainIntent = PendingIntent.getActivity(
            this, 0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getBroadcast(
            this, 1,
            Intent(ACTION_STOP).setPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FocusLoop is protecting your attention")
            .setContentText("Monitoring active")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setContentIntent(mainIntent)
            .addAction(android.R.drawable.ic_delete, "Stop", stopIntent)
            .setSilent(true)
            .build()
    }
}
