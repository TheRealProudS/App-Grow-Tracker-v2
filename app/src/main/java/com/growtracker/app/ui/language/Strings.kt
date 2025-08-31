package com.growtracker.app.ui.language

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

object Strings {
    
    // Navigation
    val navigation_overview: Map<Language, String> = mapOf(
        Language.GERMAN to "Übersicht",
        Language.ENGLISH to "Overview",
        Language.SPANISH to "Resumen",
        Language.FRENCH to "Aperçu",
        Language.ITALIAN to "Panoramica"
    )
    
    val navigation_grow: Map<Language, String> = mapOf(
        Language.GERMAN to "Grow",
        Language.ENGLISH to "Grow",
        Language.SPANISH to "Cultivo",
        Language.FRENCH to "Culture",
        Language.ITALIAN to "Coltivo"
    )
    
    val navigation_settings: Map<Language, String> = mapOf(
        Language.GERMAN to "Einstellungen",
        Language.ENGLISH to "Settings",
        Language.SPANISH to "Configuración",
        Language.FRENCH to "Paramètres",
        Language.ITALIAN to "Impostazioni"
    )
    
    // Settings Screen
    val settings_title: Map<Language, String> = mapOf(
        Language.GERMAN to "Einstellungen",
        Language.ENGLISH to "Settings",
        Language.SPANISH to "Configuración",
        Language.FRENCH to "Paramètres",
        Language.ITALIAN to "Impostazioni"
    )
    
    val settings_design_display: Map<Language, String> = mapOf(
        Language.GERMAN to "Design & Darstellung",
        Language.ENGLISH to "Design & Display",
        Language.SPANISH to "Diseño y Pantalla",
        Language.FRENCH to "Design et Affichage",
        Language.ITALIAN to "Design e Display"
    )
    
    val settings_dark_mode: Map<Language, String> = mapOf(
        Language.GERMAN to "Dark Mode",
        Language.ENGLISH to "Dark Mode",
        Language.SPANISH to "Modo Oscuro",
        Language.FRENCH to "Mode Sombre",
        Language.ITALIAN to "Modalità Scura"
    )
    
    val settings_dark_mode_subtitle: Map<Language, String> = mapOf(
        Language.GERMAN to "Dunkles Design aktivieren",
        Language.ENGLISH to "Enable dark design",
        Language.SPANISH to "Activar diseño oscuro",
        Language.FRENCH to "Activer le design sombre",
        Language.ITALIAN to "Attiva design scuro"
    )
    
    val settings_notifications: Map<Language, String> = mapOf(
        Language.GERMAN to "Benachrichtigungen",
        Language.ENGLISH to "Notifications",
        Language.SPANISH to "Notificaciones",
        Language.FRENCH to "Notifications",
        Language.ITALIAN to "Notifiche"
    )
    
    val settings_push_notifications: Map<Language, String> = mapOf(
        Language.GERMAN to "Push-Benachrichtigungen",
        Language.ENGLISH to "Push Notifications",
        Language.SPANISH to "Notificaciones Push",
        Language.FRENCH to "Notifications Push",
        Language.ITALIAN to "Notifiche Push"
    )
    
    val settings_push_notifications_subtitle: Map<Language, String> = mapOf(
        Language.GERMAN to "Erinnerungen und Updates erhalten",
        Language.ENGLISH to "Receive reminders and updates",
        Language.SPANISH to "Recibir recordatorios y actualizaciones",
        Language.FRENCH to "Recevoir des rappels et des mises à jour",
        Language.ITALIAN to "Ricevi promemoria e aggiornamenti"
    )
    
    val settings_language_region: Map<Language, String> = mapOf(
        Language.GERMAN to "Sprache & Region",
        Language.ENGLISH to "Language & Region",
        Language.SPANISH to "Idioma y Región",
        Language.FRENCH to "Langue et Région",
        Language.ITALIAN to "Lingua e Regione"
    )
    
    val settings_language: Map<Language, String> = mapOf(
        Language.GERMAN to "Sprache",
        Language.ENGLISH to "Language",
        Language.SPANISH to "Idioma",
        Language.FRENCH to "Langue",
        Language.ITALIAN to "Lingua"
    )
    
