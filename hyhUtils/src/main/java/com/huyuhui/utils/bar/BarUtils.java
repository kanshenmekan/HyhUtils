package com.huyuhui.utils.bar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.os.CancellationSignal;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Interpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsAnimationControlListenerCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import com.huyuhui.utils.rom.RomUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class BarUtils {

    public interface KeyBordHeightChangeCallBack {
        default void onPrepare() {
        }

        default void onStart(int keyBordHeight) {
        }

        void onProgress(float fraction, int height);

        default void onEnd() {
        }
    }

    private static final String TAG_TITLE_VIEW = "TAG_TITLE_VIEW";
    private static final int KEY_OFFSET = -1234;

    @SuppressLint({"InternalInsetResource", "DiscouragedApi"})
    public static int getStatusBarHeight() {
        Resources resources = Resources.getSystem();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    /**
     * Return whether the status bar is visible.
     *
     * @param window The window.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isStatusBarVisible(@NonNull final Window window) {
        int flags = window.getAttributes().flags;
        return (flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == 0 &&
                ((window.getDecorView().getWindowSystemUiVisibility() & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0);
    }

    /**
     * Return the navigation bar's height.
     *
     * @return the navigation bar's height
     */
    @SuppressLint({"InternalInsetResource", "DiscouragedApi"})
    public static int getNavBarHeight() {
        Resources res = Resources.getSystem();
        int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId != 0) {
            return res.getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }

    private static String getResNameById(Window window, int id) {
        try {
            return window.getContext().getResources().getResourceEntryName(id);
        } catch (java.lang.Exception e) {
            return "";
        }
    }

    public static boolean isNavBarVisible(Window window) {
        boolean isVisible = false;
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        int i = 0;
        int count = decorView.getChildCount();
        while (i < count) {
            View child = decorView.getChildAt(i);
            int id = child.getId();
            if (id != View.NO_ID) {
                String resourceEntryName = getResNameById(window, id);
                if ("navigationBarBackground".equals(resourceEntryName) && child.getVisibility() == View.VISIBLE) {
                    isVisible = true;
                    break;
                }
            }
            i++;
        }
        if (isVisible) {
//                对于三星手机，android10以下非OneUI2的版本，比如 s8，note8 等设备上，
//                导航栏显示存在bug："当用户隐藏导航栏时显示输入法的时候导航栏会跟随显示"，会导致隐藏输入法之后判断错误
//                这个问题在 OneUI 2 & android 10 版本已修复
            if (RomUtils.isSamsung()
                    && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
            ) {
                try {
                    return Settings.Global.getInt(
                            window.getContext().getContentResolver(),
                            "navigationbar_hide_bar_enabled"
                    ) == 0;
                } catch (Exception ignore) {
                }
            }
            int visibility = decorView.getSystemUiVisibility();
            isVisible = (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;
        }
        return isVisible;
    }

    public static BarUtils with(Activity activity) {
        return new BarUtils(activity.getWindow());
    }

    public static BarUtils with(Fragment fragment) {
        if (fragment.getActivity() == null) return null;
        return new BarUtils(fragment.getActivity().getWindow());
    }

    private final Window window;
    private boolean forceDisplayCutout = false;

    private WindowInsetsControllerCompat controller;

    public WindowInsetsCompat getWindowInsets() {
        return ViewCompat.getRootWindowInsets(window.getDecorView());
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT,
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES,
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER,
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
    })
    @interface LayoutInDisplayCutoutMode {
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public int getLayoutInDisplayCutoutMode() {
        return window.getAttributes().layoutInDisplayCutoutMode;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public BarUtils setLayoutInDisplayCutoutMode(@LayoutInDisplayCutoutMode int layoutInDisplayCutoutMode) {
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.layoutInDisplayCutoutMode = layoutInDisplayCutoutMode;
        window.setAttributes(lp);
        return this;
    }


    public BarUtils setForceDisplayCutout(boolean forceDisplayCutout) {
        if (this.forceDisplayCutout != forceDisplayCutout) {
            window.getDecorView().requestApplyInsets();
        }
        this.forceDisplayCutout = forceDisplayCutout;
        return this;
    }


    public WindowInsetsControllerCompat getController() {
        if (controller == null) {
            controller = new WindowInsetsControllerCompat(window, window.findViewById(android.R.id.content));
            controller.addOnControllableInsetsChangedListener((controller, typeMask) -> {

            });
        }
        return controller;
    }

    public BarUtils(Window window) {
        this.window = window;
        ViewCompat.setOnApplyWindowInsetsListener(window.getDecorView(), (v, insets) -> {
            if (forceDisplayCutout && insets.getDisplayCutout() != null) {
                WindowInsetsCompat removeCutoutInsets = new WindowInsetsCompat.Builder(insets)
                        .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.NONE).build();
                return ViewCompat.onApplyWindowInsets(v, removeCutoutInsets);
            } else {
                return ViewCompat.onApplyWindowInsets(v, insets);
            }
        });
    }

    /**
     * Set the status bar's visibility.
     *
     * @param isVisible   True to set status bar visible, false otherwise.
     * @param isImmersive 是否沉浸
     * @param isSticky    手势触发显示后，是否恢复隐藏
     */
    public BarUtils setStatusBarVisibility(boolean isVisible, boolean isImmersive, boolean isSticky) {
        if (isImmersive) {
            if (isSticky) {
                getController().setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            } else {
                getController().setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_DEFAULT);
            }
        }
        if (isVisible) {
            getController().show(WindowInsetsCompat.Type.statusBars());
            enableTitleView();
        } else {
            disableTitleView();
            getController().hide(WindowInsetsCompat.Type.statusBars());
        }
        return this;
    }

    public BarUtils setStatusBarVisibility(boolean isVisible, boolean isImmersive) {
        return setStatusBarVisibility(isVisible, isImmersive, true);
    }

    public BarUtils setStatusBarVisibility(boolean isVisible) {
        return setStatusBarVisibility(isVisible, true, true);
    }

    public int getStatusBarHeightCompat() {
        if (window.getDecorView().isAttachedToWindow()) {
            if (getWindowInsets() == null) {
                return BarUtils.getStatusBarHeight();
            } else {
                return getWindowInsets().getInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars()).top;
            }
        } else {
            return BarUtils.getStatusBarHeight();
        }
    }

    public boolean isStatusBarVisibleCompat() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return BarUtils.isStatusBarVisible(window);
        }
        if (window.getDecorView().isAttachedToWindow()) {
            if (getWindowInsets() == null) {
                return BarUtils.isStatusBarVisible(window);
            } else {
                return getWindowInsets().isVisible(WindowInsetsCompat.Type.statusBars());
            }
        } else {
            return BarUtils.isStatusBarVisible(window);
        }
    }

    public boolean isStatusBarLightMode() {
        return getController().isAppearanceLightStatusBars();
    }

    public BarUtils setStatusBarLightMode(boolean isLight) {
        getController().setAppearanceLightStatusBars(isLight);
        return this;
    }

    public @ColorInt int getStatusBarColor() {
        return window.getStatusBarColor();
    }

    public BarUtils setStatusBarColor(@ColorInt int statusBarColor) {
        window.setStatusBarColor(statusBarColor);
        return this;
    }


    // navigation bar

    public BarUtils setNavBarVisibility(boolean isVisible, boolean isImmersive, boolean isSticky) {
        if (isImmersive) {
            if (isSticky) {
                getController().setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            } else {
                getController().setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_DEFAULT);
            }
        }
        if (isVisible) {
            getController().show(WindowInsetsCompat.Type.navigationBars());
        } else {
            getController().hide(WindowInsetsCompat.Type.navigationBars());
        }
        return this;
    }

    public BarUtils setNavBarVisibility(boolean isVisible, boolean isImmersive) {
        return setNavBarVisibility(isVisible, isImmersive, true);
    }

    public BarUtils setNavBarVisibility(boolean isVisible) {
        return setNavBarVisibility(isVisible, true, true);
    }

    public int getNavBarHeightCompat() {
        if (window.getDecorView().isAttachedToWindow()) {
            if (getWindowInsets() == null) {
                return getStatusBarHeight();
            } else {
                Insets insets = getWindowInsets().getInsetsIgnoringVisibility(WindowInsetsCompat.Type.navigationBars());
                return Math.max(Math.max(insets.left, insets.right), insets.bottom);
            }
        } else {
            return getNavBarHeight();
        }
    }

    public boolean isNavBarVisibleCompat() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return BarUtils.isNavBarVisible(window);
        }
        if (window.getDecorView().isAttachedToWindow()) {
            if (getWindowInsets() == null) {
                return isNavBarVisible(window);
            } else {
                return getWindowInsets().isVisible(WindowInsetsCompat.Type.navigationBars());
            }
        } else {
            return isNavBarVisible(window);
        }
    }

    public @ColorInt int getNavBarColor() {
        return window.getNavigationBarColor();
    }

    public BarUtils setNavBarColor(@ColorInt int navBarColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(false);
        }
        window.setNavigationBarColor(navBarColor);
        return this;
    }

    public boolean isNavBarLightMode() {
        return getController().isAppearanceLightNavigationBars();
    }

    public BarUtils setNavBarLightMode(boolean isLight) {
        getController().setAppearanceLightNavigationBars(isLight);
        return this;
    }

    public BarUtils immerse() {
        return immerse(WindowInsetsCompat.Type.systemBars());
    }

    public BarUtils immerse(@WindowInsetsCompat.Type.InsetsType int type) {
        if (type == WindowInsetsCompat.Type.systemBars()
                || type == WindowInsetsCompat.Type.statusBars()
                || type == WindowInsetsCompat.Type.navigationBars()) {
            WindowCompat.setDecorFitsSystemWindows(window, false);
        }
        ViewCompat.setOnApplyWindowInsetsListener(window.findViewById(android.R.id.content), (v, insets) -> {
            setImmersePadding(v, type, insets);
            return ViewCompat.onApplyWindowInsets(v, insets);
            //消费掉systemBars部分的insets
//            return new WindowInsetsCompat.Builder(insets).setInsets(WindowInsetsCompat.Type.systemBars(), Insets.NONE).build();
        });
        window.getDecorView().requestApplyInsets();
        return this;
    }

    public void clearViewAppliedInsets(View view, @WindowInsetsCompat.Type.InsetsType int type) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> new WindowInsetsCompat.Builder(insets).setInsets(
                type,
                Insets.NONE
        ).build());
    }

    public BarUtils exitImmerse() {
        removeTitleView();
        WindowCompat.setDecorFitsSystemWindows(window, true);
        ViewCompat.setOnApplyWindowInsetsListener(window.findViewById(android.R.id.content), null);
        resetViewPadding();
        return this;
    }

    private void setImmersePadding(View view, @WindowInsetsCompat.Type.InsetsType int type, WindowInsetsCompat insets) {
        if (type == WindowInsetsCompat.Type.systemBars()) {
            view.setPadding(0, 0, 0, 0);
            if (isStatusBarVisibleCompat()) {
                enableTitleView();
            } else {
                disableTitleView();
            }
        } else if (type == WindowInsetsCompat.Type.statusBars()) {
            int padding = 0;
            if (isNavBarVisibleCompat()) {
                padding = insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.navigationBars()).bottom;
            }
            view.setPadding(0, 0, 0, padding);
            if (isStatusBarVisibleCompat()) {
                enableTitleView();
            } else {
                disableTitleView();
            }
        } else if (type == WindowInsetsCompat.Type.navigationBars()) {
            int padding = 0;
            if (isStatusBarVisibleCompat()) {
                padding = insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars()).top;
            }
            view.setPadding(0, padding, 0, 0);
        }
    }

    public BarUtils titleView(View view) {
        if (view.getRootView() != window.getDecorView().getRootView()) {
            return this;
        }
        View titleView = window.getDecorView().findViewWithTag(TAG_TITLE_VIEW);
        if (titleView != null && titleView != view) {
            titleView.setTag(null);
        }
        view.setTag(TAG_TITLE_VIEW);
        window.getDecorView().requestApplyInsets();
        return this;
    }

    public BarUtils removeTitleView() {
        disableTitleView();
        View view = window.getDecorView().findViewWithTag(TAG_TITLE_VIEW);
        if (view == null) return this;
        view.setTag(null);
        view.setTag(KEY_OFFSET, null);
        return this;
    }

    private void resetViewPadding() {
        disableTitleView();
        window.findViewById(android.R.id.content).setPadding(0, 0, 0, 0);
    }

    private void enableTitleView() {
        View view = window.getDecorView().findViewWithTag(TAG_TITLE_VIEW);
        if (view == null) return;
        Object offsetObj = view.getTag(KEY_OFFSET);
        if (offsetObj == null || (int) offsetObj == 0) {
            int offset = getStatusBarHeightCompat();
            view.setTag(KEY_OFFSET, offset);
            updateBarView(view, offset);
        } else {
            int offset = (int) offsetObj;
            if (offset != getStatusBarHeightCompat()) {
                offset = getStatusBarHeightCompat() - offset;
                view.setTag(KEY_OFFSET, getStatusBarHeightCompat());
                updateBarView(view, offset);
            }
        }
    }

    private void disableTitleView() {
        View view = window.getDecorView().findViewWithTag(TAG_TITLE_VIEW);
        if (view == null) return;
        Object offset = view.getTag(KEY_OFFSET);
        if (offset == null || (int) offset == 0) return;
        updateBarView(view, -(int) offset);
        view.setTag(KEY_OFFSET, 0);
    }


    private void updateBarView(View titleView, int offset) {
        ViewGroup.LayoutParams layoutParams = titleView.getLayoutParams();
        if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT || layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT) {
            titleView.post(() -> {
                titleView.setPaddingRelative(titleView.getPaddingStart(), titleView.getPaddingTop() + offset, titleView.getPaddingEnd(), titleView.getPaddingBottom());
                //存在同一时间多次post，比如设置titleView又立马removeTileView，这样getHeight就会是之前的值，但是layoutParams.height会更新
                if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT || layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                    layoutParams.height = titleView.getHeight() + offset;
                } else {
                    layoutParams.height += offset;
                }
                titleView.setLayoutParams(layoutParams);
            });
        } else {
            titleView.setPaddingRelative(titleView.getPaddingStart(), titleView.getPaddingTop() + offset, titleView.getPaddingEnd(), titleView.getPaddingBottom());
            layoutParams.height += offset;
            titleView.setLayoutParams(layoutParams);
        }
    }


    //这个方法可以动画控制系统这些types，listener的onReady方法里面通过动画setInsetsAndAlpha
    //官方例子 https://github.com/android/user-interface-samples/blob/master/WindowInsetsAnimation/app/src/main/java/com/google/android/samples/insetsanimation/SimpleImeAnimationController.kt#L352
    public void controlWindowInsetsAnimation(@WindowInsetsCompat.Type.InsetsType int types, long durationMillis,
                                             @Nullable Interpolator interpolator,
                                             @Nullable CancellationSignal cancellationSignal,
                                             @NonNull WindowInsetsAnimationControlListenerCompat listener) {
        getController().controlWindowInsetsAnimation(types, durationMillis, interpolator, cancellationSignal, listener);
    }

    public void setWindowInsetsAnimationCallback(View view, WindowInsetsAnimationCompat.Callback callback) {
        ViewCompat.setWindowInsetsAnimationCallback(view, callback);
    }


    public void hideSoftKeyboard() {
        getController().hide(WindowInsetsCompat.Type.ime());
    }

    public void showSoftKeyboard() {
        getController().show(WindowInsetsCompat.Type.ime());
    }

    public boolean isSoftKeyboardShow() {
        if (window.getDecorView().isAttachedToWindow()) {
            return getWindowInsets().isVisible(WindowInsetsCompat.Type.ime());
        } else return false;
    }

    public int getSoftKeyboardHeight() {
        if (window.getDecorView().isAttachedToWindow()) {
            return getWindowInsets().getInsetsIgnoringVisibility(WindowInsetsCompat.Type.ime()).bottom;
        } else return 0;
    }

    /**
     * 只监听键盘的高度变化，如果需要监听其他的，需要自己写setWindowInsetsAnimationCallback
     * 考虑底部高度的话，还需要考虑导航栏的高度
     *
     * @param callBack 键盘高度监听回调
     */
    public void addKeyBordHeightChangeCallBack(KeyBordHeightChangeCallBack callBack) {
        setWindowInsetsAnimationCallback(window.getDecorView().getRootView(), new WindowInsetsAnimationCompat.Callback(WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP) {
            @Override
            public void onPrepare(@NonNull WindowInsetsAnimationCompat animation) {
                super.onPrepare(animation);
                if ((animation.getTypeMask() & WindowInsetsCompat.Type.ime()) != 0) {
                    callBack.onPrepare();
                }
            }

            //当内嵌动画开始时，系统会调用 onStart。您可以使用它将所有视图属性设置为布局更改的最终状态
            @NonNull
            @Override
            public WindowInsetsAnimationCompat.BoundsCompat onStart(@NonNull WindowInsetsAnimationCompat animation, @NonNull WindowInsetsAnimationCompat.BoundsCompat bounds) {
                //bounds 代表里面有lower 和upper 分别表示引起insets的窗口的大小正在改变，该动画的下限和上限
                //如果是显示或者隐藏，那么下限就是Insets.None,那么上限就是全部显示的时候的Insets
                if ((animation.getTypeMask() & WindowInsetsCompat.Type.ime()) != 0) {
                    callBack.onStart(bounds.getUpperBound().bottom);
                }
                return super.onStart(animation, bounds);
            }

            @NonNull
            @Override
            public WindowInsetsCompat onProgress(@NonNull WindowInsetsCompat insets, @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
                //找到键盘的动画
                WindowInsetsAnimationCompat imeAnimation = null;
                for (WindowInsetsAnimationCompat animation : runningAnimations) {
                    if ((animation.getTypeMask() & WindowInsetsCompat.Type.ime()) != 0) {
                        imeAnimation = animation;
                        break;
                    }
                }
                if (imeAnimation != null) {
                    float fraction = imeAnimation.getInterpolatedFraction() < 0 ? 0 : (imeAnimation.getInterpolatedFraction() > 1 ? 1 : imeAnimation.getInterpolatedFraction());
                    callBack.onProgress(fraction, insets.getInsets(WindowInsetsCompat.Type.ime()).bottom);
                }
                return insets;
            }

            @Override
            public void onEnd(@NonNull WindowInsetsAnimationCompat animation) {
                super.onEnd(animation);
                if ((animation.getTypeMask() & WindowInsetsCompat.Type.ime()) != 0) {
                    callBack.onEnd();
                }
            }
        });
    }
}
