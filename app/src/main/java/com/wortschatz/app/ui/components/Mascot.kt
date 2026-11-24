package com.wortschatz.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.wortschatz.app.domain.settings.MascotType
import com.wortschatz.app.ui.theme.DuoTheme
import kotlin.math.sin
import kotlin.random.Random

val LocalMascot = staticCompositionLocalOf { MascotType.FOX }

/**
 * Hand-drawn mascots built from simple primitives. They scale crisply, animate with a
 * gentle bob and never require binary illustration assets.
 */
@Composable
fun Mascot(
    modifier: Modifier = Modifier,
    type: MascotType = LocalMascot.current,
    animate: Boolean = true,
    contentDescription: String? = null,
) {
    val transition = rememberInfiniteTransition(label = "mascotBob")
    val bob by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (animate) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "mascotBobValue",
    )

    val a11y = if (contentDescription != null) {
        Modifier.semantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }

    Canvas(modifier = modifier.then(a11y)) {
        withTransform({ translate(0f, -bob * size.height * 0.018f) }) {
            when (type) {
                MascotType.FOX -> drawFox()
                MascotType.CHAMELEON -> drawChameleon()
                MascotType.DOG -> drawDog()
            }
        }
    }
}

private fun DrawScope.drawFox() {
    val w = size.width
    val h = size.height
    val orange = Color(0xFFFF8A3D)
    val orangeDark = Color(0xFFE5671F)
    val cream = Color(0xFFFFF1DE)
    val ink = Color(0xFF3B2A26)
    val scarf = Color(0xFF1CB0F6)
    val scarfDark = Color(0xFF0B6E99)

    fun p(x: Float, y: Float) = Offset(w * x, h * y)
    fun s(x: Float, y: Float) = Size(w * x, h * y)

    // Bushy tail behind the body.
    rotate(degrees = 16f, pivot = p(0.77f, 0.68f)) {
        drawOval(orangeDark, topLeft = p(0.64f, 0.55f), size = s(0.29f, 0.24f))
        drawOval(cream, topLeft = p(0.82f, 0.56f), size = s(0.13f, 0.15f))
    }

    // Body, belly and feet.
    drawOval(orange, topLeft = p(0.25f, 0.44f), size = s(0.50f, 0.45f))
    drawOval(cream, topLeft = p(0.36f, 0.55f), size = s(0.28f, 0.31f))
    drawRoundRect(
        orangeDark,
        topLeft = p(0.33f, 0.86f),
        size = s(0.15f, 0.06f),
        cornerRadius = CornerRadius(w * 0.03f, h * 0.02f),
    )
    drawRoundRect(
        orangeDark,
        topLeft = p(0.53f, 0.86f),
        size = s(0.15f, 0.06f),
        cornerRadius = CornerRadius(w * 0.03f, h * 0.02f),
    )

    // Pointed ears, drawn before the head so their bases merge with it.
    val leftEar = Path().apply {
        moveTo(w * 0.29f, h * 0.24f)
        lineTo(w * 0.31f, h * 0.04f)
        lineTo(w * 0.47f, h * 0.18f)
        close()
    }
    val rightEar = Path().apply {
        moveTo(w * 0.71f, h * 0.24f)
        lineTo(w * 0.69f, h * 0.04f)
        lineTo(w * 0.53f, h * 0.18f)
        close()
    }
    drawPath(leftEar, orange)
    drawPath(rightEar, orange)
    val leftInner = Path().apply {
        moveTo(w * 0.34f, h * 0.19f)
        lineTo(w * 0.35f, h * 0.10f)
        lineTo(w * 0.43f, h * 0.18f)
        close()
    }
    val rightInner = Path().apply {
        moveTo(w * 0.66f, h * 0.19f)
        lineTo(w * 0.65f, h * 0.10f)
        lineTo(w * 0.57f, h * 0.18f)
        close()
    }
    drawPath(leftInner, Color(0xFFFFB7A0))
    drawPath(rightInner, Color(0xFFFFB7A0))

    // Head and muzzle.
    drawCircle(orange, radius = w * 0.25f, center = p(0.50f, 0.36f))
    drawOval(cream, topLeft = p(0.34f, 0.40f), size = s(0.32f, 0.19f))
    drawCircle(ink, radius = w * 0.028f, center = p(0.50f, 0.46f))
    drawArc(
        ink,
        startAngle = 15f,
        sweepAngle = 150f,
        useCenter = false,
        topLeft = p(0.43f, 0.45f),
        size = s(0.14f, 0.10f),
        style = Stroke(width = w * 0.012f, cap = StrokeCap.Round),
    )

    // Eyes and highlights.
    drawCircle(ink, radius = w * 0.030f, center = p(0.41f, 0.33f))
    drawCircle(ink, radius = w * 0.030f, center = p(0.59f, 0.33f))
    drawCircle(Color.White, radius = w * 0.011f, center = p(0.418f, 0.322f))
    drawCircle(Color.White, radius = w * 0.011f, center = p(0.598f, 0.322f))

    // Scarf.
    drawRoundRect(
        scarf,
        topLeft = p(0.32f, 0.55f),
        size = s(0.36f, 0.07f),
        cornerRadius = CornerRadius(w * 0.035f, h * 0.025f),
    )
    val scarfEnd = Path().apply {
        moveTo(w * 0.58f, h * 0.59f)
        lineTo(w * 0.69f, h * 0.65f)
        lineTo(w * 0.60f, h * 0.73f)
        close()
    }
    drawPath(scarfEnd, scarfDark)
}

