package org.catholiccompanion.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import org.catholiccompanion.app.model.PrayerStep

private val pearlWhite = Color(0xFFFDF9EC)
private val pearlShadow = Color(0xFF7D6F58)
private val antiqueGold = Color(0xFFE8BD5A)
private val activeGold = Color(0xFFFFD970)
private val chainWhite = Color(0xFFECE6D6)

@Composable
fun RosaryBeads(
    step: PrayerStep,
    stepNumber: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    val target = rosaryTargetFor(step)
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription =
                    "Complete Rosary, prayer step $stepNumber of $totalSteps; ${step.instruction}"
            },
    ) {
        val ringPoints = List(RING_BEAD_COUNT) { index ->
            rosaryLoopPoint(index, size.width, size.height)
        }
        val centerpiece = Offset(size.width / 2f, size.height * 0.665f)
        // The first pendant bead sits closest to the crucifix; the three smaller
        // beads then lead upward toward the centerpiece in physical prayer order.
        val stemPoints = listOf(
            Offset(size.width / 2f, size.height * 0.825f),
            Offset(size.width / 2f, size.height * 0.775f),
            Offset(size.width / 2f, size.height * 0.73f),
            Offset(size.width / 2f, size.height * 0.69f),
        )
        val crossCenter = Offset(size.width / 2f, size.height * 0.925f)

        ringPoints.forEachIndexed { index, point ->
            drawLine(
                color = chainWhite.copy(alpha = 0.58f),
                start = point,
                end = ringPoints[(index + 1) % ringPoints.size],
                strokeWidth = 1.6.dp.toPx(),
            )
        }
        drawLine(chainWhite.copy(alpha = 0.58f), ringPoints.first(), centerpiece, 1.6.dp.toPx())
        drawLine(chainWhite.copy(alpha = 0.58f), centerpiece, stemPoints.last(), 1.6.dp.toPx())
        stemPoints.reversed().zipWithNext().forEach { (from, to) ->
            drawLine(chainWhite.copy(alpha = 0.58f), from, to, 1.6.dp.toPx())
        }
        drawLine(chainWhite.copy(alpha = 0.58f), stemPoints.first(), crossCenter, 1.6.dp.toPx())

        ringPoints.forEachIndexed { index, point ->
            val separator = index % BEADS_PER_DECADE == 0
            drawPearlBead(
                center = point,
                radius = if (separator) 7.2.dp.toPx() else 4.6.dp.toPx(),
                active = target.ringIndex == index,
            )
        }
        stemPoints.forEachIndexed { index, point ->
            drawPearlBead(
                center = point,
                radius = if (index == 0) 7.2.dp.toPx() else 4.8.dp.toPx(),
                active = target.stemIndex == index,
            )
        }
        drawCenterpiece(centerpiece, target.centerpiece)
        drawCrucifix(crossCenter, target.crucifix)
    }
}

/** Heart-shaped loop sized to keep the devotional image visible inside the Rosary. */
internal fun rosaryLoopPoint(index: Int, width: Float, height: Float): Offset {
    require(index in 0 until RING_BEAD_COUNT)
    val angle = PI + (index * 2.0 * PI / RING_BEAD_COUNT)
    val rawX = 16.0 * sin(angle).pow(3.0)
    val rawY = 13.0 * cos(angle) - 5.0 * cos(2.0 * angle) -
        2.0 * cos(3.0 * angle) - cos(4.0 * angle)
    return Offset(
        x = width / 2f + width * 0.42f * (rawX / 16.0).toFloat(),
        y = height * 0.36f - height * 0.29f * (rawY / 17.0).toFloat(),
    )
}

private fun DrawScope.drawPearlBead(center: Offset, radius: Float, active: Boolean) {
    drawCircle(Color.Black.copy(alpha = 0.34f), radius = radius + 2.dp.toPx(), center = center + Offset(1f, 2f))
    if (active) {
        drawCircle(activeGold.copy(alpha = 0.16f), radius = radius + 14.dp.toPx(), center = center)
        drawCircle(activeGold.copy(alpha = 0.30f), radius = radius + 8.dp.toPx(), center = center)
        drawCircle(
            activeGold,
            radius = radius + 3.dp.toPx(),
            center = center,
            style = Stroke(2.3.dp.toPx()),
        )
    }
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, pearlWhite, pearlShadow),
            center = center - Offset(radius * 0.38f, radius * 0.38f),
            radius = radius * 1.65f,
        ),
        radius = radius,
        center = center,
    )
    drawCircle(
        color = if (active) activeGold else Color.White.copy(alpha = 0.82f),
        radius = radius,
        center = center,
        style = Stroke(if (active) 1.8.dp.toPx() else 0.8.dp.toPx()),
    )
}

