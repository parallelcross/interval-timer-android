package com.intervaltimer.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intervaltimer.app.ui.theme.AccentGreen
import com.intervaltimer.app.ui.theme.DarkBackground
import kotlin.random.Random

private data class ConfettiPiece(
    val x: Float,
    val speed: Float,
    val delay: Float,
    val size: Float,
    val color: Color,
    val rotation: Float,
    val rotationSpeed: Float,
    val wobble: Float,
)

private val confettiColors = listOf(
    Color(0xFF2ECC40), // green
    Color(0xFF2196F3), // blue
    Color(0xFFFF6B35), // orange
    Color(0xFFFFD700), // gold
    Color(0xFFFF4081), // pink
    Color(0xFF7C4DFF), // purple
    Color(0xFF00E5FF), // cyan
    Color(0xFFFF1744), // red
)

@Composable
fun CompletionScreen(
    onDone: () -> Unit,
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 4000, easing = LinearEasing),
        )
    }

    val pieces = remember {
        List(80) {
            ConfettiPiece(
                x = Random.nextFloat(),
                speed = 0.3f + Random.nextFloat() * 0.7f,
                delay = Random.nextFloat() * 0.3f,
                size = 6f + Random.nextFloat() * 10f,
                color = confettiColors[Random.nextInt(confettiColors.size)],
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = -200f + Random.nextFloat() * 400f,
                wobble = Random.nextFloat() * 60f,
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
    ) {
        // Confetti layer
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            pieces.forEach { piece ->
                val t = ((progress.value - piece.delay) / (1f - piece.delay)).coerceIn(0f, 1f)
                val yPos = -piece.size + t * (h + piece.size * 2) * piece.speed
                val xPos = piece.x * w + kotlin.math.sin(t * 6.28 * 2) * piece.wobble
                val rot = piece.rotation + t * piece.rotationSpeed

                rotate(degrees = rot, pivot = Offset(xPos, yPos)) {
                    drawRect(
                        color = piece.color,
                        topLeft = Offset(xPos - piece.size / 2, yPos - piece.size / 2),
                        size = Size(piece.size, piece.size * 0.6f),
                    )
                }
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp)
                .padding(top = 60.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Workout\nComplete!",
                color = Color.White,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 52.sp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Great job! You crushed it.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
            ) {
                Text(
                    text = "Done",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
