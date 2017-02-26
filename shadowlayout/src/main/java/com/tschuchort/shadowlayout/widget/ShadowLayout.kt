package com.tschuchort.shadowlayout.widget

import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import com.tschuchort.shadowlayout.misc.ifPositive

abstract class ShadowLayout(ctx: Context) : ViewGroup(ctx){

	//layout can never have more than 1 child
	protected val child: View?
		get() = getChildAt(0)

	override fun addView(child: View?, index: Int, params: LayoutParams?) {
		// don't allow more than one child view to be added
		if (childCount >= 1)
			throw UnsupportedOperationException("this layout can not have more than one child")
		else
			super.addView(child, index, params)
	}

	/**
	 * measure how big the shadow will be on each side of the view
	 * the shadow size shall be relative to the view edge
	 */
	abstract protected fun measureShadow(viewWidth: Int, viewHeight: Int): Rect

	/**
	 * notify ShadowLayout that the shadow size has changed and view has to be remeasured
	 */
	protected fun notifyShadowSizeChanged() {
		requestLayout()
	}

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
			val shadowSize = measureShadow(child!!.measuredWidth, child!!.measuredHeight)

			setPadding(
					ifPositive(shadowSize.left),
					ifPositive(shadowSize.top),
					ifPositive(shadowSize.right),
					ifPositive(shadowSize.bottom))
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