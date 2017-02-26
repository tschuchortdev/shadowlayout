package com.tschuchort.misc

import android.content.res.TypedArray
import android.graphics.Rect
import android.view.View

/**
 * get raw view bounds relative to screen
 */
internal val View.absoluteBounds: Rect
	get() {
		val location = IntArray(2)
		getLocationOnScreen(location)
		return Rect(location[0], location[1], location[0] + width, location[1] + height)
	}

internal fun View.getBounds(): Rect {
	return Rect(left, top, right, bottom)
}

internal fun <R> TypedArray.useAndRecycle(f: TypedArray.()-> R) {
	try {
		this.f()
	}
	finally {
		this.recycle()
	}
}