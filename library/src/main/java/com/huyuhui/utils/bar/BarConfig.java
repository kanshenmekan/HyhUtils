package com.huyuhui.utils.bar;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.view.WindowInsetsCompat;

class BarConfig implements Cloneable{
    private  volatile static BarConfig defaultBarConfig;
    public static BarConfig getDefaultBarConfig(){
        if (defaultBarConfig == null){
            defaultBarConfig = new BarConfig();
        }
        return defaultBarConfig;
    }
    public static void setDefaultBarConfig(BarConfig defaultBarConfig){
        BarConfig.defaultBarConfig = defaultBarConfig;
    }
     int layoutInDisplayCutoutMode = 0;
     boolean needDisplayCutout = false;

     boolean statusBarVisible = true;

     boolean isStatusBarImmersive = true;
     boolean isStatusBarSticky = true;

     boolean isStatusBarLightMode = false;
     int statusBarColor = Color.BLACK;

     boolean navBarVisible = true;
     boolean isNavBarImmersive = true;
     boolean isNavBarSticky = true;
     int navBarColor = Color.WHITE;

     boolean isNavBarLightMode = true;

     boolean isImmerse = false;
     @WindowInsetsCompat.Type.InsetsType int immerseType = WindowInsetsCompat.Type.systemBars();

    @NonNull
    @Override
    public BarConfig clone() {
        try {
            return (BarConfig) super.clone();
        } catch (CloneNotSupportedException e) {
//            throw new AssertionError();
            return new BarConfig();
        }
    }
}
