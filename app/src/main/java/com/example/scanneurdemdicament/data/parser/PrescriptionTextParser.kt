package com.example.scanneurdemdicament.data.parser

data class ParsedPrescriptionInfo(
    val detectedTitle: String,
    val doctorName: String?,
    val prescriptionDate: String?
)

object PrescriptionTextParser {

    fun parseOcrText(rawText: String): ParsedPrescriptionInfo {
        if (rawText.isBlank()) {
            return ParsedPrescriptionInfo("Ordonnance", null, null)
        }

        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        var doctorName: String? = null
        var prescriptionDate: String? = null

        // Search for Doctor Name
        val docRegex = Regex("""(?i)(?:Dr|Docteur|Professeur|Pr\.?)\s+([a-zA-ZÀ-ÿ\s'-]+)""")
        for (line in lines) {
            val match = docRegex.find(line)
            if (match != null) {
                doctorName = match.groupValues[1].trim().take(30)
                break
            }
        }

        // Search for Date (e.g. 15/02/2026 or 15-02-2026)
        val dateRegex = Regex("""\b(\d{1,2}[/.-]\d{1,2}[/.-]\d{2,4})\b""")
        for (line in lines) {
            val match = dateRegex.find(line)
            if (match != null) {
                prescriptionDate = match.groupValues[1]
                break
            }
        }

        val titleBuilder = StringBuilder("Ordonnance")
        if (!doctorName.isNullOrBlank()) {
            titleBuilder.append(" - Dr $doctorName")
        } else {
            val firstLine = lines.firstOrNull { line ->
                !line.contains("ordonnance", ignoreCase = true) &&
                !line.contains("docteur", ignoreCase = true) &&
                line.length > 3
            }
            if (firstLine != null) {
                titleBuilder.append(" - ").append(firstLine.take(25))
            }
        }

        if (!prescriptionDate.isNullOrBlank()) {
            titleBuilder.append(" ($prescriptionDate)")
        }

        return ParsedPrescriptionInfo(
            detectedTitle = titleBuilder.toString(),
            doctorName = doctorName,
            prescriptionDate = prescriptionDate
        )
    }
}