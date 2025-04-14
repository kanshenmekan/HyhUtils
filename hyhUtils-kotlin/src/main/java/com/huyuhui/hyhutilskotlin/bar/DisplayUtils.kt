@file:Suppress("unused")

package com.huyuhui.hyhutilskotlin.bar

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import androidx.window.layout.WindowMetricsCalculator
import kotlinx.coroutines.flow.Flow
import kotlin.math.roundToInt


/**
 * 获取系统屏幕宽度（像素）
 */

fun Context.getSystemScreenWidth(): Int {
    return getSystemScreenBounds().width()
}

/**
 * 获取系统屏幕高度（像素）
 */
fun Context.getSystemScreenHeight(): Int {
    return getSystemScreenBounds().height()
}

/**
 *   val windowMetrics = WindowMetricsCalculator.getOrCreate().computeMaximumWindowMetrics(this)
 *   也可以用这个实现，但是要传入的context必须是activity,所以这粒改用currentWindowMetrics和getRealMetrics的方式
 */
private fun Context.getSystemScreenBounds(): Rect {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowManager = getSystemService(WindowManager::class.java)
        windowManager.currentWindowMetrics.bounds
    } else {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        getSystemService(WindowManager::class.java)?.defaultDisplay?.getRealMetrics(metrics)
        Rect(0, 0, metrics.widthPixels, metrics.heightPixels)
    }
}

/**
 * 获取当前窗口宽度（像素）
 */
fun Activity.getAppWindowWidth(): Int {
    return getCurrentWindowBounds().width()
}

/**
 * 获取当前窗口高度（像素）
 */
fun Activity.getAppWindowHeight(): Int {
    return getCurrentWindowBounds().height()
}

private fun Activity.getCurrentWindowBounds(): Rect {
    val windowMetrics = WindowMetricsCalculator.getOrCreate()
        .computeMaximumWindowMetrics(this)
    return windowMetrics.bounds
}


/**
 * 窗口布局变化监听（如折叠屏展开/折叠、分屏模式）
 */
fun Activity.getWindowLayoutInfo(): Flow<WindowLayoutInfo> {
    return WindowInfoTracker.getOrCreate(this)
        .windowLayoutInfo(this)
}

/**
 * 解析 WindowLayoutInfo 中的折叠屏状态,对WindowLayoutInfo解析举个例子
 */
fun parseFoldState(layoutInfo: WindowLayoutInfo): String {
    val foldingFeature = layoutInfo.displayFeatures
        .filterIsInstance<FoldingFeature>()
        .firstOrNull()

    return when {
        foldingFeature == null -> "非折叠设备"
        foldingFeature.isSeparating -> "折叠状态: ${foldingFeature.orientation}"
        else -> "展开状态"
    }
}


fun Context.getScreenDensity(): Float {
    return resources.displayMetrics.density
}

fun Context.getScreenDensityDpi(): Int {
    return resources.displayMetrics.densityDpi
}

/**
 * 获取应用窗口宽度（单位：dp）
 */
fun Activity.getAppWindowWidthDp(): Float {
    return WindowMetricsCalculator.getOrCreate()
        .computeMaximumWindowMetrics(this).widthDp
}

/**
 * 获取应用窗口高度（单位：dp）
 */
fun Activity.getAppWindowHeightDp(): Float {
    return WindowMetricsCalculator.getOrCreate()
        .computeMaximumWindowMetrics(this).heightDp
}

fun Number.dpToPx(context: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        context.resources.displayMetrics
    ).roundToInt()
}

fun Number.pxToDp(context: Context): Float {
    return this.toFloat() / context.getScreenDensity()
}

fun Number.spToPx(context: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        context.resources.displayMetrics
    ).roundToInt()
}


fun Number.pxToSp(context: Context): Float {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        TypedValue.deriveDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this.toFloat(),
            context.resources.displayMetrics
        )
    } else {
        @Suppress("DEPRECATION") val scaledDensity = context.resources.displayMetrics.scaledDensity
        this.toFloat() / scaledDensity
    }
}
