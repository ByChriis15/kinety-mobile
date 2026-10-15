package com.apps.kinety_mobile.data.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val initials: String = "",
    val color: String = "#A33C59",
    val avatarUrl: String? = null
)
