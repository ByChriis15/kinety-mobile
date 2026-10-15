package com.apps.kinety_mobile.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Feed : Screen("feed")
    object Projects : Screen("projects")
    object ProjectDetail : Screen("project_detail/{projectId}") {
        fun createRoute(projectId: String) = "project_detail/$projectId"
    }
    object IssueDetail : Screen("issue_detail/{issueId}") {
        fun createRoute(issueId: String) = "issue_detail/$issueId"
    }
    object UpsertIssue : Screen("upsert_issue?issueId={issueId}&projectId={projectId}") {
        fun createRoute(issueId: String? = null, projectId: String? = null): String {
            val i = issueId ?: ""
            val p = projectId ?: ""
            return "upsert_issue?issueId=$i&projectId=$p"
        }
    }
    object Settings : Screen("settings")
}
