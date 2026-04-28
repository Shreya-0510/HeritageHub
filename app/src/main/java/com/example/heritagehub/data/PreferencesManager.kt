package com.example.heritagehub.data

import android.content.Context
import android.content.SharedPreferences

object PreferencesManager {
    private const val PREF_NAME = "heritage_hub_prefs"
    private const val KEY_REMEMBER_ME = "remember_me"
    private const val KEY_SAVED_IDENTIFIER = "saved_identifier"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setRememberMe(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_REMEMBER_ME, enabled).apply()
    }

    fun isRememberMeEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_REMEMBER_ME, false)
    }

    fun setSavedIdentifier(context: Context, identifier: String?) {
        getPrefs(context).edit().putString(KEY_SAVED_IDENTIFIER, identifier).apply()
    }

    fun getSavedIdentifier(context: Context): String? {
        return getPrefs(context).getString(KEY_SAVED_IDENTIFIER, null)
    }

    fun clearSavedCredentials(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_REMEMBER_ME)
            .remove(KEY_SAVED_IDENTIFIER)
            .apply()
    }
}
