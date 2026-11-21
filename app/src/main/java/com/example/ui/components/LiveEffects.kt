package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.AvatarPreset
import com.example.ui.theme.GlassPalette
import com.example.ui.theme.LocalGlassPalette
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class AmbientParticle(
    val xFraction: Float,
    val yFraction: Float,
    val radiusDp: Float,
    val speed: Float,
    val phase: Float,
    val usePrimaryColor: Boolean
)

@Composable
fun LiveWallpaperCanvas(
    palette: GlassPalette,
    modifier: Modifier = Modifier
) {
    val animBg1 by animateColorAsState(palette.bg1, tween(700), label = "bg1")
    val animBg2 by animateColorAsState(palette.bg2, tween(700), label = "bg2")
    val animBg3 by animateColorAsState(palette.bg3, tween(700), label = "bg3")
    val animAurora1 by animateColorAsState(palette.aurora1, tween(700), label = "aurora1")
    val animAurora2 by animateColorAsState(palette.aurora2, tween(700), label = "aurora2")
    val animP1 by animateColorAsState(palette.particle1, tween(700), label = "p1")
    val animP2 by animateColorAsState(palette.particle2, tween(700), label = "p2")

    val particles = remember {
        val rng = Random(2026)
        List(32) {
            AmbientParticle(
                xFraction = rng.nextFloat(),
                yFraction = rng.nextFloat(),
                radiusDp = 1.6f + rng.nextFloat() * 3.4f,
                speed = 0.5f + rng.nextFloat() * 1.1f,
                phase = rng.nextFloat() * (2f * PI.toFloat()),
                usePrimaryColor = it % 2 == 0
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "wallpaper")
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        // Diagonal gradient backdrop matching CSS 135deg linear-gradient
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(animBg1, animBg2, animBg3),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            )
        )

        // Drifting Aurora Blob 1
        val blob1Center = Offset(
            x = size.width * (0.24f + 0.14f * cos(drift)),
            y = size.height * (0.22f + 0.10f * sin(drift))
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(animAurora1, Color.Transparent),
                center = blob1Center,
                radius = size.minDimension * 0.72f
            ),
            radius = size.minDimension * 0.72f,
            center = blob1Center
        )

        // Drifting Aurora Blob 2
        val blob2Center = Offset(
            x = size.width * (0.78f + 0.12f * sin(drift * 0.8f)),
            y = size.height * (0.68f + 0.12f * cos(drift * 0.8f))
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(animAurora2, Color.Transparent),
                center = blob2Center,
                radius = size.minDimension * 0.68f
            ),
            radius = size.minDimension * 0.68f,
            center = blob2Center
        )

        // Soft Golden Starlight Accent Aura
        val blob3Center = Offset(
            x = size.width * (0.55f + 0.10f * cos(drift * 1.2f)),
            y = size.height * (0.42f + 0.08f * sin(drift * 1.2f))
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x1FFBBF24), Color.Transparent),
                center = blob3Center,
                radius = size.minDimension * 0.42f
            ),
            radius = size.minDimension * 0.42f,
            center = blob3Center
        )

        // Floating glassmorphic particles
        particles.forEach { p ->
            val angle = drift * p.speed + p.phase
            val cx = ((p.xFraction + 0.045f * cos(angle)) * size.width).coerceIn(0f, size.width)
            val cy = ((p.yFraction + 0.055f * sin(angle)) * size.height).coerceIn(0f, size.height)
            val baseColor = if (p.usePrimaryColor) animP1 else animP2
            val twinkle = 0.45f + 0.55f * (0.5f + 0.5f * sin(angle * 2f))
            val radiusPx = p.radiusDp.dp.toPx()

            drawCircle(
                color = baseColor.copy(alpha = baseColor.alpha * twinkle * 0.35f),
                radius = radiusPx * 2.4f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = baseColor.copy(alpha = baseColor.alpha * twinkle),
                radius = radiusPx,
                center = Offset(cx, cy)
            )
        }
    }
}

private data class ConfettiPiece(
    val startX: Float,
    val startY: Float,
    val vx: Float,
    val vy: Float,
    val sizeDp: Float,
    val rotationSpeed: Float,
    val initialRotation: Float,
    val color: Color
)

