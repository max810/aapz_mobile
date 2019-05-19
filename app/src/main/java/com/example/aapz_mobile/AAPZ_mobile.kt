package com.example.aapz_mobile

import android.app.Application
import android.content.Context


class AAPZ_mobile : Application() {

    override fun onCreate() {
        super.onCreate()
        AAPZ_mobile.appContext = applicationContext
    }

    companion object {
        var appContext: Context? = null
            private set
    }
}