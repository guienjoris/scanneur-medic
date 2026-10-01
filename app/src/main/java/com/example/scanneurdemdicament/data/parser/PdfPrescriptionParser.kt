package com.example.scanneurdemdicament.data.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.FileOutputStream

object PdfPrescriptionParser {

    fun processPdfUri(
        context: Context,
        uri: Uri,
        onSuccess: (extractedText: String, savedImagePath: String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            if (fileDescriptor == null) {
                onError("Impossible d'ouvrir le fichier PDF sélectionné.")
                return
            }

            val pdfRenderer = PdfRenderer(fileDescriptor)
            if (pdfRenderer.pageCount <= 0) {
                pdfRenderer.close()
                fileDescriptor.close()
                onError("Le fichier PDF sélectionné est vide.")
                return
            }

            val page = pdfRenderer.openPage(0)
            val renderWidth = page.width * 2
            val renderHeight = page.height * 2
            val bitmap = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888)

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            pdfRenderer.close()
            fileDescriptor.close()

            // Save rendered image to internal storage
            val savedFile = File(context.filesDir, "prescription_pdf_${System.currentTimeMillis()}.png")
            FileOutputStream(savedFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }

            // Perform OCR on rendered image
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val inputImage = InputImage.fromBitmap(bitmap, 0)

            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    onSuccess(visionText.text, savedFile.absolutePath)
                }
                .addOnFailureListener {
                    onSuccess("", savedFile.absolutePath)
                }
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e.localizedMessage ?: "Erreur lors du traitement du fichier PDF.")
        }
    }
}