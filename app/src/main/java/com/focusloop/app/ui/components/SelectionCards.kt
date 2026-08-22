package com.focusloop.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.focusloop.app.domain.model.DistractingApp
import com.focusloop.app.ui.theme.FocusPurple
import compose.icons.FeatherIcons
import compose.icons.feathericons.CheckCircle
import compose.icons.feathericons.Circle

@Composable
fun HobbySelectionCard(
    hobby: String,
    icon: ImageVector,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val borderColor = if (isSelected) FocusPurple else MaterialTheme.colorScheme.outline
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = FocusPurple, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(hobby, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
        if (isSelected) {
            Icon(FeatherIcons.CheckCircle, null, tint = FocusPurple, modifier = Modifier.size(22.dp))
        } else {
            Icon(FeatherIcons.Circle, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun AppSelectionCard(app: DistractingApp, isSelected: Boolean, onToggle: () -> Unit) {
    val borderColor = if (isSelected) FocusPurple else MaterialTheme.colorScheme.outline
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val context = LocalContext.current
    val realIcon = remember(app.packageName) {
        runCatching { context.packageManager.getApplicationIcon(app.packageName) }
            .getOrNull()
            ?.let { it.toBitmap().asImageBitmap() }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (realIcon != null) {
            Image(
                bitmap = realIcon,
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(app.colorArgb)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    app.displayName.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Text(app.displayName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
        if (isSelected) {
            Icon(FeatherIcons.CheckCircle, null, tint = FocusPurple, modifier = Modifier.size(22.dp))
        } else {
            Icon(FeatherIcons.Circle, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(22.dp))
        }
    }
}
