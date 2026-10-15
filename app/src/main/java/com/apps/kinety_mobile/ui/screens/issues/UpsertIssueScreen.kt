package com.apps.kinety_mobile.ui.screens.issues

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apps.kinety_mobile.data.model.Issue
import com.apps.kinety_mobile.data.model.IssuePriority
import com.apps.kinety_mobile.data.model.IssueStatus
import com.apps.kinety_mobile.data.model.IssueType
import com.apps.kinety_mobile.data.model.Project
import com.apps.kinety_mobile.data.model.ProjectModule
import com.apps.kinety_mobile.data.repository.KinetyRepository
import com.apps.kinety_mobile.ui.components.PriorityBadge
import com.apps.kinety_mobile.ui.components.StatusChip
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsertIssueScreen(
    issueId: String?,
    initialProjectId: String?,
    repository: KinetyRepository,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val isEditMode = !issueId.isNullOrBlank()

    val projects by repository.projectsFlow.collectAsState()
    var modules by remember { mutableStateOf<List<ProjectModule>>(emptyList()) }

    var selectedProject by remember { mutableStateOf<Project?>(null) }
    var selectedModule by remember { mutableStateOf<ProjectModule?>(null) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(IssuePriority.MEDIUM) }
    var status by remember { mutableStateOf(IssueStatus.TODO) }
    var type by remember { mutableStateOf(IssueType.ISSUE) }
    var assigneeName by remember { mutableStateOf("Kevin Alonso") }
    var code by remember { mutableStateOf("") }

    var isProjectDropdownExpanded by remember { mutableStateOf(false) }
    var isModuleDropdownExpanded by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(issueId) {
        repository.refreshProjects()
        if (isEditMode) {
            val existing = repository.getIssueById(issueId!!)
            if (existing != null) {
                title = existing.title
                description = existing.description
                priority = existing.priority
                status = existing.status
                type = existing.type
                assigneeName = existing.assigneeName ?: "Kevin Alonso"
                code = existing.code

                selectedProject = projects.firstOrNull { it.id == existing.projectId }
                    ?: repository.getProjectById(existing.projectId)
                if (selectedProject != null) {
                    modules = repository.getModulesForProject(selectedProject!!.id)
                    selectedModule = modules.firstOrNull { it.id == existing.moduleId }
                }
            }
        } else {
            // New Issue
            selectedProject = if (!initialProjectId.isNullOrBlank()) {
                projects.firstOrNull { it.id == initialProjectId }
            } else {
                projects.firstOrNull()
            }
            if (selectedProject != null) {
                modules = repository.getModulesForProject(selectedProject!!.id)
                selectedModule = modules.firstOrNull()
            }
        }
        isLoading = false
    }

    // Refresh modules whenever project changes
    fun onProjectChanged(proj: Project) {
        selectedProject = proj
        scope.launch {
            modules = repository.getModulesForProject(proj.id)
            selectedModule = modules.firstOrNull()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Editar Issue" else "Nuevo Issue",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isBlank()) {
                                errorMessage = "El título es obligatorio"
                                return@IconButton
                            }
                            if (selectedProject == null) {
                                errorMessage = "Debes seleccionar un proyecto"
                                return@IconButton
                            }

                            scope.launch {
                                val targetId = if (isEditMode) issueId!! else "iss-" + System.currentTimeMillis()
                                val issue = Issue(
                                    id = targetId,
                                    projectId = selectedProject!!.id,
                                    projectName = selectedProject!!.name,
                                    projectKey = selectedProject!!.key,
                                    moduleId = selectedModule?.id,
                                    moduleName = selectedModule?.name,
                                    code = code,
                                    title = title.trim(),
                                    description = description.trim(),
                                    status = status,
                                    priority = priority,
                                    type = type,
                                    assigneeName = assigneeName.trim()
                                )

                                if (isEditMode) {
                                    repository.updateIssue(issue)
                                } else {
                                    repository.createIssue(issue)
                                }
                                onSaved()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Guardar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Project Selector
                Column {
                    Text(
                        text = "PROYECTO *",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                .clickable { isProjectDropdownExpanded = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedProject?.let { "${it.name} [${it.key}]" } ?: "Seleccionar Proyecto",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        DropdownMenu(
                            expanded = isProjectDropdownExpanded,
                            onDismissRequest = { isProjectDropdownExpanded = false }
                        ) {
                            projects.forEach { proj ->
                                DropdownMenuItem(
                                    text = { Text("${proj.name} [${proj.key}]") },
                                    onClick = {
                                        onProjectChanged(proj)
                                        isProjectDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Module Selector
                Column {
                    Text(
                        text = "MÓDULO (Opcional)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                .clickable { isModuleDropdownExpanded = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedModule?.name ?: if (modules.isEmpty()) "Sin módulos disponibles" else "Ninguno",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        DropdownMenu(
                            expanded = isModuleDropdownExpanded,
                            onDismissRequest = { isModuleDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Ninguno / General") },
                                onClick = {
                                    selectedModule = null
                                    isModuleDropdownExpanded = false
                                }
                            )
                            modules.forEach { mod ->
                                DropdownMenuItem(
                                    text = { Text(mod.name) },
                                    onClick = {
                                        selectedModule = mod
                                        isModuleDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Título del issue *") },
                    placeholder = { Text("Ej: Resolver error al guardar sesión en splash") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    placeholder = { Text("Detalla el comportamiento esperado, pasos para reproducir...") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Priority Selection Chips
                Column {
                    Text(
                        text = "PRIORIDAD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(IssuePriority.URGENT, IssuePriority.HIGH, IssuePriority.MEDIUM, IssuePriority.LOW).forEach { prio ->
                            val isSelected = priority == prio
                            val bgColor = if (isSelected) prio.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                            val borderColor = if (isSelected) prio.color else MaterialTheme.colorScheme.outline

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor)
                                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                    .clickable { priority = prio }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = prio.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) prio.color else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Status Selection Chips
                Column {
                    Text(
                        text = "ESTADO INICIAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(IssueStatus.BACKLOG, IssueStatus.TODO, IssueStatus.IN_PROGRESS, IssueStatus.DONE).forEach { st ->
                            val isSelected = status == st
                            val bgColor = if (isSelected) st.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                            val borderColor = if (isSelected) st.color else MaterialTheme.colorScheme.outline

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor)
                                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                    .clickable { status = st }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = st.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) st.color else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Assignee
                OutlinedTextField(
                    value = assigneeName,
                    onValueChange = { assigneeName = it },
                    label = { Text("Asignado a") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Save Button
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            errorMessage = "El título es obligatorio"
                            return@Button
                        }
                        if (selectedProject == null) {
                            errorMessage = "Debes seleccionar un proyecto"
                            return@Button
                        }

                        scope.launch {
                            val targetId = if (isEditMode) issueId!! else "iss-" + System.currentTimeMillis()
                            val issue = Issue(
                                id = targetId,
                                projectId = selectedProject!!.id,
                                projectName = selectedProject!!.name,
                                projectKey = selectedProject!!.key,
                                moduleId = selectedModule?.id,
                                moduleName = selectedModule?.name,
                                code = code,
                                title = title.trim(),
                                description = description.trim(),
                                status = status,
                                priority = priority,
                                type = type,
                                assigneeName = assigneeName.trim()
                            )

                            if (isEditMode) {
                                repository.updateIssue(issue)
                            } else {
                                repository.createIssue(issue)
                            }
                            onSaved()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (isEditMode) "Actualizar Issue" else "Crear Issue",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
