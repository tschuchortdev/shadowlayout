package com.tschuchort.shadowlayout.misc

import android.content.Context
import android.graphics.*
import android.support.v8.renderscript.RenderScript

open class CssShadowParams(
		var offsetX: Int = 0,
		var offsetY: Int = 0,
		var blur: Float = 0f,
		var spread: Int = 0,
		var color: Int = Color.BLACK)

class CssShadow private constructor(builder: Builder) : Shadow{
	private var shadowBmp: Bitmap? = null
	private val paint = Paint()

	var offsetX = builder.offsetX
	var offsetY = builder.offsetY
	val blur = builder.blur
	val spread = builder.spread
	var color = builder.color

	class Builder : Shadow.Builder<CssShadow> {
		var offsetX = 0
		var offsetY = 0
		var blur = 0f
		var spread = 0
		var color = Color.BLACK

		override fun calculateShadowSize() =
			Rect(
				blur.toInt() + spread - offsetY,
				blur.toInt() + spread - offsetX,
				blur.toInt() + spread + offsetY,
				blur.toInt() + spread + offsetX)

		override fun build(bmp: Bitmap): CssShadow {
			var shadow = CssShadow(this)
			shadow.shadowBmp = null
		}



	}

	fun reconfigure(builder: Builder) {

	}

	override val shadowSize: Rect
		get() = getShadowSizeForParams(this)

	companion object {
		private var rs: RenderScript? = null

		@Synchronized
		private fun getRenderScriptSingleton(c: Context): RenderScript {
			if (rs == null)
				rs = RenderScript.create(c)

			return rs!!
		}
	}

	override fun updateSize(newWidth: Int, newHeight: Int) {
		shadowBmp = getResizedBitmap(shadowBmp,
				newWidth + shadowSize.left + shadowSize.right,
				newHeight + shadowSize.top + shadowSize.bottom)
	}


	override fun draw(canvas: Canvas) {
		updateSize(bufBmp.width, bufBmp.height)

		shadowBmp!!.eraseColor(Color.TRANSPARENT)

		colorBitmap(
				source = bufBmp, dest = shadowBmp!!,
				offsetX = offsetX.toFloat(), offsetY = offsetY.toFloat(),
				color = color)

		if (blur > 0)
			blurBitmap(shadowBmp!!, getRenderScriptSingleton(ctx), if (blur <= 25) blur else 25f) //25 is the biggest possible blur radius

		//don't do the extra step of scaling if spread is 0 anyway
		if(spread != 0) {
			canvas.drawBitmapScaled(
					bmp = shadowBmp!!,
					offsetX = -shadowSize.left.toFloat(),
					offsetY = -shadowSize.top.toFloat(),
					scaledWidth = (shadowBmp!!.width + spread * 2),
					scaledHeight = (shadowBmp!!.height + spread * 2),
					paint = paint)
		}
		else {
			canvas.drawBitmap(
					shadowBmp!!,
					-shadowSize.left.toFloat(),
					-shadowSize.top.toFloat(),
					paint)
		}
	}
}