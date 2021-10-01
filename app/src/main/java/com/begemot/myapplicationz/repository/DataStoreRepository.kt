package com.begemot.myapplicationz.repository

import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.begemot.myapplicationz.repository.DataStoreRepository.PreferenceKeys.FONT_SIZE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreRepository(private val dataStore: DataStore<Preferences>) {
    private object PreferenceKeys{
       val FONT_SIZE = intPreferencesKey("fontsize")
   }

   suspend fun saveFontSize(textSize: Int){
        dataStore.edit{ preference ->
            preference[FONT_SIZE] = textSize

        }
    }

    val readFromDataStore : Flow<MyPrefs> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                //Log.d("DataStoreRepository", exception.message.toString())
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preference ->
            val fontsize = preference[FONT_SIZE] ?: 10

            MyPrefs(fontsize)
        }


}


data class MyPrefs(val textSize:Int)


