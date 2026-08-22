package com.focusloop.app.ui.insights

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusloop.app.data.repository.SessionRepository
import com.focusloop.app.domain.model.DailyStats
import com.focusloop.app.ui.components.softShadow
import com.focusloop.app.ui.theme.*
import compose.icons.FeatherIcons
import compose.icons.feathericons.BatteryCharging
import compose.icons.feathericons.Info
import compose.icons.feathericons.Smartphone
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class InsightsUiState(
    val weekStats: List<DailyStats> = emptyList(),
    val todayDistractionMs: Long = 0,
    val weekTotalDistractionMs: Long = 0,
    val weekTotalFocusMs: Long = 0,
    val topApps: List<com.focusloop.app.data.local.dao.AppUsageSummary> = emptyList(),
    val insight: String = ""
)

class InsightsViewModel(private val sessionRepository: SessionRepository) : ViewModel() {
    private val _state = MutableStateFlow(InsightsUiState())
    val state: StateFlow<InsightsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            sessionRepository.getLastSevenDaysStats().collect { stats ->
                val weekMs = stats.sumOf { it.totalDistractionMs }
                val weekFocusMs = stats.sumOf { it.totalFocusMs }
                val todayMs = stats.firstOrNull()?.totalDistractionMs ?: 0L
                _state.value = _state.value.copy(
                    weekStats = stats,
                    todayDistractionMs = todayMs,
                    weekTotalDistractionMs = weekMs,
                    weekTotalFocusMs = weekFocusMs,
                    insight = generateInsight(stats, todayMs)
                )
            }
        }
        viewModelScope.launch {
            val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            val topApps = sessionRepository.getTopDistractingApps(weekAgo)
            _state.value = _state.value.copy(topApps = topApps)
        }
    }

    private fun generateInsight(stats: List<DailyStats>, todayMs: Long): String {
        if (stats.isEmpty()) return "Complete your first focus session to see insights."
        val avgMs = if (stats.isNotEmpty()) stats.map { it.totalDistractionMs }.average() else 0.0
        val todayMin = todayMs / 60000
        return when {
            todayMin > 60 -> "You've spent over an hour distracted today. A focus session now could make a real difference."
            todayMin < 15 -> "Great self-awareness today! You've kept distraction under 15 minutes."
            stats.size >= 2 && stats[0].totalDistractionMs < stats[1].totalDistractionMs ->
                "Your distraction time is lower than yesterday. Keep it up!"
            else -> "Your average distraction this week is ${(avgMs / 60000).toInt()} minutes per day."
        }
    }
}

@Composable
fun InsightsScreen(viewModel: InsightsViewModel) {
    val state by viewModel.state.collectAsState()
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            "Insights",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black
        )

        Spacer(Modifier.height(24.dp))

        // Today stat
        InsightCard(
            title = "Today's distraction",
            value = formatMs(state.todayDistractionMs),
            icon = FeatherIcons.Smartphone,
            color = FocusOrange
        )

        Spacer(Modifier.height(16.dp))

        // Weekly bar chart
        Text("This week", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        WeeklyBarChart(stats = state.weekStats)

        Spacer(Modifier.height(24.dp))

        // Top distracting apps
        if (state.topApps.isNotEmpty()) {
            Text("Most distracting apps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            state.topApps.forEach { app ->
                AppRow(appName = app.appName, durationMs = app.totalMs, maxMs = state.topApps.first().totalMs)
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Recovered time
        InsightCard(
            title = "Time recovered this week",
            value = formatMs(state.weekTotalFocusMs),
            icon = FeatherIcons.BatteryCharging,
            color = FocusTeal
        )

        Spacer(Modifier.height(16.dp))

        // Insight
        if (state.insight.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(FeatherIcons.Info, contentDescription = null, tint = FocusPurple, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        state.insight,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightCard(title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().softShadow(cornerRadius = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

@Composable
private fun WeeklyBarChart(stats: List<DailyStats>) {
    if (stats.isEmpty()) {
        Text(
            "Your focus story starts today.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val maxMs = stats.maxOfOrNull { it.totalDistractionMs }?.takeIf { it > 0 } ?: 1L
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        stats.takeLast(7).forEachIndexed { i, stat ->
            val fraction = stat.totalDistractionMs.toFloat() / maxMs
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((80 * fraction.coerceAtLeast(0.05f)).dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(FocusOrange.copy(alpha = 0.7f + 0.3f * fraction))
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    dayLabels.getOrElse(i) { "" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AppRow(appName: String, durationMs: Long, maxMs: Long) {
    val fraction = if (maxMs > 0) durationMs.toFloat() / maxMs else 0f
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            appName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(100.dp),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(FocusOrange)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            formatMs(durationMs),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatMs(ms: Long): String {
    if (ms <= 0) return "0 min"
    val minutes = ms / 60000
    return if (minutes < 60) "${minutes}m" else "${minutes / 60}h ${minutes % 60}m"
}