private fun DrawScope.drawChameleon() {
    val w = size.width
    val h = size.height
    val green = Color(0xFF35C98F)
    val greenDark = Color(0xFF17835B)
    val belly = Color(0xFFCCF9D9)
    val gold = Color(0xFFFFC94D)
    val ink = Color(0xFF173B2E)

    fun p(x: Float, y: Float) = Offset(w * x, h * y)
    fun s(x: Float, y: Float) = Size(w * x, h * y)

    // Branch.
    drawRoundRect(
        Color(0xFFB97A45),
        topLeft = p(0.10f, 0.83f),
        size = s(0.82f, 0.06f),
        cornerRadius = CornerRadius(w * 0.03f, h * 0.02f),
    )

    // Spiral tail.
    val tail = Path().apply {
        moveTo(w * 0.63f, h * 0.60f)
        cubicTo(w * 0.92f, h * 0.56f, w * 0.98f, h * 0.78f, w * 0.80f, h * 0.80f)
        cubicTo(w * 0.65f, h * 0.82f, w * 0.65f, h * 0.67f, w * 0.76f, h * 0.68f)
        cubicTo(w * 0.84f, h * 0.69f, w * 0.83f, h * 0.75f, w * 0.77f, h * 0.74f)
    }
    drawPath(
        tail,
        greenDark,
        style = Stroke(width = w * 0.055f, cap = StrokeCap.Round),
    )

    // Body and cream belly.
    rotate(degrees = -7f, pivot = p(0.53f, 0.58f)) {
        drawOval(green, topLeft = p(0.25f, 0.42f), size = s(0.55f, 0.34f))
        drawOval(belly, topLeft = p(0.33f, 0.53f), size = s(0.36f, 0.20f))
    }

    // Feet gripping the branch.
    drawRoundRect(
        greenDark,
        topLeft = p(0.35f, 0.72f),
        size = s(0.07f, 0.15f),
        cornerRadius = CornerRadius(w * 0.03f, h * 0.02f),
    )
    drawRoundRect(
        greenDark,
        topLeft = p(0.58f, 0.72f),
        size = s(0.07f, 0.15f),
        cornerRadius = CornerRadius(w * 0.03f, h * 0.02f),
    )
    drawRoundRect(
        greenDark,
        topLeft = p(0.30f, 0.83f),
        size = s(0.14f, 0.045f),
        cornerRadius = CornerRadius(w * 0.03f, h * 0.02f),
    )
    drawRoundRect(
        greenDark,
        topLeft = p(0.55f, 0.83f),
        size = s(0.14f, 0.045f),
        cornerRadius = CornerRadius(w * 0.03f, h * 0.02f),
    )

    // Head, crest and eye turrets.
    drawCircle(green, radius = w * 0.19f, center = p(0.38f, 0.35f))
    val crest = Path().apply {
        moveTo(w * 0.27f, h * 0.23f)
        lineTo(w * 0.35f, h * 0.07f)
        lineTo(w * 0.48f, h * 0.22f)
        close()
    }
    drawPath(crest, gold)
    drawCircle(greenDark, radius = w * 0.065f, center = p(0.33f, 0.31f))
    drawCircle(greenDark, radius = w * 0.065f, center = p(0.45f, 0.30f))
    drawCircle(Color.White, radius = w * 0.052f, center = p(0.33f, 0.31f))
    drawCircle(Color.White, radius = w * 0.052f, center = p(0.45f, 0.30f))
    drawCircle(ink, radius = w * 0.024f, center = p(0.345f, 0.315f))
    drawCircle(ink, radius = w * 0.024f, center = p(0.465f, 0.305f))
    drawCircle(Color.White, radius = w * 0.009f, center = p(0.35f, 0.305f))
    drawCircle(Color.White, radius = w * 0.009f, center = p(0.47f, 0.295f))

    // Relaxed smile and body spots.
    drawArc(
        ink,
        startAngle = 20f,
        sweepAngle = 120f,
        useCenter = false,
        topLeft = p(0.38f, 0.39f),
        size = s(0.14f, 0.09f),
        style = Stroke(width = w * 0.012f, cap = StrokeCap.Round),
    )
    drawCircle(greenDark.copy(alpha = 0.55f), radius = w * 0.023f, center = p(0.56f, 0.49f))
    drawCircle(greenDark.copy(alpha = 0.45f), radius = w * 0.017f, center = p(0.64f, 0.54f))
    drawCircle(greenDark.copy(alpha = 0.45f), radius = w * 0.014f, center = p(0.50f, 0.45f))
}

