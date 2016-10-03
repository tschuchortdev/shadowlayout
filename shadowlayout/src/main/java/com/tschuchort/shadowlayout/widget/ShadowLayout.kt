package com.tschuchort.shadowlayout.widget

import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.support.v8.renderscript.RenderScript
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.tschuchort.misc.getAbsoluteBounds
import com.tschuchort.shadowlayout.R
import com.tschuchort.shadowlayout.misc.blurBitmap
import com.tschuchort.shadowlayout.misc.dpToPx
import com.tschuchort.shadowlayout.misc.isLollipopOrNewer
import com.tschuchort.shadowlayout.misc.transformToColor

/**
 * View that can draw a shadow for a child View of any shape
 */
open class ShadowLayout: ViewGroup {
	private var childBufBmp: Bitmap? = null
	private var shadowBmp: Bitmap? = null
	private var ambientShadowBmp: Bitmap? = null
	private var bufCanvas = Canvas()
	private var paint = Paint()
	private var renderscript = RenderScript.create(context)
	private var _elevation: Float = dpToPx(2f).toFloat()
	private var _translationZ: Float = 0f
	private var liftOnTouchAnimator: ValueAnimator? = null
	var liftOnTouch: Float  = 0f
	var cacheShadow: Boolean = true

	private val child: View?
		get() = getChildAt(0)

	private val combinedElevation: Float
		get() = (elevation + translationZ)

	private val shadowRadius: Float
		get() = if(combinedElevation <= 1) 3f else combinedElevation * 2f

	private val ambientShadowOffsetY: Float
		get() = combinedElevation

	private val ambientShadowAlpha: Float
		get() = (combinedElevation + 10 + (combinedElevation / 9.38f)) / 100f

	private val directShadowOffsetY: Float
		get() = if(combinedElevation < 10) combinedElevation / 2f + 1 else combinedElevation - 4

	private val directShadowAlpha: Float
		get() = (24 - Math.round(combinedElevation / 10)) / 100f

	private val maxShadowSize: Float
		get() = elevation + liftOnTouch

	var drawOutsideBounds: Boolean = false
	var useNativeElevation: Boolean = false

	//redundant ctor used by android tools
	constructor(ctx: Context, attrs: AttributeSet? = null) : this(ctx, attrs, 0)

	constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(ctx, attrs, defStyleAttr) {
		if(attrs != null) {
			val typedAttrs = context.obtainStyledAttributes(attrs, R.styleable.ShadowLayout, 0, 0)
							 ?: throw RuntimeException("failed to obtain styled attributes")

			drawOutsideBounds = typedAttrs.getBoolean(R.styleable.ShadowLayout_drawOutsideBounds, drawOutsideBounds)
			useNativeElevation = typedAttrs.getBoolean(R.styleable.ShadowLayout_useNativeElevation, useNativeElevation) //!!! this has to be set BEFORE elevation and translation
			cacheShadow = typedAttrs.getBoolean(R.styleable.ShadowLayout_cacheShadow, cacheShadow)
			elevation = typedAttrs.getDimensionPixelSize(R.styleable.ShadowLayout_elevation, _elevation.toInt()).toFloat()
			liftOnTouch = typedAttrs.getDimensionPixelSize(R.styleable.ShadowLayout_liftOnTouch, 0).toFloat()
			typedAttrs.recycle()
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	override fun setTranslationZ(translationZ: Float) {
		if(isLollipopOrNewer() && useNativeElevation)
			child?.translationZ = translationZ
		else
			_translationZ = translationZ

		invalidate()
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	override fun getTranslationZ(): Float {
		if(isLollipopOrNewer() && useNativeElevation)
			return child?.translationZ ?: 0f
		else
			return _translationZ
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	override fun setElevation(elevation: Float) {
		if(isLollipopOrNewer() && useNativeElevation)
			child?.elevation = elevation

		_elevation = elevation
		invalidate()
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	override fun getElevation(): Float {
		if(isLollipopOrNewer() && useNativeElevation)
			return child?.elevation ?: 0f
		else
			return _elevation
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	override fun addView(newChild: View?, index: Int, params: LayoutParams?) {
		if(childCount >= 1)
			throw UnsupportedOperationException("this layout can not have more than one child")

		if(isLollipopOrNewer() && useNativeElevation)
			newChild?.elevation = _elevation

		super.addView(newChild, index, params)
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	override fun dispatchTouchEvent(event: MotionEvent): Boolean {
		//if touch actually happened on our child view. Could be outside due to margin for example
		if (child?.getAbsoluteBounds()?.contains(event.rawX.toInt(), event.rawY.toInt()) == true)
			when (event.action) {
				MotionEvent.ACTION_DOWN -> {
					liftOnTouchAnimator?.cancel()
					liftOnTouchAnimator = ValueAnimator.ofFloat(0f, liftOnTouch)
					liftOnTouchAnimator!!
							.setDuration(200)
							.addUpdateListener<Float>() { translationZ = it }
							.start()
				}

				MotionEvent.ACTION_UP,
				MotionEvent.ACTION_OUTSIDE,
				MotionEvent.ACTION_CANCEL -> {
					liftOnTouchAnimator?.cancel()
					liftOnTouchAnimator = ValueAnimator.ofFloat(liftOnTouch, 0f)
					liftOnTouchAnimator!!
							.setDuration(200)
							.addUpdateListener<Float>() { translationZ = it }
							.start()
				}
			}

		if(isEnabled) {
			super.dispatchTouchEvent(event)
			return true
		}
		else
			return super.dispatchTouchEvent(event)
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		if(!drawOutsideBounds)
			setPadding(
					maxShadowSize.toInt(),
					(maxShadowSize  - ambientShadowOffsetY).toInt(),
					maxShadowSize.toInt(),
					(maxShadowSize + directShadowOffsetY).toInt())
		else
			setPadding(0,0,0,0)

		child?.measure(
				getChildMeasureSpec(
						widthMeasureSpec,
						paddingLeft + paddingRight,
						child!!.layoutParams.width),
				getChildMeasureSpec(
						heightMeasureSpec,
						paddingTop + paddingBottom,
						child!!.layoutParams.height))

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

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		super.onSizeChanged(w, h, oldw, oldh)

		childBufBmp?.recycle()
		shadowBmp?.recycle()
		ambientShadowBmp?.recycle()

		childBufBmp = Bitmap.createBitmap(child!!.measuredWidth, child!!.measuredHeight, Bitmap.Config.ARGB_8888)

		shadowBmp = Bitmap.createBitmap(
				child!!.measuredWidth + (maxShadowSize * 2).toInt(),
				child!!.measuredHeight + (maxShadowSize * 2).toInt(),
				Bitmap.Config.ARGB_8888)

		ambientShadowBmp = Bitmap.createBitmap(
				child!!.measuredWidth + (maxShadowSize * 2).toInt(),
				child!!.measuredHeight + (maxShadowSize * 2).toInt(),
				Bitmap.Config.ARGB_8888)
	}

	override fun dispatchDraw(canvas: Canvas) {
		if(shadowRadius > 0 && (!useNativeElevation || !isLollipopOrNewer()) ) {
			childBufBmp!!.eraseColor(Color.TRANSPARENT)
			shadowBmp!!.eraseColor(Color.TRANSPARENT)
			ambientShadowBmp!!.eraseColor(Color.TRANSPARENT)

			bufCanvas.setBitmap(childBufBmp)

			//let childs draw to buffer
			super.dispatchDraw(bufCanvas)

			//offset the copy because the shadowBmp is slightly bigger than childBufBmp
			transformToColor(
					source = childBufBmp!!, dest = shadowBmp!!,
					offsetX = maxShadowSize, offsetY = maxShadowSize,
					color = Color.argb((0xFF * directShadowAlpha).toInt(), 0, 0, 0))

			transformToColor(
					source = childBufBmp!!, dest = ambientShadowBmp!!,
					offsetX = maxShadowSize, offsetY = maxShadowSize,
					color = Color.argb((0xFF * ambientShadowAlpha).toInt(), 0, 0, 0))

			blurBitmap(ambientShadowBmp!!, renderscript, if(shadowRadius <= 25) shadowRadius else 25f)
			blurBitmap(shadowBmp!!, renderscript, if(shadowRadius <= 25) shadowRadius else 25f)

			canvas.save()

			if(drawOutsideBounds) {
				val newClipBounds = canvas.clipBounds
				newClipBounds.inset(-shadowRadius.toInt(), -shadowRadius.toInt())
				canvas.clipRect(newClipBounds, Region.Op.REPLACE)
			}

			canvas.drawBitmap(ambientShadowBmp, -maxShadowSize, -maxShadowSize + ambientShadowOffsetY, paint)
			canvas.drawBitmap(shadowBmp, -maxShadowSize, -maxShadowSize + directShadowOffsetY, paint)
			canvas.drawBitmap(childBufBmp, 0f, 0f, paint)

			canvas.restore()
		}
		else
			super.dispatchDraw(canvas)
	}
}

/*
 * this function is necessary because someone screwed
 * up the method cascading by letting addUpdateListener return void
 */
private fun <T> ValueAnimator.addUpdateListener(listener: (value: T) -> Unit): ValueAnimator {
	@Suppress("UNCHECKED_CAST")
	addUpdateListener { listener.invoke(it.animatedValue as T) }
	return this
}
