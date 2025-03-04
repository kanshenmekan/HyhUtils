package com.huyuhui.hyhutilskotlin.language

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import java.util.Locale

/**
 * 这个类负责初始化的时候数据读取，然后注入AppCompatDelegate，后续判断依靠AppCompatDelegate
 */
internal object LanguagesConfig {
    private const val KEY_LANGUAGE = "key_language"
    private const val KEY_COUNTRY = "key_country"
    private var sSharedPreferencesName = "language_setting"

    @JvmStatic
    fun setSharedPreferencesName(name: String) {
        sSharedPreferencesName = name
    }

    @JvmStatic
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(sSharedPreferencesName, Context.MODE_PRIVATE)
    }

    /**
     * 读取 App 语种
     */
    @JvmStatic
    fun readConfigLanguageSetting(context: Context): Locale? {
        with(getSharedPreferences(context)) {
            val language = getString(KEY_LANGUAGE, "")!!
            val country = getString(KEY_COUNTRY, "")!!
            return if (TextUtils.isEmpty(language)) null
            else Locale(language, country)
        }
    }

    /**
     * 保存 App 语种设置
     */
    @JvmStatic
    fun saveAppLanguageSetting(context: Context, locale: Locale) {
        getSharedPreferences(context).edit()
            .putString(KEY_LANGUAGE, locale.language)
            .putString(KEY_COUNTRY, locale.country)
            .apply()
    }

    /**
     * 清除语种设置
     */
    @JvmStatic
    fun clearLanguageSetting(context: Context) {
        getSharedPreferences(context).edit()
            .remove(KEY_LANGUAGE)
            .remove(KEY_COUNTRY)
            .apply()
    }

    /**
     * 是否跟随系统
     */
    @JvmStatic
    fun isConfigSystemLanguage(context: Context): Boolean {
        val language =
            getSharedPreferences(context).getString(KEY_LANGUAGE, "")!!
        return TextUtils.isEmpty(language)
    }
}
