package com.example.wifianalyser.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.wifianalyser.models.AccessPointFilterEnum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferences @Inject constructor(private val context: Context) {

    suspend fun saveAccessPointFilter(filterEnum: AccessPointFilterEnum) {
        context.dataStore.edit { it[CURRENT_ACCESS_POINT_FILTER] = filterEnum.value }
    }

    suspend fun getAccessPointFilter(): AccessPointFilterEnum {
        val value = context.dataStore.getPreference { it[CURRENT_ACCESS_POINT_FILTER] ?: 0 }
        return AccessPointFilterEnum.fromValue(value)
    }
    private suspend fun <T, V> DataStore<T>.getPreference(transformation: (T) -> V): V {
        return data.map(transformation).first()
    }
    //THEME PREFERENCE SETUP

    suspend fun setThemeStatus(isDark:Boolean){
        context.dataStore.edit { it[THEME_STATUS] = isDark }
    }
    suspend fun getThemeStatus(): Flow<Boolean> {
      return  context.dataStore.data.map { it[THEME_STATUS]?:false }
    }


    companion object {
        val CURRENT_ACCESS_POINT_FILTER = intPreferencesKey("key_current_access_point_filter")
        val THEME_STATUS = booleanPreferencesKey("key_theme")
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("preferences")
    }

}


