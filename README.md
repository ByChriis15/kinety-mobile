# 📱 Kinety Mobile — Native Android Workspace Companion

<div align="center">

![Kinety Banner](https://img.shields.io/badge/KINETY-MOBILE%20WORKSPACE-A33C59?style=for-the-badge&logoColor=white)

**El visor ligero, ergonómico y nativo para la gestión de proyectos e issues de Kinety.**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2%2B-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android%20API%2031%2B-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2F%20Material3-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Repository-orange?style=flat-square)](https://developer.android.com/topic/architecture)
[![Storage](https://img.shields.io/badge/Local%20DB-SQLite%20%2B%20DataStore-003B57?style=flat-square&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

[Características](#-características-principales) •
[Diseño Visual](#-identidad-visual-kinety) •
[Arquitectura](#-arquitectura-y-tecnologías) •
[Instalación](#-instalación-y-ejecución) •
[Contexto Académico](#-contexto-académico)

---

</div>

## 💡 ¿Qué es Kinety Mobile?

**Kinety Mobile** es una aplicación nativa para Android concebida para vivir a la par de la plataforma web **Kinety**. Su propósito es permitir que los desarrolladores y líderes técnicos consulten en el teléfono el estado de su trabajo (**Proyectos $\rightarrow$ Módulos $\rightarrow$ Issues**) en un formato optimizado de listas y detalle, inspirado en la agilidad de herramientas como *Linear* y *GitHub Mobile*.

La experiencia se centra en **usabilidad con una sola mano y rapidez fuera del escritorio**: entrar, revisar actividades pendientes, cambiar el estado de un issue en 1 toque y registrar hallazgos al instante con persistencia local garantizada.

---

## 🚀 Características Principales

| Módulo | Funcionalidades |
| :--- | :--- |
| 🛡️ **Autenticación & Sesión** | • **Splash Screen animada** con comprobación de sesión reactiva.<br>• Registro e inicio de sesión local seguro con hash SHA-256.<br>• Persistencia con **Jetpack DataStore** (recuerda sesión activa).<br>• Acceso rápido demo en 1 clic para evaluaciones. |
| 📋 **Feed de Mis Issues** | • Lista fluida de issues con estado y prioridad claramente diferenciados.<br>• **Filtro rápido horizontal por estado:** *Todos, En Progreso, Por Hacer, Backlog, Hecho, Cancelado*.<br>• Buscador en tiempo real por código (`KIN-101`), título o descripción.<br>• **FAB ergonómico (+)** para captura rápida de issues. |
| 🔍 **Detalle e Interacción** | • **Cambio rápido de estado en 1 toque** mediante selector modal emergente.<br>• Visualización de proyecto, módulo, prioridad, tipo y encargado.<br>• Acciones completas de edición y eliminación con confirmación. |
| ✍️ **Crear y Editar Issues** | • Selectores desplegables dinámicos de **Proyecto** y **Módulo**.<br>• Generador automático de prefijos/claves según el proyecto (`MOB-101`, `KIN-205`).<br>• Asignación de prioridad (*Urgente, Alta, Media, Baja*) y tipo (*Bug/Issue* o *Mejora/Feature*). |
| 📁 **Catálogo de Proyectos** | • Lista de proyectos activos con **barra de progreso visual** (% completado).<br>• Creación y eliminación de proyectos maestros.<br>• Vista de detalle con navegación por pestañas: **Issues del proyecto** y **Módulos asociados**. |
| 🧩 **Gestión de Módulos** | • CRUD de módulos asignados a proyectos específicos (ej: *Autenticación*, *Cost Intelligence*, *UI*). |
| ⚙️ **Ajustes & Conectividad** | • **Cambio de tema en tiempo real:** *Merlot Noir* (Oscuro), *Pearl Silver* (Claro) o *Sistema*.<br>• **Sincronización opcional con Backend en Go:** Conexión con endpoints REST (`/api/me/issues`) usando token PAT `kin_...`.<br>• **Restauración de Datos Semilla (Seed Data):** Recarga instantánea de catálogo de ejemplo. |

---

## 🎨 Identidad Visual Kinety

El diseño de la aplicación replica fielmente la identidad estética de **Kinety**, adaptada a la ergonomía de pantalla táctil:

```
┌─────────────────────────────────────────────────────────┐
│  🍷 TEMA MERLOT NOIR (Oscuro por Defecto)               │
│  • Fondo Principal: #0C0A0A (Obsidiana profundo)        │
│  • Superficies y Cards: #1D1715 / #151110 (Carbón)      │
│  • Acento de Marca: #A33C59 / #6F2439 (Kinety Wine)    │
│  • Bordes Sutiles: rgba(245, 236, 231, 0.12)           │
├─────────────────────────────────────────────────────────┤
│  ⚪ TEMA PEARL SILVER (Modo Claro)                       │
│  • Fondo Principal: #F4F2EE (Perla suave)               │
│  • Superficies y Cards: #FFFFFF (Blanco puro)           │
│  • Acento de Marca: #7C2941 (Merlot Satinado)           │
├─────────────────────────────────────────────────────────┤
│  💊 FLOATING CAPSULE NAVIGATION                         │
│  Barra de navegación flotante estilo cápsula anclada    │
│  en la parte inferior con micro-skeuomorfismo y badge.  │
└─────────────────────────────────────────────────────────┘
```

---

## 🏗️ Arquitectura y Tecnologías

Kinety Mobile está estructurado bajo los principios de **Clean Architecture** y el patrón **MVVM (Model-View-ViewModel)**:

```
app/src/main/java/com/apps/kinety_mobile/
│
├── data/
│   ├── local/
│   │   ├── KinetyDatabaseHelper.kt   # SQLite OpenHelper (tablas users, projects, modules, issues)
│   │   ├── UserSessionManager.kt     # Jetpack DataStore Preferences (sesión, tema, último proyecto)
│   │   └── SeedData.kt               # Catálogo de ejemplo precargado (proyectos, módulos, issues)
│   ├── model/
│   │   ├── User.kt                   # Modelo de usuario local
│   │   ├── Project.kt                # Modelo de proyecto con estadísticas
│   │   ├── ProjectModule.kt          # Modelo de módulos por proyecto
│   │   └── Issue.kt                  # Modelo de issue con enums IssueStatus e IssuePriority
│   └── repository/
│       └── KinetyRepository.kt      # Repositorio central con StateFlows y sync HTTP con Backend Go
│
├── ui/
│   ├── components/
│   │   ├── IssueCard.kt              # Tarjeta de issue táctil y accesible
│   │   ├── KinetyBottomNav.kt        # Barra cápsula flotante animada
│   │   ├── KinetyDialogs.kt          # Modales de cambio de estado, creación y confirmación
│   │   ├── PriorityBadge.kt          # Chips de prioridad con código semántico
│   │   ├── ProjectCard.kt            # Tarjeta de proyecto con barra de avance
│   │   └── StatusChip.kt             # Chip semántico de estado interactivo
│   ├── navigation/
│   │   └── Screen.kt                 # Rutas fuertemente tipadas para Navigation Compose
│   ├── screens/
│   │   ├── splash/SplashScreen.kt    # Pantalla de bienvenida y verificación de sesión
│   │   ├── auth/AuthScreen.kt        # Login, registro y botón Demo
│   │   ├── feed/FeedScreen.kt        # Feed de Mis Issues con filtros y buscador
│   │   ├── issues/
│   │   │   ├── IssueDetailScreen.kt  # Detalle, cambio rápido de estado y acciones
│   │   │   └── UpsertIssueScreen.kt  # Formulario de creación y edición
│   │   ├── projects/
│   │   │   ├── ProjectsScreen.kt     # Listado general de proyectos
│   │   │   └── ProjectDetailScreen.kt# Detalle de proyecto (Módulos + Issues)
│   │   └── settings/SettingsScreen.kt# Ajustes de tema, sync con backend y logout
│   └── theme/
│       ├── Color.kt                  # Paleta oficial Merlot Noir y Pearl Silver
│       ├── Theme.kt                  # KinetyTheme reactivo al modo seleccionado
│       └── Type.kt                   # Tipografía Inter / Roboto
│
├── KinetyApp.kt                      # Orquestador raíz (NavHost + BottomBar)
└── MainActivity.kt                   # Punto de entrada de la actividad Android
```

---

## ⚙️ Instalación y Ejecución

### Prerrequisitos
* **Android Studio** Ladybug (o superior).
* **JDK 17** o superior (compatible con Java 25).
* Dispositivo físico o Emulador Android con **API 31+** (Android 12 a 15).

### Pasos
1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/ByChriis15/kinety-mobile.git
   cd kinety-mobile
   ```
2. **Abrir en Android Studio:**
   Selecciona la carpeta clonada y espera que Gradle sincronice las dependencias automáticamente.
3. **Compilar desde terminal (Opcional):**
   ```bash
   ./gradlew assembleDebug
   ```
4. **Ejecutar en el dispositivo:**
   Presiona `Shift + F10` o el botón **Run** ▶️ en Android Studio.

> [!TIP]
> **Credenciales de prueba rápida:**
> Al abrir la app por primera vez, puedes hacer clic en **"Acceso Rápido Demo (Kevin Alonso)"** en la pantalla de Login para acceder inmediatamente con datos de muestra ya cargados.

---

## 🔗 Integración con Kinety Backend (Go)

Kinety Mobile está diseñado bajo el principio **Offline-First**, funcionando de manera 100% autónoma. Sin embargo, cuenta con sincronización opcional con el backend real de Kinety:

* **URL por defecto en emulador:** `http://10.0.2.2:8080`
* **URL en dispositivo físico:** `http://<IP_LOCAL_DE_TU_PC>:8080`
* **Autenticación soportada:** Personal Access Tokens (`kin_...`).
* **Endpoints consumidos:** `GET /api/me/issues`, `GET /api/projects`, `GET /api/projects/{id}/modules`.

---

## 🎓 Contexto Académico

* **Institución:** Universidad Laica "Eloy Alfaro" de Manabí (ULEAM)
* **Facultad:** Ciencias de la Vida y Tecnologías
* **Carrera:** Ingeniería de Software — 6to Nivel "A"
* **Asignatura:** Desarrollo de Aplicaciones Móviles Nativas
* **Docente:** Ing. Edgardo Panchana Flores
* **Autor:** Kevin Joel Alonso Bailón
* **Periodo:** Septiembre 2026 – Febrero 2027

---

<div align="center">

Desarrollado con ❤️ y Kotlin para **Kinety**.

</div>
