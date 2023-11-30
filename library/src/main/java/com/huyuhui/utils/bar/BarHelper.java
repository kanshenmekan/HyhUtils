package com.huyuhui.utils.bar;

import android.os.Build;
import android.view.View;
import android.view.Window;

import androidx.core.view.WindowInsetsCompat;

public class BarHelper {
    private final BarConfig barConfig;
    private final BarUtils barUtils;

    public BarHelper(Window window) {
        barConfig = BarConfig.getDefaultBarConfig().clone();
        barUtils = new BarUtils(window);
    }

    public BarHelper setLayoutInDisplayCutoutMode(int layoutInDisplayCutoutMode) {
        barConfig.layoutInDisplayCutoutMode = layoutInDisplayCutoutMode;
        return this;
    }

    public BarHelper setNeedDisplayCutout(boolean needDisplayCutout) {
        barConfig.needDisplayCutout = needDisplayCutout;
        return this;
    }

    public BarHelper setStatusBarVisible(boolean isVisible) {
        return setStatusBarVisible(isVisible, true);
    }

    public BarHelper setStatusBarVisible(boolean isVisible, boolean isImmersive) {
        return setStatusBarVisible(isVisible, isImmersive, true);
    }

    public BarHelper setStatusBarVisible(boolean isVisible, boolean isImmersive, boolean isSticky) {
        barConfig.statusBarVisible = isVisible;
        barConfig.isStatusBarImmersive = isImmersive;
        barConfig.isStatusBarSticky = isSticky;
        return this;
    }

    public BarHelper setStatusBarLightMode(boolean statusBarLightMode) {
        barConfig.isStatusBarLightMode = statusBarLightMode;
        return this;
    }

    public BarHelper setStatusBarColor(int statusBarColor) {
        barConfig.statusBarColor = statusBarColor;
        return this;
    }


    public BarHelper setNavBarVisible(boolean isVisible) {
        return setNavBarVisible(isVisible, barConfig.isNavBarImmersive);
    }

    public BarHelper setNavBarVisible(boolean isVisible, boolean isImmersive) {
        return setNavBarVisible(isVisible, isImmersive, barConfig.isNavBarSticky);
    }

    public BarHelper setNavBarVisible(boolean isVisible, boolean isImmersive, boolean isSticky) {
        barConfig.navBarVisible = isVisible;
        barConfig.isNavBarImmersive = isImmersive;
        barConfig.isNavBarSticky = isSticky;
        return this;
    }

    public BarHelper setNavBarColor(int navBarColor) {
        barConfig.navBarColor = navBarColor;
        return this;
    }

    public BarHelper setNavBarLightMode(boolean navBarLightMode) {
        barConfig.isNavBarLightMode = navBarLightMode;
        return this;
    }
    public BarHelper immerse(boolean immerse) {
        return immerse(immerse,barConfig.immerseType);
    }
    public BarHelper immerse(boolean immerse, @WindowInsetsCompat.Type.InsetsType int immerseType) {
        barConfig.isImmerse = immerse;
        barConfig.immerseType = immerseType;
        return this;
    }

    public BarHelper titleView(View view) {
        barUtils.titleView(view);
        return this;
    }

    public void apply() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            barUtils.setLayoutInDisplayCutoutMode(barConfig.layoutInDisplayCutoutMode);
        }
        barUtils.setNeedDisplayCutout(barConfig.needDisplayCutout)
                .setStatusBarVisibility(barConfig.statusBarVisible, barConfig.isStatusBarImmersive, barConfig.isStatusBarSticky)
                .setStatusBarLightMode(barConfig.isStatusBarLightMode)
                .setStatusBarColor(barConfig.statusBarColor)
                .setNavBarVisibility(barConfig.navBarVisible,barConfig.isNavBarImmersive,barConfig.isNavBarSticky)
                .setNavBarColor(barConfig.navBarColor)
                .setNavBarLightMode(barConfig.isNavBarLightMode);
        if (barConfig.isImmerse){
            barUtils.immerse(barConfig.immerseType);
        }else {
            barUtils.exitImmerse();
        }
    }
}
