package com.example.adopmekotlin.activity.validation

data class ValidationResult(
    val ok: Boolean,
    val reason: String? = null
)
