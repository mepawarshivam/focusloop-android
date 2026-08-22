package com.focusloop.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.focusloop.app.FocusLoopApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Restarts monitoring after device reboot — only if the user had monitoring enabled.
 * We never silently start intrusive behavior after a reboot without prior user consent.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val app = context.applicationContext as FocusLoopApplication
        CoroutineScope(Dispatchers.IO).launch {
            val settings = app.settingsDataStore.settings.first()

            if (settings.monitoringEnabled) {
                val serviceIntent = Intent(context, FocusMonitoringService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
