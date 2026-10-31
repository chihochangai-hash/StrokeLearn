package com.strokelearn.app

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.hypot

internal data class GlyphBox(val left: Float, val top: Float, val size: Float)

internal fun glyphBox(canvas: Size): GlyphBox {
    val size = minOf(canvas.width, canvas.height) * 0.96f
    return GlyphBox(
        left = (canvas.width - size) / 2f,
        top = (canvas.height - size) / 2f,
        size = size,
    )
}

internal fun digitBoxes(canvas: Size, count: Int): List<GlyphBox> {
    val cells = count.coerceAtLeast(1)
    val cellWidth = canvas.width / cells
    val size = minOf(cellWidth * 0.92f, canvas.height * 0.96f)
    return List(cells) { index ->
        GlyphBox(
            left = index * cellWidth + (cellWidth - size) / 2f,
            top = (canvas.height - size) / 2f,
            size = size,
        )
    }
}

internal fun Pt.toOffset(box: GlyphBox) = Offset(
    box.left + x / 100f * box.size,
    box.top + y / 100f * box.size,
)

private fun dist(a: Offset, b: Offset) = hypot(a.x - b.x, a.y - b.y)

private fun pathLength(stroke: List<Pt>, box: GlyphBox): Float {
    var total = 0f
    for (i in 1 until stroke.size) {
        total += dist(stroke[i - 1].toOffset(box), stroke[i].toOffset(box))
    }
    return total
}

private data class Hit(val progress: Float, val distance: Float)

private fun closest(
    point: Offset,
    stroke: List<Pt>,
    box: GlyphBox,
    previous: Float,
    first: Boolean,
): Hit {
    val total = pathLength(stroke, box).coerceAtLeast(1f)
    var walked = 0f
    var best = Hit(0f, 1e9f)
    fun consider(progress: Float, distance: Float, preferEarlier: Boolean) {
        val better = distance < best.distance - 0.5f ||
            (kotlin.math.abs(distance - best.distance) <= 0.5f &&
                (if (preferEarlier) progress < best.progress else progress > best.progress))
        if (better) best = Hit(progress, distance)
    }
    for (i in 1 until stroke.size) {
        val a = stroke[i - 1].toOffset(box)
        val b = stroke[i].toOffset(box)
        val dx = b.x - a.x
        val dy = b.y - a.y
        val segment = hypot(dx, dy)
        if (segment <= 0.5f) continue
        val t = (((point.x - a.x) * dx + (point.y - a.y) * dy) / (segment * segment)).coerceIn(0f, 1f)
        val cx = a.x + dx * t
        val cy = a.y + dy * t
        val distance = hypot(point.x - cx, point.y - cy)
        val progress = (walked + segment * t) / total
        val inWindow = first || (progress >= previous - 0.12f && progress <= previous + 0.50f)
        if (inWindow) consider(progress, distance, first)
        walked += segment
    }
    val tolerance = box.size * 0.16f
    if (best.distance > tolerance) {
        walked = 0f
        for (i in 1 until stroke.size) {
            val a = stroke[i - 1].toOffset(box)
            val b = stroke[i].toOffset(box)
            val dx = b.x - a.x
            val dy = b.y - a.y
            val segment = hypot(dx, dy)
            if (segment <= 0.5f) continue
            val t = (((point.x - a.x) * dx + (point.y - a.y) * dy) / (segment * segment)).coerceIn(0f, 1f)
            val distance = hypot(point.x - (a.x + dx * t), point.y - (a.y + dy * t))
            val progress = (walked + segment * t) / total
            if (distance < best.distance) best = Hit(progress, distance)
            walked += segment
        }
    }
    return best
}

internal fun strokeAccepted(trace: List<Offset>, stroke: List<Pt>, box: GlyphBox): Boolean {
    if (trace.size < 3 || stroke.size < 2) return false
    val tolerance = box.size * 0.16f
    val start = stroke.first().toOffset(box)
    val end = stroke.last().toOffset(box)
    if (dist(trace.first(), start) > tolerance || dist(trace.last(), end) > tolerance) return false
    var previous = 0f
    var minP = 1f
    var maxP = 0f
    trace.forEachIndexed { index, point ->
        val hit = closest(point, stroke, box, previous, index == 0)
        if (hit.distance > tolerance) return false
        if (index > 0 && (hit.progress < previous - 0.12f || hit.progress > previous + 0.50f)) return false
        previous = hit.progress
        minP = minOf(minP, hit.progress)
        maxP = maxOf(maxP, hit.progress)
    }
    return minP <= 0.18f && maxP >= 0.82f
}
