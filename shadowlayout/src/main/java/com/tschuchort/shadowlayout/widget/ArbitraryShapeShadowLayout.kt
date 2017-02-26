package com.tschuchort.shadowlayout.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import com.tschuchort.misc.useAndRecycle
import com.tschuchort.shadowlayout.R
import com.tschuchort.shadowlayout.misc.Shadow
import com.tschuchort.shadowlayout.misc.getResizedBitmap

abstract class ArbitraryShapeShadowLayout<T : Shadow>
	@JvmOverloads constructor(ctx: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ShadowLayout(ctx) {

	private var childBufCanvas: Canvas? = null
	private var childBufBmp: Bitmap? = null
	private var paint = Paint()
	private var shadowInvalid = true
	abstract var shadow: T

	var childChangesShape = false

	init {
		if(attrs != null) {
			val typedAttrs = context.obtainStyledAttributes(attrs, R.styleable.ArbitraryShapeShadowLayout, 0, 0)
							 ?: throw RuntimeException("failed to obtain styled attributes")

			typedAttrs.useAndRecycle {
				childChangesShape = getBoolean(R.styleable.ArbitraryShapeShadowLayout_changesShape, childChangesShape)
			}
		}
	}

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		if(child != null && child!!.measuredWidth > 0 && child!!.measuredHeight > 0) {
			if (childBufBmp == null) {
				childBufBmp = Bitmap.createBitmap(child!!.measuredWidth, child!!.measuredHeight, Bitmap.Config.ARGB_8888)
			}
			else if (child!!.measuredWidth > childBufBmp!!.width || child!!.measuredHeight > childBufBmp!!.height) {
				childBufBmp = getResizedBitmap(childBufBmp!!, child!!.measuredWidth, child!!.measuredHeight)
			}

			childBufCanvas = Canvas(childBufBmp)
			childBufCanvas!!.translate(-paddingLeft.toFloat(), -paddingTop.toFloat())
		}

		recreateShadow()
	}

	fun recreateShadow() {
		shadowInvalid = true
		invalidate()
	}

	abstract fun createShadow(viewBmp: Bitmap)

	override fun dispatchDraw(canvas: Canvas) {
		if(child != null && child!!.width > 0 && child!!.height > 0) {
			super.dispatchDraw(childBufCanvas!!)

			if(shadowInvalid || childChangesShape) {
				createShadow(childBufBmp!!)
				shadowInvalid = false
			}

			shadow.draw(canvas)
			canvas.drawBitmap(childBufBmp!!, paddingLeft.toFloat(), paddingTop.toFloat(), paint)
		}
	}

	/**
	 * reserve size to prevent reallocations when resizing the child
	 */
	open fun reserveSize(width: Int, height: Int) {
		childBufBmp = getResizedBitmap(childBufBmp!!, width, height)
	}
}