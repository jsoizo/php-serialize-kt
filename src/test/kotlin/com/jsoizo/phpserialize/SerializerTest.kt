package com.jsoizo.phpserialize

import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe

class SerializerTest : FunSpec({
    val serializer = Serializer(Charsets.UTF_8)

    test("serialize string") {
        forAll(
            table(
                headers("case", "input", "expected"),
                row("any string", "apple", "s:5:\"apple\";"),
                row("empty", "", "s:0:\"\";"),
                row("multibyte", "こんにちは\uD83C\uDF85", "s:19:\"こんにちは\uD83C\uDF85\";"),
                row("special chars", "\"hello:world;\"", "s:14:\"\"hello:world;\"\";")
            )
        ) { _, input, expected ->
            val result = serializer.serialize(PString(input))
            result shouldBe expected
        }
    }

    test("serialize number") {
        forAll(
            table(
                headers("case", "input", "expected"),
                row("PHP_INT_MAX(64bit)", 9223372036854775807L, "i:9223372036854775807;"),
                row("PHP_INT_MIN(64bit)", -9223372036854775807L - 1L, "i:-9223372036854775808;"),
                row("zero", 0L, "i:0;")
            )
        ) { _, input, expected ->
            val result = serializer.serialize(PInt(input))
            result shouldBe expected
        }
    }

    test("serialize double") {
        forAll(
            table(
                headers("case", "input", "expected"),
                row("Any fractional number", 685230.15, "d:685230.15;"),
                row("Infinity", Double.POSITIVE_INFINITY, "d:INF;"),
                row("Negative Infinity", Double.NEGATIVE_INFINITY, "d:-INF;"),
                row("NaN", Double.NaN, "d:NaN;")
            )
        ) { _, input, expected ->
            val result = serializer.serialize(PDouble(input))
            result shouldBe expected
        }
    }

    test("serialize boolean") {
        forAll(
            table(
                headers("case", "input", "expected"),
                row("true", PTrue, "b:1;"),
                row("false", PFalse, "b:0;")
            )
        ) { _, input, expected ->
            val result = serializer.serialize(input)
            result shouldBe expected
        }
    }

    test("serialize array") {
        val input = pArrayOf(
            PInt(0) to PTrue,
            PInt(1) to PNull,
            PInt(2) to PDouble(-421000000.0),
            PInt(3) to PString("A to Z")
        )
        val result = serializer.serialize(input)
        result shouldBe "a:4:{i:0;b:1;i:1;N;i:2;d:-421000000;i:3;s:6:\"A to Z\";}"
    }

    test("serialize nested array") {
        val input = pArrayOf(
            PInt(42) to PTrue,
            PString("A to Z") to PArray(
                LinkedHashMap(
                    mapOf(
                        PInt(0) to PInt(1),
                        PInt(1) to PInt(2),
                        PInt(2) to PInt(3)
                    )
                )
            ),
            PInt(99) to PNull
        )
        val result = serializer.serialize(input)
        result shouldBe "a:3:{i:42;b:1;s:6:\"A to Z\";a:3:{i:0;i:1;i:1;i:2;i:2;i:3;}i:99;N;}"
    }

    test("serialize object") {
        val input = PObject(
            name = "stdClass",
            value = mapOf(
                "John" to PDouble(3.14),
                "Jane" to PDouble(2.718)
            )
        )
        val result = serializer.serialize(input)
        result shouldBe "O:8:\"stdClass\":2:{s:4:\"John\";d:3.14;s:4:\"Jane\";d:2.718;}"
    }

    test("serialize nested object") {
        val input = PObject(
            name = "Person",
            value = mapOf(
                "name" to PString("John Doe"),
                "address" to PObject(
                    name = "Address",
                    value = mapOf(
                        "street" to PString("123 Main St")
                    )
                ),
                "age" to PInt(25)
            )
        )
        val result = serializer.serialize(input)
        val expected =
            "O:6:\"Person\":3:{s:4:\"name\";s:8:\"John Doe\";s:7:\"address\";O:7:\"Address\":1:{s:6:\"street\";s:11:\"123 Main St\";}s:3:\"age\";i:25;}"
        result shouldBe expected
    }

    test("serialize null") {
        val result = serializer.serialize(PNull)
        result shouldBe "N;"
    }
})
