package com.apps.kinety_mobile.data.model

data class Project(
    val id: String,
    val name: String,
    val key: String,
    val description: String = "",
    val color: String = "#A33C59",
    val icon: String = "folder",
    val issueCount: Int = 0,
    val doneCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
