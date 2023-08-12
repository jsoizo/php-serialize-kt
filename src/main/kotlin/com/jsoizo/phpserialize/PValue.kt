package com.jsoizo.phpserialize

sealed interface PValue

@JvmInline
value class PString(val value: String) : PValue

@JvmInline
value class PInt(val value: Long) : PValue

@JvmInline
value class PDouble(val value: Double) : PValue

@JvmInline
value class PBoolean(val value: Boolean) : PValue

@JvmInline
value class PArray(val value: Map<PValue, PValue>) : PValue

@JvmInline
value class PObject(val value: Map<PString, PValue>) : PValue

data object PNull : PValue