package com.growtracker.app.ui.ai

import android.Manifest
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.content.ContextCompat.getMainExecutor
import java.util.concurrent.Executor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import com.growtracker.app.data.ai.LeafSenseKnowledgeBase
import com.growtracker.app.data.ai.KnowledgeEntry
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
// togetherWith replaces deprecated 'with' for transitionSpec
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.growtracker.app.ui.design.LeafSenseDesignTokens
import com.growtracker.app.ui.design.overlayPillBackground
import com.growtracker.app.ui.design.GradientHeroBackground
import com.growtracker.app.data.feedback.FeedbackRepository
import com.growtracker.app.data.feedback.FeedbackRecord
import com.growtracker.app.data.feedback.FeedbackReason
import com.growtracker.app.ui.ai.TFLiteLeafSenseAnalyzer.PipelineMode
import androidx.compose.animation.ExperimentalAnimationApi

/**
 * LeafSense KI – Platzhalter Screen für zukünftige ML-Fotoanalyse.
 * Schritte (geplant):
 *  - CameraX Preview einbinden
 *  - Foto capturen & vorverarbeiten
 *  - On-Device Modell (TFLite) laden
 *  - Diagnose + Empfehlungen anzeigen
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun LeafSenseScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var lastBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var results by remember { mutableStateOf<List<LeafSenseResult>>(emptyList()) }
    var confidenceThreshold by remember { mutableStateOf(0.15f) }
    var recommendations by remember { mutableStateOf<List<LeafSenseRecommendations.Recommendation>>(emptyList()) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analyzerType by remember { mutableStateOf(AnalyzerType.DUMMY) }
    var liveMode by remember { mutableStateOf(false) }
    val contextApp = LocalContext.current.applicationContext
    var showFps by remember { mutableStateOf(true) }
    // Collect persisted preference
    LaunchedEffect(Unit) {
        LeafSensePreferences.showFpsFlow(contextApp).collect { showFps = it }
    }
    // FPS tracking
    var fps by remember { mutableStateOf(0f) }
    val frameTimestamps = remember { mutableStateListOf<Long>() }
    val dummyAnalyzer = remember { DummyLeafSenseAnalyzer() }
    val tfliteAnalyzer = remember { TFLiteLeafSenseAnalyzer(context) }
    var modelReady by remember { mutableStateOf(false) }
    var modelName by remember { mutableStateOf<String?>(null) }
    var modelVersion by remember { mutableStateOf<String?>(null) }
    var modelLoadAttempted by remember { mutableStateOf(false) }
    // Feedback state
    var showFeedbackSheet by remember { mutableStateOf(false) }
    var feedbackReason by remember { mutableStateOf<FeedbackReason?>(null) }
    var feedbackCorrectedLabel by remember { mutableStateOf<String?>(null) }
    var feedbackNote by remember { mutableStateOf("") }
    var feedbackInProgress by remember { mutableStateOf(false) }
    // Register analyzer in holder for cross-screen access (e.g., statistics screen integrity panel)
    LaunchedEffect(tfliteAnalyzer) {
        AnalyzerHolder.set(tfliteAnalyzer)
        // Warm-up attempt (non-blocking UI) – try small dummy bitmap
        kotlinx.coroutines.delay(150) // slight delay to let composition settle
        val tmp = Bitmap.createBitmap(32,32, Bitmap.Config.ARGB_8888)
        tfliteAnalyzer.warmUp(tmp)
        modelReady = tfliteAnalyzer.isModelReady()
        modelName = tfliteAnalyzer.loadedModelName()
        modelVersion = tfliteAnalyzer.modelVersion()
        modelLoadAttempted = tfliteAnalyzer.loadAttempted()
    }
    // camera selector & energy saver state
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var requestedLiveBeforeBackground by remember { mutableStateOf(false) }
    val analyzer: LeafSenseAnalyzer = when (analyzerType) {
        AnalyzerType.DUMMY -> dummyAnalyzer
        AnalyzerType.TFLITE -> tfliteAnalyzer
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val globalScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LeafSense KI", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { inner ->
        val analysisScope = rememberCoroutineScope()
        val contentPadding = 16.dp
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            // Top region: camera or permission card
            Box(Modifier.fillMaxWidth().padding(horizontal = contentPadding, vertical = 12.dp)) {
                if (!hasCameraPermission) {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Kamerazugriff benötigt", style = MaterialTheme.typography.titleMedium)
                            Text("Erteile die Berechtigung um Fotos für die Analyse aufzunehmen.")
                            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) { Text("Berechtigung anfragen") }
                        }
                    }
                } else {
                    val analysisScope2 = rememberCoroutineScope()
                    CameraCaptureSection(
                        isAnalyzing = isAnalyzing,
                        liveMode = liveMode,
                        cameraSelector = cameraSelector,
                        analyzer = analyzer,
                        showFps = showFps,
                        fps = fps,
                        lastBitmap = lastBitmap,
                        onSwitchCamera = {
                            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                        },
                        onToggleLive = { liveMode = it },
                        onLiveResult = { bmp, newResults ->
                            lastBitmap = bmp
                            results = newResults
                            recommendations = LeafSenseRecommendations.generate(newResults, confidenceThreshold)
                            if (showFps) {
                                val nowTs = System.nanoTime()
                                frameTimestamps.add(nowTs)
                                val oneSecAgo = nowTs - 1_000_000_000L
                                while (frameTimestamps.firstOrNull()?.let { it < oneSecAgo } == true) { frameTimestamps.removeAt(0) }
                                fps = frameTimestamps.size.toFloat()
                            }
                        },
                        onCaptured = { bmp ->
                            lastBitmap = bmp
                            results = emptyList()
                            recommendations = emptyList()
                            isAnalyzing = true
                            analysisScope2.launch {
                                val imageRef = LeafSenseImage.BitmapRef(bmp)
                                results = analyzer.analyze(imageRef)
                                recommendations = LeafSenseRecommendations.generate(results, confidenceThreshold)
                                isAnalyzing = false
                                // Save lightweight scan history (capture-only, not live)
                                kotlin.runCatching {
                                    val top = results.firstOrNull()
                                    val rec = com.growtracker.app.data.history.ScanRecord(
                                        timestampEpochMs = System.currentTimeMillis(),
                                        label = top?.label,
                                        confidence = top?.confidence,
                                        pipelineMode = if (analyzer is TFLiteLeafSenseAnalyzer) analyzer.pipelineMode.name else null,
                                        stage0Probability = if (analyzer is TFLiteLeafSenseAnalyzer) analyzer.lastStage0Probability() else null,
                                        modelName = if (analyzer is TFLiteLeafSenseAnalyzer) analyzer.loadedModelName() else null,
                                        modelVersion = if (analyzer is TFLiteLeafSenseAnalyzer) analyzer.modelVersion() else null,
                                    )
                                    com.growtracker.app.data.history.ScanHistoryRepository.save(contextApp, rec, bmp)
                                }
                            }
                        },
                        onError = { err -> globalScope.launch { snackbarHostState.showSnackbar(err.message ?: "Unbekannter Fehler") } }
                    )
                    // Overlay narrow control bar above preview bottom
                    Box(Modifier.matchParentSize()) {}
                }
            }

            // Scrollable analytics + results
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = contentPadding, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    ModelStatusBanner(
                        ready = modelReady,
                        modelName = modelName,
                        version = modelVersion,
                        attempted = modelLoadAttempted,
                        stage0Prob = tfliteAnalyzer.lastStage0Probability(),
                        stage0Active = tfliteAnalyzer.pipelineMode == TFLiteLeafSenseAnalyzer.PipelineMode.TWO_STAGE && tfliteAnalyzer.loadedStage0Name() != null
                    )
                }
                item {
                    // Analyzer & toggles compact
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Modus", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                SegmentedAnalyzerControl(
                                    selected = analyzerType,
                                    onSelect = { newType ->
                                        if (analyzerType != newType) {
                                            analyzerType = newType
                                            lastBitmap?.let { bmp ->
                                                isAnalyzing = true
                                                results = emptyList()
                                                analysisScope.launch {
                                                    val img = LeafSenseImage.BitmapRef(bmp)
                                                    val activeAnalyzer = when (newType) { AnalyzerType.DUMMY -> dummyAnalyzer; AnalyzerType.TFLITE -> tfliteAnalyzer }
                                                    results = activeAnalyzer.analyze(img)
                                                    recommendations = LeafSenseRecommendations.generate(results, confidenceThreshold)
                                                    isAnalyzing = false
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Live", style = MaterialTheme.typography.labelSmall)
                                    Switch(checked = liveMode, onCheckedChange = { liveMode = it })
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("FPS", style = MaterialTheme.typography.labelSmall)
                                    Switch(checked = showFps, onCheckedChange = { newVal ->
                                        showFps = newVal
                                        globalScope.launch { LeafSensePreferences.setShowFps(contextApp, newVal) }
                                    })
                                }
                                if (analyzerType == AnalyzerType.TFLITE) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Filter", style = MaterialTheme.typography.labelSmall)
                                        val isTwoStage = tfliteAnalyzer.pipelineMode == PipelineMode.TWO_STAGE
                                        Switch(checked = isTwoStage, onCheckedChange = { use ->
                                            tfliteAnalyzer.pipelineMode = if (use) PipelineMode.TWO_STAGE else PipelineMode.DIRECT
                                            // Force re-run on existing bitmap to show gating effect immediately
                                            lastBitmap?.let { bmp ->
                                                analysisScope.launch {
                                                    isAnalyzing = true
                                                    val img = LeafSenseImage.BitmapRef(bmp)
                                                    val activeAnalyzer = when (analyzerType) { AnalyzerType.DUMMY -> dummyAnalyzer; AnalyzerType.TFLITE -> tfliteAnalyzer }
                                                    results = activeAnalyzer.analyze(img)
                                                    recommendations = LeafSenseRecommendations.generate(results, confidenceThreshold)
                                                    isAnalyzing = false
                                                }
                                            }
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
                if (isAnalyzing && results.isEmpty()) {
                    item { AnalyzingShimmer() }
                }
                // Empty state when nothing analyzed yet or no confident results
                if (!isAnalyzing && results.isEmpty()) {
                    item {
                        EmptyStateCard(
                            liveMode = liveMode,
                            hasImage = lastBitmap != null,
                            onHintAction = { /* reserved for future explicit action (e.g., trigger capture) */ }
                        )
                    }
                }
                if (results.isNotEmpty()) {
                    val filtered = results.filter { it.confidence >= confidenceThreshold }
                    val topRaw = results.firstOrNull()?.label ?: ""
                    val isNoCannabis = topRaw == "(Kein Cannabisblatt erkannt)"
                    item {
                        AnimatedContent(
                            targetState = isNoCannabis,
                            transitionSpec = {
                                (fadeIn(tween(200)) + slideInVertically { it/6 }) togetherWith (fadeOut(tween(150)) + slideOutVertically { it/8 })
                            }, label = "hero_or_no_cannabis"
                        ) { noCannabis ->
                            if (noCannabis) {
                                NoCannabisCard(pipelineEnabled = tfliteAnalyzer.pipelineMode == PipelineMode.TWO_STAGE)
                            } else {
                                HeroResultCard(
                                    filtered,
                                    originalCount = results.size,
                                    threshold = confidenceThreshold,
                                    onConfirm = {
                                        val top = filtered.firstOrNull()
                                        if (top != null) {
                                            globalScope.launch(Dispatchers.IO) {
                                                val rec = FeedbackRecord(
                                                    timestampEpochMs = System.currentTimeMillis(),
                                                    analyzerModelName = modelName,
                                                    analyzerModelVersion = modelVersion,
                                                    originalLabel = top.label,
                                                    originalConfidence = top.confidence,
                                                    correctedLabel = null,
                                                    reason = null,
                                                    userNote = null,
                                                    wasConfirmed = true,
                                                    imageFileName = null,
                                                    pipelineMode = tfliteAnalyzer.pipelineMode.name,
                                                    stage0Probability = tfliteAnalyzer.lastStage0Probability()
                                                )
                                                FeedbackRepository.save(contextApp, rec, lastBitmap)
                                                snackbarHostState.showSnackbar("Feedback gespeichert (bestätigt)")
                                            }
                                        }
                                    },
                                    onMislabel = {
                                        feedbackReason = null
                                        feedbackCorrectedLabel = null
                                        feedbackNote = ""
                                        showFeedbackSheet = true
                                    }
                                )
                            }
                        }
                    }
                    if (!isNoCannabis) {
                        item { ResultChips(results = filtered) }
                        item { ResultList(results = filtered, analyzerType = analyzerType, originalCount = results.size, threshold = confidenceThreshold) }
                        item { RecommendationSection(recommendations) { recommendations = LeafSenseRecommendations.generate(results, confidenceThreshold) } }
                        val top = filtered.firstOrNull()
                        if (top != null) {
                            val kb = LeafSenseKnowledgeBase.lookup(top.label)
                            if (kb != null) {
                                item { KnowledgeCard(kb) }
                            }
                        }
                    }
                }
                item {
                    AdvancedSettingsCard(
                        threshold = confidenceThreshold,
                        showFps = showFps,
                        stage0Enabled = (analyzerType == AnalyzerType.TFLITE && tfliteAnalyzer.pipelineMode == PipelineMode.TWO_STAGE && tfliteAnalyzer.loadedStage0Name() != null),
                        stage0Threshold = tfliteAnalyzer.stage0AcceptThreshold,
                        onChangeThreshold = { newVal ->
                            confidenceThreshold = newVal
                            if (results.isNotEmpty()) recommendations = LeafSenseRecommendations.generate(results, newVal)
                        },
                        onToggleFps = { enabled ->
                            showFps = enabled
                            globalScope.launch { LeafSensePreferences.setShowFps(contextApp, enabled) }
                        },
                        onChangeStage0Threshold = { newVal ->
                            tfliteAnalyzer.stage0AcceptThreshold = newVal
                            // Optionally re-run on current image to reflect gating immediately
                            lastBitmap?.let { bmp ->
                                analysisScope.launch {
                                    isAnalyzing = true
                                    val img = LeafSenseImage.BitmapRef(bmp)
                                    val activeAnalyzer = when (analyzerType) { AnalyzerType.DUMMY -> dummyAnalyzer; AnalyzerType.TFLITE -> tfliteAnalyzer }
                                    results = activeAnalyzer.analyze(img)
                                    recommendations = LeafSenseRecommendations.generate(results, confidenceThreshold)
                                    isAnalyzing = false
                                }
                            }
                        }
                    )
                }
                item { TipsSection() }
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }

    // Prepare label options (fallback list if no results yet)
    val labelOptions = remember(results) {
        val dynamic = results.map { it.label }.distinct()
        if (dynamic.isNotEmpty()) dynamic else listOf(
            "HEALTHY","GENERAL_CHLOROSIS","NUTRIENT_DEF_N","NUTRIENT_DEF_P","NUTRIENT_DEF_K","NUTRIENT_DEF_MG","NUTRIENT_DEF_FE","OVERWATER_STRESS","UNDERWATER_STRESS","HEAT_STRESS","COLD_STRESS","LIGHT_BURN","FUNGAL_SPOTS_GENERIC","MILDEW_LIKE","LEAF_PEST_INDICATOR"
        )
    }

    val currentTopLabel = results.filter { it.confidence >= confidenceThreshold }.firstOrNull()?.label

    FeedbackBottomSheet(
        visible = showFeedbackSheet,
        currentTopLabel = currentTopLabel,
        labels = labelOptions,
        reason = feedbackReason,
        onReasonChange = { feedbackReason = it },
        corrected = feedbackCorrectedLabel,
        onCorrectedChange = { feedbackCorrectedLabel = it },
        note = feedbackNote,
        onNoteChange = { feedbackNote = it },
        onDismiss = { if (!feedbackInProgress) showFeedbackSheet = false },
        onSubmit = {
            if (feedbackInProgress) return@FeedbackBottomSheet
            val top = results.firstOrNull()
            if (top == null) { showFeedbackSheet = false; return@FeedbackBottomSheet }
            feedbackInProgress = true
            globalScope.launch(Dispatchers.IO) {
                val rec = FeedbackRecord(
                    timestampEpochMs = System.currentTimeMillis(),
                    analyzerModelName = modelName,
                    analyzerModelVersion = modelVersion,
                    originalLabel = top.label,
                    originalConfidence = top.confidence,
                    correctedLabel = feedbackCorrectedLabel,
                    reason = feedbackReason,
                    userNote = feedbackNote.ifBlank { null },
                    wasConfirmed = false,
                    imageFileName = null,
                    pipelineMode = tfliteAnalyzer.pipelineMode.name,
                    stage0Probability = tfliteAnalyzer.lastStage0Probability()
                )
                FeedbackRepository.save(contextApp, rec, lastBitmap)
                withContext(Dispatchers.Main) {
                    feedbackInProgress = false
                    showFeedbackSheet = false
                    snackbarHostState.showSnackbar("Feedback gespeichert")
                }
            }
        },
        submitting = feedbackInProgress
    )

    // Energy saver: pause live when not visible / resume if user had it active
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when(event) {
                Lifecycle.Event.ON_STOP -> {
                    if (liveMode) {
                        requestedLiveBeforeBackground = true
                        liveMode = false
                    }
                }
                Lifecycle.Event.ON_START -> {
                    if (requestedLiveBeforeBackground) {
                        liveMode = true
                        requestedLiveBeforeBackground = false
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

@Composable
private fun ResultList(results: List<LeafSenseResult>, analyzerType: AnalyzerType, originalCount: Int, threshold: Float) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Analyse (${results.size}/$originalCount ≥ ${(threshold*100).toInt()}%)", style = MaterialTheme.typography.titleMedium)
            results.forEach { r ->
                ResultRow(r)
            }
            Text(
                when(analyzerType) {
                    AnalyzerType.DUMMY -> "Demo-Ausgabe (Dummy Analyzer)."
                    AnalyzerType.TFLITE -> "TFLite-Ausgabe (Fallback falls Modell fehlt)."
                } + " Später: Empfehlungen & Verlauf.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HeroResultCard(
    results: List<LeafSenseResult>,
    originalCount: Int,
    threshold: Float,
    onConfirm: () -> Unit,
    onMislabel: () -> Unit
) {
    if (results.isEmpty()) return
    val top = results.first()
    var expanded by remember { mutableStateOf(false) }
    val gradient = GradientHeroBackground(top.category)
    val detailsAlpha by animateFloatAsState(if (expanded) 1f else 0f, label = "hero_details_alpha")
    Box(
        Modifier
            .fillMaxWidth()
            .clip(LeafSenseDesignTokens.cardShapeLarge)
            .background(gradient)
            .clickable { expanded = !expanded }
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(top.label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${(top.confidence*100).toInt()}% • ${results.size}/$originalCount ≥ ${(threshold*100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
                }
                Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.35f)) {
                    Text(if (expanded) "−" else "+", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            LinearProgressIndicator(progress = { top.confidence.coerceIn(0f,1f) }, modifier = Modifier.fillMaxWidth(), color = Color.White, trackColor = Color.White.copy(alpha=0.25f))
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Details", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha=0.9f))
                    Text("Kategorie: ${top.category}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha=0.85f))
                    Text("Threshold: ${(threshold*100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha=0.7f))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = onConfirm, label = { Text("Passt") }, leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) })
                        AssistChip(onClick = onMislabel, label = { Text("Falsch?") }, leadingIcon = { Icon(Icons.Filled.Warning, contentDescription = null) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedbackBottomSheet(
    visible: Boolean,
    currentTopLabel: String?,
    labels: List<String>,
    reason: FeedbackReason?,
    onReasonChange: (FeedbackReason?) -> Unit,
    corrected: String?,
    onCorrectedChange: (String?) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    submitting: Boolean
) {
    if (!visible) return
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Feedback zur Analyse", style = MaterialTheme.typography.titleMedium)
            Text("Top Label: ${currentTopLabel ?: "–"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Grund", style = MaterialTheme.typography.labelSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FeedbackReason.values().forEach { r ->
                    FilterChip(
                        selected = reason == r,
                        onClick = { onReasonChange(if (reason == r) null else r) },
                        label = { Text(r.name) }
                    )
                }
            }
            Text("Korrektes Label (optional)", style = MaterialTheme.typography.labelSmall)
            // Simple dropdown imitation via AssistChips (limit scope for now)
            val expandedLabels = remember { mutableStateOf(false) }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AssistChip(onClick = { expandedLabels.value = !expandedLabels.value }, label = { Text(corrected ?: "Auswählen") })
                AnimatedVisibility(visible = expandedLabels.value) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        labels.take(20).forEach { l ->
                            AssistChip(onClick = { onCorrectedChange(l); expandedLabels.value = false }, label = { Text(l) })
                        }
                        AssistChip(onClick = { onCorrectedChange(null); expandedLabels.value = false }, label = { Text("Unklar / Keins") })
                    }
                }
            }
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                label = { Text("Notiz (optional)") },
                singleLine = false,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss, enabled = !submitting) { Text("Abbrechen") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onSubmit, enabled = !submitting) { Text(if (submitting) "Speichere..." else "Speichern") }
            }
        }
    }
}

@Composable
private fun KnowledgeCard(entry: KnowledgeEntry) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val color = LeafSenseDesignTokens.categoryColor(
                    when(entry.category) {
                        com.growtracker.app.data.ai.Category.HEALTH -> LeafSenseResult.Category.HEALTH
                        com.growtracker.app.data.ai.Category.DEFICIENCY -> LeafSenseResult.Category.DEFICIENCY
                        com.growtracker.app.data.ai.Category.STRESS -> LeafSenseResult.Category.STRESS
                        com.growtracker.app.data.ai.Category.PEST -> LeafSenseResult.Category.PEST
                    }
                )
                Box(Modifier.size(12.dp).background(color, shape = CircleShape))
                Column(Modifier.weight(1f)) {
                    Text(entry.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(entry.short, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(entry.priority.name, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                }
            }
            if (entry.symptoms.isNotEmpty()) {
                Text("Symptome", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                entry.symptoms.take(3).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
            }
            if (entry.actions.isNotEmpty()) {
                Text("Aktionen", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                entry.actions.take(3).forEach { act ->
                    Text("• ${act.title}: ${act.detail}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun AdvancedSettingsCard(
    threshold: Float,
    showFps: Boolean,
    stage0Enabled: Boolean,
    stage0Threshold: Float,
    onChangeThreshold: (Float) -> Unit,
    onToggleFps: (Boolean) -> Unit,
    onChangeStage0Threshold: (Float) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Erweiterte Einstellungen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                PressScale(onClick = { expanded = !expanded }) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(if (expanded) "Schließen" else "Öffnen", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            AnimatedVisibility(expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Confidence Threshold", style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = threshold,
                            onValueChange = { onChangeThreshold(it) },
                            valueRange = 0f..0.9f,
                            steps = 8
                        )
                        Text("Aktuell: ${(threshold*100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (stage0Enabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Stage-0 Filter Schwelle", style = MaterialTheme.typography.labelSmall)
                            Slider(
                                value = stage0Threshold,
                                onValueChange = { onChangeStage0Threshold(it) },
                                valueRange = 0.3f..0.9f,
                                steps = 6
                            )
                            Text("Aktuell: ${(stage0Threshold*100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text("FPS Anzeige", style = MaterialTheme.typography.labelSmall)
                            Text("Zeigt Live-Analyse Frequenz in der Kamera.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = showFps, onCheckedChange = { onToggleFps(it) })
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationSection(
    recs: List<LeafSenseRecommendations.Recommendation>,
    onRegenerate: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.TipsAndUpdates, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Empfehlungen", style = MaterialTheme.typography.titleMedium)
                }
                TextButton(onClick = onRegenerate) { Text("Aktualisieren") }
            }
            if (recs.isEmpty()) {
                Text("Keine spezifischen Empfehlungen bei aktuellem Threshold.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                recs.forEach { r -> RecommendationRow(r) }
            }
        }
    }
}

@Composable
private fun RecommendationRow(r: LeafSenseRecommendations.Recommendation) {
    val color = when(r.priority) {
        LeafSenseRecommendations.Recommendation.Priority.HIGH -> MaterialTheme.colorScheme.error
        LeafSenseRecommendations.Recommendation.Priority.MEDIUM -> MaterialTheme.colorScheme.tertiary
        LeafSenseRecommendations.Recommendation.Priority.LOW -> MaterialTheme.colorScheme.secondary
    }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(r.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = color)
            Text(
                when(r.priority) {
                    LeafSenseRecommendations.Recommendation.Priority.HIGH -> "Hoch"
                    LeafSenseRecommendations.Recommendation.Priority.MEDIUM -> "Mittel"
                    LeafSenseRecommendations.Recommendation.Priority.LOW -> "Niedrig"
                },
                style = MaterialTheme.typography.labelSmall, color = color
            )
        }
        Text(r.message, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(4.dp))
    // Cast to float explicitly to avoid any ambiguous overload issues in some toolchains
    LinearProgressIndicator(progress = { ((r.priority.ordinal + 1).toFloat() / 3f) }, modifier = Modifier.fillMaxWidth(), color = color)
    }
}

@Composable
private fun ThresholdSlider(value: Float, onChange: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Confidence Threshold", style = MaterialTheme.typography.bodySmall)
            Text("${(value * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
        }
        Slider(
            value = value,
            onValueChange = { onChange(it) },
            valueRange = 0f..0.9f,
            steps = 8
        )
    }
}

@Composable
private fun AnalyzerSwitchRow(
    selected: AnalyzerType,
    onSelect: (AnalyzerType) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        AssistChip(
            onClick = { onSelect(AnalyzerType.DUMMY) },
            label = { Text("Dummy") },
            leadingIcon = { Icon(Icons.Filled.Build, contentDescription = null) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (selected == AnalyzerType.DUMMY) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            )
        )
        AssistChip(
            onClick = { onSelect(AnalyzerType.TFLITE) },
            label = { Text("TFLite") },
            leadingIcon = { Icon(Icons.Filled.Science, contentDescription = null) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (selected == AnalyzerType.TFLITE) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun ResultRow(result: LeafSenseResult) {
    val animatedProgress by animateFloatAsState(
        targetValue = result.confidence.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "row_confidence_anim"
    )
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(result.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text("${(animatedProgress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(4.dp))
        val tinted = LeafSenseDesignTokens.severityTint(result.category, result.confidence)
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth(),
            color = tinted
        )
    }
}

@Composable
private fun TipsSection() {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Geplante Funktionen", style = MaterialTheme.typography.titleMedium)
            }
            Bullet("Nährstoffmangel / Stress-Erkennung")
            Bullet("Schädlings- oder Pilzverdacht (Bildanalyse)")
            Bullet("Automatische Wachstumsphase")
            Bullet("Empfehlungen basierend auf bisherigen Einträgen")
            Bullet("Offline-On-Device Modell + optional Cloud-Modus")
        }
    }
}

@Composable
private fun Bullet(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun CameraCaptureSection(
    isAnalyzing: Boolean,
    liveMode: Boolean,
    cameraSelector: CameraSelector,
    analyzer: LeafSenseAnalyzer,
    showFps: Boolean,
    fps: Float,
    lastBitmap: Bitmap?,
    onSwitchCamera: () -> Unit,
    onToggleLive: (Boolean) -> Unit,
    onLiveResult: (Bitmap, List<LeafSenseResult>) -> Unit,
    onCaptured: (Bitmap) -> Unit,
    onError: (Throwable) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraProviderState = remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val analysisScope = remember { CoroutineScope(Dispatchers.Default) }
    val imageCapture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }
    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }
    val executor: Executor = remember { getMainExecutor(context) }
    var captureError by remember { mutableStateOf<Throwable?>(null) }
    var lastAnalysisTime by remember { mutableStateOf(0L) }
    var liveWorking by remember { mutableStateOf(false) }
    var intervalMs by remember { mutableStateOf(750L) }
    var lastTopLabel by remember { mutableStateOf<String?>(null) }
    var lastTopConf by remember { mutableStateOf(0f) }
    var lastFrameHash by remember { mutableStateOf<Long?>(null) }
    var focusLocked by remember { mutableStateOf(false) }
    var boundCamera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    // Tap-to-focus state
    var previewSize by remember { mutableStateOf(IntSize.Zero) }
    var lastTap by remember { mutableStateOf<Offset?>(null) }
    var focusState by remember { mutableStateOf<FocusUiState>(FocusUiState.Idle) }
    val density = LocalDensity.current
    // Clear focus indicator after delay
    LaunchedEffect(focusState) {
        if (focusState is FocusUiState.Success || focusState is FocusUiState.Failure) {
            delay(1200)
            focusState = FocusUiState.Idle
            lastTap = null
        }
    }

    LaunchedEffect(liveMode) { if (!liveMode) { liveWorking = false } }
    LaunchedEffect(cameraSelector) { liveWorking = false }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Camera container with rounded shape & subtle elevation background
        Box(
            Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(LeafSenseDesignTokens.cardShapeLarge)
                .background(Color.Black.copy(alpha = 0.15f))
                .onSizeChanged { previewSize = it }
        ) {
            AndroidView(factory = { ctx ->
                        PreviewView(ctx).apply {
                            previewViewRef = this
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            cameraProviderFuture.addListener({
                                val provider = cameraProviderFuture.get()
                                cameraProviderState.value = provider
                                Log.d("LeafSense", "CameraProvider ready - initial bind (liveMode=$liveMode)")
                                val preview = Preview.Builder().build().also { it.setSurfaceProvider(surfaceProvider) }
                                fun bind() {
                                    try {
                                        provider.unbindAll()
                                        if (liveMode) {
                                            // Set analyzer each bind to ensure active
                                            imageAnalysis.clearAnalyzer()
                                            imageAnalysis.setAnalyzer(executor) { image ->
                                                val now = System.currentTimeMillis()
                                                if (!liveWorking && now - lastAnalysisTime > intervalMs) {
                                                    liveWorking = true
                                                    lastAnalysisTime = now
                                                    try {
                                                        val hash = computeLumaHash(image)
                                                        val prev = lastFrameHash
                                                        if (prev != null && prev == hash) {
                                                            liveWorking = false
                                                            image.close()
                                                            return@setAnalyzer
                                                        }
                                                        lastFrameHash = hash
                                                        val bmp = imageProxyToBitmapScaled(image, maxDimension = 640) // already rotated
                                                        val bmpRef = LeafSenseImage.BitmapRef(bmp)
                                                        analysisScope.launch {
                                                            val res = runCatching { analyzer.analyze(bmpRef) }
                                                                .onFailure { Log.e("LeafSense", "Analyze failed", it) }
                                                                .getOrElse { emptyList() }
                                                            withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                onLiveResult(bmp, res)
                                                                val top = res.firstOrNull()
                                                                if (top != null) {
                                                                    val stable = (top.label == lastTopLabel && kotlin.math.abs(top.confidence - lastTopConf) < 0.02f)
                                                                    intervalMs = when {
                                                                        stable -> (intervalMs * 1.25f).toLong().coerceAtMost(2000L)
                                                                        else -> 600L
                                                                    }
                                                                    lastTopLabel = top.label
                                                                    lastTopConf = top.confidence
                                                                }
                                                                liveWorking = false
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.e("LeafSense", "Live frame processing error", e)
                                                        captureError = e
                                                        liveWorking = false
                                                    } finally {
                                                        image.close()
                                                    }
                                                } else {
                                                    image.close()
                                                }
                                            }
                                            boundCamera = provider.bindToLifecycle(
                                                lifecycleOwner,
                                                cameraSelector,
                                                preview,
                                                imageAnalysis,
                                                imageCapture
                                            )
                                        } else {
                                            imageAnalysis.clearAnalyzer()
                                            boundCamera = provider.bindToLifecycle(
                                                lifecycleOwner,
                                                cameraSelector,
                                                preview,
                                                imageCapture
                                            )
                                        }
                                    } catch (e: Exception) {
                                        Log.e("LeafSense", "Bind error", e)
                                        captureError = e
                                    }
                                }
                                bind()
                            }, executor)
                        }
                    }, modifier = Modifier.matchParentSize())
                    // Rebind when liveMode or camera selector changes
                    LaunchedEffect(liveMode, cameraSelector) {
                        cameraProviderState.value?.let { provider ->
                            Log.d("LeafSense", "Rebinding camera (liveMode=$liveMode, selector=${if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) "BACK" else "FRONT"})")
                            try {
                                provider.unbindAll()
                                val preview = Preview.Builder().build().also { pv ->
                                    previewViewRef?.surfaceProvider?.let { pv.setSurfaceProvider(it) }
                                }
                                if (liveMode) {
                                    imageAnalysis.clearAnalyzer()
                                    imageAnalysis.setAnalyzer(executor) { image ->
                                        val now = System.currentTimeMillis()
                                        if (!liveWorking && now - lastAnalysisTime > intervalMs) {
                                            liveWorking = true
                                            lastAnalysisTime = now
                                            try {
                                                val hash = computeLumaHash(image)
                                                if (lastFrameHash != null && lastFrameHash == hash) {
                                                    liveWorking = false
                                                    image.close(); return@setAnalyzer
                                                }
                                                lastFrameHash = hash
                                                val bmp = imageProxyToBitmapScaled(image, maxDimension = 640)
                                                val bmpRef = LeafSenseImage.BitmapRef(bmp)
                                                analysisScope.launch {
                                                    val res = runCatching { analyzer.analyze(bmpRef) }
                                                        .onFailure { Log.e("LeafSense", "Analyze failed (rebind)", it) }
                                                        .getOrElse { emptyList() }
                                                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                        onLiveResult(bmp, res)
                                                        val top = res.firstOrNull()
                                                        if (top != null) {
                                                            val stable = (top.label == lastTopLabel && kotlin.math.abs(top.confidence - lastTopConf) < 0.02f)
                                                            intervalMs = if (stable) (intervalMs * 1.25f).toLong().coerceAtMost(2000L) else 600L
                                                            lastTopLabel = top.label
                                                            lastTopConf = top.confidence
                                                        }
                                                        liveWorking = false
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                Log.e("LeafSense", "Live frame processing error (rebind)", e)
                                                captureError = e
                                                liveWorking = false
                                            } finally { image.close() }
                                        } else image.close()
                                    }
                                    boundCamera = provider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageAnalysis,
                                        imageCapture
                                    )
                                } else {
                                    imageAnalysis.clearAnalyzer()
                                    boundCamera = provider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageCapture
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e("LeafSense", "Rebind failure", e)
                                captureError = e
                            }
                        }
                    }
            if (isAnalyzing) {
                Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.35f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            // Tap overlay & reticle
            Box(
                Modifier
                    .matchParentSize()
                    .pointerInput(boundCamera, previewSize) {
                        detectTapGestures { tapOffset ->
                            val cam = boundCamera ?: return@detectTapGestures
                            val pv = previewViewRef ?: return@detectTapGestures
                            if (previewSize.width == 0 || previewSize.height == 0) return@detectTapGestures
                            lastTap = tapOffset
                            focusState = FocusUiState.Focusing
                            try {
                                val normX = tapOffset.x / previewSize.width.toFloat()
                                val normY = tapOffset.y / previewSize.height.toFloat()
                                val factory = pv.meteringPointFactory
                                val afPoint = factory.createPoint(normX, normY)
                                val action = FocusMeteringAction.Builder(afPoint, FocusMeteringAction.FLAG_AF)
                                    .setAutoCancelDuration(5, java.util.concurrent.TimeUnit.SECONDS)
                                    .build()
                                val future = cam.cameraControl.startFocusAndMetering(action)
                                future.addListener({
                                    val res = runCatching { future.get() }.getOrNull()
                                    focusState = if (res?.isFocusSuccessful == true) FocusUiState.Success else FocusUiState.Failure
                                }, ContextCompat.getMainExecutor(context))
                            } catch (_: Exception) {
                                focusState = FocusUiState.Failure
                            }
                        }
                    }
            ) {
                val tap = lastTap
                if (tap != null && focusState != FocusUiState.Idle) {
                    val color = when(focusState) {
                        FocusUiState.Idle, FocusUiState.Focusing -> MaterialTheme.colorScheme.primary
                        FocusUiState.Success -> Color(0xFF4CAF50)
                        FocusUiState.Failure -> MaterialTheme.colorScheme.error
                    }
                    // Reticle size animation when focusing
                    val baseSize = 74.dp
                    val targetScale = when(focusState) { FocusUiState.Focusing -> 0.85f; FocusUiState.Success -> 1.05f; FocusUiState.Failure -> 1.05f; else -> 1f }
                    val animatedScale by animateFloatAsState(targetValue = targetScale, label = "focus_scale")
                    Box(
                        Modifier
                            .offset { IntOffset((tap.x - (baseSize.toPx()*animatedScale)/2).toInt(), (tap.y - (baseSize.toPx()*animatedScale)/2).toInt()) }
                            .size(baseSize)
                            .scale(animatedScale)
                            .border(2.dp, color, CircleShape)
                    )
                }
            }
            // Integrated overlay controls & thumbnail with gradients and press animations
            Box(modifier = Modifier.fillMaxSize()) {
                // Gradients
                Box(Modifier.matchParentSize()) {
                    Box(Modifier.fillMaxWidth().height(90.dp).align(Alignment.TopCenter).background(
                        Brush.verticalGradient(listOf(Color.Black.copy(alpha=0.55f), Color.Transparent))
                    ))
                    Box(Modifier.fillMaxWidth().height(120.dp).align(Alignment.BottomCenter).background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha=0.65f)))
                    ))
                }
                Row(Modifier.fillMaxWidth().padding(6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    val canFocus = boundCamera != null && previewViewRef != null
                    PressScale(enabled = canFocus, onClick = {
                        val cam = boundCamera ?: return@PressScale
                        val pv = previewViewRef ?: return@PressScale
                        try {
                            val factory = pv.meteringPointFactory
                            val point = factory.createPoint(0.5f, 0.5f)
                            val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                                .setAutoCancelDuration(6, java.util.concurrent.TimeUnit.SECONDS)
                                .build()
                            cam.cameraControl.startFocusAndMetering(action)
                            focusLocked = true
                        } catch (_: Exception) {}
                    }) {
                        AssistChip(onClick = {}, enabled = canFocus, label = { Text("Fokus") }, leadingIcon = { Icon(Icons.Filled.CenterFocusStrong, contentDescription = null) })
                    }
                    AnimatedVisibility(visible = liveMode && showFps, enter = fadeIn(), exit = fadeOut()) {
                        Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.35f)) {
                            Text("${fps.toInt()} fps", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
                // Floating control pill
                Row(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .background(overlayPillBackground(), LeafSenseDesignTokens.overlayPillShape)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PressScaleIcon(onClick = onSwitchCamera) { Icon(Icons.Filled.FlipCameraAndroid, contentDescription = "Flip") }
                    PressScaleIcon(onClick = {
                        if (!isAnalyzing && !liveMode) {
                            imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                                    try {
                                        val bmp = imageProxyToBitmapScaled(image, maxDimension = 960)
                                        image.close()
                                        Log.d("LeafSense", "Captured frame ${bmp.width}x${bmp.height}")
                                        onCaptured(bmp)
                                    } catch (e: Exception) {
                                        captureError = e
                                        image.close()
                                    }
                                }
                                override fun onError(exception: ImageCaptureException) { Log.e("LeafSense", "Capture error", exception); captureError = exception }
                            })
                        }
                    }) { Icon(Icons.Filled.CameraAlt, contentDescription = "Foto") }
                    PressScale(onClick = { onToggleLive(!liveMode) }) {
                        Icon(
                            if (liveMode) Icons.Filled.CameraAlt else Icons.Filled.CameraAlt,
                            contentDescription = "Live Toggle",
                            tint = if (liveMode) MaterialTheme.colorScheme.primary else Color.White.copy(alpha=0.85f)
                        )
                    }
                    if (showFps && liveMode) {
                        Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.32f)) {
                            Text("${fps.toInt()}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
                val thumb = lastBitmap
                if (thumb != null) {
                    PressScale(onClick = {
                        val bmpRef = LeafSenseImage.BitmapRef(thumb)
                        analysisScope.launch {
                            val res = runCatching { analyzer.analyze(bmpRef) }.getOrElse { emptyList() }
                            withContext(kotlinx.coroutines.Dispatchers.Main) { onLiveResult(thumb, res) }
                        }
                    }) {
                        Image(bitmap = thumb.asImageBitmap(), contentDescription = "Letztes Bild", modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .size(60.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(Color.Black.copy(alpha = 0.25f)))
                    }
                }
            }
        }
        // (Old bottom row removed - controls now overlay)
        LaunchedEffect(captureError) {
            captureError?.let { onError(it) }
        }
    }
}

enum class AnalyzerType { DUMMY, TFLITE }

// UI state for tap focus indicator
private sealed interface FocusUiState {
    data object Idle: FocusUiState
    data object Focusing: FocusUiState
    data object Success: FocusUiState
    data object Failure: FocusUiState
}

// Lightweight luma hash for frame deduplication. Samples every 32nd pixel in both directions.
private fun computeLumaHash(image: androidx.camera.core.ImageProxy): Long {
    val proxyImage = image.image ?: return 0L
    val yPlane = proxyImage.planes[0]
    val buffer = yPlane.buffer
    val rowStride = yPlane.rowStride
    val pixelStride = yPlane.pixelStride
    val w = image.width
    val h = image.height
    var hash = 1125899906842597L // prime seed
    var y = 0
    while (y < h) {
        val rowOffset = y * rowStride
        var x = 0
        while (x < w) {
            val idx = rowOffset + x * pixelStride
            val v = buffer.get(idx).toInt() and 0xFF
            hash = hash * 31 + v
            x += 32
        }
        y += 32
    }
    return hash
}

@Composable
private fun ModelStatusBanner(
    ready: Boolean,
    modelName: String?,
    version: String?,
    attempted: Boolean,
    stage0Prob: Float? = null,
    stage0Active: Boolean = false
) {
    val color = when {
        ready -> MaterialTheme.colorScheme.primaryContainer
        attempted && !ready -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val icon = when {
        ready -> Icons.Filled.Info
        attempted && !ready -> Icons.Filled.Warning
        else -> Icons.Filled.Info
    }
    ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = color)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null)
            Column(Modifier.weight(1f)) {
                Text(
                    when {
                        ready -> "Modell geladen"
                        attempted -> "Modell nicht gefunden – Demo-Ausgabe"
                        else -> "Lade Modell..."
                    }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold
                )
                val baseDetail = when {
                    ready -> listOfNotNull(modelName, version).joinToString(" · ").ifBlank { null }
                    attempted -> "Fallback aktiv"
                    else -> null
                }
                val stageInfo = if (stage0Active) {
                    val p = stage0Prob?.let { "Filter p=${"%.2f".format(it)}" } ?: "Filter aktiv"
                    p
                } else null
                val detailCombined = listOfNotNull(baseDetail, stageInfo).joinToString(" · ").ifBlank { null }
                if (detailCombined != null) {
                    Text(detailCombined, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ResultChips(results: List<LeafSenseResult>) {
    if (results.isEmpty()) return
    val scroll = rememberScrollState()
    Row(Modifier.fillMaxWidth().horizontalScroll(scroll), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        results.take(6).forEach { r ->
            val gradient = LeafSenseDesignTokens.categoryGradient(r.category)
            Surface(
                shape = CircleShape,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .height(36.dp)
                    .background(Color.Transparent)
            ) {
                Row(
                    Modifier
                        .background(gradient, CircleShape)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(Modifier.size(10.dp).background(Color.White.copy(alpha=0.85f), shape = CircleShape))
                    Text(r.label, color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
@Composable
private fun AnalyzingShimmer() {
    val pulse = rememberInfiniteTransition(label = "shimmer")
    val alpha by pulse.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(animation = tween(900, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "shimmer_alpha"
    )
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(3) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha), shape = MaterialTheme.shapes.small)
            )
        }
    }
}

@Composable
private fun SegmentedAnalyzerControl(selected: AnalyzerType, onSelect: (AnalyzerType) -> Unit) {
    val items = listOf(AnalyzerType.DUMMY to "Demo", AnalyzerType.TFLITE to "TFLite")
    val bg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { (type, label) ->
            val active = type == selected
            val targetAlpha by animateFloatAsState(if (active) 1f else 0.35f, label = "seg_alpha")
            val targetScale by animateFloatAsState(if (active) 1f else 0.95f, label = "seg_scale")
            PressScale(onClick = { if (!active) onSelect(type) }) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            else Color.White.copy(alpha = 0.07f)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .scale(targetScale)
                ) {
                    Text(label, color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = targetAlpha), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

// --- Interaction helpers ---
@Composable
private fun PressScale(
    enabled: Boolean = true,
    scaleDown: Float = 0.9f,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (pressed) scaleDown else 1f, label = "press_scale")
    Box(
        Modifier
            .scale(scale)
            .then(if (enabled) Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        val released = try { tryAwaitRelease(); true } catch (_: Exception) { false }
                        pressed = false
                        if (released) onClick()
                    }
                )
            } else Modifier)
    ) { content() }
}

@Composable
private fun PressScaleIcon(
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    PressScale(enabled = enabled, onClick = onClick) {
        IconButton(onClick = onClick, enabled = enabled) { content() }
    }
}

@Composable
private fun EmptyStateCard(
    liveMode: Boolean,
    hasImage: Boolean,
    onHintAction: () -> Unit
) {
    // Visual: soft gradient + icon mimic using simple shapes (avoid adding vector asset now)
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circle + leaf-ish bar (placeholder minimal illustration)
            Box(Modifier.size(70.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                )
                            )
                        )
                )
                Box(
                    Modifier
                        .width(18.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp, bottomStart = 4.dp, bottomEnd = 40.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.65f))
                )
            }
            Text(
                when {
                    liveMode -> "Live aktiv – halte ein Blatt ins Sichtfeld"
                    hasImage -> "Bild erfasst – starte Auswertung oder aktiviere Live"
                    else -> "Noch keine Analyse – nimm ein Foto oder aktiviere Live-Modus"
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                "Tipps: gutes Licht, Blatt flach, Kontrast zum Hintergrund.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AssistChip(
                    onClick = onHintAction,
                    label = { Text(if (liveMode) "Stabil halten" else if (hasImage) "Erneut aufnehmen" else "Foto aufnehmen") },
                    leadingIcon = { Icon(Icons.Filled.CameraAlt, contentDescription = null) }
                )
                AssistChip(
                    onClick = onHintAction,
                    label = { Text(if (liveMode) "Live stoppen" else "Live starten") },
                    leadingIcon = { Icon(Icons.Filled.Refresh, contentDescription = null) }
                )
            }
        }
    }
}

@Composable
private fun NoCannabisCard(pipelineEnabled: Boolean) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Kein Cannabisblatt erkannt", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                if (pipelineEnabled)
                    "Der Vorfilter hat das Bild nicht als Cannabis identifiziert. Versuche: Blatt näher, zentrale Ausrichtung, neutrales Licht."
                else
                    "Aktuell wurden keine passenden Blattmerkmale erkannt. (Vorfilter ist aus)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (pipelineEnabled) {
                Text("Hinweis: Unsichere Fälle erscheinen hier als neutral statt falsche Klassifikation.", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