    val settings_system_info: Map<Language, String> = mapOf(
        Language.GERMAN to "Systeminformationen anzeigen",
        Language.ENGLISH to "Show system information",
        Language.SPANISH to "Mostrar información del sistema",
        Language.FRENCH to "Afficher les informations système",
        Language.ITALIAN to "Mostra informazioni di sistema"
    )
    
    // Language Dialog
    val language_select_title: Map<Language, String> = mapOf(
        Language.GERMAN to "Sprache auswählen",
        Language.ENGLISH to "Select Language",
        Language.SPANISH to "Seleccionar Idioma",
        Language.FRENCH to "Sélectionner la Langue",
        Language.ITALIAN to "Seleziona Lingua"
    )
    
    val dialog_cancel: Map<Language, String> = mapOf(
        Language.GERMAN to "Abbrechen",
        Language.ENGLISH to "Cancel",
        Language.SPANISH to "Cancelar",
        Language.FRENCH to "Annuler",
        Language.ITALIAN to "Annulla"
    )
    
    val dialog_close: Map<Language, String> = mapOf(
        Language.GERMAN to "Schließen",
        Language.ENGLISH to "Close",
        Language.SPANISH to "Cerrar",
        Language.FRENCH to "Fermer",
        Language.ITALIAN to "Chiudi"
    )
    
    // System Info Dialog
    val system_info_title: Map<Language, String> = mapOf(
        Language.GERMAN to "Systeminformationen",
        Language.ENGLISH to "System Information",
        Language.SPANISH to "Información del Sistema",
        Language.FRENCH to "Informations Système",
        Language.ITALIAN to "Informazioni di Sistema"
    )
    
    val system_info_app_version: Map<Language, String> = mapOf(
        Language.GERMAN to "App Version",
        Language.ENGLISH to "App Version",
        Language.SPANISH to "Versión de la App",
        Language.FRENCH to "Version de l'App",
        Language.ITALIAN to "Versione App"
    )
    
    val system_info_android_version: Map<Language, String> = mapOf(
        Language.GERMAN to "Android Version",
        Language.ENGLISH to "Android Version",
        Language.SPANISH to "Versión de Android",
        Language.FRENCH to "Version Android",
        Language.ITALIAN to "Versione Android"
    )
    
    val system_info_device: Map<Language, String> = mapOf(
        Language.GERMAN to "Gerät",
        Language.ENGLISH to "Device",
        Language.SPANISH to "Dispositivo",
        Language.FRENCH to "Appareil",
        Language.ITALIAN to "Dispositivo"
    )
    
    val system_info_build: Map<Language, String> = mapOf(
        Language.GERMAN to "Build",
        Language.ENGLISH to "Build",
        Language.SPANISH to "Build",
        Language.FRENCH to "Build",
        Language.ITALIAN to "Build"
    )
    
    val system_info_kernel: Map<Language, String> = mapOf(
        Language.GERMAN to "Kernel",
        Language.ENGLISH to "Kernel",
        Language.SPANISH to "Kernel",
        Language.FRENCH to "Kernel",
        Language.ITALIAN to "Kernel"
    )
    
    val system_info_unknown: Map<Language, String> = mapOf(
        Language.GERMAN to "Unbekannt",
        Language.ENGLISH to "Unknown",
        Language.SPANISH to "Desconocido",
        Language.FRENCH to "Inconnu",
        Language.ITALIAN to "Sconosciuto"
    )
    
    // Screen Titles
    val overview_screen_title: Map<Language, String> = mapOf(
        Language.GERMAN to "Willkommen bei Grow Tracker",
        Language.ENGLISH to "Welcome to Grow Tracker",
        Language.SPANISH to "Bienvenido a Grow Tracker",
        Language.FRENCH to "Bienvenue sur Grow Tracker",
        Language.ITALIAN to "Benvenuto in Grow Tracker"
    )
    
    val grow_screen_title: Map<Language, String> = mapOf(
        Language.GERMAN to "Grow Bereich",
        Language.ENGLISH to "Grow Area",
        Language.SPANISH to "Área de Cultivo",
        Language.FRENCH to "Zone de Culture",
        Language.ITALIAN to "Area di Coltivazione"
    )
    
    // Overview Buttons
    val overview_grow_guide: Map<Language, String> = mapOf(
        Language.GERMAN to "Guide",
        Language.ENGLISH to "Grow Guide",
        Language.SPANISH to "Guía de Cultivo",
        Language.FRENCH to "Guide de Culture",
        Language.ITALIAN to "Guida alla Coltivazione"
    )
    
