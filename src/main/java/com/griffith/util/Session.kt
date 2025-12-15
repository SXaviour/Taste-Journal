package com.griffith.util

import android.content.Context

object Session {
    private const val PREF = "taste_session"
    private const val KEY_USER = "user_id"

    fun setUser(ctx: Context, id: Long) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_USER, id)
            .apply()
    }

    fun userId(ctx: Context): Long {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getLong(KEY_USER, 0L)
    }

    fun clear(ctx: Context) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_USER)
            .apply()
    }
}
