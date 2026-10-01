package com.example.scanneurdemdicament.ui.components

import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.scanneurdemdicament.data.parser.ParsedGS1Data
import com.example.scanneurdemdicament.ui.scanner.BarcodeAnalyzer
import java.util.concurrent.Executors

@Composable
fun CameraPreviewView(
    isFlashOn: Boolean,
    onBarcodeScanned: (ParsedGS1Data) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var camera by remember { mutableStateOf<Camera?>(null) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // Reactively enable/disable torch when camera or isFlashOn changes
    LaunchedEffect(isFlashOn, camera) {
        camera?.let { cam ->
            if (cam.cameraInfo.hasFlashUnit()) {
                cam.cameraControl.enableTorch(isFlashOn)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
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
                                BarcodeAnalyzer { parsed ->
                                    onBarcodeScanned(parsed)
                                }
                            )
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
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

        // Overlay with scanner reticle frame
        ScannerOverlay()
    }
}

@Composable
private fun ScannerOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "laserAnimation")
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserYRatio"
    )

    val frameColor = MaterialTheme.colorScheme.primary
    val cornerLength = 36f
    val strokeWidth = 8f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val boxSize = width.coerceAtMost(height) * 0.70f
        val left = (width - boxSize) / 2f
        val top = (height - boxSize) / 2.2f
        val right = left + boxSize
        val bottom = top + boxSize

        val cornerPx = 16.dp.toPx()

        // Dim background
        drawRect(
            color = Color.Black.copy(alpha = 0.55f)
        )

        // Clear transparent box for target area
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(boxSize, boxSize),
            cornerRadius = CornerRadius(cornerPx, cornerPx),
            blendMode = BlendMode.Clear
        )

        // Corner 1: Top Left
        drawLine(
            color = frameColor,
            start = Offset(left, top),
            end = Offset(left + cornerLength, top),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = frameColor,
            start = Offset(left, top),
            end = Offset(left, top + cornerLength),
            strokeWidth = strokeWidth
        )

        // Corner 2: Top Right
        drawLine(
            color = frameColor,
            start = Offset(right, top),
            end = Offset(right - cornerLength, top),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = frameColor,
            start = Offset(right, top),
            end = Offset(right, top + cornerLength),
            strokeWidth = strokeWidth
        )

        // Corner 3: Bottom Left
        drawLine(
            color = frameColor,
            start = Offset(left, bottom),
            end = Offset(left + cornerLength, bottom),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = frameColor,
            start = Offset(left, bottom),
            end = Offset(left, bottom - cornerLength),
            strokeWidth = strokeWidth
        )

        // Corner 4: Bottom Right
        drawLine(
            color = frameColor,
            start = Offset(right, bottom),
            end = Offset(right - cornerLength, bottom),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = frameColor,
            start = Offset(right, bottom),
            end = Offset(right, bottom - cornerLength),
            strokeWidth = strokeWidth
        )

        // Laser scan line
        val laserY = top + (boxSize * laserYRatio)
        drawLine(
            color = frameColor.copy(alpha = 0.85f),
            start = Offset(left + 12f, laserY),
            end = Offset(right - 12f, laserY),
            strokeWidth = 4f
        )
    }
}