    val overview_placeholder_1: Map<Language, String> = mapOf(
        Language.GERMAN to "Statistik",
        Language.ENGLISH to "Statistics",
        Language.SPANISH to "Estadísticas",
        Language.FRENCH to "Statistiques",
        Language.ITALIAN to "Statistiche"
    )
    
    val overview_placeholder_2: Map<Language, String> = mapOf(
        Language.GERMAN to "Feature 2",
        Language.ENGLISH to "Feature 2",
        Language.SPANISH to "Función 2",
        Language.FRENCH to "Fonction 2",
        Language.ITALIAN to "Funzione 2"
    )
    
    // Profile Section
    val profile_login: Map<Language, String> = mapOf(
        Language.GERMAN to "Anmelden",
        Language.ENGLISH to "Login",
        Language.SPANISH to "Iniciar Sesión",
        Language.FRENCH to "Connexion",
        Language.ITALIAN to "Accedi"
    )
    
    val profile_register: Map<Language, String> = mapOf(
        Language.GERMAN to "Registrieren",
        Language.ENGLISH to "Register",
        Language.SPANISH to "Registrarse",
        Language.FRENCH to "S'inscrire",
        Language.ITALIAN to "Registrati"
    )
    
    val profile_account: Map<Language, String> = mapOf(
        Language.GERMAN to "Profil",
        Language.ENGLISH to "Profile",
        Language.SPANISH to "Perfil",
        Language.FRENCH to "Profil",
        Language.ITALIAN to "Profilo"
    )
    
    // Statistics screen strings
    val statistics_title: Map<Language, String> = mapOf(
        Language.GERMAN to "Statistiken",
        Language.ENGLISH to "Statistics",
        Language.SPANISH to "Estadísticas",
        Language.FRENCH to "Statistiques",
        Language.ITALIAN to "Statistiche"
    )
    
    val statistics_water_consumption: Map<Language, String> = mapOf(
        Language.GERMAN to "Wasserverbrauch",
        Language.ENGLISH to "Water Consumption",
        Language.SPANISH to "Consumo de Agua",
        Language.FRENCH to "Consommation d'Eau",
        Language.ITALIAN to "Consumo di Acqua"
    )
    
    val statistics_fertilizer_consumption: Map<Language, String> = mapOf(
        Language.GERMAN to "Düngerverbrauch",
        Language.ENGLISH to "Fertilizer Consumption",
        Language.SPANISH to "Consumo de Fertilizante",
        Language.FRENCH to "Consommation d'Engrais",
        Language.ITALIAN to "Consumo di Fertilizzante"
    )
    
    val statistics_power_consumption: Map<Language, String> = mapOf(
        Language.GERMAN to "Stromverbrauch",
        Language.ENGLISH to "Power Consumption",
        Language.SPANISH to "Consumo de Energía",
        Language.FRENCH to "Consommation d'Énergie",
        Language.ITALIAN to "Consumo di Energia"
    )
    
    val statistics_total_cost: Map<Language, String> = mapOf(
        Language.GERMAN to "Gesamtkosten",
        Language.ENGLISH to "Total Cost",
        Language.SPANISH to "Costo Total",
        Language.FRENCH to "Coût Total",
        Language.ITALIAN to "Costo Totale"
    )
    
    val statistics_lighting_schedule: Map<Language, String> = mapOf(
        Language.GERMAN to "Beleuchtungsplan",
        Language.ENGLISH to "Lighting Schedule",
        Language.SPANISH to "Horario de Iluminación",
        Language.FRENCH to "Programme d'Éclairage",
        Language.ITALIAN to "Programma di Illuminazione"
    )
    
    val statistics_power_settings: Map<Language, String> = mapOf(
        Language.GERMAN to "Stromeinstellungen",
        Language.ENGLISH to "Power Settings",
        Language.SPANISH to "Configuración de Energía",
        Language.FRENCH to "Paramètres d'Alimentation",
        Language.ITALIAN to "Impostazioni di Alimentazione"
    )
    
