package com.huyuhui.hyhutilskotlin.language

import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppLocalesMetadataHolderService

import java.util.Locale

@Suppress("deprecation")
internal object LanguagesUtils {
    /**
     * 获取语种对象
     */
    @JvmStatic
    fun getLocale(context: Context): Locale {
        return getLocale(context.resources.configuration)
    }

    @JvmStatic
    fun getLocale(config: Configuration): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.locales[0]
        } else {
            config.locale
        }
    }

    /**
     * 设置语种对象
     */
    @JvmStatic
    fun setLocale(config: Configuration, locale: Locale) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            config.setLocales(localeList)
        } else {
            config.setLocale(locale)
        }
    }

    /**
     * 获取系统的语种对象
     */
    @JvmStatic
    fun getSystemLocale(context: Context): Locale {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 在 Android 13 上，不能用 Resources.getSystem() 来获取系统语种了
            // Android 13 上面新增了一个 LocaleManager 的语种管理类
            // 因为如果调用 LocaleManager.setApplicationLocales 会影响获取到的结果不准确
            // 所以应该得用 LocaleManager.getSystemLocales 来获取会比较精准
            val localeManager = context.getSystemService(
                LocaleManager::class.java
            )
            if (localeManager != null) {
                return localeManager.systemLocales[0]
            }
        }

        return getLocale(Resources.getSystem().configuration)
    }

    /**
     * 获取context当前应用的语言
     */
    @JvmStatic
    fun getAppLocale(context: Context): Locale {
        return if (AppCompatDelegate.getApplicationLocales().isEmpty)
            getSystemLocale(context)
        else AppCompatDelegate.getApplicationLocales()[0]!!
    }

    /**
     * 是否跟随系统的语种
     */
    @JvmStatic
    fun isSystemLanguage(): Boolean {
        return AppCompatDelegate.getApplicationLocales().isEmpty
    }

    /**
     * 设置默认的语种环境（日期格式化会用到）
     */
    @JvmStatic
    fun setDefaultLocale(context: Context) {
        val configuration = context.resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.setDefault(configuration.locales)
        } else {
            Locale.setDefault(configuration.locale)
        }
    }

    /**
     * 绑定当前 App 的语种
     */
    @JvmStatic
    fun attachLanguages(context: Context, locale: Locale): Context {
        val resources = context.resources
        val config = Configuration(resources.configuration)
        setLocale(config, locale)
        val attachedContext = context.createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
        return attachedContext
    }

    /**
     * 更新 Resources 语种
     */
    @JvmStatic
    fun updateLanguages(resources: Resources, locale: Locale) {
        val config = resources.configuration
        setLocale(config, locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    /**
     * 更新手机配置信息变化
     */
    @JvmStatic
    fun updateConfigurationChanged(context: Context, newConfig: Configuration) {
        //不为空，表示设置了不跟随系统的语言
        if (!AppCompatDelegate.getApplicationLocales().isEmpty) {
            val currentLocale = AppCompatDelegate.getApplicationLocales()[0]
            if (getLocale(newConfig) != currentLocale) {
                val config = Configuration(newConfig)
                // 绑定当前语种到这个新的配置对象中
                setLocale(config, currentLocale!!)
                val resources = context.resources
                // 更新上下文的配置信息
                resources.updateConfiguration(config, resources.displayMetrics)
            }
        }
    }

    /**
     * 获取某个语种下的 Resources 对象
     */
    @JvmStatic
    fun getLanguageResources(context: Context, locale: Locale): Resources {
        val config = Configuration()
        setLocale(config, locale)
        return context.createConfigurationContext(config).resources
    }

    /**
     * 判断这个意图的 Activity 是否存在
     */
    @JvmStatic
    fun areActivityIntent(context: Context, intent: Intent?): Boolean {
        if (intent == null) {
            return false
        }
        // 这里为什么不用 Intent.resolveActivity(intent) != null 来判断呢？
        // 这是因为在 OPPO R7 Plus （Android 5.0）会出现误判，明明没有这个 Activity，却返回了 ComponentName 对象
        val packageManager = context.packageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            ).isNotEmpty()
        }
        return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .isNotEmpty()
    }

    @JvmStatic
    fun hasAutoStoreLocales(context: Context): Boolean {
        try {
            val serviceInfo = AppLocalesMetadataHolderService.getServiceInfo(context)
            val metaData = serviceInfo.metaData
            if (metaData != null) {
                // 在meta-data中获取你感兴趣的值
                return metaData.getBoolean("autoStoreLocales")
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return false
    }
}