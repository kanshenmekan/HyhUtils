package com.huyuhui.hyhutilskotlin.language

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * API 33 及以上：setApplicationLocales 会触发 onConfigurationChanged。
 * API 33 以下：setApplicationLocales 不会触发 onConfigurationChanged，需要手动处理。
 */
@Suppress("unused")
object MultiLanguages {
    /**
     * 应用上下文对象
     */
    private var _application: Application? = null

    @JvmStatic
    internal val application
        get() = _application!!

    /**
     * 语种变化监听对象
     */
    @JvmStatic
    var onLanguageListener: OnLanguageListener? by LanguagesObserver

    @JvmStatic
    val appLanguage: Locale
        /**
         * 在init之后调用
         */
        get() {
            if (_application == null) {
                throw IllegalStateException("没有初始化")
            }
            return LanguagesUtils.getAppLocale(application)
        }


    /**
     * 初始化多语种框架
     */
    @JvmStatic
    fun init(application: Application) {
        _application = application
        LanguagesUtils.setDefaultLocale(application)
        //api小于33，设置一下语言，防止没有设置autoStoreLocales为true的情况
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && !LanguagesConfig.isConfigSystemLanguage(application)
            && !LanguagesUtils.hasAutoStoreLocales(application)
        ) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.create(
                    LanguagesUtils.getLocale(application)
                )
            )
        }

        // 等所有的任务都执行完了，再设置对系统语种的监听，用户不可能在这点间隙的时间完成切换语言的
        // 经过实践证明 IdleHandler 会在第一个 Activity attachBaseContext 之后调用的，所以没有什么问题
        Looper.myQueue().addIdleHandler {
            LanguagesObserver.register(application)
            false
        }
    }

    /**
     * 在上下文的子类中重写 attachBaseContext 方法（用于更新 Context 的语种）
     */
    @JvmStatic
    fun attach(context: Context): Context {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (LanguagesConfig.isConfigSystemLanguage(context)) {
                return context
            } else {
                val localeListCompat = AppCompatDelegate.getApplicationLocales()
                val locale = if (localeListCompat.isEmpty) {
                    LanguagesConfig.readConfigLanguageSetting(context)
                } else {
                    localeListCompat[0]
                }
                if (locale == null || LanguagesUtils.getLocale(context) == locale) {
                    return context
                }
                return LanguagesUtils.attachLanguages(context, locale)
            }
        }
        return context
    }

    /**
     * @return 语言是否发生变化
     */
    @JvmStatic
    fun applySystemLanguage(): Boolean {
        if (_application == null) return false
        if (AppCompatDelegate.getApplicationLocales().isEmpty) return false
        val oldLocale = appLanguage
        LanguagesConfig.clearLanguageSetting(application)
        if (getSystemLanguage(application) == oldLocale) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
            return false
        } else {
            //先变成系统当前的语言，然后再清除设置，跟随系统变化
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.create(getSystemLanguage(application))
            )
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                //Api33以下的在设置的时候触发回调
                LanguagesUtils.updateLanguages(
                    application.resources,
                    getSystemLanguage(application)
                )
                LanguagesUtils.setDefaultLocale(application)
                LanguagesObserver.onAppLocaleChange(oldLocale, getSystemLanguage(application))
            }
            return true
        }
    }

    /**
     * @param locale 要设置的参数
     * @return 语言是否发生改变
     */
    @JvmStatic
    fun applyCustomLanguage(locale: Locale?): Boolean {
        if (_application == null) return false
        if (locale == null) return false
        val oldLocale = appLanguage
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale))
        if (locale == oldLocale) {
            return false
        } else {
            LanguagesConfig.saveAppLanguageSetting(application, locale)
            //Api33以下的在设置的时候触发回调
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                LanguagesUtils.updateLanguages(application.resources, locale)
                LanguagesUtils.setDefaultLocale(application)
                LanguagesObserver.onAppLocaleChange(oldLocale, appLanguage)
            }
            return true
        }
    }

    /**
     * 更新 Context 的语种
     */
    @JvmStatic
    fun updateAppLanguage(context: Context) {
        updateAppLanguage(context.resources)
    }

    /**
     * 更新 Resources 的语种
     */
    @JvmStatic
    fun updateAppLanguage(resources: Resources?) {
        if (resources == null) {
            return
        }
        if (LanguagesUtils.getLocale(resources.configuration) == appLanguage) {
            return
        }
        LanguagesUtils.updateLanguages(resources, appLanguage)
    }


    /**
     * 获取系统的语种
     */
    @JvmStatic
    fun getSystemLanguage(context: Context): Locale {
        return LanguagesUtils.getSystemLocale(context)
    }

    /**
     * 是否跟随系统的语种
     */
    @JvmStatic
    fun isSystemLanguage(): Boolean {
        return LanguagesUtils.isSystemLanguage()
    }


    /**
     * 对比两个语言是否是同一个语种（比如：中文有简体和繁体，但是它们都属于同一个语种）
     */
    @JvmStatic
    fun equalsLanguage(locale1: Locale, locale2: Locale): Boolean {
        return TextUtils.equals(locale1.language, locale2.language)
    }

    /**
     * 对比两个语言是否是同一个地方的（比如：中国大陆用的中文简体，中国台湾用的中文繁体）
     */
    @JvmStatic
    fun equalsCountry(locale1: Locale, locale2: Locale): Boolean {
        return equalsLanguage(locale1, locale2) &&
                TextUtils.equals(locale1.country, locale2.country)
    }

    /**
     * 获取某个语种下的 String
     */
    @JvmStatic
    fun getLanguageString(context: Context, locale: Locale, id: Int): String {
        return getLanguageResources(context, locale).getString(id)
    }

    /**
     * 获取某个语种下的 Resources 对象
     */
    @JvmStatic
    fun getLanguageResources(context: Context, locale: Locale): Resources {
        return LanguagesUtils.getLanguageResources(context, locale)
    }


    /**
     * 设置保存的 SharedPreferences 文件名（请在 Application 初始化之前设置，可以放在 Application 中的代码块或者静态代码块）
     */
    @JvmStatic
    fun setSharedPreferencesName(name: String) {
        LanguagesConfig.setSharedPreferencesName(name)
    }


    @JvmStatic
    val languageSettingIntent: Intent?
        /**
         * 获取语种系统设置界面（Android 13 及以上才有的）
         */
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
                intent.data = ("package:" + application.packageName).toUri()
                if (LanguagesUtils.areActivityIntent(application, intent)) {
                    return intent
                }
            }
            return null
        }
}