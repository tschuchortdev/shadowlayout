package com.tschuchort.shadowlayout.misc

/**
 * Created by thilo on 14.10.16.
 */


internal fun Float.round() = Math.round(this)

internal fun Double.round() = Math.round(this)

internal fun max(vararg args: Float): Float {
	var biggest: Float = 0f

	for(arg in args)
		biggest = Math.max(arg, biggest)

	return biggest
}

internal fun max(vararg args: Int): Int {
	var biggest: Int = 0

	for(arg in args)
		biggest = Math.max(arg, biggest)

	return biggest
}