package com.tschuchort.shadowlayout.widget

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import com.tschuchort.misc.useAndRecycle
import com.tschuchort.shadowlayout.R
import com.tschuchort.shadowlayout.misc.CssShadow

open class CssShadowLayout
	@JvmOverloads constructor(ctx: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ArbitraryShapeShadowLayout<CssShadow>(ctx, attrs) {

	final override var shadow = CssShadow(ctx)

	var offsetX: Int
		get() = shadow.offsetX
		set(value) {
			shadow.offsetX = value
			notifyShadowSizeChanged()
		}

	var offsetY: Int
		get() = shadow.offsetY
		set(value) {
			shadow.offsetY = value
			notifyShadowSizeChanged()
		}

	var blur: Float = shadow.blur
		set(value) {
			field = value
			notifyShadowSizeChanged()
			recreateShadow()
		}

	var spread: Int
		get() = shadow.spread
		set(value) {
			shadow.spread = value
			notifyShadowSizeChanged()
		}

	var color: Int
		get() = shadow.color
		set(value) {
			shadow.color = value
			invalidate()
		}

	init {
		if(attrs != null) {
			val typedAttrs = context.obtainStyledAttributes(attrs, R.styleable.CssShadowLayout, 0, 0)
							 ?: throw RuntimeException("failed to obtain styled attributes")

			typedAttrs.useAndRecycle {
				offsetX = getDimensionPixelSize(R.styleable.CssShadowLayout_offsetX, offsetX)
				offsetY = getDimensionPixelSize(R.styleable.CssShadowLayout_offsetY, offsetY)
				spread = getDimensionPixelSize(R.styleable.CssShadowLayout_spread, spread)
				color = getColor(R.styleable.CssShadowLayout_color, color)
				blur = getDimensionPixelSize(R.styleable.CssShadowLayout_blur, blur.toInt()).toFloat()
			}
		}
	}

	override fun createShadow(viewBmp: Bitmap)
			= shadow.reconfigure(viewBmp, blur)

	override fun measureShadow(viewWidth: Int, viewHeight: Int)
			= shadow.copy(blur = blur).size

	override fun reserveSize(width: Int, height: Int) {
		super.reserveSize(width, height)
		shadow.reserveSize(width, height)
	}
}