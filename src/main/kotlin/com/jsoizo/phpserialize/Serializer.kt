package com.jsoizo.phpserialize

import java.nio.charset.Charset

class Serializer(private val stringCharset: Charset = Charsets.UTF_8) {
    fun serialize(value: PValue): String = when (value) {
        is PString -> serializeString(value)
        is PInt -> serializeInt(value)
        is PDouble -> serializeDouble(value)
        is PBoolean -> serializeBoolean(value)
        is PArray -> serializeArray(value)
        is PObject -> serializeObject(value)
        is PNull -> serializeNull()
    }

    private fun serializeString(value: PString): String {
        val bytes = value.value.toByteArray(stringCharset)
        return "s:${bytes.size}:\"${value.value}\";"
    }

    private fun serializeInt(value: PInt): String {
        return "i:${value.value};"
    }

    private fun serializeDouble(value: PDouble): String {
        val valueStr =
            when (value.value) {
                Double.POSITIVE_INFINITY -> "INF"
                Double.NEGATIVE_INFINITY -> "-INF"
                // https://www.php.net/manual/en/language.types.float.php
                else -> String.format("%.15f", value.value).trimEnd('0').trimEnd('.')
            }
        return "d:${valueStr};"
    }

    private fun serializeBoolean(value: PBoolean): String {
        return "b:${if (value.value) '1' else '0'};"
    }

    private fun serializeArray(value: PArray): String {
        val serializedElements = value.value.entries.joinToString("") { (key, v) ->
            serialize(key as PValue) + serialize(v)
        }
        return "a:${value.value.size}:{${serializedElements}}"
    }

    private fun serializeObject(value: PObject): String {
        val serializedFields = value.entries.joinToString("") { (key, v) ->
            serialize(PString(key)) + serialize(v)
        }
        return "O:${value.name.length}:\"${value.name}\":${value.size}:{${serializedFields}}"
    }

    private fun serializeNull(): String {
        return "N;"
    }
}
