package com.focusloop.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.focusloop.app.ui.theme.FocusPurple
import com.focusloop.app.ui.theme.FocusTeal
import kotlin.math.cos
import kotlin.math.sin

/**
 * The FocusLoop mark: an open ring sweeping from purple to teal with a
 * leading dot, evoking a loop that always brings you back to focus.
 * Mirrors the app's launcher icon (see res/drawable/ic_launcher_foreground.xml)
 * so the brand mark is consistent everywhere it appears.
 */
@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    backgroundColor: androidx.compose.ui.graphics.Color? = androidx.compose.ui.graphics.Color(0xFF15122B)
) {
    val content: @Composable () -> Unit = {
        Canvas(modifier = Modifier.size(size)) {
            val diameter = this.size.minDimension
            val strokeWidth = diameter * 0.11f
            val radius = diameter / 2f - strokeWidth
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            val startAngleDeg = 125f
            val sweepDeg = 290f

            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(FocusPurple, FocusTeal, FocusPurple),
                    center = center
                ),
                startAngle = startAngleDeg,
                sweepAngle = sweepDeg,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            val endAngleRad = Math.toRadians((startAngleDeg + sweepDeg).toDouble())
            val dotCenter = Offset(
                center.x + radius * cos(endAngleRad).toFloat(),
                center.y + radius * sin(endAngleRad).toFloat()
            )
            drawCircle(color = FocusTeal, radius = strokeWidth * 0.65f, center = dotCenter)
        }
    }

    if (backgroundColor != null) {
        androidx.compose.foundation.layout.Box(
            modifier = modifier
                .clip(RoundedCornerShape(size * 0.28f))
                .background(backgroundColor),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            content()
        }
    } else {
        androidx.compose.foundation.layout.Box(modifier = modifier, contentAlignment = androidx.compose.ui.Alignment.Center) {
            content()
        }
    }
}
