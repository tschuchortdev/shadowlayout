package com.tschuchort.misc

import android.graphics.Rect
import android.view.View

/**
 * get raw view bounds relative to screen
 */
internal fun View.getAbsoluteBounds(): Rect {
	val location = IntArray(2)
	getLocationOnScreen(location)
	return Rect(location[0], location[1], location[0] + width, location[1] + height)
}

internal fun View.getBounds(): Rect {
	return Rect(left, top, right, bottom)
}