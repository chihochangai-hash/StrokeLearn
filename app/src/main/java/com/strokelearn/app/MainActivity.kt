package com.strokelearn.app

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
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
    when (lesson) {
        Lesson.Home -> HomeScreen { lesson = it }
        Lesson.Numbers -> NumberScreen(tts) { lesson = Lesson.Home }
        Lesson.Zhuyin -> PracticeScreen(zhuyinGlyphs, "ㄅㄆㄇ", tts, onHome = { lesson = Lesson.Home })
        Lesson.Letters -> LetterScreen(tts) { lesson = Lesson.Home }
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
        modifier = Modifier.fillMaxSize().background(Color(0xFF071018)).padding(18.dp),
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
private fun NumberScreen(tts: TextToSpeech?, home: () -> Unit) {
    var number by remember { mutableIntStateOf(1) }
    var step by remember(number) { mutableIntStateOf(0) }
    val done = remember(number) { mutableStateListOf<List<Offset>>() }
    val active = remember(number, step) { mutableStateListOf<Offset>() }
    var wrong by remember(number, step) { mutableStateOf(false) }
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

    Column(Modifier.fillMaxSize().background(Color(0xFF071018))) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButtonLabel("回") { home() }
            Text(
                "$number / 100   ${step + 1}/${plan.size}",
                color = Color(0xFFD5E6F2),
                fontSize = 22.sp,
            )
            Row {
                TextButtonLabel("念") { speakText(tts, numberWords(number), Locale.TAIWAN, "n$number") }
                TextButtonLabel("上") { go(number - 1) }
                TextButtonLabel("下") { go(number + 1) }
            }
        }
        Canvas(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(number, step) {
                    detectDragGestures(
                        onDragStart = { start ->
                            wrong = false
                            active.clear()
                            active.add(start)
                        },
                        onDrag = { change, _ ->
                            val last = active.lastOrNull()
                            val point = change.position
                            if (last == null || hypot(point.x - last.x, point.y - last.y) > 4f) {
                                active.add(point)
                            }
                            change.consume()
                        },
                        onDragEnd = {
                            val boxes = digitBoxes(
                                androidx.compose.ui.geometry.Size(size.width.toFloat(), size.height.toFloat()),
                                digits.size,
                            )
                            if (strokeAccepted(active.toList(), stroke, boxes[digitIndex])) {
                                done.add(active.toList())
                                active.clear()
                                if (step + 1 >= plan.size) go(number + 1) else step += 1
                            } else {
                                wrong = true
                                active.clear()
                            }
                        },
                        onDragCancel = { active.clear() },
                    )
                },
        ) {
            val boxes = digitBoxes(size, digits.size)
            val guide = Color(0xFF6E8BA3)
            val ink = if (wrong) Color(0xFFFF6B6B) else Color(0xFFF7F7F2)
            digits.forEachIndexed { index, digit ->
                val box = boxes[index]
                digitStrokes(digit).forEachIndexed { strokeAt, points ->
                    val current = index == digitIndex && plan.take(step + 1).count { it.first == index } - 1 == strokeAt
                    drawStrokeGuide(points.map { it.toOffset(box) }, if (current) Color(0xFF7AD7FF) else guide, box.size, current)
                }
            }
            val width = boxes.first().size * 0.045f
            (done + listOf(active.toList())).forEach { trace ->
                for (p in 1 until trace.size) {
                    drawLine(ink, trace[p - 1], trace[p], width, cap = StrokeCap.Round)
                }
            }
        }
    }
}

@Composable
private fun LetterScreen(tts: TextToSpeech?, home: () -> Unit) {
    var upper by remember { mutableStateOf(true) }
    PracticeScreen(
        glyphs = if (upper) upperGlyphs else lowerGlyphs,
        title = if (upper) "ABC" else "abc",
        tts = tts,
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
    onHome: () -> Unit,
    extra: @Composable () -> Unit = {},
) {
    var index by remember(glyphs) { mutableIntStateOf(0) }
    var strokeIndex by remember(glyphs, index) { mutableIntStateOf(0) }
    val done = remember(glyphs, index) { mutableStateListOf<List<Offset>>() }
    val active = remember(glyphs, index, strokeIndex) { mutableStateListOf<Offset>() }
    var wrong by remember(glyphs, index, strokeIndex) { mutableStateOf(false) }
    val glyph = glyphs[index]
    DisposableEffect(glyph.label, glyph.speak) {
        speak(tts, glyph)
        onDispose { }
    }

    fun go(next: Int) {
        index = (next + glyphs.size) % glyphs.size
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF071018))) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButtonLabel("回") { onHome() }
            Text(
                "$title  ${glyph.label}  ${strokeIndex + 1}/${glyph.strokes.size}",
                color = Color(0xFFD5E6F2),
                fontSize = 20.sp,
            )
            Row {
                extra()
                TextButtonLabel("念") { speak(tts, glyph) }
                TextButtonLabel("上") { go(index - 1) }
                TextButtonLabel("下") { go(index + 1) }
            }
        }
        Canvas(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(glyph.label, strokeIndex) {
                    detectDragGestures(
                        onDragStart = { start ->
                            wrong = false
                            active.clear()
                            active.add(start)
                        },
                        onDrag = { change, _ ->
                            val last = active.lastOrNull()
                            val point = change.position
                            if (last == null || hypot(point.x - last.x, point.y - last.y) > 4f) {
                                active.add(point)
                            }
                            change.consume()
                        },
                        onDragEnd = {
                            val box = glyphBox(androidx.compose.ui.geometry.Size(size.width.toFloat(), size.height.toFloat()))
                            val stroke = glyph.strokes[strokeIndex]
                            if (strokeAccepted(active.toList(), stroke, box)) {
                                done.add(active.toList())
                                active.clear()
                                if (strokeIndex + 1 >= glyph.strokes.size) {
                                    go(index + 1)
                                } else {
                                    strokeIndex += 1
                                }
                            } else {
                                wrong = true
                                active.clear()
                            }
                        },
                        onDragCancel = { active.clear() },
                    )
                },
        ) {
            val box = glyphBox(size)
            val guide = Color(0xFF6E8BA3)
            val activeColor = Color(0xFF7AD7FF)
            val ink = if (wrong) Color(0xFFFF6B6B) else Color(0xFFF7F7F2)
            glyph.strokes.forEachIndexed { i, stroke ->
                drawStrokeGuide(
                    stroke.map { it.toOffset(box) },
                    if (i == strokeIndex) activeColor else guide,
                    box.size,
                    i == strokeIndex,
                )
            }
            val width = box.size * 0.045f
            (done + listOf(active.toList())).forEach { trace ->
                for (p in 1 until trace.size) {
                    drawLine(ink, trace[p - 1], trace[p], width, cap = StrokeCap.Round)
                }
            }
        }
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
