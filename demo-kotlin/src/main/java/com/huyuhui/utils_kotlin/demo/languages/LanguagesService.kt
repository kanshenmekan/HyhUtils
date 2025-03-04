package com.huyuhui.utils_kotlin.demo.languages

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.huyuhui.hyhutilskotlin.language.MultiLanguages

class LanguagesService : Service() {

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(if (newBase == null) null else MultiLanguages.attach(newBase))

    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("LanguagesService","${resources.configuration.locales}")
        return START_NOT_STICKY
    }
}