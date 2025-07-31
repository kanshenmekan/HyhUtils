package com.huyuhui.hyhutilskotlin.language

import android.app.Application
import android.content.BroadcastReceiver
import android.content.ComponentCallbacks
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import java.util.Locale
import kotlin.reflect.KProperty

internal class LanguagesObserver(private val application: Application) : ComponentCallbacks {
    /**
     * onConfigurationChanged
     * API 33及以上版本,AppCompatDelegate.setApplicationLocales会触发,在系统设置单个app语言的页面会触发，
     * 如果app设置为跟随系统语言，改变系统语言时会触发，没有跟随系统语言的情况下，改变系统语言不会触发
     *
     * API33以下AppCompatDelegate.setApplicationLocales不会触发，没有设置单个app语言的页面，
     * 更改系统语言会触发
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onAppLocaleChange(sAppLanguage, LanguagesUtils.getLocale(newConfig))
            onSystemLocaleChange(sSystemLanguage, LanguagesUtils.getSystemLocale(application))
        } else {
            //如果没有跟随系统，就把当前的设置覆盖掉系统语言的变更
            LanguagesUtils.updateConfigurationChanged(application, newConfig)
            LanguagesUtils.setDefaultLocale(application)
            //如果是跟随系统，就触发回调
            if (LanguagesUtils.isSystemLanguage()) {
                onAppLocaleChange(sAppLanguage, LanguagesUtils.getLocale(newConfig))
            }
            onSystemLocaleChange(sSystemLanguage, LanguagesUtils.getLocale(newConfig))
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
    }

    private class LanReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Intent.ACTION_LOCALE_CHANGED == intent.action) {
                onSystemLocaleChange(sSystemLanguage, LanguagesUtils.getSystemLocale(context))
            }
        }
    }

    companion object {
        /**
         * 系统语种
         */
        @Volatile
        private lateinit var sSystemLanguage: Locale

        @Volatile
        private lateinit var sAppLanguage: Locale

        private var lanReceiver: LanReceiver? = null

        @JvmStatic
        private var onLanguageListener: OnLanguageListener? = null

        @JvmStatic
        fun register(application: Application) {
            sSystemLanguage = LanguagesUtils.getSystemLocale(application)
            sAppLanguage = LanguagesUtils.getAppLocale(application)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (lanReceiver == null) {
                    lanReceiver = LanReceiver()
                    application.registerReceiver(
                        lanReceiver,
                        IntentFilter(Intent.ACTION_LOCALE_CHANGED)
                    )
                }
            }
            val languagesObserver = LanguagesObserver(application)
            application.registerComponentCallbacks(languagesObserver)
        }

        @JvmStatic
        fun onAppLocaleChange(oldLocale: Locale, newLocale: Locale) {
            if (oldLocale == newLocale) return
            val listener = onLanguageListener
            listener?.onAppLocaleChange(oldLocale, newLocale)
            sAppLanguage = newLocale
        }

        @JvmStatic
        private fun onSystemLocaleChange(oldLocale: Locale, newLocale: Locale) {
            if (oldLocale == newLocale) return
            val listener = onLanguageListener
            listener?.onSystemLocaleChange(oldLocale, newLocale)
            sSystemLanguage = newLocale
        }

        operator fun getValue(
            thisRef: Any?,
            property: KProperty<*>,
        ): OnLanguageListener? {
            return onLanguageListener
        }

        operator fun setValue(
            thisRef: Any?,
            property: KProperty<*>,
            onLanguageListener: OnLanguageListener?,
        ) {
            this.onLanguageListener = onLanguageListener
        }
    }
}
