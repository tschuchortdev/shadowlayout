package com.tschuchort.shadowlayout.misc

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.support.v8.renderscript.Allocation
import android.support.v8.renderscript.Element
import android.support.v8.renderscript.RenderScript
import android.support.v8.renderscript.ScriptIntrinsicBlur


internal fun blurBitmap(bmp: Bitmap, rs: RenderScript, radius: Float) {
	blurBitmap(bmp, bmp, rs, radius)
}

/**
 * blurs a bitmap with gaußian blur
 */
internal fun blurBitmap(sourceBmp: Bitmap, destBmp: Bitmap, rs: RenderScript, radius: Float) {
	val inAllocation = Allocation.createFromBitmap(rs, sourceBmp,
			Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT)

	val outAllocation = Allocation.createTyped(rs, inAllocation.type)
	val blurShader = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))

	blurShader.setRadius(radius)
	blurShader.setInput(inAllocation)
	blurShader.forEach(outAllocation)

	outAllocation.copyTo(destBmp)
}

/**
 * transforms the bitmap to black, preserving only alpha values
 */
internal fun transformToColor(source: Bitmap, dest: Bitmap, offsetX: Float = 0f, offsetY: Float = 0f, color: Int) {
	val paint = Paint()
	val destCanvas = Canvas(dest)

	val a = color shr 24 and 0xFF
	val r = color shr 16 and 0xFF
	val g = color shr 8 and 0xFF
	val b = color and 0xFF

	paint.colorFilter = ColorMatrixColorFilter(floatArrayOf(
			0f, 0f, 0f, 0f, r.toFloat(),
			0f, 0f, 0f, 0f, g.toFloat(),
			0f, 0f, 0f, 0f, b.toFloat(),
			0f, 0f, 0f, a.toFloat()/0xFF, 0f
	))

	destCanvas.drawBitmap(source, offsetX, offsetY, paint)
}

