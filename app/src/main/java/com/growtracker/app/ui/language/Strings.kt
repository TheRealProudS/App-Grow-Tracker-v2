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

    // Search memory settings
    val settings_search_memory: Map<Language, String> = mapOf(
        Language.GERMAN to "Suchverlauf merken",
        Language.ENGLISH to "Remember search history",
        Language.SPANISH to "Recordar historial de búsqueda",
        Language.FRENCH to "Mémoriser l'historique de recherche",
        Language.ITALIAN to "Ricorda cronologia di ricerca"
    )
    val settings_search_memory_subtitle: Map<Language, String> = mapOf(
        Language.GERMAN to "Vorschläge für Hersteller & Sorten",
        Language.ENGLISH to "Suggestions for manufacturers & strains",
        Language.SPANISH to "Sugerencias de fabricantes y cepas",
        Language.FRENCH to "Suggestions pour fabricants et variétés",
        Language.ITALIAN to "Suggerimenti per produttori e varietà"
    )
    val settings_clear_search_history: Map<Language, String> = mapOf(
        Language.GERMAN to "Suchverlauf löschen",
        Language.ENGLISH to "Clear search history",
        Language.SPANISH to "Borrar historial de búsqueda",
        Language.FRENCH to "Effacer l'historique de recherche",
        Language.ITALIAN to "Cancella cronologia di ricerca"
    )
    val settings_clear_search_history_subtitle: Map<Language, String> = mapOf(
        Language.GERMAN to "Entfernt alle Vorschläge",
        Language.ENGLISH to "Removes all suggestions",
        Language.SPANISH to "Elimina todas las sugerencias",
        Language.FRENCH to "Supprime toutes les suggestions",
        Language.ITALIAN to "Rimuove tutti i suggerimenti"
    )
    val settings_cleared_toast: Map<Language, String> = mapOf(
        Language.GERMAN to "Suchverlauf gelöscht",
        Language.ENGLISH to "Search history cleared",
        Language.SPANISH to "Historial de búsqueda borrado",
        Language.FRENCH to "Historique de recherche effacé",
        Language.ITALIAN to "Cronologia di ricerca cancellata"
    )
    val data_upload_title: Map<Language, String> = mapOf(
        Language.GERMAN to "Anonyme Bild-Uploads",
        Language.ENGLISH to "Anonymous image uploads",
        Language.SPANISH to "Cargas de imágenes anónimas",
        Language.FRENCH to "Téléversements d'images anonymes",
        Language.ITALIAN to "Caricamenti di immagini anonimi"
    )
    val generic_active: Map<Language, String> = mapOf(
        Language.GERMAN to "Aktiv",
        Language.ENGLISH to "Enabled",
        Language.SPANISH to "Activado",
        Language.FRENCH to "Activé",
        Language.ITALIAN to "Attivo"
    )
    val generic_disabled: Map<Language, String> = mapOf(
        Language.GERMAN to "Deaktiviert",
        Language.ENGLISH to "Disabled",
        Language.SPANISH to "Desactivado",
        Language.FRENCH to "Désactivé",
        Language.ITALIAN to "Disattivato"
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
        Language.ENGLISH to "Guide",
        Language.SPANISH to "Guía",
        Language.FRENCH to "Guide",
        Language.ITALIAN to "Guida"
    )
    val discord_join: Map<Language, String> = mapOf(
        Language.GERMAN to "Tritt unserem Discord bei",
        Language.ENGLISH to "Join us on Discord",
        Language.SPANISH to "Únete a nuestro Discord",
        Language.FRENCH to "Rejoignez-nous sur Discord",
        Language.ITALIAN to "Unisciti a noi su Discord"
    )
    val error_cannot_open_link: Map<Language, String> = mapOf(
        Language.GERMAN to "Kann Link nicht öffnen",
        Language.ENGLISH to "Cannot open link",
        Language.SPANISH to "No se puede abrir el enlace",
        Language.FRENCH to "Impossible d'ouvrir le lien",
        Language.ITALIAN to "Impossibile aprire il link"
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
    val plant_statistics_title: Map<Language, String> = mapOf(
        Language.GERMAN to "Pflanzenstatistik",
        Language.ENGLISH to "Plant statistics",
        Language.SPANISH to "Estadísticas de la planta",
        Language.FRENCH to "Statistiques de la plante",
        Language.ITALIAN to "Statistiche della pianta"
    )
    val energy_overview_title: Map<Language, String> = mapOf(
        Language.GERMAN to "Energie (Pflanzen)",
        Language.ENGLISH to "Energy (plants)",
        Language.SPANISH to "Energía (plantas)",
        Language.FRENCH to "Énergie (plantes)",
        Language.ITALIAN to "Energia (piante)"
    )
    val statistics_empty_title: Map<Language, String> = mapOf(
        Language.GERMAN to "Keine Daten verfügbar",
        Language.ENGLISH to "No data available",
        Language.SPANISH to "No hay datos disponibles",
        Language.FRENCH to "Aucune donnée disponible",
        Language.ITALIAN to "Nessun dato disponibile"
    )
    val statistics_empty_subtitle: Map<Language, String> = mapOf(
        Language.GERMAN to "Erstelle deine erste Growbox um Statistiken zu sehen",
        Language.ENGLISH to "Create your first growbox to see statistics",
        Language.SPANISH to "Crea tu primera growbox para ver estadísticas",
        Language.FRENCH to "Créez votre première growbox pour voir les statistiques",
        Language.ITALIAN to "Crea la tua prima growbox per vedere le statistiche"
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
    val statistics_fertilizer: Map<Language, String> = mapOf(
        Language.GERMAN to "Dünger",
        Language.ENGLISH to "Fertilizer",
        Language.SPANISH to "Fertilizante",
        Language.FRENCH to "Engrais",
        Language.ITALIAN to "Fertilizzante"
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
    val power_label_light_cycle: Map<Language, String> = mapOf(
        Language.GERMAN to "Lichtzyklus",
        Language.ENGLISH to "Light cycle",
        Language.SPANISH to "Ciclo de luz",
        Language.FRENCH to "Cycle lumineux",
        Language.ITALIAN to "Ciclo di luce"
    )
    val power_label_power: Map<Language, String> = mapOf(
        Language.GERMAN to "Leistung",
        Language.ENGLISH to "Power",
        Language.SPANISH to "Potencia",
        Language.FRENCH to "Puissance",
        Language.ITALIAN to "Potenza"
    )
    val power_label_price: Map<Language, String> = mapOf(
        Language.GERMAN to "Strompreis",
        Language.ENGLISH to "Electricity price",
        Language.SPANISH to "Precio de la electricidad",
        Language.FRENCH to "Prix de l'électricité",
        Language.ITALIAN to "Prezzo dell'elettricità"
    )

    // Plant-level power section
    val power_section_title: Map<Language, String> = mapOf(
        Language.GERMAN to "Strompreis Rechner",
        Language.ENGLISH to "Electricity cost calculator",
        Language.SPANISH to "Calculadora de coste eléctrico",
        Language.FRENCH to "Calculateur de coût électrique",
        Language.ITALIAN to "Calcolatrice costo elettrico"
    )
    val power_section_subtitle: Map<Language, String> = mapOf(
        Language.GERMAN to "Preis, Watt und Licht-Zeitraum für diese Pflanze",
        Language.ENGLISH to "Price, wattage and light period for this plant",
        Language.SPANISH to "Precio, vatios y periodo de luz para esta planta",
        Language.FRENCH to "Prix, wattage et période de lumière pour cette plante",
        Language.ITALIAN to "Prezzo, wattaggio e periodo di luce per questa pianta"
    )
    val power_section_explainer: Map<Language, String> = mapOf(
        Language.GERMAN to "Berechnung: Effektive Watt = Watt × Leistungsstufe. Tages-kWh = (Effektive Watt × Stunden)/1000. Kosten = kWh × Preis. Zeiträume über Mitternacht werden unterstützt.",
        Language.ENGLISH to "Calculation: Effective watts = watts × power level. Daily kWh = (effective watts × hours)/1000. Cost = kWh × price. Periods across midnight are supported.",
        Language.SPANISH to "Cálculo: Potencia efectiva = vatios × nivel de potencia. kWh diarios = (potencia efectiva × horas)/1000. Coste = kWh × precio. Se admiten periodos que cruzan la medianoche.",
        Language.FRENCH to "Calcul : Puissance effective = watts × niveau de puissance. kWh/jour = (puissance effective × heures)/1000. Coût = kWh × prix. Les périodes chevauchant minuit sont prises en charge.",
        Language.ITALIAN to "Calcolo: Watt effettivi = watt × livello di potenza. kWh/giorno = (watt effettivi × ore)/1000. Costo = kWh × prezzo. Supportati periodi oltre la mezzanotte."
    )
    val power_label_watt: Map<Language, String> = mapOf(
        Language.GERMAN to "Watt",
        Language.ENGLISH to "Watt",
        Language.SPANISH to "Vatios",
        Language.FRENCH to "Watt",
        Language.ITALIAN to "Watt"
    )
    val power_label_light_on_from: Map<Language, String> = mapOf(
        Language.GERMAN to "Licht an von",
        Language.ENGLISH to "Light on from",
        Language.SPANISH to "Luz encendida desde",
        Language.FRENCH to "Lumière allumée de",
        Language.ITALIAN to "Luce accesa da"
    )
    val power_label_light_on_to: Map<Language, String> = mapOf(
        Language.GERMAN to "bis",
        Language.ENGLISH to "to",
        Language.SPANISH to "hasta",
        Language.FRENCH to "à",
        Language.ITALIAN to "a"
    )
    val power_label_percent: Map<Language, String> = mapOf(
        Language.GERMAN to "Leistungsstufe (%)",
        Language.ENGLISH to "Power level (%)",
        Language.SPANISH to "Nivel de potencia (%)",
        Language.FRENCH to "Niveau de puissance (%)",
        Language.ITALIAN to "Livello di potenza (%)"
    )
    val power_stats_title: Map<Language, String> = mapOf(
        Language.GERMAN to "Energie & Kosten",
        Language.ENGLISH to "Energy & costs",
        Language.SPANISH to "Energía y costes",
        Language.FRENCH to "Énergie et coûts",
        Language.ITALIAN to "Energia e costi"
    )
    val power_stats_daily_usage: Map<Language, String> = mapOf(
        Language.GERMAN to "Verbrauch pro Tag",
        Language.ENGLISH to "Usage per day",
        Language.SPANISH to "Uso por día",
        Language.FRENCH to "Consommation par jour",
        Language.ITALIAN to "Consumo al giorno"
    )
    val power_stats_today_so_far: Map<Language, String> = mapOf(
        Language.GERMAN to "Heute bisher",
        Language.ENGLISH to "Today so far",
        Language.SPANISH to "Hoy hasta ahora",
        Language.FRENCH to "Aujourd'hui jusqu'à présent",
        Language.ITALIAN to "Oggi finora"
    )
    val power_stats_total_to_date: Map<Language, String> = mapOf(
        Language.GERMAN to "Gesamt bisher",
        Language.ENGLISH to "Total to date",
        Language.SPANISH to "Total hasta la fecha",
        Language.FRENCH to "Total à ce jour",
        Language.ITALIAN to "Totale ad oggi"
    )
    val power_stats_daily_cost: Map<Language, String> = mapOf(
        Language.GERMAN to "Kosten pro Tag",
        Language.ENGLISH to "Cost per day",
        Language.SPANISH to "Coste por día",
        Language.FRENCH to "Coût par jour",
        Language.ITALIAN to "Costo al giorno"
    )
    val power_stats_weekly: Map<Language, String> = mapOf(
        Language.GERMAN to "Woche",
        Language.ENGLISH to "Week",
        Language.SPANISH to "Semana",
        Language.FRENCH to "Semaine",
        Language.ITALIAN to "Settimana"
    )
    val power_stats_monthly: Map<Language, String> = mapOf(
        Language.GERMAN to "Monat",
        Language.ENGLISH to "Month",
        Language.SPANISH to "Mes",
        Language.FRENCH to "Mois",
        Language.ITALIAN to "Mese"
    )
    val power_stats_yearly: Map<Language, String> = mapOf(
        Language.GERMAN to "Jahr",
        Language.ENGLISH to "Year",
        Language.SPANISH to "Año",
        Language.FRENCH to "Année",
        Language.ITALIAN to "Anno"
    )
    val power_stats_effective_watt: Map<Language, String> = mapOf(
        Language.GERMAN to "Effektive Leistung",
        Language.ENGLISH to "Effective power",
        Language.SPANISH to "Potencia efectiva",
        Language.FRENCH to "Puissance effective",
        Language.ITALIAN to "Potenza effettiva"
    )

    // Overall stats section
    val overall_title: Map<Language, String> = mapOf(
        Language.GERMAN to "Gesamtstatistiken",
        Language.ENGLISH to "Overall statistics",
        Language.SPANISH to "Estadísticas generales",
        Language.FRENCH to "Statistiques globales",
        Language.ITALIAN to "Statistiche complessive"
    )
    val overall_water_total: Map<Language, String> = mapOf(
        Language.GERMAN to "Wasser gesamt",
        Language.ENGLISH to "Total water",
        Language.SPANISH to "Agua total",
        Language.FRENCH to "Eau totale",
        Language.ITALIAN to "Acqua totale"
    )
    val overall_fertilizer_total: Map<Language, String> = mapOf(
        Language.GERMAN to "Dünger gesamt",
        Language.ENGLISH to "Total fertilizer",
        Language.SPANISH to "Fertilizante total",
        Language.FRENCH to "Engrais total",
        Language.ITALIAN to "Fertilizzante totale"
    )
    val overall_power_total: Map<Language, String> = mapOf(
        Language.GERMAN to "Strom gesamt",
        Language.ENGLISH to "Total power",
        Language.SPANISH to "Energía total",
        Language.FRENCH to "Énergie totale",
        Language.ITALIAN to "Energia totale"
    )
    val overall_cost_total: Map<Language, String> = mapOf(
        Language.GERMAN to "Kosten gesamt",
        Language.ENGLISH to "Total cost",
        Language.SPANISH to "Costo total",
        Language.FRENCH to "Coût total",
        Language.ITALIAN to "Costo totale"
    )

    // Model integrity section
    val model_integrity_title: Map<Language, String> = mapOf(
        Language.GERMAN to "Modell Integrität",
        Language.ENGLISH to "Model integrity",
        Language.SPANISH to "Integridad del modelo",
        Language.FRENCH to "Intégrité du modèle",
        Language.ITALIAN to "Integrità del modello"
    )
    val model_integrity_status_label: Map<Language, String> = mapOf(
        Language.GERMAN to "Status:",
        Language.ENGLISH to "Status:",
        Language.SPANISH to "Estado:",
        Language.FRENCH to "Statut :",
        Language.ITALIAN to "Stato:"
    )
    val model_integrity_warning_mismatch: Map<Language, String> = mapOf(
        Language.GERMAN to "Warnung: Hash Mismatch",
        Language.ENGLISH to "Warning: Hash mismatch",
        Language.SPANISH to "Advertencia: Hash desigual",
        Language.FRENCH to "Avertissement : Hash différent",
        Language.ITALIAN to "Avviso: Hash non corrisponde"
    )
    val model_integrity_verified: Map<Language, String> = mapOf(
        Language.GERMAN to "Verifiziert",
        Language.ENGLISH to "Verified",
        Language.SPANISH to "Verificado",
        Language.FRENCH to "Vérifié",
        Language.ITALIAN to "Verificato"
    )
    val model_integrity_unverified: Map<Language, String> = mapOf(
        Language.GERMAN to "Nicht verifiziert",
        Language.ENGLISH to "Not verified",
        Language.SPANISH to "No verificado",
        Language.FRENCH to "Non vérifié",
        Language.ITALIAN to "Non verificato"
    )
    val model_integrity_unknown: Map<Language, String> = mapOf(
        Language.GERMAN to "Unbekannt",
        Language.ENGLISH to "Unknown",
        Language.SPANISH to "Desconocido",
        Language.FRENCH to "Inconnu",
        Language.ITALIAN to "Sconosciuto"
    )
    val model_integrity_sha_prefix: Map<Language, String> = mapOf(
        Language.GERMAN to "SHA256:",
        Language.ENGLISH to "SHA256:",
        Language.SPANISH to "SHA256:",
        Language.FRENCH to "SHA256 :",
        Language.ITALIAN to "SHA256:"
    )

    // Recent scan history
    val recent_scans_title: Map<Language, String> = mapOf(
        Language.GERMAN to "Kürzliche Scans",
        Language.ENGLISH to "Recent scans",
        Language.SPANISH to "Escaneos recientes",
        Language.FRENCH to "Analyses récentes",
        Language.ITALIAN to "Scansioni recenti"
    )
    val recent_scans_empty: Map<Language, String> = mapOf(
        Language.GERMAN to "Noch keine Aufnahmen analysiert",
        Language.ENGLISH to "No captures analyzed yet",
        Language.SPANISH to "Aún no se han analizado capturas",
        Language.FRENCH to "Aucune capture analysée pour l'instant",
        Language.ITALIAN to "Nessuna acquisizione ancora analizzata"
    )
    val recent_scans_mode_prefix: Map<Language, String> = mapOf(
        Language.GERMAN to "Modus:",
        Language.ENGLISH to "Mode:",
        Language.SPANISH to "Modo:",
        Language.FRENCH to "Mode :",
        Language.ITALIAN to "Modalità:"
    )
    val recent_scans_filter_prefix: Map<Language, String> = mapOf(
        Language.GERMAN to "Filter p=",
        Language.ENGLISH to "Filter p=",
        Language.SPANISH to "Filtro p=",
        Language.FRENCH to "Filtre p=",
        Language.ITALIAN to "Filtro p="
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

    // Grow Guide – UI labels and filters
    val guide_intro_label: Map<Language, String> = mapOf(
        Language.GERMAN to "Kurzinfo",
        Language.ENGLISH to "Quick info",
        Language.SPANISH to "Info breve",
        Language.FRENCH to "Info rapide",
        Language.ITALIAN to "Info rapida"
    )
    val guide_tip_label: Map<Language, String> = mapOf(
        Language.GERMAN to "Praxis‑Tipp",
        Language.ENGLISH to "Pro tip",
        Language.SPANISH to "Consejo práctico",
        Language.FRENCH to "Astuce pratique",
        Language.ITALIAN to "Suggerimento pratico"
    )
    val guide_search_placeholder: Map<Language, String> = mapOf(
        Language.GERMAN to "Suche im Guide…",
        Language.ENGLISH to "Search the guide…",
        Language.SPANISH to "Buscar en la guía…",
        Language.FRENCH to "Rechercher dans le guide…",
        Language.ITALIAN to "Cerca nella guida…"
    )
    // Filter chip labels
    val guide_filter_equipment: Map<Language, String> = mapOf(
        Language.GERMAN to "Ausstattung",
        Language.ENGLISH to "Equipment",
        Language.SPANISH to "Equipo",
        Language.FRENCH to "Équipement",
        Language.ITALIAN to "Attrezzatura"
    )
    val guide_filter_light: Map<Language, String> = mapOf(
        Language.GERMAN to "Licht",
        Language.ENGLISH to "Light",
        Language.SPANISH to "Luz",
        Language.FRENCH to "Lumière",
        Language.ITALIAN to "Luce"
    )
    val guide_filter_climate: Map<Language, String> = mapOf(
        Language.GERMAN to "Klima",
        Language.ENGLISH to "Climate",
        Language.SPANISH to "Clima",
        Language.FRENCH to "Climat",
        Language.ITALIAN to "Clima"
    )
    val guide_filter_watering: Map<Language, String> = mapOf(
        Language.GERMAN to "Gießen",
        Language.ENGLISH to "Watering",
        Language.SPANISH to "Riego",
        Language.FRENCH to "Arrosage",
        Language.ITALIAN to "Irrigazione"
    )
    val guide_filter_fertilizer: Map<Language, String> = mapOf(
        Language.GERMAN to "Dünger",
        Language.ENGLISH to "Fertilizer",
        Language.SPANISH to "Fertilizante",
        Language.FRENCH to "Engrais",
        Language.ITALIAN to "Fertilizzante"
    )
    val guide_filter_phases: Map<Language, String> = mapOf(
        Language.GERMAN to "Phasen",
        Language.ENGLISH to "Phases",
        Language.SPANISH to "Fases",
        Language.FRENCH to "Phases",
        Language.ITALIAN to "Fasi"
    )
    val guide_filter_harvest: Map<Language, String> = mapOf(
        Language.GERMAN to "Ernte",
        Language.ENGLISH to "Harvest",
        Language.SPANISH to "Cosecha",
        Language.FRENCH to "Récolte",
        Language.ITALIAN to "Raccolto"
    )
    val guide_filter_pests: Map<Language, String> = mapOf(
        Language.GERMAN to "Schädlinge",
        Language.ENGLISH to "Pests",
        Language.SPANISH to "Plagas",
        Language.FRENCH to "Ravageurs",
        Language.ITALIAN to "Parassiti"
    )
    val guide_filter_issues: Map<Language, String> = mapOf(
        Language.GERMAN to "Probleme",
        Language.ENGLISH to "Issues",
        Language.SPANISH to "Problemas",
        Language.FRENCH to "Problèmes",
        Language.ITALIAN to "Problemi"
    )

    // Accessibility / actions
    val a11y_back: Map<Language, String> = mapOf(
        Language.GERMAN to "Zurück",
        Language.ENGLISH to "Back",
        Language.SPANISH to "Atrás",
        Language.FRENCH to "Retour",
        Language.ITALIAN to "Indietro"
    )
    val a11y_expand: Map<Language, String> = mapOf(
        Language.GERMAN to "Aufklappen",
        Language.ENGLISH to "Expand",
        Language.SPANISH to "Expandir",
        Language.FRENCH to "Développer",
        Language.ITALIAN to "Espandi"
    )
    val a11y_collapse: Map<Language, String> = mapOf(
        Language.GERMAN to "Zuklappen",
        Language.ENGLISH to "Collapse",
        Language.SPANISH to "Contraer",
        Language.FRENCH to "Réduire",
        Language.ITALIAN to "Comprimi"
    )
    val a11y_tip: Map<Language, String> = mapOf(
        Language.GERMAN to "Tipp",
        Language.ENGLISH to "Tip",
        Language.SPANISH to "Consejo",
        Language.FRENCH to "Astuce",
        Language.ITALIAN to "Suggerimento"
    )
    val a11y_info: Map<Language, String> = mapOf(
        Language.GERMAN to "Info",
        Language.ENGLISH to "Info",
        Language.SPANISH to "Info",
        Language.FRENCH to "Info",
        Language.ITALIAN to "Info"
    )
}

@Composable
fun getString(stringMap: Map<Language, String>, languageManager: LanguageManager = LocalLanguageManager.current): String {
    return stringMap[languageManager.currentLanguage] ?: stringMap[Language.ENGLISH] ?: ""
}

// Non-composable resolver for use outside @Composable contexts (e.g., click listeners, toasts)
fun resolveString(stringMap: Map<Language, String>, language: Language): String {
    return stringMap[language] ?: stringMap[Language.ENGLISH] ?: ""
}

// Grow-specific strings
object GrowStrings {
    val my_plants_title: Map<Language, String> = mapOf(
        Language.GERMAN to "Meine Pflanzen",
        Language.ENGLISH to "My plants",
        Language.SPANISH to "Mis plantas",
        Language.FRENCH to "Mes plantes",
        Language.ITALIAN to "Le mie piante"
    )
    val add_plant_button: Map<Language, String> = mapOf(
        Language.GERMAN to "Pflanze hinzufügen",
        Language.ENGLISH to "Add plant",
        Language.SPANISH to "Añadir planta",
        Language.FRENCH to "Ajouter une plante",
        Language.ITALIAN to "Aggiungi pianta"
    )
    val no_plants: Map<Language, String> = mapOf(
        Language.GERMAN to "Keine Pflanzen vorhanden",
        Language.ENGLISH to "No plants yet",
        Language.SPANISH to "No hay plantas",
        Language.FRENCH to "Aucune plante",
        Language.ITALIAN to "Nessuna pianta"
    )
    val plants_label: Map<Language, String> = mapOf(
        Language.GERMAN to "Pflanzen",
        Language.ENGLISH to "Plants",
        Language.SPANISH to "Plantas",
        Language.FRENCH to "Plantes",
        Language.ITALIAN to "Piante"
    )
    val no_plants_yet: Map<Language, String> = mapOf(
        Language.GERMAN to "Noch keine Pflanzen",
        Language.ENGLISH to "No plants yet",
        Language.SPANISH to "Aún no hay plantas",
        Language.FRENCH to "Pas encore de plantes",
        Language.ITALIAN to "Ancora nessuna pianta"
    )
    val bloom_since_prefix: Map<Language, String> = mapOf(
        Language.GERMAN to "Blüte seit",
        Language.ENGLISH to "Blooming for",
        Language.SPANISH to "Floración desde hace",
        Language.FRENCH to "Floraison depuis",
        Language.ITALIAN to "In fioritura da"
    )
    val days_suffix: Map<Language, String> = mapOf(
        Language.GERMAN to "Tagen",
        Language.ENGLISH to "days",
        Language.SPANISH to "días",
        Language.FRENCH to "jours",
        Language.ITALIAN to "giorni"
    )
    val eta_prefix: Map<Language, String> = mapOf(
        Language.GERMAN to "ca.",
        Language.ENGLISH to "approx.",
        Language.SPANISH to "aprox.",
        Language.FRENCH to "env.",
        Language.ITALIAN to "circa"
    )
    val eta_days_to_harvest: Map<Language, String> = mapOf(
        Language.GERMAN to "Tage bis Ernte",
        Language.ENGLISH to "days to harvest",
        Language.SPANISH to "días hasta la cosecha",
        Language.FRENCH to "jours avant récolte",
        Language.ITALIAN to "giorni al raccolto"
    )

    // Units and actions
    val unit_days: Map<Language, String> = mapOf(
        Language.GERMAN to "Tage",
        Language.ENGLISH to "days",
        Language.SPANISH to "días",
        Language.FRENCH to "jours",
        Language.ITALIAN to "giorni"
    )
    val to_drying_button: Map<Language, String> = mapOf(
        Language.GERMAN to "Zur Trocknung",
        Language.ENGLISH to "Start drying",
        Language.SPANISH to "Iniciar secado",
        Language.FRENCH to "Commencer le séchage",
        Language.ITALIAN to "Avvia essiccazione"
    )

    // Plant detail – chips and sections
    val chip_pot: Map<Language, String> = mapOf(
        Language.GERMAN to "Topf",
        Language.ENGLISH to "Pot",
        Language.SPANISH to "Maceta",
        Language.FRENCH to "Pot",
        Language.ITALIAN to "Vaso"
    )
    val chip_germ_day: Map<Language, String> = mapOf(
        Language.GERMAN to "Keimtag",
        Language.ENGLISH to "Germ day",
        Language.SPANISH to "Día de germinación",
        Language.FRENCH to "Jour de germination",
        Language.ITALIAN to "Giorno di germinazione"
    )
    val chip_bloom_day: Map<Language, String> = mapOf(
        Language.GERMAN to "Blütetag",
        Language.ENGLISH to "Bloom day",
        Language.SPANISH to "Día de floración",
        Language.FRENCH to "Jour de floraison",
        Language.ITALIAN to "Giorno di fioritura"
    )
    val chip_days_to_harvest: Map<Language, String> = mapOf(
        Language.GERMAN to "Bis Ernte",
        Language.ENGLISH to "To harvest",
        Language.SPANISH to "Hasta cosecha",
        Language.FRENCH to "Avant récolte",
        Language.ITALIAN to "Al raccolto"
    )
    val entries_title: Map<Language, String> = mapOf(
        Language.GERMAN to "Einträge",
        Language.ENGLISH to "Entries",
        Language.SPANISH to "Entradas",
        Language.FRENCH to "Entrées",
        Language.ITALIAN to "Registrazioni"
    )
    val harvest_label: Map<Language, String> = mapOf(
        Language.GERMAN to "Ernte",
        Language.ENGLISH to "Harvest",
        Language.SPANISH to "Cosecha",
        Language.FRENCH to "Récolte",
        Language.ITALIAN to "Raccolto"
    )
    val no_entries_today: Map<Language, String> = mapOf(
        Language.GERMAN to "Keine Einträge für diesen Tag",
        Language.ENGLISH to "No entries for this day",
        Language.SPANISH to "Sin entradas para este día",
        Language.FRENCH to "Aucune entrée pour ce jour",
        Language.ITALIAN to "Nessuna voce per questo giorno"
    )

    // Settings (plant)
    val edit_plant_title: Map<Language, String> = mapOf(
        Language.GERMAN to "Pflanze bearbeiten",
        Language.ENGLISH to "Edit plant",
        Language.SPANISH to "Editar planta",
        Language.FRENCH to "Modifier la plante",
        Language.ITALIAN to "Modifica pianta"
    )
    val preferred_fertilizer: Map<Language, String> = mapOf(
        Language.GERMAN to "Bevorzugter Dünger",
        Language.ENGLISH to "Preferred fertilizer",
        Language.SPANISH to "Fertilizante preferido",
        Language.FRENCH to "Engrais préféré",
        Language.ITALIAN to "Fertilizzante preferito"
    )
    val no_selection: Map<Language, String> = mapOf(
        Language.GERMAN to "Keine Auswahl",
        Language.ENGLISH to "No selection",
        Language.SPANISH to "Sin selección",
        Language.FRENCH to "Aucune sélection",
        Language.ITALIAN to "Nessuna selezione"
    )
    val pot_size_liters: Map<Language, String> = mapOf(
        Language.GERMAN to "Topfgröße (L)",
        Language.ENGLISH to "Pot size (L)",
        Language.SPANISH to "Tamaño de maceta (L)",
        Language.FRENCH to "Taille du pot (L)",
        Language.ITALIAN to "Dimensione vaso (L)"
    )
    val bloom_section_title: Map<Language, String> = mapOf(
        Language.GERMAN to "Blüte",
        Language.ENGLISH to "Bloom",
        Language.SPANISH to "Floración",
        Language.FRENCH to "Floraison",
        Language.ITALIAN to "Fioritura"
    )
    val bloom_not_started: Map<Language, String> = mapOf(
        Language.GERMAN to "Noch nicht in Blüte",
        Language.ENGLISH to "Not blooming yet",
        Language.SPANISH to "Aún sin floración",
        Language.FRENCH to "Pas encore en floraison",
        Language.ITALIAN to "Non ancora in fioritura"
    )
    val bloom_start_on_selected: Map<Language, String> = mapOf(
        Language.GERMAN to "Blüte starten (am ausgewählten Tag)",
        Language.ENGLISH to "Start bloom (on selected day)",
        Language.SPANISH to "Iniciar floración (día seleccionado)",
        Language.FRENCH to "Démarrer floraison (jour sélectionné)",
        Language.ITALIAN to "Avvia fioritura (giorno selezionato)"
    )
    val bloom_reset: Map<Language, String> = mapOf(
        Language.GERMAN to "Zurücksetzen",
        Language.ENGLISH to "Reset",
        Language.SPANISH to "Restablecer",
        Language.FRENCH to "Réinitialiser",
        Language.ITALIAN to "Reimposta"
    )

    // Generic buttons
    val generic_ok: Map<Language, String> = mapOf(
        Language.GERMAN to "OK",
        Language.ENGLISH to "OK",
        Language.SPANISH to "OK",
        Language.FRENCH to "OK",
        Language.ITALIAN to "OK"
    )
    val generic_cancel: Map<Language, String> = mapOf(
        Language.GERMAN to "Abbrechen",
        Language.ENGLISH to "Cancel",
        Language.SPANISH to "Cancelar",
        Language.FRENCH to "Annuler",
        Language.ITALIAN to "Annulla"
    )
    val generic_save: Map<Language, String> = mapOf(
        Language.GERMAN to "Speichern",
        Language.ENGLISH to "Save",
        Language.SPANISH to "Guardar",
        Language.FRENCH to "Enregistrer",
        Language.ITALIAN to "Salva"
    )
    val generic_delete: Map<Language, String> = mapOf(
        Language.GERMAN to "Löschen",
        Language.ENGLISH to "Delete",
        Language.SPANISH to "Eliminar",
        Language.FRENCH to "Supprimer",
        Language.ITALIAN to "Elimina"
    )
}
