package com.tschuchort.shadowlayout.misc

import android.content.res.Resources
import android.os.Build

internal fun isSdkHigherOrEqual(sdkVersion: Int): Boolean {
	return Build.VERSION.SDK_INT > sdkVersion
}

internal fun isLollipopOrNewer(): Boolean {
	return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
}

internal fun isKitKatOrNewer(): Boolean {
	return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
}

internal fun dpToPx(dp: Float): Int {
	return (dp * Resources.getSystem().displayMetrics.density).toInt()
}

internal fun pxToDp(px: Int): Float {
	return px / Resources.getSystem().displayMetrics.density
}