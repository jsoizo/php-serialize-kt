package com.jsoizo.phpserialize

import java.nio.charset.Charset

class Unserializer(private val stringCharset: Charset = Charsets.UTF_8) {
    fun unserialize(input: String): PValue {
        val iterator = input.iterator()
        return parseValue(iterator)
    }

    private fun parseValue(iterator: CharIterator): PValue {
        val type = iterator.next()
        return if (type == 'N') parseNull(iterator)
        else {
            iterator.next() // skip colon
            when (type) {
                's' -> parseString(iterator)
                'i' -> parseInt(iterator)
                'd' -> parseDouble(iterator)
                'b' -> parseBoolean(iterator)
                'a' -> parseArray(iterator)
                'O' -> parseObject(iterator)
                else -> throw IllegalArgumentException("Unknown type: $type")
            }
        }
    }

    private fun parseString(iterator: CharIterator): PString {
        val lengthInBytes = iterator.readUntil(':').toInt()
        iterator.next() // Skip opening quote
        val value = iterator.readNByte(lengthInBytes, stringCharset)
        iterator.next() // Skip closing quote
        iterator.next() // Skip semicolon
        return PString(value)
    }

    private fun parseInt(iterator: CharIterator): PInt {
        val value = iterator.readUntil(';').toLong()
        return PInt(value)
    }

    private fun parseDouble(iterator: CharIterator): PDouble {
        val value = when (val parsed = iterator.readUntil(';')) {
            "INF" -> Double.POSITIVE_INFINITY
            "-INF" -> Double.NEGATIVE_INFINITY
            "NAN" -> Double.NaN
            else -> parsed.toDouble()
        }
        return PDouble(value)
    }

    private fun parseBoolean(iterator: CharIterator): PBoolean {
        val value = iterator.readUntil(';').firstOrNull()
        return PBoolean(value == '1')
    }

    private fun parseArray(iterator: CharIterator): PArray {
        val arraySize = iterator.readUntil(':').toInt()
        iterator.next() // skip opening brace
        val value: PArray = (0..<arraySize).fold(emptyPArray()) { acc, _ ->
            val key: PArrayKey = when (val parsed = parseValue(iterator)) {
                is PArrayKey -> parsed
                else -> throw IllegalArgumentException("Invalid Array Key")
            }
            val value = parseValue(iterator)
            acc + (key to value)
        }
        iterator.next() // skip closing brace
        return value
    }

    private fun parseObject(iterator: CharIterator): PObject {
        val name = parseString(iterator).value
        val fieldsSize = iterator.readUntil(':').toInt()
        iterator.next() // skip opening brace
        val value: Map<String, PValue> = (0..<fieldsSize).fold(emptyMap()) { acc, _ ->
            val fieldName = when (val parsed = parseValue(iterator)) {
                is PString -> parsed.value
                else -> throw IllegalArgumentException("Invalid Object field")
            }
            val value = parseValue(iterator)
            acc.plus((fieldName to value))
        }
        iterator.next() // skip closing brace
        return PObject(name, value)
    }

    private fun parseNull(iterator: CharIterator): PNull {
        iterator.next() // skip semicolon
        return PNull
    }

    private fun CharIterator.readUntil(char: Char): String {
        val sb = StringBuilder()
        while (hasNext()) {
            val c = next()
            if (c == char) break
            sb.append(c)
        }
        return sb.toString()
    }

    private fun CharIterator.readNByte(n: Int, charset: Charset): String {
        tailrec fun _readNByte(count: Int, acc: String): String =
            if (count > 0) {
                val char = next()
                val cur = if (Character.isHighSurrogate(char) && hasNext()) {
                    char.toString() + next().toString()
                } else {
                    char.toString()
                }
                val byteCount = cur.toByteArray(charset).size
                _readNByte(count - byteCount, acc + cur)
            } else acc
        return _readNByte(n, "")
    }

}