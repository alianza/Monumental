//@file:Suppress("DEPRECATION")

package com.example.monumental.viewModel.firstTime

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import com.example.monumental.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirstTimeViewModel(application: Application) : AndroidViewModel(application) {

    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val context = getApplication<Application>().applicationContext

    fun setSharedPref() {
        mainScope.launch {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val edit = prefs.edit()
            edit.putBoolean(context.getString(R.string.pref_previously_started), true)
            edit.apply()
        }
    }
}