package com.jsoizo.phpserialize

sealed interface PValue

sealed interface PArrayKey

@JvmInline
value class PString(val value: String) : PValue, PArrayKey

@JvmInline
value class PInt(val value: Long) : PValue, PArrayKey

@JvmInline
value class PDouble(val value: Double) : PValue

@JvmInline
value class PBoolean(val value: Boolean) : PValue

@JvmInline
value class PArray(val value: LinkedHashMap<PArrayKey, PValue>) : PValue, Map<PArrayKey, PValue> by value {
    operator fun plus(item: Pair<PArrayKey, PValue>): PArray = this.apply { value.put(item.first, item.second) }
}

fun emptyPArray() = PArray(LinkedHashMap())

data class PObject(val name: String, val value: Map<String, PValue>) : PValue, Map<String, PValue> by value

data object PNull : PValue