package com.jsoizo.phpserialize.error

import com.jsoizo.phpserialize.PValue

sealed class PhpUnserializeException(
    override val message: String, override val cause: Throwable?
) : Exception()

data class UnknownTypeException(val type: Char) : PhpUnserializeException("Unknown Type: $type", null)

data class UncaughtUnserializeException(override val cause: Throwable) :
    PhpUnserializeException("Uncaught Unserialize Error", cause)

data class UnexpectedValueException(override val message: String, private val value: PValue) :
    PhpUnserializeException("$message value: $value", null)