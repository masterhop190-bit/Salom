package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun GlowingArcVisualizer(
    modifier: Modifier = Modifier,
    amplitude: Float = 0f,
    isListening: Boolean = false,
    isSpeaking: Boolean = false,
    isProcessing: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_anim")

    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening) 4000 else 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot1"
    )

    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking) 3000 else 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot2"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    val activeColor = when {
        isSpeaking -> NeonPurple
        isListening -> CyberCyan
        isProcessing -> NeonEmerald
        else -> ElectricBlue
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // 1. Rotating Vector HUD Canvas Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) * 0.88f
            val dynamicRadius = baseRadius * (if (isListening || isSpeaking) (1f + amplitude * 0.35f) else pulse)

            // Ambient Holographic Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        activeColor.copy(alpha = if (isListening || isSpeaking) 0.45f else 0.22f),
                        activeColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = dynamicRadius * 1.4f
                ),
                radius = dynamicRadius * 1.4f,
                center = center
            )

            // Outer Segmented Arcs (HUD Ring 1)
            val outerStroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            val segments = 8
            val sweepAngle = 28f
            for (i in 0 until segments) {
                val startAngle = rotation1 + i * (360f / segments)
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(CyberCyan, ElectricBlue, NeonPurple, CyberCyan),
                        center = center
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = outerStroke,
                    topLeft = Offset(center.x - dynamicRadius, center.y - dynamicRadius),
                    size = androidx.compose.ui.geometry.Size(dynamicRadius * 2, dynamicRadius * 2)
                )
            }

            // Middle Counter-Rotating HUD Arcs
            val midRadius = dynamicRadius * 0.78f
            val midStroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Square)
            val midSegments = 6
            for (i in 0 until midSegments) {
                val startAngle = rotation2 + i * (360f / midSegments)
                drawArc(
                    color = activeColor.copy(alpha = 0.75f),
                    startAngle = startAngle,
                    sweepAngle = 40f,
                    useCenter = false,
                    style = midStroke,
                    topLeft = Offset(center.x - midRadius, center.y - midRadius),
                    size = androidx.compose.ui.geometry.Size(midRadius * 2, midRadius * 2)
                )
            }

            // Corner Tactical Brackets
            val bracketSize = dynamicRadius * 0.92f
            val bStroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Square)
            val bLen = 16.dp.toPx()

            // Top-Left
            drawLine(activeColor, Offset(center.x - bracketSize, center.y - bracketSize + bLen), Offset(center.x - bracketSize, center.y - bracketSize), bStroke.width)
            drawLine(activeColor, Offset(center.x - bracketSize, center.y - bracketSize), Offset(center.x - bracketSize + bLen, center.y - bracketSize), bStroke.width)

            // Top-Right
            drawLine(activeColor, Offset(center.x + bracketSize - bLen, center.y - bracketSize), Offset(center.x + bracketSize, center.y - bracketSize), bStroke.width)
            drawLine(activeColor, Offset(center.x + bracketSize, center.y - bracketSize), Offset(center.x + bracketSize, center.y - bracketSize + bLen), bStroke.width)

            // Bottom-Left
            drawLine(activeColor, Offset(center.x - bracketSize, center.y + bracketSize - bLen), Offset(center.x - bracketSize, center.y + bracketSize), bStroke.width)
            drawLine(activeColor, Offset(center.x - bracketSize, center.y + bracketSize), Offset(center.x - bracketSize + bLen, center.y + bracketSize), bStroke.width)

            // Bottom-Right
            drawLine(activeColor, Offset(center.x + bracketSize - bLen, center.y + bracketSize), Offset(center.x + bracketSize, center.y + bracketSize), bStroke.width)
            drawLine(activeColor, Offset(center.x + bracketSize, center.y + bracketSize), Offset(center.x + bracketSize, center.y + bracketSize - bLen), bStroke.width)
        }

        // 2. Center JARVIS Holographic Blue Cube Visual
        Box(
            modifier = Modifier
                .size(136.dp)
                .scale(if (isListening || isSpeaking) (1f + amplitude * 0.15f) else pulse)
                .clip(RoundedCornerShape(22.dp))
                .border(2.dp, activeColor, RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.jarvis_cube),
                contentDescription = "JARVIS Cyber Cube",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Real-time Audio Waveform Overlay on Cube
            if (isListening || isSpeaking || isProcessing) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val wavePath = Path()
                    val waveWidth = size.width * 0.85f
                    val startX = (size.width - waveWidth) / 2f
                    val centerY = size.height / 2f
                    val points = 24
                    val amp = if (isListening) (amplitude * 24f + 6f) else 12f

                    for (i in 0..points) {
                        val progress = i.toFloat() / points
                        val x = startX + progress * waveWidth
                        val y = centerY + sin(progress * 3.2f * 2 * PI + wavePhase).toFloat() * amp * (1f - kotlin.math.abs(progress - 0.5f) * 1.5f).coerceAtLeast(0f)

                        if (i == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
                    }

                    drawPath(
                        path = wavePath,
                        color = Color.White,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}
