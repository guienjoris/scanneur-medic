package com.example.scanneurdemdicament.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.scanneurdemdicament.data.local.PrescriptionEntity
import com.example.scanneurdemdicament.data.parser.PrescriptionTextParser
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionDetailDialog(
    prescription: PrescriptionEntity?,
    initialRawText: String = "",
    initialImagePath: String? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, doctorName: String?, rawText: String?, imagePath: String?, reminderTimestamp: Long?, isFetched: Boolean) -> Unit
) {
    val parsedOcr = remember(initialRawText) {
        if (prescription == null && initialRawText.isNotBlank()) {
            PrescriptionTextParser.parseOcrText(initialRawText)
        } else null
    }

    var title by remember {
        mutableStateOf(
            prescription?.title
                ?: parsedOcr?.detectedTitle
                ?: initialTitleFromRaw(initialRawText)
        )
    }

    var doctorName by remember {
        mutableStateOf(
            prescription?.doctorName
                ?: parsedOcr?.doctorName
                ?: ""
        )
    }

    var rawText by remember { mutableStateOf(prescription?.rawText ?: initialRawText) }
    val imagePath by remember { mutableStateOf(prescription?.imagePath ?: initialImagePath) }
    var reminderTimestamp by remember { mutableStateOf(prescription?.reminderTimestamp) }
    var isFetched by remember { mutableStateOf(prescription?.isFetched ?: false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showFullDocumentViewer by remember { mutableStateOf(false) }

    var selectedCalendar by remember {
        mutableStateOf(
            Calendar.getInstance().apply {
                reminderTimestamp?.let { timeInMillis = it }
            }
        )
    }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (prescription == null) "Nouvelle Ordonnance" else "Détails de l'Ordonnance",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre de l'ordonnance") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    label = { Text("Médecin (optionnel)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Document Preview Card if image/PDF exists
                if (!imagePath.isNullOrBlank() && File(imagePath!!).exists()) {
                    val bitmap = remember(imagePath) {
                        try {
                            BitmapFactory.decodeFile(imagePath!!)
                        } catch (_: Exception) {
                            null
                        }
                    }

                    if (bitmap != null) {
                        Text(
                            text = "Aperçu du document PDF / Scan :",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showFullDocumentViewer = true },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Aperçu du document",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                OutlinedButton(
                                    onClick = { showFullDocumentViewer = true },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "Ouvrir",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Agrandir")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Status Section (À récupérer / Récupérée)
                Text(
                    text = "Statut des médicaments :",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = !isFetched,
                        onClick = { isFetched = false },
                        label = { Text("À récupérer") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PendingActions,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    FilterChip(
                        selected = isFetched,
                        onClick = { isFetched = true },
                        label = { Text("Récupérée") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                if (rawText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Texte scanné (OCR) :",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = rawText,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reminder Section
                Text(
                    text = "Rappel de pharmacie :",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (reminderTimestamp != null && reminderTimestamp!! > System.currentTimeMillis()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dateFormat.format(Date(reminderTimestamp!!)),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { reminderTimestamp = null }) {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = "Supprimer le rappel",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (reminderTimestamp == null) "Programmer un rappel" else "Modifier la date du rappel"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        title.ifBlank { "Ordonnance" },
                        doctorName.ifBlank { null },
                        rawText.ifBlank { null },
                        imagePath,
                        reminderTimestamp,
                        isFetched
                    )
                }
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )

    // Full Document Image Viewer Dialog
    if (showFullDocumentViewer && !imagePath.isNullOrBlank() && File(imagePath!!).exists()) {
        val bitmap = remember(imagePath) {
            BitmapFactory.decodeFile(imagePath!!)
        }

        if (bitmap != null) {
            Dialog(
                onDismissRequest = { showFullDocumentViewer = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Document d'ordonnance complet",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )

                            IconButton(
                                onClick = { showFullDocumentViewer = false },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fermer",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedCalendar.timeInMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= System.currentTimeMillis() - 86400000
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { dateMillis ->
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = dateMillis
                            }
                            selectedCalendar.set(Calendar.YEAR, cal.get(Calendar.YEAR))
                            selectedCalendar.set(Calendar.MONTH, cal.get(Calendar.MONTH))
                            selectedCalendar.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH))
                            showTimePicker = true
                        }
                    }
                ) {
                    Text("Suivant")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedCalendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = selectedCalendar.get(Calendar.MINUTE)
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Choisir l'heure du rappel") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTimePicker = false
                        selectedCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        selectedCalendar.set(Calendar.MINUTE, timePickerState.minute)
                        selectedCalendar.set(Calendar.SECOND, 0)
                        reminderTimestamp = selectedCalendar.timeInMillis
                    }
                ) {
                    Text("Valider le rappel")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

private fun initialTitleFromRaw(text: String): String {
    if (text.isBlank()) return "Ordonnance"
    val firstLine = text.lines().firstOrNull { it.isNotBlank() } ?: "Ordonnance"
    return "Ordonnance - ${firstLine.take(20)}"
}