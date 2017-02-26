package com.tschuchort.shadowlayout.misc

import android.graphics.Canvas
import android.graphics.Rect


interface Shadow {

	/**
	 * size of the shadow relative to the views edges
	 *
	 * example: if 5px of shadow are visisble on the left side of the view
	 * shadowSize.left is 5.
	 */
	val size: Rect

	fun draw(canvas: Canvas)
}