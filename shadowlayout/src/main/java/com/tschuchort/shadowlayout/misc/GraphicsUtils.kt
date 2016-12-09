package com.tschuchort.shadowlayout.misc

import android.graphics.*
import android.support.v8.renderscript.Allocation
import android.support.v8.renderscript.Element
import android.support.v8.renderscript.RenderScript
import android.support.v8.renderscript.ScriptIntrinsicBlur
import android.util.Log

internal fun getResizedBitmap(bmp: Bitmap?, width: Int, height: Int): Bitmap {
	if(isLollipopOrNewer() && bmp != null) {
		try {
			bmp.reconfigure(width, height, Bitmap.Config.ARGB_8888)
			return bmp
		}
		catch(e: IllegalArgumentException) {
			Log.v("getResizedBitmap", "reconfigure failed, returning new bmp")
		}
	}

	bmp?.recycle()
	return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
}

internal fun Canvas.drawBitmapScaled(bmp: Bitmap, offsetX: Float, offsetY: Float, scale: Float, paint: Paint = Paint()) {
	return drawBitmapScaled(bmp,
			offsetX,
			offsetY,
			Math.round(bmp.width * scale),
			Math.round(bmp.height * scale),
			paint)
}

internal fun Canvas.drawBitmapScaled(bmp: Bitmap, offsetX: Float, offsetY: Float, scaledWidth: Int, scaledHeight: Int, paint: Paint = Paint()) {
	val m = Matrix()
	m.setTranslate(offsetX, offsetY)
	m.setScale(scaledWidth.toFloat() / bmp.width, scaledHeight.toFloat() / bmp.height)
	drawBitmap(bmp, m, paint)
}

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
internal fun colorBitmap(source: Bitmap, dest: Bitmap, offsetX: Float = 0f, offsetY: Float = 0f, color: Int) {
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

internal fun alpha(a: Float): Int {
	if (0 <= a && a <= 1)
		return Math.round(a * 255)
	else
		throw IllegalArgumentException("alpha value must be between 0 and 1")
}

