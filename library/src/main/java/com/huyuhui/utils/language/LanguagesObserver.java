package com.huyuhui.utils.language;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;

import androidx.annotation.NonNull;

import java.util.Locale;

class LanguagesObserver implements ComponentCallbacks {
    /**
     * 系统语种
     */
    private static volatile Locale sSystemLanguage;

    private static volatile Locale sAppLanguage;

    private static LanReceiver lanReceiver;

    static void register(Application application) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (lanReceiver == null) {
                lanReceiver = new LanReceiver();
                application.registerReceiver(lanReceiver, new IntentFilter(Intent.ACTION_LOCALE_CHANGED));
            }
        }
        sSystemLanguage = LanguagesUtils.getSystemLocale(application);
        sAppLanguage = MultiLanguages.getAppLanguage();
        LanguagesObserver languagesObserver = new LanguagesObserver();
        application.registerComponentCallbacks(languagesObserver);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onAppLocaleChange(sAppLanguage, LanguagesUtils.getLocale(newConfig));
            onSystemLocaleChange(sSystemLanguage, LanguagesUtils.getSystemLocale(MultiLanguages.getApplication()));
        } else {
            LanguagesUtils.updateConfigurationChanged(MultiLanguages.getApplication(), newConfig);
            LanguagesUtils.setDefaultLocale(MultiLanguages.getApplication());
            if (MultiLanguages.isSystemLanguage(MultiLanguages.getApplication())) {
                onAppLocaleChange(sAppLanguage, LanguagesUtils.getLocale(newConfig));
            }
            onSystemLocaleChange(sSystemLanguage, LanguagesUtils.getLocale(newConfig));
        }
    }

    static void onAppLocaleChange(Locale oldLocale, Locale newLocale) {
        if (oldLocale.equals(newLocale)) return;
        OnLanguageListener listener = MultiLanguages.getOnLanguagesListener();
        if (listener != null) {
            listener.onAppLocaleChange(oldLocale, newLocale);
        }
        sAppLanguage = newLocale;
    }

    private static void onSystemLocaleChange(Locale oldLocale, Locale newLocale) {
        if (oldLocale.equals(newLocale)) return;
        OnLanguageListener listener = MultiLanguages.getOnLanguagesListener();
        if (listener != null) {
            listener.onSystemLocaleChange(oldLocale, newLocale);
        }
        sSystemLanguage = newLocale;
    }

    @Override
    public void onLowMemory() {

    }

    private static class LanReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_LOCALE_CHANGED.equals(intent.getAction())) {
                onSystemLocaleChange(sSystemLanguage, LanguagesUtils.getSystemLocale(context));
            }
        }
    }
}
