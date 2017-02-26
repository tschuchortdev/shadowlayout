package com.tschuchort.shadowlayout.misc

import android.content.Context
import android.graphics.*
import android.support.v8.renderscript.RenderScript

open class CssShadowParams(
		open val offsetX: Int = 0,
		open val offsetY: Int = 0,
		open val blur: Float = 0f,
		open val spread: Int = 0,
		open var color: Int = Color.BLACK) {

	val size: Rect
		get() = Rect(
				blur.toInt() + spread - offsetY,
				blur.toInt() + spread - offsetX,
				blur.toInt() + spread + offsetY,
				blur.toInt() + spread + offsetX)

	override fun equals(other: Any?): Boolean {
		if(other is CssShadowParams) {
			return other.offsetX == offsetX
				&& other.offsetY == offsetY
				&& other.blur == blur
				&& other.spread == spread
				&& other.color == color
		}
		else
			return false
	}

	open fun copy(
			offsetX: Int = this.offsetX,
			offsetY: Int = this.offsetY,
			blur: Float = this.blur,
			spread: Int = this.spread,
			color: Int = this.color)
			= CssShadowParams(offsetX, offsetY, blur, spread, color)
}

class CssShadow(private val ctx: Context) : CssShadowParams(), Shadow {

	private var blurredBmp: Bitmap? = null //not really lateinit but the compiler can't deduce that
	private val paint = Paint()

	override var offsetX = 0
	override var offsetY = 0
	override var spread = 0

	override var color = Color.BLACK
		set(value) {
			field = value
			paint.colorFilter = FillColorFilter(value)
		}

	constructor(ctx: Context, viewBmp: Bitmap, blur: Float) : this(ctx) {
		reconfigure(viewBmp, blur)
	}

	/**
	 * reserves enough memory to display a shadow of the given configuration and view size
	 * without reallocation. Will do nothing if more than enough memory is already allocated
	 */
	@JvmOverloads
	fun reserveSize(viewWidth: Int, viewHeight: Int, blur: Float = this.blur) {
		val width = viewWidth + (blur * 2).round()
		val height = viewHeight + (blur * 2).round()

		if(blurredBmp != null)
			blurredBmp = getResizedBitmap(blurredBmp!!, width, height)
		else
			blurredBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
	}


	@JvmOverloads
	fun reconfigure(viewBmp: Bitmap, blur: Float = this.blur) {
		reserveSize(viewBmp.width, viewBmp.height, blur)

		blurredBmp!!.eraseColor(Color.TRANSPARENT)

		if (blur > 0) {
			blurBitmap(
					sourceBmp = viewBmp,
					destBmp = blurredBmp!!,
					rs = getRenderScriptSingleton(ctx),
					radius = if (blur <= 25) blur else 25f) //25 is the biggest possible blur radius
		}
	}

	override fun draw(canvas: Canvas) {
		if(blurredBmp != null) {
			//don't do the extra step of scaling if spread is 0 anyway
			if (spread != 0) {
				canvas.drawBitmapScaled(
						bmp = blurredBmp!!,
						offsetX = 0f,
						offsetY = 0f,
						scaledWidth = (blurredBmp!!.width + spread * 2),
						scaledHeight = (blurredBmp!!.height + spread * 2),
						paint = paint)

				/*paint.color = Color.RED
				paint.colorFilter = null

				canvas.drawRect(0f, 0f, blurredBmp!!.width.toFloat(), blurredBmp!!.height.toFloat(), paint)*/
			}
			else {
				canvas.drawBitmap(
						blurredBmp,
						-size.left.toFloat(),
						-size.top.toFloat(),
						paint)
			}
		}
	}

	companion object {
		private lateinit var appCtx: Context
		private val rs by lazy { RenderScript.create(appCtx) }

		private fun getRenderScriptSingleton(c: Context): RenderScript {
			appCtx = c.applicationContext
			return rs
		}
	}
}