package com.example.scanneurdemdicament.data.local

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromListToString(list: List<String>?): String {
        return list?.joinToString(separator = "||") ?: ""
    }

    @TypeConverter
    fun fromStringToList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split("||").filter { it.isNotBlank() }
    }
}