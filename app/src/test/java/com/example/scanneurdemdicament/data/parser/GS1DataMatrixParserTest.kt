package com.example.scanneurdemdicament.data.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class GS1DataMatrixParserTest {

    @Test
    fun testParseDirectCip13() {
        val raw = "3400935955838"
        val parsed = GS1DataMatrixParser.parse(raw)
        assertEquals("3400935955838", parsed.cip13)
    }

    @Test
    fun testParseParenthesizedGS1DataMatrix() {
        val raw = "(01)03400935955838(17)251231(10)LOT12345"
        val parsed = GS1DataMatrixParser.parse(raw)
        assertEquals("3400935955838", parsed.cip13)
        assertEquals("31/12/2025", parsed.expirationDate)
        assertEquals("LOT12345", parsed.lotNumber)
    }

    @Test
    fun testParseRawGS1String() {
        val raw = "01034009359558381725123110BATCHABC"
        val parsed = GS1DataMatrixParser.parse(raw)
        assertNotNull(parsed.cip13)
        assertEquals("3400935955838", parsed.cip13)
        assertEquals("31/12/2025", parsed.expirationDate)
    }
}