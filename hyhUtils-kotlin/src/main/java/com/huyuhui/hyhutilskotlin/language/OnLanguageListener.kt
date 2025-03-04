package com.huyuhui.hyhutilskotlin.language

import java.util.Locale

interface OnLanguageListener {
    /**
     * 当前应用语种发生变化时回调
     *
     * @param oldLocale         旧语种
     * @param newLocale         新语种
     */
    fun onAppLocaleChange(oldLocale: Locale?, newLocale: Locale?)

    /**
     * 手机系统语种发生变化时回调
     *
     * @param oldLocale         旧语种
     * @param newLocale         新语种
     */
    fun onSystemLocaleChange(oldLocale: Locale?, newLocale: Locale?)
}