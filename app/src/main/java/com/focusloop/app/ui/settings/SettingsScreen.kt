package com.focusloop.app.ui.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusloop.app.data.datastore.AuthDataStore
import com.focusloop.app.data.datastore.AuthState
import com.focusloop.app.data.datastore.SettingsDataStore
import com.focusloop.app.domain.model.UserSettings
import com.focusloop.app.service.FocusMonitoringService
import com.focusloop.app.ui.theme.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import compose.icons.FeatherIcons
import compose.icons.feathericons.Bell
import compose.icons.feathericons.BarChart2
import compose.icons.feathericons.Code
import compose.icons.feathericons.ExternalLink
import compose.icons.feathericons.Layers
import compose.icons.feathericons.Lock
import compose.icons.feathericons.LogOut
import compose.icons.feathericons.Moon
import compose.icons.feathericons.PlayCircle
import compose.icons.feathericons.Smartphone

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val authDataStore: AuthDataStore
) : ViewModel() {
    val settings: StateFlow<UserSettings> = settingsDataStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    val authState: StateFlow<AuthState> = authDataStore.authState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthState())

    fun update(settings: UserSettings) {
        viewModelScope.launch { settingsDataStore.updateSettings(settings) }
    }

    fun triggerTestIntervention(context: android.content.Context) {
        // Send broadcast to monitoring service to trigger a demo intervention
        context.sendBroadcast(Intent(FocusMonitoringService.ACTION_TRIGGER_DEMO).apply {
            setPackage(context.packageName)
        })
    }

    fun logOut(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authDataStore.logOut()
            onLoggedOut()
        }
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onLoggedOut: () -> Unit = {}) {
    val settings by viewModel.settings.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)

        Spacer(Modifier.height(24.dp))

        // Account
        SettingsSection("Account") {
            SettingsInfoRow("Signed in as", authState.email)
            SettingsDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.logOut(onLoggedOut) }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(FeatherIcons.LogOut, null, modifier = Modifier.size(20.dp), tint = FocusRed)
                Spacer(Modifier.width(14.dp))
                Text("Log out", style = MaterialTheme.typography.titleSmall, color = FocusRed)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Monitoring section
        SettingsSection("Monitoring") {
            SettingsSliderRow(
                label = "Intervention threshold",
                sublabel = "${settings.interventionThresholdMs / 60000} minutes",
                value = settings.interventionThresholdMs / 60000f,
                valueRange = 1f..30f,
                onValueChange = { min ->
                    viewModel.update(settings.copy(interventionThresholdMs = min.toLong() * 60000))
                }
            )
            SettingsDivider()
            SettingsSliderRow(
                label = "Cooldown after intervention",
                sublabel = "${settings.cooldownDurationMs / 60000} minutes",
                value = settings.cooldownDurationMs / 60000f,
                valueRange = 5f..60f,
                onValueChange = { min ->
                    viewModel.update(settings.copy(cooldownDurationMs = min.toLong() * 60000))
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        // Notifications section
        SettingsSection("Notifications & Feedback") {
            SettingsToggleRow(
                icon = FeatherIcons.Bell,
                label = "Notifications",
                value = settings.notificationsEnabled,
                onToggle = { viewModel.update(settings.copy(notificationsEnabled = it)) }
            )
            SettingsDivider()
            SettingsToggleRow(
                icon = FeatherIcons.Smartphone,
                label = "Vibration",
                value = settings.vibrationEnabled,
                onToggle = { viewModel.update(settings.copy(vibrationEnabled = it)) }
            )
        }

        Spacer(Modifier.height(16.dp))

        // Appearance
        SettingsSection("Appearance") {
            SettingsToggleRow(
                icon = FeatherIcons.Moon,
                label = "Dark mode",
                value = settings.darkModeEnabled,
                onToggle = { viewModel.update(settings.copy(darkModeEnabled = it)) }
            )
        }

        Spacer(Modifier.height(16.dp))

        // Permissions
        SettingsSection("Permissions") {
            SettingsActionRow(
                icon = FeatherIcons.BarChart2,
                label = "Usage Access",
                sublabel = "Required for distraction detection"
            ) {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
            SettingsDivider()
            SettingsActionRow(
                icon = FeatherIcons.Layers,
                label = "Display over other apps",
                sublabel = "For intervention overlay"
            ) {
                context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Developer Mode
        SettingsSection("Developer Mode") {
            SettingsToggleRow(
                icon = FeatherIcons.Code,
                label = "Developer mode",
                value = settings.developerModeEnabled,
                onToggle = { viewModel.update(settings.copy(developerModeEnabled = it)) }
            )
            if (settings.developerModeEnabled) {
                SettingsDivider()
                SettingsSliderRow(
                    label = "Demo threshold",
                    sublabel = "${settings.demoThresholdMs / 1000} seconds",
                    value = settings.demoThresholdMs / 1000f,
                    valueRange = 5f..60f,
                    onValueChange = { sec ->
                        viewModel.update(settings.copy(demoThresholdMs = sec.toLong() * 1000))
                    }
                )
                SettingsDivider()
                SettingsActionRow(
                    icon = FeatherIcons.PlayCircle,
                    label = "Trigger test intervention",
                    sublabel = "Simulate an intervention now",
                    actionColor = FocusPurple
                ) {
                    viewModel.triggerTestIntervention(context)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Privacy
        SettingsSection("Privacy") {
            Row(
                modifier = Modifier.padding(4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(FeatherIcons.Lock, null, tint = FocusGreen, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    "Your FocusLoop data stays on your device. No usage data, goals, or personal information is ever sent to any server.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // About
        SettingsSection("About") {
            SettingsInfoRow("FocusLoop", "v1.0 — MVP")
            SettingsDivider()
            SettingsInfoRow("Architecture", "Kotlin · Jetpack Compose · MVVM · Room")
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), content = content)
    }
}

@Composable
private fun SettingsDivider() = Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

@Composable
private fun SettingsToggleRow(icon: ImageVector, label: String, value: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = FocusPurple)
        Spacer(Modifier.width(14.dp))
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
        Switch(
            checked = value,
            onCheckedChange = onToggle,
            modifier = Modifier.height(24.dp),
            colors = SwitchDefaults.colors(checkedTrackColor = FocusPurple)
        )
    }
}

@Composable
private fun SettingsSliderRow(label: String, sublabel: String, value: Float, valueRange: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row {
            Text(label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(sublabel, style = MaterialTheme.typography.labelMedium, color = FocusPurple)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(thumbColor = FocusPurple, activeTrackColor = FocusPurple),
            steps = 0
        )
    }
}

@Composable
private fun SettingsActionRow(icon: ImageVector, label: String, sublabel: String, actionColor: Color = MaterialTheme.colorScheme.primary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = actionColor)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = actionColor)
            Text(sublabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(FeatherIcons.ExternalLink, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