private fun DrawScope.drawDog() {
    val w = size.width
    val h = size.height
    val tan = Color(0xFFE9A75C)
    val tanDark = Color(0xFFC97C35)
    val cream = Color(0xFFFFF3DC)
    val ink = Color(0xFF3B2A26)
    val bandana = Color(0xFFEA4C3C)
    val tongue = Color(0xFFFF7D8D)

    fun p(x: Float, y: Float) = Offset(w * x, h * y)
    fun s(x: Float, y: Float) = Size(w * x, h * y)

    // Curled tail.
    val tail = Path().apply {
        moveTo(w * 0.70f, h * 0.65f)
        cubicTo(w * 0.92f, h * 0.60f, w * 0.97f, h * 0.80f, w * 0.82f, h * 0.81f)
        cubicTo(w * 0.72f, h * 0.81f, w * 0.75f, h * 0.72f, w * 0.82f, h * 0.73f)
    }
    drawPath(tail, tanDark, style = Stroke(width = w * 0.07f, cap = StrokeCap.Round))

    // Body, belly, feet.
    drawOval(tan, topLeft = p(0.26f, 0.44f), size = s(0.48f, 0.44f))
    drawOval(cream, topLeft = p(0.36f, 0.54f), size = s(0.28f, 0.31f))
    drawRoundRect(
        tanDark,
        topLeft = p(0.33f, 0.85f),
        size = s(0.15f, 0.06f),
        cornerRadius = CornerRadius(w * 0.03f, h * 0.02f),
    )
    drawRoundRect(
        tanDark,
        topLeft = p(0.53f, 0.85f),
        size = s(0.15f, 0.06f),
        cornerRadius = CornerRadius(w * 0.03f, h * 0.02f),
    )

    // Pointed Shiba ears.
    val leftEar = Path().apply {
        moveTo(w * 0.31f, h * 0.26f)
        lineTo(w * 0.28f, h * 0.05f)
        lineTo(w * 0.49f, h * 0.18f)
        close()
    }
    val rightEar = Path().apply {
        moveTo(w * 0.69f, h * 0.26f)
        lineTo(w * 0.72f, h * 0.05f)
        lineTo(w * 0.51f, h * 0.18f)
        close()
    }
    drawPath(leftEar, tan)
    drawPath(rightEar, tan)
    val leftInner = Path().apply {
        moveTo(w * 0.34f, h * 0.21f)
        lineTo(w * 0.33f, h * 0.11f)
        lineTo(w * 0.43f, h * 0.18f)
        close()
    }
    val rightInner = Path().apply {
        moveTo(w * 0.66f, h * 0.21f)
        lineTo(w * 0.67f, h * 0.11f)
        lineTo(w * 0.57f, h * 0.18f)
        close()
    }
    drawPath(leftInner, Color(0xFFFFC8B6))
    drawPath(rightInner, Color(0xFFFFC8B6))

    // Head, cheeks, muzzle.
    drawCircle(tan, radius = w * 0.245f, center = p(0.50f, 0.37f))
    drawOval(cream, topLeft = p(0.33f, 0.41f), size = s(0.34f, 0.21f))
    drawOval(Color.White, topLeft = p(0.29f, 0.31f), size = s(0.20f, 0.13f))
    drawOval(Color.White, topLeft = p(0.51f, 0.31f), size = s(0.20f, 0.13f))

    // Eyes and brows.
    drawCircle(ink, radius = w * 0.029f, center = p(0.42f, 0.34f))
    drawCircle(ink, radius = w * 0.029f, center = p(0.58f, 0.34f))
    drawCircle(Color.White, radius = w * 0.010f, center = p(0.428f, 0.332f))
    drawCircle(Color.White, radius = w * 0.010f, center = p(0.588f, 0.332f))
    drawArc(
        tanDark,
        startAngle = 200f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = p(0.36f, 0.255f),
        size = s(0.12f, 0.08f),
        style = Stroke(width = w * 0.014f, cap = StrokeCap.Round),
    )
    drawArc(
        tanDark,
        startAngle = 200f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = p(0.52f, 0.255f),
        size = s(0.12f, 0.08f),
        style = Stroke(width = w * 0.014f, cap = StrokeCap.Round),
    )

    drawCircle(ink, radius = w * 0.032f, center = p(0.50f, 0.455f))
    drawArc(
        ink,
        startAngle = 15f,
        sweepAngle = 150f,
        useCenter = false,
        topLeft = p(0.43f, 0.45f),
        size = s(0.14f, 0.10f),
        style = Stroke(width = w * 0.012f, cap = StrokeCap.Round),
    )
    drawRoundRect(
        tongue,
        topLeft = p(0.465f, 0.51f),
        size = s(0.07f, 0.055f),
        cornerRadius = CornerRadius(w * 0.03f, h * 0.02f),
    )

    // Bandana.
    drawRoundRect(
        bandana,
        topLeft = p(0.31f, 0.55f),
        size = s(0.38f, 0.07f),
        cornerRadius = CornerRadius(w * 0.035f, h * 0.025f),
    )
    val knot = Path().apply {
        moveTo(w * 0.50f, h * 0.60f)
        lineTo(w * 0.38f, h * 0.72f)
        lineTo(w * 0.58f, h * 0.70f)
        close()
    }
    drawPath(knot, Color(0xFFC92E24))
    drawCircle(Color.White.copy(alpha = 0.8f), radius = w * 0.012f, center = p(0.55f, 0.63f))
}

