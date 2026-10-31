package com.strokelearn.app

internal data class Pt(val x: Float, val y: Float)

internal data class Glyph(
    val label: String,
    val speak: String,
    val locale: String,
    val strokes: List<List<Pt>>,
)

private fun s(vararg pairs: Float) = pairs.toList().chunked(2).map { Pt(it[0], it[1]) }

internal val numberGlyphs: List<Glyph> = listOf(
    digit("1", "一", s(50f, 10f, 50f, 90f)),
    digit("2", "二", s(17f, 24f, 25f, 13f, 42f, 8f, 62f, 9f, 78f, 20f, 84f, 34f, 79f, 45f, 66f, 56f, 49f, 67f, 31f, 79f, 16f, 89f, 84f, 89f)),
    digit("3", "三", s(23f, 16f, 39f, 9f, 60f, 9f, 76f, 18f, 82f, 30f, 76f, 40f, 63f, 47f, 49f, 50f, 63f, 52f, 76f, 59f, 82f, 70f, 77f, 82f, 62f, 90f, 42f, 91f, 27f, 85f, 18f, 76f)),
    digit("4", "四", s(75f, 8f, 18f, 56f, 82f, 56f), s(67f, 20f, 67f, 91f)),
    digit("5", "五", s(22f, 10f, 21f, 44f, 47f, 44f, 65f, 48f, 78f, 58f, 83f, 70f, 78f, 82f, 64f, 90f, 45f, 91f, 28f, 85f, 18f, 77f), s(22f, 10f, 81f, 10f)),
    digit("6", "六", s(75f, 11f, 59f, 20f, 42f, 35f, 27f, 53f, 20f, 69f, 23f, 82f, 36f, 90f, 54f, 91f, 70f, 83f, 80f, 69f, 78f, 58f, 66f, 51f, 50f, 49f, 35f, 54f, 24f, 64f)),
    digit("7", "七", s(16f, 10f, 84f, 10f, 40f, 91f)),
    digit("8", "八", s(52f, 8f, 37f, 11f, 25f, 21f, 23f, 33f, 33f, 43f, 50f, 50f, 66f, 57f, 78f, 68f, 77f, 80f, 67f, 89f, 51f, 92f, 36f, 89f, 25f, 80f, 23f, 68f, 34f, 57f, 50f, 50f, 65f, 44f, 77f, 34f, 78f, 22f, 68f, 12f, 52f, 8f)),
    digit("9", "九", s(74f, 25f, 62f, 12f, 45f, 9f, 29f, 15f, 19f, 30f, 18f, 45f, 25f, 58f, 39f, 65f, 55f, 64f, 69f, 56f, 76f, 44f, 76f, 60f, 76f, 76f, 76f, 91f)),
    digit("0", "零", s(50f, 8f, 33f, 12f, 20f, 25f, 12f, 42f, 13f, 59f, 21f, 76f, 35f, 88f, 50f, 92f, 65f, 88f, 79f, 76f, 87f, 59f, 88f, 42f, 80f, 25f, 67f, 12f, 50f, 8f)),
)

private fun digit(label: String, speak: String, vararg strokes: List<Pt>) =
    Glyph(label, speak, "zh-TW", strokes.toList())

internal fun digitStrokes(digit: Int): List<List<Pt>> =
    numberGlyphs.first { it.label == digit.toString() }.strokes

private fun letter(label: String, vararg strokes: List<Pt>) =
    Glyph(label, label, "en-US", strokes.toList())

