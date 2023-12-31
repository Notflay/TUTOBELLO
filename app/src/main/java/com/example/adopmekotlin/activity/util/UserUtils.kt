package com.example.adopmekotlin.activity.util

import android.content.Context
import com.example.adopmekotlin.activity.entity.User
import com.google.gson.Gson

object UserUtils {

    fun saveUserToSharedPreferences(context: Context, user: User) {
        val userPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        val userJson = Gson().toJson(user)
        userPreferences.edit().putString("user", userJson).apply()
    }

    fun getUserFromSharedPreferences(context: Context): User? {
        val userPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        val userJson = userPreferences.getString("user", null)
        val user = Gson().fromJson(userJson, User::class.java)

        return user;
    }

    fun removeUserFromSharedPreferences(context: Context) {
        val userPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        userPreferences.edit().remove("user").apply()
    }
}