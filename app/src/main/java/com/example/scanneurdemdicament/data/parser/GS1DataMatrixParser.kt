package com.example.scanneurdemdicament.data.parser

data class ParsedGS1Data(
    val rawValue: String,
    val cip13: String?,
    val lotNumber: String? = null,
    val expirationDate: String? = null
)

object GS1DataMatrixParser {

    fun parse(rawValue: String): ParsedGS1Data {
        val cleanValue = rawValue.trim()

        if (cleanValue.length == 13 && cleanValue.all { it.isDigit() }) {
            return ParsedGS1Data(rawValue = cleanValue, cip13 = cleanValue)
        }

        if (cleanValue.contains("(01)")) {
            var cip13: String? = null
            var lot: String? = null
            var expDate: String? = null

            val gtinRegex = Regex("""\(01\)(\d{14})""")
            gtinRegex.find(cleanValue)?.let { match ->
                val gtin = match.groupValues[1]
                cip13 = extractCipFromGtin(gtin)
            }

            val expRegex = Regex("""\(17\)(\d{6})""")
            expRegex.find(cleanValue)?.let { match ->
                expDate = formatExpirationDate(match.groupValues[1])
            }

            val lotRegex = Regex("""\(10\)([A-Za-z0-9]+)""")
            lotRegex.find(cleanValue)?.let { match ->
                lot = match.groupValues[1]
            }

            return ParsedGS1Data(
                rawValue = rawValue,
                cip13 = cip13,
                lotNumber = lot,
                expirationDate = expDate
            )
        }

        var cip13: String? = null
        var lotNumber: String? = null
        var expirationDate: String? = null

        val workingStr = cleanValue
            .replace("\u001d", "")
            .replace("]d2", "")
            .replace("]e0", "")

        val ai01Index = workingStr.indexOf("01")
        if (ai01Index != -1 && workingStr.length >= ai01Index + 16) {
            val potentialGtin = workingStr.substring(ai01Index + 2, ai01Index + 16)
            if (potentialGtin.all { it.isDigit() }) {
                cip13 = extractCipFromGtin(potentialGtin)
            }
        }

        val ai17Index = workingStr.indexOf("17")
        if (ai17Index != -1 && workingStr.length >= ai17Index + 8) {
            val potentialExp = workingStr.substring(ai17Index + 2, ai17Index + 8)
            if (potentialExp.all { it.isDigit() }) {
                expirationDate = formatExpirationDate(potentialExp)
            }
        }

        val ai10Index = workingStr.indexOf("10")
        if (ai10Index != -1 && workingStr.length > ai10Index + 2) {
            val rest = workingStr.substring(ai10Index + 2)
            lotNumber = rest.takeWhile { it.isLetterOrDigit() }.take(20)
        }

        if (cip13 == null) {
            val cipRegex = Regex("""34009\d{8}""")
            cipRegex.find(cleanValue)?.let { match ->
                cip13 = match.value
            }
        }

        return ParsedGS1Data(
            rawValue = rawValue,
            cip13 = cip13,
            lotNumber = lotNumber,
            expirationDate = expirationDate
        )
    }

    private fun extractCipFromGtin(gtin: String): String {
        return if (gtin.startsWith("0")) {
            gtin.substring(1)
        } else {
            gtin
        }
    }

    private fun formatExpirationDate(yymmdd: String): String {
        if (yymmdd.length != 6) return yymmdd
        val yy = yymmdd.substring(0, 2)
        val mm = yymmdd.substring(2, 4)
        val dd = yymmdd.substring(4, 6)
        val fullYear = "20$yy"
        return "$dd/$mm/$fullYear"
    }
}