package com.apps.kinety_mobile.data.model

import androidx.compose.ui.graphics.Color
import com.apps.kinety_mobile.ui.theme.PriorityHigh
import com.apps.kinety_mobile.ui.theme.PriorityLow
import com.apps.kinety_mobile.ui.theme.PriorityMedium
import com.apps.kinety_mobile.ui.theme.PriorityNone
import com.apps.kinety_mobile.ui.theme.PriorityUrgent
import com.apps.kinety_mobile.ui.theme.StatusBacklog
import com.apps.kinety_mobile.ui.theme.StatusCancelled
import com.apps.kinety_mobile.ui.theme.StatusDone
import com.apps.kinety_mobile.ui.theme.StatusInProgress
import com.apps.kinety_mobile.ui.theme.StatusTodo

enum class IssueStatus(val rawValue: String, val displayName: String, val color: Color) {
    BACKLOG("backlog", "Backlog", StatusBacklog),
    TODO("todo", "Por hacer", StatusTodo),
    IN_PROGRESS("in_progress", "En progreso", StatusInProgress),
    DONE("done", "Hecho", StatusDone),
    CANCELLED("cancelled", "Cancelado", StatusCancelled);

    companion object {
        fun fromRaw(raw: String?): IssueStatus {
            return entries.firstOrNull { it.rawValue.equals(raw, ignoreCase = true) } ?: TODO
        }
    }
}

enum class IssuePriority(val rawValue: String, val displayName: String, val color: Color) {
    URGENT("urgent", "Urgente", PriorityUrgent),
    HIGH("high", "Alta", PriorityHigh),
    MEDIUM("medium", "Media", PriorityMedium),
    LOW("low", "Baja", PriorityLow),
    NONE("none", "Ninguna", PriorityNone);

    companion object {
        fun fromRaw(raw: String?): IssuePriority {
            return entries.firstOrNull { it.rawValue.equals(raw, ignoreCase = true) } ?: MEDIUM
        }
    }
}

enum class IssueType(val rawValue: String, val displayName: String) {
    ISSUE("issue", "Bug / Issue"),
    FEATURE("feature", "Mejora / Feature");

    companion object {
        fun fromRaw(raw: String?): IssueType {
            return entries.firstOrNull { it.rawValue.equals(raw, ignoreCase = true) } ?: ISSUE
        }
    }
}

data class Issue(
    val id: String,
    val projectId: String,
    val projectName: String = "",
    val projectKey: String = "",
    val moduleId: String? = null,
    val moduleName: String? = null,
    val code: String, // e.g. "KIN-101"
    val title: String,
    val description: String = "",
    val status: IssueStatus = IssueStatus.TODO,
    val priority: IssuePriority = IssuePriority.MEDIUM,
    val type: IssueType = IssueType.ISSUE,
    val assigneeName: String? = null,
    val assigneeEmail: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
