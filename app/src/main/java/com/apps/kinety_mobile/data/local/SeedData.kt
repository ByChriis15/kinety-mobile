package com.apps.kinety_mobile.data.local

import com.apps.kinety_mobile.data.model.Issue
import com.apps.kinety_mobile.data.model.IssuePriority
import com.apps.kinety_mobile.data.model.IssueStatus
import com.apps.kinety_mobile.data.model.IssueType
import com.apps.kinety_mobile.data.model.Project
import com.apps.kinety_mobile.data.model.ProjectModule

object SeedData {
    val sampleProjects = listOf(
        Project(
            id = "proj-kinety-core",
            name = "Kinety Core Platform",
            key = "KIN",
            description = "Plataforma central de gestión de proyectos, issues e inteligencia de costos.",
            color = "#A33C59",
            icon = "layers"
        ),
        Project(
            id = "proj-kinety-mobile",
            name = "Kinety Mobile & PWA",
            key = "MOB",
            description = "Compañero nativo para consulta rápida y registro en ruta con una sola mano.",
            color = "#3E63DD",
            icon = "smartphone"
        ),
        Project(
            id = "proj-carion-ai",
            name = "Carion AI Assistant",
            key = "CAR",
            description = "Motor de asistencia contextual para triaging automático y sugerencias de código.",
            color = "#30A46C",
            icon = "sparkles"
        )
    )

    val sampleModules = listOf(
        // Kinety Core
        ProjectModule(
            id = "mod-core-auth",
            projectId = "proj-kinety-core",
            name = "Autenticación & Sesión",
            description = "Gestión de usuarios, tokens PAT, Clerk y permisos de espacio de trabajo.",
            color = "#A33C59",
            sortOrder = 1
        ),
        ProjectModule(
            id = "mod-core-issues",
            projectId = "proj-kinety-core",
            name = "Seguimiento de Issues",
            description = "Motor de ciclo de vida de issues, estados, prioridades y asignaciones.",
            color = "#6F2439",
            sortOrder = 2
        ),
        ProjectModule(
            id = "mod-core-cost",
            projectId = "proj-kinety-core",
            name = "Cost Intelligence",
            description = "Métricas de coste estimado por ciclo, tarifas horarias y desgloses financieros.",
            color = "#C07A5B",
            sortOrder = 3
        ),

        // Kinety Mobile
        ProjectModule(
            id = "mod-mob-offline",
            projectId = "proj-kinety-mobile",
            name = "Persistencia Local & Cache",
            description = "Almacenamiento local SQLite y DataStore para uso sin conexión.",
            color = "#3E63DD",
            sortOrder = 1
        ),
        ProjectModule(
            id = "mod-mob-ui",
            projectId = "proj-kinety-mobile",
            name = "Diseño Merlot & Pearl",
            description = "Componentes Jetpack Compose con micro-skeuomorfismo y ergonomía móvil.",
            color = "#8E8D90",
            sortOrder = 2
        ),

        // Carion AI
        ProjectModule(
            id = "mod-car-triage",
            projectId = "proj-carion-ai",
            name = "Triaging Automático",
            description = "Clasificación de prioridad y asignación sugerida por modelo LLM.",
            color = "#30A46C",
            sortOrder = 1
        )
    )

    val sampleIssues = listOf(
        Issue(
            id = "iss-101",
            projectId = "proj-kinety-mobile",
            projectName = "Kinety Mobile & PWA",
            projectKey = "MOB",
            moduleId = "mod-mob-ui",
            moduleName = "Diseño Merlot & Pearl",
            code = "MOB-101",
            title = "Ajustar contraste de badges de prioridad en Merlot Noir",
            description = "Garantizar que los chips Urgente (#E5484D) y Alta (#FF8533) tengan legibilidad óptima sobre fondo carbón #0C0A0A.",
            status = IssueStatus.IN_PROGRESS,
            priority = IssuePriority.URGENT,
            type = IssueType.ISSUE,
            assigneeName = "Kevin Alonso"
        ),
        Issue(
            id = "iss-102",
            projectId = "proj-kinety-mobile",
            projectName = "Kinety Mobile & PWA",
            projectKey = "MOB",
            moduleId = "mod-mob-offline",
            moduleName = "Persistencia Local & Cache",
            code = "MOB-102",
            title = "Persistencia de sesión en Splash con DataStore",
            description = "Validar token y flag 'recordarme' en el arranque de la app para redirigir directamente al Feed si la sesión sigue activa.",
            status = IssueStatus.DONE,
            priority = IssuePriority.HIGH,
            type = IssueType.FEATURE,
            assigneeName = "Kevin Alonso"
        ),
        Issue(
            id = "iss-103",
            projectId = "proj-kinety-core",
            projectName = "Kinety Core Platform",
            projectKey = "KIN",
            moduleId = "mod-core-issues",
            moduleName = "Seguimiento de Issues",
            code = "KIN-204",
            title = "Transición fluida de estados desde el detalle",
            description = "Permitir cambiar de 'Por hacer' a 'En progreso' y 'Hecho' con un solo toque desde la vista rápida del issue.",
            status = IssueStatus.TODO,
            priority = IssuePriority.MEDIUM,
            type = IssueType.FEATURE,
            assigneeName = "Michael Vinces"
        ),
        Issue(
            id = "iss-104",
            projectId = "proj-kinety-core",
            projectName = "Kinety Core Platform",
            projectKey = "KIN",
            moduleId = "mod-core-auth",
            moduleName = "Autenticación & Sesión",
            code = "KIN-205",
            title = "Validar expiración de tokens PAT kin_ en Go backend",
            description = "Comprobar que cuando un Personal Access Token expira, el middleware devuelva 401 con JSON estructurado.",
            status = IssueStatus.BACKLOG,
            priority = IssuePriority.LOW,
            type = IssueType.ISSUE,
            assigneeName = "Jonathan Marmolejo"
        ),
        Issue(
            id = "iss-105",
            projectId = "proj-carion-ai",
            projectName = "Carion AI Assistant",
            projectKey = "CAR",
            moduleId = "mod-car-triage",
            moduleName = "Triaging Automático",
            code = "CAR-301",
            title = "Sugerencia de módulo basada en palabras clave del título",
            description = "Analizar el título del nuevo issue para seleccionar automáticamente el módulo más probable (ej: 'Login' -> Auth).",
            status = IssueStatus.BACKLOG,
            priority = IssuePriority.MEDIUM,
            type = IssueType.FEATURE,
            assigneeName = "Michael Vinces"
        )
    )
}
