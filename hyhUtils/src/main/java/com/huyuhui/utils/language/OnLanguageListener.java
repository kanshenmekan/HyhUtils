package com.huyuhui.utils.language;

import java.util.Locale;

public interface OnLanguageListener {

    /**
     * 当前应用语种发生变化时回调
     *
     * @param oldLocale         旧语种
     * @param newLocale         新语种
     */
    void onAppLocaleChange(Locale oldLocale, Locale newLocale);

    /**
     * 手机系统语种发生变化时回调
     *
     * @param oldLocale         旧语种
     * @param newLocale         新语种
     */
    void onSystemLocaleChange(Locale oldLocale, Locale newLocale);
}