internal val upperGlyphs: List<Glyph> = listOf(
    letter("A", s(50f, 8f, 16f, 90f), s(50f, 8f, 84f, 90f), s(28f, 58f, 72f, 58f)),
    letter("B", s(22f, 8f, 22f, 92f), s(22f, 8f, 58f, 8f, 78f, 20f, 78f, 36f, 58f, 48f, 22f, 48f), s(22f, 48f, 62f, 48f, 82f, 62f, 82f, 78f, 62f, 92f, 22f, 92f)),
    letter("C", s(80f, 24f, 62f, 10f, 34f, 10f, 16f, 28f, 12f, 50f, 16f, 74f, 34f, 90f, 62f, 90f, 80f, 76f)),
    letter("D", s(22f, 10f, 22f, 90f), s(22f, 10f, 54f, 10f, 80f, 26f, 86f, 50f, 80f, 74f, 54f, 90f, 22f, 90f)),
    letter("E", s(24f, 10f, 24f, 90f), s(24f, 10f, 80f, 10f), s(24f, 50f, 64f, 50f), s(24f, 90f, 80f, 90f)),
    letter("F", s(24f, 10f, 24f, 92f), s(24f, 10f, 80f, 10f), s(24f, 50f, 66f, 50f)),
    letter("G", s(78f, 26f, 58f, 10f, 32f, 12f, 14f, 32f, 12f, 52f, 18f, 76f, 36f, 90f, 62f, 90f, 80f, 72f, 80f, 52f, 50f, 52f)),
    letter("H", s(22f, 10f, 22f, 90f), s(78f, 10f, 78f, 90f), s(22f, 50f, 78f, 50f)),
    letter("I", s(32f, 10f, 68f, 10f), s(50f, 10f, 50f, 90f), s(32f, 90f, 68f, 90f)),
    letter("J", s(36f, 10f, 74f, 10f), s(58f, 10f, 58f, 70f, 50f, 86f, 32f, 86f, 22f, 74f)),
    letter("K", s(24f, 10f, 24f, 90f), s(78f, 12f, 24f, 52f), s(40f, 44f, 82f, 90f)),
    letter("L", s(24f, 10f, 24f, 90f, 80f, 90f)),
    letter("M", s(14f, 90f, 14f, 12f), s(14f, 12f, 50f, 58f), s(50f, 58f, 86f, 12f), s(86f, 12f, 86f, 90f)),
    letter("N", s(20f, 90f, 20f, 12f), s(20f, 12f, 80f, 90f), s(80f, 90f, 80f, 12f)),
    letter("O", s(50f, 8f, 32f, 14f, 16f, 32f, 14f, 50f, 16f, 70f, 32f, 86f, 50f, 92f, 68f, 86f, 84f, 70f, 86f, 50f, 84f, 32f, 68f, 14f, 50f, 8f)),
    letter("P", s(24f, 10f, 24f, 92f), s(24f, 10f, 58f, 10f, 80f, 24f, 80f, 40f, 58f, 52f, 24f, 52f)),
    letter("Q", s(50f, 8f, 30f, 16f, 16f, 36f, 14f, 54f, 22f, 74f, 40f, 88f, 58f, 88f, 76f, 74f, 84f, 54f, 82f, 34f, 68f, 16f, 50f, 8f), s(62f, 68f, 86f, 92f)),
    letter("R", s(24f, 10f, 24f, 92f), s(24f, 10f, 58f, 10f, 78f, 24f, 78f, 40f, 56f, 52f, 24f, 52f), s(48f, 52f, 82f, 92f)),
    letter("S", s(76f, 22f, 58f, 10f, 34f, 12f, 20f, 26f, 22f, 40f, 40f, 50f, 62f, 56f, 78f, 68f, 76f, 82f, 58f, 92f, 32f, 90f, 18f, 78f)),
    letter("T", s(16f, 12f, 84f, 12f), s(50f, 12f, 50f, 90f)),
    letter("U", s(20f, 12f, 20f, 66f, 34f, 88f, 50f, 92f, 66f, 88f, 80f, 66f, 80f, 12f)),
    letter("V", s(14f, 12f, 50f, 90f, 86f, 12f)),
    letter("W", s(8f, 12f, 26f, 90f, 50f, 36f, 74f, 90f, 92f, 12f)),
    letter("X", s(16f, 12f, 84f, 90f), s(84f, 12f, 16f, 90f)),
    letter("Y", s(16f, 12f, 50f, 48f), s(84f, 12f, 50f, 48f, 50f, 90f)),
    letter("Z", s(16f, 12f, 84f, 12f, 16f, 90f, 84f, 90f)),
)

