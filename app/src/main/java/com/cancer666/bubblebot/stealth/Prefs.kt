package com.cancer666.bubblebot.stealth

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private lateinit var sp: SharedPreferences

    fun init(ctx: Context) {
        if (!::sp.isInitialized) {
            sp = ctx.applicationContext.getSharedPreferences("bubble_stealth", Context.MODE_PRIVATE)
        }
    }
}
