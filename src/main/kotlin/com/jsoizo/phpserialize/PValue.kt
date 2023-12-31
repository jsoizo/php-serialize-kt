package com.jsoizo.phpserialize

sealed interface PValue

sealed interface PArrayKey

@JvmInline
value class PString(val value: String) : PValue, PArrayKey

@JvmInline
value class PInt(val value: Long) : PValue, PArrayKey

@JvmInline
value class PDouble(val value: Double) : PValue

sealed interface PBoolean: PValue {
    val value: Boolean
}

object PTrue : PBoolean {
    override val value: Boolean = true
    override fun toString(): String = "PTrue"
}

object PFalse : PBoolean {
    override val value: Boolean = false
    override fun toString(): String = "PFalse"
}

data class PArray(private val value: LinkedHashMap<PArrayKey, PValue>) : PValue, Map<PArrayKey, PValue> by value {
    operator fun plus(item: Pair<PArrayKey, PValue>): PArray = this.apply { value[item.first] = item.second }
    fun get(key: String): PValue? = value[PString(key)]
    fun get(key: Long): PValue? = value[PInt(key)]
}

fun emptyPArray() = PArray(LinkedHashMap())
fun pArrayOf(vararg pairs: Pair<PArrayKey, PValue>) = emptyPArray().apply { pairs.forEach { this + it } }

data class PObject(val name: String, val value: Map<String, PValue>) : PValue, Map<String, PValue> by value

data class PSerializable(val name: String, val value: PValue): PValue

object PNull : PValue {
    override fun toString(): String = "PNull"
}
