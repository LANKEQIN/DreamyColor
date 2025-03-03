package com.lovelive.dreamycolor

import android.app.Application
import android.content.Context

class DreamyColorApplication: Application() {

    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}