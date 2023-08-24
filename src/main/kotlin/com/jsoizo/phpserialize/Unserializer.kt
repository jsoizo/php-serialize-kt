package com.jsoizo.phpserialize

import com.jsoizo.phpserialize.error.PhpUnserializeException
import com.jsoizo.phpserialize.error.UncaughtUnserializeException
import com.jsoizo.phpserialize.error.UnexpectedValueException
import com.jsoizo.phpserialize.error.UnknownTypeException
import java.nio.charset.Charset

class Unserializer(private val stringCharset: Charset = Charsets.UTF_8) {
    fun unserialize(input: String): PValue {
        val iterator = input.iterator()
        try {
            return parseValue(iterator)
        } catch (e: Exception) {
            when (e) {
                is PhpUnserializeException -> throw e
                else -> throw UncaughtUnserializeException(e)
            }
        }
    }

    private fun parseValue(iterator: CharIterator): PValue {
        val type = iterator.next()
        if (type == 'N') return parseNull(iterator)
        iterator.next() // skip colon
        return when (type) {
            's' -> parseString(iterator)
            'i' -> parseInt(iterator)
            'd' -> parseDouble(iterator)
            'b' -> parseBoolean(iterator)
            'a' -> parseArray(iterator)
            'O' -> parseObject(iterator)
            'C' -> parseSerializable(iterator)
            else -> throw UnknownTypeException(type)
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
        return if (value == '1') PTrue else PFalse
    }

    private fun parseArray(iterator: CharIterator): PArray {
        val arraySize = iterator.readUntil(':').toInt()
        iterator.next() // skip opening brace
        val value: PArray = (0 until arraySize).fold(emptyPArray()) { acc, _ ->
            val key: PArrayKey = when (val parsed = parseValue(iterator)) {
                is PArrayKey -> parsed
                else -> throw UnexpectedValueException("Invalid Array Key.", parsed)
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
        val value: Map<String, PValue> = (0 until fieldsSize).fold(emptyMap()) { acc, _ ->
            val fieldName = when (val parsed = parseValue(iterator)) {
                is PString -> parsed.value
                else -> throw UnexpectedValueException("Invalid Object field.", parsed)
            }
            val value = parseValue(iterator)
            acc.plus((fieldName to value))
        }
        iterator.next() // skip closing brace
        return PObject(name, value)
    }

    private fun parseSerializable(iterator: CharIterator): PValue {
        val name = parseString(iterator).value
        val serializedDataSize = iterator.readUntil(':').toInt()
        iterator.next() // skip opening brace
        val serializedData = iterator.readNByte(serializedDataSize, stringCharset)
        val value = parseValue(serializedData.iterator())
        iterator.next() // skip closing brace
        return PSerializable(name, value)
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
            if (count <= 0) acc else {
                val char = next()
                val cur = if (Character.isHighSurrogate(char) && hasNext()) {
                    String(charArrayOf(char, next()))
                } else {
                    char.toString()
                }
                val byteCount = cur.toByteArray(charset).size
                _readNByte(count - byteCount, acc + cur)
            }
        return _readNByte(n, "")
    }

}
