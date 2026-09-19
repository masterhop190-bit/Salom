package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SpaceCardBg
import com.example.ui.theme.SpaceCardBorder

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = SpaceCardBg.copy(alpha = 0.85f),
    borderColor: Color = SpaceCardBorder,
    borderWidth: Dp = 1.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(
            borderWidth,
            Brush.linearGradient(
                colors = listOf(
                    borderColor,
                    CyberCyan.copy(alpha = 0.2f),
                    borderColor
                )
            )
        )
    ) {
        Box(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}
