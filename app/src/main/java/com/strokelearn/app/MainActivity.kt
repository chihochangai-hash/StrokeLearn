package com.strokelearn.app

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private const val PREFS = "strokelearn"
private const val SCALE_KEY = "glyph_scale"
private const val SCALE_MIN = 0.45f
private const val SCALE_MAX = 2.6f

private enum class Lesson { Home, Numbers, Zhuyin, Letters }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StrokeApp() }
    }
}

@Composable
private fun StrokeApp() {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.setSpeechRate(0.9f)
            }
        }
        tts = engine
        onDispose {
            engine?.stop()
            engine?.shutdown()
        }
    }
    var lesson by remember { mutableStateOf(Lesson.Home) }
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    val glyphScale = remember {
        mutableFloatStateOf(prefs.getFloat(SCALE_KEY, 1f).coerceIn(SCALE_MIN, SCALE_MAX))
    }
    val onScale: (Float) -> Unit = { next ->
        val clamped = next.coerceIn(SCALE_MIN, SCALE_MAX)
        glyphScale.floatValue = clamped
        prefs.edit().putFloat(SCALE_KEY, clamped).apply()
    }
    when (lesson) {
        Lesson.Home -> HomeScreen { lesson = it }
        Lesson.Numbers -> NumberScreen(tts, glyphScale, onScale) { lesson = Lesson.Home }
        Lesson.Zhuyin -> PracticeScreen(zhuyinGlyphs, "ㄅㄆㄇ", tts, glyphScale, onScale, onHome = { lesson = Lesson.Home })
        Lesson.Letters -> LetterScreen(tts, glyphScale, onScale) { lesson = Lesson.Home }
    }
}

private fun speak(tts: TextToSpeech?, glyph: Glyph) {
    val locale = if (glyph.locale == "en-US") Locale.US else Locale.TAIWAN
    speakText(tts, glyph.speak, locale, glyph.label)
}

private fun speakText(tts: TextToSpeech?, text: String, locale: Locale, utteranceId: String) {
    val engine = tts ?: return
    val ready = engine.setLanguage(locale)
    if (ready == TextToSpeech.LANG_MISSING_DATA || ready == TextToSpeech.LANG_NOT_SUPPORTED) {
        engine.language = Locale.getDefault()
    }
    engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
}

private fun numberWords(number: Int): String {
    val digits = arrayOf("零", "一", "二", "三", "四", "五", "六", "七", "八", "九")
    return when {
        number == 100 -> "一百"
        number < 10 -> digits[number]
        number < 20 -> "十" + if (number % 10 == 0) "" else digits[number % 10]
        else -> digits[number / 10] + "十" + if (number % 10 == 0) "" else digits[number % 10]
    }
}

private fun numberDigits(number: Int): List<Int> = when {
    number >= 100 -> listOf(1, 0, 0)
    number >= 10 -> listOf(number / 10, number % 10)
    else -> listOf(number)
}

@Composable
private fun HomeScreen(open: (Lesson) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF071018))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
    ) {
        Choice("123", "數字筆順") { open(Lesson.Numbers) }
        Choice("ㄅㄆㄇ", "注音筆順") { open(Lesson.Zhuyin) }
        Choice("ABC", "英文字母") { open(Lesson.Letters) }
    }
}

@Composable
private fun ColumnScope.Choice(title: String, subtitle: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().weight(1f),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF163044), contentColor = Color.White),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 72.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, fontSize = 22.sp, color = Color(0xFFB7D3E8))
        }
    }
}

