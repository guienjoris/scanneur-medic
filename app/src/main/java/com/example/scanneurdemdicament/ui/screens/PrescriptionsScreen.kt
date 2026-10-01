package com.example.scanneurdemdicament.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.scanneurdemdicament.data.local.PrescriptionEntity
import com.example.scanneurdemdicament.data.parser.PdfPrescriptionParser
import com.example.scanneurdemdicament.ui.components.PrescriptionCardItem
import com.example.scanneurdemdicament.ui.components.PrescriptionDetailDialog
import com.example.scanneurdemdicament.ui.scanner.PrescriptionTextAnalyzer
import com.example.scanneurdemdicament.ui.viewmodel.PrescriptionFilter
import com.example.scanneurdemdicament.ui.viewmodel.PrescriptionViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionsScreen(
    viewModel: PrescriptionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val prescriptionsList by viewModel.prescriptionsState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var showOcrCameraView by remember { mutableStateOf(false) }
    var selectedPrescriptionForEdit by remember { mutableStateOf<PrescriptionEntity?>(null) }
    var showManualAddDialog by remember { mutableStateOf(false) }
    var pendingOcrText by remember { mutableStateOf("") }
    var pendingImagePath by remember { mutableStateOf<String?>(null) }
    var isProcessingPdf by remember { mutableStateOf(false) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            isProcessingPdf = true
            PdfPrescriptionParser.processPdfUri(
                context = context,
                uri = it,
                onSuccess = { extractedText, savedImagePath ->
                    isProcessingPdf = false
                    pendingOcrText = extractedText
                    pendingImagePath = savedImagePath
                    showManualAddDialog = true
                },
                onError = { errorMsg ->
                    isProcessingPdf = false
                    scope.launch {
                        snackbarHostState.showSnackbar(errorMsg)
                    }
                }
            )
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ordonnances & Rappels", fontWeight = FontWeight.Bold) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FloatingActionButton(
                    onClick = { showManualAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ajouter manuellement"
                    )
                }

                FloatingActionButton(
                    onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Importer PDF"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontWeight = FontWeight.Bold)
                    }
                }

                FloatingActionButton(
                    onClick = { showOcrCameraView = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scanner", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        if (showOcrCameraView) {
            // Live OCR Camera Scanner View
            PrescriptionOcrCameraView(
                onClose = { showOcrCameraView = false },
                onTextCaptured = { text ->
                    showOcrCameraView = false
                    pendingOcrText = text
                    showManualAddDialog = true
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == PrescriptionFilter.ALL,
                            onClick = { viewModel.onFilterSelected(PrescriptionFilter.ALL) },
                            label = { Text("Toutes (${prescriptionsList.size})") }
                        )
                        FilterChip(
                            selected = selectedFilter == PrescriptionFilter.TO_FETCH,
                            onClick = { viewModel.onFilterSelected(PrescriptionFilter.TO_FETCH) },
                            label = { Text("À récupérer") }
                        )
                        FilterChip(
                            selected = selectedFilter == PrescriptionFilter.FETCHED,
                            onClick = { viewModel.onFilterSelected(PrescriptionFilter.FETCHED) },
                            label = { Text("Récupérées") }
                        )
                    }

                    if (prescriptionsList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Aucune ordonnance enregistrée",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Scannez votre ordonnance papier ou importez un PDF pour enregistrer son texte et configurer un rappel.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = prescriptionsList,
                                key = { it.id }
                            ) { prescription ->
                                PrescriptionCardItem(
                                    prescription = prescription,
                                    onClick = { selectedPrescriptionForEdit = prescription },
                                    onToggleFetched = { isFetched ->
                                        viewModel.toggleFetched(context, prescription.id, isFetched)
                                    },
                                    onSetReminderClick = { selectedPrescriptionForEdit = prescription },
                                    onDeleteClick = {
                                        viewModel.deletePrescription(context, prescription.id)
                                    }
                                )
                            }
                        }
                    }
                }

                // Processing PDF Loading Overlay
                if (isProcessingPdf) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Traitement du fichier PDF & analyse OCR...",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }

        // Edit / Add Prescription Dialog
        if (selectedPrescriptionForEdit != null || showManualAddDialog) {
            PrescriptionDetailDialog(
                prescription = selectedPrescriptionForEdit,
                initialRawText = pendingOcrText,
                initialImagePath = pendingImagePath,
                onDismiss = {
                    selectedPrescriptionForEdit = null
                    showManualAddDialog = false
                    pendingOcrText = ""
                    pendingImagePath = null
                },
                onSave = { title, doctorName, rawText, imagePath, reminderTimestamp, isFetched ->
                    val targetPrescription = selectedPrescriptionForEdit
                    if (targetPrescription != null) {
                        viewModel.savePrescription(
                            context = context,
                            id = targetPrescription.id,
                            title = title,
                            doctorName = doctorName,
                            rawText = rawText ?: targetPrescription.rawText,
                            imagePath = imagePath ?: targetPrescription.imagePath,
                            reminderTimestamp = reminderTimestamp,
                            isFetched = isFetched
                        )
                    } else {
                        viewModel.savePrescription(
                            context = context,
                            title = title,
                            doctorName = doctorName,
                            rawText = rawText,
                            imagePath = imagePath,
                            reminderTimestamp = reminderTimestamp,
                            isFetched = isFetched
                        )
                    }
                    selectedPrescriptionForEdit = null
                    showManualAddDialog = false
                    pendingOcrText = ""
                    pendingImagePath = null
                }
            )
        }
    }
}

@Composable
private fun PrescriptionOcrCameraView(
    onClose: () -> Unit,
    onTextCaptured: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    var liveExtractedText by remember { mutableStateOf("") }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build().also { analysis ->
                            analysis.setAnalyzer(
                                cameraExecutor,
                                PrescriptionTextAnalyzer { fullText, _ ->
                                    liveExtractedText = fullText
                                }
                            )
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // Close Button Top
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
                .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(12.dp))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Fermer",
                tint = Color.White
            )
        }

        // Bottom OCR Live Preview Banner
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.85f), shape = RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Détection du texte de l'ordonnance :",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = liveExtractedText.ifBlank { "Pointez la caméra vers l'ordonnance..." },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                maxLines = 4
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onTextCaptured(liveExtractedText) },
                enabled = liveExtractedText.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enregistrer cette ordonnance", fontWeight = FontWeight.Bold)
            }
        }
    }
}