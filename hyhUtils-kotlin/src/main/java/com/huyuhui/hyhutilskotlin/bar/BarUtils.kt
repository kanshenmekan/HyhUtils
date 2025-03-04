package com.huyuhui.hyhutilskotlin.bar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Resources
import android.os.Build
import android.os.CancellationSignal
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.view.animation.Interpolator
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsAnimationControlListenerCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.huyuhui.hyhutilskotlin.rom.RomUtils.isSamsung
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.max

@Suppress("unused", "MemberVisibilityCanBePrivate")
class BarUtils(private val window: Window) {
    interface KeyBordHeightChangeCallBack {
        fun onPrepare() {
        }

        fun onStart(keyBordHeight: Int) {}
        fun onProgress(fraction: Float, height: Int)

        fun onEnd() {
        }
    }

    companion object {
        private const val TAG_TITLE_VIEW = "TAG_TITLE_VIEW"
        private const val KEY_OFFSET = -12345

        @get:SuppressLint("InternalInsetResource", "DiscouragedApi")
        val statusBarHeight: Int
            get() {
                val resources =
                    Resources.getSystem()
                val resourceId =
                    resources.getIdentifier("status_bar_height", "dimen", "android")
                return resources.getDimensionPixelSize(resourceId)
            }

        /**
         * Return whether the status bar is visible.
         *
         * @param window The window.
         * @return `true`: yes<br></br>`false`: no
         */
        @Suppress("DEPRECATION")
        fun isStatusBarVisible(window: Window): Boolean {
            val flags = window.attributes.flags
            return (flags and WindowManager.LayoutParams.FLAG_FULLSCREEN) == 0 &&
                    ((window.decorView.windowSystemUiVisibility and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
        }

        @get:SuppressLint("InternalInsetResource", "DiscouragedApi")
        val navBarHeight: Int
            /**
             * @return the navigation bar's height
             */
            get() {
                val res = Resources.getSystem()
                val resourceId =
                    res.getIdentifier("navigation_bar_height", "dimen", "android")
                return if (resourceId != 0) {
                    res.getDimensionPixelSize(resourceId)
                } else {
                    0
                }
            }

        private fun getResNameById(window: Window, id: Int): String {
            return try {
                window.context.resources.getResourceEntryName(id)
            } catch (e: Exception) {
                ""
            }
        }

        @Suppress("DEPRECATION")
        fun isNavBarVisible(window: Window): Boolean {
            var isVisible = false
            val decorView = window.decorView as ViewGroup
            var i = 0
            val count = decorView.childCount
            while (i < count) {
                val child = decorView.getChildAt(i)
                val id = child.id
                if (id != View.NO_ID) {
                    val resourceEntryName = getResNameById(window, id)
                    if ("navigationBarBackground" == resourceEntryName && child.visibility == View.VISIBLE) {
                        isVisible = true
                        break
                    }
                }
                i++
            }
            if (isVisible) {
//                对于三星手机，android10以下非OneUI2的版本，比如 s8，note8 等设备上，
//                导航栏显示存在bug："当用户隐藏导航栏时显示输入法的时候导航栏会跟随显示"，会导致隐藏输入法之后判断错误
//                这个问题在 OneUI 2 & android 10 版本已修复
                if (isSamsung
                    && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                ) {
                    try {
                        return Settings.Global.getInt(
                            window.context.contentResolver,
                            "navigationbar_hide_bar_enabled"
                        ) == 0
                    } catch (ignore: Exception) {
                    }
                }
                val visibility = decorView.systemUiVisibility
                isVisible = (visibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0
            }
            return isVisible
        }

        fun with(activity: Activity): BarUtils {
            return BarUtils(activity.window)
        }

        fun with(fragment: Fragment): BarUtils {
            return BarUtils(fragment.requireActivity().window)
        }
    }

    init {
        ViewCompat.setOnApplyWindowInsetsListener(
            window.decorView
        ) { v: View?, insets: WindowInsetsCompat ->
            if (forceDisplayCutout && insets.displayCutout != null) {
                val removeCutoutInsets = WindowInsetsCompat.Builder(insets)
                    .setInsets(
                        WindowInsetsCompat.Type.displayCutout(),
                        Insets.NONE
                    ).build()
                return@setOnApplyWindowInsetsListener ViewCompat.onApplyWindowInsets(
                    v!!, removeCutoutInsets
                )
            } else {
                return@setOnApplyWindowInsetsListener ViewCompat.onApplyWindowInsets(
                    v!!, insets
                )
            }
        }
    }

    private val controller: WindowInsetsControllerCompat by lazy {
        WindowInsetsControllerCompat(window, window.findViewById(android.R.id.content)).apply {
            addOnControllableInsetsChangedListener { _: WindowInsetsControllerCompat?, _: Int -> }
        }
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(
        value = [WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT,
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES,
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER,
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS]
    )
    internal annotation class LayoutInDisplayCutoutMode

    val windowInsets: WindowInsetsCompat?
        get() = ViewCompat.getRootWindowInsets(window.decorView)


    @get:RequiresApi(api = Build.VERSION_CODES.P)
    @set:RequiresApi(api = Build.VERSION_CODES.P)
    @LayoutInDisplayCutoutMode
    var layoutInDisplayCutoutMode: Int
        get() = window.attributes.layoutInDisplayCutoutMode
        set(value) {
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode = value
            window.attributes = lp
        }


    var forceDisplayCutout = false
        set(value) {
            if (field != value) {
                window.decorView.requestApplyInsets()
            }
            field = value
        }


    /**
     * Set the status bar's visibility.
     *
     * @param isVisible   True to set status bar visible, false otherwise.
     * @param isImmersive 是否沉浸
     * @param isSticky    手势触发显示后，是否恢复隐藏
     */
    fun setStatusBarVisibility(
        isVisible: Boolean,
        isImmersive: Boolean = true,
        isSticky: Boolean = true,
    ) = apply {
        if (isImmersive) {
            controller.systemBarsBehavior =
                if (isSticky) WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                else WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        }
        if (isVisible) {
            controller.show(WindowInsetsCompat.Type.statusBars())
            enableTitleView()
        } else {
            disableTitleView()
            controller.hide(WindowInsetsCompat.Type.statusBars())
        }
    }

    suspend fun waitForWindowAttach() = suspendCancellableCoroutine { continuation ->
        val view = window.decorView
        if (view.isAttachedToWindow) {
            continuation.resume(Unit)
        } else {
            val listener = object : ViewTreeObserver.OnWindowAttachListener {
                override fun onWindowAttached() {
                    view.viewTreeObserver.removeOnWindowAttachListener(this)
                    continuation.resume(Unit)
                }

                override fun onWindowDetached() {
                    // 不需要处理
                }
            }
            view.viewTreeObserver.addOnWindowAttachListener(listener)
            // 如果协程被取消，移除监听器
            continuation.invokeOnCancellation {
                view.viewTreeObserver.removeOnWindowAttachListener(listener)
            }
        }
    }

    val statusBarHeight: Int
        get() {
            return if (window.decorView.isAttachedToWindow) {
                windowInsets?.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars())?.top
                    ?: Companion.statusBarHeight
            } else Companion.statusBarHeight
        }
    val isStatusBarVisible: Boolean
        get() {
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) isStatusBarVisible(window)
            else windowInsets?.isVisible(WindowInsetsCompat.Type.statusBars())
                ?: isStatusBarVisible(window)
        }

    var isStatusBarLightMode: Boolean
        get() = controller.isAppearanceLightStatusBars
        set(value) {
            controller.isAppearanceLightStatusBars = value
        }


    @get:ColorInt
    var statusBarColor: Int
        get() = window.statusBarColor
        set(@ColorInt value) {
            window.statusBarColor = value
        }


    // navigation bar
    fun setNavBarVisibility(
        isVisible: Boolean,
        isImmersive: Boolean = true,
        isSticky: Boolean = true,
    ) = apply {
        if (isImmersive) {
            controller.systemBarsBehavior =
                if (isSticky) WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                else WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        }
        if (isVisible) {
            controller.show(WindowInsetsCompat.Type.navigationBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    val navBarHeight: Int
        get() {
            return if (window.decorView.isAttachedToWindow) {
                windowInsets?.let {
                    val insets =
                        it.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.navigationBars())
                    max(max(insets.left, insets.right), insets.bottom)
                } ?: Companion.navBarHeight
            } else Companion.navBarHeight
        }
    val isNavBarVisible: Boolean
        get() {
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                isNavBarVisible(window)
            } else {
                windowInsets?.isVisible(WindowInsetsCompat.Type.navigationBars())
                    ?: isNavBarVisible(window)
            }
        }

    @get:ColorInt
    var navBarColor: Int
        get() = window.navigationBarColor
        set(@ColorInt value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            window.navigationBarColor = value
        }

    var isNavBarLightMode: Boolean
        get() = controller.isAppearanceLightNavigationBars
        set(value) {
            controller.isAppearanceLightNavigationBars = value
        }

    fun immerse(@WindowInsetsCompat.Type.InsetsType type: Int = WindowInsetsCompat.Type.systemBars()) =
        apply {
            if (type == WindowInsetsCompat.Type.systemBars() || type == WindowInsetsCompat.Type.statusBars() || type == WindowInsetsCompat.Type.navigationBars()) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
            }
            ViewCompat.setOnApplyWindowInsetsListener(
                window.findViewById(android.R.id.content)
            ) { v: View, insets: WindowInsetsCompat ->
                ViewCompat.onApplyWindowInsets(v, insets)
                setImmersePadding(v, type, insets)
                WindowInsetsCompat.Builder(insets).setInsets(
                    WindowInsetsCompat.Type.systemBars(),
                    Insets.NONE
                ).build()
            }
            window.decorView.requestApplyInsets()
        }

    fun exitImmerse() = apply {
        removeTitleView()
        WindowCompat.setDecorFitsSystemWindows(window, true)
        ViewCompat.setOnApplyWindowInsetsListener(window.findViewById(android.R.id.content), null)
        resetViewPadding()
    }

    private fun setImmersePadding(
        view: View,
        @WindowInsetsCompat.Type.InsetsType type: Int,
        insets: WindowInsetsCompat,
    ) {
        if (type == WindowInsetsCompat.Type.systemBars()) {
            view.setPadding(0, 0, 0, 0)
            if (isStatusBarVisible) {
                enableTitleView()
            } else {
                disableTitleView()
            }
        } else if (type == WindowInsetsCompat.Type.statusBars()) {
            var padding = 0
            if (isNavBarVisible) {
                padding =
                    insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.navigationBars()).bottom
            }
            view.setPadding(0, 0, 0, padding)
            if (isStatusBarVisible) {
                enableTitleView()
            } else {
                disableTitleView()
            }
        } else if (type == WindowInsetsCompat.Type.navigationBars()) {
            var padding = 0
            if (isNavBarVisible) {
                padding =
                    insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars()).top
            }
            view.setPadding(0, padding, 0, 0)
        }
    }