@Composable
private fun NumberScreen(
    tts: TextToSpeech?,
    glyphScale: MutableFloatState,
    onScale: (Float) -> Unit,
    home: () -> Unit,
) {
    var number by remember { mutableIntStateOf(1) }
    var step by remember(number) { mutableIntStateOf(0) }
    val done = remember(number) { mutableStateListOf<List<Offset>>() }
    val active = remember { mutableStateListOf<Offset>() }
    var bad by remember(number) { mutableStateOf<List<Offset>>(emptyList()) }
    val digits = numberDigits(number)
    val plan = digits.flatMapIndexed { digitIndex, digit ->
        digitStrokes(digit).map { stroke -> digitIndex to stroke }
    }
    val (digitIndex, stroke) = plan[step]
    DisposableEffect(number) {
        speakText(tts, numberWords(number), Locale.TAIWAN, "n$number")
        onDispose { }
    }

    fun go(next: Int) {
        number = when {
            next > 100 -> 1
            next < 1 -> 100
            else -> next
        }
    }

    fun clearWriting() {
        done.clear()
        active.clear()
        bad = emptyList()
        step = 0
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF071018)).screenBars()) {
        LessonBar(
            title = "$number / 100   ${step + 1}/${plan.size}",
            onHome = home,
        ) {
            TextButtonLabel("念") { speakText(tts, numberWords(number), Locale.TAIWAN, "n$number") }
            TextButtonLabel("清除") { clearWriting() }
            TextButtonLabel("上") { go(number - 1) }
            TextButtonLabel("下") { go(number + 1) }
        }
        Canvas(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(number, step) {
                    drawOrPinch(
                        scale = { glyphScale.floatValue },
                        onScale = onScale,
                        onDrawStart = {
                            bad = emptyList()
                            active.clear()
                            active.add(it)
                        },
                        onDraw = { point ->
                            val last = active.lastOrNull()
                            if (last == null || hypot(point.x - last.x, point.y - last.y) > 4f) active.add(point)
                        },
                        onDrawEnd = {
                            val boxes = digitBoxes(Size(size.width.toFloat(), size.height.toFloat()), digits.size, glyphScale.floatValue)
                            if (strokeAccepted(active.toList(), stroke, boxes[digitIndex])) {
                                done.add(active.toList())
                                active.clear()
                                if (step + 1 >= plan.size) go(number + 1) else step += 1
                            } else {
                                bad = active.toList()
                                active.clear()
                            }
                        },
                        onDrawCancel = { active.clear() },
                    )
                },
        ) {
            val boxes = digitBoxes(size, digits.size, glyphScale.floatValue)
            val guide = Color(0xFF6E8BA3)
            val ink = Color(0xFFF7F7F2)
            digits.forEachIndexed { index, digit ->
                val box = boxes[index]
                digitStrokes(digit).forEachIndexed { strokeAt, points ->
                    val current = index == digitIndex && plan.take(step + 1).count { it.first == index } - 1 == strokeAt
                    drawStrokeGuide(points.map { it.toOffset(box) }, if (current) Color(0xFF7AD7FF) else guide, box.size, current)
                }
            }
            val width = boxes.first().size * 0.045f
            val traces = done.map { it to ink } + (bad to Color(0xFFFF6B6B)) + (active.toList() to ink)
            traces.forEach { (trace, color) ->
                for (p in 1 until trace.size) drawLine(color, trace[p - 1], trace[p], width, cap = StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun LetterScreen(
    tts: TextToSpeech?,
    glyphScale: MutableFloatState,
    onScale: (Float) -> Unit,
    home: () -> Unit,
) {
    var upper by remember { mutableStateOf(true) }
    PracticeScreen(
        glyphs = if (upper) upperGlyphs else lowerGlyphs,
        title = if (upper) "ABC" else "abc",
        tts = tts,
        glyphScale = glyphScale,
        onScale = onScale,
        onHome = home,
        extra = {
            TextButtonLabel(if (upper) "abc" else "ABC") { upper = !upper }
        },
    )
}

@Composable
private fun PracticeScreen(
    glyphs: List<Glyph>,
    title: String,
    tts: TextToSpeech?,
    glyphScale: MutableFloatState,
    onScale: (Float) -> Unit,
    onHome: () -> Unit,
    extra: @Composable () -> Unit = {},
) {
    var index by remember(glyphs) { mutableIntStateOf(0) }
    var strokeIndex by remember(glyphs, index) { mutableIntStateOf(0) }
    val done = remember(glyphs, index) { mutableStateListOf<List<Offset>>() }
    val active = remember { mutableStateListOf<Offset>() }
    var bad by remember(glyphs, index) { mutableStateOf<List<Offset>>(emptyList()) }
    val glyph = glyphs[index]
    DisposableEffect(glyph.label, glyph.speak) {
        speak(tts, glyph)
        onDispose { }
    }

    fun go(next: Int) {
        index = (next + glyphs.size) % glyphs.size
    }

    fun clearWriting() {
        done.clear()
        active.clear()
        bad = emptyList()
        strokeIndex = 0
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF071018)).screenBars()) {
        LessonBar(
            title = "$title  ${glyph.label}  ${strokeIndex + 1}/${glyph.strokes.size}",
        ) {
            TextButtonLabel("回") { onHome() }
            extra()
            TextButtonLabel("念") { speak(tts, glyph) }
            TextButtonLabel("清除") { clearWriting() }
            TextButtonLabel("上") { go(index - 1) }
            TextButtonLabel("下") { go(index + 1) }
        }
        Canvas(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(glyph.label, strokeIndex) {
                    drawOrPinch(
                        scale = { glyphScale.floatValue },
                        onScale = onScale,
                        onDrawStart = {
                            bad = emptyList()
                            active.clear()
                            active.add(it)
                        },
                        onDraw = { point ->
                            val last = active.lastOrNull()
                            if (last == null || hypot(point.x - last.x, point.y - last.y) > 4f) active.add(point)
                        },
                        onDrawEnd = {
                            val box = glyphBox(Size(size.width.toFloat(), size.height.toFloat()), glyphScale.floatValue)
                            val stroke = glyph.strokes[strokeIndex]
                            if (strokeAccepted(active.toList(), stroke, box)) {
                                done.add(active.toList())
                                active.clear()
                                if (strokeIndex + 1 >= glyph.strokes.size) go(index + 1) else strokeIndex += 1
                            } else {
                                bad = active.toList()
                                active.clear()
                            }
                        },
                        onDrawCancel = { active.clear() },
                    )
                },
        ) {
            val box = glyphBox(size, glyphScale.floatValue)
            val guide = Color(0xFF6E8BA3)
            val activeColor = Color(0xFF7AD7FF)
            val ink = Color(0xFFF7F7F2)
            glyph.strokes.forEachIndexed { i, stroke ->
                drawStrokeGuide(
                    stroke.map { it.toOffset(box) },
                    if (i == strokeIndex) activeColor else guide,
                    box.size,
                    i == strokeIndex,
                )
            }
            val width = box.size * 0.045f
            val traces = done.map { it to ink } + (bad to Color(0xFFFF6B6B)) + (active.toList() to ink)
            traces.forEach { (trace, color) ->
                for (p in 1 until trace.size) drawLine(color, trace[p - 1], trace[p], width, cap = StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun Modifier.screenBars() = windowInsetsPadding(WindowInsets.safeDrawing)

@Composable
private fun LessonBar(
    title: String,
    onHome: (() -> Unit)? = null,
    actions: @Composable () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onHome != null) TextButtonLabel("回") { onHome() }
        Text(title, color = Color(0xFFD5E6F2), fontSize = 20.sp, modifier = Modifier.padding(horizontal = 8.dp))
        actions()
    }
}

private suspend fun PointerInputScope.drawOrPinch(
    scale: () -> Float,
    onScale: (Float) -> Unit,
    onDrawStart: (Offset) -> Unit,
    onDraw: (Offset) -> Unit,
    onDrawEnd: () -> Unit,
    onDrawCancel: () -> Unit,
) {
    awaitEachGesture {
        val first = awaitFirstDown(requireUnconsumed = false)
        var drawing = true
        var zooming = false
        var baseDist = 1f
        var baseScale = scale()
        onDrawStart(first.position)
        while (true) {
            val event = awaitPointerEvent()
            val pressed = event.changes.filter { it.pressed }
            if (pressed.size >= 2) {
                if (drawing) {
                    drawing = false
                    onDrawCancel()
                }
                val a = pressed[0].position
                val b = pressed[1].position
                val dist = hypot(a.x - b.x, a.y - b.y).coerceAtLeast(8f)
                if (!zooming) {
                    zooming = true
                    baseDist = dist
                    baseScale = scale()
                } else {
                    onScale((baseScale * dist / baseDist).coerceIn(SCALE_MIN, SCALE_MAX))
                }
                event.changes.forEach { it.consume() }
            } else if (drawing && pressed.size == 1) {
                onDraw(pressed[0].position)
                pressed[0].consume()
            }
            if (pressed.isEmpty()) break
        }
        if (drawing) onDrawEnd()
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStrokeGuide(
    points: List<Offset>,
    color: Color,
    boxSize: Float,
    active: Boolean,
) {
    for (p in 1 until points.size) {
        drawLine(
            color = color,
            start = points[p - 1],
            end = points[p],
            strokeWidth = boxSize * 0.012f,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(boxSize * 0.025f, boxSize * 0.018f)),
        )
    }
    drawArrow(points, if (active) Color(0xFFFFD278) else Color(0xFF8AA0B4), boxSize * 0.03f)
    drawCircle(if (active) Color(0xFFFFD278) else Color(0xFFE7EEF8), boxSize * 0.018f, points.first())
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArrow(
    points: List<Offset>,
    color: Color,
    wing: Float,
) {
    var total = 0f
    for (i in 1 until points.size) total += hypot(points[i].x - points[i - 1].x, points[i].y - points[i - 1].y)
    if (total < 2f) return
    var walked = 0f
    for (i in 1 until points.size) {
        val a = points[i - 1]
        val b = points[i]
        val length = hypot(b.x - a.x, b.y - a.y)
        if (walked + length >= total * 0.55f && length > 0.5f) {
            val t = (total * 0.55f - walked) / length
            val tip = Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
            val angle = atan2(b.y - a.y, b.x - a.x)
            for (sign in floatArrayOf(-1f, 1f)) {
                val wingAngle = angle + Math.PI.toFloat() + sign * 0.7f
                val end = Offset(tip.x + wing * cos(wingAngle), tip.y + wing * sin(wingAngle))
                drawLine(color, tip, end, wing * 0.28f, cap = StrokeCap.Round)
            }
            return
        }
        walked += length
    }
}

@Composable
private fun TextButtonLabel(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C3A52), contentColor = Color.White),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(label, fontSize = 18.sp)
    }
}
