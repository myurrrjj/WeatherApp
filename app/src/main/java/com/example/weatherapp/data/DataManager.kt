package com.example.weatherapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataManager(private val context: Context) {

    private val lastCityKey = stringPreferencesKey("last_city")

    val lastCityFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[lastCityKey] ?: ""
        }

    suspend fun saveLastCity(city: String) {
        context.dataStore.edit { settings ->
            settings[lastCityKey] = city
        }
    }
}