    fun titleView(view: View) = apply {
        if (view.rootView !== window.decorView.rootView) {
            return this
        }
        val titleView = window.decorView.findViewWithTag<View>(TAG_TITLE_VIEW)
        if (titleView != null && titleView !== view) {
            titleView.tag = null
        }
        view.tag = TAG_TITLE_VIEW
        window.decorView.requestApplyInsets()
    }

    fun removeTitleView() = apply {
        disableTitleView()
        val view = window.decorView.findViewWithTag<View>(TAG_TITLE_VIEW) ?: return this
        view.tag = null
        view.setTag(KEY_OFFSET, null)
    }

    private fun resetViewPadding() {
        disableTitleView()
        window.findViewById<View>(android.R.id.content).setPadding(0, 0, 0, 0)
    }

    private fun enableTitleView() {
        val view = window.decorView.findViewWithTag<View>(TAG_TITLE_VIEW) ?: return
        val offsetObj = view.getTag(KEY_OFFSET)
        if (offsetObj == null || offsetObj as Int == 0) {
            val offset = statusBarHeight
            view.setTag(KEY_OFFSET, offset)
            updateBarView(view, offset)
        } else {
            var offset = offsetObj
            if (offset != statusBarHeight) {
                offset = statusBarHeight - offset
                view.setTag(KEY_OFFSET, statusBarHeight)
                updateBarView(view, offset)
            }
        }
    }

