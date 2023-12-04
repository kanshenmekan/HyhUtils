package com.huyuhui.utils.demo.languages

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.huyuhui.utils.language.MultiLanguages

class LanguagesService : Service() {

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(MultiLanguages.attach(newBase))

    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.e("123","${resources.configuration.locales}")
        }
        return START_NOT_STICKY
    }
}