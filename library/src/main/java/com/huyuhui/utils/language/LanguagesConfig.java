package com.huyuhui.utils.language;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Locale;

public class LanguagesConfig {
    private static final String KEY_LANGUAGE = "key_language";
    private static final String KEY_COUNTRY = "key_country";
    private static String sSharedPreferencesName = "language_setting";


    static void setSharedPreferencesName(String name) {
        sSharedPreferencesName = name;
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(sSharedPreferencesName, Context.MODE_PRIVATE);
    }

    /**
     * 读取 App 语种
     */
    static Locale readConfigLanguageSetting(Context context) {
        String language = getSharedPreferences(context).getString(KEY_LANGUAGE, "");
        String country = getSharedPreferences(context).getString(KEY_COUNTRY, "");
        if (!TextUtils.isEmpty(language)) {
            return new Locale(language, country);
        }

        return null;
    }

    /**
     * 保存 App 语种设置
     */
    static void saveAppLanguageSetting(Context context, Locale locale) {
        getSharedPreferences(context).edit()
                .putString(KEY_LANGUAGE, locale.getLanguage())
                .putString(KEY_COUNTRY, locale.getCountry())
                .apply();
    }

    /**
     * 清除语种设置
     */
    static void clearLanguageSetting(Context context) {
        getSharedPreferences(context).edit()
                .remove(KEY_LANGUAGE)
                .remove(KEY_COUNTRY)
                .apply();
    }

    /**
     * 是否跟随系统
     */
    static boolean isConfigSystemLanguage(Context context) {
        String language = getSharedPreferences(context).getString(KEY_LANGUAGE, "");
        return TextUtils.isEmpty(language);
    }

}
