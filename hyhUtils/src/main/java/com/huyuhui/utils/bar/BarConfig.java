package com.huyuhui.utils.bar;

import android.graphics.Color;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.WindowInsetsCompat;

import java.lang.ref.WeakReference;

@SuppressWarnings("unused")
public class BarConfig implements Cloneable {
    @BarUtils.LayoutInDisplayCutoutMode
    private final int layoutInDisplayCutoutMode;

    private final boolean forceDisplayCutout;
    private final boolean statusBarVisible;
    private final boolean isStatusBarImmersive;
    private final boolean isStatusBarSticky;
    private final boolean isStatusBarLightMode;
    private final int statusBarColor;
    private final boolean navBarVisible;
    private final boolean isNavBarImmersive;
    private final boolean isNavBarSticky;
    private final int navBarColor;
    private final boolean isNavBarLightMode;
    private final boolean isImmerse;
    private final int immerseType;
    private final WeakReference<View> titleView;

    private BarConfig(Builder builder) {
        this.layoutInDisplayCutoutMode = builder.layoutInDisplayCutoutMode;
        this.forceDisplayCutout = builder.forceDisplayCutout;
        this.statusBarVisible = builder.statusBarVisible;
        this.isStatusBarImmersive = builder.isStatusBarImmersive;
        this.isStatusBarSticky = builder.isStatusBarSticky;
        this.isStatusBarLightMode = builder.isStatusBarLightMode;
        this.statusBarColor = builder.statusBarColor;
        this.navBarVisible = builder.navBarVisible;
        this.isNavBarImmersive = builder.isNavBarImmersive;
        this.isNavBarSticky = builder.isNavBarSticky;
        this.navBarColor = builder.navBarColor;
        this.isNavBarLightMode = builder.isNavBarLightMode;
        this.isImmerse = builder.isImmerse;
        this.immerseType = builder.immerseType;
        this.titleView = builder.titleView;
    }

    @BarUtils.LayoutInDisplayCutoutMode
    public int getLayoutInDisplayCutoutMode() {
        return layoutInDisplayCutoutMode;
    }

    public boolean isForceDisplayCutout() {
        return forceDisplayCutout;
    }

    public boolean isStatusBarVisible() {
        return statusBarVisible;
    }

    public boolean isStatusBarImmersive() {
        return isStatusBarImmersive;
    }

    public boolean isStatusBarSticky() {
        return isStatusBarSticky;
    }

    public boolean isStatusBarLightMode() {
        return isStatusBarLightMode;
    }

    public int getStatusBarColor() {
        return statusBarColor;
    }

    public boolean isNavBarVisible() {
        return navBarVisible;
    }

    public boolean isNavBarImmersive() {
        return isNavBarImmersive;
    }

    public boolean isNavBarSticky() {
        return isNavBarSticky;
    }

    public int getNavBarColor() {
        return navBarColor;
    }

    public boolean isNavBarLightMode() {
        return isNavBarLightMode;
    }

    public boolean isImmerse() {
        return isImmerse;
    }

    public int getImmerseType() {
        return immerseType;
    }

    public WeakReference<View> getTitleView() {
        return titleView;
    }

    @NonNull
    @Override
    public BarConfig clone() {
        try {
            return (BarConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public static class Builder {
        @BarUtils.LayoutInDisplayCutoutMode
        private int layoutInDisplayCutoutMode = 0;
        private boolean forceDisplayCutout = false;
        private boolean statusBarVisible = true;
        private boolean isStatusBarImmersive = true;
        private boolean isStatusBarSticky = true;
        private boolean isStatusBarLightMode = false;
        private int statusBarColor = Color.BLACK;
        private boolean navBarVisible = true;
        private boolean isNavBarImmersive = true;
        private boolean isNavBarSticky = true;
        private int navBarColor = Color.WHITE;
        private boolean isNavBarLightMode = true;
        private boolean isImmerse = false;
        private int immerseType = WindowInsetsCompat.Type.systemBars();
        private WeakReference<View> titleView = null;


        public Builder() {
        }

        public Builder(BarConfig config) {
            this.layoutInDisplayCutoutMode = config.getLayoutInDisplayCutoutMode();
            this.forceDisplayCutout = config.isForceDisplayCutout();
            this.statusBarVisible = config.isStatusBarVisible();
            this.isStatusBarImmersive = config.isStatusBarImmersive();
            this.isStatusBarSticky = config.isStatusBarSticky();
            this.isStatusBarLightMode = config.isStatusBarLightMode();
            this.statusBarColor = config.getStatusBarColor();
            this.navBarVisible = config.isNavBarVisible();
            this.isNavBarImmersive = config.isNavBarImmersive();
            this.isNavBarSticky = config.isNavBarSticky();
            this.navBarColor = config.getNavBarColor();
            this.isNavBarLightMode = config.isNavBarLightMode();
            this.isImmerse = config.isImmerse();
            this.immerseType = config.getImmerseType();
            this.titleView = config.getTitleView();
        }

        public Builder layoutInDisplayCutoutMode(@BarUtils.LayoutInDisplayCutoutMode int mode, boolean forceDisplayCutout) {
            this.forceDisplayCutout = forceDisplayCutout;
            this.layoutInDisplayCutoutMode = mode;
            return this;
        }

        public Builder statusBarVisible(boolean visible) {
            return statusBarVisible(visible, true, true);
        }

        public Builder statusBarVisible(boolean visible, boolean immersive, boolean sticky) {
            this.statusBarVisible = visible;
            this.isStatusBarImmersive = immersive;
            this.isStatusBarSticky = sticky;
            return this;
        }

        public Builder isStatusBarLightMode(boolean lightMode) {
            this.isStatusBarLightMode = lightMode;
            return this;
        }

        public Builder statusBarColor(int color) {
            this.statusBarColor = color;
            return this;
        }

        public Builder navBarVisible(boolean visible) {
            return navBarVisible(visible, true, true);
        }

        public Builder navBarVisible(boolean visible, boolean immersive, boolean sticky) {
            this.navBarVisible = visible;
            this.isNavBarImmersive = immersive;
            this.isNavBarSticky = sticky;
            return this;
        }

        public Builder navBarColor(int color) {
            this.navBarColor = color;
            return this;
        }

        public Builder isNavBarLightMode(boolean lightMode) {
            this.isNavBarLightMode = lightMode;
            return this;
        }

        public Builder immerse(int type) {
            this.isImmerse = true;
            this.immerseType = type;
            return this;
        }

        public Builder titleView(View view) {
            this.titleView = new WeakReference<>(view);
            return this;
        }

        public BarConfig build() {
            return new BarConfig(this);
        }
    }


    public void apply(BarUtils barUtils) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            barUtils.setLayoutInDisplayCutoutMode(layoutInDisplayCutoutMode);
        }
        barUtils.setForceDisplayCutout(forceDisplayCutout)
                .setStatusBarVisibility(statusBarVisible, isStatusBarImmersive, isStatusBarSticky)
                .setStatusBarLightMode(isStatusBarLightMode)
                .setStatusBarColor(statusBarColor)
                .setNavBarVisibility(navBarVisible, isNavBarImmersive, isNavBarSticky)
                .setNavBarColor(navBarColor)
                .setNavBarLightMode(isNavBarLightMode);
        if (isImmerse) {
            barUtils.immerse(immerseType);
            if (titleView != null && titleView.get() != null) {
                barUtils.titleView(titleView.get());
            }
        } else {
            barUtils.exitImmerse();
        }
    }
}
