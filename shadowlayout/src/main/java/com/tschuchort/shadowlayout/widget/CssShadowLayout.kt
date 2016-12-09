package com.tschuchort.shadowlayout.widget

import android.content.Context
import android.util.AttributeSet
import com.tschuchort.shadowlayout.R
import com.tschuchort.shadowlayout.misc.CssShadow

open class CssShadowLayout : ShadowLayout<CssShadow> {
	//redundant ctor used by android tools
	constructor(ctx: Context, attrs: AttributeSet? = null) : this(ctx, attrs, 0)

	constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(ctx, CssShadow.Builder()) {
		if(attrs != null) {
			val typedAttrs = context.obtainStyledAttributes(attrs, R.styleable.CssShadowLayout, 0, 0)
							 ?: throw RuntimeException("failed to obtain styled attributes")

			with(shadowBuilder) {
				offsetX = typedAttrs.getDimensionPixelSize(R.styleable.CssShadowLayout_offsetX, offsetX)
				offsetY = typedAttrs.getDimensionPixelSize(R.styleable.CssShadowLayout_offsetY, offsetY)
				blur = typedAttrs.getDimensionPixelSize(R.styleable.CssShadowLayout_blur, blur.toInt()).toFloat()
				spread = typedAttrs.getDimensionPixelSize(R.styleable.CssShadowLayout_spread, spread)
				color = typedAttrs.getColor(R.styleable.CssShadowLayout_color, color)
			}

			cacheShadow = typedAttrs.getBoolean(R.styleable.CssShadowLayout_childChangesShape, cacheShadow)
			typedAttrs.recycle()
		}
	}
}