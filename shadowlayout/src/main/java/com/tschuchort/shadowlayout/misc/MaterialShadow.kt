package com.tschuchort.shadowlayout.misc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect

/**
 * Created by Thilo on 10/11/2016.
 */

interface MaterialShadow : Shadow {
	var elevation: Float
}

/**
 * modeled after:
 * https://stackoverflow.com/questions/30533055/calculating-shadow-values-for-all-material-design-elevations
 */
/*open class MaterialShadowTronsoco(elevation: Float = 0f) : MaterialShadow {
	override var elevation = elevation
	protected var directShadow = CssShadow()
	protected var ambientShadow = CssShadow()

	override fun updateSize(newWidth: Int, newHeight: Int) {
		directShadow.updateSize(newWidth, newHeight)
	}
} */

/**
 * modeled after proposed AngularJS implementation by Scott Hyndman:
 * https://codepen.io/shyndman/pen/ojxmdY?editors=001
 */
open class MaterialShadowHyndman(ctx: Context, elevation: Float = 0f) : MaterialShadow {
	override var elevation = elevation
	protected var umbraShadow = CssShadow(ctx)
	protected var penumbraShadow = CssShadow(ctx)
	protected var ambientShadow = CssShadow(ctx)

	private val umbraColor = Color.argb(alpha(0.2f), 0, 0, 0)
	private val penumbraColor = Color.argb(alpha(0.14f), 0, 0, 0)
	private val ambientColor = Color.argb(alpha(0.12f), 0, 0, 0)

	// hand made shadows for some common elevations
	// we will find the shadow parameters for other elevations by inter-/extrapolating
	// the key of the map is the elevation
	private val shadowPresets = mapOf(
			0 to ShadowParamTriple(
					CssShadowParams(0, 0, 0f, 0, umbraColor),
					CssShadowParams(0, 0, 0f, 0, penumbraColor),
					CssShadowParams(0, 0, 0f, 0, ambientColor)),
			2 to ShadowParamTriple(
					CssShadowParams(0, 3, 1f, -2, umbraColor),
					CssShadowParams(0, 2, 2f, 0, penumbraColor),
					CssShadowParams(0, 1, 5f, 0, ambientColor)),
			3 to ShadowParamTriple(
					CssShadowParams(0, 3, 3f, -2, umbraColor),
					CssShadowParams(0, 3, 4f, 0, penumbraColor),
					CssShadowParams(0, 1, 8f, 0, ambientColor)),
			4 to ShadowParamTriple(
					CssShadowParams(0, 2, 4f, -1, umbraColor),
					CssShadowParams(0, 4, 5f, 0, penumbraColor),
					CssShadowParams(0, 1, 10f, 0, ambientColor)),
			6 to ShadowParamTriple(
					CssShadowParams(0, 3, 5f, -1, umbraColor),
					CssShadowParams(0, 6, 10f, 0, penumbraColor),
					CssShadowParams(0, 1, 18f, 0, ambientColor)),
			8 to ShadowParamTriple(
					CssShadowParams(0, 5, 5f, -3, umbraColor),
					CssShadowParams(0, 8, 10f, 1, penumbraColor),
					CssShadowParams(0, 3, 14f, 2, ambientColor)),
			16 to ShadowParamTriple(
					CssShadowParams(0, 8, 10f, -5, umbraColor),
					CssShadowParams(0, 16, 24f, 2, penumbraColor),
					CssShadowParams(0, 6, 30f, 5, ambientColor)))

	override val shadowSize: Rect
		get() =
			Rect(
				max(umbraShadow.shadowSize.left, penumbraShadow.shadowSize.left, ambientShadow.shadowSize.left),
				max(umbraShadow.shadowSize.top, penumbraShadow.shadowSize.top, ambientShadow.shadowSize.top),
				max(umbraShadow.shadowSize.right, penumbraShadow.shadowSize.right, ambientShadow.shadowSize.right),
				max(umbraShadow.shadowSize.bottom, penumbraShadow.shadowSize.bottom, ambientShadow.shadowSize.bottom))

	data class ShadowParamTriple(
			var umbra: CssShadowParams,
			var penumbra: CssShadowParams,
			var ambient: CssShadowParams)

	override fun draw(canvas: Canvas, bufBmp: Bitmap) {
		val (umbraParams, penumbraParams, ambientParams) = getShadowParamsForElevation(elevation)
		umbraShadow.params = umbraParams
		penumbraShadow.params = penumbraParams
		ambientShadow.params = ambientParams

		ambientShadow.draw(canvas, bufBmp)
		umbraShadow.draw(canvas, bufBmp)
		penumbraShadow.draw(canvas, bufBmp)
	}

	override fun updateSize(newWidth: Int, newHeight: Int) {
		umbraShadow.updateSize(newWidth, newHeight)
		penumbraShadow.updateSize(newWidth, newHeight)
		ambientShadow.updateSize(newWidth, newHeight)
	}

	private fun findBoundingShadowSets(elevation: Float) =
		object {
			val lower = object {
				val elevation = shadowPresets.keys.filter { it <= elevation }.max()!!
				val umbra = shadowPresets[this.elevation]!!.umbra
				val penumbra = shadowPresets[this.elevation]!!.penumbra
				val ambient = shadowPresets[this.elevation]!!.ambient
			}

			val upper = object {
				val elevation = shadowPresets.keys.filter { it <= elevation }.min() ?: shadowPresets.keys.max()
				val umbra = shadowPresets[this.elevation]!!.umbra
				val penumbra = shadowPresets[this.elevation]!!.penumbra
				val ambient = shadowPresets[this.elevation]!!.ambient
			}
		}

	private fun getShadowParamsForElevation(elevation: Float): ShadowParamTriple {
		val boundingShadowSets = findBoundingShadowSets(elevation)
		val interpolationAmount = (elevation - boundingShadowSets.lower.elevation) / (boundingShadowSets.upper.elevation!! - boundingShadowSets.lower.elevation)

		return ShadowParamTriple(
				umbra = lerpShadows(boundingShadowSets.lower.umbra, boundingShadowSets.upper.umbra, interpolationAmount),
				penumbra = lerpShadows(boundingShadowSets.lower.penumbra, boundingShadowSets.upper.penumbra, interpolationAmount),
				ambient = lerpShadows(boundingShadowSets.lower.ambient, boundingShadowSets.upper.ambient, interpolationAmount))
	}

	private fun lerpShadows(s1: CssShadowParams, s2: CssShadowParams, t: Float = 0.5f): CssShadowParams {
		return CssShadowParams(
				offsetX = lerp(s1.offsetX, s2.offsetX, t).round(),
				offsetY = lerp(s1.offsetY, s2.offsetY, t).round(),
				blur = lerp(s1.blur, s2.blur, t),
				spread = lerp(s1.spread, s2.spread, t).round())
	}

	/**
	 * Performs linear interpolation between values a and b. Returns the value
	 * between a and b proportional to x (when x is between 0 and 1. When x is
	 * outside this range, the return value is a linear extrapolation).
	 *
	 * @param t: interpolation amount
	 */
	private fun <T : Number> lerp(a: T, b: T, t: Float) = (a.toDouble() + t * (b.toDouble() - a.toDouble())).toFloat()
}