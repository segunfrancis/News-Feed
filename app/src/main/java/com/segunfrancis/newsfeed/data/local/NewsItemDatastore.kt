package com.segunfrancis.newsfeed.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.segunfrancis.newsfeed.util.AppConstants.MENU_OPTION_PREF_KEY
import com.segunfrancis.newsfeed.util.datastore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NewsItemDatastore @Inject constructor(@ApplicationContext private val context: Context) {

    suspend fun setMenuOption(optionIndex: Int) {
        context.datastore.edit {
            it[intPreferencesKey(MENU_OPTION_PREF_KEY)] = optionIndex
        }
    }

    fun getMenuOption(): Flow<Int> {
        return context.datastore.data.map {
            it[intPreferencesKey(MENU_OPTION_PREF_KEY)] ?: 0
        }
    }
}