    private fun disableTitleView() {
        val view = window.decorView.findViewWithTag<View>(TAG_TITLE_VIEW) ?: return
        val offset = view.getTag(KEY_OFFSET)
        if (offset == null || offset as Int == 0) return
        updateBarView(view, -offset)
        view.setTag(KEY_OFFSET, 0)
    }


    private fun updateBarView(titleView: View, offset: Int) {
        val layoutParams = titleView.layoutParams
        if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT || layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT) {
            titleView.post {
                titleView.setPaddingRelative(
                    titleView.paddingStart,
                    titleView.paddingTop + offset,
                    titleView.paddingEnd,
                    titleView.paddingBottom
                )
                //存在同一时间多次post，比如设置titleView又立马removeTileView，这样view.getHeight就会是之前(最开始)的值，
                // 会造成多次offset改变都是在最开始的titleView.height，而不是在上一次的结果下进行改变
                // 但是layoutParams.height会更新
                if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT || layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                    layoutParams.height = titleView.height + offset
                } else {
                    layoutParams.height += offset
                }
                titleView.layoutParams = layoutParams
            }
        } else {
            titleView.setPaddingRelative(
                titleView.paddingStart,
                titleView.paddingTop + offset,
                titleView.paddingEnd,
                titleView.paddingBottom
            )
            layoutParams.height += offset
            titleView.layoutParams = layoutParams
        }
    }


    //这个方法可以动画控制系统这些types，listener的onReady方法里面通过动画setInsetsAndAlpha
    //官方例子 https://github.com/android/user-interface-samples/blob/master/WindowInsetsAnimation/app/src/main/java/com/google/android/samples/insetsanimation/SimpleImeAnimationController.kt#L352
    fun controlWindowInsetsAnimation(
        @WindowInsetsCompat.Type.InsetsType types: Int, durationMillis: Long,
        interpolator: Interpolator?,
        cancellationSignal: CancellationSignal?,
        listener: WindowInsetsAnimationControlListenerCompat,
    ) {
        controller.controlWindowInsetsAnimation(
            types,
            durationMillis,
            interpolator,
            cancellationSignal,
            listener
        )
    }

    fun setWindowInsetsAnimationCallback(
        view: View,
        callback: WindowInsetsAnimationCompat.Callback?,
    ) {
        ViewCompat.setWindowInsetsAnimationCallback(view, callback)
    }


    fun hideSoftKeyboard() {
        controller.hide(WindowInsetsCompat.Type.ime())
    }

    fun showSoftKeyboard() {
        controller.show(WindowInsetsCompat.Type.ime())
    }

    val isSoftKeyboardShow: Boolean
        get() {
            return if (window.decorView.isAttachedToWindow) {
                windowInsets!!.isVisible(WindowInsetsCompat.Type.ime())
            } else false
        }

    val softKeyboardHeight: Int
        get() {
            return windowInsets?.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.ime())?.bottom
                ?: 0
        }

    /**
     * 只监听键盘的高度变化，如果需要监听其他的，需要自己写setWindowInsetsAnimationCallback
     * 考虑底部高度的话，还需要考虑导航栏的高度
     * @param callBack
     */
    fun addKeyBordHeightChangeCallBack(callBack: KeyBordHeightChangeCallBack) {
        setWindowInsetsAnimationCallback(
            window.decorView.rootView,
            object : WindowInsetsAnimationCompat.Callback(
                DISPATCH_MODE_STOP
            ) {
                override fun onPrepare(animation: WindowInsetsAnimationCompat) {
                    super.onPrepare(animation)
                    if ((animation.typeMask and WindowInsetsCompat.Type.ime()) != 0) {
                        callBack.onPrepare()
                    }
                }

                //当内嵌动画开始时，系统会调用 onStart。您可以使用它将所有视图属性设置为布局更改的最终状态
                override fun onStart(
                    animation: WindowInsetsAnimationCompat,
                    bounds: WindowInsetsAnimationCompat.BoundsCompat,
                ): WindowInsetsAnimationCompat.BoundsCompat {
                    //bounds 代表里面有lower 和upper 分别表示引起insets的窗口的大小正在改变，该动画的下限和上限
                    //如果是显示或者隐藏，那么下限就是Insets.None,那么上限就是全部显示的时候的Insets
                    if ((animation.typeMask and WindowInsetsCompat.Type.ime()) != 0) {
                        callBack.onStart(bounds.upperBound.bottom)
                    }
                    return super.onStart(animation, bounds)
                }

                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: List<WindowInsetsAnimationCompat>,
                ): WindowInsetsCompat {
                    //找到键盘的动画
                    var imeAnimation: WindowInsetsAnimationCompat? = null
                    for (animation in runningAnimations) {
                        if ((animation.typeMask and WindowInsetsCompat.Type.ime()) != 0) {
                            imeAnimation = animation
                            break
                        }
                    }
                    if (imeAnimation != null) {
                        val fraction =
                            if (imeAnimation.interpolatedFraction < 0) 0f else (if (imeAnimation.interpolatedFraction > 1) 1f else imeAnimation.interpolatedFraction)
                        callBack.onProgress(
                            fraction,
                            insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                        )
                    }
                    return insets
                }

                override fun onEnd(animation: WindowInsetsAnimationCompat) {
                    super.onEnd(animation)
                    if ((animation.typeMask and WindowInsetsCompat.Type.ime()) != 0) {
                        callBack.onEnd()
                    }
                }
            })
    }


}
