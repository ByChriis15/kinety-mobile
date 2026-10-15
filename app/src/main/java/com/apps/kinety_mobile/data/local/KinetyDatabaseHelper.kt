package com.apps.kinety_mobile.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.apps.kinety_mobile.data.model.Issue
import com.apps.kinety_mobile.data.model.IssuePriority
import com.apps.kinety_mobile.data.model.IssueStatus
import com.apps.kinety_mobile.data.model.IssueType
import com.apps.kinety_mobile.data.model.Project
import com.apps.kinety_mobile.data.model.ProjectModule
import com.apps.kinety_mobile.data.model.User
import java.security.MessageDigest

class KinetyDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "kinety_mobile.db"
        const val DATABASE_VERSION = 1

        private const val TABLE_USERS = "users"
        private const val TABLE_PROJECTS = "projects"
        private const val TABLE_MODULES = "modules"
        private const val TABLE_ISSUES = "issues"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_USERS (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                initials TEXT,
                color TEXT,
                created_at INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_PROJECTS (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                key TEXT NOT NULL,
                description TEXT,
                color TEXT,
                icon TEXT,
                created_at INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_MODULES (
                id TEXT PRIMARY KEY,
                project_id TEXT NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                color TEXT,
                sort_order INTEGER,
                FOREIGN KEY (project_id) REFERENCES $TABLE_PROJECTS(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_ISSUES (
                id TEXT PRIMARY KEY,
                project_id TEXT NOT NULL,
                module_id TEXT,
                code TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT,
                status TEXT NOT NULL,
                priority TEXT NOT NULL,
                type TEXT NOT NULL,
                assignee_name TEXT,
                created_at INTEGER,
                updated_at INTEGER,
                FOREIGN KEY (project_id) REFERENCES $TABLE_PROJECTS(id) ON DELETE CASCADE,
                FOREIGN KEY (module_id) REFERENCES $TABLE_MODULES(id) ON DELETE SET NULL
            )
            """.trimIndent()
        )

        populateSeedData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ISSUES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MODULES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROJECTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun populateSeedData(db: SQLiteDatabase) {
        // Default User: Kevin Alonso
        val userValues = ContentValues().apply {
            put("id", "usr-kevin")
            put("name", "Kevin Alonso")
            put("email", "kevin@kinety.io")
            put("password_hash", hashPassword("123456"))
            put("initials", "KA")
            put("color", "#A33C59")
            put("created_at", System.currentTimeMillis())
        }
        db.insert(TABLE_USERS, null, userValues)

        // Seed Projects
        SeedData.sampleProjects.forEach { proj ->
            val cv = ContentValues().apply {
                put("id", proj.id)
                put("name", proj.name)
                put("key", proj.key)
                put("description", proj.description)
                put("color", proj.color)
                put("icon", proj.icon)
                put("created_at", proj.createdAt)
            }
            db.insert(TABLE_PROJECTS, null, cv)
        }

        // Seed Modules
        SeedData.sampleModules.forEach { mod ->
            val cv = ContentValues().apply {
                put("id", mod.id)
                put("project_id", mod.projectId)
                put("name", mod.name)
                put("description", mod.description)
                put("color", mod.color)
                put("sort_order", mod.sortOrder)
            }
            db.insert(TABLE_MODULES, null, cv)
        }

        // Seed Issues
        SeedData.sampleIssues.forEach { iss ->
            val cv = ContentValues().apply {
                put("id", iss.id)
                put("project_id", iss.projectId)
                put("module_id", iss.moduleId)
                put("code", iss.code)
                put("title", iss.title)
                put("description", iss.description)
                put("status", iss.status.rawValue)
                put("priority", iss.priority.rawValue)
                put("type", iss.type.rawValue)
                put("assignee_name", iss.assigneeName)
                put("created_at", iss.createdAt)
                put("updated_at", iss.updatedAt)
            }
            db.insert(TABLE_ISSUES, null, cv)
        }
    }

    fun resetToSeedData() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_ISSUES, null, null)
            db.delete(TABLE_MODULES, null, null)
            db.delete(TABLE_PROJECTS, null, null)
            db.delete(TABLE_USERS, null, null)
            populateSeedData(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // ==========================================
    // USER AUTHENTICATION
    // ==========================================
    fun registerUser(name: String, email: String, password: String):Result<User> {
        val db = writableDatabase
        val existing = db.rawQuery("SELECT id FROM $TABLE_USERS WHERE email = ?", arrayOf(email.trim().lowercase()))
        if (existing.count > 0) {
            existing.close()
            return Result.failure(Exception("El correo ya se encuentra registrado"))
        }
        existing.close()

        val id = "usr-" + System.currentTimeMillis()
        val initials = name.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")
            .ifEmpty { "K" }

        val values = ContentValues().apply {
            put("id", id)
            put("name", name.trim())
            put("email", email.trim().lowercase())
            put("password_hash", hashPassword(password))
            put("initials", initials)
            put("color", "#A33C59")
            put("created_at", System.currentTimeMillis())
        }

        val rowId = db.insert(TABLE_USERS, null, values)
        return if (rowId != -1L) {
            Result.success(User(id = id, name = name, email = email, initials = initials))
        } else {
            Result.failure(Exception("Error al registrar el usuario en base de datos local"))
        }
    }

    fun loginUser(email: String, password: String): User? {
        val db = readableDatabase
        val hash = hashPassword(password)
        val cursor = db.rawQuery(
            "SELECT id, name, email, initials, color FROM $TABLE_USERS WHERE email = ? AND password_hash = ?",
            arrayOf(email.trim().lowercase(), hash)
        )
        return cursor.use {
            if (it.moveToFirst()) {
                User(
                    id = it.getString(0),
                    name = it.getString(1),
                    email = it.getString(2),
                    initials = it.getString(3) ?: "",
                    color = it.getString(4) ?: "#A33C59"
                )
            } else null
        }
    }

    fun getUserById(id: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, name, email, initials, color FROM $TABLE_USERS WHERE id = ?", arrayOf(id))
        return cursor.use {
            if (it.moveToFirst()) {
                User(
                    id = it.getString(0),
                    name = it.getString(1),
                    email = it.getString(2),
                    initials = it.getString(3) ?: "",
                    color = it.getString(4) ?: "#A33C59"
                )
            } else null
        }
    }

    // ==========================================
    // PROJECTS CRUD
    // ==========================================
    fun getProjects(): List<Project> {
        val list = mutableListOf<Project>()
        val db = readableDatabase
        val query = """
            SELECT p.id, p.name, p.key, p.description, p.color, p.icon, p.created_at,
                   COUNT(i.id) as total_issues,
                   SUM(CASE WHEN i.status = 'done' THEN 1 ELSE 0 END) as done_issues
            FROM $TABLE_PROJECTS p
            LEFT JOIN $TABLE_ISSUES i ON p.id = i.project_id
            GROUP BY p.id
            ORDER BY p.name ASC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Project(
                        id = it.getString(0),
                        name = it.getString(1),
                        key = it.getString(2),
                        description = it.getString(3) ?: "",
                        color = it.getString(4) ?: "#A33C59",
                        icon = it.getString(5) ?: "folder",
                        createdAt = it.getLong(6),
                        issueCount = it.getInt(7),
                        doneCount = it.getInt(8)
                    )
                )
            }
        }
        return list
    }

    fun getProjectById(id: String): Project? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT p.id, p.name, p.key, p.description, p.color, p.icon, p.created_at,
                   COUNT(i.id) as total_issues,
                   SUM(CASE WHEN i.status = 'done' THEN 1 ELSE 0 END) as done_issues
            FROM $TABLE_PROJECTS p
            LEFT JOIN $TABLE_ISSUES i ON p.id = i.project_id
            WHERE p.id = ?
            GROUP BY p.id
            """.trimIndent(),
            arrayOf(id)
        )
        return cursor.use {
            if (it.moveToFirst()) {
                Project(
                    id = it.getString(0),
                    name = it.getString(1),
                    key = it.getString(2),
                    description = it.getString(3) ?: "",
                    color = it.getString(4) ?: "#A33C59",
                    icon = it.getString(5) ?: "folder",
                    createdAt = it.getLong(6),
                    issueCount = it.getInt(7),
                    doneCount = it.getInt(8)
                )
            } else null
        }
    }

    fun insertProject(project: Project): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", project.id)
            put("name", project.name)
            put("key", project.key.uppercase())
            put("description", project.description)
            put("color", project.color)
            put("icon", project.icon)
            put("created_at", project.createdAt)
        }
        return db.insert(TABLE_PROJECTS, null, values) != -1L
    }

    fun updateProject(project: Project): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", project.name)
            put("key", project.key.uppercase())
            put("description", project.description)
            put("color", project.color)
            put("icon", project.icon)
        }
        return db.update(TABLE_PROJECTS, values, "id = ?", arrayOf(project.id)) > 0
    }

    fun deleteProject(id: String): Boolean {
        val db = writableDatabase
        db.delete(TABLE_ISSUES, "project_id = ?", arrayOf(id))
        db.delete(TABLE_MODULES, "project_id = ?", arrayOf(id))
        return db.delete(TABLE_PROJECTS, "id = ?", arrayOf(id)) > 0
    }

    // ==========================================
    // MODULES CRUD
    // ==========================================
    fun getModulesByProject(projectId: String): List<ProjectModule> {
        val list = mutableListOf<ProjectModule>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT m.id, m.project_id, m.name, m.description, m.color, m.sort_order,
                   COUNT(i.id) as issue_count
            FROM $TABLE_MODULES m
            LEFT JOIN $TABLE_ISSUES i ON m.id = i.module_id
            WHERE m.project_id = ?
            GROUP BY m.id
            ORDER BY m.sort_order ASC, m.name ASC
            """.trimIndent(),
            arrayOf(projectId)
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    ProjectModule(
                        id = it.getString(0),
                        projectId = it.getString(1),
                        name = it.getString(2),
                        description = it.getString(3) ?: "",
                        color = it.getString(4) ?: "#6F2439",
                        sortOrder = it.getInt(5),
                        issueCount = it.getInt(6)
                    )
                )
            }
        }
        return list
    }

    fun getAllModules(): List<ProjectModule> {
        val list = mutableListOf<ProjectModule>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, project_id, name, description, color, sort_order FROM $TABLE_MODULES ORDER BY name ASC",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    ProjectModule(
                        id = it.getString(0),
                        projectId = it.getString(1),
                        name = it.getString(2),
                        description = it.getString(3) ?: "",
                        color = it.getString(4) ?: "#6F2439",
                        sortOrder = it.getInt(5)
                    )
                )
            }
        }
        return list
    }

    fun insertModule(module: ProjectModule): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", module.id)
            put("project_id", module.projectId)
            put("name", module.name)
            put("description", module.description)
            put("color", module.color)
            put("sort_order", module.sortOrder)
        }
        return db.insert(TABLE_MODULES, null, values) != -1L
    }

    fun updateModule(module: ProjectModule): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", module.name)
            put("description", module.description)
            put("color", module.color)
            put("sort_order", module.sortOrder)
        }
        return db.update(TABLE_MODULES, values, "id = ?", arrayOf(module.id)) > 0
    }

    fun deleteModule(id: String): Boolean {
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_ISSUES SET module_id = NULL WHERE module_id = ?", arrayOf(id))
        return db.delete(TABLE_MODULES, "id = ?", arrayOf(id)) > 0
    }

    // ==========================================
    // ISSUES CRUD & FILTERING
    // ==========================================
    fun getIssues(
        statusFilter: String? = null,
        projectIdFilter: String? = null,
        searchQuery: String? = null
    ): List<Issue> {
        val list = mutableListOf<Issue>()
        val db = readableDatabase

        val whereClauses = mutableListOf<String>()
        val args = mutableListOf<String>()

        if (!statusFilter.isNullOrBlank() && statusFilter != "all") {
            whereClauses.add("i.status = ?")
            args.add(statusFilter)
        }
        if (!projectIdFilter.isNullOrBlank() && projectIdFilter != "all") {
            whereClauses.add("i.project_id = ?")
            args.add(projectIdFilter)
        }
        if (!searchQuery.isNullOrBlank()) {
            whereClauses.add("(i.title LIKE ? OR i.code LIKE ? OR i.description LIKE ?)")
            val param = "%${searchQuery.trim()}%"
            args.add(param)
            args.add(param)
            args.add(param)
        }

        val whereSql = if (whereClauses.isNotEmpty()) "WHERE " + whereClauses.joinToString(" AND ") else ""

        val query = """
            SELECT i.id, i.project_id, p.name as project_name, p.key as project_key,
                   i.module_id, m.name as module_name,
                   i.code, i.title, i.description, i.status, i.priority, i.type,
                   i.assignee_name, i.created_at, i.updated_at
            FROM $TABLE_ISSUES i
            LEFT JOIN $TABLE_PROJECTS p ON i.project_id = p.id
            LEFT JOIN $TABLE_MODULES m ON i.module_id = m.id
            $whereSql
            ORDER BY 
                CASE i.priority
                    WHEN 'urgent' THEN 1
                    WHEN 'high' THEN 2
                    WHEN 'medium' THEN 3
                    WHEN 'low' THEN 4
                    ELSE 5
                END ASC,
                i.updated_at DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, if (args.isEmpty()) null else args.toTypedArray())
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToIssue(it))
            }
        }
        return list
    }

    fun getIssueById(id: String): Issue? {
        val db = readableDatabase
        val query = """
            SELECT i.id, i.project_id, p.name as project_name, p.key as project_key,
                   i.module_id, m.name as module_name,
                   i.code, i.title, i.description, i.status, i.priority, i.type,
                   i.assignee_name, i.created_at, i.updated_at
            FROM $TABLE_ISSUES i
            LEFT JOIN $TABLE_PROJECTS p ON i.project_id = p.id
            LEFT JOIN $TABLE_MODULES m ON i.module_id = m.id
            WHERE i.id = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(id))
        return cursor.use {
            if (it.moveToFirst()) cursorToIssue(it) else null
        }
    }

    private fun cursorToIssue(cursor: Cursor): Issue {
        return Issue(
            id = cursor.getString(0),
            projectId = cursor.getString(1),
            projectName = cursor.getString(2) ?: "",
            projectKey = cursor.getString(3) ?: "",
            moduleId = cursor.getString(4),
            moduleName = cursor.getString(5),
            code = cursor.getString(6),
            title = cursor.getString(7),
            description = cursor.getString(8) ?: "",
            status = IssueStatus.fromRaw(cursor.getString(9)),
            priority = IssuePriority.fromRaw(cursor.getString(10)),
            type = IssueType.fromRaw(cursor.getString(11)),
            assigneeName = cursor.getString(12),
            createdAt = cursor.getLong(13),
            updatedAt = cursor.getLong(14)
        )
    }

    fun getNextIssueCode(projectId: String): String {
        val db = readableDatabase
        val proj = getProjectById(projectId)
        val prefix = proj?.key ?: "KIN"
        val cursor = db.rawQuery(
            "SELECT COUNT(id) FROM $TABLE_ISSUES WHERE project_id = ?",
            arrayOf(projectId)
        )
        val count = cursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }
        return "$prefix-${count + 101}"
    }

    fun insertIssue(issue: Issue): Boolean {
        val db = writableDatabase
        val code = if (issue.code.isBlank()) getNextIssueCode(issue.projectId) else issue.code
        val values = ContentValues().apply {
            put("id", issue.id)
            put("project_id", issue.projectId)
            put("module_id", issue.moduleId)
            put("code", code)
            put("title", issue.title)
            put("description", issue.description)
            put("status", issue.status.rawValue)
            put("priority", issue.priority.rawValue)
            put("type", issue.type.rawValue)
            put("assignee_name", issue.assigneeName)
            put("created_at", issue.createdAt)
            put("updated_at", System.currentTimeMillis())
        }
        return db.insert(TABLE_ISSUES, null, values) != -1L
    }

    fun updateIssue(issue: Issue): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("project_id", issue.projectId)
            put("module_id", issue.moduleId)
            put("title", issue.title)
            put("description", issue.description)
            put("status", issue.status.rawValue)
            put("priority", issue.priority.rawValue)
            put("type", issue.type.rawValue)
            put("assignee_name", issue.assigneeName)
            put("updated_at", System.currentTimeMillis())
        }
        return db.update(TABLE_ISSUES, values, "id = ?", arrayOf(issue.id)) > 0
    }

    fun updateIssueStatus(issueId: String, newStatus: IssueStatus): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("status", newStatus.rawValue)
            put("updated_at", System.currentTimeMillis())
        }
        return db.update(TABLE_ISSUES, values, "id = ?", arrayOf(issueId)) > 0
    }

    fun deleteIssue(id: String): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_ISSUES, "id = ?", arrayOf(id)) > 0
    }
}
