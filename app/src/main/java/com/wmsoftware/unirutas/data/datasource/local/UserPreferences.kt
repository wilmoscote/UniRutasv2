package com.wmsoftware.unirutas.data.datasource.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wmsoftware.unirutas.domain.model.LocationInfo
import com.wmsoftware.unirutas.domain.model.User
import com.wmsoftware.unirutas.util.utilities.Const.TAG
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "userPreferences")
@Singleton
class UserPreferences @Inject constructor(@ApplicationContext private val context: Context) {
    private val dataStore: DataStore<Preferences> by lazy {
        context.dataStore
    }

    suspend fun clearDataStore() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /** GETTERS **/
    fun getUser(): Flow<User?> {
        return dataStore.data.map { preferences ->
            val jsonString = preferences[stringPreferencesKey("user")]
            if (!jsonString.isNullOrEmpty()) {
                try {
                    Json.decodeFromString<User>(jsonString)
                } catch (e: Exception) {
                    Log.e(TAG, "Error decoding user", e)
                    null
                }
            } else {
                null
            }
        }
    }

    fun getUserLastLocation(): Flow<LocationInfo?> {
        return dataStore.data.map { preferences ->
            val jsonString = preferences[stringPreferencesKey("user_last_location")]
            if (!jsonString.isNullOrEmpty()) {
                try {
                    Json.decodeFromString<LocationInfo>(jsonString)
                } catch (e: Exception) {
                    Log.e(TAG, "Error decoding location", e)
                    null
                }
            } else {
                null
            }
        }
    }

    fun getUserToken() = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey("user_token")]
    }

    /*fun getFavorites(): Flow<List<Deal>> {
        return dataStore.data.map { preferences ->
            val jsonString = preferences[stringPreferencesKey("favorites")] ?: "[]"
            Json.decodeFromString(jsonString)
        }
    }*/

    fun getFcmToken() = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey("fcm_token")]
    }

    fun getMapTypeConfig() = dataStore.data.map { preferences ->
        preferences[intPreferencesKey("map_type_config")]
    }

    fun getMapTraficConfig() = dataStore.data.map { preferences ->
        preferences[booleanPreferencesKey("map_trafic_config")]
    }

    fun getMapMyLocationConfig() = dataStore.data.map { preferences ->
        preferences[booleanPreferencesKey("map_my_location_config")]
    }

    fun getSendNotifications() = dataStore.data.map { preferences ->
        preferences[booleanPreferencesKey("send_notifications")]
    }

    /** SETTERS **/
    suspend fun saveUser(user: User?) {
        if(user != null){
            val jsonString = Json.encodeToString(User.serializer(), user)
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey("user")] = jsonString
            }
        }
    }

    suspend fun saveUserLastLocation(location: LocationInfo?){
        location?.let {
            val jsonString = Json.encodeToString(LocationInfo.serializer(), location)
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey("user_last_location")] = jsonString
            }
        }
    }

    suspend fun saveUserToken(token: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("user_token")] = token
        }
    }

    suspend fun saveMapTypeConfig(mapType: Int) {
        dataStore.edit { preferences ->
            preferences[intPreferencesKey("map_type_config")] = mapType
        }
    }

    suspend fun saveMapTraficConfig(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("map_trafic_config")] = isEnabled
        }
    }

    suspend fun saveMapMyLocationConfig(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("map_my_location_config")] = isEnabled
        }
    }

   /* suspend fun saveFavorites(favorites: List<Deal>) {
        val jsonString = Json.encodeToString(favorites)
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("favorites")] = jsonString
        }
    }
*/
    suspend fun saveFcmToken(token: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("fcm_token")] = token
        }
    }

    suspend fun saveSendNotifications(sendNotifications: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("send_notifications")] = sendNotifications
        }
    }
}