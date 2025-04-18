package com.huyuhui.utils.demo;

import android.app.Application;
import android.content.Context;
import android.util.Log;


import com.huyuhui.utils.language.MultiLanguages;
import com.huyuhui.utils.language.OnLanguageListener;

import java.util.Locale;


public final class AppApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化多语种框架
        MultiLanguages.init(this);
        // 设置语种变化监听器
        MultiLanguages.setOnLanguageListener(new OnLanguageListener() {

            @Override
            public void onAppLocaleChange(Locale oldLocale, Locale newLocale) {

                Log.e("MultiLanguages", "监听到应用切换了语种，旧语种：" + oldLocale + "，新语种：" + newLocale);
            }

            @Override
            public void onSystemLocaleChange(Locale oldLocale, Locale newLocale) {
                Log.e("MultiLanguages", "监听到系统切换了语种，旧语种：" + oldLocale + "，新语种：" + newLocale +
                        "，是否跟随系统：" + MultiLanguages.isSystemLanguage());
            }

        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        // 绑定语种
        super.attachBaseContext(MultiLanguages.attach(newBase));
    }
}