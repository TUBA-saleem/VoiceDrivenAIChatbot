
package com.example.childeducation

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object UserPreferences {
    private const val PREF_NAME = "user_prefs"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_PASSWORD = "user_password"

    fun getEmail(context: Context): Flow<String?> = flow {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        emit(prefs.getString(KEY_EMAIL, null))
    }

    fun getPassword(context: Context): Flow<String?> = flow {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        emit(prefs.getString(KEY_PASSWORD, null))
    }

    fun saveEmail(context: Context, email: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_EMAIL, email).apply()
    }

    fun savePassword(context: Context, password: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PASSWORD, password).apply()
    }

    fun clearEmail(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_EMAIL).apply()
    }

    fun clearPassword(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_PASSWORD).apply()
    }
}
