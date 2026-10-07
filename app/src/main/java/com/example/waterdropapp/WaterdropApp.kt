package com.example.waterdropapp

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class WaterdropApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val modoTema = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(modoTema)
    }
}