@Composable
fun ConfettiOverlay(
    burstId: Long,
    modifier: Modifier = Modifier
) {
    if (burstId == 0L) return

    val confettiColors = remember {
        listOf(
            Color(0xFFFDE047),
            Color(0xFFF59E0B),
            Color(0xFFC084FC),
            Color(0xFFF472B6),
            Color(0xFF38BDF8),
            Color(0xFF34D399)
        )
    }

    val pieces = remember(burstId) {
        val rng = Random(burstId)
        List(72) {
            val angle = -PI.toFloat() * (0.15f + rng.nextFloat() * 0.7f)
            val speed = 0.45f + rng.nextFloat() * 0.95f
            ConfettiPiece(
                startX = 0.2f + rng.nextFloat() * 0.6f,
                startY = 0.25f + rng.nextFloat() * 0.15f,
                vx = cos(angle) * speed * 0.55f,
                vy = sin(angle) * speed * 0.85f - 0.15f,
                sizeDp = 6f + rng.nextFloat() * 6f,
                rotationSpeed = -360f + rng.nextFloat() * 720f,
                initialRotation = rng.nextFloat() * 360f,
                color = confettiColors[it % confettiColors.size]
            )
        }
    }

    var elapsedSeconds by remember(burstId) { mutableFloatStateOf(0f) }

    LaunchedEffect(burstId) {
        val startNanos = withFrameNanos { it }
        while (elapsedSeconds < 2.8f) {
            val now = withFrameNanos { it }
            elapsedSeconds = (now - startNanos) / 1_000_000_000f
        }
    }

    if (elapsedSeconds >= 2.8f) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = elapsedSeconds
        val fade = (1f - (t / 2.8f)).coerceIn(0f, 1f)
        val gravity = 0.92f
        pieces.forEach { piece ->
            val x = (piece.startX + piece.vx * t) * size.width
            val y = (piece.startY + piece.vy * t + 0.5f * gravity * t * t) * size.height
            if (y in -40f..(size.height + 40f)) {
                val px = piece.sizeDp.dp.toPx()
                val angle = piece.initialRotation + piece.rotationSpeed * t
                rotate(degrees = angle, pivot = Offset(x, y)) {
                    drawRect(
                        color = piece.color.copy(alpha = fade),
                        topLeft = Offset(x - px / 2f, y - px * 0.35f),
                        size = Size(px, px * 0.65f)
                    )
                }
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderColor: Color? = null,
    backgroundColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalGlassPalette.current
    val shape = RoundedCornerShape(cornerRadius)
    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor ?: palette.glass,
        border = BorderStroke(1.dp, borderColor ?: palette.glassBorder),
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.02f)
                    )
                )
            ),
            content = content
        )
    }
}

@Composable
fun GlassPill(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    borderColor: Color? = null,
    textColor: Color? = null,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val palette = LocalGlassPalette.current
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor ?: Color.White.copy(alpha = 0.12f))
            .border(1.dp, borderColor ?: palette.glassBorder, CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (leadingIcon != null) leadingIcon()
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor ?: palette.textPrimary,
            maxLines = 1
        )
    }
}

@Composable
fun GlassInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 4,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val palette = LocalGlassPalette.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        placeholder = {
            if (placeholder.isNotEmpty()) {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textMuted.copy(alpha = 0.65f)
                )
            }
        },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = palette.accent,
            unfocusedBorderColor = palette.glassBorder,
            focusedContainerColor = palette.inputBg.copy(alpha = 0.78f),
            unfocusedContainerColor = palette.inputBg.copy(alpha = 0.52f),
            focusedTextColor = palette.textPrimary,
            unfocusedTextColor = palette.textPrimary,
            focusedLabelColor = palette.accent,
            unfocusedLabelColor = palette.textMuted,
            cursorColor = palette.accent
        ),
        modifier = modifier
    )
}