private fun DrawScope.drawCenterpiece(center: Offset, active: Boolean) {
    if (active) {
        drawCircle(activeGold.copy(alpha = 0.22f), 22.dp.toPx(), center)
        drawCircle(activeGold, 18.dp.toPx(), center, style = Stroke(2.dp.toPx()))
    }
    val halfWidth = 11.dp.toPx()
    val halfHeight = 15.dp.toPx()
    drawOval(
        brush = Brush.linearGradient(listOf(Color(0xFFFFE7A3), antiqueGold, Color(0xFF7A5520))),
        topLeft = center - Offset(halfWidth, halfHeight),
        size = Size(halfWidth * 2f, halfHeight * 2f),
    )
    drawOval(
        color = if (active) activeGold else pearlWhite,
        topLeft = center - Offset(halfWidth, halfHeight),
        size = Size(halfWidth * 2f, halfHeight * 2f),
        style = Stroke(1.4.dp.toPx()),
    )
    drawLine(
        color = Color(0xFF5C3B16),
        start = center - Offset(4.dp.toPx(), 4.dp.toPx()),
        end = center + Offset(0f, 5.dp.toPx()),
        strokeWidth = 1.5.dp.toPx(),
    )
    drawLine(
        color = Color(0xFF5C3B16),
        start = center + Offset(4.dp.toPx(), -4.dp.toPx()),
        end = center + Offset(0f, 5.dp.toPx()),
        strokeWidth = 1.5.dp.toPx(),
    )
}

private fun DrawScope.drawCrucifix(center: Offset, active: Boolean) {
    if (active) {
        drawCircle(activeGold.copy(alpha = 0.20f), 29.dp.toPx(), center)
    }
    val outline = if (active) activeGold else pearlWhite
    val verticalHalfWidth = 4.8.dp.toPx()
    val verticalHalfHeight = 25.dp.toPx()
    val horizontalHalfWidth = 17.dp.toPx()
    val horizontalTopOffset = 12.dp.toPx()
    val horizontalHeight = 8.dp.toPx()
    val goldBrush = Brush.linearGradient(listOf(Color(0xFFFFE7A3), antiqueGold, Color(0xFF76501C)))
    drawRoundRect(
        brush = goldBrush,
        topLeft = center - Offset(verticalHalfWidth, verticalHalfHeight),
        size = Size(verticalHalfWidth * 2f, verticalHalfHeight * 2f),
        cornerRadius = CornerRadius(3.dp.toPx()),
    )
    drawRoundRect(
        color = outline,
        topLeft = center - Offset(verticalHalfWidth, verticalHalfHeight),
        size = Size(verticalHalfWidth * 2f, verticalHalfHeight * 2f),
        cornerRadius = CornerRadius(3.dp.toPx()),
        style = Stroke(1.4.dp.toPx()),
    )
    drawRoundRect(
        brush = goldBrush,
        topLeft = center - Offset(horizontalHalfWidth, horizontalTopOffset),
        size = Size(horizontalHalfWidth * 2f, horizontalHeight),
        cornerRadius = CornerRadius(3.dp.toPx()),
    )
    drawRoundRect(
        color = outline,
        topLeft = center - Offset(horizontalHalfWidth, horizontalTopOffset),
        size = Size(horizontalHalfWidth * 2f, horizontalHeight),
        cornerRadius = CornerRadius(3.dp.toPx()),
        style = Stroke(1.4.dp.toPx()),
    )
}

internal data class RosaryTarget(
    val ringIndex: Int? = null,
    val stemIndex: Int? = null,
    val centerpiece: Boolean = false,
    val crucifix: Boolean = false,
)

internal fun rosaryTargetFor(step: PrayerStep): RosaryTarget {
    val decade = step.decade
    return when {
        step.id == "opening-sign" || step.id == "creed" || step.id == "closing-sign" ->
            RosaryTarget(crucifix = true)
        step.id == "opening-our-father" -> RosaryTarget(stemIndex = 0)
        step.id.startsWith("opening-hail-mary-") -> {
            val number = step.id.substringAfterLast('-').toIntOrNull() ?: 1
            RosaryTarget(stemIndex = number.coerceIn(1, 3))
        }
        decade != null && step.id.startsWith("decade-$decade-our-father") ->
            RosaryTarget(ringIndex = (decade - 1) * BEADS_PER_DECADE)
        decade != null && step.id.startsWith("decade-$decade-hail-mary-") -> {
            val number = step.id.substringAfterLast('-').toIntOrNull() ?: 1
            RosaryTarget(
                ringIndex = ((decade - 1) * BEADS_PER_DECADE + number)
                    .coerceIn(0, RING_BEAD_COUNT - 1),
            )
        }
        decade != null && step.id.startsWith("mystery-") ->
            RosaryTarget(ringIndex = (decade - 1) * BEADS_PER_DECADE)
        else -> RosaryTarget(centerpiece = true)
    }
}

private const val BEADS_PER_DECADE = 11
internal const val RING_BEAD_COUNT = 55
