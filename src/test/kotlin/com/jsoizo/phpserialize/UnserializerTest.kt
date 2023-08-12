package com.jsoizo.phpserialize

import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class UnserializerTest : FunSpec({
    val unserializer = Unserializer(Charsets.UTF_8)

    test("unserialize string") {
        forAll(
            table(
                headers("case", "input", "expected"),
                row("any string", "s:5:\"apple\";", "apple"),
                row("empty", "s:0:\"\";", ""),
                row("multibyte", "s:19:\"こんにちは\uD83C\uDF85\";", "こんにちは\uD83C\uDF85"),
                row("special chars", "s:14:\"\"hello:world;\"\";", "\"hello:world;\"")
            )
        ) { _, input, expected ->
            val result = unserializer.unserialize(input)
            result.shouldBeInstanceOf<PString>()
            result.value shouldBe expected
        }
    }

    test("unserialize number") {
        forAll(
            table(
                headers("case", "input", "expected"),
                row("PHP_INT_MAX(64bit)", "i:9223372036854775807;", 9223372036854775807L),
                row("PHP_INT_MIN(64bit)", "i:-9223372036854775808;", -9223372036854775807L - 1L),
                row("zero", "i:0;", 0L)
            )
        ) { _, input, expected ->
            val result = unserializer.unserialize(input)
            result.shouldBeInstanceOf<PInt>()
            result.value shouldBe expected
        }
    }

    test("unserialize double") {
        forAll(
            table(
                headers("case", "input", "expected"),
                row("Any fractional number", "685230.15", 685230.15),
                row("Infinity", "INF", Double.POSITIVE_INFINITY),
                row("Negative Infinity", "-INF", Double.NEGATIVE_INFINITY),
                row("NaN", "NaN", Double.NaN)
            )
        ) { _, inputStr, expected ->
            val input = "d:$inputStr;"
            val result = unserializer.unserialize(input)
            result.shouldBeInstanceOf<PDouble>()
            result.value shouldBe expected
        }
    }

    test("unserialize boolean") {
        forAll(
            table(
                headers("case", "input", "expected"),
                row("true", "1", true),
                row("false", "0", false)
            )
        ) { _, inputStr, expected ->
            val input = "b:$inputStr;"
            val result = unserializer.unserialize(input)
            result.shouldBeInstanceOf<PBoolean>()
            result.value shouldBe expected
        }
    }

    test("unserialize null") {
        val input = "N;"
        val result = unserializer.unserialize(input)
        result shouldBe PNull
    }
})