@Composable
fun FriendAvatar(
    name: String,
    localPhotoPath: String?,
    avatarPreset: AvatarPreset,
    size: Dp = 64.dp,
    cornerRadius: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    val palette = LocalGlassPalette.current
    val shape = RoundedCornerShape(cornerRadius)
    val gradientColors = when (avatarPreset) {
        AvatarPreset.STARLIGHT -> listOf(Color(0xFFC084FC), Color(0xFFF472B6))
        AvatarPreset.AURORA -> listOf(Color(0xFF38BDF8), Color(0xFF34D399))
        AvatarPreset.ROSEWOOD -> listOf(Color(0xFFFB7185), Color(0xFFFDBA74))
        AvatarPreset.OCEAN -> listOf(Color(0xFF22D3EE), Color(0xFF818CF8))
        AvatarPreset.GOLD -> listOf(Color(0xFFFDE047), Color(0xFFF59E0B))
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(Brush.linearGradient(gradientColors))
            .border(1.5.dp, Color.White.copy(alpha = 0.35f), shape),
        contentAlignment = Alignment.Center
    ) {
        if (!localPhotoPath.isNullOrBlank() && File(localPhotoPath).exists()) {
            AsyncImage(
                model = File(localPhotoPath),
                contentDescription = "$name portrait",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.img_friend_avatar_art_1795225040277),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.42f,
                modifier = Modifier.fillMaxSize()
            )
            val initials = name
                .trim()
                .split(Regex("\\s+"))
                .filter { it.isNotEmpty() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
                .ifEmpty { "F" }
            Text(
                text = initials,
                style = if (size >= 84.dp) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (avatarPreset == AvatarPreset.GOLD) Color(0xFF1F1300) else Color.White
            )
        }
    }
}

@Composable
fun GoldenTrophyEmblem(
    size: Dp = 68.dp,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "trophyGlow")
    val glowPulse by infinite.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w / 2f, h * 0.46f)

        // Radiant golden halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFBBF24).copy(alpha = 0.42f * glowPulse),
                    Color.Transparent
                ),
                center = center,
                radius = w * 0.56f
            ),
            radius = w * 0.56f,
            center = center
        )

        val goldBrush = Brush.linearGradient(
            colors = listOf(Color(0xFFFEF08A), Color(0xFFFBBF24), Color(0xFFD97706)),
            start = Offset(w * 0.2f, h * 0.1f),
            end = Offset(w * 0.8f, h * 0.9f)
        )

        // Left handle
        val leftHandle = Path().apply {
            moveTo(w * 0.28f, h * 0.24f)
            cubicTo(w * 0.10f, h * 0.24f, w * 0.12f, h * 0.50f, w * 0.31f, h * 0.48f)
        }
        drawPath(
            path = leftHandle,
            brush = goldBrush,
            style = Stroke(width = w * 0.06f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Right handle
        val rightHandle = Path().apply {
            moveTo(w * 0.72f, h * 0.24f)
            cubicTo(w * 0.90f, h * 0.24f, w * 0.88f, h * 0.50f, w * 0.69f, h * 0.48f)
        }
        drawPath(
            path = rightHandle,
            brush = goldBrush,
            style = Stroke(width = w * 0.06f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Trophy cup body
        val cup = Path().apply {
            moveTo(w * 0.27f, h * 0.18f)
            lineTo(w * 0.73f, h * 0.18f)
            cubicTo(w * 0.73f, h * 0.52f, w * 0.62f, h * 0.62f, w * 0.50f, h * 0.64f)
            cubicTo(w * 0.38f, h * 0.62f, w * 0.27f, h * 0.52f, w * 0.27f, h * 0.18f)
            close()
        }
        drawPath(path = cup, brush = goldBrush)

        // Stem
        drawRect(
            brush = goldBrush,
            topLeft = Offset(w * 0.45f, h * 0.62f),
            size = Size(w * 0.10f, h * 0.16f)
        )

        // Pedestal base
        val base = Path().apply {
            moveTo(w * 0.30f, h * 0.84f)
            lineTo(w * 0.70f, h * 0.84f)
            lineTo(w * 0.64f, h * 0.76f)
            lineTo(w * 0.36f, h * 0.76f)
            close()
        }
        drawPath(path = base, brush = goldBrush)

        // Center sparkle star inside cup
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = w * 0.045f,
            center = Offset(w * 0.50f, h * 0.36f)
        )
    }
}
