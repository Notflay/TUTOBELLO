package com.example.adopmekotlin.activity.entity

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val firstname: String? = null,
    val lastname: String? = null,
    val email: String? = null,
    var phoneNumber: String? = null,
)
