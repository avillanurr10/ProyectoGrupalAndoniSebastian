package com.example.gamedeals.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    fun setLocale(context: Context, language: String): Context {
        val locale = when (language) {
            "Español" -> Locale("es")
            else -> Locale("en")
        }
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        
        return context.createConfigurationContext(configuration)
    }

    fun onAttach(context: Context, defaultLanguage: String): Context {
        return setLocale(context, defaultLanguage)
    }
}
