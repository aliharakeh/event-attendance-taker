package com.example.attendancetaker.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import java.util.Locale

class LanguageManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "language_prefs"
        private const val KEY_LANGUAGE = "selected_language"
        private const val LANGUAGE_ENGLISH = "en"
        private const val LANGUAGE_ARABIC = "ar"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentLanguage = mutableStateOf(getCurrentLanguage())
    val currentLanguage: State<String> = _currentLanguage

    private var onLanguageChangeListener: (() -> Unit)? = null

    private fun getCurrentLanguage(): String {
        return sharedPreferences.getString(KEY_LANGUAGE, LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH
    }

    fun switchLanguage() {
        val newLanguage = if (_currentLanguage.value == LANGUAGE_ENGLISH) {
            LANGUAGE_ARABIC
        } else {
            LANGUAGE_ENGLISH
        }
        setLanguage(newLanguage)
    }

                fun setLanguage(languageCode: String) {
        sharedPreferences.edit()
            .putString(KEY_LANGUAGE, languageCode)
            .apply()

        updateAppLanguage(languageCode)
        _currentLanguage.value = languageCode

        // Notify listeners about language change
        onLanguageChangeListener?.invoke()
    }

    fun setOnLanguageChangeListener(listener: () -> Unit) {
        onLanguageChangeListener = listener
    }

        private fun updateAppLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    fun initializeLanguage() {
        val savedLanguage = getCurrentLanguage()
        updateAppLanguage(savedLanguage)
        _currentLanguage.value = savedLanguage
    }

    fun isArabic(): Boolean = _currentLanguage.value == LANGUAGE_ARABIC
    fun isEnglish(): Boolean = _currentLanguage.value == LANGUAGE_ENGLISH

    fun getCurrentLanguageName(): String {
        return when (_currentLanguage.value) {
            LANGUAGE_ARABIC -> "العربية"
            LANGUAGE_ENGLISH -> "English"
            else -> "English"
        }
    }
}