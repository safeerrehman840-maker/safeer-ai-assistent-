package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import kotlin.math.cos
import kotlin.math.sin

enum class AssistantStatus {
    IDLE,
    LISTENING,
    THINKING,
    WORKING,
    COMPLETED,
    NEEDS_PERMISSION
}

@Composable
fun AnimatedAssistantOrb(
    status: AssistantStatus,
    size: Dp = 160.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbMotion")

    // Pulsing scale for breathing effect
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (status == AssistantStatus.LISTENING) 0.85f else 0.95f,
        targetValue = if (status == AssistantStatus.LISTENING) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (status) {
                    AssistantStatus.LISTENING -> 600
                    AssistantStatus.THINKING -> 800
                    AssistantStatus.WORKING -> 500
                    else -> 1800
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbPulse"
    )

    // Continuous rotation for particle rings
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (status) {
                    AssistantStatus.THINKING -> 2000
                    AssistantStatus.WORKING -> 1200
                    AssistantStatus.LISTENING -> 3000
                    else -> 8000
                },
                easing = LinearEasing
            )
        ),
        label = "OrbRotation"
    )

    // Glow alpha oscillation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbGlow"
    )

    val (coreColor, ringColor) = when (status) {
        AssistantStatus.LISTENING -> Pair(EmeraldPrimary, CyanAccent)
        AssistantStatus.THINKING -> Pair(CyanAccent, Color(0xFF818CF8))
        AssistantStatus.WORKING -> Pair(Color(0xFFF59E0B), EmeraldLight)
        AssistantStatus.COMPLETED -> Pair(EmeraldLight, EmeraldPrimary)
        AssistantStatus.NEEDS_PERMISSION -> Pair(Color(0xFFEF4444), Color(0xFFF87171))
        AssistantStatus.IDLE -> Pair(EmeraldPrimary, CyanAccent)
    }

    Box(
        modifier = Modifier
            .size(size)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (this.size.minDimension / 2f) * 0.7f

            // Outer ethereal radial glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreColor.copy(alpha = glowAlpha * 0.45f),
                        ringColor.copy(alpha = glowAlpha * 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.45f * pulseScale
                ),
                radius = baseRadius * 1.45f * pulseScale,
                center = center
            )

            // Dynamic rotating orbit rings
            val rad = Math.toRadians(rotationAngle.toDouble())
            val cosVal = cos(rad).toFloat()
            val sinVal = sin(rad).toFloat()

            drawCircle(
                color = ringColor.copy(alpha = 0.6f),
                radius = baseRadius * 1.12f * pulseScale,
                center = center,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Elliptical gyroscopic orbital lines
            for (i in 0 until 3) {
                val ringOffset = (i * 60f + rotationAngle) % 360f
                val rAngle = Math.toRadians(ringOffset.toDouble())
                val orbitCenter = Offset(
                    center.x + (cos(rAngle) * 8).toFloat(),
                    center.y + (sin(rAngle) * 8).toFloat()
                )
                drawCircle(
                    color = (if (i % 2 == 0) coreColor else ringColor).copy(alpha = 0.4f),
                    radius = baseRadius * (0.85f + i * 0.1f) * pulseScale,
                    center = orbitCenter,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Core Energy Sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        coreColor,
                        ringColor.copy(alpha = 0.8f),
                        Color.Transparent
                    ),
                    center = Offset(center.x - 10f * cosVal, center.y - 10f * sinVal),
                    radius = baseRadius * 0.85f * pulseScale
                ),
                radius = baseRadius * 0.78f * pulseScale,
                center = center
            )

            // Inner neural core bright sparkle
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = baseRadius * 0.22f * (1.2f - pulseScale * 0.2f),
                center = center
            )
        }
    }
}
