package com.apps.kinety_mobile.data.model

data class ProjectModule(
    val id: String,
    val projectId: String,
    val name: String,
    val description: String = "",
    val color: String = "#6F2439",
    val sortOrder: Int = 0,
    val issueCount: Int = 0
)
