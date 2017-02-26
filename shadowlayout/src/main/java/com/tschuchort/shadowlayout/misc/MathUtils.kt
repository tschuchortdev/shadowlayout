package com.tschuchort.shadowlayout.misc

internal fun Float.ceil() = Math.ceil(this.toDouble())

internal fun Double.ceil() = Math.ceil(this)

internal fun Float.floor() = Math.floor(this.toDouble())

internal fun Double.floor() = Math.floor(this)

internal fun Float.round() = Math.round(this)

internal fun Double.round() = Math.round(this)

internal fun max(vararg args: Float): Float = args.max() ?: 0f

internal fun max(vararg args: Int): Int = args.max() ?: 0

internal fun ifPositive(num: Int) = if (num > 0) num else 0

internal fun ifPositive(num: Float) = if (num > 0) num else 0f

internal fun ifPositive(num: Double) = if (num > 0) num else 0.toDouble()