private data class ConfettiParticle(
    val x: Float,
    val phase: Float,
    val speed: Float,
    val sway: Float,
    val swayAmount: Float,
    val startOffset: Float,
    val rotation: Float,
    val spin: Float,
    val width: Float,
    val height: Float,
    val color: Color,
)

/**
 * Confetti only rains down after a flawless round, making a perfect session feel special.
 * The animation is decorative and therefore excluded from accessibility semantics.
 */
@Composable
fun Confetti(
    modifier: Modifier = Modifier,
    particleCount: Int = 80,
    seed: Int = 42,
) {
    val colors = DuoTheme.colors
    val progress = remember { Animatable(0f) }
    val palette = remember(colors) {
        listOf(colors.green, colors.gold, colors.blue, colors.streak, colors.red, Color(0xFFCE82FF))
    }
    val particles = remember(particleCount, seed, palette) {
        val random = Random(seed)
        List(particleCount) {
            ConfettiParticle(
                x = random.nextFloat(),
                phase = random.nextFloat() * 6.28f,
                speed = 0.55f + random.nextFloat() * 0.65f,
                sway = 1.5f + random.nextFloat() * 2.5f,
                swayAmount = 0.01f + random.nextFloat() * 0.03f,
                startOffset = random.nextFloat(),
                rotation = random.nextFloat() * 360f,
                spin = (random.nextFloat() - 0.5f) * 900f,
                width = 0.008f + random.nextFloat() * 0.012f,
                height = 0.018f + random.nextFloat() * 0.022f,
                color = palette[random.nextInt(palette.size)],
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3000, easing = LinearEasing),
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val p = progress.value
        particles.forEach { particle ->
            val cycle = (particle.startOffset + p * particle.speed) % 1.15f
            val y = (-0.15f + cycle * 1.25f) * size.height
            val x = particle.x * size.width +
                sin(p * 6.28f * particle.sway + particle.phase) * particle.swayAmount * size.width
            val fade = when {
                p < 0.06f -> p / 0.06f
                p > 0.82f -> ((1f - p) / 0.18f).coerceIn(0f, 1f)
                else -> 1f
            }
            if (fade <= 0f) return@forEach

            rotate(degrees = particle.rotation + p * particle.spin, pivot = Offset(x, y)) {
                drawRoundRect(
                    color = particle.color.copy(alpha = fade),
                    topLeft = Offset(x, y),
                    size = Size(size.width * particle.width, size.height * particle.height),
                    cornerRadius = CornerRadius(size.width * 0.004f),
                )
            }
        }
    }
}
