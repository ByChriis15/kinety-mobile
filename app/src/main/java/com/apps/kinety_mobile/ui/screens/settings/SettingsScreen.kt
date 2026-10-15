package com.apps.kinety_mobile.ui.screens.settings

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apps.kinety_mobile.data.repository.KinetyRepository
import com.apps.kinety_mobile.ui.components.ConfirmDeleteDialog
import com.apps.kinety_mobile.ui.theme.AppThemeMode
import com.apps.kinety_mobile.ui.theme.MerlotPrimary
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repository: KinetyRepository,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val userName by repository.sessionManager.userNameFlow.collectAsState(initial = "Kevin Alonso")
    val userEmail by repository.sessionManager.userEmailFlow.collectAsState(initial = "kevin@kinety.io")
    val themeMode by repository.sessionManager.themeModeFlow.collectAsState(initial = AppThemeMode.MERLOT_NOIR)
    val backendUrl by repository.sessionManager.backendUrlFlow.collectAsState(initial = "http://10.0.2.2:8080")
    val apiToken by repository.sessionManager.apiTokenFlow.collectAsState(initial = "")
    val lastProjectId by repository.sessionManager.lastProjectIdFlow.collectAsState(initial = null)

    var urlInput by remember { mutableStateOf(backendUrl) }
    var tokenInput by remember { mutableStateOf(apiToken) }
    var isSyncing by remember { mutableStateOf(false) }

    var showResetDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Text(
                text = "Ajustes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // User Profile Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("").ifEmpty { "K" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!lastProjectId.isNullOrBlank()) {
                            Text(
                                text = "Último proyecto: $lastProjectId",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Theme Preferences Section
            Column {
                Text(
                    text = "TEMA VISUAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Merlot Noir (Dark)
                    val isMerlot = themeMode == AppThemeMode.MERLOT_NOIR
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isMerlot) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (isMerlot) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clickable {
                                scope.launch { repository.sessionManager.setThemeMode(AppThemeMode.MERLOT_NOIR) }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = if (isMerlot) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Merlot Noir", fontSize = 12.sp, fontWeight = if (isMerlot) FontWeight.Bold else FontWeight.Normal, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    // Pearl Silver (Light)
                    val isPearl = themeMode == AppThemeMode.PEARL_SILVER
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isPearl) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (isPearl) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clickable {
                                scope.launch { repository.sessionManager.setThemeMode(AppThemeMode.PEARL_SILVER) }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LightMode, contentDescription = null, tint = if (isPearl) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Pearl Silver", fontSize = 12.sp, fontWeight = if (isPearl) FontWeight.Bold else FontWeight.Normal, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    // System
                    val isSystem = themeMode == AppThemeMode.SYSTEM
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSystem) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (isSystem) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clickable {
                                scope.launch { repository.sessionManager.setThemeMode(AppThemeMode.SYSTEM) }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SettingsBrightness, contentDescription = null, tint = if (isSystem) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Sistema", fontSize = 12.sp, fontWeight = if (isSystem) FontWeight.Bold else FontWeight.Normal, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // Backend Sync Section (Kinety Go API)
            Column {
                Text(
                    text = "CONEXIÓN A BACKEND KINETY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Sincroniza issues con tu backend Go (http://10.0.2.2:8080 para emulador o IP de tu PC)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("URL del Servidor Kinety") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = { tokenInput = it },
                    label = { Text("Token PAT (opcional, ej: kin_...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        isSyncing = true
                        scope.launch {
                            repository.sessionManager.saveBackendConfig(urlInput, tokenInput)
                            val result = repository.syncWithKinetyBackend(urlInput, tokenInput)
                            isSyncing = false
                            if (result.isSuccess) {
                                snackbarHostState.showSnackbar("Sincronización exitosa: ${result.getOrNull()} issues procesados")
                            } else {
                                snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Error al sincronizar")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isSyncing
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sincronizar con Kinety Backend")
                        }
                    }
                }
            }

            // Seed Data / Reset Section
            Column {
                Text(
                    text = "GESTIÓN DE DATOS LOCALES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restaurar Datos Semilla Iniciales")
                    }
                }
            }

            // Project Info Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "Kinety Mobile v1.0",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Workspace Companion for Engineering & Product Teams",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Logout Button
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar Sesión", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }

    if (showResetDialog) {
        ConfirmDeleteDialog(
            title = "Restaurar Datos Semilla",
            message = "¿Deseas restablecer los proyectos, módulos e issues de ejemplo? Tus cambios manuales serán reemplazados por el catálogo demo.",
            onConfirm = {
                scope.launch {
                    repository.resetSeedData()
                    snackbarHostState.showSnackbar("Catálogo inicial restaurado")
                }
            },
            onDismiss = { showResetDialog = false }
        )
    }

    if (showLogoutDialog) {
        ConfirmDeleteDialog(
            title = "Cerrar Sesión",
            message = "¿Deseas salir de tu cuenta? La base de datos local y proyectos se conservarán intactos.",
            onConfirm = {
                scope.launch {
                    repository.logout()
                    onLogout()
                }
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}
