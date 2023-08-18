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

    test("unserialize array") {
        val input = "a:4:{i:0;b:1;i:1;N;i:2;d:-421000000;i:3;s:6:\"A to Z\";}"
        val result = unserializer.unserialize(input)
        result.shouldBeInstanceOf<PArray>()
        result.size shouldBe 4
        result[PInt(0)] shouldBe PTrue
        result[PInt(1)] shouldBe PNull
        result[PInt(2)] shouldBe PDouble(-421000000.0)
        result[PInt(3)] shouldBe PString("A to Z")
    }

    test("unserialize nested array") {
        val input = "a:3:{i:42;b:1;s:6:\"A to Z\";a:3:{i:0;i:1;i:1;i:2;i:2;i:3;}i:99;N;}"
        val result = unserializer.unserialize(input)
        result.shouldBeInstanceOf<PArray>()
        result.size shouldBe 3
        result[PInt(42)] shouldBe PTrue
        val secondItem = result[PString("A to Z")].shouldBeInstanceOf<PArray>()
        secondItem.size shouldBe 3
        secondItem[PInt(0)] shouldBe PInt(1)
        secondItem[PInt(1)] shouldBe PInt(2)
        secondItem[PInt(2)] shouldBe PInt(3)
        result[PInt(99)] shouldBe PNull
    }

    test("unserialize object") {
        val input = "O:8:\"stdClass\":2:{s:4:\"John\";d:3.14;s:4:\"Jane\";d:2.718;}"
        val result = unserializer.unserialize(input)
        result.shouldBeInstanceOf<PObject>()
        result.name shouldBe "stdClass"
        result.size shouldBe 2
        result["John"] shouldBe PDouble(3.14)
        result["Jane"] shouldBe PDouble(2.718)
    }

    test("unserialize nested object") {
        val input =
            "O:6:\"Person\":3:{s:4:\"name\";s:8:\"John Doe\";s:7:\"address\";O:7:\"Address\":1:{s:6:\"street\";s:11:\"123 Main St\";}s:3:\"age\";i:25;}"
        val result = unserializer.unserialize(input)
        result.shouldBeInstanceOf<PObject>()
        result.name shouldBe "Person"
        result.size shouldBe 3
        result["name"] shouldBe PString("John Doe")
        val resultAddress = result["address"].shouldBeInstanceOf<PObject>()
        resultAddress.name shouldBe "Address"
        resultAddress.size shouldBe 1
        resultAddress["street"] shouldBe PString("123 Main St")
        result["age"] shouldBe PInt(25)
    }

    test("unserialize null") {
        val input = "N;"
        val result = unserializer.unserialize(input)
        result shouldBe PNull
    }
})