internal val lowerGlyphs: List<Glyph> = listOf(
    letter("a", s(72f, 46f, 56f, 34f, 36f, 36f, 24f, 50f, 26f, 68f, 42f, 80f, 62f, 78f, 74f, 62f), s(74f, 36f, 74f, 82f)),
    letter("b", s(28f, 10f, 28f, 84f), s(28f, 48f, 46f, 40f, 66f, 48f, 72f, 64f, 60f, 80f, 40f, 82f, 28f, 68f)),
    letter("c", s(74f, 42f, 58f, 34f, 38f, 38f, 26f, 54f, 30f, 72f, 48f, 82f, 68f, 76f)),
    letter("d", s(72f, 10f, 72f, 84f), s(72f, 48f, 54f, 40f, 34f, 48f, 28f, 64f, 40f, 80f, 60f, 82f, 72f, 68f)),
    letter("e", s(28f, 58f, 72f, 54f, 68f, 40f, 48f, 34f, 30f, 46f, 28f, 64f, 42f, 80f, 66f, 78f)),
    letter("f", s(62f, 14f, 48f, 10f, 38f, 20f, 36f, 90f), s(22f, 46f, 58f, 46f)),
    letter("g", s(70f, 36f, 52f, 30f, 34f, 40f, 28f, 56f, 40f, 70f, 60f, 68f, 70f, 52f), s(70f, 36f, 70f, 82f, 56f, 94f, 36f, 90f)),
    letter("h", s(28f, 10f, 28f, 84f), s(28f, 52f, 46f, 40f, 64f, 48f, 70f, 60f, 70f, 84f)),
    letter("i", s(48f, 8f, 48f, 24f), s(48f, 38f, 48f, 84f)),
    letter("j", s(58f, 8f, 58f, 24f), s(58f, 38f, 58f, 82f, 46f, 94f, 30f, 88f)),
    letter("k", s(30f, 10f, 30f, 84f), s(68f, 42f, 30f, 62f), s(42f, 58f, 72f, 84f)),
    letter("l", s(48f, 10f, 48f, 84f)),
    letter("m", s(18f, 84f, 18f, 42f), s(18f, 52f, 32f, 40f, 46f, 50f, 46f, 84f), s(46f, 52f, 60f, 40f, 76f, 50f, 76f, 84f)),
    letter("n", s(26f, 84f, 26f, 40f), s(26f, 52f, 44f, 40f, 64f, 50f, 68f, 62f, 68f, 84f)),
    letter("o", s(50f, 34f, 34f, 40f, 24f, 56f, 30f, 74f, 50f, 82f, 70f, 74f, 76f, 56f, 66f, 40f, 50f, 34f)),
    letter("p", s(28f, 36f, 28f, 96f), s(28f, 46f, 48f, 36f, 68f, 46f, 72f, 62f, 58f, 76f, 38f, 78f, 28f, 64f)),
    letter("q", s(72f, 36f, 72f, 96f), s(72f, 46f, 52f, 36f, 32f, 46f, 28f, 62f, 42f, 76f, 62f, 78f, 72f, 64f)),
    letter("r", s(30f, 84f, 30f, 40f), s(30f, 54f, 48f, 40f, 68f, 42f)),
    letter("s", s(70f, 42f, 52f, 34f, 34f, 42f, 40f, 54f, 62f, 62f, 66f, 74f, 48f, 82f, 28f, 74f)),
    letter("t", s(42f, 16f, 42f, 78f, 54f, 86f, 68f, 80f), s(24f, 42f, 62f, 42f)),
    letter("u", s(26f, 38f, 26f, 68f, 38f, 82f, 54f, 82f, 66f, 68f, 68f, 38f), s(68f, 38f, 68f, 84f)),
    letter("v", s(18f, 38f, 46f, 84f, 78f, 38f)),
    letter("w", s(12f, 38f, 28f, 84f, 48f, 50f, 68f, 84f, 86f, 38f)),
    letter("x", s(22f, 38f, 74f, 84f), s(74f, 38f, 22f, 84f)),
    letter("y", s(20f, 38f, 42f, 68f, 58f, 46f), s(72f, 38f, 46f, 84f, 28f, 96f)),
    letter("z", s(24f, 40f, 74f, 40f, 24f, 84f, 74f, 84f)),
)
