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
value class PArray(val value: LinkedHashMap<PArrayKey, PValue>) : PValue {
    operator fun plus(item: Pair<PArrayKey, PValue>): PArray = this.apply { value.put(item.first, item.second) }
}

fun emptyPArray() = PArray(LinkedHashMap())

@JvmInline
value class PObject(val value: Map<PString, PValue>) : PValue

data object PNull : PValue