    // Grow Section
    val grow_active_growboxes: Map<Language, String> = mapOf(
        Language.GERMAN to "Aktive Growboxen",
        Language.ENGLISH to "Active Growboxes",
        Language.SPANISH to "Cajas Activas",
        Language.FRENCH to "Growbox actives",
        Language.ITALIAN to "Growbox attive"
    )
    val grow_archived_growboxes: Map<Language, String> = mapOf(
        Language.GERMAN to "Archiv",
        Language.ENGLISH to "Archive",
        Language.SPANISH to "Archivo",
        Language.FRENCH to "Archive",
        Language.ITALIAN to "Archivio"
    )
    val grow_add_growbox: Map<Language, String> = mapOf(
        Language.GERMAN to "Growbox hinzufügen",
        Language.ENGLISH to "Add Growbox",
        Language.SPANISH to "Añadir Growbox",
        Language.FRENCH to "Ajouter Growbox",
        Language.ITALIAN to "Aggiungi Growbox"
    )
    val grow_empty_active: Map<Language, String> = mapOf(
        Language.GERMAN to "Keine aktiven Growboxen",
        Language.ENGLISH to "No active growboxes",
        Language.SPANISH to "No hay cajas activas",
        Language.FRENCH to "Aucune growbox active",
        Language.ITALIAN to "Nessuna growbox attiva"
    )
    val grow_empty_archive: Map<Language, String> = mapOf(
        Language.GERMAN to "Archiv ist leer",
        Language.ENGLISH to "Archive is empty",
        Language.SPANISH to "Archivo vacío",
        Language.FRENCH to "Archive vide",
        Language.ITALIAN to "Archivio vuoto"
    )
    
    // Optional Grow Field Labels
    val grow_field_name: Map<Language, String> = mapOf(
        Language.GERMAN to "Name",
        Language.ENGLISH to "Name",
        Language.SPANISH to "Nombre",
        Language.FRENCH to "Nom",
        Language.ITALIAN to "Nome"
    )
    val grow_field_width: Map<Language, String> = mapOf(
        Language.GERMAN to "Breite (cm)",
        Language.ENGLISH to "Width (cm)",
        Language.SPANISH to "Ancho (cm)",
        Language.FRENCH to "Largeur (cm)",
        Language.ITALIAN to "Larghezza (cm)"
    )
    val grow_field_height: Map<Language, String> = mapOf(
        Language.GERMAN to "Höhe (cm)",
        Language.ENGLISH to "Height (cm)",
        Language.SPANISH to "Altura (cm)",
        Language.FRENCH to "Hauteur (cm)",
        Language.ITALIAN to "Altezza (cm)"
    )
    val grow_field_depth: Map<Language, String> = mapOf(
        Language.GERMAN to "Tiefe (cm)",
        Language.ENGLISH to "Depth (cm)",
        Language.SPANISH to "Profundidad (cm)",
        Language.FRENCH to "Profondeur (cm)",
        Language.ITALIAN to "Profondità (cm)"
    )
    val grow_field_light_type: Map<Language, String> = mapOf(
        Language.GERMAN to "Lichtart",
        Language.ENGLISH to "Light Type",
        Language.SPANISH to "Tipo de Luz",
        Language.FRENCH to "Type de lumière",
        Language.ITALIAN to "Tipo di luce"
    )
    val grow_field_light_power: Map<Language, String> = mapOf(
        Language.GERMAN to "Leistung (W)",
        Language.ENGLISH to "Power (W)",
        Language.SPANISH to "Potencia (W)",
        Language.FRENCH to "Puissance (W)",
        Language.ITALIAN to "Potenza (W)"
    )
    val grow_details_active: Map<Language, String> = mapOf(
        Language.GERMAN to "Aktiv",
        Language.ENGLISH to "Active",
        Language.SPANISH to "Activa",
        Language.FRENCH to "Active",
        Language.ITALIAN to "Attiva"
    )
    val grow_details_inactive: Map<Language, String> = mapOf(
        Language.GERMAN to "Inaktiv",
        Language.ENGLISH to "Inactive",
        Language.SPANISH to "Inactiva",
        Language.FRENCH to "Inactive",
        Language.ITALIAN to "Inattiva"
    )
}

@Composable
fun getString(stringMap: Map<Language, String>, languageManager: LanguageManager = LocalLanguageManager.current): String {
    return stringMap[languageManager.currentLanguage] ?: stringMap[Language.ENGLISH] ?: ""
}
