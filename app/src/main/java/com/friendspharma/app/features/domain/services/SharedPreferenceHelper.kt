package com.friendspharma.app.features.domain.services

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import com.friendspharma.app.features.data.remote.model.UserDto
import com.google.gson.Gson
import javax.inject.Inject

object LocalConstant {
    const val sharedPreferences = "sharedPreferences"
    const val token = "token"
    const val user = "user"
    const val isRestrict = "isRestrict"
}

class SharedPreferenceHelper @Inject constructor(application: Application) : AndroidViewModel(
    application
) {
    private val preferences: SharedPreferences = application.getSharedPreferences(
        LocalConstant.sharedPreferences,
        MODE_PRIVATE
    )

    fun getToken(): String {
        return preferences.getString(LocalConstant.token, "") ?: ""
    }

    fun saveStringData(key: String, data: String) {
        preferences.edit {
            putString(key, data)
        }
    }

    fun getStringData(key: String): String {
        return preferences.getString(key, "") ?: ""
    }


    fun saveIntData(key: String, data: Int){
        preferences.edit{
            putInt(key, data)
        }
    }

    fun getIntData(key: String): Int {
        return preferences.getInt(key, - 1)
    }

    fun saveUser(user: UserDto) {
        preferences.edit {
            putString(LocalConstant.user, Gson().toJson(user))
        }
    }

    fun getUser(): UserDto {
        return Gson().fromJson(preferences.getString(LocalConstant.user, ""), UserDto::class.java)
            ?: UserDto()
    }

    fun deleteAll() {
        preferences.edit {
            clear()
        }
    }
}