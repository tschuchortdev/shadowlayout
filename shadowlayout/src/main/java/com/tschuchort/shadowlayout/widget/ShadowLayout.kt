package com.tschuchort.shadowlayout.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import com.tschuchort.shadowlayout.misc.Shadow
import com.tschuchort.shadowlayout.misc.getResizedBitmap

abstract class ShadowLayout<T : Shadow>(ctx: Context, protected var shadowBuilder: Shadow.Builder<T>) : ViewGroup(ctx){
	private var childBufBmp: Bitmap? = null
	protected var shadow: T? = null
	private var childBufCanvas: Canvas? = null
	var cacheShadow = true
	private var shadowInvalid = true //signals if the shadow needs to be redrawn
	private var paint = Paint()
	protected var reuseChildBitmap = true

	//layout can never have more than 1 child
	protected val child: View?
		get() = getChildAt(0)

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		if(child != null && child!!.width > 0 && child!!.height > 0) {
			if (childBufBmp == null || !reuseChildBitmap) {
				childBufBmp = Bitmap.createBitmap(child!!.width, child!!.height, Bitmap.Config.ARGB_8888)
			}
			else if (child!!.width > childBufBmp!!.width || child!!.height > childBufBmp!!.height) {
				childBufBmp = getResizedBitmap(childBufBmp!!, child!!.width, child!!.height)
			}

			childBufCanvas = Canvas(childBufBmp)
		}
	}

	/**
	 * don't allow more than one child view to be added
	 */
	override fun addView(child: View?, index: Int, params: LayoutParams?) {
		if (childCount >= 1)
			throw UnsupportedOperationException("this layout can not have more than one child")
		else
			super.addView(child, index, params)
	}

	fun invalidateShadow() {
		shadowInvalid = true
	}

	abstract fun drawShadow()

	override fun dispatchDraw(canvas: Canvas) {
		if(child != null && child!!.width > 0 && child!!.height > 0) {
			super.dispatchDraw(childBufCanvas!!)

			if(shadowInvalid) {
				if(shadow == null) {
					shadow = shadowBuilder.build()
				}
				else {
					shadow.reconfigure(shadowBuilder.view)
				}

				shadow = shadowBuilder.build()
				shadowInvalid = false
			}

			shadow.draw(canvas)
			canvas.drawBitmap(childBufBmp!!, 0f, 0f, paint)
		}
	}

	abstract fun onCalculateShadowSize(childWidth: Int, childHeight: Int): Rect


	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		child?.measure(
				getChildMeasureSpec(
						widthMeasureSpec,
						paddingLeft + paddingRight,
						child!!.layoutParams.width),
				getChildMeasureSpec(
						heightMeasureSpec,
						paddingTop + paddingBottom,
						child!!.layoutParams.height))

		if(child != null) {
			val shadowSize = shadowBuilder.calculateShadowSize()

			setPadding(
					shadowSize.left,
					shadowSize.top,
					shadowSize.right,
					shadowSize.bottom)
		}

		setMeasuredDimension(
				resolveSize((child?.measuredWidth ?: 0) + paddingRight + paddingLeft, widthMeasureSpec),
				resolveSize((child?.measuredHeight ?: 0) + paddingTop + paddingBottom, heightMeasureSpec))

	}

	override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
		child?.layout(
				paddingLeft,
				paddingTop,
				paddingLeft + child!!.measuredWidth,
				paddingTop + child!!.measuredHeight)
	}
}