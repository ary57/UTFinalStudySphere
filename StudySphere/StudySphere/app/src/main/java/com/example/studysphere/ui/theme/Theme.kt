package com.example.studysphere.ui.theme

import android.content.Context
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.example.studysphere.R

object Theme {
    // Utility methods for theme-related operations
    fun applyDarkMode(isDarkMode: Boolean) {
        val mode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun getColor(context: Context, colorResId: Int): Int {
        return ContextCompat.getColor(context, colorResId)
    }
}