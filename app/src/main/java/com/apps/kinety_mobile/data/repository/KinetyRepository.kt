package com.apps.kinety_mobile.data.repository

import android.content.Context
import com.apps.kinety_mobile.data.local.KinetyDatabaseHelper
import com.apps.kinety_mobile.data.local.UserSessionManager
import com.apps.kinety_mobile.data.model.Issue
import com.apps.kinety_mobile.data.model.IssuePriority
import com.apps.kinety_mobile.data.model.IssueStatus
import com.apps.kinety_mobile.data.model.IssueType
import com.apps.kinety_mobile.data.model.Project
import com.apps.kinety_mobile.data.model.ProjectModule
import com.apps.kinety_mobile.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class KinetyRepository(context: Context) {

    private val dbHelper = KinetyDatabaseHelper(context.applicationContext)
    val sessionManager = UserSessionManager(context.applicationContext)

    private val _issuesFlow = MutableStateFlow<List<Issue>>(emptyList())
    val issuesFlow: StateFlow<List<Issue>> = _issuesFlow.asStateFlow()

    private val _projectsFlow = MutableStateFlow<List<Project>>(emptyList())
    val projectsFlow: StateFlow<List<Project>> = _projectsFlow.asStateFlow()

    private var currentStatusFilter: String? = null
    private var currentProjectFilter: String? = null
    private var currentSearchQuery: String? = null

    suspend fun refreshAll() = withContext(Dispatchers.IO) {
        refreshProjects()
        refreshIssues(currentStatusFilter, currentProjectFilter, currentSearchQuery)
    }

    suspend fun refreshProjects() = withContext(Dispatchers.IO) {
        val list = dbHelper.getProjects()
        _projectsFlow.value = list
    }

    suspend fun refreshIssues(
        statusFilter: String? = currentStatusFilter,
        projectIdFilter: String? = currentProjectFilter,
        searchQuery: String? = currentSearchQuery
    ) = withContext(Dispatchers.IO) {
        currentStatusFilter = statusFilter
        currentProjectFilter = projectIdFilter
        currentSearchQuery = searchQuery
        val list = dbHelper.getIssues(statusFilter, projectIdFilter, searchQuery)
        _issuesFlow.value = list
    }

    suspend fun getIssueById(id: String): Issue? = withContext(Dispatchers.IO) {
        dbHelper.getIssueById(id)
    }

    suspend fun createIssue(issue: Issue): Boolean = withContext(Dispatchers.IO) {
        val success = dbHelper.insertIssue(issue)
        if (success) {
            refreshAll()
        }
        success
    }

    suspend fun updateIssue(issue: Issue): Boolean = withContext(Dispatchers.IO) {
        val success = dbHelper.updateIssue(issue)
        if (success) {
            refreshAll()
        }
        success
    }

    suspend fun updateIssueStatus(issueId: String, newStatus: IssueStatus): Boolean = withContext(Dispatchers.IO) {
        val success = dbHelper.updateIssueStatus(issueId, newStatus)
        if (success) {
            refreshAll()
        }
        success
    }

    suspend fun deleteIssue(id: String): Boolean = withContext(Dispatchers.IO) {
        val success = dbHelper.deleteIssue(id)
        if (success) {
            refreshAll()
        }
        success
    }

    // ==========================================
    // PROJECTS & MODULES
    // ==========================================
    suspend fun getProjectById(id: String): Project? = withContext(Dispatchers.IO) {
        dbHelper.getProjectById(id)
    }

    suspend fun createProject(project: Project): Boolean = withContext(Dispatchers.IO) {
        val success = dbHelper.insertProject(project)
        if (success) refreshProjects()
        success
    }

    suspend fun updateProject(project: Project): Boolean = withContext(Dispatchers.IO) {
        val success = dbHelper.updateProject(project)
        if (success) refreshProjects()
        success
    }

    suspend fun deleteProject(id: String): Boolean = withContext(Dispatchers.IO) {
        val success = dbHelper.deleteProject(id)
        if (success) refreshAll()
        success
    }

    suspend fun getModulesForProject(projectId: String): List<ProjectModule> = withContext(Dispatchers.IO) {
        dbHelper.getModulesByProject(projectId)
    }

    suspend fun getAllModules(): List<ProjectModule> = withContext(Dispatchers.IO) {
        dbHelper.getAllModules()
    }

    suspend fun createModule(module: ProjectModule): Boolean = withContext(Dispatchers.IO) {
        dbHelper.insertModule(module)
    }

    suspend fun updateModule(module: ProjectModule): Boolean = withContext(Dispatchers.IO) {
        dbHelper.updateModule(module)
    }

    suspend fun deleteModule(id: String): Boolean = withContext(Dispatchers.IO) {
        val success = dbHelper.deleteModule(id)
        if (success) refreshAll()
        success
    }

    fun getNextIssueCode(projectId: String): String {
        return dbHelper.getNextIssueCode(projectId)
    }

    // ==========================================
    // AUTH & LOCAL USERS
    // ==========================================
    suspend fun login(email: String, pass: String, rememberMe: Boolean): Result<User> = withContext(Dispatchers.IO) {
        val user = dbHelper.loginUser(email, pass)
        if (user != null) {
            sessionManager.saveSession(user.id, user.name, user.email, rememberMe)
            Result.success(user)
        } else {
            Result.failure(Exception("Credenciales incorrectas"))
        }
    }

    suspend fun register(name: String, email: String, pass: String): Result<User> = withContext(Dispatchers.IO) {
        val result = dbHelper.registerUser(name, email, pass)
        if (result.isSuccess) {
            val user = result.getOrThrow()
            sessionManager.saveSession(user.id, user.name, user.email, true)
        }
        result
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        sessionManager.clearSession()
    }

    suspend fun resetSeedData() = withContext(Dispatchers.IO) {
        dbHelper.resetToSeedData()
        refreshAll()
    }

    // ==========================================
    // OPTIONAL REMOTE KINETY GO BACKEND SYNC
    // ==========================================
    suspend fun syncWithKinetyBackend(baseUrl: String, patToken: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val cleanUrl = baseUrl.trim().removeSuffix("/")
            val url = URL("$cleanUrl/api/me/issues")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.setRequestProperty("Accept", "application/json")
            if (patToken.isNotBlank()) {
                conn.setRequestProperty("Authorization", "Bearer ${patToken.trim()}")
            }

            val code = conn.responseCode
            if (code in 200..299) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonStr)
                var imported = 0

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.optString("id", "iss-rem-$i")
                    val projectId = obj.optString("project_id", "proj-kinety-core")
                    val issueCode = obj.optString("code", "KIN-${100 + i}")
                    val title = obj.optString("title", "Issue Remoto")
                    val desc = obj.optString("description", "")
                    val status = IssueStatus.fromRaw(obj.optString("status", "todo"))
                    val priority = IssuePriority.fromRaw(obj.optString("priority", "medium"))
                    val type = IssueType.fromRaw(obj.optString("type", "issue"))

                    val existing = dbHelper.getIssueById(id)
                    val issue = Issue(
                        id = id,
                        projectId = projectId,
                        code = issueCode,
                        title = title,
                        description = desc,
                        status = status,
                        priority = priority,
                        type = type,
                        updatedAt = System.currentTimeMillis()
                    )

                    if (existing == null) {
                        dbHelper.insertIssue(issue)
                    } else {
                        dbHelper.updateIssue(issue)
                    }
                    imported++
                }

                refreshAll()
                Result.success(imported)
            } else {
                Result.failure(Exception("Error de servidor Kinety: Código HTTP $code"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("No se pudo conectar al backend de Kinety: ${e.message}"))
        }
    }
}
