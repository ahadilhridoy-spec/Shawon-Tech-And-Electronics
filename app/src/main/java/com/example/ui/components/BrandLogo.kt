package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandCyanGlow
import com.example.ui.theme.BrandCyanLight
import com.example.ui.theme.BrandSilver
import com.example.ui.theme.BrandWhite
import com.example.ui.theme.TechDarkBg

/**
 * Official Shawon Tech & Electronic Logo Emblem
 * Precisely recreates the stylized interconnected "S" ribbon:
 * - Top loop: Electric Cyan metallic gradient
 * - Bottom loop: Crisp White metallic bevel
 */
@Composable
fun ShawonBrandEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = w * 0.19f

        val cyanBrush = Brush.linearGradient(
            colors = listOf(BrandCyanGlow, BrandCyan, Color(0xFF008FA8)),
            start = Offset(w * 0.1f, 0f),
            end = Offset(w * 0.9f, h * 0.5f)
        )

        val whiteBrush = Brush.linearGradient(
            colors = listOf(Color.White, Color(0xFFF1F5F9), Color(0xFFCBD5E1)),
            start = Offset(w * 0.2f, h * 0.5f),
            end = Offset(w * 0.9f, h)
        )

        // Top Cyan Hook of the "S"
        val topPath = Path().apply {
            // Start from center top inner bar
            moveTo(w * 0.52f, h * 0.44f)
            lineTo(w * 0.32f, h * 0.44f)
            // Curve up to left
            cubicTo(
                w * 0.16f, h * 0.44f,
                w * 0.16f, h * 0.14f,
                w * 0.42f, h * 0.14f
            )
            // Line along top
            lineTo(w * 0.68f, h * 0.14f)
        }

        // Bottom White Hook of the "S"
        val bottomPath = Path().apply {
            // Start from center bottom inner bar
            moveTo(w * 0.48f, h * 0.56f)
            lineTo(w * 0.68f, h * 0.56f)
            // Curve down to right
            cubicTo(
                w * 0.84f, h * 0.56f,
                w * 0.84f, h * 0.86f,
                w * 0.58f, h * 0.86f
            )
            // Line along bottom
            lineTo(w * 0.32f, h * 0.86f)
        }

        // Draw top Cyan curve
        drawPath(
            path = topPath,
            brush = cyanBrush,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw bottom White curve
        drawPath(
            path = bottomPath,
            brush = whiteBrush,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

/**
 * Full Brand Header with Emblem + Typography
 * Matches: SHAWON TECH & ELECTRONIC
 */
@Composable
fun ShawonBrandHeader(
    modifier: Modifier = Modifier,
    emblemSize: Dp = 42.dp,
    showSubtitle: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShawonBrandEmblem(size = emblemSize)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "SHAWON",
                color = BrandWhite,
                fontSize = (emblemSize.value * 0.45f).sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )
            if (showSubtitle) {
                Text(
                    text = "TECH & ELECTRONIC",
                    color = BrandSilver,
                    fontSize = (emblemSize.value * 0.22f).sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

/**
 * Hero Banner matching the uploaded banner image
 */
@Composable
fun ShawonBrandHeroBanner(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF0F1722),
                        Color(0xFF162332),
                        Color(0xFF1B2B3D),
                        Color(0xFF131D2A)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 18.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            ShawonBrandEmblem(size = 54.dp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "SHAWON",
                    color = BrandWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "TECH & ELECTRONIC",
                    color = BrandCyanGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
            }
        }
    }
}
