package com.tschuchort.shadowlayout.misc

import android.annotation.TargetApi
import android.graphics.*
import android.support.v8.renderscript.Allocation
import android.support.v8.renderscript.Element
import android.support.v8.renderscript.RenderScript
import android.support.v8.renderscript.ScriptIntrinsicBlur
import android.util.Log

/**
 * tries to reuse the bitmap if possible, otherwise creates a new bitmap
 * the bitmap will be freed in the process
 */
internal fun getResizedBitmap(bmp: Bitmap, width: Int, height: Int): Bitmap {
	if(bmp.width != width && bmp.height != height) {
		@TargetApi(19)
		if (isKitKatOrNewer()) {
			try {
				bmp.reconfigure(width, height, bmp.config)
				return bmp
			}
			catch(e: IllegalArgumentException) {
				Log.v("getResizedBitmap", "reconfigure failed, returning new bmp")
			}
		}

		bmp.recycle()
		return Bitmap.createBitmap(width, height, bmp.config)
	}
	else {
		return bmp
	}
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
	m.setScale(scaledWidth.toFloat() / bmp.width, scaledHeight.toFloat() / bmp.height)
	m.postTranslate(offsetX, offsetY)
	drawBitmap(bmp, m, paint)
}

internal fun blurBitmap(bmp: Bitmap, rs: RenderScript, radius: Float) {
	blurBitmap(bmp, bmp, rs, radius)
}

/**
 * blurs a bitmap with gaußian blur
 */
internal fun blurBitmap(sourceBmp: Bitmap, destBmp: Bitmap, rs: RenderScript, radius: Float) {
	val inAllocation: Allocation

	if (destBmp.width < sourceBmp.width || destBmp.height < sourceBmp.height) {
		throw IllegalArgumentException("destination bitmap must be at least as big as source bitmap")
	}
	else if (destBmp.width > sourceBmp.width || destBmp.height > sourceBmp.height) {
		//inAllocation must be exactly as big as out allocation, so we need to copy the source to dest before blurring
		Canvas(destBmp).drawBitmap(sourceBmp, radius, radius, Paint())

		inAllocation = Allocation.createFromBitmap(rs, destBmp,
				Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT)
	}
	else {
		inAllocation = Allocation.createFromBitmap(rs, sourceBmp,
				Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT)
	}

	val outAllocation = Allocation.createTyped(rs, inAllocation.type)
	val blurShader = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))

	blurShader.setRadius(radius)
	blurShader.setInput(inAllocation)
	blurShader.forEach(outAllocation)

	outAllocation.copyTo(destBmp)
}

/**
 * color filter that fills all non-transparent pixels with the
 * specified color, preserving alpha
 */
internal class FillColorFilter(color: Int)
	: ColorMatrixColorFilter(floatArrayOf(
		0f, 0f, 0f, 0f, color.red().toFloat(),
		0f, 0f, 0f, 0f, color.green().toFloat(),
		0f, 0f, 0f, 0f, color.blue().toFloat(),
		0f, 0f, 0f, color.alpha().toFloat() / 0xFF, 0f
))

internal fun Int.alpha() = this shr 24 and 0xFF
internal fun Int.red() = this shr 16 and 0xFF
internal fun Int.green() = this shr 8 and 0xFF
internal fun Int.blue() = this and 0xFF

internal fun colorBitmap(source: Bitmap, dest: Bitmap, offsetX: Float = 0f, offsetY: Float = 0f, color: Int) {
	val paint = Paint()
	paint.colorFilter = FillColorFilter(color)

	Canvas(dest).drawBitmap(source, offsetX, offsetY, paint)
}

internal fun alpha(a: Float): Int {
	if (0 <= a && a <= 1)
		return Math.round(a * 255)
	else
		throw IllegalArgumentException("alpha value must be between 0 and 1")
}

