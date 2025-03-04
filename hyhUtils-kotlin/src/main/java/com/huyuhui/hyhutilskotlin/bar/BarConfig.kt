package com.huyuhui.hyhutilskotlin.bar

import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.core.view.WindowInsetsCompat
import java.lang.ref.WeakReference

@Suppress("unused")
class BarConfig private constructor(
    val layoutInDisplayCutoutMode: Int,
    val forceDisplayCutout: Boolean,
    val statusBarVisible: Boolean,
    val isStatusBarImmersive: Boolean,
    val isStatusBarSticky: Boolean,
    val isStatusBarLightMode: Boolean,
    val statusBarColor: Int,
    val navBarVisible: Boolean,
    val isNavBarImmersive: Boolean,
    val isNavBarSticky: Boolean,
    val navBarColor: Int,
    val isNavBarLightMode: Boolean,
    val isImmerse: Boolean,
    @WindowInsetsCompat.Type.InsetsType val immerseType: Int,
    val titleView: WeakReference<View>?,
) {
    class Builder {
        @BarUtils.LayoutInDisplayCutoutMode
        private var layoutInDisplayCutoutMode: Int = 0

        private var forceDisplayCutout: Boolean = false
        private var statusBarVisible: Boolean = true
        private var isStatusBarImmersive: Boolean = true
        private var isStatusBarSticky: Boolean = true
        private var isStatusBarLightMode: Boolean = false
        private var statusBarColor: Int = Color.BLACK
        private var navBarVisible: Boolean = true
        private var isNavBarImmersive: Boolean = true
        private var isNavBarSticky: Boolean = true
        private var navBarColor: Int = Color.WHITE
        private var isNavBarLightMode: Boolean = true
        private var isImmerse: Boolean = false
        private var immerseType: Int = WindowInsetsCompat.Type.systemBars()
        private var titleView: WeakReference<View>? = null

        constructor()

        constructor(config: BarConfig) {
            this.layoutInDisplayCutoutMode = config.layoutInDisplayCutoutMode
            this.forceDisplayCutout = config.forceDisplayCutout
            this.statusBarVisible = config.statusBarVisible
            this.isStatusBarImmersive = config.isStatusBarImmersive
            this.isStatusBarSticky = config.isStatusBarSticky
            this.isStatusBarLightMode = config.isStatusBarLightMode
            this.statusBarColor = config.statusBarColor
            this.navBarVisible = config.navBarVisible
            this.isNavBarImmersive = config.isNavBarImmersive
            this.isNavBarSticky = config.isNavBarSticky
            this.navBarColor = config.navBarColor
            this.isNavBarLightMode = config.isNavBarLightMode
            this.isImmerse = config.isImmerse
            this.immerseType = config.immerseType
            this.titleView = config.titleView
        }

        fun layoutInDisplayCutoutMode(mode: Int, forceDisplayCutout: Boolean = false) = apply {
            this.forceDisplayCutout = forceDisplayCutout
            this.layoutInDisplayCutoutMode = mode
        }

        fun statusBarVisible(visible: Boolean, immersive: Boolean = true, sticky: Boolean = true) =
            apply {
                this.statusBarVisible = visible
                this.isStatusBarImmersive = immersive
                this.isStatusBarSticky = sticky
            }

        fun isStatusBarLightMode(lightMode: Boolean) = apply {
            this.isStatusBarLightMode = lightMode
        }

        fun statusBarColor(color: Int) = apply {
            this.statusBarColor = color
        }

        fun navBarVisible(visible: Boolean, immersive: Boolean = true, sticky: Boolean = true) =
            apply {
                this.navBarVisible = visible
                this.isNavBarImmersive = immersive
                this.isNavBarSticky = sticky
            }


        fun navBarColor(color: Int) = apply {
            this.navBarColor = color
        }

        fun isNavBarLightMode(lightMode: Boolean) = apply {
            this.isNavBarLightMode = lightMode
        }

        fun immerse(@WindowInsetsCompat.Type.InsetsType type: Int) = apply {
            isImmerse = true
            immerseType = type
        }

        fun titleView(view: View) = apply {
            titleView = WeakReference(view)
        }

        fun build() = BarConfig(
            layoutInDisplayCutoutMode,
            forceDisplayCutout,
            statusBarVisible,
            isStatusBarImmersive,
            isStatusBarSticky,
            isStatusBarLightMode,
            statusBarColor,
            navBarVisible,
            isNavBarImmersive,
            isNavBarSticky,
            navBarColor,
            isNavBarLightMode,
            isImmerse,
            immerseType,
            titleView
        )
    }
}

fun BarUtils.apply(barConfig: BarConfig) = apply {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        layoutInDisplayCutoutMode = barConfig.layoutInDisplayCutoutMode
    }
    forceDisplayCutout = barConfig.forceDisplayCutout

    setStatusBarVisibility(
        barConfig.statusBarVisible,
        barConfig.isStatusBarImmersive,
        barConfig.isStatusBarSticky
    )
    isStatusBarLightMode = barConfig.isStatusBarLightMode
    statusBarColor = barConfig.statusBarColor

    setNavBarVisibility(
        barConfig.navBarVisible,
        barConfig.isNavBarImmersive,
        barConfig.isNavBarSticky
    )
    navBarColor = barConfig.navBarColor
    isNavBarLightMode = barConfig.isNavBarLightMode
    barConfig.titleView?.get()?.let { titleView(it) }
    if (barConfig.isImmerse) {
        immerse(barConfig.immerseType)
    } else {
        exitImmerse()
    }
}

