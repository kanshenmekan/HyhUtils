package com.huyuhui.utils.language;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;


@SuppressWarnings("unused")
public final class MultiLanguages {

    /**
     * 应用上下文对象
     */
    private static Application sApplication;

    /**
     * 语种变化监听对象
     */
    private static OnLanguageListener sLanguageListener;

    /**
     * 初始化多语种框架
     */
    public static void init(final Application application) {
        sApplication = application;
        LanguagesUtils.setDefaultLocale(application);
        //api小于33，设置一下语言，防止没有设置autoStoreLocales为true的情况
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && !LanguagesConfig.isConfigSystemLanguage(application)) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(LanguagesUtils.getLocale(application)));
        }

        // 等所有的任务都执行完了，再设置对系统语种的监听，用户不可能在这点间隙的时间完成切换语言的
        // 经过实践证明 IdleHandler 会在第一个 Activity attachBaseContext 之后调用的，所以没有什么问题
        Looper.myQueue().addIdleHandler(() -> {
            LanguagesObserver.register(application);
            return false;
        });
    }

    /**
     * 在上下文的子类中重写 attachBaseContext 方法（用于更新 Context 的语种）
     */
    public static Context attach(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (LanguagesConfig.isConfigSystemLanguage(context)) {
                return context;
            } else {
                Locale locale = LanguagesConfig.readConfigLanguageSetting(context);
                if (locale == null || LanguagesUtils.getLocale(context).equals(locale)) {
                    return context;
                }
                return LanguagesUtils.attachLanguages(context, locale);
            }
        }
        return context;
    }

    /**
     * @return 语言是否发生变化
     */
    public static boolean applySystemLanguage() {
        if (sApplication == null) return false;
        if (AppCompatDelegate.getApplicationLocales().isEmpty()) return false;
        Locale oldLocale = getAppLanguage();
        LanguagesConfig.clearLanguageSetting(getApplication());
        if (getSystemLanguage(getApplication()).equals(oldLocale)) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList());
            return false;
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(getSystemLanguage(getApplication())));
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList());
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                //Api33以下的在设置的时候触发回调
                LanguagesUtils.updateLanguages(getApplication().getResources(), getSystemLanguage(getApplication()));
                LanguagesUtils.setDefaultLocale(getApplication());
                assert oldLocale != null;
                LanguagesObserver.onAppLocaleChange(oldLocale, getSystemLanguage(getApplication()));
            }
            return true;
        }
    }

    /**
     * @param locale 要设置的参数
     * @return 语言是否发生改变
     */
    public static boolean applyCustomLanguage(Locale locale) {
        if (sApplication == null) return false;
        if (locale == null) return false;
        Locale oldLocale = getAppLanguage();
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale));
        if (locale.equals(oldLocale)) {
            return false;
        } else {
            LanguagesConfig.saveAppLanguageSetting(getApplication(), locale);
            //Api33以下的在设置的时候触发回调
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                LanguagesUtils.updateLanguages(getApplication().getResources(), locale);
                LanguagesUtils.setDefaultLocale(getApplication());
                assert oldLocale != null;
                LanguagesObserver.onAppLocaleChange(oldLocale, getAppLanguage());
            }
            return true;
        }
    }

    /**
     * 更新 Context 的语种
     */
    public static void updateAppLanguage(Context context) {
        updateAppLanguage(context.getResources());
    }

    /**
     * 更新 Resources 的语种
     */
    public static void updateAppLanguage(Resources resources) {
        if (resources == null) {
            return;
        }
        if (LanguagesUtils.getLocale(resources.getConfiguration()).equals(getAppLanguage())) {
            return;
        }
        LanguagesUtils.updateLanguages(resources, getAppLanguage());
    }

    /**
     * 在init之后调用
     */
    public static Locale getAppLanguage() {
        if (sApplication == null) {
            return null;
        }
        if (AppCompatDelegate.getApplicationLocales().isEmpty()) {
            return getSystemLanguage(getApplication());
        }
        return AppCompatDelegate.getApplicationLocales().get(0);
    }

    /**
     * 获取系统的语种
     */
    public static Locale getSystemLanguage(Context context) {
        return LanguagesUtils.getSystemLocale(context);
    }

    /**
     * 是否跟随系统的语种
     */
    public static boolean isSystemLanguage(Context context) {
        return AppCompatDelegate.getApplicationLocales().isEmpty();
    }


    /**
     * 对比两个语言是否是同一个语种（比如：中文有简体和繁体，但是它们都属于同一个语种）
     */
    public static boolean equalsLanguage(Locale locale1, Locale locale2) {
        return TextUtils.equals(locale1.getLanguage(), locale2.getLanguage());
    }

    /**
     * 对比两个语言是否是同一个地方的（比如：中国大陆用的中文简体，中国台湾用的中文繁体）
     */
    public static boolean equalsCountry(Locale locale1, Locale locale2) {
        return equalsLanguage(locale1, locale2) &&
                TextUtils.equals(locale1.getCountry(), locale2.getCountry());
    }

    /**
     * 获取某个语种下的 String
     */
    public static String getLanguageString(Context context, Locale locale, int id) {
        return getLanguageResources(context, locale).getString(id);
    }

    /**
     * 获取某个语种下的 Resources 对象
     */
    public static Resources getLanguageResources(Context context, Locale locale) {
        return LanguagesUtils.getLanguageResources(context, locale);
    }

    /**
     * 设置语种变化监听器
     */
    public static void setOnLanguageListener(OnLanguageListener listener) {
        sLanguageListener = listener;
    }

    /**
     * 设置保存的 SharedPreferences 文件名（请在 Application 初始化之前设置，可以放在 Application 中的代码块或者静态代码块）
     */
    public static void setSharedPreferencesName(String name) {
        LanguagesConfig.setSharedPreferencesName(name);
    }

    /**
     * 获取语种系统设置界面（Android 13 及以上才有的）
     */
    public static Intent getLanguageSettingIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent intent = new Intent(Settings.ACTION_APP_LOCALE_SETTINGS);
            intent.setData(Uri.parse("package:" + sApplication.getPackageName()));
            if (LanguagesUtils.areActivityIntent(sApplication, intent)) {
                return intent;
            }
        }
        return null;
    }

    /**
     * 获取语种变化监听对象
     */
    static OnLanguageListener getOnLanguagesListener() {
        return sLanguageListener;
    }

    /**
     * 获取应用上下文
     */
    static Application getApplication() {
        return sApplication;
    }
}