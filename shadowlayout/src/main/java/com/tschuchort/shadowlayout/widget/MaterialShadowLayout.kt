package com.tschuchort.shadowlayout.widget

import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.tschuchort.misc.getAbsoluteBounds
import com.tschuchort.shadowlayout.R
import com.tschuchort.shadowlayout.misc.MaterialShadow
import com.tschuchort.shadowlayout.misc.MaterialShadowHyndman
import com.tschuchort.shadowlayout.misc.dpToPx
import com.tschuchort.shadowlayout.misc.isLollipopOrNewer

/**
 * View that can draw a shadow for a child View of any shape
 */
open class MaterialShadowLayout : ShadowLayout<MaterialShadow> {
	private var _elevation: Float = dpToPx(2f).toFloat()
	private var _translationZ: Float = 0f
	private var translationZAnimator: ValueAnimator? = null
	var liftOnTouch: Float  = 0f

	private val combinedElevation: Float
		get() = (elevation + translationZ)

	var useNativeElevation = false

	//redundant ctor used by android tools
	constructor(ctx: Context, attrs: AttributeSet? = null) : this(ctx, attrs, 0)

	constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(ctx, MaterialShadowHyndman(ctx)) {
		if(attrs != null) {
			val typedAttrs = context.obtainStyledAttributes(attrs, R.styleable.MaterialShadowLayout, 0, 0)
							 ?: throw RuntimeException("failed to obtain styled attributes")

			useNativeElevation = typedAttrs.getBoolean(R.styleable.MaterialShadowLayout_useNativeElevation, useNativeElevation) //!!! this has to be set BEFORE elevation and translation
			cacheShadow = typedAttrs.getBoolean(R.styleable.MaterialShadowLayout_cacheShadow, cacheShadow)
			elevation = typedAttrs.getDimensionPixelSize(R.styleable.MaterialShadowLayout_elevation, _elevation.toInt()).toFloat()
			liftOnTouch = typedAttrs.getDimensionPixelSize(R.styleable.MaterialShadowLayout_liftOnTouch, 0).toFloat()
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
		else
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
					translationZAnimator?.cancel()
					translationZAnimator = ValueAnimator.ofFloat(0f, liftOnTouch)
					translationZAnimator!!
							.setDuration(200)
							.addUpdateListener<Float>() { translationZ = it }
							.start()
				}

				MotionEvent.ACTION_UP,
				MotionEvent.ACTION_OUTSIDE,
				MotionEvent.ACTION_CANCEL -> {
					translationZAnimator?.cancel()
					translationZAnimator = ValueAnimator.ofFloat(liftOnTouch, 0f)
					translationZAnimator!!
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

	override fun dispatchDraw(canvas: Canvas) {
		if(!useNativeElevation || !isLollipopOrNewer()) {
			shadow.elevation = combinedElevation
			super.dispatchDraw(canvas)
		}
		else
			child?.draw(canvas)
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
